package com.mediatek.wwtv.tvcenter.nav.util;
import com.mediatek.mtkmdsclient.MdsClient;
import com.mediatek.mtkmdsclient.data.tvmain.TVMainObj;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvATSCCloseCaption;
import com.mediatek.twoworlds.tv.MtkTvAnalogCloseCaption;
import com.mediatek.twoworlds.tv.MtkTvBanner;
import com.mediatek.twoworlds.tv.MtkTvISDBCloseCaption;
import com.mediatek.twoworlds.tv.MtkTvSubtitle;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.MtkTvAVMode;
import com.mediatek.twoworlds.tv.MtkTvAVModeBase;
import com.mediatek.twoworlds.tv.MtkTvEventBase;
import com.mediatek.twoworlds.tv.model.TvProviderAudioTrackBase;
import com.mediatek.twoworlds.tv.model.MtkTvEventInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvDvbChannelInfo;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.wwtv.setting.util.LanguageUtil;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.epg.EPGUtil;
import com.mediatek.wwtv.tvcenter.nav.bean.NowNextRequestData;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.bean.ProgramInfo;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFProgramInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFProgramManager;
import com.mediatek.wwtv.tvcenter.R;

import android.annotation.SuppressLint;
import android.text.format.DateFormat;

import android.content.Context;
import android.media.tv.TvTrackInfo;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;

public class BannerImplement {

    private final static String TAG = "BannerImplement";


    public final static String STR_TIME_SPAN_TAG = "(24:00)";
    // for customer special channel decrease 2000
    public final static int DECREASE_NUM = 2000;

    public final static int DOBLY_TYPE_NONE=0;
    public final static int DOBLY_TYPE_ATOMS=1;
    public final static int DOBLY_TYPE_AUDIO=2;

    private static BannerImplement mNavBannerImplement = null;

    private MtkTvBanner mMtkTvBanner;

    private MtkTvAnalogCloseCaption mMtkTvAnalogCloseCaption;

    private MtkTvATSCCloseCaption mMtkTvATSCCloseCaption;

    private MtkTvEventBase mMtkTvEventBase;

    private CommonIntegration mIntegration;

    private MtkTvTime mMtkTvTime;
    String [] audioChannel;
    private String [] mHDRVideoInfo;
    private MtkTvEventInfoBase [] mEvents = new MtkTvEventInfoBase[2];

    private Context mContext;
    private TIFProgramManager tIFProgramManager;
    private String mCurCaptionInfo=null;
    private int mDecType=-1;
    private LanguageUtil mLanguageUtil;


    private ProgramInfo mNowIProgramInfo;
    private ProgramInfo mNextIProgramInfo;

