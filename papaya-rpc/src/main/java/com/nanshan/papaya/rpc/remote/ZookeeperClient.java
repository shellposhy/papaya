package com.nanshan.papaya.rpc.remote;

import java.util.List;

/**
 * Zookeeper client
 * 
 * @author shaobo shih
 * @version 1.0
 */
public interface ZookeeperClient {
	public void create(String path, boolean ephemeral);

	public void delete(String path);

	public List<String> getChildren(String path);

	public List<String> addChildListener(String path, ChildListener listener);

	public void removeChildListener(String path, ChildListener listener);

	public void addStateListener(StateListener listener);

	public void removeStateListener(StateListener listener);

	public boolean isConnected();

	public void close();
}
