/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tv.ui.sidepanel.parentalcontrols;

import android.app.FragmentTransaction;
import android.os.Bundle;
//import android.support.v17.leanback.widget.VerticalGridView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.leanback.widget.VerticalGridView;
import com.mediatek.wwtv.setting.preferences.PreferenceUtil;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.android.tv.ui.OnRepeatedKeyInterceptListener;
import com.android.tv.ui.sidepanel.ActionItem;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.SideFragment;
import com.android.tv.ui.sidepanel.SideFragmentManager;
import com.android.tv.ui.sidepanel.SubMenuItem;
import com.mediatek.wwtv.setting.widget.view.DatePicker;
import com.mediatek.wwtv.setting.widget.view.Picker;
import com.mediatek.wwtv.setting.widget.view.Picker.ResultListener;
import com.mediatek.wwtv.setting.widget.view.TimePicker;
import com.mediatek.wwtv.tvcenter.util.DateFormatUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.function.Consumer;
import java.util.List;

public class SubChannelScheduleBlockedFragment extends SideFragment {
    private static final String TAG = "SubChannelScheduleBlockedFragment";
    private static final String ARGS_NAME = "args_name";
    private static final String ARGS_CHANNEL_ID = "args_channel_id";
    private final SideFragmentListener mSideFragmentListener =
            new SideFragmentListener() {
                @Override
                public void onSideFragmentViewDestroyed() {
                    notifyDataSetChanged();
                }
            };
    private final List<Item> mItems = new ArrayList<>();
    private String mName;
    private int mChannelId;
    public static SubChannelScheduleBlockedFragment create(String name,int channelId) {
    	SubChannelScheduleBlockedFragment fragment = new SubChannelScheduleBlockedFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_NAME, name);
        args.putInt(ARGS_CHANNEL_ID, channelId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mName=getArguments().getString(ARGS_NAME);
        mChannelId=getArguments().getInt(ARGS_CHANNEL_ID);
    }
    
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        VerticalGridView listView = (VerticalGridView) view.findViewById(R.id.side_panel_list);
        listView.setOnKeyInterceptListener(
                new OnRepeatedKeyInterceptListener(listView) {
                    @Override
                    public boolean onInterceptKeyEvent(KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_UP) {
                            switch (event.getKeyCode()) {
                                case KeyEvent.KEYCODE_DPAD_UP:
                                case KeyEvent.KEYCODE_DPAD_DOWN:
                                    break;
                                default:
                                	break;
                            }
                        }
                        return super.onInterceptKeyEvent(event);
                    }
                });
        return view;
    }
    
    

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onDestroyView");
    }

    @Override
    protected String getTitle() {
        return mName;
    }

    @Override
    protected List<Item> getItemList() {
        mItems.clear();
        //Operation Mode
        int index = MenuConfigManager.getInstance(getActivity()).getDefault(
                MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK_OPERATION_MODE + mChannelId);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "index: " + index);
        final String[] valueStrings = getActivity().getResources().getStringArray(R.array.menu_parental_block_channel_schedule_operation_array);
        String operationModeSummary = valueStrings[index];
        OperationModeItem operationModeItem=new OperationModeItem(getString(R.string.menu_parental_channel_schedule_block_operation_mode),operationModeSummary,mSideFragmentManager,index);
        mItems.add(operationModeItem);
        //Starting Date
        String strStartDate = SaveValue.getInstance(getActivity()).readStrValue(
                MenuConfigManager.TIME_START_DATE + mChannelId);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "strStartDate: " + strStartDate);
        if(strStartDate.length()!=10||index==1){
        	strStartDate=DateFormatUtil.getCurrentTime();
        	SaveValue.getInstance(getActivity()).saveStrValue(MenuConfigManager.TIME_START_DATE + mChannelId,strStartDate);
        }
       
        DateItem statingDate=new DateItem(getString(R.string.menu_parental_channel_schedule_start_date),strStartDate,DateItem.MODE_STARTING);
        mItems.add(statingDate);
        statingDate.setEnabled(index==2);
        //Starting Time
        String strStartTime = SaveValue.getInstance(getActivity()).readStrValue(
                MenuConfigManager.TIME_START_TIME + mChannelId);
        if(strStartTime.length()==1){
        	strStartTime="00:00";
        	SaveValue.getInstance(getActivity()).saveStrValue(MenuConfigManager.TIME_START_TIME + mChannelId,strStartTime);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "strStartTime: " + strStartTime);
        TimeItem statingTime=new TimeItem(getString(R.string.menu_parental_channel_schedule_start_time),strStartTime,TimeItem.MODE_STARTING);
        mItems.add(statingTime);
        statingTime.setEnabled(index!=0);
        //End Date
        String strEndDate = SaveValue.getInstance(getActivity()).readStrValue(
                MenuConfigManager.TIME_END_DATE + mChannelId);
        if(strEndDate.length()!=10||index==1){
        	strEndDate=DateFormatUtil.getCurrentTime();
        	SaveValue.getInstance(getActivity()).saveStrValue(MenuConfigManager.TIME_END_DATE + mChannelId,strEndDate);
        }
        DateItem endDate=new DateItem(getString(R.string.menu_parental_channel_schedule_end_date),strEndDate,DateItem.MODE_END);
        mItems.add(endDate);
        endDate.setEnabled(index==2);
        //End Time
        String strEndTime = SaveValue.getInstance(getActivity()).readStrValue(
                MenuConfigManager.TIME_END_TIME + mChannelId);
        if(strEndTime.length()==1){
        	strEndTime="00:00";
        	SaveValue.getInstance(getActivity()).saveStrValue(MenuConfigManager.TIME_END_TIME + mChannelId,strEndTime);
        }
        TimeItem endTime=new TimeItem(getString(R.string.menu_parental_channel_schedule_end_time),strEndTime,TimeItem.MODE_END);
        mItems.add(endTime);
        endTime.setEnabled(index!=0);
        return mItems;
    }
    
    
    
    private class OperationModeItem extends SubMenuItem {
    	private int index;
        public OperationModeItem(String title, String description,
				SideFragmentManager fragmentManager,int index) {
			super(title, description, fragmentManager);
			this.index=index;
			// TODO Auto-generated constructor stub
		}
        @Override
        protected void onUpdate() {
            super.onUpdate();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onUpdate");
        }
		@Override
		protected SideFragment getFragment() {
			// TODO Auto-generated method stub
			SideFragment fragment = OperationModeFragment.create(index,mChannelId);
            fragment.setListener(mSideFragmentListener);
            return fragment;//fragment;
		}

    }


    private class DateItem extends ActionItem {
    	 public static final int MODE_STARTING=0;
    	 public static final int MODE_END=1;
    	 private int mode; // 
    	 public DateItem(String title, String description,int mode) {
    	       super(title,description);
    	       this.mode=mode;
    	 }
		@Override
		protected void onSelected() {
			// TODO Auto-generated method stub
			  //mSideFragmentManager.setCount(mSideFragmentManager.getCount()+1);
			  Log.d("chuanfei", Calendar.getInstance().get(Calendar.YEAR)+"");
			  DatePicker  datePicker=DatePicker.newInstance("",Calendar.getInstance().get(Calendar.YEAR),24,false);
			  datePicker.setResultListener(new ResultListener(){

				@Override
				public void onCommitResult(String result) {
					// TODO Auto-generated method stub
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "result: " + result);
					mDescription=result;
					notifyDataSetChanged();
				}
			  });
			  datePicker.setFocusDisabled(mBackStack);
			  Bundle bundle=new Bundle();
			  FragmentTransaction ft = getMainActivity().getFragmentManager().beginTransaction();
			  String key=null;
			  if(mode==MODE_STARTING){
				  key=MenuConfigManager.TIME_START_DATE + mChannelId;
			  }else{
				  key=MenuConfigManager.TIME_END_DATE + mChannelId;
			  }
			  bundle.putCharSequence(PreferenceUtil.PARENT_PREFERENCE_ID, key);
			  datePicker.setArguments(bundle);
		      ft.replace(R.id.main_fragment_container, datePicker,key);
		      ft.addToBackStack(key);
		      ft.commit();
		}
    }

    private class TimeItem extends ActionItem {
	     public static final int MODE_STARTING=0;
	     public static final int MODE_END=1;
	   	 private int mode; // 
	   	 public TimeItem(String title, String description,int mode) {
	   	       super(title,description);
	   	       this.mode=mode;
	   	 }
			@Override
			protected void onSelected() {
				// TODO Auto-generated method stub
				  //mSideFragmentManager.setCount(mSideFragmentManager.getCount()+1);
				  TimePicker timePicker=TimePicker.newInstance();
				  timePicker.setResultListener(new ResultListener(){

						@Override
						public void onCommitResult(String result) {
							// TODO Auto-generated method stub
							com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "result: " + result);
							mDescription=result;
							notifyDataSetChanged();
						}
					  });
				  timePicker.setFocusDisabled(mBackStack);
				  Bundle bundle=new Bundle();
				  FragmentTransaction ft = getMainActivity().getFragmentManager().beginTransaction();
				  String key=null;
				  if(mode==MODE_STARTING){
					  key=MenuConfigManager.TIME_START_TIME + mChannelId;
				  }else{
					  key=MenuConfigManager.TIME_END_TIME + mChannelId;
				  }
				  bundle.putCharSequence(PreferenceUtil.PARENT_PREFERENCE_ID, key);
				  timePicker.setArguments(bundle);
			      ft.replace(R.id.main_fragment_container, timePicker,key);
			      ft.addToBackStack(key);
			      ft.commit();
				
			}
   }
    
    Consumer<Picker> mBackStack = picker ->{
        try {
            getMainActivity().getFragmentManager().popBackStack();
        } catch (NullPointerException e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG,"e :"+e.toString());
        }
    };
}
