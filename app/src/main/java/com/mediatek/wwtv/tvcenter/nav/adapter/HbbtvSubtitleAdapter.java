package com.mediatek.wwtv.tvcenter.nav.adapter;

import java.util.List;

import android.content.Context;
//import android.text.TextUtils;
import android.view.LayoutInflater;
//import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
//import android.view.View.OnTouchListener;
import android.widget.BaseAdapter;
//import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.R;
//import com.mediatek.wwtv.tvcenter.nav.input.AbstractInput;
//import com.mediatek.wwtv.tvcenter.nav.input.InputUtil;
//import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
//import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

//import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

public class HbbtvSubtitleAdapter extends BaseAdapter {
    private static final String TAG = "HbbtvSubtitleAdapter";
    /*
     * context
     */
    //private Context mContext;
    private LayoutInflater mInflater;
    private List<String> title;
    private List<String> subLanguage;
    public HbbtvSubtitleAdapter(Context context, List<String> title,
                                    List<String> subLanguage) {
        Context mContext = context;
        mInflater = LayoutInflater.from(mContext);
        this.title = title;
        this.subLanguage = subLanguage;

    }

    public void updateList(List<String> title,
                           List<String> subLanguage){
        this.title = title;
        this.subLanguage = subLanguage;

    }
    @Override
    public int getCount() {
        return title.size();
    }

    @Override
    public String getItem(int position) {
        return subLanguage.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder hodler;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.subtitle_hbbtv_item, null);
            hodler = new ViewHolder();
            hodler.subtitlView = (TextView) convertView
                    .findViewById(R.id.subtitle_name);
            hodler.mTextView = (TextView) convertView
                    .findViewById(R.id.subtitle_language_name);
            convertView.setTag(hodler);
        } else {
            hodler = (ViewHolder) convertView.getTag();
        }
        hodler.subtitlView.setText(title.get(position));
        hodler.mTextView.setText(getItem(position));

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- inputSourceHardwareId = ");

        return convertView;
    }

    private class ViewHolder {
        TextView subtitlView;
        TextView mTextView;
    }
}