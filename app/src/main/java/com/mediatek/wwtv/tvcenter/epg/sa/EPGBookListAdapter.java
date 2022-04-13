package com.mediatek.wwtv.tvcenter.epg.sa;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.sa.db.EPGBookListViewDataItem;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * program booked listAdapter
 * @author sin_xinsheng
 *
 */
public class EPGBookListAdapter extends BaseAdapter {
	private static final String TAG = "EPGBookListAdapter";
	private List<EPGBookListViewDataItem> mBookedListData;
	private Context mContext;
	private ListView mListView;
	private ViewHolder mViewHolder;

	EPGBookListAdapter(Context context) {
		mContext = context;
		mBookedListData = new ArrayList<EPGBookListViewDataItem>();
	}

	public EPGBookListAdapter(Context context, ListView mainList) {
		mContext = context;
		mListView = mainList;
		mBookedListData = new ArrayList<EPGBookListViewDataItem>();
	}
	
	public EPGBookListAdapter(Context context, ListView mainList, List<EPGBookListViewDataItem> programList) {
		mContext = context;
		mListView = mainList;
		mBookedListData = programList;
	}
	
	public void addBookedProgram(EPGBookListViewDataItem tempItem) {
		mBookedListData.add(tempItem);
	}

	public int getCount() {
		if (mBookedListData == null) {
			return 0;
		}
		return mBookedListData.size();
	}

	public Object getItem(int position) {
		return mBookedListData == null ? null : mBookedListData.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			mViewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.epg_book_item_layout, null);
			mViewHolder.imageView = (ImageView) convertView.findViewById(R.id.epg_book_icon);
			mViewHolder.mChannelTextView = (TextView) convertView.findViewById(R.id.epg_book_channel_name);
			mViewHolder.mProgramTextView = (TextView) convertView.findViewById(R.id.epg_book_program_name);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		if (mBookedListData != null && !mBookedListData.isEmpty()) {
			EPGBookListViewDataItem tempItem = mBookedListData.get(position);
			if (tempItem != null) {
				if (tempItem.marked) {
					mViewHolder.imageView.setVisibility(View.VISIBLE);
				} else {
					mViewHolder.imageView.setVisibility(View.INVISIBLE);
				}
				mViewHolder.mChannelTextView.setText(tempItem.mChannelNoName);
				mViewHolder.mProgramTextView.setText(tempItem.mProgramName);
			}
		}
		convertView.setLayoutParams(new LayoutParams(650*ScreenConstant.SCREEN_WIDTH /1280, (int)(ScreenConstant.SCREEN_HEIGHT*0.85*0.83/14)));
		return convertView;
	}

	class ViewHolder {
		public ImageView imageView;
		public TextView mChannelTextView;
		public TextView mProgramTextView;
	}

	public List<EPGBookListViewDataItem> getEPGBookListData() {
		return mBookedListData;
	}

	public void setEPGBookList(List<EPGBookListViewDataItem> data) {
		this.mBookedListData = data;
	}

	public void onKey(View v, int keyCode) {
		switch (keyCode) {
//		case KeyEvent.KEYCODE_DPAD_UP:
//			mPosition--;
//			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ==== main list select item position: " + mPosition);
//			com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, " ==== main list item nums: " + mListView.getAdapter().getCount());
//			if (mPosition < 0) {
//				mPosition = mListView.getAdapter().getCount() - 1;
//				com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, " main list select item position: " + mPosition);
//				mListView.setSelection(mPosition);
//			}
//			break;
//		case KeyEvent.KEYCODE_DPAD_DOWN:
//			mPosition++;
//			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ==== main list select item position: " + mPosition);
//			if (mPosition >= mListView.getAdapter().getCount()) {
//				mPosition = 0;
//				com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, " main list select item position: " + mPosition);
//				mListView.setSelection(mPosition);
//			}
//			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_E:
			int mPosition = mListView.getSelectedItemPosition();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Position: " + mPosition + "      is selected: " + mBookedListData.get(mPosition).marked);
			if (mBookedListData.get(mPosition).marked) {
				mBookedListData.get(mPosition).marked = false;
			} else {
				mBookedListData.get(mPosition).marked = true;
			}
			notifyDataSetChanged();
			break;
			default:
			  break;
		}
	}
	
}
