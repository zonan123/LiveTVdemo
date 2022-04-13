package com.mediatek.wwtv.tvcenter.nav.view;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

public class FocusLabel extends LinearLayout {
	static final String TAG = "FocusLabel";

	private int defaultWidth = 60;
	private int defaultHeight = 60;
	private int defaultOffsetX = 0;
	private int defaultOffsetY = 0;

	private int delayTime = 20 * 1000;

	private static final int DEFAULT_WIDTH = 960;
	private static final int DEFAULT_HEIGHT = 540;

	//private Context mContext;

	private ImageView focusImage;
	
	private android.view.ViewGroup.LayoutParams mLayoutParams;
	

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
            android.util.Log.d("Focuslabel","handle");
		};
	};
	private Runnable timerTask = new Runnable() {
		@Override
		public void run() {
			FocusLabel.this.setVisibility(View.INVISIBLE);
		}
	};

	public FocusLabel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public FocusLabel(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	/**
	 * show a pic on current focus window.
	 * 
	 * @param context
	 */
	public FocusLabel(Context context) {
		super(context);
		initView(context);
	}

	public void setSize(int width, int height) {
		mLayoutParams.width = width;
		mLayoutParams.height = height;
		focusImage.setLayoutParams(mLayoutParams);
	}

	public void setPadding(float x, float y) {
		int paddingLeft = (int) (x
				* ScreenConstant.SCREEN_WIDTH  - defaultWidth
				/ 2);
		int paddingTop = (int) (y * ScreenConstant.SCREEN_HEIGHT);
		setTranslationX(paddingLeft);
		setTranslationY(paddingTop);
	}
	
	public void setPaddingWithCallBack(int x, int y) {
		int paddingLeft = x
				* (ScreenConstant.SCREEN_WIDTH / DEFAULT_WIDTH) - defaultWidth
				/ 2;
		int paddingTop = y * (ScreenConstant.SCREEN_HEIGHT / DEFAULT_HEIGHT);
		setTranslationX(paddingLeft);
		setTranslationY(paddingTop);
	}

	public void setDefaultLocation() {
		setTranslationX(defaultOffsetX);
		setTranslationY(defaultOffsetY);
		mLayoutParams.width = defaultWidth;
		mLayoutParams.height = defaultHeight;
		focusImage.setLayoutParams(mLayoutParams);
	}

	public void show() {
		if (this.getTranslationX() < 10) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("FocusLabel", "focus label x position < 10");
			return;
		}
		setDefaultPic();
		setVisibility(View.VISIBLE);
		startTimerTask();
	}

	public void release() {
		setVisibility(View.GONE);
		mHandler.removeCallbacks(timerTask);
	}

	public void setDefaultPic() {
		focusImage.setBackgroundResource(R.drawable.nav_pip_focus_icon);
	}

	private void startTimerTask() {
		mHandler.removeCallbacks(timerTask);

		mHandler.postDelayed(timerTask, delayTime);
	}

	public void hiddenFocus(boolean hidden) {
		if (hidden) {
			mHandler.removeCallbacks(timerTask);
		} else {
			startTimerTask();
		}
	}

	private void initView(Context context) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in focuslabel initView");
		//mContext = context;
		((Activity) context).getLayoutInflater().inflate(
				R.layout.nav_pippop_focuslabel, this);
		focusImage = (ImageView) findViewById(R.id.nav_focus_label_image);
		focusImage.setBackgroundResource(R.drawable.nav_pip_focus_icon);
		mLayoutParams = focusImage.getLayoutParams();
		setDefaultLocation();
	}

}
