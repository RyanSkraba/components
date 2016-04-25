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

import java.io.IOException;

import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;


/**
 * created by dmytro.chmyga on Apr 25, 2016
 */
public class TSplunkEventCollectorWriter implements Writer<WriterResult> {

    @Override
    public WriterResult close() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WriteOperation<WriterResult> getWriteOperation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void open(String arg0) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void write(Object arg0) throws IOException {
        // TODO Auto-generated method stub

    }

}
