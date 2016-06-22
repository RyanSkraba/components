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
package org.talend.components.dataprep.runtime;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dataprep.tdatasetoutput.TDataSetOutputProperties;
import org.talend.daikon.NamedThing;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DataSetSink extends DataSetSourceOrSink implements Sink {

    private static final long serialVersionUID = 3228265006313531905L;

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new DataSetWriteOperation(this);
    }

    @Override
    public void initialize(RuntimeContainer runtimeContainer, ComponentProperties componentProperties) {
        runtimeProperties = ((TDataSetOutputProperties) componentProperties).getRuntimeProperties();
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer runtimeContainer) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer runtimeContainer, String s) throws IOException {
        return null;
    }
}