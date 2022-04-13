package com.mediatek.wwtv.setting.base.scan.adapter;

import java.text.DecimalFormat;
import java.util.List;

import com.mediatek.wwtv.setting.base.scan.adapter.ThirdItemAdapter.ViewHolder;
import com.mediatek.wwtv.setting.view.ScanOptionView;
import com.mediatek.wwtv.setting.util.MenuConfigManager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mediatek.wwtv.tvcenter.R;

/**
 * this adapter is used to for scan factor
 * @author sin_biaoqinggao
 *
 */
public class ScanFactorAdapter extends BaseAdapter{

	Context mContext ;
	List<ScanFactorItem> mList ;
	
	public ScanFactorAdapter(Context mContext, List<ScanFactorItem> mList) {
		super();
		this.mContext = mContext;
		this.mList = mList;
	}
	
	public List<ScanFactorItem> getList(){
		return mList;
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
		ScanFactorItem item = mList.get(position);
		ViewHolder holder ;
		if(convertView == null){
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.scanfactor_list_item,
					parent, false);
			holder = new ViewHolder();
			holder.factorTitle = (TextView)convertView.findViewById(R.id.factor_name);
			holder.factorNum = (TextView)convertView.findViewById(R.id.factor_num);
			holder.factorNUmImg = (ImageView)convertView.findViewById(R.id.factor_num_img);
			holder.optionLayout =  (LinearLayout)convertView.findViewById(R.id.factor_option_layout);
			holder.inputLayout =  (LinearLayout)convertView.findViewById(R.id.factor_input_layout);
			holder.doLayout =  (LinearLayout)convertView.findViewById(R.id.factor_do_layout);
			holder.numLayout =  (LinearLayout)convertView.findViewById(R.id.factor_num_layout);
			holder.progressLayout =  (RelativeLayout)convertView.findViewById(R.id.factor_progress_layout);
			holder.progressPrecent = (TextView)convertView.findViewById(R.id.factor_progress_percent);
			holder.progressTitle = (TextView)convertView.findViewById(R.id.factor_progress_title);
			holder.factorOption = (ScanOptionView)convertView.findViewById(R.id.factor_option);
			holder.factorOPtionImg = (ImageView)convertView.findViewById(R.id.factor_option_img);
			holder.factorProgress = (ProgressBar)convertView.findViewById(R.id.factor_progress);
			holder.factorInput = (EditText)convertView.findViewById(R.id.factor_input);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		holder.factorTitle.setText(item.title);
		if(!item.isEnable){
			holder.factorTitle.setTextColor(Color.GRAY);
			holder.factorNum.setTextColor(Color.GRAY);
			holder.factorOption.setTextColor(Color.GRAY);
			holder.factorNum.setEnabled(false);
			holder.factorOption.setEnabled(false);
			holder.factorNUmImg.setVisibility(View.GONE);
			holder.factorOPtionImg.setVisibility(View.GONE);
		}else{
			holder.factorTitle.setTextColor(Color.WHITE);
			holder.factorNum.setTextColor(Color.WHITE);
			holder.factorOption.setTextColor(Color.WHITE);
			holder.factorNum.setEnabled(true);
			holder.factorOption.setEnabled(true);
			holder.factorNUmImg.setVisibility(View.VISIBLE);
			holder.factorOPtionImg.setVisibility(View.VISIBLE);
		}
		if(item.factorType == 0){//num
			holder.numLayout.setVisibility(View.VISIBLE);
			holder.optionLayout.setVisibility(View.GONE);
			holder.progressLayout.setVisibility(View.GONE);
			holder.inputLayout.setVisibility(View.GONE);
			holder.doLayout.setVisibility(View.GONE);
			holder.factorNum.setText(item.value);
		}else if(item.factorType == 1){//option
			holder.numLayout.setVisibility(View.GONE);
			holder.optionLayout.setVisibility(View.VISIBLE);
			holder.progressLayout.setVisibility(View.GONE);
			holder.inputLayout.setVisibility(View.GONE);
			holder.doLayout.setVisibility(View.GONE);
			holder.factorOption.bindData(item.id, item.optionValues, item.optionValue);
		}else if(item.factorType == 2){//progress
			holder.factorTitle.setText("");
			holder.progressTitle.setText(item.title);
			holder.numLayout.setVisibility(View.GONE);
			holder.optionLayout.setVisibility(View.GONE);
			holder.progressLayout.setVisibility(View.VISIBLE);
			holder.inputLayout.setVisibility(View.GONE);
			holder.doLayout.setVisibility(View.GONE);
			holder.factorProgress.setProgress(item.progress);
			holder.progressPrecent.setText(item.progress+"%");
		}else if(item.factorType == 3){//input
			holder.numLayout.setVisibility(View.GONE);
			holder.optionLayout.setVisibility(View.GONE);
			holder.progressLayout.setVisibility(View.GONE);
			holder.inputLayout.setVisibility(View.VISIBLE);
			holder.doLayout.setVisibility(View.GONE);
			String value = item.inputValue+"";
			if(item.id.equals(MenuConfigManager.TV_CHANNEL_START_FREQUENCY)
				|| item.id.equals(MenuConfigManager.TV_CHANNEL_END_FREQUENCY)){
				value = new DecimalFormat("0.00").format(item.inputValue);
			}else if(item.id.equals(MenuConfigManager.TV_DVBC_SCAN_FREQUENCY)
					||item.id.equals(MenuConfigManager.TV_DVBC_SCAN_NETWORKID)){
				//deal when special inputValue
				if(item.inputValue == -1 ||item.inputValue == -3){
					//if item.value has string use this to show
					if(item.value !=null){
						value = item.value;
					}
				}
			}
			holder.factorInput.setText(value);
		}else if(item.factorType == 4){//doscan
			holder.numLayout.setVisibility(View.GONE);
			holder.optionLayout.setVisibility(View.GONE);
			holder.progressLayout.setVisibility(View.GONE);
			holder.inputLayout.setVisibility(View.GONE);
			holder.doLayout.setVisibility(View.VISIBLE);
		}
		convertView.setTag(R.id.factor_name, item);
		return convertView;
	}
	
