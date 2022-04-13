package com.mediatek.wwtv.tvcenter.nav.view;

import android.content.Context;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.widget.TextView;
import android.view.inputmethod.EditorInfo;
import android.view.Gravity;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.commonview.ConfirmDialog;
import com.mediatek.wwtv.tvcenter.commonview.ConfirmDialog.IResultCallback;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIMainDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.twoworlds.tv.MtkTvMHEG5;
import com.mediatek.twoworlds.tv.MtkTvSubtitle;
import com.mediatek.twoworlds.tv.model.MtkTvMHEG5PfgBase;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;

public class Mheg5 extends NavBasicMisc implements ComponentStatusListener.ICStatusListener, IResultCallback{
    private static final String TAG = "Mheg5";

    private MtkTvMHEG5 mheg = MtkTvMHEG5.getInstance();
    private InternalPINDialog dialog = null;
    private int mMheg5MessageUpdateType = -1;
    private int mConflictDialogType = -1;
    private int mCurrentMhegstatus = -1;
    private boolean isUIShow;

    public Mheg5(Context context){
        super(context);

        componentID = NAV_NATIVE_COMP_ID_MHEG5;

        //Add component status Listener
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_COMPONENT_HIDE, this);
//        ComponentStatusListener.getInstance().addListener(
//                ComponentStatusListener.NAV_KEY_OCCUR, this);
        ComponentStatusListener.getInstance().addListener(
                ComponentStatusListener.NAV_CHANNEL_CHANGED, this);
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public boolean isCoExist(int componentID) {
        return false;
    }

    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
        return false;
    }

    @Override
    public boolean initView() {
        return false;
    }

    @Override
    public boolean deinitView() {
        dialog = null;
        return false;
    }

    public void handlerMheg5Message(int message, int param2){
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "handlerMheg5Message, message = " + message + ",param2=="+param2);
        mMheg5MessageUpdateType = message;
        mConflictDialogType = param2;

        switch(message){
        case 1://1: MHEG5_ACTIVE
        {
//        CIMainDialog ciDialog = (CIMainDialog) ComponentsManager.getInstance().getComponentById(
//            NavBasic.NAV_COMP_ID_CI_DIALOG);
//        if ((ciDialog != null) && (ciDialog.isShowing())) {
//          ciDialog.dismiss();
//        }
            /*if(CommonIntegration.getInstance().getCurChInfo().isRadioService()){
                break;//DTV00600404
            }*/
            mCurrentMhegstatus = message;
            ComponentsManager.updateActiveCompId(true, NAV_NATIVE_COMP_ID_MHEG5);
            ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_COMPONENT_SHOW, 0);
            break;
        }
        case 2://2: MHEG5_INACTIVE
            mCurrentMhegstatus = message;
			if (ComponentsManager.getNativeActiveCompId() == NAV_NATIVE_COMP_ID_MHEG5) {
				ComponentsManager.updateActiveCompId(true, 0);
				ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_COMPONENT_HIDE, 0);
			}
            break;
        case 3://3: MHEG5_PROMPT_FOR_GUID
        CIMainDialog ciDialog = (CIMainDialog) ComponentsManager.getInstance().getComponentById(
            NavBasic.NAV_COMP_ID_CI_DIALOG);
        if ((ciDialog != null) && (ciDialog.isShowing())) {
          ciDialog.dismiss();
        }
            ComponentsManager.updateActiveCompId(true, NAV_NATIVE_COMP_ID_MHEG5);
            ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_COMPONENT_SHOW, 0);
            MtkTvMHEG5PfgBase pfg = mheg.getPfgInfo();
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "getPfgInfo:" + pfg.pfgString);
            if(pfg.show){
                if(dialog == null || (!dialog.isShowing())){
                    dialog = new InternalPINDialog(mContext, pfg.pfgString);
                    dialog.show();
                }
            }
            break;
        case 4://4: MHEG5_NO_EPG
            break;
        case 5://5: MHEG5_SBTL_ON
            ConfirmDialog dialog = new ConfirmDialog(mContext,
                    mContext.getString(R.string.nav_mheg5_tips));
            dialog.setTimeout(NAV_TIMEOUT_5);
            dialog.setCallback(this);
            dialog.showDialog();
            break;
        case 257://257: show conflict dialog
            String dialogTips = null;
            if (1 == mConflictDialogType){
                dialogTips = mContext.getString(R.string.mheg5_show_confict_dialog_tips1);
            } else if (2 == mConflictDialogType){
                dialogTips = mContext.getString(R.string.mheg5_show_confict_dialog_tips2);
            } else if (3 == mConflictDialogType){
                dialogTips = mContext.getString(R.string.mheg5_show_confict_dialog_tips3);
            }
            ConfirmDialog conflictDialog = new ConfirmDialog(mContext, dialogTips);
            conflictDialog.setTimeout(NAV_TIMEOUT_5);
            conflictDialog.setCallback(this);
            conflictDialog.showDialog();
            break;
        case 263://263 Mheg5 UI show state   1:show    0:dismiss
            if (param2 == 0) {
                isUIShow = false;
            } else {
                isUIShow = true;
            }

            break;

        default:
            break;
        }
    }

    @Override
    public void updateComponentStatus(int statusID, int value){
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus>>" + statusID + ">>>" + value);
    	if (statusID == ComponentStatusListener.NAV_CHANNEL_CHANGED) {
			if ((ComponentsManager.getNativeActiveCompId() == NAV_NATIVE_COMP_ID_MHEG5)
					&& (value != -1)) {
				com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "value != -1");
//				we should not update compID here ,
//				for it has been reset default to 0 when change begins while recive void  handler mhegMessage =2
//				dialog = null;
//				ComponentsManager.updateActiveCompId(true, 0);
			}
    	}
