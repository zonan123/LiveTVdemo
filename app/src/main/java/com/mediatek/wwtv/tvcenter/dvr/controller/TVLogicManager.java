package com.mediatek.wwtv.tvcenter.dvr.controller;

import android.content.Context;
//import android.graphics.Rect;
//import android.graphics.drawable.Drawable;
import android.view.View;

import com.mediatek.twoworlds.tv.MtkTvBroadcast;
//import com.mediatek.twoworlds.tv.MtkTvChannelList;
//import com.mediatek.twoworlds.tv.MtkTvInputSource;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.model.MtkTvATSCChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;
//import com.mediatek.twoworlds.tv.model.MtkTvChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvISDBChannelInfo;
//import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
//import com.mediatek.wwtv.tvcenter.nav.fav.TVChannel;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.IntegrationZoom;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.TwinkleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.UKChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.ZoomTipView;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
//import com.mediatek.wwtv.tvcenter.dvr.manager.Util;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

//import java.util.ArrayList;
import java.util.List;
//import java.util.Locale;

/**
 *
 */
public class TVLogicManager {
	private static TVLogicManager instance;

	// PIP/POP mode
	public static final int MODE_NORMAL = 0;
	public static final int MODE_PIP = 1;
	public static final int MODE_POP = 2;

	private final MtkTvBroadcast mChannelBroadcast;

    public TVLogicManager(Context context, DvrManager topManager) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVLogicManager", "" + context + topManager);

		mChannelBroadcast = MtkTvBroadcast.getInstance();
	}

//	public synchronized static TVLogicManager getInstance(Context context,
//            DvrManager topManager) {
//		if (instance == null) {
//			instance = new TVLogicManager(context, topManager);
//		}
//		return instance;
//	}

	public static TVLogicManager getInstance() {
		if (instance != null) {
			return instance;
		}
		return null;
	}

	public int getCurrentMode() {
		// return mTVCMManager.getCurrentOutputMode();
		return MODE_NORMAL;
	}

	public boolean isScanning() {
		// return MenuMain.mScanningStatus;
		return false;
	}

	public boolean isPipPopMode() {
		return CommonIntegration.getInstance().isPipOrPopState();
	}

	public boolean isDTV() {
		return false;// mTVCMManager.getCurrentInputSource().equalsIgnoreCase("DTV");
	}

	public boolean isCurrentChannelDTV() {
		// TVChannel mChannel = mTvChannelSelector.getCurrentChannel();
		//
		// if (mChannel != null) {
		// return mChannel.isDTV();
		// }
		return false;
	}

	public boolean dtvNotReadyForRecord() {

	/*	boolean notReady = !isDTV() || !hasVideo() || (!dtvHasSignal())
				|| (!hasDTVSignal()) || (dtvIsScrambled())
				|| (dtvIsScrambled());*/

		return !isDTV() || !hasVideo() || (!dtvHasSignal())
				|| (!hasDTVSignal()) || (dtvIsScrambled())
				|| (dtvIsScrambled());
	}

	public List<MtkTvChannelInfoBase> getChannels() {
		return CommonIntegration.getInstance().getChannelList(
				CommonIntegration.getInstance().getCurrentChannelId(), 7, 7,
				MtkTvChCommonBase.SB_VNET_ALL);
	}

	public List<MtkTvChannelInfoBase> getChannelList() {

		int length = CommonIntegration.getInstance().getChannelAllNumByAPI();
	/*	List<MtkTvChannelInfoBase> channelBaseList = CommonIntegration
				.getInstance().getChList(0, 0, length);*/

		return CommonIntegration.getInstance().getChList(0, 0, length);
	}

	public String getChannelNumber(MtkTvChannelInfoBase channel) {

		MtkTvChannelInfoBase mCurrentChannel = channel;
		String channelNumber = "";
		if (mCurrentChannel instanceof MtkTvATSCChannelInfo) {
			MtkTvATSCChannelInfo tmpAtsc = (MtkTvATSCChannelInfo) mCurrentChannel;
			channelNumber = (tmpAtsc.getMajorNum() + "." + tmpAtsc
					.getMinorNum());

		} else if (mCurrentChannel instanceof MtkTvISDBChannelInfo) {
			MtkTvISDBChannelInfo tmpIsdb = (MtkTvISDBChannelInfo) mCurrentChannel;
			channelNumber = (tmpIsdb.getMajorNum() + "." + tmpIsdb
					.getMinorNum());

		} else if (mCurrentChannel instanceof MtkTvAnalogChannelInfo
				&& MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA) {
			channelNumber = ("" + mCurrentChannel.getChannelNumber());
		} else {
			channelNumber = ("" + mCurrentChannel.getChannelNumber());
		}

		return channelNumber;
	}

	public MtkTvChannelInfoBase getCurrentChannel() {
		return CommonIntegration.getInstance().getCurChInfo();
	}

	public String getChannelName() {

		return getCurrentChannel()!=null?getCurrentChannel().getServiceName():"";
	}

	public int getChannelNumInt() {

		return getCurrentChannel()!=null?getCurrentChannel().getChannelNumber():0;
	}

	public String getChannelNumStr() {
            String  channelNumber="";
            if (getCurrentChannel() instanceof MtkTvATSCChannelInfo) {
                    MtkTvATSCChannelInfo tmpAtsc = (MtkTvATSCChannelInfo) getCurrentChannel();
                    channelNumber = "CH" +(tmpAtsc.getMajorNum() + "-" + tmpAtsc
                        .getMinorNum());
            } else if (getCurrentChannel() instanceof MtkTvISDBChannelInfo) {
                    MtkTvISDBChannelInfo tmpIsdb = (MtkTvISDBChannelInfo) getCurrentChannel();
                    channelNumber = "CH" +(tmpIsdb.getMajorNum() + "." + tmpIsdb
                        .getMinorNum());
            }else {
                    channelNumber = "CH" + getChannelNumInt();
            }
            return  channelNumber;
	}

	private boolean dtvIsScrambled() {
		boolean hasSignal = false;
		hasSignal = isAudioScrambled() || isVideoScrambled();

		return hasSignal;
	}

	private boolean dtvHasSignal() {
	/*	boolean hasSignal = false;*/
		return false;
	}

