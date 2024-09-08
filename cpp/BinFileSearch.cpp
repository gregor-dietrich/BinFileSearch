//#define _PWNEDPASSWORDCHECKER_PERFORMANCE_MODE

#include <algorithm>
#include <chrono>
#include <filesystem>
#include <functional>
#include <sstream>

#include "sha1.h"

size_t binarySearch(std::ifstream& haystack, const std::string& needle, const size_t start, const size_t end) {
	if (start > end) {
		return 0;
	}
#ifdef _PWNEDPASSWORDCHECKER_PERFORMANCE_MODE
	const
#endif
	auto middle = (start + end) / 2;
	haystack.seekg(middle, std::ios::beg);

#ifdef _PWNEDPASSWORDCHECKER_PERFORMANCE_MODE
	haystack.ignore(std::numeric_limits<std::streamsize>::max(), '\n');
#else
	char ch;
	while (middle > start && haystack.get(ch) && ch != '\n') {
		haystack.seekg(--middle, std::ios::beg);
	}
#endif

	std::string line;
	std::getline(haystack, line);

	const auto hash = line.substr(0, needle.length());
	return needle == hash ? std::stoull(line.substr(needle.length() + 1)) : 
			needle < hash ? binarySearch(haystack, needle, start, middle - 1) : 
				/* needle > hash ? */ binarySearch(haystack, needle, middle + 1, end);
}

size_t getCount(std::ifstream& haystack, const std::string& needle) {
	haystack.seekg(0, std::ifstream::end); // Forward to eof
	const size_t size = haystack.tellg();
	haystack.seekg(0, std::ifstream::beg); // Rewind to start
#ifdef _PWNEDPASSWORDCHECKER_PERFORMANCE_MODE
	// handle edge case where needle is the first line
	std::string line;
	std::getline(haystack, line);
	const auto hash = line.substr(0, needle.length());
	if (needle == hash) {
		return std::stoull(line.substr(needle.length() + 1));
	}
#endif
	return binarySearch(haystack, needle, 0, size);
}

std::string getHash(const std::string& plaintext) {
	SHA1 checksum;
	checksum.update(plaintext);
	auto hash = checksum.final();
	transform(hash.begin(), hash.end(), hash.begin(), toupper);
	return hash;
}

size_t benchmark(std::function<void()> func) {
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

	std::ostringstream ss;
	for (int i = 2; i < argc; i++) {
		const auto hash = getHash(argv[i]);
		ss << "Password: " << argv[i] << "\nHash: " << hash
		   << "\nCount: " << getCount(fileStream, hash) << "\n\n";
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
	size_t coldResult = 0;
	try {
		coldResult = benchmark([&]() { searchPasswords(argc, argv); });
	}
	catch (const std::runtime_error& err) {
		std::cerr << err.what() << '\n';
		return EXIT_FAILURE;
	}

	// warm runs
	const size_t runs = 1000;
	size_t warmResult = 0;
	try {
		for (int i = 0; i < runs; i++) {
			warmResult += benchmark([&]() { searchPasswords(argc, argv); });
		}
	}
	catch (const std::runtime_error& err) {
		std::cerr << err.what() << '\n';
		return EXIT_FAILURE;
	}

	std::cout << "Cold run time for " << argc - 2 << " passwords: " << coldResult << " microseconds (1 run)\n"
			  << "Average time taken for " << argc - 2 << " passwords: " << warmResult / runs << " microseconds (" + std::to_string(runs) + " runs)\n";
		
	return EXIT_SUCCESS;
}