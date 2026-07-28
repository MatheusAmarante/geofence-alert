package com.mateus.geofence;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class GeofenceService extends Service {
    private static final String CHANNEL_ID = "geofence_service";
    private static final int NOTIFICATION_ID = 1;
    private GeofenceManager geofenceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        geofenceManager = new GeofenceManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("📍 Alerta Local")
                .setContentText("Monitorando suas localizações...")
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
        geofenceManager.registerAllGeofences();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        geofenceManager.removeAllGeofences();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Monitoramento de Localização",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Notificação persistente do serviço de geofence");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
