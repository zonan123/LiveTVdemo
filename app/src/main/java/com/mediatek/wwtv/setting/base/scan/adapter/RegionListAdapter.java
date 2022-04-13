package com.mediatek.wwtv.setting.base.scan.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvDvbChannelInfo;
public class RegionListAdapter extends BaseAdapter {

	private static final String TAG = "RegionListAdapter";
	private final Context mContext;
	public List<TIFChannelInfo> mList ;
	LayoutInflater inflater;
	
	public RegionListAdapter(Context mContext, List<TIFChannelInfo> mList) {
		super();
		this.mContext = mContext;
		this.mList = mList;
		inflater = LayoutInflater.from(mContext);
	}
	

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mList.get(arg0);
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
		TIFChannelInfo item = mList.get(position);
		String svcProName = "¡ª¡ª¡ª¡ª";
		MtkTvChannelInfoBase channelInfoBase = item.mMtkTvChannelInfo;
		if(channelInfoBase instanceof MtkTvDvbChannelInfo){
			MtkTvDvbChannelInfo dvbchIn = (MtkTvDvbChannelInfo)channelInfoBase;
			svcProName = dvbchIn.getSvcProName();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "svcProName" + svcProName);
		}
		holder.region_name.setText(getServiceShowName(svcProName));
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showName" + getServiceShowName(svcProName));
		return convertView;
	}
	


	
	class ViewHolder{
		TextView region_name;

	}
	
	public String getServiceShowName(String name){
		String wien = mContext.getResources().getString(R.string.wueb);
		String niederosterreich = mContext.getResources().getString(R.string.niederosterreich);
		String burgenland = mContext.getResources().getString(R.string.burgenland);
		String oberosterreich = mContext.getResources().getString(R.string.oberosterreich);
		String salzburg = mContext.getResources().getString(R.string.salzburg);
		String tirol = mContext.getResources().getString(R.string.tirol);
		String vorarlberg = mContext.getResources().getString(R.string.vorarlberg);
		String steiermark = mContext.getResources().getString(R.string.steiermark);
		String karntern = mContext.getResources().getString(R.string.karntern);
		
		if("ORF [Region 1]".equals(name)){
			return wien;
		}
		if("ORF [Region 2]".equals(name)){
			return niederosterreich;
		}
		if("ORF [Region 3]".equals(name)){
			return burgenland;
		}
		if("ORF [Region 4]".equals(name)){
			return oberosterreich;
		}
		if("ORF [Region 5]".equals(name)){
			return salzburg;
		}
		if("ORF [Region 6]".equals(name)){
			return tirol;
		}
		if("ORF [Region 7]".equals(name)){
			return vorarlberg;
		}
		if("ORF [Region 8]".equals(name)){
			return steiermark;
		}
		if("ORF [Region 9]".equals(name)){
			return karntern;
		}
		return name;
	}
	

}
