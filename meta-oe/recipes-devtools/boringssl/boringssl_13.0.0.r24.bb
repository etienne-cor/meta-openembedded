DESCRIPTION = "Various utilities from Android"
SECTION = "console/utils"
LICENSE = "ISC & OpenSSL"
LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/ISC;md5=f3b90e78ea0cffb20bf5cca7947a896d \
    file://${COMMON_LICENSE_DIR}/OpenSSL;md5=4eb1764f3e65fafa1a25057f9082f2ae \
"
DEPENDS = "googletest"

SRCREV = "712fdb73260d79d7213a6c23b53765947138f0de"
SRC_URI = "git://salsa.debian.org/android-tools-team/android-platform-external-boringssl;protocol=https;nobranch=1"

S = "${WORKDIR}/git"
B = "${WORKDIR}/${BPN}"

CPPFLAGS:append = " -fPIC \
    -DBORINGSSL_ANDROID_SYSTEM \
    -DBORINGSSL_IMPLEMENTATION \
    -DBORINGSSL_SHARED_LIBRARY \
    -DOPENSSL_SMALL \
"

MAKEFILES_TO_BUILD = "libcrypto libssl"

do_compile() {
    # Needed because e.g. libcrypto uses 'DEB_HOST_ARCH' to select the correct assembly files
    case "${HOST_ARCH}" in
      arm)
        deb_host_arch=arm
      ;;
      aarch64)
        deb_host_arch=arm64
      ;;
      mips|mipsel)
        deb_host_arch=mips
      ;;
      mips64|mips64el)
        deb_host_arch=mips64
      ;;
      i586|i686|x86_64)
        deb_host_arch=amd64
      ;;
    esac

    for makefile in ${MAKEFILES_TO_BUILD}; do
        oe_runmake -f ${S}/debian/${makefile}.mk -C ${S} DEB_HOST_ARCH=${deb_host_arch}
    done
}

do_install() {
    # deploy to ${includedir}/android to avoid collision with openssl
    install -d  ${D}${includedir}/android
    install -d  ${D}${includedir}/android/openssl
    install ${S}/src/include/openssl/*h ${D}${includedir}/android/openssl

    install -d  ${D}${libdir}/android/
    install -m0755 ${S}/debian/out/*.so.* ${D}${libdir}/android/
}

FILES:${PN} += "${libdir}/android ${libdir}/android/*"

BBCLASSEXTEND = "native"
