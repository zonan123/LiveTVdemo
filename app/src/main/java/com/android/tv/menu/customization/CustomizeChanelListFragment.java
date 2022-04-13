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
package com.android.tv.menu.customization;

import android.os.Bundle;
//import android.support.v17.leanback.widget.VerticalGridView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.leanback.widget.VerticalGridView;

import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.MenuDataHelper;
import com.mediatek.wwtv.tvcenter.R;
import com.android.tv.ui.OnRepeatedKeyInterceptListener;
import com.android.tv.ui.sidepanel.ActionItem;
import com.android.tv.ui.sidepanel.CheckBoxItem;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.SideFragment;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;

import java.util.ArrayList;
import java.util.List;

public class CustomizeChanelListFragment extends SideFragment {
    private static final String TRACKER_LABEL = "CustomizeChanelListFragment";
    private int mSkipedChannelCount;
    private final List<MtkTvChannelInfoBase> mChannels = new ArrayList<>();
//    private long mLastFocusedChannelId = -1;
    private int mSelectedPosition = INVALID_POSITION;
//    private boolean mUpdated;
    private EditChannel mEidtChannel; 
//    private final ContentObserver mProgramUpdateObserver =
//            new ContentObserver(new Handler()) {
//                @Override
//                public void onChange(boolean selfChange, Uri uri) {
//                    notifyItemsChanged();
//                }
//            };
    private final Item mSkipAllItem = new SkipkAllItem();
    private final List<Item> mItems = new ArrayList<>();

    protected int getFragmentLayoutResourceId() {
        return R.layout.multi_audio_fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TRACKER_LABEL, "onCreate()" );
        // TODO Auto-generated method stub
        mEidtChannel = EditChannel.getInstance(getActivity().getApplicationContext());
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TRACKER_LABEL, "onCreateView()" );
        if (mSelectedPosition != INVALID_POSITION) {
            setSelectedPosition(mSelectedPosition);
        }
        VerticalGridView listView = (VerticalGridView) view.findViewById(R.id.side_panel_list);
        listView.setOnKeyInterceptListener(
                new OnRepeatedKeyInterceptListener(listView) {
                    @Override
                    public boolean onInterceptKeyEvent(KeyEvent event) {
                        // In order to send tune operation once for continuous channel up/down
                        // events,
                        // we only call the moveToChannel method on ACTION_UP event of channel
                        // switch keys.
                        if (event.getAction() == KeyEvent.ACTION_UP) {
                            switch (event.getKeyCode()) {
                                case KeyEvent.KEYCODE_DPAD_UP:
                                case KeyEvent.KEYCODE_DPAD_DOWN:
//                                    if (mLastFocusedChannelId != -1) {
//                                        getMainActivity()
//                                                .tuneToChannel(
//                                                        getChannelDataManager()
//                                                                .getChannel(mLastFocusedChannelId));
//                                    }
                                    break;
                                default:
                                	break;
                            }
                        }
                        return super.onInterceptKeyEvent(event);
                    }
                });
//        getActivity()
//                .getContentResolver()
//                .registerContentObserver(
//                        TvContract.Programs.CONTENT_URI, true, mProgramUpdateObserver);
//        getMainActivity().startShrunkenTvView(true, true);
//        mUpdated = false;
        return view;
    }

    @Override
    public void onDestroyView() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TRACKER_LABEL, "onDestroyView()" );
//        getActivity().getContentResolver().unregisterContentObserver(mProgramUpdateObserver);
        getChannelDataManager().applyUpdatedValuesToDb();
//        getMainActivity().endShrunkenTvView();
//        if (VERSION.SDK_INT >= VERSION_CODES.O && mUpdated) {
//            ChannelPreviewUpdater.getInstance(getMainActivity())
//                    .updatePreviewDataForChannelsImmediately();
//        }
        super.onDestroyView();
        MenuConfigManager.getInstance(getActivity().getApplicationContext()).setValue(MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
    }

    @Override
    protected String getTitle() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TRACKER_LABEL, "getTitle()" );
        return getString(R.string.menu_channel_customize_channel_list);
    }

//    @Override
//    public String getTrackerLabel() {
//        return TRACKER_LABEL;
//    }

    @Override
    protected List<Item> getItemList() {
        mItems.clear();
        mItems.add(mSkipAllItem);
        mChannels.clear();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TRACKER_LABEL, "getItemList() start..." );
        final long currentChannelId = CommonIntegration.getInstance().getCurrentChannelId();
        List<MtkTvChannelInfoBase> list = MenuDataHelper.getInstance(getActivity().getApplicationContext()).getTVChannelList();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TRACKER_LABEL, "getItemList() mid ... " );
        for(MtkTvChannelInfoBase channel : list){
            if(channel.getChannelId() != currentChannelId){
                mChannels.add(channel);
            }
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TRACKER_LABEL, "getItemList() end ... " );
//        Collections.sort(
//                mChannels,
//                (TIFChannelInfo lhs, TIFChannelInfo rhs) -> {
//                    if (lhs.isBrowsable() != rhs.isBrowsable()) {
//                        return lhs.isBrowsable() ? -1 : 1;
//                    }
//                    return compare(lhs.getDisplayNumber(), rhs.getDisplayNumber());
//                });

