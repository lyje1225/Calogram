//장웅희 개발자, 차상현 개발자
package com.project.projecth1.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.project.projecth1.FoodAddActivity;
import com.project.projecth1.R;
import com.project.projecth1.adapter.MyFragmentStateAdapter;
import com.project.projecth1.entity.Food;
import com.project.projecth1.fragment.abstracts.IFragment;
import com.project.projecth1.fragment.abstracts.ITaskFragment;
import com.project.projecth1.popupwindow.FoodAddPopup;
import com.project.projecth1.util.Constants;
import com.project.projecth1.util.GlobalVariable;
import com.project.projecth1.util.PackageManagerUtils;
import com.project.projecth1.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalorieFragment extends Fragment implements IFragment {
    private static final String TAG = CalorieFragment.class.getSimpleName();
    //private static final String TAG = "projecth1";

    private Context context;

    private ProgressDialog progressDialog;          // 로딩 dialog

    private ArrayList<Fragment> fragments;

    private ViewPager2 viewPager;
    private TextView txtDate;

    private Uri photoContentUri;                // 카메라 호출후 변수값이 초기화 되는 현상 때문에 변수 선언
    private Calendar calendar;
    private int pagePosition = 1;               // 디폴트 포지션

    private Food food;                          // 섭취한 음식 정보

    private static final int PAGE_MIDDLE = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calorie, container, false);

        // 로딩 dialog
        this.progressDialog = new ProgressDialog(this.context);
        this.progressDialog.setMessage("처리중...");
        this.progressDialog.setCancelable(false);

        this.viewPager = view.findViewById(R.id.viewPager);

        // 유지되는 페이지수를 설정
        // (3개의 페이지를 초반에 미리로딩한다. 페이지를 이동할때 마다 View 를 지우고 새로만드는 작업은 하지않게 된다)
        this.viewPager.setOffscreenPageLimit(3);

        // day 3개를 생성 (이전일, 현재일, 다음일)
        this.fragments = new ArrayList<>();
        for (int i=0; i<3; i++) {
            Fragment fragment = new CalorieDayFragment();
            // 현재 위치값 전달
            Bundle bundle = new Bundle();
            bundle.putInt("position", i);
            fragment.setArguments(bundle);
            this.fragments.add(fragment);
        }

        MyFragmentStateAdapter adapter = new MyFragmentStateAdapter(this, this.fragments);
        this.viewPager.setAdapter(adapter);

        this.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    // 스크롤이 정지되어 있는 상태
                    if (pagePosition < PAGE_MIDDLE) {
                        // 이전일
                        prevDay();
                    } else if (pagePosition > PAGE_MIDDLE) {
                        // 다음일
                        nextDay();
                    } else {
                        return;
                    }

                    // 페이지를 다시 가운데로 맞춘다 (3페이지로 계속 이전 / 다음 할 수 있게 하기위함)
                    viewPager.setCurrentItem(PAGE_MIDDLE, false);

                    // 일 만들기
                    Bundle bundle = new Bundle();
                    bundle.putLong("time_millis", calendar.getTimeInMillis());
                    ((ITaskFragment) fragments.get(PAGE_MIDDLE)).task(Constants.FragmentTaskKind.REFRESH, bundle);
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                pagePosition = position;
            }
        });

        this.txtDate = view.findViewById(R.id.txtDate);

        // 현재일
        this.calendar = Calendar.getInstance();
        this.txtDate.setText(Utils.getDate("yyyy-MM-dd", this.calendar.getTimeInMillis()));

        view.findViewById(R.id.imgPrev).setOnClickListener(view1 -> {
            // 이전일
            this.viewPager.setCurrentItem(PAGE_MIDDLE - 1, true);
        });

        view.findViewById(R.id.imgNext).setOnClickListener(view1 -> {
            // 다음일
            this.viewPager.setCurrentItem(PAGE_MIDDLE + 1, true);
        });

        view.findViewById(R.id.fabAdd).setOnClickListener(view12 -> {
            // 섭취한 음식 추가
            onPopupFoodAdd();
        });

        this.viewPager.setCurrentItem(PAGE_MIDDLE, false);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        this.context = null;
        super.onDetach();
    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    /* 이전일 */
    private void prevDay() {
        this.calendar.add(Calendar.DATE, -1);
        this.txtDate.setText(Utils.getDate("yyyy-MM-dd", this.calendar.getTimeInMillis()));
    }

    /* 다음일 */
    private void nextDay() {
        this.calendar.add(Calendar.DATE, 1);
        this.txtDate.setText(Utils.getDate("yyyy-MM-dd", this.calendar.getTimeInMillis()));
    }

    /* 섭취한 음식 등록 팝업창 호출 */
    private void onPopupFoodAdd() {
        View popupView = View.inflate(this.context, R.layout.popup_food_add, null);
        FoodAddPopup popup = new FoodAddPopup(popupView, (view, bundle) -> {
            // 섭취한 음식 정보
            this.food = new Food();
            this.food.setMealKind(bundle.getInt("meal_kind"));          // 식사시기
            this.food.setFoodName(bundle.getString("food_name"));       // 음식명
            this.food.setFoodCount(bundle.getInt("food_count"));        // 수량
            this.food.setDate(this.txtDate.getText().toString());       // 등록일

            if (view.getId() == R.id.btnCamera) {
                // 카메라 호출
                this.photoContentUri = Utils.goCamera(cameraActivityLauncher, this.context, "/projecth1", "OCR_", ".jpg");
                if (this.photoContentUri == null) {
                    // 사용가능한 카메라 앱이 없음
                    Toast.makeText(this.context, R.string.msg_camera_app_empty, Toast.LENGTH_SHORT).show();
                }
            } else {
                // 직접 등록
                this.food.setCalorie(0);
                this.food.setNutrients(new ArrayList<>());

                // 전역변수에 설정
                GlobalVariable.food = this.food;

                // 음식 등록 activity 호출
                Intent intent = new Intent(this.context, FoodAddActivity.class);
                this.foodActivityLauncher.launch(intent);
            }
        });

        // Back 키 눌렸을때 닫기 위함
        popup.setFocusable(true);
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    /* 구글 클라우드 비전 사용하기 */
    private void callCloudVision(Bitmap bitmap) {
        try {
            // text detection
            AsyncTask<Object, Void, String> textDetectionTask = new TextDetectionTask(prepareAnnotationRequest(bitmap));
            textDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "error:" + e.getMessage());
            this.progressDialog.dismiss();
            Toast.makeText(this.context, R.string.msg_error, Toast.LENGTH_SHORT).show();
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(final Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(Constants.GoogleCloudVisionApi.API_KEY) {
                    /**
                     * 중요한 식별 필드를 HTTP 에 삽입할 수 있도록 이를 재정의합니다.
                     * 이를 통해 제한된 클라우드 플랫폼 API 키를 사용할 수 있습니다.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = context.getPackageName();
                        visionRequest.getRequestHeaders().set(Constants.GoogleCloudVisionApi.ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(context.getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(Constants.GoogleCloudVisionApi.ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature textDetection = new Feature();
                textDetection.setType("TEXT_DETECTION");
                textDetection.setMaxResults(Constants.GoogleCloudVisionApi.MAX_TEXT_RESULTS);
                add(textDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    /* TextDetection 응답 값을 string 으로 변환 */
    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message;

        List<EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations();
        if (labels != null) {
            message = labels.get(0).getDescription();
        } else {
            message = "";
        }

        return message;
    }

    /* 영양소 추출 */
    private void detectNutrient(String detectedText) {
        ArrayList<String> nutrients = new ArrayList<>();
        int totalCalorie = -1;

        String[] texts = detectedText.split("\n");
        for (String text : texts) {
            text = text.replace(" ", "");       // 공백 없애기
            //Log.d(TAG, "결과값:" + text);

            // 영양소 체크
            if (Utils.isNutrient(text)) {
                Log.d(TAG, "추출:" + text);

                // 수치값 추출
                int position = -1;
                for (int i = 0; i<text.length(); i++) {
                    String tag = String.valueOf(text.charAt(i));
                    // 숫자이면 해당위치 구하기
                    if (Utils.isNumeric(tag)) {
                        position = i;
                        break;
                    }
                }

                if (position > -1) {
                    String name = text.substring(0, position);
                    String value = text.substring(position);

                    Log.d(TAG, "영양소:" + name);
                    Log.d(TAG, "수치:" + value);

                    // @로 구분 영양소명@수치값(mg)
                    String nutrient = name + "@";
                    if (value.contains("mg")) {
                        // mg
                        nutrient += value.replace("mg", "");
                    } else {
                        // g
                        long v = (long) (Double.parseDouble(value.replace("g", "")) * 1000);
                        nutrient += String.valueOf(v);
                    }

                    nutrients.add(nutrient);
                }
            }

            // 총 kcal 구하기
            if (text.contains("kcal")) {
                int position = text.indexOf("kcal");
                StringBuilder kcal = new StringBuilder();
                // kcal 이전 데이터를 확인하여 kcal 수치값을 추출함
                for (int i = position-1; i>=0; i--) {
                    String value = String.valueOf(text.charAt(i));
                    if (Utils.isNumeric(value)) {
                        kcal.insert(0, value);
                    } else {
                        // 콤마(,)는 무시
                        if (!value.equals(",")) {
                            break;
                        }
                    }
                }
                Log.d(TAG, "kcal:" + kcal.toString());

                if (Utils.isNumeric(kcal.toString())) {
                    int calorie = Integer.parseInt(kcal.toString());
                    if (totalCalorie == -1) {
                        totalCalorie = calorie;
                    } else {
                        // kcal 적은 값을 적용함
                        if (totalCalorie > calorie) {
                            totalCalorie = calorie;
                        }
                    }
                }
            }
        }

        // 총 kcal 값을 추출하지 못했으면
        if (totalCalorie == -1) {
            totalCalorie = 0;
        }

        // 섭취한 음식정보에 적용
        this.food.setCalorie(totalCalorie);
        this.food.setNutrients(nutrients);

        // 전역변수에 설정
        GlobalVariable.food = this.food;

        // 음식 등록 activity 호출
        Intent intent = new Intent(this.context, FoodAddActivity.class);
        this.foodActivityLauncher.launch(intent);
    }

    /* 카메라 ActivityForResult */
    private final ActivityResultLauncher<Intent> cameraActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (this.photoContentUri == null) {
                        return;
                    }

                    Log.d(TAG, "camera ok");

                    this.progressDialog.show();

                    Bitmap bitmap;
                    try {
                        // URI 에서 비트맵 얻기
                        bitmap = Utils.getBitmapFromUri(this.context, this.photoContentUri, 1024, 1024);
                    } catch (IOException e) {
                        // 오류
                        this.progressDialog.dismiss();
                        Toast.makeText(this.context, R.string.msg_error, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 이미지 회전 각도 구하기
                    int degree = Utils.getExifOrientation(this.context, this.photoContentUri);

                    if (degree != 0) {
                        // 회전 하기
                        bitmap = Utils.rotateBitmap(bitmap, degree);
                        Log.d(TAG, "degree:" + degree);
                    }

                    // 구글 클라우드 비전 사용하기
                    callCloudVision(bitmap);
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    // 카메라 취소이면
                    this.progressDialog.dismiss();

                    if (photoContentUri == null) {
                        return;
                    }

                    Log.d(TAG, "cancel camera");

                    // 저장할 공간을 생성한 파일 삭제
                    this.context.getContentResolver().delete(this.photoContentUri, null, null);
                }
            });

    /* 섭취한 음식등록 ActivityForResult */
    private final ActivityResultLauncher<Intent> foodActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // 음식 등록 후

                    this.food = null;

                    // 일 만들기 (새로고침)
                    Bundle bundle = new Bundle();
                    bundle.putLong("time_millis", this.calendar.getTimeInMillis());
                    ((ITaskFragment) this.fragments.get(PAGE_MIDDLE)).task(Constants.FragmentTaskKind.REFRESH, bundle);
                }
            });

    /* 텍스트 추출 AsyncTask */
    private class TextDetectionTask extends AsyncTask<Object, Void, String> {
        private final Vision.Images.Annotate request;

        private TextDetectionTask(Vision.Images.Annotate annotate) {
            this.request = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                BatchAnnotateImagesResponse response = this.request.execute();

                // TextDetection 응답 값을 string 으로 변환
                return convertResponseToString(response);
            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }

            return "";
        }

        @Override
        protected void onPostExecute(String data) {
            //Log.e(TAG, "구글비전 API 요청결과" + data);

            progressDialog.dismiss();

            if (TextUtils.isEmpty(data)) {
                Toast.makeText(context, R.string.msg_text_detection_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            String detectedText = data.trim();
            Log.d(TAG, "이미지인식결과값:" + detectedText);

            // 영양소 추출
            detectNutrient(detectedText);
        }
    }
}
