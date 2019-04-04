package com.nanshan.papaya.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nanshan.papaya.rpc.client.HelloService;
import com.nanshan.papaya.rpc.client.PersonService;
import com.nanshan.papaya.rpc.registry.ServiceRegistry;
import com.nanshan.papaya.rpc.server.service.HelloServiceImpl;
import com.nanshan.papaya.rpc.server.service.PersonServiceImpl;

public class SimpleServer {
	private static final Logger logger = LoggerFactory.getLogger(SimpleServer.class);

	public static void main(String[] args) {
		String serverAddress = "192.168.2.20:18866";
		ServiceRegistry serviceRegistry = new ServiceRegistry("192.168.2.28:2181");
		Server simpleServer = new Server(serverAddress, serviceRegistry);

		// Service register
		HelloService helloService = new HelloServiceImpl();
		PersonService personService = new PersonServiceImpl();
		simpleServer.register("com.nanshan.papaya.rpc.client.HelloService", helloService);
		simpleServer.register("com.nanshan.papaya.rpc.client.PersonService", personService);
		try {
			simpleServer.start();
		} catch (Exception ex) {
			logger.error("Exception: {}", ex);
		}
	}
}
