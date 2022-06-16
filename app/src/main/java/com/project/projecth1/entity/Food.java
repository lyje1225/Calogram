//장웅희 개발자, 차상현 개발자
package com.project.projecth1.entity;

import java.util.ArrayList;

public class Food {

    private int mealKind;                   // 0아침/1점심/2저녁
    private String foodName;                // 음식명
    private int foodCount;                  // 수량
    private int calorie;                    // 1개당 칼로리
    private ArrayList<String> nutrients;    // 영양정보 (@로 구분) 탄수화물@60000 수치값은 (mg)으로 저장
    private String date;                    // 등록일

    private long inputTimeMillis;           // 등록일시를 millisecond 로 표현 (정렬에 사용)

    // 파이어 스토어를 사용하기 위해 필요한 생성자
    public Food() {}

    public Food(int mealKind, String foodName, int foodCount, int calorie, ArrayList<String> nutrients,
                    String date, long inputTimeMillis) {
        this.mealKind = mealKind;
        this.foodName = foodName;
        this.foodCount = foodCount;
        this.calorie = calorie;
        this.nutrients = nutrients;
        this.date = date;
        this.inputTimeMillis = inputTimeMillis;
    }

    public int getMealKind() {
        return mealKind;
    }

    public String getFoodName() {
        return foodName;
    }

    public int getFoodCount() {
        return foodCount;
    }

    public int getCalorie() {
        return calorie;
    }

    public ArrayList<String> getNutrients() {
        return nutrients;
    }

    public String getDate() {
        return date;
    }

    public long getInputTimeMillis() {
        return inputTimeMillis;
    }

    public void setMealKind(int mealKind) {
        this.mealKind = mealKind;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public void setFoodCount(int foodCount) {
        this.foodCount = foodCount;
    }

    public void setCalorie(int calorie) {
        this.calorie = calorie;
    }

    public void setNutrients(ArrayList<String> nutrients) {
        this.nutrients = nutrients;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setInputTimeMillis(long inputTimeMillis) {
        this.inputTimeMillis = inputTimeMillis;
    }
}
