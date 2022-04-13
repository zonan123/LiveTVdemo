
package com.mediatek.wwtv.tvcenter.nav.util;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mediatek.twoworlds.tv.MtkTvMultiView;
import com.mediatek.wwtv.tvcenter.commonview.TvSurfaceView;
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

import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;

public class MultiViewControl extends NavBasicMisc implements
    ComponentStatusListener.ICStatusListener {

    private final CommonIntegration mCommonIntegration;

    private static final String TAG = "MultiViewControl";

    private final KeyDispatch mDispatch;

    private final ComponentsManager mComponentsManager;

  private ChannelListDialog mChannelListDialog;
    private FavoriteListDialog mFavoriteChannelListView;
  private SundryShowTextView mShowTextView;
  private BannerView mBannerView;
  private ZoomTipView mZoomTipView;
  private MiscView mMiscView;
//    private final IntegrationZoom mIntegrationZoom;

    private final SundryImplement mSundryImplement;

    private final TvCallbackHandler mTvCallbackHandler;

    private final InputSourceManager mSourceManager;

    private final MtkTvMultiView mMtkTvMultiView;
  private final PIPPOPSurfaceViewControl mViewControl;
  private FocusLabel mFocusLabel;

//  private static final int ENTER_PIP_STATE = 0;
//  private static final int ENTER_POP_STATE = 1;
//  private static final int ENTER_NORMAL_STATE = 2;
//  private static final int SWAP_CHANEGE = 3;
//  private static final int AUDIO_CHANGE = 4;
//  private static final int FOCUS_CHANGE = 5;

//  private static final int SWAP_KEY_VALUE = 315;
//  private static final int PIPPOS_KEY_VALUE = 311;
//  private static final int PIPSIZE_KYE_VALUE = 312;

  private String currentFocusWin = CommonIntegration.TV_FOCUS_WIN_MAIN;
  boolean isLastChangeFocusComplete = true;
//  private int focusIconMarginX;
//  private int focusIconMarginY;

  private String mainSourceName = "";
  private String subSourceName = "";
  private String focusSourceName = "";

    private final Handler focusLabelChangeHandler = new Handler(
      mContext.getMainLooper()) {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case TvCallbackConst.MSG_CB_CONFIG:
          TvCallbackData data = (TvCallbackData) msg.obj;
          // 1--sub source change 0--main source change
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
              "come in MultiViewControl,TvCallbackConst.MSG_CB_CONFIG,"
                  + "mainSourceName =" + mainSourceName
                  + ", subSourceName =" + subSourceName);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
              "come in MultiViewControl,TvCallbackConst.MSG_CB_CONFIG,"
                  + "CFGdata.param1 =" + data.param1
                  + ", CFGdata.param2 =" + data.param2);
          // if ((MtkTvConfigMsgBase.ACFG_MSG_PRE_CHG_INPUT ==
          // CFGdata.param1)
          // && (CFGdata.param2 == 1)) {
          // if (bindSubSource) {
          // String sourceName = mSourceManager
          // .getTVApiSubSourceName();
          // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
          // "come in TVStateControl,TvCallbackConst.MSG_CB_CONFIG"
          // + sourceName);
          // if (!mainSourceName.equals(sourceName)) {
          // mSourceManager.changeCurrentInputSourceByName(
          // sourceName, InputSourceManager.SUB);
          // } else {
          // subSourceName = sourceName;
          // }
          // bindSubSource = false;
          // } else {
          // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
          // "subsource bind success,setVisibility(View.VISIBLE)");
          // TurnkeyUiMainActivity.getInstance().getPipView()
          // .setVisibility(View.VISIBLE);
          // }
          //
          // }
          break;
        default:
          break;
      }
    };
  };

  public void setOutputView(TvSurfaceView mainOutput,
      TvSurfaceView subOutput, LinearLayout mainLY, LinearLayout subLY) {
    mViewControl.setSignalOutputView(mainOutput, subOutput, mainLY, subLY);
  }

  public MultiViewControl(Context mContext) {
    super(mContext);
    mViewControl = PIPPOPSurfaceViewControl.getSurfaceViewControlInstance();
    componentID = NAV_COMP_ID_POP;
    mDispatch = KeyDispatch.getInstance();
    mCommonIntegration = CommonIntegration.getInstance();
    mComponentsManager = ComponentsManager.getInstance();
//    mIntegrationZoom = IntegrationZoom.getInstance(mContext);
    mSundryImplement = SundryImplement
        .getInstanceNavSundryImplement(mContext);
    mSourceManager = InputSourceManager.getInstance();
    mMtkTvMultiView = MtkTvMultiView.getInstance();
    mTvCallbackHandler = TvCallbackHandler.getInstance();
    mTvCallbackHandler.addCallBackListener(focusLabelChangeHandler);
    initComponentsView();

    // Add nav resume Listener
    ComponentStatusListener.getInstance().addListener(
        ComponentStatusListener.NAV_RESUME, this);
    ComponentStatusListener.getInstance().addListener(
        ComponentStatusListener.NAV_ENTER_STANDBY, this);
    ComponentStatusListener.getInstance().addListener(
        ComponentStatusListener.NAV_ENTER_MMP, this);
    ComponentStatusListener.getInstance().addListener(
            ComponentStatusListener.NAV_KEY_OCCUR, this);
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
      com.mediatek.wwtv.tvcenter.util.MtkLog
          .d(
              TAG,
              "come in MultiViewControl ComponentStatusListener.NAV_ENTER_STANDBY "
                  + "setVisibility,mFocusLabel.getVisibility()="
                  + mFocusLabel.getVisibility());
      if (View.VISIBLE != mFocusLabel.getVisibility()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(
            TAG,
            "come in MultiViewControl ComponentStatusListener.NAV_ENTER_STANDBY "
                + "mFocusLabel.show() =="
                + mCommonIntegration.isPipOrPopState());
        if (mCommonIntegration.isPipOrPopState()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
              "come in MultiViewControl setVisibility mFocusLabel.show()");
          mFocusLabel.show();
        }
      }
    } else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(
          TAG,
          "come in MultiViewControl ComponentStatusListener.NAV_ENTER_STANDBY setVisibility="
              + mFocusLabel.getVisibility());
      if (View.VISIBLE == mFocusLabel.getVisibility()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog
            .d(
                TAG,
                "come in MultiViewControl "
                    + "ComponentStatusListener.NAV_ENTER_STANDBY mFocusLabel.release()");
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
    return false;
  }

  public void setModeToPIP() {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subTvViewVisible");
    TurnkeyUiMainActivity.getInstance().getPipView().setVisibility(View.VISIBLE);
    mMtkTvMultiView.setChgSource(false);
    mCommonIntegration.recordCurrentTvState(PipPopConstant.TV_PIP_STATE);
    mCommonIntegration.setStopTIFSetupWizardFunction(true);
    mMtkTvMultiView.setNewTvMode(PipPopConstant.TV_PIP_STATE);
    mViewControl.changeOutputWithTVState(PipPopConstant.TV_PIP_STATE);
    rebindMainAndSubInputSource(PipPopConstant.TV_PIP_STATE);
    updateFocusLabelPosition();
    TurnkeyUiMainActivity.getInstance().getBlockScreenView().setVisibility(View.INVISIBLE);
    setVisibility(View.VISIBLE);
  }

  @Override
  public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler keyCode = " + keyCode);
    Log.d(TAG, "onKeyHandler keyCode = " + keyCode);
    if (event==null || event.getRepeatCount()<1) {
        switch (keyCode) {
            case KeyMap.KEYCODE_MTKIR_PIPPOP:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                        TAG,
                        "is3rdTVSource(): " + mCommonIntegration.is3rdTVSource() +
                                ", isPipOrPopState(): " + mCommonIntegration.isPipOrPopState());
                if (mCommonIntegration.is3rdTVSource()) {
                    Toast.makeText(mContext,"Don't support Third Party Source in PIP/POP Mode!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentTVState(): " + mCommonIntegration.getCurrentTVState());
              if (mCommonIntegration.isPipOrPopState()) {
                if (reShowFocus()) {
                  return true;
                }
                int nextTVState = 0;
                hideComponentsWithPIPKey();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                    "come in MultiViewControl onKeyHandler passKeyToNative KEYCODE_MTKIR_PIPPOP");
                if (PipPopConstant.TV_PIP_STATE == mCommonIntegration
                    .getCurrentTVState()) {
                  nextTVState = PipPopConstant.TV_POP_STATE;
                } else if (PipPopConstant.TV_POP_STATE == mCommonIntegration
                    .getCurrentTVState()) {

                  nextTVState = PipPopConstant.TV_NORMAL_STATE;
                  // currentFocusWin = mCommonIntegration.getCurrentFocus();
                  focusSourceName = mSourceManager
                      .getCurrentInputSourceName(currentFocusWin);
                  TurnkeyUiMainActivity.getInstance().getTvView().reset();
                  TurnkeyUiMainActivity.getInstance().getPipView().reset();
                  String focus = mCommonIntegration.getCurrentFocus();
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "If focus is sub, need get the screen mode from api. focus: " + focus);
//                  if (null != mViewControl && CommonIntegration.TV_FOCUS_WIN_SUB.equals(focus)) {
//                    mViewControl.setScreenModeChangedFlag(true);
//                  }
                }
                mCommonIntegration.setDoPIPPOPAction(true);
                          mMtkTvMultiView.setChgSource(false);
                mMtkTvMultiView.setNewTvMode(nextTVState);
                mViewControl.changeOutputWithTVState(nextTVState);
                mCommonIntegration.recordCurrentTvState(nextTVState);
                if (PipPopConstant.TV_NORMAL_STATE == nextTVState) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog
                      .d(
                          TAG,
                          "PIPPOPConstant.TV_NORMAL_STATE,"
                              + "mCommonIntegration.setStopTIFSetupWizardFunction(false);");
                  mCommonIntegration
                      .setCurrentFocus(CommonIntegration.TV_FOCUS_WIN_MAIN);
                  mCommonIntegration.setStopTIFSetupWizardFunction(false);
                  // mSourceManager.stopPipSession();
                }
                rebindMainAndSubInputSource(nextTVState);
                if (nextTVState != PipPopConstant.TV_NORMAL_STATE) {
                  updateFocusLabelPosition();
                  Log.d(TAG, "Enter_POP_OK");
                } else {
                  setVisibility(View.GONE);
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "PipView Gone");
                  TurnkeyUiMainActivity.getInstance().getPipView().setVisibility(View.GONE);
                  Log.d(TAG, "Enter_Normal_OK");
                }

              }
              ComponentStatusListener.getInstance().updateStatus(
                  ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
              return true;
            case KeyMap.KEYCODE_DPAD_LEFT:
            case KeyMap.KEYCODE_DPAD_RIGHT:
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isLastChangeFocusComplete ="+isLastChangeFocusComplete);
              if (isLastChangeFocusComplete) {
                  isLastChangeFocusComplete = false;
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in MultiViewControl onKeyHandler,keyCode ="
                          + keyCode);
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                          "come in MultiViewControl onKeyHandler,mCommonIntegration.isPipOrPopState() ="
                              + mCommonIntegration.isPipOrPopState());
                      if (mCommonIntegration.isPipOrPopState()) {
                        if (notSupportChangeFocus()) {
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                              "come in MultiViewControl onKeyHandler notSupportChangeFocus()");
                          isLastChangeFocusComplete = true;
                          return true;
                        }
                        if (reShowFocus()) {
                          isLastChangeFocusComplete = true;
                          return true;
                        }
                        hideComponentsWithLeftKey();

                        // Freeze off
                        if (SundryImplement.getInstanceNavSundryImplement(mContext)
                            .isFreeze()) {
                          SundryImplement.getInstanceNavSundryImplement(mContext)
                              .setFreeze(false);
                        }
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d("yiqinghuang","mCommonIntegration.getCurrentFocus()"+mCommonIntegration
                                .getCurrentFocus());
                        if (CommonIntegration.TV_FOCUS_WIN_MAIN == mCommonIntegration
                            .getCurrentFocus()) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d("yiqinghuang", "setCurrentFocusSub");
                          mCommonIntegration
                              .setCurrentFocus(CommonIntegration.TV_FOCUS_WIN_SUB);
