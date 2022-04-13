package com.mediatek.wwtv.tvcenter.epg.us;

import android.content.Context;
import android.view.KeyEvent;

import com.mediatek.wwtv.tvcenter.commonview.LoadingDialog;

public class EPGProgressDialog extends LoadingDialog{

    public EPGProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public EPGProgressDialog(Context mContext){
        super(mContext);
    }

    @Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyUp(keyCode, event);
	}

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }


}
