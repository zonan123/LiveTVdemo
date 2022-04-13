package com.mediatek.wwtv.tvcenter.epg;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
//import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.wwtv.tvcenter.epg.cn.EPGCnActivity;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGEuActivity;
import com.mediatek.wwtv.tvcenter.epg.sa.EPGSaActivity;
import com.mediatek.wwtv.tvcenter.epg.us.EPGUsActivity;

import com.mediatek.wwtv.tvcenter.util.SaveValue;

import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

public class EPGPwdDialog extends Dialog {

	private static final String TAG = "EPGPwdDialog";

	private Context mContext;

	private static final int MSG_PWD_ERROR_DISMISS = 0;
	private static final int MSG_CHECK_DELAY = 100;
	public static final int PASSWORD_VIEW_PWD_INPUT = 1;
	public static final int PASSWORD_VIEW_PWD_ERROR = 2;
	public static final int PASSWORD_DISMISS_DELAY = 3;
	public static final int CHECK_PASSWORD = 4;
	public int mode = PASSWORD_VIEW_PWD_INPUT;
    private String password ="";
    private String showPasswordStr="";
    private View mAttachView;
	private LinearLayout pwdView;
	private TextView pwdError;
	private TextView pwdValue;
//	private MtkTvChannelInfoBase mCurrentLockedChannel;
//	private EPGProgramInfo mEPGProgramInfo;
	private MtkTvAppTVBase tvAppTvBase;
	private boolean misLock;
//	private int marginX;
	private int marginY;
	private int menuWidth = 390;
	private int menuHeight = 220;
//	public void setCurrChannel(MtkTvChannelInfoBase currCh) {
//		mCurrentLockedChannel = currCh;
//	}
	
	public void setAttachView(View attachView) {
		mAttachView=attachView;
	}
	
	public void initLayoutParams(){
		Window window = getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.width = (int)(menuWidth*ScreenConstant.SCREEN_WIDTH/1280.0f);
		lp.height =menuHeight;
		lp.y=marginY;
		window.setAttributes(lp);
		com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "lp.height="+lp.height+",lp.y="+lp.y);
	}
	
	
	private void mesureAttachViewLocation(){
		int [] location=new int[2];
		mAttachView.getLocationInWindow(location);
		com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "x="+location[0]+",y="+location[1]);
		menuHeight= (int)(menuHeight*ScreenConstant.SCREEN_HEIGHT/720.0f);
		com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "menuHeight="+menuHeight);
		marginY = location[1]-(ScreenConstant.SCREEN_HEIGHT-menuHeight)/2;
		com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "marginY="+marginY);
	}
	
	
  public void show(){
    if(marginY==0){
      mesureAttachViewLocation();
    }
		initLayoutParams();
		super.show();
	}

