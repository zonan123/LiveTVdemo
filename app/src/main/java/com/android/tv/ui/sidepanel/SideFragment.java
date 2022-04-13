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

package com.android.tv.ui.sidepanel;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

import com.mediatek.wwtv.setting.LiveTvSetting;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFProgramManager;
import com.android.tv.util.ViewCache;

import java.util.ArrayList;
import java.util.List;

public abstract class SideFragment<T extends Item> extends Fragment/* implements HasTrackerLabel*/ {
    public static final int INVALID_POSITION = -1;
    private static final int PRELOAD_VIEW_SIZE = 7;
    private static final int[] PRELOAD_VIEW_IDS = {
        R.layout.option_item_radio_button,
        R.layout.option_item_channel_lock,
        R.layout.option_item_check_box,
        R.layout.option_item_channel_check,
        R.layout.option_item_action
    };

    private static RecyclerView.RecycledViewPool sRecycledViewPool =
            new RecyclerView.RecycledViewPool();

    private VerticalGridView mListView;
    private ItemAdapter mAdapter;
    protected List<T> mItems=new ArrayList<T> ();
    private SideFragmentListener mListener;
    private TIFChannelManager mChannelDataManager;
    private TIFProgramManager mProgramDataManager;
    public SideFragmentManager mSideFragmentManager;
    //private Tracker mTracker;
    //private final DurationTimer mSidePanelDurationTimer = new DurationTimer();

    private final int mHideKey;
    //private final int mDebugHideKey;
    protected Context mContext;

    public SideFragment() {
        this(KeyEvent.KEYCODE_UNKNOWN/*, KeyEvent.KEYCODE_UNKNOWN*/);
    }

    /**
     * @param hideKey the KeyCode used to hide the fragment
     * @param debugHideKey the KeyCode used to hide the fragment if {@link
     *     SystemProperties#USE_DEBUG_KEYS}.
     */
    public SideFragment(int hideKey/*, int debugHideKey*/) {
        mHideKey = hideKey;
        //mDebugHideKey = debugHideKey;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mChannelDataManager = TvSingletons.getSingletons().getChannelDataManager();
        mProgramDataManager = TvSingletons.getSingletons().getProgramDataManager();
        mSideFragmentManager = getMainActivity().getSideFragmentManager();
        //mTracker = TvSingletons.getSingletons(context).getTracker();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =
                ViewCache.getInstance()
                        .getOrCreateView(inflater, getFragmentLayoutResourceId(), container);

        TextView textView = (TextView) view.findViewById(R.id.side_panel_title);
        textView.setText(getTitle());

        mListView = (VerticalGridView) view.findViewById(R.id.side_panel_list);
        mListView.setRecycledViewPool(sRecycledViewPool);

        mAdapter = new ItemAdapter(inflater, mItems);
        mListView.setAdapter(mAdapter);
        mListView.requestFocus();
        getData();
        return view;
    }

    protected void getData() {
        new Thread(new GetDataRunnable()).start();
    }

/*    @Override
    public void onResume() {
        super.onResume();
        //mTracker.sendShowSidePanel(this);
        //mTracker.sendScreenView(this.getTrackerLabel());
        //mSidePanelDurationTimer.start();
    }

    @Override
    public void onPause() {
        //mTracker.sendHideSidePanel(this, mSidePanelDurationTimer.reset());
        super.onPause();
    }

    @Override
    public void onDetach() {
        //mTracker = null;
        super.onDetach();
    }*/

    public final boolean isHideKeyForThisPanel(int keyCode) {
//        boolean debugKeysEnabled = false;//SystemProperties.USE_DEBUG_KEYS.getValue();
        return mHideKey != KeyEvent.KEYCODE_UNKNOWN
                && (mHideKey == keyCode);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListView.swapAdapter(null, true);
        if (mListener != null) {
            mListener.onSideFragmentViewDestroyed();
        }
    }

    public final void setListener(SideFragmentListener listener) {
        mListener = listener;
    }

    protected void setSelectedPosition(int position) {
        mListView.setSelectedPosition(position);
    }

    protected int getSelectedPosition() {
        return mListView.getSelectedPosition();
    }

    public void setItems(List<T> items) {
        mAdapter.reset(items);
    }

    protected void closeFragment() {
        mSideFragmentManager.popSideFragment();
    }

    protected LiveTvSetting getMainActivity() {
        return LiveTvSetting.getInstance();
    }

    protected TIFChannelManager getChannelDataManager() {
        return mChannelDataManager;
    }

    protected TIFProgramManager getProgramDataManager() {
        return mProgramDataManager;
    }

    protected void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    /*
     * HACK: The following methods bypass the updating mechanism of RecyclerView.Adapter and
     * directly updates each item. This works around a bug in the base libraries where calling
     * Adapter.notifyItemsChanged() causes the VerticalGridView to lose track of displayed item
     * position.
     */

