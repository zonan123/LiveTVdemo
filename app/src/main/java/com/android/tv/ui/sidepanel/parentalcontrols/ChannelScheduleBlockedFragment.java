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

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.leanback.widget.VerticalGridView;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.util.MenuDataHelper;
import com.mediatek.wwtv.tvcenter.R;
import com.android.tv.ui.OnRepeatedKeyInterceptListener;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.SideFragment;
import com.android.tv.ui.sidepanel.SubMenuItem;
import com.android.tv.ui.sidepanel.SideFragment.SideFragmentListener;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import java.util.ArrayList;
import java.util.List;

public class ChannelScheduleBlockedFragment extends SideFragment {
    private static final String TAG = "ChannelScheduleBlockedFragment";
    private int mSelectedPosition = INVALID_POSITION;
    private final SideFragmentListener mSideFragmentListener =
            new SideFragmentListener() {
                @Override
                public void onSideFragmentViewDestroyed() {
                    notifyDataSetChanged();
                }
            };
    private final List<Item> mItems = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
    	EditChannel mEidtChannel = EditChannel.getInstance(getActivity().getApplicationContext());
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: mEidtChannel :" + mEidtChannel.toString());
    }
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        VerticalGridView listView = (VerticalGridView) view.findViewById(R.id.side_panel_list);
        listView.setOnKeyInterceptListener(
                new OnRepeatedKeyInterceptListener(listView) {
                    @Override
                    public boolean onInterceptKeyEvent(KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_UP) {
                            switch (event.getKeyCode()) {
                                case KeyEvent.KEYCODE_DPAD_UP:
                                case KeyEvent.KEYCODE_DPAD_DOWN:
                                    break;
                                default:
                                    break;
                            }
                        }
                        return super.onInterceptKeyEvent(event);
                    }
                });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
    }
    @Override
    protected void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (mSelectedPosition != INVALID_POSITION) {
            setSelectedPosition(mSelectedPosition);
        }
    }
    @Override
    protected String getTitle() {
        return getString(R.string.menu_parental_channel_schedule_block);
    }

    @Override
    protected List<Item> getItemList() {
        mItems.clear();
        List<MtkTvChannelInfoBase> list = MenuDataHelper.getInstance(getActivity()).getTVChannelList();
        String[] optionValue = new String[] {getString(R.string.rating_schedule_off), getString(R.string.rating_schedule_block)};
        for (MtkTvChannelInfoBase infobase : list) {
            final int tid = infobase.getChannelId();
            final String name = "" + MenuDataHelper.getInstance(getActivity()).getDisplayChNumber(tid) + "        "
                    + ((infobase.getServiceName() == null) ? "" : infobase.getServiceName());
            String description = "";
            if (EditChannel.getInstance(getActivity()).getSchBlockType(infobase.getChannelId()) == 0) {
                description = optionValue[0];
            } else {
                description = optionValue[1];
            }
            
            SubMenuItem item= new SubMenuItem(name,description, mSideFragmentManager) {
                @Override
                protected SideFragment getFragment() {
                	SideFragment fragment = SubChannelScheduleBlockedFragment.create(name,tid);
                    fragment.setListener(mSideFragmentListener);
                    return fragment;
                }
            };
            mItems.add(item); 
            
            if (infobase.getChannelId() == CommonIntegration.getInstance().getCurrentChannelId()) {
                mSelectedPosition = mItems.size() - 1;
            }
            
        }
        return mItems;
    }
}
