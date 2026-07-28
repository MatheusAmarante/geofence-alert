package com.mateus.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import java.util.ArrayList;
import java.util.List;

public class GeofenceManager {
    private final Context context;
    private final GeofencingClient client;
    private final DatabaseHelper db;

    public GeofenceManager(Context context) {
        this.context = context;
        this.client = LocationServices.getGeofencingClient(context);
        this.db = new DatabaseHelper(context);
    }

    public void registerAllGeofences() {
        List<GeofenceLocation> locations = db.getEnabledLocations();
        if (locations.isEmpty()) return;

        List<Geofence> geofences = new ArrayList<>();
        for (GeofenceLocation loc : locations) {
            geofences.add(new Geofence.Builder()
                    .setRequestId(String.valueOf(loc.id))
                    .setCircularRegion(loc.latitude, loc.longitude, loc.radius)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());
        }

        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofences)
                .build();

        PendingIntent pendingIntent = getPendingIntent();

        client.addGeofences(request, pendingIntent)
                .addOnSuccessListener(aVoid -> {
                    // Geofences registrados com sucesso
                })
                .addOnFailureListener(e -> {
                    // Falha ao registrar
                });
    }

    public void removeAllGeofences() {
        client.removeGeofences(getPendingIntent());
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
