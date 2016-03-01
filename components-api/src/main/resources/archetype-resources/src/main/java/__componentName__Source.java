#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )

package ${package};

import org.talend.components.api.component.runtime.util.UnshardedInputSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;

/**
 * The ${componentName}Source provides the mechanism to supply data to other
 * components at run-time.
 * 
 * Based on the Apache Beam project, the Source mechanism is appropriate to
 * describe distributed and non-distributed data sources and can be adapted
 * to scalable big data execution engines on a cluster, or run locally.
 * 
 * This example component describes an input source that is guaranteed to be
 * run in a single JVM (whether on a cluster or locally), so:
 * 
 * <ul>
 * <li>the simplified logic for reading is found in the {@link ${componentName}UnshardedInput}, 
 *     and</li>
 * <li>adapted to the full Source specification via a helper 
 *     {@link UnshardedInputSource}.</li>
 * </ul>
 */
public class ${componentName}Source extends UnshardedInputSource<String> {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    @Override
    public void initialize(RuntimeContainer adaptor, ComponentProperties properties) {
        setUnshardedInput(new ${componentName}UnshardedInput(((${componentName}Properties) properties).filename.getStringValue()));
    }
}
