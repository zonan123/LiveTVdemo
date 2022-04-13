package com.mediatek.wwtv.tvcenter.nav.util;

import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.fav.FavoriteListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.FocusLabel;
import com.mediatek.wwtv.tvcenter.nav.view.MiscView;
import com.mediatek.wwtv.tvcenter.nav.view.SundryShowTextView;
import com.mediatek.wwtv.tvcenter.nav.view.TwinkleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.ZoomTipView;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyDispatch;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.commonview.TvSurfaceView;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

public class TVStateControl extends NavBasicMisc implements
		ComponentStatusListener.ICStatusListener {

	private final CommonIntegration mCommonIntegration;

	private static final String TAG = "TVStateControl";

	private final KeyDispatch mDispatch;

	private final ComponentsManager mComponentsManager;

	private ChannelListDialog mChannelListDialog;
    private FavoriteListDialog mFavoriteChannelListView;
	private SundryShowTextView mShowTextView;
	private BannerView mBannerView;
	private ZoomTipView mZoomTipView;
	private MiscView mMiscView;
	private final IntegrationZoom mIntegrationZoom;

	private final SundryImplement mSundryImplement;

//	private final MtkTvPipPop mMtkTvPipPop;
//	private MtkTvPipPopFucusInfoBase mMtkTvPipPopFucusInfo;

//	private final InputSourceManager mSourceManager;
	private final PIPPOPSurfaceViewControl mViewControl;
	private FocusLabel mFocusLabel;

//	private static final int ENTER_PIP_STATE = 0;
//	private static final int ENTER_POP_STATE = 1;
//	private static final int ENTER_NORMAL_STATE = 2;
//	private static final int SWAP_CHANEGE = 3;
//	private static final int AUDIO_CHANGE = 4;
//	private static final int FOCUS_CHANGE = 5;

	public static final int SWAP_KEY_VALUE = 315;
//	private static final int PIPPOS_KEY_VALUE = 311;
//	private static final int PIPSIZE_KYE_VALUE = 312;

//	private boolean bindSubSource = false;

	private String currentFocusWin;

//	private int focusIconMarginX;
//	private int focusIconMarginY;

//	private String mainSourceName = "";
//	private String subSourceName = "";
//	private String focusSourceName ="";

	public void setOutputView(TvSurfaceView mainOutput,
			TvSurfaceView subOutput, LinearLayout mainLY, LinearLayout subLY) {
		mViewControl.setSignalOutputView(mainOutput, subOutput, mainLY, subLY);
	}

	public TVStateControl(Context mContext) {
		super(mContext);
		mViewControl = PIPPOPSurfaceViewControl.getSurfaceViewControlInstance();
		componentID = NAV_COMP_ID_POP;
		mDispatch = KeyDispatch.getInstance();
		mCommonIntegration = CommonIntegration.getInstance();
		mComponentsManager = ComponentsManager.getInstance();
//		mMtkTvPipPop = MtkTvPipPop.getInstance();
		mIntegrationZoom = IntegrationZoom.getInstance(mContext);
		mSundryImplement = SundryImplement
				.getInstanceNavSundryImplement(mContext);
//		mSourceManager = InputSourceManager.getInstance(mContext);
		initComponentsView();

		// Add nav resume Listener
		ComponentStatusListener.getInstance().addListener(
				ComponentStatusListener.NAV_RESUME, this);
		ComponentStatusListener.getInstance().addListener(
				ComponentStatusListener.NAV_ENTER_STANDBY, this);
		ComponentStatusListener.getInstance().addListener(
				ComponentStatusListener.NAV_ENTER_MMP, this);
	}

	private void initComponentsView() {
		mChannelListDialog = (ChannelListDialog) mComponentsManager
				.getComponentById(NAV_COMP_ID_CH_LIST);
        mFavoriteChannelListView = (FavoriteListDialog) mComponentsManager
				.getComponentById(NAV_COMP_ID_FAV_LIST);
		mShowTextView = (SundryShowTextView) mComponentsManager
				.getComponentById(NAV_COMP_ID_SUNDRY);
		if(!TurnkeyUiMainActivity.getInstance().isUKCountry){
		mBannerView = (BannerView) mComponentsManager
				.getComponentById(NAV_COMP_ID_BANNER);
		}
		mBannerView.setAlpha(0.9f);
		mZoomTipView = (ZoomTipView) mComponentsManager
				.getComponentById(NAV_COMP_ID_ZOOM_PAN);
		mMiscView = (MiscView) mComponentsManager
				.getComponentById(NAV_COMP_ID_MISC);
	}

	@Override
	public void setVisibility(int visibility) {
		// TODO Auto-generated method stub
		if (View.VISIBLE == visibility) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(
					TAG,
					"come in TVStateControl ComponentStatusListener.NAV_ENTER_STANDBY setVisibility,mFocusLabel.getVisibility()="
							+ mFocusLabel.getVisibility());
			if (View.VISIBLE != mFocusLabel.getVisibility()) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(
						TAG,
						"come in TVStateControl ComponentStatusListener.NAV_ENTER_STANDBY  mFocusLabel.show() =="
								+ mCommonIntegration.isPipOrPopState());
				if (mCommonIntegration.isPipOrPopState()) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
							"come in TVStateControl setVisibility mFocusLabel.show()");
					mFocusLabel.show();
				}
			}
		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(
					TAG,
					"come in TVStateControl ComponentStatusListener.NAV_ENTER_STANDBY setVisibility="
							+ mFocusLabel.getVisibility());
			if (View.VISIBLE == mFocusLabel.getVisibility()) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(
						TAG,
						"come in TVStateControl ComponentStatusListener.NAV_ENTER_STANDBY mFocusLabel.release()");
				mFocusLabel.release();
			}
		}
		super.setVisibility(visibility);
	}

	@Override
	public boolean isCoExist(int componentID) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isKeyHandler(int keyCode) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyMap.KEYCODE_MTKIR_PIPPOP:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isKeyHandler, KEYCODE_MTKIR_PIPPOP");
			hideComponentsWithPIPKey();
			mCommonIntegration.setDoPIPPOPAction(true);
			if ((ComponentsManager.getNativeActiveCompId() & NAV_NATIVE_COMP_ID_BASIC) != 0) {
				// key already be passed to linux world
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
						"isKeyHandler, key already be passed to linux world(ginga)");
			} else {
//				bindSubSource = true;
				TurnkeyUiMainActivity.getInstance().getPipView()
						.setVisibility(View.INVISIBLE);
				mDispatch.passKeyToNative(keyCode, null);
				// mViewControl.mainAndSubViewReset();
				if (IntegrationZoom.ZOOM_1 != mIntegrationZoom.getCurrentZoom()) {
					mIntegrationZoom.setZoomModeToNormal();
				}
				mCommonIntegration.setStopTIFSetupWizardFunction(true);
				// rebindMainAndSubInputSource(PIPPOPConstant.TV_PIP_STATE);
				// updateFocusLabelPosition();
				// setVisibility(View.VISIBLE);
			}
			ComponentStatusListener.getInstance().updateStatus(
					ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
			return true;
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler keyCode = " + keyCode);
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyMap.KEYCODE_MTKIR_PIPPOP:
			if (mCommonIntegration.isPipOrPopState()) {
				int nextTVState = 0;
				if (reShowFocus()) {
					return true;
				}
				hideComponentsWithPIPKey();
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
						"come in TVStateControl onKeyHandler passKeyToNative KEYCODE_MTKIR_PIPPOP");
				if (PipPopConstant.TV_PIP_STATE == mCommonIntegration
						.getCurrentTVState()) {
					nextTVState = PipPopConstant.TV_POP_STATE;
				} else if (PipPopConstant.TV_POP_STATE == mCommonIntegration
						.getCurrentTVState()) {
					nextTVState = PipPopConstant.TV_NORMAL_STATE;
					currentFocusWin = mCommonIntegration.getCurrentFocus();
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"currentFocusWin:"+currentFocusWin);
//					focusSourceName = mSourceManager
//							.getCurrentInputSourceName(currentFocusWin);
				}
				mCommonIntegration.setDoPIPPOPAction(true);
				mDispatch.passKeyToNative(keyCode, event);
				//mViewControl.changeOutputWithTVState(nextTVState);
				if (PipPopConstant.TV_NORMAL_STATE == nextTVState) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(
							TAG,
							"PIPPOPConstant.TV_NORMAL_STATE,mCommonIntegration.setStopTIFSetupWizardFunction(false);");
					mCommonIntegration.setStopTIFSetupWizardFunction(false);
					//mSourceManager.stopPipSession();
				}
				//rebindMainAndSubInputSource(nextTVState);
				// if (!(nextTVState == PIPPOPConstant.TV_NORMAL_STATE)) {
				// updateFocusLabelPosition();
				// } else {
				// setVisibility(View.GONE);
				// }

			}
			ComponentStatusListener.getInstance().updateStatus(
					ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
			return true;
		case KeyMap.KEYCODE_DPAD_LEFT:
		case KeyMap.KEYCODE_DPAD_RIGHT:
			if (mCommonIntegration.isPipOrPopState()) {
				if (notSupportChangeFocus()) {
					return true;
				}
				if (reShowFocus()) {
					return true;
				}
				hideComponentsWithLeftKey();
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
						"come in TVStateControl onKeyHandler passKeyToNative,keyCode ="
							+ keyCode);
				// Freeze off
				if (SundryImplement.getInstanceNavSundryImplement(mContext)
						.isFreeze()) {
					SundryImplement.getInstanceNavSundryImplement(mContext)
							.setFreeze(false);
				}
				mDispatch.passKeyToNative(keyCode, event);
				// if (CommonIntegration.TV_FOCUS_WIN_MAIN == mCommonIntegration
				// .getCurrentFocus()) {
				// mCommonIntegration
				// .setCurrentFocus(CommonIntegration.TV_FOCUS_WIN_SUB);
				// } else {
				// mCommonIntegration
				// .setCurrentFocus(CommonIntegration.TV_FOCUS_WIN_MAIN);
				// }
				// updateFocusLabelPosition();
				ComponentStatusListener.getInstance().updateStatus(
						ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
			}
			return true;
		case KeyMap.KEYCODE_MTKIR_MTKIR_SWAP:
			if (mCommonIntegration.isPipOrPopState()) {
				if (reShowFocus()) {
					return true;
				}
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
						"come in TVStateControl onKeyHandler passKeyToNative"
								+ keyCode);
//				if (MarketRegionInfo
//						.isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT)) {
//					doSwapSource();
//				} else {
					//mDispatch.passKeyToNative(keyCode, event);
					mDispatch.passKeyToNative(SWAP_KEY_VALUE, event);
//				}
				ComponentStatusListener.getInstance().updateStatus(
						ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
			}
			return true;
		case KeyMap.KEYCODE_MTKIR_PIPPOS:
		case KeyMap.KEYCODE_MTKIR_PIPSIZE:
			if (mCommonIntegration.isPipOrPopState()) {
				if (reShowFocus() || !mCommonIntegration.isPIPState()) {
					return true;
				}
				hideComponentsWithSizeAndPosition();
				if (mSundryImplement.isFreeze()
						&& keyCode == KeyMap.KEYCODE_MTKIR_PIPSIZE) {
					mSundryImplement.setFreeze(false);
				}
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
						"come in TVStateControl onKeyHandler passKeyToNative"
								+ keyCode);
				if (MarketRegionInfo
						.isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT)) {
					if (KeyMap.KEYCODE_MTKIR_PIPPOS == keyCode) {
						mViewControl.changeSubOutputPosition();
						//mMtkTvPipPop.popNextPipWindowPosition();
						//mDispatch.passKeyToNative(PIPPOS_KEY_VALUE, event);
					} else {
						mViewControl.changeSubOutputSize();
						//mDispatch.passKeyToNative(PIPSIZE_KYE_VALUE, event);
					}
					if (CommonIntegration.TV_FOCUS_WIN_SUB
							.equals(mCommonIntegration.getCurrentFocus())) {
						updateFocusLabelPosition();
					}
				}else{
					mDispatch.passKeyToNative(keyCode, event);
				}
			}
			return true;
		default:
			if (keyCode != KeyMap.KEYCODE_POWER) {
				if (mCommonIntegration.isPipOrPopState()) {
					reShowFocus();
				}
			}
			break;
		}
		return false;
	}

	@Override
	public boolean deinitView() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVisible() {
		// TODO Auto-generated method stub
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
				"come in TVStateControl isVisible mCommonIntegration.isPipOrPopState() "
						+ mCommonIntegration.isPipOrPopState());
		return mCommonIntegration.isPipOrPopState();
	}

	private void hideComponentsWithPIPKey() {
		if (mChannelListDialog.isShowing()) {
			mChannelListDialog.dismiss();
		}

		if (mFavoriteChannelListView.isShowing()) {
			mFavoriteChannelListView.dismiss();
		}

		if (mShowTextView.isVisible()) {
			mShowTextView.setVisibility(View.GONE);
		}

		TwinkleDialog.hideTwinkle();

		if (mZoomTipView.isVisible()) {
			mZoomTipView.setVisibility(View.GONE);
		}

		if (mMiscView.isVisible()) {
			mMiscView.setVisibility(View.GONE);
		}
	}

	private void hideComponentsWithLeftKey() {
		TwinkleDialog.hideTwinkle();

		if (mShowTextView.isVisible()) {
			mShowTextView.setVisibility(View.GONE);
		}

		if (mMiscView.isVisible()) {
			mMiscView.setVisibility(View.GONE);
		}
	}

	private void hideComponentsWithSizeAndPosition() {
		if (mShowTextView.isVisible()) {
			mShowTextView.setVisibility(View.GONE);
		}

		if (mChannelListDialog.isShowing()) {
			mChannelListDialog.dismiss();
		}

		if (mMiscView.isVisible()) {
			mMiscView.setVisibility(View.GONE);
		}
	}

	private boolean notSupportChangeFocus() {
		return mChannelListDialog.isShowing()
        || mFavoriteChannelListView.isShowing()
        || mBannerView.isChangingChannelWithNum();
	}

	public boolean reShowFocus() {

		if (null != mFocusLabel && mFocusLabel.getVisibility() != View.VISIBLE) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in need to reShowFocus");
			mComponentsManager.showNavComponent(NAV_COMP_ID_POP);
			return true;
		}
		return false;
	}

	private void updateFocusLabelPosition() {
		if (CommonIntegration.TV_FOCUS_WIN_MAIN.equals(mCommonIntegration
				.getCurrentFocus())) {
			mFocusLabel.setPadding(mViewControl.getMainPosition()[0],
					mViewControl.getMainPosition()[1]);
		} else {
			mFocusLabel.setPadding(mViewControl.getSubPosition()[0],
					mViewControl.getSubPosition()[1]);
		}
	}

	@Override
	public void updateComponentStatus(int statusID, int value) {
		switch (statusID) {
		case ComponentStatusListener.NAV_RESUME:
			break;
		case ComponentStatusListener.NAV_ENTER_STANDBY:
		case ComponentStatusListener.NAV_ENTER_MMP:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
					"come in TVStateControl updateComponentStatus,statusID="
							+ statusID);
			setVisibility(View.INVISIBLE);
			break;
		default:
			break;
		}
	}

	public void setFocusLable(FocusLabel focusLabel) {
		mFocusLabel = focusLabel;
	}

