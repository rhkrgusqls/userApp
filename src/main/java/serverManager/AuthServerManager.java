package serverManager;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

//import io.netty.channel.epoll.EpollEventLoopGroup;// 리눅스 전용 epoll 이벤트 루프 그룹
//import io.netty.channel.kqueue.KQueueEventLoopGroup;// macOS/BSD 전용 kqueue 이벤트 루프 그룹

import java.io.File;
import java.nio.charset.StandardCharsets;

public class AuthServerManager extends ServerManager {
    public AuthServerManager() {
        serverIP = "34.47.125.114";
        serverPort = 2020;
        connectServer(); // 서버 접속 메서드 호출
    }

    public void test() throws Exception {
        SslContext sslCtx = SslContextBuilder.forClient() //클라이언트 인증서. 키 기반 설정
                .trustManager(new File("server-cert.pem")) //신용할 암호화 정보 파일
                .build(); //SSLContext 인스턴스 생성

        EventLoopGroup group = new NioEventLoopGroup(); //Selector 기반 그룹 비차단 소켓채널 I/O 감시 스레드 기본값 2코어
        //EventLoopGroup group = new EpollEventLoopGroup();//epoll 시스템콜 이벤트기반 비차단I/O 성능(매우높음) 적용가능 플렛폼(리눅스) 서버쪽에 적용할 네트워크
        //EventLoopGroup group = new KQueueEventLoopGroup();//kqueue 시스템콜 이벤트기반 비차단I/O 성능(매우높음) 플렛폼macOS, BSD
        //EventLoopGroup group = new DefaultEventLoopGroup(); 네트워크 I/O감시 없음, 일반작업처리(디코딩,CPU처리작업)
        try {
            Bootstrap b = new Bootstrap();//클라이언트 부스트랩 생성(소켓 연결 설정)
            b.group(group)//이벤트 루프 그룹 설정
                    .channel(NioSocketChannel.class)//소켓채널타입 지정
                    .handler(new ChannelInitializer<Channel>() { //채널 초기화 파이프라인 설정
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();//파이프라인 객체 가져오기
                            p.addLast(sslCtx.newHandler(ch.alloc()));//SSL/TLS 핸들러 추기:소켓에 SSL 암호화 계층 추가
                            p.addLast(new SimpleChannelInboundHandler<ByteBuf>() {//데이터 처리용 핸들러 추가:서버로부터 받은 데이터 처리 및 이벤트 핸들링
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                    String received = msg.toString(StandardCharsets.UTF_8);//문자열 변환설정
                                    System.out.println("Received from server: " + received);
                                    /** 이쪽에 문자열 변환 및 함수실행 작성 명령어코드/데이터 받기 **/
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    cause.printStackTrace(); //예외스택 출력
                                    ctx.close();//연결종료
                                }

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    String msg = "Hello Netty TLS Server!";
                                    ByteBuf buf = Unpooled.copiedBuffer(msg, StandardCharsets.UTF_8);//문자열 변환설정
                                    ctx.writeAndFlush(buf);//변환 문자열 전송 및 플러시
                                    System.out.println("Sent to server: " + msg);

                                    /**
                                    private Channel channel;  // 클래스 멤버 변수로 채널 참조 저장

                                    public void sendMessage(String msg) {
                                        if (channel != null && channel.isActive()) {
                                            ByteBuf buf = Unpooled.copiedBuffer(msg, StandardCharsets.UTF_8);
                                            channel.writeAndFlush(buf);
                                            System.out.println("Sent to server: " + msg);
                                        } else {
                                            System.out.println("Channel is not active.");
                                        }
                                    }
                                     **/
                                }
                            });
                        }
                    });

            ChannelFuture f = b.connect(serverIP, serverPort).sync();//서버에 연결시도 및 동기화 대기
            f.channel().closeFuture().sync();//채널 닫힐때까지 대기
        } finally {
            group.shutdownGracefully();//이벤트 루프 그룹 종료:리소스 해제 및 스레드 종료
        }
    }
}
