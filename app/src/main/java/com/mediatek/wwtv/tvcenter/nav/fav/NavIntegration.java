package com.mediatek.wwtv.tvcenter.nav.fav;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.twoworlds.tv.MtkTvChannelList;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvInputSource;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvFavoritelistInfoBase;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;


import android.content.Context;

public class NavIntegration {

	private static final int _MAX_FAV_SIZE = 7;
	public static final int CHANNEL_UP = 0;
	public static final int CHANNEL_DOWN = 1;
	public static final int CHANNEL_PRE = 2;
	public static final int SUBTITTLE_LEN = 3;
	public static NavIntegration mNavIntegration;
	Context mContext;
	public NavIntegration(Context context) {
		// TODO Auto-generated constructor stub
	    mContext=context;
	}

	public static synchronized NavIntegration getInstance(Context context) {
		if (mNavIntegration == null) {
			mNavIntegration = new NavIntegration(context);
		}
		return mNavIntegration;
	}

	public boolean isCurrentSourceTv() {
		String srcName = MtkTvInputSource.getInstance()
				.getCurrentInputSourceName();
		return (srcName != null && (srcName.equalsIgnoreCase(MtkTvInputSource.INPUT_TYPE_TV)
				|| srcName.equalsIgnoreCase(MtkTvInputSource.INPUT_TYPE_ATV)
				|| srcName.equalsIgnoreCase(MtkTvInputSource.INPUT_TYPE_DTV))) ;
	}

	public int getChannelLength() {
		int totalChannelLength = 0;
		totalChannelLength = CommonIntegration.getInstance().getChannelActiveNumByAPI();
		return totalChannelLength;
	}