//	private void rebindMainAndSubInputSource(int currentState) {
//		mainSourceName = mSourceManager
//				.getCurrentInputSourceName(InputSourceManager.MAIN);
//		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " mainSourceName=" + mainSourceName);
//		subSourceName = mSourceManager
//				.getCurrentInputSourceName(InputSourceManager.SUB);
//		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " subSourceName=" + subSourceName);
//		if(!TextUtils.isEmpty(mainSourceName)) {
//		    if (subSourceName.length() == 0) {
//		        subSourceName = MtkTvInputSource.getInstance()
//		                .getCurrentInputSourceName(InputSourceManager.SUB);
//		        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " subSourceName=" + subSourceName);
//		    }
//		    if (PIPPOPConstant.TV_NORMAL_STATE == currentState) {
//		        // mSourceManager.changeCurrentInputSourceByName(mainSourceName,
//		        // InputSourceManager.SUB);
//		        if (CommonIntegration.TV_FOCUS_WIN_SUB.equals(currentFocusWin)) {
//		            mSourceManager.changeCurrentInputSourceByName(focusSourceName,
//		                    InputSourceManager.MAIN);
//		        }else{
//		            mSourceManager.changeCurrentInputSourceByName(mainSourceName,
//		                    InputSourceManager.MAIN);
//		        }
//		        mSourceManager.stopPipSession();
//		    } else if(!TextUtils.isEmpty(subSourceName)){
//		        mSourceManager.changeCurrentInputSourceByName(mainSourceName,
//		                InputSourceManager.MAIN);
//		        if (!mainSourceName.equals(subSourceName)) {
//		            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "!mainSourceName.equals(subSourceName)");
//		            mSourceManager.changeCurrentInputSourceByName(subSourceName,
//		                    InputSourceManager.SUB);
//		        } else {
//		            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mainSourceName.equals(subSourceName)");
//		        }
//		    }
//		}
//		mCommonIntegration.setDoPIPPOPAction(false);
//	}

