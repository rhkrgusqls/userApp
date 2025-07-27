package controller;

import model.LoginRequest;
import model.LoginResponse;
import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        // 데이터베이스에서 직접 인증 확인
        try {
            Socket socket = new Socket("34.47.125.114", 3306);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // 데이터베이스 인증 요청
            String dbRequest = "AUTH:" + request.getUsername() + ":" + request.getPassword();
            out.println(dbRequest);
            
            // 응답 받기
            String response = in.readLine();
            socket.close();
            
            // 데이터베이스 응답 처리
            if (response != null && response.startsWith("SUCCESS")) {
                String jwtToken = response.substring(8);
                return new LoginResponse(true, "로그인 성공", jwtToken);
            } else {
                return new LoginResponse(false, "아이디 또는 비밀번호가 잘못되었습니다.", null);
            }
            
        } catch (Exception e) {
            return new LoginResponse(false, "데이터베이스 연결 실패: " + e.getMessage(), null);
        }
    }
} 