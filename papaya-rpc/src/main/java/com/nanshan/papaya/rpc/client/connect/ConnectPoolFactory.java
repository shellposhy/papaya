package com.nanshan.papaya.rpc.client.connect;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nanshan.papaya.rpc.client.handler.ClientHandler;
import com.nanshan.papaya.rpc.client.handler.adapter.ClientInitializer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * */
public class ConnectPoolFactory {
	private static final Logger LOG = LoggerFactory.getLogger(ConnectPoolFactory.class);
	private volatile static ConnectPoolFactory connectManage;
	private long connectTimeoutMillis = 6000;
	private AtomicInteger roundRobin = new AtomicInteger(0);
	private volatile boolean isRuning = true;

	// netty group
	private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
	// The maximum number of threads in the thread pool is recommended to be
	// twice the number of CPU cores, considering the efficiency problem.
	private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>(65536));
	// client handler list
	private CopyOnWriteArrayList<ClientHandler> connectedHandlers = new CopyOnWriteArrayList<>();
	// connected sever nodes
	private Map<InetSocketAddress, ClientHandler> connectedServerNodes = new ConcurrentHashMap<>();

	// Thread lock
	private ReentrantLock lock = new ReentrantLock();
	private Condition connected = lock.newCondition();

	// Direct instantiation is not allowed
	private ConnectPoolFactory() {
	}

	/**
	 * Instantiate the connection pool factory and maintain the singleton
	 * pattern.
	 * <p>
	 * Instantiation is double-checked and maintains efficiency in a
	 * multithreaded environment.
	 * 
	 * @return {@code ConnectPoolFactory}
	 */
	public static ConnectPoolFactory getInstance() {
		if (connectManage == null) {
			synchronized (ConnectPoolFactory.class) {
				if (connectManage == null) {
					connectManage = new ConnectPoolFactory();
				}
			}
		}
		return connectManage;
	}

	/**
	 * {@code ServiceDiscovery} When the service node is discovered, all the
	 * service nodes are updated and ready to service the business.
	 * 
	 * @param serverAddress
	 *            {@code InetSocketAddress} server nodes
	 * @return
	 */
	public void updateConnectedServer(List<String> allServerAddress) {
		LOG.debug("Update available services! service all size=" + allServerAddress.size() + "!");
		if (allServerAddress != null) {
			// Get available server node
			if (allServerAddress.size() > 0) {
				// update local serverNodes cache
				HashSet<InetSocketAddress> newAllServerNodeSet = new HashSet<InetSocketAddress>();
				for (int i = 0; i < allServerAddress.size(); ++i) {
					// Server address contains IP and port
					String[] array = allServerAddress.get(i).split(":");
					if (array.length == 2) {
						String host = array[0];
						int port = Integer.parseInt(array[1]);
						final InetSocketAddress remotePeer = new InetSocketAddress(host, port);
						newAllServerNodeSet.add(remotePeer);
					}
				}

				// Add new server node
				for (final InetSocketAddress serverNodeAddress : newAllServerNodeSet) {
					if (!connectedServerNodes.keySet().contains(serverNodeAddress)) {
						connectServerNode(serverNodeAddress);
					}
				}

				// Close and remove invalid server nodes
				for (int i = 0; i < connectedHandlers.size(); ++i) {
					ClientHandler connectedServerHandler = connectedHandlers.get(i);
					SocketAddress remotePeer = connectedServerHandler.getRemotePeer();
					if (!newAllServerNodeSet.contains(remotePeer)) {
						LOG.info("Remove invalid server node " + remotePeer);
						ClientHandler handler = connectedServerNodes.get(remotePeer);
						if (handler != null) {
							handler.close();
						}
						connectedServerNodes.remove(remotePeer);
						connectedHandlers.remove(connectedServerHandler);
					}
				}
			}
			// No available server node ( All server nodes are down )
			else {
				LOG.error("No available server node. All server nodes are down !!!");
				for (final ClientHandler connectedServerHandler : connectedHandlers) {
					SocketAddress remotePeer = connectedServerHandler.getRemotePeer();
					ClientHandler handler = connectedServerNodes.get(remotePeer);
					handler.close();
					connectedServerNodes.remove(connectedServerHandler);
				}
				connectedHandlers.clear();
			}
		}
	}

	/**
	 * Reconnect to the server.
	 * 
	 * @return
	 */
	public void reconnect(final ClientHandler handler, final SocketAddress remotePeer) {
		// if not null current handler,close and reconnect
		if (handler != null) {
			connectedHandlers.remove(handler);
			connectedServerNodes.remove(handler.getRemotePeer());
		}
		connectServerNode((InetSocketAddress) remotePeer);
	}

	/**
	 * Client {@code Request} {@code ClientHandler}
	 * 
	 * @return
	 */
	public ClientHandler handler() {
		int size = connectedHandlers.size();
		while (isRuning && size <= 0) {
			try {
				boolean available = waitingForHandler();
				if (available) {
					size = connectedHandlers.size();
				}
			} catch (InterruptedException e) {
				LOG.error("Waiting for available node is interrupted! ", e);
				throw new RuntimeException("Can't connect any servers!", e);
			}
		}
		int index = (roundRobin.getAndAdd(1) + size) % size;
		return connectedHandlers.get(index);
	}

	/**
	 * Stop connect pool and {@code EventLoopGroup}
	 * 
	 * @return
	 */
	public void stop() {
		isRuning = false;
		for (int i = 0; i < connectedHandlers.size(); ++i) {
			ClientHandler connectedServerHandler = connectedHandlers.get(i);
			connectedServerHandler.close();
		}
		signalAvailableHandler();
		threadPoolExecutor.shutdown();
		eventLoopGroup.shutdownGracefully();
	}

	/* ========private utilities======== */
	/**
	 * Check Server Node connect,if can be connected ,add the server node to
	 * connectedServerNodes{@code Map}
	 * <p>
	 * base on {@code Netty}
	 * 
	 * @param remoteAddress
	 *            {@link InetSocketAddress}
	 * @return
	 */
	private void connectServerNode(final InetSocketAddress remotePeer) {
		threadPoolExecutor.submit(new Runnable() {
			@Override
			public void run() {
				Bootstrap b = new Bootstrap();
				b.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new ClientInitializer());

				ChannelFuture channelFuture = b.connect(remotePeer);
				channelFuture.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(final ChannelFuture channelFuture) throws Exception {
						if (channelFuture.isSuccess()) {
							LOG.debug("Successfully connect to remote server. remote peer = " + remotePeer);
							ClientHandler handler = channelFuture.channel().pipeline().get(ClientHandler.class);
							addHandler(handler);
						}
					}
				});
			}
		});
	}

	/**
	 * Put {@code ClientHandler} into the connectedHandlers
	 * 
	 * @param handler
	 */
	private void addHandler(ClientHandler handler) {
		connectedHandlers.add(handler);
		InetSocketAddress remoteAddress = (InetSocketAddress) handler.getChannel().remoteAddress();
		connectedServerNodes.put(remoteAddress, handler);
		signalAvailableHandler();
	}

	/**
	 * Wakes up all waiting Client handler threads.
	 * 
	 * @return
	 */
	private void signalAvailableHandler() {
		lock.lock();
		try {
			connected.signalAll();
		} finally {
			lock.unlock();
		}
	}

	private boolean waitingForHandler() throws InterruptedException {
		lock.lock();
		try {
			return connected.await(this.connectTimeoutMillis, TimeUnit.MILLISECONDS);
		} finally {
			lock.unlock();
		}
	}

}
