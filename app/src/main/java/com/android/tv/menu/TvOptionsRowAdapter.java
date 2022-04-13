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
//import android.provider.Settings;
import android.util.Log;

import com.mediatek.twoworlds.tv.MtkTvConfig;
//import com.mediatek.twoworlds.tv.MtkTvAVMode;
//import com.mediatek.twoworlds.tv.MtkTvMHEG5;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.PartnerSettingsConfig;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrPlayback;
import com.mediatek.wwtv.tvcenter.epg.eu.EpgType;
import com.android.tv.menu.customization.CustomAction;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.setting.util.TVContent;
import java.util.ArrayList;
import java.util.List;
//import com.mediatek.wwtv.tvcenter.util.CommonUtil;
import com.mediatek.wwtv.setting.util.MenuConfigManager;


/*
 * An adapter of options.
 */
public class TvOptionsRowAdapter extends CustomizableOptionsRowAdapter {
//    private int mPositionPipAction;
//    private int mPositionCiAction;
    // If mInAppPipAction is false, system-wide PIP is used.
    //private boolean mInAppPipAction = true;
    //do not use ,do not remove ,its a private condition in picture format UI
    //private static final String TAG_IS3RD_SOURCE  = "is3rdSource";
    private static final String TAG = "TvOptionsRowAdapter";
    //private MtkTvMHEG5 mheg = MtkTvMHEG5.getInstance();

    public TvOptionsRowAdapter(Context context, List<CustomAction> customActions) {
        super(context, customActions);

       /* mInAppPipAction = context.getPackageManager().hasSystemFeature(
            android.content.pm.PackageManager.FEATURE_PICTURE_IN_PICTURE);*/
    }

    @Override
    protected List<MenuAction> createBaseActions() {
        List<MenuAction> actionList = new ArrayList<>();
        if (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SOURCE_KEY_SUPPORT)
        		||MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SOURCE_ITEM_SUPPORT)) {
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"F_SOURCE_ITEM_SUPPORT");
        	actionList.add(MenuAction.SELECT_SOURCE_ACTION);
            setOptionChangedListener(MenuAction.SELECT_SOURCE_ACTION);
        }

     /*   actionList.add(MenuAction.SELECT_AUTO_PICTURE_ACTION);
        setOptionChangedListener(MenuAction.SELECT_AUTO_PICTURE_ACTION);*/

       /* actionList.add(MenuAction.SOUND_STYLE);
        setOptionChangedListener(MenuAction.SOUND_STYLE);*/

        // Picture
        actionList.add(MenuAction.PICTURE);
        setOptionChangedListener(MenuAction.PICTURE);

        // Sound
            Log.d(TAG, "isSupportSoundSetting true");
            actionList.add(MenuAction.SOUND);
            setOptionChangedListener(MenuAction.SOUND);
        

      /*  actionList.add(MenuAction.SELECT_DISPLAY_MODE_ACTION);
        setOptionChangedListener(MenuAction.SELECT_DISPLAY_MODE_ACTION);*/

        if(MarketRegionInfo.REGION_US == MarketRegionInfo.getCurrentMarketRegion() ||
           MarketRegionInfo.REGION_SA == MarketRegionInfo.getCurrentMarketRegion() ||
           TVContent.getInstance(getMainActivity()).isCOLRegion()) {//cc default
            Log.d(TAG, "MarketRegionInfo.getCurrentMarketRegion() ="+MarketRegionInfo.getCurrentMarketRegion());
        } else {
            MenuAction.SELECT_CLOSED_CAPTION_ACTION.setActionNameResId(R.string.menu_setup_subtitle);
        }
        setOptionChangedListener(MenuAction.SELECT_CLOSED_CAPTION_ACTION);

        if(MarketRegionInfo.REGION_US == MarketRegionInfo.getCurrentMarketRegion()) {
            actionList.add(MenuAction.SELECT_AUDIO_LANGUAGE_ACTION);
            setOptionChangedListener(MenuAction.SELECT_AUDIO_LANGUAGE_ACTION);
        }

        /*if(mInAppPipAction) {
            actionList.add(MenuAction.PIP_IN_APP_ACTION);
            setOptionChangedListener(MenuAction.PIP_IN_APP_ACTION);
        }*/



        actionList.add(MenuAction.POWER_ACTION);
        setOptionChangedListener(MenuAction.POWER_ACTION);

        //CI: only cn & eu has cicard
        CommonIntegration ci = TvSingletons.getSingletons().getCommonIntegration();
        boolean isEUTV = ci.isEURegion();
        boolean isCNDTV = ci.isCNRegion();
