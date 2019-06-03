package com.nanshan.papaya.rpc.remote.zkclient;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import com.nanshan.papaya.rpc.remote.ChildListener;
import com.nanshan.papaya.rpc.remote.support.AbstractZookeeperClient;

public class Zkclients extends AbstractZookeeperClient<IZkChildListener> {
	@SuppressWarnings("unused")
	private final ZkClient client = null;
	@SuppressWarnings("unused")
	private volatile KeeperState state = KeeperState.SyncConnected;

	@Override
	public void delete(String path) {

	}

	@Override
	public List<String> getChildren(String path) {
		return null;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	protected void doClose() {

	}

	@Override
	protected void createPersistent(String path) {

	}

	@Override
	protected void createEphemeral(String path) {

	}

	@Override
	protected boolean checkExists(String path) {
		return false;
	}

	@Override
	protected IZkChildListener createTargetChildListener(String path, ChildListener listener) {
		return null;
	}

	@Override
	protected List<String> addTargetChildListener(String path, IZkChildListener listener) {
		return null;
	}

	@Override
	protected void removeTargetChildListener(String path, IZkChildListener listener) {

	}

}
