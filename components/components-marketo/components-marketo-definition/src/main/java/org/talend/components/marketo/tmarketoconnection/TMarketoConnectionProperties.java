// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.tmarketoconnection;

import static org.talend.components.marketo.MarketoComponentDefinition.RUNTIME_SOURCEORSINK_CLASS;
import static org.talend.components.marketo.MarketoComponentDefinition.USE_CURRENT_JVM_PROPS;
import static org.talend.components.marketo.MarketoComponentDefinition.getSandboxedInstance;
import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.marketo.MarketoProvideConnectionProperties;
import org.talend.components.marketo.runtime.MarketoSourceOrSinkRuntime;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.sandbox.SandboxedInstance;

public class TMarketoConnectionProperties extends ComponentPropertiesImpl implements MarketoProvideConnectionProperties {

    public static final String FORM_WIZARD = "Wizard";

    public Property<String> name = newString("name").setRequired();

    //
    public PresentationItem testConnection = new PresentationItem("testConnection", "Test connection");

    public Property<String> endpoint = newString("endpoint").setRequired();

    public Property<String> secretKey = newString("secretKey").setRequired()
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public Property<String> clientAccessId = newString("clientAccessId").setRequired();

    // advanced
    public Property<Integer> timeout = newInteger("timeout");

    public Property<Integer> attemptsIntervalTime = newInteger("attemptsIntervalTime");

    public Property<Integer> maxReconnAttemps = newInteger("maxReconnAttemps");

    public ComponentReferenceProperties<TMarketoConnectionProperties> referencedComponent = new ComponentReferenceProperties<>(
            "referencedComponent", TMarketoConnectionDefinition.COMPONENT_NAME);

    public static final String DEFAULT_ENDPOINT_REST = "https://123-ABC-456.mktorest.com/rest";

    public static final String DEFAULT_ENDPOINT_SOAP = "https://123-ABC-456.mktoapi.com/soap/mktows/3_1";

    private String repositoryLocation;

    public enum APIMode {
        REST,
        SOAP
    }

    public Property<APIMode> apiMode = newEnum("apiMode", APIMode.class);

    //
    private static final Logger LOG = LoggerFactory.getLogger(TMarketoConnectionProperties.class);

    private static final long serialVersionUID = 145738798798151L;

    public TMarketoConnectionProperties(String name) {
        super(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setupProperties() {
        super.setupProperties();

        endpoint.setValue(DEFAULT_ENDPOINT_REST);
        secretKey.setValue("");
        clientAccessId.setValue("");

        timeout.setValue(60000);
        maxReconnAttemps.setValue(5);
        attemptsIntervalTime.setValue(1000);

        apiMode.setValue(APIMode.REST);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(endpoint);
        mainForm.addRow(clientAccessId);
        mainForm.addColumn(widget(secretKey).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));

        // Advanced
        Form advancedForm = Form.create(this, Form.ADVANCED);
        advancedForm.addRow(apiMode);
        advancedForm.addRow(maxReconnAttemps);
        advancedForm.addColumn(attemptsIntervalTime);

        // A form for a reference to a connection
        Form refForm = Form.create(this, Form.REFERENCE);
        Widget compListWidget = widget(referencedComponent).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        refForm.addRow(compListWidget);
        refForm.addRow(mainForm);

        // Wizard
        Form wizardForm = Form.create(this, FORM_WIZARD);
        wizardForm.addRow(name);
        wizardForm.addRow(endpoint);
        wizardForm.addRow(clientAccessId);
        wizardForm.addRow(widget(secretKey).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        wizardForm.addRow(widget(testConnection).setLongRunning(true).setWidgetType(Widget.BUTTON_WIDGET_TYPE));
        wizardForm.addRow(maxReconnAttemps);
        wizardForm.addRow(attemptsIntervalTime);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        String refComponentIdValue = getReferencedComponentId();
        boolean useOtherConnection = refComponentIdValue != null
                && refComponentIdValue.startsWith(TMarketoConnectionDefinition.COMPONENT_NAME);

        if (DEFAULT_ENDPOINT_REST.equals(endpoint.getValue()) && APIMode.SOAP.equals(apiMode.getValue())) {
            endpoint.setValue(DEFAULT_ENDPOINT_SOAP);
        }
        if (DEFAULT_ENDPOINT_SOAP.equals(endpoint.getValue()) && APIMode.REST.equals(apiMode.getValue())) {
            endpoint.setValue(DEFAULT_ENDPOINT_REST);
        }

        if (form.getName().equals(Form.MAIN) || form.getName().equals(FORM_WIZARD)) {
            form.getWidget(endpoint.getName()).setHidden(useOtherConnection);
            form.getWidget(clientAccessId.getName()).setHidden(useOtherConnection);
            form.getWidget(secretKey.getName()).setHidden(useOtherConnection);
            //
        }
        if (form.getName().equals(Form.ADVANCED)) {
            form.getWidget(apiMode.getName()).setHidden(useOtherConnection);
            form.getWidget(maxReconnAttemps.getName()).setHidden(useOtherConnection);
            form.getWidget(attemptsIntervalTime.getName()).setHidden(useOtherConnection);
        }
    }

    public void afterReferencedComponent() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
        refreshLayout(getForm(Form.REFERENCE));
    }

    public void afterApiMode() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
        refreshLayout(getForm(Form.REFERENCE));
    }

