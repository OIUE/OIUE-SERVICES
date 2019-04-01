package org.oiue.service.io.netty;

import java.io.Serializable;

import org.oiue.service.io.Handler;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

@SuppressWarnings("serial")
class ServiceHandler extends ChannelInboundHandlerAdapter implements Serializable {
	/**
	 * 所有的活动用户
	 */
	public static final ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	private Logger logger = null;
	
	public static final String SESSION_NAME = "SERVICE_TCP_SESSION";
	public static final String REMOTE_ADDRESS = "SERVICE_REMOTE_ADDRESS";
	public static final String LAST_TIME = "SERVICE_LAST_TIME";
	public static final String SESSION_BINARY = "SERVICE_SESSION_BINARY";
//	private Handler handler;
//	private boolean binary;
	private LogService logService;
	
	public ServiceHandler(Handler handler, boolean binary, LogService logService) {
//		this.handler = handler;
//		this.binary = binary;
		this.logService = logService;
		
		this.logger = this.logService.getLogger(this.getClass());
		logger.info("new service handle binary = " + binary);
	}
	
	public String toByteString(byte[] bytes, int size) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < size; i++) {
			if (i == 0) {
				sb.append("0x");
			} else {
				sb.append(", 0x");
			}
			sb.append(toByteHex(bytes[i]));
		}
		return sb.toString();
	}
	
	private String toByteHex(byte b) {
		String temp = Integer.toHexString(0x000000FF & b);
		if (temp.length() < 2) {
			return "0" + temp;
		}
		return temp;
	}
	
	/**
	 * 读取消息通道
	 *
	 * @param context
	 * @param s
	 * @throws Exception
	 */
	protected void channelRead0(ChannelHandlerContext context, Object s) throws Exception {
		Channel channel = context.channel();
		// 当有用户发送消息的时候，对其他的用户发送消息
		for (Channel ch : group) {
			if (ch == channel) {
				ch.writeAndFlush("[you]: " + s + "\n");
			} else {
				ch.writeAndFlush("[" + channel.remoteAddress() + "]: " + s + "\n");
			}
		}
		System.out.println("[" + channel.remoteAddress() + "]: " + s + "\n");
	}
	
	/**
	 * 处理新加的消息通道
	 *
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		for (Channel ch : group) {
			if (ch == channel) {
				ch.writeAndFlush("[" + channel.remoteAddress() + "] coming");
			}
		}
		group.add(channel);
	}
	
	/**
	 * 处理退出消息通道
	 *
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		for (Channel ch : group) {
			if (ch == channel) {
				ch.writeAndFlush("[" + channel.remoteAddress() + "] leaving");
			}
		}
		group.remove(channel);
	}
	
	/**
	 * 在建立连接时发送消息
	 *
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		boolean active = channel.isActive();
		if (active) {
			System.out.println("[" + channel.remoteAddress() + "] is online");
		} else {
			System.out.println("[" + channel.remoteAddress() + "] is offline");
		}
		ctx.writeAndFlush("[server]: welcome");
	}
	
	/**
	 * 退出时发送消息
	 *
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		if (!channel.isActive()) {
			System.out.println("[" + channel.remoteAddress() + "] is offline");
		} else {
			System.out.println("[" + channel.remoteAddress() + "] is online");
		}
	}
	
	/**
	 * 异常捕获
	 *
	 * @param ctx
	 * @param e
	 * @throws Exception
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
		Channel channel = ctx.channel();
		System.out.println("[" + channel.remoteAddress() + "] leave the room");
		ctx.close().sync();
	}
}
