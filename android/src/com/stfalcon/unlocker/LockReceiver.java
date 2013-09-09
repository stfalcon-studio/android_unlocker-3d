package com.stfalcon.unlocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by anton on 8/2/13.
 */
public class LockReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //Считываем значение состояния анлокера on/off
        StringBuilder sb = new StringBuilder();

        try {
            FileInputStream is = context.openFileInput("active");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            is.close();
        } catch (OutOfMemoryError om) {
            om.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String result = sb.toString().trim();

        // Есле екран выключили, запускаем локскрин
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            if (result.equals("true")) {
                Intent intent1 = new Intent(context, UnlockScreen.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            }

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            if (result.equals("true")) {
            }
        }

        //Есле телефон перезапустили, запускаем приложение
        if (("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) && result.equals("true")) {
            Intent intent1 = new Intent(context, MainActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }

    }
}