//        boolean isFCI = MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_CI);
        boolean istwn = TVContent.getInstance(getMainActivity()).isOneImageTWN();
        boolean isTvSource = isCurrentSourceTv();
        boolean iscol  = TVContent.getInstance(getMainActivity()).isCOLRegion();
        Log.d(TAG, "createBaseActions||isEUTV =" + isEUTV + "||isCNDTV =" + isCNDTV + "||istwn =" + istwn+"||isatv ="+ci.isCurrentSourceATV());
        if (DataSeparaterUtil.getInstance() != null && DataSeparaterUtil.getInstance().isSupportCI()){
            if (isTvSource && !istwn&&!iscol  && ((isEUTV && !ci.isEUPARegion()) || isCNDTV 
            		|| ci.isEUPARegion()) && !ci.isCurrentSourceATV()) {
                actionList.add(MenuAction.BROADCAST_TV_CI_ACTION);
                setOptionChangedListener(MenuAction.BROADCAST_TV_CI_ACTION);
//                if(ci.isEUPARegion() && ci.isCurrentSourceATV()){
//                    MenuAction.setEnabled(MenuAction.BROADCAST_TV_CI_ACTION,false);
//                }
            }
        }

        //OAD
        if (MarketRegionInfo.isOadAvailable() && SaveValue.readLocalMemoryBooleanValue("is_show_OAD_ui")) {
            Log.d(TAG, "Oad is added");
            actionList.add(MenuAction.BROADCAST_TV_OAD_ACTION);
            setOptionChangedListener(MenuAction.BROADCAST_TV_OAD_ACTION);
        }

//        add ginga
        if (CommonIntegration.isSARegion() &&
                MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_GINGA)
                    && DataSeparaterUtil.getInstance() != null
                    && DataSeparaterUtil.getInstance().isSupportGinga()) {
        	 if(TVContent.getInstance(getMainActivity()).isSupportOneImage()){
              	if(!TVContent.getInstance(getMainActivity()).isOneImagePHL()){
              		 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"createBaseActions||add ginga one image");
              		 actionList.add(MenuAction.GINGA_SELECTION);
              		 setOptionChangedListener(MenuAction.GINGA_SELECTION);}
              	}else {
              		 com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"createBaseActions||add ginga");
              		 actionList.add(MenuAction.GINGA_SELECTION);
                      setOptionChangedListener(MenuAction.GINGA_SELECTION);
 				}
        }
        
        if(PartnerSettingsConfig.isMiscItemDisplay("menu_advanced_options")) {
            actionList.add(MenuAction.BROADCAST_TV_SETTINGS_ACTION);
            setOptionChangedListener(MenuAction.BROADCAST_TV_SETTINGS_ACTION);
        }

        actionList.add(MenuAction.SETTINGS_ACTION);
        setOptionChangedListener(MenuAction.SETTINGS_ACTION);

        return actionList;
    }

    @Override
    protected boolean updateActions() {
        boolean changed = false;
        // MStar Android Patch Begin
        /* Due to memory issues, we need to temporarily disable the PIP function. */
        /*
        if (updatePipAction()) {
            changed = true;
        }
        */
        // MStar Android Patch End
      /*
		if (updateSpeakersAction()) {
			changed = true;
		}
                */
        /*if(updateSoundStyleAction()){
            changed = true;
        }*/
        if (updateMultiAudioAction()) {
            changed = true;
        }
       /* if (updatePictureModeAction()) {
            changed = true;
        }*/
        if (updateCIMenuAction()){
            changed = true;
        }
        if(updateOADAction()){
            changed = true;
        }
        /*if(updateDisplayModeAction()){
            changed = true;
        }*/
        if(updateClosedCaptionAction()){
            changed = true;
        }
        if (updateGingaAction()) {
        	changed = true;
		}
        if(updateAdvancedOptions()){
            changed = true;
        }
        return changed;
    }
