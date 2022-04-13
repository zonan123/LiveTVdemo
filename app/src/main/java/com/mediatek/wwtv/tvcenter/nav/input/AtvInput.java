package com.mediatek.wwtv.tvcenter.nav.input;

import android.content.Context;
import android.media.tv.TvInputInfo;

import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.R;

public final class AtvInput extends AbstractInput {
    private static final String TAG = "AtvInput";
    public static final String DEFAULT_ID = "com.mediatek.tvinput/.tuner.TunerInputService/HW1";
    private final boolean mHidden = !MarketRegionInfo
            .isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT);

    public AtvInput() {
        super(null, ISource.TYPE_ATV);
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
            return context.getResources().getString(R.string.nav_source_atv);
        }

        return "ATV";
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
        return "ATV Id:" + getHardwareId() +
            ",TvInputInfo=" + getTvInputInfo()+
            ", State=" + getState() + ", isHidden=" + isHidden(context) +
            ", CustomSourceName=" + getCustomSourceName(context) +
            ", SourceName=" + getSourceName(context) +
            ", isBlock=" + isBlock();
    }
}
