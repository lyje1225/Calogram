//이윤제 개발자, 차상현 개발자
package com.project.projecth1;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.project.projecth1.entity.User;
import com.project.projecth1.util.Constants;
import com.project.projecth1.util.GlobalVariable;
import com.project.projecth1.util.SharedPreferencesUtils;

public class LoginActivity extends AppCompatActivity {
    //private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String TAG = "projecth1";

    private ProgressDialog progressDialog;          // 로딩 dialog

    private EditText editEmail, editPassword;

    private InputMethodManager imm;             // 키보드를 숨기기 위해 필요함

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 툴바 안보이게 하기 위함
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        this.editEmail = findViewById(R.id.editEmail);
        this.editPassword = findViewById(R.id.editPassword);

        // 로딩 dialog
        this.progressDialog = new ProgressDialog(this);
        this.progressDialog.setMessage("처리중...");
        this.progressDialog.setCancelable(false);

        // 키보드를 숨기기 위해 필요함
        this.imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        findViewById(R.id.btnOk).setOnClickListener(view -> {
            // 로그인
            if (checkData()) {
                this.progressDialog.show();

                // 로딩 dialog 를 표시하기 위해 딜레이를 줌
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // 로그인
                    login();
                }, Constants.LoadingDelay.SHORT);
            }
        });

        findViewById(R.id.txtSignUp).setOnClickListener(view -> {
            // 회원가입으로 이동
            goJoin();
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        finish();
    }

    /* 입력 데이터 체크 */
    private boolean checkData() {
        // 이메일 입력 체크
        String email = this.editEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, R.string.msg_email_check_empty, Toast.LENGTH_SHORT).show();
            this.editEmail.requestFocus();
            return false;
        }

        // 비밀번호 입력 체크
        String password = this.editPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.msg_password_check_empty, Toast.LENGTH_SHORT).show();
            this.editPassword.requestFocus();
            return false;
        }

        // 키보드 숨기기
        this.imm.hideSoftInputFromWindow(this.editPassword.getWindowToken(), 0);

        return true;
    }

    /* 로그인 */
    private void login() {
        String email = this.editEmail.getText().toString();
        final String password = this.editPassword.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reference = db.collection(Constants.FirestoreCollectionName.USER);

        // 로그인
        Query query = reference.whereEqualTo("email", email).limit(1);
        query.get().addOnCompleteListener(task -> {
            this.progressDialog.dismiss();

            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    if (task.getResult().size() == 0) {
                        // 로그인 실패 (회원이 아님)
                        Toast.makeText(this, R.string.msg_login_user_none, Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            User user = document.toObject(User.class);
                            if (user.getPassword().equals(password)) {
                                // 로그인 성공

                                // 전역변수에 documentId 와 객체를 저장
                                GlobalVariable.documentId = document.getId();
                                GlobalVariable.user = user;

                                // SharedPreferences 에 록그인 정보 저장 (자동 로그인 기능)
                                SharedPreferencesUtils.getInstance(LoginActivity.this)
                                        .put(Constants.SharedPreferencesName.USER_DOCUMENT_ID, GlobalVariable.documentId);

                                // 메인 화면으로 이동
                                goMain();
                            } else {
                                // 로그인 실패 (비밀번호 틀림)
                                Toast.makeText(this, R.string.msg_login_password_wrong, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                    }
                } else {
                    // 오류
                    Toast.makeText(this, R.string.msg_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                // 오류
                Toast.makeText(this, R.string.msg_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* 회원가입화면으로 이동 */
    private void goJoin() {
        Intent intent = new Intent(this, JoinActivity.class);
        this.activityLauncher.launch(intent);
    }

    /* 메인화면으로 이동 */
    private void goMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    /* 회원가입 ActivityForResult */
    private final ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null) {
                        Log.d(TAG, "오류");
                        return;
                    }

                    this.editEmail.setText(data.getStringExtra("email"));
                    this.editPassword.setText(data.getStringExtra("password"));

                    this.progressDialog.show();
                    // 로딩 dialog 표시하기 위해 딜레이를 줌
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // 로그인
                        login();
                    }, Constants.LoadingDelay.SHORT);
                }
            });
}