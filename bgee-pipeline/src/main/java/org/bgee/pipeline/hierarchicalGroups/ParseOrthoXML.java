package org.bgee.pipeline.hierarchicalGroups;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.modelmbean.XMLParseException;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneTO;
import org.bgee.model.dao.api.hierarchicalgroup.HierarchicalGroupTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.MySQLDAOUser;

import sbc.orthoxml.Gene;
import sbc.orthoxml.Group;
import sbc.orthoxml.Species;
import sbc.orthoxml.io.OrthoXMLReader;

/**
 * This class parses the orthoxml file which contains all the data of the
 * hierarchical orthologus groups obtained from OMA. It retrieves all the data
 * pertaining to the orthologus genes.
 * 
 * @author Komal Sanjeev
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
/**
 * @author vrechdelaval
 *
 */
public class ParseOrthoXML extends MySQLDAOUser {

	private final static Logger log =
			LogManager.getLogger(ParseOrthoXML.class.getName());
	
	private static int OMANodeId = 1;
	private int nestedSetId = 0;
	private Set<HierarchicalGroupTO> hierarchicalGroupTOs = new HashSet<HierarchicalGroupTO>();
	private Set<GeneTO> geneTOs = new HashSet<GeneTO>();
	private List<String> genesInDb;

    /**
     * Default constructor. 
     */
    public ParseOrthoXML() {
        super();
    }
    
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public ParseOrthoXML(MySQLDAOManager manager) {
        super(manager);
    }

