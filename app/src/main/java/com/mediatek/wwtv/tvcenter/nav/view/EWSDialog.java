package com.mediatek.wwtv.tvcenter.nav.view;

import java.io.IOException;

import android.media.MediaPlayer;
import com.mediatek.twoworlds.tv.MtkTvEWSPABase;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvEWSPABase.EwsInfo;
import com.mediatek.twoworlds.tv.MtkTvEWSPABase.TmdwInfoMsg;
import com.mediatek.twoworlds.tv.MtkTvUtil;
import com.mediatek.wwtv.tvcenter.R;
import android.media.AudioManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
//import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentStatusListener;
import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
import com.mediatek.wwtv.tvcenter.nav.util.MtkTvEWSPA;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.wwtv.tvcenter.util.AudioFocusManager;

public class EWSDialog extends NavBasicDialog implements
        ComponentStatusListener.ICStatusListener {

    private static final String TAG = "EWSDialog";
    private static final int EWS_CREATE_MONITOR = 1;
    private int volume = 0;//get system volume value.
    private int ewsStatus = 0;

    public static final int STATUS_AWAS = 1;
    public static final int STATUS_SIAGA = 2;
    public static final int STATUS_WASPADA = 3;

    private ImageView disasterLogoView;
    private ImageView authorityView;
    private TextView statusView;
    private TextView areaView;
    private TextView bencanaView;
    private TextView tanggalView;
    private TextView posisiView;
    private TextView karakterView; 
    private TextView infoView;
    private TextView waspadaStatusView;
    private TextView waspadaAreaView;
    private LinearLayout middleLayout;

    private Drawable[] disasterLogoDrawables;
    private Drawable[] authorityDrawables;
    private Context context;
    private MediaPlayer mPlayer;
    private MtkTvEWSPA mMtkTvEWSPA;
    private EwsInfo ewsInfo;

    private boolean isPlayTone = false ;
    private LinearLayout awasSiagaTitleRootLayout;
    private LinearLayout waspadaTitleRootLayout;
    private AudioManager audioManager;
    private boolean isStreamMute = false;
    private boolean isCreatedMonitor = false;
    private InputSourceManager inputSourceManager;

    public EWSDialog(Context context, int theme) {
        super(context, theme);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "new EWSDialog");
        this.componentID = NAV_COMP_ID_EWS;
        this.context = context;
        mHandler.sendEmptyMessageDelayed(EWS_CREATE_MONITOR, 3000);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sendEmptyMessageDelayed to do create monitor!" );
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    public EWSDialog(Context context) {
        this(context, R.style.nav_dialog);
    }

    @Override
    public boolean isCoExist(int componentID) {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_ews_view);
        findViews();
        prepareResource();
        mPlayer = new MediaPlayer();
        audioManager = ((AudioManager)mContext.getApplicationContext()
                .getSystemService(Context.AUDIO_SERVICE));
        inputSourceManager = InputSourceManager.getInstance();
    }

    @Override
    public boolean isKeyHandler(int keyCode) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "isKeyHandler,keyCode:" + keyCode);
//      if (ewsStatus == STATUS_AWAS || ewsStatus == STATUS_SIAGA) {
//          return true ;
//      }
        return false;
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchKeyEvent||ewsStatus =" + ewsStatus);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchKeyEvent||getKeyCode =" + event.getKeyCode());
        switch (ewsStatus) {
        case STATUS_AWAS:
            break;
        case STATUS_SIAGA:
            break;
        case STATUS_WASPADA:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "dispatchKeyEvent||STATUS_WASPADA");
            dismiss();
            break;
        default:
            break;
        }
        return super.dispatchKeyEvent(event);
