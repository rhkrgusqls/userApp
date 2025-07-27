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
import java.util.*;
import model.Product;

public class ApiServer {
    private static final int PORT = 2020;
    
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
                            p.addLast(new ApiServerHandler());
                        }
                    });
            
            ChannelFuture f = b.bind(PORT).sync();
            System.out.println("[Log][ApiServer] HTTPS API server started on port " + PORT);
            
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
            System.out.println("[Log][ApiServer] SSL 컨텍스트 생성 성공");
            return sslContext;
        } catch (Exception e) {
            System.out.println("[Log][ApiServer] SSL 인증서 파일을 찾을 수 없습니다.");
            System.out.println("[Log][ApiServer] 오류: " + e.getMessage());
            System.out.println("[Log][ApiServer] SSL 인증서를 생성해야 합니다!");
            throw new RuntimeException("SSL 인증서가 필요합니다", e);
        }
    }
    
    private static class ApiServerHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println("[Log][ApiServer] Received: " + msg);
            
            // 상품목록 요청 처리
            if (msg.startsWith("GET_PRODUCT_LIST%")) {
                String response = getAllProductsFromDB();
                ctx.writeAndFlush(response + "\n");
            } else {
                ctx.writeAndFlush("Unknown command: " + msg + "\n");
            }
        }
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("[Log][ApiServer] Client connected: " + ctx.channel().remoteAddress());
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("[Log][ApiServer] Client disconnected: " + ctx.channel().remoteAddress());
        }
    }
    
    // 상품목록 조회 (실제 데이터베이스에서)
    private String getAllProductsFromDB() {
        try {
            // 실제 데이터베이스에서 상품목록 조회
            List<Product> products = getAllProducts();
            
            // 응답 형식으로 변환: "productList%&products$id,상품명,재고수량,가격|id2,상품명2,재고수량2,가격2"
            StringBuilder response = new StringBuilder("productList%&products$");
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                response.append(p.getProductID()).append(",")
                       .append(p.getProductName()).append(",")
                       .append(p.getProductStock()).append(",")
                       .append(p.getProductPrice());
                
                if (i < products.size() - 1) {
                    response.append("|");
                }
            }
            
            return response.toString();
            
        } catch (Exception e) {
            System.out.println("[Log][ApiServer] 상품목록 조회 실패: " + e.getMessage());
            return "productList%error%Database connection failed";
        }
    }
    
    // 프록시 서버에 명령어만 전송하고 응답 받기
    private List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        
        try {
            // localhost의 프록시 서버에 명령어만 전송
            Socket socket = new Socket("localhost", 8080);
            
            // 상품목록 요청 명령어만 전송
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("GET_PRODUCT_LIST%");
            
            // 프록시 서버로부터 응답 받기
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            
            System.out.println("[Log][ApiServer] 프록시 서버 응답: " + response);
            
            if (response != null && response.startsWith("productList%&products$")) {
                String productsData = response.substring("productList%&products$".length());
                String[] products = productsData.split("\\|");
                
                for (String product : products) {
                    String[] parts = product.split(",");
                    if (parts.length >= 4) {
                        Product p = new Product();
                        p.setProductID(Integer.parseInt(parts[0]));
                        p.setProductName(parts[1]);
                        p.setProductStock(Integer.parseInt(parts[2]));
                        p.setProductPrice(Integer.parseInt(parts[3]));
                        productList.add(p);
                    }
                }
            }
            
            socket.close();
            System.out.println("[Log][ApiServer] 프록시 서버에서 상품목록 받기 완료: " + productList.size() + "개");
            
        } catch (Exception e) {
            System.out.println("[Log][ApiServer] 프록시 서버 연결 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        return productList;
    }
} 