// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

public interface SnowflakeRuntimeSourceOrSink extends SourceOrSink {

    List<NamedThing> getSchemaNames(RuntimeContainer container, Connection connection) throws IOException;

    Schema getSchema(RuntimeContainer container, Connection connection, String tableName) throws IOException;

    ValidationResult validateConnection(SnowflakeProvideConnectionProperties properties);

    Schema getSchemaFromQuery(RuntimeContainer container) throws IOException;

}
