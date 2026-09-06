#pragma once

#include <functional>
#include <memory>
#include "parser.h"

namespace config {

    class parser_registry {
        public:
        using factory_fn = std::function<std::unique_ptr<parser>()>;

        static parser_registry& instance();

        void register_parser(std::string_view extension_or_format, factory_fn factory);
        [[nodiscard]] std::unique_ptr<parser> create_parser(std::string_view format) const;

        private:
        std::unordered_map<std::string, factory_fn> factories_;
    };
}