    public BannerImplement(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context.getApplicationContext();
        mLanguageUtil=new LanguageUtil(mContext);
        mIntegration = CommonIntegration.getInstance();
        mMtkTvBanner = MtkTvBanner.getInstance();
        mMtkTvAnalogCloseCaption = MtkTvAnalogCloseCaption.getInstance();
        mMtkTvATSCCloseCaption = MtkTvATSCCloseCaption.getInstance();
        mMtkTvTime = MtkTvTime.getInstance();
        audioChannel = mContext.getResources().getStringArray(R.array.audio_channels_strings);
        mHDRVideoInfo = mContext.getResources().getStringArray(R.array.hdr_video_info_strings);
        tIFProgramManager=TIFProgramManager.getInstance(mContext);
        mMtkTvEventBase=new MtkTvEventBase();
        for (int i = 0; i < audioChannel.length; i++) {
          if(!TextUtils.isEmpty(audioChannel[i]) && audioChannel[i].endsWith("#CH")) {
            try {
              audioChannel[i] = mContext.getString(R.string.nav_num_CH, audioChannel[i].split("#")[0]);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
    }

    public boolean isPFHasGuidance(boolean isPF) {
        if(isIPChannel()) {
            ProgramInfo info = isPF ? mNowIProgramInfo : mNextIProgramInfo;
            return info != null && !TextUtils.isEmpty(info.getBasicDescriptionParentalGuidanceExplanatoryTest());
        } else {
            return isPF ?
                    mEvents[0] != null && !TextUtils.isEmpty(mEvents[0].getGuidanceText()) :
                    mEvents[1] != null && !TextUtils.isEmpty(mEvents[1].getGuidanceText());
        }
    }

    /**
     * get NavBannerImplement instance object
     *
     * @param context
     * @return
     */
    public static BannerImplement getInstanceNavBannerImplement(Context context) {
        if (null == mNavBannerImplement) {
            mNavBannerImplement = new BannerImplement(context);
        }
        return mNavBannerImplement;
    }

    public static void setBannerImplement(BannerImplement navBannerImplement) {
      mNavBannerImplement = navBannerImplement;
    }

    public String getBannerAudioInfoFor3rd(){
        String audioInfo="";
        TIFChannelInfo channelInfo=TIFChannelManager.getInstance(mContext).getChannelInfoByUri();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "channelInfo = " + channelInfo);
        if(channelInfo==null){
            return audioInfo;
        }
        List<TIFProgramInfo> tIFProInfoList= tIFProgramManager.queryProgramListWithGroupFor3rd((int)channelInfo.mId);
        if(tIFProInfoList !=null && !tIFProInfoList.isEmpty()){
            audioInfo =tIFProInfoList.get(0).mAudioLanguage;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " getBannerAudioInfoFor3rd audioInfo == " + audioInfo);
        }
        return audioInfo==null?"":audioInfo;
    }

    /**
     * get audio info string of the banner
     *
     * @return
     */
    public String getBannerAudioInfo(boolean isScrambled) {
      if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_MODULES_WITH_TIF)) {
        String currentAudioLang = "";
          if (TurnkeyUiMainActivity.getInstance() == null || TurnkeyUiMainActivity.getInstance().getTvView() == null) {
              Log.d(TAG, "TvView is null.");
              return currentAudioLang;
          }
        List<TvTrackInfo> audioTracks;
      if (CommonIntegration.TV_FOCUS_WIN_MAIN == mIntegration.getCurrentFocus()) {// main
        audioTracks = TurnkeyUiMainActivity.getInstance().getTvView().getTracks(TvTrackInfo.TYPE_AUDIO);
      } else {// sub
        audioTracks = TurnkeyUiMainActivity.getInstance().getPipView().getTracks(TvTrackInfo.TYPE_AUDIO);
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "audioTrack size:" + (audioTracks == null ? null:audioTracks.size()));
      Log.d(TAG, "audioTrack size:" + (audioTracks == null ? null:audioTracks.size()));

      if (audioTracks == null) {
        return currentAudioLang;
      }
      for (TvTrackInfo tvTrackInfo : audioTracks) {
          String tmpAudioDecType = tvTrackInfo.getExtra().getString("key_AudioDecodeType");
          Log.d(TAG, "tmpAudioDecType111= " + tmpAudioDecType);
      }
      if (audioTracks.isEmpty()) {
       if (mIntegration.isCurrentSourceTv()&& mIntegration.isCurCHAnalog()) {
           com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ATV Source.return null");
           //mMtkTvBanner.getAudioInfo();
//           String audioInfo = SundryImplement.getInstanceNavSundryImplement(mContext).getCurrentAudioLang();
//           com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getBannerAudioInfo, value == " + audioInfo);
         return null;
        }else if (mIntegration.isCurrentSourceTv()||mIntegration.isCurrentSourceHDMI()) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DTV or HDMI Source");
          MtkTvAVModeBase mAudioMode = MtkTvAVMode.getInstance();
          TvProviderAudioTrackBase mCurrentAudio = mAudioMode.getCurrentAudio();
          //check audio from getCurrentAudio if has info should show
          if(mCurrentAudio != null){
              return ""; // audio track size 0 and tvapi has audio. DTV01986724
              /*String currlang = m_current_audio.getAudioLanguage();
              if(TextUtils.isEmpty(currlang)){
                  currlang = "Unknown";
              }
              int audioChannelIdx = m_current_audio.getAudioChannelCount();
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "check audio from other api:"+audioChannelIdx);
              return currlang+" "+audioChannel[audioChannelIdx];*/
          }else{
              return "No Audio";
          }

        }
      }else {
          for (TvTrackInfo tvTrackInfo : audioTracks) {
              String tmpAudioDecType = tvTrackInfo.getExtra().getString("key_AudioDecodeType");
              Log.d(TAG, "tmpAudioDecType222= " + tmpAudioDecType);
          }
        audioTracks = filterAudioTracks(audioTracks);
        for (TvTrackInfo tvTrackInfo : audioTracks) {
            String tmpAudioDecType = tvTrackInfo.getExtra().getString("key_AudioDecodeType");
            Log.d(TAG, "tmpAudioDecType333= " + tmpAudioDecType);
        }
        String curTrackId = "";
        if (CommonIntegration.TV_FOCUS_WIN_MAIN == mIntegration.getCurrentFocus()) {
          curTrackId = TurnkeyUiMainActivity.getInstance().getTvView().getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
        } else {
          curTrackId = TurnkeyUiMainActivity.getInstance().getPipView().getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "curTrackId:"+curTrackId);
        Log.d(TAG, "curTrackId:"+curTrackId);
        currentAudioLang = getAudioChannelByTrackId(curTrackId,audioTracks);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentAudioChannel:"+currentAudioLang);
      }
      if (mIntegration.isCurrentSourceTv()&& mIntegration.isCurCHAnalog()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ATV Source");
        //mMtkTvBanner.getAudioInfo();
        String audioInfo = SundryImplement.getInstanceNavSundryImplement(mContext).getCurrentAudioLang();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getBannerAudioInfo, value == " + audioInfo);
        return audioInfo;
      }else {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "DTV source");
        if (!isScrambled) {
          return currentAudioLang + " "
            + SundryImplement.getInstanceNavSundryImplement(mContext).getCurrentAudioLang();
        } else {
          return SundryImplement.getInstanceNavSundryImplement(mContext).getCurrentAudioLang();
        }

      }
    }else {
      String audioInfo = mMtkTvBanner.getAudioInfo();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getBannerAudioInfo, value == " + audioInfo);
      return audioInfo;
    }
    }


    public int getDecType(){
       List<TvTrackInfo> tracks=null;
       String trackId=null;
        if (TurnkeyUiMainActivity.getInstance() == null || TurnkeyUiMainActivity.getInstance().getTvView() == null) {
            Log.d(TAG, "TvView is null.");
            return 0;
        }
       if (CommonIntegration.TV_FOCUS_WIN_MAIN == mIntegration.getCurrentFocus()) {// main
          tracks = TurnkeyUiMainActivity.getInstance().getTvView().getTracks(TvTrackInfo.TYPE_AUDIO);
          trackId = TurnkeyUiMainActivity.getInstance().getTvView().getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
       } else {// sub
          tracks = TurnkeyUiMainActivity.getInstance().getPipView().getTracks(TvTrackInfo.TYPE_AUDIO);
          trackId = TurnkeyUiMainActivity.getInstance().getPipView().getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
       }
       int iDecType = 0;
       int trackIdx = -1;
       if(tracks==null||tracks.isEmpty()){
           com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "getDecType tracks==null or tracks is empty!");
       }
       else{
           for (int i = 0; i < tracks.size(); i++) {
               if (tracks.get(i).getId().equals(trackId)) {
                   trackIdx = i;
                   break;
              }
           }
       }
       int audioChannelIdx = 0;
       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "decType,trackIdx: "+trackIdx);
       if(trackIdx == -1){
           TvProviderAudioTrackBase mCurrentAudio = MtkTvAVMode.getInstance().getInputSourceCurrentAudio();
           if (mCurrentAudio != null) {
               iDecType = mCurrentAudio.getAudioDecodeType();
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDecType,iDecType: "+iDecType);
           }
       }else{
           String decType = null;
           if (null != tracks.get(trackIdx).getExtra()) {
               decType = tracks.get(trackIdx).getExtra().getString("key_AudioDecodeType");
           }
           audioChannelIdx = tracks.get(trackIdx).getAudioChannelCount();
           com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDecType,decType: "+decType + ",audioChannelIdx:"+audioChannelIdx);
           iDecType = parseDecTypeForAudioTrack(decType);
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDecType,iDecType: "+iDecType);
      return iDecType;
    }


    private String getAudioChannelByTrackId(String trackId,List<TvTrackInfo> tracks){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAudioChannelByTrackId,trackId:" +
                 trackId + "tracks size:" + tracks.size());
        int trackIdx = -1;
        Log.d(TAG, "trackId= "+ trackId);
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).getId().equals(trackId)) {
                trackIdx = i;
                String tmpAudioDecType = tracks.get(i).getExtra().getString("key_AudioDecodeType");
                Log.d(TAG, "trackIdx= "+ trackIdx +", tmpAudioDecType= " + tmpAudioDecType);
                break;
           }
        }
        int iDecType = -1;
        int audioChannelIdx = 0;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAudioChannelByTrackId,trackIdx: "+trackIdx);
        if (!tracks.isEmpty()) {
            if(trackIdx == -1){
                MtkTvAVModeBase mAudioMode = MtkTvAVMode.getInstance();
                TvProviderAudioTrackBase mCurrentAudio = mAudioMode.getCurrentAudio();
                // check audio from getCurrentAudio if has info should show
                if (mCurrentAudio != null) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "audio----000mAudioMode.getCurrentAudio all info: "+
                             mCurrentAudio.toString());
                    iDecType = mCurrentAudio.getAudioDecodeType();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "audio----000000000000000000type:" + iDecType);
                    audioChannelIdx = mCurrentAudio.getAudioChannelCount();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "audio----000000000000000000audioChannelIdx:" + audioChannelIdx);
                }
            }else{
                String decType = null;
                if (null != tracks.get(trackIdx).getExtra()) {
                    decType = tracks.get(trackIdx).getExtra().getString("key_AudioDecodeType");
                }
                Log.d(TAG, "trackIdx= "+ trackIdx +", decType= " + decType);
                audioChannelIdx = tracks.get(trackIdx).getAudioChannelCount();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "audio----getAudioChannelByTrackId audioChannelIdx:"
                         + audioChannelIdx);
                iDecType = parseDecTypeForAudioTrack(decType);
           }
          String decAudioStr = "";
            if (iDecType == 1) {
                decAudioStr = mContext.getString(R.string.audio_type_ac3);
            } else
          if (iDecType == 4) {// AUD_DECODE_TYPE_AAC
              decAudioStr = mContext.getString(R.string.audio_type_aac);
          } else if (iDecType == 5) {// AUD_DECODE_TYPE_HEAAC
              decAudioStr = mContext.getString(R.string.audio_type_he_aac);
          } else if (iDecType == 6) {// AUD_DECODE_TYPE_HEAAC_V2
              decAudioStr = mContext.getString(R.string.audio_type_he_aacv2);
          }else if (iDecType == 10) {// AUD_DECODE_TYPE_MPEG1_L2
              decAudioStr =  mContext.getString(R.string.audio_type_mpeg1l2);
          }
          else if (iDecType == 15||iDecType == 16||iDecType == 17||iDecType == 21) {// AUD_DECODE_TYPE_DTS
              decAudioStr = showDTSInfo(iDecType);
          }
          else {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "audio----No need to display this audio dec type");
          }
          int doblyType=getDoblyType();
          //Dolby suggestion for AAC & HEAAC only show decAudioStr.
          if((doblyType==DOBLY_TYPE_AUDIO||doblyType==DOBLY_TYPE_ATOMS) && (iDecType==4||iDecType==5)){
        	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "No need append audioChannel!");
        	  return decAudioStr;
          }
          else{
        	  return (decAudioStr+" "+audioChannel[audioChannelIdx]);
          }
       } else {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "audio----getAudioChannelByTrackId audioChannelIdx:null");
        return "UnknownX";
       }
    }

    private int parseDecTypeForAudioTrack(String decType) {
        int iDecType = 0;
        try {
            if (!TextUtils.isEmpty(decType)) {
                iDecType = Integer.parseInt(decType);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "audio----decType11=="+decType+", iDecType="+iDecType);
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "audio----decType22=="+ decType);
            }
        } catch (NumberFormatException ex) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ex audio----decType33=="+ decType);
        }
        return iDecType;
    }

    public int getDoblyType(){
        int doblyType=DOBLY_TYPE_NONE;
        TIFChannelInfo curChannel= TIFChannelManager.getInstance(mContext).getCurrChannelInfo();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDoblyType----->curChannel="+curChannel);
        if (mIntegration.isCurrentSourceTv()&&(curChannel!=null&&!curChannel.isAnalogService())||mIntegration.isCurrentSourceHDMI()){
            boolean audioAtoms=mMtkTvBanner.isDisplayADAtmos();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDoblyType----->audioAtoms="+audioAtoms);
            if(audioAtoms){
                int doblyVersion=mIntegration.getDoblyVersion();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDoblyType----->doblyVersion="+doblyVersion);
                //doblyVersion==SCC_AUD_DOLBY_MS_VERSION_12V2Z( value = 5) only show dobly atmos,other not show
                //dolby 2.2  show  -- value = 5
                //dolby 2.4Y show -- value = 6
                //dolby 2.4Z show -- value = 7
                //doblyType=doblyVersion==5?DOBLY_TYPE_ATOMS:DOBLY_TYPE_AUDIO;
                if(doblyVersion == 5 || doblyVersion == 6 || doblyVersion == 7) {
                    if (DoblyUtil.isSupportDolbyAtmos(mContext)) {
                        doblyType=DOBLY_TYPE_ATOMS;
                    } else {
                        doblyType=DOBLY_TYPE_AUDIO;
                    }
                } else {
                  doblyType=DOBLY_TYPE_AUDIO;
                }
                //doblyType=doblyVersion==5?DOBLY_TYPE_ATOMS:DOBLY_TYPE_AUDIO;
            }else {
                int decType=getDecType();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDoblyType----->decType="+decType);
                setDecTypeTemp(decType);
                // ac3=DD dectype=1/eac3=DD+ dectype=2 / ac4=18 / MAT=23
                if (decType == 1 || decType == 2 || decType == 18 || decType == 23 ) {
                    doblyType=DOBLY_TYPE_AUDIO;
                }
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDoblyType----->doblyType="+doblyType);
        }
        return doblyType;
    }

    private void setDecTypeTemp(int decType) {
        mDecType = decType;
    }

    public int getDecTypeTemp() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDecTypeTemp----->mDecType="+mDecType);
        return mDecType;
    }

    public String getDtsAudioInfo() {
        String dtsDecAudioStr = showDTSInfo(getDecTypeTemp());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDtsAudioInfo----->dtsDecAudioStr="+dtsDecAudioStr);
        return dtsDecAudioStr;
    }

    public String showDTSInfo(int dectype) {
        String dtsDecAudioStr = "";
        MtkTvAVModeBase mAudioMode = MtkTvAVMode.getInstance();
        int dtsVersion = mAudioMode.getAudioInfoValue(MtkTvAVModeBase.AUDIO_INFO_GET_TYPE_DTS_VERSION);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getDtsAudioInfo----->dtsVersion="+dtsVersion);
        switch(dectype) {
        case 15: // AUD_DECODE_TYPE_DTS
            dtsDecAudioStr =  mContext.getString(R.string.audio_type_dts);
            break;
        case 16: // AUD_DECODE_TYPE_DTS_EXPRESS
            if (dtsVersion == 3) {
                dtsDecAudioStr =  mContext.getString(R.string.audio_type_dts_hd_p);
            } else if (dtsVersion == 2||dtsVersion == 1) {
                dtsDecAudioStr =  mContext.getString(R.string.audio_type_dts_express_p);
            }
            break;
        case 17: // AUD_DECODE_TYPE_DTS_HD
            if (dtsVersion == 3) {
                dtsDecAudioStr =  mContext.getString(R.string.audio_type_dts_hd_p);
            } else if (dtsVersion == 2||dtsVersion == 1) {
                dtsDecAudioStr =  mContext.getString(R.string.audio_type_dts_hd_master_audio);
            }
            break;
        case 21: // AUD_DECODE_TYPE_DTS_X
            if (dtsVersion == 3) {
                dtsDecAudioStr =  mContext.getString(R.string.audio_type_dts_x);
            } else if (dtsVersion == 2||dtsVersion == 1) {
                dtsDecAudioStr =  mContext.getString(R.string.audio_type_dts);
            }
            break;
        default:
            dtsDecAudioStr = "";
            break;
        }
        return dtsDecAudioStr;
    }

    private List<TvTrackInfo> filterAudioTracks(List<TvTrackInfo> tracks){
        List<TvTrackInfo> filterTracks = new ArrayList<TvTrackInfo>();
        List<TvTrackInfo> filterVITracks = new ArrayList<TvTrackInfo>();
        List<String> audioLangs = new ArrayList<String>();
        for(TvTrackInfo tk:tracks){
            filterTracks.add(tk);
            String type = null;
            String mixtype = null;
            String eClass = null;
            if (null != tk.getExtra()) {
                type = tk.getExtra().getString("key_AudioType");
                mixtype = tk.getExtra().getString("key_AudioMixType");
                eClass = tk.getExtra().getString("key_AudioEditorialClass");
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "filterAudioTracks:audiotype==" + type + ",mixtype=" + mixtype
                        + ",eclass=" + eClass);
            }

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

//          if(type.equals("0")){//normal
//              filterTracks.add(tk);
//              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "filterAudioTracks:normal=="+tk.getId());
//          }else if(type.equals("3")){//visually impaired
////                filterVITracks.add(tk);
//              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "filterAudioTracks:VI id =="+tk.getId());
//          }else{//other
//              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "filterAudioTracks:type is other=="+type);
//          }
        }
        filterTracks.removeAll(filterVITracks);
        for(TvTrackInfo tk:filterTracks){
            audioLangs.add(tk.getLanguage());
        }

        filterTracks.clear();
        for(TvTrackInfo tk:filterVITracks){
            String tkLang = tk.getLanguage();
            for(String lang:audioLangs){
                if(tkLang.equals(lang)){
                    filterTracks.add(tk);
                }
            }
        }

        tracks.removeAll(filterTracks);
        for(TvTrackInfo tk:tracks){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "filterAudioTracks:list tk.getLanguage =="+tk.getLanguage());
        }
        return tracks;
