
package org.talend.components.multiruntime;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.multiruntime.MultiRuntimeComponentProperties.Version;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * The MultiRuntimeComponentDefinition acts as an entry point for all of services that
 * a component provides to integrate with the Studio (at design-time) and other
 * components (at run-time).
 */
public class MultiRuntimeComponentDefinition extends AbstractComponentDefinition {

    public static final String COMPONENT_NAME = "MultiRuntimeComponent"; //$NON-NLS-1$

    public MultiRuntimeComponentDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "File/Input" }; //$NON-NLS-1$
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] {};
    }

    @Override
    public String getPngImagePath(ComponentImageType imageType) {
        switch (imageType) {
        case PALLETE_ICON_32X32:
            return "fileReader_icon32.png"; //$NON-NLS-1$
        default:
            return "fileReader_icon32.png"; //$NON-NLS-1$
        }
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return MultiRuntimeComponentProperties.class;
    }

    public static RuntimeInfo getRuntimeInfoFromVersion(Version version) {
        RuntimeInfo result = null;
        switch (version) {
        case VERSION_0_1:
            try {
                result = new JarRuntimeInfo(
                        new URL("mvn:org.talend.components/test-multiple-runtime-comp-runtime-v01/0.1.0-SNAPSHOT"),
                        DependenciesReader.computeDependenciesFilePath("org.talend.components",
                                "test-multiple-runtime-comp-runtime-v01"),
                        "org.talend.multiruntime.MultiRuntimeComponentSource");
            } catch (MalformedURLException e1) {
                throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e1);
            }
            break;
        case VERSION_0_2:
            try {
                result = new JarRuntimeInfo(
                        new URL("mvn:org.talend.components/test-multiple-runtime-comp-runtime-v02/0.1.0-SNAPSHOT"),
                        DependenciesReader.computeDependenciesFilePath("org.talend.components",
                                "test-multiple-runtime-comp-runtime-v02"),
                        "org.talend.multiruntime.MultiRuntimeComponentSource");
            } catch (MalformedURLException e1) {
                throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e1);
            }
            break;
        default:
            break;
        }
        return result;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(Properties prop, ConnectorTopology connectorTopology) {
        if (connectorTopology == ConnectorTopology.OUTGOING) {
            if (prop != null && prop instanceof MultiRuntimeComponentProperties) {
                MultiRuntimeComponentProperties mrcpProp = (MultiRuntimeComponentProperties) prop;
                return getRuntimeInfoFromVersion(mrcpProp.version.getValue());
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.OUTGOING);
    }

}
