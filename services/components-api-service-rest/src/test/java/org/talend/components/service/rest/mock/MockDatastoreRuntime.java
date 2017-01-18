//==============================================================================
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
//==============================================================================

package org.talend.components.service.rest.mock;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.daikon.properties.ValidationResult;

import static java.util.Collections.singletonList;

public class MockDatastoreRuntime implements DatastoreRuntime<MockDatastoreProperties> {

    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        return singletonList(ValidationResult.OK);
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, MockDatastoreProperties properties) {
        return ValidationResult.OK;
    }
}
