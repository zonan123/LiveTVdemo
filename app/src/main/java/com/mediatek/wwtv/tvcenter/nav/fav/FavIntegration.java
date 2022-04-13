package com.mediatek.wwtv.tvcenter.nav.fav;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.twoworlds.tv.model.MtkTvFavoritelistInfoBase;
//import com.mediatek.wwtv.tvcenter.timeshift_pvr.manager.TimeShiftManager;
//import com.mediatek.wwtv.tvcenter.timeshift_pvr.ui.PvrDialog;


/**
 */
public class FavIntegration {
	public static FavIntegration mFavIntegration;
	public static Context mContext;

	public static synchronized FavIntegration getInstance(Context context) {
		if (mFavIntegration == null) {
			mFavIntegration = new FavIntegration();
		}
		mContext = context;

		return mFavIntegration;
	}

	private static final int MAX_FAV_SIZE = 7;

	/**
	 * Handle favList's Enter key.
	 * @param select
	 * @param listener
	 */
	public boolean enterKeyPress(int select, FavoriteListListener listener) {
		if (getSelectChannel(select).getChannelID() == -1) {

			clearChannel(getCurrentChannel());
			addCurrentChannel(select,listener);
			return false;
		}

		if (!getCurrentChannel().equals(getSelectChannel(select))) {
			changeChannel(getSelectChannel(select));
			return true;
		} else {
			removeCurrentChannel(listener);
		}
		return false;
	}

	/**
	 * Handle favList's add_erase key.
	 * @param select
	 * @param listener
	 */
	public void favKeyPress(int select, FavoriteListListener listener) {
		TVChannel channel=getCurrentChannel();
		if (getSelectChannel(select).getChannelID() == -1) {
			clearChannel(channel);
			addCurrentChannel(select,listener);
			return;
		}

		if (!channel.equals(getSelectChannel(select))) {
			clearChannel(channel);
			addCurrentChannel(select,listener);
			return;
		} else {
			removeCurrentChannel(listener);
		}
	}

	private void addCurrentChannel(int select, FavoriteListListener listener) {
		// TODO Auto-generated method stub

		//debug:
		MtkTvChannelList.getInstance().addFavoritelistChannelByIndex(select);
		MtkTvChannelList.getInstance().storeFavoritelistChannel();
		listener.updateFavoriteList();
	}

	/**
	 * Try to remove this item from DB,but won't notify Call Back.
	 *
	 * @param currentChannel
	 */
	private void clearChannel(TVChannel currentChannel) {
		// TODO Auto-generated method stub
		int index=getRawFavList().indexOf(currentChannel);
		 MtkTvChannelList.getInstance().removeFavoritelistChannel(index);
		MtkTvChannelList.getInstance().storeFavoritelistChannel();
	}

	/**
	 * Remove current channel from DB,will notify Call Back
	 *
	 * @param tvChannel
	 */
	private void removeCurrentChannel(FavoriteListListener listener) {
		// TODO Auto-generated method stub
		TVChannel tvChannel = this.getCurrentChannel();
		NavIntegration.getInstance(mContext).removeFavItemFromList(tvChannel,
				listener);
		MtkTvChannelList.getInstance().storeFavoritelistChannel();
	}

	private void changeChannel(TVChannel selectChannel) {
		// TODO Auto-generated method stub
//		if(TimeShiftManager.getInstance().pvrIsRecording()){
//			PvrDialog conDialog = new PvrDialog(TimeShiftManager.getInstance().getActivity(),PvrDialog.TYPE_Confirm,PvrDialog.KEYCODE_FROM_FAV,PvrDialog.TYPE_Record);
//      // conDialog.setFavChannel(selectChannel);
//			conDialog.show();
//			return;
//		}
		NavIntegration.getInstance(mContext).selectChannel(selectChannel);
	}

	/**
	 * Get current channel
	 *
	 * @return
	 */
	private TVChannel getCurrentChannel() {
		// TODO Auto-generated method stub
		return NavIntegration.getInstance(mContext).iGetCurrentChannel();
	}

	private TVChannel getSelectChannel(int select) {
		// TODO Auto-generated method stub
		return getRawFavList().get(select);
	}

	/**
	 * @return
	 */
	private List<TVChannel> getRawFavList() {
		// TODO Auto-generated method stub
		List<TVChannel> favChannelList = new ArrayList<TVChannel>();

		List<MtkTvFavoritelistInfoBase> list = MtkTvChannelList.getInstance()
				.getFavoritelistByFilter();

		if (list != null && !list.isEmpty()) {
			for (MtkTvFavoritelistInfoBase infoBase: list) {
				TVChannel channel = new TVChannel();

				if (infoBase.getChannelId() == -1) {
					channel.setChannelID(-1);
				} else {
					try {
						channel.setChannelName(infoBase.getChannelName());
						channel.setChannelNum(infoBase.getChannelNumber());
						channel.setChannelID(infoBase.getChannelId());
						channel.setFavorite(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				favChannelList.add(channel);
			}
		}
		while (favChannelList.size() < MAX_FAV_SIZE) {
			TVChannel channel = new TVChannel();
			channel.setChannelID(-1);
			favChannelList.add(channel);
		}

		return favChannelList;
	}
}
