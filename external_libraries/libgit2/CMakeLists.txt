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

# manually create static libgit2 
# this is needed in particular for Xcode
# where static libs are not created by default

# Add resource information on Windows
# this is what libgit2package does internally, but we can't get this info from there
if(WIN32 AND NOT MINGW)
	set(SRC_RC "${CMAKE_CURRENT_SOURCE_DIR}/libgit2/src/libgit2/git2.rc")
endif()

# Create static libgit2
add_library(libgit2_static STATIC 
	dummy.c 
	$<TARGET_OBJECTS:libgit2>
	$<TARGET_OBJECTS:util>
	$<TARGET_OBJECTS:llhttp>
	$<TARGET_OBJECTS:pcre>
	$<$<NOT:$<BOOL:${WIN32}>>:$<TARGET_OBJECTS:ntlmclient>>
	$<$<BOOL:${WIN32}>:$<TARGET_OBJECTS:zlib>>
	$<TARGET_OBJECTS:xdiff>
	${SRC_RC}
)

target_include_directories(libgit2_static PUBLIC $<TARGET_PROPERTY:libgit2,INTERFACE_INCLUDE_DIRECTORIES>)

target_compile_definitions(libgit2_static PUBLIC $<TARGET_PROPERTY:libgit2,INTERFACE_COMPILE_DEFINITIONS>)

target_link_libraries(libgit2_static ${LIBGIT2_SYSTEM_LIBS})

set_target_properties(libgit2_static PROPERTIES
	C_STANDARD 90
	OUTPUT_NAME git2
)

add_library(libgit2::libgit2 ALIAS libgit2_static)
