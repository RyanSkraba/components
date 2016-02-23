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

import static org.talend.daikon.properties.PropertyFactory.*;
import static org.talend.daikon.properties.presentation.Widget.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.ProxyProperties;
import org.talend.components.common.oauth.OauthProperties;
import org.talend.components.salesforce.runtime.SalesforceSourceOrSink;
import org.talend.components.salesforce.tsalesforceconnection.TSalesforceConnectionDefinition;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.presentation.Widget.WidgetType;

public class SalesforceConnectionProperties extends ComponentProperties implements SalesforceProvideConnectionProperties {

    public static final String URL = "https://www.salesforce.com/services/Soap/u/34.0";

    public static final String OAUTH_URL = "https://login.salesforce.com/services/oauth2";

    public Property endpoint = (Property)newString("endpoint").setRequired().setDefaultValue(URL);

    public static final String FORM_WIZARD = "Wizard";

    //
    // Properties
    //

    // Only for the wizard use
    public Property name = (Property) newString("name").setRequired();

    public Property referencedComponentId = newEnum("referencedComponentId");

    public static final String LOGIN_BASIC = "Basic";

    public static final String LOGIN_OAUTH = "OAuth";

    public Property loginType = (Property) newEnum("loginType", LOGIN_BASIC, LOGIN_OAUTH).setRequired();

    public Property bulkConnection = newBoolean("bulkConnection"); //$NON-NLS-1$

    public Property needCompression = newBoolean("needCompression"); //$NON-NLS-1$

    public Property timeout = newInteger("timeout"); //$NON-NLS-1$

    public Property httpTraceMessage = newBoolean("httpTraceMessage"); //$NON-NLS-1$

    public Property httpChunked = newBoolean("httpChunked"); //$NON-NLS-1$

    public Property clientId = newString("clientId"); //$NON-NLS-1$

    //
    // Presentation items
    //
    public PresentationItem testConnection = new PresentationItem("testConnection", "Test connection");

    public PresentationItem advanced = new PresentationItem("advanced", "Advanced...");

    //
    // Nested property collections
    //
    private static final String OAUTH = "oauth";

    public OauthProperties oauth = new OauthProperties(OAUTH);

    private static final String USERPASSWORD = "userPassword";

    public SalesforceUserPasswordProperties userPassword = new SalesforceUserPasswordProperties(USERPASSWORD);

    public ProxyProperties proxy = new ProxyProperties("proxy");

    public SalesforceConnectionProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        loginType.setValue(LOGIN_BASIC);
        endpoint.setValue("\""+URL+"\"");

        Form wizardForm = new Form(this, FORM_WIZARD);
        wizardForm.addRow(name);
        wizardForm.addRow(widget(loginType).setDeemphasize(true));
        wizardForm.addRow(endpoint);
        wizardForm.addRow(oauth.getForm(Form.MAIN));
        wizardForm.addRow(userPassword.getForm(Form.MAIN));
        wizardForm.addRow(widget(advanced).setWidgetType(WidgetType.BUTTON));
        wizardForm.addColumn(widget(testConnection).setLongRunning(true).setWidgetType(WidgetType.BUTTON));

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(loginType);
        mainForm.addRow(endpoint);
        mainForm.addRow(oauth.getForm(Form.MAIN));
        mainForm.addRow(userPassword.getForm(Form.MAIN));

        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(bulkConnection);
        advancedForm.addRow(needCompression);
        advancedForm.addRow(httpTraceMessage);
        advancedForm.addRow(httpChunked);
        advancedForm.addRow(clientId);
        advancedForm.addRow(timeout);
        advancedForm.addRow(proxy.getForm(Form.MAIN));
        advanced.setFormtoShow(advancedForm);

        Form refForm = new Form(this, Form.REFERENCE);
        Widget compListWidget = widget(referencedComponentId).setWidgetType(WidgetType.COMPONENT_REFERENCE);
        compListWidget.setReferencedComponentName(TSalesforceConnectionDefinition.COMPONENT_NAME);
        refForm.addRow(compListWidget);
        refForm.addRow(mainForm);
    }

    public void afterLoginType() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(FORM_WIZARD));
    }

    public void afterReferencedComponentId() {
        refreshLayout(getForm(Form.REFERENCE));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public ValidationResult validateTestConnection() throws Exception {
        ValidationResult vr = SalesforceSourceOrSink.validateConnection(this);
        if (vr.getStatus() == ValidationResult.Result.OK) {
            getForm(FORM_WIZARD).setAllowForward(true);
        } else {
            getForm(FORM_WIZARD).setAllowForward(false);
        }
        return vr;
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN) || form.getName().equals(FORM_WIZARD)) {
            if (LOGIN_OAUTH.equals(loginType.getValue())) {
                form.getWidget(OAUTH).setVisible(true);
                form.getWidget(USERPASSWORD).setVisible(false);
            } else if (LOGIN_BASIC.equals(loginType.getValue())) {
                form.getWidget(OAUTH).setVisible(false);
                form.getWidget(USERPASSWORD).setVisible(true);
            } else {
                throw new RuntimeException("Enum value should be handled :" + loginType.getValue());
            }
        }

        boolean useOtherConnection = referencedComponentId != null;
        getForm(Form.REFERENCE).getWidget(SalesforceConnectionProperties.class).setVisible(!useOtherConnection);
        // FIXME - what about the advanced form?
    }

    @Override
    public SalesforceConnectionProperties getConnectionProperties() {
        return this;
    }
}
