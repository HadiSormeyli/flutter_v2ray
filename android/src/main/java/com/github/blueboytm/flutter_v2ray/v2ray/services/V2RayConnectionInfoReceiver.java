package com.github.blueboytm.flutter_v2ray.v2ray.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.blueboytm.flutter_v2ray.v2ray.utils.AppConfigs;

import android.util.Log;

import io.flutter.plugin.common.EventChannel;

import java.util.Map;

import com.github.blueboytm.flutter_v2ray.v2ray.services.VpnStatusListener;

import java.util.ArrayList;


public class V2RayConnectionInfoReceiver extends BroadcastReceiver {

    private VpnStatusListener callback;


    public V2RayConnectionInfoReceiver() {
        Log.d("TAG", "onReceive: init");
    }

    public void setListener(VpnStatusListener callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null && intent.getAction().equals("action.V2RAY_CONNECTION_INFO")) {
            try {
                ArrayList<String> list = new ArrayList<>();
                AppConfigs.V2RAY_STATE = (AppConfigs.V2RAY_STATES) intent.getExtras().getSerializable("STATE");
                list.add(intent.getExtras().getString("DURATION"));
                list.add(intent.getExtras().getString("UPLOAD_SPEED"));
                list.add(intent.getExtras().getString("DOWNLOAD_SPEED"));
                list.add(intent.getExtras().getString("UPLOAD_TRAFFIC"));
                list.add(intent.getExtras().getString("DOWNLOAD_TRAFFIC"));
                list.add(intent.getExtras().getSerializable("STATE").toString().substring(6));
                Log.d("TAG", "onReceive: get broad cast");
                if(callback != null) {
                    callback.onVpnStatusRequest(list);
                }
            } catch (Exception ignored) {
                Log.d("TAG", "errorooooooooooooooooooooooooooo " + ignored);
            }
        }
    }
}
