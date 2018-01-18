// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marklogic.runtime;

import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.connection.MarkLogicConnection;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.properties.ValidationResult;

/**
 * Implementation of runtime part for tMarkLogicConnection component.
 *
 */
public class TMarkLogicConnectionStandalone extends MarkLogicConnection
        implements ComponentDriverInitialization<MarkLogicConnectionProperties> {

    private static final long serialVersionUID = -40535886003777462L;

    private MarkLogicConnectionProperties properties;

    @Override
    public void runAtDriver(RuntimeContainer container) {
        connect(container);
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, MarkLogicConnectionProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    protected MarkLogicConnectionProperties getMarkLogicConnectionProperties() {
        return properties;
    }
}
