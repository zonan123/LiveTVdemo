package com.mediatek.wwtv.setting.widget.view;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.graphics.Point;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.mediatek.wwtv.setting.base.scan.adapter.SetConfigListViewAdapter;
import com.mediatek.wwtv.setting.base.scan.adapter.SetConfigListViewAdapter.DataItem;
import com.mediatek.wwtv.setting.base.scan.adapter.SetConfigListViewAdapter.SrctypeChangeListener;
import com.mediatek.wwtv.setting.base.scan.model.RespondedKeyEvent;
import com.mediatek.wwtv.setting.base.scan.model.StateScheduleList;
import com.mediatek.wwtv.setting.base.scan.model.StateScheduleListCallback;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.util.Util;
import com.mediatek.wwtv.setting.view.DateTimeInputView;
import com.mediatek.wwtv.setting.view.DateTimeView;
import com.mediatek.wwtv.setting.view.OptionView;
import com.mediatek.wwtv.tvcenter.dvr.manager.DevManager;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.dm.MountPoint;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.MtkTvTimeBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;

import com.mediatek.wwtv.tvcenter.R;
import mediatek.sysprop.VendorProperties;

public class ScheduleListItemInfoDialog  extends CommonDialog implements OnItemClickListener, OnItemSelectedListener,SrctypeChangeListener{

	public static final String SCHEDULE_PVR_SRCTYPE = "SCHEDULE_PVR_SRCTYPE";
	public static final String SCHEDULE_PVR_CHANNELLIST = "SCHEDULE_PVR_CHANNELLIST";
	public static final String SCHEDULE_PVR_REMINDER_TYPE = "SCHEDULE_PVR_REMINDER_TYPE";
	public static final String SCHEDULE_PVR_REPEAT_TYPE = "SCHEDULE_PVR_REPEAT_TYPE";
	public static final int INVALID_VALUE = 10004;
	public static final int STEP_VALUE = 1;
	public static final String TIME_DATE = "SETUP_date";
	public static final String TIME_TIME = "SETUP_time";

	public boolean epgFlag = false;

	private static final String TAG = "ScheduleListItemInfoDialog";

    private static final float wScale = 0.85f;
    private static final float hScale = 0.99f;

	private StateScheduleListCallback<?> mCallback;
	private ListView mDataItemList;
	private GridView mWeekList;

	private TextView mDiskInfo;

	private Button mBtn1;
	private Button mBtn2;
	private Button mBtn3;

    private LayoutInflater mInflater;

	private List<DataItem> mDataList;
    private MtkTvBookingBase mScheItem;
	private Map<String, Boolean> mWeekItem = new HashMap<String, Boolean>();;

	private final String[] WEEK_ARRAY;


//	private int position = 0;

	private final static int ADD_SUCCESS = 100;

	private SpecialConfirmDialog confirm;
	private boolean weekflag = false;

	enum BtnTypeEnum {
		ADD_CANCEL, EDIT_DELETE_CANCEL, REPLACE_ADD_CANCEL
	}

	private static final int UI_Index_SrcType = 0;
	private static final int UI_Index_ChannelNum = 1;
	private static final int UI_Index_StartDate = 2;
	private static final int UI_Index_StartTime = 3;
	private static final int UI_Index_StopTime = 4;
	private static final int UI_Index_ScheduleType = 5;
	private static final int UI_Index_RepeatType = 6;

	private View.OnClickListener addListener;
	private View.OnClickListener replaceListener;
	private View.OnClickListener cancelListener;
	private View.OnClickListener deleteListener;
	private View.OnClickListener editListener;


	private BtnTypeEnum btnListType = BtnTypeEnum.ADD_CANCEL;
    private int position =0;
    private int itemPosition = -1 ;
    private boolean isTTSKey = false;
    private static WeakReference<ScheduleListItemInfoDialog> scheduleListItemInfoDialog = null ;

