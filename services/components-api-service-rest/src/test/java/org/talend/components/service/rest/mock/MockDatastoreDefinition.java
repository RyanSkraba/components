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
package org.talend.components.service.rest.mock;

import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * Mock datastore definition.
 */
public class MockDatastoreDefinition extends SimpleNamedThing implements DatastoreDefinition<MockDatastoreProperties> {

    public static final String MOCK_DATASET_PROPERTIES_NAME = "mock dataset";

    private String name;

    private Class<? extends DatastoreRuntime<? extends DatastoreProperties>> runtimeClass = MockDatastoreRuntime.class;

    public MockDatastoreDefinition(String name) {
        super("mock " + name);
        this.name = name;
    }

    @Override
    public Class<MockDatastoreProperties> getPropertiesClass() {
        return MockDatastoreProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(MockDatastoreProperties properties) {
        return new SimpleRuntimeInfo(getClass().getClassLoader(), "", runtimeClass.getName());
    }

    @Override
    public DatasetProperties createDatasetProperties(MockDatastoreProperties storeProp) {
        return new MockDatasetProperties(MOCK_DATASET_PROPERTIES_NAME);
    }

    @Override
    public String getInputCompDefinitionName() {
        return null;
    }

    @Override
    public String getOutputCompDefinitionName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "mock " + name + " datastore";
    }

    @Override
    public String getTitle() {
        return "mock " + name;
    }

    @Deprecated
    @Override
    public String getImagePath() {
        return "/org/talend/components/mock/mock_" + name + "_icon32.png";
    }

    @Override
    public String getImagePath(DefinitionImageType type) {
        return null;
    }

    @Override
    public String getIconKey() {
        return null;
    }


}
