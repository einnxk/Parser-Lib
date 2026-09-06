#pragma once

#include <cstdint>

extern "C" {

    typedef struct config_parser_t config_parser_t;
    typedef struct config_value_t config_value_t;
    typedef struct config_error_t config_error_t;

    config_parser_t* config_parser_create(const char* format);
    void config_parser_destroy(config_parser_t* parser);

    int config_parse_string(
        config_parser_t* parser,
        const char* content,
        config_value_t** out_value,
        config_error_t** out_error
    );

    const char* config_value_get_string(const config_value_t* val);
    int64_t config_value_get_int(const config_value_t* val);

    const char* config_error_get_message(const config_error_t* err);
    size_t config_error_get_line(const config_error_t* err);
}