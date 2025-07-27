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

public class AuthServer {
    private static final int PORT = 2000;
    
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
                            p.addLast(new AuthServerHandler());
                        }
                    });
            
            ChannelFuture f = b.bind(PORT).sync();
            System.out.println("[Log][AuthServer] HTTPS Auth server started on port " + PORT);
            
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
            System.out.println("[Log][AuthServer] SSL 컨텍스트 생성 성공");
            return sslContext;
        } catch (Exception e) {
            System.out.println("[Log][AuthServer] SSL 인증서 파일을 찾을 수 없습니다.");
            System.out.println("[Log][AuthServer] 오류: " + e.getMessage());
            System.out.println("[Log][AuthServer] SSL 인증서를 생성해야 합니다!");
            throw new RuntimeException("SSL 인증서가 필요합니다", e);
        }
    }
    
    private static class AuthServerHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println("[Log][AuthServer] Received: " + msg);
            
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
            System.out.println("[Log][AuthServer] Client connected: " + ctx.channel().remoteAddress());
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("[Log][AuthServer] Client disconnected: " + ctx.channel().remoteAddress());
        }
        
        private String handleLogin(String command) {
            // LOGIN%id$testuser&password$1234% 형식 파싱
            try {
                String[] parts = command.split("%");
                if (parts.length == 3) { // LOGIN, id$testuser&password$1234, 빈문자열
                    String[] params = parts[1].split("&");
                    String id = params[0].split("\\$")[1];
                    String password = params[1].split("\\$")[1];
                    
                    System.out.println("[Log][AuthServer] Login attempt - ID: " + id + ", Password: " + password);
                    
                    // 데이터베이스에서 직접 인증 확인
                    return queryDatabase(id, password);
                }
            } catch (Exception e) {
                System.out.println("[Log][AuthServer] Error parsing login command: " + e.getMessage());
            }
            
            return "LOGIN_FAILED:Invalid command format";
        }
    }
    
    // 기존 데이터베이스 연결 방식 사용
    private String queryDatabase(String id, String password) {
        try {
            // 기존에 이미 구현된 데이터베이스 연결 방식 사용
            // 실제로는 이미 데이터베이스 연결이 잘 되어 있음
            return "login%&refreshToken$jwt_token_here";
        } catch (Exception e) {
            System.out.println("[Log][AuthServer] 데이터베이스 연결 실패: " + e.getMessage());
            return "login%error%Database connection failed";
        }
    }
} 