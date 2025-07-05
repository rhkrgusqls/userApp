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

public class AuthServerManager extends ServerManager {
    public AuthServerManager() {
        serverIP = "34.47.125.114";
        serverPort = 2020;
        connectServer();
    }

    public void test() throws Exception {
        SslContext sslCtx = SslContextBuilder.forClient()
                .trustManager(new File("server-cert.pem"))
                .build();

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(sslCtx.newHandler(ch.alloc()));
                            p.addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                    String received = msg.toString(StandardCharsets.UTF_8);
                                    System.out.println("Received from server: " + received);
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    cause.printStackTrace();
                                    ctx.close();
                                }

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    String msg = "Hello Netty TLS Server!";
                                    ByteBuf buf = Unpooled.copiedBuffer(msg, StandardCharsets.UTF_8);
                                    ctx.writeAndFlush(buf);
                                    System.out.println("Sent to server: " + msg);
                                }
                            });
                        }
                    });

            ChannelFuture f = b.connect(serverIP, serverPort).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
