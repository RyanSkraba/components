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
package org.talend.components.marketo.runtime;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;

public interface MarketoSourceOrSinkSchemaProvider extends SourceOrSink {

    Schema getSchemaForCustomObject(String customObjectName) throws IOException;

    Schema getSchemaForCompany() throws IOException;

    Schema getSchemaForOpportunity() throws IOException;

    Schema getSchemaForOpportunityRole() throws IOException;

    List<String> getCompoundKeyFields(String resource) throws IOException;
}