//      return true;
    }

    @Override
    public void show() {
      CommonIntegration commonIntegration = CommonIntegration.getInstance();
      if (!commonIntegration.isCurrentSourceDTV()) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showEWS||CurrentSource not DTV");
        return;
      }
        super.show();

        if (mMtkTvEWSPA != null) {
            ewsInfo = mMtkTvEWSPA.getEWSInfo((byte) 0);

            if (ewsInfo == null) {
            	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "showEWS||ewsInfo is null");
            	return;
            }else {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ewsInfo:" + "locationTypeCode = "+ ewsInfo.locationTypeCode);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ewsInfo:" + "disasterCode = "+ ewsInfo.disasterCode);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ewsInfo:" + "authority = "+ ewsInfo.authority);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ewsInfo:" + "charLocationCode = "+ ewsInfo.charLocationCode);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ewsInfo:" + "charDisasterCode = "+ ewsInfo.charDisasterCode);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ewsInfo:" + "charDisasterDate = "+ ewsInfo.charDisasterDate);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ewsInfo:" + "charDisasterPosition = "+ ewsInfo.charDisasterPosition);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "ewsInfo:" + "charDisasterCharacterstic = "+ ewsInfo.charDisasterCharacterstic);
            }
            if(inputSourceManager == null){
                inputSourceManager = InputSourceManager.getInstance();
            }
            if(inputSourceManager != null){
                inputSourceManager.disableCECOneTouchPlay(true);
            }
            ewsStatus = ewsInfo.locationTypeCode;
            com.mediatek.wwtv.tvcenter.util.MtkLog.i(TAG, "ewsStatus== "+ ewsStatus);
            sendIRControl(ewsStatus);
            setWindowPosition();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "EWSDialog show");
            showEWSDialog();
        }else {
            dismiss();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        sendIRControl(-1);
        stopTone();
        if(inputSourceManager == null){
            inputSourceManager = InputSourceManager.getInstance();
        }
        if(inputSourceManager != null){
            inputSourceManager.disableCECOneTouchPlay(false);
        }
    }
