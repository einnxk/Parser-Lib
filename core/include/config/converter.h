#pragma once

#include <expected>
#include "value.h"

namespace config {

    template<typename T>
    struct converter {
        static std::expected<T, error> from_value(const value& val);
        static value to_value(const T& obj);
    };
}
