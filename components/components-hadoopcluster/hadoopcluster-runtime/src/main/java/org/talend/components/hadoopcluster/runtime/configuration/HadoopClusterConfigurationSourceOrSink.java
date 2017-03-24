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

package org.talend.components.hadoopcluster.runtime.configuration;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.hadoopcluster.configuration.input
        .HadoopClusterConfigurationInputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

public class HadoopClusterConfigurationSourceOrSink implements SourceOrSink{

    protected HadoopClusterConfigurationInputProperties properties;

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        //TODO there is no mean for this method in this component
        return null;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws
            IOException {
        //TODO fixed schema, do not need to get on runtime
        return null;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        //TODO no need to implement it for input component only, consider later
        return ValidationResult.OK;
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (HadoopClusterConfigurationInputProperties) properties;
        return ValidationResult.OK;
    }
}
