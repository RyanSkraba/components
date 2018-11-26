// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.netsuite.NetSuiteComponentDefinition;
import org.talend.components.netsuite.NetSuiteProvideConnectionProperties;
import org.talend.components.netsuite.NetSuiteRuntime;
import org.talend.components.netsuite.NetSuiteVersion;
import org.talend.daikon.java8.Function;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResultMutable;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.serialize.PostDeserializeSetup;

/**
 * Properties of NetSuite connection component.
 */
public class NetSuiteConnectionProperties extends ComponentPropertiesImpl
        implements NetSuiteProvideConnectionProperties {

    private static final Logger LOG = LoggerFactory.getLogger(NetSuiteConnectionProperties.class);

    public static final String FORM_WIZARD = "Wizard";

    public static final NetSuiteVersion DEFAULT_API_VERSION = new NetSuiteVersion(2018, 2);

    public static final String DEFAULT_ENDPOINT_URL =
            "https://webservices.netsuite.com/services/NetSuitePort_" + DEFAULT_API_VERSION.getMajorAsString();

    /**
     * List of versions supported by NetSuite components.
     */
    public static final List<String> API_VERSIONS =
            Collections.unmodifiableList(Arrays.asList("2018.2", "2016.2", "2014.2"));

    public final Property<String> name = newString("name").setRequired();

    public final Property<String> endpoint = newString("endpoint").setRequired();

    public final Property<String> apiVersion = newString("apiVersion").setRequired();

    public final Property<String> email = newString("email").setRequired();

    public final Property<String> password = newProperty("password")
            .setRequired()
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public final Property<Integer> role = newInteger("role").setRequired();

    public final Property<String> account = newString("account").setRequired();

    public final Property<String> applicationId = newString("applicationId");

    /**
     * Specifies whether NetSuite customizations are enabled.
     * If customizations are enabled then NetSuite runtime retrieves custom record types and
     * custom fields which are exposed for components.
     */
    public final Property<Boolean> customizationEnabled = newBoolean("customizationEnabled");
    
    public final Property<Boolean> useRequestLevelCredentials = newBoolean("useRequestLevelCredentials");

    public final PresentationItem testConnection = new PresentationItem("testConnection", "Test connection");

    public final ComponentReferenceProperties<NetSuiteConnectionProperties> referencedComponent =
            new ComponentReferenceProperties("referencedComponent", NetSuiteConnectionDefinition.COMPONENT_NAME);

    /**
     * Holds data that can be used by NetSuite runtime when components are edited by a component designer.
     * This object is not serialized and intended to be used in design time only.
     */
    protected transient NetSuiteRuntime.Context designTimeContext;

    public NetSuiteConnectionProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        endpoint.setValue(DEFAULT_ENDPOINT_URL);
        apiVersion.setValue(DEFAULT_API_VERSION.getMajorAsString("."));
        apiVersion.setPossibleValues(API_VERSIONS);
        email.setValue("youremail@yourcompany.com");
        role.setValue(3);
        account.setValue("");
        applicationId.setValue("");
        customizationEnabled.setValue(true);
        useRequestLevelCredentials.setValue(false);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(endpoint);
        mainForm.addColumn(widget(apiVersion).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addRow(email);
        mainForm.addRow(widget(password).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        mainForm.addRow(role);
        mainForm.addRow(account);
        mainForm.addRow(applicationId);

        Form advForm = new Form(this, Form.ADVANCED);
        advForm.addRow(customizationEnabled);
        advForm.addRow(useRequestLevelCredentials);

        // A form for a reference to a connection
        Form refForm = Form.create(this, Form.REFERENCE);
        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        refForm.addRow(compListWidget);
        refForm.addRow(mainForm);

        // Wizard
        Form wizardForm = Form.create(this, FORM_WIZARD);
        wizardForm.addRow(name);
        wizardForm.addRow(endpoint);
        wizardForm.addColumn(widget(apiVersion).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        wizardForm.addRow(email);
        wizardForm.addRow(widget(password).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        wizardForm.addRow(role);
        wizardForm.addRow(account);
        wizardForm.addRow(applicationId);
        wizardForm.addColumn(widget(testConnection).setWidgetType(Widget.BUTTON_WIDGET_TYPE).setLongRunning(true));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        String refComponentId = getReferencedComponentId();
        boolean refConnectionUsed =
                refComponentId != null && refComponentId.startsWith(NetSuiteConnectionDefinition.COMPONENT_NAME);

        if (form.getName().equals(Form.MAIN) || form.getName().equals(FORM_WIZARD)) {
            form.getWidget(endpoint.getName()).setHidden(refConnectionUsed);
            form.getWidget(apiVersion.getName()).setHidden(refConnectionUsed);
            form.getWidget(email.getName()).setHidden(refConnectionUsed);
            form.getWidget(password.getName()).setHidden(refConnectionUsed);
            form.getWidget(role.getName()).setHidden(refConnectionUsed);
            form.getWidget(account.getName()).setHidden(refConnectionUsed);
            form.getWidget(applicationId.getName()).setHidden(refConnectionUsed);

        } else if (form.getName().equals(Form.ADVANCED)) {
            form.getWidget(customizationEnabled.getName()).setHidden(refConnectionUsed);
            form.getWidget(useRequestLevelCredentials.getName()).setHidden(refConnectionUsed);
        }
    }

    @Override
    public NetSuiteConnectionProperties getConnectionProperties() {
        return getEffectiveConnectionProperties();
    }

    /**
     * Return connection properties object which is currently in effect.
     *
     * <p>
     * If this object references to another connection component then a referenced
     * connection properties will be returned. Otherwise, this connection properties
     * object will be returned.
     *
     * @return connection properties object
     */
    public NetSuiteConnectionProperties getEffectiveConnectionProperties() {
        String refComponentId = getReferencedComponentId();
        if (refComponentId != null) {
            NetSuiteConnectionProperties properties = getReferencedConnectionProperties();
            if (properties == null) {
                LOG.error("Connection has a reference to '{}' but the referenced object is null!", refComponentId);
            }
            return properties;
        }
        return this;
    }

    @Override
    public String getReferencedComponentId() {
        return referencedComponent.componentInstanceId.getStringValue();
    }

    /**
     * Return referenced connection properties.
     *
     * @return referenced connection properties or {@code null}
     */
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

    /**
     * Return version of NetSuite.
     *
     * @return version object
     */
    public NetSuiteVersion getApiVersion() {
        if (apiVersion.getValue() != null) {
            String value = apiVersion.getStringValue();
            return NetSuiteVersion.parseVersion(value);
        }
        String endpointUrl = endpoint.getStringValue();
        return NetSuiteVersion.detectVersion(endpointUrl);
    }

    public ValidationResult validateTestConnection() throws Exception {
        ValidationResult vr = withRuntime(this, new Function<NetSuiteRuntime, ValidationResult>() {

            @Override
            public ValidationResult apply(NetSuiteRuntime runtimeService) {
                return runtimeService.validateConnection(NetSuiteConnectionProperties.this);
            }
        });
        ValidationResultMutable vrm = new ValidationResultMutable(vr);
        if (vrm.getStatus() == ValidationResult.Result.OK) {
            vrm.setMessage(getI18nMessage("message.connectionSuccessful"));
            getForm(FORM_WIZARD).setAllowForward(true);
        } else {
            getForm(FORM_WIZARD).setAllowForward(false);
        }
        return vrm;
    }

    /**
     * Return design-time context object for this connection properties.
     *
     * @return context object
     */
    public NetSuiteRuntime.Context getDesignTimeContext() {
        // If the component refers to another component
        // then we should use design-time context from referenced connection properties.
        NetSuiteConnectionProperties refProps = referencedComponent.getReference();
        if (refProps != null) {
            return refProps.getDesignTimeContext();
        }
        // Lazily create and return context object.
        if (designTimeContext == null) {
            designTimeContext = new NetSuiteComponentDefinition.DesignTimeContext();
        }
        return designTimeContext;
    }

    @Override
    public boolean postDeserialize(int version, PostDeserializeSetup setup, boolean persistent) {
        boolean migrated = super.postDeserialize(version, setup, persistent);
        migrateApiVersion();
        return migrated;
    }

    /**
     * Performs initialization of {@link #apiVersion} property for old components
     * that didn't have this property.
     */
    private void migrateApiVersion() {
        if (apiVersion.getValue() == null) {
            if (endpoint.getValue() != null) {
                String endpointUrl = endpoint.getStringValue();
                try {
                    NetSuiteVersion nsVersion = NetSuiteVersion.detectVersion(endpointUrl);
                    apiVersion.setValue(nsVersion.getMajorAsString("."));
                } catch (IllegalArgumentException e) {
                    // API version couldn't be detected, use default version
                    apiVersion.setValue(DEFAULT_API_VERSION.getMajorAsString("."));
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
        // Initialize possible values for apiVersion property.
        if (apiVersion.getPossibleValues().isEmpty()) {
            apiVersion.setPossibleValues(API_VERSIONS);
        }
    }
}
