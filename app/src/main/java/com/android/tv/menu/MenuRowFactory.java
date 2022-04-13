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
import androidx.annotation.Nullable;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.android.tv.menu.customization.CustomAction;
import com.android.tv.menu.customization.TvCustomizationManager;

import java.util.List;

/**
 * A factory class to create menu rows.
 */
public class MenuRowFactory {
	private final Context mContext;
	private final TvCustomizationManager mTvCustomizationManager;

	/** A constructor. */
	public MenuRowFactory(Context context) {
		mContext = context;
		mTvCustomizationManager = new TvCustomizationManager(context);
		mTvCustomizationManager.initialize();
	}

	/** Creates an object corresponding to the given {@code key}. */
	@Nullable
	public MenuRow createMenuRow(Menu menu, Class<?> key) {
		if (ChannelsRow.class.equals(key)) {
			return new ChannelsRow(mContext, menu);
		} else if (TvOptionsRow.class.equals(key)) {
			return new TvOptionsRow(
					mContext,
					menu,
					mTvCustomizationManager
							.getCustomActions(TvCustomizationManager.ID_OPTIONS_ROW));
		} else if (RecordRow.class.equals(key)) {
			return new RecordRow(mContext, menu);
		}
		return null;
	}

	/** A menu row which represents the TV options row. */
	public static class TvOptionsRow extends ItemListRow {
		/** The ID of the row. */
		public static final String ID = TvOptionsRow.class.getName();

		private TvOptionsRow(Context context, Menu menu,
				List<CustomAction> customActions) {
			super(context, menu, R.string.menu_title_options,
					R.dimen.action_card_height, new TvOptionsRowAdapter(
							context, customActions));
		}

		@Override
		public void onStreamInfoChanged() {
			if (getMenu().isActive()) {
				update();
			}
		}

		@Override
		public String getTitle() {
			return mContext.getString(R.string.menu_title_options);
		}
	}

	/** A menu row which represents the partner row. */
	/*
	 * public static class PipOptionsRow extends ItemListRow { private final
	 * MainActivity mMainActivity;
	 * 
	 * private PipOptionsRow(Context context, Menu menu) { super(context, menu,
	 * R.string.menu_title_pip_options, R.dimen.action_card_height, new
	 * PipOptionsRowAdapter(context)); mMainActivity = (MainActivity) context; }
	 * 
	 * @Override public boolean isVisible() { // TODO: Remove the dependency on
	 * MainActivity. return super.isVisible() && mMainActivity.isPipEnabled(); }
	 * }
	 */

	/**
	 * A menu row which represents the partner row.
	 */
	/*
	 * public static class PartnerRow extends ItemListRow { private
	 * PartnerRow(Context context, Menu menu, String title, List<CustomAction>
	 * customActions) { super(context, menu, title, R.dimen.action_card_height,
	 * new PartnerOptionsRowAdapter(context, customActions)); } }
	 */

	/**
	 * A menu row which represents the DVR row.
	 */
	public static class RecordRow extends ItemListRow {
		/** The ID of the row. */
		public static final String ID = RecordRow.class.getName();

		private RecordRow(Context context, Menu menu) {
			super(context, menu, R.string.menu_arrays_Record,
					R.dimen.action_card_height,
					new RecordRowAdapter(context));

		}

		@Override
		public boolean isVisible() {
			// TODO Auto-generated method stub
			return super.isVisible()
					&& !(StateDvrPlayback.getInstance() != null 
					&& StateDvrPlayback.getInstance().isRunning()) &&
					(CommonIntegration.getInstance().isCurrentSourceTv()||
					CommonIntegration.getInstance().isCurrentSourceDTV()||
					CommonIntegration.getInstance().isCurrentSourceATV());
		}

		@Override
		public String getTitle() {
			return mContext.getString(R.string.menu_arrays_Record);
		}
	}
}