
package com.mediatek.wwtv.tvcenter.nav.view.ciview;

import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.View;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.Display;
import android.widget.TextView;
import android.util.Log;
import android.util.TypedValue;

import com.android.tv.menu.MenuAction;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIStateChangedCallBack.CIMenuUpdateListener;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.PinDialogFragment.CancelBackListener;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.PinDialogFragment.ResultListener;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyDispatch;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.twoworlds.tv.model.MtkTvCIMMIEnqBase;
import com.mediatek.twoworlds.tv.model.MtkTvCIMMIMenuBase;
import com.mediatek.twoworlds.tv.common.MtkTvCIMsgTypeBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.MtkTvCIBase;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.twoworlds.tv.MtkTvConfig;

public class CIMainDialog extends NavBasicDialog implements ComponentStatusListener.ICStatusListener{

  private static final String TAG = "CIMainDialog";
    final CIMainDialog mCiMainDialog;
  private LayoutInflater inflater;
  private LinearLayout ciDialog,
                       mCiCamMenuLayout,
                       mCiNoCardLayout,
                       mCiMenuLayout,
                       mCiEnqLayout,
                       mCiUserPreferenceLayout;
  private CIViewType mCIViewType = CIViewType.CI_DATA_TYPE_CAM_MENU;
  private CIStateChangedCallBack mCIState = null;
  private TextView mCiCamMenu,
                   mCiPinCode,
                   mCiUserPreference;
  private TextView mCiNoCard,
                   mCiInfo;
  private TextView mCiMenuTitle,
                   mCiMenuName,
                   mCiMenuSubtitle,
                   mCiMenuBottom;
  private ListView mCiMenuList;
  private TextView mCiEnqTitle,
                   mCiEnqName,
                   mCiEnqSubtitle;
  private PinDialogFragment mCiEnqInput;
  private static final String MENU_CI_USER_PREFERENCE="menu_ci_user_preference";
  private static final String MENU_CI_USER_PREFERENCE_DEFAULT_ID="menu_ci_user_preference_default";
  private static final String MENU_CI_USER_PREFERENCE_AMMI_ID="menu_ci_user_preference_ammi";
  private static final String MENU_CI_USER_PREFERENCE_BROADCAST_ID="menu_ci_user_preference_broadcast";
  // for password
  private String mEditStr = "";// real string
  private int mCurrentIndex = 0; // index of string
  private char[] tempChar;// temp char
  char num;// input num
  private byte length;// editText length
  private String mPreEditStr = ""; // previous string
  boolean mFirstShow = false;// the first enter the password eidttext
  boolean mInputCharChange = true;// whether up or down key
  private boolean bNeedShowCamScan = false;// whether should show cam scam

  static boolean tryToCamScan = false;
  private int mEnqType = 1;// whether password or display num
  private String[] mCIGroup;
  private boolean isMmiItemBack = false;
  private int mmiMenuLevel = 0;
  private final Map<Integer, Integer> levelSelIdxMap;
  private CIStateChangedCallBack.CIMenuUpdateListener ciMenuUpdateListener;
  private boolean shouldDialogDismiss = false;
  //static AfterDealCIEnqListener AfdealCiEnqLser;
  private boolean dialogIsShow = false;
  private TvCallbackData data ;
  MenuConfigManager menuConfigManager;
  TIFChannelManager mTIFChannelManager;

  private final static int CHANNEL_LIST_SElECTED_FOR_TTS=19;
  private final static int SElECTED_CHANNEL_CAM_SCAN=20;

 /* public static void bindAfterDealCIEnqListener(AfterDealCIEnqListener lster) {
    AfdealCiEnqLser = lster;
  }*/

  public CIMainDialog(Context context) {
    this(context, R.style.nav_dialog);
  }

  public CIMainDialog(Context context, int theme) {
    super(context, theme);
    componentID = NAV_COMP_ID_CI_DIALOG;
    levelSelIdxMap = new HashMap<Integer, Integer>();
    mCiMainDialog = this;
    mContext = context;
    menuConfigManager = MenuConfigManager.getInstance(context);
    mTIFChannelManager= TIFChannelManager.getInstance(context);
    mCIState = CIStateChangedCallBack.getInstance(context);
    ComponentStatusListener lister = ComponentStatusListener.getInstance();
    lister.addListener(ComponentStatusListener.NAV_POWER_OFF, this);
  }

  public enum CIViewType {
    CI_DATA_TYPE_CAM_MENU, CI_DATA_TYPE_NO_CARD, CI_DATA_TYPE_MENU, CI_DATA_TYPE_ENQ,CI_DATA_TYPE_USER_PREFERENCE
  }

  public enum CIPinCapsType {
    CI_PIN_CAPS_NONE, CI_PIN_CAPS_CAS_ONLY, CI_PIN_CAPS_CAS_AND_FTA, CI_PIN_CAPS_CAS_ONLY_CACHED, CI_PIN_CAPS_CAS_AND_FTA_CACHED
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate ciDialog >>" + ciDialog);
      if(ciDialog == null){
          inflater = LayoutInflater.from(mContext);
          initDialog(mContext);
      }
//    showChildView(mCIViewType);
      setWindowPosition();
  }

  @Override
  public void show() {
	  if(StateDvrPlayback.getInstance() != null){
		  StateDvrPlayback.getInstance().dismissBigCtrlBar() ;
	  }
    super.show();
    mCiInfo.setText(mContext.getResources().getString(R.string.ci_title));
    showChildView(mCIViewType);
    dialogIsShow = true;
  }

  @Override
  public boolean deinitView(){
      if(mCiEnqLayout != null && mCiEnqInput != null){
          FragmentTransaction ft= TurnkeyUiMainActivity.getInstance().getFragmentManager().beginTransaction();
          ft.remove(mCiEnqInput);
          ft.commitNowAllowingStateLoss();
      }
      mCiEnqInput = null;
      super.deinitView();
      mHandler.removeCallbacksAndMessages(null);
      return false;
  }

