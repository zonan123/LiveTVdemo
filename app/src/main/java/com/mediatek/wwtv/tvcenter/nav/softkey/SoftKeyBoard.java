package com.mediatek.wwtv.tvcenter.nav.softkey;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.media.tv.TvInputInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
//import android.widget.Toast;
//import android.widget.RelativeLayout;
import android.app.Activity;

import androidx.annotation.NonNull;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.util.InstrumentationHandler;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.R;


public class SoftKeyBoard extends Dialog{
    private static final String TAG = "SoftKeyBoard";
    private static SoftKeyBoard keyBoard;
    private final int  KEY_UP_MSG = 0x1000;
    private SoftKeyBoard(Context context) {
        super(context, R.style.dialog);
    }

    public static synchronized SoftKeyBoard getInstance(Context context){
        if(keyBoard == null){
            keyBoard = new SoftKeyBoard(context);
        }
        return keyBoard;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.softkey);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate.");
        /**fist line */
        Button one = (Button) this.findViewById(R.id.one);
        Button two = (Button) this.findViewById(R.id.two);
        Button three = (Button) this.findViewById(R.id.three);
        Button four = (Button) this.findViewById(R.id.four);
        Button five = (Button) this.findViewById(R.id.five);
        Button six = (Button) this.findViewById(R.id.six);
        Button seven = (Button) this.findViewById(R.id.seven);
        Button eight = (Button) this.findViewById(R.id.eight);
        Button nine = (Button) this.findViewById(R.id.nine);
        Button zero = (Button) this.findViewById(R.id.zero);

        /**second line */
        ImageButton play = (ImageButton) this.findViewById(R.id.play);
        ImageButton pause = (ImageButton) this.findViewById(R.id.pause);
        ImageButton stop = (ImageButton) this.findViewById(R.id.stop);
//        Button etc = (Button) this.findViewById(R.id.etc);
        Button chplus = (Button) this.findViewById(R.id.chplus);
        Button info = (Button) this.findViewById(R.id.info);
        Button subtitle = (Button) this.findViewById(R.id.subtitle);
        Button ttx = (Button) this.findViewById(R.id.ttx);
        Button audio = (Button) this.findViewById(R.id.audio);
        Button epg = (Button) this.findViewById(R.id.epg);

        /**third line */
        ImageButton previous = (ImageButton) this.findViewById(R.id.previous);
        ImageButton fastrewind = (ImageButton) this.findViewById(R.id.fastrewind);
        ImageButton fastforward = (ImageButton) this.findViewById(R.id.fastforward);
        ImageButton next = (ImageButton) this.findViewById(R.id.next);
        Button chminus = (Button) this.findViewById(R.id.chminus);
        ImageButton record = (ImageButton) this.findViewById(R.id.record);
        ImageButton red = (ImageButton) this.findViewById(R.id.red);
        ImageButton green = (ImageButton) this.findViewById(R.id.green);
        ImageButton yellow = (ImageButton) this.findViewById(R.id.yellow);
        ImageButton blue = (ImageButton) this.findViewById(R.id.blue);

        /**fourth lin*/
        Button input = (Button) this.findViewById(R.id.input);
        Button exit = (Button) this.findViewById(R.id.exit);
        Button mts = (Button) this.findViewById(R.id.mts);

        one.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_1)));
        two.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_2)));
        three.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_3)));
        four.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_4)));
        five.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_5)));
        six.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_6)));
        seven.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_7)));
        eight.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_8)));
        nine.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_9)));
        zero.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_0)));

        play.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_PLAY)));
        pause.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_PAUSE,119)));
        stop.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_STOP)));
