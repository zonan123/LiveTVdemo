package com.mediatek.wwtv.setting.preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

//import android.support.v4.content.res.TypedArrayUtils;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
//import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceViewHolder;

import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.setting.util.Util;



public class PicturePreference extends Preference{
    static final String TAG = "ProgressPreference";

    private TextView mTextView;
    private ImageView mImageView;

    public PicturePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.picture_preference);
    }

    public PicturePreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.preferenceStyle,
                android.R.attr.preferenceStyle));
    }

    public PicturePreference(Context context) {
        this(context, null);
    }

   
    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onBindViewHolder.");

        mTextView =(TextView) holder.findViewById(R.id.title_tx);
    }
}
