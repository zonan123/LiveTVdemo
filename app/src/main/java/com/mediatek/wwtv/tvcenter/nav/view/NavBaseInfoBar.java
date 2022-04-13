/**
 * @Description: TODO()
 */
package com.mediatek.wwtv.tvcenter.nav.view;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;


/**
 *
 */
public class NavBaseInfoBar extends PopupWindow implements ComponentStatusListener.ICStatusListener {

	private Long mDefaultDuration = 2L * 1000;

	public Activity mContext;
//	private static NavBaseInfoBar mSelf;
	private MyHandler mHandler;

	static class MyHandler extends Handler {
		WeakReference<Activity> mActivity;

		MyHandler(Activity activity) {
			mActivity = new WeakReference<Activity>(activity);
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			default:
//				mSelf.doSomething();
				break;
			}
			super.handleMessage(msg);
		}

	}

	public NavBaseInfoBar(Activity context, int layoutID) {
		super(context.getLayoutInflater().inflate(layoutID, null),
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		initView();
//		mSelf = this;
		this.mHandler = new MyHandler(context);
		this.mContext = context;

		//Add component status Listener
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_COMPONENT_HIDE, this);
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_KEY_OCCUR, this);
	}

	/**
	 * @param context
	 * @param layoutID
	 * @param duration
	 * @param i
	 * @param j
	 */
	public NavBaseInfoBar(Activity context, int layoutID, Long duration,
			int width, int height) {
		super(context.getLayoutInflater().inflate(layoutID, null), width,
				height);
		initView();

		this.mContext = context;
		setDuration(Long.valueOf(duration));

//		mSelf = this;
		this.mHandler = new MyHandler(context);
	}

	/**
	 * @param duration
	 */
	public void setDuration(Long duration) {
		this.mDefaultDuration = duration;
	}

	public void show() {
		setLocation();
        initView();
        mHandler.postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				dismiss();
			}
        }, mDefaultDuration);
    }

	/**
	 *
	 */
	protected void setLocation() {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.e("mContext", "mContext:"+mContext);
	    View view=mContext.findViewById(R.id.linear_glview);
		showAtLocation(view,
				Gravity.CENTER, 20, view.getHeight()/2-view.getHeight()/2/3);
	}

	/**
	 * Init all views.
	 * <p>
	 * use <code> getContentView().findViewById(R.id.xxx); </code>
	 */
	public void initView() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateComponentStatus(int statusID, int value) {
		//Add component status Listener
		this.dismiss();
	}
}
