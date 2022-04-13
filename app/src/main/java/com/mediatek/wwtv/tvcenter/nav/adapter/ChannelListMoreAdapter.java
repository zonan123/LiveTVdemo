package com.mediatek.wwtv.tvcenter.nav.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.R;


public class ChannelListMoreAdapter extends BaseAdapter {
    private static final String TAG = "ChannelListMoreAdapter";
	/*
	 * context
	 */
	//private Context mContext;
	private LayoutInflater mInflater;
	private int index;
	private List<String> mConflictChannelList;
	// private TVInputManager tvInputSource;
	private Drawable mChanenlListSelectedIcon;
	private Drawable mChanenlListUnSelectedIcon;
//	private Drawable mConflictIcon;
//	private float itemHeightPercent = 0.04648f;
	private int type=0;

	public ChannelListMoreAdapter(Context context, int index,List<String> mConflictList) {
	    Context mContext = context;
		mInflater = LayoutInflater.from(mContext);
		this.index = index;
		this.mConflictChannelList = mConflictList;
		// this.tvInputSource = tvInputSource;	
		   mChanenlListSelectedIcon = mContext.getResources().getDrawable(
	                R.drawable.channel_list_options_selected_icon);
	        int iconW = (int)mContext.getResources().getDimension(R.dimen.nav_source_list_item_icon_widgh);
	        int iconH = (int)mContext.getResources().getDimension(R.dimen.nav_source_list_item_icon_height);
	        mChanenlListSelectedIcon.setBounds(0, 0,iconW,iconH);
	        mChanenlListUnSelectedIcon = mContext.getResources().getDrawable(R.drawable.channel_list_options_icon);
	        mChanenlListUnSelectedIcon.setBounds(0, 0,iconH,iconH);
	     /*   mConflictIcon.setBounds(0, 0, icon_w,icon_h);*/
		
	}

	public void updateList(int index,
			List<String> mConflictList){
		this.index = index;
		this.mConflictChannelList = mConflictList;
	}
	@Override
    public int getCount() {
		return mConflictChannelList.size();
	}

    // type 1=optin
    public void setType(int type) {
        this.type = type;
    }
	@Override
    public String getItem(int position) {
	    return  mConflictChannelList.get(position);
	//	return customLabel;
	}

	@Override
    public long getItemId(int position) {
		return position;
	}
	public void updateView(View view ,int index,int color) {
         if(view == null){
           return;  
         }
         ViewHolder hodler=(ViewHolder) view.getTag();
         hodler.mTextView.setTextColor(color);
         hodler.mIcon.setColorFilter(color);
       }
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder hodler;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.nav_channel_type_item, null);
			hodler = new ViewHolder();
			hodler.mTextView = (TextView) convertView
					.findViewById(R.id.nav_channel_type_list_item);
			hodler.mIcon = (ImageView) convertView
                    .findViewById(R.id.nav_channel_list_type_item_icon);
			convertView.setTag(hodler);
		} else {
			hodler = (ViewHolder) convertView.getTag();
		}
		hodler.mTextView.setText(getItem(position));
		if(type == 1){
		    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- index = "+ index+" position " +position);
		    if(index  == 0){
		        hodler.mTextView.setTextColor(Color.GRAY);
	            hodler.mIcon.setColorFilter(Color.GRAY);
	            hodler.mIcon.setImageDrawable(mChanenlListUnSelectedIcon);
		    }else if(index == 1 && position == 0){
		        hodler.mTextView.setTextColor(Color.GRAY);
                hodler.mIcon.setColorFilter(Color.GRAY);
                hodler.mIcon.setImageDrawable(mChanenlListUnSelectedIcon);
		    }else if(index == 1 && position == 1){
		        hodler.mIcon.setImageDrawable(mChanenlListSelectedIcon);
		    }else if(index > 1 && position == 0){
		        hodler.mIcon.setImageDrawable(mChanenlListSelectedIcon);
		    }else if(index > 1 && position > 0){
                hodler.mIcon.setImageDrawable(mChanenlListUnSelectedIcon);
            }
           
        }else{
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- index = "+ index+" position " +position);
            if (position == index || (index == -1 && position == 0)) {
                hodler.mIcon.setImageDrawable(mChanenlListSelectedIcon);
            } else{
                hodler.mIcon.setImageDrawable(mChanenlListUnSelectedIcon);
            }
        }
		

		return convertView;
	}

	private class ViewHolder {
	    ImageView mIcon;
		TextView mTextView;
	}
}