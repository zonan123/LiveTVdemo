
package com.mediatek.wwtv.tvcenter.nav.view;

import java.util.List;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.media.tv.TvContentRating;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.tv.ui.pindialog.PinDialogFragment;
import com.mediatek.tv.ui.pindialog.PinDialogFragment.OnPinCheckCallback;
import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.wwtv.setting.util.Util;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvr;
import com.mediatek.wwtv.tvcenter.dvr.controller.StateDvrFileList;
import com.mediatek.wwtv.tvcenter.dvr.controller.UImanager;
import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.fav.FavoriteListDialog;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIMainDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.DestroyApp;
import mediatek.sysprop.VendorProperties;

public class PwdDialog extends NavBasicDialog implements ComponentStatusListener.ICStatusListener {
  private static final String TAG = "PwdDialog";

  private static final int INPUT_ERROR_TIMES = 3;
  private static final int MSG_CHECK_DELAY = 500;
  public static final int PASSWORD_VIEW_SHOW_PWD_INPUT = 0;
  public static final int PASSWORD_VIEW_DISMISS_PWD_INPUT = 1;
  public static final int PASSWORD_VIEW_HINT_DVBS = 2;
  public static final int PASSWORD_VIEW_PWD_ERROR = 3;
  public static final int PASSWORD_VIEW_HIDE = 4;
  public static final int PASSWORD_WAITE = 10;
  public static final int MSG_CHECK = 0;
  public static final int PASSWORD_ERROR_DELAY_TIME = 900 * 1000;
  public static final int PASSWORD_ERROR_DELAY = 5;
  public static final int UPDATE_MESSAGE = 6;

  private static final String PWD_CHAR = "*";
  private final MtkTvAppTVBase tvAppTvBase;
  private final MtkTvPWDDialog mtkTvPwd;
  private static TvContentRating mRating;
  private boolean isUKCountry=false;
  private int mode = PASSWORD_VIEW_SHOW_PWD_INPUT;// = PASSWORD_VIEW_GONE;
  private String password = "";
//  private String password_in = "";
//  private String showPasswordStr = "";
  private final ComponentsManager comManager;
  private int mInputPwdErrorTimes;
  private TextView pwd_name;
  private int update_flag = 1;
  private int update_flag1 = 1;
  private boolean mCheckedPwd;
  private boolean isSvctxBlocked;
  LinearLayout pwdView;
  TextView pwdError;
  TextView pwdValue;
  TextView pwdValue1;
  TextView pwdValue2;
  TextView pwdValue3;
  private PinDialogFragment mPinDialogFragment;
  public PwdDialog(Context context, int theme) {
    super(context, theme);
    isUKCountry=TVContent.getInstance(context).isUKCountry();
    this.componentID = NAV_COMP_ID_PWD_DLG;
    mtkTvPwd = MtkTvPWDDialog.getInstance();
    comManager = ComponentsManager.getInstance();
    tvAppTvBase = new MtkTvAppTVBase();
    ComponentStatusListener.getInstance().addListener(ComponentStatusListener.NAV_RESUME, this);
    ComponentStatusListener.getInstance().addListener(ComponentStatusListener.NAV_KEY_OCCUR, this);
    ComponentStatusListener.getInstance().addListener(ComponentStatusListener.NAV_COMPONENT_SHOW,
        this);
    ComponentStatusListener.getInstance().addListener(ComponentStatusListener.NAV_CONTENT_ALLOWED,
        this);
    ComponentStatusListener.getInstance().addListener(ComponentStatusListener.NAV_CONTENT_BLOCKED,
        this);
    ComponentStatusListener.getInstance().addListener(ComponentStatusListener.NAV_CHANNEL_CHANGED,
        this);
    ComponentStatusListener.getInstance().addListener(ComponentStatusListener.NAV_INPUTS_PANEL_HIDE,
        this);
    ComponentStatusListener.getInstance().addListener(ComponentStatusListener.NAV_INPUTS_PANEL_SHOW,
        this);
    mInputPwdErrorTimes = 0;
    init();

    // tvCallbackHandler = TvCallbackHandler.getInstance();
  }

