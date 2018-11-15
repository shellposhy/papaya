package com.nanshan.papaya.rpc.client;

import java.lang.reflect.Proxy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.nanshan.papaya.rpc.client.connect.ConnectPoolFactory;
import com.nanshan.papaya.rpc.client.proxy.IAsyncObject;
import com.nanshan.papaya.rpc.client.proxy.ObjectProxy;
import com.nanshan.papaya.rpc.registry.ServiceDiscovery;

/**
 * Create client proxy
 * 
 * @author shellpo shih
 * @version 1.0
 */
public class Client {

	// Client thread pool
	private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>(65536));

	private String serverAddress;
	private ServiceDiscovery serviceDiscovery;

	// constructor
	public Client(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public Client(ServiceDiscovery serviceDiscovery) {
		this.serviceDiscovery = serviceDiscovery;
	}

	@SuppressWarnings("unchecked")
	public static <T> T create(Class<T> interfaceClass) {
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass },
				new ObjectProxy<T>(interfaceClass));
	}

	public static <T> IAsyncObject createAsync(Class<T> interfaceClass) {
		return new ObjectProxy<T>(interfaceClass);
	}

	public static void submit(Runnable task) {
		threadPoolExecutor.submit(task);
	}

	public void stop() {
		threadPoolExecutor.shutdown();
		serviceDiscovery.stop();
		ConnectPoolFactory.getInstance().stop();
	}

	// getter and setter
	public String getServerAddress() {
		return serverAddress;
	}
}
