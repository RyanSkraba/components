// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce;

import org.talend.components.api.ComponentProperties;
import org.talend.components.api.properties.PresentationItem;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.ValidationResult;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Layout;
import org.talend.components.api.properties.presentation.Wizard;
import org.talend.components.common.oauth.OauthProperties;
import org.talend.components.common.ProxyProperties;
import org.talend.components.common.UserPasswordProperties;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("salesforceConnectionProperties")
public class SalesforceConnectionProperties extends ComponentProperties {

    public SalesforceConnectionProperties() {
        super();
        setupLayout();
    }

    //
    // Properties
    //

    // public String apiVersion;
    public Property<String> url = new Property<String>("url", "Salesforce URL").setRequired(true) //$NON-NLS-1$//$NON-NLS-2$
            .setValue("https://www.salesforce.com/services/Soap/u/34.0"); //$NON-NLS-1$

    public enum LoginType {
                           BASIC,
                           OAUTH
    }

    public Property<LoginType>       loginType        = new Property<LoginType>("loginType", "Connection type").setRequired(true)
            .setValue(LoginType.BASIC);

    public Property<Boolean>         bulkConnection   = new Property<Boolean>("bulkConnection", "Bulk Connection");

    public Property<Boolean>         needCompression  = new Property<Boolean>("needCompression", "Need compression");

    public Property<Integer>         timeout          = new Property<Integer>("timeout", "Timeout").setValue(0);

    public Property<Boolean>         httpTraceMessage = new Property<Boolean>("httpTraceMessage", "Trace HTTP message");

    public Property<String>          clientId         = new Property<String>("clientId", "Client Id");

    public Property<ProxyProperties> proxy            = new Property<ProxyProperties>("proxy", "Proxy")
            .setValue(new ProxyProperties());

    //
    // Presentation items
    //
    public PresentationItem          connectionDesc   = new PresentationItem("connectionDesc",
            "Complete these fields in order to connect to your Salesforce account");

    public PresentationItem          testConnection   = new PresentationItem("testConnection", "Test connection");

    public PresentationItem          advanced         = new PresentationItem("advanced", "Advanced...");

    //
    // Nested property collections
    //
    public OauthProperties           oauth            = new OauthProperties();

    public UserPasswordProperties    userPassword     = new UserPasswordProperties();

    public static final String       CONNECTION       = "Connection";

    public static final String       ADVANCED         = "Advanced";

    @Override
    protected void setupLayout() {
        super.setupLayout();

        Form connectionForm = Form.create(this, CONNECTION, "Salesforce Connection Settings");

        connectionForm.addChild(connectionDesc, Layout.create().setRow(1));

        connectionForm.addChild(name, Layout.create().setRow(2));
        connectionForm.addChild(loginType, Layout.create().setRow(3).setDeemphasize(true));

        // Only one of these is visible at a time
        connectionForm.addChild(oauth.getForm(OauthProperties.OAUTH), Layout.create().setRow(4));
        connectionForm.addChild(userPassword.getForm(UserPasswordProperties.USERPASSWORD), Layout.create().setRow(4));

        connectionForm.addChild(url, Layout.create().setRow(5));

        connectionForm.addChild(advanced, Layout.create().setRow(6).setOrder(1).setWidgetType(Layout.WidgetType.BUTTON));
        connectionForm.addChild(testConnection,
                Layout.create().setRow(6).setOrder(2).setLongRunning(true).setWidgetType(Layout.WidgetType.BUTTON));
        refreshLayout(connectionForm);

        Form advancedForm = Form.create(this, ADVANCED, "Advanced Connection Settings");
        advancedForm.addChild(bulkConnection, Layout.create().setRow(1));
        advancedForm.addChild(needCompression, Layout.create().setRow(2));
        advancedForm.addChild(httpTraceMessage, Layout.create().setRow(3));
        advancedForm.addChild(clientId, Layout.create().setRow(4));
        advancedForm.addChild(timeout, Layout.create().setRow(5));
        advancedForm.addChild(proxy, Layout.create().setRow(5));
        refreshLayout(advancedForm);

        Wizard wizard = Wizard.create(this, "Connection", "Salesforce Connection");
        // TODO - need to set the icon for the wizard
        wizard.addForm(advancedForm);
    }

    public void afterLoginType() {
        refreshLayout(getForm(CONNECTION));
    }

    public ValidationResult validateTestConnection() throws Exception {
        SalesforceRuntime conn = new SalesforceRuntime();
        conn.connect(this);
        // FIXME - handle the error catching
        return new ValidationResult();
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(CONNECTION)) {
            // TODO - need a way to tell the UI that the form's layout needs to be refreshed
            // based on this change
            switch (loginType.getValue()) {
            case OAUTH:
                form.getLayout(OauthProperties.OAUTH).setVisible(true);
                form.getLayout(UserPasswordProperties.USERPASSWORD).setVisible(false);
                break;
            case BASIC:
                form.getLayout(OauthProperties.OAUTH).setVisible(false);
                form.getLayout(UserPasswordProperties.USERPASSWORD).setVisible(true);
                break;
            default:
                throw new RuntimeException("Enum value should be handled :" + loginType.getValue());
            }
        }
    }

}
