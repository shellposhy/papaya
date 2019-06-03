package com.nanshan.papaya.rpc.remote.curator;

import java.util.Collections;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

import com.nanshan.papaya.rpc.remote.ChildListener;

import cn.com.lemon.base.Strings;

public class WatcherListener implements CuratorWatcher {
	private volatile ChildListener listener;
	private CuratorFramework client;

	public WatcherListener(ChildListener childListener, CuratorFramework client) {
		this.listener = childListener;
		this.client = client;
	}

	public void process(WatchedEvent event) throws Exception {
		if (listener != null) {
			String path = event.getPath() == null ? "" : event.getPath();
			listener.childChanged(path, !Strings.isNullOrEmpty(path)
					? client.getChildren().usingWatcher(this).forPath(path) : Collections.<String> emptyList());
		}
	}

	public void unwatch() {
		this.listener = null;
	}

}
