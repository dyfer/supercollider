#include "SC_Codecvt.hpp"
#ifdef _WIN32
#    include <windows.h>
#endif

namespace SC_Codecvt {

#ifdef _WIN32

std::wstring utf8_cstr_to_utf16_wstring(const char* s) noexcept {
    if (!s || *s == '\0')
        return {};

    int required_size = MultiByteToWideChar(CP_UTF8, 0, s, -1, nullptr, 0);

    if (required_size <= 1)
        return {};

    std::wstring result(static_cast<std::size_t>(required_size - 1), L'\0');
    MultiByteToWideChar(CP_UTF8, 0, s, -1, result.data(), required_size);
    return result;
}

std::string utf16_wcstr_to_utf8_string(const wchar_t* s) noexcept {
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

std::string path_to_utf8_str(const std::filesystem::path& p) { return p.u8string(); }

std::filesystem::path utf8_str_to_path(const std::string& s) { return std::filesystem::u8path(s); }

} // SC_Codecvt
