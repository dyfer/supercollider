/**
 *  \file SC_Codecvt.hpp
 *
 *  \brief Codepage conversion utilities.
 *
 *  This header provides convenience functions to convert strings and paths between UTF-8 and the OS's
 *  filesystem API encoding. On POSIX systems, this does nothing. On Windows, this converts between
 *  UTF-8 and UTF-16. On Windows, additional functions are provided to convert between UTF-8 std::string
 *  and UTF-16 std::wstring.
 *
 *  $Author: Brian Heim $
 *
 *  \version 1.1
 *
 *  $Date: 2017-05-14 $
 *
 *  $Contact: brianlheim@gmail.com $
 *
 *  Created on: 2017-05-14
 */
/*
 *  Copyright (C) Brian Heim, 2017. All rights reserved.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *  02110-1301 USA
 */
#pragma once

#include <string> // string
#include <filesystem>

#ifdef _WIN32
#    include <windows.h> // MultiByteToWideChar, WideCharToMultiByte
#endif

/** \namespace SC_Codecvt
 *  \brief Namespace for codepage conversion functions. */
namespace SC_Codecvt {

// Windows helper functions.
// Only defined on Windows since they require windows.h.
#ifdef _WIN32

/** \brief Converts a UTF-8 char str to UTF-16 std::wstring
 *
 * This function is only defined on Windows, to avoid unnecessary header includes.
 * If invalid utf8 is provided, it will be replaced with U+FFFD. */
inline std::wstring utf8_cstr_to_utf16_wstring(const char* s) noexcept {
    if (!s || *s == '\0')
        return {};

    int required_size = MultiByteToWideChar(CP_UTF8, 0, s, -1, nullptr, 0);

    if (required_size <= 1)
        return {};

    std::wstring result(static_cast<std::size_t>(required_size - 1), L'\0');
    MultiByteToWideChar(CP_UTF8, 0, s, -1, result.data(), required_size);
    return result;
}

/** \brief Converts a UTF-16 wchar_t str to UTF-8 std::string
 *
 * This function is only defined on Windows, to avoid unnecessary header includes. */
inline std::string utf16_wcstr_to_utf8_string(const wchar_t* s) noexcept {
    if (!s || *s == L'\0')
        return {};

    int required_size = WideCharToMultiByte(CP_UTF8, 0, s, -1, nullptr, 0, nullptr, nullptr);

    if (required_size <= 1)
        return {};

    std::string result(static_cast<std::size_t>(required_size - 1), '\0');
    WideCharToMultiByte(CP_UTF8, 0, s, -1, result.data(), required_size, nullptr, nullptr);
    return result;
}

#endif // _WIN32

/** \brief Converts a path to a UTF-8 encoded string.
 *
 * On POSIX platforms, this just converts using .string(). On Windows, uses
 * conversion between UTF-16 and UTF-8. */
inline std::string path_to_utf8_str(const std::filesystem::path& p) { return p.u8string(); }

/** \brief Converts a UTF-8 encoded string to a path.
 *
 * On POSIX platforms, this converts using the default constructor. On Windows,
 * uses conversion between UTF-16 and UTF-8. */
inline std::filesystem::path utf8_str_to_path(const std::string& s) { return std::filesystem::u8path(s); }

} // SC_Codecvt
