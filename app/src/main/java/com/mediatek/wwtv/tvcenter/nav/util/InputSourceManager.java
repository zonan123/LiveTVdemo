
package com.mediatek.wwtv.tvcenter.nav.util;

import com.mediatek.wwtv.rxbus.MainActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Bundle;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import android.net.Uri;
import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.HdmiTvClient;
import android.hardware.hdmi.HdmiTvClient.InputChangeListener;
import android.hardware.hdmi.HdmiTvClient.SelectCallback;
import android.hardware.hdmi.IHdmiControlService;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputManager.TvInputCallback;

import com.android.tv.onboarding.SetupSourceActivity;
import com.mediatek.twoworlds.tv.SystemProperties;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.MtkTvInputSource;
import com.mediatek.twoworlds.tv.MtkTvMultiView;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.commonview.TvSurfaceView;
import com.mediatek.wwtv.tvcenter.nav.input.AbstractInput;
import com.mediatek.wwtv.tvcenter.nav.input.AtvInput;
import com.mediatek.wwtv.tvcenter.nav.input.DtvInput;
import com.mediatek.wwtv.tvcenter.nav.input.HdmiInput;
import com.mediatek.wwtv.tvcenter.nav.input.InputUtil;
import com.mediatek.wwtv.tvcenter.nav.inputpanel.IInputsPanelView;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.Constants;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.epg.EPGManager;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.SystemsApi;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;

public class InputSourceManager {// NOPMD
  private static final String TAG = InputSourceManager.class.getSimpleName();

  public static final String MAIN = MtkTvInputSource.INPUT_OUTPUT_MAIN;
  public static final String SUB = MtkTvInputSource.INPUT_OUTPUT_SUB;
  public static final Uri mUriMain = Uri.parse("content://main");
  public static final Uri mUriSub = Uri.parse("content://sub");
  public static final String PORT = "Port";
  private static final String PATH_CHANNEL = "channel";
  private static final String DEFAULT_INPUT_ID = "com.mediatek.tvinput/.tuner.TunerInputService/HW0";
  private static final String DEFAULT_ATV_INPUT_ID = "com.mediatek.tvinput/.tuner.TunerInputService/HW1";

  public static final int INPUT_HIDE = 0;
  public static final int INPUT_DISABLE = 1;
  public static final int INPUT_ENABLE = 2;

  private static InputSourceManager instance = new InputSourceManager(DestroyApp.appContext);

  private Context mContext = null;
  private TvInputManager mTvInputManager = null;
  private TvInputInfo mMainTvInputInfo = null;
  private TvInputInfo mSubTvInputInfo = null;

  private final MtkTvInputSource mTvInputSource = MtkTvInputSource.getInstance();

  private IInputsPanelView mInputsPanelView;

  private final SaveValue mSaveValue;

  public static long mInputKeyDownTime;

  private static final String MAIN_SOURCE_NAME = "multi_view_main_source_name";
  private static final String SUB_SOURCE_NAME = "multi_view_sub_source_name";
  private static final String MAIN_SOURCE_HARDWARE_ID = "multi_view_main_source_hardware_id";
  private static final String SUB_SOURCE_HARDWARE_ID = "multi_view_sub_source_hardware_id";
  private String focusWithChangeHdmi = "main";

  private Handler mHandler;

  private boolean needReset = false;

  public static boolean bootVideoPlaying = false;
  public static Uri uriAfterBootVideo = null;

  private HdmiTvClient mHdmiControl = null;
  private IHdmiControlService mHdmiControlService;

  public void setInputsPanelView(IInputsPanelView mInputsPanelView) {
    this.mInputsPanelView = mInputsPanelView;
  }

  public IInputsPanelView getInputsPanelView() {
    return mInputsPanelView;
  }

  public void notifyInputsPanelFocusToNext() {
    if(mInputsPanelView != null) {
      mInputsPanelView.notifyFocusToNext();
    }
  }

  public void removeMessageForOnKey() {
    if(mInputsPanelView != null) {
      mInputsPanelView.removeMessageForOnKey();
    }
  }

  private HdmiTvClient getHdmiControl() {
    if (mHdmiControl == null) {
      HdmiControlManager hdmiControlManager = (HdmiControlManager) mContext
          .getSystemService(Context.HDMI_CONTROL_SERVICE);
      if (hdmiControlManager != null) {
        mHdmiControl = (HdmiTvClient) hdmiControlManager
            .getClient(HdmiDeviceInfo.DEVICE_TV);
        if (mHdmiControl != null) {
          mHdmiControl.setInputChangeListener(new HdmiInputChangeListener());
        }
      }
    }
    return mHdmiControl;
  }

  public void deviceSelect(TvInputInfo info) {
    Log.d(TAG, "onHdmiDeviceAdded deviceSelect start:"+info);
    HdmiTvClient hdmiTvClient = getHdmiControl();
    if (hdmiTvClient == null) {
      Log.e(TAG, "deviceSelect, hdmiTvClient is null."+info);
      return;
    }
    hdmiTvClient.deviceSelect(info != null ? info.getHdmiDeviceInfo().getId() : 0,
        new SelectCallback() {
      @Override
      public void onComplete(final int result) {
        Log.d(TAG, "onHdmiDeviceAdded deviceSelect onComplete: result="
            + result);
        if (result != HdmiControlManager.RESULT_SUCCESS) {
          Log.d(TAG,
              "onHdmiDeviceAdded deviceSelect onComplete: RESULT_FAIL");
          mHandler.post(new Runnable() {
            @Override
            public void run() {
              resetCurrentInput();
            }
          });
        } else {
          Log.d(TAG,
              "onHdmiDeviceAdded deviceSelect onComplete: RESULT_SUCCESS, "
                  + "mInputContext.state = STATE_DONE");
        }
      }
    });
  }

  public String getFocusWithChangeHdmi() {
    return focusWithChangeHdmi;
  }

  public void setFocusWithChangeHdmi(String focusWithChangeHdmi) {
    this.focusWithChangeHdmi = focusWithChangeHdmi;
  }

  String currentSourceName = "";

