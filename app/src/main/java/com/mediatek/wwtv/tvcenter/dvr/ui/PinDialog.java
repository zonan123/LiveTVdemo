package com.mediatek.wwtv.tvcenter.dvr.ui;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import android.widget.TextView;



import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.MtkTvConfig;

public class PinDialog  extends Dialog {
    private static final String TAG = "PinDialog";

    private Context context;

    /**
     * PIN code dialog for change parental control settings
     */
    public static final int PIN_DIALOG_TYPE_ENTER_PIN = 2;

    /**
     * PIN code dialog for set new PIN
     */
    public static final int PIN_DIALOG_TYPE_NEW_PIN = 3;

    // PIN code dialog for checking old PIN. This is intenal only.
    private static final int PIN_DIALOG_TYPE_OLD_PIN = 4;

    private static final int PIN_DIALOG_RESULT_SUCCESS = 0;
    private static final int PIN_DIALOG_RESULT_FAIL = 1;

    private static final int MAX_WRONG_PIN_COUNT = 5;
    private static final int DISABLE_PIN_DURATION_MILLIS = 60 * 1000; // 1 minute

    public interface ResultListener {
        void done(boolean success);
        void play(String pin);
    }

    private static final int NUMBER_PICKERS_RES_ID[] = {
            R.id.first, R.id.second, R.id.third, R.id.fourth };

    private int mType;
    private ResultListener mListener;
    private int mRetCode;

    private TextView mWrongPinView;
    private View mEnterPinView;
    private TextView mTitleView;
    private PinNumberPicker[] mPickers;
    private String mPrevPin;
    private int mWrongPinCount;
    private long mDisablePinUntil;
    private final Handler mHandler = new Handler();

    public  long getPinDisabledUntil(){
        return mDisablePinUntil;
    };
    public void setPinDisabledUntil(long retryDisableTimeout){
        this.mDisablePinUntil = retryDisableTimeout;
    }
    public void setPin(String pin){
         MtkTvConfig.getInstance().setConfigString(MtkTvConfigTypeBase.CFG_PWD_PASSWORD,pin);
    }
    public  boolean isPinCorrect(String pin){
        //if (MtkTvPWDDialog.getInstance().checkPWD(pin)) {
        //   return true;
        //}
        return MtkTvPWDDialog.getInstance().checkPWD(pin);
    }
    public  boolean isPinSet(){
        return false;
    }

