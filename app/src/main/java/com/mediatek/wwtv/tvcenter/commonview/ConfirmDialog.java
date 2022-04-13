package com.mediatek.wwtv.tvcenter.commonview;

//import java.util.Timer;
//import java.util.TimerTask;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
//import android.os.Looper;
import android.os.Message;

import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.ScreenConstant;


public class ConfirmDialog extends Dialog {
    private static final String TAG = "ConfirmDialog";

    private String mTitle = "";
    private String mTips  = "";
    private String mYes   = "";
    private String mNo    = "";

    private int mSeconds = -1;

    private Handler mHandler;
    private WindowManager.LayoutParams lp;

    public static final int BTN_YES_CLICK = 0;
    public static final int BTN_NO_CLICK  = 1;

    private IResultCallback mCallback = null;

    public ConfirmDialog(Context context, String tips) {
        this(context, tips,
                context.getString(R.string.common_dialog_msg_yes),
                context.getString(R.string.common_dialog_msg_no));
    }

    public ConfirmDialog(Context context, String tips,
            String btnYesStr, String btnNoStr) {
        super(context, R.layout.menu_comm_long_dialog);

        mTips = tips;
        mYes  = btnYesStr;
        mNo   = btnNoStr;

        lp = this.getWindow().getAttributes();

        lp.width  = (int) (ScreenConstant.SCREEN_WIDTH * 0.55);
        lp.height = (int) (ScreenConstant.SCREEN_HEIGHT * 0.2);
        this.getWindow().setAttributes(lp);
    }

    /**
     *
     * @param title
     */
    public void setTitle(String title){
        mTitle = title;
    }

    public void setCallback(IResultCallback callback){
        mCallback = callback;
    }

    /**
     *
     * @param xoff
     * @param yoff
     */
    public void setPositon(int xoff, int yoff) {
        lp.x = xoff;
        lp.y = yoff;
        this.getWindow().setAttributes(lp);
    }

    /**
     *
     * @param width
     * @param height
     */
    public void setSize(int width, int height){
        lp.width  = width;
        lp.height = height;
        this.getWindow().setAttributes(lp);
    }

    /**
     *
     * @param second
     */
    public void setTimeout(int second){
        mSeconds = second;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.menu_comm_long_dialog);
        if(mTitle.length() == 0){
            ((TextView)findViewById(R.id.comm_dialog_title)).setVisibility(View.GONE);
        }
        else{
            ((TextView)findViewById(R.id.comm_dialog_title)).setText(mTitle);
        }

        ((TextView)findViewById(R.id.comm_dialog_text)).setText(mTips);
        Button btn = (Button)findViewById(R.id.comm_dialog_buttonYes);
        btn.setText(mYes);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View paramView)
            {
                endDialog(BTN_YES_CLICK);
            }
        });

        btn = (Button)findViewById(R.id.comm_dialog_buttonNo);
        btn.setText(mNo);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View paramView)
            {
                endDialog(BTN_NO_CLICK);
            }
        });

        //disable animation
        this.getWindow().setWindowAnimations(0);
    }


    private void endDialog(int result)
    {
        dismiss();

        if(mHandler != null){
            mHandler.removeMessages(BTN_NO_CLICK);
        }

        if(mCallback != null){
            mCallback.handleUserSelection(result);
        }
    }

    public void showDialog()
    {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "msg.what:" + msg.what);
                if(msg.what == BTN_NO_CLICK){
                    endDialog(BTN_NO_CLICK);
                }
            }
        };

        if(mSeconds > 0){
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mSeconds:" + mSeconds);

            Message msg = Message.obtain();
            msg.what = BTN_NO_CLICK;

            mHandler.sendMessageDelayed(msg, mSeconds);
        }

        super.show();
    }

    public interface IResultCallback{
        /*public*/ void handleUserSelection(int result);
    }
}
