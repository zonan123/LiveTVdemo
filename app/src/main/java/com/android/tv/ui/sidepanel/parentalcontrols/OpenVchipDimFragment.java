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
import android.util.Log;

import com.mediatek.twoworlds.tv.model.MtkTvOpenVCHIPPara;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.OpenVchipSubMenu;
import com.android.tv.ui.sidepanel.SideFragment;
import java.util.ArrayList;
import java.util.List;

public class OpenVchipDimFragment extends SideFragment {
    private SaveValue mSaveValue;
    private final SideFragmentListener mSideFragmentListener =
            new SideFragmentListener() {
                @Override
                public void onSideFragmentViewDestroyed() {
                    notifyDataSetChanged();
                }
            };

    public static String getDescription(Activity tvActivity) {
        return "";
    }

    @Override
    protected String getTitle() {
        return getString(R.string.parental_open_vchip_dim);
    }

    @Override
    protected List<Item> getItemList() {
    	Context mContext = getActivity().getApplicationContext();
    	TVContent mTV = TVContent.getInstance(getActivity().getApplicationContext());
        mSaveValue = SaveValue.getInstance(mContext);

        int regionIndex = mSaveValue.readValue("OpenVchipRegionIndex");
        mTV.getOpenVCHIPPara().setRegionIndex(regionIndex);
        
        mTV.getOpenVCHIPPara().setOpenVCHIPParaType(
                MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_DIM_NUM);
        int dimNum = mTV.getOpenVchip().getDimNum();
        Log.d("RRT", dimNum+"");
        List<Item> items = new ArrayList<>();
        for (int j = 0; j < dimNum; j++) {
            mTV.getOpenVCHIPPara().setOpenVCHIPParaType(
                    MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_DIM_TEXT);
            mTV.getOpenVCHIPPara().setDimIndex(j);
            final int dimIndex=j;
        items.add(
                new OpenVchipSubMenu(
                        mTV.getOpenVchip().getDimText(),
                        OpenVchipLevelFragment.getDescription(getMainActivity()),
                        getMainActivity().getSideFragmentManager(),
                        1,j) {

                    @Override
                    protected SideFragment getFragment() {
                        //modofied begin by jg_jianwang for DTV01100987
                       /*SideFragment fragment = new OpenVchipLevelFragment();
                        *fragment.setListener(mSideFragmentListener);*/
                        OpenVchipLevelFragment fragment = new OpenVchipLevelFragment();
                        fragment.setListener(mSideFragmentListener);
                        fragment.setTitle(this.getTitle());
                        //modofied end by jg_jianwang for DTV01100987
                        return fragment;
                    }
                    protected void onSelected() {
                        // TODO Auto-generated method stub
                        mSaveValue.saveValue("OpenVchipDimIndex"
                                , dimIndex);
                        super.onSelected();
                    };
                }
                );
        Log.d("RRT5", mTV.getOpenVchip().getDimText());
        }
        return items;
    }
}
