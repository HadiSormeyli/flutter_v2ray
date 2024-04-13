package com.github.blueboytm.flutter_v2ray.v2ray.services;



import android.app.Service;
import android.content.Intent;
import android.net.LocalSocket;
import android.os.IBinder;
import android.net.LocalSocketAddress;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.github.blueboytm.flutter_v2ray.v2ray.V2rayController;
import com.github.blueboytm.flutter_v2ray.v2ray.utils.MessageUtil;
import android.util.Pair;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class V2RayTestService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int key = intent.getIntExtra("key", 1);
            switch (key) {
                case 1:
                    String config = intent.getStringExtra("config");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            android.util.Log.d("MainViewModel", "onReceive: ping " + result);
                            Map<String, Long> myMap = new HashMap<String,Long>();
                            myMap.put(config, result);
                            MessageUtil.sendMsg2UI(V2RayTestService.this, 2,  (Serializable) myMap);
                        }
                    }).start();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}