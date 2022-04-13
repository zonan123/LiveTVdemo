/**
 *
 */
package com.mediatek.wwtv.tvcenter.nav.view.common;

import java.lang.ref.WeakReference;

import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;


import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;

/**
 * @author mtk40707
 *
 */
public class NavBasicView extends LinearLayout implements NavBasic, ComponentStatusListener.ICStatusListener {
    protected static String TAG = "NavBasicView";

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

    public NavBasicView(Context context){
        super(context);

        mContext = context;
        mHandler = new InternalHandler(this, context.getMainLooper());
        initView();
    }

    public NavBasicView(Context context, AttributeSet attrs){
        super(context, attrs);

        mContext = context;
        mHandler = new InternalHandler(this, context.getMainLooper());
        initView();
    }

    public NavBasicView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);

        mContext = context;
        mHandler = new InternalHandler(this, context.getMainLooper());
        initView();
    }

    @Override
    public void updateComponentStatus(int statusID, int value) {
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            synchronized(this){
                mIsComponetShow = true;
                if((componentID & NAV_COMP_ID_MASK) != 0){
                    ComponentsManager.updateActiveCompId(false, componentID);
                    ComponentStatusListener.getInstance().updateStatus(
                        ComponentStatusListener.NAV_COMPONENT_SHOW, this.componentID);
                }
            }
        } else {
            synchronized(this){
                mIsComponetShow = false;
                if(componentID == ComponentsManager.getActiveCompId()){
                    ComponentsManager.updateActiveCompId(false, 0);
                }

                if((componentID & NAV_COMP_ID_MASK) != 0){
                    ComponentStatusListener.getInstance().updateStatus(
                            ComponentStatusListener.NAV_COMPONENT_HIDE, this.getComponentID());
                }

                //close timerTask
                stopTimeout();
            }
        }

        super.setVisibility(visibility);
    }

    /* (non-Javadoc)
     * @see com.mediatek.wwtv.tvcenter.nav.NavBasic#isVisible()
     */
    @Override
    public boolean isVisible() {
        // TODO Auto-generated method stub
        synchronized(this){
            return mIsComponetShow;
        }
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

    public Handler getHandler(){
        return this.mHandler;
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
        ComponentStatusListener.getInstance().removeListener(this);
        this.mContext = null;
        this.mHandler = null;
        return false;
    }

    /**
     * this method is used to set the delay show time,
     * when timer is up,the UI should be hided
     *
     * @param delay, the time(second) for UI delay hide
     */
    public void startTimeout(int delay){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTimeout delay=" + delay + ", componentID: " + componentID);
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
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "stopTimeout, componentID: " + componentID);
        if(mHandler != null){
            mHandler.removeMessages(componentID);
        }
    }

    /**
     * InternalHandler
     */
    private static class InternalHandler extends Handler{
        private final WeakReference<NavBasicView> mView;

        public InternalHandler(NavBasicView dialog, Looper looper){
            super(looper);

            mView = new WeakReference<NavBasicView>(dialog);
        }

        public void handleMessage(Message msg){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] handlerMessage occur~");
            if(mView.get() == null){
                return ;
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] mView.get().getComponentID(): " + mView.get().getComponentID());
            if(msg.arg1 == msg.arg2 && msg.arg1 == mView.get().getComponentID()){
                if(msg.obj.equals(NavBasicDialog.NAV_COMPONENT_HIDE_FLAG)){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] gone~");
                    mView.get().setVisibility(View.GONE);
                } else if(msg.obj.equals(NavBasicDialog.NAV_COMPONENT_SHOW_FLAG)){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] visible~");
                    mView.get().setVisibility(View.VISIBLE);
                } else{
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] fail");
                }
            }
        }
    }

}
