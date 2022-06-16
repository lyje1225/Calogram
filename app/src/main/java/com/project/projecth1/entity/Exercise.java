//이윤제 개발자
package com.project.projecth1.entity;

public class Exercise {

    private String sports;                  // 운동종목
    private int exerciseTime;               // 운동시간 (분)
    private double met;                     // METs
    private double weight;                  // 운동 당시의 몸무게
    private int calorie;                    // 칼로리
    private String date;                    // 등록일

    private long inputTimeMillis;           // 등록일시를 millisecond 로 표현 (정렬에 사용)

    // 파이어 스토어를 사용하기 위해 필요한 생성자
    public Exercise() {}

    public Exercise(String sports, int exerciseTime, double met, double weight, int calorie,
                    String date, long inputTimeMillis) {
        this.sports = sports;
        this.exerciseTime = exerciseTime;
        this.met = met;
        this.weight = weight;
        this.calorie = calorie;
        this.date = date;
        this.inputTimeMillis = inputTimeMillis;
    }

    public String getSports() {
        return sports;
    }

    public int getExerciseTime() {
        return exerciseTime;
    }

    public double getMet() {
        return met;
    }

    public double getWeight() {
        return weight;
    }

    public String getDate() {
        return date;
    }

    public int getCalorie() {
        return calorie;
    }

    public long getInputTimeMillis() {
        return inputTimeMillis;
    }
}
