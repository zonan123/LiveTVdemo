package com.mediatek.wwtv.tvcenter.epg.cn;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.epg.EPGChannelInfo;
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
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mCurrentSelectPosition>>>>" + mCurrentSelectPosition);
		return mCurrentSelectPosition;
	}

	public void setmCurrentSelectPosition(int mCurrentSelectPosition) {
		this.mCurrentSelectPosition = mCurrentSelectPosition;
	}

	public void setAdpter(List<EPGProgramInfo> mChildViewData, boolean flag) {
		this.childViewData = mChildViewData;
		final int width = mWidth;
		removeAllViews();
		for (int i = 0; i < mChildViewData.size(); i++) {
			EPGTextView textView = (EPGTextView)inflate(mContext, R.layout.epg_cn_listitem_textview, null);
			textView.setProgramInfo(mChildViewData.get(i));
			int childWidth = (int) (width * mChildViewData.get(i).getmScale());

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					childWidth, LayoutParams.MATCH_PARENT);
			layoutParams.leftMargin = (int) (mChildViewData.get(i)
					.getLeftMargin()*width);
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setAdpter-----layoutParams.leftMargin---->"
					+ layoutParams.leftMargin);
//			if (i == mCurrentSelectPosition && flag == true) {
//				textView.setBackground(childViewData.get(i).isDrawLeftIcon(),
//						childViewData.get(i).isDrawRightwardIcon(), true);
//			} else {
//				textView.setBackground(childViewData.get(i).isDrawLeftIcon(),
//						childViewData.get(i).isDrawRightwardIcon(), false);
//			}
			textView.setText(textView.getShowTitle());
			int mainType = textView.getMainTypeStr();
			int subType = textView.getSubTypeStr();
			String mainStr = null;
			String subStr = null;
			//according to spec, 0x0 is undefined content
			if(1 <= mainType)
			{
				if (mainType >= 0 && mainType < DataReader.getInstance(mContext).getMainType().length) {
					mainStr = DataReader.getInstance(mContext).getMainType()[mainType-1];
					if (subType >= 0 && subType < DataReader.getInstance(mContext).getSubType().length) {
						subStr = DataReader.getInstance(mContext).getSubType()[mainType][subType];
					}
				}
				if (mainStr != null) {
					if (SaveValue.getInstance(mContext).readBooleanValue(mainStr, false) && !subHasSelected(mainType)) {
						childViewData.get(i).setBgHighLigth(true);
//						textView.setTextColor(Color.RED);
					} else if (SaveValue.getInstance(mContext).readBooleanValue(mainStr, false) && thisSubSelected(mainType, subType)) {
						childViewData.get(i).setBgHighLigth(true);
//						textView.setTextColor(Color.RED);
					} else {
						childViewData.get(i).setBgHighLigth(false);
					}
				} else {
					childViewData.get(i).setBgHighLigth(false);
				}
			} else {
				childViewData.get(i).setBgHighLigth(false);
			}
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----title---->"+ textView.getShowTitle()+"===>"+mCurrentSelectPosition
					+ "    " + mainType + "   " + subType + "	 " + mainStr + "	" + subStr);
			if (i == mCurrentSelectPosition && flag == true) {
				textView.setBackground(childViewData.get(i).isDrawLeftIcon(),
						childViewData.get(i).isDrawRightwardIcon(), true, false);
			} else {
				textView.setBackground(childViewData.get(i).isDrawLeftIcon(),
						childViewData.get(i).isDrawRightwardIcon(), false, childViewData.get(i).isBgHighLight());
			}
			addView(textView, i, layoutParams);
		}
	}

  private boolean subHasSelected(int mainTypeIndex) {
    String subType[] = DataReader.getInstance(mContext).getSubType()[mainTypeIndex];
    if (subType != null) {
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
			if (subTypeIndex >= 0 && subTypeIndex < subType.length) {
				return SaveValue.getInstance(mContext).readBooleanValue(subType[subTypeIndex], false);
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
				EPGProgramInfo childProgramInfo = childViewData
						.get(mCurrentSelectPosition);
				((EPGTextView) childView).setBackground(childProgramInfo
						.isDrawLeftIcon(), childProgramInfo
						.isDrawRightwardIcon(), false, childProgramInfo.isBgHighLight());
			}
		}
		childView = getChildAt(index);
		if (childView != null) {
			EPGProgramInfo childProgramInfo = childViewData.get(index);
			((EPGTextView) childView).setBackground(childProgramInfo
					.isDrawLeftIcon(), childProgramInfo.isDrawRightwardIcon(),
					true, false);
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
				((EPGTextView) childView).setBackground(childProgramInfo
						.isDrawLeftIcon(), childProgramInfo
						.isDrawRightwardIcon(), false, childProgramInfo.isBgHighLight());
			}
			mCurrentSelectPosition = -1;
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
				((EPGTextView) childView).setBackground(childProgramInfo
						.isDrawLeftIcon(), childProgramInfo
						.isDrawRightwardIcon(), false, childProgramInfo.isBgHighLight());
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
					((EPGTextView) childView).setBackground(childProgramInfo
							.isDrawLeftIcon(), childProgramInfo
							.isDrawRightwardIcon(), true, false);
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
				((EPGTextView) childView).setBackground(childProgramInfo
						.isDrawLeftIcon(), childProgramInfo
						.isDrawRightwardIcon(), false, childProgramInfo.isBgHighLight());
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
					((EPGTextView) childView).setBackground(childProgramInfo
							.isDrawLeftIcon(), childProgramInfo
							.isDrawRightwardIcon(), true, false);
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
			EPGTextView textView = (EPGTextView) getChildAt(i);
			if (textView != null) {
				int mainType = textView.getMainTypeStr();
				int subType = textView.getSubTypeStr();
				String mainStr = null;
				String subStr = null;
				//according to spec, 0x0 is undefined content
				if(1 <= mainType)
				{
					if (mainType >= 0 && mainType < DataReader.getInstance(mContext).getMainType().length) {
						mainStr = DataReader.getInstance(mContext).getMainType()[mainType-1];
						if (subType >= 0 && subType < DataReader.getInstance(mContext).getSubType().length) {
							subStr = DataReader.getInstance(mContext).getSubType()[mainType][subType];
              com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG,"subStr>>>" + subStr);
						}
					}
					if (mainStr != null) {
						if (SaveValue.getInstance(mContext).readBooleanValue(mainStr, false) && !subHasSelected(mainType)) {
							childViewData.get(i).setBgHighLigth(true);
						} else if (SaveValue.getInstance(mContext).readBooleanValue(mainStr, false) && thisSubSelected(mainType, subType)) {
							childViewData.get(i).setBgHighLigth(true);
						} else {
							childViewData.get(i).setBgHighLigth(false);
						}
					} else {
						childViewData.get(i).setBgHighLigth(false);
					}
				} else {
					childViewData.get(i).setBgHighLigth(false);
				}
				if (i == mCurrentSelectPosition && EPGConfig.SELECTED_CHANNEL_POSITION == currentChannelIndex) {
					textView.setBackground(childViewData.get(i).isDrawLeftIcon(), childViewData.get(i).isDrawRightwardIcon(), true, false);
				} else {
					textView.setBackground(childViewData.get(i).isDrawLeftIcon(), childViewData.get(i).isDrawRightwardIcon(), false, childViewData.get(i).isBgHighLight());
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

	public void refreshEventsLayout(EPGChannelInfo mChannel, List<EPGProgramInfo> mChildViewData, int channelIndex) {
		this.childViewData = mChildViewData;
		final int width = mWidth;
		removeAllViews();
		if (mChildViewData != null && !mChildViewData.isEmpty()) {
			setBackground(null);
		} else {
			setBackgroundResource(R.drawable.epg_analog_channel_bg);
			return;
		}
		for (int i = 0; i < mChildViewData.size(); i++) {
			EPGTextView textView = (EPGTextView)inflate(mContext, R.layout.epg_cn_listitem_textview, null);
			textView.setProgramInfo(mChildViewData.get(i));
			int childWidth = (int) (width * mChildViewData.get(i).getmScale());

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					childWidth, LayoutParams.MATCH_PARENT);
			layoutParams.leftMargin = (int) (mChildViewData.get(i)
					.getLeftMargin()*width);
			com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "setAdpter-----layoutParams.leftMargin---->"
					+ layoutParams.leftMargin);
//			if (i == mCurrentSelectPosition && flag == true) {
//				textView.setBackground(childViewData.get(i).isDrawLeftIcon(),
//						childViewData.get(i).isDrawRightwardIcon(), true);
//			} else {
//				textView.setBackground(childViewData.get(i).isDrawLeftIcon(),
//						childViewData.get(i).isDrawRightwardIcon(), false);
//			}
			textView.setText(textView.getShowTitle());
			int mainType = textView.getMainTypeStr();
			int subType = textView.getSubTypeStr();
			String mainStr = null;
			String subStr = null;
			//according to spec, 0x0 is undefined content
			if(1 <= mainType)
			{
				if (mainType >= 0 && mainType < DataReader.getInstance(mContext).getMainType().length) {
					mainStr = DataReader.getInstance(mContext).getMainType()[mainType-1];
					if (subType >= 0 && subType < DataReader.getInstance(mContext).getSubType().length) {
						subStr = DataReader.getInstance(mContext).getSubType()[mainType][subType];
					}
				}
				if (mainStr != null) {
					if (SaveValue.getInstance(mContext).readBooleanValue(mainStr, false) && !subHasSelected(mainType)) {
						mChildViewData.get(i).setBgHighLigth(true);
//						textView.setTextColor(Color.RED);
					} else if (SaveValue.getInstance(mContext).readBooleanValue(mainStr, false) && thisSubSelected(mainType, subType)) {
						mChildViewData.get(i).setBgHighLigth(true);
//						textView.setTextColor(Color.RED);
					} else {
						mChildViewData.get(i).setBgHighLigth(false);
					}
				} else {
					mChildViewData.get(i).setBgHighLigth(false);
				}
			} else {
				mChildViewData.get(i).setBgHighLigth(false);
			}
			if (channelIndex == EPGConfig.SELECTED_CHANNEL_POSITION) {
				if (EPGConfig.init) {
					mCurrentSelectPosition = mChannel.getPlayingTVProgramPositon();
				} else {
					if (EPGConfig.FROM_WHERE == EPGConfig.FROM_KEYCODE_DPAD_LEFT) {
						mCurrentSelectPosition = mChildViewData.size() - 1;
					} else if (EPGConfig.FROM_WHERE == EPGConfig.FROM_KEYCODE_DPAD_RIGHT) {
						mCurrentSelectPosition = 0;
					} else if (EPGConfig.FROM_WHERE == EPGConfig.FROM_KEYCODE_MTKIR_GREEN
							|| EPGConfig.FROM_WHERE == EPGConfig.FROM_KEYCODE_MTKIR_RED) {
						mCurrentSelectPosition = 0;
//				} else if (EPGConfig.FROM_WHERE == EPGConfig.FROM_ANOTHER_STREAM) {
//					int index = mChannel.getNextPosition(mEPGListView.getLastSelectedTVProgram());
//					mViewHolder.mDynamicLinearLayout.setSelectedPosition(index);
					}else if(EPGConfig.FROM_WHERE == EPGConfig.AVOID_PROGRAM_FOCUS_CHANGE){
						//int mCurrentSelectPosition = 
						mChannel.getPlayingTVProgramPositon();
					}else if(EPGConfig.FROM_WHERE == EPGConfig.SET_LAST_POSITION){
						if (mCurrentSelectPosition < 0) {
							mCurrentSelectPosition = 0;
						} else if (mCurrentSelectPosition >= mChildViewData.size()) {
							mCurrentSelectPosition = mChildViewData.size() - 1;
						}
					}
				}
			}
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setAdpter-----title---->"+ textView.getShowTitle()+"===>"+mCurrentSelectPosition
					+ "    " + mainType + "   " + subType + "	 " + mainStr + "	" + subStr);
			if (i == mCurrentSelectPosition  && EPGConfig.SELECTED_CHANNEL_POSITION == channelIndex) {
				textView.setBackground(childViewData.get(i).isDrawLeftIcon(),
						childViewData.get(i).isDrawRightwardIcon(), true, false);
			} else {
				textView.setBackground(childViewData.get(i).isDrawLeftIcon(),
						childViewData.get(i).isDrawRightwardIcon(), false, childViewData.get(i).isBgHighLight());
			}
			addView(textView, i, layoutParams);
		}
	}

}
