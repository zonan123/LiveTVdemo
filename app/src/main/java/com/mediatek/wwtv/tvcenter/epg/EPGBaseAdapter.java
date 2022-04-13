package com.mediatek.wwtv.tvcenter.epg;

import java.util.List;
import android.content.Context;

import android.widget.BaseAdapter;

public abstract class EPGBaseAdapter<T> extends BaseAdapter {
    public Context mContext;
    public List<T> group = null;
    public int mItemHeight=0;

	public EPGBaseAdapter(Context context) {
		mContext = context;
	}

	public Context getContext() {
		return mContext;
	}

	public int getCount() {
		return (group == null) ? 0 : group.size();
	}

	public Object getItem(int position) {
		if (group == null || position < 0 || position >= group.size()) {
			return null;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("outofindex", "position is :" + position + "==group.size() is :"
				+ group.size());
		return group.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean isEmpty() {
		return (group == null) || group.isEmpty();
	}

	public void setGroup(List<T> g) {
		group = g;
	}

	public void addGroup(List<T> g) {
		if (group != null) {
			group.addAll(g);
		}
	}

	public List<T> getGroup() {
		return group;
	}

	public int getItemHeight() {
		return mItemHeight;
	}

	public void setItemHeight(int itemHeight) {
		this.mItemHeight = itemHeight;
	}

	
	
	

}
