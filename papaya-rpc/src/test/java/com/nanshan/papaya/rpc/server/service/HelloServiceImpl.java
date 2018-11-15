package com.nanshan.papaya.rpc.server.service;

import com.nanshan.papaya.rpc.client.HelloService;
import com.nanshan.papaya.rpc.client.model.Person;
import com.nanshan.papaya.rpc.server.annotation.PapayaService;

@PapayaService(HelloService.class)
public class HelloServiceImpl implements HelloService {

	public HelloServiceImpl() {
	}

	@Override
	public String hello(String name) {
		return "Hello! " + name;
	}

	@Override
	public String hello(Person person) {
		return "Hello! " + person.getFirstName() + " " + person.getLastName();
	}
}
