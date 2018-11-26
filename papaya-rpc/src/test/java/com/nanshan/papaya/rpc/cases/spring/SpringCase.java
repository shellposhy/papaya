package com.nanshan.papaya.rpc.cases.spring;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nanshan.papaya.rpc.client.Client;
import com.nanshan.papaya.rpc.client.HelloService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client-spring.xml")
public class SpringCase {
	@Autowired
	private Client rpcClient;

	@Test
	public void hello() {
		@SuppressWarnings("static-access")
		HelloService helloService = rpcClient.create(HelloService.class);
		String result = helloService.hello("Shih Shellpo");
		System.out.println(result);
		Assert.assertEquals("Hello! Shih Shellpo", result);
	}

	@After
	public void setTear() {
		if (rpcClient != null) {
			rpcClient.stop();
		}
	}
}