//                          mMtkTvMultiView
//                              .setAudioFocus(CommonIntegration.TV_FOCUS_WIN_SUB);
                          TurnkeyUiMainActivity.getInstance().getTvView().setStreamVolume(0.0f);
                          TurnkeyUiMainActivity.getInstance().getPipView().setStreamVolume(1.0f);
                        } else {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d("yiqinghuang", "setCurrentFocusMain");
                          mCommonIntegration
                              .setCurrentFocus(CommonIntegration.TV_FOCUS_WIN_MAIN);
//                          mMtkTvMultiView
//                              .setAudioFocus(CommonIntegration.TV_FOCUS_WIN_MAIN);
                          TurnkeyUiMainActivity.getInstance().getPipView().setStreamVolume(0.0f);
                          TurnkeyUiMainActivity.getInstance().getTvView().setStreamVolume(1.0f);
                        }
                        updateFocusLabelPosition();
                        currentFocusWin = mCommonIntegration.getCurrentFocus();
                        ComponentStatusListener.getInstance().updateStatus(
                            ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
                        isLastChangeFocusComplete = true;
                      }
                      return true;
            }else {
                return true;
            }

            case KeyMap.KEYCODE_MTKIR_MTKIR_SWAP:
              if (mCommonIntegration.isPipOrPopState()) {
                if (reShowFocus()) {
                  return true;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                    "come in MultiViewControl onKeyHandler passKeyToNative"
                        + keyCode);
                /*
                 * if (MarketRegionInfo .isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT)) {
                 * doSwapSource(); } ComponentStatusListener.getInstance().updateStatus(
                 * ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
                 */
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
                    Log.d(TAG, "PIP_POS_OK");
                  } else {
                    mViewControl.changeSubOutputSize();
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                      mMtkTvMultiView.setNewTvMode(PipPopConstant.TV_PIP_STATE);
                      mSourceManager.stopPipSession(false);
                      mSourceManager
                          .changeCurrentInputSourceByName(
                              mSourceManager
                                  .getCurrentInputSourceName(InputSourceManager.SUB),
                              InputSourceManager.SUB);
                  }
                    Log.d(TAG, "PIP_SIZE_OK");
                  }
                  if (CommonIntegration.TV_FOCUS_WIN_SUB
                      .equals(mCommonIntegration.getCurrentFocus())) {
                    updateFocusLabelPosition();
                  }
                } else {
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
    }else {
        return true;
    }

    return false;
  }

  public void setCurrentFocusWin(String currentFocusWin) {
    this.currentFocusWin = currentFocusWin;
  }

  @Override
  public boolean deinitView() {
    // TODO Auto-generated method stub
    mTvCallbackHandler.removeCallBackListener(focusLabelChangeHandler);
    return false;
  }

  @Override
  public boolean isVisible() {
    // TODO Auto-generated method stub
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
        "come in MultiViewControl isVisible mCommonIntegration.isPipOrPopState() "
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
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("yiqinghuang", "mCommonIntegration.getCurrentFocus()"+mCommonIntegration
        .getCurrentFocus());
    if (CommonIntegration.TV_FOCUS_WIN_MAIN.equals(mCommonIntegration
        .getCurrentFocus())) {
      mFocusLabel.setPadding(mViewControl.getMainPosition()[0],
          mViewControl.getMainPosition()[1]);
    } else {
      mFocusLabel.setPadding(mViewControl.getSubPosition()[0],
          mViewControl.getSubPosition()[1]);
    }
    mFocusLabel.show();
  }

  @Override
  public void updateComponentStatus(int statusID, int value) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "statesID:" + statusID + ", value: " + value);
