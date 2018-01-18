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
package org.talend.components.marklogic.runtime.input;

import java.util.ArrayList;
import java.util.List;

import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marklogic.runtime.MarkLogicSourceOrSink;
import org.talend.components.marklogic.tmarklogicinput.MarkLogicInputProperties;
import org.talend.daikon.properties.ValidationResult;

public class MarkLogicSource extends MarkLogicSourceOrSink implements BoundedSource {

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
    public MarkLogicCriteriaReader createReader(RuntimeContainer adaptor) {
        return new MarkLogicCriteriaReader(this, adaptor, (MarkLogicInputProperties) ioProperties);
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        if (ioProperties instanceof MarkLogicInputProperties) {
            checkDocContentTypeSupported(((MarkLogicInputProperties) ioProperties).datasetProperties.main);
        } else {
           return new ValidationResult(ValidationResult.Result.ERROR, MESSAGES.getMessage("error.wrongProperties"));
        }
        return super.validate(container);
    }
}
