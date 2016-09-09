package org.talend.components.multiruntime;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.component.runtime.RuntimeInfo;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.runtimeservice.RuntimeUtil;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;
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
public class MultiRuntimeComponentProperties extends FixedConnectorsComponentProperties {

    public enum Version {
        VERSION_0_1,
        VERSION_0_2;
    }

    public Property<Version> version = PropertyFactory.newEnum("version", Version.class); //$NON-NLS-1$

    public SchemaProperties schema = new SchemaProperties("schema"); //$NON-NLS-1$

    protected transient PropertyPathConnector mainConnector = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

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
        form.addRow(schema.getForm(Form.REFERENCE));
        form.addRow(version);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputComponent) {
        if (isOutputComponent) {
            return Collections.singleton(mainConnector);
        }
        return Collections.emptySet();
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
