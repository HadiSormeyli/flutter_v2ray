package com.github.blueboytm.flutter_v2ray;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.Context;
import android.app.Activity;
import android.net.VpnService;
import android.util.Log;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;
import java.util.List;

import com.github.blueboytm.flutter_v2ray.v2ray.V2rayController;
import com.github.blueboytm.flutter_v2ray.v2ray.utils.AppConfigs;
import com.github.blueboytm.flutter_v2ray.v2ray.services.VpnAllRealPingBroadcastReceiver;
import com.github.blueboytm.flutter_v2ray.v2ray.services.VpnAllRealPingListener;

import com.github.blueboytm.flutter_v2ray.v2ray.utils.MessageUtil;
import com.github.blueboytm.flutter_v2ray.v2ray.services.V2RayTestService;
import com.google.gson.Gson;
import android.os.Build;


public class FlutterV2rayPlugin implements FlutterPlugin, ActivityAware {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private MethodChannel vpnControlMethod;
    private EventChannel vpnStatusEvent;
    private EventChannel vpnPingEvent;
    private EventChannel.EventSink vpnStatusSink;
    private Activity activity;
    private final BroadcastReceiver mMsgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            if (intent != null) {
                switch (intent.getIntExtra("key", 0)) {
                    case 2:
                        Map<String, Long> result = (HashMap<String, Long>) intent.getSerializableExtra("content");

                        Intent intent2 = new Intent();
                        intent2.setAction("action.VPN_ALL_REAL_PING");
                        intent2.putExtra("VPN_ALL_REAL_PING", (Serializable) result);

                        activity.sendBroadcast(intent2);
                        break;

                    default:
                        break;
                }
            }
        }
    };
    private BroadcastReceiver v2rayBroadCastReceiver;
    private VpnAllRealPingBroadcastReceiver vpnAllRealPingReceiver;


    public void testAllRealPing(List<String> configs) {
        MessageUtil.sendMsg2TestService(activity, 3, "");

        for (String config : configs) {
            MessageUtil.sendMsg2TestService(
                    activity,
                    1,
                    config
            );
        }
    }

    @SuppressLint("DiscouragedApi")
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        vpnControlMethod = new MethodChannel(binding.getBinaryMessenger(), "flutter_v2ray");
        vpnStatusEvent = new EventChannel(binding.getBinaryMessenger(), "flutter_v2ray/status");
        vpnPingEvent = new EventChannel(
                binding.getBinaryMessenger(),
                "flutter_v2ray/all_real_ping"
        );

        vpnPingEvent.setStreamHandler(
                new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object arguments, EventChannel.EventSink eventSink) {
                        vpnAllRealPingReceiver = new VpnAllRealPingBroadcastReceiver();
                        vpnAllRealPingReceiver.setListener(new VpnAllRealPingListener() {
                            @Override
                            public void onVpnAllRealPingRequest(Map<String, Long> ping) {
                                eventSink.success(new Gson().toJson(ping));
                            }
                        });

                        IntentFilter filter = new IntentFilter("action.VPN_ALL_REAL_PING");
                        activity.registerReceiver(vpnAllRealPingReceiver, filter, null, null, Context.RECEIVER_NOT_EXPORTED);
                    }

                    @Override
                    public void onCancel(Object arguments) {
                        // Implementation for onCancel
                        if (vpnAllRealPingReceiver != null) {
                            activity.unregisterReceiver(vpnAllRealPingReceiver);
                            vpnAllRealPingReceiver = null;
                        }
                    }
                }
        );

        vpnStatusEvent.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object arguments, EventChannel.EventSink events) {
                vpnStatusSink = events;
            }

            @Override
            public void onCancel(Object arguments) {
                if (vpnStatusSink != null) vpnStatusSink.endOfStream();
            }
        });
        vpnControlMethod.setMethodCallHandler((call, result) -> {
            switch (call.method) {
                case "startV2Ray":
                    if (Boolean.TRUE.equals(call.argument("proxy_only"))) {
                        V2rayController.changeConnectionMode(AppConfigs.V2RAY_CONNECTION_MODES.PROXY_ONLY);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 50);
                        }
                    }
                    V2rayController.StartV2ray(binding.getApplicationContext(), call.argument("remark"), call.argument("config"), call.argument("blocked_apps"), call.argument("bypass_subnets"));
                    result.success(null);
                    break;
                case "stopV2Ray":
                    V2rayController.StopV2ray(binding.getApplicationContext());
                    result.success(null);
                    break;
                case "initializeV2Ray":
                    V2rayController.init(binding.getApplicationContext(), binding.getApplicationContext().getResources().getIdentifier("ic_launcher", "mipmap", binding.getApplicationContext().getPackageName()), "Flutter V2ray");
                    result.success(null);
                    break;
                case "getServerDelay":
                    executor.submit(() -> {
                        try {
                            result.success(V2rayController.getV2rayServerDelay(call.argument("config")));
                        } catch (Exception e) {
                            result.success(-1);
                        }
                    });
                    break;

                case "getAllServerDelay":
                    String res = call.argument("configs");
                    List<String> configs = new Gson().fromJson(res, List.class);
                    testAllRealPing(configs);
                    break;
                case "getConnectedServerDelay":
                    executor.submit(() -> {
                        try {
                            result.success(V2rayController.getConnectedV2rayServerDelay(binding.getApplicationContext()));
                        } catch (Exception e) {
                            result.success(-1);
                        }
                    });
                    break;
                case "getCoreVersion":
                    result.success(V2rayController.getCoreVersion());
                    break;
                case "requestPermission":
                    final Intent request = VpnService.prepare(activity);
                    if (request != null) {
                        activity.startActivityForResult(request, 24);
                        result.success(false);
                        break;
                    }
                    result.success(true);
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        if (v2rayBroadCastReceiver != null) {
            vpnControlMethod.setMethodCallHandler(null);
            vpnStatusEvent.setStreamHandler(null);
            vpnPingEvent.setStreamHandler(null);
            activity.unregisterReceiver(v2rayBroadCastReceiver);
            activity.unregisterReceiver(mMsgReceiver);
            if (vpnAllRealPingReceiver != null) {
                activity.unregisterReceiver(vpnAllRealPingReceiver);
                vpnAllRealPingReceiver = null;
            }
            executor.shutdown();
        }
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        Log.d("TAG", "onReceive: ");

        v2rayBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
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
                }
            }
        };
        IntentFilter filter  = new IntentFilter("V2RAY_CONNECTION_INFO");
        activity.registerReceiver(v2rayBroadCastReceiver, filter, null, null, Context.RECEIVER_NOT_EXPORTED);
        activity.registerReceiver(mMsgReceiver, new IntentFilter("com.v2ray.action.activity"));

    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        v2rayBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(intent.getExtras().getString("DURATION"));
                    list.add(intent.getExtras().getString("UPLOAD_SPEED"));
                    list.add(intent.getExtras().getString("DOWNLOAD_SPEED"));
                    list.add(intent.getExtras().getString("UPLOAD_TRAFFIC"));
                    list.add(intent.getExtras().getString("DOWNLOAD_TRAFFIC"));
                    list.add(intent.getExtras().getSerializable("STATE").toString().substring(6));
                    vpnStatusSink.success(list);
                } catch (Exception ignored) {
                }
            }
        };
        activity.registerReceiver(v2rayBroadCastReceiver, new IntentFilter("V2RAY_CONNECTION_INFO"), null, null, Context.RECEIVER_NOT_EXPORTED);
        activity.registerReceiver(mMsgReceiver, new IntentFilter("com.v2ray.action.activity"));
    }

    @Override
    public void onDetachedFromActivity() {
    }
}
