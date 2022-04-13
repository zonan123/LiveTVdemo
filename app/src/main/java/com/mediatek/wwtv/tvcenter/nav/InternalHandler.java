package com.mediatek.wwtv.tvcenter.nav;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.tv.TvInputInfo;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.android.tv.menu.MenuOptionMain;
import com.mediatek.twoworlds.tv.MtkTvCI;
import com.mediatek.twoworlds.tv.MtkTvCIBase;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvGinga;
import com.mediatek.twoworlds.tv.MtkTvHBBTV;
import com.mediatek.twoworlds.tv.MtkTvHBBTVBase;
import com.mediatek.twoworlds.tv.MtkTvInputSource;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.MtkTvTeletext;
import com.mediatek.twoworlds.tv.common.MtkTvCIMsgTypeBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigMsgBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvNativeAppId;
import com.mediatek.wwtv.setting.preferences.SettingsPreferenceScreen;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.commonview.DiagnosticDialog;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.dvr.ui.DvrDialog;
import com.mediatek.wwtv.tvcenter.epg.EPGConfig;
import com.mediatek.wwtv.tvcenter.epg.eu.EpgType;
import com.mediatek.wwtv.tvcenter.nav.fav.FavoriteListDialog;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.util.IntegrationZoom;
import com.mediatek.wwtv.tvcenter.nav.util.MtkTvEWSPA;
import com.mediatek.wwtv.tvcenter.nav.util.MultiViewControl;
import com.mediatek.wwtv.tvcenter.nav.view.BMLMain;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.CommonMsgDialog;
import com.mediatek.wwtv.tvcenter.nav.view.DvbtInactiveChannelConfirmDialog;
import com.mediatek.wwtv.tvcenter.nav.view.EWSDialog;
import com.mediatek.wwtv.tvcenter.nav.view.FVP;
import com.mediatek.wwtv.tvcenter.nav.view.FloatView;
import com.mediatek.wwtv.tvcenter.nav.view.GingaTvDialog;
import com.mediatek.wwtv.tvcenter.nav.view.Hbbtv;
import com.mediatek.wwtv.tvcenter.nav.view.InfoBarDialog;
import com.mediatek.wwtv.tvcenter.nav.view.Mheg5;
import com.mediatek.wwtv.tvcenter.nav.view.AtscInteractive;
import com.mediatek.wwtv.tvcenter.nav.view.PwdDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SundryShowTextView;
import com.mediatek.wwtv.tvcenter.nav.view.TTXMain;
import com.mediatek.wwtv.tvcenter.nav.view.TwinkleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.UKChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.UkBannerView;
import com.mediatek.wwtv.tvcenter.nav.view.VgaPowerManager;
import com.mediatek.wwtv.tvcenter.nav.view.ZoomTipView;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIMainDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.oad.NavOADActivityNew;
import com.mediatek.wwtv.tvcenter.util.Commands;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.KeyDispatch;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import java.lang.ref.WeakReference;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.util.ScreenStatusManager;

/** Handler Message */
public class InternalHandler extends Handler {
  private static final String TAG = "TurnkeyUiMainActivity(InternalHandler)";

  public static final int MSG_START = 100;
  public static final int MSG_START_INPUT_SETUP = MSG_START + 1;
  public static final int MSG_SHOW_FAV_LIST_FULL_TOAST = MSG_START + 2;
  public static final int MSG_CLOSE_SOURCE = MSG_START + 3;
  public static final int MSG_SELECT_CURRENT_CHANNEL = MSG_START + 4;
  public static final int RESET_LAYOUT = MSG_START + 5;
  public static final int MSG_PAUSE_NATIVE_HBBTV = MSG_START + 6;
  public static final int MSG_RESUME_NATIVE_HBBTV = MSG_START + 7;
  public static final int MSG_TIME_SHIFT_NOT_AVAILABLE = MSG_START + 8;

  private final WeakReference<TurnkeyUiMainActivity> mDialog;
  private DiagnosticDialog dtcDialog;
  private final MtkTvCIBase ciBase;
  private TurnkeyUiMainActivity mTurnkey;
  private static boolean isfromturnkeyonstop;

  public InternalHandler(TurnkeyUiMainActivity dialog) {
    mTurnkey = dialog;
    mDialog = new WeakReference<TurnkeyUiMainActivity>(dialog);
    ciBase = MtkTvCI.getInstance(0);
    showDiagnosticDialog();
    com.mediatek.wwtv.tvcenter.util.MtkLog.dumpMessageQueue(new WeakReference<>(this), 1000);
  }

  @Override
  public void handleMessage(Message msg) {
    /*com.mediatek.wwtv.tvcenter.util.MtkLog.d(
        TAG,
        "[InternalHandler] handlerMessage occur~"
            + msg.what
            + ","
            + msg.arg1
            + ","
            + msg.arg2
            + ","
            + msg.obj);*/

    if (mDialog.get() != null) {

      /* For Callback */
      if ((msg.what == msg.arg1)
          && (msg.what == msg.arg2)
          && ((msg.what & TvCallbackConst.MSG_CB_BASE_FLAG) != 0)) {
        handlerCallbackMsg(msg);
        return;
      }

      /* For normal message */
      handlerMessages(msg);
    } // mDialog.get()
  }