	class ViewHolder{
		TextView factorTitle;
		LinearLayout optionLayout;
		LinearLayout numLayout;
		RelativeLayout progressLayout;
		LinearLayout inputLayout;
		LinearLayout doLayout;
		public ScanOptionView factorOption;
		public ImageView factorOPtionImg;
		public TextView factorNum;
		public ImageView factorNUmImg;
		public ProgressBar factorProgress;
		public TextView progressPrecent;
		public TextView progressTitle;
		public EditText factorInput;
		
	}

	public static class ScanFactorItem{
		public String id;
		public String title;
		//value,progress,optionValue these three property is both mutex
		public String value;
		public int progress = -1;
		public int optionValue = -1;
		public String[] optionValues;
		public boolean isEnable ;
		//default 0, 1:scanoptionView,2:progressBar,3:inputEdit,4:doscan direct
		public int factorType ;
		//inputValue & min & max is for factorinput
		public int inputValue;
		public int minValue;
		public int maxValue;
		
		public ScanOptionChangeListener listener;
		
		public ScanFactorItem(String id, String title, String value,
				boolean isEnable) {
			super();
			this.id = id;
			this.title = title;
			this.value = value;
			this.isEnable = isEnable;
			this.factorType = 0;
		}

		public ScanFactorItem(String id, String title, int optionValue,
				String[] optionValues, boolean isEnable) {
			super();
			this.id = id;
			this.title = title;
			this.optionValue = optionValue;
			this.optionValues = optionValues;
			this.isEnable = isEnable;
			this.factorType = 1;
		}

		public ScanFactorItem(String id, String title, int progress,
				boolean isEnable) {
			super();
			this.id = id;
			this.title = title;
			this.progress = progress;
			this.isEnable = isEnable;
			this.factorType = 2;
		}
		
		public ScanFactorItem(String id, String title, int inputValue,
				int minValue,int maxValue,boolean isEnable) {
			super();
			this.id = id;
			this.title = title;
			this.inputValue = inputValue;
			this.isEnable = isEnable;
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.factorType = 3;
		}
		
		public ScanFactorItem(String id, String title,
				boolean isEnable) {
			super();
			this.id = id;
			this.title = title;
			this.isEnable = isEnable;
			this.factorType = 4;
		}
		
		public void addScanOptionChangeListener(ScanOptionChangeListener listener){
			this.listener = listener;
		}
		
	}
	
	public interface ScanOptionChangeListener{
		void onScanOptionChange(String afterName);
	}
}
