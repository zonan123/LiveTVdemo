package com.mediatek.wwtv.setting.widget.view;

import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import androidx.core.text.TextUtilsCompat;

import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;

import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog.OnConfirmClickListener;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog.OnCancelClickListener;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.setting.base.scan.adapter.StateScheduleListAdapter;
import com.mediatek.wwtv.setting.base.scan.model.StateScheduleList;
import com.mediatek.wwtv.setting.util.Util;
import com.mediatek.wwtv.tvcenter.dvr.ui.ScheduleListItemDialog;
//import com.mediatek.wwtv.tvcenter.dvr.controller.RegistOnDvrDialog;
import com.mediatek.wwtv.tvcenter.dvr.manager.DevListener;
import com.mediatek.wwtv.tvcenter.dvr.manager.DevManager;
import com.mediatek.wwtv.tvcenter.dvr.manager.Core;
import com.mediatek.wwtv.tvcenter.epg.cn.EPGCnActivity;
import com.mediatek.wwtv.tvcenter.epg.eu.EPGEuActivity;
import com.mediatek.wwtv.tvcenter.epg.sa.EPGSaActivity;
import com.mediatek.wwtv.tvcenter.epg.us.EPGUsActivity;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.dm.MountPoint;
import com.mediatek.dm.DeviceManagerEvent;
import com.mediatek.twoworlds.tv.model.MtkTvBookingBase;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvTimeFormatBase;
import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.InstrumentationHandler;
import com.mediatek.twoworlds.tv.MtkTvRecordBase;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;

public class ScheduleListDialog extends CommonDialog implements OnItemClickListener ,DevListener {
	private static final String TAG = "ScheduleListDialog";

    private static final float wScale = 0.7f;
    private static final float hScale = 0.8f;

    private ListView mScheduleList;
    private TextView mNoRecord;
	private ImageView selectView;
	private TextView selectText;
    private TextView mDiskInfoTitle;
    private TextView rootview;
    private LinearLayout delet_layout;
    public boolean epgFlag = false;
    private final static int MSG_NOTIFY_DEVICE_MOUNT = 0;
    private final static int MSG_DELAY_TIME = 1000;
    public final static int Auto_Dismiss_List_Dialog_Timer = 4;

	private List<MtkTvBookingBase> itemList = new ArrayList<MtkTvBookingBase>();

    private static WeakReference<ScheduleListDialog> scheduleListDialog;
    private final Context mContext;
    private StateScheduleListAdapter stateScheduleListAdapter;
    private int position =0;
    private static NotifyUpDateEPGPvrMarkListener mUpDateEPGListener;

