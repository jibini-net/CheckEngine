#pragma once

class delta_timer
{
protected:
	double last_call;

	double current_time;

public:
	delta_timer();


	void update();

	void reset();


	double delta_time();
};