#pragma once

#include <cstddef>
#include <string>

namespace config {

    enum class error_reason {
        InvalidSyntax,
        InvalidType,
        MissingField,
        UnknownField,
        InvalidValue,
        ValidationFailed,
        InternalError
    };

    enum class error_category {
        Parse,
        Type,
        Validation,
        Internal
    };

    struct error_location {
        std::size_t line = 0;
        std::size_t column = 0;
    };

    class error {
        public:

        error(error_reason reason, error_category category, const std::string &message, error_location location) noexcept;
        ~error() noexcept = default;

        [[nodiscard]]
        error_reason reason() const noexcept;

        [[nodiscard]]
        error_category category() const noexcept;

        [[nodiscard]]
        std::string message() const noexcept;

        [[nodiscard]]
        error_location location() const noexcept;

        protected:
        error_reason reason_;
        error_category category_;
        std::string message_;
        error_location location_;
    };
}
