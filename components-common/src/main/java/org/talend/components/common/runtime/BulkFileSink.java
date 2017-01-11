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
package org.talend.components.common.runtime;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.BulkFileProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

public class BulkFileSink implements Sink {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(BulkFileSink.class);

    protected BulkFileProperties properties;

    public BulkFileSink() {
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (BulkFileProperties) properties;
        return ValidationResult.OK;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResult validate = new ValidationResult();
        if (!(properties instanceof BulkFileProperties)) {
            validate = new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                    .setMessage("properties should be of type :" + BulkFileProperties.class.getCanonicalName());
        }
        return validate;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer adaptor) throws IOException {
        return null;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new BulkFileWriteOperation(this);
    }

    public BulkFileProperties getBulkFileProperties() {
        return properties;
    }

}
