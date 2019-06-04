package com.nanshan.papaya.rpc.cases.zk;

import com.nanshan.papaya.rpc.remote.curator.CuratorClient;

public class CuratorClientTest {
	public static void main(String[] args) {
		CuratorClient client=new CuratorClient("192.168.2.28:2181", 30000L);
		client.create("/curator", true);
	}
}
