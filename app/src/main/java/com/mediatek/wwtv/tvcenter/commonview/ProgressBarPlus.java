package com.mediatek.wwtv.tvcenter.commonview;

import android.util.AttributeSet;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Color;
import android.widget.ProgressBar;

/**
 * This class is used to set progress text in the center of progress bar
 *
 * @author MTK40707
 *
 */
public class ProgressBarPlus extends ProgressBar {
    private String NAMESPACE = "http://schemas.android.com/apk/res/android";
    private Rect rect = new Rect();
    private String text;
    private Paint mPaint;

    public ProgressBarPlus (Context context) {
        super(context);
        init(null);
    }

    public ProgressBarPlus (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public ProgressBarPlus (Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @Override
    public synchronized void setProgress(int progress) {
        if(progress <= getMax()){
            setText(progress);
            super.setProgress(progress);
        }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.mPaint.getTextBounds(this.text, 0, this.text.length(), rect);
        int x = (getWidth() / 2) - rect.centerX();

        int y = (getHeight() / 2) - rect.centerY();
        canvas.drawText(this.text, x, y, this.mPaint);
    }

    /**
     * this method is used to init paint handler
     */
    private void init(AttributeSet attrs) {
        this.mPaint = new Paint();
        this.mPaint.setColor(Color.WHITE);

        if(attrs != null){
            int rId = attrs.getAttributeResourceValue(NAMESPACE, "textSize", -1);
            float textSize = (rId != -1) ? this.getResources().getDimension(rId) : 210;

            this.mPaint.setTextSize(textSize);
        }
    }

    /**
     * this method is used to set the text
     *
     * @param progress
     */
    private void setText(int progress) {
        int i = (progress * 100) / this.getMax();
        this.text = String.valueOf(i) + "%";
    }
}