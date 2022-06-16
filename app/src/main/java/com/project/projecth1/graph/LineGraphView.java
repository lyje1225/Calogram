//곽민승 개발자, 이윤제 개발자
package com.project.projecth1.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;

import java.util.ArrayList;

public class LineGraphView extends GraphView {
    private ArrayList<GraphData[]> items;

    private boolean isFirst = false;

    private ArrayList<String> labels;           // X축 라벨

    private int[] lineColor;

    private float pointSizeX = 0.0F;
    private float pointSizeY = 0.0F;

    private int valueMaxX = 0;
    private int valueMaxY = 0;

    private int divideCountX = 0;
    private int divideCountY = 0;

    private boolean graduationX = false;
    private boolean graduationY = false;

    private int lineCount;
    private int drawType = 2;

    // 선 두께는 1 ~ 5사이만 가능
    private int strokeWidth = 1;

    private static final int STROKE_WIDTH_MAX = 5;
    private static final float RAD_DATA_SIZE = 0.01F;

    public LineGraphView(Context context) {
        this(context, 1);
    }

    public LineGraphView(Context context, int lineCount) {
        super(context);
        this.lineCount = lineCount;
        init();
    }

    public LineGraphView(Context context, int lineCount, float leftGap, float rightGap, float topGap, float bottomGap) {
        super(context, leftGap, rightGap, topGap, bottomGap);
        this.lineCount = lineCount;
        init();
    }

    /* 초기값 설정 */
    private void init() {
        this.items = new ArrayList<>();

        this.lineColor = new int[this.lineCount];
        // 첫번째 데이터 색상 기본값 설정
        this.lineColor[0] = Color.parseColor("#00B0F0");
    }

    /* X축 MAX 값 및 단위  */
    public void setMaxX(int max, int divideCount, boolean graduation) {
        this.valueMaxX = max;

        this.divideCountX = divideCount;
        this.graduationX = graduation;
    }

    /* Y축 MAX 값 및 단위 */
    public void setMaxY(int max, int divideCount, boolean graduation) {
        this.valueMaxY = max;

        this.divideCountY = divideCount;
        this.graduationY = graduation;
    }

    public void setLabels(ArrayList<String> labels) {
        this.labels = labels;
    }

    /* 라인색상 설정 */
    public void setLineColor(int[] color) {
        this.lineColor = color;
    }

    /* 데이터 표시 타입 */
    public void setDrawType(int type) {
        // 0:점, 1:라인, 2:점 + 라인
        this.drawType = type;
    }

    /* 선 두께 설정 */
    public void setStrokeWidth(int width) {
        this.strokeWidth = width;

        // 선 두께는 1 ~ 5사이만 가능
        if (this.strokeWidth < 1) {
            this.strokeWidth = 1;
        } else if (this.strokeWidth > STROKE_WIDTH_MAX) {
            this.strokeWidth = STROKE_WIDTH_MAX;
        }
    }

    public void reDraw() {
        this.isFirst = true;
    }

    public void reDrawAll() {
        this.isFirst = true;
        this.background = null;
    }

    public void addData(ArrayList<GraphData[]> data) {
        this.items = data;
    }

    /* 데이터 표시 */
    private void drawData(Canvas canvas) {
        int rad = (int)(getWidth() * RAD_DATA_SIZE);

        int x1, y1;
        int[] x2 = new int[lineCount];
        int[] y2 = new int[lineCount];

        Paint pnt = new Paint();
        pnt.setStyle(Paint.Style.FILL);

        // 데이터 표시
        for (int i=0; i<this.items.size(); i++) {

            for (int j=0; j<this.items.get(i).length; j++) {
                pnt.setColor(this.lineColor[j]);

                GraphData data = this.items.get(i)[j];
                if (data.x <= this.valueMaxX && !TextUtils.isEmpty(this.labels.get(i))) {
                    x1 = this.baseX1  + (int)(data.x * this.pointSizeX);
                    y1 = this.baseY2 - (int)(data.y * this.pointSizeY);

                    // 점
                    if (this.drawType == 0 || this.drawType == 2) {
                        pnt.setStrokeWidth(1);
                        canvas.drawCircle(x1, y1, rad, pnt);
                    }

                    // 라인
                    if (this.drawType == 1 || this.drawType == 2) {
                        if (i > 0) {
                            pnt.setStrokeWidth(this.strokeWidth);
                            canvas.drawLine(x2[j], y2[j], x1, y1, pnt);
                        }
                    }

                    x2[j] = x1;
                    y2[j] = y1;
                }
            }
        }
    }

    /* 백그라운드 */
    private void buildBackLine() {
        int x1, y1;

        if (this.valueMaxX > 0 ) {
            this.pointSizeX = (this.sizeX / (float) this.valueMaxX);
        }

        if (this.valueMaxY > 0 ) {
            this.pointSizeY = (this.sizeY / (float) this.valueMaxY);
        }

        Canvas canvas = new Canvas(this.background);

        Paint pnt = new Paint();

        // 텍스트
        pnt.setStrokeWidth(1);
        pnt.setStyle(Paint.Style.FILL);
        pnt.setColor(Color.parseColor("#111111"));
        pnt.setTextSize(this.fontSize);

        // X축 시작점
        pnt.setTextAlign(Paint.Align.CENTER);
        x1 = this.baseX1 - (this.fontSize / 2);
        y1 = this.baseY2 + (int) (this.fontSize * LABEL_X_GAP);
        canvas.drawText(this.labels.get(0), x1, y1, pnt);

        // Y축 시작점
        pnt.setTextAlign(Paint.Align.RIGHT);
        x1 = this.baseX1 - (int) (this.fontSize / 1.5);
        y1 = this.baseY2 + (int) (this.fontSize * LABEL_Y_GAP);
        canvas.drawText("0", x1, y1, pnt);

        // X 표시
        if ((this.valueMaxX % this.divideCountX) == 0) {
            pnt.setTextAlign(Paint.Align.CENTER);

            for (int i = 1; i <= this.divideCountX; i++) {
                int x = this.baseX1 + (int)(((this.valueMaxX / this.divideCountX) * i) * this.pointSizeX);

                //눈금
                if (this.graduationX) {
                    canvas.drawLine(x, this.baseY1, x, this.baseY2, pnt);
                }

                // 라벨
                canvas.drawText(this.labels.get(i), x, this.baseY2 + (int) (this.fontSize * LABEL_X_GAP), pnt);
            }
        }

        // Y 표시
        if ((this.valueMaxY % this.divideCountY) == 0) {
            pnt.setTextAlign(Paint.Align.RIGHT);

            for (int i = 1; i <= this.divideCountY; i++) {
                int y = this.baseY2 - (int)(((this.valueMaxY / this.divideCountY) * i) * this.pointSizeY);

                //눈금
                if (this.graduationY) {
                    canvas.drawLine(this.baseX1, y, this.baseX2, y, pnt);
                }

                // 라벨
                String labelY = String.valueOf((this.valueMaxY / this.divideCountY) * i);
                canvas.drawText(labelY, this.baseX1 - (int) (this.fontSize / 1.5), y + (int) (this.fontSize * LABEL_Y_GAP), pnt);
            }
        }

        this.isFirst = false;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (this.isFirst) {
            buildBackLine();
            canvas.drawBitmap(this.background, 0, 0, null);
        }

        drawData(canvas);
    }
}
