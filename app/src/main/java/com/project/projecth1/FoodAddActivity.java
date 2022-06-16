//장웅희 개발자, 이윤제 개발자
package com.project.projecth1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.project.projecth1.adapter.NutrientAdapter;
import com.project.projecth1.data.SportsData;
import com.project.projecth1.entity.Exercise;
import com.project.projecth1.entity.Food;
import com.project.projecth1.entity.Nutrient;
import com.project.projecth1.fragment.abstracts.ITaskFragment;
import com.project.projecth1.listener.OnItemClickListener;
import com.project.projecth1.popupwindow.NutrientPopup;
import com.project.projecth1.util.Constants;
import com.project.projecth1.util.GlobalVariable;
import com.project.projecth1.util.Utils;

import java.util.ArrayList;
import java.util.Objects;

public class FoodAddActivity extends AppCompatActivity {
    private static final String TAG = FoodAddActivity.class.getSimpleName();
    //private static final String TAG = "projecth1";

    private ProgressDialog progressDialog;          // 로딩 dialog

    private EditText editCalorie;
    private TextView txtNone;

    private RecyclerView recyclerView;
    private NutrientAdapter adapter;
    private ArrayList<Nutrient> items;

    private InputMethodManager imm;             // 키보드를 숨기기 위해 필요함

    private int selectedPosition;               // 영양정보 리스트 위치 (수정시 사용)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_add);

        setTitle(R.string.title_food_add);

        // 홈버튼(<-) 표시
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // 로딩 dialog
        this.progressDialog = new ProgressDialog(this);
        this.progressDialog.setMessage("처리중...");
        this.progressDialog.setCancelable(false);

        // 음식정보
        ((TextView) findViewById(R.id.txtMealKind)).setText(Utils.getMealKind(GlobalVariable.food.getMealKind()));
        ((TextView) findViewById(R.id.txtFoodName)).setText(GlobalVariable.food.getFoodName());
        ((TextView) findViewById(R.id.txtFoodCount)).
                setText(Utils.formatComma(GlobalVariable.food.getFoodCount()) + "개");

        // 1개당 칼로리
        this.editCalorie = findViewById(R.id.editCalorie);
        this.editCalorie.setText(String.valueOf(GlobalVariable.food.getCalorie()));

        this.txtNone = findViewById(R.id.txtNone);

        // 리사이클러뷰
        this.recyclerView = findViewById(R.id.recyclerView);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // 키보드를 숨기기 위해 필요함
        this.imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        findViewById(R.id.imgNutrientAdd).setOnClickListener(view -> {
            // 영양정보 추가
            onPopupNutrient("", 0, "mg");
        });

        findViewById(R.id.btnOk).setOnClickListener(view -> {
            // 저장
            if (checkData()) {
                this.progressDialog.show();

                // 로딩 dialog 를 표시하기 위해 딜레이를 줌
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // 저장
                    save();
                }, Constants.LoadingDelay.SHORT);
            }
        });

        // 영양정보 목록
        listNutrient();
    }

    @Override
    public void onBackPressed() {
        if (this.progressDialog.isShowing()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (this.progressDialog.isShowing()) {
                return true;
            }

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* 영양정보 목록 */
    private void listNutrient() {
        // 영양정보
        this.items = new ArrayList<>();
        for (String nutrient : GlobalVariable.food.getNutrients()) {
            String[] data = nutrient.split("@");
            this.items.add(new Nutrient(data[0], Integer.parseInt(data[1])));
        }

        if (this.items.size() == 0) {
            // 영양정보가 없으면
            this.txtNone.setVisibility(View.VISIBLE);
        } else {
            this.txtNone.setVisibility(View.GONE);
        }

        // 리스트에 어뎁터 설정
        this.adapter = new NutrientAdapter(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 영양성분 선택 (수정하기)
                String unit = "mg";
                long value = items.get(position).getValue();
                if (value >= 1000) {
                    unit = "g";
                }

                selectedPosition = position;
                onPopupNutrient(items.get(position).getName(), value, unit);
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                // 영양성분 삭제하기
                new AlertDialog.Builder(FoodAddActivity.this)
                        .setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
                            // 삭제
                            // 리스트에서 삭제
                            adapter.remove(position);

                            if (items.size() == 0) {
                                txtNone.setVisibility(View.VISIBLE);
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .setCancelable(false)
                        .setTitle(R.string.dialog_title_delete)
                        .setMessage(R.string.dialog_msg_delete)
                        .show();
            }
        }, this.items);
        this.recyclerView.setAdapter(this.adapter);
    }

    /* 영양정보 등록/수정 팝업창 호출 */
    private void onPopupNutrient(String nutrient, long value, String unit) {
        // 키보드 숨기기
        this.imm.hideSoftInputFromWindow(this.editCalorie.getWindowToken(), 0);

        View popupView = View.inflate(this, R.layout.popup_nutrient, null);
        NutrientPopup popup = new NutrientPopup(popupView, (view, bundle) -> {
            // 영양정보 등록/수정
            if (bundle.getInt("mode") == 0) {
                // 등록
                Nutrient item = new Nutrient(bundle.getString("nutrient"), bundle.getLong("value"));

                // 리스트에 추가
                this.adapter.add(item, -1);
                this.recyclerView.scrollToPosition(this.items.size() - 1);

                this.txtNone.setVisibility(View.GONE);
            } else {
                // 수정
                this.items.get(this.selectedPosition).setName(bundle.getString("nutrient"));
                this.items.get(this.selectedPosition).setValue(bundle.getLong("value"));

                this.adapter.notifyItemChanged(this.selectedPosition);
            }
        }, nutrient, value, unit);
        // Back 키 눌렸을때 닫기 위함
        popup.setFocusable(true);
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    /* 입력 데이터 체크 */
    private boolean checkData() {
        // 이메일 입력 체크
        String calorie = this.editCalorie.getText().toString();
        if (TextUtils.isEmpty(calorie)) {
            Toast.makeText(this, R.string.msg_calorie_check_empty, Toast.LENGTH_SHORT).show();
            this.editCalorie.requestFocus();
            return false;
        }

        if (!Utils.isNumeric(calorie)) {
            Toast.makeText(this, R.string.msg_calorie_check_wrong, Toast.LENGTH_SHORT).show();
            this.editCalorie.requestFocus();
            return false;
        }

        // 영양정보
        if (this.items.size() == 0) {
            Toast.makeText(this, R.string.msg_nutrient_empty, Toast.LENGTH_SHORT).show();
            return false;
        }

        // 키보드 숨기기
        this.imm.hideSoftInputFromWindow(this.editCalorie.getWindowToken(), 0);

        return true;
    }

    /* 저장 */
    private void save() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String calorie = this.editCalorie.getText().toString();
        GlobalVariable.food.setCalorie(Integer.parseInt(calorie));  // 칼로리 설정

        ArrayList<String> nutrients = new ArrayList<>();
        for (Nutrient nutrient : this.items) {
            String data = nutrient.getName() + "@" + nutrient.getValue();
            nutrients.add(data);
        }
        GlobalVariable.food.setNutrients(nutrients);                // 영양정보 설정

        GlobalVariable.food.setInputTimeMillis(System.currentTimeMillis());

        // 섭취한 음식 등록
        db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId)
                .collection(Constants.FirestoreCollectionName.FOOD)
                .add(GlobalVariable.food)
                .addOnSuccessListener(documentReference -> {
                    // 성공
                    this.progressDialog.dismiss();

                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // 등록 실패
                    this.progressDialog.dismiss();
                    Toast.makeText(this, R.string.msg_error, Toast.LENGTH_SHORT).show();
                });
    }
}
