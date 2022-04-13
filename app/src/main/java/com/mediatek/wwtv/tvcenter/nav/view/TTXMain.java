package com.mediatek.wwtv.tvcenter.nav.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.util.BannerImplement;
import com.mediatek.wwtv.tvcenter.nav.util.SundryImplement;
import com.mediatek.wwtv.tvcenter.nav.util.TeletextImplement;
import com.mediatek.wwtv.tvcenter.nav.util.TeletextImplement.OnStopTTXCallback;
//import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicView;
//import com.mediatek.wwtv.tvcenter.timeshift_pvr.manager.TimeShiftManager;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyDispatch;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.tv.ini.IniDocument;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.twoworlds.tv.MtkTvScreenSaverBase;


public class TTXMain extends NavBasicMisc {
	private static final String TAG = "TTXMain";

	//private static TTXMain instance;

	private BannerImplement mNavBannerImplement = null;

	private TTXTopDialog topDialog;
	public TTXFavListDialog favDialog;
	private TeletextImplement mTTXImpl;
	public boolean isActive = false;
	public boolean isNoTTX = false;
//	private TTXToast noTopToast;

	private final static String IS_TTX_SHOW = "is_ttx_show";

	private final static int NO_SIGNAL = 1;
	public final static int BANNER_MSG_NAV = 0;

	private static final String ACTION_INI_PATH ="/vendor/etc/customer_keymap.ini";
	private static final String SECTION_NAME ="IR";
	private String mixReuse = "";
	private String holdReuse = "";
	private String indexReuse = "";
	private String revealReuse = "";
	private String subcodeReuse = "";
	private String sizeReuse = "";
	private String exitReuse = "";

	private final static int KEY_CODE_SLEEP = 305;
	private IniDocument iniDoc;
	private boolean isTTXSupported = false;
	private MtkTvScreenSaverBase mScreenSaver;

	public TTXMain(Context context) {
		super(context);
		mContext = context;
		this.componentID = NAV_COMP_ID_TELETEXT;
		init();
	}

	//public synchronized static TTXMain getInstance(Context mContext) {
	//	if (instance == null) {
	//		instance = new TTXMain(mContext);
	//	}
	//	return instance;
	//}

	private void init() {
		topDialog = new TTXTopDialog(mContext);
		favDialog = new TTXFavListDialog(mContext);
		mTTXImpl = TeletextImplement.getInstance();
		TvCallbackHandler mTTXCallbackHandler = TvCallbackHandler.getInstance();
		mTTXCallbackHandler.addCallBackListener(mHandler);

		mNavBannerImplement = BannerImplement
				.getInstanceNavBannerImplement(mContext);

		String cusdata = DataSeparaterUtil.getInstance().getIniFilePath("apk", "m_pCustomerKeyMapName");
		if(TextUtils.isEmpty(cusdata)) {
		cusdata = ACTION_INI_PATH;
		}

		iniDoc = new IniDocument(cusdata);

		mixReuse = readFromINI("MIX_KEYCODE");
		holdReuse = readFromINI("HOLD_KEYCODE");
		indexReuse = readFromINI("INDEX_KEYCODE");
		revealReuse = readFromINI("REVEAL_KEYCODE");
		subcodeReuse = readFromINI("SUBCODE_KEYCODE");
		sizeReuse = readFromINI("SIZE_KEYCODE");
		exitReuse = readFromINI("CANCEL_KEYCODE");
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TTXMain init||mixReuse =" + mixReuse + "||holdReuse ="
				+ holdReuse + "||indexReuse =" + indexReuse + "||revealReuse ="
				+ revealReuse + "||subcodeReuse =" + subcodeReuse
				+ "||sizeReuse =" + sizeReuse + "||exitReuse =" + exitReuse);

		isTTXSupported = DataSeparaterUtil.getInstance() != null && DataSeparaterUtil.getInstance().isSupportTeletext();
		mScreenSaver = new MtkTvScreenSaverBase();
	}

	@Override
	public synchronized boolean deinitView() {
		//instance = null;
		TvCallbackHandler.getInstance().removeCallBackListener(mHandler);
		return super.deinitView();
	}

	@Override
	public boolean isVisible() {
	    // TODO Auto-generated method stub
	    return isActive;
	}
	@Override
	public boolean isKeyHandler(int keyCode) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isKeyHandler key =" + keyCode);
		if (keyCode == KeyMap.KEYCODE_MTKIR_MTKIR_TTX) {
			if (mNavBannerImplement != null
					&& !mNavBannerImplement.isShowTtxIcon()) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TTX not supported on current channel");
				Toast.makeText(mContext, R.string.menu_teletext_notsupport_tip,
						1000).show();
				return false;
			}

			if (!isTTXSupported) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TTX not Supported");
                return false;
            }

			String source = CommonIntegration.getInstance().getCurrentSource();
			if (source.contains("HDMI")) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "HDMI SOurce");
				return false;
			}

			if(mScreenSaver.getScrnSvrMsgID() == 21) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ttxmain scream");
				return false;
			}

			if (SundryImplement.getInstanceNavSundryImplement(mContext)
					.isFreeze()) {
				SundryImplement.getInstanceNavSundryImplement(mContext)
						.setFreeze(false);
			}
			if (!isNoTTX && mTTXImpl.startTTX(keyCode) == 0) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "ttxmain||isActive =" + isActive);
