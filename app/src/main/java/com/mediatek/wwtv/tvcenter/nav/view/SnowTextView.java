package com.mediatek.wwtv.tvcenter.nav.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.mediatek.wwtv.tvcenter.util.ScreenConstant;

public class SnowTextView extends TextView {
    private final static String TAG = "SnowTextView";

    public final static int DRAW_TEXT = 1;

    public final static boolean ANIMATOR = false;

    private Paint mPaint = new Paint();
    private Bitmap bitmap = null;

    public SnowTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    private Handler mHandler = new Handler() {
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//            case DRAW_TEXT:
//                SnowTextView.this.invalidate();
//                break;
//
//            default:
//                break;
//            }
//        }
//    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onDraw>>>" + canvas + "   " + bitmap);
            canvas.drawBitmap(
                bitmap,
                new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                new Rect(0, 0, this.getWidth(),  this.getHeight()),
                mPaint);
        }
    }

    public void setBitmap(Bitmap bmp) {
        bitmap = bmp;
        this.invalidate();
    }

    @Override
    public void setVisibility(int visibility) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setVisibility>>>" + visibility);
        if(ANIMATOR) {
            if (visibility == View.GONE) {
                clearAnimations();
            } else if (visibility == View.VISIBLE) {
                clearAnimations();
                setAnimator();
            }
        }

        super.setVisibility(visibility);
    }

    private void clearAnimations() {
        this.setAlpha(1);
        this.setRotationX(0);
        this.setRotationY(0);
        this.setTranslationX(0);
        this.setTranslationY(0);
        this.setScaleX(1);
        this.setScaleY(1);
    }

    private void setAnimator(){
        Interpolator accelerator = new LinearInterpolator();
        ObjectAnimator translation = ObjectAnimator.ofFloat(this, "translationX", -ScreenConstant.SCREEN_WIDTH/3f, ScreenConstant.SCREEN_WIDTH/3f);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(this, "translationY", -ScreenConstant.SCREEN_HEIGHT/3f,ScreenConstant.SCREEN_HEIGHT/3f);
        ObjectAnimator visToInvis = ObjectAnimator.ofFloat(this,
                "rotationY", -40f, 40f);
        ObjectAnimator invisToInvis = ObjectAnimator.ofFloat(this,
                "rotationX", -50f, 50f);
        translation.setDuration(6000);
        translation.setRepeatMode(ObjectAnimator.REVERSE);
        translation.setRepeatCount(android.animation.ValueAnimator.INFINITE);

        translationY.setDuration(4500);
        translationY.setRepeatMode(ObjectAnimator.REVERSE);
        translationY.setRepeatCount(android.animation.ValueAnimator.INFINITE);

        visToInvis.setRepeatMode(ObjectAnimator.REVERSE);
        visToInvis.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        this.setScaleX(0.8f);
        invisToInvis.setRepeatMode(ObjectAnimator.REVERSE);
        invisToInvis.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        invisToInvis.setDuration(7000);
        visToInvis.setDuration(5000);
        invisToInvis.setInterpolator(accelerator);
        visToInvis.setRepeatCount(android.animation.ValueAnimator.INFINITE);

        AnimatorSet set =new AnimatorSet();
        set.playTogether(invisToInvis, visToInvis,translation,translationY);
        set.start();
    }
}
