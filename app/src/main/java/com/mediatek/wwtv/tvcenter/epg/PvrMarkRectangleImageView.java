package com.mediatek.wwtv.tvcenter.epg;

import android.content.Context;
import android.graphics.Canvas;
//import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.mediatek.wwtv.tvcenter.R;


public class PvrMarkRectangleImageView extends ImageView {

  private static final String TAG = "PvrMarkRectangleImageView";
  private Context mContext;

  public PvrMarkRectangleImageView(Context context) {
    super(context);
    // TODO Auto-generated constructor stub
    mContext = context;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG," 1 param");
    initPaint();
  }

  public PvrMarkRectangleImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG," 2 params");
    initPaint();
  }

  public PvrMarkRectangleImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mContext = context;
    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG," 3 params");
    initPaint();
  }
  
  Paint paint = new Paint();
  private float left;
  private float top;
  private float right;
  private float bottom;
  private int leftMargin;
  private int mRectWidth;

  private void initPaint() {
    paint.setAntiAlias(true);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(5.0f);
    paint.setAlpha(100);
  }
  
  public void set2Points( int left, int top, int right, int bottom){
    this.left = (float)left;
    this.top = (float)top;
    this.right = (float)right;
    this.bottom = (float)bottom;
  }

  
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    System.err.println("Guanglei left: "+left);
    System.err.println("Guanglei top: "+top);
    System.err.println("Guanglei right: "+right);
    System.err.println("Guanglei bottom: "+bottom);

    canvas.drawRect(left, top, right, bottom, paint);
    
  }
  
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

    public int getLeftMargin() {
        return leftMargin < 0 ? 0 : leftMargin;
    }

    public void setLeftMargin(int margin) {
        leftMargin = (margin < 0 ? 0 : margin);
    }

    public int getRectWidth() {
        return mRectWidth < 0 ? 0 : mRectWidth ;
    }

    public void setRectWidth(int width) {
        mRectWidth = (width < 0 ? 0 : width);
    }

    public void setMarkColor(int type) {
        System.err.println("Guanglei type: "+type);
        if(type == 1){//reminder
            paint.setColor(mContext.getResources().getColor(R.color.epg_pvr_reminder));
        } else {//record
            paint.setColor(mContext.getResources().getColor(R.color.epg_pvr_record));
        }
    }
}














