package com.android.tv.license;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class LicenseRecyclerView extends RecyclerView {

    public  int startPostion;
    private int moveToPosition;
    private int lastKeyCode = 0;
    public LicenseRecyclerView(@NonNull Context context) {
        super(context);
    }

    public LicenseRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LicenseRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
            if(moveToPosition - 1 < getAdapter().getItemCount()) {
                if(lastKeyCode != 0 && lastKeyCode != keyCode){
                    moveToPosition += startPostion;
                }
                moveToPosition += 10;
                smoothScrollToPosition(moveToPosition);
                lastKeyCode = keyCode;
            }else{
                smoothScrollToPosition(getAdapter().getItemCount());
                moveToPosition = getAdapter().getItemCount();
            }
        }else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
            if(moveToPosition + 1> startPostion) {
                if(lastKeyCode != 0 && lastKeyCode != keyCode){
                    moveToPosition -= startPostion;
                }
                moveToPosition -= 10;
                smoothScrollToPosition(moveToPosition);
                lastKeyCode = keyCode;
            }else{
                smoothScrollToPosition(0);
                moveToPosition = 0;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        return super.onKeyUp(keyCode, event);
    }

    public void setStartPostion(int startPostion) {
        this.startPostion = startPostion;
        this.moveToPosition = startPostion;
    }
}
