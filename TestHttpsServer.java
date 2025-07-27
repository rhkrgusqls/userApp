import java.io.*;
import java.net.*;
import java.security.*;
import javax.net.ssl.*;

public class TestHttpsServer {
    private static final int PORT = 2010;
    
    public static void main(String[] args) {
        try {
            // SSL 컨텍스트 생성 (기본 설정)
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            
            // SSL 서버 소켓 팩토리 생성
            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            
            // 서버 소켓 생성
            SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(PORT);
            System.out.println("[Log][Server] HTTPS 서버 시작됨 - 포트: " + PORT);
            System.out.println("[Log][Server] SSL 핸드셰이크 대기 중...");
            
            // 클라이언트 연결 대기
            while (true) {
                try {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    System.out.println("[Log][Server] 클라이언트 연결됨: " + clientSocket.getInetAddress());
                    
                    // 클라이언트 처리를 별도 스레드에서 실행
                    new Thread(() -> handleClient(clientSocket)).start();
                } catch (Exception e) {
                    System.err.println("[Log][Server] 클라이언트 처리 오류: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("서버 시작 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void handleClient(SSLSocket clientSocket) {
        try {
            // SSL 핸드셰이크 시작
            System.out.println("[Log][Server] SSL 핸드셰이크 시작...");
            clientSocket.startHandshake();
            System.out.println("[Log][Server] SSL 핸드셰이크 완료!");
            
            // 데이터 스트림 생성
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            // 클라이언트로부터 데이터 읽기
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("[Log][Server] 받은 데이터: " + inputLine);
                
                // 로그인 명령어 처리
                if (inputLine.startsWith("LOGIN%")) {
                    String response = handleLogin(inputLine);
                    out.println(response);
                    System.out.println("[Log][Server] 응답 전송: " + response);
                } else {
                    out.println("Unknown command: " + inputLine);
                }
            }
            
        } catch (Exception e) {
            System.err.println("[Log][Server] 클라이언트 처리 중 오류: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("[Log][Server] 클라이언트 연결 종료");
            } catch (IOException e) {
                System.err.println("[Log][Server] 소켓 종료 오류: " + e.getMessage());
            }
        }
    }
    
    private static String handleLogin(String command) {
        try {
            String[] parts = command.split("%");
            if (parts.length == 3) { // LOGIN, id$testuser&password$1234, 빈문자열
                String[] params = parts[1].split("&");
                String id = params[0].split("\\$")[1];
                String password = params[1].split("\\$")[1];
                
                System.out.println("[Log][Server] 로그인 시도 - ID: " + id + ", Password: " + password);
                
                // 데이터베이스에서 직접 인증 확인
                return queryDatabase(id, password);
            }
        } catch (Exception e) {
            System.out.println("[Log][Server] 로그인 명령어 파싱 오류: " + e.getMessage());
        }
        
        return "LOGIN_FAILED:Invalid command format";
    }
    
    // 데이터베이스와 직접 통신하는 메서드
    private static String queryDatabase(String id, String password) {
        try {
            // 데이터베이스 서버 연결 (34.47.125.114)
            Socket socket = new Socket("34.47.125.114", 3306); // MySQL 기본 포트
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // 데이터베이스 인증 요청
            String dbRequest = "AUTH:" + id + ":" + password;
            out.println(dbRequest);
            
            // 응답 받기
            String response = in.readLine();
            
            socket.close();
            
            // 데이터베이스 응답 처리
            if (response != null && response.startsWith("SUCCESS")) {
                String jwtToken = response.substring(8); // JWT 토큰 추출
                return "login%&refreshToken$" + jwtToken;
            } else {
                return "login%error%Invalid credentials";
            }
            
        } catch (Exception e) {
            System.out.println("[Log][Server] 데이터베이스 연결 실패: " + e.getMessage());
            return "login%error%Database connection failed";
        }
    }
} 