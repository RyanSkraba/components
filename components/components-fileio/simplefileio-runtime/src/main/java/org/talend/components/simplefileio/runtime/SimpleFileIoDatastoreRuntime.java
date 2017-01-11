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

import static java.util.Collections.emptyList;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.simplefileio.SimpleFileIoDatastoreProperties;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;
import org.talend.daikon.properties.ValidationResult;

public class SimpleFileIoDatastoreRuntime implements DatastoreRuntime<SimpleFileIoDatastoreProperties> {

    /**
     * The datastore instance that this runtime is configured for.
     */
    private SimpleFileIoDatastoreProperties properties = null;

    /**
     * Helper method for any runtime to get the appropriate {@link UgiDoAs} for executing.
     * 
     * @param properties datastore properties, containing credentials for the cluster.
     * @return An object that can be used to execute actions with the correct credentials.
     */
    public static UgiDoAs getUgiDoAs(SimpleFileIoDatastoreProperties properties) {
        if (properties.useKerberos.getValue())
            return UgiDoAs.ofKerberos(properties.kerberosPrincipal.getValue(), properties.kerberosKeytab.getValue());
        else if (properties.userName.getValue() != null && !properties.userName.getValue().isEmpty())
            return UgiDoAs.ofSimple(properties.userName.getValue());
        else
            return UgiDoAs.ofNone();
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, SimpleFileIoDatastoreProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        return emptyList();
    }
}
