/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.mediatek.wwtv.tvcenter.nav.view.ciview;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.util.KeyMap;


public class PinDialogFragment extends Fragment {
	static final String TAG = "PinDialogFragment";
	static boolean DEBUG = true;
    private View rootv;
    Fragment rootFragment;
	Context context;

	/**
	 * PIN code dialog for unlock channel
	 */
	public static final int PIN_DIALOG_TYPE_UNLOCK_CHANNEL = 0;

	/**
	 * PIN code dialog for unlock content. Only difference between
	 * {@code PIN_DIALOG_TYPE_UNLOCK_CHANNEL} is it's title.
	 */
	public static final int PIN_DIALOG_TYPE_UNLOCK_PROGRAM = 1;

	/**
	 * PIN code dialog for change parental control settings
	 */
	public static final int PIN_DIALOG_TYPE_ENTER_PIN = 2;

	/**
	 * PIN code dialog for set new PIN
	 */
	public static final int PIN_DIALOG_TYPE_NEW_PIN = 3;

	// PIN code dialog for checking old PIN. This is intenal only.
	static final int PIN_DIALOG_TYPE_OLD_PIN = 4;

	static final int PIN_DIALOG_RESULT_SUCCESS = 0;
	static final int PIN_DIALOG_RESULT_FAIL = 1;

	static final int MAX_WRONG_PIN_COUNT = 5;
	static final int DISABLE_PIN_DURATION_MILLIS = 60 * 1000; // 1
																		// minute
    private int pinCodeLength = 3;
	private static boolean isBlindAns = true;
	public interface ResultListener {
		void done(String pinCode);
	}
	//when press back key
	public interface CancelBackListener {
		void cancel();
	}
	public void setPinCodeLength(int pinCodeLength){
	       this.pinCodeLength=pinCodeLength;
	    }
	public void setBlindAns(boolean isBlindAns){this.isBlindAns=isBlindAns;	}
	private static final int NUMBER_PICKERS_RES_ID[] = { R.id.first,
			R.id.second, R.id.third, R.id.fourth, R.id.five };

	private ResultListener mListener;
	private CancelBackListener mCancelListener;
	int mRetCode;

	private PinNumberPicker[] mPickers;
	String mPrevPin;
	int mWrongPinCount;
	final Handler mHandler = new Handler();

	public boolean isPinCorrect(String pin) {
		return false;
	}

	public boolean isPinSet() {
		return false;
	}
	public void requestPickerFocus(){
	    initView();
		mPickers[0].requestFocus();
		mPickers[0].updateFocus();
	}

	// public PinDialogFragment(int type, ResultListener listener, Context
	// context) {
	// mListener = listener;
	// mRetCode = PIN_DIALOG_RESULT_FAIL;
	// this.context = context;
	// PinNumberPicker.loadResources(context);
	// }

	public PinDialogFragment() {
		super();
		rootFragment=this;
	}

	public void setResultListener(ResultListener listener) {
		this.mListener = listener;
	}
	
	public void setCancelBackListener(CancelBackListener listener) {
		this.mCancelListener = listener;
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreateView");
	    rootv = inflater.inflate(R.layout.ci_pin_code_fragment,
	                   container, false);		
		PinNumberPicker.loadResources((Context) this.getActivity());
		return rootv;
	}
	
	private void initView(){
        mPickers = new PinNumberPicker[pinCodeLength];
        for (int i = 0; i < pinCodeLength; i++) {
            mPickers[i] = (PinNumberPicker) rootv
                    .findViewById(NUMBER_PICKERS_RES_ID[i]);
            mPickers[i].setValueRange(0, 9);
            mPickers[i].setPinDialogFragment(this);
            mPickers[i].updateFocus();
            mPickers[i].setVisibility(View.VISIBLE);
        }
        for (int i = 0; i < pinCodeLength-1; i++) {
            mPickers[i].setNextNumberPicker(mPickers[i + 1]);
        }
        mPickers[0].requestFocus();
    }
	private void done(String pin) {
		resetPinInput();
		if (mListener != null) {
			mListener.done(pin);
		}
	}
	
	private boolean cancelback() {
		resetPinInput();
		if (mCancelListener != null) {
			mCancelListener.cancel();
			return true;
		}else{
			return false;
		}
	}

