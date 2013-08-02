package com.stfalcon.unlocker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

/**
 * Created by user on 8/2/13.
 */
public class UnlockScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);

        Button unlock = (Button)findViewById(R.id.button_unlock);


    }
}
