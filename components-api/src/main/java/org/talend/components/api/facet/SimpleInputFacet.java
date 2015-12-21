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

import org.talend.components.api.runtime.ReturnObject;

/**
 * Code to execute the component's facet. This can be used at runtime or design time as required.
 */
public interface SimpleInputFacet extends ComponentFacet {

    /**
     * Connect to an external database
     */
    public void connection();

    /**
     * Retrieve information from the database and put them into the return object
     * 
     * @param returnObject Object that know how to correctly return the current object for any runtime
     * @throws Exception
     */
    public void execute(ReturnObject returnObject) throws Exception;

    /**
     * Close the connection to the database
     */
    public void tearDown();
}
