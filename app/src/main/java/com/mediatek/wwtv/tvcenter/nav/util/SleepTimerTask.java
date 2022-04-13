package com.mediatek.wwtv.tvcenter.nav.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;

/**
 *
 * @author sin_biaoqinggao
 *
 */
public class SleepTimerTask{
    private final static String TAG = "SleepTimerTask";
	private final static  ScheduledThreadPoolExecutor mExec = new ScheduledThreadPoolExecutor(1);;
	private static  CustomTimerTask mTimerTask;
//	private static ScheduledFuture mCurrTask;
	private final static Map<String,ScheduledFuture> mTaskMap=new HashMap<String, ScheduledFuture>();
    private Context mContext;
	private long mills ;
	public static final int SHOW_DIALOG = 12;
	public static final int HIDE_DIALOG = 13;

	private static String hideId = "hideDialog";

	SleepTimerTipDialog tipDialog = null;
	private final Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case SHOW_DIALOG:
				String mItemId = (String)msg.obj;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d("SleepTimerTask", "sleep timer dialog show ");
				tipDialog = new SleepTimerTipDialog(mContext,mItemId);
				//tipDialog.setOwnerActivity((Activity)mContext);
				tipDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
				tipDialog.show();
                tipDialog.setPositon(-400, 300);
				break;
			case HIDE_DIALOG:
				com.mediatek.wwtv.tvcenter.util.MtkLog.d("SleepTimerTask", "sleep timer dialog hide ");
				if(tipDialog !=null){
					tipDialog.dismiss();
				}
				break;
				default:
				    break;
			}
			super.handleMessage(msg);
		}

	};

	public SleepTimerTask(Context context,Handler handler,long mills){
	  com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,"handler>>" + handler);
		mContext = context;
		this.mills = mills;
//		if (mExec == null) {
//			mExec = new ScheduledThreadPoolExecutor(1);
//		}
//		if (mTaskMap == null) {
//		  mTaskMap = ;
//		}
	}

	public SleepTimerTask(){
	    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,"SleepTimerTask");
	}

	public void doExec(String itemId) {
		if(mExec != null){
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("SleepTimerTask", "itemId is:"+itemId);
			ScheduledFuture task = mTaskMap.get(itemId);
			if(task != null){
				//cancel execute(if is running not interrupt)
				boolean ret = task.cancel(false);
				mExec.remove(mTimerTask);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d("SleepTimerTask", "task cancel " + ret);
			}else{
				com.mediatek.wwtv.tvcenter.util.MtkLog.d("SleepTimerTask", "task is null");
			}
			mTimerTask = new CustomTimerTask(itemId);
			task =  mExec.schedule(mTimerTask, mills, TimeUnit.MILLISECONDS);
			mTaskMap.put(itemId, task);
		}else{
			com.mediatek.wwtv.tvcenter.util.MtkLog.e("SleepTimerTask", "sorry Timer is null");
		}

	}

	public static void doCancelTask(String itemId){
		if(itemId.equals(MtkTvConfigType.CFG_MISC_AUTO_SLEEP)){
			MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_MISC_AUTO_SLEEP, 0);
		}else{
			//set sleeptimer to off
			MtkTvTime.getInstance().setSleepTimer(0);
			ScheduledFuture task = mTaskMap.get(itemId);
			if( task != null){
				//cancel execute(if is running not interrupt)
				boolean ret =  task.cancel(false);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d("SleepTimerTask", "sleep timer is canceled =="+ret);
				mTaskMap.put(itemId, null);
			}
			ScheduledFuture task2 = mTaskMap.get(hideId);
			if(task2 != null){
				boolean ret =  task2.cancel(false);
				com.mediatek.wwtv.tvcenter.util.MtkLog.d("SleepTimerTask", "sleep hide dialog task is canceled =="+ret);
				mTaskMap.put(hideId, null);
			}
		}

	}

	public void doHideDialogTask(){
		HideDialogTask mTimerTask = new HideDialogTask();
        long remainTime = MtkTvTime.getInstance().getSleepTimerRemainingTime();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "doHideDialogTask,remainTime =="+remainTime);
		ScheduledFuture task =  mExec.schedule(mTimerTask, 1000 * (remainTime - 10), TimeUnit.MILLISECONDS);
		mTaskMap.put(hideId, task);
	}

	/**
	 * shutDown pool
	 */
	public static void shutDown(){
		if(mExec != null){
			mExec.shutdown();
		}
	}

	private boolean haveRemainTime(){
		return (MtkTvTime.getInstance().getSleepTimerRemainingTime() > 0);
	}

	class CustomTimerTask implements Runnable{
		String mItemId;
		CustomTimerTask(String itemId){
			mItemId = itemId;
		}
		@Override
		public void run() {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("SleepTimerTask", "send sleep tip broadcast");
			if(mItemId.equals(MtkTvConfigType.CFG_MISC_AUTO_SLEEP)){
				int value = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_AUTO_SLEEP);
				if(value==0){
					com.mediatek.wwtv.tvcenter.util.MtkLog.d("SleepTimerTask", "auto sleep offed so don't send broadcast ");
				}else{
					showDialog(mItemId);
				}
			}else{
				if(haveRemainTime()){
					showDialog(mItemId);
				}else{
					com.mediatek.wwtv.tvcenter.util.MtkLog.d("SleepTimerTask", "sleep timer offed so don't send broadcast ");
				}
			}
		}

	}

	private void showDialog(String mItemID){
		Message msg = mHandler.obtainMessage();
		msg.what = SHOW_DIALOG;
		msg.obj = mItemID;
		mHandler.sendMessage(msg);
		doHideDialogTask();
	}

	class HideDialogTask implements Runnable{

		@Override
		public void run() {
			hideDialog();
		}

	}

	public void hideDialog(){
		Message msg = mHandler.obtainMessage();
		msg.what = HIDE_DIALOG;
		mHandler.sendMessage(msg);
	}

}