	public TVChannel iGetCurrentChannel() {
		TVChannel channel = new TVChannel();

		int chId = CommonIntegration.getInstance().getCurrentChannelId();// chBroadCast.getCurrentChannelId();
		MtkTvChannelInfoBase currentChannel = CommonIntegration.getInstance()
				.getChannelById(chId);
        if(currentChannel != null){
		try {
			channel.setChannelNum(String.valueOf(currentChannel
					.getChannelNumber()));
			channel.setChannelID(currentChannel.getChannelId());
			channel.setFreq(currentChannel.getFrequency());

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
        }
		List<TVChannel> favList = getFavoriteList();
		if (favList.contains(channel)) {
			channel.setFavorite(true);
		} else {
			channel.setFavorite(false);
		}
		return channel;
	}

	public void isSetChannelFavorite(Object iGetCurrentChannel) {
		if (!CommonIntegration.getInstance().isFavListFull()) {
			MtkTvChannelList.getInstance().addFavoritelistChannel();
		}

	}

	public List<TVChannel> getFavoriteList() {
		List<TVChannel> favChannelList = new ArrayList<TVChannel>();

		List<MtkTvFavoritelistInfoBase> list = MtkTvChannelList.getInstance()
				.getFavoritelistByFilter();

		if (list != null && !list. isEmpty()) {
			for (MtkTvFavoritelistInfoBase mMtkTvFavoritelistInfoBase:list) {
				TVChannel channel = new TVChannel();

				if (mMtkTvFavoritelistInfoBase.getChannelId() != -1) {

					try {
						channel.setChannelName(mMtkTvFavoritelistInfoBase.getChannelName());
						channel.setChannelNum(mMtkTvFavoritelistInfoBase.getChannelNumber());
						channel.setChannelID(mMtkTvFavoritelistInfoBase.getChannelId());
						MtkTvChannelInfoBase mtkTvChannelInfo =CommonIntegration.getInstance().getChannelById(mMtkTvFavoritelistInfoBase.getChannelId());
						if(mtkTvChannelInfo != null){
							channel.setFreq(mtkTvChannelInfo.getFrequency());
						}
						channel.setFavorite(true);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				favChannelList.add(channel);
			}
		}

		return favChannelList;
	}

	public int getFavoriteChannelsCount() {
		int count = 0;

		List<MtkTvFavoritelistInfoBase> list = MtkTvChannelList.getInstance()
				.getFavoritelistByFilter();

		for (MtkTvFavoritelistInfoBase simpleChannel : list) {
			if (simpleChannel.getChannelId() != -1) {
				count++;
			}
		}
		return count;
	}

	public synchronized boolean changeChannelToNextFav() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();

		if (!CommonIntegration.getInstance().isCurrentSourceTv()) {
			return false;
		}

		int totalCount = getChannelLength();
		List<TVChannel> mfavChannelList = getFavoriteList();
		TVChannel currentChannel = iGetCurrentChannel();

		if ((getFavoriteChannelsCount() <= 0)
				|| ((getFavoriteChannelsCount() == 1) && (getFavoriteList()
						.indexOf(currentChannel) != -1)) || totalCount <= 0) {
			return false;
		}

		int index = getFavoriteList().indexOf(currentChannel);
		if (index == -1) {
			for (TVChannel tvChannel: mfavChannelList) {
				if (compareChannum(tvChannel, currentChannel)) {
					selectChannel(tvChannel); // default behavior
					return true;
				}
			}
			selectChannel(mfavChannelList.get(0)); // default behavior
			return true;
		} else {
			index++;
			if (index >= mfavChannelList.size()) {
				index = 0;
			}

			int count = 0;
			do {
				if (mfavChannelList.get(index).getChannelID() != -1) {
					selectChannel(mfavChannelList.get(index));
					return true;
				}
				index=(++index)%_MAX_FAV_SIZE;
				count++;
			} while (count < _MAX_FAV_SIZE);
		}
		return false;
	}

	private boolean compareChannum(TVChannel facChannel,
			TVChannel currentChannel) {
		String favChannelNum = facChannel.getChannelNum();
		String currentChannum = currentChannel.getChannelNum();

		if (favChannelNum == null || favChannelNum.equalsIgnoreCase("null")) {
			return false;
		}

		favChannelNum = favChannelNum.replaceAll("[*_-]", ".");
		favChannelNum = favChannelNum.replaceAll("[*_-]", ".");

		currentChannum = currentChannum.replaceAll("[*_-]", ".");
		currentChannum = currentChannum.replaceAll("[*_-]", ".");

		// return Float.valueOf(mfavChannelList.get(i).getChannelNum()) > Float
		// .valueOf(currentChannel.getChannelNum());

		return favChannelNum.compareTo(currentChannum) > 0 ? true : false;
	}

	public void selectFavChanUp() {
		selectFavChanUp(true);
	}

	public void selectFavChanUp(boolean favListOnScreen) {
		// TODO Auto-generated method stub
		List<TVChannel> favList = getFavoriteList();
		if (favList.isEmpty()) {
			return;
		}
		int index = favList.indexOf(iGetCurrentChannel());

		// index>=0,current channel is FavChannel.
		if (index >= 0 && index < favList.size() - 1) {
			selectChannel(favList.get(index + 1));
			return;
		} else if (index == favList.size() - 1) {
			return;
		}

		// MtkTvChannelInfoBase
		// base=CommonIntegration.getInstance().getCurChInfo();
		// int num=base.getChannelNumber();
		int num = Integer.valueOf(iGetCurrentChannel().getChannelNum());

		for (TVChannel tvChannel:favList) {
			if (num < Integer.valueOf(tvChannel.getChannelNum())) {
				selectChannel(tvChannel);
				return;
			}
		}
		selectChannel(favList.get(0));
	}

	public void selectFavChanDown() {
		// TODO Auto-generated method stub
		List<TVChannel> favList = getFavoriteList();
		int index = favList.indexOf(iGetCurrentChannel());
		index--;

		if (index >= 0 && index <= favList.size() - 1) {
			selectChannel(favList.get(index));
		} 
	}

	public void setChannel(int channelPre) {
		setSourcetoTv();
		// mTvChannelSelector.selectPrev();
	}

	private void setSourcetoTv() {
		// TODO Auto-generated method stub
		CommonIntegration.getInstance().iSetSourcetoTv();
	}

	public void selectChannel(TVChannel selectedChannel) {

		if (CommonIntegration.getInstance().isCurrentSourceTv()) {
			MtkTvChannelInfo channel = new MtkTvChannelInfo();
			channel.setChannelId(selectedChannel.getChannelID());

			CommonIntegration.getInstance().selectChannelById(
					selectedChannel.getChannelID());
		}
	}

	public void setChannelFavorite(FavoriteListListener listener,
			TVChannel currentChannel) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
		if (currentChannel != null && listener != null) {
			if (currentChannel.isFavorite()) {
				currentChannel.setFavorite(false);
				removeFavItemFromList(currentChannel, listener);
				return;
			} else {
				if (!CommonIntegration.getInstance().isFavListFull()) {
					MtkTvChannelList.getInstance().addFavoritelistChannel();
					currentChannel.setFavorite(true);
				}
			}
		}
	}

	public void addChannelFavorite(FavoriteListListener listener,
			TVChannel currentChannel, int index) {
		int originalIndex = getRawFavList().indexOf(currentChannel);
		if (originalIndex != -1 && originalIndex != index) {
	//		cleanFavChannel(originalIndex);

			// tmp
			MtkTvChannelList.getInstance().addFavoritelistChannelByIndex(index);
		}
	}

	public void addChannelFavorite(FavoriteListListener listener,
			TVChannel currentChannel) {
		List<TVChannel> channels = getRawFavList();

		int originalIndex = getRawFavList().indexOf(currentChannel);

		if (originalIndex == -1) {
			for (int i = 0; i < _MAX_FAV_SIZE; i++) {
				if (channels.get(i).getChannelID() == -1) {

					// tmp
					MtkTvChannelList.getInstance()
							.addFavoritelistChannelByIndex(i);
					return;
				}
			}

			// show fav list==Full!
		} 
	}

	/**
	 * After remove favlist item, must reload the favlist from MW again for
	 * refreshing MW data.
	 *
	 * @param currentChannel
	 */
	public void removeFavItemFromList(TVChannel currentChannel,
			FavoriteListListener listener) {
		int index = getFavoriteList().indexOf(currentChannel);
		if (index != -1) {
			List<MtkTvFavoritelistInfoBase> list = MtkTvChannelList
					.getInstance().getFavoritelistByFilter();
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getChannelId() == currentChannel.getChannelID()) {
					MtkTvChannelList.getInstance().removeFavoritelistChannel(i);
					break;
				}
			}
		}
		listener.updateFavoriteList();
	}

	/**
	 * After swap the items, must reload the favlist from MW again for
	 * refreshing MW data.
	 *
	 * @param currentChannel
	 */
	public void moveUP(TVChannel currentChannel, FavoriteListListener listener) {
		List<TVChannel> favChannels = getFavoriteList();
		int index = favChannels.indexOf(currentChannel);
		if (index <= 0) {
			return;
		}

		swapFavlist(index, index - 1);

		listener.updateFavoriteList();
	}

	/**
	 * After swap the items, must reload the favlist from MW again for
	 * refreshing MW data.
	 *
	 * @param currentChannel
	 */
	public void moveDown(TVChannel currentChannel, FavoriteListListener listener) {

		List<TVChannel> favChannels = getFavoriteList();
		int index = favChannels.indexOf(currentChannel);
		if (index >= favChannels.size() - 1) {
			return;
		}

		swapFavlist(index, index + 1);

		listener.updateFavoriteList();
	}

	private void swapFavlist(int index, int index2) {
		// TODO Auto-generated method stub
		int indexMin = Math.min(index, index2);
		int indexMax = Math.max(index, index2);

		List<TVChannel> favList = getFavoriteList();
		TVChannel channeMin = favList.get(indexMin);
		TVChannel channeMax = favList.get(indexMax);

		List<MtkTvFavoritelistInfoBase> list = MtkTvChannelList.getInstance()
				.getFavoritelistByFilter();

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getChannelId() == channeMin.getChannelID()) {
				index = i;
			}

			if (list.get(i).getChannelId() == channeMax.getChannelID()) {
				index2 = i;
			}
		}

		MtkTvChannelList.getInstance().swapFavoritelistByIndex(index, index2);
	}

	public List<TVChannel> getChannelList() {

		int length = CommonIntegration.getInstance().getChannelActiveNumByAPI();
		List<MtkTvChannelInfoBase> channelBaseList = CommonIntegration
				.getInstance().getChannelList(0, 0, length,
						MtkTvChCommonBase.SB_VNET_ALL);
		List<TVChannel> favChannelList = new ArrayList<TVChannel>();

		for (MtkTvChannelInfoBase channel : channelBaseList) {
			TVChannel favChannel = new TVChannel();
			try {
				favChannel.setChannelNum(String.valueOf(channel
						.getChannelNumber()));
				favChannel.setChannelID(channel.getChannelId());
				favChannel.setFreq(channel.getFrequency());

				favChannelList.add(favChannel);
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}

		return favChannelList;
	}

	public boolean dispatchKeyToTimeshift() {
		 int setUpTShiftValue = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.SETUP_SHIFTING_MODE);
         if(setUpTShiftValue != 0/*0 close*/){
             //SETUP_SHIFTING_MODE default close when SETUP_SHIFTING_MODE is opened, freeze function close.
             return true;
         }
		if (CommonIntegration.getInstance().isCurrentSourceATV()) {
			return false;
		}

		int value = MtkTvConfig.getInstance().getConfigValue(
				MtkTvConfigType.CFG_RECORD_REC_TSHIFT_MODE);
		return value != 0;
	}

	/**
	 * Is the current channel fav-channel?
	 *
	 * @return
	 */
/*	private boolean cleanFavChannel(int index) {

		return true;
	}*/

	List<TVChannel> getRawFavList() {
		// TODO Auto-generated method stub
		List<TVChannel> favChannelList = new ArrayList<TVChannel>();

		List<MtkTvFavoritelistInfoBase> list = MtkTvChannelList.getInstance()
				.getFavoritelistByFilter();

		if (list != null &&!list.isEmpty()) {
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
		while (favChannelList.size() < _MAX_FAV_SIZE) {
			TVChannel channel = new TVChannel();
			channel.setChannelID(-1);
			favChannelList.add(channel);
		}

		return favChannelList;
	}

}