//    switch (statusID) {
//      case ComponentStatusListener.NAV_RESUME:
//        break;
//      case ComponentStatusListener.NAV_ENTER_STANDBY:
//          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
//                  "come in MultiViewControl updateComponentStatus,statusID="
//                      + statusID);
//          if (mFocusLabel!=null) {
//              setVisibility(View.INVISIBLE);
//          }
//              break;
//      case ComponentStatusListener.NAV_ENTER_MMP:
//        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
//            "come in MultiViewControl updateComponentStatus,statusID="
//                + statusID);
//        setVisibility(View.INVISIBLE);
//        break;
////      case ComponentStatusListener.NAV_KEY_OCCUR:
////          switch (value) {
////            case KeyMap.KEYCODE_MTKIR_ASPECT:
////              if (mCommonIntegration.isPipOrPopState()) {
////                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isPOPState");
//////                mViewControl.setScreenModeChangedFlag(true);
////              }
////          }
//      default:
//        break;
//    }
  }

  public void setFocusLable(FocusLabel focusLabel) {
    mFocusLabel = focusLabel;
  }

  private void rebindMainAndSubInputSource(int currentState) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " currentState=" + currentState);
    /*
    mainSourceName = mSourceManager
        .getCurrentInputSourceName(InputSourceManager.MAIN);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " mainSourceName=" + mainSourceName);
    subSourceName = mSourceManager
        .getCurrentInputSourceName(InputSourceManager.SUB);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " subSourceName=" + subSourceName);

    if ((null == subSourceName) || subSourceName.equals(mainSourceName)
        || (subSourceName.length() == 0) || subSourceName.equals("0")
        || mSourceManager.isConflicted(mainSourceName, subSourceName)
        || !(mSourceManager.isSourceEnable(subSourceName))) {
      subSourceName = mSourceManager
          .querySourceNameWithAnother(mainSourceName);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " querySourceNameWithAnother,subSourceName="
          + subSourceName);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "rebindMainAndSubInputSource,currentState =="
        + currentState);
    if (PIPPOPConstant.TV_NORMAL_STATE == currentState) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
          "rebindMainAndSubInputSource,PIPPOPConstant.TV_NORMAL_STATE");
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "rebindMainAndSubInputSource,currentFocusWin =="
          + currentFocusWin);
      if (CommonIntegration.TV_FOCUS_WIN_SUB.equals(currentFocusWin)) {
        mSourceManager.changeCurrentInputSourceByName(focusSourceName,
            InputSourceManager.MAIN);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeCurrentInputSourceByNameMain"+focusSourceName);
//         mSourceManager.changeCurrentInputSourceByName(mainSourceName,
//         InputSourceManager.SUB);
        mSourceManager.saveOutputSourceName(mainSourceName,
            InputSourceManager.SUB);
      } else {
        mSourceManager.changeCurrentInputSourceByName(mainSourceName,
            InputSourceManager.MAIN);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeCurrentInputSourceByNameMain"+mainSourceName);
      }
      // mSourceManager.stopPipSession();
      currentFocusWin = CommonIntegration.TV_FOCUS_WIN_MAIN;
//      mMtkTvMultiView.setAudioFocus(CommonIntegration.TV_FOCUS_WIN_MAIN);
    } else if (PIPPOPConstant.TV_PIP_STATE == currentState) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
          "rebindMainAndSubInputSource,PIPPOPConstant.TV_PIP_STATE");
      mSourceManager.stopSession(false);
      mSourceManager.changeCurrentInputSourceByName(mainSourceName,
          InputSourceManager.MAIN);

      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "!mainSourceName.equals(subSourceName)");
      mSourceManager.changeCurrentInputSourceByName(subSourceName,
          InputSourceManager.SUB);
    } else {
      mSourceManager.stopSession(false);
      mSourceManager.stopPipSession(false);
      mSourceManager.changeCurrentInputSourceByName(mainSourceName,
          InputSourceManager.MAIN);
      mSourceManager.changeCurrentInputSourceByName(subSourceName,
          InputSourceManager.SUB);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
          "rebindMainAndSubInputSource,PIPPOPConstant.TV_POP_STATE");
    }
    mCommonIntegration.setDoPIPPOPAction(false);
    */
  }

