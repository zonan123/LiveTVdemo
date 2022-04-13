package com.mediatek.wwtv.tvcenter.util.tif;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvEvent;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvEventInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvEventComom;
import com.mediatek.twoworlds.tv.model.MtkTvEventGroupBase;
import com.mediatek.twoworlds.tv.model.MtkTvDvbChannelInfo;
import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.epg.EPGChannelInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGTimeConvert;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.R;

/**
 *
 * @author sin_xinsheng
 *
 */
public abstract class TIFFunctionUtil {
	private static final String TAG = "TIFFunctionUtil";

	private static final String CUR_CHANNEL_ID = MtkTvConfigType.CFG_NAV_AIR_CRNT_CH;
	/**for channel list dialog*/
	public static final int CH_MASK_ALL = MtkTvChCommonBase.SB_VNET_ALL;
	public static final int CH_UP_DOWN_MASK =  MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_VISIBLE | MtkTvChCommonBase.SB_VNET_FAKE;
	public static final int CH_UP_DOWN_VAL =  MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_VISIBLE ;
	public static final int CH_LIST_MASK = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE;
	public static final int CH_LIST_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE ;

	public static final int CH_FAKE_MASK =  MtkTvChCommonBase.SB_VNET_FAKE;
	public static final int CH_FAKE_VAL = MtkTvChCommonBase.SB_VNET_FAKE;

	public static final int CH_LIST_RADIO_MASK = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE | MtkTvChCommonBase.SB_VNET_RADIO_SERVICE;
	public static final int CH_LIST_RADIO_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_RADIO_SERVICE;
	public static final int CH_LIST_ANALOG_MASK = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE | MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE;
	public static final int CH_LIST_ANALOG_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE;
	public static final int CH_LIST_DIGITAL_MASK = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE | MtkTvChCommonBase.SB_VNET_RADIO_SERVICE | MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE;
	public static final int CH_LIST_DIGITAL_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE ;
	/**for epg channel list*/
	public static final int CH_LIST_EPG_MASK = MtkTvChCommonBase.SB_VNET_ACTIVE ;
	public static final int CH_LIST_EPG_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE;
	public static final int CH_LIST_EPG_US_MASK = MtkTvChCommonBase.SB_VNET_EPG | MtkTvChCommonBase.SB_VNET_FAKE;
	public static final int CH_LIST_EPG_US_VAL = MtkTvChCommonBase.SB_VNET_EPG;
	/**for inactive channel*/
	public static final int CH_CONFIRM_REMOVE_MASK = MtkTvChCommonBase.SB_VNET_REMOVAL_TO_CONFIRM;
	public static final int CH_CONFIRM_REMOVE_VAL = MtkTvChCommonBase.SB_VNET_REMOVAL_TO_CONFIRM;
	public static final int CH_LIST_FAV1_MASK = MtkTvChCommonBase.SB_VNET_FAVORITE1;
	public static final int CH_LIST_FAV1_VAL = MtkTvChCommonBase.SB_VNET_FAVORITE1;

	public static final int CH_SCRAMBLED_MASK = MtkTvChCommonBase.SB_VNET_SCRAMBLED;
    public static final int CH_SCRAMBLED_VAL = MtkTvChCommonBase.SB_VNET_SCRAMBLED;
    private static  int mCurCategories =-1;
    public static  String current3rdMId ="current_3rd_channel_mId";
    public static  String channelType ="TYPE_PREVIEW";
    public static final int sourceATVSvlId =2;
    public static final int channelDataValuelength =MtkTvChCommonBase.TV_DB_BLOB_LEN;
    public static final String inputIdStart ="com.mediatek.tvinput/.";
  /**
   * A constant for the key of the extra data for the app linking intent.
   */
  public static final String EXTRA_APP_LINK_CHANNEL_URI = "app_link_channel_uri";

