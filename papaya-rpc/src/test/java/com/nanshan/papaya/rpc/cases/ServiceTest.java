package com.nanshan.papaya.rpc.cases;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nanshan.papaya.rpc.client.HelloService;
import com.nanshan.papaya.rpc.client.PersonService;
import com.nanshan.papaya.rpc.client.model.Person;
import com.nanshan.papaya.rpc.client.ClientFuture;
import com.nanshan.papaya.rpc.client.Client;
import com.nanshan.papaya.rpc.client.proxy.IAsyncObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client-spring.xml")
public class ServiceTest {

    @Autowired
    private Client rpcClient;

    @Test
    public void helloTest1() {
        @SuppressWarnings("static-access")
		HelloService helloService = rpcClient.create(HelloService.class);
        String result = helloService.hello("World");
        Assert.assertEquals("Hello! World", result);
    }

    @SuppressWarnings("static-access")
	@Test
    public void helloTest2() {
        HelloService helloService = rpcClient.create(HelloService.class);
        Person person = new Person("Yong", "Huang");
        String result = helloService.hello(person);
        Assert.assertEquals("Hello! Yong Huang", result);
    }

    @SuppressWarnings("static-access")
	@Test
    public void helloPersonTest() {
        PersonService personService = rpcClient.create(PersonService.class);
        int num = 5;
        List<Person> persons = personService.GetTestPerson("xiaoming", num);
        List<Person> expectedPersons = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            expectedPersons.add(new Person(Integer.toString(i), "xiaoming"));
        }
        assertThat(persons, equalTo(expectedPersons));

        for (int i = 0; i < persons.size(); ++i) {
            System.out.println(persons.get(i));
        }
    }

    @SuppressWarnings("static-access")
	@Test
    public void helloFutureTest1() throws ExecutionException, InterruptedException {
        IAsyncObject helloService = rpcClient.createAsync(HelloService.class);
        ClientFuture result = helloService.call("hello", "World");
        System.out.println(result.get().toString()+">>>>>>");
        Assert.assertEquals("Hello! World", result.get());
    }

    @SuppressWarnings("static-access")
	@Test
    public void helloFutureTest2() throws ExecutionException, InterruptedException {
        IAsyncObject helloService = rpcClient.createAsync(HelloService.class);
        Person person = new Person("Yong", "Huang");
        ClientFuture result = helloService.call("hello", person);
        Assert.assertEquals("Hello! Yong Huang", result.get());
    }

    @SuppressWarnings({ "static-access", "unchecked" })
	@Test
    public void helloPersonFutureTest1() throws ExecutionException, InterruptedException {
        IAsyncObject helloPersonService = rpcClient.createAsync(PersonService.class);
        int num = 5;
        ClientFuture result = helloPersonService.call("GetTestPerson", "xiaoming", num);
        List<Person> persons = (List<Person>) result.get();
        List<Person> expectedPersons = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            expectedPersons.add(new Person(Integer.toString(i), "xiaoming"));
        }
        assertThat(persons, equalTo(expectedPersons));

        for (int i = 0; i < num; ++i) {
            System.out.println(persons.get(i));
        }
    }

    @After
    public void setTear() {
        if (rpcClient != null) {
            rpcClient.stop();
        }
    }

}
