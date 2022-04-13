
package com.mediatek.wwtv.setting.fragments;


import android.app.Fragment;
import android.content.Context;



import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;




import android.widget.RadioButton;


import com.mediatek.twoworlds.tv.model.MtkTvUSTvRatingSettingInfoBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.util.RatingConstHelper;
import com.mediatek.wwtv.setting.util.TVContent;

import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.setting.widget.detailui.Action.DataType;

import android.media.tv.TvInputManager;
import android.media.tv.TvContentRating;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RatingFragment extends Fragment {

    private Context context;

    private RadioButton vRB_Y_A;
    private RadioButton vRB_Y7_A;
    private RadioButton vRB_G_A;
    private RadioButton vRB_PG_A;
    private RadioButton vRB_14_A;
    private RadioButton vRB_MA_A;
    private RadioButton vRB_PG_D;
    private RadioButton vRB_14_D;
    private RadioButton vRB_PG_L;
    private RadioButton vRB_14_L;
    private RadioButton vRB_MA_L;
    private RadioButton vRB_PG_S;
    private RadioButton vRB_14_S;
    private RadioButton vRB_MA_S;
    private RadioButton vRB_PG_V;
    private RadioButton vRB_14_V;
    private RadioButton vRB_MA_V;
    private RadioButton vRB_Y7_FV;

    private boolean vRB_Y_A_Check;
    private boolean vRB_Y7_A_Check;
    private boolean vRB_G_A_Check;
    private boolean vRB_PG_A_Check;
    private boolean vRB_14_A_Check;
    private boolean vRB_MA_A_Check;
    private boolean vRB_PG_D_Check;
    private boolean vRB_14_D_Check;
    private boolean vRB_PG_L_Check;
    private boolean vRB_14_L_Check;
    private boolean vRB_MA_L_Check;
    private boolean vRB_PG_S_Check;
    private boolean vRB_14_S_Check;
    private boolean vRB_MA_S_Check;
    private boolean vRB_PG_V_Check;
    private boolean vRB_14_V_Check;
    private boolean vRB_MA_V_Check;
    private boolean vRB_Y7_FV_Check;

    private List<RadioButton> mGroup = new ArrayList<RadioButton>();
    TVContent mTV;
    TvInputManager mTvInputManager ;
    //a rating list with whole sub ratings
    List<TvContentRating> mWholeRatingList = new ArrayList<TvContentRating>();
    //a rating list with part sub ratings
    List<String> subRatingsList = new ArrayList<String>();


    private boolean isPositionView = false;
    private ViewGroup mRootView;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
	    mTvInputManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);

	}

	public void setAction(Action action){
        Action mAction;
		mAction = action;
		if(action.mDataType == DataType.POSITIONVIEW){
			isPositionView = true;
		}
		 com.mediatek.wwtv.tvcenter.util.MtkLog.d("Rating", "mAction="+mAction+",isPositionView="+isPositionView);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = (ViewGroup) inflater.inflate(R.layout.menu_rating_view,
				null);
		init();
		setListener();
		 if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING)){
         	initRatingFromTIF();
         }else{
         	initRatingSetting();
         }
		return mRootView;
	}
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if(!vRB_Y_A.hasFocus()){
			vRB_Y_A.requestFocus();
		}
	}

    private void setListener() {
        for (RadioButton radioButton : mGroup){
            radioButton.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_DPAD_CENTER:
                            case KeyEvent.KEYCODE_ENTER:
                                if (((RadioButton) v).isChecked()) {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("Rating", "setChecked(false)");
                                    ((RadioButton) v).setChecked(false);
                                    setRatingInfo(v,false);
                                    if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING)){
                                        initRatingSettingForTIF();
                                        generateContentRatingPlus();
                                    }else{
                                        initRatingSetting();
                                    }

                                } else {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("Rating", "setChecked(true)");
                                    ((RadioButton) v).setChecked(true);
                                    setRatingInfo(v,true);
                                    if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING)){
                                        initRatingSettingForTIF();
                                        generateContentRatingPlus();
                                    }else{
                                        initRatingSetting();
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                    return true;
                }
            });
        }
    }

    public void init() {

        mTV = TVContent.getInstance(context);
        vRB_Y_A = (RadioButton) mRootView.findViewById(R.id.radioButton1);

        vRB_Y7_A = (RadioButton) mRootView.findViewById(R.id.radioButton7);
        vRB_Y7_FV = (RadioButton) mRootView.findViewById(R.id.radioButton12);

        vRB_G_A = (RadioButton) mRootView.findViewById(R.id.radioButton13);

        vRB_PG_A = (RadioButton) mRootView.findViewById(R.id.radioButton19);
        vRB_PG_D = (RadioButton) mRootView.findViewById(R.id.radioButton20);
        vRB_PG_L = (RadioButton) mRootView.findViewById(R.id.radioButton21);
        vRB_PG_S = (RadioButton) mRootView.findViewById(R.id.radioButton22);
        vRB_PG_V = (RadioButton) mRootView.findViewById(R.id.radioButton23);

        vRB_14_A = (RadioButton) mRootView.findViewById(R.id.radioButton25);
        vRB_14_D = (RadioButton) mRootView.findViewById(R.id.radioButton26);
        vRB_14_L = (RadioButton) mRootView.findViewById(R.id.radioButton27);
        vRB_14_S = (RadioButton) mRootView.findViewById(R.id.radioButton28);
        vRB_14_V = (RadioButton) mRootView.findViewById(R.id.radioButton29);

        vRB_MA_A = (RadioButton) mRootView.findViewById(R.id.radioButton31);
        vRB_MA_L = (RadioButton) mRootView.findViewById(R.id.radioButton33);
        vRB_MA_S = (RadioButton) mRootView.findViewById(R.id.radioButton34);
        vRB_MA_V = (RadioButton) mRootView.findViewById(R.id.radioButton35);
        mGroup.clear();
        mGroup.add(vRB_Y_A);
        mGroup.add(vRB_Y7_A);
        mGroup.add(vRB_Y7_FV);
        mGroup.add(vRB_G_A);
        mGroup.add(vRB_PG_A);
        mGroup.add(vRB_PG_D);
        mGroup.add(vRB_PG_L);
        mGroup.add(vRB_PG_S);
        mGroup.add(vRB_PG_V);
        mGroup.add(vRB_14_A);
        mGroup.add(vRB_14_D);
        mGroup.add(vRB_14_L);
        mGroup.add(vRB_14_S);
        mGroup.add(vRB_14_V);
        mGroup.add(vRB_MA_A);
        mGroup.add(vRB_MA_L);
        mGroup.add(vRB_MA_S);
        mGroup.add(vRB_MA_V);

        vRB_14_A.setNextFocusLeftId(R.id.radioButton29);
        vRB_14_D.setNextFocusDownId(R.id.radioButton20);
        vRB_MA_A.setNextFocusDownId(R.id.radioButton1);
        vRB_MA_A.setNextFocusLeftId(R.id.radioButton35);
        vRB_MA_A.setNextFocusRightId(R.id.radioButton33);
        vRB_MA_L.setNextFocusLeftId(R.id.radioButton31);
        vRB_14_V.setNextFocusRightId(R.id.radioButton25);
        vRB_MA_L.setNextFocusDownId(R.id.radioButton21);
        vRB_MA_S.setNextFocusDownId(R.id.radioButton22);
        vRB_MA_V.setNextFocusDownId(R.id.radioButton23);
        vRB_MA_V.setNextFocusRightId(R.id.radioButton31);

        vRB_Y_A.setNextFocusDownId(R.id.radioButton7);
        vRB_Y_A.setNextFocusUpId(R.id.radioButton31);
        vRB_Y_A.setNextFocusLeftId(R.id.radioButton1);
        vRB_Y_A.setNextFocusRightId(R.id.radioButton1);

        vRB_Y7_A.setNextFocusDownId(R.id.radioButton13);
        vRB_Y7_A.setNextFocusUpId(R.id.radioButton1);
        vRB_Y7_A.setNextFocusLeftId(R.id.radioButton12);
        vRB_Y7_A.setNextFocusRightId(R.id.radioButton12);

        vRB_Y7_FV.setNextFocusDownId(R.id.radioButton12);
        vRB_Y7_FV.setNextFocusUpId(R.id.radioButton12);
        vRB_Y7_FV.setNextFocusLeftId(R.id.radioButton7);
        vRB_Y7_FV.setNextFocusRightId(R.id.radioButton7);

        vRB_G_A.setNextFocusDownId(R.id.radioButton19);
        vRB_G_A.setNextFocusUpId(R.id.radioButton7);
        vRB_G_A.setNextFocusLeftId(R.id.radioButton13);
        vRB_G_A.setNextFocusRightId(R.id.radioButton13);

        vRB_PG_A.setNextFocusDownId(R.id.radioButton25);
        vRB_PG_A.setNextFocusUpId(R.id.radioButton13);
        vRB_PG_A.setNextFocusLeftId(R.id.radioButton23);
        vRB_PG_A.setNextFocusRightId(R.id.radioButton20);

        vRB_PG_D.setNextFocusDownId(R.id.radioButton26);
        vRB_PG_D.setNextFocusUpId(R.id.radioButton26);
        vRB_PG_D.setNextFocusLeftId(R.id.radioButton19);
        vRB_PG_D.setNextFocusRightId(R.id.radioButton21);

        vRB_PG_L.setNextFocusDownId(R.id.radioButton27);
        vRB_PG_L.setNextFocusUpId(R.id.radioButton33);
        vRB_PG_L.setNextFocusLeftId(R.id.radioButton20);
        vRB_PG_L.setNextFocusRightId(R.id.radioButton22);

        vRB_PG_S.setNextFocusDownId(R.id.radioButton28);
        vRB_PG_S.setNextFocusUpId(R.id.radioButton34);
        vRB_PG_S.setNextFocusLeftId(R.id.radioButton21);
        vRB_PG_S.setNextFocusRightId(R.id.radioButton23);

        vRB_PG_V.setNextFocusDownId(R.id.radioButton29);
        vRB_PG_V.setNextFocusUpId(R.id.radioButton35);
        vRB_PG_V.setNextFocusLeftId(R.id.radioButton22);
        vRB_PG_V.setNextFocusRightId(R.id.radioButton19);
    }

    /**
     * @deprecated
     */
    public void initRatingFromTIF(){
    	List<TvContentRating> currRatings = mTvInputManager.getBlockedRatings();
    	for(TvContentRating rate:currRatings){
    		if(rate.getMainRating().equals(RatingConstHelper.RATING_US_TV_Y)){
    			List<String> subRatings = rate.getSubRatings();
    			if(subRatings != null && subRatings.size() ==1 && subRatings.get(0).equals(RatingConstHelper.SUB_RATING_US_TV_A)){
    				vRB_Y_A_Check = true;
    			}
    		}else if(rate.getMainRating().equals(RatingConstHelper.RATING_US_TV_Y7)){
    			List<String> subRatings = rate.getSubRatings();
    			if(subRatings != null && !subRatings.isEmpty()){
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_A)){
    					vRB_Y7_A_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_FV)){
    					vRB_Y7_FV_Check = true;
    				}
    			}
    		}else if(rate.getMainRating().equals(RatingConstHelper.RATING_US_TV_G)){
    			List<String> subRatings = rate.getSubRatings();
    			if(subRatings != null && !subRatings.isEmpty()){
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_A)){
    					vRB_G_A_Check = true;
    				}

    			}
    		}else if(rate.getMainRating().equals(RatingConstHelper.RATING_US_TV_PG)){
    			List<String> subRatings = rate.getSubRatings();
    			if(subRatings != null && !subRatings.isEmpty()){
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_A)){
    					vRB_PG_A_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_D)){
    					vRB_PG_D_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_L)){
    					vRB_PG_L_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_S)){
    					vRB_PG_S_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_V)){
    					vRB_PG_V_Check = true;
    				}

    			}
    		}else if(rate.getMainRating().equals(RatingConstHelper.RATING_US_TV_14)){
    			List<String> subRatings = rate.getSubRatings();
    			if(subRatings != null && !subRatings.isEmpty()){
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_A)){
    					vRB_14_A_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_D)){
    					vRB_14_D_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_L)){
    					vRB_14_L_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_S)){
    					vRB_14_S_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_V)){
    					vRB_14_V_Check = true;
    				}

    			}
    		}else if(rate.getMainRating().equals(RatingConstHelper.RATING_US_TV_MA)){
    			List<String> subRatings = rate.getSubRatings();
    			if(subRatings != null && !subRatings.isEmpty()){
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_A)){
    					vRB_MA_A_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_L)){
    					vRB_MA_L_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_S)){
    					vRB_MA_S_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_V)){
    					vRB_MA_V_Check = true;
    				}

    			}
    		}
    	}
    	initRatingSettingForTIF();
    }

    public void initRatingFromTIFPlus(){
    	List<TvContentRating> currRatings = mTvInputManager.getBlockedRatings();
    	for(TvContentRating rate:currRatings){
    		if(rate.getMainRating().equals(RatingConstHelper.RATING_US_TV_Y)){
    			List<String> subRatings = rate.getSubRatings();
    			if(subRatings != null && subRatings.size() ==1 && subRatings.get(0).equals(RatingConstHelper.SUB_RATING_US_TV_A)){
    				vRB_Y_A_Check = true;
    			}
    		}else if(rate.getMainRating().equals(RatingConstHelper.RATING_US_TV_Y7)){
    			List<String> subRatings = rate.getSubRatings();
    			if(subRatings != null && !subRatings.isEmpty()){
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_A)){
    					vRB_Y7_A_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_FV)){
    					vRB_Y7_FV_Check = true;
    				}
    			}
    		}else if(rate.getMainRating().equals(RatingConstHelper.RATING_US_TV_G)){
    			List<String> subRatings = rate.getSubRatings();
    			if(subRatings != null && !subRatings.isEmpty()){
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_A)){
    					vRB_G_A_Check = true;
    				}

    			}
    		}else if(rate.getMainRating().equals(RatingConstHelper.RATING_US_TV_PG)){
    			List<String> subRatings = rate.getSubRatings();
    			if(subRatings != null && !subRatings.isEmpty()){
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_A)){
    					vRB_PG_A_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_D)){
    					vRB_PG_D_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_L)){
    					vRB_PG_L_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_S)){
    					vRB_PG_S_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_V)){
    					vRB_PG_V_Check = true;
    				}

    			}
    		}else if(rate.getMainRating().equals(RatingConstHelper.RATING_US_TV_14)){
    			List<String> subRatings = rate.getSubRatings();
    			if(subRatings != null && !subRatings.isEmpty()){
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_A)){
    					vRB_14_A_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_D)){
    					vRB_14_D_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_L)){
    					vRB_14_L_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_S)){
    					vRB_14_S_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_V)){
    					vRB_14_V_Check = true;
    				}

    			}
    		}else if(rate.getMainRating().equals(RatingConstHelper.RATING_US_TV_MA)){
    			List<String> subRatings = rate.getSubRatings();
    			if(subRatings != null && !subRatings.isEmpty()){
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_A)){
    					vRB_MA_A_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_L)){
    					vRB_MA_L_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_S)){
    					vRB_MA_S_Check = true;
    				}
    				if(subRatings.contains(RatingConstHelper.SUB_RATING_US_TV_V)){
    					vRB_MA_V_Check = true;
    				}

    			}
    		}
    	}
    	initRatingSettingForTIF();
    }

    public void initRatingSettingForTIF() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("Rating", "initRatingSettingForTIF");
        if (vRB_Y_A_Check) {
            vRB_Y_A.setChecked(true);
        }else {
            vRB_Y_A.setChecked(false);
        }

       if (vRB_Y7_A_Check) {
           vRB_Y7_A.setChecked(true);
        }else {
            vRB_Y7_A.setChecked(false);
        }

        if (vRB_G_A_Check) {
           vRB_G_A.setChecked(true);
        }else {
            vRB_G_A.setChecked(false);
        }

        if (vRB_PG_A_Check) {
           vRB_PG_A.setChecked(true);
        }else {
            vRB_PG_A.setChecked(false);
        }

        if (vRB_14_A_Check) {
           vRB_14_A.setChecked(true);
        }else {
            vRB_14_A.setChecked(false);
        }

        if (vRB_MA_A_Check) {
            vRB_MA_A.setChecked(true);
        }else {
            vRB_MA_A.setChecked(false);
        }

        if (vRB_14_D_Check) {
           vRB_14_D.setChecked(true);
        }else {
            vRB_14_D.setChecked(false);
        }

        if (vRB_14_L_Check) {
            vRB_14_L.setChecked(true);
        }else {
            vRB_14_L.setChecked(false);
        }

        if (vRB_14_S_Check) {
           vRB_14_S.setChecked(true);
        }else{
            vRB_14_S.setChecked(false);
        }

        if (vRB_14_V_Check) {
            vRB_14_V.setChecked(true);
        }else {
            vRB_14_V.setChecked(false);
        }

        if (vRB_MA_L_Check) {
            vRB_MA_L.setChecked(true);
        }else{
            vRB_MA_L.setChecked(false);
        }

        if (vRB_MA_S_Check) {
           vRB_MA_S.setChecked(true);
        }else {
            vRB_MA_S.setChecked(false);
        }

        if (vRB_MA_V_Check) {
            vRB_MA_V.setChecked(true);
        }else {
            vRB_MA_V.setChecked(false);
        }

        if (vRB_PG_D_Check) {
           vRB_PG_D.setChecked(true);
        }else {
            vRB_PG_D.setChecked(false);
        }

        if (vRB_PG_L_Check) {
           vRB_PG_L.setChecked(true);
        }else{
            vRB_PG_L.setChecked(false);
        }

        if (vRB_PG_S_Check) {
           vRB_PG_S.setChecked(true);
        }else {
            vRB_PG_S.setChecked(false);
        }

        if (vRB_PG_V_Check) {
            vRB_PG_V.setChecked(true);
        }else {
            vRB_PG_V.setChecked(false);
        }

        if (vRB_Y7_FV_Check) {
           vRB_Y7_FV.setChecked(true);
        }else {
            vRB_Y7_FV.setChecked(false);
        }
    }

    public void initRatingSetting() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("Rating", "initRatingSetting"+mTV.getATSCRating().getUSTvRatingSettingInfo().isUsAgeTvYBlock());
        MtkTvUSTvRatingSettingInfoBase info= mTV.getATSCRating().getUSTvRatingSettingInfo();
        if (info.isUsAgeTvYBlock()) {
            vRB_Y_A.setChecked(true);
        }else {
            vRB_Y_A.setChecked(false);
        }

       if (info.isUsAgeTvY7Block()) {
           vRB_Y7_A.setChecked(true);
        }else {
            vRB_Y7_A.setChecked(false);
        }

        if (info.isUsAgeTvGBlock()) {
           vRB_G_A.setChecked(true);
        }else {
            vRB_G_A.setChecked(false);
        }

        if (info.isUsAgeTvPGBlock()) {
           vRB_PG_A.setChecked(true);
        }else {
            vRB_PG_A.setChecked(false);
        }

        if (info.isUsAgeTv14Block()) {
           vRB_14_A.setChecked(true);
        }else {
            vRB_14_A.setChecked(false);
        }

        if (info.isUsAgeTvMABlock()) {
            vRB_MA_A.setChecked(true);
        }else {
            vRB_MA_A.setChecked(false);
        }

        if (info.isUsCntTv14DBlock()) {
           vRB_14_D.setChecked(true);
        }else {
            vRB_14_D.setChecked(false);
        }

        if (info.isUsCntTv14LBlock()) {
            vRB_14_L.setChecked(true);
        }else {
            vRB_14_L.setChecked(false);
        }

        if (info.isUsCntTv14SBlock()) {
           vRB_14_S.setChecked(true);
        }else{
            vRB_14_S.setChecked(false);
        }

        if (info.isUsCntTv14VBlock()) {
            vRB_14_V.setChecked(true);
        }else {
            vRB_14_V.setChecked(false);
        }

        if (info.isUsCntTvMALBlock()) {
            vRB_MA_L.setChecked(true);
        }else{
            vRB_MA_L.setChecked(false);
        }

        if (info.isUsCntTvMASBlock()) {
           vRB_MA_S.setChecked(true);
        }else {
            vRB_MA_S.setChecked(false);
        }

        if (info.isUsCntTvMAVBlock()) {
            vRB_MA_V.setChecked(true);
        }else {
            vRB_MA_V.setChecked(false);
        }

        if (info.isUsCntTvPGDBlock()) {
           vRB_PG_D.setChecked(true);
        }else {
            vRB_PG_D.setChecked(false);
        }

        if (info.isUsCntTvPGLBlock()) {
           vRB_PG_L.setChecked(true);
        }else{
            vRB_PG_L.setChecked(false);
        }

        if (info.isUsCntTvPGSBlock()) {
           vRB_PG_S.setChecked(true);
        }else {
            vRB_PG_S.setChecked(false);
        }

        if (info.isUsCntTvPGVBlock()) {
            vRB_PG_V.setChecked(true);
        }else {
            vRB_PG_V.setChecked(false);
        }

        if (info.isUsCntTvY7FVBlock()) {
           vRB_Y7_FV.setChecked(true);
        }else {
            vRB_Y7_FV.setChecked(false);
        }
    }