  private final class HdmiInputChangeListener implements InputChangeListener {
    @Override
    public void onChanged(final HdmiDeviceInfo device) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "HdmiInputChangeListener, onChanged,  device = " + device.toString());
      AbstractInput input = InputUtil.getInput(getCurrentInputSourceHardwareId());
      if (input != null) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, input.toString(mContext));
      }
      if (DestroyApp.isCurActivityTkuiMainActivity() && input != null && input.isHDMI() &&
          input.getTvInputInfo().getHdmiDeviceInfo() != null &&
          input.getTvInputInfo().getHdmiDeviceInfo().getPortId() == device.getPortId() &&
          input.getTvInputInfo().getHdmiDeviceInfo().getPhysicalAddress() == device.getPhysicalAddress() &&
          input.getTvInputInfo().getHdmiDeviceInfo().getId() == device.getId()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "HdmiInputChangeListener, onChanged, return");
        return;
      }
      if (input != null) {
        if (input instanceof HdmiInput) {
          HdmiInput hdmiInput = (HdmiInput) input;
          if (DestroyApp.isCurActivityTkuiMainActivity()
              && hdmiInput.getPortId() == device.getPortId()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "HdmiInputChangeListener, onChanged, return 1");
            return;
          }
        }
      }

      if(!SystemsApi.isUserSetupComplete(mContext) ||
          com.mediatek.twoworlds.tv.MtkTvScan.getInstance().isScanning() ||
          DestroyApp.isCurOADActivityNew()) {
        return;
      }
      String inputId = "";
      List<TvInputInfo> tvInputList = mTvInputManager.getTvInputList();
      for (TvInputInfo tvInputInfo : tvInputList) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[TIF]" + tvInputInfo.toString());
        if (tvInputInfo.getType() == TvInputInfo.TYPE_HDMI &&
            tvInputInfo.getHdmiDeviceInfo() != null &&
            tvInputInfo.getHdmiDeviceInfo().getPortId() == device.getPortId() &&
            tvInputInfo.getHdmiDeviceInfo().getPhysicalAddress() == device.getPhysicalAddress() &&
            tvInputInfo.getHdmiDeviceInfo().getId() == device.getId()) {
          inputId = tvInputInfo.getId();
          break;
        }
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "input:" + inputId);
      if (!TextUtils.isEmpty(inputId)) {
        Uri uri = TvContract.buildChannelUriForPassthroughInput(inputId);
        if(bootVideoPlaying) {
          uriAfterBootVideo = uri;
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"bootVideoPlaying, return, uriAfterBootVideo:"+uri);
          return;
        }
        //del by xun : don't Change source when hdmi plutin with cec
        /*if (DestroyApp.isCurActivityTkuiMainActivity()) {
          mHandler.post(new Runnable() {
            @Override
            public void run() {
              Intent intent = new Intent(Intent.ACTION_VIEW);
              intent.setData(uri);
              processInputUri(intent);
              if (!TurnkeyUiMainActivity.getInstance().isUKCountry) {
                BannerView banerView = (BannerView) ComponentsManager.getInstance().
                    getComponentById(NavBasic.NAV_COMP_ID_BANNER);
                if (banerView != null) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "processInputUri, showSimpleBanner");
                  banerView.showSimpleBanner();
                }
              }
            }
          });
        } else {
          SystemsApi.dayDreamAwaken(mContext);
          Intent intent = new Intent(Intent.ACTION_VIEW);
          intent.setData(uri);
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          intent.putExtra("livetv", true);
          mContext.startActivity(intent);
        }*/
      }
    }
  }

  private final TvInputCallback mAvailabilityListener = new TvInputCallback() {
    /**
     * This is called when the state of a given TV input is changed.
     *
     * @param inputId The id of the TV input.
     * @param state State of the TV input. The value is one of the following:
     *          <ul>
     *          <li>{@link TvInputManager#INPUT_STATE_CONNECTED}
     *          <li>{@link TvInputManager#INPUT_STATE_CONNECTED_STANDBY}
     *          <li>{@link TvInputManager#INPUT_STATE_DISCONNECTED}
     *          </ul>
     */
    @Override
    public void onInputStateChanged(String inputId, int state) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onInputStateChanged: inputId:" + inputId
          + ", state:" + state);
      /* dispatch message */
      for (ISourceListListener listener:mRigister) {
          listener.onAvailabilityChanged(inputId, state);
      }
      InputUtil.updateState(inputId, state);
      if (state != TvInputManager.INPUT_STATE_CONNECTED) {
        int inputSourceHardwareId = getCurrentInputSourceHardwareId();
        AbstractInput input = InputUtil.getInput(inputSourceHardwareId);
        if(input != null && input.getTvInputInfo() != null
            && (input.getTvInputInfo().getType() == TvInputInfo.TYPE_COMPONENT ||
                input.getTvInputInfo().getType() == TvInputInfo.TYPE_COMPOSITE)
            && input.getTvInputInfo().getId().equals(inputId)) {
          //changeToDefaultSource();
           com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onInputStateChanged: inputId:" + inputId
          + ", state:" + state + "doNothing");
        }
      } else {
        int inputSourceHardwareId = getCurrentInputSourceHardwareId();
        AbstractInput input = InputUtil.getInput(inputSourceHardwareId);
        if (input != null && (input.isVGA()||input.isComponent()||input.isComposite()) && input.getTvInputInfo().getId().equals(inputId) &&
            TurnkeyUiMainActivity.getInstance() != null &&
            TurnkeyUiMainActivity.getInstance().getTvView() != null) {
          TurnkeyUiMainActivity.getInstance().getTvView().reset();
          resetCurrentInput();
        }
      }
      if (mInputsPanelView != null) {
        mInputsPanelView.notifyInputsChanged();
      }
    }

    /**
     * This is called when a TV input is added.
     *
     * @param inputId The id of the TV input.
     */
    @Override
    public void onInputAdded(String inputId) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onInputAdded: inputId:" + inputId);

      TvInputInfo inputInfo = getTvInputInfo(CommonIntegration.getInstance().getCurrentFocus());
      InputUtil.buildSourceList(mContext);
      TvInputInfo newInputInfo = mTvInputManager.getTvInputInfo(inputId);
      if (needReset) {
        needReset = false;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onInputAdded: try to resetCurrentInput again.");
        TIFChannelManager.getInstance(mContext).handleUpdateChannels();
        resetCurrentInput();
      }
      if ((newInputInfo == null || newInputInfo.getType() != TvInputInfo.TYPE_TUNER) &&
          !TvSingletons.getSingletons().getTurnkeyUiMainActiviteActive()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TV in the background, return.");
        return;
      }
      if (inputInfo != null && newInputInfo != null &&
          newInputInfo.getType() == TvInputInfo.TYPE_HDMI
          && newInputInfo.getHdmiDeviceInfo() != null) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onInputAdded:    inputInfo:" + inputInfo.toString());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onInputAdded: newInputInfo:" + newInputInfo.toString());
        if (TextUtils.equals(inputInfo.getId(), newInputInfo.getParentId()) ||
            TextUtils.equals(inputInfo.getId(), newInputInfo.getId())) {
          deviceSelect(newInputInfo);
        }
      }
      if (!MarketRegionInfo.F_3RD_INPUTS_SUPPORT) {
        return;
      }
      if (mTvInputManager != null) {
        TvInputInfo input = mTvInputManager.getTvInputInfo(inputId);
        if (input != null && CommonIntegration.is3rdTVSource(input)) {
          if (!DestroyApp.isCurActivityTkuiMainActivity() &&
              SystemsApi.isUserSetupComplete(mContext) &&
              !com.mediatek.twoworlds.tv.MtkTvScan.getInstance().isScanning()) {
            if (TurnkeyUiMainActivity.getInstance() != null
                && TurnkeyUiMainActivity.getInstance().isInPictureInPictureMode()) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "FINISH TURNKUI");
              TurnkeyUiMainActivity.getInstance().finish();
            }
            Intent intent = new Intent(mContext, SetupSourceActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "launch SourceSetupActivity!!!");
          }
        }
      }
      if (mInputsPanelView != null) {
        mInputsPanelView.notifyInputsChanged();
      }
    }

    @Override
    public void onInputUpdated(String inputId) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onInputUpdated: inputId:" + inputId);
      InputUtil.buildSourceList(mContext);
      if (mInputsPanelView != null) {
        mInputsPanelView.notifyInputsChanged();
      }
    }

    /**
     * This is called when a TV input is removed.
     *
     * @param inputId The id of the TV input.
     */
    @Override
    public void onInputRemoved(String inputId) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onInputRemoved: inputId:" + inputId);
      InputUtil.buildSourceList(mContext);
      if (mInputsPanelView != null) {
        mInputsPanelView.notifyInputsChanged();
      }
    }
  };

  public void resetScreenOffFlag() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetScreenOffFlag");
  }

  public void setShutDownFlag(boolean flag) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setShutDownFlag:" + flag);
  }

  private void setupInputAdapter() {
    try {
      mTvInputManager = (TvInputManager) mContext
          .getSystemService(Context.TV_INPUT_SERVICE);
    } catch (Exception ex) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ex.toString());
    }
    List<TvInputInfo> inputs = mTvInputManager.getTvInputList();

    InputUtil.buildSourceList(mContext);

    if (inputs == null || inputs.isEmpty()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setupInputAdapter failed");
    }
  }

  public void disableCECOneTouchPlay(final boolean enabled) {
      try {
          mHdmiControlService.setProhibitMode(enabled);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "enableCECOneTouchPlay enabled>> " + enabled);
      } catch (Exception e) {
          Log.d(TAG, "setProhibitMode fail.\n" + e.getMessage());
      }
  }

  private InputSourceManager(Context context) {
    mContext = context;
    mSaveValue = SaveValue.getInstance(context);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "InputSourceManager() init");
    if (CommonIntegration.isEUPARegion()) {
      currentSourceName = "DTV";
    } else {
      currentSourceName = "TV";
    }
    TvSingletons.getSingletons().getTvInputManagerHelper().addCallback(
        mAvailabilityListener);
    setupInputAdapter();
    mHandler = new Handler(context.getMainLooper());
    getHdmiControl();
    mHdmiControlService = IHdmiControlService.Stub.asInterface(ServiceManager.getService(Context.HDMI_CONTROL_SERVICE));

    mContext.getContentResolver().registerContentObserver(
        Settings.Secure.CONTENT_URI.buildUpon().appendPath(Settings.Secure.TV_INPUT_HIDDEN_INPUTS).build(),
        true,
        new ContentObserver(null) {
          @Override
          public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TV_INPUT_HIDDEN_INPUTS:");
            if(TvSingletons.getSingletons().getTurnkeyUiMainActiviteActive()) {
              int id = mSaveValue.readValue(MAIN_SOURCE_HARDWARE_ID, -1);
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TV_INPUT_HIDDEN_INPUTS:" + id);
              if(id != -1) {
                AbstractInput currentInput = InputUtil.getInput(id);
                if(currentInput != null && currentInput.isHidden(mContext)) {
                  for (AbstractInput input : InputUtil.getSourceList()) {
                    if (!input.isNeedAbortTune() && !input.isHidden(mContext)) {
                      final int hardwareId = input.getHardwareId();
                      mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                          changeInputSourceByHardwareId(hardwareId);
                        }
                      });
                      break;
                    }
                  }
                }
              }
            }
          }
        });
    mContext.getContentResolver().registerContentObserver(
            Settings.Secure.CONTENT_URI.buildUpon().appendPath(Settings.Secure.USER_SETUP_COMPLETE).build(),
            true,
            new ContentObserver(null) {
              @Override
              public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "user_setup_complete changed," + uri);
                InputUtil.buildSourceList(mContext);
              }
            }
    );
    RxBus.instance.onEvent(MainActivityDestroyEvent.class)
        .doOnComplete(this::release)
        .subscribe();
  }

