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

import android.os.Bundle;

import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.android.tv.parental.ContentRatingSystem;
import com.android.tv.parental.ContentRatingsManager;
import com.android.tv.parental.ParentalControlSettings;
import com.android.tv.ui.sidepanel.CheckBoxItem;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.SideFragment;
import com.android.tv.util.TvSettings;
import com.android.tv.util.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.mediatek.twoworlds.tv.MtkTvConfig;


public class RatingSystemsFragment extends SideFragment {

    private static final String TAG = "RatingSystemsFragment";
    private static String mSelectCountry;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if(CommonIntegration.isUSRegion()){
            mSelectCountry = "US";
        }
        else if(CommonIntegration.isEURegion()){
            mSelectCountry = Utils.convertConurty(MtkTvConfig.getInstance().getCountry());
        }
        else if(CommonIntegration.isSARegion()){
            mSelectCountry = "BR";
        }*/
        updateCurrentCountry();
        setDefaultRatingSystemsIfNeeded((Activity) getActivity());
    }

    private static void updateCurrentCountry(){
        if(CommonIntegration.isUSRegion()){
            mSelectCountry = "US";
        }
        else if(CommonIntegration.isEURegion()){
            mSelectCountry = Utils.convertConurty(MtkTvConfig.getInstance().getCountry());
        }
        else if(CommonIntegration.isSARegion()){
            mSelectCountry = "BR";
        }
    }

    public static String getDescription(Activity tvActivity) {
        updateCurrentCountry();
        setDefaultRatingSystemsIfNeeded(tvActivity);

        List<ContentRatingSystem> contentRatingSystems =
                TvSingletons.getSingletons()
                	.getContentRatingsManager().getContentRatingSystems();
        Collections.sort(contentRatingSystems, ContentRatingSystem.DISPLAY_NAME_COMPARATOR);
        StringBuilder builder = new StringBuilder();
        for (ContentRatingSystem s : contentRatingSystems) {
            if (!TvSingletons.getSingletons().
				getParentalControlSettings().isContentRatingSystemEnabled(s)) {
                continue;
            }
            builder.append(s.getDisplayName());
            builder.append(", ");
        }
        return builder.length() > 0
                ? builder.substring(0, builder.length() - 2)
                : tvActivity.getString(R.string.menu_arrays_None);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.option_country_rating_systems);
    }

    //@Override
    //public String getTrackerLabel() {
    //    return TRACKER_LABEL;
    //}

    @Override
    protected List<Item> getItemList() {
        ContentRatingsManager contentRatingsManager =
				TvSingletons.getSingletons().getContentRatingsManager();
//        ParentalControlSettings parentalControlSettings =
//                TvSingletons.getSingletons().getParentalControlSettings();
        List<ContentRatingSystem> contentRatingSystems =
                contentRatingsManager.getContentRatingSystems();
        Collections.sort(contentRatingSystems, ContentRatingSystem.DISPLAY_NAME_COMPARATOR);
        List<Item> items = new ArrayList<>();
        List<Item> itemsHidden = new ArrayList<>();
        List<Item> itemsHiddenMultipleCountries = new ArrayList<>();
        boolean hasMappingCountries = false;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mSelectCountry="+mSelectCountry);
        // Add default, custom and preselected content rating systems to the "short" list.
        for (ContentRatingSystem s : contentRatingSystems) {
        	Log.d(TAG, "mSelectCountry = "+ mSelectCountry);
            if (s.getCountries() != null && s.getCountries().contains(mSelectCountry)) {
            	Log.d(TAG, "has mSelectCountry ");
                items.add(new RatingSystemItem(s));
                hasMappingCountries = true;
            } else if (s.isCustom()) {
            	Log.d(TAG, "isCustom");
                items.add(new RatingSystemItem(s));
            } else {
                List<String> countries = s.getCountries();

                if (countries.size() > 2) {
                	Log.d(TAG, "countries.size() > 2");
                    // Convert country codes to display names.
                    /*for (int i = 0; i < countries.size(); ++i) {
                        countries.set(i, new Locale("", countries.get(i)).getDisplayCountry());
                    }
                    Collections.sort(countries);
                    StringBuilder builder = new StringBuilder();
                    for (String country : countries) {
                        builder.append(country);
                        builder.append(", ");
                    }*/
                    itemsHiddenMultipleCountries.add(
                            new RatingSystemItem(s));
                } else {
                	Log.d(TAG, "itemsHidden");
                    itemsHidden.add(new RatingSystemItem(s));
                }
            }
        }
        if(!hasMappingCountries){
            items.addAll(itemsHiddenMultipleCountries);
        }
        // Add the rest of the content rating systems to the "long" list.
        /*final List<Item> allItems = new ArrayList<>(items);
        allItems.addAll(itemsHidden);
        allItems.addAll(itemsHiddenMultipleCountries);*/

        // Add "See All" to the "short" list.
        /*items.add(
                new ActionItem(getString(R.string.option_see_all_rating_systems)) {
                    @Override
                    protected void onSelected() {
                        setItems(allItems);
                    }
                });*/
        return items;
    }

    private static void setDefaultRatingSystemsIfNeeded(Activity tvActivity) {
        if (TvSettings.isContentRatingSystemSet(tvActivity)) {
            return;
        }
        /*if(CommonIntegration.isUSRegion()){
        	mSelectCountry = "US";
        }
        else if(CommonIntegration.isEURegion()){
        	mSelectCountry = Util.convertConurty(MtkTvConfig.getInstance().getCountry());
        }
        else if(CommonIntegration.isSARegion()){
        	mSelectCountry = "BR";
        }*/
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mSelectCountry="+mSelectCountry);
        // Sets the default if the content rating system has never been set.
        List<ContentRatingSystem> contentRatingSystems =
                TvSingletons.getSingletons()
                	.getContentRatingsManager().getContentRatingSystems();
        ContentRatingsManager manager =
				TvSingletons.getSingletons().getContentRatingsManager();
        ParentalControlSettings settings =
			TvSingletons.getSingletons().getParentalControlSettings();
        ContentRatingSystem otherCountries = null;
        boolean hasMappingCountries = false;
        for (ContentRatingSystem s : contentRatingSystems) {
            if (!s.isCustom()
                    && s.getCountries() != null
                    && s.getCountries().contains(mSelectCountry)) {
                settings.setContentRatingSystemEnabled(manager, s, true);
                hasMappingCountries = true;
            }

            if(!s.isCustom()
                    && s.getCountries() != null
                    && s.getCountries().size() > 2){
            	Log.d(TAG, "otherCountries = s");
                otherCountries = s;
            }
        }

        if(!hasMappingCountries && otherCountries != null){
            settings.setContentRatingSystemEnabled(manager, otherCountries, true);
        }
    }

    private class RatingSystemItem extends CheckBoxItem {
        private final ContentRatingSystem mContentRatingSystem;

        RatingSystemItem(ContentRatingSystem contentRatingSystem) {
            this(contentRatingSystem, null);
        }

        RatingSystemItem(ContentRatingSystem contentRatingSystem, String description) {
            super(contentRatingSystem.getDisplayName(), description, description != null);
            Log.d(TAG, "contentRatingSystem.getDisplayName() = "+ contentRatingSystem.getDisplayName());
            mContentRatingSystem = contentRatingSystem;
        }

        @Override
        protected void onBind(View view) {
            super.onBind(view);
            View channelContent = view.findViewById(R.id.channel_content);
            if(channelContent != null){
                channelContent.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onUpdate() {
            super.onUpdate();
            setChecked(TvSingletons.getSingletons()
                            .getParentalControlSettings()
                            .isContentRatingSystemEnabled(mContentRatingSystem));
        }

        @Override
        protected int getResourceId() {
            return R.layout.option_item_cb_right;
        }

        @Override
        protected void onSelected() {
            super.onSelected();
			TvSingletons.getSingletons()
                    .getParentalControlSettings()
                    .setContentRatingSystemEnabled(
                            TvSingletons.getSingletons().getContentRatingsManager(),
                            mContentRatingSystem,
                            isChecked());
        }
    }
}
