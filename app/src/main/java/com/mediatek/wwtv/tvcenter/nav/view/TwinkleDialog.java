package com.mediatek.wwtv.tvcenter.nav.view;

import android.content.Context;
import android.media.tv.TvInputManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.mediatek.twoworlds.tv.MtkTvScreenSaverBase;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.widget.view.DiskSettingDialog;
import com.mediatek.wwtv.setting.widget.view.DiskSettingSubMenuDialog;
import com.mediatek.wwtv.setting.widget.view.ScheduleListDialog;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.dvr.controller.UImanager;
import com.mediatek.wwtv.tvcenter.dvr.ui.ScheduleListItemDialog;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.input.AbstractInput;
import com.mediatek.wwtv.tvcenter.nav.input.InputUtil;
import com.mediatek.wwtv.tvcenter.nav.util.BannerImplement;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener.ICStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIMainDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
//add by xun
import com.ist.systemuilib.tvapp.TvToSystemManager;
//end xun
import java.util.List;

public class TwinkleDialog extends NavBasicDialog implements ICStatusListener {

  private static final String TAG = "TwinkleDialog";
  //add by xun
  private TvToSystemManager mTvToSystemManager;
  //end xun
  public static final int MSG_STR_ID_0_EMPTY = 0;
  public static final int MSG_STR_ID_1_NO_SIGNAL = 1;
  public static final int MSG_STR_ID_2_SCAN_CH = 2;
  public static final int MSG_STR_ID_3_GETTING_DATA = 3;
  public static final int MSG_STR_ID_4_LOCKED_CH = 4;
  public static final int MSG_STR_ID_5_LOCKED_PROG = 5;
  public static final int MSG_STR_ID_6_LOCKED_INP = 6;
  public static final int MSG_STR_ID_7_NO_EVN_TILE = 7;
  public static final int MSG_STR_ID_8_HIDDEN_CH = 8;
  public static final int MSG_STR_ID_9_AUDIO_PROG = 9;
  public static final int MSG_STR_ID_10_NO_AUDIO_VIDEO = 10;
  public static final int MSG_STR_ID_11_NO_EVN_DTIL = 11;
  public static final int MSG_STR_ID_12_NO_CH_DTIL = 12;
  public static final int MSG_STR_ID_13_NO_AUDIO_STRM = 13;
  public static final int MSG_STR_ID_14_NO_VIDEO_STRM = 14;
  public static final int MSG_STR_ID_15_PLS_WAIT = 15;
  public static final int MSG_STR_ID_16_VIDEO_NOT_SUPPORT = 16;
  public static final int MSG_STR_ID_17_HD_VIDEO_NOT_SUPPORT = 17;
  public static final int MSG_STR_ID_18_NON_BRDCSTING = 18;
  public static final int MSG_STR_ID_19_NO_CH_IN_LIST = 19;
  public static final int MSG_STR_ID_20_TTX_SBTI_X_RATED_BLOCKED = 20;
  public static final int MSG_STR_ID_21_SCRAMBLED = 21;
  public static final int MSG_STR_ID_22_DVBS_MOVING_POSITIONER = 22;
  public static final int MSG_STR_ID_23_SERVICE_NOT_RUNNING = 23;
  private static final int MSG_STR_ID_255_LAST_VALID_ENTRY = 255;

  private boolean screenOnFlag = false;
  private boolean isHostQuietTuneStatusForZiggo = false;

  private TextView mTitle;
  private TextView mMessage;
  static int lastIndex = MSG_STR_ID_0_EMPTY;
  private Handler handler = new Handler();
  //private static final int SHOW_TWINKLE = 100;

  private MtkTvScreenSaverBase mScreenSaver;

  private CommonIntegration commonIntegration;
  private ComponentsManager comManager;
  private InputSourceManager inputSourceManager;
  
  private static final int RETRIEVING_DATA_DELAY = 100;// 400ms
  
  private BannerImplement mBannerImplement = null;

  public TwinkleDialog(Context context) {
    this(context, R.style.nav_dialog);
  }

