package com.mediatek.wwtv.tvcenter.nav.util;

import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import androidx.annotation.NonNull;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.SparseArray;

import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ComponentStatusListener {
    private static final String TAG = "ComponentStatusListener";
//    private static final int STATUS_CHANGED = 1;

    private static ComponentStatusListener mCSListener = null;
    private static int mParam1 = 0;

    private Handler mHandler = null;
    private SparseArray<List<ICStatusListener>> mRigister;//local variables

    //please add your status ID here
    public static final int NAV_COMPONENT_HIDE  = 1;//its value is component id
    public static final int NAV_COMPONENT_SHOW  = 2;//its value is component id
    public static final int NAV_RESUME          = 3;//its value is 0
    public static final int NAV_PAUSE           = 4;//its value is 0
    public static final int NAV_KEY_OCCUR       = 5;//its value is key code
    public static final int NAV_ENTER_LANCHER   = 6;//its value is 0
    public static final int NAV_ENTER_MMP       = 7;//its value is 0
    public static final int NAV_ENTER_STANDBY   = 8;//its value is 0
    public static final int NAV_INPUT_SELECT    = 9;//its value is 0
    public static final int NAV_CHANNEL_CHANGED = 10;//its value is 0(success) or -1(fail);
    public static final int NAV_CONTENT_ALLOWED = 12;//its value is 0
    public static final int NAV_CONTENT_BLOCKED = 13;
    public static final int NAV_ENTER_ANDR_PIP  = 14;
    public static final int NAV_EXIT_ANDR_PIP   = 15;
    public static final int NAV_CHANNEL_RETURN  = 16;
    public static final int NAV_POWER_OFF       = 17;
    public static final int NAV_POWER_ON        = 18;
    public static final int NAV_SHUT_DOWN       = 19;
    public static final int NAV_LANGUAGE_CHANGED= 20;
    public static final int NAV_CH_CHANGE_BY_DIGITAL= 21;
    public static final int NAV_VGA_NO_SIGNAL= 22;
    public static final int NAV_INPUTS_PANEL_HIDE = 23;
    public static final int NAV_INPUTS_PANEL_SHOW = 24;
    public static final int NAV_INPUTS_PANEL_ENTER_KEY = 25;
    public static final int NAV_INPUTS_PANEL_SOURCE_KEY = 26;
    public static final int NAV_DVR_FILELIST_HIDE = 27;
    public static final int NAV_OAD_STATE       = 28;
    public static final int NAV_PVR_DIALOG_HIDE = 29;
    public static final int NAV_HOST_TUNE_STATUS= 30;

    public static final int MSG_DEBUG= 101;
    //end

    private ComponentStatusListener(){
        mRigister = new SparseArray<List<ICStatusListener>>();

        mHandler = new InternalHandler(this);
        mHandler.sendEmptyMessageDelayed(MSG_DEBUG, 30_000);
    }

    public static synchronized ComponentStatusListener getInstance() {
        if(mCSListener == null){
            mCSListener = new ComponentStatusListener();

        }
        return mCSListener;
    }

    public void updateStatus(int statusID){
        delayUpdateStatus(statusID, 0, 0);
    }

    public void updateStatus(int statusID, int value){
        delayUpdateStatus(statusID, value, 0);
    }

    public void delayUpdateStatus(int statusID, long delayMillis){
        delayUpdateStatus(statusID, 0, delayMillis);
    }

    public void delayUpdateStatus(int statusID, int value, long delayMillis){
        Message msg = Message.obtain();
        msg.what = statusID;
        msg.arg2 = value;

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateStatus, statusID=" + statusID + ",value=" + value + ",delayMillis=" + delayMillis);
        if(delayMillis > 0) {
            mHandler.sendMessageDelayed(msg, delayMillis);
        }
        else {
            mHandler.sendMessage(msg);
        }
    }

    public boolean addListener(int statusID, ICStatusListener listener){
        synchronized(ComponentStatusListener.class) {
            List<ICStatusListener> handlers = mRigister.get(statusID);

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ComponentStatusListener, key=" + statusID);
            if (handlers != null) {
                for (ICStatusListener handler : handlers) {
                    if (listener.equals(handler)) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addListener, already existed");
                        return false;//already existed
                    }
                }
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ComponentStatusListener, new ArrayList");
                handlers = new ArrayList<ICStatusListener>();
                mRigister.append(statusID, handlers);
            }

            return handlers.add(listener);
        }
    }

    public boolean removeListener(ICStatusListener listener){
        synchronized(ComponentStatusListener.class) {
            int size = mRigister.size();

            for (int i = 0; i < size; i++) {
                List<ICStatusListener> handlers = mRigister.valueAt(i);
                handlers.remove(listener);
            }

            return true;
        }
    }

    public boolean removeAll(){
        synchronized(ComponentStatusListener.class){
            Log.i(TAG, "removeAll MSG_DEBUG ");
            mHandler.removeCallbacksAndMessages(null);
            mRigister.clear();
            mCSListener = null;
        }
        return true;
    }

    public static int getParam1(){
        return mParam1;
    }

    public static void setParam1(int param){
        synchronized(ComponentStatusListener.class){
            mParam1 = param;
        }
    }

    public interface ICStatusListener{
      void updateComponentStatus(int statusID, int value);
    }

    /**
     * InternalHandler
     */
    private static class InternalHandler extends Handler{
        private final WeakReference<ComponentStatusListener> mDialog;
        private AtomicInteger mMsgCount = new AtomicInteger(0);
        private AtomicLong mLastLogTime = new AtomicLong(0L);
        public InternalHandler(ComponentStatusListener dialog){
            super(Looper.getMainLooper());
            mDialog = new WeakReference<ComponentStatusListener>(dialog);
        }

        public void handleMessage(Message msg){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "[InternalHandler] handlerMessage occur~" + msg.what);
            if(msg.what == MSG_DEBUG){
                Log.d(TAG, "MSG_DEBUG when=" + msg.getWhen() + " uptimeMillis =" + SystemClock.uptimeMillis());
                sendEmptyMessageDelayed(MSG_DEBUG, 30_000);
                return;
            }
            if(mDialog.get() == null){
                return ;
            }

            synchronized(ComponentStatusListener.class){
                List<ICStatusListener> handlers = mDialog.get().mRigister.get(msg.what);
                if(handlers != null){
                    for(ICStatusListener listener : handlers){
                        listener.updateComponentStatus(msg.what, msg.arg2);
                    }
                }

                if(msg.what == NAV_COMPONENT_SHOW &&
                	TurnkeyUiMainActivity.getInstance() != null &&
                    TurnkeyUiMainActivity.getInstance().isInPictureInPictureMode()) {
                    ComponentsManager.getInstance().hideAllComponents();
                }
            }
        }


        @Override public void dispatchMessage(@NonNull Message msg) {
            int count = mMsgCount.decrementAndGet();
            super.dispatchMessage(msg);
        }

        @Override public boolean sendMessageAtTime(@NonNull Message msg, long uptimeMillis) {
            boolean ret = super.sendMessageAtTime(msg, uptimeMillis);
            int count = mMsgCount.incrementAndGet();
            if(count > 100){
                long now = SystemClock.uptimeMillis();
                long lastPrint = mLastLogTime.get();
                if(now - lastPrint > 10_000){
                    mLastLogTime.set(now);
                    Exception e = new Exception("sendMessageAtTime MSG_DEBUG : there are " + count
                        + " messages hasn't beed handled msg=" + msg.toString()
                        + " uptimeMillis=" + SystemClock.uptimeMillis()
                    );
                    Log.w(TAG, e.getMessage(), e);
                }

            }
            return  ret;
        }
    }
}
