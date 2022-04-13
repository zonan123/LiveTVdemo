package com.android.tv.menu.customization;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.mediatek.wwtv.tvcenter.R;
import android.transition.Scene;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;

public class CustomizeChanelListActivity extends Activity{

    private static final String TAG = "CustomizeChanelListActivity";
    private String SETTINGS_FRAGMENT_TAG = "CustomizeChanelListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   //   SetupAnimationHelper.initialize(this);
      setContentView(R.layout.activity_customizechannellist);
    
//      getFragmentManager().beginTransaction().replace(R.id.container,ChanelListFragment).commit();
      
      if (savedInstanceState == null) {

          final ViewGroup root = (ViewGroup) findViewById(R.id.container);
          root.getViewTreeObserver().addOnPreDrawListener(
                  new ViewTreeObserver.OnPreDrawListener() {
                      @Override
                      public boolean onPreDraw() {
                          root.getViewTreeObserver().removeOnPreDrawListener(this);
                          final Scene scene = new Scene(root);
                          scene.setEnterAction(new Runnable() {

                            @Override
                              public void run() {
                                CustomizeChanelListFragment chanelListFragment = new CustomizeChanelListFragment();
                                  if (getFragmentManager().isStateSaved()
                                          || getFragmentManager().isDestroyed()) {
                                      Log.d(TAG, "Got torn down before adding fragment");
                                      return;
                                  }
                                  if (chanelListFragment != null) {
                                      getFragmentManager().beginTransaction()
                                              .add(R.id.container, chanelListFragment,
                                                      SETTINGS_FRAGMENT_TAG)
                                              .commitNow();
                                  }
                              }
                          });

                          final Slide slide = new Slide(Gravity.END);
                          slide.setSlideFraction(
                                  getResources().getDimension(R.dimen.lb_settings_pane_width)
                                          / root.getWidth());
                          TransitionManager.go(scene, slide);

                          // Skip the current draw, there's nothing in it
                          return false;
                      }
                  });
      }
    }
    
    @Override
    public void finish() {
        final Fragment fragment = getFragmentManager().findFragmentByTag(SETTINGS_FRAGMENT_TAG);
        if (isResumed() && fragment != null) {
            final ViewGroup root = (ViewGroup) findViewById(R.id.container);
            final Scene scene = new Scene(root);
            scene.setEnterAction(new Runnable() {

                @Override
                  public void run() {
                    getFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commitNow();
                }
              });
            final Slide slide = new Slide(Gravity.END);
            slide.setSlideFraction(
                    getResources().getDimension(R.dimen.lb_settings_pane_width) / root.getWidth());
            slide.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    getWindow().setDimAmount(0);
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    transition.removeListener(this);
                    CustomizeChanelListActivity.super.finish();
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                	Log.d(TAG, "onTransitionCancel");
                }

                @Override
                public void onTransitionPause(Transition transition) {
                	Log.d(TAG, "onTransitionPause");
                }

                @Override
                public void onTransitionResume(Transition transition) {
                	Log.d(TAG, "onTransitionResume");
                }
            });
            TransitionManager.go(scene, slide);
        } else {
            super.finish();
        }
    }

    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }


}
