package com.mediatek.wwtv.tvcenter.epg;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;
import android.widget.Toast;

import com.mediatek.wwtv.rxbus.MainActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.wwtv.tvcenter.epg.cn.EPGCnActivity;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGEu2ndActivity;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGEuActivity;
import com.mediatek.wwtv.tvcenter.epg.eu.EpgType;
import com.mediatek.wwtv.tvcenter.epg.sa.EPGSa2ndActivity;
import com.mediatek.wwtv.tvcenter.epg.sa.EPGSaActivity;
import com.mediatek.wwtv.tvcenter.epg.us.EPGUsActivity;
import com.mediatek.wwtv.tvcenter.nav.util.SundryImplement;
import com.mediatek.wwtv.tvcenter.util.CommonUtil;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;

public class EPGManager {
    //Broadcast type--EU/PAD/COL/CND: DB_ATV/DB_DTV
    //Medium type--US/SA/PAA: DB_AIR/DB_CAB/DB_SAT
    private static final String TAG = "EPGManager";
    public static final String BroadCast_Type_EU  = "EU";
    public static final String BroadCast_Type_PAD  = "PAD";
    public static final String BroadCast_Type_COL  = "COL";
    public static final String BroadCast_Type_CND  = "CND";
    private static EPGManager staticManager;
    private final Context mContext;

//    public MtkTvChannelList

    public static synchronized EPGManager getInstance(Activity activity) {
        if (staticManager == null) {
            staticManager = new EPGManager(activity);
        }
        return staticManager;
    }

    public EPGManager(Context context){
        mContext=context;
        if (!CommonIntegration.getInstance().isContextInit()) {
            CommonIntegration.getInstance().setContext(mContext.getApplicationContext());
        }

        RxBus.instance.onFirstEvent(MainActivityDestroyEvent.class)
            .doOnSuccess(it -> this.free())
            .subscribe();

    }

    private void free(){
        Log.d(TAG,"free");
        synchronized(EPGManager.class){
            staticManager = null;
        }
    }

    public boolean isEnterEPGEnable() {
        boolean startEpgSuccess = false;

        if(!MarketRegionInfo.isFunctionSupport(
                MarketRegionInfo.F_EPG_SUPPORT) ||
            DataSeparaterUtil.getInstance().isAtvOnly()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "not support epg or isAtvOnly~");
            return startEpgSuccess;
        }

        if(CommonIntegration.getInstance().is3rdTVSource()) {
            return true;
        }
        if(CommonIntegration.getInstance().isPipOrPopState()){
            return startEpgSuccess;
        }
        if (!CommonIntegration.getInstance().isCurrentSourceTv() ) {
            return startEpgSuccess;
        }

        String country = MtkTvConfig.getInstance().getCountry();
        boolean isSupportFVP = country.equalsIgnoreCase(EpgType.COUNTRY_UK) && CommonUtil.isSupportFVP(true);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isSupportFVP>> "+isSupportFVP);
        if(!isSupportFVP){
            List<TIFChannelInfo> tIFChannelInfoListAll  = CommonIntegration.getInstance().getChannelListForEPG();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tIFChannelInfoListAll>> "+tIFChannelInfoListAll);
            if(tIFChannelInfoListAll == null || tIFChannelInfoListAll.isEmpty()){
                return startEpgSuccess;
            }
        }

        if (CommonIntegration.getInstance().isMenuInputTvBlock()) {
          return startEpgSuccess;
        }
        
