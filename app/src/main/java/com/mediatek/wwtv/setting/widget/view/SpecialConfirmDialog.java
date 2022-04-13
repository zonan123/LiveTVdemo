package com.mediatek.wwtv.setting.widget.view;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.mediatek.wwtv.setting.util.Util;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;


public class SpecialConfirmDialog  extends CommonDialog{

	private static final float wScale = 0.55f;
	private static final float hScale = 0.35f;


	private TextView mTV1;
	private TextView mTV2;

	private Button positiveBtn;
	private Button negativeBtn;

	private List<MtkTvBookingBase> mItems;

	public SpecialConfirmDialog(Context context) {
		super(context, R.layout.pvr_tshift_special_confirmdialog);
		Point outSize = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getRealSize(outSize);
		this.getWindow().setLayout(
				(int) (outSize.x * wScale),
				(int) (outSize.y * hScale));

		setCancelable(true);
		initView2();
	}

	/*public SpecialConfirmDialog(Context context,
			List<ScheduleItem> item) {
		super(context, R.layout.pvr_tshift_special_confirmdialog);
		Point outSize = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getRealSize(outSize);
		this.getWindow().setLayout(
				(int) (outSize.x* wScale),
				(int) (outSize.y * hScale));

		this.mItems = item;

		setCancelable(true);
		initView2();
	}*/

	public SpecialConfirmDialog(Context context, List<MtkTvBookingBase> item) {
		super(context, R.layout.pvr_tshift_special_confirmdialog);

		Point outSize = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getRealSize(outSize);
		int tvWidth = outSize.x;//windowManager.getDefaultDisplay().getRawWidth();
		int tvHeight = outSize.y;//windowManager.getDefaultDisplay().getRawHeight();

		this.getWindow().setLayout((int) (tvWidth * wScale),
				(int) (tvHeight * hScale));
		this.mItems = item;

		setCancelable(true);
		initView2();
	}

	private void initView2() {
		mTV1 = (TextView) findViewById(R.id.diskop_title_line1);
		mTV1.setText("");

		mTV2 = (TextView) findViewById(R.id.diskop_title_line2);
		mTV2.setText("");

		positiveBtn = (Button) findViewById(R.id.confirm_btn_yes);
		negativeBtn = (Button) findViewById(R.id.confirm_btn_no);

		setItemValue(mItems);
	}

	/**
	 * id:1001365643949250
	 *
	 * @param view
	 * @param item
	 */
	private void setItemValue(List<MtkTvBookingBase> items) {

		TableLayout rootView = (TableLayout)findViewById(R.id.device_info);
		if (items == null ) {
			Util.showDLog("specialConifrmDilog.setItemValue():"
					+ "MtkTvBookingBase==null");
			rootView.setVisibility(View.INVISIBLE);
			return;
		}


		for(MtkTvBookingBase item :items){
			TableRow row = new TableRow(mContext);
			TextView label = new TextView(mContext);
			label.setTextColor(mContext.getResources().getColor(R.color.yellow));
			label.setText(mContext.getResources().getString(R.string.special_config_lable, item.getEventTitle()));
			row.addView(label);

			TextView size = new TextView(mContext);
			size.setTextColor(mContext.getResources().getColor(R.color.yellow));
			size.setText(Util.dateToStringYMD3(new Date(item.getRecordStartTime()*1000)));
			row.addView(size);

			TextView duration = new TextView(mContext);
			duration.setTextColor(mContext.getResources().getColor(R.color.yellow));
            duration.setText(Util.longToHrMinN(item.getRecordDuration()));
			row.addView(duration);

			TextView repeatType = new TextView(mContext);
			repeatType.setTextColor(mContext.getResources().getColor(R.color.yellow));
			String[] repeat = mContext.getResources().getStringArray(
					R.array.pvr_tshift_repeat_type);
			if(item.getRepeatMode()==128){
				repeatType.setText(repeat[0]);
			}else if(item.getRepeatMode()==0){
				repeatType.setText(repeat[2]);
			}else{
				repeatType.setText(repeat[1]);
			}
			row.addView(repeatType);

			TextView scheduleType = new TextView(mContext);
			scheduleType.setTextColor(mContext.getResources().getColor(R.color.yellow));
			String[] schedule = mContext.getResources().getStringArray(
					R.array.pvr_tshift_schedule_type);
			String type = schedule[item.getRecordMode()==2 ? 0 :item.getRecordMode()];
			scheduleType.setText(type);
			row.addView(scheduleType);

			rootView.addView(row);

		}
		//rootView.setVisibility(View.VISIBLE);
		/*TextView label = (TextView) findViewById(R.id.schedule_channel_name);
		label.setText("CH" + item.getChannelName());

		TextView size = (TextView) findViewById(R.id.schedule_date);
		size.setText(Util.dateToStringYMD3(item.getStartTime()));
		;

		TextView duration = (TextView) findViewById(R.id.schedule_duration);

		duration.setText(Util.longToHrMin(item.getDuration()));

		TextView repeatType = (TextView) findViewById(R.id.schedule_internel);
		String[] repeat = mContext.getResources().getStringArray(
				R.array.pvr_tshift_repeat_type);

		repeatType.setText(repeat[item.getRepeatType()]);

		TextView scheduleType = (TextView) findViewById(R.id.schedule_notification);
		String[] schedule = mContext.getResources().getStringArray(
				R.array.pvr_tshift_schedule_type);
		String type = schedule[item.getRemindType()];
		scheduleType.setText(type);*/

	}

	public void setPositiveButton(Button.OnClickListener listener) {
		positiveBtn.setOnClickListener(listener);
	}

	public void setNegativeButton(Button.OnClickListener listener) {
		negativeBtn.setOnClickListener(listener);
	}

	public void setTitle(String line1, String line2) {
		mTV1.setText(line1);
		mTV2.setText(line2);
	}

	/*
	 *
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		Util.showDLog("CommonConfirmDialog.onKeyDown()" + keyCode);
		if (keyCode == KeyMap.KEYCODE_DPAD_CENTER) {
			View view = getCurrentFocus();
			if(view!=null) {
				switch (view.getId()) {
					case R.id.confirm_btn_yes:
					case R.id.confirm_btn_no:
						onClick(view);
						break;
					default:
						Util.showDLog("Current Focus !=Confirm Button.");
						break;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	
	 @Override
	    public void show() {
		getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);//important
		 super.show();
	 
	 }
}
