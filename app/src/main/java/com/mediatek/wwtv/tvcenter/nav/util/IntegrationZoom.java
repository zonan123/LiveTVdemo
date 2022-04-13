/**
 * use for connect user interface and common logic
 */

package com.mediatek.wwtv.tvcenter.nav.util;

import com.mediatek.twoworlds.tv.MtkTvAVMode;
import com.mediatek.twoworlds.tv.MtkTvUtil;
//import com.mediatek.twoworlds.tv.MtkTvAppTV;
import com.mediatek.twoworlds.tv.model.MtkTvRectangle;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.view.ZoomTipView;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import android.content.Context;
import android.view.View;

public class IntegrationZoom {
    private static final String TAG = "IntegrationZoom";

    private static IntegrationZoom instance;

    public static final int ZOOM_UP = 0;
    public static final int ZOOM_DOWN = 1;
    public static final int ZOOM_LEFT = 2;
    public static final int ZOOM_RIGHT = 3;
    public static final int ZOOM_0_5 = 0;
    public static final int ZOOM_1 = 1;
    public static final int ZOOM_2 = 2;
    private int current_zoom = ZOOM_1;

    private static final String SOURCE_MAIN = "main";
//    private static final String SOURCE_SUB = "sub";

    private MtkTvUtil mtkTvUtil;
//    private MtkTvAppTV mtkTvAppTv;

    private TVAsyncExecutor executor;

    private ZoomListener zoomListener = null;

    /**
     * when power on TV first time, should start setup wizard
     */

    private IntegrationZoom(Context context) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,"context: "+context);
        mtkTvUtil = MtkTvUtil.getInstance();