  @Override
  public boolean isCoExist(int componentID) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"componentID>> "+componentID + ", simple Dialog>> "+NAV_COMP_ID_SIMPLE_DIALOG);
      if(componentID == NAV_COMP_ID_SIMPLE_DIALOG){
         int confirmClickId =
                 ((SimpleDialog) ComponentsManager.getInstance()
                         .getComponentById(NavBasic.NAV_COMP_ID_SIMPLE_DIALOG)).getConfirmClickId();
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"confirmClickId>> "+confirmClickId + ", CI Dialog>> "+NAV_COMP_ID_CI_DIALOG);
          if(confirmClickId ==  NAV_COMP_ID_CI_DIALOG){
              return true;
          }
      }
    return false;
  }

  public static void resetTryCamScan() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d("NavCI", "resetTryCamScan() tryToCamScan is:" + tryToCamScan);
    if (tryToCamScan) {
      tryToCamScan = false;
    }

  }

  private void setWindowPosition() {
    WindowManager m = getWindow().getWindowManager();
    Display display = m.getDefaultDisplay();
    Window window = getWindow();
    WindowManager.LayoutParams lp = window.getAttributes();
    TypedValue sca = new TypedValue();
    mContext.getResources().getValue(R.dimen.nav_ci_window_size_width,sca ,true);
    float w  = sca.getFloat();
    mContext.getResources().getValue(R.dimen.nav_ci_window_size_height,sca ,true);
    float h  = sca.getFloat();

    int menuWidth = (int) (display.getWidth() * w);
    int menuHeight = (int) (display.getHeight() * h);
    lp.width = menuWidth;
    lp.height = menuHeight;
    lp.gravity = Gravity.CENTER;
    window.setAttributes(lp);
  }

  public void setNeedShowCamScan(boolean bNeed) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setNeedShowCamScan:" + bNeed);
    bNeedShowCamScan = bNeed;
  }

  public void showChildView(CIViewType viewType) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "showChildView, viewType=" + viewType);
    mCIViewType = viewType;
    LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    switch (viewType) {
      case CI_DATA_TYPE_CAM_MENU:
        ciDialog.removeAllViews();
        ciDialog.addView(mCiCamMenuLayout, layoutParams);
        mCiCamMenu = (TextView) mCiCamMenuLayout.findViewById(R.id.menu_ci_cam_menu);
        mCiCamMenu.setText(mContext.getResources().getString(R.string.menu_setup_ci_cam_menu));
        mCiCamMenu.setOnKeyListener(mCardNameListener);
        mCiUserPreference = (TextView) mCiCamMenuLayout.findViewById(R.id.menu_ci_user_preference);
        mCiUserPreference.setText(mContext.getResources().getString(R.string.menu_ci_user_preference));
        mCiUserPreference.setOnKeyListener(mCardNameListener);
        int menuId = MtkTvCIBase.getCamPinCaps();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showChildView,menuID=" + menuId);
        if (menuId == CIPinCapsType.CI_PIN_CAPS_CAS_ONLY_CACHED.ordinal()
          || menuId == CIPinCapsType.CI_PIN_CAPS_CAS_AND_FTA_CACHED.ordinal()) {
          mCiPinCode = (TextView) mCiCamMenuLayout.findViewById(R.id.menu_ci_pin_code);
          mCiPinCode.setText(mContext.getResources().getString(R.string.menu_setup_ci_pin_code));
          mCiPinCode.setVisibility(View.VISIBLE);
          mCiPinCode.setOnKeyListener(mCardNameListener);
        }
        TextView mCiCamScan = (TextView) mCiCamMenuLayout.findViewById(R.id.menu_ci_cam_scan);
        mCiCamScan.setText(mContext.getResources().getString(R.string.menu_ci_cam_scan));
        if (bNeedShowCamScan) {
          if(CIStateChangedCallBack.getInstance(mContext).getCIHandle() != null
                  && 1 == CIStateChangedCallBack.getInstance(mContext).getCIHandle()
                  .getProfileSupport(CommonIntegration.getInstance().getTunerMode())){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "true = bNeedShowCamScan");
            mCiCamScan.setVisibility(View.VISIBLE);
            mCiCamScan.setOnKeyListener(mCardNameListener);
          }else {
              mCiCamScan.setVisibility(View.GONE);
          }
        }else if (TVContent.getInstance(mContext).isConfigVisible("g_misc__cam_profile_scan")) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "visible g_misc__cam_profile_scan");
            if(CIStateChangedCallBack.getInstance(mContext).getCIHandle() != null
                    && 1 == CIStateChangedCallBack.getInstance(mContext).getCIHandle()
                    .getProfileSupport(CommonIntegration.getInstance().getTunerMode())){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "g_misc__cam_profile_scan~");
                mCiCamScan.setVisibility(View.VISIBLE);
                mCiCamScan.setOnKeyListener(mCardNameListener);
            } else {
                mCiCamScan.setVisibility(View.GONE);
            }
        }else {
          mCiCamScan.setVisibility(View.GONE);
        }
        break;
      case CI_DATA_TYPE_NO_CARD:
        ciDialog.removeAllViews();
        ciDialog.addView(mCiNoCardLayout, layoutParams);
        mCiNoCard = (TextView) mCiNoCardLayout.findViewById(R.id.menu_ci_no_card);
        mCiNoCard.requestFocus();
        mCiNoCard.setOnKeyListener(mCardNameListener);
        break;
      case CI_DATA_TYPE_MENU:
        ciDialog.removeAllViews();
        ciDialog.addView(mCiMenuLayout, layoutParams);
        mCiMenuTitle = (TextView) mCiMenuLayout.findViewById(R.id.menu_ci_main_title);
        mCiMenuName = (TextView) mCiMenuLayout.findViewById(R.id.menu_ci_main_name);
        mCiMenuSubtitle = (TextView) mCiMenuLayout.findViewById(R.id.menu_ci_main_subtitle);
        mCiMenuBottom = (TextView) mCiMenuLayout.findViewById(R.id.menu_ci_main_bottom);
        mCiMenuList = (ListView) mCiMenuLayout.findViewById(R.id.menu_ci_main_list);
        mCiMenuList.setOnKeyListener(mMenuListKeyListener);
        mCiMenuList.setAccessibilityDelegate(mAccDelegateforCiList);
        break;
      case CI_DATA_TYPE_ENQ:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CI_DATA_TYPE_ENQ");
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ciDialog:"+ciDialog);
        ciDialog.removeAllViews();
        ciDialog.addView(mCiEnqLayout, layoutParams);
        mCiEnqTitle = (TextView) mCiEnqLayout.findViewById(R.id.menu_ci_enq_title);
        mCiEnqName = (TextView) mCiEnqLayout.findViewById(R.id.menu_ci_enq_name);
        mCiEnqSubtitle = (TextView) mCiEnqLayout.findViewById(R.id.menu_ci_enq_subtitle);
