package com.mediatek.wwtv.tvcenter.epg.us;

import android.content.Context;
import android.media.tv.TvContract;

import com.mediatek.twoworlds.tv.MtkTvEventATSC;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.model.MtkTvATSCChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvEventInfoBase;
import com.mediatek.twoworlds.tv.MtkTvBanner;
import com.mediatek.twoworlds.tv.model.MtkTvISDBChannelInfo;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.EPGUtil;

import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.tif.TIFProgramInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFProgramManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EPGUsManager {
	private static final String TAG = "EPGUsManager";
    private Context mContext;
    private static EPGUsManager epgUsManager;
    private EPGUsChannelManager channelManager;
    public static final int Message_Refresh_Time=1001;
    public static final int Message_Refresh_ListView=1002;
    public static final int Message_LoadData=1003;
    public static final int Message_ReFreshData=1004;
    public static final int Message_ShouldFinish=1005;
    public static final int PER_PAGE_NUM = 6;
    public static boolean requestComplete;
    private List<ListItemData> dataGroup;
    public static final int Message_CHANGE_CHANNEL = 1006;
    public synchronized static EPGUsManager getInstance(Context context){
    	if(epgUsManager == null) {
			epgUsManager = new EPGUsManager(context.getApplicationContext());
		}
        return epgUsManager ;
    }

    private EPGUsManager(Context context) {
        mContext = context;
        channelManager = EPGUsChannelManager.getInstance(mContext);
        channelManager.initChannelData();
        dataGroup = new ArrayList<ListItemData>();
    }

    /**
     * the time show on the right of the top
     */
    public String getTimeToShow(){
        return EPGUtil.formatCurrentTime(mContext);
    }

    public EPGProgressDialog loading(Context context,boolean load){
        EPGProgressDialog pDialog = new EPGProgressDialog(context,R.style.dialog);
//        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        pDialog.setView(null);
        EPGUtil.setPositon(-700, -400, pDialog);
        pDialog.show();
        return pDialog;
    }

    /**
     * Channel Manager
     */
    public boolean onLeftChannel(){
        return channelManager.preChannel();
    }

    public boolean onRightChannel(){
        return channelManager.nextChannel();
    }

    public void initChannels(){
        channelManager.initChannelData();
    }

    public boolean isCurChATV(){
    	boolean isATV = CommonIntegration.getInstance().isCurrentSourceATV();
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "isAnalogService:" + isATV);
        return isATV;
    }

    private String getChannelNumber(MtkTvChannelInfoBase mCurrentChannel){
        String value="";
        if(mCurrentChannel instanceof MtkTvATSCChannelInfo){
            MtkTvATSCChannelInfo tmpAtsc = (MtkTvATSCChannelInfo)mCurrentChannel;
            value = tmpAtsc.getMajorNum()+"-" +tmpAtsc.getMinorNum();
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "getChannelNumCur1===>"+value);

        }else if(mCurrentChannel instanceof MtkTvISDBChannelInfo){
            MtkTvISDBChannelInfo tmpIsdb = (MtkTvISDBChannelInfo)mCurrentChannel;
            value = tmpIsdb.getMajorNum()+"-"+tmpIsdb.getMinorNum();
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "getChannelNumCur2===>"+value);
        }else{
            value =  " "+mCurrentChannel.getChannelNumber();
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "getChannelNumCur3===>"+value);
        }
        return value+" ";
    }

    public int getChannelIdCur(){
        if (channelManager==null||channelManager.getChannelCurrent()==null) {
            return 1;
        }else {
            return channelManager.getChannelCurrent().getChannelId();
        }
    }

    public String getChannelNumCur(){
    	if (CommonIntegration.supportTIFFunction()) {
    		if (channelManager != null) {
    			return channelManager.getCurrentChannelNum();
    		} else {
    			return "";
    		}
    	} else {
    		if (channelManager==null||channelManager.getChannelCurrent()==null) {
    			return "";
    		}else {
    			return getChannelNumber(channelManager.getChannelCurrent());
    		}
    	}
    }

    public String getChannelNameCur(){
    	if (CommonIntegration.supportTIFFunction()) {
    		if (channelManager != null) {
    			return channelManager.getCurrentChannelName();
    		} else {
    			return "";
    		}
    	} else {
    		if (channelManager==null||channelManager.getChannelCurrent()==null) {
    			return "";
    		}else if(channelManager.getChannelCurrent().getServiceName()==null){
    			return "";
    		}else{
    			return channelManager.getChannelCurrent().getServiceName();
    		}
    	}
    }

    public String getChannelNumPre(){
    	if (CommonIntegration.supportTIFFunction()) {
    		if (channelManager != null) {
    			return channelManager.getPreChannelNum();
    		} else {
    			return "";
    		}
    	} else {
    		if (channelManager==null||channelManager.getChannelPrevious()==null) {
    			return "";
    		}else {
    			return getChannelNumber(channelManager.getChannelPrevious());
    		}
    	}
    }

    public String getChannelNumNext(){
    	if (CommonIntegration.supportTIFFunction()) {
    		if (channelManager != null) {
    			return channelManager.getNextChannelNum();
    		} else {
    			return "";
    		}
    	} else {
    		if (channelManager==null||channelManager.getChannelNext()==null) {
    			return "";
    		}else {
    			return getChannelNumber(channelManager.getChannelNext());
    		}
    	}
    }

    /***
     * load event data for current channel
     * @param startTime
     */
    public List<Integer> getDataGroup(long startTime, int countNum){
    	 List<Integer> requestList = new ArrayList<Integer>();
    	 if(channelManager==null){
    		 com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG,"channelManager==null");
    		 return requestList;
    	 }
        int[] requests = channelManager.loadProgramEvent(channelManager.getCurrentChId(),startTime, countNum);
