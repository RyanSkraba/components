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
package org.talend.components.mongodb.tmongodboutput;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.SimpleOutputDefinition;
import org.talend.components.api.runtime.ComponentRuntime;

import aQute.bnd.annotation.component.Component;

/**
 * Component that can connect to a salesforce system and get some data out of it.
 */

@Component(name = Constants.COMPONENT_BEAN_PREFIX + MongoDBOutputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class MongoDBOutputDefinition extends SimpleOutputDefinition {

    public static final String COMPONENT_NAME = "tMongoDBOutputNew"; //$NON-NLS-1$

    public static final String POM_LOCATION = "/org/talend/components/mongodb/pom.xml";

    public MongoDBOutputDefinition() {
        super(COMPONENT_NAME, POM_LOCATION);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "NoSQL/Output", "NoSQL/MongoDB" };
    }

    // TODO Where is the create Runtime?
    @Override
    public ComponentRuntime createRuntime() {
        return null;
    }

    // public FrameworkRuntime createFrameworkRuntime() {
    // // create Framework runtime
    // return new FilterColumnRuntime();
    // }

    @Override
    public Class<?> getPropertyClass() {
        // TODO Find a way to force the name of the schema of the property.
        // maybe we can add class on the PropertyFactory in order to force the name the name of the schema, but not the
        // content of it.
        return MongoDBOutputColumnProperties.class;
    }

}
