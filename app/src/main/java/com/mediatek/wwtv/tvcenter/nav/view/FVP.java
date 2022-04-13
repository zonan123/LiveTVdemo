package com.mediatek.wwtv.tvcenter.nav.view;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.mediatek.support.fvp.FreeviewPlayClient;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvHBBTV;
import com.mediatek.twoworlds.tv.MtkTvHtmlAgentBase;
import com.mediatek.twoworlds.tv.MtkTvKeyEvent;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.commonview.ConfirmDialog.IResultCallback;
import com.mediatek.wwtv.tvcenter.epg.eu.EpgType;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.CommonUtil;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.KeyDispatch;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import mediatek.sysprop.VendorProperties;

public class FVP extends NavBasicMisc
    implements ComponentStatusListener.ICStatusListener, IResultCallback {
  private static final String TAG = "FVP";
  private Context context;
  private MtkTvKeyEvent mtkKeyEvent;
  private ProgressBar progressBar;
  boolean first = false;
  private boolean isActive = false;
  private int currentFVPState = -1;
  private final static String PACKAGE_NETWORK = "com.android.tv.settings";
  private final static String ACTIVITY_NETWORK = "com.android.tv.settings.connectivity.NetworkActivity";
  private final static int MSG_KEY_EPG = 5;
  private final static int MSG_KEY_FVP = 6;
  private final static int MSG_SHOW_PROGRESSBAR = 7;
  private final static int MSG_HIDE_PROGRESSBAR = 8;
  private CompositeDisposable mDisposables = new CompositeDisposable();
  private  final  static int ID = 0x9001;
  public FVP(Context mContext) {
    super(mContext);
    // TODO Auto-generated constructor stub

    this.componentID = NAV_NATIVE_COMP_ID_FVP;
    context = mContext;
    // Add component status Listener
    ComponentStatusListener.getInstance()
        .addListener(ComponentStatusListener.NAV_COMPONENT_HIDE, this);
    ComponentStatusListener.getInstance()
        .addListener(ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
    mtkKeyEvent = MtkTvKeyEvent.getInstance();

    ViewGroup layoutGroup =
        (ViewGroup) ((Activity) mContext).findViewById(android.R.id.content).getRootView();
    progressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleLarge);
    progressBar.setIndeterminate(true);

    RelativeLayout.LayoutParams params =
        new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    RelativeLayout rLayout = new RelativeLayout(mContext);
    rLayout.setGravity(Gravity.CENTER);
    rLayout.addView(progressBar);
    layoutGroup.addView(rLayout, params);
    hideProgressbar();
  }

  public void hideProgressbar() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hide==progressbar");
    progressBar.setVisibility(View.INVISIBLE);
  }

  public void showProgressbar() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "show==progressbar");
    progressBar.setVisibility(View.VISIBLE);
  }


  @Override
  public boolean isVisible() {
    // there is no UI in android, always false
    return false;
  }

  @Override
  public boolean isCoExist(int componentID) {
    return false;
  }

  @Override
  public boolean isKeyHandler(int keyCode) {
    switch (keyCode) {
      case KeyMap.KEYCODE_MTKIR_GUIDE:
      case KeyMap.KEYCODE_MTKIR_SUBCODE:
        return updateStatus(keyCode);
      default:
        break;
    }
    return false;
  }

  public void handlerFVPMessage(int type, int message) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "handlerFVPMessage, type=" + type + ", message=" + message);
    switch (type) {
      //case 0:
      case 2:
        {
          currentFVPState = type;
          ComponentsManager.getInstance().hideAllComponents();
          ComponentsManager.updateActiveCompId(true, NAV_NATIVE_COMP_ID_FVP);
          ComponentStatusListener.getInstance()
              .updateStatus(ComponentStatusListener.NAV_COMPONENT_SHOW, 0);
          break;
        }
      case 1:
        currentFVPState = type;
        if (ComponentsManager.getNativeActiveCompId() == NAV_NATIVE_COMP_ID_FVP) {
          ComponentsManager.updateActiveCompId(true, 0);
          ComponentStatusListener.getInstance()
              .updateStatus(ComponentStatusListener.NAV_COMPONENT_HIDE, 0);
        }
        break;
      case 3:
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "handlerFVPMessage  fail");
        break;
      default:
        break;
    }
    
    if (type == 2) {
      isActive = true;
    } else {
      isActive = false;
    }

    if(type != 0) {
      if(handler.hasMessages(MSG_KEY_EPG)) {
        handler.removeMessages(MSG_KEY_EPG);
        handler.sendEmptyMessage(MSG_HIDE_PROGRESSBAR);
        KeyDispatch.getInstance().passKeyToNative(KeyMap.KEYCODE_MTKIR_GUIDE, null);
      }

      else if(handler.hasMessages(MSG_KEY_FVP)) {
        handler.removeMessages(MSG_KEY_FVP);
        handler.sendEmptyMessage(MSG_HIDE_PROGRESSBAR);
        KeyDispatch.getInstance().passKeyToNative(KeyMap.KEYCODE_MTKIR_SUBCODE, null);
      }
    }
  }

  @Override
  public void updateComponentStatus(int statusID, int value) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus>>" + statusID + ">>>" + value);
    if (statusID == ComponentStatusListener.NAV_CHANNEL_CHANGED) {
    //  if (value != -1) {}
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"change channel");
    } else if (statusID == ComponentStatusListener.NAV_COMPONENT_HIDE) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus||currentFVPState =" + currentFVPState);
      if (currentFVPState == 0 || currentFVPState == 2) {
        ComponentsManager.updateActiveCompId(true, NAV_NATIVE_COMP_ID_FVP);
        ComponentStatusListener.getInstance()
            .updateStatus(ComponentStatusListener.NAV_COMPONENT_SHOW, 0);
      }
      if (!ComponentsManager.getInstance().isComponentsShow()) {
        if (DestroyApp.isCurActivityTkuiMainActivity() && DestroyApp.isCurTaskTKUI()) {
          ComponentsManager.nativeComponentReActive();
          FreeviewPlayClient.getInstance().disableTalkBack(true);
        }
      }
    } else if (statusID == ComponentStatusListener.NAV_COMPONENT_SHOW) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(
          TAG,
          "updateComponentStatus>>"
              + statusID
              + "---"
              + Integer.toHexString(ComponentsManager.getInstance().getActiveCompId()));
      switch (ComponentsManager.getInstance().getActiveCompId()) {
        case NAV_COMP_ID_CH_LIST:
        case NAV_COMP_ID_DIALOG_MSG:
        case NAV_COMP_ID_FAV_LIST:
        case NAV_COMP_ID_GINGA_TV:
        case NAV_COMP_ID_INPUT_SRC:
        case NAV_COMP_ID_PWD_DLG:
        case NAV_COMP_ID_CI:
        case NAV_COMP_ID_OAD:
        case NAV_COMP_ID_SAT_SEL:
        case NAV_COMP_ID_SUNDRY_DIALOG:
        case NAV_COMP_ID_MENU_OPTION_DIALOG:
        case NAV_COMP_ID_CI_DIALOG:
        case NAV_COMP_ID_TIFTIMESHIFT_VIEW:
        case NAV_COMP_ID_ONE_KEY_DIALOG:
        case NAV_COMP_ID_SIMPLE_DIALOG:
          FreeviewPlayClient.getInstance().disableTalkBack(false);
          break;
        default:
          break;
      }
    }
  }

  @Override
  public void handleUserSelection(int result) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleUserSelection, result==" + result);
  }

  public boolean updateStatus(int keyCode) {
    int fvp = VendorProperties.mtk_tif_fvp().orElse(0);
    boolean[] isFVPSupportArray = {false};
    MtkTvHBBTV mtkTvHBBTV = MtkTvHBBTV.getInstance();
    mtkTvHBBTV.getFVPSupport(isFVPSupportArray);

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(
        TAG,
        "isFVPSupport==="
            + isFVPSupportArray[0]
            + " fvp==="
            + fvp
            + ",isenable="
            + MtkTvConfig.getInstance().getConfigValue(MtkTvConfigTypeBase.CFG_MISC_MDS_TOU_STATE));
    if (CommonUtil.isSupportFVP(true)
        && CommonIntegration.getInstance().isCurrentSourceTv()
        && !CommonIntegration.getInstance().is3rdTVSource()) {
      List<TIFChannelInfo> channelList =
          TIFChannelManager.getInstance(mContext).getCurrentSVLChannelList();
      if ((channelList == null || channelList.isEmpty())
          && CommonIntegration.getInstance().getChannelActiveNumByAPIForScan() == 0) {
        Intent intent = new Intent(mContext, com.android.tv.onboarding.SetupSourceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
      } else if (!first && !isNetworkConnected()) {
        showInfoDialog(keyCode);
        return true;
      } else {
        if (ComponentsManager.getNativeActiveCompId() == NAV_NATIVE_COMP_ID_FVP) {
          return true;
        }
        KeyDispatch.getInstance()
            .passKeyToNative(keyCode, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        handlerFVPMessage(2, 0);
        return true;
      }
    }
    return false;
  }

  private boolean isNetworkConnected() {
    ConnectivityManager connMan =
        TurnkeyUiMainActivity.getInstance()
            .getSystemService(
                ConnectivityManager.class); // getSystemService(ConnectivityManager.class);
    NetworkInfo netInfo = connMan.getActiveNetworkInfo();
    if (netInfo == null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "NetworkInfo is null; network is not connected");
      return false;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isnetwork==" + netInfo.isConnected());
    return netInfo.isConnected();
  }

  private void showInfoDialog(final int keyCode){
    first = true;
    SimpleDialog simpleDialog = new SimpleDialog(context);
    simpleDialog.setContent(R.string.fvp_alert_dialog);
    simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
    simpleDialog.setScheduleDismissTime(10_000);
    simpleDialog.setCancelText(R.string.common_dialog_msg_no);
    simpleDialog.setConfirmText(R.string.common_dialog_msg_yes);
    simpleDialog.setDefaultButton(SimpleDialog.ButtonType.Confirm);
    simpleDialog.setOnConfirmClickListener(dialogId -> {
      Intent intent = new Intent(Intent.ACTION_MAIN);
      intent.setComponent(new ComponentName(PACKAGE_NETWORK,ACTIVITY_NETWORK));
      intent.putExtra(
              com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SRC,
              com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SRC_LIVE_TV);
      context.startActivity(intent);
    }, ID);
    simpleDialog.setOnCancelClickListener(dialogId -> {
      int dfbkeycode =  mtkKeyEvent.androidKeyToDFBkey(keyCode);
      mtkKeyEvent.sendKeyClick(dfbkeycode);
      handlerFVPMessage(2,0);
    }, ID);
    simpleDialog.show();
  }


  public boolean isFVPActive() {
    return isActive;
  }

  private final Handler handler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
          switch (msg.what) {
              case MSG_KEY_EPG:
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timeout!!! MSG_KEY_EPG");
                  KeyDispatch.getInstance().gotoPage(true);
                  hideProgressbar();
                  break;
              case MSG_KEY_FVP:
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timeout!!! MSG_KEY_FVP");
                  KeyDispatch.getInstance().gotoPage(false);
                  hideProgressbar();
                  break;
              case MSG_SHOW_PROGRESSBAR:
                  showProgressbar();
                  break;
              case MSG_HIDE_PROGRESSBAR:
                  hideProgressbar();
                  break;
          }

      }
  };

  @Override
  public void setVisibility(int visibility) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "FVP visibility===" + visibility);
    if (!isActive) {
      return;
    }
    super.setVisibility(visibility);
  }

  public boolean available(int keyCode) {
      Log.d(TAG, "FVP available, "+keyCode);
      if(!CommonUtil.isSupportFVP(true)) {
        Log.d(TAG, "available not supportFVP");
        return false;
      }
      MtkTvHtmlAgentBase mtkTvHtmlAgentBase = new MtkTvHtmlAgentBase();
      MtkTvHtmlAgentBase.HtmlAgentStatus status = mtkTvHtmlAgentBase.getCurrStatus();
      if(status != MtkTvHtmlAgentBase.HtmlAgentStatus.HTML_AGENT_STATUS_LOADING
          && status != MtkTvHtmlAgentBase.HtmlAgentStatus.HTML_AGENT_STATUS_UNKNOWN){
          return true;
      }else{
          mDisposables.add(Observable.interval(1, TimeUnit.SECONDS)
            .map(it -> mtkTvHtmlAgentBase.getCurrStatus())
              .doOnNext(it ->{com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"available status=" + it);})
              .filter(it -> it != MtkTvHtmlAgentBase.HtmlAgentStatus.HTML_AGENT_STATUS_LOADING
                  && it != MtkTvHtmlAgentBase.HtmlAgentStatus.HTML_AGENT_STATUS_UNKNOWN)
              .firstElement()
              .timeout(30, TimeUnit.SECONDS)
              .ignoreElement()
              .onErrorComplete()
              .doOnSubscribe(it-> showProgressbar())
              .subscribeOn(AndroidSchedulers.mainThread())
              .observeOn(AndroidSchedulers.mainThread())
              .doFinally(()-> hideProgressbar())
              .subscribe(()->{
                KeyDispatch.getInstance().gotoPage(KeyMap.KEYCODE_MTKIR_GUIDE == keyCode);
              }));
        return false;
      }
  }

  @Override public boolean deinitView() {
    handler.removeCallbacksAndMessages(null);
    mDisposables.clear();
    return super.deinitView();

  }
}
