package com.canceylan.motoweather;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefresh;
    private WeatherUI weatherUI;
    private WeatherManager weatherManager;
    private FusedLocationProviderClient fusedLocationClient;
    private SharedPreferences prefs;
    private MotoSafetyEngine safetyEngine;

    // Splash Perdesi (Yüklenme Ekranı)
    private RelativeLayout layoutSplash;

    // Dil Ayarlarını Yükle (Çok Önemli)
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Tema Ayarını Uygula
        prefs = getSharedPreferences("MotoWeatherPrefs", MODE_PRIVATE);
        int themeMode = prefs.getInt("temaModu", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        // 2. Tam Ekran (Edge-to-Edge) Modunu Aç
        // Bu kod sayesinde alttaki gri navigasyon barı şeffaf olur ve arkaplan oraya kadar uzanır.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Window window = getWindow();
                window.setFlags(
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                );
            }
        } catch (Exception e) { e.printStackTrace(); }

        setContentView(R.layout.activity_main);

        // 3. İlk Kurulum Kontrolü
        if (!prefs.getBoolean("setupDone", false)) {
            new InitialSetupDialog(this, this::restartApp).show();
        }

        // 4. Nesneleri Başlat
        safetyEngine = new MotoSafetyEngine(this);
        weatherUI = new WeatherUI(this);
        weatherManager = new WeatherManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 5. UI Bağlantıları
        swipeRefresh = findViewById(R.id.swipeRefresh);
        HorizontalScrollView scrollSaatlik = findViewById(R.id.scrollSaatlik);
        ImageView btnSettings = findViewById(R.id.btnMainSettings);
        layoutSplash = findViewById(R.id.layoutSplash);

        // 6. Ayarlar Butonu
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this);
                View view = getLayoutInflater().inflate(R.layout.dialog_settings, null);
                dialog.setContentView(view);

                new SettingsManager(MainActivity.this, view, dialog, () -> {
                    // Ayarlar kapandığında yapılacaklar:
                    if (safetyEngine != null) safetyEngine.reloadSettings(); // Motor ayarlarını tazele
                    refreshWeatherData(); // Havayı tekrar çek (Belki birim değişti)
                });
                dialog.show();
            });
        }

        // 7. Scroll Çakışmasını Önle (Yatay kaydırırken yenileme çalışmasın)
        if (scrollSaatlik != null && swipeRefresh != null) {
            scrollSaatlik.setOnTouchListener((v, event) -> {
                swipeRefresh.setEnabled(event.getAction() == android.view.MotionEvent.ACTION_UP || event.getAction() == android.view.MotionEvent.ACTION_CANCEL);
                return false;
            });
        }

        if (swipeRefresh != null) swipeRefresh.setOnRefreshListener(this::getLocation);

        // 8. İzinler ve Bildirim Planlama
        checkNotificationPermission();
        try { gunlukBildirimiPlanla(); } catch (Exception e) { e.printStackTrace(); }

        // BAŞLA
        getLocation();
    }

    private void refreshWeatherData() {
        String sLat = prefs.getString("lastLat", null);
        String sLon = prefs.getString("lastLon", null);

        if (sLat != null && sLon != null) {
            double lat = Double.parseDouble(sLat);
            double lon = Double.parseDouble(sLon);
            if(swipeRefresh != null) swipeRefresh.setRefreshing(true);

            weatherManager.getWeather(lat, lon, new WeatherManager.WeatherCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    weatherUI.updateData(response, () -> {
                        if(swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    });
                }
                @Override
                public void onError(String message) {
                    if(swipeRefresh != null) swipeRefresh.setRefreshing(false);
                }
            });
        } else {
            getLocation();
        }
    }

    private void hideSplash() {
        if (layoutSplash != null && layoutSplash.getVisibility() == View.VISIBLE) {
            layoutSplash.animate()
                    .alpha(0f)
                    .setDuration(700)
                    .withEndAction(() -> layoutSplash.setVisibility(View.GONE))
                    .start();
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            hideSplash();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                // Şehir ismini bulmak için arka planda thread aç
                new Thread(() -> findCityName(location.getLatitude(), location.getLongitude())).start();

                weatherManager.getWeather(location.getLatitude(), location.getLongitude(), new WeatherManager.WeatherCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        weatherUI.updateData(response, () -> {
                            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                            hideSplash(); // Veri gelince perdeyi kaldır
                        });

                        prefs.edit()
                                .putString("lastLat", String.valueOf(location.getLatitude()))
                                .putString("lastLon", String.valueOf(location.getLongitude()))
                                .apply();
                    }
                    @Override
                    public void onError(String message) {
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                        hideSplash(); // Hata olsa da perdeyi kaldır
                    }
                });
            } else {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                hideSplash();
            }
        }).addOnFailureListener(e -> {
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            hideSplash();
        });
    }

    // --- ŞEHİR İSMİNİ BULMA (UYGULAMA DİLİNE GÖRE) ---
    private void findCityName(double lat, double lon) {
        try {
            // 1. Kullanıcının seçtiği dili al
            String langCode = prefs.getString("Language", Locale.getDefault().getLanguage());

            // 2. Locale oluştur
            Locale geoLocale;
            if (langCode == null || langCode.isEmpty()) {
                geoLocale = Locale.getDefault();
            } else {
                geoLocale = new Locale(langCode);
            }

            // 3. Geocoder'ı bu dille başlat (Şehir ismi o dilde gelsin)
            Geocoder geocoder = new Geocoder(this, geoLocale);

            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String ilce = addresses.get(0).getSubAdminArea();
                String il = addresses.get(0).getAdminArea();

                if (ilce == null) ilce = addresses.get(0).getLocality();

                String konum = (ilce != null ? ilce : "") + (il != null ? ", " + il : "");
                if(konum.startsWith(", ")) konum = konum.substring(2);

                final String finalKonum = konum;
                runOnUiThread(() -> weatherUI.updateLocationText(finalKonum));
            }
        } catch (Exception e) {
            runOnUiThread(() -> weatherUI.updateLocationText(getString(R.string.konum_bilinmiyor)));
        }
    }

    private void gunlukBildirimiPlanla() {
        try {
            WorkManager.getInstance(this).cancelUniqueWork("MotoGunlukBildirim");
            int hedefSaat = prefs.getInt("alarmSaat", 9);
            int hedefDakika = prefs.getInt("alarmDakika", 0);
            java.util.Calendar simdi = java.util.Calendar.getInstance();
            java.util.Calendar hedef = java.util.Calendar.getInstance();
            hedef.set(java.util.Calendar.HOUR_OF_DAY, hedefSaat);
            hedef.set(java.util.Calendar.MINUTE, hedefDakika);
            hedef.set(java.util.Calendar.SECOND, 0);
            if (hedef.before(simdi)) hedef.add(java.util.Calendar.DAY_OF_MONTH, 1);
            long gecikme = hedef.getTimeInMillis() - simdi.getTimeInMillis();
            PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(BildirimWorker.class, 24, TimeUnit.HOURS).setInitialDelay(gecikme, TimeUnit.MILLISECONDS).build();
            WorkManager.getInstance(this).enqueueUniquePeriodicWork("MotoGunlukBildirim", ExistingPeriodicWorkPolicy.UPDATE, req);
        } catch(Exception e){ e.printStackTrace(); }
    }

    // Uygulamayı Yeniden Başlatma (Dil veya ayarlar değişince)
    private void restartApp() {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
        finish();
        System.exit(0);
    }
}