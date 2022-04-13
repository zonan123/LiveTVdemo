package com.mediatek.wwtv.tvcenter.nav;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.mediatek.tv.ui.MyToast;
import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvGinga;
import com.mediatek.twoworlds.tv.MtkTvMultiView;
import com.mediatek.twoworlds.tv.MtkTvRecord;
import com.mediatek.twoworlds.tv.MtkTvTimeshift;
import com.mediatek.twoworlds.tv.MtkTvUtil;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.widget.view.ScheduleListDialog;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.dvr.ui.DvrDialog;
import com.mediatek.wwtv.tvcenter.nav.util.BannerImplement;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.util.MultiViewControl;
import com.mediatek.wwtv.tvcenter.nav.util.TeletextImplement;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.TTXMain;
import com.mediatek.wwtv.tvcenter.nav.view.UKChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.oad.NavOADActivityNew;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeshiftView;
import com.mediatek.wwtv.tvcenter.util.AudioFocusManager;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.InstrumentationHandler;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.util.WakeLockHelper;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;

import java.util.List;

import mediatek.sysprop.VendorProperties;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeShiftManager;

public final class TurnkeyReceiver implements ComponentStatusListener.ICStatusListener {
  private static final String TAG = "TurnkeyReceiver";

  private static final String TIMESHIFT_MODE_OFF = "com.mediatek.timeshift.mode.off";
  public static final String LIVE_SETTING_SELECT_SOURCE = "com.mediatek.select.source";
  public static final String LIVE_SETTING_SELECT_SOURCE_NAME = "com.mediatek.select.sourcename";
  public static final String LIVE_SETTING_SELECT_TUNER_TYPE = "com.mediatek.select.tunertype";

  private TurnkeyUiMainActivity mActivity = null;

  public boolean mCanChangeChannel = true;
  public boolean mIsFromTK = false;
  public int mSendKeyCode;

  public TurnkeyReceiver(TurnkeyUiMainActivity activity) {
    mActivity = activity;
  }

  public Runnable mChannelUpRunnable =
      new Runnable() {

        @Override
        public void run() {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KeyMap.KEYCODE_MTKIR_CHUP>>>");
          if (mActivity.isUKCountry) {
            UKChannelListDialog mUKChListDialog =
                (UKChannelListDialog)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
            if (SaveValue.getInstance(mActivity).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TIMESHIFT_START KEYCODE_MTKIR_CHUP");
              if (mUKChListDialog != null) {
                mSendKeyCode = KeyMap.KEYCODE_MTKIR_CHUP;
                mUKChListDialog.showDialogForPVRAndTShift(
                    DvrDialog.TYPE_Confirm_From_ChannelList,
                    mSendKeyCode,
                    DvrDialog.TYPE_Timeshift,
                    -1);
              }
              mCanChangeChannel = true;
            } else {
              if (mUKChListDialog != null) {
                mUKChListDialog.channelUpDown(true);
              }
              mCanChangeChannel = true;
            }
          } else {
            if (SaveValue.getInstance(mActivity).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TIMESHIFT_START KEYCODE_MTKIR_CHUP");

              ChannelListDialog mChListDialog =
                  (ChannelListDialog)
                      ComponentsManager.getInstance()
                          .getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
              if (mChListDialog != null) {
                mSendKeyCode = KeyMap.KEYCODE_MTKIR_CHUP;
                mChListDialog.showDialogForPVRAndTShift(
                    DvrDialog.TYPE_Confirm_From_ChannelList,
                    mSendKeyCode,
                    DvrDialog.TYPE_Timeshift,
                    -1);
              }
              mCanChangeChannel = true;
            } else {
              boolean ret = mActivity.mCommonIntegration.channelUp();
              if (ret) {
                mIsFromTK = true;
                mSendKeyCode = KeyMap.KEYCODE_MTKIR_CHUP;
              } else {
                mCanChangeChannel = true;
              }
            }
          }
        }
      };

