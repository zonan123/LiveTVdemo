package com.mediatek.wwtv.tvcenter.nav.util;

import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import com.mediatek.twoworlds.tv.MtkTvAVMode;
import com.mediatek.twoworlds.tv.MtkTvGinga;
import com.mediatek.twoworlds.tv.MtkTvMHEG5;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvAppTV;
import com.mediatek.twoworlds.tv.MtkTvCI;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.model.MtkTvAudioApdBase;
import com.mediatek.twoworlds.tv.model.TvProviderAudioTrackBase;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.util.LanguageUtil;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.Constants;

import android.content.Context;
import android.media.AudioManager;
import android.media.tv.TvTrackInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.content.ContentResolver;
import com.mediatek.wwtv.setting.util.TVContent;


public final class SundryImplement {
    private static String TAG = "SundryImplement";
    public static final String A_MAIN_AUDIO = "a_main_audio";
    public static final String A_MAIN_AUDIO_HAS_AD = "a_main_audio_has_ad";
    public static final String A_MAIN_AUDIO_HAS_SPS = "a_main_audio_has_sps";
    public static final String A_MAIN_AUDIO_HAS_AD_SPS = "a_main_audio_has_ad_sps";
    public static final String A_HI_AUDIO = "a_hi_audio";
    public static final String A_BROADCAST_MIXED_VI_AUDIO = "a_broadcast_mixed_vi_audio";
    public static final String A_RECEIVE_MIX_VI = "a_receive_mix_vi";
    public static final String A_RECEIVE_MIXED_VI_MAIN="a_receive_mixed_vi_main";
    public static final String A_RECEIVE_MIXED_VI_MAIN_ID="a_receive_mixed_vi_main_id";
    public static final String A_RECEIVE_MIXED_VI_MAIN_MESSAGE_ID="a_receive_mixed_vi_main_msg_id";
    public static final String A_DISPLAY_AUDIO_LANGUAGE = "a_display_audio_language";
    public static final String A_SPS_MIX_AUDIO = "a_sps_mix_audio";
    public static final String A_SPS_MIXED_MAIN = "a_sps_mixed_main";
    public static final String A_SPS_MIXED_MAIN_ID = "a_sps_mixed_main_id";
    public static final String A_AD_SPS_MIX_AUDIO = "a_ad_sps_mix_audio";
    public static final String A_AD_SPS_MIXED_MAIN_AUDIO = "a_ad_sps_mixed_main_audio";
    public static final String A_AD_SPS_MIXED_MAIN_ID_AUDIO = "a_ad_sps_mixed_main_id_audio";
        /**Virtual X*/
    public static final String KEY_SOUND_DTS_VIRTUAL_X = "sound_dts_virtual_x";
    /**Sonic Emotion*/
    public static final String KEY_SONIC_EMOTION = "sound_sonic_emotion";
    /**DBX Enable*/
    public static final String KEY_SOUND_DBX_ENABLE = "sound_dbx_enable";
    //Sound -- dap
    public static final String KEY_SOUND_ADVANCED_DOLBY_AP = "sound_advanced_dolby_ap";
    /** Enable*/
    public static final String KEY_SOUND_DTS_STUDIO_ENABLE = "sound_dts_studio_enable";
    private static final int SCDB_OPTION_MASK_AC4_PRE_STRM_COMP = 8;
    private static final int SCDB_OPTION_MASK_AC4_AUX_STREAM_COMP = 4;

    private Context mContext;
    private static SundryImplement instance;
    private   MtkTvAVMode navMtkTvAVMode;
//    private static MtkTvTime navMtkTvTime;
    private   MtkTvGinga navMtkTvGinga;
    private   MtkTvMHEG5 navMtkTvMHEG5;
    private MtkTvAppTV mtkTvAppTv;
    private List<TvTrackInfo> mMtsAudioTracks;
//    private TvView mainTvView,subTvView;
    private CommonIntegration mCommonIntegration;
    private InputSourceManager mInputSourceManager;

