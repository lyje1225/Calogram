//장웅희 개발자
package com.project.projecth1.entity;

public class Nutrient {

    private String name;        // 영양성분
    private long value;         // 내용량 (mg)

    public Nutrient(String name, long value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public long getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
