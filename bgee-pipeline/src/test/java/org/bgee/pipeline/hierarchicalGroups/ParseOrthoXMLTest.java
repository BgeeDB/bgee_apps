package org.bgee.pipeline.hierarchicalGroups;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.modelmbean.XMLParseException;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.hierarchicalgroup.HierarchicalGroupDAO.HierarchicalGroupTO;
import org.bgee.pipeline.TestAncestor;
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
	private final static Logger log = LogManager.getLogger(ParseOrthoXMLTest.class.getName());

    private static final String OMAFILE = "/orthoxml/fakeOMA.orthoxml";

	public ParseOrthoXMLTest(){
		super();
	}
	
	@Override
	protected Logger getLogger() {
		return log;
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
        		log.debug("No equivalent child node found for {}", 
        				expectedChild.getOMANodeId());
        		return log.exit(false);
        	}      
        }
        return log.exit(true);
	}

	/**
     * Test {@link ParseOrthoXML#generateTOsFromFile()}.
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws XMLParseException 
	 * @throws XMLStreamException 
	 * @throws FileNotFoundException 
	 * @throws DAOException 
	 */
	@SuppressWarnings("unchecked")
//	@Test
	public void testGenerateTOs() throws NoSuchFieldException, SecurityException, 
								IllegalArgumentException, IllegalAccessException,
								NoSuchMethodException, InvocationTargetException, DAOException, 
								FileNotFoundException, XMLStreamException, XMLParseException {
		log.entry();
		
		// Expected HierarchicalGroupTOs
		// First group
		HierarchicalGroupTO hierarchicalGroupTO1 = new HierarchicalGroupTO(
				1, 1, 1, 4, "Euteleostomi");
		HierarchicalGroupTO hierarchicalGroupTO2 = new HierarchicalGroupTO(
				2, 1, 2, 3, null);
		// Second group
		HierarchicalGroupTO hierarchicalGroupTO3 = new HierarchicalGroupTO(
				3, 2, 5, 24, "Euteleostomi");
		HierarchicalGroupTO hierarchicalGroupTO4 = new HierarchicalGroupTO(
				4, 2, 6, 11, "Vertebrata");
		HierarchicalGroupTO hierarchicalGroupTO5 = new HierarchicalGroupTO(
				5, 2, 7, 8, null);
		HierarchicalGroupTO hierarchicalGroupTO6 = new HierarchicalGroupTO(
				6, 2, 9, 10, null);
		HierarchicalGroupTO hierarchicalGroupTO7 = new HierarchicalGroupTO(
				7, 2, 11, 14, null);
		HierarchicalGroupTO hierarchicalGroupTO8 = new HierarchicalGroupTO(
				8, 2, 12, 13, "Tetrapoda");
		HierarchicalGroupTO hierarchicalGroupTO9 = new HierarchicalGroupTO(
				9, 2, 14, 21, null);
		HierarchicalGroupTO hierarchicalGroupTO10 = new HierarchicalGroupTO(
				10, 2, 15, 18, "Cladistia");
		HierarchicalGroupTO hierarchicalGroupTO11 = new HierarchicalGroupTO(
				11, 2, 16, 17, null);
		HierarchicalGroupTO hierarchicalGroupTO12 = new HierarchicalGroupTO(
				12, 2, 18, 19, null);
		// Third group
		HierarchicalGroupTO hierarchicalGroupTO13 = new HierarchicalGroupTO(
				13, 3, 21, 24, "Chordata");
		HierarchicalGroupTO hierarchicalGroupTO14 = new HierarchicalGroupTO(
				14, 3, 22, 23, null);
		Set<HierarchicalGroupTO> expectedTOs = new HashSet<HierarchicalGroupTO>();
		expectedTOs.add(hierarchicalGroupTO1);
		expectedTOs.add(hierarchicalGroupTO2);
		expectedTOs.add(hierarchicalGroupTO3);
		expectedTOs.add(hierarchicalGroupTO4);
		expectedTOs.add(hierarchicalGroupTO5);
		expectedTOs.add(hierarchicalGroupTO6);
		expectedTOs.add(hierarchicalGroupTO7);
		expectedTOs.add(hierarchicalGroupTO8);
		expectedTOs.add(hierarchicalGroupTO9);
		expectedTOs.add(hierarchicalGroupTO10);
		expectedTOs.add(hierarchicalGroupTO11);
		expectedTOs.add(hierarchicalGroupTO12);
		expectedTOs.add(hierarchicalGroupTO13);
		expectedTOs.add(hierarchicalGroupTO14);
		
		MockDAOManager mockManager = new MockDAOManager();
		ParseOrthoXML parser = new ParseOrthoXML(mockManager);
		parser.parseXML(ParseOrthoXMLTest.class.getResource(OMAFILE).getPath());
		
		Field genesInDb = parser.getClass().getDeclaredField("genesInDb");
		genesInDb.setAccessible(true);
		genesInDb.set(parser, Arrays.asList("ENSDARG00000039453", "ENSDARG00000069839",
							 "ENSDARG00000078198", "ENSDARG00000069837", "ENSDARG00000078286", 
							 "ENSDARG00000075443", "ENSDARG00000079599", "ENSG00000211633", 
							 "ENSG00000242580", "ENSG00000211655", "ENSMUSG00000050742", 
							 "ENSXETG00000024927", "ENSXETG00000021946", "ENSXETG00000021946", 
							 "ENSXETG00000030835"));
		
        Method methodGenerateTOs = parser.getClass().getDeclaredMethod("generateTOsFromFile", String.class);
        methodGenerateTOs.setAccessible(true);
		methodGenerateTOs.invoke(parser, ParseOrthoXMLTest.class.getResource(OMAFILE).getPath());
		
		Field hierarchicalGroupTOs = parser.getClass().getDeclaredField("hierarchicalGroupTOs");
		hierarchicalGroupTOs.setAccessible(true);
		Set<HierarchicalGroupTO> setTO = 
				(Set<HierarchicalGroupTO>) hierarchicalGroupTOs.get(parser);
		for (Iterator<HierarchicalGroupTO> iterator = setTO.iterator(); iterator.hasNext();) {
			HierarchicalGroupTO hierarchicalGroupTO = iterator.next();
			
			log.debug("OMANodeId={}, OMAGroupId={}, left={}, right={}, taxRange={}", 
					hierarchicalGroupTO.getNodeId(), hierarchicalGroupTO.getOMAGroupId(),
					hierarchicalGroupTO.getNodeLeftBound(), 
					hierarchicalGroupTO.getNodeRightBound(),
					hierarchicalGroupTO.getNcbiTaxonomyId());
			hierarchicalGroupTO.getNodeId();

		}
		log.exit();
	}
	
	/**
     * Test {@link ParseOrthoXML#count(Group)}.
	 * @throws XMLStreamException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws FileNotFoundException
	 * @throws XMLParseException
	 */
	@Test
	public void testCountChildren() throws XMLStreamException, NoSuchMethodException, SecurityException, 
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, 
			FileNotFoundException, XMLParseException {
        log.entry();
        
        MockDAOManager mockManager = new MockDAOManager();
        ParseOrthoXML parser = new ParseOrthoXML(mockManager);
        
		OrthoXMLReader reader = new OrthoXMLReader(new File(
				ParseOrthoXMLTest.class.getResource(OMAFILE).getPath()));
	
        Method method = parser.getClass().getDeclaredMethod("countChildren", Group.class);
        method.setAccessible(true);
		
		// Count and check the number of group/subgroups in the first group
		Group group = reader.next();
		int i = (int) method.invoke(parser, group);
        assertEquals("False count: found "+i+" group/subgroup instead of 1", 1, i);
        
		// Count and check the number of group/subgroups in the second group
		group = reader.next();
		i = (int) method.invoke(parser, group);
        assertEquals("False count: found "+i+" group/subgroup instead of 9", 9, i);

		// Count and check the number of group/subgroups in the third group
		group = reader.next();
		i = (int) method.invoke(parser, group);
        assertEquals("False count: found "+i+" group/subgroup instead of 1", 1, i);
        log.exit();
	}

	public void testGetSpecies() {
	}
}
