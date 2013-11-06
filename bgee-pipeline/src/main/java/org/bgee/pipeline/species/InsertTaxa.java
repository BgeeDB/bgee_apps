package org.bgee.pipeline.species;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.species.SpeciesTO;
import org.bgee.model.dao.api.species.TaxonTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.MySQLDAOUser;
import org.bgee.pipeline.OntologyUtils;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import owltools.graph.OWLGraphManipulator;
import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;
import owltools.io.ParserWrapper;

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
     * A {@code String} defining the category of the synonym providing the common 
     * name of taxa in the taxonomy ontology. 
     * See {@code owltools.graph.OWLGraphWrapper.ISynonym}.
     */
    private static final String SYNCOMMONNAMECAT = "genbank_common_name";
    
    /**
     * A {@code OWLGraphWrapper} wrapping the NCBI taxonomy {@code OWLOntology}.
     * This attribute is set by the method {@link #loadTaxOntology(String)}, 
     * and is then used by subsequent methods called.
     */
    private OWLGraphWrapper taxOntWrapper;
    /**
     * Default constructor. 
     */
    public InsertTaxa() {
        super();
    }
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public InsertTaxa(MySQLDAOManager manager) {
        super(manager);
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
     * @throws FileNotFoundException        If some files could not be found.
     * @throws IOException                  If some files could not be used.
     * @throws OWLOntologyCreationException If an error occurred while loading 
     *                                      the NCBI taxonomy ontology.
     * @throws OBOFormatParserException     If an error occurred while loading 
     *                                      the NCBI taxonomy ontology.
     * @throws IllegalArgumentException     If the files used provided invalid information.
     * @throws DAOException                 If an error occurred while inserting 
     *                                      the data into the Bgee database.
     */
    public static void main(String[] args) throws FileNotFoundException, 
        OWLOntologyCreationException, OBOFormatParserException, IllegalArgumentException, 
        DAOException, IOException {
        log.entry((Object[]) args);
        int expectedArgLength = 2;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                    "provided, expected " + expectedArgLength + " arguments, " + args.length + 
                    " provided."));
        }
        
        InsertTaxa insert = new InsertTaxa();
        insert.insertSpeciesAndTaxa(args[0], args[1]);
        
        log.exit();
    }
    
    /**
     * Inserts species and taxa into the Bgee database. This method uses the path 
     * to the TSV file containing the ID of the species used in Bgee, corresponding to 
     * their NCBI taxonomy ID (e.g., 9606 for human). This is the file 
     * to modify to add/remove a species. The first line is a header line, 
     * and the second column is present only for human readability. Only the 
     * first column is used by the pipeline. This method also uses the path 
     * to the file storing the NCBI taxonomy as an ontology. The taxonomy 
     * could be the complete NCBI taxonomy, and the ontology stored in OWL or OBO; 
     * the taxonomy that we generate is a lighter version stored in OBO format 
     * (see {@link GenerateTaxonOntology}).
     * 
     * @param speciesFile   A {@code String} that is the path to the TSV file 
     *                      containing the ID of the species used in Bgee
     * @param ncbiOntFile   A {@code String} that is the path to the NCBI taxonomy 
     *                      ontology.
     * @throws FileNotFoundException        If some files could not be found.
     * @throws IOException                  If some files could not be used.
     * @throws OWLOntologyCreationException If an error occurred while loading 
     *                                      the NCBI taxonomy ontology.
     * @throws OBOFormatParserException     If an error occurred while loading 
     *                                      the NCBI taxonomy ontology.
     * @throws IllegalArgumentException     If the files used provided invalid information.
     * @throws DAOException                 If an error occurred while inserting 
     *                                      the data into the Bgee database.
     */
    public void insertSpeciesAndTaxa(String speciesFile, String ncbiOntFile) 
            throws FileNotFoundException, IOException, OWLOntologyCreationException, 
            OBOFormatParserException, IllegalArgumentException, DAOException {
        log.entry(speciesFile, ncbiOntFile);
        
        //catch any IllegalStateException to wrap it into a IllegalArgumentException 
        //(a IllegalStateException would be generated because the OWLOntology loaded 
        //from ncbiOntFile would be invalid, so it would be a wrong argument)
        try {
            //get the NCBI IDs of the species to include in Bgee
            Set<Integer> speciesIds = this.getSpeciesIds(speciesFile);
            
            //load the NCBI taxonomy ontology
            this.loadTaxOntology(ncbiOntFile);
            //filter the ontology to get a light taxonomy with only relevant taxa and species.
            this.filterTaxOntology(speciesIds);
            
            //now we get the SpeciesTOs to insert their information into the database;
            //they should be the leaves of the ontology
            Set<SpeciesTO> speciesTOs = this.getSpeciesTOs();
            
            //now get the TaxonTOs to insert their information into the database.
            //Be aware that calling getTaxonTOs will result in removing the species 
            //from the ontology wrapped in taxOntWrapper (species used in Bgee were 
            //the leaves of the ontology until this point, now they are removed)
            Set<TaxonTO> taxonTOs = this.getTaxonTOs();
            
            //now we start a transaction to insert taxa and species in the Bgee data source.
            //note that we do not need to call rollback if an error occurs, calling 
            //closeDAO will rollback any ongoing transaction
            try {
                this.startTransaction();
                //need to insert the taxa first, because species have a reference 
                //to their parent taxon
                this.getTaxonDAO().insertTaxa(taxonTOs);
                //insert species
                this.getSpeciesDAO().insertSpecies(speciesTOs);
                this.commit();
            } finally {
                this.closeDAO();
            }
        } catch (IllegalStateException e) {
            log.catching(e);
            throw log.throwing(new IllegalArgumentException("The OWLOntology loaded " +
            		"from the ontology file is invalid", e));
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
     * Loads the {@code OWLOntology} loaded from the file located at {@code ncbiOntFile}, 
     * wraps it into an {@code OWLGraphWrapper}, and sets the attribute 
     * {@link #taxOntWrapper} using it.
     * 
     * @param ncbiOntFile   A {@code String} that is the name of the local NCBI 
     *                      ontology file.
     * @throws OWLOntologyCreationException If an error occurred while loading 
     *                                      the ontology.
     * @throws OBOFormatParserException     If the file could not be parsed correctly.
     * @throws IOException                  If the file could not be read.
     */
    private void loadTaxOntology(String ncbiOntFile) 
            throws OWLOntologyCreationException, OBOFormatParserException, IOException {
        log.entry(ncbiOntFile);
        ParserWrapper parserWrapper = new ParserWrapper();
        this.taxOntWrapper = new OWLGraphWrapper(parserWrapper.parse(ncbiOntFile));
        log.exit();
    }
    
    /**
     * Modifies the {@code OWLOntology} wrapped into {link #taxOntWrapper}, 
     * so that it will only include the subgraphs to the root containing 
     * the requested species, and so that the requested species will be leaves 
     * of the ontology. The requested species are specified by providing 
     * their NCBI taxonomy IDs using the {@code ncbiSpeciesIds} argument 
     * (for instance, should contain {@code 9606} to include human, as it is its ID 
     * on the NCBI taxonomy website).
     * 
     * @param ncbiSpeciesIds    A {@code Set} of {@code Integer}s that are the NCBI IDs 
     *                          of the species used to filter the ontology.
     */
    private void filterTaxOntology(Set<Integer> ncbiSpeciesIds) {
        log.entry(ncbiSpeciesIds);
        
        //transform the NCBI IDs into ontology IDs
        Set<String> speciesIds = new HashSet<String>();
        for (Integer id: ncbiSpeciesIds) {
            speciesIds.add(this.getTaxOntologyId(id)); 
        }
        //use an OWLGraphManipulator to keep only subgraphs that include 
        //the requested species
        OWLGraphManipulator manipulator = new OWLGraphManipulator(this.taxOntWrapper);
        manipulator.filterSubgraphs(speciesIds);
        //and remove terms descendants of the requested species, so that they will be 
        //leaves of the ontology. To make use of the manipulator, we will consider 
        //each direct descendant of a requested species as the root of a subgraph 
        //to remove.
        Set<String> subgraphRootsToDel = new HashSet<String>();
        for (String speciesId: speciesIds) {
            for (OWLClass classToDel: this.taxOntWrapper.getOWLClassDirectDescendants(
                    this.taxOntWrapper.getOWLClassByIdentifier(speciesId))) {
                subgraphRootsToDel.add(this.taxOntWrapper.getIdentifier(classToDel));
            }
        }
        manipulator.removeSubgraphs(subgraphRootsToDel, false);
        
        log.exit();
    }
    
    /**
     * Obtain the species from the NCBI taxonomy ontology wrapped into 
     * {@link #taxOntWrapper}, converts them into {@code SpeciesTO}s, and 
     * returns them in a {@code Set}.
     * <p>
     * The principle is that, at this point, the species should be the leaves 
     * of the ontology, and only them.
     * 
     * @return  A {@code Set} of {@code SpeciesTO}s corresponding to the species 
     *          retrieved from the taxonomy ontology wrapped into {@link #taxOntWrapper}.
     * @throws IllegalStateException    If the {@code OWLOntology} used, wrapped 
     *                                  into {@link #taxOntWrapper}, does not allow 
     *                                  to properly acquire {@code SpeciesTO}s.
     */
    private Set<SpeciesTO> getSpeciesTOs() throws IllegalStateException {
        log.entry();
        
        Set<SpeciesTO> speciesTOs = new HashSet<SpeciesTO>();
        //the species should be the leaves of the ontology
        for (OWLClass leaf: this.taxOntWrapper.getOntologyLeaves()) {
            speciesTOs.add(this.toSpeciesTO(leaf));
        }
        if (speciesTOs.isEmpty()) {
            throw log.throwing(new IllegalStateException("The taxonomy ontology " +
            		"did not allow to acquire any species"));
        }
        return log.exit(speciesTOs);
    }
    
    /**
     * Transforms the provided {@code speciesOWLClass} into a {@code SpeciesTO}. 
     * Unexpected errors would occur if {@code speciesOWLClass} does not correspond 
     * to a species in the NCBO taxonomy ontology.
     * 
     * @param speciesOWLClass   A {@code OWLClass} representing a species in the 
     *                          NCBI taxonomy ontology to be transformed into an 
     *                          {@code SpeciesTO}.
     * @return  A {@code SpeciesTO} corresponding to {@code speciesOWLClass}.
     * @throws IllegalStateException    If the {@code OWLOntology} used, wrapped 
     *                                  into {@link #taxOntWrapper}, does not allow 
     *                                  to identify the parent taxon of 
     *                                  {@code speciesOWLClass}.
     */
    private SpeciesTO toSpeciesTO(OWLClass speciesOWLClass) throws IllegalStateException {
        log.entry(speciesOWLClass);
        
        //we need the parent of this leaf to know the parent taxon ID 
        //of the species
        Set<OWLClass> parents = 
                this.taxOntWrapper.getOWLClassDirectAncestors(speciesOWLClass);
        if (parents.size() != 1) {
            throw log.throwing(new IllegalStateException("The taxonomy ontology " +
                    "has incorrect relations between taxa"));
        }
        //get the NCBI ID of the parent taxon of this species.
        //we retrieve the Integer value of the ID used on the NCBI website, 
        //because this is how we store this ID in the database. But we convert it 
        //to a String because the Bgee classes only accept IDs as Strings.
        String parentTaxonId = String.valueOf(
                this.getTaxNcbiId(this.taxOntWrapper.getIdentifier(
                parents.iterator().next())));
        
        //get the NCBI ID of this species.
        //we retrieve the Integer value of the ID used on the NCBI website, 
        //because this is how we store this ID in the database. But we convert it 
        //to a String because the Bgee classes only accept IDs as Strings.
        String speciesId = String.valueOf(this.getTaxNcbiId(
                this.taxOntWrapper.getIdentifier(speciesOWLClass)));
        
        //get the common name synonym
        String commonName = this.getCommonNameSynonym(speciesOWLClass);
        
        //get the genus and species name from the scientific name
        String scientificName = this.taxOntWrapper.getLabel(speciesOWLClass);
        String[] nameSplit = scientificName.split(" ");
        String genus = nameSplit[0];
        String speciesName = nameSplit[1];
        
        //create and return the SpeciesTO
        return log.exit(new SpeciesTO(speciesId, commonName, genus, 
                speciesName, parentTaxonId));
    }
    
    /**
     * Obtains the taxa from the NCBI taxonomy ontology wrapped into 
     * {@link #taxOntWrapper}, converts them into {@code TaxonTO}s, and 
     * returns them in a {@code Set}.
     * <p>
     * <strong>Warning:</strong> this method will modify the ontology wrapped into 
     * {@link #taxOntWrapper}, by removing all its leaves: this method must be called 
     * when the ontology still includes the species; the species at this point 
     * must be the leaves of the ontology; these leaves will be used to identify 
     * the least common ancestors of all possible pairs of species (so, all possible 
     * pairs of leaves), in order to identify important branchings for Bgee; 
     * these species will then be removed from the ontology, in order to compute 
     * the parameters of a nested set model, for the taxa only (the taxonomy 
     * is represented as a nested set model in Bgee, and does not include the species).
     * 
     * @return  A {@code Set} of {@code TaxonTO}s corresponding to the taxa 
     *          retrieved from the taxonomy ontology wrapped into {@link #taxOntWrapper}.
     * @throws IllegalStateException    If the {@code OWLOntology} used, wrapped 
     *                                  into {@link #taxOntWrapper}, does not allow 
     *                                  to properly acquire any {@code TaxonTO}s.
     */
    private Set<TaxonTO> getTaxonTOs() throws IllegalStateException {
        log.entry();
        
        //need an OntologyUtils to perform the operations
        OntologyUtils utils = new OntologyUtils(this.taxOntWrapper);
        
        //get the least common ancestors of the species used in Bgee: 
        //at this point, the species used in Bgee are the leaves of the ontology 
        //wrapped in taxOntWrapper; we get the least common ancestors of all possible 
        //pairs of leaves (so, all possible pairs of species), in order to identify 
        //the important branching in the ontology for Bgee.
        Set<OWLClass> lcas = utils.getLeafLeastCommonAncestors();
        
        //now we remove the species (the leaves), in order to compute the parameters 
        //of the nested set model, only for the taxa (the taxonomy is represented 
        //as a nested set model in Bgee)
        this.removeSpecies();
        
        //we want to order the taxa based on their scientific name, so we create 
        //a Comparator. This comparator needs the OWLGraphWrapper, so we make 
        //a final variable for taxOntWrapper
        final OWLGraphWrapper wrapper = this.taxOntWrapper;
        Comparator<OWLClass> comparator = new Comparator<OWLClass>() {
            @Override
            public int compare(OWLClass o1, OWLClass o2) {
                return wrapper.getLabel(o1).compareTo(wrapper.getLabel(o2));
            }
        };
        //now we create a List with OWLClass order based on the comparator
        List<OWLClass> classOrder = 
                new ArrayList<OWLClass>(this.taxOntWrapper.getAllOWLClasses());
        Collections.sort(classOrder, comparator);
        
        //get the parameters for the nested set model
        Map<OWLClass, Map<String, Integer>> nestedSetModelParams = 
                utils.computeNestedSetModelParams(classOrder);
        
        //OK, now we have everything to instantiate the TaxonTOs
        Set<TaxonTO> taxonTOs = new HashSet<TaxonTO>();
        for (OWLClass taxon: this.taxOntWrapper.getAllOWLClasses()) {
            //get the NCBI ID of this taxon.
            //we retrieve the Integer value of the ID used on the NCBI website, 
            //because this is how we store this ID in the database. But we convert it 
            //to a String because the Bgee classes only accept IDs as Strings.
            String taxonId = String.valueOf(this.getTaxNcbiId(
                    this.taxOntWrapper.getIdentifier(taxon)));
            String commonName = this.getCommonNameSynonym(taxon);
            String scientificName = this.taxOntWrapper.getLabel(taxon);
            Map<String, Integer> taxonParams = nestedSetModelParams.get(taxon);
            
            taxonTOs.add(
                    new TaxonTO(taxonId, 
                    commonName, 
                    scientificName, 
                    taxonParams.get(OntologyUtils.LEFTBOUNDKEY), 
                    taxonParams.get(OntologyUtils.RIGHTBOUNDKEY), 
                    taxonParams.get(OntologyUtils.LEVELKEY), 
                    lcas.contains(taxon)));
        }
        
        if (taxonTOs.isEmpty()) {
            throw log.throwing(new IllegalStateException("The taxonomy ontology " +
                    "did not allow to acquire any taxon"));
        }
        
        return log.exit(taxonTOs);
    }
    
    /**
     * Removes the species from the NCBI taxonomy {@code OWLOntology} wrapped into 
     * {@link #taxOntWrapper}. At this point, the species used in Bgee should be 
     * the leaves of the ontology. So all leaves will be removed from the ontology.
     */
    private void removeSpecies() {
        log.entry();
        
        //use an OWLGraphManipulator to remove the leaves from the ontology.
        //At this point, leaves should be the species used in Bgee.
        OWLGraphManipulator manipulator = new OWLGraphManipulator(this.taxOntWrapper);
        for (OWLClass species: this.taxOntWrapper.getOntologyLeaves()) {
            manipulator.removeClassAndPropagateEdges(
                    this.taxOntWrapper.getIdentifier(species));
        }
        
        log.exit();
    }
    
    /**
     * Returns the synonym corresponding to the common name of the provided 
     * {@code owlClass}. The category of such a synonym is {@link #SYNCOMMONNAMECATE}, 
     * see {@code owltools.graph.OWLGraphWrapper.ISynonym}. Returns {@code null} 
     * if no common name synonym was found.
     * 
     * @param owlClass  The {@code OWLClass} which we want to retrieve 
     *                  the common name for.
     * @return          A {@code String} that is the common name of {@code owlClass}.
     */
    private String getCommonNameSynonym(OWLClass owlClass) {
        log.entry(owlClass);
        
        String commonName = null;
        List<ISynonym> synonyms = this.taxOntWrapper.getOBOSynonyms(owlClass);
        if (synonyms != null) {
            for (ISynonym syn: synonyms) {
                if (syn.getCategory().equals(SYNCOMMONNAMECAT)) {
                    commonName = syn.getLabel();
                    break;
                }
            }
        }
        
        return log.exit(commonName);
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
