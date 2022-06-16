//이윤제 개발자, 차상현 개발자
package com.project.projecth1.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.projecth1.R;
import com.project.projecth1.fragment.abstracts.IFragment;
import com.project.projecth1.listener.OnPopupClickListener;
import com.project.projecth1.popupwindow.AccountEditPopup;
import com.project.projecth1.util.Constants;
import com.project.projecth1.util.GlobalVariable;
import com.project.projecth1.util.Utils;

public class AccountFragment extends Fragment implements IFragment {
    //private static final String TAG = AccountFragment.class.getSimpleName();
    private static final String TAG = "projecth1";

    private Context context;

    private ProgressDialog progressDialog;          // 로딩 dialog

    private TextView txtEmail, txtName, txtGender, txtBirthDate, txtHeight, txtWeight, txtBasicMetabolicRate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // 로딩 dialog
        this.progressDialog = new ProgressDialog(this.context);
        this.progressDialog.setMessage("처리중...");
        this.progressDialog.setCancelable(false);

        this.txtEmail = view.findViewById(R.id.txtEmail);
        this.txtName = view.findViewById(R.id.txtName);
        this.txtGender = view.findViewById(R.id.txtGender);
        this.txtBirthDate = view.findViewById(R.id.txtBirthDate);
        this.txtHeight = view.findViewById(R.id.txtHeight);
        this.txtWeight = view.findViewById(R.id.txtWeight);
        this.txtBasicMetabolicRate = view.findViewById(R.id.txtBasicMetabolicRate);

        view.findViewById(R.id.imgAccountEdit).setOnClickListener(view1 -> {
            // 정보 수정
            onPopupAccountEdit();
        });

        // 나의 정보
        infoMy();

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
        return this.progressDialog.isShowing();
    }

    /* 나의 정보 */
    private void infoMy() {
        this.txtEmail.setText(GlobalVariable.user.getEmail());          // 이메일
        this.txtName.setText(GlobalVariable.user.getName());            // 이름

        // 성별
        if (GlobalVariable.user.getGender().equals(Constants.Gender.MALE)) {
            this.txtGender.setText("남");
        } else {
            this.txtGender.setText("여");
        }

        this.txtBirthDate.setText(GlobalVariable.user.getBirthDate());  // 생년월일
        this.txtHeight.setText((Math.round(GlobalVariable.user.getHeight() * 100) / 100.0) + "cm"); // 신장
        this.txtWeight.setText((Math.round(GlobalVariable.user.getWeight() * 100) / 100.0) + "kg"); // 체중

        // 기초대사량 계산하기
        calcBasicMetabolicRate();
    }

    /* 기초대사량 계산하기 */
    private void calcBasicMetabolicRate() {
        // 나이
        int age = Utils.getAge(Integer.parseInt(GlobalVariable.user.getBirthDate().substring(0,4)),
                Integer.parseInt(GlobalVariable.user.getBirthDate().substring(4,6)),
                Integer.parseInt(GlobalVariable.user.getBirthDate().substring(6,8)));

        // 기초대사량
        int value = (int) Utils.getBasicMetabolicRate(GlobalVariable.user.getGender(),
                age, GlobalVariable.user.getHeight(), GlobalVariable.user.getWeight());
        this.txtBasicMetabolicRate.setText(Utils.formatComma(value) + "kcal");
    }

    /* 정보 수정 팝업창 호출 */
    private void onPopupAccountEdit() {
        View popupView = View.inflate(getContext(), R.layout.popup_account_edit, null);
        AccountEditPopup popup = new AccountEditPopup(popupView, (view, bundle) -> {
            // 정보 수정
            this.progressDialog.show();

            // 로딩 dialog 를 표시하기 위해 딜레이를 줌
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // 정보 수정
                modifyAccount(bundle.getDouble("height"), bundle.getDouble("weight"));
            }, Constants.LoadingDelay.SHORT);
        }, GlobalVariable.user.getHeight(), GlobalVariable.user.getWeight());

        // Back 키 눌렸을때 닫기 위함
        popup.setFocusable(true);
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    /* 정보 수정 */
    private void modifyAccount(double height, double weight) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // user document 참조
        DocumentReference reference = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId);
        // 정보 수정
        reference.update("height", height, "weight", weight)
                .addOnSuccessListener(aVoid -> {
                    // 성공
                    this.progressDialog.dismiss();

                    this.txtHeight.setText((Math.round(height * 100) / 100.0) + "cm");  // 신장
                    this.txtWeight.setText((Math.round(weight * 100) / 100.0) + "kg");  // 체중

                    // 전역변수에 저장
                    GlobalVariable.user.setHeight(height);
                    GlobalVariable.user.setWeight(weight);

                    // 기초대사량 계산하기
                    calcBasicMetabolicRate();
                })
                .addOnFailureListener(e -> {
                    // 실패
                    this.progressDialog.dismiss();
                    Toast.makeText(getContext(), R.string.msg_error, Toast.LENGTH_SHORT).show();
                });
    }
}
