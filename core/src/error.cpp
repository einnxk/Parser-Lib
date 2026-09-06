#include "config/error.h"

namespace config {

    error::error(const error_reason reason, const error_category category, const std::string &message, const error_location location) noexcept {
        reason_ = reason;
        category_ = category;
        message_ = message;
        location_ = location;
    }

    error_reason error::reason() const noexcept {
        return reason_;
    }

    error_category error::category() const noexcept {
        return category_;
    }

    std::string error::message() const noexcept {
        return message_;
    }

    error_location error::location() const noexcept {
        return location_;
    }
}
