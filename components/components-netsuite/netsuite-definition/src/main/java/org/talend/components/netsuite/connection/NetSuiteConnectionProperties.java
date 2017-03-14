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

package org.talend.components.netsuite.connection;

import static org.talend.components.netsuite.NetSuiteComponentDefinition.withRuntime;
import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.EnumSet;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.netsuite.NetSuiteComponentDefinition;
import org.talend.components.netsuite.NetSuiteProvideConnectionProperties;
import org.talend.components.netsuite.NetSuiteRuntime;
import org.talend.daikon.java8.Function;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

/**
 *
 */
public class NetSuiteConnectionProperties extends ComponentPropertiesImpl
        implements NetSuiteProvideConnectionProperties {

    public static final String FORM_WIZARD = "Wizard";

    public static final String DEFAULT_ENDPOINT_URL =
            "https://webservices.netsuite.com/services/NetSuitePort_2016_2";

    public final Property<String> name = newString("name")
            .setRequired();

    public final Property<String> endpoint = newString("endpoint")
            .setRequired();

    public final Property<String> email = newString("email")
            .setRequired();

    public final Property<String> password = newProperty("password")
            .setRequired()
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public final Property<Integer> role = newInteger("role")
            .setRequired();

    public final Property<String> account = newString("account")
            .setRequired();

    public final Property<String> applicationId = newString("applicationId");

    public final PresentationItem testConnection = new PresentationItem(
            "testConnection", "Test connection");

    public final ComponentReferenceProperties<NetSuiteConnectionProperties> referencedComponent =
            new ComponentReferenceProperties("referencedComponent", NetSuiteConnectionDefinition.COMPONENT_NAME);

    protected transient NetSuiteRuntime.Context designTimeContext;

    public NetSuiteConnectionProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        endpoint.setValue(DEFAULT_ENDPOINT_URL);
        email.setValue("youremail@yourcompany.com");
        role.setValue(3);
        account.setValue("");
        applicationId.setValue("");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(endpoint);
        mainForm.addRow(email);
        mainForm.addRow(widget(password)
                .setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        mainForm.addRow(role);
        mainForm.addRow(account);
        mainForm.addRow(applicationId);

        // A form for a reference to a connection
        Form refForm = Form.create(this, Form.REFERENCE);
        Widget compListWidget = widget(referencedComponent)
                .setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        refForm.addRow(compListWidget);
        refForm.addRow(mainForm);

        // Wizard
        Form wizardForm = Form.create(this, FORM_WIZARD);
        wizardForm.addRow(name);
        wizardForm.addRow(endpoint);
        wizardForm.addRow(email);
        wizardForm.addRow(widget(password)
                .setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        wizardForm.addRow(role);
        wizardForm.addRow(account);
        wizardForm.addRow(applicationId);
        wizardForm.addColumn(widget(testConnection)
                .setWidgetType(Widget.BUTTON_WIDGET_TYPE).setLongRunning(true));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        String refComponentId = getReferencedComponentId();
        boolean refConnectionUsed = refComponentId != null
                && refComponentId.startsWith(NetSuiteConnectionDefinition.COMPONENT_NAME);

        if (form.getName().equals(Form.MAIN) || form.getName().equals(FORM_WIZARD)) {
            form.getWidget(endpoint.getName()).setHidden(refConnectionUsed);
            form.getWidget(email.getName()).setHidden(refConnectionUsed);
            form.getWidget(password.getName()).setHidden(refConnectionUsed);
            form.getWidget(role.getName()).setHidden(refConnectionUsed);
            form.getWidget(account.getName()).setHidden(refConnectionUsed);
            form.getWidget(applicationId.getName()).setHidden(refConnectionUsed);
        }
    }

    @Override
    public NetSuiteConnectionProperties getConnectionProperties() {
        String refComponentId = getReferencedComponentId();
        return refComponentId != null ? getReferencedConnectionProperties() : this;
    }

    public String getReferencedComponentId() {
        return referencedComponent.componentInstanceId.getStringValue();
    }

    public NetSuiteConnectionProperties getReferencedConnectionProperties() {
        NetSuiteConnectionProperties refProps = referencedComponent.getReference();
        if (refProps != null) {
            return refProps;
        }
        return null;
    }

    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.REFERENCE));
    }

    public ValidationResult validateTestConnection() throws Exception {
        ValidationResult vr = withRuntime(this, new Function<NetSuiteRuntime, ValidationResult>() {
            @Override public ValidationResult apply(NetSuiteRuntime runtimeService) {
                return runtimeService.validateConnection(NetSuiteConnectionProperties.this);
            }
        });
        if (vr.getStatus() == ValidationResult.Result.OK) {
            vr.setMessage("Connection successful");
            getForm(FORM_WIZARD).setAllowForward(true);
        } else {
            getForm(FORM_WIZARD).setAllowForward(false);
        }
        return vr;
    }

    public NetSuiteRuntime.Context getDesignTimeContext() {
        NetSuiteConnectionProperties refProps = referencedComponent.getReference();
        if (refProps != null) {
            return refProps.getDesignTimeContext();
        }
        if (designTimeContext == null) {
            designTimeContext = new NetSuiteComponentDefinition.DesignTimeContext();
        }
        return designTimeContext;
    }
}