//    /**
//     * @deprecated
//     *
//     * @author sin_biaoqinggao
//     * generate TvContentRating object by check which radio button is checked
//     */
//    private void generateContentRating(){
//    	com.mediatek.wwtv.tvcenter.util.MtkLog.d("RatingView", "US generateContentRating");
//    	mWholeRatingList.clear();
//    	String domain = RatingConst.RATING_DOMAIN;
//    	String sys = RatingConst.RATING_SYS_US_TV;
//    	String ratingName = "";
//    	String [] subRatings = null;
//    	int[] indexs = null;
//    	//US_TV_Y
//    	if(vRB_Y_A.isChecked()){
//    		subRatingsList.clear();
//        	ratingName = RatingConst.RATING_US_TV_Y;
//    		subRatings = RatingConst.US_TV_Y_SUB_RATINGS;
//    		TvContentRating us_tv_y = TvContentRating.createRating(domain,sys,ratingName,subRatings);
//    		mWholeRatingList.add(us_tv_y);
//    	}else{
////    		ratingName = RatingConst.RATING_US_TV_Y;
////    		TvContentRating us_tv_y = TvContentRating.createRating(domain,sys,ratingName);
////    		mWholeRatingList.add(us_tv_y);
//    	}
//    	//US_TV_Y7
//    	subRatingsList.clear();
//    	ratingName = RatingConst.RATING_US_TV_Y7;
//		subRatings = RatingConst.US_TV_Y7_SUB_RATINGS;
//		indexs = new int[]{-1,-1};
//		if(vRB_Y7_A.isChecked()){
//			indexs[0] = 0;
//		}
//		if(vRB_Y7_FV.isChecked()){
//			indexs[1] = 1;
//		}
//		for(int i = 0;i<indexs.length;i++){
//			int subidx = indexs[i];
//			if(subidx >= 0){
//				subRatingsList.add(subRatings[subidx]);
//			}
//		}
//		subRatings = subRatingsList.toArray(new String[0]);
//		if(subRatings != null && subRatings.length >0){
//			TvContentRating us_tv_y7 = TvContentRating.createRating(domain,sys,ratingName,subRatings);
//			mWholeRatingList.add(us_tv_y7);
//		}else{
////			TvContentRating us_tv_y7 = TvContentRating.createRating(domain,sys,ratingName);
////			mWholeRatingList.add(us_tv_y7);
//		}
//
//    	//US_TV_G
//    	if(vRB_G_A.isChecked()){
//    		ratingName = RatingConst.RATING_US_TV_G;
//    		subRatings = RatingConst.US_TV_G_SUB_RATINGS;
//    		TvContentRating us_tv_g = TvContentRating.createRating(domain,sys,ratingName,subRatings);
//    		mWholeRatingList.add(us_tv_g);
//    	}else{
////    		ratingName = RatingConst.RATING_US_TV_G;
////    		TvContentRating us_tv_g = TvContentRating.createRating(domain,sys,ratingName);
////    		mWholeRatingList.add(us_tv_g);
//    	}
//
//    	//US_TV_PG
//    	subRatingsList.clear();
//		ratingName = RatingConst.RATING_US_TV_PG;
//		subRatings = RatingConst.US_TV_PG_SUB_RATINGS;
//		indexs = new int[]{-1,-1,-1,-1,-1};
//		if(vRB_PG_A.isChecked()){
//			indexs[0] = 0;
//		}
//		if(vRB_PG_D.isChecked()){
//			indexs[1] = 1;
//		}
//		if(vRB_PG_L.isChecked()){
//			indexs[2] = 2;
//		}
//		if(vRB_PG_S.isChecked()){
//			indexs[3] = 3;
//		}
//		if(vRB_PG_V.isChecked()){
//			indexs[4] = 4;
//		}
//		for(int i = 0;i<indexs.length;i++){
//			int subidx = indexs[i];
//			if(subidx >= 0){
//				subRatingsList.add(subRatings[subidx]);
//			}
//		}
//		subRatings = subRatingsList.toArray(new String[0]);
//
//		if(subRatings != null && subRatings.length >0){
//			TvContentRating us_tv_pg = TvContentRating.createRating(domain,sys,ratingName,subRatings);
//			mWholeRatingList.add(us_tv_pg);
//		}else{
////			TvContentRating us_tv_pg = TvContentRating.createRating(domain,sys,ratingName);
////			mWholeRatingList.add(us_tv_pg);
//		}
//
//    	//US_TV_14
//		subRatingsList.clear();
//		ratingName = RatingConst.RATING_US_TV_14;
//		subRatings = RatingConst.US_TV_14_SUB_RATINGS;
//		indexs = new int[]{-1,-1,-1,-1,-1};
//		if(vRB_14_A.isChecked()){
//			indexs[0] = 0;
//		}
//		if(vRB_14_D.isChecked()){
//			indexs[1] = 1;
//		}
//		if(vRB_14_L.isChecked()){
//			indexs[2] = 2;
//		}
//		if(vRB_14_S.isChecked()){
//			indexs[3] = 3;
//		}
//		if(vRB_14_V.isChecked()){
//			indexs[4] = 4;
//		}
//		for(int i = 0;i<indexs.length;i++){
//			int subidx = indexs[i];
//			if(subidx >= 0){
//				subRatingsList.add(subRatings[subidx]);
//			}
//		}
//		subRatings = subRatingsList.toArray(new String[0]);
//		if(subRatings != null && subRatings.length >0){
//			TvContentRating us_tv_14 = TvContentRating.createRating(domain,sys,ratingName,subRatings);
//			mWholeRatingList.add(us_tv_14);
//		}else{
////			TvContentRating us_tv_14 = TvContentRating.createRating(domain,sys,ratingName);
////			mWholeRatingList.add(us_tv_14);
//		}
//
//		//US_TV_MA
//		subRatingsList.clear();
//		ratingName = RatingConst.RATING_US_TV_MA;
//		subRatings = RatingConst.US_TV_MA_SUB_RATINGS;
//		indexs = new int[]{-1,-1,-1,-1};
//		if(vRB_MA_A.isChecked()){
//			indexs[0] = 0;
//		}
//		if(vRB_MA_L.isChecked()){
//			indexs[1] = 1;
//		}
//		if(vRB_MA_S.isChecked()){
//			indexs[2] = 2;
//		}
//		if(vRB_MA_V.isChecked()){
//			indexs[3] = 3;
//		}
//		for(int i = 0;i<indexs.length;i++){
//			int subidx = indexs[i];
//			if(subidx >= 0){
//				subRatingsList.add(subRatings[subidx]);
//			}
//		}
//		subRatings = subRatingsList.toArray(new String[0]);
//		if(subRatings != null && subRatings.length >0){
//			TvContentRating us_tv_ma = TvContentRating.createRating(domain,sys,ratingName,subRatings);
//			mWholeRatingList.add(us_tv_ma);
//		}else{
////			TvContentRating us_tv_ma = TvContentRating.createRating(domain,sys,ratingName);
////			mWholeRatingList.add(us_tv_ma);
//		}
//
//		setAvailableRating();
//    }
//

    /**
     * @author sin_biaoqinggao
     * generate TvContentRating object by check which radio button is checked
     */
    private void generateContentRatingPlus(){
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d("RatingView", "US generateContentRating");
    	mWholeRatingList.clear();
    	String domain = RatingConstHelper.RATING_DOMAIN;
    	String sys = RatingConstHelper.RATING_SYS_US_TV;
    	String ratingName = "";
    	String [] subRatings = null;
    	int[] indexs = null;
    	//US_TV_Y
    	if(vRB_Y_A.isChecked()){
    		subRatingsList.clear();
        	ratingName = RatingConstHelper.RATING_US_TV_Y;
    		subRatings = RatingConstHelper.US_TV_Y_SUB_RATINGS;
    		TvContentRating usTvy = TvContentRating.createRating(domain,sys,ratingName,subRatings);
    		mWholeRatingList.add(usTvy);
    	}
    	//US_TV_Y7
    	subRatingsList.clear();
    	ratingName = RatingConstHelper.RATING_US_TV_Y7;
		subRatings = RatingConstHelper.US_TV_Y7_SUB_RATINGS;
		indexs = new int[]{-1,-1};
		if(vRB_Y7_A.isChecked()){
			indexs[0] = 0;
		}
		if(vRB_Y7_FV.isChecked()){
			indexs[1] = 1;
		}
		for (int index:indexs){
            int subidx = index;
            if(subidx >= 0){
                subRatingsList.add(subRatings[subidx]);
            }
        }
		subRatings = subRatingsList.toArray(new String[0]);
		if(subRatings != null){
			for(String sub:subRatings){
				TvContentRating usTvy7 = TvContentRating.createRating(domain,sys,ratingName,new String[]{sub});
				mWholeRatingList.add(usTvy7);
			}
		}

    	//US_TV_G
    	if(vRB_G_A.isChecked()){
    		ratingName = RatingConstHelper.RATING_US_TV_G;
    		subRatings = RatingConstHelper.US_TV_G_SUB_RATINGS;
    		TvContentRating usTvg = TvContentRating.createRating(domain,sys,ratingName,subRatings);
    		mWholeRatingList.add(usTvg);
    	}

    	//US_TV_PG
    	subRatingsList.clear();
		ratingName = RatingConstHelper.RATING_US_TV_PG;
		subRatings = RatingConstHelper.US_TV_PG_SUB_RATINGS;
		indexs = new int[]{-1,-1,-1,-1,-1};
		if(vRB_PG_A.isChecked()){
			indexs[0] = 0;
		}
		if(vRB_PG_D.isChecked()){
			indexs[1] = 1;
		}
		if(vRB_PG_L.isChecked()){
			indexs[2] = 2;
		}
		if(vRB_PG_S.isChecked()){
			indexs[3] = 3;
		}
		if(vRB_PG_V.isChecked()){
			indexs[4] = 4;
		}
		for (int index:indexs){
            int subidx = index;
            if(subidx >= 0){
                subRatingsList.add(subRatings[subidx]);
            }
        }
		subRatings = subRatingsList.toArray(new String[0]);

		if(subRatings != null){
			for(String sub:subRatings){
				TvContentRating usTvpg = TvContentRating.createRating(domain,sys,ratingName,new String[]{sub});
				mWholeRatingList.add(usTvpg);
			}
		}

    	//US_TV_14
		subRatingsList.clear();
		ratingName = RatingConstHelper.RATING_US_TV_14;
		subRatings = RatingConstHelper.US_TV_14_SUB_RATINGS;
		indexs = new int[]{-1,-1,-1,-1,-1};
		if(vRB_14_A.isChecked()){
			indexs[0] = 0;
		}
		if(vRB_14_D.isChecked()){
			indexs[1] = 1;
		}
		if(vRB_14_L.isChecked()){
			indexs[2] = 2;
		}
		if(vRB_14_S.isChecked()){
			indexs[3] = 3;
		}
		if(vRB_14_V.isChecked()){
			indexs[4] = 4;
		}
		for (int index:indexs){
            int subidx = index;
            if(subidx >= 0){
                subRatingsList.add(subRatings[subidx]);
            }
        }
		subRatings = subRatingsList.toArray(new String[0]);
		if(subRatings != null){
			for(String sub:subRatings){
				TvContentRating usTv14 = TvContentRating.createRating(domain,sys,ratingName,new String[]{sub});
				mWholeRatingList.add(usTv14);
			}
		}

		//US_TV_MA
		subRatingsList.clear();
		ratingName = RatingConstHelper.RATING_US_TV_MA;
		subRatings = RatingConstHelper.US_TV_MA_SUB_RATINGS;
		indexs = new int[]{-1,-1,-1,-1};
		if(vRB_MA_A.isChecked()){
			indexs[0] = 0;
		}
		if(vRB_MA_L.isChecked()){
			indexs[1] = 1;
		}
		if(vRB_MA_S.isChecked()){
			indexs[2] = 2;
		}
		if(vRB_MA_V.isChecked()){
			indexs[3] = 3;
		}
		for (int index:indexs){
            int subidx = index;
            if(subidx >= 0){
                subRatingsList.add(subRatings[subidx]);
            }
        }
		subRatings = subRatingsList.toArray(new String[0]);
		if(subRatings != null){
			for(String sub:subRatings){
				TvContentRating usTvma = TvContentRating.createRating(domain,sys,ratingName,new String[]{sub});
				mWholeRatingList.add(usTvma);
			}
		}

		setAvailableRatingPlus();
    }

    /**
     * setAvailableRating
     * check which rating obj should add to TvInputManger
     * or remove from TvInputManger
     * @author sin_biaoqinggao
     */
