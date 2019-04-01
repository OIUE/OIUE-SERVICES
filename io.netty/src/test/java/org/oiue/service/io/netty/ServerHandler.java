package org.oiue.service.io.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * 服务端业务处理类 (编写主要的业务逻辑)
 * 
 * @author 
 */
public class ServerHandler extends ChannelHandlerAdapter {

	/**
	 * 每当从客户端收到新的数据时，这个方法会在收到消息时被调用 ByteBuf是一个引用计数对象，这个对象必须显示地调用release()方法来释放。
	 * 请记住处理器的职责是释放所有传递到处理器的引用计数对象。
	 */
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			// do something
			// 接收客户端发送的数据 ByteBuf
			ByteBuf buf = (ByteBuf) msg;
			// 创建一个和buf长度一样的空字节数组
			byte[] data = new byte[buf.readableBytes()];
			// 将buf中的数据读取到data数组中
			buf.readBytes(data);
			// 将data数据包装成string输出
			String request = new String(data, "utf-8");
			System.out.println("server :" + request);

			// 以上代码是接收客户端信息//

			// server端向client发送反馈数据
			// 如果是绑定了多个端口 那么都会进行发送
			ctx.writeAndFlush(Unpooled.copiedBuffer("888".getBytes())).addListener(ChannelFutureListener.CLOSE);// 添加监听
																												// 当服务端向客户端发送完数据后，关闭connect连接
			/**
			 * ChannelFutureListener,当一个写请求完成时通知并且关闭Channel 加上监听 意味着服务端回送数据到客户端时 连接关闭(短连接)
			 * 不加监听 意味着客户端与服务端一直保持连接状态(长连接)
			 */

			ctx.close();
		} finally {
			// Discard the received data silently.
			ReferenceCountUtil.release(msg);
		}
	}

	/**
	 * exceptionCaught()事件处理方法是当出现Throwable对象才会被调用 即当Netty由于IO错误或者处理器在处理事件时抛出的异常时
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// Close the connection when an exception is raised.
		cause.printStackTrace();
		ctx.close();
	}

}