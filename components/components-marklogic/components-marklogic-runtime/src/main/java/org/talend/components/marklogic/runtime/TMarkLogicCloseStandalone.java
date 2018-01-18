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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.connection.MarkLogicConnection;
import org.talend.components.marklogic.tmarklogicclose.MarkLogicCloseProperties;
import org.talend.daikon.properties.ValidationResult;

import com.marklogic.client.DatabaseClient;

/**
 * Implementation of runtime part for tMarkLogicClose component.
 *
 */
public class TMarkLogicCloseStandalone implements ComponentDriverInitialization<MarkLogicCloseProperties> {

    private static final long serialVersionUID = -7276483774240763425L;

    private transient static final Logger LOGGER = LoggerFactory.getLogger(TMarkLogicCloseStandalone.class);

    private MarkLogicCloseProperties properties;

    @Override
    public void runAtDriver(RuntimeContainer container) {
        if (container != null) {
            DatabaseClient containerClient = (DatabaseClient) container.getComponentData(properties.getReferencedComponentId(),
                    MarkLogicConnection.CONNECTION);
            if (containerClient != null) {
                containerClient.release();
                LOGGER.info("Connection released");
            }
        }
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, MarkLogicCloseProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }
}
