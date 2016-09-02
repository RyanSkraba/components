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
package org.talend.components.api.test;

import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.RuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;

public class SimpleComponentDefinition extends AbstractComponentDefinition {

    /**
     * 
     * @param componentName
     */
    public SimpleComponentDefinition(String componentName) {
        super(componentName);
    }

    private String imagePath;

    private String mavenGroupId;

    private String mavenArtifactId;

    private String name;

    private Class<? extends ComponentProperties> propertyClass;

    @Override
    public Property[] getReturnProperties() {
        return new Property[0];
    }

    @Override
    public String getPngImagePath(ComponentImageType imageType) {
        return imagePath;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return propertyClass;
    }

    /**
     * Sets the imagePath.
     * 
     * @param imagePath the imagePath to set
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * Sets the mavenGroupId.
     * 
     * @param mavenGroupId the mavenGroupId to set
     */
    public void setMavenGroupId(String mavenGroupId) {
        this.mavenGroupId = mavenGroupId;
    }

    /**
     * Sets the mavenArtifactId.
     * 
     * @param mavenArtifactId the mavenArtifactId to set
     */
    public void setMavenArtifactId(String mavenArtifactId) {
        this.mavenArtifactId = mavenArtifactId;
    }

    /**
     * Sets the name.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the propertyClass.
     * 
     * @param propertyClass the propertyClass to set
     */
    public void setPropertyClass(Class<? extends ComponentProperties> propertyClass) {
        this.propertyClass = propertyClass;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(Properties properties, ConnectorTopology componentType) {
        return null;
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return null;
    }

}
