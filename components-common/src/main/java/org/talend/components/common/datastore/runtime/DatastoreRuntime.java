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
package org.talend.components.common.datastore.runtime;

import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.daikon.properties.ValidationResult;

public interface DatastoreRuntime<DatastorePropT extends DatastoreProperties> extends RuntimableRuntime<DatastorePropT> {

    /**
     * perform a series of health checks like cheking the connection is possible or the status of each clusters. This
     * method will be called in the same process where the runtime will actually be executed.
     */
    Iterable<ValidationResult> doHealthChecks(RuntimeContainer container);
}
