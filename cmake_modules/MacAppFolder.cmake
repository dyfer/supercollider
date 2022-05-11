if(APPLE)

	###### The user may set the app name, causing a standalone build
	######	cmake -D standalone:string=MyFabApp ../
	if ("${standalone}" STREQUAL "")
		set(scappbundlename ${PROJECT_NAME})
	else()
		# We're building a standalone, change the app name.
		set(scappbundlename ${standalone})
		message(STATUS "Building sc in STANDALONE mode. App name: " ${standalone})
	endif()

	set(scappcontentsdir "${scappbundlename}/${scappbundlename}.app/Contents" CACHE STRING "Installation path for the Contents dir")
	set(scappauxresourcesdir "${scappcontentsdir}/Resources" CACHE STRING "Installation path for the Resource dir")
	set(scappbindir "${scappcontentsdir}/MacOS" CACHE STRING "Installation path for the Bin dir")

	###### Allow user to select a FHS-style install
	# TODO not yet used
	option(INSTALL_FHS
		"use FHS-style install (e.g. to /usr/local) rather than to a mac-style app folder"
		FALSE)

endif(APPLE)
