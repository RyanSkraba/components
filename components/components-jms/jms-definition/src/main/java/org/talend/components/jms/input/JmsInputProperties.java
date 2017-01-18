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

package org.talend.components.jms.input;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.components.jms.JmsDatasetDefinition;
import org.talend.components.jms.JmsDatasetProperties;

import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class JmsInputProperties extends ComponentPropertiesImpl implements IOProperties {

    public JmsInputProperties(String name) {
        super(name);
    }

    public Property<Integer> timeout = PropertyFactory.newInteger("timeout", -1);

    public Property<Integer> max_msg = PropertyFactory.newInteger("max_msg", -1);

    public Property<String> msg_selector = PropertyFactory.newString("msg_selector", "");

    transient public ReferenceProperties<JmsDatasetProperties> datasetRef = new ReferenceProperties<>("datasetRef",
            JmsDatasetDefinition.NAME);

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(timeout);
        mainForm.addRow(max_msg);
        mainForm.addRow(msg_selector);
    }

    @Override
    public DatasetProperties getDatasetProperties() {
        return datasetRef.getReference();
    }

    @Override
    public void setDatasetProperties(DatasetProperties datasetProperties) {
        datasetRef.setReference(datasetProperties);
    }
}
