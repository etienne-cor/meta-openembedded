DESCRIPTION = "Various utilities from Android"
SECTION = "console/utils"
LICENSE = "Apache-2.0 & GPL-2.0-only & BSD-2-Clause & BSD-3-Clause"
LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10 \
    file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6 \
    file://${COMMON_LICENSE_DIR}/BSD-2-Clause;md5=cb641bc04cda31daea161b1bc15da69f \
    file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9 \
"
DEPENDS = "libbsd libpcre zlib libcap libusb squashfs-tools p7zip libselinux googletest"

SRCREV = "712fdb73260d79d7213a6c23b53765947138f0de"
SRC_URI = "git://salsa.debian.org/android-tools-team/android-platform-external-boringssl;protocol=https;nobranch=1"

# Patches copied from android-platform-tools/debian/patches
# and applied in the order defined by the file debian/patches/series
SRC_URI += " \
"

# patches which don't come from debian
SRC_URI += " \
"

S = "${WORKDIR}/git"
B = "${WORKDIR}/${BPN}"

# http://errors.yoctoproject.org/Errors/Details/1debian881/
ARM_INSTRUCTION_SET:armv4 = "arm"
ARM_INSTRUCTION_SET:armv5 = "arm"

COMPATIBLE_HOST:powerpc = "(null)"
COMPATIBLE_HOST:powerpc64 = "(null)"
COMPATIBLE_HOST:powerpc64le = "(null)"

# Find libbsd headers during native builds
CC:append:class-native = " -I${STAGING_INCDIR}"
CC:append:class-nativesdk = " -I${STAGING_INCDIR}"

CPPFLAGS:append = " -fPIC"

#PREREQUISITE_core = "liblog libbase libsparse liblog libcutils"
PREREQUISITE_core = ""
TOOLS_TO_BUILD = "libcrypto_utils"

do_compile() {

    case "${HOST_ARCH}" in
      arm)
        export android_arch=linux-arm
        cpu=arm
        deb_host_arch=arm
      ;;
      aarch64)
        export android_arch=linux-arm64
        cpu=arm64
        deb_host_arch=arm64
      ;;
      riscv64)
        export android_arch=linux-riscv64
      ;;
      mips|mipsel)
        export android_arch=linux-mips
        cpu=mips
        deb_host_arch=mips
      ;;
      mips64|mips64el)
        export android_arch=linux-mips64
        cpu=mips64
        deb_host_arch=mips64
      ;;
      powerpc|powerpc64)
        export android_arch=linux-ppc
      ;;
      i586|i686|x86_64)
        export android_arch=linux-x86
        cpu=x86_64
        deb_host_arch=amd64
      ;;
    esac

    export SRCDIR=${S}

    oe_runmake -f ${S}/debian/libcrypto.mk -C ${S}
}

do_install() {
    # deploy to ${includedir}/android to avoid collision with openssl
    install -d  ${D}${includedir}/android
    install -d  ${D}${includedir}/android/openssl
    install ${S}/src/include/openssl/*h ${D}${includedir}/android/openssl

    install -d  ${D}${libdir}/android/
    install -m0755 ${S}/debian/out/*.so.* ${D}${libdir}/android/
}

RDEPENDS:${BPN} = "p7zip"

FILES:${PN} += "${libdir}/android ${libdir}/android/*"

BBCLASSEXTEND = "native"
