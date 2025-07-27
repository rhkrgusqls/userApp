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

                if (id.isEmpty() || pw.isEmpty()) {
                    errorTextView.setText("아이디와 비밀번호를 입력해주세요.");
                    errorTextView.setVisibility(View.VISIBLE);
                    return;
                }

                // 실제 서버 통신으로 변경
                Log.d("Login", "서버에 로그인 요청: " + id);

                // 로딩 표시
                loginButton.setEnabled(false);
                loginButton.setText("로그인 중...");

                                // Netty 클라이언트로 서버 연동
                try {
                    NettyClient nettyClient = new NettyClient();
                    nettyClient.login(id, pw, new NettyClient.LoginCallback() {
                        @Override
                        public void onSuccess(String response) {
                            Log.d("Login", "onSuccess() 콜백 호출됨, 응답: " + response);
                            runOnUiThread(() -> {
                                try {
                                    Log.d("Login", "runOnUiThread() 실행, 로그인 성공: " + response);
                                    errorTextView.setVisibility(View.GONE);
                                    loginButton.setEnabled(true);
                                    loginButton.setText("Login");

                                    // JWT 토큰 저장 (나중에 사용)
                                    String jwtToken = "";
                                    if (response.startsWith("login%&refreshToken$")) {
                                        jwtToken = response.substring("login%&refreshToken$".length());
                                        Log.d("Login", "JWT 토큰 저장: " + jwtToken);
                                        // TODO: SharedPreferences에 토큰 저장
                                    }

                                    // 임시로 토스트 메시지 표시
                                    if (!jwtToken.isEmpty()) {
                                        Toast.makeText(LoginActivity.this, "로그인 성공! JWT 토큰: " + jwtToken.substring(0, 20) + "...", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_LONG).show();
                                    }
                                    
                                    // 메인 화면으로 이동
                                    Log.d("Login", "MainActivity로 이동");
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    Log.d("Login", "MainActivity 시작 완료");
                                    finish();
                                    Log.d("Login", "LoginActivity 종료");
                                } catch (Exception e) {
                                    Log.e("Login", "UI 업데이트 오류", e);
                                    errorTextView.setText("UI 오류: " + e.getMessage());
                                    errorTextView.setVisibility(View.VISIBLE);
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                try {
                                    Log.e("Login", "로그인 실패: " + error);
                                    errorTextView.setText("로그인 실패: " + error);
                                    errorTextView.setVisibility(View.VISIBLE);
                                    loginButton.setEnabled(true);
                                    loginButton.setText("Login");
                                } catch (Exception e) {
                                    Log.e("Login", "오류 메시지 표시 실패", e);
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    Log.e("Login", "NettyClient 생성 실패", e);
                    runOnUiThread(() -> {
                        try {
                            errorTextView.setText("클라이언트 초기화 실패: " + e.getMessage());
                            errorTextView.setVisibility(View.VISIBLE);
                            loginButton.setEnabled(true);
                            loginButton.setText("Login");
                        } catch (Exception uiError) {
                            Log.e("Login", "UI 오류 처리 실패", uiError);
                        }
                    });
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