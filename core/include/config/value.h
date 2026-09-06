#pragma once

#include <variant>
#include <string>
#include <vector>
#include <unordered_map>
#include <memory_resource>
#include <utility>
#include "exception.hpp"

namespace config {

    enum class value_type {
        Null_value,
        Char,
        Byte,
        Boolean,
        Integer,
        Float,
        Short,
        Double,
        Long,
        String,
        Map,
        Array,
        List,
        Set,
        Enumeration,
        Section,
        Object,
    };

    class value {
        public:
        using string_type = std::pmr::string;
        using array_type = std::pmr::vector<value>;
        using list_type = std::pmr::vector<value>;
        using set_type = std::pmr::vector<value>;
        using map_type = std::pmr::unordered_map<string_type, value>;
        using section_type = map_type;

        struct enumeration_data {
            string_type enum_name;
            string_type value_name;
        };

        struct object_data {
            string_type type_name;
            map_type fields;
        };

        using variant_type = std::variant<
            std::monostate,
            char,
            int8_t,
            bool,
            int32_t,
            float,
            int16_t,
            double,
            int64_t,
            string_type,
            map_type,
            array_type,
            list_type,
            set_type,
            enumeration_data,
            section_type,
            object_data
        >;

        value() noexcept {}

        template<typename T>
        explicit value(T&& val, const value_type explicit_type)
            : type_(explicit_type), data_(std::forward<T>(val)) {}

        [[nodiscard]] value_type type() const noexcept {
            return type_;
        }

        [[nodiscard]] bool is_null() const noexcept {
            return type_ == value_type::Null_value;
        }

        template<typename T>
        [[nodiscard]] const T& as() const {
            if (auto* ptr = std::get_if<T>(&data_)) {
                return *ptr;
            }
            throw config_exception(error(
                error_reason::InvalidType,
                error_category::Type,
                "Requested type does not match the underlying value_type",
                {}
            ));
        }

        template<typename T>
        [[nodiscard]] T& as() {
            if (auto* ptr = std::get_if<T>(&data_)) {
                return *ptr;
            }
            throw config_exception(error(
                error_reason::InvalidType,
                error_category::Type,
                "Requested type does not match the underlying value_type",
                {}
            ));
        }

        private:
        value_type type_ = value_type::Null_value;
        variant_type data_{std::monostate{}};
    };
}
