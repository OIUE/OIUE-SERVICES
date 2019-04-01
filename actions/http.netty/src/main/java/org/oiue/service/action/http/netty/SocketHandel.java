package org.oiue.service.action.http.netty;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletResponse;

import org.oiue.service.action.http.netty.OiueHttpServiceImpl.ServletParent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

public class SocketHandel extends BaseHttpHandler {
	private WebSocketServerHandshaker handshaker;
	private final String wsUri = "/ws";

	/*
	 * channelAction
	 * channel 通道 action 活跃的
	 * 当客户端主动链接服务端的链接后，这个通道就是活跃的了。也就是客户端与服务端建立了通信通道并且可以传输数据
	 *
	 */
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel().localAddress().toString() + " 通道已激活！");
	}

	/*
	 * channelInactive
	 * channel 通道 Inactive 不活跃的
	 * 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端的关闭了通信通道并且不可以传输数据
	 *
	 */
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel().localAddress().toString() + " 通道不活跃！");
		// 关闭流
	}

	private String getMessage(ByteBuf buf) {
		byte[] con = new byte[buf.readableBytes()];
		buf.readBytes(con);
		try {
			return new String(con, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 功能：读取服务器发送过来的信息
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {// 如果是HTTP请求，进行HTTP操作
			System.out.println("into hettpHandle");
			handleHttpRequest(ctx, (FullHttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {// 如果是Websocket请求，则进行websocket操作
			System.out.println("into websockethandel");
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	// 处理HTTP的代码
	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws UnsupportedEncodingException {
		System.out.println("handleHttpRequest method==========" + req.method());
		System.out.println("handleHttpRequest uri==========" + req.uri());
		// 如果HTTP解码失败，返回HHTP异常
		Map<String, String> parmMap = new HashMap<>();
		if (req instanceof HttpRequest) {
			HttpMethod method = req.method();
			System.out.println("this is httpconnect");
			// 如果是websocket请求就握手升级
			if (wsUri.equalsIgnoreCase(req.uri())) {
				System.out.println("websocket 请求接入");
				WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("http://localhost:8880/index.html#/", null, false);
				handshaker = wsFactory.newHandshaker(req);
				if (handshaker == null) {
					WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
				} else {
					handshaker.handshake(ctx.channel(), req);
				}
			}
			if (HttpMethod.POST == method) {
				// 是POST请求
//				HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(req);
//				decoder.offer(req);
//				System.out.println(decoder.getBodyHttpDatas());
				FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
				ServletParent sp = OiueHttpServiceImpl.getServlet(req.uri());
//				sp.servlet.service(new ServletRequest() {
//					
//				}, new ServletResponse() {
//					
//				});
				
			}
			if (HttpMethod.GET == method) {
				// 是GET请求
				System.out.println(req.content());
				// 编码解码
				ByteBuf in = (ByteBuf) req.content();
				byte[] byt = new byte[in.readableBytes()];
				in.readBytes(byt);
				String body = new String(byt, "UTF-8");
				System.out.println("server channelRead...; received收到客户端消息:" + body);
				QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
				System.out.println(decoder.toString());
				/*
				 * ctx.channel().writeAndFlush(new TextWebSocketFrame("服务端数据"+body));
				 */
				// 将数据写入通道
				channels.writeAndFlush(new TextWebSocketFrame(body));
			}
		}
	}

	// 握手请求不成功时返回的应答
	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		// 返回应答给客户端
		if (res.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
		}
		// 如果是非Keep-Alive，关闭连接
		ChannelFuture f = ctx.channel().writeAndFlush(res);
	}

	// 处理Websocket的代码
	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
		// 判断是否是关闭链路的指令
		System.out.println("websocket get");
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return;
		}
		// 判断是否是Ping消息
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		// 文本消息，不支持二进制消息
		if (frame instanceof TextWebSocketFrame) {
			// 返回应答消息
			String request = ((TextWebSocketFrame) frame).text();
			ctx.channel().writeAndFlush(new TextWebSocketFrame(request + " , 欢迎使用Netty WebSocket服务，现在时刻：" + new java.util.Date().toString()));
		}
	}

	/**
	 * 功能：服务端发生异常的操作
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		System.out.println("异常信息：\r\n" + cause.getMessage());
	}
}