  public TwinkleDialog(Context context, int theme) {
    super(context, theme);
    this.componentID = NAV_COMP_ID_TWINKLE_DIALOG;
    ComponentStatusListener.getInstance().addListener(
        ComponentStatusListener.NAV_COMPONENT_HIDE, this);
    ComponentStatusListener.getInstance().addListener(
        ComponentStatusListener.NAV_COMPONENT_SHOW, this);
    ComponentStatusListener.getInstance().addListener(
        ComponentStatusListener.NAV_RESUME, this);
    ComponentStatusListener.getInstance().addListener(
        ComponentStatusListener.NAV_VGA_NO_SIGNAL, this);
    ComponentStatusListener.getInstance().addListener(
        ComponentStatusListener.NAV_DVR_FILELIST_HIDE, this);
    ComponentStatusListener.getInstance().addListener(
            ComponentStatusListener.NAV_PVR_DIALOG_HIDE, this);
    ComponentStatusListener.getInstance().addListener(
        ComponentStatusListener.NAV_HOST_TUNE_STATUS, this);

    mScreenSaver = new MtkTvScreenSaverBase();
    commonIntegration = CommonIntegration.getInstance();
    comManager = ComponentsManager.getInstance();
    inputSourceManager = InputSourceManager.getInstance();
    mBannerImplement = BannerImplement.getInstanceNavBannerImplement(mContext);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.nav_twinkle_dialog);
    mTitle = (TextView)findViewById(R.id.simple_dialog_tittle);
    mMessage = (TextView)findViewById(R.id.simple_dialog_content);
  }

  @Override
  public void dismiss() {
    super.dismiss();
    com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "dismiss.");
  }
  
  @Override
  public void show() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "show.  lastIndex:"+lastIndex);
    //add by xun
    if(mTvToSystemManager == null){
      mTvToSystemManager = TvToSystemManager.getInstance();
    }
    if(mTvToSystemManager != null && mTvToSystemManager.isUseCustomizeUi()) {
      return;
    }
    //end xun

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"DvbtInactiveChannelConfirmDialog.getInstance(mContext).isShowing():"+DvbtInactiveChannelConfirmDialog.getInstance(mContext).isShowing());
    if (isHostQuietTuneStatusForZiggo) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isHostQuietTuneStatusForZiggo, return !");
      return;
    }
    if(DvbtInactiveChannelConfirmDialog.getInstance(mContext).isShowing()){
      if (isShowing()) {
        dismiss();
      }
       return;
      }
    AbstractInput input = InputUtil.getInput(inputSourceManager.getCurrentInputSourceHardwareId());

    if (input != null && (input.isVGA() || input.isComponent() || input.isComposite()) && lastIndex != MSG_STR_ID_6_LOCKED_INP) {
      if (input.getState() != TvInputManager.INPUT_STATE_CONNECTED && !commonIntegration.isCHChanging()) {
        lastIndex = MSG_STR_ID_1_NO_SIGNAL;
      }
    }

    if(lastIndex == MSG_STR_ID_0_EMPTY || lastIndex ==MSG_STR_ID_255_LAST_VALID_ENTRY) {
      if (isShowing()) {
        dismiss();
      }
      return;
    }
    if (commonIntegration.is3rdTVSource()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "current source is is3rdTVSource!");
      PwdDialog pwdDialog = (PwdDialog) ComponentsManager
          .getInstance().getComponentById(
              NavBasic.NAV_COMP_ID_PWD_DLG);
      boolean isBlockFor3rd = pwdDialog.isContentBlock(true);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "SHOW_TWINKLE||isBlockFor3rd ="
          + isBlockFor3rd + "||SourceBlocked ="
          + commonIntegration.isCurrentSourceBlockEx());
      if (isBlockFor3rd
          && commonIntegration.isCurrentSourceBlockEx()
          && !(isComponentsShow() && !isShowing())
          && DestroyApp.isCurActivityTkuiMainActivity()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "SHOW_TWINKLE||3rd Source Blocked");
        lastIndex = MSG_STR_ID_6_LOCKED_INP;
      }
      return;
    }
    
    if (!isComponentsShow()
        && !isUImanagerShowing()
        && DestroyApp.isCurActivityTkuiMainActivity()
        && (NAV_NATIVE_COMP_ID_GINGA != ComponentsManager.getNativeActiveCompId())
        && !isCommonDialogShowing()) {
      if (screenOnFlag == true) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showSpecialView||getScrnSvrMsgID =" + mScreenSaver.getScrnSvrMsgID());
        lastIndex = mScreenSaver.getScrnSvrMsgID();
        screenOnFlag = false;
      }
    } else {
      return;
    }
    super.show();
    initData();
  }

  public void reset() {
    dismiss();
    lastIndex = MSG_STR_ID_0_EMPTY;
  }
  
  public void handleCallBack(int type) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " handleCallBack MSG_CB_SCREEN_SAVER_MSG type =" + type
        + ",lastIndex==" + lastIndex);
    lastIndex = type;
    if (lastIndex != MSG_STR_ID_3_GETTING_DATA) {
      show();
    } else {
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "delay to show retrieving data!");
          show();
        }
      }, RETRIEVING_DATA_DELAY);
    }
  }

  public void handlerHbbtvMessage(int type, int message) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerHbbtvMessage, type=" + type + ", message="
        + message);
    switch (type) {
    case 1:// HBBTV_COMP_ACTIVE
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerHbbtvMessage, hbbtv is showing");
      dismiss();
      break;
    case 4:// HBBTV_APP_TERMINATED
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerHbbtvMessage, hbbtv is hiding");
      show();
      break;
    default:
      break;
    }

  }

  @Override
  public void updateComponentStatus(int statusID, int value) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "twinkle||updateComponentStatus||statusID =" + statusID);
    Mheg5 mheg5 = (Mheg5) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_NATIVE_COMP_ID_MHEG5);
    switch (statusID) {
      case ComponentStatusListener.NAV_COMPONENT_HIDE:
      case ComponentStatusListener.NAV_PVR_DIALOG_HIDE:
        if (value == NavBasic.NAV_COMP_ID_TWINKLE_DIALOG) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "twinkle hide itself !!!");
          return;
        }
