package com.mediatek.wwtv.tvcenter.nav.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicView;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.IntegrationZoom;

public class ZoomTipView extends NavBasicView {
    private final static String TAG = "ZoomTipView";

//    private RelativeLayout zoomRelativeLayout;
	private IntegrationZoom mIntegrationZoom;
   

    public ZoomTipView(Context context) {
        super(context);
        
    }

    public ZoomTipView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        this.componentID = NavBasicView.NAV_COMP_ID_ZOOM_PAN;
    }

    public ZoomTipView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        this.componentID = NavBasicView.NAV_COMP_ID_ZOOM_PAN;
    }

    @Override
    public void setVisibility(int visibility) {

    	if (visibility == View.VISIBLE) {
    		startTimeout(NAV_TIMEOUT_5);
    	}
        super.setVisibility(visibility);
    }

    @Override
    public boolean isKeyHandler(int keyCode) {
        return false;
    }

    @Override
    public boolean isCoExist(int componentID) {
        // TODO Auto-generated method stub
    	switch(componentID){
    	case NAV_COMP_ID_SUNDRY:
    	case NAV_COMP_ID_BANNER:
    	case NAV_COMP_ID_PVR_TIMESHIFT:
    		return true;
    	default:
    	    break;
    	}
        return false;
    }

    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
        boolean isHandled = true;
        // TODO Auto-generated method stub
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler: keyCode=" + keyCode);

        switch (keyCode) {
		case KeyMap.KEYCODE_DPAD_DOWN:
			startTimeout(NAV_TIMEOUT_5);
			mIntegrationZoom.moveScreenZoom(IntegrationZoom.ZOOM_DOWN);
			break;
		case KeyMap.KEYCODE_DPAD_UP:
			startTimeout(NAV_TIMEOUT_5);
			mIntegrationZoom.moveScreenZoom(IntegrationZoom.ZOOM_UP);
			break;
		case KeyMap.KEYCODE_DPAD_LEFT:
			startTimeout(NAV_TIMEOUT_5);
			mIntegrationZoom.moveScreenZoom(IntegrationZoom.ZOOM_LEFT);
			break;
		case KeyMap.KEYCODE_DPAD_RIGHT:
			startTimeout(NAV_TIMEOUT_5);
			mIntegrationZoom.moveScreenZoom(IntegrationZoom.ZOOM_RIGHT);
			break;
		case KeyMap.KEYCODE_BACK:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler hide");
            this.setVisibility(View.GONE);
            SundryShowTextView sundryShowTextView = ((SundryShowTextView)ComponentsManager.getInstance().getComponentById(NAV_COMP_ID_SUNDRY));
            if (sundryShowTextView != null && sundryShowTextView.getVisibility() == View.VISIBLE) {
            	sundryShowTextView.setVisibility(View.GONE);
            }
            break;
		default:
			isHandled = false;
			break;
        }

        return isHandled;
    }

    @Override
    public boolean initView(){
    	
    	((Activity) mContext).getLayoutInflater().inflate(
				R.layout.nav_zoom_layout, this);
//        
//        zoomRelativeLayout = (RelativeLayout) findViewById(R.id.nav_channel_zoom);
    	this.componentID = NavBasicView.NAV_COMP_ID_ZOOM_PAN;
		mIntegrationZoom = IntegrationZoom.getInstance(mContext);

        return true;
    }

}