//  private void doSwapSource() {
//    String currentMainSourceName = mSourceManager
//        .getCurrentInputSourceName(InputSourceManager.MAIN);
//    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " currentMainSourceName=" + currentMainSourceName);
//    String currentSubSourceName = mSourceManager
//        .getCurrentInputSourceName(InputSourceManager.SUB);
//    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " currentSubSourceName=" + currentSubSourceName);
//    if (!currentMainSourceName.equals(currentSubSourceName)) {
//      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "!currentMainSourceName.equals(currentSubSourceName)");
//      mSourceManager.changeCurrentInputSourceByName(
//          currentMainSourceName, InputSourceManager.SUB);
//      mSourceManager.changeCurrentInputSourceByName(currentSubSourceName,
//          InputSourceManager.MAIN);
//    } else {
//      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentMainSourceName.equals(currentSubSourceName)");
//    }
//  }

  public void setNormalTvModeWithGooglePiP(){
      mainSourceName = mSourceManager
              .getCurrentInputSourceName(InputSourceManager.MAIN);
      subSourceName = mSourceManager
              .getCurrentInputSourceName(InputSourceManager.SUB);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in setNormalTvModeWithEas,currentFocusWin=="
              + currentFocusWin + ", subSourceName ==" + subSourceName
              + ", mainSourceName == " + mainSourceName);
      mCommonIntegration.setDoPIPPOPAction(true);
      mMtkTvMultiView.setChgSource(false);
      mCommonIntegration.setStopTIFSetupWizardFunction(false);
      mMtkTvMultiView.setNewTvMode(PipPopConstant.TV_NORMAL_STATE);
      mViewControl.changeOutputWithTVState(PipPopConstant.TV_NORMAL_STATE);
      mCommonIntegration.recordCurrentTvState(PipPopConstant.TV_NORMAL_STATE);
      mCommonIntegration.setCurrentFocus(CommonIntegration.TV_FOCUS_WIN_MAIN);
