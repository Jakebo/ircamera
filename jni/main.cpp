#include "Palettes.h"
#include "SPI.h"
#include "Lepton_I2C.h"

#include "leptonSDKEmb32PUB/LEPTON_SDK.h"
#include "leptonSDKEmb32PUB/LEPTON_SYS.h"
#include "leptonSDKEmb32PUB/LEPTON_Types.h"
#include "leptonSDKEmb32PUB/LEPTON_AGC.h"

#include <jni.h>
#include <android/log.h>

#define PACKET_SIZE 164
#define PACKET_SIZE_UINT16 (PACKET_SIZE/2)
#define PACKETS_PER_FRAME 60
#define FRAME_SIZE_UINT16 (PACKET_SIZE_UINT16*PACKETS_PER_FRAME)
#define FPS 27

uint8_t result[PACKET_SIZE*PACKETS_PER_FRAME];

static LEP_CAMERA_PORT_DESC_T _port;

static const char *TAG = "HoneywellIRCamera";

static int ReadFrame(uint8_t *result)
{
    struct spi_ioc_transfer xfer[1];
    int j;
    int resets = 0;
    int status;
    uint8_t *bp;
    
    memset(xfer, 0, sizeof(xfer));
    xfer[0].len = PACKET_SIZE;
    
    for (j = 0; j < PACKETS_PER_FRAME; ++j) {
        bp = result + sizeof(uint8_t) * PACKET_SIZE * j;
        xfer[0].rx_buf = (unsigned long)bp;

        status = ioctl(spi_cs0_fd, SPI_IOC_MESSAGE(1), xfer);
        if (status < 0) {
            perror("SPI_IOC_MESSAGE");
            continue;
        }

        if (bp[1] != j) { // Check packet number
            j = -1;
            resets += 1;
            usleep(1000);
            if (resets == 750) {
                __android_log_print(ANDROID_LOG_INFO, TAG, "Rest spi port");
                SpiClosePort(0);
                usleep(750000);
                SpiOpenPort(0);
            }
        }
    }

    bp = result;
//    __android_log_print(ANDROID_LOG_INFO, TAG, "readframe: %#x %#x %#x %#x", bp[4], bp[5], bp[6], bp[7]);

    return 0;
}

extern "C" JNIEXPORT jint JNICALL Java_com_example_jakebo_honeywellircamera_Lepton_GetIRFrame
(JNIEnv *env, jclass cls, jshortArray frameData, jshortArray maxMinValue) {
    jshort* arr;
    jshort* maxMin;
    jint length;

    unsigned char *in;
    unsigned short *out;
    uint8_t *frame = result;
    int minValue = 65535;
    int maxValue = 0;

    arr = env->GetShortArrayElements(frameData, NULL);
    maxMin = env->GetShortArrayElements(maxMinValue, NULL);
    length = env->GetArrayLength(frameData);

    ReadFrame(frame);

    in = &frame[0];
    out = (unsigned short *)&arr[0];
    
    for (int iRow = 0; iRow < 60; ++iRow) {
        in += 4;
        for (int iCol = 0; iCol < 80; ++iCol) {
            unsigned short value = in[0];
            value <<= 8;
            value |= in[1];
            in += 2;
            if (value > maxValue) maxValue = value;
            if (value < minValue) minValue = value;
            *(out++) = value;
        }
    }

    maxMin[0] = maxValue;
    maxMin[1] = minValue;
    
    out = (unsigned short *)&arr[0];
//    __android_log_print(ANDROID_LOG_INFO, TAG, "getframe: %#x %#x %#x %#x", frame[4],frame[5],frame[6],frame[7]);
//    __android_log_print(ANDROID_LOG_INFO, TAG, "flip frame: %#x %#x",
//                        out[0], out[1]);
    
    env->ReleaseShortArrayElements(frameData, arr, 0);
    env->ReleaseShortArrayElements(maxMinValue, maxMin, 0);
    
    return 0;
}

extern "C" JNIEXPORT jint JNICALL Java_com_example_jakebo_honeywellircamera_Lepton_InitIRCamera
/*static jint InitIRCamera*/(JNIEnv *env, jclass cls) {
    LEP_SYS_TELEMETRY_ENABLE_STATE_E enableState;
    
    LEP_OpenPort(1, LEP_CCI_TWI, 400, &_port);
    //LEP_RunSysFFCNormalization(&_port);
    //LEP_SetAgcEnableState(&_port, LEP_AGC_ENABLE);
    //LEP_SetSysTelemetryEnableState(&_port, LEP_TELEMETRY_ENABLED);
    //LEP_GetSysTelemetryEnableState(&_port, &enableState);
    //__android_log_print(ANDROID_LOG_INFO, TAG, "telemetry state: %d\n", enableState);
    SpiOpenPort(0);
    
    return 0;
}

extern "C" JNIEXPORT jint JNICALL Java_com_example_jakebo_honeywellircamera_Lepton_FFCNormalization
/*static jint InitIRCamera*/(JNIEnv *env, jclass cls) {

    LEP_RunSysFFCNormalization(&_port);
    
    return 0;
}

extern "C" JNIEXPORT jint JNICALL Java_com_example_jakebo_honeywellircamera_Lepton_DeinitIRCamera
(JNIEnv *env, jclass cls) {
    SpiClosePort(0);
    
    return 0;
}

extern "C" JNIEXPORT jfloat JNICALL Java_com_example_jakebo_honeywellircamera_Lepton_GetSysAuxTemperatureCelcius
/*static jfloat GetSysAuxTemperatureCelcius*/(JNIEnv *env, jclass cls)
{
    LEP_SYS_FPA_TEMPERATURE_CELCIUS_T temperature;

    LEP_GetSysFpaTemperatureCelcius(&_port, &temperature);

    //__android_log_print(ANDROID_LOG_INFO, TAG, "temperature: %f\n", temperature);
    
    return temperature;
}

#if 0
static const char *classPathName = "com.example.jakebo.honeywellircamera.Lepton";

static JNINativeMethod methods[] = {
//    { "GetSysAuxTemperatureCelcius", "()F", (void*)GetSysAuxTemperatureCelcius },
    { "InitIRCamera", "()I", (void*)InitIRCamera},
};

static int registerNativeMethods(JNIEnv *env,
                                 const char *className,
                                 JNINativeMethod *gMethods,
                                 int numMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        __android_log_print(ANDROID_LOG_INFO, TAG, "Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

static int registerNatives(JNIEnv *env)
{
    if (!registerNativeMethods(env,
                               classPathName,
                               methods,
                               sizeof(methods) / sizeof(methods[0]))) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

typedef union {
    JNIEnv *env;
    void *venv;
} UnionJNIEnvToVoid;

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv *env = NULL;

    __android_log_print(ANDROID_LOG_INFO, TAG, "JNI_OnLoad");
    
    if (vm->GetEnv(&uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        __android_log_print(ANDROID_LOG_INFO, TAG, "ERROR: GetEnv failed");
        goto bail;
    }
    env = uenv.env;

    if (registerNatives(env) != JNI_TRUE) {
        __android_log_print(ANDROID_LOG_INFO, TAG, "ERROR: registerNatives failed");
        goto bail;
    }

    result = JNI_VERSION_1_4;

bail:
    return result;
}
#endif
