DESCRIPTION = "A microbenchmark support library"
HOMEPAGE = "https://github.com/google/benchmark"
SECTION = "libs"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0f6f3bbd074b822ddbb3b4d0e7f8d652"

SRC_URI = "git://github.com/google/benchmark.git;protocol=https;branch=main"
SRCREV = "d845b7b3a27d54ad96280a29d61fa8988d4fddcf"

S = "${WORKDIR}/git"

EXTRA_OECMAKE = " \
    -DBUILD_SHARED_LIBS=yes \
    -DBENCHMARK_ENABLE_TESTING=no \
    -DCMAKE_BUILD_TYPE=Release \
"

inherit cmake

FILES:${PN}-dev += "${libdir}/cmake"
