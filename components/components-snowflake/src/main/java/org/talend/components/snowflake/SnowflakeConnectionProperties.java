// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.Properties;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.common.UserPasswordProperties;
import org.talend.components.snowflake.runtime.SnowflakeSourceOrSink;
import org.talend.components.snowflake.tsnowflakeconnection.TSnowflakeConnectionDefinition;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResultMutable;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.serialize.PostDeserializeSetup;
import org.talend.daikon.serialize.migration.SerializeSetVersion;

public class SnowflakeConnectionProperties extends ComponentPropertiesImpl implements SnowflakeProvideConnectionProperties, SerializeSetVersion {

    private static final I18nMessages i18nMessages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(SnowflakeConnectionProperties.class);

    private static final int SPECIFY_LOGIN_TIMEOUT_VERSION_NUMBER = 1;

    protected static final int DEFAULT_LOGIN_TIMEOUT = 15;

    private static final String USERPASSWORD = "userPassword";

    public static final String FORM_WIZARD = "Wizard";

    // Only for the wizard use
    public Property<String> name = newString("name").setRequired();

    public enum Tracing {
        OFF("OFF"),
        SEVERE("SEVERE"),
        WARNING("WARNING"),
        INFO("INFO"),
        CONFIG("CONFIG"),
        FINE("FINE"),
        FINER("FINER"),
        FINEST("FINEST"),
        ALL("ALL");

        String value;

        private Tracing(String val) {
            this.value = val;
        }
    }

    public Property<Integer> loginTimeout = newInteger("loginTimeout");

    public Property<String> account = newString("account").setRequired(); //$NON-NLS-1$

    public UserPasswordProperties userPassword = new UserPasswordProperties(USERPASSWORD);

    public Property<String> warehouse = newString("warehouse"); //$NON-NLS-1$

    public Property<String> db = newString("db").setRequired(); //$NON-NLS-1$

    public Property<String> schemaName = newString("schemaName").setRequired(); //$NON-NLS-1$

    public Property<String> role = newString("role"); //$NON-NLS-1$

    public Property<Tracing> tracing = newEnum("tracing", Tracing.class); //$NON-NLS-1$

    // Presentation items
    public PresentationItem testConnection = new PresentationItem("testConnection", "Test connection");

    public PresentationItem advanced = new PresentationItem("advanced", "Advanced...");

    public ComponentReferenceProperties<SnowflakeConnectionProperties> referencedComponent = new ComponentReferenceProperties<>(
            "referencedComponent", TSnowflakeConnectionDefinition.COMPONENT_NAME);

    public SnowflakeConnectionProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        loginTimeout.setValue(DEFAULT_LOGIN_TIMEOUT);
        tracing.setValue(Tracing.OFF);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form wizardForm = Form.create(this, FORM_WIZARD);
        wizardForm.addRow(name);
        wizardForm.addRow(account);
        wizardForm.addRow(userPassword.getForm(Form.MAIN));
        wizardForm.addRow(warehouse);
        wizardForm.addRow(schemaName);
        wizardForm.addRow(db);
        wizardForm.addRow(widget(advanced).setWidgetType(Widget.BUTTON_WIDGET_TYPE));
        wizardForm.addColumn(widget(testConnection).setLongRunning(true).setWidgetType(Widget.BUTTON_WIDGET_TYPE));

        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(account);
        mainForm.addRow(userPassword.getForm(Form.MAIN));
        mainForm.addRow(warehouse);
        mainForm.addRow(schemaName);
        mainForm.addRow(db);

