package com.mediatek.wwtv.setting.base.scan.adapter;

import java.util.List;

import com.mediatek.wwtv.tvcenter.R;
//import com.mediatek.wwtv.setting.base.EditTextActivity;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.MenuDataHelper;


import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.setting.widget.detailui.Action.DataType;

import android.content.Context;

import android.graphics.Color;
import android.text.InputFilter;
import android.text.InputType;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EditDetailAdapter extends BaseAdapter implements
		View.OnKeyListener {

	public static final String TAG = "EditDetailAdapter";
	Context mContext;
	List<EditItem> mList;

	public EditDetailAdapter(Context mContext, List<EditItem> mList) {
		this.mContext = mContext;
		this.mList = mList;
	}

	public void setNewList(List<EditItem> mList) {
		this.mList = mList;
		this.notifyDataSetChanged();
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
	public List getList(){
		return mList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final EditItem item = mList.get(position);
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(
					R.layout.editdetail_list_item_channel, parent, false);
			holder = new ViewHolder();
			holder.itemName = (TextView) convertView
					.findViewById(R.id.editdetail_name);
			holder.itemValue = (EditText) convertView
					.findViewById(R.id.editdetail_value);
			holder.itemOption = (TextView) convertView
					.findViewById(R.id.editdetail_option);
			holder.itemOpLayout = (LinearLayout) convertView
					.findViewById(R.id.editdetail_option_layout);
			holder.imgOption = (ImageView) convertView
					.findViewById(R.id.editdetail_img);
			holder.hasSubLayout = convertView
					.findViewById(R.id.editdetail_hassub_layout);
			holder.imgEditdetail = (ImageView) convertView
					.findViewById(R.id.iv_editdetail);
			holder.rlRelativeLayout = (RelativeLayout) convertView
					.findViewById(R.id.rl_editdetail_channel);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.itemName.setText(item.title);
		//not used
//		holder.rlRelativeLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//			
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//				if (item.dataType == DataType.INPUTBOX&&item.isEnable) {
//					Log.d(TAG, "hasFocus:"+hasFocus);
//					ImageView img = (ImageView) v.findViewById(R.id.iv_editdetail);
//					if (hasFocus&&img!=null) {
//						Log.d(TAG, "shoeIMG");
//						img.setVisibility(View.VISIBLE);
//					}
//					if (!hasFocus&&img!=null) {
//						Log.d(TAG, "NOTshoeIMG");
//						img.setVisibility(View.INVISIBLE);
//					}
//				}
//			}
//		});
		
		if (item.dataType == DataType.OPTIONVIEW) {
			holder.itemOpLayout.setVisibility(View.VISIBLE);
			if (item.id.equals(MenuConfigManager.TV_FINETUNE) && item.isEnable) {
				holder.itemOption.setText(String.format(mContext.getResources().getString(R.string.channel_finetune_MHz), item.optionValues[item.optionValue]));
			}else {
			holder.itemOption.setText(item.optionValues[item.optionValue]);
			}
			holder.itemValue.setVisibility(View.GONE);
			holder.hasSubLayout.setVisibility(View.GONE);
		} else if (item.dataType == DataType.HAVESUBCHILD) {
			holder.itemValue.setVisibility(View.GONE);
			holder.itemOpLayout.setVisibility(View.GONE);
			if (item.isEnable) {
				holder.hasSubLayout.setVisibility(View.VISIBLE);
			} else {
				holder.hasSubLayout.setVisibility(View.GONE);
			}
		} else if (item.dataType == DataType.INPUTBOX) {
			
			InputFilter[] filters4 = {new LengthFilter(4)};
			InputFilter[] filters64 = {new LengthFilter(64)};
			holder.itemValue.setInputType(InputType.TYPE_CLASS_TEXT);
			if(item.id.equals(MenuConfigManager.TV_CHANNEL_SA_NAME)){
				holder.itemValue.setFilters(filters64);
			}else if(item.id.equals(MenuConfigManager.TV_CHANNEL_NO)){
				holder.itemValue.setFilters(filters4);
				holder.itemValue.setInputType(InputType.TYPE_CLASS_NUMBER);
			} else if (item.id.equals(MenuConfigManager.TV_FREQ) || item.id.equals(MenuConfigManager.TV_FREQ_SA)) {
				holder.itemValue.setInputType(InputType.TYPE_CLASS_NUMBER);
			}

			if (item.value != null && !item.isEnable) {
				// channel info edit unable
				if (item.value.length() > 32) {
					holder.itemValue.setText(String.format(mContext.getResources().getString(R.string.channel_name_too_lang), item.value.substring(0, 32)));
				} else {
					holder.itemValue.setText(item.value);
				}
			} else if (item.value != null) {
				//holder.itemValue.setVisibility(View.VISIBLE);
				if (item.value.length() > 16) {
					holder.itemValue.setText(String.format(mContext.getResources().getString(R.string.channel_name_too_lang), item.value.substring(0, 16)));
				} else {
					holder.itemValue.setText(item.value);
				}
			}
			holder.itemValue.setVisibility(View.VISIBLE);
			holder.itemOpLayout.setVisibility(View.GONE);
			holder.hasSubLayout.setVisibility(View.GONE);
		} else if (item.dataType == DataType.TEXTCOMMVIEW) {
			holder.itemValue.setVisibility(View.VISIBLE);
			holder.itemValue.setText(item.value);
			holder.itemOpLayout.setVisibility(View.GONE);
			holder.hasSubLayout.setVisibility(View.GONE);
		}

		if (!item.isEnable) {
			holder.itemName.setTextColor(Color.GRAY);
			holder.itemValue.setTextColor(Color.GRAY);
			holder.itemOption.setTextColor(Color.GRAY);
			holder.itemValue.setEnabled(false);
			holder.itemOption.setEnabled(false);
			holder.itemValue.setBackgroundColor(mContext.getResources()
					.getColor(R.color.mmp_black));
			holder.imgOption.setVisibility(View.GONE);
		} else {
			holder.itemName.setTextColor(mContext.getResources().getColor(
					R.color.content_title_text_color));
			holder.itemValue.setTextColor(mContext.getResources().getColor(
					R.color.content_title_text_color));
			holder.itemOption.setTextColor(mContext.getResources().getColor(
					R.color.content_title_text_color));
			holder.itemValue.setEnabled(true);
			holder.itemOption.setEnabled(true);
			holder.itemValue.setBackgroundColor(mContext.getResources()
					.getColor(R.color.mmp_black));
			holder.imgOption.setVisibility(View.VISIBLE);
		}
		convertView.setTag(R.id.editdetail_value, item);
		return convertView;
	}

	/**
	 * option turn left
	 * 
	 * @param selectView
	 */
	public void optionTurnLeft(View selectView, String[] mData) {
		EditItem item = (EditItem) selectView.getTag(R.id.editdetail_value);
		if (item != null && item.isEnable) {
			if (item.optionValues != null) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "optionTurnLeft:" + item.optionValues);
				ViewHolder holder = (ViewHolder) selectView.getTag();
				item.optionValue--;
				if (item.optionValue < 0) {
					item.optionValue = item.optionValues.length - 1;
				}
				holder.itemOption.setText(item.optionValues[item.optionValue]);
				if (item.id.equals(MenuConfigManager.TV_CHANNEL_COLOR_SYSTEM)) {
					MenuDataHelper.getInstance(mContext)
							.updateChannelColorSystem(item.optionValue, mData);
				} else if (item.id.equals(MenuConfigManager.TV_SOUND_SYSTEM)) {
					MenuDataHelper.getInstance(mContext)
							.updateChannelSoundSystem(item.optionValue, mData);
				} else if (item.id.equals(MenuConfigManager.TV_AUTO_FINETUNE)
						|| item.id.equals(MenuConfigManager.TV_FINETUNE)) {
					MenuDataHelper.getInstance(mContext).updateChannelIsFine(
							mData[3], item.optionValue);
				} else if (item.id.equals(MenuConfigManager.TV_SKIP)) {
					MenuDataHelper.getInstance(mContext).updateChannelSkip(
							mData[3], item.optionValue);
				}
			}

		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "optionTurnLeft-> not option item so do nothing");
		}

	}

	/**
	 * option turn right
	 * 
	 * @param selectView
	 */
	public void optionTurnRight(View selectView, String[] mData) {
		EditItem item = (EditItem) selectView.getTag(R.id.editdetail_value);
		if (item != null && item.isEnable) {
			if (item.optionValues != null) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "optionTurnRight:" + item.optionValues);
				ViewHolder holder = (ViewHolder) selectView.getTag();
				item.optionValue++;
				if (item.optionValue > item.optionValues.length - 1) {
					item.optionValue = 0;
				}
				holder.itemOption.setText(item.optionValues[item.optionValue]);
				if (item.id.equals(MenuConfigManager.TV_CHANNEL_COLOR_SYSTEM)) {
					MenuDataHelper.getInstance(mContext)
							.updateChannelColorSystem(item.optionValue, mData);
				} else if (item.id.equals(MenuConfigManager.TV_SOUND_SYSTEM)) {
					MenuDataHelper.getInstance(mContext)
							.updateChannelSoundSystem(item.optionValue, mData);
				} else if (item.id.equals(MenuConfigManager.TV_AUTO_FINETUNE)
						|| item.id.equals(MenuConfigManager.TV_FINETUNE)) {
					MenuDataHelper.getInstance(mContext).updateChannelIsFine(
							mData[3], item.optionValue);
				} else if (item.id.equals(MenuConfigManager.TV_SKIP)) {
					MenuDataHelper.getInstance(mContext).updateChannelSkip(
							mData[3], item.optionValue);
				}
			}
		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "optionTurnRight-> not option item so do nothing");
		}
	}

	class ViewHolder {
		RelativeLayout rlRelativeLayout;
		TextView itemName;
		EditText itemValue;
		TextView itemOption;
		LinearLayout itemOpLayout;
		ImageView imgOption;
		View hasSubLayout;
		ImageView imgEditdetail;
	}

	 public void isShowIMGEdit(View view, Boolean isFocused) {
	 ImageView img = (ImageView) view.findViewById(R.id.iv_editdetail);
	 if (img != null) {
	 Log.d(TAG, "img NOT null");
	 if (isFocused) {
	 img.setVisibility(View.INVISIBLE);
	 } else {
	 img.setVisibility(View.VISIBLE);
	 }
	 }
	 //this.notifyDataSetChanged();
	 }

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		return false;
	}

	public static class EditItem {
		public String id;
		public String title;
		public String value;
		public boolean isEnable;
		public boolean isDigit;
		public String[] optionValues;
		public int optionValue;

		public float minValue;
		public float maxValue;
		public Action.DataType dataType;

		public EditItem(String id, String title, String value,
				boolean isEnable, boolean isDigit, DataType dataType) {
			super();
			this.id = id;
			this.title = title;
			this.value = value;
			this.isEnable = isEnable;
			this.isDigit = isDigit;
			this.dataType = dataType;
		}

		public EditItem(String id, String title, int opvalue,
				String[] optionValues, boolean isEnable, DataType dataType) {
			super();
			this.id = id;
			this.title = title;
			this.optionValue = opvalue;
			this.isEnable = isEnable;
			this.optionValues = optionValues;
			this.dataType = dataType;
		}

	}
	

}
