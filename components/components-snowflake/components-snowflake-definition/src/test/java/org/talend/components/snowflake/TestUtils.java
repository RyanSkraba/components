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

package org.talend.components.snowflake;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * Class for tests util methods.
 */
public abstract class TestUtils {

    public static String getResourceAsString(Class clazz, String path) throws IOException {
        return getResourceAsString(clazz, path, "UTF-8");
    }

    public static String getResourceAsString(Class clazz, String path, String encoding) throws IOException {
        InputStream is = clazz.getResourceAsStream(path);
        if (is == null) {
            throw new FileNotFoundException(String.format(
                    "Resource not found: %s, %s", clazz.getName(), path));
        }
        return IOUtils.toString(is, encoding);
    }
}
