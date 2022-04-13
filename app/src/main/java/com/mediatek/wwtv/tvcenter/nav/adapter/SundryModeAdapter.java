package com.mediatek.wwtv.tvcenter.nav.adapter;

import java.util.List;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SundryModeAdapter extends BaseAdapter {
	private static final String TAG = "SundryModeAdapter";

	//private Context mContext;
	private LayoutInflater mInflater;

	private String[] pictureModeStringArray;
	private String[] soundEffectModeStringArray;
	private String[] screenModeStringArray;

	private List<Integer> mModeList;

	private int currentKeyVlaue = -1;
	
//	private static final int LIST_PAGE_MAX = 7;

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mModeList.size();
	}

	@Override
	public Integer getItem(int position) {
		// TODO Auto-generated method stub
		return mModeList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder mViewHolder;
		if (null == convertView) {
			convertView = mInflater.inflate(R.layout.nav_sundry_dialog_item,
					null);
			mViewHolder = new ViewHolder();
			mViewHolder.itemTextView = (TextView) convertView
					.findViewById(R.id.nav_sundry_mode_list_item_view);
			mViewHolder.itemTextView
					.setHeight((int) (ScreenConstant.SCREEN_HEIGHT * 0.05));
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}

		if (null != mModeList && !mModeList.isEmpty() && currentKeyVlaue != -1) {
			switch (currentKeyVlaue) {
			case KeyMap.KEYCODE_MTKIR_PEFFECT:
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "come in getView == " + mModeList.get(position));
				mViewHolder.itemTextView
						.setText(pictureModeStringArray[(mModeList
								.get(position)).intValue()]);
				break;
			case KeyMap.KEYCODE_MTKIR_SEFFECT:

				mViewHolder.itemTextView
						.setText(soundEffectModeStringArray[(mModeList
								.get(position)).intValue()]);
				break;
			case KeyMap.KEYCODE_MTKIR_ASPECT:
				mViewHolder.itemTextView
						.setText(screenModeStringArray[(mModeList.get(position))
								.intValue()]);
				break;
			default:
				break;
			}
		}

		return convertView;
	}

	public SundryModeAdapter(Context context) {
	    Context mContext = context;
		mInflater = LayoutInflater.from(context);

		pictureModeStringArray = mContext.getResources().getStringArray(
				R.array.picture_effect_array_us);
		soundEffectModeStringArray = mContext.getResources().getStringArray(
				R.array.menu_audio_equalizer_array_us);
		screenModeStringArray = mContext.getResources().getStringArray(
				R.array.screen_mode_array_us);
	}

	public void updateList(List<Integer> modeList, int keyValue) {
		mModeList = modeList;
		currentKeyVlaue = keyValue;
		notifyDataSetChanged();
	}

	public void updateList(List<Integer> modeList) {
		mModeList = modeList;
		notifyDataSetChanged();
	}

	private class ViewHolder {
		TextView itemTextView;
	}

}
