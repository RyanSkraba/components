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

import static org.talend.components.api.properties.presentation.Widget.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.PresentationItem;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.ValidationResult;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget.WidgetType;
import org.talend.components.common.ProxyProperties;
import org.talend.components.common.UserPasswordProperties;
import org.talend.components.common.oauth.OauthProperties;

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

    public static final String       MAIN             = "Main";

    public static final String       ADVANCED         = "Advanced";

    @Override
    protected void setupLayout() {
        super.setupLayout();

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
        advancedForm.addColumn(proxy);
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
            switch (loginType.getValue()) {
            case OAUTH:
                form.getWidget(oauth.setupFormName(OauthProperties.OAUTH)).setVisible(true);
                url.setValue("https://login.salesforce.com/services/oauth2");
                form.getWidget(userPassword.setupFormName(UserPasswordProperties.USERPASSWORD)).setVisible(false);
                break;
            case BASIC:
                form.getWidget(oauth.setupFormName(OauthProperties.OAUTH)).setVisible(false);
                url.setValue("https://www.salesforce.com/services/Soap/u/34.0");
                form.getWidget(userPassword.setupFormName(UserPasswordProperties.USERPASSWORD)).setVisible(true);
                break;
            default:
                throw new RuntimeException("Enum value should be handled :" + loginType.getValue());
            }
        }
    }

}
