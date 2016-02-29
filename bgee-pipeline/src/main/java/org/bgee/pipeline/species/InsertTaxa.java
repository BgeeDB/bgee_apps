package org.bgee.pipeline.species;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.keyword.KeywordDAO.EntityToKeywordTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.MySQLDAOUser;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.annotations.AnnotationCommon;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;

import owltools.graph.OWLGraphManipulator;
import owltools.graph.OWLGraphUtil;
import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;

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
     * A {@code String} defining the category of the synonym providing the common 
     * name of taxa in the taxonomy ontology. 
     * See {@code owltools.graph.OWLGraphWrapper.ISynonym}.
     */
    private static final String SYN_COMMON_NAME_CAT = "genbank_common_name";
    
    /**
     * A {@code String} that is the key to retrieve the IDs of the species used in Bgee 
     * from the {@code Map}s returned by {@link #getSpeciesFromFile(String)}, 
     * and that is also the name of the column to retrieve these IDs from the TSV file 
     * storing the species used in Bgee.
     */
    public static final String SPECIES_ID_KEY = "taxon ID";
    /**
     * A {@code String} that is the key to retrieve the genus name (for instance, 
     * "homo") of the species used in Bgee from the {@code Map}s returned by 
     * {@link #getSpeciesFromFile(String)}, and that is also the name of the column 
     * to retrieve these genus names from the TSV file storing the species used in Bgee.
     */
    public static final String SPECIES_GENUS_KEY = "genus";
    /**
     * A {@code String} that is the key to retrieve the species name (for instance, 
     * "sapiens") of the species used in Bgee from the {@code Map}s returned by 
     * {@link #getSpeciesFromFile(String)}, and that is also the name of the column 
     * to retrieve these names from the TSV file storing the species used in Bgee.
     */
    public static final String SPECIES_NAME_KEY = "species";
    /**
     * A {@code String} that is the key to retrieve the common name (for instance, 
     * "human") of the species used in Bgee from the {@code Map}s returned by 
     * {@link #getSpeciesFromFile(String)}, and that is also the name of the column 
     * to retrieve these common names from the TSV file storing the species used in Bgee.
     */
    public static final String SPECIES_COMMON_NAME_KEY = "speciesCommonName";
    /**
     * A {@code String} that is the key to retrieve the path to the genome file 
     * for a species used in Bgee, from the {@code Map}s returned by 
     * {@link #getSpeciesFromFile(String)}, and that is also the name of the column 
     * to retrieve these values from the TSV file storing the species used in Bgee.
     * <p>
     * This is the path to retrieve the genome file we use for this species, 
     * from the GTF directory of the Ensembl FTP, without the Ensembl version suffix, 
     * nor the file type suffixes. For instance, for human, the GTF file in Ensembl 75 
     * is stored at: {@code ftp://ftp.ensembl.org/pub/release-75/gtf/homo_sapiens/Homo_sapiens.GRCh37.75.gtf.gz}.
     * This field would then contain: {@code homo_sapiens/Homo_sapiens.GRCh37}
     * This field is needed because we use for some species the genome of another species 
     * (for instance, chimp genome for bonobo species).
     */
    public static final String SPECIES_GENOME_FILE_KEY= "genomeFilePath";
    /**
     * A {@code String} that is the key to retrieve the genome version for a species
     * used in Bgee, from the {@code Map}s returned by {@link #getSpeciesFromFile(String)},
     * and that is also the name of the column to retrieve these values from 
     * the TSV file storing the species used in Bgee.
     */
    public static final String SPECIES_GENOME_VERSION_KEY= "genomeVersion";
    /**
     * A {@code String} that is the key to retrieve the ID of the data source providing 
     * genome information for a species, from the {@code Map}s returned by {@link #getSpeciesFromFile(String)},
     * and that is also the name of the column to retrieve these values from 
     * the TSV file storing the species used in Bgee.
     */
    public static final String SPECIES_GENOME_DATA_SOURCE_ID_KEY= "dataSourceId";
    /**
     * A {@code String} that is the key to retrieve the ID of the species whose the genome 
     * was used for a species used in Bgee, from the {@code Map}s returned by 
     * {@link #getSpeciesFromFile(String)}, and that is also the name of the column 
     * to retrieve these values from the TSV file storing the species used in Bgee.
     * <p>
     * This is used when a genome is not in Ensembl. For instance, for bonobo (ID 9597), 
     * we use the chimp genome (ID 9598), because bonobo is not in Ensembl. 
     */
    public static final String SPECIES_GENOME_ID_KEY= "genomeSpeciesId";
    /**
     * A {@code String} that is the key to retrieve the fake prefix of genes for species 
     * whose genome is not in Ensembl, and that are used in Bgee, 
     * from the {@code Map}s returned by 
     * {@link #getSpeciesFromFile(String)}, and that is also the name of the column 
     * to retrieve these values from the TSV file storing the species used in Bgee.
     * <p>
     * This is because when another genome was used for a species in Bgee, we change 
     * the gene ID prefix (for instance, the chimp gene IDs, starting with 'ENSPTRG', 
     * will be changed to 'PPAG' when used for the bonobo).
     */
    public static final String SPECIES_FAKE_GENE_PREFIX_KEY= "fakeGeneIdPrefix";
    /**
     * A {@code String} that is the key to retrieve keywords associated to a species, 
     * separated by a separator {@link org.bgee.pipeline.annotations.AnnotationCommon#ENTITY_SEPARATORS}.
     */
    public static final String SPECIES_KEYWORDS_KEY= "keywords";
    
    /**
     * A {@code OWLGraphWrapper} wrapping the NCBI taxonomy {@code OWLOntology}.
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
     * <li>path to the tsv files containing the species used in Bgee. This is the file 
     * to modify to add/remove a species. The first line should be a header line, 
     * defining 7 columns, named exactly as: {@link #SPECIES_ID_KEY}, 
     * {@link #SPECIES_GENUS_KEY}, {@link #SPECIES_NAME_KEY}, {@link #SPECIES_COMMON_NAME_KEY}, 
     * {@link #SPECIES_GENOME_FILE_KEY}, {@link SPECIES_GENOME_VERSION_KEY},
     * {@link #SPECIES_GENOME_ID_KEY}, {@link #SPECIES_FAKE_GENE_PREFIX_KEY} (in whatever order).
     * the IDs should correspond to the NCBI taxonomy ID (e.g., 9606 for human).
     * <li>path to the tsv files containing the IDs of the taxa to be inserted in Bgee, 
     * corresponding to the NCBI taxonomy ID (e.g., 9605 for homo). Whatever the values 
     * in this file are, the branches which the species used in Bgee belong to 
     * will be inserted in any case. Using this file, you can specify additional taxa 
     * to be inserted (along with their ancestors), which is useful, for instance 
     * to back up our homology annotations. It does not matter whether this file 
     * also includes the species used in Bgee, or their ancestors. The first line 
     * of this file should be a header line, efining a column to get IDs from, named 
     * exactly "taxon ID" (other columns are optional and will be ignored).
     * <li>path to the file storing the NCBI taxonomy as an ontology. This taxonomy 
     * must contain all the branches related to the species and taxa to be used 
     * in Bgee.
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
        int expectedArgLength = 3;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                    "provided, expected " + expectedArgLength + " arguments, " + args.length + 
                    " provided."));
        }
        
        InsertTaxa insert = new InsertTaxa();
        insert.insertSpeciesAndTaxa(args[0], args[1], args[2]);
        
        log.exit();
    }
    
    /**
     * Inserts species and taxa into the Bgee database. This method uses: 
     * <ul>
     * <li>the path to the tsv files containing the species used in Bgee. This is the file 
     * to modify to add/remove a species. The first line should be a header line, 
     * defining 7 columns, named exactly as: {@link #SPECIES_ID_KEY}, 
     * {@link #SPECIES_GENUS_KEY}, {@link #SPECIES_NAME_KEY}, {@link #SPECIES_COMMON_NAME_KEY}, 
     * {@link #SPECIES_GENOME_FILE_KEY}, {@link SPECIES_GENOME_VERSION_KEY},
     * {@link #SPECIES_GENOME_ID_KEY}, {@link #SPECIES_FAKE_GENE_PREFIX_KEY} (in whatever order).
     * the IDs should correspond to the NCBI taxonomy ID (e.g., 9606 for human).
     * <li>the path to a TSV file containing the NCBI taxonomy IDs of additional taxa 
     * to be inserted. Whatever the values in this file are, the branches which 
     * the species used in Bgee belong to will be inserted in any case. Using this file, 
     * you can specify additional taxa to be inserted (along with their ancestors), 
     * which is useful, for instance to back up our homology annotations. It does not 
     * matter whether this file also includes the species used in Bgee, or their ancestors. 
     * The first line of this file should be a header line, defining a column to get IDs from, 
     * named exactly "taxon ID" (other columns are optional and will be ignored). 
     * This file can contain no taxa, as long as the header line is present.
     * <li>the path to the file storing the NCBI taxonomy as an ontology. This taxonomy 
     * must contain all the branches related to the species and taxa to be used 
     * in Bgee.
     * </ul>
     * 
     * @param speciesFile   A {@code String} that is the path to the TSV file 
     *                      containing the species used in Bgee
     * @param taxonFile     A {@code String} that is the path to the TSV file 
     *                      containing the IDs of additional taxa to insert in Bgee.
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
    public void insertSpeciesAndTaxa(String speciesFile, String taxonFile, 
            String ncbiOntFile) throws FileNotFoundException, IOException, 
            OWLOntologyCreationException, OBOFormatParserException, 
            IllegalArgumentException, DAOException {
        log.entry(speciesFile, taxonFile, ncbiOntFile);
        
        this.insertSpeciesAndTaxa(this.getSpeciesFromFile(speciesFile), 
                AnnotationCommon.getTaxonIds(taxonFile), 
                OntologyUtils.loadOntology(ncbiOntFile));
        
        log.exit();
    }
    
    /**
     * Extract the information about the species to include in Bgee from the provided 
     * TSV file. The first line of this file should be a header line, 
     * defining 7 columns, named exactly as: {@link #SPECIES_ID_KEY}, 
     * {@link #SPECIES_GENUS_KEY}, {@link #SPECIES_NAME_KEY}, {@link #SPECIES_COMMON_NAME_KEY}, 
     * {@link #SPECIES_GENOME_FILE_KEY}, {@link SPECIES_GENOME_VERSION_KEY}, 
     * {@link #SPECIES_GENOME_DATA_SOURCE_ID_KEY}, 
     * {@link #SPECIES_GENOME_ID_KEY}, {@link #SPECIES_FAKE_GENE_PREFIX_KEY}, 
     * {@link #SPECIES_KEYWORDS_KEY} (in whatever order). 
     * This method returns a {@code Collection} where each 
     * species is represented by a {@code Map}, containing information mapped 
     * to the keys listed above. In these {@code Map}s, the value associated to 
     * {@link #SPECIES_ID_KEY} will be an {@code Integer}, other values will be 
     * {@code String}s.
     * 
     * @param speciesFile   A {@code String} that is the path to the TSV file 
     *                      containing the species used in Bgee
     * @return              A {@code Collection} of {@code Map}s where each {@code Map} 
     *                      represents a species, with information about it mapped to 
     *                      the keys {@link #SPECIES_ID_KEY}, {@link #SPECIES_GENUS_KEY}, 
     *                      {@link #SPECIES_NAME_KEY}, {@link #SPECIES_COMMON_NAME_KEY}, 
     *                      {@link #SPECIES_GENOME_FILE_KEY}, {@link #SPECIES_GENOME_VERSION_KEY}, 
     *                      {@link #SPECIES_GENOME_DATA_SOURCE_ID_KEY}, 
     *                      {@link #SPECIES_GENOME_ID_KEY}, {@link #SPECIES_FAKE_GENE_PREFIX_KEY}, 
     *                      {@link #SPECIES_KEYWORDS_KEY}.
     *                      The values associated to {@link #SPECIES_ID_KEY} and 
     *                      {@link #SPECIES_GENOME_DATA_SOURCE_ID_KEY} are 
     *                      an {@code Integer}, other values are {@code String}s.
     * @throws FileNotFoundException        If the file could not be found.
     * @throws IOException                  If the file could not be read.
     */
    private Collection<Map<String, Object>> getSpeciesFromFile(String speciesFile) 
            throws FileNotFoundException, IOException {
        log.entry(speciesFile);

        Collection<Map<String, Object>> allSpecies = new HashSet<Map<String, Object>>();
        try (ICsvMapReader mapReader = 
                new CsvMapReader(new FileReader(speciesFile), Utils.TSVCOMMENTED)) {
            
            String unexpectedFormat = "The provided TSV species file is not " +
            		"in the expected format";
            String[] header = mapReader.getHeader(true);
            if (header.length != 10) {
                throw log.throwing(new IllegalArgumentException(unexpectedFormat));
            }
            
            CellProcessor[] processors = new CellProcessor[header.length];
            for (int i = 0; i < header.length; i++) {
                if (header[i].equalsIgnoreCase(SPECIES_ID_KEY)) {
                    processors[i] = new UniqueHashCode(new NotNull(new ParseInt()));
                } else if (header[i].equalsIgnoreCase(SPECIES_GENUS_KEY)) {
                    processors[i] = new NotNull();
                } else if (header[i].equalsIgnoreCase(SPECIES_NAME_KEY)) {
                    processors[i] = new NotNull();
                } else if (header[i].equalsIgnoreCase(SPECIES_COMMON_NAME_KEY)) {
                    processors[i] = new UniqueHashCode(new NotNull());
                } else if (header[i].equalsIgnoreCase(SPECIES_GENOME_FILE_KEY)) {
                    processors[i] = new NotNull();
                } else if (header[i].equalsIgnoreCase(SPECIES_GENOME_VERSION_KEY)) {
                    processors[i] = new NotNull();
                } else if (header[i].equalsIgnoreCase(SPECIES_GENOME_DATA_SOURCE_ID_KEY)) {
                    processors[i] = new NotNull(new ParseInt());
                } else if (header[i].equalsIgnoreCase(SPECIES_GENOME_ID_KEY)) {
                    processors[i] = new Optional(new ParseInt());
                } else if (header[i].equalsIgnoreCase(SPECIES_FAKE_GENE_PREFIX_KEY)) {
                    processors[i] = new Optional();
                } else if (header[i].equalsIgnoreCase(SPECIES_KEYWORDS_KEY)) {
                    processors[i] = new Optional(new AnnotationCommon.ParseMultipleStringValues());
                } else {
                    throw log.throwing(new IllegalArgumentException(unexpectedFormat));
                }
            }
            
            Map<String, Object> species;
            while( (species = mapReader.read(header, processors)) != null ) {
                allSpecies.add(species);
            }
        }
        if (allSpecies.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The provided TSV " +
            		"species file did not allow to acquire any species."));
        }
        
        return log.exit(allSpecies);
    }
    
    /**
     * Inserts species and taxa into the Bgee database. The arguments are: 
     * <ul>
     * <li>A {@code Collection} of {@code Map}s where each {@code Map} represents 
     * a species, with information about it mapped to the keys {@link #SPECIES_ID_KEY}, 
     * {@link #SPECIES_GENUS_KEY}, {@link #SPECIES_NAME_KEY}, {@link #SPECIES_COMMON_NAME_KEY}.
     * Each {@code Map} should contain exactly these 4 entries, with no {@code null} 
     * values permitted. Value associated to {@link #SPECIES_ID_KEY} should be 
     * an {@code Integer} (the NCBI taxonomy ID, for instance, 9606 for human), 
     * other values should be {@code String}s.
     * <li>a {@code Set} of {@code Integer}s that are the NCBI taxonomy IDs of additional taxa 
     * to be inserted. Whatever the values in this {@code Set} are, the branches which 
     * the species used in Bgee belong to will be inserted in any case. Using these IDs, 
     * you can specify additional taxa to be inserted (along with their ancestors), 
     * which is useful, for instance to back up our homology annotations. It does not 
     * matter whether these IDs also include the species used in Bgee, or their ancestors.
     * <li>an {@code OWLOntology} representing the NCBI taxonomy ontology. This taxonomy 
     * must contain all the branches related to the species and taxa to be used in Bgee.
     * </ul>
     * 
     * @param allSpecies    A {@code Collection} of {@code Map}s where each {@code Map} 
     *                      represents a species to be used in Bgee.
     * @param taxonIds      a {@code Set} of {@code Integer}s that are the IDs 
     *                      of additional taxa to be inserted in Bgee
     * @param taxOntology   An {@code OWLOntology} that is the NCBI taxonomy 
     *                      ontology.
     * @throws IllegalArgumentException     If the arguments provided invalid information.
     * @throws DAOException                 If an error occurred while inserting 
     *                                      the data into the Bgee database.
     */
    public void insertSpeciesAndTaxa(Collection<Map<String, Object>> allSpecies, 
            Set<Integer> taxonIds, OWLOntology taxOntology) 
            throws IllegalArgumentException, DAOException {
        log.entry(allSpecies, taxonIds, taxOntology);
        log.info("Starting insertion of species and taxa...");
        
        //catch any IllegalStateException to wrap it into a IllegalArgumentException 
        //(a IllegalStateException would be generated because the OWLOntology loaded 
        //from ncbiOntFile would be invalid, so it would be a wrong argument)
        try {
            
            //load the NCBI taxonomy ontology
            this.taxOntWrapper = new OWLGraphWrapper(taxOntology);
            
            //get the SpeciesTOs to insert their information into the database
            Set<SpeciesTO> speciesTOs = this.getSpeciesTOs(allSpecies);
            
            //retrieve keywords associated to species
            //Suppress warning because SPECIES_KEYWORDS_KEY field is processed by ParseMultipleStringValues, 
            //which return a List<String>
            @SuppressWarnings("unchecked")
            Map<Integer, Set<String>> speciesIdToKeywords = allSpecies.stream()
                    .filter(spe -> spe.get(SPECIES_KEYWORDS_KEY) != null)
                    .collect(Collectors.toMap(
                            spe -> (Integer) spe.get(SPECIES_ID_KEY), 
                            spe -> new HashSet<>((List<String>) spe.get(SPECIES_KEYWORDS_KEY))));
            Set<String> allKeywords = speciesIdToKeywords.values().stream()
                    .filter(set -> set != null)
                    .flatMap(set -> set.stream())
                    .collect(Collectors.toSet());
            
            //now get the TaxonTOs to insert their information into the database.
            //note that using this method will modify the taxonomy ontology, 
            //by removing the Bgee species from it, and removing all taxa not related 
            //to neither speciesIds nor taxonIds.
            //we provide only the species IDs
            Set<Integer> speciesIds = new HashSet<Integer>();
            for (Map<String, Object> species: allSpecies) {
                speciesIds.add((Integer) species.get(SPECIES_ID_KEY));
            }
            Set<TaxonTO> taxonTOs = this.getTaxonTOs(speciesIds, taxonIds);
            
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
                
                //insert keywords
                this.getKeywordDAO().insertKeywords(allKeywords);
                //now we retrieve the keywords to find their IDs
                final Map<String, String> keywordToId = this.getKeywordDAO().getKeywords(allKeywords).stream()
                        .collect(Collectors.toMap(keywordTO -> keywordTO.getName(), 
                                                  keywordTO -> keywordTO.getId()));
                //and we insert relations to species IDs
                this.getKeywordDAO().insertKeywordToSpecies(speciesIdToKeywords.entrySet().stream()
                        .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                        .flatMap(entry -> entry.getValue().stream().map(keyword -> 
                            new EntityToKeywordTO(Integer.toString(entry.getKey()), keywordToId.get(keyword))))
                        .collect(Collectors.toSet()));
                
                this.commit();
            } finally {
                this.closeDAO();
            }
        } catch (IllegalStateException e) {
            log.catching(e);
            throw log.throwing(new IllegalArgumentException(
                    "The OWLOntology provided is invalid", e));
        }

        log.info("Done inserting species and taxa.");
        log.exit();
    }
    
    /**
     * Use the species provided through {@code species}, and the NCBI taxonomy 
     * ontology wrapped into {@link #taxOntWrapper}, to generate the corresponding 
     * {@code SpeciesTO}s, and returns them in a {@code Set}.
     * <p>
     * {@code species} should be a {@code Collection} of {@code Map}s where each 
     * {@code Map} represents a species, with information about it mapped to the keys 
     * {@link #SPECIES_ID_KEY}, {@link #SPECIES_GENUS_KEY}, {@link #SPECIES_NAME_KEY}, 
     * {@link #SPECIES_COMMON_NAME_KEY}. Each {@code Map} should contain exactly these 
     * 4 entries, with no {@code null} values permitted. Value associated to 
     * {@link #SPECIES_ID_KEY} should be an {@code Integer} (the NCBI taxonomy ID, 
     * for instance, 9606 for human), other values should be {@code String}s.
     * 
     * @param allSpecies    A {@code Collection} of {@code Map}s where each {@code Map} 
     *                      represents a species to be used in Bgee.
     * @return  A {@code Set} of {@code SpeciesTO}s corresponding to the species 
     *          retrieved from the taxonomy ontology wrapped into {@link #taxOntWrapper}.
     * @throws IllegalStateException    If the {@code OWLOntology} used, wrapped 
     *                                  into {@link #taxOntWrapper}, does not allow 
     *                                  to properly acquire {@code SpeciesTO}s.
     */
    private Set<SpeciesTO> getSpeciesTOs(Collection<Map<String, Object>> allSpecies) 
            throws IllegalStateException {
        log.entry(allSpecies);
        
        Set<SpeciesTO> speciesTOs = new HashSet<SpeciesTO>();
        for (Map<String, Object> species: allSpecies) {
            int speciesId = (Integer) species.get(SPECIES_ID_KEY);
            OWLClass speciesCls = this.taxOntWrapper.getOWLClassByIdentifier(
                    OntologyUtils.getTaxOntologyId(speciesId), true);
            if (speciesCls == null) {
                throw log.throwing(new IllegalStateException(
                        "The provided species ID " + speciesId + 
                        "corresponds to no taxon in the taxonomy ontology."));
            }
            
            //get the parent taxon ID of the species
            Set<OWLClass> parents = 
                    this.taxOntWrapper.getOWLClassDirectAncestors(speciesCls);
            if (parents.size() != 1) {
                throw log.throwing(new IllegalStateException("The taxonomy ontology " +
                        "has incorrect relations between taxa"));
            }
            //get the NCBI ID of the parent taxon of this species.
            //we retrieve the Integer value of the ID used on the NCBI website, 
            //because this is how we store this ID in the database. But we convert it 
            //to a String because the Bgee classes only accept IDs as Strings.
            String parentTaxonId = String.valueOf(OntologyUtils.getTaxNcbiId(
                    this.taxOntWrapper.getIdentifier(parents.iterator().next())));
            
            String commonName  = (String) species.get(SPECIES_COMMON_NAME_KEY);
            String genus       = (String) species.get(SPECIES_GENUS_KEY);
            String speciesName = (String) species.get(SPECIES_NAME_KEY);
            if (StringUtils.isBlank(commonName) || StringUtils.isBlank(genus) || 
                    StringUtils.isBlank(speciesName)) {
                throw log.throwing(new IllegalArgumentException("The provided species " +
                        "contain incorrect information: " + commonName + " - " + 
                        genus + " - " + speciesName));
            }
            String genomeFilePath = (String) species.get(SPECIES_GENOME_FILE_KEY);
            if (StringUtils.isBlank(genomeFilePath)) {
                throw log.throwing(new IllegalArgumentException(
                        "Missing path to genome file for species: " + commonName));
            }
            String genomeVersion = (String) species.get(SPECIES_GENOME_VERSION_KEY);
            if (StringUtils.isBlank(genomeVersion)) {
                throw log.throwing(new IllegalArgumentException(
                        "Missing path to genome version for species: " + commonName));
            }
            Integer dataSourceId = (Integer) species.get(SPECIES_GENOME_DATA_SOURCE_ID_KEY);
            
            Integer genomeSpeciesId = (Integer) species.get(SPECIES_GENOME_ID_KEY);
            String fakeGeneIdPrefix = (String) species.get(SPECIES_FAKE_GENE_PREFIX_KEY);
            
            speciesTOs.add(new SpeciesTO(String.valueOf(speciesId), commonName, genus, 
                    speciesName, parentTaxonId, genomeFilePath, genomeVersion, 
                    (dataSourceId == null ? null: String.valueOf(dataSourceId)), 
                    (genomeSpeciesId == null ? null: String.valueOf(genomeSpeciesId)), 
                    fakeGeneIdPrefix));
        }
        if (speciesTOs.size() != allSpecies.size()) {
            throw log.throwing(new IllegalStateException("The taxonomy ontology " +
            		"did not allow to acquire all the requested species"));
        }
        return log.exit(speciesTOs);
    }
    
    /**
     * Filter and obtains requested taxa from the NCBI taxonomy ontology wrapped into 
     * {@link #taxOntWrapper}, converts them into {@code TaxonTO}s, and 
     * returns them in a {@code Set}.
     * <p>
     * The ontology wrapped into {@link #taxOntWrapper} will be modified to remove 
     * any taxa not related to the species provided through {@code speciesIds}, 
     * or to the taxa provided through {@code taxonIds} (only those taxa and their 
     * ancestors, and the ancestors of the species used in Bgee, will be kept).
     * Also, the species will be removed from the ontology, in order to compute 
     * the parameters of a nested set model, for the taxa only (the taxonomy 
     * is represented as a nested set model in Bgee, and does not include the species).
     * 
     * @param speciesIds    a {@code Set} of {@code Integer}s that are the IDs 
     *                      of the species used in Bgee
     * @param taxonIds      a {@code Set} of {@code Integer}s that are the IDs 
     *                      of additional taxa to be inserted in Bgee
     * @return  A {@code Set} of {@code TaxonTO}s corresponding to the taxa 
     *          retrieved from the taxonomy ontology wrapped into {@link #taxOntWrapper}.
     * @throws IllegalStateException    If the {@code OWLOntology} used, wrapped 
     *                                  into {@link #taxOntWrapper}, does not allow 
     *                                  to properly acquire any {@code TaxonTO}s.
     */
    private Set<TaxonTO> getTaxonTOs(Set<Integer> speciesIds, Set<Integer> taxonIds) 
            throws IllegalStateException {
        log.entry(speciesIds, taxonIds);
        
        //get the least common ancestors of the species used in Bgee: 
        //we get the least common ancestors of all possible pairs of species), 
        //in order to identify the important branching in the ontology for Bgee.
        Set<OWLClass> lcas = new HashSet<OWLClass>();
        
        for (int speciesId1: speciesIds) {
            OWLClass species1 = this.taxOntWrapper.getOWLClassByIdentifier(
                    OntologyUtils.getTaxOntologyId(speciesId1), true);
            for (int speciesId2: speciesIds) {
                OWLClass species2 = this.taxOntWrapper.getOWLClassByIdentifier(
                        OntologyUtils.getTaxOntologyId(speciesId2), true);
                if (species1 == species2) {
                    continue;
                }
                for (OWLObject lca: OWLGraphUtil.findLeastCommonAncestors(
                        this.taxOntWrapper, species1, species2)) {
                    if (lca instanceof OWLClass) {
                        lcas.add((OWLClass) lca);
                    }
                }
            }
        }
        if (lcas.isEmpty()) {
            throw log.throwing(new IllegalStateException("The ontology " +
                    "did not allow to identify any least common ancestors of species used."));
        }
        
        //now keep only the taxa related to speciesIds or taxonIds, and remove 
        //the species used in Bgee in order to compute the parameters 
        //of the nested set model,
        this.filterOntology(speciesIds, taxonIds);
        
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
        Map<OWLClass, Map<String, Integer>> nestedSetModelParams;
        try {
            //need an OntologyUtils to perform the operations
            OntologyUtils utils = new OntologyUtils(this.taxOntWrapper);
            nestedSetModelParams = utils.computeNestedSetModelParams(null, classOrder);
        } catch (UnknownOWLOntologyException e) {
          //should not be thrown, OntologyUtils has been provided directly with 
            //an OWLGraphWrapper
            throw log.throwing(new IllegalStateException("An OWLGraphWrapper should " +
                    "have been arleady privided"));
        }
        
        //OK, now we have everything to instantiate the TaxonTOs
        Set<TaxonTO> taxonTOs = new HashSet<TaxonTO>();
        for (OWLClass taxon: this.taxOntWrapper.getAllOWLClasses()) {
            //get the NCBI ID of this taxon.
            //we retrieve the Integer value of the ID used on the NCBI website, 
            //because this is how we store this ID in the database. But we convert it 
            //to a String because the Bgee classes only accept IDs as Strings.
            String taxonId = String.valueOf(OntologyUtils.getTaxNcbiId(
                    this.taxOntWrapper.getIdentifier(taxon)));
            String commonName = this.getCommonNameSynonym(taxon);
            String scientificName = this.taxOntWrapper.getLabel(taxon);
            Map<String, Integer> taxonParams = nestedSetModelParams.get(taxon);
            
            taxonTOs.add(
                    new TaxonTO(taxonId, 
                    commonName, 
                    scientificName, 
                    taxonParams.get(OntologyUtils.LEFT_BOUND_KEY), 
                    taxonParams.get(OntologyUtils.RIGHT_BOUND_KEY), 
                    taxonParams.get(OntologyUtils.LEVEL_KEY), 
                    lcas.contains(taxon)));
        }
        
        if (taxonTOs.isEmpty()) {
            throw log.throwing(new IllegalStateException("The taxonomy ontology " +
                    "did not allow to acquire any taxon"));
        }
        
        return log.exit(taxonTOs);
    }
    
    /**
     * Modifies the {@code OWLOntology} wrapped into {link #taxOntWrapper} to remove 
     * any taxa not related to the species provided through {@code speciesIds}, 
     * or to the taxa provided through {@code taxonIds} (only those taxa and their 
     * ancestors, and the ancestors of the species used in Bgee, will be kept).
     * Also, the species will be removed from the ontology, in order to compute 
     * the parameters of a nested set model, for the taxa only (the taxonomy 
     * is represented as a nested set model in Bgee, and does not include the species).
     * The IDs used are NCBI taxonomy IDs (for instance, {@code 9606} for human).
     * 
     * @param ncbiSpeciesIds    A {@code Set} of {@code Integer}s that are the NCBI IDs 
     *                          of the species to be used in Bgee (along with their 
     *                          ancestor taxa).
     * @param taxonIds          A {@code Set} of {@code Integer}s that are the IDs 
     *                          of additional taxa to be inserted in Bgee (along with 
     *                          their ancestors)
     * @throws IllegalStateException    If the {@code OWLOntology} wrapped into 
     *                                  {link #taxOntWrapper} does not contain 
     *                                  some of the requested species or taxa.
     */
    private void filterOntology(Set<Integer> ncbiSpeciesIds, Set<Integer> ncbiTaxonIds) 
        throws IllegalStateException {
        log.entry(ncbiSpeciesIds, ncbiTaxonIds);
        log.trace("Filtering ontology to keep only requested species and taxa...");
        
        Set<Integer> allTaxonIds = new HashSet<Integer>(ncbiSpeciesIds);
        allTaxonIds.addAll(ncbiTaxonIds);
        
        Set<OWLClass> owlClassesToKeep = new HashSet<OWLClass>();
        for (int taxonId: allTaxonIds) {
            OWLClass taxClass = this.taxOntWrapper.getOWLClassByIdentifier(
                    OntologyUtils.getTaxOntologyId(taxonId), true);
            if (taxClass == null) {
                throw log.throwing(new IllegalStateException("Taxon " + taxonId + 
                        " was not found in the ontology"));
            }
            if (!ncbiSpeciesIds.contains(taxonId)) {
                owlClassesToKeep.add(taxClass);
            }
            owlClassesToKeep.addAll(this.taxOntWrapper.getOWLClassAncestors(taxClass));
        }
        OWLGraphManipulator manipulator = new OWLGraphManipulator(this.taxOntWrapper);
        int taxonRemovedCount = manipulator.filterClasses(owlClassesToKeep).size();
        
        log.trace("Done filtering, {} taxa removed", taxonRemovedCount);
        log.exit();
    }
    
    /**
     * Returns the synonym corresponding to the common name of the provided 
     * {@code owlClass}. The category of such a synonym is {@link #SYN_COMMON_NAME_CAT}, 
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
                if (syn.getCategory().equals(SYN_COMMON_NAME_CAT)) {
                    commonName = syn.getLabel();
                    break;
                }
            }
        }
        
        return log.exit(commonName);
    }
}
