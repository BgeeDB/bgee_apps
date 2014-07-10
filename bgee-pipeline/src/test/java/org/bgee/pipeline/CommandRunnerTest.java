package org.bgee.pipeline;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.uberon.Uberon;
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
     * Test the method {@link CommandRunner#socketUberonStagesBetween(Uberon, int)}
     * @throws UnknownHostException
     * @throws IOException
     */
    @Test
    public void testSocketUberonStagesBetween() throws Exception {
        
        final int port = 15555;
        final String host = "127.0.0.1";
        final Uberon mockUberon = mock(Uberon.class);
        when(mockUberon.getStageIdsBetween(eq("ID1"), eq("ID3"))).thenReturn(
                Arrays.asList("ID1", "ID2", "ID3"));
        
        /**
         * An anonymous class to launch the Socket Server from another thread, 
         * to proceed with the test.
         */
        class ThreadTest extends Thread {
            public volatile Exception exceptionThrown = null;
            @Override
            public void run() {
                try {
                    CommandRunner.socketUberonStagesBetween(mockUberon, port);
                } catch (IOException e) {
                    exceptionThrown = e;
                } 
            }
        }

        ThreadTest test = new ThreadTest();
        try {
            test.start();
            //wait for this thread's turn
            int i = 0;
            while (!CommandRunner.socketServerLaunched) {
                if (i > 10) {
                    throw new AssertionError("Could not launch the SocketServer.");
                }
                Thread.sleep(500);
                i++;
            }
            //check that no exception was thrown in the second thread 
            if (test.exceptionThrown != null) {
                throw test.exceptionThrown;
            }
            
            try (Socket echoSocket = new Socket(host, port);
                    PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(echoSocket.getInputStream()));
                    ) {
                
                out.println("ID1,ID3");
                assertEquals("Incorrect value returned through socket", "ID1" + 
                   CommandRunner.SOCKET_RESPONSE_SEPARATOR + "ID2" + 
                   CommandRunner.SOCKET_RESPONSE_SEPARATOR + "ID3", 
                        in.readLine());
                out.println("quit");
            }
        } catch (InterruptedException e) {
            test.interrupt();
            Thread.currentThread().interrupt();
        } 
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
