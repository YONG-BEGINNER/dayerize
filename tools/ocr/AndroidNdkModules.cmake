# Compatibility shim for upstream Tesseract4Android with CMake 3.31:
# import the same NDK cpu-features implementation used by CMake 3.22.
function(android_ndk_import_module_cpufeatures)
    add_library(cpufeatures STATIC "${ANDROID_NDK}/sources/android/cpufeatures/cpu-features.c")
    target_include_directories(cpufeatures PUBLIC "${ANDROID_NDK}/sources/android/cpufeatures")
endfunction()
