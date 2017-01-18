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

package org.talend.components.adapter.beam.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;

public class FixedFlowSourceOrSink implements SourceOrSink {

    protected FixedFlowProperties properties;

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return null;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        return null;
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (FixedFlowProperties) properties;
        return ValidationResult.OK;
    }

    //TODO no need after AnyProperty support serializable
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(properties.toSerialized());
    }

    private void readObject(ObjectInputStream in) throws IOException {
        properties = Properties.Helper.fromSerializedPersistent(in.readUTF(), FixedFlowProperties.class).object;
    }
}
