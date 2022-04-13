
package com.mediatek.wwtv.tvcenter.dvr.ui;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Gravity;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;


import androidx.annotation.NonNull;

import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.dvr.controller.UImanager;
import com.mediatek.wwtv.tvcenter.dvr.manager.CoreHelper;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrConstant;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
/**
 *
 */
@SuppressLint("ViewConstructor")
public class BaseInfoBar extends PopupWindow {

    public Long mDefaultDuration = 10L * 1000;
    public Long mRefreshDuration = 1L * 1000;
    Handler handler = null;
    private static final HandlerThread ht  = new HandlerThread("HandlerThread"); ;
    public Context mContext;
//    private ScheduledExecutorService scheduExec;

//    private ScheduledFuture<?> scheduleFuture;
//    private final static ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(5);
    public BaseInfoBar(Context context, int layoutID) {
        super(LayoutInflater.from(context).inflate(layoutID, null),
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        initView();
        this.mContext = context;
        doSomething();
    }

    /**
     * @param context
     * @param layoutID
     * @param duration
     * @param i
     * @param j
     */
    public BaseInfoBar(Context context, int layoutID, Long duration,
            int width, int height) {
        super(LayoutInflater.from(context).inflate(layoutID, null), width,
                height);
        initView();

        this.mContext = context;
        setDuration(Long.valueOf(duration));
        doSomething();
    }

    static {
        ht.start();
    }
    /**
     * @param duration
     */
    public void setDuration(Long duration) {
        this.mDefaultDuration = duration;
    }

    public void show() {
        show(null);
    }

    public void show(BaseInfoBar bBar) {
      
        if (isShowing()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("BaseInfoBar","showing");
        } else {
            setLocation();
            initView();
        }
        startTimeTask(bBar);
    }

    /**
	 *
	 */
    protected void setLocation() {
        try {
            showAtLocation((View)TurnkeyUiMainActivity.getInstance().findViewById(CoreHelper.ROOT_VIEW),
                    Gravity.CENTER, 20, 20);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Init all views.
     * <p>
     * use <code> getContentView().findViewById(R.id.xxx); </code>
     */
    public void initView() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("baseinfobar","initview");
    }

    public void doSomething() { 
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("baseinfobar","dosomething");
        handler = new Handler(ht.getLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("BaseInfoBar","msg");
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void dismiss() {
        Activity activity = TurnkeyUiMainActivity.getInstance();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("dismiss", "dismiss()>" + isShowing() + ">>>"
                + (activity == null ? activity : activity.isFinishing()));
        if (isShowing() && activity != null && !activity.isFinishing()) {
        	super.dismiss();
        }
        stopTimerTask();
    }
    
    Runnable clearTask = new Runnable() {
        @Override
        public void run() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e("BaseInfoBar", "BaseInfoBar.this.dismiss() start....");
            boolean attentionShow = UImanager.showing;
            if (attentionShow) {
                if (DvrManager.getInstance() != null) {
                    DvrManager.getInstance().getTopHandler()
                    .sendEmptyMessage(DvrConstant.Dissmiss_Info_Bar);
                }
            } else {
                if (StateDvr.getInstance() != null
                        && StateDvr.getInstance().getStatePVRHandler() != null) {
                    StateDvr.getInstance().getStatePVRHandler()
                    .sendEmptyMessage(DvrConstant.Dissmiss_PVR_BigCtrlbar);
                }
                if (StateDvrFileList.getInstance() != null
                        && StateDvrFileList.getInstance().getHandler() != null) {
                    StateDvrFileList.getInstance().getHandler()
                    .sendEmptyMessage(DvrConstant.Dissmiss_PVR_BigCtrlbar);
                }
            }
        }
    };
    public void startTimeTask(final BaseInfoBar bBar) {
        ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(5);

        try {
            if (mDefaultDuration == null || mDefaultDuration < 0){
                mDefaultDuration = 5L * 1000;
                }

//            if (scheduleFuture != null){
//                scheduleFuture.cancel(true);
//                }

//            scheduleFuture = scheduExec
//                    .schedule(clearTask, mDefaultDuration, TimeUnit.MILLISECONDS);
            handler.postDelayed(clearTask,mDefaultDuration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopTimerTask() {
        try {
            handler.removeCallbacks(clearTask);
//            scheduleFuture.cancel(true);
//            clearTask.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
