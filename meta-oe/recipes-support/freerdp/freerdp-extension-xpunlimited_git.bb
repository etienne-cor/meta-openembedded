# Copyright (C) 2011 O.S. Systems Software Ltda. All Rights Reserved
# Released under the MIT license

DESCRIPTION = "FreeRDP XPUnlimited RDP Extension support"
HOMEPAGE = "https://github.com/OSSystems/FreeRDP-Extension-XPUnlimited"
DEPENDS = "freerdp"
SECTION = "x11/network"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=3b83ef96387f14655fc854ddc3c6bd57"

PV = "gitr${SRCPV}"
PKGV = "${GITPKGVTAG}"
PR = "r0"

inherit pkgconfig cmake

SRCREV = "d27f90a0128c687ce74cc922dba8b7eeb33f0654"
SRC_URI = "git://github.com/OSSystems/FreeRDP-Extension-XPUnlimited.git;protocol=git"

S = "${WORKDIR}/git"

FILES_${PN} += "${libdir}/freerdp/extensions/*.so"
FILES_${PN}-dbg += "${libdir}/freerdp/extensions/.debug/*.so"
