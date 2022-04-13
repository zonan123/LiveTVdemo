package com.mediatek.wwtv.tvcenter.nav.view;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener.ICStatusListener;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.InstrumentationHandler;
import com.mediatek.wwtv.tvcenter.R;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class OneKeyMenuDialog extends NavBasicDialog implements
		ICStatusListener {
	private static final String TAG = "OneKeyMenuDialog";
	private Context mContext;
	private List<OneKeyAction> mList = null;
	private RecyclerView mRecyclerView;
	private OneKeyListAdapter mAdapter;
	//private OneKeyAction powerOff, channelNext, channelLast, volumeIncrease,
	//		volumeDecrease, sourceControl;
	
	private static final int POSITION_POWER_OFF    = 0;
	private static final int POSITION_CHANNEL_NEXT = POSITION_POWER_OFF + 1;
	private static final int POSITION_CHANNEL_LAST = POSITION_POWER_OFF + 2;
	private static final int POSITION_VOLUME_INC   = POSITION_POWER_OFF + 3;
	private static final int POSITION_VOLUME_DEC   = POSITION_POWER_OFF + 4;
	private static final int POSITION_SOURCE       = POSITION_POWER_OFF + 5;
	
	
	private static final int AUTO_DISMISS = 0x01;
	private static final int DURATION_TIME = 5 * 1000;
	private MyHandler mHandler;
	private int currentPosition = 0;
	
	private static class MyHandler extends Handler {
		WeakReference<OneKeyMenuDialog> mDialogReference;

		public MyHandler(OneKeyMenuDialog dialog) {
			mDialogReference = new WeakReference<OneKeyMenuDialog>(dialog);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			final OneKeyMenuDialog mDialog = mDialogReference.get();
			Log.d(TAG, "handleMessage||what =" + msg.what);
			switch (msg.what) {
			case AUTO_DISMISS:
				if (mDialog != null && mDialog.isShowing()) {
					mDialog.dismiss();
				}
				break;
			default:
				break;
			}
		}
	};

	private OneKeyMenuDialog(Context context, int theme) {
		super(context, theme);
		mContext = context;
		this.componentID = NavBasic.NAV_COMP_ID_ONE_KEY_DIALOG;
		mHandler = new MyHandler(this);
	}
	
	public OneKeyMenuDialog(Context context) {
		this(context, R.style.nav_dialog);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nav_one_key_menu_dialog);
		setCancelable(false);
		setCanceledOnTouchOutside(false);
		setWindowPosition();
		init();
		
		LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
		layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		mRecyclerView.setLayoutManager(layoutManager);

		Log.d(TAG, "onCreate||mList.size() =" + mList.size());
		mAdapter = new OneKeyListAdapter(mContext, mList);
		mRecyclerView.setAdapter(mAdapter);
		currentPosition = 0;
	}

	public void init() {
		Log.d(TAG, "init||addAction");
		mRecyclerView = (RecyclerView) findViewById(R.id.one_key_action_list);
		
		mList = new ArrayList<OneKeyMenuDialog.OneKeyAction>();
		
		OneKeyAction powerOff = new OneKeyAction(
				mContext.getString(R.string.one_key_powered_off), mContext
						.getResources().getDrawable(R.drawable.one_key_power_off));
		OneKeyAction channelNext = new OneKeyAction(
				mContext.getString(R.string.one_key_ch_next), mContext
						.getResources().getDrawable(R.drawable.one_key_ch_next));
		OneKeyAction channelLast = new OneKeyAction(
				mContext.getString(R.string.one_key_ch_last), mContext
						.getResources().getDrawable(R.drawable.one_key_ch_last));
		OneKeyAction volumeIncrease = new OneKeyAction(
				mContext.getString(R.string.one_key_volume_increase), mContext
						.getResources().getDrawable(R.drawable.one_key_vol_increase));
		OneKeyAction volumeDecrease = new OneKeyAction(
				mContext.getString(R.string.one_key_volume_decrease), mContext
						.getResources().getDrawable(R.drawable.one_key_vol_decrease));
		OneKeyAction sourceControl = new OneKeyAction(
				mContext.getString(R.string.one_key_source_control), mContext
						.getResources().getDrawable(R.drawable.one_key_source));

		mList.add(powerOff);
		mList.add(channelNext);
		mList.add(channelLast);
		mList.add(volumeIncrease);
		mList.add(volumeDecrease);
		mList.add(sourceControl);
	}

	/*
	 * called to set dialog position to avoid overlapping with other component,
	 * such as twinkle,banner,etc
	 */
	private void setWindowPosition() {
		Window window = getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.y -= 80;
		window.setAttributes(lp);
	}

	@Override
	public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
		return onKeyHandler(keyCode, event);
	}

	@Override
	public boolean onKeyHandler(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyHandler||code =" + keyCode);
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			dismiss();
			return true;
			// Start over when reaching the last item
		case KeyMap.KEYCODE_DPAD_RIGHT:
			resetHandler();
			currentPosition ++;
			Log.d(TAG, "DpadRight||currentPosition =" + currentPosition);
			if (currentPosition == mList.size()) {
				Log.d(TAG, "DpadRight||resetAdapterAndFocus");
				mRecyclerView.clearFocus();
				mRecyclerView.setAdapter(mAdapter);
				currentPosition = 0;
				return true;
			}
			break;
		case KeyMap.KEYCODE_MTKIR_CHUP:
		case KeyMap.KEYCODE_MTKIR_CHDN:
			if (TurnkeyUiMainActivity.getInstance() != null) {
	            Log.d(TAG, "return key to main");
	            return TurnkeyUiMainActivity.getInstance().onKeyHandler(keyCode, event);
	        }
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void show() {
		ComponentsManager.getInstance().hideAllComponents();
		super.show();
		resetHandler();
	}

	@Override
	public void updateComponentStatus(int statusID, int value) {
		// TODO Auto-generated method stub

	}
	@Override
	public boolean isShowing() {
		Log.d(TAG, "isShowing");
		return super.isShowing();
	}
	
	public void performClick() {
		Log.d(TAG, "performClick||currentPosition =" + currentPosition);
		resetHandler();
		switch (currentPosition) {
		case POSITION_POWER_OFF:
			if (isScreenOn(mContext)) {
				InstrumentationHandler.getInstance().sendKeyDownUpSync(KeyEvent.KEYCODE_POWER);
			}
			break;
		case POSITION_CHANNEL_NEXT:
                InstrumentationHandler.getInstance().sendKeyDownUpSync(KeyMap.KEYCODE_MTKIR_CHUP);
                resetHandler();
			break;
		case POSITION_CHANNEL_LAST:
			InstrumentationHandler.getInstance().sendKeyDownUpSync(KeyMap.KEYCODE_MTKIR_CHDN);
                resetHandler();
			break;
		case POSITION_VOLUME_INC:
			InstrumentationHandler.getInstance().sendKeyDownUpSync(KeyMap.KEYCODE_VOLUME_UP);
                resetHandler();
			break;
		case POSITION_VOLUME_DEC:
			InstrumentationHandler.getInstance().sendKeyDownUpSync(KeyMap.KEYCODE_VOLUME_DOWN);
                resetHandler();
			break;
		case POSITION_SOURCE:
			startInputsPanel(mContext);
			break;

		default:
			break;
		}
	}
	
	private boolean isScreenOn(Context context) {
		DisplayManager dm = (DisplayManager) context
				.getSystemService(Context.DISPLAY_SERVICE);
		for (Display displayManager : dm.getDisplays()) {
			if (displayManager.getState() != Display.STATE_OFF) {
				Log.d("TAG", "poweredOff");
				return true;
			}
		}
		return false;
	}
	
	private void startInputsPanel(Context context) {
		Intent intent = new Intent("com.android.tv.action.VIEW_INPUTS");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setPackage("com.mediatek.wwtv.tvcenter");
    intent.putExtra("extra_is_turnkey_top_running", DestroyApp.isCurActivityTkuiMainActivity());
		context.getApplicationContext().startActivity(intent);
	}
	
	/*
	 * called to clear earlier message and send a new one
	 */
	public void resetHandler() {
		if (mHandler.hasMessages(AUTO_DISMISS)) {
			mHandler.removeMessages(AUTO_DISMISS);
		}
		mHandler.sendEmptyMessageDelayed(AUTO_DISMISS,DURATION_TIME);
	}
	public class OneKeyAction {
		private String mKeyName;
		private Drawable mKeyIcon;

		public OneKeyAction(String keyName, Drawable keyIcon) {
			mKeyName = keyName;
			mKeyIcon = keyIcon;
		}

		public String getOneKeyName() {
			return mKeyName;
		}

		public Drawable getOneKeyIcon() {
			return mKeyIcon;
		}
	}

}
