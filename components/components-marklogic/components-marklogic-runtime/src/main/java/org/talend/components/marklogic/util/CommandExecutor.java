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

package org.talend.components.marklogic.util;

import java.io.IOException;

/**
 * Util class used for mock system package.
 */
public class CommandExecutor {

    /**
     * Creates and execute system command line process
     *
     * @param command String to execute in console
     * @return Process which was executed
     * @throws IOException when execution exception occurs
     */
    public static Process executeCommand(String command) throws IOException {
        return Runtime.getRuntime().exec(command);
    }

}
