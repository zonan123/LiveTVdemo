package com.mediatek.wwtv.tvcenter.nav.view.ciview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.PinDialogFragment.ResultListener;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.twoworlds.tv.MtkTvCI;

public final class CIPinCodeDialog extends Dialog {

	private static final String TAG = "CIPinCodeDialog";
	private Context mContext;
	
	private PinDialogFragment pinDialogFragment;
	private CIStateChangedCallBack mCIState;
	String realPwd = "";
	String showPwd;
	static final int PIN_CODE_LEN = 4;
	private static CIPinCodeDialog mDialog;
	LinearLayout dialogLayout;
	private boolean isKeyShowDialog = false;

	private CIPinCodeDialog(Context context) {
		super(context, R.style.Theme_TurnkeyCommDialog);
		mContext = context;
		//mDialog = this;
	}

	public synchronized static CIPinCodeDialog getInstance(Context context) {
		if (mDialog == null) {
			mDialog = new CIPinCodeDialog(context);
		}
		return mDialog;
	}

	public void setCIStateChangedCallBack(CIStateChangedCallBack state) {
		mCIState = state;
		mCIState.setPinCodeDialog(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate");
		setContentView(getLayoutInflater().inflate(
				R.layout.menu_ci_pin_code_dialog, null));
		setWindowPosition();
		dialogLayout = (LinearLayout) findViewById(R.id.pin_code_dialog);
		TextView mTitle = (TextView) findViewById(R.id.ci_input_pin_code_title);
		pinDialogFragment = (PinDialogFragment) (((TurnkeyUiMainActivity) mContext)
				.getFragmentManager()
				.findFragmentById(R.id.ci_input_pin_code_num));
		pinDialogFragment.setResultListener(new ResultListener() {

			@Override
			public void done(String pinCode) {
				MtkTvCI ci = mCIState.getCIHandle();
				if (ci != null) {
					ci.setCamPinCode(pinCode);
					dismiss();
				}
			}
		});
		mTitle.setText(R.string.menu_setup_ci_pin_code_input_tip);
	}

	@Override
	public void show() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "show");
		isKeyShowDialog = true;
		super.show();
		pinDialogFragment.setPinCodeLength(4);
		pinDialogFragment.requestPickerFocus();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchKeyEvent event.getKeyCode() "+event.getKeyCode());
	      if (event.getAction() == KeyEvent.ACTION_DOWN) {
	          switch (event.getKeyCode()) {
	            case KeyMap.KEYCODE_MTKIR_CHUP:
	            case KeyMap.KEYCODE_MTKIR_CHDN:
	                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TurnkeyUiMainActivity");	            
	                dismiss();
                    break;
                default:
                    break;
	          }
	        }
		if (isKeyShowDialog) {
			isKeyShowDialog = false;
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	private void setWindowPosition() {	
		WindowManager m = getWindow().getWindowManager();
	    Display display = m.getDefaultDisplay();
	    Window window = getWindow();
	    WindowManager.LayoutParams lp = window.getAttributes();
	    TypedValue sca = new TypedValue();
	    mContext.getResources().getValue(R.dimen.nav_ci_window_size_width,sca ,true);
	    float w  = sca.getFloat();
	    mContext.getResources().getValue(R.dimen.nav_ci_window_size_height,sca ,true);
	    float h  = sca.getFloat();
	    
	    int menuWidth = (int) (display.getWidth() * w);
	    int menuHeight = (int) (display.getHeight() * h);
	    lp.width = menuWidth;
	    lp.height = menuHeight;
	    lp.gravity = Gravity.CENTER;
	    window.setAttributes(lp);
		
	}
}
