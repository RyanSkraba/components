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
package org.talend.components.common.datastore;

import org.talend.components.common.dataset.DatasetProperties;
import org.talend.daikon.definition.Definition;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Defines a business object that represents a technology that can provide a set of records.
 */
public interface DatastoreDefinition<DatastorePropT extends DatastoreProperties> extends Definition<DatastorePropT> {

    public DatasetProperties createDatasetProperties(DatastorePropT storeProp);

    public String getInputCompDefinitionName();

    public String getOutputCompDefinitionName();

    /**
     * @param properties an instance of the definition.
     * @return an object that can be used to create a runtime instance of this definition, configured by the properties
     * of the instance and the context. This can be null if no runtime applies.
     */
    RuntimeInfo getRuntimeInfo(DatastorePropT properties);

}
