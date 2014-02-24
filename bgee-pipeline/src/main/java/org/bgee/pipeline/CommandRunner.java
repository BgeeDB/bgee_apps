package org.bgee.pipeline;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
            
        //---------- Genes -----------
        case "InsertGO": 
            InsertGO.main(newArgs);
            break;
        case "ExtractObsoleteIds": 
            //in that case, we actually pass the real arg array: OntologyTools 
            //can perform several operations, and thus need a command as first argument. 
            OntologyTools.main(args);
            break;
            
        default: 
            throw log.throwing(new IllegalArgumentException("The first argument " +
            		"provided does not correspond to any known action."));
        }
        
        log.exit();
    }
}
