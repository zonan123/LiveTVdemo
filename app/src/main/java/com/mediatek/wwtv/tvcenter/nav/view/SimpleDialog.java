package com.mediatek.wwtv.tvcenter.nav.view;

import java.lang.ref.WeakReference;

import com.mediatek.wwtv.tvcenter.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;


public class SimpleDialog extends NavBasicDialog implements
		ComponentStatusListener.ICStatusListener {
	private static final String TAG = "SimpleDialog";
	/**
	 * add your own id to differ from others
	 * 
	 */
	public static final int DIALOG_ID_PVR = 0x01;
	public static final int DIALOG_ID_SAVE_FINETUNE = DIALOG_ID_PVR + 1;

	public static final int DIALOG_LEVEL_INFO    = 0;
	public static final int DIALOG_LEVEL_WARNING = DIALOG_LEVEL_INFO + 1;
	public static final int DIALOG_LEVEL_ERROR   = DIALOG_LEVEL_INFO + 2;

	public enum ButtonType{
		Confirm,
		Cancel
	}



	private final static int BACKGROUND_IMAGES[] = {
			R.drawable.simple_dialog_info_icon,
			R.drawable.simple_dialog_warning_icon,
			R.drawable.simple_dialog_error_icon };

	private final static int DIALOG_TITLE[] = {
			R.string.simple_dialog_title_info,
			R.string.simple_dialog_title_warning,
			R.string.simple_dialog_title_error };

	private int level = 0;
	private String title;
	private String content;
	private String confirmText;
	private String cancelText;
	private Context mContext;
	private ImageView levelIcon;

	private TextView titleView;
	private TextView contentView;
	private Button confirmButton;
	private Button cancelButton;
	private LinearLayout btLayout;
	private Bundle bundle;
	private ButtonType defaultButton = ButtonType.Cancel;
	private OnConfirmClickListener mConfirmListener;
	private OnCancelClickListener mCancelListener;
    private OnConfirmResultClickListener mConfirmResultListener;
    private OnTimeoutListener onTimeoutListener;

	private int confirmClickId;
    private int confirmClickResultId;
	private int cancelClickId;
	private int timeoutId;

	private ColorStateList focusedTextColor;
	private ColorStateList normalTextColor;

	private MyHandler mHandler;
	private long delayedtime = 0;
	private static final int AUTO_DISMISS = 0x01;
	private boolean mCancelable = true;

	private static class MyHandler extends Handler {
		WeakReference<SimpleDialog> mDialogReference;

		public MyHandler(SimpleDialog dialog) {
			mDialogReference = new WeakReference<SimpleDialog>(dialog);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			final SimpleDialog mDialog = mDialogReference.get();
			Log.d(TAG, "handleMessage||what =" + msg.what);
			switch (msg.what) {
			case AUTO_DISMISS:
				if (mDialog != null && mDialog.isShowing()) {
					mDialog.dismiss();
					if (mDialog.onTimeoutListener != null) {
						mDialog.onTimeoutListener.onTimeout(mDialog.timeoutId);
					}
				}
				break;
			default:
				break;
			}
		}
	};

	private SimpleDialog(Context context, int theme) {
		super(context, theme);
		mContext = context;
		mHandler = new MyHandler(this);
		this.componentID = NavBasic.NAV_COMP_ID_SIMPLE_DIALOG;
		ComponentStatusListener.getInstance().addListener(
				ComponentStatusListener.NAV_COMPONENT_HIDE, this);
		ComponentStatusListener.getInstance().addListener(
				ComponentStatusListener.NAV_COMPONENT_SHOW, this);
	}

	public SimpleDialog(Context context) {
		this(context, R.style.nav_dialog);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nav_simple_dialog);
//		setCancelable(false);
		setCanceledOnTouchOutside(false);
		init();
	}


	@Override
	public void setCancelable(boolean flag) {
		super.setCancelable(flag);
		mCancelable = flag;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.d(TAG, "onBackPressed" + mCancelable);
		if (mCancelable) {
			dismiss();
		}
		if(onBackListener != null) {
			onBackListener.onBack(backDialogId);
			onBackListener = null;
		}
	}

	private void init() {

		focusedTextColor = mContext.getResources().getColorStateList(
				R.color.white);
		normalTextColor = mContext.getResources().getColorStateList(
				R.color.simple_text_normal);

		levelIcon = (ImageView) findViewById(R.id.simple_dialog_icon);
		titleView = (TextView) findViewById(R.id.simple_dialog_tittle);
		contentView = (TextView) findViewById(R.id.simple_dialog_content);
		confirmButton = (Button) findViewById(R.id.simple_dialog_confirm);
		cancelButton = (Button) findViewById(R.id.simple_dialog_cancel);

		btLayout = (LinearLayout) findViewById(R.id.simple_bt_layout);
		refreshUI();
	}

	private void refreshUI() {
		confirmButton.setFocusable(true);
		cancelButton.setFocusable(true);
		if (defaultButton == ButtonType.Cancel) {
			cancelButton.requestFocus();
		} else {
			confirmButton.requestFocus();
		}
		Log.d(TAG, "refreshUI||level =" + level);
		levelIcon.setBackgroundResource(BACKGROUND_IMAGES[level]);

		titleView.setText(DIALOG_TITLE[level]);

		if (!TextUtils.isEmpty(title)) {
			titleView.setText(title);
		}

		if (!TextUtils.isEmpty(content)) {
			contentView.setText(content);
		}

		if (!TextUtils.isEmpty(confirmText)) {
			confirmButton.setVisibility(View.VISIBLE);
			confirmButton.setText(confirmText);
		} else {
			confirmButton.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(cancelText)) {
			cancelButton.setVisibility(View.VISIBLE);
			cancelButton.setText(cancelText);
		} else {
			cancelButton.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(cancelText) && TextUtils.isEmpty(confirmText)) {
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btLayout
					.getLayoutParams();
			Log.d(TAG, "isEmpty:true");
			layoutParams.setMargins(0, layoutParams.topMargin,
					layoutParams.rightMargin, 10);
			btLayout.setLayoutParams(layoutParams);
		} else {
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btLayout
					.getLayoutParams();
			Log.d(TAG, "isEmpty:flase");
			layoutParams.setMargins(0, layoutParams.topMargin,
					layoutParams.rightMargin, 28);
			btLayout.setLayoutParams(layoutParams);
		}

		confirmButton.setOnClickListener(new MyClickListner());
		cancelButton.setOnClickListener(new MyClickListner());

		confirmButton.setOnFocusChangeListener(new MyFocusListner());
		cancelButton.setOnFocusChangeListener(new MyFocusListner());
	}

	public void setLevel(int level) {
		if (level < 0 || level > BACKGROUND_IMAGES.length) {
			this.level = 0;
		} else {
			this.level = level;
		}
	}

	public void setContent(int resId) {
		this.content = mContext.getString(resId);
	}

	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * called to set text on confirm button; Note: If neither setConfirmText()
	 * nor setCancelText is called,then no buttons will be shown
	 * 
	 */
	public void setConfirmText(int resId) {
		this.confirmText = mContext.getString(resId);
	}

	private int backDialogId;
	private OnBackListener onBackListener;
	public void setOnBackListener(
			OnBackListener backListener, int dialogId) {
		onBackListener = backListener;
		backDialogId = dialogId;
	}

	public interface OnBackListener {
		void onBack(int dialogId);
	}
	public void setCancelText(int resId) {
		this.cancelText = mContext.getString(resId);
	}

	public void setScheduleDismissTime(long delayedtime) {
		this.delayedtime = delayedtime;
	}

	public void setDefaultButton(ButtonType buttonType){
		defaultButton = buttonType;
	}

	public void setBundle(Bundle bundle){
		this.bundle = bundle;
	}
	
	public Bundle getBundle(){
		return bundle != null ? bundle : null;
	}
	@Override
	public void updateComponentStatus(int statusID, int value) {
		switch (statusID) {
		case ComponentStatusListener.NAV_COMPONENT_HIDE:
			if (value == NavBasic.NAV_COMP_ID_SIMPLE_DIALOG) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "SimpleDialog is hidden!");
			}
			break;
		case ComponentStatusListener.NAV_COMPONENT_SHOW:
			if (value == NavBasic.NAV_COMP_ID_TWINKLE_DIALOG) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "twinkle showing");
			}
			break;
		default:
			break;
		}

	}

	@Override
	public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
		return onKeyHandler(keyCode, event);
	}

	@Override
	public boolean onKeyHandler(int keyCode, KeyEvent event) {
		if (delayedtime > 0L) {
			mHandler.removeMessages(AUTO_DISMISS);
			mHandler.sendEmptyMessageDelayed(AUTO_DISMISS, delayedtime);
		}
		Log.d(TAG, "onKeyHandler||code =" + keyCode + "event :" + event.getAction() + KeyEvent.ACTION_DOWN);
//		switch (keyCode) {
//		case KeyEvent.KEYCODE_BACK:
//			dismiss();
//			return true;
//		default:
//			break;
//		}
		return false;
	}

	@Override
	public void show() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
		Log.d(TAG, "show confirmClickId" + confirmClickId + "getActiveCompId :" + ComponentsManager.getActiveCompId());
		if(confirmClickId == NAV_COMP_ID_CI_DIALOG || ComponentsManager.getActiveCompId() == NAV_COMP_ID_CI_DIALOG){
			ComponentsManager.getInstance().hideAllComponents(NAV_COMP_ID_EAS, NAV_COMP_ID_EWS,NAV_COMP_ID_CI_DIALOG,NAV_COMP_ID_SIMPLE_DIALOG);
		} else {
			ComponentsManager.getInstance().hideAllComponents(NAV_COMP_ID_EAS, NAV_COMP_ID_EWS,NAV_COMP_ID_SIMPLE_DIALOG);
		}
		super.show();
		refreshUI();
		if (mHandler.hasMessages(AUTO_DISMISS)) {
			mHandler.removeMessages(AUTO_DISMISS);
		}
		Log.d(TAG, "show||delayedtime =" + delayedtime);
		if (delayedtime > 0L) {
			mHandler.removeMessages(AUTO_DISMISS);
			mHandler.sendEmptyMessageDelayed(AUTO_DISMISS, delayedtime);
		}
	}

	@Override
	public void dismiss() {
		mHandler.removeMessages(AUTO_DISMISS);
		mHandler.removeCallbacksAndMessages(null);
		super.dismiss();
		//new Exception().printStackTrace();
		if (this.isShowing()) {
			this.dismiss();
		}
		resetAll();
	}
	
	@Override
	public boolean isShowing() {
		Log.d(TAG, "isShowing");
		return super.isShowing();
	}
	
	private void resetAll() {
		Log.d(TAG, "resetAll");
		level = 0;
		content = "";
		confirmText = "";
		cancelText = "";
		delayedtime = 0L;
		bundle = null;
	}
	
	public void setOnConfirmClickListener(
			OnConfirmClickListener onConfirmClickListener, int dialogId) {
		mConfirmListener = onConfirmClickListener;
		mConfirmResultListener = null;
		confirmClickId = dialogId;
	}

	public int getConfirmClickId(){
		return confirmClickId;
	}

	public void setOnCancelClickListener(
			OnCancelClickListener onCancelClickListener, int dialogId) {
		mCancelListener = onCancelClickListener;
		cancelClickId = dialogId;
	}

	public interface OnConfirmClickListener {
		void onConfirmClick(int dialogId);
	}

	public interface OnCancelClickListener {
		void onCancelClick(int dialogId);
	}

	public interface OnTimeoutListener {
		void onTimeout(int dialogId);
	}

	public void setOnTimeoutListener(OnTimeoutListener onTimeoutListener, int timeoutId) {
		this.onTimeoutListener = onTimeoutListener;
		this.timeoutId = timeoutId;
	}

	private class MyClickListner implements View.OnClickListener {
		@Override
		public void onClick(View arg0) {
			mHandler.removeMessages(AUTO_DISMISS);
			switch (arg0.getId()) {
			case R.id.simple_dialog_confirm:
				if (mConfirmListener != null) {
					mConfirmListener.onConfirmClick(confirmClickId);
                    mConfirmListener=null;
				}
                                dismiss();
                                if (mConfirmResultListener != null) {
                                        mConfirmResultListener.onConfirmResultClick(confirmClickResultId);
                                        mConfirmResultListener=null;
                                }
				break;
			case R.id.simple_dialog_cancel:
				if (mCancelListener != null) {
					mCancelListener.onCancelClick(cancelClickId);
                    mCancelListener=null;
				}
				dismiss();
				break;
			default:
				break;
			}
            mCancelListener=null;
            mConfirmResultListener=null;
            mConfirmListener=null;
		}
	}

	private class MyFocusListner implements View.OnFocusChangeListener {

		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			switch (arg0.getId()) {
			case R.id.simple_dialog_confirm:
				confirmButton.setTextColor(arg1 ? focusedTextColor
						: normalTextColor);
				break;
			case R.id.simple_dialog_cancel:
				cancelButton.setTextColor(arg1 ? focusedTextColor
						: normalTextColor);
				break;
			default:
				break;
			}
		}
	}

	public void setOnConfirmResultClickListener(
			OnConfirmResultClickListener onConfirmResultClickListener, int dialogId) {
		mConfirmResultListener = onConfirmResultClickListener;
		mConfirmListener = null;
		confirmClickResultId = dialogId;
	}

	public interface OnConfirmResultClickListener {
		void onConfirmResultClick(int dialogId);
	}

}
