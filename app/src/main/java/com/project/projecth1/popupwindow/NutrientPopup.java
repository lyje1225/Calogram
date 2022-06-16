//장웅희 개발자, 차상현 개발자
package com.project.projecth1.popupwindow;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.project.projecth1.R;
import com.project.projecth1.listener.OnPopupClickListener;
import com.project.projecth1.util.Utils;

public class NutrientPopup extends PopupWindow {

    private OnPopupClickListener listener;
    private Spinner spNutrient;
    private EditText editValue;
    private TextView txtUnit;

    private int mode;                       // 등록(0), 수정(1)

    public NutrientPopup(View view, OnPopupClickListener listener, String nutrient, long value, String unit) {
        super(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        this.listener = listener;

        String title = "영양정보";
        if (TextUtils.isEmpty(nutrient)) {
            title += " 등록";
            this.mode = 0;
        } else {
            title += " 수정";
            this.mode = 1;
        }
        ((TextView) view.findViewById(R.id.txtTitle)).setText(title);

        this.spNutrient = view.findViewById(R.id.spNutrient);
        this.editValue = view.findViewById(R.id.editValue);
        this.txtUnit = view.findViewById(R.id.txtUnit);

        // 영양성분 구성
        String[] nutrients = view.getResources().getStringArray(R.array.nutrient_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), R.layout.spinner_item, nutrients);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        this.spNutrient.setAdapter(adapter);

        // 수정모드이면
        if (this.mode == 1) {
            int position = 0;
            for (int i=0; i<nutrients.length; i++) {
                if (nutrients[i].equals(nutrient)) {
                    position = i;
                    break;
                }
            }
            this.spNutrient.setSelection(position);

            // 내용량
            if (unit.equals("g")) {
                this.editValue.setText(String.valueOf(value / 1000));
            } else {
                // mg
                this.editValue.setText(String.valueOf(value));
            }
        }

        this.txtUnit.setText(unit);

        view.findViewById(R.id.btnOk).setOnClickListener(view1 -> {
            // 확인
            if (checkData()) {
                if (this.listener != null) {
                    // 내용량
                    long v = Long.parseLong(this.editValue.getText().toString());
                    if (this.txtUnit.getText().toString().equals("g")) {
                        v *= 1000;
                    }

                    // 모드와 영양정보를 넘겨줌
                    Bundle bundle = new Bundle();
                    bundle.putString("nutrient", this.spNutrient.getSelectedItem().toString());
                    bundle.putLong("value", v);
                    bundle.putInt("mode", this.mode);

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
        // 내용량 입력 체크
        String value = this.editValue.getText().toString();
        if (TextUtils.isEmpty(value)) {
            Toast.makeText(getContentView().getContext(), R.string.msg_nutrient_value_check_empty, Toast.LENGTH_SHORT).show();
            this.editValue.requestFocus();
            return false;
        }

        if (!Utils.isNumeric(value)) {
            Toast.makeText(getContentView().getContext(), R.string.msg_nutrient_value_check_wrong, Toast.LENGTH_SHORT).show();
            this.editValue.requestFocus();
            return false;
        }

        return true;
    }
}
