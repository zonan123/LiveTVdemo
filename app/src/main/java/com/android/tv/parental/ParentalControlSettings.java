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

package com.android.tv.parental;

import android.content.Context;
import android.media.tv.TvContentRating;
import android.media.tv.TvInputManager;
import android.util.Log;

import com.android.tv.parental.ContentRatingSystem.Order;
import com.android.tv.parental.ContentRatingSystem.Rating;
import com.android.tv.parental.ContentRatingSystem.SubRating;
import com.android.tv.util.TvSettings;
import com.android.tv.util.TvSettings.ContentRatingLevel;
import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParentalControlSettings {
	private final static String TAG ="ParentalControlSettings";

    /** The rating and all of its sub-ratings are blocked. */
    public static final int RATING_BLOCKED = 0;

    /** The rating is blocked but not all of its sub-ratings are blocked. */
    public static final int RATING_BLOCKED_PARTIAL = 1;

    /** The rating is not blocked. */
    public static final int RATING_NOT_BLOCKED = 2;

    private final Context mContext;
    private final TvInputManager mTvInputManager;

    // mRatings is expected to be synchronized with mTvInputManager.getBlockedRatings().
    private Set<TvContentRating> mRatings;
    private Set<TvContentRating> mCustomRatings;

    public ParentalControlSettings(Context context) {
        mContext = context;
        mTvInputManager = (TvInputManager) mContext.getSystemService(Context.TV_INPUT_SERVICE);
    }

    public boolean isParentalControlsEnabled() {
        return mTvInputManager.isParentalControlsEnabled();
    }

    public void setParentalControlsEnabled(boolean enabled) {
        mTvInputManager.setParentalControlsEnabled(enabled);
    }

    public void setContentRatingSystemEnabled(
            ContentRatingsManager manager,
            ContentRatingSystem contentRatingSystem,
            boolean enabled) {
        if (enabled) {
            TvSettings.addContentRatingSystem(mContext, contentRatingSystem.getId());

            // Ensure newly added system has ratings for current level set
            updateRatingsForCurrentLevel(manager);
        } else {
            // Ensure no ratings are blocked for the selected rating system
            for (TvContentRating tvContentRating : mTvInputManager.getBlockedRatings()) {
                if (contentRatingSystem.ownsRating(tvContentRating)) {
                    mTvInputManager.removeBlockedRating(tvContentRating);
                }
            }

            TvSettings.removeContentRatingSystem(mContext, contentRatingSystem.getId());
        }
    }

    public boolean isContentRatingSystemEnabled(ContentRatingSystem contentRatingSystem) {
        return TvSettings.hasContentRatingSystem(mContext, contentRatingSystem.getId());
    }

    //add by jg_jianwang for DTV01090214
    public boolean hasContentRatingSystemSet() {
        return TvSettings.hasContentRatingSystem(mContext);
    }

    public void loadRatings() {
        mRatings = new HashSet<>(mTvInputManager.getBlockedRatings());
        if(mRatings!= null) {
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mRatings="+mRatings.size());
        }
    }
    
    
    public Set<TvContentRating> getRatings(){
//    	Set<TvContentRating> ratings = new HashSet<>(mTvInputManager.getBlockedRatings());
    	return new HashSet<>(mTvInputManager.getBlockedRatings());
    }

    private void storeRatings() {
        Set<TvContentRating> removed = new HashSet<>(mTvInputManager.getBlockedRatings());
        removed.removeAll(mRatings);
        for (TvContentRating tvContentRating : removed) {
            String str = tvContentRating.flattenToString();
            String last = str.substring(str.length() - 1);
            if (("D".equals(last) || "S".equals(last) || "V".equals(last)) && CommonIntegration.isSARegion()) {
                Log.d(TAG, "storeRatings: " + str);
                continue;
            }
            mTvInputManager.removeBlockedRating(tvContentRating);
        }

        Set<TvContentRating> added = new HashSet<>(mRatings);
        added.removeAll(mTvInputManager.getBlockedRatings());
        for (TvContentRating tvContentRating : added) {
            mTvInputManager.addBlockedRating(tvContentRating);
        }
    }

    private void updateRatingsForCurrentLevel(ContentRatingsManager manager) {
        @ContentRatingLevel int currentLevel = getContentRatingLevel();
        if (currentLevel != TvSettings.CONTENT_RATING_LEVEL_CUSTOM) {
            mRatings = ContentRatingLevelPolicy.getRatingsForLevel(this, manager, currentLevel);
            if (currentLevel != TvSettings.CONTENT_RATING_LEVEL_NONE) {
                // UNRATED contents should be blocked unless the rating level is none or custom
                mRatings.add(TvContentRating.UNRATED);
            }
            storeRatings();
        }
    }

    public void clearRatingBeforeSetRating(){
        mRatings.clear();
        storeRatings();
    }

    public void setContentRatingLevel(
            ContentRatingsManager manager, @ContentRatingLevel int level) {
        @ContentRatingLevel int currentLevel = getContentRatingLevel();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentLevel="+currentLevel+",level="+level);
        if (level == currentLevel) {
            return;
        }
        if (currentLevel == TvSettings.CONTENT_RATING_LEVEL_CUSTOM) {
            mCustomRatings = mRatings;
        }
        TvSettings.setContentRatingLevel(mContext, level);
        if (level == TvSettings.CONTENT_RATING_LEVEL_CUSTOM) {
            if (mCustomRatings != null) {
                mRatings = new HashSet<>(mCustomRatings);
            }
        } else {
            mRatings = ContentRatingLevelPolicy.getRatingsForLevel(this, manager, level);
            if (level != TvSettings.CONTENT_RATING_LEVEL_NONE
				/*&& Boolean.TRUE.equals(Experiments.ENABLE_UNRATED_CONTENT_SETTINGS.get())*/
                    ) {
                // UNRATED contents should be blocked unless the rating level is none or custom
                mRatings.add(TvContentRating.UNRATED);
            }else{
            	mRatings.clear();
            }
        }
        storeRatings();
    }

    @ContentRatingLevel
    public int getContentRatingLevel() {
    	int currentLevel=TvSettings.getContentRatingLevel(mContext);
		if (currentLevel < TvSettings.CONTENT_RATING_LEVEL_NONE
				|| currentLevel > TvSettings.CONTENT_RATING_LEVEL_CUSTOM) {
			currentLevel = 0;
		}
        return currentLevel;
    }

    /** Sets the blocked status of a unrated contents. */
    public boolean setUnratedBlocked(boolean blocked) {
        boolean changed = false;
//        if (blocked) {
//            changed = mRatings.add(TvContentRating.UNRATED);
//            mTvInputManager.addBlockedRating(TvContentRating.UNRATED);
//        } else {
//            changed = mRatings.remove(TvContentRating.UNRATED);
//            mTvInputManager.removeBlockedRating(TvContentRating.UNRATED);
//        }
        TVContent.getInstance(mContext).setBlockUnrated(blocked);
        /*if (changed) {
            // change to custom level if the blocked status is changed
            changeToCustomLevel();
        }*/
        return changed;
    }

    /**
     * Sets the blocked status of a given content rating.
     *
     * <p>Note that a call to this method automatically changes the current rating level to {@code
     * TvSettings.CONTENT_RATING_LEVEL_CUSTOM} if needed.
     *
     * @param contentRatingSystem The content rating system where the given rating belongs.
     * @param rating The content rating to set.
     * @return {@code true} if changed, {@code false} otherwise.
     * @see #setSubRatingBlocked
     */
    public boolean setRatingBlocked(
            ContentRatingSystem contentRatingSystem, Rating rating, boolean blocked) {
        return setRatingBlockedInternal(contentRatingSystem, rating, null, blocked);
    }

    /**
     * Checks whether any of given ratings is blocked.
     *
     * @param ratings The array of ratings to check
     * @return {@code true} if a rating is blocked, {@code false} otherwise.
     */
    public boolean isRatingBlocked(TvContentRating[] ratings) {
        return getBlockedRating(ratings) != null;
    }

    /**
     * Checks whether any of given ratings is blocked and returns the first blocked rating.
     *
     * @param ratings The array of ratings to check
     * @return The {@link TvContentRating} that is blocked.
     */
    public TvContentRating getBlockedRating(TvContentRating[] ratings) {
        if (ratings == null || ratings.length <= 0) {
            return mTvInputManager.isRatingBlocked(TvContentRating.UNRATED)
                    ? TvContentRating.UNRATED
                    : null;
        }
        for (TvContentRating rating : ratings) {
            if (mTvInputManager.isRatingBlocked(rating)) {
                return rating;
            }
        }
        return null;
    }

    /**
     * Checks whether a given rating is blocked by the user or not.
     *
     * @param contentRatingSystem The content rating system where the given rating belongs.
     * @param rating The content rating to check.
     * @return {@code true} if blocked, {@code false} otherwise.
     */
    public boolean isRatingBlocked(ContentRatingSystem contentRatingSystem, Rating rating) {
        //modified start by jg_jianwang for DTV01081110
        /*boolean isRatingBlocked=false;
    	TvContentRating tvContentRating=toTvContentRating(contentRatingSystem, rating);
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tvContentRating---->domain:"+tvContentRating.getDomain()+",ratingSystem:"+tvContentRating.getRatingSystem()+",mainRating:"+tvContentRating.getMainRating());
    	for(TvContentRating tempRating : mRatings){
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "tempRating---->domain:"+tempRating.getDomain()+",ratingSystem:"+tempRating.getRatingSystem()+",mainRating:"+tempRating.getMainRating());
    		if(TextUtils.equals(tempRating.getDomain(), tvContentRating.getDomain())
    				&&TextUtils.equals(tempRating.getRatingSystem(), tvContentRating.getRatingSystem())
    				&&TextUtils.equals(tempRating.getMainRating(), tvContentRating.getMainRating())){
    			isRatingBlocked=true;
    			break;
    		}
    	}
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isRatingBlocked ="+isRatingBlocked);*/
        return mRatings.contains(toTvContentRating(contentRatingSystem, rating));
        //modified end by jg_jianwang for DTV01081110
    }

    /**
     * Sets the blocked status of a given content sub-rating.
     *
     * <p>Note that a call to this method automatically changes the current rating level to {@code
     * TvSettings.CONTENT_RATING_LEVEL_CUSTOM} if needed.
     *
     * @param contentRatingSystem The content rating system where the given rating belongs.
     * @param rating The content rating associated with the given sub-rating.
     * @param subRating The content sub-rating to set.
     * @return {@code true} if changed, {@code false} otherwise.
     * @see #setRatingBlocked
     */
    public boolean setSubRatingBlocked(
            ContentRatingSystem contentRatingSystem,
            Rating rating,
            SubRating subRating,
            boolean blocked) {
        return setRatingBlockedInternal(contentRatingSystem, rating, subRating, blocked);
    }

    /**
     * Checks whether a given content sub-rating is blocked by the user or not.
     *
     * @param contentRatingSystem The content rating system where the given rating belongs.
     * @param rating The content rating associated with the given sub-rating.
     * @param subRating The content sub-rating to check.
     * @return {@code true} if blocked, {@code false} otherwise.
     */
    public boolean isSubRatingEnabled(
            ContentRatingSystem contentRatingSystem, Rating rating, SubRating subRating) {
        return mRatings.contains(toTvContentRating(contentRatingSystem, rating, subRating));
    }

    private boolean setRatingBlockedInternal(
            ContentRatingSystem contentRatingSystem,
            Rating rating,
            SubRating subRating,
            boolean blocked) {
        TvContentRating tvContentRating =
                (subRating == null)
                        ? toTvContentRating(contentRatingSystem, rating)
                        : toTvContentRating(contentRatingSystem, rating, subRating);
        boolean changed;
        if (blocked) {
            changed = mRatings.add(tvContentRating);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "add rating block :"+tvContentRating.flattenToString());
            mTvInputManager.addBlockedRating(tvContentRating);
            if (ScanContent.isDVBSForTivusatOp()) {
                MtkTvChannelList.getInstance().channellistResetTmpUnlock(CommonIntegration.getInstance().getSvl(), 0, true, false);
            }
        } else {
            changed = mRatings.remove(tvContentRating);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "remove rating block :"+tvContentRating.flattenToString());
            mTvInputManager.removeBlockedRating(tvContentRating);
        }
        if (changed) {
            changeToCustomLevel();
        }
        return changed;
    }

    private void changeToCustomLevel() {
        if (getContentRatingLevel() != TvSettings.CONTENT_RATING_LEVEL_CUSTOM) {
            TvSettings.setContentRatingLevel(mContext, TvSettings.CONTENT_RATING_LEVEL_CUSTOM);
        }
    }

    /**
     * Returns the blocked status of a given rating. The status can be one of the followings: {@link
     * #RATING_BLOCKED}, {@link #RATING_BLOCKED_PARTIAL} and {@link #RATING_NOT_BLOCKED}
     */
    public int getBlockedStatus(ContentRatingSystem contentRatingSystem, Rating rating) {
        if (isRatingBlocked(contentRatingSystem, rating)) {
            return RATING_BLOCKED;
        }
        for (SubRating subRating : rating.getSubRatings()) {
            if (isSubRatingEnabled(contentRatingSystem, rating, subRating)) {
                return RATING_BLOCKED_PARTIAL;
            }
        }
        return RATING_NOT_BLOCKED;
    }

    private TvContentRating toTvContentRating(
            ContentRatingSystem contentRatingSystem, Rating rating) {
        return TvContentRating.createRating(
                contentRatingSystem.getDomain(), contentRatingSystem.getName(), rating.getName());
    }

    private TvContentRating toTvContentRating(
            ContentRatingSystem contentRatingSystem, Rating rating, SubRating subRating) {
        return TvContentRating.createRating(
                contentRatingSystem.getDomain(),
                contentRatingSystem.getName(),
                rating.getName(),
                subRating.getName());
    }
    
    public void setRelativeRatingsEnabled(ContentRatingSystem contentRatingSystem,Rating selectRating,boolean enabled){
    	List<Order> orders=contentRatingSystem.getOrders();
    	if(orders == null||orders.isEmpty()){
    		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "orders==null or orders.size<=0");
			return;
		}
    	Order order=orders.get(0);
    	for(Rating rating:contentRatingSystem.getRatings()){
    		int selectedRatingOrderIndex=order.getRatingIndex(selectRating);
    		int ratingOrderIndex=order.getRatingIndex(rating);
    		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectedRatingOrderIndex="+selectedRatingOrderIndex+",ratingOrderIndex="+ratingOrderIndex);
    		if (ratingOrderIndex != -1&&selectedRatingOrderIndex!=-1
                    && ((ratingOrderIndex > selectedRatingOrderIndex && enabled)
                            || (ratingOrderIndex < selectedRatingOrderIndex && !enabled))) {
    			setRatingBlocked(contentRatingSystem, rating, enabled);
    			setRelativesetSubRatingEnabled(contentRatingSystem,enabled,rating);
            }
    	}
    }
    
    private void setRelativesetSubRatingEnabled(ContentRatingSystem contentRatingSystem,boolean enabled,Rating rating){
    	for(SubRating subRating:rating.getSubRatings()){
    		setSubRatingBlocked(contentRatingSystem, rating, subRating, enabled);
    	}
    }

   //add start by jg_jianwang for DTV01081110
    public void setRelativeRating2SubRatingEnabled(ContentRatingSystem contentRatingSystem,boolean enabled,Rating relativeRating,SubRating subRating){
        List<Order> orders=contentRatingSystem.getOrders();
        if(orders == null||orders.isEmpty()){
            return;
        }
        Order order=orders.get(0);
        for(Rating rating:contentRatingSystem.getRatings()){
            int selectedRatingOrderIndex=order.getRatingIndex(relativeRating);
            int ratingOrderIndex=order.getRatingIndex(rating);
            if (ratingOrderIndex != -1&&selectedRatingOrderIndex!=-1
                    && ((ratingOrderIndex > selectedRatingOrderIndex && enabled)
                            || (ratingOrderIndex < selectedRatingOrderIndex && !enabled))) {
                List<SubRating> subRatingslist = rating.getSubRatings();
                if(subRatingslist.contains(subRating)){
                    SubRating relativeSub = subRatingslist.get(subRatingslist.indexOf(subRating));
                    if(isSubRatingEnabled(contentRatingSystem,rating,relativeSub) == enabled){
                        continue;
                    }
                    setSubRatingBlocked(contentRatingSystem, rating, relativeSub, enabled);
                }
            }
        }
    }
    //add end by jg_jianwang for DTV01081110
}
