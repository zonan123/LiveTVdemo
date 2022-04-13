package com.mediatek.wwtv.tvcenter.nav.view;

import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import java.lang.reflect.Field;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.os.Handler;
import android.os.Message;

public class TvView extends ImageView {
	static final String TAG = "TvView";

	private int mScreenWidth;
	private int mScreenHeight;
	private LayoutParams wmParams;
	private WindowManager windowManager;
	private static float viewX;
	private static float viewY;
	private static float viewWidth;
	private static float viewHeight;
//	private static final String PhoneBarChange = "com.mediatek.phonestatus.bar.change";
	private boolean statusBarVisible = true;
	private Context mContext;
	private int lastScreenTypeValue = 0;
	private static final int CHANGE_TV_VIEW_POSITION = 11;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case CHANGE_TV_VIEW_POSITION:
				setTvViewPosition(viewX,viewY,viewWidth,viewHeight);
				break;
			default:
				break;
			}
		};
	};
	
	private Runnable mRunnable = new Runnable(){
		public void run(){
			int messageFullScreenValue = 0;
			try{
				messageFullScreenValue = Settings.System.getInt(mContext.getContentResolver(),"fullScreenOrNot");
			} catch(SettingNotFoundException e){
				e.printStackTrace();
			}
			if(messageFullScreenValue == 0 && lastScreenTypeValue != 0){
				statusBarVisible = true;
				mHandler.removeMessages(CHANGE_TV_VIEW_POSITION);
				mHandler.sendEmptyMessage(CHANGE_TV_VIEW_POSITION);
				lastScreenTypeValue = messageFullScreenValue;
			}else if(messageFullScreenValue == 1 && lastScreenTypeValue != 1){
				statusBarVisible = false;
				mHandler.removeMessages(CHANGE_TV_VIEW_POSITION);
				mHandler.sendEmptyMessage(CHANGE_TV_VIEW_POSITION);
				lastScreenTypeValue = messageFullScreenValue;
			}
			mHandler.postDelayed(this,200);
		}
	};
	
	/*BroadcastReceiver mBroadcastRecevier = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent){
			if(intent.getAction() == PhoneBarChange){
				if(intent.getBooleanExtra("barvisible",true)){
					statusBarVisible = true;
				}else {
					statusBarVisible = false;
				}
			}
			setTvViewPosition(viewX,viewY,viewWidth,viewHeight);
		}
	};*/

	public TvView(Context context) {
		super(context);
		mContext = context;
		windowManager = (WindowManager) context.getApplicationContext()
				.getSystemService(context.WINDOW_SERVICE);
		mScreenWidth = ScreenConstant.SCREEN_WIDTH;
		mScreenHeight = ScreenConstant.SCREEN_HEIGHT;
		wmParams = new LayoutParams();
		wmParams.type = LayoutParams.TYPE_PHONE;
		wmParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		//wmParams.format = android.graphics.PixelFormat.PIXEL_VIDEO_HOLE;
		windowManager.addView(this, wmParams);
		setBackgroundResource(R.drawable.translucent_background);
		
	}

	public void setTvViewPosition(float x, float y, float width, float height) {
		viewX = x;
		viewY = y;
		viewWidth = width;
		viewHeight = height;
		wmParams.width = (int) (width * mScreenWidth);
		wmParams.height = (int) (height * mScreenHeight);
		wmParams.x = (int) (x * mScreenWidth);
		//if (mScreenWidth == 1280 && mScreenHeight == 720) {
			//wmParams.y = (int) (y * mScreenHeight);
		//} else {
		if(statusBarVisible){
			wmParams.y = (int) (y * mScreenHeight) - getSystemBarheight();
		}else{
			wmParams.y = (int) (y * mScreenHeight);
		}
		//}
		com.mediatek.wwtv.tvcenter.util.MtkLog.i("OSD", "~~~~~~~~mScrrenWidth:" + mScreenWidth
				+ "~~mScreenHeight:" + mScreenHeight);
		com.mediatek.wwtv.tvcenter.util.MtkLog.i("OSD", "~~wmParams.width: " + wmParams.width
				+ "~~wmParams.height: " + wmParams.height + "~~wmParams.x: "
				+ wmParams.x + "~~wmParams.y:" + wmParams.y + "~~");
		windowManager.updateViewLayout(this, wmParams);
	}

	public void show(Context context) {
		//IntentFilter filter = new IntentFilter(PhoneBarChange);
		//mContext.registerReceiver(mBroadcastRecevier,filter);
		//}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in TvView to show tvview");
		setVisibility(View.VISIBLE);	
	}
	
	public void startCheckPosition(){
		try{
			lastScreenTypeValue = Settings.System.getInt(mContext.getContentResolver(),"fullScreenOrNot");
		} catch(SettingNotFoundException e){
			e.printStackTrace();
		}
		mHandler.post(mRunnable);
	}

	public void hide() {
		/*try{
			mContext.unregisterReceiver(mBroadcastRecevier);
		}
		catch(Exception e){
		
		}*/
		setVisibility(View.INVISIBLE);
		mHandler.removeCallbacks(mRunnable);
	}
	
	public int getSystemBarheight() {
		Class<?> c = null;

		Object obj = null;

		Field field = null;

		int x = 0;
		int sbar = 0;

		try {

			c = Class.forName("com.android.internal.R$dimen");

			obj = c.newInstance();

			field = c.getField("status_bar_height");

			x = Integer.parseInt(field.get(obj).toString());

			sbar = getResources().getDimensionPixelSize(x);
			return sbar;

		} catch (Exception e1) {

			com.mediatek.wwtv.tvcenter.util.MtkLog.e("tag", "get status bar height fail");

			e1.printStackTrace();
			return 0;

		}

	}

}
