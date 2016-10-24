# Copyright 2005 The Android Open Source Project
#
# Android.mk for TinyXml.
#
# Add -DTIXML_USE_STL to CFLAGS to use STL.
#
LOCAL_PATH:=$(call my-dir)

commonSources:= \
	leptonSDKEmb32PUB/crc16fast.c \
	leptonSDKEmb32PUB/LEPTON_SDK.c \
	leptonSDKEmb32PUB/LEPTON_VID.c \
	leptonSDKEmb32PUB/bbb_I2C.c \
	leptonSDKEmb32PUB/LEPTON_SYS.c \
	leptonSDKEmb32PUB/LEPTON_I2C_Service.c \
	leptonSDKEmb32PUB/LEPTON_I2C_Protocol.c \
	leptonSDKEmb32PUB/LEPTON_AGC.c \
	main.cpp \
	SPI.cpp

# For the device
# =====================================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	$(commonSources)

LOCAL_MODULE:= honeywellflir

LOCAL_SHARED_LIBRARIES := \
	libc \
	libstdc++ \
	libc++_shared \
	libutils \
	liblog

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
##include $(BUILD_EXECUTABLE)


