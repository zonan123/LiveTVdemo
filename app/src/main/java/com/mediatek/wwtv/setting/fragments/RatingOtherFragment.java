
package com.mediatek.wwtv.setting.fragments;

import android.app.Fragment;
import android.content.Context;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;

import android.widget.RadioButton;
import android.widget.TextView;

import com.mediatek.twoworlds.tv.MtkTvATSCRatingBase;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.setting.preferences.PreferenceUtil;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.RatingConstHelper;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;

import com.mediatek.wwtv.setting.widget.detailui.Action;
import com.mediatek.wwtv.setting.widget.detailui.Action.DataType;

import android.media.tv.TvInputManager;
import android.media.tv.TvContentRating;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class RatingOtherFragment extends Fragment {


    private TextView vTextView1;
    private TextView vTextView2;
    private TextView vTextView3;
    private TextView vTextView4;
    private TextView vTextView5;
    private TextView vTextView6;
    private RadioButton vRadioButton1;
    private RadioButton vRadioButton2;
    private RadioButton vRadioButton3;
    private RadioButton vRadioButton4;
    private RadioButton vRadioButton5;
    private RadioButton vRadioButton6;
    private List<RadioButton> mGroup = new ArrayList<RadioButton>();
    TVContent mTV;
    String mItemId;
    TvInputManager mTvInputManager;
    private boolean isPositionView = false;
    private ViewGroup mRootView;
   
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Context mContext;
		mContext = getActivity();
	    mTV = TVContent.getInstance(mContext);
	    Bundle bundle = getArguments();
	    if(bundle != null){
	    	mItemId = bundle.getCharSequence(PreferenceUtil.PARENT_PREFERENCE_ID).toString();
	    }
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d("guanglei", "mItemId:" + mItemId);
	    mTvInputManager = (TvInputManager) mContext.getSystemService(Context.TV_INPUT_SERVICE);
	}
	
	public void setAction(Action action){
        Action mAction;
		mAction = action;
		if(action.mDataType == DataType.POSITIONVIEW){
			isPositionView = true;
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("Rating", "mAction:"+mAction+",isPositionView="+isPositionView);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = (ViewGroup) inflater.inflate(R.layout.menu_french_ratings_view,
				null);
		initView();
		init(mItemId);
		setListener();
		return mRootView;
	}

    private void initView() {
        vTextView1 = (TextView) mRootView.findViewById(R.id.textview1);
        vTextView2 = (TextView) mRootView.findViewById(R.id.textview2);
        vTextView3 = (TextView) mRootView.findViewById(R.id.textview3);
        vTextView4 = (TextView) mRootView.findViewById(R.id.textview4);
        vTextView5 = (TextView) mRootView.findViewById(R.id.textview5);
        vTextView6 = (TextView) mRootView.findViewById(R.id.textview6);
        vRadioButton1 = (RadioButton) mRootView.findViewById(R.id.radioButton1);
        vRadioButton2 = (RadioButton) mRootView.findViewById(R.id.radioButton2);
        vRadioButton3 = (RadioButton) mRootView.findViewById(R.id.radioButton3);
        vRadioButton4 = (RadioButton) mRootView.findViewById(R.id.radioButton4);
        vRadioButton5 = (RadioButton) mRootView.findViewById(R.id.radioButton5);
        vRadioButton6 = (RadioButton) mRootView.findViewById(R.id.radioButton6);
        mGroup.add(vRadioButton1);
        mGroup.add(vRadioButton2);
        mGroup.add(vRadioButton3);
        mGroup.add(vRadioButton4);
        mGroup.add(vRadioButton5);
        mGroup.add(vRadioButton6);
    }

    private void setListener() {
        for (RadioButton radioButton:mGroup){
            radioButton.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_DPAD_CENTER:
                            case KeyEvent.KEYCODE_ENTER:
                                if (((RadioButton) v).isChecked()) {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("Rating", "setChecked false:");
                                    if(false && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING)){
                                        setRatingInfoForTIF(v, false);
                                    }else{
                                        ((RadioButton) v).setChecked(false);
                                        setRatingInfo(v, false);
                                    }
                                } else {
                                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("Rating", "setChecked true:");
                                    if(false && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING)){
                                        setRatingInfoForTIF(v, true);
                                    }else{
                                        ((RadioButton) v).setChecked(true);
                                        setRatingInfo(v, true);
                                    }
                                }
                                if(false && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING)){
                                    generateRating(mItemId);
                                }else{
                                    initRatingSetting(mItemId);
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

    @Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if(!vRadioButton1.hasFocus()){
			vRadioButton1.requestFocus();
		}
	}
    private void setRatingInfo(View v, boolean flag) {
        //fix CR DTV00585937
        if (mItemId.equals(MenuConfigManager.PARENTAL_US_MOVIE_RATINGS)) {
            switch (((RadioButton) v).getId()) {
                case R.id.radioButton1:
                    if (flag) {
                        mTV.getATSCRating().setUSMovieRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_MOVIE_G);
                    } else {
                        mTV.getATSCRating().setUSMovieRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_MOVIE_PG);
                    }
                    break;
                case R.id.radioButton2:
                    if (flag) {
                        mTV.getATSCRating().setUSMovieRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_MOVIE_PG);
                    } else {
                        mTV.getATSCRating().setUSMovieRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_MOVIE_PG13);
                    }
                    break;
                case R.id.radioButton3:
                    if (flag) {
                        mTV.getATSCRating().setUSMovieRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_MOVIE_PG13);
                    } else {
                        mTV.getATSCRating().setUSMovieRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_MOVIE_R);
                    }
                    break;
                case R.id.radioButton4:
                    if (flag) {
                        mTV.getATSCRating().setUSMovieRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_MOVIE_R);
                    } else {
                        mTV.getATSCRating().setUSMovieRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_MOVIE_NC_17);
                    }
                    break;
                case R.id.radioButton5:
                    if (flag) {
                        mTV.getATSCRating().setUSMovieRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_MOVIE_NC_17);
                    } else {
                        mTV.getATSCRating().setUSMovieRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_MOVIE_X);
                    }
                    break;
                case R.id.radioButton6:
                    if (flag) {
                        mTV.getATSCRating().setUSMovieRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_MOVIE_X);
                    } else {
                        mTV.getATSCRating().setUSMovieRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_MOVIE_OFF);
                    }
                    break;
                default:
                    break;
            }
        }else if (mItemId.equals(MenuConfigManager.PARENTAL_CANADIAN_ENGLISH_RATINGS)){
            switch (((RadioButton) v).getId()) {
                case R.id.radioButton1:
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d("Rating", "setCANEngRatingSettingInfo flag:"+flag);
                    if (flag) {
                        mTV.getATSCRating().setCANEngRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_ENG_C);
                    } else {
                        mTV.getATSCRating().setCANEngRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_ENG_C8);
                    }
                    break;
                case R.id.radioButton2:
                    if (flag) {
                        mTV.getATSCRating().setCANEngRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_ENG_C8);
                    } else {
                        mTV.getATSCRating().setCANEngRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_ENG_G);
                    }
                    break;
                case R.id.radioButton3:
                    if (flag) {
                        mTV.getATSCRating().setCANEngRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_ENG_G);
                    } else {
                        mTV.getATSCRating().setCANEngRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_ENG_PG);
                    }
                    break;
                case R.id.radioButton4:
                    if (flag) {
                        mTV.getATSCRating().setCANEngRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_ENG_PG);
                    } else {
                        mTV.getATSCRating().setCANEngRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_ENG_14);
                    }
                    break;
                case R.id.radioButton5:
                    if (flag) {
                        mTV.getATSCRating().setCANEngRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_ENG_14);
                    } else {
                        mTV.getATSCRating().setCANEngRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_ENG_18);
                    }
                    break;
                case R.id.radioButton6:
                    if (flag) {
                        mTV.getATSCRating().setCANEngRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_ENG_18);
                    } else {
                        mTV.getATSCRating().setCANEngRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_ENG_OFF);
                    }
                    break;
                default:
                    break;
            }
        }else {
            switch (((RadioButton) v).getId()) {
                case R.id.radioButton1:
                    if (flag) {
                        mTV.getATSCRating().setCANFreRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_FRE_G);
                    } else {
                        mTV.getATSCRating().setCANFreRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_FRE_8);
                    }
                    break;
                case R.id.radioButton2:
                    if (flag) {
                        mTV.getATSCRating().setCANFreRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_FRE_8);
                    } else {
                        mTV.getATSCRating().setCANFreRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_FRE_13);
                    }
                    break;
                case R.id.radioButton3:
                    if (flag) {
                        mTV.getATSCRating().setCANFreRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_FRE_13);
                    } else {
                        mTV.getATSCRating().setCANFreRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_FRE_16);
                    }
                    break;
                case R.id.radioButton4:
                    if (flag) {
                        mTV.getATSCRating().setCANFreRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_FRE_16);
                    } else {
                        mTV.getATSCRating().setCANFreRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_FRE_18);
                    }
                    break;
                case R.id.radioButton5:
                    if (flag) {
                        mTV.getATSCRating().setCANFreRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_FRE_18);
                    } else {
                        mTV.getATSCRating().setCANFreRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_FRE_OFF);
                    }
                    break;
                case R.id.radioButton6:
                    if (flag) {
                        mTV.getATSCRating().setCANFreRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_FRE_18);
                    } else {
                        mTV.getATSCRating().setCANFreRatingSettingInfo(MtkTvATSCRatingBase.MTKTV_ATSC_CANADA_FRE_OFF);
                    }
                    break;
                default:
                    break;
            }
        }
    }
    
	private void setRatingInfoForTIF(View v, boolean flag) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("RatingOtherView", "setRatingInfoForTIF flag:"+flag);
		switch (((RadioButton) v).getId()) {
		case R.id.radioButton1:
			if (flag) {
				vRadioButton1.setChecked(true);
				vRadioButton2.setChecked(true);
				vRadioButton3.setChecked(true);
				vRadioButton4.setChecked(true);
				vRadioButton5.setChecked(true);
				vRadioButton6.setChecked(true);
			} else {
				vRadioButton1.setChecked(false);
			}
			break;
		case R.id.radioButton2:
			if (flag) {
				vRadioButton2.setChecked(true);
				vRadioButton3.setChecked(true);
				vRadioButton4.setChecked(true);
				vRadioButton5.setChecked(true);
				vRadioButton6.setChecked(true);
			} else {
				vRadioButton1.setChecked(false);
				vRadioButton2.setChecked(false);
			}
			break;
		case R.id.radioButton3:
			if (flag) {
				vRadioButton3.setChecked(true);
				vRadioButton4.setChecked(true);
				vRadioButton5.setChecked(true);
				vRadioButton6.setChecked(true);
			} else {
				vRadioButton1.setChecked(false);
				vRadioButton2.setChecked(false);
				vRadioButton3.setChecked(false);
			}
			break;
		case R.id.radioButton4:
			if (flag) {
				vRadioButton4.setChecked(true);
				vRadioButton5.setChecked(true);
				vRadioButton6.setChecked(true);
			} else {
				vRadioButton1.setChecked(false);
				vRadioButton2.setChecked(false);
				vRadioButton3.setChecked(false);
				vRadioButton4.setChecked(false);
			}
			break;
		case R.id.radioButton5:
			if (flag) {
				vRadioButton5.setChecked(true);
				vRadioButton6.setChecked(true);
			} else {
				vRadioButton1.setChecked(false);
				vRadioButton2.setChecked(false);
				vRadioButton3.setChecked(false);
				vRadioButton4.setChecked(false);
				vRadioButton5.setChecked(false);
			}
			break;
		case R.id.radioButton6:
			if (flag) {
				vRadioButton6.setChecked(true);
			} else {
				vRadioButton1.setChecked(false);
				vRadioButton2.setChecked(false);
				vRadioButton3.setChecked(false);
				vRadioButton4.setChecked(false);
				vRadioButton5.setChecked(false);
				vRadioButton6.setChecked(false);
			}
			break;
        default:
            break;
		}

	}

    public void init(String itemID) {
        mItemId = itemID;
        vTextView6.setVisibility(View.VISIBLE);
        vRadioButton6.setVisibility(View.VISIBLE);
        if (itemID.equals(MenuConfigManager.PARENTAL_US_MOVIE_RATINGS)) {
            vTextView1.setText(R.string.menu_rating_movie_g);
            vTextView2.setText(R.string.menu_rating_movie_pg);
            vTextView3.setText(R.string.menu_rating_movie_pg13);
            vTextView4.setText(R.string.menu_rating_movie_r);
            vTextView5.setText(R.string.menu_rating_movie_nc17);
            vTextView6.setText(R.string.menu_rating_movie_x);
            vRadioButton5.setNextFocusDownId(R.id.radioButton6);
            vRadioButton6.setNextFocusDownId(R.id.radioButton1);
            vRadioButton1.setNextFocusUpId(R.id.radioButton6);

        } else if (itemID.equals(MenuConfigManager.PARENTAL_CANADIAN_ENGLISH_RATINGS)) {
            vTextView1.setText(R.string.menu_rating_english_c);
            vTextView2.setText(R.string.menu_rating_english_c8);
            vTextView3.setText(R.string.menu_rating_english_g);
            vTextView4.setText(R.string.menu_rating_english_pg);
            vTextView5.setText(R.string.menu_rating_english_14);
            vTextView6.setText(R.string.menu_rating_english_18);

            vRadioButton5.setNextFocusDownId(R.id.radioButton6);
            vRadioButton1.setNextFocusUpId(R.id.radioButton6);
            vRadioButton6.setNextFocusDownId(R.id.radioButton1);
        } else {
            vTextView1.setText(R.string.menu_rating_french_g);
            vTextView2.setText(R.string.menu_rating_french_8ans);
            vTextView3.setText(R.string.menu_rating_french_13ans);
            vTextView4.setText(R.string.menu_rating_french_16ans);
            vTextView5.setText(R.string.menu_rating_french_18ans);
            vTextView6.setVisibility(View.INVISIBLE);
            vRadioButton6.setVisibility(View.INVISIBLE);

            vRadioButton1.setNextFocusUpId(R.id.radioButton5);
            vRadioButton5.setNextFocusDownId(R.id.radioButton1);
        }
        vRadioButton1.setNextFocusLeftId(R.id.radioButton1);
        vRadioButton1.setNextFocusRightId(R.id.radioButton1);

        vRadioButton2.setNextFocusLeftId(R.id.radioButton2);
        vRadioButton2.setNextFocusRightId(R.id.radioButton2);

        vRadioButton3.setNextFocusLeftId(R.id.radioButton3);
        vRadioButton3.setNextFocusRightId(R.id.radioButton3);

        vRadioButton4.setNextFocusLeftId(R.id.radioButton4);
        vRadioButton4.setNextFocusRightId(R.id.radioButton4);

        vRadioButton5.setNextFocusLeftId(R.id.radioButton5);
        vRadioButton5.setNextFocusRightId(R.id.radioButton5);

        vRadioButton6.setNextFocusLeftId(R.id.radioButton6);
        vRadioButton6.setNextFocusRightId(R.id.radioButton6);
        if(false && MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING)){
        	 initRatingSettingPlus(itemID);
        }else{
        	 initRatingSetting(itemID);
        }
       
    }

    private void initRatingSetting(String itemID) {
        int ratingBlock = 0;
        if (itemID.equals(MenuConfigManager.PARENTAL_US_MOVIE_RATINGS)) {
            ratingBlock = mTV.getATSCRating().getUSMovieRatingSettingInfo();
			for (int i = 0; i < mGroup.size(); i++) {
				if (i >= ratingBlock) {
					mGroup.get(i).setChecked(true);
				} else {
					mGroup.get(i).setChecked(false);
				}
				}
        } else if (itemID.equals(MenuConfigManager.PARENTAL_CANADIAN_ENGLISH_RATINGS)) {
            ratingBlock = mTV.getATSCRating().getCANEngRatingSettingInfo();
			for (int i = 0; i < mGroup.size(); i++) {
				if (i >= ratingBlock-1 && ratingBlock != 0) {
					mGroup.get(i).setChecked(true);
				} else {
					mGroup.get(i).setChecked(false);
				}
				}
        } else {
            ratingBlock = mTV.getATSCRating().getCANFreRatingSettingInfo();
			for (int i = 0; i < mGroup.size(); i++) {
				if (i >= ratingBlock-1 && ratingBlock != 0) {
					mGroup.get(i).setChecked(true);
				} else {
					mGroup.get(i).setChecked(false);
				}
				}

        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("Rating", "ratingBlock:" + ratingBlock);

    }
    
    private void initRatingSettingPlus(String itemID) {
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d("RatingOtherView", "initRatingSettingPlus:" + itemID);
        List<TvContentRating> currRatings = mTvInputManager.getBlockedRatings();
        if (itemID.equals(MenuConfigManager.PARENTAL_US_MOVIE_RATINGS)) {
        	List<TvContentRating> mvRatings = new ArrayList<TvContentRating>();
        	for(TvContentRating rating:currRatings){
        		if(rating.getRatingSystem().equals(RatingConstHelper.RATING_SYS_US_MV)){
        			mvRatings.add(rating);
        		}
        	}
        	
        	for(TvContentRating rating:mvRatings){
        		if(rating.getMainRating().equals(RatingConstHelper.RATING_US_MV_G)){
        			vRadioButton1.setChecked(true);
        		}else if(rating.getMainRating().equals(RatingConstHelper.RATING_US_MV_PG)){
        			vRadioButton2.setChecked(true);
        		}else if(rating.getMainRating().equals(RatingConstHelper.RATING_US_MV_PG13)){
        			vRadioButton3.setChecked(true);
        		}else if(rating.getMainRating().equals(RatingConstHelper.RATING_US_MV_R)){
        			vRadioButton4.setChecked(true);
        		}else if(rating.getMainRating().equals(RatingConstHelper.RATING_US_MV_NC17)){
        			vRadioButton5.setChecked(true);
        		}else if(rating.getMainRating().equals(RatingConstHelper.RATING_US_MV_X)){
        			vRadioButton6.setChecked(true);
        		}
        	}
        } else if (itemID.equals(MenuConfigManager.PARENTAL_CANADIAN_ENGLISH_RATINGS)) {
        	List<TvContentRating> mvRatings = new ArrayList<TvContentRating>();
        	for(TvContentRating rating:currRatings){
        		if(rating.getRatingSystem().equals(RatingConstHelper.RATING_SYS_US_CA_EN_TV)){
        			mvRatings.add(rating);
        		}
        	}
        	
        	for(TvContentRating rating:mvRatings){
        		if(rating.getMainRating().equals(RatingConstHelper.RATING_US_CA_EN_TV_C)){
        			vRadioButton1.setChecked(true);
        		}else if(rating.getMainRating().equals(RatingConstHelper.RATING_US_CA_EN_TV_C8)){
        			vRadioButton2.setChecked(true);
        		}else if(rating.getMainRating().equals(RatingConstHelper.RATING_US_CA_EN_TV_G)){
        			vRadioButton3.setChecked(true);
        		}else if(rating.getMainRating().equals(RatingConstHelper.RATING_US_CA_EN_TV_PG)){
        			vRadioButton4.setChecked(true);
        		}else if(rating.getMainRating().equals(RatingConstHelper.RATING_US_CA_EN_TV_14)){
        			vRadioButton5.setChecked(true);
        		}else if(rating.getMainRating().equals(RatingConstHelper.RATING_US_CA_EN_TV_18)){
        			vRadioButton6.setChecked(true);
        		}
        	}
        } else {
        	List<TvContentRating> mvRatings = new ArrayList<TvContentRating>();
        	for(TvContentRating rating:currRatings){
        		if(rating.getRatingSystem().equals(RatingConstHelper.RATING_SYS_US_CA_FR_TV)){
        			mvRatings.add(rating);
        		}
        	}
        	
        	for(TvContentRating rating:mvRatings){
        		if(rating.getMainRating().equals(RatingConstHelper.RATING_US_CA_FR_TV_G)){
            		vRadioButton1.setChecked(true);
        		}else if(rating.getMainRating().equals(RatingConstHelper.RATING_US_CA_FR_TV_8)){
        			vRadioButton2.setChecked(true);
        		}else if(rating.getMainRating().equals(RatingConstHelper.RATING_US_CA_FR_TV_13)){
        			vRadioButton3.setChecked(true);
        		}else if(rating.getMainRating().equals(RatingConstHelper.RATING_US_CA_FR_TV_16)){
        			vRadioButton4.setChecked(true);
        		}else if(rating.getMainRating().equals(RatingConstHelper.RATING_US_CA_FR_TV_18)){
        			vRadioButton5.setChecked(true);
        		}
        	}
        	
        }

    }
    
    private void generateRating(String itemID){
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d("RatingOtherView", "generateRating:" + itemID);
    	List<TvContentRating> currRatings = mTvInputManager.getBlockedRatings();
    	if (itemID.equals(MenuConfigManager.PARENTAL_US_MOVIE_RATINGS)){
    		String system = RatingConstHelper.RATING_SYS_US_MV;
    		for(TvContentRating rating:currRatings){
    			if(rating.getRatingSystem().equals(system)){
    				mTvInputManager.removeBlockedRating(rating);
    			}
        	}
    		if(vRadioButton1.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_MV_G);
    			mTvInputManager.addBlockedRating(rating);
    		}else if(vRadioButton2.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_MV_PG);
    			mTvInputManager.addBlockedRating(rating);
    		}else if(vRadioButton3.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_MV_PG13);
    			mTvInputManager.addBlockedRating(rating);
    		}else if(vRadioButton4.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_MV_R);
    			mTvInputManager.addBlockedRating(rating);
    		}else if(vRadioButton5.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_MV_NC17);
    			mTvInputManager.addBlockedRating(rating);
    		}else if(vRadioButton6.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_MV_X);
    			mTvInputManager.addBlockedRating(rating);
    		}
    		for(TvContentRating rating:currRatings){
    			if(rating.getRatingSystem().equals(system)){
    				com.mediatek.wwtv.tvcenter.util.MtkLog.d("RatingOtherView", "generateRating item:" + rating.getMainRating());
    			}
        	}
    	}else if (itemID.equals(MenuConfigManager.PARENTAL_CANADIAN_ENGLISH_RATINGS)){
    		String system = RatingConstHelper.RATING_SYS_US_CA_EN_TV;
    		for(TvContentRating rating:currRatings){
    			if(rating.getRatingSystem().equals(system)){
    				mTvInputManager.removeBlockedRating(rating);
    			}
        	}
    		if(vRadioButton1.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_CA_EN_TV_C);
    			mTvInputManager.addBlockedRating(rating);
    		}else if(vRadioButton2.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_CA_EN_TV_C8);
    			mTvInputManager.addBlockedRating(rating);
    		}else if(vRadioButton3.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_CA_EN_TV_G);
    			mTvInputManager.addBlockedRating(rating);
    		}else if(vRadioButton4.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_CA_EN_TV_PG);
    			mTvInputManager.addBlockedRating(rating);
    		}else if(vRadioButton5.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_CA_EN_TV_14);
    			mTvInputManager.addBlockedRating(rating);
    		}else if(vRadioButton6.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_CA_EN_TV_18);
    			mTvInputManager.addBlockedRating(rating);
    		}
    		for(TvContentRating rating:currRatings){
    			if(rating.getRatingSystem().equals(system)){
    				com.mediatek.wwtv.tvcenter.util.MtkLog.d("RatingOtherView", "generateRating item:" + rating.getMainRating());
    			}
        	}
    	}else{
    		String system = RatingConstHelper.RATING_SYS_US_CA_FR_TV;
    		for(TvContentRating rating:currRatings){
    			if(rating.getRatingSystem().equals(system)){
    				mTvInputManager.removeBlockedRating(rating);
    			}
        	}
    		if(vRadioButton1.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_CA_FR_TV_G);
    			mTvInputManager.addBlockedRating(rating);
    		}else if(vRadioButton2.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_CA_FR_TV_8);
    			mTvInputManager.addBlockedRating(rating);
    		}else if(vRadioButton3.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_CA_FR_TV_13);
    			mTvInputManager.addBlockedRating(rating);
    		}else if(vRadioButton4.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_CA_FR_TV_16);
    			mTvInputManager.addBlockedRating(rating);
    		}else if(vRadioButton5.isChecked()){
    			TvContentRating rating = TvContentRating.createRating(RatingConstHelper.RATING_DOMAIN,system,RatingConstHelper.RATING_US_CA_FR_TV_18);
    			mTvInputManager.addBlockedRating(rating);
    		}
    		for(TvContentRating rating:currRatings){
    			if(rating.getRatingSystem().equals(system)){
    				com.mediatek.wwtv.tvcenter.util.MtkLog.d("RatingOtherView", "generateRating item:" + rating.getMainRating());
    			}
        	}
    		
    	}
    	
    }
}
