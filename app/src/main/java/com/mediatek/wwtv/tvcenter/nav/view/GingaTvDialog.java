package com.mediatek.wwtv.tvcenter.nav.view;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mediatek.wwtv.tvcenter.commonview.ConfirmDialog;
import com.mediatek.wwtv.tvcenter.commonview.ConfirmDialog.IResultCallback;
import com.mediatek.wwtv.tvcenter.commonview.CustListView;
import com.mediatek.wwtv.tvcenter.commonview.CustListView.UpDateListView;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.twoworlds.tv.MtkTvGinga;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvConfig;


/**
 * @author MTK40707
 */
public class GingaTvDialog extends NavBasicDialog implements
		ComponentStatusListener.ICStatusListener, IResultCallback {
	private static final String TAG = "GingaTvDialog";

	// dialog size
	private static int mWidth = 650;
	private static int mHeight = 390;
	private static int mPerPage = 10;

	// local view variables
	private CustListView mCustListView = null;
	private TextView mTextViewMessage = null;
	private LinearLayout mLinearLayout = null;

	private GingaTvAdapter mGingaTvAdapter = null;

	private String mSelectedAppId = "";
	private Map<String, String> mCurrentApps = null;
	private boolean mGetGingaInfo = false;
	private AudioManager mAudioManager;
	private static final String RECOVER_VOLUME = "recover_volume_value";
	//private SimpleDialog mSimpleDialog = null;

	private boolean isFistSet = true;

	private final static int LOADING_SHOW = 2;
	private final static int LOADING_HIDE = 3;

	/**
	 * @param context
	 */
	public GingaTvDialog(Context context) {
		this(context, R.style.nav_dialog);
	}

	/**
	 * @param context
	 * @param theme
	 */
	public GingaTvDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		this.componentID = NAV_COMP_ID_GINGA_TV;

		mCurrentApps = new HashMap<String, String>();

		// //Add key Listener
		// ComponentStatusListener.getInstance().addListener(
		// ComponentStatusListener.NAV_KEY_OCCUR, this);
		// Add component status Listener
		ComponentStatusListener.getInstance().addListener(
				ComponentStatusListener.NAV_COMPONENT_HIDE, this);
		// Add enter lancher Listener
		ComponentStatusListener.getInstance().addListener(
				ComponentStatusListener.NAV_ENTER_LANCHER, this);
		// Add enter lancher Listener
		ComponentStatusListener.getInstance().addListener(
				ComponentStatusListener.NAV_RESUME, this);
		mAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	public boolean isKeyHandler(int keyCode) {
		// TODO Auto-generated method stub
		//if (keyCode == KeyMap.KEYCODE_MTKIR_SUBTITLE) {
			// if ((null != StateFileList.getInstance())
			// && StateFileList.getInstance().isShowing()) {
			// return false;
			// }
		//	return true;
		//}

		return keyCode == KeyMap.KEYCODE_MTKIR_SUBTITLE;
	}

	@Override
	public boolean isCoExist(int componentID) {
		// TODO Auto-generated method stub
		//if (componentID == NAV_COMP_ID_BANNER || componentID == NAV_COMP_ID_POP
		//		|| componentID == NAV_COMP_ID_PVR_TIMESHIFT) {
		//	return true;
		//}

		return componentID == NAV_COMP_ID_BANNER || componentID == NAV_COMP_ID_POP
				|| componentID == NAV_COMP_ID_PVR_TIMESHIFT;
	}

	@Override
	public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
		boolean isHandled = true;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler: keyCode=" + keyCode);
		keyCode = KeyMap.getKeyCode(keyCode, event);
		switch (keyCode) {
		case KeyMap.KEYCODE_MTKIR_GUIDE:
		case KeyMap.KEYCODE_MTKIR_SOURCE:
		case KeyMap.KEYCODE_MENU:
			// other components or activity may need these key
		case KeyMap.KEYCODE_MTKIR_PIPPOP:
			this.dismiss();
			isHandled = false;
			break;
		case KeyMap.KEYCODE_MTKIR_GREEN:
		case KeyEvent.KEYCODE_G:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_GREEN");
			if (mCurrentApps != null && mCurrentApps.size() > mPerPage) {
				if (mCustListView.getSelectedItemPosition() == mPerPage - 1) {
					// update adapter list data by get next page datas
					mGingaTvAdapter.updateData((List<String>) (mCustListView
							.getNextList()));
				}
				mCustListView.setSelection(mPerPage - 1);
			}
			return true;
		case KeyMap.KEYCODE_MTKIR_RED:
		case KeyEvent.KEYCODE_R:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KEYCODE_MTKIR_RED");
			if (mCurrentApps != null && mCurrentApps.size() > mPerPage) {
				if (mCustListView.getSelectedItemPosition() == 0) {
					// update adapter list data by get pro page datas
					mGingaTvAdapter.updateData((List<String>) (mCustListView
							.getPreList()));
				}
				mCustListView.setSelection(0);
			}
			return true;
		case KeyMap.KEYCODE_BACK:
		case KeyMap.KEYCODE_MTKIR_SUBTITLE:
			this.dismiss();
			break;
		case KeyMap.KEYCODE_MTKIR_MUTE:
			break;
		case KeyMap.KEYCODE_MTKIR_MTKIR_SWAP:
		case KeyMap.KEYCODE_MTKIR_STOP:
			this.dismiss();
			isHandled = false;
			break;
		default:
			isHandled = false;
			break;
		}

		/* Back Key to TurnkeyUiMainActivity */
		if ((isHandled == false) && TurnkeyUiMainActivity.getInstance() != null) {
			return TurnkeyUiMainActivity.getInstance().onKeyHandler(keyCode,
					event);
		}
		return isHandled;
	}

	@Override
	public boolean initView() {
		setContentView(R.layout.nav_ginga_tv);

		mCustListView = (CustListView) findViewById(R.id.nav_ginga_tv_listview);
		mTextViewMessage = (TextView) findViewById(R.id.nav_ginga_message);
		mLinearLayout = (LinearLayout) findViewById(R.id.nav_ginga_tv_pageupdown);

		mGingaTvAdapter = new GingaTvAdapter(getContext());
		mCustListView.setAdapter(mGingaTvAdapter);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		// trigger ginga
		clearApps();

		mGetGingaInfo = true;
		MtkTvGinga.getInstance().getApplicationInfo();

		setWindowPosition();
		// update list
		updateList();

		// check ginga setting
		int value = MtkTvConfig.getInstance()
				.getConfigValue(MtkTvConfigType.CFG_GINGA_GINGA_ENABLE);
		if (mCurrentApps.isEmpty() || (value == 0)) {
			mCustListView.setVisibility(View.GONE);
			mTextViewMessage.setVisibility(View.VISIBLE);
		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "gingTv||show apps");
			mCustListView.setVisibility(View.VISIBLE);
			mTextViewMessage.setVisibility(View.GONE);

			mCustListView.setFocusable(true);
			mCustListView.setSelection(0);
			mCustListView.requestFocus();
		}

		this.startTimeout(NAV_TIMEOUT_5);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchKeyEvent: keyCode=" + keyCode);
		this.startTimeout(NAV_TIMEOUT_5);
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
			case KeyMap.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_E:
				// start ginga app
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectedItem:" + mCustListView.getSelectedItem());
				this.dismiss();

				// [ADD]check cc setting
				MtkTvConfig config = MtkTvConfig.getInstance();
				int digcc = config
						.getConfigValue(MtkTvConfigType.CFG_CC_DIGITAL_CC);
				int supercc = config
						.getConfigValue(MtkTvConfigType.CFG_CC_DIGITAL_CC);
				if (digcc == MtkTvConfigType.ACFG_CC_ON || supercc != 0) {
					// ConfirmDialog dialog = new ConfirmDialog(mContext,
					// mContext.getString(R.string.nav_ginga_tv_for_tips));
					// dialog.setTimeout(NAV_TIMEOUT_5);
					// dialog.setCallback(this);
					// dialog.showDialog();
					//
					Log.d(TAG,"handle WARNING_MSG_STOP_CC_START_GINGA");
					// CommonMsgDialog mCommonMsgDialog = (CommonMsgDialog)
					// ComponentsManager
					// .getInstance().getComponentById(NavBasic.NAV_COMP_ID_DIALOG_MSG);
					// if(mCommonMsgDialog != null){
					// mCommonMsgDialog.commonMsgHanndler(CommonMsgDialog.WARNING_MSG_STOP_CC_START_GINGA,
					// 0,
					// 0, null);
					// }
					// return true;
				}

				String selected = (String) mCustListView.getSelectedItem();
				if (mCurrentApps != null
						&& mCurrentApps.containsValue(selected)) {
					for (String key : mCurrentApps.keySet()) {
						String value = mCurrentApps.get(key);
						if (!value.equals(selected)) {
							continue;
						}

						if (mSelectedAppId != null
								&& mSelectedAppId.equals(key)) {
							// already select it, pass key to ginga app
							com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startApplication, key=" + key);
							if (mSelectedAppId.length() > 0) {
								MtkTvGinga.getInstance().stopApplication(key);
							}
						} else {
							// stop last app before starting next one
							MtkTvGinga.getInstance().stopApplication(
									mSelectedAppId);
							MtkTvGinga.getInstance().startApplication(key);
							// InfoBarDialog.getInstance(mContext)
							// .show(InfoBarDialog.INFO_BAR,mContext.getString(R.string.nav_ginga_tv_for_infobar));
						}
					}
				}

				return true;
			default:
				break;
			}
		}

		return super.dispatchKeyEvent(event);
	}

	/**
	 * the method is used to resize the position & size of dialog
	 */
	private void setWindowPosition() {
		// TODO
		Window window = getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();

		lp.width = mWidth * ScreenConstant.SCREEN_WIDTH / 1280;
		lp.height = mHeight * ScreenConstant.SCREEN_HEIGHT / 720;

		lp.x = ScreenConstant.SCREEN_WIDTH / 2 - lp.width / 2;
		lp.y = ScreenConstant.SCREEN_HEIGHT / 2 - lp.height / 2 - 50;

		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ScreenConstant.SCREEN_WIDTH="
				+ ScreenConstant.SCREEN_WIDTH
				+ ",ScreenConstant.SCREEN_HEIGHT="
				+ ScreenConstant.SCREEN_HEIGHT + ",lp.width=" + lp.width + ","
				+ lp.x + ", lp.height=" + lp.height + "," + lp.y);
		window.setAttributes(lp);
	}

	private boolean updateList() {
		List<String> list;

		// page up/down show/hide
		if ((mCurrentApps != null) && (mCurrentApps.size() > mPerPage)) {
			mLinearLayout.setVisibility(View.VISIBLE);
		} else {
			mLinearLayout.setVisibility(View.INVISIBLE);
		}
		// if ginga enable is off,clear list to avoid ui error
		int gingaEnable = MtkTvConfig.getInstance()
				.getConfigValue(MtkTvConfigType.CFG_GINGA_GINGA_ENABLE);
		if (gingaEnable == 0) {
			Log.d(TAG, "gingaDisabled");
			mGingaTvAdapter.clearList();
			if (mCurrentApps != null) {
				mCurrentApps.clear();
			} else {
				Log.d(TAG, "gingaDisabled : mCurrentApps == null");
			}
			return false;
		}

		if (mCurrentApps == null || mCurrentApps.isEmpty()) {
			return false;
		} else {
			list = new ArrayList<String>();
			for (String key : mCurrentApps.keySet()) {
				list.add(mCurrentApps.get(key));
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "create list:" + mCurrentApps.get(key));
			}
		}

		mCustListView.initData(list, mPerPage, mUpDateListView);

		mGingaTvAdapter.updateData((List<String>) (mCustListView
				.getCurrentList()));
		return true;
	}

	private final UpDateListView mUpDateListView = new UpDateListView() {
		@Override
		public void updata() {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "UpDateListView");

			if (mCustListView.getSelectedItemPosition() == 0) {
				// update adapter list data by get pro page datas
				mGingaTvAdapter.updateData((List<String>) (mCustListView
						.getPreList()));
			} else {
				// update adapter list data by get pro page datas
				mGingaTvAdapter.updateData((List<String>) (mCustListView
						.getCurrentList()));
			}
			mCustListView.setAdapter(mGingaTvAdapter);
		}
	};

	private class GingaTvAdapter extends BaseAdapter {
		private final LayoutInflater mInflater;
		private List<String> mCurrentList = null;

		public GingaTvAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			if (mCurrentList != null) {
				return mCurrentList.size();
			}
			return 0;
		}

		@Override
		public String getItem(int position) {
			if (mCurrentList != null) {
				return mCurrentList.get(position);
			}
			return "";
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void updateData(List<String> mCurrentApps) {
			this.mCurrentList = mCurrentApps;
			notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder hodler;
			String mCurrentApp;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.nav_ginga_tv_item,
						null);
				hodler = new ViewHolder();
				hodler.mImageView = (ImageView) convertView
						.findViewById(R.id.nav_ginga_tv_item_imageView);
				hodler.mTextView = (TextView) convertView
						.findViewById(R.id.nav_ginga_tv_item_textView);
				convertView.setTag(hodler);
			} else {
				hodler = (ViewHolder) convertView.getTag();
			}

			mCurrentApp = mCurrentList.get(position);
			hodler.mTextView.setText(mCurrentApp);

			if (mCurrentApps != null) {
				String selectName = mCurrentApps.get(mSelectedAppId);
				if (selectName != null && selectName.equals(mCurrentApp)) {
					hodler.mImageView
							.setImageResource(R.drawable.nav_source_list_select_icon);
					return convertView;
				}
			}

			hodler.mImageView.setImageResource(0);
			return convertView;
		}

		public void clearList() {
			if (mCurrentList != null) {
				mCurrentList.clear();
				notifyDataSetChanged();
			}
		}

		private class ViewHolder {
			ImageView mImageView;
			TextView mTextView;
		}
	}

	/**
	 * InternalHandler
	 */
	private static class InternalHandler extends Handler {
		private final WeakReference<GingaTvDialog> mDialog;

		public InternalHandler(GingaTvDialog dialog) {
			mDialog = new WeakReference<GingaTvDialog>(dialog);
		}

		@Override
		public void handleMessage(Message msg) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] handlerMessage occur~");
			if (mDialog.get() == null) {
				return;
			}

		}
	}

	public void changeVolume(int type, int level) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeVolume||type =" + type + "||level =" + level);
		switch (type) {
		case 0:// set volume
			int minValue = mAudioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC);
			int currentValue = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			int newValue = minValue + ((currentValue - minValue) * level) / 100;

			if (isFistSet) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeVolume||currentValue =" + currentValue);
				isFistSet = false;
				SaveValue.getInstance(mContext).saveValue(RECOVER_VOLUME,currentValue);
			} else {
				int savedValue = SaveValue.getInstance(mContext).readValue(RECOVER_VOLUME, -1);
				if (savedValue != -1) {
					newValue = minValue + ((savedValue - minValue) * level) / 100;
				}
			}

			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newValue,
					AudioManager.FLAG_PLAY_SOUND);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeVolume||minValue =" + minValue	+ "||newValue =" + newValue);
			break;
		case 1:// no operate
			break;
		case 2:// set mute
			mAudioManager.adjustVolume(AudioManager.ADJUST_MUTE,
					AudioManager.FLAG_PLAY_SOUND);
			break;
		case 3:// set unmute
			mAudioManager.adjustVolume(AudioManager.ADJUST_UNMUTE,
					AudioManager.FLAG_PLAY_SOUND);
			break;
		case 4:// recover volume
			isFistSet = true;
			int recoverValue = SaveValue.getInstance(mContext).readValue(
					RECOVER_VOLUME, -1);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeVolume||recoverValue =" + recoverValue);
			if (recoverValue != -1) {
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						recoverValue, AudioManager.FLAG_PLAY_SOUND);
				SaveValue.getInstance(mContext).saveValue(RECOVER_VOLUME, -1);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * this mthod is used to add ginga info to object
	 *
	 * @param name
	 *            , ginga app name
	 * @param id
	 *            , ginga id
	 */
	public void addGingaAppInfo(int type, String id, String name) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addGingaAppInfo||type =" + type);
		switch (type) {
		case 0:
			if (mSelectedAppId.equals(id)) {
				mSelectedAppId = "";
			}
			ComponentsManager.updateActiveCompId(true, 0);
//			TwinkleDialog.showTwinkle();
			break;
		case 1:
			if (mSelectedAppId.equals(id)) {
				mSelectedAppId = "";
			}
			break;
		case 2:
			mSelectedAppId = id;
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addGingaAppInfo||CurrentScreenMode ="
					+ CommonIntegration.getInstance().getCurrentScreenMode());
			if (CommonIntegration.SCREEN_MODE_DOT_BY_DOT == CommonIntegration
					.getInstance().getCurrentScreenMode()) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addGingaAppInfo||MODE_NORMAL");
				CommonIntegration.getInstance().setCurrentScreenMode(
						CommonIntegration.SCREEN_MODE_NORMAL);
			}

