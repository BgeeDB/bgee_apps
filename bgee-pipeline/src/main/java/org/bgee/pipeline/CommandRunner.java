package org.bgee.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.annotations.AnnotationCommon;
import org.bgee.pipeline.annotations.SimilarityAnnotation;
import org.bgee.pipeline.gene.InsertGO;
import org.bgee.pipeline.ontologycommon.OntologyTools;
import org.bgee.pipeline.species.GenerateTaxonOntology;
import org.bgee.pipeline.species.InsertTaxa;
import org.bgee.pipeline.uberon.TaxonConstraints;
import org.bgee.pipeline.uberon.Uberon;

/**
 * Entry point of the Bgee pipeline. It is a really basic tool, only used to dispatch 
 * commands to the relevant classes. It does not handle complex parameters, such as 
 * {@code -option myvalue}. Only parameter values are provided to the {@code main} 
 * method, so their order does matter.
 * <p>
 * The first argument is always the name of the action to perform, that will allow 
 * this class to know to which class to dispatch the work. Following arguments 
 * are simply the arguments provided to the {@code main} method of the class 
 * which the work is dispatched to. This first action argument will be removed  
 * from the parameter list before being passed to the class doing the work.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class CommandRunner {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = 
            LogManager.getLogger(CommandRunner.class.getName());
    
    /**
     * A {@code String} that is the separator between elements of a same list, 
     * when a list needs to be provided as a single argument for a command line usage. 
     * For instance, a list of IDs to provide as a single argument would be: 
     * {@code Id1 + listSeparator + Id2 + listSeparator + ...}.
     * 
     * @see #parseListArgument(String)
     */
    public static final String LIST_SEPARATOR = ",";
    /**
     * A {@code String} that represents the character to provide an empty list, as argument 
     * of command line usage.
     * 
     * @see #parseListArgument(String)
     */
    public static final String EMPTY_LIST = "-";
    
    
    /**
     * Entry point method of the Bgee pipeline. The first element in {@code args} 
     * should be the name of the action to perform (most of the time, it is 
     * the simple name of the class that will perform the action). All following 
     * elements should be the arguments expected by the {@code main} method 
     * of the class performing the action. 
     * <p>
     * This {@code main} method does not parse {@code args} to allow the use 
     * of option names (as for instance, {@code -option myvalue}). So parameters 
     * must be provided in expected order. 
     * 
     * @param args          {@code Array} of {@code String}s containing the parameters. 
     *                      First element should be the name of the action to perform 
     *                      (usually, it is the simple name of the targeted class).
     * @throws IllegalArgumentException If {@code args} does not contain the 
     *                                  expected parameters.
     * @throws Exception                Any kind of {@code Exception} thrown by 
     *                                  the class performing the action.
     */
    public static void main(String[] args) throws IllegalArgumentException, Exception {
        log.entry((Object[]) args);
        
        if (args.length < 1) {
            throw log.throwing(new IllegalArgumentException("At least one argument " +
            		"must be provided to determine the job requested to the pipeline."));
        }
        
        //make a new String array from args with first element removed
        String[] newArgs = new String[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            newArgs[i-1] = args[i];
        }
        
        
        //now choose the class to dispatch the work
        switch(args[0]) {
        
        //---------- species and taxonomy -----------
        case "GenerateTaxonOntology": 
            GenerateTaxonOntology.main(newArgs);
            break;
        case "InsertTaxa": 
            InsertTaxa.main(newArgs);
            break;
            
        //---------- uberon -----------
        case "TaxonConstraints": 
            TaxonConstraints.main(newArgs);
            break;
        case "Uberon": 
            Uberon.main(newArgs);
            break;
            
        //---------- Similarity annotation -----------
        case "SimilarityAnnotation": 
            SimilarityAnnotation.main(newArgs);
            break;
            
        //---------- General annotations -----------
        case "AnnotationCommon": 
            AnnotationCommon.main(newArgs);
            break;
            
        //---------- Genes -----------
        case "InsertGO": 
            InsertGO.main(newArgs);
            break;
        case "OntologyTools": 
            OntologyTools.main(newArgs);
            break;
            
        default: 
            throw log.throwing(new UnsupportedOperationException("The following action " +
                    "is not recognized: " + args[0]));
        }
        
        log.exit();
    }
    
    /**
     * Split {@code listArg} based on {@link #LIST_SEPARATOR}. The resulting {@code String}s 
     * are returned as a {@code List}, in the order they were obtained from {@code listArg}.
     * This method is used when a list needs to be provided as a single argument, 
     * for a command line usage. 
     * 
     * @param listArg   A {@code String} corresponding to a list of elements separated by 
     *                  {@link #LIST_SEPARATOR}.
     * @return          A {@code List} of {@code String}s that are the result of the split 
     *                  of {@code listArg}, according to {@code LIST_SEPARATOR}.
     */
    public static List<String> parseListArgument(String listArg) {
        log.entry(listArg);
        
        List<String> resultingList = new ArrayList<String>();
        for (String arg: listArg.split(LIST_SEPARATOR)) {
            if (StringUtils.isNotBlank(arg)) {
                resultingList.add(arg);
            }
        }
        if (resultingList.size() == 1 && resultingList.get(0).equals(EMPTY_LIST)) {
            resultingList.clear();
        }
        
        return log.exit(resultingList);
    }
}
