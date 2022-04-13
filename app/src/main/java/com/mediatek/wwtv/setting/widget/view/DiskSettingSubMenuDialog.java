package com.mediatek.wwtv.setting.widget.view;

import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.mediatek.dm.MountPoint;
import com.mediatek.dm.DeviceManagerEvent;
import com.mediatek.dm.DeviceManager;
import com.mediatek.wwtv.tvcenter.dvr.manager.DevListener;
import com.mediatek.wwtv.tvcenter.dvr.manager.DevManager;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrConstant;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.tiftimeshift.TifTimeShiftManager;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.twoworlds.tv.MtkTvTimeshift;
import com.mediatek.wwtv.setting.util.Util;
import com.mediatek.wwtv.tvcenter.R;


public class DiskSettingSubMenuDialog  extends CommonDialog implements DevListener{

    private static String TAG = "DiskSettingSubMenuDialog";
	private static final float wScale = 0.38f;
	private static final float hScale = 0.26f;

	private TextView mTitle1;
	private TextView mTitle2;
	private TextView mProgressValueTextview;
	private ProgressBar mProgressBar;
	private float speedRate = 0.0f;	private final MountPoint mountPoint;
	private FileOutputStream fis = null;

	private final static int LAYOUT_FORMAT_CONFIRM = R.layout.pvr_tshift_confirmdialog;
	private final static int LAYOUT_PROGRESS = R.layout.pvr_tshift_dialog_layout;
	private final int LAYOUT_LOADING = R.layout.pvr_tshift_dialog_layout_loading;

	private final int FORMAT_CONFIRM_BTN_YES = R.id.confirm_btn_yes;
	private final int FORMAT_CONFIRM_BTN_NO = R.id.confirm_btn_no;
	private final int EXIT_BTN = R.id.pvr_tshift_device_diskop_exit;
    private static Map<String, Float> speedList = new HashMap<String, Float>();
	public enum UiType {
		FORMATCONFIRM, FORMAT_ING, FORMAT_DONE,FORMAT_FAIL, SPEEDTEST_ING, SPEEDTEST_DONE
	}

	private UiType mType;

    private static final int SPEED_TEST_FAILED = 0x100;
    private static final int FORMAT_FAILED = 0x101;
    private static final int SPEED_TEST_SUC = 0x102;
    private static final int FORMAT_SUC = 0x103;
    private static final int SPEED_TEST_PROGRESS = 0x104;
    private static boolean isFormat = false;
    private boolean isHome = false;
    private static WeakReference<DiskSettingSubMenuDialog> mDiskSettingSubMenuDialog;


	public DiskSettingSubMenuDialog(Context context,
			UiType type,MountPoint mountPoint) {
	    super(context, LAYOUT_FORMAT_CONFIRM);
	    Point outSize = new Point();
	    getWindow().getWindowManager().getDefaultDisplay().getRealSize(outSize);
	    if(type==UiType.FORMAT_ING){
	    	this.getWindow().setLayout(
					(int) (outSize.x * 0.08),
					(int) (outSize.y * 0.13));
	    }else{
	    	this.getWindow().setLayout(
					(int) (outSize.x * wScale),
					(int) (outSize.y * hScale));
	    }
	    this.getWindow().setBackgroundDrawableResource(R.drawable.nav_simple_dialog_bg);
		this.mType = type;
		this.mountPoint = mountPoint;
		setCancelable(false);
	}

