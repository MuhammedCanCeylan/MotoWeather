package com.canceylan.motoweather;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class RiskTimelineView extends View {

    private Paint paint;
    private RectF rectF;
    private int[] colors; // 24 saatin renkleri
    private float[] positions; // Renklerin konumları

    public RiskTimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectF = new RectF();
        // Varsayılan: Hepsi Yeşil (Veri gelmezse)
        colors = new int[]{0xFF4CAF50, 0xFF4CAF50};
        positions = new float[]{0f, 1f};
    }

    // 24 Saatin renklerini buraya gönderiyoruz
    public void setRiskData(int[] hourColors) {
        if (hourColors == null || hourColors.length < 2) return;

        this.colors = hourColors;
        this.positions = new float[hourColors.length];

        // 0.0 ile 1.0 arasına 24 saati yayıyoruz
        for (int i = 0; i < hourColors.length; i++) {
            positions[i] = (float) i / (hourColors.length - 1);
        }

        invalidate(); // Yeniden çiz
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int radius = 20; // Köşe yuvarlaklığı

        rectF.set(0, 0, width, height);

        // Olayın kalbi burası: LinearGradient ile pürüzsüz geçiş
        if (width > 0 && height > 0) {
            Shader shader = new LinearGradient(0, 0, width, 0, colors, positions, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            canvas.drawRoundRect(rectF, radius, radius, paint);
        }
    }
}