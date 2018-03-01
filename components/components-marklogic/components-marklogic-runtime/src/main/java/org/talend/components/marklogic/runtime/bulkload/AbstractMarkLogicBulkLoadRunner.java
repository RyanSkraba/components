//==============================================================================
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
//==============================================================================

package org.talend.components.marklogic.runtime.bulkload;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.marklogic.tmarklogicbulkload.MarkLogicBulkLoadProperties;
import org.talend.components.marklogic.tmarklogicconnection.MarkLogicConnectionProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractMarkLogicBulkLoadRunner {

    private MarkLogicConnectionProperties connectionProperties;

    private MarkLogicBulkLoadProperties bulkLoadProperties;

    protected AbstractMarkLogicBulkLoadRunner(MarkLogicBulkLoadProperties properties) {
        this.bulkLoadProperties = properties;
        this.connectionProperties = properties.getConnection();
    }

    /**
     * Prepare MLCP arguments and start bulkloading
     */
    protected void performBulkLoad() {
        runBulkLoading(prepareMLCPCommand());

    }

    /**
     * Prepare arguments for MLCP program
     *
     * @return array of String arguments for MLCP
     */
    protected String[] prepareMLCPCommand() {
        List<String> mlcpCommand = new ArrayList<>();

        String database = connectionProperties.database.getStringValue();

        //need to process load folder value (it should start from '/' and has only unix file-separators)
        String loadPath = bulkLoadProperties.loadFolder.getStringValue();

        if (loadPath.contains(":")) {
            loadPath = "/" + loadPath;
        }

        loadPath = (loadPath.replaceAll("\\\\", "/"));

        String prefix = bulkLoadProperties.docidPrefix.getStringValue();
        if (prefix != null && (prefix.endsWith("/") || prefix.endsWith("\\"))) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        String additionalMLCPParameters = bulkLoadProperties.mlcpParams.getStringValue();

        mlcpCommand.add("import");
        mlcpCommand.add("-host");
        mlcpCommand.add(connectionProperties.host.getValue());

        mlcpCommand.add("-username");
        mlcpCommand.add(connectionProperties.username.getValue());

        mlcpCommand.add("-password");
        mlcpCommand.add(connectionProperties.password.getValue());

        mlcpCommand.add("-port");
        mlcpCommand.add(connectionProperties.port.getValue().toString());

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
        if (StringUtils.isNotEmpty(additionalMLCPParameters) && !("\"\"".equals(additionalMLCPParameters))) {
            CommandLine commandLineAdditionalParams = CommandLine.parse(additionalMLCPParameters);
            mlcpCommand.add(commandLineAdditionalParams.getExecutable());
            mlcpCommand.addAll(Arrays.asList(commandLineAdditionalParams.getArguments()));
        }

        return mlcpCommand.toArray(new String[0]);
    }

    /**
     * Execute MLCP program
     *
     * @param parameters concatenated String (as commandline command) or String args for main method of mlcp
     */
    protected abstract void runBulkLoading(String... parameters);
}
