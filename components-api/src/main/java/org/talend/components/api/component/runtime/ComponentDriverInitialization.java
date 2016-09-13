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
 * Basic interface for defining properties and common methods required for Runtimes of components without connectors, or
 * components which need some pre-processing before performing the actual job.
 */
public interface ComponentDriverInitialization extends ComponentRuntime {

    /**
     * Execute some code before the job or flow is started. This code will be called on driver node only, before actual
     * runtime process. In case of components with connectors, it will be called before serializing and sending runtimes
     * to worker nodes.
     * <p>
     * In case of components without connectors, no other work should be performed for such runtime, only this method
     * will be called after runtime initialization.
     * </p>
     * 
     * @throw an RuntimeException if anything fails during the run of this method.
     */
    void runAtDriver();

}
