//곽민승 개발자
package com.project.projecth1.entity;

public class NutrientGraphItem {

    private String name;        // 영양성분
    private int percent;        // 퍼센트 (소수점 1자리에서 *10) 45.9 => 459
    private int color;          // 그래프 색상

    public NutrientGraphItem(String name, int percent, int color) {
        this.name = name;
        this.percent = percent;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public int getPercent() {
        return percent;
    }

    public int getColor() {
        return color;
    }
}
