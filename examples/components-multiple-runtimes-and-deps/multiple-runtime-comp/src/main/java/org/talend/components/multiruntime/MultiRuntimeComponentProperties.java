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
package org.talend.components.multiruntime;

import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 * The ComponentProperties subclass provided by a component stores the
 * configuration of a component and is used for:
 * 
 * <ol>
 * <li>Specifying the format and type of information (properties) that is
 * provided at design-time to configure a component for run-time,</li>
 * <li>Validating the properties of the component at design-time,</li>
 * <li>Containing the untyped values of the properties, and</li>
 * <li>All of the UI information for laying out and presenting the
 * properties to the user.</li>
 * </ol>
 * 
 * The MultiRuntimeComponentProperties has two properties:
 * <ol>
 * <li>{code filename}, a simple property which is a String containing the
 * file path that this component will read.</li>
 * <li>{code schema}, an embedded property referring to a Schema.</li>
 * </ol>
 */
public class MultiRuntimeComponentProperties extends ComponentPropertiesImpl {

    public enum Version {
        VERSION_0_1,
        VERSION_0_2;
    }

    public Property<Version> version = PropertyFactory.newEnum("version", Version.class); //$NON-NLS-1$

    public MultiRuntimeComponentProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        // Code for property initialization goes here
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN);
        form.addRow(version);
    }

    // this simulates the possible client callback that wuld need some runtime access for getting schema for instance.
    public String getVersion1RuntimeResult() {
        RuntimeInfo runtimeInfoFromVersion = MultiRuntimeComponentDefinition.getRuntimeInfoFromVersion(Version.VERSION_0_1);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfoFromVersion,
                getClass().getClassLoader())) {
            Source source = (Source) sandboxedInstance.getInstance();
            return source.validate(null).getMessage();
        }
    }

    public String getVersion2RuntimeResult() {
        RuntimeInfo runtimeInfoFromVersion = MultiRuntimeComponentDefinition.getRuntimeInfoFromVersion(Version.VERSION_0_2);
        try (SandboxedInstance sandboxedInstance = RuntimeUtil.createRuntimeClass(runtimeInfoFromVersion,
                getClass().getClassLoader())) {
            Source source = (Source) sandboxedInstance.getInstance();
            return source.validate(null).getMessage();
        }
    }

}
