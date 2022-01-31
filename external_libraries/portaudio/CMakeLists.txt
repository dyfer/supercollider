# we put portaudio_submodule in a subdirectory in order to disable flags and definitions
unset(CMAKE_C_FLAGS)
unset(CMAKE_CXX_FLAGS)
if(WIN32)
    remove_definitions(-DWIN32_LEAN_AND_MEAN -DNOMINMAX)
endif()

# set(PA_BUILD_STATIC ON CACHE BOOL "PortAudio static library" FORCE)
# set(PA_BUILD_SHARED OFF CACHE BOOL "PortAudio shared library" FORCE)

# disable platform suffix
# set(PA_LIBNAME_ADD_SUFFIX OFF CACHE BOOL "PortAudio static library suffix" FORCE)
# disable install target
# set(PA_DISABLE_INSTALL ON CACHE BOOL "Disable PortAudio install" FORCE)

if(APPLE)
    set(PA_USE_JACK OFF CACHE BOOL "Disable Jack backend" FORCE)
endif()

add_subdirectory(portaudio_submodule "${CMAKE_BINARY_DIR}/portaudio")

if(WIN32 AND NOT PA_USE_ASIO)
    message(STATUS "Building PortAudio WITHOUT support for ASIO")
endif()

# when building PA ourselves, we need to specify PORTAUDIO_INCLUDE_DIRS and PORTAUDIO_LIBRARIES
set(PORTAUDIO_INCLUDE_DIRS "${CMAKE_CURRENT_SOURCE_DIR}/portaudio_submodule/include" CACHE PATH "Portaudio include directory" FORCE)
# set(PORTAUDIO_LIBRARIES portaudio_static CACHE PATH "PORTAUDIO_LIBRARIES" FORCE)
set(PORTAUDIO_LIBRARIES "${CMAKE_BINARY_DIR}/portaudio/$<CONFIG>/libportaudio.dylib" CACHE PATH "PORTAUDIO_LIBRARIES" FORCE)
# list(APPEND CMAKE_PREFIX_PATH "${CMAKE_CURRENT_BINARY_DIR}/portaudio_submodule/${CMAKE_BUILD_TYPE}/")
# list(APPEND CMAKE_PREFIX_PATH "${CMAKE_BINARY_DIR}/portaudio/RelWithDebInfo")
# set(CMAKE_PREFIX_PATH "${CMAKE_BINARY_DIR}/portaudio/RelWithDebInfo/;${CMAKE_PREFIX_PATH}" PARENT_SCOPE)
# set(CMAKE_PREFIX_PATH "${CMAKE_BINARY_DIR}/portaudio/RelWithDebInfo/;${CMAKE_PREFIX_PATH}")
# list(APPEND CMAKE_PREFIX_PATH "${CMAKE_BINARY_DIR}/portaudio/RelWithDebInfo")
# message("CMAKE_PREFIX_PATH: " ${CMAKE_PREFIX_PATH})
