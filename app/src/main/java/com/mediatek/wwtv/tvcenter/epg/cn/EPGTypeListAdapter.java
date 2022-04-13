package com.mediatek.wwtv.tvcenter.epg.cn;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.wwtv.tvcenter.util.SaveValue;

import android.content.Context;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class EPGTypeListAdapter extends BaseAdapter {

	private static final String TAG = "EPGListAdapter";
	List<EPGListViewDataItem> mData;
	Context mContext;
	SaveValue sv;

	ListView mList;
	ListView sList;

	ViewHolder mViewHolder;

	static int mPosition = 0;
	static int sPosition = 0;
	private static boolean mFocus = true;

	EPGTypeListAdapter(Context context) {
		mContext = context;
		sv = SaveValue.getInstance(context);

	}

	public EPGTypeListAdapter(Context context, ListView mainList,
			ListView subList) {
		mContext = context;
		mList = mainList;
		sList = subList;
		sv = SaveValue.getInstance(context);
	}

	public boolean isMfocus() {
		return mFocus;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		if (mData == null) {
			return 0;
		}
		return mData.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mData == null ? null : mData.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		if (convertView == null) {
			mViewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.epg_sa_type_item_layout, null);
			mViewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.epg_type_icon);
			mViewHolder.mTextView = (TextView) convertView
					.findViewById(R.id.epg_type_name);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		if (mData != null && !mData.isEmpty()) {
			if (null !=mData.get(position)
					 &&  mData.get(position).data.length() != 0) {
				if (mViewHolder.mTextView != null) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- Text View is null ---");
				mViewHolder.mTextView.setText(mData.get(position).data);
				mViewHolder.mTextView.setTextColor(Color.LTGRAY);
				com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "++++++++++ getView() Position: " + position
						+ "    Data: "
						+ sv.readBooleanValue(mData.get(position).data, false));
				if (sv.readBooleanValue(mData.get(position).data, false)) {
					mViewHolder.imageView.setVisibility(View.VISIBLE);
				} else {
					mViewHolder.imageView.setVisibility(View.INVISIBLE);
				}
			 }
			}

		}
		convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)(ScreenConstant.SCREEN_HEIGHT*0.85*0.83/14)));
		return convertView;
	}

	class ViewHolder {
		ImageView imageView;
		TextView mTextView;
	}

	public List<EPGListViewDataItem> getEPGData() {
		return mData;
	}

	public void setEPGGroup(List<EPGListViewDataItem> data) {
		this.mData = data;
	}

	public List<EPGListViewDataItem> loadEPGFilterTypeData(String country) {
		String[][] sType = new String[11][20];
		String[] mType = null;
		mType = mContext.getResources().getStringArray(
				R.array.nav_epg_filter_type_cn);
		sType[0] = mContext.getResources().getStringArray(
				R.array.nav_epg_subtype_movie_cn);
		sType[1] = mContext.getResources().getStringArray(
				R.array.nav_epg_subtype_news_cn);
		sType[2] = mContext.getResources().getStringArray(
				R.array.nav_epg_subtype_show_cn);
		sType[3] = mContext.getResources().getStringArray(
				R.array.nav_epg_subtype_sports_cn);
		sType[4] = mContext.getResources().getStringArray(
				R.array.nav_epg_subtype_children_cn);
		sType[5] = mContext.getResources().getStringArray(
				R.array.nav_epg_subtype_music_cn);
		sType[6] = mContext.getResources().getStringArray(
				R.array.nav_epg_subtype_arts_cn);
		sType[7] = mContext.getResources().getStringArray(
				R.array.nav_epg_subtype_social_cn);
		sType[8] = mContext.getResources().getStringArray(
				R.array.nav_epg_subtype_education_cn);
		sType[9] = mContext.getResources().getStringArray(
				R.array.nav_epg_subtype_leisure_cn);
		sType[10] = mContext.getResources().getStringArray(
				R.array.nav_epg_subtype_special_cn);
		List<EPGListViewDataItem> mDataGroup = new ArrayList<EPGListViewDataItem>();
		for (int i = 0; i < mType.length; i++) {
			EPGListViewDataItem mTypeData = new EPGListViewDataItem(mType[i]);
			List<EPGListViewDataItem> mSubTypeData = new ArrayList<EPGListViewDataItem>();
			for (int j = 0; j < sType[i].length; j++) {
				EPGListViewDataItem sTypeData = new EPGListViewDataItem(
						sType[i][j]);
				mSubTypeData.add(sTypeData);
			}
			mTypeData.setSubChildDataItem(mSubTypeData);
			mDataGroup.add(mTypeData);

		}
		return mDataGroup;

	}

	public class EPGListViewDataItem {
		protected String data;
		boolean marked = false;
		List<EPGListViewDataItem> mSubChildDataItem;

		public List<EPGListViewDataItem> getSubChildDataItem() {
			return mSubChildDataItem;
		}

		public void setSubChildDataItem(
				List<EPGListViewDataItem> mSubChildDataItem) {
			this.mSubChildDataItem = mSubChildDataItem;
		}

		public EPGListViewDataItem(String data) {
			this.data = data;
		}

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}

		public boolean isMarked() {
			return marked;
		}

		public void setMarked(boolean marked) {
			this.marked = marked;
		}
	}

	public void onMainKey(View v, int keyCode) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ==== main list select item position: " + mPosition + ">>>" + sPosition + ">>>" + mFocus);
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_RIGHT:
            if(sList == null){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"do not have sub type,so do nothing!");
                break;
            }
			if (mFocus) {
				mList.clearFocus();
				mList.setFocusable(false);
				sPosition = 0;
				((TextView) ((LinearLayout) mList.getSelectedView())
						.getChildAt(1)).setTextColor(Color.YELLOW);
				if(sList != null)
				{
                    sList.setFocusable(true);
					sList.setSelection(sPosition);
					sList.requestFocus();
				}
				mFocus = false;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, " ==== main list item nums: " + mList.getAdapter().getCount());
			if (mFocus) {
				mPosition--;
				if (mPosition < 0) {
					mPosition = mList.getAdapter().getCount() - 1;
					com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, " main list select item position: "
							+ mPosition);
					mList.setSelection(mPosition);
				}
			}

			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if (mFocus) {
				mPosition++;
				if (mPosition >= mList.getAdapter().getCount()) {
					mPosition = 0;
					com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, " main list select item position: "
							+ mPosition);
					mList.setSelection(mPosition);
				}
			}
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_E:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Position: " + mPosition + "      is selected: "
					+ mData.get(mPosition).marked);
			EpgType.mHasEditType = true;
			if (sv.readBooleanValue(mData.get(mPosition).data, false)) {
				mData.get(mPosition).marked = false;
				((LinearLayout) mList.getSelectedView()).getChildAt(0)
						.setVisibility(View.INVISIBLE);
				sv.saveBooleanValue(mData.get(mPosition).data, false);
				for (int i = 0; i < mData.get(mPosition).getSubChildDataItem()
						.size(); i++) {
					sv.saveBooleanValue(mData.get(mPosition)
							.getSubChildDataItem().get(i).data, false);
				}
				if(sList != null)
				{
					((BaseAdapter) sList.getAdapter()).notifyDataSetChanged();
				}

			} else {
				mData.get(mPosition).marked = true;

				((LinearLayout) mList.getSelectedView()).getChildAt(0)
						.setVisibility(View.VISIBLE);
				sv.saveBooleanValue(mData.get(mPosition).data, true);
			}
			break;
			default:
			  break;
		}
	}

	public void onSubKey(View v, int keyCode) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ==== sub list select item position: " + mPosition + ">>>" + sPosition + ">>>" + mFocus);
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (!mFocus) {
				sList.clearFocus();
				sList.setFocusable(false);
				mList.setFocusable(true);
				mList.setSelection(mPosition);
				mList.requestFocus();
				((TextView) ((LinearLayout) mList.getSelectedView())
						.getChildAt(1)).setTextColor(Color.LTGRAY);
				sPosition = 0;
				mFocus = true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if (!mFocus) {
				sPosition++;
				if (sPosition >= sList.getAdapter().getCount()) {
					sPosition = 0;
					sList.setSelection(sPosition);
				}
			}
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			if (!mFocus) {
				sPosition--;
				if (sPosition < 0) {
					sPosition = sList.getAdapter().getCount() - 1;
					sList.setSelection(sPosition);
				}
			}
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_E:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Sub List Position: " + sPosition
					+ "      is selected: " + mData.get(sPosition).marked);
			EpgType.mHasEditType = true;
			if (sv.readBooleanValue(mData.get(sPosition).data, false)) {
				mData.get(sPosition).marked = false;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "------- sub list not marked ---------");
				((LinearLayout) sList.getSelectedView()).getChildAt(0)
						.setVisibility(View.INVISIBLE);
				sv.saveBooleanValue(mData.get(sPosition).data, false);

			} else {
				mData.get(sPosition).marked = true;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "-------sub list is marked ---------");
				((LinearLayout) sList.getSelectedView()).getChildAt(0)
						.setVisibility(View.VISIBLE);
				sv.saveBooleanValue(mData.get(sPosition).data, true);
				setMainItemVisible();
			}
			break;
			default:
			  break;
		}
	}

	public void setMainItemVisible() {
		((EPGTypeListAdapter) mList.getAdapter()).mData.get(mPosition).marked = true;
		sv.saveBooleanValue(((EPGTypeListAdapter) mList.getAdapter()).mData
				.get(mPosition).data, true);
		((EPGTypeListAdapter) mList.getAdapter()).notifyDataSetChanged();
	}
}
