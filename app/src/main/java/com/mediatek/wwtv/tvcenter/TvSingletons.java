package com.mediatek.wwtv.tvcenter;

//import android.content.Context;

import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFProgramManager;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.android.tv.parental.ParentalControlSettings;
import com.android.tv.parental.ContentRatingsManager;
import com.android.tv.util.TvInputManagerHelper;

import java.util.concurrent.Executor;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;

/** Interface for singletons obj. */
public interface TvSingletons {

    /** Returns the @{@link TvSingletons} using the application context. */
    static TvSingletons getSingletons() {
        return (TvSingletons)DestroyApp.getSingletons();
    }

    TvInputManagerHelper getTvInputManagerHelper();

    TIFChannelManager getChannelDataManager();

    TIFProgramManager getProgramDataManager();

    InputSourceManager getInputSourceManager();

    CommonIntegration getCommonIntegration();

    ParentalControlSettings getParentalControlSettings();

    ContentRatingsManager getContentRatingsManager();

    Executor getDbExecutor();

    boolean getTurnkeyUiMainActiviteActive();
    void setTurnkeyUiMainActiviteActive(Boolean isActive);
}
