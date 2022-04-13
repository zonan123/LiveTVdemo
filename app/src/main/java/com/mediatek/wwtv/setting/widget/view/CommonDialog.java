package com.mediatek.wwtv.setting.widget.view;

import android.app.Activity;
import android.app.Dialog;

import android.content.Context;
import android.util.Log;
import android.view.View;
import com.mediatek.wwtv.rxbus.ActivityDestroyEvent;
import com.mediatek.wwtv.rxbus.RxBus;
import com.mediatek.wwtv.tvcenter.R;
import io.reactivex.rxjava3.disposables.Disposable;

public class CommonDialog extends Dialog implements View.OnClickListener {

    protected Context mContext;
    private Disposable mDisposable;

    protected CommonDialog(Context context, int layoutID) {
        super(context, R.style.MTK_Dialog_bg);
        this.mContext = context;

        setContentView(layoutID);
        initView();
    }

    public void initView() {
        Log.d("CommonDialog", "initView");
    }

    @Override
    public void onClick(View v) {
        Log.d("CommonDialog", "onClick");
    }

    @Override public boolean isShowing() {
        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            if (activity.isDestroyed()) {
                Log.i("CommonDialog", "isShowing isDestroyed " + activity);
                return false;
            }
        }
        boolean ret = super.isShowing();
        Log.v("CommonDialog", "isShowing ret=" + ret);
        return ret;
    }

    @Override protected void onStart() {
        super.onStart();
        mDisposable = RxBus.instance.onEvent(ActivityDestroyEvent.class)
            .filter(it -> {
                if (it.activityClass == mContext.getClass()) {
                    if (super.isShowing()) {
                        dismiss();
                    }
                    return true;
                }
                return false;
            })
            .firstElement().subscribe();
    }

    @Override protected void onStop() {
        super.onStop();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }
}
