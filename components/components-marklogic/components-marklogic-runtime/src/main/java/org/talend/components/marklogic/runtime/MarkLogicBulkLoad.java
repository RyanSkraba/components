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

import com.marklogic.contentpump.ContentPump;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.marklogic.tmarklogicbulkload.MarkLogicBulkLoadProperties;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResultMutable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MarkLogicBulkLoad implements ComponentDriverInitialization {

    private static final I18nMessages MESSAGES = GlobalI18N.getI18nMessageProvider().getI18nMessages(MarkLogicBulkLoad.class);

    private transient static final Logger LOGGER = LoggerFactory.getLogger(MarkLogicBulkLoad.class);

    private MarkLogicBulkLoadProperties bulkLoadProperties;

    @Override
    public void runAtDriver(RuntimeContainer container) {
        String[] mlcpCommandArray = prepareMlcpCommandArray();

        LOGGER.debug(MESSAGES.getMessage("messages.debug.command", mlcpCommandArray));
        LOGGER.info(MESSAGES.getMessage("messages.info.startBulkLoad"));
        try {
            ContentPump.runCommand(mlcpCommandArray);
        } catch (IOException e) {
            LOGGER.error(MESSAGES.getMessage("messages.error.exception", e.getMessage()));
            throw new ComponentException(e);
        }
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, Properties properties) {
        ValidationResultMutable validationResult = new ValidationResultMutable();
        if (properties instanceof MarkLogicBulkLoadProperties) {
            bulkLoadProperties = (MarkLogicBulkLoadProperties) properties;

            if (isRequiredPropertiesMissed()) {
                validationResult.setStatus(ValidationResult.Result.ERROR);
                validationResult.setMessage(MESSAGES.getMessage("error.missedProperties"));
            }

        } else {
            validationResult.setStatus(ValidationResult.Result.ERROR);
            validationResult.setMessage(MESSAGES.getMessage("error.wrongProperties"));
        }

        return validationResult;
    }

    private boolean isRequiredPropertiesMissed() {
        MarkLogicConnectionProperties connection = bulkLoadProperties.connection;
        if (connection.isReferencedConnectionUsed()) {
            connection = bulkLoadProperties.connection.referencedComponent.getReference();
        }
        return connection.host.getStringValue().isEmpty()
                || connection.port.getValue() == null
                || connection.database.getStringValue().isEmpty()
                || connection.username.getStringValue().isEmpty()
                || connection.password.getStringValue().isEmpty()
                || bulkLoadProperties.loadFolder.getStringValue().isEmpty();
    }


    String[] prepareMlcpCommandArray() {
        List<String> mlcpCommand = new ArrayList<>();

        MarkLogicConnectionProperties connection = bulkLoadProperties.connection;
        boolean useExistingConnection = connection.isReferencedConnectionUsed();
        //connection properties could be also taken from referencedComponent
        String userName = useExistingConnection ?
                connection.referencedComponent.getReference().username.getStringValue() :
                connection.username.getStringValue();
        String password = useExistingConnection ?
                connection.referencedComponent.getReference().password.getStringValue() :
                connection.password.getStringValue();
        String host = useExistingConnection ?
                connection.referencedComponent.getReference().host.getStringValue() :
                connection.host.getStringValue();
        Integer port = useExistingConnection ?
                connection.referencedComponent.getReference().port.getValue() :
                connection.port.getValue();
        String database = useExistingConnection ?
                connection.referencedComponent.getReference().database.getStringValue() :
                connection.database.getStringValue();

        //need to process load folder value (it should start from '/' and has only unix file-separators)
        String loadPath = bulkLoadProperties.loadFolder.getStringValue();
        if(loadPath.contains(":")){
            loadPath = "/" + loadPath;
        }

        loadPath = (loadPath.replaceAll("\\\\","/"));

        String prefix = bulkLoadProperties.docidPrefix.getStringValue();
        if(prefix != null && (prefix.endsWith("/") || prefix.endsWith("\\"))){
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        String additionalMLCPParameters = bulkLoadProperties.mlcpParams.getStringValue();

        mlcpCommand.add("import");
        mlcpCommand.add("-host");
        mlcpCommand.add(host);

        mlcpCommand.add("-username");
        mlcpCommand.add(userName);

        mlcpCommand.add("-password");
        mlcpCommand.add(password);

        mlcpCommand.add("-port");
        mlcpCommand.add(port.toString());

        if (StringUtils.isNotEmpty(database) && !("\"\"".equals(database))) {
            mlcpCommand.add("-database");
            mlcpCommand.add(database);
        }

        mlcpCommand.add("-input_file_path");
        mlcpCommand.add(loadPath);

        if (StringUtils.isNotEmpty(prefix)) {
            mlcpCommand.add("-output_uri_replace");
            mlcpCommand.add("\"" + loadPath + ",'" + prefix + "'\"");
        }
        if (StringUtils.isNotEmpty(additionalMLCPParameters) && !(("\"\"".equals(additionalMLCPParameters)))) {
            CommandLine commandLineAdditionalParams = CommandLine.parse(additionalMLCPParameters);
            mlcpCommand.add(commandLineAdditionalParams.getExecutable());
            mlcpCommand.addAll(Arrays.asList(commandLineAdditionalParams.getArguments()));
        }

        return mlcpCommand.toArray(new String[0]);
    }
}
