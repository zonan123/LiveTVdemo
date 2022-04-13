package com.mediatek.wwtv.setting.preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

//import android.support.v4.content.res.TypedArrayUtils;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
//import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceViewHolder;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.TextToSpeechUtil;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.setting.util.Util;



public class ProgressPreference extends Preference{
    static final String TAG = "ProgressPreference";

    private boolean isPositionView = false;

    private ProgressBar mProgressView;
    private SeekBar mSeekBarView;
    private TextView mValueView;
    private boolean isSeekbar=true;
    private boolean isTTSEnable=false;
    private final TextToSpeechUtil ttUtil;

    private boolean isClickable = true;
    private Context mContext;

    private int mCurrentValue = 50;
    private int mMinValue = 0;
    private int mMaxValue = 100;
    private int mStep = 1;

    public ProgressPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context.getApplicationContext();
        isTTSEnable = Util.isTTSEnabled(context);
        ttUtil = new TextToSpeechUtil(context);
        setLayoutResource(R.layout.progress_preference);
        setPositionView(false);
    }

    public ProgressPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.preferenceStyle,
                android.R.attr.preferenceStyle));
    }

    public ProgressPreference(Context context) {
        this(context, null);
    }

    public void setCurrentValue(int value) {
        mCurrentValue = value;
    }

    public int getCurrentValue() {
        return mCurrentValue;
    }

    public void setMinValue(int value) {
        mMinValue = value;
    }

    public int getMinValue() {
        return mMinValue;
    }

    public void setMaxValue(int value) {
        mMaxValue = value;
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setPositionView(boolean value) {
        isPositionView = value;
    }

    public boolean isPositionView() {
        return isPositionView;
    }
    /**
     * @return the mStep
     */
    public int getmStep() {
        return mStep;
    }

    /**
     * @param mStep the mStep to set
     */
    public void setmStep(int mStep) {
        this.mStep = mStep;
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onBindViewHolder.");

        mProgressView =(ProgressBar) holder.findViewById(R.id.preference_progress_view);
        mSeekBarView = (SeekBar)holder.findViewById(R.id.preference_seekbar_view);
        mValueView = (TextView) holder.findViewById(R.id.preference_progress_value);

        mSeekBarView.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mSeekBarView.setAccessibilityDelegate(mAccDelegate);
        mProgressView.setFocusableInTouchMode(false);
        mProgressView.setAccessibilityDelegate(mAccDelegate);
        if(isClickable){
            holder.itemView.setOnKeyListener(mTextKeyListener);
        }else {
            holder.itemView.setOnKeyListener(null);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onBindViewHolder " +isPositionView +" mCurrentValue: "+mCurrentValue);
        if(isPositionView){
            mSeekBarView.setVisibility(View.VISIBLE);
            mProgressView.setVisibility(View.GONE);
        }else{
             mSeekBarView.setVisibility(View.GONE);
             mProgressView.setVisibility(View.VISIBLE);
        }

        bindData();
    }

    private void bindData() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "bindData.");
        if(mValueView == null || mProgressView == null){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "retrun..........");
            return;
        }
        mValueView.setText(mContext.getResources().getString(R.string.nav_percent_sign, getCurrentValue()));

        if(isPositionView){
            mSeekBarView.setMax(getMaxValue() - getMinValue());
            mSeekBarView.setProgress(getCurrentValue() - getMinValue());
        }
        else {
            mProgressView.setMax(getMaxValue() - getMinValue());
            mProgressView.setProgress(getCurrentValue() - getMinValue());
        }
    }
    public void setValue(int value){
        setCurrentValue(value);
        bindData();
    }

    View.OnKeyListener mTextKeyListener = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKey." + keyCode);

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                int step = 0;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSeekbar." + !isSeekbar);
                if(isTTSEnable){
                   if(keyCode == KeyMap.KEYCODE_MTKIR_RED) {
                        step = -1;
                    }else if(keyCode == KeyMap.KEYCODE_MTKIR_GREEN) {
                        step = 1;
                    }
                }else{
                    if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                      step = -1;
                    }
                     else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        step = 1;
                     }
                    else {
                       return false;
                   }
                }
                step = mCurrentValue + step*getmStep();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "step = " + step + "  mCurrentValue = " + mCurrentValue);
                if(step > mMaxValue || step < mMinValue) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "invalid new value." + step);
                    return false;
                }

                setValue(step);
                if(isTTSEnable){
                    ttUtil.speak(step + "");
                }
                if(isPositionView){
                    mSeekBarView.setProgress(getCurrentValue() - getMinValue());
                }
                else {
                    mProgressView.setProgress(getCurrentValue() - getMinValue());
                }

                mValueView.setText(String.valueOf(getCurrentValue()));

                callChangeListener(String.valueOf(getCurrentValue()));
            }

            return false;
        }
    };

    OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
        /**
         * Notification that the progress level has changed. Clients can use the fromUser parameter
         * to distinguish user-initiated changes from those that occurred programmatically.
         *
         * @param seekBar The SeekBar whose progress has changed
         * @param progress The current progress level. This will be in the range 0..max where max
         *        was set by {@link ProgressBar#setMax(int)}. (The default value for max is 100.)
         * @param fromUser True if the progress change was initiated by the user.
         */
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onProgressChanged. " + progress + ", " + fromUser);
            setCurrentValue(progress);

            bindData();
        }

        /**
         * Notification that the user has started a touch gesture. Clients may want to use this
         * to disable advancing the seekbar.
         * @param seekBar The SeekBar in which the touch gesture began
         */
        public void onStartTrackingTouch(SeekBar seekBar) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStartTrackingTouch. ");
        }

        /**
         * Notification that the user has finished a touch gesture. Clients may want to use this
         * to re-enable advancing the seekbar.
         * @param seekBar The SeekBar in which the touch gesture began
         */
        public void onStopTrackingTouch(SeekBar seekBar) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStopTrackingTouch. ");
        }
    };

    private AccessibilityDelegate mAccDelegate = new AccessibilityDelegate() {
        public void sendAccessibilityEvent(View host, int eventType) {
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sendAccessibilityEvent.1 " + eventType + ",isSeekbar "+isSeekbar +" , " +host);
             if(host.getId() == R.id.preference_seekbar_view){
                 isSeekbar = true;
             }else{
                 isSeekbar =false;
             }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sendAccessibilityEvent. 2 " + eventType + ",isSeekbar "+isSeekbar +" , "  + host);
        }
    };
}
