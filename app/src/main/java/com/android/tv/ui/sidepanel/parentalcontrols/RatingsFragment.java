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
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import com.android.tv.parental.ContentRatingSystem;
import com.android.tv.parental.ContentRatingSystem.Rating;
import com.android.tv.parental.ParentalControlSettings;
import com.android.tv.ui.sidepanel.CheckBoxItem;
import com.android.tv.ui.sidepanel.DividerItem;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.RadioButtonItem;
import com.android.tv.ui.sidepanel.SideFragment;
import com.android.tv.util.TvSettings;
import com.android.tv.util.TvSettings.ContentRatingLevel;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
// import com.android.tv.common.experiments.Experiments;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RatingsFragment extends SideFragment {
  private static final String TAG = "RatingsFragment";
  private static final SparseIntArray sLevelResourceIdMap;
  private static final SparseIntArray sDescriptionResourceIdMap;
  private int mItemsSize;
  private TVContent mTvContent;

  static {
    sLevelResourceIdMap = new SparseIntArray(5);
    sLevelResourceIdMap.put(TvSettings.CONTENT_RATING_LEVEL_NONE, R.string.menu_arrays_None);
    sLevelResourceIdMap.put(TvSettings.CONTENT_RATING_LEVEL_HIGH, R.string.option_rating_high);
    sLevelResourceIdMap.put(TvSettings.CONTENT_RATING_LEVEL_MEDIUM, R.string.option_rating_medium);
    sLevelResourceIdMap.put(TvSettings.CONTENT_RATING_LEVEL_LOW, R.string.option_rating_low);
    sLevelResourceIdMap.put(TvSettings.CONTENT_RATING_LEVEL_CUSTOM, R.string.option_rating_custom);

    sDescriptionResourceIdMap = new SparseIntArray(sLevelResourceIdMap.size());
    sDescriptionResourceIdMap.put(
        TvSettings.CONTENT_RATING_LEVEL_HIGH, R.string.option_rating_high_description);
    sDescriptionResourceIdMap.put(
        TvSettings.CONTENT_RATING_LEVEL_MEDIUM, R.string.option_rating_medium_description);
    sDescriptionResourceIdMap.put(
        TvSettings.CONTENT_RATING_LEVEL_LOW, R.string.option_rating_low_description);
    sDescriptionResourceIdMap.put(
        TvSettings.CONTENT_RATING_LEVEL_CUSTOM, R.string.option_rating_custom_description);
  }

  private final List<RatingLevelItem> mRatingLevelItems = new ArrayList<>();
  // A map from the rating system ID string to RatingItem objects.
  private final Map<String, List<RatingItem>> mContentRatingSystemItemMap = new ArrayMap<>();
  private CheckBoxItem mBlockUnratedItem;
  private ParentalControlSettings mParentalControlSettings;

  public static String getDescription(Activity tvActivity) {
    @ContentRatingLevel
    /*int currentLevel = TvSingletons.getSingletons()
    .getParentalControlSettings().getContentRatingLevel();*/
    int level = checkRatingLevel(tvActivity);
    if (sLevelResourceIdMap.indexOfKey(level) >= 0) {
      return tvActivity.getString(sLevelResourceIdMap.get(level));
    }
    return null;
  }

  @Override
  protected String getTitle() {
    return getString(R.string.option_ratings);
  }

  @Override
  protected List<Item> getItemList() {
    TvSingletons.getSingletons().getParentalControlSettings().loadRatings();
    List<Item> items = new ArrayList<>();
    boolean hasContentRatingSystem =
        mParentalControlSettings.hasContentRatingSystemSet(); // add by jg_jianwang for DTV01090214
    if (mBlockUnratedItem != null
    /*&& Boolean.TRUE.equals(Experiments.ENABLE_UNRATED_CONTENT_SETTINGS.get())*/ ) {
      items.add(mBlockUnratedItem);
      items.add(new DividerItem());
    }

    mRatingLevelItems.clear();
    for (int i = 0; i < sLevelResourceIdMap.size(); ++i) {
      mRatingLevelItems.add(new RatingLevelItem(sLevelResourceIdMap.keyAt(i)));
    }
    updateRatingLevels();
    updateRatingLevelsEnable(hasContentRatingSystem); // add by jg_jianwang for DTV01090214
    items.addAll(mRatingLevelItems);

    mContentRatingSystemItemMap.clear();

    List<ContentRatingSystem> contentRatingSystems =
        TvSingletons.getSingletons().getContentRatingsManager().getContentRatingSystems();
    Collections.sort(contentRatingSystems, ContentRatingSystem.DISPLAY_NAME_COMPARATOR);

    if (hasContentRatingSystem) { // add begin by jg_jianwang for DTV01090214
      for (ContentRatingSystem s : contentRatingSystems) {

        if (mParentalControlSettings.isContentRatingSystemEnabled(s)) {
          List<RatingItem> ratingItems = new ArrayList<>();
          //                    boolean hasSubRating = false;
          items.add(new DividerItem(s.getDisplayName()));
          for (Rating rating : s.getRatings()) {
            if (rating.getValue() != null) {
              Log.d(TAG, "title :" + rating.getTitle() + "value :" + rating.getValue());
            }
            if (TextUtils.equals(rating.getName(), "NULL")) {
              continue;
            }
            RatingItem item =
                rating.getSubRatings().isEmpty()
                    ? new RatingItem(s, rating)
                    : new RatingWithSubItem(s, rating);
            items.add(item);
            if (rating.getSubRatings().isEmpty()) {
              ratingItems.add(item);
            } else {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "hasSubRating=true");
              //                            hasSubRating = true;
            }
          }
          // Only include rating systems that don't contain any sub ratings in the map for
          // simplicity.
          //                if (!hasSubRating) {
          mContentRatingSystemItemMap.put(s.getId(), ratingItems);
          //                }
        }
      }
    } // add end by jg_jianwang for DTV01090214
    //        if (LicenseUtils.hasRatingAttribution(getMainActivity().getAssets())) {
    //            // Display the attribution if our content rating system is selected.
    //            items.add(new DividerItem());
    //            items.add(new AttributionItem(getMainActivity()));
    //        }
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
            //                            setChecked(
            //                                    mParentalControlSettings.isRatingBlocked(
            //                                            new TvContentRating[]
            // {TvContentRating.UNRATED}));
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
            if (mParentalControlSettings.setUnratedBlocked(isChecked())) {
              updateRatingLevels();
            }
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

  public static int checkRatingLevel(Context context) {
    int ratingLevel =
        TvSingletons.getSingletons().getParentalControlSettings().getContentRatingLevel();
    if (ratingLevel != TvSettings.CONTENT_RATING_LEVEL_NONE) {
      return ratingLevel;
    }

    int blockSize = TvSingletons.getSingletons().getParentalControlSettings().getRatings().size();
    int level =
        blockSize > 0
            ? TvSettings.CONTENT_RATING_LEVEL_CUSTOM
            : TvSettings.CONTENT_RATING_LEVEL_NONE;
    Log.d(TAG, "CheckRatingLevel ratingLevel:" + ratingLevel + ",level:" + level);
    if (ratingLevel != level) {
      TvSettings.setContentRatingLevel(context, level);
    }
    return level;
  }

  private void updateRatingLevels() {
    @ContentRatingLevel int ratingLevel = mParentalControlSettings.getContentRatingLevel();
    for (RatingLevelItem ratingLevelItem : mRatingLevelItems) {
      ratingLevelItem.setChecked(ratingLevel == ratingLevelItem.mRatingLevel);
    }
  }

  // add by jg_jianwang for DTV01090214
  private void updateRatingLevelsEnable(boolean enabled) {
    for (RatingLevelItem ratingLevelItem : mRatingLevelItems) {
      ratingLevelItem.setEnabled(enabled);
    }
  }

  private class RatingLevelItem extends RadioButtonItem {
    private final int mRatingLevel;

    private RatingLevelItem(int ratingLevel) {
      super(
          getString(sLevelResourceIdMap.get(ratingLevel)),
          (sDescriptionResourceIdMap.indexOfKey(ratingLevel) >= 0)
              ? getString(sDescriptionResourceIdMap.get(ratingLevel))
              : null);
      mRatingLevel = ratingLevel;
    }

    @Override
    protected int getResourceId() {
      return R.layout.option_item_rb_right;
    }

    @Override
    protected void onSelected() {
      super.onSelected();
      mParentalControlSettings.setContentRatingLevel(
          TvSingletons.getSingletons().getContentRatingsManager(), mRatingLevel);
      /*if (mBlockUnratedItem != null
                          && Boolean.TRUE.equals(Experiments.ENABLE_UNRATED_CONTENT_SETTINGS.get())) {
                      // set checked if UNRATED is blocked, and set unchecked otherwise.
      //                mBlockUnratedItem.setChecked(
      //                        mParentalControlSettings.isRatingBlocked(
      //                                new TvContentRating[] {TvContentRating.UNRATED}));
                  }*/
      notifyItemsChanged(mRatingLevelItems.size());
    }
  }

  private class RatingItem extends CheckBoxItem {
    protected final ContentRatingSystem mContentRatingSystem;
    protected final Rating mRating;
    private final Drawable mIcon;
    private CompoundButton mCompoundButton;

    private RatingItem(ContentRatingSystem contentRatingSystem, Rating rating) {
      super(rating.getTitle(), rating.getDescription());
      mContentRatingSystem = contentRatingSystem;
      mRating = rating;
      mIcon = rating.getIcon();
    }

    @Override
    protected void onBind(View view) {
      super.onBind(view);

      mCompoundButton = (CompoundButton) view.findViewById(getCompoundButtonId());
      mCompoundButton.setVisibility(View.VISIBLE);

      ImageView imageView = (ImageView) view.findViewById(R.id.icon);
      if (mIcon != null) {
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageDrawable(mIcon);
      } else {
        imageView.setVisibility(View.GONE);
      }
    }

    @Override
    protected void onUnbind() {
      super.onUnbind();
      mCompoundButton = null;
    }

    @Override
    protected int getDescriptionViewId() {
      return R.id.description;
    }

    protected int getTitleViewId() {
      return R.id.title;
    }

    @Override
    protected void onUpdate() {
      super.onUpdate();
      mCompoundButton.setButtonDrawable(getButtonDrawable());
      setChecked(mParentalControlSettings.isRatingBlocked(mContentRatingSystem, mRating));
    }

    @Override
    protected void onSelected() {
      super.onSelected();
      if (mParentalControlSettings.setRatingBlocked(mContentRatingSystem, mRating, isChecked())) {
        updateRatingLevels();
      }
      mParentalControlSettings.setRelativeRatingsEnabled(
          mContentRatingSystem, mRating, isChecked());
      notifyDataSetChanged();
    }

    @Override
    protected int getResourceId() {
      return R.layout.option_item_rating;
    }

    protected int getButtonDrawable() {
      return R.drawable.btn_lock_material_anim;
    }

    private void setRatingBlocked(boolean isChecked) {
      if (isChecked() == isChecked) {
        return;
      }
      mParentalControlSettings.setRatingBlocked(mContentRatingSystem, mRating, isChecked);
      notifyUpdated();
    }
  }

  private class RatingWithSubItem extends RatingItem {
    private RatingWithSubItem(ContentRatingSystem contentRatingSystem, Rating rating) {
      super(contentRatingSystem, rating);
    }

    @Override
    protected void onSelected() {

      mSideFragmentManager.show(SubRatingsFragment.create(mContentRatingSystem, mRating.getName()));
    }

    @Override
    protected int getButtonDrawable() {
      int blockedStatus = mParentalControlSettings.getBlockedStatus(mContentRatingSystem, mRating);
      if (blockedStatus == ParentalControlSettings.RATING_BLOCKED) {
        return R.drawable.btn_lock_material;
      } else if (blockedStatus == ParentalControlSettings.RATING_BLOCKED_PARTIAL) {
        return R.drawable.btn_partial_lock_material;
      }
      return R.drawable.btn_unlock_material;
    }
  }

  /** Opens a dialog showing the sources of the rating descriptions. */
  public static class AttributionItem extends Item {
    public static final String DIALOG_TAG = AttributionItem.class.getSimpleName();
    //        public static final String TRACKER_LABEL = "Sources for content rating systems";
    private final Activity mMainActivity;

    public AttributionItem(Activity mainActivity) {
      mMainActivity = mainActivity;
    }

    @Override
    protected int getResourceId() {
      return R.layout.option_item_attribution;
    }

    @Override
    protected void onSelected() {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onSelected");
      /*WebDialogFragment dialog =
      WebDialogFragment.newInstance(
              LicenseUtils.RATING_SOURCE_FILE,
              mMainActivity.getString(R.string.option_attribution),
              TRACKER_LABEL);*/
      // mMainActivity.getOverlayManager().showDialogFragment(DIALOG_TAG, dialog, false);
    }
  }
}
