package com.stfalcon.unlocker;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 8/23/13
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        setContentView(R.layout.activity_main);

        /*
        //for unlock
        TextView tvGesture = (TextView) findViewById(R.id.tv_unlock_gesture_isnt_coor);
        Typeface robotoThin = Typeface.createFromAsset(this.getAssets(), "Roboto-Thin.ttf");
        tvGesture.setTypeface(robotoThin);
        DigitalClock dClock = (DigitalClock) findViewById(R.id.clock);
        dClock.setTypeface(robotoThin);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.peekFastDrawable();
        View rl_unlock_screen = findViewById(R.id.rl_unlock_screen);
        rl_unlock_screen.setBackground(wallpaperDrawable);*/


        /*//for main
        Typeface robotoThin = Typeface.createFromAsset(this.getAssets(), "Roboto-Thin.ttf");
        TextView tvNewGesture = (TextView) findViewById(R.id.tv_main_new_gesture);
        tvNewGesture.setTypeface(robotoThin);*/



    }
}
