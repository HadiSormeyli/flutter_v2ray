package com.github.blueboytm.flutter_v2ray.v2ray.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.Map;
import com.github.blueboytm.flutter_v2ray.v2ray.services.VpnAllRealPingListener;


public class VpnAllRealPingBroadcastReceiver extends BroadcastReceiver {
    private VpnAllRealPingListener callback;

    public void setListener(VpnAllRealPingListener callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null && intent.getAction() != null && intent.getAction().equals("action.VPN_ALL_REAL_PING")) {
            Map<String, Long> info = (Map<String, Long>) intent.getSerializableExtra("VPN_ALL_REAL_PING");
            android.util.Log.d("MainViewModel", "onReceive: best4 " + info);

            if(callback != null) {
                callback.onVpnAllRealPingRequest(info);
            }
        }
    }
}
