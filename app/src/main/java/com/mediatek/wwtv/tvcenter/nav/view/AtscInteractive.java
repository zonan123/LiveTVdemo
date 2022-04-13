/**
 *
 */
package com.mediatek.wwtv.tvcenter.nav.view;

import android.content.Context;
import com.mediatek.wwtv.tvcenter.R;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.app.Activity;
import com.mediatek.wwtv.tvcenter.commonview.ConfirmDialog;
import com.mediatek.wwtv.tvcenter.commonview.ConfirmDialog.IResultCallback;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;

import com.mediatek.wwtv.tvcenter.util.KeyMap;
import android.widget.Toast;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.tv.ui.MyToast;
import android.os.Handler;

/**
 * @author sin_xuejiuliang
 *
 */
public class AtscInteractive extends NavBasicMisc implements ComponentStatusListener.ICStatusListener, IResultCallback{
    private static final String TAG = "ATSC3[inactive]";
    private boolean isenable = false;
    private boolean isflag = false;
    public AtscInteractive(Context mContext) {
        super(mContext);
        this.componentID = NAV_NATIVE_COMP_ID_ATSC3;
        //Add component status Listener
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_COMPONENT_HIDE, this);
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_CHANNEL_CHANGED, this);

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
        return false;
    }
      
    
    public void handlerATSC3Message(TvCallbackData data){
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "handlerATSC3Message, type=" + data.param1+ ", message=" + data.paramBool1 +", duration="+data.param2+", isAS3="
            +data.paramBool2+", data.param4="+data.param4);
        if(data.paramBool2){
            MyToast myToast = new MyToast(mContext,new Handler());
            isenable = data.paramBool1;
            if(isenable){
                isflag = false;
                ComponentsManager.getInstance().hideAllComponents();
                myToast.show(mContext.getString(R.string.atsc_start_on_screen,data.param2/60),10000);
                //Toast.makeText(mContext,mContext.getString(R.string.atsc_start_on_screen,data.param2),Toast.LENGTH_LONG).show();
            }
            if(!isenable&&!isflag){
                //Toast.makeText(mContext,mContext.getString(R.string.atsc_end_on_screen),Toast.LENGTH_LONG).show();
                myToast.show(mContext.getString(R.string.atsc_end_on_screen),10000);
            }
        }else{
        switch(data.param4){
            case 0://startinteractive
                ComponentsManager.updateActiveCompId(true, NAV_NATIVE_COMP_ID_ATSC3);
                ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_COMPONENT_SHOW, 0);
                break;
            case 1://stop interactive
                if(ComponentsManager.getNativeActiveCompId()==NAV_NATIVE_COMP_ID_ATSC3){
                ComponentsManager.updateActiveCompId(true, 0);
                ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_COMPONENT_HIDE, 0);
                } 
                break;
            default:
                break;
        }

            }
       
    }
 
    @Override
    public void updateComponentStatus(int statusID, int value){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus>>" + statusID + ">>>" + value);
        if (statusID == ComponentStatusListener.NAV_CHANNEL_CHANGED) {
        if(value != -1&&isenable){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateActiveCompId  isenable");
            isflag = true;
        }
        }

        else if(statusID == ComponentStatusListener.NAV_COMPONENT_HIDE){
            if(!ComponentsManager.getInstance().isComponentsShow()){
                ComponentsManager.nativeComponentReActive();
            }
        }
    }

    @Override
    public void handleUserSelection(int result){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleUserSelection, result==" + result);
    }


    
}