//    	else if(statusID == ComponentStatusListener.NAV_KEY_OCCUR){
//            switch(value){
//            case KeyMap.KEYCODE_MTKIR_CHDN:
//            case KeyMap.KEYCODE_MTKIR_CHUP:
//            case KeyMap.KEYCODE_MTKIR_PRECH:
//                if(ComponentStatusListener.getParam1() != -1){
//                    dialog = null;
//                    ComponentsManager.updateActiveCompId(true, 0);
//                }
//                break;
//            }
//        }
        else if(statusID == ComponentStatusListener.NAV_COMPONENT_HIDE){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus||currentMhegState =" + mCurrentMhegstatus);
            if (mCurrentMhegstatus == 1) {
                ComponentsManager.updateActiveCompId(true, NAV_NATIVE_COMP_ID_MHEG5);
                ComponentStatusListener.getInstance().updateStatus(ComponentStatusListener.NAV_COMPONENT_SHOW, 0);
            }
            if(!ComponentsManager.getInstance().isComponentsShow()){
                ComponentsManager.nativeComponentReActive();
            }
        }
    }

    private class InternalPINDialog extends NavBasicDialog{
        private String password = "";
        private String showPasswordStr = "";
        private String mPfg = "";
        private TextView pwdError;
        private TextView pwdValue;

        public InternalPINDialog(Context context, String pfg){
            super(context, R.id.nav_tv_pwd_value);

            mPfg = pfg;
        }

        @Override
        public boolean initView(){
            setContentView(R.layout.nav_mheg_pwd_view);

            pwdError = (TextView) findViewById(R.id.nav_mheg5_pwd_error);
            pwdError.setText(mPfg);
            pwdError.setVisibility(View.VISIBLE);

            pwdValue = (TextView) findViewById(R.id.nav_mheg5_pwd_value);
            pwdValue.setInputType(EditorInfo.TYPE_NULL);
			pwdValue.setVisibility(View.VISIBLE);

            return true;
        }

        @Override
        protected void onStart() {
            // TODO Auto-generated method stub
            super.onStart();

            Window window = getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();

            //int menuWidth = (494 + 329)*ScreenConstant.SCREEN_WIDTH /1280;
            //int menuHeight = 420*ScreenConstant.SCREEN_HEIGHT /720;
            //lp.width = menuWidth;
            //lp.height = menuHeight;

			lp.width =(int) (ScreenConstant.SCREEN_WIDTH * 0.45);
			lp.height =(int) (ScreenConstant.SCREEN_HEIGHT * 0.15);
            //lp.x = ScreenConstant.SCREEN_WIDTH / 2 - menuWidth / 2;
            //lp.y = (int) (ScreenConstant.SCREEN_HEIGHT / 2) - menuHeight / 2;
			lp.x = 0;
			lp.y = 0;

			lp.gravity =Gravity.CENTER;
            window.setAttributes(lp);
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchKeyEvent: keyCode=" + keyCode);

            if (event.getAction()==KeyEvent.ACTION_DOWN) {
                this.startTimeout(NavBasicDialog.NAV_TIMEOUT_120);

                switch (keyCode) {
                case KeyMap.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_E:
                    //check pin code
                    if(password.length() == 4){
                        this.dismiss();
                    }
                    break;
                case KeyMap.KEYCODE_BACK:
                    this.dismiss();
                    break;
                case KeyMap.KEYCODE_0:
                case KeyMap.KEYCODE_1:
                case KeyMap.KEYCODE_2:
                case KeyMap.KEYCODE_3:
                case KeyMap.KEYCODE_4:
                case KeyMap.KEYCODE_5:
                case KeyMap.KEYCODE_6:
                case KeyMap.KEYCODE_7:
                case KeyMap.KEYCODE_8:
                case KeyMap.KEYCODE_9:
                    if(password.length() < 4){
                        password=password + (keyCode - 7);
                        showPasswordStr = showPasswordStr + "*";
                        pwdValue.setText(showPasswordStr);

                        if(password.length() == 4){
                            this.startTimeout(NavBasicDialog.NAV_TIMEOUT_1);
                        }
                    }
                    break;
                default:
                    break;
                }
            }

            return super.dispatchKeyEvent(event);
        }

        @Override
        public void dismiss() {
            mheg.setPfgResult(MtkTvPWDDialog.getInstance().checkPWD(password));

            super.dismiss();
            //update status
        }
    }

    @Override
    public void handleUserSelection(int result){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleUserSelection,result==" + result + ",mMheg5MessageUpdateType=="+mMheg5MessageUpdateType);
        if(result == ConfirmDialog.BTN_YES_CLICK){
            if (257 == mMheg5MessageUpdateType){
                mheg.setMheg5Status(mMheg5MessageUpdateType, mConflictDialogType, 1, 0);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleUserSelection,BTN_YES_CLICK,mConflictDialogType=="+mConflictDialogType);
            } else {
                //subtitle off
                MtkTvSubtitle subtitle = MtkTvSubtitle.getInstance();
                subtitle.dealStream(MtkTvSubtitle.DealType.DEAL_TYPE_STOP_CURRENT.getDealType());
            }
        } else {
            if (257 == mMheg5MessageUpdateType){
                mheg.setMheg5Status(mMheg5MessageUpdateType, mConflictDialogType, 0, 0);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleUserSelection,BTN_NO_CLICK,mConflictDialogType=="+mConflictDialogType);
            }
        }
    }


    public boolean getIsMheg5UiShow() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getIsMheg5UiShow. isUIShow=" + isUIShow);
        return isUIShow;
    }
}
