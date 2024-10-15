package com.github.blueboytm.flutter_v2ray.v2ray.utils;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import java.io.Serializable;
import android.os.Bundle;
import com.github.blueboytm.flutter_v2ray.v2ray.services.V2RayTestService;


public class MessageUtil {

    public static void sendMsg2UI(Context ctx, int what, Serializable content) {
        sendMsg(ctx, "com.v2ray.action.activity", what, content);
    }

    public static void sendMsg2TestService(Context ctx, int what, String config) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(ctx, V2RayTestService.class));
            intent.putExtra("key", what);
            intent.putExtra("config", config);
            ctx.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMsg(Context ctx, String action, int what, Serializable content) {
        try {
            Intent intent = new Intent();
            intent.setAction(action);
//            intent.setPackage("com.github.blueboytm.flutter_v2ray");
            intent.putExtra("key", what);
            android.util.Log.d("Plugin", "sendMsg: " + what);


            Bundle bundle = new Bundle();
            bundle.putSerializable("content", content);
            intent.putExtras(bundle);

            ctx.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
