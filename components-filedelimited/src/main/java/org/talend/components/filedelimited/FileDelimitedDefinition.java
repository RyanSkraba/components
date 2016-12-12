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

package org.talend.components.filedelimited;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.property.Property;

public abstract class FileDelimitedDefinition extends AbstractComponentDefinition {

    public FileDelimitedDefinition(String componentName) {
        super(componentName, ExecutionEngine.DI);
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] {};
    }

    public String getMavenGroupId() {
        return "org.talend.components";
    }

    public String getMavenArtifactId() {
        return "components-filedelimited";
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return FileDelimitedProperties.class;
    }

    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { FileDelimitedProperties.class };
    }

}
