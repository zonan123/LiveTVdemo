package com.mediatek.wwtv.setting.preferences;

import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;

import com.mediatek.wwtv.tvcenter.R;


public class DialogPreference extends Preference {
    static final String TAG = "DialogPreference";

    private Dialog mDialog;

    public DialogPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DialogPreference(Context context, AttributeSet attrs) {
		this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.preferenceStyle,
				android.R.attr.preferenceStyle));
    }

    public DialogPreference(Context context) {
        this(context, null);
    }

    public void setDialog(Dialog dialog) {
        mDialog = dialog;
    }

    public Dialog getDialog() {
        return mDialog;
    }

    @Override
    protected void onClick() {
        if(mDialog != null) {
            mDialog.show();
        }
    }
}

