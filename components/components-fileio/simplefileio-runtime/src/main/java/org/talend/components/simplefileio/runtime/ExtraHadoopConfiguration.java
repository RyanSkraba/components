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
package org.talend.components.simplefileio.runtime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;

/**
 * Supply Hadoop configuration values for runtime objects.
 *
 * These configuration values should override the default job configuration that is discovered on the nodes of the
 * cluster (and might not be available in the driver).
 *
 * This class is not immutable, but its contents should not be changed once the job is launched.
 */
public class ExtraHadoopConfiguration implements Serializable {

    /** Hadoop configuration that should be added for the InputFormat. */
    private final Map<String, String> extraConfig = new HashMap<>();

    /**
     * Add configuration to the Hadoop configuration.
     *
     * @param key The hadoop key to set.
     * @param value The value to use.
     */
    public void set(String key, String value) {
        extraConfig.put(key, value);
    }

    /**
     * Add all of the extra Hadoop configuration settings to the given instance.  This instance is unchanged.
     * 
     * @param config The destination storage that should include the values in this object.
     */
    public void addTo(Configuration config) {
        for (Map.Entry<String, String> entry : extraConfig.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Add all of the extra Hadoop configuration settings from the given instance.  That instance is unchanged.
     *
     * @param config The source storage that should be added to the values in this object.
     */
    public void addFrom(ExtraHadoopConfiguration config) {
        for (Map.Entry<String, String> entry : config.extraConfig.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }
}
