//차상현 개발자, 장웅희 개발자
package com.project.projecth1.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.FirebaseFirestore;
import com.project.projecth1.R;
import com.project.projecth1.adapter.MyFragmentStateAdapter;
import com.project.projecth1.data.SportsData;
import com.project.projecth1.entity.Exercise;
import com.project.projecth1.fragment.abstracts.IFragment;
import com.project.projecth1.fragment.abstracts.ITaskFragment;
import com.project.projecth1.popupwindow.ExerciseAddPopup;
import com.project.projecth1.util.Constants;
import com.project.projecth1.util.GlobalVariable;
import com.project.projecth1.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;

public class ExerciseFragment extends Fragment implements IFragment {
    //private static final String TAG = ExerciseFragment.class.getSimpleName();
    private static final String TAG = "projecth1";

    private Context context;

    private ArrayList<Fragment> fragments;

    private ViewPager2 viewPager;
    private TextView txtDate;

    private Calendar calendar;
    private int pagePosition = 1;               // 디폴트 포지션

    private static final int PAGE_MIDDLE = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        this.viewPager = view.findViewById(R.id.viewPager);

        // 유지되는 페이지수를 설정
        // (3개의 페이지를 초반에 미리로딩한다. 페이지를 이동할때 마다 View 를 지우고 새로만드는 작업은 하지않게 된다)
        this.viewPager.setOffscreenPageLimit(3);

        // day 3개를 생성 (이전일, 현재일, 다음일)
        this.fragments = new ArrayList<>();
        for (int i=0; i<3; i++) {
            Fragment fragment = new ExerciseDayFragment();
            // 현재 위치값 전달
            Bundle bundle = new Bundle();
            bundle.putInt("position", i);
            fragment.setArguments(bundle);
            this.fragments.add(fragment);
        }

        MyFragmentStateAdapter adapter = new MyFragmentStateAdapter(this, this.fragments);
        this.viewPager.setAdapter(adapter);

        this.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    // 스크롤이 정지되어 있는 상태
                    if (pagePosition < PAGE_MIDDLE) {
                        // 이전일
                        prevDay();
                    } else if (pagePosition > PAGE_MIDDLE) {
                        // 다음일
                        nextDay();
                    } else {
                        return;
                    }

                    // 페이지를 다시 가운데로 맞춘다 (3페이지로 계속 이전 / 다음 할 수 있게 하기위함)
                    viewPager.setCurrentItem(PAGE_MIDDLE, false);

                    // 일 만들기
                    Bundle bundle = new Bundle();
                    bundle.putLong("time_millis", calendar.getTimeInMillis());
                    ((ITaskFragment) fragments.get(PAGE_MIDDLE)).task(Constants.FragmentTaskKind.REFRESH, bundle);
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                pagePosition = position;
            }
        });

        this.txtDate = view.findViewById(R.id.txtDate);

        // 현재일
        this.calendar = Calendar.getInstance();
        this.txtDate.setText(Utils.getDate("yyyy-MM-dd", this.calendar.getTimeInMillis()));

        view.findViewById(R.id.imgPrev).setOnClickListener(view1 -> {
            // 이전일
            this.viewPager.setCurrentItem(PAGE_MIDDLE - 1, true);
        });

        view.findViewById(R.id.imgNext).setOnClickListener(view1 -> {
            // 다음일
            this.viewPager.setCurrentItem(PAGE_MIDDLE + 1, true);
        });

        view.findViewById(R.id.fabAdd).setOnClickListener(view12 -> {
            // 운동 추가
            onPopupExerciseAdd();
        });

        this.viewPager.setCurrentItem(PAGE_MIDDLE, false);

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
        return false;
    }

    /* 이전일 */
    private void prevDay() {
        this.calendar.add(Calendar.DATE, -1);
        this.txtDate.setText(Utils.getDate("yyyy-MM-dd", this.calendar.getTimeInMillis()));
    }

    /* 다음일 */
    private void nextDay() {
        this.calendar.add(Calendar.DATE, 1);
        this.txtDate.setText(Utils.getDate("yyyy-MM-dd", this.calendar.getTimeInMillis()));
    }

    /* 운동 등록 팝업창 호출 */
    private void onPopupExerciseAdd() {
        View popupView = View.inflate(this.context, R.layout.popup_exercise_add, null);
        ExerciseAddPopup popup = new ExerciseAddPopup(popupView, (view, bundle) -> {
            // 운동 등록
            inputExercise(bundle.getString("sports"), bundle.getInt("exercise_time"));
        });

        // Back 키 눌렸을때 닫기 위함
        popup.setFocusable(true);
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    /* 운동 등록 */
    private void inputExercise(String sports, int exerciseTime) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // METs 구하기
        double met = SportsData.getInstance().getMet(sports);

        // 칼로리 구하기
        int calorie = (int) (met * (exerciseTime / 60.0) * GlobalVariable.user.getWeight());

        final Exercise exercise = new Exercise(sports, exerciseTime, met, GlobalVariable.user.getWeight(),
                calorie, this.txtDate.getText().toString(), System.currentTimeMillis());

        // 운동 등록
        db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId)
                .collection(Constants.FirestoreCollectionName.EXERCISE)
                .add(exercise)
                .addOnSuccessListener(documentReference -> {
                    // 성공

                    // 일 만들기 (새로고침)
                    Bundle bundle = new Bundle();
                    bundle.putLong("time_millis", this.calendar.getTimeInMillis());
                    ((ITaskFragment) this.fragments.get(PAGE_MIDDLE)).task(Constants.FragmentTaskKind.REFRESH, bundle);
                })
                .addOnFailureListener(e -> {
                    // 등록 실패
                    Toast.makeText(this.context, R.string.msg_error, Toast.LENGTH_SHORT).show();
                });
    }
}
