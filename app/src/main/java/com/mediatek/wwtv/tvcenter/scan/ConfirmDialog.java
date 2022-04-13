package com.mediatek.wwtv.tvcenter.scan;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;

import com.mediatek.wwtv.setting.base.scan.model.CableOperator;
import com.mediatek.wwtv.tvcenter.nav.view.TwinkleDialog;
import com.mediatek.wwtv.tvcenter.nav.view.UkBannerView;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvScan;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.base.scan.ui.ScanDialogActivity;
import com.mediatek.wwtv.setting.base.scan.ui.ScanThirdlyDialog;
import com.mediatek.wwtv.setting.base.scan.ui.ScanViewActivity;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.commonview.TurnkeyCommDialog;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.BannerView;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

/**
 * Only For EU-PT-Channel Full Scan on Wizard & Menu. (Wizard ?)
 * After scan complete or scan cancel, should pop up dialog to confirm whether save channels when country=PT.
 */
public final class ConfirmDialog {

	private Context mContext;
	private Handler mHandler;

	private ConfirmDialog(Context mContext) {
		super();
		this.mContext = mContext;
		if(mContext instanceof ScanDialogActivity){
		    this.mHandler = TVContent.getInstance(mContext).mHandler;
		}else if (mContext instanceof TurnkeyUiMainActivity) {
		    this.mHandler = ((TurnkeyUiMainActivity)mContext).getHandler();
		    ((TurnkeyUiMainActivity)mContext).setConfirmDialog(this);
        }

	}

	private static ConfirmDialog mDialog;
	public synchronized static ConfirmDialog getInstance(Context context){
        if(mDialog == null){
            mDialog = new ConfirmDialog(context);
        }
        return mDialog;
    }

	private static TurnkeyCommDialog mConfirmDialog;
	private TurnkeyCommDialog mFvpErrorConfirmDialog;

	public boolean isConfirmDialogShowing() {
	    return mConfirmDialog != null && mConfirmDialog.isShowing();
	}
	
