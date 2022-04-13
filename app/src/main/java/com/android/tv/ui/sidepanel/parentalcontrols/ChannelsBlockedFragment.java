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

import android.app.ProgressDialog;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.core.text.TextUtilsCompat;
import androidx.leanback.widget.VerticalGridView;

import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.util.MenuDataHelper;
import com.mediatek.wwtv.tvcenter.R;
import com.android.tv.ui.OnRepeatedKeyInterceptListener;
import com.android.tv.ui.sidepanel.ActionItem;
import com.android.tv.ui.sidepanel.ChannelCheckItem;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.SideFragment;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ChannelsBlockedFragment extends SideFragment {
    private static final String TAG = "ChannelsBlockedFragment";
    private int mBlockedChannelCount;
    private final List<MtkTvChannelInfoBase> mChannels = new ArrayList<>();
    private long mLastFocusedChannelId = -1;
    private int mSelectedPosition = INVALID_POSITION;
    private boolean mUpdated;
    private EditChannel mEidtChannel; 
    private final ContentObserver mProgramUpdateObserver =
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    notifyItemsChanged();
                }
            };
    private final Item mLockAllItem = new BlockAllItem();
    private final List<Item> mItems = new ArrayList<>();
    private ProgressDialog pdialog;
    private CompositeDisposable mcCompositeDisposable = new CompositeDisposable();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        mEidtChannel = EditChannel.getInstance(getActivity().getApplicationContext());
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: mProgramUpdateObserver :" + mProgramUpdateObserver.toString());
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
                        // In order to send tune operation once for continuous channel up/down
                        // events,
                        // we only call the moveToChannel method on ACTION_UP event of channel
                        // switch keys.
                        if (event.getAction() == KeyEvent.ACTION_UP) {
                            switch (event.getKeyCode()) {
                                case KeyEvent.KEYCODE_DPAD_UP:
                                case KeyEvent.KEYCODE_DPAD_DOWN:
                                    if (mLastFocusedChannelId != -1) {
                                        Log.d(TAG, "onInterceptKeyEvent: ");
//                                        getMainActivity()
//                                                .tuneToChannel(
//                                                        getChannelDataManager()
//                                                                .getChannel(mLastFocusedChannelId));
                                    }
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
        mUpdated = false;
        return view;
    }

    @Override
    public void onDestroyView() {
//        getActivity().getContentResolver().unregisterContentObserver(mProgramUpdateObserver);
        getChannelDataManager().applyUpdatedValuesToDb();
//        getMainActivity().endShrunkenTvView();
        if (VERSION.SDK_INT >= VERSION_CODES.O && mUpdated) {
            Log.d(TAG, "onDestroyView: ");
//            ChannelPreviewUpdater.getInstance(getMainActivity())
//                    .updatePreviewDataForChannelsImmediately();
        }
        mcCompositeDisposable.clear();
        super.onDestroyView();
    }

    @Override
    protected String getTitle() {
        return getString(R.string.option_channels_locked);
    }

    private void showBlockDelayDialog() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showBlockDelayDialog()");
        pdialog = null;
        pdialog = new ProgressDialog(mContext,R.style.TranslucentGrayProgressDialog);
        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdialog.setTitle(mContext.getResources().getString(
            R.string.menu_tv_block_loading));
        pdialog.setCancelable(false);
        pdialog.show();
        WindowManager.LayoutParams params = pdialog.getWindow().getAttributes();
        if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {
            params.x = 300;
        } else {
            params.x = -300;
        }
        params.width = (int)(pdialog.getWindow().getWindowManager().getDefaultDisplay().getWidth() * 0.3);
        pdialog.getWindow().setAttributes(params);
    }
    

