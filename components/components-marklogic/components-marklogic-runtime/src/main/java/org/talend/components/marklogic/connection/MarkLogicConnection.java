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
package org.talend.components.marklogic.connection;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.exceptions.MarkLogicErrorCode;
import org.talend.components.marklogic.exceptions.MarkLogicException;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.SecurityContext;
import com.marklogic.client.FailedRequestException;

/**
 * Common implementation for creating or getting connection to MarkLogic database.
 *
 */
public abstract class MarkLogicConnection {

    private transient static final Logger LOGGER = LoggerFactory.getLogger(MarkLogicConnection.class);

    private static final I18nMessages MESSAGES = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(MarkLogicConnection.class);

    public static final String CONNECTION = "connection";

    private static final String ERROR_KEY = "message";

    /**
     * Creates new {@link DatabaseClient} connection or gets referenced one from container.
     *
     * @param container
     * @return connection to MarkLogic database.
     * @throws IOException thrown if referenced connection is not connected.
     */
    public DatabaseClient connect(RuntimeContainer container) {
        MarkLogicConnectionProperties properties = getMarkLogicConnectionProperties();
        if (properties.getReferencedComponentId() != null && container != null) {
            DatabaseClient client = (DatabaseClient) container.getComponentData(properties.getReferencedComponentId(), CONNECTION);
            if (client != null) {
                return client;
            }

            throw new MarkLogicException(
                    new MarkLogicErrorCode(
                            MESSAGES.getMessage("error.invalid.referenceConnection", properties.getReferencedComponentId())
                    )
            );
        }
        SecurityContext context = "BASIC".equals(properties.authentication.getValue())
                ? new DatabaseClientFactory.BasicAuthContext(properties.username.getValue(), properties.password.getValue())
                : new DatabaseClientFactory.DigestAuthContext(properties.username.getValue(), properties.password.getValue());
        DatabaseClient client = DatabaseClientFactory.newClient(properties.host.getValue(), properties.port.getValue(),
                properties.database.getValue(), context);

        testConnection(client);

        LOGGER.info("Connected to MarkLogic server");
        if (container != null) {
            container.setComponentData(container.getCurrentComponentId(), CONNECTION, client);
            LOGGER.info("Connection stored in container");
        }
        return client;
    }

    private void testConnection(DatabaseClient client) {
        try {
            // Since creating client is not enough for verifying established connection, need to make fake call.
            client.openTransaction().commit();
        } catch (Exception e) {
            MarkLogicErrorCode errorCode = new MarkLogicErrorCode(
                    e instanceof FailedRequestException ? MESSAGES.getMessage("error.invalid.credentials")
                            : MESSAGES.getMessage("error.server.notReachable"));
            throw new MarkLogicException(errorCode, e);
        }
    }

    protected abstract MarkLogicConnectionProperties getMarkLogicConnectionProperties();

}
