package com.mediatek.wwtv.tvcenter.nav.input;

import android.content.Context;
import android.media.tv.TvInputInfo;

import com.mediatek.twoworlds.tv.MtkTvInputSourceBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.CommonUtil;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

public class AntennaInput extends AbstractInput{

    private static final String TAG = "AntennaInput";
    private final boolean mHidden = !MarketRegionInfo
            .isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT);

    public AntennaInput() {
        super(null, TYPE_AIR);
    }

    @Override
    public String getId() {
        return TAG;
    }

    @Override
    protected int getHardwareId(MtkTvInputSourceBase.InputSourceRecord record) {
        return (record.getId() << 16) | MtkTvConfigTypeBase.ACFG_BS_SRC_AIR;
    }

    @Override
    public String getCustomSourceName(Context context) {
        return getSourceName(context);
    }

    @Override
    public String getSourceName(Context context) {
        if(context != null) {
            if (CommonUtil.isSupportFVP(true)) {
                return context.getString(R.string.input_dtv_customer_name_freeview);
            } else {
                return context.getString(R.string.menu_arrays_Antenna);
            }
        }

        return "DTV";
    }

    @Override
    public TvInputInfo getTvInputInfo() {
        return null;
    }

    @Override
    public boolean isHidden(Context context) {
        return mHidden ||
                InputUtil.getTvInputManager().getTvInputInfo(DtvInput.DEFAULT_ID) == null ||
                DataSeparaterUtil.getInstance().isTunerModeIniExist() && !DataSeparaterUtil.getInstance().isDVBTSupport();
    }
}
