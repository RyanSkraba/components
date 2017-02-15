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

package org.talend.components.elasticsearch;

import org.talend.components.common.dataset.DatasetProperties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class ElasticsearchDatasetProperties extends PropertiesImpl
        implements DatasetProperties<ElasticsearchDatastoreProperties> {

    public final ReferenceProperties<ElasticsearchDatastoreProperties> datastoreRef = new ReferenceProperties<>("datastoreRef",
            ElasticsearchDatastoreDefinition.NAME);

    public Property<String> index = PropertyFactory.newString("index").setRequired();

    public Property<String> type = PropertyFactory.newString("type").setRequired();

    // public SchemaProperties main = new SchemaProperties("main");

    public ElasticsearchDatasetProperties(String name) {
        super(name);
    }

    @Override
    public ElasticsearchDatastoreProperties getDatastoreProperties() {
        return datastoreRef.getReference();
    }

    @Override
    public void setDatastoreProperties(ElasticsearchDatastoreProperties datastoreProperties) {
        datastoreRef.setReference(datastoreProperties);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(index);
        mainForm.addRow(type);
        // mainForm.addRow(main.getForm(Form.MAIN));
    }

}
