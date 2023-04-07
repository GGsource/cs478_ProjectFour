package edu.uic.cs478.s2023.projectfour;


import android.os.Handler;
import android.os.Looper;

public class GamerThread extends Thread{
    private static final String TAG = "GamerThread";

    public Handler handler;

    @Override
    public void run() {
        Looper.prepare();

        handler = new Handler();

        Looper.loop();
    }
}
