// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.dataprep.tdatasetoutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.dataprep.DataPrepProperties;
import org.talend.components.dataprep.runtime.DataPrepOutputModes;
import org.talend.components.dataprep.runtime.RuntimeProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

import java.util.Collections;
import java.util.Set;

/**
 * The ComponentProperties subclass provided by a component stores the configuration of a component and is used for:
 * 
 * <ol>
 * <li>Specifying the format and type of information (properties) that is provided at design-time to configure a
 * component for run-time,</li>
 * <li>Validating the properties of the component at design-time,</li>
 * <li>Containing the untyped values of the properties, and</li>
 * <li>All of the UI information for laying out and presenting the properties to the user.</li>
 * </ol>
 * 
 * The TDataSetOutputProperties has two properties:
 * <ol>
 * <li>{code dataSetName}, a simple property which is a String containing the file path that this component will read.
 * </li>
 * <li>{code schema}, an embedded property referring to a Schema.</li>
 * </ol>
 */
public class TDataSetOutputProperties extends DataPrepProperties {

    private static final Logger LOG = LoggerFactory.getLogger(TDataSetOutputProperties.class);

    public final Property<DataPrepOutputModes> mode = PropertyFactory.newEnum("mode", DataPrepOutputModes.class);

    public final Property<String> dataSetName = PropertyFactory.newString("dataSetName").setRequired();

    public final Property<String> dataSetId = PropertyFactory.newString("dataSetId").setRequired();

    public final Property<Integer> limit = PropertyFactory.newInteger("limit", 100);

    public TDataSetOutputProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.emptySet();
        } else {
            return Collections.singleton(mainConnector);
        }
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = getForm(Form.MAIN);
        form.addRow(mode);
        form.addRow(dataSetName);
        form.addRow(dataSetId);
        form.addRow(limit);
    }

    public void afterMode() {
        refreshLayout(getForm(Form.MAIN));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupProperties() {
        super.setupProperties();
        mode.setValue(DataPrepOutputModes.Create);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (form.getName().equals(Form.MAIN)) {
            DataPrepOutputModes localMode = mode.getValue();
            switch (localMode) {
                case Create:
                    form.getWidget(login.getName()).setHidden(false);
                    form.getWidget(pass.getName()).setHidden(false);
                    form.getWidget(dataSetName.getName()).setHidden(false);
                    form.getWidget(dataSetId.getName()).setHidden(true);
                    break;
                case Update:
                    form.getWidget(login.getName()).setHidden(false);
                    form.getWidget(pass.getName()).setHidden(false);
                    form.getWidget(dataSetId.getName()).setHidden(false);
                    form.getWidget(dataSetName.getName()).setHidden(true);
                    break;
                case LiveDataset:
                    form.getWidget(login.getName()).setHidden(true);
                    form.getWidget(pass.getName()).setHidden(true);
                    form.getWidget(dataSetName.getName()).setHidden(true);
                    form.getWidget(dataSetId.getName()).setHidden(true);
                    break;
                default:
                    break;
            }
        }
    }

    public RuntimeProperties getRuntimeProperties() {
        RuntimeProperties runtimeProperties = new RuntimeProperties();
        runtimeProperties.setUrl(url.getStringValue());
        runtimeProperties.setLogin(login.getStringValue());
        runtimeProperties.setPass(pass.getStringValue());
        runtimeProperties.setMode(mode.getValue());
        if (mode.getValue().equals(DataPrepOutputModes.Create)) {
            runtimeProperties.setDataSetName(dataSetName.getStringValue());
        } else {
            runtimeProperties.setDataSetName(dataSetId.getStringValue());
        }
        runtimeProperties.setLimit(limit.getStringValue());
        return runtimeProperties;
    }
}
