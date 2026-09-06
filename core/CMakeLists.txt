cmake_minimum_required(VERSION 4.1)

project(config-core LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

add_library(config-core
        src/error.cpp)

target_include_directories(config-core PUBLIC include)
target_compile_features(config-core PUBLIC cxx_std_20)

if (MSVC)
    target_compile_options(config-core PRIVATE /W4)
else()
    target_compile_options(config-core PRIVATE
            -Wall
            -Wextra
            -Wpedantic
    )
endif()