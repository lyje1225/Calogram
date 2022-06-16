//곽민승 개발자, 이윤제 개발자
package com.project.projecth1.graph;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

public class GraphView extends View {
    protected Context context;
    protected Bitmap background;

    protected int backColor = Color.parseColor("#FFFFFF");

    protected int fontSize;

    protected int sizeX = 0;
    protected int sizeY = 0;

    protected String unitX = "";
    protected String unitY = "";

    protected int baseX1, baseY1, baseX2, baseY2;

    protected float leftGap, rightGap, topGap, bottomGap;

    protected static final float DEFAULT_LEFT_GAP = 0.1F;
    protected static final float DEFAULT_RIGHT_GAP = 0.1F;
    protected static final float DEFAULT_TOP_GAP = 0.09F;
    protected static final float DEFAULT_BOTTOM_GAP = 0.09F;

    protected static final float UNIT_X_GAP = 0.02F;
    protected static final float UNIT_Y_GAP = 0.03F;

    protected static final float LABEL_X_GAP = 1.2F;
    protected static final float LABEL_Y_GAP = 0.2F;

    protected static final float FONT_SIZE_RATE = 0.03F;

    private static final int FONT_SIZE_MIN = 24;
    private static final int FONT_SIZE_MAX = 32;

    public GraphView(Context context) {
        super(context);

        this.context = context;

        this.leftGap = DEFAULT_LEFT_GAP;
        this.rightGap = DEFAULT_RIGHT_GAP;
        this.topGap = DEFAULT_TOP_GAP;
        this.bottomGap = DEFAULT_BOTTOM_GAP;
    }

    public GraphView(Context context, float leftGap, float rightGap, float topGap, float bottomGap) {
        super(context);

        if (leftGap > 0) {
            this.leftGap = leftGap;
        } else {
            this.leftGap = DEFAULT_LEFT_GAP;
        }

        if (rightGap > 0) {
            this.rightGap = rightGap;
        } else {
            this.rightGap = DEFAULT_RIGHT_GAP;
        }

        if (topGap > 0) {
            this.topGap = topGap;
        } else {
            this.topGap = DEFAULT_TOP_GAP;
        }

        if (bottomGap > 0) {
            this.bottomGap = bottomGap;
        } else {
            this.bottomGap = DEFAULT_BOTTOM_GAP;
        }
    }

    private void setBasePosition() {
        this.baseX1 = (int) (getWidth() * this.leftGap);
        this.baseY1 = (int) (getHeight() * this.topGap);
        this.baseX2 = (int) (getWidth() * (1.0 - this.rightGap));
        this.baseY2 = (int) (getHeight() * (1.0 - this.bottomGap));
    }

    public void setBackColor(int color) {
        this.backColor = color;
    }

    public void setXUnit(String unit) {
        this.unitX = unit;
    }

    public void setYUnit(String unit) {
        this.unitY = unit;
    }

    private void buildBack() {
        int x1, y1, x2, y2;

        setBasePosition();

        if (getWidth() > getHeight()) {
            this.fontSize = (int) (getHeight() * FONT_SIZE_RATE);
        } else {
            this.fontSize = (int) (getWidth() * FONT_SIZE_RATE);
        }
        Log.d("FONT", "f: " + this.fontSize);

        if (FONT_SIZE_MIN > this.fontSize) {
            this.fontSize = FONT_SIZE_MIN;
        }

        if (FONT_SIZE_MAX < this.fontSize) {
            this.fontSize = FONT_SIZE_MAX;
        }

        this.background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(this.background);
        canvas.drawColor(this.backColor);

        Paint pnt = new Paint();
        pnt.setStrokeWidth(2);
        pnt.setColor(Color.parseColor("#111111"));
        pnt.setStyle(Paint.Style.STROKE);

        // 테두리
        //canvas.drawRect(0, 0, getWidth(), getHeight(), pnt);

        pnt.setStrokeWidth(1);

        canvas.drawLine(this.baseX1, this.baseY2, this.baseX2, this.baseY2, pnt);

        this.sizeX = this.baseX2 - this.baseX1 + 1;

        //canvas.drawLine(this.baseX1, this.baseY1, this.baseX1, this.baseY2, pnt);

        this.sizeY = this.baseY2 - this.baseY1 + 1;

        pnt.setStrokeWidth(1);
        pnt.setStyle(Paint.Style.FILL);
        pnt.setColor(Color.parseColor("#111111"));
        pnt.setTextSize(this.fontSize);

        if (!TextUtils.isEmpty(this.unitX)) {
            pnt.setTextAlign(Paint.Align.LEFT);

            x1 = (int) (getWidth() * (1.0 - this.rightGap + UNIT_X_GAP));
            y1 = this.baseY2 + (int) (this.fontSize * LABEL_X_GAP);
            canvas.drawText(this.unitX, x1, y1, pnt);
        }

        if (!TextUtils.isEmpty(this.unitY)) {
            pnt.setTextAlign(Paint.Align.RIGHT);

            x1 = this.baseX1 - (this.fontSize / 2);
            y1 = (int) (getHeight() * (this.topGap - UNIT_Y_GAP));
            canvas.drawText(this.unitY, x1, y1, pnt);
        }
    }

    public void onDraw(Canvas canvas) {
        if (this.background == null) buildBack();
        canvas.drawBitmap(this.background, 0, 0, null);
    }
}
