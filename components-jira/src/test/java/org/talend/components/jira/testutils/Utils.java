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
package org.talend.components.jira.testutils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains utility methods for tests
 */
public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    /**
     * Read file content to String with default utf8 and returns it
     * 
     * @param path file path
     * @return file contents
     * @throws IOException in case of exception
     */
    public static String readFile(String path) {
        return readFile(path, StandardCharsets.UTF_8);
    }

    /**
     * Read file content to String and returns it
     * 
     * @param path file path
     * @param encoding charset encoding
     * @return file contents
     * @throws IOException in case of exception
     */
    public static String readFile(String path, Charset encoding) {
        byte[] encoded = null;
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            LOG.error("Exception during file read. {}", e.getMessage());
        }
        return new String(encoded, encoding);
    }
}
