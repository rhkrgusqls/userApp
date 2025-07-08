/*
ToDo:
    1.서버쪽을 epoll으로 설정 및 통신조율 및 이벤트 처리 테스트
    2.이벤트 컨트롤러 작성
    3.메시지전송 명령어 호출부 작성
    4.재연결 호출 작성
Issue:
    1.
 */


package serverManager;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class ServerManager {
    protected String serverIP;                      // 서버 IP 주소
    protected int serverPort;                       // 서버 포트 번호

    protected SslContext sslCtx;                    // SSL/TLS 설정 객체
    protected EventLoopGroup group;                 // 이벤트 루프 그룹 (I/O 이벤트 감시용 스레드풀)
    protected Channel channel;                       // 연결된 채널 (네트워크 입출력 통신 객체)
    protected String serverCertDir; // 서버 인증서 경로

    // SSLContext 초기화: 클라이언트용 SSL 설정 생성 및 신뢰할 인증서 지정
    protected void initSslContext() throws Exception {
        sslCtx = SslContextBuilder.forClient()
                .trustManager(new File(serverCertDir))  // 서버 인증서 파일 지정
                .build();
    }

    // 서버 연결 함수: 이벤트 루프 그룹 생성, Bootstrap 설정 및 서버 연결 수행
    public void connectServer() throws InterruptedException {
        group = new NioEventLoopGroup(); // NIO 기반 이벤트 루프 그룹 생성 (기본 스레드 수 = CPU 코어 수 * 2)
        try {
            Bootstrap b = new Bootstrap(); // 클라이언트 부트스트랩 객체 생성
            b.group(group)                 // 이벤트 루프 그룹 설정
                    .channel(NioSocketChannel.class) // NIO 소켓 채널 타입 지정 (TCP)
                    .handler(new ChannelInitializer<Channel>() { // 채널 초기화 시 파이프라인 설정
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(sslCtx.newHandler(ch.alloc())); // SSL/TLS 핸들러 추가 (암호화 계층)
                            p.addLast(createChannelHandler());       // 데이터 처리용 핸들러 추가 (서브클래스에서 오버라이드 가능)
                        }
                    });

            ChannelFuture future = b.connect(serverIP, serverPort).sync(); // 서버에 연결 시도 및 완료 대기
            channel = future.channel();                                     // 연결된 채널 저장
            channel.closeFuture().sync();                                   // 채널 닫힘 대기 (서버와 연결 종료 대기)
        } finally {
            group.shutdownGracefully(); // 이벤트 루프 그룹 종료, 자원 해제 및 스레드 종료
        }
    }

    // 기본 채널 핸들러 생성 함수 (서브클래스에서 필요에 따라 오버라이드)
    protected ChannelInboundHandler createChannelHandler() {
        return new SimpleChannelInboundHandler<ByteBuf>() {
            // 서버로부터 수신한 데이터를 UTF-8 문자열로 변환 후 출력
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                String received = msg.toString(StandardCharsets.UTF_8);
                System.out.println("Received: " + received);
            }

            // 예외 발생 시 스택 트레이스 출력 후 채널 닫기
            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                cause.printStackTrace();
                ctx.close();
            }

            // 채널이 활성화되었을 때 호출 (연결 성공 시)
            @Override
            public void channelActive(ChannelHandlerContext ctx) {
                System.out.println("Channel active");
            }
        };
    }

    // 메시지 전송 함수: 연결된 채널이 활성 상태일 때 UTF-8 문자열을 ByteBuf로 변환 후 전송
    public void sendMessage(String msg) {
        if (channel != null && channel.isActive()) {
            ByteBuf buf = Unpooled.copiedBuffer(msg, StandardCharsets.UTF_8);
            channel.writeAndFlush(buf);
            System.out.println("Sent: " + msg);
        } else {
            System.out.println("Channel is not active.");
        }
    }

    // 서버 연결 종료 함수: 이벤트 루프 그룹 종료
    public void disconnectServer() {
        if (group != null) {
            group.shutdownGracefully();
        }
    }
}
