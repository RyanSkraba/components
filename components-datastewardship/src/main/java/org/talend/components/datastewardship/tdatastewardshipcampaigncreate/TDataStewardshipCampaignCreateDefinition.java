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
package org.talend.components.datastewardship.tdatastewardshipcampaigncreate;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.OutputComponentDefinition;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.datastewardship.TdsDefinition;
import org.talend.components.datastewardship.runtime.TdsCampaignSink;
import org.talend.daikon.properties.property.Property;

import aQute.bnd.annotation.component.Component;

/**
 * Data Stewardship Campaign output component definition
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + TDataStewardshipCampaignCreateDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TDataStewardshipCampaignCreateDefinition extends TdsDefinition implements OutputComponentDefinition {

    /**
     * component name
     */
    public static final String COMPONENT_NAME = "tDataStewardshipCampaignCreate";  //$NON-NLS-1$

    /**
     * Constructor sets component name
     */
    public TDataStewardshipCampaignCreateDefinition() {
        super(COMPONENT_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sink getRuntime() {
        return new TdsCampaignSink();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TDataStewardshipCampaignCreateProperties.class;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_TOTAL_RECORD_COUNT_PROP, RETURN_SUCCESS_RECORD_COUNT_PROP, RETURN_REJECT_RECORD_COUNT_PROP,
                RETURN_ERROR_MESSAGE_PROP };
    }

}
