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
package org.talend.components.splunk.runtime;

import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.RuntimeContainer;


/**
 * created by dmytro.chmyga on Apr 25, 2016
 */
public class TSplunkEventCollectorWriteOperation implements WriteOperation<WriterResult> {

    private static final long serialVersionUID = 939083892871460237L;

    @Override
    public Writer<WriterResult> createWriter(RuntimeContainer arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void finalize(Iterable<WriterResult> arg0, RuntimeContainer arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public Sink getSink() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void initialize(RuntimeContainer arg0) {
        // TODO Auto-generated method stub

    }

}
