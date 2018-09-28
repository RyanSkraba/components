package org.talend.components.common.tableaction;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TableActionTest {

    private TableAction tableAction;

    @Before
    public void init(){
        tableAction = new TableAction() {

            @Override
            public List<String> getQueries() throws Exception {
                return Lists.newArrayList();
            }
        };
    }

    @Test
    public void escape(){
        assertFalse(tableAction.isEscaped("\"hello"));
        assertTrue(tableAction.isEscaped("\"hello\""));
        assertEquals("\"hello\"", tableAction.escape("hello"));
    }

    @Test
    public void updateCaseIdentifier(){
        tableAction.getConfig().SQL_UPPERCASE_IDENTIFIER = false;
        tableAction.getConfig().SQL_LOWERCASE_IDENTIFIER = false;

        String hello = "HeLlO";
        assertEquals(hello, tableAction.updateCaseIdentifier(hello));

        tableAction.getConfig().SQL_UPPERCASE_IDENTIFIER = true;
        assertEquals("HELLO", tableAction.updateCaseIdentifier(hello));

        tableAction.getConfig().SQL_UPPERCASE_IDENTIFIER = false;
        tableAction.getConfig().SQL_LOWERCASE_IDENTIFIER = true;
        assertEquals("hello", tableAction.updateCaseIdentifier(hello));


        //Uppercase take precedence on lowercase.
        tableAction.getConfig().SQL_UPPERCASE_IDENTIFIER = true;
        tableAction.getConfig().SQL_LOWERCASE_IDENTIFIER = true;
        assertEquals("HELLO", tableAction.updateCaseIdentifier(hello));

    }

    @Test
    public void buildFullTableName(){
        assertEquals("aaaa-bbbb-cccc-dddd-eeee", tableAction.buildFullTableName(new String[]{"aaaa", "bbbb", "cccc", "dddd", "eeee"}, "-", false));
        assertEquals("\"aaaa\"-\"bbbb\"-\"cccc\"-\"dddd\"-\"eeee\"", tableAction.buildFullTableName(new String[]{"aaaa", "bbbb", "cccc", "dddd", "eeee"}, "-", true));

        tableAction.getConfig().SQL_UPPERCASE_IDENTIFIER = true;
        assertEquals("\"AAAA\"_\"BBBB\"_\"CCCC\"_\"DDDD\"_\"EEEE\"", tableAction.buildFullTableName(new String[]{"aaaa", "bbbb", "cccc", "dddd", "eeee"}, "_", true));

        assertEquals("\"AAAA\"_\"BBBB\"", tableAction.buildFullTableName(new String[]{null, "aaaa", null, null, "bbbb", null}, "_", true));
    }

    @Test
    public void setConfig(){
        TableActionConfig config1 = new TableActionConfig();
        config1.SQL_CREATE_TABLE = "config1";

        TableActionConfig config2 = new TableActionConfig();
        config2.SQL_CREATE_TABLE = "config2";

        tableAction.setConfig(config1);
        assertEquals("config1", tableAction.getConfig().SQL_CREATE_TABLE);

        tableAction.setConfig(config2);
        assertEquals("config2", tableAction.getConfig().SQL_CREATE_TABLE);
    }

}