//      if(filterVITracks.size() >0){
//          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "filterAudioTracks:return filterVITracks ==");
//          return filterVITracks;
//      }else{
//          return filterTracks;
//      }
    }

    /**
     * get caption info string of the banner
     *
     * @return
     */
    public String getBannerCaptionInfo() {
        return getBannerCaptionInfo(false);
    }

    public String getBannerCaptionInfo(boolean isPF) {
        if(mIntegration.is3rdTVSource()){
            mCurCaptionInfo = TurnkeyUiMainActivity.getInstance().getTvView().getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in is3rdTVSource getBannerCaptionInfo, value == " + mCurCaptionInfo);
        }else{
            if(isIPChannel()) {
                ProgramInfo info = isPF ? mNowIProgramInfo : mNextIProgramInfo;
                if (info != null) {
                    mCurCaptionInfo = info.getCaptionLanguage();
                }
            } else {
                mCurCaptionInfo = mMtkTvBanner.getCaption();
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getBannerCaptionInfo, value == " + mCurCaptionInfo);
        }
        if(MarketRegionInfo.REGION_EU == MarketRegionInfo
                .getCurrentMarketRegion()){
            if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_COL) && CommonIntegration.getInstance().isCurrentSourceATV()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "col ntsc, direct return." + mCurCaptionInfo);
                return mCurCaptionInfo;
            }
            if(!TextUtils.isEmpty(mCurCaptionInfo)) {
                String[] strings = mCurCaptionInfo.split(" ");
                mCurCaptionInfo = mLanguageUtil.getSubitleNameByValue(strings[0]);
                if (strings.length == 2) {
                    mCurCaptionInfo = CommonIntegration.getSafeString(mCurCaptionInfo, " ", strings[1]);
                }
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getBannerCaptionInfo, getSubitleNameByValue:mCurCaptionInfo == " + mCurCaptionInfo);
        }
        return mCurCaptionInfo;
    }

    /**
     * get current channel name string of the banner
     *
     * @return
     */
    public String getCurrentChannelName() {
        String channelName="";
        if(mIntegration.is3rdTVSource()){
            channelName=getCurrentTifchannel().mDisplayName;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getCurrentChannelName is3rdTVSource, channelName == " + channelName);
        }else{
            channelName = mIntegration.getAvailableString(mMtkTvBanner.getChannelName());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getCurrentChannelName TVSource, channelName == " + channelName);
        }
        return channelName;
    }

    public boolean isCurrentHiddenChannel() {
      MtkTvChannelInfoBase channel = mIntegration.getChannelById(mIntegration.getCurrentChannelId());
      boolean result = channel != null && !mIntegration.checkChMask(channel,
          CommonIntegration.CH_LIST_MASK, CommonIntegration.CH_LIST_VAL);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isCurrentHiddenChannel:" + result);
      return result;
    }

    /**
     * get the current channel number string of the banenr
     *
     * @return
     */
    public String getCurrentChannelNum() {
        String channelNumber="";
        if(mIntegration.is3rdTVSource()){
            channelNumber=getCurrentTifchannel().mDisplayNumber;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getCurrentChannelNum is3rdTVSource, channelNumber == " + channelNumber);
        }else{
            channelNumber = mMtkTvBanner.getChannelNumber();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getCurrentChannelNum, TVSource channelNumber == " + channelNumber);
            if (mIntegration.isCurrentSourceATVforEuPA() && !TextUtils.isEmpty(channelNumber)) {
                channelNumber = mIntegration.getAnalogChannelDisplayNumInt(channelNumber) + "";
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getCurrentChannelNum, PA ATVSource channelNumber == " + channelNumber);
            }
        }
        return channelNumber;
    }

    /**
     * get current input source name of the banner
     *
     * @return
     */
    public String getCurrentInputName() {
        if(mIntegration.is3rdTVSource()){
            return getCurrentTifchannel().mInputServiceName;
        }else{
            String inputName = mMtkTvBanner.getIptsName();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getCurrentInputName, value == " + inputName);
            return inputName;
        }
    }

    /**
     * get input source caption info of the banner
     *
     * @return
     */
    public String getInputCaptionInfo() {
    /*  if(mIntegration.is3rdTVSource()){
            return "";
        }else{*/
            String inputCaptionInfo = mMtkTvBanner.getIptsCC();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getInputCaptionInfo, value == "
                    + inputCaptionInfo);
            return inputCaptionInfo;
        //}
    }

    /**
     * get rating info if the current input source is not TV
     *
     * @return
     */
    public String getInputRating() {
        /*if(mIntegration.is3rdTVSource()){
            return "";
        }else{*/
            String inputRating = mMtkTvBanner.getIptsRating();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getInputRating, value == " + inputRating);
            return inputRating;
        // }
    }

    /**
     * get the resolution info of the current input source is not TV
     *
     * @return
     */
    public String getInputResolution() {
        if(mIntegration.is3rdTVSource()){
            return mMtkTvBanner.getIptsRslt();
        }else{
            String inputResolution = mMtkTvBanner.getIptsRslt();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getInputResolution, value == " + inputResolution);
            return inputResolution;
        }
    }

    /**
     * get special info string of the special state
     *
     * @return
     */
    public String getSpecialMessage() {
    /*  if(mIntegration.is3rdTVSource()){
            return "";
        }else{*/
            String specialMessage = mMtkTvBanner.getMsg();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getSpecialMessage, value == " + specialMessage);
            return specialMessage;
    //  }
    }

    /**
     * get next program time string of the banner
     *
     * @return
     */
    public String getNextProgramTime() {
        if (isIPChannel()) {
            if (mNextIProgramInfo!=null) {
                String progTime = IPChannelDataReader.getInstance().parseDuration(
                        mNextIProgramInfo.getPublishedStartTime(), mNextIProgramInfo.getPublishedDuration());
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " mNextIProgramInfo getNextProgTime progTime, value == " + progTime);
                return  progTime;
            }
        }
        int timeType= EPGUtil.judgeFormatTime(mContext);
        if(mIntegration.is3rdTVSource()){
            String pattern=timeType==1?"MM-dd HH:mm":"MM-dd hh:mm,a";
            SimpleDateFormat sdf=new SimpleDateFormat(pattern,Locale.ENGLISH);
            List<TIFProgramInfo> tIFProInfoList= tIFProgramManager.queryProgramListWithGroupFor3rd((int)TIFChannelManager.getInstance(mContext).getChannelInfoByUri().mId);
            if(tIFProInfoList !=null && tIFProInfoList.size()>1){
                String programTime = sdf.format(new Date(tIFProInfoList.get(1).mStartTimeUtcSec*1000)) +" - "+sdf.format(new Date(tIFProInfoList.get(1).mEndTimeUtcSec*1000));
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in getNextProgramTime, mStartTimeUtcSec " +tIFProInfoList.get(1).mStartTimeUtcSec +" mEndTimeUtcSec " +tIFProInfoList.get(1).mEndTimeUtcSec);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in getNextProgramTime, value == " + programTime);
                return  programTime;
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in getNextProgramTime, value == " );
                return "";
            }
        }else{
            String nextProgramTime = mMtkTvBanner.getNextProgTime();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "before come in getNextProgramTime, value == " + nextProgramTime);
            nextProgramTime=convertFormatTimeByType(nextProgramTime,timeType);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "after come in getNextProgramTime, value == " + nextProgramTime);
            return nextProgramTime;
        }
    }

    /**
     * get next program title of the banner
     *
     * @return
     */
    public String getNextProgramTitle() {
        if (isIPChannel()) {
            if (mNextIProgramInfo!=null) {
                String programTitle = mNextIProgramInfo.getMainTitle();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " mNextIProgramInfo in getNextProgramTitle, value == " + programTitle);
                return  programTitle;
            }
        }
        if(mIntegration.is3rdTVSource()){
        	TIFChannelInfo channelInfo=TIFChannelManager.getInstance(mContext).getChannelInfoByUri();
            if(channelInfo==null){
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, " channelInfo==null" );
                return "";
            }
            List<TIFProgramInfo> tIFProInfoList= tIFProgramManager.queryProgramListWithGroupFor3rd((int)channelInfo.mId);
            if(tIFProInfoList !=null && tIFProInfoList.size()>1){
                String programTitle = tIFProInfoList.get(1).mTitle;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in getProgramTitle, value == " + programTitle);
                return  programTitle;
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in getProgramTime, value == " );
                return "";
            }
        }else{
            String nextProgramTitle = mMtkTvBanner.getNextProgTitle();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getNextProgramTitle, value == "
                    + nextProgramTitle);
            if(TextUtils.isEmpty(nextProgramTitle)){
              nextProgramTitle = mIntegration.isCurCHAnalog()?"":mContext.getString(R.string.banner_no_program_title);
            }
            return nextProgramTitle;
        }
    }
    /**
     * get ChannelLogo of the banner
     *
     * @return
     */
    public String getChannelLogo(){
        return mMtkTvBanner.getChannelLogo();
    }
    /**
     * get progSecondaryTitle of the banner
     *
     * @return
     */
    public String getProgSecondaryTitle(){
        String progSecondaryTitle = "";
        if (isIPChannel()) {
            if (mNowIProgramInfo!=null) {
                progSecondaryTitle = mNowIProgramInfo.getSecondaryTitle();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " mNowIProgramInfo in getProgSecondaryTitle, value == " + progSecondaryTitle);
                return  progSecondaryTitle;
            }
        } else {
            progSecondaryTitle = mMtkTvBanner.getProgSecondaryTitle();
        }
        return progSecondaryTitle;
    }
    public String getNextProgDetail() {
        if (isIPChannel()) {
            if (mNextIProgramInfo!=null) {
                String nextProgDetail = mNextIProgramInfo.getMediumSynopsis();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " mNextIProgramInfo in getNextProgDetail, value == " + nextProgDetail);
                return  nextProgDetail;
            }
        }

        if(mIntegration.is3rdTVSource()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getNextProgramDetails, 3rd.");
            return "";
        }else{
            String programDetails = mMtkTvBanner.getNextProgDetail();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getNextProgramDetails, value == " + programDetails);
            if(TextUtils.isEmpty(programDetails)){
                programDetails = mIntegration.isCurCHAnalog()?"":mContext.getString(R.string.nav_No_program_details);
            }
            return mIntegration.getAvailableStringForProg(programDetails);
        }
    }

    @SuppressLint("SimpleDateFormat")
    public int getCurrentChannelProgress(){
        if (isIPChannel()) {
            if(mNowIProgramInfo != null) {
                long seconds = mMtkTvTime.getBroadcastLocalTime().toSeconds();
                try {
                    Date startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(mNowIProgramInfo.getPublishedStartTime());
                    if (startDate != null) {
                        long fvpDuration = IPChannelDataReader.getInstance().getFvpDuration(mNowIProgramInfo.getPublishedDuration());
                        if (fvpDuration == 0) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentChannelProgress fvpDuration = 0.");
                            return 0;
                        }
                        int progress = (int) (((seconds * 1000 - startDate.getTime()) * 100) / (fvpDuration * 1000));
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentChannelProgress===="+progress);
                        return progress< 0 || progress > 100 ? 0 : progress;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentChannelProgress ip channel no progress.");
            return 0;
        }
        int progress=0;
        String programTime =getProgramTime();
        String currentTimeString = getCurrentTime();
        if(programTime.contains("AM")||programTime.contains("PM")){
            programTime=programTime.replace("AM", "");
            programTime=programTime.replace("PM", "");
            programTime=programTime.replace(" ", "");
            programTime=programTime.replace("  ", "");
            currentTimeString=currentTimeString.replace("AM", "");
            currentTimeString=currentTimeString.replace("PM", "");
            currentTimeString=currentTimeString.replace(" ", "");
            currentTimeString=currentTimeString.replace("  ", "");
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "programTime===="+programTime+",currentTimeString===="+currentTimeString);
        }
        String[] timeArr=programTime.split("-");
        if(timeArr!=null&&timeArr.length==2){
            int startTime=timeToSecond(timeArr[0]);
            int endTime=timeToSecond(timeArr[1]);
            int currentTime=timeToSecond(currentTimeString);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime===="+startTime+",currentTime===="+currentTime+",endTime===="+endTime);
            if(startTime == endTime) {
                return 0;
            }
            progress=(int)(((float)(currentTime-startTime)/(float)(endTime-startTime))*100f);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "progress===="+progress);
      return progress;
    }
    private int timeToSecond(String time){
        String[] times=time.split(":");
        if(times!=null&&times.length==2){
            int hour=Integer.parseInt(times[0]);
            int min=Integer.parseInt(times[1]);
            return hour*60+min;
        }
        return 0;
    }
    @SuppressLint("SimpleDateFormat")
    public int getNextChannelProgress(){
        if (isIPChannel()) {
            if(mNextIProgramInfo != null) {
                long seconds = mMtkTvTime.getBroadcastLocalTime().toSeconds();
                try {
                    Date startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(mNextIProgramInfo.getPublishedStartTime());
                    if (startDate != null) {
                        long fvpDuration = IPChannelDataReader.getInstance().getFvpDuration(mNextIProgramInfo.getPublishedDuration());
                        if (fvpDuration == 0) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getNextChannelProgress fvpDuration = 0.");
                            return 0;
                        }
                        int progress = (int) (((seconds * 1000 - startDate.getTime()) * 100) / (fvpDuration * 1000));
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getNextChannelProgress===="+progress);
                        return progress< 0 || progress > 100 ? 0 : progress;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getNextChannelProgress ip channel no progress.");
            return 0;
        }
        int progress=0;
        String programTime =getNextProgramTime();
        String currentTimeString = getCurrentTime();
        if(programTime.contains("AM")||programTime.contains("PM")){
            programTime=programTime.replace("AM", "");
            programTime=programTime.replace("PM", "");
            programTime=programTime.replace(" ", "");
            programTime=programTime.replace("  ", "");
            currentTimeString=currentTimeString.replace("AM", "");
            currentTimeString=currentTimeString.replace("PM", "");
            currentTimeString=currentTimeString.replace(" ", "");
            currentTimeString=currentTimeString.replace("  ", "");
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "programTime===="+programTime+",currentTimeString===="+currentTimeString);
        }
        String[] timeArr=programTime.split("-");
        if(timeArr!=null&&timeArr.length==2){
            int startTime=timeToSecond(timeArr[0]);
            int endTime=timeToSecond(timeArr[1]);
            int currentTime=timeToSecond(currentTimeString);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime===="+startTime+",currentTime===="+currentTime+",endTime===="+endTime);
            if (startTime == endTime) {
                return 0;
            }
            progress=(int)(((float)(currentTime-startTime)/(float)(endTime-startTime))*100f);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "progress===="+progress);
      return progress;
    }

    public String getChannelDurationMin(boolean isPF, String progrAMTime) {
      long durationMills = 0;
      if(isPF) {
        if(mEvents[0] != null) {
          durationMills = mEvents[0].getDuration();
        }
      } else {
        if(mEvents[1] != null) {
          durationMills = mEvents[1].getDuration();
        }
      }
      if(durationMills <= 0) {
        return formatDuration(getChannelDurationMin(progrAMTime));
      } else {
        return formatDuration(durationMills);
      }
    }

    public String formatDuration(long duration) {
      StringBuilder sb = new StringBuilder();
      int secDuration = (int)(duration % 60);
      int hourDuration = (int) (duration / (60 * 60));
      int minDuration = (int) ((duration - hourDuration * 60 * 60)/60);

      if(hourDuration > 0) {
        sb.append(" ");
        sb.append(mContext.getResources().
            getQuantityString(R.plurals.duration_hour, hourDuration, hourDuration));
        sb.append(" ");
      }
      if(minDuration > 0) {
        sb.append(" ");
        sb.append(mContext.getResources().
            getQuantityString(R.plurals.duration_minute, minDuration, minDuration));
        sb.append(" ");
      }
      if(secDuration > 0) {
        sb.append(" ");
        sb.append(mContext.getResources().
            getQuantityString(R.plurals.duration_second, secDuration, secDuration));
        sb.append(" ");
      }
      return sb.toString().trim();
    }

    public int getChannelDurationMin(String progrAMTime) {
      int progress = 0;
      String tempProgrAMTime = progrAMTime;
      if (progrAMTime.contains("AM") || progrAMTime.contains("PM")) {
        progrAMTime = progrAMTime.replace("AM", "");
        progrAMTime = progrAMTime.replace("PM", "");
        progrAMTime = progrAMTime.replace(" ", "");
        progrAMTime = progrAMTime.replace("  ", "");
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "progrAMTime====" + progrAMTime);
      String[] timeArr = progrAMTime.split("-");
      String[] tempTimeArr = tempProgrAMTime.split("-");

      if (timeArr != null && timeArr.length == 2) {
        String startTime = tempTimeArr[0];
        String endTime = tempTimeArr[1];
        int startTimeMin = timeToSecond(timeArr[0].trim());
        int endTimeMin = timeToSecond(timeArr[1].trim());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime: " + startTime + ", endTime:" + endTime);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTimeMin: " + startTimeMin + ", endTimeMin:" + endTimeMin);
        if (startTime.contains("AM") && endTime.contains("AM") ||
            startTime.contains("PM") && endTime.contains("PM")) {
          if (endTimeMin > startTimeMin) {
            if (endTimeMin >= 12 * 60) {
              // 04:00PM-12:00PM
              progress = endTimeMin + 12 * 60 - startTimeMin;
            } else {
              // 04:00AM-05:00AM
              // 04:00PM-05:00PM
              progress = endTimeMin - startTimeMin;
            }
          } else {
            if (startTimeMin >= 12 * 60) {
              // 12:00PM-02:00PM
              progress = endTimeMin + 12 * 60 - startTimeMin;
            } else {
              // 04:00AM-02:00AM
              // 04:00PM-02:00PM
              progress = endTimeMin + 24 * 60 - startTimeMin;
            }
          }
        } else if (startTime.contains("AM") && endTime.contains("PM") ||
            startTime.contains("PM") && endTime.contains("AM")) {
          if (endTimeMin >= 12 * 60) { //12:30 PM
            // 04:00AM-12:00PM
            progress = endTimeMin - startTimeMin;
          } else {
            // 04:00AM-05:00PM
            // 04:00AM-02:00PM
            // 04:00PM-05:00AM
            // 04:00PM-02:00AM
            // 12:30PM-02:00AM
            if(startTimeMin >= 12*60) {
              progress = 12*60 + endTimeMin -(startTimeMin - 12*60);
            } else {
              progress = endTimeMin + 12 * 60 - startTimeMin;
            }
          }
        } else {
          if (endTimeMin > startTimeMin) {
            progress = endTimeMin - startTimeMin;
          } else {
            progress = endTimeMin + 24 * 60 - startTimeMin;
          }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime====" + startTimeMin + ",endTime====" + startTimeMin);
      }

      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "progress====" + progress);
      return Math.abs(progress);
    }

    /**
     * get nextProgSecondaryTitle of the banner
     *
     * @return
     */
    public String getNextProgSecondaryTitle(){
        if (isIPChannel()) {
            if (mNextIProgramInfo!=null) {
                String progSecTitle = mNextIProgramInfo.getSecondaryTitle();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " mNextIProgramInfo in getNextProgSecondaryTitle, value == " + progSecTitle);
                return  progSecTitle;
            }
        }
        return mMtkTvBanner.getNextProgSecondaryTitle();
    }

    /**
     * get next program category
     *
     * @return
     */
    public String getNextProgramCategory() {
        if(mIntegration.is3rdTVSource()){
            /*List<TIFProgramInfo> tIFProInfoList= tIFProgramManager.queryProgramListWithGroupFor3rd((int)TIFChannelManager.getInstance(mContext).getChannelInfoByUri().mId);
            if( tIFProInfoList !=null && tIFProInfoList.size()>0){
                String programCategory = tIFProInfoList.get(0).mCanonicalGenre;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in programCategory, value == " + programCategory);
                return  programCategory;
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in programCategory, value == " );
                return "";
            }*/
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in programCategory, value == " );
            return "";
        }else{
            int categoryIndex=-1;
            String[] categoryArr = null;
            if (isIPChannel()) {
                if(mNextIProgramInfo != null) {
                    categoryIndex = mNextIProgramInfo.getBasicDescriptionGenreCategoryIndex();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getNextProgramCategory, ip categoryIndex == " + categoryIndex);
                    categoryArr = mContext.getResources()
                            .getStringArray(R.array.nav_banner_ipchannel_category_array);
                }
            } else {
                try{
                    String strProgCategoryIdx=mMtkTvBanner.getNextProgCategoryIdx();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getProgramCategory, strProgCategoryIdx == " + strProgCategoryIdx);
                    if (TextUtils.isEmpty(strProgCategoryIdx)) {
                        return "";
                    }
                    categoryIndex=Integer.parseInt(strProgCategoryIdx);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getProgramCategory, categoryIndex == " + categoryIndex);
                }catch(NumberFormatException e){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "come in getProgramCategory Parse ProgCategoryIdx exception!");
                    e.printStackTrace();
                }
                if (CommonIntegration.isEURegion()) {
                    categoryArr = mContext.getResources()
                            .getStringArray(R.array.nav_banner_category_array);
                } else if (CommonIntegration.isSARegion()) {
                    categoryArr = mContext.getResources()
                            .getStringArray(R.array.nav_banner_filter_sa_type);
                }
            }

            if (categoryArr==null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "come in getNextProgramCategory categoryArr is null!");
                return "";
            }
            if(categoryIndex>=categoryArr.length||categoryIndex<0){
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "come in getNextProgramCategory Invalid categoryIndex!");
                return "";
            }
            String programCategory = categoryArr[categoryIndex];
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getNextProgramCategory, value == " + programCategory);
            return programCategory;
        }
    }

    /**
     * get program category
     *
     * @return
     */
    public String getProgramCategory() {
        if(mIntegration.is3rdTVSource()){
            List<TIFProgramInfo> tIFProInfoList= tIFProgramManager.queryProgramListWithGroupFor3rd((int)TIFChannelManager.getInstance(mContext).getChannelInfoByUri().mId);
            if( tIFProInfoList !=null && !tIFProInfoList.isEmpty()){
                String programCategory = tIFProInfoList.get(0).mCanonicalGenre;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in programCategory, value == " + programCategory);
                return  programCategory;
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in programCategory, value == " );
                return "";
            }
        }else{
            int categoryIndex=-1;
            String[] categoryArr = null;
            if (isIPChannel()) {
                if(mNowIProgramInfo != null) {
                    categoryIndex = mNowIProgramInfo.getBasicDescriptionGenreCategoryIndex();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getProgramCategory, ip categoryIndex == " + categoryIndex);
                    categoryArr = mContext.getResources()
                            .getStringArray(R.array.nav_banner_ipchannel_category_array);
                }
            } else {
                try{
                    String strProgCategoryIdx=mMtkTvBanner.getProgCategoryIdx();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getProgramCategory, strProgCategoryIdx == " + strProgCategoryIdx);
                    if (TextUtils.isEmpty(strProgCategoryIdx)) {
                        return "";
                    }
                    categoryIndex=Integer.parseInt(strProgCategoryIdx);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getProgramCategory, categoryIndex == " + categoryIndex);
                }catch(NumberFormatException e){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "come in getProgramCategory Parse ProgCategoryIdx exception!");
                    e.printStackTrace();
                }
                if (CommonIntegration.isEURegion()) {
                    categoryArr = mContext.getResources()
                            .getStringArray(R.array.nav_banner_category_array);
                } else if (CommonIntegration.isSARegion()) {
                    categoryArr = mContext.getResources()
                            .getStringArray(R.array.nav_banner_filter_sa_type);
                }
            }

            if (categoryArr==null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "come in getProgramCategory categoryArr is null!");
                return "";
            }
            if(categoryIndex>=categoryArr.length||categoryIndex<0){
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "come in getProgramCategory Invalid categoryIndex!");
                return "";
            }
            String programCategory = categoryArr[categoryIndex];
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getProgramCategory, value == " + programCategory);
            return programCategory;
        }
    }

    /**
     * get current program details
     *
     * @return
     */
    public String getProgramDetails() {
        if (isIPChannel()) {
            if (mNowIProgramInfo!=null) {
                String programDetail = mNowIProgramInfo.getMediumSynopsis();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " mNowIProgramInfo in getProgramDetails, value == " + programDetail);
                return  programDetail;
            }
        }
        if(mIntegration.is3rdTVSource()){
             List<TIFProgramInfo> tIFProInfoList= tIFProgramManager.queryProgramListWithGroupFor3rd((int)TIFChannelManager.getInstance(mContext).getChannelInfoByUri().mId);
                if( tIFProInfoList !=null && !tIFProInfoList.isEmpty()){
                    String programDetails = tIFProInfoList.get(0).mLongDescription;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in programDetails, value == " + programDetails);
                    return  programDetails;
                }else{
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in getProgramTitle, value == " );
                    return "";
                }
        }else{
            String programDetails = mMtkTvBanner.getProgDetail();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getProgramDetails, value == " + programDetails);
            if(TextUtils.isEmpty(programDetails)){
              programDetails = mIntegration.isCurCHAnalog()?"":mContext.getString(R.string.nav_No_program_details);
            }
            return mIntegration.getAvailableStringForProg(programDetails);
        }
    }

    /**
     * get program details page index
     *
     * @return
     */
    public String getProgramPageIndex() {
        /*if(mIntegration.is3rdTVSource()){
            return "";
        }else{*/
            String programPageIndex = mMtkTvBanner.getProgDetailPageIdx();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getProgramPageIndex, value == "
                    + programPageIndex);
            return programPageIndex;
    //  }
    }

    public String getIPProgramTimeMins() {
        if (isIPChannel()) {
            if (mNowIProgramInfo!=null) {
                long progTimeMins = IPChannelDataReader.getInstance().
                        getFvpDuration(mNowIProgramInfo.getPublishedDuration());
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " mNowIProgramInfo getIPProgramTimeMins progTimeMins, value == " + progTimeMins);
                return  formatDuration(progTimeMins);
            }
        }

        return null;
    }

    public String getIPNextProgramTimeMins() {
        if (isIPChannel()) {
            if (mNextIProgramInfo!=null) {
                long progTimeMins = IPChannelDataReader.getInstance().
                        getFvpDuration(mNextIProgramInfo.getPublishedDuration());
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " mNextIProgramInfo getIPNextProgramTimeMins progTimeMins, value == " + progTimeMins);
                return  formatDuration(progTimeMins);
            }
        }

        return null;
    }

    /**
     * get program time
     *
     * @return
     */
    public String getProgramTime() {
        if (isIPChannel()) {
            if (mNowIProgramInfo!=null) {
                String progTime = IPChannelDataReader.getInstance().parseDuration(
                                    mNowIProgramInfo.getPublishedStartTime(), mNowIProgramInfo.getPublishedDuration());
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " mNowIProgramInfo getProgramTime progTime, value == " + progTime);
                return  progTime;
            }
        }
        int timeType= EPGUtil.judgeFormatTime(mContext);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getProgramTime, timeType == " + timeType);
        if(mIntegration.is3rdTVSource()){
            String pattern=timeType==1?"MM-dd HH:mm":"MM-dd hh:mm a";
            SimpleDateFormat sdf=new SimpleDateFormat(pattern,Locale.ENGLISH);
            List<TIFProgramInfo> tIFProInfoList= tIFProgramManager.queryProgramListWithGroupFor3rd((int)TIFChannelManager.getInstance(mContext).getChannelInfoByUri().mId);
            if(tIFProInfoList !=null && !tIFProInfoList.isEmpty()){
                String programTime = sdf.format(new Date(tIFProInfoList.get(0).mStartTimeUtcSec*1000)) +" - "+sdf.format(new Date(tIFProInfoList.get(0).mEndTimeUtcSec*1000));
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in getProgramTime, mStartTimeUtcSec " +tIFProInfoList.get(0).mStartTimeUtcSec*1000 +" mEndTimeUtcSec " +tIFProInfoList.get(0).mEndTimeUtcSec*1000);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in getProgramTime, value == " + programTime);
                return  programTime;
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in getProgramTime, value == " );
                return "";
            }
        }else{
            String programTime = mMtkTvBanner.getProgTime();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "before come in getProgramTime, programTime == " + programTime);
            programTime=convertFormatTimeByType(programTime,timeType);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "after come in getProgramTime, value == " + programTime);
            return programTime;
        }
    }

    private String convertFormatTimeByType(String time,int timeType){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "convertFormatTimeByType, time ="+time+",timeType="+timeType);
        if(time==null){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "convertFormatTimeByType, time == null");
            return "";
        }
        boolean needFormat=false;
        if(time.contains("AM")||time.contains("PM")){
            //Need to convert time
            if(timeType==1){
                needFormat=true;
            }
        }
        else{
            //Need to convert time
            if(timeType==0){
                needFormat=true;
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "convertFormatTimeByType, needFormat ="+needFormat);
        String convertTime=time;
        if(needFormat){
            String[] timeArr=time.split("-");
            if(timeArr!=null&&timeArr.length==2){
                String tempStr=timeArr[1];
                boolean flag=false;
                if(tempStr.contains(STR_TIME_SPAN_TAG)){
                    tempStr=tempStr.substring(0, tempStr.indexOf(STR_TIME_SPAN_TAG));
                    flag=true;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "convertFormatTimeByType, tempStr ="+tempStr);
                }
                if(timeType==1){
                    convertTime=formatTime12To24(timeArr[0])+"-"+formatTime12To24(tempStr);
                }
                else{
                    convertTime=formatTime24To12(timeArr[0])+"-"+formatTime24To12(tempStr);
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "convertFormatTimeByType, flag ="+flag);
                convertTime=flag?convertTime+STR_TIME_SPAN_TAG:convertTime;
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "convertFormatTimeByType-----> convertTime="+convertTime );
        return convertTime;
    }






    private String formatTime24To12(String strTime){
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
        String formatTime="";
        try{
            Date time=sdf.parse(strTime);
            SimpleDateFormat formatSdf=new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
            formatTime=formatSdf.format(time);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "formatTime24_12-----> formatTime="+formatTime );
        return formatTime;
    }

    private String formatTime12To24(String strTime){
        SimpleDateFormat sdf=new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
        String formatTime="";
        try{
            Date time=sdf.parse(strTime);
            SimpleDateFormat formatSdf=new SimpleDateFormat("HH:mm");
            formatTime=formatSdf.format(time);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "formatTime12_24-----> formatTime="+formatTime );
        return formatTime;
    }

    /**
     * get current program title
     *
     * @return
     */
    public String getProgramTitle() {
        if (isIPChannel()) {
            if (mNowIProgramInfo!=null) {
                String programTitle = mNowIProgramInfo.getMainTitle();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " mNowIProgramInfo in getProgramTitle, value == " + programTitle);
                return  programTitle;
            }
        }
        if(mIntegration.is3rdTVSource()){
            TIFChannelInfo channelInfo=TIFChannelManager.getInstance(mContext).getChannelInfoByUri();
            if(channelInfo==null){
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, " channelInfo==null" );
                return "";
            }
            List<TIFProgramInfo> tIFProInfoList= tIFProgramManager.queryProgramListWithGroupFor3rd((int)channelInfo.mId);
            if( tIFProInfoList !=null && !tIFProInfoList.isEmpty()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " tIFProInfoList  " +tIFProInfoList.get(0).toString());
                String programTitle = tIFProInfoList.get(0).mTitle;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in getProgramTitle, value == " + programTitle);
                return  programTitle;
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in getProgramTitle, value == " );
                return "";
            }
        }else{
            String programTitle = mMtkTvBanner.getProgTitle();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getProgramTitle, value == " + programTitle);
            if(TextUtils.isEmpty(programTitle)){
              programTitle = mIntegration.isCurCHAnalog()?"":mContext.getString(R.string.banner_no_program_title);
            }
            return mIntegration.getAvailableString(programTitle);
        }
    }

    /**
     * get program rating
     *
     * @return
     */
    public String getProgramRating() {
        if(mIntegration.is3rdTVSource()){
              List<TIFProgramInfo> tIFProInfoList= tIFProgramManager.queryProgramListWithGroupFor3rd((int)TIFChannelManager.getInstance(mContext).getChannelInfoByUri().mId);
              if(tIFProInfoList !=null && !tIFProInfoList.isEmpty()){
                    String programRateing = tIFProInfoList.get(0).mRating;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in programRateing, value == " + programRateing);
                    return  programRateing;
                }else{
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in programRateing, value == " );
                    return "";
                }
        }else{
        	String programRating=null;
        	if(CommonIntegration.isEURegion()){
        	    if(isIPChannel()) {
                    if(mNowIProgramInfo != null && !TextUtils.isEmpty(mNowIProgramInfo.getParentalRating())) {
                        programRating = mNowIProgramInfo.getParentalRating();
                    }
                } else {
                    int channelId=CommonIntegration.getInstance().getCurrentChannelId();
                    MtkTvEventInfoBase mtkTvEventInfoBase=mMtkTvEventBase.getPFEventInfoByChannel(channelId,true);
                    if(mtkTvEventInfoBase==null){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "mtkTvEventInfoBase==null");
                        return "";
                    }
                    int ratingValue=mtkTvEventInfoBase.getEventRatingType();
                    String strRating= mtkTvEventInfoBase.getEventRating();
                    mEvents[0] = mtkTvEventInfoBase;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getProgramRating------->"+mtkTvEventInfoBase.toString());
                    programRating=mIntegration.mapRating2CustomerStr(ratingValue,strRating);
                    if(TurnkeyUiMainActivity.isUKCountry && TextUtils.equals(programRating, mContext.getString(R.string.rating_not_defined))) {
                        programRating = "";
                    }
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getProgramRating------->programRating="+programRating);
        	}
        	else{
        		programRating = mMtkTvBanner.getRating();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getProgramRating, value == " + programRating);
        	}
        	return programRating;

        }
    }

    public String getNextProgramRating() {
        if(mIntegration.is3rdTVSource()){
            List<TIFProgramInfo> tIFProInfoList= tIFProgramManager.queryProgramListWithGroupFor3rd((int)TIFChannelManager.getInstance(mContext).getChannelInfoByUri().mId);
            if( tIFProInfoList !=null && !tIFProInfoList.isEmpty()){
                  String programRateing = tIFProInfoList.get(0).mRating;
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in programRateing, value == " + programRateing);
                  return  programRateing;
              }else{
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " is3rdTVSourcecome in programRateing, value == " );
                  return "";
              }
        }else{
            String programRating=null;
            if(CommonIntegration.isEURegion()){
                if (isIPChannel()) {
                    if (mNextIProgramInfo != null && !TextUtils.isEmpty(mNextIProgramInfo.getParentalRating())) {
                        programRating = mNextIProgramInfo.getParentalRating();
                    }
                } else {
                    int channelId=CommonIntegration.getInstance().getCurrentChannelId();
                    MtkTvEventInfoBase mtkTvEventInfoBase=mMtkTvEventBase.getPFEventInfoByChannel(channelId,false);
                    if(mtkTvEventInfoBase==null){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "mtkTvEventInfoBase==null");
                        return "";
                    }
                    int ratingValue=mtkTvEventInfoBase.getEventRatingType();
                    String strRating= mtkTvEventInfoBase.getEventRating();
                    mEvents[1] = mtkTvEventInfoBase;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getNextProgrameRating------->"+mtkTvEventInfoBase.toString());
                    programRating=mIntegration.mapRating2CustomerStr(ratingValue,strRating);
                    if(TurnkeyUiMainActivity.isUKCountry && TextUtils.equals(programRating, mContext.getString(R.string.rating_not_defined))) {
                        programRating = "";
                    }
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getNextRating------->programRating="+programRating);
            }
            else{
                programRating = mMtkTvBanner.getNextRating();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getProgramRating, value == " + programRating);
            }
            return programRating;
        }
    }

    public String getCurrentTime() {
      MtkTvTimeFormatBase time = mMtkTvTime.getBroadcastLocalTime();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentTime,hour="+ time.hour +
          ",minute=" + time.minute);
      String timeStr = EPGUtil.formatTime(time.hour, time.minute, mContext);

      if (TVContent.getInstance(mContext).isMYSCountry()
              ||TVContent.getInstance(mContext).isTHACountry()) {
          String dateStr = EPGUtil.getYMDLocalTime();
          String weekStr = EPGUtil.getWeek(time.weekDay);

          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentTime,timeStr="+ timeStr +
                  ",dateStr=" + dateStr + ", weekStr=" + weekStr);

          StringBuilder sb = new StringBuilder();
          sb.append(timeStr);
          sb.append(" ");
          sb.append(dateStr);
          sb.append(weekStr);
          return sb.toString();
      }

      return timeStr;
    }

    /**
     * get current time
     *
     * @return
     */
    public String[] getCurrentDateTime() {
        MtkTvTimeFormatBase time = mMtkTvTime.getBroadcastLocalTime();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentTime,hour="+ time.hour +
            ",minute=" + time.minute);
        GregorianCalendar gregorianCalendar = new GregorianCalendar(
            time.year, time.month, time.monthDay, time.hour, time.minute, time.second);