    /**
     * Main method to trigger the insertion of the hierarchical orthologus 
     * groups obtained from OMA into the Bgee database. Parameters that must 
     * be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the file storing the hierarchical orthologus groups in OrthoXML.
     * </ol>
     * 
     * @param args	An {@code Array} of {@code String}s containing the requested parameters.
     * @throws FileNotFoundException		If some files could not be found.
     * @throws IllegalArgumentException		If the files used provided invalid information.
     * @throws DAOException					If an error occurred while inserting
											the data into the Bgee database.
     * @throws XMLStreamException			If there is an error in the well-formedness of
											the XML or other unexpected processing errors.
     * @throws XMLParseException			If there is an error in parsing the XML retrieved
											by the OrthoXMLReader.
     */
    public static void main(String[] args) throws FileNotFoundException, 
    		IllegalArgumentException, DAOException, 
    		XMLStreamException, XMLParseException {
        log.entry((Object[]) args);
        
        int expectedArgLength = 1;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                    "provided, expected " + expectedArgLength + " arguments, " + args.length + 
                    " provided."));
        }
    
        ParseOrthoXML parser = new ParseOrthoXML();
        parser.parseXML(args[0]);
        
        
        log.exit();
    }

    /**
	 * Performs the complete task of reading the Hierarchical Groups orthoxml
	 * file and adding the data into the database.
	 * <p>
	 * This method reads the Hierarchical Groups OrthoXML file, and adds all the
	 * data required to the database. It first retrieves genes id from Ensembl of 
	 * the Bgee database to be able to check if genes IDs of the file are Ensembl ID
	 * Then it iterates through all the orthologous groups present in the file and 
	 * builds a {@code Collection} of {@code HierarchicalGroupTO}s as a nested set
	 * model and a {@code Collection} of {@code GeneTO}s.
	 * <p>
	 * After the data is added to the OMAHierarchicalGroup table and the gene table
	 * is updated.
	 * 
     * @param orthoXMLFile	A {@code String} that is the path to the OMA groups file.	
     * @throws DAOException				If an error occurred while inserting
										the data into the Bgee database.
     * @throws FileNotFoundException	If some files could not be found.
     * @throws XMLStreamException		If there is an error in the well-formedness of
										the XML or other unexpected processing errors.
     * @throws XMLParseException		If there is an error in parsing the XML retrieved
										by the OrthoXMLReader.
     */
    public void parseXML(String orthoXMLFile) throws DAOException, FileNotFoundException, 
    		XMLStreamException, XMLParseException {  
    	log.entry();
    	log.info("Start parsing of OrthoXML file...");

    	// Catch any IllegalStateException to wrap it into a IllegalArgumentException 
    	// (a IllegalStateException would be generated because the OrthoXML groups
    	// loaded from the file would be invalid, so it would be a wrong argument).
    	try {
    		// Retrieve genes id from Ensembl of the Bgee database to be able to check
    		// if OMA genes are Ensembl ID and update OMAGroupId in gene table.
    		this.getGenesOfDb();

    		// Construct HierarchicalGroupTOs and GeneTOs
    		this.setTOs(orthoXMLFile);

    		// Start a transaction to insert HierarchicalGroupTOs and update GeneTOs 
    		// in the Bgee data source.Note that we do not need to call rollback if
    		// an error occurs, calling closeDAO will rollback any ongoing transaction.
    		try {
    			this.startTransaction();

    			log.info("Start inserting of hierarchical groups...");
    			this.getHierarchicalGroupDAO().insertHierarchicalGroups(
    					hierarchicalGroupTOs);
    			log.info("Done inserting hierarchical groups");

    			log.info("Start updating genes...");
    			this.getGeneDAO().updateOMAGroupIDs(geneTOs);
    			log.info("Done updating genes.");

    			this.commit();
    		} finally {
    			this.closeDAO();
    		}
    		log.info("Done parsing of OrthoXML file: "
    				+ "{} hierarchical groups inserted and "
    				+ "{} genes inserted.", 
    				hierarchicalGroupTOs.size(), 
    				geneTOs.size());
    	} catch (IllegalStateException e) {
    		log.catching(e);
    		throw log.throwing(new IllegalArgumentException(
    				"The OrthoXML provided is invalid", e));
    	}

    	log.exit();
    }

	/**
	 * Retrieves all Ensembl gene IDs present into the Bgee database.
	 * 
     * @throws DAOException				If an error occurred while inserting
										the data into the Bgee database.
	 */
	private void getGenesOfDb() throws DAOException {
    	log.entry();
		
        try {
            this.startTransaction();
            
            log.info("Start getting gene IDs...");
            List<GeneTO> genes = this.getGeneDAO().getAllGeneIDs();
            for (GeneTO gene: genes) {
            	genesInDb.add(gene.getId());
            }
            log.info("Done getting gene IDs");
            
            this.commit();
        } finally {
            this.closeDAO();
        }
        log.info("Done getting gene IDs, {} genes found", genesInDb.size());
        log.exit();
	}

	/**
	 * Extract all relevant information from the OrthoXML file and fill the 
	 * {@code Collection} of {@code HierarchicalGroupTO}s as a nested set
	 * model and the {@code Collection} of {@code GeneTO}s.
	 * 
     * @param orthoXMLFile	A {@code String} that is the path to the OMA groups file.	
     * @throws FileNotFoundException	If some files could not be found.
     * @throws XMLStreamException		If there is an error in the well-formedness of
										the XML or other unexpected processing errors.
     * @throws XMLParseException		If there is an error in parsing the XML retrieved
										by the OrthoXMLReader.
	 */
	private void setTOs(String orthoXMLFile) throws FileNotFoundException,  
			XMLStreamException, XMLParseException {
		log.entry();
        log.info("Retrieving hierarchical groups...");
        
		OrthoXMLReader reader = new OrthoXMLReader(new File(orthoXMLFile));
		Group group;
		// Read all the groups in the file iteratively
		while ((group = reader.next()) != null) {
			Deque<Group> dequeGroup = new ArrayDeque<Group>();
			int OMAGroupId = Integer.parseInt(group.getId());
			dequeGroup.addLast(group);
			while (!dequeGroup.isEmpty()) {
				Group currentGroup = dequeGroup.removeFirst();
				addHierarchicalGroupTO(OMAGroupId, 
						currentGroup.getProperty("TaxRange"), count(group));
				if (currentGroup.getGenes() != null) {
					for (Gene groupGene : currentGroup.getGenes()) {
						// Create a {@code HierarchicalGroupTO} for each 
						// {@code Gene}. {@code Gene}s haven't got child.
						addHierarchicalGroupTO(OMAGroupId, 
								currentGroup.getProperty("TaxRange"), 0);
						// Parse gene identifiers (named protId in OrthoXML file)
						List<String> genes = Arrays.asList(
								groupGene.getProteinIdentifier().split("; "));
						for (String geneId : genes) {
							if (genesInDb.contains(geneId)) {
								// Add new {@code GeneTO} to {@code Collection} 
								// of {@code GeneTO}s to be able to update
								// OMAGroupId in gene table.
								geneTOs.add(new GeneTO(geneId, OMAGroupId));
								break;
							}
						}
						// Genes haven't got child
						nestedSetId++;
					}
				}
				if (currentGroup.getChildren() != null) {
					// Add to {@code Deque} all children of the current {@code Group}.
					for (Group childGroup : currentGroup.getChildren()) {
						dequeGroup.addLast(childGroup);
					}
				} else {
					// No child
					nestedSetId++;
				}
			}
		}
        log.info("Done retrieving hierarchical groups.");
        log.exit();
	}

	/**
	 * Given a OMAGroupId with the taxonomy range and a number of children, 
	 * calculate nested set bounds and fill the {@code Collection} of 
	 * {@code HierarchicalGroupTO}s as a nested set model.
	 * 
	 * @param OMAGroupId	A {@code int} that is the OMA group ID.
	 * @param taxRange		A {@code String} that is the taxonomy range 
	 * 						of the {@code HierarchicalGroupTO} to create.
	 * @param nbChild		A {@code int} that is the number of children 
	 * 						of the {@code HierarchicalGroupTO} to create.
	 */
	private void addHierarchicalGroupTO(int OMAGroupId, String taxRange, int nbChild) {
		nestedSetId++;
		// Left
		int left = nestedSetId;
		// Right = left + 2 * number of children + 1;
		int right = nestedSetId + 2 * nbChild + 1;
		hierarchicalGroupTOs.add(new HierarchicalGroupTO(OMANodeId++, OMAGroupId, left, right, taxRange));
	}
	
	/**
	 * Counts the number of {@code Group}s of the current group/subgroup.
	 * <p>
	 * This method recursively iterates through all the groups of the tree
	 * whose {@code Group} is passed as a parameter and returns the total
	 * number of groups in the tree (including the root group).
	 * 
	 * @param group
	 *            the {@code Group} object of the group/subgroup whose 
	 *            total number of children groups are to be counted
	 * @return an {@code int} giving the total number of groups in the
	 *         group/subgroup
	 */
	private static int count(Group group) {
		log.entry(group);
		int c = 1;
		for (Group childGroup : group.getChildren()) {
			c += count(childGroup);
		}
		return log.exit(c);
	}
	
	/**
	 * Reads the species IDs of all the species present in the orthoxml file.
	 * 
	 * @throws XMLParseException
	 *             if there is an error in parsing the XML retrieved by the
	 *             OrthoXMLReader
	 * @throws XMLStreamException
	 *             if there is an error in the well-formedness of the XML or
	 *             other unexpected processing errors.
	 * @throws FileNotFoundException
	 *             if the OrthoXMLReader cannot find the file
	 */
	public static ArrayList<String> getSpecies(File file)
			throws FileNotFoundException, XMLStreamException, XMLParseException {

		log.entry();

		// Read the species iteratively
		OrthoXMLReader reader = new OrthoXMLReader(file);

		List<Species> species = new ArrayList<Species>();
		species = reader.getSpecies();

		ArrayList<String> speciesIds = new ArrayList<String>();
		for (Species specie : species) {
			speciesIds.add(specie.getName());
		}

		return log.exit(speciesIds);
	}
}
