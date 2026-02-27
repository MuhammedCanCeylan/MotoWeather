package com.canceylan.motoweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    // DÜZELTME: MotoSafetyEngine ile aynı dosya ismini kullanmalı!
    public static final String FILE_NAME = "MotoWeatherPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // --- DİL BUTONLARINI TANIMLAMA VE ÇALIŞTIRMA ---
        // activity_settings.xml içinde bu ID'leri verdiğinden emin ol: btnTr, btnEn

        View btnTr = findViewById(R.id.btnTr);
        View btnEn = findViewById(R.id.btnEn);

        if (btnTr != null) {
            btnTr.setOnClickListener(v -> changeLanguage("tr"));
        }

        if (btnEn != null) {
            btnEn.setOnClickListener(v -> changeLanguage("en"));
        }
        // ------------------------------------------------

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings, new SettingsFragment()).commit();
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Güvenlik Ayarları");
        }
    }

    // --- DİL DEĞİŞTİRME MEKANİZMASI ---
    private void changeLanguage(String langCode) {
        // 1. Dili Ayarla
        setLocale(langCode);

        // 2. Tercihi Kaydet (Uygulama açılınca hatırlasın diye)
        SharedPreferences prefs = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        prefs.edit().putString("Language", langCode).apply();

        // 3. Kullanıcıya Bilgi Ver
        Toast.makeText(this, "Dil Değiştirildi / Language Changed: " + langCode.toUpperCase(), Toast.LENGTH_SHORT).show();

        // 4. UYGULAMAYI YENİDEN BAŞLAT (Dilin aktif olması için şart!)
        Intent i = new Intent(this, MainActivity.class);
        // Geri tuşuna basınca eski dile dönmesin diye geçmişi temizle
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void setLocale(String langCode) {
        Locale myLocale = new Locale(langCode);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(myLocale);
        res.updateConfiguration(conf, dm);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // MotoSafetyEngine ile uyumlu dosya ismi
            getPreferenceManager().setSharedPreferencesName(FILE_NAME);
            getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);

            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            // --- 1. RÜZGAR AYARI ---
            EditTextPreference windPref = findPreference("pref_wind_limit");
            if (windPref != null) {
                windPref.setOnBindEditTextListener(e -> e.setInputType(InputType.TYPE_CLASS_NUMBER));
                windPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean success = requireContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putString("pref_wind_limit", newValue.toString())
                            .commit();
                    return true;
                });
            }

            // --- 2. SEVİYE AYARI ---
            ListPreference levelPref = findPreference("pref_rider_level");
            if (levelPref != null) {
                levelPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    requireContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
                            .edit().putString("pref_rider_level", newValue.toString()).commit();
                    return true;
                });
            }

            // --- 3. DİĞERLERİ ---
            EditTextPreference rainPref = findPreference("pref_rain_limit");
            if (rainPref != null) {
                rainPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    requireContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
                            .edit().putString("pref_rain_limit", newValue.toString()).commit();
                    return true;
                });
            }

            EditTextPreference tempPref = findPreference("pref_temp_min");
            if (tempPref != null) {
                tempPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    requireContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
                            .edit().putString("pref_temp_min", newValue.toString()).commit();
                    return true;
                });
            }
        }
    }
}