/*
 * Add ch+/- key event to display all text,including all 3 levels
 *
 */
    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
        return onKeyHandler(keyCode, event);
    }

    @Override
    public boolean onKeyHandler(int keyCode, KeyEvent event) {
        boolean isHandle = true;
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler");

        switch (keyCode) {
        case KeyMap.KEYCODE_BACK:
            if (ewsStatus == STATUS_WASPADA) {
                dismiss();
            }
            break;
        case KeyMap.KEYCODE_MENU:
        case KeyMap.KEYCODE_MTKIR_CHUP:
        case KeyMap.KEYCODE_MTKIR_CHDN:
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onKeyHandler||dismiss");
            if (ewsStatus == STATUS_WASPADA) {
                dismiss();
            }
            isHandle = false;
            break;
        case KeyMap.KEYCODE_DPAD_UP:
        case KeyMap.KEYCODE_DPAD_DOWN:
            if (ewsStatus == STATUS_WASPADA) {
                isHandle = false;
            } else {
                isHandle = true;
            }
            break;
        default:
            isHandle = false;
            break;
        }
        if (!isHandle && TurnkeyUiMainActivity.getInstance() != null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "TurnkeyUiMainActivity");
            return TurnkeyUiMainActivity.getInstance().onKeyHandler(keyCode,
                    event);
        }
        return isHandle;
    }

    @Override
    public void updateComponentStatus(int statusID, int value) {
    	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "updateComponentStatus");
    }

    private void findViews() {
        disasterLogoView = (ImageView) findViewById(R.id.disaster_logo);
        authorityView = (ImageView) findViewById(R.id.disaster_authority);
        awasSiagaTitleRootLayout = (LinearLayout)findViewById(R.id.title_info_root_for_awas_siaga);
        waspadaTitleRootLayout = (LinearLayout)findViewById(R.id.title_info_root_for_waspada);
        statusView = (TextView) findViewById(R.id.disaster_status);
        areaView = (TextView) findViewById(R.id.disaster_area);
        waspadaStatusView = (TextView) findViewById(R.id.disaster_status_for_waspada);
        waspadaAreaView = (TextView) findViewById(R.id.disaster_area_for_waspada);
        bencanaView = (TextView) findViewById(R.id.ews_bencana);
        tanggalView = (TextView) findViewById(R.id.ews_tanggal);
        posisiView = (TextView) findViewById(R.id.ews_posisi);
        karakterView = (TextView) findViewById(R.id.ews_karakter);
        infoView = (TextView) findViewById(R.id.infomation);
        middleLayout = (LinearLayout) findViewById(R.id.ews_middle_lay);

        waspadaAreaView.setSelected(true);
        areaView.setSelected(true);
        bencanaView.setSelected(true);
        tanggalView.setSelected(true);
        posisiView.setSelected(true);
        karakterView.setSelected(true);
    }

    private void prepareResource() {
        String[] logoStrings = null;
        TypedArray typedArray = null;
        try {
            logoStrings = context.getResources().getStringArray(
                R.array.ews_disaster_logo);
            typedArray = context.getResources().obtainTypedArray(
                    R.array.ews_disaster_logo);
            disasterLogoDrawables = new Drawable[logoStrings.length];
            for (int i = 0; i < logoStrings.length; i++) {
               disasterLogoDrawables[i] = typedArray.getDrawable(i);
            }
            // authority
            String[] logoStrings1 = context.getResources().getStringArray(
                     R.array.ews_authority);
            TypedArray typedArray1 = context.getResources().obtainTypedArray(
                     R.array.ews_authority);
            authorityDrawables = new Drawable[logoStrings1.length];
            for (int j = 0; j < logoStrings1.length; j++) {
                authorityDrawables[j] = typedArray1.getDrawable(j);
            }
        } catch (Exception e) {
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getStringArray exception");
        }

        try {
            if(typedArray != null){
                typedArray.recycle();}
        } catch (Exception e) {
        	com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "recycle exception");
        }
    }

    private String getEwsStatus(int ews) {
        String ewsString = "";
        switch (ews) {
        case 1:
            ewsString = "AWAS";
            break;
        case 2:
            ewsString = "SIAGA";
            break;
        case 3:
            ewsString = "WASPADA";
            break;
        default:
            break;
        }
        return ewsString;
    }
    /**
     *   [MTK Internal] This API is for MTK control IR use only.
     *   IR Remote related features
     *   @value      case 0: Ignore All IR Key except KEY_UP and KEY_DOWN
     *               case 1: Ignore All IR Key
     *               case 2: Ignore All IR except system Key
     *               case 3: Restart IR Key
     *for case STATUS_AWAS and STATUS_SIAGA,modify to respond to ch+/- key event
     **/
    private void sendIRControl (int ewsStatusInt){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "sendIRControl||ewsStatusInt =: " + ewsStatusInt);
        switch (ewsStatusInt) {
        case STATUS_AWAS:
            MtkTvUtil.IRRemoteControl(0);
            break;
        case STATUS_SIAGA:
            MtkTvUtil.IRRemoteControl(0);
            break;
        case STATUS_WASPADA:
            MtkTvUtil.IRRemoteControl(3);
            break;
        default:
            MtkTvUtil.IRRemoteControl(3);
            break;
        }
    }
