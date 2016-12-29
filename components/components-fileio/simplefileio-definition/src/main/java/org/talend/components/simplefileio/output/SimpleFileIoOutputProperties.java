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

package org.talend.components.simplefileio.output;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.io.IOProperties;
import org.talend.components.simplefileio.SimpleFileIoDatasetDefinition;
import org.talend.components.simplefileio.SimpleFileIoDatasetProperties;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;

public class SimpleFileIoOutputProperties extends ComponentPropertiesImpl implements IOProperties {

    public SimpleFileIoOutputProperties(String name) {
        super(name);
    }

    transient public ReferenceProperties<SimpleFileIoDatasetProperties> datasetRef = new ReferenceProperties<>("datasetRef",
            SimpleFileIoDatasetDefinition.NAME);

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
    }

    @Override
    public SimpleFileIoDatasetProperties getDatasetProperties() {
        return datasetRef.getReference();
    }

    @Override
    public void setDatasetProperties(DatasetProperties datasetProperties) {
        datasetRef.setReference(datasetProperties);
    }
}