//        String timeStr = EPGUtil.formatTime(time.hour, time.minute, mContext);
        String timeStr = DateFormat.getTimeFormat(mContext).format(gregorianCalendar.getTime());

        String dateStr = EPGUtil.getYMDLocalTime();
        String weekStr = new SimpleDateFormat("E", Locale.getDefault()).
            format(gregorianCalendar.getTime());

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getCurrentTime,timeStr="+ timeStr +
            ",dateStr=" + dateStr + ", weekStr=" + weekStr);

        return new String[]{
            timeStr,
            new StringBuilder().append(timeStr).append("  ").append(weekStr).toString(),
            dateStr
        };
    }

    /**
     * get TV turner mode
     *
     * @return
     */
    public String getTVTurnerMode() {
        if(mIntegration.is3rdTVSource()){
            return "";
        }else{
            String turnerMode = "";
            int iTurnerMode = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TUNER_MODE);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getTVTurnerMode, iTurnerMode == " + iTurnerMode);
            String[] tnuerArr = mContext.getResources()
                    .getStringArray(R.array.menu_tv_tuner_mode_array_full_eu_sat_only);
            if(iTurnerMode>=tnuerArr.length||iTurnerMode<0){
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Invalid turnerMode!");
                return "";
            }
            turnerMode=tnuerArr[iTurnerMode];
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getTVTurnerMode, turnerMode == " + turnerMode);
            //special for cn 's ATV
            if(MarketRegionInfo.REGION_CN == MarketRegionInfo.getCurrentMarketRegion()){
                if(CommonIntegration.getInstance().isCurrentSourceATV()){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTVTurnerMode---->Current source is atv.");
                    turnerMode = "Cable";
                }
            }
            return turnerMode;
        }
    }

    /**
     * get video info
     *
     * @return
     */
    public String getCurrentVideoInfo() {
        if(mIntegration.is3rdTVSource()){
            return getCurrentTifchannel().mVideoFormat;
        }else{
            String videoInfo = mMtkTvBanner.getVideoInfo();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getCurrentVideoInfo, value == " + videoInfo);
            return videoInfo==null?"":videoInfo;
        }
    }

    public String getCurrentHDRVideoInfo() {
        MtkTvAVModeBase mAudioMode = MtkTvAVMode.getInstance();
        int hdrValue = mAudioMode.getVideoInfoValue(MtkTvAVModeBase.VIDEOINFO_TYPE_HDR_EOTF_TYPE);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getCurrentHDRVideoInfo, hdrValue == " + hdrValue);
        if (0<=hdrValue && hdrValue<mHDRVideoInfo.length) {
            return mHDRVideoInfo[hdrValue];
        }

        return mHDRVideoInfo[0];
    }

    public String getHDRVideoInfoByIndex(int hdrValue) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getHDRVideoInfoByIndex, hdrValue == " + hdrValue);
        if (0<=hdrValue && hdrValue<mHDRVideoInfo.length) {
            return mHDRVideoInfo[hdrValue];
        }

        return mHDRVideoInfo[0];
    }

    /**
     * get audio eye icon display state, not clear what's meaning of audio eye
     *
     * @return
     */
    public boolean isShowADEIcon() {
        /*if(mIntegration.is3rdTVSource()){
            return false;
        }else{*/
            boolean showADEIcon = mMtkTvBanner.isDisplayADEyeIcon();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowADEIcon, value == " + showADEIcon);
            return showADEIcon;
    //  }
    }

    public boolean isShowADEIcon(boolean pf) {
        if(isIPChannel()) {
            ProgramInfo info = pf ? mNowIProgramInfo : mNextIProgramInfo;
            return info != null && info.isAD();
        } else {
            return pf ? mMtkTvBanner.isDisplayADEyeIcon() : mMtkTvBanner.isDisplayNextADEyeIcon();
        }
    };

    public TvTrackInfo getSelectedSubTitle(){
        if (TurnkeyUiMainActivity.getInstance() == null || TurnkeyUiMainActivity.getInstance().getTvView() == null) {
            Log.d(TAG, "TvView is null.");
            return null;
        }
        String currentTvSubTitleId = TurnkeyUiMainActivity.getInstance().getTvView().getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE);
        List<TvTrackInfo> subtitleListTv = TurnkeyUiMainActivity.getInstance().getTvView().getTracks(TvTrackInfo.TYPE_SUBTITLE);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSelectedSubTitle---->currentTvSubTitleId="+currentTvSubTitleId+",subtitleListTv="+subtitleListTv);
        if(subtitleListTv==null || currentTvSubTitleId==null){
            return null;
        }