//  public static synchronized InputSourceManager getInstancea(Context context) {
//    if (instance == null) {
//      synchronized (InputSourceManager.class) {
//        if (instance == null) {
//          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getInstance new");
//          instance = new InputSourceManager(context);
//        }
//      }
//    }
//    return instance;
//  }

  public static synchronized InputSourceManager getInstance() {
    return instance;
  }

  /**
   * for google TV to TKUI with action.VIEW
   *
   * @param uri the Uri of TV or other source(from google TV code)
   * @return if is channel or source Uri, return true, else false
   */
  public boolean processInputUri(Intent mainIntent) {
    final Uri uri = mainIntent.getData();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "processInputUri xiaojie>>" + uri);
    if (uri == null) {
      return false;
    }
    boolean timeshiftStart = SaveValue.readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START);
    boolean isRecording = StateDvr.getInstance() != null && StateDvr.getInstance().isRecording();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "processInputUri isRecording="+isRecording + ", timeshiftStart="+timeshiftStart);
    if (isChannelUriForTunerInput(uri)) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "processInputUri xiaojie1>>");

      if(timeshiftStart) {
        ComponentStatusListener.getInstance().delayUpdateStatus(
            ComponentStatusListener.NAV_INPUTS_PANEL_ENTER_KEY, -2, 500);
        return true;
      }
      long channelId = ContentUris.parseId(uri);
      long uriId = SaveValue.getInstance(mContext).readLongValue(DvrManager.DVR_URI_ID);
      if(uriId != channelId && isRecording) {
        Bundle extras = mainIntent.getExtras();
        if(extras != null && extras.getBoolean("android.intent.extra.START_PLAYBACK")) {
          if (TurnkeyUiMainActivity.getInstance() != null && TurnkeyUiMainActivity.getInstance().getTvView() != null) {
            TurnkeyUiMainActivity.getInstance().getTvView().reset();
            ComponentsManager.getInstance().hideAllComponents();
          }
        }
        ComponentStatusListener.getInstance().updateStatus(
                ComponentStatusListener.NAV_INPUTS_PANEL_ENTER_KEY, -2);
        return true;
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "processInputUri channel id>>" + channelId);
      return tuneChannelByUri(uri);
    } else if (TvContract.isChannelUriForPassthroughInput(uri)) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "processInputUri xiaojie2>>");
      // If mInitChannelUri is for a passthrough TV input.
      String inputId = uri.getPathSegments().get(1);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "processInputUri input id>>" + inputId + ">>"
          + mMainTvInputInfo);
      if(timeshiftStart) {
        ComponentStatusListener.getInstance().delayUpdateStatus(
            ComponentStatusListener.NAV_INPUTS_PANEL_ENTER_KEY, -1, 500);
        return true;
      }
      if (!TextUtils.isEmpty(inputId)) {
        List<AbstractInput> abstractInputs = InputUtil.getSourceList();
        InputUtil.dump(mContext, abstractInputs);
        TvInputInfo tvInputInfo = mTvInputManager.getTvInputInfo(inputId);
        if (tvInputInfo != null && tvInputInfo.getType() == TvInputInfo.TYPE_TUNER) {
          if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)) {
            AbstractInput input = null;
            if(DtvInput.DEFAULT_ID.equals(inputId)) {
              input = InputUtil.getInputByType(AbstractInput.TYPE_DTV);
            } else if(AtvInput.DEFAULT_ID.equals(inputId)){
              input = InputUtil.getInputByType(AbstractInput.TYPE_ATV);
            }
            if (input != null) {
              return changeInputSourceByHardwareId(input.getHardwareId()) == 0;
            }
          } else if (inputId.contains("com.mediatek.tvinput/.tuner.TunerInputService")) {
            AbstractInput tvInput = InputUtil.getInputByType(AbstractInput.TYPE_TV);
            if (tvInput != null) {
              return changeInputSourceByHardwareId(tvInput.getHardwareId()) == 0;
            }
          }
        }
        for (AbstractInput abstractInput : abstractInputs) {
          if (abstractInput.getTvInputInfo() != null) {
            if (abstractInput.getTvInputInfo().getId().equals(inputId)) {
              if (abstractInput.isNeedAbortTune()) {
                String msgString = mContext.getResources().getString(
                    R.string.special_input_toast,
                    InputUtil.getSourceList(mContext).get(abstractInput.getHardwareId()));
                Toast.makeText(mContext, msgString, 0).show();
              }
              return changeInputSourceByHardwareId(abstractInput.getHardwareId()) == 0;
            }
            String parentId = abstractInput.getTvInputInfo().getParentId();
            if (!TextUtils.isEmpty(parentId) && TextUtils.equals(parentId, inputId)) {
              return changeInputSourceByHardwareId(abstractInput.getHardwareId()) == 0;
            }
          }
        }
      }
      return false;
    } else if (isChannelUriForDigitalTurn(uri)) {
      String strChannelNum = uri.getQueryParameter("channel_num");
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "processInputUri turn channel by digital num. strChannelNum=" + strChannelNum);
      try {
        int chNum = Integer.parseInt(strChannelNum);
        ComponentStatusListener.getInstance().updateStatus(
            ComponentStatusListener.NAV_CH_CHANGE_BY_DIGITAL, chNum);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
      return true;
    }
    else {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "processInputUri xiaojie3>>");
      if(timeshiftStart) {
        ComponentStatusListener.getInstance().delayUpdateStatus(
            ComponentStatusListener.NAV_INPUTS_PANEL_ENTER_KEY, -1, 500);
        return true;
      }
      if (null != uri.getQueryParameter("input")) {
        TurnkeyUiMainActivity.getInstance().getTvView()
            .tune(uri.getQueryParameter("input"), uri);
        return true;
      }
      else {
        boolean result = false;
        String tunerType = uri.getQueryParameter("TunerType");
        if (!TextUtils.isEmpty(tunerType)) {
          AbstractInput input = InputUtil.getInputByType(Integer.parseInt(tunerType));
          if (input != null) {
            result = changeInputSourceByHardwareId(input.getHardwareId()) == 0;
          }
        } else {
          result = changeToTVSource() == 0;
        }
        List<TIFChannelInfo> channelList = TIFChannelManager.getInstance(mContext)
            .getCurrentSVLChannelList();
        if (MarketRegionInfo.F_3RD_INPUTS_SUPPORT && (channelList == null || channelList.isEmpty()) &&
            CommonIntegration.getInstance().getChannelActiveNumByAPIForScan() == 0) {
          Intent intent = new Intent(mContext, SetupSourceActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          mContext.startActivity(intent);
          mainIntent.putExtra("SetupSource", true);
        } else {
          if (TvContract.Programs.CONTENT_URI.equals(uri)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "processInputUri xiaojie4>>");
            // open epg
            EPGManager
                .getInstance(TurnkeyUiMainActivity.getInstance())
                .
                startEpg(TurnkeyUiMainActivity.getInstance(), NavBasic.NAV_REQUEST_CODE);
          }
        }
        return result;
      }
    }
  }

  public boolean isNotProcessInputUri(Intent intent, TvSurfaceView tvView) {
    boolean epg = intent.getBooleanExtra("EPG", false);
    boolean fvp = intent.getBooleanExtra("FVP", false);
    Uri uri = intent.getData();
    if(uri.toString().startsWith(TvContract.Channels.CONTENT_URI.toString()) &&
            tvView != null && tvView.isStart()) {
      AbstractInput input = InputUtil.getInput(InputSourceManager.getInstance().getCurrentInputSourceHardwareId());
      if (epg && !uri.getBooleanQueryParameter("FST", false)
              && !TvContract.isChannelUriForTunerInput(uri)) {
        if (input.isTV() || input.isDTV()) {
          return true;
        }
      } else if (fvp) {
        if(InputUtil.F_TUNER_MODE_AS_SOURCE_SUPPORT ? input.isAIR() : input.getType() == AbstractInput.TYPE_DTV) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns {@code true}, if {@code uri} is a channel URI for turn channel by digital number.
   */
  private boolean isChannelUriForDigitalTurn(Uri uri) {
    String channelNum = uri.getQueryParameter("channel_num");
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isChannelUriForDigitalTurn channelNum=" + channelNum);
    return isTvUri(uri) && !TextUtils.isEmpty(channelNum);
  }

  /**
   * Returns {@code true}, if {@code uri} is a channel URI for a tuner input. It is copied from the
   * hidden method TvContract.isChannelUriForTunerInput.
   */
  private boolean isChannelUriForTunerInput(Uri uri) {
    return isTvUri(uri) && isTwoSegmentUriStartingWith(uri, PATH_CHANNEL);
  }

  private boolean isTvUri(Uri uri) {
    return uri != null
        && ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())
        && TvContract.AUTHORITY.equals(uri.getAuthority());
  }

  private boolean isTwoSegmentUriStartingWith(Uri uri,
      String pathSegment) {
    List<String> pathSegments = uri.getPathSegments();
    return pathSegments.size() == 2 && pathSegment.equals(pathSegments.get(0));
  }

  public static void remove() {
    instance = null;
  }

  /*
   * after APK started, mMainTvInputInfo is still null, but the input will default select
   * mMainTvInputInfo should be reset to current input
   */
  public int resetCurrentInput() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resetCurrentInput");
    int hardwareId = getCurrentInputSourceHardwareId(CommonIntegration.getInstance()
        .getCurrentFocus());
    AbstractInput input = InputUtil.getInput(hardwareId);
    if (input != null && input.isNeedAbortTune() && !input.isVGA() ||
        input != null && input.isHidden(mContext)) {
      return changeToDefaultSource();
    }
    return changeInputSourceByHardwareId(hardwareId);
  }

  public List<Integer> getInputHardwareIdList() {
    List<AbstractInput> list = InputUtil.getSourceList();
    List<Integer> result = new ArrayList<Integer>();
    Iterator<AbstractInput> iterator = list.iterator();
    while (iterator.hasNext()) {
      AbstractInput input = iterator.next();
      if (!input.isHidden(mContext)) {
        result.add(input.getHardwareId());
      }
    }
    return result;
  }

  public List<String> getConflictSourceList() {
    ArrayList<String> list = new ArrayList<String>();

    if (StateDvr.getInstance() != null && StateDvr.getInstance().isRunning()) {
      return getPVRConflictSourceList(DvrManager.getInstance().getController()
          .getSrcType());
    }

    // normal state;
    if (CommonIntegration.getInstance().isTVNormalState()) {
      return list;
    }

    AbstractInput cuInput = InputUtil.getInput(getCurrentInputSourceHardwareId());
    List<AbstractInput> sourceList = InputUtil.getSourceList();
    Iterator<AbstractInput> iterator = sourceList.iterator();
    while (iterator.hasNext()) {
      AbstractInput input = iterator.next();
      if (cuInput.getConflict(input)) {
        String label = input.getCustomSourceName(mContext);
        if (TextUtils.isEmpty(label) || TextUtils.equals(label, "null")) {
          label = input.getSourceName(mContext);
        }
        list.add(label);
      }
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getConflictSourceList: focus:" + list);

    return list;
  }

  public boolean getConflict(int hardwardId) {
    if (StateDvr.getInstance() != null && StateDvr.getInstance().isRunning()) {
      return getPVRConflict(hardwardId);
    } else if (CommonIntegration.getInstance().isPipOrPopState()) {
      int inputSourceHardwareId = getCurrentInputSourceHardwareId();
      AbstractInput cuInput = InputUtil.getInput(inputSourceHardwareId);
      AbstractInput tarInput = InputUtil.getInput(hardwardId);
      return cuInput != null && tarInput != null && cuInput.getConflict(tarInput);
    }
    return false;
  }

  public boolean getPVRConflict(int hardwardId) {
    boolean isCoExist = true;
    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_DVR)) {
      String currentInputSource = DvrManager.getInstance()
          .getPVRRecordingSrc();
      int id = getHardwareIdBySourceName(currentInputSource);
      AbstractInput curInput = InputUtil.getInput(id);
      AbstractInput tarInput = InputUtil.getInput(hardwardId);
      if (curInput != null && tarInput != null && !curInput.equals(tarInput)) {
        if (InputUtil.F_TUNER_MODE_AS_SOURCE_SUPPORT) {
          if (curInput.isAIR() || curInput.isCAB() || curInput.isSAT()) {
            isCoExist = tarInput.isHDMI();
          }
        } else {
          if (curInput.isDTV() || curInput.isTV()) {
            isCoExist =  tarInput.isDTV() || tarInput.isTV() || tarInput.isHDMI();
          }
        }
      }
    }
    return !isCoExist;
  }

  public List<String> getPVRConflictSourceList(String recordSource) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getPVRConflictSourceList()");

    ArrayList<String> list = new ArrayList<String>();
    String currentInputSource = null;

    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_DVR)) {
      currentInputSource = DvrManager.getInstance()
          .getPVRRecordingSrc();
    }

    if (currentInputSource == null
        || currentInputSource.equalsIgnoreCase("")) {
      return list;
    }
    if ("ATV".equalsIgnoreCase(recordSource)) {
      list.add("Composite");
      list.add("ATV");
      list.add("TV");
      list.add("Component");
      list.add("SVIDEO");
      list.add("SCART");
      list.add("VGA");
      list.add("HDMI 1");
      list.add("HDMI 2");
      list.add("HDMI 3");
      list.add("HDMI 4");
      list.add("MMP");
      list.add("Port1Mobile");
      list.add("Port1BD");
      list.add("Port2BD");
      list.add("Port3BD");
      list.add("Port4BD");

    } else if ("TV".equalsIgnoreCase(recordSource)) {
      list.add("MMP");
      list.add("ATV");
    } else if ("COMPOSITE".equalsIgnoreCase(recordSource)) {
      if (CommonIntegration.getInstance().getCurChInfo() != null
          && CommonIntegration.getInstance().getCurChInfo()
              .getBrdcstType() == MtkTvChCommonBase.BRDCST_TYPE_ANALOG) {
        list.add("TV");
      }
      list.add("SCART");
      list.add("SVIDEO");
      list.add("MMP");
    } else if ("SVIDEO".equalsIgnoreCase(recordSource)) {
      list.add("Composite");
      list.add("Component");
      list.add("SCART");
      list.add("VGA");
      list.add("HDMI 1");
      list.add("HDMI 2");
      list.add("HDMI 3");
      list.add("HDMI 4");
      list.add("TV");
      list.add("MMP");
      list.add("Port1Mobile");
      list.add("Port1BD");
      list.add("Port2BD");
      list.add("Port3BD");
      list.add("Port4BD");
    } else if ("SCART".equalsIgnoreCase(recordSource)) {
      list.add("Composite");
      list.add("Component");
      list.add("SVIDEO");
      list.add("VGA");
      list.add("HDMI 1");
      list.add("HDMI 2");
      list.add("HDMI 3");
      list.add("HDMI 4");
      list.add("TV");
      list.add("MMP");
      list.add("Port1Mobile");
      list.add("Port1BD");
      list.add("Port2BD");
      list.add("Port3BD");
      list.add("Port4BD");
    } else if ("Component".equalsIgnoreCase(recordSource)) {
      list.add("Composite");
      list.add("SCART");
      list.add("SVIDEO");
      list.add("VGA");
      list.add("HDMI 1");
      list.add("HDMI 2");
      list.add("HDMI 3");
      list.add("HDMI 4");
      list.add("TV");
      list.add("MMP");
      list.add("Port1Mobile");
      list.add("Port1BD");
      list.add("Port2BD");
      list.add("Port3BD");
      list.add("Port4BD");
    } else if ("VGA".equalsIgnoreCase(recordSource)) {
      list.add("Composite");
      list.add("SCART");
      list.add("SVIDEO");
      list.add("Component");
      list.add("HDMI 1");
      list.add("HDMI 2");
      list.add("HDMI 3");
      list.add("HDMI 4");
      list.add("TV");
      list.add("MMP");
      list.add("Port1Mobile");
      list.add("Port1BD");
      list.add("Port2BD");
      list.add("Port3BD");
      list.add("Port4BD");
    } else if ("HDMI".equalsIgnoreCase(recordSource)) {
      list.add("Composite");
      list.add("SCART");
      list.add("SVIDEO");
      list.add("Component");
      list.add("HDMI 1");
      list.add("HDMI 2");
      list.add("HDMI 3");
      list.add("HDMI 4");
      list.add("TV");
      list.add("MMP");
      list.add("HDMI");
      list.add("Port1Mobile");
      list.add("Port1BD");
      list.add("Port2BD");
      list.add("Port3BD");
      list.add("Port4BD");
    }

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getPVRConflictSourceList()," + list);
    return list;
  }

  public int getCurrentInputSourceHardwareId(String path) {
    int result = 0;
    if (MAIN.equals(path)) {
      result = mSaveValue.readValue(MAIN_SOURCE_HARDWARE_ID, -1);
      if (result == -1) {
          if (TurnkeyUiMainActivity.getInstance() == null
              || TurnkeyUiMainActivity.getInstance().getTvView() == null) {
              return -1;
          }
        int type = SaveValue.readWorldInputType(mContext);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "first getCurrentInputSourceHardwareId:" + type);
        if (type == Constants.INPUT_TYPE_OTHER) {
          for (AbstractInput input : InputUtil.getSourceList()) {
            if (!input.isNeedAbortTune()) {
              result = input.getHardwareId();
              break;
            }
          }
        } else if (type == Constants.INPUT_TYPE_DTV) {
          AbstractInput input;
          if (InputUtil.F_TUNER_MODE_AS_SOURCE_SUPPORT) {
            int tunerMode = CommonIntegration.getInstance().getTunerMode();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tunerMode:" + tunerMode);
            switch (tunerMode) {
              case 0:
                input = InputUtil.getInputByType(AbstractInput.TYPE_AIR);
                break;
              case 1:
                input = InputUtil.getInputByType(AbstractInput.TYPE_CAB);
                break;
              default:
                input = InputUtil.getInputByType(AbstractInput.TYPE_SAT);
                break;
            }
          } else {
            input = InputUtil.getInputByType(AbstractInput.TYPE_DTV);
          }
          if (input != null) {
            result = input.getHardwareId();
          }
        } else if (type == Constants.INPUT_TYPE_ATV) {
          AbstractInput input = InputUtil.getInputByType(AbstractInput.TYPE_ATV);
          if (input != null) {
            result = input.getHardwareId();
          }
        }
        //mSaveValue.saveValue(MAIN_SOURCE_HARDWARE_ID, result);
      }
    } else if (SUB.equals(path)) {
      result = mSaveValue.readValue(SUB_SOURCE_HARDWARE_ID, -1);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentInputSourceHardwareId:" + result);
    result = InputUtil.checkInvalideInput(result);
    return result;
  }

  public AbstractInput getCurrentAbstractInput() {
    return InputUtil.getInput(getCurrentInputSourceHardwareId());
  }

  public int getCurrentInputSourceHardwareId() {
    return getCurrentInputSourceHardwareId(CommonIntegration.getInstance().getCurrentFocus());
  }

  public void saveInputSourceHardwareId(int hardwareId, String path) {
    if (MAIN.equals(path)) {
      mSaveValue.saveValue(MAIN_SOURCE_HARDWARE_ID, hardwareId);
      AbstractInput input = InputUtil.getInput(hardwareId);
      if (input != null) {
        SaveValue.writeWorldStringValue(mContext, MAIN_SOURCE_NAME,
            input.getSourceName(mContext), true);
        int inputType = Constants.INPUT_TYPE_OTHER;
        if (input.isTV()) {
          inputType = Constants.INPUT_TYPE_TV;
        } else if (input.isDTV()) {
          inputType = Constants.INPUT_TYPE_DTV;
        } else if (input.isATV()) {
          inputType = Constants.INPUT_TYPE_ATV;
        } else if (input.isHDMI()) {
          inputType = Constants.INPUT_TYPE_HDMI;
        } else if (input.isComponent()) {
          inputType = Constants.INPUT_TYPE_COMPONENT;
        } else if (input.isComposite()) {
          inputType = Constants.INPUT_TYPE_COMPOSITE;
        } else if (input.isVGA()) {
          inputType = Constants.INPUT_TYPE_VGA;
        }
        SaveValue.writeWorldInputType(mContext, inputType);
        SaveValue.saveWorldValue(mContext,"MAIN_INPUT_TYPE_FOR_HOME_CHANNEL", inputType, true);
      }
    } else if (SUB.equals(path)) {
      mSaveValue.saveValue(SUB_SOURCE_HARDWARE_ID, hardwareId);
    }
  }

  public String getCurrentInputSourceName(String path) {
    int id = getCurrentInputSourceHardwareId(path);
    AbstractInput input = InputUtil.getInput(id);
    String currentSourceName = input != null ? input.getSourceNameForUI(mContext) : null;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentInputSourceName, path=" + path + ", SourceName=" + currentSourceName);
    return currentSourceName;
  }

  public int autoChangeToNextInputSource(String path) {
    int id = getNextEnableInputSourceHardwareId(path);
    return changeInputSourceByHardwareId(id, path);
  }

  public int getNextEnableInputSourceHardwareId(String path) {
    int id = getCurrentInputSourceHardwareId(path);
    List<AbstractInput> list = InputUtil.getSourceList();
    int nextIndex = -1;
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).getHardwareId() == id) {
        nextIndex = i;
        break;
      }
    }
    do {
      nextIndex++;
      nextIndex = nextIndex >= list.size() || nextIndex < 0 ? 0 : nextIndex;
    } while (list.get(nextIndex).isNeedAbortTune() || list.get(nextIndex).isHidden(mContext));
    int result = list.get(nextIndex).getHardwareId();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getNextEnableInputSourceHardwareId " + result);
    return result;
  }

  public int changeToTVSource() {
    int targetType = AbstractInput.TYPE_TV;
    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)) {
      if (InputUtil.F_TUNER_MODE_AS_SOURCE_SUPPORT) {
        targetType = AbstractInput.TYPE_AIR;
      } else {
        targetType = AbstractInput.TYPE_DTV;
      }
    }
    AbstractInput input = InputUtil.getInputByType(targetType);
    if (input != null) {
      return changeInputSourceByHardwareId(input.getHardwareId());
    } else {
      return changeToDefaultSource();
    }
  }

  public int changeToDefaultSource() {
    int hardwareId = getCurrentInputSourceHardwareId();
    List<AbstractInput> list = InputUtil.getSourceList();
    int targetId = -1;
    for (AbstractInput abstractInput : list) {
      if (!abstractInput.isNeedAbortTune()) {
        targetId = abstractInput.getHardwareId();
        break;
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeToDefaultSource targetId:" + targetId);
    if (targetId != -1 && targetId != hardwareId) {
      return changeInputSourceByHardwareId(targetId);
    }
    return -1;
  }

  public int changeCurrentInputSourceByName(String inputSourceName) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeCurrentInputSourceByName inputSourceName:"
        + inputSourceName);
    return changeCurrentInputSourceByName(inputSourceName,
        CommonIntegration.getInstance().getCurrentFocus());
  }

  public void changeToTVAndSelectChannel(String inputSourceName, int channelID) {
    TIFChannelInfo mChannelInfo = TIFChannelManager.getInstance(mContext)
        .getTIFChannelInfoPLusByProviderId(channelID);
    tuneChannelByTIFChannelInfoForAssistant(mChannelInfo);
  }

  public void autoChangeTestSourceChange(String inputSourceName, String path) {
    if (inputSourceName.startsWith("HDMI")) {
      inputSourceName = "HDMI " + inputSourceName.substring(4);
    }
    Log.d(TAG, "inputSourceAutoChangeTest,inputSourceName" + inputSourceName);
    int id = getHardwareIdByOriginalSourceName(inputSourceName);
    Log.d(TAG, "inputSourceAutoChangeTest,hardwardId:" + id);
    if (changeInputSourceByHardwareId(id, path) == 0) {
      Log.d(TAG, "inputSourceAutoChangeTest,changeCurrentInputSourceByName success");
      mHandler.postDelayed(new Runnable() {

        @Override
        public void run() {
          String result = mTvInputSource.getCurrentInputSourceName();
          Log.d(TAG, "source name:" + result);
          SystemProperties.set("auto_test.input.name", result);
        }
      }, 5000);
    }
  }

  public String autoChangeTestGetCurrentSourceName(String path) {
    String currentSourceName;
    currentSourceName = getCurrentInputSourceName(path);
    Log.d(TAG, "autoChangeTestGetCurrentSourceName, Name:"
        + currentSourceName);
    return currentSourceName;
  }

  public int changeInputSourceByHardwareId(int hardwareId) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeInputSourceByHardwareId hardwareId:" + hardwareId);
    return changeInputSourceByHardwareId(hardwareId, true);
  }

  public int changeInputSourceByHardwareId(int hardwareId, String path) {
    return changeInputSourceByHardwareId(hardwareId, true, path);
  }

  public int changeInputSourceByHardwareId(int hardwareId, boolean checkPVR) {
    return changeInputSourceByHardwareId(hardwareId, checkPVR, CommonIntegration.getInstance()
        .getCurrentFocus());
  }

  public int changeInputSourceByHardwareId(int hardwareId, boolean checkPVR, String path) {
    AbstractInput input = InputUtil.getInput(hardwareId);
    if (TextUtils.isEmpty(path) ||
        TurnkeyUiMainActivity.getInstance() == null ||
        TurnkeyUiMainActivity.getInstance().getTvView() == null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeInputSourceByHardwareId error, hardwareId:" + hardwareId + ",path:"
          + path);
      return -1;
    } else if (input == null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sourceList not ready hardwareId:" + hardwareId);
      needReset = true;
      return -1;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeInputSourceByHardwareId hardwareId:" + hardwareId + ",path:" + path);

    // if dvr playback running ,return and stop dvr
    if (StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
      StateDvrPlayback.getInstance().stopDvrFilePlay();
    }
    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_DVR)) {
      if (StateDvr.getInstance() != null && StateDvr.getInstance().isRecording()) {
        StateDvr.getInstance().showSmallCtrlBar();
        StateDvr.getInstance().clearWindow(true);
      }
    }
    if (checkPVR && null != StateDvr.getInstance() && StateDvr.getInstance().isRecording()) {
      int pvrTunerType = mSaveValue.readValue(DvrManager.DVR_BG_TUNER_TYPE, -1);
      AbstractInput pvrInput = InputUtil.getInputById(pvrTunerType);
      if (pvrInput != null && input.getHardwareId() != pvrInput.getHardwareId() && (input.isDTV() || input.isATV())) {
        ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_INPUTS_PANEL_ENTER_KEY,input.getHardwareId());
        return 0;
      }
    }
    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)) {
      if (input.isATV()) {
        //if (null != StateDvr.getInstance()
        //        && StateDvr.getInstance().isRecording()) {
        //  DvrManager.getInstance().stopDvr();
        //}
        CommonIntegration.getInstance().setBrdcstType(CommonIntegration.BRDCST_TYPE_ATV);
      } else if (input.isDTV()) {
        CommonIntegration.getInstance().setBrdcstType(CommonIntegration.BRDCST_TYPE_DTV);
      }
    }
    saveCurrentSource(input, path);
    if (InputUtil.F_TUNER_MODE_AS_SOURCE_SUPPORT && input.isDTV()) {
      MenuConfigManager.getInstance(mContext).setTunerModeForInputsPanel(input.getType()-20001,false);
    }
    if (MarketRegionInfo.F_3RD_INPUTS_SUPPORT && (input.isTV() || input.isDTV())) {
      int current3rdMId = SaveValue.getInstance(mContext).readValue(TIFFunctionUtil.current3rdMId,
          -1);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "current3rdId:" + current3rdMId);
      TIFChannelInfo channelInfo = null;
      if (current3rdMId != -1) {
        channelInfo = TIFChannelManager.getInstance(mContext).getTIFChannelInfoById(
            current3rdMId);
        if (channelInfo == null) {
          channelInfo = TIFChannelManager.getInstance(mContext).queryChannelById(current3rdMId);
        }
      }
      if(channelInfo == null && CommonIntegration.getInstance().getChannelNumByAPIForBanner() == 0) {
        //check whether has 3rd channels
        List<TIFChannelInfo> list3rd = TIFChannelManager.getInstance(mContext).get3RDChannelList();
        if(list3rd != null && !list3rd.isEmpty()) {
          channelInfo = list3rd.get(0);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "list3rd first:" + channelInfo.toString());
        }
      }
      if (channelInfo != null) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tune 3rd channel.");
        MtkTvMultiView.getInstance().setChgSource(false);
        TurnkeyUiMainActivity.getInstance().getTvView().tune(channelInfo.mInputServiceName,
            TvContract.buildChannelUri(channelInfo.mId));
        return 0;
      }
    }
