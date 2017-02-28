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

package org.talend.components.bigquery;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.exception.ComponentException;
import org.talend.components.bigquery.runtime.IBigQueryDatasetRuntime;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class BigQueryDatasetProperties extends PropertiesImpl implements DatasetProperties<BigQueryDatastoreProperties> {

    public final ReferenceProperties<BigQueryDatastoreProperties> datastoreRef = new ReferenceProperties<>("datastoreRef",
            BigQueryDatastoreDefinition.NAME);

    public Property<String> bqDataset = PropertyFactory.newString("bqDataset");

    public Property<SourceType> sourceType = PropertyFactory.newEnum("sourceType", SourceType.class);

    public Property<String> tableName = PropertyFactory.newString("tableName");

    public Property<String> query = PropertyFactory.newString("query");

    public Property<Boolean> useLegacySql = PropertyFactory.newBoolean("useLegacySql", false);

    public SchemaProperties main = new SchemaProperties("main");

    public BigQueryDatasetProperties(String name) {
        super(name);
    }

    @Override
    public BigQueryDatastoreProperties getDatastoreProperties() {
        return datastoreRef.getReference();
    }

    @Override
    public void setDatastoreProperties(BigQueryDatastoreProperties datastoreProperties) {
        datastoreRef.setReference(datastoreProperties);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        sourceType.setValue(SourceType.TABLE_NAME);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(Widget.widget(bqDataset).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        mainForm.addRow(sourceType);
        mainForm.addRow(Widget.widget(tableName).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
        mainForm.addRow(useLegacySql).addColumn(Widget.widget(query).setWidgetType(Widget.TEXT_AREA_WIDGET_TYPE));
        mainForm.addRow(main.getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        form.getWidget(tableName).setVisible(sourceType.getValue() == SourceType.TABLE_NAME);
        form.getWidget(useLegacySql).setVisible(sourceType.getValue() == SourceType.QUERY);
        form.getWidget(query).setVisible(sourceType.getValue() == SourceType.QUERY);
    }

    public ValidationResult beforeBqDataset() {
        BigQueryDatasetDefinition definition = new BigQueryDatasetDefinition();
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(this);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfo, getClass().getClassLoader())) {
            IBigQueryDatasetRuntime runtime = (IBigQueryDatasetRuntime) sandboxedInstance.getInstance();
            runtime.initialize(null, this);
            List<NamedThing> datasets = new ArrayList<>();
            for (String dataset : runtime.listDatasets()) {
                datasets.add(new SimpleNamedThing(dataset, dataset));
            }
            bqDataset.setPossibleValues(datasets);
            return ValidationResult.OK;
        } catch (Exception e) {
            return new ValidationResult(new ComponentException(e));
        }
    }

    public void afterSourceType() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void beforeTableName() {
        BigQueryDatasetDefinition definition = new BigQueryDatasetDefinition();
        RuntimeInfo runtimeInfo = definition.getRuntimeInfo(this);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfo, getClass().getClassLoader())) {
            IBigQueryDatasetRuntime runtime = (IBigQueryDatasetRuntime) sandboxedInstance.getInstance();
            runtime.initialize(null, this);
            List<NamedThing> tables = new ArrayList<>();
            for (String table : runtime.listTables()) {
                tables.add(new SimpleNamedThing(table, table));
            }
            this.tableName.setPossibleValues(tables);
        } catch (Exception e) {
            TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_EXCEPTION).setAndThrow(e.getMessage());
        }
    }

    public enum SourceType {
        TABLE_NAME,
        QUERY;
    }
}
