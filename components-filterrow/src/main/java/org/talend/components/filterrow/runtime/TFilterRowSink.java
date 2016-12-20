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
package org.talend.components.filterrow.runtime;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

/**
 * created by dmytro.chmyga on Dec 19, 2016
 */
public class TFilterRowSink implements Sink {

    private static final long serialVersionUID = 2558711916032472975L;

    @Override
    public Schema getEndpointSchema(RuntimeContainer arg0, String arg1) throws IOException {
        return null;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer arg0) throws IOException {
        return null;
    }

    @Override
    public ValidationResult validate(RuntimeContainer arg0) {
        return ValidationResult.OK;
    }

    @Override
    public ValidationResult initialize(RuntimeContainer arg0, ComponentProperties arg1) {
        return null;
    }

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new TFilterRowWriteOperation(this);
    }

}
