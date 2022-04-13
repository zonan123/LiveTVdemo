package com.mediatek.wwtv.tvcenter.epg.cn;

import java.util.List;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.cn.EPGTypeListAdapter.EPGListViewDataItem;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
//import com.mediatek.twoworlds.tv.MtkTvConfig;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemSelectedListener;

public class EpgType extends Dialog {

	private static final String TAG = "EpgType";
	private static boolean disableSubFilter = false; //fix CR DTV00587033
	public static final String COUNTRY_UK = "GBR"; 
	public static final String COUNTRY_NZL = "NZL";
	public static final String COUNTRY_AUS = "AUS";
	public static boolean mHasEditType;
	private String mEpgCountry;
	ListView epgList;
	ListView epgSubList;
	EPGTypeListAdapter listAdapter;
	EPGTypeListAdapter subAdapter;
	List<EPGListViewDataItem> mData;
	private Context mContext;

	public EpgType(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		this.mContext = context;
	}

	public EpgType(Context context, int theme) {
		super(context, theme);
		this.mContext = context;
	}

	public EpgType(Context context) {
		super(context, R.style.Theme_EpgSubTypeDialog);
		this.mContext = context;
	}
//	private boolean SubFilterDisable()
//	{
//		mEpgCountry = MtkTvConfig.getInstance().getCountry();
//		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "scube" + mEpgCountry);
//		if(mEpgCountry.equals(COUNTRY_UK) ||
//				mEpgCountry.equals(COUNTRY_NZL) || mEpgCountry.equals(COUNTRY_AUS))
//		{
//			return true;
//		}else{
//			return false;
//		}
//	}
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		disableSubFilter = SubFilterDisable();
		if(disableSubFilter)
		{
			setContentView(R.layout.epg_type_eu_main);
		}else{
			setContentView(R.layout.epg_type_main);
		}
		setWindowPosition();
		epgList = (ListView) findViewById(R.id.nav_epg_type_view);
		epgList.setDividerHeight(0);
		if(!disableSubFilter)
		{
			epgSubList = (ListView) findViewById(R.id.nav_epg_sub_type_view);
			epgSubList.setDividerHeight(0);
		}		

		listAdapter = new EPGTypeListAdapter(mContext, epgList, epgSubList);
		if(!disableSubFilter){
			subAdapter = new EPGTypeListAdapter(mContext, epgList, epgSubList);
		}
		if (mEpgCountry != null && mEpgCountry.equals(COUNTRY_UK)) {
			mData = listAdapter.loadEPGFilterTypeData(COUNTRY_UK);
		} else if (mEpgCountry != null && mEpgCountry.equals(COUNTRY_AUS)) {
			mData = listAdapter.loadEPGFilterTypeData(COUNTRY_AUS);
		} else {
			mData = listAdapter.loadEPGFilterTypeData("");
		}
		listAdapter.setEPGGroup(mData);
		if (mData != null) {
			epgList.setAdapter(listAdapter);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "*********** mData is not null**************");
			if(!disableSubFilter){
				subAdapter.setEPGGroup(mData.get(0).getSubChildDataItem());
			}
		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "*********** mData is null **************");
		}
		if(!disableSubFilter){
			epgSubList.setAdapter(subAdapter);
		}
		epgList.setOnKeyListener(new View.OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (listAdapter.isMfocus()) {
						listAdapter.onMainKey(v, keyCode);
					}
				}
				return false;
			}

		});
		if(!disableSubFilter){
			epgList.setOnItemSelectedListener(new OnItemSelectedListener() {

				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "epgList.setOnItemSelectedListener Position:" + position + ">>>" + listAdapter.isMfocus());
					if (listAdapter.isMfocus()) {
						List<EPGListViewDataItem> subChildDataItem = listAdapter
								.getEPGData().get(position).getSubChildDataItem();
						subAdapter.setEPGGroup(subChildDataItem);
						epgSubList.setAdapter(subAdapter);
					}

				}

				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub

				}

			});
			epgSubList.setOnKeyListener(new View.OnKeyListener() {

				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						if (!subAdapter.isMfocus()) {
							subAdapter.onSubKey(v, keyCode);
						}
					}
					return false;
				}

			});
			epgSubList.setOnItemSelectedListener(new OnItemSelectedListener() {

				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "epgSubList.setOnItemSelectedListener Position:" + position);

				}

				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub

				}

			});
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyMap.KEYCODE_MTKIR_BLUE:
		case KeyEvent.KEYCODE_B:
		case KeyEvent.KEYCODE_BACK:
			if (this.isShowing()) {
				dismiss();
				EPGCnActivity epgActivity = (EPGCnActivity)mContext;
				epgActivity.changeBottomViewText(false, 0);
				if (mHasEditType) {
					epgActivity.notifyEPGLinearlayoutRefresh();
					mHasEditType = false;
				}
			}
			return true;
		case KeyMap.KEYCODE_MTKIR_SOURCE:
		case KeyMap.KEYCODE_VOLUME_UP:
		case KeyMap.KEYCODE_VOLUME_DOWN:
			return true;
		case KeyMap.KEYCODE_MTKIR_GUIDE:
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "event.getRepeatCount()>>>" + event.getRepeatCount());
			if (event.getRepeatCount() <= 0) {
				if (this.isShowing()) {
					dismiss();
					mHasEditType = false;
				}
				EPGCnActivity epgActivity = (EPGCnActivity)mContext;
				epgActivity.onKeyDown(keyCode, event);
			}
			break;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyUp>>>>" + keyCode + "  " + event.getAction());
 		switch (keyCode) {
//		case KeyMap.KEYCODE_MTKIR_GUIDE:
//			if (this.isShowing()) {
//				dismiss();
//				mHasEditType = false;
//			}
//			EPGEuActivity epgActivity = (EPGEuActivity)mContext;
//			epgActivity.onKeyUp(keyCode, event);
//			return true;
		default:
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	private static int menuWidth = 800;
	private static int menuHeight = 610;
	public void setWindowPosition() {
		Window window = getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		menuWidth = 650*ScreenConstant.SCREEN_WIDTH /1280;
		menuHeight = 610*ScreenConstant.SCREEN_HEIGHT/720;
		if(disableSubFilter)
		{
			menuWidth = menuWidth/2;
		}
		lp.width = menuWidth;
		lp.height = menuHeight;
		window.setAttributes(lp);
	}

	
}