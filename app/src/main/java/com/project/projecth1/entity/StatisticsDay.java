//차상현 개발자
package com.project.projecth1.entity;

public class StatisticsDay {

    public int day;                 // 일
    public int foodCalorie;         // 섭취한 칼로리
    public int exerciseCalorie;     // 운동 칼로리

    public StatisticsDay(int day, int foodCalorie, int exerciseCalorie) {
        this.day = day;
        this.foodCalorie = foodCalorie;
        this.exerciseCalorie = exerciseCalorie;
    }
}
