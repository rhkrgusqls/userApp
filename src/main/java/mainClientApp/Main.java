package mainClientApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import java.io.*;
import java.net.*;
import serverManager.*;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        // Spring Boot 웹 서버 시작
        SpringApplication.run(Main.class, args);
        
        // Auth 서버 시작 (포트 2000) - 별도 스레드
        new Thread(() -> {
            AuthServer authServer = new AuthServer();
            try {
                authServer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        
        // API 서버 시작 (포트 2020) - 별도 스레드
        new Thread(() -> {
            ApiServer apiServer = new ApiServer();
            try {
                apiServer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    @EventListener
    public void onServletWebServerInitialized(ServletWebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        System.out.println("[Log][Server] Spring Boot HTTP server started on port " + port);
    }
}
