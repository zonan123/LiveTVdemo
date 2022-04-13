package com.mediatek.wwtv.tvcenter.epg.eu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.ListAdapter;

import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.epg.IPageCallback;
import com.mediatek.wwtv.tvcenter.epg.NoScrollListView;



public class EPGEventListView extends NoScrollListView {
	private static final String TAG = "EPGEventListView";

	private IPageCallback mPageCallback;

	public EPGEventListView(Context context) {
		super(context);
		init(context);
	}

	public void addPageCallback(IPageCallback pageCallback) {
		mPageCallback = pageCallback;
	}


	public EPGEventListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public EPGEventListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		Context mContext = context;
		com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "mContext>>>"+mContext);
	}

	public int getCount() {
		ListAdapter adapter = getAdapter();
		return adapter == null ? 0 : adapter.getCount();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_DOWN: {
			int selectedItemPos = getSelectedItemPosition()
					% DataReader.PER_PAGE_CHANNEL_NUMBER;
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown:selectedItemPos=" + selectedItemPos);
			if (selectedItemPos == getCount() - 1
					&& mPageCallback.hasNextPage()) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "goto next page");
				mPageCallback.onRefreshPage();
				setSelection(0);
				return true;
			}
		}
			break;
		case KeyEvent.KEYCODE_DPAD_UP: {
			int selectedItemPos = getSelectedItemPosition()
					% DataReader.PER_PAGE_CHANNEL_NUMBER;
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown:selectedItemPos=" + selectedItemPos);
			if (selectedItemPos == 0) {
				if(mPageCallback.hasPrePage()){
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "goto pre page");
					mPageCallback.onRefreshPage();
					setSelection(getCount() - 1);
				}
				return true;
			}
		}
			break;
			default:
			  break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
