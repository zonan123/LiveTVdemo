package com.mediatek.wwtv.tvcenter.commonview;

//import android.os.Bundle;
//import android.os.Message;
//import android.net.Uri;
import android.content.Context;
//import android.content.Intent;
import android.view.View;
import android.util.AttributeSet;
//import android.media.PlaybackParams;



/**
 * TvBlockView
 *
 * @author MTK40707
 *
 */
public class TvBlockView extends View {
    private static final String TAG = "TvBlockView";

    public static final int BLOCK_EMPTY                           = 0x0;
    public static final int BLOCK_COMMON                          = 0x1;
    public static final int BLOCK_3RD_CHANNEL_INPUT_BLOCK         = 0x2;
    public static final int BLOCK_BY_EVENT                        = 0x4;
    public static final int BLOCK_PVR_LAST_MEMORY_BLOCK           = 0x8;
    public static final int BLOCK_UNTIL_TUNE_INPUT_AFTER_BOOT     = 0x10;

    private int mBlockStatus = 0;

    public TvBlockView(Context context){
        super(context);
    }

    public TvBlockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TvBlockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setVisibility(int visibility) {
        setVisibility(visibility, BLOCK_COMMON);
    }

    public void setVisibility(int visibility, int bitMask) {
        if(visibility == View.GONE) {
            mBlockStatus = mBlockStatus & (~bitMask);
            if(mBlockStatus == BLOCK_EMPTY) {
                super.setVisibility(visibility);
            }
        }
        else {
            mBlockStatus |= bitMask;
            super.setVisibility(visibility);
        }
		com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "mBlockStatus=" + mBlockStatus);
    }

    public boolean isBlock() {
    	return mBlockStatus != BLOCK_EMPTY;
    }

	public boolean isBlock(int bitMask) {
    	return (mBlockStatus & bitMask) != 0;
	}
}
