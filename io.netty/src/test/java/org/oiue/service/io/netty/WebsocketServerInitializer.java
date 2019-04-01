package org.oiue.service.io.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebsocketServerInitializer extends
        ChannelInitializer<SocketChannel> {
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //对ByteBuf数据流进行处理，转换成http的对象
        /*pipeline.addLast(new ServerHandler());*/
    /*  pipeline.addLast(new Routing());*/
        pipeline.addLast(new HttpServerCodec());// Http消息编码解码
        pipeline.addLast(new HttpObjectAggregator(64*1024));
        pipeline.addLast(new ChunkedWriteHandler());
        /*pipeline.addLast(new HttpRequestHandler("/ws"));
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        pipeline.addLast(new ServerHandler());*/
        pipeline.addLast(new SocketHandel());//自定义处理类
    }
}