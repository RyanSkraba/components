package org.talend.component.salesforce;

import org.talend.component.ComponentProperties;
import org.talend.component.annotation.Order;
import org.talend.component.annotation.Page;
import org.talend.component.annotation.Required;
import org.talend.component.common.OauthProperties;
import org.talend.component.common.ProxyProperties;
import org.talend.component.common.UserPasswordProperties;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("salesforceProperties")
public class SalesforceProperties extends ComponentProperties {

	public enum LoginType {
		BASIC, OAUTH
	};

	protected static final String LOGIN = "login";
	protected static final String MAIN = "main";
	
	public SalesforceProperties() {
		proxy = new ProxyProperties();
		oauth = new OauthProperties();
		userPassword = new UserPasswordProperties();
	}

	@Order(1)
	@Required
	@Page(LOGIN)
	public LoginType loginType;

	@Order(1)
	@Page(MAIN)
	public boolean bulkConnection;

	public String apiVersion;

	public String endPoint;

	public boolean needCompression;

	public int timeout;

	public boolean httpTraceMessage;

	public String clientId;

	@Order(3)
	public ProxyProperties proxy;

	@Order(2)
	@Required
	@Page(LOGIN)
	public OauthProperties oauth;

	@Order(2)
	@Required
	@Page(LOGIN)
	public UserPasswordProperties userPassword;

	public LoginType getLoginType() {
		return loginType;
	}

	public void setLoginType(LoginType loginType) {
		this.loginType = loginType;
	}

	public boolean isBulkConnection() {
		return bulkConnection;
	}

	public void setBulkConnection(boolean bulkConnection) {
		this.bulkConnection = bulkConnection;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public boolean isNeedCompression() {
		return needCompression;
	}

	public void setNeedCompression(boolean needCompression) {
		this.needCompression = needCompression;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isHttpTraceMessage() {
		return httpTraceMessage;
	}

	public void setHttpTraceMessage(boolean httpTraceMessage) {
		this.httpTraceMessage = httpTraceMessage;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public ProxyProperties getProxy() {
		return proxy;
	}

	public void setProxy(ProxyProperties proxy) {
		this.proxy = proxy;
	}

	public OauthProperties getOauth() {
		return oauth;
	}

	public void setOauth(OauthProperties oauth) {
		this.oauth = oauth;
	}

	public UserPasswordProperties getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(UserPasswordProperties userPassword) {
		this.userPassword = userPassword;
	}

}
