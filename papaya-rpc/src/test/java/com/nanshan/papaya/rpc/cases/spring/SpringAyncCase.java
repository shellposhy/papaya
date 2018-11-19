package com.nanshan.papaya.rpc.cases.spring;

import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nanshan.papaya.rpc.client.Client;
import com.nanshan.papaya.rpc.client.ClientFuture;
import com.nanshan.papaya.rpc.client.HelloService;
import com.nanshan.papaya.rpc.client.model.Person;
import com.nanshan.papaya.rpc.client.proxy.IAsyncObject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client-spring.xml")
public class SpringAyncCase {
	@Autowired
	private Client rpcClient;

	@Test
	public void test() {
		@SuppressWarnings("static-access")
		IAsyncObject asyncObject = rpcClient.createAsync(HelloService.class);
		Person person = new Person("Shih", "Shellpo");
		ClientFuture result = asyncObject.call("hello", person);
		try {
			System.out.println((String)result.get());
			Assert.assertEquals("Hello! Shih Shellpo", (String)result.get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@After
	public void setTear() {
		if (rpcClient != null) {
			rpcClient.stop();
		}
	}
}
