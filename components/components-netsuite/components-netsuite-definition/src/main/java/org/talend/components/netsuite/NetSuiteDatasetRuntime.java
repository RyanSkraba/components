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

package org.talend.components.netsuite;

import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.netsuite.schema.SearchInfo;
import org.talend.daikon.NamedThing;

/**
 *
 */
public interface NetSuiteDatasetRuntime {

    List<NamedThing> getRecordTypes();

    List<NamedThing> getSearchableTypes();

    SearchInfo getSearchInfo(String typeName);

    List<String> getSearchFieldOperators();

    Schema getSchema(String typeName);

    Schema getSchemaForUpdate(String typeName);

    Schema getSchemaForDelete(String typeName);

    Schema getSchemaForUpdateFlow(String typeName, Schema schema);

    Schema getSchemaForDeleteFlow(String typeName, Schema schema);

    Schema getSchemaForUpdateReject(String typeName, Schema schema);

    Schema getSchemaForDeleteReject(String typeName, Schema schema);
}
