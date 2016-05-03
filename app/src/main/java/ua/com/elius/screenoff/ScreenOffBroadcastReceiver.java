package ua.com.elius.screenoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenOffBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "ScreenOffReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
    }

}
