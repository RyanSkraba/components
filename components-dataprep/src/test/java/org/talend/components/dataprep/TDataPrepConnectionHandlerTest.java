package org.talend.components.dataprep;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by stavytskyi on 4/26/16.
 */
public class TDataPrepConnectionHandlerTest {

    private static DataPrepConnectionHandler connectionHandler;

    @BeforeClass
    public static void setConnectionHandler(){
//        connectionHandler = new DataPrepConnectionHandler("http://10.42.10.60:8888","maksym@dataprep.com","maksym");
    }

    @Test
    @Ignore
    public void validate() {
        connectionHandler = new DataPrepConnectionHandler("http://10.42.10.60:8888","maksym@dataprep.com","maksym",
                "read", "sldfjsl");
        assertTrue(connectionHandler.validate());
    }
}
