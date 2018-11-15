package com.nanshan.papaya.rpc.server.service;

import java.util.ArrayList;
import java.util.List;

import com.nanshan.papaya.rpc.client.PersonService;
import com.nanshan.papaya.rpc.client.model.Person;
import com.nanshan.papaya.rpc.server.annotation.PapayaService;

/**
 * Created by luxiaoxun on 2016-03-10.
 */
@PapayaService(PersonService.class)
public class PersonServiceImpl implements PersonService {

    @Override
    public List<Person> GetTestPerson(String name, int num) {
        List<Person> persons = new ArrayList<>(num);
        for (int i = 0; i < num; ++i) {
            persons.add(new Person(Integer.toString(i), name));
        }
        return persons;
    }
}
