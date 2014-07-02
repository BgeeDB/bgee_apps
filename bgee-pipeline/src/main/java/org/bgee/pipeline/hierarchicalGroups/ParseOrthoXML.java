package org.bgee.pipeline.hierarchicalGroups;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.modelmbean.XMLParseException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupTO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.api.species.TaxonDAO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTOResultSet;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.MySQLDAOUser;

import sbc.orthoxml.Group;
import sbc.orthoxml.io.OrthoXMLReader;

/**
 * This class parses the OrthoXML file which contains all the data of the hierarchical 
 * orthologous groups obtained from OMA. It retrieves all the data pertaining to the
 * orthologous genes.
 * 
 * @author Komal Sanjeev
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public class ParseOrthoXML extends MySQLDAOUser {

    private final static Logger log = LogManager.getLogger(ParseOrthoXML.class.getName());

    /**
     * A {@code String} that is the pattern use to split OMA Gene Identifiers.
     */
    private final static String GENE_ID_SPLIT_PATTERN = "; ";

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
     * A {@code Set} of {@code HierarchicalGroupTO}s containing hierarchical groups to be
     * inserted into the Bgee database. See this method for details.
     * 
     * @see #generateTOsFromGroup()
     */
    private Set<HierarchicalGroupTO> hierarchicalGroupTOs;
    
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
    private Set<String> geneIdsInBgee;

    /**
     * A {@code Set} of {@code String}s containing taxon IDs of the Bgee database. See 
     * this method for details.
     * 
     * @see #loadTaxonIdsFromDb()
     */
    private Set<String> taxonIdsInBgee;

    /**
     * A {@code Map} storing the mappings from genome species IDs to fake gene ID prefixes
     * of species using that genome. See this method for details.
     * 
     * @see #loadFakePrefixesFromDb()
     */
    private Map<Integer, Set<String>> speciesPrefixes;
    
    /**
     * A {@code Map} storing the mappings from gene IDs to the first OMA Node ID found in 
     * the file to be able to check if the gene is present in different HOGs. See this 
     * method for details.
     * 
     * see #addGeneTO()
     */
    private Map<String, String> genesUpdated;

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
        this.hierarchicalGroupTOs = new HashSet<HierarchicalGroupTO>();
        this.geneTOs = new HashSet<GeneTO>();
        this.geneIdsInBgee = new HashSet<String>();
        this.taxonIdsInBgee = new HashSet<String>();
        this.speciesPrefixes = new HashMap<Integer, Set<String>>();
        this.genesUpdated = new HashMap<String, String>();
    }

    /**
     * Main method to trigger the insertion of the hierarchical orthologous groups
     * obtained from OMA and the update of genes into the Bgee database. Parameters that
     * must be provided in order in {@code args} are:
     * <ol>
     * <li>path to the file storing the hierarchical orthologous groups in OrthoXML.
     * </ol>
     * 
     * @param args An {@code Array} of {@code String}s containing the requested parameters.
     * @throws FileNotFoundException    If some files could not be found.
     * @throws IllegalArgumentException If the files used provided invalid information.
     * @throws DAOException             If an error occurred while getting or updating 
     *                                  the data into the Bgee database.
     * @throws XMLStreamException       If there is an error in the well-formedness of 
     *                                  the XML or other unexpected processing errors.
     * @throws XMLParseException        If there is an error in parsing the XML retrieved
     *                                  by the OrthoXMLReader.
     */
    public static void main(String[] args) throws FileNotFoundException, 
            IllegalArgumentException, DAOException, 
            XMLStreamException, XMLParseException {
        log.entry((Object[]) args);
        
        int expectedArgLength = 1;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of " +
                    "arguments provided, expected " + expectedArgLength + " arguments, " +
                    args.length + " provided."));
        }
    
        ParseOrthoXML parser = new ParseOrthoXML();
        parser.parseXML(args[0]);
        
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
     * {@code HierarchicalGroupTO}s as a nested set model, and a {@code Collection} of
     * {@code GeneTO}s, in order to insert data into the OMAHierarchicalGroup table and to
     * update data in the gene table.
     * 
     * @param orthoXMLFile A {@code String} that is the path to the OMA groups file.
     * @throws DAOException If an error occurred while getting or updating the data into
     *             the Bgee database.
     * @throws FileNotFoundException If some files could not be found.
     * @throws XMLStreamException If there is an error in the well-formedness of the XML
     *             or other unexpected processing errors.
     * @throws XMLParseException If there is an error in parsing the XML retrieved by the
     *             OrthoXMLReader.
     */
    public void parseXML(String orthoXMLFile) throws DAOException, FileNotFoundException, 
            XMLStreamException, XMLParseException {  
        log.entry();
        log.info("Start parsing of OrthoXML file...");

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
            
            // Retrieve genomeSpeciesId-fakeGeneIdPrefix from Bgee to be able to duplicate
            // genes with the fake prefix when a species using a genome of  another species.
            this.loadFakePrefixesFromDb();
            
            // Construct HierarchicalGroupTOs and GeneTOs
            this.generateTOsFromFile(orthoXMLFile);

            // Start a transaction to insert HierarchicalGroupTOs and update GeneTOs
            // in the Bgee data source. Note that we do not need to call rollback if
            // an error occurs, calling closeDAO will rollback any ongoing transaction.
            int nbInsertedGroups = 0, nbUpdatedGenes = 0;

            this.startTransaction(); 

            log.info("Start inserting of hierarchical groups...");
            nbInsertedGroups = this.getHierarchicalGroupDAO()
                    .insertHierarchicalGroups(hierarchicalGroupTOs);
            log.info("Done inserting hierarchical groups");

            log.info("Start updating genes...");
            nbUpdatedGenes = this.getGeneDAO().updateGenes(geneTOs,
                    Arrays.asList(GeneDAO.Attribute.OMAPARENTNODEID));
            log.info("Done updating genes.");

            this.commit();
            log.info("Done parsing of OrthoXML file: {} hierarchical groups inserted " +
                    "and {} genes updated.", nbInsertedGroups, nbUpdatedGenes);
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
        dao.setAttributes(GeneDAO.Attribute.ID);
        GeneTOResultSet rsGenes = dao.getAllGenes();
        while (rsGenes.next()) {
            this.geneIdsInBgee.add(rsGenes.getTO().getId());
        }
        if (log.isInfoEnabled()) {
            log.info("Done retrieving gene IDs, {} genes found", geneIdsInBgee.size());
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
        dao.setAttributes(TaxonDAO.Attribute.ID);
        TaxonTOResultSet rsTaxa = dao.getAllTaxa();
        while (rsTaxa.next()) {
            TaxonTO t = rsTaxa.getTO();
            this.taxonIdsInBgee.add(t.getId());
        }
        if (log.isInfoEnabled()) {
            log.info("Done retrieving taxon IDs, {} taxa found", taxonIdsInBgee.size());
        }
    
        log.exit();
    }

    /**
     * Retrieves genome species ID and fake gene ID prefixes of species using a genome of 
     * another species.
     * 
     * @throws DAOException     If an error occurred while getting the data from the Bgee
     *                          database.
     * @see #speciesPrefixes                
     */
    private void loadFakePrefixesFromDb() throws DAOException {
        log.entry();
        
        log.info("Start retrieving fake gene ID prefixes...");
        SpeciesDAO speciesDAO = this.getSpeciesDAO();
        speciesDAO.setAttributes(SpeciesDAO.Attribute.ID, 
                SpeciesDAO.Attribute.FAKE_GENE_ID_PREFIX);
        SpeciesTOResultSet rsSpecies = speciesDAO.getAllSpecies();
        while (rsSpecies.next()) {
            SpeciesTO speciesTO = rsSpecies.getTO();
            log.debug(speciesTO.getName()+" - "+speciesTO.getGenomeSpeciesId()+" - "+speciesTO.getFakeGeneIdPrefix());
            if (StringUtils.isNotBlank(speciesTO.getGenomeSpeciesId()) && 
                    !speciesTO.getId().equals(speciesTO.getGenomeSpeciesId())) {
                int genomeSpeciesId = Integer.parseInt(speciesTO.getGenomeSpeciesId());
                if (speciesPrefixes.get(genomeSpeciesId) == null) {
                    this.speciesPrefixes.put(Integer.parseInt(speciesTO.getGenomeSpeciesId()),
                            new HashSet<String>());
                }
                this.speciesPrefixes.get(genomeSpeciesId).add(speciesTO.getFakeGeneIdPrefix());
                
                log.debug("Added fake gene ID prefix {} for species {}, using genome of species {}", 
                        speciesTO.getFakeGeneIdPrefix(), speciesTO.getId(), 
                        speciesTO.getGenomeSpeciesId());
            }
        }
        log.debug("Association betweeen species with fake genomes and their fake " +
                  "geneId prefix: {}", this.speciesPrefixes);
        if (log.isInfoEnabled()) {
            log.info("Done retrieving fake gene ID prefixes, {} genomes found",
                    speciesPrefixes.size());
        }
        log.exit();
    }

    /**
     * Extract all relevant information from the OrthoXML file. This method computes 
     * a nested set model of the OMA groups, then generates a {@code Collection} 
     * of {@code HierarchicalGroupTO}s and a {@code Collection} of {@code GeneTO}s 
     * to store information into the database.
     * 
     * @param orthoXMLFile  A {@code String} that is the path to the OMA group file which 
     *                      data will be retrieved from.
     * @throws FileNotFoundException    If some files could not be found.
     * @throws XMLStreamException       If there is an error in the well-formedness of the 
     *                                  XML or other unexpected processing errors.
     * @throws XMLParseException        If there is an error in parsing the XML retrieved
     *                                  by the OrthoXMLReader.
     */
    private void generateTOsFromFile(String orthoXMLFile) throws FileNotFoundException,
            XMLStreamException, XMLParseException {
        log.entry(orthoXMLFile);
        OrthoXMLReader reader = new OrthoXMLReader(new File(orthoXMLFile));
        Group group;
        // Read all the groups in the file iteratively
        while ((group = reader.next()) != null) {
            this.generateTOsFromGroup(group, group.getId());
            // We increment the nestedSetBoundSeed because we are at a leaf of the 
            // nested set model
            this.nestedSetBoundSeed++;
        }
        log.info("Done retrieving hierarchical groups.");
        log.exit();
    }
    
    /**
     * Extract all relevant information from a {@code Group} and create a 
     * {@code Collection} of {@code HierarchicalGroupTO}s as a nested set model, and a
     * {@code Collection} of {@code GeneTO}s.
     * 
     * @param group         A {@code Group} that is the OMA group which data will be 
     *                      retrieved.
     * @param omaXrefId     A {@code String} that the OMA cross-reference ID to use for
     *                      subgroups.
     */
    private void generateTOsFromGroup(Group group, String omaXrefId) {
        log.entry(group, omaXrefId);

        // First, we increment the nestedSetBoundSeed because we will create a new 
        // hierarchical group
        this.nestedSetBoundSeed++;

        // Then, we add a HierarchicalGroupTO in collection containing hierarchical groups
        // to be inserted into the Bgee database.
        // The last argument is the number of children of the HierarchicalGroupTO to create. 
        // So, we need to remove 1 to countGroups() to subtract the current group.
        
        //TODO check if species or taxon
        
        //TODO: shouldn't it be added only if it is a taxon relevant to Bgee?
        
        //      what we do with gene of a taxon unrelevant to Bgee?
        this.addHierarchicalGroupTO(omaNodeId, omaXrefId, this.nestedSetBoundSeed,
                group.getProperty("TaxId"), countGroups(group) - 1);

        if (group.getGenes() != null) {
            log.debug("Retrieving genes from group {}", group);
            for (sbc.orthoxml.Gene groupGene : group.getGenes()) {
                log.debug("Retrieving gene {} with identifier {}", groupGene, 
                        groupGene.getGeneIdentifier());
                List<String> geneIds = retrieveSplittedGeneIdentifier(groupGene);
                boolean isInBgee = false ;
                for (String geneId : geneIds) {
                    log.debug("Examining OMA geneId {}", geneId);
                    int taxId = groupGene.getSpecies().getNcbiTaxId();
                    if (geneIdsInBgee.contains(geneId)) {
                        isInBgee = true;
                        this.addGeneTO(new GeneTO(geneId, "", "", 0, 0, omaNodeId, true),
                                omaXrefId);
                    }
                    // Check if this taxon corresponds to a species for which we are using 
                    //the genome of another species
                    if (this.speciesPrefixes.containsKey(taxId)) {
                        // Change prefix of the gene to create a fake gene IDs of other
                        // species using this genome.
                        Matcher m = Pattern.compile("^([A-Za-z]+)(\\d.+)").matcher(geneId);
                        if (m.matches()) {
                            for (String newGeneId: this.speciesPrefixes.get(taxId)) {
                                String duplicateId = newGeneId + m.group(2);
                                log.debug("Generating fake geneId from {} to {}, " +
                                          "because belonging to species {}", 
                                          geneId, duplicateId, taxId);
                                if (this.addGeneTO(new GeneTO(duplicateId, "", "", 0, 0,
                                        omaNodeId, true), omaXrefId)) {
                                    isInBgee = true;
                                }
                            }
                        }
                    }
                    if (this.addGeneTO(new GeneTO(geneId, "", "", 0, 0, omaNodeId, true),
                            omaXrefId)) {
                        isInBgee = true;
                    }
                }
                if (!isInBgee) {
                    log.warn("No gene ID in {} found in Bgee for the node {}",
                            groupGene.getGeneIdentifier(), omaNodeId);
                }
            }
        }
        // Incrementing the node ID. Done after to be able to set OMA parent node ID
        // into gene table
        omaNodeId++;
        if (group.getChildren() != null && group.getChildren().size() > 0) {
            for (Group childGroup : group.getChildren()) {
                // Recurse
                generateTOsFromGroup(childGroup, omaXrefId);
                this.nestedSetBoundSeed++;
            }
        }
        log.exit();
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
     * {@code HierarchicalGroupTO}s to be as a nested set model.
     * 
     * @param omaNodeId             An {@code int} that is the unique ID the hierarchical
     *                              group.
     * @param omaXrefId             A {@code String} that is the OMA cross-reference ID.
     * @param nestedSetBoundSeed    An {@code int} that is the seed use to determine 
     *                              unique left and right bound values of the nested set 
     *                              model of the hierarchical group.
     * @param taxId                 An {@code int} that is the taxonomy id of the
     *                              {@code HierarchicalGroupTO} to create.
     * @param nbChild               An {@code int} that is the number of children of the
     *                              {@code HierarchicalGroupTO} to create.
     */
    private void addHierarchicalGroupTO(int omaNodeId, String omaXrefId, 
            int nestedSetBoundSeed, String taxId, int nbChild) {
        log.entry(omaXrefId, taxId, nbChild);
        // Left
        int left = nestedSetBoundSeed;
        // Right = left + 2 * number of children + 1;
        int right = nestedSetBoundSeed + 2 * nbChild + 1;
        this.hierarchicalGroupTOs.add(
                new HierarchicalGroupTO(omaNodeId, omaXrefId, left, right, 
                        (taxId == null ? 0: Integer.valueOf(taxId)))); 
        log.exit();
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
     */
    private boolean addGeneTO(GeneTO geneTO, String omaXrefId) {
        log.entry(geneTO, omaXrefId);
        
        if (!this.geneIdsInBgee.contains(geneTO.getId())) {
            log.debug("Gene discarded because not in Bgee: {}", geneTO.getId());
            return log.exit(false);
        }
        
        if (genesUpdated.containsKey(geneTO.getId())
                && !genesUpdated.get(geneTO.getId()).equals(omaXrefId)) {
            log.warn("The gene {} is in diffent hierarchical orthologous groups: " +
                    "/{}/ and /{}/", geneTO.getId(), genesUpdated.get(geneTO.getId()),
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
    private static int countGroups(Group group) {
        log.entry(group);
        int c = 1;
        for (Group childGroup : group.getChildren()) {
            c += countGroups(childGroup);
        }
        return log.exit(c);
    }
}
