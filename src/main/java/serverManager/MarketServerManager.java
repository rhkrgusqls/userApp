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

import io.netty.channel.*;

/**
 * MarketServerManager 클래스는 ServerManager를 상속받아
 * 마켓 서버 전용 네트워크 연결 및 데이터 처리 기능을 구현한다.
 */
public class MarketServerManager extends ServerManager{
    public MarketServerManager() throws Exception {
        serverIP = "34.47.125.114";  // 마켓 서버 IP
        serverPort = 2000;            // 마켓 서버 포트
        serverCertDir = "server-cert.pem";// 추후 수정
        initSslContext();             // SSL 컨텍스트 초기화 (부모 클래스 메서드)
        connectServer();              // 서버에 연결 (부모 클래스 메서드)
    }

    // 채널 핸들러 오버라이드: 서버와 통신 시 데이터 수신, 예외 처리, 연결 활성화 이벤트 처리
    @Override
    protected ChannelInboundHandler createChannelHandler() {
        return new SimpleChannelInboundHandler<io.netty.buffer.ByteBuf>() {
            // 수신한 데이터를 문자열로 변환 후 출력 (필요시 데이터 컨트롤러 역할 구현 가능)
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, io.netty.buffer.ByteBuf msg) {
                String received = msg.toString(java.nio.charset.StandardCharsets.UTF_8);
                System.out.println("Received from server: " + received);
                //이곳에 루프문 작성(마켓 API전용)
            }

            // 예외 발생 시 스택 트레이스 출력 및 채널 닫기
            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                cause.printStackTrace();
                ctx.close();
            }

            // 채널 활성화 시 호출: 연결 성공 시 초기 메시지 전송
            @Override
            public void channelActive(ChannelHandlerContext ctx) {
                System.out.println("Channel is active");
                sendMessage("Hello Netty TLS Server!");
            }
        };
    }
}
