/**
 *
 */
package com.mediatek.wwtv.tvcenter.nav.view;

import android.content.Context;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

/**
 * @author MTK40707
 *
 */
public final class InfoBarDialog extends NavBasicDialog implements ComponentStatusListener.ICStatusListener{
    private static final String TAG = "InfoBarDialog";

    //dialog size
    private static int mWidth = 576;
    private static int mHeight = 50;

    //type
    public static final int INFO_BAR = 0;
    public static final int WARNING_BAR = 1;
    public static final int URGENT_BAR = 2;

    //trigger type
    public static final int LEVER_INFO = 0;
    public static final int EDGE_INFO = 1;

    //local variables
    //private static InfoBarDialog mInfoBarDialog = null;

    private int mCurrentShowType = -1;
    private int mTriggerType = -1;
    private String mInfo = "";

    //views
    private ImageView mImageView = null;
    private TextView mTextView = null;

    //private InfoBarDialog(Context context) {
	//	this(context, R.style.nav_dialog);
    //}

    /**
     * @param context
     * @param theme
     */
    public InfoBarDialog(Context context, int theme) {
        super(context, R.style.nav_dialog);
        // TODO Auto-generated constructor stub
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "InfoBarDialog||theme =" + theme);
        this.componentID = NAV_COMP_ID_INFO_BAR;

        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_COMPONENT_HIDE, this);
    }

    @Override
    public boolean isKeyHandler(int keyCode) {
        return false;
    }

    @Override
    public boolean isCoExist(int componentID) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
        switch (keyCode) {
        case KeyMap.KEYCODE_BACK:
            if(mTriggerType == LEVER_INFO){
                return true;
            }

            this.dismiss();
            break;
        case KeyMap.KEYCODE_MTKIR_ZOOM:
        	return true ;

        default:
            break;
        }
        /* Back Key to TurnkeyUiMainActivity */
        if(TurnkeyUiMainActivity.getInstance() != null){
            return TurnkeyUiMainActivity.getInstance().onKeyHandler(keyCode, event);
        }

        return false;
    }

    @Override
    public boolean initView() {
        super.initView();
        setContentView(R.layout.nav_ib_view);

        mImageView = (ImageView) findViewById(R.id.ib_image_icon);
        mTextView = (TextView) findViewById(R.id.ib_text);

        setWindowPosition();

        return true;
    }

    //public synchronized static InfoBarDialog getInstance(Context context) {
    //    if(mInfoBarDialog == null){
    //        mInfoBarDialog = new InfoBarDialog(context, R.layout.nav_ib_view);
    //        }
    //
    //    return mInfoBarDialog;
    //}

    /**
     *
     * @param showType
     * @param info
     * @return
     */
    public boolean show(int showType, String info){
        super.show();
        mTriggerType = LEVER_INFO;

        return attachData(showType, info);
    }

    /**
     *
     * @param showType
     * @param info
     * @param timeout NAV_TIMEOUT_5 / NAV_TIMEOUT_10
     * @return
     */
    public boolean show(int showType, String info, int timeout){
        super.show();

        mTriggerType = EDGE_INFO;
        this.startTimeout(timeout);

        return attachData(showType, info);
    }

    /**
     * @deprecated
     */
    @Override
    public void show(){
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "show");
    	super.show();
    }

    @Override
    public void dismiss()
    {
        dismiss(EDGE_INFO);
    }

    /**
     * this method is used to hide level trigger info bar which is
     * show by call the method:show(int showType, String info)
     * @param type
     */
    public void dismiss(int type){
        if(mTriggerType == type){
            mCurrentShowType = -1;
            mTriggerType = -1;
        }

        super.dismiss();
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyDown: keyCode=" + keyCode);
//
//        return super.onKeyHandler(keyCode, event);
//    }
@Override
public boolean dispatchKeyEvent(KeyEvent event) {
	super.dispatchKeyEvent(event);
	return true;
}
    /**
     *
     */
    private void setWindowPosition() {
        // TODO
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();

        lp.width = mWidth*ScreenConstant.SCREEN_WIDTH/1280;
        lp.height = mHeight*ScreenConstant.SCREEN_HEIGHT/720;

        lp.x = 0;
        lp.y = (int) ScreenConstant.SCREEN_HEIGHT / 2 - lp.height / 2 - 100;

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ScreenConstant.SCREEN_WIDTH=" + ScreenConstant.SCREEN_WIDTH +
                ",ScreenConstant.SCREEN_HEIGHT=" + ScreenConstant.SCREEN_HEIGHT +
                ",lp.width=" + lp.width + "," + lp.x + ", lp.height=" + lp.height + "," + lp.y);
        window.setAttributes(lp);
    }

    /**
     * prepared data for UI & show Dialog
     *
     * @param showType
     * @param info
     * @return false / true
     */
    private boolean attachData(int showType, String info){
        mCurrentShowType = showType;
        mInfo = info;

        switch(mCurrentShowType){
        case INFO_BAR:
            mImageView.setImageResource(R.drawable.nav_ib_info_icon);
            break;
        case WARNING_BAR:
            mImageView.setImageResource(R.drawable.nav_ib_warning_icon);
            break;
        case URGENT_BAR:
            mImageView.setImageResource(R.drawable.nav_ib_urgent_icon);
            break;
        default:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "/can not find type~");
            return false;
        }
        mTextView.setText(mInfo);
        return true;
    }

    /**
     *
     * @return
     */
    public boolean isInfoIn(){
        if(mTriggerType == LEVER_INFO){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " isInfoIn true");
            return true;
        }
        else{
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " isInfoIn false");
            return false;
        }
    }

    /**
     *
     * @return
     */
    public boolean showInfoIn(){
        if(mTriggerType == LEVER_INFO){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " showInfoIn LEVER_INFO");
            return show(mCurrentShowType, mInfo);
        }
        else{
            return false;
        }
    }

    @Override
    public void updateComponentStatus(int statusID, int value){
        if((mTriggerType == LEVER_INFO) && (!ComponentsManager.getInstance().isComponentsShow())){
            super.show();
        }
    }

    @Override
    public synchronized boolean deinitView(){
        super.deinitView();
        //mInfoBarDialog = null;
        mCurrentShowType = -1;
        mTriggerType = -1;
        return true;
    }

    public void handlerMessage(int code){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " handlerMessage code = " + code);

        if((code == 4 || code == 5 || code == 10 || code == 11 || code == 18 ) && mTriggerType == LEVER_INFO){
            mCurrentShowType = -1;
            mTriggerType = -1;

            if(this.isVisible()){
                this.dismiss();
				GingaTvDialog gingaTvDialog = (GingaTvDialog) ComponentsManager
						.getInstance().getComponentById(NavBasic.NAV_COMP_ID_GINGA_TV);
				gingaTvDialog.dismiss();
            }
        }
    }
}
