package com.nanshan.papaya.rpc.client;

import com.nanshan.papaya.rpc.client.model.Person;

public interface HelloService {
	String hello(String name);

	String hello(Person person);
}
