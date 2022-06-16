package com.project.projecth1.util;

public class Constants {

    /* 구글 클라우드 비전 api 관련 상수 */
    public static class GoogleCloudVisionApi {
        public static final String API_KEY = "AIzaSyDq43EKm58wcU8lkt-2sBMDMmo_w6eNqeA";
        public static final String ANDROID_CERT_HEADER = "X-Android-Cert";
        public static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
        public static final int MAX_TEXT_RESULTS = 10;
    }

    /* SharedPreferences 관련 상수 */
    public static class SharedPreferencesName {
        public static final String USER_DOCUMENT_ID = "user_document_id";   // 사용자 Fire store Document ID
    }

    /* 액티비티에서 프레그먼트에 요청할 작업 종류 */
    public static class FragmentTaskKind {
        public static final int REFRESH = 0;                    // 새로고침
    }

    /* Fire store Collection 이름 */
    public static class FirestoreCollectionName {
        public static final String USER = "users";              // 사용자
        public static final String FOOD = "foods";              // 섭취한 음식
        public static final String EXERCISE = "exercise";       // 운동
    }

    /* 성별 */
    public static class Gender {
        public static final String MALE = "M";
        public static final String FEMALE = "F";
    }

    /* 식사시기 */
    public static class MealKind {
        public static final int BREAKFAST = 0;
        public static final int LUNCH = 1;
        public static final int DINNER = 2;
    }

    /* 로딩 딜레이 */
    public static class LoadingDelay {
        public static final int SHORT = 300;
        public static final int LONG = 1000;
    }
}
