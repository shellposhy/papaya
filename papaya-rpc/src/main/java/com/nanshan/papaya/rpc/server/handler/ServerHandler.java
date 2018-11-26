package com.nanshan.papaya.rpc.server.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.Map;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.papaya.protocol.Request;
import com.papaya.protocol.Response;
import com.nanshan.papaya.rpc.server.Server;

/**
 * Server handler
 * <p>
 * Process client requests {@code Request}
 * 
 * @author shellpo shih
 * @version 1.0
 */
public class ServerHandler extends SimpleChannelInboundHandler<Request> {

	private static final Logger LOG = LoggerFactory.getLogger(ServerHandler.class);
	// client handler,receiving client handler request
	private final Map<String, Object> handlers;

	public ServerHandler(Map<String, Object> handlers) {
		this.handlers = handlers;
	}

	/**
	 * Process client requests
	 * 
	 * @param request
	 *            client package {@code Object}
	 * @param ctx
	 */
	@Override
	public void channelRead0(final ChannelHandlerContext ctx, final Request request) throws Exception {
		Server.submit(new Runnable() {
			@Override
			public void run() {
				LOG.info("Receive request " + request.getRequestId());
				// Response the client result
				Response response = new Response();
				// put request id into the response
				response.setRequestId(request.getRequestId());
				try {
					Object result = handle(request);
					// The server processes the data required by the client
					response.setResult(result);
				} catch (Throwable t) {
					response.setError(t.toString());
					LOG.error("RPC Server handle request error", t);
				}
				ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture channelFuture) throws Exception {
						LOG.info("Send response for request " + request.getRequestId());
					}
				});
			}
		});
	}

	/**
	 * Business logic data processing and returns results.
	 * 
	 * @param request
	 * @return Java {@code Object}
	 */
	private Object handle(Request request) throws Throwable {
		String className = request.getClassName();
		Object serviceBean = handlers.get(className);

		// service proxy
		Class<?> serviceClass = serviceBean.getClass();
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();

		// Debug the class name and method
		LOG.debug(serviceClass.getName() + ":" + methodName);
		for (int i = 0; i < parameterTypes.length; ++i) {
			LOG.debug(parameterTypes[i].getName());
		}
		for (int i = 0; i < parameters.length; ++i) {
			LOG.debug(parameters[i].toString());
		}

		// Cglib reflect
		FastClass serviceFastClass = FastClass.create(serviceClass);
		FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
		return serviceFastMethod.invoke(serviceBean, parameters);
	}

	/**
	 * Exception caught process
	 * <p>
	 * if sever channel process exists exception,close it
	 * 
	 * @param ctx
	 * @param cause
	 * @return
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		LOG.error("server caught exception", cause);
		// If the client forces close a connection, the server throws:
		// java.io.IOException: the remote host forces an existing
		// connection to be closed.
		Channel channel = ctx.channel();
		if (channel.isActive()) {
			channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
		ctx.close();
	}
}
