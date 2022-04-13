/**
 * @Description: TODO()
 */

package com.mediatek.wwtv.tvcenter.dvr.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.controller.DVRFiles;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.PwdDialog;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.dvr.controller.DVRArrayAdapter;
import com.mediatek.wwtv.tvcenter.dvr.controller.RegistOnDvrDialog;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateBase;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;

import com.mediatek.wwtv.tvcenter.util.KeyMap;
import android.view.accessibility.AccessibilityEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.mediatek.wwtv.tvcenter.util.Toast;

/**
 *extends from popwindow to dialog
 */
public class DvrFilelist extends Dialog {

    private static String TAG = "DvrFilelist";
    private final static int mDefaultDuration = 10;

    private final Activity mContext;
    private final StateBase mState;


    private TextView[] mPVRFileInfo = new TextView[7];
    private ListView mPVRFileListLV;
    private OnItemClickListener listener;

    private RelativeLayout mInfoWindow;
    private final FileReceiver fileReceiver;
    private boolean showInfoWindow = false;

    private List<DVRFiles> mFileList = new ArrayList<DVRFiles>();
    private List<DVRFiles> mCurrentPageList = new ArrayList<DVRFiles>();

    private int mCurrentPage = 0;

    private int subStartindex = 1;
    private int mMaxPage = 0;
    private  static int TOTAL_ITEM_EVERY_PAGE = 5;


    private DVRArrayAdapter pVRArrayAdapter;
    private final Handler handler;

    TextView titlelisTextView;
    TextView page_up;
    TextView page_down;
    TextView delet;
    TextView info;
    private ProgressBar pregressbar;
    private LinearLayout playlist_hint;

	public static final String DELET_DVR_ID_STRING="delet_dvr_position";


    private final AccessibilityDelegate mAccDelegate = new AccessibilityDelegate() {
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

            if(mPVRFileListLV.equals(host)){
            	resetTimeout();
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
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":" + texts);
                // confirm which item is focus
                if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {// move
                	                                                          // focus
                    int index = findSelectItem(texts.get(0).toString());
                    if (index >= 0) {
                        // mPVRFileListLV.setSelection(index);
                    	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"index="+index);
                        setselectItem(index);
                        resetTimeout();
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"focus="+texts.get(0).toString());
                }
                else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {// select
                	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"click="+texts.get(0).toString());
                    if (texts.get(0).toString().equals("Page Up")) {
                        pageUP();
                    } else if (texts.get(0).toString().equals("Page Down")) {
                        pageDown();
                    }
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