//	private String getScrambleState() {
//		return null;// mTVcontent.getChannelSelector().getScrambleState();
//	}

	private boolean isAudioScrambled() {
//		String state = getScrambleState();
		// if (state.equals(NavIntegration.STATE_AUDIO_AND_VIDEO_SCRAMBLED)
		// || state.equals(NavIntegration.STATE_AUDIO_CLEAR_VIDEO_SCRAMBLED)) {
		// return true;
		// }
		return false;
	}

	private boolean isVideoScrambled() {
//		String state = getScrambleState();
		return false;
	}

	public boolean hasDTVSignal() {
		/*boolean hasSignal = false;*/

		return false;
	}

	private boolean hasVideo() {
	    return true;
	}

	public boolean prepareScheduleTask(MtkTvBookingBase item) {
		if (item == null) {
			return false;
		}
		try {
			changeDTVSource();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void changeDTVSource() {
		CommonIntegration.getInstance().iSetSourcetoTv();
	}

	public void resumeTV() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("TVLogicManager", "resumeTv" );
	}

	public void selectChannel(int keyCode) {
		if (keyCode == KeyMap.KEYCODE_MTKIR_CHDN) {
			CommonIntegration.getInstance().channelDown();
		} else {
			CommonIntegration.getInstance().channelUp();
		}

	}

	public void selectChannelByNum(int channelNum) {
		mChannelBroadcast.channelSelectByChannelNumber(channelNum);
	}
	public void removeZoomTips(){
		ZoomTipView mZoomTip = ((ZoomTipView) ComponentsManager.getInstance()
				.getComponentById(NavBasic.NAV_COMP_ID_ZOOM_PAN));
		mZoomTip.setVisibility(View.GONE);
	}
	public void removeTwinkView(){
		TwinkleDialog.hideTwinkle();
	}

	public void reSetZoomValues(Context mContext){
		IntegrationZoom.getInstance(mContext).setZoomMode(
				IntegrationZoom.ZOOM_1);
		removeZoomTips();
	}

	public void removeChannelList(Context mContext){
	    NavBasicDialog channelListDialog = (NavBasicDialog) ComponentsManager
                .getInstance().getComponentById(
                        NavBasicMisc.NAV_COMP_ID_CH_LIST);
	   boolean channellistd=false;
        if (channelListDialog instanceof ChannelListDialog) {
//            ChannelListDialog dialog = (ChannelListDialog) channelListDialog;
            channellistd = channelListDialog != null
                    && channelListDialog.isShowing();
        } else if (channelListDialog instanceof UKChannelListDialog) {
//            UKChannelListDialog dialog = (UKChannelListDialog) channelListDialog;
            channellistd = channelListDialog != null
                    && channelListDialog.isShowing();
        }
        if(channellistd){
            channelListDialog.dismiss();
        }
	}
}