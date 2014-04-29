package org.bgee.pipeline.hierarchicalGroups;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.management.modelmbean.XMLParseException;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 * 
 */
public class ParseOrthoXML {

	private final static Logger log =
			LogManager.getLogger(ParseOrthoXML.class.getName());

	
	private static int hierarchicalGroupId = 1;
	private int nestedSetId = 1;
	private String orthoXmlFile;

//	public void deriveOrthologusGroups() {
//		
//		ParseOrthoXML parser = new ParseOrthoXML();
//
//		parser.setOrthoXmlFile(this.getClass()
//				.getResource("/orthoxml/HierarchicalGroups.orthoxml")
//				.toString());
//
//		// Get the orthoXML file.
//	}

	/**
	 * Performs the complete task of reading the Hierarchical Groups orthoxml
	 * file and adding the data into the database.
	 * <p>
	 * This method reads the Hierarchical Groups OrthoXML file, and adds all the
	 * data required to the database. It first iterates through all the
	 * orthologus groups present in the file and builds a tree like model with
	 * {@code Node}s. It then iterates through this tree to build a nested
	 * set model.
	 * <p>
	 * After the data required to store the groups as a nested set model is
	 * generated, this data is added to the HierarchicalGroup and gene tables.
	 * 
	 * @throws FileNotFoundException
	 *             if the OrthoXMLReader cannot find the file
	 * @throws XMLStreamException
	 *             if there is an error in the well-formedness of the XML or
	 *             other unexpected processing errors.
	 * @throws XMLParseException
	 *             if there is an error in parsing the XML retrieved by the
	 *             OrthoXMLReader
	 * @throws SQLException
	 *             if there is an error establishing a connection to the
	 *             database
	 * 
	 */
	public void parseXML() throws FileNotFoundException, XMLStreamException,
			XMLParseException, SQLException {

		log.entry();
		log.debug("Hierarchical Groups OrthoXML File: {}", this.getOrthoXmlFile());

		File file = new File(this.getOrthoXmlFile());

		OrthoXMLReader reader = new OrthoXMLReader(file);
		Group group;
		Node rootNode;
		
		// read all the groups in the file iteratively
		while ((group = reader.next()) != null) {

			log.debug("OrthologusGroupId: {}", group.getId());

			rootNode = new Node();
			rootNode.setHierarchicalGroupId(hierarchicalGroupId++);

			log.info("Start building Tree...");
			// Build the tree of the current group
			ParseOrthoXML.buildTree(group, rootNode);
            log.info("Done inserting Tree.");

			log.info("Start building Nested Set Model...");
			// After building the tree, now traverse the tree again to
			// assign the hierarchical left bound and right bound in order to
			// store them as a nested set.
			nestedSetId = 0;
			this.buildNestedSet(rootNode);
            log.info("Done building Nested Set Model.");

			log.info("Start adding data to hierarchicalGroup table...");
			// Add data of this group to the HierarchicalGroup table
			this.addToHierarchicalGroupTable(group, rootNode);
			log.info("Done adding data to hierarchicalGroup table.");

			log.info("Start adding data to gene table...");
			// Add data of this group to the gene table
			this.addToGeneTable(group, rootNode);
			log.info("Done adding data to gene table.");

		}

		log.exit();
	}

	/**
	 * Builds a {@code Tree} of the {@code Group} passed as a parameter.
	 * <p>
	 * This method builds a tree of the current {@code Group} with the root
	 * {@code Node} which is passed at a parameter. It recursively iterates
	 * through all the sub-groups at all the taxonomic levels of the group and
	 * builds a
	 * 
	 * @param group
	 *            the {@code Group} object of the current orthologus group
	 *            whose tree is to be built
	 * @param node
	 *            the root {@code Node} of the {@code Tree} whose tree
	 *            is to be built
	 * 
	 */
	public static void buildTree(Group group, Node node) {

		log.entry(group, node);

		// Adding the NCBI taxonomy ID,i.e,the hierarchical level
		node.setNcbiTaxonomyRange(group.getProperty("TaxRange"));

		// For all the leaves, i.e {@code Gene}, create a node for each of
		// them and add data about their GeneIDs to the {@code Node}
		// object.
		Node leaf;
		for (Gene gene : group.getGenes()) {
			leaf = new Node();
			leaf.setHierarchicalGroupId(hierarchicalGroupId++);
			//TODO comment and parse string?
			leaf.setGeneID(gene.getProteinIdentifier());
			node.addChild(leaf);
		}

		// Iterating through all the children of the current group
		Node childNode;
		for (Group child : group.getChildren()) {
			// Make a new node object for every child, and set a unique ID
			childNode = new Node();
			childNode.setHierarchicalGroupId(hierarchicalGroupId++);

			// Add that node as a child to the parent node
			node.addChild(childNode);

			// Recurse!!
			buildTree(child, childNode);
		}
		log.exit();
	}

	/**
	 * Build the nested set model for the current {@code Group} whose root
	 * {@code Node} is passed as a parameter.
	 * <p>
	 * This method assign the hierarchical left and right bounds to all the
	 * nodes in the current {@code Group} (tree) whose root
	 * {@code Node} is passed as a parameter.
	 * <p>
	 * The right bound can be calculated using this formula:
	 * <p>
	 * rightBound = leftBound + 2(number of child nodes) + 1
	 * 
	 * @param node
	 *            the root {@code Node} of the tree whose nested set model
	 *            is to be built.
	 */
	public void buildNestedSet(Node node) {
		log.entry(node);
		// For every node visited, increment the ID.
		nestedSetId++;
		// Left
		node.setHierarchicalLeftBound(nestedSetId);
		// Right = left + 2*numberOfChildren + 1;
		node.setHierarchicalRightBound(nestedSetId + 2 * (count(node) - 1) + 1);

		// Recurse!!
		for (Node childNode : node.getChildNodes()) {
			buildNestedSet(childNode);
			nestedSetId++;
		}
		log.exit();
	}

