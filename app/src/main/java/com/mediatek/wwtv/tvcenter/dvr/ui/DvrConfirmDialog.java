package com.mediatek.wwtv.tvcenter.dvr.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.manager.DvrManager;
import com.mediatek.wwtv.tvcenter.dvr.ui.DvrDialog.DialogDismissRecevier;
import com.mediatek.wwtv.tvcenter.dvr.ui.DvrDialog.PvrReceiver;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;

public class DvrConfirmDialog extends android.app.Dialog  implements View.OnClickListener{
	
    public static final int DVR_CONFIRM_TYPE_SCHEDULE = 1 ;
    
    public final static String DISSMISS_DIALOG = "com.mediatek.dialog.dismiss";
    
    private Context mContext;
    
    //private TextView titleView, textView;
    //private Button buttonYes, buttonNo;
    
	private String schedulePromt ;// words showing on dialog
	private String scheduleTitle ;// dialog title
	private int confirmType = 0 ;// UI type 
	

	

	public DvrConfirmDialog(Context context) {
		super(context,R.style.Theme_pvr_Dialog);
	}
	
	public DvrConfirmDialog (Context context ,String prompt,String title ,int confirmType){
		super(context,R.style.Theme_pvr_Dialog);
		this.mContext = context;
		this.schedulePromt = prompt;
		this.scheduleTitle = title;
		this.confirmType = confirmType ;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registReciver();
		setContentView(R.layout.pvr_dialog_one_button);
		initView(confirmType);
		setPositon(0, 0);
	}
	
	private void registReciver (){
		DialogDismissRecevier dreceiver = new DialogDismissRecevier();
        IntentFilter inf = new IntentFilter("com.mtk.dialog.dismiss");
        mContext.registerReceiver(dreceiver, inf);
        
        PvrReceiver receiver = new PvrReceiver();
        IntentFilter filter = new IntentFilter(DISSMISS_DIALOG);
        mContext.registerReceiver(receiver, filter);
	}
	private void initView(int type){
	    android.util.Log.d("DvrConfirmDialog", "type=="+type);
		TextView titleView = (TextView) findViewById(R.id.comm_dialog_title);
        TextView textView = (TextView) findViewById(R.id.comm_dialog_text);
        Button buttonYes = (Button) findViewById(R.id.comm_dialog_buttonYes);
        Button buttonNo = (Button) findViewById(R.id.comm_dialog_buttonNo);
        buttonNo.setVisibility(View.VISIBLE);
        
        textView.setText(schedulePromt);
        titleView.setText(scheduleTitle);
        
        buttonNo.setOnClickListener(this);
        buttonYes.setOnClickListener(this);
	}

    /**
     * Set the dialog's position relative to the (0,0)
     */
    public void setPositon(int xoff, int yoff) {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = xoff;
        lp.y = yoff;
        lp.width = 700;
        lp.height = 400;
        window.setAttributes(lp);
    }
	
	
    class DialogDismissRecevier extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DvrConfirmDialog.this.dismiss();
        }
    }
    class PvrReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals(DISSMISS_DIALOG)) { 
				DvrConfirmDialog.this.dismiss();
             }
        }
    }
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.comm_dialog_buttonYes:
			TurnkeyUiMainActivity.resumeTurnkeyActivity(mContext);
			if (confirmType == DVR_CONFIRM_TYPE_SCHEDULE) {
				DvrManager.getInstance().handleRecordNTF(true);
			}
			break;
		case R.id.comm_dialog_buttonNo:
			if (confirmType == DVR_CONFIRM_TYPE_SCHEDULE) {
				DvrManager.getInstance().handleRecordNTF(false);
			}
			break;

		default:
			break;
		}
		dismiss();
	}
    
}
