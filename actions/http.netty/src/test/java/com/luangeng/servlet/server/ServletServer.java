package com.luangeng.servlet.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class ServletServer {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        new ServletServer().run(port);
    }

    public void run(final int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch)
                                throws Exception {

                            ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());

                            //將多個消息轉換為單一的FullHttpRequest或FullHttpResponse對象
                            ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));

                            ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());

                            ch.pipeline().addLast("ServletHandler", new ServletHandler());
                        }
                    });
            ChannelFuture future = b.bind("localhost", port).sync();
            System.out.println("Servlet Server startup.");
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}