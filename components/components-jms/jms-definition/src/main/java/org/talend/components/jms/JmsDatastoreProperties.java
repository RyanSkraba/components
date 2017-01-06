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

package org.talend.components.jms;

import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;

import org.talend.components.common.UserPasswordProperties;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class JmsDatastoreProperties extends PropertiesImpl implements DatastoreProperties {

    public enum JmsVersion {
        V_1_1
    }

    public JmsDatastoreProperties(String name) {
        super(name);
    }

    public Property<JmsVersion> version = newEnum("version", JmsVersion.class).setRequired();

    public Property<String> contextProvider = PropertyFactory.newString("contextProvider").setRequired();

    public Property<String> serverUrl = PropertyFactory.newString("serverUrl");

    public Property<String> connectionFactoryName = PropertyFactory.newString("connectionFactoryName");

    public Property<Boolean> needUserIdentity = newBoolean("needUserIdentity");

    // TODO check if it is not better to do "UserPasswordProperties" class like for cassandra
    public UserPasswordProperties userPassword = new UserPasswordProperties("userPassword");

    // Those advanced settings could be either in the datastore or in the dataset
    public Property<Boolean> useHttps = PropertyFactory.newBoolean("useHttps");

    public Property<String> httpsSettings = PropertyFactory.newString("httpsSettings");

    @Override
    public void setupProperties() {
        super.setupProperties();
        version.setValue(JmsVersion.V_1_1);
        contextProvider.setValue("com.tibco.tibjms.naming.TibjmsInitialContextFactory");
        serverUrl.setValue("tibjmsnaming://localhost:7222");
        connectionFactoryName.setValue("GenericConnectionFactory");
        needUserIdentity.setValue(false);
        userPassword.userId.setValue("");
        userPassword.password.setValue("");
        useHttps.setValue(false);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(version);
        mainForm.addRow(contextProvider);
        mainForm.addRow(serverUrl);
        mainForm.addRow(connectionFactoryName);
        mainForm.addRow(userPassword);

        mainForm.addRow(useHttps);
        mainForm.addRow(httpsSettings);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        // Main properties
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(userPassword.getName()).setVisible(needUserIdentity);
            form.getWidget(httpsSettings.getName()).setVisible(useHttps);
        }
    }

    public void afterNeedUserIdentity() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterUseHttps() {
        refreshLayout(getForm(Form.MAIN));
    }
}