  public boolean isSvctxBlocked(){
      return isSvctxBlocked;
  }
  public void setSvctxBlocked(boolean isBlocked){
      Log.d(TAG, "setSvctxBlocked: " + isBlocked);
      isSvctxBlocked = isBlocked;
  }
  private void init(){
      mPinDialogFragment=PinDialogFragment.create(PinDialogFragment.PIN_DIALOG_TYPE_COMMON_UNLOCK);
      mPinDialogFragment.setOnPinCheckCallback(new OnPinCheckCallback(){

            @Override
            public boolean onCheckPIN(String pin) {
                return checkPWD(pin);
            }

            @Override
            public void startTimeout() {
                // TODO Auto-generated method stub
                PwdDialog.this.startTimeout(NAV_TIMEOUT_10);
            }

            @Override
            public void stopTimeout() {
                // TODO Auto-generated method stub
                PwdDialog.this.stopTimeout();
            }

            @Override
            public void pinExit() {
                PwdDialog.this.notifyNavHide();
            }
            @Override
            public void onKey(int keyCode, KeyEvent event) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onKey:"+keyCode+",event:"+event);
                TurnkeyUiMainActivity.getInstance().onKeyHandler(keyCode,
                        event, false);
                //fix DTV02032438
                dismiss();
            }
          });
  }


    public PwdDialog(Context context) {
    this(context, R.style.nav_dialog);
  }

    @Override
    public boolean deinitView() {
        ComponentStatusListener.getInstance().removeListener(this);
        mHandler.removeCallbacksAndMessages(null);
        return super.deinitView();
    }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.nav_input_pwd_view);
    // tvCallbackHandler.addCallBackListener(TvCallbackConst.MSG_CB_PWD_DLG_MSG, mHandler);
    findViews();
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate mode = " + mode);
    // showPasswordView(mode);
  }

  @Override
  public boolean isCoExist(int componentID) {
    switch (componentID) {
      case NAV_COMP_ID_BANNER:
      case NAV_COMP_ID_POP:
        return true;
      default:
          break;
    }
    return false;
  }

  @Override
  public boolean isKeyHandler(int keyCode) {
    boolean isHandler = false;
    if (keyCode == KeyMap.KEYCODE_DPAD_CENTER) {
        if (((FavoriteListDialog) ComponentsManager.getInstance().getComponentById(
                NAV_COMP_ID_FAV_LIST)).isShowing()) {
            return false;
        }

        int showFlag = mtkTvPwd.PWDShow();
        if(isContentBlock()) {
            showFlag = PASSWORD_VIEW_SHOW_PWD_INPUT;
        }
        if (mInputPwdErrorTimes < INPUT_ERROR_TIMES) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isKeyHandler showFlag = " + showFlag);
            switch (showFlag) {
              case PASSWORD_VIEW_SHOW_PWD_INPUT:
              case PASSWORD_VIEW_HINT_DVBS:
              case PASSWORD_VIEW_PWD_ERROR:
                mode = showFlag;
                isHandler = true;
              break;
              default:
                  break;
            }
        } else {
            switch (showFlag) {
              case PASSWORD_VIEW_SHOW_PWD_INPUT:
              case PASSWORD_VIEW_HINT_DVBS:
              case PASSWORD_VIEW_PWD_ERROR:
                mode = showFlag;
                isHandler = true;
                showPasswordView(PASSWORD_WAITE);
                break;
                default:
                    break;
            }
        }
    }
    return isHandler;
  }

  @Override
  public void show() {
    // update status
    boolean is3rdTVSource = CommonIntegration.getInstance().is3rdTVSource();

    if (DestroyApp.isCurActivityTkuiMainActivity()
        && (!is3rdTVSource || isContentBlock(is3rdTVSource))) {
//        super.show();
        showPasswordView(mode);
    }
  }

  @Override
  public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
    return onKeyHandler(keyCode, event);
  }

  @Override
  public boolean onKeyHandler(int keyCode, KeyEvent event) {
    boolean isHandle = true;

    keyCode = KeyMap.getKeyCode(keyCode, event);
    if (keyCode == KeyEvent.KEYCODE_MEDIA_EJECT && CommonIntegration.getInstance().isCurrentSourceBlocked()) {
        return true;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler keyCode = " + keyCode + "mode = " + mode);
    if (mInputPwdErrorTimes < INPUT_ERROR_TIMES) {
      switch (keyCode) {
        case KeyMap.KEYCODE_DPAD_CENTER:
          if (mode == PASSWORD_VIEW_PWD_ERROR) {

            showPasswordView(PASSWORD_VIEW_SHOW_PWD_INPUT);

          } else if (mode == PASSWORD_VIEW_SHOW_PWD_INPUT) {
            String inputStr = getInputString();
            if (null == inputStr || inputStr.length() == 0) {
              dismiss();
            } else {
              mHandler.removeMessages(MSG_CHECK);
              checkPassWord(inputStr);
            }
          }
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
          startTimeout(NAV_TIMEOUT_10);
          if (password.length() < 4 && pwdView != null && pwdView.getVisibility() == View.VISIBLE) {
            password = password + (keyCode - 7);
            // showPasswordStr=showPasswordStr+PWD_CHAR;
//            password_in = "" + (keyCode - 7);
            if (password.length() == 1) {
              pwdValue.setText(PWD_CHAR);
            }
            if (password.length() == 2) {
              pwdValue1.setText(PWD_CHAR);
            }
            if (password.length() == 3) {
              pwdValue2.setText(PWD_CHAR);
            }
            if (password.length() == 4) {
              pwdValue3.setText(PWD_CHAR);
            }
            // pwdValue.setText(password);
          }
          break;
        case KeyMap.KEYCODE_MTKIR_EJECT:
            if(!isUKCountry){
                BannerView banneView = (BannerView) ComponentsManager.getInstance().getComponentById(
                    NavBasic.NAV_COMP_ID_BANNER);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "banneView   pwd  " + banneView);
                if (banneView != null) {
                  banneView.changeFavChannel();
                }
              }
          break;
        case KeyMap.KEYCODE_BACK:
          dismiss();
          return true;
        case KeyEvent.KEYCODE_DPAD_DOWN:
        case KeyEvent.KEYCODE_DPAD_UP:
            return false;

        default:
          isHandle = false;
          break;
      }
    } else {
      isHandle = false;
    }
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isHandle>>>>" + isHandle + "   mContext>" + mContext);
    if (isHandle == false && null != mContext && mContext instanceof TurnkeyUiMainActivity) {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "enter TurnkeyUiMainActivity mContext");
      return TurnkeyUiMainActivity.getInstance().onKeyHandler(keyCode, event);
    }

    return isHandle;

  };



  private void findViews() {
    pwdError = (TextView) findViewById(R.id.nav_tv_pwd_error);
    pwd_name = (TextView) findViewById(R.id.nav_tv_pwd_name);
    pwdView = (LinearLayout) findViewById(R.id.nav_tv_pwd_view);
    pwdValue = (TextView) findViewById(R.id.nav_tv_pwd_value);
    pwdValue1 = (TextView) findViewById(R.id.nav_tv_pwd_value1);
    pwdValue2 = (TextView) findViewById(R.id.nav_tv_pwd_value2);
    pwdValue3 = (TextView) findViewById(R.id.nav_tv_pwd_value3);
    pwdValue3.setInputType(EditorInfo.TYPE_NULL);
    pwdValue3.addTextChangedListener(passwordInputTextWatcher3);

    pwdValue.setInputType(EditorInfo.TYPE_NULL);
    pwdValue.addTextChangedListener(passwordInputTextWatcher);
  }

  public void setWindowPosition() {
    Window window = getWindow();
    WindowManager.LayoutParams lp = window.getAttributes();

    int marginY = 4 * ScreenConstant.SCREEN_HEIGHT / 720;
    int marginX = ScreenConstant.SCREEN_WIDTH * 240 / 1280;

    int menuWidth = (494 + 329) * ScreenConstant.SCREEN_WIDTH / 1280;
    int menuHeight = 420 * ScreenConstant.SCREEN_HEIGHT / 720;
    lp.width = menuWidth;
    lp.height = menuHeight;

    lp.x = ScreenConstant.SCREEN_WIDTH / 2 - menuWidth / 2 - marginX;
    lp.y = ScreenConstant.SCREEN_HEIGHT / 2 - marginY - menuHeight / 2;

    window.setAttributes(lp);
  }

  @Override
  public void dismiss() {
    // tvCallbackHandler.removeCallBackListener(TvCallbackConst.MSG_CB_PWD_DLG_MSG, mHandler);
//    super.dismiss();
     if(isShowing()){
         mPinDialogFragment.dismiss();
     }

  }

  TextWatcher passwordInputTextWatcher = new TextWatcher() {

    @Override
    public void afterTextChanged(Editable s) {
      // if (s.toString().length() == 1) {
      // mHandler.sendEmptyMessageDelayed(MSG_CHECK, MSG_CHECK_DELAY);
      // }
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "afterTextChanged");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
        int after) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "beforeTextChanged");
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before,
        int count) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "onTextChanged");
    }
  };

  TextWatcher passwordInputTextWatcher3 = new TextWatcher() {

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      // TODO Auto-generated method stub

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
        int after) {
      // TODO Auto-generated method stub

    }

    @Override
    public void afterTextChanged(Editable s) {
      // TODO Auto-generated method stub
      if (s.toString().length() == 1) {
        mHandler.sendEmptyMessageDelayed(MSG_CHECK, MSG_CHECK_DELAY);
      }
    }
  };

  public void handleCallBack(int type) {
      if (mInputPwdErrorTimes < INPUT_ERROR_TIMES) {
          mode=type;
          if(mode==PASSWORD_VIEW_SHOW_PWD_INPUT){
              //comManager.showNavComponent(NAV_COMP_ID_PWD_DLG);
			    if (isShowing()) {
                    startTimeout(NAV_TIMEOUT_10);
                } else {
                    comManager.showNavComponent(NAV_COMP_ID_PWD_DLG);
                }
          }
          else if(mode==PASSWORD_VIEW_DISMISS_PWD_INPUT){
              dismiss();
          }
      }
  }


  private final Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      /*
       * case TvCallbackConst.MSG_CB_PWD_DLG_MSG: TvCallbackData data = (TvCallbackData)msg.obj; //
       * PwdDialog.this.show(); // PwdDialog.this.showPasswordView(PASSWORD_VIEW_PWD_INPUT); mode =
       * data.param1; comManager.showNavComponent(mode); break;
       */
        case MSG_CHECK: {
          checkPassWord(password);
          break;
        }
        case UPDATE_MESSAGE:
          update_flag1 = update_flag;
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "update_flag1 = " + update_flag1);
          if (!(StateDvrFileList.getInstance() != null && StateDvrFileList
                .getInstance().isShowing())) {
            if (StateDvr.getInstance() != null&& StateDvr.getInstance().isBigCtrlBarShow()) {
            	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isBigCtrlBarShow");
            } else if(mCheckedPwd) {
                updateState();
            }
          }
          break;
        case PASSWORD_ERROR_DELAY:
          mInputPwdErrorTimes = 0;
          break;
        default:
          break;
      }
    }
  };

  public void checkPassWord(String pwd) {
    mCheckedPwd=true;
    boolean isPass = mtkTvPwd.checkPWD(pwd);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkPassWord isPass = " + isPass + "pwd =" + pwd);
    if (isPass) {
      if(CommonIntegration.getInstance().is3rdTVSource()) {
        TurnkeyUiMainActivity.getInstance().getTvView().unblockContent(mRating);
        mRating = null;
      }

      tvAppTvBase.unlockService(CommonIntegration.getInstance().getCurrentFocus());
      dismiss();
      CIMainDialog ciMainDialog = (CIMainDialog) ComponentsManager
              .getInstance().getComponentById(
                      NavBasic.NAV_COMP_ID_CI_DIALOG);
      if(ciMainDialog != null && ciMainDialog.getHandler() != null){
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismiss ,sendEmptyMessage(0xF6)" );
          ciMainDialog.getHandler().sendEmptyMessage(0xF6);
      }

      mInputPwdErrorTimes = 0;
    } else {
      mInputPwdErrorTimes++;
      if (mInputPwdErrorTimes < INPUT_ERROR_TIMES) {
        showPasswordView(PASSWORD_VIEW_PWD_ERROR);
        Toast.makeText(mContext, R.string.nav_parent_wrong_psw, Toast.LENGTH_SHORT).show();
      } else {
        showPasswordView(PASSWORD_WAITE);
        mHandler.sendEmptyMessageDelayed(PASSWORD_ERROR_DELAY, PASSWORD_ERROR_DELAY_TIME);
      }
    }
  }

  public boolean checkPWD(String pwd) {
        mCheckedPwd=true;
        boolean isPass = mtkTvPwd.checkPWD(pwd);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "checkPWD isPass = " + isPass + "pwd =" + pwd);
        if (isPass) {
          if(CommonIntegration.getInstance().is3rdTVSource()) {
            TurnkeyUiMainActivity.getInstance().getTvView().unblockContent(mRating);
            mRating = null;
          }
          tvAppTvBase.unlockService(CommonIntegration.getInstance().getCurrentFocus());
          CIMainDialog ciMainDialog = (CIMainDialog) ComponentsManager
                  .getInstance().getComponentById(
                          NavBasic.NAV_COMP_ID_CI_DIALOG);
          if(ciMainDialog != null && ciMainDialog.getHandler() != null){
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dismiss ,sendEmptyMessage(0xF6)" );
              ciMainDialog.getHandler().sendEmptyMessage(0xF6);
          }
            setSvctxBlocked(false);
        }
        return isPass;
      }




  public void showPasswordView(int mode){
      mCheckedPwd=false;
      super.setTTSEnabled(Util.isTTSEnabled(TurnkeyUiMainActivity.getInstance()));
      if(isShowing()){
          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "PWDDialog is showing!");
          return;
      }
      mPinDialogFragment.setShowing(true);
      Log.d(TAG, "isShowing()" + isShowing());
      mPinDialogFragment.show(TurnkeyUiMainActivity.getInstance().getFragmentManager(), "PinDialogFragment");
      startTimeout(NAV_TIMEOUT_10);
      ComponentsManager.updateActiveCompId(false, componentID);
      ComponentStatusListener.getInstance().updateStatus(
          ComponentStatusListener.NAV_COMPONENT_SHOW, componentID);
  }

  public void showPasswordViewOld(int mode) {
    mCheckedPwd=false;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showPasswordView mode = " + mode);
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ENTER2 = " + mContext.getResources().getText(R.string.nav_parent_psw));
    pwd_name.setText(mContext.getResources().getText(R.string.nav_parent_psw));
    pwdValue.setText(null);
    pwdValue1.setText(null);
    pwdValue2.setText(null);
    pwdValue3.setText(null);

    password = "";
//    showPasswordStr = "";
    this.mode = mode;
    switch (mode) {
      case PASSWORD_VIEW_SHOW_PWD_INPUT:
        pwdView.setVisibility(View.VISIBLE);
        pwdError.setVisibility(View.GONE);
        startTimeout(NAV_TIMEOUT_10);
        if (0 != VendorProperties.mtk_auto_test().orElse(0)) {
          Log.d(TAG, "auto_test for pwd-dialog show");
        }
        break;
      case PASSWORD_VIEW_PWD_ERROR:
        pwdValue.setText(null);
        pwdValue1.setText(null);
        pwdValue2.setText(null);
        pwdValue3.setText(null);
        password = "";
//        showPasswordStr = "";
        // Toast.makeText(mContext, R.string.nav_parent_wrong_psw, Toast.LENGTH_SHORT).show();

        // pwdError.setText(R.string.nav_parent_wrong_psw);
        // pwdError.setVisibility(View.VISIBLE);
        // pwdView.setVisibility(View.GONE);
        startTimeout(NAV_TIMEOUT_10);
        break;
      case PASSWORD_VIEW_HINT_DVBS:
      case PASSWORD_WAITE:
        pwdError.setText(R.string.nav_parent_dvbs_hint);
        pwdError.setVisibility(View.VISIBLE);
        pwdView.setVisibility(View.GONE);
        startTimeout(NAV_TIMEOUT_5);
        break;
      case PASSWORD_VIEW_DISMISS_PWD_INPUT:
      case PASSWORD_VIEW_HIDE:
        dismiss();
        break;
      default:
          com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "default");
          break;
    }
  }

  public String getInputString() {
    if (password != null) {
      return password;
    }
    return null;
  }

  @Override
  public void updateComponentStatus(int statusID, int value) {
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus statusID =" + statusID + "   "
        + CommonIntegration.getInstance().isPipOrPopState());
    if(statusID ==  ComponentStatusListener.NAV_INPUTS_PANEL_SHOW) {
      if(isShowing()) {
        dismiss();
      }
    }
    
    if(statusID == ComponentStatusListener.NAV_RESUME) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "pwd tv resume");
        if (((FavoriteListDialog) ComponentsManager.getInstance().getComponentById(
                NAV_COMP_ID_FAV_LIST)).isShowing()) {
            return;
        }