    private final AccessibilityDelegate mDelegate = new AccessibilityDelegate() {
    	public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
    			AccessibilityEvent event) {
			do {
				if (!mDataItemList.equals(host) ) {
					break;
				}
				List<CharSequence> texts = event.getText();
				if (texts == null) {
					break;
				}
				if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TYPE_VIEW_ACCESSIBILITY_FOCUSED");
					int index = findSelectItem(texts.get(0).toString());
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "index = " + index);
					if (index >= 0) {
						((SetConfigListViewAdapter) mDataItemList.getAdapter())
								.setSelectPos(index);
						showHightLight(index);
						itemPosition = index ;
						isTTSKey= true;
					}
				} else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TYPE_VIEW_CLICKED");

				}
			} while (false);

			try {//host.onRequestSendAccessibilityEventInternal(child, event);
				Class clazz = Class.forName("android.view.ViewGroup");
				java.lang.reflect.Method getter =
					clazz.getDeclaredMethod("onRequestSendAccessibilityEventInternal",
						View.class, AccessibilityEvent.class);
				return (boolean)getter.invoke(host, child, event);
			} catch (Exception e) {
				android.util.Log.d(TAG, "Exception " + e);
			}
			return true;
    	}
    };

    private int findSelectItem (String text){
    	if (mDataList == null ) {
			return -1 ;
		}
    	for (int i = 0; i < mDataList.size(); i++) {
			if (mDataList.get(i).getmName().equals(text)) {
				return i;
			}
		}
    	return -1;
    }
    public ScheduleListItemInfoDialog(Context context){
    	super(context, R.layout.pvr_tshfit_schudulelist_item);
    	WEEK_ARRAY = mContext.getResources().getStringArray(R.array.week_day);
    	initDialog();

    }
    private void initDialog(){
        	scheduleListItemInfoDialog = new WeakReference<>(this);
        	mInflater = (LayoutInflater) mContext
    				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
	/**
	 * @param context
	 * @param layoutID
	 */
	public ScheduleListItemInfoDialog(Context context,
            MtkTvBookingBase item) {
		super(context, R.layout.pvr_tshfit_schudulelist_item);
		Point outSize = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getRealSize(outSize);
		getWindow().setLayout((int) (outSize.x * wScale),
				(int) (outSize.y* hScale));
		this.position = position;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ScheduleListItemInfoDialog = " + item.toString());
		setScheItem(item);
		this.mWeekItem = prepareWeekItem(getScheItem());

		WEEK_ARRAY = mContext.getResources().getStringArray(R.array.week_day);
		initDialog();
		initView2();
	}

    /**
     * @param context
     * @param layoutID
     */
    public ScheduleListItemInfoDialog(Context context,
            MtkTvBookingBase item ,int position) {
        super(context, R.layout.pvr_tshfit_schudulelist_item);
        Point outSize = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getRealSize(outSize);
        getWindow().setLayout((int) (outSize.x * wScale),
                (int) (outSize.y * hScale));
        setScheItem(item);
        this.mWeekItem = prepareWeekItem(getScheItem());
        this.position =position ;

        WEEK_ARRAY = mContext.getResources().getStringArray(R.array.week_day);
        initDialog();
        initView2();
    }

	public void dismiss() {
		// TODO Auto-generated method stub
		if (null  != confirm &&confirm.isShowing()) {
			confirm.dismiss();
		}
		super.dismiss();
	}

	/**
	 * @param item
	 * @return
	 */
    private Map<String, Boolean> prepareWeekItem(MtkTvBookingBase item) {
        Map<String, Boolean> mapList = new HashMap<String, Boolean>();
        if (item.getRepeatMode() == 0 || item.getRepeatMode() == 128) {
			String[] array = mContext.getResources().getStringArray(R.array.week_day);
			int weekday = MtkTvTime.getInstance().getLocalTime().weekDay;
			for(int i=0;i<7;i++){
				if(i==weekday){
					mapList.put(array[i], true);
				}else{
					mapList.put(array[i], false);
                }
			}
		}else{
			 int repeat = item.getRepeatMode() ;
				String[] array =mContext.getResources().getStringArray(R.array.week_day);

				for(int i=0;i<7;i++){
					if((repeat & (1 << i))!=0){
						mapList.put(array[i], true);
					} else {
						mapList.put(array[i], false);
						}

				}
			}
        // }

		return mapList;
	}

	@Override
	public void initView() {
		// TODO Auto-generated method stub
		super.initView();
		mDataItemList = (ListView) findViewById(R.id.schedulelist_item_list);
		mDataItemList.setDivider(null);

		mDiskInfo = (TextView) findViewById(R.id.schedulelist_item_title_diskinfo);
		mWeekList = (GridView) findViewById(R.id.week_gridview);
		//mWeekList.setFocusable(false);
		//mWeekList.setClickable(false);
		//mWeekList.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		mBtn1 = (Button) findViewById(R.id.schedulelist_item_btn_first);
		mBtn2 = (Button) findViewById(R.id.schedulelist_item_btn_second);
		mBtn3 = (Button) findViewById(R.id.schedulelist_item_btn_third);

		initListener();
	}
	android.os.Handler handler = new android.os.Handler(){
        @Override
	    public void handleMessage(android.os.Message msg) {

	        switch (msg.what) {
                case 111:
                	if(getBtnListType() == BtnTypeEnum.EDIT_DELETE_CANCEL){
                		mBtn3.setFocusable(true);
  	                    mBtn3.requestFocus();
  	                    mBtn3.setSelected(true);
                	}else{
                        mBtn3.setFocusable(true);
                        mBtn3.requestFocus();
                        mBtn3.setSelected(true);
                        if (getScheItem().getRecordMode() != 0) {
                            if (getScheItem().getSourceType() == 1) {
                                OptionView view = (OptionView) mDataItemList.getChildAt(1);
                                if (view != null && view.getValueView() != null) {
                                    view.getValueView().setVisibility(View.INVISIBLE);
                                }
                            }
                        }
            		}
                    break;
                case 222:
                    mBtn1.setFocusable(false);
                    mBtn1.clearFocus();
                    mBtn1.setSelected(false);
                    mBtn2.setFocusable(false);
                    mBtn2.clearFocus();
                    mBtn2.setSelected(false);
                    mBtn3.setFocusable(false);
                    mBtn3.clearFocus();
                    mBtn3.setSelected(false);
                    break;
                case 333:
                    mBtn3.setFocusable(true);
                    mBtn1.setFocusable(true);
                    mBtn1.requestFocus();
                    mBtn1.setSelected(true);
                    break;
                case ADD_SUCCESS:
                	ScheduleListItemInfoDialog.this.dismiss();
                    ScheduleListDialog scheduleListDialog = new ScheduleListDialog(mContext,position);
                	scheduleListDialog.setEpgFlag(epgFlag);
                	scheduleListDialog.show();
                	 break;
                default:
                    break;
            }
	    };
	};
	@SuppressLint("NewApi")
	private void initView2() {
		SetConfigListViewAdapter infoAdapter = new SetConfigListViewAdapter(
				mContext);
		mDataList = initItemList();
		infoAdapter.setmGroup(mDataList);
		infoAdapter.setSrctypeChangelistener(this);
		mDataItemList.setAdapter(infoAdapter);
		mDataItemList.setOnItemClickListener(this);
		mDataItemList.setOnItemSelectedListener(this);
		mWeekList.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
		mWeekList.setOnItemClickListener(this);
		//mWeekList.setFocusable(false);
		//mWeekList.setClickable(false);
		//mWeekList.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		mWeekList.setAdapter(new WeekListAdapter(mWeekItem));
        if (getScheItem().getChannelId() == 0) {
			setBtnListType(BtnTypeEnum.ADD_CANCEL);
		} else {
			setBtnListType(BtnTypeEnum.EDIT_DELETE_CANCEL);
		}

		MountPoint mp = null ;
		List<MountPoint> list = DevManager.getInstance().getMountList();
		if(list!=null && !list.isEmpty()){
			mp = list.get(0);
		}
		mDiskInfo.setText(Util.getGBSizeOfDisk(mp));

        if (getScheItem().getRepeatMode() == 0 || getScheItem().getRepeatMode() == 128) {
			mWeekList.setVisibility(View.INVISIBLE);
        } else {
            mWeekList.setVisibility(View.VISIBLE);
		}
//		mWeekList.setFocusable(true);
//		mWeekList.requestFocus();
		// mBtn3.setSelected(true);

		reSetBtnList();
        handler.sendEmptyMessageDelayed(111, 300);
		updateDissmissTimer();
		mDataItemList.setAccessibilityDelegate(mDelegate);
	}

	/**
	 *
	 */
	private void reSetBtnList() {

		switch (getBtnListType()) {

		case ADD_CANCEL:
			mBtn1.setVisibility(View.INVISIBLE);
			mBtn2.setText(mContext.getResources().getString(
					R.string.pvr_schedule_add));
			mBtn2.setVisibility(View.VISIBLE);
			mBtn3.setText(mContext.getResources().getString(
					R.string.pvr_schedule_cancel));

			mBtn2.setOnClickListener(addListener);
			mBtn3.setOnClickListener(cancelListener);
			break;
		case EDIT_DELETE_CANCEL:
			mBtn1.setVisibility(View.VISIBLE);
			mBtn1.setText(mContext.getResources().getString(
					R.string.pvr_schedule_edit));
			mBtn2.setText(mContext.getResources().getString(
					R.string.pager_mid));
			mBtn2.setVisibility(View.VISIBLE);
			mBtn3.setText(mContext.getResources().getString(
					R.string.pvr_schedule_ok));

			mBtn1.setOnClickListener(editListener);
			mBtn2.setOnClickListener(deleteListener);
			mBtn3.setOnClickListener(cancelListener);
			break;

		case REPLACE_ADD_CANCEL:
			mBtn1.setVisibility(View.VISIBLE);
			mBtn1.setText(mContext.getResources().getString(
					R.string.pvr_schedule_replace));
			mBtn2.setText(mContext.getResources().getString(
					R.string.pvr_schedule_add));
			mBtn2.setVisibility(View.VISIBLE);
			mBtn3.setText(mContext.getResources().getString(
					R.string.pvr_schedule_cancel));

			mBtn1.setOnClickListener(replaceListener);
			mBtn2.setOnClickListener(addListener);
			mBtn3.setOnClickListener(cancelListener);
			mBtn3.setFocusable(true);
            mBtn3.requestFocus();
            mBtn3.setSelected(true);
			break;
		default:
			break;
		}
		reSetFocusPath();
	}

	void reSetFocusPath() {
		if(getBtnListType()!=BtnTypeEnum.EDIT_DELETE_CANCEL){
			mBtn1.setNextFocusUpId(R.id.week_gridview);
			mBtn1.setNextFocusLeftId(R.id.week_gridview);
		}else{
			mBtn1.setNextFocusUpId(mBtn1.getId());
        }
        mBtn1.setNextFocusLeftId(mBtn3.getId());
		if(getBtnListType()!=BtnTypeEnum.EDIT_DELETE_CANCEL){
			mBtn2.setNextFocusUpId(R.id.week_gridview);
		}else{
			mBtn2.setNextFocusUpId(mBtn2.getId());
		}
		mBtn2.setNextFocusLeftId(R.id.week_gridview);

		int id = R.id.week_gridview;
		if (mWeekList.getVisibility() != View.VISIBLE) {
			id = R.id.schedulelist_item_list;
		}

		if (mBtn1.getVisibility() == View.VISIBLE) {
			if(getBtnListType()!=BtnTypeEnum.EDIT_DELETE_CANCEL){
				mBtn1.setNextFocusUpId(id);
				mBtn1.setNextFocusLeftId(id);
			}else{
				mBtn1.setNextFocusUpId(mBtn1.getId());
				mBtn1.setNextFocusLeftId(mBtn3.getId());
            }
			if(mBtn2.getVisibility()==View.VISIBLE){
				mBtn1.setNextFocusRightId(mBtn2.getId());
			}else{
				mBtn1.setNextFocusRightId(mBtn3.getId());
			}
		}
		if (mBtn2.getVisibility() == View.VISIBLE) {
			if(mWeekList.getVisibility()== View.VISIBLE){
				if(getBtnListType()!=BtnTypeEnum.EDIT_DELETE_CANCEL){
					mBtn2.setNextFocusUpId(mWeekList.getId());
				}else{
					mBtn2.setNextFocusUpId(mBtn2.getId());
				}
			}
			if(getBtnListType()!=BtnTypeEnum.EDIT_DELETE_CANCEL){
				mBtn2.setNextFocusUpId(id);
			}else{
				mBtn2.setNextFocusUpId(mBtn2.getId());
			}
			if(mBtn1.getVisibility()==View.VISIBLE){
				mBtn2.setNextFocusLeftId(mBtn1.getId());
			}else{
				mBtn2.setNextFocusLeftId(mBtn3.getId());
			}
			mBtn2.setNextFocusRightId(mBtn3.getId());
		}
		if (mBtn3.getVisibility() == View.VISIBLE) {
			if(mWeekList.getVisibility()== View.VISIBLE){
				if(getBtnListType()!=BtnTypeEnum.EDIT_DELETE_CANCEL){
					mBtn3.setNextFocusUpId(mWeekList.getId());
				}else{
					mBtn3.setNextFocusUpId(mBtn3.getId());
				}
			}
			if(getBtnListType()!=BtnTypeEnum.EDIT_DELETE_CANCEL){
				mBtn3.setNextFocusUpId(id);
			}else{
				mBtn3.setNextFocusUpId(mBtn3.getId());
			}
			if(mBtn2.getVisibility()==View.VISIBLE){
				mBtn3.setNextFocusLeftId(mBtn2.getId());
			}else{
				mBtn3.setNextFocusLeftId(mBtn1.getId());
			}
			if(mBtn1.getVisibility()== View.VISIBLE){
				mBtn3.setNextFocusRightId(mBtn1.getId());
			}else{
				mBtn3.setNextFocusRightId(mBtn2.getId());
			}

		}

	}

	/**
	 * @return
	 */
	private List<DataItem> initItemList() {
        MtkTvBookingBase item = getScheItem();

		List<DataItem> items = new ArrayList<DataItem>();

		// src type;
		String[] srcArray = mContext.getResources().getStringArray(
                R.array.pvr_tshift_srctype);

		List<TIFChannelInfo> list = TIFChannelManager.getInstance(mContext).
                getAllDTVTIFChannels();
        // queryChanelListAll(TIFFunctionUtil.CH_LIST_MASK, TIFFunctionUtil.CH_LIST_VAL);

		if (list==null||list.isEmpty()) {
			dismiss();
		}
		int defsrctype = 0;
        if (item.getRecordMode() != 0) {
			for(int i=0;i<srcArray.length;i++){
                if (item.getSourceType() == 1) {
					defsrctype = i;
					break;
				}
			}
		}
		String srcTypeTitle = SCHEDULE_PVR_SRCTYPE;
		DataItem srcType = new DataItem(srcTypeTitle,
				mContext.getString(R.string.schedule_pvr_srctype),
				INVALID_VALUE, INVALID_VALUE,
				defsrctype, srcArray, STEP_VALUE,
				DataItem.DataType.OPTIONVIEW);
		items.add(srcType);

		// Channel Num;
		// String[] channelArray = mContext.getResources().getStringArray(
		// R.array.pvr_tshift_channel_temp_list);

		String[] channelArray = new String[list.size()];
		List<Integer> chList = new ArrayList<Integer>();
		for (int i = 0; i < list.size(); i++) {
			String channelName = list.get(i).mDisplayName;
			String channelNumber = ""+list.get(i).mDisplayNumber;
			if (channelName == null) {
				// channelArray[i]=+"";
				channelName = channelNumber;
			}

			if(channelName.equalsIgnoreCase(channelNumber)){
				channelNumber="";	//avoid the situation like this: "14  14".
			}

            channelArray[i] = String.format("CH%s:%3s%s", channelNumber, "",
					channelName);
			chList.add(list.get(i).mMtkTvChannelInfo.getChannelId());


		}

//		String channelListTitle = SCHEDULE_PVR_CHANNELLIST;
		int channelIndex = 0;
		//fix DTV00610793
		if(null != chList && !chList.isEmpty()){
			for(int i=0;i<chList.size();i++){
                if (chList.get(i) == item.getChannelId()) {
					channelIndex = i;
				}
            }
        }
        if (item.getChannelId() == 0) {
            for (int i = 0; i < chList.size(); i++) {
                if (chList.get(i) == EditChannel.getInstance(mContext).getCurrentChannelId()) {
                    channelIndex = i;
                }
            }
//            if (channelIndex == -1) {
//                channelIndex = 0;
//            }
        }

        Util.showDLog(TAG, "schedulelist channelIndex:" + channelIndex + "==>:"
                + channelIndex);

        DataItem channelList = new DataItem(SCHEDULE_PVR_CHANNELLIST,
                mContext.getString(R.string.schedule_pvr_channel_num),
                INVALID_VALUE, INVALID_VALUE,
                channelIndex, channelArray, STEP_VALUE,
                DataItem.DataType.OPTIONVIEW);
        items.add(channelList);

        // start date
        long startDate = item.getRecordStartTime();
        if (startDate == 0) {
            startDate = System.currentTimeMillis() / 1000;
        }

        DataItem timeSetDate = new DataItem(TIME_DATE,
                mContext.getString(R.string.schedule_pvr_start_date),
                INVALID_VALUE, INVALID_VALUE,
                0, null, STEP_VALUE,
                DataItem.DataType.DATETIMEVIEW);
        timeSetDate.setmDateTimeType(DateTimeView.DATETYPE);
        timeSetDate.setmDateTimeStr(Util.timeToDateStringEx(startDate*1000, 0));
        //TODO
        timeSetDate.setAutoUpdate(false);

        DataItem timeSetTime = new DataItem(TIME_TIME,
                mContext.getString(R.string.schedule_pvr_start_time),
                INVALID_VALUE, INVALID_VALUE,
                0, null, STEP_VALUE,
                DataItem.DataType.DATETIMEVIEW);
        timeSetTime.setmDateTimeType(DateTimeView.TIMETYPE);
        String str = Util.timeToTimeStringEx(startDate * 1000,0);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime = " + startDate +  " str = " + str);
        timeSetTime.setmDateTimeStr(str);
        timeSetTime.setAutoUpdate(false);

        DataItem timeSetTime2 = new DataItem(TIME_TIME,
                mContext.getString(R.string.schedule_pvr_stop_time),
                INVALID_VALUE, INVALID_VALUE,
                0, null, STEP_VALUE,
                DataItem.DataType.DATETIMEVIEW);
        timeSetTime2.setmDateTimeType(DateTimeView.TIMETYPE);
        long duration = item.getRecordDuration();
        long endtime = startDate + duration;
        String str2 = Util.timeToTimeStringEx(endtime * 1000,0);
        timeSetTime2.setmDateTimeStr(str2);
        timeSetTime2.setAutoUpdate(false);

        items.add(timeSetDate);
        items.add(timeSetTime);
        items.add(timeSetTime2);

        // schedule type;
        boolean issupportpvr =DataSeparaterUtil.getInstance() != null && DataSeparaterUtil.getInstance().isSupportPvr();
    	String[] reminderArray  = mContext.getResources().getStringArray(
                R.array.pvr_tshift_schedule_type);

    	String[] scheduelWatchArray = new String[]{reminderArray[1]};

        String reminderTitle = SCHEDULE_PVR_REMINDER_TYPE;
        DataItem reminderType = new DataItem(reminderTitle,
                mContext.getString(R.string.schedule_pvr_reminder_type),
                INVALID_VALUE, INVALID_VALUE,
                item.getRecordMode() == 2 ? 0 : item.getRecordMode(),
                		issupportpvr?reminderArray:scheduelWatchArray,
                STEP_VALUE, DataItem.DataType.OPTIONVIEW);
        items.add(reminderType);

        // Repeat type;
        String[] repeatArray = mContext.getResources().getStringArray(
                R.array.pvr_tshift_repeat_type);
        String repeatTitle = SCHEDULE_PVR_REPEAT_TYPE;
        int repeatmode = 0;
        if (item.getRepeatMode() == 0 ) {
            repeatmode = 2;
        } else if(item.getRepeatMode() == 128 ) {
            repeatmode = 0;
        }else{
        	repeatmode=1;
        }
        DataItem repeatType = new DataItem(repeatTitle,
                mContext.getString(R.string.schedule_pvr_repeat_type),
                INVALID_VALUE, INVALID_VALUE,
                repeatmode, repeatArray, STEP_VALUE,
                DataItem.DataType.OPTIONVIEW);
        items.add(repeatType);

        return items;
    }

    private void initListener() {
        // TODO Auto-generated method stub

        // btns,listener13
        // btns,listener2
        mDataItemList.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("mDataItemList.onFocusChange," , v.getClass().getSimpleName());
                if (v instanceof OptionView) {
                    if (hasFocus) {
                        ((OptionView) v).setRightImageSource(true);
                    } else
                    {
                        ((OptionView) v).setRightImageSource(false);
                    }
                }
            }
        });
        mDataItemList.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // updateDissmissTimer();
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mDataItemList.onKey = " + keyCode);
                switch (keyCode) {
                    case KeyMap.KEYCODE_DPAD_UP:
                        return false;
                    case KeyMap.KEYCODE_DPAD_DOWN:
                        return false;
                    case KeyMap.KEYCODE_DPAD_LEFT:
                    case KeyMap.KEYCODE_MTKIR_RED:
                        View optionViewL = mDataItemList.getChildAt(mDataItemList
                                .getSelectedItemPosition());
                        itemPosition = mDataItemList.getSelectedItemPosition();
                        if (optionViewL instanceof RespondedKeyEvent) {
                            ((RespondedKeyEvent) optionViewL).onKeyLeft();

                            if (optionViewL instanceof OptionView) {
                                hiddenWeekList(optionViewL);
                            }

                        } else {
                            Util.showDLog("optionView instanceof RespondedKeyEvent: "
                                    + "false");
                        }
                        return true;
                    case KeyMap.KEYCODE_DPAD_RIGHT:
                    case KeyMap.KEYCODE_MTKIR_GREEN:
                        View optionViewR = mDataItemList.getSelectedView();
                        itemPosition = mDataItemList.getSelectedItemPosition();

                        if (optionViewR instanceof RespondedKeyEvent) {
                            ((RespondedKeyEvent) optionViewR).onKeyRight();

                            if (optionViewR instanceof OptionView) {
                                hiddenWeekList(optionViewR);
                            }
                        } else {
                            Util.showDLog("optionView instanceof RespondedKeyEvent: "
                                    + "false");
                        }

                        return true;
                    case KeyMap.KEYCODE_DPAD_CENTER:

                        break;
                    case KeyMap.KEYCODE_0:
                    case KeyMap.KEYCODE_1:
                    case KeyMap.KEYCODE_2:
                    case KeyMap.KEYCODE_3:
                    case KeyMap.KEYCODE_4:
                    case KeyMap.KEYCODE_5:
                    case KeyMap.KEYCODE_6:
                    case KeyMap.KEYCODE_7:
                    case KeyMap.KEYCODE_8:
                    case KeyMap.KEYCODE_9:
                        View view = mDataItemList.getSelectedView();

                        handleKeyboardInput(view, keyCode);
                        break;
                    case KeyMap.KEYCODE_MTKIR_INFO:

                        dumpValuesInfo();
                        break;

                    default:

                        break;
                }

                if (keyCode == KeyMap.KEYCODE_DPAD_CENTER) {
                    if (mDataItemList.getFocusedChild() != null) {
                        mDataItemList.getFocusedChild().performClick();
                    }

                }

                return false;
            }
        });

        mWeekList.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                // updateDissmissTimer();

                Util.showDLog("mWeekList.setOnKeyListener: keycode:" + keyCode);
                Util.showDLog("mWeekList.setOnKeyListener Action:"
                        + event.getAction());
                if (keyCode == KeyMap.KEYCODE_DPAD_CENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (mWeekList.getSelectedView() != null) {
                        Util.showDLog("mWeekList.getSelectedView():::"
                                + mWeekList.getSelectedView().getClass()
                                        .toString());
                        View view = mWeekList.getSelectedView().findViewById(
                                R.id.schedule_list_item_week_item_cb);
                        if (view != null) {
                            view.performClick();
                        } else {
                            Util.showDLog("findViewById R.id.schedule_list_item_week_item_cb Null");

                        }

                    } else {
                        Util.showDLog("mWeekList.getSelectedView() Null");
                    }

                }

                return false;
            }

        });

        addListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 0. check item's value.
                // 1.prepare scheduleItem data
                // 2.write data to DB.
                MtkTvBookingBase item = prepareItem();
                int code = checkItem(item);
                if (code != 0) {
                    Util.showELog("checkItem fail,Error Code: " + code);
                    Toast.makeText(mContext, "Start time error ! The time now is before current time",
                            Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                List<MtkTvBookingBase> items = getReplaceItems(item);
                if (items != null && !items.isEmpty()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "items===" + items.size());
                    showAddConfirmDialog(items, item);
                } else if (StateScheduleList.getInstance() != null
                		&& StateScheduleList.getInstance().queryItem().size() >= 5) {
                    Toast.makeText(mContext, "Requested add of schedule exceeds maximum allowed!",
                            Toast.LENGTH_SHORT).show();
                }else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "items==" + item.toString());
                    boolean addSuccess = StateScheduleList.getInstance().insertItem(item);
                    if (addSuccess) {
                        handler.sendEmptyMessage(ADD_SUCCESS);
                    }
                }
            }

        };

        replaceListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 0. check item's value.
                // 1.prepare scheduleItem data
                // 2.write data to DB.
                MtkTvBookingBase item = prepareItem();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "item 1 =" + item.toString());
                int code = checkItem(item);

                if (code == 0) {
                    List<MtkTvBookingBase> items = getReplaceItems(item);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " items 1 = " + items);
                    if (items != null && !items.isEmpty()) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "items===" + items.size());
                        handler.sendEmptyMessageDelayed(222, 300);
                        showReplaceConfirmDialog(items, item);
                        // removeFocus();
                    } else {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "items ==null");
                        handler.sendEmptyMessageDelayed(222, 300);
                        StateScheduleList.getInstance().replaceItem(item);
                        ScheduleListItemInfoDialog.this.dismiss();
                        ScheduleListDialog scheduleListDialog = new ScheduleListDialog(mContext,position);
                        scheduleListDialog.setEpgFlag(epgFlag);
                        scheduleListDialog.show();
                    }

                } else {
                    Toast.makeText(mContext, "Replace fail", Toast.LENGTH_SHORT)
                            .show();
                }

            }
        };

        cancelListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScheduleListDialog scheduleListDialog = new ScheduleListDialog(mContext,position);
                scheduleListDialog.setEpgFlag(epgFlag);
                scheduleListDialog.show();
                ScheduleListItemInfoDialog.this.dismiss();
            }
        };

        deleteListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmDialog();
            }
        };

        editListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBtnListType(BtnTypeEnum.REPLACE_ADD_CANCEL);
                reSetBtnList();
            }
        };

    }

    private void hiddenWeekList(View optionViewL) {
        DataItem item = mDataList.get(itemPosition);
        if (item.getmItemID().equalsIgnoreCase(
                SCHEDULE_PVR_REPEAT_TYPE)) {
            int value = ((OptionView) optionViewL).getValue();
            if (value == 0 || value == 2) {
                mWeekList.setVisibility(View.INVISIBLE);
            } else {
                mWeekList.setVisibility(View.VISIBLE);
            }
        }

    }
