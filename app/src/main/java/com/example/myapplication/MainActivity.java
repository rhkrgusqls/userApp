package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private ImageView topProduct1, topProduct2, topProduct3;
    private ImageView bottomProduct1, bottomProduct2, bottomProduct3;
    private ImageView setProduct1, setProduct2, setProduct3;

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // XML 파일명에 맞게 설정

        // 상품 이미지 연결
        topProduct1 = findViewById(R.id.topProduct1);  // 아래에서 ID 할당할 거야
        topProduct2 = findViewById(R.id.topProduct2);
        topProduct3 = findViewById(R.id.topProduct3);

        // 예시: 상품 클릭 → 상세 페이지 이동
        topProduct1.setOnClickListener(v -> goToDetail("라운드넥 티셔츠", 8000));
        topProduct2.setOnClickListener(v -> goToDetail("로고 후드티", 13000));
        topProduct3.setOnClickListener(v -> goToDetail("기본 맨투맨", 11000));

        // 하단 네비게이션
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(navListener);
    }

    private void goToDetail(String productName, int price) {
        Log.d("ProductClick", "상품: " + productName + ", 가격: " + price + "원");

        Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
        intent.putExtra("productName", productName);
        intent.putExtra("productPrice", price);
        startActivity(intent);
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        switch (item.getItemId()) {
            case R.id.menu_home:
                Toast.makeText(this, "홈", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_search:
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            case R.id.menu_mypage:
                Toast.makeText(this, "마이페이지", Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    };
}