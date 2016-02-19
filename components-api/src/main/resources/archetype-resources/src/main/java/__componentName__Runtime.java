#set( $symbol_pound = '#' )
        #set( $symbol_dollar = '$' )
        #set( $symbol_escape = '\' )

package ${package};

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentRuntime;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.schema.SchemaElement;
import org.talend.daikon.schema.SchemaElement.Type;

public class ${componentName}Runtime extends ComponentRuntime {
    private static final Logger LOGGER = LoggerFactory.getLogger(${componentName}Runtime.class);

    private ${componentName}Properties properties;
    private BufferedReader reader;
    private String schemaName;


    @Override
    public void inputBegin(ComponentProperties props) throws Exception {
        properties = (${componentName}Properties) props;
        LOGGER.debug("open: " + properties.filename.getStringValue());
        // get first schema element
        if (!properties.schema.schema.getChildren().isEmpty()) {
            SchemaElement firstSe = properties.schema.schema.getChildren().get(0);// take the first scheme element.
            if (firstSe.getType() == Type.STRING) {
                schemaName = firstSe.getName();
            } else {
                throw new ComponentException(ComponentsErrorCode.SCHEMA_TYPE_MISMATCH,
                        ExceptionContext.withBuilder().put("component", props.getName()).put("expected", Type.STRING)
                                .put("current", firstSe.getTitle()).build());

            }
        } else {
            throw new ComponentException(ComponentsErrorCode.SCHEMA_MISSING,
                    ExceptionContext.withBuilder().put("component", props.getName()).build());

        }
        // get the file reader
        reader = new BufferedReader(new FileReader(properties.filename.getStringValue()));
    }

    @Override
    public Map<String, Object> inputRow() throws Exception {
        LOGGER.debug("read: " + properties.filename.getStringValue());
        Map<String, Object> row = new HashMap();
        String line = reader.readLine();
        if (line == null)
           return null;
        row.put(schemaName, line);
        return row;
    }

    @Override
    public void inputEnd() throws Exception {
        LOGGER.debug("end: " + properties.filename.getStringValue());
        reader.close();
    }

    @Override
    public void outputBegin(ComponentProperties props) throws Exception {
        // Nothing
    }

    @Override
    public void outputMain(Map<String, Object> row) throws Exception {
        // Nothing
    }

    @Override
    public void outputEnd() throws Exception {
        // Nothing
    }

}
