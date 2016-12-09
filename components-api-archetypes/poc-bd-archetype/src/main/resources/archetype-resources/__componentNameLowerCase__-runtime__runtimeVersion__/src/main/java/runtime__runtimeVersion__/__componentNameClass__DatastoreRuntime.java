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
package ${package}.runtime${runtimeVersion};

import ${packageTalend}.api.container.RuntimeContainer;
import ${packageTalend}.common.datastore.runtime.DatastoreRuntime;
import ${packageTalend}.${componentNameLowerCase}.${componentNameClass}DatastoreProperties;
import ${packageDaikon}.properties.ValidationResult;

public class ${componentNameClass}DatastoreRuntime implements DatastoreRuntime<${componentNameClass}DatastoreProperties> {

    /**
     * The datastore instance that this runtime is configured for.
     */
    private ${componentNameClass}DatastoreProperties properties = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ${componentNameClass}DatastoreProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        return null;
    }
}
