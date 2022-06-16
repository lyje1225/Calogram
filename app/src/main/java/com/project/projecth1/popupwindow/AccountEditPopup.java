//이윤제 개발자
package com.project.projecth1.popupwindow;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.project.projecth1.R;
import com.project.projecth1.listener.OnPopupClickListener;
import com.project.projecth1.util.Utils;

public class AccountEditPopup extends PopupWindow {

    private OnPopupClickListener listener;
    private EditText editHeight, editWeight;

    public AccountEditPopup(View view, OnPopupClickListener listener, double height, double weight) {
        super(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        this.listener = listener;

        this.editHeight = view.findViewById(R.id.editHeight);
        this.editWeight = view.findViewById(R.id.editWeight);

        this.editHeight.setText(String.valueOf((Math.round(height * 100) / 100.0)));
        this.editWeight.setText(String.valueOf((Math.round(weight * 100) / 100.0)));

        view.findViewById(R.id.btnOk).setOnClickListener(view1 -> {
            // 확인
            if (checkData()) {
                if (this.listener != null) {
                    String heightStr = this.editHeight.getText().toString();
                    String weightStr = this.editWeight.getText().toString();

                    // 신장과 체중을 넘겨줌
                    Bundle bundle = new Bundle();
                    bundle.putDouble("height", Double.parseDouble(heightStr));
                    bundle.putDouble("weight", Double.parseDouble(weightStr));

                    this.listener.onClick(view1, bundle);
                }

                dismiss();
            }
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(view1 -> {
            // 취소
            dismiss();
        });
    }

    /* 입력 데이터 체크 */
    private boolean checkData() {
        // 키 입력 체크
        String height = this.editHeight.getText().toString();
        if (TextUtils.isEmpty(height)) {
            Toast.makeText(getContentView().getContext(), R.string.msg_user_height_check_empty, Toast.LENGTH_SHORT).show();
            this.editHeight.requestFocus();
            return false;
        }

        if (!Utils.isNumeric(height)) {
            Toast.makeText(getContentView().getContext(), R.string.msg_user_height_check_wrong, Toast.LENGTH_SHORT).show();
            this.editHeight.requestFocus();
            return false;
        }

        // 몸무게 입력 체크
        String weight = this.editWeight.getText().toString();
        if (TextUtils.isEmpty(weight)) {
            Toast.makeText(getContentView().getContext(), R.string.msg_user_weight_check_empty, Toast.LENGTH_SHORT).show();
            this.editWeight.requestFocus();
            return false;
        }

        if (!Utils.isNumeric(weight)) {
            Toast.makeText(getContentView().getContext(), R.string.msg_user_weight_check_wrong, Toast.LENGTH_SHORT).show();
            this.editWeight.requestFocus();
            return false;
        }

        return true;
    }
}
