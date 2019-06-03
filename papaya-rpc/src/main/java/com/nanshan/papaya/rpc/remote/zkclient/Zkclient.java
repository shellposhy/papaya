package com.nanshan.papaya.rpc.remote.zkclient;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import com.nanshan.papaya.rpc.remote.ChildListener;
import com.nanshan.papaya.rpc.remote.StateListener;
import com.nanshan.papaya.rpc.remote.support.AbstractZookeeperClient;

/**
 * Zkclient utility class that integrates Zookeeper basic operations.
 * 
 * @see AbstractZookeeperClient
 * @author shaobo shih
 * @version 1.0
 */
public class Zkclient extends AbstractZookeeperClient<IZkChildListener> {
	private final ZkclientWrapper client;
	private final long timeout;
	private volatile KeeperState state = KeeperState.SyncConnected;

	public Zkclient(String serverAddr, long timeout) {
		this.timeout = timeout;
		client = new ZkclientWrapper(serverAddr, this.timeout);
		client.addListener(new IZkStateListener() {

			@Override
			public void handleStateChanged(KeeperState state) throws Exception {
				Zkclient.this.state = state;
				if (state == KeeperState.Disconnected) {
					stateChanged(StateListener.DISCONNECTED);
				} else if (state == KeeperState.SyncConnected) {
					stateChanged(StateListener.CONNECTED);
				}
			}

			@Override
			public void handleSessionEstablishmentError(Throwable error) throws Exception {
				stateChanged(StateListener.DISCONNECTED);
			}

			@Override
			public void handleNewSession() throws Exception {
				stateChanged(StateListener.RECONNECTED);
			}
		});
		client.start();
	}

	@Override
	public void delete(String path) {
		try {
			client.delete(path);
		} catch (ZkNoNodeException e) {
		}
	}

	@Override
	public List<String> getChildren(String path) {
		try {
			return client.getChildren(path);
		} catch (ZkNoNodeException e) {
			return null;
		}
	}

	@Override
	public boolean isConnected() {
		return state == KeeperState.SyncConnected;
	}

	@Override
	protected void doClose() {
		client.close();
	}

	@Override
	protected void createPersistent(String path) {
		try {
			client.createPersistent(path);
		} catch (ZkNodeExistsException e) {
		}
	}

	@Override
	protected void createEphemeral(String path) {
		try {
			client.createEphemeral(path);
		} catch (ZkNodeExistsException e) {
		}
	}

	@Override
	protected boolean checkExists(String path) {
		try {
			return client.exists(path);
		} catch (Throwable t) {
		}
		return false;
	}

	@Override
	protected IZkChildListener createTargetChildListener(String path, ChildListener listener) {
		return new IZkChildListener() {
			@Override
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				listener.childChanged(parentPath, currentChilds);
			}
		};
	}

	@Override
	protected List<String> addTargetChildListener(String path, IZkChildListener listener) {
		return client.subscribeChildChanges(path, listener);
	}

	@Override
	protected void removeTargetChildListener(String path, IZkChildListener listener) {
		client.unsubscribeChildChanges(path, listener);
	}

}