  /**
   * An intent action to launch setup activity of a TV input. The intent should include TV input ID
   * in the value of {@link EXTRA_INPUT_ID}. Optionally, given the value of
   * {@link EXTRA_ACTIVITY_AFTER_COMPLETION}, the activity will be launched after the setup activity
   * successfully finishes.
   */
  public static final String INTENT_ACTION_INPUT_SETUP =
      "com.android.tv.intent.action.INPUT_SETUP";
  /**
   * A constant for the key to indicate a TV input ID for the intent action
   * {@link INTENT_ACTION_INPUT_SETUP}.
   * <p>
   * Value type: String
   */
  public static final String EXTRA_INPUT_ID =
      "com.android.tv.intent.extra.INPUT_ID";
  /**
   * A constant for the key to indicate an Activity launch intent for the intent action
   * {@link INTENT_ACTION_INPUT_SETUP}.
   * <p>
   * Value type: Intent (Parcelable)
   */
  public static final String EXTRA_ACTIVITY_AFTER_COMPLETION =
      "com.android.tv.intent.extra.ACTIVITY_AFTER_COMPLETION";

	/**
	 * get current channel id
	 * @return
	 */
	public static int getCurrentChannelId() {
        int chId = MtkTvConfig.getInstance().getConfigValue(CUR_CHANNEL_ID);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentChannelId chId = " + chId);
        return chId;

    }
	
	/**
	 * not start with "com.mediatek.tvinput/." is 3rd
	 * @return
	 */
	public static boolean is3rdTVSource(TIFChannelInfo tIFChannelInfo){
		return tIFChannelInfo != null && tIFChannelInfo.mInputServiceName!=null && !tIFChannelInfo.mInputServiceName.startsWith(inputIdStart);
	}

	/**
	 * check mask
	 * @param chinfo
	 * @param attentionMask
	 * @param expectValue
	 * @return
	 */
	public static boolean checkChMask(MtkTvChannelInfoBase chinfo,int attentionMask,int expectValue){
	    if(CommonIntegration.isTVSourceSeparation()){
	        if(CommonIntegration.getInstance().isCurrentSourceDTVforEuPA()){
	            if(chinfo != null && !chinfo.isUserDelete() && !chinfo.isAnalogService()){
	                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask chinfo.getNwMask() = "+chinfo.getNwMask() +"atentionMask="+attentionMask +" expectValue="+expectValue );
	                if((chinfo.getNwMask() & attentionMask) == expectValue){
	                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask true");
	                    return true;
	                }
	            }
	        }else if(CommonIntegration.getInstance().isCurrentSourceATVforEuPA() ){
	            if(chinfo != null && !chinfo.isUserDelete() && chinfo.isAnalogService()){
	                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask chinfo.getNwMask() = "+chinfo.getNwMask() +"atentionMask="+attentionMask +" expectValue="+expectValue );
	                if((chinfo.getNwMask() & attentionMask) == expectValue){
	                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask true");
	                    return true;
	                }
	            }
	        }
	    }else{
	        if(chinfo != null && (!chinfo.isUserDelete() || (chinfo.isUserDelete() && CommonIntegration.isSARegion()))){
	            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask chinfo.getNwMask() = "+chinfo.getNwMask() +"atentionMask="+attentionMask +" expectValue="+expectValue );
	            if((chinfo.getNwMask() & attentionMask) == expectValue){
	                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask true");
	                return true;
	            }
	        }
	        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask false");
	    }

		return false;
	}

