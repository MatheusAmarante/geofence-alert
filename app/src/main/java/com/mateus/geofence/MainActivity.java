package com.mateus.geofence;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;

    private DatabaseHelper db;
    private GeofenceManager geofenceManager;
    private RecyclerView recyclerView;
    private LocationAdapter adapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        geofenceManager = new GeofenceManager(this);

        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        FloatingActionButton fab = findViewById(R.id.fab);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(v -> showAddDialog(null));

        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLocations();
    }

    private void loadLocations() {
        List<GeofenceLocation> locations = db.getAllLocations();
        adapter = new LocationAdapter(locations, this);
        recyclerView.setAdapter(adapter);

        if (locations.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void checkPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

        boolean allGranted = true;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        } else {
            startGeofenceService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startGeofenceService();
            } else {
                Toast.makeText(this, "Permissões de localização são necessárias!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startGeofenceService() {
        Intent serviceIntent = new Intent(this, GeofenceService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void showAddDialog(GeofenceLocation editLocation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_location, null);
        builder.setView(dialogView);

        EditText nameInput = dialogView.findViewById(R.id.nameInput);
        EditText latInput = dialogView.findViewById(R.id.latInput);
        EditText lngInput = dialogView.findViewById(R.id.lngInput);
        EditText radiusInput = dialogView.findViewById(R.id.radiusInput);
        Spinner alarmSpinner = dialogView.findViewById(R.id.alarmSpinner);
        Button pickLocationBtn = dialogView.findViewById(R.id.pickLocationBtn);

        String[] alarmModes = {"🔊📳 Ambos", "🔊 Som", "📳 Vibração", "🔕 Silencioso"};
        String[] alarmValues = {"both", "sound", "vibrate", "silent"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, alarmModes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alarmSpinner.setAdapter(spinnerAdapter);

        boolean isEdit = editLocation != null;
        builder.setTitle(isEdit ? "Editar Local" : "Novo Local");

        if (isEdit) {
            nameInput.setText(editLocation.name);
            latInput.setText(String.valueOf(editLocation.latitude));
            lngInput.setText(String.valueOf(editLocation.longitude));
            radiusInput.setText(String.valueOf(editLocation.radius));
            for (int i = 0; i < alarmValues.length; i++) {
                if (alarmValues[i].equals(editLocation.alarmMode)) {
                    alarmSpinner.setSelection(i);
                    break;
                }
            }
        } else {
            radiusInput.setText("200");
        }

        pickLocationBtn.setOnClickListener(v -> {
            String latStr = latInput.getText().toString().trim();
            String lngStr = lngInput.getText().toString().trim();
            if (!latStr.isEmpty() && !lngStr.isEmpty()) {
                try {
                    double lat = Double.parseDouble(latStr);
                    double lng = Double.parseDouble(lngStr);
                    String uri = "geo:" + lat + "," + lng + "?q=" + lat + "," + lng;
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Latitude/Longitude inválida", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Preencha latitude e longitude primeiro", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String latStr = latInput.getText().toString().trim();
            String lngStr = lngInput.getText().toString().trim();
            String radiusStr = radiusInput.getText().toString().trim();

            if (name.isEmpty() || latStr.isEmpty() || lngStr.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double lat = Double.parseDouble(latStr);
                double lng = Double.parseDouble(lngStr);
                int radius = radiusStr.isEmpty() ? 200 : Integer.parseInt(radiusStr);
                String alarmMode = alarmValues[alarmSpinner.getSelectedItemPosition()];

                if (isEdit) {
                    db.updateLocation(editLocation.id, name, lat, lng, radius, alarmMode);
                } else {
                    db.addLocation(name, lat, lng, radius, alarmMode);
                }

                // Re-registra geofences
                geofenceManager.removeAllGeofences();
                geofenceManager.registerAllGeofences();

                loadLocations();
                Toast.makeText(this, isEdit ? "Local atualizado!" : "Local adicionado!", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Valores numéricos inválidos", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // Adapter para a lista
    private class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
        private final List<GeofenceLocation> locations;
        private final Context context;

        LocationAdapter(List<GeofenceLocation> locations, Context context) {
            this.locations = locations;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_location, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GeofenceLocation loc = locations.get(position);

            holder.nameText.setText(loc.name);
            holder.coordsText.setText(String.format("📍 %.6f, %.6f | Raio: %dm",
                    loc.latitude, loc.longitude, loc.radius));
            holder.modeText.setText(loc.getAlarmModeLabel());
            holder.enabledSwitch.setChecked(loc.enabled);

            holder.enabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                db.toggleLocation(loc.id, isChecked);
                geofenceManager.removeAllGeofences();
                geofenceManager.registerAllGeofences();
            });

            holder.itemView.setOnClickListener(v -> showAddDialog(loc));

            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Remover local")
                        .setMessage("Remover \"" + loc.name + "\"?")
                        .setPositiveButton("Remover", (dialog, which) -> {
                            db.deleteLocation(loc.id);
                            geofenceManager.removeAllGeofences();
                            geofenceManager.registerAllGeofences();
                            loadLocations();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return locations.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameText, coordsText, modeText;
            SwitchMaterial enabledSwitch;

            ViewHolder(View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.nameText);
                coordsText = itemView.findViewById(R.id.coordsText);
                modeText = itemView.findViewById(R.id.modeText);
                enabledSwitch = itemView.findViewById(R.id.enabledSwitch);
            }
        }
    }
}
