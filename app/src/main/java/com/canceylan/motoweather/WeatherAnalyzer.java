package com.canceylan.motoweather;

import android.content.Context;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import java.util.Calendar;

public class WeatherAnalyzer {

    public static void analizEt(Context context, double ruzgar, double hamle, double sicaklik, int yagmur, double gorus,
                                int gunduzMu, String motor, String seviye, int[] limitler, boolean[] izinler, int[] yasaklar,
                                TextView txtBaslik, TextView txtDetay, TextView txtOneri, CardView kart, boolean anaKart) {

        // Seviye Indexi
        int idx = seviye.equals("Orta") ? 1 : (seviye.equals("Ileri") ? 2 : 0);

        int lRuzgar = limitler[idx]; // Rüzgar Limiti
        int lYagmur = limitler[idx+3]; // Yağmur Limiti (Diziyi düzleştirdik varsayıyorum ya da MainActivity'den doğru parametre gelecek)
        // NOT: Basitlik için limitleri parametre olarak tek tek almak yerine MainActivity'de hazırlayıp buraya işlenmiş veriyi atmak daha kolay.
        // Ama şimdilik MainActivity'deki mantığı buraya taşıyalım.
    }

    // KAFAN KARIŞMASIN DİYE ANALİZ KODUNU MAIN ACTIVITY'DE TUTALIM, SADECE AYARLARI AYIRALIM.
    // ŞU AN İÇİN SADECE SETTINGS CLASS'INI AYIRMAK YETERLİ OLACAK.
}