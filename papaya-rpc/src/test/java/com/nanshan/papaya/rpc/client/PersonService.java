package com.nanshan.papaya.rpc.client;

import java.util.List;

import com.nanshan.papaya.rpc.model.Person;

/**
 * Created by luxiaoxun on 2016-03-10.
 */
public interface PersonService {
    List<Person> GetTestPerson(String name, int num);
}