  private void showDiagnosticDialog() {
    if (dtcDialog != null) {
      return;
    }
    dtcDialog = new DiagnosticDialog(mDialog.get());
    dtcDialog.setOnCancelListener(
        new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            ciBase.setHDSConfirm(1);
          }
        });
    dtcDialog.setOnShowListener(
        new DialogInterface.OnShowListener() {
          @Override
          public void onShow(DialogInterface dialog) {
            ciBase.setHDSConfirm(0);
          }
        });
  }

  /**
   * this method is used to handle callback APIs
   *
   * @param msg
   */
  private void handlerCallbackMsg(Message msg) {
    TvCallbackData data = (TvCallbackData) msg.obj;

    //com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "msg = " + msg.what);

    switch (msg.what) {
      case TvCallbackConst.MSG_CB_GINGA_VOLUME_MSG:
        GingaTvDialog gingaDlg =
            (GingaTvDialog)
                ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_GINGA_TV);
        if (gingaDlg != null) {
          gingaDlg.changeVolume(data.param1, data.param2);
        }
        break;
      case TvCallbackConst.MSG_CB_GINGA_MSG:
        gingaDlg =
            (GingaTvDialog)
                ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_GINGA_TV);
        if (gingaDlg != null) {
          gingaDlg.addGingaAppInfo(data.param1, data.paramStr1, (String) data.paramObj1);
        }
        break;
      case TvCallbackConst.MSG_CB_CI_MSG:
        EWSDialog ewsDialog2 = (EWSDialog) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_EWS);
        if(ewsDialog2 != null && ewsDialog2.isShowing() && ewsDialog2.isPlayTone()){
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_CI_MSG ewsDialog2 is showing~ ");
          return;
        }
        // if pwd dialog not show
        boolean isPWDNotShow = MtkTvPWDDialog.getInstance().PWDShow() != 0;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_CI_MSG isPWDNotShow==" + isPWDNotShow);
        // only cn & eu has cicard
        boolean isDTV = false;
        boolean isEUTV = false;
        if (MarketRegionInfo.REGION_CN == MarketRegionInfo.getCurrentMarketRegion()
            || CommonIntegration.getInstance().isEUPARegion()) {
          isDTV = CommonIntegration.getInstance().isCurrentSourceDTV();
        } else if (MarketRegionInfo.REGION_EU == MarketRegionInfo.getCurrentMarketRegion()) {
          isEUTV = CommonIntegration.getInstance().isCurrentSourceTv();
        }
        if ((isEUTV || isDTV)) {
          CIMainDialog ciMainDialog =
              (CIMainDialog)
                  ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_CI_DIALOG);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_CI_MSG ciMainDialog==" + ciMainDialog);
          if(ciMainDialog == null){
            return;
          }
          if (isPWDNotShow) {
            ciMainDialog.handleCIMessage(data);
          } else {
            // delay 2500ms to show cimain dialog
            if (!ciMainDialog.isDialogIsShow()) {
              ciMainDialog.handleCIMessageDelay(data);
            } else {
              ciMainDialog.handleCIMessage(data);
            }
          }
        }

        // something goes wrong,it will show dialog
        int isShow = data.param3; // 0 represents show,1 represents dismiss.
        int messageType = data.param2;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "messageType" + messageType);
        if (messageType == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_HDS_REQUEST) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MTKTV_CI_NFY_COND_HDS_REQUEST>>>>isShow:" + isShow);
          if (isShow == 0) {
            if (dtcDialog != null) {
              dtcDialog.show();
            } else {
              showDiagnosticDialog();
              dtcDialog.show();
            }
          } else if (isShow == 1) {
            if (dtcDialog != null) {
              dtcDialog.cancel();
            }
          }
        }
        break;
      case TvCallbackConst.MSG_CB_EAS_MSG:
        // will keep play dvr file when eas msg comming for CR DTV00830571.
        // eas msg only
        // function on TV with its in playing .
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "MSG_CB_EAS_MSG, data.param1=" + data.param1 + ",data.param2="+data.param2);
        if(data.param1 == 1){
          if(!TvSingletons.getSingletons().getTurnkeyUiMainActiviteActive()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TK stop, won't show EAS UI!");
            return;
          }
        }
        if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
          break;
        }
        ActivityManager aManager =
            (ActivityManager) mDialog.get().getSystemService(Context.ACTIVITY_SERVICE);
        aManager.killBackgroundProcesses("com.mediatek.wwtv.setting");
        FloatView floatView =
            (FloatView) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_EAS);
        if (floatView != null) {
          floatView.handleEasMessage(data.param1, data.param2);
        }
        if (((TvCallbackData) msg.obj).param1 == 1) {
          mDialog.get().sendBroadcast(new Intent(DvrDialog.DISSMISS_DIALOG), "com.mediatek.tv.permission.BROADCAST");

          if (CommonIntegration.getInstance().isPipOrPopState()) {
            if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)) {
              ((MultiViewControl)
                      ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_POP))
                  .setNormalTvModeWithEas();
            }
          }
        }
        break;
      case TvCallbackConst.MSG_CB_MHEG5_MSG:
        {
          Mheg5 mheg5 =
              (Mheg5)
                  ComponentsManager.getInstance()
                      .getComponentById(NavBasic.NAV_NATIVE_COMP_ID_MHEG5);
          if (mheg5 != null) {
            mheg5.handlerMheg5Message(data.param1, data.param2);
          }
          if (!mTurnkey.isUKCountry) {
            BannerView bannerView =
                (BannerView)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
            if (bannerView != null) {
              bannerView.handlerMheg5Message(data.param1, data.param2);
            }
          } else {
            UkBannerView bannerView =
                (UkBannerView)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
            if (bannerView != null) {
              bannerView.handlerMheg5Message(data.param1, data.param2);
            }
          }
        }
        break;
      case TvCallbackConst.MSG_CB_TTX_MSG:
        TTXMain ttx =
            (TTXMain)
                ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_TELETEXT);
        if (ttx != null) {
          ttx.handlerTTXMessage(data.param1);
        }
        break;
      case TvCallbackConst.MSG_CB_NO_USED_KEY_MSG:
        if (data.param2 != 0) { // just handle keydown event
          com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "MSG_CB_NO_USED_KEY_MSG, data.param2=" + data.param2);
          return;
        }


        TurnkeyUiMainActivity turnkeyUiMainActivity = mDialog.get();
        int keycode = turnkeyUiMainActivity.mKeyDispatch.getPassedAndroidKey();

        if (turnkeyUiMainActivity.mKeyDispatch.androidKeyToDFBkey(keycode) != data.param1) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "key not map, data.param2=" + data.param1);
          return;
        }

        turnkeyUiMainActivity.onKeyHandler(keycode, null, true);
        break;
      case TvCallbackConst.MSG_CB_EWS_MSG:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_EWS_MSG" + "  data.param1 = " + data.param1);
        if(!TvSingletons.getSingletons().getTurnkeyUiMainActiviteActive()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TK stop, won't show EWS UI!");
            return;
        }
        EWSDialog ewsDialog =
            (EWSDialog) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_EWS);
        if (data.param1 == 1 || data.param1 == 0) { // ews gone or error
          if (ewsDialog != null && ewsDialog.isShowing()) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismiss ewsDialog");
              ewsDialog.dismiss();
          }
        } else if (data.param1 == 2 || data.param1 == 3) { // ews found or
          // changed
            boolean isSignalLoss = TVContent.getInstance(mDialog.get()).isSignalLoss();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSignalLoss: "+isSignalLoss);
            if (isSignalLoss) {
                break;
            }
          if (ewsDialog != null && DestroyApp.isCurActivityTkuiMainActivity()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showEwsDialog");
            ComponentsManager.getInstance().showNavComponent(NavBasic.NAV_COMP_ID_EWS);
          } else if (ewsDialog != null && !DestroyApp.isCurActivityTkuiMainActivity()) {
            if (MtkTvScan.getInstance().isScanning()) { // scanning
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isScanning");
              break;
            }
            if(DestroyApp.getTopActivity() != null && DestroyApp.getTopActivity().getIntent() != null){
                String actionID = DestroyApp.getTopActivity().getIntent().getStringExtra("ActionID");
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "actionID: "+actionID + ", DestroyApp.getRunningActivity()>> "+DestroyApp.getRunningActivity());
                if("ScanViewActivity".equals(DestroyApp.getRunningActivity())
                      && (MenuConfigManager.TV_DVBC_SINGLE_RF_SCAN.equals(actionID)
                      || MenuConfigManager.TV_DVBT_SINGLE_RF_SCAN.equals(actionID))){
                    break;
                }
            }
            if(!ewsDialog.isShowing()) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ewsDialog not show!");
              mDialog.get().resumeTurnkeyActivity(mDialog.get().getApplicationContext());
            }
            ComponentsManager.getInstance().showNavComponent(NavBasic.NAV_COMP_ID_EWS);
          }
        }
        break;

      case TvCallbackConst.MSG_CB_EWS_SA_MSG:// sa;philippines;suspend;
          boolean isScreenOn = CommonIntegration.getInstance().isScreenOn(mDialog.get().getApplicationContext());
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_EWS_SA_MSG" + "  data.param1 = " + data.param1 + ",isScreenOn>>"+isScreenOn);
          if(isScreenOn){
              return;
          }
          if (data.param1 == 0) {//start ews msg
              ((AudioManager)mDialog.get().getApplicationContext()
                      .getSystemService(Context.AUDIO_SERVICE)).setStreamMute(AudioManager.STREAM_SYSTEM, false);
          } else if(data.param1 == 1){//end ews msg
              ((AudioManager)mDialog.get().getApplicationContext()
                      .getSystemService(Context.AUDIO_SERVICE)).setStreamMute(AudioManager.STREAM_SYSTEM, true);
          }
          break;

      case TvCallbackConst.MSG_CB_HBBTV_MSG:
        {
          Hbbtv hbbtv =
              (Hbbtv)
                  ComponentsManager.getInstance()
                      .getComponentById(NavBasic.NAV_NATIVE_COMP_ID_HBBTV);
          if (hbbtv != null) {
            hbbtv.handlerHbbtvMessage(data.param1, data.param2);
          }

          TwinkleDialog twinkleDialog = (TwinkleDialog) ComponentsManager
                  .getInstance().getComponentById(
                          NavBasic.NAV_COMP_ID_TWINKLE_DIALOG);
          if (twinkleDialog != null) {
            twinkleDialog.handlerHbbtvMessage(data.param1, data.param2);
          }

          if (!mTurnkey.isUKCountry) {
            BannerView bannerView =
                (BannerView)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
            if (bannerView != null) {
              bannerView.handlerHbbtvMessage(data.param1, data.param2);
            }
          } else {
            UkBannerView bannerView =
                (UkBannerView)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
            if (bannerView != null) {
              bannerView.handlerHbbtvMessage(data.param1, data.param2);
            }
          }
        }
        break;
      case TvCallbackConst.MSG_CB_SVCTX_NOTIFY:
        {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mgs=MSG_CB_SVCTX_NOTIFY----->data.parama1  = " + data.param1);
          if (mTurnkey.mHdmidaiManager != null) {
            mTurnkey.mHdmidaiManager.handleSvctxNotify(data.param1);
          }
          if (data.param1 == CommonIntegration.SVCTX_NTFY_CODE_VIDEO_FMT_UPDATE
              || data.param1 == CommonIntegration.SVCTX_NTFY_CODE_VIDEO_ONLY_SVC) {
            if (!mTurnkey.isUKCountry) {
              BannerView bannerView =
                  (BannerView)
                      ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
              if (bannerView != null) { // tune channel after tune source
                // success
                bannerView.unScramebled();
              }
            }
            /*if (1 == MtkTvConfig.getInstance().getConfigValue(
                    MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in set CFG_MISC_AV_COND_MMP_MODE, 0");
                MtkTvConfig.getInstance().setConfigValue(
                        MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE, 0);
            }*/
          } else if (data.param1 == CommonIntegration.SVCTX_NTFY_CODE_CHANNEL_CHANGE) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_SVCTX_NOTIFY--->data.param1==SVCTX_NTFY_CODE_CHANNEL_CHANGE");
            CommonIntegration.getInstance().setCHChanging(false);
            PwdDialog mPWDDialog =
                (PwdDialog)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "change channel dismiss pwd");
            if (mPWDDialog != null) {
              mPWDDialog.dismiss();
              mPWDDialog.setSvctxBlocked(false);
            }
            showBanner();
            ComponentStatusListener.getInstance()
                .updateStatus(ComponentStatusListener.NAV_CHANNEL_CHANGED, 0);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ======== ComponentStatusListener.NAV_CHANNEL_CHANGED");
          } else if (data.param1 == CommonIntegration.SVCTX_NTFY_CODE_BLOCK
              || data.param1 == CommonIntegration.SVCTX_NTFY_CODE_UNBLOCK) {
            if (!mTurnkey.isUKCountry) {
              BannerView bannerView =
                  (BannerView)
                      ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
              if (bannerView != null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setBlockStateFromSVCTX");
                bannerView.setBlockStateFromSVCTX(
                    data.param1 == CommonIntegration.SVCTX_NTFY_CODE_BLOCK ? true : false);
              }
            }
            PwdDialog mPWDDialog =
                (PwdDialog)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
            if (mPWDDialog != null) {
                mPWDDialog.setSvctxBlocked(data.param1 == CommonIntegration.SVCTX_NTFY_CODE_BLOCK);
              int type =
                  data.param1 == CommonIntegration.SVCTX_NTFY_CODE_BLOCK
                      ? PwdDialog.PASSWORD_VIEW_SHOW_PWD_INPUT
                      : PwdDialog.PASSWORD_VIEW_DISMISS_PWD_INPUT;
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_SVCTX_NOTIFY--->type=" + type);
              boolean isTtxRatingOn = MtkTvTeletext.getInstance().getTtxRatingOn();
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isTtxRatingOn = " + isTtxRatingOn);
              if (!isTtxRatingOn&&!StateDvrFileList.getInstance(DvrManager.getInstance()).isShowing()) {
                mPWDDialog.handleCallBack(type);
              }
            }
          } else if (data.param1 == CommonIntegration.SVCTX_NTFY_CODE_RATING) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_SVCTX_NOTIFY--->data.param1==SVCTX_NTFY_CODE_RATING");
            if (!mTurnkey.isUKCountry) {
              BannerView bannerView =
                  (BannerView)
                      ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
              if (bannerView != null) { //
                bannerView.updateRatingInfo();
              }
            }
          } else if (data.param1 == CommonIntegration.SVCTX_NTFY_SIGNA_LOSS) {
            PwdDialog mPWDDialog =
                (PwdDialog)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
            if (mPWDDialog != null) {
              Log.d(TAG, "handlerCallbackMsg:  SVCTX_NTFY_SIGNA_LOSS");
              mPWDDialog.setSvctxBlocked(false);
              mPWDDialog.dismiss();
            }
          }
          InfoBarDialog info =
              (InfoBarDialog)
                  ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_INFO_BAR);
          if (info != null) {
            info.handlerMessage(data.param1);
          }

          if (data.param1 == CommonIntegration.SVCTX_NTFY_CODE_VIDEO_FMT_UPDATE) { // timing
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "native component id:" + ComponentsManager.getNativeActiveCompId());
            // Show banner after video fmt update msg, because get value
            // resolution will null before this msg.
