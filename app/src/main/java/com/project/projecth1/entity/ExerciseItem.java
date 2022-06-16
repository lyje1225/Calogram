//이윤제 개발자
package com.project.projecth1.entity;

public class ExerciseItem {

    public String id;                   // Exercise Doc ID
    public Exercise exercise;           // Exercise 객체

    public ExerciseItem(String id, Exercise exercise) {
        this.id = id;
        this.exercise = exercise;
    }
}
