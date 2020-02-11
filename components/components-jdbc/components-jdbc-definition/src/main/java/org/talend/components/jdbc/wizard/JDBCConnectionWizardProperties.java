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

import static org.talend.daikon.properties.presentation.Widget.widget;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.JdbcRuntimeInfo;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.setting.JdbcRuntimeSourceOrSink;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 * this is the entrance properties for all the wizards(root jdbc wizard, all the right click wizards), we will store it in the
 * item file as the root properties, the schemas are the sub elements of it, we may also need to store the query as a property of
 * this
 * properties for the reading and editing query wizard though the query will not be displayed in this properties
 *
 */
public class JDBCConnectionWizardProperties extends ComponentPropertiesImpl implements RuntimeSettingProvider {

    public Property<String> name = PropertyFactory.newString("name").setRequired();

    private String repositoryLocation;

    public JDBCConnectionModule connection = new JDBCConnectionModule("connection").useInWizard();

    public Property<String> mappingFile = PropertyFactory.newProperty("mappingFile");

    public transient PresentationItem testConnection = new PresentationItem("testConnection", "Test connection");

    public JDBCConnectionWizardProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form wizardForm = CommonUtils.addForm(this, Form.MAIN);
        wizardForm.addRow(name);
        wizardForm.addRow(connection.getForm(Form.MAIN));
        wizardForm.addRow(widget(mappingFile).setWidgetType("widget.type.mappingType"));
        wizardForm.addRow(widget(testConnection).setLongRunning(true).setWidgetType(Widget.BUTTON_WIDGET_TYPE));
    }

    public ValidationResult validateTestConnection() {
        JdbcRuntimeInfo jdbcRuntimeInfo = new JdbcRuntimeInfo(this, "org.talend.components.jdbc.runtime.JDBCSourceOrSink");
        try (SandboxedInstance sandboxI = RuntimeUtil.createRuntimeClassWithCurrentJVMProperties(jdbcRuntimeInfo,
                connection.getClass().getClassLoader())) {
            JdbcRuntimeSourceOrSink sourceOrSink = (JdbcRuntimeSourceOrSink) sandboxI.getInstance();
            sourceOrSink.initialize(null, this);
            ValidationResult vr = sourceOrSink.validate(null);
            if (vr.getStatus() == ValidationResult.Result.OK) {
                vr = new ValidationResult(ValidationResult.Result.OK, "Connection successful");
                getForm(Form.MAIN).setAllowFinish(true);
            } else {
                getForm(Form.MAIN).setAllowFinish(false);
            }
            return vr;
        }
    }

    public JDBCConnectionWizardProperties setRepositoryLocation(String location) {
        repositoryLocation = location;
        return this;
    }

    public ValidationResult afterFormFinishMain(Repository<Properties> repo) throws Exception {
        JdbcRuntimeInfo jdbcRuntimeInfo = new JdbcRuntimeInfo(this, "org.talend.components.jdbc.runtime.JDBCSourceOrSink");
        try (SandboxedInstance sandboxI = RuntimeUtil.createRuntimeClassWithCurrentJVMProperties(jdbcRuntimeInfo,
                connection.getClass().getClassLoader())) {
            JdbcRuntimeSourceOrSink sourceOrSink = (JdbcRuntimeSourceOrSink) sandboxI.getInstance();
            sourceOrSink.initialize(null, this);
            ValidationResult vr = sourceOrSink.validate(null);
            if (vr.getStatus() != ValidationResult.Result.OK) {
                return vr;
            }

            repo.storeProperties(this, this.name.getValue(), repositoryLocation, null);
            // no need to store the schemas, tup will do it by the old way, so only need to store the connection properties
            return ValidationResult.OK;
        }
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

    }

    @Override
    public AllSetting getRuntimeSetting() {
        AllSetting setting = new AllSetting();

        CommonUtils.setCommonConnectionInfo(setting, connection);

        return setting;
    }

}
