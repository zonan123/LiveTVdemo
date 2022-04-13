package com.mediatek.wwtv.tvcenter.epg.eu;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.epg.EPGConfig;
import com.mediatek.wwtv.tvcenter.util.SaveValue;


import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EPGTypeListAdapter extends BaseAdapter {

	private static final String TAG = "EPGTypeListAdapter";
	List<EPGListViewDataItem> mData;
	Context mContext;
	boolean isSubDisabled = false;
	String mainDetail = "";
	SaveValue sv;
	ListView mList;
	ViewHolder mViewHolder;

	int mPosition = 0;
	int sPosition = 0;

	EPGTypeListAdapter(Context context) {
		mContext = context;
		sv = SaveValue.getInstance(context);

	}

	public EPGTypeListAdapter(Context context, ListView mainList, boolean disabled) {
		mContext = context;
		mList = mainList;
		isSubDisabled = disabled;
		mPosition = 0;
		sPosition = 0;
		sv = SaveValue.getInstance(context);
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
					R.layout.epg_type_item_layout, null);
			mViewHolder.mMainTypeDetail = (TextView)convertView.findViewById(R.id.epg_sub_type_name);
			mViewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
			mViewHolder.mMainType = (TextView) convertView.findViewById(R.id.epg_type_name);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		if (mData != null && !mData.isEmpty()) {
			if (null !=mData.get(position)
					 &&  mData.get(position).data.length() != 0) {
				if (mViewHolder.mMainType != null) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- Text View is null ---");
				mViewHolder.mMainType.setText(mData.get(position).data);
				mViewHolder.mMainType.setTextColor(Color.LTGRAY);
				com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "++++++++++ getView() Position: " + position
						+ "    Data: "
						+ sv.readBooleanValue(String.format("%s%s", EPGConfig.PREFIX, mData.get(position).data), false));
				if (isSubDisabled) {
					if (sv.readBooleanValue(String.format("%s%s", EPGConfig.PREFIX, mData.get(position).data), false)) {
						mViewHolder.checkBox.setChecked(true);
					} else {
						mViewHolder.checkBox.setChecked(false);
					}
				} else {
					mViewHolder.checkBox.setVisibility(View.INVISIBLE);
					mViewHolder.mMainTypeDetail.setVisibility(View.VISIBLE);
					String detail = "";
					for (int i = 0; i < mData.get(position).detailList.size(); i++) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "++++guanglei+++++ getView() readBooleanValue: " + sv.readBooleanValue(String.format("%s%s", EPGConfig.PREFIX, mData.get(position).detailList.get(i))));
						if(sv.readBooleanValue(String.format("%s%s%s", EPGConfig.PREFIX, mData.get(position).getData(), mData.get(position).detailList.get(i)), false)){
                            detail = detail + "/" + mData.get(position).detailList.get(i);
						}
					}
					if(detail.startsWith("/")){
						detail = detail.substring(1);
					}
                    mViewHolder.mMainTypeDetail.setText(TextUtils.equals(detail,"") ? mContext.getString(R.string.epg_type_none) : detail);
				}
			 }
        }

      }
		convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)(getHeight())));//ScreenConstant.SCREEN_HEIGHT
		return convertView;
	}

	class ViewHolder {
	  CheckBox checkBox;
		TextView mMainType;
		TextView mMainTypeDetail;
	}

	public List<EPGListViewDataItem> getEPGData() {
		return mData;
	}

	public List<EPGListViewDataItem> loadEPGFilterTypeData() {
		this.mData = DataReader.getInstance(mContext).loadEPGFilterTypeData(mList, isSubDisabled);
		return mData;
	}

	public class EPGListViewDataItem {
		protected String data;
		boolean marked = false;
		protected List<String> detailList = new ArrayList<String>();
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
		
		public void setDetail(List<String> detailList) {
			this.detailList = detailList;
		}
		
		public List<String> getDetailList() {
			return detailList;
		}
	}

	public void onMainKey(View v, int keyCode) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ==== main list select item position: " + mPosition + ">>>" + sPosition);
		switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_UP:
			com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, " ==== main list item nums: " + mList.getAdapter().getCount());
			mPosition--;
			if (mPosition < 0) {
				mPosition = mList.getAdapter().getCount() - 1;
				com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, " main list select item position: "
						+ mPosition);
				mList.setSelection(mPosition);
			}
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			mPosition++;
			if (mPosition >= mList.getAdapter().getCount()) {
				mPosition = 0;
				com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, " main list select item position: "
						+ mPosition);
				mList.setSelection(mPosition);
			}
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_E:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Position: " + mPosition + "      is selected: "
					+ mData.get(mPosition).marked);
			EpgType.mHasEditType = true;
			if (isSubDisabled) {
				if (sv.readBooleanValue(String.format("%s%s", EPGConfig.PREFIX, mData.get(mPosition).data), false)) {
					mData.get(mPosition).marked = false;
					((CheckBox)(((RelativeLayout) mList.getSelectedView()).getChildAt(1))).setChecked(false);
					sv.saveBooleanValue(String.format("%s%s", EPGConfig.PREFIX, mData.get(mPosition).data), false);
				} else {
					mData.get(mPosition).marked = true;
					
					((CheckBox)(((RelativeLayout) mList.getSelectedView()).getChildAt(1))).setChecked(true);
					sv.saveBooleanValue(String.format("%s%s", EPGConfig.PREFIX, mData.get(mPosition).data), true);
				}
			}
			break;
			default:
			  break;
		}
	}

	// cal height
  public float getHeight() {
    float curDensity = mContext.getResources().getDisplayMetrics().density;
    float heightPixels = mContext.getResources().getDisplayMetrics().heightPixels;

    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "curDensity: " + curDensity + " heightPixels: "
        + heightPixels +", 7 item>>>"+(heightPixels - 90 * curDensity - curDensity*(7-1))/7);
    return (heightPixels - 90 * curDensity - curDensity*(7-1))/7;
  }
}
