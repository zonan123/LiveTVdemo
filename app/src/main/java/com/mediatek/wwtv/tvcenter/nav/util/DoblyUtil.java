package com.mediatek.wwtv.tvcenter.nav.util;

import android.content.Context;
import android.media.AudioManager;

import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;


public class DoblyUtil {

    private final static String TAG = "DoblyUtil";

    public static boolean isSupportDolbyAtmos(Context context){
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int deviceType = audioManager.getDevicesForStream(AudioManager.STREAM_MUSIC);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isSupportDolbyAtmos deviceType=="+deviceType);

        if (deviceType == AudioManager.DEVICE_OUT_HDMI_ARC
                || deviceType == AudioManager.DEVICE_OUT_HDMI){
            return false;
        }

        if (deviceType != AudioManager.DEVICE_OUT_SPEAKER){
            return true;
        }

        int isDAPSwitch = MtkTvConfig.getInstance().getConfigValue(
                MtkTvConfigType.CFG_AUD_DOLBY_AUDIO_PROCESSING);
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "isDAPSwitch == " + isDAPSwitch);
        if (0 == isDAPSwitch){
            return false;
        }

        int isSurroundVirtualizerSwitch = MtkTvConfig.getInstance().getConfigValue(
                MtkTvConfigType.CFG_AUD_DOLBY_SURROUND_VIRTUALIZER);
        int isDolbyAtmosSwitch = MtkTvConfig.getInstance().getConfigValue(
                MtkTvConfigType.CFG_AUD_DOLBY_ATMOS);
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "isSurroundVirtualizerSwitch == " + isSurroundVirtualizerSwitch
                + ",isDolbyAtmosSwitch == "+isDolbyAtmosSwitch);

        return (!(0 == isSurroundVirtualizerSwitch && 0 == isDolbyAtmosSwitch));
    }
}
