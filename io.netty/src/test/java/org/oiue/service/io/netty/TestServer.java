package org.oiue.service.io.netty;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class TestServer {
	/* 端口号 */
	static final int PORT1 = Integer.parseInt(System.getProperty("port", "8765"));

	static final int PORT2 = Integer.parseInt(System.getProperty("port", "8764"));

	@Test
	public void testLis() {
		EventLoopGroup bossGroup = null;
		EventLoopGroup workerGroup = null;
		ServerBootstrap b = null;
		try {
			// 1:第一个线程组是用于接收Client连接的
			bossGroup = new NioEventLoopGroup(); // (1)
			// 2:第二个线程组是用于实际的业务处理操作的
			workerGroup = new NioEventLoopGroup();
			// 3:创建一个启动NIO服务的辅助启动类ServerBootstrap 就是对我们的Server进行一系列的配置
			b = new ServerBootstrap();// (2)
			// 4:绑定两个线程组
			b.group(bossGroup, workerGroup)
					// 5:需要指定使用NioServerSocketChannel这种类型的通道
					.channel(NioServerSocketChannel.class)// (3) 服务端 -->NioServerSocketChannel
					// 6:一定要使用childHandler 去绑定具体的事件处理器
//					.childHandler(new ChannelInitializer<SocketChannel>() // (4) childHandler
//					{
//						@Override
//						protected void initChannel(SocketChannel sc) throws Exception {
//							// 7:将自定义的serverHandler加入到管道中去（多个）
//							sc.pipeline().addLast(new ServerHandler());// handler中实现真正的业务逻辑
////	                    sc.pipeline().addLast(new ServerHandler());
////	                    sc.pipeline().addLast(new ServerHandler());
//						}
//					})
					.childHandler(new ChannelInitializer<SocketChannel>() { // 绑定客户端连接时候触发操作
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							System.out.println("connected...; Client:" + ch.remoteAddress());
							ch.pipeline().addLast(new EchoServerHandler()); // 客户端触发操作
						}
					})
					/**
					 * 服务器端TCP内核模块维护两个队列，我们称之为A,B吧 客户端向服务端connect的时候，会发送带有SYN标志的包(第一次握手)
					 * 服务端收到客户端发来的SYN时，向客户端发送SYN ACK确认(第二次握手)
					 * 此时TCP内核模块把客户端连接加入到A队列中，最后服务端收到客户端发来的ACK时(第三次握手)
					 * TCP内核模块把客户端连接从A队列移到B队列，连接成功，应用程序的accept会返回 也就是说accept从B队列中取出完成三次握手的连接
					 * A队列和B队列的长度之和是backLog,当A,B队列的长度之和大于backLog时，新连接将会被TCP内核拒绝
					 * 所以，如果backLog过小，可能会出现accept速度跟不上,A,B队列满了，导致新的客户端无法连接，
					 * 要注意的是，backLog对程序支持的连接数并无影响，backLog影响的只是还没有被accept取出的连接
					 */
					// 8:设置TCP连接的缓冲区
					.option(ChannelOption.SO_BACKLOG, 128)// (5)
//	            .option(ChannelOption.SO_SNDBUF, 32*1024) //设置发送缓冲大小
//	            .option(ChannelOption.SO_RCVBUF, 32*1024) //设置接收缓冲大小
					// 9:保持连接
					.childOption(ChannelOption.SO_KEEPALIVE, true);// (6)
			// 10:绑定指定的端口 进行监听
			// 此处端口号先写死 也可以绑定多个端口
			ChannelFuture cf2 = b.localAddress(new InetSocketAddress("0.0.0.0", PORT1)).bind().sync(); // (7)
			ChannelFuture cf3 = b.bind(PORT2).sync(); // (7) 绑定多个端口

			// Thread.sleep(10000);
			cf2.channel().closeFuture().sync(); // 异步等待关闭
			cf3.channel().closeFuture().sync(); // 异步等待关闭

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	class EchoServerHandler extends ChannelInboundHandlerAdapter {
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			System.out.println("server channelRead...; received:" + ctx + "\t" + msg);
			// 传统的HTTP接入
			if (msg instanceof FullHttpRequest) {
				getRequestParams(ctx, ((FullHttpRequest) msg));
				// WebSocket接入
			} else if (msg instanceof WebSocketFrame) {
				
			}
			ctx.write(msg);
		}

		private Map<String, String> getRequestParams(ChannelHandlerContext ctx, FullHttpRequest req) {

			Map<String, String> requestParams = new HashMap<>();
			// 处理get请求
			if (req.method() == HttpMethod.GET) {
				QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
				Map<String, List<String>> parame = decoder.parameters();
				Iterator<Entry<String, List<String>>> iterator = parame.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, List<String>> next = iterator.next();
					requestParams.put(next.getKey(), next.getValue().get(0));
				}
			}
			// 处理POST请求
			if (req.method() == HttpMethod.POST) {
				HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), req);
				List<InterfaceHttpData> postData = decoder.getBodyHttpDatas(); //
				for (InterfaceHttpData data : postData) {
					if (data.getHttpDataType() == HttpDataType.Attribute) {
						MemoryAttribute attribute = (MemoryAttribute) data;
						requestParams.put(attribute.getName(), attribute.getValue());
					}
				}
			}
			return requestParams;
		}

		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			System.out.println("server channelReadComplete.." + ctx);
			// 第一种方法：写一个空的buf，并刷新写出区域。完成后关闭sock channel连接。
			ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
			// ctx.flush(); // 第二种方法：在client端关闭channel连接，这样的话，会触发两次channelReadComplete方法。
			// ctx.flush().close().sync(); // 第三种：改成这种写法也可以，但是这中写法，没有第一种方法的好。
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			System.out.println("server occur exception:" + ctx + "\t" + cause.getMessage());
			cause.printStackTrace();
			ctx.close(); // 关闭发生异常的连接
		}
	}
}
