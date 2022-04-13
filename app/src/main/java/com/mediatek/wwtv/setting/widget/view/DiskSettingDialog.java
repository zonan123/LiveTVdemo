package com.mediatek.wwtv.setting.widget.view;

import android.app.Activity;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.LayoutDirection;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.text.TextUtilsCompat;

import com.mediatek.dm.MountPoint;
//import com.mediatek.wwtv.setting.base.scan.model.StateDiskSettingsCallback;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.Util;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.SimpleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.dm.DeviceManager;
import com.mediatek.dm.DeviceManagerEvent;
import com.mediatek.wwtv.tvcenter.dvr.controller.RegistOnDvrDialog;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.manager.DevListener;
import com.mediatek.wwtv.tvcenter.dvr.manager.DevManager;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;

public class DiskSettingDialog extends CommonDialog implements
        OnItemClickListener, OnClickListener, DevListener {

    private static String TAG = "DiskSettingDialog";
    private Button mSetShiftBtn;
    private Button mSetPVRBtn;
    private Button mFormatBtn;
    private Button mSpeedTestBtn;
    private TextView texttitle;
    private RelativeLayout rootLayout;
    private ListView mUsbList;


    private LinearLayout mOpMenuList;
    /** dialog time out */
    private final static int TIMEOUT = 15000;
    private final static int DIALOG_DISMISS = 1;
    private final static int DIALOG_REFASH = 2;
    // private StateBase mState;
    public List<MountPoint> mountList = new ArrayList<MountPoint>();
    StateInitDiskItemAdapter adapter;
    private final Context mContext;
    private final StorageManager mStorageManager;
    private static WeakReference<DiskSettingDialog> mDiskSettingDialog = null;

    private AccessibilityDelegate mAccDelegate = new AccessibilityDelegate() {

        public boolean onRequestSendAccessibilityEvent(ViewGroup host,
                View child, AccessibilityEvent event) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onRequestSendAccessibilityEvent." + host + ","
                    + child + "," + event);
            do {

                if (mUsbList.equals(host)) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "host:" + mUsbList + "," + host);
                    break;
                } else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":host =" + false);

                }

                List<CharSequence> texts = event.getText();
                if (texts == null) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "texts :" + texts);
                    break;
                }

                // confirm which item is focus
                if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {// move
                                                                                                 // focus

                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, ":ttsSelectchannelIndex =");

                } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {// select
                                                                                          // item
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "click item");

                    showOpMenuList();

                }

            } while (false);

            try {// host.onRequestSendAccessibilityEventInternal(child, event);
                Class clazz = Class.forName("android.view.ViewGroup");
                java.lang.reflect.Method getter = clazz.getDeclaredMethod(
                        "onRequestSendAccessibilityEventInternal", View.class,
                        AccessibilityEvent.class);
                return (boolean) getter.invoke(host, child, event);
            } catch (Exception e) {
                android.util.Log.d(TAG, "Exception " + e);
            }
            return true;
        }

    };

    /**
     * @param context
     * @param layoutID
     */
    public DiskSettingDialog(Context context, int layoutID) {
        super(context, layoutID);
        Point outSize = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getRealSize(outSize);
        // this.getWindow().setLayout(
        // (int) (outSize.x * wScale),
        // (int) (outSize.y * hScale));
        // this.getWindow()
        // .setBackgroundDrawableResource(R.drawable.tv_background);
        this.mContext = context;
        mountList = intiView2();

        mStorageManager = (StorageManager) mContext
                .getSystemService(Context.STORAGE_SERVICE);
        adapter = new StateInitDiskItemAdapter<MountPoint>(mContext, mountList);
        setAdapter(adapter);

    }

    @Override
    public void initView() {
        // TODO Auto-generated method stub
        super.initView();
        mDiskSettingDialog = new WeakReference<>(this);
        rootLayout = (RelativeLayout) findViewById(R.id.device_info_list_root);
        mSetShiftBtn = (Button) findViewById(R.id.disksetting_setshift);
        mSetPVRBtn = (Button) findViewById(R.id.disksetting_setpvr);
        mFormatBtn = (Button) findViewById(R.id.disksetting_format);
        mSpeedTestBtn = (Button) findViewById(R.id.disksetting_speedtest);
        texttitle = (TextView) findViewById(R.id.device_info_title);
        texttitle
                .setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        if (CommonIntegration.isCNRegion()) {
            mSetShiftBtn.setVisibility(View.GONE);
            mSetPVRBtn.setFocusable(true);
            mSetPVRBtn.setFocusableInTouchMode(true);
            mSetPVRBtn.requestFocus();
        } else {
            mSetShiftBtn.setFocusable(true);
            mSetShiftBtn.setFocusableInTouchMode(true);
            mSetShiftBtn.requestFocus();
        }

        mOpMenuList = (LinearLayout) findViewById(R.id.device_info_sub_menu);

        mOpMenuList.setVisibility(View.GONE);
        mUsbList = (ListView) findViewById(R.id.device_info_list);

        mUsbList.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onFocusChange");
            }
        });
        mUsbList.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }

                switch (keyCode) {
                case KeyMap.KEYCODE_MTKIR_GREEN:

                    break;
                case KeyMap.KEYCODE_MTKIR_BLUE:
                    DiskSettingDialog.this.dismiss();

                    break;

                case KeyMap.KEYCODE_DPAD_CENTER:
                case KeyMap.KEYCODE_DPAD_RIGHT:

                    showOpMenuList();
                    refreshTime();
                    return false;
                case KeyMap.KEYCODE_DPAD_UP:
                case KeyMap.KEYCODE_DPAD_DOWN:
                    refreshTime();
                    return false;
                default:
                    break;
                }
                return false;
            }
        });

        mUsbList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                showOpMenuList();
            }
        });
        initListener();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        handler.removeMessages(DIALOG_DISMISS);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "keycode==" + keyCode);
        switch (keyCode) {
        case KeyMap.KEYCODE_DPAD_CENTER:
        case KeyMap.KEYCODE_DPAD_RIGHT:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "1111");
            if (rootLayout.getVisibility() == View.VISIBLE){
            	 showOpMenuList();
            }
            refreshTime();
            break;
        case KeyMap.KEYCODE_DPAD_LEFT:
            if (rootLayout.getVisibility() == View.GONE) {
                hiddenOpMenuList();
            }
            handler.sendEmptyMessageDelayed(DIALOG_DISMISS, TIMEOUT);
            break;
        case KeyMap.KEYCODE_BACK:
            // hiddenOpMenuList();
            // dismiss();
            if (rootLayout.getVisibility() == View.GONE) {
                hiddenOpMenuList();
                return true;
            }
            handler.sendEmptyMessageDelayed(DIALOG_DISMISS, 500);
            break;
        case KeyMap.KEYCODE_MTKIR_BLUE:
            this.dismiss();
            break;
        default:
            handler.sendEmptyMessageDelayed(DIALOG_DISMISS, TIMEOUT);
            break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private List<MountPoint> intiView2() {
        List<MountPoint> list = DeviceManager.getInstance()
                .getMountPointList();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "LIST null" + list.size());
        if ( list.isEmpty()) {
            list = new ArrayList<MountPoint>();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "LIST size=" + list.size());
            MountPoint nullPoint = new MountPoint("", "", mContext
                    .getResources().getString(R.string.dvr_device_no));
            list.add(nullPoint);

        } else {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "LIST size=! null" + list.get(0).mMountPoint);
        }

        return list;
    }

    public void refreshList(ListView view) {
        mountList = intiView2();
        adapter.setGroup(mountList);
        view.setAdapter(adapter);

    }

    public void refreshList() {
        mOpMenuList.setVisibility(View.GONE);
        rootLayout.setVisibility(View.VISIBLE);
        texttitle.setText(R.string.pvr_title_device_info);
        mountList = intiView2();
        adapter.setGroup(mountList);
        adapter.setSelect(getSelectedPosition());
        setAdapter(adapter);
    }

    public ListView getListView() {
        return mUsbList;
    }

    void setAdapter(StateInitDiskItemAdapter adapter) {
        mUsbList.setAdapter(adapter);
    }

    public int getSelectedPosition() {
        return mUsbList.getSelectedItemPosition();
    }

    public int setSelectedPosition(int position) {
        mUsbList.setSelection(position);
        return 0;
    }

    /**
	 *
	 */
    private void initListener() {
        mSetShiftBtn.setOnClickListener(this);
        mSetPVRBtn.setOnClickListener(this);
        mFormatBtn.setOnClickListener(this);
        mSpeedTestBtn.setOnClickListener(this);
        mUsbList.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.disksetting_setshift:
            setTSHIFT();
            break;
        case R.id.disksetting_setpvr:
            setPVR();
            break;
        case R.id.disksetting_format:
            if (StateDvr.getInstance() != null
                    && StateDvr.getInstance().isRecording()) {
                Toast.makeText(mContext, mContext.getString(R.string.recording_formate_msg),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            if (SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(
                    MenuConfigManager.TIMESHIFT_START)) {
                Toast.makeText(mContext, mContext.getString(R.string.timeshif_formate_msg),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            SimpleDialog simpleDialog = (SimpleDialog) ComponentsManager
                    .getInstance().getComponentById(
                            NavBasic.NAV_COMP_ID_SIMPLE_DIALOG);
            simpleDialog.setLevel(SimpleDialog.DIALOG_LEVEL_WARNING);
            simpleDialog.setOnConfirmClickListener(new RegistOnDvrDialog(),
                    getSelectedPosition() * 1000);
            simpleDialog.setOnCancelClickListener(
                    new SimpleDialog.OnCancelClickListener() {
                        @Override
                        public void onCancelClick(int dialogId) {
                            // TODO Auto-generated method stub
                            show();
                        }
                    }, getSelectedPosition() * 1000);
            simpleDialog.setConfirmText(R.string.pvr_confirm_yes);
            simpleDialog.setCancelText(R.string.pvr_confirm_no);
            simpleDialog.setContent(R.string.dvr_device_formate_title);
            simpleDialog.show();
            // ArrayList<MountPoint> deviceList =
            // DeviceManager.getInstance().getMountPointList();
            // Util.showDLog("deviceList.size()::" + deviceList.size());
            // int selection = getSelectedPosition();
            // DiskSettingSubMenuDialog mSubSettingDialog = new
            // DiskSettingSubMenuDialog(mContext,
            // DiskSettingSubMenuDialog.UiType.FORMATCONFIRM,deviceList.get(selection));
            // mSubSettingDialog.show();
            this.dismiss();
            break;
        case R.id.disksetting_speedtest:
            if (StateDvr.getInstance() != null
                    && StateDvr.getInstance().isRecording()) {
                Toast.makeText(
                        mContext,
                        mContext.getString(R.string.disksetting_recording_speed_fail),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            if (SaveValue.getInstance(mContext).readLocalMemoryBooleanValue(
                    MenuConfigManager.TIMESHIFT_START)) {
                Toast.makeText(
                        mContext,
                        mContext.getString(R.string.disksetting_timeshift_speed_fail),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            ArrayList<MountPoint> deviceList1 = DeviceManager.getInstance()
                    .getMountPointList();
            int selection1 = getSelectedPosition();
            StatFs stat = new StatFs(deviceList1.get(selection1).mMountPoint);
            Float fSize = (float) stat.getAvailableBytes()
                    / (1024 * 1024 * 1024);
            if (fSize <= 0) {
                Toast.makeText(
                        mContext,
                        mContext.getString(R.string.disksetting_no_enough_speed_fail),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            if (deviceList1.get(selection1).mFsType
                    .equals(MountPoint.FS_TYPE.FS_TYPE_NTFS)) {
                Toast.makeText(
                        mContext,
                        mContext.getString(R.string.disksetting_ntfs_speed_fail),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            this.dismiss();
            DiskSettingSubMenuDialog mSpeechTest = new DiskSettingSubMenuDialog(
                    mContext, DiskSettingSubMenuDialog.UiType.SPEEDTEST_ING,
                    deviceList1.get(selection1));
            mSpeechTest.show();
            break;
        default:
            break;
        }
        super.onClick(v);
    }

    /*
	 *
	 */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onItemClick");
    }

    public void hiddenOpMenuList() {
        mOpMenuList.setVisibility(View.GONE);
        rootLayout.setVisibility(View.VISIBLE);
        texttitle.setText(R.string.pvr_title_device_info);

    }

    private void showOpMenuList() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "string" + mountList.get(0).mMountPoint);
        if (!("".equalsIgnoreCase(mountList.get(0).mMountPoint))) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "string11" + mountList.get(0).mMountPoint);
            mOpMenuList.setVisibility(View.VISIBLE);
            rootLayout.setVisibility(View.GONE);
            texttitle.setText(R.string.dvr_device_info_title);
            if (CommonIntegration.isCNRegion()) {
                mSetShiftBtn.setVisibility(View.GONE);
                mSetPVRBtn.requestFocus();
                mSetPVRBtn.setFocusable(true);
                mSetPVRBtn.setFocusableInTouchMode(true);
                mSetPVRBtn.requestFocus();
            } else {
                mSetShiftBtn.requestFocus();
                mSetShiftBtn.setFocusable(true);
                mSetShiftBtn.setFocusableInTouchMode(true);
                mSetShiftBtn.requestFocus();
            }
            if (DataSeparaterUtil.getInstance() != null
                    && !DataSeparaterUtil.getInstance().isSupportPvr()) {
                mSetPVRBtn.setVisibility(View.GONE);
            }
        }
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case DIALOG_DISMISS:
                handler.removeMessages(msg.what);
                DiskSettingDialog.this.dismiss();
                break;
            case DIALOG_REFASH:
                refreshList();
                break;
            default :
                break;

            }
        }

    };

    private void refreshTime() {
        handler.removeMessages(DIALOG_DISMISS);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "default msg=");
        handler.sendEmptyMessageDelayed(DIALOG_DISMISS, TIMEOUT);
    }

    public boolean setTSHIFT() {
        ArrayList<MountPoint> deviceList = DeviceManager.getInstance()
                .getMountPointList();

        int selection = getSelectedPosition();
        String diskPath = deviceList.get(selection).mMountPoint;
        String path = Util.getPath(diskPath,mContext);
        if (selection >= 0) {
            Util.tempSetTHIFT(path);
        }
        for (MountPoint point : deviceList) {
            if (!diskPath.equalsIgnoreCase(point.mMountPoint)) {
                Util.tempDelTSHIFT(point, mStorageManager,Util.getPath(point.mMountPoint,mContext));
            }
        }
        adapter.setTypeFlag(StateInitDiskItemAdapter.TYPE_TIMESHIFT);
        refreshList();
        if (selection >= 0) {
            setSelectedPosition(selection);
        }
        return false;
    }

    public boolean setPVR() {
        ArrayList<MountPoint> deviceList = DeviceManager.getInstance()
                .getMountPointList();

        int selection = getSelectedPosition();
        String diskPath = deviceList.get(selection).mMountPoint;
        String path = Util.getPath(diskPath,mContext);

        if (selection >= 0) {
            Util.tempSetPVR(path);
        }
        for (MountPoint point : deviceList) {
            if (!diskPath.equalsIgnoreCase(point.mMountPoint)) {
                Util.tempDelPVR(Util.getPath(point.mMountPoint,mContext));
            }
        }

        adapter.setTypeFlag(StateInitDiskItemAdapter.TYPE_PVR);
        refreshList();
        if (selection >= 0) {
            setSelectedPosition(selection);
        }

        return false;
    }

    @Override
    public void show() {
        super.show();
        setWindowPosition();
        mUsbList.setAccessibilityDelegate(mAccDelegate);
        handler.sendEmptyMessageDelayed(DIALOG_DISMISS, TIMEOUT);
    }
    @Override
    public void dismiss(){
        ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_PVR_DIALOG_HIDE,0);
        super.dismiss();
    }

    @Override
    public void onEvent(DeviceManagerEvent event) {
        switch (event.getType()) {
        case DeviceManagerEvent.mounted:
            DiskSettingSubMenuDialog.resetSpeedList(event.getMountPointPath());
            handler.sendEmptyMessage(DIALOG_REFASH);
            break;
        case DeviceManagerEvent.umounted:
            if (!DiskSettingSubMenuDialog.isFormat()) {
                DiskSettingSubMenuDialog.resetSpeedList(event
                        .getMountPointPath());
                handler.sendEmptyMessage(DIALOG_REFASH);
            }
            break;
        default:
            break;
        }
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
//        float chmarginY = sca.getFloat();
        mContext.getResources().getValue(R.dimen.nav_channellist_marginX, sca,
                true);
//        float chmarginX = sca.getFloat();
        mContext.getResources().getValue(R.dimen.nav_channellist_size_width,
                sca, true);
        float chwidth = sca.getFloat();
        mContext.getResources().getValue(R.dimen.nav_channellist_size_height,
                sca, true);
//        float chheight = sca.getFloat();
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

    @Override protected void onStart() {
        super.onStart();
        DevManager.getInstance().addDevListener(this);
    }

    @Override protected void onStop() {
        super.onStop();
        DevManager.getInstance().removeDevListener(this);
    }

    public static DiskSettingDialog getDiskSettingDialog(){
        if (mDiskSettingDialog == null){
            return null;
        }
        return mDiskSettingDialog.get();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }
}
