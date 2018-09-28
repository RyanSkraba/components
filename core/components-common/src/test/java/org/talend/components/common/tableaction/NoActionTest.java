package org.talend.components.common.tableaction;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class NoActionTest {

    @Test
    public void getQueriesTest(){
        TableAction ta = new NoAction();

        try {
            List<String> queries = ta.getQueries();
            assertEquals(0, queries.size());
        }
        catch (Exception e){
            assertTrue("No exception can be thrown by NoAction TableAction.", false);
        }

    }

}