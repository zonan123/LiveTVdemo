package com.android.tv.menu;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;

import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import android.content.Context;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChannelsRowAdapterNew extends OptionsRowAdapter implements
		AccessibilityStateChangeListener {
	private static final String TAG = "ChannelsRowAdapterNew";
	private final AccessibilityManager accessibilityManager;
	private Context context;
	private List<MenuAction> actionList = new ArrayList<MenuAction>();

	private Menu mMenu;
	private boolean mShowChannelUpDown;

	public ChannelsRowAdapterNew(Context context, Menu menu) {
		super(context);
		this.context = context;
		mMenu = menu;

		setHasStableIds(true);
		accessibilityManager = context
				.getSystemService(AccessibilityManager.class);
		mShowChannelUpDown = accessibilityManager.isEnabled();

	}

	@Override
	public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
		Log.d(TAG, "onAttachedToRecyclerView");
		super.onAttachedToRecyclerView(recyclerView);
		accessibilityManager.addAccessibilityStateChangeListener(this);
	}

	@Override
	public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
		Log.d(TAG, "onDetachedFromRecyclerView");
		super.onDetachedFromRecyclerView(recyclerView);
		accessibilityManager.removeAccessibilityStateChangeListener(this);
	}

	@Override
	protected List<MenuAction> createActions() {
		
		/*Log.d(TAG, "createActions||isFavoriteItemShow =" + isFavoriteItemShow());
		if (isFavoriteItemShow()) {
			boolean isFav = com.mediatek.wwtv.tvcenter.nav.fav.FavChannelManager
					.getInstance(context).isFavChannel();
			if (!isFav) {
				actionList.add(MenuAction.MY_FAVORITE_ACTION);
			} else {
				actionList.add(MenuAction.MY_FAVORITE_ACTION_SELECTED);
			}
			setOptionChangedListener(MenuAction.MY_FAVORITE_ACTION);
		}*/
		
		Log.d(TAG, "createItems||isGuideItemShow =" + isGuideItemShow());
		if (isGuideItemShow()) {
			actionList.add(MenuAction.PROGRAM_GUIDE_ACTION);
			setOptionChangedListener(MenuAction.PROGRAM_GUIDE_ACTION);
		}
		if (mShowChannelUpDown) {
			actionList.add(MenuAction.CHANNEL_UP_ACTION);
			actionList.add(MenuAction.CHANNEL_DOWN_ACTION);
			setOptionChangedListener(MenuAction.CHANNEL_UP_ACTION);
		}
		
		Log.d(TAG, "createItems||isSetupItemShow =" + isSetupItemShow());
		if (isSetupItemShow()) {
			actionList.add(MenuAction.NEW_CHANNELS_ACTION);
		}
		
		Log.d(TAG, "createItems||isAppLinkShow =" + isAppLinkShow());
		if (isAppLinkShow()) {
			MenuAction.APP_LINK_ACTION.setChannelInfo(TIFChannelManager
					.getInstance(context).getChannelInfoByUri());
			actionList.add(MenuAction.APP_LINK_ACTION);
			setOptionChangedListener(MenuAction.APP_LINK_ACTION);
		}

		actionList.add(MenuAction.CHANNEL);
		setOptionChangedListener(MenuAction.CHANNEL);

		return actionList;
	}
	
	@Override
	protected boolean updateActions() {
		int currentIndex = 0;
		/*boolean isFavUnselected = false;
		boolean isFavSelected   = false;
		
		boolean isFav = com.mediatek.wwtv.tvcenter.nav.fav.FavChannelManager
				.getInstance(context).isFavChannel();
		Log.d(TAG, "updateActions||isFavoriteItemShow =" + isFavoriteItemShow()
				+ "||isFav = " + isFav);
		if(!isFavoriteItemShow()){
			for (int i=0;i<actionList.size();i++){
				if(actionList.get(i).equals(MenuAction.MY_FAVORITE_ACTION)||
					actionList.get(i).equals(MenuAction.MY_FAVORITE_ACTION_SELECTED)){
					actionList.remove(i);
				}
			}
		} else {
			if (!isFav) {
				isFavUnselected = updateItem(isFavoriteItemShow(),MenuAction.MY_FAVORITE_ACTION, currentIndex);
			} else {
				isFavSelected = updateItem(isFavoriteItemShow(),MenuAction.MY_FAVORITE_ACTION_SELECTED, currentIndex);
			}
			if (isFavUnselected || isFavSelected) {
				currentIndex ++;
			}
		}*/


		Log.d(TAG, "updateActions||isGuideItemShow =" + isGuideItemShow());
		if (updateItem(isGuideItemShow(), MenuAction.PROGRAM_GUIDE_ACTION, currentIndex)) {
			currentIndex ++;
		}


		if(updateItem(mShowChannelUpDown,MenuAction.CHANNEL_UP_ACTION,currentIndex)){
			currentIndex++;
		}

		if(updateItem(mShowChannelUpDown,MenuAction.CHANNEL_DOWN_ACTION,currentIndex)){
			currentIndex++;
		}

		Log.d(TAG, "updateActions||isSetupItemShow =" + isSetupItemShow());
		if (updateItem(isSetupItemShow(), MenuAction.NEW_CHANNELS_ACTION, currentIndex)) {
			currentIndex ++;
		}
		
		Log.d(TAG, "updateActions||isAppLinkShow =" + isAppLinkShow());
		Log.d(TAG, "getItemList||isAppLinkShow =" + currentIndex+"----"+getItemList().size());
		if (updateItem(isAppLinkShow(), MenuAction.APP_LINK_ACTION, currentIndex)) {
			if(getItemList().size() > currentIndex) {
				getItemList().get(currentIndex).setChannelInfo(
						TIFChannelManager.getInstance(context)
								.getChannelInfoByUri());
			}
			currentIndex ++;
		}
		Log.d(TAG, "updateActions||currentIndex =" + currentIndex);
		notifyDataSetChanged();
		return true;
	}

	@Override
	protected void executeAction(int type) {
		Log.d(TAG, "executeActionType =" + type);
		switch (type) {
		/*case MenuOptionMain.OPTION_MY_FAVORITE:
			onFavoriteClicked();
			break;*/
		case MenuOptionMain.OPTION_PROGRAM_GUIDE:
			MenuAction.onProgramGuideClicked(getMainActivity());
			break;
		case MenuOptionMain.OPTION_CHANNEL_UP:
			MenuAction.onChannelUpClicked();
			break;
		case MenuOptionMain.OPTION_CHANNEL_DOWN:
			MenuAction.onChannelDownClicked();
			break;
		case MenuOptionMain.OPTION_NEW_CHANNELS:
			MenuAction.onNewChannelsClicked(getMainActivity());
			break;
		case MenuOptionMain.OPTION_APP_LINK:
			MenuAction.onAppLinkClicked(getMainActivity(),
					MenuAction.APP_LINK_ACTION.getChannelInfo());
			break;
		case MenuOptionMain.OPTION_CHANNEL:
			MenuAction.onChannelClicked(getMainActivity());
			break;
		default:
			break;
		}
	}

	@Override
	public void onAccessibilityStateChanged(boolean arg0) {
		Log.d(TAG, "onStateChanged||arg0 =" + arg0);
		mShowChannelUpDown = arg0;
		if (getItemCount() == 0) {
			createActions();
		} else {
			updateActions();
		}
	}

	/*private boolean isFavoriteItemShow() {
		CommonIntegration ci = TvSingletons.getSingletons()
				.getCommonIntegration();
		return ci.isCurrentSourceTv()
				&& TIFChannelManager.getInstance(context).hasActiveChannel()
				&& !ci.isCurrentSourceBlockEx() && !ci.is3rdTVSource();
	}*/

	private boolean isGuideItemShow() {
		if (StateDvrPlayback.getInstance() != null
				&& StateDvrPlayback.getInstance().isRunning()) {
			return false;
		}
		boolean isEpgSupported = MarketRegionInfo
				.isFunctionSupport(MarketRegionInfo.F_EPG_SUPPORT);
		boolean isAtvOnly = DataSeparaterUtil.getInstance().isAtvOnly();
		Log.d(TAG, "isGuideItemShow||isEpgSupported =" + isEpgSupported
				+ "||isAtvOnly =" + isAtvOnly);
		return isEpgSupported && !isAtvOnly;
	}

	private boolean isSetupItemShow() {
		if(DataSeparaterUtil.getInstance().isGMSExisted()){
			return false;
		}
		CommonIntegration ci = TvSingletons.getSingletons()
				.getCommonIntegration();
		if (StateDvrPlayback.getInstance() != null
				&& StateDvrPlayback.getInstance().isRunning()) {
			return false;
		}
		return ci.isCurrentSourceTv() && !ci.isMenuInputTvBlock();
	}

	private boolean isAppLinkShow() {
		TIFChannelInfo currentChannel = TIFChannelManager.getInstance(context)
				.getChannelInfoByUri();
		if (currentChannel == null) {
			Log.d(TAG, "isAppLinkShow||currentChannel = null");
			return false;
		}

		if (currentChannel.getAppLinkType(context) == TIFChannelInfo.APP_LINK_TYPE_NONE) {
			Log.d(TAG, "isAppLinkShow||APP_LINK_TYPE_NONE");
			return false;
		}

		if (com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager
				.getInstance().getTvInputAppInfo(
						currentChannel.mInputServiceName) == null) {
			Log.d(TAG, "isAppLinkShow||mInputServiceName = null");
			return false;
		}
		if (StateDvrPlayback.getInstance() != null
				&& StateDvrPlayback.getInstance().isRunning()) {
			Log.d(TAG, "isAppLinkShow||pvr is running");
			return false;
		}
		return true;
	}
	
	private boolean updateItem(boolean needToShow, MenuAction item, int index) {
		boolean isItemInList = index < actionList.size()
				&& item.equals(actionList.get(index));

		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateItem------"+needToShow+"----"+index+"-----"+actionList.size()+"-------"+isItemInList);

		if (needToShow && !isItemInList) {
			
			/*if (item.equals(MenuAction.MY_FAVORITE_ACTION)
					||item.equals(MenuAction.MY_FAVORITE_ACTION_SELECTED)) {
				updateFavoriteIcon();
			} else {*/
				actionList.add(index, item);
				notifyItemInserted(index);
//			}
			
		} else if (!needToShow && isItemInList) {
			actionList.remove(index);
			notifyItemRemoved(index);
		} /*else if (needToShow && isItemInList) {
			*//*
			if (item.equals(MenuAction.MY_FAVORITE_ACTION)
					||item.equals(MenuAction.MY_FAVORITE_ACTION_SELECTED)) {
				updateFavoriteIcon();
			}*//*
		}*/
		return needToShow;
	}
	
	/*private void onFavoriteClicked() {
		com.mediatek.wwtv.tvcenter.nav.fav.FavChannelManager.getInstance(
				context).favAddOrErase();
		updateFavoriteIcon();
		if (mMenu != null) {
			mMenu.scheduleHide();
		}
	}*/

	/*private void updateFavoriteIcon() {
		boolean isFav = com.mediatek.wwtv.tvcenter.nav.fav.FavChannelManager
				.getInstance(context).isFavChannel();
		Log.d(TAG, "updateFavoriteIcon||isFav =" + isFav);
		if (isFav) {
			actionList.set(0, MenuAction.MY_FAVORITE_ACTION_SELECTED);
		} else {
			actionList.set(0, MenuAction.MY_FAVORITE_ACTION);
		}
		notifyItemChanged(0);
	}*/
}
