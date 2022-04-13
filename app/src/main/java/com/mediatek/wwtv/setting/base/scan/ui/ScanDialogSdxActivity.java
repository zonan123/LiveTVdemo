package com.mediatek.wwtv.setting.base.scan.ui;

import java.io.File;
import java.util.List;

import com.mediatek.wwtv.setting.LiveTvSetting;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.commonview.Loading;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.MessageType;
import com.mediatek.wwtv.tvcenter.util.SDXFileParser;
import com.mediatek.wwtv.tvcenter.util.SDXFileParser.ResultCallback;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ScanDialogSdxActivity extends BaseCustomActivity implements ResultCallback{
    
    private static final String TAG = "ScanDialogSdxActivity";
    private Context mContext;
    private Loading loading;
    // scan status
    private TextView mStateTextView;
    private TextView mDVBSChannels;
    private TextView mAnalogChannel;
    private TextView mNumberChannel;
    private TextView mTunerModeView;
    // scan progress
    private ProgressBar mScanprogressbar;
    private TextView mFinishpercentage;
    // By Ese key, scan the number of units Dialog was dimiss
    private TVContent mTV;
    private int mLastChannelId;
    private Toast toast;
    private boolean isChannelSelected;
    private String mSdxFileName = null;
    private SDXFileParser mFileParser;
    private final static int SCAN_MSG = 1;

    private CompositeDisposable mDisposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mTV = TVContent.getInstance(mContext);
        mTV.stopTimeShift();
        mTV.mHandler = mHandler;
        mSdxFileName = getIntent().getStringExtra("FileName");
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mSdxFileName="+mSdxFileName);
        initTvscan(mContext, getIntent().getStringExtra("ActionID"));
        mFileParser = SDXFileParser.getInstance(mContext);
        startTask();
    }
    
    
    private void startTask() {
        if(mSdxFileName == null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "SdxFile null return");
            return;
        }
        mLastChannelId = EditChannel.getInstance(mContext).getCurrentChannelId();
        mFileParser.setCallback(this);
        mDisposables.add(Completable.defer(()->{
            mTV.getScanManager().prepareSdxScan(this);
            return Completable.complete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(()->{
                    mFileParser.startParse(mTV.getScanManager().getDVBSallOrbPos(), CommonIntegration.getInstance().getSvl(), mTV.getScanManager().getDVBSsatIds());
         }));
    }
    
    private void setScanMsg(int stringId) {
        mHandler.removeMessages(SCAN_MSG);
        Message msg = Message.obtain();
        msg.what = SCAN_MSG;
        msg.arg1 = stringId;
        mHandler.sendMessage(msg);
    }
    
    private void initTvscan(final Context context, String itemId) {
        setContentView(R.layout.scan_dialog_layout);

        this.mContext = context;
        if(!LiveTvSetting.isBootFromLiveTV()){//from settings
            getWindow().setBackgroundDrawableResource(R.drawable.screen_background_black);
        }
        mStateTextView = (TextView) findViewById(R.id.state);
        mStateTextView.setText(context.getString(R.string.menu_setup_channel_scan));

        loading = (Loading) findViewById(R.id.setup_tv_scan_loading);
        mScanprogressbar = (ProgressBar) findViewById(R.id.scanprogressbar);
        mScanprogressbar.setMax(100);
        mScanprogressbar.setProgress(0);

        mFinishpercentage = (TextView) findViewById(R.id.finishpercentage);
        mFinishpercentage.setText(String.format("%3d%s",0,"%"));
        mDVBSChannels = (TextView) findViewById(R.id.dvbsusedigital_channels);
        mNumberChannel = (TextView) findViewById(R.id.numberchannel);
        mAnalogChannel = (TextView) findViewById(R.id.analoguechannel);
        TextView mTunerModeView = (TextView) findViewById(R.id.trun_mode);
        mTunerModeView.setText(mContext.getString(R.string.dvbs_general_satellite));

        mDVBSChannels.setText(String.format("%s%3d",mContext.getString(R.string.menu_setup_channel_scan_dig), 0));
        mAnalogChannel.setText(mContext.getString(R.string.menu_setup_satellite_name) + mTV.getScanManager().getFirstSatName());
        mNumberChannel.setText(String.format("%s: ",getString(R.string.satellites)));

    }
    
    public CharSequence getSatName() {
        String scanIndex=String.format("%d/%d", mTV.getScanManager().getDVBSCurrentIndex()+1,mTV.getScanManager().getDVBSTotalSatSize());
        return String.format("%s: %4s",getString(R.string.satellites),scanIndex);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if(mFileParser != null && mFileParser.getStatus() == AsyncTask.Status.RUNNING){
            mFileParser.cancelTask();
        }
        isChannelSelected = true;
        cancleToast();
        mTV.setConfigValue(MtkTvConfigTypeBase.CFG_MISC_CHANNEL_STORE, 0);
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown"+keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(mStateTextView.getText().toString().equals(mContext.getString(R.string.dvbs_status_storing))){
                    showWaitToast();
                    return false;
                }else if(mStateTextView.getText().toString().equals(mContext.getString(R.string.menu_setup_channel_scan))){
                    mFileParser.cancelTask();
                    mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan_cancel));
                    loading.stopDraw();
                    loading.setVisibility(View.INVISIBLE);
                    return false;
                }else {
                    cancleToast();
                    super.onKeyDown(keyCode, event);
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isChannelSelected "+isChannelSelected);
               /*mTV.getScanManager().rollbackChannelsForSdx();
               if(!isChannelSelected){
                    sendMsgSelectCh();
                    if (mStateTextView.getText().toString().equals(mContext.getString(R.string.menu_setup_channel_scan))){
                        mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan_cancel));
                    }
                    loading.stopDraw();
                    loading.setVisibility(View.INVISIBLE);
                    showToast();
                    return false;
                }else {
                    cancleToast();
                    super.onKeyDown(keyCode, event);                    
                }*/
            case KeyEvent.KEYCODE_MENU:
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void sendMsgSelectCh() {
        if(!mHandler.hasMessages(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL)){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sendMsgSelectCh");
            mHandler.sendEmptyMessageDelayed(MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL,
                    MessageType.delayMillis5);
        }
    }
    
    private Handler mHandler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "MessageType.MESSAGE_AFTER_SCAN_SELECT_CHANNEL>>>");
                selectChannel();
                isChannelSelected = true;
                break;
            case SCAN_MSG:
                mStateTextView.setText(msg.arg1);
                if(msg.arg1 == R.string.menu_setup_channel_scan_done) {
                    mScanprogressbar.setProgress(100);
                    mFinishpercentage.setText(String.format("%3d%s", 100, "%"));
                }
                break;
            default:
                break;
            }
        }

    };
    
    private void selectChannel() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectChannel>>>>>" + mLastChannelId  + ">>" + TIFChannelManager.getInstance(mContext).isChannelsExist());
        new Thread(){
            public void run() {
                TIFChannelInfo info = TIFChannelManager.getInstance(mContext).getFirstChannelForScan();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectChannel>>>>>" + info);
                if(info != null){
                    TIFChannelManager.getInstance(mContext).selectChannelByTIFInfo(info);
                    SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),0);
                }else {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectChannel>>>>> null");
                    EditChannel.getInstance(mContext).selectChannel(mLastChannelId);
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"select broadcast chlist type");
                SaveValue.getInstance(mContext).saveValue("type_"+CommonIntegration.getInstance().getSvl(),0);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"set cam Scan complete");
                com.mediatek.twoworlds.tv.MtkTvCI.getInstance(0).setScanComplete(); 
            };
        }.start();
    }
    
   /* private void showToast(){
        if (toast == null) {
            toast = Toast.makeText(mContext, R.string.menu_setup_ci_10s_answer_tip, Toast.LENGTH_SHORT);
        }
        toast.show();
    }*/

    private void showWaitToast(){
        if (toast == null) {
            toast = Toast.makeText(mContext, R.string.dvbs_storing_wait, Toast.LENGTH_SHORT);
        }
        toast.show();
    }
    
    private void cancleToast(){
        if (toast != null) {
            toast.cancel();
        }
    }

    /*private String convertSatNameByPos(int pos) {
        StringBuilder sb = new StringBuilder();
        sb.append(pos / 10);
        if(pos % 10 != 0) {
          sb.append(".").append(pos % 10);
        }
        String suf = pos > 0 ? "E" : "W";
        return sb.append(suf).toString();
      }*/
    
    private String getWarningInfoByOrbit(List<Integer> targetOrbits) {
        String[] satNames = mContext.getResources().getStringArray(R.array.dvbs_satllite_name_arrays);
        String[] orbits = mContext.getResources().getStringArray(R.array.dvbs_satllite_orbit_arrays);
        int findOrbit = 0;
        StringBuilder builder = new StringBuilder();
        String findName = null;
        for (int target : targetOrbits) {
            for (int i = 0; i < orbits.length; i++) {
                if(orbits[i].contains("E")){
                    findOrbit = (int)(Float.parseFloat(orbits[i].substring(0, orbits[i].length() - 1)) * 10f);
                }else {
                    findOrbit = -(int)(Float.parseFloat(orbits[i].substring(0, orbits[i].length() - 1)) * 10f);
                }
                if(findOrbit == target){
                    findName = satNames[i];
                    break;
                }
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"target="+target+",findName="+findName);
            if(findName != null) {
                builder.append(" ").append(findName).append("\n");
            }
        }
        return builder.toString();
    }
    
    private void showWarningDialog(String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.simple_dialog_title_warning)
        .setMessage(content)
        .setCancelable(true)
        .setPositiveButton(android.R.string.ok, new OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                  dialog.dismiss();
            }
        }).show();
    }


    @Override
    public void onPreExcete() {
        mStateTextView.setText(mContext.getString(R.string.menu_setup_channel_scan));
        loading.drawLoading();
    }


    @Override
    public void onCancelled() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onCancelled");
    }


    int lastProgress = -1; int lastChannelNum = -1; int lastScanningSatIndex = -1;
    @Override
    public void onProgress(int progress, int channelNum, int scanningSatIndex) {
        if(lastProgress != progress) {
            lastProgress = progress;
            mScanprogressbar.setProgress(progress);
            mFinishpercentage.setText(String.format("%3d%s", progress, "%"));
        }
        if(lastScanningSatIndex != scanningSatIndex) {
            lastScanningSatIndex = scanningSatIndex;
            mTV.getScanManager().setDvbsCurSatName(mTV.getScanManager().getDVBSallSatNames()[scanningSatIndex]);
            mTV.getScanManager().setDvbsCurSatIndex(scanningSatIndex);
            mAnalogChannel.setText(mContext.getString(R.string.menu_setup_satellite_name) + mTV.getScanManager().getFirstSatName());
            mNumberChannel.setText(getSatName());
        }
        if(lastChannelNum != channelNum) {
            lastChannelNum = channelNum;
            mDVBSChannels.setText(String.format("%s%3d", mContext.getString(R.string.menu_setup_channel_scan_dig), channelNum));
            mTV.getScanManager().setChannelsNum(channelNum, 2);
        }
    }


    @Override
    public void onParseError() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"Parse Error !!");
        setScanMsg(R.string.menu_setup_channel_scan_error);
        mHandler.post(new Runnable() {
            
            @Override
            public void run() {
                loading.stopDraw();
                loading.setText("");
            }
        });
    }


    @Override
    public void onParseComplete(List<MtkTvChannelInfoBase> result) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onPostExecute result size="+result.size());
        if(result.isEmpty()){
            setScanMsg(R.string.menu_setup_channel_scan_done);
            loading.stopDraw();
            loading.setText("");
            if(!mFileParser.getmTargetOrbits().isEmpty()){
                String satString = getWarningInfoByOrbit(mFileParser.getmTargetOrbits());
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"satString="+satString);
                if(satString != null && satString.length() > 0) {
                    String contentString = mContext.getString(R.string.dvbs_sdx_select_satellite_warning,
                            satString);
                    showWarningDialog(contentString);
                }
            }
        }
        //mTV.getScanManager().rollbackChannelsForSdx();
    }


    @Override
    public void onStartDoInBackground() {
        isChannelSelected = false;
        List<File> files = CommonIntegration.getInstance().getUDiskFiles("sdx");
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStartDoInBackground ");
        File findFile = null;
        for(File file : files){
            if(mSdxFileName.equals(file.getName())){
                findFile = file;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "doInBackground findFile="+findFile.getName());
            }
        }
        mFileParser.setSDXFile(findFile);
    }


    @Override
    public void onStartStore() {
        setScanMsg(R.string.dvbs_status_storing);
    }
    
    @Override
    public void onChannelListLoadComplete() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onChannelListLoadComplete ");
        if (mStateTextView.getText().toString().equals(mContext.getString(R.string.dvbs_status_storing))){
            setScanMsg(R.string.menu_setup_channel_scan_done);
            loading.stopDraw();
            loading.setText("");
            mFileParser.cleanSdxChannelListener();
        }
        sendMsgSelectCh();
    }

}