//    private void setAvailableRating(){
//
//    	List<TvContentRating> currRatings = mTvInputManager.getBlockedRatings();
//		List<TvContentRating> tmpRatings = new ArrayList<TvContentRating>();
//		for(int i =0 ;i < mWholeRatingList.size();i++){
//			//com.mediatek.wwtv.tvcenter.util.MtkLog.d("TvContentRating", "rating is =="+mWholeRatingList.get(i).flattenToString());
//			if(currRatings != null && currRatings.contains(mWholeRatingList.get(i))){
//				com.mediatek.wwtv.tvcenter.util.MtkLog.d("TvContentRating","contains:"+mWholeRatingList.get(i).getMainRating());
//				tmpRatings.add(mWholeRatingList.get(i));
//			}
//		}
//		mWholeRatingList.removeAll(tmpRatings);
//		currRatings.removeAll(tmpRatings);
//
//		//first add rating then remove rating
//		com.mediatek.wwtv.tvcenter.util.MtkLog.d("TvContentRating","currRatings.size =="+currRatings.size());
//		for(TvContentRating tcr : currRatings){
//			mTvInputManager.removeBlockedRating(tcr);
//		}
//		com.mediatek.wwtv.tvcenter.util.MtkLog.d("TvContentRating","mWholeRatingList.size =="+mWholeRatingList.size());
//		for(TvContentRating tcr : mWholeRatingList){
//			mTvInputManager.addBlockedRating(tcr);
//		}
//
//    }

    /**
     * setAvailableRatingPlus
     * check which rating obj should add to TvInputManger
     * or remove from TvInputManger
     * @author sin_biaoqinggao
     */
    private void setAvailableRatingPlus(){

    	List<TvContentRating> currRatings = mTvInputManager.getBlockedRatings();
		//List<TvContentRating> tmpRatings = new ArrayList<TvContentRating>();

		if(currRatings.size() < mWholeRatingList.size()){//add
			mWholeRatingList.removeAll(currRatings);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("TvContentRating","add "+mWholeRatingList.size()+" rating");
			for(TvContentRating tcr : mWholeRatingList){
				mTvInputManager.addBlockedRating(tcr);
			}
		}else if(currRatings.size() > mWholeRatingList.size()){//remove
			currRatings.removeAll(mWholeRatingList);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d("TvContentRating","remove "+currRatings.size()+" rating");
			for(TvContentRating tcr : currRatings){
				mTvInputManager.removeBlockedRating(tcr);
			}
		}else{
			com.mediatek.wwtv.tvcenter.util.MtkLog.e("TvContentRating","same size something wrong!");

		}

    }

    /**request unlock rating
     * if the remove rating obj is larger than the obj which'll be add request obj unlock
     * else don't request
     * @author sin_biaoqinggao
     * @param rating
     */
