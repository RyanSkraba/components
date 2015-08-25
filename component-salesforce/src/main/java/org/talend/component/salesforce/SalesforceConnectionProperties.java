package org.talend.component.salesforce;

import org.talend.component.ComponentProperties;
import org.talend.component.common.OauthProperties;
import org.talend.component.common.ProxyProperties;
import org.talend.component.common.UserPasswordProperties;
import org.talend.component.properties.Property;
import org.talend.component.properties.ValidationResult;
import org.talend.component.properties.presentation.Form;
import org.talend.component.properties.presentation.Layout;

import com.fasterxml.jackson.annotation.JsonRootName;
import org.talend.component.properties.presentation.Wizard;

@JsonRootName("salesforceConnectionProperties") public class SalesforceConnectionProperties extends ComponentProperties {

    public SalesforceConnectionProperties() {
        setupLayout();
    }

    // public String apiVersion;
    public Property<String> url = new Property<String>("url", "Salesforce URL").setRequired(true) //$NON-NLS-1$//$NON-NLS-2$
            .setValue("https://www.salesforce.com/services/Soap/u/25.0"); //$NON-NLS-1$

    public enum LoginType {
        BASIC,
        OAUTH
    }

    public Property<LoginType> loginType = new Property<LoginType>("logintype", "Connection type").setRequired(true)
            .setValue(LoginType.BASIC);

    public Property<OauthProperties> oauth = new Property<OauthProperties>("oauth", "OAuth connection")
            .setValue(new OauthProperties());

    public Property<UserPasswordProperties> userPassword = new Property<UserPasswordProperties>("userPassword",
            "Basic connection").setValue(new UserPasswordProperties());

    public Property<Boolean> bulkConnection = new Property<Boolean>("bulkConnection", "Bulk Connection");

    public Property<Boolean> needCompression = new Property<Boolean>("needCompression", "Need compression");

    public Property<Boolean> testConnection = new Property<Boolean>("testConnection", "Test connection");

    public Property<Integer> timeout = new Property<Integer>("timeout", "Timeout").setValue(0);

    public Property<Boolean> httpTraceMessage = new Property<Boolean>("httpTraceMessage", "Trace HTTP message");

    public Property<String> clientId = new Property<String>("clientId", "Client Id");

    public Property<ProxyProperties> proxy = new Property<ProxyProperties>("proxy", "Proxy").setValue(new ProxyProperties());

    private void setupLayout() {
        Form form;
        Wizard wizard;

        form = Form.create("Connection", "Salesforce Connection Settings");
        forms.add(form);
        form.addProperty(url.setLayout(Layout.create().setOrder(1).setRow(1)));
        form.addProperty(loginType.setLayout(Layout.create().setRow(2)));

        // Only one of these is visible at a time
        form.addProperty(oauth.setLayout(Layout.create().setRow(3)));
        form.addProperty(userPassword.setLayout(Layout.create().setRow(3)));

        form.addProperty(testConnection.setLayout(Layout.create().setOrder(99)));

        form = Form.create("Advanced", "Advanced Connection Settings");
        forms.add(form);
        form.addProperty(bulkConnection.setLayout(Layout.create().setRow(1)));
        form.addProperty(needCompression.setLayout(Layout.create().setRow(2)));
        form.addProperty(httpTraceMessage.setLayout(Layout.create().setRow(3)));
        form.addProperty(clientId.setLayout(Layout.create().setRow(4)));
        form.addProperty(timeout.setLayout(Layout.create().setRow(5)));
        form.addProperty(proxy.setLayout(Layout.create().setRow(5)));

        wizard = Wizard.create("Connection", "Salesforce Connection");
        wizards.add(wizard);
        wizard.addForm(form);

        refreshLayout();
    }

    public ValidationResult validateLoginType() {
        refreshLayout();
        return new ValidationResult();
    }

    public ValidationResult validateTestConnection() {
        // Do the actions to get the connection and return if it worked
        return new ValidationResult();
    }


    @Override public void refreshLayout() {
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
