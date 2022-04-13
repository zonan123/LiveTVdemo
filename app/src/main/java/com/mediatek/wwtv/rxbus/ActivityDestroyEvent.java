package com.mediatek.wwtv.rxbus;

import android.app.Activity;

public class ActivityDestroyEvent {
    public final Class<? extends Activity> activityClass;

    public ActivityDestroyEvent(Class<? extends Activity> activityClass){
        this.activityClass = activityClass;
    }
}
