package com.mediatek.wwtv.tvcenter.epg.eu;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.mediatek.twoworlds.tv.model.MtkTvAnalogChannelInfo;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.EPGBaseAdapter;
import com.mediatek.wwtv.tvcenter.epg.EPGChannelInfo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;



public class EPGEuChannelAdapter extends EPGBaseAdapter<EPGChannelInfo> {
//	private static final String TAG = "EPGEuChannelAdapter";
	private Drawable mAnalogIcon;
	private int mSelPosNoFocus=-1; //select position but without focus.

	
	public void setSelPosNoFocus(int position){
		mSelPosNoFocus=position;
		notifyDataSetChanged();
	}
	
	public EPGEuChannelAdapter(Context context) {
		super(context);
		mAnalogIcon = context.getResources().getDrawable(R.drawable.epg_channel_icon);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mViewHolder;
		if (convertView == null) {
			mViewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.epg_eu_2nd_listview_item_layout, null);
			mViewHolder.llayoutGroup = (LinearLayout) convertView.findViewById(R.id.epg_channel_item);
			mViewHolder.icon = (ImageView) convertView.findViewById(R.id.epg_radio_icon);
			mViewHolder.number = (TextView) convertView.findViewById(R.id.epg_channel_number);
			mViewHolder.name = (TextView) convertView.findViewById(R.id.epg_channel_name);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
    AbsListView.LayoutParams lp=new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
    mViewHolder.llayoutGroup.setLayoutParams(lp);
    Log.d("EPGEuChannelAdapter", "for fix channel list show abnomal.");
		EPGChannelInfo channelInfo=group.get(position);
		if (channelInfo.getTVChannel()==null) {
//            Drawable nothingIcon = mContext.getResources().getDrawable(R.drawable.translucent_background);
//            nothingIcon.setBounds(0, 0, mAnalogIcon.getMinimumWidth(), mAnalogIcon.getMinimumWidth());
//            mViewHolder.number.setCompoundDrawables(nothingIcon, null, null, null);
            mViewHolder.icon.setVisibility(View.INVISIBLE);
        }
        else if (channelInfo.getTVChannel().isRadioService()) {
			Drawable radioIcon = mContext.getResources().getDrawable(R.drawable.epg_radio_channel_icon);
			radioIcon.setBounds(0, 0, mAnalogIcon.getMinimumWidth(), mAnalogIcon.getMinimumWidth());
//			mViewHolder.number.setCompoundDrawables(radioIcon, null, null, null);
	          mViewHolder.icon.setVisibility(View.VISIBLE);
	          mViewHolder.icon.setImageDrawable(radioIcon);
		} else if (channelInfo.getTVChannel() instanceof MtkTvAnalogChannelInfo) {
//			Drawable analogIcon = mContext.getResources().getDrawable(R.drawable.epg_channel_icon);
//			analogIcon.setBounds(0, 0, analogIcon.getMinimumWidth(), analogIcon.getMinimumWidth());
//			mViewHolder.number.setCompoundDrawables(analogIcon, null, null, null);
	          mViewHolder.icon.setVisibility(View.VISIBLE);
	          mViewHolder.icon.setImageDrawable(mAnalogIcon);
		} else {
//			Drawable nothingIcon = mContext.getResources().getDrawable(R.drawable.translucent_background);
//			nothingIcon.setBounds(0, 0, mAnalogIcon.getMinimumWidth(), mAnalogIcon.getMinimumWidth());
//			mViewHolder.number.setCompoundDrawables(nothingIcon, null, null, null);
			mViewHolder.icon.setVisibility(View.INVISIBLE);
		}
		if(mSelPosNoFocus==position){
			mViewHolder.number.setTextColor(mContext.getResources().getColor(R.color.yellow));
			mViewHolder.name.setTextColor(mContext.getResources().getColor(R.color.yellow));
		}else{
			mViewHolder.number.setTextColor(mContext.getResources().getColor(R.drawable.epg_channel_font));
			mViewHolder.name.setTextColor(mContext.getResources().getColor(R.drawable.epg_channel_font));
		}
		mViewHolder.number.setCompoundDrawablePadding(10);
		mViewHolder.number.setText(channelInfo.getDisplayNumber());
		mViewHolder.name.setText(String.valueOf(channelInfo.getName()));
		return convertView;
	}

	class ViewHolder {
		LinearLayout llayoutGroup;
		TextView number;
		TextView name;
		ImageView icon;
	}


}
