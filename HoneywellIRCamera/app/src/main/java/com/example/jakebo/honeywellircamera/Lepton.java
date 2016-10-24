package com.example.jakebo.honeywellircamera;

/**
 * Created by jakebo on 9/22/16.
 */
public class Lepton {
    private static native int InitIRCamera();
    private static native int GetIRFrame(short[] frameData, short[] maxMinValue);
    private static native int FFCNormalization();
    private static native float GetSysAuxTemperatureCelcius();

    static {
        System.loadLibrary("honeywellflir");
    }

    public int LeptonInit() {
        //return InitIRCamera();
        InitIRCamera();
        FFCNormalization();
        return 0;
    }

    public void LeptonGetFrame(short[] frameData, short[] maxMinValue) {
        GetIRFrame(frameData, maxMinValue);
    }

    public float LeptonGetCelcius() {
        return GetSysAuxTemperatureCelcius();
    }

    public double LeptonGetTemperatureByRawValue(int rawValue) {
        return (0.0217 * rawValue) + GetSysAuxTemperatureCelcius() - 177.77;
    }
}
