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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.android.tv.parental.ContentRatingSystem;
import com.android.tv.parental.ContentRatingSystem.Rating;
import com.android.tv.parental.ContentRatingSystem.SubRating;
import com.android.tv.ui.sidepanel.CheckBoxItem;
import com.android.tv.ui.sidepanel.DividerItem;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.SideFragment;

import java.util.ArrayList;
import java.util.List;

public class SubRatingsFragment extends SideFragment {
//	private final static String TAG ="SubRatingsFragment";

    private static final String ARGS_CONTENT_RATING_SYSTEM_ID = "args_content_rating_system_id";
    private static final String ARGS_RATING_NAME = "args_rating_name";

    private ContentRatingSystem mContentRatingSystem;
    private Rating mRating;
    private final List<SubRatingItem> mSubRatingItems = new ArrayList<>();
    public static SubRatingsFragment create(
            ContentRatingSystem contentRatingSystem, String ratingName) {
        SubRatingsFragment fragment = new SubRatingsFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_CONTENT_RATING_SYSTEM_ID, contentRatingSystem.getId());
        args.putString(ARGS_RATING_NAME, ratingName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentRatingSystem =
                TvSingletons.getSingletons()
                        .getContentRatingsManager()
                        .getContentRatingSystem(
                                getArguments().getString(ARGS_CONTENT_RATING_SYSTEM_ID));
        if (mContentRatingSystem != null) {
            mRating = mContentRatingSystem.getRating(getArguments().getString(ARGS_RATING_NAME));
        }
        if (mRating == null) {
            closeFragment();
        }
    }
    
    
    
    
    

    @Override
    protected String getTitle() {
        return getString(R.string.option_subrating_title, mRating.getTitle());
    }

    //@Override
    //public String getTrackerLabel() {
    //    return TRACKER_LABEL;
    //}

    @Override
    protected List<Item> getItemList() {
        List<Item> items = new ArrayList<>();
        items.add(new RatingItem());
        items.add(new DividerItem(getString(R.string.option_subrating_header)));
        mSubRatingItems.clear();
        for (SubRating subRating : mRating.getSubRatings()) {
            mSubRatingItems.add(new SubRatingItem(subRating));
        }
        items.addAll(mSubRatingItems);
        return items;
    }

    private class RatingItem extends CheckBoxItem {
        private RatingItem() {
            super(mRating.getTitle(), mRating.getDescription());
        }

        @Override
        protected void onBind(View view) {
            super.onBind(view);

            CompoundButton button = (CompoundButton) view.findViewById(getCompoundButtonId());
            button.setButtonDrawable(R.drawable.btn_lock_material_anim);
            button.setVisibility(View.VISIBLE);

            Drawable icon = mRating.getIcon();
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
            setChecked(isRatingEnabled());
        }

        @Override
        protected void onSelected() {
            super.onSelected();
            boolean checked = isChecked();
            setRatingEnabled(checked,mRating);
            //modified begin by jg_jianwang for DTV01081110
            /*if (checked) {
                TvSingletons.getSingletons().getParentalControlSettings().setRelativeRatingsEnabled(mContentRatingSystem,mRating,checked);
                // If the rating is checked, check and disable all the sub rating items.
                for (SubRating subRating : mRating.getSubRatings()) {
                    setSubRatingEnabled(subRating, true,mRating);
                }
                for (SubRatingItem item : mSubRatingItems) {
                    item.setChecked(true);
                    item.setEnabled(false);
                }
            } else {
                // If the rating is unchecked, just enable all the sub rating items and do not
                // change the check state.
                for (SubRatingItem item : mSubRatingItems) {
                    item.setEnabled(true);
                }

            }*/
            TvSingletons.getSingletons().getParentalControlSettings().setRelativeRatingsEnabled(mContentRatingSystem,mRating,checked);//add by jg_jianwang for DTV01081110
            for (SubRating subRating : mRating.getSubRatings()) {
                setSubRatingEnabled(subRating, checked,mRating);
            }
            for (SubRatingItem item : mSubRatingItems) {
                item.setChecked(checked);
            }
          //modified end by jg_jianwang for DTV01081110
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

    private class SubRatingItem extends CheckBoxItem {
        private final SubRating mSubRating;
        private SubRatingItem(SubRating subRating) {
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
            //deleted by jg_jianwang for DTV01081110
            //setEnabled(!isRatingEnabled());
        }

        @Override
        protected void onSelected() {
            super.onSelected();
            setSubRatingEnabled(mSubRating, isChecked(),mRating);
            //modified begin by jg_jianwang for DTV01081110
            TvSingletons.getSingletons().getParentalControlSettings().setRelativeRating2SubRatingEnabled(mContentRatingSystem,isChecked(),mRating,mSubRating);
            /*if(!isChecked()){
	           if(!isRatingBlocked()){
	            	TvSingletons.getSingletons().getParentalControlSettings().setRelativeRatingsEnabled(mContentRatingSystem,mRating,false);
	           }
            }*/
            //modified end by jg_jianwang for DTV01081110
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
    
/*    private boolean isRatingBlocked(){
    	 for (SubRatingItem subRating : mSubRatingItems) {
         	if(subRating.isChecked()){
         		return true;
         	}
         }
    	 return false;
    }*/

    private boolean isRatingEnabled() {
        return TvSingletons.getSingletons()
                .getParentalControlSettings()
                .isRatingBlocked(mContentRatingSystem, mRating);
    }

    private boolean isSubRatingEnabled(SubRating subRating) {
        return TvSingletons.getSingletons()
                .getParentalControlSettings()
                .isSubRatingEnabled(mContentRatingSystem, mRating, subRating);
    }

    private void setRatingEnabled(boolean enabled,Rating rating) {
        TvSingletons.getSingletons()
                .getParentalControlSettings()
                .setRatingBlocked(mContentRatingSystem, rating, enabled);
    }

    private void setSubRatingEnabled(SubRating subRating, boolean enabled,Rating rating) {
        TvSingletons.getSingletons()
                .getParentalControlSettings()
                .setSubRatingBlocked(mContentRatingSystem, rating, subRating, enabled);
    }
}
