package serverManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class HttpServer {
    private static final int PORT = 2010;
    
    public void start() throws Exception {
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
                            p.addLast(new StringDecoder(CharsetUtil.UTF_8));
                            p.addLast(new StringEncoder(CharsetUtil.UTF_8));
                            p.addLast(new HttpServerHandler());
                        }
                    });
            
            ChannelFuture f = b.bind(PORT).sync();
            System.out.println("[Log][Server] HTTP server started on port " + PORT);
            
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    
    private static class HttpServerHandler extends SimpleChannelInboundHandler<String> {
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
                    
                    if ("testuser".equals(id) && "1234".equals(password)) {
                        return "LOGIN_SUCCESS:testuser";
                    } else {
                        return "LOGIN_FAILED:Invalid credentials";
                    }
                }
            } catch (Exception e) {
                System.out.println("[Log][Server] Error parsing login command: " + e.getMessage());
            }
            
            return "LOGIN_FAILED:Invalid command format";
        }
    }
} 