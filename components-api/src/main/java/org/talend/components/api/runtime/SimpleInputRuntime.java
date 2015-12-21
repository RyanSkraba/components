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

public interface SimpleInputRuntime<FrameworkObject> extends FrameworkRuntime {

    /**
     * Retrieve or generate input data and put them into the main flow compatible with the current Framework
     */
    public void genericExecute() throws Exception;

    /**
     * Retrieve the main output for tor the current framework
     *
     * @return
     */
    public FrameworkObject getMainOutput();
}
