package com.mediatek.wwtv.tvcenter.dvr.controller;


import java.util.List;
import java.util.Locale;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.db.SubScheduleItemModel;

import android.content.Context;
import android.util.LayoutDirection;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.text.TextUtilsCompat;

public class SubScheduleAdapter extends BaseAdapter {
	private Context context;
	private List<SubScheduleItemModel>itemStrings;
	private boolean flag = false;

	public SubScheduleAdapter(Context context,List<SubScheduleItemModel> item){
		super();
		this.context = context;
		itemStrings = item;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return itemStrings.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return itemStrings.get(arg0);
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
			arg1 = LayoutInflater.from(context).inflate(R.layout.sub_schedule_item, null);
			arg1.setTag(viewHolder);
		}else {
			viewHolder  = (ViewHolder)arg1.getTag();
		}
		
		viewHolder.checkBox = (CheckBox)arg1.findViewById(R.id.week_type);
		viewHolder.textView = (TextView)arg1.findViewById(R.id.item_name);
		if(itemStrings.get(arg0).isVisible()){
			viewHolder.checkBox.setVisibility(View.VISIBLE);
		}else {
			viewHolder.checkBox.setVisibility(View.GONE);
		}
		if(itemStrings.get(arg0).isChecked()){
			viewHolder.checkBox.setChecked(true);
		}else{
			viewHolder.checkBox.setChecked(false);
		}
		if(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())== LayoutDirection.RTL&&flag){
			viewHolder.textView.setGravity(Gravity.END);
		}
		viewHolder.textView.setText(itemStrings.get(arg0).getItemString());
		return arg1;
	}

	public class ViewHolder{
		CheckBox checkBox;
		TextView textView;
	}

	public void setChannelFlag(boolean flag){
		this.flag = flag;
	}

	public void updateList(int index, ListView ls){
//		int fr = ls.getFirstVisiblePosition();
//		View view = ls.getChildAt(index);
//		ViewHolder viewHolder = (ViewHolder)view.getTag();
//		viewHolder.checkBox.setChecked(itemStrings.get(index).isChecked());
		notifyDataSetChanged();
//		notifyDataSetInvalidated();
	}
	
}