//        int curSubtitleSize=subtitleListTv.size();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSelectedSubTitle---->currentTvSubTitleId="+currentTvSubTitleId);
//        boolean hasFind = false;
        for (TvTrackInfo trackInfo:subtitleListTv) {
            if (currentTvSubTitleId.equals(trackInfo.getId())) {
                return trackInfo;
            }
        }
        return null;
    }


    /**
     * get audio eye icon display state, not clear what's meaning of audio eye
     *
     * @return
     */
    public boolean isShowEARIcon() {
        /*if(mIntegration.is3rdTVSource()){
            return false;
        }else{*/
            boolean showEarIcon = mMtkTvBanner.isDisplayADEarIcon();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowEARIcon, value == " + showEarIcon);
            return showEarIcon||isShowHOH();
    //  }
    }

    public boolean isShowEARIcon(boolean pf) {
        if(isIPChannel()) {
            ProgramInfo info = pf ? mNowIProgramInfo : mNextIProgramInfo;
            return info != null && info.isSL();
        } else {
            return pf ? mMtkTvBanner.isDisplayADEarIcon() : mMtkTvBanner.isDisplayNextADEarIcon();
        }
    };

    /**
     *  get hard of hearing subtitle icon display state
     * @return
     */
    private boolean isShowHOH(){
        boolean isHearingImpaired=false;
        TvTrackInfo selectedTrackInfo=getSelectedSubTitle();
        if(selectedTrackInfo==null){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowHOH, selectedTrackInfo ==null ");
            return false;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowHOH, selectedTrackInfo = " + selectedTrackInfo);
        if(selectedTrackInfo!=null&&selectedTrackInfo.getExtra()!=null){
            String hearingImpaired = selectedTrackInfo.getExtra().getString("key_HearingImpaired");
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowHOH, hearingImpaired = " + hearingImpaired);
            isHearingImpaired=TextUtils.equals(hearingImpaired,"true");
        }
        return isHearingImpaired;
    }

    public boolean isShowCaptionIconForUK(boolean isPF) {
        return isPF ? mMtkTvBanner.isDisplaySubtHearImpairIcon() :
                mMtkTvBanner.isDisplayNextSubtHearImpairIcon();
    }

    /**
     * get caption icon display state
     *
     * @return
     */
    public boolean isShowCaptionIcon(boolean isPF) {
        if (isIPChannel()) {
            ProgramInfo info = isPF ? mNowIProgramInfo : mNextIProgramInfo;
            return info != null && info.isCaptionLanguageState();
        }
        boolean showCaptionIcon = mMtkTvBanner.isDisplayCaptionIcon();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowCaptionIcon, value == " + showCaptionIcon);
        return showCaptionIcon;
    }

    /**
     * get caption icon display state
     *
     * @return
     */
    public boolean isShowCaptionIcon() {
        return isShowCaptionIcon(false);
    }

    /**
     * get fav list icon display state
     *
     * @return
     */
    public boolean isShowFavoriteIcon() {
        /*if(mIntegration.is3rdTVSource()){
            return false;
        }else{*/
            boolean showFavoriteIcon = mMtkTvBanner.isDisplayFavIcon();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowFavoriteIcon, value == "
                    + showFavoriteIcon);
            return showFavoriteIcon;
        //}
    }

    /**
     * get channel frame display state
     *
     * @return
     */
    public boolean isShowFrameCH() {
    /*  if(mIntegration.is3rdTVSource()){
            return false;
        }else{*/
            boolean showFrameCh = mMtkTvBanner.isDisplayFrmCH();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowFrameCH, value == " + showFrameCh);
            return showFrameCh;
    //  }
    }

    /**
     * get detail frame display state
     *
     * @return
     */
    public boolean isShowFrameDetails() {
        /*if(mIntegration.is3rdTVSource()){
            return false;
        }else{*/
            boolean showFrameDetails = mMtkTvBanner.isDisplayFrmDetail();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowFrameDetails, value == "
                    + showFrameDetails);
            return showFrameDetails;
        //}
    }
    public boolean isDolbyVision(){
        int cur = MtkTvConfig.getInstance().getConfigValue(
                        MtkTvConfigType.CFG_GRP_DOVI_INFO);
        Log.i(TAG, "isDolbyVision,cur=="+cur);
        return cur != 0;
    }
    /**
     * get information frame display state
     *
     * @return
     */
    public boolean isShowFrameInfo() {
        /*if(mIntegration.is3rdTVSource()){
            return false;
        }else{*/
            boolean showFrameInfo = mMtkTvBanner.isDisplayFrmInfo();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowFrameInfo, value == " + showFrameInfo);
            return showFrameInfo;
        //}
    }

    /**
     * get ginga logo icon display state
     *
     * @return
     */
    public boolean isShowGingaIcon() {
    /*  if(mIntegration.is3rdTVSource()){
            return false;
        }else{*/
            boolean showGingaIcon = mMtkTvBanner.isDisplayGingaIcon();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowGingaIcon, value == " + showGingaIcon);
            return showGingaIcon;
    //  }
    }

    /**
     * get input source lock icon display state
     *
     * @return
     */
    public boolean isShowInputLockIcon() {
    /*  if(mIntegration.is3rdTVSource()){
            return false;
        }else{*/
            boolean showInputLockIcon = mMtkTvBanner.isDisplayIptsLockIcon();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowInputLockIcon, value == "
                    + showInputLockIcon);
            return showInputLockIcon;
    //  }
    }

    /**
     * get logo icon display state
     *
     * @return
     */
    public boolean isShowChannelLogoIcon() {
    /*  if(mIntegration.is3rdTVSource()){
            return false;
        }else{*/
            boolean showChannelLogoIcon = mMtkTvBanner.isDisplayLogoIcon();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowChannelLogoIcon, value == "
                    + showChannelLogoIcon);
            return showChannelLogoIcon;
    //  }
    }

    /**
     * get program detail info page down icon display state
     *
     * @return
     */
    public boolean isShowDetailsDownIcon() {
    /*  if(mIntegration.is3rdTVSource()){
            return false;
        }else{*/
            boolean showDetailsDownIcon = mMtkTvBanner
                    .isDisplayProgDetailDownIcon();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowDetailsDownIcon, value == "
                    + showDetailsDownIcon);
            return showDetailsDownIcon;
    //  }
    }

    /**
     * get program detail info page up icon display state
     *
     * @return
     */
    public boolean isShowDetailsUpIcon() {
    /*  if(mIntegration.is3rdTVSource()){
            return false;
        }else{*/
            boolean showDetailsUpIcon = mMtkTvBanner.isDisplayProgDetailUpIcon();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowDetailsUpIcon, value == "
                    + showDetailsUpIcon);
            return showDetailsUpIcon;
