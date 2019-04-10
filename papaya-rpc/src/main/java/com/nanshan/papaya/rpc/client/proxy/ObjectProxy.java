package com.nanshan.papaya.rpc.client.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nanshan.papaya.rpc.client.ClientFuture;
import com.nanshan.papaya.rpc.client.connect.ConnectPoolFactory;
import com.nanshan.papaya.rpc.client.handler.ClientHandler;
import com.papaya.protocol.Request;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static cn.com.lemon.annotation.Reflections.type;
import static cn.com.lemon.base.Strings.uuid;
import static cn.com.lemon.base.Strings.isNullOrEmpty;

/**
 * Dynamic object proxy, and create request request, and send.
 * 
 * @author shellpo shih
 * @version 1.0
 */
public class ObjectProxy<T> implements InvocationHandler, IAsyncObject {
	private static final Logger LOG = LoggerFactory.getLogger(ObjectProxy.class);
	// Proxy class
	private Class<T> clazz;
	private String serverAddress;

	public ObjectProxy(Class<T> clazz) {
		this.clazz = clazz;
	}

	public ObjectProxy(Class<T> clazz, String serverAddress) {
		this.clazz = clazz;
		this.serverAddress = serverAddress;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] paramaters) throws Throwable {
		if (Object.class == method.getDeclaringClass()) {
			String name = method.getName();
			// Object#equals() method
			if ("equals".equals(name)) {
				return proxy == paramaters[0];
			}
			// Object#hashCode() method
			else if ("hashCode".equals(name)) {
				return System.identityHashCode(proxy);
			}
			// Object#toString() method
			else if ("toString".equals(name)) {
				return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy))
						+ ", with InvocationHandler " + this;
			} else {
				throw new IllegalStateException(String.valueOf(method));
			}
		}

		// Create the default initialization request.
		Request request = new Request();
		request.setRequestId(uuid());
		request.setClassName(method.getDeclaringClass().getName());
		request.setMethodName(method.getName());
		request.setParameterTypes(method.getParameterTypes());
		request.setParameters(paramaters);
		// Client call
		ClientHandler handler = null;
		/** support Multiservice different node */
		if (isNullOrEmpty(this.serverAddress)) {
			// single server node
			handler = ConnectPoolFactory.getInstance().handler();
		} else {
			handler = ConnectPoolFactory.getInstance().handler(this.serverAddress);
		}
		ClientFuture rpcFuture = handler.send(request);
		return rpcFuture.get();
	}

	@Override
	public ClientFuture call(String funcName, Object... paramaters) {
		ClientHandler handler = null;
		/** support Multiservice different node */
		if (isNullOrEmpty(this.serverAddress)) {
			// single server node
			handler = ConnectPoolFactory.getInstance().handler();
		} else {
			handler = ConnectPoolFactory.getInstance().handler(this.serverAddress);
		}
		Request request = init(this.clazz.getName(), funcName, paramaters);
		ClientFuture rpcFuture = handler.send(request);
		return rpcFuture;
	}

	/* ========private utilities======== */
	private Request init(String className, String methodName, Object[] paramaters) {
		LOG.debug("Class Name=[" + className + "]");
		LOG.debug("Method Name=[" + methodName + "]");
		// Create the default initialization request.
		Request request = new Request();
		request.setRequestId(uuid());
		request.setClassName(className);
		request.setMethodName(methodName);
		request.setParameters(paramaters);

		Class<?>[] parameterTypes = new Class[paramaters.length];
		// Get the class type
		for (int i = 0; i < paramaters.length; i++) {
			parameterTypes[i] = type(paramaters[i]);
		}
		request.setParameterTypes(parameterTypes);
		return request;
	}

}
