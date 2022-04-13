
package com.mediatek.wwtv.tvcenter.nav.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.tv.TvTrackInfo;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeShiftManager;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.IntegrationZoom;
import com.mediatek.wwtv.tvcenter.nav.util.IntegrationZoom.ZoomListener;
import com.mediatek.wwtv.tvcenter.nav.util.SundryImplement;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicView;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;
import com.mediatek.wwtv.tvcenter.util.TextToSpeechUtil;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeshiftView;

public class SundryShowTextView extends NavBasicView implements
    ComponentStatusListener.ICStatusListener {
  private final static String TAG = "NavSundryShowTextView";

  public static final String SLEEP_TIMER = "SETUP_sleep_timer";
  private static final int MTS_AUDIO_CHANGE = 6;
  private TextView sundryTextView;
  private int lastPressKeyCode;
  private SundryImplement mNavSundryImplement;
  private TvCallbackHandler mTvCallbackHandler;
  private String[] /*sleepTimeStringArray,*/ mZoomMode;

  private int[] supportScreenModes;

  private int times = 0;
  private ZoomTipView mZoomTip;

  private boolean isSundryTime = false;
  private IntegrationZoom mIntegrationZoom;
  private ComponentsManager comManager;
  private CommonIntegration mIntegration;

  private SaveValue mSaveValue;
//  private boolean isSleep = false;
  private TextToSpeechUtil ttsUtil;

  // API
  private MtkTvTime mTime;

  private final Handler navSundryHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      // misc
      switch (msg.what) {
        case TvCallbackConst.MSG_CB_FEATURE_MSG:
           break;
        case TvCallbackConst.MSG_CB_SVCTX_NOTIFY:
          if (!mIntegration.isPipOrPopState()) {
            TvCallbackData data = (TvCallbackData) msg.obj;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                "come in TvCallbackConst.MSG_CB_SVCTX_NOTIFY =="
                    + data.param1);
            if ((5 == data.param1) || (10 == data.param1)) {
              if (mIntegrationZoom == null) {

                mIntegrationZoom = IntegrationZoom
                    .getInstance(null);

              }
              mIntegrationZoom.setZoomModeToNormalWithThread();
            }
          }
          break;
        case TvCallbackConst.MSG_CB_AV_MODE_MSG:
          TvCallbackData dataAvMode = (TvCallbackData) msg.obj;
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in TvCallbackConst.MSG_CB_AV_MODE_MSG ==" + dataAvMode.param1);
          if (!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MODULES_WITH_TIF)) {
            if ((MTS_AUDIO_CHANGE == dataAvMode.param1) && mIsComponetShow
                && (KeyMap.KEYCODE_MTKIR_MTSAUDIO == lastPressKeyCode)) {
              sundryTextView.setText(mNavSundryImplement
                  .getCurrentAudioLang());
            }
          }
          break;
        default:
          break;
      }

    }
  };

  public SundryShowTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public SundryShowTextView(Context context) {
    super(context);
    init(context);
  }

  public SundryShowTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  private void init(Context context) {
    componentID = NAV_COMP_ID_SUNDRY;
    comManager = ComponentsManager.getInstance();
    mIntegration = CommonIntegration.getInstance();
    mSaveValue = SaveValue.getInstance(context);
    mZoomTip = ((ZoomTipView) comManager.getComponentById(NAV_COMP_ID_ZOOM_PAN));
    mIntegrationZoom = IntegrationZoom.getInstance(mContext);
    mIntegrationZoom.setZoomListener(mZoomListener);
    mNavSundryImplement = SundryImplement
        .getInstanceNavSundryImplement(context);
    mTvCallbackHandler = TvCallbackHandler.getInstance();

    mTvCallbackHandler.addCallBackListener(
        TvCallbackConst.MSG_CB_FEATURE_MSG, navSundryHandler);
    mTvCallbackHandler.addCallBackListener(
        TvCallbackConst.MSG_CB_AV_MODE_MSG, navSundryHandler);
    mTvCallbackHandler.addCallBackListener(
        TvCallbackConst.MSG_CB_SVCTX_NOTIFY, navSundryHandler);

//    screenModeStringArray = getResources().getStringArray(
//        R.array.screen_mode_array_us);
//    soundEffectStringArray = getResources().getStringArray(
//        R.array.menu_audio_equalizer_array_us);

//    sleepTimeStringArray = getResources().getStringArray(
//        R.array.nav_sleep_strings);

    mZoomMode = getResources().getStringArray(R.array.nav_zoom_mode);
    initSundryView();
    mTime = MtkTvTime.getInstance();
    // add listener
    ComponentStatusListener.getInstance().addListener(
        ComponentStatusListener.NAV_ENTER_STANDBY, this);
    ComponentStatusListener.getInstance().addListener(
        ComponentStatusListener.NAV_PAUSE, this);
    ComponentStatusListener.getInstance().addListener(
        ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
    ComponentStatusListener.getInstance().addListener(
            ComponentStatusListener.NAV_LANGUAGE_CHANGED, this);
    ttsUtil =  new TextToSpeechUtil(context);
  }

  @Override
  public void setVisibility(int visibility) {
    if (View.VISIBLE == visibility) {
      startTimeout(NAV_TIMEOUT_5);
    }
//    else {
//      isSleep = false;
//    }

    ((TurnkeyUiMainActivity) mContext).getSundryLayout().setVisibility(
        visibility);

    super.setVisibility(visibility);
  }

  private final ZoomListener mZoomListener = new ZoomListener() {

    @Override
    public void zoomShow(int value) {
      mZoomMode = getResources().getStringArray(R.array.nav_zoom_mode);
      sundryTextView.setText(mZoomMode[value]);
      startTimeout(NAV_TIMEOUT_5);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "zoomShow value = " + value + "mZoomTip" + mZoomTip);
      if (mZoomTip == null) {
        mZoomTip = ((ZoomTipView) comManager.getComponentById(NAV_COMP_ID_ZOOM_PAN));

      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "zoomShow value = " + value + "mZoomTip" + mZoomTip);
      if (mZoomTip != null) {
        if (value == IntegrationZoom.ZOOM_2) {
          comManager.showNavComponent(NAV_COMP_ID_ZOOM_PAN);
        } else {
          mZoomTip.setVisibility(View.GONE);
        }
      }
    }

  };

  @Override
  public boolean isKeyHandler(int keyCode){
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isKeyHandler");
    // TODO Auto-generated method stub
    lastPressKeyCode = keyCode;
    isSundryTime = false;
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "keyCode:" + keyCode);
    boolean isFvp = ComponentsManager.getNativeActiveCompId()==NAV_NATIVE_COMP_ID_FVP;
    switch (keyCode) {
//      case KeyMap.KEYCODE_MTKIR_ASPECT:
      case KeyMap.KEYCODE_MTKIR_ZOOM:
        if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU
            && ComponentsManager.getActiveCompId() == NavBasic.NAV_COMP_ID_TELETEXT) {
          return false;
        }
        if (mNavSundryImplement == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isKeyHandler mNavSundryImplement == null!!!");
            return false;
        }
        supportScreenModes = mNavSundryImplement.getSupportScreenModes();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "supportScreenModes:" + supportScreenModes);
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "ComponentsManager.getActiveCompId():" + ComponentsManager.getActiveCompId());
        if ((null != supportScreenModes
            && !mNavSundryImplement.getInternalScrnMode()) && !mNavSundryImplement.isGingaWindowResize()
             &&ComponentsManager.getNativeActiveCompId()!=NAV_NATIVE_COMP_ID_BML) {
          if (!mIsComponetShow) {
//            if ((IntegrationZoom.ZOOM_1 != mIntegrationZoom
//                .getCurrentZoom())
//                && !mIntegration.isPipOrPopState()) {
//              if ((null != mZoomTip)
//                  && (View.VISIBLE == mZoomTip.getVisibility())) {
//                mZoomTip.setVisibility(View.GONE);
//              }
//
//              if (ComponentsManager.getNativeActiveCompId() != NAV_NATIVE_COMP_ID_GINGA) {
//                mIntegrationZoom.setZoomMode(IntegrationZoom.ZOOM_1);
//              }
//            }
            mContext.startActivity(new Intent("android.settings.SETTINGS").putExtra(
                    com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
                    com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE_DISPLAY_MODE_SRC));
            return false;
          }
        } else {
          sundryTextView.setText(R.string.nav_no_function);
        }
        return true;
      case KeyMap.KEYCODE_MTKIR_FREEZE:
