//이윤제 개발자, 곽민승 개발자
package com.project.projecth1.popupwindow;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import com.project.projecth1.R;
import com.project.projecth1.listener.OnPopupClickListener;
import com.project.projecth1.util.Utils;

import java.util.ArrayList;

public class FoodAddPopup extends PopupWindow {

    private OnPopupClickListener listener;
    private Spinner spMealKind, spFoodCount;
    private EditText editFoodName;

    public FoodAddPopup(View view, OnPopupClickListener listener) {
        super(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        this.listener = listener;

        this.spMealKind = view.findViewById(R.id.spMealKind);
        this.spFoodCount = view.findViewById(R.id.spFoodCount);
        this.editFoodName = view.findViewById(R.id.editFoodName);

        // 아침/점심/저녁 구성
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(view.getContext(), R.layout.spinner_item,
                view.getResources().getStringArray(R.array.meal_kind_list));
        adapter1.setDropDownViewResource(R.layout.spinner_dropdown_item);
        this.spMealKind.setAdapter(adapter1);

        // 음식수량 구성
        ArrayList<String> counts = new ArrayList<>();
        for (int i=1; i<=30; i++) {
            counts.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(view.getContext(), R.layout.spinner_item, counts);
        adapter2.setDropDownViewResource(R.layout.spinner_dropdown_item);
        this.spFoodCount.setAdapter(adapter2);

        view.findViewById(R.id.btnCamera).setOnClickListener(view1 -> {
            // 촬영
            input(view1);
        });

        view.findViewById(R.id.btnText).setOnClickListener(view1 -> {
            // 직접등록
            input(view1);
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(view1 -> {
            // 취소
            dismiss();
        });
    }

    /* 입력 데이터 체크 */
    private boolean checkData() {
        // 음식명 입력 체크
        String foodName = this.editFoodName.getText().toString();
        if (TextUtils.isEmpty(foodName)) {
            Toast.makeText(getContentView().getContext(), R.string.msg_food_name_check_empty, Toast.LENGTH_SHORT).show();
            this.editFoodName.requestFocus();
            return false;
        }

        return true;
    }

    /* 직접등록 / 촬영 */
    private void input(View view) {
        if (checkData()) {
            if (this.listener != null) {
                // 정보를 넘겨줌
                Bundle bundle = new Bundle();
                bundle.putInt("meal_kind", this.spMealKind.getSelectedItemPosition());
                bundle.putString("food_name", this.editFoodName.getText().toString());
                bundle.putInt("food_count", Integer.parseInt(this.spFoodCount.getSelectedItem().toString()));

                this.listener.onClick(view, bundle);
            }

            dismiss();
        }
    }
}