        private int findSelectItem(String text) {
            if (mCurrentPageList == null) {
                return -1;
            }
            String pvrFileName = "";
            for (int i = 0; i < mCurrentPageList.size(); i++) {
                DVRFiles dvrFiles = mCurrentPageList.get(i);
                if(!dvrFiles.getmDetailInfo().isEmpty()){
                    pvrFileName=
                            dvrFiles.getChannelName()+"_"+dvrFiles.getDate().replace("/", "")+
                                    "_"+dvrFiles.getmDetailInfo();
                }else{
                    pvrFileName =
                            dvrFiles.getChannelName()+"_"+dvrFiles.getDate().replace("/", "");
                }
                String selectText = text;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"pvrfile->"+pvrFileName);
                if (pvrFileName.equals(selectText)) {
                    return i;
                }
            }
            return -1;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate");
        setContentView(R.layout.pvr_timeshfit_playlist);
        initViews();
    }

    public DvrFilelist(Activity context, StateBase state,
            Handler handler) {

        super(context, R.style.nav_dialog);
        this.mContext = context;
        this.handler = handler;
        // initViews();
        // initList();
        fileReceiver = new FileReceiver();
        mState = state;


    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filefilter = new IntentFilter("com.mediatek.pvr.file");
        mContext.registerReceiver(fileReceiver, filefilter);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            mContext.unregisterReceiver(fileReceiver);
        } catch (Exception e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e("PVRFILE", "unregister error");
        }
    }

    public List<DVRFiles> getmFileList() {
        return mFileList;
    }

    public void setmFileList(List<DVRFiles> mFileList) {
        this.mFileList = mFileList;
    }

    /**
	 *
	 */
    private void initViews() {
        TextView tv1 = (TextView) findViewById(R.id.pvr_channel_num);
        TextView tv2 = (TextView) findViewById(R.id.pvr_channel_str);
        TextView tv3 = (TextView) findViewById(R.id.pvr_programename);
        TextView tv4 = (TextView) findViewById(R.id.pvr_channel_date);
        TextView tv5 = (TextView) findViewById(R.id.pvr_week);
        TextView tv6 = (TextView) findViewById(R.id.pvr_time);
        TextView tv7 = (TextView) findViewById(R.id.pvr_duration);
        TextView tv8 = (TextView) findViewById(R.id.pvr_programe_info);
		titlelisTextView   =  (TextView) findViewById(R.id.playlist_title);
		page_up = (TextView) findViewById(R.id.playlist_page_up);
		page_down = (TextView) findViewById(R.id.playlist_page_down);
		delet = (TextView) findViewById(R.id.playlist_delet);
		info = (TextView) findViewById(R.id.playlist_info);
        pregressbar = (ProgressBar)findViewById(R.id.dvr_record_list_progressbar);
        playlist_hint = (LinearLayout)findViewById(R.id.playlist_hint);

        mPVRFileInfo = new TextView[] {
                tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8
        };
        mInfoWindow = (RelativeLayout) findViewById(R.id.pvr_file_info);
        mPVRFileListLV = (ListView) findViewById(R.id.playlist_list);
        mPVRFileListLV.setFocusableInTouchMode(true);
        TypedValue sca = new TypedValue();
        mContext.getResources().getValue(R.dimen.dvr_file_page_max,sca ,true);
        TOTAL_ITEM_EVERY_PAGE  =(int) sca.getFloat();
        if(pVRArrayAdapter==null) {
            pVRArrayAdapter = new DVRArrayAdapter<DVRFiles>(mContext,
                    R.layout.recode_list_item, mCurrentPageList, subStartindex, 0);
            mPVRFileListLV.setAdapter(pVRArrayAdapter);
        }
        mPVRFileListLV.setDivider(null);
        mPVRFileListLV.setSelected(true);
        mPVRFileListLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mPVRFileListLV.setSelector(R.color.nav_button_select);
        mPVRFileListLV.requestFocus();
        setSelection(0);
        mPVRFileListLV.setOnItemClickListener(getListener());

    }

    // init dialog position
    public void setWindowPosition() {
        WindowManager m = getWindow().getWindowManager();
        Display display = m.getDefaultDisplay();
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        int marginY = (int) (display.getHeight() * 0.0139);
        int marginX = (int) (display.getWidth() * 0.15);
        int menuWidth = (int) (display.getWidth() );
        int menuHeight = (int) (display.getHeight() );
        lp.width = menuWidth;
        lp.height = menuHeight;
        int x = display.getWidth() / 2 - menuWidth / 2 - marginX;
        int y = display.getHeight() / 2 - marginY - menuHeight / 2;
        lp.x = x;
        lp.y = y;
        window.setAttributes(lp);
    }

    @Override
    public void show() {
        super.show();
        titlelisTextView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        page_up.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        page_down.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        delet.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        info.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        mPVRFileListLV.setAccessibilityDelegate(mAccDelegate);
        setWindowPosition();
        resetTimeout();
    }

    private void resetTimeout() {
        if(mPVRFileListLV.getVisibility()==View.VISIBLE) {
            handler.removeMessages(StateDvrFileList.AUTO_DISMISS_FILE_LIST);
            handler.sendEmptyMessageDelayed(StateDvrFileList.AUTO_DISMISS_FILE_LIST,
                    mDefaultDuration * 1000);
        }
    }

    public void setLVonItemClickListener(OnItemClickListener listener) {
        mPVRFileListLV.setOnItemClickListener(listener);
    }

    public void dimissInfobar() {
    	if (handler.hasMessages(StateDvrFileList.AUTO_DISMISS_FILE_LIST)) {
    		handler.removeMessages(StateDvrFileList.AUTO_DISMISS_FILE_LIST);
		}
        if (MtkTvPWDDialog.getInstance().PWDShow() == PwdDialog.PASSWORD_VIEW_SHOW_PWD_INPUT) {
            ComponentsManager.getInstance().showNavComponent(NavBasic.NAV_COMP_ID_PWD_DLG);
        }
        try {
            mContext.unregisterReceiver(fileReceiver);
        } catch (Exception e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e("PVRFILE", "unregister error");
        }
        ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_DVR_FILELIST_HIDE,0);
        super.dismiss();
    }

    public void initList() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.printStackTrace();
        if (mFileList != null) {
            if(pregressbar!=null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"hide progress bar");
                pregressbar.setVisibility(View.GONE);
            }
            mCurrentPageList = mFileList.subList(0,
                    Math.min(mFileList.size(), TOTAL_ITEM_EVERY_PAGE));

            refreshMaxPage();
            if (mPVRFileListLV != null) {
                mPVRFileListLV.setVisibility(View.VISIBLE);
                resetTimeout();
                refreshCurrentPage(0);
            }
            if(getmFileList().isEmpty()){
                playlist_hint.setVisibility(View.GONE);
            }else {
                playlist_hint.setVisibility(View.VISIBLE);
            }
        }
    }

    private void refreshMaxPage() {
        mMaxPage = mFileList.size() / TOTAL_ITEM_EVERY_PAGE;
        if ((mFileList.size() % TOTAL_ITEM_EVERY_PAGE) != 0) {
            mMaxPage++;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int selection = mPVRFileListLV.getSelectedItemPosition();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selecton  = " +selection);
        if (selection == AdapterView.INVALID_POSITION) {
            if (mPVRFileListLV.getCount() > 0) {
                mPVRFileListLV.setSelection(0);
            } else {
                if (showInfoWindow){
                    mInfoWindow.setVisibility(View.INVISIBLE);
                    }
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//                        DvrManager.getInstance().restoreToDefault(StatusType.FILELIST);
                        ((StateDvrFileList) mState).dissmiss();
                        return true;
                    }
					if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
//                        DvrManager.getInstance().restoreToDefault(StatusType.FILELIST);
                        ((StateDvrFileList) mState).dissmiss();
                    }
                    if (TurnkeyUiMainActivity.getInstance() != null) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TurnkeyUiMainActivity");
                        return TurnkeyUiMainActivity.getInstance().onKeyHandler(event.getKeyCode(),
                                event, false);
                    }
                }
                return true;
            }
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
			    case KeyMap.KEYCODE_MENU:
