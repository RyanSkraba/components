package org.talend.component.salesforce;

import org.talend.component.ComponentProperties;
import org.talend.component.common.OauthProperties;
import org.talend.component.common.ProxyProperties;
import org.talend.component.common.UserPasswordProperties;
import org.talend.component.properties.Property;
import org.talend.component.properties.layout.Layout;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("salesforceConnectionProperties")
public class SalesforceConnectionProperties extends ComponentProperties {

    protected static final String PAGE_WIZARD_LOGIN = "wizardLogin";

    // public String apiVersion;
    public Property<String> url = new Property<String>("url", "Salesforce URL", "https://www.salesforce.com/services/Soap/u/25.0",
            true);

    public enum LoginType {
                           BASIC,
                           OAUTH
    }

    public Property<LoginType> loginType = new Property<LoginType>("logintype", "Connection type", LoginType.BASIC, true); //$NON-NLS-1$//$NON-NLS-2$

    public Property<OauthProperties> oauth = new Property<OauthProperties>("oauth", "OAuth connection", new OauthProperties(),
            false);

    public Property<UserPasswordProperties> userPassword = new Property<UserPasswordProperties>("userPassword",
            "Basic connection", new UserPasswordProperties(), false);

    public Property<Boolean> bulkConnection = new Property<Boolean>("bulkConnection", "Bulk Connection", false, false);

    public SalesforceConnectionProperties() {
        setupLayout();
        refreshLayout();
    }

    public Property<Boolean> needCompression = new Property<Boolean>("needCompression", "Need compression", false, false);

    public Property<Integer> timeout = new Property<Integer>("timeout", "Timeout", 0, false);

    public Property<Boolean> httpTraceMessage = new Property<Boolean>("httpTraceMessage", "Trace HTTP message", false, false);

    public Property<String> clientId = new Property<String>("clientId", "Client Id", null, false);;

    public Property<ProxyProperties> proxy = new Property<ProxyProperties>("proxy", "Proxy", new ProxyProperties(), false);;

    /**
     * DOC sgandon Comment method "setupLayout".
     */
    private void setupLayout() {
        url.setLayout(Layout.create().setGroup(PAGE_WIZARD_LOGIN).setOrder(1).setRow(1));
        loginType.setLayout(Layout.create().setGroup(PAGE_WIZARD_LOGIN).setRow(2));
        // tell the client to call back on change value
        loginType.setRequestRefreshLayoutOnChange(true);
        oauth.setLayout(Layout.create().setGroup(PAGE_WIZARD_LOGIN).setRow(3));
        userPassword.setLayout(Layout.create().setGroup(PAGE_WIZARD_LOGIN).setRow(3));
        bulkConnection.setLayout(Layout.create().setGroup(PAGE_WIZARD_LOGIN).setRow(4));
        proxy.setLayout(Layout.create().setGroup(PAGE_WIZARD_LOGIN).setOrder(3));
    }

    @Override
    public void refreshLayout() {
        switch (loginType.getValue()) {
        case OAUTH:
            oauth.getLayout().setVisible(true);
            userPassword.getLayout().setVisible(false);
            break;
        case BASIC:
            oauth.getLayout().setVisible(false);
            userPassword.getLayout().setVisible(true);
            break;
        default:
            throw new RuntimeException("Enum value should be handled :" + loginType.getValue());
        }
    }

}
