package org.bgee.pipeline.gene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.modelmbean.XMLParseException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalNodeTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalNodeToGeneTO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.api.species.TaxonDAO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTOResultSet;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.MySQLDAOUser;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;

import sbc.orthoxml.Gene;
import sbc.orthoxml.Group;
import sbc.orthoxml.Species;
import sbc.orthoxml.io.OrthoXMLReader;

/**
 * This class parses the OrthoXML file which contains all the data of the hierarchical 
 * orthologous groups obtained from OMA. It retrieves all the data pertaining to the
 * orthologous genes.
 * 
 * @author Komal Sanjeev
 * @author Valentine Rech de Laval
 * @author Julien Wollbrett
 * @version Bgee 14
 * @since Bgee 13
 */
//FIXME: reactivate after fix
public class ParseOrthoXML extends MySQLDAOUser {

    private final static Logger log = LogManager.getLogger(ParseOrthoXML.class.getName());

    /**
     * A {@code String} that is the pattern use to split OMA Gene Identifiers.
     */
    private final static String GENE_ID_SPLIT_PATTERN = "; ";
    /**
     * A {@code String} that is the name of the attribute to retrieve the tax ID property.
     */
    private final static String TAX_ID_ATTRIBUTE = "taxid";
    /**
     * A {@code String} that is the name of the attribute to retrieve the tax range property.
     */
    private final static String TAX_RANGE_ATTRIBUTE = "TaxRange";
    
    /**
     * A {@code String} that is the name of the column to retrieve Xref IDs 
     * from the mapping TSV file.
     */
    public static final String XREF_ID_KEY = "xref ID";
    /**
     * A {@code String} that is the name of the column to retrieve gene IDs 
     * from the mapping TSV file.
     */
    public static final String GENE_ID_KEY = "gene ID";

    /**
     * An {@code int} that is a unique ID for each node inside an OMA Hierarchical
     * Orthologous Group. It's unique over all hierarchical groups.
     */
    private int omaNodeId;
    
    /**
     * An {@code int} that is the seed use to determine unique left and right bound values
     * of the nested set model of hierarchical groups.
     */
    private int nestedSetBoundSeed;
    
    /**
     * A {@code Set} of {@code HierarchicalNodeTO}s containing hierarchical groups to be
     * inserted into the Bgee database. See this method for details.
     * 
     * @see #generateTOsFromGroup()
     */
    private Set<HierarchicalNodeTO> hierarchicalNodeTOs;
    
    /**
     * A {@code Set} of {@code HierarchicalNodeTOGeneTO}s containing mapping between groups, taxonomy level
     * and genes to be inserted into the Bgee database. See this method for details.
     * 
     * @see #generateTOsFromGroup()
     */
    private Set<HierarchicalNodeToGeneTO> hierarchicalNodeToGeneTOs;
    
    /**
     * A {@code Set} of {@code GeneTO}s containing genes to be updated into the Bgee
     * database. See this method for details.
     * 
     * @see #generateTOsFromGroup()
     */
    private Set<GeneTO> geneTOs;
    
    /**
     * A {@code Set} of {@code String}s containing gene IDs of the Bgee database. See this
     * method for details.
     * 
     * @see #loadGeneIdsFromDb()
     */
    private Map<String, Integer> ensemblIdToBgeeIdInBgee;

    /**
     * A {@code Set} of {@code String}s containing taxon IDs of the Bgee database. See 
     * this method for details.
     * 
     * @see #loadTaxonIdsFromDb()
     */
    private Set<Integer> taxonIdsInBgee;

    /**
     * A {@code Set} of {@code String}s containing species IDs of the Bgee database. See 
     * this method for details.
     * 
     * @see #loadFakePrefixesFromDb()
     */
    private Set<Integer> speciesIdsInBgee;

    /**
     * A {@code Map} storing the mappings from genome species IDs to fake gene ID prefixes
     * of species using that genome. See this method for details.
     * 
     * @see #loadFakePrefixesFromDb()
     */
    private Map<Integer, Set<Integer>> speciesPrefixes;
    
