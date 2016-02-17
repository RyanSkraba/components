#set( $symbol_pound = '#' )
        #set( $symbol_dollar = '$' )
        #set( $symbol_escape = '\' )

package ${package};

import java.util.Map;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentRuntime;

public class ${componentName}Runtime extends ComponentRuntime {
   private static final Logger LOGGER = LoggerFactory.getLogger(${componentName}Runtime.class);

   ${componentName}Properties _properties;
   BufferedReader _reader;

    @Override
    public void inputBegin(ComponentProperties props) throws Exception {
        _properties = (${componentName}Properties)props;
        LOGGER.debug("open: " + _properties.filename.getStringValue());
        _reader = new BufferedReader(new FileReader(_properties.filename.getStringValue()));
    }

    @Override
    public Map<String, Object> inputRow() throws Exception {
        LOGGER.debug("read: " + _properties.filename.getStringValue());
        Map<String, Object> row = new HashMap();
        String line = _reader.readLine();
        if (line == null)
           return null;
        row.put("line", line);
        return row;
    }

    @Override
    public void inputEnd() throws Exception {
        LOGGER.debug("end: " + _properties.filename.getStringValue());
        _reader.close();
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
