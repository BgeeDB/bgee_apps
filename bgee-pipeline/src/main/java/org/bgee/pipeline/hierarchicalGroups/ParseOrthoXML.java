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

import sbc.orthoxml.Gene;
import sbc.orthoxml.Group;
import sbc.orthoxml.Species;
import sbc.orthoxml.io.OrthoXMLReader;

/**
 * This class parses the orthoxml file which contains all the data of the
 * hierarchical orthologus groups obtained from OMA. It retrieves all the data
 * pertaining to the orthologus genes
 * 
 * @author Komal Sanjeev
 * 
 */
public class ParseOrthoXML {

	private static long hierarchicalGroupId = 1;
	private static long nestedSetId = 1;

	/**
	 * Performs the complete task of reading the orthoxml file and adding the
	 * data into the database.
	 * <p>
	 * 
	 * 
	 * @throws FileNotFoundException
	 *             if the OrthoXMLReader cannot find the file
	 * @throws XMLStreamException
	 *             if there is an error in the well-formedness of the XML or
	 *             other unexpected processing errors.
	 * @throws XMLParseException
	 *             if there is an error in parsing the XML retrieved by the
	 *             PrthoXMLReader
	 * @throws SQLException
	 *             if there is an error establishing a connection to the
	 *             database
	 * 
	 */
	public void parseXML() throws FileNotFoundException, XMLStreamException,
			XMLParseException, SQLException {

		// Get the orthoXML file.
		String orthoXmlFile = this.getClass()
				.getResource("/orthoxml/HierarchicalGroupsTest.orthoxml")
				.toString();
		File file = new File(orthoXmlFile);

		OrthoXMLReader reader = new OrthoXMLReader(file);

		ParseOrthoXML.hierarchicalGroupId = 1;
		Group group;

		// read all the groups in the file iteratively
		while ((group = reader.next()) != null) {

			Node rootNode = new Node();
			rootNode.setHierarchicalGroupId(ParseOrthoXML.hierarchicalGroupId++);

			// Build the tree of the current group
			BuildTree(group, rootNode);

			// After building the tree, now traverse the tree again to
			// assign the hierarchical left bound and right bound in order to
			// store them as a nested set.
			ParseOrthoXML.nestedSetId = 0;
			buildNestedSet(rootNode);

			// Add data of the tree to database
			addToHierarchicalGroupsTable(group, rootNode);
		}
	}

	/**
	 * Builds a <code>Tree</code> of the <code>Group</code> passed as a
	 * parameter.
	 * <p>
	 * This method builds a tree of the current <code>Group</code> with the root
	 * <code>Node</code> which is passed at a parameter. It recursively iterates
	 * through all the sub-groups at all the taxonomic levels of the group and
	 * builds a
	 * 
	 * @param group
	 *            the <code>Group</code> object of the current orthologus group
	 *            whose tree is to be built
	 * @param node
	 *            the root <code>Node</code> of the <code>Tree</code> whose tree
	 *            is to be built
	 * 
	 */
	public void BuildTree(Group group, Node node) {

		// Adding the NCBI taxonomy ID,i.e,the hierarchical level
		node.setNcbiTaxonomyId(group.getProperty("TaxRange"));

		// Iterating through all the children of the current group
		for (Group child : group.getChildren()) {

			// Make a new node object for every child, and set a unique ID
			Node childNode = new Node();
			childNode.setHierarchicalGroupId(ParseOrthoXML.hierarchicalGroupId++);

			// Add that node as a child to the parent node
			node.addChild(childNode);

			// For all the leaves, i.e, the genes, create a node for each of
			// them add data about their GeneIDs to the <code>Node</code>
			// object.
			for (Gene gene : child.getGenes()) {
				Node leaf = new Node();
				childNode.addChild(leaf);
				leaf.setHierarchicalGroupId(ParseOrthoXML.hierarchicalGroupId++);
				leaf.setGeneID(gene.getProteinIdentifier());
			}

			// Recurse!!
			BuildTree(child, childNode);
		}

	}

