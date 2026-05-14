# - Find sndfile
# Search in vcpkg, pkgconfig, and system locations.
# Create unified target Sndfile::sndfile

if(DEFINED ENV{VCPKG_ROOT})
    find_package(sndfile CONFIG QUIET)
endif()

if(NOT TARGET Sndfile::sndfile)
    if(TARGET sndfile::sndfile)
        add_library(Sndfile::sndfile ALIAS sndfile::sndfile)
    elseif(TARGET sndfile)
        add_library(Sndfile::sndfile ALIAS sndfile)
    endif()
endif()

# if sndfile was not found in vcpkg
if(NOT TARGET Sndfile::sndfile)

    # check pkgconfig first
    find_package(PkgConfig QUIET)
    if(PKG_CONFIG_FOUND)
        pkg_check_modules(PC_SNDFILE QUIET sndfile)
    endif()

    find_path(SNDFILE_INCLUDE_DIR
        NAMES sndfile.h
        HINTS
            ${PC_SNDFILE_INCLUDEDIR}
            ${PC_SNDFILE_INCLUDE_DIRS}
            "/usr/local/opt/libsndfile/include"
            "$ENV{PROGRAMFILES\(X86\)}/libsndfile/include"
            "$ENV{ProgramFiles}/libsndfile/include"
        PATHS
            /usr/local/include
            /usr/include
    )

    find_library(SNDFILE_LIBRARY
        NAMES sndfile sndfile-1 libsndfile libsndfile-1 libsndfile.dylib libsndfile.a
        HINTS
            ${PC_SNDFILE_LIBDIR}
            ${PC_SNDFILE_LIBRARY_DIRS}
            "/usr/local/opt/libsndfile/lib"
            "$ENV{PROGRAMFILES\(X86\)}/libsndfile/lib"
            "$ENV{PROGRAMFILES\(X86\)}/libsndfile/bin"
            "$ENV{ProgramFiles}/libsndfile/lib"
            "$ENV{ProgramFiles}/libsndfile/bin"
        PATHS
            /usr/local/lib
            /usr/lib
    )

    if(WIN32)
        find_program(SNDFILE_RUNTIME_DLL
            NAMES libsndfile.dll libsndfile-1.dll sndfile.dll
            HINTS
                "$ENV{PROGRAMFILES\(X86\)}/libsndfile/bin"
                "$ENV{ProgramFiles}/libsndfile/bin"
        )
    endif()

    include(FindPackageHandleStandardArgs)
    find_package_handle_standard_args(Sndfile
        REQUIRED_VARS SNDFILE_LIBRARY SNDFILE_INCLUDE_DIR
    )

    if(NOT SNDFILE_FOUND)
        message(FATAL_ERROR "Could not find sndfile.")
    endif()

    # create the unified target
    add_library(Sndfile::sndfile UNKNOWN IMPORTED)
    
    # configure the target properties, including the DLL if found
    if(WIN32 AND SNDFILE_RUNTIME_DLL)
        set_target_properties(Sndfile::sndfile PROPERTIES
            IMPORTED_LOCATION "${SNDFILE_RUNTIME_DLL}"
            IMPORTED_IMPLIB   "${SNDFILE_LIBRARY}"
            INTERFACE_INCLUDE_DIRECTORIES "${SNDFILE_INCLUDE_DIR}"
        )
    else()
        set_target_properties(Sndfile::sndfile PROPERTIES
            IMPORTED_LOCATION "${SNDFILE_LIBRARY}"
            INTERFACE_INCLUDE_DIRECTORIES "${SNDFILE_INCLUDE_DIR}"
        )
    endif()

    if(PC_SNDFILE_CFLAGS_OTHER)
        set_target_properties(Sndfile::sndfile PROPERTIES
            INTERFACE_COMPILE_OPTIONS "${PC_SNDFILE_CFLAGS_OTHER}"
        )
    endif()

    mark_as_advanced(SNDFILE_INCLUDE_DIR SNDFILE_LIBRARY SNDFILE_RUNTIME_DLL)
endif()

# check for format support
# note: this only check whehter libsndfile's _version_ supports the given format
# it does not check whether support for that format was actually compiled into the library

include(CMakePushCheckState)
cmake_push_check_state(RESET)

get_target_property(SNDFILE_TARGET_INCLUDES Sndfile::sndfile INTERFACE_INCLUDE_DIRECTORIES)
set(CMAKE_REQUIRED_INCLUDES ${SNDFILE_TARGET_INCLUDES})

include(CheckCSourceCompiles)
check_c_source_compiles("
    # include <sndfile.h>
    int main() {return SF_FORMAT_VORBIS;}
  "
  SNDFILE_HAS_VORBIS
)

check_c_source_compiles("
    # include <sndfile.h>
    int main() {return SF_FORMAT_OPUS;}
  "
  SNDFILE_HAS_OPUS
)

check_c_source_compiles("
    # include <sndfile.h>
    int main() {return SF_FORMAT_MPEG;}
  "
  SNDFILE_HAS_MPEG
)

cmake_pop_check_state()
