#include "logger.h"

#include <ctime>

#define LOGGER_PATTERN std::string(\
"\033[0;37m%d{yyyy-MM-dd} %t{hh:mm:ss} \033[1;36m[%5p\033[1;36m]\
[\033[1;37m%-15c\033[1;36m]\033[0;37m: %m%n\033[0m")


logger::logger(std::string pattern, std::string name)
{
	this->pattern = pattern;
	this->name = name;
}

logger::logger(std::string name) : logger::logger(LOGGER_PATTERN, name)
{ }

void print(std::string message, int width, bool left_justify)
{
	if (!left_justify)
		std::cout << message.substr(0, width == 0 ? message.length() : width);
	for (int i = 0; i < width - (int)message.length(); i++)
		std::cout << " ";
	if (left_justify)
		std::cout << message.substr(0, width == 0 ? message.length() : width);
}

void logger::log(std::string level, std::string level_color, std::string message)
{
	auto pattern_c = pattern.c_str();
	auto now_c = time(nullptr);

	bool within_brackets = false;
	bool within_token = false;

	bool is_left_justify = false;
	int num_digits_accum = 0;

	auto date_time = localtime(&now_c);

	char prev_command = ' ';
	char prev_digit = ' ';

	int num_digit_count = 0;

	for (int i = 0; i < pattern.length(); i++)
	{
		char c = pattern_c[i];

		if (within_brackets)
		{
			if (c == prev_digit)
				num_digit_count++;
			else
			{
				num_digit_count = 0;

				now_c = time(nullptr);
				date_time = localtime(&now_c);

				date_time->tm_year += 1900;
				date_time->tm_mon += 1;
			}

			switch (c)
			{
			case 'y':
				std::cout << date_time->tm_year / (int)pow(10, 3 - num_digit_count);
				date_time->tm_year %= (int)pow(10, 3 - num_digit_count);
				break;
			case 'M':
				std::cout << date_time->tm_mon / (int)pow(10, 1 - num_digit_count);
				date_time->tm_mon %= (int)pow(10, 1 - num_digit_count);
				break;
			case 'd':
				std::cout << date_time->tm_mday / (int)pow(10, 1 - num_digit_count);
				date_time->tm_mday %= (int)pow(10, 1 - num_digit_count);
				break;

			case 'h':
				std::cout << date_time->tm_hour / (int)pow(10, 1 - num_digit_count);
				date_time->tm_hour %= (int)pow(10, 1 - num_digit_count);
				break;
			case 'm':
				std::cout << date_time->tm_min / (int)pow(10, 1 - num_digit_count);
				date_time->tm_min %= (int)pow(10, 1 - num_digit_count);
				break;
			case 's':
				std::cout << date_time->tm_sec / (int)pow(10, 1 - num_digit_count);
				date_time->tm_sec %= (int)pow(10, 1 - num_digit_count);
				break;

			case '}' :
				within_brackets = false;
				within_token = false;
				break;

			default:
				std::cout << c;
			}

			prev_digit = c;
		} else if (within_token)
		{
			switch (c)
			{
			case '{':
				within_brackets = true;
				break;
		
			case 'd':
			case 't':
				break;

			case 'm':
				print(message, num_digits_accum, is_left_justify);
				within_token = false;
				break;
			case 'c':
				print(name, num_digits_accum, is_left_justify);
				within_token = false;
				break;
			case 'n':
				std::cout << std::endl;
				within_token = false;
				break;
			case 'p':
				std::cout << level_color;
				print(level, num_digits_accum, is_left_justify);
				within_token = false;
				break;

			case '-':
				is_left_justify = true;
				break;

			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				num_digits_accum *= 10;
				num_digits_accum += c - '0';
				break;

			default:
				within_token = false;
			}

			prev_command = c;
		} else
		{
			switch (c)
			{
			case '%':
				within_token = true;
				is_left_justify = false;
				num_digits_accum = 0;
				break;

			default:
				std::cout << c;
			}
		}
	}
}

void logger::debug(std::string message)
{
	this->log("DEBUG", "\033[1;34m", message);
}

void logger::info(std::string message)
{
	this->log("INFO", "\033[1;32m", message);
}

void logger::warn(std::string message)
{
	this->log("WARN", "\033[1;33m", message);
}

void logger::error(std::string message)
{
	this->log("ERROR", "\033[1;31m", message);
}