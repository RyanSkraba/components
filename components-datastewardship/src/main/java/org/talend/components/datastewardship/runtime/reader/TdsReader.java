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
package org.talend.components.datastewardship.runtime.reader;

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.datastewardship.connection.TdsConnection;
import org.talend.components.datastewardship.runtime.TdsSource;

public abstract class TdsReader implements Reader<IndexedRecord> {

    /**
     * {@link Source} instance, which had created this {@link Reader}
     */
    protected final TdsSource source;

    /**
     * Http connection
     */
    protected TdsConnection tdsConn;

    /**
     * Denotes this {@link Reader} was started
     */
    protected boolean started;

    /**
     * Denotes this {@link Reader} has more records
     */
    protected boolean hasMoreRecords;
    
    /**
     * Return result
     */
    protected Result result;

    public TdsReader(TdsSource source) {
        this.source = source;
        this.tdsConn = new TdsConnection(source.getUrl(), source.getUsername(), source.getPassword());
        this.result = new Result();
    }

}
