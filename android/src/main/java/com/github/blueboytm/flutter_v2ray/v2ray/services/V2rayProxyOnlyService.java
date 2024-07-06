package com.github.blueboytm.flutter_v2ray.v2ray.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.github.blueboytm.flutter_v2ray.v2ray.core.V2rayCoreManager;
import com.github.blueboytm.flutter_v2ray.v2ray.interfaces.V2rayServicesListener;
import com.github.blueboytm.flutter_v2ray.v2ray.utils.AppConfigs;
import com.github.blueboytm.flutter_v2ray.v2ray.utils.V2rayConfig;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.graphics.Color;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.core.app.NotificationCompat;

import android.os.Handler;
import android.os.Looper;
import com.github.blueboytm.flutter_v2ray.v2ray.utils.Utilities;

public class V2rayProxyOnlyService extends Service implements V2rayServicesListener {

    private NotificationManager mNotificationManager = null;
    private NotificationCompat.Builder mBuilder;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateNotificationRunnable;

    private V2rayConfig v2rayConfig;

    @Override
    public void onCreate() {
        super.onCreate();
        V2rayCoreManager.getInstance().setUpListener(this);
    }



    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            try {
                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            } catch (Exception e) {
                return null;
            }
        }
        return mNotificationManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannelID(final String Application_name) {
        String notification_channel_id = "DEV7_DEV_V_E_CH_ID";
        NotificationChannel notificationChannel = new NotificationChannel(
                notification_channel_id, Application_name + " Background Service", NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.setImportance(NotificationManager.IMPORTANCE_LOW);
        notificationChannel.setSound(null, null);
        Objects.requireNonNull(getNotificationManager()).createNotificationChannel(notificationChannel);
        return notification_channel_id;
    }

    private int judgeForNotificationFlag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        } else {
            return PendingIntent.FLAG_UPDATE_CURRENT;
        }
    }

    private void showNotification() {
        Intent launchIntent = getPackageManager().
                getLaunchIntentForPackage(getApplicationInfo().packageName);
        launchIntent.setAction("FROM_DISCONNECT_BTN");
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent notificationContentPendingIntent = PendingIntent.getActivity(
                this, 0, launchIntent, judgeForNotificationFlag());

        Intent stopIntent = new Intent(this, V2rayVPNService.class);
        stopIntent.putExtra("COMMAND", AppConfigs.V2RAY_SERVICE_COMMANDS.STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 1, stopIntent, judgeForNotificationFlag());

        String notificationChannelID = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannelID = createNotificationChannelID(v2rayConfig.APPLICATION_NAME);
        }

        mBuilder =
                new NotificationCompat.Builder(this, notificationChannelID);
        mBuilder.setSmallIcon(v2rayConfig.APPLICATION_ICON)
                .setContentTitle(v2rayConfig.REMARK)
                .setContentIntent(notificationContentPendingIntent)
                .addAction(-1, "Stop", stopPendingIntent);
        startForeground(1, mBuilder.build());
        startUpdatingNotification();
    }

    private String getNotificationContentText() {
        return Utilities.parseTraffic(V2rayCoreManager.getInstance().uploadSpeed, false, true) + "↑ " +
                Utilities.parseTraffic(V2rayCoreManager.getInstance().downloadSpeed, false, true) + "↓";
    }

    private void updateNotification() {
        if(mBuilder != null) {
            mBuilder.setContentText(getNotificationContentText());
            getNotificationManager().notify(1, mBuilder.build());
        }
    }

    private void startUpdatingNotification() {
        updateNotificationRunnable = new Runnable() {
            @Override
            public void run() {
                updateNotification();
                handler.postDelayed(this, 500);
            }
        };
        handler.post(updateNotificationRunnable);
    }

    private void stopUpdatingNotification() {
        handler.removeCallbacks(updateNotificationRunnable);
    }





    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppConfigs.V2RAY_SERVICE_COMMANDS startCommand = (AppConfigs.V2RAY_SERVICE_COMMANDS) intent.getSerializableExtra("COMMAND");
        if (startCommand.equals(AppConfigs.V2RAY_SERVICE_COMMANDS.START_SERVICE)) {
            v2rayConfig = (V2rayConfig) intent.getSerializableExtra("V2RAY_CONFIG");
            if (v2rayConfig == null) {
                this.onDestroy();
            }
            if (V2rayCoreManager.getInstance().isV2rayCoreRunning()) {
                V2rayCoreManager.getInstance().stopCore();
            }
            assert v2rayConfig != null;
            if (V2rayCoreManager.getInstance().startCore(v2rayConfig)) {
                showNotification();
                Log.e(V2rayProxyOnlyService.class.getSimpleName(), "onStartCommand success => v2ray core started.");
            } else {
                this.onDestroy();
            }
        } else if (startCommand.equals(AppConfigs.V2RAY_SERVICE_COMMANDS.STOP_SERVICE)) {
            V2rayCoreManager.getInstance().stopCore();
        } else if (startCommand.equals(AppConfigs.V2RAY_SERVICE_COMMANDS.MEASURE_DELAY)) {
            new Thread(() -> {
                Intent sendB = new Intent("CONNECTED_V2RAY_SERVER_DELAY");
                sendB.putExtra("DELAY", String.valueOf(V2rayCoreManager.getInstance().getConnectedV2rayServerDelay()));
                sendBroadcast(sendB);
            }, "MEASURE_CONNECTED_V2RAY_SERVER_DELAY").start();
        } else {
            this.onDestroy();
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        stopUpdatingNotification();
        stopForeground(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onProtect(int socket) {
        return true;
    }

    @Override
    public Service getService() {
        return this;
    }

    @Override
    public void startService() {
        //ignore
    }

    @Override
    public void stopService() {
        try {
            stopSelf();
        } catch (Exception e) {
            //ignore
        }
    }
}