//        mtkTvAppTv = MtkTvAppTV.getInstance();
        executor = TVAsyncExecutor.getInstance();
    }

    public static synchronized IntegrationZoom getInstance(Context context) {
        if (instance == null) {
            instance = new IntegrationZoom(context);
        }
        return instance;
    }

    public interface ZoomListener {
        void zoomShow(int value);
    }

    public void setZoomListener(ZoomListener listener) {
        zoomListener = listener;
    }

    /**
     * zoom is useful in different screen mode
     */
    public boolean screenModeZoomShow() {
        boolean show = MtkTvAVMode.getInstance().isZoomEnable();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "screenModeZoomShow show = " + show);
        return show;

    }

    /**
     * move screen by direction
     * @param moveType ZOOM_UP,ZOOM_DOWN,ZOOM_LEFT,ZOOM_RIGHT.
     */
    public void moveScreenZoom(int moveType) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "moveScreenZoom moveType =" + moveType + "screenModeZoomShow() ="
                + screenModeZoomShow() + "current_zoom =" + current_zoom);
        if (screenModeZoomShow() && (current_zoom == ZOOM_2)) {

            MtkTvRectangle mSrcRectangleRectF = mtkTvUtil.getScreenSourceRect(SOURCE_MAIN);
            float l = 0;
            float t = 0;
            float r = 0;
            float b = 0;

            if (null != mSrcRectangleRectF) {
                l = mSrcRectangleRectF.getX();
                t = mSrcRectangleRectF.getY();
                r = mSrcRectangleRectF.getW();
                b = mSrcRectangleRectF.getH();

                switch (moveType) {
                    case ZOOM_UP:
                        t -= 0.02;
                        if (t < 0.0) {
                            t = 0.0f;
                        }
                        break;
                    case ZOOM_DOWN:
                        t += 0.02;
                        if (t > 0.5) {
                            t = 0.5f;
                        }
                        break;
                    case ZOOM_LEFT:
                        l -= 0.02;
                        if (l < 0.0) {
                            l = 0.0f;
                        }
                        break;
                    case ZOOM_RIGHT:
                        l += 0.02;
                        if (l > 0.5) {
                            l = 0.5f;
                        }
                        break;
                    default:
                        break;
                }

                CommonIntegration.getInstance().updateOutputChangeState(
                        CommonIntegration.ZOOM_CHANGE_BEFORE);
                mtkTvUtil.setScreenSourceRect(SOURCE_MAIN, new MtkTvRectangle(l, t, r, b));// .setSrcRectangle(new RectF(l, t, r, b));
                CommonIntegration.getInstance().updateOutputChangeState(
                        CommonIntegration.ZOOM_CHANGE_AFTER);
            }
        }
    }

    public boolean showCurrentZoom() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showCurrentZoom");
        if (screenModeZoomShow()) {
            if (zoomListener != null) {
                zoomListener.zoomShow(getCurrentZoom());
            }
            return true;
        }
        return false;
    }

    /*
     * get current zoom
     */
    public int getCurrentZoom() {
        MtkTvRectangle mScreenRectangle = mtkTvUtil.getScreenOutputDispRect(SOURCE_MAIN);
        MtkTvRectangle mSrcRectangle = mtkTvUtil.getScreenSourceRect(SOURCE_MAIN);
        if (null != mScreenRectangle && null != mSrcRectangle) {
            float tmpscr = mScreenRectangle.getW(); // - mScreenRectangle.getX();
            float tmpsrc = mSrcRectangle.getW(); // - mSrcRectangle.getX();

            if (tmpscr < 0.6 && tmpscr > 0.4) {
                current_zoom = ZOOM_0_5;
            } else {
                if (tmpsrc < 0.6 && tmpsrc > 0.4) {
                    current_zoom = ZOOM_2;
                } else {
                    current_zoom = ZOOM_1;
                }
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentZoom current_zoom =" + current_zoom);
        return current_zoom;
    }

    // when suc use callback to update ui.
    public boolean nextZoom() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nextZoom~~");
        if (screenModeZoomShow())
        {
            current_zoom = getCurrentZoom();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nextZoom current_zoom = " + current_zoom);
            current_zoom = (current_zoom + 1) % 3;

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "nextZoom add current_zoom = " + current_zoom);
            executor.execute(new Runnable() {
                public void run() {
                    setZoomMode(current_zoom);
                }
            });
            return true;
        }
        return false;
    }

    public void setZoomMode(int zoomMode) {
        if (screenModeZoomShow()) {
//            mtkTvAppTv.setVideoMute("main",true);
            switch (zoomMode) {
                case ZOOM_1:
                    CommonIntegration.getInstance().updateOutputChangeState(
                            CommonIntegration.ZOOM_CHANGE_BEFORE);
                    mtkTvUtil.setScreenSourceRect(SOURCE_MAIN, new MtkTvRectangle(0.0f, 0.0f, 1.0f,
                            1.0f));
                    mtkTvUtil.setScreenOutputDispRect(SOURCE_MAIN, new MtkTvRectangle(0.0f, 0.0f,
                            1.0f, 1.0f));
                    CommonIntegration.getInstance().updateOutputChangeState(
                            CommonIntegration.ZOOM_CHANGE_AFTER);
                    break;
                case ZOOM_2:
                    CommonIntegration.getInstance().updateOutputChangeState(
                            CommonIntegration.ZOOM_CHANGE_BEFORE);
                    mtkTvUtil.setScreenSourceRect(SOURCE_MAIN, new MtkTvRectangle(0.25f, 0.25f,
                            0.5f, 0.5f));
                    mtkTvUtil.setScreenOutputDispRect(SOURCE_MAIN, new MtkTvRectangle(0.0f, 0.0f,
                            1.0f, 1.0f));
                    CommonIntegration.getInstance().updateOutputChangeState(
                            CommonIntegration.ZOOM_CHANGE_AFTER);
                    break;
                case ZOOM_0_5:
                    CommonIntegration.getInstance().updateOutputChangeState(
                            CommonIntegration.ZOOM_CHANGE_BEFORE);
                    mtkTvUtil.setScreenSourceRect(SOURCE_MAIN, new MtkTvRectangle(0.0f, 0.0f, 1.0f,
                            1.0f));
                    mtkTvUtil.setScreenOutputDispRect(SOURCE_MAIN, new MtkTvRectangle(0.25f, 0.25f,
                            0.5f,
                            0.5f));
                    CommonIntegration.getInstance().updateOutputChangeState(
                            CommonIntegration.ZOOM_CHANGE_AFTER);
                    break;
                default:
                    break;
            }
            current_zoom = getCurrentZoom();
            TurnkeyUiMainActivity.getInstance().getHandler().post(new Runnable() {

                @Override
                public void run() {
//                    executor.execute(new Runnable() {
//                        public void run() {
//                            mtkTvAppTv.setVideoMute("main",false);
//                        }
//                    });
                    if (zoomListener != null) {
                        zoomListener.zoomShow(current_zoom);
                    }
                }
            });
        }
    }

    public void setZoomModeToNormal() {
        setZoomMode(ZOOM_1);
    }

    public void setZoomModeToNormalWithThread() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(getCurrentZoom() != ZOOM_1 && screenModeZoomShow()) {
                    setZoomModeToNormal();
                    final ZoomTipView mZoomTip = ((ZoomTipView) ComponentsManager
                            .getInstance()
                            .getComponentById(NavBasic.NAV_COMP_ID_ZOOM_PAN));
                    if (mZoomTip != null
                            && mZoomTip.getVisibility() == View.VISIBLE) {
                        TurnkeyUiMainActivity.getInstance().runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mZoomTip.setVisibility(View.GONE);
                                    }
                                });
                    }
                }
            }
        });
    }
}
