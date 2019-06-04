package com.nanshan.papaya.rpc.cases.zk;

import com.nanshan.papaya.rpc.remote.zkclient.Zkclient;

public class ZkClientTest {

	public static void main(String[] args) {
		Zkclient zkclient = new Zkclient("192.168.2.28:2181", 3000L);
		zkclient.create("/test", true);
	}
}
