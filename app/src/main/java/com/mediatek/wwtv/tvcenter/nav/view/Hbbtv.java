/**
 *
 */
package com.mediatek.wwtv.tvcenter.nav.view;

import android.content.Context;
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.Intent;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
import com.mediatek.wwtv.tvcenter.R;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.app.Activity;
import com.mediatek.wwtv.tvcenter.commonview.ConfirmDialog;
import com.mediatek.wwtv.tvcenter.commonview.ConfirmDialog.IResultCallback;
import com.mediatek.wwtv.tvcenter.dvr.controller.RegistOnDvrDialog;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;

import com.mediatek.wwtv.tvcenter.util.KeyMap;
//import com.mediatek.twoworlds.tv.MtkTvBroadcastBase;
//import com.mediatek.twoworlds.tv.model.MtkTvExternalUIStatusBase;
import com.mediatek.twoworlds.tv.MtkTvHBBTV;
//import com.mediatek.wwtv.tvcenter.nav.view.HbbtvAudioSubtitleDialog;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.util.ScreenStatusManager;
/**
 * @author MTK40707
 *
 */
public class Hbbtv extends NavBasicMisc implements ComponentStatusListener.ICStatusListener, IResultCallback{
    private static final String TAG = "Hbbtv";
    private static final int MTKTVAPI_HBBTV_FUNC_ACCEPT = 4;
    private static final int MTKTVAPI_HBBTV_FUNC_CANCEL = 5;
    private static final int FAILDIALOGDISS=1;
    private static final int INACTIVEHBBTV=2;
	private ProgressBar progressBar;
    SimpleDialog  faildialog=null;
	private boolean is_START_STREAMING = false;
    private int currentHbbtvState = 0;
    public Hbbtv(Context mContext) {
        super(mContext);
        this.componentID = NAV_NATIVE_COMP_ID_HBBTV;

        //Add component status Listener
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_COMPONENT_HIDE, this);
//        ComponentStatusListener.getInstance().addListener(
//                ComponentStatusListener.NAV_KEY_OCCUR, this);
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
        init(mContext);

    }
  public void init(Context mContext){
	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"init==progressbar");
	  ViewGroup layoutGroup = (ViewGroup)((Activity)mContext).findViewById(android.R.id.content).getRootView();
	  progressBar = new ProgressBar(mContext, null,android.R.attr.progressBarStyleLarge);
	  progressBar.setIndeterminate(true);
	
	  RelativeLayout.LayoutParams params = new 
			  RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
	  RelativeLayout rLayout = new RelativeLayout(mContext);
	  rLayout.setGravity(Gravity.CENTER);
	  rLayout.addView(progressBar);
	  layoutGroup.addView(rLayout,params);
	  hide();
	  //mMtkTvBroadcastBase = new MtkTvBroadcastBase();
  }
    @Override
    public boolean isVisible() {
        //there is no UI in android, always false
        return false;
    }

    @Override
    public boolean isCoExist(int componentID) {
        return false;
    }

    @Override
    public boolean isKeyHandler(int keyCode) {
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"hbbtv keycode=="+keyCode);
    	if(ComponentsManager.getNativeActiveCompId()==NAV_NATIVE_COMP_ID_HBBTV&&is_START_STREAMING){
            HbbtvAudioSubtitleDialog hbbtvAudioSubtitleDialog = new HbbtvAudioSubtitleDialog(mContext);
    	switch (keyCode) {
		case KeyMap.KEYCODE_MTKIR_MTSAUDIO:
		    hbbtvAudioSubtitleDialog.setHbbtvType(HbbtvAudioSubtitleDialog.KEY_AUDIO);
		    hbbtvAudioSubtitleDialog.show();
			return true;
		case KeyMap.KEYCODE_MTKIR_SUBTITLE:
		case KeyMap.KEYCODE_MTKIR_MTKIR_CC:
            hbbtvAudioSubtitleDialog.setHbbtvType(HbbtvAudioSubtitleDialog.KEY_SUBTITLE);
            hbbtvAudioSubtitleDialog.show();
			return true;
		default:
			break;
		}
    	}
        return false;
    }
      
    
    public void handlerHbbtvMessage(int type, int message){
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "handlerHbbtvMessage, type=" + type + ", message=" + message);
//        if(type != 1){
//            return ;
//        }

        switch(type){
            case 1://HBBTV_COMP_ACTIVE
            case 3://HBBTV_APP_RUUNING
                // case 5://HBBTV_START_STREAMING
                currentHbbtvState = type;
            	handler.removeMessages(INACTIVEHBBTV);
                ComponentsManager.updateActiveCompId(true, NAV_NATIVE_COMP_ID_HBBTV);
                ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_COMPONENT_SHOW, 0);
                break;
