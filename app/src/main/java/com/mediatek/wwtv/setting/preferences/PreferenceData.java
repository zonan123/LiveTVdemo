
package com.mediatek.wwtv.setting.preferences;

import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import com.mediatek.wwtv.setting.LiveTvSetting;
import com.mediatek.wwtv.setting.util.GetTimeZone;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.MenuDataHelper;
import com.mediatek.twoworlds.tv.model.MtkTvOpenVCHIPSettingInfoBase;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;

import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.setting.base.scan.adapter.SatListAdapter.SatItem;
import com.mediatek.wwtv.setting.base.scan.model.SatelliteInfo;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.scan.EditChannel;
//import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.view.FacVideo;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.SundryImplement;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import android.content.Context;
import android.media.tv.TvTrackInfo;
//import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceCategory;
//import android.support.v7.preference.PreferenceScreen;
//import android.support.v7.preference.ListPreference;
//import android.support.v14.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
//import com.mediatek.twoworlds.tv.MtkTvSubtitle;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeShiftManager;


import com.mediatek.twoworlds.tv.MtkTvATSCCloseCaption;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvAVMode;
import com.mediatek.twoworlds.tv.model.TvProviderAudioTrackBase;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFFunctionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.mediatek.wwtv.tvcenter.util.InstrumentationHandler;
import android.view.KeyEvent;

import com.mediatek.wwtv.setting.util.RegionConst;
public class PreferenceData {
    private static final String TAG = "PreferenceData";
    private volatile static PreferenceData mInstance = null;
    private PreferenceScreen mScreen;
    public TVContent mTV;
    public MenuConfigManager mConfigManager = null;
    private final Context mContext;
    public boolean isNeedRefreshTime = false;

    private PreferenceData(Context context) {
        mContext = context;
        mTV = TVContent.getInstance(context);
        mConfigManager = MenuConfigManager.getInstance(context);
        RxBus.instance.onEvent(ActivityDestroyEvent.class)
                .filter(it -> it.activityClass == LiveTvSetting.class)
                .firstElement()
                .doOnSuccess(it  -> free())
                .subscribe();
    }

    private static synchronized void free(){
        Log.i(TAG, "free");
        mInstance = null;
    }

    public static synchronized PreferenceData getInstance(Context context) {
        if (null == mInstance) {
            mInstance = new PreferenceData(context.getApplicationContext());
        }

        return mInstance;
    }

    public static synchronized void setPreferenceDataNull() {
        if(mInstance != null){
            mInstance = null;
        }
    }

    public synchronized PreferenceScreen getData() {
        return mScreen;
    }

    public synchronized void setData(PreferenceScreen screen) {
        mScreen = screen;
    }

    public interface OnDataChangeListener {
        boolean onDataChangeListener(PreferenceScreen screen, Object data);
    }

