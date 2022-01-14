# this CMake script outputs the value of the SC_VERSION variable
# note: this should be run from the top level directory
# command: cmake -P cmake_modules/GetSCVersion.cmake
include(SCVersion.txt)
execute_process(COMMAND ${CMAKE_COMMAND} -E echo "${SC_VERSION}")
