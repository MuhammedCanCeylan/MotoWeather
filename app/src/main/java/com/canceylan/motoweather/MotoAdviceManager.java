package com.canceylan.motoweather;

import android.content.Context;

public class MotoAdviceManager {

    /**
     * ÇOKLU DİL DESTEKLİ VERSİYON
     * Artık metinleri strings.xml dosyasından çekiyor.
     */
    public static String[] getAdvice(Context context, MotoSafetyEngine.RiskAnalysis analiz, double temp, double wind, int rainProb, boolean isNight) {
        String title = "";
        StringBuilder body = new StringBuilder(); // StringBuilder daha verimlidir

        // 1. KIRMIZI ALARM
        if (analiz.status == MotoSafetyEngine.RiskStatus.DANGER) {
            title = context.getString(R.string.adv_title_danger);

            if (wind > 60) {
                body.append(context.getString(R.string.adv_body_danger_wind));
            } else if (temp < 0) {
                body.append(context.getString(R.string.adv_body_danger_ice));
            } else if (rainProb > 80 && wind > 40) {
                body.append(context.getString(R.string.adv_body_danger_storm));
            } else {
                body.append(context.getString(R.string.adv_body_danger_general));
            }
            return new String[]{title, body.toString()};
        }

        // 2. NORMAL ANALİZ
        title = context.getString(R.string.adv_title_safe);

        // A) SICAKLIK
        if (temp <= 5) {
            title = context.getString(R.string.adv_title_cold_freeze);
            body.append(context.getString(R.string.adv_body_cold_freeze));
        }
        else if (temp <= 12) {
            title = context.getString(R.string.adv_title_cold);
            body.append(context.getString(R.string.adv_body_cold));
        }
        else if (temp <= 25) {
            if (wind < 20 && rainProb < 20) title = context.getString(R.string.adv_title_perfect);
            body.append(context.getString(R.string.adv_body_perfect));
        }
        else if (temp <= 32) {
            title = context.getString(R.string.adv_title_warm);
            body.append(context.getString(R.string.adv_body_warm));
        }
        else {
            title = context.getString(R.string.adv_title_hot);
            body.append(context.getString(R.string.adv_body_hot));
        }

        body.append("\n\n");

        // B) YAĞMUR
        if (rainProb > 50 || analiz.message.contains("Yağmur")) {
            title = context.getString(R.string.adv_title_wet);
            body.append(context.getString(R.string.adv_body_wet));
        }
        else if (rainProb > 20) {
            body.append(context.getString(R.string.adv_body_wet_light));
        }

        // C) RÜZGAR
        if (wind > 40) {
            if (!title.contains("Alarm")) title = context.getString(R.string.adv_title_wind);
            body.append("\n\n").append(context.getString(R.string.adv_body_wind_strong));
        }
        else if (wind > 25) {
            body.append("\n\n").append(context.getString(R.string.adv_body_wind_light));
        }

        // D) GECE
        if (isNight) {
            if (!title.contains("Alarm")) title += context.getString(R.string.adv_suffix_night);
            body.append("\n\n").append(context.getString(R.string.adv_body_night));
        }

        // E) FİNAL
        if (analiz.status == MotoSafetyEngine.RiskStatus.SAFE && body.length() < 100) {
            body.append("\n").append(context.getString(R.string.adv_body_wish));
        }

        return new String[]{title, body.toString().trim()};
    }
}