//            case 2://HBBTV_COMP_INACTIVE
            case 4://HBBTV_APP_TERMINATED
                // case 6://HBBTV_STOP_STREAMING
                 currentHbbtvState = type;
            	 handler.sendEmptyMessageDelayed(INACTIVEHBBTV, 1000);
            	
                break;
            case 8://HBBTV_COMP_REQUEST_RUNNING
                ConfirmDialog dialog = new ConfirmDialog(mContext,
                        mContext.getString(R.string.hbbtv_show_confict_dialog_tips));
                dialog.setTimeout(NAV_TIMEOUT_5);
                dialog.setCallback(this);
                dialog.showDialog();
                break;
            case 259:
            	showLoading();
            	break;
            case 260:
            	hide();
            	break;
            //case 3://download_fail
            //    break;
            case 5:
            	is_START_STREAMING = true;
            ScreenStatusManager.getInstance().setScreenOn(((TurnkeyUiMainActivity)mContext).getWindow(),ScreenStatusManager.SCREEN_ON_HBBTV);
            	break;
            case 6:
            	is_START_STREAMING = false;
            ScreenStatusManager.getInstance().setScreenOff(((TurnkeyUiMainActivity)mContext).getWindow(),ScreenStatusManager.SCREEN_ON_HBBTV);
            	break;
            case 266:
            	showHBBTVDialog();
            	break;

            case 267:
            	handler.removeMessages(FAILDIALOGDISS);
            	handler.sendEmptyMessage(FAILDIALOGDISS);
            	break;
        	case 10000:
                 currentHbbtvState = type;
                 handler.sendEmptyMessage(INACTIVEHBBTV);
                break;
            default:
                break;
        }
		
    }
    public Boolean getStreamBoolean(){
    	
    	return is_START_STREAMING;
    }
    @Override
    public void updateComponentStatus(int statusID, int value){
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus>>" + statusID + ">>>" + value);
    	if (statusID == ComponentStatusListener.NAV_CHANNEL_CHANGED) {
    		if(value != -1){
    			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateActiveCompId");
                //ComponentsManager.updateActiveCompId(true, 0);
                //ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_COMPONENT_HIDE, 0);
            }
    	}
//    	else if(statusID == ComponentStatusListener.NAV_KEY_OCCUR){
//            switch(value){
//            case KeyMap.KEYCODE_MTKIR_CHDN:
//            case KeyMap.KEYCODE_MTKIR_CHUP:
//            case KeyMap.KEYCODE_MTKIR_PRECH:
//                if(ComponentStatusListener.getParam1() != -1){
//                    ComponentsManager.updateActiveCompId(true, 0);
//                }
//                break;
//            }
//        }
        else if(statusID == ComponentStatusListener.NAV_COMPONENT_HIDE){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus||currentHbbtvState =" + currentHbbtvState);
            if (currentHbbtvState == 1 || currentHbbtvState == 3) {
                ComponentsManager.updateActiveCompId(true, NAV_NATIVE_COMP_ID_HBBTV);
                ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_COMPONENT_SHOW, 0);
            }
            if(!ComponentsManager.getInstance().isComponentsShow()){
                ComponentsManager.nativeComponentReActive();
            }
        }
    }

    @Override
    public void handleUserSelection(int result){
        MtkTvHBBTV mtkTvHBBTV = MtkTvHBBTV.getInstance();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleUserSelection, result==" + result);
        if(result == ConfirmDialog.BTN_YES_CLICK){
            mtkTvHBBTV.exchangeData(MTKTVAPI_HBBTV_FUNC_ACCEPT, null);
        } else {
            mtkTvHBBTV.exchangeData(MTKTVAPI_HBBTV_FUNC_CANCEL, null);
        }
    }

	 public void hide(){
		 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"hide==progressbar");
    	progressBar.setVisibility(View.INVISIBLE);
    }
    
    public void showLoading(){
    	 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"show==progressbar");
    	progressBar.setVisibility(View.VISIBLE);
    }
    
    public void showHBBTVDialog(){
        faildialog= (SimpleDialog) ComponentsManager
                .getInstance().getComponentById(
                        NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
        faildialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
        faildialog.setContent(R.string.hbbtv_failed_message);
        faildialog.setConfirmText(R.string.hbbtv_close_dialog);
        faildialog.setOnConfirmClickListener(new SimpleDialog.OnConfirmClickListener() {
            @Override
            public void onConfirmClick(int dialogId) {
                faildialog.dismiss();
                handler.removeMessages(FAILDIALOGDISS);
            }
        }, -1);
        faildialog.setOnCancelClickListener(new RegistOnDvrDialog()
                , -1);
		faildialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
		faildialog.show();
		handler.sendEmptyMessageDelayed(FAILDIALOGDISS, 10000);
    }
    
    Handler handler = new Handler(){
    	 @Override
         public void handleMessage(Message msg){
    		 switch (msg.what) {
			case FAILDIALOGDISS:
				if(faildialog!=null&&faildialog.isShowing()){
    				faildialog.dismiss();
                    }
				break;
			case INACTIVEHBBTV:
				  if(ComponentsManager.getNativeActiveCompId()==NAV_NATIVE_COMP_ID_HBBTV){
		                ComponentsManager.updateActiveCompId(true, 0);
		                ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_COMPONENT_HIDE, 0);
		                is_START_STREAMING = false;
	                }
				break;
			default:
				break;
			}
    	 }
    };
    
}
