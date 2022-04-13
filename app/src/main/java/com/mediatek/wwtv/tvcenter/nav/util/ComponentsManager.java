package com.mediatek.wwtv.tvcenter.nav.util;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicView;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyDispatch;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public final class ComponentsManager {
    private static final String TAG = "ComponentsManager";
    private List<NavBasicDialog>      mNavDialogs = null;
    private List<NavBasicView>        mNavViews   = null;
    private List<NavBasicMisc>        mNavMiscs    = null;

    private static boolean            isFromNative          = false;
    private static int                mCurrentAndroidCompId = 0;
    private static int                mCurrentNativeCompId  = 0;

    //local variables
    private static ComponentsManager mNavComponentsManager = null;

    private static CommonIntegration mNavIntegration = null;

    private ComponentsManager(){
        mNavDialogs = new ArrayList<NavBasicDialog>();
        mNavViews = new ArrayList<NavBasicView>();
        mNavMiscs = new ArrayList<NavBasicMisc>();
    }

    public static synchronized ComponentsManager getInstance() {
        if(mNavComponentsManager == null){
            mNavComponentsManager = new ComponentsManager();
        }

        return mNavComponentsManager;
    }

    /**
     * this method is used to get the component object by component id
     *
     * @param compId
     * @return
     */
    public NavBasic getComponentById(int compId){
        int i;
        try{
            for(i = 0; i < mNavDialogs.size(); i++){
                if(mNavDialogs.get(i).getComponentID() == compId){
                    return mNavDialogs.get(i);
                }
            }

            for(i = 0; i < mNavViews.size(); i++){
                if(mNavViews.get(i).getComponentID() == compId){
                    return mNavViews.get(i);
                }
            }

            for(i = 0; i < mNavMiscs.size(); i++){
                if(mNavMiscs.get(i).getComponentID() == compId){
                    return mNavMiscs.get(i);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * this method is used to clear all component objects
     *
     */
    public void clear(){
        if(mNavDialogs != null){
            mNavDialogs.clear();
        }
        else{
            mNavDialogs = new ArrayList<NavBasicDialog>();
        }

        if(mNavViews != null){
            mNavViews.clear();
        }
        else{
            mNavViews = new ArrayList<NavBasicView>();
        }

        if(mNavMiscs != null){
            mNavMiscs.clear();
        }
        else{
            mNavMiscs = new ArrayList<NavBasicMisc>();
        }
    }

    /**
     * this method is used to add dialog component
     *
     * @param mNavBasicDialog
     */
    public void addDialog(NavBasicDialog mNavBasicDialog){
        mNavDialogs.add(mNavBasicDialog);

        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mNavDialogs size:" + mNavDialogs.size() + "mNavBasicDialog object:" + mNavBasicDialog);
    }

    /**
     * this method is used to add view component
     *
     * @param mNavBasicView
     */
    public void addView(NavBasicView mNavBasicView){
        mNavViews.add(mNavBasicView);

        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mNavViews size:" + mNavViews.size() + "mNavBasicView object:" + mNavBasicView);
    }

    /**
     * this method is used to add basic component which is only implements NavBasic
     *
     * @param mNavBasic
     */
    public void addMisc(NavBasicMisc mNavMisc){
        mNavMiscs.add(mNavMisc);

        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mNavMiscs size:" + mNavMiscs.size() + "NavBasicMisc object:" + mNavMisc);
    }

    /**
     * this method is used to check the status of component
     *
     */
    public boolean isCompsDestroyed(){
        if((mNavDialogs != null) && !mNavDialogs.isEmpty()){
            return false;
        }else if((mNavViews != null) && !mNavViews.isEmpty()){
            return false;
        }else if((mNavMiscs != null) && !mNavMiscs.isEmpty()){// NOPMD
            return false;
        }else{
            return true;
        }
    }

    /**
     * this method is used to check which dialog component handle this key
     *
     * @param keyCode, RC key
     *
     * @return null if there is no component handle that key,
     * component object if key is the hot key of the dialog component
     */
    private NavBasicDialog isDialogKeyHandle(int keyCode){
        try{
            //int i = 0, size = mNavDialogs.size();
            for(NavBasicDialog dialog:mNavDialogs){
                if(dialog.isKeyHandler(keyCode)){
                    return dialog;
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * this method is used to check which view component handle this key
     *
     * @param keyCode, RC key
     *
     * @return null if there is no component handle that key,
     * component object if key is the hot key of the view component
     */
    private NavBasicView isViewKeyHandle(int keyCode){
        try{
            //int i = 0, size = mNavViews.size();
            for(NavBasicView navView:mNavViews){
                if(navView.isKeyHandler(keyCode)){
                    return navView;
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * this method is used to check which object component handle this key
     *
     * @param keyCode
     * @return
     */
    private NavBasicMisc isMiscKeyHandle(int keyCode){
        try{
            //int i = 0, size = mNavMiscs.size();
            for(NavBasicMisc bm:mNavMiscs){
                if(bm.isKeyHandler(keyCode)){
                    return bm;
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * this method is used to hide the componets which is not conexist with current component
     *
     * @param basic, the component object
     * @param handler, the handler of the main activity be used to hide the view component
     *
     * @return true if some components be hide, false if none
     */
    private boolean hideUnCoExistComponent(NavBasic basic){
//        int i, size;
//        NavBasicDialog mNavdlg;
//        NavBasicView mNavView;
//        NavBasicMisc mNavMisc;

        try{
            for(NavBasicDialog mNavdlg:mNavDialogs){
                //mNavdlg = mNavDialogs.get(i);
                if(mNavdlg.isVisible() && (!basic.isCoExist(mNavdlg.getComponentID()))){
                    if(mNavdlg.getPriority() > basic.getPriority()){
                        return false;
                    }

                    mNavdlg.dismiss();
                }
            }

            for(NavBasicView mNavView:mNavViews){
                //mNavView = mNavViews.get(i);
                if(mNavView.isVisible() && (!basic.isCoExist(mNavView.getComponentID()))){
                    if(mNavView.getPriority() > basic.getPriority()){
                        return false;
                    }

                    if(mNavView.getComponentID() == NavBasic.NAV_COMP_ID_TIFTIMESHIFT_VIEW &&
                            mNavIntegration.isCurrentSourceBlockEx() &&
                            !mNavIntegration.isCurrentSourceTv()){
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "NAV_COMP_ID_TIFTIMESHIFT_VIEW~");
                    } else {
                        mNavView.setVisibility(View.GONE);
//                        Message msg = Message.obtain();
//                        msg.obj = NavBasic.NAV_COMPONENT_HIDE_FLAG;
//                        msg.arg1 = msg.arg2 = mNavView.getComponentID();
//                        mNavView.getHandler().sendMessage(msg);
                    }
                }
            }

            for(NavBasicMisc mNavMisc:mNavMiscs){
                //mNavMisc = mNavMiscs.get(i);
                if(mNavMisc.isVisible() && (!basic.isCoExist(mNavMisc.getComponentID()))){
                    if(mNavMisc.getPriority() > basic.getPriority()){
                        return false;
                    }
                    mNavMisc.setVisibility(View.GONE);
//                    Message msg = Message.obtain();
//                    msg.obj = NavBasic.NAV_COMPONENT_HIDE_FLAG;
//                    msg.arg1 = msg.arg2 = mNavMisc.getComponentID();
//                    mNavMisc.getHandler().sendMessage(msg);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return true;
    }

    /**
     * this method is used to hide all components
     *
     * @return
     */
    public boolean hideAllComponents(){
        return hideAllComponents((Integer) null);
    }

    /**
     * this method is used to hide all components
     *
     * @return
     */
    public boolean hideAllComponents(Integer... exceptIdArrays){
        try{
            List<Integer> exceptIds = null;
            if(exceptIdArrays != null) {
                exceptIds = Arrays.asList(exceptIdArrays);
            }

            for(NavBasicDialog nd:mNavDialogs){
                if(nd.isVisible()){
                    if(exceptIds != null && exceptIds.contains(nd.getComponentID())) {
                        continue;
                    }
                    nd.dismiss();
                }
            }

            for(NavBasicView view: mNavViews){
                if(view.isVisible()){
                    if(exceptIds != null && exceptIds.contains(view.getComponentID())) {
                        continue;
                    }
                    view.setVisibility(View.GONE);

                }
            }

            for(NavBasicMisc misc :mNavMiscs){
                if(misc.isVisible()){
                    if(exceptIds != null && exceptIds.contains(misc.getComponentID())) {
                        continue;
                    }
                    misc.setVisibility(View.GONE);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return true;
    }

    /**
     * this method is used to trigger the component by hot key, and show the component
     *
     * @param keyCode, the hot key
     * @param event, key event
     *
     * @return component object if component is triggered, null if none
     */
    public NavBasic showNavComponent(int keyCode, KeyEvent event){
        try{
            NavBasicDialog mNavdlg = isDialogKeyHandle(keyCode);
            if((mNavdlg != null) && hideUnCoExistComponent(mNavdlg)){
                mNavdlg.show();
                return mNavdlg;
            }

            NavBasicMisc mNavMisc = isMiscKeyHandle(keyCode);
            if((mNavMisc != null) && hideUnCoExistComponent(mNavMisc)){
                mNavMisc.setVisibility(View.VISIBLE);
//                Message msg = Message.obtain();
//                msg.obj = NavBasic.NAV_COMPONENT_SHOW_FLAG;
//                msg.arg1 = msg.arg2 = mNavMisc.getComponentID();
//                mNavMisc.getHandler().sendMessage(msg);
                return mNavMisc;
                //wait for enhance
            }

            NavBasicView mNavView = isViewKeyHandle(keyCode);
            if((mNavView != null) && hideUnCoExistComponent(mNavView)){
                mNavView.setVisibility(View.VISIBLE);
//                Message msg = Message.obtain();
//                msg.obj = NavBasic.NAV_COMPONENT_SHOW_FLAG;
//                msg.arg1 = msg.arg2 = mNavView.getComponentID();
//                mNavView.getHandler().sendMessage(msg);
                return mNavView;
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    /**
     *
     * this method is used to trigger the component by component id
     *
     * @param compId, the component id
     *
     * @return component object if component is triggered, null if none
     */
    public NavBasic showNavComponent(int compId){
//        int i, size;
//        NavBasicDialog mNavdlg;
//        NavBasicView mNavView;
//        NavBasicMisc mNavMisc;

        try{
            for(NavBasicDialog mNavdlg:mNavDialogs){
                if((mNavdlg.getComponentID() == compId) && hideUnCoExistComponent(mNavdlg)){
                    mNavdlg.show();
                    return mNavdlg;
                }
            }

            for(NavBasicView mNavView:mNavViews){
                //mNavView = mNavViews.get(i);
                if((mNavView.getComponentID() == compId) && hideUnCoExistComponent(mNavView)){
                    mNavView.setVisibility(View.VISIBLE);
//                    Message msg = Message.obtain();
//                    msg.obj = NavBasic.NAV_COMPONENT_SHOW_FLAG;
//                    msg.arg1 = msg.arg2 = mNavView.getComponentID();
//                    mNavView.getHandler().sendMessage(msg);
                    return mNavView;
                }
            }

            for(NavBasicMisc mNavMisc:mNavMiscs){
                //mNavMisc = mNavMiscs.get(i);
                if((mNavMisc.getComponentID() == compId) && hideUnCoExistComponent(mNavMisc)){
                    mNavMisc.setVisibility(View.VISIBLE);
//                    Message msg = Message.obtain();
//                    msg.obj = NavBasic.NAV_COMPONENT_SHOW_FLAG;
//                    msg.arg1 = msg.arg2 = mNavMisc.getComponentID();
//                    mNavMisc.getHandler().sendMessage(msg);
                    return mNavMisc;
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * this method is used to dispatch key to active component
     *
     * @param keyCode, key code
     * @param event, event
     *
     * @return true if key handled, otherwise, false
     */
    public boolean dispatchKeyToActiveComponent(int keyCode, KeyEvent event){
        boolean isHandled = false;
//        int i, size;
////        NavBasicDialog mNavdlg;
//        NavBasicView mNavView;
//        NavBasicMisc mNavMisc;
        try{
            if((mCurrentNativeCompId & NavBasic.NAV_NATIVE_COMP_ID_BASIC) != 0){
                if ((MarketRegionInfo
                        .isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT))
                        && (mCurrentNativeCompId == NavBasic.NAV_NATIVE_COMP_ID_MHEG5)
                        && (keyCode == KeyMap.KEYCODE_MTKIR_PIPPOP)) {
                    PIPPOPSurfaceViewControl.getSurfaceViewControlInstance()
                            .changeOutputWithTVState(
                                    PipPopConstant.TV_PIP_STATE);
                }
                if (KeyDispatch.getInstance().passKeyToNative(keyCode, event)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "key is respond by native module.event != NULL");
                    return true;// will callback key if native component do not handle
                }

            }
/* need not send to Dialog Component, because key is from it
 *
            for(i = 0, size = mNavDialogs.size(); i < size; i++){
                mNavdlg = mNavDialogs.get(i);
                if(mNavdlg.isVisible() &&
                   mNavdlg.getComponentID() != mCurrentAndroidCompId){
                    isHandled |= mNavdlg.onKeyHandler(keyCode, event, true);
                }
            }
*/
            for(NavBasicView mNavView:mNavViews){
                //mNavView = mNavViews.get(i);
                if(mNavView.isVisible() &&
                   mNavView.getComponentID() != mCurrentAndroidCompId){
                    isHandled |= mNavView.onKeyHandler(keyCode, event, true);
                }
            }

            for(NavBasicMisc mNavMisc:mNavMiscs){
                //mNavMisc = mNavMiscs.get(i);
                if(mNavMisc.isVisible() &&
                   mNavMisc.getComponentID() != mCurrentAndroidCompId){
                    isHandled |= mNavMisc.onKeyHandler(keyCode, event, true);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return isHandled;
    }

    /**
     * this method is used to dispatch key to active component(key is from native)
     *
     * @param keyCode, key code
     * @param event, event
     * @param noKeyDown
     *
     * @return true if key handled, otherwise, false
     */
    public boolean dispatchKeyToActiveComponent(int keyCode, KeyEvent event, boolean noKeyDown){
        boolean isHandled = false;
//        int i, size;
////        NavBasicDialog mNavdlg;
//        NavBasicView mNavView;
//        NavBasicMisc mNavMisc;
        try{
            if((!noKeyDown) && ((mCurrentNativeCompId & NavBasic.NAV_NATIVE_COMP_ID_BASIC) != 0)){
                if ((MarketRegionInfo
                        .isFunctionSupport(MarketRegionInfo.F_TIF_SUPPORT))
                        && (mCurrentNativeCompId == NavBasic.NAV_NATIVE_COMP_ID_MHEG5)
                        && (keyCode == KeyMap.KEYCODE_MTKIR_PIPPOP)) {
                    PIPPOPSurfaceViewControl.getSurfaceViewControlInstance()
                            .changeOutputWithTVState(
                                    PipPopConstant.TV_PIP_STATE);
                }

                if (KeyDispatch.getInstance().passKeyToNative(keyCode, event)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "key is respond by native module");
                    return true;// will callback key if native component do not handle
                }
            }

/* need not send to Dialog Component, because key is from it
 *
            for(i = 0, size = mNavDialogs.size(); i < size; i++){
                mNavdlg = mNavDialogs.get(i);
                if(mNavdlg.isVisible()  && (ComponentsManager.isFromNative ||
                   mNavdlg.getComponentID() != mCurrentAndroidCompId)){
                    isHandled |= mNavdlg.onKeyHandler(keyCode, null, true);
                }
            }
*/
            for(NavBasicView mNavView:mNavViews){
                //mNavView = mNavViews.get(i);
                if(mNavView.isVisible() && (ComponentsManager.isFromNative ||
                   mNavView.getComponentID() != mCurrentAndroidCompId)){
                    isHandled |= mNavView.onKeyHandler(keyCode, event, true);
                }
            }

            for(NavBasicMisc mNavMisc:mNavMiscs){
                //mNavMisc = mNavMiscs.get(i);
                if(mNavMisc.isVisible() && (ComponentsManager.isFromNative ||
                   mNavMisc.getComponentID() != mCurrentAndroidCompId)){
                    isHandled |= mNavMisc.onKeyHandler(keyCode, event, true);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return isHandled;
    }

    /**
     * thie methods is used to deinit all components
     */
    public void deinitComponents(){
        //int i, size;
        for(NavBasicDialog nd: mNavDialogs){
            safelyRun(nd::deinitView);
        }

        for(NavBasicView nv :mNavViews){
            safelyRun(nv::deinitView);
        }

        for(NavBasicMisc nm:mNavMiscs){
            safelyRun(nm::deinitView);
        }
    }
    private void safelyRun(Runnable runnable){
        try{
            runnable.run();
        }catch (Exception e){
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void deinitMiscsComponent(int mNavMisc){
        try{
            for(int i = 0; i < mNavMiscs.size(); i++){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deinitMiscsComponent----:"+mNavMiscs.get(i).getComponentID()+"----:"+mNavMisc);
                if(mNavMiscs.get(i).getComponentID() == mNavMisc){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "deinitMiscsComponent--remove--:"+mNavMiscs.get(i).getComponentID());
                    mNavMiscs.remove(i);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }


    /**
     * thie methods is used to deinit all nav dialog
     */
	public void deinitNavDialog() {
		//int i, size;
        for (NavBasicDialog nd: mNavDialogs) {
            nd.deinitView();
        }
        mNavDialogs.clear();
	}

    /**
     * this method is used to check the status of components
     *
     * @return true if any one show, false if all components hide
     */
    public boolean isComponentsShow(){
        //int i, size;

        try{
            for(NavBasicDialog nd: mNavDialogs){
                if(nd.isVisible()){
                    return true;
                }
            }

            for(NavBasicView nv: mNavViews){
                if(nv.isVisible()){
                    return true;
                }
            }

            for(NavBasicMisc nm:mNavMiscs){
                if(nm.isVisible()){
                    return true;
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public boolean isComponentsShowIgnoreTwinkle(){
        int i;
        int size;

        try{
            for(i = 0, size = mNavDialogs.size(); i < size; i++){
                NavBasicDialog dialog = mNavDialogs.get(i);
                if(mNavDialogs.get(i).isVisible() &&
                        dialog.getComponentID() != NavBasic.NAV_COMP_ID_TWINKLE_DIALOG){
                    return true;
                }
            }

            for(i = 0, size = mNavViews.size(); i < size; i++){
                if(mNavViews.get(i).isVisible()){
                    return true;
                }
            }

            for(i = 0, size = mNavMiscs.size(); i < size; i++){
                if(mNavMiscs.get(i).isVisible()){
                    return true;
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public List<Integer> getCurrentActiveComps(){
        //int i = 0, size = 0;
        List<Integer> list = new ArrayList<Integer>();

        try{
            for(NavBasicDialog nd: mNavDialogs){
                if(nd.isVisible()){
                    list.add(nd.getComponentID());
                }
            }

            for(NavBasicView nv: mNavViews){
                if(nv.isVisible()){
                    list.add(nv.getComponentID());
                }
            }

            for(NavBasicMisc nm:mNavMiscs){
                if(nm.isVisible()){
                    list.add(nm.getComponentID());
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return list;
    }

    /**
     * this method is used to update the component id when component is active or hide
     *
     * @param isFromNative, if the active component is from android, isFromNative should be false;
     * otherwise, isFromNative is true
     *
     * @param componentId, refer class com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic
     *
     */
    public static void updateActiveCompId(boolean isFromNative, int componentId){
        synchronized(ComponentsManager.class){
            if(isFromNative){
                ComponentsManager.mCurrentNativeCompId = componentId;
            }
            else{
                ComponentsManager.mCurrentAndroidCompId = componentId;
            }

            if(componentId != 0){
                ComponentsManager.isFromNative = isFromNative;
            }
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "componentId:" + componentId + ", isFromNative=" + isFromNative);
    }

    public static void nativeComponentReActive(){
        synchronized(ComponentsManager.class){
            if((mCurrentNativeCompId & NavBasic.NAV_NATIVE_COMP_ID_BASIC) != 0){
                ComponentsManager.isFromNative = true;
            }
        }
    }

    /**
     * this method is used to get the active component id
     *
     * @return the component id, if id & NavBasic.NAV_COMP_ID_BASIC is
     * not equal zero, current active component is from android;
     * if id & NavBasic.NAV_NATIVE_COMP_ID_BASIC is not equal zero,
     * current active component is from native;
     * otherwise, there is not any active component
     */
    public static int getActiveCompId(){
        synchronized(ComponentsManager.class){
            return (ComponentsManager.isFromNative) ?
                    ComponentsManager.mCurrentNativeCompId :
                        ComponentsManager.mCurrentAndroidCompId;
        }
    }

    /**
     * this method is used to get the native active component id
     *
     * @return the component id
     */
    public static int getNativeActiveCompId(){
        return ComponentsManager.mCurrentNativeCompId;
    }

    @Override
    public String toString(){
        String str = "NavComponentsManager:\n";

        try{
            str += "\nDialog:\n";
            for(int i = 0; (mNavDialogs != null) && (i < mNavDialogs.size()); i++){
                if(mNavDialogs.get(i) == null){
                    str += i + ":null\n";
                }
                else{
                    str += Integer.toHexString(mNavDialogs.get(i).getComponentID()) +
                            ", status:" + mNavDialogs.get(i).isVisible() +";\n";
                }
            }

            str += "\nView:\n";
            for(int i = 0; (mNavViews != null) && (i < mNavViews.size()); i++){
                if(mNavViews.get(i) == null){
                    str += i + ":null\n";
                }
                else{
                    str += Integer.toHexString(mNavViews.get(i).getComponentID()) +
                            ", status:" + mNavViews.get(i).isVisible() +";\n";
                }
            }

            str += "\nMisc:\n";
            for(int i = 0; (mNavMiscs != null) && (i < mNavMiscs.size()); i++){
                if(mNavMiscs.get(i) == null){
                    str += "null\n";
                }
                else{
                    str += Integer.toHexString(mNavMiscs.get(i).getComponentID()) +
                            ", status:" + mNavMiscs.get(i).isVisible() +";\n";
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        str +="\nmCurrentAndroidCompId = " + Integer.toHexString(mCurrentAndroidCompId) +
                ", mCurrentNativeCompId = " + Integer.toHexString(mCurrentNativeCompId) +
                ", isFromNative = " + isFromNative + "\n";
        return str;
    }

    public boolean isContainsComponentsForCurrentActiveComps(int... compIds) {
        List<Integer> ids = getCurrentActiveComps();
        boolean find = false;
        for (int compId : compIds) {
            if (ids.contains(compId)) {
                find = true;
                break;
            }
        }
        return find;
    }
}

