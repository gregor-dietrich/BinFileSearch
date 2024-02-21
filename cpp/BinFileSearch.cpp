#include <algorithm>
#include <filesystem>

#include "sha1.h"

uint64_t binarySearch(std::ifstream& haystack, const std::string& needle, const uint64_t start, const uint64_t end) {
	if (start > end) return 0;

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

int main(int argc, char** argv) {
	if (argc < 3) {
		std::cerr << "Error: " << argc << " arguments provided, but at least 3 are required! Arguments found: \n";
		for (int i = 0; i < argc; i++) {
			std::cerr << i << '\t' << argv[i] << '\n';
		}
		return EXIT_FAILURE;
	}

	const auto path = argv[1];

	if (!std::filesystem::exists(path)) {
		std::cerr << "Error: File does not exist: " << path << '\n';
		return EXIT_FAILURE;
	}

	if (!std::filesystem::is_regular_file(path)) {
		std::cerr << "Error: " << path << " is not a file!\n";
		return EXIT_FAILURE;
	}

	std::ifstream haystack;
	try {
		haystack.open(path);

		if (!haystack.good()) {
			throw std::runtime_error("Error: Couldn't create input file stream.");
		}

		for (int i = 2; i < argc; i++) {
			std::cout << "Password: " << argv[i] << '\n' << "Hash: " << getHash(argv[i]) << '\n'
			<< "Count: " << getCount(haystack, getHash(argv[i])) << "\n\n";
		}
	} catch (const std::runtime_error& err) {
		std::cerr << err.what() << '\n';
		return EXIT_FAILURE;
	}

	if (haystack.is_open()) {
		haystack.close();
	}

	return EXIT_SUCCESS;
}