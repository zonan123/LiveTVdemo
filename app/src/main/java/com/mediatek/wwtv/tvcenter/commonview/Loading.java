package com.mediatek.wwtv.tvcenter.commonview;

//import java.util.Timer;
//import java.util.TimerTask;



import android.content.Context;
//import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/*
 * for three points...
 */

public class Loading extends TextView {
    private static String TAG = "DvrFilelist";
    private boolean isLoading=false;
	private int count = 0;
	private static final long delay = 500;
	private static final long period = 500;
	private final static int REFRESH = 0;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH:
				startLoading();
				break;
				default:
				    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "default");
				    break;
			}
			super.handleMessage(msg);
		}

	};

	public Loading(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void drawLoading() {
	    isLoading=true;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "drawLoading()");
		count = 0;
		mHandler.removeMessages(REFRESH);
		mHandler.sendEmptyMessageDelayed(REFRESH, delay);
	}

	private void startLoading() {
		if (getVisibility() != View.VISIBLE) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "drawLoading() setVisibility");
			setVisibility(View.VISIBLE);
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count % 4; i++) {
			builder.append(" .");
		}
		final String temp = builder.toString();
			post(new Runnable() {
			public void run() {
				setText(temp);
			}
		});
		count++;

		mHandler.sendEmptyMessageDelayed(REFRESH, period);
	}

	// stop drawing
	public void stopDraw() {
	    isLoading=false;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopDraw()");
		mHandler.removeMessages(REFRESH);
		count = 0;
		setText("");
	}
    public boolean isLoading(){
        return this.isLoading;
    }
/*	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}*/

}