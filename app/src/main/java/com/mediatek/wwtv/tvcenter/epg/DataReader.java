package com.mediatek.wwtv.tvcenter.epg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ListView;

import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.twoworlds.tv.MtkTvEvent;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvEventInfo;
import com.mediatek.twoworlds.tv.model.MtkTvEventInfoBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGTypeListAdapter;
import com.mediatek.wwtv.tvcenter.epg.eu.EpgType;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGTypeListAdapter.EPGListViewDataItem;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;

import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;
import com.mediatek.wwtv.tvcenter.util.tif.TIFProgramInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFProgramManager;
import com.mediatek.wwtv.tvcenter.util.SaveValue;

public final class DataReader {
	private static final String TAG = "DataReader";
	public static final int PER_PAGE_CHANNEL_NUMBER = 6;
	private static DataReader dtReader;
	private List<MtkTvChannelInfoBase> tvChannelList;
	private EPGTimeConvert tmCvt;
    private CommonIntegration integration;
    private TIFChannelManager mTIFChannelManager;
    private TIFProgramManager mTIFProgramManager;
    private List<TIFChannelInfo> mTIFChannelInfoList;
    private static Map<Long, List<TIFProgramInfo>> mTIFProgramInfoMapList;
    private Context mContext;
	private String[][] sType;
	private String[] mType;
	private MtkTvEvent tvEvent;
  private String [] mMonthFull;
  private String [] mMonthSimple;
  private String [] mWeekFull;
  private String [] mWeekSimple;
	private DataReader(Context context) {
		mContext = context;
		tmCvt = EPGTimeConvert.getInstance();
		tvEvent = MtkTvEvent.getInstance();
		integration = CommonIntegration.getInstanceWithContext(context.getApplicationContext());
		mTIFChannelManager = TIFChannelManager.getInstance(mContext);
		mTIFProgramManager = TIFProgramManager.getInstance(mContext);
		loadProgramType();
		loadMonthAndWeekRes();
	}
	
	
	

	public static synchronized DataReader getInstance(Context context) {
//		if (dtReader == null) {
			dtReader = new DataReader(context.getApplicationContext());
//		}
		return dtReader;
	}
	

   public static synchronized DataReader getInstanceWithoutContext() {
        if (dtReader == null) {
            dtReader = new DataReader(DestroyApp.appContext);
        }
        return dtReader;
    }
   public String mapRating2CustomerStr(int ratingValue,String strRating) {
	   return integration.mapRating2CustomerStr(ratingValue,strRating);
   }

   /**
    * set EPG active window
    */
   public List<EPGChannelInfo> setActiveWindow(List<EPGChannelInfo> channels, int dayNum, int startHour) {
	   long startTime = tmCvt.setDate(EPGUtil.getCurrentDateDayAsMills(), dayNum, startHour);
	   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setActiveWindow>>startHour>" + dayNum + "  startHour>" + startHour + "   startTime>" + startTime);
	   for (EPGChannelInfo iiiii:channels) {
		   com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setActiveWindow>>" + iiiii.mId + "  " + iiiii.getTVChannel().getChannelId() + "  " + iiiii.getName());
	   }
	   if (CommonIntegration.isSARegion()) {
		   TIFFunctionUtil.setActivityWindow(tvChannelList, startTime);
	   } else {
		   List<MtkTvChannelInfoBase> tempApiChannelList = TIFFunctionUtil.getApiChannelListFromEpgChannel(channels);
		   TIFFunctionUtil.setActivityWindow(tempApiChannelList, startTime);
	   }
	   return channels;
   }
   
   /**
    * set EPG active window
    */
   public List<EPGChannelInfo> setActiveWindow(EPGChannelInfo channel, int dayNum, int startHour,int hourDurtion) {
	   List<EPGChannelInfo> channels=new ArrayList<EPGChannelInfo>();
	   channels.add(channel);
	   long durtion=hourDurtion*60L*60*1000;
	   long startTime = tmCvt.setDate(EPGUtil.getCurrentDateDayAsMills(), dayNum, startHour);
	   com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setActiveWindow>>dayNum>" + dayNum + "  startHour>" + startHour + "   startTime>" + startTime+",hourDurtion="+hourDurtion);
	   List<MtkTvChannelInfoBase> tempApiChannelList = TIFFunctionUtil.getApiChannelListFromEpgChannel(channels);
	   TIFFunctionUtil.setActivityWindow(tempApiChannelList, startTime,durtion);
	   return channels;
   }
   
   public long getStartTime(int dayNum, int startHour){
	   return tmCvt.setDate(EPGUtil.getCurrentDateDayAsMills(), dayNum, startHour);
   }

    public List<EPGProgramInfo> getProgramListByChId(EPGChannelInfo channelInfo,int dayNum,int startHour,int hourDurtion){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getProgramListByChId----->dayNum="+dayNum+",startHour="+startHour+",hourDurtion="+hourDurtion);
        long startTime = tmCvt.setDate(EPGUtil.getCurrentDateDayAsMills(), dayNum, startHour)*1000;
        long endTime=startTime+hourDurtion*60*60*1000;
        int mtkChId=-1;
        long chId=channelInfo.mId;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mtkChId="+mtkChId+",chId="+chId+",startTime="+startTime+",endTime="+endTime);
        if(channelInfo.getTVChannel()!=null){
            mtkChId=channelInfo.getTVChannel().getChannelId();
        }
        return mTIFProgramManager.queryProgramByChannelId(mtkChId,chId, startTime, endTime);
    }

