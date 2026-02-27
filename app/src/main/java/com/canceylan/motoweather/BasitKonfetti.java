package com.canceylan.motoweather;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BasitKonfetti extends View {
    private List<Parcacik> parcaciklar = new ArrayList<>();
    private Paint paint;
    private Random random = new Random();
    private boolean animasyonAktif = false;
    private int mod = 0; // 1: Yağmur, 2: Kar

    public BasitKonfetti(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
    }

    public void yagmurBaslat() {
        mod = 1;
        baslat(Color.BLUE);
    }

    public void karBaslat() {
        mod = 2;
        baslat(Color.WHITE);
    }

    private void baslat(int renk) {
        animasyonAktif = true;
        parcaciklar.clear();
        // Ekrana rastgele 100 parçacık ekle
        for (int i = 0; i < 100; i++) {
            parcaciklar.add(new Parcacik(random.nextInt(1000), random.nextInt(2000) * -1, renk));
        }
        invalidate(); // Çizimi başlat
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!animasyonAktif) return;

        for (Parcacik p : parcaciklar) {
            paint.setColor(p.renk);
            if (mod == 1) {
                // Yağmur: Dikdörtgen çiz
                canvas.drawRect(p.x, p.y, p.x + 5, p.y + 25, paint);
                p.y += 30; // Hızlı düş
            } else {
                // Kar: Yuvarlak çiz
                canvas.drawCircle(p.x, p.y, 10, paint);
                p.y += 10; // Yavaş düş
            }

            // Ekrandan çıkınca yukarı ışınla
            if (p.y > getHeight()) {
                p.y = -50;
                p.x = random.nextInt(getWidth());
            }
        }
        invalidate(); // Döngüyü sürdür
    }

    private class Parcacik {
        float x, y;
        int renk;
        Parcacik(float x, float y, int renk) {
            this.x = x; this.y = y; this.renk = renk;
        }
    }
}