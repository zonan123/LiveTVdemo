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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.leanback.widget.VerticalGridView;

import com.mediatek.wwtv.tvcenter.R;
import com.android.tv.ui.OnRepeatedKeyInterceptListener;
import com.android.tv.ui.sidepanel.InputCheckItem;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.SideFragment;
import com.mediatek.wwtv.tvcenter.nav.input.AbstractInput;
import com.mediatek.wwtv.tvcenter.nav.input.InputUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InputsBlockedFragment extends SideFragment {
    public static final String TAG = "InputsBlock";
    private long mLastFocusedChannelId = -1;
    private int mSelectedPosition = INVALID_POSITION;
    //private boolean mUpdated;
    /*private final ContentObserver mProgramUpdateObserver =
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    notifyItemsChanged();
                }
            };*/
    private final List<Item> mItems = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate start.");
    }
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
    protected String getTitle() {
        return getString(R.string.option_inputs_locked);
    }

    @Override
    protected List<Item> getItemList() {
        mItems.clear();
        Log.d(TAG, "add Input block");
        Map<Integer, String> maps=InputUtil.getSourceListForInputsBlocked(getActivity());
//        ArrayList<String> lists = mInputSourceManager.getInputSourceList();
        for (Integer hardwareId : maps.keySet()) {
        	String input=maps.get(hardwareId);
            mItems.add(new InputBlockedItem(input,hardwareId));
            Log.d(TAG, "getItemList :"+input);
        }
        return mItems;
    }


    private class InputBlockedItem extends InputCheckItem {
    	private AbstractInput mInputInfo;
        private InputBlockedItem(String inputName,int hardwareId) {
            super(inputName);
            mInputInfo=InputUtil.getInput(hardwareId);
        }
        @Override
        protected int getResourceId() {
            return R.layout.option_item_channel_lock;
        }

        //add begin by jg_jianwang for DTV01090398
        @Override
        protected void onBind(View view) {
            super.onBind(view);
            View channelContent = view.findViewById(R.id.channel_content);
            if(channelContent != null){
                channelContent.setVisibility(View.GONE);
            }
        }
        //add end by jg_jianwang for DTV01090398

        @Override
        protected void onUpdate() {
            Log.d("TAG", "onUpdate()");
            super.onUpdate();
            setChecked(mInputInfo.isBlock());
        }

        @Override
        protected void onSelected() {
            Log.d("TAG", "onSelected()");
            super.onSelected();
            mInputInfo.block(isChecked());
            //mUpdated = true;

            if(mInputInfo.getType() == AbstractInput.TYPE_TV ||
               mInputInfo.getType() == AbstractInput.TYPE_DTV) {
                if(isChecked()) {
                    com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener.
                        getInstance().updateStatus(com.mediatek.wwtv.tvcenter.
                        nav.util.ComponentStatusListener.NAV_CONTENT_BLOCKED, 0);
                }
                else {
                    com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener.
                        getInstance().updateStatus(com.mediatek.wwtv.tvcenter.
                        nav.util.ComponentStatusListener.NAV_CONTENT_ALLOWED, 0);
                }
            }
        }

        @Override
        protected void onFocused() {
            Log.d("TAG", "onFocused()");
            super.onFocused();
        }
    }
}
