package com.mediatek.wwtv.setting.base.scan.model;

public interface RespondedKeyEvent {

	 void setValue(int value);

	 int getValue();

	 void onKeyLeft();

	 void onKeyRight();

	 void onKeyEnter();

	 void showValue(int value);
}

