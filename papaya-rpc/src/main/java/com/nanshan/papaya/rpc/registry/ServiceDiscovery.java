package com.nanshan.papaya.rpc.registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nanshan.papaya.rpc.client.connect.ConnectPoolFactory;

/**
 * Zookeeper service discovery
 * 
 * @author shellpo shih
 * @version 1.0
 */
public class ServiceDiscovery {

	private static final Logger LOG = LoggerFactory.getLogger(ServiceDiscovery.class);
	private CountDownLatch latch = new CountDownLatch(1);
	private volatile List<String> dataList = new ArrayList<String>();

	// zookeeper address
	private String registryAddress;
	private ZooKeeper zookeeper;

	// constructor
	public ServiceDiscovery(String registryAddress) {
		this.registryAddress = registryAddress;
		zookeeper = connect();
		if (zookeeper != null) {
			watch(zookeeper);
		}
	}

	/**
	 * {@code ZooKeeper} discover service
	 * 
	 * @return {@code String}
	 */
	public String discover() {
		String data = null;
		int size = dataList.size();
		if (size > 0) {
			if (size == 1) {
				data = dataList.get(0);
				LOG.debug("using only data: {}", data);
			} else {
				data = dataList.get(ThreadLocalRandom.current().nextInt(size));
				LOG.debug("using random data: {}", data);
			}
		}
		return data;
	}

	/**
	 * Stop the {@code ZooKeeper} server
	 * 
	 * @return
	 */
	public void stop() {
		if (zookeeper != null) {
			try {
				zookeeper.close();
			} catch (InterruptedException e) {
				LOG.error("", e);
			}
		}
	}

	/* ========private utilities======== */
	/**
	 * Create {@code ZooKeeper} connect
	 * 
	 * @return {@code ZooKeeper}
	 */
	private ZooKeeper connect() {
		ZooKeeper zk = null;
		try {
			zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					if (event.getState() == Event.KeeperState.SyncConnected) {
						latch.countDown();
					}
				}
			});
			latch.await();
		} catch (IOException | InterruptedException e) {
			LOG.error("", e);
		}
		return zk;
	}

	/**
	 * Find and discover new services, or update services.
	 * 
	 * @param {@code
	 * 			{@link ZooKeeper}}
	 * @return
	 */
	private void watch(final ZooKeeper zooKeeper) {
		try {
			List<String> nodeList = zooKeeper.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					if (event.getType() == Event.EventType.NodeChildrenChanged) {
						watch(zooKeeper);
					}
				}
			});
			List<String> dataList = new ArrayList<String>();
			for (String node : nodeList) {
				byte[] bytes = zooKeeper.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
				dataList.add(new String(bytes));
			}
			LOG.debug("node data: {}", dataList);
			this.dataList = dataList;

			LOG.debug("Service discovery triggered updating connected server node.");
			ConnectPoolFactory.getInstance().updateConnectedServer(this.dataList);
		} catch (KeeperException | InterruptedException e) {
			LOG.error("", e);
		}
	}

}
