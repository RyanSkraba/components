//==============================================================================
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
//==============================================================================

package org.talend.components.service.rest;

import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.daikon.definition.Definition;

/**
 * Supported definition types.
 */
public enum DefinitionType {
    COMPONENT(ComponentDefinition.class),
    DATA_STORE(DatastoreDefinition.class);

    /** The target class for the type. */
    private Class<? extends Definition> targetClass;

    /**
     * Private constructor.
     * @param targetClass the target class.
     */
    DefinitionType(Class<? extends Definition> targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * @return the target class.
     */
    public Class<? extends Definition> getTargetClass() {
        return targetClass;
    }



}
