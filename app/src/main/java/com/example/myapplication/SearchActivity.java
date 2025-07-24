package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SearchActivity extends AppCompatActivity {

    private ImageButton backButton;
    private EditText searchInput;
    private ImageView searchIcon;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 위젯 연결
        backButton = findViewById(R.id.backButton);
        searchInput = findViewById(R.id.searchInput);
        searchIcon = findViewById(R.id.searchIcon);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // 뒤로가기
        backButton.setOnClickListener(v -> finish());

        // 검색 아이콘 클릭
        searchIcon.setOnClickListener(v -> {
            String keyword = searchInput.getText().toString().trim();
            if (!keyword.isEmpty()) {
                Toast.makeText(this, "검색어: " + keyword, Toast.LENGTH_SHORT).show();
                Log.d("Search", "사용자 검색어: " + keyword);
                // TODO: 실제 검색 결과 화면으로 이동하거나 리스트 업데이트
            } else {
                Toast.makeText(this, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show();
            }
        });

        // 하단 네비게이션 처리
        bottomNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    startActivity(new Intent(this, MainActivity.class));
                    return true;
                case R.id.menu_search:
                    return true; // 현재 화면
                case R.id.menu_mypage:
                    Toast.makeText(this, "마이페이지는 아직 없습니다", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });
    }
}