	/**
	 * Build the nested set model for the current <code>Group</code> whose root
	 * <code>Node</code> is passed as a parameter.
	 * <p>
	 * This method assign the hierarchical left and right bounds to all the
	 * nodes in the current <code>Group</code> (tree) whose root
	 * <code>Node</code> is passed as a parameter.
	 * <p>
	 * The right bound can be calculated using this formula:
	 * <p>
	 * rightBound = leftBound + 2(number of child nodes) + 1
	 * 
	 * @param node
	 *            the root <code>Node</code> of the tree whose nested set model
	 *            is to be built.
	 */
	public void buildNestedSet(Node node) {
		// For every node visited, increment the ID.
		ParseOrthoXML.nestedSetId++;
		// Left
		node.setHierarchicalLeftBound(ParseOrthoXML.nestedSetId);
		// Right = left + 2*numberOfChildren + 1;
		node.setHierarchicalRightBound(ParseOrthoXML.nestedSetId + 2
				* (count(node) - 1) + 1);

		// Recurse!!
		for (Node childNode : node.getChildNodes()) {
			buildNestedSet(childNode);
			ParseOrthoXML.nestedSetId++;
		}
	}

	/**
	 * Counts the number of nodes of the current tree/subtree.
	 * <p>
	 * This method recursively iterates through all the nodes of the tree whose
	 * root <code>Node</code> is passed as a parameter and returns the total
	 * number of nodes in the tree (including the root node).
	 * 
	 * @param node
	 *            the <code>Node</code> object of the tree/subtree whose total
	 *            number of children nodes are to be counted
	 * @return an <code>int</code> giving the total number of nodes in the
	 *         tree/subtree
	 */
	public static int count(Node node) {
		int c = 1;
		for (Node childNode : node.getChildNodes()) {
			c += count(childNode);
		}
		return c;
	}

	/**
	 * Add data of the <code>Tree</code> whose root <code>Node</code> is being
	 * passed as a parameter.
	 * <p>
	 * This method recursively iterates through the <code>Tree</code> whose root
	 * <code>Node</code> is passed as a parameter, and adds the data pertaining
	 * to every node into the hierarchicalGroup table.
	 * 
	 * @param group
	 *            the orthologus group ID of the current <code>Group</code>
	 *            whose data is being added into the database.
	 * @param root
	 *            the root node of the <code>Group</code> whose data is being
	 *            added into the database
	 * @throws SQLException
	 *             if there is an error establishing a connection to the
	 *             database
	 */
	public void addToHierarchicalGroupsTable(Group group, Node node)
			throws SQLException {

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		}

		Connection connection = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/database", "username", "password");
		String sql = "INSERT INTO hierarchicalGroup (uniqueRowId, hierarchicalGroupId, hierarchicalGroupLeftBound, hierarchicalGroupRightBound, "
				+ "ncbiTaxonomyId, ncbiGeneId)" + " VALUES (?, ?, ?, ?, ?, ?)";

		try {
			PreparedStatement preparedStatement = connection
					.prepareStatement(sql);
			preparedStatement.setLong(1, node.getHierarchicalGroupId());
			preparedStatement.setString(2, group.getId());
			preparedStatement.setLong(3, node.getHierarchicalLeftBound());
			preparedStatement.setLong(4, node.getHierarchicalRightBound());
			preparedStatement.setString(5, node.getNcbiTaxonomyId());
			preparedStatement.setString(6, node.getGeneID());
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			System.out.println(e.toString());
		} finally {
			connection.close();
		}

		// Recurse!!
		for (Node childNode : node.getChildNodes()) {
			addToHierarchicalGroupsTable(group, childNode);
		}
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
	public static void getSpecies(File file) throws FileNotFoundException,
			XMLStreamException, XMLParseException {

		// Read the species iteratively
		OrthoXMLReader reader = new OrthoXMLReader(file);

		List<Species> species = new ArrayList<Species>();
		species = reader.getSpecies();

		for (Species specie : species) {
			System.out.println(specie.getName());
		}
	}

}