private  MtkTvBookingBase getRemoveList(List<MtkTvBookingBase> mtktvbooklist,MtkTvBookingBase m){

	for(MtkTvBookingBase mm : mtktvbooklist){
		if(mm.getRecordStartTime()==m.getRecordStartTime()&&
			mm.getEventTitle().equals(m.getEventTitle())&&
			mm.getChannelId()==m.getChannelId()&&
			mm.getDeviceIndex()==m.getDeviceIndex()&&
			mm.getGenre()==m.getGenre()&&
			mm.getRecordDuration()==m.getRecordDuration()&&
			mm.getRecordDelay()==m.getRecordDelay()&&
			mm.getRecordMode()==m.getRecordMode()&&
			mm.getRepeatMode()==m.getRepeatMode()&&
			mm.getSourceType()==m.getSourceType()&&
			mm.getTunerType()==m.getTunerType()&&
			mm.getRsltMode()==m.getRsltMode()&&
			mm.getInfoData()==m.getInfoData()){
			m.setBookingId(mm.getBookingId());
		}
	}
	return m;
}


    private List<MtkTvBookingBase> getReplaceItems(MtkTvBookingBase item) {
        Long startTime = item.getRecordStartTime();
        Long endTime = startTime + item.getRecordDuration();
        List<MtkTvBookingBase> replacetItems = new ArrayList<MtkTvBookingBase>();
        List<MtkTvBookingBase> items = null;
        items = StateScheduleList.getInstance().queryItem();
        if (items != null && !items.isEmpty()) {
            for (MtkTvBookingBase sItem : items) {
                Long sstartTime = sItem.getRecordStartTime();
                Long sendTime = sstartTime + sItem.getRecordDuration();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " |startTime " + startTime +" |endTime " + endTime
                		+ " |sstartTime "+ sstartTime + " |sendTime " + sendTime );
                if (startTime <= sstartTime && endTime > sstartTime) {
                    replacetItems.add(sItem);

                } else if (startTime >= sstartTime && endTime <= sendTime) {
                    replacetItems.add(sItem);
                } else if (startTime < sendTime && endTime >= sendTime) {
                    replacetItems.add(sItem);
                } else if (item.getRepeatMode() == 2) {// daily
                    long mod = (sstartTime - startTime) / (24 * 60 * 60);
                    long yushu = (sstartTime - startTime) % (24 * 60 * 60);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mod===" + mod + "yushu==" + yushu);
                    // if (mod >= 0) {
                        if (yushu != 0 && yushu > 86300) {
                            mod = mod + 1;
                        }
                        if (yushu > -60 && yushu < 0) {
                            startTime = startTime + mod * 24 * 60 * 60 + yushu;
                            endTime = endTime + mod * 24 * 60 * 60 + yushu;
                        } else {
                            startTime = startTime + mod * 24 * 60 * 60;
                            endTime = endTime + mod * 24 * 60 * 60;
                        }
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime=" + startTime + " sstartTime==" + sstartTime
                                + " endtime=" + endTime + "sendTime==" + sendTime);
                        if (startTime <= sstartTime && endTime > sstartTime) {
                            replacetItems.add(sItem);

                        } else if (startTime >= sstartTime && endTime <= sendTime) {
                            replacetItems.add(sItem);
                        } else if (startTime < sendTime && endTime >= sendTime) {
                            replacetItems.add(sItem);
                        }
                    // }
                } else if (sItem.getRepeatMode() == 2) {
                    long mod = (startTime - sstartTime) / (24 * 60 * 60);
                    long yushu = (startTime - sstartTime) % (24 * 60 * 60);
                    if (item.getRepeatMode() != 2) {
                    	if (mod < 0 ) {
							return replacetItems ;
						}
                    }
                    if (yushu < 60) {
                        startTime = startTime - mod * 24 * 60 * 60 - yushu;
                        endTime = endTime - mod * 24 * 60 * 60 - yushu;
                    } else {
                        startTime = startTime - mod * 24 * 60 * 60;
                        endTime = endTime - mod * 24 * 60 * 60;
                    }
                    if (startTime <= sstartTime && endTime > sstartTime) {
                        replacetItems.add(sItem);
                    } else if (startTime >= sstartTime && endTime <= sendTime) {
                        replacetItems.add(sItem);
                    } else if (startTime < sendTime && endTime >= sendTime) {
                        replacetItems.add(sItem);
                    }
                }
            }
        }
        return replacetItems;
    }

    // p
    // {
    // if (optionViewR instanceof RespondedKeyEvent) {
    // ((RespondedKeyEvent) optionViewR).onKeyRight();
    //
    // if (optionViewR instanceof OptionView) {
    // hiddenWeekList(optionViewR);
    // }
    // }
    // }

    private void dumpValuesInfo() {
        // String str = "";
        // for (int i = 0; i < mDataItemList.getCount(); i++) {
        // View view = mDataItemList.getChildAt(i);
        //
        // if (view instanceof DateTimeInputView) {
        // str = ((DateTimeInputView) view).getmDateTimeView().getmDate();
        // } else if (view instanceof OptionView) {
        // str = ((OptionView) view).getValueView().getText().toString();
        // }
        //
        // Util.showDLog("dumpValuesInfo,value: " + i + " :" + str);
        // }
        // boolean isChecked = false;
        //
        // for (int i = 0; i < 7; i++) {
        // isChecked = ((CheckBox) mWeekList.getChildAt(i).findViewById(
        // R.id.schedule_list_item_week_item_cb)).isChecked();
        // Util.showDLog("dumpValuesInfo,isChecked? " + i + " :" + isChecked);
        // }
		if(getCurrentFocus()!=null) {
			String focusName = getCurrentFocus().getClass().getSimpleName();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("focusName:", focusName);
		}
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Util.showDLog("keyCode UP:" + keyCode);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyUp  = " + keyCode);
    	if (mDataItemList == null){
    		return false;
    	}

        if (mDataItemList.hasFocus()) {
        	View optionView = (View) mDataItemList.getChildAt(itemPosition);
        	if (!isTTSKey) {
            ((SetConfigListViewAdapter) mDataItemList.getAdapter()).setSelectPos(mDataItemList
                    .getSelectedItemPosition());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mDataItemList.getSelectedItemPosition() = " + mDataItemList.getSelectedItemPosition());
            showHightLight(mDataItemList.getSelectedItemPosition());
        	}

//        	int  a = mDataItemList.getSelectedItemPosition();
//        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getSelectedItemPosition = " + a);
//            itemPosition = mDataItemList.getSelectedItemPosition();
            switch (keyCode) {
        case KeyMap.KEYCODE_DPAD_LEFT:
        case KeyMap.KEYCODE_MTKIR_RED:
//            View optionViewL = mDataItemList.getChildAt(mDataItemList
//                    .getSelectedItemPosition());

            if (optionView instanceof RespondedKeyEvent && isTTSKey ) {
                ((RespondedKeyEvent) optionView).onKeyLeft();

                if (optionView instanceof OptionView) {
                    hiddenWeekList(optionView);
                }

            } else {
                Util.showDLog("optionView instanceof RespondedKeyEvent: "
                        + "false");
            }
            return true;
        case KeyMap.KEYCODE_DPAD_RIGHT:
        case KeyMap.KEYCODE_MTKIR_GREEN:
//            View optionViewR = mDataItemList.getSelectedView();

            if (optionView instanceof RespondedKeyEvent && isTTSKey ) {
                ((RespondedKeyEvent) optionView).onKeyRight();

                if (optionView instanceof OptionView) {
                    hiddenWeekList(optionView);
                }
            } else {
                Util.showDLog("optionView instanceof RespondedKeyEvent: "
                        + "false");
            }

            return true;
        case KeyMap.KEYCODE_DPAD_CENTER:

            break;
        case KeyMap.KEYCODE_0:
        case KeyMap.KEYCODE_1:
        case KeyMap.KEYCODE_2:
        case KeyMap.KEYCODE_3:
        case KeyMap.KEYCODE_4:
        case KeyMap.KEYCODE_5:
        case KeyMap.KEYCODE_6:
        case KeyMap.KEYCODE_7:
        case KeyMap.KEYCODE_8:
        case KeyMap.KEYCODE_9:
//            View view = mDataItemList.getSelectedView();
            if (optionView instanceof RespondedKeyEvent && isTTSKey ) {
            	handleKeyboardInput(optionView, keyCode);
            } else {
                Util.showDLog("optionView instanceof RespondedKeyEvent: "
                        + "false");
            }
            break;
        case KeyMap.KEYCODE_MTKIR_INFO:

            dumpValuesInfo();
            break;
         default:
        	 break;
            }
        } else {
            if (keyCode == KeyMap.KEYCODE_DPAD_DOWN) {

                if (mDataItemList.getSelectedItemPosition() == (mDataItemList.getCount() - 1)
                        && !mBtn1.hasFocus() && !mBtn2.hasFocus() && !mBtn3.hasFocus()) {
                    if (mWeekList.getVisibility() == View.VISIBLE) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"com.mediatek.wwtv.tvcenter.util.MtkLog");
                    } else {
                        mBtn3.requestFocus();
                    }
                    if (mWeekList.getVisibility() == View.VISIBLE
                            && mWeekList.getSelectedItemPosition() > 3) {
                        if (weekflag) {
                            mBtn3.requestFocus();
                            weekflag = false;
                        }

                    }
                    reSetFocusPath();
                    hiddenHightLight();
                    ((SetConfigListViewAdapter) mDataItemList.getAdapter()).setSelectPos(-1);
                } else if (mDataItemList.getSelectedItemPosition() == 0
                        && !mBtn1.hasFocus() && !mBtn2.hasFocus() && !mBtn3.hasFocus()){
                    if (mWeekList.getVisibility() == View.VISIBLE && mWeekList.hasFocus()
                            && mWeekList.getSelectedItemPosition() > 3) {
                        mBtn3.requestFocus();
                        reSetFocusPath();
                        hiddenHightLight();
                        ((SetConfigListViewAdapter) mDataItemList.getAdapter()).setSelectPos(-1);
                    }

                }else if (mBtn1.hasFocus() || mBtn2.hasFocus() || mBtn3.hasFocus()) {
                    if (getBtnListType() != BtnTypeEnum.EDIT_DELETE_CANCEL) {
                        if (mDataItemList.getChildAt(0) != null) {
                            mWeekList.setSelection(-1);
                            mDataItemList.requestFocus();
                            mDataItemList.setSelection(0);
                            ((SetConfigListViewAdapter) mDataItemList.getAdapter()).setSelectPos(0);
                            showHightLight(mDataItemList.getSelectedItemPosition());
                        }
                    } else {
                        if (mBtn1.hasFocus()){
                        	 mBtn1.requestFocus();
                        }

                        if (mBtn2.hasFocus()){
                        	 mBtn2.requestFocus();
                        }

                        if (mBtn3.hasFocus()){
                        	 mBtn3.requestFocus();
                        }

                    }
                } else {
                    hiddenHightLight();
                    ((SetConfigListViewAdapter) mDataItemList.getAdapter()).setSelectPos(-1);
                }
            } else {
                hiddenHightLight();
                ((SetConfigListViewAdapter) mDataItemList.getAdapter()).setSelectPos(-1);
            }
        }
        this.updateDissmissTimer();
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        keyCode = KeyMap.getKeyCode(keyCode, event);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown = " + keyCode);
        switch (keyCode) {
            case KeyMap.KEYCODE_DPAD_DOWN:
                if (mWeekList.getVisibility() == View.VISIBLE
                        && mWeekList.getSelectedItemPosition() > 3) {
                    if (mBtn1.hasFocus() || mBtn2.hasFocus() || mBtn3.hasFocus()) {
                        return false;
                    } else {
                        weekflag = true;
                        return true;
                    }

                }
                break;
            case KeyMap.KEYCODE_DPAD_UP:
                if (mDataItemList.hasFocus() && mDataItemList.getSelectedItemPosition() == 0) {
                    mBtn3.requestFocus();
                    return true;
                }
                break;
            case KeyMap.KEYCODE_MTKIR_INFO:

                dumpValuesInfo();
                break;
            case KeyMap.KEYCODE_DPAD_CENTER:
                if (mWeekList.hasFocus()) {
                    Util.showDLog("mWeekList.hasFocus()");
                    ((CheckBox) mWeekList.getFocusedChild()).performClick();
                }

                if (mDataItemList.hasFocus()) {
                    Util.showDLog("mDataItemList.hasFocus()");
					if(mDataItemList.getFocusedChild()!=null){
						(mDataItemList.getFocusedChild()).performClick();
					}

                }

                break;
            case KeyMap.KEYCODE_BACK:
                ScheduleListDialog scheduleListDialog = new ScheduleListDialog(mContext,position);
                scheduleListDialog.setEpgFlag(epgFlag);
                scheduleListDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);//important
                scheduleListDialog.show();
                this.dismiss();
                Util.showDLog(TAG, "KEYCODE_BACK");

                break;
            case KeyMap.KEYCODE_MTKIR_CHUP:
                if (!DestroyApp.isCurEPGActivity()) {
                    CommonIntegration.getInstance().channelUp();
                }
                break;
            case KeyMap.KEYCODE_MTKIR_CHDN:
                if (!DestroyApp.isCurEPGActivity()) {
                    CommonIntegration.getInstance().channelDown();
                }
                break;
            case KeyMap.KEYCODE_MTKIR_PRECH:
                if (!DestroyApp.isCurEPGActivity()) {
                    CommonIntegration.getInstance().channelPre();
                }
                break;
             default:
            	 break;

