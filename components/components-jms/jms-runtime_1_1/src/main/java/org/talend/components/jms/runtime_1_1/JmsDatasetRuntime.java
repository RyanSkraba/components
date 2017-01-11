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

package org.talend.components.jms.runtime_1_1;

import org.talend.components.jms.JmsDatasetProperties;
import org.talend.components.jms.JmsMessageType;
import org.talend.components.jms.JmsProcessingMode;

public class JmsDatasetRuntime {

    private JmsDatasetProperties properties;

    private JmsMessageType msgType;

    private JmsProcessingMode processingMode;

    public JmsDatastoreRuntime datastoreRuntime = new JmsDatastoreRuntime();
}
