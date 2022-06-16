//차상현 개발자, 이윤제 개발자
package com.project.projecth1.data;

import com.project.projecth1.entity.Sports;

import java.util.ArrayList;

public class SportsData {
    private volatile static SportsData _instance = null;

    private ArrayList<Sports> items;

    /* 싱글톤 패턴 적용 */
    public static SportsData getInstance() {
        if (_instance == null) {
            synchronized (SportsData.class) {
                if (_instance == null) {
                    _instance = new SportsData();
                }
            }
        }

        return _instance;
    }

    private SportsData() {
        // 초기화 (데이터 생성)
        init();
    }

    /* 초기화 (데이터 생성) */
    private void init() {
        this.items = new ArrayList<>();

        this.items.add(new Sports("런닝", 7.0));
        this.items.add(new Sports("축구", 7.0));
        this.items.add(new Sports("웨이트", 5.5));
        this.items.add(new Sports("자전거", 4.0));
        this.items.add(new Sports("수영", 6.0));
        this.items.add(new Sports("등산", 8.0));
        this.items.add(new Sports("골프", 3.5));
        this.items.add(new Sports("야구", 5.0));
        this.items.add(new Sports("줄넘기", 10.0));
        this.items.add(new Sports("에어로빅", 4.0));
    }

    public ArrayList<Sports> getItems() {
        return items;
    }

    /* 운동종목 목록 */
    public ArrayList<String> getNameList() {
        ArrayList<String> list = new ArrayList<>();

        for (Sports sports : this.items) {
            list.add(sports.getName());
        }

        return list;
    }

    /* MET 값 구하기 */
    public double getMet(String name) {
        for (Sports sports : this.items) {
            if (sports.getName().equals(name)) {
                return sports.getMet();
            }
        }

        return 0;
    }
}
