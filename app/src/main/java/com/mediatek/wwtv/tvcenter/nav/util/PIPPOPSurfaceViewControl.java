package com.mediatek.wwtv.tvcenter.nav.util;

import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;


import android.widget.LinearLayout;
import com.mediatek.wwtv.tvcenter.commonview.TvSurfaceView;

public class PIPPOPSurfaceViewControl {
  
  private static final String TAG = "PIPPOPSurfaceViewControl";
//	private TvSurfaceView mainOutputView;
//	private TvSurfaceView subOutputView;
//	private SurfaceHolder mainSurfaceHolder;
//	private SurfaceHolder subSurfaceHolder;

	private float mainOutputX;
	private float mainOutputY;
	private float mainOutputW;
//	private float mainOutputH;
	private float subOutputX;
	private float subOutputY;
	private float subOutputW;
	//private float subOutputH;

	private int pipOutputPos;
	private int pipOutputSize;

//	private LinearLayout mainLayout;
//	private LinearLayout subLayout;
//	private FrameLayout.LayoutParams mainOutputLayoutParams;
//	private FrameLayout.LayoutParams subOutputLayoutParams;

	private static PIPPOPSurfaceViewControl mpPippopSurfaceViewControl;

//	private int lastScreenMode = -1;
//	private int[] supportScreenModes;
//	private SundryImplement mNavSundryImplement;

//	private final static int SCREEN_MODE_DOT_BY_DOT = 6;
//	private final static int SCREEN_MODE_AUTO = 7;
//  private boolean hasChanged = false;

	public PIPPOPSurfaceViewControl() {
		super();
		pipOutputPos = MtkTvConfig.getInstance().getConfigValue(
				MtkTvConfigType.CFG_PIP_POP_PIP_POSITION)/2;
		pipOutputSize = MtkTvConfig.getInstance().getConfigValue(
				MtkTvConfigType.CFG_PIP_POP_PIP_SIZE);
//		mNavSundryImplement = SundryImplement
//        .getInstanceNavSundryImplement(TurnkeyUiMainActivity.getInstance());
		// TODO Auto-generated constructor stub
	}

	public static synchronized PIPPOPSurfaceViewControl getSurfaceViewControlInstance() {
		if (null == mpPippopSurfaceViewControl) {
			mpPippopSurfaceViewControl = new PIPPOPSurfaceViewControl();
		}
		return mpPippopSurfaceViewControl;
	}

//	private void init() {
//		mainOutputLayoutParams = (FrameLayout.LayoutParams) mainOutputView.getLayoutParams();
//		subOutputLayoutParams = (FrameLayout.LayoutParams) subOutputView.getLayoutParams();
//		mainSurfaceHolder = mainOutputView.getHolder();
//		subSurfaceHolder = subOutputView.getHolder();
//	}

	public void setSignalOutputView(TvSurfaceView mainOutput,
			TvSurfaceView subOutput,LinearLayout mainLY, LinearLayout subLY) {
//		mainOutputView = mainOutput;
//		subOutputView = subOutput;
//		mainLayout = mainLY;
//		subLayout = subLY;
//		init();
	  if(mainOutput != null) {
	    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mainOutput: " + mainOutput.toString());
	  }
    if(subOutput != null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subOutput: " + subOutput.toString());
    }
    if(mainLY != null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mainLY: " + mainLY.toString());
    }
    if(subLY != null) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "subLY: " + subLY.toString());
    }
	}

	public void changeOutputWithTVState(int state) {
          /*
		setMainOutPutPos(state);
		setSubOutPutPos(state);
		updateMainAndSubOutputPostion();
		if(MarketRegionInfo
				.isFunctionSupport(MarketRegionInfo.F_MULTI_VIEW_SUPPORT)){
      if (PIPPOPConstant.TV_PIP_STATE == state) {
        if (SCREEN_MODE_DOT_BY_DOT == MtkTvConfig.getInstance().getConfigValue(
            MenuConfigManager.SCREEN_MODE)) {
          lastScreenMode = SCREEN_MODE_DOT_BY_DOT;
        }
      } else if (PIPPOPConstant.TV_POP_STATE == state) {
				if(SCREEN_MODE_DOT_BY_DOT == MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.SCREEN_MODE)){
          // lastScreenMode = SCREEN_MODE_DOT_BY_DOT;
//					MtkTvConfig.getInstance().setConfigValue(MenuConfigManager.SCREEN_MODE, SCREEN_MODE_AUTO);
					supportScreenModes = mNavSundryImplement.getSupportScreenModes();
					if (null != supportScreenModes) {
					  int index = getCurrentValueIndex(supportScreenModes,
                mNavSundryImplement.getCurrentScreenMode());
					  if (index < supportScreenModes.length - 1) {
              index = index + 1;
            } else {
              index = 0;
            }
					  mNavSundryImplement.setCurrentScreenMode(supportScreenModes[index]);
					}
				}
			}else if(PIPPOPConstant.TV_NORMAL_STATE == state){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("MultiViewControl", "hasChanged: " + hasChanged);
        if (hasChanged) {
          hasChanged = false;
          supportScreenModes = mNavSundryImplement.getSupportScreenModes();
          if (null != supportScreenModes) {
            int index = getCurrentValueIndex(supportScreenModes,
                mNavSundryImplement.getCurrentScreenMode());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("MultiViewControl", "index: " + index);
            if (index > supportScreenModes.length - 1) {
              index = 0;
            }
            int result = mNavSundryImplement
                .setCurrentScreenMode(supportScreenModes[index]);
          }
          lastScreenMode = -1;

        } else if (SCREEN_MODE_DOT_BY_DOT == lastScreenMode) {
            MtkTvConfig.getInstance().setConfigValue(MenuConfigManager.SCREEN_MODE,
                SCREEN_MODE_DOT_BY_DOT);
            lastScreenMode = -1;
        }
			}
		}
                */
	}

  // pop change screen mode is true,or is false
