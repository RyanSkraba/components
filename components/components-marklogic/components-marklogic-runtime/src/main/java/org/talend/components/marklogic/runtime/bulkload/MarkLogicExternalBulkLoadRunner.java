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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marklogic.exceptions.MarkLogicErrorCode;
import org.talend.components.marklogic.exceptions.MarkLogicException;
import org.talend.components.marklogic.tmarklogicbulkload.MarkLogicBulkLoadProperties;
import org.talend.components.marklogic.util.CommandExecutor;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class MarkLogicExternalBulkLoadRunner extends AbstractMarkLogicBulkLoadRunner {

    private static final I18nMessages MESSAGES = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(MarkLogicExternalBulkLoadRunner.class);

    private transient static final Logger LOGGER = LoggerFactory.getLogger(MarkLogicBulkLoad.class);

    private static final String CMD_CALL = "cmd /c mlcp.bat ";

    private static final String SH_CALL = "mlcp.sh ";

    protected MarkLogicExternalBulkLoadRunner(MarkLogicBulkLoadProperties bulkLoadProperties) {
        super(bulkLoadProperties);
    }

    @Override
    public void performBulkLoad() {
        String[] mlcpCommandArray = prepareMLCPCommand();
        String mlcpCMDCommand = prepareMLCPCommandCMD(mlcpCommandArray);
        runBulkLoading(mlcpCMDCommand);
    }

    String prepareMLCPCommandCMD(String[] mlcpCommandArray) {
        StringBuilder mlcpCMDCommand = new StringBuilder(prepareMlcpCommandStart(System.getProperty("os.name")));
        for (String commandElement : mlcpCommandArray) {
            mlcpCMDCommand.append(" ").append(commandElement);
        }
        return mlcpCMDCommand.toString();
    }

    String prepareMlcpCommandStart(String osName) {
        if (osName.toLowerCase().startsWith("windows")) {
            return CMD_CALL;
        } else {
            return SH_CALL;
        }
    }

    @Override
    protected void runBulkLoading(String... parameters) {
        String mlcpCommand = parameters[0];
        LOGGER.debug(MESSAGES.getMessage("messages.debug.command", mlcpCommand));
        LOGGER.info(MESSAGES.getMessage("messages.info.startBulkLoad"));
        try {
            Process mlcpProcess = CommandExecutor.executeCommand(mlcpCommand);

            try (InputStream normalInput = mlcpProcess.getInputStream();
                    InputStream errorInput = mlcpProcess.getErrorStream()) {

                Thread normalInputReadProcess = getInputReadProcess(normalInput, System.out);
                normalInputReadProcess.start();

                Thread errorInputReadProcess = getInputReadProcess(errorInput, System.err);
                errorInputReadProcess.start();

                mlcpProcess.waitFor();
                normalInputReadProcess.interrupt();
                errorInputReadProcess.interrupt();

                LOGGER.info(MESSAGES.getMessage("messages.info.finishBulkLoad"));
            }

        } catch (Exception e) {
            LOGGER.error(MESSAGES.getMessage("messages.error.exception", e.getMessage()));
            throw new MarkLogicException(new MarkLogicErrorCode(e.getMessage()), e);
        }
    }

    private Thread getInputReadProcess(final InputStream inputStream, final PrintStream outStream) {
        return new Thread() {

            public void run() {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outStream.println(line);
                    }
                } catch (IOException ioe) {
                    handleIOException(ioe);
                }
            }
        };
    }

    private void handleIOException(IOException ioe) {
        LOGGER.error(MESSAGES.getMessage("messages.error.ioexception", ioe.getMessage()));
        ioe.printStackTrace();
    }
}
