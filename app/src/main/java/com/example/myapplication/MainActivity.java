package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
        
        try {
            Log.d("MainActivity", "onCreate 시작");
            setContentView(R.layout.activity_main);
            Log.d("MainActivity", "레이아웃 로드 성공");

            recyclerView = findViewById(R.id.recyclerView);
            bottomNav = findViewById(R.id.bottomNavigation);
            Log.d("MainActivity", "위젯 찾기 성공");

            // RecyclerView 설정
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            productList = new ArrayList<>();

            // 서버에서 상품목록 요청
            loadProductsFromServer();

            adapter = new ProductAdapter(this, productList);
            recyclerView.setAdapter(adapter);
            Log.d("MainActivity", "RecyclerView 설정 완료");

            // BottomNavigation 설정 (하드코딩 수정)
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                Log.d("MainActivity", "선택된 메뉴 ID: " + itemId);
                
                if (itemId == R.id.nav_home) {
                    Log.d("MainActivity", "홈 메뉴 선택");
                    // 홈 화면은 이미 현재 화면이므로 아무것도 하지 않음
                    return true;
                } else if (itemId == R.id.nav_search) {
                    Log.d("MainActivity", "검색 메뉴 선택");
                    // SearchActivity로 이동
                    Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                    startActivity(searchIntent);
                    return true;
                } else if (itemId == R.id.nav_mypage) {
                    Log.d("MainActivity", "마이페이지 메뉴 선택");
                    // 마이페이지 기능 (나중에 구현)
                    android.widget.Toast.makeText(MainActivity.this, "마이페이지 기능 준비 중", android.widget.Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
            Log.d("MainActivity", "BottomNavigation 설정 완료");
            
            android.widget.Toast.makeText(this, "MainActivity 시작 성공!", android.widget.Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e("MainActivity", "MainActivity 로드 실패", e);
            android.widget.Toast.makeText(this, "MainActivity 오류: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }
    
    // 서버에서 상품목록 로드
    private void loadProductsFromServer() {
        try {
            NettyClient nettyClient = new NettyClient();
            nettyClient.getProducts(new NettyClient.ProductCallback() {
                @Override
                public void onSuccess(List<ProductItem> products) {
                    runOnUiThread(() -> {
                        productList.clear();
                        productList.addAll(products);
                        adapter.notifyDataSetChanged();
                        Log.d("MainActivity", "상품목록 로드 성공: " + products.size() + "개");
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Log.e("MainActivity", "상품목록 로드 실패: " + error);
                        // 오류 시 기본 상품 표시
                        loadDefaultProducts();
                    });
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "NettyClient 생성 실패", e);
            loadDefaultProducts();
        }
    }
    
    // 기본 상품목록 (오류 시 사용)
    private void loadDefaultProducts() {
        productList.add(new ProductItem("상품 로드 실패", 0, R.drawable.placeholder_image));
        adapter.notifyDataSetChanged();
    }
}