//				save flag for HotKeyDispatcher to avoid showing ad or sleep
				SaveValue.saveWorldBooleanValue(mContext, IS_TTX_SHOW, true,
						false);
				return isActive;
			} else if (isNoTTX) {
				isNoTTX = false;
			}
		}
		return false;
	}

	@Override
	public boolean isCoExist(int componentID) {
		return false;
	}

	@Override
	public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
		boolean isHandled = true;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TTXMainonKeyHandler keyCode =" + keyCode + "fromNative ="
				+ fromNative);
		if(!TextUtils.isEmpty(mixReuse) && keyCode == Integer.valueOf(mixReuse)) {
			KeyDispatch.getInstance().passKeyToNative(KeyMap.KEYCODE_MTKIR_MIX, event);
			return true;
		} else if(!TextUtils.isEmpty(holdReuse) && keyCode == Integer.valueOf(holdReuse)) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onKeyHandler holdReuse");
			KeyDispatch.getInstance().passKeyToNative(KeyMap.KEYCODE_MTKIR_HOLD, event);
			return true;
		}else if(!TextUtils.isEmpty(indexReuse) && keyCode == Integer.valueOf(indexReuse)) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onKeyHandler indexReuse");
			KeyDispatch.getInstance().passKeyToNative(KeyMap.KEYCODE_MTKIR_INDEX, event);
			return true;
		}else if(!TextUtils.isEmpty(revealReuse) && keyCode == Integer.valueOf(revealReuse)) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onKeyHandler revealReuse");
			KeyDispatch.getInstance().passKeyToNative(KeyMap.KEYCODE_MTKIR_REVEAL, event);
			return true;
		}else if(!TextUtils.isEmpty(subcodeReuse) && keyCode == Integer.valueOf(subcodeReuse)) {
			KeyDispatch.getInstance().passKeyToNative(KeyMap.KEYCODE_MTKIR_SUBCODE, event);
			return true;
		}else if(!TextUtils.isEmpty(sizeReuse) && keyCode == Integer.valueOf(sizeReuse)) {
			KeyDispatch.getInstance().passKeyToNative(KeyMap.KEYCODE_MTKIR_SIZE, event);
			return true;
		}else if(!TextUtils.isEmpty(exitReuse) && keyCode == Integer.valueOf(exitReuse)) {
			KeyDispatch.getInstance().passKeyToNative(KEY_CODE_SLEEP, event);
			return true;
		}
		switch (keyCode) {
		case KeyMap.KEYCODE_MENU:
		case KeyMap.KEYCODE_MTKIR_TIMER:
//		case KeyMap.KEYCODE_MTKIR_ZOOM:
		case KeyMap.KEYCODE_MTKIR_PEFFECT:
		case KeyMap.KEYCODE_MTKIR_MTSAUDIO:
		case KeyMap.KEYCODE_MTKIR_SLEEP:
			break;
		case KeyMap.KEYCODE_MTKIR_EJECT:
			// add cur show page focus on add page/ rm cur show page from
			// favlist, focus on 0.
			//if (!CommonIntegration.getInstance().isCurrentSourceHDMI()) {
			//	com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Other Source");
			//	favDialog.show();
			//	favDialog.setFavPage(mTTXImpl.getCurrentTeletextPage());
			//} else {
			//	com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "HDMI Source");
			//}
			break;

		case KeyMap.KEYCODE_MTKIR_PAUSE:
			// focus select cur show page, if has. or select first.
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "pause key received");
			//favDialog.setPositionByPage(mTTXImpl.getCurrentTeletextPage());
			//favDialog.show();
			break;

		case KeyMap.KEYCODE_MTKIR_STOP:
			//favDialog.processExtFAVkey();
			break;

		case KeyMap.KEYCODE_PERIOD:
//			if (mTTXImpl.hasTopInfo()) {
//				topDialog.show();
//			} else {
//				Toast.makeText(mContext, R.string.menu_teletext_no_top_tip,
//						1000).show();
//				return false;
//			}
			break;

		case KeyMap.KEYCODE_BACK:
//			if (noTopToast != null && noTopToast.isShowing()) {
//				return true;
//			}
//			stopTTX();
			isHandled = KeyDispatch.getInstance().passKeyToNative(
					20000, event);
			break;
		case KeyMap.KEYCODE_MTKIR_SOURCE:
//		case KeyMap.KEYCODE_MTKIR_REPEAT:
			break;
		case KeyMap.KEYCODE_MTKIR_GUIDE:// map guide key to root menu key
//			isHandled = KeyDispatch.getInstance().passKeyToNative(
//					KeyEvent.KEYCODE_HOME, event);
			break;
		case KeyMap.KEYCODE_MTKIR_SEFFECT:// map red key to dpad up key
