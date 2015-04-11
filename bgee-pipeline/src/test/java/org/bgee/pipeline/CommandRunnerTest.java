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
     * Test {@link CommandRunner#parseArgument(String)}
     */
    @Test
    public void shouldParseArgument() {
        assertEquals("Incorrect argument parsing", "abc", CommandRunner.parseArgument("abc"));
        assertEquals("Incorrect argument parsing", "", CommandRunner.parseArgument(""));
        assertEquals("Incorrect argument parsing", null, CommandRunner.parseArgument(null));
        assertEquals("Incorrect argument parsing", null, CommandRunner.parseArgument(
                CommandRunner.EMPTY_ARG));
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
     * Test method {@link CommandRunner#parseListArgumentAsInt(String)}.
     */
    @Test
    public void shouldParseListArgumentAsInt() {
        assertEquals("Incorrect parsing of string as list", 
                Arrays.asList(1, 2, 3), 
                CommandRunner.parseListArgumentAsInt(
                        "1" + CommandRunner.LIST_SEPARATOR + 
                        "2" + CommandRunner.LIST_SEPARATOR + 
                        "3" + CommandRunner.LIST_SEPARATOR));
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
        expectedMap.put("key4", new HashSet<String>(Arrays.asList("value4_1", 
                "value4_2", "value4_3")));
        expectedMap.put("key5", new HashSet<String>());
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
                            CommandRunner.LIST_SEPARATOR + 
                        "key4" + CommandRunner.KEY_VALUE_SEPARATOR + "value4_1" + 
                            CommandRunner.VALUE_SEPARATOR + "value4_2" + 
                            CommandRunner.VALUE_SEPARATOR + "value4_3" + 
                            CommandRunner.LIST_SEPARATOR + 
                        "key5" + CommandRunner.KEY_VALUE_SEPARATOR + 
                            CommandRunner.EMPTY_LIST));
        
        expectedMap.clear();
        expectedMap.put("key1", new HashSet<String>(Arrays.asList("value1")));
        assertEquals("Incorrect parsing of string as map", 
                expectedMap, 
                CommandRunner.parseMapArgument(
                        " key1 " + CommandRunner.KEY_VALUE_SEPARATOR + " value1 "));
    }

    /**
     * Test method {@link CommandRunner#parseMapArgumentAsInteger(String)}.
     */
    @Test
    public void shouldParseMapArgumentAsInteger() {
        Map<String, Set<Integer>> expectedMap = new HashMap<String, Set<Integer>>();
        expectedMap.put("key1", new HashSet<Integer>(Arrays.asList(1, 2)));
        expectedMap.put("key2", new HashSet<Integer>(Arrays.asList(2)));
        expectedMap.put("key3", new HashSet<Integer>(Arrays.asList(3)));
        expectedMap.put("key4", new HashSet<Integer>(Arrays.asList(4, 5, 6)));
        assertEquals("Incorrect parsing of string as map", 
                expectedMap, 
                CommandRunner.parseMapArgumentAsInteger(
                        " key1 " + CommandRunner.KEY_VALUE_SEPARATOR + " 1 " + 
                            CommandRunner.LIST_SEPARATOR + 
                        "key2" + CommandRunner.KEY_VALUE_SEPARATOR + "2" + 
                            CommandRunner.LIST_SEPARATOR + 
                        "key1" + CommandRunner.KEY_VALUE_SEPARATOR + " 1 " + 
                            CommandRunner.LIST_SEPARATOR + 
                        "key1" + CommandRunner.KEY_VALUE_SEPARATOR + "2" + 
                            CommandRunner.LIST_SEPARATOR + 
                        "key3" + CommandRunner.KEY_VALUE_SEPARATOR + "3" + 
                            CommandRunner.LIST_SEPARATOR + 
                        "key4" + CommandRunner.KEY_VALUE_SEPARATOR + "4" + 
                            CommandRunner.VALUE_SEPARATOR + "5" + 
                            CommandRunner.VALUE_SEPARATOR + "6"));
        
        expectedMap.clear();
        expectedMap.put("key1", new HashSet<Integer>(Arrays.asList(1)));
        assertEquals("Incorrect parsing of string as map", 
                expectedMap, 
                CommandRunner.parseMapArgumentAsInteger(
                        " key1 " + CommandRunner.KEY_VALUE_SEPARATOR + " 1 "));
    }
}
