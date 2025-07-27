package serverManager;

public class ServerManager {
    private static final boolean USE_HTTPS = true; // HTTPS 서버 사용
    
    public static void main(String[] args) {
        try {
            if (USE_HTTPS) {
                System.out.println("[Log][Server] HTTPS 서버 시작 중...");
                HttpsServer httpsServer = new HttpsServer();
                httpsServer.start();
            } else {
                System.out.println("[Log][Server] HTTP 서버 시작 중...");
                HttpServer httpServer = new HttpServer();
                httpServer.start();
            }
        } catch (Exception e) {
            System.err.println("[Log][Server] 서버 시작 실패: " + e.getMessage());
            e.printStackTrace();
            
            // HTTPS 실패 시 HTTP로 폴백
            if (USE_HTTPS) {
                System.out.println("[Log][Server] HTTPS 실패, HTTP 서버로 폴백...");
                try {
                    HttpServer httpServer = new HttpServer();
                    httpServer.start();
                } catch (Exception fallbackException) {
                    System.err.println("[Log][Server] HTTP 서버도 실패: " + fallbackException.getMessage());
                }
            }
        }
    }
}
