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
package org.talend.components.jdbc.wizard;

import static org.talend.daikon.properties.presentation.Widget.widget;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.runtime.JDBCSourceOrSink;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class JDBCConnectionWizardProperties extends ComponentPropertiesImpl implements RuntimeSettingProvider {

    public Property<String> name = PropertyFactory.newString("name").setRequired();

    public JDBCConnectionModule connection = new JDBCConnectionModule("connection");

    public PresentationItem testConnection = new PresentationItem("testConnection", "Test connection");

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
        wizardForm.addRow(widget(testConnection).setLongRunning(true).setWidgetType(Widget.BUTTON_WIDGET_TYPE));
    }

    public ValidationResult validateTestConnection() throws Exception {
        JDBCSourceOrSink sourceOrSink = new JDBCSourceOrSink();
        sourceOrSink.initialize(null, this);
        ValidationResult vr = sourceOrSink.validate(null);
        if (vr.getStatus() == ValidationResult.Result.OK) {
            vr.setMessage("Connection successful");
            getForm(Form.MAIN).setAllowForward(true);
        } else {
            getForm(Form.MAIN).setAllowForward(false);
        }
        return vr;
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

    }

    @Override
    public AllSetting getRuntimeSetting() {
        // TODO Auto-generated method stub
        return null;
    }

}
