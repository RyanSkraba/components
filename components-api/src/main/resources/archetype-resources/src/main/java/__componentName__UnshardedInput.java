#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.util.UnshardedInputIterator;

/**
 * Simplified input to build an UnshardedInputSource.
 */
public class ${componentName}UnshardedInput implements UnshardedInputIterator<String> {

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(${componentName}Definition.class);

    private final String filename;

    private BufferedReader reader = null;

    private transient String current;

    public FileInputUnshardedInput(String filename) {
        this.filename = filename;
    }

    @Override
    public void setup() throws IOException {
        LOGGER.debug("open: " + filename); //$NON-NLS-1$
        reader = new BufferedReader(new FileReader(filename));
        current = reader.readLine();
    }

    @Override
    public boolean hasNext() {
        return current != null;
    }

    @Override
    public String next() {
        try {
            String oldCurrent = current;
            current = reader.readLine();
            return oldCurrent;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        reader.close();
        LOGGER.debug("close: " + filename); //$NON-NLS-1$
    }
}
