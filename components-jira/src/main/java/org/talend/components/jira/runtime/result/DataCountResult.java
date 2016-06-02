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
package org.talend.components.jira.runtime.result;

import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.RuntimeContainer;

/**
 * FIXME Remove it
 * 
 * It is temporary fix. This class duplicates one from components-common. It should be removed
 * after new version of components-common appear in the repository
 * 
 * Result, which which is returned by {@link Writer#close()} and consumed by
 * {@link WriteOperation#finalize(Iterable, RuntimeContainer)} <br>
 * 
 * Stores: <br>
 * 1. number of all data consumed by {@link Writer} <br>
 * 2. number of success data 3. number of reject data
 */
public class DataCountResult extends WriterResult {

    private static final long serialVersionUID = -8631227771462168357L;

    /**
     * Number of successfully written data
     */
    private int successCount;

    /**
     * Number of data rejected by data sink
     */
    private int rejectCount;

    /**
     * Constructor sets output results to return to {@link WriteOperation}
     * 
     * @param uId unique Id provided when calling {@link Writer#open(String)}
     * @param dataCount number of data passed to {@link Writer#write(Object)}
     * @param successCount number of successfully written data
     * @param rejectCount number of data rejected
     */
    public DataCountResult(String uId, int dataCount, int successCount, int rejectCount) {
        super(uId, dataCount);
        this.successCount = successCount;
        this.rejectCount = rejectCount;
    }

    /**
     * Returns number of successfully written data
     * 
     * @return number of successfully written data
     */
    public int getSuccessCount() {
        return successCount;
    }

    /**
     * Returns number of data rejected by data sink
     * 
     * @return number of data rejected
     */
    public int getRejectCount() {
        return rejectCount;
    }

}
