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

import com.fasterxml.jackson.annotation.JsonRootName;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.PresentationItem;
import org.talend.components.api.properties.ValidationResult;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget.WidgetType;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.common.ProxyProperties;
import org.talend.components.common.UserPasswordProperties;
import org.talend.components.common.oauth.OauthProperties;

import static org.talend.components.api.properties.presentation.Widget.widget;
import static org.talend.components.api.schema.SchemaFactory.newSchemaElement;

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
    public SchemaElement url = newSchemaElement("url", "Salesforce URL").setRequired(true); //$NON-NLS-1$//$NON-NLS-2$

    public enum LoginType {
        BASIC,
        OAUTH
    }

    public SchemaElement loginType = newSchemaElement(SchemaElement.Type.ENUM, "loginType", "Connection type").setRequired(true);

    public SchemaElement bulkConnection = newSchemaElement(SchemaElement.Type.BOOLEAN, "bulkConnection", "Bulk Connection");

    public SchemaElement needCompression = newSchemaElement(SchemaElement.Type.BOOLEAN, "needCompression", "Need compression");

    public SchemaElement timeout = newSchemaElement(SchemaElement.Type.INT, "timeout", "Timeout");

    public SchemaElement httpTraceMessage = newSchemaElement("httpTraceMessage", "Trace HTTP message");

    public SchemaElement clientId = newSchemaElement("clientId", "Client Id");

    //
    // Presentation items
    //
    public PresentationItem connectionDesc = new PresentationItem("connectionDesc",
            "Complete these fields in order to connect to your Salesforce account");

    public PresentationItem testConnection = new PresentationItem("testConnection", "Test connection");

    public PresentationItem advanced = new PresentationItem("advanced", "Advanced...");

    //
    // Nested property collections
    //
    public OauthProperties oauth = new OauthProperties();

    public UserPasswordProperties userPassword = new UserPasswordProperties();

    public ProxyProperties proxy = new ProxyProperties();

    public static final String MAIN = "Main";

    public static final String ADVANCED = "Advanced";

    @Override protected void setupLayout() {
        super.setupLayout();

        setValue(loginType, LoginType.BASIC);

        Form connectionForm = Form.create(this, MAIN, "Salesforce Connection Settings");
        connectionForm.addRow(connectionDesc);

        connectionForm.addRow(widget(loginType).setDeemphasize(true));

        // Only one of these is visible at a time
        connectionForm.addRow(oauth.getForm(OauthProperties.OAUTH));
        connectionForm.addRow(userPassword.getForm(UserPasswordProperties.USERPASSWORD));

        connectionForm.addRow(url);

        connectionForm.addRow(widget(advanced).setWidgetType(WidgetType.BUTTON));
        connectionForm.addColumn(widget(testConnection).setLongRunning(true).setWidgetType(WidgetType.BUTTON));

        refreshLayout(connectionForm);

        Form advancedForm = Form.create(this, ADVANCED, "Advanced Connection Settings");
        advancedForm.addRow(bulkConnection);
        advancedForm.addRow(needCompression);
        advancedForm.addRow(httpTraceMessage);
        advancedForm.addRow(clientId);
        advancedForm.addRow(timeout);
        advancedForm.addRow(proxy.getForm(ProxyProperties.PROXY));
        refreshLayout(advancedForm);
    }

    public void afterLoginType() {
        refreshLayout(getForm(MAIN));
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
        if (form.getName().equals(setupFormName(MAIN))) {
            switch ((LoginType) getValue(loginType)) {
            case OAUTH:
                form.getWidget(oauth.setupFormName(OauthProperties.OAUTH)).setVisible(true);
                setValue(url, "https://login.salesforce.com/services/oauth2");
                form.getWidget(userPassword.setupFormName(UserPasswordProperties.USERPASSWORD)).setVisible(false);
                break;
            case BASIC:
                form.getWidget(oauth.setupFormName(OauthProperties.OAUTH)).setVisible(false);
                setValue(url, "https://www.salesforce.com/services/Soap/u/34.0");
                form.getWidget(userPassword.setupFormName(UserPasswordProperties.USERPASSWORD)).setVisible(true);
                break;
            default:
                throw new RuntimeException("Enum value should be handled :" + getValue(loginType));
            }
        }
    }

}
