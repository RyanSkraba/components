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

import static org.talend.components.api.properties.presentation.Widget.widget;

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
        connectionForm.addChild(widget(connectionDesc).setRow(1));

        connectionForm.addChild(widget(loginType).setRow(3).setDeemphasize(true));

        // Only one of these is visible at a time
        connectionForm.addChild(widget(oauth.getForm(OauthProperties.OAUTH)).setRow(4));
        connectionForm.addChild(widget(userPassword.getForm(UserPasswordProperties.USERPASSWORD)).setRow(4));

        connectionForm.addChild(widget(url).setRow(5));

        connectionForm.addChild(widget(advanced).setRow(6).setOrder(1).setWidgetType(WidgetType.BUTTON));
        connectionForm.addChild(widget(testConnection).setRow(6).setOrder(2).setLongRunning(true).setWidgetType(WidgetType.BUTTON));
        refreshLayout(connectionForm);

        Form advancedForm = Form.create(this, ADVANCED, "Advanced Connection Settings");
        advancedForm.addChild(widget(bulkConnection).setRow(1));
        advancedForm.addChild(widget(needCompression).setRow(2));
        advancedForm.addChild(widget(httpTraceMessage).setRow(3));
        advancedForm.addChild(widget(clientId).setRow(4));
        advancedForm.addChild(widget(timeout).setRow(5));
        advancedForm.addChild(widget(proxy).setRow(5));
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
                form.getWidget(userPassword.setupFormName(UserPasswordProperties.USERPASSWORD)).setVisible(false);
                break;
            case BASIC:
                form.getWidget(oauth.setupFormName(OauthProperties.OAUTH)).setVisible(false);
                form.getWidget(userPassword.setupFormName(UserPasswordProperties.USERPASSWORD)).setVisible(true);
                break;
            default:
                throw new RuntimeException("Enum value should be handled :" + loginType.getValue());
            }
        }
    }

}
