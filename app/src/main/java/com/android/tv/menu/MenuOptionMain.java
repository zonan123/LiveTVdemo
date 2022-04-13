package com.android.tv.menu;

import com.mediatek.wwtv.tvcenter.util.KeyMap;

import android.util.Log;
import android.content.Context;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.os.SystemClock;
import com.android.tv.menu.Menu.OnAutoHideListener;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.twoworlds.tv.MtkTvBroadcastBase;
import com.mediatek.twoworlds.tv.model.MtkTvExternalUIStatusBase;

//import android.content.IntentFilter;
//import android.content.BroadcastReceiver;
//import android.content.Intent;

public class MenuOptionMain extends com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc implements
ComponentStatusListener.ICStatusListener{
    public static final String TAG = "MenuOptionMain";

    public static final int OPTION_CLOSED_CAPTIONS = 0;
    //public static final int OPTION_DISPLAY_MODE = 1;
    public static final int OPTION_IN_APP_PIP = 2;
    public static final int OPTION_SYSTEMWIDE_PIP = 3;
    public static final int OPTION_MULTI_AUDIO = 4;
    public static final int OPTION_MORE_CHANNELS = 5;
    public static final int OPTION_DEVELOPER = 6;
    public static final int OPTION_SETTINGS = 7;
    //public static final int OPTION_MY_FAVORITE = 8;
    // Funai Android Patch Begin
    //public static final int OPTION_AUTO_PICTURE = 9;
//    public static final int OPTION_SPEAKERS = 10;
    // Funai Android Patch End
    public static final int OPTION_BROADCAST_TV_SETTINGS = 11;
    public static final int OPTION_POWER = 12;
    public static final int OPTION_BROADCAST_TV_OAD = 13;
    public static final int OPTION_BROADCAST_TV_CI = 14;

    public static final int OPTION_SOURCE_CAPTIONS=15;
    public static final int OPTION_DVR_LIST = 16;
    public static final int OPTION_TSHIFT_MODE = 17;
    public static final int OPTION_SCHEDULE_LIST = 18;
    public static final int OPTION_DEVICE_INFO = 19;
    public static final int OPTION_PVR_START = 20;
    public static final int OPTION_PVR_STOP = 21;
    
    public static final int OPTION_GINGA = 22;
    public static final int OPTION_PROGRAM_GUIDE = 23;
    public static final int OPTION_NEW_CHANNELS = 24;
    public static final int OPTION_CHANNEL_UP = 25;
    public static final int OPTION_CHANNEL_DOWN = 26;
    public static final int OPTION_APP_LINK = 27;

    public static final int OPTION_PIP_INPUT = 100;
    public static final int OPTION_PIP_SWAP = 101;
    public static final int OPTION_PIP_SOUND = 102;
    public static final int OPTION_PIP_LAYOUT = 103 ;
    public static final int OPTION_PIP_SIZE = 104;
    //public static final int OPTION_SOUND_STYLE = 105;
    public static final int OPTION_CHANNEL = 106;
    public static final int OPTION_PICTURE = 107;
    public static final int OPTION_SOUND = 108;

    private Menu mMenu;
    private MenuView mMenuView;
    long lastMenuKeyTime = 0;
    private static final int INTERVAL_TIME = 400; 
    private final SparseArray<OptionChangedListener> mOptionChangedListeners = new SparseArray<>();
    
    private MtkTvBroadcastBase mMtkTvBroadcastBase;
    //private BroadcastReceiver headReceiver;

    public MenuOptionMain(Context context) {
        super(context);
        componentID = NavBasic.NAV_COMP_ID_MENU_OPTION_DIALOG;
        mMenuView = (MenuView)((TurnkeyUiMainActivity) context).findViewById(R.id.menu);
        mMenuView.removeViews(2, mMenuView.getChildCount()-2);
        mMenu = new Menu(
                context,
                mMenuView,
                new MenuRowFactory(context),
                new Menu.OnMenuVisibilityChangeListener() {
                    @Override
                    public void onMenuVisibilityChange(boolean visible) {
                        //
                    }
                },new OnAutoHideListener(){

                    @Override
                    public void onAutoHide() {
                        // TODO Auto-generated method stub
                        setVisibility(View.INVISIBLE);
                    }

                });
        mMtkTvBroadcastBase = new MtkTvBroadcastBase();
        //initSoundStyleBroadcastReceiver();
//		ComponentStatusListener.getInstance().addListener(
//				ComponentStatusListener.NAV_LANGUAGE_CHANGED, this);
    }

    @Override
    public boolean isCoExist(int componentID) {
        return componentID == this.componentID;
    }

    @Override
    public boolean isKeyHandler(int keyCode) {
        Log.d(TAG, "isKeyHandler,keyCode=" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                 long currentTime = SystemClock.uptimeMillis();
                 if(currentTime - lastMenuKeyTime >INTERVAL_TIME){
                     lastMenuKeyTime = currentTime;
                     return true;
                 }else {
                     return false;
                 }

            default:
            break;
        }
        return false;
    }

    @Override
    public void setVisibility(int visibility) {
        if(View.VISIBLE == visibility) {
            if(mMenu.isActive()){
                super.setVisibility(View.INVISIBLE);
                mMenu.hide(true);
                transferExternalUIStatus(false);
                /*if(headReceiver != null){
                    mContext.unregisterReceiver(headReceiver);
                    headReceiver = null;
                    Log.d(TAG, "unregisterReceiver=111");
                }*/
            }else {
                //Log.d(TAG, "registHeadsetReciver"+headReceiver);
                super.setVisibility(visibility);
                mMenu.show(Menu.REASON_LAUNCH_TV_OPTIONS);
                transferExternalUIStatus(true);
                //registHeadsetReciver();
            }
        }
        else {
            super.setVisibility(visibility);
            mMenu.hide(true);
            transferExternalUIStatus(false);
            /*if(headReceiver != null){
                mContext.unregisterReceiver(headReceiver);
                headReceiver = null;
                Log.d(TAG, "unregisterReceiver=222");
            }*/
        }
    }

    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyHandler,keyCode=" + keyCode);
        mMenu.scheduleHide();
        if(event != null){//for from native no used key
            mMenuView.dispatchKeyEvent(event);
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            setVisibility(View.INVISIBLE);
            return true;
            case KeyEvent.KEYCODE_CHANNEL_UP:
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
            setVisibility(View.INVISIBLE);
            break;
            case KeyMap.KEYCODE_MTKIR_MTKIR_TTX:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_PAGE_DOWN:
            return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            //getMainActivity().getTvOptionsManager().set
            setVisibility(View.INVISIBLE);
            return true;

            default:
            break;
        }
        return super.onKeyHandler(keyCode, event, false);
    }
    
    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
        Log.d(TAG, "onKeyHandler keyCode = " + keyCode+",event = "+event+" ,fromNative="+fromNative);
        return onKeyHandler(keyCode, event);
    }

    public String getOptionString(int option) {
        return "";
    }

    public void notifyOptionChanged(int option) {
        OptionChangedListener listener = mOptionChangedListeners.get(option);
        if (listener != null) {
            listener.onOptionChanged(getOptionString(option));
        }
    }

    public void setOptionChangedListener(int option, OptionChangedListener listener) {
        mOptionChangedListeners.put(option, listener);
    }

    public boolean isShowing() {
        return mMenu.isActive();
    }

    /**
     * An interface used to monitor option changes.
     */
    public interface OptionChangedListener {
        void onOptionChanged(String newOption);
    }

	@Override
	public void updateComponentStatus(int statusID, int value) {
		// TODO Auto-generated method stub
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus statusID =" + statusID
				+ ">>value=" + value);
		// TODO Auto-generated method stub
		switch (statusID) {
			case ComponentStatusListener.NAV_LANGUAGE_CHANGED:
				mMenu.updateLanguage();
				break;
			default:
				break;
		}
	}

	private void transferExternalUIStatus(boolean isShow) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "transferExternalUIStatus||isShow =" + isShow);
        if (mMtkTvBroadcastBase != null) {
	    	 mMtkTvBroadcastBase.transferExternalUIStatus(
	    			 new MtkTvExternalUIStatusBase(MtkTvExternalUIStatusBase.EXTERNAL_UI_ID_MENU,isShow));
		}
	}

    /*private void registHeadsetReciver (){
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        if(headReceiver == null){
            initSoundStyleBroadcastReceiver();
        }
        mContext.registerReceiver(headReceiver,mFilter);
        mContext.sendBroadcast(new Intent(
                "android.intent.action.HEADSET_PLUG"));
        Log.d(TAG, "registHeadsetReciver11:"+headReceiver);

    }*/

    /* private void  initSoundStyleBroadcastReceiver(){
           headReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 Log.d(TAG, "registHeadsetReciver  onReceive");
                 String action = intent.getAction();
                 if (action != null && "android.intent.action.HEADSET_PLUG".equals(action)) {
                     if (intent.hasExtra("state")) {
                         Log.d(TAG, "onReceive-----true:");
                         notifyOptionChanged(OPTION_SOUND_STYLE);
                         if (intent.getIntExtra("state", 0) == 0) {//no connected
                             Log.d(TAG, "onReceive-----no connected:");
                         }else if (intent.getIntExtra("state", 0) == 1) {//connected
                             Log.d(TAG, "onReceive-----connected:");
                         }
                     }
                 }
             }
         };
    }*/

}