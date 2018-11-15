package com.nanshan.papaya.rpc.cases;

import java.util.concurrent.TimeUnit;

import com.nanshan.papaya.rpc.client.HelloService;
import com.nanshan.papaya.rpc.client.ClientFuture;
import com.nanshan.papaya.rpc.client.Client;
import com.nanshan.papaya.rpc.client.proxy.IAsyncObject;
import com.nanshan.papaya.rpc.registry.ServiceDiscovery;

/**
 * Created by luxiaoxun on 2016/3/16.
 */
public class BenchmarkAsync {
    public static void main(String[] args) throws InterruptedException {
        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("192.168.1.101:2181");
        final Client rpcClient = new Client(serviceDiscovery);

        int threadNum = 10;
        final int requestNum = 20;
        Thread[] threads = new Thread[threadNum];

        long startTime = System.currentTimeMillis();
        //benchmark for async call
        for (int i = 0; i < threadNum; ++i) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < requestNum; i++) {
                        try {
                            @SuppressWarnings("static-access")
							IAsyncObject client = rpcClient.createAsync(HelloService.class);
                            ClientFuture helloFuture = client.call("hello", Integer.toString(i));
                            String result = (String) helloFuture.get(3000, TimeUnit.MILLISECONDS);
                            System.out.println(result);
                            if (!result.equals("Hello! " + i))
                                System.out.println("error = " + result);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                }
            });
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        long timeCost = (System.currentTimeMillis() - startTime);
        String msg = String.format("Async call total-time-cost:%sms, req/s=%s", timeCost, ((double) (requestNum * threadNum)) / timeCost * 1000);
        System.out.println(msg);

        rpcClient.stop();

    }
}
