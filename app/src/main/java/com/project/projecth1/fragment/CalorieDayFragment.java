//곽민승 개발자, 장웅희 개발자
package com.project.projecth1.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.project.projecth1.R;
import com.project.projecth1.adapter.FoodAdapter;
import com.project.projecth1.entity.Food;
import com.project.projecth1.entity.FoodItem;
import com.project.projecth1.entity.Nutrient;
import com.project.projecth1.entity.NutrientGraphItem;
import com.project.projecth1.fragment.abstracts.IFragment;
import com.project.projecth1.fragment.abstracts.ITaskFragment;
import com.project.projecth1.util.Constants;
import com.project.projecth1.util.GlobalVariable;
import com.project.projecth1.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CalorieDayFragment extends Fragment implements IFragment, ITaskFragment {
    private static final String TAG = CalorieDayFragment.class.getSimpleName();
    //private static final String TAG = "projecth1";

    private ProgressDialog progressDialog;          // 로딩 dialog

    // 그래프 관련 변수
    private LinearLayout layGraphArea, layLegendB;
    private View[] viewLegends, viewGraphs;
    private TextView[] txtLegends;

    private RecyclerView recyclerView;
    private FoodAdapter adapter;

    private TextView txtCalorie, txtNone;

    private ArrayList<FoodItem> items;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calorie_day, container, false);

        // 로딩 dialog
        this.progressDialog = new ProgressDialog(getContext());
        this.progressDialog.setMessage("처리중...");
        this.progressDialog.setCancelable(false);

        // 그래프 관려 변수
        this.layGraphArea = view.findViewById(R.id.layGraphArea);
        this.layLegendB = view.findViewById(R.id.layLegendB);

        // 범례 View
        int[] legendVRes = { R.id.viewLegend1, R.id.viewLegend2, R.id.viewLegend3, R.id.viewLegend4 };
        this.viewLegends = new View[legendVRes.length];
        for (int i=0; i<legendVRes.length; i++) {
            this.viewLegends[i] = view.findViewById(legendVRes[i]);
        }

        // 범례 Text
        int[] legendTRes = { R.id.txtLegend1, R.id.txtLegend2, R.id.txtLegend3, R.id.txtLegend4 };
        this.txtLegends = new TextView[legendTRes.length];
        for (int i=0; i<legendTRes.length; i++) {
            this.txtLegends[i] = view.findViewById(legendTRes[i]);
        }

        // 그래프 View
        int[] graphVRes = { R.id.viewGraph1, R.id.viewGraph2, R.id.viewGraph3, R.id.viewGraph4 };
        this.viewGraphs = new View[graphVRes.length];
        for (int i=0; i<graphVRes.length; i++) {
            this.viewGraphs[i] = view.findViewById(graphVRes[i]);
        }

        // 리사이클러뷰
        this.recyclerView = view.findViewById(R.id.recyclerView);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        this.txtCalorie = view.findViewById(R.id.txtCalorie);
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
            this.txtCalorie.setText("-");
            this.items.clear();
            this.adapter.notifyDataSetChanged();

            this.layGraphArea.setVisibility(View.GONE);
            this.layLegendB.setVisibility(View.VISIBLE);

            for (View view : this.viewLegends) {
                view.setVisibility(View.VISIBLE);
            }

            for (TextView textView : this.txtLegends) {
                textView.setVisibility(View.VISIBLE);
            }

            for (View view : this.viewGraphs) {
                view.setVisibility(View.VISIBLE);
            }

            // 일 페이지 생성
            createDay(timeMillis);
        }
    }

    /* 일 페이지 생성 */
    private void createDay(long timeMillis) {
        this.progressDialog.show();

        // 로딩 dialog 를 표시하기 위해 딜레이를 줌
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 섭취한 음식 목록
            listFood(Utils.getDate("yyyy-MM-dd", timeMillis));
        }, Constants.LoadingDelay.SHORT);
    }

    /* 섭취한 음식 목록 */
    private void listFood(String date) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 섭취한 음식 목록 (아침/점심/저녁 순으로 정렬)
        Query query = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId)
                .collection(Constants.FirestoreCollectionName.FOOD)
                .whereEqualTo("date", date)
                .orderBy("mealKind");

        query.get().addOnCompleteListener(task -> {
            this.progressDialog.dismiss();

            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    this.items = new ArrayList<>();

                    // 섭취한 음식 목록
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Food food = document.toObject(Food.class);

                        // 섭취한 음식 추가
                        this.items.add(new FoodItem(document.getId(), food));
                    }

                    if (items.size() == 0) {
                        // 섭취한 음식 목록 없으면
                        this.txtNone.setVisibility(View.VISIBLE);
                    } else {
                        this.txtNone.setVisibility(View.GONE);
                    }

                    // 리스트에 어뎁터 설정
                    this.adapter = new FoodAdapter(null, this.items);
                    this.recyclerView.setAdapter(this.adapter);

                    // 칼로리 표시
                    displayCalorie();

                    // 그래프 표시
                    displayGraph();
                }
            } else {
                // 오류
                Log.d(TAG, "error:" + task.getException().toString());

                this.txtCalorie.setText("오류");
                this.layGraphArea.setVisibility(View.GONE);
            }
        });
    }

    /* 칼로리 표시 */
    private void displayCalorie() {
        int calorie = 0;

        for (FoodItem item : this.items) {
            calorie += item.food.getCalorie() * item.food.getFoodCount();
        }

        this.txtCalorie.setText(Utils.formatComma(calorie) + "kcal");       // 섭취한 칼로리
    }

    /* 그래프 표시 */
    private void displayGraph() {
        // 영양정보 내용량 합산
        ArrayList<Nutrient> nutrients = new ArrayList<>();
        long total = 0;     // 내용량 총량 (영양성분별 비율을 구하기 위함)
        for (FoodItem item : this.items) {
            // 영양정보
            for (String str : item.food.getNutrients()) {
                // 영양성분@내용량(mg)
                String[] data = str.split("@");
                long value = item.food.getFoodCount() * Long.parseLong(data[1]);    // 내용량
                boolean exist = false;
                for (Nutrient nutrient : nutrients) {
                    // 영양성분이 존재하면
                    if (nutrient.getName().equals(data[0])) {
                        // 내용량 +
                        nutrient.setValue(nutrient.getValue() + value);
                        exist = true;
                    }
                }

                // 존재하지 않으면
                if (!exist) {
                    nutrients.add(new Nutrient(data[0], value));
                }

                total += value;
            }
        }

        // 내용량이 0인 성분은 삭제
        for (int i=0; i<nutrients.size(); i++) {
            if (nutrients.get(i).getValue() == 0) {
                nutrients.remove(i);
                i--;
            }
        }

        if (nutrients.size() == 0) {
            return;
        }

        this.layGraphArea.setVisibility(View.VISIBLE);

        // 내용량이 많은순으로 정렬
        Collections.sort(nutrients, getComparator());

        // 그래프 구성에 사용
        ArrayList<NutrientGraphItem> graphItems = new ArrayList<>();
        int percentSum = 0;
        int count = 0;
        for (Nutrient nutrient : nutrients) {
            if (count == 3) {
                String name;
                int color;
                if (nutrients.size() > 4) {
                    // 기타 항목 만들기
                    name = "기타";
                    color = ContextCompat.getColor(getContext(), R.color.nutrient_color_0);
                } else {
                    name = nutrient.getName();
                    color = getNutrientColor(name);
                }
                graphItems.add(new NutrientGraphItem(name, (1000 - percentSum), color));
                break;
            } else {
                // 소수점 한자리까지 표시 (반올림) * 10
                int percent = (int) Math.round((nutrient.getValue() * 100.0 / total) * 10);
                percentSum += percent;

                graphItems.add(new NutrientGraphItem(nutrient.getName(), percent, getNutrientColor(nutrient.getName())));
            }
            count++;
        }

        if (graphItems.size() < 4) {
            for (int i=this.viewLegends.length-1; i>=graphItems.size(); i--) {
                this.viewLegends[i].setVisibility(View.GONE);
                this.txtLegends[i].setVisibility(View.GONE);
                this.viewGraphs[i].setVisibility(View.GONE);
            }

            if (graphItems.size() < 3) {
                this.layLegendB.setVisibility(View.GONE);
            }
        }

        for (int i=0; i<graphItems.size(); i++) {
            final NutrientGraphItem item = graphItems.get(i);

            // 범례
            this.viewLegends[i].setBackgroundColor(item.getColor());
            String text = item.getName() + "(" + (item.getPercent() / 10.0) + "%)";
            this.txtLegends[i].setText(text);

            // 그래프 구성
            final int position = i;
            this.viewGraphs[i].post(() -> {
                // 색상 지정
                this.viewGraphs[position].setBackgroundColor(item.getColor());

                // 넓이 조절
                ((LinearLayout.LayoutParams) this.viewGraphs[position].getLayoutParams()).weight = item.getPercent();
                this.viewGraphs[position].requestLayout();
            });
        }
    }

    /* 데이터 정렬을 위한 Comparator (내용량 DESC) */
    private Comparator<Nutrient> getComparator() {
        Comparator<Nutrient> comparator = (sort1, sort2) -> {
            // 정렬
            return Double.compare(sort2.getValue(), sort1.getValue());
        };

        return comparator;
    }

    /* 영양성분 색상 얻기 */
    private int getNutrientColor(String nutrient) {
        int color;

        int position = 0;
        for (String name : getResources().getStringArray(R.array.nutrient_list)) {
            if (nutrient.equals(name)) {
                break;
            }
            position++;
        }

        switch (position) {
            case 0:
                // 나트륨
                color = ContextCompat.getColor(getContext(), R.color.nutrient_color_1);
                break;
            case 1:
                // 탄수화물
                color = ContextCompat.getColor(getContext(), R.color.nutrient_color_2);
                break;
            case 2:
                // 당류
                color = ContextCompat.getColor(getContext(), R.color.nutrient_color_3);
                break;
            case 3:
                // 지방
                color = ContextCompat.getColor(getContext(), R.color.nutrient_color_4);
                break;
            case 4:
                // 단백질
                color = ContextCompat.getColor(getContext(), R.color.nutrient_color_5);
                break;
            default:
                color = ContextCompat.getColor(getContext(), R.color.nutrient_color_0);
                break;
        }

        return color;
    }
}