        Form advancedForm = Form.create(this, Form.ADVANCED);
        advancedForm.addRow(loginTimeout);
        advancedForm.addRow(widget(tracing).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        advancedForm.addRow(role);
        advanced.setFormtoShow(advancedForm);

        // A form for a reference to a connection, used in a tSnowflakeInput for example
        Form refForm = Form.create(this, Form.REFERENCE);
        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        refForm.addRow(compListWidget);
        refForm.addRow(mainForm);
    }

    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.REFERENCE));
        refreshLayout(getForm(Form.ADVANCED));
    }

    protected void setHiddenProps(Form form, boolean hidden) {
        form.getWidget(USERPASSWORD).setHidden(hidden);
        form.getWidget(account.getName()).setHidden(hidden);
        form.getWidget(warehouse.getName()).setHidden(hidden);
        form.getWidget(schemaName.getName()).setHidden(hidden);
        form.getWidget(db.getName()).setHidden(hidden);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        boolean useOtherConnection = getReferencedComponentId() != null;
        if (form.getName().equals(Form.MAIN) || form.getName().equals(FORM_WIZARD)) {
            if (useOtherConnection) {
                setHiddenProps(form, true);
            } else {
                setHiddenProps(form, false);
                // Do nothing
                form.setHidden(false);
            }
        }

        if (form.getName().equals(Form.ADVANCED)) {
            if (useOtherConnection) {
                form.setHidden(true);
            } else {
                form.setHidden(false);
            }
        }
    }

    public ValidationResult validateTestConnection() throws Exception {
        ValidationResultMutable vr = new ValidationResultMutable(SnowflakeSourceOrSink.validateConnection(this));
        if (vr.getStatus() == ValidationResult.Result.OK) {
            vr.setMessage(i18nMessages.getMessage("messages.connectionSuccessful"));
            getForm(FORM_WIZARD).setAllowForward(true);
        } else {
            getForm(FORM_WIZARD).setAllowForward(false);
        }
        return vr;
    }

    @Override
    public SnowflakeConnectionProperties getConnectionProperties() {
        return this;
    }

    public String getReferencedComponentId() {
        return referencedComponent.componentInstanceId.getStringValue();
    }

    public SnowflakeConnectionProperties getReferencedConnectionProperties() {
        SnowflakeConnectionProperties refProps = referencedComponent.getReference();
        if (refProps != null) {
            return refProps;
        }
        return null;
    }

    public Properties getJdbcProperties() {
        String user = userPassword.userId.getStringValue();
        String password = userPassword.password.getStringValue();
        String loginTimeout = String.valueOf(this.loginTimeout.getValue());

        Properties properties = new Properties();

        if (user != null) {
            properties.put("user", user);
        }

        if (password != null) {
            properties.put("password", password);
        }

        if (loginTimeout != null) {
            properties.put("loginTimeout", String.valueOf(this.loginTimeout.getValue()));
        }

        return properties;
    }

    public String getConnectionUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        String account = this.account.getStringValue();

        if (account == null || account.isEmpty()) {
            throw new IllegalArgumentException(i18nMessages.getMessage("error.missingAccount"));
        }

        String warehouse = this.warehouse.getStringValue();
        String db = this.db.getStringValue();
        String schema = this.schemaName.getStringValue();
        String role = this.role.getStringValue();
        String tracing = this.tracing.getStringValue();

        appendProperty("warehouse", warehouse, stringBuilder);
        appendProperty("db", db, stringBuilder);
        appendProperty("schema", schema, stringBuilder);
        appendProperty("role", role, stringBuilder);
        appendProperty("tracing", tracing, stringBuilder);

        return new StringBuilder().append("jdbc:snowflake://").append(account).append(".snowflakecomputing.com").append("/?")
                .append(stringBuilder).toString();
    }

    private void appendProperty(String propertyName, String propertyValue, StringBuilder builder) {
        if (propertyValue != null && !propertyValue.isEmpty()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(propertyName).append("=").append(propertyValue);
        }
    }

    @Override
    public boolean postDeserialize(int version, PostDeserializeSetup setup, boolean persistent) {
        boolean migrated = super.postDeserialize(version, setup, persistent);
        if (version < SPECIFY_LOGIN_TIMEOUT_VERSION_NUMBER && loginTimeout.getValue() == null) {
            loginTimeout.setValue(DEFAULT_LOGIN_TIMEOUT);
            migrated = true;
        }
        return migrated;
    }

    @Override
    public int getVersionNumber() {
        return 1;
    }

}
