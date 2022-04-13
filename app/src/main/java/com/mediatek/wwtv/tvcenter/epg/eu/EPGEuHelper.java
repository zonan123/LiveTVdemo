package com.mediatek.wwtv.tvcenter.epg.eu;

import android.os.Bundle;


import com.mediatek.wwtv.tvcenter.commonview.TvSurfaceView;
import com.mediatek.wwtv.tvcenter.epg.EPGChannelInfo;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.mediatek.twoworlds.tv.MtkTvHBBTVBase;
import com.mediatek.twoworlds.tv.common.MtkTvTISMsgBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;

public class EPGEuHelper {
	private static final String TAG = "EPGEuHelper";
	private MtkTvChannelInfoBase mCurChannel;
	private final Bundle mBundle = new Bundle();
	private MtkTvHBBTVBase mMtkTvHBBTVBase=new MtkTvHBBTVBase();
	
	public MtkTvChannelInfoBase getCurChannel(){
		return mCurChannel;
	}
	public void selectCHWithEnterEPG() {
		mMtkTvHBBTVBase.disableHbbtvByEPG();
		mCurChannel = CommonIntegration.getInstance()
				.getCurChInfo();
		boolean isBarkChannel=CommonIntegration.getInstance().isBarkChannel(mCurChannel);
		CommonIntegration.getInstance().setTurnCHOfExitEPG(isBarkChannel);
		if (isBarkChannel) {
			setBarkerChannel(true);
			int channelId=CommonIntegration.getInstance().getCurrentChannelId();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectCHWithEnterEPG>>>channelId=" + channelId);
			CommonIntegration.getInstance().selectChannelById(channelId);
		}
	}
	
	public boolean isBarkerChannel(){
		return CommonIntegration.getInstance().isBarkChannel(mCurChannel);
	}
	
	public void selectCHAfterExitEPG() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectCHAfterExitEPG start!");
		mMtkTvHBBTVBase.enableHbbtvByEPG();
//    	int chId = CommonIntegration.getInstance().getCurrentChannelId();
    	if (CommonIntegration.getInstance().isBarkChannel(mCurChannel)) {
			setBarkerChannel(false);
			CommonIntegration.getInstance().selectChannelById(mCurChannel.getChannelId());
    	}
    }
	
	private void setBarkerChannel(boolean isBarkerChannel) {
		if (isBarkerChannel) {
			mBundle.putByte(MtkTvTISMsgBase.MSG_CHANNEL_IS_BARKER_CHANNEL,
					MtkTvTISMsgBase.MTK_TIS_VALUE_TRUE);
		} else {
			mBundle.putByte(MtkTvTISMsgBase.MSG_CHANNEL_IS_BARKER_CHANNEL,
					MtkTvTISMsgBase.MTK_TIS_VALUE_FALSE);
		}
		TvSurfaceView tvView=TurnkeyUiMainActivity.getInstance().getTvView();
		if(tvView!=null){
		  tvView.sendAppPrivateCommand(MtkTvTISMsgBase.MTK_TIS_MSG_CHANNEL,
		      mBundle);
		}
	}
	
	
	public void turnChannel(EPGChannelInfo changeChannel) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "changeChannel=" + changeChannel);
        if(changeChannel == null){
            return;
        }
		mCurChannel = changeChannel.getTVChannel();
		boolean isBarkerChannel=CommonIntegration.getInstance().isBarkChannel(mCurChannel);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isBarkerChannel=" + isBarkerChannel);
		CommonIntegration.getInstance().setTurnCHOfExitEPG(isBarkerChannel);
		setBarkerChannel(isBarkerChannel);
		CommonIntegration.getInstance().selectChannelById((int) changeChannel.mId);
	}

}
