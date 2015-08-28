package org.talend.components.salesforce;

import org.talend.component.ComponentProperties;
import org.talend.component.properties.Property;
import org.talend.component.properties.ValidationResult;
import org.talend.component.properties.presentation.Form;
import org.talend.component.properties.presentation.Layout;
import org.talend.component.properties.presentation.Wizard;
import org.talend.components.common.OauthProperties;
import org.talend.components.common.ProxyProperties;
import org.talend.components.common.UserPasswordProperties;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("salesforceConnectionProperties")
public class SalesforceConnectionProperties extends ComponentProperties {

    public SalesforceConnectionProperties() {
        super();
        setupLayout();
    }

    // public String apiVersion;
    public Property<String> url = new Property<String>("url", "Salesforce URL").setRequired(true) //$NON-NLS-1$//$NON-NLS-2$
            .setValue("https://www.salesforce.com/services/Soap/u/25.0"); //$NON-NLS-1$

    public enum LoginType {
                           BASIC,
                           OAUTH
    }

    public Property<LoginType>              loginType        = new Property<LoginType>("logintype", "Connection type")
            .setRequired(true).setValue(LoginType.BASIC);

    public Property<OauthProperties>        oauth            = new Property<OauthProperties>("oauth", "OAuth connection")
            .setValue(new OauthProperties());

    public Property<UserPasswordProperties> userPassword     = new Property<UserPasswordProperties>("userPassword",
            "Basic connection").setValue(new UserPasswordProperties());

    public Property<Boolean>                bulkConnection   = new Property<Boolean>("bulkConnection", "Bulk Connection");

    public Property<Boolean>                needCompression  = new Property<Boolean>("needCompression", "Need compression");

    // TODO - I'm not happy with this and "advanced" as properties. We need to have a way to express
    // something that's not a property (current buttons and text that needs to go on the form).
    // Perhaps make a superclass of Property which contains both Property and UIThing. The callbacks
    // need to be easy and uniform for both
    public Property<Boolean>                testConnection   = new Property<Boolean>("testConnection", "Test connection");

    public Property<Boolean>                advanced         = new Property<Boolean>("advanced", "Advanced...");

    public Property<Integer>                timeout          = new Property<Integer>("timeout", "Timeout").setValue(0);

    public Property<Boolean>                httpTraceMessage = new Property<Boolean>("httpTraceMessage", "Trace HTTP message");

    public Property<String>                 clientId         = new Property<String>("clientId", "Client Id");

    public Property<ProxyProperties>        proxy            = new Property<ProxyProperties>("proxy", "Proxy")
            .setValue(new ProxyProperties());

    @Override
    protected void setupLayout() {
        Form form;
        Wizard wizard;

        super.setupLayout();

        // TODO - we might want to allow the same property in different positions on different forms, so the
        // Layout might be per Property per Form.

        form = Form.create("Connection", "Salesforce Connection Settings");
        forms.add(form);
        form.addProperty(name, Layout.create().setRow(1));
        form.addProperty(loginType, Layout.create().setRow(2).setDeemphasize(true));

        // Only one of these is visible at a time
        form.addProperty(oauth, Layout.create().setRow(3));
        form.addProperty(userPassword, Layout.create().setRow(3));

        form.addProperty(url, Layout.create().setRow(4));

        form.addProperty(advanced, Layout.create().setRow(5).setOrder(1));
        form.addProperty(testConnection, Layout.create().setRow(5).setOrder(2).setLongRunning(true));

        form = Form.create("Advanced", "Advanced Connection Settings");
        forms.add(form);
        form.addProperty(bulkConnection, Layout.create().setRow(1));
        form.addProperty(needCompression, Layout.create().setRow(2));
        form.addProperty(httpTraceMessage, Layout.create().setRow(3));
        form.addProperty(clientId, Layout.create().setRow(4));
        form.addProperty(timeout, Layout.create().setRow(5));
        form.addProperty(proxy, Layout.create().setRow(5));

        wizard = Wizard.create("Connection", "Salesforce Connection");
        // TODO - need to set the icon for the wizard
        wizards.add(wizard);
        wizard.addForm(form);

        refreshLayout();
    }

    public ValidationResult validateLoginType() {
        refreshLayout();
        return new ValidationResult();
    }

    public ValidationResult validateTestConnection() {
        // TODO - Do the actions to get the connection and return if it worked
        return new ValidationResult();
    }

    @Override
    public void refreshLayout() {
        // switch (loginType.getValue()) {
        // case OAUTH:
        // oauth.getLayout().setVisible(true);
        // userPassword.getLayout().setVisible(false);
        // break;
        // case BASIC:
        // oauth.getLayout().setVisible(false);
        // userPassword.getLayout().setVisible(true);
        // break;
        // default:
        // throw new RuntimeException("Enum value should be handled :" + loginType.getValue());
        // }
    }

}
