package android.src.main.java.com.github.blueboytm.flutter_v2ray.v2ray.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StopServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        V2rayCoreManager.getInstance().stopCore();
        Log.d("StopServiceReceiver", "stopCore called");
    }
}
