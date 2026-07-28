package com.mateus.geofence;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "geofence_alert";

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event == null || event.hasError()) return;

        int transition = event.getGeofenceTransition();
        if (transition != Geofence.GEOFENCE_TRANSITION_ENTER) return;

        List<Geofence> triggeringGeofences = event.getTriggeringGeofences();
        if (triggeringGeofences == null || triggeringGeofences.isEmpty()) return;

        DatabaseHelper db = new DatabaseHelper(context);

        for (Geofence geofence : triggeringGeofences) {
            int locationId = Integer.parseInt(geofence.getRequestId());
            List<GeofenceLocation> all = db.getAllLocations();

            for (GeofenceLocation loc : all) {
                if (loc.id == locationId && loc.enabled) {
                    triggerAlarm(context, loc);
                    break;
                }
            }
        }
    }

    private void triggerAlarm(Context context, GeofenceLocation location) {
        createNotificationChannel(context);

        String title = "📍 Você chegou!";
        String message = location.name;

        // Vibração
        if (shouldVibrate(location.alarmMode)) {
            vibrate(context);
        }

        // Som
        Uri soundUri = shouldPlaySound(location.alarmMode)
                ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                : null;

        // Intent para abrir o app
        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, location.id, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(soundUri)
                .setVibrate(shouldVibrate(location.alarmMode)
                        ? new long[]{0, 500, 200, 500, 200, 500}
                        : null)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .build();

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.notify(location.id, notification);
    }

    private boolean shouldVibrate(String mode) {
        return "vibrate".equals(mode) || "both".equals(mode);
    }

    private boolean shouldPlaySound(String mode) {
        return "sound".equals(mode) || "both".equals(mode);
    }

    private void vibrate(Context context) {
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = context.getSystemService(VibratorManager.class);
            vibrator = vm.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 500, 200, 500, 200, 500};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                vibrator.vibrate(pattern, -1);
            }
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alertas de Localização",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificações quando você chega em um local");
            channel.enableVibration(true);

            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), attrs);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
