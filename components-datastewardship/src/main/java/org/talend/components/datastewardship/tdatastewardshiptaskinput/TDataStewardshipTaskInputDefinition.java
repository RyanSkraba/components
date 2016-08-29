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
package org.talend.components.datastewardship.tdatastewardshiptaskinput;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.datastewardship.TdsDefinition;
import org.talend.components.datastewardship.runtime.TdsTaskSource;
import org.talend.daikon.properties.property.Property;

import aQute.bnd.annotation.component.Component;

/**
 * Data Stewardship Task input component definition
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + TDataStewardshipTaskInputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TDataStewardshipTaskInputDefinition extends TdsDefinition implements InputComponentDefinition {

    /**
     * Component name
     */
    public static final String COMPONENT_NAME = "tDataStewardshipTaskInput";  //$NON-NLS-1$

    /**
     * Constructor sets component name
     */
    public TDataStewardshipTaskInputDefinition() {
        super(COMPONENT_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Source getRuntime() {
        return new TdsTaskSource();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TDataStewardshipTaskInputProperties.class;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_TOTAL_RECORD_COUNT_PROP, RETURN_SUCCESS_RECORD_COUNT_PROP, RETURN_REJECT_RECORD_COUNT_PROP,
                RETURN_ERROR_MESSAGE_PROP };
    }

}