//            if (CommonIntegration.getInstance().isCurrentSourceHDMI()) {
//              showBanner();
//            }

            TVAsyncExecutor.getInstance()
                .execute(
                    new Runnable() {

                      @Override
                      public void run() {
                        // to do zoom 1x
                        IntegrationZoom integrationZoom = IntegrationZoom.getInstance(null);
                        if ((integrationZoom.ZOOM_1 != integrationZoom.getCurrentZoom())
                            && integrationZoom.screenModeZoomShow()
                            && !CommonIntegration.getInstance().isPipOrPopState()
                            && ComponentsManager.getNativeActiveCompId() == 0) {
                          integrationZoom.setZoomMode(IntegrationZoom.ZOOM_1);
                          final ZoomTipView mZoomTip =
                              ((ZoomTipView)
                                  ComponentsManager.getInstance()
                                      .getComponentById(NavBasic.NAV_COMP_ID_ZOOM_PAN));
                          if (mZoomTip != null && mZoomTip.getVisibility() == View.VISIBLE) {
                            mDialog
                                .get()
                                .runOnUiThread(
                                    new Runnable() {

                                      @Override
                                      public void run() {
                                        mZoomTip.setVisibility(View.GONE);
                                      }
                                    });
                          }
                        }
                        if (integrationZoom.screenModeZoomShow()
                            && !CommonIntegration.getInstance().isPipOrPopState()) {
                          final SundryShowTextView stxtView =
                              (SundryShowTextView)
                                  ComponentsManager.getInstance()
                                      .getComponentById(NavBasic.NAV_COMP_ID_SUNDRY);
                          if (stxtView != null && stxtView.getVisibility() == View.VISIBLE) {
                            mDialog
                                .get()
                                .runOnUiThread(
                                    new Runnable() {

                                      @Override
                                      public void run() {
                                        stxtView.setVisibility(View.GONE);
                                      }
                                    });
                          }
                        }
                      }
                    });
          }
          VgaPowerManager vga =
              (VgaPowerManager)
                  ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_POWER_OFF);
          if (vga != null) {
            vga.handlerMessage(data.param1);

            // update Menu
            MenuOptionMain menuOptionMain =
                (MenuOptionMain)
                    ComponentsManager.getInstance()
                        .getComponentById(NavBasic.NAV_COMP_ID_MENU_OPTION_DIALOG);

            // fix cr 00612621,00613021,00613258,00613232,00613980
            if (data.param1 == 5) { // signal loss

              if (menuOptionMain != null && menuOptionMain.isVisible()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "-----signal loss-----");
               // menuOptionMain.notifyOptionChanged(MenuOptionMain.OPTION_AUTO_PICTURE);
                //menuOptionMain.notifyOptionChanged(MenuOptionMain.OPTION_DISPLAY_MODE);
              }

              boolean isCurrTv =
                  TVContent.getInstance(mDialog.get())
                      .isCurrentSourceTv();
              boolean isLastVGA =
                  TVContent.getInstance(mDialog.get())
                      .isLastInputSourceVGA();
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "check source isCurrTv ==" + isCurrTv + ",isLastVGA ==" + isLastVGA);
              if (isLastVGA) {
                // if menu is available finish it .fix CR 00611237by
                // sin_biaoqinggao
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "check MenuMain is available ");
              }
              ewsDialog =
                  (EWSDialog)
                      ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_EWS);
              if (ewsDialog != null && ewsDialog.isVisible()) {
                ewsDialog.dismiss();
              }

            } else if (data.param1 == 0) { // signal reconnected
              if (menuOptionMain != null && menuOptionMain.isVisible()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "-----signal reconnected-----");
                //menuOptionMain.notifyOptionChanged(MenuOptionMain.OPTION_AUTO_PICTURE);
                //menuOptionMain.notifyOptionChanged(MenuOptionMain.OPTION_DISPLAY_MODE);
              }
            }
          }
          if (DvrManager.getInstance() != null) {
            DvrManager.getInstance().getController().onTvShow();
          }
          break;
        }
      case TvCallbackConst.MSG_CB_WARNING_MSG:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_WARNING_MSG data.param1>> "+data.param1);
        if(data.param1 == 0 || data.param1 == 1){
          if(!TvSingletons.getSingletons().getTurnkeyUiMainActiviteActive()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TK stop, won't show special service or Ews!");
            return;
          }
        }
        CommonMsgDialog mCommonMsgDialog =
            (CommonMsgDialog)
                ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_DIALOG_MSG);
        if (mCommonMsgDialog != null) {
          ComponentsManager.getInstance().hideAllComponents();
          mCommonMsgDialog.commonMsgHanndler(data.param1, data.param2, data.param3, data.paramStr1);
        }
        break;
      case TvCallbackConst.MSG_CB_SCREEN_SAVER_MSG:
        TwinkleDialog twinkleDialog = (TwinkleDialog) ComponentsManager
                .getInstance().getComponentById(
                        NavBasic.NAV_COMP_ID_TWINKLE_DIALOG);
        if (twinkleDialog != null) {
          twinkleDialog.handleCallBack(data.param1);
        }
        break;
      case TvCallbackConst.MSG_CB_PWD_DLG_MSG:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "receive TvCallbackConst.MSG_CB_PWD_DLG_MSG");
        // PwdDialog mPWDDialog = (PwdDialog) ComponentsManager
        // .getInstance().getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
        // if (mPWDDialog != null) {
        // mPWDDialog.handleCallBack(data.param1);
        // }
        break;
      case TvCallbackConst.MSG_CB_CHANNEL_LIST_UPDATE:
        {
          if (data.param1 == CommonIntegration.getInstance().getSvl()) {
            if (mDialog.get().isUKCountry) {
              UKChannelListDialog mChListDialog =
                  (UKChannelListDialog)
                      ComponentsManager.getInstance()
                          .getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
              if (mChListDialog != null && mChListDialog.isVisible()) {
                mChListDialog.handleCallBack();
              }
            } else {
              ChannelListDialog mChListDialog =
                  (ChannelListDialog)
                      ComponentsManager.getInstance()
                          .getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
              if (mChListDialog != null && mChListDialog.isVisible()) {
                mChListDialog.handleCallBack();
              }
            }
          }
          break;
        }
      case TvCallbackConst.MSG_CB_CHANNELIST:
        {
          if (mDialog.get().isUKCountry) {
            UKChannelListDialog mChListDialog =
                (UKChannelListDialog)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
            if (mChListDialog != null && mChListDialog.isVisible()) {
              mChListDialog.handleCallBack();
            }
          } else {
            ChannelListDialog mChListDialog =
                (ChannelListDialog)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
            if (mChListDialog != null && mChListDialog.isVisible()) {
              mChListDialog.handleCallBack();
            }
            FavoriteListDialog favDialog =
                (FavoriteListDialog)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_FAV_LIST);
            if (favDialog != null && favDialog.isVisible()) {
              favDialog.handleCallBack();
            }
          }
          break;
        }
      case TvCallbackConst.MSG_CB_NFY_UPDATE_TV_PROVIDER_LIST:
        {
          if (CommonIntegration.getInstance().getSvl() == 1) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                "===", "receive MSG_CB_NFY_UPDATE_TV_PROVIDER_LIST may showDVBTInActiveDialog");
            mTurnkey.showDVBTInActiveDialog();
          }
          if(CommonIntegration.getInstance().getSvl() == data.param1){
            if(mDialog.get().isUKCountry){
              UKChannelListDialog mChListDialog = (UKChannelListDialog) ComponentsManager
                      .getInstance().getComponentById(
                              NavBasic.NAV_COMP_ID_CH_LIST);
              if (mChListDialog != null) {
                mChListDialog.channelListRearrangeFav();
              }
            }else{
              ChannelListDialog mChListDialog = (ChannelListDialog) ComponentsManager
                      .getInstance().getComponentById(
                              NavBasic.NAV_COMP_ID_CH_LIST);
              if (mChListDialog != null) {
                mChListDialog.channelListRearrangeFav();
              }
            }
          }
          break;
        }
      case TvCallbackConst.MSG_CB_NFY_UPDATE_SATELLITE_LIST:
        {
          if (mDialog.get().isUKCountry) {
            UKChannelListDialog mChListDialog =
                (UKChannelListDialog)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
            if (mChListDialog != null && mChListDialog.isVisible()) {
              mChListDialog.handleCallBack();
            }
          } else {
            ChannelListDialog mChListDialog =
                (ChannelListDialog)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
            if (mChListDialog != null && mChListDialog.isVisible()) {
              mChListDialog.handleCallBack();
            }
          }

          break;
        }
      case TvCallbackConst.MSG_CB_BANNER_CHANNEL_LOGO:
        {
          if (mDialog.get().isUKCountry) {
            UKChannelListDialog mChListDialog =
                (UKChannelListDialog)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
            if (mChListDialog != null && mChListDialog.isVisible()) {
              mChListDialog.handleCallBack();
            }
          } else {
            ChannelListDialog mChListDialog =
                (ChannelListDialog)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
            if (mChListDialog != null && mChListDialog.isVisible()) {
              mChListDialog.handleCallBack();
            }
          }

          break;
        }
      case TvCallbackConst.MSG_CB_NFY_TUNE_CHANNEL_BROADCAST_MSG:
        {
          break;
        }
      case TvCallbackConst.MSG_CB_CONFIG:
        {
          if (data.param1 == MtkTvConfigMsgBase.ACFG_MSG_PRE_CHG_INPUT) { // change
            // input
            if (ComponentsManager.getActiveCompId() == NavBasic.NAV_COMP_ID_INPUT_SRC) {
              ComponentsManager.updateActiveCompId(true, 0);
            }
            if (!mTurnkey.isUKCountry) {
              BannerView bannerView =
                  (BannerView)
                      ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
              if (bannerView != null) { // tune channel after tune source success
                bannerView.tuneChannelAfterTuneTVSource();
              }
            }
            PwdDialog mPwdDialog =
                (PwdDialog)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
            if (mPwdDialog != null && MtkTvPWDDialog.getInstance().PWDShow() != 0) {
              mPwdDialog.dismiss();
            }
            GingaTvDialog ginga =
                (GingaTvDialog)
                    ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_GINGA_TV);
            if (ginga != null) {
              ginga.handleSvctxMessage(data.param1);
            }

            TVAsyncExecutor.getInstance()
                .execute(
                    new Runnable() {
                      @Override
                      public void run() {
                        // to do zoom 1x
                        IntegrationZoom integrationZoom = IntegrationZoom.getInstance(null);
                        if ((integrationZoom.ZOOM_1 != integrationZoom.getCurrentZoom())
                            && integrationZoom.screenModeZoomShow()) {
                          integrationZoom.setZoomMode(IntegrationZoom.ZOOM_1);
                          final ZoomTipView mZoomTip =
                              ((ZoomTipView)
                                  ComponentsManager.getInstance()
                                      .getComponentById(NavBasic.NAV_COMP_ID_ZOOM_PAN));
                          if (mZoomTip != null && mZoomTip.getVisibility() == View.VISIBLE) {
                            mDialog
                                .get()
                                .runOnUiThread(
                                    new Runnable() {

                                      @Override
                                      public void run() {
                                        mZoomTip.setVisibility(View.GONE);
                                      }
                                    });
                          }
                        }
                        if (integrationZoom.screenModeZoomShow()) {
                          final SundryShowTextView stxtView =
                              (SundryShowTextView)
                                  ComponentsManager.getInstance()
                                      .getComponentById(NavBasic.NAV_COMP_ID_SUNDRY);
                          if (stxtView != null && stxtView.getVisibility() == View.VISIBLE) {
                            mDialog
                                .get()
                                .runOnUiThread(
                                    new Runnable() {

                                      @Override
                                      public void run() {
                                        stxtView.setVisibility(View.GONE);
                                      }
                                    });
                          }
                        }
                      }
                    });
          } else if (data.param1 == MtkTvConfigMsgBase.ACFG_MSG_CHG_INPUT) {
            if (CommonIntegration.getInstance().isDoPIPPOPAction()) {
              return;
            }

            if (!DestroyApp.isCurTaskTKUI()) {
              return;
            }
            /*
             * InputSourceManager.getInstance().scartAutoChanged();
             * InputSourceManager
             * .getInstance().tuneChannelAfterTuneTVSource();
             * ChannelListDialog mChListDialog = (ChannelListDialog)
             * ComponentsManager
             * .getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST
             * ); if (mChListDialog != null && mChListDialog.isShowing()) {
             * mChListDialog.dismiss(); }
             */
          } else if (data.param1 == 10 && (data.param2 == 5 || data.param2 == 6)) {
            if (!com.mediatek.wwtv.setting.util.MenuConfigManager.PICTURE_MODE_dOVI) {
              com.mediatek.wwtv.setting.util.MenuConfigManager.PICTURE_MODE_dOVI = true;
              if (SettingsPreferenceScreen.isSetPrefCret) {
                SettingsPreferenceScreen sps = SettingsPreferenceScreen.getInstance();
                if (sps != null) {
                  sps.notifyPreferenceForVideo();
                }
              }
            }
          } else if (data.param1 == 10 && (data.param2 != 5 && data.param2 != 6)) {
            if (com.mediatek.wwtv.setting.util.MenuConfigManager.PICTURE_MODE_dOVI) {
              com.mediatek.wwtv.setting.util.MenuConfigManager.PICTURE_MODE_dOVI = false;
              if (SettingsPreferenceScreen.isSetPrefCret) {
                SettingsPreferenceScreen sps = SettingsPreferenceScreen.getInstance();
                if (sps != null) {
                  sps.notifyPreferenceForVideo();
                }
              }
            }
          }
          break;
        }
      case TvCallbackConst.MSG_CB_OAD_MSG:
        {
        	 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_OAD_MSG,msg_type:" + data.param1);
             EWSDialog ewsDialog3 = (EWSDialog) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_EWS);
             if(ewsDialog3 != null && ewsDialog3.isShowing() && ewsDialog3.isPlayTone()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_OAD_MSG ewsDialog3 is showing~ ");
                return;
             }
        	 if(data.param1 == 15){
                 SaveValue.setLocalMemoryValue("is_show_OAD_ui", false);
             }else if(data.param1 == 16){
                 SaveValue.setLocalMemoryValue("is_show_OAD_ui", true);
             }
          if(!SaveValue.readLocalMemoryBooleanValue("is_show_OAD_ui")){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Current country is not support OAD!");
            return;
          }
          TurnkeyUiMainActivity activity = mDialog.get();

          if (data.param1 == 12 && SaveValue.readLocalMemoryBooleanValue("showUpgradeMsg")) {

            Toast.makeText(activity, "Upgrade success.", Toast.LENGTH_LONG).show();
            SaveValue.setLocalMemoryValue("showUpgradeMsg", false);
            // reset this flag for oad
            SaveValue.getInstance(mDialog.get()).saveBooleanValue("rejectOadUpdated", false);
            break;
          }

          if (!DestroyApp.isCurOADActivityNew()
                  && data.param1 != 12
                  && data.param1 >= 0 && data.param1 <= 13) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start OAD Activity");
            Intent intent = new Intent(activity, NavOADActivityNew.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("updateType", data.param1);
            activity.startActivity(intent);
          }
          break;
        }
      case TvCallbackConst.MSG_CB_RECORD_NFY:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "RECORD_NFY---->:");
        TvCallbackData dataRecord = (TvCallbackData) msg.obj;

        if (mDialog.get().isShutdownFlow) {
          dataRecord.param3 = 1;
        }
        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_DVR)&&DvrManager.getInstance()!=null) {
          DvrManager.getInstance().handleRecordNTF(dataRecord);
        }
        break;
      case TvCallbackConst.MSG_CB_TIME_SHIFT_NFY:
        if (DvrManager.getInstance().isSuppport()) {
          break;
        }
        break;
      case TvCallbackConst.MSG_CB_NFY_NATIVE_APP_STATUS:
        {
          TvCallbackData apdata = (TvCallbackData) msg.obj;
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(
              TAG, "apdata.param1: " + apdata.param1 + ", apdata.paramBool1: " + apdata.paramBool1);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isShutdownFlow>>>" + mDialog.get().isShutdownFlow);
          if (mDialog.get().isShutdownFlow) {
            if (DestroyApp.isCurOADActivityNew()) {
              return;
            }

            if (DestroyApp.isCurEPGActivity()) { // fix Cr:DTV00716758
              // DTV00726928
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "destory EPG");
              TurnkeyUiMainActivity.resumeTurnkeyActivity(mDialog.get());
              return;
            }
          } else if (MtkTvNativeAppId.MTKTV_NATIVE_APP_ID_NAV == apdata.param1) {
            if (apdata.paramBool1 == true && DestroyApp.isCurActivityTkuiMainActivity()) {
              Intent mIntent = new Intent();
              mIntent.putExtra(
                  TurnkeyUiMainActivity.class.getSimpleName(), DestroyApp.getRunningActivity());

              TurnkeyUiMainActivity.resumeTurnkeyActivity(mDialog.get());
            }
          }
          break;
        }
      case TvCallbackConst.MSG_CB_AV_MODE_MSG:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handle MSG_CB_AV_MODE_MSG");
        if (!mTurnkey.isUKCountry) {
          BannerView bannerView =
              (BannerView)
                  ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
          if (bannerView != null && bannerView.isVisible()) {
            // bannerView.show(false, -1, false);
            bannerView.updateBasicBarAudio();
          }
        }
        break;
      case TvCallbackConst.MSG_CB_FVP_MSG:
        if (MtkTvConfig.getInstance().getCountry().equals(EpgType.COUNTRY_UK)) {
          FVP fvp =
              (FVP)
                  ComponentsManager.getInstance().getComponentById(NavBasic.NAV_NATIVE_COMP_ID_FVP);
          if (fvp != null) {
            fvp.handlerFVPMessage(data.param1, data.param2);
          }
        }
        if(TurnkeyUiMainActivity.isUKCountry) {
          UkBannerView bannerView =
                  (UkBannerView)
                          ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
          if (bannerView != null) {
            bannerView.handlerFVPMessage(data.param1, data.param2);
          }
        }
        break;
      case TvCallbackConst.MSG_CB_BML_MSG:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_BML_MSG");
        BMLMain bml =
            (BMLMain)
                ComponentsManager.getInstance().getComponentById(NavBasic.NAV_NATIVE_COMP_ID_BML);
        if (bml != null) {
          bml.handleBMLMessage(data.param1, data.param2);
        }
        break;
      case TvCallbackConst.MSG_CB_GINGA_UPDATE_MSG:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_GINGA_UPDATE_MSG");
        GingaTvDialog mGingaTvDialog =
            (GingaTvDialog)
                ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_GINGA_TV);
        if (mGingaTvDialog != null) {
          mGingaTvDialog.handleLoadingMessage(data.param1);
        }
        break;
      case TvCallbackConst.MSG_CB_ON_SCREEN_A3WAM_MSG:
        AtscInteractive atsc3 = (AtscInteractive) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_NATIVE_COMP_ID_ATSC3);
        if(atsc3!=null){
            atsc3.handlerATSC3Message(data);
        }
        break;
      default:
        break;
    }
  }

  private void showBanner() {
    boolean isLiveTVPause = mDialog.get().isLiveTVPause();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MSG_CB_SVCTX_NOTIFY--->isLiveTVPause=" + isLiveTVPause);
    if (!isLiveTVPause) {
      if (!mTurnkey.isUKCountry) {
        BannerView bannerView =
            (BannerView)
                ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
        if (bannerView != null) { //
          bannerView.showBannerAfterSVCChange();
        }
      } else {
        UkBannerView bannerView =
            (UkBannerView)
                ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
        if (bannerView != null) { //
          bannerView.showBasicBanner(false);
        }
      }
    }
  }

  private void handlerMessages(Message msg) {
    switch (msg.what) {
      case RESET_LAYOUT:
        resetTurnkeyLayout();
        break;
      case MSG_START_INPUT_SETUP:
        {
          TvInputInfo info =
              InputSourceManager.getInstance().getTvInputInfo(InputSourceManager.MAIN);
          if ((info != null) && (!TIFChannelManager.getInstance(mDialog.get()).isChannelsExist())) {
            if (info.createSetupIntent() != null) {
              mDialog.get().startActivity(info.createSetupIntent());
            }
          }
          break;
        }
      case MSG_SELECT_CURRENT_CHANNEL:
        if (MtkTvInputSource.INPUT_TYPE_TV.equalsIgnoreCase(
            InputSourceManager.getInstance()
                .getCurrentInputSourceName(CommonIntegration.getInstance().getCurrentFocus()))) {
          TIFChannelManager.getInstance(mDialog.get()).selectCurrentChannelWithTIF();
        }
        break;
      case MSG_SHOW_FAV_LIST_FULL_TOAST:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in MSG_SHOW_FAV_LIST_FULL_TOAST");
        CommonIntegration.getInstance().showFavFullMsg();
        CommonIntegration.getInstance().setShowFAVListFullToastDealy(false);
        break;
      case MSG_CLOSE_SOURCE:
        Commands.stopCmd();

        if (MarketRegionInfo.REGION_US == MarketRegionInfo.getCurrentMarketRegion()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "MSG_CLOSE_SOURCE, EASSetAndroidLaunchStatus(true)");
          mDialog.get().mEas.EASSetAndroidExitEASNoChangeChannelStatus(true);
          mDialog.get().mEas.EASSetAndroidLaunchStatus(true);
        }

        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_GINGA)) {
          MtkTvGinga.getInstance().stopGinga();
        }

        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT)) {
          if (mDialog.get().mCommonIntegration.isPipOrPopState()) {
            if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)) {
              ((MultiViewControl)
                      ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_POP))
                  .setVisibility(View.INVISIBLE);
              ((MultiViewControl)
                      ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_POP))
                  .setNormalTvModeWithLauncher(false);
            }
          } else {
            InputSourceManager.getInstance().stopSession();
          }
        }

        /** the method used to stop dvr play when comes to luncher */
        if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "turnkey callback : stop dvr ");
          StateDvrPlayback.getInstance().stopDvrFilePlay(true);
        }

        mDialog.get().mAudioManager.abandonAudioFocus();

        // Reset Overscan

        break;
      case ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ChannelListDialog.MESSAGE_DEFAULT_CAN_CHANGECHANNEL_DELAY reach");
        mDialog.get().mTurnkeyReceiver.mCanChangeChannel = true;
        break;

      case ChannelListDialog.FIND_CHANNELLIST:
        showEditTextAct((Intent) msg.obj);
        break;
      case EPGConfig.EPG_MUTE_VIDEO_AND_AUDIO:
        boolean isBarkerChannel = (Boolean) msg.obj;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EPG_MUTE_VIDEO_AND_AUDIO isBarkerChannel=" + isBarkerChannel);
        mDialog.get().muteVideoAndAudio(isBarkerChannel);
        break;
      case MSG_RESUME_NATIVE_HBBTV:
        MtkTvHBBTV.getInstance()
            .setExternalData(
                MtkTvHBBTVBase.ExternalDataType.EXTERNAL_DATA_TYPE_LIVE_TV.ordinal(),
                new int[] {1}); // must in main thread
        break;
      case MSG_PAUSE_NATIVE_HBBTV:
        MtkTvHBBTV.getInstance()
            .setExternalData(
                MtkTvHBBTVBase.ExternalDataType.EXTERNAL_DATA_TYPE_LIVE_TV.ordinal(),
                new int[] {0});
        break;
      case MSG_TIME_SHIFT_NOT_AVAILABLE:
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "msg.arg1 ==" + msg.arg1);
        if (msg.arg1 == 77) {
          isfromturnkeyonstop = true;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "isfromturnkeyonstop ==" + isfromturnkeyonstop);
        if (msg.arg1 == 99 && isfromturnkeyonstop) {
          Toast.makeText(
                  TurnkeyUiMainActivity.getInstance(),
                  TurnkeyUiMainActivity.getInstance()
                      .getResources()
                      .getString(R.string.timeshift_stopped),
                  Toast.LENGTH_SHORT)
              .show();
          com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "timeshiftstopped");
          isfromturnkeyonstop = false;
        }

        if (msg.arg1 == 88) {
          isfromturnkeyonstop = false;
        }
        break;
      default:
        break;
    }
  }

  protected void showEditTextAct(Intent intent) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showEditTextAct.FIND_CHANNELLIST");

    mDialog.get().startActivityForResult(intent, NavBasic.NAV_REQUEST_CODE);
  }

  /** show inactive channels dialog */
  protected void showDVBTInactiveChannelsConfirmDialog() {
    final TurnkeyUiMainActivity activity = mDialog.get();

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDVBTInactiveChannelsConfirmDialog()-TKUI");
    boolean isTVSource = CommonIntegration.getInstance().isCurrentSourceTv();
    if (!isTVSource) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDVBTInactiveChannelsConfirmDialog(),Not TV Source");
      return;
    }

    if (ComponentsManager.getInstance()
        .getComponentById(NavBasic.NAV_COMP_ID_PVR_TIMESHIFT)
        .isVisible()) {

      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDVBTInactiveChannelsConfirmDialog(),PVR is Running");
      return;
    }

    if (activity != null) {
      if (!activity.isResumed()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showDVBTInactiveChannelsConfirmDialog(),Nav Not Resumed");
        return;
      }

      activity.runOnUiThread(
          () -> {
            DvbtInactiveChannelConfirmDialog dialog =
                DvbtInactiveChannelConfirmDialog.getInstance(activity);
            dialog.showConfirmDialog();
          });
    }
  }

  protected void resetTurnkeyLayout() {
    TurnkeyUiMainActivity activity = mDialog.get();
    if (activity != null) {
      WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
      lp.width = lp.height = WindowManager.LayoutParams.FILL_PARENT;
      lp.x = lp.y = 0;
      activity.getWindow().setAttributes(lp);
      activity.OriginalView.invalidate();
    }
  }
} // end of InternalHandler