//      mMtkTvMultiView.setAudioFocus(CommonIntegration.TV_FOCUS_WIN_MAIN);
      rebindMainAndSubInputSource(PipPopConstant.TV_NORMAL_STATE);
//      TurnkeyUiMainActivity.getInstance().getTvView().reset();
      TurnkeyUiMainActivity.getInstance().getPipView().reset();
      TurnkeyUiMainActivity.getInstance().getPipView().setVisibility(View.GONE);
  }
  public void setNormalTvModeWithEas() {
        mainSourceName = mSourceManager
                .getCurrentInputSourceName(InputSourceManager.MAIN);
        subSourceName = mSourceManager
                .getCurrentInputSourceName(InputSourceManager.SUB);
        // focusSourceName = mSourceManager
        // .getCurrentInputSourceName(currentFocusWin);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in setNormalTvModeWithEas,currentFocusWin=="
                + currentFocusWin + ", subSourceName ==" + subSourceName
                + ", mainSourceName == " + mainSourceName);
//         TurnkeyUiMainActivity.getInstance().getTvView().reset();
         TurnkeyUiMainActivity.getInstance().getPipView().reset();
         mMtkTvMultiView.setNewTvMode(PipPopConstant.TV_NORMAL_STATE);
        mViewControl.changeOutputWithTVState(PipPopConstant.TV_NORMAL_STATE);
        mCommonIntegration.recordCurrentTvState(PipPopConstant.TV_NORMAL_STATE);
        mCommonIntegration.setCurrentFocus(CommonIntegration.TV_FOCUS_WIN_MAIN);
        TurnkeyUiMainActivity.getInstance().getPipView().setVisibility(View.GONE);
        // mSourceManager.changeCurrentInputSourceByName("TV", InputSourceManager.MAIN);
        if ("TV".equals(subSourceName)) {
            // mSourceManager.changeCurrentInputSourceByName(focusSourceName,
            // InputSourceManager.MAIN);
            // mSourceManager.changeCurrentInputSourceByName(mainSourceName,
            // InputSourceManager.SUB);
            mSourceManager.saveOutputSourceName("TV", InputSourceManager.MAIN);
            mSourceManager.saveOutputSourceName(mainSourceName,
                    InputSourceManager.SUB);

        }
  }

  public void setNormalTvModeWithMenuReset() {
    TurnkeyUiMainActivity.getInstance().getPipView().reset();
    mMtkTvMultiView.setNewTvMode(PipPopConstant.TV_NORMAL_STATE);
    mViewControl.changeOutputWithTVState(PipPopConstant.TV_NORMAL_STATE);
    mCommonIntegration.recordCurrentTvState(PipPopConstant.TV_NORMAL_STATE);
    mCommonIntegration.setCurrentFocus(CommonIntegration.TV_FOCUS_WIN_MAIN);
    mSourceManager.saveOutputSourceName("", InputSourceManager.SUB);
    TurnkeyUiMainActivity.getInstance().getPipView().setVisibility(View.GONE);

  }

    public void setNormalTvModeWithLauncher(boolean enterLauncher) {
    mainSourceName = mSourceManager
        .getCurrentInputSourceName(InputSourceManager.MAIN);
    focusSourceName = mSourceManager
        .getCurrentInputSourceName(currentFocusWin);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in setNormalTvModeWithLauncher,currentFocusWin=="
        + currentFocusWin + ", focusSourceName ==" + focusSourceName
        + ", mainSourceName == " + mainSourceName);
    TurnkeyUiMainActivity.getInstance().getTvView().reset();
    TurnkeyUiMainActivity.getInstance().getPipView().reset();
    TurnkeyUiMainActivity.getInstance().getPipView().setVisibility(View.GONE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mMtkTvMultiView.setNewTvMode(PipPopConstant.TV_NORMAL_STATE);

                mCommonIntegration.recordCurrentTvState(PipPopConstant.TV_NORMAL_STATE);
                mCommonIntegration.setCurrentFocus(CommonIntegration.TV_FOCUS_WIN_MAIN);
            }
        }).start();
    if (null != mViewControl && mCommonIntegration.isPIPState()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "need update the screen mode,so set the flag to be true!");
