/**
 *
 */
package com.mediatek.wwtv.tvcenter.nav.view.common;

import java.lang.ref.WeakReference;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Window;


import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
/**
 * @author mtk40707
 *
 */
public class NavBasicDialog extends Dialog implements NavBasic, ComponentStatusListener.ICStatusListener {
    protected static String TAG = "NavBasicDialog";

    //id
    protected int componentID = 0;
    //priority
    protected int componentPriority = NAV_PRIORITY_DEFAULT;
    //context
    protected Context mContext = null;
    //status
    protected boolean mIsComponetShow = false;
    //handler
    private Handler mHandler = null;

    private boolean mTTSEnable;

    public NavBasicDialog(Context context, int theme){
        super(context, theme);

        mContext = context;
        mHandler = new InternalHandler(this, context.getMainLooper());
    }

    @Override
    public void show() {
        //update status
        mIsComponetShow = true;
        if((componentID & NAV_COMP_ID_MASK) != 0){
            ComponentsManager.updateActiveCompId(false, componentID);
            ComponentStatusListener.getInstance().updateStatus(
                ComponentStatusListener.NAV_COMPONENT_SHOW, this.componentID);
        }
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        //update status

        synchronized(this){
            mIsComponetShow = false;
            if(componentID == ComponentsManager.getActiveCompId()){
                ComponentsManager.updateActiveCompId(false, 0);
            }

            if((componentID & NAV_COMP_ID_MASK) != 0){
                ComponentStatusListener.getInstance().updateStatus(
                        ComponentStatusListener.NAV_COMPONENT_HIDE, this.componentID);
            }
        }

        stopTimeout();
    }

    protected void notifyNavHide() {
        synchronized(this){
            if(componentID == ComponentsManager.getActiveCompId()){
                ComponentsManager.updateActiveCompId(false, 0);
            }

            if((componentID & NAV_COMP_ID_MASK) != 0){
                ComponentStatusListener.getInstance().updateStatus(
                        ComponentStatusListener.NAV_COMPONENT_HIDE, this.componentID);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.mediatek.wwtv.tvcenter.nav.NavBasic#isVisible()
     */
    @Override
    public boolean isVisible() {
        // TODO Auto-generated method stub
        return mIsComponetShow;
    }

    /* (non-Javadoc)
     * @see com.mediatek.wwtv.tvcenter.nav.NavBasic#isKeyHandler(int)
     */
    @Override
    public boolean isKeyHandler(int keyCode) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.mediatek.wwtv.tvcenter.nav.NavBasic#getComponentID()
     */
    @Override
    public int getComponentID() {
        // TODO Auto-generated method stub
        return componentID;
    }

    /* (non-Javadoc)
     * @see com.mediatek.wwtv.tvcenter.nav.NavBasic#getPriority()
     */
    @Override
    public int getPriority() {
        return componentPriority;
    }

    /* (non-Javadoc)
     * @see com.mediatek.wwtv.tvcenter.nav.NavBasic#isCoExist(int)
     */
    @Override
    public boolean isCoExist(int componentID) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.mediatek.wwtv.tvcenter.nav.NavBasic#onKeyHandler(int, android.view.KeyEvent, boolean)
     */
    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.mediatek.wwtv.tvcenter.nav.NavBasic#onKeyHandler(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        return onKeyHandler(keyCode, event, false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(((componentID & NAV_COMP_ID_MASK) != 0) &&
           (componentID != ComponentsManager.getActiveCompId())){
            ComponentsManager.updateActiveCompId(false, componentID);
        }

        // TODO Auto-generated method stub
        onKeyHandler(keyCode, event, false);
        switch (keyCode) {
        case KeyMap.KEYCODE_VOLUME_DOWN:
        case KeyMap.KEYCODE_VOLUME_UP:
        case KeyMap.KEYCODE_MTKIR_MUTE:
            return true;
        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /* (non-Javadoc)
     * @see com.mediatek.wwtv.tvcenter.nav.NavBasic#initView()
     */
    @Override
    public boolean initView() {
        // TODO Auto-generated method stub

        return false;
    }

    /* (non-Javadoc)
     * @see com.mediatek.wwtv.tvcenter.nav.NavBasic#startComponent()
     */
    @Override
    public boolean startComponent(){

        return false;
    }

    /* (non-Javadoc)
     * @see com.mediatek.wwtv.tvcenter.nav.NavBasic#deinitView()
     */
    @Override
    public boolean deinitView(){
        // TODO Auto-generated method stub
        mHandler.removeCallbacksAndMessages(null);
        ComponentStatusListener.getInstance().removeListener(this);
        this.mContext = null;
        this.mHandler = null;
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        //disable animation
        Window window = this.getWindow();
        window.setWindowAnimations(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DestroyApp.add(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        DestroyApp.remove(this);
    }


    /**
     * this method is used to set the delay show time,
     * when timer is up,the UI should be hided
     *
     * @param delay, the time(second) for UI delay hide
     */
    public void startTimeout(int delay){
        if(mTTSEnable) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TTS is enabled,don't remove dialog");
            return;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTimeout delay=" + delay);
        if(mHandler != null){
            mHandler.removeMessages(componentID);

            Message msg = Message.obtain();
            msg.obj = NAV_COMPONENT_HIDE_FLAG;
            msg.what = componentID;
            msg.arg1 = componentID;
            msg.arg2 = componentID;
            mHandler.sendMessageDelayed(msg, delay);
        }
    }

    @Override
    public void updateComponentStatus(int statusID, int value) {
    }

    //add begin by jg_jianwang for DTV01110498
    protected void setTTSEnabled(boolean enable) {
        mTTSEnable = enable;
    }
    //add end by jg_jianwang for DTV01110498

    /**
     * this method is used to stop time out
     *
     */
    public void stopTimeout(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopTimeout");
        if(mHandler != null){
            mHandler.removeMessages(componentID);
        }
    }

    /**
     * InternalHandler
     */
    private static class InternalHandler extends Handler{
        private final WeakReference<NavBasicDialog> mDialog;

        public InternalHandler(NavBasicDialog dialog, Looper looper){
            super(looper);

            mDialog = new WeakReference<NavBasicDialog>(dialog);
        }

        public void handleMessage(Message msg){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] handlerMessage occur~");
            if(mDialog.get() == null){
                return ;
            }

            if(msg.arg1 == msg.arg2){
                if(msg.arg1 == mDialog.get().getComponentID() &&
                   msg.obj.equals(NavBasicDialog.NAV_COMPONENT_HIDE_FLAG)){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] dismiss()~");
                    mDialog.get().dismiss();
                }
            }
        }
    }
}
