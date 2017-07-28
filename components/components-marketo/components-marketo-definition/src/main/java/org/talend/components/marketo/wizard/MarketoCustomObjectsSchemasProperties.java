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
package org.talend.components.marketo.wizard;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.components.marketo.MarketoComponentDefinition.RUNTIME_SOURCEORSINK_CLASS;
import static org.talend.components.marketo.MarketoComponentDefinition.getSandboxedInstance;
import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.slf4j.Logger;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.marketo.MarketoProvideConnectionProperties;
import org.talend.components.marketo.runtime.MarketoSourceOrSinkRuntime;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectSyncAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.OutputOperation;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.sandbox.SandboxedInstance;

public class MarketoCustomObjectsSchemasProperties extends ComponentPropertiesImpl implements MarketoProvideConnectionProperties {

    public static final String FORM_CUSTOMOBJECTS = "CustomObjects";

    public TMarketoConnectionProperties connection = new TMarketoConnectionProperties("connection");

    private String repositoryLocation;

    public Property<List<NamedThing>> selectedCustomObjectsNames = newProperty(new TypeLiteral<List<NamedThing>>() {
    }, "selectedCustomObjectsNames"); //$NON-NLS-1$

    private static final Logger LOG = getLogger(MarketoCustomObjectsSchemasProperties.class);

    public MarketoCustomObjectsSchemasProperties(String name) {
        super(name);
    }

    @Override
    public TMarketoConnectionProperties getConnectionProperties() {
        return connection;
    }

    public MarketoCustomObjectsSchemasProperties setConnection(TMarketoConnectionProperties connection) {
        this.connection = connection;
        return this;
    }

    public MarketoCustomObjectsSchemasProperties setRepositoryLocation(String repositoryLocation) {
        this.repositoryLocation = repositoryLocation;
        return this;
    }

    public String getRepositoryLocation() {
        return repositoryLocation;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form containerForm = Form.create(this, FORM_CUSTOMOBJECTS);
        containerForm.addRow(widget(selectedCustomObjectsNames).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        refreshLayout(containerForm);
    }

    public void beforeFormPresentCustomObjects() throws IOException {
        List<NamedThing> customObjectsNames;
        try {
            SandboxedInstance sandboxedInstance = getRuntimeSandboxedInstance();
            MarketoSourceOrSinkRuntime sos = (MarketoSourceOrSinkRuntime) sandboxedInstance.getInstance();
            sos.initialize(null, this);
            customObjectsNames = sos.getSchemaNames(null);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw e;
        }
        selectedCustomObjectsNames.setPossibleValues(customObjectsNames);
        getForm(FORM_CUSTOMOBJECTS).setAllowBack(true);
        getForm(FORM_CUSTOMOBJECTS).setAllowFinish(true);
    }

    public ValidationResult afterFormFinishCustomObjects(Repository<Properties> repo) throws Exception {
        try {
            SandboxedInstance sandboxedInstance = getRuntimeSandboxedInstance();
            MarketoSourceOrSinkRuntime sos = (MarketoSourceOrSinkRuntime) sandboxedInstance.getInstance();
            sos.initialize(null, this);
            String repoLoc = repo.storeProperties(connection, connection.name.getValue(), repositoryLocation, null);
            String storeId;
            for (NamedThing nl : selectedCustomObjectsNames.getValue()) {
                String customObjectId = nl.getName();
                storeId = nl.getName().replaceAll("-", "_").replaceAll(" ", "_");
                MarketoComponentWizardBaseProperties customObjectProps = new MarketoComponentWizardBaseProperties(customObjectId);
                customObjectProps.init();
                customObjectProps.connection = connection;
                customObjectProps.inputOperation.setValue(InputOperation.CustomObject);
                customObjectProps.outputOperation.setValue(OutputOperation.syncCustomObjects);
                customObjectProps.customObjectAction.setValue(CustomObjectAction.get);
                customObjectProps.customObjectSyncAction.setValue(CustomObjectSyncAction.createOrUpdate);
                customObjectProps.schemaInput.schema.setValue(sos.getEndpointSchema(null, customObjectId));
                customObjectProps.customObjectName.setValue(nl.getName());
                repo.storeProperties(customObjectProps, storeId, repoLoc, "schemaInput.schema");
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
            ValidationResultMutable vr = new ValidationResultMutable();
            vr.setStatus(Result.ERROR);
            vr.setMessage(e.getMessage());
            return vr;
        }
        return ValidationResult.OK;
    }

    protected SandboxedInstance getRuntimeSandboxedInstance() {
        return getSandboxedInstance(RUNTIME_SOURCEORSINK_CLASS);
    }

}
