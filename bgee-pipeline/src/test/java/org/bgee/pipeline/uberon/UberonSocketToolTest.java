package org.bgee.pipeline.uberon;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClass;

import owltools.graph.OWLGraphWrapper;

public class UberonSocketToolTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(UberonSocketToolTest.class.getName());
    /**
     * Default Constructor. 
     */
    public UberonSocketToolTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the method {@link UberonSocketTool#UberonSocketTool(UberonDevStage, int, ServerSocket)}
     * @throws UnknownHostException
     * @throws IOException
     */
    @Test
    public void testSocketUberonStagesBetween() throws Exception {
        
        final int port = 15556;
        final String host = "127.0.0.1";
        final UberonDevStage mockUberon = mock(UberonDevStage.class);
        when(mockUberon.getStageIdsBetween(eq("ID1"), eq("ID3"), eq(0))).thenReturn(
                Arrays.asList("ID1", "ID2", "ID3"));
        
        /**
         * An anonymous class to launch the Socket Server from another thread, 
         * to proceed with the test.
         */
        class ThreadTest extends Thread {
            public volatile Exception exceptionThrown = null;
            public volatile UberonSocketTool socketTool = null;
            @Override
            public void run() {
                ServerSocket server = null;
                try {
                    getLogger().debug("Trying to launch ServerSocket...");
                    server = new ServerSocket(port);
                    getLogger().debug("ServerSocket launched: {}", server);
                    getLogger().debug("Trying to instantiate UberonSocketTool...");
                    UberonSocketTool tool = new UberonSocketTool(mockUberon, 0, server);
                    this.socketTool = tool; //to be sure it is initialized before being set
                    this.socketTool.startListening();
                    getLogger().debug("ServerSocket launched: {}", this.socketTool);
                } catch (IOException e) {
                    exceptionThrown = e;
                } finally {
                    if (server != null) {
                        try {
                            server.close();
                        } catch (IOException e) {
                            this.exceptionThrown = e;
                        }
                    }
                }
            }
        }

        ThreadTest test = new ThreadTest();
        try {
            getLogger().debug("Launching second thread");
            test.start();
            //wait for this thread's turn
            int i = 0;
            while (test.socketTool == null) {
                if (i > 10) {
                    if (test.exceptionThrown != null) {
                        throw test.exceptionThrown;
                    }
                    throw new AssertionError("Could not launch the SocketServer.");
                }
                Thread.sleep(500);
                i++;
            }
            getLogger().debug("Return from launching second thread");
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
     * Test the method {@link UberonSocketTool#UberonSocketTool(OntologyUtils, ServerSocket)}
     * @throws UnknownHostException
     * @throws IOException
     */
    @Test
    public void testSocketUberonIdMappings() throws Exception {
        
        final int port = 15556;
        final String host = "127.0.0.1";
        final OntologyUtils mockUtils = mock(OntologyUtils.class);
        OWLClass mockClass = mock(OWLClass.class);
        when(mockUtils.getOWLClass(eq("ID1"))).thenReturn(mockClass);
        OWLGraphWrapper mockWrapper = mock(OWLGraphWrapper.class);
        when(mockUtils.getWrapper()).thenReturn(mockWrapper);
        when(mockWrapper.getIdentifier(eq(mockClass))).thenReturn("ID:2");
        
        /**
         * An anonymous class to launch the Socket Server from another thread, 
         * to proceed with the test.
         */
        class ThreadTest extends Thread {
            public volatile Exception exceptionThrown = null;
            public volatile UberonSocketTool socketTool = null;
            @Override
            public void run() {
                ServerSocket server = null;
                try {
                    getLogger().debug("Trying to launch ServerSocket...");
                    server = new ServerSocket(port);
                    getLogger().debug("ServerSocket launched: {}", server);
                    getLogger().debug("Trying to instantiate UberonSocketTool...");
                    UberonSocketTool tool = new UberonSocketTool(mockUtils, server);
                    this.socketTool = tool; //to be sure it is initialized before being set
                    this.socketTool.startListening();
                    getLogger().debug("ServerSocket launched: {}", this.socketTool);
                } catch (IOException e) {
                    exceptionThrown = e;
                } finally {
                    if (server != null) {
                        try {
                            server.close();
                        } catch (IOException e) {
                            this.exceptionThrown = e;
                        }
                    }
                }
            }
        }

        ThreadTest test = new ThreadTest();
        try {
            getLogger().debug("Launching second thread");
            test.start();
            //wait for this thread's turn
            int i = 0;
            while (test.socketTool == null) {
                if (i > 10) {
                    if (test.exceptionThrown != null) {
                        throw test.exceptionThrown;
                    }
                    throw new AssertionError("Could not launch the SocketServer.");
                }
                Thread.sleep(500);
                i++;
            }
            getLogger().debug("Return from launching second thread");
            //check that no exception was thrown in the second thread 
            if (test.exceptionThrown != null) {
                throw test.exceptionThrown;
            }
            
            try (Socket echoSocket = new Socket(host, port);
                    PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(echoSocket.getInputStream()));
                    ) {
                
                out.println("ID1");
                assertEquals("Incorrect value returned through socket", "ID:2", 
                        in.readLine());
                out.println("quit");
            }
        } catch (InterruptedException e) {
            test.interrupt();
            Thread.currentThread().interrupt();
        } 
    }
}
