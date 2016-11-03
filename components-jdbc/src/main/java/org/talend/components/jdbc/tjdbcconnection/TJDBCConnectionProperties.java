// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jdbc.tjdbcconnection;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TJDBCConnectionProperties extends ComponentPropertiesImpl implements RuntimeSettingProvider {

    public TJDBCConnectionProperties(String name) {
        super(name);
    }

    // main
    public JDBCConnectionModule connection = new JDBCConnectionModule("connection");

    public Property<Boolean> shareConnection = PropertyFactory.newBoolean("shareConnection");

    public Property<String> sharedConnectionName = PropertyFactory.newString("sharedConnectionName");

    public Property<Boolean> useDataSource = PropertyFactory.newBoolean("useDataSource").setRequired();

    public Property<String> dataSource = PropertyFactory.newProperty("dataSource").setRequired();

    // advanced
    public Property<Boolean> useAutoCommit = PropertyFactory.newBoolean("useAutoCommit");

    public Property<Boolean> autocommit = PropertyFactory.newBoolean("autocommit");

    @Override
    public void setupProperties() {
        super.setupProperties();
        useAutoCommit.setValue(true);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = CommonUtils.addForm(this, Form.MAIN);

        mainForm.addRow(connection.getForm(Form.MAIN));
        mainForm.addRow(shareConnection);
        mainForm.addRow(sharedConnectionName);
        mainForm.addRow(useDataSource);
        mainForm.addRow(dataSource);

        Form advancedForm = CommonUtils.addForm(this, Form.ADVANCED);
        advancedForm.addRow(useAutoCommit);
        advancedForm.addColumn(autocommit);
    }

    public void afterShareConnection() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterUseDataSource() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterUseAutoCommit() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (Form.ADVANCED.equals(form.getName())) {
            form.getWidget(autocommit.getName()).setHidden(!useAutoCommit.getValue());
        }

        if (Form.MAIN.equals(form.getName())) {
            if (shareConnection.getValue()) {
                form.getWidget(sharedConnectionName.getName()).setHidden(false);

                form.getWidget(useDataSource.getName()).setHidden(true);
                form.getWidget(dataSource.getName()).setHidden(true);
            } else {
                form.getWidget(sharedConnectionName.getName()).setHidden(true);

                form.getWidget(useDataSource.getName()).setHidden(false);
                form.getWidget(dataSource.getName()).setHidden(!useDataSource.getValue());
            }

            if (useDataSource.getValue()) {
                form.getWidget(dataSource.getName()).setHidden(false);

                form.getWidget(shareConnection.getName()).setHidden(true);
                form.getWidget(sharedConnectionName.getName()).setHidden(true);
            } else {
                form.getWidget(dataSource.getName()).setHidden(true);

                form.getWidget(shareConnection.getName()).setHidden(false);
                form.getWidget(sharedConnectionName.getName()).setHidden(!shareConnection.getValue());
            }
        }
    }

    @Override
    public AllSetting getRuntimeSetting() {
        AllSetting setting = new AllSetting();

        setting.setDriverPaths(this.connection.driverTable.drivers.getValue());
        setting.setDriverClass(this.connection.driverClass.getValue());
        setting.setJdbcUrl(this.connection.jdbcUrl.getValue());
        setting.setUsername(this.connection.userPassword.userId.getValue());
        setting.setPassword(this.connection.userPassword.password.getValue());

        setting.setUseAutoCommit(this.useAutoCommit.getValue());
        setting.setAutocommit(this.autocommit.getValue());

        return setting;
    }

}
