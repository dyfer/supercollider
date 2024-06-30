#libgit2 is in subdirectory to set variables etc

unset(CMAKE_C_FLAGS)
unset(CMAKE_CXX_FLAGS)

message(STATUS "Using bundled libgit2")

set(BUILD_SHARED_LIBS OFF CACHE BOOL "Libgit2: build static library" FORCE)
set(BUILD_CLI OFF CACHE BOOL "Libgit2: build cli" FORCE)
set(BUILD_EXAMPLES OFF CACHE BOOL "Libgit2: build examples" FORCE)
set(BUILD_TESTS OFF CACHE BOOL "Libgit2: build tests" FORCE)
set(REGEX_BACKEND builtin CACHE STRING "Libgit2: use builtin reqex backend" FORCE) # to avoid system more dependencies for now
if(WIN32)
	set(USE_BUNDLED_ZLIB ON CACHE BOOL "Libgit2: use bundled zlib" FORCE)
endif()

add_subdirectory(libgit2 EXCLUDE_FROM_ALL)

# we don't want to force C standard from libgit2 on the whole project
unset(CMAKE_C_STANDARD CACHE)
