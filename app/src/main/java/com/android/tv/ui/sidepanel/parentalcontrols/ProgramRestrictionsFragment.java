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

package com.android.tv.ui.sidepanel.parentalcontrols;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.android.tv.ui.sidepanel.ActionItem;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.SideFragment;
import com.android.tv.ui.sidepanel.SubMenuItem;
import com.android.tv.ui.sidepanel.SwitchItem;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;


import java.util.ArrayList;
import java.util.List;

public class ProgramRestrictionsFragment extends SideFragment {
    private static final String TAG = "ProgramRestrictionsFragment";
    private List<ActionItem> mActionItems;
    private final SideFragmentListener mSideFragmentListener =
            new SideFragmentListener() {
                @Override
                public void onSideFragmentViewDestroyed() {
                    notifyDataSetChanged();
                }
            };
    private boolean mRatingEnable=false;
    public static String getDescription(Activity tvActivity) {
        return CommonIntegration.isEURegion() || CommonIntegration.isSARegion() ? RatingsSimpleFragment.getDescription(tvActivity) : RatingsFragment.getDescription(tvActivity);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.option_program_restrictions);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	TvSingletons.getSingletons().getParentalControlSettings().loadRatings();
    }
    //@Override
    //public String getTrackerLabel() {
    //    return TRACKER_LABEL;
    //}
    @Override
    protected List<Item> getItemList() {
    	int ratingEnable= TVContent.getInstance(getActivity()).getRatingEnable();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ratingEnable="+ratingEnable);
        mRatingEnable=ratingEnable==1;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mRatingEnable="+mRatingEnable);
        List<Item> items = new ArrayList<>();
        mActionItems = new ArrayList<>();
        items.add(
                new SwitchItem(
                        getString(R.string.common_on),
                        getString(R.string.common_off)) {
                    @Override
                    protected void onUpdate() {
                        super.onUpdate();
                        setChecked(mRatingEnable);
                    }

                    @Override
                    protected void onSelected() {
                        super.onSelected();
                        boolean checked = isChecked();
                        TVContent.getInstance(getActivity()).setRatingEnable(checked);
                        enableActionItems(checked);
                    }
                });
        mActionItems.add(
                new SubMenuItem(
                        getString(R.string.option_country_rating_systems),
                        RatingSystemsFragment.getDescription(getMainActivity()),
                        getMainActivity().getSideFragmentManager()) {
                    @Override
                    protected SideFragment getFragment() {
                        SideFragment fragment = new RatingSystemsFragment();
                        fragment.setListener(mSideFragmentListener);
                        return fragment;
                    }
                });

        SubMenuItem ratingsItem;
        if (CommonIntegration.isEURegion() || CommonIntegration.isSARegion()){
            String ratingsDescription = RatingsSimpleFragment.getDescription(getMainActivity());
            ratingsItem =
                    new SubMenuItem(
                            getString(CommonIntegration.isSARegion() ? R.string.option_ratings_age : R.string.option_ratings),
                            ratingsDescription,
                            getMainActivity().getSideFragmentManager()) {
                        @Override
                        protected SideFragment getFragment() {
                            SideFragment fragment = new RatingsSimpleFragment();
                            fragment.setListener(mSideFragmentListener);
                            return fragment;
                        }
                    };
        }else {
            String ratingsDescription = RatingsFragment.getDescription(getMainActivity());
            ratingsItem =
                    new SubMenuItem(
                            getString(R.string.option_ratings),
                            ratingsDescription,
                            getMainActivity().getSideFragmentManager()) {
                        @Override
                        protected SideFragment getFragment() {
                            SideFragment fragment = new RatingsFragment();
                            fragment.setListener(mSideFragmentListener);
                            return fragment;
                        }
                    };
        }
        // When "None" is selected for rating systems, disable the Ratings option.
        if (RatingSystemsFragment.getDescription(getMainActivity())
                .equals(getString(R.string.menu_arrays_None))) {
            ratingsItem.setEnabled(false);
        }
        mActionItems.add(ratingsItem);

        //content ratings for SA
        if (CommonIntegration.isSARegion()) {
            mActionItems.add(new SubMenuItem(
                    getString(R.string.option_ratings_content),
                    null,
                    getMainActivity().getSideFragmentManager()
            ) {
                @Override
                protected SideFragment getFragment() {
                    SideFragment fragment = new ContentRatingsFragment();
                    fragment.setListener(mSideFragmentListener);
                    return fragment;
                }
            });
        }

        boolean isSupportOpenVChip = DataSeparaterUtil.getInstance().isOpenVChipSupport();
        Log.d(TAG, "isSupportOpenVChip :" + isSupportOpenVChip);
        if (CommonIntegration.isUSRegion() && isSupportOpenVChip && ! TVContent.getInstance(getActivity()).isKoreaCountry()) {
        	SubMenuItem openVChip=new SubMenuItem(
                    getString(R.string.option_program_open_vchip),
                    "",
                    mSideFragmentManager) {
                @Override
                protected SideFragment getFragment() {
                    SideFragment fragment = new OpenVchipRegionFragment();
                    fragment.setListener(mSideFragmentListener);
                    return fragment;
                }
            };
        	mActionItems.add(openVChip);
         }
        items.addAll(mActionItems);
        enableActionItems(mRatingEnable);
        return items;
    }
    private void enableActionItems(boolean enabled) {
        for (ActionItem actionItem : mActionItems) {
            actionItem.setEnabled(enabled);
        }
    }
}
