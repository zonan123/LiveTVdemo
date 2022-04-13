package com.mediatek.wwtv.tvcenter.util;

import android.util.Log;
import android.view.KeyEvent;
import com.mediatek.twoworlds.tv.MtkTvKeyEvent;
import com.mediatek.twoworlds.tv.MtkTvHtmlAgentBase;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.FVP;
import com.mediatek.wwtv.tvcenter.nav.view.Hbbtv;
import com.mediatek.wwtv.tvcenter.nav.view.TTXMain;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicMisc;

/** @author MTK40707 */
public final class KeyDispatch {
  private static final String TAG = "KeyDispatch";

  private static KeyDispatch mKeyDispatch = null;

  private final MtkTvKeyEvent mKey;
  private final MtkTvHtmlAgentBase mHtmlAgent;

  // the key passed to linux world
  private int mPassedAndroidKey = -1;
  private int mPassedAndroidScanKey = -1;

  private boolean mIsLongpressed = false;

  private KeyDispatch() {
    mHtmlAgent = new MtkTvHtmlAgentBase();
    mKey = MtkTvKeyEvent.getInstance();
  }

  public static synchronized KeyDispatch getInstance() {
    if (mKeyDispatch == null) {
      mKeyDispatch = new KeyDispatch();
    }

    return mKeyDispatch;
  }

