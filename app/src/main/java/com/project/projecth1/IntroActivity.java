//이윤제 개발자, 곽민승 개발자
package com.project.projecth1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.project.projecth1.entity.User;
import com.project.projecth1.util.Constants;
import com.project.projecth1.util.GlobalVariable;
import com.project.projecth1.util.SharedPreferencesUtils;

import java.util.List;

public class IntroActivity extends AppCompatActivity {
    //private static final String TAG = IntroActivity.class.getSimpleName();
    private static final String TAG = "projecth1";

    private boolean executed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 툴바 안보이게 하기 위함
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_intro);

        this.executed = true;

        // 인트로 화면을 1초동안 보여주고 메인으로 이동
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            this.executed = false;

            // 권한체크
            checkPermission();
        }, Constants.LoadingDelay.LONG);
    }

    @Override
    public void onBackPressed() {
        if (this.executed) {
            return;
        }

        moveTaskToBack(true);
        finish();
    }

    /* 권한 체크 */
    private void checkPermission() {
        // 권한 체크
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener(){
                    /* 권한 여부를 다 묻고 실행되는 메소드 */
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        // check if all permissions are granted
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            // 모든 권한 허용
                            init();
                        } else {
                            // 거부한 권한수
                            Log.d(TAG, "Denied:" + multiplePermissionsReport.getDeniedPermissionResponses().size());
                            Toast.makeText(IntroActivity.this, R.string.permission_rationale_app_use, Toast.LENGTH_SHORT).show();
                        }
                    }

                    /* 이전 권한 여부를 거부한 권한이 있으면 실행되는 메소드 */
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        // list : 거부한 권한 이름이 리스트

                        // 권한 거부시 설정 다이얼로그 보여주기
                        showPermissionRationale(permissionToken);
                    }
                })
                .withErrorListener(dexterError -> {
                    // 권한설정 오류
                    Toast.makeText(this, R.string.msg_error, Toast.LENGTH_SHORT).show();
                })
                .check();
    }

    /* 만약 권한을 거절했을 경우, 다이얼로그 보여주기 */
    private void showPermissionRationale(PermissionToken token) {
        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.dialog_allow, (dialog, which) -> {
                    // 다시 권한요청 후 거부했을 경우 (onPermissionRationaleShouldBeShown) 메서드가 다시 실행 안됨 (권한 설정 못함)
                    // 어플리케이션 설정에서 직접 권한설정을 해야함
                    token.continuePermissionRequest();
                })
                .setNegativeButton(R.string.dialog_deny, (dialog, which) -> {
                    // 권한 요청 취소
                    token.cancelPermissionRequest();
                })
                .setCancelable(false)
                .setMessage(R.string.permission_rationale_app_use)
                .show();
    }

    /* 초기화 */
    private void init() {
        // 사용자 등록 Doc ID
        String id = SharedPreferencesUtils.getInstance(this).get(Constants.SharedPreferencesName.USER_DOCUMENT_ID);

        Log.d(TAG, "id: " + id);

        if (!TextUtils.isEmpty(id)) {
            // 사용자 Doc ID 가 있으면 자동 로그인
            login(id);
        } else {
            // 로그인 화면으로 이동
            goLogin();
        }
    }

    /* 로그인 */
    private void login(final String id) {
        // 파이어스토어 db
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // users 컬렉션에서 doc 얻기
        DocumentReference reference = db.collection(Constants.FirestoreCollectionName.USER).document(id);
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 성공
                DocumentSnapshot document = task.getResult();
                if (document != null) {
                    if (document.exists()) {
                        // 사용자 객체 생성
                        User user = document.toObject(User.class);

                        // 전역변수에 documentId 와 객체를 저장
                        GlobalVariable.documentId = document.getId();
                        GlobalVariable.user = user;

                        // 메인으로 이동
                        goMain();
                        return;
                    }
                }
            }

            // 로그인 화면으로 이동
            goLogin();
        });
    }

    /* 로그인화면으로 이동 */
    private void goLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        finish();
    }

    /* 메인화면으로 이동 */
    private void goMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }
}