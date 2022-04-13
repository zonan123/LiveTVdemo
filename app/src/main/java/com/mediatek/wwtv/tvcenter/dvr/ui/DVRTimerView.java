package com.mediatek.wwtv.tvcenter.dvr.ui;

import android.app.Activity;
import android.content.Context;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import androidx.core.text.TextUtilsCompat;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;

import java.util.Locale;

/**
 *
 */
public class DVRTimerView extends CommonInfoBar {
    final String TAG = "DVRTimerView";
    private final LayoutParams wmParams;
    private int channelId = 0;
    private final static int defaultWidth = 280;
    private final static int defaultHeight = 100;
    private final static int defaultOffsetX = 20;

    private OnDismissListener mOnDismissListener;

    private WindowManager windowManager;
    private View mRootView;
    private TextView mInfo;

    /**
     * @param context
     */
    public DVRTimerView(Context context) {
        super(context);
        wmParams = new LayoutParams();
    }

    /**
     *
     */
    @Override
    protected void setLocation() {
        addToWM();
    }

    /*
     *
     */
    @Override
    public void setInfo(String info) {
        if (mRootView != null) {
            if (mInfo.getVisibility() != View.INVISIBLE) {
                mInfo.setVisibility(View.VISIBLE);
            }
            mInfo.setText(info);
        }
    }

    public void setCurrentTime(long mills) {
        mills += 1;
        long minute = mills / 60;
        long hour = minute / 60;
        long second = mills % 60;
        minute %= 60;
        setInfo(String.format("[%02d:%02d:%02d]", hour, minute,
                second));
    }

    /**
     *
     */
    private void addToWM() {
    android.util.Log.d("DVRTimerView", "addTO");
        windowManager = (WindowManager) mContext.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);

        wmParams.type = LayoutParams.TYPE_APPLICATION_OVERLAY;//TYPE_PHONE;
        wmParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
        if(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())== LayoutDirection.RTL){
            wmParams.gravity = Gravity.BOTTOM | Gravity.END;
        }else {
            wmParams.gravity = Gravity.BOTTOM | Gravity.START;
        }
        wmParams.format = android.graphics.PixelFormat.TRANSLUCENT;
        getContentView().setBackgroundResource(
                R.drawable.translucent_background);

        mRootView = getContentView();
        mInfo = ((TextView) mRootView.findViewById(R.id.info));
        setDefaultLocation();
        try
        {
            windowManager.addView(mRootView, wmParams);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Class clazz = Class.forName("android.widget.PopupWindow");
            java.lang.reflect.Method setShowing =
                clazz.getDeclaredMethod("setShowing", boolean.class);
            setShowing.invoke(this, true);
        } catch (Exception e) {
            android.util.Log.d("DVRTimerView", "Exception " + e);
        }
    }

    public void doSomething() {
        super.doSomething();
        android.util.Log.d("DVRTimerView", "dosomething");
    }

    public void setDefaultLocation() {
        wmParams.x = defaultOffsetX;
        wmParams.y =(int)(DvrManager.getInstance().getTVHeight() * 0.07);//defaultOffsetY;

        wmParams.width = defaultWidth;
        wmParams.height = defaultHeight;
    }

    public void changeLocation() {

        wmParams.x = 0;
        wmParams.y = 110;
        windowManager.updateViewLayout(mRootView, wmParams);
    }

    /**
     *
     */
    @Override
    public void dismiss() {
    android.util.Log.d("DVRTimerView", "dismiss");
        if (windowManager != null && mRootView != null) {
            try {
                windowManager.removeView(mRootView);
            } catch (Exception e) {
                e.toString();
            }
        }

        if (getmOnDismissListener() != null) {
            getmOnDismissListener().onDismiss();
        }

        try {
            Class clazz = Class.forName("android.widget.PopupWindow");
            java.lang.reflect.Method setShowing =
                clazz.getDeclaredMethod("setShowing", boolean.class);
            setShowing.invoke(this, false);
        } catch (Exception e) {
            android.util.Log.d("DVRTimerView", "Exception " + e);
        }
    }

    public OnDismissListener getmOnDismissListener() {
        return mOnDismissListener;
    }

    public void setmOnDismissListener(OnDismissListener mOnDismissListener) {
        this.mOnDismissListener = mOnDismissListener;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }


    @Override public void startTimeTask(BaseInfoBar bBar) {
        Log.v(TAG, " startTimeTask do nothing");
       //super.startTimeTask(bBar);
    }
}