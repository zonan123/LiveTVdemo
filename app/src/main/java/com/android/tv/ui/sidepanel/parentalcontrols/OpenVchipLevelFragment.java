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

package com.android.tv.ui.sidepanel.parentalcontrols;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.leanback.widget.VerticalGridView;

import com.mediatek.twoworlds.tv.model.MtkTvOpenVCHIPSettingInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvOpenVCHIPPara;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.R;
import com.android.tv.ui.OnRepeatedKeyInterceptListener;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.OpenVchipLevelItem;
import com.android.tv.ui.sidepanel.SideFragment;
//import android.support.v17.leanback.widget.VerticalGridView;

import java.util.ArrayList;
import java.util.List;

public class OpenVchipLevelFragment extends SideFragment {
    private static final String TAG = "OpenVchipLevelFragment";
    private TVContent mTV;
    private Context mContext;
    private static final String mRegionString = "OpenVchipRegionIndex";
    private static final String mDimString = "OpenVchipDimIndex";
    private long mLastFocusedChannelId = -1;
    private int mSelectedPosition = INVALID_POSITION;
//    private boolean mUpdated;
    private String mTitle;//add by jg_jianwang for DTV01100987
    private final List<Item> mItems = new ArrayList<>();
    private SaveValue mSaveValue;
    /*private final SideFragmentListener mSideFragmentListener =
            new SideFragmentListener() {
                @Override
                public void onSideFragmentViewDestroyed() {
                    notifyDataSetChanged();
                }
            };*/

    public static String getDescription(Activity tvActivity) {
        return "";
    }

    @Override
    protected String getTitle() {
        //modified by jg_jianwang for DTV01100987
        //return getString(R.string.parental_open_vchip_level);
        return mTitle.isEmpty() ? getString(R.string.parental_open_vchip_level) : mTitle;
    }

    //add by jg_jianwang for DTV01100987
    protected void setTitle(String title){
        if(title != null){
            mTitle = title;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
        mTV = TVContent.getInstance(getActivity().getApplicationContext());
        mSaveValue = SaveValue.getInstance(mContext);  
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (mSelectedPosition != INVALID_POSITION) {
            setSelectedPosition(mSelectedPosition);
        }
        VerticalGridView listView = (VerticalGridView) view.findViewById(R.id.side_panel_list);
        listView.setOnKeyInterceptListener(
                new OnRepeatedKeyInterceptListener(listView) {
                    @Override
                    public boolean onInterceptKeyEvent(KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_UP) {
                            switch (event.getKeyCode()) {
                                case KeyEvent.KEYCODE_DPAD_UP:
                                case KeyEvent.KEYCODE_DPAD_DOWN:
                                    if (mLastFocusedChannelId != -1) {
                                        Log.d(TAG, "onInterceptKeyEvent key_dpad_up/down.");
                                    }
                                    break;
                                default:
                                	break;
                            }
                        }
                        return super.onInterceptKeyEvent(event);
                    }
                });
        //mUpdated = false;
        return view;
    }
    
    @Override
    protected List<Item> getItemList() {
        mItems.clear();
        int regionIndex = mSaveValue.readValue(mRegionString);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "regionIndex="+regionIndex);
        int dimIndex = mSaveValue.readValue(mDimString);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dimIndex="+dimIndex);
        mTV.getOpenVCHIPPara().setRegionIndex(regionIndex);
        mTV.getOpenVCHIPPara().setDimIndex(dimIndex);
        mTV.getOpenVCHIPPara().setOpenVCHIPParaType(
                MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_LVL_NUM);
        int levelNum = mTV.getOpenVchip().getLevelNum();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "levelNum="+levelNum);
        mTV.getOpenVCHIPPara().setOpenVCHIPParaType(
                MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_LVL_ABBR);
        for (int k = 0; k < levelNum; k++) {
            mTV.getOpenVCHIPPara().setLevelIndex(k + 1);
            String textString = mTV.getOpenVchip().getLvlAbbrText();
            if (textString == null || textString.isEmpty()){
                textString = mContext.getResources().getString(R.string.menu_string_rrt5_none_title);
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "textString="+textString+",regionIndex="+regionIndex+",dimIndex="+dimIndex+",k="+k);
            mItems.add(new OpenVchipItem(regionIndex,dimIndex,k,textString));
        }

        return mItems;
    }
    private class OpenVchipItem extends OpenVchipLevelItem {
        private OpenVchipItem(int mRegion,int mDim,int mLevel,String mLevelString) {
            super(mRegion,mDim,mLevel,mLevelString);
        }

        @Override
        protected int getResourceId() {
            return R.layout.option_item_channel_lock;
        }

        //add begin by jg_jianwang for DTV01298059
        @Override
        protected void onBind(View view) {
            super.onBind(view);
            View channelContent = view.findViewById(R.id.channel_content);
            if(channelContent != null){
                channelContent.setVisibility(View.GONE);
            }
        }
        //add end by jg_jianwang for DTV01298059

        @Override
        protected void onUpdate() {
            super.onUpdate();
            int index = getmOpenVchipLevel();
            int reginIndex = getmOpenVchipRegion();
            int dimIndex = getmOpenVchipDim();
            MtkTvOpenVCHIPSettingInfoBase info = mTV.getNewOpenVchipSetting(
                    reginIndex, dimIndex);
            byte[] block = info.getLvlBlockData();
            int iniValue = block[index];
            setChecked(iniValue == 1);
        }

        @Override
        protected void onSelected() {
            super.onSelected();
            int index = getmOpenVchipLevel();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "index="+index);
            int reginIndex = getmOpenVchipRegion();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "reginIndex="+reginIndex);
            int dimIndex = getmOpenVchipDim();
            MtkTvOpenVCHIPSettingInfoBase info = mTV.getNewOpenVchipSetting(
                    reginIndex, dimIndex);
            byte[] block = info.getLvlBlockData();
            EditChannel.getInstance(mContext).setOpenVCHIP(reginIndex,dimIndex,index);
            notifyItemsChanged();
            //mUpdated = true;
        }

        @Override
        protected void onFocused() {
            super.onFocused();
            Log.d(TAG, "onFocused");
        }
    }
}