    /**
     * A {@code Map} storing the mappings from gene IDs to the first OMA Node ID found in 
     * the file to be able to check if the gene is present in different HOGs. See this 
     * method for details.
     * 
     * see #addGeneTO()
     */
    private Map<Integer, String> genesUpdated;

    /**
     * Default constructor. 
     */
    public ParseOrthoXML() {
        this(null);
    }
    
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager   The {@code MySQLDAOManager} to use.
     */
    public ParseOrthoXML(MySQLDAOManager manager) {
        super(manager);
        this.omaNodeId = 1;
        this.nestedSetBoundSeed = 0;
        this.hierarchicalNodeTOs = new HashSet<HierarchicalNodeTO>();
        this.hierarchicalNodeToGeneTOs = new HashSet<HierarchicalNodeToGeneTO>();
        this.geneTOs = new HashSet<GeneTO>();
        this.ensemblIdToBgeeIdInBgee = new HashMap<String, Integer>();
        this.taxonIdsInBgee = new HashSet<Integer>();
        this.speciesIdsInBgee = new HashSet<Integer>();
        this.speciesPrefixes = new HashMap<Integer, Set<Integer>>();
        this.genesUpdated = new HashMap<Integer, String>();
    }

    /**
     * Main method to trigger the insertion of the hierarchical orthologous groups
     * obtained from OMA and the update of genes into the Bgee database. Parameters that
     * must be provided in order in {@code args} are:
     * <ol>
     * <li>path to the file storing the hierarchical orthologous groups in OrthoXML.
     * <li>path to the file storing the mapping between cross-reference and gene ID. 
     * The first line of this file should be a header line, defining 2 columns, 
     * named exactly as: {@link #GENE_ID_KEY}, {@link #XREF_ID_KEY} (in whatever order).
     * </ol>
     * 
     * @param args An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If the files used provided invalid information.
     * @throws DAOException             If an error occurred while getting or updating 
     *                                  the data into the Bgee database.
     * @throws XMLStreamException       If there is an error in the well-formedness of 
     *                                  the XML or other unexpected processing errors.
     * @throws XMLParseException        If there is an error in parsing the XML retrieved
     *                                  by the OrthoXMLReader.
     * @throws IOException              If the mapping file could not be read.
     */
    public static void main(String[] args) throws IllegalArgumentException, DAOException, 
            XMLStreamException, XMLParseException, IOException {
        log.entry((Object[]) args);
        
        int expectedArgLengthWithoutMapping = 1;
        int expectedArgLengthWithMapping = 2;
        if (args.length != expectedArgLengthWithoutMapping && 
            args.length != expectedArgLengthWithMapping) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of " +
                    "arguments provided, expected " + expectedArgLengthWithoutMapping + 
                    "or " + expectedArgLengthWithMapping + " arguments, " +
                    args.length + " provided."));
        }
    
        ParseOrthoXML parser = new ParseOrthoXML();
        if (args.length == expectedArgLengthWithoutMapping) {
            parser.parseXML(args[0], null);
        } else {
            parser.parseXML(args[0], args[1]);
        }
        
        log.exit();
    }

    /**
     * Performs the complete task of reading the Hierarchical Groups orthoxml file and
     * adding the data into the database.
     * <p>
     * First, it retrieves gene IDs from Bgee to be able to check if OMA genes are in Bgee
     * and to update OMAParentNodeId in gene table. Second, it retrieves taxon IDs to be
     * able to check if OMA HOGs correspond to taxa present in Bgee. Third, it retrieves
     * genomeSpeciesId-fakeGeneIdPrefix from Bgee to be able to duplicate genes with the
     * fake prefix when a species uses a genome of another species. Then, this method
     * reads the Hierarchical Orthologous Groups OrthoXML file, iterates through all the
     * orthologous groups present in the file and builds a {@code Collection} of
     * {@code HierarchicalNodeTO}s as a nested set model, and a {@code Collection} of
     * {@code GeneTO}s, in order to insert data into the OMAHierarchicalGroup table and to
     * update data in the gene table.
     * 
     * @param orthoXMLFile              A {@code String} that is the path to the OMA groups file.
     * @param geneMappingFile           A {@code String} that is the path to the gene mapping file.
     * @throws DAOException             If an error occurred while getting or updating the data
     *                                  into the Bgee database.
     * @throws XMLStreamException       If there is an error in the well-formedness of the XML
     *                                  or other unexpected processing errors.
     * @throws XMLParseException        If there is an error in parsing the XML retrieved by the
     *                                  OrthoXMLReader.
     * @throws IOException              If the mapping file could not be read.
     */
    public void parseXML(String orthoXMLFile, String geneMappingFile) 
        throws DAOException, XMLStreamException, XMLParseException, IOException {  
        log.entry(orthoXMLFile, geneMappingFile);
        log.info("Start parsing of OrthoXML file...");

        // First, if provided, we read mapping file save data in a map
        Map<String,String> geneMapping = new HashMap<String, String>();
        if (geneMappingFile != null) {
            geneMapping = this.readMappingFile(geneMappingFile);
        }
        // Catch any IllegalStateException to wrap it into a IllegalArgumentException 
        // (a IllegalStateException would be generated because the OrthoXML groups
        // loaded from the file would be invalid, so it would be a wrong argument).
        try {
            // Retrieve gene IDs of the Bgee database to be able to check if OMA genes are
            // in Bgee and to update OMAParentNodeId in gene table.
            this.loadGeneIdsFromDb();

            // Retrieve taxon IDs of the Bgee database to be able to check if OMA HOGs 
            // correspond to taxa present in Bgee.
            this.loadTaxonIdsFromDb();
            
            // Retrieve genomeSpeciesId from Bgee to be able to duplicate
            // genes with the fake prefix when a species using a genome of another species.
            this.loadFakeGenomeSpeciesFromDb();
            
            // Construct HierarchicalNodeTOs and GeneTOs
            this.generateTOsFromFile(orthoXMLFile, geneMapping);

            // Start a transaction to insert HierarchicalNodeTOs and update GeneTOs
            // in the Bgee data source. Note that we do not need to call rollback if
            // an error occurs, calling closeDAO will rollback any ongoing transaction.
            int nbInsertedGroups = 0, nbUpdatedGenes = 0, nbInsertedGroupToGene = 0;

            this.startTransaction(); 

            log.info("Start inserting of hierarchical groups...");
            nbInsertedGroups = this.getHierarchicalGroupDAO()
                    .insertHierarchicalNodes(this.hierarchicalNodeTOs);
            log.info("Done inserting hierarchical groups");

            log.info("Start updating genes...");
            nbUpdatedGenes = this.getGeneDAO().updateGenes(this.geneTOs,
                    Arrays.asList(GeneDAO.Attribute.OMA_PARENT_NODE_ID));
            log.info("Done updating genes.");
            System.out.println(this.hierarchicalNodeToGeneTOs.size());
            log.info("Start inserting gene to hierarchical group mapping...");
            nbInsertedGroupToGene = this.getHierarchicalGroupDAO()
            		.insertHierarchicalNodeToGene(this.hierarchicalNodeToGeneTOs);
            log.info("Done inserting gene to hierarchical group mapping.");

            this.commit();
            log.info("Done parsing of OrthoXML file: {} hierarchical groups inserted " +
                    ",{} genes updated, and {} mapping between hierarchical group and genes inserted.", nbInsertedGroups, nbUpdatedGenes, nbInsertedGroupToGene);
        } catch (IllegalStateException e) {
            log.catching(e);
            throw log.throwing(new IllegalArgumentException(
                    "The OrthoXML file provided is invalid", e));
        } finally {
            this.closeDAO();
        }
        log.exit();
    }

    /**
     * Extract the information about the gene mapping to include in Bgee from the provided 
     * TSV file. The first line of this file should be a header line, defining 2 columns, 
     * named exactly as: {@link #GENE_ID_KEY}, {@link #XREF_ID_KEY} (in whatever order). 
     *
     * @param geneMappingFile           A {@code String} that is the path to the gene mapping file.
     * @return                          The {@code Map} where keys are {@code String}s corresponding 
     *                                  to gene Xref Ids, the associated values being {@code String}s 
     *                                  corresponding to gene IDs. 
     * @throws IOException              If the mapping file could not be read.
     * @throws FileNotFoundException    If the file could not be found.
     */
    private Map<String, String> readMappingFile(String geneMappingFile) 
            throws FileNotFoundException, IOException {
        log.entry(geneMappingFile);
        
        Map<String, String> geneMapping = new HashMap<String, String>();
        try (ICsvMapReader reader = new CsvMapReader(new FileReader(geneMappingFile), 
                Utils.TSVCOMMENTED)) {
            String unexpectedFormat = "The provided TSV species file is not " +
                "in the expected format";

            // the header columns are used as the keys to the Map
            final String[] header = reader.getHeader(true);
            if (header.length != 2) {
                throw log.throwing(new IllegalArgumentException(unexpectedFormat));
            }
            
            CellProcessor[] processors = new CellProcessor[header.length];
            for (int i = 0; i < header.length; i++) {
                if (header[i].equalsIgnoreCase(GENE_ID_KEY) ||
                        header[i].equalsIgnoreCase(XREF_ID_KEY)) {
                    processors[i] = new StrNotNullOrEmpty();
                } else {
                    throw log.throwing(new IllegalArgumentException(unexpectedFormat));
                }
            }
                
            Map<String, Object> customerMap;
            while ((customerMap = reader.read(header, processors)) != null) {
                geneMapping.put(
                    (String) customerMap.get(XREF_ID_KEY), 
                    (String) customerMap.get(GENE_ID_KEY));
            }
        }
        
        if (geneMapping.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                "The provided TSV gene mapping file did not allow to acquire any mapping."));
        }

        return log.exit(geneMapping);
    }

    /**
     * Retrieves all gene IDs present into the Bgee database.
     * 
     * @throws DAOException     If an error occurred while getting the data from the Bgee
     *                          database.
     * @see #geneIdsInBgee
     */
    private void loadGeneIdsFromDb() throws DAOException {
        log.entry();
        
        log.info("Start retrieving gene IDs...");
        GeneDAO dao = this.getGeneDAO();
        
        //TODO return only 
        //dao.setAttributes(GeneDAO.Attribute.ID);
        try (GeneTOResultSet rsGenes = dao.getAllGenes()) {
            while (rsGenes.next()) {
            	if(this.ensemblIdToBgeeIdInBgee.containsKey(rsGenes.getTO().getGeneId())){
            		log.error("Two bgeeGeneIds have the same ID :{}",rsGenes.getTO().getGeneId());
            	}
                this.ensemblIdToBgeeIdInBgee.put(rsGenes.getTO().getGeneId(), rsGenes.getTO().getId());
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Done retrieving gene IDs, {} genes found", this.ensemblIdToBgeeIdInBgee.size());
        }

        log.exit();
    }

    /**
     * Retrieves all taxon IDs present into the Bgee database.
     * 
     * @throws DAOException     If an error occurred while getting the data from the Bgee
     *                          database.
     * @see #taxonIdsInBgee
     */
    private void loadTaxonIdsFromDb() throws DAOException {
        log.entry();
        
        log.info("Start retrieving taxon IDs...");
        TaxonDAO dao = this.getTaxonDAO();
        //dao.setAttributes(TaxonDAO.Attribute.ID);
        try (TaxonTOResultSet rsTaxa = dao.getAllTaxa()) {
            while (rsTaxa.next()) {
                this.taxonIdsInBgee.add(rsTaxa.getTO().getId());
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Done retrieving taxon IDs, {} taxa found", this.taxonIdsInBgee.size());
        }
    
        log.exit();
    }

    /**
     * Retrieves genome species ID of species using a genome of another species.
     * 
     * @throws DAOException             If an error occurred while getting the data from the Bgee
     *                                  database.
     * @throws IllegalArgumentException If there is a fake gene ID prefix is empty.
     * @see #speciesPrefixes                
     */
    private void loadFakeGenomeSpeciesFromDb() throws DAOException, IllegalArgumentException {
        log.entry();
        
        log.info("Start retrieving fake gene ID prefixes...");
        SpeciesDAO speciesDAO = this.getSpeciesDAO();
        //TODO manage attributs
//        speciesDAO.setAttributes(SpeciesDAO.Attribute.ID, SpeciesDAO.Attribute.COMMON_NAME, 
//                SpeciesDAO.Attribute.GENOME_SPECIES_ID, SpeciesDAO.Attribute.FAKE_GENE_ID_PREFIX);
        
        try (SpeciesTOResultSet rsSpecies = speciesDAO.getAllSpecies()) {
            while (rsSpecies.next()) {
                SpeciesTO speciesTO = rsSpecies.getTO();
                this.speciesIdsInBgee.add(speciesTO.getId());
                log.debug("{} - {}",speciesTO.getGenus()+" "+speciesTO.getSpeciesName(), speciesTO.getGenomeSpeciesId());
                if (!speciesTO.getGenomeSpeciesId().equals(NumberUtils.INTEGER_ZERO) &&
                        !speciesTO.getId().equals(speciesTO.getGenomeSpeciesId())) {
                    int genomeSpeciesId = speciesTO.getGenomeSpeciesId();
                    if (this.speciesPrefixes.get(genomeSpeciesId) == null) {
                        this.speciesPrefixes.put(
                                speciesTO.getGenomeSpeciesId(),
                                new HashSet<Integer>());
                    }
                    this.speciesPrefixes.get(genomeSpeciesId).add(speciesTO.getId());

                    log.debug("Use genome of species {} for species {}", speciesTO.getId()+" "+speciesTO.getSpeciesName(), 
                            speciesTO.getId());
                }
            }
        }
        log.debug("Association betweeen species with fake genomes and their fake " +
                  "geneId prefix: {}", this.speciesPrefixes);
        if (log.isInfoEnabled()) {
            log.info("Done retrieving fake gene ID prefixes, {} genomes found",
                    this.speciesPrefixes.size());
        }
        log.exit();
    }

    /**
     * Extract all relevant information from the OrthoXML file. This method computes 
     * a nested set model of the OMA groups, then generates a {@code Collection} 
     * of {@code HierarchicalNodeTO}s and a {@code Collection} of {@code GeneTO}s 
     * to store information into the database.
     * 
     * @param orthoXMLFile              A {@code String} that is the path to the OMA group file  
     *                                  which data will be retrieved from.
     * @param geneMappingFile           A {@code String} that is the path to the gene mapping file.
     * @throws FileNotFoundException    If some files could not be found.
     * @throws XMLStreamException       If there is an error in the well-formedness of the 
     *                                  XML or other unexpected processing errors.
     * @throws XMLParseException        If there is an error in parsing the XML retrieved
     *                                  by the OrthoXMLReader.
     */
    private void generateTOsFromFile(String orthoXMLFile, Map<String,String> geneMapping)
        throws FileNotFoundException,
            XMLStreamException, XMLParseException {
        log.entry(orthoXMLFile, geneMapping);
        OrthoXMLReader reader = new OrthoXMLReader(new File(orthoXMLFile));
        List<Species> speciesInFile = reader.getSpecies();
        List<Integer> speciesIdsInFile = new ArrayList<Integer>();  
        for (Species species : speciesInFile) {
            speciesIdsInFile.add(species.getNcbiTaxId());
        }
        // Common species
        List<Integer> common = new ArrayList<Integer>(this.speciesIdsInBgee);
        common.retainAll(speciesIdsInFile);
        if (!common.isEmpty()) {
            log.trace("The common species between Bgee and OMA file species are: {}", common);
        } else {
            throw log.throwing(new IllegalArgumentException(
                    "There is no common species between Bgee and OMA file species."));
        }
        
        // Species in Bgee but not in provided OMA file
        List<Integer> speciesBgeeSpecific = new ArrayList<Integer>(this.speciesIdsInBgee);
        speciesBgeeSpecific.removeAll(speciesIdsInFile);
        if (!speciesBgeeSpecific.isEmpty()) {
            log.trace("The species specific to Bgee are: {}", speciesBgeeSpecific);
        }
        
        // Species in provided OMA file but not in Bgee
        List<Integer> speciesOMASpecific = new ArrayList<Integer>(speciesIdsInFile);
        speciesOMASpecific.removeAll(this.speciesIdsInBgee);
        if (!speciesOMASpecific.isEmpty()) {
            log.trace("The species specific to OMA file are: {}", speciesOMASpecific);
        }

        // Read all the groups in the file iteratively
        Group group = null;
        while ((group = reader.next()) != null) {
            this.generateTOsFromGroup(group, group.getId(), geneMapping);
            // We increment the nestedSetBoundSeed because we move to the next OMA group.
            this.nestedSetBoundSeed++;
        }
        log.info("Done retrieving hierarchical groups.");
        log.exit();
    }
    
    /**
     * Extract all relevant information from a {@code Group} and create a 
     * {@code Collection} of {@code HierarchicalNodeTO}s as a nested set model, and a
     * {@code Collection} of {@code GeneTO}s.
     * 
     * @param group             A {@code Group} that is the OMA group which data will be 
     *                          retrieved.
     * @param omaXrefId         A {@code String} that the OMA cross-reference ID to use for
     *                          subgroups.
     * @param geneMappingFile   A {@code String} that is the path to the gene mapping file.
     * @return                  A {@code boolean} that is {@code true} if one
     *                          {@code HierarchicalNodeTO} has been added representing 
     *                          the given {@code Group}. 
     */
    private boolean generateTOsFromGroup(Group group, String omaXrefId, 
                    Map<String,String> geneMapping) {
        log.entry(group, omaXrefId, geneMapping);
        // First, we check if the group represents a taxon presents in Bgee or if it's a 
        // paralog group. If wrong, we don't insert a hierarchical groupTO.
        String groupTaxId = group.getProperty(TAX_ID_ATTRIBUTE);
        if (groupTaxId != null && !this.taxonIdsInBgee.contains(Integer.parseInt(groupTaxId))) {
            log.warn("{} ({}) isn't a taxon relevant to Bgee",
                    group.getProperty(TAX_RANGE_ATTRIBUTE), groupTaxId);
            return log.exit(false);
        } 

        // Second, we increment the nestedSetBoundSeed because we will create a new 
        // hierarchical group
        this.nestedSetBoundSeed++;

        // We add a HierarchicalNodeTO in collection containing hierarchical groups to be 
        // inserted into the Bgee database
        // The last argument is the number of children of the HierarchicalNodeTO to create. 
        // So, we need to remove 1 to countGroups() to subtract the current group.
        this.addHierarchicalNodeTO(this.omaNodeId, omaXrefId, this.nestedSetBoundSeed,
                group.getProperty(TAX_ID_ATTRIBUTE), countGroups(group) - 1);
    	this.addHierarchicalNodeTOGeneTO(
    			group.getProperty(TAX_ID_ATTRIBUTE), group.getNestedGenes(),
    			this.omaNodeId);
        // Then, we retrieve gene data.
        if (group.getGenes() != null) {
            log.debug("Retrieving genes from group {}", group);
            for (sbc.orthoxml.Gene groupGene : group.getGenes()) {
                log.debug("Retrieving gene with identifier {}", groupGene.getGeneIdentifier());
                boolean isInBgee = false ;
                for (String omaGeneId : retrieveSplittedGeneIdentifier(groupGene)) {
                    log.debug("Examining OMA geneId {}", omaGeneId);
                    // we need to copy the gene ID to be able to modify it 
                    // if geneId is not in bgee, and use it for generate fake geneId 
                    String currentGeneId = omaGeneId;
                    if (this.addGeneTO(new GeneTO(ensemblIdToBgeeIdInBgee.get(omaGeneId),omaGeneId, null, null, null, null, 
                            this.omaNodeId,null, null),
                            omaXrefId)) {
                        isInBgee = true;
                    } else if (!geneMapping.isEmpty()) {
                        log.debug("Trying to find a x-ref for geneId {} from mapping file", omaGeneId);
                        if (geneMapping.containsKey(omaGeneId)) {
                            currentGeneId = geneMapping.get(omaGeneId);
                            log.debug("Mapping found for geneId {}: {}", omaGeneId, currentGeneId);
                            if (this.addGeneTO(new GeneTO(ensemblIdToBgeeIdInBgee.get(currentGeneId), currentGeneId, null, null, null, null, 
                                    this.omaNodeId, null, null), omaXrefId)) {
                                isInBgee = true;
                            }
                        }
                    }
                }
                if (!isInBgee) {
                    log.warn("No gene ID in {} found in Bgee for the node {}",
                            groupGene.getGeneIdentifier(), this.omaNodeId);
                }
            }
        }

        // Incrementing the node ID. Done after to be able to set OMA parent node ID
        // into gene table
        this.omaNodeId++;
        
        if (group.getChildren() != null && group.getChildren().size() > 0) {
            for (Group childGroup : group.getChildren()) {
                // Recurse
                if (generateTOsFromGroup(childGroup, omaXrefId, geneMapping)) {
                    // We increment the nestedSetBoundSeed because we are at a leaf of the 
                    // nested set model
                    this.nestedSetBoundSeed++;
                }
            }
        }
        return log.exit(true);
    }

    /**
     * Retrieves the split {@code String} of OMA Gene Identifier.
     * 
     * @param gene   A {@code Gene} that is the OrthoXML gene gene whose gene identifier 
     *               will be split.  
     * @return       A {@List} of {@code String}s containing all gene IDs of the provided 
     *               OrthoXML {@code Gene}.
     */
    private List<String> retrieveSplittedGeneIdentifier(sbc.orthoxml.Gene gene) {
        log.entry();
        return log.exit(Arrays.asList(gene.getGeneIdentifier().split(GENE_ID_SPLIT_PATTERN)));
    }

    /**
     * Given a OMA cross-reference ID with the taxonomy id and a number of children, 
     * calculate nested set bounds and add it in the {@code Collection} of 
     * {@code HierarchicalNodeTO}s to be as a nested set model.
     * 
     * @param omaNodeId             An {@code int} that is the unique ID the hierarchical
     *                              group.
     * @param omaXrefId             A {@code String} that is the OMA cross-reference ID.
     * @param nestedSetBoundSeed    An {@code int} that is the seed use to determine 
     *                              unique left and right bound values of the nested set 
     *                              model of the hierarchical group.
     * @param taxId                 An {@code int} that is the taxonomy id of the
     *                              {@code HierarchicalNodeTO} to create.
     * @param nbChild               An {@code int} that is the number of children of the
     *                              {@code HierarchicalNodeTO} to create.
     */
    private void addHierarchicalNodeTO(int omaNodeId, String omaXrefId, 
            int nestedSetBoundSeed, String taxId, int nbChild) {
        log.entry(omaNodeId, omaXrefId, nestedSetBoundSeed, taxId, nbChild);
        // Left
        int left = nestedSetBoundSeed;
        // Right = left + 2 * number of children + 1;
        int right = nestedSetBoundSeed + 2 * nbChild + 1;
        this.hierarchicalNodeTOs.add(
                new HierarchicalNodeTO(this.omaNodeId, omaXrefId, left, right, 
                        (taxId == null ? 0: Integer.valueOf(taxId)))); 
        log.exit();
    }
    
    /**
<<<<<<< Updated upstream
     * Given a OMA cross-reference ID with the taxonomy id and a number of children, 
     * calculate nested set bounds and add it in the {@code Collection} of 
     * {@code HierarchicalNodeTO}s to be as a nested set model.
=======
     * Given a taxonId, an OMA node Id and a list of genes create a {@code Collection} of 
     * {@code HierarchicalNodeToGeneTO}s.
>>>>>>> Stashed changes
     * 
     * @param taxonId               An {@code int} that is the taxonomy id of the
     *                              {@code HierarchicalNodeTO} to create.
     * @param genes                 A {@code List} of {@code Gene} corresponding to all orthologous
     *                              genes of this OMA node Id
     * @param omaNodeId             An {@code int} that is the unique ID the hierarchical
     *                              group.
<<<<<<< Updated upstream
     * @param omaXrefId             A {@code String} that is the OMA cross-reference ID.
     * @param nestedSetBoundSeed    An {@code int} that is the seed use to determine 
     *                              unique left and right bound values of the nested set 
     *                              model of the hierarchical group.
     * @param taxId                 An {@code int} that is the taxonomy id of the
     *                              {@code HierarchicalNodeTO} to create.
     * @param nbChild               An {@code int} that is the number of children of the
     *                              {@code HierarchicalNodeTO} to create.
=======
>>>>>>> Stashed changes
     */
    private void addHierarchicalNodeTOGeneTO(String taxonId, List<Gene> genes, Integer OmaNodeId) {
        log.entry(taxonId, OmaNodeId, genes);
        if(taxonId != null){
            genes.stream().forEach(g -> {
            	if(ensemblIdToBgeeIdInBgee.get(g.getGeneIdentifier()) != null){
<<<<<<< Updated upstream
//                	System.out.println(taxonId+" -> "+ensemblIdToBgeeIdInBgee.get(g.getGeneIdentifier())+" -> "+OmaNodeId);
            		this.hierarchicalNodeToGeneTOs.add(new HierarchicalNodeToGeneTO(
=======

            		this.hierarchicalGroupToGeneTOs.add(new HierarchicalGroupToGeneTO(
>>>>>>> Stashed changes
            				OmaNodeId, ensemblIdToBgeeIdInBgee.get(g.getGeneIdentifier()), 
    	        			Integer.valueOf(taxonId)));
            	}
            });
        }
    }

    /**
     * Given a {@code GeneTO} and an OMA cross-reference ID, add the {@code GeneTO} in the 
     * {@code Collection} of {@code GeneTO}s to be update.
     * <p>
     * If the given {@code GeneTO} has already been inserted in the {@code Collection} 
     * with another OMA cross-reference ID, ​​a warning will be generated.
     * 
     * @param geneTO        A {@code GeneTO} to add. 
     * @param omaXrefId     A {@code String} that is the OMA cross-reference ID.
     * @return              A {@code boolean} that is {@code true} if the given 
     *                      {@code GeneTO} has been added to the {@code Collection} of 
     *                      {@code GeneTO}s to be update 
     */
    private boolean addGeneTO(GeneTO geneTO, String omaXrefId) {
        log.entry(geneTO, omaXrefId);
        
        if (!this.ensemblIdToBgeeIdInBgee.containsKey(geneTO.getGeneId())) {
            log.debug("Gene discarded because not in Bgee: {}", geneTO.getGeneId());
            return log.exit(false);
        }
        
        if (this.genesUpdated.containsKey(geneTO.getGeneId())
                && !this.genesUpdated.get(geneTO.getId()).equals(omaXrefId)) {
            log.warn("The gene {} is in different hierarchical orthologous groups: " +
                    "/{}/ and /{}/", geneTO.getGeneId(), this.genesUpdated.get(geneTO.getGeneId()),
                    omaXrefId);
            return log.exit(false); 
        } 
        
        this.genesUpdated.put(geneTO.getId(), omaXrefId);
        // Add new {@code GeneTO} to {@code Collection} of {@code GeneTO}s to be able 
        // to update omaXrefId in gene table.
        this.geneTOs.add(geneTO);
        return log.exit(true);
    }

    /**
     * Count the number of {@code Group}s in the provided {@code Group}.
     * <p>
     * This method recursively iterates through all subgroups of {@code Group} passed as a
     * parameter and returns the total number of {@code Group}s of the parameter
     * including the provided {@code Group}.
     * 
     * @param group The {@code Group} whose the number of {@code Group}s are to be counted.
     * @return      An {@code int} giving the total number of {@code Group}s in the given
     *              {@code Group} including the provided {@code Group}.
     */
    private int countGroups(Group group) {
        log.entry(group);
        String groupTaxId = group.getProperty(TAX_ID_ATTRIBUTE);
        // We check if the group represents a taxon presents Bgee or if it's a paralog group.
        // If wrong, no hierarchical group is inserted, so, there is no group to count.
        if (groupTaxId != null && !this.taxonIdsInBgee.contains(Integer.parseInt(groupTaxId))) {
            return log.exit(0);
        }
        int c = 1;
        for (Group childGroup : group.getChildren()) {
            c += this.countGroups(childGroup);
        }
        return log.exit(c);
    }
}
