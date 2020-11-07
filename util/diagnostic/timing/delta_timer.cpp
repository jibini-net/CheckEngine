#include "delta_timer.h"

#include "GLFW/glfw3.h"

delta_timer::delta_timer()
{
	this->update();
	this->reset();
}

void delta_timer::update()
{
	this->current_time = glfwGetTime();
}

void delta_timer::reset()
{
	this->last_call = current_time;
}

double delta_timer::delta_time()
{
	return this->current_time - this->last_call;
}