package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView titleText, priceText, totalText, quantityText;
    private ImageButton backButton, cartButton, plusButton, minusButton;
    private Button addToCartButton;
    private BottomNavigationView bottomNavigationView;

    private int price = 0;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // 인텐트로 전달받은 상품 정보
        String productName = getIntent().getStringExtra("productName");
        price = getIntent().getIntExtra("productPrice", 0);

        // 위젯 연결
        titleText = findViewById(R.id.titleText);
        priceText = findViewById(R.id.priceText);
        totalText = findViewById(R.id.totalText);
        quantityText = findViewById(R.id.quantityText);
        backButton = findViewById(R.id.backButton);
        cartButton = findViewById(R.id.cartButton);
        plusButton = findViewById(R.id.plusButton);
        minusButton = findViewById(R.id.minusButton);
        addToCartButton = findViewById(R.id.addToCartButton);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // 내용 표시
        titleText.setText(productName);
        priceText.setText(price + "₩");
        totalText.setText(price * quantity + "₩");

        // 뒤로가기 버튼
        backButton.setOnClickListener(v -> finish());

        // 장바구니 버튼(기능 없음)
        cartButton.setOnClickListener(v ->
                Toast.makeText(this, "장바구니 기능은 아직 없습니다", Toast.LENGTH_SHORT).show());

        // 수량 조절
        plusButton.setOnClickListener(v -> {
            quantity++;
            quantityText.setText(String.valueOf(quantity));
            totalText.setText((price * quantity) + "₩");
        });

        minusButton.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityText.setText(String.valueOf(quantity));
                totalText.setText((price * quantity) + "₩");
            }
        });

        // 장바구니 추가
        addToCartButton.setOnClickListener(v -> {
            Toast.makeText(this, "장바구니에 추가되었습니다", Toast.LENGTH_SHORT).show();
            // TODO: DB 연결 시 API 요청
        });

        // 하단 네비게이션 처리 (하드코딩 수정)
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d("ProductDetail", "선택된 메뉴 ID: " + itemId);
            
            if (itemId == R.id.nav_home) {
                Log.d("ProductDetail", "홈 메뉴 선택");
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_search) {
                Log.d("ProductDetail", "검색 메뉴 선택");
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (itemId == R.id.nav_mypage) {
                Log.d("ProductDetail", "마이페이지 메뉴 선택");
                Toast.makeText(this, "마이페이지 기능 준비 중", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
}