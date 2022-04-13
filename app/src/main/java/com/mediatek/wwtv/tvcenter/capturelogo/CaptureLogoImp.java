package com.mediatek.wwtv.tvcenter.capturelogo;

import com.mediatek.wwtv.tvcenter.capturelogo.TVStorage.LogoCaptureListener;


import android.content.Context;
import android.graphics.Rect;

import com.mediatek.twoworlds.tv.MtkTvAVMode;
/**
 * capture logo related interface
 *
 * @author MTK40462
 *
 */
public final class CaptureLogoImp {
    private static final String TAG = "CaptureLogoImp";
    public static CaptureLogoImp capImp;
    public static Context mContext;
    private static TVStorage tvLogo;

    private static int saveId = 0;
    private static Rect mArea = new Rect();
    public static int TV_LOGO = TVStorage.CAP_LOGO_TV;
    public static int MMP_VIDEO_LOGO = TVStorage.CAP_LOGO_MM_VIDEO;
    public static int MMP_IMAGE_LOGO = TVStorage.CAP_LOGO_MM_IMAGE;

    private CaptureLogoImp(Context context) {
    	init(context);
    }
    private void init(Context context){
        mContext = context;
        tvLogo = TVStorage.getInstance(context);
    }

    public static synchronized CaptureLogoImp getInstance(Context context) {
        if (capImp == null) {
            capImp = new CaptureLogoImp(context);
        }
        return capImp;
    }

    /**
     * select position to save the TV screen
     *
     * @param logoId
     *            0 or 1
     */
    public void setSavePosition(int logoId) {
        this.saveId = logoId;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "----- Save position: " + saveId);
    }

    /**
     * if capture area is not full screen, select a special area
     *
     * @param r
     *            the selected area position and size
     */
    public void setSpecialArea(Rect r) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "----------Special Area Rect------------------");
        if (r != null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Left: " + r.left + "  Top: " + r.top + "  Width: "
                    + r.width() + "  Height: " + r.height());
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "select full screen");
        }
        this.mArea = r;
    }

    /**
     * begin capture the current screen
     *
     * @param listener
     */
    public void setLogoCaptureListener(LogoCaptureListener listener, int sourceType) {
        tvLogo.captureLogo(sourceType, mArea, saveId, listener);
    }

    /**
     * begin capture the current screen
     *
     * @param listener
     */
    public void setLogoCaptureListener(LogoCaptureListener listener, int sourceType,
            int skBitmap, int bufferWidth, int bufferHeight, int bufferPitch, int colorMode) {

        tvLogo.captureLogo(sourceType, mArea, saveId, listener, skBitmap,
                bufferWidth, bufferHeight, bufferPitch, colorMode);
    }

    /**
     * cancel capturing
     */
    public void removeLogoCaptureListener(int sourceType) {
        tvLogo.cancleCaptureLogo(sourceType);
    }

    /**
     * release resouce when capture logo.
     * @param sourceType
     */
    public void finishLogoCaputer(int sourceType){
        tvLogo.finishCaptureLogo(sourceType);
    }

    /**
     * freeze the main Screen
     *
     * @param freezed
     *            freeze:true , not freeze:false
     */
    public void freezeScreen(boolean freezed) {
        MtkTvAVMode.getInstance().setFreeze("main", freezed);
    }

    public boolean isFreeze(){
        return MtkTvAVMode.getInstance().isFreeze("main");
    }
}
