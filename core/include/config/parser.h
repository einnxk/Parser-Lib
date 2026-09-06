#pragma once

#include "expected"
#include "value.h"
#include "error.h"

namespace config {

    class parser {
        public:

        virtual ~parser() = default;

        [[nodiscard]]
        virtual std::expected<value, error> parse_string(std::string_view content) = 0;

        [[nodiscard]]
        virtual std::expected<value, error> parse_file(std::string_view path) = 0;
    };
}