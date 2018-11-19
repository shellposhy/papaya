package com.nanshan.papaya.rpc.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringServer {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		new ClassPathXmlApplicationContext("server-spring.xml");
	}
}