//  public void setScreenModeChangedFlag(boolean flag) {
//    com.mediatek.wwtv.tvcenter.util.MtkLog.d("MultiViewControl", "setScreenModeChangedFlag");
//    hasChanged = flag;
//  }

	public int getCurrentValueIndex(int[] currentArray, int value) {
    if (currentArray != null) {
      for (int i = 0; i < currentArray.length; i++) {
        if (value == currentArray[i]) {
          return i;
        }
      }
    }

    return 0;
  }

	public void changeSubOutputPosition() {
		if (pipOutputPos < 4) {
			pipOutputPos++;
		} else {
			pipOutputPos = 0;
		}
		setSubOutPutPos(PipPopConstant.TV_PIP_STATE);
//		changeSubOutputViewPosition();

		MtkTvConfig.getInstance().setConfigValue(
				MtkTvConfigType.CFG_PIP_POP_PIP_POSITION, pipOutputPos * 2);
	}

	public void changeSubOutputSize() {
		if (pipOutputSize < 2) {
			pipOutputSize++;
		} else {
			pipOutputSize = 0;
		}
		setSubOutPutPos(PipPopConstant.TV_PIP_STATE);
//		changeSubOutputViewSize();

		MtkTvConfig.getInstance().setConfigValue(
				MtkTvConfigType.CFG_PIP_POP_PIP_SIZE, pipOutputSize);
	}

	public void setSubOutPutPos(int tvState) {
	    float subOutputH = 0;
		switch (tvState) {
		case PipPopConstant.TV_NORMAL_STATE:
			subOutputX = 0;
			subOutputY = 0;
			subOutputW = 0;
			subOutputH = 0;
//			pipOutputPos = 0;
//			pipOutputSize = 0;
			break;
		case PipPopConstant.TV_POP_STATE:
			subOutputX = 0.5f;
			subOutputY = 0.25f;
			subOutputW = 1;
			subOutputH = 0.75f;
			break;
		case PipPopConstant.TV_PIP_STATE:
			switch (pipOutputPos) {
			case PipPopConstant.PIP_POS_ZERO:
				if (pipOutputSize == PipPopConstant.PIP_SIZE_SMALL) {
					subOutputX = 0.7f;
					subOutputY = 0.7f;
					subOutputW = 0.9f;
					subOutputH = 0.9f;
				} else if (pipOutputSize == PipPopConstant.PIP_SIZE_MIDDLE) {
					subOutputX = 0.65f;
					subOutputY = 0.65f;
					subOutputW = 0.9f;
					subOutputH = 0.9f;
				} else {
					subOutputX = 0.5f;
					subOutputY = 0.5f;
					subOutputW = 0.9f;
					subOutputH = 0.9f;
				}
				break;
			case PipPopConstant.PIP_POS_ONE:
				if (pipOutputSize == PipPopConstant.PIP_SIZE_SMALL) {
					subOutputX = 0.7f;
					subOutputY = 0.1f;
					subOutputW = 0.9f;
					subOutputH = 0.3f;
				} else if (pipOutputSize == PipPopConstant.PIP_SIZE_MIDDLE) {
					subOutputX = 0.65f;
					subOutputY = 0.1f;
					subOutputW = 0.9f;
					subOutputH = 0.35f;
				} else {
					subOutputX = 0.5f;
					subOutputY = 0.1f;
					subOutputW = 0.9f;
					subOutputH = 0.5f;
				}
				break;
			case PipPopConstant.PIP_POS_TWO:
				if (pipOutputSize == PipPopConstant.PIP_SIZE_SMALL) {
					subOutputX = 0.4f;
					subOutputY = 0.4f;
					subOutputW = 0.6f;
					subOutputH = 0.6f;
				} else if (pipOutputSize == PipPopConstant.PIP_SIZE_MIDDLE) {
					subOutputX = 0.375f;
					subOutputY = 0.375f;
					subOutputW = 0.625f;
					subOutputH = 0.625f;
				} else {
					subOutputX = 0.3f;
					subOutputY = 0.3f;
					subOutputW = 0.7f;
					subOutputH = 0.7f;
				}
				break;
			case PipPopConstant.PIP_POS_THREE:
				if (pipOutputSize == PipPopConstant.PIP_SIZE_SMALL) {
					subOutputX = 0.1f;
					subOutputY = 0.1f;
					subOutputW = 0.3f;
					subOutputH = 0.3f;
				} else if (pipOutputSize == PipPopConstant.PIP_SIZE_MIDDLE) {
					subOutputX = 0.1f;
					subOutputY = 0.1f;
					subOutputW = 0.35f;
					subOutputH = 0.35f;
				} else {
					subOutputX = 0.1f;
					subOutputY = 0.1f;
					subOutputW = 0.5f;
					subOutputH = 0.5f;
				}
				break;
			case PipPopConstant.PIP_POS_FOUR:
				if (pipOutputSize == PipPopConstant.PIP_SIZE_SMALL) {
					subOutputX = 0.1f;
					subOutputY = 0.7f;
					subOutputW = 0.3f;
					subOutputH = 0.9f;
				} else if (pipOutputSize == PipPopConstant.PIP_SIZE_MIDDLE) {
					subOutputX = 0.1f;
					subOutputY = 0.65f;
					subOutputW = 0.35f;
					subOutputH = 0.9f;
				} else {
					subOutputX = 0.1f;
					subOutputY = 0.5f;
					subOutputW = 0.5f;
					subOutputH = 0.9f;
				}
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}

		com.mediatek.wwtv.tvcenter.util.MtkLog.i("SCAL sub", "~~~~~~~(x,y,w,h)" + "(" + subOutputX + ","
				+ subOutputY + "," + subOutputW + "," + subOutputH + ")");
	}

//	private void setMainOutPutPos(int TVState) {
//
//		switch (TVState) {
//		case PIPPOPConstant.TV_NORMAL_STATE:
//		case PIPPOPConstant.TV_PIP_STATE:
//
//			mainOutputX = 0;
//			mainOutputY = 0;
//			mainOutputW = 1;
//			mainOutputH = 1;
//
//			break;
//		case PIPPOPConstant.TV_POP_STATE:
//			mainOutputX = 0;
//			mainOutputY = 0.25f;
//			mainOutputW = 0.5f;
//			mainOutputH = 0.75f;
//			break;
//
//		default:
//			break;
//		}
//
//		com.mediatek.wwtv.tvcenter.util.MtkLog.i("SCAL mian", "~~~~~~~(x,y,w,h)" + "(" + mainOutputX + ","
//				+ mainOutputY + "," + mainOutputW + "," + mainOutputH + ")");
//
//	}

//	private void updateMainAndSubOutputPostion() {
//		Canvas mainCanvas,subCanvas;
//		mainOutputLayoutParams.width = (int) (ScreenConstant.SCREEN_WIDTH * (mainOutputW - mainOutputX));
//		mainOutputLayoutParams.height = (int) (ScreenConstant.SCREEN_HEIGHT * (mainOutputH - mainOutputY));
//		subOutputLayoutParams.width = (int) (ScreenConstant.SCREEN_WIDTH * (subOutputW - subOutputX));
//		subOutputLayoutParams.height = (int) (ScreenConstant.SCREEN_HEIGHT * (subOutputH - subOutputY));

//		try{
//			mainCanvas = mainSurfaceHolder.lockCanvas(null);
//			mainOutputView.setTranslationX(mainOutputX);
//			mainOutputView.setTranslationY(mainOutputY);
//		mainLayout.setPadding(
//				(int) (ScreenConstant.SCREEN_WIDTH * mainOutputX),
//				(int) (ScreenConstant.SCREEN_HEIGHT * mainOutputY),
//				(int) (ScreenConstant.SCREEN_WIDTH * (1 - mainOutputW)),
//				(int) (ScreenConstant.SCREEN_HEIGHT * (1 - mainOutputH)));
//		mainOutputLayoutParams.gravity = Gravity.LEFT|Gravity.TOP;
//		mainOutputLayoutParams.leftMargin = (int) (ScreenConstant.SCREEN_WIDTH * mainOutputX);
//		mainOutputLayoutParams.topMargin = (int) (ScreenConstant.SCREEN_HEIGHT * mainOutputY);
//		mainOutputLayoutParams.width = (int) (ScreenConstant.SCREEN_WIDTH * (mainOutputW - mainOutputX));
//		mainOutputLayoutParams.height = (int) (ScreenConstant.SCREEN_HEIGHT * (mainOutputH - mainOutputY));
//		com.mediatek.wwtv.tvcenter.util.MtkLog.d("SCAL", "mainOutputLayoutParams.width = "
//				+ mainOutputLayoutParams.width
//				+ ", mainOutputLayoutParams.height = "
//				+ mainOutputLayoutParams.height);
//		if(mainOutputLayoutParams.width == ScreenConstant.SCREEN_WIDTH) {
//            mainOutputLayoutParams.leftMargin = 0;
//            mainOutputLayoutParams.rightMargin = 0;
//            mainOutputLayoutParams.topMargin = 0;
//            mainOutputLayoutParams.bottomMargin = 0;
//            mainOutputLayoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
//            mainOutputLayoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
//        }
//		mainOutputView.setLayoutParams(mainOutputLayoutParams);
//		mainOutputView.invalidate();


//		subLayout.setPadding((int) (ScreenConstant.SCREEN_WIDTH * subOutputX),
//				(int) (ScreenConstant.SCREEN_HEIGHT * subOutputY),
//				(int) (ScreenConstant.SCREEN_WIDTH * (1 - subOutputW)),
//				(int) (ScreenConstant.SCREEN_HEIGHT * (1 - subOutputH)));
//		subOutputLayoutParams.gravity = Gravity.LEFT|Gravity.TOP;
//		subOutputLayoutParams.leftMargin = (int) (ScreenConstant.SCREEN_WIDTH * subOutputX);
//		subOutputLayoutParams.topMargin = (int) (ScreenConstant.SCREEN_HEIGHT * subOutputY);
//		subOutputLayoutParams.width = (int) (ScreenConstant.SCREEN_WIDTH * (subOutputW - subOutputX));
//		subOutputLayoutParams.height = (int) (ScreenConstant.SCREEN_HEIGHT * (subOutputH - subOutputY));
//		com.mediatek.wwtv.tvcenter.util.MtkLog.d("SCAL", "subOutputLayoutParams.width = "
//				+ subOutputLayoutParams.width
//				+ ", subOutputLayoutParams.height = "
//				+ subOutputLayoutParams.height);
//		subOutputView.setLayoutParams(subOutputLayoutParams);
//		subOutputView.invalidate();
//		}finally{
//			mainSurfaceHolder.unlockCanvasAndPost(subCanvas);
//		}
//	}

//	private void changeSubOutputViewPosition() {
////		subOutputView.setTranslationX(subOutputX);
////		subOutputView.setTranslationY(subOutputY);
//		subOutputLayoutParams.leftMargin = (int) (ScreenConstant.SCREEN_WIDTH * subOutputX);
//		subOutputLayoutParams.topMargin = (int) (ScreenConstant.SCREEN_HEIGHT * subOutputY);
////		subLayout.setPadding((int) (ScreenConstant.SCREEN_WIDTH * subOutputX),
////				(int) (ScreenConstant.SCREEN_HEIGHT * subOutputY), 0, 0);
//		subOutputView.setLayoutParams(subOutputLayoutParams);
//		subOutputView.invalidate();
//	}

//	private void changeSubOutputViewSize() {
//		subOutputLayoutParams.leftMargin = (int) (ScreenConstant.SCREEN_WIDTH * subOutputX);
//		subOutputLayoutParams.topMargin = (int) (ScreenConstant.SCREEN_HEIGHT * subOutputY);
//		subOutputLayoutParams.width = (int) (ScreenConstant.SCREEN_WIDTH * (subOutputW - subOutputX));
//		subOutputLayoutParams.height = (int) (ScreenConstant.SCREEN_HEIGHT * (subOutputH - subOutputY));
//		subOutputView.setLayoutParams(subOutputLayoutParams);
//		subOutputView.invalidate();
////		subLayout.setPadding((int) (ScreenConstant.SCREEN_WIDTH * subOutputX),
////				(int) (ScreenConstant.SCREEN_HEIGHT * subOutputY), 0, 0);
//
//	}

	public float[] getSubPosition() {
	  return new float[] { (subOutputX + subOutputW) / 2,
				subOutputY };
	}

	public float[] getMainPosition() {
	  return  new float[] { (mainOutputX + mainOutputW) / 2,
				mainOutputY };
	}
}
