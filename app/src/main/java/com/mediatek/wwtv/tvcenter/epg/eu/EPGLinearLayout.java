package com.mediatek.wwtv.tvcenter.epg.eu;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.EPGConfig;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;
import com.mediatek.wwtv.tvcenter.epg.DataReader;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.mediatek.wwtv.tvcenter.util.SaveValue;
import com.mediatek.twoworlds.tv.MtkTvConfig;

public class EPGLinearLayout extends LinearLayout {
	private int mMax;
	private Context mContext;
	private static final String TAG = "EPGLinearLayout";
	private int mWidth;

	public static int mCurrentSelectPosition = 0;
	private List<EPGProgramInfo> childViewData;
	private float curDensity;

	public EPGLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		curDensity = mContext.getResources().getDisplayMetrics().density;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "curDensity: " + curDensity);
	}

	public EPGLinearLayout(Context context) {
		super(context);
		mContext = context;
		curDensity = mContext.getResources().getDisplayMetrics().density;
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "curDensity: " + curDensity);
	}

	public void setWidth(int mWidth) {
		this.mWidth = mWidth;
	}

	public int getmCurrentSelectPosition() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("EPGEuActivity", "mCurrentSelectPosition>>>>" + mCurrentSelectPosition);
		return mCurrentSelectPosition;
	}

	public void setmCurrentSelectPosition(int mCurrentSelectPosition) {
		this.mCurrentSelectPosition = mCurrentSelectPosition;
	}

	public void setAdpter(List<EPGProgramInfo> mChildViewData, boolean flag) {
		this.childViewData = mChildViewData;
		final int width = mWidth;
		removeAllViews();

        float scale = 0.0f;
        int curWidthFromLeft = 0;
        for (int i = 0; i < mChildViewData.size(); i++) {
            EPGTextView textView = (EPGTextView)inflate(mContext, R.layout.epg_eu_listitem_textview, null);
            textView.setProgramInfo(mChildViewData.get(i));

            scale += mChildViewData.get(i).getmScale();
            int childWidth = (int)(width * scale - curWidthFromLeft);
            curWidthFromLeft = (int)(width * scale);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "scale: " + mChildViewData.get(i).getmScale()
                    +", childWidth>>"+childWidth);
            if (childWidth > width) {
                childWidth = width;
            }
           if(childWidth <= 1 && mChildViewData.get(i).getmScale() > 0.0f){
               if((int)curDensity > 1){
                   childWidth = (int)(1*curDensity);
               }else {
                   childWidth = (int)(2*curDensity);
               }
               com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "childWidth <= 1, event duration is too short.");
           }
			// fix show null string DTV01958568
			if (childWidth < (int)(30*curDensity)) {
				textView.setTextSize(mContext.getResources().getDimension(R.dimen.epg_short_event_ts));
			}

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					childWidth, LayoutParams.MATCH_PARENT);
			layoutParams.leftMargin = (int) (mChildViewData.get(i)
					.getLeftMargin()*width);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----layoutParams.leftMargin---->"
					+ layoutParams.leftMargin);

      String noProgramTitle = mContext.getResources().getString(R.string.nav_epg_no_program_title);
      String country = CommonIntegration.getInstance().getCountryCode();
      if (country != null && (country.equals(EpgType.COUNTRY_DNK)
          || country.equals(EpgType.COUNTRY_FIN)
          || country.equals(EpgType.COUNTRY_IRL)
          || country.equals(EpgType.COUNTRY_NOR)
          ||country.equals(EpgType.COUNTRY_SWE))) {
        noProgramTitle = "";
      }
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----noProgramTitle--->" + noProgramTitle);
      String title=TextUtils.isEmpty(textView.getShowTitle())?noProgramTitle:textView.getShowTitle();
      textView.setText(title);

			int mainType = textView.getMainTypeStr();
			int subType = textView.getSubTypeStr();
			String mainStr = null;
			String subStr = null;
			boolean isFiltered = false;
			//according to spec, 0x0 is undefined content
			if(0 <= mainType)
			{
				if (mainType >= 0 && mainType < DataReader.getInstance(mContext).getMainType().length) {
					mainStr = DataReader.getInstance(mContext).getMainType()[mainType];
					String[][] subTypeArr = DataReader.getInstance(mContext).getSubType();
					if (subTypeArr != null && subTypeArr.length > mainType 
					    && subTypeArr[mainType] != null && subType >= 0
					    && subType < DataReader.getInstance(mContext).getSubType()[mainType].length) {
						subStr = DataReader.getInstance(mContext).getSubType()[mainType][subType];
					}
				}
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mainStr="+mainStr+",subStr="+subStr);
				if (mainStr != null) {
					String key = String.format("%s%s", EPGConfig.PREFIX, mainStr);
					if (SaveValue.getInstance(mContext).readBooleanValue(key, false) || subHasSelected(mainType)) {
						isFiltered = true;
					} else {
						isFiltered = false;
					}
				} else {
					isFiltered = false;
				}
			}
			childViewData.get(i).setBgHighLigth(isFiltered);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----title---->"+ textView.getShowTitle()+"===>"+mCurrentSelectPosition
					+ "    " + mainType + "   " + subType + "	 " + mainStr + "	" + subStr);
			if (i == mCurrentSelectPosition && flag) {
                textView.setBackground(isFiltered, true);
			} else {
                textView.setBackground(isFiltered,false);
			}
			if(!mChildViewData.isEmpty()&&i<mChildViewData.size()-1){
				LinearLayout viewGroup=new LinearLayout(mContext);
				LinearLayout.LayoutParams viewGroupLp = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        layoutParams.width=childWidth-(int)(1*curDensity);
				viewGroup.addView(textView,0, layoutParams);
				LinearLayout.LayoutParams diverLp = new LinearLayout.LayoutParams(
            (int)(1*curDensity), LayoutParams.MATCH_PARENT);
				View diverView=new View(mContext);
				diverView.setBackgroundColor(mContext.getResources().getColor(R.color.epg_divider));
				viewGroup.addView(diverView,1, diverLp);
				addView(viewGroup, i, viewGroupLp);
			}
			else{
				layoutParams.width=childWidth;
				addView(textView, i, layoutParams);
			}
		}
	}

	private EPGTextView getEPGTextView(View childView){
		EPGTextView epgTextView=null;
		if(childView instanceof EPGTextView){
			epgTextView=(EPGTextView) childView;
		}
		else {
			LinearLayout viewGroup=(LinearLayout)childView;
			epgTextView=(EPGTextView)viewGroup.getChildAt(0);
		}
		return epgTextView;
	}

	private boolean subHasSelected(int mainTypeIndex) {
	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mainTypeIndex>>>"+mainTypeIndex);
    String[][] subTypeArr = DataReader.getInstance(mContext).getSubType();
    if(subTypeArr != null && subTypeArr.length > mainTypeIndex){
      String subType[] = DataReader.getInstance(mContext).getSubType()[mainTypeIndex];
      if (subType != null) {
        for (int i = 0; i < subType.length; i++) {
        	String key = String.format("%s%s%s", EPGConfig.PREFIX, DataReader.getInstance(mContext).getMainType()[mainTypeIndex], subType[i]);
          if (SaveValue.getInstance(mContext).readBooleanValue(key, false)) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subType i>>>>"+i+",subType[i]>>>"+subType[i]);
            return true;
          }
        }
      }
    }
		return false;
	}

	public void setSelectedPosition(int index) {
		View childView;
		if (mCurrentSelectPosition != -1) {
			childView = getChildAt(mCurrentSelectPosition);
			if (childView != null
					&& mCurrentSelectPosition < childViewData.size()) {
         EPGProgramInfo childProgramInfo = childViewData.get(mCurrentSelectPosition);
            getEPGTextView(childView).setBackground(childProgramInfo.isBgHighLight(), false);
			}
		}
		childView = getChildAt(index);
		if (childView != null) {
			EPGProgramInfo childProgramInfo = childViewData.get(index);
      // getEPGTextView(childView).setBackground(childProgramInfo
      // .isDrawLeftIcon(), childProgramInfo.isDrawRightwardIcon(),
      // true, false);
            getEPGTextView(childView).setBackground(childProgramInfo.isBgHighLight(),true);
			mCurrentSelectPosition = index;
		}
	}

	public void clearSelected() {
		View childView;
		if (mCurrentSelectPosition != -1) {
			childView = getChildAt(mCurrentSelectPosition);
			if (childView != null
					&& mCurrentSelectPosition < childViewData.size()) {
         EPGProgramInfo childProgramInfo = childViewData
         .get(mCurrentSelectPosition);
        // getEPGTextView(childView).setBackground(childProgramInfo
        // .isDrawLeftIcon(), childProgramInfo
        // .isDrawRightwardIcon(), false, childProgramInfo.isBgHighLight());
                getEPGTextView(childView).setBackground(childProgramInfo.isBgHighLight(),false);
			}
//			mCurrentSelectPosition = -1;
		}
	}

	public boolean onKeyLeft() {
		if (childViewData==null) {
			return false;
		}
		View childView;
		if (mCurrentSelectPosition != -1) {
			childView = getChildAt(mCurrentSelectPosition);
			if (childView != null
					&& mCurrentSelectPosition < childViewData.size()) {
         EPGProgramInfo childProgramInfo = childViewData
         .get(mCurrentSelectPosition);
        // getEPGTextView(childView).setBackground(childProgramInfo
        // .isDrawLeftIcon(), childProgramInfo
        // .isDrawRightwardIcon(), false, childProgramInfo.isBgHighLight());
                getEPGTextView(childView).setBackground(childProgramInfo.isBgHighLight(),false);
			} else {
				return false;
			}

			if (mCurrentSelectPosition > 0) {
				--mCurrentSelectPosition;
				childView = getChildAt(mCurrentSelectPosition);
				if (childView != null
						&& mCurrentSelectPosition < childViewData.size()) {
           EPGProgramInfo childProgramInfo = childViewData
           .get(mCurrentSelectPosition);
          // getEPGTextView(childView).setBackground(childProgramInfo
          // .isDrawLeftIcon(), childProgramInfo
          // .isDrawRightwardIcon(), true, false);
                    getEPGTextView(childView).setBackground(childProgramInfo.isBgHighLight(),true);
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	public boolean onKeyRight() {
		if (childViewData==null) {
			return false;
		}
		View childView;
		final int count = getChildCount();
		if (mCurrentSelectPosition != -1) {
			childView = getChildAt(mCurrentSelectPosition);
			if (childView != null
					&& mCurrentSelectPosition < childViewData.size()) {
         EPGProgramInfo childProgramInfo = childViewData
         .get(mCurrentSelectPosition);
        // getEPGTextView(childView).setBackground(childProgramInfo
        // .isDrawLeftIcon(), childProgramInfo
        // .isDrawRightwardIcon(), false, childProgramInfo.isBgHighLight());
                getEPGTextView(childView).setBackground(childProgramInfo.isBgHighLight(),false);
			} else {
				return false;
			}
			if (mCurrentSelectPosition < count - 1
					&& mCurrentSelectPosition < childViewData.size() - 1) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "=======onKeyRight=============count========="
						+ count);
				++mCurrentSelectPosition;
				childView = getChildAt(mCurrentSelectPosition);
				if (childView != null) {
           EPGProgramInfo childProgramInfo = childViewData
           .get(mCurrentSelectPosition);
          // getEPGTextView(childView).setBackground(childProgramInfo
          // .isDrawLeftIcon(), childProgramInfo
          // .isDrawRightwardIcon(), true, false);
                    getEPGTextView(childView).setBackground(childProgramInfo.isBgHighLight(),true);
				} else {
					return false;
				}
			} else {
				return false;
			}
			return true;
		}
		return true;
	}

	public void refreshTextLayout(int currentChannelIndex) {
		if (childViewData == null) {
			return;
		}
		int count = getChildCount();
		for (int i = 0; i < count; i++) {


			EPGTextView textView =getEPGTextView(getChildAt(i));
			if (textView != null) {
				int mainType = textView.getMainTypeStr();
				int subType = textView.getSubTypeStr();
				String mainStr = null;
				String subStr = null;
				boolean isFiltered = false;
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " mainType >>" + mainType + ", subType: "+subType);
				//according to spec, 0x0 is undefined content
				if(0 <= mainType)
				{
					if (mainType >= 0 && mainType < DataReader.getInstance(mContext).getMainType().length) {
						mainStr = DataReader.getInstance(mContext).getMainType()[mainType];
	          String[][] subTypeArr = DataReader.getInstance(mContext).getSubType();
	          if (subTypeArr != null && subTypeArr.length > mainType 
	              && subTypeArr[mainType] != null && subType >= 0
	              && subType < DataReader.getInstance(mContext).getSubType()[mainType].length) {
							subStr = DataReader.getInstance(mContext).getSubType()[mainType][subType];
						}
					}
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " mainStr >>" + mainStr + ", subStr: "+subStr);
					if (mainStr != null) {
						String key = String.format("%s%s", EPGConfig.PREFIX, mainStr);
						if (SaveValue.getInstance(mContext).readBooleanValue(key, false) || subHasSelected(mainType)) {
							isFiltered = true;
						} else {
							isFiltered = false;
						}
					} else {
						isFiltered = false;
					}
				}
				childViewData.get(i).setBgHighLigth(isFiltered);
				if (i == mCurrentSelectPosition && EPGConfig.SELECTED_CHANNEL_POSITION == currentChannelIndex) {
                    textView.setBackground(isFiltered, true);
				} else {
                    textView.setBackground(isFiltered, false);
				}
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

	}

	public int getMax() {
		return mMax;
	}

	public void setMax(int mMax) {
		this.mMax = mMax;
	}

}
