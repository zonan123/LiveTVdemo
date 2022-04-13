package com.mediatek.wwtv.tvcenter.nav.input;

import android.content.Context;
import android.media.tv.TvInputInfo;

import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvInputSourceBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

public class CableInput extends AbstractInput{

    private static final String TAG = "CableInput";
    private final boolean mHidden = !MarketRegionInfo
            .isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT);

    public CableInput() {
        super(null, TYPE_CAB);
    }

    @Override
    public String getId() {
        return TAG;
    }

    @Override
    protected int getHardwareId(MtkTvInputSourceBase.InputSourceRecord record) {
        return (record.getId() << 16) | MtkTvConfigTypeBase.ACFG_BS_SRC_CABLE;
    }

    @Override
    public String getSourceName(Context context) {
        return context.getString(R.string.menu_arrays_Cable);
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
        String country = MtkTvConfig.getInstance().getCountry();
        return mHidden ||
                MtkTvConfigTypeBase.S3166_CFG_COUNT_TWN.equals(country) ||
                MtkTvConfigTypeBase.S3166_CFG_COUNT_HKG.equals(country) ||
                InputUtil.getTvInputManager().getTvInputInfo(DtvInput.DEFAULT_ID) == null ||
                DataSeparaterUtil.getInstance().isTunerModeIniExist() && !DataSeparaterUtil.getInstance().isDVBCSupport();
    }
}
