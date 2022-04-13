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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.android.tv.parental.ContentRatingSystem;
import com.android.tv.parental.ContentRatingSystem.Rating;
import com.android.tv.parental.ParentalControlSettings;
import com.android.tv.ui.sidepanel.CheckBoxItem;
import com.android.tv.ui.sidepanel.DividerItem;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.SideFragment;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
// import com.android.tv.common.experiments.Experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContentRatingsFragment extends SideFragment {
    private static final String TAG = "ContentRatingsFragment";

    private ParentalControlSettings mParentalControlSettings;
    private ContentRatingSystem currentContentRatingSystems;
    private Rating mCurrentRating;


    @Override
    protected String getTitle() {
        return getString(R.string.option_ratings_content);
    }

    @Override
    protected List<Item> getItemList() {
        TvSingletons.getSingletons().getParentalControlSettings().loadRatings();
        List<Item> items = new ArrayList<>();
        boolean hasContentRatingSystem =
                mParentalControlSettings.hasContentRatingSystemSet();

        List<ContentRatingSystem> contentRatingSystems =
                TvSingletons.getSingletons().getContentRatingsManager().getContentRatingSystems();
        Collections.sort(contentRatingSystems, ContentRatingSystem.DISPLAY_NAME_COMPARATOR);

        if (hasContentRatingSystem) {
            for (ContentRatingSystem s : contentRatingSystems) {
                if (mParentalControlSettings.isContentRatingSystemEnabled(s)) {
                    items.add(new DividerItem(s.getDisplayName()));
                    for (Rating rating : s.getRatings()) {
                        if (TextUtils.equals(rating.getName(), "NULL")) {
                            Log.d(TAG, "getItemList: ");
                            currentContentRatingSystems = TvSingletons.getSingletons().getContentRatingsManager().getContentRatingSystem(s.getId());
                            mCurrentRating = rating;
                            for (ContentRatingSystem.SubRating subRating : rating.getSubRatings()) {
                                items.add(new SubRatingItem(subRating));
                            }
                        }
                    }
                }
            }
        }

        return items;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mParentalControlSettings = TvSingletons.getSingletons().getParentalControlSettings();
        Log.d(TAG, "onCreate: ");
    }


    private class SubRatingItem extends CheckBoxItem {
        private final ContentRatingSystem.SubRating mSubRating;

        private SubRatingItem(ContentRatingSystem.SubRating subRating) {
            super(subRating.getTitle(), subRating.getDescription());
            mSubRating = subRating;
        }

        @Override
        protected void onBind(View view) {
            super.onBind(view);

            CompoundButton button = (CompoundButton) view.findViewById(getCompoundButtonId());
            button.setButtonDrawable(R.drawable.btn_lock_material_anim);
            button.setVisibility(View.VISIBLE);

            Drawable icon = mSubRating.getIcon();
            ImageView imageView = (ImageView) view.findViewById(R.id.icon);
            if (icon != null) {
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageDrawable(icon);
            } else {
                imageView.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onUpdate() {
            super.onUpdate();
            setChecked(isSubRatingEnabled(mSubRating));
        }

        @Override
        protected void onSelected() {
            super.onSelected();
            setSubRatingEnabled(mSubRating, isChecked(), mCurrentRating);
            TvSingletons.getSingletons().getParentalControlSettings().setRelativeRating2SubRatingEnabled(currentContentRatingSystems, isChecked(), mCurrentRating, mSubRating);
        }

        @Override
        protected int getResourceId() {
            return R.layout.option_item_rating;
        }

        @Override
        protected int getDescriptionViewId() {
            return R.id.description;
        }

        protected int getTitleViewId() {
            return R.id.title;
        }

    }

    private boolean isSubRatingEnabled(ContentRatingSystem.SubRating subRating) {
        return TvSingletons.getSingletons()
                .getParentalControlSettings()
                .isSubRatingEnabled(currentContentRatingSystems, mCurrentRating, subRating);
    }

    private void setSubRatingEnabled(ContentRatingSystem.SubRating subRating, boolean enabled, Rating rating) {
        TvSingletons.getSingletons()
                .getParentalControlSettings()
                .setSubRatingBlocked(currentContentRatingSystems, rating, subRating, enabled);
    }
}
