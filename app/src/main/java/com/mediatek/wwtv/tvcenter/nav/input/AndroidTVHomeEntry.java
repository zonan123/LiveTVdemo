package com.mediatek.wwtv.tvcenter.nav.input;

import android.content.Context;
import android.media.tv.TvInputInfo;

import com.mediatek.wwtv.tvcenter.R;

public class AndroidTVHomeEntry extends AbstractInput {

    public AndroidTVHomeEntry() {
        super(null, ISource.ANDROID_TV_HOME);
    }

    @Override
    protected void init(TvInputInfo tvInputInfo, int type) {
        mTvInputInfo = null;
        mType = type;
        mHardwareId = type;
    }

    @Override
    public boolean isHidden(Context context) {
        return false;
    }

    @Override
    public String toString(Context context) {
        return "AndroidTVHomeEntry";
    }

    @Override
    public String getSourceName(Context context) {
        return context.getResources().getString(R.string.inputspanel_android_tv_home);
    }

    @Override
    public String getCustomSourceName(Context context) {
        return context.getResources().getString(R.string.inputspanel_android_tv_home);
    }
}