    public synchronized void invalidate(String itemID, Object newValue) {
        //you can get current Preference data from mScreen.
        //then update data
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, itemID + "," + newValue);
        if (itemID.startsWith(MenuConfigManager.CFG_MENU_AUDIOINFO_GET_STRING)) {
             Preference visuallyImpairedAudioInfo = null;
             for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                 visuallyImpairedAudioInfo = mScreen.getPreference(i);
                 ListPreference listPreference =(ListPreference )visuallyImpairedAudioInfo;
                 if(listPreference.getKey() == itemID){
                     continue;
                 }else{
                        listPreference.setValue("1");
                        listPreference.setSummary("1");
                 }

             }

          }
        if (itemID.startsWith(MenuConfigManager.SOUNDTRACKS_GET_STRING)) {
             Preference soundtracks = null;
             for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                 soundtracks = mScreen.getPreference(i);
                 ListPreference listPreference =(ListPreference )soundtracks;
                 if(listPreference.getKey() == itemID){
                     continue;
                 }else{
                        listPreference.setValue("1");
                        listPreference.setSummary("1");
                 }

             }
          }

        switch (itemID) {
        case MenuConfigManager.PARENTAL_RATINGS_ENABLE:
	        int value = (int)newValue;
            Preference tempPre = null;
            for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                tempPre = mScreen.getPreference(i);
                if(tempPre.getKey() == itemID){
                    continue;
                }
                if(value == 1 &&
                        mTV.getRatingEnable() == 1){
                    if (tempPre.getKey() != MenuConfigManager.PARENTAL_OPEN_VCHIP) {
                        tempPre.setEnabled(true);
                    }else {
                        if (mTV.getATSCRating().isOpenVCHIPInfoAvailable()) {
                            tempPre.setEnabled(true);
                        }else {
                            tempPre.setEnabled(false);
                        }
                    }
                } else if(value == 0){
                    tempPre.setEnabled(false);
                }
            }
            break;

        case MenuConfigManager.SETUP_CAPTION_STYLE:
            int cSvalue = Integer.parseInt((String)newValue);
            if (1 == cSvalue){
                setCaptionStyle(cSvalue);
            }
            Preference cStempPre = null;
            for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                cStempPre = mScreen.getPreference(i);
                if(cStempPre.getKey() == itemID){
                    continue;
                }
                if(cSvalue == 1){
                    cStempPre.setEnabled(true);
                } else if(cSvalue == 0){
                    cStempPre.setEnabled(false);
                }
            }

            break;

        case MenuConfigManager.AUTO_ADJUST:
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "AUTO_ADJUST" + " == " + itemID);
             //int autoadjustvalue = (int) newValue;
             Preference autoadjusttempPre = null;
             for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                 autoadjusttempPre =  mScreen.getPreference(i);
                 if(autoadjusttempPre.getKey() == itemID){
                     continue;
                 }
                 if(autoadjusttempPre instanceof ProgressPreference){
                     ProgressPreference autoadjust =(ProgressPreference) autoadjusttempPre;
                      //h.position
                     if(autoadjust.getKey().equals(MenuConfigManager.HPOSITION)){
                         autoadjust.setCurrentValue(mConfigManager.getDefault(MenuConfigManager.HPOSITION));
                     }else if(autoadjust.getKey().equals(MenuConfigManager.VPOSITION)){
                     //v.position
                         autoadjust.setCurrentValue(mConfigManager.getDefault(MenuConfigManager.VPOSITION));
                     }else if(autoadjust.getKey().equals(MenuConfigManager.PHASE)){
                     //phase
                         autoadjust.setCurrentValue(mConfigManager.getDefault(MenuConfigManager.PHASE));
                     }else if(autoadjust.getKey().equals(MenuConfigManager.CLOCK)){
                     //clock
                         autoadjust.setCurrentValue(mConfigManager.getDefault(MenuConfigManager.CLOCK));
                     }


                 }

             }
             break;

         case MenuConfigManager.EFFECT:
             int effectValue = Integer.parseInt((String)newValue);
             Preference effectTempPre = null;
             for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                 effectTempPre = mScreen.getPreference(i);
                 if(effectTempPre.getKey() == itemID){
                     continue;
                 }
                 if(effectValue == 0){
                     effectTempPre.setEnabled(false);
                 } else {
                     effectTempPre.setEnabled(true);
                 }
             }
             break;

         case MenuConfigManager.COLOR_G_R:
         case MenuConfigManager.COLOR_G_G:
         case MenuConfigManager.COLOR_G_B:
             Preference colorTempPre = null;
             for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                 colorTempPre = mScreen.getPreference(i);
                 if(colorTempPre.getKey() ==  MenuConfigManager.COLOR_TEMPERATURE){
                     ListPreference listPreference =(ListPreference )colorTempPre;
                        listPreference.setValue(String.valueOf(mConfigManager.getDefault(MenuConfigManager.COLOR_TEMPERATURE) -
                                mConfigManager.getMin(MenuConfigManager.COLOR_TEMPERATURE)));
                        listPreference.setSummary(listPreference.getEntries()[mConfigManager.getDefault(MenuConfigManager.COLOR_TEMPERATURE) -
                                                                            mConfigManager.getMin(MenuConfigManager.COLOR_TEMPERATURE)]);
                      //  colorTempPre=listPreference; fix DTV00935766
                 }

             }
             break;

         case MenuConfigManager.VGA_MODE:
             int vgaModeValue = Integer.parseInt((String)newValue);
             Preference vgaModePre = null;
             for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                 vgaModePre = mScreen.getPreference(i);
                if (vgaModePre.getKey() == MenuConfigManager.DNR
                    || vgaModePre.getKey() == MenuConfigManager.MPEG_NR
                    || vgaModePre.getKey() == MenuConfigManager.ADAPTIVE_LUMA_CONTROL
                    || vgaModePre.getKey() == MenuConfigManager.FLESH_TONE
                    || vgaModePre.getKey() == MenuConfigManager.BLUE_STRETCH){
                    if (vgaModeValue == 0){
                        vgaModePre.setEnabled(false);
                    } else {
                        vgaModePre.setEnabled(true);
                    }
                }
             }
             break;

         case MenuConfigManager.HDMI_MODE:
             int hdmiModeValue = Integer.parseInt((String)newValue);
             Preference hdmiModePre = null;
             for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                 hdmiModePre = mScreen.getPreference(i);
                if (hdmiModePre.getKey() == MenuConfigManager.DNR
                    || hdmiModePre.getKey() == MenuConfigManager.MPEG_NR
                    || hdmiModePre.getKey() == MenuConfigManager.ADAPTIVE_LUMA_CONTROL
                    || hdmiModePre.getKey() == MenuConfigManager.FLESH_TONE
                    || hdmiModePre.getKey() == MenuConfigManager.BLUE_STRETCH){
                    if (hdmiModeValue != 1){
                        hdmiModePre.setEnabled(true);
                    } else {
                        hdmiModePre.setEnabled(false);
                    }
                }
             }
             break;

         case MenuConfigManager.GAME_MODE:
             int gameModeValue = Integer.parseInt((String)newValue);
             Preference gameModePre = null;
             for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                 gameModePre = mScreen.getPreference(i);
                if (gameModePre.getKey() == MenuConfigManager.MJC ){
                    if (gameModeValue == 0){
                        if (CommonIntegration.getInstance().isPipOrPopState()) {
                            gameModePre.setEnabled(false);
                        } else {
                            gameModePre.setEnabled(true);
                        }
                    } else {
                        gameModePre.setEnabled(false);
                    }
                }
                if (gameModePre.getKey() == MenuConfigManager.DI_FILM_MODE){
                    if (gameModeValue == 1){
                           gameModePre.setEnabled(false);
                    } else {
                           gameModePre.setEnabled(true);
                    }
                }
             }
             break;
         case MenuConfigManager.BRIGHTNESS:
         case MenuConfigManager.CONTRAST:
         case MenuConfigManager.SATURATION:
         case MenuConfigManager.HUE:
         case MenuConfigManager.SHARPNESS:
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "BRIGHTNESS  == " + itemID);
             Preference pictureModePre = null;
             for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                 pictureModePre = mScreen.getPreference(i);
                if (pictureModePre.getKey() == MenuConfigManager.PICTURE_MODE){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CONTRAST == " + pictureModePre.getKey());
                    ListPreference listPreference =(ListPreference )pictureModePre;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CONTRAST value== " + ""+(mConfigManager.getDefault(MenuConfigManager.PICTURE_MODE) -
                            mConfigManager.getMin(MenuConfigManager.PICTURE_MODE)));
			 		int cur = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_VIDEO_PIC_MODE);
			 		if(cur == 5 || cur == 6) {
			 			//int modeindex = mConfigManager.getDefault(MenuConfigManager.PICTURE_MODE) -
				 												//mConfigManager.getMin(MenuConfigManager.PICTURE_MODE);
			 			Log.d(TAG, "the picture mode index" + cur);
			 			//modeindex = modeindex -5;
			 			cur = cur -5;
			 			listPreference.setValue(String.valueOf(cur));
			 			listPreference.setSummary(listPreference.getEntries()[cur]);
			 		} else {
			 			listPreference.setValue(String.valueOf(mConfigManager.getDefault(MenuConfigManager.PICTURE_MODE) -
			 				mConfigManager.getMin(MenuConfigManager.PICTURE_MODE)));
			 			listPreference.setSummary(listPreference.getEntries()[mConfigManager.getDefault(MenuConfigManager.PICTURE_MODE) -
			 		                         			 				mConfigManager.getMin(MenuConfigManager.PICTURE_MODE)]);
			 		}
                }
             }
             break;

         case MenuConfigManager.VIDEO_3D_MODE:
         case MenuConfigManager.VIDEO_3D_NAV:
         case MenuConfigManager.VIDEO_3D_IMG_SFTY:
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "3D  == " + itemID);
                 Preference video3DModePre = null;
                 for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                     video3DModePre = mScreen.getPreference(i);
                     if(video3DModePre.getKey()==MenuConfigManager.VIDEO_3D_MODE){
                         if(mConfigManager.getDefault(video3DModePre.getKey()) ==0){
                             mConfigManager.setValue(video3DModePre.getKey(),0 );
                         }

                         String[] m3DModeArr=mContext.getResources().getStringArray(
                                    R.array.menu_video_3d_mode_array);
                         if(mConfigManager.getDefault(MenuConfigManager.VIDEO_3D_NAV)==1){
                             m3DModeArr = mContext.getResources().getStringArray(
                                       R.array.menu_video_3d_mode_array_for_1);
                        }else if(mConfigManager.getDefault(MenuConfigManager.VIDEO_3D_NAV)==0)  {
                             m3DModeArr = mContext.getResources().getStringArray(
                                     R.array.menu_video_3d_mode_array_for_0);
                        }
                         ListPreference listPreference=(ListPreference )video3DModePre;
                         listPreference.setEntries(m3DModeArr);
                         listPreference.setEntryValues(PreferenceUtil.getCharSequence(m3DModeArr.length));
                         listPreference.setValue(""+mConfigManager.getDefault(MenuConfigManager.VIDEO_3D_MODE));
                         video3DModePre=listPreference;

                     }
                     video3DModePre.setEnabled(mConfigManager.isConfigEnabled(video3DModePre.getKey()));
                 }
                 break;
         case MenuConfigManager.TV_CHANNEL_CLEAR:
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "start changeEnable TV_CHANNEL_CLEAR");
             Preference cleanList=null;
             for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                 cleanList = mScreen.getPreference(i);
				 if(cleanList == null){ break;}
                 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable cleanList.getKey() is "+cleanList.getKey());
                 if(cleanList.getKey() == MenuConfigManager.DVBS_SAT_RE_SCAN && cleanList instanceof DialogPreference){
                	 mScreen.removePreference(cleanList);
                	 Context themeContext = mContext;
                	 if(SettingsPreferenceScreen.getInstance() != null 
                			 && SettingsPreferenceScreen.getInstance().mPreferenceManager != null){
                		 themeContext = SettingsPreferenceScreen.getInstance().mPreferenceManager.getContext();
                	 }
                	 cleanList = PreferenceUtil.getInstance(themeContext).createPreference(MenuConfigManager.DVBS_SAT_RE_SCAN,
                             R.string.menu_s_sate_rescan);
                	 cleanList.setOrder(0);
                	 mScreen.addPreference(cleanList);
                	 continue;
                 }else if (cleanList.getKey() == MenuConfigManager.TV_CHANNEL_SKIP
                    || cleanList.getKey() == MenuConfigManager.TV_CHANNEL_SORT
                    || cleanList.getKey() == MenuConfigManager.TV_CHANNEL_DELETE
                    || cleanList.getKey() == MenuConfigManager.TV_CHANNEL_EDIT
                    || cleanList.getKey() == MenuConfigManager.TV_SA_CHANNEL_EDIT
                    || cleanList.getKey() == MenuConfigManager.TV_CHANNEL_MOVE
                    || cleanList.getKey() == MenuConfigManager.TV_CHANNEL_CLEAR
                    || cleanList.getKey() == MenuConfigManager.TV_FAVORITE_NETWORK){

                    if (CommonIntegration.isCNRegion()) {
                        if (CommonIntegration.getInstanceWithContext(mContext).hasActiveChannel()) {
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable true");
                          if (cleanList != null) {
                              cleanList.setVisible(true);
                          }
                        } else {
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable false");
                          if (cleanList != null ) {
                              cleanList.setVisible(false);
                          }
                        }
                      } else {
                        if (CommonIntegration.getInstanceWithContext(mContext).hasActiveChannel()) {
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable true cleanList.getKey() is "+cleanList.getKey() );
                          cleanList.setVisible(true);
                          if(cleanList.getKey() != MenuConfigManager.TV_SA_CHANNEL_EDIT){
                              MenuDataHelper.getInstance(mContext).setSkipSortEditItemHid(cleanList);
                          }
                        } else {
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable false cleanList.getKey() is "+cleanList.getKey());
                          cleanList.setVisible(false);
                        }
                      }
                      cleanList.setVisible(false);
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "end changeEnable set success");
                }else if(cleanList.getKey() == MenuConfigManager.TV_CHANNELFINE_TUNE){
                    if (CommonIntegration.getInstanceWithContext(mContext).hasActiveChannel()) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable TV_CHANNELFINE_TUNE");

                        if (CommonIntegration.isSARegion()) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable TV_CHANNELFINE_TUNE isSARegion()");
                          if (TIFChannelManager.getInstance(mContext).hasATVChannels()) {
                              cleanList.setVisible(true);
                          } else {
                              cleanList.setVisible(false);
                          }
                        }
                        if (CommonIntegration.isEURegion()) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable TV_CHANNELFINE_TUNE isEURegion()");
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable TV_CHANNELFINE_TUNE !isCurrentSourceDTV " + !mTV.isCurrentSourceDTV());
                          if (!mTV.isCurrentSourceDTV()) {
                              cleanList.setVisible(true);
                          } else {
                              cleanList.setVisible(false);
                          }

                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mTV.isTurkeyCountry()> "+mTV.isTurkeyCountry() +"dvbs_operator_name_tivibu > "+ScanContent.getDVBSCurrentOPStr(mContext)+"  >>   "+mContext
                                  .getString(R.string.dvbs_operator_name_tivibu));
                          if(mTV.isTurkeyCountry() && ScanContent.getDVBSCurrentOPStr(mContext).equalsIgnoreCase(mContext
                                  .getString(R.string.dvbs_operator_name_tivibu))){
                              cleanList.setVisible(false);
                          }else{
                              cleanList.setVisible(true);
                          }
                        }
                   //     setSkipSortEditItemHid(channelskip, channelSort, channelEdit);
                      } else {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeEnable false TV_CHANNELFINE_TUNE");
                        cleanList.setVisible(false);
                      }
                    cleanList.setVisible(false);
                }
             }
             //DTV02082547 reset to broadcast type
             SaveValue.getInstance(mContext).saveValue(CommonIntegration.CH_TYPE_BASE+CommonIntegration.getInstance().getSvl(),0);
             SaveValue.getInstance(mContext).saveValue(CommonIntegration.CH_TYPE_SECOND,-1);
             break;
         case MenuConfigManager.TUNER_MODE:
             if (CommonIntegration.isUSRegion()) {
                 Preference tunermode = null;
                 for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                     tunermode = mScreen.getPreference(i);
                    if (tunermode.getKey() == MenuConfigManager.CHANNEL_CUSTOMIZE_CHANNEL_LIST ){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "US change tuner");
                        if (CommonIntegration.getInstanceWithContext(mContext).hasActiveChannel(true)){
                            tunermode.setVisible(true);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "US show customize");
                        } else {
                            tunermode.setVisible(false);
                        }
                    }
                 }
             }else if(CommonIntegration.isEURegion()){
                 boolean isCLTypeShow = mTV.isConfigVisible(MenuConfigManager.CHANNEL_LIST_TYPE);
                 String profileName = MtkTvConfig.getInstance().
                         getConfigString(MenuConfigManager.CHANNEL_LIST_SLOT);
                 if ((isCLTypeShow && !TextUtils.isEmpty(profileName)) || mConfigManager.getDefault(MenuConfigManager.CHANNEL_LIST_TYPE) > 0 ) {
                     String[] channelListType = mContext.getResources().getStringArray(
                             R.array.menu_tv_channel_listtype);
                     int channellisttypevalue= mConfigManager.getDefault(MenuConfigManager.CHANNEL_LIST_TYPE);
                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MenuConfigManager.CHANNEL_LIST_TYPE add");
                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "profileName :" +profileName+" channellisttypevalue is "+channellisttypevalue);
                     if(!TextUtils.isEmpty(profileName)){
                         channelListType[1]= profileName;
                     }
                     ListPreference listPreference=(ListPreference)SettingsPreferenceScreen.tvChannelListType;
                     listPreference.setEntries(channelListType);
                     listPreference.setEntryValues(PreferenceUtil.getCharSequence(channelListType.length));
                     SettingsPreferenceScreen.tvChannelListType.setSummary(channelListType[channellisttypevalue]);
                     SettingsPreferenceScreen.tvChannelListType.setVisible(true);
                 }
             }
             TVAsyncExecutor.getInstance().execute(new Runnable() {
                 @Override
                   public void run() {
                     TIFChannelManager.getInstance(mContext).getChannelListForFindOrNomal();
                   }
               });
             CommonIntegration.getInstance().getChannelAllandActionNum();
             break;

         case MenuConfigManager.CHANNEL_LCN:
             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MenuConfigManager.CHANNEL_LCN:");
             if(mTV.getConfigValue(MenuConfigManager.CHANNEL_LCN) !=0 &&
                     !CommonIntegration.getInstance().isCurrentSourceATVforEuPA() ){
                 Preference lcn = null;
                 for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                     lcn = mScreen.getPreference(i);
                     if (lcn.getKey() == MenuConfigManager.TV_CHANNEL_SORT
                             || lcn.getKey() == MenuConfigManager.TV_CHANNEL_MOVE){
                         lcn.setVisible(false);
                     }
                 }
             }
             break;
         case MenuConfigManager.CHANNEL_LIST_TYPE:
             int channelListTypeValue = Integer.parseInt((String)newValue);
             Preference tvChannelListType = null;
             for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                 tvChannelListType = mScreen.getPreference(i);
                 if(tvChannelListType.getKey() == itemID){
                     String[] channelListType = mContext.getResources().getStringArray(
                             R.array.menu_tv_channel_listtype);
                     if(channelListTypeValue == 0){
                         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MenuConfigManager.CHANNEL_LIST_TYPE, profileName : " +channelListType[0]);
                         tvChannelListType.setSummary(channelListType[0]);
                     } else {
                         String profileName = MtkTvConfig.getInstance().
                                 getConfigString(MenuConfigManager.CHANNEL_LIST_SLOT);
                         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MenuConfigManager.CHANNEL_LIST_TYPE, profileName : " +profileName);
                         tvChannelListType.setSummary(profileName);
                     }
                     break;
                 }

             }
             break;
         default:
             break;
        }

        //swtich

        //CTS verify start
        if(itemID.startsWith(MenuConfigManager.PARENTAL_TIF_CONTENT_RATGINS_SYSTEM)){
            String[] strInfo = itemID.split("\\|");
            String prefix = MenuConfigManager.PARENTAL_TIF_RATGINS_SYSTEM_CONTENT +
                        "|" + strInfo[strInfo.length - 2];

            for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                Preference tempPre = mScreen.getPreference(i);

                if(!tempPre.getKey().startsWith(prefix)){
                    continue;
                }

                tempPre.setEnabled((int)newValue == 1);
                if((int)newValue == 0) {
                    ((SwitchPreference)tempPre).setChecked(false);
                }
            }
        }
        if(itemID.equals(MenuConfigManager.POWER_SETTING_CONFIG_VALUE)){
        	for(int i = 0;i < mScreen.getPreferenceCount(); i++){
                Preference tempPre = mScreen.getPreference(i);
                if(tempPre.getKey().equals(MenuConfigManager.POWER_SETTING_CONFIG_VALUE)){
                	 ListPreference listPreference=(ListPreference )tempPre;

                	listPreference.setSummary(listPreference.getEntries()[Integer.valueOf((String)newValue)]);
                	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "power setting: " +listPreference.getSummary());
                }
        	}
        }
        //CTS verify end
    }

    public void setTVItemsVisibility(PreferenceScreen preferenceScreen) {
      boolean startTag = false;
      String tabTV = mContext.getResources().getString(R.string.menu_tab_tv);
      String tabSetUp = mContext.getResources().getString(R.string.menu_tab_setup);
      for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
        Preference tempPre = preferenceScreen.getPreference(i);
        if (tempPre.getKey().equals(tabTV) && tempPre instanceof PreferenceCategory) {
          startTag = true;
        } else if (tempPre.getKey().equals(tabSetUp) && tempPre instanceof PreferenceCategory) {
          startTag = false;
          break;
        }
        if (startTag) {
          if (mTV.isCurrentSourceTv() && !mTV.isCurrentSourceBlocking()) {
            tempPre.setVisible(true);
          } else {
            tempPre.setVisible(false);
          }
        }
      }
    }
    Action satelliteManualTuning;

    /**
     * pause
     */
    public synchronized void resume() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "resume()");
        int index = 0;
        if (null == mScreen || SettingsPreferenceScreen.getInstance() == null) {
            return;
        }
        setTVItemsVisibility(mScreen);

        SettingsPreferenceScreen.getInstance().notifyPreferenceForVideo();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mScreen.getKey "+mScreen.getKey());
        if (MenuConfigManager.SETUP_DIGITAL_STYLE.equals(mScreen.getKey())) {
            int csDef =mTV.getConfigValue(MenuConfigManager.SETUP_CAPTION_STYLE) ;
            if (1 == csDef){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");//setCaptionStyle(csDef);
            }
        } else if (MenuConfigManager.CAPTION.equals(mScreen.getKey())){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");//MtkTvATSCCloseCaption.getInstance().atscCCDemoSet(0, 0);
        }

        MenuDataHelper mHelper = MenuDataHelper.getInstance(mContext);
        boolean isM7HasNumExceed4K = false;
        if (mConfigManager.getDefault(MenuConfigManager.TUNER_MODE) == 2 && mTV.isM7ScanMode()) {
            isM7HasNumExceed4K = mHelper.isM7HasNumExceed4K();
        }

        for (int i = 0; i < mScreen.getPreferenceCount(); i++) {
            Preference tempPre = mScreen.getPreference(i);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mScreen.getPreferenceCount() "+mScreen.getPreferenceCount());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mScreen.getPreference(i) "+tempPre.getKey());

            String tabVideo = mContext.getResources().getString(R.string.menu_tab_video);
            if(tempPre.getKey().equals(tabVideo) && tempPre instanceof PreferenceCategory) {
            	Preference prefVgaMode = mScreen.findPreference(MenuConfigManager.VGA_MODE);
            	Preference prefVga = mScreen.findPreference(MenuConfigManager.VGA);
            	if(!(prefVgaMode.isVisible() || prefVga.isVisible())) {
            		tempPre.setVisible(false);
            	} else {
            		tempPre.setVisible(true);
            	}
            }

            CommonIntegration ci = CommonIntegration.getInstance();
		    if(tempPre.getKey().startsWith(MenuConfigManager.SETUP_OAD_SETTING)){
				if (SaveValue.getInstance(mContext).readBooleanValue(MenuConfigManager.PVR_START)
					|| SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)
                    ||mTV.isCurrentSourceATV()){
					tempPre.setEnabled(false);
				} else if((ci.isEURegion() && !ci.isEUPARegion() && ci.isCurrentSourceDTV()) ||
                        (ci.isEUPARegion() && ci.isCurrentSourceDTVforEuPA()) && !ci.is3rdTVSource()){
                    tempPre.setEnabled(true);
                } else
				{
				    tempPre.setEnabled(false);
				}

		    }

            if(tempPre.getKey().equals(MenuConfigManager.TV_EU_CHANNEL)||
                    tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL)){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isPipOrPopState() ="+CommonIntegration.getInstance().isPipOrPopState());
                if(CommonIntegration.getInstance().isPipOrPopState() || mTV.isCanScan()){
                    tempPre.setEnabled(false);
                }else {
                    tempPre.setEnabled(true);
                }
                if(EditChannel.getInstance(mContext).getBlockChannelNumForSource() > 0 /*||
                        TVContent.getInstance(mContext).getRatingEnable() == 1*/
                        || (mTV.getCurrentTunerMode() == 1 && (ScanContent.isZiggoUPCOp() || ScanContent.isVooOp()))){
                    SettingsPreferenceScreen.getInstance().mScanPreferenceClickListener.setActionId(tempPre.getKey());
                    tempPre.setOnPreferenceClickListener(SettingsPreferenceScreen.getInstance().mScanPreferenceClickListener);
                }else {
                    SettingsPreferenceScreen.getInstance().mScanPreferenceClickListener.setActionId(null);
                    tempPre.setOnPreferenceClickListener(null);
                }
            }
            
            if(CommonIntegration.isEURegion() && mTV.isIDNCountry()){
                if(tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_SCAN_DVBT)
                        || tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_SCAN_DVBC)){
                    tempPre.setOnPreferenceClickListener(SettingsPreferenceScreen.getInstance().mScanPreferenceClickListener);
                }
            }
            
            if(tempPre.getKey().equals(MenuConfigManager.CHANNEL_DVBT_SCAN_TYPE) ||
                    tempPre.getKey().equals(MenuConfigManager.CHANNEL_DVBT_STORE_TYPE) ||
                    tempPre.getKey().equals(MenuConfigManager.CHANNEL_DVBC_SCAN_TYPE) ||
                    tempPre.getKey().equals(MenuConfigManager.CHANNEL_DVBC_STORE_TYPE)){
                if(CommonIntegration.getInstance().isCurrentSourceATVforEuPA() || 
                        (mTV.isCOLRegion() && mTV.getCurrentTunerMode() == 1)){
                    tempPre.setVisible(false);
                }else {
                    tempPre.setVisible(true);
                    if(mTV.getCurrentTunerMode() == 1 && ScanContent.isZiggoUPCOp()){//DTV03022859
                        tempPre.setEnabled(false);
                    }else {
                        tempPre.setEnabled(true);
                    }
                }

            }

            if(MenuConfigManager.TV_DVBC_SINGLE_RF_SCAN.equals(tempPre.getKey())) {
                if(mTV.getCurrentTunerMode() == 1 && (ScanContent.isZiggoUPCOp() || ScanContent.isAkadoOp()
                        || ScanContent.isOnlimeOp() || ScanContent.isRostelecomSpbOp())){
                    tempPre.setVisible(false);
                }else {
                    tempPre.setVisible(true);
                }
            }

            if(MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LONGITUDE.equals(tempPre.getKey())
                    || MenuConfigManager.DVBS_DETAIL_DISEQC_MOVEMENT_LATITUDE.equals(tempPre.getKey())){
                String summ = mConfigManager.getUSALSDescription(mConfigManager.getDefault(tempPre.getKey()), tempPre.getKey());
                tempPre.setSummary(summ);
            }

            if(MenuConfigManager.DVBS_SAT_ATENNA_TYPE_USERDEF.equals(tempPre.getKey())){
                String value = ((ListPreference)(mScreen.findPreference(MenuConfigManager.DVBS_SAT_ATENNA_TYPE_BANDFREQ))).getValue();
                if(mContext.getString(R.string.dvbs_band_freq_user_define).equals(value)){
                    List<SatelliteInfo> list = ScanContent.getDVBSsatellites(mContext);
                    int bandFreq = !list.isEmpty() ? list.get(0).getBandFreq() : 0;
                    tempPre.setSummary(bandFreq+"");
                    tempPre.setVisible(true);
                }else {
                    tempPre.setVisible(false);
                }
            }

            if(tempPre.getKey().equals(MenuConfigManager.SETUP_AUTO_CHANNEL_UPDATE) ||
                    tempPre.getKey().equals(MenuConfigManager.SETUP_CHANNEL_UPDATE_MSG)){//DTV03022861
                if(mTV.needDisableChannelUpdateUI()){
                    tempPre.setEnabled(false);
                }else {
                    tempPre.setEnabled(true);
                }
            }


            String tabAudio = mContext.getResources().getString(R.string.menu_tab_audio);
            if(tempPre.getKey().equals(tabAudio) && tempPre instanceof PreferenceCategory) {
//            	new Timer().schedule(new TimerTask() {
//					@Override
//					public void run() {
            	boolean flag = false;
            	boolean visible = false;
            	if(mTV.isCurrentSourceBlocking()) {
            		flag = false;
            	} else {
            		flag = true;
            	}
            	Preference pref = mScreen.findPreference(MenuConfigManager.SOUND_TRACKS);
                if(pref != null)
                {
                   /* boolean enable = mTV.isConfigEnabled(MenuConfigManager.SOUNDTRACKS_GET_ENABLE);
                    if(CommonIntegration.isEURegion() && flag
                        && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SET_AUDIO_TRACK_SUPP)
                        && mTV.isCurrentSourceDTV() *//*&& enable*//*) {
                        pref.setVisible(true);
                        visible = true;
                    } else {
                        pref.setVisible(false);
                    }*/

                    if(CommonIntegration.getInstance().hasActiveChannel()){
                        pref.setEnabled(true);

                        TifTimeShiftManager mTimeShiftManager = TurnkeyUiMainActivity
                                .getInstance().getmTifTimeShiftManager();
                        if(SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START) &&
                                (mTimeShiftManager != null && mTimeShiftManager.getPlayStatus() == mTimeShiftManager.PLAY_STATUS_PAUSED)){
                            pref.setEnabled(false);
                        }
                    } else {
                        pref.setEnabled(false);
                    }
                }

            	pref = mScreen.findPreference(MenuConfigManager.TYPE);
				if(pref != null)
				{
					if(!CommonIntegration.isCNRegion() && mTV.isCurrentSourceTv() && flag
									&& mTV.isConfigVisible(MenuConfigManager.CFG_MENU_AUDIO_AD_TYP)) {
						pref.setVisible(true);
						visible = true;

					} else {
						pref.setVisible(false);
					}
				}


            	pref = mScreen.findPreference(MenuConfigManager.VISUALLY_IMPAIRED);
				if(pref != null)
				{
					if(!CommonIntegration.isCNRegion() && mTV.isCurrentSourceTv() && flag
							&& mTV.isConfigVisible(MenuConfigManager.CFG_MENU_AUDIO_AD_TYP)) {
						pref.setVisible(true);
						visible = true;
						if ( MenuConfigManager.getInstance(mContext.getApplicationContext())
								.getDefault(MenuConfigManager.TYPE) == 2
								&& mTV.isConfigEnabled(MenuConfigManager.CFG_MENU_AUDIO_AD_TYP)) {
							pref.setEnabled(true);
						} else {
							pref.setEnabled(false);
						}
					} else {
						if(pref != null){
							pref.setVisible(false);
						}
					}
				}

                if(mScreen.findPreference(MenuConfigManager.TV_MTS_MODE) != null ||
                        mScreen.findPreference(MenuConfigManager.TV_AUDIO_LANGUAGE) != null ||
                        mScreen.findPreference(MenuConfigManager.TV_AUDIO_LANGUAGE_2) != null){
                    visible = true;
                }

            	tempPre.setVisible(visible);
					}
