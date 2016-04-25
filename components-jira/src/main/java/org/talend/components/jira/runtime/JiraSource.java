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
package org.talend.components.jira.runtime;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jira.tjirainput.TJiraInputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

/**
 * Jira source implementation
 */
public class JiraSource implements Source{
    
    /**
     * Jira component properties
     */
    private TJiraInputProperties properties;

    
    /**
     * Stores component properties in this object
     * 
     * @param container runtime container
     * @param properties
     */
    @Override
    public void initialize(RuntimeContainer container, ComponentProperties properties) {
        //FIXME could it throw cast exception?
        this.properties = (TJiraInputProperties) properties;
    }

    /**
     * What should I validate here?
     * validate connection to Jira here
     * 
     */
    @Override
    public ValidationResult validate(RuntimeContainer container) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Schema getSchema(RuntimeContainer container, String schemaName) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Reader createReader(RuntimeContainer container) {
        // TODO Auto-generated method stub
        return null;
    }

}
