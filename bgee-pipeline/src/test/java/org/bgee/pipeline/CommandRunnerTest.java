package org.bgee.pipeline;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * Unit tests for the class {@link CommandRunner}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class CommandRunnerTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(CommandRunnerTest.class.getName());
    /**
     * Default Constructor. 
     */
    public CommandRunnerTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test method {@link CommandRunner#parseListArgument(String)}.
     */
    @Test
    public void shouldParseListArgument() {
        assertEquals("Incorrect parsing of string as list", 
                Arrays.asList("ID1", "ID2", "ID3"), 
                CommandRunner.parseListArgument(
                        "ID1" + CommandRunner.LIST_SEPARATOR + 
                        "ID2" + CommandRunner.LIST_SEPARATOR + 
                        "ID3" + CommandRunner.LIST_SEPARATOR));
        

        assertEquals("Incorrect parsing of string as list", 
                Arrays.asList("ID1"), 
                CommandRunner.parseListArgument("ID1"));
        

        assertEquals("Incorrect parsing of empty list", 
                new ArrayList<String>(), 
                CommandRunner.parseListArgument(CommandRunner.EMPTY_LIST));
    }

    /**
     * Test method {@link CommandRunner#parseMapArgument(String)}.
     */
    @Test
    public void shouldParseMapArgument() {
        Map<String, Set<String>> expectedMap = new HashMap<String, Set<String>>();
        expectedMap.put("key1", new HashSet<String>(Arrays.asList("value1", "value2")));
        expectedMap.put("key2", new HashSet<String>(Arrays.asList("value2")));
        expectedMap.put("key3", new HashSet<String>(Arrays.asList("value3")));
        assertEquals("Incorrect parsing of string as map", 
                expectedMap, 
                CommandRunner.parseMapArgument(
                        " key1 " + CommandRunner.KEY_VALUE_SEPARATOR + " value1 " + 
                            CommandRunner.LIST_SEPARATOR + 
                        "key2" + CommandRunner.KEY_VALUE_SEPARATOR + "value2" + 
                            CommandRunner.LIST_SEPARATOR + 
                        "key1" + CommandRunner.KEY_VALUE_SEPARATOR + " value1 " + 
                            CommandRunner.LIST_SEPARATOR + 
                        "key1" + CommandRunner.KEY_VALUE_SEPARATOR + "value2" + 
                            CommandRunner.LIST_SEPARATOR + 
                        "key3" + CommandRunner.KEY_VALUE_SEPARATOR + "value3" + 
                            CommandRunner.LIST_SEPARATOR));
        
        expectedMap.clear();
        expectedMap.put("key1", new HashSet<String>(Arrays.asList("value1")));
        assertEquals("Incorrect parsing of string as map", 
                expectedMap, 
                CommandRunner.parseMapArgument(
                        " key1 " + CommandRunner.KEY_VALUE_SEPARATOR + " value1 "));
    }
}