	@Override
	public void initView() {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"init data");
        mDiskSettingSubMenuDialog = new WeakReference<>(this);
        super.initView();
	}

	/*
	 *
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyMap.KEYCODE_BACK:
			if(mType==UiType.SPEEDTEST_DONE ||mType==UiType.FORMAT_DONE ){
				new Handler().postDelayed(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub

						 DiskSettingSubMenuDialog.this.dismiss();
							DiskSettingDialog dialog = new DiskSettingDialog(mContext,R.layout.pvr_timeshfit_deviceinfo);
				    		dialog.show();
					}

				},300);
			}

			break;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	/*
	 *
	 */
	@Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
		refreshUI(mType);
	}

	/**
	 *
	 */
	private void initViewItem() {
		mTitle1 = (TextView) this.findViewById(R.id.diskop_title_line1);
		mTitle2 = (TextView) this.findViewById(R.id.diskop_title_line2);
		mProgressValueTextview = (TextView) this
				.findViewById(R.id.finishpercentage);
		mProgressBar = (ProgressBar) this.findViewById(R.id.progressbar);
		ImageView iv = (ImageView)this.findViewById(R.id.iv);
		mProgressValueTextview.setText("0%");
		iv.setVisibility(View.GONE);
		mProgressBar.setMax(100);
		mProgressBar.setProgress(0);

	}

	@SuppressLint("DefaultLocale")
	public void refreshProgres(int percent) {
		if (percent >= 100) {
			percent = 100;
			if(mType==UiType.SPEEDTEST_ING)
			{
				refreshUI(UiType.SPEEDTEST_DONE);
			}
			if(mType==UiType.FORMAT_ING)
			{
				refreshUI(UiType.FORMAT_FAIL);
			}

		}
		if (percent < 0) {
			percent = 0;
		}
		StringBuilder progress = new StringBuilder();
		progress.append(String.format("%3.0f", percent * 1f)).append("%");
		mProgressValueTextview.setText(progress);
		mProgressBar.setProgress(percent);
	}

	public void refreshUI(UiType type) {
		mType=type;
		Button exitBtn;
		switch (type) {
		case FORMATCONFIRM:
                setContentView(LAYOUT_FORMAT_CONFIRM);

                TextView mTV1 = (TextView) findViewById(R.id.diskop_title_line1_confirm);
			mTV1.setText(String.format("%s %s", mContext.getResources().getString(
					R.string.format_confirm_dialog_line1), mContext.getResources().getString(
					R.string.format_confirm_dialog_line2)));

//                TextView mTV2 = (TextView) findViewById(R.id.diskop_title_line2_confirm);
//			mTV2.setText(mContext.getResources().getString(
//					R.string.format_confirm_dialog_line2));

			Button btn = (Button) findViewById(R.id.confirm_btn_yes);
			btn.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View v) {
					refreshUI(UiType.FORMAT_ING);
				}
			});

			Button cancelBTN = (Button) findViewById(R.id.confirm_btn_no);
			cancelBTN.requestFocus();
			cancelBTN.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View v) {
					DiskSettingSubMenuDialog.this.dismiss();
					DiskSettingDialog dialog = new DiskSettingDialog(mContext,R.layout.pvr_timeshfit_deviceinfo);
		    		dialog.show();
				}
			});

			break;
		case FORMAT_ING:
			setContentView(LAYOUT_LOADING);
			showFormatDisk();
			break;
		case FORMAT_DONE:
			mTitle1.setText("");
			mTitle2.setText(mContext.getResources().getString(
					R.string.disk_setting_format_done));
			mProgressValueTextview.setText(mContext.getString(R.string.full_max_percent));

			break;
		case SPEEDTEST_ING:
			setContentView(LAYOUT_PROGRESS);
			initViewItem();

			mTitle1.setText(mContext.getResources().getString(
					R.string.disk_setting_speedtesting));
			mTitle2.setText(mContext.getResources().getString(
					R.string.disk_setting_unplugdevice_warnning));
			showSpeedTest("");
			//getCallBack().showSpeedTest();
			exitBtn = (Button) findViewById(R.id.pvr_tshift_device_diskop_exit);
			exitBtn.setVisibility(View.GONE);
			exitBtn.requestFocus();
			break;
		case SPEEDTEST_DONE:
			/*if(mState.getMaxSpeed()==0.0f){
				mState.getHandler().sendEmptyMessageAtTime(mState.SHOW_1ST_MENU, 50);
				this.dismiss();
				break;
			}*/

			mTitle1.setText(mContext.getResources().getString(
					R.string.disk_setting_speedtest_done));

			String speedStr=mContext.getResources().getString(
					R.string.disk_setting_speedtest_maxspeed)+String.format("%3.1f", speedRate)+
					mContext.getResources().getString(
					R.string.disk_setting_speedtest_maxspeed_mb);
			mTitle2.setText(speedStr);

			mProgressValueTextview.setText(mContext.getString(R.string.full_max_percent));

			exitBtn = (Button) findViewById(R.id.pvr_tshift_device_diskop_exit);
			exitBtn.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mProgressBar.getProgress()==100) {
						DiskSettingSubMenuDialog.this.dismiss();
						DiskSettingDialog dialog = new DiskSettingDialog(mContext,R.layout.pvr_timeshfit_deviceinfo);
			    		dialog.show();
					}else {
						Toast.makeText(mContext, mContext.getString(R.string.disk_formating_wait), Toast.LENGTH_LONG).show();
					}
					DiskSettingSubMenuDialog.this.dismiss();

				}
			});
			exitBtn.setVisibility(View.VISIBLE);
			exitBtn.requestFocus();
			break;
		case FORMAT_FAIL:
			mTitle1.setText("");
			mTitle2.setText(mContext.getResources().getString(
					R.string.disk_setting_format_fail));
			mProgressValueTextview.setText(mContext.getString(R.string.full_max_percent));
			exitBtn = (Button) findViewById(R.id.pvr_tshift_device_diskop_exit);
			exitBtn.requestFocus();
			exitBtn.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					DiskSettingSubMenuDialog.this.dismiss();
				    DiskSettingDialog dialog = new DiskSettingDialog(mContext,R.layout.pvr_timeshfit_deviceinfo);
			    	dialog.show();
				}
			});
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case FORMAT_CONFIRM_BTN_YES:
             refreshUI(UiType.FORMAT_ING);
			break;
		case FORMAT_CONFIRM_BTN_NO:
             dismiss();
			break;
		case EXIT_BTN:
             dismiss();
			//mState.setSpeedTesting(false);
                // dismiss();
			break;
		default:
			break;

		}

		// mCallback.setPVR();

		super.onClick(v);
	}

    public boolean showFormatDisk() {
        if (isStopDvrRec()) {
            return false;
        }
        if (isStopTshiftRec()) {
            return false;
        }
        startFormatThread();
        return true;
    }
    private boolean isStopDvrRec (){
        if (StateDvr.getInstance() != null && StateDvr.getInstance().isRecording()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "StateDvr.stopRecording ");
            DvrManager.getInstance().getController().addEventHandler(dvrEventHandler);
            StateDvr.getInstance().getController().stopRecording();
            return true;
        }
        return false;
    }
    private boolean isStopTshiftRec (){
        if (TifTimeShiftManager.getInstance() != null
                && TifTimeShiftManager.getInstance().isTimeshiftStarted()){
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "StopTshiftRec");
            DvrManager.getInstance().getController().addEventHandler(dvrEventHandler);
            MtkTvTimeshift.getInstance().setAutoRecord(false);
            TifTimeShiftManager.getInstance().stopAll();
            return true;
        }
		MtkTvTimeshift.getInstance().setAutoRecord(false);
        return false;
    }
    private void startTshiftRec(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "startTshiftRec ? ");
        if (TifTimeShiftManager.getInstance() != null
                && DvrManager.getInstance().timeShiftIsEnable()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "startTshiftRec ++");
            MtkTvTimeshift.getInstance().setAutoRecord(true);
        }
    }
    private void startFormatThread (){
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "startFormatThread()");
        if (isFormat) {
            return;
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
				setFormat(true);
				try {
					DeviceManager dm = DeviceManager.getInstance();
					if (mountPoint == null){
						return;
					}

					String externSd = mountPoint.mDeviceName;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "umountVol()");
					dm.umountVol(externSd);
                    int resultFormat = dm.formatVol("fat32",mountPoint.mDeviceName); // DMRemoteservice.formatVol(extern_sd);
                    com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "formatVol()");
					if (resultFormat == 0) {
						dm.mountVol(externSd);
                        handler.sendEmptyMessageDelayed(FORMAT_SUC, 100);
					}else {//format failed
                        handler.sendEmptyMessageDelayed(FORMAT_FAILED, 100);
					}
				} catch (Exception e) {
                    handler.sendEmptyMessageDelayed(FORMAT_FAILED, 100);
                }finally{
                    startTshiftRec();
				}

            }
        }, 2500);
	}

	public boolean showSpeedTest (String args){
        String realPath = Util.getPath(mountPoint.mMountPoint,mContext);
		new Thread(new Runnable() {
			public void run() {
				//int index = (int) (Math.random() * 1000);
				List<MountPoint> mps = DevManager.getInstance().getMountList();
				if(mps==null || mps!=null && mps.isEmpty()){
					return ;
				}

				DevManager.getInstance().addDevListener(DiskSettingSubMenuDialog.this);
		        //change /storage/ to /mnt/media_rw/, /storage/ can not write
		        String subPath = mountPoint.mMountPoint.substring(9, mountPoint.mMountPoint.length());
		        String changeDeleteFile = "/mnt/media_rw/" + subPath;
		        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "deleteSelectedFile,changeDeleteFile=" + changeDeleteFile+","+realPath);
                String path = realPath/*mountPoint.mMountPoint+"/Android"*/+"/speedTest0.dat";//String.format(realPath+ "/" + "speedTest%d.dat", index);
				File testFile = new File(path);
				float maxSpeed = 0.0f;
				Long minTime = Long.MAX_VALUE;
				if (testFile.exists()) {
					testFile.delete();
				}
				try {
					testFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				int bufferSize = 1024 * 100; // 7.7
				final float defaultCount = 500L;
				final int defTestTimes =3;
				float counts = defaultCount;
				byte[] writeByte = new byte[bufferSize];
				Long startTime = 0L;
				Long endTime;
				try {
					fis = new FileOutputStream(testFile);
				} catch (FileNotFoundException e1) {
					return;
				}

				startTime = System.currentTimeMillis();
				Long startTime1 = 0L;
				Long startTime2 = 0L;
				int progress = 0;
				try {
					while (counts > 0 ) {
						startTime1 = System.currentTimeMillis();
						fis.write(writeByte);
						startTime2 = System.currentTimeMillis();

						if (minTime > startTime2 - startTime1
								&& (startTime2 - startTime1) > 0) {
							minTime = startTime2 - startTime1;
						}
						counts--;
//						if (counts <= 0) {
//							counts = defaultCount;
//							testTimes--;
//							if (testTimes <= 0) {
//								break;
//							} else {
//								continue;
//							}
//						}
						if(defaultCount != 0) {
							progress = (int) (((defaultCount - counts) / defaultCount) * 100);
						}
						if (progress != 100) {
							Message msg = handler.obtainMessage();
							msg.arg1 = progress;
                            msg.what = SPEED_TEST_PROGRESS;
							handler.sendMessage(msg);
						}
                        if(isHome){
                            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,"home launch");
                            break;
                        }
					}
					fis.close();
					fis = null;
				} catch (IOException e) {
					e.printStackTrace();
					bufferSize=0;
                    handler.sendEmptyMessage(SPEED_TEST_FAILED);
				} finally {
					testFile.delete();
				}
                if(isHome){
                    dismiss();

                }else{
				endTime = System.currentTimeMillis();

				maxSpeed = bufferSize * 1000f / minTime / 1024 / 1024;
				String speedMax = new BigDecimal(String.valueOf(maxSpeed)).setScale(1,
						BigDecimal.ROUND_HALF_UP).toString();
				maxSpeed = Float.parseFloat(speedMax);
				float average = bufferSize * defaultCount * 1000L
						/ (endTime - startTime) / 1024 / 1024;
				if(defTestTimes != 0) {
					speedRate = average / defTestTimes;
				}
                speedList.put("/storage/"+subPath, speedRate);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, speedList.toString());
				Message msg = handler.obtainMessage();
				msg.arg1 = 100;
				msg.what = SPEED_TEST_SUC;
				handler.sendMessage(msg);
                }
			}
		}).start();
		return false ;
	}

    public static float getUsbSpeed(MountPoint item) {

        float speed = 0;
        if (item != null && speedList != null && !speedList.isEmpty()) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MountPoint=" + item.mMountPoint);
            for (String key : speedList.keySet()) {
                if (key.equals(item.mMountPoint)) {
                    speed = speedList.get(key);
                    break;
                }
            }
        }
        return speed;
    }

    public static void resetSpeedList(String item) {

        if (item != null && speedList != null && !speedList.isEmpty()) {
            for (String key : speedList.keySet()) {
                if (key.equals(item)) {
                    speedList.remove(item);
                    break;
                }
            }
        }
    }

	private final Handler handler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			super.dispatchMessage(msg);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"dispatchMessage  msg= " + msg.what);
			switch(msg.what){
            case SPEED_TEST_PROGRESS:
				refreshProgres(msg.arg1);
				break;
            case FORMAT_SUC:
                removeMessages(FORMAT_SUC);
				setFormat(false);
				DiskSettingSubMenuDialog.this.dismiss();
				DiskSettingDialog mDialog = new DiskSettingDialog(mContext,
						R.layout.pvr_timeshfit_deviceinfo);
				mDialog.show();
	    		break;
            case FORMAT_FAILED ://failed
			{
				Toast.makeText(mContext, mContext.getString(R.string.disk_setting_format_fail), Toast.LENGTH_SHORT).show();
                removeMessages(FORMAT_FAILED);
				setFormat(false);
				DiskSettingSubMenuDialog.this.dismiss();
				DiskSettingDialog mDialog1 = new DiskSettingDialog(mContext,
						R.layout.pvr_timeshfit_deviceinfo);
				mDialog1.show();
			}
                break;
            case SPEED_TEST_FAILED : // speed test failed
			{
			Toast.makeText(mContext, mContext.getString(R.string.disksetting_speed_fail), Toast.LENGTH_SHORT).show();
                    removeMessages(SPEED_TEST_FAILED);
			setFormat(false);
			DiskSettingSubMenuDialog.this.dismiss();
			DiskSettingDialog mDialog1 = new DiskSettingDialog(mContext,
					R.layout.pvr_timeshfit_deviceinfo);
			mDialog1.show();
			}
                break;
            case SPEED_TEST_SUC:
            {
                refreshProgres(msg.arg1);
			}
                break;
             default:
                break;
		}
        }
    };
    private final Handler dvrEventHandler = new Handler(){
        int mm = 0;
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int callBack = msg.what;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"dvrEventHandler  = " + callBack);
            switch (callBack) {
            case DvrConstant.MSG_CALLBACK_RECORD_STOPPED://4100
                DvrManager.getInstance().getController().removeEventHandler(dvrEventHandler);
                showFormatDisk();
                break;
            case DvrConstant.MSG_CALLBACK_TIMESHIFT_STATE://4103
                if (mm == callBack) {
                    DvrManager.getInstance().getController().removeEventHandler(dvrEventHandler);
                    showFormatDisk();
                    mm = 0;
                    return;
                }
                mm = DvrConstant.MSG_CALLBACK_TIMESHIFT_EVENT;
                    break;
            case DvrConstant.MSG_CALLBACK_TIMESHIFT_EVENT:
                if (mm == callBack) {
                    DvrManager.getInstance().getController().removeEventHandler(dvrEventHandler);
                    showFormatDisk();
                    mm = 0;
                    return;
                }
                mm = DvrConstant.MSG_CALLBACK_TIMESHIFT_STATE;
            default:
                break;
            }
        }
	};

	@Override
	public void onEvent(DeviceManagerEvent event) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"DeviceManagerEvent event = " + event + " -- " +  event.getType());
		switch(event.getType()){
		case DeviceManagerEvent.umounted:
			 if (fis != null) {
             	try {
                	fis.close();
                    fis = null;
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        break;
		case DeviceManagerEvent.ejecting:
			//format will cause unmount , the UI can not fresh corectly
//				handler.sendEmptyMessage(1);
			break;
		default :
			break;
		}
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (isFormat()) {
			return true;
		}

		return super.dispatchKeyEvent(event);

	}
	public static void setFormat(boolean isFormat) {
		 DiskSettingSubMenuDialog.isFormat = isFormat;
	}
	public static boolean isFormat (){
		return isFormat;
	}

    public static DiskSettingSubMenuDialog getmDiskSettingSubMenuDialog(){
		if(mDiskSettingSubMenuDialog == null){
			return null;
		}
        return mDiskSettingSubMenuDialog.get();
    }

    public  void setHome(boolean flag){
        isHome = flag;
    }

    public void dismiss(){
        super.dismiss();
        dvrEventHandler.removeCallbacksAndMessages(null);
		handler.removeCallbacksAndMessages(null);
    
    }

	@Override protected void onStart() {
		super.onStart();
		DevManager.getInstance().addDevListener(this);
	}

	@Override protected void onStop() {
		super.onStop();
		DevManager.getInstance().removeDevListener(this);
	}
	
	public Handler getDvrEventHandler(){
		return dvrEventHandler;
	}
}
