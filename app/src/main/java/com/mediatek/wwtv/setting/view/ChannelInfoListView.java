package com.mediatek.wwtv.setting.view;

import com.mediatek.wwtv.setting.base.scan.adapter.ChannelInfoAdapter;


import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ChannelInfoListView extends ListView{

	private int itemsCount;

	private int itemHeight;

	private ListAdapter adapter;

	private int scrollDuration = 1000;

	private boolean isScrollTop;

    Context mContext ;
    int currentItemPosition;
    int lastItemPosition ;

	private OnScrollBottomListener onScrollBottomListener;

	private OnScrollTopListener onScrollTopListener;

	public ChannelInfoListView(Context context) {
		this(context, null);
	}

	public ChannelInfoListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setSmoothScrollbarEnabled(true);
		mContext = context ;

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (adapter != null) {
			itemHeight = this.getChildAt(0).getHeight();
		}

	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		this.adapter = adapter;
		itemsCount = adapter.getCount();
		currentItemPosition = 0;
		lastItemPosition = 0;

	}

	/**
	 *
	 * @param scrollDutation
	 */
	public void setScrollDuration(int scrollDutation) {
		this.scrollDuration = scrollDutation;

	}

	public int getCurrentItemPosition() {
		return currentItemPosition;
	}

	public void setCurrentItemPosition(int currentItemPosition) {
		this.currentItemPosition = currentItemPosition;
	}

	public void smoothScrollTo(final int pos){
		this.postDelayed(new Runnable() {
			@Override
			public void run() {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(VIEW_LOG_TAG, "itemHeight:"+itemHeight);
				smoothScrollBy(itemHeight*(pos+1), scrollDuration);
			}
		},500);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(VIEW_LOG_TAG, "onKeyDown.....keyCode:"+keyCode+",");

		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
			if(adapter instanceof ChannelInfoAdapter){
				((ChannelInfoAdapter) adapter).goToPrevPage();
			}
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			if(adapter instanceof ChannelInfoAdapter){
				((ChannelInfoAdapter) adapter).goToNextPage();
			}
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			setCurrAndLastPos();
			if (currentItemPosition == itemsCount - 1) {
				if (onScrollBottomListener != null) {
					onScrollBottomListener.onScrollBottom();
				}
				return true;
			} else {
				currentItemPosition ++;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(VIEW_LOG_TAG, "onKey-dpadDown scroll:"+(currentItemPosition-lastItemPosition));
				this.smoothScrollBy(itemHeight*(currentItemPosition-lastItemPosition), scrollDuration);
				return false;
			}
		}else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			setCurrAndLastPos();
			if (currentItemPosition == 0) {

				if (onScrollTopListener != null) {
					onScrollTopListener.onScrollTop();
				}
				return isScrollTop;
			} else {
				currentItemPosition --;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(VIEW_LOG_TAG, "onKey-dpadUp scroll:"+(currentItemPosition-lastItemPosition));
				if(currentItemPosition > lastItemPosition){
					smoothScrollBy(itemHeight*(currentItemPosition-lastItemPosition), scrollDuration);
				}else{
					smoothScrollBy(-itemHeight, scrollDuration);
				}

				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setCurrAndLastPos(){
		lastItemPosition = currentItemPosition;
		if(adapter instanceof ChannelInfoAdapter){
			int selecpos=((ChannelInfoAdapter) adapter).getSelectPos();
			if(currentItemPosition ==0 && selecpos > currentItemPosition){
				currentItemPosition = selecpos;
			}
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(VIEW_LOG_TAG, "lastItemPosition:"+lastItemPosition+",currentItemPosition=="+currentItemPosition);
	}

	/**
	 */
	public interface OnScrollBottomListener {
		void onScrollBottom();
	}

	public void setOnScrollBottomListener(
			OnScrollBottomListener onScrollBottomListener) {

		this.onScrollBottomListener = onScrollBottomListener;
	}

	/**
	 */
	public interface OnScrollTopListener {
		void onScrollTop();
	}

	public void setOnScrollTopListener(OnScrollTopListener onScrollTopListener) {
		isScrollTop = true;
		this.onScrollTopListener = onScrollTopListener;
	}

}
