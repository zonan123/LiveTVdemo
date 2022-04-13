package com.mediatek.wwtv.tvcenter.epg.sa;

import java.util.List;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;
import com.mediatek.wwtv.tvcenter.epg.sa.db.DBMgrProgramList;
import com.mediatek.wwtv.tvcenter.epg.sa.db.EPGBookListViewDataItem;


import android.content.Context;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EpgProgramItemView extends LinearLayout{
	private static String TAG = "EpgProgramItemView";
	private Context mContext;
	private ImageView mBookIcon;
	private EPGTextView mEPGTextView;
	private EPGProgramInfo tvProgramInfo;
    private TextView mEPGDivider;

	public EpgProgramItemView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public EpgProgramItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public EpgProgramItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}
	
	public EpgProgramItemView(Context context, EPGProgramInfo mTVProgramInfo) {
		super(context);
		mContext = context;
		this.tvProgramInfo = mTVProgramInfo;
		init();
	}

	 private void init() {
		 LinearLayout lv = (LinearLayout) inflate(mContext,R.layout.epg_program_text_item_layout, null);
	     LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	     addView(lv, params);
	     mBookIcon = (ImageView) lv.findViewById(R.id.epg_book_icon);
	     mBookIcon.setVisibility(View.GONE);
	     mEPGTextView = (EPGTextView) lv.findViewById(R.id.epg_textview);
	     mEPGTextView.setEllipsize(TextUtils.TruncateAt.END);
	     mEPGTextView.setProgram(tvProgramInfo);
         mEPGDivider = (TextView) lv.findViewById(R.id.epg_divider);
	 }

		public void setTextSize(float size) {
			mEPGTextView.setTextSize(size);
		}

		public void hideDivider() {
			mEPGDivider.setVisibility(View.GONE);
		}
		
		public void setBackground(boolean isTypeFiltered, boolean selected) {
			    if (selected) {
				  this.setBackgroundColor(getResources().getColor(R.color.epg_lv_item_bg_color_selected));
				  mEPGTextView.setTextColor(getResources().getColor(R.color.epg_ts_bright_white));
				  this.setEllipsize(true);
			    } else if(isTypeFiltered){
			      this.setBackgroundColor(getResources().getColor(R.color.epg_event_filtered_bg_color));
			      mEPGTextView.setTextColor(getResources().getColor(R.color.epg_ts_grey_white));
			      this.setEllipsize(false);
			    } else {
			      this.setBackgroundColor(getResources().getColor(R.color.epg_lv_item_bg_color_unselected));
			      mEPGTextView.setTextColor(getResources().getColor(R.color.epg_ts_grey_white));
			      this.setEllipsize(false);
				}
	    }
		
	    public void setEllipsize(boolean selected) {
		    if (selected) {
		    	mEPGTextView.setEllipsize(TruncateAt.MARQUEE);
		    	mEPGTextView.setMarqueeRepeatLimit(-1);
		    	mEPGTextView.setSingleLine();
		    	mEPGTextView.setClickable(true);
		    } else {
		    	mEPGTextView.setEllipsize(TextUtils.TruncateAt.END);
		    }
	    }

	public void setBackground(boolean isDrawLeftIcon, boolean isDrawRightwardIcon, boolean selected, boolean isBgHighLight) {
//		mEPGTextView.setBackground(drawLeftIcon, drawRightwardIcon, selected);
//		boolean istypefilter = false;
//		if (istypefilter) {
//			if (isDrawRightwardIcon && isDrawLeftIcon) {
//				if (selected) {
//					setBackgroundResource(R.drawable.epg_left_right_hi);
//				} else {
//					setBackgroundResource(R.drawable.npg_img_pbb_cate_1_r_ar);
//				}
//			} else if (isDrawLeftIcon) {
//				if (selected) {
//					setBackgroundResource(R.drawable.epg_left_high);
//				} else {
//					setBackgroundResource(R.drawable.npg_img_pbb_cate_1_l_ar);
//				}
//			} else if (isDrawRightwardIcon) {
//				if (selected) {
//					setBackgroundResource(R.drawable.epg_right_high);
//				} else {
//					setBackgroundResource(R.drawable.npg_img_pbb_cate_1_lr_ar);
//				}
//			} else {
//				if (selected) {
//					setBackgroundResource(R.drawable.epg_textviewselected);
//				} else {
//					setBackgroundResource(R.drawable.epg_textviewnormal);
//				}
//			}
//		} else {
		if (isBgHighLight) {
			if (isDrawRightwardIcon && isDrawLeftIcon) {
				if (selected) {
					setBackgroundResource(R.drawable.epg_left_right);
				} else {
					setBackgroundResource(R.drawable.epg_img_pbb_cate_1_lr_ar);
				}
			} else if (isDrawLeftIcon) {
				if (selected) {
					setBackgroundResource(R.drawable.epg_left_hi);
				} else {
					setBackgroundResource(R.drawable.epg_img_pbb_cate_1_l_ar);
				}
			} else if (isDrawRightwardIcon) {
				if (selected) {
					setBackgroundResource(R.drawable.epg_right_hi);
				} else {
					setBackgroundResource(R.drawable.epg_img_pbb_cate_1_r_ar);
				}
			} else {
				if (selected) {
					setBackgroundResource(R.drawable.epg_list_item);
				} else {
					setBackgroundResource(R.drawable.epg_img_pbb_cate_1);
				}
			}
		} else {
			if (isDrawRightwardIcon && isDrawLeftIcon) {
				if (selected) {
					setBackgroundResource(R.drawable.epg_left_right);
				} else {
					setBackgroundResource(R.drawable.epg_left_right_no);
				}
			} else if (isDrawLeftIcon) {
				if (selected) {
					setBackgroundResource(R.drawable.epg_left_hi);
				} else {
					setBackgroundResource(R.drawable.epg_left_no);
				}
			} else if (isDrawRightwardIcon) {
				if (selected) {
					setBackgroundResource(R.drawable.epg_right_hi);
				} else {
					setBackgroundResource(R.drawable.epg_right_no);
				}
			} else {
				if (selected) {
					setBackgroundResource(R.drawable.epg_list_item);
				} else {
					setBackgroundResource(R.drawable.epg_list_item_normal);
				}
			}
		}
//		}
	}

	public String getShowTitle() {
		return mEPGTextView.getShowTitle();
	}

	public void setText(String showTitle) {
		mEPGTextView.setText(showTitle);
	}

	public int getMainTypeStr() {
		return mEPGTextView.getMainTypeStr();
	}

	public int getSubTypeStr() {
		return mEPGTextView.getSubTypeStr();
	}

	public void setTextColor(int red) {
		mEPGTextView.setTextColor(red);
	}

	public void setBookVisibility() {
		if (tvProgramInfo == null) {
			return;
		}
		DBMgrProgramList mDBMgrProgramList = DBMgrProgramList.getInstance(mContext);
		mDBMgrProgramList.getReadableDB();
		List<EPGBookListViewDataItem> mBookedList = mDBMgrProgramList.getProgramList();
		mDBMgrProgramList.closeDB();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setBookVisibility>>>" + mBookedList.size());
		boolean hasFind = false;
		for (EPGBookListViewDataItem tempInfo:mBookedList) {
			if(tempInfo.mProgramStartTime == tvProgramInfo.getmStartTime() && tempInfo.mChannelId == tvProgramInfo.getChannelId()
					&& tempInfo.mProgramId == tvProgramInfo.getProgramId()) {
				hasFind = true;
				break;
			}
		}
		if (hasFind) {
			mBookIcon.setVisibility(View.VISIBLE);
		} else {
			mBookIcon.setVisibility(View.GONE);
		}
	}

	

}
