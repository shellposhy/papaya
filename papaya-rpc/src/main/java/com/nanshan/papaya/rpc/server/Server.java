package com.nanshan.papaya.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.papaya.common.extension.Rpc;
import com.papaya.protocol.Request;
import com.papaya.protocol.Response;
import com.papaya.protocol.protostuff.ProtostuffDecoder;
import com.papaya.protocol.protostuff.ProtostuffEncoder;
import com.nanshan.papaya.rpc.registry.ServiceRegistry;
import com.nanshan.papaya.rpc.server.handler.ServerHandler;

/**
 * Server
 *
 * @author shellpo shih
 * @version 1.0
 */
public class Server implements ApplicationContextAware, InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(Server.class);

	// zookeeper register server address
	private String serverAddress;
	// service registry
	private ServiceRegistry serviceRegistry;

	// Server handler process and Service registration container
	private Map<String, Object> handlers = new HashMap<String, Object>();
	private static ThreadPoolExecutor threadPoolExecutor;

	// netty event thread excutor
	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;

	// constructor
	public Server(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public Server(String serverAddress, ServiceRegistry serviceRegistry) {
		this.serverAddress = serverAddress;
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * Server service registration
	 * 
	 * @param interfaceName
	 *            the target {@code Object} name
	 * @param serviceBean
	 *            the target {@code Object} service
	 */
	public Server register(String interfaceName, Object serviceBean) {
		LOG.info("Service registration container:{}", interfaceName);
		if (!handlers.containsKey(interfaceName)) {
			// Service registration container
			handlers.put(interfaceName, serviceBean);
		}
		return this;
	}

	/**
	 * Sever start
	 * <p>
	 * The main thread
	 * 
	 * @return
	 */
	public void start() throws Exception {
		if (bossGroup == null && workerGroup == null) {
			bossGroup = new NioEventLoopGroup();
			workerGroup = new NioEventLoopGroup();
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel channel) throws Exception {
							channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
									.addLast(new ProtostuffDecoder(Request.class))
									.addLast(new ProtostuffEncoder(Response.class))
									.addLast(new ServerHandler(handlers));
						}
					}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

			// service address
			String[] array = serverAddress.split(":");
			String host = array[0];
			int port = Integer.parseInt(array[1]);
			ChannelFuture future = bootstrap.bind(host, port).sync();
			LOG.info("Server started on port {}", port);

			// Register the current service provider address to zookeeper
			if (serviceRegistry != null) {
				serviceRegistry.register(serverAddress);
			}
			future.channel().closeFuture().sync();
		}
	}

	/**
	 * Process the {@code PapayaService} tags annotation
	 * <p>
	 * this tags used for rpc(Remote Procedure Call) service
	 * 
	 * @return
	 */
	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(Rpc.class);
		if (serviceBeanMap.size() > 0) {
			for (Object serviceBean : serviceBeanMap.values()) {
				String interfaceName = serviceBean.getClass().getAnnotation(Rpc.class).value().getName();
				LOG.info("Loading service: {}", interfaceName);
				handlers.put(interfaceName, serviceBean);
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		start();
	}

	/**
	 * Stop the server
	 * 
	 * @return
	 */
	public void stop() {
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
		}
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
		}
	}

	/**
	 * Execute the thread
	 * 
	 * @param task
	 * @return
	 */
	public static void submit(Runnable task) {
		if (threadPoolExecutor == null) {
			synchronized (Server.class) {
				if (threadPoolExecutor == null) {
					threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS,
							new ArrayBlockingQueue<Runnable>(65536));
				}
			}
		}
		threadPoolExecutor.submit(task);
	}
}