/*
    private boolean updatePipAction() {
        // There are four states.
        // Case 1. The device doesn't even have any input for PIP. (e.g. OTT box without HDMI input)
        //    => Remove the icon.
        // Case 2. The device has one or more inputs for PIP but none of them are currently
        // available.
        //    => Show the icon but disable it.
        // Case 3. The device has one or more available PIP inputs and now it's tuned off.
        //    => Show the icon with "Off".
        // Case 4. The device has one or more available PIP inputs but it's already turned on.
        //    => Show the icon with "On".

        boolean changed = false;

        // Case 1
        PipInputManager pipInputManager = getMainActivity().getPipInputManager();
        if (ENABLE_IN_APP_PIP && pipInputManager.getPipInputSize(false) > 1) {
            if (!mInAppPipAction) {
                removeAction(mPositionPipAction);
                addAction(mPositionPipAction, MenuAction.PIP_IN_APP_ACTION);
                mInAppPipAction = true;
                changed = true;
            }
        } else {
            if (mInAppPipAction) {
                removeAction(mPositionPipAction);
                mInAppPipAction = false;
                if (Features.PICTURE_IN_PICTURE.isEnabled(getMainActivity())) {
                    addAction(mPositionPipAction, MenuAction.SYSTEMWIDE_PIP_ACTION);
                }
                return true;
            }
            return false;
        }

        // Case 2
        boolean isPipEnabled = getMainActivity().isPipEnabled();
        boolean oldEnabled = MenuAction.PIP_IN_APP_ACTION.isEnabled();
        boolean newEnabled = pipInputManager.getPipInputSize(true) > 0;
        if (oldEnabled != newEnabled) {
            // Should not disable the item if the PIP is already turned on so that the user can
            // force exit it.
            if (newEnabled || !isPipEnabled) {
                MenuAction.PIP_IN_APP_ACTION.setEnabled(newEnabled);
                changed = true;
            }
        }

        // Case 3 & 4 - we just need to update the icon.
        MenuAction.PIP_IN_APP_ACTION.setDrawableResId(
                isPipEnabled ? R.drawable.ic_tvoption_pip : R.drawable.ic_tvoption_pip_off);
        return changed;
    }
*/
	/*private boolean updateSpeakersAction() {
		if (isSettingsMatch(Settings.Global.HDMI_CONTROL_ENABLED)
				|| isSettingsMatch(Settings.Global.HDMI_CONTROL_AUTO_DEVICE_OFF_ENABLED)
				|| isSettingsMatch(Settings.Global.HDMI_CONTROL_AUTO_WAKEUP_ENABLED)) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateSpeakersAction||enable");
			MenuAction.setEnabled(MenuAction.SELECT_SPEAKERS_ACTION, true);
		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateSpeakersAction||disable");
			MenuAction.setEnabled(MenuAction.SELECT_SPEAKERS_ACTION, false);
		}
		return true;
	}*/

	/*private boolean isSettingsMatch(String uri) {
		return Settings.Global.getInt(
				((Context) getMainActivity()).getContentResolver(), uri, 1) == 1;
	}*/
    
    private boolean updateClosedCaptionAction() {
		boolean mShow = false;

        if(CommonIntegration.getInstance().isCurrentSourceTv()||
            CommonIntegration.getInstance().isCurrentSourceATV()||
            CommonIntegration.getInstance().isCurrentSourceDTV() ||
            CommonIntegration.getInstance().isCurrentSourceComposite()) {

            if (MarketRegionInfo.REGION_EU == MarketRegionInfo
                    .getCurrentMarketRegion()
                    || DataSeparaterUtil.getInstance().isAtvOnly()) {
                if (CommonIntegration.getInstance().is3rdTVSource()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateClosedCaptionAction||3rdTVSource");
                    mShow = true;
                }
            } else {
                MenuAction.setEnabled(MenuAction.SELECT_CLOSED_CAPTION_ACTION,
                        !CommonIntegration.getInstance().isCurrentSourceHDMI());
                mShow = true;
            }
		/*if (MarketRegionInfo.REGION_SA == MarketRegionInfo
				.getCurrentMarketRegion()) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateClosedCaptionAction||REGION_SA");
			mShow = false;
		}*/
            if (TVContent.getInstance(getMainActivity()).isCOLRegion()) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateClosedCaptionAction||isCOLRegion");
                mShow = true;
            }
        }
        if (mShow && (DvrManager.getInstance().getState()) instanceof StateDvrPlayback &&
                DvrManager.getInstance().getState().isRunning()) {
            MenuAction.setEnabled(MenuAction.SELECT_CLOSED_CAPTION_ACTION,false);
        }

		// add it if not existed
		if (mShow && getActionIndex(MenuAction.SELECT_CLOSED_CAPTION_ACTION
						.getType()) < 0) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateClosedCaptionAction||addAction");
			addAction(getActionIndex(MenuAction.SOUND
							.getType()) + 1,
					MenuAction.SELECT_CLOSED_CAPTION_ACTION);
		// remove it if existed
		} else if (!mShow) {
			int index = getActionIndex(MenuAction.SELECT_CLOSED_CAPTION_ACTION
					.getType());
			if (index >= 0) {
				removeAction(index);
				notifyItemRemoved(index);
			}
		}

		return true;
	}

   /* boolean updateSoundStyleAction(){
        Log.d(TAG, "updateSoundStyleAction");
        if(CommonUtil.isSupportSoundStyle((Context) getMainActivity())){
            MenuAction.setEnabled(MenuAction.SOUND_STYLE,true);
        } else {
            MenuAction.setEnabled(MenuAction.SOUND_STYLE,false);
        }
        return true;
    }*/

    boolean updateMultiAudioAction() {
    	CommonIntegration ci = TvSingletons.getSingletons().getCommonIntegration();
    	if (ci != null && ci.isCurrentSourceTv()) {
    		Log.d(TAG, "updateMultiAudioAction||CurrentSourceTv");
    		MenuAction.setEnabled(MenuAction.SELECT_AUDIO_LANGUAGE_ACTION,true);
    	} else {
    		MenuAction.setEnabled(MenuAction.SELECT_AUDIO_LANGUAGE_ACTION,false);
		}
        return true;
    }

   /* private boolean updatePictureModeAction() {
    	TVContent tvContent = TVContent.getInstance((Context)getMainActivity());
    	boolean isLoss = tvContent.isSignalLoss();
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"updatePictureModeAction||isLoss =" + isLoss);
    	MenuAction.setEnabled(MenuAction.SELECT_AUTO_PICTURE_ACTION,!isLoss);
        return true;
    }*/

    private boolean updateCIMenuAction() {
        //CI: only cn & eu has cicard
    	boolean isShow = false;
        CommonIntegration ci = TvSingletons.getSingletons().getCommonIntegration();
        boolean isEUTV = ci.isEURegion() && ci.isCurrentSourceTv();
        boolean isCNDTV = ci.isCNRegion() && ci.isCurrentSourceDTV();
        boolean isFCI = DataSeparaterUtil.getInstance() != null 
        		&& DataSeparaterUtil.getInstance().isSupportCI();
        boolean istwn = TVContent.getInstance(getMainActivity()).isOneImageTWN();
        boolean isTvSource = isCurrentSourceTv();
        boolean iscol  = TVContent.getInstance(getMainActivity()).isCOLRegion();
        Log.d(TAG, "updateCIMenuAction||isEUTV =" + isEUTV + "||isCNDTV =" + isCNDTV + "||isFCI =" + isFCI+"||isatv ="+ci.isCurrentSourceATV());
        if (!iscol&&isTvSource && !istwn && isFCI && ((isEUTV && !ci.isEUPARegion()) || isCNDTV 
        		|| (ci.isEUPARegion() && ci.isCurrentSourceDTV())) && !ci.isCurrentSourceATV()){
        	isShow = true;
//            MenuAction.setEnabled(MenuAction.BROADCAST_TV_CI_ACTION,true);
        }else{
        	isShow = false;
//            MenuAction.setEnabled(MenuAction.BROADCAST_TV_CI_ACTION,false);
        }
        //add it if not existed
        Log.d(TAG, "updateCIMenuAction||isShow =" + isShow);
		if (isShow){
            if(getActionIndex(MenuAction.BROADCAST_TV_CI_ACTION.getType()) < 0){
                Log.d(TAG, "updateCIMenuAction||addAction");
                addAction(getActionIndex(MenuAction.POWER_ACTION.getType()) + 1,
                        MenuAction.BROADCAST_TV_CI_ACTION);
            }

		} else {
                int index = getActionIndex(MenuAction.BROADCAST_TV_CI_ACTION.getType());
                if (index >= 0) {
                    removeAction(index);
                    notifyItemRemoved(index);
                }
        }
        return true;
    }
    
	private boolean updateOADAction() {
		CommonIntegration ci = TvSingletons.getSingletons().getCommonIntegration();
		boolean isShow = false;
		boolean isFCI = MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_OAD);
		boolean isSupport = DataSeparaterUtil.getInstance() != null
				&& DataSeparaterUtil.getInstance().isSupportOAD();
		boolean isTvSource = isCurrentSourceTv();
		Log.d(TAG, "updateOADAction||isFCI =" + isFCI + "||isSupport ="
				+ isSupport + "||isTvSource =" + isTvSource);

		if (isFCI && isSupport && isTvSource && MarketRegionInfo.isOadAvailable()&& SaveValue.readLocalMemoryBooleanValue("is_show_OAD_ui")) {
			if (ci.isEURegion() || ci.isEUPARegion()) {
				if (!MtkTvConfig.getInstance().getCountry()
						.equals(EpgType.COUNTRY_UK)) {
					isShow = true;
				}
			}
		}

		if (ci.isPipOrPopState()
				|| ((DvrManager.getInstance().getState()) instanceof StateDvrPlayback 
						&& DvrManager.getInstance().getState().isRunning())
				|| null != StateDvr.getInstance()
						&& StateDvr.getInstance().isRecording()
				|| ci.is3rdTVSource()
                || isCurrentSourceATV()
                ||SaveValue.getInstance((Context)getMainActivity()).readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START)) {
			isShow = false;
		}
		Log.d(TAG, "updateOADAction||isShow =" + isShow);
		// add it if not existed
		if (isShow) {
			if (getActionIndex(MenuAction.BROADCAST_TV_OAD_ACTION.getType()) < 0) {
				int ciIndex = getActionIndex(MenuAction.BROADCAST_TV_CI_ACTION
						.getType());
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateOADAction||ciIndex =" + ciIndex);
				if (ciIndex < 0) {
					addAction(getActionIndex(MenuAction.POWER_ACTION.getType()) + 1,
							MenuAction.BROADCAST_TV_OAD_ACTION);
				} else {
					addAction(getActionIndex(MenuAction.BROADCAST_TV_CI_ACTION.getType()) + 1,
							MenuAction.BROADCAST_TV_OAD_ACTION);
				}
			}
			// remove it if existed
		} else {
			int index = getActionIndex(MenuAction.BROADCAST_TV_OAD_ACTION.getType());
			if (index >= 0) {
				removeAction(index);
				notifyItemRemoved(index);
			}
		}
		return true;
	}

	/*private boolean updateDisplayModeAction() {
		CommonIntegration ci = TvSingletons.getSingletons()
				.getCommonIntegration();
		MtkTvAVMode navMtkTvAVMode = MtkTvAVMode.getInstance();
		boolean is3Rd = ci.is3rdTVSource();
		boolean isEnable = false;
		SaveValue.writeWorldStringValue((Context) getMainActivity(),
				is3Rd ? "1" : "0", TAG_IS3RD_SOURCE, true);
		boolean isScanMode = mheg.getInternalScrnMode();

		if (navMtkTvAVMode != null) {
			int[] allScreenMode = navMtkTvAVMode.getAllScreenMode();
			isEnable = allScreenMode != null && allScreenMode.length > 0;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateDisplayModeAction||is3Rd =" + is3Rd
				+ "||isEnable =" + isEnable + "||isScanMode =" + isScanMode);
		MenuAction.setEnabled(MenuAction.SELECT_DISPLAY_MODE_ACTION, !is3Rd
				&& !isScanMode && isEnable);
		return !is3Rd && !isScanMode && isEnable;
	}*/
    
    private boolean updateGingaAction (){
//    	if(TVContent.getInstance(getMainActivity()).isSupportOneImage()){
//        	if(!TVContent.getInstance(getMainActivity()).isOneImagePHL()){
//        		MenuAction.setEnabled(MenuAction.GINGA_SELECTION,false);
//        	}else {
//        		MenuAction.setEnabled(MenuAction.GINGA_SELECTION,true);
//			}
//        }
    	return true;
    }

    private boolean updateAdvancedOptions(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateAdvancedOptions");
        if ((DvrManager.getInstance().getState())  instanceof StateDvrPlayback &&
                DvrManager.getInstance().getState().isRunning()) {
            MenuAction.setEnabled(MenuAction.BROADCAST_TV_SETTINGS_ACTION,false);
        }else {
            MenuAction.setEnabled(MenuAction.BROADCAST_TV_SETTINGS_ACTION,true);
        }
        return true;
    }

    @Override
    protected void executeBaseAction(int type) {
        switch (type) {
            case MenuOptionMain.OPTION_CLOSED_CAPTIONS:
                MenuAction.showCCSetting((Context)getMainActivity());
                break;
            /*case MenuOptionMain.OPTION_DISPLAY_MODE:
                MenuAction.showPictureFormatSetting((Context)getMainActivity());
                break;*/
            case MenuOptionMain.OPTION_IN_APP_PIP:
                MenuAction.enterAndroidPIP();
                break;
            case MenuOptionMain.OPTION_SYSTEMWIDE_PIP:
                break;
            case MenuOptionMain.OPTION_MULTI_AUDIO:
                MenuAction.showMultiAudioSetting((Context)getMainActivity());
                break;
            /*case MenuOptionMain.OPTION_AUTO_PICTURE:
                MenuAction.showPictureModeSetting((Context)getMainActivity());
                break;*/
           /* case MenuOptionMain.OPTION_SOUND_STYLE:
                MenuAction.showSoundStyleSetting((Context)getMainActivity());
                break;*/
            case MenuOptionMain.OPTION_PICTURE:
                MenuAction.showPictureSetting((Context)getMainActivity());
                break;
            case MenuOptionMain.OPTION_SOUND:
                MenuAction.showSoundSetting((Context)getMainActivity());
                break;
            case MenuOptionMain.OPTION_MORE_CHANNELS:
                break;
            case MenuOptionMain.OPTION_DEVELOPER:
                break;
           /* case MenuOptionMain.OPTION_MY_FAVORITE:
                break;*/
            case MenuOptionMain.OPTION_SETTINGS:
                MenuAction.showSetting((Context)getMainActivity());
                break;
            case MenuOptionMain.OPTION_BROADCAST_TV_SETTINGS:
                MenuAction.showBroadcastTvSetting((android.app.Activity)getMainActivity());
                break;
//            case MenuOptionMain.OPTION_SPEAKERS:
//                MenuAction.showSoundSpeakersSetting((Context)getMainActivity());
//                break;
            case MenuOptionMain.OPTION_POWER:
                MenuAction.showPowerSetting((Context)getMainActivity());
                break;
            case MenuOptionMain.OPTION_BROADCAST_TV_OAD:
                MenuAction.showOAD(getMainActivity());
                break;
            case MenuOptionMain.OPTION_BROADCAST_TV_CI:
                MenuAction.showCI(getMainActivity());
                break;
            case MenuOptionMain.OPTION_SOURCE_CAPTIONS:
                MenuAction.enterAndroidSource(getMainActivity());
                break;
			case MenuOptionMain.OPTION_GINGA:
				MenuAction.showGinga(getMainActivity());
				break;
			default:
				break;
	        }
    }
    
    private boolean isCurrentSourceTv() {
		return CommonIntegration.getInstance().isCurrentSourceTv();
	}

    private boolean isCurrentSourceATV() {
        return CommonIntegration.getInstance().isCurrentSourceATV();
    }
}