  public String[] mtsAudioMode;
  Map<String, String> audioSpecialMap;
    public List<TvTrackInfo> getmMtsAudioTracks() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getmMtsAudioTracks");
		return mMtsAudioTracks;
	}

	public void setmMtsAudioTracks(List<TvTrackInfo> mMtsAudioTracks) {
		this.mMtsAudioTracks = mMtsAudioTracks;
	}

    private SundryImplement(Context context) {
        mContext = context;
        navMtkTvAVMode = MtkTvAVMode.getInstance();
//        navMtkTvTime = MtkTvTime.getInstance();
        navMtkTvGinga = MtkTvGinga.getInstance();
        navMtkTvMHEG5 = MtkTvMHEG5.getInstance();
        mtkTvAppTv = MtkTvAppTV.getInstance();
//        if(TurnkeyUiMainActivity.getInstance() != null){
//        	mainTvView = TurnkeyUiMainActivity.getInstance().getTvView();
//        	subTvView = TurnkeyUiMainActivity.getInstance().getPipView();
//        }
		mCommonIntegration = CommonIntegration.getInstance();
    mInputSourceManager = InputSourceManager.getInstance();
    mtsAudioMode = mContext.getResources().getStringArray(
        R.array.nav_mts_strings);
    //load special audio map
    {
      audioSpecialMap  = new HashMap<String, String>();
//      audioSpecialMap.put("und", "Undefined audio");
//      if (com.mediatek.wwtv.setting.util.TVContent.getInstance(
//              mContext).isFraCountry()) {
//          audioSpecialMap.put("qaa", "V.O.");
//      }else{
//          audioSpecialMap.put("qaa", "Original audio");
//      }
//      audioSpecialMap.put("qab", "Second audio");
//      audioSpecialMap.put("qac", "Third audio");
//      audioSpecialMap.put("mul", "Multiple");
//      audioSpecialMap.put("wel","Welsh");
//      audioSpecialMap.put("per", "Persian");
//      audioSpecialMap.put("ger", "German");
//      audioSpecialMap.put("dut", "Dutch");
//      audioSpecialMap.put("fre", "French");
//      audioSpecialMap.put("nar", "Narrator");
//      audioSpecialMap.put("esl", "Spanish");
//      audioSpecialMap.put("chi", "Chinese");
//      audioSpecialMap.put("msa", "Bahasa Melayu");
//      audioSpecialMap.put("qad", "Narrator");
//      audioSpecialMap.put("deu", "Deutsch");
//	  audioSpecialMap.put("baq","Basque");
    }
  }

    public static synchronized SundryImplement getInstanceNavSundryImplement(
            Context context) {   	
        if (instance == null) {
            instance = new SundryImplement(context.getApplicationContext());
        }
        return instance;
    }
    
    public static synchronized void setInstanceNavSundryImplementNull() {
        instance = null;
    }

    public boolean isHeadphoneSetOn() {
      AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
      boolean result = audioManager.isWiredHeadsetOn();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isHeadphoneSetOn:"+result);
      return result;
    }
    
    public List<TvTrackInfo> getTrackList() {
        List<TvTrackInfo> audioTracks = TurnkeyUiMainActivity.getInstance().getTvView().getTracks(TvTrackInfo.TYPE_AUDIO);
        if(audioTracks == null) {
            return new ArrayList<TvTrackInfo>();
        }
        return filterAudioTracksForAC4(audioTracks);
    }

    //public TvTrackInfo getCurrentTvTrackInfo() {
    //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentTvTrackInfo");
    //    TvTrackInfo result = null;
    //    List<TvTrackInfo> audioTracks = TurnkeyUiMainActivity.getInstance().getTvView().getTracks(TvTrackInfo.TYPE_AUDIO);
    //    if(audioTracks == null) {
    //        return result;
    //    }
    //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "audioTrack size:" + audioTracks.size());
    //    if (audioTracks.isEmpty()) {
    //        if (mCommonIntegration.isCurrentSourceATV()) {
    //            result = getCurrentATvTrackInfo();
    //        }
    //    } else {
    //        if (mCommonIntegration.isCurrentSourceATV()) {
    //            result = getCurrentATvTrackInfo();
    //        } else if(mCommonIntegration.isCurrentSourceDTV()){
    //            audioTracks = filterAudioTracksForAC4(audioTracks);
    //            String currentIdString = getCurrentTvTrackId();
    //            result = getTvTrackInfoByTrackId(currentIdString, audioTracks);
    //            String lang = getAudioTrackLangById(currentIdString,audioTracks);
    //            result.getExtra().putString(A_DISPLAY_AUDIO_LANGUAGE, lang);
    //        }
    //    }
    //    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TvTrackInfo:lang:"+result.getLanguage()+
    //            " id:"+result.getId()+" Bundle:"+(result.getExtra() == null ? "null":result.getExtra().toString()));
    //    return result;
    //}

    public String getCurrentTvTrackId() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getCurrentTvTrackId");
        String curTrackId = TurnkeyUiMainActivity.getInstance().getTvView().getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getTvView().getSelectedTrack:"+curTrackId);
        TvProviderAudioTrackBase currentAd = navMtkTvAVMode.getCurrentAd();
        if(currentAd != null) {
            curTrackId = String.valueOf(currentAd.getAudioId());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"navMtkTvAVMode.getCurrentAd:"+curTrackId);
        }
        return curTrackId;
    }

    public TvTrackInfo getTvTrackInfoByTrackId(String id, List<TvTrackInfo> tracks) {
        TvTrackInfo result = null;
        for(TvTrackInfo track : tracks) {
            if(track.getId().equals(id)) {
                result = track;
                break;
            }
        }
        return result;
    }

    private TvTrackInfo getCurrentATvTrackInfo() {
        String currentAudioLang = navMtkTvAVMode.getAudioLang();
        Bundle bundle = new Bundle();
        bundle.putString(A_DISPLAY_AUDIO_LANGUAGE, currentAudioLang);
        return new TvTrackInfo.Builder(TvTrackInfo.TYPE_AUDIO, "-1").setExtra(bundle).build();
    }

    //public String getCurrentAudioLangForAC4() {
    //    String result = "";
    //    TvTrackInfo currentTvTrackInfo = getCurrentTvTrackInfo();
    //    if(currentTvTrackInfo != null && currentTvTrackInfo.getExtra() != null) {
    //        result = currentTvTrackInfo.getExtra().getString(A_DISPLAY_AUDIO_LANGUAGE);
    //    }
    //    return result;
    //}

    /**
     * get the current audio language
     * @return
     */
    public synchronized String getCurrentAudioLang(){
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentAudioLang");
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "navMtkTvAVMode.getAudioLang():"+navMtkTvAVMode.getAudioLang());
    String currentAudioLang = "";
    if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MODULES_WITH_TIF)) {
      List<TvTrackInfo> audioTracks ;
      if (CommonIntegration.TV_FOCUS_WIN_MAIN.equals(mCommonIntegration.getCurrentFocus())) {// main
        audioTracks = TurnkeyUiMainActivity.getInstance().getTvView().getTracks(TvTrackInfo.TYPE_AUDIO);
      } else {// sub
        audioTracks = TurnkeyUiMainActivity.getInstance().getPipView().getTracks(TvTrackInfo.TYPE_AUDIO);
      }
      if(audioTracks==null){
    	return currentAudioLang;
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "audioTrack size:" + audioTracks.size());
      if (audioTracks.isEmpty()) {
	  	if (mInputSourceManager.isCurrentTvSource(mCommonIntegration.getCurrentFocus())
            && mInputSourceManager.isCurrentAnalogSource(mCommonIntegration.getCurrentFocus())&& !mCommonIntegration.is3rdTVSource()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ATV Source.return null");
          currentAudioLang = null;
        }else {
            currentAudioLang = mContext.getResources().getString(R.string.nav_no_function);
        }
        return currentAudioLang;
      }else {
          audioTracks = filterAudioTracks(filterAudioTracksForAC4(audioTracks));//filterAudioTracks(audioTracks);//
    	  String curTrackId = "";
          if (CommonIntegration.TV_FOCUS_WIN_MAIN.equals(mCommonIntegration.getCurrentFocus())) {
            curTrackId = TurnkeyUiMainActivity.getInstance().getTvView().getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
          } else {
            curTrackId = TurnkeyUiMainActivity.getInstance().getPipView().getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
          }
        if (!CommonIntegration.getInstance().is3rdTVSource()&&mInputSourceManager.isCurrentTvSource(mCommonIntegration.getCurrentFocus())
            && mInputSourceManager.isCurrentAnalogSource(mCommonIntegration.getCurrentFocus())) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ATV Source");
          try{
        	  currentAudioLang = (Integer.parseInt(curTrackId)<14)?mtsAudioMode[Integer.parseInt(curTrackId)]:
        		  mtsAudioMode[0]  ;//navMtkTvAVMode.getAudioLang();
        	  }catch(NumberFormatException e){
        		  e.printStackTrace();
        	}
        } else if ((mInputSourceManager.isCurrentTvSource(mCommonIntegration.getCurrentFocus()) &&
        			!mCommonIntegration.isCurCHAnalog()) ||
                    mCommonIntegration.is3rdTVSource()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DTV source");
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "curTrackId:"+curTrackId);
          currentAudioLang = getAudioTrackLangById(curTrackId, audioTracks);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentAudioLang:"+currentAudioLang);
      }
    } else {
      currentAudioLang = navMtkTvAVMode.getAudioLang();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getCurrentAudioLang,text = " + currentAudioLang);
    }
		return currentAudioLang;
    }
    
    public String[] getAllMtsModes() {        
        return getAllMtsModesList().toArray(new String[0]);
    }
    
    private List<String> getAllMtsModesList() {
        List<TvProviderAudioTrackBase> tracks = navMtkTvAVMode.getAudioAvailableRecord();
        List<String> modes = new ArrayList<String>();
        for (TvProviderAudioTrackBase tvProviderAudioTrackBase : tracks) {
            int audioId = tvProviderAudioTrackBase.getAudioId();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAllMtsModes audioId="+audioId);
            String currentAudioLang = null;
            if(audioId < mtsAudioMode.length && audioId > -1){
                currentAudioLang = mtsAudioMode[audioId];
            }
            if(!modes.contains(currentAudioLang) && currentAudioLang != null){
                modes.add(currentAudioLang);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAllMtsModes addAudioLang="+currentAudioLang);
            }
        }
        return modes;
    }
    
    public String getMtsModeString(int index){
        if(index >= 0 && index <= mtsAudioMode.length){
            return mtsAudioMode[index];
        }else {
            return "";
        }
    }
    
    public int getMtsByModeString(String mode){
        for(int i = 0; i<mtsAudioMode.length; i++){
            if(mode.equalsIgnoreCase(mtsAudioMode[i])){
                return i;
            }
        }
        return 0;
    }
    
    public String getMtsSummaryByAcfgValue(int configValue){
        List<String> list = getAllMtsModesList();
        if(list.isEmpty()){
            return null;
        }
        String mode = getMtsModeString(configValue);
        String summary = "";
        if(list.contains(mode)){
            summary = mode;
        }else if(!list.isEmpty()){
            summary = list.get(0);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getMtsSummaryByAcfgValue summary="+summary);
        return summary;
    }
    
    public String getMtsCurIndexByAcfgValue(int configValue){
        List<String> list = getAllMtsModesList();
        String mode = getMtsModeString(configValue);
        if(list.contains(mode)){
            return mode;
        }else {
            return null;
        }
    }

    public List<TvProviderAudioTrackBase> filterAudioTracksForNav(List<TvProviderAudioTrackBase> tracks) {
		List<TvProviderAudioTrackBase> filterTracks = new ArrayList<TvProviderAudioTrackBase>();
		List<TvProviderAudioTrackBase> filterVITracks = new ArrayList<TvProviderAudioTrackBase>();
		List<TvProviderAudioTrackBase> fiListSplits = new ArrayList<TvProviderAudioTrackBase>();
		List<String> audioLangs = new ArrayList<String>();
		for(TvProviderAudioTrackBase tk : tracks){
			filterTracks.add(tk);
            String type = null;
            String mixtype = null;
            String eClass = null;
            type = String.valueOf(tk.getAudioType());
            mixtype = String.valueOf(tk.getAudioMixType());
            eClass = String.valueOf(tk.getAudioEditorialClass());
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "filterAudioTracksForNav:"+tk.toString());
			if(mixtype != null && mixtype.equals("1")){
				if(eClass != null && eClass.equals("2")){
					filterVITracks.add(tk);
				}else{
					if(eClass != null && eClass.equals("0") &&(type != null && type.equals("3"))){
						filterVITracks.add(tk);
					}
				}
			}else if(mixtype != null && !mixtype.equals("2")){
				if(type != null && type.equals("3")){
					filterVITracks.add(tk);
				}
			}
			if(TextUtils.isEmpty(tk.getAudioLanguage()) && tk.getAudioFormat()==2 && tk.getAudioIndex()==2) {
			    fiListSplits.add(tk);
			}
		}
		filterTracks.removeAll(filterVITracks);
		for(TvProviderAudioTrackBase tk : filterTracks){
			audioLangs.add(tk.getAudioLanguage());
		}

		filterTracks.clear();
		for(TvProviderAudioTrackBase tk : filterVITracks){
			String tkLang = tk.getAudioLanguage();
			for(String lang : audioLangs){
				if(tkLang.equals(lang)){
					filterTracks.add(tk);
				}
			}
		}

		tracks.removeAll(filterTracks);
        for (TvProviderAudioTrackBase tk:fiListSplits) {
            List<TvProviderAudioTrackBase> tempsInfos = new ArrayList<TvProviderAudioTrackBase>();
            String leftLang = "";
            String rightLang = "";
            for (TvProviderAudioTrackBase tvProviderAudioTrackBase : tracks) {
                if(tk.getAudioPid()==tvProviderAudioTrackBase.getAudioPid()) {
                    tempsInfos.add(tvProviderAudioTrackBase);
                }
            }
            for (TvProviderAudioTrackBase tvTrackInfo : tempsInfos) {
                if(tvTrackInfo.getAudioIndex()==0) {
                    leftLang = tvTrackInfo.getAudioLanguage();
                } else if(tvTrackInfo.getAudioIndex()==1) {
                    rightLang = tvTrackInfo.getAudioLanguage();
                }
            }
            String unknown = DestroyApp.appContext.getString(R.string.nav_unknown_audio_sundry);
            tk.setAudioLanguage((TextUtils.isEmpty(leftLang) ? unknown : leftLang) + " + " + (TextUtils.isEmpty(rightLang) ? unknown : leftLang));
        }

        for (TvProviderAudioTrackBase tk : tracks) {
            Iterator<TvProviderAudioTrackBase> iterator = fiListSplits.iterator();
            while (iterator.hasNext()) {
                TvProviderAudioTrackBase next = iterator.next();
                if(TextUtils.isEmpty(tk.getAudioLanguage()) && tk.getAudioId()==next.getAudioId()) {
                    tk.setAudioLanguage(next.getAudioLanguage());
                    iterator.remove();
                }
            }
        }
		return tracks;
	}

    private boolean isSupportVI() {
        return false;//MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_AUD_AUD_TYPE) == 2;
		
    }

    public List<TvTrackInfo> filterAudioTracks(List<TvTrackInfo> tracks) {
		List<TvTrackInfo> filterTracks = new ArrayList<TvTrackInfo>();
		List<TvTrackInfo> filterVITracks = new ArrayList<TvTrackInfo>();
		List<TvTrackInfo> fiListSplits = new ArrayList<TvTrackInfo>();
		List<TvTrackInfo> fiListDeletes = new ArrayList<TvTrackInfo>();
		List<String> audioLangs = new ArrayList<String>();
		for(TvTrackInfo tk:tracks){
			filterTracks.add(tk);
            String type = null;
            String mixtype = null;
            String eClass = null;
            String audioFormt=null;
            String audioIndex=null;
            if (null != tk.getExtra()) {
                type = tk.getExtra().getString("key_AudioType");
                mixtype = tk.getExtra().getString("key_AudioMixType");
                eClass = tk.getExtra().getString("key_AudioEditorialClass");
                audioFormt = tk.getExtra().getString("key_AudioFormt");
                audioIndex = tk.getExtra().getString("key_AudioIndex");
            }

            if(!TextUtils.isEmpty(audioFormt) && "2".equals(audioFormt)
                    && !TextUtils.isEmpty(audioIndex) && "2".equals(audioIndex)
                    && TextUtils.isEmpty(tk.getLanguage())) {
                fiListSplits.add(tk);
            }

			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "filterAudioTracks:lang:"+tk.getLanguage()+
			        " id:"+tk.getId()+" Bundle:"+(tk.getExtra() == null ? "null":tk.getExtra().toString()));

			if(!isSupportVI()) {
			    if(mixtype != null && mixtype.equals("1")){
			        if(eClass != null && eClass.equals("2")){
			            filterVITracks.add(tk);
			            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "filterAudioTracks:langvi:"+tk.getLanguage()+
						        " id:"+tk.getId()+" Bundle:"+(tk.getExtra() == null ? "null":tk.getExtra().toString()));
			        }else{
			            if(eClass != null && eClass.equals("0") &&(type != null && type.equals("3"))){
			                filterVITracks.add(tk);
			                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "filterAudioTracks:langvi:"+tk.getLanguage()+
			    			        " id:"+tk.getId()+" Bundle:"+(tk.getExtra() == null ? "null":tk.getExtra().toString()));
			            }
			        }
			    }else if(mixtype != null && !mixtype.equals("2")){
			        if(type != null && type.equals("3")){
			            filterVITracks.add(tk);
			        }
			    }
			}
		}
		filterTracks.removeAll(filterVITracks);
		for(TvTrackInfo tk:filterVITracks){
			audioLangs.add(tk.getLanguage());
            fiListDeletes.add(tk);
		}
		filterVITracks.clear();
		for(TvTrackInfo tk:filterTracks){
			String tkLang = tk.getLanguage();

			for (int i=0;i<audioLangs.size();i++){
			    if(tkLang.equals(audioLangs.get(i))){
			    	filterVITracks.add(fiListDeletes.get(i));
                }
            }
//			for(String lang:audioLangs){
//				if(tkLang.equals(lang)){
//					filterTracks.add(tk);//tk
//				}
//			}
		}
		tracks.removeAll(filterVITracks);
		for(TvTrackInfo tk:tracks){
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "filterAudioTracks:list tk.getLanguage =="+tk.getLanguage()+",tk_id=="+tk.getId());
		}
		List<TvTrackInfo> newInfos = new ArrayList<TvTrackInfo>();

		for (TvTrackInfo tk:fiListSplits) {
		    List<TvTrackInfo> tempsInfos = new ArrayList<TvTrackInfo>();
		    String leftLang = "";
		    String rightLang = "";
		    for (TvTrackInfo tvTrackInfo : tracks) {
		        if(tk.getExtra() != null && tvTrackInfo.getExtra() !=null &&
		                tk.getExtra().getString("key_AudioPid").equals(tvTrackInfo.getExtra().getString("key_AudioPid"))) {
                    tempsInfos.add(tvTrackInfo);
                }
            }
            for (TvTrackInfo tvTrackInfo : tempsInfos) {
                if(tvTrackInfo.getExtra() != null && tvTrackInfo.getExtra().getString("key_AudioIndex").equals("0")) {
                    leftLang = tvTrackInfo.getLanguage();
                } else if(tvTrackInfo.getExtra() != null && tvTrackInfo.getExtra().getString("key_AudioIndex").equals("1")) {
                    rightLang = tvTrackInfo.getLanguage();
                }
            }
            String unknown = DestroyApp.appContext.getString(R.string.nav_unknown_audio_sundry);
            newInfos.add(new TvTrackInfo.Builder(TvTrackInfo.TYPE_AUDIO,tk.getId())
                    .setLanguage((TextUtils.isEmpty(leftLang) ? unknown : leftLang) + " + " + (TextUtils.isEmpty(rightLang) ? unknown : leftLang)).build());
        }

		List<TvTrackInfo> result = new ArrayList<TvTrackInfo>();

		for (TvTrackInfo tvTrackInfo : tracks) {


		    int index = -1;
		    for (int i = 0; i < newInfos.size(); i++) {
                if(tvTrackInfo.getId().equals(newInfos.get(i).getId())) {
                    index = i;
                    break;
                }
            }
		    if(index >= 0) {
		        result.add(newInfos.get(index));
		        newInfos.remove(index);

		    } else {
		        result.add(tvTrackInfo);

		    }
        }
		return result;
	}

    /**
   * DTV get track language by trackId
   * @param trackId
   * @return
   */
  private String getAudioTrackLangById(String trackId,List<TvTrackInfo> tracks){
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAudioTrackLangById,trackId:"+trackId);
    boolean isScrambled = false;
    if(!TurnkeyUiMainActivity.getInstance().isUKCountry){
    BannerView bannerView = (BannerView)ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
    if (bannerView != null) {
      isScrambled = bannerView.isAudioScrambled();
    }
    }
    int trackIdx = 0;
    String viString = "";
    if (tracks.size() != 1) {
      for (int i = 0; i < tracks.size(); i++) {
        if (tracks.get(i).getId().equals(trackId)) {
          trackIdx = i;
          break;
        }
      }
      //scrambled audio
      if (isScrambled) {
        return (trackIdx+1)+"/"+tracks.size() + " " + mContext.getString(R.string.nav_channel_audio_scrambled);
      }
      String lang ="";
      try {
		
	
      if(tracks.get(trackIdx).getExtra()!=null&&
    		  !TextUtils.isEmpty(tracks.get(trackIdx).getExtra().getString("key_DriverAutoSelect"))){
      if(tracks.get(trackIdx).getExtra().getString("key_DriverAutoSelect").equals("1")){
    	  lang =mContext.getResources().getString(R.string.nav_unknown_audio_sundry) ;
    	  return lang;
      }}else{
    	  lang =  tracks.get(trackIdx).getLanguage();
      }
      } catch (Exception e) {
  		// TODO: handle exception
    	  e.printStackTrace();
    	  com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
  	}
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "lang:"+lang);
      if(isSupportVI() && aAudIsVisualImpairedAudio(tracks.get(trackIdx))) {
          viString = "(VI)";
      }
      return buildAudioLangStrByLang((trackIdx+1) +"/"+tracks.size(), lang) + viString;
    }else {
      //scrambled audio
      if (isScrambled) {
        return mContext.getString(R.string.nav_channel_audio_scrambled);
      }
      String lang = tracks.get(trackIdx).getLanguage();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "lang:"+lang);
      if(isSupportVI() && aAudIsVisualImpairedAudio(tracks.get(trackIdx))) {
          viString = "(VI)";
      }
      return buildAudioLangStrByLang(lang)+viString;
    }
  }
  
  public synchronized List<TvTrackInfo> filterAudioTracksForAC4(List<TvTrackInfo> tracks) {
      // find main/hi/broadcast audio.
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"filterAudioTracks for ac4");
      List<TvTrackInfo> mainTracks = new ArrayList<TvTrackInfo>();
      List<TvTrackInfo> mixTracks = new ArrayList<TvTrackInfo>();
      // filter special main stream
      List<TvTrackInfo> poorTracks = new ArrayList<TvTrackInfo>();
      for (TvTrackInfo track : tracks) {
          if(track.getExtra() != null) {
              if(aAudioIsMaskStream(track)) {
                  track.getExtra().putBoolean(A_HI_AUDIO, true);
                  mixTracks.add(track);
                  fillDisplayAudioLang(track);
              }
              if(aAudIsIndependentVisualImpairedAudio(track)) {
                  track.getExtra().putBoolean(A_BROADCAST_MIXED_VI_AUDIO, true);
                  mixTracks.add(track);
                  fillDisplayAudioLang(track);
              }
              if(aAudIsMixableVisualImpairedAudio(track) ||
                  aAudIsSpsAudio(track) ||
                  aAudIsAdSpsAudio(track)) {
                  mixTracks.add(track);
              }
          }
//          else {
////              poorTracks.add(track);
//          }
      }
      for (TvTrackInfo track : tracks) {
          boolean isFind = false;
          for (TvTrackInfo ad : mixTracks) {
              if(track.getId().equals(ad.getId())) {
                  isFind = true;
                  break;
              }
          }
          if(!isFind) {
        	  if(track.getExtra() != null) {
        	  track.getExtra().putBoolean(A_MAIN_AUDIO, true);
              mainTracks.add(track);
              fillDisplayAudioLang(track);
        	  }
          }
      }

      // 1.find receive audio.
      // 2.find the same lang audio from main audio.
      for(TvTrackInfo track : mixTracks) {
          // 1.find receive audio.
          TvTrackInfo mainTrackInfo = findMixMainTrack(track,mainTracks);
          if(aAudIsMixableVisualImpairedAudio(track)) {
            // 2.find the same lang audio from main audio.
            if(mainTrackInfo == null) {
              // receive mix vi
              track.getExtra().putBoolean(A_RECEIVE_MIX_VI, true);
              fillDisplayAudioLang(track);
            } else {
              //remark main audio that has ad.
              mainTrackInfo.getExtra().putBoolean(A_MAIN_AUDIO_HAS_AD, true);
              // receive mixed vi
              track.getExtra().putBoolean(A_RECEIVE_MIXED_VI_MAIN, true);
              track.getExtra().putString(A_RECEIVE_MIXED_VI_MAIN_ID, mainTrackInfo.getId());
              track.getExtra().putString(A_RECEIVE_MIXED_VI_MAIN_MESSAGE_ID,
                  mainTrackInfo.getExtra().getString("key_AudioMsgId"));
              // put lang
              fillMixedDisplayAudioLang(mainTrackInfo, track);
            }
          } else if(aAudIsSpsAudio(track)) {
              if(mainTrackInfo == null) {
                // receive mix vi
                track.getExtra().putBoolean(A_SPS_MIX_AUDIO, true);
                fillDisplayAudioLang(track,false);
              } else {
                //remark main audio that has ad.
                mainTrackInfo.getExtra().putBoolean(A_MAIN_AUDIO_HAS_SPS, true);
                // receive mixed vi
                track.getExtra().putBoolean(A_SPS_MIXED_MAIN, true);
                track.getExtra().putString(A_SPS_MIXED_MAIN_ID, mainTrackInfo.getId());
                fillMixedDisplayAudioLang(mainTrackInfo, track);
              }
          } else if(aAudIsAdSpsAudio(track)) {
              if(mainTrackInfo == null) {
                // receive mix vi
                track.getExtra().putBoolean(A_AD_SPS_MIX_AUDIO, true);
                fillDisplayAudioLang(track,false);
              } else {
                //remark main audio that has ad.
                mainTrackInfo.getExtra().putBoolean(A_MAIN_AUDIO_HAS_AD_SPS, true);
                // receive mixed vi
                track.getExtra().putBoolean(A_AD_SPS_MIXED_MAIN_AUDIO, true);
                track.getExtra().putString(A_AD_SPS_MIXED_MAIN_ID_AUDIO, mainTrackInfo.getId());
                fillMixedDisplayAudioLang(mainTrackInfo, track);
              }
          }
      }

      for (TvTrackInfo track:mainTracks) {
          String pid = "";
          String compTag = "";
          if(track.getExtra() != null) {
              pid = track.getExtra().getString("key_AudioPid");
              compTag = track.getExtra().getString("key_AudioCompTag");
          }
          if(TextUtils.isEmpty(pid) || TextUtils.isEmpty(compTag)){ continue;}
          if(aAudioIsMaskStream(track, SCDB_OPTION_MASK_AC4_PRE_STRM_COMP) &&
                  !pid.equals(compTag)) {
              for (TvTrackInfo track2:mixTracks) {
                  if(track2.getExtra() != null) {
                      String pid2 = track2.getExtra().getString("key_AudioPid");
                      if(compTag.equals(pid2) &&
                              track2.getExtra().getBoolean(A_RECEIVE_MIXED_VI_MAIN) &&
                              track2.getExtra().getString(A_RECEIVE_MIXED_VI_MAIN_ID,"-1").equals(track.getId()) &&
                              aAudioIsMaskStream(track2,SCDB_OPTION_MASK_AC4_AUX_STREAM_COMP)) {
                          poorTracks.add(track);
                          poorTracks.add(track2);
                      }
                  }
              }
          }
      }

      for(TvTrackInfo tk : tracks) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tracks    :lang:"+tk.getLanguage()+
                  " id:"+tk.getId()+" Bundle:"+(tk.getExtra() == null ? "null":tk.getExtra().toString()));
      }

      for(TvTrackInfo tk : mainTracks) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mainTracks:lang:"+tk.getLanguage()+
                  " id:"+tk.getId()+" Bundle:"+(tk.getExtra() == null ? "null":tk.getExtra().toString()));
      }

      for(TvTrackInfo tk : poorTracks) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "poorTracks:lang:"+tk.getLanguage()+
                  " id:"+tk.getId()+" Bundle:"+(tk.getExtra() == null ? "null":tk.getExtra().toString()));
      }

      tracks.removeAll(poorTracks);

      return tracks;
  }

  private TvTrackInfo findMixMainTrack(TvTrackInfo track, List<TvTrackInfo> mainTracks) {
    List<TvTrackInfo> mixTracks = new ArrayList<TvTrackInfo>();
    for(TvTrackInfo mainTrack : mainTracks) {
      if(!track.getId().equals(mainTrack.getId()) &&
          !TextUtils.isEmpty(track.getLanguage()) &&
          track.getLanguage().equals(mainTrack.getLanguage()) &&
          track.getExtra().getString("key_AudioEncodeType")
          .equals(mainTrack.getExtra().getString("key_AudioEncodeType"))) {
        mixTracks.add(mainTrack);
      }
    }
    if(mixTracks.isEmpty()) {
      return null;
    } else if(mixTracks.size() == 1) {
      return mixTracks.get(0);
    } else {
      Collections.sort(mixTracks, new Comparator<TvTrackInfo>() {

        @Override
        public int compare(TvTrackInfo lhs, TvTrackInfo rhs) {
          String lp = lhs.getExtra().getString("key_AudioPreseIdx");
          lp=lp==null?"0":lp;
          String rp = rhs.getExtra().getString("key_AudioPreseIdx");
          rp=rp==null?"0":rp;
          return Integer.parseInt(lp) - Integer.parseInt(rp);
        }
      });
      return mixTracks.get(0);
    }
  }

  private void fillMixedDisplayAudioLang(TvTrackInfo mainTrackInfo, TvTrackInfo track) {
      String text = "";
      if("1".equals(track.getExtra().getString("key_AudioTextLabel"))){
        text = navMtkTvAVMode.getApdText(
            Integer.parseInt(mainTrackInfo.getExtra().getString("key_AudioMsgId"))).getAudioApdText();
      }
      if(!TextUtils.isEmpty(text)) {
        text += " ";
      }
      if("1".equals(track.getExtra().getString("key_AudioTextLabel"))){
        text += navMtkTvAVMode.getApdText(
            Integer.parseInt(track.getExtra().getString("key_AudioMsgId"))).getAudioApdText();
      }
      //can't get apd text, show lang.
      if(TextUtils.isEmpty(text)) {
        text = buildAudioLangStrByLang(track.getLanguage());
      }
      track.getExtra().putString(A_DISPLAY_AUDIO_LANGUAGE, text);
  }

  private void fillDisplayAudioLang(TvTrackInfo track, boolean isAPD) {
      String text = "";
      if(!isAPD) {
        text = buildAudioLangStrByLang(track.getLanguage());
      } else {
        MtkTvAudioApdBase apd = null;
        if("1".equals(track.getExtra().getString("key_AudioTextLabel"))){
          apd = navMtkTvAVMode.getApdText(
              Integer.parseInt(track.getExtra().getString("key_AudioMsgId")));
        }
        if(apd != null) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getAudioApdLang "+apd.getAudioApdLang()+" getAudioApdStatus "+apd.getAudioApdStatus()+" getAudioApdTextLen "+apd.getAudioApdTextLen()+" getAudioApdText "+apd.getAudioApdText());
          text = apd.getAudioApdText();
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"fillDisplayAudioLang--text:"+text);
      if(TextUtils.isEmpty(text)) {
          text = buildAudioLangStrByLang(track.getLanguage());
        }else {
          text = buildAudioLangStrByLang(track.getLanguage())+"   "+text;
      }
      }
      track.getExtra().putString(A_DISPLAY_AUDIO_LANGUAGE, text);
  }

  private void fillDisplayAudioLang(TvTrackInfo track) {
      fillDisplayAudioLang(track, true);
  }