   /**
    * read EPG with TIF programs
    */
   public List<EPGChannelInfo> readProgramInfoByTIF(List<EPGChannelInfo> channels, int dayNum, int startHour) {
	   long startTime = tmCvt.setDate(EPGUtil.getCurrentDateDayAsMills(), dayNum, startHour);
	   mTIFProgramInfoMapList = mTIFProgramManager.queryProgramListWithGroupByChannelId(channels, startTime);

        if(integration.is3rdTVSource()) {
            TIFFunctionUtil.getEpgChannelProgramsGroupEx(channels, mTIFProgramInfoMapList, startTime);
        }
        else {
            TIFFunctionUtil.getEpgChannelProgramsGroup(channels, mTIFProgramInfoMapList, startTime);
        }
	   return channels;
   }

   /**
	 *
	 * @param chList
	 *            channel list
	 * @param dayNum
	 *            [0-7]
	 * @param startTime
	 *            the duration start time
	 * @param mTimeSpan
	 *            the duration time
	 */
	public void readChannelProgramInfoByTime(List<EPGChannelInfo> chList, int dayNum, int startTime, int mTimeSpan) {
		long sTime = tmCvt.setDate(EPGUtil.getCurrentDateDayAsMills(), dayNum, startTime);
		int size = chList.size();
		MtkTvChannelInfoBase[] channels = new MtkTvChannelInfoBase[size];
		for (int i = 0; i < size; i++) {
			channels[i] = chList.get(i).getTVChannel();
		}
		int chIdx = 0;
		for (EPGChannelInfo child : chList) {
			long duration = tmCvt.getHourtoMsec(mTimeSpan);
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "set start time : [" + EPGUtil.getCurrentTime()+ "]duration>>" + duration);
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "set start time : [dayNum:" + dayNum+ "]"+"[startTime:" + sTime+ "]");
			List<EPGProgramInfo> mTVProgramInfo = new ArrayList<EPGProgramInfo>();
			mTVProgramInfo = readChannelProgramInfoByTime(channels[chIdx++], sTime, duration);
			child.setmTVProgramInfoList(mTVProgramInfo);
		}
	}

	/***
	 * connect to TVAPI MtkTvEvent.java
	 * List<MtkTvEventInfo>  MtkTvGetEventListByChannelId(int channelId, long startTime, long duration)
	 * @param chinfo
	 * @param startTime
	 * @param duration
	 * @return
	 */
	public List<EPGProgramInfo> readChannelProgramInfoByTime(MtkTvChannelInfoBase chinfo,
			long startTime, long duration) {
		List<EPGProgramInfo> mTVProgramInfoList = new ArrayList<EPGProgramInfo>();
//		List<MtkTvEventInfo> mTVEventList = new ArrayList<MtkTvEventInfo>();
//		evMonitor.setTime(startTime, duration);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- set time : [" + startTime + "," + duration + "]");
//		evMonitor.syncRead();
	    final MtkTvChannelInfoBase tvChannel = chinfo;
		 List<MtkTvEventInfoBase> mTVEventList = null;
		try {
		    tvEvent.setMaxEventNum(32);
		    mTVEventList = tvEvent.getEventListByChannelId(tvChannel.getChannelId(), startTime, duration);
		} catch (Exception e) {
        // TODO: handle exception
        e.printStackTrace();
    }

		if (mTVEventList != null) {
		    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "---mTVEventList get :" +mTVEventList.size());
			for (int i = 0; i < mTVEventList.size(); i++) {
				long proStartTime = mTVEventList.get(i).getStartTime();
				long proEndTime = proStartTime + mTVEventList.get(i).getDuration();
//				Date mStart = new Date(proStartTime);
//				Date mEnd = new Date(proEndTime);
				com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "+++++++ event guidance ++++++++++++" + mTVEventList.get(i).getChannelId() + "   " + mTVEventList.get(i).getEventId() + "   "+ mTVEventList.get(i).getGuidanceText() + "    " + mTVEventList.get(i).getDuration());
				com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "+++++++ event name ++++++++++++"+ mTVEventList.get(i).getEventTitle() + "   " + proStartTime + "   " + proEndTime);
				com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "+++++++ event detail ++++++++++++"+ mTVEventList.get(i).getEventDetail() + "     >>" + mTVEventList.get(i).getEventRating()
						+ "  extend detail:" + mTVEventList.get(i).getEventDetailExtend());
				int cat[] = mTVEventList.get(i).getEventCategory();
				String title = mTVEventList.get(i).getEventTitle();
				String guidence = mTVEventList.get(i).getGuidanceText();
				String detail = mTVEventList.get(i).getEventDetail();
				String extendDetail = mTVEventList.get(i).getEventDetailExtend();
				String resultDetail = getResultDetail(guidence, detail, extendDetail);
				title = integration.getAvailableString(title);
				resultDetail = integration.getAvailableString(resultDetail);
				EPGProgramInfo mTVprogramInfo = new EPGProgramInfo(proStartTime,
				        proEndTime, title==null || TextUtils.equals(title, "") ? mContext.getString(R.string.nav_epg_no_title):title,  resultDetail, 0,
						1, true);
				mTVprogramInfo.setHasSubTitle(mTVEventList.get(i).isCaption());
				mTVprogramInfo.setCategoryType(cat);
				mTVprogramInfo.setChannelId(tvChannel.getChannelId());
				mTVprogramInfo.setProgramId(mTVEventList.get(i).getEventId());
				mTVprogramInfo.setProgramBlock(tvEvent.checkEventBlock(tvChannel.getChannelId(), mTVEventList.get(i).getEventId()));
				com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "++++programblock++" + tvEvent.checkEventBlock(tvChannel.getChannelId(), mTVEventList.get(i).getEventId())
						+ "   " + mTVEventList.get(i).isCaption());
				setMStype(mTVprogramInfo, cat);
				mTVprogramInfo.setRatingType(mTVEventList.get(i).getEventRating());
				mTVprogramInfo.setRatingValue(mTVEventList.get(i).getEventRatingType());
				float width = getProWidth(mTVprogramInfo,  (MtkTvEventInfo)mTVEventList.get(i), startTime);
				mTVprogramInfo.setmScale(width);
				if (i == 0) {
					float mLeftMargin = tmCvt.countShowWidth(proStartTime,
							startTime);
					if (mLeftMargin > 0) {
						mTVprogramInfo.setLeftMargin(mLeftMargin);
					} else {
						mTVprogramInfo.setLeftMargin(0.0f);
					}

				} else {
					float mLeftMargin = getProLeftMargin(mTVprogramInfo,
					        (MtkTvEventInfo)mTVEventList.get(i - 1), (MtkTvEventInfo)mTVEventList.get(i));
					mTVprogramInfo.setLeftMargin(mLeftMargin);
					com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "+++++++ mLeftMargin ++++++++++++"
							+ mLeftMargin);
				}
				mTVProgramInfoList.add(mTVprogramInfo);
			}

		} else {
			EPGProgramInfo mTVprogramInfo = new EPGProgramInfo(startTime, startTime + duration, null, null, 0,
					1, true);
			mTVprogramInfo.setmScale(1.0f);
			mTVProgramInfoList.add(mTVprogramInfo);
		}

		return mTVProgramInfoList;
	}

	public String getResultDetail(String guidence, String detail, String extendDetail) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "guidence>>" + guidence+ ">>");
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "guidence222>>" + detail + ">>" + extendDetail + ">>>");
		String resultDetail = "";
		if (guidence == null) {
			guidence = "";
		}
		if (detail == null) {
			detail = "";
		}
		if (extendDetail == null) {
			extendDetail = "";
		}
		if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA) {
			if(!TextUtils.equals(guidence, "")) {
				resultDetail = guidence;
			}
			if (TextUtils.equals(resultDetail, "")) {
				resultDetail = detail;
			} else if (!TextUtils.equals(detail, "")) {
				resultDetail = resultDetail + "\n" + detail;
			}
		} else if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU) {
			if (!TextUtils.equals(guidence, "")) {
				resultDetail = guidence;
			}
			if (TextUtils.equals(resultDetail, "")) {
				resultDetail = detail;
			} else if (!TextUtils.equals(detail, "")) {
				resultDetail = resultDetail + "\n" + detail;
			}
			if (TextUtils.equals(resultDetail, "")) {
				resultDetail = extendDetail;
			} else if (!TextUtils.equals(extendDetail, "")) {
				resultDetail = resultDetail + "\n" + extendDetail;
			}
		}
		return resultDetail;
	}

	public float getProWidth(EPGProgramInfo epgProInfo, MtkTvEventInfoBase event,
			long startTime) {
		float width = 0.0f;
		long proStartTime = event.getStartTime();
		long proEndTime = proStartTime + event.getDuration();
		long duration = tmCvt.getHourtoMsec(EPGConfig.mTimeSpan);
		if (proStartTime < startTime && proEndTime > (startTime + duration)) {
			epgProInfo.setDrawLeftIcon(true);
			epgProInfo.setDrawRightIcon(true);
			width = 1.0f;
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setAdpter-----layoutParams.1---------------->" );
		} else if (proStartTime < startTime) {
			width = tmCvt.countShowWidth(proEndTime, startTime);
			epgProInfo.setDrawLeftIcon(true);
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setAdpter-----layoutParams.2---------------->" );
		} else if (proEndTime > (startTime + duration)) {
			width = tmCvt.countShowWidth(startTime + duration, proStartTime);
			epgProInfo.setDrawRightIcon(true);
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setAdpter-----layoutParams.3---------------->" );
		} else {
			width = tmCvt.countShowWidth(event.getDuration());
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setAdpter-----layoutParams.4---------------->"+event.getDuration() );
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " program width: " + "proEndTime:"+proEndTime+"_proEndTime:"+proEndTime
		        +"_startTime:"+startTime+"_width:"+width);
		return width;
	}

	public float getProWidth(EPGProgramInfo epgProInfo, long startTime, long duration) {
		float width = 0.0f;
		long proStartTime = epgProInfo.getmStartTime();
		long proEndTime = epgProInfo.getmEndTime();
		if (proStartTime < startTime && proEndTime > (startTime + duration)) {
			epgProInfo.setDrawLeftIcon(true);
			epgProInfo.setDrawRightIcon(true);
			width = 1.0f;
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setAdpter-----layoutParams.1---------------->" );
		} else if (proStartTime < startTime) {
			width = tmCvt.countShowWidth(proEndTime, startTime);
			epgProInfo.setDrawLeftIcon(true);
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setAdpter-----layoutParams.2---------------->" );
		} else if (proEndTime > (startTime + duration)) {
			width = tmCvt.countShowWidth(startTime + duration, proStartTime);
			epgProInfo.setDrawRightIcon(true);
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setAdpter-----layoutParams.3---------------->" );
		} else {
			width = tmCvt.countShowWidth(proEndTime - proStartTime);
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setAdpter-----layoutParams.4---------------->");
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " program width: " + "proEndTime:"+proEndTime+"_proEndTime:"+proEndTime
		        +"_startTime:"+startTime+"_width:"+width);
		return width;
	}

	public float getProLeftMargin(EPGProgramInfo mTVprogramInfo,
			MtkTvEventInfoBase preTvEvent, MtkTvEventInfoBase currentTvEvent) {
		float leftMargin = 0.0f;
		long startTime = preTvEvent.getStartTime() + preTvEvent.getDuration();
		long endTime = currentTvEvent.getStartTime();

		leftMargin = tmCvt.countShowWidth(endTime, startTime);
		return leftMargin;
	}

	public float getProLeftMargin(EPGProgramInfo mTVprogramInfo,
			EPGProgramInfo preTvEvent, EPGProgramInfo currentTvEvent) {
		float leftMargin = 0.0f;
		long startTime = preTvEvent.getmEndTime();
		long endTime = currentTvEvent.getmStartTime();

		leftMargin = tmCvt.countShowWidth(endTime, startTime);
		return leftMargin;
	}

	public List<EPGProgramInfo> getChannelProgramList(MtkTvChannelInfoBase channel, int dayNum, int startTime, int mTimeSpan) {
		if (channel != null) {
			long sTime = tmCvt.setDate(EPGUtil.getCurrentDateDayAsMills(), dayNum, startTime);
			long duration = tmCvt.getHourtoMsec(mTimeSpan);
			List<EPGProgramInfo> mTVProgramInfo = new ArrayList<EPGProgramInfo>();
			mTVProgramInfo = readChannelProgramInfoByTime(channel, sTime, duration);
			return mTVProgramInfo;
		}
		return null;
	}

	/********************************************************
	 *
	 * Channel function
	 *
	 **********************************************************/

    /**
     * getCurrentChannelList by type of EPGChannelInfo used for the UI
     *
     * @return
     */
	public List<EPGChannelInfo> getChannelList() {
		List<EPGChannelInfo> mChannelList = new ArrayList<EPGChannelInfo>();
		EPGChannelInfo chInfo = null;
		if (CommonIntegration.supportTIFFunction()) {
			mTIFChannelInfoList = mTIFChannelManager.getTIFPreOrNextChannelList(TIFFunctionUtil.getCurrentChannelId(), false, true, PER_PAGE_CHANNEL_NUMBER, TIFFunctionUtil.CH_LIST_EPG_MASK, TIFFunctionUtil.CH_LIST_EPG_VAL);
			tvChannelList = TIFFunctionUtil.getApiChannelList(mTIFChannelInfoList);
			if (mTIFChannelInfoList != null) {
				for (TIFChannelInfo tempTifChannel:mTIFChannelInfoList) {
					chInfo = new EPGChannelInfo(tempTifChannel);
					mChannelList.add(chInfo);
				}
			}
		} else {
			tvChannelList = integration.getChList(getCurrentChId(), 0, 6);
			if(null != tvChannelList){
				com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setAdapter.list."+tvChannelList.size());
        for (MtkTvChannelInfoBase mtkTvChannelInfo : tvChannelList) {
          chInfo = new EPGChannelInfo(mtkTvChannelInfo);
          mChannelList.add(chInfo);
        }
			}
		}
		return mChannelList;
	}

	/**
	 * update the channel list info
	 * @param oldList
	 * @param mNewList
	 */
	public void updateChannList(List<EPGChannelInfo> oldList, List<EPGChannelInfo> mNewList) {
		if (oldList != null && mNewList != null) {
			for (int i = 0; i < oldList.size(); i++) {
				EPGChannelInfo oldChannel = oldList.get(i);
				EPGChannelInfo newChannel = null;
				for (int j = 0; j < mNewList.size(); j++) {
					newChannel = mNewList.get(j);
					if (oldChannel.getTVChannel().getChannelId() == newChannel.getTVChannel().getChannelId()) {
						newChannel.setmTVProgramInfoList(oldChannel.getmTVProgramInfoList());
						oldList.set(i, newChannel);
						mNewList.remove(j);
						break;
					}
				}
			}
		}
	}

    /**
     * getallChannelList by type of EPGChannelInfo used for the UI
     *
     * @return
     */
		List<EPGChannelInfo> mChannelList = new ArrayList<EPGChannelInfo>();
		public List<EPGChannelInfo> getAllChannelList(boolean filterAnalog) {
			EPGChannelInfo chInfo = null;
			int length = integration.getAllEPGChannelLength();
			tvChannelList = integration.getChannelList(0, 0, length, integration.getChListFilterEPG());
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "epg get all channel list size:"+tvChannelList.size());
			if(null != tvChannelList){
			  for (MtkTvChannelInfoBase mtkTvChannelInfoBase : tvChannelList) {
			    if(filterAnalog&&mtkTvChannelInfoBase!=null&&mtkTvChannelInfoBase.isAnalogService()){
			      continue;
			    }
			    chInfo = new EPGChannelInfo(mtkTvChannelInfoBase);
			    mChannelList.add(chInfo);
          
        }
			}
			return mChannelList;
	  }
		
		public List<EPGChannelInfo> getAllChannelList() {
			return getAllChannelList(false);
		}

	/**
	 * get epg all channel list by tif
	 * @return
	 */
	public List<EPGChannelInfo> getAllChannelListByTIF(boolean filterAnalog) {
		List<EPGChannelInfo> mChannelList = new ArrayList<EPGChannelInfo>();
		mTIFChannelInfoList=new ArrayList<TIFChannelInfo>();
		EPGChannelInfo chInfo = null;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAllChannelListByTIF guanglei");

        List<TIFChannelInfo> tIFChannelInfoListAll  = CommonIntegration.getInstance().getChannelListForEPG();
        if(tIFChannelInfoListAll!=null){
            mTIFChannelInfoList.addAll(tIFChannelInfoListAll);
        }
        if(mTIFChannelInfoList != null){
            tvChannelList = TIFFunctionUtil.getApiChannelList(mTIFChannelInfoList);
            for (TIFChannelInfo tempTifChannel:mTIFChannelInfoList) {
                chInfo = new EPGChannelInfo(tempTifChannel);
                mChannelList.add(chInfo);
            }
        }
        return mChannelList;
    }

	public List<EPGChannelInfo> getAllChannelListByTIF() {
		return getAllChannelListByTIF(true);
	}
	
	
	
	

	/**
	 * get the pre or next channel list form current channel
	 * @param nextPage
	 * @return
	 */
    public List<MtkTvChannelInfoBase> getChannelList(boolean nextPage){
        List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
        MtkTvChannelInfoBase chInfo = null;
//        if(mChannelAdapter != null && mChannelAdapter.getCount() >0){
            if(nextPage){
                chInfo = integration.getCurChInfo();//.getItem(mChannelAdapter.getCount()-1);
                if ( chInfo != null){
                    list = integration.getChList(chInfo.getChannelId()+1, 0, 6);
                }
            }else{
                chInfo = integration.getCurChInfo();
                if ( chInfo != null){
                    list = integration.getChList(chInfo.getChannelId(),  6,0 );
                }
                        //mChListCenter.getChannelListByFilter("", CURRENT_CHANNEL_TYPE, chInfo.getChannelId(), CHANNEL_LIST_PAGE_MAX-1, 1);
            }
//        }
        return list;
    }

    /**
     * get the pre or next channel list form current channel by TIF
     * @param nextPage
     * @return
     */
    public List<EPGChannelInfo> getChannelListByTIF(boolean nextPage){
    	List<EPGChannelInfo> mChannelList = new ArrayList<EPGChannelInfo>();
    	EPGChannelInfo chInfo = null;
    	mTIFChannelInfoList = mTIFChannelManager.getTIFPreOrNextChannelList(TIFFunctionUtil.getCurrentChannelId(), !nextPage, false, PER_PAGE_CHANNEL_NUMBER, TIFFunctionUtil.CH_LIST_EPG_MASK, TIFFunctionUtil.CH_LIST_EPG_VAL);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTIFChannelInfoList?>>>" + (mTIFChannelInfoList!=null?mTIFChannelInfoList.size():mTIFChannelInfoList));
    	tvChannelList = TIFFunctionUtil.getApiChannelList(mTIFChannelInfoList);
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tvChannelList?>>>" + (tvChannelList!=null?tvChannelList.size():tvChannelList));
        for (TIFChannelInfo tempTifChannel:mTIFChannelInfoList) {
            chInfo = new EPGChannelInfo(tempTifChannel);
            mChannelList.add(chInfo);
        }
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mChannelList?>>>" + mChannelList.size());
        return mChannelList;
    }

    public int getCurrentChId(){
        return integration.getCurrentChannelId();
    }

    /**
     * identify the channel used by channelInfo,
     * return channelInfo
     * @return
     */
    public MtkTvChannelInfoBase getCurrentPlayChannel() {
        MtkTvChannelInfoBase mChannel;
        mChannel = (MtkTvChannelInfoBase)integration.getCurChInfo();//MtkTvChannelList.getCurrentChannel();
        return mChannel;
    }

    /***
     *
     * @param channel
     * @return
     */
    public int getChannelPosition(MtkTvChannelInfoBase channel) {
        int position = 0;
        // channel in the collection location
        if(null != tvChannelList){
            position = tvChannelList.indexOf(channel);
        }
        if (position < 0) {
        	position = 0;
        }
        return position;
    }

    
    public int get3rdCurrentPosition() {
		int curChannelId = integration.getCurrentChannelId();
		List<EPGChannelInfo> listEPGChannelInfo = getAllChannelListByTIF();
		int position = 0;
		for (int i = 0; i < listEPGChannelInfo.size(); i++) {
			EPGChannelInfo info = listEPGChannelInfo.get(i);
			if (curChannelId == info.mId) {
				position = i;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "get3rdCurrentPositioncur>>>ChannelId="+curChannelId);
				break;
			}
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "get3rdCurrentPosition>>>position=" +position);
		return position;
	}

	public int getCurrentPlayChannelPosition() {
		int position = 0;
		if (integration.is3rdTVSource()) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "get3rdCurrentPosition is3rdTVSource");
			position = get3rdCurrentPosition();
		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "get3rdCurrentPosition getCurrentPlayChannel");
			position = getChannelPosition(getCurrentPlayChannel());
		}
		return position;
	}

    public boolean isChannelExit(int channelId) {
      return mTIFChannelManager.getAPIChannelInfoById(channelId) != null;
    }

    public EPGChannelInfo getChannelByChannelNum(short channelNum) {
        List<EPGChannelInfo> mChannelList = getChannelList();
        for (EPGChannelInfo child : mChannelList) {
            if (child.getmChanelNum() == channelNum){
              return child;
            }
        }
        return null;
    }

    public void selectChannel(MtkTvChannelInfoBase chInfo){
    	integration.selectChannelByInfo(chInfo);
    }

    public void selectChannelByTIF(long id) {
        mTIFChannelManager.selectChannelByTIFInfo(mTIFChannelManager.getTIFChannelInfoPLusByProviderId(id));
    }

    public boolean isTvSourceLock() {
    	int showFlag = MtkTvPWDDialog.getInstance().PWDShow();
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showFlag>>>" + showFlag);
//    	if (showFlag == 0) {
    		return integration.isMenuInputTvBlock();
//    	}
//    	return false;
    }

    public boolean isChannelBlocked(MtkTvChannelInfoBase chInfo){
    	if (chInfo != null) {
    		return chInfo.isBlock();
    	}
        return false;
    }

    public String getCurrentSubtitleLang(){
        return null;
    }

    /**
     * set program mainType and subType
     * @param program
     * @param categoryType
     */
    public void setMStype(EPGProgramInfo program, int categoryType[]) {
    	int index = categoryType[0];
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subIndex>>>" + index + "   " + program.getmTitle());
        switch (MarketRegionInfo.getCurrentMarketRegion()){
            case MarketRegionInfo.REGION_EU:
            String country = integration.getCountryCode();
            if(index < 0){
              program.setMainType(-1);
              program.setSubType(-1);
            }else {
                int mainIndex = (index & 0xf0) >> 4;
                if (mainIndex >= 0) {
                    if (country != null && country.equals(EpgType.COUNTRY_UK)) {
                        switch (mainIndex) {
                            case 6:
                                mainIndex = 3;
                                break;
                            case 7:
                            case 8:
                                mainIndex = 2;
                                break;
                            case 9:
                                mainIndex = 6;
                                break;
                            case 10:
                                mainIndex = 7;
                                break;
                            case 11:
                            case 13:
                            case 14:
                                mainIndex = 8;
                                break;
                            case 12:
                                mainIndex =9;
                                break;
                            case 15:
                                mainIndex = 10;
                                break;
                            default:
                                break;
                        }
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "UK mType.length>>>" + mType.length +",mainIndex>>>"+mainIndex);
                        if(mainIndex >= mType.length){
                            //according to spec, 0x0 is undefined content
                            mainIndex = 0;
                        }
                        program.setMainType(mainIndex);
                        program.setSubType(-1);
                    } else if (country != null && country.equals(EpgType.COUNTRY_AUS)) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "AUS mainIndex>>>" + mainIndex);
                        if(mainIndex >= mType.length){
                            //according to spec, 0x0 is undefined content
                            mainIndex = 0;
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "AUS mType.length>>>" + mType.length);
                        }
                        program.setMainType(mainIndex);
                        program.setSubType(-1);
                    } else {
                        switch (mainIndex) {
                            case 13:
                            case 14:
                                mainIndex = 13;//for mapping un-defined case, reserved for future used.
                                break;
                            case 15:
                                mainIndex = 14;//for mapping user deifined case.
                                break;
                            default:
                                break;
                        }
                        if (mainIndex >= 0 && mainIndex < mType.length) {
                            program.setMainType(mainIndex);
                            int subIndex = index & 0x0f;
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mainIndex>>>" + mainIndex + "  subIndex>>> " + subIndex);
                            if (sType[mainIndex] != null && subIndex >=0 && subIndex < sType[mainIndex].length) {
                                program.setSubType(subIndex);
                            } else {
                                program.setSubType(-1);
                            }
                        } else {
                            program.setMainType(-1);
                            program.setSubType(-1);
                        }
                    }
                } else {
                    program.setMainType(-1);
                    program.setSubType(-1);
                }
            }
            break;
		default:
			if (index < 0) {
				program.setMainType(-1);
				program.setSubType(-1);
			} else {
				int mainIndex = (index & 0xf0) >> 4;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mainIndex>>>" + mainIndex);
				if(mainIndex > 12 && MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA){
					mainIndex = mainIndex - 1;
				}
				if (mainIndex >= 0 && mainIndex < mType.length) {
					program.setMainType(mainIndex);
					int subIndex = (index & 0xf0) << 4;
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subIndex>>>"  + subIndex+",sType[mainIndex].length>>>"+sType[mainIndex].length);
					if (subIndex >=0 && subIndex < sType[mainIndex].length) {
						program.setSubType(subIndex);
					} else {
						program.setSubType(-1);
					}
				} else {
					program.setMainType(-1);
					program.setSubType(-1);
				}
			}
			break;
		}
    }

    public String[] getMainType() {
    	return mType;
    }

    public String[][] getSubType() {
    	return sType;
    }

    public void loadProgramType() {
		switch (MarketRegionInfo.getCurrentMarketRegion()) {
		case MarketRegionInfo.REGION_EU:
	    	sType = new String[15][];
	    	String country = integration.getCountryCode();
			if (country != null && country.equals(EpgType.COUNTRY_UK)) {
				mType = mContext.getResources().getStringArray(R.array.nav_epg_filter_type_UK);
			} else if (country != null && country.equals(EpgType.COUNTRY_AUS)) {
				sType = new String[14][];
				mType = mContext.getResources().getStringArray(R.array.nav_epg_filter_type_AUS);
			} else {
				mType = mContext.getResources().getStringArray(
						R.array.nav_epg_filter_type);
				sType[0] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_undefined);
				sType[1] = mContext.getResources().getStringArray(
						R.array.nav_epg_subtype_movie);
				sType[2] = mContext.getResources().getStringArray(
						R.array.nav_epg_subtype_news);
				sType[3] = mContext.getResources().getStringArray(
						R.array.nav_epg_subtype_show);
				sType[4] = mContext.getResources().getStringArray(
						R.array.nav_epg_subtype_sports);
				sType[5] = mContext.getResources().getStringArray(
						R.array.nav_epg_subtype_children);
				sType[6] = mContext.getResources().getStringArray(
						R.array.nav_epg_subtype_music);
				sType[7] = mContext.getResources().getStringArray(
						R.array.nav_epg_subtype_arts);
				sType[8] = mContext.getResources().getStringArray(
						R.array.nav_epg_subtype_social);
				sType[9] = mContext.getResources().getStringArray(
						R.array.nav_epg_subtype_education);
				sType[10] = mContext.getResources().getStringArray(
						R.array.nav_epg_subtype_leisure);
				sType[11] = mContext.getResources().getStringArray(
						R.array.nav_epg_subtype_special);
                sType[12] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_adult);
                sType[13] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_reserved_for_future_use);
                sType[14] = mContext.getResources().getStringArray(
                        R.array.nav_epg_subtype_user_defined);
			}
			break;
		case MarketRegionInfo.REGION_SA:
			sType = new String[15][];
			mType = mContext.getResources().getStringArray(
					R.array.nav_epg_filter_sa_type);
			sType[0] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_news);
			sType[1] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_sports);
			sType[2] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_information);
			sType[3] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_dramas);
			sType[4] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_music);
			sType[5] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_variety);
			sType[6] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_movie);
			sType[7] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_animation);
			sType[8] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_documentary);
			sType[9] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_theatre);
			sType[10] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_hobby_education);
			sType[11] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_weflare);
			sType[12] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_reserved);
			sType[13] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_extension);
			sType[14] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_other);
			break;
		case MarketRegionInfo.REGION_CN:
		default:
			sType = new String[11][];
			mType = mContext.getResources().getStringArray(
					R.array.nav_epg_filter_type_cn);
			sType[0] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_movie_cn);
			sType[1] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_news_cn);
			sType[2] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_show_cn);
			sType[3] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sports_cn);
			sType[4] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_children_cn);
			sType[5] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_music_cn);
			sType[6] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_arts_cn);
			sType[7] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_social_cn);
			sType[8] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_education_cn);
			sType[9] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_leisure_cn);
			sType[10] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_special_cn);
			break;
		}
    }

    public void cleanMStypeDB() {
    	if (mType == null) {
    		loadProgramType();
    	}
    	for (int i = 0; i < mType.length; i++) {
    		if (SaveValue.getInstance(mContext).readBooleanValue(mType[i], false)) {
    			SaveValue.getInstance(mContext).removekey(mType[i]);
    		}
    		if (sType[i] != null) {
    			for (int j = 0; j < sType[i].length; j++) {
    				if (SaveValue.getInstance(mContext).readBooleanValue(sType[i][j], false)) {
    					SaveValue.getInstance(mContext).removekey(sType[i][j]);
    				}
    			}
    		}
    	}
    }

    public void loadMonthAndWeekRes() {
    	mMonthFull = new String[12];
    	mMonthSimple = new String[12];
    	mWeekFull = new String[7];
    	mWeekSimple = new String[7];
    	mMonthFull[0] = mContext.getString(R.string.nav_epg_January);
    	mMonthFull[1] = mContext.getString(R.string.nav_epg_February);
    	mMonthFull[2] = mContext.getString(R.string.nav_epg_March);
    	mMonthFull[3] = mContext.getString(R.string.nav_epg_April);
    	mMonthFull[4] = mContext.getString(R.string.nav_epg_May);
    	mMonthFull[5] = mContext.getString(R.string.nav_epg_June);
    	mMonthFull[6] = mContext.getString(R.string.nav_epg_July);
    	mMonthFull[7] = mContext.getString(R.string.nav_epg_August);
    	mMonthFull[8] = mContext.getString(R.string.nav_epg_September);
    	mMonthFull[9] = mContext.getString(R.string.nav_epg_October);
    	mMonthFull[10] = mContext.getString(R.string.nav_epg_November);
    	mMonthFull[11] = mContext.getString(R.string.nav_epg_December);

    	mMonthSimple[0] = mContext.getString(R.string.nav_epg_Jan);
    	mMonthSimple[1] = mContext.getString(R.string.nav_epg_Feb);
    	mMonthSimple[2] = mContext.getString(R.string.nav_epg_Mar);
    	mMonthSimple[3] = mContext.getString(R.string.nav_epg_Apr);
    	mMonthSimple[4] = mContext.getString(R.string.nav_epg_may);
    	mMonthSimple[5] = mContext.getString(R.string.nav_epg_Jun);
    	mMonthSimple[6] = mContext.getString(R.string.nav_epg_Jul);
    	mMonthSimple[7] = mContext.getString(R.string.nav_epg_Aug);
    	mMonthSimple[8] = mContext.getString(R.string.nav_epg_Sep);
    	mMonthSimple[9] = mContext.getString(R.string.nav_epg_Oct);
    	mMonthSimple[10] = mContext.getString(R.string.nav_epg_Nov);
    	mMonthSimple[11] = mContext.getString(R.string.nav_epg_Dec);

    	mWeekFull[0] = mContext.getString(R.string.nav_epg_Sunday);
    	mWeekFull[1] = mContext.getString(R.string.nav_epg_Monday);
    	mWeekFull[2] = mContext.getString(R.string.nav_epg_Tuesday);
    	mWeekFull[3] = mContext.getString(R.string.nav_epg_Wednesday);
    	mWeekFull[4] = mContext.getString(R.string.nav_epg_Thursday);
    	mWeekFull[5] = mContext.getString(R.string.nav_epg_Friday);
    	mWeekFull[6] = mContext.getString(R.string.nav_epg_Saturday);

    	mWeekSimple[0] = mContext.getString(R.string.nav_epg_Sun);
    	mWeekSimple[1] = mContext.getString(R.string.nav_epg_Mon);
    	mWeekSimple[2] = mContext.getString(R.string.nav_epg_Tue);
    	mWeekSimple[3] = mContext.getString(R.string.nav_epg_Wed);
    	mWeekSimple[4] = mContext.getString(R.string.nav_epg_Thur);
    	mWeekSimple[5] = mContext.getString(R.string.nav_epg_Fri);
    	mWeekSimple[6] = mContext.getString(R.string.nav_epg_Sat);
    }

    public String[] getWeekFullArray() {
    	return mWeekFull;
    }

    public String[] getWeekSimpleArray() {
    	return mWeekSimple;
    }

    public String[] getMonthFullArray() {
    	return mMonthFull;
    }

    public String[] getMonthSimpleArray() {
    	return mMonthSimple;
    }


    public List<EPGListViewDataItem> loadEPGFilterTypeData(ListView mList, boolean isSubDisabled) {
      String[][] sType = null;
      String[] mType = null;
      String country = integration.getCountryCode();
      switch (MarketRegionInfo.getCurrentMarketRegion()) {
        case MarketRegionInfo.REGION_EU:
          sType = new String[15][20];
          if (country != null && country.equals(EpgType.COUNTRY_UK)) {
                  mType = mContext.getResources().getStringArray(R.array.nav_epg_filter_type_UK);
          } else if (country != null && country.equals(EpgType.COUNTRY_AUS)) {
              mType = mContext.getResources().getStringArray(R.array.nav_epg_filter_type_AUS);
          } else {
              mType = mContext.getResources().getStringArray(
                              R.array.nav_epg_filter_type);
              sType[0] = mContext.getResources().getStringArray(
                  R.array.nav_epg_subtype_undefined);
              sType[1] = mContext.getResources().getStringArray(
                  R.array.nav_epg_subtype_movie);
              sType[2] = mContext.getResources().getStringArray(
                  R.array.nav_epg_subtype_news);
              sType[3] = mContext.getResources().getStringArray(
                  R.array.nav_epg_subtype_show);
              sType[4] = mContext.getResources().getStringArray(
                  R.array.nav_epg_subtype_sports);
              sType[5] = mContext.getResources().getStringArray(
                  R.array.nav_epg_subtype_children);
              sType[6] = mContext.getResources().getStringArray(
                  R.array.nav_epg_subtype_music);
              sType[7] = mContext.getResources().getStringArray(
                  R.array.nav_epg_subtype_arts);
              sType[8] = mContext.getResources().getStringArray(
                  R.array.nav_epg_subtype_social);
              sType[9] = mContext.getResources().getStringArray(
                  R.array.nav_epg_subtype_education);
              sType[10] = mContext.getResources().getStringArray(
                  R.array.nav_epg_subtype_leisure);
              sType[11] = mContext.getResources().getStringArray(
                  R.array.nav_epg_subtype_special);
              sType[12] = mContext.getResources().getStringArray(
                      R.array.nav_epg_subtype_adult);
              sType[13] = mContext.getResources().getStringArray(
                      R.array.nav_epg_subtype_reserved_for_future_use);
              sType[14] = mContext.getResources().getStringArray(
                  R.array.nav_epg_subtype_user_defined);
            }
          break;
        case MarketRegionInfo.REGION_SA:
          sType = new String[15][];
          mType = mContext.getResources().getStringArray(
              R.array.nav_epg_filter_sa_type);
          sType[0] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_sa_news);
          sType[1] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_sa_sports);
          sType[2] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_information);
          sType[3] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_dramas);
          sType[4] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_music);
		  sType[5] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_variety);
		  sType[6] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_movie);
		  sType[7] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_animation);
          sType[8] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_sa_documentary);
          sType[9] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_sa_theatre);
          sType[10] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_sa_hobby_education);
          sType[11] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_sa_weflare);
          sType[12] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_sa_reserved);
          sType[13] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_extension);
		  sType[14] = mContext.getResources().getStringArray(
					R.array.nav_epg_subtype_sa_other);
          break;
        case MarketRegionInfo.REGION_CN:
        default:
          sType = new String[11][];
          mType = mContext.getResources().getStringArray(
              R.array.nav_epg_filter_type_cn);
          sType[0] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_movie_cn);
          sType[1] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_news_cn);
          sType[2] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_show_cn);
          sType[3] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_sports_cn);
          sType[4] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_children_cn);
          sType[5] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_music_cn);
          sType[6] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_arts_cn);
          sType[7] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_social_cn);
          sType[8] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_education_cn);
          sType[9] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_leisure_cn);
          sType[10] = mContext.getResources().getStringArray(
              R.array.nav_epg_subtype_special_cn);
          break;
        }
        
        ArrayList<EPGListViewDataItem> mDataGroup = new ArrayList<EPGListViewDataItem>();
  
        for (int i = 0; i < mType.length; i++) {
          EPGTypeListAdapter adapter = new EPGTypeListAdapter(mContext, mList, isSubDisabled);
          EPGListViewDataItem mTypeData = adapter.new EPGListViewDataItem(mType[i]);
          ArrayList<EPGListViewDataItem> mSubTypeData = new ArrayList<EPGListViewDataItem>();
          ArrayList<String> detailData = new ArrayList<String>();
          if(country != null && !country.equals(EpgType.COUNTRY_UK)
              && !country.equals(EpgType.COUNTRY_AUS)
              && !country.equals(EpgType.COUNTRY_NZL)){
            for (int j = 0; j < sType[i].length; j++) {
				EPGListViewDataItem sTypeData = adapter.new EPGListViewDataItem(sType[i][j]);
              detailData.add(sType[i][j]);
              mSubTypeData.add(sTypeData);
            }
          }
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mType:"+ i + ", "+mType[i]);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "detailData size:"+detailData.size());
          mTypeData.setDetail(detailData);
          mTypeData.setSubChildDataItem(mSubTypeData);
          mDataGroup.add(mTypeData);
        }
        return mDataGroup;
        
  }

    
}