//    @Override
//    public String getTrackerLabel() {
//        return TRACKER_LABEL;
//    }
    
    @Override
    protected void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (mSelectedPosition != INVALID_POSITION) {
            setSelectedPosition(mSelectedPosition);
        }
    }

    @Override
    protected List<Item> getItemList() {
         boolean isEuPARegion = CommonIntegration.getInstance().isCurrentSourceATVforEuPA();
         mItems.clear();
         mItems.add(mLockAllItem);
         mChannels.clear();
         List<MtkTvChannelInfoBase> list = MenuDataHelper.getInstance(getActivity().getApplicationContext()).getTVChannelList();
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "list.size="+list.size());
         mChannels.addAll(list);
         final long currentChannelId = CommonIntegration.getInstance().getCurrentChannelId();
         boolean hasHiddenChannels = false;
        Log.d(TAG, "hasHiddenChannels: " + hasHiddenChannels);
         for (MtkTvChannelInfoBase channel : list) {
             int tid = channel.getChannelId();
             Log.d(TAG, "tid: " + tid);
             if(isEuPARegion){
                 channel._setChannelNumber(CommonIntegration.getInstance().getAnalogChannelDisplayNumInt(channel.getChannelNumber()));
             }
//             channel.setServiceName(CommonIntegration.getInstance().
//                     getAvailableString(channel.getServiceName()));
             mItems.add(new ChannelBlockedItem(channel));
             if (channel.getChannelId() == currentChannelId) {
                 mSelectedPosition = mItems.size() - 1;
             }
         }  
         mBlockedChannelCount= mEidtChannel.getBlockChannelNumForSource();
         com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mBlockedChannelCount="+mBlockedChannelCount);
         return mItems;
    }

    private class BlockAllItem extends ActionItem {
        private TextView mTextView;

        public BlockAllItem() {
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
            boolean lock = !areAllChannelsBlocked();
//            mEidtChannel.blockChannel(mChannels,lock);
            showBlockDelayDialog();

            Disposable subscribe = Completable.create(emitter -> {
                mEidtChannel.blockAllChannels(mChannels, lock);
                emitter.onComplete();
            }).subscribeOn(Schedulers.io())
                .delay(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    for (TIFChannelInfo channelInfo : getChannelDataManager().getChannelList()) {
                        channelInfo.mLocked=lock;
                    }
                    mBlockedChannelCount = lock ? mChannels.size() : 0;
                    notifyItemsChanged();
                    mUpdated = true;
                    pdialog.dismiss();
                }, Throwable::printStackTrace);
            mcCompositeDisposable.add(subscribe);
        }

        @Override
        protected void onFocused() {
            super.onFocused();
            mLastFocusedChannelId = -1;
        }

        private void updateText() {
            mTextView.setText(
                    getString(
                            areAllChannelsBlocked()
                                    ? R.string.option_channels_unlock_all
                                    : R.string.option_channels_lock_all));
        }

        private boolean areAllChannelsBlocked() {
            return mBlockedChannelCount == mChannels.size();
        }
    }

    private class ChannelBlockedItem extends ChannelCheckItem {
        private ChannelBlockedItem(MtkTvChannelInfoBase channel) {
            super(channel/*, getChannelDataManager(), getProgramDataManager()*/);
        }

        @Override
        protected int getResourceId() {
            return R.layout.option_item_channel_lock;
        }

        @Override
        protected void onUpdate() {
            super.onUpdate();
//            setChecked(mEidtChannel.isChannelBlock(getChannel().getChannelId()));
            setChecked(mEidtChannel.isChannelBlockForRecord(getChannel().getSvlId(), getChannel().getSvlRecId()));
        }

        @Override
        protected void onSelected() {
            super.onSelected();
            getChannelDataManager().updateLocked((long)(getChannel().getChannelId()), isChecked());
            long channelId=getChannel().getChannelId();
            mEidtChannel.blockChannel(getChannel(), isChecked());
            getChannel().setBlock(isChecked());
            TIFChannelInfo info =TIFChannelManager.getInstance(getActivity()).getTIFChannelInfoById((int)channelId);
            if(info!=null){
	            for (TIFChannelInfo channel : getChannelDataManager().getChannelList()) {
	            	if(channel.mId==info.mId){
	            		channel.mLocked=isChecked();
	            		break;
	            	}
	            }
            }
            mBlockedChannelCount += isChecked() ? 1 : -1;
            notifyItemChanged(mLockAllItem);
            mUpdated = true;
        }

        @Override
        protected void onFocused() {
            Log.d("TAG", "onFocused()");
            super.onFocused();
            mLastFocusedChannelId = getChannel().getChannelId();
        }
    }
    
}
