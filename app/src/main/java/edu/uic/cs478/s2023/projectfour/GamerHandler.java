package edu.uic.cs478.s2023.projectfour;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class GamerHandler extends Handler {
    private static final String TAG = "GamerHandler";
    public static final int MESSAGE = 0;
    public static final int RUNNABLE = 1;
    Handler mainHandler = new Handler(Looper.getMainLooper());
    Context ctx;
    GamerHandler(Context givenCtx) {
        ctx = givenCtx;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE:
                Bundle bundle = msg.getData();
                String toastMsg = bundle.getString("MSG");
//                        Log.d(TAG, "run: About to make a toast with " + toastMsg);
                Toast.makeText(ctx, toastMsg, Toast.LENGTH_LONG).show();
//                        Log.d(TAG, "Congratulated player on win.");
                break;
        }
    }
}