//            	}, 500);
//            }

            if (tempPre.getKey().startsWith(MenuConfigManager.SOUNDTRACKS_GET_STRING) && tempPre instanceof ListPreference) {
				/*ListPreference listPreference =(ListPreference )tempPre;
            	final String[] spls = tempPre.getKey().split("_");
				String trackID = spls[1];
				int currentID  = mConfigManager.getDefault(MtkTvConfigType.CFG_MENU_SOUNDTRACKS_GET_CURRENT);
				if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MODULES_WITH_TIF)) {
					if(null != trackID && currentID >= 0){
						if(trackID.equals(String.valueOf(currentID))){
							listPreference.setValue("0");
						}else{
						    listPreference.setValue("1");
						}
					}
                }*/

            	ListPreference listPreference =(ListPreference )tempPre;
            	final String[] spls = tempPre.getKey().split("_");
            	new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
				String currentID = "";
				String trackId = "";
				if (spls != null && spls.length == 2) {
					trackId = spls[1];
				}
            	if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MODULES_WITH_TIF)) {
            		if (CommonIntegration.TV_FOCUS_WIN_MAIN == CommonIntegration.getInstance().getCurrentFocus()) {
            			currentID = TurnkeyUiMainActivity.getInstance().getTvView()
            					.getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
            		} else {
            			currentID = TurnkeyUiMainActivity.getInstance().getPipView()
            					.getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
            		}
            	} else {
            		 TvProviderAudioTrackBase audioInfo=MtkTvAVMode.getInstance().getCurrentAudio();
            		if(audioInfo != null){
                        currentID = String.valueOf(audioInfo.getAudioId());
                    }
            	}
            	com.mediatek.wwtv.tvcenter.util.MtkLog.d("yajun","currentID = " + currentID);

            	if( null != currentID && currentID.length() > 0) {
            		if(currentID.equalsIgnoreCase(trackId)) {
            			listPreference.setValue("0");
            		} else {
            			listPreference.setValue("1");
            		}
            	}
					}
            	}, 500);

            /*	else {
            		int currentIndex = mTV.getConfigValue(MenuConfigManager.SOUNDTRACKS_GET_CURRENT);
            		String[] spls = tempPre.getKey().split("_");
            		int prefindex = 0;
            		if (spls != null && spls.length == 2) {
            			prefindex = Integer.parseInt(spls[1]);
            		}
            		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "soundtracks is " + currentIndex + " " + prefindex);
            		if(prefindex == currentIndex) {
            			listPreference.setValue("0");
            		} else {
            			listPreference.setValue("1");
            		}
            	} */

            }
            if (tempPre.getKey().equals(MenuConfigManager.TV_FAVORITE_NETWORK)){
                        CommonIntegration.getInstance().hasActiveChannel();
                tempPre.setEnabled(!TVContent.getInstance(mContext).isCOLRegion() && CommonIntegration.getInstance().isFavoriteNetworkEnable());
            }

            if (tempPre.getKey().equals(MenuConfigManager.TIME_DATE) ||
                tempPre.getKey().equals(MenuConfigManager.TIME_TIME) ) {
                isNeedRefreshTime = true;
                index = SaveValue.getInstance(mContext).readValue(MenuConfigManager.AUTO_SYNC);
                if (index == 1 || index == 2) {
                    tempPre.setEnabled(false);
                }else {
                    tempPre.setEnabled(true);
                }

                continue;
            }
            if(MenuConfigManager.CHANNEL_LCN.equals(tempPre.getKey())){
                if(mTV.needDisableLcnUI()){
                    tempPre.setEnabled(false);
                }else {
                    tempPre.setEnabled(true);
                }
                if(DataSeparaterUtil.getInstance().isSupportLCN()){
                    tempPre.setVisible(true);
                }else {
                    tempPre.setVisible(false);
                }
            }
            if(tempPre.getKey().startsWith(MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_CHANNELLIST)){
                int tid = Integer.parseInt(tempPre.getKey().substring(
                        MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_CHANNELLIST.length(),
                        tempPre.getKey().length()));
                String description = "";
                String[] optionValue = new String[] { "Off", "Block" };
                if (EditChannel.getInstance(mContext).getSchBlockType(tid) == 0) {
                    description = optionValue[0];
                } else {
                    description = optionValue[1];
                }
                tempPre.setSummary(description);
                continue;
            }
            if (tempPre.getKey().equals(MenuConfigManager.PARENTAL_CHANNEL_BLOCK)) {
                tempPre.setVisible(false);
            }
            if (tempPre.getKey().equals(MenuConfigManager.TKGS_SETTING)) {
                //get tuner mode 0:air, 1:cable, 2:prefer sat or general sat
//            	int tuneMode = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_SRC);
//                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tkgs_setting = "+tuneMode);
            	 boolean isPre = CommonIntegration.getInstance()
                 .isPreferSatMode();
            	 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tkgs_ispre"+isPre);
            	if(isPre){
            		 tempPre.setVisible(true);
            	}else{
            		 tempPre.setVisible(false);
            	}
            }
            if (tempPre.getKey().equals(MenuConfigManager.DVBS_SAT_MANUAL_TURNING)) {
            	 // "Automatic", "Customisable", "TKGS Off"
        		int tkgsmode = mConfigManager.getDefault(MenuConfigManager.TKGS_OPER_MODE);
        		boolean satOperOnly = CommonIntegration.getInstance()
                          .isPreferSatMode();
        		boolean isTkgs = mTV.isTKGSOperator();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "satOperOnly="+satOperOnly+"mTV.isTurkeyCountry() :"+mTV.isTurkeyCountry()+"mTV.isTKGSOperator(): "+mTV.isTKGSOperator()+"tkgsmode="+tkgsmode);
                // Satellite satelliteManualTuning
        		if(satelliteManualTuning == null){
        			 satelliteManualTuning = new Action(
                             MenuConfigManager.DVBS_SAT_MANUAL_TURNING,
                             mContext.getString(R.string.menu_s_sate_tuning),
                             MenuConfigManager.INVALID_VALUE,
                             MenuConfigManager.INVALID_VALUE,
                             MenuConfigManager.INVALID_VALUE, null,
                             MenuConfigManager.STEP_VALUE, Action.DataType.SATELITEINFO);
        		}
                List<SatItem> manualSatList = MenuDataHelper.getInstance(mContext).buildDVBSSATDetailInfo(
                        satelliteManualTuning, ScanContent.getDVBSsatellites(mContext), 1);
          		 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "manualSatList="+manualSatList.size());
          		 if (!manualSatList.isEmpty() && mConfigManager.getDefault(MenuConfigManager.CHANNEL_LIST_TYPE) == 0) {
          			 if(satOperOnly&&isTkgs&&tkgsmode == 0){
          				tempPre.setEnabled(false);
          				 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"satelliteManualTuning---->"+false);
          			 }else{
          				tempPre.setEnabled(true);
          				 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"satelliteManualTuning"+true);
          			 }
          		 }else{
          			tempPre.setEnabled(false);
          			 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"satelliteManualTuning---->"+false);
          		 }

            }
            if (tempPre.getKey().equals(MenuConfigManager.DVBS_SAT_UPDATE_SCAN)) {
            	  MtkTvScanDvbsBase base = new MtkTvScanDvbsBase();
          		  MtkTvScanDvbsBase.MtkTvSbDvbsBGMData bgmData = base.new MtkTvSbDvbsBGMData();
          		 base.dvbsGetSaveBgmData(bgmData);
          	  	 int scanTimes = bgmData.i4ScanTimes;
          	  	 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "scanTimes"+scanTimes);
          		 if(scanTimes>0 && mConfigManager.getDefault(MenuConfigManager.CHANNEL_LIST_TYPE) == 0){
          			tempPre.setEnabled(true);
          			 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVBS_SAT_UPDATE_SCAN--->"+true);
          		 }else{
          			tempPre.setEnabled(false);
          			 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVBS_SAT_UPDATE_SCAN--->"+false);
          		 }
            }

            if (tempPre.getKey().equals(MenuConfigManager.DVBS_RESCAN_SINGLE) ||
                    tempPre.getKey().equals(MenuConfigManager.DVBS_RESCAN_TONEBURST) ||
                    tempPre.getKey().equals(MenuConfigManager.DVBS_RESCAN_DISEQC11) ||
                    tempPre.getKey().equals(MenuConfigManager.DVBS_RESCAN_DISEQC12) ||
                    tempPre.getKey().equals(MenuConfigManager.DVBS_RESCAN_UNICABLE1) ||
                    tempPre.getKey().equals(MenuConfigManager.DVBS_RESCAN_UNICABLE2)){
                if(ScanContent.isPreferedSat() && ScanContent.isOnlyM7OperatorCountry(mContext)){
                    tempPre.setEnabled(false);
                }
                continue;
            }

            if (CommonIntegration.isEURegion()) {

                if (tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_SKIP) ||
                        tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_SORT) ||
                        tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_EDIT) ||
                        tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_MOVE) ||
                        tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_CLEAR) ||
                        tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_DELETE)) {
                    boolean isEnabled = CommonIntegration.getInstance().hasActiveChannel();
                    if (isEnabled && !CommonIntegration.getInstance().is3rdTVSource()) {
                        tempPre.setEnabled(true);
                        tempPre.setVisible(true);
                        //begin Op
                        if (mConfigManager.getDefault(MenuConfigManager.TUNER_MODE) == 0) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVBT channel option");
//                            if ((tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_SORT) ||
//                                    tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_MOVE))) {
//                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVBT LCN");
//                                if (mTV.getConfigValue(MenuConfigManager.CHANNEL_LCN) != 0 &&
//                                        !CommonIntegration.getInstance().isCurrentSourceATVforEuPA()) {
//                                    if (mTV.getConfigValue(MenuConfigManager.CHANNEL_LCN) == 1) {
//                                        Log.d(TAG, "DVBT lcn on");
//                                        tempPre.setVisible(false);
//                                    } else if (mTV.getConfigValue(MenuConfigManager.CHANNEL_LCN) == 2 && mTV.isNordicCountry()) {
//                                        Log.d(TAG, "DVBT lcn default and isNordicCountry");
//                                        tempPre.setVisible(false);
//                                    }
//                                }
//                            }
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVBT end");
                        } else if (mConfigManager.getDefault(MenuConfigManager.TUNER_MODE) == 1) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVBC channel option");
                            //begin Op
                            if (tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_DELETE) ||
                                    tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_EDIT)) {
                                if (ScanContent.isRCSRDSOp()) {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "is RCS/RDS");
                                    tempPre.setVisible(false);
                                }
                            }
                            if ((ScanContent.isDSmartOp() || ScanContent.isZiggoUPCOp()) && !tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_CLEAR)) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isD_SmartOp/Ziggo/Upc");
                                tempPre.setVisible(false);
                            }
                            //end Op begin LCN
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVBC LCN");
                            if ((tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_SORT) ||
                                    tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_MOVE))) {
                                if (mTV.getConfigValue(MenuConfigManager.CHANNEL_LCN) != 0 &&
                                    !CommonIntegration.getInstance().isCurrentSourceATVforEuPA()) {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVBC LCN != OFF");
                                    if (ScanContent.isCanalDigitalOp()) {
                                        tempPre.setVisible(false);
                                    }
                                }
                            }
                        } else if (mConfigManager.getDefault(MenuConfigManager.TUNER_MODE) == 2) {
                            //begin Op
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVBS channel option");
                            if (mTV.isTurkeyCountry() && ScanContent.isDVBSTivibuOP()) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isEnabled,isTurkeyCountry tivibu true");
                                tempPre.setVisible(false);
                            } else {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isEnabled,isTurkeyCountry tivibu false");
                                tempPre.setVisible(true);
                                if (tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_SKIP)) {
                                    if (!MenuDataHelper.getInstance(mContext).hasNoFavChannelForSkip(MenuConfigManager.TV_CHANNEL_SKIP)) {
                                        tempPre.setVisible(false);
                                    }
                                }
                            }
                            boolean isTKGS = ScanContent.isPreferedSat() && mTV.isTurkeyCountry() && mTV.isTKGSOperator();
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isEnabled,isTKGS " + isTKGS);
                            if (isTKGS && !tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_SKIP)) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isEnabled, mTV.getTKGSOperatorMode()" + mTV.getTKGSOperatorMode());
                                if (mTV.getTKGSOperatorMode() == 0) {
                                    tempPre.setVisible(false);
                                } else if ((tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_EDIT) ||
                                        tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_CLEAR) ||
                                        tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_DELETE)) && mTV.getTKGSOperatorMode() == 1) {
                                    tempPre.setVisible(false);
                                }
                            }

                            if (tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_DELETE) ||
                                    tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_EDIT)) {
                                if (ScanContent.isDVBSForRCSRDSOp()) {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isDVBSForRCSRDSOp");
                                    tempPre.setVisible(false);
                                }
                            }
                            if (ScanContent.isDVBSForDSmartOp() && !tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_CLEAR)) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isDVBSForD_SmartOp");
                                tempPre.setVisible(false);
                            }

                            if (mTV.isM7ScanMode() && !tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_CLEAR)) {
                                //M7 mode and channel num > 4000
                                Log.d(TAG, "DVBS M7");
                                if (!isM7HasNumExceed4K) {
                                    Log.d(TAG, "M7 has 4000 more");
                                    tempPre.setVisible(false);
                                }
                            }
                            //DVBS move sort
                            if (tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_MOVE) ||
                                    tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_SORT)) {
                                if (ScanContent.isDVBSSkyOP() || ScanContent.isDVBSCanalDigitalOP()) {
                                    tempPre.setVisible(false);
                                }
                            }
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DVBS end");
                        }
                        //end Op begin FavoriteNetwork select
                        if (!tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_CLEAR) && CommonIntegration.getInstance().isFavoriteNetworkEnable()) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isFavoriteNetworkEnable() tempPre.getKey() " + tempPre.getKey());
                            tempPre.setVisible(false);
                        }
                        //end FavoriteNetwork select
                    } else {
                        tempPre.setVisible(false);
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isEnabled,hasActivtChannel " + isEnabled);
                } else if (tempPre.getKey().equals(MenuConfigManager.TV_CHANNELFINE_TUNE)) {
                    tempPre.setEnabled(true);
                    MtkTvChannelInfoBase mtktvChannelinfo = CommonIntegration.getInstance().getCurChInfoByTIF();
                    //boolean isEnabled = CommonIntegration.getInstance().hasActiveChannel();
                    if (mtktvChannelinfo != null
                            && mtktvChannelinfo.isAnalogService()) {
                        tempPre.setVisible(true);
                    } else {
                        tempPre.setVisible(false);
                    }
                } else if (tempPre.getKey().equals(MenuConfigManager.TV_ANALOG_SCAN)) {
                    if (ScanContent.isCountryUK() || (CommonIntegration.isEUPARegion() && CommonIntegration.getInstance().isCurrentSourceDTV())) {
                        tempPre.setVisible(false);
                    } else {
                        tempPre.setVisible(true);
                    }
                }
            }
            if (CommonIntegration.isUSRegion()) {
                //if (tempPre.getKey().equals(MenuConfigManager.TUNER_MODE)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tunemode,hasActivtChannel ");
                    //for (int j = 0; j < mScreen.getPreferenceCount(); j++) {
                        //Preference tempPref = mScreen.getPreference(i);

                        if (tempPre.getKey().equals(MenuConfigManager.CHANNEL_CUSTOMIZE_CHANNEL_LIST)) {
                        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "key-------------"+tempPre.getKey());
                            tempPre.setEnabled(true);

                                    boolean isEnabled = MenuDataHelper.getInstance(mContext).hasNoFavChannelForSkip(MenuConfigManager.TV_CHANNEL_SKIP);
                                    if(isEnabled){
                                    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "key1");
                                    	tempPre.setVisible(true);
                                    }else {
                                    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "key2");
                                    	tempPre.setVisible(false);
                                    }
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isEnabled,hasActivtChannel isEnabled "+isEnabled);
                         }
                     //}
                   //}
              }


            if (CommonIntegration.isSARegion()) {
                boolean isEnabled = CommonIntegration.getInstance().hasActiveChannel();
                if (tempPre.getKey().equals(MenuConfigManager.TV_CHANNEL_SKIP)) {
                    if(isEnabled && !CommonIntegration.getInstance().is3rdTVSource()
                            && MenuDataHelper.getInstance(mContext).hasNoFavChannelForSkip(MenuConfigManager.TV_CHANNEL_SKIP) ){
                        tempPre.setVisible(true);
                    }else {
                        tempPre.setVisible(false);
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSARegion,isEnabled,hasActivtChannel "+isEnabled);
                }else if(tempPre.getKey().equals(MenuConfigManager.TV_CHANNELFINE_TUNE)){
                    tempPre.setEnabled(true);
                    if(isEnabled && !CommonIntegration.getInstance().is3rdTVSource()){
                        MtkTvChannelInfoBase mtktvChannelinfo = CommonIntegration.getInstance().getCurChInfoByTIF();
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSARegion,isEnabled,hasActivtChannel "+isEnabled);
                        if(mtktvChannelinfo != null
                        		&& mtktvChannelinfo.isAnalogService()
                        		&& !TIFFunctionUtil
    							.checkChMask(mtktvChannelinfo, TIFFunctionUtil.CH_FAKE_MASK,
    									TIFFunctionUtil.CH_FAKE_VAL)){
                            tempPre.setVisible(true);
                        }else {
                            tempPre.setVisible(false);
                        }
                    }else{
                        tempPre.setVisible(false);
                    }

                }else if(tempPre.getKey().equals(MenuConfigManager.TV_SA_CHANNEL_EDIT)){
                    if(isEnabled && !CommonIntegration.getInstance().is3rdTVSource()){
                        tempPre.setVisible(true);
                    }else {
                        tempPre.setVisible(false);
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSARegion,isEnabled,hasActivtChannel "+isEnabled);
                }
            }
            if(tempPre.getKey().equals(MenuConfigManager.SETUP_POWER_ON_CH)){
                if(CommonIntegration.getInstance().hasActiveChannel()){
                    tempPre.setEnabled(true);
                }else {
                    tempPre.setEnabled(false);
                }
            }

            else if (tempPre.getKey().equals(MenuConfigManager.SYSTEM_INFORMATION)) {
                if(mTV.isCurrentSourceDTV()){
                    tempPre.setEnabled(true);
                }else {
                    tempPre.setEnabled(false);
                }
                continue;
            }
            /*if (tempPre.getKey().equals(MenuConfigManager.TIMER1) ||
                tempPre.getKey().equals(MenuConfigManager.SETUP_POWER_ON_CH)) {
                index = mConfigManager.getDefault(MenuConfigManager.POWER_ON_TIMER);
                if (index == 1 || index ==2 ) {
                    tempPre.setEnabled(true);
                }else {
                    tempPre.setEnabled(false);
                }

                continue;
            }*/
            /*if (tempPre.getKey().equals(MenuConfigManager.TUNER_MODE)) {
                //update
                if(CommonIntegration.isEURegion()){
                    int tunerMode = CommonIntegration.getInstance().getSvl();
                     if (tunerMode == 3 || tunerMode == 4) {
                         if(mScreen.findPreference((CharSequence)MenuConfigManager.BISS_KEY) ==null&&SettingsPreferenceScreen.bissPreference!=null){
                             mScreen.addPreference(SettingsPreferenceScreen.bissPreference);
                         }
                      }else{
                          if(mScreen.findPreference((CharSequence)MenuConfigManager.BISS_KEY) !=null){
                              mScreen.removePreference(SettingsPreferenceScreen.bissPreference);
                          }
                      }
                     Log.d("BaseContentFragment","tunerMode:"+tunerMode);
                     boolean satOperOnly = CommonIntegration.getInstance().isPreferSatMode();
                     com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TKGSSetting:"+satOperOnly+ mTV.isTurkeyCountry()+ mTV.isTKGSOperator());
                     if (satOperOnly && mTV.isTurkeyCountry()) {
                         if(mScreen.findPreference((CharSequence)MenuConfigManager.TKGS_SETTING) ==null&&SettingsPreferenceScreen.tkgsSetting!=null){
                             com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "add tkgsSetting");
                             mScreen.addPreference(SettingsPreferenceScreen.tkgsSetting);
                         }
                      }else{
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "remove tkgsSetting");
                          if(mScreen.findPreference((CharSequence)MenuConfigManager.TKGS_SETTING) !=null){
                              mScreen.removePreference(SettingsPreferenceScreen.tkgsSetting);
                          }
                      }
                }
               }*/
            if (tempPre.getKey().equals(MenuConfigManager.TIMER2)) {
                index = mConfigManager.getDefault(MenuConfigManager.POWER_OFF_TIMER);
                if (index == 1 || index == 2) {
                    tempPre.setEnabled(true);
                }else {
                    tempPre.setEnabled(false);
                }

                continue;
            }
            if (tempPre.getKey().equals(MenuConfigManager.SUBTITLE_GROUP)) {
               if(CommonIntegration.isEUPARegion() && mTV.isCurrentSourceHDMI()){
                    tempPre.setEnabled(false);
                }else {
                    tempPre.setEnabled(true);
                }
                continue;
            }

            if (tempPre.getKey().equals(MenuConfigManager.SETUP_TELETEXT)) {
                if(CommonIntegration.isEUPARegion() && mTV.isCurrentSourceHDMI()){
                     tempPre.setEnabled(false);
                 }else {
                     tempPre.setEnabled(true);
                 }
                 continue;
             }

            else if (tempPre.getKey().equals(MenuConfigManager.SETUP_POWER_ONCHANNEL_LIST)) {
                index =SaveValue.getInstance(mContext).readValue(MenuConfigManager.SELECT_MODE);
                if (index == 0 ) {
                    tempPre.setEnabled(false);
                }else {
                    tempPre.setEnabled(true);
                }
                continue ;
            }

            if (tempPre instanceof ProgressPreference) {
                ProgressPreference tmp = (ProgressPreference) tempPre;
                if("vSignalQualityProgress".equals(tempPre.getKey()) || "vSignalProgress".equals(tempPre.getKey())){
                    continue;
                }
                tmp.setCurrentValue(mConfigManager.getDefault(tmp.getKey()));
            }
            else if(tempPre instanceof SwitchPreference){
				SaveValue save = SaveValue.getInstance(mContext);
                if(MenuConfigManager.AUTOMATIC_CONTENT.equals(tempPre.getKey())){
					int def = save.readWorldIntValue(mContext,"persist.vendor.sys.alphonso.acr");
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "persist.vendor.sys.alphonso.acr: " + def);
                    ((SwitchPreference)tempPre).setChecked(def == 1);
                    Preference preRecommendations = PreferenceData.getInstance(mContext.getApplicationContext())
                                                                                  .getData().findPreference(MenuConfigManager.RECOMMENDATIONS);
                    preRecommendations.setEnabled(def == 1);
                }
				else if(MenuConfigManager.RECOMMENDATIONS.equals(tempPre.getKey())){
                    int def = save.readWorldIntValue(mContext,"persist.vendor.sys.alphonso.acr.recommendations");
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "persist.vendor.sys.alphonso.acr.recommendations: " + def);
                    ((SwitchPreference)tempPre).setChecked(def == 1);
                }
            }
            else if (tempPre instanceof ListPreference) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ListPreference" );
                ListPreference tmp = (ListPreference) tempPre;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tmp.getKey(): " + tmp.getKey());
                if (tmp.getKey().equals(MenuConfigManager.SETUP_TIME_ZONE)) {
                    boolean saveValue = SaveValue.getInstance(mContext).readBooleanValue(
                            "Zone_time");
                    if (saveValue == true) {
                        tmp.setSummary("As Broadcast");
                        continue;
                    }
                    GetTimeZone getZone = GetTimeZone.getInstance(mContext);
                    String[] timezones = getZone.generateTimeZonesArray();
                    int defzone = getZone.getCurrentTimeZoneIndex();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "timezones[defzone]: " + timezones[defzone]);
                    tmp.setSummary(timezones[defzone]);
                    continue;
                }
                if (tmp.getKey().contains(MenuConfigManager.SETUP_REGION_PHILIPPINES_SETTING)) {
                    int itemPosition=SaveValue.getInstance(mContext).readValue(MenuConfigManager.SETUP_REGION_PHILIPPINES_SETTING);
                    String resArray[];
                    PreferenceScreen regionSettingScreen=SettingsPreferenceScreen.getInstance().regionSettingScreen;
                    //boolean isEcuador=mTV.isEcuadorCountry();
                    for(int p=0;p<regionSettingScreen.getPreferenceCount();p++){
                        ListPreference proPf = (ListPreference)regionSettingScreen.getPreference(p);
                        if(p==itemPosition){
                            int selectPosition=SaveValue.getInstance(mContext).readValue(MenuConfigManager.SETUP_REGION_SETTING_SELECT);
                            int resArrayindex=RegionConst.getEcuadorCityArray(itemPosition);
                            resArray= mContext.getResources().getStringArray(resArrayindex);
                            proPf.setSummary(resArray[selectPosition]);
                            proPf.setValue(String.valueOf(selectPosition));
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onItemPosition:"+p+",itemPosition:"+itemPosition+",summary:"+resArray[selectPosition]+",selectPosition:"+selectPosition);
                        }else{
                            int resArrayindex=RegionConst.getEcuadorCityArray(p);
                            resArray =mContext.getResources().getStringArray(resArrayindex);
                            proPf.setSummary("");
                            proPf.setValue(String.valueOf(-1));
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onItemPosition:"+p+",itemPosition:"+itemPosition+",summary:"+resArray[0]);
                        }
                    }
                    return;
                }
                if (tmp.getKey().contains(MenuConfigManager.SETUP_REGION_SETTING_LUZON)
                    || tmp.getKey().contains(MenuConfigManager.SETUP_REGION_SETTING_VISAYAS)
                    || tmp.getKey().contains(MenuConfigManager.SETUP_REGION_SETTING_MINDANAO)
                        ) {
                    int cityIndex[]=null;
                    if (tmp.getKey().contains(MenuConfigManager.SETUP_REGION_SETTING_LUZON)){
                        cityIndex=RegionConst.phiProsCityLuzong;
                    }else if(tmp.getKey().contains(MenuConfigManager.SETUP_REGION_SETTING_VISAYAS)){
                        cityIndex=RegionConst.phiProsCityVisayas;
                    }else{
                        cityIndex=RegionConst.phiProsCityMindanao;
                    }
                    int itemPosition=SaveValue.getInstance(mContext).readValue(MenuConfigManager.SETUP_REGION_PHILIPPINES_SETTING);
                    String resArray[];
                    PreferenceScreen regionSettingScreen=SettingsPreferenceScreen.getInstance().regionSettingScreen;
                    String pId=SaveValue.getInstance(mContext).readStrValue(MenuConfigManager.SETUP_REGION_SETTING);
                    for(int p=0;p<regionSettingScreen.getPreferenceCount();p++){
                        ListPreference proPf = (ListPreference)regionSettingScreen.getPreference(p);
                        if(p==itemPosition&&tmp.getKey().contains(pId)){
                            int selectPosition=SaveValue.getInstance(mContext).readValue(MenuConfigManager.SETUP_REGION_SETTING_SELECT);
                            int resArrayindex=cityIndex[itemPosition];
                            resArray= mContext.getResources().getStringArray(resArrayindex);
                            proPf.setSummary(resArray[selectPosition]);
                            proPf.setValue(String.valueOf(selectPosition));
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onItemPosition:"+p+",itemPosition:"+itemPosition+",summary:"+resArray[selectPosition]+",selectPosition:"+selectPosition);
                        }else{
                            int resArrayindex=RegionConst.getEcuadorCityArray(p);
                            resArray =mContext.getResources().getStringArray(resArrayindex);
                            proPf.setSummary("");
                            proPf.setValue(String.valueOf(-1));
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onItemPosition:"+p+",itemPosition:"+itemPosition+",summary:"+resArray[0]);
                        }
                    }
                    return;
                }
                index = mConfigManager.getDefault(tmp.getKey());
                PreferenceUtil util = PreferenceUtil.getInstance(mContext);

                int subIndex;

                if (tmp.getKey().equals(MenuConfigManager.TV_AUDIO_LANGUAGE)) {
                    index = util.mOsdLanguage.getAudioLanguage(tmp.getKey());
                } else if (tmp.getKey().startsWith(MenuConfigManager.PARENTAL_INPUT_BLOCK_SOURCE)){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "do nothing");//index = EditChannel.getInstance(
                    //    mContext).isInputBlock((String)tmp.getTitle()) ? 1 : 0;
                } else if (tmp.getKey().equals(MenuConfigManager.DIGITAL_SUBTITLE_LANG)){
                    subIndex = MtkTvConfig.getInstance().getConfigValue(
                            MenuConfigManager.DIGITAL_SUBTITLE_LANG_ENABLE);
                    if((subIndex == 0) || CommonIntegration.isTVSourceSeparation()&& !(mTV.isCurrentSourceDTV() || mTV.isCurrentSourceATV()||
                            mTV.isCurrentSourceComposite())){
                        tmp.setEnabled(false);
                    }else {
                        tmp.setEnabled(true);
                    }
                    index = util.mOsdLanguage.getSubtitleLanguage(tmp.getKey());
                }else if(tmp.getKey().equals(MenuConfigManager.DIGITAL_SUBTITLE_LANG_2ND)
                        || tmp.getKey().equals(MenuConfigManager.SUBTITLE_TYPE)){
                    subIndex = MtkTvConfig.getInstance().getConfigValue(
                            MenuConfigManager.DIGITAL_SUBTITLE_LANG_ENABLE);
                     //subIndex = util.mOsdLanguage.getSubtitleLanguage(MenuConfigManager.DIGITAL_SUBTITLE_LANG);
                    //int subOffIndex = ((ListPreference)mScreen.findPreference(MenuConfigManager.DIGITAL_SUBTITLE_LANG_2ND)).getEntries().length - 1;
                    if((subIndex == 0) || CommonIntegration.isTVSourceSeparation() && !(mTV.isCurrentSourceDTV() || mTV.isCurrentSourceATV()||
                            mTV.isCurrentSourceComposite())){
                        tmp.setEnabled(false);
                    }else {
                        tmp.setEnabled(true);
                    }
                    if(tmp.getKey().equals(MenuConfigManager.SUBTITLE_TYPE)){
                        index = mConfigManager.getDefault(tmp.getKey());
                    }else{
                        index = util.mOsdLanguage.getSubtitleLanguage(tmp.getKey());
                    }
                }else if (tmp.getKey().equals(MenuConfigManager.ANALOG_SUBTITLE)) {
                    if(CommonIntegration.isTVSourceSeparation() && mTV.isCurrentSourceDTV()){
                        tmp.setEnabled(false);
                    }else {
                        tmp.setEnabled(true);
                    }
                } else if (tmp.getKey().equals(MenuConfigManager.DIGITAL_SUBTITLE_LANG_ENABLE)) {
                    if (CommonIntegration.isTVSourceSeparation()&& !(mTV.isCurrentSourceDTV() || mTV.isCurrentSourceATV()||
                            mTV.isCurrentSourceComposite())) {
                        tmp.setEnabled(false);
                    }else {
                        tmp.setEnabled(true);
                    }
                } else if (tmp.getKey().equals(MenuConfigManager.SUBTITLE_TRACKS)) {
                    if (CommonIntegration.isTVSourceSeparation()&& !(mTV.isCurrentSourceDTV() || mTV.isCurrentSourceATV()||
                            mTV.isCurrentSourceComposite())) {
                        tmp.setEnabled(false);
                    }else {
                        tmp.setEnabled(true);
                    }
                    List<TvTrackInfo> tracks = TurnkeyUiMainActivity.getInstance().getTvView()
                            .getTracks(TvTrackInfo.TYPE_SUBTITLE);
                    subIndex = MtkTvConfig.getInstance().getConfigValue(
                            MenuConfigManager.DIGITAL_SUBTITLE_LANG_ENABLE);
                    /*
                     * subOffIndex = ((ListPreference)mScreen.findPreference(MenuConfigManager.
                     * DIGITAL_SUBTITLE_LANG_2ND)).getEntries().length - 1;
                     */
                    if (subIndex == 0) {
                        tmp.setSummary(mContext.getResources().getString(R.string.common_off));
                        tmp.setEnabled(false);
                        SaveValue.getInstance(mContext).saveValue(MenuConfigManager.SUBTITLE_TRACKS,255);
                        //MtkTvSubtitle.getInstance().playStream(255);
                        String offValue = String.valueOf(0XFF);
                        TurnkeyUiMainActivity.getInstance().getTvView().selectTrack(TvTrackInfo.TYPE_SUBTITLE,(String)offValue);
                        continue;
                    }
                    if( tracks == null || tracks.isEmpty()){
                        tmp.setSummary("");
                        tmp.setEnabled(false);
                        if(PreferenceUtil.getInstance(mContext).isFromSubtitleTrackNoSignal){
                            PreferenceUtil.getInstance(mContext).isFromSubtitleTrackNoSignal = false;
                            InstrumentationHandler.getInstance().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                        }
                        continue;
                    }
                    int currentID = SaveValue.getInstance(mContext).readValue(
                            MenuConfigManager.SUBTITLE_TRACKS);
                    CharSequence[] entryValues = tmp.getEntryValues();
                    CharSequence[] entries = tmp.getEntries();
                    tmp.setEnabled(true);
                    for (int subtitleIndex = 0; subtitleIndex < entryValues.length; subtitleIndex++) {
                        if (entryValues[subtitleIndex].equals(String.valueOf(currentID))) {
                            tmp.setSummary(entries[subtitleIndex]);
                            tmp.setValue(entryValues[subtitleIndex].toString());
                            break;
                        }
                    }
                    continue;
                }else if (tmp.getKey().equals(MenuConfigManager.AUTO_SYNC)) {
                            index = SaveValue.getInstance(mContext).readValue(MenuConfigManager.AUTO_SYNC);
                        } else if (tmp.getKey().equals(MenuConfigManager.SCREEN_MODE)) {
                  index = mConfigManager.getScreenMode(mConfigManager.getScreenModeList(),
                      MenuConfigManager.SCREEN_MODE);
                        }else if (tmp.getKey().equals(MenuConfigManager.SELECT_MODE)) {
                          index =SaveValue.getInstance(mContext).readValue(MenuConfigManager.SELECT_MODE);
                } else if (tmp.getKey().equals(MenuConfigManager.SLEEP_TIMER)) {
                  showSleepTimerInfo(tmp);
                  continue;
                }else if (tmp.getKey().contains(MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_OPERATION_MODE)) {
                    index = SettingsPreferenceScreen.block;
                    switch (index) {
                        case 0:
                            mScreen.getPreference(0).setSummary(tmp.getEntries()[0]);
                            mScreen.getPreference(1).setEnabled(false);
                            mScreen.getPreference(2).setEnabled(false);
                            mScreen.getPreference(3).setEnabled(false);
                            mScreen.getPreference(4).setEnabled(false);

                            break;
                        case 1:
                            mScreen.getPreference(0).setSummary(tmp.getEntries()[1]);
                            mScreen.getPreference(1).setEnabled(false);
                            mScreen.getPreference(2).setEnabled(true);
                            mScreen.getPreference(3).setEnabled(false);
                            mScreen.getPreference(4).setEnabled(true);
                            break;
                        case 2:
                            mScreen.getPreference(0).setSummary(tmp.getEntries()[2]);
                            mScreen.getPreference(1).setEnabled(true);
                            mScreen.getPreference(2).setEnabled(true);
                            mScreen.getPreference(3).setEnabled(true);
                            mScreen.getPreference(4).setEnabled(true);
                            break;

                        default:
                            break;
                    }
                } else if (tmp.getKey().startsWith(MenuConfigManager.TV_AUDIO_LANGUAGE_2)){
                 index = util.mOsdLanguage.getAudioLanguage(tmp.getKey());
                } else if (tmp.getKey().startsWith(MenuConfigManager.PARENTAL_OPEN_VCHIP_LEVEL)) {
                    int levIndex = Integer.parseInt(tmp.getKey().substring(
                            MenuConfigManager.PARENTAL_OPEN_VCHIP_LEVEL.length(),
                            tmp.getKey().length()));
                    CharSequence[] entries = tmp.getEntries();
                    byte[] levelBlicks = mTV.getNewOpenVchipSetting(SettingsPreferenceScreen.reginIndex,
                        SettingsPreferenceScreen.dimIndex).getLvlBlockData();
                    List<Preference> levels = new ArrayList<Preference>();
                    for (int j = 0; j < mScreen.getPreferenceCount(); j++) {
                        levels.add(mScreen.getPreference(j));
                    }
                    for (int m = 0; m < levelBlicks.length; m++) {
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "print levelBlicks[ " + m + "]=" + levelBlicks[m]);
                    }
                    MtkTvOpenVCHIPSettingInfoBase info = TVContent.getInstance(mContext).getNewOpenVchipSetting(SettingsPreferenceScreen.reginIndex,
                            SettingsPreferenceScreen.dimIndex);
                    byte[] block = info.getLvlBlockData();
                    int iniValue = block[levIndex];
                    tmp.setValueIndex(iniValue);
                    tmp.setSummary(entries[iniValue]);
                    continue;
                }else if(MenuConfigManager.TUNER_MODE.equals(tmp.getKey())){
                    int value = mConfigManager.getDefault(tmp.getKey());
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TUNER_MODE value="+value);
                    CharSequence[] entryValues = tmp.getEntryValues();
                    CharSequence[] entries = tmp.getEntries();
                    for (int f = 0; f < entryValues.length; f++) {
                        if (entryValues[f].equals(value+"")) {
                          tmp.setSummary(entries[f]);
                          tmp.setValue(value+"");
                        }
                      }
                    continue;
                }else if(MenuConfigManager.CHANNEL_LIST_TYPE.equals(tmp.getKey())){
                    boolean isCLTypeShow = mTV.isConfigVisible(MenuConfigManager.CHANNEL_LIST_TYPE);
                    String profileName = MtkTvConfig.getInstance().
                            getConfigString(MenuConfigManager.CHANNEL_LIST_SLOT);
                    if ((!isCLTypeShow || TextUtils.isEmpty(profileName)) && mConfigManager.getDefault(MenuConfigManager.CHANNEL_LIST_TYPE) == 0 ) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MenuConfigManager.CHANNEL_LIST_TYPE remove");
                        tmp.setVisible(false);
                        //mScreen.removePreference(tmp);
                    }
                    continue;
                }else if(MenuConfigManager.DVBS_SAT_ATENNA_TYPE_TUNER.equals(tmp.getKey())){
                    String value = ((ListPreference)tmp).getValue();
                    CharSequence[] entryValues = tmp.getEntryValues();
                    CharSequence[] entries = tmp.getEntries();
                    for (int f = 0; f < entryValues.length; f++) {
                        if (entryValues[f].equals(value)) {
                          tmp.setSummary(entries[f]);
                        }
                      }
                    continue;
                }else if(MenuConfigManager.DVBS_SAT_ATENNA_TYPE_BANDFREQ.equals(tmp.getKey())){
                    tmp.setSummary(((ListPreference)tmp).getValue());
                    continue;
                }else {
                  index = mConfigManager.getDefault(tmp.getKey());
                }
                if(MenuConfigManager.VGA_MODE.equals(tmp.getKey())) {
                	index -= 1;
                }
                index = (index < 0) ? 0 : index;
                CharSequence[] entries = tmp.getEntries();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "=== index" + index + "  tmp.getKey():" + tmp.getKey()
                    + "  entries:" + entries.length);
                if (entries.length <= index && !MenuConfigManager.TV_MTS_MODE.equals(tempPre.getKey())) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "error." + tempPre + "," + index);
                    continue;
                }
                if(MenuConfigManager.POWER_SETTING_CONFIG_VALUE.equals(tmp.getKey())){
                	index =PreferenceUtil.getInstance(mContext).mConfigManager.getDefaultPowerSetting(mContext);
                }
                if(MenuConfigManager.TV_MTS_MODE.equals(tempPre.getKey())){
                    SundryImplement sundry = SundryImplement.getInstanceNavSundryImplement(mContext);
                    String summary = sundry.getMtsSummaryByAcfgValue(index);
                    if(summary == null) {
                        ((ListPreference)tempPre).setEntries(new String[0]);
                        ((ListPreference)tempPre).setEntryValues(new String[0]);
                        tmp.setSummary("");
                        continue;
                    }
                    if("".equals(summary)){
                        mConfigManager.setValue(tempPre.getKey(), 0);
                        tmp.setSummary(entries[0]);
                    }else {
                        String[] mtsStrings = sundry.getAllMtsModes();
                        ((ListPreference)tempPre).setEntries(mtsStrings);
                        ((ListPreference)tempPre).setEntryValues(PreferenceUtil.getCharSequence(mtsStrings.length));
                        tmp.setSummary(summary);
                    }
                    continue;
                }
                tmp.setSummary(entries[index]);
            }
            else if (tempPre instanceof Preference) {
                String key = tempPre.getKey();
                if (key.contains(MenuConfigManager.TIME_START_DATE)
                    || key.contains(MenuConfigManager.TIME_START_TIME)
                    || key.contains(MenuConfigManager.TIME_END_TIME)
                    || key.contains(MenuConfigManager.TIME_END_DATE)) {
                    String dateTime = SaveValue.getInstance(mContext).readStrValue(key);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dateTime: " + dateTime);

                    tempPre.setSummary(dateTime);
                }
            }

            if(!tempPre.isEnabled()){
                tempPre.setSelectable(false);
            }else{
                tempPre.setSelectable(true);
            }
        }//for

        // add case by case flow here
        // listen the signal status
        if (MenuConfigManager.FACTORY_VIDEO.equals(mScreen.getKey())) {
            for (int i = 0; i < mScreen.getPreferenceCount(); i++) {
                Preference tempPre = mScreen.getPreference(i);
                if (MenuConfigManager.FV_AUTOCOLOR.equals(
                    tempPre.getKey())) {
                    ((FacVideo) (tempPre.getOnPreferenceClickListener())).addListener();
                }
            }
        }
    }
    public int getIndexByLeftTime(Long timeLeft) {
      if (timeLeft < 10){
          return 0;
      }
      if (timeLeft < 20) {
        return 1;
      }
      if (timeLeft < 30){
          return 2;
      }
      if (timeLeft < 40){
          return 3;
      }
      if (timeLeft < 50){
          return 4;
      }
      if (timeLeft < 60){
          return 5;
      }
      if (timeLeft < 90){
          return 6;
      }
      if (timeLeft < 120){
          return 7;
      }
      return 8;
    }

    public void showSleepTimerInfo(Preference pref){
      if (MenuConfigManager.SLEEP_TIMER.equals(pref.getKey())) {
              int remaintime = mTV.getSleepTimerRemaining();
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "remaintime=="+remaintime);
              Long timeLeft = 0L;
              if (remaintime != 0) {
                if(remaintime% 600 == 0){//time is 10 min,20 min ... need not +1
                  timeLeft = (long) mTV.getSleepTimerRemaining() / 60;
                }else{
                  timeLeft = (long) mTV.getSleepTimerRemaining() / 60 + 1;
                }
              }
              String[] optionTiemSleep = mContext.getResources()
                  .getStringArray(R.array.menu_setup_sleep_timer_array);
              if (timeLeft > 0) {
                  Pattern pattern = Pattern.compile("^\\d*\\d");
                  Matcher matcher = pattern
                          .matcher(optionTiemSleep[optionTiemSleep.length - 1]);
                  pref.setSummary(matcher.replaceFirst(String
                          .valueOf(timeLeft)));
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "leftTimer------------------------>"
                          + matcher.replaceFirst(String.valueOf(timeLeft)));
              } else {
                pref.setSummary(optionTiemSleep[0]);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "not  timeLeft>0");
              }
          }else{
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "not sleeptimer");
          }
    }

    public void handleSleepTimerChange(ListPreference tmp, int value) {
      int remaintime = mTV.getSleepTimerRemaining();
      int lastValue = 0;
      Long timeLeft = 0L;
      if (remaintime != 0) {
        timeLeft = (long) mTV.getSleepTimerRemaining() / 60 + 1;
      }
      if (timeLeft > 0) {
        lastValue = getIndexByLeftTime(timeLeft);
      } else {
        lastValue = 0;
      }
      if (remaintime != 0) {
        timeLeft = (long) mTV.getSleepTimerRemaining() / 60 + 1;
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleSleepTimerChange value:" + value
          + "  lastValue:" + lastValue);
      // for sleep timer need to call mTV.setSleepTimer many times
      if (value > lastValue) {
        if (lastValue == 0 && value == tmp.getEntries().length - 1) {
          mTV.setSleepTimer(false);
        } else {
          int times = value - lastValue;
          while (times > 0) {
            times--;
            mTV.setSleepTimer(true);
          }
        }
      } else {
        if (value == 0 && lastValue == tmp.getEntries().length - 1) {
          mTV.setSleepTimer(true);
        } else {
          int times = lastValue - value;
          while (times > 0) {
            times--;
            mTV.setSleepTimer(false);
          }
        }
      }
    }

    /**
     * pause
     */
    public synchronized void pause() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"pause...");
        // add case by case flow here
        // listen the signal status
        if (MenuConfigManager.FACTORY_VIDEO.equals(mScreen.getKey())) {
            for (int i = 0; i < mScreen.getPreferenceCount(); i++) {
                Preference tempPre = mScreen.getPreference(i);
                if (MenuConfigManager.FV_AUTOCOLOR.equals(
                    tempPre.getKey())) {
                    ((FacVideo) (tempPre.getOnPreferenceClickListener())).removeListener();
                }
            }
        }

        //for sleep timer to update ui
        for (int i = 0; i < mScreen.getPreferenceCount(); i++) {
          Preference tempPre = mScreen.getPreference(i);
          if (tempPre.getKey().equals(MenuConfigManager.TIME_DATE) ||
                  tempPre.getKey().equals(MenuConfigManager.TIME_TIME) ) {
              isNeedRefreshTime = false;
          }
          if (tempPre.getKey().equals(MenuConfigManager.SLEEP_TIMER)) {
            ListPreference sleepTimerPref = (ListPreference)tempPre;
            int remaintime = mTV.getSleepTimerRemaining();
            Long timeLeft = 0L;
            if (remaintime != 0) {
              timeLeft = (long) mTV.getSleepTimerRemaining() / 60 + 1;
            }
            if (timeLeft > 0) {
              int value = PreferenceData.getInstance(mContext.getApplicationContext())
                  .getIndexByLeftTime(timeLeft);
              if (value == 0) {
                value = 1;
              }
              sleepTimerPref.setValueIndex(value);
            } else {
              sleepTimerPref.setValueIndex(0);
            }
            break;
          }
        }
    }

    public void setCaptionStyle(int value){
        MtkTvATSCCloseCaption.getInstance().atscCCDemoSet(0, value);
    }

}