//      }
    }

    /**
     * get TTX icon display state
     *
     * @return
     */
    public boolean isShowTtxIcon() {
    /*  if(mIntegration.is3rdTVSource()){
            return false;
        }else{*/
            boolean showTtxIcon = mMtkTvBanner.isDisplayTtxIcon();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowTtxIcon, value == " + showTtxIcon);
            return showTtxIcon;
        //}
    }

    /**
     * get TV lock icon display state
     *
     * @return
     */
    public boolean isShowTVLockIcon() {
    /*  if(mIntegration.is3rdTVSource()){
            return false;
        }else{*/
            boolean showTVLockIcon = mMtkTvBanner.isDisplayTVLockIcon();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowTVLockIcon, value == " + showTVLockIcon);
            return showTVLockIcon;
    //  }
    }

    /**
     * check whether show banner bar
     *
     * @return
     */
    public boolean isShowBannerBar() {
    /*  if(mIntegration.is3rdTVSource()){
            return true;
        }else{*/
            boolean showTVLockIcon = mMtkTvBanner.isDisplayBanner();//true;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in isShowBannerBar, value == " + showTVLockIcon);
            return showTVLockIcon;
    //  }
    }

    public int changeNextCloseCaption() {
        int result = -1;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentSourceATV>>>" + mIntegration.isCurrentSourceATV() + "   " + mIntegration.isCurrentSourceDTV());

        switch (MarketRegionInfo.getCurrentMarketRegion()) {
        case MarketRegionInfo.REGION_SA:
            result = MtkTvISDBCloseCaption.getInstance().ISDBCCNextStream();
            break;
        case MarketRegionInfo.REGION_US:
            result = mMtkTvAnalogCloseCaption.analogCCNextStream();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "REGION_US call analogCCNextStream.");
            break;
        case MarketRegionInfo.REGION_CN:
        case MarketRegionInfo.REGION_EU:
            if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_COL) && CommonIntegration.getInstance().isCurrentSourceATV()) {
              result = mMtkTvAnalogCloseCaption.analogCCNextStream();
            } else if (mIntegration.isCurrentSourceTv()) {
                if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_SUBTITLE)){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeNextCloseCaption tif subtitle.");
                    changeToNextStreamByTif();
                } else{
                    result = MtkTvSubtitle.getInstance().nextStream();
                }
            }
            break;
        default:
            break;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeNextCloseCaption result:" + result);
        return result;

    }

    private void changeToNextStreamByTif() {
        String focusWin = CommonIntegration.getInstance().getCurrentFocus();
        if(focusWin.equals(InputSourceManager.MAIN)){
            if (TurnkeyUiMainActivity.getInstance() == null || TurnkeyUiMainActivity.getInstance().getTvView() == null) {
                Log.d(TAG, "TvView is null.");
                return;
            }
            String currentTvSubTitleId = TurnkeyUiMainActivity.getInstance().getTvView().getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE);
            List<TvTrackInfo> subtitleListTv = TurnkeyUiMainActivity.getInstance().getTvView().getTracks(TvTrackInfo.TYPE_SUBTITLE);
            if(subtitleListTv==null){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeToNextStreamByTif---->subtitleListTv is null.");
                return;
            }
            int curSubtitleSize=subtitleListTv.size();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeToNextStreamByTif---->curSubtitleSize="+curSubtitleSize);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeToNextStreamByTif---->currentTvSubTitleId="+currentTvSubTitleId);
            boolean hasFind = false;
            int i = 0;
            if (currentTvSubTitleId != null) {
                for (int j=0; j < curSubtitleSize; j++) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeToNextStreamByTif id="+subtitleListTv.get(j).getId()+",language="+subtitleListTv.get(j).getLanguage()+",currentTvSubTitleId="+currentTvSubTitleId);
                }
                for (; i < curSubtitleSize; i++) {
                    if (currentTvSubTitleId.equals(subtitleListTv.get(i).getId())) {
                        hasFind = true;
                        break;
                    }
                }
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeToNextStreamByTif---->hasFind="+hasFind+",index="+i);
            if (!hasFind && curSubtitleSize > 0) {
                TurnkeyUiMainActivity.getInstance().getTvView().selectTrack(TvTrackInfo.TYPE_SUBTITLE, subtitleListTv.get(0).getId());
            } else {
                if ((i + 1) < curSubtitleSize) {
                    TurnkeyUiMainActivity.getInstance().getTvView().selectTrack(TvTrackInfo.TYPE_SUBTITLE, subtitleListTv.get(i + 1).getId());
                }
                else {
                    MtkTvSubtitle.getInstance().nextStream();
//                  TurnkeyUiMainActivity.getInstance().getTvView().selectTrack(TvTrackInfo.TYPE_SUBTITLE, String.valueOf(0xFF));
                }
            }
        }
        else if(focusWin.equals(InputSourceManager.SUB)){
            String currentPipSubTitleId = TurnkeyUiMainActivity.getInstance().getPipView().getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE);
            List<TvTrackInfo> subtitleListPip = TurnkeyUiMainActivity.getInstance().getPipView().getTracks(TvTrackInfo.TYPE_SUBTITLE);
            if(subtitleListPip==null){
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "subtitleListPip==null");
                return;
            }
            boolean hasFind = false;
            int i = 0;
            if (currentPipSubTitleId != null) {
                for (; i < subtitleListPip.size(); i++) {
                    if (currentPipSubTitleId.equals(subtitleListPip.get(i).getId())) {
                        hasFind = true;
                        break;
                    }
                }
            }
            if (!hasFind && !subtitleListPip.isEmpty()) {
                TurnkeyUiMainActivity.getInstance().getPipView().selectTrack(TvTrackInfo.TYPE_SUBTITLE, subtitleListPip.get(0).getId());
            } else {
                if ((i + 1) < subtitleListPip.size()) {
                    TurnkeyUiMainActivity.getInstance().getPipView().selectTrack(TvTrackInfo.TYPE_SUBTITLE, subtitleListPip.get(i + 1).getId());
                } else {
                    MtkTvSubtitle.getInstance().nextStream();
                    TurnkeyUiMainActivity.getInstance().getPipView().selectTrack(TvTrackInfo.TYPE_SUBTITLE, String.valueOf(0xff));
                }
            }
        }
    }

    public int setCCVisiable(boolean visiable){
/*      if(mIntegration.is3rdTVSource()){
            return -1;
        }
*/      int result = -1;

        switch (MarketRegionInfo.getCurrentMarketRegion()) {
        case MarketRegionInfo.REGION_SA:
            if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TV_CAPTION)){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("Abstract", "Abstract set ISDB caption....");
                setCaptionEnabled(visiable);
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("Abstract", "set ISDBCCEnable....");
//              if (mIntegration.isCurrentSourceTv()) {
                    result = MtkTvISDBCloseCaption.getInstance().ISDBCCEnable(visiable);
//              }
            }
            break;
        case MarketRegionInfo.REGION_US:
            if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TV_CAPTION)){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("Abstract", "Abstract set us caption....");
                setCaptionEnabled(visiable);
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("Abstract", "Abstract set tvapi....");
                if (mIntegration.isCurrentSourceATV()) {
                    result = mMtkTvAnalogCloseCaption.analogCCSetCcVisible(visiable);
                } else if (mIntegration.isCurrentSourceDTV()) {
                    result = mMtkTvATSCCloseCaption.atscCCSetCcVisible(visiable);
                }
            }
            break;
        case MarketRegionInfo.REGION_CN:
        case MarketRegionInfo.REGION_EU:
            if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TV_CAPTION)){
                setCaptionEnabled(visiable);
            }