    public ValidationResult validateTestConnection() {
        try (SandboxedInstance sandboxedInstance = getSandboxedInstance(RUNTIME_SOURCEORSINK_CLASS, USE_CURRENT_JVM_PROPS)) {
            MarketoSourceOrSinkRuntime sos = (MarketoSourceOrSinkRuntime) sandboxedInstance.getInstance();
            sos.initialize(null, this);
            ValidationResult vr = sos.validateConnection(this);
            if (vr.getStatus() == ValidationResult.Result.OK) {
                getForm(FORM_WIZARD).setAllowForward(true);
                getForm(FORM_WIZARD).setAllowFinish(true);
            } else {
                getForm(FORM_WIZARD).setAllowForward(false);
            }

            return vr;
        }
    }

    public ValidationResult afterFormFinishWizard(Repository<Properties> repo) {
        try (SandboxedInstance sandboxedInstance = getSandboxedInstance(RUNTIME_SOURCEORSINK_CLASS, USE_CURRENT_JVM_PROPS)) {
            MarketoSourceOrSinkRuntime sos = (MarketoSourceOrSinkRuntime) sandboxedInstance.getInstance();
            sos.initialize(null, this);
            ValidationResult vr = sos.validateConnection(this);
            if (vr.getStatus() != ValidationResult.Result.OK) {
                return vr;
            }
            try {
                repo.storeProperties(this, this.name.getValue(), repositoryLocation, null);
                return ValidationResult.OK;
            } catch (Exception e) {
                return new ValidationResult(Result.ERROR, e.getMessage());
            }
        }
    }

    @Override
    public TMarketoConnectionProperties getConnectionProperties() {
        String refComponentId = getReferencedComponentId();
        return refComponentId != null ? getReferencedConnectionProperties() : this;
    }

    public String getReferencedComponentId() {
        return referencedComponent.componentInstanceId.getStringValue();
    }

    public TMarketoConnectionProperties getReferencedConnectionProperties() {
        TMarketoConnectionProperties refProps = referencedComponent.getReference();
        if (refProps != null) {
            return refProps;
        }
        LOG.debug("Connection has a reference to `{}` but the referenced Object is null!", getReferencedComponentId());
        return null;
    }

    public TMarketoConnectionProperties setRepositoryLocation(String repositoryLocation) {
        this.repositoryLocation = repositoryLocation;
        return this;
    }

}
