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
package org.talend.components.api.runtime;

public interface RejectableTransformationRuntime<FrameworkObject> extends FrameworkRuntime {

    /**
     * Execute a transformation with a main flow and a reject flow compatible with the current Framework
     *
     * @param inputs
     * @throws Exception
     */
    public void genericExecute(FrameworkObject inputs) throws Exception;

    /**
     * Retrieve the main output for tor the current framework
     *
     * @return
     */
    public FrameworkObject getMainOutput();

    /**
     * Retrieve the error output for tor the current framework
     *
     * @return
     */
    public FrameworkObject getErrorOutput();

}
