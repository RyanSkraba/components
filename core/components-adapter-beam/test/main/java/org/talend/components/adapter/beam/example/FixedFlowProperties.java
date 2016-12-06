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

package org.talend.components.adapter.beam.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class FixedFlowProperties extends ComponentPropertiesImpl {

    public Property<String> data = PropertyFactory.newString("data");

    public Property<String> rowDelimited = PropertyFactory.newString("rowDelimited");

    public Property<Schema> schema = PropertyFactory.newSchema("schema");

    public FixedFlowProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        Schema schemaValue = SchemaBuilder.record("row").namespace("fixedFlow").fields() //
                .name("col1").type(Schema.create(Schema.Type.STRING)).noDefault() //
                .endRecord();
        schema.setValue(schemaValue);
    }

}
