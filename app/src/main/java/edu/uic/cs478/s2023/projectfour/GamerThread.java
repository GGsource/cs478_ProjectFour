package edu.uic.cs478.s2023.projectfour;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class GamerThread extends Thread{
    private static final String TAG = "GamerThread";

    public Handler handler;

    public final int THREAD_TEAM;

    GamerThread(int TEAM) {
        THREAD_TEAM = TEAM;
    }

    @Override
    public void run() {
        Looper.prepare();

        handler = new Handler();
        Log.d(TAG, "run: Thread Started: " + this.getName());

        Looper.loop();
    }
}
