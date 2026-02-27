package com.canceylan.motoweather;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class AstroPathView extends View {

    // --- AYARLAR ---
    private static final float LINE_THICKNESS = 6f; // Çizgi kalınlığı
    private static final String COLOR_EMPTY_LINE = "#80FFFFFF";
    private static final String COLOR_FILLED_LINE = "#FFC107"; // Sarı
    private static final String COLOR_FILLED_NIGHT = "#B0C4DE"; // Mavi

    private Paint emptyPaint;
    private Paint filledPaint;
    private Paint backupCirclePaint; // Resim yoksa çizilecek top için
    private Path pathTrack;
    private Path pathProgress;
    private PathMeasure pathMeasure;

    private Bitmap iconSun;
    private Bitmap iconMoon;
    private Matrix matrix;
    private RectF arcBounds;

    private float currentProgress = 0f;
    private boolean isNightMode = false;

    public AstroPathView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 1. Kalemleri Hazırla
        emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyPaint.setStyle(Paint.Style.STROKE);
        emptyPaint.setStrokeWidth(LINE_THICKNESS);
        emptyPaint.setColor(Color.parseColor(COLOR_EMPTY_LINE));
        emptyPaint.setPathEffect(new DashPathEffect(new float[]{20f, 10f}, 0f));
        emptyPaint.setStrokeCap(Paint.Cap.ROUND);

        filledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        filledPaint.setStyle(Paint.Style.STROKE);
        filledPaint.setStrokeWidth(LINE_THICKNESS);
        filledPaint.setColor(Color.parseColor(COLOR_FILLED_LINE));
        filledPaint.setStrokeCap(Paint.Cap.ROUND);
        filledPaint.setShadowLayer(10f, 0f, 0f, Color.parseColor(COLOR_FILLED_LINE));

        // Resim yüklenemezse çizilecek yedek top
        backupCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backupCirclePaint.setStyle(Paint.Style.FILL);
        backupCirclePaint.setColor(Color.YELLOW);
        backupCirclePaint.setShadowLayer(10f, 0f, 0f, Color.YELLOW);

        pathTrack = new Path();
        pathProgress = new Path();
        pathMeasure = new PathMeasure();
        matrix = new Matrix();
        arcBounds = new RectF();

        // 2. RESİMLERİ GÜVENLİ YÜKLE
        // Senin attığın resimde "ic_sun" ve "ic_moon" vardı, onları kullanıyoruz
        try {
            iconSun = BitmapFactory.decodeResource(getResources(), R.drawable.ic_sun);
            iconMoon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_moon);

            // Eğer decodeResource null dönerse (dosya bozuksa vs.)
            if (iconSun == null) throw new Exception("Güneş resmi bulunamadı");

            // Boyutlandırma (Çok büyükse küçültelim)
            iconSun = Bitmap.createScaledBitmap(iconSun, 80, 80, true);

            if (iconMoon != null) {
                iconMoon = Bitmap.createScaledBitmap(iconMoon, 70, 70, true);
            } else {
                iconMoon = iconSun; // Ay yoksa gece de güneş kullan
            }

        } catch (Exception e) {
            // Hata olursa null bırak, onDraw'da kontrol edeceğiz (ÇÖKME ENGELLEYİCİ)
            System.out.println("AstroPathView Resim Hatası: " + e.getMessage());
            iconSun = null;
            iconMoon = null;
        }

        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setupArcPath(w, h);
    }

    private void setupArcPath(int w, int h) {
        pathTrack.reset();
        // Kenarlardan boşluk bırakalım
        float padding = 40f;
        // Yayı oluşturacak kutuyu belirliyoruz
        arcBounds.set(padding, h * 0.2f, w - padding, h * 1.5f);
        pathTrack.addArc(arcBounds, 180f, 180f);
        pathMeasure.setPath(pathTrack, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (pathTrack.isEmpty()) return;

        // Arka Plan Çizgisi
        canvas.drawPath(pathTrack, emptyPaint);

        // İlerleme Çizgisi
        float length = pathMeasure.getLength();
        float distance = length * currentProgress;

        pathProgress.reset();
        pathMeasure.getSegment(0f, distance, pathProgress, true);

        // Gece/Gündüz Renk Ayarı
        filledPaint.setColor(Color.parseColor(isNightMode ? COLOR_FILLED_NIGHT : COLOR_FILLED_LINE));
        filledPaint.setShadowLayer(10f, 0f, 0f, filledPaint.getColor());

        canvas.drawPath(pathProgress, filledPaint);

        // İkon Konumu Hesapla
        float[] pos = new float[2];
        float[] tan = new float[2];
        pathMeasure.getPosTan(distance, pos, tan);

        // Hangi ikonu çizeceğiz?
        Bitmap icon = isNightMode ? iconMoon : iconSun;

        if (icon != null) {
            // Resim varsa resmi çiz
            matrix.reset();
            matrix.postTranslate(pos[0] - icon.getWidth() / 2f, pos[1] - icon.getHeight() / 2f);
            canvas.drawBitmap(icon, matrix, null);
        } else {
            // !!! KRİTİK NOKTA: Resim yoksa SARI TOP çiz (Çökmemesi için) !!!
            backupCirclePaint.setColor(isNightMode ? Color.LTGRAY : Color.YELLOW);
            canvas.drawCircle(pos[0], pos[1], 20f, backupCirclePaint);
        }
    }

    public void setProgress(float progress) {
        this.currentProgress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    public void setNightMode(boolean isNight) {
        this.isNightMode = isNight;
        invalidate();
    }
}