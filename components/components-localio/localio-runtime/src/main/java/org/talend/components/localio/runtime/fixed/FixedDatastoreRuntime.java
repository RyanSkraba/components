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
package org.talend.components.localio.runtime.fixed;

import static java.util.Collections.emptyList;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.localio.fixed.FixedDatastoreProperties;
import org.talend.daikon.properties.ValidationResult;

public class FixedDatastoreRuntime implements DatastoreRuntime<FixedDatastoreProperties> {

    /**
     * The datastore instance that this runtime is configured for.
     */
    private FixedDatastoreProperties properties = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, FixedDatastoreProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        return emptyList();
    }
}