//			if (InfoBarDialog.getInstance(mContext).isInfoIn()) {
// 					InfoBarDialog.getInstance(mContext).dismiss(
//				 	InfoBarDialog.LEVER_INFO);
//			}

			List<Integer> activeComps = ComponentsManager.getInstance()
					.getCurrentActiveComps();
			boolean isBannerActive = false;
			for (Integer compId : activeComps) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "compId:" + compId);
				if (compId == NAV_COMP_ID_BANNER) {
					isBannerActive = true;
					break;
				}
			}
			// hide all nav comps
			if (!mGetGingaInfo && !isBannerActive) {
				ComponentsManager.getInstance().hideAllComponents();
			}
			mGetGingaInfo = false;

			ComponentsManager
					.updateActiveCompId(true, NAV_NATIVE_COMP_ID_GINGA);
			break;
		default:
			return;
		}
		if (mCurrentApps != null && mCurrentApps.size() < 100) {
			mCurrentApps.put(id, name);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addGingaAppInfo: name:" + name + ",id:" + id
					+ ",mSelectedAppId=" + mSelectedAppId);
		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addGingaAppInfo: mCurrentApps = null or space full");
		}
		// update UI
		if (this.isVisible()) {
			// update list
			updateList();
			if (mCurrentApps == null || mCurrentApps.isEmpty()) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "show no apps");
				mCustListView.setVisibility(View.GONE);
				mTextViewMessage.setVisibility(View.VISIBLE);
			} else {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "show apps");
				mCustListView.setVisibility(View.VISIBLE);
				mTextViewMessage.setVisibility(View.GONE);

				mCustListView.setFocusable(true);
				mCustListView.setSelection(0);
				mCustListView.requestFocus();
			}
		}
	}

	/**
	 * this mthod is used to clear info
	 *
	 * @param code
	 *            , svctx code
	 */
	public void handleSvctxMessage(int code) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " handleSvctxMessage code = " + code);

		if (code == 1 || code == 2) {
			clearApps();
		}
	}

	/**
	 * this method is used to clear infos
	 */
	public void clearApps() {
		mCurrentApps.clear();
		mSelectedAppId = "";
	}

	@Override
	public void updateComponentStatus(int statusID, int value) {
		if (statusID == ComponentStatusListener.NAV_COMPONENT_HIDE) {
			if (!ComponentsManager.getInstance().isComponentsShow()) {
				ComponentsManager.nativeComponentReActive();
			}
		} else if (statusID == ComponentStatusListener.NAV_ENTER_LANCHER) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus||mSelectedAppId ="
					+ mSelectedAppId);

