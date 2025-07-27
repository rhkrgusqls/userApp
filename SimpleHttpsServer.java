import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;
import java.util.concurrent.*;

public class SimpleHttpsServer {
    private static final int PORT = 2010;
    private SSLServerSocket serverSocket;
    
    public void start() throws Exception {
        // SSL 컨텍스트 생성
        SSLContext sslContext = createSSLContext();
        
        // SSL 서버 소켓 팩토리 생성
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        
        // 서버 소켓 생성
        serverSocket = (SSLServerSocket) ssf.createServerSocket(PORT);
        System.out.println("[Log][Server] HTTPS 서버 시작됨 - 포트: " + PORT);
        
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
    }
    
    private void handleClient(SSLSocket clientSocket) {
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
    
    private SSLContext createSSLContext() throws Exception {
        // 자체 서명된 인증서 생성
        KeyPair keyPair = generateKeyPair();
        X509Certificate cert = generateSelfSignedCertificate(keyPair);
        
        // 키스토어 생성
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        keyStore.setKeyEntry("server", keyPair.getPrivate(), "password".toCharArray(), new Certificate[]{cert});
        
        // 키 매니저 팩토리 생성
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "password".toCharArray());
        
        // SSL 컨텍스트 생성
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        
        System.out.println("[Log][Server] SSL 컨텍스트 생성 완료");
        return sslContext;
    }
    
    private KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }
    
    private X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws Exception {
        // 간단한 자체 서명 인증서 생성
        java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
        
        // 임시로 더미 인증서 생성 (실제로는 BouncyCastle 등 사용)
        // 여기서는 간단한 방법으로 처리
        return null; // 실제 구현에서는 유효한 인증서 반환
    }
    
    private String handleLogin(String command) {
        try {
            String[] parts = command.split("%");
            if (parts.length == 2) {
                String[] params = parts[1].split("&");
                String id = params[0].split("\\$")[1];
                String password = params[1].split("\\$")[1];
                
                System.out.println("[Log][Server] 로그인 시도 - ID: " + id + ", Password: " + password);
                
                if ("testuser".equals(id) && "1234".equals(password)) {
                    return "LOGIN_SUCCESS:testuser";
                } else {
                    return "LOGIN_FAILED:Invalid credentials";
                }
            }
        } catch (Exception e) {
            System.out.println("[Log][Server] 로그인 명령어 파싱 오류: " + e.getMessage());
        }
        
        return "LOGIN_FAILED:Invalid command format";
    }
    
    public static void main(String[] args) {
        try {
            SimpleHttpsServer server = new SimpleHttpsServer();
            server.start();
        } catch (Exception e) {
            System.err.println("서버 시작 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 