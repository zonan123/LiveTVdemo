package com.mediatek.wwtv.tvcenter.oad;

import android.content.Context;
import com.mediatek.twoworlds.tv.MtkTvOAD;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvConfig;


public class NavOADController{

    private NavOADActivity activity;
    private NavOADCallback callBack;

    public NavOADController(NavOADActivity activity) {
        super();
        this.activity = activity;
        registerCallback(activity);
    }

    public NavOADActivity getActivity() {
        return activity;
    }

    public void setActivity(NavOADActivity activity) {
        this.activity = activity;
    }

    public void registerCallback(NavOADActivity activity2) {
        callBack=NavOADCallback.getInstance(activity2);
    }

    public void unRegisterCallback() {
        callBack.removeListener();
    }

    public int manualDetect() {
        return MtkTvOAD.getInstance().startManualDetect();
    }

    public int cancelManualDetect() {
        return MtkTvOAD.getInstance().stopManualDetect();
    }

    public int acceptDownload() {
        return MtkTvOAD.getInstance().startDownload();
    }
    public String getScheduleInfo() {
        MtkTvOAD.getInstance().getScheduleInfo();
        return MtkTvOAD.getInstance().mscheduleInfo;
    }
    public int acceptScheduleOAD() {
        return MtkTvOAD.getInstance().acceptSchedule();
    }

    public int acceptJumpChannel() {
        return MtkTvOAD.getInstance().startJumpChannel();
    }

    public int cancelDownload() {
        return MtkTvOAD.getInstance().stopDownload();
    }

    public int acceptFlash() {
        return MtkTvOAD.getInstance().startFlash();
    }

    public int acceptRestart() {
        return MtkTvOAD.getInstance().acceptRestart();
    }

    public int remindMeLater() {
        return MtkTvOAD.getInstance().remindMeLater();
    }

    public int setOADAutoDownload(boolean auto){
        return MtkTvOAD.getInstance().setAutoDownload(auto);
    }

    public static void setOADAutoDownload(Context context,
            boolean enableAutoDownload) {
        MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_OAD_OAD_SEL_OPTIONS_AUTO_DOWNLOAD, (enableAutoDownload ? 1 : 0));
    }

    public static boolean getOADAutoDownload(Context context) {
        return MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_OAD_OAD_SEL_OPTIONS_AUTO_DOWNLOAD) == 0 ? false : true;
    }

}
