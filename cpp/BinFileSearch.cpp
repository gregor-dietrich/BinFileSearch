#include <algorithm>
#include <iostream>

#include "sha1.h"

constexpr auto path = "D:/pwned-passwords/pwned-passwords-sha1-ordered-by-hash-full.txt";

uint64_t binarySearch(std::ifstream& haystack, const std::string& needle, const uint64_t start, const uint64_t end) {
	if (start > end) return 0;

	const auto middle = (start + end) / 2;
	haystack.seekg(middle, std::ios::beg);
	haystack.ignore(std::numeric_limits<std::streamsize>::max(), '\n');

	std::string line;
	std::getline(haystack, line);
	const auto hash = line.substr(0, 40);

	return needle == hash ? std::stoull(line.substr(41)) : needle < hash ? binarySearch(haystack, needle, start, middle - 1) : needle > hash ? binarySearch(haystack, needle, middle + 1, end) : -1;
}

uint64_t getCount(const std::string& needle) {
	std::ifstream haystack(path);
	if (!haystack) return -1;

	haystack.seekg(0, std::ifstream::end);
	const uint64_t size = haystack.tellg();
	haystack.seekg(0, std::ifstream::beg);

	const auto result = binarySearch(haystack, needle, 0, size);
	haystack.close();
	return result;
}

std::string getHash(const std::string& plaintext) {
	SHA1 checksum;
	checksum.update(plaintext);
	auto hash = checksum.final();
	transform(hash.begin(), hash.end(), hash.begin(), toupper);
	return hash;
}

int main(int argc, char** argv) {
	for (int i = 1; i < argc; i++) std::cout << "Password: " << argv[i] << "\n" << "Hash: " << getHash(argv[i]) << "\n" << "Count: " << getCount(getHash(argv[i])) << "\n\n";
	return EXIT_SUCCESS;
}