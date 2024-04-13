package android.src.main.java.com.github.blueboytm.flutter_v2ray.v2ray.services;

import java.util.Map;

public interface VpnAllRealPingListener {
    void onVpnAllRealPingRequest(Map<String, Long> ping);
}