    private final View.AccessibilityDelegate mAccDelegate = new View.AccessibilityDelegate() {
        @Override
        public void sendAccessibilityEvent(View host, int eventType) {
            try {//host.sendAccessibilityEventInternal(eventType);
                Class clazz = Class.forName("android.view.View");
                java.lang.reflect.Method getter =
                        clazz.getDeclaredMethod("sendAccessibilityEventInternal", int.class);
                getter.invoke(host, eventType);
            } catch (Exception e) {
                android.util.Log.d(TAG, "Exception " + e);
            }

            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sendAccessibilityEvent." + eventType + "," + host);
        }

        @Override
        public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
                                                       AccessibilityEvent event) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onRequestSendAccessibilityEvent." + host + "," + child + "," + event);
            do {
                List<CharSequence> texts = event.getText();
                if (texts == null) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":" + texts);
                    break;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "texts->" + texts);
                // confirm which item is focus
                int index =-1;
                if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {// move
                    // focus
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "focus=" + texts.get(1).toString());
                    index = findSelectItem(texts);
                    mScheduleList.setSelection(index);

                } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {// select
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "click=" + texts.get(0).toString());

                }

            } while (false);

            try {//host.onRequestSendAccessibilityEventInternal(child, event);
                Class clazz = Class.forName("android.view.ViewGroup");
                java.lang.reflect.Method getter =
                        clazz.getDeclaredMethod("onRequestSendAccessibilityEventInternal",
                                View.class, AccessibilityEvent.class);
                return (boolean) getter.invoke(host, child, event);
            } catch (Exception e) {
                android.util.Log.d(TAG, "Exception " + e);
            }

            return true;
        }
    };
        private int findSelectItem(List<CharSequence> texts){
            int index =0;
            if(!texts.isEmpty()) {
                for(int i=0;i<getItemList().size();i++){
                    if(Util.longStrToTimeStrN(getItemList().get(i).getRecordStartTime() * 1000).equals(texts.get(1).toString())){
                        index = i;
                        return index;
                    }
                }

            }
            return index;
        }

	public ScheduleListDialog(Context context,int position) {
		super(context, R.layout.pvr_tshfit_schudulelist);
        scheduleListDialog = new WeakReference<>(this);
		DevManager.getInstance().addDevListener(this);
		mContext = context;
		this.position=position ;
//        scheduleListDialog = this;

		/*if (!CommonIntegration.getInstance().isContextInit()) {
			CommonIntegration.getInstance().setContext(mContext.getApplicationContext());
         */
		initData();
		 //initView2();
	}

	private void initData(){
		this.getWindow().setLayout(
				(int) (ScreenConstant.SCREEN_WIDTH * wScale),
				(int) (ScreenConstant.SCREEN_HEIGHT * hScale));
		/*if (!CommonIntegration.getInstance().isContextInit()) {
		CommonIntegration.getInstance().setContext(mContext.getApplicationContext());
     */
	try {
        List<MtkTvBookingBase> books = StateScheduleList.getInstance().queryItem();
        if (books != null) {
//            Iterator<MtkTvBookingBase> iterator = books.iterator();
//            while (iterator.hasNext()) {
//                TIFChannelInfo channelInfo = TIFChannelManager.getInstance(mContext)
//                        .getTIFChannelInfoById(iterator.next().getChannelId());
//                if (channelInfo == null) {
//                    StateScheduleList.getInstance(mContext).deleteItem(iterator.next());
//                    iterator.remove();
//                }else
//                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"channelinfo"+channelInfo.toString());
//            }
            setItemList(books);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
	}

    public static ScheduleListDialog getDialog() {
        if (scheduleListDialog == null){
            return null;
        }
        return scheduleListDialog.get();
    }

    @Override
    public void initView() {
        super.initView();
        mScheduleList = (ListView) findViewById(R.id.schedulelist_list);
        mScheduleList.setDivider(null);
        mNoRecord = (TextView) findViewById(R.id.schedulelist_nofiles);
        mDiskInfoTitle = (TextView) findViewById(R.id.schedulelist_title_diskinfo);
        selectView = (ImageView) findViewById(R.id.schedulelist_icon_select);
        selectText = (TextView) findViewById(R.id.schedulelist_icon_selecttext);
        TextView addText = (TextView) findViewById(R.id.schedulelist_icon_selecttext);
        TextView deletText = (TextView) findViewById(R.id.schedulelist_icon_selecttext);
        delet_layout = (LinearLayout)findViewById(R.id.delet_layout);
        rootview = (TextView)findViewById(R.id.schedulelist_title_txt);
        rootview.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        selectText.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        addText.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        deletText.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        mDiskInfoTitle.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        initListener();
    }

    private void initView2() {
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"is true"+modifyUIWhenNoChannels() );
        if (!modifyUIWhenNoChannels()) {
        if (!getItemList().isEmpty()) {
        	stateScheduleListAdapter=	new StateScheduleListAdapter<MtkTvBookingBase>(
                    mContext, getItemList());
        	  mScheduleList.setAdapter(stateScheduleListAdapter);
            mScheduleList.setSelection(position);
            mScheduleList.setVisibility(View.VISIBLE);
            mNoRecord.setVisibility(View.INVISIBLE);
            selectView.setVisibility(View.VISIBLE);
            selectText.setVisibility(View.VISIBLE);
            delet_layout.setVisibility(View.VISIBLE);
            } else {
                mNoRecord.setVisibility(View.VISIBLE);
                mScheduleList.setVisibility(View.INVISIBLE);
                selectView.setVisibility(View.GONE);
                selectText.setVisibility(View.GONE);
                delet_layout.setVisibility(View.GONE);
            }
        }
        setDiskInfoTitle();
        updateDissmissTimer();

    }

    private boolean modifyUIWhenNoChannels() {
        // TODO Auto-generated method stub
        if (TIFChannelManager.getInstance(mContext).
                getAllDTVTIFChannels().size() <= 0) {

            ImageView iconView = (ImageView) findViewById(R.id.schedule_add_item_icon);
            TextView strView = (TextView) findViewById(R.id.schedule_add_item_str);

            iconView.setVisibility(View.INVISIBLE);
            strView.setVisibility(View.INVISIBLE);

            mNoRecord.setText(mContext.getString(R.string.schedulelist_nofiles_disable_add));
            return true;
        } else {
            return false;
    }
    }

    public void setDiskInfoTitle() {
        MountPoint mp = null;
        List<MountPoint> list = DevManager.getInstance().getMountList();
        if (list != null && !list.isEmpty()) {
            mp = list.get(0);
        }
        String str = Util.getGBSizeOfDisk(mp);
        rootview.setText(String.format("%s-", mContext.getString(R.string.pvr_schedulepvr_schedule_list2)));
        mDiskInfoTitle.setText(str);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event){
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"dispatchkeycode=" +event);
    	if(event.getKeyCode()==KeyMap.KEYCODE_MTKIR_EJECT||event.getKeyCode()==KeyMap.KEYCODE_MTKIR_RECORD){
    		onKeyDown(event.getKeyCode(),event);
    		return true;
    	}

    	return super.dispatchKeyEvent(event);
    }

	/*
	 *
	 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // setItemList(StateScheduleList.queryItem(mState.getActivity(),0));
		updateDissmissTimer();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"keycode=" +keyCode);
        switch (keyCode) {
//            case KeyMap.KEYCODE_MTKIR_EJECT:
            case KeyMap.KEYCODE_MTKIR_RECORD:
            case KeyMap.KEYCODE_MTKIR_YELLOW:
                if (TIFChannelManager.getInstance(mContext).
                        getAllDTVTIFChannels().size() <= 0) {
                    return true;
                }
                if (getItemList() != null && getItemList().size() < MtkTvRecordBase.MAX_REMINDER_SLOT_NUM) {
                	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"add item" );
                	if(isEpgFlag()){
                		showEpgitemlist();
                		this.dismiss();
                	}else{
                    MtkTvBookingBase item = new MtkTvBookingBase();
                    item.setEventTitle("1");
                    item.setTunerType(CommonIntegration.getInstance().getTunerMode());
                    long mStartTime = getBroadcastLocalTime();
                    if (mStartTime != -1L) {
                    	item.setRecordStartTime(mStartTime);
                    }
                    item.setRecordDuration(2*60);
                    item.setRepeatMode(128);
                    if(!(DataSeparaterUtil.getInstance() != null && DataSeparaterUtil.getInstance().isSupportPvr())){
        				item.setRecordMode(0);
        			}
                    showItemInfoDialog(item);
                	}
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.pvr_schedule_max_schedule_error),
                            Toast.LENGTH_SHORT).show();
                }
			return true;
            case KeyMap.KEYCODE_BACK:
//                this.dismiss();
            	handler.removeMessages(StateScheduleList.Auto_Dismiss_List_Dialog_Timer);
                handler.sendEmptyMessageDelayed(StateScheduleList.Auto_Dismiss_List_Dialog_Timer,
                        500);
                // DevManager.getInstance().removeDevListener(this);
                com.mediatek.wwtv.tvcenter.util.MtkLog.e("KEYCODE_BACK", "KEYCODE_BACK");
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
         case KeyMap.KEYCODE_MTKIR_RED:
    		 InstrumentationHandler.getInstance().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
    			break;
         case KeyMap.KEYCODE_MTKIR_BLUE:
        	 final List<MtkTvBookingBase> items = getItemList();
        	 if(items.isEmpty()){
        		 break;
        	 }
 			dismiss();
 			SimpleDialog simpleDialog =  (SimpleDialog) ComponentsManager
                .getInstance().getComponentById(
                        NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
 			simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
 			simpleDialog.setConfirmText(R.string.pvr_confirm_yes);
 			simpleDialog.setCancelText(R.string.pvr_confirm_no);
 			simpleDialog.setOnConfirmClickListener(new OnConfirmClickListener() {

 				@Override
 				public void onConfirmClick(int dialogId) {
 					// TODO Auto-generated method stub
 					MtkTvBookingBase item = items.get(mScheduleList.getSelectedItemPosition());
 	                StateScheduleList.getInstance().deleteItem(item);
 	                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showAddConfirmDialog + currenItem  = ");
 	                dismiss();
 	                ScheduleListDialog scheduleListDialog = new ScheduleListDialog(mContext,0);
 	                scheduleListDialog.setEpgFlag(false);
 	                scheduleListDialog.show();
 				}
 			},
 					2333);
            simpleDialog.setOnCancelClickListener(new OnCancelClickListener(){
                @Override
                public void onCancelClick(int dialogId) {
                    ScheduleListDialog scheduleListDialog = new ScheduleListDialog(mContext,0);
                    scheduleListDialog.setEpgFlag(false);
                    scheduleListDialog.show();
                }
            },
 					2333);
 			 String title1 = mContext.getResources().getString(
 		                R.string.pvr_schedulelist_delete_line1);
 		        String title2 = mContext.getResources().getString(
 		                R.string.pvr_schedulelist_delete_line2);
 			simpleDialog.setContent( title1+" "+title2);
 			simpleDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);//important
 			simpleDialog.show();
 			break;
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }


    private void showEpgitemlist(){
    	Activity act = DestroyApp.getTopActivity();
    	switch (MarketRegionInfo.getCurrentMarketRegion()) {
        case MarketRegionInfo.REGION_CN:
        	 ((EPGCnActivity)act).calledByScheduleList();
            break;
        case MarketRegionInfo.REGION_US:
            ((EPGUsActivity)act).calledByScheduleList();
            break;
        case MarketRegionInfo.REGION_EU:
            ((EPGEuActivity)act).calledByScheduleList();
            break;
        case MarketRegionInfo.REGION_SA:
            ((EPGSaActivity)act).calledByScheduleList();
            break;
        default:
            break;
    }
    }
    /**
     * get time from broadcast for sync android time
     * @return
     */
    private long getBroadcastLocalTime (){
    	MtkTvTimeFormatBase mTime = MtkTvTime.getInstance().getBroadcastLocalTime();
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getBroadcastLocalTime == " + mTime.toSeconds());
    	return mTime.toSeconds();
    }

    private void initListener() {
        mScheduleList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        handler.removeMessages(StateScheduleList.Auto_Dismiss_List_Dialog_Timer);
        MtkTvBookingBase mItem =  getItemList().get(position) ;
        long rec = mItem.getRecordStartTime();
        MtkTvTimeFormatBase timeBaseFrom = new   MtkTvTimeFormatBase();
        timeBaseFrom.setByUtc(rec);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("Start time Log", "mItem.getRecordStartTime() = " +rec);
        MtkTvTimeFormatBase timeBaseTo   = new   MtkTvTimeFormatBase();
        timeBaseFrom.print("Jiayang.li show sys_utc----");
        MtkTvTime.getInstance().convertTime(MtkTvTime.MTK_TV_TIME_CVT_TYPE_SYS_UTC_TO_BRDCST_LOCAL, timeBaseFrom, timeBaseTo);
        timeBaseTo.print("Jiayang.li show brcst_utc--");
        mItem.setRecordStartTime(timeBaseTo.toSeconds());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("Start time Log", "timeBaseTo.toSeconds() = " + timeBaseTo.toSeconds());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onItemClick + item info = " + mItem.toString());
                this.position=position ;
        if(mItem.getEventId()==0){
           ScheduleListItemDialog scheduleListItemDialog = new ScheduleListItemDialog(mContext, mItem);
           scheduleListItemDialog.setType(1);//1:edit
		   scheduleListItemDialog.show();
           dismiss();
        }else {
           Toast.makeText(mContext, mContext.getString(R.string.dvr_schedule_event_toast), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @return the itemList
     */
	public List<MtkTvBookingBase> getItemList() {
        return itemList;
    }

    /**
     * @param itemList the itemList to set
     */
	public void setItemList(List<MtkTvBookingBase> itemList) {
        this.itemList = itemList;
        initView2();
    }

    public void updateDissmissTimer() {
        handler.removeMessages(StateScheduleList.Auto_Dismiss_List_Dialog_Timer);
        handler.sendEmptyMessageDelayed(StateScheduleList.Auto_Dismiss_List_Dialog_Timer,
                Core.AUTO_DISSMISS_TIMER);
    }

    public void dismiss() {
        DevManager.getInstance().removeDevListener(this);
        ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_PVR_DIALOG_HIDE,0);
        super.dismiss();
        if(mUpDateEPGListener != null){
            mUpDateEPGListener.updatePvrMark();
        }
    }

    //for update epg pvr mark begin
    public interface NotifyUpDateEPGPvrMarkListener {
        void updatePvrMark();
    }

    public static void setUpDateEPGPvrMarkListener(NotifyUpDateEPGPvrMarkListener listener) {
        mUpDateEPGListener = listener;
    }
    //for update epg pvr mark end

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Auto_Dismiss_List_Dialog_Timer:
                    if (ScheduleListDialog.this != null && ScheduleListDialog.this.isShowing()) {
                        ScheduleListDialog.this.dismiss();
                    }
                    break;
                case MSG_NOTIFY_DEVICE_MOUNT:
                    setDiskInfoTitle();
                    break;
                default :
                	break;
            }

        }
    };

    @Override
    public void onEvent(DeviceManagerEvent event) {
        switch (event.getType()) {
            case DeviceManagerEvent.mounted:
            case DeviceManagerEvent.umounted:
                handler.sendEmptyMessageDelayed(MSG_NOTIFY_DEVICE_MOUNT, MSG_DELAY_TIME);
                break;
            default:
                break;
        }
    }

	private void showItemInfoDialog(MtkTvBookingBase t) {
	    ScheduleListItemDialog scheduleListItemDialog = new ScheduleListItemDialog(mContext, t);
        scheduleListItemDialog.setEventType(0);
        scheduleListItemDialog.setType(0);
        scheduleListItemDialog.show();
        this.dismiss();

    }

    public boolean isEpgFlag() {
        return epgFlag;
    }

    public void setEpgFlag(boolean epgFlag) {
        this.epgFlag = epgFlag;
    }

    @Override
    public void show(){
//    	 scheduleListDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//important
    	initData();
    	super.show();
    	setWindowPosition();
        mScheduleList.setAccessibilityDelegate(mAccDelegate);
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
// 		float chmarginY = sca.getFloat();
 		mContext.getResources().getValue(R.dimen.nav_channellist_marginX, sca,
 				true);
// 		float chmarginX = sca.getFloat();
 		mContext.getResources().getValue(R.dimen.nav_channellist_size_width,
 				sca, true);
 		float chwidth = sca.getFloat();
 		mContext.getResources().getValue(R.dimen.nav_channellist_size_height,
 				sca, true);
// 		float chheight = sca.getFloat();
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
}