//            else{
//                if (mIntegration.isCurrentSourceTv()) {
//                    //result = MtkTvSubtitle.getInstance().nextStream();
//                }
//            }
            break;
        default:
            break;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setCCVisiable index ="+ result);


        return result;


    }

    /**
     * if current source is atv or not
     * @return
     */
    public boolean isCurrentSourceATV(){
    /*  if(mIntegration.is3rdTVSource()){
            return false;
        }else{*/
            return mIntegration.isCurrentSourceATV();
    //  }
    }
    /**
     * get current cc or subtitle value
     * @return
     */
    public String getCurrentCCOrSubtitleValue(){
    /*  if(mIntegration.is3rdTVSource()){
            return "";
        }*/
        switch (MarketRegionInfo.getCurrentMarketRegion()) {
        case MarketRegionInfo.REGION_CN:
            break;
        case MarketRegionInfo.REGION_SA:
            if (mIntegration.isCurrentSourceTv()) {
                return "Language"+MtkTvISDBCloseCaption.getInstance().ISDBCCGetCCString();
            }
            break;
        case MarketRegionInfo.REGION_US:
            if (mIntegration.isCurrentSourceATV()) {
                String res = "";
                int index = mMtkTvAnalogCloseCaption.analogCCNextStream();
                switch(index){
                    case 1:
                        res ="CC1";
                        break;
                    case 2:
                        res ="CC2";
                        break;
                    case 3:
                        res ="CC3";
                        break;
                    case 4:
                        res ="CC4";
                        break;
                    case 5:
                        res ="TEXT1";
                        break;
                    case 6:
                        res ="TEXT2";
                        break;
                    case 7:
                        res ="TEXT13";
                        break;
                    case 8:
                        res ="TEXT4";
                        break;
                    default:
                        break;


                }
                return res;
            } else if (mIntegration.isCurrentSourceDTV()) {
                return "Service"+mMtkTvATSCCloseCaption.atscCCNextStream();
            }
            break;
        case MarketRegionInfo.REGION_EU:
            if (mIntegration.isCurrentSourceTv()) {
                return "";// MtkTvSubtitle.getInstance().nextStream();
            }
            break;
        default:
            break;
        }

        return "";
    }

    /**
     * @author sin_biaoqinggao
     * @param enabled
     */
    private void setCaptionEnabled(boolean enabled){
    /*  if(mIntegration.is3rdTVSource()){
            return ;
        }*/
        String focusWin = CommonIntegration.getInstance().getCurrentFocus();
        if (TurnkeyUiMainActivity.getInstance() == null || TurnkeyUiMainActivity.getInstance().getTvView() == null) {
            Log.d(TAG, "TvView is null.");
            return;
        }
        if(focusWin.equals(InputSourceManager.MAIN)){
            TurnkeyUiMainActivity.getInstance().getTvView().setCaptionEnabled(enabled);
        }else if(focusWin.equals(InputSourceManager.SUB)){
            TurnkeyUiMainActivity.getInstance().getPipView().setCaptionEnabled(enabled);
        }
    }

    /**
     * @author jg_xiaominli
     * @param
     * @return TIFChannelInfo
     */
    public TIFChannelInfo getCurrentTifchannel(){
        if(mIntegration.is3rdTVSource()){
             TIFChannelInfo tifchannel=TIFChannelManager.getInstance(mContext).getChannelInfoByUri();
              if (tifchannel != null) {
                return tifchannel;
              }
        }
        return  new TIFChannelInfo();
    }

    /**
     * Add show the  information of LiveTV at TVLauncher Notification bar, which is playing in PIP
     * @author jg_xiaominli
     * @param
     * @return String
     * */
    public String getTVLauncherInfoForLiveTv(){
        String tvLaunchInfo="";
        if(mIntegration.is3rdTVSource()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTVLauncherInfoForLiveTv is3rdTVSource ");
            tvLaunchInfo = getCurrentInputName()+" - "+getCurrentTifchannel().mDisplayName;
        }else if(mIntegration.isCurrentSourceTv() || mIntegration.isCurrentSourceATV() || mIntegration.isCurrentSourceDTV()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTVLauncherInfoForLiveTv isCurrentSourceTv ");
            String curCHName=getCurrentChannelName();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTVLauncherInfoForLiveTv curCHName= "+curCHName);
            if(!TextUtils.isEmpty(curCHName)){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTVLauncherInfoForLiveTv isCurrentSourceTv getCurrentChannelName ");
                tvLaunchInfo = curCHName;
            }else{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTVLauncherInfoForLiveTv isCurrentSourceTv getCurrentChannelNum ");
                tvLaunchInfo = getCurrentChannelNum();
            }
            String programTitle=getProgramTitle();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTVLauncherInfoForLiveTv programTitle ="+programTitle);
            if(programTitle != null && !TextUtils.equals(programTitle, mContext.getString(R.string.banner_no_program_title))){
                tvLaunchInfo =tvLaunchInfo+" - "+getProgramTitle();
            }

        }else{
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTVLauncherInfoForLiveTv not isCurrentSourceTv and 3rd ");
            tvLaunchInfo =getCurrentInputName();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getTVLauncherInfoForLiveTv TVLauncherInfoForLiveTv= "+tvLaunchInfo);
        return  tvLaunchInfo;
    }

    public void reset() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"reset start.");
        mNowIProgramInfo = null;
        mNextIProgramInfo = null;
    }

    public boolean isEmptyForIPProgram() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isEmptyForMds mNowIProgramInfo="+mNowIProgramInfo);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isEmptyForMds mNextIProgramInfo="+mNextIProgramInfo);
        return mNowIProgramInfo == null || mNextIProgramInfo == null;
    }

    public @NonNull Single<Boolean> fetchProgramInfo() {
        MtkTvChannelInfoBase mMtkTvChannelInfo = getIPChannelInfo();
        if (mMtkTvChannelInfo == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"createGetProgramInfoRequestJson mMtkTvChannelInfo is null.");
            return Single.just(false);
        }
        int progmId = -1;
        if (mMtkTvChannelInfo instanceof MtkTvDvbChannelInfo) {
            progmId = ((MtkTvDvbChannelInfo)mMtkTvChannelInfo).getProgId();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"createGetProgramInfoRequestJson progmId="+progmId);
        }
        return MdsClient.getInstance().
                rxMakeRequest(new NowNextRequestData(progmId + "", NowNextRequestData.TYPE_NOW), TVMainObj.class)
                .map(tvMainObj -> {
                    List<ProgramInfo> programInfos = IPChannelDataReader.getInstance().toCreateProgram(tvMainObj.tVAMain);
                    for (ProgramInfo programInfo : programInfos) {
                        if (programInfo.getBasicDescriptionMemberOfTypeCrId().endsWith("now")) {
                            mNowIProgramInfo = programInfo;
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"mNowIProgramInfo:"+mNowIProgramInfo.toString());
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"saveIPProgramInfo programtitle="+programInfo.getMainTitle());
                        } else if (programInfo.getBasicDescriptionMemberOfTypeCrId().endsWith("later")){
                            mNextIProgramInfo = programInfo;
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"mNextIProgramInfo:"+mNextIProgramInfo.toString());
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"saveIPProgramInfo programtitle2="+programInfo.getMainTitle());
                        }
                    }
                    return true;
                });
    }

    public boolean isIPChannel() {
        MtkTvChannelInfoBase channelInfo = getIPChannelInfo();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isIPChannel ipchannel="+(channelInfo!=null?channelInfo.getServiceType():null));
        return channelInfo!=null&&channelInfo.getServiceType()==18;
    }

    public MtkTvChannelInfoBase getIPChannelInfo() {
        TIFChannelInfo mTIFChannelInfo = TIFChannelManager.getInstance(mContext).getCurrChannelInfo();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getIPChannelInfo mTIFChannelInfo="+(mTIFChannelInfo==null?null:"is not null."));
        return mTIFChannelInfo==null?null:mTIFChannelInfo.mMtkTvChannelInfo;
    }

    public void getDVBSTransponder(String[] mTransponder) {
        TIFChannelInfo currChannelInfo = TIFChannelManager.getInstance(mContext).getCurrChannelInfo();
        if (currChannelInfo != null && currChannelInfo.mMtkTvChannelInfo instanceof MtkTvDvbChannelInfo) {
            MtkTvDvbChannelInfo mtkTvDvbChannelInfo = (MtkTvDvbChannelInfo) currChannelInfo.mMtkTvChannelInfo;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getDVBSTransponder:"+mtkTvDvbChannelInfo.toString());
            mTransponder[0] = mContext.getString(R.string.banner_transponder_frequency,mtkTvDvbChannelInfo.getFrequency());
            mTransponder[1] = mContext.getString(R.string.banner_transponder_sysbol_rate,mtkTvDvbChannelInfo.getSymRate());
            switch (mtkTvDvbChannelInfo.getPol()) {
                case 1:
                    mTransponder[2] = mContext.getString(R.string.dvbs_tp_pol_1_HORIZONTAL);
                    break;
                case 2:
                    mTransponder[2] = mContext.getString(R.string.dvbs_tp_pol_2_VERTICAL);
                    break;
                case 3:
                    mTransponder[2] = mContext.getString(R.string.dvbs_tp_pol_3_LEFT);
                    break;
                case 4:
                    mTransponder[2] = mContext.getString(R.string.dvbs_tp_pol_4_RIGHT);
                    break;
                default:
                    break;
            }
        }
    }

    // /**
    // * set next close caption of atv
    // * @return
    // */
    // public int setNextAtvCloseCaption(){
    // return mMtkTvAnalogCloseCaption.nextStream();
    // }
    //
    // /**
    // * set next close caption of dtv
    // * @return
    // */
    // public int setNextDtvCloseCaption(){
    // return mMtkTvATSCCloseCaption.nextStream();
    // }
}
