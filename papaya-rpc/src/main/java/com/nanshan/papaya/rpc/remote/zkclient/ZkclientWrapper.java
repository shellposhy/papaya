package com.nanshan.papaya.rpc.remote.zkclient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;

import cn.com.lemon.base.Preasserts;

/**
 * Zkclient wrapper class that can monitor the state of the connection
 * automatically after the connection is out of time It is also consistent with
 * the use of curator
 *
 * @author shaobo shih
 * @version 1.0
 */
public class ZkclientWrapper {
	private final Logger logger = LoggerFactory.getLogger(ZkclientWrapper.class);
	private long timeout;
	private ZkClient client;
	private SettableFuture<ZkClient> completableFuture;
	private volatile boolean started = false;

	public ZkclientWrapper(final String serverAddr, long timeout) {
		this.timeout = timeout;
		completableFuture = SettableFuture.create();
		completableFuture.set(new ZkClient(serverAddr, Integer.MAX_VALUE));
	}

	public void start() {
		if (!started) {
			try {
				client = completableFuture.get(timeout, TimeUnit.MILLISECONDS);
			} catch (Throwable t) {
				logger.error("Timeout! zookeeper server can not be connected in : " + timeout + "ms!", t);
				completableFuture.cancel(false);
			}
			started = true;
		} else {
			logger.warn("Zkclient has already been started!");
		}
	}

	public void addListener(IZkStateListener listener) {
		if (completableFuture.isDone()) {
			client.subscribeStateChanges(listener);
		}
	}

	public boolean isConnected() {
		return client != null;
	}

	public void createPersistent(String path) {
		Preasserts.checkNotNull(client, new NullPointerException("Zookeeper is not connected yet!"));
		client.createPersistent(path, true);
	}

	public void createEphemeral(String path) {
		Preasserts.checkNotNull(client, new NullPointerException("Zookeeper is not connected yet!"));
		client.createEphemeral(path);
	}

	public void delete(String path) {
		Preasserts.checkNotNull(client, new NullPointerException("Zookeeper is not connected yet!"));
		client.delete(path);
	}

	public List<String> getChildren(String path) {
		Preasserts.checkNotNull(client, new NullPointerException("Zookeeper is not connected yet!"));
		return client.getChildren(path);
	}

	public boolean exists(String path) {
		Preasserts.checkNotNull(client, new NullPointerException("Zookeeper is not connected yet!"));
		return client.exists(path);
	}

	public void close() {
		Preasserts.checkNotNull(client, new NullPointerException("Zookeeper is not connected yet!"));
		client.close();
	}

	public List<String> subscribeChildChanges(String path, final IZkChildListener listener) {
		Preasserts.checkNotNull(client, new NullPointerException("Zookeeper is not connected yet!"));
		return client.subscribeChildChanges(path, listener);
	}

	public void unsubscribeChildChanges(String path, IZkChildListener listener) {
		Preasserts.checkNotNull(client, new NullPointerException("Zookeeper is not connected yet!"));
		client.unsubscribeChildChanges(path, listener);
	}
}