/*        mCiEnqInput = (PinDialogFragment) (((TurnkeyUiMainActivity) mContext).getFragmentManager()
            .findFragmentById(R.id.ci_input_pin_code));*/
        /*PinDialogFragment mCiEnqInputtetmp =  (PinDialogFragment) ((TurnkeyUiMainActivity) mContext).getFragmentManager().findFragmentByTag("mCiEnqInput");
        if(mCiEnqInputtetmp != null){
            ((TurnkeyUiMainActivity) mContext).getFragmentManager().beginTransaction().remove(mCiEnqInputtetmp);
        }
        mCiEnqInput = new PinDialogFragment();
        ((TurnkeyUiMainActivity) mContext).getFragmentManager().beginTransaction().replace(R.id.ci_input_pin_code,mCiEnqInput,"mCiEnqInput").commit();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ciDialog number key slid.");*/
        if(mCiEnqInput == null){
          mCiEnqInput = (PinDialogFragment) (((TurnkeyUiMainActivity) mContext).getFragmentManager()
              .findFragmentById(R.id.ci_input_pin_code));
            if(mCiEnqInput == null){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"mCiEnqInput is still null!");
                return;
            }
        }
        mCiEnqInput.setResultListener(new ResultListener() {

          @Override
          public void done(String pinCode) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "answerEnquiry:"+pinCode);
            mCIState.answerEnquiry(1, pinCode);
            mHandler.removeMessages(0xF5);
            mHandler.sendEmptyMessageDelayed(0xF5, 1000);
          }
        });
        mCiEnqInput.setCancelBackListener(new CancelBackListener(){

            @Override
            public void cancel() {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCiEnqInput keyback cancel");
//                mCIState.answerEnquiry(0, ""); for fix DTV02157283
                isMmiItemBack = true;
                mmiMenuLevel--;
                dismiss();
            }
        });
        mCiEnqInput.setPinCodeLength(mCIState.getAnsTextLen());
        mCiEnqInput.setBlindAns(mCIState.isBlindAns());
        mCiEnqInput.requestPickerFocus();
        break;
      case CI_DATA_TYPE_USER_PREFERENCE:
          //show user preference items
          ciDialog.removeAllViews();
          ciDialog.addView(mCiUserPreferenceLayout, layoutParams);
          TextView mCiUPDefault=(TextView) mCiUserPreferenceLayout.findViewById(R.id.menu_ci_user_preference_default);
          mCiUPDefault.setText(mContext.getResources().getString(R.string.menu_ci_user_preference_item_default));
          mCiUPDefault.setOnKeyListener(mUserPreferenceKeyListener);
          TextView mCiUPAmmi=(TextView) mCiUserPreferenceLayout.findViewById(R.id.menu_ci_user_preference_ammi);
          mCiUPAmmi.setText(mContext.getResources().getString(R.string.menu_ci_user_preference_item_ammi));
          mCiUPAmmi.setOnKeyListener(mUserPreferenceKeyListener);
          TextView mCiUPBroadcast=(TextView) mCiUserPreferenceLayout.findViewById(R.id.menu_ci_user_preference_broadcast);
          mCiUPBroadcast.setText(mContext.getResources().getString(R.string.menu_ci_user_preference_item_broadcast));
          mCiUPBroadcast.setOnKeyListener(mUserPreferenceKeyListener);
          String ciValue= SaveValue.getInstance(mContext).readStrValue(MENU_CI_USER_PREFERENCE);
          if(MENU_CI_USER_PREFERENCE_DEFAULT_ID.equals(ciValue)){
              mCiUPDefault.requestFocus();
          }else if(MENU_CI_USER_PREFERENCE_AMMI_ID.equals(ciValue)){
              mCiUPAmmi.requestFocus();
          }else if(MENU_CI_USER_PREFERENCE_BROADCAST_ID.equals(ciValue)){
              mCiUPBroadcast.requestFocus();
          }else{
              mCiUPDefault.requestFocus();
          }
          break;
      default:
        break;
    }
  }
  private void initDialog(Context context) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "initDialog"+context.toString());
    mCiCamMenuLayout = (LinearLayout) inflater.inflate(R.layout.menu_ci_cam_menu, null);
    mCiNoCardLayout = (LinearLayout) inflater.inflate(R.layout.menu_ci_no_card, null);
    mCiMenuLayout = (LinearLayout) inflater.inflate(R.layout.menu_ci_main, null);
    mCiEnqLayout = (LinearLayout) inflater.inflate(R.layout.menu_ci_enq, null);
    mCiUserPreferenceLayout=(LinearLayout) inflater.inflate(R.layout.menu_ci_user_preference, null);
    setContentView(R.layout.ci_dialog);
    setWindowPosition();
      ciDialog = (LinearLayout) findViewById(R.id.ci_dialog);
    mCiInfo = (TextView) findViewById(R.id.ci_title);
    ciMenuUpdateListener = new CIMenuUpdateListener() {

      @Override
      public void menuEnqClosed() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "menuEnqClosed");
        showChildView(CIViewType.CI_DATA_TYPE_NO_CARD);
        showNoCardInfo(mCIState.getCIHandle().getCamName());
        mCiNoCard.requestFocus();
        // mean entry not null so ,the last operate is TYPE_ENQ
        if (mCIState.getMtkTvCIMMIEnq() != null) {
          shouldDialogDismiss = true;
        }
        mHandler.sendEmptyMessage(0XF1);
      }

      @Override
      public void enqReceived(MtkTvCIMMIEnqBase enquiry) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "enqReceived,enquiry:" + enquiry);
        showChildView(CIViewType.CI_DATA_TYPE_ENQ);
        showCiEnqInfo(mCIState.getCIName(), "", enquiry.getText());
        showCiEnqInfo(mCIState.getCIHandle().getCamName(), "", enquiry.getText());
        // send msg to escape may dismiss the ci info dialog
        mHandler.sendEmptyMessage(0XF2);
        mHandler.removeMessages(0xF4);
      }

      @Override
      public void menuReceived(MtkTvCIMMIMenuBase menu) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "menuReceived, menu=" + menu);

        showChildView(CIViewType.CI_DATA_TYPE_MENU);
        if(menu != null ){
            if( menu.getItemList()== null){
            String[] list={"back"};
            menu.setItemList(list);
        }
        showCiMenuInfo(mCIState.getCIHandle().getCamName(), menu.getTitle(), menu.getSubtitle(),
            menu.getBottom(), menu.getItemList());
        mHandler.removeMessages(0xF4);
        mHandler.sendEmptyMessageDelayed(0xF4, 60000);
        }
      }

      @Override
      public void ciRemoved() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ciRemoved");
        showChildView(CIViewType.CI_DATA_TYPE_NO_CARD);
        showNoCardInfo(mContext.getString(R.string.menu_setup_ci_no_card));
        bNeedShowCamScan = false;
      }

      @Override
      public void ciCamScan(int message) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ciCamScan");
        if(checkPvrAndTimeShiftStatus()){
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "pvr or timeshift is running!");
          return;
        }
        if(CIStateChangedCallBack.getInstance(mContext).getCIHandle() != null
                && 0== CIStateChangedCallBack.getInstance(mContext).getCIHandle()
                .getProfileSupport(CommonIntegration.getInstance().getTunerMode())){
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "not support!");
          return;
        }
        camScanReqShow(message);
      }
    };
    mCiEnqInput = (PinDialogFragment) (((TurnkeyUiMainActivity) mContext).getFragmentManager()
            .findFragmentById(R.id.ci_input_pin_code));
  }

  public void handleCIMessageDelay(TvCallbackData data) {
    Message msg = mHandler.obtainMessage();
    msg.what = 0XF3;
    msg.obj = data;
    mHandler.sendMessageDelayed(msg, 2500);
  }
  public void handleCIMessage(TvCallbackData data) {
    int navcompid = ComponentsManager.getNativeActiveCompId();
      if(data != null && mContext != null){
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleCIMessage:" + data.param2+",nav-components-id=="+navcompid);
      } else {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleCIMessage, data is null!!!");
          return;
      }
 /*   if(navcompid == NavBasic.NAV_NATIVE_COMP_ID_MHEG5){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleCIMessage: now nav-components-id is mheg5");
        needShowInfoDialog = false ;
    }else{
    }*/
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleCIMessage: isScanning ,not show dialog  "+TVContent.getInstance(mContext).isScanning());
    if(DestroyApp.isCurScanActivity()){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleCIMessage: isScanning ,not show dialog");
        return;
    }

      int curDensity = (int) mContext.getResources().getDisplayMetrics().density;
      if(data.param2 == 1){
          Toast toast1 = Toast.makeText(mContext, mContext.getResources()
                  .getString(R.string.ci_inserted_msg), Toast.LENGTH_SHORT);
          toast1.setGravity(Gravity.BOTTOM|Gravity.END, 20 * curDensity, 20 * curDensity);
          toast1.show();
      } else if(data.param2 == 2){
          Toast toast2 = Toast.makeText(mContext, mContext.getResources()
                  .getString(R.string.ci_removed_msg), Toast.LENGTH_SHORT);
          toast2.setGravity(Gravity.BOTTOM|Gravity.END, 20 * curDensity, 20 * curDensity);
          toast2.show();
      }

    if(ciDialog == null){
        inflater = LayoutInflater.from(mContext);
        initDialog(mContext);
    }
    mHandler.removeMessages(0xF5); // every message cancel 0xF5 handle
    CIStateChangedCallBack.getInstance(mContext).handleCiCallback(mContext, data,
        ciMenuUpdateListener);

    boolean needShowInfoDialog = false;
    if(data.param2 == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_MMI_ENQUIRY||
            data.param2 == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_MMI_MENU ||
            data.param2 == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_MMI_LIST){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleCIMessage need show dialog");
        needShowInfoDialog = true;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleCIMessage needShowInfoDialog: "+needShowInfoDialog);

    if(data.param2 == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_PROFILE_SEARCH_CANCELED
          ||  data.param2 == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_PROFILE_SEARCH_ENDED){
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleCIMessage:select channel start:");
       mHandler.removeMessages(SElECTED_CHANNEL_CAM_SCAN);
       mHandler.sendEmptyMessageDelayed(SElECTED_CHANNEL_CAM_SCAN, 1000);

    }
    if (!isShowing() && needShowInfoDialog) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleCIMessage:show cidialog:");
        //send finish livetvsettings app broadcast
        Intent intent = new Intent("finish_live_tv_settings");
        mContext.sendBroadcast(intent, "com.mediatek.tv.permission.BROADCAST");
        ComponentsManager.getInstance().showNavComponent(NavBasic.NAV_COMP_ID_CI_DIALOG);
        mHandler.removeMessages(0xF4);
        mHandler.sendEmptyMessageDelayed(0xF4, 60000);
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
      // TODO Auto-generated method stub
      return true;
  }

  @Override
  public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
    if (mCIState.camUpgradeStatus()) {
      return true;
    }
    return super.onKeyHandler(keyCode, event, fromNative);
  }

  @Override
  public boolean onKeyHandler(int keyCode, KeyEvent event) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "onKeyHandler ");
    return onKeyHandler(keyCode, event, false);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
      Log.v(TAG, "dispatchKeyEvent action: "+ event.getAction()+",event>>>"+event);
	  if (event.getAction() == KeyEvent.ACTION_DOWN) {
          int keyCode = event.getKeyCode();
	      if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
	          || keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
	          || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
	        mHandler.removeMessages(0xF4);
	        mHandler.sendEmptyMessageDelayed(0xF4, 60000);
	      }
	    }
	return mCIState.camUpgradeStatus() || super.dispatchKeyEvent(event);
  }
  final  View.OnKeyListener mCiUserPreferenceListener = new View.OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
          if (event.getAction() == KeyEvent.ACTION_DOWN) {
              if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                  if(v.getId() == R.id.menu_ci_user_preference){
                      //user preference
                       showChildView(CIViewType.CI_DATA_TYPE_USER_PREFERENCE);
                  }
              }
          }
          return false;
      }
  };
  private final View.OnKeyListener mCardNameListener = new View.OnKeyListener() {

      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        boolean isHandled = true;
        if (mCIState.camUpgradeStatus()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mCardNameListener cam upgrading..., disable key process");
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mCardNameListener, keyCode=" + keyCode);
          if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                  && event.getRepeatCount() <= 0) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCIViewType:" + mCIViewType);
            if (mCIViewType == CIViewType.CI_DATA_TYPE_CAM_MENU) {
              if (v.getId() == R.id.menu_ci_cam_menu) {
                showChildView(CIViewType.CI_DATA_TYPE_NO_CARD);
                showNoCardInfo(CIStateChangedCallBack.getInstance(mContext).getCIName());
              } else if (v.getId() == R.id.menu_ci_cam_scan) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start cam scan true");
                if(!checkPvrAndTimeShiftStatus()){
                  int tunerMode= MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TUNER_MODE);
                  if(tunerMode>1){
                    camScanSatelliteReqShow();
                  }else{
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("NavCI", "camScanReqShow startcamScan true");
                    mCIState.getCIHandle().startCamScan(true);
                  }
                }
              } else if(v.getId() == R.id.menu_ci_user_preference){
                  //user preference
                  showChildView(CIViewType.CI_DATA_TYPE_USER_PREFERENCE);
              }else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CIPinCodeDialog show");
                CIPinCodeDialog dialog = CIPinCodeDialog.getInstance(mContext);
                dialog.setCIStateChangedCallBack(mCIState);
                dialog.show();
              }
            } else {// has card
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCamActive:" + mCIState.isCamActive());
              if (mCIState.isCamActive() == true) {
                if (mCIState.getCIHandle().getMenuListID() != -1
                    || mCIState.getCIHandle().getEnqID() != -1) {
                  mCIState.getCIHandle().setMMICloseDone();
                }
                mCIState.getCIHandle().enterMMI();
              }
            }
          } else if (keyCode == KeyEvent.KEYCODE_BACK) {
        	  if(KeyDispatch.getScanCode(keyCode, event) != -1){
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "key exit");
                  dismiss();
              }else{
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "key back");
                  if (mCIViewType != CIViewType.CI_DATA_TYPE_CAM_MENU && mCiCamMenu != null) {//
                    showChildView(CIViewType.CI_DATA_TYPE_CAM_MENU);
                    mCiCamMenu.requestFocus();
                    return true;
                  }
              }
          } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");
          } else if (keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN || keyCode == KeyEvent.KEYCODE_CHANNEL_UP
              || keyCode == KeyMap.KEYCODE_MTKIR_STOP) {
              isHandled = false;
          }
        }
        if (isHandled == false && TurnkeyUiMainActivity.getInstance() != null) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TurnkeyUiMainActivity");
          dismiss();
          return TurnkeyUiMainActivity.getInstance().onKeyHandler(keyCode, event);
        }
        return false;
      }
    };
  // listener for cardListView
  private final View.OnKeyListener mMenuListKeyListener = new View.OnKeyListener() {
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mMenuListKeyListener, onKey, keyCode=" + keyCode);
      if (mCIState.camUpgradeStatus()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mMenuListKeyListener cam upgrading..., disable key process");
        return true;
      }
      if (event.getAction() == KeyEvent.ACTION_DOWN) {
        int position = mCiMenuList.getSelectedItemPosition();
        switch (keyCode) {
          case KeyEvent.KEYCODE_DPAD_CENTER:
          case KeyEvent.KEYCODE_ENTER:
              if(event.getRepeatCount() <= 0){
                  if (position < 0) {
                      position = 0;
                  }
                  mCIState.selectMenuItem(position);
                  levelSelIdxMap.put(mmiMenuLevel, mCiMenuList.getSelectedItemPosition());
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d("levelSelIdxMap", "enter pos--idx==" + mCiMenuList.getSelectedItemPosition()
                          + ",level==" + mmiMenuLevel);
                  mmiMenuLevel++;
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mmiMenuLevel++:"+mmiMenuLevel);
              }
            break;

          case KeyEvent.KEYCODE_BACK:
        	  if(KeyDispatch.getScanCode(keyCode, event) != -1){
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "key exit");
                  dismiss();
                  return true;
              }else{
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "key back");
                  isMmiItemBack = true;
                  mmiMenuLevel--;
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mmiMenuLevel--");
                  mCIState.cancelCurrMenu();
                  return true;
              }
          case KeyEvent.KEYCODE_MENU:
            dismiss();
            return true;
          case KeyMap.KEYCODE_MTKIR_CHUP:
          case KeyMap.KEYCODE_MTKIR_CHDN:
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TurnkeyUiMainActivity");
              if ( TurnkeyUiMainActivity.getInstance() != null) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TurnkeyUiMainActivity.getInstance()");
                  dismiss();
                  return TurnkeyUiMainActivity.getInstance().onKeyHandler(keyCode, event);
               }
           default:
               break;
        }
      }
      return false;
    }
  };
  // listener for enq
  final View.OnKeyListener mEnqInputKeyListener = new View.OnKeyListener() {

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
      if (event.getAction() == KeyEvent.ACTION_DOWN) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mEnqInputKeyListener,onKey, keyCode=" + keyCode);
        switch (keyCode) {
          case KeyEvent.KEYCODE_DPAD_CENTER:
          case KeyEvent.KEYCODE_ENTER:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mEditStr:" + mEditStr);
            mCIState.answerEnquiry(1, mEditStr);
            break;

          case KeyEvent.KEYCODE_BACK:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "keyback");
            mCIState.answerEnquiry(0, "");
            isMmiItemBack = true;
            mmiMenuLevel--;
            return true;
          default:
            break;
        }
      }
      return false;
    }
  };

  // listener for user preference
  private final View.OnKeyListener mUserPreferenceKeyListener = new View.OnKeyListener() {

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
      if (event.getAction() == KeyEvent.ACTION_DOWN) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mEnqInputKeyListener,onKey, keyCode=" + keyCode);
        switch (keyCode) {
          case KeyEvent.KEYCODE_DPAD_CENTER:
          case KeyEvent.KEYCODE_ENTER:
              if(v.getId()==R.id.menu_ci_user_preference_default){
                  //set param
                  menuConfigManager.setValue(MenuConfigManager.USER_PREFERENCE,0);
                  backToLastPage();
                  SaveValue.getInstance(mContext).saveStrValue(MENU_CI_USER_PREFERENCE, MENU_CI_USER_PREFERENCE_DEFAULT_ID);
              }else if(v.getId()==R.id.menu_ci_user_preference_ammi){
                //set param
            	  menuConfigManager.setValue(MenuConfigManager.USER_PREFERENCE,1);
                  backToLastPage();
                  SaveValue.getInstance(mContext).saveStrValue(MENU_CI_USER_PREFERENCE, MENU_CI_USER_PREFERENCE_AMMI_ID);
              }else if(v.getId()==R.id.menu_ci_user_preference_broadcast){
                //set param
            	  menuConfigManager.setValue(MenuConfigManager.USER_PREFERENCE,2);
                  backToLastPage();
                  SaveValue.getInstance(mContext).saveStrValue(MENU_CI_USER_PREFERENCE, MENU_CI_USER_PREFERENCE_BROADCAST_ID);
              }
            break;
          case KeyEvent.KEYCODE_BACK:
              showChildView(CIViewType.CI_DATA_TYPE_CAM_MENU);
              mCiUserPreference.requestFocus();
              return true;
          default:
            break;
        }
      }
      return false;
    }
  };

  private void backToLastPage(){
      showChildView(CIViewType.CI_DATA_TYPE_CAM_MENU);
      mCiUserPreference.requestFocus();
  }
  // numkey Listener
  NumberKeyListener numberKeyListener = new NumberKeyListener() {

    @Override
    protected char[] getAcceptedChars() {
      return new char[] {
        'a'
      };
    }

    @Override
    public int getInputType() {
      return 0;
    }
  };

  // for no card or card name
  public void showNoCardInfo(String cardName) {
    if (cardName == null) {
      cardName = "";
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "showNoCardInfo, cardName=" + cardName);
    mmiMenuLevel = 0;
    levelSelIdxMap.clear();
    if (cardName == null || cardName.length() == 0) {
      // no card insert
      cardName = mContext.getString(R.string.menu_setup_ci_no_card);
    }
    mCiNoCard.setText(cardName.trim());
  }

  // for data_menu
  public void showCiMenuInfo(String cardName, String cardTitle, String cardSubtitle,
      String cardBottom, String[] cardListData) {
    if (cardTitle == null) {
      cardTitle = "";
    }
    if (cardName == null) {
      cardName = "";
    }
    if (cardSubtitle == null) {
      cardSubtitle = "";
    }
    if (cardBottom == null) {
      cardBottom = "";
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "showCiMenuInfo, cardTitle=" +cardName   + ",cardName=" + cardTitle
        + ",cardSubtitle=" + cardSubtitle + ",cardBottom=" + cardBottom);

    mCiMenuTitle.setText(cardName.trim());
    mCiMenuName.setText(cardTitle.trim());
    mCiMenuSubtitle.setText(cardSubtitle.trim());
    mCiMenuBottom.setText(cardBottom.trim());
    List<String> tempItemList = new ArrayList<String>();
    for (String s : cardListData) {
      if (!TextUtils.isEmpty(s)) {
        tempItemList.add(s);
      } else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "a empty item so needn't to show");
      }
    }
    mCIGroup = tempItemList.toArray(new String[0]);
    for (int i = 0; i < mCIGroup.length; i++) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCIGroup[" + i + "]=" + mCIGroup[i]);
    }
    CIListAdapter mCiAdapter = new CIListAdapter(mContext);
    mCiAdapter.setCIGroup(mCIGroup);
    mCiMenuList.setAdapter(mCiAdapter);
    mCiAdapter.notifyDataSetChanged();
    mCiMenuList.setFocusable(true);
    mCiMenuList.requestFocus();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isMmiItemBack:"+isMmiItemBack+",levelSelIdxMap:"+levelSelIdxMap+",mmiMenuLevel:"+mmiMenuLevel);
    if (isMmiItemBack && levelSelIdxMap != null && !levelSelIdxMap.isEmpty()) {
      isMmiItemBack = false;
      int key = mmiMenuLevel-1;
      if(key <0) {
          key =0;
      }
      int idx = levelSelIdxMap.get(key);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d("levelSelIdxMap", "idx==" + idx + ",level==" + (key));
      mCiMenuList.setSelection(levelSelIdxMap.get(key));
    }
  }

  public void camScanReqShow(int message) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "camScanReqShow() message is:" + message);
    // MTKTV_CI_CAM_SCAN_ENQ_SCHEDULE is send many times
    // so check is camScan = true to escape show many dialog
      SimpleDialog camScanCofirm = (SimpleDialog) ComponentsManager
              .getInstance().getComponentById(NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
      if(camScanCofirm == null){
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "camScanReqShow() camScanCofirm == null!");
          return;
      }
      if (message == MtkTvCIMsgTypeBase.MTKTV_CI_CAM_SCAN_ENQ_SCHEDULE) {
        if ((camScanCofirm != null && camScanCofirm.isShowing()) || tryToCamScan) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MTKTV_CI_CAM_SCAN_ENQ_SCHEDULE camScanReqShowed needn't show again try:"
            + tryToCamScan);
          return;
      }
    }
      mCIState = CIStateChangedCallBack.getInstance(mContext);

      camScanCofirm.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);

    if (message == MtkTvCIMsgTypeBase.MTKTV_CI_CAM_SCAN_ENQ_WARNING) {
        bNeedShowCamScan = true;
        camScanCofirm.setContent(R.string.cam_scan_warning);
    } else if (message == MtkTvCIMsgTypeBase.MTKTV_CI_CAM_SCAN_ENQ_URGENT) {
        bNeedShowCamScan = true;
        camScanCofirm.setContent(R.string.cam_scan_urgent);
    } else if (message == MtkTvCIMsgTypeBase.MTKTV_CI_CAM_SCAN_ENQ_NOT_INIT) {
        bNeedShowCamScan = true;
        camScanCofirm.setContent(R.string.cam_scan_not_init);
    } else if (message == MtkTvCIMsgTypeBase.MTKTV_CI_CAM_SCAN_ENQ_SCHEDULE) {
        bNeedShowCamScan = true;
        camScanCofirm.setContent(R.string.cam_scan_schedule);
    }

      camScanCofirm.setConfirmText(R.string.menu_ok);
      camScanCofirm.setCancelText(R.string.menu_cancel);
      camScanCofirm.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
      camScanCofirm.show();
      camScanCofirm.setOnDismissListener(null);
      camScanCofirm.setOnKeyListener(new DialogInterface.OnKeyListener() {
          @Override
          public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            int action = event.getAction();
            if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "camScanReqShow back button, start camscan with false");
                camScanCofirm.dismiss();
                mCIState.getCIHandle().startCamScan(false);
              return true;
            }
            return false;
          }
      });

      camScanCofirm.setOnConfirmClickListener(new SimpleDialog.OnConfirmClickListener() {
          @Override
          public void onConfirmClick(int dialogId) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dialogId>> "+dialogId);
              if(dialogId == NavBasic.NAV_COMP_ID_CI_DIALOG){

                  if(checkPvrAndTimeShiftStatus()){
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "pvr or timeshift is running!");
                      return;
                  }
                  tryToCamScan = true;
                  int tunerMode= MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TUNER_MODE);
                  if(tunerMode>1){
                      mHandler.removeMessages(0XF7);
                      mHandler.sendEmptyMessageDelayed(0XF7, 200);
                  }else if(mCIState != null){
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "camScanReqShow OK, startcamScan true");
                      mCIState.getCIHandle().startCamScan(true);
                  }
              }
          }
      }, NavBasic.NAV_COMP_ID_CI_DIALOG);

      camScanCofirm.setOnCancelClickListener(new SimpleDialog.OnCancelClickListener() {
          @Override
          public void onCancelClick(int dialogId) {
              if(dialogId == NavBasic.NAV_COMP_ID_CI_DIALOG && mCIState != null){
                  mCIState.getCIHandle().startCamScan(false);
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "camScanReqShow cancel, startcamScan false");
              }
          }
      }, NavBasic.NAV_COMP_ID_CI_DIALOG);
  }

  public void camScanSatelliteReqShow() {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "camScanSatelliteReqShow()");
      SimpleDialog camScanSatelliteCofirm = (SimpleDialog) ComponentsManager
              .getInstance().getComponentById(NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
      camScanSatelliteCofirm.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
      camScanSatelliteCofirm.setContent(R.string.cam_scan_set_satellte);
      camScanSatelliteCofirm.setConfirmText(R.string.menu_ok);
      camScanSatelliteCofirm.setCancelText(R.string.menu_cancel);
      camScanSatelliteCofirm.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
      camScanSatelliteCofirm.show();

      camScanSatelliteCofirm.setOnDismissListener(null);
      camScanSatelliteCofirm.setOnKeyListener(new DialogInterface.OnKeyListener() {
          @Override
          public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
              int action = keyEvent.getAction();
              if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "camScanSatelliteReqShow back button,startcamScan false");
                  camScanSatelliteCofirm.dismiss();
                  if(mCIState != null){
                      mCIState.getCIHandle().startCamScan(false);
                  }
                  return true;
              }
              return false;
          }
      });

      camScanSatelliteCofirm.setOnConfirmClickListener(new SimpleDialog.OnConfirmClickListener() {
          @Override
          public void onConfirmClick(int dialogId) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "camScanSatelliteReqShow yes button, jump to showCamScanOperators");
              if(dialogId == NavBasic.NAV_COMP_ID_CI_DIALOG && mCIState != null){
                  tryToCamScan = true;
                  MenuAction.showCamScanOperators(mContext);
              }
          }
      },NavBasic.NAV_COMP_ID_CI_DIALOG);

      camScanSatelliteCofirm.setOnCancelClickListener(new SimpleDialog.OnCancelClickListener() {
          @Override
          public void onCancelClick(int dialogId) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "camScanSatelliteReqShow no button, startcamScan true!!!");
              if(dialogId == NavBasic.NAV_COMP_ID_CI_DIALOG && mCIState != null){
                  mCIState.getCIHandle().startCamScan(true);
              }
          }
      },NavBasic.NAV_COMP_ID_CI_DIALOG);
  }

  // for data_enq
  private void showCiEnqInfo(String cardTitle, String cardName, String cardSubtitle) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showCiEnqInfo:title->"+cardTitle+",cardName->"+cardName+",subtitle->"+cardSubtitle);
    if (cardTitle == null) {
      cardTitle = "";
    }
    if (cardName == null) {
      cardName = "";
    }
    if (cardSubtitle == null) {
      cardSubtitle = "";
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "showCiMenuInfo, cardTitle=" + cardTitle + ",cardName=" + cardName
        + ",cardSubtitle=" + cardSubtitle);

    mHandler.removeMessages(1);
    mCiEnqTitle.setText(cardTitle.trim());
    mCiEnqName.setText(cardName.trim());
    mCiEnqSubtitle.setText(cardSubtitle.trim());
    mEditStr = "";
    mCurrentIndex = 0;
    mInputCharChange = false;
    if (mCIState.isBlindAns()) {
      mEnqType = 1;
      mPreEditStr = "_";
    } else {
      mEnqType = 0;
      mPreEditStr = "_";
    }
    mFirstShow = true;
    // reset text
    if ((mCIState.getAnsTextLen() & (byte) 0xf0) == 0) {
      length = mCIState.getAnsTextLen();
    } else {
      length = (byte) 0x0f;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "---------length------" + length);
  }


  private final Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case 0XF1:
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "enq Closed send msg to dismiss");
          if (shouldDialogDismiss) {
            shouldDialogDismiss = false;
          /*  if (AfdealCiEnqLser != null) {
              AfdealCiEnqLser.enqPinWaitInputState(false);
              AfdealCiEnqLser.enqPinSuccess();
            }*/

          }
          dismiss();
          break;
        case 0XF2:
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "enq enter send remove msg");
          shouldDialogDismiss = false;
    /*      if (AfdealCiEnqLser != null) {
            AfdealCiEnqLser.enqPinWaitInputState(true);
          }*/
          removeMessages(0XF1);
          break;
        case 0XF3:
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleCi message delay remove msg");
          // removeMessages(0XF3);
          boolean isPWDShow = MtkTvPWDDialog.getInstance().PWDShow() == 0;
          if(!isPWDShow){
              TvCallbackData data = (TvCallbackData) msg.obj;
              handleCIMessage(data);
          }else{

              data = (TvCallbackData) msg.obj;
          }

          break;
        case 0xF4:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CIMainDialog after 1min dismiss");
