package com.nanshan.papaya.rpc.cases.simple;

import java.util.List;

import com.nanshan.papaya.rpc.client.Client;
import com.nanshan.papaya.rpc.client.HelloService;
import com.nanshan.papaya.rpc.client.PersonService;
import com.nanshan.papaya.rpc.client.model.Person;
import com.nanshan.papaya.rpc.registry.ServiceDiscovery;

/**
 * {@code HelloService} service invocation.
 * 
 * @author shellpo shih
 */
public class SimpleCase {

	// test case
	public static void main(String[] args) {
		// Service discovery
		ServiceDiscovery discovery = new ServiceDiscovery("192.168.1.101:2181");

		// client invoke
		final Client client = new Client(discovery);
		final Person person = new Person();
		person.setFirstName("shih");
		person.setLastName("shellpo");

		// Thread executor
		long startTime = System.nanoTime();
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				@SuppressWarnings("static-access")
				final HelloService service = client.create(HelloService.class);
				String result = service.hello("First Say!");
				System.out.println(result);
				String personResult = service.hello(person);
				System.out.println(personResult);

				@SuppressWarnings("static-access")
				final PersonService personService = client.create(PersonService.class);
				List<Person> persons = personService.GetTestPerson("Shih Shellpo", 2);
				System.out.println("Person size=" + persons.size());
			}
		});
		thread.start();
		long timeCost = (System.nanoTime() - startTime);
		System.out.println("Request cost times=" + timeCost);
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			client.stop();
		}
	}
}
