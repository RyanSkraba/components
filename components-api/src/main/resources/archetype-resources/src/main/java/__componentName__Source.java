#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )

package ${package};

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;

import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.schema.Schema;

public class ${componentName}Source implements BoundedSource {

    private static final Logger LOG = LoggerFactory.getLogger(${componentName}Source.class);

    private ${componentName}Properties properties;

    public ${componentName}Source() {
    }

    @Override
    public void initialize(RuntimeContainer adaptor, ComponentProperties properties) {
        this.properties = (${componentName}Properties) properties;
    }

    @Override
    public ValidationResult validate(RuntimeContainer adaptor) {
        return new ValidationResult();
    }

    @Override
    public BoundedReader createReader(RuntimeContainer adaptor) {
        return new ${componentName}Reader(adaptor, this, properties);
    }

    @Override
    public List<? extends BoundedSource> splitIntoBundles(long desiredBundleSizeBytes, RuntimeContainer adaptor)
            throws Exception {
        List<BoundedSource> list = new ArrayList<>();
        list.add(this);
        return list;
    }

    @Override
    public long getEstimatedSizeBytes(RuntimeContainer adaptor) {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(RuntimeContainer adaptor) {
        return false;
    }

    @Override
    public Schema getSchema(RuntimeContainer adaptor, String schemaName) throws IOException {
        return null;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer adaptor) throws IOException {
        return null;
    }


}