//	public void setEPGprogram(EPGProgramInfo ePGProgramInfo) {
//		mEPGProgramInfo = ePGProgramInfo;
//	}

	public EPGPwdDialog(Context context, int theme) {
		super(context, theme);
		setContentView(R.layout.epg_pwd_view);
		findViews();
	}

	public EPGPwdDialog(Context context) {
		this(context, R.style.nav_dialog);
		com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "Constructor!!");
		mContext = context;
		password ="";
		showPasswordStr="";
		tvAppTvBase = new MtkTvAppTVBase();
	}

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MSG_PWD_ERROR_DISMISS: {
				mHandler.removeMessages(MSG_PWD_ERROR_DISMISS);
				if (pwdError.getVisibility() != View.GONE) {
					pwdError.setVisibility(View.GONE);
				}
				break;
			}
			case PASSWORD_DISMISS_DELAY:
				dismiss();
				break;
			case CHECK_PASSWORD:
				mHandler.removeMessages(CHECK_PASSWORD);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkPassWord>>>" + password);
				checkPassWord(password);
				break;
			default:
				break;
			}

		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate");
	}

	@Override
	protected void onStart() {
	    super.onStart();
		password ="";
		showPasswordStr="";
		showPasswordView(PASSWORD_VIEW_PWD_INPUT);
		if (pwdError.getVisibility() != View.GONE) {
			pwdError.setVisibility(View.GONE);
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown>>>" + keyCode);
		switch (event.getKeyCode()) {
		case KeyMap.KEYCODE_MENU:
		case KeyMap.KEYCODE_MTKIR_YELLOW:
		case KeyMap.KEYCODE_MTKIR_BLUE:
		case KeyMap.KEYCODE_MTKIR_GREEN:
		case KeyMap.KEYCODE_MTKIR_RED:
			return true;
		case KeyMap.KEYCODE_BACK:
			dismiss();
			break;
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
			inputChar(keyCode - 7);
			return true;

		default:
			break;
		}

//		if (null != mContext && mContext instanceof EPGActivity) {
//			((EPGActivity) mContext).onKeyDown(keyCode, event);
//		}

		if (keyCode == KeyMap.KEYCODE_VOLUME_UP
				|| keyCode == KeyMap.KEYCODE_VOLUME_DOWN) {
			return true;
		}

		return super.onKeyDown(keyCode, event);

	};



	private void findViews() {
		pwdError = (TextView) findViewById(R.id.epg_pwd_error);
		pwdView = (LinearLayout) findViewById(R.id.epg_input_pwd_view);
		pwdValue = (TextView) findViewById(R.id.epg_pwd_value);
		pwdValue.setInputType(EditorInfo.TYPE_NULL);
		pwdValue.addTextChangedListener(passwordInputTextWatcher);
	}

	

	public void dismiss() {
		mHandler.removeMessages(PASSWORD_DISMISS_DELAY);
		mHandler.removeMessages(MSG_PWD_ERROR_DISMISS);
		mHandler.removeCallbacksAndMessages(null);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "misLock>>>" + misLock);
		if (mContext instanceof EPGEuActivity) {
			((EPGEuActivity)mContext).setIsNeedFirstShowLock(misLock);
		} else if (mContext instanceof EPGSaActivity) {
			((EPGSaActivity)mContext).setIsNeedFirstShowLock(misLock);
                } else if (mContext instanceof EPGCnActivity) {
                        ((EPGCnActivity)mContext).setIsNeedFirstShowLock(misLock);
		}
		try{
		    super.dismiss();
		}catch(Exception e){
		    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "error:"+e.getMessage());
		}

	}

	TextWatcher passwordInputTextWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "s.toString()>>>" + s.toString() + "   " + s.toString().length());
			if (s.toString().length() == 4) {
				mHandler.sendEmptyMessageDelayed(CHECK_PASSWORD, MSG_CHECK_DELAY);
			}
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "beforeTextChanged s.toString()>>>" + s.toString());
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTextChanged s.toString()>>>" + s.toString());
		}
	};

	public void checkPassWord(String pwd) {
		if (MtkTvPWDDialog.getInstance().checkPWD(pwd)) {
//			if (mCurrentLockedChannel != null) {
//			EPGManager.getInstance().openLockedSourceAndChannel(mCurrentLockedChannel);
//		} else if (mEPGProgramInfo != null) {
//			mEPGProgramInfo.setProgramBlock(false);
//		}
			tvAppTvBase.unlockService(CommonIntegration.getInstance().getCurrentFocus());
			if (mContext instanceof EPGUsActivity) {
				((EPGUsActivity)mContext).setProgramBlock(false);
			}
			misLock = false;
			mHandler.sendEmptyMessageDelayed(PASSWORD_DISMISS_DELAY, 400);
//			CheckLockSignalChannelState.getInstance(mContext)
//			.checkLockedSignStateOrHasChannel(true);

		} else {
			showPasswordView(PASSWORD_VIEW_PWD_ERROR);
			mHandler.sendEmptyMessageDelayed(MSG_PWD_ERROR_DISMISS, 2000);
		}
	}

	public void showPasswordView(int mode) {
		pwdValue.setText(null);
		password ="";
		showPasswordStr="";
		switch (mode) {
		case PASSWORD_VIEW_PWD_INPUT:
			if (pwdView.getVisibility() != View.VISIBLE) {
				pwdView.setVisibility(View.VISIBLE);
			}
			this.mode = PASSWORD_VIEW_PWD_INPUT;
			break;

		case PASSWORD_VIEW_PWD_ERROR:
		    Log.d(TAG,"password error");
			if (pwdView.getVisibility() != View.VISIBLE) {
				pwdView.setVisibility(View.VISIBLE);
			}
			if (pwdError.getVisibility() != View.VISIBLE) {
				pwdError.setVisibility(View.VISIBLE);
//				Toast.makeText(mContext, R.string.nav_parent_wrong_psw, Toast.LENGTH_SHORT).show();
			}
			this.mode = PASSWORD_VIEW_PWD_ERROR;
			break;
    default:
      break;
  }
	}

	public void inputChar(int num) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "password>>>" + password);
		if (password != null && password.length() < 4) {
			mHandler.removeMessages(MSG_PWD_ERROR_DISMISS);
			mHandler.sendEmptyMessage(MSG_PWD_ERROR_DISMISS);
			sendAutoDismissMessage();
			password=password +num;
			showPasswordStr=showPasswordStr+"*";
			pwdValue.setText(showPasswordStr);
		}
	}

	public String getInputString() {
		if (password != null) {
			return password;
		}
		return null;
	}

   public boolean isPasswordRight(Context context, String mString) {
        String mPassword;

        mPassword = SaveValue.getInstance(context).readStrValue("password", "1234");
        return mPassword.equals(mString);
    }

   public void sendAutoDismissMessage() {
	   misLock = true;
	   mHandler.removeMessages(PASSWORD_DISMISS_DELAY);
	   mHandler.sendEmptyMessageDelayed(PASSWORD_DISMISS_DELAY, 10*1000);
	}
}
