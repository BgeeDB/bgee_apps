package org.bgee.pipeline.species;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.MySQLDAOUser;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import owltools.graph.OWLGraphManipulator;
import owltools.graph.OWLGraphWrapper;
import owltools.io.ParserWrapper;
import owltools.sim.SimEngine;

/**
 * Class responsible for inserting species and related NCBI taxonomy into 
 * the Bgee database. This class uses a file containing the IDs of the species 
 * to insert into Bgee, and a simplified version of the NCBI taxonomy stored 
 * as an ontology file. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class InsertTaxa extends MySQLDAOUser {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(InsertTaxa.class.getName());
    
    /**
     * A {@code String} that is the prefix to add to the NCBI taxonomy IDs 
     * (that are {@code Integer}s) to obtain IDs used in the taxonomy ontology. 
     * For instance, if a taxon has the ID {@code 9606} on the NCBI taxonomy website, 
     * it will have the ID {@code NCBITaxon:9606} in the ontology file.
     */
    private static final String ONTOLOGYIDPREFIX = "NCBITaxon:";
    /**
     * Default constructor. 
     */
    public InsertTaxa() {
        super();
    }
    
    /**
     * Main method to trigger the insertion of species and taxonomy into the Bgee 
     * database. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the tsv files containing the ID of the species used in Bgee, 
     * corresponding to the NCBI taxonomy ID (e.g., 9606 for human). This is the file 
     * to modify to add/remove a species. The first line is a header line, 
     * and the second column is present only for human readability. Only the 
     * first column is used by the pipeline. 
     * <li>path to the file storing the NCBI taxonomy as an ontology. The taxonomy 
     * could be the complete NCBI taxonomy, and the ontology stored in OWL or OBO; 
     * the taxonomy that we generate is a lighter version stored in OBO format 
     * (see {@link GenerateTaxonOntology}).
     * </ol>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If {@code args} does not contain the proper 
     *                                  parameters.
     */
    public static void main(String[] args) {
        log.entry((Object[]) args);
        int expectedArgLength = 2;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                    "provided, expected " + expectedArgLength + " arguments, " + args.length + 
                    " provided."));
        }
        
        log.exit();
    }
    
    public void insertSpeciesAndTaxa(String speciesFile, String ncbiOntFile) 
            throws FileNotFoundException, IOException, OWLOntologyCreationException, 
            OBOFormatParserException {
        log.entry(speciesFile, ncbiOntFile);
        
        //get the NCBI IDs of the species to include in Bgee
        Set<Integer> speciesIds = this.getSpeciesIds(speciesFile);
        
        //load the NCBI taxonomy ontology
        OWLOntology taxOnt = this.getTaxOntology(ncbiOntFile);
        //wrapper the ontology into a OWLGraphWrapper to perform the following actions
        OWLGraphWrapper wrapper = new OWLGraphWrapper(taxOnt);
        //filter the ontology to get a light taxonomy with only relevant taxa and species.
        this.filterTaxOntology(wrapper, speciesIds);
        
        //now we start a transaction to insert taxa and species in the Bgee data source.
        //note that we do not need to call rollback if an error occurs, calling 
        //closeDAO will rollback any ongoing transaction
        try {
            this.startTransaction();
            //insert the taxa
            this.insertTaxa(wrapper);
            //insert the species
            this.insertSpecies(wrapper);
            this.commit();
        } finally {
            this.closeDAO();
        }
        
        log.exit();
    }
    
    /**
     * Get the IDs of the species used in Bgee from the TSV file named {@code speciesFile}.
     * The IDs are {@code Integer} corresponding to the NCBI taxonomy ID (e.g., 9606 
     * for human). The first line should be a header line, and the second column 
     * be present only for human readability. Only the first column is used 
     * by the pipeline.
     * 
     * @param speciesFile   A {@code String} that is the path to the TSV file 
     *                      containing the list of species used in Bgee.
     * @return              A {@code Set} of {Integer}s that are the NCBI IDs 
     *                      of the species present in {@code speciesFile}.
     * @throws FileNotFoundException    If {@code speciesFile} could not be found.
     * @throws IOException              If {@code speciesFile} could not be read.
     * @throws IllegalArgumentException If the file located at {@code speciesFile} 
     *                                  did not allow to obtain any valid species ID.
     */
    private Set<Integer> getSpeciesIds(String speciesFile) throws IllegalArgumentException, 
        FileNotFoundException, IOException {
        log.entry(speciesFile);
        Set<Integer> speciesIds = new HashSet<Integer>();
        
        try (ICsvMapReader mapReader = new CsvMapReader(
                new FileReader(speciesFile), CsvPreference.TAB_PREFERENCE)) {
            mapReader.getHeader(true); 
            //define our own headers, because only the first column is used
            String columnName = "speciesId";
            String[] headers = new String[] {columnName, null};
            //constrain the first column to be not-null, unique, and parse it to Integer.
            //we don't care about the second column
            CellProcessor[] processors = new CellProcessor[] {
                    new NotNull(new UniqueHashCode(new ParseInt())), null};
            Map<String, Object> speciesMap;
            while( (speciesMap = mapReader.read(headers, processors)) != null ) {
                    speciesIds.add((Integer) speciesMap.get(columnName));
            }
        }
        
        if (speciesIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The species file " +
                    speciesFile + " did not contain any valid species ID"));
        }
        
        return log.exit(speciesIds);
    }
    
    /**
     * Loads and returns the {@code OWLOntology} loaded from the file located at 
     * {@code ncbiOntFile}.
     * 
     * @param ncbiOntFile   A {@code String} that is the name of the local NCBI 
     *                      ontology file.
     * @return              The {@code OWLOntology} loaded from {@code ncbiOntFile}.
     * @throws OWLOntologyCreationException If an error occurred while loading 
     *                                      the ontology.
     * @throws OBOFormatParserException     If the file could not be parsed correctly.
     * @throws IOException                  If the file could not be read.
     */
    private OWLOntology getTaxOntology(String ncbiOntFile) 
            throws OWLOntologyCreationException, OBOFormatParserException, IOException {
        log.entry(ncbiOntFile);
        ParserWrapper parserWrapper = new ParserWrapper();
        return log.exit(parserWrapper.parse(ncbiOntFile));
    }
    
    /**
     * Modifies the {@code OWLOntology} wrapped into {@code taxOntWrapper}, 
     * so that it will only include the subgraphs to the root containing 
     * the requested species, and so that the requested species will be leaves 
     * of the ontology. The requested species are specified by providing 
     * their NCBI taxonomy IDs using the {@code ncbiSpeciesIds} argument 
     * (for instance, should contain {@code 9606} to include human, as it is its ID 
     * on the NCBI taxonomy website).
     * 
     * @param taxOntWrapper     The {@code OWLGraphWrapper} wrapping the 
     *                          {@code OWLOntology} to modify.
     * @param ncbiSpeciesIds    A {@code Set} of {@code Integer}s that are the NCBI IDs 
     *                          of the species used to filter the ontology.
     */
    private void filterTaxOntology(OWLGraphWrapper taxOntWrapper, 
            Set<Integer> ncbiSpeciesIds) {
        log.entry(taxOntWrapper, ncbiSpeciesIds);
        
        //transform the NCBI IDs into ontology IDs
        Set<String> speciesIds = new HashSet<String>();
        for (Integer id: ncbiSpeciesIds) {
            speciesIds.add(this.getTaxOntologyId(id)); 
        }
        //use an OWLGraphManipulator to keep only subgraphs that include 
        //the requested species
        OWLGraphManipulator manipulator = new OWLGraphManipulator(taxOntWrapper);
        manipulator.filterSubgraphs(speciesIds);
        //and remove terms descendants of the requested species, so that they will be 
        //leaves of the ontology. To make use of the manipulator, we will consider 
        //each direct descendant of a requested species as the root of a subgraph 
        //to remove.
        Set<String> subgraphRootsToDel = new HashSet<String>();
        for (String speciesId: speciesIds) {
            for (OWLClass classToDel: taxOntWrapper.getOWLClassDirectDescendants(
                    taxOntWrapper.getOWLClassByIdentifier(speciesId))) {
                subgraphRootsToDel.add(taxOntWrapper.getIdentifier(classToDel));
            }
        }
        manipulator.removeSubgraphs(subgraphRootsToDel, false);
        
        log.exit();
    }
    
    private void insertTaxa(OWLGraphWrapper taxOntWrapper) 
            throws IllegalStateException {
        log.entry(taxOntWrapper);
        
        //get the least common ancestors
        Set<Integer> lcaIds = this.getLeastCommonAncestors(taxOntWrapper);
        
        //Now we generate the TaxonTOs to provide to the TaxonDAO to perform insertions.
        //As the TaxonTOs are immutable, we store the attributes we generate 
        //in a Map, before actually instantiating the TaxonTOs.
        Map<OWLClass, Map<String, Integer>> nestedModelParams = 
                new HashMap<OWLClass, Map<String, Integer>>();
        //keys in the OWLClass Map
        String leftBoundKey = "left";
        String rightBoundKey = "right";
        String levelKey = "level";
        
        //we want to order the taxon based on their scientific name, so we create 
        //a Comparator. This comparator needs the OWLGraphWrapper, so we make 
        //a final variable for taxOntWrapper
        final OWLGraphWrapper wrapper = taxOntWrapper;
        Comparator comparator = new Comparator<OWLClass>() {
            @Override
            public int compare(OWLClass o1, OWLClass o2) {
                return wrapper.getLabel(o1).compareTo(wrapper.getLabel(o2));
            }
        };
        
        //now we walk the ontology, starting from the root, that should be unique.
        //we use a Deque to store OWLClasses to walk through 
        Set<OWLClass> roots = taxOntWrapper.getOntologyRoots();
        if (roots.size() != 1) {
            throw log.throwing(new IllegalStateException("Incorrect number of roots " +
            		"in the taxonomy ontology"));
        }
        Deque<OWLClass> deque = new ArrayDeque<OWLClass>();
        deque.addFirst(roots.iterator().next());
        OWLClass classInspected;
        int leftBound = 0;
        int rightBound = 0;
        int level = 0;
        while ((classInspected = deque.pollFirst()) != null) {
            leftBound++;
            
            Map<String, Integer> currentClassParams = nestedModelParams.get(classInspected);
            if (currentClassParams == null) {
                currentClassParams = new HashMap<String, Integer>();
                currentClassParams.put(leftBoundKey, leftBound);
                nestedModelParams.put(classInspected, currentClassParams);
            }
            
            Set<OWLClass> directDescendants = 
                    taxOntWrapper.getOWLClassDirectDescendants(classInspected);
        }
        
        
        log.exit();
    }
    
    
    
    /**
     * Get the NCBI IDs (which are integers with no prefix) of the least common 
     * ancestors of each pair of leaves in the {@code OWLOntology} wrapped into 
     * {@code taxOntWrapper}.
     * <p>
     * The rational is that the provided {@code OWLOntology} should contain 
     * all species used in Bgee as leaves, and that we want to identify the most 
     * important taxa, the taxa "branching" the Bgee species together. So we want 
     * to retrieve the least common ancestor of each pair of species.
     * 
     * @param taxOntWrapper     The {@code OWLGraphWrapper} wrapping the 
     *                          {@code OWLOntology} to modify.
     * @return          A {@code Set} of {@code Integer}s that are the NCBI IDs 
     *                  of the least common ancestors of the leaves of {@code taxOnt}.
     *                  
     * @throws IllegalStateException        If the ontology did not allow 
     *                                      to retrieve proper least common ancestors.
     */
    private Set<Integer> getLeastCommonAncestors(OWLGraphWrapper taxOntWrapper) 
            throws IllegalStateException {
        log.entry(taxOntWrapper);
        
        Set<Integer> lcaIds = new HashSet<Integer>();

        SimEngine se = new SimEngine(taxOntWrapper);
        //we want to find the least common ancestor of all possible pairs 
        //of leaves in the ontology
        Set<OWLClass> leaves = taxOntWrapper.getOntologyLeaves();
        for (OWLClass leave1: leaves) {
            for (OWLClass leave2: leaves) {
                if (leave1.equals(leave2)) {
                    continue;
                }
                Set<OWLObject> lcas = se.getLeastCommonSubsumers(leave1, leave2);
                //we should have only one least common ancestor, and it should be 
                //an OWLClass
                if (lcas.size() != 1) {
                    throw log.throwing(new IllegalStateException("Some taxa in " +
                    		"the taxonomy ontology have more than one parent."));
                }
                OWLObject lca = lcas.iterator().next();
                if (!(lca instanceof OWLClass)) {
                    throw log.throwing(new IllegalStateException("Some taxa in " +
                            "the taxonomy ontology have incorrect relations."));
                }
                //OK, valid LCA, add its NCBI ID to the list
                lcaIds.add(this.getTaxNcbiId(taxOntWrapper.getIdentifier(lca)));
            }
        }
        
        if (lcaIds.isEmpty()) {
            throw log.throwing(new IllegalStateException("The taxonomy ontology " +
            		"did not allow to identify any least common ancestors of species used."));
        }
        
        return log.exit(lcaIds);
        
    }
    
    /**
     * Transforms a NCBI ID (which are integers, for instance, {@code 9606} for human) 
     * into the equivalent ID used in the generated taxonomy ontology (which are 
     * strings with a prefix).
     * 
     * @param ncbiId    An {@code int} that is the ID of a taxon or species as used 
     *                  on the NCBI website.
     * @return          A {@code String} that is the corresponding ID as used in 
     *                  the taxonomy ontology.
     */
    private String getTaxOntologyId(int ncbiId) {
        return ONTOLOGYIDPREFIX + ncbiId;
    }
    /**
     * Transform the ID of a taxonomy term in the generated ontology (which are strings 
     * with a given prefix) into the equivalent ID used on the NCBI website (which 
     * are integers with no prefix).
     * 
     * @param ontologyTermId    A {@code String} that is the ID of a term in 
     *                          the taxonomy ontology.
     * @return                  An {@code int} that is the corresponding ID 
     *                          on the NCBI website. 
     */
    private int getTaxNcbiId(String ontologyTermId) {
        return Integer.parseInt(ontologyTermId.substring(ONTOLOGYIDPREFIX.length()));
    }
}
