/**
* @Description: TODO()
*/
package com.mediatek.wwtv.tvcenter.dvr.controller;

import android.app.Activity;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.fav.FavoriteListDialog;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.dvr.ui.CommonInfoBar;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

/**
 *
 */
public class UImanager{

	private CommonInfoBar mPopup;
    public static boolean showing = false;

	public void onPause() { 
        android.util.Log.d("UImanage","onPause");

    }

	public void onResume() { 
        android.util.Log.d("UImanage","onResume");

    }

	/*
	 *
	 */
	public void hiddenAllViews() {
		try{
			if(mPopup!=null && mPopup.isShowing()){
				mPopup.dismiss();
				showing = false;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

    public void showInfoBar(String info,Long duration){
        android.util.Log.d("UImanage","info--->"+info+"duration--->"+duration);
    }

    public void showInfoBar(String info){
        CommonIntegration.getInstance().closeFavFullMsg();
        if (DvrManager.getInstance().isInPictureMode()) {
            return;
        }
        try{
            if(ComponentsManager.getActiveCompId()==NavBasicMisc.NAV_COMP_ID_FAV_LIST){
                ((FavoriteListDialog) ComponentsManager.getInstance().getComponentById(
                        NavBasicMisc.NAV_COMP_ID_FAV_LIST)).dismiss();
            }
            if (mPopup != null&& (Activity)mPopup.getContentView().getContext()==TurnkeyUiMainActivity.getInstance()) {
                mPopup.setInfo(info);
            }else {
                mPopup = new CommonInfoBar(TurnkeyUiMainActivity.getInstance(),info);
            }
            mPopup.show();
            showing = true;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void dissmiss() {
        try{
            if (null != mPopup){
                mPopup.dismiss();
                }
            showing = false;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
