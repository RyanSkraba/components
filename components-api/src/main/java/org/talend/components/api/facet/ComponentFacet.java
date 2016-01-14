// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.facet;

import java.io.Serializable;

import org.talend.components.api.properties.ComponentProperties;

/**
 * Code to execute the component's facet. This can be used at runtime or design time as required.
 */
public interface ComponentFacet extends Serializable {

    /**
     * used to initialise this facet before the process is done.
     * 
     * @param context ComponentPropertie used to get parameters for the process
     */
    void setUp(ComponentProperties context);

    /**
     * Called after the process is over to release all resources opened.
     */
    void tearDown();
}
