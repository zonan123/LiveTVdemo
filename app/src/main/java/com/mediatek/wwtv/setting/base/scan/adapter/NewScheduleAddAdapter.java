package com.mediatek.wwtv.setting.base.scan.adapter;

import java.util.ArrayList;

import java.util.List;

import com.mediatek.wwtv.setting.base.scan.model.UpdateTime;
import com.mediatek.wwtv.setting.view.DateTimeInputView;
import com.mediatek.wwtv.setting.view.DateTimeView;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.controller.ScheduleItemModel;
public class NewScheduleAddAdapter extends BaseAdapter {

	public List<ScheduleItemModel> itemList = new ArrayList<ScheduleItemModel>();
	private Context context;
	
	
	public NewScheduleAddAdapter (Context context){
		super();
		this.context = context;
	}
	public NewScheduleAddAdapter (Context context,List<ScheduleItemModel> item){
		super();
		this.context = context;
		this.itemList = item;
	}
	public void setItem(List<ScheduleItemModel> item){
		this.itemList = item;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return itemList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return itemList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder  ;
		if(arg1==null){
			viewHolder = new ViewHolder();
			arg1 = LayoutInflater.from(context).inflate(R.layout.pvr_timeshift_schedule_item_new_layout, null);
			arg1.setTag(viewHolder);
		}else {
			viewHolder  = (ViewHolder)arg1.getTag();
		}
		viewHolder.titleTextView = (TextView)arg1.findViewById(R.id.item_title);
		viewHolder.contentTextView = (TextView)arg1.findViewById(R.id.item_content);
		viewHolder.da = (DateTimeView)arg1.findViewById(R.id.item_date_time);
		viewHolder.titleTextView.setText(itemList.get(arg0).getTitle());
		viewHolder.contentTextView.setVisibility(View.GONE);
		if(itemList.get(arg0).getTitle().equals(context
				.getString(R.string.schedule_pvr_start_date))||
				itemList.get(arg0).getTitle().equals(context
				.getString(R.string.schedule_pvr_start_time))||
				itemList.get(arg0).getTitle().equals(context
				.getString(R.string.schedule_pvr_stop_time))
				){
			viewHolder.contentTextView.setVisibility(View.VISIBLE);
			viewHolder.da.setVisibility(View.GONE);
//			viewHolder.contentTextView.setText("");
//			viewHolder.da.setText(itemList.get(arg0).getContent());
			viewHolder.contentTextView.setText(itemList.get(arg0).getContent());
		}else{
			viewHolder.contentTextView.setVisibility(View.VISIBLE);
			viewHolder.da.setVisibility(View.GONE);
			viewHolder.contentTextView.setText(itemList.get(arg0).getContent());

		}
		Log.d("NewSchedule","isEnabled->"+isEnabled(arg0));
		if(!isEnabled(arg0)){
			viewHolder.contentTextView.setTextColor(Color.GRAY);
			viewHolder.titleTextView.setTextColor(Color.GRAY);
		}else{
			viewHolder.contentTextView.setTextColor(Color.WHITE);
			viewHolder.titleTextView.setTextColor(Color.WHITE);
		}
		return arg1;
	}
	

	@Override
	public boolean isEnabled(int position) {
		return !itemList.get(position).isEnabled();
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	private class ViewHolder {
		TextView titleTextView;
		TextView contentTextView;
		DateTimeView da;
		
	}
}
