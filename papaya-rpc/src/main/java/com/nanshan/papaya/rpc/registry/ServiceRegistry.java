package com.nanshan.papaya.rpc.registry;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.papaya.common.Constants;

/**
 * Zookeeper service registry
 * 
 * @author shellpo shih
 * @version 1.0
 */
public class ServiceRegistry {

	private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistry.class);
	private CountDownLatch latch = new CountDownLatch(1);

	// zookeeper address
	private String registryAddress;

	// constructor
	public ServiceRegistry(String registryAddress) {
		this.registryAddress = registryAddress;
	}

	public void register(String data) {
		if (data != null) {
			ZooKeeper zk = connect();
			if (zk != null) {
				createRootNode(zk); // Add root node if not exist
				createNode(zk, data);
			}
		}
	}

	/* ========private utilities======== */
	/**
	 * Create zookeeper connect
	 * 
	 * @return {@code ZooKeeper}
	 */
	private ZooKeeper connect() {
		ZooKeeper zk = null;
		try {
			zk = new ZooKeeper(registryAddress, Constants.ZK_SESSION_TIMEOUT, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					if (event.getState() == Event.KeeperState.SyncConnected) {
						latch.countDown();
					}
				}
			});
			latch.await();
		} catch (IOException e) {
			LOG.error("", e);
		} catch (InterruptedException ex) {
			LOG.error("", ex);
		}
		return zk;
	}

	/**
	 * register root node in register center.
	 * 
	 * @param zooKeeper
	 *            {@code ZooKeeper}
	 */
	private void createRootNode(ZooKeeper zk) {
		try {
			Stat s = zk.exists(Constants.ZK_REGISTRY_PATH, false);
			if (s == null) {
				zk.create(Constants.ZK_REGISTRY_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		} catch (KeeperException e) {
			LOG.error(e.toString());
		} catch (InterruptedException e) {
			LOG.error(e.toString());
		}
	}

	/**
	 * register service node in register center.
	 * 
	 * @param zooKeeper
	 *            {@code ZooKeeper}
	 * @param data
	 */
	private void createNode(ZooKeeper zk, String data) {
		try {
			byte[] bytes = data.getBytes();
			String path = zk.create(Constants.ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL_SEQUENTIAL);
			LOG.info("create zookeeper node ({} => {})", path, data);
		} catch (KeeperException e) {
			LOG.error("", e);
		} catch (InterruptedException ex) {
			LOG.error("", ex);
		}
	}
}