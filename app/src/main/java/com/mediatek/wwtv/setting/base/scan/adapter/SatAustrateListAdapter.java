package com.mediatek.wwtv.setting.base.scan.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.mediatek.wwtv.tvcenter.R;
public class SatAustrateListAdapter extends BaseAdapter {

	public String[] mList ;
	LayoutInflater inflater;
	
	public SatAustrateListAdapter(Context mContext, String[] mList) {
		super();
		this.mList = mList;
		inflater = LayoutInflater.from(mContext);
	}
	

	@Override
	public int getCount() {
		return mList.length;
	}

	@Override
	public Object getItem(int arg0) {
		return mList[arg0];
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.region_list_item, parent, false);
            holder.region_name = (TextView) convertView.findViewById(R.id.region_name);
            convertView.setTag(holder);
            
        }else{
        	holder = (ViewHolder)convertView.getTag();
        }
		holder.region_name.setText(mList[position]);
		return convertView;
	}
	


	
	class ViewHolder{
		TextView region_name;

	}
}
