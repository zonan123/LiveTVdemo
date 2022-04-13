package com.mediatek.wwtv.setting.view;

import java.io.File;
import java.io.FileInputStream;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

public class LicenseInfoDialog extends Dialog {
    private TextView vLicenseInfo;
    private TextView vPageNum;
    Window window;
    WindowManager.LayoutParams lp;
    public int width = 0;
    public int height = 0;
    private static String TAG = "LicenseInfoView";
    private int PAGETEXTCOUNT = 1600;
    private int PAGENUM = 1;
    private int mCurrentPage;
    boolean isPositionView = false ;
    private Context mContext;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            vLicenseInfo.setText(noticeString);
            mCurrentPage = 1;
            PAGENUM = noticeString.length() / PAGETEXTCOUNT
                    + (noticeString.length() % PAGETEXTCOUNT == 0 ? 0 : 1);
            vPageNum.setText(mContext.getResources().getString(R.string.license_info_dialog_sign, mCurrentPage,PAGENUM) );
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("LicenseInfoView",
             "PAGENUM:"+PAGENUM+"noticeString.length():"+noticeString.length());
        }
    };

    public LicenseInfoDialog(Context context) {
        super(context, R.style.Theme_TurnkeyCommDialog);
//        mContext = context;
        window = getWindow();
        mContext = context;
        lp = window.getAttributes();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_license_info);
        width = (int) (ScreenConstant.SCREEN_WIDTH * 0.45);
        lp.width = width;
        height = (int) (ScreenConstant.SCREEN_HEIGHT * 0.85);
        lp.height = height;
        lp.x = 0 - lp.width / 3;
        window.setAttributes(lp);
        init();
    }

    public void setPositon(int xoff, int yoff) {
            Window window = this.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.x = xoff;
            lp.y = yoff;
//            this.xOff = xoff;
//            this.yOff = yoff;
            window.setAttributes(lp);
      }

    public void init() {
//        mTVContent = TVContent.getInstance(this.mContext);
        vLicenseInfo = (TextView)findViewById(R.id.common_license_name);
        vPageNum = (TextView)findViewById(R.id.common_pagenum);
        getLicenseInfo();
    }

    public void getLicenseInfo() {
        new Thread() {

            @Override
            public void run() {
                super.run();
                noticeString = "";
                reshFile("/");
                mHandler.sendEmptyMessage(0);
            }

        }.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    public void onKeyLeft() {
        handleKey(KeyEvent.KEYCODE_DPAD_LEFT);
    }

    public void onKeyRight() {
        handleKey(KeyEvent.KEYCODE_DPAD_RIGHT);
    }

    public void handleKey(int keycode) {
        switch (keycode) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
            prePage();
            break;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            nextPage();
            break;
        default:
            break;
        }
    }

    private void nextPage() {
        if (PAGENUM == 0) {
            return;
        }
        mCurrentPage++;
        if (mCurrentPage > PAGENUM) {
            mCurrentPage = PAGENUM;
        }
        int end = mCurrentPage * PAGETEXTCOUNT;
        if (end > noticeString.length()) {
            end = noticeString.length();
        }
        vLicenseInfo.setText(noticeString.substring((mCurrentPage - 1)
                * PAGETEXTCOUNT, end));
        vPageNum.setText(mContext.getResources().getString(R.string.license_info_dialog_sign, mCurrentPage,PAGENUM));
    }

    private void prePage() {
        if (PAGENUM == 0) {
            return;
        }
        mCurrentPage--;
        if (mCurrentPage < 1) {
            mCurrentPage = 1;
        }
        int end = mCurrentPage * PAGETEXTCOUNT;
        if (end > noticeString.length()) {
            end = noticeString.length();
        }
        vLicenseInfo.setText(noticeString.substring((mCurrentPage - 1)
                * PAGETEXTCOUNT, end));
        vPageNum.setText(mContext.getResources().getString(R.string.license_info_dialog_sign, mCurrentPage,PAGENUM));
    }

    String noticeString = "";

    private void reshFile(String path) {
        File file = new File(path);
        File[] arrFiles = file.listFiles();
        if (arrFiles != null) {
            for (File mFile : arrFiles) {
                if (mFile != null
                        && mFile.isDirectory()
                        && (!mFile.getPath().startsWith("/sys") && !mFile
                                .getPath().startsWith("/proc"))) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                            "arrFiles[i].getPath():" + mFile.getPath());
                    reshFile(mFile.getPath());
                } else {
                    if (mFile != null && mFile.isFile()
                            && mFile.getName().contains("NOTICE")) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(
                                TAG,
                                " maybe licence ->file :"
                                        + mFile.getName());
                        if (mFile.getName().endsWith(".txt")) {
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,
                                    "NOTICE file Name" + mFile.getPath());
                            readFile(mFile);
                            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "NOTICE noticeString:" + noticeString);
                        }
                    }
                }
            }
        }
    }

    private void readFile(File file) {
        FileInputStream fin =null;
        try {
            fin = new FileInputStream(file);
            int length = fin.available();
            byte[] buffer = new byte[length];
            int bufferReadInt= fin.read(buffer);
            noticeString += new String(buffer, "UTF-8");
            Log.d("readFile", "error==" + noticeString+bufferReadInt);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("readFile", "error==" + e.getMessage());
        }finally{
            try {
                if(fin!=null){
                    fin.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                com.mediatek.wwtv.tvcenter.util.MtkLog.d("readFile", "error==" + e.getMessage());
            }
        }
    }
}
