package com.mediatek.wwtv.tvcenter.dvr.controller;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Handler;

//import com.mediatek.wwtv.setting.util.Util;
import com.mediatek.wwtv.setting.widget.view.DiskSettingSubMenuDialog;

import com.mediatek.dm.DeviceManager;
import com.mediatek.dm.MountPoint;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrConstant;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.dvr.ui.DvrFilelist;
import com.mediatek.wwtv.tvcenter.dvr.ui.ScheduleListItemDialog;
import com.mediatek.wwtv.tvcenter.epg.EPGManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog.OnCancelClickListener;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog.OnConfirmClickListener;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog.OnConfirmResultClickListener;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;

public class RegistOnDvrDialog implements OnConfirmClickListener,
		OnCancelClickListener,OnConfirmResultClickListener {
	public static final String TAG = "RegistOnDvrDialog";
	public static final int TYPE_Record = 1;
	public static final int TYPE_GUIDE = 2;
	public static final int TYPE_DEVICE_FORMATE = 3;
	public static final int TYPE_SCHEDULE_RECORD = 4;
	public static final int TYPE_FVP_GUIDE=5;
    public static final int TYPE_DVR_FILE_DELET=6;
	private static final int TYPE_ChangeChanel_UP = 11235;
	private static final int TYPE_ChangeChanel_Down = 11236;
	private static final int TYPE_ChangeChanel_Pre = 11237;
	public static final int TYPE_Change_ChannelNum = 11238;

 static Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case TYPE_ChangeChanel_UP:
				removeMessages(TYPE_ChangeChanel_UP);
				CommonIntegration.getInstance().channelUp();
				// ComponentStatusListener.getInstance().updateStatus(
				// ComponentStatusListener.NAV_KEY_OCCUR, keyCode);
				break;
			case TYPE_ChangeChanel_Down:
				removeMessages(TYPE_ChangeChanel_Down);
				CommonIntegration.getInstance().channelDown();
				break;
			case TYPE_ChangeChanel_Pre:
				removeMessages(TYPE_ChangeChanel_Pre);
				CommonIntegration.getInstance().channelPre();
				break;
            default:
                break;
			}
		}
	};

	@Override
	public void onCancelClick(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case TYPE_SCHEDULE_RECORD:
			DvrManager.getInstance().handleRecordNTF(false);
			break;
        case TYPE_DVR_FILE_DELET:
            if (DvrManager.getInstance().setState(
                    StateDvrFileList.getInstance(DvrManager.getInstance()))) {
//                StateDvrFileList.getInstance().setFlag(true);
                StateDvrFileList.getInstance().showPVRlist();
//                StateDvrFileList.getInstance().setFlag(false);
            }
            break;
		default:
			break;
		}
	}

	@Override
	public void onConfirmClick(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case KeyMap.KEYCODE_MTKIR_CHUP:
		case KeyMap.KEYCODE_DPAD_UP:
			DvrManager.getInstance().stopAllRunning();
			StateDvr.getInstance().setChangeSource(true);
			mHandler.sendEmptyMessageDelayed(TYPE_ChangeChanel_UP, 1000);
			break;
		case KeyMap.KEYCODE_MTKIR_CHDN:
		case KeyMap.KEYCODE_DPAD_DOWN:
			DvrManager.getInstance().stopAllRunning();
			StateDvr.getInstance().setChangeSource(true);
			mHandler.sendEmptyMessageDelayed(TYPE_ChangeChanel_Down, 1000);
			break;
		case KeyMap.KEYCODE_MTKIR_PRECH:
			DvrManager.getInstance().stopAllRunning();
			StateDvr.getInstance().setChangeSource(true);
			mHandler.sendEmptyMessageDelayed(TYPE_ChangeChanel_Pre, 1000);
			break;
		case KeyMap.KEYCODE_MTKIR_GUIDE:
			DvrManager.getInstance().stopAllRunning();
			DvrManager.getInstance().getTopHandler()
					.removeMessages(DvrConstant.RECORD_FINISH_AND_SAVE);

			if (CommonIntegration.getInstance().isCurrentSourceTv()) {
				EPGManager.getInstance(TurnkeyUiMainActivity.getInstance())
						.startEpg(TurnkeyUiMainActivity.getInstance(),
								NavBasic.NAV_REQUEST_CODE);
				DvrManager.getInstance().getTopHandler()
						.removeMessages(DvrConstant.RECORD_FINISH_AND_SAVE);

			}
			break;
		case KeyMap.KEYCODE_MTKIR_RECORD:
			 if (TIFChannelManager.getInstance(TurnkeyUiMainActivity.getInstance()).
                     getAllDTVTIFChannels().size() <= 0) {
                 return ;
             }
			MtkTvBookingBase item = new MtkTvBookingBase();
			item.setEventTitle("1");
			item.setTunerType(CommonIntegration.getInstance().getTunerMode());
			long mStartTime = getBroadcastLocalTime();
			if (mStartTime != -1L) {
				item.setRecordStartTime(mStartTime);
			}
			item.setRecordDuration(2 * 60);
			item.setRepeatMode(128);
			if (!(DataSeparaterUtil.getInstance() != null && DataSeparaterUtil
					.getInstance().isSupportPvr())) {
				item.setRecordMode(0);
			}
			ScheduleListItemDialog scheduleListItemDialog = new ScheduleListItemDialog(
					TurnkeyUiMainActivity.getInstance(), item);
			scheduleListItemDialog.show();
			break;
		case TYPE_SCHEDULE_RECORD:
			DvrManager.getInstance().handleRecordNTF(true);
			break;
		case TYPE_FVP_GUIDE:
			DvrManager.getInstance().stopAllRunning();
			DvrManager.getInstance().getTopHandler()
					.removeMessages(DvrConstant.RECORD_FINISH_AND_SAVE);
			  com.mediatek.wwtv.tvcenter.util.KeyDispatch.getInstance().passKeyToNative(
	                    KeyMap.KEYCODE_MTKIR_GUIDE, null);
			break;
        case TYPE_DVR_FILE_DELET:
            SimpleDialog simpleDialog = (SimpleDialog)ComponentsManager.getInstance().
                    getComponentById(NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
            long deletid = simpleDialog.getBundle().getLong(DvrFilelist.DELET_DVR_ID_STRING);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"delet="+deletid);
			DvrManager.getInstance().getController().deletePvrFiles(TurnkeyUiMainActivity.getInstance(), deletid);
            //DvrManager.getInstance().getController()
            //.deletePvrFiles(TurnkeyUiMainActivity.getInstance(),deletid);
            if (DvrManager.getInstance().setState(
                        StateDvrFileList.getInstance(DvrManager.getInstance()))) {
//				StateDvrFileList.getInstance().delete(deletid);
                StateDvrFileList.getInstance().showPVRlist();
            }
            break;
        default:
            break;
		}
		if (id == 0 || id >= 1000) {
			ArrayList<MountPoint> deviceList = DeviceManager.getInstance()
					.getMountPointList();
			int selection = id / 1000;
			DiskSettingSubMenuDialog mSubSettingDialog = new DiskSettingSubMenuDialog(
					TurnkeyUiMainActivity.getInstance(),
					DiskSettingSubMenuDialog.UiType.FORMAT_ING,
					deviceList.get(selection));
			mSubSettingDialog.show();
		}
	}


    @Override
    public void onConfirmResultClick(int id) {
        switch (id) {
            case TYPE_SCHEDULE_RECORD:
                DvrManager.getInstance().handleRecordNTF(true);
                break;
            default:
                break;

        }


    }

	/**
	 * get time from broadcast for sync android time
	 * 
	 * @return
	 */
	private long getBroadcastLocalTime() {
		MtkTvTimeFormatBase mTime = MtkTvTime.getInstance()
				.getBroadcastLocalTime();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getBroadcastLocalTime == " + mTime.toSeconds());
		return mTime.toSeconds();
	}
}
