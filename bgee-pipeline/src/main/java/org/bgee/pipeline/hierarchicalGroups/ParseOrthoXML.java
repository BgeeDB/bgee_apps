package org.bgee.pipeline.hierarchicalGroups;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    
    
    //TODO: I used to initiliatize attributes in constructor. No solution better than another.
    
    //TODO: javadoc
    //TODO: why is it static??
    //TODO: attributes don't start with a upper case letter
    private static int OMANodeId = 1;
    //TODO: javadoc
    private int nestedSetId = 0;
    
    // A Set of HierarchicalGroupTOs to insert into the Bgee database
    //TODO: javadoc
    private Set<HierarchicalGroupTO> hierarchicalGroupTOs = 
            new HashSet<HierarchicalGroupTO>();
    // A Set of GeneTOs to update into the Bgee database
    //TODO: javadoc
    private Set<GeneTO> geneTOs = new HashSet<GeneTO>();
    
    // A Set of geneId presents into the Bgee database
    //TODO: javadoc
    private Set<String> geneIdsInBgee = new HashSet<String>();
    // A Map of genomeSpeciesId-fakeGeneIdPrefix to be able to duplicate genes with the
    // fake prefix when a species using a genome of another species.
    //TODO: javadoc
    //TODO: what if the genome od a species is used for several other species? => Map<Integer, Set<String>>
    private Map<Integer, String> speciesPrefixes = new HashMap<Integer, String>();
    
    // A Map of geneId-OMANodeID to be able to check if the gene is present in two
    // different HOGs
    //TODO: javadoc
    private Map<String, String> genesUpdated = new HashMap<String, String>();

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
        //TODO: initialize class attributes (and declare them final?)
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
     * First, it retrieves genes id from Bgee to be able to check if OMA genes are in Bgee 
     * and to update OMAParentNodeId in gene table. Second it retrieves 
     * genomeSpeciesId-fakeGeneIdPrefix from Bgee to be able to duplicate genes with the
     * fake prefix when a species uses a genome of another species. Then, this method
     * reads the Hierarchical Orthologous Groups OrthoXML file, iterates through all the 
     * orthologous groups present in the file and builds a {@code Collection} of 
     * {@code HierarchicalGroupTO}s as a nested set model, and a {@code Collection} of 
     * {@code GeneTO}s, in order to inset data into the OMAHierarchicalGroup table and  
     * to update data in the gene table.
     * 
     * @param orthoXMLFile      A {@code String} that is the path to the OMA groups file.
     * @throws DAOException             If an error occurred while getting or updating the 
     *                                  data into the Bgee database.
     * @throws FileNotFoundException    If some files could not be found.
     * @throws XMLStreamException       If there is an error in the well-formedness of the
     *                                  XML or other unexpected processing errors.
     * @throws XMLParseException        If there is an error in parsing the XML retrieved
     *                                  by the OrthoXMLReader.
     */
    public void parseXML(String orthoXMLFile) throws DAOException, FileNotFoundException, 
            XMLStreamException, XMLParseException {  
        log.entry();
        log.info("Start parsing of OrthoXML file...");

        // Catch any IllegalStateException to wrap it into a IllegalArgumentException 
        // (a IllegalStateException would be generated because the OrthoXML groups
        // loaded from the file would be invalid, so it would be a wrong argument).
        try {
            // Retrieve genes id of the Bgee database to be able to check if OMA genes are
            // in Bgee and to update OMAParentNodeId in gene table.
            this.loadGenesFromDb();

            // Retrieve genomeSpeciesId-fakeGeneIdPrefix from Bgee to be able to duplicate
            // genes with the fake prefix when a species using a genome of  another species.
            this.loadSpeciesFromDb();
            
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
     */
    //TODO: add @see to geneIdsInBgee?
    private void loadGenesFromDb() throws DAOException {
        log.entry();
        
        log.info("Start retrieving gene IDs...");
        //TODO: how did the previous version pass unit tests? It shouldn't have.
        GeneDAO dao = this.getGeneDAO();
        dao.setAttributes(GeneDAO.Attribute.ID);
        GeneTOResultSet rsGenes = dao.getAllGenes();
        while (rsGenes.next()) {
            this.geneIdsInBgee.add(rsGenes.getTO().getId());
        }
        //TODO: if (log.isInfoEnabled)?
        log.info("Done retrieving gene IDs, {} genes found", geneIdsInBgee.size());

        log.exit();
    }

    /**
     * Retrieves genome species ID and fake gene ID prefix of species using a genome of 
     * another species.
     * 
     * @throws DAOException     If an error occurred while getting the data from the Bgee
     *                          database.
     */
    //TODO: add @see to speciesPrefixes?
    //TODO: this method does not load the species, only the fake prefixes. Modify method name and log.info
    private void loadSpeciesFromDb() throws DAOException {
        log.entry();
        
        log.info("Start retrieving species...");
        //TODO: how did the previous version pass unit tests? It shouldn't have.
        SpeciesDAO speciesDAO = this.getSpeciesDAO();
        speciesDAO.setAttributes(SpeciesDAO.Attribute.ID, 
                SpeciesDAO.Attribute.FAKE_GENE_ID_PREFIX);
        SpeciesTOResultSet rsSpecies = speciesDAO.getAllSpecies();
        while (rsSpecies.next()) {
            SpeciesTO speciesTO = rsSpecies.getTO();
            if (StringUtils.isNotBlank(speciesTO.getGenomeSpeciesId()) && 
                    !speciesTO.getId().equals(speciesTO.getGenomeSpeciesId())) {
                this.speciesPrefixes.put(Integer.parseInt(speciesTO.getGenomeSpeciesId()),
                        speciesTO.getFakeGeneIdPrefix());
                log.debug("Added fake geneId prefix {} for species {}, using genome of species {}", 
                        speciesTO.getFakeGeneIdPrefix(), speciesTO.getId(), 
                        speciesTO.getGenomeSpeciesId());
            }
        }
        log.debug("Association betweeen species with fake genomes and their fake geneId prefix: {}", 
                this.speciesPrefixes);
        //TODO: if (log.isInfoEnabled)?
        log.info("Done retrieving species, {} species found", speciesPrefixes.size());

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
        //TODO: no need for a buffered reader or something like that?
        OrthoXMLReader reader = new OrthoXMLReader(new File(orthoXMLFile));
        Group group;
        // Read all the groups in the file iteratively
        while ((group = reader.next()) != null) {
            this.generateTOsFromGroup(group, group.getId());
            //TODO: comment to explain the use of this nestedSetId attribute
            this.nestedSetId++;
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
     * @param OMAGroupId    A {@code String} that the OMA group ID to use for subgroups.
     */
    //TODO: inject the nestedSetId and OMANodeId as well?
    private void generateTOsFromGroup(Group group, String OMAGroupId) {
        log.entry(group, OMAGroupId);
        //TODO: comment to explain the use of this nestedSetId attribute
        this.nestedSetId++;
        // Add a HierarchicalGroupTO in collection.
        //TODO: shouldn't it be added only if it is a taxon relevant to Bgee?
        //TODO: what are the differences between nestedSetId, OMAGroupId, OMANodeId?
        this.addHierarchicalGroupTO(OMAGroupId, group.getProperty("TaxId"),
                countGroups(group) - 1);//TODO: explain the -1
        if (group.getGenes() != null) {
            log.debug("Retrieving genes from group {}", group);
            for (sbc.orthoxml.Gene groupGene : group.getGenes()) {
                log.debug("Retrieving gene {} with identifier {}", groupGene, 
                        groupGene.getGeneIdentifier());
                //TODO: define the splitting String as a final static attribute.
                //Split the string in a dedicated method (in case it becomes more complex)
                List<String> geneIds = Arrays.asList(groupGene.getGeneIdentifier().split("; "));
                boolean isInBgee = false ;
                for (String geneId : geneIds) {
                    log.debug("Examining OMA geneId {}", geneId);
                    int taxId = groupGene.getSpecies().getNcbiTaxId();
                    if (geneIdsInBgee.contains(geneId)) {
                        isInBgee = true;
                        this.addGeneTO(new GeneTO(geneId, "", "", 0, 0, OMANodeId, true),
                                OMAGroupId);
                    }
                    // Check if this taxon corresponds to a species for which we are using 
                    //the genome of another species
                    if (this.speciesPrefixes.containsKey(taxId)) {
                        //Change prefix of the gene to create a fake gene ID.
                        //TODO: not all gene IDs start with ENS
                        //TODO: what if a species genome is used for several other species?
                        String duplicate = geneId.replaceFirst("^ENS[A-Z][A-Z][A-Z]G", 
                                this.speciesPrefixes.get(taxId));
                        log.debug("Generating fake geneId from {} to {}, because belonging to species {}", 
                                geneId, duplicate, taxId);
                        if (this.addGeneTO(new GeneTO(duplicate, "", "", 0, 0, OMANodeId, true),
                                OMAGroupId)) {
                            isInBgee = true;
                        }
                    }
                    if (this.addGeneTO(new GeneTO(geneId, "", "", 0, 0, OMANodeId, true), 
                            OMAGroupId)) {
                        isInBgee = true;
                    }
                }
                if (!isInBgee) {
                    log.warn("No gene ID in {} found in Bgee for the node {}",
                            groupGene.getGeneIdentifier(), OMANodeId);
                }
            }
        }
        // Incrementing the node ID. Done after to be able to set OMA parent node ID
        // into gene table
        OMANodeId++;
        if (group.getChildren() != null && group.getChildren().size() > 0) {
            for (Group childGroup : group.getChildren()) {
                // Recurse
                generateTOsFromGroup(childGroup, OMAGroupId);
                nestedSetId++;
            }
        }
        log.exit();
    }

    /**
     * Given a OMA Group Id with the taxonomy range and a number of children, calculate
     * nested set bounds and add it in the {@code Collection} of 
     * {@code HierarchicalGroupTO}s to be as a nested set model.
     * 
     * @param OMAGroupId    A {@code String} that is the OMA group ID.
     * @param taxRange      An {@code int} that is the taxonomy range of the
     *                      {@code HierarchicalGroupTO} to create.
     * @param nbChild       An {@code int} that is the number of children of the
     *                      {@code HierarchicalGroupTO} to create.
     */
    //TODO: inject OMANodeId and nestedSetId?
    private void addHierarchicalGroupTO(String OMAGroupId, String taxId, int nbChild) {
        log.entry(OMAGroupId, taxId, nbChild);
        // Left
        int left = nestedSetId;
        // Right = left + 2 * number of children + 1;
        int right = nestedSetId + 2 * nbChild + 1;
        hierarchicalGroupTOs.add(
                new HierarchicalGroupTO(OMANodeId, OMAGroupId, left, right, 
                        (taxId == null ? 0: Integer.valueOf(taxId)))); 
        log.exit();
    }

    /**
     * Given a {@code GeneTO} and an OMA Group ID, add the {@code GeneTO} in the 
     * {@code Collection} of {@code GeneTO}s to be update.
     * <p>
     * If the given {@code GeneTO} has already been inserted in the {@code Collection} 
     * with another OMA Group ID, ​​a warning will be generated.
     * 
     * @param geneTO        A {@code GeneTO} to add. 
     * @param OMAGroupID    A {@code String} that is the OMA group ID.
     */
    private boolean addGeneTO(GeneTO geneTO, String OMAGroupId) {
        log.entry(geneTO, OMAGroupId);
        
        if (!this.geneIdsInBgee.contains(geneTO.getId())) {
            log.debug("Gene discarded because not in Bgee: {}", geneTO.getId());
            return log.exit(false);
        }
        
        if (genesUpdated.containsKey(geneTO.getId())
                && !genesUpdated.get(geneTO.getId()).equals(OMAGroupId)) {
            log.warn("The gene {} is in diffent hierarchical orthologous groups: " +
                    "/{}/ and /{}/", geneTO.getId(), genesUpdated.get(geneTO.getId()),
                    OMAGroupId);
            return log.exit(false); 
        } 
        
        this.genesUpdated.put(geneTO.getId(), OMAGroupId);
        // Add new {@code GeneTO} to {@code Collection} of {@code GeneTO}s to be able 
        // to update OMAGroupId in gene table.
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
