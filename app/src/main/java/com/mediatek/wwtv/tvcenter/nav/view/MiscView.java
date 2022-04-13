package com.mediatek.wwtv.tvcenter.nav.view;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicView;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;


public class MiscView extends NavBasicView implements ComponentStatusListener.ICStatusListener {
	private static final String TAG = "MiscView";
	
	private TextView mView ;
	
	
//	private static final int NO_FUNCTION = 0;
	private static final int VIDEO_3D = 1;
	
	private ComponentsManager comManager;
	private TvCallbackHandler mTvCallbackHandler;
	
	public MiscView(Context context, AttributeSet attrs) {
		super(context, attrs);
		componentID = NAV_COMP_ID_MISC;
		ComponentStatusListener.getInstance().addListener(ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
	}
	public MiscView(Context context) {
		this(context, null);
	}
	
	
	
	@Override
	public boolean initView(){
		((Activity) getContext()).getLayoutInflater().inflate(
				R.layout.nav_sundry_view, this);	
		mView = (TextView) findViewById(R.id.nav_sundry_textview_id);
		comManager = ComponentsManager.getInstance();
		mTvCallbackHandler = TvCallbackHandler.getInstance();
		mTvCallbackHandler.addCallBackListener(miscHandler);
		return true;
		
	}
	
	@Override
	public boolean deinitView() {
		mTvCallbackHandler.removeCallBackListener(miscHandler);

		return true;
	}
	
    @Override
    public boolean isCoExist(int componentID) {
        // TODO Auto-generated method stub
    	switch(componentID){
    	case NAV_COMP_ID_CEC:
    	case NAV_COMP_ID_BANNER:
    		return true;
        default:
            break;
    	}
        return false;
    }
	
	@Override
    public boolean isKeyHandler(int keyCode) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isKeyHandler keyCode ="+keyCode);
		switch(keyCode){
//		case KeyMap.KEYCODE_MTKIR_FREEZE:
		case KeyMap.KEYCODE_MTKIR_PIPPOP:
		case KeyMap.KEYCODE_MTKIR_PIPPOS:
		case KeyMap.KEYCODE_MTKIR_PIPSIZE:
		    boolean isFvp = ComponentsManager.getNativeActiveCompId()==NAV_NATIVE_COMP_ID_FVP;
		       if(isFvp){
		           break;
		       }
//		case KeyMap.KEYCODE_MTKIR_ZOOM:
		//case KeyMap.KEYCODE_MTKIR_ASPECT:
		//case KeyMap.KEYCODE_MTKIR_MTSAUDIO:
			mView.setText(R.string.nav_no_function);
			comManager.showNavComponent(NAV_COMP_ID_MISC);		
			if (StateDvr.getInstance() != null && StateDvr.getInstance().isRecording()) {
				StateDvr.getInstance().clearWindow(true);
			}
			return true;
        default:
            break;
		}
        return false;
    }

	@Override
    public boolean onKeyHandler(int keyCode, KeyEvent event) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler>no fromNative");
       return onKeyHandler(keyCode, event,false);
    };
	
    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
        if(mView != null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler>" + fromNative + "  " + keyCode + "  " + mView.getText().toString() + "  " + getVisibility());
            switch(keyCode){
            case KeyMap.KEYCODE_BACK:
                if (getVisibility() == View.VISIBLE && mContext != null
                && mView.getText().equals(mContext.getText(R.string.nav_no_function))) {
                    setVisibility(View.GONE);
                    return true;
                }
                break;
            default:
                break;
            }
        }
        return false;
    }
	
	
	@Override
	public void setVisibility(int visibility) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"setVisibility visibility ="+visibility);
		if (View.VISIBLE == visibility) {
			startTimeout(NAV_TIMEOUT_5);
		}
		super.setVisibility(visibility);
	}

	
	
	
	private Handler miscHandler = new Handler() {
		public void handleMessage(Message msg) {
			// misc
			switch (msg.what) {
			case TvCallbackConst.MSG_CB_FEATURE_MSG:
				TvCallbackData data = (TvCallbackData) msg.obj;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"come in TvCallbackConst.MSG_CB_FEATURE_MSG ==" + data.param1);
				switch (data.param1) {
//				case NO_FUNCTION:
//					sundryTextView.setText(R.string.nav_no_function);
//					// setVisibility(View.VISIBLE);
//					comManager.showNavComponent(NAV_COMP_ID_SUNDRY);
//					break;
				case VIDEO_3D:
					mView.setText(R.string.menu_video_3d_wear_glass);
					// setVisibility(View.VISIBLE);
					comManager.showNavComponent(NAV_COMP_ID_SUNDRY);
					break;
                default:
                    break;
				}
				break;
            default:
                break;
			}
		}
	};

	@Override
	public void updateComponentStatus(int statusID, int value) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus>>" + statusID + "  " + value);
		switch (statusID) {
		case ComponentStatusListener.NAV_CHANNEL_CHANGED:
			if (mContext != null && getVisibility() == View.VISIBLE) {
				setVisibility(View.GONE);
			}
			break;

		default:
			break;
		}
	}
	
	

}
