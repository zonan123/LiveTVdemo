package com.mediatek.wwtv.tvcenter.dvr.ui;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateBase;
import com.mediatek.wwtv.tvcenter.dvr.manager.Util;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

public class CommonConfirmDialog extends CommonDialog {

    private float wScale = 0.3f;
    private float hScale = 0.35f;

    public StateBase mState;

    private TextView mTV1;
    private TextView mTV2;

    private Button positiveBtn;
    private Button negativeBtn;

    public CommonConfirmDialog(Context context, StateBase state) {
        super(context, R.layout.pvr_tshift_confirmdialog);

        this.getWindow().setLayout(
                (int) (Util.getTVWidth() * wScale),
                (int) (Util.getTVHeight() * hScale));
        this.mState = state;

        setCancelable(true);
        initView2();
    }

    private void initView2() {
        mTV1 = (TextView) findViewById(R.id.diskop_title_line1);
        mTV1.setText("");

        mTV2 = (TextView) findViewById(R.id.diskop_title_line2);
        mTV2.setText("");

        positiveBtn = ((Button) findViewById(R.id.confirm_btn_yes));
        negativeBtn = ((Button) findViewById(R.id.confirm_btn_no));
    }

    public void setPositiveButton(Button.OnClickListener listener) {
        positiveBtn.setOnClickListener(listener);
    }

    public void setNegativeButton(Button.OnClickListener listener) {
        negativeBtn.setOnClickListener(listener);
    }

    /*
     * setTitle
     */
    public void setTitle(String line1, String line2) {
        mTV1.setText(line1);
        mTV2.setText(line2);
    }

    /*
     * onKeyDown
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyMap.KEYCODE_DPAD_CENTER) {
            onClick(getCurrentFocus());
        }
        return super.onKeyDown(keyCode, event);
    }
}
