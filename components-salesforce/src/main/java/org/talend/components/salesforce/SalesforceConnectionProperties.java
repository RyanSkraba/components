package org.talend.components.salesforce;

import org.talend.components.base.ComponentProperties;
import org.talend.components.base.properties.PresentationItem;
import org.talend.components.base.properties.Property;
import org.talend.components.base.properties.ValidationResult;
import org.talend.components.base.properties.presentation.Form;
import org.talend.components.base.properties.presentation.Layout;
import org.talend.components.base.properties.presentation.Wizard;
import org.talend.components.common.OauthProperties;
import org.talend.components.common.ProxyProperties;
import org.talend.components.common.UserPasswordProperties;

import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.AbstractCollection;

@JsonRootName("salesforceConnectionProperties") public class SalesforceConnectionProperties extends ComponentProperties {

    public SalesforceConnectionProperties() {
        super();
        setupLayout();
    }

    //
    // Properties
    //

    // public String apiVersion;
    public Property<String> url = new Property<String>("url", "Salesforce URL").setRequired(true) //$NON-NLS-1$//$NON-NLS-2$
            .setValue("https://www.salesforce.com/services/Soap/u/25.0"); //$NON-NLS-1$

    public enum LoginType {
        BASIC,
        OAUTH
    }

    public Property<LoginType> loginType = new Property<LoginType>("logintype", "Connection type").setRequired(true)
            .setValue(LoginType.BASIC);

    public Property<Boolean> bulkConnection = new Property<Boolean>("bulkConnection", "Bulk Connection");

    public Property<Boolean> needCompression = new Property<Boolean>("needCompression", "Need compression");

    public Property<Integer> timeout = new Property<Integer>("timeout", "Timeout").setValue(0);

    public Property<Boolean> httpTraceMessage = new Property<Boolean>("httpTraceMessage", "Trace HTTP message");

    public Property<String> clientId = new Property<String>("clientId", "Client Id");

    public Property<ProxyProperties> proxy = new Property<ProxyProperties>("proxy", "Proxy").setValue(new ProxyProperties());

    //
    // Presentation items
    //
    public PresentationItem testConnection = new PresentationItem("testConnection", "Test connection");

    public PresentationItem advanced = new PresentationItem("advanced", "Advanced...");

    //
    // Nested property collections
    //
    public OauthProperties oauth = new OauthProperties();

    public UserPasswordProperties userPassword = new UserPasswordProperties();

    public static final String CONNECTION = "Connection";

    public static final String ADVANCED = "Advanced";

    @Override protected void setupLayout() {
        super.setupLayout();

        Form connectionForm = Form.create(this, CONNECTION, "Salesforce Connection Settings");
        connectionForm.addChild(name, Layout.create().setRow(1));
        connectionForm.addChild(loginType, Layout.create().setRow(2).setDeemphasize(true));

        // Only one of these is visible at a time
        connectionForm.addChild(oauth.getForm(OauthProperties.OAUTH), Layout.create().setRow(3));
        connectionForm.addChild(userPassword.getForm(UserPasswordProperties.USERPASSWORD), Layout.create().setRow(3));

        connectionForm.addChild(url, Layout.create().setRow(4));

        connectionForm.addChild(advanced, Layout.create().setRow(5).setOrder(1).setWidgetType(Layout.WidgetType.BUTTON));
        connectionForm.addChild(testConnection,
                Layout.create().setRow(5).setOrder(2).setLongRunning(true).setWidgetType(Layout.WidgetType.BUTTON));

        Form advancedForm = Form.create(this, ADVANCED, "Advanced Connection Settings");
        advancedForm.addChild(bulkConnection, Layout.create().setRow(1));
        advancedForm.addChild(needCompression, Layout.create().setRow(2));
        advancedForm.addChild(httpTraceMessage, Layout.create().setRow(3));
        advancedForm.addChild(clientId, Layout.create().setRow(4));
        advancedForm.addChild(timeout, Layout.create().setRow(5));
        advancedForm.addChild(proxy, Layout.create().setRow(5));

        Wizard wizard = Wizard.create(this, "Connection", "Salesforce Connection");
        // TODO - need to set the icon for the wizard
        wizard.addForm(advancedForm);

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

    @Override public void refreshLayout() {
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