//	private void doSwapSource() {
//		String currentMainSourceName = mSourceManager
//				.getCurrentInputSourceName(InputSourceManager.MAIN);
//		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " currentMainSourceName=" + currentMainSourceName);
//		String currentSubSourceName = mSourceManager
//				.getCurrentInputSourceName(InputSourceManager.SUB);
//		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " currentSubSourceName=" + currentSubSourceName);
//		if(!TextUtils.isEmpty(currentMainSourceName)) {
//		    if (currentSubSourceName.length() == 0) {
//		        currentSubSourceName = MtkTvInputSource.getInstance()
//		                .getCurrentInputSourceName(InputSourceManager.SUB);
//		        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
//		                "currentSubSourceName.length() == 0,currentSubSourceName="
//		                        + currentSubSourceName);
//		    }
//		    if (!TextUtils.isEmpty(currentSubSourceName) && !currentMainSourceName.equals(currentSubSourceName)) {
//		        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "!currentMainSourceName.equals(currentSubSourceName)");
//		        mSourceManager.changeCurrentInputSourceByName(
//		                currentMainSourceName, InputSourceManager.SUB);
//		        mSourceManager.changeCurrentInputSourceByName(currentSubSourceName,
//		                InputSourceManager.MAIN);
//		    } else {
//		        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentMainSourceName.equals(currentSubSourceName)");
//		    }
//		}
//	}
}
