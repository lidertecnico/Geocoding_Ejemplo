package aplicacionesmoviles.avanzado.todosalau.geocodingejemplo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private TextView tvLocationInfo;
    private TextView tvCounter;
    private LocationManager locationManager;
    private Button btnGetLocation;
    private CountDownTimer countDownTimer;
    private int countdownDurationSeconds = 120; // Duración del contador en segundos (2 minutos)
    private boolean locationFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencias a elementos de la interfaz de usuario
        tvLocationInfo = findViewById(R.id.tvLocationInfo);
        tvCounter = findViewById(R.id.tvCounter);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Solicitar permiso de ubicación si no está otorgado
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }

        // Configurar el botón para iniciar el contador
        btnGetLocation.setOnClickListener(view -> startCountdown());
    }

    private void startCountdown() {
        // Deshabilitar el botón mientras se ejecuta el contador
        btnGetLocation.setEnabled(false);

        // Iniciar el contador
        countDownTimer = new CountDownTimer(countdownDurationSeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Actualizar el TextView del contador con el tiempo restante
                tvCounter.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                // Restaurar el texto del botón y habilitarlo nuevamente
                btnGetLocation.setEnabled(true);
                if (!locationFound) {
                    // Si no se ha encontrado la ubicación al finalizar el contador
                    tvLocationInfo.setText("Tiempo agotado. Ubicación no encontrada.");
                }
            }
        }.start();

        // Solicitar actualizaciones de ubicación
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    // Detener el contador
                    countDownTimer.cancel();
                    locationFound = true;
                    // Obtener la ubicación actual
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Geocodificar la ubicación
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (addresses.size() > 0) {
                            String address = addresses.get(0).getAddressLine(0);
                            tvLocationInfo.setText("Ubicación geocodificada: " + address);
                            tvCounter.setText("!Listo, quedó traducida la ubicación!");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onProviderEnabled(@NonNull String provider) {
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                }

                // Este método está obsoleto, pero todavía lo mantenemos para compatibilidad con versiones anteriores
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}