  public Runnable mChannelDownRunnable =
      new Runnable() {

        @Override
        public void run() {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KeyMap.KEYCODE_MTKIR_CHDN>>>");
          if (mActivity.isUKCountry) {
            UKChannelListDialog mUKChListDialog =
                (UKChannelListDialog)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
            if (SaveValue.getInstance(mActivity).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TIMESHIFT_START KEYCODE_MTKIR_CHDN");
              if (mUKChListDialog != null) {
                mSendKeyCode = KeyMap.KEYCODE_MTKIR_CHDN;
                mUKChListDialog.showDialogForPVRAndTShift(
                    DvrDialog.TYPE_Confirm_From_ChannelList,
                    mSendKeyCode,
                    DvrDialog.TYPE_Timeshift,
                    -1);
              }
              mCanChangeChannel = true;
            } else {
              if (mUKChListDialog != null) {
                mUKChListDialog.channelUpDown(false);
              }
              mCanChangeChannel = true;
            }
          } else {
            if (SaveValue.getInstance(mActivity).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TIMESHIFT_START KEYCODE_MTKIR_CHDN");
              ChannelListDialog mChListDialog =
                  (ChannelListDialog)
                      ComponentsManager.getInstance()
                          .getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
              if (mChListDialog != null) {
                mSendKeyCode = KeyMap.KEYCODE_MTKIR_CHDN;
                mChListDialog.showDialogForPVRAndTShift(
                    DvrDialog.TYPE_Confirm_From_ChannelList,
                    mSendKeyCode,
                    DvrDialog.TYPE_Timeshift,
                    -1);
              }
              mCanChangeChannel = true;
            } else {
              boolean ret = mActivity.mCommonIntegration.channelDown();
              if (ret) {
                mIsFromTK = true;
                mSendKeyCode = KeyMap.KEYCODE_MTKIR_CHDN;
              } else {
                mCanChangeChannel = true;
              }
            }
          }
        }
      };

