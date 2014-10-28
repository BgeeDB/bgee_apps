package org.bgee.pipeline.uberon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.CommandRunner;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * An utility class allowing to query Uberon-related methods over sockets. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class UberonSocketTool {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(UberonSocketTool.class.getName());
    
    private enum SocketAction {
        STAGES_BETWEEN, ID_MAPPINGS;
    }
    
    /**
     * Several actions can be launched from this main method, depending on the first 
     * element in {@code args}: 
     * <ul>
     * <li>If the first element in {@code args} is "stageRange", the action 
     * will be to launch a {@code ServerSocket} to perform stage range queries, 
     * see {@link UberonSocketTool#UberonSocketTool(UberonDevStage, int, ServerSocket)}.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the file storing the Uberon developmental ontology.
     *   <li>path to the taxon constraints file
     *   <li>A {@code Map} where keys are {@code String}s corresponding to OBO-like IDs 
     *   of {@code OWLClass}es, the associated value being a {@code Set} of {Integer}s 
     *   allowing to override taxon constraints (see 
     *   {@link org.bgee.pipeline.uberon.UberonDevStage#UberonDevStage(String, String, Map)})
     *   <li>the ID of the species to consider to retrieve stage ranges.
     *   <li>the port to connect the {@code ServerSocket} to.
     *   </ol>
     * <li>If the first element in {@code args} is "idMapping", the action 
     * will be to launch a {@code ServerSocket} to perform ID mapping queries, 
     * see {@link UberonSocketTool#UberonSocketTool(OntologyUtils, ServerSocket)}.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the file storing the Uberon ontology.
     *   <li>the port to connect the {@code ServerSocket} to.
     *   </ol>
     * </ul>
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If {@code args} does not contain the proper 
     *                                  parameters or does not allow to obtain 
     *                                  correct information.
     */
    public static void main(String[] args) throws NumberFormatException, 
        OWLOntologyCreationException, OBOFormatParserException, IOException {
        log.entry((Object[]) args);
        
        if (args[0].equalsIgnoreCase("stageRange")) {
            UberonSocketTool tool = new UberonSocketTool(new UberonDevStage(args[1], args[2], 
                    CommandRunner.parseMapArgumentAsInteger(args[3])), 
                    Integer.parseInt(args[4]), 
                    new ServerSocket(Integer.parseInt(args[5])));
            tool.startListening();
        } else if (args[0].equalsIgnoreCase("idMapping")) {
            UberonSocketTool tool = new UberonSocketTool(new Uberon(args[1]), 
                    new ServerSocket(Integer.parseInt(args[2])));
            tool.startListening();
        } else {
            throw log.throwing(new UnsupportedOperationException("The following action " +
                    "is not recognized: " + args[0]));
        }
        
        log.exit();
    }
    
    /**
     * An {@code UberonDevStage} used to answer queries relative to stage ranges. 
     * @see #socketStagesBetween()
     */
    private final UberonDevStage uberonDevStage;
    /**
     * An {@code UberonCommon} used to answer queries relative to ID mappings.
     * @see #socketIdMappings()
     */
    private final UberonCommon uberonCommon;
    /**
     * An {@code int} that is the ID of the species which stage range queries are considering.
     * @see #uberonDevStage
     * @see #socketStagesBetween()
     */
    private final int devStageSpeciesId;
    
    /**
     * The {@code ServerSocket} used to communicate: to acquire queries and to return results.
     */
    private final ServerSocket serverSocket;
    /**
     * The {@code SocketAction} defining which queries will be performed. 
     */
    private final SocketAction action;
    
    
    /**
     * Default constructor private on purpose, parameters must always be provided 
     * at instantiation.
     */
    @SuppressWarnings("unused")
    private UberonSocketTool() {
        this(null, 0, null, null);
    }
    
    /**
     * Constructor to use a {@code ServerSocket} to perform stage range queries, as with 
     * the method {@link UberonDevStage#getStageIdsBetween(String, String, int)}. 
     * This method is written so that external applications can query for stage ranges, 
     * without needing to reload the ontology for each query. Using sockets, 
     * the ontology can be kept loaded, answering several stage range queries. 
     * The method used to obtain stage ranges is 
     * {@link org.bgee.pipeline.uberon.Uberon#getStageIdsBetween(String, String)}.
     * 
     * @param uberon        An {@code UberonDevStage} used to perform stage range queries, 
     *                      see {@link UberonDevStage#getStageIdsBetween(String, String, int)}.
     * @param speciesId     An {@code int} that is the ID of the species to consider to retrieve 
     *                      stage ranges, see {@link UberonDevStage#getStageIdsBetween(String, String, int)}.
     * @param serverSocket  The {@code ServerSocket} to use to communicate.
     */
    public UberonSocketTool(UberonDevStage uberon, int speciesId, ServerSocket serverSocket) {
        this(uberon, speciesId, serverSocket, SocketAction.STAGES_BETWEEN);
    }
    /**
     * Constructor to use a {@code ServerSocket} to perform ID mapping queries, as with 
     * the method {@link UberonCommon#getOWLClass(String)}. 
     * This method is written so that external applications can query for mappings, 
     * without needing to reload the ontology for each query. Using sockets, 
     * the ontology can be kept loaded, answering several queries. 
     * The method used to obtain mappings is {@link OntologyUtils#getOWLClass(String)}.
     * @param UberonCommon  The {@code UberonCommon} used to perform ID mappings.
     * @param serverSocket  The {@code ServerSocket} to use to communicate.
     */
    public UberonSocketTool(UberonCommon uberon, ServerSocket serverSocket) {
        this(uberon, 0, serverSocket, SocketAction.ID_MAPPINGS);
    }
    
    /**
     * Private constructor accepting all possible constructor arguments, even if not supposed 
     * to be used together. This constructor also launches the {@code ServerSocket} 
     * and parameterize it depending on the requested {@code action}.
     * 
     * @param uberon        An {@code UberonCommon} used to perform stage range queries, 
     *                      or ID mapping queries.
     * @param speciesId     An {@code int} that is the ID of the species to consider to retrieve 
     *                      stage ranges, see {@link UberonDevStage#getStageIdsBetween(String, String, int)}.
     * @param serverSocket  The {@code ServerSocket} to use to communicate.
     * @param action        The {@code SocketAction} defining which query to perform.
     */
    private UberonSocketTool(UberonCommon uberon, int speciesId, 
            ServerSocket serverSocket, SocketAction action) {
        this.uberonCommon = uberon;
        if (uberon instanceof UberonDevStage) {
            this.uberonDevStage = (UberonDevStage) uberon;
        } else {
            this.uberonDevStage = null;
        }
        this.devStageSpeciesId = speciesId;
        this.serverSocket = serverSocket;
        this.action = action;
    }
    
    /**
     * Start listening to clients through the {@code ServerSocket} provided at instantiation.
     * @throws IOException  If an error occurred while reading input from client 
     *                      or while returning response. 
     */
    public void startListening() throws IOException {
        this.genericSocket(this.action);
    }
    
    /**
     * This method uses the {@code #serverSocket} to receive queries that will be delegated 
     * to {@link #stageRangeQuery(String)} if {@code action} is equal to 
     * {@link SocketAction STAGES_BETWEEN}, or to {@link #idMappingQuery(String)} 
     * if {@code action} is equal to {@link SocketAction STAGES_BETWEEN}. The value returned 
     * by one of these methods is then returned using the server socket. 
     * <p>
     * Note that for {@link #stageRangeQuery(String)} to work properly, a loaded {@code UberonDevStage} 
     * and a {@code speciesId} must have been provided at instantiation. 
     * For {@link #idMappingQuery(String)} to work properly, a loaded {@code OntologyUtils} 
     * must have been provided at instantiation. 
     * 
     * @param action    A {@code SocketAction} defining the query to execute.
     * @throws IOException  If an error occurred while reading input from client 
     *                      or while returning response. 
     */
    private void genericSocket(SocketAction action) throws IOException {
        log.entry(action);
        
        log.debug("Trying to acquire client socket from ServerSocket {}...", 
                this.serverSocket);
        try (Socket clientSocket = this.serverSocket.accept();
                PrintWriter out =
                        new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));) {

            log.debug("Client socket acquired.");
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                try {
                    log.debug("Receiving query: " + inputLine);
                    inputLine = inputLine.trim();

                    if (inputLine.equals("exit") || inputLine.equals("logout") || 
                            inputLine.equals("quit") || inputLine.equals("bye")) {
                        out.println("Bye.");
                        log.debug("Exiting.");
                        break;
                    }

                    String output = "";
                    if (action.equals(SocketAction.STAGES_BETWEEN)) {
                        output = this.stageRangeQuery(inputLine);
                    } else if (action.equals(SocketAction.ID_MAPPINGS)) {
                        output = this.idMappingQuery(inputLine);
                    } 

                    log.debug("Sending response: {}", output);
                    out.println(output);
                } catch (Exception e) {
                    log.catching(e);
                    out.println(e);
                }
            }
        } finally {
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
        }
        
        log.exit();
    }
    
    /**
     * Extract from {@code input} a start stage ID and a end stage ID and perform 
     * a stage range query using {@link UberonDevStage#getStageIdsBetween(String, String, int)}.
     * The {@code UberonDevStage} object should be provided at instantiation. 
     * The IDs in {@code input} are separated by {@link CommandRunner#LIST_SEPARATOR}. 
     * The value returned by {@code UberonDevStage} is returned as a {@code String}. 
     * @param input A {@code String} from which to extract a OBO-like start stage ID and 
     *              end stage ID.
     * @return      A {@code String} containing the response to the query, 
     *              an empty {@code String} if no response was found. 
     */
    private String stageRangeQuery(String input) {
        log.entry(input);
        
        String output = "";
        List<String> params = CommandRunner.parseListArgument(input);
        if (params.size() != 2) {
            throw log.throwing(new IllegalArgumentException("Incorrect number " +
            		"of stage IDs provided."));
        } else {
            log.debug("Start stage retrieved: {} - End stage retrieved: {}", 
                    params.get(0), params.get(1));

            List<String> stageIds = this.uberonDevStage.getStageIdsBetween(params.get(0), 
                    params.get(1), this.devStageSpeciesId);
            for (String stageId: stageIds) {
                if (StringUtils.isNotBlank(output)) {
                    output += CommandRunner.SOCKET_RESPONSE_SEPARATOR;
                }
                output += stageId;
            }

            if (StringUtils.isBlank(output)) {
                throw log.throwing(new IllegalArgumentException("No results for provided " +
                		"start and end stages (" + params.get(0) + " - " + params.get(1) + ")"));
            }
        }
        
        return log.exit(output);
    }
    
    /**
     * Extract from {@code input} the ID provided and try to retrieve the Uberon ID 
     * to actually use using {@link OntologyUtils#getOWLClasses(String, boolean)}.
     * The {@code OntologyUtils} object should be provided at instantiation. 
     * The OBO-like ID of the {@code OWLClass} returned by {@code OntologyUtils} is returned 
     * as a {@code String}. 
     * @param input A {@code String} from which to extract an ID to map it to Uberon.
     * @return      A {@code String} containing the OBO-like ID of the corresponding 
     *              {@code OWLClass}, an empty {@code String} if none could be found. 
     */
    private String idMappingQuery(String input) {
        log.entry(input);
        
        Set<OWLClass> classes = this.uberonCommon.getOWLClasses(input, false);
        this.uberonCommon.getOntologyUtils().retainLeafClasses(
                classes, this.uberonCommon.getOntologyUtils().getGenericPartOfProps());
        
        if (classes.size() == 1) {
            return log.exit(this.uberonCommon.getOntologyUtils().getWrapper().getIdentifier(
                    classes.iterator().next()));
        } 
        if (log.isWarnEnabled()) {
            if (classes.isEmpty()) {
                log.warn("Could not find any OWLClass corresponding to: {}", input);
            } else {
                log.warn("ID {} mapped to more than one class, cannot choose: {}", 
                        input, classes);
            }
        }
        
        return log.exit("");
    }
}
