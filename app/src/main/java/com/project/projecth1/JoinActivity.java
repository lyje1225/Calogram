//차상현 개발자, 장웅희 개발자
package com.project.projecth1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.project.projecth1.entity.User;
import com.project.projecth1.util.Constants;
import com.project.projecth1.util.Utils;

public class JoinActivity extends AppCompatActivity {
    //private static final String TAG = JoinActivity.class.getSimpleName();
    private static final String TAG = "projecth1";

    private ProgressDialog progressDialog;          // 로딩 dialog

    private EditText editEmail, editName, editPassword1, editPassword2;
    private EditText editBirthDate, editHeight, editWeight;

    private String gender = "";                 // 성별 (M:남, F:여)

    private InputMethodManager imm;             // 키보드를 숨기기 위해 필요함

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 툴바 안보이게 하기 위함
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_join);

        // 로딩 dialog
        this.progressDialog = new ProgressDialog(this);
        this.progressDialog.setMessage("처리중...");
        this.progressDialog.setCancelable(false);

        this.editEmail = findViewById(R.id.editEmail);
        this.editName = findViewById(R.id.editName);
        this.editPassword1 = findViewById(R.id.editPassword1);
        this.editPassword2 = findViewById(R.id.editPassword2);

        this.editBirthDate = findViewById(R.id.editBirthDate);
        this.editHeight = findViewById(R.id.editHeight);
        this.editWeight = findViewById(R.id.editWeight);

        // 성별 선택 리스너 등록
        ((RadioGroup) findViewById(R.id.rdgGender)).setOnCheckedChangeListener(mCheckedChangeListener);

        // 키보드를 숨기기 위해 필요함
        this.imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        findViewById(R.id.btnOk).setOnClickListener(view -> {
            // 가입
            if (checkData()) {
                this.progressDialog.show();

                // 로딩 dialog 를 표시하기 위해 딜레이를 줌
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // 이메일 중복체크 후 가입
                    join();
                }, Constants.LoadingDelay.SHORT);
            }
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

        // 이메일 유효성 체크
        if (!Utils.isEmail(email)) {
            Toast.makeText(this, R.string.msg_email_check_wrong, Toast.LENGTH_SHORT).show();
            this.editEmail.requestFocus();
            return false;
        }

        // 이름 입력 체크
        String name = this.editName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.msg_user_name_check_empty, Toast.LENGTH_SHORT).show();
            this.editName.requestFocus();
            return false;
        }

        // 비밀번호 입력 체크
        String password1 = this.editPassword1.getText().toString();
        if (TextUtils.isEmpty(password1)) {
            Toast.makeText(this, R.string.msg_password_check_empty, Toast.LENGTH_SHORT).show();
            this.editPassword1.requestFocus();
            return false;
        }

        // 비밀번호 확인 체크
        String password2 = this.editPassword2.getText().toString();
        if (!password1.equals(password2)) {
            Toast.makeText(this, R.string.msg_password_check_confirm, Toast.LENGTH_SHORT).show();
            this.editPassword2.requestFocus();
            return false;
        }

        // 성별 체크
        if (TextUtils.isEmpty(this.gender)) {
            Toast.makeText(this, R.string.msg_gender_check_empty, Toast.LENGTH_SHORT).show();
            return false;
        }

        // 생년월일 체크
        String birthDate = this.editBirthDate.getText().toString();
        if (TextUtils.isEmpty(birthDate)) {
            Toast.makeText(this, R.string.msg_birth_date_check_empty, Toast.LENGTH_SHORT).show();
            this.editBirthDate.requestFocus();
            return false;
        }

        // 자리수 체크 (1950104 날자체크(true 로 반환되기 때문에 자리수 체크함))
        if (birthDate.length() != 8) {
            Toast.makeText(this, R.string.msg_birth_date_check_wrong, Toast.LENGTH_SHORT).show();
            this.editBirthDate.requestFocus();
            return false;
        }

        // 생년월일 유효성 체크
        if (!Utils.isDate(birthDate, "yyyyMMdd")) {
            Toast.makeText(this, R.string.msg_birth_date_check_wrong, Toast.LENGTH_SHORT).show();
            this.editBirthDate.requestFocus();
            return false;
        }

        // 키 입력 체크
        String height = this.editHeight.getText().toString();
        if (TextUtils.isEmpty(height)) {
            Toast.makeText(this, R.string.msg_user_height_check_empty, Toast.LENGTH_SHORT).show();
            this.editHeight.requestFocus();
            return false;
        }

        if (!Utils.isNumeric(height)) {
            Toast.makeText(this, R.string.msg_user_height_check_wrong, Toast.LENGTH_SHORT).show();
            this.editHeight.requestFocus();
            return false;
        }

        // 몸무게 입력 체크
        String weight = this.editWeight.getText().toString();
        if (TextUtils.isEmpty(weight)) {
            Toast.makeText(this, R.string.msg_user_weight_check_empty, Toast.LENGTH_SHORT).show();
            this.editWeight.requestFocus();
            return false;
        }

        if (!Utils.isNumeric(weight)) {
            Toast.makeText(this, R.string.msg_user_weight_check_wrong, Toast.LENGTH_SHORT).show();
            this.editWeight.requestFocus();
            return false;
        }

        // 키보드 숨기기
        this.imm.hideSoftInputFromWindow(this.editPassword2.getWindowToken(), 0);

        return true;
    }

    /* 회원가입 */
    private void join() {
        String email = this.editEmail.getText().toString();
        String name = this.editName.getText().toString();
        String birthDate = this.editBirthDate.getText().toString();
        String heightStr = this.editHeight.getText().toString();
        String weightStr = this.editWeight.getText().toString();
        String password = this.editPassword1.getText().toString();

        // 회원정보
        final User user = new User(email, password, name, birthDate, this.gender,
                Double.parseDouble(heightStr), Double.parseDouble(weightStr), System.currentTimeMillis());

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reference = db.collection(Constants.FirestoreCollectionName.USER);

        // 이메일 중복 체크
        Query query = reference.whereEqualTo("email", email);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    if (task.getResult().size() == 0) {
                        // 이메일 중복 아님

                        // 회원가입 하기 (자동 문서 ID 값 생성 (컬렉션에 add 하면 document 가 자동 생성됨))
                        db.collection(Constants.FirestoreCollectionName.USER)
                                .add(user)
                                .addOnSuccessListener(documentReference -> {
                                    // 성공
                                    this.progressDialog.dismiss();

                                    // 로그인 Activity 에 전달 (바로 로그인 되게 하기 위함)
                                    Intent intent = new Intent();
                                    intent.putExtra("email", user.getEmail());
                                    intent.putExtra("password", user.getPassword());
                                    setResult(Activity.RESULT_OK, intent);

                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // 회원가입 실패
                                    this.progressDialog.dismiss();
                                    Toast.makeText(this, R.string.msg_error, Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // 이메일 중복
                        this.progressDialog.dismiss();
                        Toast.makeText(this, R.string.msg_email_check_overlap, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    this.progressDialog.dismiss();
                    Toast.makeText(this, R.string.msg_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                // 오류
                this.progressDialog.dismiss();
                Toast.makeText(this, R.string.msg_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    private final RadioGroup.OnCheckedChangeListener mCheckedChangeListener = (radioGroup, checkedId) -> {
        switch (checkedId) {
            case R.id.rdM:
                // 남자
                gender = Constants.Gender.MALE;
                break;
            case R.id.rdF:
                // 여자
                gender = Constants.Gender.FEMALE;
                break;
        }
    };
}