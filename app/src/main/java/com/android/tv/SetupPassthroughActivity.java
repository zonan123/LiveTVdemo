/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tv;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.tv.TvInputInfo;
import android.os.Bundle;
import android.util.Log;

//import com.android.tv.common.SoftPreconditions;
import com.android.tv.common.TvCommonConstantsUtils;
//import com.android.tv.data.epg.EpgFetcher;
//import com.android.tv.experiments.Experiments;
import com.android.tv.util.SetupUtils;
import com.android.tv.util.TvInputManagerHelper;
//import com.android.tv.menu.Utils;

import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.commonview.BaseActivity;

/**
 * An activity to launch a TV input setup activity.
 *
 * <p> After setup activity is finished, all channels will be browsable.
 */
public class SetupPassthroughActivity extends BaseActivity {
    private static final String TAG = "SetupPassthroughAct";
    private static final int REQUEST_START_SETUP_ACTIVITY = 200;
    private static final String DTV_ID = "com.mediatek.tvinput/.tuner.TunerInputService/HW0";
    //private static final String ATV_ID = "com.mediatek.tvinput/.tuner.TunerInputService/HW1";

    private TvInputInfo mTvInputInfo;
    private Intent mActivityAfterCompletion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
//        SoftPreconditions.checkState(
//                intent.getAction().equals(TvCommonConstants.INTENT_ACTION_INPUT_SETUP));
//        ApplicationSingletons appSingletons = TvApplication.getSingletons(this);
        TvInputManagerHelper inputManager = ((DestroyApp)getApplicationContext()).getTvInputManagerHelper();
        String inputId = intent.getStringExtra(TvCommonConstantsUtils.EXTRA_INPUT_ID);
        mTvInputInfo = inputManager.getTvInputInfo(inputId);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TvInputId " + inputId + " / TvInputInfo " + mTvInputInfo);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "intent:" + intent);
        if (mTvInputInfo == null) {
            Log.w(TAG, "There is no input with the ID " + inputId + ".");
            finish();
            return;
        }
        Intent setupIntent = intent.getExtras().getParcelable(TvCommonConstantsUtils.EXTRA_SETUP_INTENT);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Setup activity launch intent: " + setupIntent);
        if (setupIntent == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "The input (" + mTvInputInfo.getId() + ") doesn't have setup.");
            finish();
            return;
        }
        SetupUtils.grantEpgPermission(this, mTvInputInfo.getServiceInfo().packageName);
        mActivityAfterCompletion = intent.getParcelableExtra(
                TvCommonConstantsUtils.EXTRA_ACTIVITY_AFTER_COMPLETION);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Activity after completion " + mActivityAfterCompletion);
        // If EXTRA_SETUP_INTENT is not removed, an infinite recursion happens during
        // setupIntent.putExtras(intent.getExtras()).
        Bundle extras = intent.getExtras();
        extras.remove(TvCommonConstantsUtils.EXTRA_SETUP_INTENT);
        setupIntent.putExtras(extras);
        try {
            if(CommonIntegration.isTVSourceSeparation()/*MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SEP_TV_SRC_SUPPORT)*/) {
                int type = -1;
                if(DTV_ID.equals(inputId)) {
                    type = 0;
                } else if(inputId.startsWith("com.mediatek.tvinput/.tuner.TunerInputService")) {
                    type = 1;
                }
                setupIntent.putExtra("setup_source_scan_type", type);
            }
            startActivityForResult(setupIntent, REQUEST_START_SETUP_ACTIVITY);
        } catch (ActivityNotFoundException e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Can't find activity: " + setupIntent.getComponent());
            finish();
            return;
        }
//        if (Utils.isInternalTvInput(this, mTvInputInfo.getId()) && Experiments.CLOUD_EPG.get()) {
//            EpgFetcher.getInstance(this).stop();
//        }
    }

    @Override
    protected void onDestroy() {
//        if (mTvInputInfo != null && Utils.isInternalTvInput(this, mTvInputInfo.getId())
//                && Experiments.CLOUD_EPG.get()) {
//            EpgFetcher.getInstance(this).start();
//        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "onActivityResult, requestCode=" + requestCode + ",resultCode=" + resultCode);
        boolean setupComplete = requestCode == REQUEST_START_SETUP_ACTIVITY
                && resultCode == Activity.RESULT_OK;
        if (!setupComplete) {
            setResult(resultCode, data);
            finish();
            return;
        }
        DestroyApp.setActivityActiveStatus(true);
        SetupUtils.createForTvSingletons(this).onTvInputSetupFinished(mTvInputInfo.getId(), new Runnable() {
            @Override
            public void run() {
                if (mActivityAfterCompletion != null) {
                    try {
                        startActivity(mActivityAfterCompletion);
                    } catch (ActivityNotFoundException e) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG, "Activity launch failed", e);
                    }
                }
                setResult(resultCode, data);
                finish();
            }
        });
    }
}
