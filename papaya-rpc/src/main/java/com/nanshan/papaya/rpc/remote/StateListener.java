package com.nanshan.papaya.rpc.remote;

/**
 * Zookeeper Node state listener
 * 
 * @author shaobo shih
 * @version 1.0
 */
public interface StateListener {

	int DISCONNECTED = 0;

	int CONNECTED = 1;

	int RECONNECTED = 2;

	void stateChanged(int connected);

}
