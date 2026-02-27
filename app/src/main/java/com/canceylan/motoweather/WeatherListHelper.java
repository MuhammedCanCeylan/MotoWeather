package com.canceylan.motoweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherListHelper {
    private Context context;
    private WeatherFormatter formatter;
    private MotoSafetyEngine safetyEngine;

    public WeatherListHelper(Context context) {
        this.context = context;
        this.formatter = new WeatherFormatter(context);
        this.safetyEngine = new MotoSafetyEngine(context);
    }

    // --- ÖZEL: UYGULAMANIN DİLİNİ ÇEKME ---
    private Locale getAppLocale() {
        SharedPreferences prefs = context.getSharedPreferences("MotoWeatherPrefs", Context.MODE_PRIVATE);
        String langCode = prefs.getString("Language", "en");
        return new Locale(langCode);
    }

    // --- GÜNLÜK LİSTE ---
    public void updateDaily(LinearLayout container, JSONObject dailyData, boolean useFahrenheit) {
        try {
            safetyEngine.reloadSettings();

            if (container == null) return;
            container.removeAllViews();

            if (!dailyData.has("time")) return;

            JSONArray times = dailyData.getJSONArray("time");
            JSONArray maxTemps = dailyData.getJSONArray("temperature_2m_max");
            JSONArray minTemps = dailyData.getJSONArray("temperature_2m_min");
            JSONArray codes = dailyData.getJSONArray("weathercode");

            JSONArray windSpeeds = dailyData.has("windspeed_10m_max") ? dailyData.getJSONArray("windspeed_10m_max") : null;
            JSONArray rainProbs = dailyData.has("precipitation_probability_max") ? dailyData.getJSONArray("precipitation_probability_max") : null;

            LayoutInflater inflater = LayoutInflater.from(context);
            Locale appLocale = getAppLocale(); // Seçili dili al

            for (int i = 0; i < 5 && i < times.length(); i++) {
                View itemView = inflater.inflate(R.layout.item_gunluk_kart, container, false);

                TextView txtGun = itemView.findViewById(R.id.txtGun);
                TextView txtTarih = itemView.findViewById(R.id.txtTarih);
                TextView txtEmoji = itemView.findViewById(R.id.txtEmoji);
                TextView txtMinTemp = itemView.findViewById(R.id.txtMinTemp);
                TextView txtMaxTemp = itemView.findViewById(R.id.txtMaxTemp);

                LinearLayout layoutGizliDetay = itemView.findViewById(R.id.layoutGizliDetay);
                TextView txtRuzgarDetay = itemView.findViewById(R.id.txtRuzgarDetay);
                TextView txtNemDetay = itemView.findViewById(R.id.txtNemDetay);
                View cardRoot = itemView.findViewById(R.id.cardRoot);

                RiskTimelineView riskTimelineBar = itemView.findViewById(R.id.riskTimelineBar);

                String dateStr = times.getString(i);
                double max = maxTemps.getDouble(i);
                double min = minTemps.getDouble(i);
                int code = codes.getInt(i);

                double windVal = (windSpeeds != null) ? windSpeeds.getDouble(i) : 0;
                int rainVal = (rainProbs != null) ? rainProbs.getInt(i) : 0;

                // TARIH FORMATLAMA (DİLE GÖRE)
                try {
                    SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date date = inFormat.parse(dateStr);

                    // Burada "tr" yerine appLocale kullanıyoruz!
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", appLocale);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", appLocale);

                    if (txtGun != null) txtGun.setText(dayFormat.format(date));
                    if (txtTarih != null) txtTarih.setText(dateFormat.format(date));
                } catch (Exception e) {
                    if (txtGun != null) txtGun.setText(dateStr);
                }

                if (txtEmoji != null) txtEmoji.setText(IconHelper.getWeatherEmoji(code, max, false));
                if (txtMinTemp != null) txtMinTemp.setText(formatTemp(min, useFahrenheit));
                if (txtMaxTemp != null) txtMaxTemp.setText(formatTemp(max, useFahrenheit));

                // ÇOKLU DİL DESTEKLİ DETAYLAR
                if (txtRuzgarDetay != null) {
                    String label = context.getString(R.string.label_maks_ruzgar, (int)windVal + " km/h");
                    txtRuzgarDetay.setText(label);
                }
                if (txtNemDetay != null) {
                    String label = context.getString(R.string.label_yagis_ihtimali, "%" + rainVal);
                    txtNemDetay.setText(label);
                }

                int[] dailyColors = generateDailyRiskColors(max, min, windVal, rainVal);
                if (riskTimelineBar != null) riskTimelineBar.setRiskData(dailyColors);

                if (cardRoot != null && layoutGizliDetay != null) {
                    cardRoot.setOnClickListener(v -> layoutGizliDetay.setVisibility(
                            layoutGizliDetay.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
                }
                container.addView(itemView);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- SAATLİK LİSTE ---
    public void updateHourly(LinearLayout container, JSONObject hourlyData, int currentHour, boolean useFahrenheit) {
        try {
            safetyEngine.reloadSettings();
            if (container == null) return;
            container.removeAllViews();

            if (!hourlyData.has("time")) return;

            JSONArray times = hourlyData.getJSONArray("time");
            JSONArray temps = hourlyData.getJSONArray("temperature_2m");
            JSONArray codes = hourlyData.getJSONArray("weathercode");

            LayoutInflater inflater = LayoutInflater.from(context);

            int count = 0;
            for (int i = 0; i < times.length(); i++) {
                String timeStr = times.getString(i);
                String hourOnly = timeStr.substring(timeStr.indexOf("T") + 1);

                // Basit mantık: i < currentHour atlaması JSON yapısına göre değişebilir
                // Ama şimdilik API'den gelen sıraya güveniyoruz

                if (count >= 24) break;

                View itemView = inflater.inflate(R.layout.item_saatlik_buzlu, container, false);

                TextView txtSaat = itemView.findViewById(R.id.txtSaat);
                TextView txtEmoji = itemView.findViewById(R.id.txtEmoji);
                TextView txtDurumKisa = itemView.findViewById(R.id.txtDurumKisa);
                TextView txtDerece = itemView.findViewById(R.id.txtDerece);

                double temp = temps.getDouble(i);
                int code = codes.getInt(i);
                // Basit gece kontrolü (saate göre)
                int h = Integer.parseInt(hourOnly.split(":")[0]);
                boolean isNight = (h < 6 || h >= 20);

                if (txtSaat != null) txtSaat.setText(hourOnly);
                if (txtEmoji != null) txtEmoji.setText(IconHelper.getWeatherEmoji(code, temp, isNight));
                if (txtDerece != null) txtDerece.setText(formatTemp(temp, useFahrenheit));

                // BURASI ARTIK XML'DEN GELİYOR
                if (txtDurumKisa != null) txtDurumKisa.setText(getWeatherDescription(code));

                container.addView(itemView);
                count++;
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private int[] generateDailyRiskColors(double maxTemp, double minTemp, double maxWind, int rainProb) {
        int[] colors = new int[24];
        for (int h = 0; h < 24; h++) {
            double currentTemp = (h >= 7 && h <= 19) ? maxTemp : minTemp;
            double currentWind = (h >= 10 && h <= 18) ? maxWind : maxWind * 0.7;
            int currentRain = rainProb;
            colors[h] = safetyEngine.analyzeHour(currentWind, currentRain, currentTemp, h);
        }
        return colors;
    }

    // --- HAVA DURUMU KODLARINI TERCÜME EDEN METOD ---
    private String getWeatherDescription(int code) {
        if (code == 0) return context.getString(R.string.desc_gunesli);
        if (code == 1 || code == 2 || code == 3) return context.getString(R.string.desc_parcali);
        if (code == 45 || code == 48) return context.getString(R.string.desc_sis);
        if (code >= 51 && code <= 67) return context.getString(R.string.desc_yagmur);
        if (code >= 71 && code <= 77) return context.getString(R.string.desc_kar);
        if (code >= 80 && code <= 82) return context.getString(R.string.desc_saganak);
        if (code >= 95) return context.getString(R.string.desc_firtina);
        return context.getString(R.string.risk_uygun_baslik); // Normal
    }

    private String formatTemp(double tempC, boolean useFahrenheit) {
        if (useFahrenheit) {
            double tempF = (tempC * 1.8) + 32;
            return String.format(Locale.getDefault(), "%.0f°F", tempF);
        }
        return String.format(Locale.getDefault(), "%.0f°", tempC);
    }
}