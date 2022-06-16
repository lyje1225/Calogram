//장웅희 개발자
package com.project.projecth1.entity;

public class FoodItem {

    public String id;                   // Food Doc ID
    public Food food;                   // Food 객체

    public FoodItem(String id, Food food) {
        this.id = id;
        this.food = food;
    }
}
