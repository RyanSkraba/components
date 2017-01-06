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
package org.talend.components.simplefileio.runtime;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.simplefileio.SimpleFileIoDatastoreProperties;
import org.talend.daikon.properties.ValidationResult;

import static java.util.Collections.emptyList;

public class SimpleFileIoDatastoreRuntime implements DatastoreRuntime<SimpleFileIoDatastoreProperties> {

    /**
     * The datastore instance that this runtime is configured for.
     */
    private SimpleFileIoDatastoreProperties properties = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, SimpleFileIoDatastoreProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        return emptyList();
    }
}
