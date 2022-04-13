package com.mediatek.wwtv.tvcenter.nav.view;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import com.mediatek.twoworlds.tv.MtkTvGingaBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyMap;


public class CommonMsgDialog extends NavBasicDialog {

	private static String TAG = "CommonMsgDialog";

	private TextView mDialogContentTextView;


	private static final int WARNING_MSG_EWS = 0;
	private static final int WARNING_MSG_SPECIAL = 1;
	private static final int WARNING_MSG_BOOK_REMINDER = 2;
	public static final int WARNING_MSG_STOP_GINGA_START_CC = 3;
	public static final int WARNING_MSG_STOP_CC_START_GINGA = 4;
	private static final int WARNING_MSG_DIALOG_HIDE = 255;

	private static final int TIME_OUT = 5000;

	private int mChannelID;

	private int mMsgType;

	private boolean isAutoDismiss = true;

	private String bookReminderString;

	private final CommonIntegration mCommonIntegration;


	public void commonMsgHanndler(int msgType, int channelId, int timeOut, String eventTitle){
		mMsgType = msgType;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"mMsgType:"+mMsgType);
		switch (msgType) {
		case WARNING_MSG_EWS:
			mChannelID = channelId;
			if (!isShowing()) {
				TurnkeyUiMainActivity.resumeTurnkeyActivity(mContext);
				show();
			}
			mDialogContentTextView
					.setText(R.string.common_dialog_ews_service_content);
			break;
		case WARNING_MSG_SPECIAL:
			mChannelID = channelId;
			if (!isShowing()) {
				TurnkeyUiMainActivity.resumeTurnkeyActivity(mContext);
				show();

			}
			mDialogContentTextView
					.setText(R.string.common_dialog_special_service_content);
			break;
		case WARNING_MSG_BOOK_REMINDER:
			mChannelID = channelId;
			bookReminderString = eventTitle + " " + bookReminderString;
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
					"come in CommonMsgDialog mDialogMsgHandler,get eventName == "
							+ eventTitle);
			if (!isShowing()) {
				show();
			}
			mDialogContentTextView.setText(bookReminderString);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
					"come in CommonMsgDialog mDialogMsgHandler,get delay time == "
							+ timeOut);
			startTimeout(timeOut);
			break;
		case WARNING_MSG_STOP_CC_START_GINGA:
                if (MenuConfigManager.getInstance(mContext)
                        .getDefault(MenuConfigManager.SETUP_ENABLE_CAPTION) == 0) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in WARNING_MSG_STOP_CC_START_GINGA");
                    mChannelID = channelId;
                    isAutoDismiss = true;
                    if (!isShowing()) {
                        TurnkeyUiMainActivity.resumeTurnkeyActivity(mContext);
                        show();
                    }
                    mDialogContentTextView
                            .setText(R.string.nav_ginga_tv_for_tips);
                    startTimeout(TIME_OUT);

		    }

			break;
		case WARNING_MSG_STOP_GINGA_START_CC:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in WARNING_MSG_STOP_GINGA_START_CC");
			mChannelID = channelId;
			isAutoDismiss = true;
			if (!isShowing()) {
				TurnkeyUiMainActivity.resumeTurnkeyActivity(mContext);
				show();
			}
			mDialogContentTextView
				.setText(R.string.common_dialog_start_cc_stop_ginga);
			startTimeout(TIME_OUT);
			break;
		case WARNING_MSG_DIALOG_HIDE:
			dismiss();
			break;
		default:
			break;
		}
	}

	@Override
	public void dismiss(){
		super.dismiss();
		if (isAutoDismiss) {
			if (mMsgType == WARNING_MSG_STOP_GINGA_START_CC) {
				MtkTvGingaBase mGinga = new MtkTvGingaBase();
			    mGinga.warningStartCC(false);
			}else if (mMsgType == WARNING_MSG_STOP_CC_START_GINGA) {
				MtkTvGingaBase mGinga = new MtkTvGingaBase();
				mGinga.warningStartGingaApp(false);
			}
		}
	}

	@Override
    public boolean isCoExist(int componentID) {
		return true;
	};

	@Override
	public boolean isKeyHandler(int keyCode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyMap.KEYCODE_MENU:
		case KeyMap.KEYCODE_MTKIR_GUIDE:
			this.dismiss();
			break;
		case KeyMap.KEYCODE_BACK:
			this.dismiss();
			return true;
		case KeyMap.KEYCODE_DPAD_DOWN:
		case KeyMap.KEYCODE_DPAD_UP:
		case KeyMap.KEYCODE_VOLUME_DOWN:
		case KeyMap.KEYCODE_VOLUME_UP:
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
		case KeyMap.KEYCODE_MTKIR_MUTE:
		case KeyMap.KEYCODE_MTKIR_INFO:
			return true;
		case KeyMap.KEYCODE_DPAD_LEFT:
		case KeyMap.KEYCODE_DPAD_RIGHT:
			if (mMsgType == WARNING_MSG_STOP_CC_START_GINGA || mMsgType == WARNING_MSG_STOP_GINGA_START_CC) {
				startTimeout(TIME_OUT);
			}
			return true;
		default:
			break;
		}

		/* Back Key to TurnkeyUiMainActivity */
		if (TurnkeyUiMainActivity.getInstance() != null) {
			return TurnkeyUiMainActivity.getInstance().onKeyHandler(keyCode,
					event);
		}

		return false;
	}

	public CommonMsgDialog(Context context) {
		this(context, R.style.nav_dialog);
	}

	public CommonMsgDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		componentID = NAV_COMP_ID_DIALOG_MSG;
		mCommonIntegration = CommonIntegration.getInstance();
		bookReminderString = context.getResources().getString(
				R.string.commmon_dialog_book_reminder_content);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
    setContentView(R.layout.nav_simple_dialog);
		initDialogView();
    setPositon();
	}

	private void initDialogView() {
	  TextView dialogType = (TextView) findViewById(R.id.simple_dialog_tittle);
    dialogType.setText(R.string.simple_dialog_title_warning);
		mDialogContentTextView = (TextView) findViewById(R.id.simple_dialog_content);
		Button mDialogYesButton = (Button) findViewById(R.id.simple_dialog_confirm);
    if(mDialogYesButton.getVisibility() != View.VISIBLE){
      mDialogYesButton.setVisibility(View.VISIBLE);
    }
		mDialogYesButton.setText(R.string.common_dialog_msg_yes);
		mDialogYesButton.setOnClickListener(buttonClickListener);
		Button mDialogNoButton = (Button) findViewById(R.id.simple_dialog_cancel);
    if(mDialogNoButton.getVisibility() != View.VISIBLE){
      mDialogNoButton.setVisibility(View.VISIBLE);
    }
		mDialogNoButton.setText(R.string.common_dialog_msg_no);
		mDialogNoButton.setFocusable(true);
		mDialogNoButton.requestFocus();
		mDialogNoButton.setOnClickListener(buttonClickListener);
	}

	
    public void setPositon() {
        int windowWid = 0;
        int windowHei = 0;
        WindowManager windowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        if(windowManager != null){
            DisplayMetrics dm = new DisplayMetrics();
            windowManager.getDefaultDisplay().getRealMetrics(dm);
            windowHei = dm.heightPixels;
            windowWid = dm.widthPixels;
        }
        Window window = this.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "position =="+lp.x+","+lp.y+","+windowWid + ",windowHei:"+windowHei);
        window.setAttributes(lp);
    }
  
	View.OnClickListener buttonClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.simple_dialog_confirm:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "yes mMsgType =="+mMsgType);
				if (mMsgType == WARNING_MSG_STOP_CC_START_GINGA) {
					isAutoDismiss = false;
					MtkTvGingaBase mGinga = new MtkTvGingaBase();
					mGinga.warningStartGingaApp(true);
					InfoBarDialog infoBarDialog = (InfoBarDialog) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_INFO_BAR);
					if (infoBarDialog != null) {
						infoBarDialog.show(InfoBarDialog.INFO_BAR,
							mContext.getString(R.string.nav_ginga_tv_for_infobar));
					}
				} else if (mMsgType == WARNING_MSG_STOP_GINGA_START_CC) {
					isAutoDismiss = false;
					MtkTvGingaBase mGinga = new MtkTvGingaBase();
				    mGinga.warningStartCC(true);
				} else {
					if(StateDvr.getInstance() != null && StateDvr.getInstance().isRecording()){
                    	DvrManager.getInstance().stopDvr();
                        try {
                        		Thread.sleep(2000);
                            } catch (Exception e) {
                            	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sleep exception");
                            }
                    }
					mCommonIntegration.selectChannelById(mChannelID);
				}
				dismiss();
				break;
			case R.id.simple_dialog_cancel:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "no mMsgType =="+mMsgType);
				if (mMsgType == WARNING_MSG_STOP_CC_START_GINGA) {
					isAutoDismiss = false;
					MtkTvGingaBase mGinga = new MtkTvGingaBase();
					mGinga.warningStartGingaApp(false);
				} else if (mMsgType == WARNING_MSG_STOP_GINGA_START_CC) {
					isAutoDismiss = false;
					MtkTvGingaBase mGinga = new MtkTvGingaBase();
				    mGinga.warningStartCC(false);
				}
				dismiss();
				break;
				default:
				    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "default");
				    break;
			}
		}
	};
}
