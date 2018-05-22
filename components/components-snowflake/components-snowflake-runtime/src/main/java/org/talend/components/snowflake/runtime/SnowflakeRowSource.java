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
package org.talend.components.snowflake.runtime;

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.container.RuntimeContainer;

/**
 * This class implements {@link Source} for tSnowflakeRow component with
 * {@link ConnectorTopology#OUTGOING} topology.
 *
 */
public class SnowflakeRowSource extends SnowflakeRowSourceOrSink implements Source {

    private static final long serialVersionUID = -3509327791069972700L;

    @Override
    public Reader<IndexedRecord> createReader(RuntimeContainer container) {
        return new SnowflakeRowReader(container, this);
    }
}