//        boolean hasHiddenChannels = false;
//        for (TIFChannelInfo channel : mChannels) {
//            if (!channel.isBrowsable() && !hasHiddenChannels) {
//                mItems.add(new DividerItem(getString(R.string.option_channels_subheader_hidden)));
//                hasHiddenChannels = true;
//            }

//        }
        for (MtkTvChannelInfoBase channel : mChannels) {
//            int tid = channel.getChannelId();
//            String name = "" + mDataHelper.getDisplayChNumber(tid) + "        "
//                    + ((channel.getServiceName() == null) ? "" : channel.getServiceName());
            mItems.add(new ChannelSkipedItem(channel));
            if (mEidtChannel.isChannelSkip((int)channel.getChannelId())) {
                ++mSkipedChannelCount;
            }
            if (channel.getChannelId() == currentChannelId) {
                mSelectedPosition = mItems.size() - 1;
            }
        }        return mItems;
    }

    private class SkipkAllItem extends ActionItem {
        private TextView mTextView;

        public SkipkAllItem() {
            super(null);
        }

        @Override
        protected void onBind(View view) {
            super.onBind(view);
            mTextView = (TextView) view.findViewById(R.id.title);
        }

        @Override
        protected void onUpdate() {
            super.onUpdate();
            updateText();
        }

        @Override
        protected void onUnbind() {
            super.onUnbind();
            mTextView = null;
        }

        @Override
        protected void onSelected() {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TRACKER_LABEL, "onCreate()" );
            boolean skip = !areAllChannelsSkiped();
            for (MtkTvChannelInfoBase channel : mChannels) {
                long channelId=channel.getChannelId();
                mEidtChannel.setChannelSkip((int)channelId, skip);
            }
            final long currentChannelId = CommonIntegration.getInstance().getCurrentChannelId();
            for (TIFChannelInfo channelInfo : getChannelDataManager().getCurrentSVLChannelList()) {
                if(channelInfo.mMtkTvChannelInfo != null && currentChannelId != channelInfo.mMtkTvChannelInfo.getChannelId()){
                    channelInfo.mMtkTvChannelInfo.setSkip(skip);
                }
            }
            mSkipedChannelCount = skip ? mChannels.size() : 0;
            notifyItemsChanged();
//            mUpdated = true;
        }

        private void updateText() {
            
            mTextView.setText(
                    getString(
                            areAllChannelsSkiped()
                                    ? R.string.option_channels_unskip_all
                                    : R.string.option_channels_skip_all));
        }

        private boolean areAllChannelsSkiped() {
            return mSkipedChannelCount == mChannels.size();
        }
    }

    private class ChannelSkipedItem extends CheckBoxItem {
        private ChannelSkipedItem(MtkTvChannelInfoBase channel) {
            super(channel/*, getChannelDataManager(), getProgramDataManager()*/);
        }

        @Override
        protected int getResourceId() {
            return R.layout.option_item_check_box;
        }

        @Override
        protected void onUpdate() {
            Log.d("TAG", "onUpdate()");
            super.onUpdate();
            setChecked(mEidtChannel.isChannelSkip((int)getChannel().getChannelId()));
        }

        @Override
        protected void onSelected() {
            Log.d("TAG", "onSelected()");
            super.onSelected();
            Log.d("TAG", "onSelected()1 getChannel().getId() = "+getChannel().getChannelId());
        //    getChannelDataManager().updateLocked((long)(getChannel().getChannelId()), isChecked());
        //    Log.d("TAG", "onSelected()2 getChannel().getId() = "+getChannel().getChannelId());
            long channelId=getChannel().getChannelId();
            mEidtChannel.setChannelSkip((int)channelId, isChecked());
            TIFChannelInfo info =TIFChannelManager.getInstance(getActivity()).getTIFChannelInfoById((int)channelId);
            for (TIFChannelInfo channel : getChannelDataManager().getCurrentSVLChannelList()) {
            	if(channel!=null && info!=null && channel.mId == info.mId){
            		channel.mMtkTvChannelInfo.setSkip(isChecked());
            		break;
            	}
            }
            mSkipedChannelCount += isChecked() ? 1 : -1;
            notifyItemChanged(mSkipAllItem);
//            mUpdated = true;
        }

        @Override
        protected void onFocused() {
            Log.d("TAG", "onFocused()");
            super.onFocused();
//            mLastFocusedChannelId = getChannel().getChannelId();
        }
    }
}
