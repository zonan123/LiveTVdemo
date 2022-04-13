package com.mediatek.wwtv.tvcenter.epg.sa;

import java.util.List;

import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.wwtv.tvcenter.epg.EPGUtil;
import com.mediatek.wwtv.tvcenter.epg.sa.db.DBMgrProgramList;
import com.mediatek.wwtv.tvcenter.epg.sa.db.EPGBookListViewDataItem;

import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

/**
 * EPG Alarm program
 * @author sin_xinsheng
 *
 */
public class AlarmReceiver extends BroadcastReceiver{
	private static String TAG = "AlarmReceiver";
	private static String SA_EOG_DIALOG_ACTION = "com.mediatek.wwtv.tvcenter.saepg.activity";
	//private TurnkeyCommDialog mBookProgramConfirmDialog;
	private Context mContext;
	private EPGBookListViewDataItem tempInfo;
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
            int curChannelId = CommonIntegration
                .getInstanceWithContext(mContext.getApplicationContext()).getCurrentChannelId();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "curChannelId: "+curChannelId);
            if(curChannelId == tempInfo.mChannelId &&
                    CommonIntegration.getInstance().isCurrentSourceTv()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "need not to change channel");
                return;
            }
			if (tempInfo != null && tempInfo.mChannelId != 0) {
				Intent t = new Intent(SA_EOG_DIALOG_ACTION);
				t.putExtra("currentMills", tempInfo.mProgramStartTime);
				t.putExtra("programname", tempInfo.mProgramName);
				t.putExtra("channelid", tempInfo.mChannelId);
				t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(t);
			}
			super.handleMessage(msg);
		}

	};

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		String action = intent.getAction();
		if (action.equals(AlarmMgr.ALARM_EPG_ACTION)) {
			long currentMills = EPGUtil.getCurrentTime();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentMills: " + currentMills);
			MtkTvTimeFormatBase mtkTvTimeFormatBase = MtkTvTime.getInstance().getBroadcastTime();
			mtkTvTimeFormatBase.set(currentMills);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentMills>>" + mtkTvTimeFormatBase.hour + "   "
                + mtkTvTimeFormatBase.minute + "   " + mtkTvTimeFormatBase.second);
			if (currentMills != 0) {
				DBMgrProgramList.getInstance(mContext).getWriteableDB();
				List<EPGBookListViewDataItem> tempList= DBMgrProgramList.getInstance(mContext).getProgramList();
				tempInfo = null;
				for (EPGBookListViewDataItem tempItem:tempList) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempItem>mProgramStartTime:" + tempItem.mProgramStartTime);
                    mtkTvTimeFormatBase.set(tempItem.mProgramStartTime);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mProgramStartTime>>" + mtkTvTimeFormatBase.hour + "   "
                            + mtkTvTimeFormatBase.minute + "   " + mtkTvTimeFormatBase.second);
                    if ((tempItem.mProgramStartTime >= currentMills &&
                            tempItem.mProgramStartTime - currentMills <=6)
                            || (currentMills > tempItem.mProgramStartTime &&
                            currentMills - tempItem.mProgramStartTime <=6)) {
						tempInfo = tempItem;
						DBMgrProgramList.getInstance(mContext).deleteProgram(tempItem);
						if (tempItem.mProgramStartTime >= currentMills) {
							mHandler.removeMessages(0);
                            mHandler.sendEmptyMessageDelayed(0,
                                (tempItem.mProgramStartTime - currentMills)*1000);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start time >= currentMills");
						} else {
							mHandler.sendEmptyMessageDelayed(0, 0);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start time < currentMills");
						}
						break;
					}
				}
				DBMgrProgramList.getInstance(mContext).closeDB();
//				if (tempInfo == null) {
//					tempInfo = new EPGBookListViewDataItem();
//					tempInfo.mChannelId = 10;
//					tempInfo.mProgramName = "sfdas";
//				}
//				showBookConfirm(context);
			}
            scheduleAlarms(context);
		}
	}

    private void scheduleAlarms(Context context){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "scheduleAlarms");
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(AlarmMgr.ALARM_EPG_ACTION);
        intent.addFlags(0x01000000);//for fix DTV01982284
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,intent, 0);
        AlarmMgr.getInstance(context).cancelAlarm();
        mgr.set(AlarmManager.RTC, EPGUtil.getCurrentTime() * 1000 + 2*1000, pendingIntent);
    }


}
