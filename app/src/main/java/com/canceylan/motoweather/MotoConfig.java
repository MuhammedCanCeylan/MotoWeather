package com.canceylan.motoweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class MotoConfig {
    private static final String FILE_NAME = "MOTO_HARD_SAVE"; // Dosya adı bu

    // ZORLA KAYDETME (String gelir, int gibi davranırız)
    public static void setWindLimit(Context context, String value) {
        SharedPreferences prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        // commit() kullanarak işlemi bekletiyoruz, yazmadan geçmez!
        prefs.edit().putString("pref_wind_limit", value).commit();
        Log.e("MOTO_CONFIG", "ZORLA YAZILDI: " + value);
    }

    public static void setRiderLevel(Context context, String level) {
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
                .edit().putString("pref_rider_level", level).commit();
    }

    // GÜVENLİ OKUMA
    public static int getWindLimit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String val = prefs.getString("pref_wind_limit", "25");
        try {
            return (int) Double.parseDouble(val);
        } catch (Exception e) {
            return 25;
        }
    }

    public static String getRiderLevel(Context context) {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
                .getString("pref_rider_level", "custom");
    }

    // TÜM DOSYAYI GÖSTER (Debug)
    public static String getAllData(Context context) {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).getAll().toString();
    }
}