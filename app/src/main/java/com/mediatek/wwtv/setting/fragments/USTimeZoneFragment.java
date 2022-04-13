package com.mediatek.wwtv.setting.fragments;

import android.app.AlarmManager;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.tvcenter.R;


public class USTimeZoneFragment extends Fragment implements OnKeyListener {
    private static final String TAG = "USTimeZoneFragment";

    private Integer[] mTimeZoneImg = {
            R.drawable.time_zone_map_0, R.drawable.time_zone_map_7,R.drawable.time_zone_map_6,
            R.drawable.time_zone_map_5,R.drawable.time_zone_map_4,R.drawable.time_zone_map_3,R.drawable.time_zone_map_2,R.drawable.time_zone_map_1
    };

	private Context mContext;
	private Action mAction;
    private ImageView mZoneImg;
    private MenuConfigManager mConfigManager;
    private String[] zoneArray = null;
    int defIndex ;

    public void setAction(Action action) {
		mAction = action;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	mContext=getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
        ViewGroup mRootView;
    	mRootView=(ViewGroup)inflater.inflate(R.layout.ustimezone_frag, null);
    	mZoneImg = (ImageView)mRootView.findViewById(R.id.timezone_img);
        bindData();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreateView~");
    	return mRootView;
    }
    
    /**
     *
     */
    private void bindData (){
    	mConfigManager = MenuConfigManager.getInstance(mContext);
    	zoneArray = mContext.getResources().getStringArray(
    			R.array.menu_setup_us_timezone_array);
    	defIndex = mConfigManager.getDefault(MenuConfigManager.SETUP_US_SUB_TIME_ZONE);
    	mZoneImg.setImageResource(mTimeZoneImg[defIndex]);
        mZoneImg.setFocusable(true);
        mZoneImg.requestFocus();
        mZoneImg.setOnKeyListener(this);
//        for (int i = 0; i < TimeZone.getAvailableIDs().length; i++) {
//            Log.d(TAG, "TimeZone"+TimeZone.getAvailableIDs()[i]);
//        }
    }

    public void onKeyLeft() {
		switchValuePrevious();
		mZoneImg.setImageResource(mTimeZoneImg[defIndex]);
	}

	public void onKeyRight() {
		switchValueNext();
		mZoneImg.setImageResource(mTimeZoneImg[defIndex]);
	}

    /**
     *
     */
    private void switchValuePrevious(){
    	if(defIndex == 0 ){
    		defIndex = zoneArray.length -1;
    	}else{
    		defIndex -- ;
    	}
    	setAndroidTimeZone(defIndex);
    	mConfigManager.setValue(MenuConfigManager.SETUP_US_SUB_TIME_ZONE,defIndex,mAction);
    }
    
    private void setAndroidTimeZone(int defIndex){
        AlarmManager mAlarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        switch(defIndex){
            case 0://Eastern
                mAlarmManager.setTimeZone("US/Eastern");
                break;
            case 1://Hawaii
                mAlarmManager.setTimeZone("US/Hawaii");
                break;
            case 2://Alaska
                mAlarmManager.setTimeZone("US/Alaska");
                break;
            case 3://Pacific
                mAlarmManager.setTimeZone("US/Pacific");
                break;
            case 4://Arizona
                mAlarmManager.setTimeZone("US/Arizona");
                break;
            case 5://Mountain
                mAlarmManager.setTimeZone("US/Mountain");
                break;
            case 6://Central
                mAlarmManager.setTimeZone("US/Central");
                break;
            case 7://Indiana
                mAlarmManager.setTimeZone("US/East-Indiana");
                break;
            default:
                break;
        }
    }
    
    
    /**
     *
     */
    private void switchValueNext(){
    	if(defIndex == zoneArray.length -1 ){
    		defIndex = 0;
    	}else{
    		defIndex ++ ;
    	}
        setAndroidTimeZone(defIndex);
    	mConfigManager.setValue(MenuConfigManager.SETUP_US_SUB_TIME_ZONE,defIndex,mAction);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onDestroyView~");
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "event.getAction(): " + event.getAction() + ",keyCode: " + keyCode);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "back key down.");
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "left key down.");
                    onKeyLeft();
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "right key down.");
                    onKeyRight();
                    return true;
                case KeyMap.KEYCODE_MTKIR_RED:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "left key down.");
                    onKeyLeft();
                    return true;
                case KeyMap.KEYCODE_MTKIR_BLUE: 
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "right key down.");
                    onKeyRight();
                    return true;
                default:
                    break;
            }
            return false;
        }
        return false;
    }

}
