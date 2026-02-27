package com.canceylan.motoweather;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WeatherFormatter {

    private Context context;
    private SharedPreferences prefs;

    public WeatherFormatter(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("MotoWeatherPrefs", Context.MODE_PRIVATE);
    }

    // --- YENİLENMİŞ, 50+ EMOJİ DESTEKLİ SİSTEM ---
    public String getEmojiForCode(int code, int isDay) {
        // GÜNDÜZ İKONLARI (isDay == 1)
        if (isDay == 1) {
            switch (code) {
                case 0: return "☀️";   // Açık (Güneş)
                case 1: return "🌤️";   // Az Bulutlu (Güneşli)
                case 2: return "⛅";   // Parçalı Bulutlu
                case 3: return "☁️";   // Çok Bulutlu
                case 45: return "🌫️";  // Sis
                case 48: return "🌁";   // Kırağı/Pus
                case 51: return "💧";   // Hafif Çiseleme
                case 53: return "☔";   // Çiseleme
                case 55: return "🌧️";   // Yoğun Çiseleme
                case 61: return "🌦️";   // Hafif Yağmur
                case 63: return "🌧️";   // Yağmur
                case 65: return "⛈️";   // Şiddetli Yağmur
                case 66: return "🥶";   // Dondurucu Yağmur
                case 67: return "🧊";   // Şiddetli Dondurucu
                case 71: return "🌨️";   // Hafif Kar
                case 73: return "❄️";   // Kar
                case 75: return "☃️";   // Yoğun Kar
                case 77: return "🌨️";   // Kar Taneleri
                case 80: return "🌦️";   // Sağanak
                case 81: return "🌧️";   // Şiddetli Sağanak
                case 82: return "🌊";   // Felaket (Sel Riski)
                case 85: return "❄️";   // Kar Sağanağı
                case 86: return "🌬️";   // Tipi
                case 95: return "⚡";   // Fırtına
                case 96: return "⛈️";   // Dolu
                case 99: return "🌪️";   // Şiddetli Dolu/Fırtına
            }
        }
        // GECE İKONLARI (isDay == 0) - Gece sürüşü için özel
        else {
            switch (code) {
                case 0: return "🌕";   // Açık (Dolunay)
                case 1: return "🌚";   // Az Bulutlu Gece
                case 2: return "☁️";   // Parçalı Bulutlu
                case 3: return "☁️";   // Çok Bulutlu
                case 45: return "🌫️";  // Sis
                case 48: return "🌁";   // Pus
                case 51: return "💧";   // Çiseleme
                case 61: return "☔";   // Yağmur
                case 63: return "🌧️";   // Yağmur
                case 65: return "⛈️";   // Şiddetli Yağmur
                case 71: return "🌨️";   // Kar
                case 95: return "🌩️";   // Gece Fırtınası
                default: return "🌧️";
            }
        }
        return "❓";
    }

    public String getShortCode(int code) {
        if (code == 0) return "A";
        if (code == 1) return "AB";
        if (code == 2) return "PB";
        if (code == 3) return "CB";
        if (code == 45) return "SIS";
        if (code == 48) return "PUS";
        if (code >= 51 && code <= 55) return "HY";
        if (code >= 61 && code <= 65) return "Y";
        if (code >= 66 && code <= 67) return "D-Y";
        if (code >= 80 && code <= 82) return "SY";
        if (code >= 71 && code <= 77) return "K";
        if (code >= 85 && code <= 86) return "KY";
        if (code >= 95) return "GSY";
        return "";
    }

    public String formatTarih(String dateStr) {
        try { String[] parts = dateStr.split("-"); return parts[2] + "/" + parts[1]; }
        catch (Exception e) { return dateStr; }
    }

    public String formatGunIsmi(String dateStr) {
        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            c.setTime(sdf.parse(dateStr));
            return new SimpleDateFormat("EEEE", Locale.getDefault()).format(c.getTime());
        } catch (Exception e) { return "Day"; }
    }

    public String formatSaat(int saat) {
        String formatTercihi = prefs.getString("saatBirim", "24");
        if (formatTercihi.equals("12")) {
            String ampm = (saat >= 12) ? "PM" : "AM";
            int saat12 = (saat > 12) ? saat - 12 : saat;
            if (saat12 == 0) saat12 = 12;
            return String.format(Locale.getDefault(), "%d:00 %s", saat12, ampm);
        } else {
            return String.format(Locale.getDefault(), "%02d:00", saat);
        }
    }

    public String formatSaatString(String saatStr) {
        try {
            String formatTercihi = prefs.getString("saatBirim", "24");
            if (!formatTercihi.equals("12")) return saatStr;
            String[] parcalar = saatStr.split(":");
            int saat = Integer.parseInt(parcalar[0]);
            int dakika = Integer.parseInt(parcalar[1]);
            String ampm = (saat >= 12) ? "PM" : "AM";
            int saat12 = (saat > 12) ? saat - 12 : saat;
            if (saat12 == 0) saat12 = 12;
            return String.format(Locale.getDefault(), "%d:%02d %s", saat12, dakika, ampm);
        } catch (Exception e) { return saatStr; }
    }

    public int saatDakikayaCevir(String s) {
        try { String[] p = s.split(":"); return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]); } catch (Exception e) { return 0; }
    }
}