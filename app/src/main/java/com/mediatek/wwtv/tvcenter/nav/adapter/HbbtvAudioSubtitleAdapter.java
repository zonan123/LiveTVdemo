package com.mediatek.wwtv.tvcenter.nav.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
//import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
//import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
//import android.view.View.OnTouchListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.R;
//import com.mediatek.wwtv.tvcenter.nav.input.AbstractInput;
//import com.mediatek.wwtv.tvcenter.nav.input.InputUtil;
//import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
//import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

//import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

public class HbbtvAudioSubtitleAdapter extends BaseAdapter {
    private static final String TAG = "HbbtvAudioSubtitleAdapter";
    /*
     * context
     */
    //private Context mContext;
    private LayoutInflater mInflater;
    private List<Integer> mSourceList;
    private List<String> mConflictInputsList;
    // private TVInputManager tvInputSource;
    private Drawable mSourceSelectedIcon;
    private Drawable mSourceUnSelectedIcon;


    public HbbtvAudioSubtitleAdapter(Context context, List<Integer> mSourceList,
                             List<String> mConflictList

            , Drawable mSourceSelectedIcon, Drawable mSourceUnSelectedIcon,
                             Drawable mConflictIcon) {
        Context mContext = context;
        mInflater = LayoutInflater.from(mContext);
        this.mSourceList = mSourceList;
        this.mConflictInputsList = mConflictList;
        // this.tvInputSource = tvInputSource;
        this.mSourceSelectedIcon = mSourceSelectedIcon;
        this.mSourceUnSelectedIcon = mSourceUnSelectedIcon;
        //Drawable mConflictIcons = mConflictIcon;
        TypedValue typedValue = new TypedValue();
        mContext.getResources().getValue(R.dimen.nav_source_list_dialog_item_height,typedValue ,true);
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG,""+mConflictIcon.toString());
    }

    public void updateList(List<Integer> mSourceList,
                           List<String> mConflictList){
        this.mSourceList = mSourceList;
        this.mConflictInputsList = mConflictList;
    }
    @Override
    public int getCount() {
        return mSourceList.size();
    }

    @Override
    public String getItem(int position) {
//        AbstractInput input = InputUtil.getInput(mSourceList.get(position));
//        String customLabel = input.getCustomSourceName(mContext);
//        if(TextUtils.isEmpty(customLabel) || TextUtils.equals(customLabel, "null")) {
//            return input.getSourceName(mContext);
//        }
        return mConflictInputsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder hodler;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.nav_source_item, null);
            hodler = new ViewHolder();
            hodler.mTextView = (TextView) convertView
                    .findViewById(R.id.nav_source_list_item_tv);
            hodler.mIcon = (ImageView) convertView
                    .findViewById(R.id.nav_source_list_item_icon);
            convertView.setTag(hodler);
        } else {
            hodler = (ViewHolder) convertView.getTag();
        }
        hodler.mTextView.setText(getItem(position));
        int inputSourceHardwareId = 0;//InputSourceManager.getInstance().getCurrentInputSourceHardwareId();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "--- inputSourceHardwareId = "+ inputSourceHardwareId);
        if (mSourceList.get(position) == 1) {
            hodler.mIcon.setImageDrawable(mSourceSelectedIcon);
        } else{
            hodler.mIcon.setImageDrawable(mSourceUnSelectedIcon);
        }
        return convertView;
    }

    private class ViewHolder {
        ImageView mIcon;
        TextView mTextView;
    }
}