//장웅희 개발자
package com.project.projecth1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.project.projecth1.fragment.AccountFragment;
import com.project.projecth1.fragment.CalorieFragment;
import com.project.projecth1.fragment.ExerciseFragment;
import com.project.projecth1.fragment.StatisticsFragment;
import com.project.projecth1.fragment.abstracts.IFragment;
import com.project.projecth1.util.Constants;
import com.project.projecth1.util.SharedPreferencesUtils;

public class MainActivity extends AppCompatActivity {
    //private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG = "projecth1";

    private BackPressHandler backPressHandler;

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 종료 핸들러
        this.backPressHandler = new BackPressHandler(this);

        // 네비게이션 뷰 (하단에 표시되는 메뉴)
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);
        bottomNavigationView.setOnItemSelectedListener(mItemSelectedListener);

        // 칼로리를 첫 화면으로 표시
        setTitle(R.string.menu_calorie);
        this.fragment = new CalorieFragment();

        // Fragment 메니저를 이용해서 layContent 레이아웃에 Fragment 넣기
        getSupportFragmentManager().beginTransaction().add(R.id.layContent, this.fragment).commit();
    }

    @Override
    public void onBackPressed() {
        this.backPressHandler.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // main 메뉴 생성
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            // 로그아웃
            new AlertDialog.Builder(this)
                    .setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
                        // 로그아웃
                        logout();
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .setCancelable(false)
                    .setTitle(R.string.dialog_title_logout)
                    .setMessage(R.string.dialog_msg_logout)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* 로그아웃 */
    private void logout() {
        // Document Id 값 clear
        SharedPreferencesUtils.getInstance(this).put(Constants.SharedPreferencesName.USER_DOCUMENT_ID, "");

        // 로그인화면으로 이동
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /* BottomNavigationView 선택 리스너 */
    private final NavigationBarView.OnItemSelectedListener mItemSelectedListener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // 실행중인지 체크
            if (((IFragment) fragment).isExecuted()) {
                return false;
            }

            switch (item.getItemId()) {
                case R.id.menu_button_calorie:
                    // 칼로리
                    setTitle(R.string.menu_calorie);
                    fragment = new CalorieFragment();
                    break;
                case R.id.menu_button_exercise:
                    // 운동
                    setTitle(R.string.menu_exercise);
                    fragment = new ExerciseFragment();
                    break;
                case R.id.menu_button_statistics:
                    // 통계
                    setTitle(R.string.menu_statistics);
                    fragment = new StatisticsFragment();
                    break;
                case R.id.menu_button_account:
                    // 정보
                    setTitle(R.string.menu_account);
                    fragment = new AccountFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.layContent, fragment).commit();
            return true;
        }
    };

    /* Back Press Class */
    private class BackPressHandler {
        private final Context context;
        private Toast toast;

        private long backPressedTime = 0;

        public BackPressHandler(Context context) {
            this.context = context;
        }

        public void onBackPressed() {
            if (System.currentTimeMillis() > this.backPressedTime + (Constants.LoadingDelay.LONG * 2)) {
                this.backPressedTime = System.currentTimeMillis();

                this.toast = Toast.makeText(this.context, R.string.msg_back_press_end, Toast.LENGTH_SHORT);
                this.toast.show();
                return;
            }

            if (System.currentTimeMillis() <= this.backPressedTime + (Constants.LoadingDelay.LONG * 2)) {
                // 종료
                moveTaskToBack(true);
                finish();
                this.toast.cancel();
            }
        }
    }
}