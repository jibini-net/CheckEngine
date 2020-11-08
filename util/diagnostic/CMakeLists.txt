cmake_minimum_required (VERSION 3.8)

add_library (check_diagnostic STATIC 
	"logging/logger.cpp"
	
	"timing/delta_timer.cpp"
)