#pragma once

struct vector
{
	double x;

	double y;


	vector();

	vector(double x, double y);


	double length();

	vector normalize();

	vector scale(double scale);

	vector add(vector other);
};