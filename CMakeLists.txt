cmake_minimum_required (VERSION 3.8)

project ("CheckEngine")

# Export to root and library directories on build
set (CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR})
set (CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
set (CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)

# Include source roots from project and GLFW/GLEW
include_directories (${PROJECT_SOURCE_DIR})
include_directories (${PROJECT_SOURCE_DIR}/glfw/include)
include_directories (${PROJECT_SOURCE_DIR}/glew/include)

# Main engine executable and game bootstrapper
add_executable (CheckEngine
	"CheckEngine.cpp"
)

# System OpenGL library (must be installed)
find_package (OpenGL REQUIRED)
target_link_libraries (CheckEngine OpenGL::GL)
# GLFW (download source into project directory)
add_subdirectory ("glfw")
target_link_libraries (CheckEngine glfw ${GLFW_LIBRARIES})
# GLEW (download source into project directory)
add_subdirectory ("glew/build/cmake")
target_link_libraries (CheckEngine glew ${GLEW_LIBRARIES})

# Engine performance and status profiling
add_subdirectory ("util/diagnostic")
target_link_libraries (CheckEngine check_diagnostic)
# Engine intrinsic design and singletons
add_subdirectory ("util/intrinsics")
target_link_libraries (CheckEngine check_intrinsics)
# Engine collections and sorting algorithms
add_subdirectory ("util/collection")
target_link_libraries (CheckEngine check_collection)
# Engine vector math and math utilities
add_subdirectory ("util/math")
target_link_libraries (CheckEngine check_math)
# GLFW and OpenGL context management
add_subdirectory ("graphics/context")
target_link_libraries (CheckEngine check_context)