/**
 * should be mute TV program audio and keep EWS file play.
 */
    private void playTone() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "playTone");

        AudioFocusManager.getInstance(mContext).muteTVAudio(
            AudioFocusManager.AUDIO_EWBS);

        try {
            android.content.res.AssetFileDescriptor afd =
                context.getResources().getAssets().openFd("music/IndonesiaEWS.mp3");
            if (afd != null) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "file exist");
                mPlayer.reset();
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.setDataSource(afd.getFileDescriptor());
                mPlayer.setLooping(true);
                mPlayer.prepare();
                mPlayer.start();
                setPlayTone(true);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,40,0);
                int volumeT = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "volumeT:" + volumeT);
            }
        } catch (IllegalArgumentException e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "IllegalArgumentException:" + e.getMessage());
        } catch (IllegalStateException e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "IllegalStateException:" + e.getMessage());
        } catch (IOException e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "IOException:" + e.getMessage());
        } catch (Exception e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Exception:" + e.getMessage());
        }
    }
 /**
 * we need stop audio player when state changed .
 * call setvolume only when ewsStatus is level 1 or 2
 */
    private void stopTone(){
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG ,"stopTone||ewsStatus =" + ewsStatus + "||volume = " + volume + ", isStreamMute>> " + isStreamMute);

        AudioFocusManager.getInstance(mContext).unmuteTVAudio(
            AudioFocusManager.AUDIO_EWBS);

        if (ewsStatus == STATUS_AWAS ||ewsStatus == STATUS_SIAGA
                || (ewsStatus == STATUS_WASPADA && isPlayTone)) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,volume,0);
            if(isStreamMute){
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                isStreamMute = false;
            }
        }

        try {
            mPlayer.stop();
            mPlayer.reset();
            setPlayTone(false);
        } catch (Exception e) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "stopTone(); --> exception ");
        }
    }

    public void setWindowPosition() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setWindowPosition");
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (ewsStatus == STATUS_AWAS ) {
            lp.width = ScreenConstant.SCREEN_WIDTH;
            lp.height = ScreenConstant.SCREEN_HEIGHT;
            lp.x = 0;
            lp.y = 0;
            lp.alpha = 1f ;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "STATUS_AWAS");
            window.setAttributes(lp);
        }else if ( ewsStatus == STATUS_SIAGA) {
            lp.width = (int) (ScreenConstant.SCREEN_WIDTH * 0.9);
            lp.height = (int) (ScreenConstant.SCREEN_HEIGHT * 0.9);
//            lp.x = (int) (ScreenConstant.SCREEN_WIDTH * 0.05);
//            lp.y = (int) (ScreenConstant.SCREEN_HEIGHT * 0.05);
            lp.x = 0;
            lp.y = 0;
//            lp.gravity = Gravity.CENTER ;
            lp.alpha = 1f ;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "STATUS_SIAGA");
            window.setAttributes(lp);
        } else if (ewsStatus == STATUS_WASPADA) {
            lp.width = ScreenConstant.SCREEN_WIDTH;
            lp.height = (int) (ScreenConstant.SCREEN_HEIGHT * 0.45);
            lp.y = ScreenConstant.SCREEN_HEIGHT - (int) (ScreenConstant.SCREEN_HEIGHT * 0.45);
            lp.x = 0;
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "STATUS_WASPADA");
            window.setAttributes(lp);
            }
    }