//			isHandled = KeyDispatch.getInstance().passKeyToNative(
//					KeyEvent.KEYCODE_DPAD_UP, event);
			break;
		case KeyMap.KEYCODE_MTKIR_ASPECT:// map guide key to dpad down key
//			isHandled = KeyDispatch.getInstance().passKeyToNative(
//					KeyEvent.KEYCODE_DPAD_DOWN, event);
			break;
		case KeyMap.KEYCODE_MTKIR_PIPPOS:// map guide key to dpad left key
//			isHandled = KeyDispatch.getInstance().passKeyToNative(
//					KeyEvent.KEYCODE_DPAD_LEFT, event);
			break;
			// send  size/PIPSIZE key to native
//		case KeyMap.KEYCODE_MTKIR_PIPSIZE:
//			isHandled = KeyDispatch.getInstance().passKeyToNative(
//					KeyEvent.KEYCODE_DPAD_RIGHT, event);
//			break;
		default:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler dispatch native");
			// dispatch native
			isHandled = KeyDispatch.getInstance().passKeyToNative(keyCode,
					event);
			break;
		}
		return isHandled;
	}

	public void handlerTTXMessage(int message) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "handlerTTXMessage, message =" + message);
		switch (message) {
		case 0: // 0: TELETEXT_AVAILABLE
			break;
		case 1: // 1: TELETEXT_UNAVAILABLE
			break;
		case 2: // 2: TELETEXT_ACTIVE
			isActive = true;

			((NavBasicView) ComponentsManager.getInstance().getComponentById(
					NavBasicMisc.NAV_COMP_ID_BANNER)).setVisibility(View.GONE);

			if (ComponentsManager.getActiveCompId() == NavBasicMisc.NAV_COMP_ID_SUNDRY) {
				((SundryShowTextView) ComponentsManager.getInstance()
						.getComponentById(NavBasicMisc.NAV_COMP_ID_SUNDRY))
						.setVisibility(View.GONE);
			}
			if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_DVR)) {
				DvrManager.getInstance().uiManager.hiddenAllViews();
			}
//			else {
//				TimeShiftManager.getInstance().uiManager.hiddenAllViews();
//			}

			if (null != StateDvrFileList.getInstance()
					&& StateDvrFileList.getInstance().isShowing()) {
				StateDvrFileList.getInstance().dissmiss();
			}
			setVisibilityForNotify(View.VISIBLE);
			break;
		case 3: // 3:TELETEXT_INACTIVE
			isActive = false;
			stopTTX();
			if (favDialog != null && favDialog.isShowing()) {
				favDialog.dismiss();
			}
			if (topDialog != null && topDialog.isShowing()) {
				topDialog.dismiss();
			}
			break;
		case 4: // 4: NO_TELETEXT
			isNoTTX = true;
			isActive = false;
			Toast.makeText(mContext, R.string.menu_teletext_notsupport_tip,
					1000).show();
			setVisibilityForNotify(View.GONE);
			if (favDialog != null && favDialog.isShowing()) {
				favDialog.dismiss();
			}
			break;
		default:
			break;
		}
	}

	public void stopTTX() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopTTX is called");
		SaveValue.saveWorldBooleanValue(mContext, IS_TTX_SHOW, false, false);
		mTTXImpl.stopTTX(new OnStopTTXCallback() {
			@Override
			public void onStopTTX(final int resultCode) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (resultCode == 0){
							setVisibilityForNotify(View.GONE);
						}
					}
				});
			}
		});
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == TvCallbackConst.MSG_CB_BANNER_MSG) {
				TvCallbackData specialMsgData = (TvCallbackData) msg.obj;
				if (BANNER_MSG_NAV == specialMsgData.param1) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in handleMessage value=== "
							+ specialMsgData.param2);
					switch (specialMsgData.param2) {
					case NO_SIGNAL:
						if (topDialog != null && topDialog.isShowing()) {
							topDialog.dismiss();
						}
						if (favDialog != null && favDialog.isShowing()) {
							favDialog.dismiss();
						}
						break;
					default:
					    break;
					}
				}
			}
		}
	};

//	@Override
//	public void setVisibility(int visibility) {
//		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setVisibility");
//		super.setVisibility(visibility);
//	}

	public void setVisibilityForNotify(int visibility) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setVisibilityForNotify");
		super.setVisibility(visibility);
	}


    private String readFromINI(String keyName){
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "readFromINI keyName =" + keyName);
    	String key = "";
    	if (TextUtils.isEmpty(keyName)) {
    		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "readFromINI keyName is null !!!");
    		return "";
		}
    	if (iniDoc == null) {
    		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "iniDoc is null !!!");
			return "";
		}
    	try {
    		key = iniDoc.getTagValue(SECTION_NAME,keyName);
		} catch (Exception e) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTagValue exception !!!");
		}
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "readFromINI key =" + key);
    	return key.trim();
    }
}
