package com.mediatek.wwtv.setting.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.mediatek.wwtv.tvcenter.R;

public class JumpPageDialog extends Dialog {
    private OnJumpPageClickListener mListener;
    private EditText editText;
    private Context mContext;
    private static final String TAG = "JumpPageDialog";

    public JumpPageDialog(Context context, int themeResId, OnJumpPageClickListener listener) {
        super(context, themeResId);
        mContext = context;
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_jump_page_dialog);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        init();
        editText.setFocusable(true);
        editText.requestFocus();
        editText.post(new Runnable() {
			
			@Override
			public void run() {
				closeSoftKeybord(editText, mContext);
			}
		});
    }
    
    private void init(){
        editText = findViewById(R.id.simple_dialog_content);
        Button mButtonConfirm = findViewById(R.id.simple_dialog_confirm);
        mButtonConfirm.setVisibility(View.VISIBLE);
        mButtonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = editText.getText().toString();
                mListener.onClick(str);
                dismiss();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "KeyHandler||code =" + keyCode + "mContext" + mContext);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                dismiss();
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void closeSoftKeybord(EditText mEditText, Context mcContext) {
		InputMethodManager imm = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
	}


    public interface OnJumpPageClickListener {
        void onClick(String channelNum);
    }
}


