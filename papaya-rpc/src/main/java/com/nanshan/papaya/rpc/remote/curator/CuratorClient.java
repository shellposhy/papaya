package com.nanshan.papaya.rpc.remote.curator;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;

import com.nanshan.papaya.rpc.remote.ChildListener;
import com.nanshan.papaya.rpc.remote.StateListener;
import com.nanshan.papaya.rpc.remote.support.AbstractZookeeperClient;

/**
 * Curator utility class that integrates Zookeeper basic operations.
 * 
 * @see AbstractZookeeperClient
 * @author shaobo shih
 * @version 1.0
 */
public class CuratorClient extends AbstractZookeeperClient<CuratorWatcher> {

	private final CuratorFramework client;
	private final long timeout;

	public CuratorClient(String serverAddr, long timeout) {
		this.timeout = timeout;
		try {
			CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().connectString(serverAddr)
					.retryPolicy(new RetryNTimes(1, 1000)).connectionTimeoutMs(((Long) this.timeout).intValue());
			client = builder.build();
			client.getConnectionStateListenable().addListener(new ConnectionStateListener() {

				@Override
				public void stateChanged(CuratorFramework client, ConnectionState newState) {
					if (newState == ConnectionState.LOST) {
						CuratorClient.this.stateChanged(StateListener.DISCONNECTED);
					} else if (newState == ConnectionState.CONNECTED) {
						CuratorClient.this.stateChanged(StateListener.CONNECTED);
					} else if (newState == ConnectionState.RECONNECTED) {
						CuratorClient.this.stateChanged(StateListener.RECONNECTED);
					}

				}
			});
			client.start();
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public void delete(String path) {
		try {
			client.delete().forPath(path);
		} catch (NoNodeException e) {
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public List<String> getChildren(String path) {
		try {
			return client.getChildren().forPath(path);
		} catch (NoNodeException e) {
			return null;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public boolean isConnected() {
		return client.getZookeeperClient().isConnected();
	}

	@Override
	protected void doClose() {
		client.close();

	}

	@Override
	protected void createPersistent(String path) {
		try {
			client.create().forPath(path);
		} catch (NodeExistsException e) {
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	protected void createEphemeral(String path) {
		try {
			client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
		} catch (NodeExistsException e) {
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	protected boolean checkExists(String path) {
		try {
			if (client.checkExists().forPath(path) != null) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	@Override
	protected CuratorWatcher createTargetChildListener(String path, ChildListener listener) {
		return new WatcherListener(listener, client);
	}

	@Override
	protected List<String> addTargetChildListener(String path, CuratorWatcher listener) {
		try {
			return client.getChildren().usingWatcher(listener).forPath(path);
		} catch (NoNodeException e) {
			return null;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	protected void removeTargetChildListener(String path, CuratorWatcher listener) {
		((WatcherListener) listener).unwatch();
	}

}
