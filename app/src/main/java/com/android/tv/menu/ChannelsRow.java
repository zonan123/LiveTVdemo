package com.android.tv.menu;

import android.content.Context;

import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.R;

public class ChannelsRow extends ItemListRow{
	public static final String ID = ChannelsRow.class.getName();
	
    /** Minimum count for recent channels. */
    public static final int MIN_COUNT_FOR_RECENT_CHANNELS = 5;
    /** Maximum count for recent channels. */
    public static final int MAX_COUNT_FOR_RECENT_CHANNELS = 10;
    
	public ChannelsRow(Context context, Menu menu) {
		super(context, menu,R.string.menu_title_channels, 
				R.dimen.card_layout_height, new ChannelsRowAdapterNew(context, menu));
	}
	@Override
	public boolean isVisible() {
        return super.isVisible() && InputSourceManager.getInstance().isCurrentTvSource(CommonIntegration.getInstance().getCurrentFocus());
	}
	@Override
	public String getTitle() {
    	return mContext.getString(R.string.menu_title_channels);
	}
	@Override
	public String getId() {
		return ID;
	}
}