//        int showFlag = mtkTvPwd.PWDShow();
        if(isContentBlock()) {
            showPasswordView(PASSWORD_VIEW_SHOW_PWD_INPUT);
        }
        if (mInputPwdErrorTimes < INPUT_ERROR_TIMES) {
            if (isSvctxBlocked) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "pwd tv blocked");
                showPasswordView(PASSWORD_VIEW_SHOW_PWD_INPUT);
            }
//        } else {
//            switch (showFlag) {
//              case PASSWORD_VIEW_SHOW_PWD_INPUT:
//              case PASSWORD_VIEW_HINT_DVBS:
//              case PASSWORD_VIEW_PWD_ERROR:
//                mode = showFlag;
//                showPasswordView(PASSWORD_WAITE);
//                break;
//            }
        }
    }
    
    if (mInputPwdErrorTimes >= INPUT_ERROR_TIMES) {
      int showFlag = mtkTvPwd.PWDShow();
      if (showFlag == 0) {
//        ChannelListDialog mChListDialog = (ChannelListDialog) ComponentsManager.getInstance()
//            .getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
//        if (mChListDialog != null && mChListDialog.isVisible()) {
//          mChListDialog.dismiss();
//        }
    	  NavBasicDialog mChListDialog = (NavBasicDialog) ComponentsManager
    			  .getInstance()
    			  .getComponentById(NavBasicMisc.NAV_COMP_ID_CH_LIST);
        if(mChListDialog instanceof ChannelListDialog){
        	ChannelListDialog dialog = (ChannelListDialog) mChListDialog;
          	if (dialog != null && dialog.isVisible()) {
			dialog.dismiss();
		}
        }
      }

      switch (statusID) {
        case ComponentStatusListener.NAV_CHANNEL_CHANGED:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "PWD_NAV_CHANNEL_CHANGED");
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "change_channel showFlag = " + showFlag);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "this.isShowing() = " + this.isShowing());

            switch (showFlag) {
              case PASSWORD_VIEW_SHOW_PWD_INPUT:
              case PASSWORD_VIEW_HINT_DVBS:
              case PASSWORD_VIEW_PWD_ERROR:
                mode = showFlag;
                if (this.isShowing()) {
                  showPasswordView(mode);
                } else {
                  comManager.showNavComponent(NAV_COMP_ID_PWD_DLG);
                }
                break;

              default:
                if (this.isShowing()) {
                  dismiss();
                }
                break;
            }

            break;
        default:
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "default");
            break;
      }

      return;
    }

      switch (statusID) {
          case ComponentStatusListener.NAV_INPUTS_PANEL_HIDE:
          case ComponentStatusListener.NAV_COMPONENT_SHOW:
              // DTV00622165 not a problem
              List<Integer> cpmsIDs = ComponentsManager.getInstance().getCurrentActiveComps();
              boolean coexitsComp = false;
              // PwdDialog mPwdDialog = (PwdDialog)
              // ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_PWD_DLG);
              // if (mPwdDialog == null) {
              if (!cpmsIDs.contains(NAV_COMP_ID_FAV_LIST)
                  && !cpmsIDs.contains(NAV_COMP_ID_MISC)
                  && !cpmsIDs.contains(NAV_COMP_ID_SUNDRY)
                  && ((!UImanager.showing) && (!(StateDvrFileList.getInstance() != null && StateDvrFileList
                  .getInstance().isShowing())))
                  && cpmsIDs.contains(NAV_COMP_ID_BANNER)
                  && !((com.android.tv.menu.MenuOptionMain) ComponentsManager.getInstance().getComponentById(
                  NavBasic.NAV_COMP_ID_MENU_OPTION_DIALOG)).isShowing()) {
                  if (CommonIntegration.getInstance().isCurrentSourceBlocked()) {
                      if (!isUKCountry) {
                          BannerView banner1 = (BannerView) ComponentsManager.getInstance().getComponentById(
                              NavBasic.NAV_COMP_ID_BANNER);
                          if (banner1.PWD_SHOW_FLAG == 1) {
                              coexitsComp = false;
                          } else {
                              coexitsComp = true;
                          }
                      }
                  } else {
                      if (!isUKCountry) {
                          BannerView banner = (BannerView) ComponentsManager.getInstance().getComponentById(
                              NavBasic.NAV_COMP_ID_BANNER);
                          if (banner.getSimpleBanner().getChannelNameVisibility() == View.VISIBLE
                              || !isPutyDigitalStr(banner.getSimpleBanner().getFirstLineStr())) {
                              //remove for CR DTV00828172, not show pwd dialog by show banner
                              //coexitsComp = true;
                              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isPutyDigitalStr");
                          }
                      }
                  }
              }
              if (isContentBlock()) {
                  coexitsComp = true;
              }
              if (coexitsComp) {
                  if (isContentBlock()) {
                      update_flag = PASSWORD_VIEW_SHOW_PWD_INPUT;
                  } else {
                      update_flag = mtkTvPwd.PWDShow();
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus mtkTvPwd.PWDShow()=" + mtkTvPwd.PWDShow());
                  }
                  com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus update_flag=" + update_flag);
                  if (CommonIntegration.getInstance().isPipOrPopState()) {
                      int showFlag = mtkTvPwd.PWDShow();
                      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus showFlag = " + showFlag);
                      switch (showFlag) {
                          case PASSWORD_VIEW_SHOW_PWD_INPUT:
                          case PASSWORD_VIEW_HINT_DVBS:
                          case PASSWORD_VIEW_PWD_ERROR:
                              mode = showFlag;
                              if (this.isShowing()) {
                                  showPasswordView(mode);
                              } else {
                                  comManager.showNavComponent(NAV_COMP_ID_PWD_DLG);
                              }
                              break;
                          default:
                              if (this.isShowing()) {
                                  dismiss();
                              }
                              break;
                      }
                  } else {
                      mHandler.removeMessages(UPDATE_MESSAGE);
                      mHandler.sendEmptyMessageDelayed(UPDATE_MESSAGE, 100);
                  }
              }
              com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "7777770000");
              break;
          case ComponentStatusListener.NAV_KEY_OCCUR:
              if (CommonIntegration.getInstance().isPipOrPopState()) {
                  switch (value) {
                      case KeyMap.KEYCODE_DPAD_LEFT:
                      case KeyMap.KEYCODE_DPAD_RIGHT:
                          int showFlag = mtkTvPwd.PWDShow();
                          com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "NAV_KEY_OCCUR showFlag = " + showFlag);
                          switch (showFlag) {
                              case PASSWORD_VIEW_SHOW_PWD_INPUT:
                              case PASSWORD_VIEW_HINT_DVBS:
                              case PASSWORD_VIEW_PWD_ERROR:
                                  mode = showFlag;
                                  if (this.isShowing()) {
                                      showPasswordView(mode);
                                  } else {
                                      comManager.showNavComponent(NAV_COMP_ID_PWD_DLG);
                                  }
                                  break;
                              case PASSWORD_VIEW_DISMISS_PWD_INPUT:
                                  if (isShowing()) {
                                      dismiss();
                                  }
                                  break;
                              default:
                                  com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "default");
                                  break;
                          }
                          break;
                      default:
                          com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "default");
                          break;
                  }
              }
              break;
          case ComponentStatusListener.NAV_CONTENT_ALLOWED: {
              boolean is3rd = CommonIntegration.getInstance().is3rdTVSource();
              if (mContext != null && is3rd && false ==
                  TurnkeyUiMainActivity.getInstance().getTvView().isContentBlock(is3rd)) {

                  TurnkeyUiMainActivity.getInstance().getTvView().unblockContent(mRating);
                  break;
              }

              int pwdFlag = mtkTvPwd.PWDShow();
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Pwd content allowed, the current PWDShow:" + pwdFlag);
              if (pwdFlag != 0) {
                  dismiss();
              }
              break;
          }
          case ComponentStatusListener.NAV_CONTENT_BLOCKED:
              com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Pwd content blocked");

              if (mContext != null &&
                  CommonIntegration.getInstance().is3rdTVSource()) {
                  TurnkeyUiMainActivity.getInstance().getTvView().blockContent();

                  if (DestroyApp.isCurActivityTkuiMainActivity()) {
                      comManager.showNavComponent(NAV_COMP_ID_PWD_DLG);
                  }
              }
              break;
          case ComponentStatusListener.NAV_CHANNEL_CHANGED:
              //modified begin by jg_jianwang for DTV01366338
        /*com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "PWD_NAV_CHANNEL_CHANGED");
        int showFlag = mtkTvPwd.PWDShow();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "change_channel showFlag = " + showFlag);
        switch (showFlag) {
          case PASSWORD_VIEW_SHOW_PWD_INPUT:
          case PASSWORD_VIEW_HINT_DVBS:
          case PASSWORD_VIEW_PWD_ERROR:
            mode = showFlag;
            if (this.isShowing()) {
              showPasswordView(mode);
            } else {
              comManager.showNavComponent(NAV_COMP_ID_PWD_DLG);
            }
            break;
          default:
            if (this.isShowing()) {
              dismiss();
            }
            break;
        }*/
              //modified end by jg_jianwang for DTV01366338
          default:
              break;
      }
  }

  public boolean isShowing(){
      return mPinDialogFragment!=null&&mPinDialogFragment.isShowing();
  }

  @Override
  public boolean isVisible() {
    return isShowing();
  }

  private void updateState() {
    // TODO Auto-generated method stub
    int showFlag = update_flag1;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateState showFlag = " + showFlag);
    switch (showFlag) {
      case PASSWORD_VIEW_SHOW_PWD_INPUT:
      case PASSWORD_VIEW_HINT_DVBS:
      case PASSWORD_VIEW_PWD_ERROR:
        mode = showFlag;
        boolean isMenuShow = ((com.android.tv.menu.MenuOptionMain)
            ComponentsManager.getInstance().getComponentById(
                NavBasic.NAV_COMP_ID_MENU_OPTION_DIALOG)).isShowing();
        if (isShowing()) {
          showPasswordView(mode);
        }
        else if (!isMenuShow){
          comManager.showNavComponent(NAV_COMP_ID_PWD_DLG);
        }
        break;

      default:
        if (this.isShowing()) {
          dismiss();
        }
        break;
    }
  }

  public void updatePwd() {// source block show this
    if (CommonIntegration.getInstance().isCurrentSourceBlocked()) {
      int showFlag = mtkTvPwd.PWDShow();
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus showFlag = " + showFlag);
      switch (showFlag) {
        case PASSWORD_VIEW_SHOW_PWD_INPUT:
        case PASSWORD_VIEW_HINT_DVBS:
        case PASSWORD_VIEW_PWD_ERROR:
          mode = showFlag;
          if (this.isShowing()) {
            showPasswordView(mode);
          } else {
            comManager.showNavComponent(NAV_COMP_ID_PWD_DLG);
          }
          break;
        default:
          if (this.isShowing()) {
            dismiss();
          }
          break;
      }
    }
  }

  private boolean isPutyDigitalStr(String str) {
    if (str == null) {
      return false;
    }
    String mathchStr = "[0-9]+";
    return str.matches(mathchStr);
  }

  public boolean isContentBlock() {
    return isContentBlock(CommonIntegration.getInstance().is3rdTVSource());
  }

  public boolean isContentBlock(boolean is3rdChannel) {
    return (is3rdChannel && mRating != null) ||//for cts verifer
        TurnkeyUiMainActivity.getInstance().getTvView().isContentBlock(is3rdChannel);
  }

  public static void setContentBlockRating(TvContentRating rating) {
    mRating = rating;
  }
}
