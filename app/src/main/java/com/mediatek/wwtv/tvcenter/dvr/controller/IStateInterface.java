/**   
 * @Description: TODO()
 */
package com.mediatek.wwtv.tvcenter.dvr.controller;

/**
 * 
 */
public interface IStateInterface {

	boolean onKeyDown(int keycode);

	void onResume();
	
	void onPause();

	void onStop();

	void onRelease();
	
	void hiddenNotCoExistWindow(int compID);
}
