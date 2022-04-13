package com.mediatek.wwtv.setting.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

/**
 * Created by sin_chuanfeicai on 4/6/2021
 */
public class ChannelOptionsNumDialog extends Dialog {

    private static final String TAG = "ChannelOptionsNumDialog";
    private TextView mTxtNum;

    private static final int NAV_TIMEOUT = 3 * 1000;// 3S
    private static final int MSG_INPUT_DIGIT_NUM = 1;
    private OnTurnCHCallback mOnTurnCHCallback;
    private String mStrInputNum = "";
    private int mCurKeyCode;
    private Context mContext;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "msg.what=" + msg.what);
            switch (msg.what) {
                case MSG_INPUT_DIGIT_NUM:
                    Log.d(TAG, "handleMessage----->mStrInputNum=" + mStrInputNum);
                    if (mOnTurnCHCallback != null)
                        mOnTurnCHCallback.onTurnCH(mStrInputNum);
                    dismiss();
                    break;
            }
        }

    };

    public void show() {
        super.show();
        mHandler.removeMessages(MSG_INPUT_DIGIT_NUM);
        mHandler.sendEmptyMessageDelayed(MSG_INPUT_DIGIT_NUM, NAV_TIMEOUT);
    }

    public void addTurnCHCallback(OnTurnCHCallback onTurnCHCallback) {
        mOnTurnCHCallback = onTurnCHCallback;
    }
    public ChannelOptionsNumDialog(@NonNull Context context, int keyCode) {
        super(context, R.style.Transparent);
        mContext = context;
        mCurKeyCode = keyCode;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_num_channel_options);
        initView();
        initData();
    }

    private void initView() {
        getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        mTxtNum = getWindow().findViewById(R.id.txt_num);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.TOP | Gravity.END);
        lp.x = 250;
        lp.y = 500;
        lp.height = 160;
        lp.width = 240;
        window.setAttributes(lp);
    }

    private void initData() {
        mStrInputNum = mCurKeyCode + "";
        mTxtNum.setText(mStrInputNum);
    }

    //@Override
    //public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
    //    switch (keyCode)
    //    return super.onKeyDown(keyCode, event);
    //}

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        Log.d(TAG, "dispatchKeyEvent: " + event.getKeyCode());
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyMap.KEYCODE_0:
                case KeyMap.KEYCODE_1:
                case KeyMap.KEYCODE_2:
                case KeyMap.KEYCODE_3:
                case KeyMap.KEYCODE_4:
                case KeyMap.KEYCODE_5:
                case KeyMap.KEYCODE_6:
                case KeyMap.KEYCODE_7:
                case KeyMap.KEYCODE_8:
                case KeyMap.KEYCODE_9:
                    keyHandler(event.getKeyCode() - KeyMap.KEYCODE_0);
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public interface OnTurnCHCallback {
        void onTurnCH(String chDigit);
    }

    public void keyHandler(int keyCode) {
        Log.d(TAG, "keyHandler----->keyCode=" + keyCode);
        inputNumKey(keyCode);
        mTxtNum.setText(mStrInputNum);
        mHandler.removeMessages(MSG_INPUT_DIGIT_NUM);
        mHandler.sendEmptyMessageDelayed(MSG_INPUT_DIGIT_NUM, NAV_TIMEOUT);
    }

    private void reset() {
        mStrInputNum = "";
    }

    public void dismiss() {
        super.dismiss();
        reset();
        mHandler.removeMessages(MSG_INPUT_DIGIT_NUM);
    }

    private void inputNumKey(int keyCode) {
        if (mStrInputNum.startsWith("0")) {
            mStrInputNum = keyCode + "";
        } else {
            mStrInputNum = mStrInputNum + keyCode;
        }
        mStrInputNum = trimStartsWith0(mStrInputNum);
        mStrInputNum = mStrInputNum.length() > 4 ? mStrInputNum.substring(4, 5)
            : mStrInputNum;
        Log.d(TAG, "inputNumKey----->mStrInputNum=" + mStrInputNum);
    }

    private String trimStartsWith0(String inputStr) {
        if (inputStr.startsWith("0")) {
            inputStr = inputStr.substring(1);
            trimStartsWith0(inputStr);
        }
        return inputStr;
    }
}
