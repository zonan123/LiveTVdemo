package com.mediatek.wwtv.setting.base.scan.adapter;

import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mediatek.wwtv.setting.util.Util;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;

public class StateScheduleListAdapter<T> extends BaseAdapter  {

	private final List<T> mDiskList;
	private final LayoutInflater mInflater;
	private final Context mContext;

	public StateScheduleListAdapter(Context mContext, List<T> itemList) {
		super();
		this.mDiskList = itemList;
		this.mContext = mContext;
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/*
	 *
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDiskList.size();
	}

	/*
	 *
	 */
	@Override
	public T getItem(int position) {
		// TODO Auto-generated method stub
		return mDiskList.get(position);
	}

	/*
	 *
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 *
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view;

		if (convertView == null) {
			view = mInflater.inflate(R.layout.pvr_tshfit_schudule_item_layout,
					null);
		} else {
			view = convertView;
		}

		setItemValue(view, (MtkTvBookingBase) getItem(position));
		return view;
	}

	/**
	 * id:1001365643949250
	 * @param view
	 * @param item
	 */
	private void setItemValue(View view, MtkTvBookingBase item) {
		TextView label = (TextView) view
				.findViewById(R.id.schedule_channel_name);
		if(item.getSourceType()==0){
			label.setText(String.format("CH%s", item.getEventTitle()));
		}else{
			label.setVisibility(View.GONE);
		}

		TextView size = (TextView) view.findViewById(R.id.schedule_date);
        size.setText(Util.longStrToTimeStrN(item.getRecordStartTime() * 1000));

		TextView duration = (TextView) view
				.findViewById(R.id.schedule_duration);


        duration.setText(Util.longToHrMinN(item.getRecordDuration()));

		TextView repeatType = (TextView) view
				.findViewById(R.id.schedule_internel);
		String[] repeat = mContext.getResources().getStringArray(
				R.array.pvr_tshift_repeat_type);
		if(item.getRepeatMode()==128){
			repeatType.setText(repeat[0]);
		}else if(item.getRepeatMode()==0){
			repeatType.setText(repeat[2]);
		}else{
			repeatType.setText(repeat[1]);
		}

		TextView scheduleType = (TextView) view
				.findViewById(R.id.schedule_notification);
		String[] schedule = mContext.getResources().getStringArray(
				R.array.pvr_tshift_schedule_type);
		String type=schedule[item.getRecordMode()==2 ? 0 :item.getRecordMode()];
		scheduleType.setText(type);

	}

}
