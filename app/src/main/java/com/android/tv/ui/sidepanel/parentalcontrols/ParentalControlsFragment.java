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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.setting.util.MenuDataHelper;

import com.mediatek.tv.ui.pindialog.PinDialogFragment;
import com.android.tv.ui.sidepanel.ActionItem;
import com.android.tv.ui.sidepanel.Item;
import com.android.tv.ui.sidepanel.SideFragment;
import com.android.tv.ui.sidepanel.SideFragmentManager;
import com.android.tv.ui.sidepanel.SubMenuItem;
import com.mediatek.wwtv.tvcenter.nav.input.InputUtil;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import java.util.ArrayList;
import java.util.List;

public class ParentalControlsFragment extends SideFragment {
    private static final String TAG = "ParentalControlsFragment";
    private boolean isTVOrCTSSource=true;//TV Or STC Source
    private boolean isComponentOrComposite = true;
    private EditChannel mEidtChannel; 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        mEidtChannel = EditChannel.getInstance(getActivity().getApplicationContext());
        super.onCreate(savedInstanceState);
    }
    public ParentalControlsFragment() {
        super();
        isTVOrCTSSource=isTVOrCTSSource();
        isComponentOrComposite = isComponentSource();
    }
    private boolean isTVOrCTSSource() {
 		String path = CommonIntegration.getInstance().getCurrentFocus();
 		boolean isCurrentTvSource=InputSourceManager.getInstance().isCurrentTvSource(path);
 		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCurrentTvSourc="+isCurrentTvSource);
 		return isCurrentTvSource;
 	}

    private boolean isComponentSource() {
        int inputID = InputSourceManager.getInstance().getCurrentInputSourceHardwareId();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isComponentSource inputID = "+inputID);
        return InputUtil.getInput(inputID).isComponent() || InputUtil.getInput(inputID).isComposite();
    }

    /*private boolean isCTSSource() {
 		String path = CommonIntegration.getInstance().getCurrentFocus();
 		boolean isCurrentTvSource = InputSourceManager.getInstance()
 				.isCurrentTvSource(path);
 		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isCTSSource isCurrentTvSource = "+isCurrentTvSource);
 		return isCurrentTvSource;
 	}*/
    private final SideFragmentListener mSideFragmentListener =
            new SideFragmentListener() {
                @Override
                public void onSideFragmentViewDestroyed() {
                    notifyDataSetChanged();
                }
            };

    @Override
    protected String getTitle() {
        return getString(R.string.menu_channel_parental_controls);
    }

    //@Override
    //public String getTrackerLabel() {
    //    return TRACKER_LABEL;
    //}
    @Override
    protected List<Item> getItemList() {
        List<Item> items = new ArrayList<>();
        if(isTVOrCTSSource || isComponentOrComposite){
            if(!isComponentOrComposite){
                SubMenuItem channelBlockItem=new ChannelBlockItem(getString(R.string.option_channels_locked),"",mSideFragmentManager); 
                channelBlockItem.setEnabled(MenuDataHelper.getInstance(getActivity().getApplicationContext()).getTVChannelList().size()>0);
                items.add(channelBlockItem);
            }
            if (!CommonIntegration.isCNRegion()) {
                SubMenuItem programBlockItem=new ProgramBlockItem(
                        getString(R.string.option_program_restrictions),  
                        ProgramRestrictionsFragment.getDescription(getMainActivity()),
                        mSideFragmentManager);
                items.add(programBlockItem);
            }
        	//PA Region & ATV show program_restrictions gray.
        	/*if(CommonIntegration.isEUPARegion()&&CommonIntegration.getInstance().isCurrentSourceATVforEuPA()){
        		programBlockItem.setEnabled(false);
        	} */
        	
        	if(CommonIntegration.isSARegion()){
        		SubMenuItem channelScheduleBlockItem=new ChannelScheduleBlockItem(getString(R.string.menu_parental_channel_schedule_block),getMainActivity().getSideFragmentManager());
        		items.add(channelScheduleBlockItem);
        	}
        }
        items.add(
                new SubMenuItem(
                        getString(R.string.option_inputs_locked),
                        mSideFragmentManager) {
                    @Override
                    protected SideFragment getFragment() {
                        SideFragment fragment = new InputsBlockedFragment();
                        fragment.setListener(mSideFragmentListener);
                        return fragment;
                    }
                });
        items.add(
                new ActionItem(getString(R.string.option_change_pin)) {
                    @Override
                    protected void onSelected() {
                        PinDialogFragment fragment =
                                PinDialogFragment.create(PinDialogFragment.PIN_DIALOG_TYPE_NEW_PIN);

                        getMainActivity().hide();
                        fragment.show(getMainActivity().getFragmentManager(), "PinDialogFragment");
                    }
                });
        return items;
    }
    
    
    class ChannelScheduleBlockItem extends SubMenuItem{
    	public ChannelScheduleBlockItem(String title,
				SideFragmentManager fragmentManager) {
			super(title, fragmentManager);
			// TODO Auto-generated constructor stub
		}
    	
            @Override
            protected SideFragment getFragment() {
                SideFragment fragment = new ChannelScheduleBlockedFragment();
                fragment.setListener(mSideFragmentListener);
                return fragment;
            }
     }

    class ProgramBlockItem extends SubMenuItem{
    	public ProgramBlockItem(String title,
				SideFragmentManager fragmentManager) {
			super(title, fragmentManager);
			// TODO Auto-generated constructor stub
		}
    	
    	public ProgramBlockItem(String title, String description, SideFragmentManager fragmentManager) {
			super(title,description, fragmentManager);
			// TODO Auto-generated constructor stub
		}
            @Override
            protected SideFragment getFragment() {
                SideFragment fragment = new ProgramRestrictionsFragment();
                fragment.setListener(mSideFragmentListener);
                return fragment;
            }
     }
    
    
    
    class ChannelBlockItem extends SubMenuItem{
    	TextView mDescriptionView;
		public ChannelBlockItem(String title,
				SideFragmentManager fragmentManager) {
			super(title, fragmentManager);
			// TODO Auto-generated constructor stub
		}
		public ChannelBlockItem(String title, String description, SideFragmentManager fragmentManager) {
			super(title,description, fragmentManager);
			// TODO Auto-generated constructor stub
		}

		@Override
        protected SideFragment getFragment() {
            SideFragment fragment = new ChannelsBlockedFragment();
            fragment.setListener(mSideFragmentListener);
            return fragment;//fragment;
        }

        @Override
        protected void onBind(View view) {
            super.onBind(view);
            mDescriptionView = (TextView) view.findViewById(R.id.description);
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onUpdate() {
            super.onUpdate();
            int lockedAndBrowsableChannelCount = mEidtChannel.getBlockChannelNumForSource();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "lockedAndBrowsableChannelCount:" + lockedAndBrowsableChannelCount);
            if (lockedAndBrowsableChannelCount > 0) {
                mDescriptionView.setText(
                        Integer.toString(lockedAndBrowsableChannelCount));
            } else {
                mDescriptionView.setText(
                        getMainActivity().getString(R.string.option_no_locked_channel));
            }
        }

        @Override
        protected void onUnbind() {
            super.onUnbind();
            mDescriptionView = null;
        }
    	
    }
}
