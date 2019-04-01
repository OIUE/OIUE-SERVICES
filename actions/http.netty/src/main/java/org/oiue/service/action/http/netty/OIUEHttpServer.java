package org.oiue.service.action.http.netty;

import java.util.Map;

import org.junit.Test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class OIUEHttpServer {
    private Channel channel;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private int port;
    public OIUEHttpServer(int port) {
        this.port = port;
    }
    public void run() throws Exception {
        try {
            //创建ServerBootstrap实例
            ServerBootstrap b = new ServerBootstrap();  
           //设置并绑定Reactor线程池
            b.group(bossGroup, workerGroup)
            //设置并绑定服务端Channel
             .channel(NioServerSocketChannel.class)  
             .childHandler(new WebsocketServerInitializer())   
             .option(ChannelOption.SO_BACKLOG, 128)           
             .childOption(ChannelOption.SO_KEEPALIVE, true);
            System.out.println("WebsocketChatServer Start:" + port);
            ChannelFuture f = b.bind(port).sync();//// 服务器异步创建绑定
            channel = f.channel();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            channel.closeFuture().syncUninterruptibly();
            System.out.println("WebsocketChatServer Stop:" + port);
        }
    }

    public void destroy() {
        if (channel != null){
            channel.close();
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        System.out.println("WebsocketChatServer Destroy:" + port);
    }


    public void updated(Map props) {
        int port=8080;
        try {
			new OIUEHttpServer(port).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    @Test
    public void testStart() {
    	 int port=8080;
         try {
 			new OIUEHttpServer(port).run();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
    }
    public static void main(String[] args) throws Exception {
        int port=8080;
        new OIUEHttpServer(port).run();
    }
}