//        int i_ret = MtkTvEventATSC.getInstance().loadEvents(5243136,0,10, requests);
        if (requests != null) {
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getDataGroup:returnhandle:"+requests + "     " + requests.length);
        	if (requests!=null) {
        	  for (int i : requests) {
              if (i != 0) {
                requestList.add(i);
              }
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getDataGroup:returnhandle:" + i);
            }
        	}
        }
        return requestList;
    }

    public List<ListItemData> getDataGroup(){
        if (CommonIntegration.getInstance().is3rdTVSource()) {
            List<ListItemData> tmp = loadEvents();
            if(tmp != null) {
                dataGroup = tmp;
            }
        }
        return dataGroup;
    }

    public synchronized void addDataGroupItem(ListItemData itemData){
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "addDataGroupItem>>" + itemData);
    	if (dataGroup != null) {
    		int size = dataGroup.size();
			for (int i = 0; i < size; i++) {
				if (dataGroup.get(i).getEventId() == itemData.getEventId()) {
					dataGroup.remove(i);
					break;
				}
			}
			size = dataGroup.size();
    		if (size == 0 || itemData.getMillsStartTime() > dataGroup.get(size - 1).getMillsStartTime()) {
    			dataGroup.add(itemData);
    		} else {
    			List<ListItemData> preList = new ArrayList<ListItemData>();
    			List<ListItemData> nextList = new ArrayList<ListItemData>();
    			for (int i = 0; i < size; i++) {
    				if (itemData.getMillsStartTime() < dataGroup.get(i).getMillsStartTime()) {
    					preList.add(itemData);
    					for (int j = i; j < size; j++) {
    						nextList.add(dataGroup.get(j));
    					}
    					break;
    				} else {
    					preList.add(dataGroup.get(i));
    				}
    			}
    			dataGroup.clear();
    			dataGroup.addAll(preList);
    			dataGroup.addAll(nextList);
    		}
    	}
    }

    public synchronized void updateDataGroup(ListItemData itemData, int eventId) {
    	if (dataGroup == null) {
    		return;
    	}
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateDataGroup>>" + itemData + "  " + eventId);
    	int size = dataGroup.size();
    	boolean updateFlag = false;
    	for (int i = 0; i < size; i++) {
			if (dataGroup.get(i).getEventId() == eventId) {
				updateFlag = true;
				if (itemData != null) {
					dataGroup.set(i, itemData);
				} else {
					dataGroup.remove(i);
				}
				break;
			}
		}
    	if (!updateFlag && itemData != null) {
    		if (dataGroup.isEmpty()) {
    			dataGroup.add(itemData);
        	} else if (!containsEvent(itemData)) {
        		List<ListItemData> preList = new ArrayList<ListItemData>();
        		List<ListItemData> nextList = new ArrayList<ListItemData>();
        		for (int i = 0; i < size; i++) {
    				if (itemData.getMillsStartTime() < dataGroup.get(i).getMillsStartTime()) {
    					preList.add(itemData);
    					for (int j = i; j < size; j++) {
    						nextList.add(dataGroup.get(j));
    					}
    					break;
    				} else {
    					preList.add(dataGroup.get(i));
    				}
    			}
        		dataGroup.clear();
        		dataGroup.addAll(preList);
        		dataGroup.addAll(nextList);
        	}
    	}
    }

    private boolean containsEvent(ListItemData itemData) {
    	if (dataGroup == null) {
    		return false;
    	}
    	int size = dataGroup.size();
    	for (int i = 0; i < size; i++) {
			if (dataGroup.get(i).getEventId() == itemData.getEventId()) {
				return true;
			}
		}
    	return false;
    }

    public boolean clearEvent(int eventId) {
    	if (dataGroup == null) {
    		return false;
    	}
    	int size = dataGroup.size();
    	for (int i = 0; i < size; i++) {
			if (dataGroup.get(i).getEventId() == eventId) {
				dataGroup.remove(i);
				return true;
			}
		}
    	return false;
    }

    public void clearDataGroup(){
    	if (dataGroup != null) {
    		dataGroup.clear();
    	}
    }

    public int groupSize() {
    	if (dataGroup != null) {
    		return dataGroup.size();
    	}
    	return 0;
    }

    public MtkTvEventInfoBase getEvent(int requestId){
    	if (channelManager != null) {
    		return channelManager.getProgramInfo(requestId);
    	}
    	return null;
    }

    public ListItemData getEventItem(int requestId){
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getEventItem>>" + requestId);
        ListItemData itemData = null;
        MtkTvEventInfoBase programInfo= getEvent(requestId);
        // avoid anr
        // if (programInfo != null) {
        // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "programInfo>>>" + programInfo.getChannelId() + "    " +
        // programInfo.getEventId() + "    " + programInfo.getEventTitle() + "   "
        // + programInfo.getStartTime() + "    " + programInfo.getDuration() + "    " +
        // programInfo.getEventDetail()
        // + "  " + programInfo.getEventRating());
        // } else {
        // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "programInfo>programInfo>>" + programInfo);
        // }
        if (programInfo == null || programInfo.getStartTime() < 0) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "programInfo == null || programInfo.getStartTime() < 0.");
        }else {
            String eventRating = programInfo.getEventRating();
            String eventDetail = programInfo.getEventDetail();
            String eventTitle = programInfo.getEventTitle();
            itemData = new ListItemData();
            itemData.setEventId(requestId);
            itemData.setItemId(programInfo.getChannelId());
//          itemData.setItemDay(EPGUtil.getTodayOrTomorrow(programInfo.getStartTime(), startTime));
          itemData.setItemTime(EPGUtil.getWeekDayofTime(programInfo.getStartTime(),EPGUtil.getCurrentDayStartTime()));
            // avoid anr
            // com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getEventItem>>   rating>>" + programInfo.getEventRating() + "  " +
            // MtkTvEventATSC.getInstance().checkEventBlock(requestId)
            // + "   " + MtkTvEventATSC.getInstance().checkEventBlock(requestId,
            // channelManager.getCurrentChId()));
            String title = (eventTitle == null || eventTitle.equals("")) ? mContext
                    .getString(R.string.nav_epg_no_program_title) : eventTitle;
//          itemData.setProgramTime(EPGUtil.getProTime(programInfo.getStartTime(), programInfo.getDuration()));
//          itemData.setProgramStartTime(EPGUtil.formatStartTime(programInfo.getStartTime()));
            // fix CR: TDV00583320
            // avoid anr
            itemData.setItemProgramType((eventRating == null || eventRating
                    .equals("")) ? mContext.getString(R.string.nav_epg_not_rated) : eventRating);
            itemData.setMillsStartTime(programInfo.getStartTime());
            itemData.setMillsDurationTime(programInfo.getDuration());
            itemData.setItemProgramDetail((eventDetail == null || eventDetail.equals("")) ? mContext
                    .getString(R.string.nav_epg_no_program_detail)
                    : eventDetail);
            itemData.setItemProgramName(title);
            itemData.setCC(programInfo.isCaption());// ///////cc
            // itemData.setBlocked(false);//////////blocked
            itemData.setBlocked(MtkTvEventATSC.getInstance().checkEventBlock(requestId,
                    channelManager.getCurrentChId()));
            // avoid anr
            // com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Epg getEventItem:"+programInfo.getEventTitle() + "  " +
            // programInfo.isCaption() + "  " +
            // MtkTvEventATSC.getInstance().checkEventBlock(requestId));
        }
        return itemData;
    }

    public ListItemData getEventItemD(int value){
        ListItemData itemData = new ListItemData();
         itemData.setItemId(value);
//          itemData.setItemDay(EPGUtil.getTodayOrTomorrow(programInfo.getStartTime(), startTime));
          itemData.setItemTime(value+"");
          String title = "No program."+value;
          itemData.setItemProgramName(title);
          itemData.setCC(true);/////////cc
          itemData.setBlocked(false);//////////blocked

        return itemData;
    }

    public ListItemData getNoProItem(){
        ListItemData itemData =  new ListItemData();
//            itemData.setItemId(programInfo.getChannelId());
//          itemData.setItemDay(EPGUtil.getTodayOrTomorrow(programInfo.getStartTime(), startTime));
//          itemData.setItemTime(EPGUtil.formatStartTime(programInfo.getStartTime()));
          String title = mContext.getString(R.string.nav_epg_no_program_title);
          itemData.setItemProgramName(title);
          itemData.setProgramStartTime("");
        if (MtkTvBanner.getInstance().isDisplayCaptionIcon()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setCC(true)!!!");
            itemData.setCC(true);// cc show
        } else {
            itemData.setCC(false);// cc dismiss
        }
          itemData.setBlocked(false);//////////blocked
          itemData.setValid(false);

        return itemData;
    }

	public void clearData() {
		clearDataGroup();
        if(epgUsManager != null){
            epgUsManager = null;
        }
	}

    private List<ListItemData> loadEvents() {
        if (!CommonIntegration.supportTIFFunction()) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadEvents, supportTIFFunction");
            return null;
        }

        if (!CommonIntegration.getInstance().is3rdTVSource()) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadEvents, is3rdTVSource");
            return null;
        }

        long currntChId = TurnkeyUiMainActivity.getInstance().getTvView().getCurrentChannelId();
		if(currntChId == -1) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadEvents, currntChId");
            return null;
		}
		//
        MtkTvTimeFormatBase tvTime = MtkTvTime.getInstance().getLocalTime();
		long startTime = (tvTime.toMillis() - (tvTime.minute * 60) - tvTime.second) * 1000;
		long endTime = startTime + 24 * 60 * 60 * 1000;
		String selection = TvContract.Programs.COLUMN_CHANNEL_ID + " in " +
			"(" + currntChId + ")" + " and " + TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS + " > ? and "
					+ TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS + " < ?";
		String[] selectionArgs = { String.valueOf(startTime), String.valueOf(endTime)};

        Map<Long, List<TIFProgramInfo>> maps =
            TIFProgramManager.getInstance(mContext).queryProgramListWithGroupCondition(
            null, selection, selectionArgs, null);

		if(maps == null) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadEvents, maps");
			android.util.Log.d(TAG, "loadEvents, maps");
            return null;
		}

		List<TIFProgramInfo> tempProgramList = maps.get(currntChId);
		if(tempProgramList == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "loadEvents, tempProgramList, " +
                selection + "," + startTime + "," + endTime);
            return null;
        }
		List<ListItemData> itemData = new ArrayList<ListItemData>();

		for(TIFProgramInfo info : tempProgramList) {
			android.util.Log.d(TAG, "loadEvents, TIFProgramInfo " + info.mTitle);
			ListItemData data = new ListItemData();
          	data.setEventId(info.mEventId);
			data.setItemProgramName(info.mTitle);
          	data.setMillsStartTime(info.mStartTimeUtcSec);
          	data.setMillsDurationTime(info.mEndTimeUtcSec - info.mStartTimeUtcSec);
          	data.setItemProgramDetail(info.mDescription + info.mLongDescription);
          	data.setCC(false);
          	data.setBlocked(false);
          	data.setValid(false);
			itemData.add(data);
		}

		return itemData;
    }

}
