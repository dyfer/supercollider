#libgit2 is in subdirectory to set variables etc

unset(CMAKE_C_FLAGS)
unset(CMAKE_CXX_FLAGS)

message(STATUS "Using bundled libgit2")

set(BUILD_SHARED_LIBS OFF CACHE BOOL "Libgit2: build static library" FORCE)
set(BUILD_CLI OFF CACHE BOOL "Libgit2: build cli" FORCE)
set(BUILD_EXAMPLES OFF CACHE BOOL "Libgit2: build examples" FORCE)
set(BUILD_TESTS OFF CACHE BOOL "Libgit2: build tests" FORCE)

add_subdirectory(libgit2 EXCLUDE_FROM_ALL)

# manually create static libgit2 
# this is needed in particular for Xcode
# where static libs are not created by default

# Add resource information on Windows
# this is what libgit2package does internally, but we can't get this info from there
if(WIN32 AND NOT CYGWIN)
	set(SRC_RC "${CMAKE_CURRENT_SOURCE_DIR}/libgit2/src/libgit2/git2.rc")
endif()

# Create static libgit2
add_library(libgit2_static STATIC 
	dummy.c 
	$<TARGET_OBJECTS:libgit2>
	$<TARGET_OBJECTS:util>
	$<TARGET_OBJECTS:llhttp>
	$<TARGET_OBJECTS:ntlmclient>
	$<TARGET_OBJECTS:xdiff>
	${SRC_RC}
)

target_link_libraries(libgit2_static ${LIBGIT2_SYSTEM_LIBS})

set_target_properties(libgit2_static PROPERTIES
	C_STANDARD 90
	OUTPUT_NAME git2
)

add_library(libgit2::libgit2 ALIAS libgit2_static)
