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

import org.talend.components.common.SslProperties;
import org.talend.components.common.UserPasswordProperties;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class ElasticsearchDatastoreProperties extends PropertiesImpl implements DatastoreProperties {

    public EnumProperty<ElasticsearchVersion> version = PropertyFactory.newEnum("version", ElasticsearchVersion.class);

    public Property<String> nodes = PropertyFactory.newString("nodes").setRequired();

    public UserPasswordProperties auth = new UserPasswordProperties("auth", true);

    // Do not supported by ElasticsearchIO yet
    public SslProperties ssl = new SslProperties("ssl");

    public ElasticsearchDatastoreProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        version.setValue(ElasticsearchVersion.V_2_4);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(version);
        mainForm.addRow(nodes);
        mainForm.addRow(auth);
        // mainForm.addRow(ssl);
    }

    public enum ElasticsearchVersion {
        V_2_4
    }
}
