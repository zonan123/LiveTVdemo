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
//import android.support.v17.leanback.widget.VerticalGridView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.leanback.widget.VerticalGridView;

import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.R;
import com.android.tv.ui.OnRepeatedKeyInterceptListener;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.RadioButtonItem;
import com.android.tv.ui.sidepanel.SideFragment;

import java.util.ArrayList;
import java.util.List;

public class OperationModeFragment extends SideFragment {
    //private static final String TAG = "OperationModeFragment";
    private static final String ARGS_INDEX = "args_index";
    private static final String ARGS_CHANNEL_ID = "args_channel_id";
    
    /*private final SideFragmentListener mSideFragmentListener =
            new SideFragmentListener() {
                @Override
                public void onSideFragmentViewDestroyed() {
                    notifyDataSetChanged();
                }
            };*/
    private final List<Item> mItems = new ArrayList<>();
    private int mIndex;
    private int mChannelId;
    public static OperationModeFragment create(int index,int channelId) {
    	OperationModeFragment fragment = new OperationModeFragment();
    	Bundle args = new Bundle();
        args.putInt(ARGS_INDEX, index);
        args.putInt(ARGS_CHANNEL_ID, channelId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mIndex = getArguments().getInt(ARGS_INDEX);
        mChannelId=getArguments().getInt(ARGS_CHANNEL_ID);
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
    protected String getTitle() {
        return getString(R.string.menu_parental_channel_schedule_block_operation_mode);
    }

    @Override
    protected List<Item> getItemList() {
        mItems.clear();
        final String[] valueStrings = getActivity().getResources().getStringArray(R.array.menu_parental_block_channel_schedule_operation_array);
        for(int i=0;i<valueStrings.length;i++){
        	String title=valueStrings[i];
        	RadioButtonItem item=new OperationModeItem(title,i);
        	item.setChecked(i==mIndex);
        	mItems.add(item);
        }
        return mItems;
    }


    private class OperationModeItem extends RadioButtonItem {
        private final int mIndex;
        private OperationModeItem(String title,int index) {
        	super(title);
        	mIndex = index;
        }

        @Override
        protected void onSelected() {
            super.onSelected();
            MenuConfigManager.getInstance(getActivity()).setValue(MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_OPERATION_MODE + mChannelId, mIndex);
        }
    }
    
}
