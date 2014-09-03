package org.bgee.pipeline.uberon;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.MySQLDAOUser;
import org.bgee.pipeline.annotations.AnnotationCommon;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owltools.graph.OWLGraphWrapper;

/**
 * Class dedicated to the insertion of Uberon information into the Bgee data source.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class InsertUberon extends MySQLDAOUser {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(InsertUberon.class.getName());
    
    /**
     * Several actions can be launched from this main method, depending on the first 
     * element in {@code args}: 
     * <ul>
     * <li>If the first element in {@code args} is "insertStages", the action 
     * will be to insert a developmental stage ontology into the database, 
     * see {@link #insertStageOntologyIntoDataSource(UberonDevStage, Collection)}.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the file storing the Uberon ontology, see {@link #setPathToUberonOnt(String)}.
     *   <li>path to a file storing the Uberon taxon constraints
     *   <li>A Map<String, Set<Integer>> to potentially override taxon constraints 
     *   (recommended for developmental stages), see {@link 
     *   org.bgee.pipeline.CommandRunner#parseMapArgumentAsInteger(String)} to see 
     *   how to provided it. Can be empty.
     *   <li>Path to the file listing species used in Bgee. can be empty.
     *   </ol>
     *   Example of command line usage for this task: 
     *   {@code java -Xmx2g -jar myJar 
     *   InsertUberon insertStages dev_stage_ontology.owl taxonConstraints.tsv 
     *   HsapDv:/9606,MmusDv:/10090  
     *   bgeeSpecies.tsv
     * </ul>
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If {@code args} does not contain the proper 
     *                                  parameters or does not allow to obtain 
     *                                  correct information.
     */
    public static void main(String[] args) throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException, IllegalArgumentException {
        log.entry((Object[]) args);
        
        if (args[0].equalsIgnoreCase("insertStages")) {
            if (args.length < 4 || args.length > 5) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "4 or 5 arguments, " + args.length + " provided."));
            }
            
            UberonDevStage ub = new UberonDevStage(args[1], args[2], 
                    CommandRunner.parseMapArgumentAsInteger(args[3]));
            InsertUberon insert = new InsertUberon();
            Collection<Integer> speciesIds = new HashSet<Integer>();
            if (args.length > 4 && StringUtils.isNotBlank(args[4])) {
                speciesIds = AnnotationCommon.getTaxonIds(args[4]);
            }
            insert.insertStageOntologyIntoDataSource(ub, speciesIds);
            
        } else {
            throw log.throwing(new UnsupportedOperationException("The following action " +
                    "is not recognized: " + args[0]));
        }
        
        log.exit();
    }

    /**
     * Default constructor using default {@code MySQLDAOManager}.
     */
    public InsertUberon() {
        this(null);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public InsertUberon(MySQLDAOManager manager) {
        super(manager);
    }
    
    public void insertStageOntologyIntoDataSource(UberonDevStage uberon, 
            Collection<Integer> speciesIds) {
        log.entry(uberon, speciesIds);
        
        //no nested set model provided, need to compute it, starting from the root 
        //of the ontology. 
        Set<OWLClass> roots = uberon.getOntologyUtils().getWrapper().getOntologyRoots();
        if (roots.size() != 1) {
            throw log.throwing(new IllegalStateException("Incorrect number of roots " +
                    "in the developmental stage ontology: " + roots.size() + " - " + roots));
        }
        //we modify the taxon constraints so that only terms belonging to at least one 
        //of the requested species will be considered
        for (Set<Integer> taxa: uberon.getTaxonConstraints().values()) {
            taxa.retainAll(speciesIds);
        }
        //generate the nested set model then do the insertion
        Map<OWLClass, Map<String, Integer>> nestedSetModel = 
                uberon.generateStageNestedSetModel(roots.iterator().next());
        
        //generate the StageTOs
        Set<StageTO> stageTOs = new HashSet<StageTO>();
        OWLGraphWrapper wrapper = uberon.getOntologyUtils().getWrapper();
        for (Entry<OWLClass, Map<String, Integer>> stageEntry: nestedSetModel.entrySet()) {
            OWLClass OWLClassStage = stageEntry.getKey();
            //keep the stage only if exists in one of the requested species
            if (!uberon.existsInAtLeastOneSpecies(OWLClassStage, speciesIds)) {
                continue;
            }
            
            //check that we always have an ID and a name
            String id = wrapper.getIdentifier(OWLClassStage);
            if (StringUtils.isBlank(id)) {
                throw log.throwing(new IllegalStateException("No OBO-like ID retrieved for " + 
                                      OWLClassStage));
            }
            String name = wrapper.getLabel(OWLClassStage);
            if (StringUtils.isBlank(name)) {
                throw log.throwing(new IllegalStateException("No label retrieved for " + 
                                      OWLClassStage));
            }
            stageTOs.add(
                    new StageTO(id, name,  wrapper.getDef(OWLClassStage), 
                    stageEntry.getValue().get(OntologyUtils.LEFT_BOUND_KEY), 
                    stageEntry.getValue().get(OntologyUtils.RIGHT_BOUND_KEY), 
                    stageEntry.getValue().get(OntologyUtils.LEVEL_KEY), 
                    wrapper.getSubsets(OWLClassStage).contains(UberonDevStage.TOO_GRANULAR_SUBSET), 
                    id.startsWith("UBERON:")));//currently, grouping stages are simply all Uberon stages
        }
        
        //insert the stage TOs
        try {
            this.startTransaction();
            this.getStageDAO().insertStages(stageTOs);
            this.commit();
        } finally {
            this.closeDAO();
        }
        
        log.exit();
    }
}
