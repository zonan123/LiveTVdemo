package com.mediatek.wwtv.tvcenter.nav.input;

import android.content.Context;
import android.media.tv.TvInputInfo;
import android.media.tv.TvView;
import android.net.Uri;

import com.mediatek.twoworlds.tv.MtkTvInputSourceBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

public class SatelliteInput extends AbstractInput{

    private static final String TAG = "CableInput";
    private final boolean mHidden = !MarketRegionInfo
            .isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT);

    public SatelliteInput() {
        super(null, TYPE_SAT);
    }

    @Override
    public String getId() {
        return TAG;
    }

    @Override
    protected int getHardwareId(MtkTvInputSourceBase.InputSourceRecord record) {
        return (record.getId() << 16) | MtkTvConfigTypeBase.APP_CFG_BS_SRC_SAT;
    }

    @Override
    public String getSourceName(Context context) {
        return context.getString(R.string.menu_arrays_Satellite);
    }

    @Override
    public String getCustomSourceName(Context context) {
        return getSourceName(context);
    }

    @Override
    public TvInputInfo getTvInputInfo() {
        return null;
    }

    @Override
    public boolean isHidden(Context context) {
        return mHidden || InputUtil.getTvInputManager().getTvInputInfo(DtvInput.DEFAULT_ID) == null || !isSupportDVBS();
    }

    private boolean isSupportDVBS() {
        boolean result;
        if(!DataSeparaterUtil.getInstance().isTunerModeIniExist()){
            if(CommonIntegration.isEUPARegion()) {
                result = MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_PA_DVBS_SUPPORT);
            } else {
                result = MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DVBS) &&
                        !MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_COL);
            }
        } else {
            if(CommonIntegration.isEUPARegion()) {
                result = MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_PA_DVBS_SUPPORT) &&
                        DataSeparaterUtil.getInstance().isDVBSSupport();
            } else {
                result = MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_DVBS) &&
                        DataSeparaterUtil.getInstance().isDVBSSupport() &&
                        !MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_EU_COL);
            }
        }
        return result;
    }
}
