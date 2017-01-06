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

import static org.talend.daikon.properties.property.PropertyFactory.*;

import org.talend.components.common.dataset.DatasetProperties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class JmsDatasetProperties extends PropertiesImpl implements DatasetProperties<JmsDatastoreProperties> {

    public final Property<JmsMessageType> msgType = newEnum("msgType", JmsMessageType.class).setRequired();

    public final Property<JmsProcessingMode> processingMode = newEnum("processingMode", JmsProcessingMode.class);

    public final Property<String> queueTopicName = PropertyFactory.newString("queueTopicName", "");

    public final transient ReferenceProperties<JmsDatastoreProperties> datastoreRef = new ReferenceProperties<>("datastoreRef",
            JmsDatastoreDefinition.NAME);

    public JmsDatasetProperties(String name) {
        super(name);
    }

    public enum AdvancedPropertiesArrayType {
        ROW,
        CONTENT
    }

    @Override
    public JmsDatastoreProperties getDatastoreProperties() {
        return datastoreRef.getReference();
    }

    @Override
    public void setDatastoreProperties(JmsDatastoreProperties datastoreProperties) {
        datastoreRef.setReference(datastoreProperties);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(msgType);
        mainForm.addRow(queueTopicName);
        mainForm.addRow(processingMode);
    }
}
