#pragma once

#include <stdexcept>
#include "error.h"

namespace config {

    class config_exception : public std::runtime_error {
        public:

        explicit config_exception(error error)
            : std::runtime_error(error.message().data()), error_(std::move(error)) {}

        [[nodiscard]]
        const error& original_error() const noexcept {
            return error_;
        }

        private:
        error error_;
    };
}