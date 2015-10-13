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
import static org.talend.components.api.schema.SchemaFactory.newProperty;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.PresentationItem;
import org.talend.components.api.properties.ValidationResult;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget.WidgetType;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.common.ProxyProperties;
import org.talend.components.common.UserPasswordProperties;
import org.talend.components.common.oauth.OauthProperties;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("salesforceConnectionProperties")
public class SalesforceConnectionProperties extends ComponentProperties {

    public static final String URL = "https://www.salesforce.com/services/Soap/u/34.0";

    public static final String OAUTH_URL = "https://login.salesforce.com/services/oauth2";

    public static final String FORM_WIZARD = "Wizard";

    //
    // Properties
    //

    // Only for the wizard use
    public SchemaElement name = newProperty("name").setRequired(true);

    public static final String LOGIN_BASIC = "Basic";

    public static final String LOGIN_OAUTH = "OAuth";

    public SchemaElement loginType = newProperty(Type.ENUM, "loginType").setRequired(true);

    public SchemaElement bulkConnection = newProperty(SchemaElement.Type.BOOLEAN, "bulkConnection"); //$NON-NLS-1$

    public SchemaElement needCompression = newProperty(SchemaElement.Type.BOOLEAN, "needCompression"); //$NON-NLS-1$

    public SchemaElement timeout = newProperty(SchemaElement.Type.INT, "timeout"); //$NON-NLS-1$

    public SchemaElement httpTraceMessage = newProperty("httpTraceMessage"); //$NON-NLS-1$

    public SchemaElement clientId = newProperty("clientId"); //$NON-NLS-1$

    //
    // Presentation items
    //
    public PresentationItem testConnection = new PresentationItem("testConnection", "Test connection");

    public PresentationItem advanced = new PresentationItem("advanced", "Advanced...");

    //
    // Nested property collections
    //
    public OauthProperties oauth = new OauthProperties();

    public SalesforceUserPasswordProperties userPassword = new SalesforceUserPasswordProperties();

    public ComponentProperties proxy = new ProxyProperties();

    @Override
    public ComponentProperties init() {
        super.init();
        List<String> loginTypes = new ArrayList<>();
        loginTypes.add(LOGIN_BASIC);
        loginTypes.add(LOGIN_OAUTH);
        loginType.setPossibleValues(loginTypes);
        return this;
    }

    @Override
    protected void setupLayout() {
        super.setupLayout();

        setValue(loginType, LOGIN_BASIC);

        Form wizardForm = Form.create(this, FORM_WIZARD, getI18nMessage("property.form.Main.title"));
        wizardForm.setSubtitle(getI18nMessage("property.form.Main.subtitle"));
        wizardForm.addRow(name);
        wizardForm.addRow(widget(loginType).setDeemphasize(true));

        // Only one of these is visible at a time
        wizardForm.addRow(oauth.getForm(Form.MAIN));
        wizardForm.addRow(userPassword.getForm(Form.MAIN));

        wizardForm.addRow(widget(advanced).setWidgetType(WidgetType.BUTTON));
        wizardForm.addColumn(widget(testConnection).setLongRunning(true).setWidgetType(WidgetType.BUTTON));

        Form mainForm = Form.create(this, Form.MAIN, getI18nMessage("property.form.Main.title"));
        mainForm.addRow(widget(loginType).setDeemphasize(true));
        // Only one of these is visible at a time
        mainForm.addRow(oauth.getForm(Form.MAIN));
        mainForm.addRow(userPassword.getForm(Form.MAIN));
        mainForm.addRow(widget(testConnection).setLongRunning(true).setWidgetType(WidgetType.BUTTON));

        Form advancedForm = Form.create(this, Form.ADVANCED, getI18nMessage("property.form.Advanced.title"));
        advancedForm.addRow(bulkConnection);
        advancedForm.addRow(needCompression);
        advancedForm.addRow(httpTraceMessage);
        advancedForm.addRow(clientId);
        advancedForm.addRow(timeout);
        advancedForm.addRow(proxy.getForm(Form.MAIN));
        advanced.setFormtoShow(advancedForm);
    }

    public void afterLoginType() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(FORM_WIZARD));
    }

    public ValidationResult validateTestConnection() throws Exception {
        SalesforceRuntime conn = new SalesforceRuntime();
        return conn.connectWithResult(this);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN) || form.getName().equals(FORM_WIZARD)) {
            if (LOGIN_OAUTH.equals(getValue(loginType))) {
                form.getWidget(OauthProperties.class).setVisible(true);
                form.getWidget(SalesforceUserPasswordProperties.class).setVisible(false);
            } else if (LOGIN_BASIC.equals(getValue(loginType))) {
                form.getWidget(OauthProperties.class).setVisible(false);
                form.getWidget(SalesforceUserPasswordProperties.class).setVisible(true);
            } else {
                throw new RuntimeException("Enum value should be handled :" + getValue(loginType));
            }
        }
    }

}
