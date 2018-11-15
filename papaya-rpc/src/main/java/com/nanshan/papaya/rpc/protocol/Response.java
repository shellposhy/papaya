package com.nanshan.papaya.rpc.protocol;

/**
 * Base Response
 * 
 * @author shellpo shih
 * @version 1.0
 */
public class Response {
	private String requestId;
	private String error;
	private Object result;

	public boolean isError() {
		return error != null;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
}
