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
        
        // HTTPS 서버 시작 (별도 스레드)
        new Thread(() -> {
            HttpsServer server = new HttpsServer();
            try {
                server.start();
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
