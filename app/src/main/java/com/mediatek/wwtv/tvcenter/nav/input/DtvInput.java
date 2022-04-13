package com.mediatek.wwtv.tvcenter.nav.input;

import android.content.Context;
import android.media.tv.TvInputInfo;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.CommonUtil;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

public class DtvInput extends AbstractInput {
    private static final String TAG = "DtvInput";
    public static final String DEFAULT_ID = "com.mediatek.tvinput/.tuner.TunerInputService/HW0";
    private final boolean mHidden = !MarketRegionInfo
            .isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT);

    public static final int TUNER_TYPE_ANTENNA = 0;
    public static final int TUNER_TYPE_CABLE = 1;
    public static final int TUNER_TYPE_SATELLITE = 2;

    public DtvInput() {
        super(null, ISource.TYPE_DTV);
    }

    @Override
    public String getId() {
        return TAG;
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
                return context.getString(R.string.nav_source_dtv);
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
        return mHidden || InputUtil.getTvInputManager().getTvInputInfo(DEFAULT_ID) == null;
    }

    @Override
    public String toString(Context context) {
        return "DTV Id:" + getHardwareId() +
            ",TvInputInfo=" + getTvInputInfo()+
            ", State=" + getState() + ", isHidden=" + isHidden(context) +
            ", CustomSourceName=" + getCustomSourceName(context) +
            ", SourceName=" + getSourceName(context) +
            ", isBlock=" + isBlock();
    }
}