//        handler.post(new Runnable() {
//
//          @Override
//          public void run() {
            show();
//          }
//        });
        break;
      case ComponentStatusListener.NAV_RESUME:
        if(!isComponentsShow() && ComponentsManager.getActiveCompId()
            != NavBasic.NAV_COMP_ID_TWINKLE_DIALOG) {
          show();
        } else if (ComponentsManager.getActiveCompId() == NavBasic.NAV_NATIVE_COMP_ID_MHEG5) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mheg5.getIsMheg5UiShow()=" + (mheg5==null?mheg5:mheg5.getIsMheg5UiShow()));
            if (mheg5 != null && !mheg5.getIsMheg5UiShow()) {
                show();
            }
        }
        break;
      case ComponentStatusListener.NAV_COMPONENT_SHOW:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "NAV_COMPONENT_SHOW||value =" + value);
        PwdDialog pwdDialog = (PwdDialog) comManager.getComponentById(NAV_COMP_ID_PWD_DLG);
        int activeCompId = ComponentsManager.getActiveCompId();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ComponentsManager.getActiveCompId()" + activeCompId);
        if (activeCompId == NavBasic.NAV_NATIVE_COMP_ID_HBBTV){ //if hbbtv not notify this message
          return;
        }
        boolean isPwdShowing = pwdDialog.isShowing();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "NAV_COMPONENT_SHOW||isPwdShowing =" + isPwdShowing);

        if (lastIndex==MSG_STR_ID_1_NO_SIGNAL && value == NavBasic.NAV_COMP_ID_TIFTIMESHIFT_VIEW) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Tiftimeshiftview showing and no signal need show.!");
            return;
        }

        if ((value!=NAV_COMP_ID_TWINKLE_DIALOG)||isPwdShowing){
            if ((mheg5 != null && ComponentsManager.getActiveCompId() == NavBasic.NAV_NATIVE_COMP_ID_MHEG5 && !mheg5.getIsMheg5UiShow())) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "NAV_COMPONENT_SHOW mheg5 active but no UI show twinkle");
                if(!isShowing()){
                    show();
                }
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "NAV_COMPONENT_SHOW dismiss twinkle");
                if (isShowing()) {
                    dismiss();
                }
            }

        } else {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "twinkle show itself !");
        }
        break;
      case ComponentStatusListener.NAV_POWER_ON:
        screenOnFlag = true;
        break;
      case ComponentStatusListener.NAV_VGA_NO_SIGNAL:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "twinkle handle message NAV_VGA_NO_SIGNAL!");
        handleCallBack(MSG_STR_ID_1_NO_SIGNAL);
        break;
      case ComponentStatusListener.NAV_DVR_FILELIST_HIDE:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DvrFileListHide");
        if (StateDvr.getInstance() != null
            && StateDvr.getInstance().isRecording()) {
          show();
        }
        break;
      case ComponentStatusListener.NAV_HOST_TUNE_STATUS:
        isHostQuietTuneStatusForZiggo = value == 1;
        break;
      default:
        break;
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TwinkleDialog onKeyDown."+keyCode);

    /* Back Key to TurnkeyUiMainActivity */
    if (TurnkeyUiMainActivity.getInstance() != null) {
//      if (keyCode != KeyEvent.KEYCODE_BACK){
//        dismiss();
//      }
      return TurnkeyUiMainActivity.getInstance().onKeyHandler(keyCode, event);
    }
    return false;
  }

	private boolean isUImanagerShowing() {
		if (UImanager.showing||
				(StateDvrPlayback.getInstance()!=null
				&&
				StateDvrPlayback.getInstance().isRunning()&&lastIndex!=9)
                ||DiskSettingDialog.getDiskSettingDialog()!=null&&DiskSettingDialog.getDiskSettingDialog().isShowing()
                || DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog() != null && DiskSettingSubMenuDialog.getmDiskSettingSubMenuDialog().isShowing()
				||ScheduleListItemDialog
                    .getInstance() != null&&ScheduleListItemDialog.getInstance().isShowing()
                 ||ScheduleListDialog.getDialog() != null
                    && ScheduleListDialog.getDialog().isShowing()) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isUImanagerShowing true");
			return true;
		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isUImanagerShowing false");
			return false;
		}
	}

  private boolean isComponentsShow() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isComponentsShow");
    List<Integer> cpmIDs = ComponentsManager.getInstance()
        .getCurrentActiveComps();
    //boolean coexitComp = true;
    int typeComponentsShow = -1;
    for (Integer id : cpmIDs) {
	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isComponentsShow  id:"+id);
      if (id == NAV_COMP_ID_PVR_TIMESHIFT) {
        // NAV_COMP_ID_PVR_TIMESHIFT ==
        // ComponentsManager.getActiveCompId()
        //coexitComp = false;
        typeComponentsShow = 0;
        ComponentsManager.updateActiveCompId(false, id);
      }else if (id == NAV_COMP_ID_TIFTIMESHIFT_VIEW){
        typeComponentsShow = 1;
      }else{
        typeComponentsShow = 2;
		break;
      }
    }
	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isComponentsShow   typeComponentsShow:"+typeComponentsShow);
    if (typeComponentsShow ==  0
        && (StateDvr.getInstance() != null && StateDvr.getInstance()
            .isSmallCtrlBarShow())
        && ((StateDvrFileList.getInstance() == null) || (!StateDvrFileList
            .getInstance().isShowing()))) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isComponentsShow false 2");
      return false;
    } else if (SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START) && typeComponentsShow != 2) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isComponentsShow false 0");
      return false;
    } else if (!ComponentsManager.getInstance().isComponentsShowIgnoreTwinkle()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isComponentsShow false 1");
      return false;
    } else if(StateDvrPlayback.getInstance()!=null&&StateDvrPlayback.getInstance().isRunning()) {
      CIMainDialog ciMainDialog = (CIMainDialog) ComponentsManager.getInstance().getComponentById(NAV_COMP_ID_CI_DIALOG);
      SundryShowTextView showTextView = (SundryShowTextView) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_SUNDRY);
      boolean hasComponentsShowWhenPlayback = SaveValue.readWorldBooleanValue(mContext, "is_menu_show")
              || showTextView != null && showTextView.isVisible()
              || ciMainDialog != null && ciMainDialog.isDialogIsShow();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isComponentsShow false 3, hasComponentsShowWhenPlayback:" + !hasComponentsShowWhenPlayback);
      return hasComponentsShowWhenPlayback;
    }else{
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isComponentsShow true");
      return true;
    }
  }

  /*private boolean isPvrOrDvrShow() {
    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_DVR)) {
      boolean isDvrManagerPvrDialogShow = DvrManager.getInstance().isPvrDialogShow
          || (StateDvr.getInstance() != null && StateDvr
              .getInstance().isRecording());
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isPvrOrDvrShow isDvrManagerPvrDialogShow=="
          + isDvrManagerPvrDialogShow);
      return isDvrManagerPvrDialogShow;
    }
    return false;
  }*/

  private boolean isCommonDialogShowing() {
    boolean result = false;
    TurnkeyUiMainActivity activity = (TurnkeyUiMainActivity) mContext;
    if (activity != null && activity.getConfirmDialog() != null) {
      result = activity.getConfirmDialog().isConfirmDialogShowing();
    }
    return result;
  }

  private boolean isVGADisConnected() {
    AbstractInput input = InputUtil.getInput(
        inputSourceManager.getCurrentInputSourceHardwareId());
    return input != null && input.isVGA() && input.getState() != TvInputManager.INPUT_STATE_CONNECTED;
  }

  public static void hideTwinkle() {
    TwinkleDialog twinkleDialog = ((TwinkleDialog) ComponentsManager.getInstance()
        .getComponentById(NavBasic.NAV_COMP_ID_TWINKLE_DIALOG));
    if(twinkleDialog != null && twinkleDialog.isShowing()) {
      twinkleDialog.dismiss();
    }
  }

  public static void showTwinkle() {
    ComponentsManager.getInstance().showNavComponent(NAV_COMP_ID_TWINKLE_DIALOG);
  }
  
  private void initData() {
    int titleId = -1;
    int messageId = -1;
    switch (lastIndex) {
      case MSG_STR_ID_0_EMPTY:
        break;
      case MSG_STR_ID_1_NO_SIGNAL:
        if(commonIntegration.isCurrentSourceTv()) {
          titleId = R.string.twinkle_dialog_no_signal_title;
          messageId = R.string.twinkle_dialog_no_signal_msg;
        } else {
          titleId = R.string.twinkle_dialog_no_signal_title;
          messageId = R.string.twinkle_dialog_no_signal_nontv_msg;
        }
        break;
      case MSG_STR_ID_2_SCAN_CH:
        titleId = R.string.twinkle_dialog_please_scan_channels_title;
        messageId = R.string.twinkle_dialog_please_scan_channel_msg;
        break;
      case MSG_STR_ID_3_GETTING_DATA:
        titleId = R.string.nav_Retrieving_Data;
        messageId = R.string.twinkle_dialog_please_scan_channels_msg;
        break;
      case MSG_STR_ID_4_LOCKED_CH:
        titleId = R.string.twinkle_dialog_channel_lock_title;
        messageId = R.string.twinkle_dialog_channel_lock_msg;
        break;
      case MSG_STR_ID_5_LOCKED_PROG:
        messageId = R.string.twinkle_dialog_program_lock_msg;
        String ratingString = mBannerImplement.getProgramRating();
        if(ScanContent.isZiggoUPCOp()) {
          titleId = R.string.twinkle_dialog_program_lock_title;
        } else {
          if(!TextUtils.isEmpty(ratingString) &&
              !TextUtils.equals(mContext.getString(R.string.rating_not_defined), ratingString)) {
            String titleString = mContext.getString(R.string.twinkle_dialog_program_lock_value, 
                mBannerImplement.getProgramRating());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "titleString:"+titleString);
            mTitle.setText(titleString);
            mTitle.setVisibility(View.VISIBLE);
            titleId = 0;
          } else {
            titleId = R.string.twinkle_dialog_program_lock_title;
          }
        }
        break;
      case MSG_STR_ID_6_LOCKED_INP:
        titleId = R.string.twinkle_dialog_input_lock_title;
        messageId = R.string.twinkle_dialog_input_lock_msg;
        break;
      case MSG_STR_ID_7_NO_EVN_TILE:
        titleId = R.string.nav_No_program_title;
        messageId = R.string.nav_No_program_title;
        break;
      case MSG_STR_ID_8_HIDDEN_CH:
        titleId = R.string.nav_hidden_channel;
        messageId = R.string.twinkle_dialog_hidden_channel_msg;
        break;
      case MSG_STR_ID_9_AUDIO_PROG:
        titleId = R.string.nav_Audio_Program;
        messageId = R.string.twinkle_dialog_audio_program_msg;
        break;
      case MSG_STR_ID_10_NO_AUDIO_VIDEO:
        titleId = R.string.nav_No_Audio_and_Video;
        messageId = R.string.twinkle_dialog_no_audio_video_msg;
        break;
      case MSG_STR_ID_11_NO_EVN_DTIL:
        titleId = R.string.nav_No_program_details;
        messageId = R.string.nav_No_program_details;
        break;
      case MSG_STR_ID_12_NO_CH_DTIL:
        titleId = R.string.nav_No_channel_details;
        messageId = R.string.nav_No_channel_details;
        break;
      case MSG_STR_ID_13_NO_AUDIO_STRM:
        titleId = R.string.nav_No_Audio_Stream;
        messageId = R.string.nav_No_Audio_Stream;
        break;
      case MSG_STR_ID_14_NO_VIDEO_STRM:
        titleId = R.string.nav_No_Video_Stream;
        messageId = R.string.nav_No_Video_Stream;
        break;
      case MSG_STR_ID_15_PLS_WAIT:
        titleId = R.string.twinkle_dialog_please_wait_title;
        messageId = R.string.twinkle_dialog_please_wait_msg;
        break;
      case MSG_STR_ID_16_VIDEO_NOT_SUPPORT:
        titleId = R.string.nav_Video_not_Support;
        messageId = R.string.twinkle_dialog_video_not_support_msg;
        break;
      case MSG_STR_ID_17_HD_VIDEO_NOT_SUPPORT:
        titleId = R.string.nav_Video_not_Support;
        messageId = R.string.twinkle_dialog_video_not_support_msg;
        break;
      case MSG_STR_ID_18_NON_BRDCSTING:
        titleId = R.string.nav_Non_Broadcasting;
        messageId = R.string.twinkle_dialog_non_broadcasting_msg;
        break;
      case MSG_STR_ID_19_NO_CH_IN_LIST:
        titleId = R.string.nav_No_Channel;
        messageId = R.string.twinkle_dialog_no_channel_msg;
        break;
      case MSG_STR_ID_20_TTX_SBTI_X_RATED_BLOCKED:
        titleId = R.string.twinkle_dialog_program_lock_title_non_ziggo_title;
        messageId = R.string.nav_This_content_is_blocked;
        break;
      case MSG_STR_ID_21_SCRAMBLED:
        titleId = R.string.twinkle_dialog_scrambled_title;
        messageId = R.string.twinkle_dialog_scrambled_msg;
        break;
      case MSG_STR_ID_22_DVBS_MOVING_POSITIONER:
        titleId = R.string.twinkle_dialog_moving_positioner_title;
        messageId = R.string.twinkle_dialog_moving_positioner_msg;
        break;
      case MSG_STR_ID_23_SERVICE_NOT_RUNNING:
        titleId = R.string.nav_Service_Not_Running;
        messageId = R.string.twinkle_dialog_nav_service_not_running_msg;
        break;
      case MSG_STR_ID_255_LAST_VALID_ENTRY:
        break;
      default:
        break;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initData:");
    if(titleId == -1) {
      mTitle.setVisibility(View.GONE);
    } else if(titleId != 0){
      mTitle.setText(titleId);
      mTitle.setVisibility(View.VISIBLE);
    }
    if(messageId == -1) {
      mMessage.setVisibility(View.GONE);
    } else if(messageId != 0){
      mMessage.setText(messageId);
      mMessage.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public boolean deinitView(){
    super.deinitView();
    ComponentStatusListener.getInstance().removeListener(this);
//    ComponentStatusListener.getInstance().removeAll();
    com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "deinitView lastIndex:"+lastIndex);
    return false;
  }
}