# verify app but provide paths to search for the libraries in (e.g. inside the app bundle)
#get_bundle_keys("${app}" "${libs}" "${dirs}" keys IGNORE_ITEM "${CFG_IGNORE_ITEM}")

cmake_policy(PUSH)
cmake_policy(SET CMP0057 NEW) # if IN_LIST

include(BundleUtilities)

function(gp_file_type_rpaths original_file file type_var rpaths)
  if(NOT IS_ABSOLUTE "${original_file}")
    message(STATUS "warning: gp_file_type expects absolute full path for first arg original_file")
  endif()

  get_filename_component(exepath "${original_file}" PATH)

  set(type "")
  gp_resolved_file_type("${original_file}" "${file}" "${exepath}" "" type "${rpaths}")

  set(${type_var} "${type}" PARENT_SCOPE)
endfunction()

function(verify_bundle_prerequisites_dirs bundle result_var info_var dirs)
  set(result 1)
  set(info "")
  set(count 0)

  set(options)
  set(oneValueArgs)
  set(multiValueArgs IGNORE_ITEM)
  cmake_parse_arguments(CFG "${options}" "${oneValueArgs}" "${multiValueArgs}" ${ARGN} )

  get_bundle_main_executable("${bundle}" main_bundle_exe)

  get_bundle_all_executables("${bundle}" file_list)
  foreach(f ${file_list})
      get_filename_component(exepath "${f}" PATH)
      math(EXPR count "${count} + 1")

      message(STATUS "executable file ${count}: ${f}")

      set(prereqs "")
      get_filename_component(prereq_filename ${f} NAME)

      if(NOT prereq_filename IN_LIST CFG_IGNORE_ITEM)
        get_item_rpaths(${f} _main_exe_rpaths)
        # list(APPEND _main_exe_rpaths "${dirs}")
        # message(STATUS "_main_exe_rpaths: " "${_main_exe_rpaths}")
        get_prerequisites("${f}" prereqs 1 1 "${exepath}" "${_main_exe_rpaths}")

        # On the Mac,
        # "embedded" and "system" prerequisites are fine... anything else means
        # the bundle's prerequisites are not verified (i.e., the bundle is not
        # really "standalone")
        #
        # On Windows (and others? Linux/Unix/...?)
        # "local" and "system" prereqs are fine...
        #

        set(external_prereqs "")

        foreach(p ${prereqs})
          set(p_type "")
          gp_file_type("${f}" "${p}" p_type "${dirs}")

          if(APPLE)
            if(NOT p_type STREQUAL "embedded" AND NOT p_type STREQUAL "system")
              set(external_prereqs ${external_prereqs} "${p}")
            endif()
          else()
            if(NOT p_type STREQUAL "local" AND NOT p_type STREQUAL "system")
              set(external_prereqs ${external_prereqs} "${p}")
            endif()
          endif()
        endforeach()

        if(external_prereqs)
          # Found non-system/somehow-unacceptable prerequisites:
          set(result 0)
          set(info ${info} "external prerequisites found:\nf='${f}'\nexternal_prereqs='${external_prereqs}'\n")
        endif()
      else()
        message(STATUS "Ignoring file: ${prereq_filename}")
      endif()
  endforeach()

  if(result)
    set(info "Verified ${count} executable files in '${bundle}'")
  endif()

  set(${result_var} "${result}" PARENT_SCOPE)
  set(${info_var} "${info}" PARENT_SCOPE)
endfunction()

function(verify_app_dirs app dirs)
  set(verified 0)
  set(info "")

  message(STATUS "verify_app_dirs")
  message(STATUS "  app='${app}'")
  message(STATUS "  dirs='${dirs}'")

  set(options)
  set(oneValueArgs)
  set(multiValueArgs IGNORE_ITEM)
  cmake_parse_arguments(CFG "${options}" "${oneValueArgs}" "${multiValueArgs}" ${ARGN} )

  get_bundle_and_executable("${app}" bundle executable valid)

  message(STATUS "===========================================================================")
  message(STATUS "Analyzing app='${app}'")
  message(STATUS "bundle='${bundle}'")
  message(STATUS "executable='${executable}'")
  message(STATUS "valid='${valid}'")

  # Verify that the bundle does not have any "external" prerequisites:
  #
  verify_bundle_prerequisites_dirs("${bundle}" verified info "${dirs}" IGNORE_ITEM "${CFG_IGNORE_ITEM}")
  message(STATUS "verified='${verified}'")
  message(STATUS "info='${info}'")
  message(STATUS "")

  if(verified)
    # Verify that the bundle does not have any symlinks to external files:
    #
    verify_bundle_symlinks("${bundle}" verified info)
    message(STATUS "verified='${verified}'")
    message(STATUS "info='${info}'")
    message(STATUS "")
  endif()

  if(NOT verified)
    message(FATAL_ERROR "error: verify_app_dirs failed")
  endif()
endfunction()

cmake_policy(POP)