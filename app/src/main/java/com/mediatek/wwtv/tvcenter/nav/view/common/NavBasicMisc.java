package com.mediatek.wwtv.tvcenter.nav.view.common;

import java.lang.ref.WeakReference;

import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;

public class NavBasicMisc implements NavBasic, ComponentStatusListener.ICStatusListener {
    protected static String TAG = "NavBasicMisc";
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

    public NavBasicMisc(Context mContext){
        this.mContext = mContext;
        mHandler = new InternalHandler(this, mContext.getMainLooper());
    }

    @Override
    public boolean isVisible() {
      synchronized(this){
        return mIsComponetShow;
      }
    }

    @Override
    public boolean isKeyHandler(int keyCode) {
        // TODO Auto-generated method stub
        return false;
    }

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

    public Handler getHandler(){
        return this.mHandler;
    }

    @Override
    public boolean isCoExist(int componentID) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        return onKeyHandler(keyCode, event, false);
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
    public boolean deinitView() {
        // TODO Auto-generated method stub
        ComponentStatusListener.getInstance().removeListener(this);
        this.mContext = null;
        this.mHandler = null;
        return false;
    }

    @Override
    public void updateComponentStatus(int statusID, int value) {
    }

    /**
     * this method is used to set the visibility of component
     *
     * @param visibility
     */
    public void setVisibility(int visibility) {
        if(visibility == View.VISIBLE){
            synchronized(this){
                mIsComponetShow = true;
                if((componentID & NAV_COMP_ID_MASK) != 0){
                    ComponentsManager.updateActiveCompId(false, componentID);

                    ComponentStatusListener.getInstance().updateStatus(
                        ComponentStatusListener.NAV_COMPONENT_SHOW, this.componentID);
                }
            }
        }
        else{
            synchronized(this){
                mIsComponetShow = false;
                if(componentID == ComponentsManager.getActiveCompId()){
                    ComponentsManager.updateActiveCompId(false, 0);
                }

                if((componentID & NAV_COMP_ID_MASK) != 0){
                    ComponentStatusListener.getInstance().updateStatus(
                            ComponentStatusListener.NAV_COMPONENT_HIDE, this.getComponentID());
                }
            }

            stopTimeout();
        }
    }

    /**
     * this method is used to set the delay show time,
     * when timer is up,the UI should be hided
     *
     * @param delay, the time(second) for UI delay hide
     */
    public void startTimeout(int delay){
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
        private final WeakReference<NavBasicMisc> mMisc;

        public InternalHandler(NavBasicMisc dialog, Looper looper){
            super(looper);

            mMisc = new WeakReference<NavBasicMisc>(dialog);
        }

        public void handleMessage(Message msg){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] handlerMessage occur~");
            if(mMisc.get() == null){
                return ;
            }

             if(msg.arg1 == msg.arg2 && msg.arg1 == mMisc.get().getComponentID()){
                if(msg.obj.equals(NavBasicDialog.NAV_COMPONENT_HIDE_FLAG)){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] invisible~");
                    mMisc.get().setVisibility(View.INVISIBLE);
                }
                else if(msg.obj.equals(NavBasicDialog.NAV_COMPONENT_SHOW_FLAG)){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] visible~");
                    mMisc.get().setVisibility(View.VISIBLE);
                }
                else{
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] fail");
                }
            }
        }
    }
}