private boolean aAudioIsMaskStream(TvTrackInfo track, int flag) {
      boolean result = false;
      if(track.getExtra() != null) {
          String mask = track.getExtra().getString("key_AudioOptMask");
          if(!TextUtils.isEmpty(mask)) {
              if((Integer.parseInt(mask) & flag) != 0) {
                  result = true;
              }
          }
      }
      return result;
  }
  
////main audio
//  private boolean a_audio_is_main_audio(TvTrackInfo tAudioMpeg) {
//      String type = null;
//      if (null != tAudioMpeg.getExtra()) {
//          type = tAudioMpeg.getExtra().getString("key_AudioType");
//          //type = 0/1/5
//          if(String.valueOf(TvProviderAudioTrackBase.AUD_TYPE_UNKNOWN).equals(type) ||
//                  String.valueOf(TvProviderAudioTrackBase.AUD_TYPE_CLEAN).equals(type) ||
//                  String.valueOf(TvProviderAudioTrackBase.AUD_TYPE_COMPLETE_MAIN).equals(type)) {
//              return true;
//          }
//      }
//      return false;
//  }

  //HI audio
  private boolean aAudioIsMaskStream(TvTrackInfo tAudioMpeg) {
      String type = null;String eClass = null;
      if (null != tAudioMpeg.getExtra()) {
          type = tAudioMpeg.getExtra().getString("key_AudioType");
          eClass = tAudioMpeg.getExtra().getString("key_AudioEditorialClass");
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"aAudioIsMaskStream eClass:"+eClass+" type:"+type);
//          if(eClass.equals("3") || type.equals("2")) {
//          }
          if(String.valueOf(TvProviderAudioTrackBase.AUD_EDITORIAL_CLASS_HEARING_IMPAIRED_CLEAN).equals(eClass) ||
                  String.valueOf(TvProviderAudioTrackBase.AUD_TYPE_HEARING_IMPAIRED).equals(type)) {
              return true;
          }
      }
      return false;
  }
  @Deprecated
  private boolean aAudIsVisualImpairedAudio(TvTrackInfo tAudioMpeg) {
      return aAudIsIndependentVisualImpairedAudio(tAudioMpeg) || aAudIsMixableVisualImpairedAudio(tAudioMpeg);
  }