//        if (StatePVR.getInstance() != null && StatePVR.getInstance().isRecording()) {
//          StatePVR.getInstance().dissmissBigCtrlBar();
//        }
//        if (StateFileList.getInstance() != null && StateFileList.getInstance().isShowing()) {
//          StateFileList.getInstance().getManager().restoreAllToNormal();
//        }
          if(isFvp){
              break;
          }
        int value = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.SETUP_SHIFTING_MODE);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "value:" + value);
        if (value != 0/* 0 close */) {
          // SETUP_SHIFTING_MODE default close when SETUP_SHIFTING_MODE is opened, freeze function
          // close.

          return false;
        }
        if (!mIsComponetShow) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isKeyHandler, FREEZE");
          handlerFreezeKey();
        }
        return true;
      case KeyMap.KEYCODE_MTKIR_MTSAUDIO:
    	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isKeyHandler mIsComponetShow = " + mIsComponetShow);
    	  if(isFvp){
    	      break;
    	    }
    	  if(ComponentsManager.getNativeActiveCompId()==NAV_NATIVE_COMP_ID_HBBTV){
    		  Hbbtv hbbtv = (Hbbtv) ComponentsManager.
                      getInstance().getComponentById(NavBasic.NAV_NATIVE_COMP_ID_HBBTV);
             if(hbbtv!=null&&hbbtv.getStreamBoolean()){
    		  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getNativeActiveCompId() = " + NAV_NATIVE_COMP_ID_HBBTV);
    		  return true;
             }
    	  }
        if(StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "StateDvrPlayback is runing, not handle");
            return false;
        }
        if(mNavSundryImplement.isFreeze()) {
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "is freeze");
            sundryTextView.setText(R.string.nav_no_function);
            return true;
        }
        if(SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)){
//        if(TifTimeShiftManager.getInstance().isPaused()||
//        		(TifTimeShiftManager.getInstance().isForwarding()&&!TifTimeShiftManager.getInstance().isNormalPlaying()) ||
//        		TifTimeShiftManager.getInstance().isRewinding()){
            TifTimeshiftView mTifTimeshiftView = (TifTimeshiftView)ComponentsManager.getInstance()
                .getComponentById(NavBasicView.NAV_COMP_ID_TIFTIMESHIFT_VIEW);
            if(mTifTimeshiftView!=null&&mTifTimeshiftView.isVisible()){
                mTifTimeshiftView.setVisibility(View.GONE);
            }
        	if(TifTimeShiftManager.getInstance().getTimeshiftSpeed()!=1){
             sundryTextView.setText(R.string.nav_no_function);
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timeshift is run"+TifTimeShiftManager.getInstance().isPaused()+",,,"+
            		 TifTimeShiftManager.getInstance().isForwarding() +",,,"
            		 +TifTimeShiftManager.getInstance().isRewinding());
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timeshift is run");
             return true;
        }
            }
        if (!mIsComponetShow) {
          String currentAudioLang = mNavSundryImplement.getCurrentAudioLang();
          if (TextUtils.isEmpty(currentAudioLang)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isKeyHandler 1");
            sundryTextView.setText(R.string.nav_no_function);
          } else {
              if (CommonIntegration.getInstance().isCurrentSourceBlockEx()) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "source block !");
                  sundryTextView.setText(R.string.nav_no_function);
            }else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isKeyHandler 2, text = " + currentAudioLang);

                    sundryTextView.setText(currentAudioLang);
            }
          }
          if(ttsUtil!=null){
              ttsUtil.speak(sundryTextView.getText().toString());
          }
        }
        if (StateDvrFileList.getInstance() != null
                && StateDvrFileList.getInstance().isShowing()) {
            DvrManager.getInstance().restoreToDefault(StateDvrFileList.getInstance());
        }
        return true;
      case KeyMap.KEYCODE_MTKIR_SLEEP:
        return true;
      case KeyMap.KEYCODE_MTKIR_PEFFECT:
          mContext.startActivity(new Intent("android.settings.SETTINGS").putExtra(
                  com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
                  com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE_PICTURE_STYLE_SRC));
          return false;
      case KeyMap.KEYCODE_MTKIR_SEFFECT:
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isKeyHandler KEYCODE_MTKIR_SEFFECT");
          if (!mIsComponetShow) {
            if(mNavSundryImplement.isHeadphoneSetOn()||mNavSundryImplement.isOnePlusOpened()) {
              sundryTextView.setText(R.string.nav_no_function);
              return true;
            } else {
              mContext.startActivity(new Intent("android.settings.SETTINGS").putExtra(
                        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
                        com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_SOUND_STYLE_SRC));
              return false;
            }
          }
          break;