//			if (mSelectedAppId != null && mSelectedAppId.length() > 0) {
//				MtkTvGinga.getInstance().stopApplication(mSelectedAppId);
//			}
		} else if (statusID == ComponentStatusListener.NAV_RESUME) {
			MtkTvGinga.getInstance().getApplicationInfo();
		}
	}

	@Override
	public void handleUserSelection(int result) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleUserSelection, result=" + result);

		if (result == ConfirmDialog.BTN_YES_CLICK) {
			MtkTvConfig config = MtkTvConfig.getInstance();
			config.setConfigValue(MtkTvConfigType.CFG_CC_DIGITAL_CC,
					MtkTvConfigType.ACFG_CC_OFF);
		} else {
			return;
		}

		String selected = (String) mCustListView.getSelectedItem();
		if (mCurrentApps != null && mCurrentApps.containsValue(selected)) {
			for (String key : mCurrentApps.keySet()) {
				String value = mCurrentApps.get(key);
				if (value.equals(selected) && (!mSelectedAppId.equals(key))) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startApplication, key=" + key);
					MtkTvGinga.getInstance().startApplication(key);

					// InfoBarDialog.getInstance(mContext).show(InfoBarDialog.INFO_BAR,
					// mContext.getString(R.string.nav_ginga_tv_for_infobar));
//					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleUserSelection||SimpleDialog");
//					handleLoadingMessage(LOADING_SHOW);
				}
			}
		}
	}

	@Override
	public void dismiss() {
		super.dismiss();
		if (isShowing()) {
			this.dismiss();
		}
	}

	@Override
	public void show() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ginga is showing");
		super.show();
	}

	public void handleLoadingMessage(int status) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleLoadingMessage||status =" + status);
        SimpleDialog mSimpleDialog = (SimpleDialog) ComponentsManager
                .getInstance().getComponentById(NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
		switch (status) {
		case LOADING_SHOW:
			mSimpleDialog.setContent(mContext.getString(R.string.nav_ginga_tv_for_infobar));
			mSimpleDialog.show();
			break;
		case LOADING_HIDE:
			if (mSimpleDialog != null && mSimpleDialog.isShowing()) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleLoadingMessage||dismiss");
				mSimpleDialog.dismiss();
			} else {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mSimpleDialog  LOADING_HIDE()");
			}
			break;

		default:
			break;
		}
	}
}