//broadcase mixed VI audio
  private boolean aAudIsIndependentVisualImpairedAudio(TvTrackInfo tAudioMpeg) {
      boolean bIsIndependent = false;
      String type = null;
      String mixtype = null;
      String eClass = null;
      if (null != tAudioMpeg.getExtra()) {
          type = tAudioMpeg.getExtra().getString("key_AudioType");
          mixtype = tAudioMpeg.getExtra().getString("key_AudioMixType");
          eClass = tAudioMpeg.getExtra().getString("key_AudioEditorialClass");
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"aAudIsIndependentVisualImpairedAudio  mixtype:"+mixtype+" eClass:"+eClass+" type:"+type);
//          if(!TextUtils.isEmpty(type) && !TextUtils.isEmpty(mixtype) && !TextUtils.isEmpty(eClass)) {
//              if(mixtype.equals("2")) {
//                  if(eClass.equals("2") || eClass.equals("4") || (eClass.equals("0") && type.equals("3"))) {
//                      bIsIndependent = true;
//                  }
//              }
//          }
          if(String.valueOf(TvProviderAudioTrackBase.AUD_MIX_TYPE_INDEPENDENT).equals(mixtype)) {
              if(String.valueOf(TvProviderAudioTrackBase.AUD_EDITORIAL_CLASS_VISUAL_IMPAIRED_AD).equals(eClass) ||
                      String.valueOf(TvProviderAudioTrackBase.AUD_EDITORIAL_CLASS_VISUAL_IMPAIRED_SPOKEN_SUBTITLE).equals(eClass) ||
                      (String.valueOf(TvProviderAudioTrackBase.AUD_EDITORIAL_CLASS_RESERVED).equals(eClass) &&
                              String.valueOf(TvProviderAudioTrackBase.AUD_TYPE_VISUAL_IMPAIRED).equals(type))) {
                  bIsIndependent = true;
              }
          }
      }
      return bIsIndependent;
  }

