package com.github.blueboytm.flutter_v2ray.v2ray.services;

import java.util.ArrayList;

public interface VpnStatusListener {
    void onVpnStatusRequest(ArrayList<String> list);
}