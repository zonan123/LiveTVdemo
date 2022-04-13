package com.android.tv.menu;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.util.Log;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;




public class RecordRowAdapter extends OptionsRowAdapter {
	private static final String TAG = "ChannelsRowAdapterNew";
	private Context mContext;
    boolean pvrSupport = false;
	boolean	tshiftSupport = false;
	boolean	pvrkeysupport=false;

	public RecordRowAdapter(Context context) {
		super(context);
		this.mContext = context;
	}

	@Override
	protected List<MenuAction> createActions() {
		//boolean pvrSupport = false;
		//boolean tshiftSupport = false;
		List<MenuAction> mActionList = new ArrayList<MenuAction>();
		if (MarketRegionInfo
				.isFunctionSupport(MarketRegionInfo.F_TIF_TIMESHIFT)
				&& DataSeparaterUtil.getInstance() != null 
				&& DataSeparaterUtil.getInstance().isSupportTShift()) {
			tshiftSupport = true;
		}

		if (MarketRegionInfo
				.isFunctionSupport(MarketRegionInfo.F_SET_RECORD_SETTING_SUPP)
				&& DataSeparaterUtil.getInstance() != null
				&& DataSeparaterUtil.getInstance().isSupportPvr()) {
			pvrSupport = true;
		}

        if(DataSeparaterUtil.getInstance() != null && DataSeparaterUtil.getInstance().isSupportPvrKey()){
                   pvrkeysupport = true;
               }

        if(pvrkeysupport){//&&DvrManager.getInstance() != null&&!DvrManager.getInstance().pvrIsRecording()) {
            mActionList.add(MenuAction.PVR_START_ACTION);
            setOptionChangedListener(MenuAction.PVR_START_ACTION);
        }

        if(pvrkeysupport) {
            mActionList.add(MenuAction.PVR_STOP_ACTION);
            setOptionChangedListener(MenuAction.PVR_STOP_ACTION);
        }

		if (pvrSupport) {
			mActionList.add(MenuAction.DVR_LIST_ACTION);
			setOptionChangedListener(MenuAction.DVR_LIST_ACTION);
		}

		if (pvrSupport || tshiftSupport) {
			mActionList.add(MenuAction.DEVICE_INFO_ACTION);
			setOptionChangedListener(MenuAction.DEVICE_INFO_ACTION);
		}

		 if(pvrSupport) {
			mActionList.add(MenuAction.SCHEDULE_LIST_ACTION);
			setOptionChangedListener(MenuAction.SCHEDULE_LIST_ACTION);
		 }

		if (tshiftSupport) {
			mActionList.add(MenuAction.TSHIFT_MODE_ACTION);
			setOptionChangedListener(MenuAction.TSHIFT_MODE_ACTION);
		}
		return mActionList;
	}

	@Override
	protected boolean updateActions() {
		// TODO Auto-generated method stub
		return updateScheduleItem();
	}

	private boolean updateScheduleItem() {
		boolean isAnalogTv = false;
       List<TIFChannelInfo> books = TIFChannelManager.getInstance(mContext).
                        getAllDTVTIFChannels();
		if (CommonIntegration.getInstance().isCurCHAnalog()
				|| CommonIntegration.getInstance().isCurrentSourceATV()||books.isEmpty()) {
			Log.d(TAG, "updateScheduleItem||false");
			MenuAction.setEnabled(MenuAction.SCHEDULE_LIST_ACTION, false);
		} else {
			isAnalogTv = CommonIntegration.getInstance().isCurrentSourceDTV()
					|| CommonIntegration.getInstance().isCurrentSourceTv();
			Log.d(TAG, "updateScheduleItem||isAnalogTv =" + isAnalogTv);
			MenuAction.setEnabled(MenuAction.SCHEDULE_LIST_ACTION, isAnalogTv);
		}
        if(pvrkeysupport){//  For no pvr key and stop key custemer

        if(DvrManager.getInstance() != null&&DvrManager.getInstance().pvrIsRecording()){
          // show stop menu when pvr is recording.
            int index = getActionIndex(
                    MenuAction.PVR_START_ACTION.getType());
            if( index >= 0){
                removeAction(index);
                notifyItemRemoved(index);
            }
            index = getActionIndex(//add it if not existed
                    MenuAction.PVR_STOP_ACTION.getType());
            if( index >= 0) {
                removeAction(index);
                notifyItemRemoved(index);
            }

            addAction(0,
            MenuAction.PVR_STOP_ACTION);

        }else{

            if( !CommonIntegration.getInstance().isCurrentSourceDTV()
               &&!CommonIntegration.getInstance().isCurrentSourceTv()){
            // remove start and stop menu when not recording and under 3rd source.
                int index = getActionIndex(
                        MenuAction.PVR_START_ACTION.getType());
                if(index >= 0){
                    removeAction(index);
                    notifyItemRemoved(index);
                }
                index = getActionIndex(//add it if not existed
                        MenuAction.PVR_STOP_ACTION.getType());
                if(index >= 0) {
                    removeAction(index);
                    notifyItemRemoved(index);
                }

            }else {// show start menu when not recording and under tv source.
                int index = getActionIndex(//add it if not existed
                        MenuAction.PVR_STOP_ACTION.getType());
                if(index >= 0) {
                    removeAction(index);
                    notifyItemRemoved(index);
                }
                if(getActionIndex(//add it if not existed
                    MenuAction.PVR_START_ACTION.getType()) < 0) {
                    addAction(0,
                    MenuAction.PVR_START_ACTION);
                    }
                }
            }
            
        }
		return true;
	}

	@Override
	protected void executeAction(int type) {
		Log.d(TAG, "executeBaseAction: type=" + type);
		switch (type) {
        case MenuOptionMain.OPTION_PVR_START:
            MenuAction.showStartPvr(mContext);
            break;
        case MenuOptionMain.OPTION_PVR_STOP:
            MenuAction.showStopPvr(mContext);
            break;
		case MenuOptionMain.OPTION_DVR_LIST:
			MenuAction.showRecordList();
			break;
		case MenuOptionMain.OPTION_DEVICE_INFO:
			MenuAction.showRecordDeviceInfo(mContext);
			break;
		case MenuOptionMain.OPTION_SCHEDULE_LIST:
			MenuAction.showScheduleList(mContext);
			break;
		case MenuOptionMain.OPTION_TSHIFT_MODE:
			MenuAction.showTShiftMode(mContext);
			break;
		default:
			break;
		}
	}

}
