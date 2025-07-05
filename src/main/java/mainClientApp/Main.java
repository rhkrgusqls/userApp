package mainClientApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.*;
import java.net.*;
import serverManager.*;
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        AuthServerManager manager = new AuthServerManager();
        try {
            manager.test();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
