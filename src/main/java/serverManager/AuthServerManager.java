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

public class AuthServerManager extends ServerManager {

    public AuthServerManager() throws Exception {
        serverIP = "34.47.125.114";
        serverPort = 2020;
        serverCertDir = "server-cert.pem";
        initSslContext();
        connectServer();
    }

    @Override
    protected ChannelInboundHandler createChannelHandler() {
        return new SimpleChannelInboundHandler<io.netty.buffer.ByteBuf>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, io.netty.buffer.ByteBuf msg) {
                String received = msg.toString(java.nio.charset.StandardCharsets.UTF_8);
                System.out.println("Received from server: " + received);
                // 컨트롤러 역할 등 커스터마이징 가능
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                cause.printStackTrace();
                ctx.close();
            }

            @Override
            public void channelActive(ChannelHandlerContext ctx) {
                System.out.println("Channel is active");
                sendMessage("Hello Netty TLS Server!");
            }
        };
    }
}

