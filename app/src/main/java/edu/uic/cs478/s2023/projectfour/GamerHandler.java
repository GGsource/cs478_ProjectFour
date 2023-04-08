package edu.uic.cs478.s2023.projectfour;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class GamerHandler extends Handler {
    private static final String TAG = "GamerHandler";

    public static final int RED_JUST_PLAYED = 0;
    public static final int BLUE_JUST_PLAYED = 1;

    Handler mainHandler = new Handler(Looper.getMainLooper());
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case RED_JUST_PLAYED:
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
                break;
            case BLUE_JUST_PLAYED:
                break;
        }
    }
}