    public PinDialog(int type, ResultListener listener, Context context) {
        super(context, R.style.nav_dialog);

        mType = type;
        mListener = listener;
        mRetCode = PIN_DIALOG_RESULT_FAIL;
        this.context = context;
        PinNumberPicker.loadResources(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDisablePinUntil = getPinDisabledUntil();

        initView();
    }

    private void initView() {
        setContentView(R.layout.pin_dvr_dialog);

        mWrongPinView = (TextView)findViewById(R.id.wrong_pin);
        mEnterPinView = findViewById(R.id.enter_pin);
        mTitleView = (TextView)mEnterPinView.findViewById(R.id.title);

        switch (mType) {
            case PIN_DIALOG_TYPE_ENTER_PIN:
                mTitleView.setText(context.getString(R.string.menu_parental_password_enter));
                break;
            case PIN_DIALOG_TYPE_NEW_PIN:
                if (!isPinSet()) {
                    mTitleView.setText(context.getString(R.string.menu_parental_password_new));
                } else {
                    mTitleView.setText("Reinput New Password");
                    mType = PIN_DIALOG_TYPE_OLD_PIN;
                }
                break;
            default:
                break;

        }

        mPickers = new PinNumberPicker[NUMBER_PICKERS_RES_ID.length];
        for (int i = 0; i < NUMBER_PICKERS_RES_ID.length; i++) {
            mPickers[i] = (PinNumberPicker) findViewById(NUMBER_PICKERS_RES_ID[i]);
            mPickers[i].setValueRange(0, 9);
            mPickers[i].setPinDialog(this);
            mPickers[i].updateFocus();
        }
        for (int i = 0; i < NUMBER_PICKERS_RES_ID.length - 1; i++) {
            mPickers[i].setNextNumberPicker(mPickers[i + 1]);
        }
        mPickers[0].requestFocus();

        updateWrongPin();
    }

    private final Runnable mUpdateEnterPinRunnable = new Runnable() {
        @Override
        public void run() {
            updateWrongPin();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.width  = (int) (ScreenConstant.SCREEN_WIDTH * 0.55);
        lp.height = (int) (ScreenConstant.SCREEN_HEIGHT * 0.35);
        lp.gravity= Gravity.CENTER ;
        this.getWindow().setAttributes(lp);
    }

    private void updateWrongPin() {
        long timer = (mDisablePinUntil - System.currentTimeMillis()) / 1000;
        boolean enabled = timer < 1;
        if (enabled) {
            mWrongPinView.setVisibility(View.INVISIBLE);
            mEnterPinView.setVisibility(View.VISIBLE);
            mWrongPinCount = 0;
        } else {
            mEnterPinView.setVisibility(View.INVISIBLE);
            mWrongPinView.setVisibility(View.VISIBLE);
            if(mType == PIN_DIALOG_TYPE_NEW_PIN){
                 mWrongPinView.setText(context
                            .getString(R.string.menu_parental_password_matchinfo));
            }else{
                mWrongPinView.setText(context
                        .getString(R.string.menu_parental_password_incorrect));
            }
            mHandler.postDelayed(mUpdateEnterPinRunnable, 2000);
        }
    }

    private void exit(int retCode,String pin) {
        mRetCode = retCode;
        if (mListener != null) {
            mListener.done(mRetCode == PIN_DIALOG_RESULT_SUCCESS);
            mListener.play(pin);
        }

        dismiss();
    }

    private void handleWrongPin() {
		mWrongPinCount++;
        if (mWrongPinCount >= MAX_WRONG_PIN_COUNT) {
            mDisablePinUntil = System.currentTimeMillis() + DISABLE_PIN_DURATION_MILLIS;
            setPinDisabledUntil(mDisablePinUntil);
            updateWrongPin();
        } else {
            mDisablePinUntil = System.currentTimeMillis() + 2000;
            setPinDisabledUntil(mDisablePinUntil);
            updateWrongPin();
        }
    }


    private void done(String pin) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "done: mType=" + mType + " pin=" + pin);
        switch (mType) {
            case PIN_DIALOG_TYPE_ENTER_PIN:
                // TODO: Implement limited number of retrials and timeout logic.
                if (isPinCorrect(pin)) {
                    resetPinInput();
                    exit(PIN_DIALOG_RESULT_SUCCESS,pin);
                } else {
                    resetPinInput();
                    handleWrongPin();
                }
                break;
            case PIN_DIALOG_TYPE_NEW_PIN:
                resetPinInput();
                if (mPrevPin == null) {
                    mPrevPin = pin;
                    mTitleView.setText(context.getString(R.string.menu_parental_password_new_re));
                } else {
                    if (pin.equals(mPrevPin)) {
                        setPin(pin);
                        exit(PIN_DIALOG_RESULT_SUCCESS,pin);
                    } else {
                        mTitleView.setText(context.getString(R.string.menu_parental_password_new));
                        mPrevPin = null;
                        handleWrongPin();
                    }
                }
                break;
            case PIN_DIALOG_TYPE_OLD_PIN:
                if (isPinCorrect(pin)) {
                    mType = PIN_DIALOG_TYPE_NEW_PIN;
                    resetPinInput();
                    mTitleView.setText(context.getString(R.string.menu_parental_password_new));
                } else {
                    handleWrongPin();
                }
                break;
            default:
                break;
        }
    }

    public int getType() {
        return mType;
    }

    private String getPinInput() {
        String result = "";
        try {
            for (PinNumberPicker pnp : mPickers) {
                //pnp.updateText();
                result += pnp.getValue();
            }
        } catch (IllegalStateException e) {
            result = "";
        }
        return result;
    }

    private void resetPinInput() {
        for (PinNumberPicker pnp : mPickers) {
            pnp.setValueRange(0, 9);
        }
        mPickers[0].requestFocus();
    }

    public void setResultListener(ResultListener listener) {
        mListener = listener;
    }

    public static final class PinNumberPicker extends FrameLayout {
        private static final int NUMBER_VIEWS_RES_ID[] = {
            R.id.previous2_number,
            R.id.previous_number,
            R.id.current_number,
            R.id.next_number,
            R.id.next2_number };
        private static final int CURRENT_NUMBER_VIEW_INDEX = 2;

        private static Animator sFocusedNumberEnterAnimator;
        private static Animator sFocusedNumberExitAnimator;
        private static Animator sAdjacentNumberEnterAnimator;
        private static Animator sAdjacentNumberExitAnimator;

        private static float sAlphaForFocusedNumber;
        private static float sAlphaForAdjacentNumber;

        private int mMinValue;
        private int mMaxValue;
        private int mCurrentValue;
        private int mNextValue;
        private int mNumberViewHeight;
        private PinDialog mDialog;
        private PinNumberPicker mNextNumberPicker;
        private boolean mCancelAnimation;

        private final View mNumberViewHolder;
        private final View mBackgroundView;
        private final TextView[] mNumberViews;
        private final OverScroller mScroller;

