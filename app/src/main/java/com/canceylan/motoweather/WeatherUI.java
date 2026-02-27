package com.canceylan.motoweather;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class WeatherUI {

    private Activity activity;
    private MotoSafetyEngine safetyEngine;
    private WeatherFormatter formatter;
    private WeatherListHelper listHelper;
    private SharedPreferences prefs;

    // UI Elemanları
    private TextView txtDurumBaslik, txtRuzgar, txtSicaklik, txtEkipman, txtOneri, txtKonum, txtTarih, txtAnaVeriler, txtYagmurOrani, txtHissedilen, txtBasinc, txtGunDogumu, txtGunBatimi;
    private TextView txtBirim;
    private TextView txtAnaIkon;
    private TextView txtAltRiskBar;

    private ImageView btnInfo;
    private LinearLayout layoutGunlukTahminler, layoutSaatlik, layoutTavsiye;
    private LottieAnimationView lottieWeather;
    private AstroPathView astroView;

    private boolean useFahrenheit = false;

    public WeatherUI(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences("MotoWeatherPrefs", Context.MODE_PRIVATE);
        this.useFahrenheit = prefs.getBoolean("useFahrenheit", false);

        this.safetyEngine = new MotoSafetyEngine(activity);
        this.formatter = new WeatherFormatter(activity);
        this.listHelper = new WeatherListHelper(activity);
        initViews();
    }

    private void initViews() {
        try {
            txtDurumBaslik = activity.findViewById(R.id.txtDurumBaslik);
            txtTarih = activity.findViewById(R.id.txtTarih);
            txtAnaVeriler = activity.findViewById(R.id.txtAnaVeriler);
            txtRuzgar = activity.findViewById(R.id.txtRuzgar);
            txtSicaklik = activity.findViewById(R.id.txtSicaklik);
            txtBirim = activity.findViewById(R.id.txtBirim);
            txtKonum = activity.findViewById(R.id.txtKonum);
            txtYagmurOrani = activity.findViewById(R.id.txtYagmurOrani);
            txtHissedilen = activity.findViewById(R.id.txtHissedilen);
            txtBasinc = activity.findViewById(R.id.txtBasinc);
            layoutTavsiye = activity.findViewById(R.id.layoutTavsiye);
            txtOneri = activity.findViewById(R.id.txtOneri);
            txtEkipman = activity.findViewById(R.id.txtEkipman);
            txtAltRiskBar = activity.findViewById(R.id.txtAltRiskBar);

            try {
                txtAnaIkon = activity.findViewById(R.id.txtAnaIkon);
            } catch (Exception e) { txtAnaIkon = null; }

            layoutGunlukTahminler = activity.findViewById(R.id.layoutGunlukTahminler);
            layoutSaatlik = activity.findViewById(R.id.layoutSaatlik);
            astroView = activity.findViewById(R.id.astroView);
            txtGunDogumu = activity.findViewById(R.id.txtGunDogumu);
            txtGunBatimi = activity.findViewById(R.id.txtGunBatimi);
            btnInfo = activity.findViewById(R.id.btnInfo);
            lottieWeather = activity.findViewById(R.id.lottieWeather);

            if (btnInfo != null) btnInfo.setOnClickListener(v -> showInfoDialog());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showInfoDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            View view = LayoutInflater.from(activity).inflate(R.layout.dialog_ikon_rehberi, null);
            builder.setView(view);
            AlertDialog dialog = builder.create();
            if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            View btnKapat = view.findViewById(R.id.btnKapat);
            if (btnKapat != null) btnKapat.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void updateLocationText(String text) {
        if (txtKonum != null) txtKonum.setText(text);
    }

    private String formatTemp(double tempC) {
        if (useFahrenheit) {
            double tempF = (tempC * 1.8) + 32;
            return String.format(Locale.US, "%.0f", tempF);
        }
        return String.format(Locale.US, "%.0f", tempC);
    }

    private String getTempUnit() {
        return useFahrenheit ? "°F" : "°";
    }

    // --- ÖZEL DİL ÇEKME METODU ---
    // Tarihlerin doğru dilde görünmesi için bunu kullanacağız
    private Locale getAppLocale() {
        String langCode = prefs.getString("Language", "en"); // Varsayılan İngilizce olsun
        return new Locale(langCode);
    }

    public void updateData(JSONObject json, Runnable onAnimationReady) {
        try {
            this.useFahrenheit = prefs.getBoolean("useFahrenheit", false);
            if (safetyEngine != null) safetyEngine.reloadSettings();

            // Uygulamanın seçili dili
            Locale currentLocale = getAppLocale();

            if (txtBirim != null) txtBirim.setText(useFahrenheit ? "°F" : "°");

            // --- TARİH DÜZELTME ---
            // Artık telefonun değil, uygulamanın diline göre tarih yazacak
            if (txtTarih != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("d MMMM EEEE", currentLocale);
                txtTarih.setText(sdf.format(Calendar.getInstance().getTime()));
            }

            if (!json.has("current_weather")) return;
            JSONObject current = json.getJSONObject("current_weather");

            double ruzgar = current.getDouble("windspeed");
            double sicaklik = current.getDouble("temperature");
            int weatherCode = current.getInt("weathercode");
            int isDay = current.getInt("is_day");

            int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            boolean isNight = (isDay == 0);

            JSONObject hourly = json.getJSONObject("hourly");
            int humidity = 50;
            double apparentTemp = sicaklik;
            double pressure = 1013;
            int rainProb = 0;

            try {
                JSONArray timeArray = hourly.getJSONArray("time");
                int index = 0;
                for(int i=0; i<timeArray.length(); i++) {
                    String t = timeArray.getString(i);
                    // Saat eşleştirmesini Locale.US ile yapıyoruz (API formatı değişmez)
                    if(t.endsWith(String.format(Locale.US, "T%02d:00", currentHour))) {
                        index = i;
                        break;
                    }
                }
                if(hourly.has("relativehumidity_2m")) humidity = hourly.getJSONArray("relativehumidity_2m").getInt(index);
                if(hourly.has("apparent_temperature")) apparentTemp = hourly.getJSONArray("apparent_temperature").getDouble(index);
                if(hourly.has("surface_pressure")) pressure = hourly.getJSONArray("surface_pressure").getDouble(index);
                if(hourly.has("precipitation_probability")) rainProb = hourly.getJSONArray("precipitation_probability").getInt(index);
            } catch (Exception e) { e.printStackTrace(); }

            if (txtSicaklik != null) txtSicaklik.setText(formatTemp(sicaklik));
            if (txtRuzgar != null) txtRuzgar.setText(String.format(currentLocale, "%.1f %s", ruzgar, activity.getString(R.string.unit_kmh)));
            if (txtYagmurOrani != null) txtYagmurOrani.setText("%" + humidity);
            if (txtHissedilen != null) {
                String tempStr = formatTemp(apparentTemp) + getTempUnit();
                txtHissedilen.setText(activity.getString(R.string.label_hissedilen, tempStr));
            }
            if (txtBasinc != null) txtBasinc.setText(String.format(currentLocale, "%.0f hPa", pressure));

            // MOTORCU ANALİZİ
            if (safetyEngine != null) {
                // Not: GÜVENLİ yazısı MotoSafetyEngine'den geliyor.
                // Onu düzeltmek için o dosyayı atman lazım.
                String kisaMesaj = safetyEngine.getRiskMessage(ruzgar, 0, sicaklik, currentHour);
                int renk = safetyEngine.analyzeHour(ruzgar, 0, sicaklik, currentHour);

                if (txtDurumBaslik != null) {
                    txtDurumBaslik.setText(kisaMesaj);
                    txtDurumBaslik.setTextColor(renk);
                }

                MotoSafetyEngine.RiskAnalysis analiz = safetyEngine.analyze(sicaklik, ruzgar, rainProb, !isNight);

                if (txtOneri != null && txtEkipman != null) {
                    // Dil desteği için activity gönderiyoruz
                    String[] tavsiyeler = MotoAdviceManager.getAdvice(activity, analiz, sicaklik, ruzgar, rainProb, isNight);
                    txtOneri.setText(tavsiyeler[0]);
                    txtEkipman.setText(tavsiyeler[1]);
                }

                if (txtAltRiskBar != null) {
                    txtAltRiskBar.setText(analiz.message.toUpperCase());
                    try {
                        txtAltRiskBar.setBackgroundColor(Color.parseColor(analiz.colorCode));
                    } catch (Exception e) {}
                }
            }

            if (txtAnaIkon != null) {
                try {
                    String emoji = IconHelper.getWeatherEmoji(weatherCode, sicaklik, isNight);
                    txtAnaIkon.setText(emoji);
                } catch (Exception e) {}
            }

            updateWeatherAnimation(weatherCode, isNight, onAnimationReady);

            if (json.has("daily")) {
                JSONObject daily = json.getJSONObject("daily");
                if (txtAnaVeriler != null) {
                    double minToday = daily.getJSONArray("temperature_2m_min").getDouble(0);
                    double maxToday = daily.getJSONArray("temperature_2m_max").getDouble(0);
                    txtAnaVeriler.setText(activity.getString(R.string.format_min_max, formatTemp(minToday), formatTemp(maxToday)));
                }
                updateSun(daily);

                // NOT: Listelerdeki 'Sisli', 'Parçalı Bulutlu' yazıları listHelper içinde.
                // Onu da düzeltmek için WeatherListHelper.java lazım.
                listHelper.updateDaily(layoutGunlukTahminler, daily, useFahrenheit);
            }

            listHelper.updateHourly(layoutSaatlik, hourly, currentHour, useFahrenheit);

        } catch (Exception e) {
            e.printStackTrace();
            if (onAnimationReady != null) onAnimationReady.run();
        }
    }

    public void updateData(JSONObject json) {
        updateData(json, null);
    }

    private void updateWeatherAnimation(int code, boolean isNight, Runnable onReady) {
        if (lottieWeather == null) {
            if (onReady != null) onReady.run();
            return;
        }

        int animRes;
        if (isNight) {
            if (code >= 95) animRes = R.raw.anim_firtina;
            else if ((code >= 71 && code <= 77) || code == 85 || code == 86) animRes = R.raw.anim_kar;
            else if ((code >= 51 && code <= 67) || code == 80 || code == 81 || code == 82) animRes = R.raw.anim_yagmur;
            else if (code == 45 || code == 48) animRes = R.raw.anim_sis;
            else animRes = R.raw.anim_gece;
        } else {
            if (code >= 95) animRes = R.raw.anim_firtina;
            else if ((code >= 71 && code <= 77) || code == 85 || code == 86) animRes = R.raw.anim_kar;
            else if ((code >= 51 && code <= 67) || code == 80 || code == 81 || code == 82) animRes = R.raw.anim_yagmur;
            else if (code == 45 || code == 48) animRes = R.raw.anim_sis;
            else if (code == 2 || code == 3) animRes = R.raw.anim_bulutlu;
            else animRes = R.raw.anim_gunesli;
        }

        lottieWeather.setAnimation(animRes);
        lottieWeather.removeAllAnimatorListeners();
        lottieWeather.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (onReady != null) onReady.run();
            }
            @Override public void onAnimationEnd(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });
        lottieWeather.playAnimation();
    }

    private void updateSun(JSONObject daily) {
        try {
            String sunrise = "06:00"; String sunset = "18:00";
            if (daily.has("sunrise")) { String r = daily.getJSONArray("sunrise").getString(0); if(r.contains("T")) sunrise = r.split("T")[1]; }
            if (daily.has("sunset")) { String r = daily.getJSONArray("sunset").getString(0); if(r.contains("T")) sunset = r.split("T")[1]; }

            if (txtGunDogumu != null) txtGunDogumu.setText(formatter.formatSaatString(sunrise));
            if (txtGunBatimi != null) txtGunBatimi.setText(formatter.formatSaatString(sunset));

            updateAstroVisuals(sunrise, sunset);
        } catch (Exception e) {}
    }

    private void updateAstroVisuals(String sunriseStr, String sunsetStr) {
        if (astroView == null) return;
        try {
            int sunriseMin = formatter.saatDakikayaCevir(sunriseStr);
            int sunsetMin = formatter.saatDakikayaCevir(sunsetStr);
            Calendar c = Calendar.getInstance();
            int currentMin = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
            boolean isNight = false;
            float progress = 0f;

            if (currentMin >= sunriseMin && currentMin <= sunsetMin) {
                isNight = false;
                float totalDay = sunsetMin - sunriseMin;
                float elapsed = currentMin - sunriseMin;
                progress = elapsed / totalDay;
            } else {
                isNight = true;
                float totalNight = (24 * 60) - sunsetMin + sunriseMin;
                float elapsed;
                if (currentMin > sunsetMin) elapsed = currentMin - sunsetMin;
                else elapsed = (24 * 60 - sunsetMin) + currentMin;
                progress = elapsed / totalNight;
            }
            astroView.setNightMode(isNight);
            astroView.setProgress(progress);
        } catch (Exception e) { e.printStackTrace(); }
    }
}