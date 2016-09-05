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
package org.talend.components.datastewardship.runtime;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.datastewardship.TdsProperties;
import org.talend.components.datastewardship.connection.TdsConnection;
import org.talend.daikon.NamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

/**
 * TDS {@link SourceOrSink}
 */
public class TdsSourceOrSink implements SourceOrSink {

    private static final long serialVersionUID = -1780693801850579840L;

    private static final Logger LOG = LoggerFactory.getLogger(TdsSourceOrSink.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(TdsSourceOrSink.class);

    /**
     * Data schema
     */
    private Schema schema;

    /**
     * Host and port of TDS server. E.g. "http://localhost:8080"
     */
    private String url;

    /**
     * TDS user name, which is used in requests
     */
    private String username;

    /**
     * TDS user password, which is used in requests
     */
    private String password;

    @Override
    public Schema getEndpointSchema(RuntimeContainer arg0, String arg1) throws IOException {
        return null;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer arg0) throws IOException {
        return null;
    }

    /**
     * Initializes this {@link SourceOrSink} with user specified properties
     * 
     * @param container {@link RuntimeContainer} instance
     * @param properties user specified properties
     */
    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        TdsProperties tdsProperties = (TdsProperties) properties;
        url = tdsProperties.connection.url.getValue();
        username = tdsProperties.connection.username.getValue();
        password = tdsProperties.connection.password.getValue();
        schema = tdsProperties.schema.schema.getValue();
        return ValidationResult.OK;
    }

    /**
     * Validates connection to the Host
     * 
     * @param container Runtime container
     * @return {@link Result#OK} if connection was established and {@link Result#ERROR} otherwise
     */
    @Override
    public ValidationResult validate(RuntimeContainer container) {
        TdsConnection tdsConn = new TdsConnection(url, username, password);
        String errorMessage;
        try {
            int statusCode = tdsConn.checkConnection();
            if (statusCode == SC_OK) {
                return ValidationResult.OK;
            } else {
                errorMessage = messages.getMessage("error.wrongStatusCode", statusCode); //$NON-NLS-1$
                LOG.debug(errorMessage);
            }
        } catch (IOException e) {
            errorMessage = messages.getMessage("error.connectionException", e); //$NON-NLS-1$
            LOG.debug(errorMessage);
        }
        String validationFailed = messages.getMessage("error.hostNotValidated", url); //$NON-NLS-1$
        StringBuilder sb = new StringBuilder(validationFailed);
        sb.append(System.lineSeparator());
        sb.append(errorMessage);
        ValidationResult validationResult = new ValidationResult();
        validationResult.setStatus(Result.ERROR);
        validationResult.setMessage(sb.toString());
        return validationResult;
    }

    /**
     * Returns TDS server host and port value resource
     * 
     * @return host and port value
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns TDS server user ID
     * 
     * @return TDS server user ID
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns TDS server user password
     * 
     * @return TDS server user password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Getter for schema.
     * 
     * @return the schema
     */
    public Schema getSchema() {
        return schema;
    }

}
