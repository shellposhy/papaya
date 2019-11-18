package com.nanshan.papaya.rpc.client.handler.adapter;

import com.nanshan.papaya.rpc.client.handler.ClientHandler;
import com.papaya.common.Constants;
import com.papaya.protocol.Request;
import com.papaya.protocol.Response;
import com.papaya.protocol.protostuff.ProtostuffDecoder;
import com.papaya.protocol.protostuff.ProtostuffEncoder;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Multi client Channel Handler
 * 
 * @see ChannelInitializer
 * @author shellpo shih
 * @version 1.0
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel socketChannel) throws Exception {
		ChannelPipeline cp = socketChannel.pipeline();
		cp.addLast(new ProtostuffEncoder(Request.class));
		cp.addLast(new LengthFieldBasedFrameDecoder(Constants.MaxFrameLength, 0, 4, 0, 0));
		cp.addLast(new ProtostuffDecoder(Response.class));
		cp.addLast(new ClientHandler());
	}
}
