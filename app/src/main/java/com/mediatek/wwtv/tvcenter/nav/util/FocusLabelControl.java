package com.mediatek.wwtv.tvcenter.nav.util;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
//import com.mediatek.twoworlds.tv.MtkTvPipPop;
//import com.mediatek.twoworlds.tv.model.MtkTvPipPopFucusInfoBase;
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


public class FocusLabelControl extends NavBasicMisc implements ComponentStatusListener.ICStatusListener{
    private static final String TAG = "FocusLabelControl";

    private FocusLabel mFocusLabel;

    private final KeyDispatch mDispatch;

    private final CommonIntegration mCommonIntegration;

    private final ComponentsManager mComponentsManager;

    private ChannelListDialog mChannelListDialog;
    private FavoriteListDialog mFavoriteChannelListView;
    private SundryShowTextView mShowTextView;
    private BannerView mBannerView;
    private ZoomTipView mZoomTipView;
    private MiscView mMiscView;
    private final IntegrationZoom mIntegrationZoom;

    private final SundryImplement mSundryImplement;

//    private final MtkTvPipPop mMtkTvPipPop;
//    private MtkTvPipPopFucusInfoBase mMtkTvPipPopFucusInfo;

//    private static final int ENTER_PIP_STATE = 0;
//    private static final int ENTER_POP_STATE = 1;
//    private static final int ENTER_NORMAL_STATE = 2;
//    private static final int AUDIO_CHANGE = 4;
//    private static final int FOCUS_CHANGE = 5;

    public FocusLabelControl(Context mContext) {
        super(mContext);
        componentID = NAV_COMP_ID_POP;
        mDispatch = KeyDispatch.getInstance();
        mCommonIntegration = CommonIntegration.getInstance();
        mComponentsManager = ComponentsManager.getInstance();
//        mMtkTvPipPop = MtkTvPipPop.getInstance();
        mIntegrationZoom = IntegrationZoom.getInstance(mContext);
        mSundryImplement = SundryImplement.getInstanceNavSundryImplement(mContext);
        initComponentsView();

        //Add nav resume Listener
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_RESUME, this);
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_ENTER_STANDBY, this);
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
		mZoomTipView = (ZoomTipView) mComponentsManager
				.getComponentById(NAV_COMP_ID_ZOOM_PAN);
		mMiscView = (MiscView) mComponentsManager
				.getComponentById(NAV_COMP_ID_MISC);
    }

    @Override
    public void setVisibility(int visibility) {
        // TODO Auto-generated method stub
        if (View.VISIBLE == visibility) {
            if (View.VISIBLE != mFocusLabel.getVisibility()) {
            	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in FocusLabelControl ComponentStatusListener.NAV_ENTER_STANDBY  mFocusLabel.show()");
            	if(mCommonIntegration.isPipOrPopState()){
            		mFocusLabel.show();
            	}
            }
        } else {
            if(View.VISIBLE == mFocusLabel.getVisibility()){
            	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in FocusLabelControl ComponentStatusListener.NAV_ENTER_STANDBY mFocusLabel.release()");
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
            if((ComponentsManager.getNativeActiveCompId() & NAV_NATIVE_COMP_ID_BASIC) != 0){
                //key already be passed to linux world
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
						"isKeyHandler, key already be passed to linux world(ginga)");
			} else {
				mDispatch.passKeyToNative(keyCode, null);
				if (IntegrationZoom.ZOOM_1 != mIntegrationZoom.getCurrentZoom()) {
					mIntegrationZoom.setZoomModeToNormal();
				}
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
                if (reShowFocus()) {
                    return true;
                }
                hideComponentsWithPIPKey();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "come in FocusLabelControl onKeyHandler passKeyToNative KEYCODE_MTKIR_PIPPOP");
                mDispatch.passKeyToNative(keyCode, event);
            }
            ComponentStatusListener.getInstance().updateStatus(
                    ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
            return true;
        case KeyMap.KEYCODE_DPAD_LEFT:
        case KeyMap.KEYCODE_DPAD_RIGHT:
            if (mCommonIntegration.isPipOrPopState()) {
                if(notSupportChangeFocus()){
                    return true;
                }
                if (reShowFocus()) {
                    return true;
                }
                hideComponentsWithLeftKey();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "come in FocusLabelControl onKeyHandler passKeyToNative KEYCODE_DPAD_LEFT");
                //Freeze off
                if(SundryImplement.getInstanceNavSundryImplement(mContext).isFreeze()){
                    SundryImplement.getInstanceNavSundryImplement(mContext).setFreeze(false);
                }
                mDispatch.passKeyToNative(keyCode, event);
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
                        "come in FocusLabelControl onKeyHandler passKeyToNative"
                                + keyCode);
                mDispatch.passKeyToNative(keyCode, event);
                ComponentStatusListener.getInstance().updateStatus(
                        ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
            }
            return true;
        case KeyMap.KEYCODE_MTKIR_PIPPOS:
        case KeyMap.KEYCODE_MTKIR_PIPSIZE:
            if (mCommonIntegration.isPipOrPopState()) {
                if (reShowFocus()) {
                    return true;
                }
                hideComponentsWithSizeAndPosition(keyCode);
                if(mSundryImplement.isFreeze() && (keyCode == KeyMap.KEYCODE_MTKIR_PIPSIZE)){
                	mSundryImplement.setFreeze(false);
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                        "come in FocusLabelControl onKeyHandler passKeyToNative"
                                + keyCode);
                mDispatch.passKeyToNative(keyCode, event);
            }
            return true;
        default:
        	if(keyCode != KeyMap.KEYCODE_POWER){
        		reShowFocus();
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
                "come in FocusLabelControl isVisible mCommonIntegration.isPipOrPopState() "
                        + mCommonIntegration.isPipOrPopState());
        return mCommonIntegration.isPipOrPopState();
//            return true;
//        }
//
//        return false;
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

    private void hideComponentsWithSizeAndPosition(int key){
      com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,"key:"+key);
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
        return (mChannelListDialog.isShowing()
                || mFavoriteChannelListView.isShowing()
                || mBannerView.isChangingChannelWithNum()) ;
    }

    public boolean reShowFocus(){
        if (null != mFocusLabel && mFocusLabel.getVisibility() != View.VISIBLE) {
            mComponentsManager.showNavComponent(NAV_COMP_ID_POP);
            return true;
        }
        return false;
    }

    @Override
    public void updateComponentStatus(int statusID, int value){
    	switch (statusID) {
		case ComponentStatusListener.NAV_RESUME:
			break;
		case ComponentStatusListener.NAV_ENTER_STANDBY:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in FocusLabelControl ComponentStatusListener.NAV_ENTER_STANDBY");
			setVisibility(View.INVISIBLE);
			break;
		default:
			break;
		}
    }

    public void setFocusLable(FocusLabel focusLabel){
    	mFocusLabel = focusLabel;
    }
}
