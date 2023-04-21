DESCRIPTION = "Various utilities from Android"
SECTION = "console/utils"
LICENSE = "Apache-2.0 & GPL-2.0-only & BSD-2-Clause & BSD-3-Clause"
LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10 \
    file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6 \
    file://${COMMON_LICENSE_DIR}/BSD-2-Clause;md5=cb641bc04cda31daea161b1bc15da69f \
    file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9 \
"
# squashfs-tools needed by extras/libext4_utils.mk
DEPENDS = "boringssl libbsd libpcre libcap libusb squashfs-tools p7zip googletest protobuf protobuf-c-native brotli"

SRCREV = "0462a4cf9e89bc8533cc16c9f7b38350bc66d793"
SRC_URI = " \
    git://salsa.debian.org/android-tools-team/android-platform-tools;protocol=https;nobranch=1 \
"

# Patches copied from android-platform-tools/debian/patches
# and applied in the order defined by the file debian/patches/series
SRC_URI += " \
    file://debian/system/move-log-file-to-proper-dir.patch \
    file://debian/system/Added-missing-headers.patch \
    file://debian/system/libusb-header-path.patch \
    file://debian/system/stdatomic.patch \
    file://debian/system/throw-exception-on-unknown-os.patch \
    file://debian/system/add-missing-headers.patch \
    file://debian/system/hard-code-build-number.patch \
    file://debian/system/stub-out-fastdeploy.patch \
    file://debian/system/fix-standard-namespace-errors.patch \
    file://debian/system/workaround__builtin_available.patch \
    file://debian/system/unwindstack-porting.patch \
    file://debian/system/support-mips.patch \
    file://debian/system/Revert-Remove-mips-build.patch \
    file://debian/system/Revert-Remove-mips-support-fr.patch \
    file://debian/system/Fix-include-path.patch \
    file://debian/system/Implement-const_iterator-operator.patch \
"

# patches which don't come from debian
SRC_URI += " \
    file://rules_yocto.mk;subdir=git \
    file://android-tools-adbd.service \
    file://adbd.mk;subdir=git/debian/system \
    file://remount \
    file://0001-Fixes-for-yocto-build.patch \
    file://0002-android-tools-modifications-to-make-it-build-in-yoct.patch \
    file://0003-Update-usage-of-usbdevfs_urb-to-match-new-kernel-UAP.patch \
    file://0004-adb-Fix-build-on-big-endian-systems.patch \
    file://0005-adb-Allow-adbd-to-be-run-as-root.patch \
    file://0006-Revert-debian-patch-Fix-include-path.patch \
    file://0001-link-to-boringssl-instead-of-openssl.patch \
"

S = "${WORKDIR}/git"
B = "${WORKDIR}/${BPN}"

# http://errors.yoctoproject.org/Errors/Details/1debian881/
ARM_INSTRUCTION_SET:armv4 = "arm"
ARM_INSTRUCTION_SET:armv5 = "arm"

COMPATIBLE_HOST:powerpc = "(null)"
COMPATIBLE_HOST:powerpc64 = "(null)"
COMPATIBLE_HOST:powerpc64le = "(null)"

inherit systemd

SYSTEMD_SERVICE:${PN}-adbd = "android-tools-adbd.service"

# Find libbsd headers during native builds
CC:append:class-native = " -I${STAGING_INCDIR}"
CC:append:class-nativesdk = " -I${STAGING_INCDIR}"
# Necessary to find boringssl
CPPFLAGS:prepend = " -I${STAGING_INCDIR}/android "
LDFLAGS += "-L${STAGING_LIBDIR}/android"
# Workaround to compile clang macros with gcc:
CPPFLAGS:append = " -D_Nonnull='' -D_Nullable='' -D'EXCLUDES()='"

PREREQUISITE_core = "liblog libbase libsparse liblog libcutils libcrypto_utils"
TOOLS_TO_BUILD = "libadb libziparchive fastboot adb img2simg simg2img libbacktrace"
TOOLS_TO_BUILD:append:class-target = " adbd"

do_compile() {

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

	for proto in ${S}/packages/modules/adb/fastdeploy/proto ${S}/packages/modules/adb/proto; do \
		(cd ${proto} && \
			find . -name '*.proto' -printf 'Regenerate %p\n' \
			-exec protoc --cpp_out=. {} \;) \
	done

    for tool in ${PREREQUISITE_core}; do
      oe_runmake -f ${S}/debian/system/${tool}.mk -C ${S}
    done

    for i in `find ${S}/debian/system/extras/ -name "*.mk"`; do
        oe_runmake -f $i -C ${S}
    done

    for tool in ${TOOLS_TO_BUILD}; do
        if [ "$tool" = "libbacktrace" ]; then
            oe_runmake -f ${S}/debian/system/${tool}.mk -C ${S} DEB_HOST_ARCH=${deb_host_arch}
        else
            oe_runmake -f ${S}/debian/system/${tool}.mk -C ${S}
        fi
    done

}

do_install() {
    install -d ${D}${base_sbindir}
    install -m 0755 ${S}/../remount -D ${D}${base_sbindir}/remount

    for tool in img2simg simg2img fastboot adbd; do
        if echo ${TOOLS_TO_BUILD} | grep -q "$tool" ; then
            install -D -p -m0755 ${S}/debian/out/system/core/$tool ${D}${bindir}/$tool
        fi
    done

    # grep adb also matches adbd, so handle adb separately from other tools
    if echo ${TOOLS_TO_BUILD} | grep -q "adb " ; then
        install -d ${D}${bindir}
        install -m0755 ${S}/debian/out/system/core/adb ${D}${bindir}
    fi

    # Outside the if statement to avoid errors during do_package
    install -D -p -m0644 ${WORKDIR}/android-tools-adbd.service \
      ${D}${systemd_unitdir}/system/android-tools-adbd.service

    install -d  ${D}${libdir}/android/
    install -m0755 ${S}/debian/out/system/core/*.so.* ${D}${libdir}/android/
    if echo ${TOOLS_TO_BUILD} | grep -q "mkbootimg" ; then
        install -d ${D}${bindir}
        install -m0755 ${B}/mkbootimg/mkbootimg ${D}${bindir}
    fi
}

PACKAGES =+ "${PN}-fstools ${PN}-adbd"

RDEPENDS:${BPN} = "${BPN}-conf p7zip"

FILES:${PN}-adbd = "\
    ${bindir}/adbd \
    ${systemd_unitdir}/system/android-tools-adbd.service \
"

FILES:${PN}-fstools = "\
    ${bindir}/ext2simg \
    ${bindir}/ext4fixup \
    ${bindir}/img2simg \
    ${bindir}/make_ext4fs \
    ${bindir}/simg2img \
    ${bindir}/simg2simg \
    ${bindir}/simg_dump \
    ${bindir}/mkuserimg \
"
FILES:${PN} += "${libdir}/android ${libdir}/android/*"

BBCLASSEXTEND = "native"

android_tools_enable_devmode() {
    touch ${IMAGE_ROOTFS}/var/usb-debugging-enabled
}

ROOTFS_POSTPROCESS_COMMAND_${PN}-adbd += "${@bb.utils.contains("USB_DEBUGGING_ENABLED", "1", "android_tools_enable_devmode;", "", d)}"
