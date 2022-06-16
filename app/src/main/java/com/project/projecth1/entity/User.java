//차상현 개발자
package com.project.projecth1.entity;

public class User {

    private String email;                   // 이메일 (아이디)
    private String password;                // 비밀번호

    private String name;                    // 이름
    private String birthDate;               // 생년월일
    private String gender;                  // 성별 (M/F)
    private double height;                  // 신장
    private double weight;                  // 체중

    private long joinTimeMillis;            // 가입일시를 millisecond 로 표현

    // 파이어 스토어를 사용하기 위해 필요한 생성자
    public User() {}

    public User(String email, String password, String name, String birthDate, String gender,
                double height, double weight, long joinTimeMillis) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.joinTimeMillis = joinTimeMillis;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public double getHeight() {
        return height;
    }

    public double getWeight() {
        return weight;
    }

    public long getJoinTimeMillis() {
        return joinTimeMillis;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