  /**
   * this method is used to check whether the value of key code is valid for native UI
   *
   * @param keyCode
   * @return true: Operation successfully; false,failed
   */
  private boolean isKeyValid(int keyCode) {
    switch (keyCode) {
        case KeyMap.KEYCODE_MENU:
        case KeyMap.KEYCODE_MTKIR_ANGLE:
            // case KeyMap.KEYCODE_MTKIR_MTSAUDIO:
            return false;
        case KeyMap.KEYCODE_MTKIR_REPEAT:
            TTXMain ttx = (TTXMain) ComponentsManager.getInstance()
                    .getComponentById(NavBasicMisc.NAV_COMP_ID_TELETEXT);
            if (NavBasic.NAV_NATIVE_COMP_ID_HBBTV == ComponentsManager
                    .getActiveCompId()) {
                return true;
            }
            if (ttx != null && ttx.isActive) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TTX is active,return true");
                return true;
            }
            return false;
        case KeyMap.KEYCODE_MTKIR_MUTE:
            return false;
        case KeyMap.KEYCODE_MTKIR_PRECH:
        case KeyMap.KEYCODE_MTKIR_CHDN:
        case KeyMap.KEYCODE_MTKIR_CHUP: {
            TTXMain ttxMain =
                    (TTXMain)
                            ComponentsManager.getInstance()
                                    .getComponentById(NavBasicMisc.NAV_COMP_ID_TELETEXT);
            if (ttxMain != null && ttxMain.isActive) {
                return true;
            }

            return ((FVP)
                    ComponentsManager.getInstance()
                            .getComponentById(NavBasicMisc.NAV_NATIVE_COMP_ID_FVP))
                    .isFVPActive();
        }
        case KeyMap.KEYCODE_MTKIR_SUBTITLE:
        case KeyMap.KEYCODE_MTKIR_MTKIR_CC:
        case KeyMap.KEYCODE_MTKIR_MTSAUDIO:
            if (ComponentsManager.getNativeActiveCompId() == NavBasicMisc.NAV_NATIVE_COMP_ID_HBBTV) {
                Hbbtv hbbtv = (Hbbtv) ComponentsManager.
                        getInstance().getComponentById(NavBasic.NAV_NATIVE_COMP_ID_HBBTV);
                if (hbbtv != null && hbbtv.getStreamBoolean()) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getNativeActiveCompId() = " + NavBasicMisc.NAV_NATIVE_COMP_ID_HBBTV);
                    return false;
                }
            }
            break;
        case KeyMap.KEYCODE_MTKIR_GUIDE:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getActiveCompId() = " + ComponentsManager.getActiveCompId());
            if(ComponentsManager.getActiveCompId() == NavBasicMisc.NAV_NATIVE_COMP_ID_GINGA){
                return false;
            }
            return true;
        default:
            break;
    }

    return true;
  }

    /**
     * this method is used to remove the object
     */
    static synchronized void remove() {
        mKeyDispatch = null;
    }

    public void gotoPage(final int flag){
        Log.i(TAG, "goToPage");
        com.mediatek.wwtv.tvcenter.util.TVAsyncExecutor.getInstance().execute(
            () -> {
                mHtmlAgent.goToPage(flag, "");
            });
    }
    public void gotoPage(boolean isEpg){
        gotoPage(isEpg ? MtkTvHtmlAgentBase.HTML_AGENT_PAGE_EPG : MtkTvHtmlAgentBase.HTML_AGENT_PAGE_MINI_GUIDE);
    }

    /**
     * this method is used to pass Click Key(DOWN & UP key) to linux world
     * @param keyCode
     * @param event
     * @return true: Operation successfully; false,failed
     */
    public boolean passKeyToNative(int keyCode, KeyEvent event){
        com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "passKeyToNative keyCode:" + keyCode + " event:" + event);
        int dfbkeycode = -1;

        switch(keyCode){
            case KeyMap.KEYCODE_MTKIR_GUIDE:
            case KeyMap.KEYCODE_MTKIR_SUBCODE: {
                FVP fvp = (FVP)ComponentsManager.getInstance().getComponentById(
                    NavBasicMisc.NAV_NATIVE_COMP_ID_FVP);
                if (fvp != null && fvp.available(keyCode)) {
                    gotoPage(keyCode == KeyMap.KEYCODE_MTKIR_GUIDE ?
                        MtkTvHtmlAgentBase.HTML_AGENT_PAGE_EPG :
                        MtkTvHtmlAgentBase.HTML_AGENT_PAGE_MINI_GUIDE);
                    return true;
                }
                break;
            }
            default: {
                Log.d(TAG, "passKeyToNative Goto_default");
                break;
            }
        }

        synchronized(KeyDispatch.class){
            //record
            mPassedAndroidKey = keyCode;
            mPassedAndroidScanKey = getScanCode(mPassedAndroidKey, event);
            if(mPassedAndroidScanKey != -1) {
                mPassedAndroidKey = mPassedAndroidScanKey;
            }
        }
    if (event == null) {
      dfbkeycode = mKey.androidKeyToDFBkey(mPassedAndroidKey);

      if ((dfbkeycode != -1) && (mKey.sendKeyClick(dfbkeycode) == 0)) {
        return true;
      }
    } else {
      if (isKeyValid(mPassedAndroidKey)) {
        dfbkeycode = mKey.androidKeyToDFBkey(mPassedAndroidKey, event);

        if (dfbkeycode == -1) {
          return false;
        }

        if(keyCode == KeyEvent.KEYCODE_SHIFT_LEFT ||
           keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
          if(event.getAction() == KeyEvent.ACTION_UP) {
            return (mKey.sendKey(1, dfbkeycode) == 0);
          }
          else if(event.getAction() == KeyEvent.ACTION_DOWN) {
            return (mKey.sendKey(0, dfbkeycode) == 0);
          }
        }

        if (event.getRepeatCount() > 0) {
          if (!mIsLongpressed) {
            mIsLongpressed = true;
            com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "repeat, mIsLongpressed = " + mIsLongpressed);
            return (mKey.sendKey(0, dfbkeycode) == 0);
          } else {
            /* need not send key again*/
            return true;
          }
        } else {
          return (mKey.sendKeyClick(dfbkeycode) == 0);
        } // repeat
      } // isKeyValid
    } // event

    return false;
  }

  /**
   * this method is used to send Key to linux world
   *
   * @param updown ,updown=0: KEYDOWN event, updown=1: KEYUP event
   * @param keyCode
   * @param event
   * @return true: Operation successfully; false,failed
   */
  public boolean passKeyToNative(int updown, int keyCode, KeyEvent event) {
    int dfbkeycode = -1;

    if ((updown == 1) && mIsLongpressed) {
      mIsLongpressed = false;
      com.mediatek.wwtv.tvcenter.util.MtkLog.v(TAG, "cancel, mIsLongpressed = " + mIsLongpressed);
    }

    synchronized (KeyDispatch.class) {
      // record
      mPassedAndroidKey = keyCode;
      mPassedAndroidScanKey = getScanCode(mPassedAndroidKey, event);
      if (mPassedAndroidScanKey != -1) {
        mPassedAndroidKey = mPassedAndroidScanKey;
      }
    }

    if (event == null) {
      dfbkeycode = mKey.androidKeyToDFBkey(mPassedAndroidKey);
    } else {
      if (isKeyValid(mPassedAndroidKey)) {
        dfbkeycode = mKey.androidKeyToDFBkey(mPassedAndroidKey, event);
      } else {
        return false;
      }
    }

    if (dfbkeycode == -1) {
      return false;
    }

    return mKey.sendKey(updown, dfbkeycode) == 0;
  }

  public int getPassedAndroidKey() {
    return mPassedAndroidKey;
  }

  public int androidKeyToDFBkey(int keycode) {
    synchronized (KeyDispatch.class) {
      if (mPassedAndroidScanKey == KeyMap.getCustomKey(KeyMap.KEYCODE_MTKIR_EPG)) {
        keycode = KeyMap.getCustomKey(KeyMap.KEYCODE_MTKIR_EPG); // for exit key
      } else if (mPassedAndroidScanKey == KeyMap.getCustomKey(KeyMap.KEYCODE_MTKBT_EPG)) {
        keycode = KeyMap.getCustomKey(KeyMap.KEYCODE_MTKBT_EPG); // for exit key
      }
    }

    return mKey.androidKeyToDFBkey(keycode);
  }

  public static int getScanCode(int keyCode, KeyEvent event) {
    if (keyCode == KeyMap.KEYCODE_BACK) {
      int value = (event == null) ? -1 : (event.getScanCode() + KeyMap.KEYCODE_SCANCODE_OFFSET);

      if (value == KeyMap.getCustomKey(KeyMap.KEYCODE_MTKIR_EPG)) {
        return value;
      } else if (value == KeyMap.getCustomKey(KeyMap.KEYCODE_MTKBT_EPG)) {
        return value;
      }
    }

    return -1;
  }

  /**
   * this method is used to send a Mouse Move Event to Linux world.
   *
   * @param abs_x
   * @param abs_y
   * @return true: Operation successfully; false,failed
   *     <p>public boolean sendMouseMove(int abs_x, int abs_y){ if(mKey.sendMouseMove(abs_x, abs_y)
   *     == 0){ return true; } else{ return false; } }
   */
  /**
   * this method is used to send a Mouse Button Event to Linux world.
   *
   * @param up, To indicate it is pressed or release. 0 = Pressed, 1 = Released.
   * @param button, To indicate which button generates this event. 1 = Left Button 2 = Right Button
   *     4 = Middle button.
   * @return true: Operation successfully; false,failed
   *     <p>public boolean sendMouseButton(int up, int button) { if(mKey.sendMouseButton(up, button)
   *     == 0){ return true; } else{ return false; } }
   */

  /**
   * this method is used to get the status of long pressed key
   *
   * @return
   */
  public boolean isLongPressed() {
    return mIsLongpressed;
  }
}
