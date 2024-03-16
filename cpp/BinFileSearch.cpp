#include <algorithm>
#include <chrono>
#include <filesystem>
#include <functional>
#include <sstream>

#include "sha1.h"

uint64_t binarySearch(std::ifstream& haystack, const std::string& needle, const uint64_t start, const uint64_t end) {
	if (start > end) {
		return 0;
	}

	const auto middle = (start + end) / 2;
	haystack.seekg(middle, std::ios::beg);
	haystack.ignore(std::numeric_limits<std::streamsize>::max(), '\n');

	std::string line;
	std::getline(haystack, line);

	if (line.length() < needle.length() + 2 || line[needle.length()] != ':') {
		throw std::runtime_error("Error parsing line: " + line);
	}

	const auto hash = line.substr(0, needle.length());
	return needle == hash ? std::stoull(line.substr(needle.length() + 1)) : 
			needle < hash ? binarySearch(haystack, needle, start, middle - 1) : 
				/* needle > hash ? */ binarySearch(haystack, needle, middle + 1, end);
}

uint64_t getCount(std::ifstream& haystack, const std::string& needle) {
	haystack.seekg(0, std::ifstream::end); // Forward to eof
	const uint64_t size = haystack.tellg();
	haystack.seekg(0, std::ifstream::beg); // Rewind to start
	return binarySearch(haystack, needle, 0, size);
}

std::string getHash(const std::string& plaintext) {
	SHA1 checksum;
	checksum.update(plaintext);
	auto hash = checksum.final();
	transform(hash.begin(), hash.end(), hash.begin(), toupper);
	return hash;
}

long long benchmark(std::function<void()> func) {
	auto start = std::chrono::high_resolution_clock::now();
	func();
	auto duration = std::chrono::duration_cast<std::chrono::microseconds>(std::chrono::high_resolution_clock::now() - start);
	return duration.count();
}

void searchPasswords(int argc, char** argv) {
	std::ifstream fileStream;
	fileStream.open(argv[1]);

	if (!fileStream.good()) {
		throw std::runtime_error("Error: Couldn't create input file stream.");
	}

	std::stringstream ss;
	for (int i = 2; i < argc; i++) {
		const auto hash = getHash(argv[i]);
		ss << "Password: " << argv[i] << '\n' << "Hash: " << getHash(hash) << '\n'
		   << "Count: " << getCount(fileStream, hash) << "\n\n";
	}
	std::cout << ss.str() << '\n';

	if (fileStream.is_open()) {
		fileStream.close();
	}
}

int main(int argc, char** argv) {
	if (argc < 3) {
		std::cerr << "Error: " << argc << " arguments provided, but at least 3 are required! Arguments found: \n";
		for (int i = 0; i < argc; i++) {
			std::cerr << i << '\t' << argv[i] << '\n';
		}
		return EXIT_FAILURE;
	}

	if (!std::filesystem::exists(argv[1])) {
		std::cerr << "Error: File does not exist: " << argv[1] << '\n';
		return EXIT_FAILURE;
	}

	if (!std::filesystem::is_regular_file(argv[1])) {
		std::cerr << "Error: " << argv[1] << " is not a file!\n";
		return EXIT_FAILURE;
	}

	// cold runs
	long long coldResult = 0;
	try {
		coldResult = benchmark([&]() { searchPasswords(argc, argv); });
	}
	catch (const std::runtime_error& err) {
		std::cerr << err.what() << '\n';
		return EXIT_FAILURE;
	}

	// warm runs
	const int runs = 1000;
	long long warmResult = 0;
	for (int i = 0; i < runs; i++) {
		try {
			warmResult += benchmark([&]() { searchPasswords(argc, argv); });
		}
		catch (const std::runtime_error& err) {
			std::cerr << err.what() << '\n';
			return EXIT_FAILURE;
		}
	}

	std::cout << "Cold run time for 10 passwords: " << coldResult << " microseconds (1 run)\n"
			  << "Average time taken for 10 passwords: " << warmResult / runs << " microseconds (" + std::to_string(runs) + " runs)\n";
		
	return EXIT_SUCCESS;
}