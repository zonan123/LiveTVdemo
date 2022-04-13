package com.mediatek.wwtv.tvcenter.epg.sa;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.EPGConfig;
import com.mediatek.wwtv.tvcenter.epg.EPGProgramInfo;
import com.mediatek.wwtv.tvcenter.epg.DataReader;

import com.mediatek.wwtv.tvcenter.util.SaveValue;

public class EPGLinearLayout extends LinearLayout {
	private int mMax;
	private Context mContext;
	private static final String TAG = "EPGLinearLayout";
	private int mWidth;

	public static int mCurrentSelectPosition = 0;
	private List<EPGProgramInfo> childViewData;

	public EPGLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public EPGLinearLayout(Context context) {
		super(context);
		mContext = context;
	}

	public void setWidth(int mWidth) {
		this.mWidth = mWidth;
	}

	public int getmCurrentSelectPosition() {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d("EPGSaActivity", "mCurrentSelectPosition>>>>" + mCurrentSelectPosition);
		return mCurrentSelectPosition;
	}

	public void setmCurrentSelectPosition(int mCurrentSelectPosition) {
		this.mCurrentSelectPosition = mCurrentSelectPosition;
	}

	public void setAdapterByEpgProgramItemView(List<EPGProgramInfo> mChildViewData, boolean flag) {
		this.childViewData = mChildViewData;
		final int width = mWidth;
		removeAllViews();
		for (int i = 0; i < mChildViewData.size(); i++) {
			EpgProgramItemView eTextView = new EpgProgramItemView(mContext, mChildViewData.get(i));
			int childWidth = (int) (width * mChildViewData.get(i).getmScale());
			com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "setAdpter-----layoutParams.width---->"+width+"==>"+mChildViewData.get(i).getmScale()
                    +"===>"+ width * mChildViewData.get(i).getmScale());
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(childWidth, LayoutParams.MATCH_PARENT);
			layoutParams.leftMargin =(int) (mChildViewData.get(i).getLeftMargin()*width);
            if (i == mChildViewData.size() - 2) {
                if (childWidth > width - 10) {
                    childWidth = childWidth - 20;
                }
            }
            if(childWidth <= 0 && i == mChildViewData.size() - 1){
                childWidth = 21;
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "childWidth <= 0, fixed");
            }
            // fix show null string DTV01958568
            if (childWidth < 30) {
        	   eTextView.setTextSize(6);
            }
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----layoutParams.leftMargin---->" + layoutParams.leftMargin
					+ "   " + flag + "   " + mCurrentSelectPosition + "  " + childViewData.get(i).isDrawLeftIcon());
			String title = eTextView.getShowTitle();
			eTextView.setText((title==null || title.equals(""))?mContext.getString(R.string.nav_epg_no_title):title);
			int mainType = eTextView.getMainTypeStr();
			int subType = eTextView.getSubTypeStr();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----mainType---->" + mainType+",subType>>>>"+subType);
			String mainStr = null;
			String subStr = null;
			boolean isFiltered = false;
			if(mainType >= 0){
				if (mainType < DataReader.getInstance(mContext).getMainType().length) {
				  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----DataReader.getInstance(mContext).getMainType().length---->"
				+ DataReader.getInstance(mContext).getMainType().length);
					mainStr = DataReader.getInstance(mContext).getMainType()[mainType];
					if (subType >= 0 && subType < DataReader.getInstance(mContext).getSubType().length) {
						subStr = DataReader.getInstance(mContext).getSubType()[mainType][subType];
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----DataReader.getInstance(mContext).getSubType().length---->"
				        + DataReader.getInstance(mContext).getSubType().length);
					}
				}
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----mainStr---->" + mainStr+",subStr>>>>"+subStr +",save ?>>>"+
            SaveValue.getInstance(mContext).readBooleanValue(mainStr, false));
				if (mainStr != null) {
					String key = String.format("%s%s", EPGConfig.PREFIX, mainStr);
					if (SaveValue.getInstance(mContext).readBooleanValue(key, false) || thisSubSelected(mainType, subType)) {
						isFiltered = true;
					} else {
						isFiltered = false;
					}
				} else {
					isFiltered = false;
				}
			}
			childViewData.get(i).setBgHighLigth(isFiltered);
