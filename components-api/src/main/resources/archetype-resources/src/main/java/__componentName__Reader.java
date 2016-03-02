#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )

package ${package};

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsErrorCode;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.schema.SchemaElement;

public class ${componentName}Reader implements BoundedReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(${componentName}Reader.class);

    protected ${componentName}Source source;

    protected ${componentName}Properties properties;

    protected RuntimeContainer container;

    protected BufferedReader reader;

    protected Map<String, Object> row;

    protected String schemaName;

    public ${componentName}Reader(RuntimeContainer container, ${componentName}Source source, ${componentName}Properties props) {
        this.source = source;
        this.container = container;
        this.properties = props;
    }

    @Override
    public boolean start() throws IOException {
        LOGGER.debug("open: " + properties.filename.getStringValue());
        // get first schema element
        if (!properties.schema.schema.getChildren().isEmpty()) {
            // take the first schema element.
            SchemaElement firstSe = properties.schema.schema.getChildren().get(0);
            if (firstSe.getType() == SchemaElement.Type.STRING) {
                schemaName = firstSe.getName();
            } else {
                throw new ComponentException(ComponentsErrorCode.SCHEMA_TYPE_MISMATCH,
                        ExceptionContext.withBuilder().put("component", properties.getName()).put("expected", SchemaElement.Type.STRING)
                                .put("current", firstSe.getTitle()).build());
            }
        } else {
            throw new ComponentException(ComponentsErrorCode.SCHEMA_MISSING,
                    ExceptionContext.withBuilder().put("component", properties.getName()).build());

        }
        // get the file reader
        reader = new BufferedReader(new FileReader(properties.filename.getStringValue()));
        return advance();
    }

    @Override
    public boolean advance() throws IOException {
        LOGGER.debug("read: " + properties.filename.getStringValue());
        row = new HashMap();
        String line = reader.readLine();
        if (line == null)
           return false;
        row.put(schemaName, line);
        return true;
    }

    @Override
    public Object getCurrent() throws NoSuchElementException {
        return row;
    }

    @Override
    public Instant getCurrentTimestamp() throws NoSuchElementException {
        return null;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public Double getFractionConsumed() {
        return null;
    }

    @Override
    public BoundedSource getCurrentSource() {
        return source;
    }

    @Override
    public BoundedSource splitAtFraction(double fraction) {
        return null;
    }
}