  public Runnable mChannelPreRunnable =
      new Runnable() {

        @Override
        public void run() {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KeyMap.KEYCODE_MTKIR_PRECH>>>");
          if (SaveValue.getInstance(mActivity.getApplicationContext())
              .readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)) {
            if (mActivity.isUKCountry) {
              UKChannelListDialog mUKChListDialog =
                  (UKChannelListDialog)
                      ComponentsManager.getInstance()
                          .getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
              if (mUKChListDialog != null) {
                mSendKeyCode = KeyMap.KEYCODE_MTKIR_CHDN;
                mUKChListDialog.showDialogForPVRAndTShift(
                    DvrDialog.TYPE_Confirm_From_ChannelList,
                    mSendKeyCode,
                    DvrDialog.TYPE_Timeshift,
                    -1);
              }
            } else {
              com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "TIMESHIFT_START KEYCODE_MTKIR_PRECH");
              ChannelListDialog mChListDialog =
                  (ChannelListDialog)
                      ComponentsManager.getInstance()
                          .getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
              if (mChListDialog != null) {
                mSendKeyCode = KeyMap.KEYCODE_MTKIR_PRECH;
                mChListDialog.showDialogForPVRAndTShift(
                    DvrDialog.TYPE_Confirm_From_ChannelList,
                    mSendKeyCode,
                    DvrDialog.TYPE_Timeshift,
                    -1);
              }
            }
            mCanChangeChannel = true;
          } else {
            boolean ret = mActivity.mCommonIntegration.channelPre();
            if (ret) {
              mIsFromTK = true;
              mSendKeyCode = KeyMap.KEYCODE_MTKIR_PRECH;
            } else {
              mCanChangeChannel = true;
            }
          }
        }
      };

  public void registerTvReceiver() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in registerTvReceiver");

    if (mActivity == null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "error.");
      return;
    }

    //
    IntentFilter intentFilter1 = new IntentFilter();
    intentFilter1.addAction(ScreenConstant.ACTION_PREPARE_SHUTDOWN);
    intentFilter1.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    intentFilter1.addAction(TIMESHIFT_MODE_OFF);
    intentFilter1.addAction(LIVE_SETTING_SELECT_SOURCE);
    mActivity.registerReceiver(mReceiver, intentFilter1);

    //
    IntentFilter filter = new IntentFilter();
    filter.addAction("com.mediatek.select.TV");
    filter.addAction("com.mediatek.select.DTV");
    filter.addAction("com.mediatek.select.Composite");
    filter.addAction("com.mediatek.select.Component");
    filter.addAction("com.mediatek.select.SCART");
    filter.addAction("com.mediatek.select.HDMI1");
    filter.addAction("com.mediatek.select.HDMI2");
    filter.addAction("com.mediatek.select.HDMI3");
    filter.addAction("com.mediatek.select.HDMI4");
    filter.addAction("com.mediatek.select.VGA");
    mActivity.registerReceiver(selectSourceReceiver, filter);

    //
    filter = new IntentFilter();
    filter.addAction("com.mediatek.tv.selectchannel");
    filter.addAction("com.mediatek.tv.channelupdown");
    filter.addAction("com.mediatek.tv.channelpre");
    mActivity.registerReceiver(selectChannelReceiver, filter);

    //
    filter = new IntentFilter();
    filter.addAction("com.mediatek.tv.setup.resetdefault");
    filter.addAction("com.mediatek.tv.factory.cleanstorage");
    filter.addAction("com.mediatek.tv.parental.cleanall");
    mActivity.registerReceiver(resetOrCleanReceiver, filter);

    //
    filter = new IntentFilter();
    filter.addAction("com.mediatek.tv.easmsg");
    filter.addAction("com.mediatek.tv.callcc");
    mActivity.registerReceiver(easOrCCReceiver, filter);

    filter = new IntentFilter();
    filter.addAction(TifTimeshiftView.ACTION_TIMESHIFT_STOP);
    mActivity.registerReceiver(addFacTvdiagnosticReceiver, filter);

    filter = new IntentFilter();
    filter.addAction("mtk.intent.event.boot.video.started");
    filter.addAction("mtk.intent.event.boot.video.finished");
    mActivity.registerReceiver(bootVideoReceiver, filter);

    //////////////////////////
    // add lisnter as last one
    ComponentStatusListener lister = ComponentStatusListener.getInstance();
    lister.addListener(ComponentStatusListener.NAV_ENTER_LANCHER, this);
    lister.addListener(ComponentStatusListener.NAV_ENTER_MMP, this);
    lister.addListener(ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
    lister.addListener(ComponentStatusListener.NAV_CONTENT_ALLOWED, this);
    lister.addListener(ComponentStatusListener.NAV_ENTER_ANDR_PIP, this);
    lister.addListener(ComponentStatusListener.NAV_EXIT_ANDR_PIP, this);
    lister.addListener(ComponentStatusListener.NAV_POWER_ON, this);
    lister.addListener(ComponentStatusListener.NAV_POWER_OFF, this);
    lister.addListener(ComponentStatusListener.NAV_INPUTS_PANEL_ENTER_KEY, this);
  }

  public void unregisterTvReceiver() {
    if (mActivity == null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "unregisterTvReceiver, error.");
      return;
    }

    mActivity.unregisterReceiver(mReceiver);
    mActivity.unregisterReceiver(selectSourceReceiver);
    mActivity.unregisterReceiver(selectChannelReceiver);
    mActivity.unregisterReceiver(resetOrCleanReceiver);
    mActivity.unregisterReceiver(easOrCCReceiver);
    mActivity.unregisterReceiver(addFacTvdiagnosticReceiver);
    mActivity.unregisterReceiver(bootVideoReceiver);

    if (mActivity.mThreadHandler != null) {
      mActivity.mThreadHandler.removeCallbacks(mChannelUpRunnable);
      mActivity.mThreadHandler.removeCallbacks(mChannelDownRunnable);
      mActivity.mThreadHandler.removeCallbacks(mChannelPreRunnable);
      mChannelUpRunnable = null;
      mChannelDownRunnable = null;
      mChannelPreRunnable = null;
    }

    mActivity = null;
  }

  private final BroadcastReceiver mReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mReceiver,the intent action = " + intent.getAction());

          if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "reason = " + intent.getExtra("reason"));
            if (!"homekey".equals(intent.getExtra("reason"))) {
              return; // only handle home key
            }

            mActivity.resetInputFlag();

            if (MarketRegionInfo.REGION_US == MarketRegionInfo.getCurrentMarketRegion()) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "ACTION_CLOSE_SYSTEM_DIALOGS, EASSetAndroidLaunchStatus(true)");
              mActivity.mEas.EASSetAndroidLaunchStatus(true);
            }

            if (mActivity.isInPictureInPictureMode()) {
              return; // do not handle in andorid pip mode.
            }

            if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_GINGA)) {
              MtkTvGinga.getInstance().stopGinga();
            }
            if (mActivity.mCommonIntegration.isPipOrPopState()) {
              if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)) {
                ((MultiViewControl)
                        ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_POP))
                    .setVisibility(View.INVISIBLE);
              }
            }

            ComponentStatusListener.getInstance()
                .updateStatus(ComponentStatusListener.NAV_ENTER_LANCHER, 1);
          } else if (intent.getAction().equals(TIMESHIFT_MODE_OFF)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "===TIMESHIFT_MODE_OFF====");
            if (intent.getBooleanExtra("timeshiftmod", false)) {
              if (VendorProperties.mtk_tif_timeshift().orElse(0) == 1) {
                TifTimeshiftView tifTimeshiftView =
                    (TifTimeshiftView)
                        ComponentsManager.getInstance()
                            .getComponentById(NavBasic.NAV_COMP_ID_TIFTIMESHIFT_VIEW);
                tifTimeshiftView.stopTifTimeShift();
              }
            } else {
              if (VendorProperties.mtk_tif_timeshift().orElse(0) == 1) {
                TifTimeshiftView tifTimeshiftView =
                    (TifTimeshiftView)
                        ComponentsManager.getInstance()
                            .getComponentById(NavBasic.NAV_COMP_ID_TIFTIMESHIFT_VIEW);
                tifTimeshiftView.stopTifTimeShift();
              }
            }

          } else if (LIVE_SETTING_SELECT_SOURCE.equals(intent.getAction())) {
            if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT)) {
              String extraSourceName = intent.getStringExtra(LIVE_SETTING_SELECT_SOURCE_NAME);
              int tunerType = intent.getIntExtra(LIVE_SETTING_SELECT_TUNER_TYPE, -1);
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " LIVE_SETTING_SELECT_SOURCE, extraSourceName =" + extraSourceName + ", tunerType: " + tunerType);
              if(tunerType >= 0 && tunerType <= 2) {
                if (null != mActivity.mInputSourceManager) {
                  mActivity.mInputSourceManager.changeInputSourceByHardwareId(tunerType);
                }
              } else
              if (TextUtils.isEmpty(extraSourceName)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " LIVE_SETTING_SELECT_SOURCE, (" + ").equals(extraSourceName)");
                if (null != mActivity.mInputSourceManager) {
                  mActivity.mInputSourceManager.resetCurrentInput();
                }

                if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)) {
                  mActivity.getPipView().setStreamVolume(0.0f);
                  mActivity.getTvView().setStreamVolume(1.0f);
                }
              } else {
                if (null != mActivity.mInputSourceManager) {
                  mActivity.mInputSourceManager.changeInputSourceByHardwareId(
                      mActivity.mInputSourceManager.getHardwareIdByOriginalSourceName(
                          extraSourceName));
                  if (!TurnkeyUiMainActivity.getInstance().isUKCountry) {
                    BannerView bannerView =
                        (BannerView)
                            ComponentsManager.getInstance()
                                .getComponentById(NavBasic.NAV_COMP_ID_BANNER);
                    if (bannerView != null) {
                      bannerView.showSimpleBanner();
                    }
                  }
                }
              }
            }
          }
        }
      };

  BroadcastReceiver selectSourceReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectSourceReceiver");
          if (1 == VendorProperties.mtk_auto_test().orElse(0)) {
            String action = intent.getAction();
            String sourcename = action.substring(action.lastIndexOf(".") + 1);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectSourceReceiver,sourcename" + sourcename);
            if (mActivity.mInputSourceManager != null
                && !sourcename.equalsIgnoreCase(
                    mActivity.mInputSourceManager.autoChangeTestGetCurrentSourceName(
                        mActivity.mCommonIntegration.getCurrentFocus()))) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectSourceReceiver,autoChangeTestSourceChange =" + sourcename);
              mActivity.mInputSourceManager.autoChangeTestSourceChange(
                  sourcename, mActivity.mCommonIntegration.getCurrentFocus());
            }
          }
        }
      };

  BroadcastReceiver selectChannelReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelReceiver");
          String action = intent.getAction();
          if ("com.mediatek.tv.selectchannel".equals(action)) {
            String uriStr = intent.getStringExtra("channelUriStr");
            String channelNum = intent.getStringExtra("channelNum");
            String channelName = intent.getStringExtra("channelName");

            if (mActivity.mInputSourceManager != null) {
              if (channelNum != null || channelName != null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                    TAG,
                    "selectChannelReceiver() channelNum: "
                        + channelNum
                        + " channelName: "
                        + channelName);
                TIFChannelInfo info = null;
                if (channelNum != null) {
                  info =
                      TIFChannelManager.getInstance(mActivity.getApplicationContext())
                          .getChannelByNumOrName(channelNum, false);
                } else if (channelName != null) {
                  info =
                      TIFChannelManager.getInstance(mActivity.getApplicationContext())
                          .getChannelByNumOrName(channelName, true);
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannelReceiver() info: " + info);
                mActivity.mInputSourceManager.tuneChannelByTIFChannelInfoForAssistant(info);

              } else if (uriStr != null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "began to get select data:" + uriStr);
                //                        String path = intent.getStringExtra("path");
                final Uri channelUri = Uri.parse(uriStr);
                TIFChannelManager.getInstance(mActivity.getApplicationContext())
                    .selectChannelByTIFInfo(
                        TIFChannelManager.getInstance(mActivity.getApplicationContext())
                            .getTifChannelInfoByUri(channelUri));
              }
            }
          } else if ("com.mediatek.tv.channelupdown".equals(action)) {
            boolean isUp = intent.getBooleanExtra("upOrdown", true);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "change channel to up or down:" + isUp);
            // for channel up and channel down
            NavBasicDialog channelListDialog =
                (NavBasicDialog)
                    ComponentsManager.getInstance()
                        .getComponentById(NavBasicMisc.NAV_COMP_ID_CH_LIST);
            if (channelListDialog instanceof ChannelListDialog) {
              ChannelListDialog dialog = (ChannelListDialog) channelListDialog;
              dialog.channelUpDown(isUp);
            } else if (channelListDialog instanceof UKChannelListDialog) {
              UKChannelListDialog dialog = (UKChannelListDialog) channelListDialog;
              dialog.channelUpDown(isUp);
            }

          } else if ("com.mediatek.tv.channelpre".equals(action)) {
            // for channel pre
            if (mActivity.mCommonIntegration != null) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "change channel pre");
              mActivity.mCommonIntegration.channelPre();
            }
          }
        }
      };

  // just for LiveTVSetting's reset Default or clean storage and ...
  BroadcastReceiver resetOrCleanReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetOrCleanReceiver");
          String action = intent.getAction();
          com.mediatek.wwtv.setting.util.TVContent menuTV =
              com.mediatek.wwtv.setting.util.TVContent.getInstance(context);
          if ("com.mediatek.tv.setup.resetdefault".equals(action)) {
            TurnkeyUiMainActivity.setStartTv(true);
            menuTV.cleanLocalData();
          } else if ("com.mediatek.tv.factory.cleanstorage".equals(action)) {
            TurnkeyUiMainActivity.setStartTv(true);
            menuTV.cleanLocalData();
          } else if ("com.mediatek.tv.parental.cleanall".equals(action)) {
            TurnkeyUiMainActivity.setStartTv(true);
            menuTV.cleanLocalData();
          }
        }
      };

  // for receive the earthquake info
  BroadcastReceiver easOrCCReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "easReceiver");
          String action = intent.getAction();
          if ("com.mediatek.tv.easmsg".equals(action)) {
            int compId = intent.getIntExtra(NavBasic.NAV_COMPONENT_SHOW_FLAG, 0);
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "easReceiver, compId=" + compId);
            if ((compId & NavBasic.NAV_COMP_ID_MASK) == NavBasic.NAV_COMP_ID_BASIC) {
              if (mActivity.mNavCompsMagr != null) {
                mActivity.mNavCompsMagr.showNavComponent(compId);
              }
            }
          } else if ("com.mediatek.tv.callcc".equals(action)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "easReceiver, callcc=");
            boolean visible = intent.getBooleanExtra("ccvisible", false);
            if (visible) {
              MtkTvConfig.getInstance()
                  .setConfigValue(MtkTvConfigType.CFG_CC_DCS, 1); // update value to driver
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "easReceiver, callcc visible is=" + visible);
            BannerImplement.getInstanceNavBannerImplement(context).setCCVisiable(visible);
          }
        }
      };

  BroadcastReceiver addFacTvdiagnosticReceiver =
      new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
          // TODO Auto-generated method stub
          TifTimeshiftView tifTimeshiftView =
              (TifTimeshiftView)
                  ComponentsManager.getInstance()
                      .getComponentById(NavBasic.NAV_COMP_ID_TIFTIMESHIFT_VIEW);
          tifTimeshiftView.stopTifTimeShift();
        }
      };

      BroadcastReceiver bootVideoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "bootVideoReceiver," + intent.getAction());
          if("mtk.intent.event.boot.video.started".equals(intent.getAction())) {
            InputSourceManager.bootVideoPlaying = true;
          } else if("mtk.intent.event.boot.video.finished".equals(intent.getAction())) {
            InputSourceManager.bootVideoPlaying = false;
            if(InputSourceManager.uriAfterBootVideo != null) {
              Intent i = new Intent(Intent.ACTION_VIEW);
              if(DestroyApp.isCurActivityTkuiMainActivity()) {
                i.setData(InputSourceManager.uriAfterBootVideo);
                mActivity.processInputUri(i);
              } else {
                i.setData(InputSourceManager.uriAfterBootVideo);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("livetv", true);
                mActivity.getApplicationContext().startActivity(i);
              }
              InputSourceManager.uriAfterBootVideo = null;
            }
          }
        }
      };

  @Override
  public void updateComponentStatus(int statusID, int value) {
    if (statusID == ComponentStatusListener.NAV_ENTER_LANCHER) {
      mActivity.mAudioManager.abandonAudioFocus();
      MtkTvMultiView.getInstance().setChgSource(false);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in NAV_ENTER_LANCHER,to stop session");
      if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT)) {
        if (mActivity.mCommonIntegration.isPipOrPopState()) {
          if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)) {
            ((MultiViewControl)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_POP))
                .setNormalTvModeWithLauncher(false);
          }
        }
      }

      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "enter lancher");

      if (VendorProperties.mtk_tif_timeshift().orElse(0) != 1) {
        TVAsyncExecutor.getInstance()
            .execute(
                new Runnable() {
                  @Override
                  public void run() {
                    try {
                      Thread.sleep(2000);
                    } catch (Exception ex) {
                      ex.printStackTrace();
                    }
                    MtkTvTimeshift.getInstance().stop();
                  }
                });
      }

      //            if (StateDvr.getInstance() != null && StateDvr.getInstance().isRunning()) {
      //                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
      //                        "DvrManager.isSuppportDualTuner() = " +
      // DvrManager.isSuppportDualTuner());
      //                if (DvrManager.isSuppportDualTuner()) {
      //                    // DvrManager.getInstance().getController().stopRecordingMultiple();
      //                } else {
      //                    // DvrManager.getInstance().getController().stopRecording();
      //                }
      //            }
      if (ScheduleListDialog.getDialog() != null && ScheduleListDialog.getDialog().isShowing()) {
        // CR 959741 ,follow linux ,dismiss schedule dialog when confirm dialog show.
        ScheduleListDialog.getDialog().dismiss();
      }

      /** the method used to stop dvr play when comes to luncher */
      if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
        // only use to judge weather need resume turnkey .(set true : fix CR DTV00782979)
        DvrManager.getInstance().setStopDvrNotResumeLauncher(true);
      }
      TTXMain ttxMain =
          (TTXMain)
              ComponentsManager.getInstance().getComponentById(NavBasicMisc.NAV_COMP_ID_TELETEXT);
      if (ttxMain != null && ttxMain.isActive) {
        TeletextImplement.getInstance().stopTTX();
      }

      mActivity.sendBroadcast(new Intent(DvrDialog.DISSMISS_DIALOG), "com.mediatek.tv.permission.BROADCAST");

    } else if (statusID == ComponentStatusListener.NAV_ENTER_MMP) {
      (new MtkTvAppTVBase()).updatedSysStatus(MtkTvAppTVBase.SYS_MMP_RESUME);
    } else if (statusID == ComponentStatusListener.NAV_CHANNEL_CHANGED) {
      mActivity.mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
      ComponentStatusListener.setParam1(value);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mIsFromTK>>>" + mIsFromTK);
      List<Integer> cpmsIDs = ComponentsManager.getInstance().getCurrentActiveComps();
      if (mIsFromTK || cpmsIDs.contains(NavBasic.NAV_COMP_ID_FAV_LIST)) {
        ComponentStatusListener.getInstance()
            .updateStatus(ComponentStatusListener.NAV_KEY_OCCUR, mSendKeyCode);
      }
      mCanChangeChannel = true;
    } else if (statusID == ComponentStatusListener.NAV_ENTER_ANDR_PIP) {
      MultiViewControl mMultiViewControl =
          new MultiViewControl(TurnkeyUiMainActivity.getInstance().getApplicationContext());
      if (CommonIntegration.getInstance().isPipOrPopState()) {
        mMultiViewControl.setNormalTvModeWithGooglePiP();
        ComponentStatusListener.getInstance()
            .delayUpdateStatus(ComponentStatusListener.NAV_ENTER_ANDR_PIP, 2500);
        return;
      }

      mActivity.enterPictureInPictureMode(new android.app.PictureInPictureParams.Builder().build());
      SaveValue.getInstance(mActivity).saveBooleanValue("isInPictureInPicureMode", true);
      DvrManager.getInstance().setStopDvrNotResumeLauncher(true);
      MtkTvConfig.getInstance()
          .setAndroidWorldInfoToLinux(
              MtkTvConfigType.CFG_ANDROID_MODE_MULTI_WINDOW,
              MtkTvConfigType.ANDROID_WORLD_INFO_ENTER_MULTI_WIN);

      MtkTvUtil.getInstance().setOSDPlaneEnable(0, 0);
      MtkTvUtil.getInstance().setOSDPlaneEnable(1, 0);
    } else if (statusID == ComponentStatusListener.NAV_EXIT_ANDR_PIP) {
      SaveValue.getInstance(mActivity).saveBooleanValue("isInPictureInPicureMode", false);
      TVAsyncExecutor.getInstance()
          .execute(
              new Runnable() {
                @Override
                public void run() {
                  MtkTvConfig.getInstance()
                      .setAndroidWorldInfoToLinux(
                          MtkTvConfigType.CFG_ANDROID_MODE_MULTI_WINDOW,
                          MtkTvConfigType.ANDROID_WORLD_INFO_EXIT_MULTI_WIN);

                  MtkTvUtil.getInstance().setOSDPlaneEnable(0, 1);
                  MtkTvUtil.getInstance().setOSDPlaneEnable(1, 1);

                  mActivity.mAudioManager.unmuteTVAudio(AudioFocusManager.AUDIO_NAVIGATOR);
                }
            });
        } else if (statusID == ComponentStatusListener.NAV_POWER_ON) {
            powerOn();
        } else if (statusID == ComponentStatusListener.NAV_POWER_OFF) {
            powerOff(value);
        } else if (statusID == ComponentStatusListener.NAV_CONTENT_ALLOWED) {
            mActivity.mHandler.removeMessages(ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY);
            ComponentStatusListener.setParam1(value);
            mCanChangeChannel = true;
        }else if(statusID == ComponentStatusListener.NAV_INPUTS_PANEL_ENTER_KEY) {
            showConfirmDialog(value);
        }
    }

  private void showConfirmDialog(int value) {
    if (mActivity != null && !DestroyApp.isCurActivityTkuiMainActivity()) {
      TurnkeyUiMainActivity.resumeTurnkeyActivity(mActivity);
    }
    boolean timeShiftStart = SaveValue.readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START);
    boolean isRecording = StateDvr.getInstance() != null && StateDvr.getInstance().isRecording();
    SimpleDialog simpleDialog = (SimpleDialog)ComponentsManager.getInstance().
            getComponentById(NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
    simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
    simpleDialog.setConfirmText(R.string.pvr_confirm_yes);
    simpleDialog.setCancelText(R.string.pvr_confirm_no);
    simpleDialog.setOnConfirmClickListener(new SimpleDialog.OnConfirmClickListener() {

      @Override
      public void onConfirmClick(int dialogId) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onConfirmClick.");
        if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "turnkey receive : stop dvr ");
          StateDvrPlayback.getInstance().stopDvrFilePlay(false);
        }
        DvrManager.getInstance().stopAllRunning();
        TifTimeshiftView tifTimeshiftView = (TifTimeshiftView) ComponentsManager
                .getInstance().getComponentById(
                        NavBasic.NAV_COMP_ID_TIFTIMESHIFT_VIEW);
        if (TifTimeShiftManager.getInstance() != null) {
          TifTimeShiftManager.getInstance().stopAll();
        }
        if(value >= 0) {
          mActivity.mInputSourceManager.changeInputSourceByHardwareId(value, false);
        } else if(mActivity != null && mActivity.getIntent() != null){
          mActivity.getTvView().postDelayed(new Runnable() {
            @Override
            public void run() {
              mActivity.mInputSourceManager.processInputUri(mActivity.getIntent());
            }
          },1000);

        }
      }
    },-1);
    simpleDialog.setOnCancelClickListener(new SimpleDialog.OnCancelClickListener() {
      @Override
      public void onCancelClick(int dialogId) {
        if (mActivity != null) {
          if (mActivity.getTvView() != null && !mActivity.getTvView().isStart()) {
            long channelId = SaveValue.getInstance(mActivity).readLongValue(DvrManager.DVR_URI_ID, -1);
            if (channelId != -1) {
              mActivity.mInputSourceManager.tuneChannelByUri(TvContract.buildChannelUri(channelId));
            }
          }
        }
      }
    },-1);
    simpleDialog.setOnTimeoutListener(new SimpleDialog.OnTimeoutListener() {
      @Override public void onTimeout(int dialogId) {
        if (mActivity != null) {
          long channelId = SaveValue.getInstance(mActivity).readLongValue(DvrManager.DVR_URI_ID, -1);
          if (channelId != -1) {
            mActivity.mInputSourceManager.tuneChannelByUri(TvContract.buildChannelUri(channelId));
          }
        }
      }
    }, -1);
    if(value >= -1) {
      if(isRecording) {
        simpleDialog.setContent(R.string.dvr_dialog_message_record_source);
      } else if(timeShiftStart) {
        simpleDialog.setContent(R.string.dvr_dialog_message_timeshift_source);
      }
    } else {
      if(isRecording) {
        simpleDialog.setContent(R.string.dvr_dialog_message_record_channel);
      } else if(timeShiftStart) {
        simpleDialog.setContent(R.string.dvr_dialog_message_timeshift_channel);
      }
    }
    if (mActivity != null) {
      int delay = mActivity.getResources().getInteger(R.integer.pvr_dialog_time_out);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("showConfirmDialog, ", "delay: " + delay);
      simpleDialog.setScheduleDismissTime(delay);
      simpleDialog.show();
    }
  }

  private void powerOff(int value) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("TurnkeyReceiver", "powerOff value=" + value);
    WakeLockHelper.acquireWakeLock(mActivity, "POWER_OFF");
    mActivity.isShutdownFlow = true;
    mActivity.getTvView().setStart(false);
    if(DestroyApp.isCurTaskTKUI()){
        CommonIntegration.resetBlueMuteForLiveTv(mActivity);
    }
    if (SaveValue.getInstance(mActivity).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("TurnkeyReceiver", "stop timeshift");
      TifTimeshiftView tifTimeshiftView =
          (TifTimeshiftView)
              ComponentsManager.getInstance()
                  .getComponentById(NavBasic.NAV_COMP_ID_TIFTIMESHIFT_VIEW);
      tifTimeshiftView.setVisibility(View.GONE);
      tifTimeshiftView.stopTifTimeShift();
    }
    mActivity.sendBroadcast(new Intent(DvrDialog.DISSMISS_DIALOG), "com.mediatek.tv.permission.BROADCAST");
    if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
      StateDvrPlayback.getInstance().stopDvrFilePlay();
    }

    if (value == ComponentStatusListener.NAV_SHUT_DOWN) {
      mActivity.mInputSourceManager.setShutDownFlag(true);
    }

    if (DestroyApp.isCurOADActivityNew()) {
      NavOADActivityNew.getInstance().stopOAD();
    }
    // for fix DTV00852899 ,dismiss dvr dialog as linux do .
    if (StateDvr.getInstance() != null) {
      StateDvr.getInstance().hideDvrdialog();
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(
        TAG,
        "DestroyApp.isCurTaskTKUI() ="
            + DestroyApp.isCurTaskTKUI()
            + "DestroyApp.isCurActivityTkuiMainActivity()"
            + DestroyApp.isCurActivityTkuiMainActivity());
    if (DestroyApp.isCurTaskTKUI() && !DestroyApp.isCurActivityTkuiMainActivity()) {
      TurnkeyUiMainActivity.resumeTurnkeyActivity(mActivity.getApplicationContext());
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in Intent.ACTION_SCREEN_OFF or ACTION_SHUTDOWN");
    TTXMain ttxMain =
        (TTXMain)
            ComponentsManager.getInstance().getComponentById(NavBasicMisc.NAV_COMP_ID_TELETEXT);
    if (ttxMain != null && ttxMain.favDialog != null) {
      ttxMain.favDialog.clearFavlist();
    }

    mActivity.mNoNeedResetSource = true;
    mActivity.mNoNeedChangeChannel = false;

    do {
      if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_DVR)) {
        if ((null != StateDvr.getInstance() && StateDvr.getInstance().isRecording())) {
          break;
        }
      }

      List<MtkTvBookingBase> books = MtkTvRecord.getInstance().getBookingList();
      if (books != null && !books.isEmpty()) {
        break;
      }
    } while (false);
    mActivity.mNavCompsMagr.hideAllComponents();

    ComponentStatusListener.getInstance()
        .updateStatus(ComponentStatusListener.NAV_ENTER_STANDBY, -1);
  }

  private void powerOn() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ACTION_SCREEN_ON isShutdownFlow: " + mActivity.isShutdownFlow);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(
        TAG,
        "DestroyApp.isCurTaskTKUI() ="
            + DestroyApp.isCurTaskTKUI()
            + "DestroyApp.isCurActivityTkuiMainActivity()"
            + DestroyApp.isCurActivityTkuiMainActivity());
    /*if (StateDvr.getInstance() != null && StateDvr.getInstance().isRecording()) {
        if(!DestroyApp.isCurActivityTkuiMainActivity()){
           StateDvr.getInstance().clearWindow(false);
        }
    }*/
    if(DestroyApp.isCurTaskTKUI()){
        CommonIntegration.resetBlueMuteForLiveTv(mActivity);
    }

    showToast();
    while (mActivity.isShutdownFlow) {
      mActivity.isStartTv = true;
      mActivity.mInputSourceManager.resetScreenOffFlag();
      if (TvSingletons.getSingletons().getTurnkeyUiMainActiviteActive()
          && mActivity.getTvView() != null
          && !mActivity.getTvView().isStart()) {
        mActivity.mInputSourceManager.resetCurrentInput();
      }
      mActivity.isShutdownFlow = false;
      SaveValue saveVv = SaveValue.getInstance(mActivity);
      String isTuneDone = saveVv.readStrValue("FineTune_IsFineTuneDone");
      com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "ACTION_SCREEN_ON,isTuneDone==" + isTuneDone);
      if ("false".equals(isTuneDone)) {
        saveVv.saveStrValue("FineTune_IsFineTuneDone", "true");
        String hz = saveVv.readStrValue("FineTune_RestoreHz");
        final float resHz = Float.parseFloat(hz);
        mActivity.mThreadHandler.postDelayed(
            new Runnable() {
              @Override
              public void run() {
                mActivity.mCommonIntegration.restoreFineTune(resHz);
              }
            },
            3000);
      }
    }
  } // powerOn

  private void showToast() {
    int errorDuration = SaveValue.readWorldIntValue(mActivity, DvrManager.BGM_PVR_LOSS);
    String content = SaveValue.readWorldStringValue(mActivity, DvrManager.BGM_PVR_FAIL_STR);
    String nameString = mActivity.getString(R.string.bgm_fail_record_before, content);
    String durationLossString =
        mActivity.getString(R.string.bgm_loss_time_pvr, content, errorDuration);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "type==" + nameString + "," + durationLossString);
    if (SaveValue.readWorldBooleanValue(mActivity, DvrManager.BGM_PVR_FAIL)) {
      SaveValue.saveWorldBooleanValue(mActivity, DvrManager.BGM_PVR_FAIL, false, true);
      Toast.makeText(mActivity, nameString, Toast.LENGTH_LONG).show();
    }
    if (errorDuration > 1) {
      //Toast.makeText(mActivity, durationLossString, Toast.LENGTH_LONG).show();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"errorDuration->"+errorDuration);
      MyToast myToast = new MyToast(mActivity,new Handler());
      myToast.show(durationLossString,6000);
      SaveValue.saveWorldValue(mActivity, DvrManager.BGM_PVR_LOSS, 0, true);
    }
  }
}