//      mViewControl.setScreenModeChangedFlag(true);
    }
        mViewControl.changeOutputWithTVState(PipPopConstant.TV_NORMAL_STATE);
    if (CommonIntegration.TV_FOCUS_WIN_SUB.equals(currentFocusWin)) {
            if (enterLauncher) {
                mSourceManager.changeCurrentInputSourceByName(focusSourceName,
                        InputSourceManager.MAIN);
            } else {
                mSourceManager.saveOutputSourceName(focusSourceName,
                        InputSourceManager.MAIN);
            }
      mSourceManager.saveOutputSourceName(mainSourceName,
          InputSourceManager.SUB);
    } else {
            if (enterLauncher) {
                mSourceManager.changeCurrentInputSourceByName(mainSourceName,
                        InputSourceManager.MAIN);
            }
        }
    currentFocusWin = CommonIntegration.TV_FOCUS_WIN_MAIN;
        new Thread(new Runnable() {
            @Override
            public void run() {
//                mMtkTvMultiView.setAudioFocus(CommonIntegration.TV_FOCUS_WIN_MAIN);

            }
        }).start();

        if (enterLauncher) {
            TurnkeyUiMainActivity.getInstance().getTvView().setStreamVolume(1.0f);
            // TurnkeyUiMainActivity.getInstance().getPipView().setStreamVolume(0.0f);
        }
    }

}
