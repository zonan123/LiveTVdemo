package com.mediatek.wwtv.tvcenter.nav.view;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;

import com.mediatek.twoworlds.tv.MtkTvUtil;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyReceiver;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.FocusLabelControl;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.util.MultiViewControl;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.twoworlds.tv.MtkTvEASBase;
import com.mediatek.twoworlds.tv.model.MtkTvEASParaBase;
import com.mediatek.wwtv.tvcenter.util.Constants;
import com.mediatek.wwtv.tvcenter.util.SaveValue;

public class FloatView extends NavBasicMisc implements
                ComponentStatusListener.ICStatusListener{

    private static String TAG = "FloatView";
    private static final String EAS_ACTION = "mtk.intent.EAS.message";
    private static final int STATUS_BLOCK_ALL = 0;
    private static final int STATUS_ALLOW_CHANGE_CHANNEL = 1;
    private static final int STATUS_RESTART_ALL = -1;
    private static int mEastype = 0;
    private boolean isEasPlaying;

    public FloatView(Context mContext) {
        super(mContext);
        this.mContext = mContext;
        this.componentID = NAV_COMP_ID_EAS;
        this.componentPriority = NAV_PRIORITY_HIGH_3;//priority is highest

        //Add enter lancher Listener
        ComponentStatusListener.getInstance().addListener(ComponentStatusListener.NAV_RESUME, this);
        ComponentStatusListener.getInstance().addListener(ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
    }

    @Override
    public boolean isVisible() {
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isVisible>>"+super.isVisible());
        return super.isVisible();
    }

    @Override
    public boolean isCoExist(int componentID) {
        return false;
    }

    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
         com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "keyCode:" + keyCode);
        boolean flag = true;
        switch (keyCode) {
            case KeyMap.KEYCODE_POWER:
                flag = false;
                break;
            case KeyMap.KEYCODE_VOLUME_DOWN:
            case KeyMap.KEYCODE_VOLUME_UP:
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "FloatView ----  KeyMap.KEYCODE_VOLUME_UP");
                flag = false;
                break;
            case KeyMap.KEYCODE_MTKIR_CHUP:
            case KeyMap.KEYCODE_MTKIR_CHDN:
            //case KeyMap.KEYCODE_MTKIR_PRECH:
                BannerView bannerView = (BannerView) ComponentsManager.
                    getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
                if (bannerView != null) {
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"channel change key received, disable banner until next channel changed.");
                  bannerView.controlBannerByEASMessage(true);//disable banner show
                }
                if(mEastype == 1){
                    MtkTvEASBase eas = new MtkTvEASBase();
                    eas.EASSetAndroidExitEASNoChangeChannelStatus(true);
                    eas.EASSetAndroidLaunchStatus(true);
                    flag = false;
                }
                break;
            default:
                break;
        }

        return flag;
    }

    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event) {
        return super.onKeyHandler(keyCode, event);
    }

    @Override
    public boolean initView() {
        return true;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setVisibility ---------visibility "+visibility);
        if(visibility == View.INVISIBLE || visibility == View.GONE){
            sendIRControl(STATUS_RESTART_ALL);
        }
    }

    public void handleEasMessage(int msgType, int data){
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "handleEasMessage, msgType=" + msgType +", data=" + data + ", mContext>> "+ mContext);
        InputSourceManager inputSourceManager = InputSourceManager.getInstance();
        if(msgType == 1){
            if(!TvSingletons.getSingletons().getTurnkeyUiMainActiviteActive()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TK stop, won't show EAS UI!");
                return;
            }
        }
        if(mContext==null){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mContext==null");
            return;
        }
        if(msgType == 1){
            if(inputSourceManager != null){
                inputSourceManager.disableCECOneTouchPlay(true);
            }
            isEasPlaying = true;
            SaveValue.writeWorldStringValue(mContext, Constants.MTK_3RD_APP_FLAG, (isEasPlaying ?"1":"0"), false);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "writeWorldStringValue isEasPlaying="+isEasPlaying);
            if(!isVisible()){
                TurnkeyUiMainActivity.resumeTurnkeyActivity(mContext);
            	if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT)){
            		if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)){
            			MultiViewControl multiViewControl=((MultiViewControl) ComponentsManager.getInstance()
        						.getComponentById(NAV_COMP_ID_POP));
            			if(multiViewControl!=null){
            				multiViewControl.setVisibility(View.INVISIBLE);
                         }
            		}
            	}else{
            		FocusLabelControl focusLabelControl=((FocusLabelControl) ComponentsManager.getInstance()
    						.getComponentById(NAV_COMP_ID_POP));
        			if(focusLabelControl!=null){
        				focusLabelControl.setVisibility(View.INVISIBLE);
                     }
            	}
                  ComponentsManager.getInstance().showNavComponent(NAV_COMP_ID_EAS);
            }

			this.mEastype = data;
            sendIRControl(mEastype);
		} else if (msgType == 0) {
            isEasPlaying = false;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isEasPlaying="+isEasPlaying);
            SaveValue.writeWorldStringValue(mContext, Constants.MTK_3RD_APP_FLAG, (isEasPlaying ?"1":"0"), false);
            setVisibility(View.INVISIBLE);
            this.mEastype = 0;
            if(inputSourceManager != null){
                inputSourceManager.disableCECOneTouchPlay(false);
            }
        }
        
        TVAsyncExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
            	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Send broadcast for mtk.intent.EAS.message.permission");
                Intent intent = new Intent();
                intent.putExtra("EASPLAYFLAG",isEasPlaying);
                intent.setAction(EAS_ACTION);
                mContext.sendBroadcast(intent,"mtk.intent.EAS.message.permission");
            }
        });   	

    }

    public boolean isEasPlaying() {
        return isEasPlaying;
    }

    @Override
    public void updateComponentStatus(int statusID, int value){
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG," updateComponentStatus, statusID= " + statusID + ", value>>"+value);

        if(!TvSingletons.getSingletons().getTurnkeyUiMainActiviteActive()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TK stop, do nothing!");
            return;
        }
        if(statusID == ComponentStatusListener.NAV_RESUME){
            MtkTvEASParaBase info = new MtkTvEASParaBase();
            MtkTvEASBase eas = new MtkTvEASBase();
            eas.getEASCurrentStatus(info);
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,
					"1 updateComponentStatus, EASType=" + info.getEASType()
							+ ", ChannelChange=" + info.getChannelChange());

			handleEasMessage(info.getEASType(), info.getChannelChange());
        } else if (statusID == ComponentStatusListener.NAV_CHANNEL_CHANGED) {
          BannerView bannerView = (BannerView) ComponentsManager.
              getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
          if (bannerView != null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"updateComponentStatus enable banner~ statusId:"+statusID + ",value:"+value);
            bannerView.controlBannerByEASMessage(false);//enable banner show
          }
			if (value != -1) {
			    MtkTvEASBase eas = new MtkTvEASBase();
			    if (eas.EASGetAndroidLaunchStatus()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EASSetAndroidLaunchStatus(false)");
                    eas.EASSetAndroidLaunchStatus(false);
                }
				MtkTvEASParaBase info = new MtkTvEASParaBase();
				eas.getEASCurrentStatus(info);
				com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "updateComponentStatus, EASType=" + info.getEASType() + ", ChannelChange="
								+ info.getChannelChange());
                handleEasMessage(info.getEASType(), info.getChannelChange());
            }
        }
    }

    /**
     *   [MTK Internal] This API is for MTK control IR use only.
     *   IR Remote related features
     *   @value      case 0: Ignore All IR Key except KEY_UP and KEY_DOWN
     *               case 1: Ignore All IR Key
     *               case 2: Ignore All IR except 3 volume Key and power key
     *               case 3: Restart IR Key
     *               case 6: Ignore All IR Key except 3 volume Key and power key and Ch +/- key --for EAS respond to ch+/- key event
     *               mute/vol+/- never mute.
     **/
    private void sendIRControl (int easStatus){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sendIRControl||easStatus =: " + easStatus);
        switch (easStatus) {
            case STATUS_BLOCK_ALL:
                MtkTvUtil.IRRemoteControl(2);
                break;
            case STATUS_ALLOW_CHANGE_CHANNEL:
                MtkTvUtil.IRRemoteControl(6);
                break;
            case STATUS_RESTART_ALL:
                MtkTvUtil.IRRemoteControl(3);
                break;
            default:
                MtkTvUtil.IRRemoteControl(3);
                break;
        }
    }
}