//    private void requestUnLockRating(TvContentRating rating){
//		String focusWin = CommonIntegration.getInstance().getCurrentFocus();
//		if(focusWin.equals(InputSourceManager.MAIN)){
//			//TurnkeyUiMainActivity.getInstance().getTvView().requestUnblockContent(rating);
//		}else if(focusWin.equals(InputSourceManager.SUB)){
//			//TurnkeyUiMainActivity.getInstance().getPipView().requestUnblockContent(rating);
//		}
	//}

//    /**
//     * @deprecated
//     *
//     * @author sin_biaoqinggao
//     * generate TvContentRating object by check which radio button is checked
//     * this method's subrating don't contain US_TV_A
//     */
//    private void generateContentRatingWithoutTV_A(){
//    	mWholeRatingList.clear();
//    	String domain = RatingConst.RATING_DOMAIN;
//    	String sys = RatingConst.RATING_SYS_US_TV;
//    	String ratingName = "";
//    	String [] subRatings = null;
//    	int[] indexs = null;
//    	//US_TV_Y
//    	if(vRB_Y_A.isChecked()){
//    		subRatingsList.clear();
//        	ratingName = RatingConst.RATING_US_TV_Y;
//    		subRatings = RatingConst.US_TV_Y_SUB_RATINGS_N_A;
//    		TvContentRating us_tv_y = TvContentRating.createRating(domain,sys,ratingName,subRatings);
//    		mWholeRatingList.add(us_tv_y);
//    	}
//    	//US_TV_Y7
//    	subRatingsList.clear();
//    	ratingName = RatingConst.RATING_US_TV_Y7;
//		subRatings = RatingConst.US_TV_Y7_SUB_RATINGS_N_A;
//		indexs = new int[]{-1};
//		if(vRB_Y7_FV.isChecked()){
//			indexs[0] = 0;
//		}
//		for(int i = 0;i<indexs.length;i++){
//			int subidx = indexs[i];
//			if(subidx >= 0){
//				subRatingsList.add(subRatings[subidx]);
//			}
//		}
//		subRatings = subRatingsList.toArray(new String[0]);
//		if(subRatings != null && subRatings.length >0){
//			TvContentRating us_tv_y7 = TvContentRating.createRating(domain,sys,ratingName,subRatings);
//			mWholeRatingList.add(us_tv_y7);
//		}else if(vRB_Y7_A.isChecked()){//if only vRB_Y7_A checked create rating with no subratings
//			TvContentRating us_tv_y7 = TvContentRating.createRating(domain,sys,ratingName);
//			mWholeRatingList.add(us_tv_y7);
//		}
//
//    	//US_TV_G
//    	if(vRB_G_A.isChecked()){
//    		ratingName = RatingConst.RATING_US_TV_G;
//    		subRatings = RatingConst.US_TV_G_SUB_RATINGS_N_A;
//    		TvContentRating us_tv_g = TvContentRating.createRating(domain,sys,ratingName,subRatings);
//    		mWholeRatingList.add(us_tv_g);
//    	}
//
//    	//US_TV_PG
//    	subRatingsList.clear();
//		ratingName = RatingConst.RATING_US_TV_PG;
//		subRatings = RatingConst.US_TV_PG_SUB_RATINGS_N_A;
//		indexs = new int[]{-1,-1,-1,-1};
//		if(vRB_PG_D.isChecked()){
//			indexs[0] = 0;
//		}
//		if(vRB_PG_L.isChecked()){
//			indexs[1] = 1;
//		}
//		if(vRB_PG_S.isChecked()){
//			indexs[2] = 2;
//		}
//		if(vRB_PG_V.isChecked()){
//			indexs[3] = 3;
//		}
//		for(int i = 0;i<indexs.length;i++){
//			int subidx = indexs[i];
//			if(subidx >= 0){
//				subRatingsList.add(subRatings[subidx]);
//			}
//		}
//		subRatings = subRatingsList.toArray(new String[0]);
//
//		if(subRatings != null && subRatings.length >0){
//			TvContentRating us_tv_pg = TvContentRating.createRating(domain,sys,ratingName,subRatings);
//			mWholeRatingList.add(us_tv_pg);
//		}else if(vRB_PG_A.isChecked()){//if only vRB_PG_A checked create rating with no subratings
//			TvContentRating us_tv_pg = TvContentRating.createRating(domain,sys,ratingName);
//			mWholeRatingList.add(us_tv_pg);
//		}
//
//    	//US_TV_14
//		subRatingsList.clear();
//		ratingName = RatingConst.RATING_US_TV_14;
//		subRatings = RatingConst.US_TV_14_SUB_RATINGS_N_A;
//		indexs = new int[]{-1,-1,-1,-1};
//		if(vRB_14_D.isChecked()){
//			indexs[0] = 0;
//		}
//		if(vRB_14_L.isChecked()){
//			indexs[1] = 1;
//		}
//		if(vRB_14_S.isChecked()){
//			indexs[2] = 2;
//		}
//		if(vRB_14_V.isChecked()){
//			indexs[3] = 3;
//		}
//		for(int i = 0;i<indexs.length;i++){
//			int subidx = indexs[i];
//			if(subidx >= 0){
//				subRatingsList.add(subRatings[subidx]);
//			}
//		}
//		subRatings = subRatingsList.toArray(new String[0]);
//		if(subRatings != null && subRatings.length >0){
//			TvContentRating us_tv_14 = TvContentRating.createRating(domain,sys,ratingName,subRatings);
//			mWholeRatingList.add(us_tv_14);
//		}else if(vRB_14_A.isChecked()){//if only vRB_14_A checked create rating with no subratings
//			TvContentRating us_tv_14 = TvContentRating.createRating(domain,sys,ratingName);
//			mWholeRatingList.add(us_tv_14);
//		}
//
//		//US_TV_MA
//		subRatingsList.clear();
//		ratingName = RatingConst.RATING_US_TV_MA;
//		subRatings = RatingConst.US_TV_MA_SUB_RATINGS_N_A;
//		indexs = new int[]{-1,-1,-1};
//		if(vRB_MA_L.isChecked()){
//			indexs[0] = 0;
//		}
//		if(vRB_MA_S.isChecked()){
//			indexs[1] = 1;
//		}
//		if(vRB_MA_V.isChecked()){
//			indexs[2] = 2;
//		}
//		for(int i = 0;i<indexs.length;i++){
//			int subidx = indexs[i];
//			if(subidx >= 0){
//				subRatingsList.add(subRatings[subidx]);
//			}
//		}
//		subRatings = subRatingsList.toArray(new String[0]);
//		if(subRatings != null && subRatings.length >0){
//			TvContentRating us_tv_ma = TvContentRating.createRating(domain,sys,ratingName,subRatings);
//			mWholeRatingList.add(us_tv_ma);
//		}else if(vRB_MA_A.isChecked()){//if only vRB_MA_A checked create rating with no subratings
//			TvContentRating us_tv_ma = TvContentRating.createRating(domain,sys,ratingName);
//			mWholeRatingList.add(us_tv_ma);
//		}
//
//		setAvailableRating();
//    }

    private void setRatingInfo(View v,boolean flag) {
        MtkTvUSTvRatingSettingInfoBase info= mTV.getATSCRating().getUSTvRatingSettingInfo();
        switch (((RadioButton) v).getId()) {
            case R.id.radioButton1:
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("Rating", "setUsAgeTvYBlock flag:"+flag);
                if (flag) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("Rating", "setUsAgeTvYBlock:"+flag);

                    info.setUsAgeTvYBlock(flag);
                    info.setUsAgeTvY7Block(flag);
                    info.setUsAgeTvGBlock(flag);
                    info.setUsAgeTvPGBlock(flag);
                    info.setUsAgeTv14Block(flag);
                    info.setUsAgeTvMABlock(flag);
                    info.setUsCntTvY7FVBlock(flag);
                    info.setUsCntTvPGDBlock(flag);
                    info.setUsCntTvPGLBlock(flag);
                    info.setUsCntTvPGSBlock(flag);
                    info.setUsCntTvPGVBlock(flag);
                    info.setUsCntTv14DBlock(flag);
                    info.setUsCntTv14LBlock(flag);
                    info.setUsCntTv14SBlock(flag);
                    info.setUsCntTv14VBlock(flag);
                    info.setUsCntTvMALBlock(flag);
                    info.setUsCntTvMASBlock(flag);
                    info.setUsCntTvMAVBlock(flag);

                    vRB_Y_A_Check = true;
                    vRB_Y7_A_Check = true;
                    vRB_G_A_Check = true;
                    vRB_PG_A_Check = true;
                    vRB_14_A_Check = true;
                    vRB_MA_A_Check = true;
                    vRB_PG_D_Check = true;
                    vRB_14_D_Check = true;
                    vRB_PG_L_Check = true;
                    vRB_14_L_Check = true;
                    vRB_MA_L_Check = true;
                    vRB_PG_S_Check = true;
                    vRB_14_S_Check = true;
                    vRB_MA_S_Check = true;
                    vRB_PG_V_Check = true;
                    vRB_14_V_Check = true;
                    vRB_MA_V_Check = true;
                    vRB_Y7_FV_Check = true;
                }else {
                    info.setUsAgeTvYBlock(flag);
                    vRB_Y_A_Check = false;
                }
                break;
            case R.id.radioButton7:
                if (flag) {
                    info.setUsAgeTvY7Block(flag);
                    info.setUsAgeTvGBlock(flag);
                    info.setUsAgeTvPGBlock(flag);
                    info.setUsAgeTv14Block(flag);
                    info.setUsAgeTvMABlock(flag);
                    info.setUsCntTvY7FVBlock(flag);
                    info.setUsCntTvPGDBlock(flag);
                    info.setUsCntTvPGLBlock(flag);
                    info.setUsCntTvPGSBlock(flag);
                    info.setUsCntTvPGVBlock(flag);
                    info.setUsCntTv14DBlock(flag);
                    info.setUsCntTv14LBlock(flag);
                    info.setUsCntTv14SBlock(flag);
                    info.setUsCntTv14VBlock(flag);
                    info.setUsCntTvMALBlock(flag);
                    info.setUsCntTvMASBlock(flag);
                    info.setUsCntTvMAVBlock(flag);

                    vRB_Y7_A_Check = true;
                    vRB_G_A_Check = true;
                    vRB_PG_A_Check = true;
                    vRB_14_A_Check = true;
                    vRB_MA_A_Check = true;
                    vRB_PG_D_Check = true;
                    vRB_14_D_Check = true;
                    vRB_PG_L_Check = true;
                    vRB_14_L_Check = true;
                    vRB_MA_L_Check = true;
                    vRB_PG_S_Check = true;
                    vRB_14_S_Check = true;
                    vRB_MA_S_Check = true;
                    vRB_PG_V_Check = true;
                    vRB_14_V_Check = true;
                    vRB_MA_V_Check = true;
                    vRB_Y7_FV_Check = true;
                }else {
                    info.setUsAgeTvYBlock(flag);
                    info.setUsAgeTvY7Block(flag);
                    info.setUsCntTvY7FVBlock(flag);
                    vRB_Y_A_Check = false;
                    vRB_Y7_A_Check = false;
                    vRB_Y7_FV_Check = false;
                }
                break;
            case R.id.radioButton13:
                if (flag) {
                    info.setUsAgeTvGBlock(flag);
                    info.setUsAgeTvPGBlock(flag);
                    info.setUsAgeTv14Block(flag);
                    info.setUsAgeTvMABlock(flag);
                    info.setUsCntTvPGDBlock(flag);
                    info.setUsCntTvPGLBlock(flag);
                    info.setUsCntTvPGSBlock(flag);
                    info.setUsCntTvPGVBlock(flag);
                    info.setUsCntTv14DBlock(flag);
                    info.setUsCntTv14LBlock(flag);
                    info.setUsCntTv14SBlock(flag);
                    info.setUsCntTv14VBlock(flag);
                    info.setUsCntTvMALBlock(flag);
                    info.setUsCntTvMASBlock(flag);
                    info.setUsCntTvMAVBlock(flag);

                    vRB_G_A_Check = true;
                    vRB_PG_A_Check = true;
                    vRB_14_A_Check = true;
                    vRB_MA_A_Check = true;
                    vRB_PG_D_Check = true;
                    vRB_14_D_Check = true;
                    vRB_PG_L_Check = true;
                    vRB_14_L_Check = true;
                    vRB_MA_L_Check = true;
                    vRB_PG_S_Check = true;
                    vRB_14_S_Check = true;
                    vRB_MA_S_Check = true;
                    vRB_PG_V_Check = true;
                    vRB_14_V_Check = true;
                    vRB_MA_V_Check = true;
                }else {
                    info.setUsAgeTvYBlock(flag);
                    info.setUsAgeTvY7Block(flag);
                    info.setUsCntTvY7FVBlock(flag);
                    info.setUsAgeTvGBlock(flag);

                    vRB_Y_A_Check = false;
                    vRB_Y7_A_Check = false;
                    vRB_Y7_FV_Check = false;
                    vRB_G_A_Check = false;
                }
                break;
            case R.id.radioButton19:
                if (flag) {
                    info.setUsAgeTvPGBlock(flag);
                    info.setUsAgeTv14Block(flag);
                    info.setUsAgeTvMABlock(flag);
                    info.setUsCntTvPGDBlock(flag);
                    info.setUsCntTvPGLBlock(flag);
                    info.setUsCntTvPGSBlock(flag);
                    info.setUsCntTvPGVBlock(flag);
                    info.setUsCntTv14DBlock(flag);
                    info.setUsCntTv14LBlock(flag);
                    info.setUsCntTv14SBlock(flag);
                    info.setUsCntTv14VBlock(flag);
                    info.setUsCntTvMALBlock(flag);
                    info.setUsCntTvMASBlock(flag);
                    info.setUsCntTvMAVBlock(flag);

                    vRB_PG_A_Check = true;
                    vRB_14_A_Check = true;
                    vRB_MA_A_Check = true;
                    vRB_PG_D_Check = true;
                    vRB_14_D_Check = true;
                    vRB_PG_L_Check = true;
                    vRB_14_L_Check = true;
                    vRB_MA_L_Check = true;
                    vRB_PG_S_Check = true;
                    vRB_14_S_Check = true;
                    vRB_MA_S_Check = true;
                    vRB_PG_V_Check = true;
                    vRB_14_V_Check = true;
                    vRB_MA_V_Check = true;
                }else {
                    info.setUsAgeTvYBlock(flag);
                    info.setUsAgeTvY7Block(flag);
                    info.setUsCntTvY7FVBlock(flag);
                    info.setUsAgeTvGBlock(flag);
                    info.setUsAgeTvPGBlock(flag);
                    info.setUsCntTvPGDBlock(flag);
                    info.setUsCntTvPGLBlock(flag);
                    info.setUsCntTvPGSBlock(flag);
                    info.setUsCntTvPGVBlock(flag);

                    vRB_Y_A_Check = false;
                    vRB_Y7_A_Check = false;
                    vRB_Y7_FV_Check = false;
                    vRB_G_A_Check = false;
                    vRB_PG_A_Check = false;
                    vRB_PG_D_Check = false;
                    vRB_PG_L_Check = false;
                    vRB_PG_S_Check = false;
                    vRB_PG_V_Check = false;
                }
                break;
            case R.id.radioButton25:
                if (flag) {
                    info.setUsAgeTv14Block(flag);
                    info.setUsAgeTvMABlock(flag);
                    info.setUsCntTv14DBlock(flag);
                    info.setUsCntTv14LBlock(flag);
                    info.setUsCntTv14SBlock(flag);
                    info.setUsCntTv14VBlock(flag);
                    info.setUsCntTvMALBlock(flag);
                    info.setUsCntTvMASBlock(flag);
                    info.setUsCntTvMAVBlock(flag);

                    vRB_14_A_Check = true;
                    vRB_MA_A_Check = true;
                    vRB_14_D_Check = true;
                    vRB_14_L_Check = true;
                    vRB_MA_L_Check = true;
                    vRB_14_S_Check = true;
                    vRB_MA_S_Check = true;
                    vRB_14_V_Check = true;
                    vRB_MA_V_Check = true;
                }else {
                    info.setUsAgeTvYBlock(flag);
                    info.setUsAgeTvY7Block(flag);
                    info.setUsCntTvY7FVBlock(flag);
                    info.setUsAgeTvGBlock(flag);
                    info.setUsAgeTvPGBlock(flag);
                    info.setUsCntTvPGDBlock(flag);
                    info.setUsCntTvPGLBlock(flag);
                    info.setUsCntTvPGSBlock(flag);
                    info.setUsCntTvPGVBlock(flag);
                    info.setUsAgeTv14Block(flag);
                    info.setUsCntTv14DBlock(flag);
                    info.setUsCntTv14LBlock(flag);
                    info.setUsCntTv14SBlock(flag);
                    info.setUsCntTv14VBlock(flag);

                    vRB_Y_A_Check = false;
                    vRB_Y7_A_Check = false;
                    vRB_Y7_FV_Check = false;
                    vRB_G_A_Check = false;
                    vRB_PG_A_Check = false;
                    vRB_PG_D_Check = false;
                    vRB_PG_L_Check = false;
                    vRB_PG_S_Check = false;
                    vRB_PG_V_Check = false;
                    vRB_14_A_Check = false;
                    vRB_14_D_Check = false;
                    vRB_14_L_Check = false;
                    vRB_14_S_Check = false;
                    vRB_14_V_Check = false;

                }
                break;
            case R.id.radioButton31:
                if (flag) {
                    info.setUsAgeTvMABlock(flag);
                    info.setUsCntTvMALBlock(flag);
                    info.setUsCntTvMASBlock(flag);
                    info.setUsCntTvMAVBlock(flag);

                    vRB_MA_A_Check = true;
                    vRB_MA_L_Check = true;
                    vRB_MA_S_Check = true;
                    vRB_MA_V_Check = true;
                }else {
                    info.setUsAgeTvYBlock(flag);
                    info.setUsAgeTvY7Block(flag);
                    info.setUsCntTvY7FVBlock(flag);
                    info.setUsAgeTvGBlock(flag);
                    info.setUsAgeTvPGBlock(flag);
                    info.setUsCntTvPGDBlock(flag);
                    info.setUsCntTvPGLBlock(flag);
                    info.setUsCntTvPGSBlock(flag);
                    info.setUsCntTvPGVBlock(flag);
                    info.setUsAgeTv14Block(flag);
                    info.setUsCntTv14DBlock(flag);
                    info.setUsCntTv14LBlock(flag);
                    info.setUsCntTv14SBlock(flag);
                    info.setUsCntTv14VBlock(flag);
                    info.setUsAgeTvMABlock(flag);
                    info.setUsCntTvMALBlock(flag);
                    info.setUsCntTvMASBlock(flag);
                    info.setUsCntTvMAVBlock(flag);

                    vRB_Y_A_Check = false;
                    vRB_Y7_A_Check = false;
                    vRB_Y7_FV_Check = false;
                    vRB_G_A_Check = false;
                    vRB_PG_A_Check = false;
                    vRB_PG_D_Check = false;
                    vRB_PG_L_Check = false;
                    vRB_PG_S_Check = false;
                    vRB_PG_V_Check = false;
                    vRB_14_A_Check = false;
                    vRB_14_D_Check = false;
                    vRB_14_L_Check = false;
                    vRB_14_S_Check = false;
                    vRB_14_V_Check = false;
                    vRB_MA_A_Check = false;
                    vRB_MA_L_Check = false;
                    vRB_MA_S_Check = false;
                    vRB_MA_V_Check = false;

                }
                break;
            case R.id.radioButton12:
                info.setUsCntTvY7FVBlock(flag);
                vRB_Y7_FV_Check = flag;
                break;
            case R.id.radioButton20:
                info.setUsCntTvPGDBlock(flag);
                vRB_PG_D_Check = flag;
                if (flag) {
                    info.setUsCntTv14DBlock(flag);
                    vRB_14_D_Check = flag;
                }
                break;
            case R.id.radioButton21:
                info.setUsCntTvPGLBlock(flag);
                vRB_PG_L_Check = flag;
                if (flag) {
                    info.setUsCntTv14LBlock(flag);
                    info.setUsCntTvMALBlock(flag);
                    vRB_14_L_Check = flag;
                    vRB_MA_L_Check = flag;
                }
                break;
            case R.id.radioButton22:
                info.setUsCntTvPGSBlock(flag);
                vRB_PG_S_Check = flag;
                if (flag) {
                    info.setUsCntTv14SBlock(flag);
                    info.setUsCntTvMASBlock(flag);
                    vRB_14_S_Check = flag;
                    vRB_MA_S_Check = flag;
                }
                break;
            case R.id.radioButton23:
                info.setUsCntTvPGVBlock(flag);
                vRB_PG_V_Check = flag;
                if (flag) {
                    info.setUsCntTv14VBlock(flag);
                    info.setUsCntTvMAVBlock(flag);
                    vRB_14_V_Check = flag;
                    vRB_MA_V_Check = flag;
                }
                break;
            case R.id.radioButton26:
                info.setUsCntTv14DBlock(flag);
                vRB_14_D_Check = flag;
                if (!flag) {
                    info.setUsCntTvPGDBlock(flag);
                    vRB_PG_D_Check = flag;
                }

                break;
            case R.id.radioButton27:
                info.setUsCntTv14LBlock(flag);
                vRB_14_L_Check = flag;
                if (flag) {
                    info.setUsCntTvMALBlock(flag);
                    vRB_MA_L_Check = flag;
                }else {
                    info.setUsCntTvPGLBlock(flag);
                    vRB_PG_L_Check = flag;
                }
                break;
            case R.id.radioButton28:
                info.setUsCntTv14SBlock(flag);
                vRB_14_S_Check = flag;
                if (flag) {
                    info.setUsCntTvMASBlock(flag);
                    vRB_MA_S_Check = flag;
                }else {
                    info.setUsCntTvPGSBlock(flag);
                    vRB_PG_S_Check = flag;
                }
                break;
            case R.id.radioButton29:
                info.setUsCntTv14VBlock(flag);
                vRB_14_V_Check = flag;
                if (flag) {
                    info.setUsCntTvMAVBlock(flag);
                    vRB_MA_V_Check = flag;
                }else {
                    info.setUsCntTvPGVBlock(flag);
                    vRB_PG_V_Check = flag;
                }
                break;
            case R.id.radioButton33:
                info.setUsCntTvMALBlock(flag);
                vRB_MA_L_Check = flag;
                if (!flag) {
                    info.setUsCntTv14LBlock(flag);
                    info.setUsCntTvPGLBlock(flag);
                    vRB_14_L_Check = flag;
                    vRB_PG_L_Check = flag;
                }
                break;
            case R.id.radioButton34:
                info.setUsCntTvMASBlock(flag);
                vRB_MA_S_Check = flag;
                if (!flag) {
                    info.setUsCntTv14SBlock(flag);
                    info.setUsCntTvPGSBlock(flag);
                    vRB_14_S_Check = flag;
                    vRB_PG_S_Check = flag;
                }
                break;
            case R.id.radioButton35:
                info.setUsCntTvMAVBlock(flag);
                vRB_MA_V_Check = flag;
                if (!flag) {
                    info.setUsCntTv14VBlock(flag);
                    info.setUsCntTvPGVBlock(flag);
                    vRB_14_V_Check = flag;
                    vRB_PG_V_Check = flag;
                }
                break;
            default:
                break;
        }
        if(!MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING)){
            Map<String,Boolean> map = new HashMap<String,Boolean>();
            map.put("Tv_Y", info.isUsAgeTvYBlock());
            map.put("Tv_Y7", info.isUsAgeTvY7Block());
            map.put("Tv_G", info.isUsAgeTvGBlock());
            map.put("Tv_PG", info.isUsAgeTvPGBlock());
            map.put("Tv_14", info.isUsAgeTv14Block());
            map.put("Tv_MA", info.isUsAgeTvMABlock());

            map.put("Tv_14_D", info.isUsCntTv14DBlock());
            map.put("Tv_14_L", info.isUsCntTv14LBlock());
            map.put("Tv_14_S", info.isUsCntTv14SBlock());
            map.put("Tv_14_V", info.isUsCntTv14VBlock());
            map.put("Tv_MA_S", info.isUsCntTvMASBlock());
            map.put("Tv_MA_L", info.isUsCntTvMALBlock());
            map.put("Tv_MA_V", info.isUsCntTvMAVBlock());

            map.put("Tv_PG_D", info.isUsCntTvPGDBlock());
            map.put("Tv_PG_L", info.isUsCntTvPGLBlock());
            map.put("Tv_PG_S", info.isUsCntTvPGSBlock());
            map.put("Tv_PG_V", info.isUsCntTvPGVBlock());

            map.put("Tv_Y7_FV", info.isUsCntTvY7FVBlock());
//          com.mediatek.wwtv.tvcenter.util.MtkLog.d("Ratings", "info map is=="+map);

            mTV.getATSCRating().setUSTvRatingSettingInfo(info);
        }
    }

}
