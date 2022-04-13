package com.mediatek.wwtv.tvcenter.dvr.ui;

import android.util.Log;
import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.LayoutDirection;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.text.TextUtilsCompat;

import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;
import com.mediatek.wwtv.setting.base.scan.adapter.NewScheduleAddAdapter;
import com.mediatek.wwtv.setting.base.scan.model.StateScheduleList;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.util.Util;
import com.mediatek.wwtv.setting.view.DateTimeInputView;
import com.mediatek.wwtv.setting.view.DateTimeView;
import com.mediatek.wwtv.setting.widget.view.ScheduleListDialog;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.controller.RegistOnDvrDialog;
import com.mediatek.wwtv.tvcenter.dvr.controller.ScheduleItemModel;
import com.mediatek.wwtv.tvcenter.dvr.controller.SubScheduleAdapter;
import com.mediatek.wwtv.tvcenter.dvr.db.SubScheduleItemModel;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog.OnConfirmClickListener;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.InstrumentationHandler;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.twoworlds.tv.MtkTvTimeBase;
import com.mediatek.twoworlds.tv.MtkTvRecordBase;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import mediatek.sysprop.VendorProperties;


public class ScheduleListItemDialog extends CommonDialog {
    private final static String TAG = "ScheduleListItemDialog[new]";
    private MtkTvBookingBase mScheItem;
    private ListView listView;
    private ListView channel_repeat_list;
    private ListView weekListView;
    private TextView titleTextView;
    NewScheduleAddAdapter adapter;
    SimpleDateFormat sdfDate;
    String[] channelArray;
    DateTimeView dateTimeView;
    String startDateString;
    String startTimeString;
    String endTimeString;
    String dateTypeString;
    int dateType = 2;
    long startRecordTime = 0;
    long startDate = 0;
    int position = 0;
    private int TYPE = 0;// 0:add, 1:edit
    private int eventType = 0;// 0:normal,1:event track
    private int eventId = 0;
    private long mInitStartTime=0;
    private long mInitDurationTime=0;
    private static ScheduleListItemDialog scheduleListItemDialog;
    private LinearLayout editLayout;
    List<ScheduleItemModel> itemModels = new ArrayList<ScheduleItemModel>();
    List<SubScheduleItemModel> scheduleItemModelsmode = new ArrayList<SubScheduleItemModel>();
    List<SubScheduleItemModel> scheduleItemModelsrepeat = new ArrayList<SubScheduleItemModel>();
    List<SubScheduleItemModel> scheduleItemModelevent = new ArrayList<SubScheduleItemModel>();
    List<SubScheduleItemModel> scheduleItemModelweek = new ArrayList<SubScheduleItemModel>();
    private int type = 0;
    private  int channelIndex=0;
    private final static int CHANNELINFO =0;
    private final static int STARTDATE =1;
    private final static int STARTTIME =2;
    private final static int STOPTIME =3;
    private final static int  SCHEDULETYPE=4;
    private final static int REPEATTYPE =5;
    private final static int EVENTMODE =6;
    private final View.AccessibilityDelegate mDelegate = new View.AccessibilityDelegate() {
        public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
                                                       AccessibilityEvent event) {
            do {
                List<CharSequence> texts = event.getText();
                if (texts == null) {
                    break;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"text->"+texts+",type->"+type);
                switch(host.getId()){
                    case R.id.schedulelist_list:
                        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TYPE_VIEW_ACCESSIBILITY_FOCUSED");
                            position = findSelectItem(texts);
                            //listView.setSelection(position);
                        } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TYPE_VIEW_CLICKED");
                            switchType(position);
                        }
                        break;
                    case R.id.channel_repeat_list:
                        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANNEL TYPE_VIEW_ACCESSIBILITY_FOCUSED");
                            position = findSelectMore(texts);
                            channel_repeat_list.setSelection(position);
                        } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "CHANNEL　TYPE_VIEW_CLICKED");
                            setResult(position,type);
                        }
                        break;
                    case R.id.channel_week_list:
                        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "WEEK TYPE_VIEW_ACCESSIBILITY_FOCUSED");
                            position = findSelectMore(texts);
                            weekListView.setSelection(position);
                        } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "WEEK TYPE_VIEW_CLICKED");

                        }
                        break;
                    case R.id.common_datetimeview:
                        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TIME TYPE_VIEW_ACCESSIBILITY_FOCUSED");
                            //position = findSelectItem(texts);
                        } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TIME TYPE_VIEW_CLICKED");

                        }
                        break;
                    default:
                        break;
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

    private int findSelectItem(List<CharSequence> texts){
        int index =0;
        if(!texts.isEmpty()) {
            for(int i=0;i<itemModels.size();i++){
                if(itemModels.get(i).getTitle().equals(texts.get(0).toString())){
                    index = i;
                    return index;
                }
            }

        }
        return index;
    }

    private int findSelectMore(List<CharSequence> texts){
        int index =0;
        if(!texts.isEmpty()) {
            for(int i=0;i<channelArray.length;i++){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,channelArray[i]+"=="+texts.get(0).toString());
                if(channelArray[i].equals(texts.get(0).toString())){
                    index = i;
                    type = 0;
                    return index;
                }
            }

        }
        if(!texts.isEmpty()) {
            for(int i=0;i<scheduleItemModelevent.size();i++){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,scheduleItemModelevent.get(i).getItemString()+"=3="+texts.get(0).toString());
                if(scheduleItemModelevent.get(i).getItemString().equals(texts.get(0).toString())){
                    index = i;
                    type = 3;
                    return index;
                }
            }
        }
        if(!texts.isEmpty()) {
            for(int i=0;i<scheduleItemModelsrepeat.size();i++){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,scheduleItemModelsrepeat.get(i).getItemString()+"=2="+texts.get(0).toString());
                if(scheduleItemModelsrepeat.get(i).getItemString().equals(texts.get(0).toString())){
                    index = i;
                    type = 2;
                    return index;
                }
            }
        }
        if(!texts.isEmpty()) {
            for(int i=0;i<scheduleItemModelsmode.size();i++){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,scheduleItemModelsmode.get(i).getItemString()+"=1="+texts.get(0).toString());
                if(scheduleItemModelsmode.get(i).getItemString().equals(texts.get(0).toString())){
                    index = i;
                    type = 1 ;
                    return index;
                }
            }
        }
        if(!texts.isEmpty()) {
            for(int i=0;i<scheduleItemModelweek.size();i++){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,scheduleItemModelweek.get(i).getItemString()+"=3="+texts.get(0).toString());
                if(scheduleItemModelweek.get(i).getItemString().equals(texts.get(0).toString())){
                    index = i;
                    type = 1 ;
                    return index;
                }
            }
        }
        return index;
    }

    private void setResult(int index,int type){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"index->"+index+",type->"+type);
        int select = 0;
        switch (type){
            case 0:
                setChanneid(index);
                select = 1;
                break;
            case 1:
                getScheItem().setRecordMode(index == 0 ? 2 : 1);
                if(getScheItem().getRecordMode()==1){
                    select = 3;
                }else {
                    select = 4;
                }
                break;
            case 2:
                if (index == 1) {
                    showWeekList();
                    return;
                } else if (index == 2) {// daily
                    getScheItem().setRepeatMode(0);
                } else {
                    getScheItem().setRepeatMode(128);// once
                }
                if(getScheItem().getRecordMode()==1){
                    select = 4;
                }else {
                    select = 5;
                }
                break;
            case 3:
                getScheItem().setType(index);
                if (index == 1) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EventId==" + getEventId());
                    getScheItem().setEventId(getEventId());
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"new time->"+getScheItem().getRecordStartTime()+","+
                            getScheItem().getRecordDuration()+
                            " olc time->"+mInitStartTime+","+
                            mInitDurationTime);
                    if(getScheItem().getRecordStartTime()!=mInitStartTime
                    ||getScheItem().getRecordDuration()!= mInitDurationTime){
                        getScheItem().setRecordStartTime(mInitStartTime);
                        getScheItem().setRecordDuration(mInitDurationTime);
                        initDataTime();
                    }
                } else {
                    getScheItem().setEventId(0);
                }
                select = 6;
                break;
            default:
                break;
        }

        initData();
        channel_repeat_list.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        listView.setSelection(select);
        editLayout.setVisibility(View.VISIBLE);
    }
    public ScheduleListItemDialog(Context context, MtkTvBookingBase item) {
        super(context, R.layout.pvr_timeshift_schedulelist_item);
        mInitStartTime = item.getRecordStartTime();
        mInitDurationTime = item.getRecordDuration();
        setScheItem(item);
        //initData();
        // initDataTime();
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"2222new time->"+getScheItem().getRecordStartTime()+","+
                getScheItem().getRecordDuration()+
                " olc time->"+mInitStartTime+","+
                mInitDurationTime);
    }

    protected ScheduleListItemDialog(Context context, int layoutID) {
        super(context, layoutID);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void initView() {
        // TODO Auto-generated method stub
        super.initView();
        synchronized (ScheduleListItemDialog.class) {
            scheduleListItemDialog = this;
        }
        listView = (ListView) findViewById(R.id.schedulelist_list);
        channel_repeat_list = (ListView) findViewById(R.id.channel_repeat_list);
        weekListView = (ListView) findViewById(R.id.channel_week_list);
        dateTimeView = (DateTimeView) findViewById(R.id.common_datetimeview);
        titleTextView = (TextView) findViewById(R.id.schedule_add_title);
        editLayout = (LinearLayout)findViewById(R.id.red_edit_layout);
        titleTextView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        RxBus.instance.onEvent(ActivityDestroyEvent.class)
            .filter(it -> it.activityClass == mContext.getClass())
            .firstElement()
            .doOnSuccess(it -> {
                Log.i(TAG, "rxbus free scheduleListItemDialog");
                synchronized (ScheduleListItemDialog.class) {
                    scheduleListItemDialog = null;
                }
            }).subscribe();
    }

    /**
     * init time
     */
    private void initDataTime() {
        startDate = mScheItem.getRecordStartTime();
        if (startDate == 0) {
            startDate = System.currentTimeMillis() / 1000;
        }
        startDateString = Util.timeToDateStringEx(startDate * 1000, 0);
        startTimeString = Util.timeToTimeStringEx(startDate * 1000, 0);
        long duration = mScheItem.getRecordDuration();
        long endtime = startDate + duration;
        endTimeString = Util.timeToTimeStringEx(endtime * 1000, 0);
        setItemDate();
    }

    @SuppressLint("NewApi")
    private void initData() {
      if(!itemModels.isEmpty()){
        itemModels.clear();
       }
//        ScheduleItemModel sItemModel = new ScheduleItemModel();
//        sItemModel.setTitle(mContext.getString(R.string.schedule_pvr_srctype));
//        sItemModel.setContent(mContext.getResources().getStringArray(
//                R.array.pvr_tshift_srctype)[0]);
//        getScheItem().setSourceType(0);
//        itemModels.add(sItemModel);

        ScheduleItemModel sItemModel1 = new ScheduleItemModel();
        sItemModel1.setTitle(mContext
                .getString(R.string.schedule_pvr_channel_num));
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"channelname== "+channelname());
        sItemModel1.setContent(channelname());
        itemModels.add(sItemModel1);

        ScheduleItemModel sItemModel2 = new ScheduleItemModel();
        sItemModel2.setTitle(mContext
                .getString(R.string.schedule_pvr_start_date));

        sItemModel2.setContent(startDateString);

        ScheduleItemModel sItemModel3 = new ScheduleItemModel();
        sItemModel3.setTitle(mContext
                .getString(R.string.schedule_pvr_start_time));

        sItemModel3.setContent(startTimeString);

        ScheduleItemModel sItemModel4 = new ScheduleItemModel();
        sItemModel4.setContent(endTimeString);
        sItemModel4.setTitle(mContext
                .getString(R.string.schedule_pvr_stop_time));
        sItemModel4.setEnabled(getScheItem().getType() == 1);
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"gettype->"+getScheItem().getType());
        if(getScheItem().getType()!=0&&getScheItem().getRecordMode()!=1){
            sItemModel2.setEnabled(true);
            sItemModel3.setEnabled(true);
        }else{
            sItemModel2.setEnabled(false);
            sItemModel3.setEnabled(false);
        }
        itemModels.add(sItemModel2);
        itemModels.add(sItemModel3);
        if(getScheItem().getRecordMode()!=1) {
            itemModels.add(sItemModel4);
        }
        String[] reminderArray = mContext.getResources().getStringArray(
                R.array.pvr_tshift_schedule_type);

        String[] scheduelWatchArray = new String[] { reminderArray[1] };

        ScheduleItemModel sItemModel5 = new ScheduleItemModel();
        sItemModel5.setTitle(mContext
                .getString(R.string.schedule_pvr_reminder_type));
        sItemModel5.setContent(reminderArray[mScheItem.getRecordMode() == 2 ? 0
                : mScheItem.getRecordMode()]);
        getScheItem().setRecordMode(
                mScheItem.getRecordMode() == 0 ? 2 : mScheItem.getRecordMode());
        itemModels.add(sItemModel5);

        ScheduleItemModel sItemModel6 = new ScheduleItemModel();
        sItemModel6.setTitle(mContext
                .getString(R.string.schedule_pvr_repeat_type));
        sItemModel6.setContent(getWeek());
        itemModels.add(sItemModel6);

        // event or normal
        ScheduleItemModel sItemModel7 = new ScheduleItemModel();
        sItemModel7.setTitle(mContext
                .getString(R.string.dvr_schedule_event_type_name));
        sItemModel7.setContent(getEventName());
        if(getScheItem().getRecordMode()!=1) {
            itemModels.add(sItemModel7);
        }

        if(adapter==null) {
            adapter = new NewScheduleAddAdapter(mContext, itemModels);
            listView.setAdapter(adapter);
        }else {
            adapter.setItem(itemModels);
            adapter.notifyDataSetChanged();
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "size==" + itemModels.size());
        listView.setSelected(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setSelector(R.color.nav_button_select);
        listView.requestFocus();
        listView.setSelection(position);
        initOnClick();
    }

    private void initOnClick() {
        listView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int keyCode, KeyEvent event) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onkeylistener=" + keyCode+","+listView.getSelectedItemPosition());
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                    switch (keyCode) {
                    case KeyMap.KEYCODE_DPAD_RIGHT:
                    case KeyMap.KEYCODE_DPAD_CENTER:
                    case KeyMap.KEYCODE_MTKIR_RED:
                        position = listView.getSelectedItemPosition();
                        switchType(position);
                        return true;
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
                        break;
                    default:
                        break;
                    }
                }
                // TODO Auto-generated method stub
                return false;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onkeydown==" + keyCode);
        switch (keyCode) {
        case KeyMap.KEYCODE_MTKIR_RED:
            if (dateTimeView.getVisibility() == View.VISIBLE){
                dateTimeView.onKeyLeft();
            }
            break;
            case KeyMap.KEYCODE_MTKIR_YELLOW:
                if (dateTimeView.getVisibility() == View.VISIBLE) {
                    dateTimeView.onKeyRight();
                }
                break;
        case KeyMap.KEYCODE_MTKIR_GREEN:
            if (weekListView.getVisibility() == View.VISIBLE) {
                weekListView.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                editLayout.setVisibility(View.VISIBLE);
                saveWeekDay();
                initDataTime();
                initData();
                return true;
            }
            if (dateTimeView.getVisibility() == View.VISIBLE) {
                refreshDate();
                return true;
            }
            if ((position == 5 || position == 6 || position == 7)
                    && listView.getVisibility() == View.GONE) {
                InstrumentationHandler.getInstance().sendKeyDownUpSync(
                        KeyEvent.KEYCODE_DPAD_CENTER);
                return true;
            }
            setItemDate();
            addScheduleList();
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
            if(dateTimeView.getVisibility() == View.VISIBLE){
            handleKeyboardInput(dateTimeView, keyCode);
                }
            break;
        case KeyMap.KEYCODE_DPAD_RIGHT:
            if (dateTimeView.getVisibility() == View.VISIBLE){
                dateTimeView.onKeyRight();
                }
            break;
        case KeyMap.KEYCODE_DPAD_LEFT:
            if (dateTimeView.getVisibility() == View.VISIBLE) {
                dateTimeView.onKeyLeft();
            }
            break;
        case KeyMap.KEYCODE_MTKIR_BLUE:
        case KeyMap.KEYCODE_BACK:
            editLayout.setVisibility(View.VISIBLE);
            if (dateTimeView.getVisibility() == View.VISIBLE) {
                dateTimeView.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                return true;
            }
            if (channel_repeat_list.getVisibility() == View.VISIBLE) {
                listView.setVisibility(View.VISIBLE);
                channel_repeat_list.setVisibility(View.GONE);
                return true;
            }
            if(StateScheduleList.getInstance().queryItem()!=null&&!StateScheduleList.getInstance().queryItem().isEmpty()){
                ScheduleListDialog scheduleListDialog = new ScheduleListDialog(
                        mContext, 0);
                scheduleListDialog.setEpgFlag(false);
                scheduleListDialog.show();
            }
            dismiss();
            break;
        default:
            break;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * add schedule
     */
    private void addScheduleList() {
        int code = checkItem(getScheItem());
        if (code != 0) {
            Toast.makeText(mContext,
                    mContext.getString(R.string.pvr_schedule_time_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if(getScheItem().getRecordMode()==1){
            getScheItem().setEventId(0);
            getScheItem().setRecordDuration(1);
        }
        getScheItem().setSvlId(CommonIntegration.getInstance().getSvl());
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG,"svlId==="+getScheItem().getSvlId());
        final List<MtkTvBookingBase> items = getReplaceItems(getScheItem());
        if (!items.isEmpty() || TYPE == 1) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "items===" + items.size());
            SimpleDialog simpleDialog = (SimpleDialog) ComponentsManager
                                            .getInstance().getComponentById(NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
            simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
            simpleDialog.setConfirmText(R.string.pvr_confirm_yes);
            simpleDialog.setCancelText(R.string.pvr_confirm_no);
            simpleDialog.setOnConfirmClickListener(
                    new OnConfirmClickListener() {

                        @Override
                        public void onConfirmClick(int dialogId) {
                            // TODO Auto-generated method stub
                             if(TYPE==1&&items.isEmpty()){
                             StateScheduleList.getInstance().replaceItem(getScheItem());
                             }else{
                            for (MtkTvBookingBase item : items) {
                                int bookingid = item.getBookingId();
                                StateScheduleList.getInstance()
                                        .deleteItem(item);
                                for (MtkTvBookingBase item1 : items) {
                                    int bookingid1 = item1.getBookingId();
                                    if (bookingid < bookingid1) {
                                        item1.setBookingId(bookingid1 - 1);
                                    }
                                }
                            }
                            StateScheduleList.getInstance().insertItem(
                                    getScheItem());
                                }
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                    "showAddConfirmDialog + currenItem  = "
                                            + getScheItem().toString());
                            ScheduleListDialog scheduleListDialog = new ScheduleListDialog(
                                    mContext, 0);
                            scheduleListDialog.setEpgFlag(false);
                            scheduleListDialog.show();
                            dismiss();
                        }
                    }, 2333);
            simpleDialog
                    .setOnCancelClickListener(new RegistOnDvrDialog(), 2333);
            simpleDialog.setContent(mContext.getResources().getString(
                    R.string.pvr_schedulelist_replace_line1)
                    + " "
                    + mContext.getResources().getString(
                            R.string.pvr_schedulelist_replace_line2));
            simpleDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);//important
            simpleDialog.show();
        } else if (StateScheduleList.getInstance() != null
                && StateScheduleList.getInstance().queryItem().size() >=  MtkTvRecordBase.MAX_REMINDER_SLOT_NUM) {
            Toast.makeText(mContext,
                    mContext.getString(R.string.pvr_schedule_max_schedule_error),
                    Toast.LENGTH_SHORT).show();
        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "items==" + getScheItem().toString());
           StateScheduleList.getInstance()
                    .insertItem(getScheItem());
            this.dismiss();
            ScheduleListDialog scheduleListDialog = new ScheduleListDialog(
                    mContext, 0);
            scheduleListDialog.setEpgFlag(false);
            scheduleListDialog.show();
        }
    }

    private void refreshDate() {

        switch (dateType) {
        case 2:
            startDateString = dateTimeView.getmDate();
            setItemDate();
            break;
        case 3:
            startTimeString = dateTimeView.getmDate();
            setItemDate();
            break;
        case 4:
            endTimeString = dateTimeView.getmDate();

            setDuration();
            break;
        default:
            break;
        }
        dateTimeView.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        editLayout.setVisibility(View.VISIBLE);
        // initDataTime();
        initData();
    }

    private void setDuration() {
        // TODO Auto-generated method stub
        long startRecordEndTime = 0;
        if (0 == VendorProperties.mtk_system_timesync_existed().orElse(0)) {
            startRecordEndTime = Util.strToTimeEx(startDateString + " "
                    + endTimeString, 0);

            startRecordEndTime = getUTCtime(startRecordEndTime);

        } else {

            startRecordEndTime = Util.strToTimeEx(startDateString + " "
                    + endTimeString, 0);
        }

        long duration = startRecordEndTime - startRecordTime;
        if (duration <= 0) {
            duration += 24 * 60 * 60;
        }

        com.mediatek.wwtv.tvcenter.util.MtkLog.d("{ScheduleListTimeInfo}", "setRecordDuration" + duration);
        getScheItem().setRecordDuration(duration);
    }

    private long getUTCtime(long startRecordEndTime) {
        MtkTvTimeFormatBase timeBaseFrom = new MtkTvTimeFormatBase();
        timeBaseFrom.setByUtc(startRecordEndTime);
        MtkTvTimeFormatBase timeBaseTo = new MtkTvTimeFormatBase();
        timeBaseFrom.print("Jiayang.li --brdcst-local");
        MtkTvTimeBase time = new MtkTvTimeBase();
        time.convertTime(time.MTK_TV_TIME_CVT_TYPE_BRDCST_LOCAL_TO_SYS_UTC,
                timeBaseFrom, timeBaseTo);
        timeBaseTo.print("Jiayang.li--sys-utc");
        return timeBaseTo.toSeconds();
    }
    private long getBroadCastUTCtime(long startRecordEndTime) {
        MtkTvTimeFormatBase timeBaseFrom = new MtkTvTimeFormatBase();
        timeBaseFrom.setByUtc(startRecordEndTime);
        MtkTvTimeFormatBase timeBaseTo = new MtkTvTimeFormatBase();
        timeBaseFrom.print("Jiayang.li --brdcst-local");
        MtkTvTimeBase time = new MtkTvTimeBase();
        time.convertTime(time.MTK_TV_TIME_CVT_TYPE_BRDCST_LOCAL_TO_BRDCST_UTC,
                timeBaseFrom, timeBaseTo);
        timeBaseTo.print("Jiayang.li--sys-utc");
        return timeBaseTo.toSeconds();
    }
    private void setItemDate() {
        if (0 == VendorProperties.mtk_system_timesync_existed().orElse(0)) {
            startRecordTime = Util.strToTimeEx(startDateString + " "
                    + startTimeString, 0);

            // com.mediatek.wwtv.tvcenter.util.MtkLog.d("Jiayang.li", "timeFrom_startTIme:" +
            // timeBaseFrom.toUtc() );
            if(getScheItem().getType()==1) {
                getScheItem().setSourceType((int) getBroadCastUTCtime(startRecordTime));
                com.mediatek.wwtv.tvcenter.util.MtkLog.w(TAG,"brdcst == "+getScheItem().getSourceType());
            }
            startRecordTime = getUTCtime(startRecordTime);
        } else {
            startRecordTime = Util.strToTimeEx(startDateString + " "
                    + startTimeString, 0);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"startRecordTime->"+startRecordTime);
        getScheItem().setRecordStartTime(startRecordTime);
        setDuration();
        // initData();
    }

    /**
     * onclick witch positon to show
     *
     * @param position
     */
    private void switchType(int position) {
        if(position!=0){
        editLayout.setVisibility(View.GONE);
        }
        int relPosition=0;
        String title = itemModels.get(position).titleString;
        if(title.equals(mContext.getString(R.string.schedule_pvr_channel_num))){
            relPosition = CHANNELINFO;
        }
        if(title.equals(mContext.getString(R.string.schedule_pvr_start_date))){
            relPosition = STARTDATE;
        }
        if(title.equals(mContext.getString(R.string.schedule_pvr_start_time))){
            relPosition = STARTTIME;
        }
        if(title.equals(mContext.getString(R.string.schedule_pvr_stop_time))){
            relPosition = STOPTIME;
        }
        if(title.equals(mContext.getString(R.string.schedule_pvr_reminder_type))){
            relPosition = SCHEDULETYPE;
        }
        if(title.equals(mContext.getString(R.string.schedule_pvr_repeat_type))){
            relPosition = REPEATTYPE;
        }
        if(title.equals(mContext.getString(R.string.dvr_schedule_event_type_name))){
            relPosition = EVENTMODE;
        }
        switch (relPosition) {
        case CHANNELINFO:
            if(getType()==1){
                if(CommonIntegration.getInstance().getTunerMode() != getScheItem()
                        .getTunerType()){
                    Toast.makeText(mContext,mContext.getString(R.string.channel_name_can_not_edit),Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            showChannelList();
            break;
        case STARTDATE:
            dateType = 2;
            dateTypeString = DateTimeInputView.TIME_DATE;
            showDateTime(DateTimeView.DATETYPE, startDateString);
            break;
        case STARTTIME:
            dateType = 3;
            dateTypeString = DateTimeInputView.TIME_TIME;
            showDateTime(DateTimeView.TIMETYPE, startTimeString);
            break;
        case STOPTIME:
            dateType = 4;
            dateTypeString = DateTimeInputView.TIME_TIME;
            showDateTime(DateTimeView.TIMETYPE, endTimeString);
            break;
        case SCHEDULETYPE:
            showScheduleWay();
            break;
        case REPEATTYPE:
            showRepeatType();
            break;
        case EVENTMODE:
            showEventList();
            break;
        default:
            break;
        }
    }

    /**
     * show channel list
     */
    private void showChannelList() {
        List<SubScheduleItemModel> scheduleItemModels = new ArrayList<SubScheduleItemModel>();
        for (String i : channelArray) {
            SubScheduleItemModel scheduleItemModel = new SubScheduleItemModel();
            scheduleItemModel.setChecked(false);
            scheduleItemModel.setVisible(false);
            scheduleItemModel.setItemString(i);
            scheduleItemModels.add(scheduleItemModel);
        }
        SubScheduleAdapter scheduleAdapter = new SubScheduleAdapter(mContext,
                scheduleItemModels);
        channel_repeat_list.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        scheduleAdapter.setChannelFlag(true);
        channel_repeat_list.setAdapter(scheduleAdapter);
        channel_repeat_list.setSelected(true);
        channel_repeat_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        channel_repeat_list.setSelector(R.color.nav_button_select);
        channel_repeat_list.requestFocus();
        channel_repeat_list.setSelection(channelIndex);


        channel_repeat_list
                .setOnKeyListener(new View.OnKeyListener() {

                    @Override
                    public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                        // TODO Auto-generated method stub
                        channelIndex = channel_repeat_list
                                .getSelectedItemPosition();
                        if (arg2.getAction() == KeyEvent.ACTION_DOWN){
                            switch (arg1) {
                            case KeyMap.KEYCODE_DPAD_CENTER:
                               setResult(channelIndex,0);
                                return true;
                            case KeyMap.KEYCODE_BACK:
                                if (channel_repeat_list.getVisibility() == View.VISIBLE) {
                                    channel_repeat_list
                                            .setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);
                                    editLayout.setVisibility(View.VISIBLE);
                                    return true;
                                }
                                break;
                            default:
                                break;
                            }
                        }
                        return false;
                    }
                });
    }

    /**
     * set channel id to tvapi
     *
     * @param channelIndex
     */
    private void setChanneid(int channelIndex) {
        String channlString = TIFChannelManager.getInstance(mContext)
                .getAllDTVTIFChannels().get(channelIndex).mDisplayNumber;
        // int channelNum =
        // Integer.valueOf(channlString.split("-")[0]);
        MtkTvChannelInfoBase mChannelInfo = TIFChannelManager
                .getInstance(mContext).getAllDTVTIFChannels().get(channelIndex).mMtkTvChannelInfo;
        int channelID = mChannelInfo.getChannelId();
        getScheItem().setEventTitle("" + channlString);
        getScheItem().setChannelId(channelID);
    }

    /**
     * show date ui
     *
     * @param type
     *            date or time
     * @param date
     *            time for string
     */
    private void showDateTime(int type, String date) {
        dateTimeView.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        this.dateTimeView.setDateStr(date);
        this.dateTimeView.setCurrentSelectedPosition(-1);
        this.dateTimeView.postInvalidate();
        dateTimeView.mType = type;
    }

    /**
     * show schedule mode, reminder or record
     */
    private void showScheduleWay() {
        String[] reminderArray = mContext.getResources().getStringArray(
                R.array.pvr_tshift_schedule_type);
      if(!scheduleItemModelsmode.isEmpty()){
        scheduleItemModelsmode.clear();
       }
        for (String i : reminderArray) {
            SubScheduleItemModel scheduleItemModel = new SubScheduleItemModel();
            scheduleItemModel.setVisible(false);
            scheduleItemModel.setItemString(i);
            scheduleItemModelsmode.add(scheduleItemModel);
        }
        SubScheduleAdapter scheduleAdapter = new SubScheduleAdapter(mContext,
                scheduleItemModelsmode);
        channel_repeat_list.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        channel_repeat_list.setAdapter(scheduleAdapter);
        channel_repeat_list.setSelected(true);
        channel_repeat_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        channel_repeat_list.setSelector(R.color.nav_button_select);
        channel_repeat_list.requestFocus();
        channel_repeat_list.setSelection(getScheItem().getRecordMode()==2?0:1);
        channel_repeat_list
                .setOnKeyListener(new View.OnKeyListener() {

                    @Override
                    public boolean onKey(View arg0, int arg1, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (event.getAction() == KeyEvent.ACTION_DOWN){
                            switch (arg1) {
                            case KeyMap.KEYCODE_DPAD_CENTER:
                                setResult(channel_repeat_list.getSelectedItemPosition(),1);
                                return true;
                            case KeyMap.KEYCODE_BACK:
                                if (channel_repeat_list.getVisibility() == View.VISIBLE) {
                                    channel_repeat_list
                                            .setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);
                                    editLayout.setVisibility(View.VISIBLE);
                                    return true;
                                }
                                break;
                            default:
                                break;
                            }
                        }
                        return false;
                    }
                });
    }

    /**
     * show repeat type :once . daily .week
     */
    private void showRepeatType() {
        String[] repeatArray = mContext.getResources().getStringArray(
                R.array.pvr_tshift_repeat_type);
        if(!scheduleItemModelsrepeat.isEmpty()) {
            scheduleItemModelsrepeat.clear();
        }
        for (String i :repeatArray) {
            SubScheduleItemModel scheduleItemModel = new SubScheduleItemModel();
            scheduleItemModel.setVisible(false);
            scheduleItemModel.setItemString(i);
            scheduleItemModelsrepeat.add(scheduleItemModel);
        }
        SubScheduleAdapter scheduleAdapter = new SubScheduleAdapter(mContext,
                scheduleItemModelsrepeat);
        channel_repeat_list.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        channel_repeat_list.setAdapter(scheduleAdapter);
        channel_repeat_list.setSelected(true);
        channel_repeat_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        channel_repeat_list.setSelector(R.color.nav_button_select);
        channel_repeat_list.requestFocus();
        int setIndex  = 0;
        if(getScheItem().getRepeatMode()==128){
            setIndex = 0;
        }else
        if(getScheItem().getRepeatMode()==0){
            setIndex = 2;
        }else {
            setIndex = 1;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG,"setIndex->"+setIndex);
        channel_repeat_list.setSelection(setIndex);
        channel_repeat_list
                .setOnKeyListener(new View.OnKeyListener() {

                    @Override
                    public boolean onKey(View arg0, int arg1, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (event.getAction() == KeyEvent.ACTION_DOWN){
                            switch (arg1) {
                            case KeyMap.KEYCODE_DPAD_CENTER:
                               setResult(channel_repeat_list.getSelectedItemPosition(),2);
                                return true;
                            case KeyMap.KEYCODE_BACK:
                                if (channel_repeat_list.getVisibility() == View.VISIBLE) {
                                    channel_repeat_list
                                            .setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);
                                    editLayout.setVisibility(View.VISIBLE);
                                    return true;
                                }
                                break;

                            default:
                                break;
                            }
                        }
                        return false;
                    }
                });
    }

    /**
     * show weeklist
     */
    public void showWeekList() {
        int repeat = getScheItem().getRepeatMode();
        String[] array = mContext.getResources().getStringArray(
                R.array.week_day);
        if(!scheduleItemModelweek.isEmpty()) {
            scheduleItemModelweek.clear();
        }
        if (repeat == 0 || repeat == 128) {
            int weekday = MtkTvTime.getInstance().getLocalTime().weekDay;
            for (int i = 0; i < 7; i++) {
                SubScheduleItemModel scheduleItemModelss = new SubScheduleItemModel();
                if (i == weekday) {
                    scheduleItemModelss.setChecked(true);
                } else {
                    scheduleItemModelss.setChecked(false);
                }
                scheduleItemModelss.setItemString(array[i]);
                scheduleItemModelss.setVisible(true);
                scheduleItemModelweek.add(scheduleItemModelss);
            }
        } else {
            for (int i = 0; i < 7; i++) {
                SubScheduleItemModel scheduleItemModel = new SubScheduleItemModel();
                if ((repeat & (1 << i)) != 0) {
                    scheduleItemModel.setChecked(true);
                } else {
                    scheduleItemModel.setChecked(false);
                }
                scheduleItemModel.setItemString(array[i]);
                scheduleItemModel.setVisible(true);
                scheduleItemModelweek.add(scheduleItemModel);
            }
        }
        final SubScheduleAdapter scheduleAdapter = new SubScheduleAdapter(
                mContext, scheduleItemModelweek);
        weekListView.setVisibility(View.VISIBLE);
        channel_repeat_list.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        weekListView.setAdapter(scheduleAdapter);
        weekListView.setSelected(true);
        weekListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        weekListView.setSelector(R.color.nav_button_select);
        weekListView.requestFocus();
        weekListView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                // TODO Auto-generated method stub
                switch (arg1) {
                case KeyMap.KEYCODE_DPAD_CENTER:
                    break;
                case KeyMap.KEYCODE_BACK:
                case KeyMap.KEYCODE_MTKIR_BLUE:
                    if (weekListView.getVisibility() == View.VISIBLE) {
                        weekListView.setVisibility(View.GONE);
                        channel_repeat_list.setVisibility(View.VISIBLE);
                        return true;
                    }
                    break;

                default:
                    break;
                }
                return false;
            }
        });
        weekListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                    int position, long arg3) {
                // TODO Auto-generated method stub
                int index = weekListView.getSelectedItemPosition();
                CheckBox checkBox = (CheckBox) weekListView.getSelectedView()
                        .findViewById(R.id.week_type);
                SubScheduleItemModel s = ((SubScheduleItemModel) weekListView
                        .getAdapter().getItem(index));
                if (!s.isChecked()) {
                    scheduleItemModelweek.get(position).setChecked(true);
                    checkBox.setChecked(true);
                    ((SubScheduleItemModel) weekListView.getAdapter().getItem(
                            index)).setChecked(true);
                } else {
                    checkBox.setChecked(false);
                    scheduleItemModelweek.get(position).setChecked(false);
                    ((SubScheduleItemModel) weekListView.getAdapter().getItem(
                            index)).setChecked(false);
                }
                scheduleAdapter.updateList(index, weekListView);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onitemclick==" + position);
                scheduleAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     *
     * @return get channel name
     */
    private String channelname() {
        if(getType()==1) {
            TIFChannelInfo tifChannelInfo = TIFChannelManager.getInstance(mContext)
                    .getChannelInfoByChannelIdAndSvlId(getScheItem().getChannelId(), getScheItem().getSvlId());
            if(CommonIntegration.getInstance().getTunerMode() != getScheItem()
                    .getTunerType()){
                String number="" ;
                String name ="";
                if(tifChannelInfo == null){
                    number = "0";
                    name = "0";
                }else{
                   number = tifChannelInfo.mDisplayNumber;
                   name = tifChannelInfo.mDisplayName;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG," tifChannelInfo.mDisplayNumber== "+ number+",tifChannelInfo.mDisplayName== "+ name);
                return  String.format("CH%s:%3s%s", number, "",
                        name);
            }
        }
        List<TIFChannelInfo> list = TIFChannelManager.getInstance(mContext)
                .getAllDTVTIFChannels();
        channelArray = new String[list.size()];
        List<Integer> chList = new ArrayList<Integer>();
        for (int i = 0; i < list.size(); i++) {
            String channelName = list.get(i).mDisplayName;
            String channelNumber = "" + list.get(i).mDisplayNumber;
            if (channelName == null) {
                // channelArray[i]=+"";
                channelName = channelNumber;
            }

            if (channelName.equalsIgnoreCase(channelNumber)) {
                channelNumber = ""; // avoid the situation like this: "14  14".
            }

            channelArray[i] = String.format("CH%s:%3s%s", channelNumber, "",
                    channelName);
            chList.add(list.get(i).mMtkTvChannelInfo.getChannelId());

        }

        //int channelIndex = 0;
        // fix DTV00610793
        if (!chList.isEmpty() ) {
            for (int i = 0; i < chList.size(); i++) {
                if (chList.get(i) == getScheItem().getChannelId()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"selet position->"+getScheItem().getChannelId()+","+i);
                    channelIndex = i;
                    setChanneid(channelIndex);
                }
            }
        }
        if (getScheItem().getChannelId() == 0) {
            for (int i = 0; i < chList.size(); i++) {
                if (chList.get(i) == EditChannel.getInstance(mContext)
                        .getCurrentChannelId()) {
                    channelIndex = i;
                    setChanneid(channelIndex);
                }
            }
        }
        return channelArray[channelIndex];
    }

    private String getWeek() {
        int repeat = getScheItem().getRepeatMode();
        if (repeat == 128) {
            repeat = 0;
        } else if (repeat == 0) {
            repeat = 2;
        } else {
            repeat = 1;
        }
        String[] repeatArray = mContext.getResources().getStringArray(
                R.array.pvr_tshift_repeat_type);

        return repeatArray[repeat];
        // String[] array
        // =mContext.getResources().getStringArray(R.array.week_day);
        // StringBuilder stringBuilder = new StringBuilder();
        // for(int i=0;i<7;i++){
        // if((repeat & (1 << i))!=0){
        // stringBuilder.append(array[i]+" ");
        // }
        //
        // }
        // return stringBuilder.toString();
    }

    private String getEventName() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getEventType()=="+getEventType());
        if (getScheItem().getType() == 0) {
            return mContext.getString(R.string.dvr_schedule_event_type_normal);
        } else {
            return mContext.getString(R.string.dvr_schedule_event_type_track);
        }
    }

    private void showEventList() {
        String[] eventarray = mContext.getResources().getStringArray(
                R.array.pvr_schedule_event_array);
        if(!scheduleItemModelevent.isEmpty()) {
            scheduleItemModelevent.clear();
        }
        if (getEventType() == 1) {
            for (String i:eventarray) {
                SubScheduleItemModel scheduleItemModel = new SubScheduleItemModel();
                scheduleItemModel.setVisible(false);
                scheduleItemModel.setItemString(i);
                scheduleItemModelevent.add(scheduleItemModel);
            }
        } else {
            SubScheduleItemModel scheduleItemModel1 = new SubScheduleItemModel();
            scheduleItemModel1.setVisible(false);
            scheduleItemModel1.setItemString(eventarray[0]);
            scheduleItemModelevent.add(scheduleItemModel1);
        }

        SubScheduleAdapter scheduleAdapter = new SubScheduleAdapter(mContext,
                scheduleItemModelevent);
        channel_repeat_list.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        channel_repeat_list.setAdapter(scheduleAdapter);
        channel_repeat_list.setSelected(true);
        channel_repeat_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        channel_repeat_list.setSelector(R.color.nav_button_select);
        channel_repeat_list.requestFocus();
        channel_repeat_list.setSelection(getScheItem().getEventId()==0?0:1);
        channel_repeat_list
                .setOnKeyListener(new View.OnKeyListener() {

                    @Override
                    public boolean onKey(View arg0, int arg1, KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (event.getAction() == KeyEvent.ACTION_DOWN){
                            switch (arg1) {
                            case KeyMap.KEYCODE_DPAD_CENTER:
                               setResult(channel_repeat_list.getSelectedItemPosition(),3);
                                return true;
                            case KeyMap.KEYCODE_BACK:
                                if (channel_repeat_list.getVisibility() == View.VISIBLE) {
                                    channel_repeat_list
                                            .setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);
                                    editLayout.setVisibility(View.VISIBLE);
                                    return true;
                                }
                                break;

                            default:
                                break;
                            }
                        }
                        return false;
                    }
                });

    }

    /**
     * @param mScheItem
     *            the mScheItem to set
     */
    public void setScheItem(MtkTvBookingBase mScheItem) {
        this.mScheItem = mScheItem;

        initDataTime();
    }

    public MtkTvBookingBase getScheItem() {

        return mScheItem;
    }

    public void show() {
        if (!(DataSeparaterUtil.getInstance() != null && DataSeparaterUtil
                .getInstance().isSupportPvr())) {
            return;
        }
        initData();
        super.show();
        setWindowPosition();
       if(getType()==0){
        titleTextView.setText(String.format("%s - %s", mContext
                .getString(R.string.menu_setup_schedule_list), mContext.getString(R.string.pvr_schedule_add)));
            }else{
        titleTextView.setText(String.format("%s - %s", mContext
                .getString(R.string.menu_setup_schedule_list), mContext.getString(R.string.pvr_schedule_edit)));

            }
        listView.setAccessibilityDelegate(mDelegate);
        channel_repeat_list.setAccessibilityDelegate(mDelegate);
        weekListView.setAccessibilityDelegate(mDelegate);
        dateTimeView.setAccessibilityDelegate(mDelegate);
        listView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        channel_repeat_list.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        weekListView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        dateTimeView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
    }

    // init dialog position
    public void setWindowPosition() {
        WindowManager m = getWindow().getWindowManager();
        Display display = m.getDefaultDisplay();
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        TypedValue sca = new TypedValue();
        mContext.getResources().getValue(R.dimen.nav_channellist_marginY, sca,
                true);
        mContext.getResources().getValue(R.dimen.nav_channellist_marginX, sca,
                true);
        mContext.getResources().getValue(R.dimen.nav_channellist_size_width,
                sca, true);
        float chwidth = sca.getFloat();
        mContext.getResources().getValue(R.dimen.nav_channellist_size_height,
                sca, true);
        mContext.getResources().getValue(R.dimen.nav_channellist_page_max, sca,
                true);
        int menuWidth = (int) (display.getWidth() * chwidth);
        lp.width = menuWidth;
        lp.height = display.getHeight();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setWindowPosition menuWidth " + menuWidth + " x "
                + " display.getWidth() " + display.getWidth());
        if(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())== LayoutDirection.RTL){
            lp.gravity = Gravity.START;
        }else {
            lp.gravity = Gravity.END;
        }
        window.setAttributes(lp);
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

        if (view instanceof DateTimeView) {
            DateTimeView dateTimeView = (DateTimeView) view;
            dateTimeView.input(ch, null);
            return true;
        }
        return false;
    }

    private void saveWeekDay() {
        StringBuilder weekValue = new StringBuilder();
        int weekdey = dayForWeek(startDateString);
        int diff = 0;
        int diff2 = 7;
        for (int i = 6; i >= 0; i--) {
            boolean selected = ((SubScheduleItemModel) weekListView
                    .getAdapter().getItem(i)).isChecked();
            if (selected) {
                if ((i + 1) - weekdey > 0) {
                    diff = (i + 1) - weekdey;
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
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "diff" + startRecordTime + "   diff2==" + diff2);
        startRecordTime = Util.strToTimeEx(startDateString + " "
                + startTimeString, 0)
                + getday(Integer.parseInt(weekValue.toString(), 2)) * 24 * 60
                * 60;
        getScheItem().setRecordStartTime(startRecordTime);
        getScheItem().setRepeatMode(Integer.parseInt(weekValue.toString(), 2));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, startRecordTime+" 2 to 10 ==" + Integer.parseInt(weekValue.toString(), 2));
    }

    private int getday(int repeatcount) {
        int weekday = MtkTvTime.getInstance().getLocalTime().weekDay;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "weekday==" + weekday);
        int count = -6;
        for (int i = 6; i >= 0; i--) {
            if ((repeatcount & (1 << i)) == (1 << i)) {
                if (i >= weekday) {
                    count = i - weekday;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "count1==" + count);
                } else {
                    if (count < 0) {
                        count = i - weekday;
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "count2==" + count);
                    }
                }

            }
        }
        if (count < 0) {
            count = count + 7;
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "count==" + count);
        return count;
    }

    /**
     * @param item
     * @return error code.like: out of date,...
     */
    private int checkItem(MtkTvBookingBase item) {
        // Error 1,2,3,4,5,6,7...
        final int timeError = 1;

        MtkTvTimeFormatBase mTime = MtkTvTime.getInstance()
                .getBroadcastLocalTime();
        MtkTvTimeFormatBase timeBaseTo = new MtkTvTimeFormatBase();
        MtkTvTimeBase time = new MtkTvTimeBase();
        time.convertTime(time.MTK_TV_TIME_CVT_TYPE_BRDCST_LOCAL_TO_SYS_UTC,
                mTime, timeBaseTo);
        timeBaseTo.print("Jiayang.li--sys-utc");
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getBroadcastLocalTime == " + timeBaseTo.toSeconds());
        Long startTime = item.getRecordStartTime();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime = " + startTime + "  currentTime = " + mTime.toSeconds()
                + ", systime=" + System.currentTimeMillis());
        if (startTime <= (timeBaseTo.toSeconds())) {
            return timeError;
        }

        return 0;
    }

    public int dayForWeek(String pTime) {
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

    private List<MtkTvBookingBase> getReplaceItems(MtkTvBookingBase item) {
        Long startTime = item.getRecordStartTime();
        Long endTime = startTime + item.getRecordDuration();
        List<MtkTvBookingBase> replacetItems = new ArrayList<MtkTvBookingBase>();
        List<MtkTvBookingBase> items = null;
        items = StateScheduleList.getInstance().queryItem();
        if (!items.isEmpty()) {
            for (MtkTvBookingBase sItem : items) {
                Long sstartTime = sItem.getRecordStartTime();
                Long sendTime = sstartTime + sItem.getRecordDuration();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " |startTime " + startTime + " |endTime "
                        + endTime + " |sstartTime " + sstartTime
                        + " |sendTime " + sendTime);
                if (startTime <= sstartTime && endTime > sstartTime) {
                    replacetItems.add(sItem);

                } else if (startTime >= sstartTime && endTime <= sendTime) {
                    replacetItems.add(sItem);
                } else if (startTime < sendTime && endTime >= sendTime) {
                    replacetItems.add(sItem);
                } else if (sItem.getRepeatMode() == 0||item.getRepeatMode() == 0) {// daily
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
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startTime=" + startTime + " sstartTime=="
                            + sstartTime + " endtime=" + endTime + "sendTime=="
                            + sendTime);
                    if (startTime <= sstartTime && endTime > sstartTime) {
                        replacetItems.add(sItem);

                    } else if (startTime >= sstartTime && endTime <= sendTime) {
                        replacetItems.add(sItem);
                    } else if (startTime < sendTime && endTime >= sendTime) {
                        replacetItems.add(sItem);
                    }
                    // }
                } else if (sItem.getRepeatMode() >0&&sItem.getRepeatMode()<128||
                item.getRepeatMode() >0&&item.getRepeatMode()<128) {
                    long mod = (startTime - sstartTime) / (24 * 60 * 60);
                    long yushu = (startTime - sstartTime) % (24 * 60 * 60);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"mod-->"+mod+",,yushu-->"+yushu);
                    if (yushu < 60&&yushu>=0) {
                        startTime = startTime - mod * 24 * 60 * 60 - yushu;
                        endTime = endTime - mod * 24 * 60 * 60 - yushu;
                    } else {
                        startTime = startTime - mod * 24 * 60 * 60;
                        endTime = endTime - mod * 24 * 60 * 60;
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"starttime->"+startTime+",,endtime->"+endTime);
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

    public void setType(int type) {
        TYPE = type;
    }

    public int getType() {
        return TYPE;
    }

    public void setEventType(int type) {
        eventType = type;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventId(int id) {
        eventId = id;
    }

    public int getEventId() {
        return eventId;
    }

    public synchronized static ScheduleListItemDialog getInstance(){
        return scheduleListItemDialog;
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
