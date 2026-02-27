package com.canceylan.motoweather;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast; // Mesaj için
import java.util.Locale;

public class CardManager {

    public static void initCard(View cardView, Activity activity, double wind, double rain, double minTemp) {
        LinearLayout hiddenLayout = cardView.findViewById(R.id.layoutGizliDetay);
        TextView txtRuzgar = cardView.findViewById(R.id.txtRuzgarDetay);
        TextView txtNem = cardView.findViewById(R.id.txtNemDetay);

        if (hiddenLayout != null) {
            hiddenLayout.setLayoutTransition(new LayoutTransition());

            if (txtRuzgar != null) txtRuzgar.setText(String.format(Locale.getDefault(), "Maks. Rüzgar: %.0f km/h", wind));
            if (txtNem != null) txtNem.setText(String.format(Locale.getDefault(), "Toplam Yağış: %.1f mm", rain));

            SafetyRiskBar safetyBar;

            // Eğer bar zaten varsa onu al, yoksa yeni yap
            View existingBar = hiddenLayout.findViewWithTag("SAFETY_BAR");
            if (existingBar instanceof SafetyRiskBar) {
                safetyBar = (SafetyRiskBar) existingBar;
                // ÖNEMLİ: Bar zaten varsa bile verileri güncelle
                safetyBar.updateWeatherData(wind, rain, minTemp);
            } else {
                safetyBar = new SafetyRiskBar(activity, wind, rain, minTemp);
                safetyBar.setTag("SAFETY_BAR");
                LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 120);
                barParams.setMargins(0, 20, 0, 10);
                safetyBar.setLayoutParams(barParams);
                hiddenLayout.addView(safetyBar);
            }



            // CardManager setOnClickListener içi:
            cardView.setOnClickListener(v -> {
                if (hiddenLayout.getVisibility() == View.GONE) {
                    hiddenLayout.setVisibility(View.VISIBLE);

                    safetyBar.refreshAndAnimate();

                    MotoSafetyEngine eng = safetyBar.getEngine();
                   // Toast.makeText(activity, eng.debugStatus, Toast.LENGTH_LONG).show();

                } else {
                    hiddenLayout.setVisibility(View.GONE);
                }
            });
        }
    }

    private static class SafetyRiskBar extends View {
        private Paint glowPaint, corePaint, bgPaint;
        private double dayMaxWind, dayRain, dayMinTemp;
        private float progress = 0f;
        private MotoSafetyEngine safetyEngine;

        public SafetyRiskBar(Context context, double wind, double rain, double minTemp) {
            super(context);
            this.dayMaxWind = wind;
            this.dayRain = rain;
            this.dayMinTemp = minTemp;
            this.safetyEngine = new MotoSafetyEngine(context);
            init();
        }

        // Verileri güncellemek için metod
        public void updateWeatherData(double wind, double rain, double temp) {
            this.dayMaxWind = wind;
            this.dayRain = rain;
            this.dayMinTemp = temp;
        }

        public MotoSafetyEngine getEngine() { return safetyEngine; }

        private void init() {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG); bgPaint.setColor(Color.parseColor("#222222")); bgPaint.setStyle(Paint.Style.STROKE); bgPaint.setStrokeWidth(16f); bgPaint.setStrokeCap(Paint.Cap.ROUND);
            glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG); glowPaint.setStyle(Paint.Style.STROKE); glowPaint.setStrokeWidth(25f); glowPaint.setStrokeCap(Paint.Cap.ROUND); glowPaint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));
            corePaint = new Paint(Paint.ANTI_ALIAS_FLAG); corePaint.setStyle(Paint.Style.STROKE); corePaint.setStrokeWidth(6f); corePaint.setStrokeCap(Paint.Cap.ROUND); corePaint.setColor(Color.WHITE);
        }

        public void refreshAndAnimate() {
            if (safetyEngine != null) safetyEngine.reloadSettings();
            progress = 0f;
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(1000);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(animation -> { progress = (float) animation.getAnimatedValue(); invalidate(); });
            animator.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float width = getWidth(), centerY = getHeight() / 2, padding = 40f, segmentWidth = (width - (padding * 2)) / 24f;
            canvas.drawLine(padding, centerY, width - padding, centerY, bgPaint);
            int hoursToDraw = (int) (24 * progress);

            for (int i = 0; i < hoursToDraw; i++) {
                float startX = padding + (i * segmentWidth), endX = startX + segmentWidth;

                // Simülasyon
                double currentWind = simulateHourlyWind(i);
                double currentTemp = simulateHourlyTemp(i);
                double currentRain = dayRain > 0 ? (dayRain / 5) : 0;

                // Rengi Al
                int color = safetyEngine.analyzeHour(currentWind, (int) currentRain, currentTemp, i);

                glowPaint.setColor(color);
                // Gece saatleri karanlık görünsün ama renk net olsun
                glowPaint.setAlpha(safetyEngine.isNight(i) ? 100 : 200);
                canvas.drawLine(startX, centerY, endX, centerY, glowPaint);
                corePaint.setShadowLayer(5, 0, 0, color);
                canvas.drawLine(startX, centerY, endX, centerY, corePaint);
            }
            if (progress > 0.9f) {
                Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG); textPaint.setColor(Color.GRAY); textPaint.setTextSize(24); textPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("00:00", padding, centerY + 60, textPaint); canvas.drawText("12:00", width / 2, centerY + 60, textPaint); canvas.drawText("24:00", width - padding, centerY + 60, textPaint);
            }
        }
        private double simulateHourlyWind(int hour) { if (hour < 8 || hour > 20) return dayMaxWind * 0.5; if (hour >= 12 && hour <= 16) return dayMaxWind; return dayMaxWind * 0.75; }
        private double simulateHourlyTemp(int hour) { if (hour < 7) return dayMinTemp; if (hour > 12 && hour < 16) return dayMinTemp + 8; return dayMinTemp + 3; }



    }


}