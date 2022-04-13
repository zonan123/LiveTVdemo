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

package com.android.tv.menu;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;
import android.util.Log;


import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.util.InstrumentationHandler;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import java.util.ArrayList;
import java.util.List;

/** An adapter of the Channels row. */
public class ChannelsRowAdapter extends
		ItemListRowView.ItemListAdapter<ChannelsRowItem> implements
		AccessibilityStateChangeListener {
	private static final String TAG = ChannelsRowAdapter.class.getSimpleName();

	private static final int[] BACKGROUND_IMAGES = { 
		R.drawable.ic_favorite_normal, R.drawable.ic_favorite_selected };
	private static final int[] FAVORITE_TIPS = {
		R.string.options_item_add_to_favorite_normal,
		R.string.options_item_add_to_favorite_selected };

	private final Context mContext;
	private final Menu mMenu;

	private boolean mShowChannelUpDown;
	private ImageView mFavoriteImageView = null;
	private TextView mFavoriteTextView = null;

	private TextView mGuideView = null;
	private TextView mNewChannelView = null;

	public ChannelsRowAdapter(Context context, Menu menu) {
		super(context);
		mContext = context;
		mMenu = menu;
		/*
		 * TvSingletons tvSingletons = TvSingletons.getSingletons(context);
		 * mTracker = tvSingletons.getTracker(); if
		 * (CommonFeatures.DVR.isEnabled(context)) { mDvrDataManager =
		 * tvSingletons.getDvrDataManager(); } else { mDvrDataManager = null; }
		 * mRecommender = recommender;
		 */
		setHasStableIds(true);
		AccessibilityManager accessibilityManager = context
				.getSystemService(AccessibilityManager.class);
		mShowChannelUpDown = accessibilityManager.isEnabled();
		accessibilityManager.addAccessibilityStateChangeListener(this);
	}

	@Override
	public int getItemViewType(int position) {
		return getItemList().get(position).getLayoutId();
	}

	@Override
	protected int getLayoutResId(int viewType) {
		return viewType;
	}

	@Override
	public long getItemId(int position) {
		return getItemList().get(position).getItemId();
	}

	@Override
    public void onBindViewHolder(MyViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        Log.d(TAG, "onBindViewHolder " + viewType);
        if (viewType == R.layout.menu_card_guide) {
            viewHolder.itemView.setOnClickListener(this::onGuideClicked);
            mGuideView = (TextView)viewHolder.itemView.findViewById(
                    R.id.menu_card_guide_text);
            mGuideView.setText(mContext.getString(R.string.channels_item_program_guide));
        } else if (viewType == R.layout.menu_card_up) {
            viewHolder.itemView.setOnClickListener(this::onChannelUpClicked);
        } else if (viewType == R.layout.menu_card_down) {
            viewHolder.itemView.setOnClickListener(this::onChannelDownClicked);
        } else if (viewType == R.layout.menu_card_setup) {
            viewHolder.itemView.setOnClickListener(this::onSetupClicked);
            mNewChannelView = (TextView)viewHolder.itemView.findViewById(
            		R.id.menu_card_new_channel_text);
            mNewChannelView.setText(R.string.channels_item_setup);
        } else if (viewType == R.layout.menu_card_app_link) {
            viewHolder.itemView.setOnClickListener(this::onAppLinkClicked);
        } else if (viewType == R.layout.menu_card_favorite_channels) {
            viewHolder.itemView.setOnClickListener(this::onFavoriteClicked);
            mFavoriteImageView = (ImageView)viewHolder.itemView.findViewById(
                    R.id.menu_card_favorite_image);

            mFavoriteTextView = (TextView)viewHolder.itemView.findViewById(
                    R.id.menu_card_favorite_text);

            updateFavoriteIcon();
        /*
        } else if (viewType == R.layout.menu_card_dvr) {
            viewHolder.itemView.setOnClickListener(this::onDvrClicked);
            SimpleCardView view = (SimpleCardView) viewHolder.itemView;
            view.setText(R.string.channels_item_dvr);
        */
        } else {
            viewHolder.itemView.setTag(getItemList().get(position).getChannel());
            viewHolder.itemView.setOnClickListener(this::onChannelClicked);
        }
        super.onBindViewHolder(viewHolder, position);
    }

	@Override
	public void update() {
		if (getItemCount() == 0) {
			createItems();
		} else {
			updateItems();
		}
	}

	private void onGuideClicked(View unused) {
		Log.d(TAG, "onGuideClicked " + unused);
		// mTracker.sendMenuClicked(R.string.channels_item_program_guide);
		if (DvrManager.getInstance().pvrIsRecording()) {
			ComponentsManager.getInstance().hideAllComponents();
			StateDvr.getInstance().onKeyDown(KeyMap.KEYCODE_MTKIR_GUIDE);
		} else {
			getMainActivity().showEPG();
		}
	}

	private void onChannelDownClicked(View unused) {
		Log.d(TAG, "onChannelDownClicked " + unused);
		InstrumentationHandler.getInstance().sendKeyDownUpSync(
				KeyEvent.KEYCODE_CHANNEL_DOWN);
	}

	private void onChannelUpClicked(View unused) {
		Log.d(TAG, "onChannelUpClicked " + unused);
		InstrumentationHandler.getInstance().sendKeyDownUpSync(
				KeyEvent.KEYCODE_CHANNEL_UP);
	}

	private void onSetupClicked(View unused) {
		Log.d(TAG, "onSetupClicked " + unused);
		if (SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(
				MenuConfigManager.TIMESHIFT_START)) {
			Toast.makeText(mContext, mContext.getResources().getString(R.string.warning_time_shifting_recording), Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (DvrManager.getInstance().pvrIsRecording()) {
			Toast.makeText(
					mContext,
					mContext.getResources().getString(
							R.string.title_pvr_running), Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (StateDvrPlayback.getInstance() != null
				&& StateDvrPlayback.getInstance().isRunning()) {
			Toast.makeText(mContext, "pvrplayback is running !",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent(mContext,
				com.android.tv.onboarding.SetupSourceActivity.class);

		getMainActivity().startActivity(intent);
	}

	private void onFavoriteClicked(View unused) {
		Log.d(TAG, "onGuideClicked " + unused);
		TVAsyncExecutor.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				com.mediatek.wwtv.tvcenter.nav.fav.FavChannelManager
						.getInstance(mContext).favAddOrErase();
				// update UI
				getMainActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updateFavoriteIcon();
					}
				});

				if (mMenu != null) {
					mMenu.scheduleHide();
				}
			}
		});
	}

	private void updateFavoriteIcon() {
		boolean isFav = com.mediatek.wwtv.tvcenter.nav.fav.FavChannelManager
				.getInstance(mContext).isFavChannel();
		Log.d(TAG, "updateFavoriteIcon " + isFav);

		if (mFavoriteImageView != null) {
			mFavoriteImageView.setImageDrawable(mContext.getResources()
					.getDrawable(BACKGROUND_IMAGES[isFav ? 1 : 0]));
		}

		if (mFavoriteTextView != null) {
			mFavoriteTextView.setText(FAVORITE_TIPS[isFav ? 1 : 0]);
		}
	}

	private void onAppLinkClicked(View view) {
		// mTracker.sendMenuClicked(R.string.channels_item_app_link);
		Intent intent = ((AppLinkCardView) view).getIntent();
		if (intent != null) {
			getMainActivity().startActivitySafe(intent);
		}
	}

	private void onChannelClicked(View view) {
		// Always send the label "Channels" because the channel ID or name or
		// number might be
		// sensitive.
		// mTracker.sendMenuClicked(R.string.menu_title_channels);
		try {
			TIFChannelInfo channel = (TIFChannelInfo) view.getTag();
			getMainActivity().getTvView().tune(
					channel.mInputServiceName,
					android.content.ContentUris.withAppendedId(
							android.media.tv.TvContract.Channels.CONTENT_URI,
							channel.mId));
			MenuOptionMain tvOptionsManager = getMainActivity()
					.getTvOptionsManager();
			if (tvOptionsManager != null) {
				tvOptionsManager.setVisibility(View.INVISIBLE);
			}
		} catch (Exception ex) {
		    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Exception msg:" + ex.getMessage());
		}
	}

	private void createItems() {
        List<ChannelsRowItem> items = new ArrayList<>();

        if(needToShowFavoriteItem()) {
            items.add(ChannelsRowItem.FAVORITE_ITEM);
        }

        if(needToShowGuideItem()) {
            items.add(ChannelsRowItem.GUIDE_ITEM);
        }
        if (mShowChannelUpDown) {
            items.add(ChannelsRowItem.UP_ITEM);
            items.add(ChannelsRowItem.DOWN_ITEM);
        }

        if (needToShowSetupItem()) {
            items.add(ChannelsRowItem.SETUP_ITEM);
        }

        if (needToShowAppLinkItem()) {
            ChannelsRowItem.APP_LINK_ITEM.setChannel(
                TIFChannelManager.getInstance(mContext).getChannelInfoByUri());
            items.add(ChannelsRowItem.APP_LINK_ITEM);
        }

        setItemList(items);
    }

	private void updateItems() {
		List<ChannelsRowItem> items = getItemList();

		int currentIndex = 0;
		if (updateItem(needToShowFavoriteItem(), ChannelsRowItem.FAVORITE_ITEM,
				currentIndex)) {
			updateFavoriteIcon();
			++currentIndex;
		}
		if (updateItem(needToShowGuideItem(), ChannelsRowItem.GUIDE_ITEM,
				currentIndex)) {
			++currentIndex;
		}
		if (updateItem(mShowChannelUpDown, ChannelsRowItem.UP_ITEM,
				currentIndex)) {
			++currentIndex;
		}
		if (updateItem(mShowChannelUpDown, ChannelsRowItem.DOWN_ITEM,
				currentIndex)) {
			++currentIndex;
		}
		if (updateItem(needToShowSetupItem(), ChannelsRowItem.SETUP_ITEM,
				currentIndex)) {
			++currentIndex;
		}

		if (updateItem(needToShowAppLinkItem(), ChannelsRowItem.APP_LINK_ITEM,
				currentIndex)) {
			getItemList().get(currentIndex).setChannel(
					TIFChannelManager.getInstance(mContext)
							.getChannelInfoByUri());
			notifyDataSetChanged();
			++currentIndex;
		}

		int numOldChannels = items.size() - currentIndex;
		if (numOldChannels > 0) {
			while (items.size() > currentIndex) {
				items.remove(items.size() - 1);
			}
			notifyItemRangeRemoved(currentIndex, numOldChannels);
		}

		int numNewChannels = items.size() - currentIndex;
		if (numNewChannels > 0) {
			notifyItemRangeInserted(currentIndex, numNewChannels);
		}
	}

	/** Returns {@code true} if the item should be shown. */
	private boolean updateItem(boolean needToShow, ChannelsRowItem item,
			int index) {
		List<ChannelsRowItem> items = getItemList();
		boolean isItemInList = index < items.size()
				&& item.equals(items.get(index));
		if (needToShow && !isItemInList) {
			items.add(index, item);
			notifyItemInserted(index);
		} else if (!needToShow && isItemInList) {
			items.remove(index);
			notifyItemRemoved(index);
		}
		return needToShow;
	}

	private boolean needToShowFavoriteItem() {
		CommonIntegration ci = TvSingletons.getSingletons()
				.getCommonIntegration();
		return (ci.isCurrentSourceTv() && TIFChannelManager.getInstance(mContext).hasActiveChannel()
				&& !ci.isCurrentSourceBlocked() && !ci.is3rdTVSource());
	}

	private boolean needToShowGuideItem() {
		if (StateDvrPlayback.getInstance() != null
				&& StateDvrPlayback.getInstance().isRunning()) {
			return false;
		}
		return MarketRegionInfo
				.isFunctionSupport(MarketRegionInfo.F_EPG_SUPPORT)
				&& !DataSeparaterUtil.getInstance().isAtvOnly();
	}

	private boolean needToShowSetupItem() {
		return !(StateDvrPlayback.getInstance() != null
				&& StateDvrPlayback.getInstance().isRunning());
	}

	private boolean needToShowAppLinkItem() {
		TIFChannelInfo currentChannel = TIFChannelManager.getInstance(mContext)
				.getChannelInfoByUri();

		if (currentChannel == null) {
			return false;
		}
		if (currentChannel.getAppLinkType(mContext) == TIFChannelInfo.APP_LINK_TYPE_NONE) {
			return false;
		}
		if (com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager
				.getInstance().getTvInputAppInfo(
						currentChannel.mInputServiceName) == null) {
			return false;
		}
		
		return !(StateDvrPlayback.getInstance() != null
				&& StateDvrPlayback.getInstance().isRunning());

		// ChannelsRowItem.APP_LINK_ITEM.setChannel(currentChannel);
	}

	@Override
	public void onAccessibilityStateChanged(boolean enabled) {
		mShowChannelUpDown = enabled;
		update();
	}
}
