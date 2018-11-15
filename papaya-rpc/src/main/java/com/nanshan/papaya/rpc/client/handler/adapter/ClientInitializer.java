package com.nanshan.papaya.rpc.client.handler.adapter;

import com.nanshan.papaya.rpc.client.handler.ClientHandler;
import com.nanshan.papaya.rpc.protocol.Request;
import com.nanshan.papaya.rpc.protocol.Response;
import com.nanshan.papaya.rpc.protocol.protostuff.ProtostuffDecoder;
import com.nanshan.papaya.rpc.protocol.protostuff.ProtostuffEncoder;

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
		cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
		cp.addLast(new ProtostuffDecoder(Response.class));
		cp.addLast(new ClientHandler());
	}
}
