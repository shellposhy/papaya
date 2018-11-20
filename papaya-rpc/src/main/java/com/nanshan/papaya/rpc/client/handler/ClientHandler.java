package com.nanshan.papaya.rpc.client.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nanshan.papaya.rpc.client.ClientFuture;
import com.papaya.protocol.Request;
import com.papaya.protocol.Response;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Client inbound operation
 * 
 * @author shellpo shih
 * @version 1.0
 */
public class ClientHandler extends SimpleChannelInboundHandler<Response> {
	private static final Logger LOG = LoggerFactory.getLogger(ClientHandler.class);

	// hang future
	private ConcurrentHashMap<String, ClientFuture> pendingClient = new ConcurrentHashMap<String, ClientFuture>();

	// Channel context
	private volatile Channel channel;
	// Each client remote server address
	private SocketAddress remoteAddress;

	/**
	 * When {@code Channel} registered ,set the server {@code InetSocketAddress}
	 * address
	 * 
	 * @param ctx
	 *            {@code ChannelPipeline}
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		this.remoteAddress = this.channel.remoteAddress();
	}

	/**
	 * Calls the next Channel
	 * 
	 * @param ctx
	 *            {@code ChannelPipeline}
	 */
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
		this.channel = ctx.channel();
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
		String requestId = response.getRequestId();
		ClientFuture clientFuture = pendingClient.get(requestId);
		if (clientFuture != null) {
			pendingClient.remove(requestId);
			clientFuture.execute(response);
		}
	}

	/**
	 * When handler occurs exception,close {@code ChannelHandlerContext}
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOG.error("client caught exception", cause);
		ctx.close();
	}

	public void close() {
		channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * {@code Request} send
	 * 
	 * @param request
	 *            {@code Request}
	 */
	public ClientFuture send(Request request) {
		final CountDownLatch latch = new CountDownLatch(1);
		ClientFuture rpcFuture = new ClientFuture(request);
		pendingClient.put(request.getRequestId(), rpcFuture);
		channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				latch.countDown();
			}
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			LOG.error(e.getMessage());
		}
		return rpcFuture;
	}

	// getter and setter
	public Channel getChannel() {
		return channel;
	}

	public SocketAddress getRemotePeer() {
		return remoteAddress;
	}
}