	/**
     * check mask and sdtServiceType
     * @param chinfo
     * @param attentionMask
     * @param expectValue
     * @return
     */
    public static boolean checkChMaskForDuk(TIFChannelInfo chinfo,int attentionMask,int expectValue,int sdtServiceType){

            if(chinfo != null && !chinfo.isUserDelete()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMaskForDuk sdtServiceType = " +sdtServiceType);
                if(sdtServiceType !=-1){
                    if(chinfo.mDataValue != null &&
                            chinfo.mDataValue.length ==channelDataValuelength &&
                            (chinfo.mDataValue[6] & attentionMask) == expectValue){
                        if(sdtServiceType ==CommonIntegration.CH_SDT_SERVICE_TYPE_RADIO){
                            if(chinfo.mDataValue[9] == sdtServiceType
                                    || chinfo.mDataValue[9] == CommonIntegration.CH_SDT_SERVICE_TYPE_RADIO2){
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMaskForDuk sdtServiceType HD true");
                                return true;
                            }
                        }else{
                            if (sdtServiceType == CommonIntegration.SVL_SERVICE_TYPE_IP_SVC && chinfo.mDataValue[5] == sdtServiceType){
                                return true;
                            }else if((chinfo.mDataValue[9] == sdtServiceType)){
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMaskForDuk sdtServiceType true");
                                return true;
                            }
                        }
                    }
                }else{
                     if(attentionMask == CommonIntegration.CH_LIST_3RDCAHNNEL_MASK){
                        if(chinfo.mDataValue == null || chinfo.mDataValue.length != channelDataValuelength){
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMaskForDuk true 3rd");
                            return true;
                        }
                    }else if((attentionMask & CH_LIST_FAV1_MASK) == CH_LIST_FAV1_VAL && attentionMask != CommonIntegration.CH_LIST_3RDCAHNNEL_MASK){
                        if(chinfo.mDataValue != null &&
                                chinfo.mMtkTvChannelInfo != null &&
                                (chinfo.mMtkTvChannelInfo.getNwMask() & attentionMask) == expectValue){
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMaskForDuk CH_LIST_FAV1_MASK true");
                            return true;
                        }
                    }else{
                        if((MarketRegionInfo.F_3RD_INPUTS_SUPPORT && (chinfo.mDataValue == null || chinfo.mDataValue.length != channelDataValuelength))
                                || (chinfo.mDataValue != null && chinfo.mDataValue.length ==channelDataValuelength &&
                                (chinfo.mDataValue[6] & attentionMask) == expectValue)){
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMaskForDuk true");
                            return true;
                        }
                    }

                }
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMaskForDuk false");

        return false;
    }
	   /**
     * check Category mask
     * @param chinfo
     * @param CategoryMask
     * @param CategoryValue
     * @return
     */
    public static boolean checkChCategoryMask(MtkTvChannelInfoBase info,int categoryMask,int categoryValue){
//      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask chinfo="+chinfo);
        if(info instanceof MtkTvDvbChannelInfo){
            MtkTvDvbChannelInfo chinfo =(MtkTvDvbChannelInfo) info;
            if(chinfo != null && (!chinfo.isUserDelete() || (chinfo.isUserDelete() && CommonIntegration.isSARegion()))){
                if(chinfo != null && categoryMask > 0){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChCategoryMask chinfo.getCategoryMask() = "+chinfo.getCategoryMask() +"categoryMask="+categoryMask +" categoryValue="+categoryValue );
                    if((chinfo.getCategoryMask() & categoryMask) == categoryValue){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask true");
                        return true;
                    }
                }else if(chinfo != null && categoryMask == 0){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChCategoryMask chinfo.getChannelNumber() = "+chinfo.getChannelNumber() +" categoryMask = "+categoryMask +" categoryValue = "+categoryValue );
                    if(chinfo.getChannelNumber() >= CommonIntegration.CATEGORIES_CHANNELNUM_BASE){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask true");
                        return true;
                    }
                }else if(chinfo != null && categoryMask == -1){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChCategoryMask true");
                    return true;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChCategoryMask false");
            }
        }else{
            return true;
        }

        return false;
    }
	/**
	 * check mask
	 * @param chinfo
	 * @param attentionMask
	 * @param expectValue
	 * @return
	 */
	public static boolean checkChMask(TIFChannelInfo chinfo,int attentionMask,int expectValue){
		if(chinfo != null && chinfo.mMtkTvChannelInfo != null){
			if((chinfo.mMtkTvChannelInfo.getNwMask() & attentionMask) == expectValue){
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask true");
				return true;
			}
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask false");
		return false;
	}

    /**
     * check mask
     * @param chinfo
     * @param attentionMask
     * @param expectValue
     * @return
     */
    public static boolean checkChMaskformDataValue(TIFChannelInfo chinfo,int attentionMask,int expectValue){
        if(CommonIntegration.isTVSourceSeparation()){
            if(CommonIntegration.getInstance().isCurrentSourceDTVforEuPA()){
                if(chinfo != null && !chinfo.isUserDelete() && !chinfo.isAnalogService()){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask chinfo.mData"+chinfo.mData);
                    if(chinfo.mDataValue != null &&
                            chinfo.mDataValue.length ==channelDataValuelength &&
                            (chinfo.mDataValue[6] & attentionMask) == expectValue){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask true");
                        return true;
                    }else if(chinfo.mDataValue != null &&
                            chinfo.mDataValue.length ==6){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask true");
                        return true;
                    }
                }
            }else if(CommonIntegration.getInstance().isCurrentSourceATVforEuPA() ){
                if(chinfo != null && !chinfo.isUserDelete() && chinfo.isAnalogService()){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask chinfo.mData"+chinfo.mData);
                    if(chinfo.mDataValue != null &&
                            chinfo.mDataValue.length ==channelDataValuelength &&
                            (chinfo.mDataValue[6] & attentionMask) == expectValue){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask true");
                        return true;
                    }else if(chinfo.mDataValue != null &&
                            chinfo.mDataValue.length ==6){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask true");
                        return true;
                    }
                }
            }
        }else{
            if(chinfo != null && (!chinfo.isUserDelete() || (chinfo.isUserDelete() && CommonIntegration.isSARegion()))){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask chinfo.mData"+chinfo.mData);
                if(chinfo.mDataValue != null &&
                        chinfo.mDataValue.length ==channelDataValuelength &&
                        (chinfo.mDataValue[6] & attentionMask) == expectValue){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask true");
                    return true;
                }else if(chinfo.mDataValue != null &&
                            chinfo.mDataValue.length ==6){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask true");
                        return true;
                    }
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkChMask false");
        }

        return false;
    }

	/**
	 * get channel with program
	 * @param channelList
	 * @param programMapList
	 * @param startTime
	 * @return
	 */
	public static List<EPGChannelInfo> getEpgChannelProgramsGroup(List<EPGChannelInfo> channelList, Map<Long, List<TIFProgramInfo>> programMapList, long startTime) {
		List<EPGProgramInfo> ePGProgramList = null;
		long duration = 2 * 60 * 60;
		long endTime = startTime + duration;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime>>" + startTime + "   endTime>>" + endTime);

		Map<Integer,EPGProgramInfo> mapEPGChannelInfo=new HashMap<Integer,EPGProgramInfo>();
		for (EPGChannelInfo tempChannelInfo:channelList) {
			List<TIFProgramInfo> tempProgramList = null;
			if (programMapList != null){
				tempProgramList = programMapList.get(tempChannelInfo.mId);
			}
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "***************************tempChannelInfo.name="+tempChannelInfo.getName()+"*****************************");
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempProgramList>>" + tempChannelInfo.mId + "  " + (tempProgramList==null?tempProgramList:tempProgramList.size()));
			ePGProgramList = new ArrayList<EPGProgramInfo>();
			if (tempProgramList == null) {
				tempChannelInfo.setmTVProgramInfoList(ePGProgramList);
				continue;
			}
			for (TIFProgramInfo tempProgramInfo:tempProgramList) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempProgramInfo.mStartTimeUtcSec>>" + tempProgramInfo.mStartTimeUtcSec + "   " + tempProgramInfo.mEndTimeUtcSec);
				if (tempProgramInfo.mEndTimeUtcSec > startTime && tempProgramInfo.mStartTimeUtcSec < endTime && !containsEvent(ePGProgramList, tempProgramInfo)) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start creat epg evevt info>>>" + tempChannelInfo.getTVChannel().getChannelId() + "   " + tempProgramInfo.mEventId);
					String title = CommonIntegration.getInstance().getAvailableStringForProg(tempProgramInfo.mTitle);
                    //for fix event time overlay
                    if(ePGProgramList.size() - 1 >= 0 &&
                            ePGProgramList.get(ePGProgramList.size() - 1).getmEndTime() > tempProgramInfo.getmStartTimeUtcSec()){
                        tempProgramInfo.setmStartTimeUtcSec(ePGProgramList.get(ePGProgramList.size() - 1).getmEndTime());
                    }

					EPGProgramInfo tempEPGinfo = new EPGProgramInfo(tempChannelInfo.getTVChannel().getChannelId(), tempProgramInfo.mEventId,
							tempProgramInfo.mStartTimeUtcSec, tempProgramInfo.mEndTimeUtcSec, title, tempProgramInfo.mRating);
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mEventId="+tempProgramInfo.mEventId+",ChannelId="+tempChannelInfo.getTVChannel().getChannelId());
					MtkTvEventInfoBase apiEPGInfo = MtkTvEvent.getInstance().getEventInfoByEventId(tempProgramInfo.mEventId, tempChannelInfo.getTVChannel().getChannelId());
					if (apiEPGInfo == null || apiEPGInfo.getStartTime() == 0) {
						continue;
					}
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "apiEPGInfo>>>" + apiEPGInfo.getEventRating() + "  event.getStartTime()>>>" + apiEPGInfo.getStartTime() + "   " + apiEPGInfo.getDuration());
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "----------------tempProgramInfo.mTitle="+tempProgramInfo.mTitle+"------------------------------------------------");
					MtkTvEventGroupBase[] mtkTvEventGroups=apiEPGInfo.getEventGroup();
					if(mtkTvEventGroups!=null &&mtkTvEventGroups.length>0){
						for(MtkTvEventGroupBase mtkTvEventGroup:mtkTvEventGroups){
							int eventType=mtkTvEventGroup.geteventType();
							com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "eventType="+eventType);
							//Normal Event,need group,skip
							if(eventType==0){
								continue;
							}
							if(eventType==1){
								mapEPGChannelInfo.put(tempProgramInfo.mEventId,tempEPGinfo);
							}
							MtkTvEventComom[] eventComoms=mtkTvEventGroup.geteventCommom();
							if(eventComoms==null || eventComoms.length==0){
								continue;
							}
							for(MtkTvEventComom eventComom:eventComoms){
								com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "eventCommom.channelId="+eventComom.getChannelId()+",eventCommom.eventId="+eventComom.getEventId());
								if(tempProgramInfo.mEventId!=eventComom.getEventId()){
									continue;
								}
								else if(eventType==2){
									EPGProgramInfo mainEpgProgramInfo=mapEPGChannelInfo.get(eventComom.getEventId());
									if(mainEpgProgramInfo==null){
										continue;
									}
									tempEPGinfo.setmTitle(mainEpgProgramInfo.getmTitle());
									break;
								}
							}
						}
					}
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "-----------------------------------------------------------------");
					tempEPGinfo.setProgramBlock(MtkTvEvent.getInstance().checkEventBlock(tempChannelInfo.getTVChannel().getChannelId(), apiEPGInfo.getEventId()));
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tvEvent.checkEventBlock:" + tempEPGinfo.isProgramBlock());
					tempEPGinfo.setCategoryType(apiEPGInfo.getEventCategory());
					tempEPGinfo.setHasSubTitle(apiEPGInfo.isCaption());
					DataReader.getInstanceWithoutContext().setMStype(tempEPGinfo, apiEPGInfo.getEventCategory());
					tempEPGinfo.setRatingType(apiEPGInfo.getEventRating());//get rating from api
					tempEPGinfo.setRatingValue(apiEPGInfo.getEventRatingType());
					String resultDetail = DataReader.getInstanceWithoutContext().getResultDetail(apiEPGInfo.getGuidanceText(), tempProgramInfo.mDescription, tempProgramInfo.mLongDescription);
					resultDetail = CommonIntegration.getInstance().getAvailableStringForProg(resultDetail);
					tempEPGinfo.setDescribe(resultDetail);
					float width = DataReader.getInstanceWithoutContext().getProWidth(tempEPGinfo, startTime, duration);
					tempEPGinfo.setmScale(width);
					if (ePGProgramList.isEmpty()) {
						float mLeftMargin = EPGTimeConvert.countShowWidth(tempEPGinfo.getmStartTime(), startTime);
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mLeftMargin?>i == 0>>" + tempEPGinfo.getmStartTime() + "   " + startTime + "   " + mLeftMargin);
						if (mLeftMargin > 0) {
							tempEPGinfo.setLeftMargin(mLeftMargin);
						} else {
							tempEPGinfo.setLeftMargin(0.0f);
						}
					} else {
						float mLeftMargin = DataReader.getInstanceWithoutContext().getProLeftMargin(tempEPGinfo, ePGProgramList.get(ePGProgramList.size() - 1), tempEPGinfo);
						tempEPGinfo.setLeftMargin(mLeftMargin);
					}
					ePGProgramList.add(tempEPGinfo);
				}
			}
			tempChannelInfo.setmTVProgramInfoList(ePGProgramList);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "***********************************************************************");
		}
		return channelList;
	}

	/**
	 * get channel with program
	 * @param channelList
	 * @param programMapList
	 * @param startTime
	 * @return
     */
    public static List<EPGChannelInfo> getEpgChannelProgramsGroupEx(List<EPGChannelInfo> channelList, Map<Long, List<TIFProgramInfo>> programMapList, long startTime) {
        List<EPGProgramInfo> ePGProgramList = null;
        String noProgramTitle = DestroyApp.appContext.getResources().getString(R.string.nav_epg_no_program_title);
        long duration = 2 * 60 * 60;
        long endTime = startTime + duration;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelList:"+channelList);
        if(channelList == null || programMapList == null){
          return null;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime>>" + startTime + "   endTime>>" + endTime+ ",channelList size:"+channelList.size());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "programMapList:"+programMapList);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime>>" + startTime + "   endTime>>" + endTime+ ",programMapList size:"+programMapList.size());

        for (EPGChannelInfo tempChannelInfo : channelList) {
            List<TIFProgramInfo> tempProgramList = programMapList.get(tempChannelInfo.mId);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getEpgChannelProgramsGroupEx," + tempChannelInfo.mId + "  " +
                (tempProgramList == null ? tempProgramList : tempProgramList.size()));

            ePGProgramList = new ArrayList<EPGProgramInfo>();
            if (tempProgramList == null) {
                tempChannelInfo.setmTVProgramInfoList(ePGProgramList);
                continue;
            }

            for (int i = 0; i < tempProgramList.size(); i++) {
                TIFProgramInfo tempProgramInfo  = tempProgramList.get(i);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getEpgChannelProgramsGroupEx, StartTime:" +
                    tempProgramInfo.mStartTimeUtcSec + "," + tempProgramInfo.mEndTimeUtcSec
                    +",tempProgramInfo:"+tempProgramInfo.mTitle
                    +",tempProgramInfo mEventId:"+tempProgramInfo.mEventId
                    +",tempProgramInfo tempChannelInfo mId:"+(int)tempChannelInfo.mId
                    +",tempProgramInfo mRating:"+tempProgramInfo.mRating);

                if(i > 0 && tempProgramInfo.mStartTimeUtcSec < tempProgramList.get(i-1).mEndTimeUtcSec){
                    if(tempProgramInfo.mEndTimeUtcSec >= endTime){
                      continue;//fix 3rd overlap issue.
                    }else {
                      tempProgramInfo.mStartTimeUtcSec = tempProgramList.get(i-1).mEndTimeUtcSec;
                    }
                }
                if (tempProgramInfo.mEndTimeUtcSec >= startTime &&
                    tempProgramInfo.mStartTimeUtcSec <= endTime &&
                    (!containsEvent(ePGProgramList, tempProgramInfo))) {
                    EPGProgramInfo tempEPGinfo = new EPGProgramInfo(
                        (int)tempChannelInfo.mId,
                        tempProgramInfo.mEventId,
                        tempProgramInfo.mStartTimeUtcSec,
                        tempProgramInfo.mEndTimeUtcSec,
                        tempProgramInfo.mTitle,
                        tempProgramInfo.mRating);

                    tempEPGinfo.setRatingType(tempProgramInfo.getmRating());
                    tempEPGinfo.setDescribe(tempProgramInfo.getmDescription());

                    float width = DataReader.getInstanceWithoutContext().getProWidth(tempEPGinfo, startTime, duration);
                    tempEPGinfo.setmScale(width);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getEpgChannelProgramsGroupEx, width:" +width);

                    EPGProgramInfo blankEPGinfo = null;
                    if (ePGProgramList.isEmpty()) {
                        float mLeftMargin = EPGTimeConvert.countShowWidth(tempEPGinfo.getmStartTime(), startTime);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mLeftMargin?>i == 0>>" + tempEPGinfo.getmStartTime() + "," + startTime + "," + mLeftMargin);
                        if (mLeftMargin > 0) {
                            tempEPGinfo.setLeftMargin(mLeftMargin);
                            blankEPGinfo = new EPGProgramInfo(
                                (int)tempChannelInfo.mId,
                                -1,//event id
                                startTime,
                                tempProgramInfo.mStartTimeUtcSec,
                                noProgramTitle,
                                "");// rating 
                            blankEPGinfo.setRatingType("");
                            blankEPGinfo.setDescribe(noProgramTitle);
                            float firstWidth = DataReader.getInstanceWithoutContext().getProWidth(blankEPGinfo, startTime, duration);
                            blankEPGinfo.setmScale(firstWidth);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getEpgChannelProgramsGroupEx, 0 blankEPGinfo firstWidth:" +firstWidth);
                            blankEPGinfo.setLeftMargin(0.0f);

                            tempEPGinfo.setLeftMargin(0.0f);
                        } else {
                            tempEPGinfo.setLeftMargin(0.0f);
                        }
                    } else {
                        float mLeftMargin = DataReader.getInstanceWithoutContext().getProLeftMargin(tempEPGinfo, ePGProgramList.get(ePGProgramList.size() - 1), tempEPGinfo);
                        tempEPGinfo.setLeftMargin(mLeftMargin);
                        if(mLeftMargin > 0 && i > 1){
                          blankEPGinfo = new EPGProgramInfo(
                              (int)tempChannelInfo.mId,
                              -1,//event id
                              tempProgramList.get(i-1).mEndTimeUtcSec,
                              tempProgramInfo.mStartTimeUtcSec,
                              noProgramTitle,
                              "");// rating 
                          blankEPGinfo.setRatingType("");
                          blankEPGinfo.setDescribe(noProgramTitle);
                          float blankWidth = DataReader.getInstanceWithoutContext().getProWidth(blankEPGinfo, startTime, duration);
                          blankEPGinfo.setmScale(blankWidth);
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getEpgChannelProgramsGroupEx, > 0 blankEPGinfo blankWidth:" +blankWidth);
                          blankEPGinfo.setLeftMargin(0.0f);
                        }
                        tempEPGinfo.setLeftMargin(0.0f);
                    }
                if(blankEPGinfo != null){
                  ePGProgramList.add(blankEPGinfo);
                }
                ePGProgramList.add(tempEPGinfo);
            }
        }
        EPGProgramInfo lastBlankEPGinfo = null;
        if(ePGProgramList.get(ePGProgramList.size() - 1).getmEndTime() < endTime){
          lastBlankEPGinfo = new EPGProgramInfo(
              (int)tempChannelInfo.mId,
              -1,//event id
              ePGProgramList.get(ePGProgramList.size() - 1).getmEndTime(),
              endTime,
              noProgramTitle,
              "");// rating
          lastBlankEPGinfo.setRatingType("");
          lastBlankEPGinfo.setDescribe(noProgramTitle);
          float lastWidth = DataReader.getInstanceWithoutContext().getProWidth(lastBlankEPGinfo, startTime, duration);
          lastBlankEPGinfo.setmScale(lastWidth);
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getEpgChannelProgramsGroupEx, lastBlankEPGinfo lastWidth:" +lastWidth);
          lastBlankEPGinfo.setLeftMargin(0.0f);
        }
        if(lastBlankEPGinfo != null){
          ePGProgramList.add(lastBlankEPGinfo);
        }
            tempChannelInfo.setmTVProgramInfoList(ePGProgramList);
        }
        return channelList;
    }

    public static boolean containsEvent(List<EPGProgramInfo> ePGProgramList, TIFProgramInfo tempEPGinfo) {
		if (ePGProgramList != null) {
			for (EPGProgramInfo info : ePGProgramList) {
				if (info.getmStartTime() == tempEPGinfo.mStartTimeUtcSec && info.getmEndTime() == tempEPGinfo.mEndTimeUtcSec) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * TIF channel to api channel
	 * @param tifChannelList
	 * @return
	 */
	public static List<MtkTvChannelInfoBase> getApiChannelList(List<TIFChannelInfo> tifChannelList) {
		List<MtkTvChannelInfoBase> chlist = new ArrayList<MtkTvChannelInfoBase>();
		if (tifChannelList == null || tifChannelList.isEmpty()) {
			return chlist;
		}
		for (TIFChannelInfo tempTifChannel:tifChannelList) {
			chlist.add(tempTifChannel.mMtkTvChannelInfo);
		}
		return chlist;
	}

	/**
	 * API channel to TIF channel
	 * @param tifChannelList
	 * @return
	 */
	public static List<TIFChannelInfo> getTIFChannelList(List<MtkTvChannelInfoBase> apiChannelList) {
		List<TIFChannelInfo> chlist = new ArrayList<TIFChannelInfo>();
		if (apiChannelList == null || apiChannelList.isEmpty()) {
			return chlist;
		}
		TIFChannelInfo tempTIFChInfo = null;
		for (MtkTvChannelInfoBase tempApiChannel:apiChannelList) {
			tempTIFChInfo = new TIFChannelInfo();
			tempTIFChInfo.mMtkTvChannelInfo = tempApiChannel;
			chlist.add(tempTIFChInfo);
		}
		return chlist;
	}

	/**
	 * EPG channel to api channel
	 * @param tifChannelList
	 * @return
	 */
	public static List<MtkTvChannelInfoBase> getApiChannelListFromEpgChannel(List<EPGChannelInfo> epgChannelList) {
		List<MtkTvChannelInfoBase> chlist = new ArrayList<MtkTvChannelInfoBase>();
		for (EPGChannelInfo tempEpgChannel:epgChannelList) {
			chlist.add(tempEpgChannel.getTVChannel());
		}
		return chlist;
	}

	/**
	 * set program active window data
	 * @param channels
	 * @param startTime
	 */
	public static void setActivityWindow(List<MtkTvChannelInfoBase> channels, long startTime) {
		MtkTvEvent.getInstance().setCurrentActiveWindows(channels, startTime);
	}


	/**
	 * set program active window data
	 * @param channels
	 * @param startTime
	 * @param durtion
	 */
	public static void setActivityWindow(List<MtkTvChannelInfoBase> channels, long startTime,long durtion) {
		MtkTvEvent.getInstance().setCurrentActiveWindows(channels, startTime,durtion);
	}

	/**
	 * get epg event info from tv api
	 * @param channelId the channel id that need get eventInfo
	 * @param startTime the start time of eventInfo
	 * @param duratuon calculate end of program time
	 * @return
	 */
	public static List<MtkTvEventInfoBase> getEpgEventListByChannelId(int channelId, long startTime, long duratuon) throws Exception {
		return MtkTvEvent.getInstance().getEventListByChannelId(channelId, startTime, duratuon);
	}


	/**
	 * get EU yatai DTV/ATV api

	 * @return
	 */
	public static boolean isEUPARegion(){
		return CommonIntegration.getInstance().isEUPARegion();
	}


    public static void setmCurCategories(int curCategories){
        mCurCategories =curCategories;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setmCurCategories," + mCurCategories);
    }

    public static int getmCurCategories(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getmCurCategories," + mCurCategories);
        return mCurCategories ;
    }


}
