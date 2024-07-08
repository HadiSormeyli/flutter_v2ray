package android.src.main.java.com.github.blueboytm.flutter_v2ray.v2ray.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import io.flutter.plugin.common.EventChannel;

import java.util.Map;

import com.github.blueboytm.flutter_v2ray.v2ray.services.VpnAllRealPingListener;
import java.util.ArrayList;


public class V2RayConnectionInfoReceiver extends BroadcastReceiver {
    private EventChannel.EventSink vpnStatusSink;

    public void setListener(EventChannel.EventSink vpnStatusSink) {
        this.vpnStatusSink = vpnStatusSink;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TAG", "onReceive: get broad");
        if (intent != null && intent.getAction() != null && intent.getAction().equals("V2RAY_CONNECTION_INFO")) {
            try {
                ArrayList<String> list = new ArrayList<>();
                list.add(intent.getExtras().getString("DURATION"));
                list.add(intent.getExtras().getString("UPLOAD_SPEED"));
                list.add(intent.getExtras().getString("DOWNLOAD_SPEED"));
                list.add(intent.getExtras().getString("UPLOAD_TRAFFIC"));
                list.add(intent.getExtras().getString("DOWNLOAD_TRAFFIC"));
                list.add(intent.getExtras().getSerializable("STATE").toString().substring(6));
                Log.d("TAG", "onReceive: get broad cast");
                vpnStatusSink.success(list);
            } catch (Exception ignored) {
                Log.d("TAG", "error");
            }
        }
    }
}
