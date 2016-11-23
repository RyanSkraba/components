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
package org.talend.components.snowflake.runtime;

import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.snowflake.tsnowflakeinput.TSnowflakeInputProperties;
import org.talend.daikon.exception.TalendRuntimeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SnowflakeSource extends SnowflakeSourceOrSink implements BoundedSource {

    public SnowflakeSource() {
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
    public BoundedReader createReader(RuntimeContainer container) {
        if (properties instanceof TSnowflakeInputProperties) {
            TSnowflakeInputProperties sfInProps = (TSnowflakeInputProperties) properties;
            try {
                return new SnowflakeReader(container, this, sfInProps);
            } catch (IOException e) {
                TalendRuntimeException.unexpectedException(e);
            }
        }
        TalendRuntimeException.unexpectedException("Unknown properties: " + properties);
        return null;
    }

}
