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
package org.talend.components.api.runtime;

import org.apache.avro.generic.IndexedRecord;

/**
 * Code to execute the component's facet. This can be used at runtime or design time as required.
 */
public interface SimpleInputRuntime<OutputObject extends IndexedRecord> extends BaseRuntime {

    /**
     * called to create all the inputs values they should all be outputed using the soc instance.
     * 
     * @param soc object used to ouput things.
     * @throws Exception
     */
    public void execute(SingleOutputConnector<OutputObject> soc) throws Exception;

}
