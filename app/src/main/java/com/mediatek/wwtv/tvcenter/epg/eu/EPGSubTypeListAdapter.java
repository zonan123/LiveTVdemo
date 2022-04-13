package com.mediatek.wwtv.tvcenter.epg.eu;

import java.util.List;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.EPGConfig;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGTypeListAdapter.EPGListViewDataItem;
import com.mediatek.wwtv.tvcenter.util.SaveValue;


import android.content.Context;
import android.graphics.Color;
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

public class EPGSubTypeListAdapter extends BaseAdapter {

	private static final String TAG = "EPGSubTypeListAdapter";
	
	Context mContext;
	SaveValue sv;
	ViewHolder mViewHolder;
	private List<EPGListViewDataItem> sData;

	int sPosition = 0;
	ListView sList;
	private String mark = "";

	EPGSubTypeListAdapter(Context context,ListView subList) {
		mContext = context;
		sv = SaveValue.getInstance(context);
		sPosition = 0;
		sList = subList;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		if (sData == null) {
			return 0;
		}
		return sData.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return sData == null ? null : sData.get(position);
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
		if (sData != null && !sData.isEmpty()) {
			if (null !=sData.get(position)
					 &&  sData.get(position).data.length() != 0) {
				if (mViewHolder.mMainType != null) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- Text View is null ---");
				mViewHolder.mMainType.setText(sData.get(position).data);
				mViewHolder.mMainType.setTextColor(Color.LTGRAY);
				mViewHolder.checkBox.setVisibility(View.VISIBLE);
				mViewHolder.mMainTypeDetail.setVisibility(View.GONE);
				com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "++++++++++ getView() Position: " + position
						+ "    Data: "
						+ sv.readBooleanValue(String.format("%s%s%s", EPGConfig.PREFIX, mark, sData.get(position).data), false));
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "guanglei sData position:"+ position + sData.get(position).data + mark
						+ ", sv.readBooleanValue(sData.get(position).data, false): "+sv.readBooleanValue(String.format("%s%s%s", EPGConfig.PREFIX, mark, sData.get(position).data), false));
				if (sv.readBooleanValue(String.format("%s%s%s", EPGConfig.PREFIX, mark, sData.get(position).data), false)) {
					mViewHolder.checkBox.setChecked(true);
				} else {
					mViewHolder.checkBox.setChecked(false);
				}

			 }
        }

      }
		convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)(getHeight())));//ScreenConstant.SCREEN_HEIGHT/10
		return convertView;
	}

	class ViewHolder {
		CheckBox checkBox;
		TextView mMainType;
		TextView mMainTypeDetail;
	}

	public void setEPGGroup(List<EPGListViewDataItem> subChildDataItem) {
		sData = subChildDataItem;
	}

	public void setSelectedMainType(String mainType) {
		mark = mainType;
	}

	public void resetPosition() {
		sPosition = 0;
		sList.setSelection(0);
	}
	
	public void onSubKey(View v, int keyCode) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ==== sub list select item position>>>" + sPosition);
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_DOWN:
			sPosition++;
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " ==== sub sList.getAdapter().getCount():" + sList.getAdapter().getCount() + "  sPosition:" + sPosition);
			if (sPosition >= sList.getAdapter().getCount()) {
				sPosition = 0;
				sList.setSelection(sPosition);
			}
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			sPosition--;
			if (sPosition < 0) {
				sPosition = sList.getAdapter().getCount() - 1;
				sList.setSelection(sPosition);
			}
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_E:
			int pos = sList.getSelectedItemPosition();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Sub List Position: " + pos
					+ "      is selected: " + sData.get(pos).marked
					+ ", mark: " + mark
					+ ",sData.get(sPosition).data:"+sData.get(pos).data
					+ ",guanglei sv.readBooleanValue(EPGConfig.PREFIX + mark + sData.get(pos).data, false): "
					+ sv.readBooleanValue(String.format("%s%s%s", EPGConfig.PREFIX, mark, sData.get(pos).data), false));
			
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"guanglei, pos: "+ pos + ",sPosition: " + sPosition);
			EpgType.mHasEditType = true;
			if (sv.readBooleanValue(String.format("%s%s%s", EPGConfig.PREFIX, mark, sData.get(pos).data), false)) {
				sData.get(pos).marked = false;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "------- sub list not marked ---------");
				((CheckBox)(((RelativeLayout) sList.getSelectedView()).getChildAt(1))).setChecked(false);
				sv.saveBooleanValue(String.format("%s%s%s", EPGConfig.PREFIX, mark, sData.get(pos).data, mark), false);

			} else {
				sData.get(pos).marked = true;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "-------sub list is marked ---------");
				((CheckBox)(((RelativeLayout) sList.getSelectedView()).getChildAt(1))).setChecked(true);
				sv.saveBooleanValue(String.format("%s%s%s", EPGConfig.PREFIX, mark, sData.get(pos).data), true);
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

    com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "curDensity: " + curDensity + "   heightPixels: "
        + heightPixels +", 9 item>>>"+(heightPixels - 90 * curDensity- curDensity*(9-1))/9);
    return (heightPixels - 90 * curDensity - curDensity*(9-1))/9;
  }

}
