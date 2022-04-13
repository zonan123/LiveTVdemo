package com.mediatek.wwtv.tvcenter.epg.eu;

import android.content.Context;

import com.mediatek.wwtv.setting.util.Util;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.epg.EPGBaseAdapter;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;
import com.mediatek.wwtv.tvcenter.epg.EPGUtil;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;



public class EPGEuEventAdapter extends EPGBaseAdapter<EPGProgramInfo> {
	private static final String TAG = "EPGEuEventAdapter";

	public EPGEuEventAdapter(Context context) {
		super(context);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mViewHolder;
		if (convertView == null) {
			mViewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.epg_eu_2nd_listitem_textview, null);
			mViewHolder.llayoutGroup = (LinearLayout) convertView.findViewById(R.id.epg_2nd_list_item);
			mViewHolder.time = (TextView) convertView.findViewById(R.id.epg_2nd_list_item_time);
			mViewHolder.title = (TextView) convertView.findViewById(R.id.epg_2nd_list_item_title);
			AbsListView.LayoutParams lp=new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
			mViewHolder.llayoutGroup.setLayoutParams(lp);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		EPGProgramInfo programInfo=group.get(position);
		boolean highlight=highlightEpgType(programInfo);
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "highlight="+highlight);
		mViewHolder.llayoutGroup.setBackgroundResource(highlight? R.drawable.epg_2nd_event_highlight_bg:R.drawable.epg_2nd_event_bg);
    String startTime=EPGUtil.judgeFormatTime(mContext)==1?programInfo.getmStartTimeStr():Util.formatTime24To12(programInfo.getmStartTimeStr());
    String endTime=EPGUtil.judgeFormatTime(mContext)==1?programInfo.getmEndTimeStr():Util.formatTime24To12(programInfo.getmEndTimeStr());
    mViewHolder.time.setText(String.format("%s-%s", startTime, endTime));
		String title=TextUtils.isEmpty(programInfo.getmTitle())?mContext.getResources().getString(R.string.nav_epg_no_program_title):programInfo.getmTitle();
		mViewHolder.title.setText(title);
		return convertView;
	}

	class ViewHolder {
		LinearLayout llayoutGroup;
		TextView time;
		TextView title;
	}

	private boolean highlightEpgType(EPGProgramInfo programInfo){
		int mainType = programInfo.getMainType();
		int subType = programInfo.getSubType();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mainType="+mainType+",subType="+subType);
		if(mainType<1){
			return false;
		}
		int mainTypeLength=DataReader.getInstance(mContext).getMainType().length;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mainTypeLength="+mainTypeLength);
		if(mainType >= mainTypeLength){
			return false;
		}
		String mainStr=DataReader.getInstance(mContext).getMainType()[mainType-1];
		String subStr=null;
		if (subType >= 0 && subType < DataReader.getInstance(mContext).getSubType().length) {
			subStr = DataReader.getInstance(mContext).getSubType()[mainType-1][subType];
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mainStr="+mainStr+",subStr="+subStr);
		if (SaveValue.getInstance(mContext).readBooleanValue(mainStr, false) && !subHasSelected(mainType-1)) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subHasSelected");
			return true;
		} else if (SaveValue.getInstance(mContext).readBooleanValue(mainStr, false) && thisSubSelected(mainType-1, subType)) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "thisSubSelected");
			return true;
		} 
		return false;
	}
	
	private boolean subHasSelected(int mainTypeIndex) {
		String subType[] = DataReader.getInstance(mContext).getSubType()[mainTypeIndex];
		if (subType != null) {
		  for (String string : subType) {
		    if (SaveValue.getInstance(mContext).readBooleanValue(string, false)) {
		      return true;
		    }
      }
		}
		return false;
	}

	private boolean thisSubSelected(int mainTypeIndex, int subTypeIndex) {
		String subType[] = DataReader.getInstance(mContext).getSubType()[mainTypeIndex];
		if (subType != null) {
			if (subTypeIndex >= 0 && subTypeIndex < subType.length) {
				return SaveValue.getInstance(mContext).readBooleanValue(subType[subTypeIndex], false);
			}
		}
		return false;
	}
}
