package com.android.tv.ui.sidepanel;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.tv.TvTrackInfo;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClosedCaptionFragment extends SideFragment {
    private static final String TAG = "ClosedCaptionFragment";
    /*private static final String TRACKER_LABEL = "closed caption";
    private boolean mResetClosedCaption;
    private int mClosedCaptionOption;
    private String mClosedCaptionLanguage;
    private String mClosedCaptionTrackId;
    private ClosedCaptionOptionItem mSelectedItem;*/
    private int mSelectedPosition = 0;

    public ClosedCaptionFragment() {
        super(KeyEvent.KEYCODE_CAPTIONS/*, KeyEvent.KEYCODE_S*/);
    }

    @Override
    protected String getTitle() {
        if(MarketRegionInfo.REGION_US == MarketRegionInfo.getCurrentMarketRegion() ||
           MarketRegionInfo.REGION_SA == MarketRegionInfo.getCurrentMarketRegion()) {//cc default
            return getString(R.string.side_panel_title_closed_caption);
        } else {
            return getString(R.string.menu_setup_subtitle);
        }
    }

    protected void refreshUI(){
        setSelectedPosition(mSelectedPosition);
    }

    @Override
    protected List<Item> getItemList() {
        List<Item> items = new ArrayList<>();
        //mSelectedItem = null;
        TurnkeyUiMainActivity.getInstance().getTvView().setCaptionEnabled(true);
        String currentCCId = TurnkeyUiMainActivity.getInstance().getTvView()
                .getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE);
        List<TvTrackInfo> tracks = TurnkeyUiMainActivity.getInstance()
                .getTvView().getTracks(TvTrackInfo.TYPE_SUBTITLE);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "currentCCId="+currentCCId);
        if (tracks != null && !tracks.isEmpty()) {
            String selectedTrackId = null;
            if (null != currentCCId) {
                int i;
                for (i = 0; i < tracks.size(); i++) {
                    if (currentCCId.equals(tracks.get(i).getId())) {
                        break;
                    }
                }
                selectedTrackId = tracks.get(
                        i % tracks.size()).getId();
            }else {
                selectedTrackId = null;
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "selectedTrackId="+selectedTrackId);
            ClosedCaptionOptionItem item = new ClosedCaptionOptionItem(null, null);
            items.add(item);
            if (selectedTrackId == null) {
                //mSelectedItem = item;
                item.setChecked(true);
                mSelectedPosition = 0;
                //setSelectedPosition(0);
            }
            for (int i = 0; i < tracks.size(); i++) {
                item = new ClosedCaptionOptionItem(tracks.get(i), i);
                if (TextUtils.equals(selectedTrackId, tracks.get(i).getId())) {
                    //mSelectedItem = item;
                    item.setChecked(true);
                    mSelectedPosition = i + 1;
                    //setSelectedPosition(i + 1);
                }
                items.add(item);
            }
        }
        items.add(
                new ActionItem(
                        getString(R.string.closed_caption_system_settings),
                        getString(R.string.closed_caption_system_settings_description)) {
                    @Override
                    protected void onSelected() {
                        //Intent intent = new Intent("android.settings.SETTINGS");
                        /*intent.putExtra(
                            com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE,
                            com.mediatek.wwtv.tvcenter.util.EventHelper.MTK_EVENT_EXTRA_SUB_TYPE_CAPTIONS_SRC);*/
                        Intent intent = new Intent("android.settings.CAPTIONING_SETTINGS");
                        try {
                            getMainActivity().startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(
                                    getMainActivity(),
                                    getString(R.string.msg_unable_to_start_system_captioning_settings),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    protected void onFocused() {
                        super.onFocused();
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onFocused");
                        /*if (mSelectedItem != null) {
                            getMainActivity()
                            .selectSubtitleTrack(
                                    mSelectedItem.mOption, mSelectedItem.mTrackId);
                        }*/
                    }
                });
        return items;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private String getLabel(TvTrackInfo track, Integer trackIndex) {
        if (track == null) {
            return getString(R.string.closed_caption_option_item_off);
        } else if (track.getLanguage() != null) {
            return new Locale(track.getLanguage()).getDisplayName();
        }
        return getString(R.string.closed_caption_unknown_language, trackIndex + 1);
    }

    private class ClosedCaptionOptionItem extends RadioButtonItem {
        private final int mOption;
        private final String mTrackId;

        private ClosedCaptionOptionItem(TvTrackInfo track, Integer trackIndex) {
            super(getLabel(track, trackIndex));
            if (track == null) {
                mOption = 0;
                mTrackId = null;
            } else {
                mOption = 1;
                mTrackId = track.getId();
            }
        }

        @Override
        protected void onSelected() {
            super.onSelected();
            //mSelectedItem = this;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onSelected mTrackId="+mTrackId);
            TurnkeyUiMainActivity.getInstance().getTvView()
            .selectTrack(TvTrackInfo.TYPE_SUBTITLE, mTrackId);
            getActivity().finish();
        }

    }

    protected int getFragmentLayoutResourceId() {
        return R.layout.multi_audio_fragment;
    }
}
