package com.canceylan.motoweather;

import android.content.Context;
import java.util.Random;

public class RiskAnalyzer {

    private Context context;
    private Random random;

    // Analiz Sonucu Modeli
    public static class AnalysisResult {
        public String shortStatus;
        public String message;
        public String gearAdvice;
        public int colorResId;

        public AnalysisResult(String s, String m, String g, int c) {
            this.shortStatus = s;
            this.message = m;
            this.gearAdvice = g;
            this.colorResId = c;
        }
    }

    public RiskAnalyzer(Context context) {
        this.context = context;
        this.random = new Random();
    }

    public AnalysisResult analyze(double wind, double temp, int rainProb, double visibility, int isDay) {
        // 1. TEHLİKELİ DURUMLAR (Kırmızı)
        // Yağmur çoksa, fırtına varsa, görüş yoksa veya don varsa
        if (rainProb > 70 || wind > 45 || visibility < 1000 || temp < 0) {
            return new AnalysisResult(
                    context.getString(R.string.risk_tehlikeli_baslik),
                    getRandomMessage(R.array.msg_pool_tehlike), // XML'den çekiyor
                    context.getString(R.string.ekipman_full),
                    R.color.risk_tehlikeli
            );
        }

        // 2. DİKKAT GEREKTİREN (Sarı)
        // Hafif yağmur, rüzgar veya soğuk varsa
        if (rainProb > 40 || wind > 25 || temp < 10) {
            return new AnalysisResult(
                    context.getString(R.string.risk_dikkat_baslik),
                    getRandomMessage(R.array.msg_pool_dikkat), // XML'den çekiyor
                    context.getString(R.string.ekipman_yagmurluk),
                    R.color.risk_dikkat
            );
        }

        // 3. KEYİFLİ (Yeşil)
        // Her şey yolunda
        return new AnalysisResult(
                context.getString(R.string.risk_uygun_baslik),
                getRandomMessage(R.array.msg_pool_keyif), // XML'den çekiyor
                context.getString(R.string.ekipman_rahat),
                R.color.risk_uygun
        );
    }

    // --- GEVEZE TAVSİYE MOTORU ---
    // XML'deki String Array'den rastgele bir cümle çeker
    private String getRandomMessage(int arrayId) {
        String[] pool = context.getResources().getStringArray(arrayId);
        return pool[random.nextInt(pool.length)];
    }
}