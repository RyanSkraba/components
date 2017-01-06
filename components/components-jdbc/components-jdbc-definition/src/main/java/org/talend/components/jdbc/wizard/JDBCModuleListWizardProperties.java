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
package org.talend.components.jdbc.wizard;

import static org.talend.daikon.properties.presentation.Widget.*;
import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.jdbc.JdbcRuntimeInfo;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.setting.JdbcSourceOrSinkWithQuery;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

public class JDBCModuleListWizardProperties extends ComponentPropertiesImpl implements RuntimeSettingProvider {

    private String name;

    private JDBCConnectionModule connection;

    private String repositoryLocation;

    private List<NamedThing> moduleNames;

    public Property<List<NamedThing>> selectedModuleNames = newProperty(new TypeLiteral<List<NamedThing>>() {
    }, "selectedModuleNames");

    public JDBCModuleListWizardProperties(String name) {
        super(name);
    }

    public JDBCModuleListWizardProperties setConnection(JDBCConnectionModule connection) {
        this.connection = connection;
        return this;
    }

    @Override
    public JDBCModuleListWizardProperties setName(String name) {
        this.name = name;
        return this;
    }

    public JDBCModuleListWizardProperties setRepositoryLocation(String location) {
        repositoryLocation = location;
        return this;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form moduleForm = Form.create(this, Form.MAIN);
        moduleForm.addRow(widget(selectedModuleNames).setWidgetType(Widget.NAME_SELECTION_AREA_WIDGET_TYPE));
    }

    public void beforeFormPresentMain() throws Exception {
        JdbcRuntimeInfo jdbcRuntimeInfo = new JdbcRuntimeInfo(this, "org.talend.components.jdbc.runtime.JDBCSourceOrSink");
        try (SandboxedInstance sandboxI = RuntimeUtil.createRuntimeClass(jdbcRuntimeInfo,
                connection.getClass().getClassLoader())) {
            JdbcSourceOrSinkWithQuery sourceOrSink = (JdbcSourceOrSinkWithQuery) sandboxI.getInstance();
            sourceOrSink.initialize(null, this);
            moduleNames = sourceOrSink.getSchemaNames(null);
            selectedModuleNames.setPossibleValues(moduleNames);
            getForm(Form.MAIN).setAllowBack(true);
            getForm(Form.MAIN).setAllowFinish(true);
        }
    }

    public ValidationResult afterFormFinishMain(Repository<Properties> repo) throws Exception {
        JdbcRuntimeInfo jdbcRuntimeInfo = new JdbcRuntimeInfo(this, "org.talend.components.jdbc.runtime.JDBCSourceOrSink");
        try (SandboxedInstance sandboxI = RuntimeUtil.createRuntimeClass(jdbcRuntimeInfo,
                connection.getClass().getClassLoader())) {
            JdbcSourceOrSinkWithQuery sourceOrSink = (JdbcSourceOrSinkWithQuery) sandboxI.getInstance();
            sourceOrSink.initialize(null, this);
            ValidationResult vr = sourceOrSink.validate(null);
            if (vr.getStatus() != ValidationResult.Result.OK) {
                return vr;
            }

            String connRepLocation = repo.storeProperties(connection, name, repositoryLocation, null);

            for (NamedThing nl : selectedModuleNames.getValue()) {
                String tablename = nl.getName();
                Schema schema = sourceOrSink.getEndpointSchema(null, tablename);

                // only use one properties contains all information(connection info, table name, schema), not have to be
                // TJDBCInputProperties
                // it seems that we map the wizard meta data to component properties by the field path
                // TODO make sure it works
                TJDBCInputProperties properties = new TJDBCInputProperties(tablename);
                properties.connection = connection;
                properties.init();

                properties.tableSelection.tablename.setValue(tablename);
                properties.main.schema.setValue(schema);
                repo.storeProperties(properties, tablename, connRepLocation, "schema.schema");
            }
            return ValidationResult.OK;
        }
    }

    @Override
    public AllSetting getRuntimeSetting() {
        // TODO Auto-generated method stub
        return null;
    }
}
