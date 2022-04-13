package com.mediatek.wwtv.setting.base.scan.adapter;
import java.util.List;

import com.mediatek.wwtv.setting.util.MenuConfigManager;

import com.mediatek.wwtv.tvcenter.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ThirdItemAdapter extends BaseAdapter{

	public static final String TAG ="ThirdItemAdapter";
	Context mContext ;
	List<ThirdItem> mList ;
	
	public ThirdItemAdapter(Context mContext, List<ThirdItem> mList) {
		super();
		this.mContext = mContext;
		this.mList = mList;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ThirdItem item = mList.get(position);
		ViewHolder holder;
		if(convertView == null){
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.editdetail_list_item,
					parent, false);
			holder = new ViewHolder();
			holder.itemName = (TextView)convertView.findViewById(R.id.editdetail_name);
			holder.itemValue = (EditText)convertView.findViewById(R.id.editdetail_value);
			holder.itemOption = (TextView)convertView.findViewById(R.id.editdetail_option);
			holder.itemOpLayout =  (LinearLayout)convertView.findViewById(R.id.editdetail_option_layout);
			holder.imgOption = (ImageView)convertView.findViewById(R.id.editdetail_img);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.itemName.setText(item.title);
		if(item.optionValues != null){
			holder.itemOpLayout.setVisibility(View.VISIBLE);
			holder.itemValue.setVisibility(View.GONE);
			holder.itemOption.setText(item.optionValues[item.optionValue]);
		}else{
			holder.itemValue.setVisibility(View.VISIBLE);
			holder.itemOpLayout.setVisibility(View.GONE);
		}
		if(!item.isEnable){
			holder.itemName.setTextColor(Color.GRAY);
			holder.itemValue.setTextColor(Color.GRAY);
			holder.itemOption.setTextColor(Color.GRAY);
			holder.itemValue.setEnabled(false);
			holder.itemOption.setEnabled(false);
			holder.imgOption.setVisibility(View.GONE);
		}else{
			holder.itemName.setTextColor(Color.WHITE);
			holder.itemValue.setTextColor(Color.WHITE);
			holder.itemOption.setTextColor(Color.WHITE);
			holder.itemValue.setEnabled(true);
			holder.itemOption.setEnabled(true);
			if(item.optionValues!=null && item.optionValues.length>1){
				holder.imgOption.setVisibility(View.VISIBLE);
			}else{
				holder.imgOption.setVisibility(View.GONE);
			}
			
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "biaoqingdev:thirdadapter enable"+item.isEnable+",item.title="+item.title);
		convertView.setTag(R.id.editdetail_value, item);
		return convertView;
	}
	
	/**
	 * option turn left
	 * @param selectView
	 */
	public void  optionTurnLeft(View selectView,String[] mData){
		ThirdItem item = (ThirdItem)selectView.getTag(R.id.editdetail_value);
		if(item != null && item.optionValues !=null && item.isEnable){
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "optionTurnLeft:"+item.optionValues);
			ViewHolder holder = (ViewHolder) selectView.getTag();
			item.optionValue --;
			if(item.optionValue < 0){
				item.optionValue = item.optionValues.length -1;
			}
			holder.itemOption.setText(item.optionValues[item.optionValue]);
			if(item.id.equals(MenuConfigManager.TV_CHANNEL_AFTER_SCAN_UK_REGION)){
				item.callValueChange();
			}else if(item.id.equals(mContext.getString(R.string.scan_trd_uk_reg_reg_x, 2))){
				item.callValueChange();
			}
		}else{
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "optionTurnLeft-> not option item so do nothing");
		}
		
	}
	
	/**
	 * option turn right
	 * @param selectView
	 */
	public void  optionTurnRight(View selectView,String[] mData){
		ThirdItem item = (ThirdItem)selectView.getTag(R.id.editdetail_value);
		if(item != null && item.optionValues !=null && item.isEnable){
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "optionTurnRight:"+item.optionValues);
			ViewHolder holder = (ViewHolder) selectView.getTag();
			item.optionValue ++;
			if(item.optionValue > item.optionValues.length -1){
				item.optionValue = 0;
			}
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "optionTurnRight:text is:"+item.optionValues[item.optionValue]);
			holder.itemOption.setText(item.optionValues[item.optionValue]);
			if(item.id.equals(MenuConfigManager.TV_CHANNEL_AFTER_SCAN_UK_REGION)){
				item.callValueChange();
			}else if(item.id.equals(mContext.getString(R.string.scan_trd_uk_reg_reg_x, 2))){
				item.callValueChange();
			}
		}else{
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "optionTurnRight-> not option item so do nothing");
		}
	}
	
	class ViewHolder {
		TextView itemName;
		EditText itemValue;
		TextView itemOption;
		LinearLayout itemOpLayout;
		ImageView imgOption;
	}
	
	public static class ThirdItem{
		public String id;
		public String title;
		public int optionValue;
		public String[] optionValues;
		public boolean isEnable;
		OnValueChangeListener listener;
		
		public ThirdItem(String id, String title, int optionValue,
				String[] optionValues, boolean isEnable) {
			super();
			this.id = id;
			this.title = title;
			this.optionValue = optionValue;
			this.optionValues = optionValues;
			this.isEnable = isEnable;
		}
		
		public void setValueChangeListener(OnValueChangeListener ler){
			listener = ler;
		}
		
		public void callValueChange(){
			if(listener != null){
				listener.afterValueChanged(optionValues[optionValue]);
			}
		}
		
		public interface OnValueChangeListener{
			void  afterValueChanged(String afterName);
		}
		
	}

}
