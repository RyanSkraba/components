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
package org.talend.components.api.component.runtime;

import java.io.Serializable;

import org.talend.components.api.container.RuntimeContainer;

/**
 * A suggested implementation of the result of a series of writes which is returned by {@link Writer#close()} and
 * consumed by {@link WriteOperation#finalize(Iterable, RuntimeContainer)}.
 */
public class WriterResult implements Serializable {

    private static final long serialVersionUID = 8670579213592463768L;

    protected String uId;

    private int dataCount;

    public String getuId() {
        return this.uId;
    }

    /**
     * create a writer result for a single writer.
     * 
     * @param uId unique Id provided when calling {@link Writer#open(String)}
     */
    public WriterResult(String uId, int dataCount) {
        this.uId = uId;
        this.dataCount = dataCount;
    }

    public int getDataCount() {
        return dataCount;
    }

}
