
package com.mediatek.wwtv.tvcenter.dvr.controller;

import android.content.Context;
import android.content.res.Resources;


import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.dvr.manager.Controller;


/**
 *
 */
public class StateBase implements IStateInterface {

    protected StatusType type = StatusType.UNKNOWN;
    protected DvrManager mManager;
    protected Context mContext;
    private boolean isRunning = false;

    public StateBase(Context context, DvrManager manager) {
        super();

        this.mManager = manager;
        this.mContext = context;
        initView();
    }

    public void initView() {
        android.util.Log.d("StateBase","initview");
    }

    /**
     * hideView
     */
    public void hideView() {
    android.util.Log.d("StateBase","hideview");
    }

    /**
     * showView
     */
    public void showView() {
    android.util.Log.d("StateBase","showview");
    }

    /**
     * getType
     *
     * @return the type
     */
    public StatusType getType() {
        return type;
    }

    public void setType(StatusType type) {
        this.type = type;
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
    /**
     * getController
     *
     * @return the controller
     */
    public Controller getController() {
        if (null == mManager) {
            return null;
        }

        return mManager.getController();
    }

    public Resources getResource() {
        return mContext.getResources();
    }

    /**
     * getManager
     *
     * @return the manager
     */
    public DvrManager getManager() {
        return mManager;
    }

    @Override
    public boolean onKeyDown(int keycode) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRelease() {
        // TODO Auto-generated method stub

    }

    @Override
    public void hiddenNotCoExistWindow(int compID) {
        // TODO Auto-generated method stub

    }

    /**
     * Free some resource
     */
    public void free(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.i("StateBase","free");
    }
}
