package com.papaya.common;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.papaya.common.utils.CollectionUtils;

/**
 * URL - Uniform Resource Locator (Immutable, ThreadSafe)
 * <p>
 * url example:
 * <ul>
 * <li>http://www.facebook.com/friends?param1=value1&amp;param2=value2
 * <li>http://username:password@10.20.130.230:8080/list?version=1.0.0
 * <li>ftp://username:password@192.168.1.7:21/1/read.txt
 * <li>registry://192.168.1.7:9090/org.apache.dubbo.service1?param1=value1&amp;
 * param2=value2
 * </ul>
 *
 * @see java.net.URL
 * @see java.net.URI
 */
public class URL implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String protocol;

	private final String username;

	private final String password;

	// by default, host to registry
	private final String host;

	// by default, port to registry
	private final int port;

	private final String path;

	private final Map<String, String> parameters;

	protected URL() {
		this.protocol = null;
		this.username = null;
		this.password = null;
		this.host = null;
		this.port = 0;
		this.path = null;
		this.parameters = null;
	}

	public URL(String protocol, String host, int port) {
		this(protocol, null, null, host, port, null, (Map<String, String>) null);
	}

	public URL(String protocol, String host, int port, String[] pairs) {
		this(protocol, null, null, host, port, null, CollectionUtils.toStringMap(pairs));
	}

	public URL(String protocol, String host, int port, Map<String, String> parameters) {
		this(protocol, null, null, host, port, null, parameters);
	}

	public URL(String protocol, String host, int port, String path) {
		this(protocol, null, null, host, port, path, (Map<String, String>) null);
	}

	public URL(String protocol, String host, int port, String path, String... pairs) {
		this(protocol, null, null, host, port, path, CollectionUtils.toStringMap(pairs));
	}

	public URL(String protocol, String host, int port, String path, Map<String, String> parameters) {
		this(protocol, null, null, host, port, path, parameters);
	}

	public URL(String protocol, String username, String password, String host, int port, String path) {
		this(protocol, username, password, host, port, path, (Map<String, String>) null);
	}

	public URL(String protocol, String username, String password, String host, int port, String path, String... pairs) {
		this(protocol, username, password, host, port, path, CollectionUtils.toStringMap(pairs));
	}

	public URL(String protocol, String username, String password, String host, int port, String path,
			Map<String, String> parameters) {
		if ((username == null || username.length() == 0) && password != null && password.length() > 0) {
			throw new IllegalArgumentException("Invalid url, password without username!");
		}
		this.protocol = protocol;
		this.username = username;
		this.password = password;
		this.host = host;
		this.port = (port < 0 ? 0 : port);
		// trim the beginning "/"
		while (path != null && path.startsWith("/")) {
			path = path.substring(1);
		}
		this.path = path;
		if (parameters == null) {
			parameters = new HashMap<String, String>();
		} else {
			parameters = new HashMap<String, String>(parameters);
		}
		this.parameters = Collections.unmodifiableMap(parameters);
	}

	/**
	 * Parse url string
	 *
	 * @param url
	 *            URL string
	 * @return URL instance
	 * @see URL
	 */
	public static URL valueOf(String url) {
		if (url == null || (url = url.trim()).length() == 0) {
			throw new IllegalArgumentException("url == null");
		}
		String protocol = null;
		String username = null;
		String password = null;
		String host = null;
		int port = 0;
		String path = null;
		Map<String, String> parameters = null;
		int i = url.indexOf("?"); // seperator between body and parameters
		if (i >= 0) {
			String[] parts = url.substring(i + 1).split("\\&");
			parameters = new HashMap<String, String>();
			for (String part : parts) {
				part = part.trim();
				if (part.length() > 0) {
					int j = part.indexOf('=');
					if (j >= 0) {
						parameters.put(part.substring(0, j), part.substring(j + 1));
					} else {
						parameters.put(part, part);
					}
				}
			}
			url = url.substring(0, i);
		}
		i = url.indexOf("://");
		if (i >= 0) {
			if (i == 0) {
				throw new IllegalStateException("url missing protocol: \"" + url + "\"");
			}
			protocol = url.substring(0, i);
			url = url.substring(i + 3);
		} else {
			// case: file:/path/to/file.txt
			i = url.indexOf(":/");
			if (i >= 0) {
				if (i == 0) {
					throw new IllegalStateException("url missing protocol: \"" + url + "\"");
				}
				protocol = url.substring(0, i);
				url = url.substring(i + 1);
			}
		}

		i = url.indexOf("/");
		if (i >= 0) {
			path = url.substring(i + 1);
			url = url.substring(0, i);
		}
		i = url.lastIndexOf("@");
		if (i >= 0) {
			username = url.substring(0, i);
			int j = username.indexOf(":");
			if (j >= 0) {
				password = username.substring(j + 1);
				username = username.substring(0, j);
			}
			url = url.substring(i + 1);
		}
		i = url.lastIndexOf(":");
		if (i >= 0 && i < url.length() - 1) {
			if (url.lastIndexOf("%") > i) {
				// ipv6 address with scope id
				// e.g. fe80:0:0:0:894:aeec:f37d:23e1%en0
				// see https://howdoesinternetwork.com/2013/ipv6-zone-id
				// ignore
			} else {
				port = Integer.parseInt(url.substring(i + 1));
				url = url.substring(0, i);
			}
		}
		if (url.length() > 0) {
			host = url;
		}
		return new URL(protocol, username, password, host, port, path, parameters);
	}

	public static String encode(String value) {
		if (value == null || value.length() == 0) {
			return "";
		}
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static String decode(String value) {
		if (value == null || value.length() == 0) {
			return "";
		}
		try {
			return URLDecoder.decode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	// getter and seter
	public String getProtocol() {
		return protocol;
	}

	public URL setProtocol(String protocol) {
		return new URL(protocol, username, password, host, port, path, getParameters());
	}

	public String getUsername() {
		return username;
	}

	public URL setUsername(String username) {
		return new URL(protocol, username, password, host, port, path, getParameters());
	}

	public String getPassword() {
		return password;
	}

	public URL setPassword(String password) {
		return new URL(protocol, username, password, host, port, path, getParameters());
	}

	public String getHost() {
		return host;
	}

	public URL setHost(String host) {
		return new URL(protocol, username, password, host, port, path, getParameters());
	}

	public int getPort() {
		return port;
	}

	public URL setPort(int port) {
		return new URL(protocol, username, password, host, port, path, getParameters());
	}

	public int getPort(int defaultPort) {
		return port <= 0 ? defaultPort : port;
	}

	public String getPath() {
		return path;
	}

	public URL setPath(String path) {
		return new URL(protocol, username, password, host, port, path, getParameters());
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + port;
		result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		URL other = (URL) obj;
		if (host == null) {
			if (other.host != null) {
				return false;
			}
		} else if (!host.equals(other.host)) {
			return false;
		}
		if (parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!parameters.equals(other.parameters)) {
			return false;
		}
		if (password == null) {
			if (other.password != null) {
				return false;
			}
		} else if (!password.equals(other.password)) {
			return false;
		}
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		if (protocol == null) {
			if (other.protocol != null) {
				return false;
			}
		} else if (!protocol.equals(other.protocol)) {
			return false;
		}
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
			return false;
		}
		return true;
	}

}