//            dismiss();//for fix DTV02174850
            break;

        case CHANNEL_LIST_SElECTED_FOR_TTS:
        	mCiMenuList.setSelection(msg.arg1);
        	break;

        case 0xF5:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Cam  1s no answer , pop toast ");
            Toast.makeText(mContext, mContext.getString(R.string.menu_setup_ci_10s_answer_tip),
                    Toast.LENGTH_LONG).show();
            break;
        case SElECTED_CHANNEL_CAM_SCAN:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleMessage SElECTED_CHANNEL_CAM_SCAN");
            if(DestroyApp.isCurActivityTkuiMainActivity()){
	            if( mTIFChannelManager.hasActiveChannel()){
	                TIFChannelInfo mTIFChannelInfo= mTIFChannelManager.getCurrChannelInfo();
	                if(mTIFChannelInfo == null){
	                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleMessage SElECTED_CHANNEL_CAM_SCAN currentchanenl is null.");
	                    if(mTIFChannelManager.getAllDTVTIFChannels().size() > 0){
	                        mTIFChannelInfo=mTIFChannelManager.getAllDTVTIFChannels().get(0);
	                    }
	                }
	                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleCIMessage:select channel mTIFChannelInfo :"+mTIFChannelInfo);
	                mTIFChannelManager.selectChannelByTIFInfo(mTIFChannelInfo);
	                mCIState.getCIHandle().setScanComplete();
	                TVContent.getInstance(mContext).setConfigValue(MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
	            }
	            mCIState.insertOrRemove=MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_CARD_INSERT;
            }
            break;
        case 0xF6:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "PWD dialog dismiss ,deal ci last mess ");
            // removeMessages(0XF3);
            boolean isPWDShow1 = MtkTvPWDDialog.getInstance().PWDShow() == 0;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "PWD dialog dismiss ,deal ci last mess isPWDShow is"+isPWDShow1);
            if(data != null){
              TvCallbackData dataTemp = data;
              data=null;
              handleCIMessage(dataTemp);
            }
            break;
          case 0xF7:
              camScanSatelliteReqShow();
              break;
          default:
            break;
      }
      // setPassword();
    }
  };
  public Handler getHandler() {
      return this.mHandler;
  }
  // handler set password
  void setPassword() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setPassword,mEnqType:" + mEnqType);
    if (mEnqType == 1) {
      mInputCharChange = false;
      mEditStr = mPreEditStr;
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentIndex:" + mCurrentIndex + ",mEditStr:" + mEditStr + ",length:"
          + length);
      if (mCurrentIndex <= mEditStr.length() - 1 && mEditStr.length() < length) {
        mCurrentIndex++;
        if (mCurrentIndex > mEditStr.length() - 1) {
          mPreEditStr = mPreEditStr + "_";
          tempChar = mPreEditStr.toCharArray();
          for (int i = 0; i < tempChar.length - 1; i++) {
            tempChar[i] = '*';
          }
          tempChar[tempChar.length - 1] = '_';
        } else {
          tempChar = mEditStr.toCharArray();
          for (int i = 0; i < mEditStr.length(); i++) {
            tempChar[i] = '*';
          }
        }
      } else if (mCurrentIndex > mEditStr.length() - 1 && mEditStr.length() < length) {
        mCurrentIndex++;
        if (mCurrentIndex <= length - 1) {
          mPreEditStr = mPreEditStr + "_";
          tempChar = mPreEditStr.toCharArray();
          for (int i = 0; i < tempChar.length - 1; i++) {
            tempChar[i] = '*';
          }
          tempChar[tempChar.length - 1] = '_';
        } else {
          mCurrentIndex = length - 1;
          mPreEditStr = mPreEditStr + "_";
          tempChar = mPreEditStr.toCharArray();
          for (int i = 0; i < tempChar.length; i++) {
            tempChar[i] = '*';
          }
        }
      } else {
        mCurrentIndex++;
        if (mCurrentIndex > length - 1) {
          mCurrentIndex = length - 1;
        }
        tempChar = mEditStr.toCharArray();
        for (int i = 0; i < mEditStr.length(); i++) {
          tempChar[i] = '*';
        }
      }
    }
  }

  public CIViewType getCurrCIViewType() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrCIViewType:" + mCIViewType);
    return mCIViewType;
  }

  public void setCurrCIViewType(CIViewType type) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setCurrCIViewType:" + type);
    mCIViewType = type;
  }

  public void handlerMessage(int code){
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " handlerMessage code = " + code);

      if((code == 4 || code == 5 || code == 10 || code == 11)){
          if(this.isVisible()){
              this.dismiss();
          }
      }
  }

    @Override
    public void dismiss() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " dismiss cidialog");
        if(mCIState !=null){
            mCIState.setCIClose();
            mCIState.setCamUpgrade(0);
        }
        dialogIsShow = false;
        super.dismiss();
        insertOrRemoveDialog();
        boolean isPWDShow = MtkTvPWDDialog.getInstance().PWDShow() == 0;
        if (isPWDShow) {
          ComponentsManager.getInstance().showNavComponent(NavBasic.NAV_COMP_ID_PWD_DLG);
        }
    }
    @Override
    public void updateComponentStatus(int statusID, int value) {
        Log.d(TAG, " updateComponentStatus statusID "+statusID);
        if (mCIState != null && statusID == ComponentStatusListener.NAV_POWER_OFF) {
            Log.d(TAG, " updateComponentStatus statusID NAV_POWER_OFF");
            mCIState.setCamUpgrade(0);
        }
    }

    public void insertOrRemoveDialog() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "insertOrRemoveDialog() ");

        mCIState = CIStateChangedCallBack.getInstance(mContext);
        String messtext;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "insertOrRemoveDialog() mCIState.insertOrRemove is "
                + mCIState.insertOrRemove);
        if (mCIState.insertOrRemove == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_CARD_INSERT) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "insertOrRemoveDialog() CHANNEL_LIST_TYPE is "
                    + menuConfigManager.getDefault(MenuConfigManager.CHANNEL_LIST_TYPE));
            if(menuConfigManager.getDefault(MenuConfigManager.CHANNEL_LIST_TYPE) == 1){
                mCIState.insertOrRemove = -1;
                return ;
            }
            boolean isCLTypeShow = TVContent.getInstance(mContext).isConfigVisible(MenuConfigManager.CHANNEL_LIST_TYPE);
            String profileName = MtkTvConfig.getInstance().
                    getConfigString(MenuConfigManager.CHANNEL_LIST_SLOT);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "insertOrRemoveDialog, isCLTypeShow : " +isCLTypeShow + ",profileName>> "+profileName);
            if (isCLTypeShow && !TextUtils.isEmpty(profileName)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "insertOrRemoveDialog.CHANNEL_LIST_TYPE, isCLTypeShow : " +isCLTypeShow);
                messtext= mContext.getString(R.string.insert_Cam_Select_Canscan);
            }else{
                mCIState.insertOrRemove = -1;
                return ;
            }
        } else if (mCIState.insertOrRemove == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_CARD_REMOVE) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "insertOrRemoveDialog() CHANNEL_LIST_TYPE is "
                    + menuConfigManager.getDefault(MenuConfigManager.CHANNEL_LIST_TYPE));
            if(menuConfigManager.getDefault(MenuConfigManager.CHANNEL_LIST_TYPE) == 0){
                mCIState.insertOrRemove = -1;
              return ;
            }
            messtext= mContext.getString(R.string.uninsert_Cam_Select_broadcast);
        } else {
            return;
        }

        SimpleDialog insertOrRemoveDialog = (SimpleDialog) ComponentsManager
                .getInstance().getComponentById(NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
        insertOrRemoveDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        insertOrRemoveDialog.setContent(messtext);
        insertOrRemoveDialog.setConfirmText(R.string.menu_ok);
        insertOrRemoveDialog.setCancelText(R.string.menu_cancel);
        insertOrRemoveDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
        insertOrRemoveDialog.show();

        insertOrRemoveDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                int action = keyEvent.getAction();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "insertOrRemoveDialog back button");
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && action == KeyEvent.ACTION_DOWN) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"insertOrRemoveDialog dismiss~");
                    insertOrRemoveDialog.dismiss();
                    return true;
                }
                return false;
            }
        });

        insertOrRemoveDialog.setOnConfirmClickListener(new SimpleDialog.OnConfirmClickListener() {
            @Override
            public void onConfirmClick(int dialogId) {
                if(dialogId == NavBasic.NAV_COMP_ID_CI_DIALOG && mCIState != null){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "insertOrRemoveDialog yes button");
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"insertOrRemoveDialog CHANNEL_LIST_TYPE ");
                    if (mCIState.insertOrRemove == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_CARD_INSERT) {
                        menuConfigManager.setValue( MenuConfigManager.CHANNEL_LIST_TYPE, 1);
                    } else if (mCIState.insertOrRemove == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_CARD_REMOVE) {
                        menuConfigManager.setValue(MenuConfigManager.CHANNEL_LIST_TYPE, 0);
                    }
                    mCIState.insertOrRemove = -1;
                }
            }
        }, NavBasic.NAV_COMP_ID_CI_DIALOG);

        insertOrRemoveDialog.setOnCancelClickListener(new SimpleDialog.OnCancelClickListener() {
            @Override
            public void onCancelClick(int dialogId) {
                if(dialogId == NavBasic.NAV_COMP_ID_CI_DIALOG && mCIState != null){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "insertOrRemoveDialog no button");
                    mCIState.insertOrRemove = -1;
                }
            }
        },NavBasic.NAV_COMP_ID_CI_DIALOG);
    }

 /* public static interface AfterDealCIEnqListener {
    // input cam pin code is success(pin code is correct)
    public void enqPinSuccess();

    // 1.input pin code is not correct,need input again(Video is scramble)
    // 2.first show input pincode fragment, wait input pincode(Video is scramble)
    public void enqPinWaitInputState(boolean isPincodeShow);
  }*/

  public boolean isDialogIsShow() {
    return dialogIsShow;
  }

  public void setDialogIsShow(boolean dialogIsShow) {
    this.dialogIsShow = dialogIsShow;
  }

	private AccessibilityDelegate mAccDelegateforCiList = new AccessibilityDelegate() {

        public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
            AccessibilityEvent event) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onRequestSendAccessibilityEvent." + host + "," + child + "," + event);
            do {

                if(!mCiMenuList.equals(host)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "host:" + mCiMenuList + "," + host);
                    break;
                }else{
                	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":host =" + false);

                }

                List<CharSequence> texts = event.getText();
                if(texts == null) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "texts :" + texts);
                    break;
                }

                //confirm which item is focus
                if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {//move focus
                    int index = findSelectItem(texts.get(0).toString());
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":index =" + index);
                    if(index >= 0) {
                    	mHandler.removeMessages(CHANNEL_LIST_SElECTED_FOR_TTS);
                    	Message msg = Message.obtain();
						msg.what = CHANNEL_LIST_SElECTED_FOR_TTS;
						msg.arg1 = index;
						mHandler.sendMessageDelayed(msg, 400);
                    }
                }

            } while(false);

			try {//host.onRequestSendAccessibilityEventInternal(child, event);
				Class clazz = Class.forName("android.view.ViewGroup");
				java.lang.reflect.Method getter =
					clazz.getDeclaredMethod("onRequestSendAccessibilityEventInternal",
						View.class, AccessibilityEvent.class);
				return (boolean)getter.invoke(host, child, event);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
        }

        private int findSelectItem(String text) {
        	 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "texts =" +text) ;
            if(mCIGroup == null) {
                return -1;
            }
            for(int i = 0; i < mCIGroup.length; i++) {
            	 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":index =" + mCIGroup[i] + " text = " +text);
                if(mCIGroup[i].equals(text)) {
                    return i;
                }
            }

            return -1;
        }
    };

    public boolean checkPvrAndTimeShiftStatus() {
        if(CommonIntegration.getInstance().isPvrRunning()){
              Toast.makeText(mContext, mContext.getResources()
                      .getString(R.string.pvr_running_warning_msg), Toast.LENGTH_LONG).show();
            return true;
        }else if(CommonIntegration.getInstance().isTimeShiftRunning(mContext)){
            Toast.makeText(mContext, mContext.getResources()
                    .getString(R.string.timeshift_running_warning_msg), Toast.LENGTH_LONG).show();
            return true;
        } else {
            return false;
        }
    }

}