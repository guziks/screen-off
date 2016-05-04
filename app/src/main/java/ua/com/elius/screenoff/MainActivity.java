package ua.com.elius.screenoff;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "MainActivity";

    private static final int MINIMAL_TIMEOUT = 0;
    private static final long BLACK_SCREEN_ANIMATION_DURATION = 1000;

    private TextView mTimeoutTextView;
    private int mTimeout;
    private boolean mAlteredTimeout;
    private boolean mCanWrite = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideUI();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            requestPermissionWriteSettings();
        }
    }

    private void hideUI() {
        View decorView = getWindow().getDecorView();
        int uiOptions = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionWriteSettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Can not show permission request dialog", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPermissions();
        if (mCanWrite) {
            minimizeTimeout();
            blackScreen();
        }
    }

    private void blackScreen() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            int colorFrom = Color.TRANSPARENT;
            int colorTo = Color.BLACK;
            ValueAnimator colorAnimation;
            colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(BLACK_SCREEN_ANIMATION_DURATION);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                public void onAnimationUpdate(ValueAnimator animator) {
                    getWindow().getDecorView().setBackgroundColor((int) animator.getAnimatedValue());
                }

            });
            colorAnimation.start();
        } else {
            getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAlteredTimeout) {
            restoreTimeout();
        }
        finish();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            mCanWrite = false;
        } else {
            mCanWrite = true;
        }
    }

    public void saveTimeout() {
        mTimeout = Settings.System.getInt(this.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, 0);

        Log.i(TAG, "Timeout saved: " + mTimeout);
    }

    public void minimizeTimeout() {
        saveTimeout();
        mAlteredTimeout = true;
        Settings.System.putInt(this.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, MINIMAL_TIMEOUT);

        Log.i(TAG, "Timeout minimized");
    }

    public void restoreTimeout() {
        mAlteredTimeout = false;
        Settings.System.putInt(this.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, mTimeout);

        Log.i(TAG, "Timeout restored: " + mTimeout);
    }
}