//                    DvrManager.getInstance().restoreToDefault(StatusType.FILELIST);
                    ((StateDvrFileList) mState).dissmiss();
                    break;
                case KeyMap.KEYCODE_DPAD_CENTER:
                    mPVRFileListLV.getOnItemClickListener().onItemClick(mPVRFileListLV,
                            mPVRFileListLV.getSelectedView(), selection,
                            mPVRFileListLV.getSelectedItemId());
                    return true;
                case KeyMap.KEYCODE_DPAD_UP:
                    resetTimeout();
                    selection--;
                    if (selection < 0) {
                        pageUP();
                        selection = 0;
                    } else {
                        setselectItem(selection);
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selecton up = " +selection);
                    return true;
                case KeyMap.KEYCODE_DPAD_DOWN:
                    resetTimeout();
                    selection++;
                    if (selection >= mPVRFileListLV.getCount()) {
                        pageDown();
                        selection = 0;
                    } else {
                        setselectItem(selection);
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selecton down = " +selection);
                    return true;
                case KeyMap.KEYCODE_MTKIR_GREEN:
                    resetTimeout();
                    pageDown();
                    selection = 0;
                    return true;
                case KeyMap.KEYCODE_MTKIR_YELLOW:
                    resetTimeout();
                    if(!getmFileList().isEmpty()){
                        showDeletDialog();
                    }
                    return true;
                case KeyMap.KEYCODE_MTKIR_RED:
                    resetTimeout();
                    pageUP();
                    selection = 0;
                    return true;
                case KeyMap.KEYCODE_MTKIR_INFO:
                case KeyMap.KEYCODE_MTKIR_BLUE:
                    resetTimeout();
                    info();
                    return true;
                case KeyMap.KEYCODE_BACK:
                    //DvrManager.getInstance().restoreToDefault(StatusType.FILELIST);
                    ((StateDvrFileList) mState).dissmiss();
                    return true;
                default:
                    break;
            }
            if (TurnkeyUiMainActivity.getInstance() != null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TurnkeyUiMainActivity");
                return TurnkeyUiMainActivity.getInstance().onKeyHandler(event.getKeyCode(), event,
                        false);
            }
        }
        super.dispatchKeyEvent(event);
        return true;

    }

    private void showDeletDialog(){
        if(mFileList.get((mCurrentPage)*5+mPVRFileListLV.getSelectedItemPosition()).isRecording()){
            Toast.makeText(mContext,mContext.getString(R.string.delet_recording_fail),Toast.LENGTH_LONG).show();
            return;
        }
        SimpleDialog simpleDialog = (SimpleDialog) ComponentsManager
                .getInstance().getComponentById(
                        NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
        if (simpleDialog!=null&&simpleDialog.isShowing()) {
            simpleDialog.dismiss();
        }
        if(simpleDialog != null ) {
            simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
            simpleDialog.setConfirmText(R.string.pvr_confirm_yes);
            simpleDialog.setCancelText(R.string.pvr_confirm_no);
            simpleDialog.setContent(mContext.getResources().getString(
                    R.string.delete_pvr_file_dialog_confirm1) + mContext.getResources().getString(
                    R.string.format_confirm_dialog_line2));
            simpleDialog.setOnConfirmClickListener(new RegistOnDvrDialog(), RegistOnDvrDialog.TYPE_DVR_FILE_DELET);
            simpleDialog.setOnCancelClickListener(new RegistOnDvrDialog(), RegistOnDvrDialog.TYPE_DVR_FILE_DELET);
            Bundle bundle = new Bundle();
            //bundle.putLong(DELET_DVR_ID_STRING, showAndDelet());
            bundle.putLong(DELET_DVR_ID_STRING, mFileList.get((mCurrentPage) * 5 + mPVRFileListLV.getSelectedItemPosition()).getmId());
            simpleDialog.setBundle(bundle);
            ((StateDvrFileList) mState).dissmiss();
            simpleDialog.show();
        }
    }

    public long showAndDelet(){
        int selection = mPVRFileListLV.getSelectedItemPosition();
        DVRFiles fileName = mCurrentPageList.get(selection);
       return fileName.getmId();
    }

    private void setselectItem(int selection) {

        setSelection(selection);
        try {
            for (int i = 0; i < mPVRFileListLV.getCount(); i++) {
                mPVRFileListLV.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 *
	 */
    private void info() {
        if (showInfoWindow){
            mInfoWindow.setVisibility(View.INVISIBLE);
        }else{
            mInfoWindow.setVisibility(View.VISIBLE);
            }

        showInfoWindow = !showInfoWindow;
    }

    public DVRFiles getSelectedFile() {
        int selection = mPVRFileListLV.getSelectedItemPosition();
        return mCurrentPageList.get(selection);
    }

    public void deleteFile(int position) {

        int selection = position;//mPVRFileListLV.getSelectedItemPosition();
        if(mCurrentPageList.isEmpty()){
             com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"mCurrentPageList.isEmpty");
            return;
        }
        DVRFiles fileName = mFileList.get(selection);
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"isrecording->"+fileName.isRecording());
//        StateDvrFileList.getInstance().setFlag(false);
        if(fileName.isRecording()){
            Toast.makeText(mContext,mContext.getString(R.string.delet_recording_fail),Toast.LENGTH_LONG).show();
            return;
        }

        int selectionInTotalList = mFileList.indexOf(fileName);
        //boolean value = false;
        //int valueInt = -1;

        if (selectionInTotalList != -1) {
            DvrManager.getInstance().getController().deletePvrFiles(mContext, fileName.getmId());
            mFileList.remove(selectionInTotalList);
            initList();
        }
        /*if (value || valueInt == 0) {
            mCurrentPageList.remove(selection);
            int index = mFileList.indexOf(fileName);
            if (index != -1) {
                mCurrentPageList.remove(fileName);
            }
        } else {
            mCurrentPageList.remove(selection);

        }

        if (mCurrentPageList.isEmpty()) {
            pageUP();
        } else {
            refreshCurrentPage(selection == 0 ? 0 : selection - 1);
        }*/

        refreshMaxPage();
    }

    private void refreshCurrentPage() {
        int selection = mPVRFileListLV.getSelectedItemPosition();
        refreshCurrentPage(selection);
    }

    /**
	 *
	 */
    private void pageDown() {
        ++mCurrentPage;
        if (mCurrentPage >= mMaxPage) {
            mCurrentPage = 0;
        }
        if (mCurrentPage < 0) {
            mCurrentPage = 0;
        }
        subStartindex = mCurrentPage * TOTAL_ITEM_EVERY_PAGE + 1;
        mCurrentPageList = mFileList.subList(
                mCurrentPage * TOTAL_ITEM_EVERY_PAGE,
                Math.min((mCurrentPage + 1) * TOTAL_ITEM_EVERY_PAGE,
                        mFileList.size()));

        refreshAdapter(0);
    }

    /**
	 *
	 */
    private void pageUP() {
        if (mCurrentPage == 0) {// Fix CR:DTV00583562
            if (0 == mMaxPage) {
                setSelection(mCurrentPageList.size() - 1);
                return;
            }
            mCurrentPage = mMaxPage - 1;

            if (mCurrentPage < 0) {
                mCurrentPage = 0;
            }
            subStartindex = mCurrentPage * TOTAL_ITEM_EVERY_PAGE + 1;
            mCurrentPageList = mFileList.subList(
                    mCurrentPage * TOTAL_ITEM_EVERY_PAGE,
                    Math.min((mCurrentPage + 1) * TOTAL_ITEM_EVERY_PAGE,
                            mFileList.size()));

            refreshAdapter(mCurrentPageList.size() - 1);

        } else
        {
            mCurrentPage = Math.max(0, (--mCurrentPage));
            subStartindex = mCurrentPage * TOTAL_ITEM_EVERY_PAGE + 1;
            mCurrentPageList = mFileList.subList(
                    mCurrentPage * TOTAL_ITEM_EVERY_PAGE,
                    Math.min((mCurrentPage + 1) * TOTAL_ITEM_EVERY_PAGE,
                            mFileList.size()));
            refreshAdapter(mCurrentPageList.size() - 1);
        }
    }

    private void refreshCurrentPage(int selectItem) {
        mCurrentPageList = mFileList.subList(
                mCurrentPage * TOTAL_ITEM_EVERY_PAGE,
                Math.min((mCurrentPage + 1) * TOTAL_ITEM_EVERY_PAGE,
                        mFileList.size()));
        refreshAdapter(selectItem);
    }

    /**
	 *
	 */
    private void refreshAdapter(int selectItem) {

        pVRArrayAdapter.setCurrenSelect(selectItem);
        pVRArrayAdapter.setmObjects(mCurrentPageList);
        pVRArrayAdapter.setSubStartIndex(subStartindex);
        mPVRFileListLV.setAdapter(pVRArrayAdapter);
        pVRArrayAdapter.notifyDataSetChanged();

        // fix DTV00587114
        if (mFileList.isEmpty()) {
            if (showInfoWindow){
                mInfoWindow.setVisibility(View.INVISIBLE);
                }
            return;
        }
        setSelection(selectItem);

    }

    @SuppressLint("NewApi")
    void setSelection(final int index) {

        if (index >= mPVRFileListLV.getCount() || mCurrentPageList.get(index) == null) {

            mPVRFileInfo[0].setText("");
            mPVRFileInfo[1].setText("");
            mPVRFileInfo[2].setText("");
            mPVRFileInfo[3].setText("");
            mPVRFileInfo[4].setText("");
            mPVRFileInfo[5].setText("");
            mPVRFileInfo[6].setText("");
            mPVRFileInfo[7].setText("");
            return;
        }

        mPVRFileListLV.setSelection(index);

        mPVRFileInfo[0].setText(String.format("CH%s", mCurrentPageList.get(index).getChannelNum()));
        String chanelname = mCurrentPageList.get(index).getChannelName();
//        if (chanelname.length() > 10) {
//            chanelname = (chanelname.substring(0, 9)) + "...";
//        }
        mPVRFileInfo[1].setText(chanelname);
        String programName="";
        Pattern pattern = Pattern.compile("\\d{4}_\\d{6}.pvr");
        Matcher matcher = pattern.matcher(mCurrentPageList.get(index).getProgramName());
        while (matcher.find()) {
        	programName = matcher.group();
        }
        mPVRFileInfo[2].setText(programName);
        mPVRFileInfo[3].setText(mCurrentPageList.get(index).getDate());
        mPVRFileInfo[4].setText(mCurrentPageList.get(index).getWeek());
        mPVRFileInfo[5].setText(mCurrentPageList.get(index).getTime());
        if (DvrManager.getInstance().pvrIsRecording()
                && ((StateDvrFileList) mState).getSelectedFile().isRecording()) {
            mPVRFileInfo[6].setText(mContext.getString(R.string.time_zero_start));
        } else {
            mPVRFileInfo[6].setText(mCurrentPageList.get(index).getDurationStr());
        }
        mPVRFileInfo[7].setText(mCurrentPageList.get(index).getmDetailInfo());
    }

    /**
     * @return the listener
     */
    public OnItemClickListener getListener() {
        return listener;
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private void resetFile() {
        try {
            if (mFileList != null) {
                for (DVRFiles pvrfile : mFileList) {
                    if (pvrfile.isRecording) {
                        pvrfile.isRecording = false;
                        // pvrfile.setDuration(MtkTvRecord.getInstance().getRecordingPosition());
                        break;
                    }
                }
                refreshCurrentPage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class FileReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().endsWith("com.mediatek.pvr.file")) {
                resetFile();
            }
        }
    }
}
