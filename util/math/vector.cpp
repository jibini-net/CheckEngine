#include "vector.h"

#include <math.h>

vector::vector() : vector(0.0, 0.0)
{  }

vector::vector(double x, double y)
{
	this->x = x;
	this->y = y;
}

double vector::length()
{
	return sqrt(this->x * this->x + this->y * this->y);
}

vector vector::normalize()
{
	double length = this->length();

	return vector
	{
		this->x / length,
		this->y / length
	};
}

vector vector::scale(double scale)
{
	return vector
	{
		this->x * scale,
		this->y * scale
	};
}

vector vector::add(vector other)
{
	return vector
	{
		this->x + other.x,
		this->y + other.y
	};
}