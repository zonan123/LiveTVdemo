package com.mediatek.wwtv.setting.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

public class VersionInfoDialog extends Dialog {
    private TextView modelNameShow;
    private TextView versionShow;
    private TextView serialNumShow;
    private Context mContext;
    private TVContent mTVContent;
    Window window;
    WindowManager.LayoutParams lp;
    public int width = 0;
    public int height = 0;
    public VersionInfoDialog(Context context) {
        super(context, R.style.Theme_TurnkeyCommDialog);
        mContext = context;
        window = getWindow();
        lp = window.getAttributes();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_version_info);
        width = (int) (ScreenConstant.SCREEN_WIDTH * 0.55);
        lp.width = width;
        height = (int) (ScreenConstant.SCREEN_HEIGHT * 0.35);
        lp.height = height;
        lp.x = 0 - lp.width / 3;
        window.setAttributes(lp);

//      Window window = getWindow();
//      WindowManager.LayoutParams lp = window.getAttributes();
//      lp.width= ScreenConstant.SCREEN_WIDTH;
//      lp.height= ScreenConstant.SCREEN_HEIGHT;
//      window.setAttributes(lp);

        init();
    }

    public void setPositon(int xoff, int yoff) {
            Window window = this.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.x = xoff;
            lp.y = yoff;
            window.setAttributes(lp);
      }

    public void init() {
        mTVContent = TVContent.getInstance(this.mContext);
        modelNameShow = (TextView)findViewById(R.id.common_versioninfo_name_r);
        com.mediatek.wwtv.tvcenter.util.MtkLog.e("chengcl", "modelNameShow=="+modelNameShow);
        versionShow = (TextView) findViewById(R.id.common_versioninfo_ver_r);
        serialNumShow = (TextView)findViewById(R.id.common_versioninfo_num_r);

        setValue();

    }

    /**
     * read Model Name, Version and Serial Number from common logic
     */
    public void setValue() {
        String modelName ="";
        modelName = mTVContent.getSysVersion(3, modelName);
        com.mediatek.wwtv.tvcenter.util.MtkLog.e("chengcl", "modelNameShow=="+modelNameShow+"  modelName="+modelName);
        int index=modelName.lastIndexOf("_")+1;
        modelNameShow.setText(modelName.substring(index));
        String version ="";
        version = mTVContent.getSysVersion(0, version);
        versionShow.setText(version);
        String serialNum ="";
        serialNum = mTVContent.getSysVersion(2, serialNum);
        serialNumShow.setText(serialNum);
    }
}
