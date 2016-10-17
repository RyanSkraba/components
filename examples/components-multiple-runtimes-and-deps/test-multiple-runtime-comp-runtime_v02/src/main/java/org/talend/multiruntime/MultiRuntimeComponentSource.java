package org.talend.multiruntime;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.multiruntime.MultiRuntimeComponentProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.test.MyClass2;

/**
 * The MultiRuntimeComponentSource provides the mechanism to supply data to other components at run-time.
 *
 * Based on the Apache Beam project, the Source mechanism is appropriate to describe distributed and non-distributed
 * data sources and can be adapted to scalable big data execution engines on a cluster, or run locally.
 *
 * This example component describes an input source that is guaranteed to be run in a single JVM (whether on a cluster
 * or locally), so:
 *
 * <ul>
 * <li>the simplified logic for reading is found in the {@link MultiRuntimeComponentReader}, and</li>
 * </ul>
 */
public class MultiRuntimeComponentSource implements BoundedSource {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Configuration extracted from the input properties. */
    private MultiRuntimeComponentProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (MultiRuntimeComponentProperties) properties;
        return ValidationResult.OK;
    }

    @Override
    public BoundedReader createReader(RuntimeContainer container) {
        return null;
    }

    @Override
    public ValidationResult validate(RuntimeContainer adaptor) {
        return ValidationResult.OK.setMessage(MyClass2.showAnotherMe());
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return null;
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        // TODO Auto-generated method stub
        return false;
    }

}
