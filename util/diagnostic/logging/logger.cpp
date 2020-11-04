#include "logger.h"

logger::logger(std::string pattern, std::string name)
{
	this->pattern = pattern;
	this->name = name;
}

logger::logger(std::string name) : logger::logger(name, DEFAULT_LOGGER_PATTERN)
{ }

void logger::log(std::string level, std::string message)
{
	std::string mutate = this->pattern;
}