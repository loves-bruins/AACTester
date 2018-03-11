# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
project(aac_enc)
set_property(SOURCE src/main/cpp/src/asm/ARM5E/band_nrg_5.s PROPERTY LANGUAGE C)
include_directories(src/main/cpp/inc)
add_library( # Sets the name of the library.
             aac_enc

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/aac_enc.cpp
             src/main/cpp/src/aac_rom.c
             src/main/cpp/src/aacenc_core.c
             src/main/cpp/src/aacenc.c
             src/main/cpp/src/adj_thr.c
             src/main/cpp/src/asm/ARMV5E/AutoCorrelation_v5.s
             src/main/cpp/src/band_nrg.c
             src/main/cpp/basic_op/basicop2.c
             src/main/cpp/src/bit_cnt.c
             src/main/cpp/src/bitbuffer.c
             src/main/cpp/src/bitenc.c
             src/main/cpp/src/block_switch.c
             src/main/cpp/src/asm/ARMV5E/CalcWindowEnergy_v5.s
             src/main/cpp/src/channel_map.c
             src/main/cpp/src/cmnMemory.c
             src/main/cpp/src/dyn_bits.c
             src/main/cpp/src/grp_data.c
             src/main/cpp/src/interface.c
             src/main/cpp/src/line_pe.c
             src/main/cpp/src/memalign.c
             src/main/cpp/src/ms_stereo.c
             src/main/cpp/basic_op/oper_32b.c
             src/main/cpp/src/pre_echo_control.c
             src/main/cpp/src/asm/ARMV7/PrePostMDCT_v7.s
             src/main/cpp/src/psy_configuration.c
             src/main/cpp/src/psy_main.c
             src/main/cpp/src/qc_main.c
             src/main/cpp/src/quantize.c
             src/main/cpp/src/asm/ARMV7/R4R8First_v7.s
             src/main/cpp/src/asm/ARMV7/Radix4FFT_v7.s
             src/main/cpp/src/sf_estim.c
             src/main/cpp/src/spreading.c
             src/main/cpp/src/stat_bits.c
             src/main/cpp/src/tns.c
             src/main/cpp/src/transform.c)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       aac_enc

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )