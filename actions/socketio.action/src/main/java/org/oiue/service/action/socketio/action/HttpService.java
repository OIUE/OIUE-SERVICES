package org.oiue.service.action.socketio.action;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface HttpService {
	void handleHttpRequset(ChannelHandlerContext ctx, FullHttpRequest request);
}