#pragma once

#include <iostream>
#include <string>

#define DEFAULT_LOGGER_PATTERN std::string("")

class logger
{
private:
	std::string name;
	std::string pattern;

public:
	logger(std::string pattern, std::string name);
	logger(std::string name);

	void log(std::string level, std::string message);
};