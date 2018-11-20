package com.papaya.common;

import java.util.regex.Pattern;

/**
 * Constants
 * 
 * @author Shih Shellpo
 * @version 1.0
 */
public class Constants {
	/* zookeeper */
	public static final int ZK_SESSION_TIMEOUT = 5000;
	public static final String ZK_REGISTRY_PATH = "/registry";
	public static final String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";

	public static final Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");
	public static final String ANYHOST_VALUE = "0.0.0.0";
}
