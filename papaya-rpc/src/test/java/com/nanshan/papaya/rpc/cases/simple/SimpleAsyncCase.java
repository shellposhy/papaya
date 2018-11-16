package com.nanshan.papaya.rpc.cases.simple;

import java.util.concurrent.ExecutionException;

import com.nanshan.papaya.rpc.client.Client;
import com.nanshan.papaya.rpc.client.ClientFuture;
import com.nanshan.papaya.rpc.client.HelloService;
import com.nanshan.papaya.rpc.client.proxy.IAsyncObject;
import com.nanshan.papaya.rpc.registry.ServiceDiscovery;

/**
 * {@code HelloService} service invocation.
 * 
 * @author shellpo shih
 */
public class SimpleAsyncCase {

	// test case
	public static void main(String[] args) {
		// Service discovery
		ServiceDiscovery discovery = new ServiceDiscovery("192.168.1.101:2181");

		// client invoke
		final Client client = new Client(discovery);

		Thread thread = new Thread(new Runnable() {
			public void run() {
				@SuppressWarnings("static-access")
				// Dynamic proxy object
				IAsyncObject helloService = client.createAsync(HelloService.class);

				ClientFuture clientFuture = helloService.call("hello", "Shih Shellpo");
				try {
					String result = (String) clientFuture.get();
					System.out.println(result);
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		});
		thread.setName("Simple_Async_Case");
		thread.start();

		try {
			thread.join();
			client.stop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}
