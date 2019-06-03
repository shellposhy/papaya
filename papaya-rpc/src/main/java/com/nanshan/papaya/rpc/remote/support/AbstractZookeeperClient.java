package com.nanshan.papaya.rpc.remote.support;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nanshan.papaya.rpc.remote.ChildListener;
import com.nanshan.papaya.rpc.remote.StateListener;
import com.nanshan.papaya.rpc.remote.ZookeeperClient;

/**
 * The default Zookeeper client abstract implementation
 * 
 * @see ZookeeperClient
 * @author shaobo shih
 * @version 1.0
 */
public abstract class AbstractZookeeperClient<TargetChildListener> implements ZookeeperClient {
	protected static final Logger logger = LoggerFactory.getLogger(AbstractZookeeperClient.class);
	private final Set<StateListener> stateListeners = new CopyOnWriteArraySet<StateListener>();
	private final ConcurrentMap<String, ConcurrentMap<ChildListener, TargetChildListener>> childListeners = new ConcurrentHashMap<String, ConcurrentMap<ChildListener, TargetChildListener>>();
	private volatile boolean closed = false;

	@Override
	public void create(String path, boolean ephemeral) {
		if (!ephemeral) {
			if (checkExists(path)) {
				return;
			}
		}
		int i = path.lastIndexOf('/');
		if (i > 0) {
			create(path.substring(0, i), false);
		}
		if (ephemeral) {
			createEphemeral(path);
		} else {
			createPersistent(path);
		}
	}

	@Override
	public List<String> addChildListener(String path, ChildListener listener) {
		ConcurrentMap<ChildListener, TargetChildListener> listeners = childListeners.get(path);
		if (listeners == null) {
			childListeners.putIfAbsent(path, new ConcurrentHashMap<ChildListener, TargetChildListener>());
			listeners = childListeners.get(path);
		}
		TargetChildListener targetListener = listeners.get(listener);
		if (targetListener == null) {
			listeners.putIfAbsent(listener, createTargetChildListener(path, listener));
			targetListener = listeners.get(listener);
		}
		return addTargetChildListener(path, targetListener);
	}

	@Override
	public void removeChildListener(String path, ChildListener listener) {
		ConcurrentMap<ChildListener, TargetChildListener> listeners = childListeners.get(path);
		if (listeners != null) {
			TargetChildListener targetListener = listeners.remove(listener);
			if (targetListener != null) {
				removeTargetChildListener(path, targetListener);
			}
		}
	}

	protected void stateChanged(int state) {
		if (stateListeners.size() > 0) {
			for (StateListener listener : stateListeners) {
				listener.stateChanged(state);
			}
		}
	}

	@Override
	public void addStateListener(StateListener listener) {
		stateListeners.add(listener);
	}

	@Override
	public void removeStateListener(StateListener listener) {
		stateListeners.remove(listener);
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}
		closed = true;
		try {
			doClose();
		} catch (Throwable e) {
			logger.warn(e.getMessage(), e);
		}
	}

	public Set<StateListener> getStateListeners() {
		return stateListeners;
	}

	protected abstract void doClose();

	protected abstract void createPersistent(String path);

	protected abstract void createEphemeral(String path);

	protected abstract boolean checkExists(String path);

	protected abstract TargetChildListener createTargetChildListener(String path, ChildListener listener);

	protected abstract List<String> addTargetChildListener(String path, TargetChildListener listener);

	protected abstract void removeTargetChildListener(String path, TargetChildListener listener);
}
