package com.mateus.geofence;

public class GeofenceLocation {
    public int id;
    public String name;
    public double latitude;
    public double longitude;
    public int radius;
    public String alarmMode; // "sound", "vibrate", "both", "silent"
    public boolean enabled;

    public String getAlarmModeLabel() {
        switch (alarmMode) {
            case "sound": return "🔊 Som";
            case "vibrate": return "📳 Vibração";
            case "both": return "🔊📳 Ambos";
            case "silent": return "🔕 Silencioso";
            default: return alarmMode;
        }
    }
}
