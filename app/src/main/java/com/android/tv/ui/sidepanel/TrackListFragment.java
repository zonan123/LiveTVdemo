package com.android.tv.ui.sidepanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.util.SundryImplement;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.tif.TvInputCallbackMgr;

import android.media.tv.TvTrackInfo;
import android.os.Bundle;
import android.view.View;
import android.text.TextUtils;
import com.mediatek.twoworlds.tv.MtkTvAVMode;
import com.mediatek.twoworlds.tv.MtkTvEventBase;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.twoworlds.tv.model.MtkTvEventComponentDescriptorBase;
import com.mediatek.twoworlds.tv.model.MtkTvEventInfoBase;
import android.util.Log;
import com.mediatek.twoworlds.tv.MtkTvAVModeBase;
import com.mediatek.twoworlds.tv.model.TvProviderAudioTrackBase;

public class TrackListFragment extends
        SideFragment<TrackListFragment.TrackItem> {

    private static final String TAG = "TrackListFragment";

    private SundryImplement sundryImplement;

    private MtkTvAVMode mtkTvAVMode;

    private MtkTvEventBase mMtkTvEventBase;

    @Override
    protected String getTitle() {
        return getString(R.string.menu_audio_sound_tracks);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mtkTvAVMode = MtkTvAVMode.getInstance();
        sundryImplement = SundryImplement.getInstanceNavSundryImplement(getContext());
        mMtkTvEventBase = new MtkTvEventBase();
        TvInputCallbackMgr.getInstance(getActivity().getApplicationContext()).setSoundTracksCallback(creatSoundTracksCallback());
    }

    @Override
    protected List<TrackListFragment.TrackItem> getItemList() {
        List<TrackListFragment.TrackItem> result = new ArrayList<TrackListFragment.TrackItem>();
        if(CommonIntegration.getInstance().isCurrentSourceDTV()) {
            List<TvTrackInfo> trackList = sundryImplement.getTrackList();
            TvTrackInfo currentTvTrackInfo = sundryImplement.getTvTrackInfoByTrackId(
                    sundryImplement.getCurrentTvTrackId(), trackList);
            String driverAudioType = currentTvTrackInfo != null ? currentTvTrackInfo.getExtra().getString("key_AudioDriverAutoSelect"):"0";
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "track list currentTvTrackInfo :"+currentTvTrackInfo);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "track list driverAudioType :"+driverAudioType);

            int opreatorValue = ScanContent.getDVBSCurrentOP();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "SoundTrack opreator: " + opreatorValue);
            MtkTvEventComponentDescriptorBase[] mComponentDescriptor = null;
            if (MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_SKY_DEUTSCHLAND == opreatorValue) {
                int channelId = CommonIntegration.getInstance().getCurrentChannelId();
                MtkTvEventInfoBase mMtkTvEventInfoBase = mMtkTvEventBase.getPFEventInfoByChannel(
                        channelId, true);
                if(mMtkTvEventInfoBase != null){
                    mComponentDescriptor = mMtkTvEventInfoBase
                            .getEventComponent(); 
                }
               if(mComponentDescriptor != null){
                   for (MtkTvEventComponentDescriptorBase desc : mComponentDescriptor) {
                       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mComponentDescriptor ComponentLang text =  "
                               + desc.getComponentLang());
                       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mComponentDescriptor getComponentTag text =  "
                               + desc.getComponentTag());
                       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mComponentDescriptor getStreamContent text =  "
                               + desc.getStreamContent());
                       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mComponentDescriptor getStreamContentExt text =  "
                               + desc.getStreamContentExt());
                       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mComponentDescriptor getComponentType text =  "
                               + desc.getComponentType());
                       com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mComponentDescriptor ComponentText text =  "
                               + desc.getComponentText());
                   } 
               }
                
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tracks current Bundle:"+(currentTvTrackInfo != null ? currentTvTrackInfo.getExtra().toString():"null"));
            String dValue = "";

            for (TvTrackInfo tvTrackInfo : trackList) {
                if (trackList == null) {
                    continue;
                }
                if (tvTrackInfo.getExtra() != null) {
                    if (tvTrackInfo.getExtra().getBoolean(sundryImplement.A_HI_AUDIO)){
                        dValue = mContext.getResources().getString(R.string.audio_hi);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "track list A_HI_AUDIO :"+tvTrackInfo.getExtra().getBoolean(sundryImplement.A_HI_AUDIO));
                    }else if(tvTrackInfo.getExtra().getBoolean(sundryImplement.A_RECEIVE_MIX_VI)){
                        dValue = mContext.getResources().getString(R.string.audio_vi);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "track list A_RECEIVE_MIX_VI :"+tvTrackInfo.getExtra().getBoolean(sundryImplement.A_RECEIVE_MIX_VI));
                    }else if(tvTrackInfo.getExtra().getBoolean(sundryImplement.A_BROADCAST_MIXED_VI_AUDIO)){
                        dValue = mContext.getResources().getString(R.string.audio_vi);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "track list A_BROADCAST_MIXED_VI_AUDIO :"+tvTrackInfo.getExtra().getBoolean(sundryImplement.A_BROADCAST_MIXED_VI_AUDIO));

                    }else if(tvTrackInfo.getExtra().getBoolean(sundryImplement.A_RECEIVE_MIXED_VI_MAIN)){
                        dValue = mContext.getResources().getString(R.string.audio_vi);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "track list A_RECEIVE_MIXED_VI_MAIN :"+tvTrackInfo.getExtra().getBoolean(sundryImplement.A_RECEIVE_MIXED_VI_MAIN));
                    }else {
                        dValue = "";
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "track list dValue :");
                    }

                TrackItem trackItem = null;
                if (MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_SKY_DEUTSCHLAND == opreatorValue) {
                    if (null != mComponentDescriptor) {
                        String compTag = tvTrackInfo
                                .getExtra()
                                .getString("key_AudioCompTag");
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tvTrackInfo compTag =  "
                                    + compTag);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mComponentDescriptor length =  "
                                    + mComponentDescriptor.length);
                        for (MtkTvEventComponentDescriptorBase desc : mComponentDescriptor) {
                            if (compTag.equalsIgnoreCase(
                                    Short.toString(desc.getComponentTag()))) {
                                trackItem = new TrackItem(
                                        desc.getComponentLang() + "   "
                                                + desc.getComponentText(),
                                        tvTrackInfo);
                                break;
                            }
                        }
                    }
                } else {

                    String AudioTracksByTrackId = "";
                    if(currentTvTrackInfo != null && tvTrackInfo.getId() != null){
                        AudioTracksByTrackId = getAudioTracksByTrackId(tvTrackInfo.getId(),trackList);
                    }

                    trackItem = new TrackItem(
                            tvTrackInfo.getExtra().getString(
                                    sundryImplement.A_DISPLAY_AUDIO_LANGUAGE)+AudioTracksByTrackId+"  "+dValue,
                            tvTrackInfo);
                }
                result.add(trackItem);
                    //fix DTV02014558
                    if ("0".equals(driverAudioType)) {
                        if(trackItem != null && currentTvTrackInfo != null && currentTvTrackInfo.getId() != null && tvTrackInfo.getId() != null) {
                            trackItem
                                    .setChecked(tvTrackInfo.getId().equals(currentTvTrackInfo.getId()));
                        }
                    }
                }
            }
            if(!trackList.isEmpty()){
                result.add(new TrackItem(mContext.getResources().getString(R.string.default_audio),null));
            }
        }
        return result;
    }

    public class TrackItem extends RadioButtonItem {

        TvTrackInfo trackInfo = null;
        View iconAd;
        View iconSps;
        View iconAdSps;
        View iconDoblyAtmos;
        View iconDoblyAudio;
        String title;

        public TrackItem(String title, TvTrackInfo trackInfo) {
            super(title);
            this.trackInfo = trackInfo;
            this.title = title;
        }

        @Override
        protected void onBind(View view) {
            super.onBind(view);
            iconAd = view.findViewById(R.id.icon_ad);
            iconSps = view.findViewById(R.id.icon_sps);
            iconAdSps = view.findViewById(R.id.icon_ad_sps);
            iconDoblyAtmos = view.findViewById(R.id.icon_dobly_atoms);
            iconDoblyAudio = view.findViewById(R.id.icon_dobly_audio);

            if(trackInfo != null && trackInfo.getExtra() != null) {
                Bundle extra = trackInfo.getExtra();
                List<String> dolby = Arrays.asList("1", "12", "26");
                if (dolby.contains(extra.getString("key_AudioEncodeType"))) {
                    iconDoblyAudio.setVisibility(View.VISIBLE);
                } else {
                    iconDoblyAudio.setVisibility(View.GONE);
                }
                if (extra.getBoolean(sundryImplement.A_BROADCAST_MIXED_VI_AUDIO) ||
                        extra.getBoolean(sundryImplement.A_RECEIVE_MIX_VI) ||
                        extra.getBoolean(sundryImplement.A_RECEIVE_MIXED_VI_MAIN)) {
                    iconAd.setVisibility(View.VISIBLE);
                    iconSps.setVisibility(View.GONE);
                    iconAdSps.setVisibility(View.GONE);
                } else if(extra.getBoolean(sundryImplement.A_SPS_MIX_AUDIO) ||
                      extra.getBoolean(sundryImplement.A_SPS_MIXED_MAIN)){
                    iconAd.setVisibility(View.GONE);
                    iconSps.setVisibility(View.VISIBLE);
                    iconAdSps.setVisibility(View.GONE);
                } else if(extra.getBoolean(sundryImplement.A_AD_SPS_MIX_AUDIO) ||
                      extra.getBoolean(sundryImplement.A_AD_SPS_MIXED_MAIN_AUDIO)) {
                    iconAd.setVisibility(View.GONE);
                    iconSps.setVisibility(View.GONE);
                    iconAdSps.setVisibility(View.VISIBLE);
                } else {
                    iconAd.setVisibility(View.GONE);
                    iconSps.setVisibility(View.GONE);
                    iconAdSps.setVisibility(View.GONE);
                }
                if (!extra.getString("key_AudioAtmos").equals("0")) {
                    iconDoblyAtmos.setVisibility(View.VISIBLE);
                    iconDoblyAudio.setVisibility(View.GONE);
                } else {
                    iconDoblyAtmos.setVisibility(View.GONE);
                }
            }
        }

        @Override
        protected int getResourceId() {
            return R.layout.track_option_item_radio_button;
        }

        @Override
        protected void onSelected() {
            if(isChecked()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onSelected repeat");
                return;
            }
            super.onSelected();
            if(!TextUtils.isEmpty(title) && title.equals(mContext.getResources().getString(R.string.default_audio))){
                mtkTvAVMode.selectDefaultAudio("main");
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onSelected selectDefaultAudio");
            } else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onSelected    :lang:"+trackInfo.getLanguage()+
                        " id:"+trackInfo.getId()+" Bundle:"+(trackInfo.getExtra() == null ? "null":trackInfo.getExtra().toString()));
                if(trackInfo.getExtra().getBoolean(sundryImplement.A_RECEIVE_MIXED_VI_MAIN)) {
                    boolean ret = mtkTvAVMode.selectMainSubAudioById(
                            Integer.parseInt(trackInfo.getExtra().getString(sundryImplement.A_RECEIVE_MIXED_VI_MAIN_ID)),
                            Integer.parseInt(trackInfo.getId()));
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectMainSubAudioById ad result:"+ret);
                } else if(trackInfo.getExtra().getBoolean(sundryImplement.A_SPS_MIXED_MAIN)) {
                    boolean ret = mtkTvAVMode.selectMainSubAudioById(
                            Integer.parseInt(trackInfo.getExtra().getString(sundryImplement.A_SPS_MIXED_MAIN_ID)),
                            Integer.parseInt(trackInfo.getId()));
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectMainSubAudioById sps result:"+ret);
                } else if(trackInfo.getExtra().getBoolean(sundryImplement.A_AD_SPS_MIXED_MAIN_AUDIO)) {
                    boolean ret = mtkTvAVMode.selectMainSubAudioById(
                            Integer.parseInt(trackInfo.getExtra().getString(sundryImplement.A_AD_SPS_MIXED_MAIN_ID_AUDIO)),
                            Integer.parseInt(trackInfo.getId()));
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectMainSubAudioById ad_sps result:"+ret);
                } else {
                    boolean ret = mtkTvAVMode.selectMainSubAudioById(Integer.parseInt(trackInfo.getId()), 0);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectMainSubAudioById for stop result:"+ret);
                }
            }
        }
    }

    protected int getFragmentLayoutResourceId() {
        return R.layout.track_list_fragment;
    }

    private String getAudioTracksByTrackId(String trackId,List<TvTrackInfo> tracks){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getAudioTracksByTrackId,trackId:" +
                trackId + "tracks size:" + tracks.size());

        int trackIdx = -1;
        Log.d(TAG, "trackId= "+ trackId);
        for (int i=0;i<tracks.size();i++){
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
//            int dtsVersion = MtkTvAVMode.getInstance().getAudioInfoValue(MtkTvAVModeBase.AUDIO_INFO_GET_TYPE_DTS_VERSION);
            if (iDecType == 4) {// AUD_DECODE_TYPE_AAC
                decAudioStr = mContext.getResources().getString(R.string.audio_type_aac);
            } else if (iDecType == 5) {// AUD_DECODE_TYPE_HEAAC
               decAudioStr = mContext.getResources().getString(R.string.audio_type_he_aac);
            } else if (iDecType == 6) {// AUD_DECODE_TYPE_HEAAC_V2
               decAudioStr = mContext.getResources().getString(R.string.audio_type_he_aacv2);
            }else if (iDecType == 10) {// AUD_DECODE_TYPE_MPEG1_L2
                decAudioStr =  mContext.getResources().getString(R.string.audio_type_mpeg1l2);
            }
//            else if (iDecType == 15||iDecType == 16||iDecType == 17||iDecType == 21) {// AUD_DECODE_TYPE_DTS
//                decAudioStr = getDtsAudioInfo();
//            }
            else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "audio----No need to display this audio dec type");
            }
            return decAudioStr = decAudioStr != ""?"("+decAudioStr+")":decAudioStr;
//            int doblyType=getDoblyType();
//            //Dolby suggestion for AAC & HEAAC only show decAudioStr.
//            if((doblyType==DOBLY_TYPE_AUDIO||doblyType==DOBLY_TYPE_ATOMS) && (iDecType==4||iDecType==5)){
//                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "No need append audioChannel!");
//                return decAudioStr;
//            } else{
//                return (decAudioStr+" "+audioChannel[audioChannelIdx]);
//            }
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "audio----getAudioChannelByTrackId audioChannelIdx:null");
            return "";
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
    
    private TvInputCallbackMgr.SoundTracksCallback creatSoundTracksCallback(){
        return new TvInputCallbackMgr.SoundTracksCallback() {
            
            @Override
            public void onTracksChanged(String inputId, List<TvTrackInfo> tracks) {
                mItems.clear();
                mItems.addAll(getItemList());
                refreshUI();
            }
        };
    }

}
