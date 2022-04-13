
package com.mediatek.wwtv.tvcenter.nav.inputpanel;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
import android.text.TextUtils;

import androidx.leanback.widget.VerticalGridView;

import com.mediatek.tv.ini.IniDocument;
import com.mediatek.twoworlds.tv.MtkTvHighLevelBase;
import com.mediatek.twoworlds.tv.MtkTvKeyEvent;
import com.mediatek.twoworlds.tv.MtkTvMultiView;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.commonview.BaseActivity;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.input.AbstractInput;
import com.mediatek.wwtv.tvcenter.nav.input.InputUtil;
import com.mediatek.wwtv.tvcenter.nav.inputpanel.InputsAdapter.OnItemClickListerner;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.InstrumentationHandler;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.TextToSpeechUtil;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;

import java.io.File;
import java.util.List;

/**
 * @author sin_yupengwang
 */
public class InputsPanelActivity extends BaseActivity implements IInputsPanelView,
      ComponentStatusListener.ICStatusListener {

  protected static final String TAG = "InputsPanelActivity";
  private static final int AUTO_TUNE = 999;
  private VerticalGridView mListView;
  private InputsAdapter mAdapter;
  private InputSourceManager mInputSourceManager;
  private boolean isTurnkeySencordRunning;

  MtkTvHighLevelBase mMtkTvHighLevelBase ;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.nav_inputs);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onCreate");
    ComponentStatusListener.getInstance().updateStatus(
        ComponentStatusListener.NAV_INPUTS_PANEL_SHOW);
    ComponentStatusListener.getInstance().addListener(
        ComponentStatusListener.NAV_INPUTS_PANEL_SOURCE_KEY, this);
    mInputSourceManager = InputSourceManager.getInstance();
    mListView = (VerticalGridView) findViewById(R.id.side_panel_list);
    mListView.setAdapter(mAdapter = new InputsAdapter(this, InputUtil.getEnableSourceList(this)));
    if(TextToSpeechUtil.isTTSEnabled(this)) {
      mAdapter.setOnItemClickListerner(mOnItemClickListerner);
    }
    isTurnkeySencordRunning = getIntent().getBooleanExtra("extra_is_turnkey_top_running", false);
    mMtkTvHighLevelBase= new MtkTvHighLevelBase();
	getFacCusInfo();

	initHandler(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    InputSourceManager.getInstance().setInputsPanelView(this);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onStart");
  }

  @Override
  protected void onPause() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onPause");
    handler.removeMessages(AUTO_TUNE);
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    notifyFocusChanged();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onResume");
  }

  @Override
  protected void onStop() {
    super.onStop();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onStop");
    InputSourceManager.getInstance().setInputsPanelView(null);
    if(!isFinishing()) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"call super.finish()");
      super.finish();
    }
    ComponentStatusListener.getInstance().updateStatus(
        ComponentStatusListener.NAV_INPUTS_PANEL_HIDE);
    ComponentStatusListener.getInstance().removeListener(this);
  }

  @Override
  public void notifyInputsChanged() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"notifyInputsChanged");
    if (mListView != null && mAdapter != null) {
      mAdapter.refresh(InputUtil.getEnableSourceList(this));
      mListView.setSelectedPosition(
          mAdapter.getSelectPosition(mInputSourceManager.getCurrentInputSourceHardwareId()));
    }
  }

  @Override
  public void notifyFocusChanged() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"notifyFocusChanged");
    if (mListView != null && mAdapter != null) {
      mListView.setSelectedPosition(
          mAdapter.getSelectPosition(mInputSourceManager.getCurrentInputSourceHardwareId()));
    }
  }

  @Override
  public void notifyFocusToNext() {
    if (mListView != null && mAdapter != null) {
      int curPosition = mListView.getSelectedPosition();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"notifyFocusToNext " + curPosition +"    "+mAdapter.getItemCount());
      int next = curPosition + 1;
      if(next >= mAdapter.getItemCount()) {
        next = 0;
      }
      mListView.setSelectedPosition(next);
      handler.removeMessages(AUTO_TUNE);
      handler.sendEmptyMessageDelayed(AUTO_TUNE, 1500);
    }
  }

  @Override
  public void removeMessageForOnKey() {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"removeMessageForOnKey.");
    handler.removeMessages(AUTO_TUNE);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if(!TextToSpeechUtil.isTTSEnabled(this)) {
      int keyCode = event.getKeyCode();
      switch (keyCode) {
        case KeyMap.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_ENTER:
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"dispatchKeyEvent");
          if(event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            int curPosition = mListView.getSelectedPosition();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"dispatchKeyEvent:"+curPosition);
            mOnItemClickListerner.onItemClick(null, curPosition);
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
  public void updateComponentStatus(int statusID, int value) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"updateComponentStatus " + statusID);
    if(statusID == ComponentStatusListener.NAV_INPUTS_PANEL_SOURCE_KEY) {
      if(!TvSingletons.getSingletons().getTurnkeyUiMainActiviteActive()) {
        int curPosition = mListView.getSelectedPosition();
        curPosition++;
        if(curPosition >= mAdapter.getItemCount()) {
          curPosition = 0;
        }
        mListView.setSelectedPosition(curPosition);
      } else {
        int curPosition = mListView.getSelectedPosition();

        while (true) {
          curPosition++;
          if (curPosition == mAdapter.getItemCount() - 1) {
            AbstractInput input = mAdapter.getItem(curPosition);
            if (input.isTVHome()) {
              curPosition = 0;
            }
          }
          if(curPosition >= mAdapter.getItemCount()) {
            curPosition = 0;
          }
          AbstractInput input = mAdapter.getItem(curPosition);;
          if (input != null && !input.isNeedAbortTune()) {
            break;
          }
        }
        if(curPosition > mAdapter.getItemCount() - 1) {
          curPosition = 0;
        }
        mListView.setSelectedPosition(curPosition);
        mOnItemClickListerner.onItemClick(null,mListView.getSelectedPosition());
      }
    }
  }

  private OnItemClickListerner mOnItemClickListerner = new OnItemClickListerner() {

    @Override
    public void onItemClick(View v, int position) {
      boolean isTurnkeyActive = TvSingletons.getSingletons().getTurnkeyUiMainActiviteActive();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onItemClick position:"+position + " isTurnkeyActive:"+isTurnkeyActive);
      List<AbstractInput> enableSourceList = InputUtil.getEnableSourceList(InputsPanelActivity.this);
      if (!enableSourceList.isEmpty()) {
        AbstractInput input = enableSourceList.get(position);
        if (input != null) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"input:"+input.toString(InputsPanelActivity.this));
          if(input.isTVHome()) {
            InstrumentationHandler.getInstance()
                .sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
            return;
          }
          if (input.isNeedAbortTune()) {
            String msgString = getResources().getString(
                R.string.special_input_toast,
                InputUtil.getSourceList(InputsPanelActivity.this).get(input.getHardwareId()));
            Toast.makeText(InputsPanelActivity.this, msgString, 0).show();
            return;
          }
          if(ScanContent.isZiggo() &&
              !CommonIntegration.getInstance().isALLOWChangeForZiggo(input.isTV() || input.isDTV() || input.isATV())) {
            finish();
            return;
          }
          if(abortTuneIfNecessary(input)) {
            finish();
            return;
          }
          int hardwareId = mInputSourceManager.getCurrentInputSourceHardwareId();
          if (isTurnkeyActive && isTurnkeySencordRunning) {
            if(hardwareId == input.getHardwareId()) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onItemClick not need.");
              if (ComponentsManager.getNativeActiveCompId() != 0) {
                MtkTvKeyEvent mtkKeyEvent = MtkTvKeyEvent.getInstance();
                int dfbkeycode = mtkKeyEvent.androidKeyToDFBkey(KeyEvent.KEYCODE_TV);
                mtkKeyEvent.sendKeyClick(dfbkeycode);
              }
              finish();
              return;
            }
            AbstractInput currentInput = InputUtil.getInputById(hardwareId);
            if (currentInput != null && currentInput.isDTV() && input.isDTV()) {
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"change tuner Type, no need set ChgSource to 1 .");
            } else {
              MtkTvMultiView.getInstance().setChgSource(true);
            }
            mInputSourceManager.changeInputSourceByHardwareId(input.getHardwareId());
            finish();
          } else {
            if (!TvSingletons.getSingletons().getTurnkeyUiMainActiviteActive()) {
              mMtkTvHighLevelBase.muteVDP(0);
            }
            Intent intent = new Intent("mtk.intent.input.source");
            intent.setPackage("com.mediatek.wwtv.mediaplayer");
            sendBroadcast(intent);
            InputSourceManager.getInstance().startLiveTV(InputsPanelActivity.this,input.getHardwareId());
          }
        }
      }
    }
  };

  private boolean abortTuneIfNecessary(AbstractInput input) {
    boolean timeShiftStart = SaveValue.readLocalMemoryBooleanValue(MenuConfigManager.TIMESHIFT_START);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"TIMESHIFT_START:"+timeShiftStart);
    if(timeShiftStart) {
      ComponentStatusListener.getInstance().delayUpdateStatus(
          ComponentStatusListener.NAV_INPUTS_PANEL_ENTER_KEY,input.getHardwareId(),500);
      return true;
    }
    if (StateDvr.getInstance() != null && StateDvr.getInstance().isRecording()) {
      int pvrTunerType = SaveValue.getInstance(this).readValue(DvrManager.DVR_BG_TUNER_TYPE, -1);
      AbstractInput pvrInput = InputUtil.getInputById(pvrTunerType);
      if (pvrInput != null) {
        long uriId = SaveValue.getInstance(this).readLongValue(DvrManager.DVR_URI_ID);
        if (!isTurnkeySencordRunning) {
          Intent intent = new Intent();
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          if (input.getHardwareId() == pvrInput.getHardwareId()) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(TvContract.buildChannelUri(uriId));
            startActivity(intent);
            mInputSourceManager.saveInputSourceHardwareId(input.getHardwareId(), "main");
          } else if (input.getHardwareId() != pvrInput.getHardwareId() && (input.isDTV() || input.isATV())) {
            intent.setClass(this, TurnkeyUiMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            mInputSourceManager.saveInputSourceHardwareId(input.getHardwareId(), "main");
            //ComponentStatusListener.getInstance().delayUpdateStatus(
            //    ComponentStatusListener.NAV_INPUTS_PANEL_ENTER_KEY, input.getHardwareId(), 1000);
          } else {
            return false;
          }
        } else {
          if (input.getHardwareId() == pvrInput.getHardwareId()) {
            mInputSourceManager.tuneChannelByUri(TvContract.buildChannelUri(uriId));
          } else if (input.getHardwareId() != pvrInput.getHardwareId() && (input.isDTV() || input.isATV())) {
            ComponentStatusListener.getInstance().delayUpdateStatus(
                ComponentStatusListener.NAV_INPUTS_PANEL_ENTER_KEY, input.getHardwareId(), 500);
          } else {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown, keyCode: " + keyCode);

    if(handlerFacKey(keyCode)) {
      facTimes ++;
      if(facTimes == getSerialKeys().split(",").length){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "try start factory activity.");
        updateFragment(getPackageNames(),getMainActivity());
        facTimes = -1;
      }
      return true;
    } else {
      return super.onKeyDown(keyCode, event);
    }
  }

  public void updateFragment(String packageName,String activityName) {
    if(isPackageExist(packageName)){
      try{
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityName));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.finish();
      }catch (Exception e) {
        e.printStackTrace();
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Exception = " + e.toString());
      }
    }
  }

  private boolean isPackageExist(String packageName){
    try {
      ApplicationInfo app = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "packageInfo = " + app.toString());
      return true;
    }catch(PackageManager.NameNotFoundException e){
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, e.toString());
      return false;
    } catch (Exception e) {
      return false;
    }
  }

  private String serialKeys;
  private String packageName;
  private String mainActivity;
  /**

   */
  private void getFacCusInfo (){
    try{
      String path = getFile();
      if (!TextUtils.isEmpty(path)) {
        IniDocument idc = new IniDocument(path);
        serialKeys = idc.get("SerialKeys").toString().trim();
        packageName = idc.get("PackageName").toString().trim();
        mainActivity = idc.get("MainActivity").toString().trim();

        serialKeys = keyTool(serialKeys);
        packageName = keyTool(packageName);
        mainActivity = keyTool(mainActivity);

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"idc.get(SerialKeys) = " + serialKeys);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"idc.get(PackageName) = " + packageName);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"idc.get(MainActivity) = " + mainActivity);
      }
    } catch (Exception e){
      e.printStackTrace();
    }
    if (TextUtils.isEmpty(serialKeys)) {
      serialKeys = "7,7,7,7";
    }
  }

  private String getSerialKeys (){
    return serialKeys;
  }
  private String getPackageNames (){
    return packageName;
  }
  private String getMainActivity (){
    return mainActivity;
  }
  int facTimes = 0;

  private boolean handlerFacKey(int keyCode){
    if(facTimes < 0){
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"handlerFacKey false");
      return false;
    }
    String [] facKeys = getSerialKeys().split(",");
    int key = Integer.parseInt(facKeys[facTimes].trim());
    return keyCode == key;
  }

  private String keyTool(String key){
    if(key != null && !key.isEmpty()){
      if(key.startsWith("[")){
        key = key.replace("[","");
      }
      if(key.endsWith("]")){
        key = key.replace("]","");
      }
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " = " + key );
    return key;
  }

  private String getFile(){
    String path                    = "/vendor/etc/";
    String vendorPath             = "/vendor/tvconfig/apollo/";
    String configFile             = "ManualEnterFactory.ini";
    String[] paths;
    String cusdata = DataSeparaterUtil.getInstance().getIniFilePath("apk", "m_pManualEnterFactoryName");
    if(!TextUtils.isEmpty(cusdata)) {
      paths = new String[]{cusdata, vendorPath + configFile,  path+ configFile};
    } else {
      paths = new String[]{vendorPath + configFile,  path+ configFile};
    }

    for(String s : paths) {
      File f = new File(s);
      if (f.exists()) {
        return s;
      }
    }
    return null;
  }

  private Handler handler;
  private void initHandler(Context context) {
    handler = new Handler(context.getMainLooper()) {
      @Override
      public void handleMessage(Message msg) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "autoTuneInputForOneKey"+msg.what);
        if(msg.what == AUTO_TUNE) {
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "autoTuneInputForOneKey");
          mOnItemClickListerner.onItemClick(null, mListView.getSelectedPosition());
        }
      };
    };
  }

}
