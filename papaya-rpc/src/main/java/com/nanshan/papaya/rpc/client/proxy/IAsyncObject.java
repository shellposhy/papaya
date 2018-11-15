package com.nanshan.papaya.rpc.client.proxy;

import com.nanshan.papaya.rpc.client.ClientFuture;

/**
 * Asynchronous object proxy interface
 * 
 * @author shellpo shih
 * @version 1.0
 */
public interface IAsyncObject {
	public ClientFuture call(String funcName, Object... args);
}