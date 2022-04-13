/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tv.ui.sidepanel.parentalcontrols;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.TextView;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.twoworlds.tv.model.MtkTvOpenVCHIPPara;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.setting.widget.view.LiveTVDialog;
import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.wwtv.tvcenter.R;
import com.android.tv.ui.sidepanel.ActionItem;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.OpenVchipSubMenu;
import com.android.tv.ui.sidepanel.SideFragment;
import java.util.ArrayList;
import java.util.List;

public class OpenVchipRegionFragment extends SideFragment {
    public static final String TAG = "OpenVchipRegionFragment";
    private Context mContext;
    private TVContent mTV;
    private SaveValue mSaveValue;
    private LiveTVDialog factroyCofirm = null;
    private ProgressDialog pdialog = null;
    private int mRegionNum = 0;
    private static final int MESSAGE_DISMISS_DIALOG = 0x001;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_DISMISS_DIALOG) {
                if (pdialog != null) {
                    pdialog.dismiss();
                    getData();
                }
            }
                
        };
    };;
    private final SideFragmentListener mSideFragmentListener =
            new SideFragmentListener() {
                @Override
                public void onSideFragmentViewDestroyed() {
                    notifyDataSetChanged();
                }
            };
    public static String getDescription(Activity tvActivity) {
        return RatingsFragment.getDescription(tvActivity);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.parental_open_vchip_region);
    }

    @Override
    protected List<Item> getItemList() {
        
        mContext = getActivity();
        mTV = TVContent.getInstance(mContext);
        mSaveValue = SaveValue.getInstance(mContext);
        mTV.getOpenVCHIPPara().setOpenVCHIPParaType(
                MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_RGN_NUM);
        mRegionNum = mTV.getOpenVchip().getRegionNum();
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < mRegionNum; i++) {
            mTV.getOpenVCHIPPara().setOpenVCHIPParaType(
                    MtkTvOpenVCHIPPara.OPEN_VCHIP_KEY_GET_RGN_TEXT);
            mTV.getOpenVCHIPPara().setRegionIndex(i);
            final int regionIndex=i;
            items.add(
//                new OpenVchipRegion(i+"")
                new OpenVchipSubMenu(
                        mTV.getOpenVchip().getRegionText(),
                        OpenVchipDimFragment.getDescription(getMainActivity()),
                        getMainActivity().getSideFragmentManager(),
                        0,i) {
                    @Override
                    protected SideFragment getFragment() {
                        SideFragment fragment = new OpenVchipDimFragment();
                        fragment.setListener(mSideFragmentListener);
                        return fragment;
                    }
                    @Override
                            protected void onSelected() {
                                // TODO Auto-generated method stub
                                mSaveValue.saveValue("OpenVchipRegionIndex"
                                        , regionIndex);
                                super.onSelected();
                            }
                }
                );
        }
        items.add(
                new ResetAllItem());
        return items;
    }
    private class ResetAllItem extends ActionItem {
        private TextView mTextView;
        public ResetAllItem() {
            super(null);
        }

        @Override
        protected void onBind(View view) {
            super.onBind(view);
            mTextView = (TextView) view.findViewById(R.id.title);
            this.setEnabled(mRegionNum != 0);
        }

        @Override
        protected void onUpdate() {
            super.onUpdate();
            mTextView.setText(getString(R.string.parental_rrt5_reset));
        }

        @Override
        protected void onUnbind() {
            super.onUnbind();
            mTextView = null;
        }

        @Override
        protected void onSelected() {
            factroyCofirm = new LiveTVDialog(mContext, 3);
            factroyCofirm.setMessage(mContext.getString(
                R.string.parental_rrt5_reset));
            factroyCofirm.setButtonYesName(mContext.getString(
                R.string.menu_ok));
            factroyCofirm.setButtonNoName(mContext.getString(
                R.string.menu_cancel));

            factroyCofirm.show();
            factroyCofirm.setPositon(-20, 70);
            factroyCofirm.getButtonNo().requestFocus();
            OnKeyListener listener = new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_ENTER ||
                            keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                            || keyCode == KeyMap.KEYCODE_MTKIR_RED) {
                            if (v.getId() == factroyCofirm.getButtonYes().getId()) {
                                factroyCofirm.dismiss();
                                pdialog = ProgressDialog.show(mContext,
                                    "Reset RRT5", "Reseting please wait...", false, false);
                                mTV.resetRRT5();
                                mHandler.sendEmptyMessageDelayed(MESSAGE_DISMISS_DIALOG,1000);
                                return true;
                            } else if (v.getId() == factroyCofirm.getButtonNo().getId()) {
                                factroyCofirm.dismiss();
                                return true;
                            }
                        }
                    }
                    return false;
                }
            };

            factroyCofirm.getButtonNo().setOnKeyListener(listener);
            factroyCofirm.getButtonYes().setOnKeyListener(listener);
        }

        @Override
        protected void onFocused() {
            super.onFocused();
            Log.d(TAG, "onFocused");
        }
    }
}
