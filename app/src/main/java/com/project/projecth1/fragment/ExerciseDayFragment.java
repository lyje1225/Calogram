//차상현 개발자, 장웅희 개발자
package com.project.projecth1.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.project.projecth1.R;
import com.project.projecth1.adapter.ExerciseAdapter;
import com.project.projecth1.adapter.FoodAdapter;
import com.project.projecth1.entity.Exercise;
import com.project.projecth1.entity.ExerciseItem;
import com.project.projecth1.entity.Food;
import com.project.projecth1.entity.FoodItem;
import com.project.projecth1.fragment.abstracts.IFragment;
import com.project.projecth1.fragment.abstracts.ITaskFragment;
import com.project.projecth1.listener.OnItemClickListener;
import com.project.projecth1.util.Constants;
import com.project.projecth1.util.GlobalVariable;
import com.project.projecth1.util.Utils;

import java.util.ArrayList;

public class ExerciseDayFragment extends Fragment implements IFragment, ITaskFragment {
    //private static final String TAG = ExerciseDayFragment.class.getSimpleName();
    private static final String TAG = "projecth1";

    private ProgressDialog progressDialog;          // 로딩 dialog

    private RecyclerView recyclerView;
    private ExerciseAdapter adapter;

    private TextView txtCalorie1, txtCalorie2, txtCalorie3, txtNone;

    private ArrayList<ExerciseItem> items;

    private int foodCalorie;                        // 섭취한 음식 칼로리
    private int basicMetabolicRate;                 // 기초 대사량

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_day, container, false);

        // 로딩 dialog
        this.progressDialog = new ProgressDialog(getContext());
        this.progressDialog.setMessage("처리중...");
        this.progressDialog.setCancelable(false);

        // 리사이클러뷰
        this.recyclerView = view.findViewById(R.id.recyclerView);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        this.txtCalorie1 = view.findViewById(R.id.txtCalorie1);
        this.txtCalorie2 = view.findViewById(R.id.txtCalorie2);
        this.txtCalorie3 = view.findViewById(R.id.txtCalorie3);
        this.txtNone = view.findViewById(R.id.txtNone);

        int position;

        // Argument 에서 값 얻기
        Bundle bundle = getArguments();
        if (bundle != null) {
            // 페이지 위치값 얻기
            position = bundle.getInt("position", 0);
        } else {
            position = 0;
        }

        view.post(() -> {
            // 가운데 페이지이면 (첫 실행시 한번만 실행됨)
            if (position == 1) {
                // 나이
                int age = Utils.getAge(Integer.parseInt(GlobalVariable.user.getBirthDate().substring(0,4)),
                        Integer.parseInt(GlobalVariable.user.getBirthDate().substring(4,6)),
                        Integer.parseInt(GlobalVariable.user.getBirthDate().substring(6,8)));

                // 기초 대사량 구하기
                this.basicMetabolicRate = (int) Utils.getBasicMetabolicRate(GlobalVariable.user.getGender(),
                        age, GlobalVariable.user.getHeight(), GlobalVariable.user.getWeight());

                // 일 페이지 생성
                createDay(System.currentTimeMillis());
            }
        });

        return view;
    }

    @Override
    public boolean isExecuted() {
        return this.progressDialog.isShowing();
    }

    @Override
    public void task(int kind, Bundle bundle) {
        if (kind == Constants.FragmentTaskKind.REFRESH) {
            long timeMillis = bundle.getLong("time_millis");

            // clear
            this.txtCalorie1.setText("-");
            this.txtCalorie2.setText("-");
            this.txtCalorie3.setText("-");

            this.items.clear();
            this.adapter.notifyDataSetChanged();

            // 일 페이지 생성
            createDay(timeMillis);
        }
    }

    /* 섭취한 음식 칼로리 구하기 */
    private void calcFoodCalorie(long timeMillis) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 섭취한 음식 목록
        Query query = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId)
                .collection(Constants.FirestoreCollectionName.FOOD)
                .whereEqualTo("date", Utils.getDate("yyyy-MM-dd", timeMillis));

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    this.items = new ArrayList<>();

                    // 섭취한 음식 목록
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Food food = document.toObject(Food.class);

                        // 칼로리 구하기
                        this.foodCalorie += food.getFoodCount() * food.getCalorie();
                    }
                }
            } else {
                // 오류
                Log.d(TAG, "error:" + task.getException().toString());
                this.foodCalorie = 0;
            }
        });
    }

    /* 일 페이지 생성 */
    private void createDay(long timeMillis) {
        this.foodCalorie = 0;
        // 섭취한 음식 칼로리 구하기
        calcFoodCalorie(timeMillis);

        this.progressDialog.show();

        // 로딩 dialog 를 표시하기 위해 딜레이를 줌
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 운동 목록
            listExercise(Utils.getDate("yyyy-MM-dd", timeMillis));
        }, Constants.LoadingDelay.SHORT);
    }

    /* 운동 목록 */
    private void listExercise(String date) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 운동 목록 (최근등록 순으로 정렬)
        Query query = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId)
                .collection(Constants.FirestoreCollectionName.EXERCISE)
                .whereEqualTo("date", date)
                .orderBy("inputTimeMillis", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(task -> {
            this.progressDialog.dismiss();

            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    this.items = new ArrayList<>();

                    // 운동 목록
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Exercise exercise = document.toObject(Exercise.class);

                        // 운동 추가
                        this.items.add(new ExerciseItem(document.getId(), exercise));
                    }

                    if (items.size() == 0) {
                        // 운동 목록 없으면
                        this.txtNone.setVisibility(View.VISIBLE);
                    } else {
                        this.txtNone.setVisibility(View.GONE);
                    }

                    // 리스트에 어뎁터 설정
                    this.adapter = new ExerciseAdapter(null, this.items);
                    this.recyclerView.setAdapter(this.adapter);

                    // 칼로리 표시
                    displayCalorie();
                }
            } else {
                // 오류
                Log.d(TAG, "error:" + task.getException().toString());

                this.txtCalorie1.setText("오류");
                this.txtCalorie2.setText("오류");
                this.txtCalorie3.setText("오류");
            }
        });
    }

    /* 칼로리 표시 */
    private void displayCalorie() {
        int calorie = 0;

        for (ExerciseItem item : this.items) {
            calorie += item.exercise.getCalorie();
        }

        // 태운칼로리 (기초대사량 + 운동 칼로리)
        int burnCalorie = this.basicMetabolicRate + calorie;

        // 남은칼로리 (섭취한칼로리가 기초대사량보다 적으면 운동칼로리만 표시함)
        int remainingCalorie;
        if (this.foodCalorie < this.basicMetabolicRate) {
            remainingCalorie = -calorie;
        } else {
            remainingCalorie = this.foodCalorie - burnCalorie;
        }

        this.txtCalorie1.setText(Utils.formatComma(remainingCalorie) + "kcal");     // 남은 칼로리 (섭취한 음식칼로리 - 태운칼로리)
        this.txtCalorie2.setText(Utils.formatComma(basicMetabolicRate) + "kcal");     // 기초대사량
        this.txtCalorie3.setText(Utils.formatComma(burnCalorie) + "kcal");          // 태운 칼로리
    }
}
