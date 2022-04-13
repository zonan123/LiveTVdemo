package com.android.tv.onboarding;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.media.tv.TvInputInfo;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.android.tv.SetupPassthroughActivity;
import com.android.tv.common.actions.InputSetupActionUtils;
import com.android.tv.common.ui.setup.OnActionClickListener;
import com.android.tv.common.ui.setup.SetupFragment;
import com.android.tv.common.ui.setup.SetupMultiPaneFragment;
import com.android.tv.common.ui.setup.animation.SetupAnimationHelper;
import com.mediatek.tv.ui.pindialog.PinDialogFragment;
import com.mediatek.tv.ui.pindialog.PinDialogFragment.OnPinCheckedListener;
import com.android.tv.util.OnboardingUtils;
import com.android.tv.util.SetupUtils;
import com.android.tv.util.TvInputManagerHelper;

import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.base.scan.ui.ScanDialogActivity;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor;
import com.mediatek.wwtv.tvcenter.commonview.BaseActivity;
import com.mediatek.twoworlds.tv.MtkTvScan;

import java.util.List;

public class SetupSourceActivity extends BaseActivity implements OnActionClickListener,OnPinCheckedListener{

    private static final int REQUEST_CODE_START_SETUP_ACTIVITY = 1;
    private static final String TAG = "SetupSourceActivity";
    private String mInputIdUnderSetup;
    private TvInputInfo mInputInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      SetupAnimationHelper.initialize(this);
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_setupsource);

      if(savedInstanceState == null) {
        SetupSourcesFragment setupFragment = new SetupSourcesFragment();
        setupFragment.enableFragmentTransition(
            SetupFragment.FRAGMENT_ENTER_TRANSITION
            | SetupFragment.FRAGMENT_EXIT_TRANSITION
            | SetupFragment.FRAGMENT_RETURN_TRANSITION
            | SetupFragment.FRAGMENT_REENTER_TRANSITION);
        setupFragment.setFragmentTransition(SetupFragment.FRAGMENT_EXIT_TRANSITION, Gravity.END);
        getFragmentManager().beginTransaction().replace(R.id.container,setupFragment).commit();
      }
    }

    @Override
    public boolean onActionClick(String category, int id, Bundle params) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "category:"+category+" id:"+id+" params:"+params);
      if(category.equals(SetupSourcesFragment.ACTION_CATEGORY)) {
        switch (id) {
          case SetupMultiPaneFragment.ACTION_DONE:
            finish();
            break;
          case SetupSourcesFragment.ACTION_ONLINE_STORE:
            try {
              startActivity(OnboardingUtils.ONLINE_STORE_INTENT);
            } catch (Exception e) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Exception1");
            }
            break;
          case SetupSourcesFragment.ACTION_SETUP_INPUT:
              if(params == null) {
                  break;
              }
              String inputId = params.getString(SetupSourcesFragment.ACTION_PARAM_KEY_INPUT_ID);
              TvInputManagerHelper managerHelper = ((DestroyApp)getApplicationContext()).getTvInputManagerHelper();
              mInputInfo = managerHelper.getTvInputInfo(inputId);

              if(mInputInfo.getServiceInfo().packageName.equals("com.mediatek.tvinput")){
                  if(TVContent.getInstance(this).isTvInputBlock() || EditChannel.getInstance(this).getBlockChannelNumForSource() > 0
                          /*|| TVContent.getInstance(this).getRatingEnable() == 1*/
                          || (TVContent.getInstance(this).getCurrentTunerMode() == 1 && (ScanContent.isZiggoUPCOp() || ScanContent.isVooOp()))){
                      Log.d(TAG, "SetupSourceActivity show Pwd");
                      PinDialogFragment dialog =
                              PinDialogFragment.create(
                              PinDialogFragment.PIN_DIALOG_TYPE_START_SCAN);
                          dialog.show(getFragmentManager(), "PinDialogFragment");
                  }else {
                      handleSetupInput(mInputInfo);
                }
              }else {
                  handleSetupInput(mInputInfo);
            }

            break;
		default:
			break;
        }
      }
      return true;
    }

    private void handleSetupInput(TvInputInfo input) {
      if(input.getServiceInfo().packageName.equals("com.mediatek.tvinput") && CommonIntegration.getInstance().isCNRegion()) {
        if(MtkTvScan.getInstance().isScanning()){
            Toast.makeText(this, R.string.menu_string_toast_scanning_background, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ScanDialogActivity.class);
        if(CommonIntegration.getInstance().isCNRegion() && CommonIntegration.getInstance().isCurrentSourceATV()) {
            intent.putExtra("need_full_screen",true);
            intent.putExtra("ActionID", MenuConfigManager.TV_SYSTEM);
            intent.putExtra("ActionParentID", MenuConfigManager.TV_CHANNEL_SCAN);
        } else {
            intent.putExtra("need_full_screen",true);
            intent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN);
        }

        startActivity(intent);
        finish();
      } else {
        Intent intent = createSetupIntent(input.createSetupIntent(), input.getId());
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, input.toString());
        if (intent == null) {
          Toast.makeText(
              this,
              R.string.msg_no_setup_activity,
              Toast.LENGTH_SHORT)
              .show();
          return;
        }
        // Even though other app can handle the intent, the setup launched by
        // Live
        // channels should go through Live channels SetupPassthroughActivity.
        intent.setComponent(
            new ComponentName(this, SetupPassthroughActivity.class));
        try {
          // Now we know that the user intends to set up this input. Grant
          // permission for writing EPG data.
          mInputIdUnderSetup = input.getId();
          SetupUtils.grantEpgPermission(
              this, input.getServiceInfo().packageName);
          startActivityForResult(intent, REQUEST_CODE_START_SETUP_ACTIVITY);
        } catch (ActivityNotFoundException e) {
          mInputIdUnderSetup = null;
          Toast.makeText(
              this,
              getString(
                  R.string.msg_unable_to_start_setup_activity,
                  input.loadLabel(this)),
                  Toast.LENGTH_SHORT)
                  .show();
        }
      }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "onActivityResult, requestCode=" + requestCode + ",resultCode=" + resultCode);
        if(requestCode == REQUEST_CODE_START_SETUP_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                TIFChannelManager channelManager = ((DestroyApp)getApplicationContext()).getChannelDataManager();
                int count = channelManager.getChannelCountForInput(mInputIdUnderSetup);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"count:"+count);
                String text;
                if (count > 0) {
                    if(count == 1) {
                        text = getString(R.string.msg_channel_added_one,count);
                    } else {
                        text = getString(R.string.msg_no_channel_added_other,count);
                    }
                } else {
                    text = getString(R.string.msg_no_channel_added);
                }
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

            }
			try {
				//force browseable
				ContentValues values = new ContentValues();
				values.put(TvContract.Channels.COLUMN_BROWSABLE, 1);
				values.put(TvContract.Channels.COLUMN_SEARCHABLE, 1);
				int count = this.getContentResolver().update(TvContract.Channels.CONTENT_URI, values,
						TvContract.Channels.COLUMN_INPUT_ID + " = ?", new String[]{mInputIdUnderSetup});
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"count after:"+count + ":" + mInputIdUnderSetup);
				if(count == 0 && !mInputIdUnderSetup.contains("com.mediatek.tvinput")){
				    selectOther3rdChannel();
				}else if(count > 0 && CommonIntegration.getInstance().getCurrentChannelId() == -1){
				    selectAdded3rdChannel();
                }
			} catch (Exception e) {
				e.printStackTrace();
			}
			mInputIdUnderSetup = null;
        }
    }

    private void selectOther3rdChannel() {
        List<TIFChannelInfo> list = TIFChannelManager.getInstance(this).get3RDChannelList();
        TIFChannelInfo foundInfo = null;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"lselectAdded3rdChannel list.size="+list.size());
        if(!list.isEmpty()){
            for(TIFChannelInfo info : list){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"list input id="+info.mInputServiceName);
                if(mInputIdUnderSetup.equals(info.mInputServiceName)){
                    continue;
                }
                foundInfo = info;
            }
            if(foundInfo != null){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"foundInfo input id="+foundInfo.mInputServiceName);
                TIFChannelManager.getInstance(this).selectChannelByTIFInfo(foundInfo);
            }else {
                setBroadCastChannelList();
            }
        }else {
            setBroadCastChannelList();
        }
    }

    private void selectAdded3rdChannel() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectAdded3rdChanne");
        final String innputIdUnderSetupCopy = mInputIdUnderSetup;
        TVAsyncExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                int tryCount = 0;
                while(tryCount <= 2){
                    List<TIFChannelInfo> list = TIFChannelManager.getInstance(SetupSourceActivity.this).get3RDChannelList();
                    TIFChannelInfo foundInfo = null;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"selectAdded3rdChannel list.size="+list.size());
                    if(list.size() > 0){
                        for(TIFChannelInfo info : list){
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"lselectAdded3rdChannel input id="+info.mInputServiceName);
                            if(innputIdUnderSetupCopy.equals(info.mInputServiceName)){
                                foundInfo = info;
                                break;
                            }
                        }
                        if(foundInfo != null){
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"foundInfo input id="+foundInfo.mInputServiceName);
                            TIFChannelManager.getInstance(SetupSourceActivity.this).selectChannelByTIFInfo(foundInfo);
                            return;
                        }
                    }else {
                        tryCount++;
                    }
                    try {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Thread sleep beacuse not get added 3rd channel, trycount:"+tryCount);
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Exception");
                    }
                }
            }
          });
    }

    private void setBroadCastChannelList() {
        SaveValue.getInstance(this).saveValue("type_"+CommonIntegration.getInstance().getSvl(),0);
        SaveValue.getInstance(this).saveValue(CommonIntegration.channelListfortypeMask,CommonIntegration.CH_LIST_MASK);
        SaveValue.getInstance(this).saveValue(CommonIntegration.channelListfortypeMaskvalue,CommonIntegration.CH_LIST_VAL);
    }

    /**
     * Returns an intent to start the setup activity for the TV input using {@link
     * InputSetupActionUtils#INTENT_ACTION_INPUT_SETUP}.
     */
    public Intent createSetupIntent(Intent originalSetupIntent, String inputId) {
        if (originalSetupIntent == null) {
            return null;
        }
        Intent setupIntent = new Intent(originalSetupIntent);
        if (!InputSetupActionUtils.hasInputSetupAction(originalSetupIntent)) {
            Intent intentContainer = new Intent(InputSetupActionUtils.INTENT_ACTION_INPUT_SETUP);
            intentContainer.putExtra(InputSetupActionUtils.EXTRA_SETUP_INTENT, originalSetupIntent);
            intentContainer.putExtra(InputSetupActionUtils.EXTRA_INPUT_ID, inputId);
            setupIntent = intentContainer;
        }
        return setupIntent;
    }

    @Override
    public void onPinChecked(boolean checked, int type, String rating) {
        if(type == PinDialogFragment.PIN_DIALOG_TYPE_START_SCAN){
            if (checked) {
                handleSetupInput(mInputInfo);
            }
        }
    }
}
