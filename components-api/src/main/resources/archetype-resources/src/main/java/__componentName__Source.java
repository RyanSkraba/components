#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.io.File;
import java.io.IOException;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.util.UnshardedInputSource;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.ValidationResult;

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

    /** Configuration extracted from the input properties. */
    private ${componentName}Properties properties;
    
    private transient Schema schema;

    @Override
    public void initialize(RuntimeContainer adaptor, ComponentProperties properties) {
        this.properties = (${componentName}Properties) properties; 
        schema = new Schema.Parser().parse(this.properties.schema.schema.getStringValue()); 
        setUnshardedInput(new ${componentName}Reader(this, this.properties.filename.getStringValue()));
    }
    
    @Override
    public ValidationResult validate(RuntimeContainer adaptor) {
        // Check that the file exists.
        File f = new File(this.properties.filename.getStringValue());
        if (!f.exists()) {
            ValidationResult vr = new ValidationResult();
            vr.setMessage("The file '" + f.getPath() + "' does not exist."); //$NON-NLS-1$//$NON-NLS-2$
            vr.setStatus(ValidationResult.Result.ERROR);
            return vr;
        }
        // Check that there is exactly one column to contain the output.
        if (schema.getFields().size() != 1) {
            ValidationResult vr = new ValidationResult();
            vr.setMessage("The schema must have exactly one column."); //$NON-NLS-1$
            vr.setStatus(ValidationResult.Result.ERROR);
            return vr;
        }
        
        return ValidationResult.OK;
    }
    
    @Override
    public Schema getSchemaFromProperties(RuntimeContainer adaptor) throws IOException {
        return schema;
    }

    @Override
    public Schema getPossibleSchemaFromProperties(RuntimeContainer adaptor) throws IOException {
        return schema;
    }
    
}
