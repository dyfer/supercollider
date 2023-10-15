set(VCPKG_TARGET_ARCHITECTURE x86)
set(VCPKG_CRT_LINKAGE dynamic)
set(VCPKG_LIBRARY_LINKAGE dynamic)
set(VCPKG_BUILD_TYPE release)
if(PORT MATCHES "readline") # only specific libraries are build as shared
    set(VCPKG_LIBRARY_LINKAGE static)
endif()
