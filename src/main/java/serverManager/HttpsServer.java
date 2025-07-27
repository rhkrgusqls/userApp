package serverManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HttpsServer {
    private static final int PORT = 2010;
    
    public void start() throws Exception {
        // SSL 컨텍스트 생성 (자체 서명된 인증서 사용)
        SslContext sslCtx = createSSLContext();
        
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            
                            // SSL 핸들러 추가
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc()));
                            }
                            
                            p.addLast(new StringDecoder(CharsetUtil.UTF_8));
                            p.addLast(new StringEncoder(CharsetUtil.UTF_8));
                            p.addLast(new HttpsServerHandler());
                        }
                    });
            
            ChannelFuture f = b.bind(PORT).sync();
            System.out.println("[Log][Server] HTTPS server started on port " + PORT);
            
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    
    private SslContext createSSLContext() {
        try {
            // 자체 서명된 인증서 생성 (개발용)
            SslContext sslContext = SslContextBuilder.forServer(
                    new File("server-cert.pem"),
                    new File("server-key.pem")
            ).build();
            System.out.println("[Log][Server] SSL 컨텍스트 생성 성공");
            return sslContext;
        } catch (Exception e) {
            System.out.println("[Log][Server] SSL 인증서 파일을 찾을 수 없습니다.");
            System.out.println("[Log][Server] 오류: " + e.getMessage());
            System.out.println("[Log][Server] SSL 인증서를 생성해야 합니다!");
            throw new RuntimeException("SSL 인증서가 필요합니다", e);
        }
    }
    
    private static class HttpsServerHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println("[Log][Server] Received: " + msg);
            
            // 로그인 명령어 처리
            if (msg.startsWith("LOGIN%")) {
                String response = handleLogin(msg);
                ctx.writeAndFlush(response + "\n");
            } else {
                ctx.writeAndFlush("Unknown command: " + msg + "\n");
            }
        }
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("[Log][Server] Client connected: " + ctx.channel().remoteAddress());
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("[Log][Server] Client disconnected: " + ctx.channel().remoteAddress());
        }
        
        private String handleLogin(String command) {
            // LOGIN%id$testuser&password$1234% 형식 파싱
            try {
                String[] parts = command.split("%");
                if (parts.length == 3) { // LOGIN, id$testuser&password$1234, 빈문자열
                    String[] params = parts[1].split("&");
                    String id = params[0].split("\\$")[1];
                    String password = params[1].split("\\$")[1];
                    
                    System.out.println("[Log][Server] Login attempt - ID: " + id + ", Password: " + password);
                    
                                    // 데이터베이스에서 직접 인증 확인
                return queryDatabase(id, password);
                }
            } catch (Exception e) {
                System.out.println("[Log][Server] Error parsing login command: " + e.getMessage());
            }
            
            return "LOGIN_FAILED:Invalid command format";
        }
    }
    
    // 데이터베이스와 직접 통신하는 메서드
    private String queryDatabase(String id, String password) {
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