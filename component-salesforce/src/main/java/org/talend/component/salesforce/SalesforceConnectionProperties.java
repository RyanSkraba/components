package org.talend.component.salesforce;

import org.talend.component.ComponentProperties;
import org.talend.component.annotation.Order;
import org.talend.component.annotation.Group;
import org.talend.component.annotation.Required;
import org.talend.component.annotation.Row;
import org.talend.component.common.OauthProperties;
import org.talend.component.common.ProxyProperties;
import org.talend.component.common.UserPasswordProperties;

import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonRootName("salesforceConnectionProperties") public class SalesforceConnectionProperties extends ComponentProperties {

    public enum LoginType {
        BASIC, OAUTH
    }

    protected static final String PAGE_WIZARD_LOGIN = "wizardLogin";

    public SalesforceConnectionProperties() {
        proxy = new ProxyProperties();
        oauth = new OauthProperties();
        userPassword = new UserPasswordProperties();
    }

    public static final String ENDPOINT = "endPoint";

    public static final String LOGINTYPE = "loginType";

    public static final String OAUTH = "oauth";

    public static final String USERPASSWORD = "userPassword";

    @Order(1) @Row(1) @Required @Group(PAGE_WIZARD_LOGIN) public String endPoint;

    @Row(2) @Required @Group(PAGE_WIZARD_LOGIN)  public LoginType loginType = LoginType.BASIC;

    public boolean validateLoginType(LoginType loginType) {
        System.out.println("validateLogintype: " + getLoginType());
        // Need to reset the required fields... Maybe could just pass the entire properties object back.
        return true;
    }

    @Row(3) @Required @Group(PAGE_WIZARD_LOGIN) public OauthProperties oauth;

    @Row(3) @Required @Group(PAGE_WIZARD_LOGIN) public UserPasswordProperties userPassword;

    @Row(4) @Group(PAGE_WIZARD_LOGIN) public boolean bulkConnection;

    public String apiVersion;

    public boolean needCompression;

    public int timeout;

    public boolean httpTraceMessage;

    public String clientId;

    @Order(3) public ProxyProperties proxy;

    @Override public String[] getProperties(String page) {
        List<String> props = new ArrayList();
        props.add(ENDPOINT);
        if (page.equals(PAGE_WIZARD_LOGIN)) {
            if (loginType == LoginType.OAUTH) {
                Collections.addAll(props, oauth.getProperties(null));
            } else if (loginType == LoginType.BASIC) {
                Collections.addAll(props, userPassword.getProperties(null));
            }
        }

        return (String[]) props.toArray();
    }

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
