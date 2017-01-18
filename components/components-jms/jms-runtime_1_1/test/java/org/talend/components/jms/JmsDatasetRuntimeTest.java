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

package org.talend.components.jms;

import org.apache.avro.Schema;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jms.runtime_1_1.JmsDatasetRuntime;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.runtime.RuntimeInfo;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class JmsDatasetRuntimeTest {

    private final JmsDatasetRuntime datasetRuntime = new JmsDatasetRuntime();

}
