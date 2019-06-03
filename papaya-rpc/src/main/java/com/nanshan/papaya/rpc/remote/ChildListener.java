package com.nanshan.papaya.rpc.remote;

import java.util.List;

/**
 * Zookeeper Node change listener
 * 
 * @author shaobo shih
 * @version 1.0
 */
public interface ChildListener {

	public void childChanged(String path, List<String> children);

}
