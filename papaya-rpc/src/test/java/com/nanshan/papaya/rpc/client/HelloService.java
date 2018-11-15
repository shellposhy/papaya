package com.nanshan.papaya.rpc.client;

import com.nanshan.papaya.rpc.model.Person;

public interface HelloService {
    String hello(String name);

    String hello(Person person);
}