        public PinNumberPicker(Context context) {
            this(context, null);
        }

        public PinNumberPicker(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public PinNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
            this(context, attrs, defStyleAttr, 0);
        }

        public PinNumberPicker(Context context, AttributeSet attrs, int defStyleAttr,
                int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            View view = inflate(context, R.layout.pin_number_picker, this);
            mNumberViewHolder = view.findViewById(R.id.number_view_holder);
            mBackgroundView = view.findViewById(R.id.focused_background);
            mNumberViews = new TextView[NUMBER_VIEWS_RES_ID.length];
            for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
                mNumberViews[i] = (TextView) view.findViewById(NUMBER_VIEWS_RES_ID[i]);
            }
            Resources resources = context.getResources();
            mNumberViewHeight = resources.getDimensionPixelOffset(
                    R.dimen.pin_number_picker_text_view_height);

            mScroller = new OverScroller(context);

            mNumberViewHolder.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    updateFocus();
                }
            });

            mNumberViewHolder.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_DPAD_UP:
                            case KeyEvent.KEYCODE_DPAD_DOWN: {
                                if (!mScroller.isFinished() || mCancelAnimation) {
                                    endScrollAnimation();
                                }
                                if (mScroller.isFinished() || mCancelAnimation) {
                                    mCancelAnimation = false;
                                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                                        mNextValue = adjustValueInValidRange(mCurrentValue + 1);
                                        startScrollAnimation(true);
                                        mScroller.startScroll(0, 0, 0, mNumberViewHeight,
                                                getResources().getInteger(
                                                        R.integer.pin_number_scroll_duration));
                                    } else {
                                        mNextValue = adjustValueInValidRange(mCurrentValue - 1);
                                        startScrollAnimation(false);
                                        mScroller.startScroll(0, 0, 0, -mNumberViewHeight,
                                                getResources().getInteger(
                                                        R.integer.pin_number_scroll_duration));
                                    }
                                    updateText();
                                    invalidate();
                                }
                                return true;
                            }
                            default:
                                break;
                        }
                    } else if (event.getAction() == KeyEvent.ACTION_UP) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_DPAD_UP:
                            case KeyEvent.KEYCODE_DPAD_DOWN: {
                                mCancelAnimation = true;
                                return true;
                            }
                            default:
                                break;
                        }
                    }
                    return false;
                }
            });
            mNumberViewHolder.setScrollY(mNumberViewHeight);
        }

        static synchronized void loadResources(Context context) {
            if (sFocusedNumberEnterAnimator == null) {
                TypedValue outValue = new TypedValue();
                context.getResources().getValue(
                        R.dimen.pin_alpha_for_focused_number, outValue, true);
                sAlphaForFocusedNumber = outValue.getFloat();
                context.getResources().getValue(
                        R.dimen.pin_alpha_for_adjacent_number, outValue, true);
                sAlphaForAdjacentNumber = outValue.getFloat();

                sFocusedNumberEnterAnimator = AnimatorInflater.loadAnimator(context,
                        R.animator.pin_focused_number_enter);
                sFocusedNumberExitAnimator = AnimatorInflater.loadAnimator(context,
                        R.animator.pin_focused_number_exit);
                sAdjacentNumberEnterAnimator = AnimatorInflater.loadAnimator(context,
                        R.animator.pin_adjacent_number_enter);
                sAdjacentNumberExitAnimator = AnimatorInflater.loadAnimator(context,
                        R.animator.pin_adjacent_number_exit);
            }
        }

        @Override
        public void computeScroll() {
            super.computeScroll();
            if (mScroller.computeScrollOffset()) {
                mNumberViewHolder.setScrollY(mScroller.getCurrY() + mNumberViewHeight);
                updateText();
                invalidate();
            } else if (mCurrentValue != mNextValue) {
                mCurrentValue = mNextValue;
            }
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                int keyCode = event.getKeyCode();
                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                    setNextValue(keyCode - KeyEvent.KEYCODE_0);
                    if(mNextNumberPicker==null){
                        if (mScroller.isFinished() || mCancelAnimation) {
                            mCancelAnimation = false;
                            startScrollAnimation(true);
                            mScroller.startScroll(0, 0, 0, 0,
                                      getResources().getInteger(
                                           R.integer.pin_number_scroll_duration));
                            mCurrentValue = mNextValue;
                            updateText();
                            invalidate();
                            endScrollAnimation();

                        }
                        return true;
                    }
                } else if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER
                        && keyCode != KeyEvent.KEYCODE_ENTER) {
                    return super.dispatchKeyEvent(event);
                }
                if (mNextNumberPicker == null) {
                    String pin = mDialog.getPinInput();
                    if (!TextUtils.isEmpty(pin)) {
                        mDialog.done(pin);
                    }
                } else {
                    mNextNumberPicker.requestFocus();
                }
                return true;
            }
            return super.dispatchKeyEvent(event);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            mNumberViewHolder.setFocusable(enabled);
            for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
                mNumberViews[i].setEnabled(enabled);
            }
        }

        void startScrollAnimation(boolean scrollUp) {
            if (scrollUp) {
                sAdjacentNumberExitAnimator.setTarget(mNumberViews[1]);
                sFocusedNumberExitAnimator.setTarget(mNumberViews[2]);
                sFocusedNumberEnterAnimator.setTarget(mNumberViews[3]);
                sAdjacentNumberEnterAnimator.setTarget(mNumberViews[4]);
            } else {
                sAdjacentNumberEnterAnimator.setTarget(mNumberViews[0]);
                sFocusedNumberEnterAnimator.setTarget(mNumberViews[1]);
                sFocusedNumberExitAnimator.setTarget(mNumberViews[2]);
                sAdjacentNumberExitAnimator.setTarget(mNumberViews[3]);
            }
            sAdjacentNumberExitAnimator.start();
            sFocusedNumberExitAnimator.start();
            sFocusedNumberEnterAnimator.start();
            sAdjacentNumberEnterAnimator.start();
        }

        void endScrollAnimation() {
            sAdjacentNumberExitAnimator.end();
            sFocusedNumberExitAnimator.end();
            sFocusedNumberEnterAnimator.end();
            sAdjacentNumberEnterAnimator.end();
            mCurrentValue = mNextValue;
            mNumberViews[1].setAlpha(sAlphaForAdjacentNumber);
            mNumberViews[2].setAlpha(sAlphaForFocusedNumber);
            mNumberViews[3].setAlpha(sAlphaForAdjacentNumber);
        }

        void setValueRange(int min, int max) {
            if (min > max) {
                throw new IllegalArgumentException(
                        "The min value should be greater than or equal to the max value");
            }
            mMinValue = min;
            mMaxValue = max;
            mNextValue = mCurrentValue = mMinValue - 1;
            clearText();
            mNumberViews[CURRENT_NUMBER_VIEW_INDEX].setText("—");
        }

        void setPinDialog(PinDialog dlg) {
            mDialog = dlg;
        }

        void setNextNumberPicker(PinNumberPicker picker) {
            mNextNumberPicker = picker;
        }

        int getValue() {
            if (mCurrentValue < mMinValue || mCurrentValue > mMaxValue) {
                throw new IllegalStateException("Value is not set");
            }
            return mCurrentValue;
        }

        // Will take effect when the focus is updated.
        void setNextValue(int value) {
            if (value < mMinValue || value > mMaxValue) {
                throw new IllegalStateException("Value is not set");
            }
            mNextValue = adjustValueInValidRange(value);
        }

        void updateFocus() {
            endScrollAnimation();
            if (mNumberViewHolder.isFocused()) {
                mBackgroundView.setVisibility(View.VISIBLE);
                updateText();
            } else {
                mBackgroundView.setVisibility(View.GONE);
                if (!mScroller.isFinished()) {
                    mCurrentValue = mNextValue;
                    mScroller.abortAnimation();
                }
                clearText();
                mNumberViewHolder.setScrollY(mNumberViewHeight);
            }
        }

        private void clearText() {
            for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
                if (i != CURRENT_NUMBER_VIEW_INDEX) {
                    mNumberViews[i].setText("");
                } else if (mCurrentValue >= mMinValue && mCurrentValue <= mMaxValue) {
                    mNumberViews[i].setText(String.valueOf(mCurrentValue));
                }
            }
        }

        private void updateText() {
            if (mNumberViewHolder.isFocused()) {
                if (mCurrentValue < mMinValue || mCurrentValue > mMaxValue) {
                    mNextValue = mCurrentValue = mMinValue;
                }
                int value = adjustValueInValidRange(mCurrentValue - CURRENT_NUMBER_VIEW_INDEX);
                for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
                    mNumberViews[i].setText(String.valueOf(adjustValueInValidRange(value)));
                    value = adjustValueInValidRange(value + 1);
                }
            }
        }

        private int adjustValueInValidRange(int value) {
            int interval = mMaxValue - mMinValue + 1;
            if (value < mMinValue - interval || value > mMaxValue + interval) {
                throw new IllegalArgumentException("The value( " + value
                        + ") is too small or too big to adjust");
            }
            return (value < mMinValue) ? value + interval
                    : (value > mMaxValue) ? value - interval : value;
        }
    }
}