//receive mix VI
  private boolean aAudIsMixableVisualImpairedAudio(TvTrackInfo tAudioMpeg) {
      boolean isMixable = false;
      String type = null;
      String mixtype = null;
      String eClass = null;
      if (null != tAudioMpeg.getExtra()) {
          type = tAudioMpeg.getExtra().getString("key_AudioType");
          mixtype = tAudioMpeg.getExtra().getString("key_AudioMixType");
          eClass = tAudioMpeg.getExtra().getString("key_AudioEditorialClass");
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"aAudIsMixableVisualImpairedAudio  mixtype:"+mixtype+" eClass:"+eClass+" type:"+type);
//          if(!TextUtils.isEmpty(type) && !TextUtils.isEmpty(mixtype) && !TextUtils.isEmpty(eClass)) {
//              if(mixtype.equals("1")) {
//                  if(eClass.equals("2") || (eClass.equals("0") && type.equals("3"))) {
//                      b_is_mixable = true;
//                  }
//              } else if(!mixtype.equals("2") && type.equals("3")){
//                  b_is_mixable = true;
//              }
//          }
          if(String.valueOf(TvProviderAudioTrackBase.AUD_MIX_TYPE_SUPPLEMENTARY).equals(mixtype)) {
              if(String.valueOf(TvProviderAudioTrackBase.AUD_EDITORIAL_CLASS_VISUAL_IMPAIRED_AD).equals(eClass) ||
                      (String.valueOf(TvProviderAudioTrackBase.AUD_EDITORIAL_CLASS_RESERVED).equals(eClass) &&
                              String.valueOf(TvProviderAudioTrackBase.AUD_TYPE_VISUAL_IMPAIRED).equals(type))) {
                  isMixable = true;
              }
          } else if(!String.valueOf(TvProviderAudioTrackBase.AUD_MIX_TYPE_INDEPENDENT).equals(mixtype) &&
                  String.valueOf(TvProviderAudioTrackBase.AUD_TYPE_VISUAL_IMPAIRED).equals(type)) {
              isMixable = true;
          }
      }
      return isMixable;
  }

  //sps
  private boolean aAudIsSpsAudio(TvTrackInfo tAudioMpeg) {
      boolean bIsSps = false;
      String mixtype = null;String eClass = null;
      if (null != tAudioMpeg.getExtra()) {
          mixtype = tAudioMpeg.getExtra().getString("key_AudioMixType");
          eClass = tAudioMpeg.getExtra().getString("key_AudioEditorialClass");
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"aAudIsMixableVisualImpairedAudio " + "eClass:"+eClass+" mixtype:"+mixtype);
          //Mix_type = 1 && e_class = 4
          if(String.valueOf(TvProviderAudioTrackBase.AUD_MIX_TYPE_SUPPLEMENTARY).equals(mixtype) &&
              String.valueOf(TvProviderAudioTrackBase.AUD_EDITORIAL_CLASS_VISUAL_IMPAIRED_SPOKEN_SUBTITLE).equals(eClass)) {
            bIsSps = true;
          }
      }
      return bIsSps;
  }

  //ad_sps
  private boolean aAudIsAdSpsAudio(TvTrackInfo tAudioMpeg) {
      boolean isAd = false;
      String mixtype = null;
      String eClass = null;
      if (null != tAudioMpeg.getExtra()) {
          mixtype = tAudioMpeg.getExtra().getString("key_AudioMixType");
          eClass = tAudioMpeg.getExtra().getString("key_AudioEditorialClass");
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"aAudIsMixableVisualImpairedAudio " + "eClass:"+eClass+" mixtype:"+mixtype);
          //Mix_type = 1 && e_class = 5
          if(String.valueOf(TvProviderAudioTrackBase.AUD_MIX_TYPE_SUPPLEMENTARY).equals(mixtype) &&
              String.valueOf(5).equals(eClass)) {
              isAd = true;
          }
      }
      return isAd;
  }

  /**
   * DTV get track language by trackId NO number for nav
   * @param trackId
   * @return
   */
  public String getAudioTrackLangByIdNoNumbForNav(String trackId , List<TvProviderAudioTrackBase> tracks) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAudioTrackLangByIdNoNumbForNav,trackId:" + trackId);
    int trackIdx = 0;
    if (tracks.size() != 1) {
      for (int i = 0; i < tracks.size(); i++) {
        if (String.valueOf(tracks.get(i).getAudioId()).equals(trackId)) {
          trackIdx = i;
          break;
        }
      }
      String lang = tracks.get(trackIdx).getAudioLanguage();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "lang:"+lang);
      return buildAudioLangStrByLang((trackIdx+1)+"/"+tracks.size(), lang);
    }else {
      String lang = tracks.get(trackIdx).getAudioLanguage();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "lang:"+lang);
      return buildAudioLangStrByLang(lang);
    }
  }

  /**
   * DTV get track language by trackId NO number
   * @param trackId
   * @return
   */
  public String getAudioTrackLangByIdNoNumb(String trackId,List<TvTrackInfo> tracks) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAudioTrackLangByIdNoNumb,trackId:" + trackId);
    int trackIdx = 0;
    if (tracks.size() != 1) {
      for (int i = 0; i < tracks.size(); i++) {
        if (tracks.get(i).getId().equals(trackId)) {
          trackIdx = i;
          break;
        }
      }
      String lang = tracks.get(trackIdx).getLanguage();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "lang:"+lang);
      return buildAudioLangStrByLang((trackIdx+1)+"/"+tracks.size(), lang);
    }else {
      String lang = tracks.get(trackIdx).getLanguage();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "lang:"+lang);
      return buildAudioLangStrByLang(lang);
    }
  }

  /**
   * DTV get track language by trackId NO number
   * @param trackId
   * @return
   */
  public String getAudioTrackLangByIdForSoundTrack(Context context,String trackId,List<TvTrackInfo> tracks) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAudioTrackLangByIdForSoundTrack,trackId:" + trackId);
    if(tracks == null || tracks.isEmpty()) {return "Unknown";}
    int trackIdx = 0;
    int trackSum = tracks.size();
    if (trackSum != 1) {
      for (int i = 0; i < tracks.size(); i++) {
        if (tracks.get(i).getId().equals(trackId)) {
          trackIdx = i;
          break;
        }
      }
    }
    return buildAudioLangStrByLangForSoundTrack(context,tracks.get(trackIdx), trackIdx, trackSum);
  }

  private String buildAudioLangStrByLang(String lang) {
    if (!TextUtils.isEmpty(lang)) {
      if(audioSpecialMap.containsKey(lang.toLowerCase(Locale.getDefault()))) {
        return audioSpecialMap.get(lang.toLowerCase(Locale.getDefault()));
      } else {
    	  LanguageUtil mLanguageUtil = new LanguageUtil(mContext);
//        Locale locale = new Locale(lang);
//        return locale.getDisplayLanguage();
    	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"lang=="+new Locale(lang));
    	  return mLanguageUtil.getMtsNameByValue(lang);
      }
    } else if(isCountryIndonesia()){
      return mContext.getResources().getString(R.string.menu_arrays_Undefined);
      } else if(TVContent.getInstance(mContext).isTHACountry()){
        return mContext.getResources().getString(R.string.menu_arrays_Undefined);
      }else {
      return mContext.getResources().getString(R.string.nav_unknown_audio_sundry);
    }
  }

  private String buildAudioLangStrByLangForSoundTrack(Context context,TvTrackInfo track,int trackIdx,int trackSum) {
    Log.d(TAG,"context: " + context);
      int opreatorString = ScanContent.getDVBSCurrentOP();
      String lang = track.getLanguage();
      Log.d(TAG,"SoundTrack opreator: " + opreatorString+", trackIdx :"+trackIdx+", trackSum :"+trackSum);
      if(opreatorString == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_SKY_DEUTSCHLAND){
          String adTypeString ="";
          String deuFrefix = "DEU ";
          if (null != track.getExtra()) {
              adTypeString = track.getExtra().getString("key_AudioEncodeType");
          }
          Log.d(TAG,"adTypeString: " + adTypeString);
          if (!TextUtils.isEmpty(adTypeString)) {
              int adType = Integer.parseInt(adTypeString);
              if (adType == 1 || adType == 12 || adType == 26){
                  return deuFrefix+"Dolby Digital 2.0";
              }
          }
          Log.d(TAG,"lang: " + lang);
          if (!TextUtils.isEmpty(lang)) {
              if(audioSpecialMap.containsKey(lang.toLowerCase(Locale.getDefault()))) {
                return deuFrefix+audioSpecialMap.get(lang.toLowerCase(Locale.getDefault()));
              } else {
                Locale locale = new Locale(lang);
                return deuFrefix+locale.getDisplayLanguage();
              }
          }else if(isCountryIndonesia()){
              return "Undefined audio";
          }else {
              return "Unknown";
          }
      }else {
          Log.d(TAG,"default track count: " + trackSum);
          return trackSum > 1 ? buildAudioLangStrByLang((trackIdx+1) +"/"+trackSum, lang) :
              buildAudioLangStrByLang(lang);
      }
  }

  private String buildAudioLangStrByLang(String pref, String lang) {
    if (!TextUtils.isEmpty(lang)) {
      if(audioSpecialMap.containsKey(lang.toLowerCase(Locale.getDefault()))) {
        if (MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_CH_LST_TYPE) > 0) {
          if ("qaa".equalsIgnoreCase(lang)) {//dtv00724640
            Log.d(TAG,"qaa: " + MtkTvCI.getInstance(Constants.slot_id).getProfileISO639LangCode());
            audioSpecialMap.put("qaa", MtkTvCI.getInstance(Constants.slot_id).getProfileISO639LangCode());
          }
          if ("und".equalsIgnoreCase(lang)) {
            Log.d(TAG,"und: " + MtkTvCI.getInstance(Constants.slot_id).getProfileISO639LangCode());
            audioSpecialMap.put("und", MtkTvCI.getInstance(Constants.slot_id).getProfileISO639LangCode());
          }
        } else{
          if ("qaa".equalsIgnoreCase(lang)) {
            if (TVContent.getInstance(
                mContext).isFraCountry()) {
              audioSpecialMap.put("qaa", "V.O.");
            }else {
              audioSpecialMap.put("qaa", "Original audio");
            }
          }
          if ("und".equalsIgnoreCase(lang)) {
            audioSpecialMap.put("und", "Undefined audio");
          }
        }
        return pref + " " + audioSpecialMap.get(lang.toLowerCase(Locale.getDefault()));
      } else {
        String[] split = lang.split(" \\+ ");
        LanguageUtil mLanguageUtil = new LanguageUtil(mContext);
        if (split.length == 2) {
        	return pref+ " " + mLanguageUtil.getMtsNameByValue(split[0].toLowerCase(Locale.getDefault()))+ " + "+mLanguageUtil.getMtsNameByValue(split[1].toLowerCase(Locale.getDefault()));
//            return pref + " " + new Locale(split[0]).getDisplayLanguage()+" + "+new Locale(split[1]).getDisplayLanguage();
        } else {
        	 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"lang==="+new Locale(lang));
        	return pref + " "+ mLanguageUtil.getMtsNameByValue(lang.toLowerCase(Locale.getDefault()));
//            return pref + " " + new Locale(lang).getDisplayLanguage();
        }
      }
    } else if(isCountryIndonesia()){
      return pref+" "+mContext.getResources().getString(R.string.menu_arrays_Undefined);
      } else if(TVContent.getInstance(mContext).isTHACountry()){
        return mContext.getResources().getString(R.string.menu_arrays_Undefined);
      }else {
      return pref+" "+mContext.getResources().getString(R.string.nav_unknown_audio_sundry);
    }
  }

  /**
   * set next audio language
   *
   * @return
   */
    public int setNextAudioLang(){

    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setNextAudioLang");
		int result = -1;
		if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MODULES_WITH_TIF)) {
			String nextSelectID;
            List<TvTrackInfo> tracks = TurnkeyUiMainActivity.getInstance().getTvView()
                .getTracks(TvTrackInfo.TYPE_AUDIO);
            List<TvTrackInfo> filterTracks = filterAudioTracks(filterAudioTracksForAC4(tracks));
            String currentID = TurnkeyUiMainActivity.getInstance().getTvView()
                .getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
            nextSelectID = getNextTrackID(currentID,filterTracks);

            if (null != nextSelectID  && !nextSelectID.equalsIgnoreCase(currentID)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                    "come in setNextAudioLang with TIF,focus win main to set");
                getNextTrack(currentID,filterTracks);

                TurnkeyUiMainActivity.getInstance().getTvView()
                    .selectTrack(TvTrackInfo.TYPE_AUDIO, nextSelectID);
            }
		} else {
			result = navMtkTvAVMode.setNextAudioLang();
		}
		return result;
    }

