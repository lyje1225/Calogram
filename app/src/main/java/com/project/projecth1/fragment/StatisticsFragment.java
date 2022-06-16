//곽민승 개발자, 이윤제 개발자
package com.project.projecth1.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.project.projecth1.R;
import com.project.projecth1.adapter.MyFragmentStateAdapter;
import com.project.projecth1.fragment.abstracts.IFragment;
import com.project.projecth1.fragment.abstracts.ITaskFragment;
import com.project.projecth1.util.Constants;
import com.project.projecth1.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;

public class StatisticsFragment extends Fragment implements IFragment {
    //private static final String TAG = StatisticsFragment.class.getSimpleName();
    private static final String TAG = "projecth1";

    private Context context;

    private ArrayList<Fragment> fragments;

    private ViewPager2 viewPager;
    private TextView txtMonth;

    private Calendar calendar;
    private int pagePosition = 1;               // 디폴트 포지션

    private static final int PAGE_MIDDLE = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        this.viewPager = view.findViewById(R.id.viewPager);

        // 유지되는 페이지수를 설정
        // (3개의 페이지를 초반에 미리로딩한다. 페이지를 이동할때 마다 View 를 지우고 새로만드는 작업은 하지않게 된다)
        this.viewPager.setOffscreenPageLimit(3);

        // month 3개를 생성 (이전월, 현재월, 다음월)
        this.fragments = new ArrayList<>();
        for (int i=0; i<3; i++) {
            Fragment fragment = new StatisticsMonthFragment();
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
                        // 이전월
                        prevMonth();
                    } else if (pagePosition > PAGE_MIDDLE) {
                        // 다음월
                        nextMonth();
                    } else {
                        return;
                    }

                    // 페이지를 다시 가운데로 맞춘다 (3페이지로 계속 이전 / 다음 할 수 있게 하기위함)
                    viewPager.setCurrentItem(PAGE_MIDDLE, false);

                    // 월 만들기
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

        this.txtMonth = view.findViewById(R.id.txtMonth);

        // 현재월
        this.calendar = Calendar.getInstance();
        this.txtMonth.setText(Utils.getDate("yyyy-MM", this.calendar.getTimeInMillis()));

        view.findViewById(R.id.imgPrev).setOnClickListener(view1 -> {
            // 이전달
            this.viewPager.setCurrentItem(PAGE_MIDDLE - 1, true);
        });

        view.findViewById(R.id.imgNext).setOnClickListener(view1 -> {
            // 다음달
            this.viewPager.setCurrentItem(PAGE_MIDDLE + 1, true);
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

    /* 이전달 */
    private void prevMonth() {
        this.calendar.add(Calendar.MONTH, -1);
        this.txtMonth.setText(Utils.getDate("yyyy-MM", this.calendar.getTimeInMillis()));
    }

    /* 다음달 */
    private void nextMonth() {
        this.calendar.add(Calendar.MONTH, 1);
        this.txtMonth.setText(Utils.getDate("yyyy-MM", this.calendar.getTimeInMillis()));
    }
}
