//곽민승 개발자, 이윤제 개발자
package com.project.projecth1.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.projecth1.R;
import com.project.projecth1.entity.Exercise;
import com.project.projecth1.entity.Food;
import com.project.projecth1.entity.StatisticsDay;
import com.project.projecth1.fragment.abstracts.IFragment;
import com.project.projecth1.fragment.abstracts.ITaskFragment;
import com.project.projecth1.graph.GraphData;
import com.project.projecth1.graph.LineGraphView;
import com.project.projecth1.util.Constants;
import com.project.projecth1.util.GlobalVariable;
import com.project.projecth1.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;

public class StatisticsMonthFragment extends Fragment implements IFragment, ITaskFragment {
    private static final String TAG = StatisticsMonthFragment.class.getSimpleName();
    //private static final String TAG = "projecth1";

    private ProgressDialog progressDialog;          // 로딩 dialog

    private FloatingActionButton fabPrev, fabNext;

    private LineGraphView graph;                    // 선그래프
    private ArrayList<StatisticsDay> items;         // 일자별 섭취/운동 칼로리 정보 array

    private int graphPosition;                      // 그래프 위치


    private static final int GRAPH_Y_MAX = 4500;
    private static final int GRAPH_X_MAX = 7;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics_month, container, false);

        // 로딩 dialog
        this.progressDialog = new ProgressDialog(getContext());
        this.progressDialog.setMessage("처리중...");
        this.progressDialog.setCancelable(false);

        FrameLayout layGraph = view.findViewById(R.id.layGraph);

        // 그래프 초기화
        this.graph = new LineGraphView(getContext(), 2, 0.12F, 0.12F, 0.06F, 0.06F);
        layGraph.addView(this.graph, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        int position;
        // Argument 에서 값 얻기
        Bundle bundle = getArguments();
        if (bundle != null) {
            // 페이지 위치값 얻기
            position = bundle.getInt("position", 0);
        } else {
            position = 0;
        }

        this.fabPrev = view.findViewById(R.id.fabPrev);
        this.fabNext = view.findViewById(R.id.fabNext);

        this.fabPrev.setVisibility(View.GONE);
        this.fabNext.setVisibility(View.GONE);

        this.fabPrev.setOnClickListener(view1 -> {
            // 이전 그래프
            this.graphPosition--;

            // 그래프 그리기
            drawGraph();

        });

        this.fabNext.setOnClickListener(view1 -> {
            // 다음 그래프
            this.graphPosition++;

            // 그래프 그리기
            drawGraph();
        });

        view.post(() -> {
            // 가운데 페이지이면 (첫 실행시 한번만 실행됨)
            if (position == 1) {
                // 월 페이지 생성
                createMonth(System.currentTimeMillis());
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

            this.fabPrev.setVisibility(View.GONE);
            this.fabNext.setVisibility(View.GONE);

            // 그래프값 초기화
            ArrayList<GraphData[]> graphDataList = new ArrayList<>();
            for (int i=0; i<GRAPH_X_MAX; i++) {
                // 데이터 구성
                GraphData[] graphData = new GraphData[2];
                graphData[0] = new GraphData(i, 0);
                graphData[1] = new GraphData(i, 0);

                graphDataList.add(graphData);
            }
            this.graph.addData(graphDataList);          // 데이터 넣기
            this.graph.invalidate();

            // 월 페이지 생성
            createMonth(timeMillis);
        }
    }

    /* 일 페이지 생성 */
    private void createMonth(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);

        // 월 최대일
        int dayMax = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 통계값 초기화
        this.items = new ArrayList<>();
        for (int i=0; i<dayMax; i++) {
            // array (position + 1) 값이 day 와 동일함
            this.items.add(new StatisticsDay(i+1, 0, 0));
        }

        this.progressDialog.show();

        // 로딩 dialog 를 표시하기 위해 딜레이를 줌
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 섭취한 음식 칼로리 구하기
            setFoodCalorie(Utils.getDate("yyyy-MM", timeMillis));
        }, Constants.LoadingDelay.SHORT);
    }

    /* 섭취한 음식 칼로리 구하기 */
    private void setFoodCalorie(final String month) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 기간
        String date1 = month + "-01";
        String date2 = month + "-31";

        // 한달동안 섭취한 음식 내역 얻기
        Query query = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId)
                .collection(Constants.FirestoreCollectionName.FOOD)
                .whereGreaterThanOrEqualTo("date", date1)
                .whereLessThanOrEqualTo("date", date2)
                .orderBy("date");

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // 섭취한 음식 내역
                        Food food = document.toObject(Food.class);

                        int day = Integer.parseInt(food.getDate().substring(8, 10));

                        // 해당일에 섭취한 칼로리 추가
                        this.items.get(day -1).foodCalorie += food.getCalorie() * food.getFoodCount();
                    }
                }

                // 운동 칼로리 구하기
                setExerciseCalorie(month);
            } else {
                Log.d(TAG, "error:" + task.getException().toString());
                this.progressDialog.dismiss();
                Toast.makeText(getContext(), getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* 운동 칼로리 구하기 */
    private void setExerciseCalorie(final String month) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 기간
        String date1 = month + "-01";
        String date2 = month + "-31";

        // 한달동안 운동 내역 얻기
        Query query = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId)
                .collection(Constants.FirestoreCollectionName.EXERCISE)
                .whereGreaterThanOrEqualTo("date", date1)
                .whereLessThanOrEqualTo("date", date2)
                .orderBy("date");

        query.get().addOnCompleteListener(task -> {
            this.progressDialog.dismiss();

            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // 운동 내역
                        Exercise exercise = document.toObject(Exercise.class);

                        int day = Integer.parseInt(exercise.getDate().substring(8, 10));

                        // 해당일에 운동 칼로리 추가
                        this.items.get(day -1).exerciseCalorie += exercise.getCalorie();
                    }
                }

                this.graphPosition = 0;

                // 그래프 그리기
                drawGraph();
            } else {
                Log.d(TAG, "error:" + task.getException().toString());
                Toast.makeText(getContext(), getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* 그래프 그리기 */
    private void drawGraph() {
        int position = (this.graphPosition * GRAPH_X_MAX);

        if (position < 0 || position > (this.items.size() - 1)) {
            return;
        }

        ArrayList<GraphData[]> graphDataList = new ArrayList<>();

        ArrayList<String> labels = new ArrayList<>();   // X축 라벨

        for (int i=0; i<GRAPH_X_MAX; i++) {
            GraphData[] graphData = new GraphData[2];   // 데이터

            if ((i + position) < this.items.size()) {
                // X축 라벨값 설정
                labels.add(String.valueOf(this.items.get(i + position).day));

                // Y축 max 값보다 크면 max 값으로 설정
                graphData[0] = new GraphData(i, Math.min(this.items.get(i + position).foodCalorie, GRAPH_Y_MAX));       // 섭취한 칼로리
                graphData[1] = new GraphData(i, Math.min(this.items.get(i + position).exerciseCalorie, GRAPH_Y_MAX));   // 운동 칼로리
            } else {
                labels.add("");
                graphData[0] = new GraphData(i, 0);
                graphData[1] = new GraphData(i, 0);
            }

            graphDataList.add(graphData);
        }

        this.graph.setMaxX((GRAPH_X_MAX - 1), 6, false);
        this.graph.setMaxY(GRAPH_Y_MAX, 6, true);

        this.graph.setLabels(labels);               // X축 라벨
        this.graph.addData(graphDataList);          // 데이터 넣기

        // 그래프 색상
        int[] color = new int[2];
        color[0] = ContextCompat.getColor(getContext(), R.color.graph_data_food_color);     // 섭취한 칼로리
        color[1] = ContextCompat.getColor(getContext(), R.color.graph_data_exercise_color); // 운동 칼로리

        // 색상 적용
        this.graph.setLineColor(color);

        // 선 두께 설정
        this.graph.setStrokeWidth(3);

        // 그리기
        this.graph.reDrawAll();
        this.graph.invalidate();

        if (position == 0) {
            this.fabPrev.setVisibility(View.GONE);
            this.fabNext.setVisibility(View.VISIBLE);
        } else if ((position + GRAPH_X_MAX) > (this.items.size() - 1)) {
            this.fabPrev.setVisibility(View.VISIBLE);
            this.fabNext.setVisibility(View.GONE);
        } else {
            this.fabPrev.setVisibility(View.VISIBLE);
            this.fabNext.setVisibility(View.VISIBLE);
        }
    }
}
