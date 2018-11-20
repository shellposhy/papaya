package com.nanshan.papaya.rpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.papaya.protocol.Request;
import com.papaya.protocol.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 */
public class ClientFuture implements Future<Object> {
	private static final Logger LOG = LoggerFactory.getLogger(ClientFuture.class);

	// synchronizer
	private ClientSynchronizer synchronizer;
	private Request request;
	private Response response;
	private long startTime;
	private long responseTimeThreshold = 5000;

	private List<ClientCallback> waitCallbacks = new ArrayList<ClientCallback>();
	private ReentrantLock lock = new ReentrantLock();

	// constructor
	public ClientFuture(Request request) {
		this.synchronizer = new ClientSynchronizer();
		this.request = request;
		this.startTime = System.currentTimeMillis();
	}

	/**
	 * Client callback result processing
	 * 
	 * @param reponse
	 *            {@code Server} result
	 * @return
	 */
	public void execute(Response reponse) {
		this.response = reponse;
		synchronizer.release(1);
		invoke();
		// Threshold
		long responseTime = System.currentTimeMillis() - startTime;
		if (responseTime > this.responseTimeThreshold) {
			LOG.warn("Service response time is too slow. Request id = " + reponse.getRequestId() + ". Response Time = "
					+ responseTime + "ms");
		}
	}

	public ClientFuture callback(ClientCallback callback) {
		lock.lock();
		try {
			if (isDone()) {
				run(callback);
			} else {
				this.waitCallbacks.add(callback);
			}
		} finally {
			lock.unlock();
		}
		return this;
	}

	// Synchronizer synchronization processing
	@Override
	public boolean isDone() {
		return synchronizer.isDone();
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		synchronizer.acquire(-1);
		if (this.response != null) {
			return this.response.getResult();
		} else {
			return null;
		}
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		boolean success = synchronizer.tryAcquireNanos(-1, unit.toNanos(timeout));
		if (success) {
			if (this.response != null) {
				return this.response.getResult();
			} else {
				return null;
			}
		} else {
			throw new RuntimeException(
					"Timeout exception. Request id: " + this.request.getRequestId() + ". Request class name: "
							+ this.request.getClassName() + ". Request method: " + this.request.getMethodName());
		}
	}

	@Override
	public boolean isCancelled() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		throw new UnsupportedOperationException();
	}

	/* ========private utilities======== */
	/* ========callback process======== */
	private void invoke() {
		lock.lock();
		try {
			for (final ClientCallback callback : waitCallbacks) {
				run(callback);
			}
		} finally {
			lock.unlock();
		}
	}

	private void run(final ClientCallback callback) {
		final Response res = this.response;
		Client.submit(new Runnable() {
			@Override
			public void run() {
				if (!res.isError()) {
					callback.success(res.getResult());
				} else {
					callback.fail(new RuntimeException("Response error", new Throwable(res.getError())));
				}
			}
		});
	}

	/**
	 * Provides a client framework for implementing blocking locks and related
	 * synchronizers (semaphores, events, etc) that rely on first-in-first-out
	 * (FIFO) wait queues.
	 */
	static class ClientSynchronizer extends AbstractQueuedSynchronizer {
		private static final long serialVersionUID = 1L;
		// future status
		private final int done = 1;// client finish
		private final int pending = 0;// client hang

		@Override
		protected boolean tryAcquire(int arg) {
			return getState() == done;
		}

		@Override
		protected boolean tryRelease(int arg) {
			if (getState() == pending) {
				if (compareAndSetState(pending, done)) {
					return true;
				} else {
					return false;
				}
			} else {
				return true;
			}
		}

		public boolean isDone() {
			getState();
			return getState() == done;
		}
	}
}