//			eTextView.setBookVisibility();
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----title---->"+ eTextView.getShowTitle()+"===>"+mCurrentSelectPosition
					+ "    " + mainType + "   " + subType + "    " + mainStr + "    " + subStr +",isFiltered>>>"+isFiltered);
			if (i == mCurrentSelectPosition && flag == true) {
				eTextView.setBackground(isFiltered, true);
			} else {
				eTextView.setBackground(isFiltered, false);
			}
			if(childViewData.size() == 1 || i == childViewData.size()-1){
				eTextView.hideDivider();
			}
			addView(eTextView, i, layoutParams);
		}
	}

	public void setAdpter(List<EPGProgramInfo> mChildViewData, boolean flag) {
		this.childViewData = mChildViewData;
		final int width = mWidth;
		for (int i = 0; i < mChildViewData.size(); i++) {

			EPGTextView textView = new EPGTextView(mContext, mChildViewData
					.get(i));
			int childWidth = (int) (width * mChildViewData.get(i).getmScale());
	         com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "setAdpter-----layoutParams.width---->"+width+"==>"+mChildViewData.get(i).getmScale()
	                    +"===>"+ width * mChildViewData.get(i).getmScale());
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					childWidth, LayoutParams.MATCH_PARENT);
			layoutParams.leftMargin =(int) (mChildViewData.get(i).getLeftMargin()*width);
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----layoutParams.leftMargin---->"
					+ layoutParams.leftMargin);
			if (i == mCurrentSelectPosition && flag == true) {
				textView.setBackground(childViewData.get(i).isDrawLeftIcon(),
						childViewData.get(i).isDrawRightwardIcon(), true);
			} else {
				textView.setBackground(childViewData.get(i).isDrawLeftIcon(),
						childViewData.get(i).isDrawRightwardIcon(), false);
			}
			textView.setText(textView.getShowTitle());
			int mainType = textView.getMainTypeStr();
			int subType = textView.getSubTypeStr();
			String mainStr = null;
			String subStr = null;
			if (mainType >= 0 && mainType < DataReader.getInstance(mContext).getMainType().length) {
				mainStr = DataReader.getInstance(mContext).getMainType()[mainType];
				if (subType >= 0 && subType < DataReader.getInstance(mContext).getSubType().length) {
					subStr = DataReader.getInstance(mContext).getSubType()[mainType][subType];
				}
			}
			if (mainStr != null) {
				if (SaveValue.getInstance(mContext).readBooleanValue(mainStr, false) && !subHasSelected(mainType,subType)) {
					textView.setTextColor(Color.RED);
				} else if (SaveValue.getInstance(mContext).readBooleanValue(mainStr, false) && thisSubSelected(mainType, subType)) {
					textView.setTextColor(Color.RED);
//				} else {
//					setBackgroundColor(Color.GREEN);
				}
//			} else {
//				setBackgroundColor(Color.GREEN);
			}
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----title---->"+ textView.getShowTitle()+"===>"+mCurrentSelectPosition
					+ "    " + mainType + "   " + subType + "    " + mainStr + "    " + subStr);
			addView(textView, i, layoutParams);
		}
	}

	private boolean subHasSelected(int mainTypeIndex,int subTypeIndex) {
		String subType[] = DataReader.getInstance(mContext).getSubType()[mainTypeIndex];
		if (subTypeIndex>=0 && subType != null) {
		  for (String string : subType) {
		    if (SaveValue.getInstance(mContext).readBooleanValue(string, false)) {
		      return true;
		    }
        
      }
		}
		return false;
	}

	private boolean thisSubSelected(int mainTypeIndex, int subTypeIndex) {
		String subType[] = DataReader.getInstance(mContext).getSubType()[mainTypeIndex];
		if (subType != null) {
		  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subType.length>>>>"+subType.length + ",subTypeIndex>>>"+subTypeIndex);
		  for(int i =0;i<subType.length;i++){
		    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subType i>>>>"+i+",subType[i]>>>"+subType[i]);
			String key = String.format("%s%s%s", EPGConfig.PREFIX, DataReader.getInstance(mContext).getMainType()[mainTypeIndex], subType[i]);
		    if(SaveValue.getInstance(mContext).readBooleanValue(key, false)){
		      return true;
		    }
		  }
		  return false;
		}
		return false;
	}

	public void setAdpterByLayout(int startTime, int dayNum) {
		childViewData = null;
		removeAllViews();
        EpgProgramItemView textView = new EpgProgramItemView(mContext);
        int childWidth = 300;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(childWidth, LayoutParams.MATCH_PARENT);
        layoutParams.leftMargin = 20;
        textView.setText(mContext.getString(R.string.nav_epg_no_program_data));
        addView(textView, 0, layoutParams);
        textView.hideDivider();
    }

	   public void setAdpter() {

	            EPGTextView textView = new EPGTextView(mContext);
	            int childWidth = 300;

	            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
	                    childWidth, LayoutParams.MATCH_PARENT);
	            layoutParams.leftMargin = 20;
	            textView.setText(mContext.getString(R.string.nav_epg_no_program_data));

	            addView(textView, 0, layoutParams);
	    }

	public void setSelectedPosition(int index) {
		View childView;
		if (mCurrentSelectPosition != -1) {
			childView = getChildAt(mCurrentSelectPosition);
			if (childViewData==null) {
                return;
            }
			if (childView != null
					&& mCurrentSelectPosition < childViewData.size()) {
				EPGProgramInfo childProgramInfo = childViewData
						.get(mCurrentSelectPosition);
				((EpgProgramItemView) childView).setBackground(childProgramInfo.isBgHighLight(),false);
			}
		}
		childView = getChildAt(index);
		if (childView != null) {
			if (childViewData != null) {
				EPGProgramInfo childProgramInfo = childViewData.get(index);
				((EpgProgramItemView) childView).setBackground(childProgramInfo.isBgHighLight(),true);
				mCurrentSelectPosition = index;
			} else {
				mCurrentSelectPosition = 0;
			}
		}
	}

	public void clearSelected() {
		View childView;
		if (mCurrentSelectPosition != -1) {
			childView = getChildAt(mCurrentSelectPosition);
			if (childViewData==null) {
                return;
            }
			if (childView != null
					&& mCurrentSelectPosition < childViewData.size()) {
				EPGProgramInfo childProgramInfo = childViewData
						.get(mCurrentSelectPosition);
				((EpgProgramItemView) childView).setBackground(childProgramInfo.isBgHighLight(),false);
			}
			mCurrentSelectPosition = -1;
		}
	}

	public boolean onKeyLeft() {
		View childView;
		if (childViewData==null) {
//			if (mDayNum > 0) {
//				if (mStartTime >= EPGConfig.mTimeSpan) {
//					return false;
//				}
//			} else {
//				if (mStartTime > DataReader.getInstanceWithoutContext().getFirstDayStartTime()) {
//					return false;
//				}
//			}
            return false;
        }
		if (mCurrentSelectPosition != -1) {
			childView = getChildAt(mCurrentSelectPosition);
			if (childView != null
					&& mCurrentSelectPosition < childViewData.size()) {
				EPGProgramInfo childProgramInfo = childViewData
						.get(mCurrentSelectPosition);
				((EpgProgramItemView) childView).setBackground(childProgramInfo.isBgHighLight(),false);
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
					((EpgProgramItemView) childView).setBackground(childProgramInfo.isBgHighLight(),true);
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
		View childView;
		final int count = getChildCount();
		if (childViewData==null) {
            return false;
        }
		if (mCurrentSelectPosition != -1) {
			childView = getChildAt(mCurrentSelectPosition);
			if (childView != null
					&& mCurrentSelectPosition < childViewData.size()) {
				EPGProgramInfo childProgramInfo = childViewData
						.get(mCurrentSelectPosition);
				((EpgProgramItemView) childView).setBackground(childProgramInfo.isBgHighLight(),false);
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
					((EpgProgramItemView) childView).setBackground(childProgramInfo.isBgHighLight(),true);
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

	

	public String getProgramTypeByProgram(EPGProgramInfo program) {
        if (program == null) {
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "null == program");
            return "";
        }
        String[] mainType = mContext.getResources().getStringArray(
                R.array.nav_epg_filter_sa_type);
	    int index = program.getMainType();
	    if (index >= 0 && index < mainType.length) {
		    return mainType[index];
	    }
		return "";
	}

	public void refreshTextLayout(int currentChannelIndex) {
		if (childViewData == null) {
			return;
		}
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			EpgProgramItemView textView = (EpgProgramItemView) getChildAt(i);
			if (textView != null) {
				int mainType = textView.getMainTypeStr();
				int subType = textView.getSubTypeStr();
	      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----mainType---->" + mainType+",subType>>>>"+subType);
				String mainStr = null;
				boolean isFiltered = false;
				if(mainType >= 0){
					if (mainType < DataReader.getInstance(mContext).getMainType().length) {
						mainStr = DataReader.getInstance(mContext).getMainType()[mainType];
					}
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter---mainType-->" + mainType+",subType>>>>"+subType);
					if (mainStr != null) {
						String key = String.format("%s%s", EPGConfig.PREFIX, mainStr);
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter----readBooleanValue>>>"+
								SaveValue.getInstance(mContext).readBooleanValue(key, false));
						if (SaveValue.getInstance(mContext).readBooleanValue(key, false) || thisSubSelected(mainType, subType)) {
							isFiltered = true;
						} else {
							isFiltered = false;
						}
					} else {
						isFiltered = false;
					}
				}
				childViewData.get(i).setBgHighLigth(isFiltered);
//				textView.setBookVisibility();
				if (i == mCurrentSelectPosition && EPGConfig.SELECTED_CHANNEL_POSITION == currentChannelIndex) {
					textView.setBackground(isFiltered, true);
				} else {
					textView.setBackground(isFiltered, false);
				}
			}
		}
	}
}
