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
package org.talend.components.jdbc.runtime;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.runtime.reader.JDBCRowReader;
import org.talend.daikon.properties.ValidationResult;

/**
 * JDBC row runtime execution object for input action
 *
 */
public class JDBCRowSource extends JDBCRowSourceOrSink implements BoundedSource {

    private static final long serialVersionUID = -9111994542816954024L;

    @SuppressWarnings("rawtypes")
    @Override
    public BoundedReader createReader(RuntimeContainer container) {
        JDBCRowReader reader = new JDBCRowReader(container, this, properties);
        return reader;
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        List<BoundedSource> list = new ArrayList<>();
        list.add(this);
        return list;
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

    @Override
    public ValidationResult validate(RuntimeContainer runtime) {
        ValidationResult vr = new ValidationResult();
        try {
            connect(runtime);
        } catch (Exception ex) {
            fillValidationResult(vr, ex);
        }
        return vr;
    }

}
