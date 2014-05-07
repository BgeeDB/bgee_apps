package org.bgee.pipeline.hierarchicalGroups;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.management.modelmbean.XMLParseException;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;
import org.junit.BeforeClass;
import org.junit.Test;

import sbc.orthoxml.Group;
import sbc.orthoxml.io.OrthoXMLReader;

/**
 * Tests the functions of {@link #org.bgee.pipeline.hierarchicalGroups.ParseOrthoXML}
 * 
 * @author Komal Sanjeev
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public class ParseOrthoXMLTest extends TestAncestor {
	
    /**
     * {@code Logger} of the class. 
     */
	private final static Logger log = 
			LogManager.getLogger(ParseOrthoXMLTest.class.getName());

	private static ParseOrthoXML parser;

    private static final String OMAFILE = "/orthoxml/fakeOMA.orthoxml";

	public ParseOrthoXMLTest(){
		super();
	}
	
	public void testParseXML() {
		
		log.debug("Testing if the OrthoXML file is parsed correctly..");

		//TODO Check if we have unique OMANodeID
	}

    /**
     * Test {@link ParseOrthoXML#buildTree(Group)}.
     */
	public void testBuildTree() throws FileNotFoundException, XMLStreamException, 
	XMLParseException, IllegalAccessException, IllegalArgumentException, 
	InvocationTargetException, NoSuchMethodException, SecurityException {
		File file = new File(ParseOrthoXMLTest.class.getResource(OMAFILE).getPath());
		OrthoXMLReader reader = new OrthoXMLReader(file);
		Group group;
        // generate the expected data
		Node expectedNode1 = new Node();
		expectedNode1.setOMANodeId(1);
		expectedNode1.setOMAGroupId(1);
		expectedNode1.setNcbiTaxonomyRange("Euteleostomi");
		Node node2 = new Node();
		node2.setOMANodeId(2);
		node2.setOMAGroupId(1);
		node2.setGeneIDs(Arrays.asList("ENSDARG00000069839"));
		expectedNode1.addChild(node2);
		Node node3 = new Node();
		node3.setOMANodeId(3);
		node3.setOMAGroupId(1);
		Node node4 = new Node();
		node4.setOMANodeId(4);
		node4.setOMAGroupId(1);
		node4.setGeneIDs(Arrays.asList("ENSXETG00000021946", "Rep609"));
		node3.addChild(node4);
		Node node5 = new Node();
		node5.setOMANodeId(5);
		node5.setOMAGroupId(1);
		node5.setGeneIDs(Arrays.asList("ENSXETG00000021946", "Rep610"));
		node3.addChild(node5);
		expectedNode1.addChild(node3);

		// read first group in the file
		group = reader.next();
		log.debug("OrthologusGroupId: {}", group.getId());
		ParseOrthoXML parse = new ParseOrthoXML();
        Method method = parse.getClass().getDeclaredMethod("buildTree",
        		Group.class);
        method.setAccessible(true);

		// Build the tree of the current group
		Node rootNode = (Node) method.invoke(parse, group);
		//check data
		if (!this.areNodesEqual(expectedNode1, rootNode)) {
			throw new AssertionError("Incorrect generated node from OMA Group");
		}
	}

	/**
     * Method to compare two {@code Node}s, to check for complete equality
     * of each attribute.
     * 
	 * @param node1	A {@code Node} to be compared to {@code node2}.
	 * @param node2	A {@code Node} to be compared to {@code node1}.
	 * @return		{@code true} if {@code node1} and {@code node2} has all attributes equal
	 * 				as well as for child {@code Node}s.
	 */
	private boolean areNodesEqual(Node node1, Node node2) {
        log.entry(node1, node2);

        if ((node1.getOMANodeId() == node2.getOMANodeId())
        	&& (node1.getOMAGroupId() == node2.getOMAGroupId())
        	&& (node1.getNcbiTaxonomyRange() == null && 
        		node2.getNcbiTaxonomyRange() == null ||
        		node1.getNcbiTaxonomyRange() != null &&
        		node1.getNcbiTaxonomyRange().equals(
        			node2.getNcbiTaxonomyRange()))
        	&& (node1.getGeneIDs() == null &&
        		node2.getGeneIDs() == null ||
        		node1.getGeneIDs().containsAll(node2.getGeneIDs()) &&
        		node2.getGeneIDs().containsAll(node1.getGeneIDs()))) {
        	//Equivalent nodes
        } else {
            log.debug("Nodes are not equivalent {}", node1.getOMANodeId());
            return log.exit(false);
        }
   	
    	// Check if children are equals (recurse) 
        for (Node expectedChild: node1.getChildNodes()) {
        	boolean found = false;
        	for (Node generatedChild: node2.getChildNodes()) {
        		if (areNodesEqual(expectedChild, generatedChild)) {
        			found = true;
        		}
        	}
        	if (!found) {
        		log.debug("No equivalent child node found for {}", expectedChild.getOMANodeId());
        		return log.exit(false);
        	}      
        }
        return log.exit(true);
	}

	public void testBuildNestedSetModel() {
	}

	public void testCount() {
	}

	public void testGetSpecies() {
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
