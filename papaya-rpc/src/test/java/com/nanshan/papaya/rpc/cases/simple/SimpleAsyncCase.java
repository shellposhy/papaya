package com.nanshan.papaya.rpc.cases.simple;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.nanshan.papaya.rpc.client.Client;
import com.nanshan.papaya.rpc.client.ClientFuture;
import com.nanshan.papaya.rpc.client.HelloService;
import com.nanshan.papaya.rpc.client.PersonService;
import com.nanshan.papaya.rpc.client.model.Person;
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

		// Simulated data
		final Person person = new Person();
		person.setFirstName("shih");
		person.setLastName("shellpo");

		Thread thread = new Thread(new Runnable() {
			public void run() {
				@SuppressWarnings("static-access")
				// Dynamic proxy object
				IAsyncObject helloService = client.createAsync(HelloService.class);
				@SuppressWarnings("static-access")
				IAsyncObject personService = client.createAsync(PersonService.class);

				ClientFuture clientFuture = helloService.call("hello", "Shih Shellpo");
				ClientFuture personFuture = helloService.call("hello", person);
				Object[] objects = { "Shellpo Shih", 1 };
				ClientFuture personListFuture = personService.call("GetTestPerson", objects);
				try {
					String result = (String) clientFuture.get();
					System.out.println(result);
					String personResult = (String) personFuture.get();
					System.out.println(personResult);
					@SuppressWarnings("unchecked")
					List<Person> persons = (List<Person>) personListFuture.get();
					System.out.println("Person size=" + persons.size());
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