	public void showFvpAdviseScanDialog(Context context) {
	    mConfirmDialog = new TurnkeyCommDialog(context,
                3);
        mConfirmDialog
                .setMessage(context.getString(R.string.fvp_advise_scan_msg));
        mConfirmDialog.setButtonYesName(context
                .getString(R.string.menu_ok));
        mConfirmDialog.setButtonNoName(context
                .getString(R.string.exit));
        mConfirmDialog.show();        
        WindowManager.LayoutParams lp = mConfirmDialog.getWindow().getAttributes();
        lp.height = (int) (ScreenConstant.SCREEN_HEIGHT * 0.25);
        mConfirmDialog.getWindow().setAttributes(lp);
        mConfirmDialog.setPositon(-20, 70);
        mConfirmDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                int action = event.getAction();
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && action == KeyEvent.ACTION_DOWN) {
                    Log.d("ConfirmDialog", "on KEYCODE_BACK");
                    mConfirmDialog.dismiss();
                    return true;
                }
                return false;
            }
        });

        mConfirmDialog.getButtonNo().requestFocus();
        
        mConfirmDialog.getButtonNo().setOnKeyListener(new OnKeyListener() {
            
            @Override
            public boolean onKey(View arg0, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER
                            || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        Log.d("ConfirmDialog", "on Button no KEYCODE_ENTER KEYCODE_DPAD_CENTER");
                        mConfirmDialog.dismiss();
                        return true;
                    }
                }
                return false;
            }
        });
        mConfirmDialog.getButtonYes().setOnKeyListener(new OnKeyListener() {
            
            @Override
            public boolean onKey(View arg0, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER
                            || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        Log.d("ConfirmDialog", "on Button yes KEYCODE_ENTER KEYCODE_DPAD_CENTER");
                        mConfirmDialog.dismiss();
                        if(context instanceof ScanDialogActivity){
                            ((ScanDialogActivity) context).finish();
                        }
						startDvbtAdviseScan();
                        return true;
                    }
                }
                return false;
            }
        });      
        
        mConfirmDialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                if(!TurnkeyUiMainActivity.isUKCountry){
                    BannerView bannerView = (BannerView)ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
                    if (bannerView != null) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d("DVBT_TNTHD_ConfirmDialog","bannerView.isVisible():"+bannerView.isVisible());
						bannerView.setVisibility(View.INVISIBLE);
                        bannerView.isKeyHandler(KeyMap.KEYCODE_MTKIR_INFO);
                    }
                }else{
                    UkBannerView bannerView = (UkBannerView)ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
                    if (bannerView != null) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d("DVBT_TNTHD_ConfirmDialog","bannerView.isVisible():"+bannerView.isVisible());
						bannerView.setVisibility(View.VISIBLE);
                        bannerView.isKeyHandler(KeyMap.KEYCODE_MTKIR_INFO);
                    }
                }
                showTwinkle();
            }
        });
    }
	
	public void showDVBTFvpScanErrorDialog(final Context context) {
	    mFvpErrorConfirmDialog = new TurnkeyCommDialog(context,
	            3);
	    mFvpErrorConfirmDialog
	    .setMessage(context.getString(R.string.fvp_scan_error_msg));
	    mFvpErrorConfirmDialog.setButtonYesName(context
	            .getString(R.string.menu_ok));
	    mFvpErrorConfirmDialog.setButtonNoName(context
	            .getString(R.string.menu_ok));
	    mFvpErrorConfirmDialog.show();        
	    WindowManager.LayoutParams lp = mFvpErrorConfirmDialog.getWindow().getAttributes();
	    lp.height = (int) (ScreenConstant.SCREEN_HEIGHT * 0.25);
	    mFvpErrorConfirmDialog.getWindow().setAttributes(lp);
	    mFvpErrorConfirmDialog.setPositon(-20, 70);
	    mFvpErrorConfirmDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
	        public boolean onKey(DialogInterface dialog, int keyCode,
	                KeyEvent event) {
	            int action = event.getAction();
	            if (keyCode == KeyEvent.KEYCODE_BACK
	                    && action == KeyEvent.ACTION_DOWN) {
	                mFvpErrorConfirmDialog.dismiss();
	                return true;
	            }
	            return false;
	        }
	    });
	    
	    mFvpErrorConfirmDialog.getButtonNo().requestFocus();
	    mFvpErrorConfirmDialog.getButtonYes().setVisibility(View.GONE);
	    
	    mFvpErrorConfirmDialog.getButtonNo().setOnKeyListener(new OnKeyListener() {
	        
	        @Override
	        public boolean onKey(View arg0, int keyCode, KeyEvent event) {
	            if (event.getAction() == KeyEvent.ACTION_DOWN) {
	                if (keyCode == KeyEvent.KEYCODE_ENTER
	                        || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
	                    mFvpErrorConfirmDialog.dismiss();
						if(context instanceof ScanDialogActivity){
							((ScanDialogActivity) context).setScanComplete();
						}
	                    if(ScanThirdlyDialog.haveTargetRegionForUK()){
	                        new ScanThirdlyDialog(context, 2).show();
	                    }
	                    return true;
	                }
	            }
	            return false;
	        }
	    });
	    mFvpErrorConfirmDialog.getButtonYes().setOnKeyListener(new OnKeyListener() {
	        
	        @Override
	        public boolean onKey(View arg0, int keyCode, KeyEvent event) {
	            if(event.getAction() == KeyEvent.ACTION_DOWN) {
	                if (keyCode == KeyEvent.KEYCODE_ENTER
	                        || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
	                    mFvpErrorConfirmDialog.dismiss();
	                    startDvbtBroadcastScan();
	                    if(context instanceof ScanDialogActivity){
	                        ((ScanDialogActivity) context).finish();
	                    }
	                    return true;
	                }
	            }
	            return false;
	        }
	    });      
	}
	
	public boolean isFvpErrorDialogShowing() {
        return mFvpErrorConfirmDialog != null && mFvpErrorConfirmDialog.isShowing();
    }
	public void showBgScanAddedDialog(){
		if (mConfirmDialog != null && mConfirmDialog.isShowing()){
			return ;
		}
		mConfirmDialog = new TurnkeyCommDialog(mContext,7);
		mConfirmDialog.setMessage(mContext.getString(R.string.nav_channel_new_channels));
		mConfirmDialog.setButtonNoName(mContext.getString(R.string.menu_ok));
		mConfirmDialog.show();
		mConfirmDialog.setPositon(-20, 70);
		mConfirmDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
								 KeyEvent event) {
				int action = event.getAction();
				if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN) {
					mConfirmDialog.dismiss();
					return true;
				}
				return false;
			}
		});
		mConfirmDialog.getButtonNo().requestFocus();
		mConfirmDialog.getButtonNo().setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View arg0, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER
							|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						Log.d("ConfirmDialog", "on Button yes KEYCODE_ENTER KEYCODE_DPAD_CENTER");
						mConfirmDialog.dismiss();
						return true;
					}
				}
				return false;
			}
		});
	}

	public void dismissConfirmDialog(){
		if(mConfirmDialog != null){
			mConfirmDialog.dismiss();
		}
	}

	public void showConfirmDialog() {
		if (mConfirmDialog != null && mConfirmDialog.isShowing()){
			return ;
		}
	    mConfirmDialog = new TurnkeyCommDialog(mContext,
	                3);
		mConfirmDialog
				.setMessage(mContext.getString(R.string.tnt_hd_nw_chg));
		mConfirmDialog.setButtonYesName(mContext
				.getString(R.string.menu_setup_button_yes));
		mConfirmDialog.setButtonNoName(mContext
				.getString(R.string.menu_setup_button_no));
		mConfirmDialog.show();

		mConfirmDialog.getButtonNo().requestFocus();

		mConfirmDialog.setPositon(-20, 70);
		mConfirmDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				int action = event.getAction();
				if (keyCode == KeyEvent.KEYCODE_BACK
						&& action == KeyEvent.ACTION_DOWN) {
					mConfirmDialog.dismiss();
					return true;
				}
				return false;
			}
		});

		OnKeyListener yesListener = new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER
							|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						mConfirmDialog.dismiss();
						if(TVContent.getInstance(mContext).isTvInputBlock() || EditChannel.getInstance(mContext).getBlockChannelNumForSource() > 0
		                          /*|| TVContent.getInstance(mContext).getRatingEnable() == 1*/
								|| (TVContent.getInstance(mContext).getCurrentTunerMode() == 1 && (ScanContent.isZiggoUPCOp() || ScanContent.isVooOp()))){
		                      Log.d("DVBT_INTHD_ConfirmDialog", "show Pwd");
		                      com.mediatek.tv.ui.pindialog.PinDialogFragment dialog =
		                              com.mediatek.tv.ui.pindialog.PinDialogFragment.create(
		                                      com.mediatek.tv.ui.pindialog.PinDialogFragment.PIN_DIALOG_TYPE_START_SCAN);
		                          dialog.show(((Activity)mContext).getFragmentManager(), "PinDialogFragment");
		                  }else {
		                      startMenuFullScan();
                        }
						return true;
					}
				}
				return false;
			}
		};

		OnKeyListener noListener = new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER
							|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						mConfirmDialog.dismiss();
						if(!ScanContent.isCountryNZL()){
						    Log.d("ConfirmDialog", "setDisableNetworkChangeForCrnt");
						    MtkTvScan.getInstance().getScanDvbtInstance().setDisableNetworkChangeForCrnt(true);
						}
						return true;
					}
				}
				return false;
			}
		};
		mConfirmDialog.getButtonNo().setOnKeyListener(noListener);
		mConfirmDialog.getButtonYes().setOnKeyListener(yesListener);

		mConfirmDialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                if(!TurnkeyUiMainActivity.isUKCountry){
                    BannerView bannerView = (BannerView)ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
                    if (bannerView != null) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d("ConfirmDialog","bannerView.isVisible():"+bannerView.isVisible());
						bannerView.setVisibility(View.INVISIBLE);
                        bannerView.isKeyHandler(KeyMap.KEYCODE_MTKIR_INFO);
                    }
                }else{
                    UkBannerView bannerView = (UkBannerView)ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_BANNER);
                    if (bannerView != null) {
						com.mediatek.wwtv.tvcenter.util.MtkLog.d("ConfirmDialog","bannerView.isVisible():"+bannerView.isVisible());
						bannerView.setVisibility(View.VISIBLE);
                        bannerView.isKeyHandler(KeyMap.KEYCODE_MTKIR_INFO);
                    }
                }
                showTwinkle();
            }
        });
	}

	private void showTwinkle() {
	    mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
				TwinkleDialog.showTwinkle();
            }
        }, 2000);
	}

	private void startMenuFullScan() {
		showDVBTAutoOrUpdateScan();
	}
	
	private void startDvbtBroadcastScan(){
	    Intent intent = new Intent(mContext, ScanDialogActivity.class);
        intent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN_DVBT);
        intent.putExtra("broadcastScan", true);
        mContext.startActivity(intent);
	}

	private void startDvbtAdviseScan(){
	    Intent intent = new Intent(mContext, ScanDialogActivity.class);
        intent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN_DVBT);
        intent.putExtra("adviseScan", true);
        mContext.startActivity(intent);
	}

	/**
	 * for EU DVBT auto or update scan
	 * @param param can be null, not used
	 */
	public void showDVBTAutoOrUpdateScan() {
	    int tuneMode = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_SRC);
		if (tuneMode == 0) {
		    Intent intent = new Intent(mContext, ScanDialogActivity.class);
			intent.putExtra("need_full_screen",true);
			intent.putExtra("need_select_channel",true);
	        intent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN_DVBT);
	        mContext.startActivity(intent);
        }else if(tuneMode ==1){
            Intent intent = new Intent(mContext, ScanViewActivity.class);
            intent.putExtra("need_full_screen",true);
            intent.putExtra("need_select_channel",true);
			CableOperator operator = ScanContent.getCurrentOperator();
			if(operator.ordinal() != CableOperator.OTHER.ordinal()){
				intent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN_DVBC_OPERATOR + "#" + operator.ordinal());
			}else{
				intent.putExtra("ActionID", MenuConfigManager.TV_CHANNEL_SCAN_DVBC);
			}
            mContext.startActivity(intent);
        }



		com.mediatek.wwtv.tvcenter.util.MtkLog.d("showDTVScan(MtkTvScanBase param))");
	}

	public void showItalyNoChannelsDialog(Context context) {

		mConfirmDialog = new TurnkeyCommDialog(context,
				3);
		mConfirmDialog
				.setMessage(context.getString(R.string.dvbt_ita_no_channels));
		mConfirmDialog.setButtonYesName(context
				.getString(R.string.menu_ok));
		mConfirmDialog.setButtonNoName(context
				.getString(R.string.menu_cancel));
		mConfirmDialog.show();

		mConfirmDialog.getButtonYes().requestFocus();

		mConfirmDialog.setPositon(-20, 70);
		mConfirmDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
								 KeyEvent event) {
				int action = event.getAction();
				if (keyCode == KeyEvent.KEYCODE_BACK
						&& action == KeyEvent.ACTION_DOWN) {
					mConfirmDialog.dismiss();
//                  startMenuFullScan();
					return true;
				}
				return false;
			}
		});

		OnKeyListener yesListener = new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER
							|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						mConfirmDialog.dismiss();
						MtkTvConfig.getInstance().setConfigValue(MtkTvConfigTypeBase.CFG_BS_BS_TERRESTRIAL_BRDCSTER, 1);
						((ScanDialogActivity)context).continueScan();
						return true;
					}
				}
				return false;
			}
		};

		OnKeyListener noListener = new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER
							|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						mConfirmDialog.dismiss();
						return true;
					}
				}
				return false;
			}
		};
		mConfirmDialog.getButtonNo().setOnKeyListener(noListener);
		mConfirmDialog.getButtonYes().setOnKeyListener(yesListener);
	}

	@Override
	public String toString() {
	    return "ConfirmDialog"; // To fix pmd issue
	}
}
