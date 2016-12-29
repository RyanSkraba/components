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
package org.talend.components.api.component.runtime;

/**
 * When creating a {@link org.talend.daikon.runtime.RuntimeInfo} object via a
 * {@link org.talend.components.api.component.ComponentDefinition}, you might want to specify where the runtime instance
 * will be running.
 */
public enum ExecutionEngine {
    DI,
    DI_SPARK_BATCH,
    DI_SPARK_STREAMING,
    BEAM
}
