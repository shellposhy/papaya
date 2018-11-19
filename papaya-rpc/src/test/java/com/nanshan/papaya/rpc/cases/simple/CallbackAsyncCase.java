package com.nanshan.papaya.rpc.cases.simple;

import java.util.concurrent.CountDownLatch;

import com.nanshan.papaya.rpc.client.Client;
import com.nanshan.papaya.rpc.client.ClientCallback;
import com.nanshan.papaya.rpc.client.ClientFuture;
import com.nanshan.papaya.rpc.client.HelloService;
import com.nanshan.papaya.rpc.client.proxy.IAsyncObject;
import com.nanshan.papaya.rpc.registry.ServiceDiscovery;

/**
 * {@code HelloService} service invocation.
 * 
 * @author shellpo shih
 */
public class CallbackAsyncCase {

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		final CountDownLatch countDownLatch = new CountDownLatch(1);

		// Service discovery
		ServiceDiscovery discovery = new ServiceDiscovery("192.168.1.101:2181");
		// client
		final Client client = new Client(discovery);

		try {
			IAsyncObject helloService = client.createAsync(HelloService.class);
			ClientFuture helloFuture = helloService.call("hello", "Shih Shellpo");
			helloFuture.callback(new ClientCallback() {
				@Override
				public void success(Object result) {
					String output = (String) result;
					System.out.println(output);
					countDownLatch.countDown();
				}

				@Override
				public void fail(Exception e) {
					System.out.println(e);
					countDownLatch.countDown();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		client.stop();
	}

}