//    private boolean isHasAdForMainAudio(TvTrackInfo targetTrack, List<TvTrackInfo> tracks) {
//        boolean result = false;
//        for(TvTrackInfo track : tracks) {
//            if(track.getExtra() != null &&
//                    track.getExtra().getBoolean(A_RECEIVE_MIXED_VI_MAIN) &&
//                    track.getExtra().getString(A_RECEIVE_MIXED_VI_MAIN_ID).equals(targetTrack.getId())) {
//                result = true;
//                break;
//            }
//        }
//        return result;
//    }

    /**
     * get current time
     *
     * @return
     */
    public String getCurrentTime(){
        MtkTvTimeFormatBase time = MtkTvTime.getInstance().getBroadcastLocalTime();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentTime,hour="+ time.hour +
            ",minute=" + time.minute);
        GregorianCalendar gregorianCalendar = new GregorianCalendar(
            time.year, time.month, time.monthDay, time.hour, time.minute, time.second);
        return DateFormat.getTimeFormat(mContext).format(gregorianCalendar.getTime());
    }

    public boolean isCountryIndonesia() {
      return MtkTvConfig.getInstance().getCountry().equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_IDN);
    }

    /**
     * get current screen mode
     * @return
     */
    public int getCurrentScreenMode(){
        return navMtkTvAVMode.getScreenMode();
    }

    /**
     * set current screen mode
     * @param mode
     * @return
     */
    public int setCurrentScreenMode(int mode){
        return navMtkTvAVMode.setScreenMode(mode);
    }

    public void setVideoMute(boolean isMute) {
        mtkTvAppTv.setVideoMute("main",isMute);
    }

    /**
     * get the support value array of the screen mode with the current action
     * @return
     */
    public int[] getSupportScreenModes(){
        return navMtkTvAVMode.getAllScreenMode();
    }

    /**
     * get the current picture mode
     * @return
     */
    public int getCurrentPictureMode(){
        return navMtkTvAVMode.getPictureMode();
    }

    /**
     * set the current picture mode
     * @param mode
     * @return
     */
    public int setCurrentPictureMode(int mode){
        return navMtkTvAVMode.setPictureMode(mode);
    }

    /**
     * get the support value array of the picture mode with the current action
     * @return
     */
    public int[] getSupportPictureModes(){
        return navMtkTvAVMode.getAllPictureMode();
    }

    /**
     * get current sound effect
     * @return
     */
    public int getCurrentSoundEffect(){
        return navMtkTvAVMode.getSoundEffect();
    }

    /**
     * set current sound effect
     * @param mode
     * @return
     */
    public int setCurrentSoundEffect(int mode){
        return navMtkTvAVMode.setSoundEffect(mode);
    }

    /**
     * get the support value array of the sound effect with the current action
     * @return
     */
    public int[] getSupportSoundEffects(){
        return navMtkTvAVMode.getAllSoundEffect();
    }

    /**
     * get main output if freeze or not
     * @return
     */
    public boolean isFreeze(){
        int result = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_PIP_POP_TV_FOCUS_WIN);

        if (0 == result) {
            return navMtkTvAVMode.isFreeze("main");
        } else {//1
            return navMtkTvAVMode.isFreeze("sub");
        }
    }

    /**
     * set freeze
     * @param isFreeze
     */
    public int setFreeze(boolean isFreeze){
        int result = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_PIP_POP_TV_FOCUS_WIN);

        if (0 == result) {
            return navMtkTvAVMode.setFreeze("main", isFreeze);
        } else {//1
            return navMtkTvAVMode.setFreeze("sub", isFreeze);
        }
    }

    public boolean isGingaWindowResize() {
        boolean result = navMtkTvGinga.isGingaWindowResize();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isGingaWindowResize:"+result);
        return result;
    }

    public boolean getInternalScrnMode() {
        boolean result = navMtkTvMHEG5.getInternalScrnMode();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getInternalScrnMode:"+result);
        return result;
    }

	private String getNextTrackID(String currentSelectID,
			List<TvTrackInfo> currentTrackList) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getNextTrackID, the currentSelectID = "
				+ currentSelectID);
		if (null != currentTrackList) {
			for (TvTrackInfo info: currentTrackList) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getNextTrackID, the ID = "
						+ info.getId());
			}
		}

		String nextID = null;
		if (null != currentSelectID) {
			if (null != currentTrackList) {
				for (int i = 0; i < currentTrackList.size(); i++) {
					if (currentSelectID.equals(currentTrackList.get(i).getId())) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getNextTrackID, the i = " + i);
						if ((i + 1) < currentTrackList.size()) {
							nextID = currentTrackList.get(i + 1).getId();
						} else {
							nextID = currentTrackList.get(0).getId();
						}
						break;
					}
				}
			}
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getNextTrackID, the nextID = " + nextID);
		return nextID;
	}
	
	private TvTrackInfo getNextTrack(String currentSelectID,
			List<TvTrackInfo> currentTrackList) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getNextTrackID, the currentSelectID = "
				+ currentSelectID);
		TvTrackInfo tracks=null;
		if (null != currentTrackList) {
			for (TvTrackInfo info:currentTrackList) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getNextTrackID, the ID = "
						+ info.getId());
			}
		}

		String nextID = null;
		if (null != currentSelectID) {
			if (null != currentTrackList) {
				for (int i = 0; i < currentTrackList.size(); i++) {
					if (currentSelectID.equals(currentTrackList.get(i).getId())) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getNextTrackID, the i = " + i);
						if ((i + 1) < currentTrackList.size()) {
							nextID = currentTrackList.get(i + 1).getId();
							tracks = currentTrackList.get(i+1);
						} else {
							nextID = currentTrackList.get(0).getId();
							tracks = currentTrackList.get(0);
						}
						break;
					}
				}
			}
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getNextTrackID, the nextID = " + nextID);
		return tracks;
	}

     public boolean isOnePlusOpened() {
            return  isDtsOpened() ||
                    isSonicEmotionOpened() ||
                    isDBXOpened() ||
                    isDAPOpened() ||
                    isDTSStudioOpened();
        }

     private boolean isDtsOpened() {
        int vx =getSettingValueInt(mContext.getContentResolver(),KEY_SOUND_DTS_VIRTUAL_X);

        Log.e(TAG, "isDtsOpened " + vx );
       
        return vx != 0;
    }

     
     private boolean isSonicEmotionOpened() {
        return getSettingValueInt(
            mContext.getContentResolver(),
            KEY_SONIC_EMOTION) > 0;
        }
     
    private boolean isDBXOpened() {
        return getSettingValueInt(
            mContext.getContentResolver(),
            KEY_SOUND_DBX_ENABLE) > 0;
    }

    private boolean isDAPOpened() {
        return getSettingValueInt(
            mContext.getContentResolver(),
            KEY_SOUND_ADVANCED_DOLBY_AP) > 0;
    }
    private boolean isDTSStudioOpened(){        
        return getSettingValueInt(
                mContext.getContentResolver(),
                KEY_SOUND_DTS_STUDIO_ENABLE) > 0;
                    
    }
    public int getSettingValueInt(ContentResolver mResolver ,String settingKey){
        int value = Settings.Global.getInt(mResolver,settingKey,0);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSettingValueInt : settingKey = " + settingKey + "  value = " + value);
        return value;
    }

}
