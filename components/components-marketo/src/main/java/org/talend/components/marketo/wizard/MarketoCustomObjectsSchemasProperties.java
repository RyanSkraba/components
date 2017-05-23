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

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.marketo.MarketoComponentProperties.APIMode;
import org.talend.components.marketo.MarketoProvideConnectionProperties;
import org.talend.components.marketo.runtime.MarketoSourceOrSink;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.CustomObjectAction;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;

public class MarketoCustomObjectsSchemasProperties extends ComponentPropertiesImpl implements MarketoProvideConnectionProperties {

    public static final String FORM_CUSTOMOBJECTS = "CustomObjects";

    public TMarketoConnectionProperties connection = new TMarketoConnectionProperties("connection");

    private String repositoryLocation;

    private List<NamedThing> customObjectsNames;

    public Property<List<NamedThing>> selectedCustomObjectsNames = newProperty(new TypeLiteral<List<NamedThing>>() {
    }, "selectedCustomObjectsNames"); //$NON-NLS-1$

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoCustomObjectsSchemasProperties.class);

    public MarketoCustomObjectsSchemasProperties(String name) {
        super(name);
    }

    @Override
    public TMarketoConnectionProperties getConnectionProperties() {
        return connection;
    }

    @Override
    public APIMode getApiMode() {
        // REST only
        return APIMode.REST;
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
    public void setupProperties() {
        super.setupProperties();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form containerForm = Form.create(this, FORM_CUSTOMOBJECTS);
        containerForm.addRow(widget(selectedCustomObjectsNames).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        refreshLayout(containerForm);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
    }

    public void beforeFormPresentCustomObjects() throws IOException {
        try {
            customObjectsNames = MarketoSourceOrSink.getSchemaNames(null, connection);
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
            String repoLoc = repo.storeProperties(connection, connection.name.getValue(), repositoryLocation, null);
            String storeId;

            for (NamedThing nl : selectedCustomObjectsNames.getValue()) {
                String customObjectId = nl.getName();
                storeId = nl.getName().replaceAll("-", "_").replaceAll(" ", "_");
                TMarketoInputProperties customObjectProps = new TMarketoInputProperties(customObjectId);
                customObjectProps.init();
                customObjectProps.connection = connection;
                customObjectProps.inputOperation.setValue(InputOperation.CustomObject);
                customObjectProps.customObjectAction.setValue(CustomObjectAction.get);
                customObjectProps.schemaInput.schema
                        .setValue(MarketoSourceOrSink.getEndpointSchema(null, customObjectId, connection));
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

}