//    MtkTvMultiView.getInstance().setChgSource(true);
    if (MAIN.equalsIgnoreCase(path)) {
      // TurnkeyUiMainActivity.getInstance().getTvView().setStreamVolume(1.0f);
      if (input.isTV() || input.isDTV() || input.isATV()) {
        int channelId = CommonIntegration.getInstance().getCurrentChannelId();
        TIFChannelInfo tifChannelInfo = TIFChannelManager.getInstance(mContext)
            .getTIFChannelInfoById(channelId);
        if (tifChannelInfo == null) {
          tifChannelInfo = TIFChannelManager.getInstance(mContext).queryChannelById(channelId);
        }
        if (tifChannelInfo != null) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tifChannelInfo.mInputServiceName:" + tifChannelInfo.mInputServiceName
              + "tifChannelInfo.mId:" + tifChannelInfo.mId);
          input.tune(TurnkeyUiMainActivity.getInstance().getTvView(),
              tifChannelInfo.mInputServiceName,
              TvContract.buildChannelUri(tifChannelInfo.mId));
        } else {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "no channel");
          input.tune(TurnkeyUiMainActivity.getInstance().getTvView(),
              input.isATV() ? DEFAULT_ATV_INPUT_ID : DEFAULT_INPUT_ID, mUriMain);
        }
      } else {
        TurnkeyUiMainActivity.getInstance().getTvView().tune(input.getId(), mUriMain);
      }
    } else if (SUB.equalsIgnoreCase(path)) {
      TurnkeyUiMainActivity.getInstance().getPipView().setStreamVolume(0.0f);
    }
    return 0;
  }

  public boolean tuneChannelByUri(Uri uri) {
    if (uri == null) {
      return false;
    }
    long channelId = ContentUris.parseId(uri);
    TIFChannelInfo mChannelInfo = null;
    if (uri != null && uri.getBooleanQueryParameter("FST", false)) {
      List<TIFChannelInfo> svlChannelList = TIFChannelManager.getInstance(mContext)
          .getCurrentSVLChannelList();
      if (svlChannelList != null && !svlChannelList.isEmpty()) {
        mChannelInfo = svlChannelList.get(0);
        mChannelInfo.mMtkTvChannelInfo = TIFChannelManager.getInstance(mContext)
            .getAPIChannelInfoById(mChannelInfo.mInternalProviderFlag3);
      }
    } else {
      mChannelInfo = TIFChannelManager.getInstance(mContext)
          .getTIFChannelInfoPLusByProviderId(channelId);
      if (mChannelInfo != null && mChannelInfo.mMtkTvChannelInfo == null) {
        // network
        return tuneChannelByTIFChannelInfoForAssistant(mChannelInfo) == 0;
      }
    }
    if (mChannelInfo != null && mChannelInfo.mMtkTvChannelInfo != null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("TAG", "processInputUri mChannelInfo = " + mChannelInfo);
      if(CommonIntegration.isEURegion() &&
          !mChannelInfo.mMtkTvChannelInfo.isNumberSelectable()) {
        return false;
      }
      return tuneChannelByTIFChannelInfoForAssistant(mChannelInfo) == 0;
    } else {
      return false;
    }
  }

  public int tuneChannelByTIFChannelInfoForAssistant(TIFChannelInfo tifChannelInfo) {
    if (tifChannelInfo == null || TurnkeyUiMainActivity.getInstance().getTvView() == null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tuneChannelByTIFChannelInfoForAssistant error 1.");
      return -1;
    }
    List<AbstractInput> list = InputUtil.getSourceList();
    AbstractInput input = null;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelInfo:" + tifChannelInfo.toString());
    if (tifChannelInfo.mMtkTvChannelInfo == null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tune 3rd channel");
      for (AbstractInput abstractInput : list) {
        if (abstractInput.isTV() || abstractInput.isDTV()) {
          input = abstractInput;
          break;
        }
      }
    } else {
      if (tifChannelInfo.mInternalProviderFlag1 != CommonIntegration.getInstance().getSvl()) {
        MenuConfigManager.getInstance(mContext).setTunerModeFromSvlId(
            tifChannelInfo.mInternalProviderFlag1);
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
          "tifChannelInfo.mMtkTvChannelInfo:" + tifChannelInfo.mMtkTvChannelInfo.toString());
      boolean isAnalog = tifChannelInfo.mMtkTvChannelInfo.getBrdcstType() == MtkTvChCommonBase.BRDCST_TYPE_ANALOG;
      if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)) {
        input = InputUtil
            .getInputByType(isAnalog ? AbstractInput.TYPE_ATV : AbstractInput.TYPE_DTV);
      } else {
        input = InputUtil.getInputById(AbstractInput.TYPE_TV);
      }
    }
    if (input == null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tuneChannelByTIFChannelInfoForAssistant error 2.");
      return -1;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "input:" + input.toString(mContext));
    String path = CommonIntegration.getInstance().getCurrentFocus();