	private String getPinInput() {
	  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getPinInput");
		String result = "";
		int i=0;
		try {
			for (PinNumberPicker pnp : mPickers) {
				// pnp.updateText();
				result += pnp.getValue();
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "pnp["+i++ + "]:"+pnp.getValue());
			}
		} catch (IllegalStateException e) {
			result = "";
		}
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "result:"+result);
		return result;
	}

	@Override
	public void onResume() {
		super.onResume();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onResume");
	}

	@Override
	public void onStart() {
		super.onStart();
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStart");
	}

	public void resetPinInput() {
		for (PinNumberPicker pnp : mPickers) {
			pnp.setValueRange(0, 9);
		}
		mPickers[0].requestFocus();
	}

	public static final class PinNumberPicker extends FrameLayout {
		private static final int NUMBER_VIEWS_RES_ID[] = {
				R.id.previous2_number, R.id.previous_number,
				R.id.current_number, R.id.next_number, R.id.next2_number };
		private static final int CURRENT_NUMBER_VIEW_INDEX = 2;

		private static Animator sFocusedNumberEnterAnimator;
		private static Animator sFocusedNumberExitAnimator;
		private static Animator sAdjacentNumberEnterAnimator;
		private static Animator sAdjacentNumberExitAnimator;

		private static float sAlphaForFocusedNumber;
		private static float sAlphaForAdjacentNumber;

		private int mMinValue;
		private int mMaxValue;
		private int mCurrentValue;
		private int mNextValue;
		private int mNumberViewHeight;
		private PinDialogFragment mDialog;
		private PinNumberPicker mNextNumberPicker;
		private boolean mCancelAnimation;

		private final View mNumberViewHolder;
		private final View mBackgroundView;
		private final TextView[] mNumberViews;
		private final OverScroller mScroller;

		public PinNumberPicker(Context context) {
			this(context, null);
		}

		public PinNumberPicker(Context context, AttributeSet attrs) {
			this(context, attrs, 0);
		}

		public PinNumberPicker(Context context, AttributeSet attrs,
				int defStyleAttr) {
			this(context, attrs, defStyleAttr, 0);
		}

		public PinNumberPicker(Context context, AttributeSet attrs,
				int defStyleAttr, int defStyleRes) {
			super(context, attrs, defStyleAttr, defStyleRes);
			View view = inflate(context, R.layout.ci_fragment_number_picker,
					this);
			mNumberViewHolder = view.findViewById(R.id.number_view_holder);
			mBackgroundView = view.findViewById(R.id.focused_background);
			mNumberViews = new TextView[NUMBER_VIEWS_RES_ID.length];
			for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
				mNumberViews[i] = (TextView) view
						.findViewById(NUMBER_VIEWS_RES_ID[i]);
			}
			Resources resources = context.getResources();
			mNumberViewHeight = resources
					.getDimensionPixelOffset(R.dimen.pin_number_picker_text_view_height);

			mScroller = new OverScroller(context);

			mNumberViewHolder
					.setOnFocusChangeListener(new OnFocusChangeListener() {
						@Override
						public void onFocusChange(View v, boolean hasFocus) {
							updateFocus();
						}
					});

			mNumberViewHolder.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
						case KeyEvent.KEYCODE_DPAD_DOWN:
							if (!mScroller.isFinished() || mCancelAnimation) {
								endScrollAnimation();
							}
							if (mScroller.isFinished() || mCancelAnimation) {
								mCancelAnimation = false;
								if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
									mNextValue = adjustValueInValidRange(mCurrentValue + 1);
									startScrollAnimation(true);
									mScroller
											.startScroll(
													0,
													0,
													0,
													mNumberViewHeight,
													getResources()
															.getInteger(
																	R.integer.pin_number_scroll_duration));
								} else {
									mNextValue = adjustValueInValidRange(mCurrentValue - 1);
									startScrollAnimation(false);
									mScroller
											.startScroll(
													0,
													0,
													0,
													-mNumberViewHeight,
													getResources()
															.getInteger(
																	R.integer.pin_number_scroll_duration));
								}
								updateText();
								invalidate();
							}
							return true;
						default:
                            break;
						}
					} else if (event.getAction() == KeyEvent.ACTION_UP) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
						case KeyEvent.KEYCODE_DPAD_DOWN:
							mCancelAnimation = true;
							return true;
						default:
						    break;
						}
					}
					return false;
				}
			});
			mNumberViewHolder.setScrollY(mNumberViewHeight);
		}

		synchronized static void loadResources(Context context) {
			if (sFocusedNumberEnterAnimator == null) {
				TypedValue outValue = new TypedValue();
				context.getResources().getValue(
						R.dimen.pin_alpha_for_focused_number, outValue,
						true);
				sAlphaForFocusedNumber = outValue.getFloat();
				context.getResources().getValue(
						R.dimen.pin_alpha_for_adjacent_number, outValue,
						true);
				sAlphaForAdjacentNumber = outValue.getFloat();

				sFocusedNumberEnterAnimator = AnimatorInflater.loadAnimator(
						context, R.animator.pin_focused_number_enter);
				sFocusedNumberExitAnimator = AnimatorInflater.loadAnimator(
						context, R.animator.pin_focused_number_exit);
				sAdjacentNumberEnterAnimator = AnimatorInflater.loadAnimator(
						context, R.animator.pin_adjacent_number_enter);
				sAdjacentNumberExitAnimator = AnimatorInflater.loadAnimator(
						context, R.animator.pin_adjacent_number_exit);
			}
		}

		@Override
		public void computeScroll() {
			super.computeScroll();
			if (mScroller.computeScrollOffset()) {
				mNumberViewHolder.setScrollY(mScroller.getCurrY()
						+ mNumberViewHeight);
				updateText();
				invalidate();
			} else if (mCurrentValue != mNextValue) {
				mCurrentValue = mNextValue;
			}
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchKeyEvent,keyCode:" + event.getKeyCode());
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				int keyCode = event.getKeyCode();
				if (keyCode >= KeyEvent.KEYCODE_0
						&& keyCode <= KeyEvent.KEYCODE_9) {
					setNextValue(keyCode - KeyEvent.KEYCODE_0);
			        if (mNextNumberPicker == null) {
			            if (mScroller.isFinished() || mCancelAnimation) {
				            mCancelAnimation = false;
				            startScrollAnimation(true);
				            mScroller.startScroll(0, 0, 0, 0,
				                  getResources().getInteger(R.integer.pin_number_scroll_duration));
				            mCurrentValue = mNextValue;
				            updateText();
				            invalidate();
				            endScrollAnimation();
			            }
			            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "0~9");
//			            return true;
			        }
				}else if(keyCode == KeyEvent.KEYCODE_BACK){
					if(!mDialog.cancelback()) {
					    return super.dispatchKeyEvent(event);
					}
				} else if(keyCode == KeyMap.KEYCODE_MTKIR_CHUP || keyCode == KeyMap.KEYCODE_MTKIR_CHDN){
				    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TurnkeyUiMainActivity");
                    if (TurnkeyUiMainActivity.getInstance() != null) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TurnkeyUiMainActivity.getInstance()");
                        mDialog.resetPinInput();
//                        if(mDialog.mCancelListener !=null){  for fix DTV02157283
//                            mDialog.mCancelListener.cancel();
//                        }
                       
                        CIMainDialog ciMainDialog = (CIMainDialog) ComponentsManager.getInstance()
                                .getComponentById(NavBasic.NAV_COMP_ID_CI_DIALOG);
                        ciMainDialog.dismiss();
                        return TurnkeyUiMainActivity.getInstance().onKeyHandler(event.getKeyCode(), event);
                     }
				}else if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER
                        && keyCode != KeyEvent.KEYCODE_ENTER) {
                    return super.dispatchKeyEvent(event);
                }
				if(keyCode != KeyEvent.KEYCODE_BACK){
				    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mNextNumberPicker: "+mNextNumberPicker);
					if (mNextNumberPicker == null) {
						String pin = mDialog.getPinInput();
						com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getPinInput:"+pin);
						if (!TextUtils.isEmpty(pin)) {
							mDialog.done(pin);
						}
					} else {
						mNextNumberPicker.requestFocus();
					}
				}
                 
				return true;
			}
			return super.dispatchKeyEvent(event);
		}

		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			mNumberViewHolder.setFocusable(enabled);
			for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
				mNumberViews[i].setEnabled(enabled);
			}
		}

		void startScrollAnimation(boolean scrollUp) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startScrollAnimation()");
			if (scrollUp) {
				sAdjacentNumberExitAnimator.setTarget(mNumberViews[1]);
				sFocusedNumberExitAnimator.setTarget(mNumberViews[2]);
				sFocusedNumberEnterAnimator.setTarget(mNumberViews[3]);
				sAdjacentNumberEnterAnimator.setTarget(mNumberViews[4]);
			} else {
				sAdjacentNumberEnterAnimator.setTarget(mNumberViews[0]);
				sFocusedNumberEnterAnimator.setTarget(mNumberViews[1]);
				sFocusedNumberExitAnimator.setTarget(mNumberViews[2]);
				sAdjacentNumberExitAnimator.setTarget(mNumberViews[3]);
			}
			sAdjacentNumberExitAnimator.start();
			sFocusedNumberExitAnimator.start();
			sFocusedNumberEnterAnimator.start();
			sAdjacentNumberEnterAnimator.start();
		}

		void endScrollAnimation() {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "endScrollAnimation()");
			sAdjacentNumberExitAnimator.end();
			sFocusedNumberExitAnimator.end();
			sFocusedNumberEnterAnimator.end();
			sAdjacentNumberEnterAnimator.end();
			mCurrentValue = mNextValue;
			mNumberViews[1].setAlpha(sAlphaForAdjacentNumber);
			mNumberViews[2].setAlpha(sAlphaForFocusedNumber);
			mNumberViews[3].setAlpha(sAlphaForAdjacentNumber);
		}

		void setValueRange(int min, int max) {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setValueRange()");
			if (min > max) {
				throw new IllegalArgumentException(
						"The min value should be greater than or equal to the max value");
			}
			mMinValue = min;
			mMaxValue = max;
			mNextValue = mCurrentValue = mMinValue - 1;
			clearText();
			mNumberViews[CURRENT_NUMBER_VIEW_INDEX].setText("--");
		}

		void setPinDialogFragment(PinDialogFragment dlg) {
			mDialog = dlg;
		}

		void setNextNumberPicker(PinNumberPicker picker) {
			mNextNumberPicker = picker;
		}

		int getValue() {
			if (mCurrentValue < mMinValue || mCurrentValue > mMaxValue) {
				throw new IllegalStateException("Value is not set");
			}
			return mCurrentValue;
		}

		// Will take effect when the focus is updated.
		void setNextValue(int value) {
		  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setNextValue:"+value);
			if (value < mMinValue || value > mMaxValue) {
				throw new IllegalStateException("Value is not set");
			}
			mNextValue = adjustValueInValidRange(value);
		}

		void updateFocus() {
		  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "update focus");
			endScrollAnimation();
			if (mNumberViewHolder.isFocused()) {
				mBackgroundView.setVisibility(View.VISIBLE);
				updateText();
			} else {
				mBackgroundView.setVisibility(View.GONE);
				if (!mScroller.isFinished()) {
					mCurrentValue = mNextValue;
					mScroller.abortAnimation();
				}
				clearText();
				mNumberViewHolder.setScrollY(mNumberViewHeight);
			}
		}

		private void clearText() {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "clearText()");
			for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
				if (i != CURRENT_NUMBER_VIEW_INDEX) {
					mNumberViews[i].setText("");
				} else if (mCurrentValue >= mMinValue
						&& mCurrentValue <= mMaxValue) {
					com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"isBlindAns>> "+isBlindAns);
					if(isBlindAns){
						mNumberViews[i].setText("*");
					}else{
						mNumberViews[i].setText(String.valueOf(mCurrentValue));
					}
				}
			}
		}

		private void updateText() {
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateText()");
			if (mNumberViewHolder.isFocused()) {
				if (mCurrentValue < mMinValue || mCurrentValue > mMaxValue) {
					mNextValue = mCurrentValue = mMinValue;
				}
				int value = adjustValueInValidRange(mCurrentValue
						- CURRENT_NUMBER_VIEW_INDEX);
				for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
					mNumberViews[i].setText(String
							.valueOf(adjustValueInValidRange(value)));
					value = adjustValueInValidRange(value + 1);
				}
			}
		}

		private int adjustValueInValidRange(int value) {
		  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "adjustValueInValidRange:"+value);
			int interval = mMaxValue - mMinValue + 1;
			if (value < mMinValue - interval || value > mMaxValue + interval) {
				throw new IllegalArgumentException("The value( " + value
						+ ") is too small or too big to adjust");
			}
			int retValue = (value < mMinValue) ? value + interval
          : (value > mMaxValue) ? value - interval : value;
			com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "return value:"+retValue);
			return retValue;
		}
	}
}
