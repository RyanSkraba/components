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
package org.talend.components.kafka;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.daikon.properties.property.Property;

public abstract class KafkaIOBasedDefinition extends AbstractComponentDefinition {

    public KafkaIOBasedDefinition(String componentName) {
        super(componentName, ExecutionEngine.BEAM);
    }

    @Override
    public String getIconKey() {
        return "kafka";
    }

    @Override
    public String[] getFamilies() {
        return new String[] { KafkaFamilyDefinition.NAME };
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[0];
    }

}
