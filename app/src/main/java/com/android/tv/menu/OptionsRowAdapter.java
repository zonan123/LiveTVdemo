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

package com.android.tv.menu;

import android.content.Context;
import android.view.View;
import android.util.Log;
import android.util.TypedValue;
import com.mediatek.wwtv.tvcenter.R;
import com.android.tv.menu.MenuOptionMain.OptionChangedListener;
import java.util.List;
//
//import com.android.tv.menu.MenuAction;
//import com.mediatek.wwtv.setting.util.TVContent;
//import com.mediatek.wwtv.tvcenter.TvSingletons;
//import com.mediatek.twoworlds.tv.MtkTvAVMode;
//import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
//import com.mediatek.twoworlds.tv.MtkTvMHEG5;
//import com.mediatek.wwtv.tvcenter.util.SaveValue;
//import com.mediatek.wwtv.tvcenter.util.CommonUtil;

/*
 * An adapter of options.
 */
public abstract class OptionsRowAdapter extends
		ItemListRowView.ItemListAdapter<MenuAction> {
	private static final String TAG = "OptionsRowAdapter";
	private List<MenuAction> mActionList;

	private final static int BACKGROUND_IMAGES[] = {
			R.drawable.tv_options_item_background1,
			// R.drawable.tv_options_item_background2,
			R.drawable.tv_options_item_background3,
			R.drawable.tv_options_item_background4,
			R.drawable.tv_options_item_background5 };

	private final static int HOVER_BACKGROUND_IMAGES[] = {
			R.drawable.tv_options_item_hover1,
			// R.drawable.tv_options_item_hover2,
			R.drawable.tv_options_item_hover3,
			R.drawable.tv_options_item_hover4,
			R.drawable.tv_options_item_hover5 };

	private Context mContext;

	private int ANIMATION_ELEVATION = 2;
	private int ANIMATION_TRANSLATION_Z = 4;
	private float ANIMATION_SCALE_X = 1.25f;
	private float ANIMATION_SCALE_Y = 1.25f;
	private int menuBgConfig = 0;

	private int menuFocusedDrawable = 0;
	private int menuUnFocusedDrawable = 0;
  
  	//private static final String TAG_IS3RD_SOURCE  = "is3rdSource";
	//private MtkTvMHEG5 mheg = MtkTvMHEG5.getInstance();
  	private int mCurrentFocus = -1;

	private final View.OnClickListener mMenuActionOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			final MenuAction action = (MenuAction) view.getTag();
			Log.d(TAG, "onClick: child=" + action);
			view.post(new Runnable() {
				@Override
				public void run() {
					int resId = action.getActionNameResId();
					if (resId == 0) {
					    Log.d(TAG, "run: resId == 0");
					} else {
					    Log.d(TAG, "run: resId =" + resId);
					}
					executeAction(action.getType());
				}
			});
		}
	};

	// MStar Android Patch Begin
	private final View.OnFocusChangeListener mMenuActionOnFocusChangeListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View view, boolean hasFocus) {
			MenuAction action = (MenuAction) view.getTag();
			int actionType = action.getType();
			int actionIndex = getActionIndex(actionType) == -1 ? 0
					: getActionIndex(actionType);
			Log.d(TAG, "onFocusChange||actionIndex =" + actionIndex+"--currentfocus:"+mCurrentFocus+"--hasFocus-:"+hasFocus+"---type:"+actionType);
			if (hasFocus) {
				Log.d(TAG, "onFocusChange||hasFocus type=" +actionType);
				mCurrentFocus = actionIndex;
				view.setElevation(ANIMATION_ELEVATION);
				view.animate().scaleX(ANIMATION_SCALE_X)
						.scaleY(ANIMATION_SCALE_Y)
						.translationZ(ANIMATION_TRANSLATION_Z).start();
				if(actionType != MenuOptionMain.OPTION_APP_LINK){

					if (menuBgConfig == 1) {
						view.setBackgroundResource(menuFocusedDrawable);
					} else {
						view.setBackgroundResource(HOVER_BACKGROUND_IMAGES[actionIndex
								% HOVER_BACKGROUND_IMAGES.length]);
					}
				}

			} else {
				mCurrentFocus = -1;
				Log.d(TAG, "onFocusChange||unhasFocus type=" +actionType);
				view.setElevation(0);
				view.animate().scaleX(1.0f).scaleY(1.0f).translationZ(0)
						.start();
				if(actionType != MenuOptionMain.OPTION_APP_LINK){

					if (menuBgConfig == 1) {
						view.setBackgroundResource(menuUnFocusedDrawable);
					} else {
						view.setBackgroundResource(BACKGROUND_IMAGES[actionIndex
								% BACKGROUND_IMAGES.length]);
					}
				}
			}
			/*
			 * if (hasFocus) { final MenuAction action = (MenuAction)
			 * view.getTag(); TtsUtils.speak(action.getActionName(mContext),
			 * true); TtsUtils.speak(action.getActionDescription(mContext),
			 * false); }
			 */
		}
	};

	// MStar Android Patch End

	public OptionsRowAdapter(Context context) {
		super(context);
		mContext = context;
		getAnimationCfg();
	}

	/**
	 * Update action list and its content.
	 */
	@Override
	public void update() {
		if (mActionList == null) {
			mActionList = createActions();
			updateActions();
			setItemList(mActionList);
		} else {
			if (updateActions()) {
				setItemList(mActionList);
			}
		}
	}

	@Override
	protected int getLayoutResId(int viewType) {
		if(viewType == MenuOptionMain.OPTION_APP_LINK){
			return R.layout.menu_card_new_app_link;
		} else {
			return R.layout.menu_card_action;
		}
	}

	protected abstract List<MenuAction> createActions();

	protected abstract boolean updateActions();

	protected abstract void executeAction(int type);

	/**
	 * Gets the action at the given position. Note that action at the position
	 * may differ from returned by {@link #createActions}. See
	 * {@link CustomizableOptionsRowAdapter}
	 */
	protected MenuAction getAction(int position) {
		return mActionList.get(position);
	}

	/**
	 * Sets the action at the given position. Note that action at the position
	 * may differ from returned by {@link #createActions}. See
	 * {@link CustomizableOptionsRowAdapter}
	 */
	protected void setAction(int position, MenuAction action) {
		mActionList.set(position, action);
	}

	/**
	 * Adds an action to the given position. Note that action at the position
	 * may differ from returned by {@link #createActions}. See
	 * {@link CustomizableOptionsRowAdapter}
	 */
	protected void addAction(int position, MenuAction action) {
		mActionList.add(position, action);
	}

	/**
	 * Removes an action at the given position. Note that action at the position
	 * may differ from returned by {@link #createActions}. See
	 * {@link CustomizableOptionsRowAdapter}
	 */
	protected void removeAction(int position) {
		mActionList.remove(position);
	}

	protected int getActionSize() {
		return mActionList.size();
	}

	protected int getActionIndex(int type) {
		for (int index = 0; index < getActionSize(); index++) {
			MenuAction mAction = mActionList.get(index);
			if (mAction.getType() == type) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * Item in position 0 gets default focus,so it should have hover background
	 * instead of normal background
	 */
	@Override
	public void onBindViewHolder(MyViewHolder viewHolder, int position) {
		super.onBindViewHolder(viewHolder, position);

		viewHolder.itemView.setTag(getItemList().get(position));
		viewHolder.itemView.setOnClickListener(mMenuActionOnClickListener);
		viewHolder.itemView
				.setOnFocusChangeListener(mMenuActionOnFocusChangeListener);

		Log.d(TAG, "onBindViewHolder||position =" + position+"---currentFocus:"+mCurrentFocus+"---type:"+mActionList.get(position).getType());
		if(mActionList.get(position).getType() != MenuOptionMain.OPTION_APP_LINK){

		if (menuBgConfig == 1) {
			viewHolder.itemView
					.setBackgroundResource(position == 0 ? menuFocusedDrawable
							: menuUnFocusedDrawable);
		} else {
//			if (position == getFirstEnabledPosition()) {
//				viewHolder.itemView
//						.setBackgroundResource(HOVER_BACKGROUND_IMAGES[position
//								% HOVER_BACKGROUND_IMAGES.length]);
//				viewHolder.itemView.requestFocus();
//			} else {
                  if(mCurrentFocus != position){
					  Log.d(TAG, "onBindViewHolder||hasFocus type=" +mActionList.get(position).getType());
				viewHolder.itemView
						.setBackgroundResource(BACKGROUND_IMAGES[position
								% BACKGROUND_IMAGES.length]);
                     }
//			}

		}
		}
	}

	@Override
	public int getItemViewType(int position) {
		// This makes 1:1 mapping from MenuAction to ActionCardView. That is, an
		// ActionCardView will
		// not be used(recycled) by other type of MenuAction. So the selection
		// state of the view can
		// be preserved.
		return mActionList.get(position).getType();
	}

	protected void setOptionChangedListener(final MenuAction action) {
		MenuOptionMain om = getMainActivity().getTvOptionsManager();
		if (om != null) {
			om.setOptionChangedListener(action.getType(),
					new OptionChangedListener() {
						@Override
						public void onOptionChanged(String newOption) {
							/*if (action.getType() == MenuOptionMain.OPTION_AUTO_PICTURE
									||action.getType() == MenuOptionMain.OPTION_DISPLAY_MODE
									||action.getType() == MenuOptionMain.OPTION_SOUND_STYLE){
								if(mActionList.contains(action)){
									for (MenuAction menuAction : mActionList) {
										if(menuAction.getType() == action.getType()){
											if(action.getType() == MenuOptionMain.OPTION_AUTO_PICTURE){
												TVContent tvContent = TVContent.getInstance((Context)getMainActivity());
												boolean isLoss = tvContent.isSignalLoss();
												menuAction.setEnabled(menuAction,!isLoss);
											} else if(action.getType() == MenuOptionMain.OPTION_DISPLAY_MODE){

												CommonIntegration ci = TvSingletons.getSingletons()
														.getCommonIntegration();
												MtkTvAVMode navMtkTvAVMode = MtkTvAVMode.getInstance();
												boolean is3Rd = ci.is3rdTVSource();
												boolean isEnable = false;
												SaveValue.writeWorldStringValue((Context)getMainActivity(),
														is3Rd ? "1" : "0", TAG_IS3RD_SOURCE, true);
												boolean isScanMode = mheg.getInternalScrnMode();

												if (navMtkTvAVMode != null) {
													int[] allScreenMode = navMtkTvAVMode.getAllScreenMode();
													isEnable = allScreenMode != null && allScreenMode.length > 0;
													com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "allScreenMode---="+allScreenMode+"---"+isEnable );
												}
												com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateDisplayModeAction||is3Rd =" + is3Rd
														+ "||isEnable =" + isEnable + "||isScanMode =" + isScanMode);
												menuAction.setEnabled(menuAction, !is3Rd
														&& !isScanMode && isEnable);
											} else if(action.getType() == MenuOptionMain.OPTION_SOUND_STYLE){
												com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateSoundStyle---:"+CommonUtil.isSupportSoundStyle((Context) getMainActivity()));
												if(CommonUtil.isSupportSoundStyle((Context) getMainActivity())){
													menuAction.setEnabled(menuAction,true);
												} else {
													menuAction.setEnabled(menuAction,false);
												}
											}
										}
									}
								}
							}*/
							setItemList(mActionList);
						}
					});
		}
	}

	private void getAnimationCfg() {
		TypedValue typedValueElevation = new TypedValue();
		TypedValue typedValueTranslation = new TypedValue();

		TypedValue typedValueScaleX = new TypedValue();
		TypedValue typedValueScaleY = new TypedValue();

		TypedValue typedValueBg = new TypedValue();

		mContext.getResources().getValue(R.dimen.nav_menu_animation_elevation,
				typedValueElevation, true);
		mContext.getResources().getValue(
				R.dimen.nav_menu_animation_translattion_z,
				typedValueTranslation, true);

		mContext.getResources().getValue(R.dimen.nav_menu_animation_scale_x,
				typedValueScaleX, true);
		mContext.getResources().getValue(R.dimen.nav_menu_animation_scale_y,
				typedValueScaleY, true);

		mContext.getResources().getValue(R.dimen.nav_menu_animation_background,
				typedValueBg, true);

		ANIMATION_ELEVATION = (int) typedValueElevation.getFloat();
		ANIMATION_TRANSLATION_Z = (int) typedValueTranslation.getFloat();

		menuBgConfig = (int) typedValueBg.getFloat();

		ANIMATION_SCALE_X = typedValueScaleX.getFloat();
		ANIMATION_SCALE_Y = typedValueScaleY.getFloat();

		Log.d(TAG, "OptionsRowAdapter||Elevation =" + ANIMATION_ELEVATION
				+ "||Translation =" + ANIMATION_TRANSLATION_Z + "||ScaleX ="
				+ ANIMATION_SCALE_X + "||ScaleY =" + ANIMATION_SCALE_Y
				+ "||menuBgConfig =" + menuBgConfig);

		menuFocusedDrawable = R.drawable.tv_options_focused;
		menuUnFocusedDrawable = R.drawable.tv_options_unfocused;
	}

	/**
	 * get first enabled MenuAction
	 */
	/*private int getFirstEnabledPosition() {
		int firstEnabled = 0;
		for (int i = 0; i < getItemCount(); i++) {
			if (getItemList().get(i).isEnabled()) {
				firstEnabled = i;
				break;
			}
		}
		Log.d(TAG, "getFirstEnabledPosition =" + firstEnabled);
		return firstEnabled;
	}*/
}
