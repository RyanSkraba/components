// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.netsuite.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Properties;

/**
 *
 */
public class TestUtils {

    public static Properties loadPropertiesFromLocation(String location) throws IOException {
        URI uri = URI.create(location);
        if (uri.getScheme() == null) {
            try {
                uri = new URI("classpath", location, null);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }
        return loadPropertiesFromLocation(uri);
    }

    public static Properties loadProperties(Properties source, Collection<String> propertyNames) {
        Properties properties = new Properties();
        for (String propertyName : propertyNames) {
            String value = source.getProperty(propertyName);
            if (value != null) {
                properties.setProperty(propertyName, value);
            }
        }
        return properties;
    }

    public static Properties loadPropertiesFromLocation(URI location) throws IOException {
        InputStream stream;
        if (location.getScheme().equals("classpath")) {
            stream = TestUtils.class.getResourceAsStream(location.getSchemeSpecificPart());
        } else {
            stream = location.toURL().openStream();
        }
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } finally {
            stream.close();
        }
        return properties;
    }

}
