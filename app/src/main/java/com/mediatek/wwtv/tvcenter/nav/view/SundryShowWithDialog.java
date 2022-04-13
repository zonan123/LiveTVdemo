package com.mediatek.wwtv.tvcenter.nav.view;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.commonview.CustListView;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.adapter.SundryModeAdapter;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.IntegrationZoom;
import com.mediatek.wwtv.tvcenter.nav.util.SundryImplement;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

public class SundryShowWithDialog extends NavBasicDialog {
	private static final String TAG = "SundryShowWithDialog";
	private int lastPressKeyCode;

	private SundryImplement mSundryImplement;

	private TextView sundryModeTitleTextView;

	private CustListView sundryModeListView;

	private SundryModeAdapter mSundryModeAdapter;

	private List<Integer> supportPictureModeList;
	private List<Integer> supportSoundEffectList;
	private List<Integer> supportScreenModeList;

	private CommonIntegration mCommonIntegration;

	private IntegrationZoom mIntegrationZoom;

	private ZoomTipView mZoomTip;


	private int index = 0;

	private static final int LIST_PAGE_MAX = 7;

	public SundryShowWithDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		mSundryImplement = SundryImplement
				.getInstanceNavSundryImplement(context);
		mCommonIntegration = CommonIntegration.getInstance();
		ComponentsManager comManager = ComponentsManager.getInstance();
		mIntegrationZoom = IntegrationZoom.getInstance(context);
		mSundryModeAdapter = new SundryModeAdapter(context);
		mZoomTip = ((ZoomTipView) comManager
				.getComponentById(NAV_COMP_ID_ZOOM_PAN));
	}

	public SundryShowWithDialog(Context context) {
		this(context, R.style.nav_dialog);
		componentID = NAV_COMP_ID_SUNDRY_DIALOG;
		setContentView(R.layout.nav_sundry_dialog_list);
		initDialog();
	}

	@Override
	public boolean isKeyHandler(int keyCode) {
		// TODO Auto-generated method stub
		if (1 == SaveValue.getInstance(mContext).readValue(MenuConfigManager.MODE_LIST_STYLE)) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isKeyHandler");
			int currentModeIndex;
			switch (keyCode) {
			case KeyMap.KEYCODE_MTKIR_PEFFECT:
				lastPressKeyCode = keyCode;
				int[] supportPictureModes = mSundryImplement
						.getSupportPictureModes();
				supportPictureModeList.clear();
				if(supportPictureModes!=null){
    				for (int i :supportPictureModes) {
    					supportPictureModeList.add(Integer
    							.valueOf(i));
    				}
				}
				currentModeIndex = getCurrentValueIndex(supportPictureModes,
						mSundryImplement.getCurrentPictureMode());
				updataModeDate(supportPictureModeList, currentModeIndex,
						keyCode);
				sundryModeTitleTextView
						.setText(R.string.menu_video_picture_mode);
				return true;
			case KeyMap.KEYCODE_MTKIR_SEFFECT:
				lastPressKeyCode = keyCode;
				int[] supportSoundEffect = mSundryImplement
						.getSupportSoundEffects();
				supportSoundEffectList.clear();
				if(supportSoundEffect!=null){
    				for (int i :supportSoundEffect) {
    					supportSoundEffectList.add(Integer
    							.valueOf(i));
    				}
				}
				currentModeIndex = getCurrentValueIndex(supportSoundEffect,
						mSundryImplement.getCurrentSoundEffect());
				updataModeDate(supportSoundEffectList, currentModeIndex,
						keyCode);
				sundryModeTitleTextView.setText(R.string.menu_audio_equalize);
				return true;

			case KeyMap.KEYCODE_MTKIR_ASPECT:
				lastPressKeyCode = keyCode;
				int[] supportScreenModes = mSundryImplement
						.getSupportScreenModes();
				if (null != supportScreenModes) {
					if (!mIsComponetShow) {
						if (mSundryImplement.isFreeze()) {
							mSundryImplement.setFreeze(false);
						}
						if ((IntegrationZoom.ZOOM_1 != mIntegrationZoom
								.getCurrentZoom())
								&& !mCommonIntegration.isPipOrPopState()) {
							if ((null != mZoomTip)
									&& (View.VISIBLE == mZoomTip
											.getVisibility())) {
								mZoomTip.setVisibility(View.GONE);
							}

							if (ComponentsManager.getNativeActiveCompId() != NAV_NATIVE_COMP_ID_GINGA) {
								mIntegrationZoom
										.setZoomMode(IntegrationZoom.ZOOM_1);
							}
						}
						supportScreenModeList.clear();
						for (int i:supportScreenModes) {
							supportScreenModeList.add(Integer
									.valueOf(i));
						}
						currentModeIndex = getCurrentValueIndex(
								supportScreenModes,
								mSundryImplement.getCurrentScreenMode());
					updataModeDate(supportScreenModeList, currentModeIndex,
							keyCode);
						sundryModeTitleTextView
								.setText(R.string.menu_setup_screenmode);
				}
				return true;
				}
			default:
				break;
			}
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isKeyHandler false");
		return false;
	}

	@Override
	public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
		// TODO Auto-generated method stub
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in onKeyHandler,keycode == " + keyCode);
		boolean isHandler = true;
		boolean changeValueFlag;
		if (mIsComponetShow && (lastPressKeyCode == keyCode)) {
			changeValueFlag = true;
		} else {
			changeValueFlag = false;
		}
		switch (keyCode) {
		case KeyMap.KEYCODE_MTKIR_PEFFECT:
			int[] supportPictureModes = mSundryImplement
					.getSupportPictureModes();
		    supportPictureModeList.clear();
		    if(supportPictureModes==null){
		    	return true;}
		    for (int i :supportPictureModes) {
		        supportPictureModeList.add(Integer
		                .valueOf(i));
		    }
			if (changeValueFlag) {
				index = getCurrentValueIndex(supportPictureModes,
						mSundryImplement.getCurrentPictureMode());
				if (index < supportPictureModeList.size() - 1) {
					index = index + 1;
				} else {
					index = 0;
				}
				if(mSundryImplement.getSupportPictureModes()!=null){
				mSundryImplement.setCurrentPictureMode(mSundryImplement
						.getSupportPictureModes()[index]);}
			} else {
				index = getCurrentValueIndex(supportPictureModes,
						mSundryImplement.getCurrentPictureMode());
				sundryModeTitleTextView
						.setText(R.string.menu_video_picture_mode);

			}
			updataModeDate(supportPictureModeList, index, keyCode);
			break;
		case KeyMap.KEYCODE_MTKIR_SEFFECT:
			int[] supportSoundEffect = mSundryImplement
					.getSupportSoundEffects();
			  if(supportSoundEffect==null){
			    	return true;
			    }
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in onKeyHandler supportSoundEffect.length =="
					+ supportSoundEffect.length);
		    supportSoundEffectList.clear();
		    for (int i:supportSoundEffect) {
		        supportSoundEffectList.add(Integer
		                .valueOf(i));
		    }
			if (changeValueFlag) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
						"come in onKeyHandler supportSoundEffectList.size()==="
								+ supportSoundEffectList.size());
				index = getCurrentValueIndex(supportSoundEffect,
						mSundryImplement.getCurrentSoundEffect());
				if (index < supportSoundEffectList.size() - 1) {
					index = index + 1;
				} else {
					index = 0;
				}
				if(mSundryImplement
						.getSupportSoundEffects()!=null){
				mSundryImplement.setCurrentSoundEffect(mSundryImplement
						.getSupportSoundEffects()[index]);
				}
			} else {
				index = getCurrentValueIndex(supportSoundEffect,
						mSundryImplement.getCurrentSoundEffect());
				sundryModeTitleTextView
						.setText(R.string.menu_audio_equalize);
			}
			updataModeDate(supportSoundEffectList, index, keyCode);
			break;
		case KeyMap.KEYCODE_MTKIR_ASPECT:
			int[] supportScreenModes;
			if (changeValueFlag) {
				int result = 0;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
						"come in onKeyHandler KEYCODE_MTKIR_ASPECT changeValueFlag");
				mCommonIntegration
						.updateOutputChangeState(CommonIntegration.SCREEN_MODE_CHANGE_BEFORE);
				do {
					supportScreenModes = mSundryImplement
							.getSupportScreenModes();

					if (null != supportScreenModes) {
						supportScreenModeList.clear();
						for (int i :supportScreenModes) {
							supportScreenModeList.add(Integer
									.valueOf(i));
						}
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(
								TAG,
								"come in onKeyHandler KEYCODE_MTKIR_ASPECT getCurrentScreenMode == "
										+ mSundryImplement
												.getCurrentScreenMode());
						index = getCurrentValueIndex(supportScreenModes,
								mSundryImplement.getCurrentScreenMode());
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
								"come in onKeyHandler KEYCODE_MTKIR_ASPECT get current index == "
										+ index);
						if (index < supportScreenModeList.size() - 1) {
							index = index + 1;
						} else {
							index = 0;
						}
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
								"come in onKeyHandler KEYCODE_MTKIR_ASPECT change next screen mode index == "
										+ index);
						result = mSundryImplement
								.setCurrentScreenMode(supportScreenModes[index]);
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
								"come in onKeyHandler KEYCODE_MTKIR_ASPECT change next screen mode result == "
										+ result);
					}
				} while (0 != result);
				mCommonIntegration
						.updateOutputChangeState(CommonIntegration.SCREEN_MODE_CHANGE_AFTER);
			} else {
				supportScreenModes = mSundryImplement.getSupportScreenModes();
				if (null != supportScreenModes) {
					supportScreenModeList.clear();
					for (int i : supportScreenModes) {
						supportScreenModeList.add(Integer
								.valueOf(i));
					}
					index = getCurrentValueIndex(supportScreenModes,
							mSundryImplement.getCurrentScreenMode());
					if (mSundryImplement.isFreeze()) {
						mSundryImplement.setFreeze(false);
					}
					if (IntegrationZoom.ZOOM_1 != mIntegrationZoom
							.getCurrentZoom()) {
						if ((null != mZoomTip)
								&& (View.VISIBLE == mZoomTip.getVisibility())) {
							mZoomTip.setVisibility(View.GONE);
						}
						mIntegrationZoom.setZoomMode(IntegrationZoom.ZOOM_1);
					}
				}
				sundryModeTitleTextView.setText(R.string.menu_setup_screenmode);
			}
			if (null != supportScreenModes) {
				updataModeDate(supportScreenModeList, index, keyCode);
			} else {
				//need to do
				isHandler = false;
				dismiss();
			}
			break;

		case KeyMap.KEYCODE_BACK:
			dismiss();
			return true;
		case KeyMap.KEYCODE_MENU:
		case KeyMap.KEYCODE_MTKIR_GUIDE:
		case KeyMap.KEYCODE_VOLUME_DOWN:
		case KeyMap.KEYCODE_VOLUME_UP:
		case KeyMap.KEYCODE_MTKIR_PIPPOP:
		case KeyMap.KEYCODE_MTKIR_PIPPOS:
		case KeyMap.KEYCODE_MTKIR_MTKIR_SWAP:
		case KeyMap.KEYCODE_MTKIR_PIPSIZE:
		case KeyMap.KEYCODE_MTKIR_INFO:
			dismiss();
			isHandler = false;
			break;
		case KeyMap.KEYCODE_MTKIR_CHDN:
		case KeyMap.KEYCODE_MTKIR_CHUP:
		case KeyMap.KEYCODE_MTKIR_PRECH:
			return true;
		case KeyMap.KEYCODE_DPAD_DOWN:
		case KeyMap.KEYCODE_DPAD_UP:
			if (mCommonIntegration.isPipOrPopState()) {
				dismiss();
				isHandler = false;
				break;
			} else {
				return true;
			}
		default:
			isHandler = false;
			break;
		}

		if (isHandler) {
			lastPressKeyCode = keyCode;
			startTimeout(NAV_TIMEOUT_5);
		}
		if (!isHandler && TurnkeyUiMainActivity.getInstance() != null) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in keyHandler dispatch key to turnkey");
			return TurnkeyUiMainActivity.getInstance().onKeyHandler(keyCode,
					event);
		}
		return isHandler;
	}

	@Override
	public boolean isCoExist(int componentID) {
		// TODO Auto-generated method stub
		switch (componentID) {
		case NAV_COMP_ID_CEC:
			return KeyMap.KEYCODE_MTKIR_FREEZE != lastPressKeyCode;
		case NAV_COMP_ID_ZOOM_PAN:
		case NAV_COMP_ID_BANNER:
		case NAV_COMP_ID_POP:
		case NAV_COMP_ID_PVR_TIMESHIFT:
			return true;
			default:
			    break;
		}
		return false;
	}

	private void initDialog() {
		sundryModeTitleTextView = (TextView) findViewById(R.id.nav_sundry_mode_title);
		sundryModeListView = (CustListView) findViewById(R.id.nav_sundry_mode_listview);

		sundryModeListView.setOnKeyListener(new SundryModeListOnKey());
		supportPictureModeList = new ArrayList<Integer>();
		supportSoundEffectList = new ArrayList<Integer>();
		supportScreenModeList = new ArrayList<Integer>();
		Window window = getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();

		lp.width = (int) (ScreenConstant.SCREEN_WIDTH * 0.27);
		lp.height = (int) (ScreenConstant.SCREEN_HEIGHT * 0.56);

		lp.x = -(int) (ScreenConstant.SCREEN_WIDTH * 0.2);
		lp.y = (int) (ScreenConstant.SCREEN_HEIGHT * 0.4);
		window.setAttributes(lp);
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
		startTimeout(NAV_TIMEOUT_5);
	}

	class SundryModeListOnKey implements View.OnKeyListener {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			int slectPosition = sundryModeListView.getSelectedItemPosition();

			if (slectPosition < 0) {
				slectPosition = 0;
			}
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_CENTER:
					int selectedValue = (Integer) sundryModeListView
							.getSelectedItem();
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
							"come in SundryModeListOnKey get selected item vlaue == "
									+ selectedValue);
					switch (lastPressKeyCode) {
					case KeyMap.KEYCODE_MTKIR_PEFFECT:
						if (selectedValue != mSundryImplement
								.getCurrentPictureMode()) {
							com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
									"come in SundryModeListOnKey setCurrentPictureMode vlaue == "
											+ selectedValue);
							mSundryImplement
									.setCurrentPictureMode(selectedValue);
						} else {
							dismiss();
						}
						break;
					case KeyMap.KEYCODE_MTKIR_SEFFECT:
						if (selectedValue != mSundryImplement
								.getCurrentSoundEffect()) {
							com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
									"come in SundryModeListOnKey setCurrentSoundEffect vlaue == "
											+ selectedValue);
							mSundryImplement
									.setCurrentSoundEffect(selectedValue);
						} else {
							dismiss();
						}
						break;
					default:
						break;
					}

					startTimeout(NAV_TIMEOUT_5);
					return true;
				case KeyEvent.KEYCODE_DPAD_DOWN:
					startTimeout(NAV_TIMEOUT_5);
					return false;

				case KeyEvent.KEYCODE_DPAD_UP:
					startTimeout(NAV_TIMEOUT_5);
					if(slectPosition == 0 && !sundryModeListView.hasPrePage()){
						return true;
					}
					return false;

				default:
					return false;
				}
			}
			return false;
		}
	}

	// gain the index of the current value whit the current type
	public int getCurrentValueIndex(int[] currentArray, int value) {
		if (currentArray != null) {
			for (int i = 0; i < currentArray.length; i++) {
				if (value == currentArray[i]) {
					return i;
				}
			}
		}

		return 0;
	}

	private void updataModeDate(List<Integer> modeList, int currentModeIndex,
			int key) {
		sundryModeListView.initData(modeList, LIST_PAGE_MAX, modeListUpdate);
		int currentIndexPage = currentModeIndex / LIST_PAGE_MAX + 1;
		if (currentIndexPage > 1) {
			mSundryModeAdapter.updateList((List<Integer>) sundryModeListView
					.getListWithPage(currentIndexPage), key);
		} else {
			mSundryModeAdapter.updateList(
					(List<Integer>) sundryModeListView.getCurrentList(), key);
		}

		sundryModeListView.setAdapter(mSundryModeAdapter);
		sundryModeListView.setFocusable(true);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in updataModeDate setSelection currentModeIndex =="
				+ currentModeIndex);
		sundryModeListView.setSelection(currentModeIndex % LIST_PAGE_MAX);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in updataModeDate setSelection ==="
				+ currentModeIndex % LIST_PAGE_MAX);
		sundryModeListView.requestFocus();
	}

	private CustListView.UpDateListView modeListUpdate = new CustListView.UpDateListView() {
		@Override
		public void updata() {
			// TODO Auto-generated method stub
			if (sundryModeListView.getSelectedItemPosition() == 0) {
				mSundryModeAdapter
						.updateList((List<Integer>) sundryModeListView
								.getPreList());
			} else {
				mSundryModeAdapter
						.updateList((List<Integer>) sundryModeListView
								.getNextList());
			}
			sundryModeListView.setAdapter(mSundryModeAdapter);
		}

	};
}