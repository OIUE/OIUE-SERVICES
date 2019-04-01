package org.oiue.service.io.netty;

import java.net.SocketAddress;
import java.util.HashMap;

import org.oiue.service.io.Handler;
import org.oiue.service.io.TcpService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TcpServiceImpl implements TcpService {
	protected HashMap<SocketAddress, ChannelFuture> acceptorMap = new HashMap<>();
	protected Logger logger;
	protected LogService logService;
	
	public TcpServiceImpl(LogService logService) {
		this.logService = logService;
		logger = logService.getLogger(this.getClass());
	}
	
	public synchronized void unregisterAll() {
		if (logger.isInfoEnabled()) {
			logger.info("unregisterAll");
		}
		// for (IoAcceptor e : acceptorMap.values()) {
		// e.dispose();
		// }
		acceptorMap.clear();
	}
	
	@Override
	public synchronized boolean register(SocketAddress address, Handler handler, boolean binary, int idleTime, String charsetName) {
		if (logger.isInfoEnabled()) {
			logger.info("register address = " + address + ", binary = " + binary + ", ide time = " + idleTime + "ms, charset = " + charsetName);
		}
		try {
			EventLoopGroup group = new NioEventLoopGroup();
			ServerBootstrap sb = new ServerBootstrap();
			sb.group(group) // 绑定线程池
					.channel(NioServerSocketChannel.class) // 指定使用的channel
					.localAddress(address)// 绑定监听端口
					.childHandler(new ChannelInitializer<SocketChannel>() { // 绑定客户端连接时候触发操作

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							System.out.println("connected...; Client:" + ch.remoteAddress());
							ch.pipeline().addLast(new EchoServerHandler()); // 客户端触发操作
						}
					});
			acceptorMap.put(address, sb.bind().sync()); // 服务器异步创建绑定
			return true;
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("register error", e);
			}
			return false;
		}
	}
	
	@Override
	public synchronized void unregister(SocketAddress address) {
		if (logger.isInfoEnabled()) {
			logger.info("unregister address = " + address);
		}
	}
	
	@Override
	public boolean connect(SocketAddress address, Handler handler, boolean binary, int connectTimeout, int idleTime, String charsetName) {
		if (logger.isInfoEnabled()) {
			logger.info("connect address = " + address + ", binary = " + binary + ", connectTimeout = " + connectTimeout + "ms, ide time = " + idleTime + "ms, charset = " + charsetName);
		}
		try {
			
			return true;
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("connect error", e);
			}
		}
		return false;
	}
	
	class EchoServerHandler extends ChannelInboundHandlerAdapter {
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			System.out.println("server channelRead...; received:" + msg);
			ctx.write(msg);
		}
		
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			System.out.println("server channelReadComplete..");
			// 第一种方法：写一个空的buf，并刷新写出区域。完成后关闭sock channel连接。
			ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
			// ctx.flush(); // 第二种方法：在client端关闭channel连接，这样的话，会触发两次channelReadComplete方法。
			// ctx.flush().close().sync(); // 第三种：改成这种写法也可以，但是这中写法，没有第一种方法的好。
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			System.out.println("server occur exception:" + cause.getMessage());
			cause.printStackTrace();
			ctx.close(); // 关闭发生异常的连接
		}
	}
}
