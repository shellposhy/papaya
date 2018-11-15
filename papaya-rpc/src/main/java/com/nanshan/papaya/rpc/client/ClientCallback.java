package com.nanshan.papaya.rpc.client;

/**
 * Client callback interface
 * 
 * @author shellpo shih
 * @version 1.0
 */
public interface ClientCallback {

	void success(Object result);

	void fail(Exception e);

}
