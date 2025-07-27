package com.example.myapplication;

import android.util.Log;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class NettyClient {
    private static final String TAG = "NettyClient";
    private static final String SERVER_HOST = "10.0.2.2"; // Android 에뮬레이터에서 호스트 접근
    private static final int SERVER_PORT = 2010;
    
    private Channel channel;
    private SslContext sslCtx;
    private CompletableFuture<String> responseFuture;
    
    public interface LoginCallback {
        void onSuccess(String response);
        void onError(String error);
    }
    
    public NettyClient() {
        try {
            initializeSSL();
        } catch (Exception e) {
            Log.e(TAG, "SSL 초기화 실패", e);
            // SSL 초기화 실패 시에도 클라이언트는 생성되도록 함
            sslCtx = null;
        }
    }
    
    private void initializeSSL() {
        try {
            // SSL 컨텍스트 초기화 (개발용 - 모든 인증서 신뢰)
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            Log.d(TAG, "SSL 컨텍스트 초기화 성공 (개발용)");
        } catch (Exception e) {
            Log.e(TAG, "SSL 초기화 실패", e);
            Log.d(TAG, "SSL 오류 상세: " + e.getMessage());
            // 예외를 던지지 않고 null로 설정
            sslCtx = null;
        }
    }
    

    
    public void login(String username, String password, LoginCallback callback) {
        new Thread(() -> {
            try {
                String loginCommand = String.format("LOGIN%%id$%s&password$%s%%", username, password);
                
                // SSL 핸드셰이크를 통한 로그인 시도
                Log.d(TAG, "SSL 핸드셰이크를 통한 로그인 시도");
                String response = sendCommand(loginCommand);
                Log.d(TAG, "sendCommand() 완료, 응답: " + response);
                
                if (response != null) {
                    // 서버 응답 형식 파싱
                    if (response.startsWith("login%&refreshToken$")) {
                        // 성공: "login%&refreshToken$[JWT토큰]"
                        String jwtToken = response.substring("login%&refreshToken$".length());
                        Log.d(TAG, "JWT 토큰 발급 성공: " + jwtToken);
                        callback.onSuccess(response);
                    } else if (response.startsWith("login%error%")) {
                        // 실패: "login%error%[오류메시지]"
                        String errorMessage = response.substring("login%error%".length());
                        Log.e(TAG, "로그인 실패: " + errorMessage);
                        callback.onError(errorMessage);
                    } else {
                        Log.w(TAG, "알 수 없는 응답 형식: " + response);
                        callback.onError("알 수 없는 서버 응답");
                    }
                } else {
                    callback.onError("서버 응답 없음");
                }
            } catch (Exception e) {
                Log.e(TAG, "로그인 오류", e);
                String errorMessage = "네트워크 오류: " + e.getMessage();
                if (e instanceof java.util.concurrent.TimeoutException) {
                    errorMessage = "서버 응답 시간 초과";
                } else if (e instanceof javax.net.ssl.SSLException) {
                    errorMessage = "SSL 연결 오류: " + e.getMessage();
                }
                callback.onError(errorMessage);
            }
        }).start();
    }
    
    // SSL 없이 연결하는 메서드 추가
    private String sendCommandWithoutSSL(String command) throws Exception {
        responseFuture = new CompletableFuture<>();
        
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            // SSL 없이 연결
                            p.addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                    String received = msg.toString(StandardCharsets.UTF_8);
                                    Log.d(TAG, "HTTP 서버 응답: " + received);
                                    responseFuture.complete(received);
                                }
                                
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    Log.e(TAG, "HTTP 채널 오류", cause);
                                    responseFuture.completeExceptionally(cause);
                                    ctx.close();
                                }
                                
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    Log.d(TAG, "HTTP 서버에 연결됨, 명령어 전송: " + command);
                                    ByteBuf buf = Unpooled.copiedBuffer(command, StandardCharsets.UTF_8);
                                    ctx.writeAndFlush(buf);
                                }
                            });
                        }
                    });
            
            ChannelFuture f = b.connect(SERVER_HOST, SERVER_PORT).sync();
            channel = f.channel();
            
            // 응답 대기 (5초 타임아웃)
            String response = responseFuture.get(5, TimeUnit.SECONDS);
            return response;
            
        } finally {
            group.shutdownGracefully();
        }
    }
    
    private String sendCommand(String command) throws Exception {
        responseFuture = new CompletableFuture<>();
        
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // SSL 핸드셰이크를 위해 타임아웃 증가
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            
                            // SSL 핸들러 추가 (반드시 필요)
                            if (sslCtx != null) {
                                Log.d(TAG, "SSL 핸들러 추가");
                                p.addLast(sslCtx.newHandler(ch.alloc(), SERVER_HOST, SERVER_PORT));
                            } else {
                                Log.e(TAG, "SSL 컨텍스트가 없습니다");
                                throw new RuntimeException("SSL 컨텍스트가 없습니다");
                            }
                            
                            p.addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                    String received = msg.toString(StandardCharsets.UTF_8);
                                    Log.d(TAG, "SSL을 통한 서버 응답: " + received);
                                    Log.d(TAG, "응답 길이: " + received.length());
                                    Log.d(TAG, "응답 시작 부분: " + (received.length() > 20 ? received.substring(0, 20) + "..." : received));
                                    
                                    if (received.trim().isEmpty()) {
                                        Log.w(TAG, "빈 응답 받음");
                                    }
                                    
                                    // 응답 형식 확인
                                    if (received.startsWith("login%&refreshToken$")) {
                                        Log.d(TAG, "JWT 토큰 응답 감지");
                                    } else if (received.startsWith("login%error%")) {
                                        Log.e(TAG, "오류 응답 감지");
                                    } else {
                                        Log.w(TAG, "알 수 없는 응답 형식: " + received);
                                    }
                                    
                                    // 응답 완료 처리
                                    Log.d(TAG, "responseFuture.complete() 호출");
                                    responseFuture.complete(received);
                                    Log.d(TAG, "responseFuture.complete() 완료");
                                }
                                
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    Log.e(TAG, "SSL 채널 오류", cause);
                                    responseFuture.completeExceptionally(cause);
                                    ctx.close();
                                }
                                
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    Log.d(TAG, "SSL 핸드셰이크 완료, 명령어 전송: " + command);
                                    ByteBuf buf = Unpooled.copiedBuffer(command, StandardCharsets.UTF_8);
                                    ctx.writeAndFlush(buf);
                                }
                                
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) {
                                    Log.d(TAG, "SSL 연결 종료");
                                }
                            });
                        }
                    });
            
            Log.d(TAG, "SSL 서버에 연결 시도 중...");
            ChannelFuture f = b.connect(SERVER_HOST, SERVER_PORT).sync();
            channel = f.channel();
            
            Log.d(TAG, "SSL 연결 성공, 응답 대기 중...");
            // 응답 대기 (10초 타임아웃 - SSL 핸드셰이크 고려)
            Log.d(TAG, "responseFuture.get() 호출 시작");
            String response = responseFuture.get(10, TimeUnit.SECONDS);
            Log.d(TAG, "responseFuture.get() 완료, 응답: " + response);
            return response;
            
        } finally {
            group.shutdownGracefully();
        }
    }
} 