//    saveCurrentSource(input, path);
    if (input.isATV()) {
      if (null != StateDvr.getInstance()
          && StateDvr.getInstance().isRecording()) {
        DvrManager.getInstance().stopDvr();
      }
      CommonIntegration.getInstance().setBrdcstType(CommonIntegration.BRDCST_TYPE_ATV);
    } else if (input.isDTV()) {
      CommonIntegration.getInstance().setBrdcstType(CommonIntegration.BRDCST_TYPE_DTV);
    }
//    MtkTvMultiView.getInstance().setChgSource(tifChannelInfo.mMtkTvChannelInfo != null);
    input.tune(TurnkeyUiMainActivity.getInstance().getTvView(), tifChannelInfo.mInputServiceName,
        TvContract.buildChannelUri(tifChannelInfo.mId));
    saveCurrentSource(input, path);
    CommonIntegration.getInstance().setChanenlListType(tifChannelInfo.mMtkTvChannelInfo == null);
    return 0;
  }

  public int changeCurrentInputSourceByName(String inputSourceName,
      String path) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeCurrentInputSourceByName Name:" + inputSourceName
        + ",path:" + path);
    if (!TextUtils.isEmpty(inputSourceName)) {
      AbstractInput input = InputUtil.getInputBySourceName(mContext, inputSourceName);
      if (input != null) {
        changeInputSourceByHardwareId(input.getHardwareId(), path);
      }
    }
    return -1;
  }

  private void saveCurrentSource(AbstractInput input, String path) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "saveCurrentSource:" + input.toString(mContext));
    saveInputSourceHardwareId(input.getHardwareId(), path);
    saveOutputSourceName(input.getSourceName(mContext), path);
  }

  public boolean isCurrentHDMISource(String path) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentHDMISource");
    int id = getCurrentInputSourceHardwareId(path);
    AbstractInput input = InputUtil.getInputById(id);
    if (input != null && input.isHDMI()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentHDMISource inputType:" + input.getType());
      return true;
    }
    return false;
  }

  public boolean isCurrentTvSource(String path) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentTvSource");
    int id = getCurrentInputSourceHardwareId(path);
    AbstractInput input = InputUtil.getInputById(id);
    if (input != null && (input.isTV() || input.isDTV() || input.isATV())) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentTvSource inputType:" + input.getType());
      return true;
    }
    return false;
  }

  public boolean isCurrentDTvSource(String path) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentDTvSource");
    int id = getCurrentInputSourceHardwareId(path);
    AbstractInput input = InputUtil.getInputById(id);
    return input != null && input.isDTV();
  }

  public boolean isCurrentATvSource(String path) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentATvSource");
    int id = getCurrentInputSourceHardwareId(path);
    AbstractInput input = InputUtil.getInputById(id);
    return input != null && input.isATV();
  }

  public boolean isCurrentAnalogSource(String path) {
    if (!isCurrentTvSource(path)) {
      return false;
    }
    MtkTvChannelInfoBase info =
        CommonIntegration.getInstance().getCurChInfo();
    if (info != null) {
      if (MtkTvChCommonBase.BRDCST_TYPE_ANALOG == info.getBrdcstType()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "current source is analog Tv source");
        return true;
      }
    }
    return false;
  }

  public boolean isCurrentSpeicalSource() {
    AbstractInput input = InputUtil.getInputById(getCurrentInputSourceHardwareId(MAIN));
    return input != null && (input.isVGA() || input.isComponent() || input.isComposite());
  }

  public int getHardwareIdBySourceName(String name) {
    int result = -1;
    if (!TextUtils.isEmpty(name)) {
      Map<Integer, String> map = InputUtil.getSourceList(mContext);
      Iterator<Entry<Integer, String>> iterator = map.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<Integer, String> entry = iterator.next();
        if (name.equals(entry.getValue())) {
          result = entry.getKey();
          break;
        }
      }
    }
    return result;
  }

  public boolean isBlock(String name) {
    int id = getCurrentInputSourceHardwareId(name);
    AbstractInput input = InputUtil.getInput(id);
    return input != null && input.isBlock();
  }

  public boolean isBlockEx(String name) {
    int id = getHardwareIdBySourceName(name);
    AbstractInput input = InputUtil.getInput(id);
    return input != null && input.isBlockEx();
  }

  public boolean isCurrentInputBlock(String path) {
    int id = getCurrentInputSourceHardwareId(path);
    AbstractInput input = InputUtil.getInput(id);
    return input != null && input.isBlock();
  }

  /*
    refer CommonIntegration.isCurrentSourceBlockEx()
    call CFG_NAV_BLOCKED_STATUS
   */
  @Deprecated
  public boolean isCurrentInputBlockEx(String path) {
    int id = getCurrentInputSourceHardwareId(path);
    AbstractInput input = InputUtil.getInput(id);
    return input != null && input.isBlockEx();
  }

  public void setBlock(String name, boolean isBlock) {
    int id = getHardwareIdBySourceName(name);
    int ret = -1;
    AbstractInput input = InputUtil.getInput(id);
    if(input != null) {
      ret =  input.block(isBlock);
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setBlock,ret is ====" + ret);
  }

  /**
   * isCurrentSourceBlocked current source is blocked or not
   */
  public boolean isCurrentSourceBlocked(String path) {
    return isCurrentInputBlock(path);
  }

  public boolean isTvInputBlocked() {
    return mTvInputSource.checkIsMenuTvBlock();
  }

  /**
   * reset input source
   */
  public void resetDefault() {
    List<AbstractInput> list = InputUtil.getSourceList();
    for (AbstractInput input : list) {
      if (input.isTV() || input.isDTV()) {
        changeInputSourceByHardwareId(input.getHardwareId());
      }
    }
  }

  /**
   * Input Info
   */
  public TvInputInfo getTvInputInfo(String path) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTvInputInfo, path=" + path);
    int id = getCurrentInputSourceHardwareId(path);
    AbstractInput input = InputUtil.getInput(id);
    if (input != null) {
      return input.getTvInputInfo();
    }

    if (path.equalsIgnoreCase(MAIN)) {
      return mMainTvInputInfo;
    } else if (path.equalsIgnoreCase(SUB)) {
      return mSubTvInputInfo;
    }

    return null;
  }

  /**
   * Input Id
   */
  public String getTvInputId(String path) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTvInputId, path=" + path);
    int id = getCurrentInputSourceHardwareId(path);
    AbstractInput input = InputUtil.getInput(id);
    if (input != null && input.getTvInputInfo() != null) {
      return input.getTvInputInfo().getId();
    }
    if (path.equalsIgnoreCase(MAIN) && mMainTvInputInfo != null) {
      return mMainTvInputInfo.getId();
    } else if (path.equalsIgnoreCase(SUB) && mSubTvInputInfo != null) {
      return mSubTvInputInfo.getId();
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTvInputId=null");
    return null;
  }

  public String getInputSourceByAPI(String focus) {
    return mTvInputSource.getCurrentInputSourceName(focus);
  }

  public void stopSession() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopSession");
    if (TurnkeyUiMainActivity.getInstance() != null && TurnkeyUiMainActivity.getInstance().getTvView() != null) {
      TurnkeyUiMainActivity.getInstance().getTvView().reset();
      TurnkeyUiMainActivity.getInstance().requestVisibleBehind(false);
    }
    SaveValue.writeWorldStringValue(mContext, MAIN_SOURCE_NAME,
        "Null", true);
    SaveValue.writeWorldInputType(mContext, Constants.INPUT_TYPE_OTHER);
  }

  public void stopSession(boolean isSetTVInfoNull) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopSession:" + isSetTVInfoNull);
    if (isSetTVInfoNull) {
      mMainTvInputInfo = null;
    }
    TurnkeyUiMainActivity.getInstance().getTvView().reset();
    SaveValue.writeWorldStringValue(mContext, MAIN_SOURCE_NAME,
        "Null", true);
  }

  public void stopPipSession() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopPipSession");

    // mSubTvInputInfo = null;
    TurnkeyUiMainActivity.getInstance().getPipView().reset();
  }

  public void stopPipSession(boolean isSetTVInfoNull) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopPipSession");
    if (isSetTVInfoNull) {
      mSubTvInputInfo = null;
    }
    TurnkeyUiMainActivity.getInstance().getPipView().reset();
  }

  /**
   * the interface is used to notify the status of source list
   */
  private final List<ISourceListListener> mRigister = new ArrayList<ISourceListListener>();// local

  public boolean addListener(ISourceListListener listener) {
    mRigister.add(listener);

    return true;
  }

  public boolean removeListener(ISourceListListener listener) {
    mRigister.remove(listener);

    return true;
  }

  public interface ISourceListListener {
    void onAvailabilityChanged(String inputId, int state);
  }

  public void saveOutputSourceName(String inputSourceName, String path) {
    if (MAIN.equals(path)) {
      mSaveValue.saveStrValue(MAIN_SOURCE_NAME, inputSourceName);
      SaveValue.writeWorldStringValue(mContext, MAIN_SOURCE_NAME,
          inputSourceName, true);
    } else {
      mSaveValue.saveStrValue(SUB_SOURCE_NAME, inputSourceName);
    }
  }

  public void swapMainAndSubSource() {
    String mainSourceName = getCurrentInputSourceName(MAIN);
    String subSourceName = getCurrentInputSourceName(SUB);
    saveOutputSourceName(subSourceName, MAIN);
    saveOutputSourceName(mainSourceName, SUB);
  }

  public String getCurrentChannelSourceName() {
    return getCurrentInputSourceName(CommonIntegration.getInstance().getCurrentFocus());
  }

  public TvInputInfo getTvInputInfo() {
    return null;
  }

  public ApplicationInfo getTvInputAppInfo(String inputId) {
    return DestroyApp.getSingletons().getTvInputManagerHelper().getTvInputAppInfo(inputId);
  }

  public void retryLoadSourceListAfterStartSession() {
    mHandler.postDelayed(retryLoadSourceListAfterStartSessionFailedRunnable, 200);
  }

  private Runnable retryLoadSourceListAfterStartSessionFailedRunnable = new Runnable() {

    @Override
    public void run() {
      List<TvInputInfo> inputs = mTvInputManager.getTvInputList();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "inputs size:" + (inputs == null ? -1 : inputs.size()));
      if (inputs == null || inputs.isEmpty()) {
        mHandler.postDelayed(this, 200);
      } else {
        resetCurrentInput();
      }
    }
  };

  public void release() {
    if (mHandler != null) {
      mHandler.removeCallbacksAndMessages(null);
    }
  }

  public int getHardwareIdByOriginalSourceName(String name) {
    int result = -1;
    if (!TextUtils.isEmpty(name)) {
      int size = mTvInputSource.getInputSourceTotalNumber();
      for (int i = 0; i < size; i++) {
        MtkTvInputSource.InputSourceRecord record = new MtkTvInputSource.InputSourceRecord();
        mTvInputSource.getInputSourceRecbyidx(i, record);
        String sourceName = mTvInputSource.getInputSourceNamebySourceid(record.getId());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "each records, name:" + sourceName);
        if (!TextUtils.isEmpty(sourceName) && sourceName.equalsIgnoreCase(name)) {
          result = InputUtil.checkInvalideInput(record.getId() << 16);
          break;
        }
      }
    }
    return result;
  }

  public void startLiveTV(Context context, int hardwareId) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startLiveTV,hardwareId=" + hardwareId);
    AbstractInput input = InputUtil.getInputById(hardwareId);
    if (input == null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "input == null");
      return;
    }
    saveInputSourceHardwareId(hardwareId, "main");
    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri uri = null;
    if (input.isATV() || input.isDTV() || input.isTV()) {
      uri = TvContract.Channels.CONTENT_URI.buildUpon()
          .appendQueryParameter("TunerType", String.valueOf(input.getType())).build();
    } else {
      uri = TvContract.buildChannelUriForPassthroughInput(input.getId());
    }
    intent.setData(uri);
    if (context instanceof Application) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }
}
