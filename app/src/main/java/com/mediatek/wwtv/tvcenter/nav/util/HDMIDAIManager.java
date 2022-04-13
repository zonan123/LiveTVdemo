package com.mediatek.wwtv.tvcenter.nav.util;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.mediatek.twoworlds.tv.MtkTvDAIBase;
import com.mediatek.twoworlds.tv.MtkTvTVCallbackHandler;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;


public class HDMIDAIManager {

  protected static final String TAG = "HDMIDAIManager";
  private TurnkeyUiMainActivity mActivity;
  private final ConnectivityManager mConnectivityManager;
  private static boolean mNetAvailable = false;
  private static boolean mHasSignal = false;
  private SimpleDialog mAlertDialog;

  private MtkTvDAIBase mtkTvDAI;
  private final TVCallBack mTVCallBack;

  protected static boolean popUI = true;
  protected static final int DAI_HDMI_RETRY = 99;
  protected static final int DAI_HDMI_RETRY_COUNT = 30;
  private int retry = 0;

  public HDMIDAIManager(TurnkeyUiMainActivity mainActivity) {
    mActivity = mainActivity;
    mConnectivityManager = ((ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE));
    mConnectivityManager.registerDefaultNetworkCallback(mNetworkCallback,new Handler(Looper.getMainLooper()));
    initHandler(mainActivity);
    mtkTvDAI = new MtkTvDAIBase();
    mtkTvDAI.Init();
    mTVCallBack = new TVCallBack();
  }

  private final NetworkCallback mNetworkCallback = new NetworkCallback() {

    @Override
    public void onLost(Network network) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onLost");
      if(mNetAvailable && mAlertDialog != null && mAlertDialog.isShowing()){
        mAlertDialog.dismiss();
        Toast.makeText(mActivity, R.string.network_not_avaiable, Toast.LENGTH_LONG).show();
      }
      mNetAvailable = false;
    }

    @Override
    public void onAvailable(Network network) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onAvailable");
      mNetAvailable = true;
      checkShowDialogCondition();
    }
  };

  public class TVCallBack extends MtkTvTVCallbackHandler {

    public int notifyDAIConsentUpdated(String url) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"notifyDAIConsentUpdated:" + url);
      if(DestroyApp.isCurActivityTkuiMainActivity() && mHasSignal && mNetAvailable &&
          (mAlertDialog == null || !mAlertDialog.isShowing())) {
        showConfirmDialog();
      }
      return 0;
    }
    
  }

  public void handleSvctxNotify(int type) {
    if(!CommonIntegration.getInstance().isCurrentSourceHDMI()) {
      mHasSignal = false;
      return;
    }
    if(type == 0 || type == 20 || type == 21 || type == 37 || type == 38) {
      mHasSignal = true;
    } else {
      mHasSignal = false;
      if((type == 5 || type == 10) && mAlertDialog != null && mAlertDialog.isShowing()) {
        mAlertDialog.dismiss();
      }
    }
    checkShowDialogCondition();
  }

  public void checkShowDialogCondition() {
    if(!DestroyApp.isCurActivityTkuiMainActivity()) {
      return;
    }

    int state=mtkTvDAI.GetConsentState();

    if (state == 1) {
      popUI = false;
    }

    if (state == -1 || state == -2) {
      mHandler.sendEmptyMessageDelayed(DAI_HDMI_RETRY, 1000);
    }

    if(mHasSignal && mNetAvailable && (state == 2 || popUI)) {
      showConfirmDialog();
    }
  }

  private void initHandler(Context context) {
    if(mHandler == null) {
      mHandler = new Handler(context.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
          if(msg.what == DAI_HDMI_RETRY) {
            if(retry <= DAI_HDMI_RETRY_COUNT) {
              if (mtkTvDAI.GetConsentState() == -1 || mtkTvDAI.GetConsentState() == -2) {
                mtkTvDAI = new MtkTvDAIBase();
                mtkTvDAI.Init();
                retry ++;
                mHandler.sendEmptyMessageDelayed(DAI_HDMI_RETRY, 1000);
              }
            }
          }
        }
      };
    }
  }

  private Handler mHandler;

  public void showConfirmDialog() {
    if (mAlertDialog == null) {
      mAlertDialog = (SimpleDialog)ComponentsManager.getInstance().
              getComponentById(NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
    }
    if(mAlertDialog != null && !mAlertDialog.isShowing()) {
      mAlertDialog.setContent(R.string.dai_hdmi_dialog_msg);
      mAlertDialog.setLevel(SimpleDialog.DIALOG_LEVEL_INFO);
      mAlertDialog.setConfirmText(R.string.pvr_confirm_yes);
      mAlertDialog.setCancelText(R.string.pvr_confirm_no);
      mAlertDialog.setOnConfirmClickListener(new SimpleDialog.OnConfirmClickListener() {
        @Override
        public void onConfirmClick(int dialogId) {
          loadDAIConsentWebPage();
        }
      },-1);
      mAlertDialog.setDefaultButton(SimpleDialog.ButtonType.Confirm);
      mAlertDialog.setCancelable(false);
      mAlertDialog.show();
    }
  }

  public void onPause() {
    if(mAlertDialog != null) {
      mAlertDialog.dismiss();
    }
  }

  public void release() {
    mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
    mTVCallBack.removeListener();
    mActivity = null;
    if (mHandler != null) {
      mHandler.removeCallbacksAndMessages(null);
    }
  }

  public void closeConsentForDAI() {
    mtkTvDAI.CloseConsent();
  }

  public void loadDAIConsentWebPage() {
    Intent intent = new Intent("com.mediatek.wwtv.webview.action.CUSTOMIZATION");
    intent.setPackage("com.mediatek.wwtv.webview");
    intent.putExtra("forDAI", true);
    try {
      mActivity.startActivity(intent);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