        switch (MarketRegionInfo.getCurrentMarketRegion()) {
        case MarketRegionInfo.REGION_CN:
             if (!CommonIntegration.getInstance().isCurrentSourceDTV() ) {
                 return startEpgSuccess;
             }

             if (!CommonIntegration.getInstance().hasDTVChannels()) {
                 return startEpgSuccess;
             }
            break;
        case MarketRegionInfo.REGION_US:
            TIFChannelInfo channelInfo = TIFChannelManager.getInstance(mContext).getCurrChannelInfo();
            if(channelInfo == null || channelInfo.mMtkTvChannelInfo == null
                    || channelInfo.mMtkTvChannelInfo.isAnalogService()
                    || CommonIntegration.getInstance().isCurrentSourceATV()){
                com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "US current is ATV. Do not support EPG!");
                return startEpgSuccess;
            }
            break;
        case MarketRegionInfo.REGION_EU:
            com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "REGION_EU");
          if(CommonIntegration.getInstance().isCurrentSourceATV()
                  && (CommonIntegration.getInstance().getCurChInfo() != null && CommonIntegration.getInstance().getCurChInfo().isAnalogService())){
            return startEpgSuccess;
          }
            if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_OCEANIA) && country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_NZL)) {
                return startEpgSuccess;
            }
            break;
        case MarketRegionInfo.REGION_SA:
            com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "REGION_SA");
          if(CommonIntegration.getInstance().isCurrentSourceATV()
                  && (CommonIntegration.getInstance().getCurChInfo() != null && CommonIntegration.getInstance().getCurChInfo().isAnalogService())){
            return startEpgSuccess;
          }
            break;
        default:
            break;
        }
        return true;
    }

    public boolean startEpg(Activity activity, int requestCode) {
        if (requestCode>=0 && (!isEnterEPGEnable())) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "EnterEPGEnable is false or Do not support EPG!");
            if(!(com.mediatek.wwtv.tvcenter.util.CommonUtil.isSupportFVP(true) &&
                 CommonIntegration.getInstance().isCurrentSourceTv())) {
                Toast.makeText(activity, activity.getString(R.string.no_support_epg), Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        TVAsyncExecutor.getInstance().execute(
                () -> {
                    if (SundryImplement
                            .getInstanceNavSundryImplement(activity).isFreeze()) {// freeze off
                        SundryImplement
                                .getInstanceNavSundryImplement(activity).setFreeze(false);
                    }
                });


        if(com.mediatek.wwtv.tvcenter.util.CommonUtil.isSupportFVP(true)&&!CommonIntegration.getInstance().is3rdTVSource()) {
            List<TIFChannelInfo> channelList = TIFChannelManager.getInstance(activity).getCurrentSVLChannelList();
            if ((channelList == null || channelList.isEmpty()) &&
                CommonIntegration.getInstance().getChannelActiveNumByAPIForScan() == 0) {
                Intent intent = new Intent(activity, com.android.tv.onboarding.SetupSourceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            }
            else {
                com.mediatek.wwtv.tvcenter.util.KeyDispatch.getInstance().passKeyToNative(
                    com.mediatek.wwtv.tvcenter.util.KeyMap.KEYCODE_MTKIR_GUIDE, null);
            }
            return true;
        }

        if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_US ||
            MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA) {
            if(!CommonIntegration.getInstance().is3rdTVSource()){
                MtkTvChannelInfoBase curChannel = CommonIntegration.getInstance().getCurChInfo();
                if(curChannel == null){
                  return false;
                }
                boolean isEpgVisible = curChannel.isEpgVisible();
                com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "isEpgVisible: " + isEpgVisible);
                if (!isEpgVisible ) {
                    Toast.makeText(activity, activity.getResources()
                            .getString(R.string.current_channel_invalid_msg), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        Point outSize = new Point();
        activity.getWindow().getWindowManager().getDefaultDisplay().getRealSize(outSize);
        ScreenConstant.SCREEN_WIDTH = outSize.x;
        ScreenConstant.SCREEN_HEIGHT = outSize.y;
        switch (MarketRegionInfo.getCurrentMarketRegion()) {
            case MarketRegionInfo.REGION_CN:
                startActivity(activity, EPGCnActivity.class, requestCode);
                break;
            case MarketRegionInfo.REGION_US:
                startActivity(activity, EPGUsActivity.class, requestCode);
                break;
            case MarketRegionInfo.REGION_EU:
                boolean support1DEPG=MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EPG_1D_SUPPORT);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "support1DEPG="+support1DEPG);
                if(DataSeparaterUtil.getInstance() != null && DataSeparaterUtil.getInstance().is1DEPGEnabled()){
                    startActivity(activity, EPGEu2ndActivity.class, requestCode);
                }
                else{
                    startActivity(activity, EPGEuActivity.class, requestCode);
                }
                break;
            case MarketRegionInfo.REGION_SA:
              boolean supportSa1DEPG=MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EPG_1D_SUPPORT);
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "supportSa1DEPG="+supportSa1DEPG);
              if(DataSeparaterUtil.getInstance() != null && DataSeparaterUtil.getInstance().is1DEPGEnabled()){
                startActivity(activity, EPGSa2ndActivity.class, requestCode);
              }else{
                startActivity(activity, EPGSaActivity.class, requestCode);
              }
                break;
            default:
                break;
        }
        return true;
    }

    public void openLockedSourceAndChannel(MtkTvChannelInfoBase currrntChannel) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "openLockedSourceAndChannel");
        if (currrntChannel != null && currrntChannel.isBlock()) {
            currrntChannel.setBlock(false);
            List<MtkTvChannelInfoBase> list = new ArrayList<MtkTvChannelInfoBase>();
            list.add(currrntChannel);
            CommonIntegration.getInstance().setChannelList(MtkTvChannelList.CHLST_OPERATOR_MOD, list);
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "after set currrntChannel>>>>>" + currrntChannel + "   " + currrntChannel.isBlock());
        }
    }

    public void startActivity(Activity startActivity,Class<?> finishActivity, int requestCode){
        Intent intent = new Intent(startActivity, finishActivity);
        intent.putExtra(finishActivity.getSimpleName(), startActivity.getClass().getSimpleName());

        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "startActivity," + finishActivity.getSimpleName()+","+startActivity.getClass().getSimpleName());

        startActivity.startActivityForResult(intent, requestCode);
      }
}
