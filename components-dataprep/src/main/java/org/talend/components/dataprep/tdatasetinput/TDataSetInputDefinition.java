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
package org.talend.components.dataprep.tdatasetinput;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dataprep.DataPrepDefinition;
import org.talend.components.dataprep.runtime.DataSetSource;

import aQute.bnd.annotation.component.Component;

/**
 * The TDataSetInputDefinition acts as an entry point for all of services that a component provides to integrate with
 * the Studio (at design-time) and other components (at run-time).
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + TDataSetInputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TDataSetInputDefinition extends DataPrepDefinition implements InputComponentDefinition {

    public static final String COMPONENT_NAME = "tDatasetInput";

    public TDataSetInputDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TDataSetInputProperties.class;
    }

    @Override
    public Source getRuntime() {
        return new DataSetSource();
    }
}