	/**
	 * Counts the number of nodes of the current tree/subtree.
	 * <p>
	 * This method recursively iterates through all the nodes of the tree whose
	 * root {@code Node} is passed as a parameter and returns the total
	 * number of nodes in the tree (including the root node).
	 * 
	 * @param node
	 *            the {@code Node} object of the tree/subtree whose total
	 *            number of children nodes are to be counted
	 * @return an {@code int} giving the total number of nodes in the
	 *         tree/subtree
	 */
	public static int count(Node node) {
		log.entry(node);
		int c = 1;
		for (Node childNode : node.getChildNodes()) {
			c += count(childNode);
		}
		return log.exit(c);
	}

	/**
	 * Add data of the {@code Group} whose root {@code Node} is being
	 * passed as a parameter.
	 * <p>
	 * This method recursively iterates through the {@code Node}s of the
	 * {@code Group} whose root {@code Node} is passed as a parameter,
	 * and adds the data pertaining to every node into the hierarchicalGroup
	 * table. The data includes the hierarchicalGroupId, orthologusGroupId,
	 * hierarchicalLeftBound, hierarchicalRightBound, and the ncbiTaxonomyId.
	 * 
	 * @param group
	 *            the orthologus group ID of the current {@code Group}
	 *            whose data is being added into the database.
	 * @param root
	 *            the root node of the {@code Group} whose data is being
	 *            added into the database
	 * @throws SQLException
	 *             if there is an error establishing a connection to the
	 *             database
	 */
	public void addToHierarchicalGroupTable(Group group, Node node)
			throws SQLException {

		/*log.entry(group, node);

		BgeeProperties props = BgeeProperties.getBgeeProperties();

		BgeeDataSource source = BgeeDataSource.getBgeeDataSource();
		BgeeConnection connection = source.getConnection();

		String sql = "INSERT INTO hierarchicalGroup (uniqueRowId, hierarchicalGroupId, hierarchicalGroupLeftBound, hierarchicalGroupRightBound, "
				+ "ncbiTaxonomyId, ncbiGeneId)" + " VALUES (?, ?, ?, ?, ?, ?)";

		try {
			BgeePreparedStatement bgeePreparedStatement = connection
					.prepareStatement(sql);
			bgeePreparedStatement.setLong(1, node.getHierarchicalGroupId());
			bgeePreparedStatement.setString(2, group.getId());
			bgeePreparedStatement.setLong(3, node.getHierarchicalLeftBound());
			bgeePreparedStatement.setLong(4, node.getHierarchicalRightBound());
			bgeePreparedStatement.setString(5, node.getNcbiTaxonomyId());

			if (log.isDebugEnabled()) {
				log.debug(bgeePreparedStatement.toString());
			}

			bgeePreparedStatement.executeUpdate();

		} catch (SQLException e) {
			log.error(e.toString());
		} finally {
			connection.close();
		}

		// Recurse!!
		for (Node childNode : node.getChildNodes()) {
			addToHierarchicalGroupTable(group, childNode);
		}
		log.exit();*/
	}

	/**
	 * Add data of the {@code Group} whose root {@code Node} is being
	 * passed as a parameter.
	 * <p>
	 * This method recursively iterates through the {@code Node}s of the
	 * {@code Group} whose root {@code Node} is passed as a parameter,
	 * and adds the data pertaining to every node into the gene table. The data
	 * includes the hierarchicalGroupId corresponding to the geneId.
	 * 
	 * @param group
	 *            the orthologus group ID of the current {@code Group}
	 *            whose data is being added into the database.
	 * @param root
	 *            the root node of the {@code Group} whose data is being
	 *            added into the database
	 * @throws SQLException
	 *             if there is an error establishing a connection to the
	 *             database
	 */
	public void addToGeneTable(Group group, Node node) throws SQLException {

		log.entry(group, node);

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			log.error("here.. " + e.toString());
		}

		// If gene,
		if (node.getGeneID() != null) {

			String[] geneIds = node.getGeneID().split("; ");

			Connection connection = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/db", "user", "pass");

			String sql = "UPDATE gene SET hierarchicalGroupId='"
					+ node.getHierarchicalGroupId() + "' WHERE ";

			for (String id : geneIds) {
				sql = sql + "geneId='" + id + "' OR ";
			}

			sql = sql + " geneId='';";

			try {
				PreparedStatement preparedStatement = connection
						.prepareStatement(sql);

				if (log.isDebugEnabled()) {
					log.debug(preparedStatement.toString());
				}

				preparedStatement.executeUpdate();
			} catch (SQLException e) {
				log.error(e.toString());
			} finally {
				connection.close();
			}

		}
		// Recurse!!
		for (Node childNode : node.getChildNodes()) {
			addToGeneTable(group, childNode);
		}
		log.exit();
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

	/**
	 * @return the orthoXmlFile
	 */
	public String getOrthoXmlFile() {
		return this.orthoXmlFile;
	}

	/**
	 * @param orthoXmlFile
	 *            the orthoXmlFile to set
	 */
	public void setOrthoXmlFile(String orthoXmlFile) {
		this.orthoXmlFile = orthoXmlFile;
	}

}
