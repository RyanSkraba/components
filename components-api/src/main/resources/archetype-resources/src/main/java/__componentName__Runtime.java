#set( $symbol_pound = '#' )
        #set( $symbol_dollar = '$' )
        #set( $symbol_escape = '\' )

        package ${package};

        import java.util.Map;
        import java.util.HashMap;
        import java.io.FileReader;
        import java.io.BufferedReader;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentRuntime;

public class ${componentName}Runtime extends ComponentRuntime {

        ${componentName}Properties _properties;
   BufferedReader _reader;

    @Override
    public void inputBegin(ComponentProperties props) throws Exception {
        _properties = (${componentName}Properties)props;
        System.out.println("open: " + _properties.filename.getStringValue());
        _reader = new BufferedReader(new FileReader(_properties.filename.getStringValue()));
    }

    @Override
    public Map<String, Object> inputRow() throws Exception {
        System.out.println("read: " + _properties.filename.getStringValue());
        Map<String, Object> row = new HashMap();
        String line = _reader.readLine();
        if (line == null)
           return null;
        row.put("line", line);
        return row;
    }

    @Override
    public void inputEnd() throws Exception {
        System.out.println("end: " + _properties.filename.getStringValue());
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
