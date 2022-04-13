package com.mediatek.wwtv.tvcenter.nav.view;

import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.mediatek.wwtv.rxbus.MainActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.commonview.TurnkeyCommDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

/**
 * Only For DVBT inactive channels removes tip
 */
public final class DvbtInactiveChannelConfirmDialog {

	private Context mContext;
	
	private TurnkeyCommDialog confirmDialog ;
	
	private static DvbtInactiveChannelConfirmDialog mDialog;
	
	public static synchronized DvbtInactiveChannelConfirmDialog getInstance(Context context){
	    if(mDialog == null){
	        mDialog = new DvbtInactiveChannelConfirmDialog(context);
	    }
	    return mDialog;
	}

	private DvbtInactiveChannelConfirmDialog(Context mContext) {
		super();
		this.mContext = mContext;

		RxBus.instance.onFirstEvent(MainActivityDestroyEvent.class)
				.doOnSuccess(it -> DvbtInactiveChannelConfirmDialog.free())
				.subscribe();
	}

	private static synchronized void free(){
		mDialog = null;
	}

	public void showConfirmDialog() {
		TwinkleDialog.hideTwinkle();
	    if(confirmDialog == null){
	        confirmDialog = new TurnkeyCommDialog(mContext, 3);
	    }
		confirmDialog.setMessage(mContext.getString(R.string.dvbt_inactivechannel_nw_chg));
		confirmDialog.setButtonYesName(mContext.getString(R.string.menu_setup_button_yes));
		confirmDialog.setButtonNoName(mContext.getString(R.string.menu_setup_button_no));
		confirmDialog.show();
		confirmDialog.getButtonNo().requestFocus();
		confirmDialog.setPositon(-20, 70);
		confirmDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				int action = event.getAction();
				if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN) {
					confirmDialog.dismiss();
					TwinkleDialog.showTwinkle();
					return true;
				}
				return false;
			}
		});
		/*((TurnkeyUiMainActivity)mContext).getHandler().postDelayed(new Runnable() {
            
            @Override
            public void run() {
                confirmDialog.dismiss();
            }
        }, 5000);
		showInactive();*/
		confirmDialog.getButtonNo().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
                CommonIntegration.getInstance().clearMaskForInactiveChannels();
                TwinkleDialog.showTwinkle();
            }
        });
		confirmDialog.getButtonYes().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
                CommonIntegration.getInstance().deleteAllInactiveChannels();
				TwinkleDialog.showTwinkle();
                //showInactive();
            }
        });
	}


//	private void showInactive() {
//		InactiveChannelDialog dialog = new InactiveChannelDialog(mContext);
//		dialog.showInactiveChannels();
//		dialog.deleteAllInactiveChannels();
//	}
	
	public boolean isShowing(){
	    return confirmDialog != null&& confirmDialog.isShowing();
	}
}