//  private void
    public void showEWSDialog() {
//        int iDisasterCode = ewsInfo.disasterCode - 1;
        int authority = ewsInfo.authority - 1 < 0?0:ewsInfo.authority - 1;
        String sLocationCode = ewsInfo.charLocationCode;
        String sDisasterCode = ewsInfo.charDisasterCode;
        String sDisasterDate = ewsInfo.charDisasterDate;
        String sDisasterPosi = ewsInfo.charDisasterPosition;
        String sDisasterChar = ewsInfo.charDisasterCharacterstic;
        TmdwInfoMsg infoMsg[] = ewsInfo.infoMsg;
//        int infoMsgNum = ewsInfo.charInfoMsgNum;
        String msgString = getMsgString(infoMsg);

        if(ewsStatus != STATUS_WASPADA && !isPlayTone){
            if(audioManager.isStreamMute(AudioManager.STREAM_MUSIC)){
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                isStreamMute = true;
                volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }else{
                volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            }

        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG ,"showEWSDialog||volume = " + volume + ",isPlayTone>>"+isPlayTone
                + ", isStreamMute>> "+ isStreamMute + ", ewsStatus>> "+ ewsStatus);

        if (ewsStatus == STATUS_AWAS || ewsStatus == STATUS_SIAGA) {
            middleLayout.setVisibility(View.VISIBLE);
//            disasterLogoView.setImageDrawable(disasterLogoDrawables[iDisasterCode]);
            showDisaster();
            authorityView.setImageDrawable(authorityDrawables[authority]);

            if (STATUS_AWAS == ewsStatus) {
                statusView.setTextColor(context.getResources().getColor(
                        R.color.red_new));
            } else {
                statusView.setTextColor(context.getResources().getColor(
                        R.color.orange));
            }
            waspadaTitleRootLayout.setVisibility(View.GONE);
            awasSiagaTitleRootLayout.setVisibility(View.VISIBLE);
            statusView.setText(context.getResources().getString(R.string.ews_status, getEwsStatus(ewsStatus)));
            areaView.setText(sLocationCode);
            bencanaView.setText(sDisasterCode);
            tanggalView.setText(sDisasterDate);
            posisiView.setText(sDisasterPosi);
            karakterView.setText(sDisasterChar);
            infoView.setText(String.format("%s,%s,Daerah Anda:Status %s", sDisasterCode, msgString, getEwsStatus(ewsStatus)));
            playTone();
        }else if (ewsStatus == STATUS_WASPADA) {
            if (isPlayTone) {
                stopTone();
            }
            middleLayout.setVisibility(View.GONE);
//            disasterLogoView.setImageDrawable(disasterLogoDrawables[iDisasterCode]);
            showDisaster();
            authorityView.setImageDrawable(authorityDrawables[authority]);
            waspadaTitleRootLayout.setVisibility(View.VISIBLE);
            awasSiagaTitleRootLayout.setVisibility(View.GONE);

            waspadaStatusView.setTextColor(context.getResources().getColor(R.drawable.green));
            waspadaStatusView.setText(String.format("%s:", context.getResources().getString(R.string.ews_status, getEwsStatus(ewsStatus))));
            waspadaAreaView.setText(sLocationCode);
            infoView.setText(String.format("%s,%s,%s,%s,%s,Daerah Anda:Status %s",
                    sDisasterCode, sDisasterDate, sDisasterPosi, sDisasterChar, msgString, getEwsStatus(ewsStatus)));
        }else {
// ewsinfo wrong
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "EWS INFO == null or bad ");
        }
    }
    private void showDisaster(){
        if (ewsInfo.disasterCode < 16 && ewsInfo.disasterCode >0 ) {
            disasterLogoView.setVisibility(View.VISIBLE);
            disasterLogoView.setImageDrawable(disasterLogoDrawables[ewsInfo.disasterCode-1]);
        }else if (ewsInfo.disasterCode == 255) {
            disasterLogoView.setVisibility(View.VISIBLE);
            disasterLogoView.setImageResource(R.drawable.disaster_15_warning);
        }else {
            disasterLogoView.setVisibility(View.INVISIBLE);
        }
    }

    private String getMsgString(TmdwInfoMsg[] infoMsg) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getMsgString");
        StringBuffer sBuffer = new StringBuffer();
        for (TmdwInfoMsg info:infoMsg) {
            sBuffer.append(info.charInfoMsg);
        }
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "getmsgString end,msg:" + sBuffer.toString());
        return sBuffer.toString();
    }

    public boolean isPlayTone() {
        return isPlayTone;
    }

    public void setPlayTone(boolean isPlayTone) {
        this.isPlayTone = isPlayTone;
    }

	public static byte [] stringToByte(String s) {
        byte[] b = new byte[3];
        if (s != null && s.length() == 5) {
        	byte[] bytes = s.getBytes();
        	for (int i = 0; i < bytes.length; i++) {
                System.out.println(i + "  = "
                        + Integer.toBinaryString(bytes[i] - 48));
        	}
        	b[0] = (byte) (((bytes[0] - 48) * 16) | (bytes[1] - 48));
        	b[1] = (byte) (((bytes[2] - 48) * 16) | (bytes[3] - 48));
        	b[2] = (byte) (((bytes[4] - 48) * 16) | (0x0F));
        }
        return b;
	}

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "handleMessage:" + msg.what + isCreatedMonitor);
            switch (msg.what){
                case EWS_CREATE_MONITOR:
                    if(!isCreatedMonitor){
                        mMtkTvEWSPA = MtkTvEWSPA.getInstance();
                        mMtkTvEWSPA.createMonitorInst((byte) 0x00);
                        String value = MtkTvConfig.getInstance().getConfigString(CommonIntegration.OCEANIA_POSTAL);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "postalcode:["+value+"]");
                        MtkTvEWSPABase.EwspaRet ret = MtkTvEWSPA.getInstance().setLocationCode(
                                stringToByte(value));
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setLocationCode response:" + ret);
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "createMonitorInst");
                        isCreatedMonitor = true;
                        CommonIntegration.getInstance().setIsCreatedMonitor(true);
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
