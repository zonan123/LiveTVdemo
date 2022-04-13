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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.android.tv.parental.ContentRatingSystem;
import com.android.tv.parental.ContentRatingSystem.Rating;
import com.android.tv.parental.ParentalControlSettings;
import com.android.tv.ui.sidepanel.CheckBoxItem;
import com.android.tv.ui.sidepanel.DividerItem;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.RadioButtonItem;
import com.android.tv.ui.sidepanel.SideFragment;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
// import com.android.tv.common.experiments.Experiments;
//
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RatingsSimpleFragment extends SideFragment {
  private static final String TAG = "RatingsSimpleFragment";
  private int mItemsSize;
  private TVContent mTvContent;
  // A map from the rating system ID string to RatingItem objects.
  //    private final Map<String, List<RatingItem>> mContentRatingSystemItemMap = new ArrayMap<>();
  private CheckBoxItem mBlockUnratedItem;
  private ParentalControlSettings mParentalControlSettings;

  public static String getDescription(Activity tvActivity) {
    Log.d(TAG, "getDescription for null");
    return null;
  }

  @Override
  protected String getTitle() {
    return getString(R.string.option_ratings);
  }

  @Override
  protected List<Item> getItemList() {
    boolean isFirstBlock = true;
    TvSingletons.getSingletons().getParentalControlSettings().loadRatings();
    List<Item> items = new ArrayList<>();
    boolean hasContentRatingSystem =
        mParentalControlSettings.hasContentRatingSystemSet(); // add by jg_jianwang for DTV01090214
    if (mBlockUnratedItem != null
    /*&& Boolean.TRUE.equals(Experiments.ENABLE_UNRATED_CONTENT_SETTINGS.get())*/ ) {
      items.add(mBlockUnratedItem);
      items.add(new DividerItem());
    }

    //        mContentRatingSystemItemMap.clear();

    List<ContentRatingSystem> contentRatingSystems =
        TvSingletons.getSingletons().getContentRatingsManager().getContentRatingSystems();
    Collections.sort(contentRatingSystems, ContentRatingSystem.DISPLAY_NAME_COMPARATOR);

    if (hasContentRatingSystem) { // add begin by jg_jianwang for DTV01090214
      for (ContentRatingSystem s : contentRatingSystems) {

        if (mParentalControlSettings.isContentRatingSystemEnabled(s)) {
          isFirstBlock = true;
          //                    List<RatingItem> ratingItems = new ArrayList<>();
          //                    boolean hasSubRating = false;
          items.add(new DividerItem(s.getDisplayName()));
          RatingLevelItem noneItem = new RatingLevelItem(getString(R.string.menu_arrays_None));
          if (!mTvContent.isFraCountry()) {
            //add none item except france
            items.add(noneItem);
          }
          for (Rating rating : s.getRatings()) {
            if (rating.getValue() != null) {
              Log.d(TAG, "title :" + rating.getTitle() + "value :" + rating.getValue());
            }
            if (TextUtils.equals(rating.getName(), "NULL")) {
              continue;
            }
            //                        RatingItem item =
            //                                rating.getSubRatings().isEmpty()
            //                                        ? new RatingItem(s, rating)
            //                                        : new RatingWithSubItem(s, rating);
            String title =
                "DVB".equals(rating.getName().substring(0, 3))
                    ? rating.getTitle() + " " + getString(R.string.rating_years)
                    : rating.getTitle();
            RatingLevelItem ratingLevelItem = new RatingLevelItem(s, rating, title);
            items.add(ratingLevelItem);
            if (mParentalControlSettings.isRatingBlocked(s, rating) && isFirstBlock) {
              ratingLevelItem.setChecked(true);
              isFirstBlock = false;
            }
          }
          if (isFirstBlock) {
            noneItem.setChecked(true);
          }
          //                    mContentRatingSystemItemMap.put(s.getId(), ratingItems);
        }
      }
    } // add end by jg_jianwang for DTV01090214
    mItemsSize = items.size();
    return items;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mTvContent = TVContent.getInstance(getActivity());
    mParentalControlSettings = TvSingletons.getSingletons().getParentalControlSettings();
    mBlockUnratedItem =
        new CheckBoxItem(getResources().getString(R.string.option_block_unrated_programs)) {

          @Override
          protected void onUpdate() {
            super.onUpdate();
            setChecked(mTvContent.getBlockUnrated() == 1 ? true : false);
          }

          @Override
          protected int getResourceId() {
            return R.layout.option_item_cb_right;
          }

          @Override
          protected void onBind(View view) {
            super.onBind(view);
            View channelContent = view.findViewById(R.id.channel_content);
            if (channelContent != null) {
              channelContent.setVisibility(View.GONE);
            }
          }

          @Override
          protected void onSelected() {
            super.onSelected();
            mParentalControlSettings.setUnratedBlocked(isChecked());
          }
        };
  }

  @Override
  public void onResume() {
    super.onResume();
    // Although we set the attribution item at the end of the item list non-focusable, we do get
    // its position when the fragment is resumed. This ensures that we do not select the
    // non-focusable item at the end of the list. See b/17387103.
    if (getSelectedPosition() >= mItemsSize) {
      setSelectedPosition(mItemsSize - 1);
    }
  }

  private class RatingLevelItem extends RadioButtonItem {
    //        private final int mRatingLevel;
    protected final ContentRatingSystem mContentRatingSystem;
    protected final Rating mRating;

    private RatingLevelItem(ContentRatingSystem contentRatingSystem, Rating rating, String title) {
      super(title, null);
      //            mRatingLevel = ratingLevel;
      mContentRatingSystem = contentRatingSystem;
      mRating = rating;
      Log.d(TAG, rating.getName());
    }

    private RatingLevelItem(String title) {
      super(title, null);
      mContentRatingSystem = null;
      mRating = null;
    }

    @Override
    protected int getResourceId() {
      return R.layout.option_item_rb_left;
    }

    @Override
    protected void onSelected() {
      super.onSelected();
      //            mParentalControlSettings.setContentRatingLevel(
      //                    TvSingletons.getSingletons().getContentRatingsManager(),
      //                    mRatingLevel);
      /*if (mBlockUnratedItem != null
                          && Boolean.TRUE.equals(Experiments.ENABLE_UNRATED_CONTENT_SETTINGS.get())) {
                      // set checked if UNRATED is blocked, and set unchecked otherwise.
      //                mBlockUnratedItem.setChecked(
      //                        mParentalControlSettings.isRatingBlocked(
      //                                new TvContentRating[] {TvContentRating.UNRATED}));
                  }*/
      // notifyItemsChanged(mRatingLevelItems.size());
      if (mRating == null) {
        Log.d(TAG, "onSelected   NONE");
        mParentalControlSettings.clearRatingBeforeSetRating();
        return;
      }
      mParentalControlSettings.clearRatingBeforeSetRating();
      mParentalControlSettings.setRatingBlocked(mContentRatingSystem, mRating, isChecked());
      mParentalControlSettings.setRelativeRatingsEnabled(
          mContentRatingSystem, mRating, isChecked());
    }
  }
}
