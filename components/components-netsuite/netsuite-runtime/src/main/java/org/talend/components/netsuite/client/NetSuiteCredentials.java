// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.netsuite.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;
import java.util.Properties;

import org.apache.cxf.helpers.IOUtils;

public class NetSuiteCredentials {

    private String email;

    private String password;

    private String account;

    private String roleId;

    private String applicationId;

    private int numberOfSeats = 1;

    private String id;

    private String companyId;

    private String userId;

    private String partnerId;

    private String privateKey; // path to private key in der format

    private boolean useSsoLogin = false;

    public NetSuiteCredentials() {
    }

    public NetSuiteCredentials(String email, String password, String account, String roleId) {
        this(email, password, account, roleId, 1);
    }

    public NetSuiteCredentials(String email, String password, String account, String roleId, int numberOfSeats) {
        this.email = email;
        this.password = password;
        this.account = account;
        this.roleId = roleId;
        this.numberOfSeats = numberOfSeats;
    }

    public String getAccount() {
        return account;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRoleId() {
        return roleId;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public boolean isUseSsoLogin() {
        return useSsoLogin;
    }

    public void setUseSsoLogin(boolean useSsoLogin) {
        this.useSsoLogin = useSsoLogin;
    }

    public static NetSuiteCredentials loadFromLocation(URI location, String propertyPrefix) throws IOException {
        InputStream stream;
        if (location.getScheme().equals("classpath")) {
            stream = NetSuiteCredentials.class.getResourceAsStream(location.getSchemeSpecificPart());
        } else {
            stream = location.toURL().openStream();
        }
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } finally {
            stream.close();
        }
        return loadFromProperties(properties, propertyPrefix);
    }

    public static NetSuiteCredentials loadFromProperties(Properties properties, String prefix) {
        NetSuiteCredentials credentials = new NetSuiteCredentials();
        credentials.setEmail(properties.getProperty(prefix + "email"));
        credentials.setPassword(properties.getProperty(prefix + "password"));
        credentials.setRoleId(properties.getProperty(prefix + "roleId"));
        credentials.setAccount(properties.getProperty(prefix + "account"));
        credentials.setApplicationId(properties.getProperty(prefix + "applicationId"));
        return credentials;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NetSuiteCredentials that = (NetSuiteCredentials) o;
        return numberOfSeats == that.numberOfSeats && useSsoLogin == that.useSsoLogin && Objects.equals(email, that.email)
                && Objects.equals(account, that.account) && Objects.equals(roleId, that.roleId) && Objects
                .equals(applicationId, that.applicationId) && Objects.equals(id, that.id) && Objects
                .equals(companyId, that.companyId) && Objects.equals(userId, that.userId) && Objects
                .equals(partnerId, that.partnerId) && Objects.equals(privateKey, that.privateKey);
    }

    @Override public int hashCode() {
        return Objects.hash(email, account, roleId, applicationId, numberOfSeats, id, companyId, userId, partnerId, privateKey,
                useSsoLogin);
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("NetSuiteCredentials{");
        sb.append("email='").append(email).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", account='").append(account).append('\'');
        sb.append(", roleId='").append(roleId).append('\'');
        sb.append(", applicationId='").append(applicationId).append('\'');
        sb.append(", numberOfSeats=").append(numberOfSeats);
        sb.append(", id='").append(id).append('\'');
        sb.append(", companyId='").append(companyId).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", partnerId='").append(partnerId).append('\'');
        sb.append(", privateKey='").append(privateKey).append('\'');
        sb.append(", useSsoLogin=").append(useSsoLogin);
        sb.append('}');
        return sb.toString();
    }
}