package com.mediatek.wwtv.tvcenter.epg.eu;

import java.util.List;
import java.util.Locale;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGTypeListAdapter.EPGListViewDataItem;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.wwtv.tvcenter.util.SaveValue;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.LayoutDirection;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemSelectedListener;

import androidx.core.text.TextUtilsCompat;

public class EpgType extends Dialog {

	private static final String TAG = "EpgType";
	private static boolean disableSubFilter = false; //fix CR DTV00587033
	public static final String COUNTRY_UK = "GBR"; 
	public static final String COUNTRY_NZL = "NZL";
	public static final String COUNTRY_AUS = "AUS";
  public static final String COUNTRY_DNK = "DNK"; //Denmark
  public static final String COUNTRY_FIN = "FIN"; //Finland
  public static final String COUNTRY_IRL = "IRL"; //Ireland
  public static final String COUNTRY_NOR = "NOR"; //Norway
  public static final String COUNTRY_SWE = "SWE"; //Sweden
	public static boolean mHasEditType;
	ListView epgList;
//	ListView epgSubList;
	EPGTypeListAdapter listAdapter;
//	EPGTypeListAdapter subAdapter;
	SaveValue sv;
	private Context mContext;
	private EPGSubTypeListAdapter subAdapter;
	private LinearLayout epgMainView;
	private LinearLayout epgSubView;
	private ListView epgSubList;

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
	private boolean subFilterDisable()
	{
		String mEpgCountry = MtkTvConfig.getInstance().getCountry();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "scube" + mEpgCountry);
		return mEpgCountry.equals(COUNTRY_UK) ||
				mEpgCountry.equals(COUNTRY_NZL) || mEpgCountry.equals(COUNTRY_AUS);
		
	}
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		disableSubFilter = subFilterDisable();
		
		setContentView(R.layout.epg_type_eu_main);
		setWindowPosition();
		init();
        initListener();
	}


	private void init() {
		epgMainView = (LinearLayout)findViewById(R.id.nav_epg_filter_main_type_layout);
		epgList = (ListView) findViewById(R.id.nav_epg_type_view);
		listAdapter = new EPGTypeListAdapter(mContext, epgList, disableSubFilter);
		List<EPGListViewDataItem> mData = listAdapter.loadEPGFilterTypeData();
		
		if(!disableSubFilter){
			epgSubView = (LinearLayout)findViewById(R.id.nav_epg_filter_sub_type_layout);
			epgSubList = (ListView) findViewById(R.id.nav_epg_sub_type_view);
			subAdapter = new EPGSubTypeListAdapter(mContext, epgSubList);
			if(mData != null){
				epgSubList.setAdapter(subAdapter);
			}
		}

		if (mData != null) {
			epgList.setAdapter(listAdapter);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "*********** mData is not null**************");
		} else {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "*********** mData is null **************");
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "******guanglei*****dispatchKeyEvent **************event: "+event.getKeyCode()+" , " + event.getAction());
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			switch (event.getKeyCode()) {
			case KeyMap.KEYCODE_MTKIR_BLUE:
			case KeyEvent.KEYCODE_B:
			case KeyEvent.KEYCODE_BACK:
				if(!disableSubFilter && epgSubView.getVisibility() == View.VISIBLE){
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "******guanglei*****sub visible back key **************");
					epgMainView.setVisibility(View.VISIBLE);
					epgSubView.setVisibility(View.INVISIBLE);
					listAdapter.notifyDataSetChanged();
					return true;
				}
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_E:
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "guanglei dispatchKeyEvent KEYCODE_DPAD_CENTER " + epgMainView.getVisibility());
				
				if(!disableSubFilter && epgMainView.getVisibility() == View.VISIBLE){
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "guanglei epgMainView.getVisibility() == View.VISIBLE");
					epgMainView.setVisibility(View.INVISIBLE);
					epgSubView.setVisibility(View.VISIBLE);
					subAdapter.resetPosition();
					return true;
				}
				break;
			default:
				break;
			}
		 }
		
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "******guanglei*****onKeyDown **************");
		switch (keyCode) {
		case KeyMap.KEYCODE_MTKIR_BLUE:
		case KeyEvent.KEYCODE_B:
		case KeyEvent.KEYCODE_BACK:
			if (this.isShowing()) {
				dismiss();
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
				Activity epgActivity = (Activity)mContext;
				epgActivity.onKeyDown(keyCode, event);
			}
			break;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initListener() {
		epgList.setOnKeyListener(new View.OnKeyListener() {
			
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "epgList onKey");
				if (disableSubFilter && event.getAction() == KeyEvent.ACTION_DOWN) {
					listAdapter.onMainKey(v, keyCode);
				}
				return false;
			}
			
		});
		
		  if(!disableSubFilter){
			epgList.setOnItemSelectedListener(new OnItemSelectedListener() {
		
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					List<EPGListViewDataItem> subChildDataItem = listAdapter
							.getEPGData().get(position).getSubChildDataItem();
					subAdapter.setEPGGroup(subChildDataItem);
					String mainType = listAdapter.getEPGData().get(position).getData();
					subAdapter.setSelectedMainType(mainType);
					epgSubList.setAdapter(subAdapter);
		
				}
		
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub
		
				}
		
			});

			epgSubList.setOnKeyListener(new View.OnKeyListener() {
				
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "guanglei epgSubList onKey");
					
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						subAdapter.onSubKey(v, keyCode);
					}
					return false;
				}
			});
		  }
	}

    //init dialog position
    public void setWindowPosition() {
        WindowManager m = getWindow().getWindowManager();
        Display display = m.getDefaultDisplay();
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        TypedValue sca = new TypedValue();
        mContext.getResources().getValue(R.dimen.nav_channellist_marginY,sca ,true);
        mContext.getResources().getValue(R.dimen.nav_channellist_marginX,sca ,true);
        mContext.getResources().getValue(R.dimen.nav_channellist_size_width,sca ,true);
        float chwidth  = sca.getFloat();
        mContext.getResources().getValue(R.dimen.nav_channellist_size_height,sca ,true);
        mContext.getResources().getValue(R.dimen.nav_channellist_page_max,sca ,true);
        int menuWidth = (int) (display.getWidth() * chwidth);
        lp.width = menuWidth;
        lp.height = display.getHeight();
		if(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())== LayoutDirection.RTL){
			lp.gravity = Gravity.START;
		}else {
			lp.gravity = Gravity.END;
		}
        window.setAttributes(lp);
    }

}