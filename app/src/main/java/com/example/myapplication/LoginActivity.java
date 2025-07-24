package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText idEditText;
    private EditText pwEditText;
    private Button loginButton;
    private TextView errorTextView;
    private TextView registerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 위젯 연결
        idEditText = findViewById(R.id.idEditText);
        pwEditText = findViewById(R.id.pwEditText);
        loginButton = findViewById(R.id.loginButton);
        errorTextView = findViewById(R.id.errorTextView);
        registerText = findViewById(R.id.registerText);

        // 로그인 버튼 클릭 이벤트
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = idEditText.getText().toString().trim();
                String pw = pwEditText.getText().toString().trim();

                // TODO 서버 연동
                // 예시: boolean isSuccess = maincontroller.login(id, pw);

                if (id.equals("test") && pw.equals("1234")) {
                    Log.d("Login", "로그인 성공: " + id);
                    errorTextView.setVisibility(View.GONE);

                    // 메인 화면으로 이동
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    Log.d("Login", "로그인 실패");
                    errorTextView.setVisibility(View.VISIBLE);
                }
            }
        });

        // 회원가입 텍스트 클릭
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 회원가입 화면으로 이동 (나중에 구현)
                Toast.makeText(LoginActivity.this, "회원가입 화면으로 이동 예정", Toast.LENGTH_SHORT).show();
            }
        });
    }
}