    protected void notifyItemChanged(int position) {
        notifyItemChanged(mAdapter.getItem(position));
    }

    protected void notifyItemChanged(Item item) {
        item.notifyUpdated();
    }

    /** Notifies all items of ItemAdapter has changed without structural changes. */
    protected void notifyItemsChanged() {
        notifyItemsChanged(0, mAdapter.getItemCount());
    }

    /**
     * Notifies some items of ItemAdapter has changed starting from position <code>positionStart
     * </code> to the end without structural changes.
     */
    protected void notifyItemsChanged(int positionStart) {
        notifyItemsChanged(positionStart, mAdapter.getItemCount() - positionStart);
    }

    protected void notifyItemsChanged(int positionStart, int itemCount) {
        while (itemCount-- != 0) {
            notifyItemChanged(positionStart++);
        }
    }

    /*
     * END HACK
     */

    protected int getFragmentLayoutResourceId() {
        return R.layout.option_fragment;
    }

    protected abstract String getTitle();

    //@Override
    //public abstract String getTrackerLabel();

    protected abstract List<T> getItemList();

    public interface SideFragmentListener {
        void onSideFragmentViewDestroyed();
    }

    /** Preloads the item views. */
    public static void preloadItemViews(Context context) {
        ViewCache.getInstance()
                .putView(context, R.layout.option_fragment, new FrameLayout(context), 1);
        VerticalGridView fakeParent = new VerticalGridView(context);
        for (int id : PRELOAD_VIEW_IDS) {
            sRecycledViewPool.setMaxRecycledViews(id, PRELOAD_VIEW_SIZE);
            ViewCache.getInstance().putView(context, id, fakeParent, PRELOAD_VIEW_SIZE);
        }
    }

    /** Releases the recycled view pool. */
    public static void releaseRecycledViewPool() {
        sRecycledViewPool.clear();
    }

    @Override public void onDestroy() {
        super.onDestroy();
        releaseRecycledViewPool();
    }

    private static class ItemAdapter<T extends Item> extends RecyclerView.Adapter<ViewHolder> {
        private final LayoutInflater mLayoutInflater;
        private List<T> mItems;

        private ItemAdapter(LayoutInflater layoutInflater, List<T> items) {
            mLayoutInflater = layoutInflater;
            mItems = items;
        }

        private void reset(List<T> items) {
            mItems = items;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = ViewCache.getInstance().getOrCreateView(mLayoutInflater, viewType, parent);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.onBind(this, getItem(position));
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            holder.onUnbind();
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getResourceId();
        }

        @Override
        public int getItemCount() {
            return mItems == null ? 0 : mItems.size();
        }

        private T getItem(int position) {
            return mItems.get(position);
        }

        private void clearRadioGroup(T item) {
            int position = mItems.indexOf(item);
            for (int i = position - 1; i >= 0; --i) {
            	item = mItems.get(i);
                if (item instanceof RadioButtonItem) {
                    ((RadioButtonItem) item).setChecked(false);
                } else {
                    break;
                }
            }
            for (int i = position + 1; i < mItems.size(); ++i) {
            	item = mItems.get(i);
                if (item instanceof RadioButtonItem) {
                    ((RadioButtonItem) item).setChecked(false);
                } else {
                    break;
                }
            }
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnFocusChangeListener {
        private ItemAdapter mAdapter;
        public Item mItem;

        private ViewHolder(View view) {
            super(view);
            itemView.setOnClickListener(this);
            itemView.setOnFocusChangeListener(this);
        }

        public void onBind(ItemAdapter adapter, Item item) {
            mAdapter = adapter;
            mItem = item;
            mItem.onBind(itemView);
            mItem.onUpdate();
        }

        public void onUnbind() {
            mItem.onUnbind();
            mItem = null;
            mAdapter = null;
        }

        @Override
        public void onClick(View view) {
            if (mItem instanceof RadioButtonItem) {
                mAdapter.clearRadioGroup(mItem);
            }
            if (view.getBackground() instanceof RippleDrawable) {
                view.postDelayed(
                        () -> {
                            if (mItem != null) {
                                mItem.onSelected();
                            }
                        },
                        view.getResources().getInteger(R.integer.side_panel_ripple_anim_duration));
            } else {
                mItem.onSelected();
            }
        }

        @Override
        public void onFocusChange(View view, boolean focusGained) {
            if (focusGained) {
                mItem.onFocused();
            }
        }
    }

    protected void refreshUI(){
        notifyDataSetChanged();
    }

    private class GetDataRunnable implements Runnable{

		@Override
		public void run() {
			mItems.clear();
        	mItems.addAll(getItemList());
        	Activity activity=getActivity();
        	if(activity!=null){
	        	activity.runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
					    refreshUI();
					}

	        	});
        	}
		}

    }
}
