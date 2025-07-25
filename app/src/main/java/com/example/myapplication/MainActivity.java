package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<ProductItem> productList;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        bottomNav = findViewById(R.id.bottomNavigation);

        // 가로 스크롤 레이아웃
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 세로 2열 카드 배치

        productList = new ArrayList<>();

        // 서버 DB 연결 시 아래를 Retrofit 등으로 대체
        productList.add(new ProductItem("라운드넥 티셔츠", 8000, R.drawable.placeholder_image));
        productList.add(new ProductItem("로고 후드티", 13000, R.drawable.placeholder_image));
        productList.add(new ProductItem("기본 맨투맨", 11000, R.drawable.placeholder_image));
        productList.add(new ProductItem("데님 팬츠", 17000, R.drawable.placeholder_image));
        productList.add(new ProductItem("블랙 셋업", 29000, R.drawable.placeholder_image));

        adapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(adapter);

        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    return true;
                case R.id.menu_search:
                    // TODO - SearchActivity 연동
                    return true;
                case R.id.menu_mypage:
                    return true;
            }
            return false;
        });
    }
}