//        etc.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_SLASH)));
        chplus.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_CHUP)));
        info.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_INFO)));
        subtitle.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_MTKIR_CC)));
        ttx.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_MTKIR_TTX)));
        audio.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_AUDIO)));
        epg.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_GUIDE)));

        previous.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_PREVIOUS)));
        fastrewind.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_REWIND)));
        fastforward.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_FASTFORWARD)));
        next.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_NEXT)));
        chminus.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_CHDN)));
        record.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_RECORD)));
        red.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_RED)));
        green.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_GREEN)));
        yellow.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_YELLOW)));
        blue.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_BLUE)));

        input.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_TV_INPUT)));
        exit.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK)));
        mts.setOnClickListener(new SoftKeyClickListener(getSoftKeyEvent(KeyEvent.ACTION_DOWN,KeyMap.KEYCODE_MTKIR_MTS)));

        info.setFocusable(true);
        info.setFocusableInTouchMode(true);
        info.requestFocus();
        info.requestFocusFromTouch();

    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown>>>>" + keyCode + "  " + event.getAction());
		switch(keyCode){
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_BACK:
				return super.onKeyDown(keyCode, event);
            default:
		        return onKeyHandler(keyCode,event,true);
		}
	}

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyUp>>>>" + keyCode + "  " + event.getAction());
        return super.onKeyUp(keyCode, event);
    }

    private boolean onKeyHandler(int keyCode, KeyEvent event, boolean isDown) {

            Dialog dialog = DestroyApp.getActiveDialog();
            if(dialog != null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dialog onKeyHandler>>>>" +keyCode + ",isDown+"+isDown);
                if(isDown) {
                    return dialog.onKeyDown(keyCode, event);
                }
                else {
                    return dialog.onKeyUp(keyCode, event);
                }
            }

            Activity activity = DestroyApp.getTopActivity();

            if(activity != null) {
                TvInputInfo tvInputInfo =
                    InputSourceManager.getInstance().getTvInputInfo(InputSourceManager.MAIN);
                if (tvInputInfo != null
                    && tvInputInfo.getType() == TvInputInfo.TYPE_HDMI
                    && tvInputInfo.getHdmiDeviceInfo() != null) {
                    activity.dispatchKeyEvent(event);
                    Message keyUpMsg = handler.obtainMessage(KEY_UP_MSG);
                    keyUpMsg.obj = event;
                    handler.sendMessageDelayed(keyUpMsg,120);
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "activity onKeyHandler>>>>" +keyCode + ",isDown+"+isDown);
                    if(isDown) {
                        return activity.onKeyDown(keyCode, event);
                    }
                    else {
                        return activity.onKeyUp(keyCode, event);
                    }
                }
            }
            return false;
    }

    private KeyEvent getSoftKeyEvent(int action,int keycode) {
        KeyEvent event = new KeyEvent(
            action,
            keycode);
        return event;
    }

    private KeyEvent getSoftKeyEvent(int action,int keycode,int scancode) {
        KeyEvent event = new KeyEvent(0,0,action,keycode,0,0,1,scancode);
        return event;
    }



    @Override
    public void show() {
        Window window = getWindow();
        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        WindowManager.LayoutParams lp = window.getAttributes();
//      lp.x = 0;
        lp.y = 180;
        window.setAttributes(lp);
        window.setGravity(Gravity.BOTTOM);
        super.show();
    }


    private class SoftKeyClickListener implements  View.OnClickListener{
            private KeyEvent keyEvent;

            public SoftKeyClickListener(KeyEvent keyEvent){
                this.keyEvent = keyEvent;
            }

            @Override
            public void onClick(View view){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "keyEvent code" + keyEvent.getKeyCode());
                switch(keyEvent.getKeyCode()){
                    case KeyEvent.KEYCODE_TV_INPUT:
                        dismiss();
                        InstrumentationHandler.getInstance()
                                .sendKeyDownUpSync(KeyEvent.KEYCODE_TV_INPUT);
                        return;
                    case KeyEvent.KEYCODE_BACK:
                    case KeyMap.KEYCODE_MTKIR_MTS:
                    case KeyMap.KEYCODE_MTKIR_RECORD:
                    case KeyMap.KEYCODE_MTKIR_AUDIO:
                        dismiss();
                        break;
                }
                onKeyHandler(keyEvent.getKeyCode(),keyEvent,true);
            }
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case KEY_UP_MSG:
                    KeyEvent event = (KeyEvent) msg.obj;
                    KeyEvent upEvent = event.changeAction(event,KeyEvent.ACTION_UP);
                    Activity activity = DestroyApp.getTopActivity();
                    if(activity != null){
                        activity.dispatchKeyEvent(upEvent);
                    }
                    break;
            }
        }
    };

}