//      case KeyMap.KEYCODE_MTKIR_ZOOM:
//        // banner view hide .
//        BannerView bannerView = ((BannerView) ComponentsManager.getInstance()
//            .getComponentById(NavBasicMisc.NAV_COMP_ID_BANNER));
//        if (bannerView.isShown()) {
//          bannerView.setVisibility(View.GONE);
//        }
//        // ginga loading dialog hide
//        InfoBarDialog info = (InfoBarDialog) ComponentsManager.
//                getInstance().getComponentById(NavBasic.NAV_COMP_ID_INFO_BAR);
//        if (info != null) {
//            info.handlerMessage(4);
//        }
//        // dvr ui / old time shift ui hide .
//        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_DVR)) {
//            DvrManager.getInstance().uiManager.dissmiss();
//        } else {
////          TimeShiftManager.getInstance().uiManager.dissmiss();
//        }
//        return mIntegrationZoom.showCurrentZoom();
      default:
        break;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isKeyHandler false");
    return false;
  }

  @Override
  public boolean isCoExist(int componentID) {
    // TODO Auto-generated method stub
    switch (componentID) {
      case NAV_COMP_ID_CEC:
        return KeyMap.KEYCODE_MTKIR_FREEZE == lastPressKeyCode;
      case NAV_COMP_ID_ZOOM_PAN:
      case NAV_COMP_ID_BANNER:
      case NAV_COMP_ID_POP:
      case NAV_COMP_ID_PVR_TIMESHIFT:
        return true;
        default:
            break;
    }
    return false;
  }

  @Override
  public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
    // TODO Auto-generated method stub
    boolean isHandler = true;
    boolean changeValueFlag;
    isSundryTime = false;
    if (mNavSundryImplement == null) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onKeyHandler mNavSundryImplement == null!!!");
        return false;
    }
    if (mIsComponetShow && (lastPressKeyCode == keyCode)) {
      changeValueFlag = true;
    } else {
      changeValueFlag = false;
    }
    boolean isFvp = ComponentsManager.getNativeActiveCompId()==NAV_NATIVE_COMP_ID_FVP;
    switch (keyCode) {
//      case KeyMap.KEYCODE_MTKIR_ASPECT:
      case KeyMap.KEYCODE_MTKIR_ZOOM:
//        isSleep = false;
        if (1 == mSaveValue.readValue(MenuConfigManager.MODE_LIST_STYLE)) {
          isHandler = false;
          setVisibility(View.GONE);
        } else {
          com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "ComponentsManager.getNativeActiveCompId():"
              + ComponentsManager.getNativeActiveCompId());
          supportScreenModes = mNavSundryImplement.getSupportScreenModes();
          if ((null != supportScreenModes&&!mNavSundryImplement.getInternalScrnMode()) && !mNavSundryImplement.isGingaWindowResize()
            &&ComponentsManager.getNativeActiveCompId()!=NAV_NATIVE_COMP_ID_BML) {
              mIntegrationZoom.setZoomModeToNormalWithThread();
              mContext.startActivity(new Intent("android.settings.SETTINGS").putExtra(
                      com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
                      com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE_DISPLAY_MODE_SRC));
          } else {
            sundryTextView.setText(R.string.nav_no_function);
          }
        }
        break;
      case KeyMap.KEYCODE_MTKIR_FREEZE:
//        isSleep = false;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler, KEYCODE_MTKIR_FREEZE");
        if(isFvp){
            break;
        }
        int value = MtkTvConfig.getInstance().getConfigValue(
            MtkTvConfigType.CFG_RECORD_REC_TSHIFT_MODE);
        if (value != 0) {
          isHandler = false;
          setVisibility(View.GONE);
          return false;
        }
        handlerFreezeKey();
        break;
      case KeyMap.KEYCODE_MTKIR_MTSAUDIO:
//        isSleep = false;
        if(isFvp){
            break;
        }
        if(StateDvrPlayback.getInstance() != null && StateDvrPlayback.getInstance().isRunning()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "StateDvrPlayback is runing, not handle");
            return false;
        }
        if(ComponentsManager.getNativeActiveCompId()==NAV_NATIVE_COMP_ID_HBBTV){
  		  Hbbtv hbbtv = (Hbbtv) ComponentsManager.
                    getInstance().getComponentById(NavBasic.NAV_NATIVE_COMP_ID_HBBTV);
           if(hbbtv!=null&&hbbtv.getStreamBoolean()){
  		  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getNativeActiveCompId() = " + NAV_NATIVE_COMP_ID_HBBTV);
  		  hbbtv.isKeyHandler(KeyMap.KEYCODE_MTKIR_MTSAUDIO);
  		  return true;
           }
  	  }
        if(mNavSundryImplement.isFreeze()) {
            sundryTextView.setText(R.string.nav_no_function);
            break;
        }
        if(SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)){
//        if(TifTimeShiftManager.getInstance().isPaused()||
//        		(TifTimeShiftManager.getInstance().isForwarding()&&!TifTimeShiftManager.getInstance().isNormalPlaying()) ||
//        		TifTimeShiftManager.getInstance().isRewinding()){
            TifTimeshiftView mTifTimeshiftView = (TifTimeshiftView)ComponentsManager.getInstance()
                .getComponentById(NavBasicView.NAV_COMP_ID_TIFTIMESHIFT_VIEW);
            if(mTifTimeshiftView!=null&&mTifTimeshiftView.isVisible()){
                mTifTimeshiftView.setVisibility(View.GONE);
            }
        	if(TifTimeShiftManager.getInstance().getTimeshiftSpeed()!=1){
         sundryTextView.setText(R.string.nav_no_function);
            break;
       }
            }
        if (CommonIntegration.getInstance().isCurrentSourceBlockEx()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "source block !!");
            sundryTextView.setText(R.string.nav_no_function);
            break;
        }
        if(event.getRepeatCount() > 0){//filter long press
            break;
        }
        if (changeValueFlag) {
          mNavSundryImplement.setNextAudioLang();
        } else {
          String currentAudioLang = mNavSundryImplement.getCurrentAudioLang();
          if (TextUtils.isEmpty(currentAudioLang)) {
            sundryTextView.setText(R.string.nav_no_function);
          } else {
            sundryTextView.setText(currentAudioLang);
          }
        }
        if (StateDvrFileList.getInstance() != null
                && StateDvrFileList.getInstance().isShowing()) {
            DvrManager.getInstance().restoreToDefault(StateDvrFileList.getInstance());
        }
        break;
      case KeyMap.KEYCODE_MTKIR_SLEEP:
        break;
      case KeyMap.KEYCODE_MTKIR_PEFFECT:
        mContext.startActivity(new Intent("android.settings.SETTINGS").putExtra(
                  com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
                  com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE_PICTURE_STYLE_SRC));
        break;
      case KeyMap.KEYCODE_MTKIR_SEFFECT:
//        isSleep = false;
        if (1 == mSaveValue.readValue(MenuConfigManager.MODE_LIST_STYLE)) {
          isHandler = false;
          setVisibility(View.GONE);
        } else if(mNavSundryImplement.isHeadphoneSetOn()||mNavSundryImplement.isOnePlusOpened()) {
          sundryTextView.setText(R.string.nav_no_function);
        } else {
          mContext.startActivity(new Intent("android.settings.SETTINGS").putExtra(
                    com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
                    com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_SOUND_STYLE_SRC));
        }
        break;
//        return true;
//      case KeyMap.KEYCODE_MTKIR_ZOOM:
//        isSleep = false;
//        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " keyhandler KEYCODE_MTKIR_ZOOM changeValueFlag =" + changeValueFlag);
//        if (changeValueFlag) {
//          isHandler = mIntegrationZoom.nextZoom();
//        } else {
//          boolean isFreeze = mNavSundryImplement.isFreeze();
//          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "keyhandler KEYCODE_MTKIR_ZOOM: isFreeze:" + isFreeze);
//          if (isFreeze) {
//
//            int setValue = mNavSundryImplement.setFreeze(false);
//          }
//          isHandler = mIntegrationZoom.showCurrentZoom();
//
//        }
//
//        // mCurIndex = (++mCurIndex)%3;
//        // sundryTextView.setText(mZoomMode[mCurIndex]);
//        // if(mCurIndex == 1){
//        // mZoomTip.setVisibility(View.VISIBLE);
//        // }
//        // startTimeout(NavBasic.NAV_TIMEOUT_5);
//        break;
      case KeyMap.KEYCODE_BACK:
      case KeyMap.KEYCODE_MTKIR_RECORD:
      case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
      case KeyMap.KEYCODE_MTKIR_REWIND:
      case KeyMap.KEYCODE_MTKIR_FASTFORWARD:
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "KeyMap.KEYCODE_BACK>>");
        setVisibility(View.GONE);
        // gone zoom tip

        if (mZoomTip != null) {
          mZoomTip.setVisibility(View.GONE);
        }
        if(keyCode==KeyMap.KEYCODE_BACK){
            isHandler = true;
        }else{
            isHandler = false;
        }
        break;
      case KeyMap.KEYCODE_MTKIR_MTKIR_CC:
//        isSleep = false;
        if (MarketRegionInfo.REGION_EU != MarketRegionInfo.getCurrentMarketRegion()) {
          if (mNavSundryImplement.isFreeze()) {// freeze off
            handlerFreezeKey();
          }
          isHandler = false;
        } else {
          return true;
        }
        break;
      case KeyMap.KEYCODE_MTKIR_SUBTITLE:
//        isSleep = false;
        if (MarketRegionInfo.REGION_EU == MarketRegionInfo.getCurrentMarketRegion()) {
          if (mNavSundryImplement.isFreeze()) {// freeze off
            handlerFreezeKey();
          }
          isHandler = false;
        } else {
          return true;
        }
        break;
      default:
        isHandler = false;
        break;
    }

    if (isHandler) {
      lastPressKeyCode = keyCode;
      startTimeout(NAV_TIMEOUT_5);
    }
    Log.d(TAG, "return isHandler val :" + isHandler);
    return isHandler;
  }

  // handler freeze key
  private void handlerFreezeKey() {
    if (mZoomTip != null) { // fix DTV00582185
      mZoomTip.setVisibility(View.GONE);
    }
    if (CommonIntegration.getInstance().is3rdTVSource()) {
        sundryTextView.setText(R.string.nav_no_function);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerFreezeKey(): is3rdTVSource:true" );
      return;
    }
    boolean isFreeze = mNavSundryImplement.isFreeze();
    isFreeze = !isFreeze;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerFreezeKey(): isFreeze:" + isFreeze);
    int setValue = mNavSundryImplement.setFreeze(isFreeze);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handlerFreezeKey, isFreeze:" + isFreeze + ", setValue:" + setValue);
    // indicate no function
    if (setValue == -2) {
      sundryTextView.setText(R.string.nav_no_function);
      return;
    }

    String[] freezeModeName = mContext.getResources().getStringArray(
        R.array.nav_freeze_strings);
    if (isFreeze) {
      sundryTextView.setText(freezeModeName[0]);
    } else {
      sundryTextView.setText(freezeModeName[1]);
    }
  }

  // change sleep
  public void adjustTime(String[] time) {
    int minutes = times / 60;

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "times=" + times + ", minutes=" + minutes);
//    sundryTextView.setText("Sleep:" + (minutes + 1) + " Minutes");
    sundryTextView.setText(mContext.getString(R.string.nav_sleep_min, (minutes+1)));
  }

  private void initSundryView() {
    LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.MATCH_PARENT);
    addView(inflate(TurnkeyUiMainActivity.getInstance(), R.layout.nav_sundry_view, null), layoutParams);
    sundryTextView = (TextView) findViewById(R.id.nav_sundry_textview_id);
    sundryTextView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
  }

  // gain the index of the current value whit the current type
  public int getCurrentValueIndex(int[] currentArray, int value) {
    if (currentArray != null) {
      for (int i = 0; i < currentArray.length; i++) {
        if (value == currentArray[i]) {
          return i;
        }
      }
    }

    return 0;
  }

  @Override
  public boolean deinitView() {
    mTvCallbackHandler.removeCallBackListener(
        TvCallbackConst.MSG_CB_FEATURE_MSG, navSundryHandler);
    mTvCallbackHandler.removeCallBackListener(
        TvCallbackConst.MSG_CB_AV_MODE_MSG, navSundryHandler);
    mTvCallbackHandler.removeCallBackListener(
        TvCallbackConst.MSG_CB_SVCTX_NOTIFY, navSundryHandler);
    mIntegrationZoom.setZoomListener(null);
    return true;
  }

  Runnable updateTime = new Runnable() {

    @Override
    public void run() {
      // TODO Auto-generated method stub
      if (isSundryTime && mIsComponetShow) {
        sundryTextView.setText(mNavSundryImplement.getCurrentTime());
        navSundryHandler.postDelayed(this, NAV_TIMEOUT_1);
      }
    }
  };

  @Override
  public void updateComponentStatus(int statusID, int value) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus statusID =" + statusID);

    if (statusID == ComponentStatusListener.NAV_ENTER_STANDBY) {
      if (mIntegrationZoom == null) {

        mIntegrationZoom = IntegrationZoom.getInstance(null);

      }
      mIntegrationZoom.setZoomModeToNormalWithThread();

      times = mTime.getSleepTimerRemainingTime();
      if (times > 0) {
        // set sleep off
        int time = 0;
        while (true) {
          time = mTime.getSleepTimer();
          if (time == 0) {
            break;
          }
        }
      }
    } else if (statusID == ComponentStatusListener.NAV_PAUSE) {
      new Thread(new Runnable() {

        @Override
        public void run() {
          if (mNavSundryImplement.isFreeze()) {// freeze off
            mNavSundryImplement.setFreeze(false);
          }
          if (mIntegrationZoom == null) {
            mIntegrationZoom = IntegrationZoom.getInstance(null);
          }
          if (!mIntegration.isPOPState()
              && mIntegrationZoom.screenModeZoomShow()
              && IntegrationZoom.ZOOM_1 != mIntegrationZoom
                  .getCurrentZoom()) {
            if ((null != mZoomTip)
                && (View.VISIBLE == mZoomTip.getVisibility())) {
              ((Activity) mContext).runOnUiThread(new Runnable() {

                @Override
                public void run() {
                  mZoomTip.setVisibility(View.GONE);
                }
              });
            }
            mIntegrationZoom.setZoomMode(IntegrationZoom.ZOOM_1);
          }
        }
      }).start();
    } else if (statusID == ComponentStatusListener.NAV_CHANNEL_CHANGED) {
      new Thread(new Runnable() {

        @Override
        public void run() {
          if ((IntegrationZoom.ZOOM_1 != mIntegrationZoom.getCurrentZoom())
              && mIntegrationZoom.screenModeZoomShow()) {
            //mIntegrationZoom.setZoomMode(IntegrationZoom.ZOOM_1);
            if (mZoomTip != null && mContext != null) {
              ((Activity) mContext).runOnUiThread(new Runnable() {

                @Override
                public void run() {
                  mZoomTip.setVisibility(View.GONE);
                }
              });
            }
          }
          if (mContext != null && getVisibility() == View.VISIBLE) {
            ((Activity) mContext).runOnUiThread(new Runnable() {

              @Override
              public void run() {
                setVisibility(View.GONE);
              }
            });
          }
        }
      }).start();
    }else if (statusID == ComponentStatusListener.NAV_LANGUAGE_CHANGED){
      com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"update context");
      mNavSundryImplement.setInstanceNavSundryImplementNull();
      init(TurnkeyUiMainActivity.getInstance());
    }
  }

  public void updateTrackChanged(int trackType, String trackId) {
    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MODULES_WITH_TIF)) {
      if ((TvTrackInfo.TYPE_AUDIO == trackType) && mIsComponetShow
          && (KeyMap.KEYCODE_MTKIR_MTSAUDIO == lastPressKeyCode)) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in updateTrackChanged, to update current AUDIO_TRACK");
        if (CommonIntegration.getInstance().isCurrentSourceBlockEx()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "source block !!");
            sundryTextView.setText(R.string.nav_no_function);
        }else {
             sundryTextView.setText(mNavSundryImplement
                     .getCurrentAudioLang());
        }

        if(ttsUtil != null){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"tts is on=== ");
            sundryTextView.setFocusable(true);
            sundryTextView.requestFocus();
            ttsUtil.speak(sundryTextView.getText().toString());
        }
      }
    }
  }
}
