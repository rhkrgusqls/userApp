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

        // 하단 네비게이션 처리 (하드코딩 수정)
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d("Search", "선택된 메뉴 ID: " + itemId);
            
            if (itemId == R.id.nav_home) {
                Log.d("Search", "홈 메뉴 선택");
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_search) {
                Log.d("Search", "검색 메뉴 선택 (현재 화면)");
                return true; // 현재 화면
            } else if (itemId == R.id.nav_mypage) {
                Log.d("Search", "마이페이지 메뉴 선택");
                Toast.makeText(this, "마이페이지 기능 준비 중", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
}