#pragma once

#include <iostream>
#include <string>

class logger
{
private:
	std::string name;
	std::string pattern;

public:
	logger(std::string pattern, std::string name);
	logger(std::string name);


	void log(std::string level, std::string level_color, std::string message);


	void debug(std::string message);

	void info(std::string message);

	void warn(std::string message);

	void error(std::string message);
};