//차상현 개발자, 이윤제 개발자
package com.project.projecth1.entity;

public class Sports {

    private String name;        // 운동종목
    private double met;         // METs

    public Sports(String name, double met) {
        this.name = name;
        this.met = met;
    }

    public String getName() {
        return name;
    }

    public double getMet() {
        return met;
    }
}
