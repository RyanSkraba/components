package org.talend.component.common;

import org.talend.component.ComponentProperties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("oauthProperties")
public class ProxyProperties extends ComponentProperties {
	
	@JsonProperty
	public boolean useProxy;

	@JsonProperty
	public String host;

	@JsonProperty
	public int port;

	@JsonProperty
	public String userName;

	@JsonProperty
	public String password;
	
	public boolean isUseProxy() {
		return useProxy;
	}
	public void setUseProxy(boolean useProxy) {
		this.useProxy = useProxy;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}


}
