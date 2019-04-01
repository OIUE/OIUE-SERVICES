package org.oiue.service.action.http.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

public class BaseHttpHandler extends SimpleChannelInboundHandler<Object> {
	public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
		Channel incoming = ctx.channel();
		System.out.println("Client:" + incoming.remoteAddress() + "received msg：" + msg);
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		channels.add(incoming);
		channels.writeAndFlush(new TextWebSocketFrame(" Client " + ctx.channel() + " connect..."));
		System.out.println("Client:" + incoming.remoteAddress() + "connect");
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		channels.writeAndFlush(new TextWebSocketFrame(" Client " + ctx.channel() + " disconnect..."));
		System.out.println("Client:" + incoming.remoteAddress() + "disconnect");
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		channels.writeAndFlush(new TextWebSocketFrame(" Client " + ctx.channel() + " active..."));
		System.out.println("Client:" + incoming.remoteAddress() + "active");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		channels.writeAndFlush(new TextWebSocketFrame(" Client " + ctx.channel() + " inactive..."));
		System.out.println("Client:" + incoming.remoteAddress() + "inactive");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Channel incoming = ctx.channel();
		channels.writeAndFlush(new TextWebSocketFrame(" Client " + ctx.channel() + " error..."));
		System.out.println("Client:" + incoming.remoteAddress() + "error");
		cause.printStackTrace();
		ctx.close();
	}
}