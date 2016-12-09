//==============================================================================
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
//==============================================================================
package org.talend.components.service.rest.mock;

import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Mock dataset definition.
 */
public class MockDatasetDefinition extends SimpleNamedThing implements DatasetDefinition<MockDatasetProperties> {

    private String name;

    private Class<? extends DatastoreRuntime<? extends DatastoreProperties>> runtimeClass = MockDatastoreRuntime.class;

    public MockDatasetDefinition(String name) {
        super("mock " + name);
        this.name = name;
    }

    @Override
    public Class<MockDatasetProperties> getPropertiesClass() {
        return MockDatasetProperties.class;
    }

    @Override
    public String getDisplayName() {
        return "mock " + name + " datastore";
    }

    @Override
    public String getTitle() {
        return "mock " + name;
    }

    @Override
    public String getImagePath() {
        return "/org/talend/components/mock/mock_" + name + "_icon32.png";
    }

    @Override
    public RuntimeInfo getRuntimeInfo(MockDatasetProperties properties, Object ctx) {
        return new SimpleRuntimeInfo(getClass().getClassLoader(), "", MockDatasetRuntime.class.getName());
    }
}
