package com.luangeng.servlet.server;

import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.Servlet;

import com.luangeng.servlet.impl.HttpRequest;
import com.luangeng.servlet.impl.HttpResponse;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.HttpUtil;
import io.netty.util.CharsetUtil;

public class ServletHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.content().writeBytes(Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
        ctx.channel().write(response).addListener(ChannelFutureListener.CLOSE);
    }

    private FullHttpResponse getHttpResponse(HttpResponse response) {
        int statusCode = response.getStatus();
        FullHttpResponse nettyResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(statusCode));

//        HttpUtil.setContentLength(nettyResponse, response.getContentLength());

        Map<String, String> responseHeaders = response.getHeaders();
        for (Map.Entry<String, String> header : responseHeaders.entrySet()) {
            nettyResponse.headers().add(header.getKey(), header.getValue());
        }
        String responseString = response.getResult();
        if (responseString != null) {
            Charset encoding = Charset.forName(response.getCharacterEncoding());
            nettyResponse.content().writeBytes(Unpooled.copiedBuffer(responseString, encoding));
        }
        return nettyResponse;
    }

	@Override
	protected void messageReceived(ChannelHandlerContext context, FullHttpRequest httpRequest) throws Exception {
		String uri = httpRequest.uri();

        Servlet servlet = ServletMng.getServlet(uri);
        if (servlet == null) {
            sendError(context, HttpResponseStatus.NOT_FOUND);
            return;
        }

        SerConfig config = new SerConfig();
        servlet.init(config);

        HttpRequest request = new HttpRequest(httpRequest);
        HttpResponse response = new HttpResponse();
        servlet.service(request, response);


        FullHttpResponse nettyResponse = getHttpResponse(response);
        nettyResponse.setProtocolVersion(httpRequest.protocolVersion());

        context.writeAndFlush(nettyResponse);
        context.channel().close();
	}
}