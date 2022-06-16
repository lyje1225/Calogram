//차상현 개발자, 장웅희 개발자
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
import com.project.projecth1.data.SportsData;
import com.project.projecth1.listener.OnPopupClickListener;
import com.project.projecth1.util.Utils;

public class ExerciseAddPopup extends PopupWindow {

    private OnPopupClickListener listener;
    private Spinner spSports;
    private EditText editExerciseTime;

    public ExerciseAddPopup(View view, OnPopupClickListener listener) {
        super(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        this.listener = listener;

        this.spSports = view.findViewById(R.id.spSports);
        this.editExerciseTime = view.findViewById(R.id.editExerciseTime);

        // 운동종목 구성
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContentView().getContext(), R.layout.spinner_item,
                SportsData.getInstance().getNameList());
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        this.spSports.setAdapter(adapter);

        view.findViewById(R.id.btnOk).setOnClickListener(view1 -> {
            // 확인
            if (checkData()) {
                if (this.listener != null) {
                    String exerciseTimeStr = this.editExerciseTime.getText().toString();

                    // 운동종목과 시간을 넘겨줌
                    Bundle bundle = new Bundle();
                    bundle.putString("sports", this.spSports.getSelectedItem().toString());
                    bundle.putInt("exercise_time", Integer.parseInt(exerciseTimeStr));

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
        // 운동시간 입력 체크
        String exerciseTime = this.editExerciseTime.getText().toString();
        if (TextUtils.isEmpty(exerciseTime)) {
            Toast.makeText(getContentView().getContext(), R.string.msg_exercise_time_check_empty, Toast.LENGTH_SHORT).show();
            this.editExerciseTime.requestFocus();
            return false;
        }

        if (!Utils.isNumeric(exerciseTime)) {
            Toast.makeText(getContentView().getContext(), R.string.msg_exercise_time_check_wrong, Toast.LENGTH_SHORT).show();
            this.editExerciseTime.requestFocus();
            return false;
        }

        return true;
    }
}
