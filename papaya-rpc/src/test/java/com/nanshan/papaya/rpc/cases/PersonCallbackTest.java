package com.nanshan.papaya.rpc.cases;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.nanshan.papaya.rpc.client.ClientCallback;
import com.nanshan.papaya.rpc.client.PersonService;
import com.nanshan.papaya.rpc.client.model.Person;
import com.nanshan.papaya.rpc.client.ClientFuture;
import com.nanshan.papaya.rpc.client.Client;
import com.nanshan.papaya.rpc.client.proxy.IAsyncObject;
import com.nanshan.papaya.rpc.registry.ServiceDiscovery;

/**
 * Created by luxiaoxun on 2016/3/17.
 */
public class PersonCallbackTest {
	public static void main(String[] args) {
		ServiceDiscovery serviceDiscovery = new ServiceDiscovery("127.0.0.1:2181");
		final Client rpcClient = new Client(serviceDiscovery);
		final CountDownLatch countDownLatch = new CountDownLatch(1);

		try {
			@SuppressWarnings("static-access")
			IAsyncObject client = rpcClient.createAsync(PersonService.class);
			int num = 5;
			ClientFuture helloPersonFuture = client.call("GetTestPerson", "xiaoming", num);
			helloPersonFuture.callback(new ClientCallback() {
				@Override
				public void success(Object result) {
					@SuppressWarnings("unchecked")
					List<Person> persons = (List<Person>) result;
					for (int i = 0; i < persons.size(); ++i) {
						System.out.println(persons.get(i));
					}
					countDownLatch.countDown();
				}

				@Override
				public void fail(Exception e) {
					System.out.println(e);
					countDownLatch.countDown();
				}
			});

		} catch (Exception e) {
			System.out.println(e);
		}

		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		rpcClient.stop();

		System.out.println("End");
	}
}
