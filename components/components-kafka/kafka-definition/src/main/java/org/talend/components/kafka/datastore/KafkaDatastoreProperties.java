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
package org.talend.components.kafka.datastore;

import org.talend.components.common.SslProperties;
import org.talend.components.common.SslProperties.FormType;
import org.talend.components.common.SslProperties.StoreType;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class KafkaDatastoreProperties extends PropertiesImpl implements DatastoreProperties {

    public EnumProperty<KafkaVersion> version = PropertyFactory.newEnum("version", KafkaVersion.class);

    public Property<String> brokers = PropertyFactory.newString("brokers").setRequired();

    public Property<Boolean> useSsl = PropertyFactory.newBoolean("useSsl", false);

    public EnumProperty<StoreType> trustStoreType = PropertyFactory.newEnum("trustStoreType", StoreType.class);

    public Property<String> trustStorePath = PropertyFactory.newString("trustStorePath");

    public Property<String> trustStorePassword = PropertyFactory.newString("trustStorePassword");

    public Property<Boolean> needClientAuth = PropertyFactory.newBoolean("needClientAuth", false);

    public EnumProperty<StoreType> keyStoreType = PropertyFactory.newEnum("keyStoreType", StoreType.class);

    public Property<String> keyStorePath = PropertyFactory.newString("keyStorePath");

    public Property<String> keyStorePassword = PropertyFactory.newString("keyStorePassword");

    // If verify client hostname with the hostname in the cert. for debugging, usually set false
    public Property<Boolean> verifyHost = PropertyFactory.newBoolean("verifyHost", true);

    public KafkaDatastoreProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        version.setValue(KafkaVersion.V_0_10_1_0);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(version);
        mainForm.addRow(brokers);
        mainForm.addRow(useSsl);
        mainForm.addRow(trustStoreType);
        mainForm.addRow(trustStorePath);
        mainForm.addRow(Widget.widget(trustStorePassword).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        mainForm.addRow(needClientAuth);
        mainForm.addRow(keyStoreType);
        mainForm.addRow(keyStorePath);
        mainForm.addRow(Widget.widget(keyStorePassword).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        mainForm.addRow(verifyHost);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(trustStoreType).setVisible(useSsl);
            form.getWidget(trustStorePath).setVisible(useSsl);
            form.getWidget(trustStorePassword).setVisible(useSsl);

            form.getWidget(needClientAuth).setVisible(useSsl);
            form.getWidget(verifyHost).setVisible(useSsl);

            boolean needClient = useSsl.getValue() && needClientAuth.getValue();
            form.getWidget(keyStoreType).setVisible(needClient);
            form.getWidget(keyStorePath).setVisible(needClient);
            form.getWidget(keyStorePassword).setVisible(needClient);

        }
    }

    public void afterUseSsl() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterNeedClientAuth() {
        refreshLayout(getForm(Form.MAIN));
    }
    
    public enum KafkaVersion {
        V_0_10_1_0,
        V_0_9_0_1
    }
}