//            case KeyMap.KEYCODE_MTKIR_CHUP:
//                CommonIntegration.getInstance().channelUp();
//                break;
//            case KeyMap.KEYCODE_MTKIR_CHDN:
//                CommonIntegration.getInstance().channelDown();
//                break;
//            case KeyMap.KEYCODE_MTKIR_PRECH:
//                CommonIntegration.getInstance().channelPre();
//                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        // CheckBox tmp = (CheckBox) view
        // .findViewById(R.id.schedule_list_item_week_item_cb);
        // tmp.setChecked(!tmp.isChecked());
        Util.showDLog("onItemClick.................." + mDataItemList);
    }

    public void setCallback(StateScheduleListCallback callback) {
        mCallback = callback;
    }

    public StateScheduleListCallback<?> getmCallback() {
        return mCallback;
    }


    // Deal with key figures input
    private boolean handleKeyboardInput(View view, int keyCode) {
        char ch = 0;
        switch (keyCode) {
            case KeyMap.KEYCODE_0:
                ch = '0';
                break;
            case KeyMap.KEYCODE_1:
                ch = '1';
                break;
            case KeyMap.KEYCODE_2:
                ch = '2';
                break;
            case KeyMap.KEYCODE_3:
                ch = '3';
                break;
            case KeyMap.KEYCODE_4:
                ch = '4';
                break;
            case KeyMap.KEYCODE_5:
                ch = '5';
                break;
            case KeyMap.KEYCODE_6:
                ch = '6';
                break;
            case KeyMap.KEYCODE_7:
                ch = '7';
                break;
            case KeyMap.KEYCODE_8:
                ch = '8';
                break;
            case KeyMap.KEYCODE_9:
                ch = '9';
                break;
            default:
                break;
        }

        if (view instanceof DateTimeInputView) {

            DateTimeInputView dateTimeInputView = (DateTimeInputView) view;
            DateTimeView dateTimeView = dateTimeInputView.getmDateTimeView();
            if (dateTimeView != null) {
                dateTimeView.input(ch, dateTimeInputView.getmDataItem());
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public BtnTypeEnum getBtnListType() {
        return btnListType;
    }

    public void setBtnListType(BtnTypeEnum mType) {
        this.btnListType = mType;
    }

    /**
     * @param item
     * @return error code.like: out of date,...
     */
    private int checkItem(MtkTvBookingBase item) {
        // Error 1,2,3,4,5,6,7...
        final int timeError = 1;

        MtkTvTimeFormatBase mTime = MtkTvTime.getInstance().getBroadcastLocalTime();
	    MtkTvTimeFormatBase timeBaseTo   = new   MtkTvTimeFormatBase();
	    MtkTvTimeBase time = new MtkTvTimeBase();
	    time.convertTime(time.MTK_TV_TIME_CVT_TYPE_BRDCST_LOCAL_TO_SYS_UTC, mTime, timeBaseTo );
	    timeBaseTo.print("Jiayang.li--sys-utc");
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getBroadcastLocalTime == " + timeBaseTo.toSeconds());
        Long startTime = item.getRecordStartTime();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime = " +  startTime + "  currentTime = " +timeBaseTo.toSeconds()+", systime="+ System.currentTimeMillis());
        if (startTime <= (timeBaseTo.toSeconds())) {
            return timeError;
        }

        return 0;
    }

    long startRecordTime = 0l;
    String startDate = "";

    private MtkTvBookingBase prepareItem() {
        MtkTvBookingBase item = new MtkTvBookingBase();

        for (int i = 0; i < mDataItemList.getCount(); i++) {
            View optionViewL = mDataItemList.getChildAt(i);

            switch (i) {
                case UI_Index_SrcType: // src type,default:DTV
                    int srctype = ((OptionView) optionViewL).getValue();
                    // String srcname = srcArray[srctype];
                    item.setSourceType(srctype);
                    break;

                case UI_Index_ChannelNum: // channel number
                    // Fix CR: DTV00583013
                    try {
                        int channelIndex = ((OptionView) optionViewL).getValue();
                        String channlString = TIFChannelManager.getInstance(mContext)
                                .getAllDTVTIFChannels()
                                .get(channelIndex)
                                .mDisplayNumber;
//                        int channelNum = Integer.valueOf(channlString.split("-")[0]);
                        MtkTvChannelInfoBase mChannelInfo = TIFChannelManager
                                .getInstance(mContext)
                                .getAllDTVTIFChannels()
                                .get(channelIndex)
                                .mMtkTvChannelInfo;
                        int channelID = mChannelInfo.getChannelId();
                        item.setEventTitle(""+channlString);
                        item.setChannelId(channelID);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case UI_Index_StartDate: // start date
                    startDate = ((DateTimeInputView) optionViewL)
                            .getmDateTimeView().getmDate();
                    // item.
                    break;

                case UI_Index_StartTime: // start time
                    String startTime = ((DateTimeInputView) optionViewL)
                            .getmDateTimeView().getmDate();


					if (0 == VendorProperties.mtk_system_timesync_existed().orElse(0)) {
						startRecordTime = Util.strToTimeEx(startDate + " " + startTime , 0);
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"schedulelist=="+startRecordTime);
					    MtkTvTimeFormatBase timeBaseFrom = new   MtkTvTimeFormatBase();
					    timeBaseFrom.setByUtc(startRecordTime);
					    MtkTvTimeFormatBase timeBaseTo   = new   MtkTvTimeFormatBase();

					    MtkTvTimeBase time = new MtkTvTimeBase();
//						com.mediatek.wwtv.tvcenter.util.MtkLog.d("Jiayang.li", "timeFrom_startTIme:" + timeBaseFrom.toUtc()  );
					    timeBaseFrom.print("Jiayang.li --brdcst-local");
					    time.convertTime(time.MTK_TV_TIME_CVT_TYPE_BRDCST_LOCAL_TO_SYS_UTC, timeBaseFrom, timeBaseTo );
					    timeBaseTo.print("Jiayang.li--sys-utc");
					    startRecordTime = timeBaseTo.toSeconds();
					}
				else{
					startRecordTime = Util.strToTime(startDate + " " + startTime , 0);
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"schedulelist no=="+startRecordTime);
					}
                    item.setRecordStartTime(startRecordTime);

					 com.mediatek.wwtv.tvcenter.util.MtkLog.d("{ScheduleListTimeInfo}", "setRecordStartTime:" + startRecordTime  );
                    break;

                case UI_Index_StopTime: // stop time
                    String endTime = ((DateTimeInputView) optionViewL)
                            .getmDateTimeView().getmDate();

					long startRecordEndTime = 0;

					if (0 == VendorProperties.mtk_system_timesync_existed().orElse(0)) {
                          startRecordEndTime = Util.strToTimeEx(startDate + " " + endTime , 0);
						  MtkTvTimeFormatBase timeBaseFrom = new   MtkTvTimeFormatBase();
						  timeBaseFrom.setByUtc(startRecordEndTime);
						  MtkTvTimeFormatBase timeBaseTo   = new   MtkTvTimeFormatBase();

						  MtkTvTimeBase time = new MtkTvTimeBase();
						  time.convertTime(time.MTK_TV_TIME_CVT_TYPE_BRDCST_LOCAL_TO_SYS_UTC, timeBaseFrom, timeBaseTo );
						  startRecordEndTime = timeBaseTo.toSeconds();

						}
					else{

                          startRecordEndTime = Util.strToTime(startDate + " " + endTime , 0);
						}

                    long duration = startRecordEndTime - startRecordTime;
                    if (duration <= 0) {
                        duration += 24 * 60 * 60;
                    }

					com.mediatek.wwtv.tvcenter.util.MtkLog.d("{ScheduleListTimeInfo}", "setRecordDuration" + duration );
                    item.setRecordDuration(duration);
                    break;

                case UI_Index_ScheduleType: // schedule type
                    int scheduleType = ((OptionView) optionViewL).getValue();
                    if (scheduleType == 0) {
                        scheduleType += 2;
                    }
                    item.setRecordMode(scheduleType);
                    break;

                case UI_Index_RepeatType: // repeat type
//                    int repeatType = ((OptionView) optionViewL).getValue();
                    // item.setRepeatMode(repeatType);
                    break;
                default:
                    break;
            }
        }
        int repeatType = ((OptionView) mDataItemList
                .getChildAt(UI_Index_RepeatType)).getValue();
        StringBuilder weekValue = new StringBuilder();
        if (repeatType == 1) { // weekly
            int weekdey = dayForWeek(startDate);
            int diff = 0;
            int diff2 = 7;
            for (int i = 6; i >= 0; i--) {
                boolean selected = ((WeekListAdapter) mWeekList.getAdapter())
                        .isChecked(i);
                if (selected) {
                    if ((i + 1) - weekdey > 0) {
                        diff = i + 1 - weekdey;
                    } else if ((i + 1) - weekdey < 0) {
                        diff = 7 - (weekdey - (i + 1));
                    } else {
                        diff = 0;
                    }
                    if (diff2 >= diff) {
                        diff2 = diff;
                    }
                }
                weekValue.append(selected ? 1 : 0);
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "diff" + diff + "   diff2==" + diff2);
            startRecordTime = startRecordTime + getday(Integer.parseInt(weekValue.toString(), 2)) * 24 * 60 * 60;
            item.setRecordStartTime(startRecordTime);
            item.setRepeatMode(Integer.parseInt(weekValue.toString(), 2));
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"2 to 10 =="+Integer.parseInt(weekValue.toString(), 2));
        } else if (repeatType == 2) {//daily
            item.setRepeatMode(0);
        } else {
            item.setRepeatMode(128);//once
        }

        // item.setTaskID(getScheItem().getTaskID());
        item.setTunerType(CommonIntegration.getInstance().getTunerMode());
        return item;
    }

    private  int getday(int repeatcount){
    	int weekday = MtkTvTime.getInstance().getLocalTime().weekDay;
		int count = 0;
		for (int i = 6; i >= 0; i--) {
			if ((repeatcount & (1 << i)) == 1) {
				if (count > (i - weekday)){
					count = i - weekday;
				}

			}
			if (count < 0) {
				count = count + 7;
			}
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"count=="+count);
		return count;
    }

    public int dayForWeek(String pTime){
        try {
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = sdFormat.parse(pTime);
        Calendar calendar = new GregorianCalendar();
        calendar.set(date.getYear(), date.getMonth(), date.getDay());
        return calendar.get(Calendar.DAY_OF_WEEK);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * @return the mScheItem
     */
    public MtkTvBookingBase getScheItem() {
        return mScheItem;
    }

    /**
     * @param mScheItem the mScheItem to set
     */
    public void setScheItem(MtkTvBookingBase mScheItem) {
        this.mScheItem = mScheItem;
    }

    private void showDeleteConfirmDialog() {
        List<MtkTvBookingBase> items = new ArrayList<MtkTvBookingBase>();
        items.add(prepareItem());
        confirm = new SpecialConfirmDialog(mContext,
                items);

        confirm.setPositiveButton(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showDLog("showDeleteConfirmDialog().PositiveButton");
                StateScheduleList.getInstance().deleteItem(getScheItem());
                ScheduleListDialog scheduleListDialog = new ScheduleListDialog(mContext,position);
                scheduleListDialog.setEpgFlag(epgFlag);
                scheduleListDialog.show();
                dismiss();
            }
        });

        confirm.setNegativeButton(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showDLog("showDeleteConfirmDialog().NegativeButton");
                dismiss();
            }
        });

        String title1 = mContext.getResources().getString(
                R.string.pvr_schedulelist_delete_line1);
        String title2 = mContext.getResources().getString(
                R.string.pvr_schedulelist_delete_line2);
        confirm.setTitle(title1, title2);
        confirm.show();
    }

    private void showAddConfirmDialog(final List<MtkTvBookingBase> items,
            final MtkTvBookingBase currenItem) {
        confirm = new SpecialConfirmDialog(mContext,
                items);
        confirm.setPositiveButton(null);
        confirm.setNegativeButton(null);
        confirm.setPositiveButton(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showDLog("showDeleteConfirmDialog().PositiveButton");
                for (MtkTvBookingBase item : items) {
                    int bookingid = item.getBookingId();
                    StateScheduleList.getInstance().deleteItem(item);
                    for (MtkTvBookingBase item1 : items) {
                        int bookingid1 = item1.getBookingId();
                        if (bookingid < bookingid1) {
                            item1.setBookingId(bookingid1 - 1);
                        }
                    }
                }
                StateScheduleList.getInstance().insertItem(currenItem);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showAddConfirmDialog + currenItem  = " +currenItem.toString());
                ScheduleListDialog scheduleListDialog = new ScheduleListDialog(mContext,position);
                scheduleListDialog.setEpgFlag(epgFlag);
                scheduleListDialog.show();
                dismiss();

            }
        });
        confirm.setNegativeButton(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showDLog("showDeleteConfirmDialog().NegativeButton");
                ScheduleListDialog scheduleListDialog = new ScheduleListDialog(mContext,position);
                scheduleListDialog.setEpgFlag(epgFlag);
                scheduleListDialog.show();
                dismiss();
            }
        });
        confirm.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface arg0) {
                // TODO Auto-generated method stub
                handler.sendEmptyMessageDelayed(333, 300);
            }
        });
        String title1 = mContext.getResources().getString(
                R.string.pvr_schedulelist_replace_line1);
        String title2 = mContext.getResources().getString(
                R.string.pvr_schedulelist_replace_line2);

        confirm.setTitle(title1, title2);
        confirm.show();
    }

    private void showReplaceConfirmDialog(final List<MtkTvBookingBase> items,
            final MtkTvBookingBase currenItem) {
        // List<MtkTvBookingBase> items = new ArrayList<MtkTvBookingBase>();
        // items.add(getScheItem());
        confirm = new SpecialConfirmDialog(mContext,
                items);
        confirm.setPositiveButton(null);
        confirm.setNegativeButton(null);

        confirm.setPositiveButton(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showDLog("showDeleteConfirmDialog().PositiveButton");
                StateScheduleList.getInstance().deleteItem(getScheItem());//delete itself.
                StateScheduleList.getInstance().deleteItem(getRemoveList(items, currenItem));
                for (MtkTvBookingBase item : items) {
                    int bookingid = item.getBookingId();
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "item 2 = " + item.toString());
                    StateScheduleList.getInstance().deleteItem(item);
                    for (MtkTvBookingBase item1 : items) {
                        int bookingid1 = item1.getBookingId();
                        if (bookingid < bookingid1) {
                            item1.setBookingId(bookingid1 - 1);
                        }
                    }
                }

                StateScheduleList.getInstance().insertItem(currenItem);
                ScheduleListDialog scheduleListDialog = new ScheduleListDialog(mContext,position);
                scheduleListDialog.setEpgFlag(epgFlag);
                scheduleListDialog.show();
                dismiss();

            }
        });
        confirm.setNegativeButton(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showDLog("showDeleteConfirmDialog().NegativeButton");
                dismiss();
            }
        });
        confirm.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface arg0) {
                // TODO Auto-generated method stub
                handler.sendEmptyMessageDelayed(333, 300);
            }
        });
        String title1 = mContext.getResources().getString(
                R.string.pvr_schedulelist_replace_line1);
        String title2 = mContext.getResources().getString(
                R.string.pvr_schedulelist_replace_line2);

        confirm.setTitle(title1, title2);
        confirm.show();
    }

    // Adapter:
    public class WeekListAdapter extends BaseAdapter {

        // HashMap<String, Boolean>
        private final List<Boolean> weekList = new ArrayList<Boolean>();
        private ViewGroup mGroup;

        public WeekListAdapter(Map<String, Boolean> list) {
            for (String mString : WEEK_ARRAY) {
            	if(list.get(mString) == null){
            		weekList.add(false);
            	}else{
            		weekList.add(list.get(mString));
            	}
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(
                        R.layout.pvr_tshift_schedule_list_item_week, null);
                mGroup = parent;
            } else {
                view = convertView;
            }
            if (view.isFocused()) {
                view.setBackgroundResource(R.drawable.top_focus_img);
            } else {
                view.setBackgroundResource(Color.TRANSPARENT);
            }
            setItemValue(view, WEEK_ARRAY[position], getItem(position));
            return view;
        }

        /**
         * @param view
         * @param item
         */
        private void setItemValue(View view, String name, Boolean item) {
            CheckBox tmp = (CheckBox) view
                    .findViewById(R.id.schedule_list_item_week_item_cb);
            tmp.setChecked(item);
            tmp.setText(name);

        }

        @Override
        public final int getCount() {
            return weekList.size();
        }

        @Override
        public final Boolean getItem(int position) {
            return weekList.get(position);
        }

        public final Boolean isChecked(int position) {
            View view = mGroup.getChildAt(position).findViewById(
                    R.id.schedule_list_item_week_item_cb);
            if (view instanceof CheckBox) {
                return ((CheckBox) view).isChecked();
            } else {
                return false;
            }
        }

        @Override
        public final long getItemId(int position) {
            return position;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
            long id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub

    }

    public void updateDissmissTimer() {
    	// TODO Auto-generated method stub
    }

    // fix DTV00586312
    private void showHightLight(int id) {
        if (id == 1) {
            ((OptionView) mDataItemList.getChildAt(id)).setRightImageSource(true);
            ((OptionView) mDataItemList.getChildAt(0)).setRightImageSource(false);
            ((OptionView) mDataItemList.getChildAt(5)).setRightImageSource(false);
            ((OptionView) mDataItemList.getChildAt(6)).setRightImageSource(false);
            hiddenYellow();
        } else if (id == 5) {
            ((OptionView) mDataItemList.getChildAt(id)).setRightImageSource(true);
            ((OptionView) mDataItemList.getChildAt(0)).setRightImageSource(false);
            ((OptionView) mDataItemList.getChildAt(1)).setRightImageSource(false);
            ((OptionView) mDataItemList.getChildAt(6)).setRightImageSource(false);
            hiddenYellow();
        } else if (id == 6) {
            ((OptionView) mDataItemList.getChildAt(id)).setRightImageSource(true);
            ((OptionView) mDataItemList.getChildAt(0)).setRightImageSource(false);
            ((OptionView) mDataItemList.getChildAt(1)).setRightImageSource(false);
            ((OptionView) mDataItemList.getChildAt(5)).setRightImageSource(false);
            hiddenYellow();
        } else if (id == 0) {
            ((OptionView) mDataItemList.getChildAt(id)).setRightImageSource(true);
            ((OptionView) mDataItemList.getChildAt(1)).setRightImageSource(false);
            ((OptionView) mDataItemList.getChildAt(5)).setRightImageSource(false);
            ((OptionView) mDataItemList.getChildAt(6)).setRightImageSource(false);
            hiddenYellow();
        } else {
            hiddenHightLight();
            ((DateTimeInputView) mDataItemList.getChildAt(id)).setFlag();
            if (id == 2) {
                ((DateTimeInputView) mDataItemList.getChildAt(3)).setWhiteColor();
                ((DateTimeInputView) mDataItemList.getChildAt(4)).setWhiteColor();
            } else if (id == 3) {
                ((DateTimeInputView) mDataItemList.getChildAt(2)).setWhiteColor();
                ((DateTimeInputView) mDataItemList.getChildAt(4)).setWhiteColor();
            } else if (id == 4) {
                ((DateTimeInputView) mDataItemList.getChildAt(2)).setWhiteColor();
                ((DateTimeInputView) mDataItemList.getChildAt(3)).setWhiteColor();
            }

        }
    }

    // fix DTV00586312
    private void hiddenHightLight() {
        if (mDataItemList != null) {
            if (mDataItemList.getChildAt(5) != null){
            	((OptionView) mDataItemList.getChildAt(5)).setRightImageSource(false);
            }

            if (mDataItemList.getChildAt(0) != null){
            	 ((OptionView) mDataItemList.getChildAt(0)).setRightImageSource(false);
            }

            if (mDataItemList.getChildAt(1) != null){
            	((OptionView) mDataItemList.getChildAt(1)).setRightImageSource(false);
            }

            if (mDataItemList.getChildAt(6) != null){
            	 ((OptionView) mDataItemList.getChildAt(6)).setRightImageSource(false);
            }

        }
    }

    // fix DTV00586900
    private void hiddenYellow() {
        if (mDataItemList.getChildAt(2) != null){
        	 ((DateTimeInputView) mDataItemList.getChildAt(2)).setWhiteColor();
        }

        if (mDataItemList.getChildAt(3) != null){
        	((DateTimeInputView) mDataItemList.getChildAt(3)).setWhiteColor();
        }

        if (mDataItemList.getChildAt(4) != null){
        	((DateTimeInputView) mDataItemList.getChildAt(4)).setWhiteColor();
        }

    }

    @Override
    public void srcTypeChange(int value) {
        if (mDataItemList != null) {
            OptionView view = (OptionView) mDataItemList.getChildAt(1);
            if (value == 1) {
                view.getValueView().setVisibility(View.INVISIBLE);
            } else {
                view.getValueView().setVisibility(View.VISIBLE);
            }
        }

    }

    public boolean isEpgFlag() {
        return epgFlag;
    }

    public void setEpgFlag(boolean epgFlag) {
        this.epgFlag = epgFlag;
    }

   /* public void show() {
        // TODO Auto-generated method stub
        super.show();
    }*/

    public static ScheduleListItemInfoDialog getscheduleListItemInfoDialog(){
    	if(scheduleListItemInfoDialog == null){
    		return null;
		}
	    return  scheduleListItemInfoDialog.get();
    }
}
