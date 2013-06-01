package org.bgee.pipeline.hierarchicalGroups;

import java.io.File;
import java.io.FileNotFoundException;

import javax.management.modelmbean.XMLParseException;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;

import static org.mockito.Mockito.*;

import sbc.orthoxml.Group;
import sbc.orthoxml.io.OrthoXMLReader;

/**
 * Tests the functions of {@link #org.bgee.pipeline.hierarchicalGroups.ParseOrthoXML}
 * 
 * @author Komal
 *
 */
public class ParseOrthoXMLTest extends TestAncestor {
	
	private static long hierarchicalGroupId = 1;
	private static long nestedSetId = 1;

	private final static Logger log = LogManager
			.getLogger(ParseOrthoXMLTest.class.getName());

	private ParseOrthoXML parser;

	public ParseOrthoXMLTest(){
		super();
	}
	
	
	public void testParseXML() {
		
		log.debug("Testing if the OrthoXML file is parsed correctly..");

		parser.setOrthoXmlFile(this.getClass()
				.getResource("/orthoxml/HierarchicalGroupsTest.orthoxml")
				.toString());

	}

	public void testBuildTree() throws FileNotFoundException, XMLStreamException, XMLParseException {
		
		File file = new File(parser.getOrthoXmlFile());
		
		OrthoXMLReader reader = new OrthoXMLReader(file);

		ParseOrthoXMLTest.hierarchicalGroupId = 1;
		Group group;

		// read all the groups in the file iteratively
		while ((group = reader.next()) != null) {
			
			if (log.isDebugEnabled()) {
				log.debug("OrthologusGroupId: {}", group.getId());
			}

			Node rootNode = new Node();
			rootNode.setHierarchicalGroupId(ParseOrthoXMLTest.hierarchicalGroupId++);

			if (log.isInfoEnabled()) {
				log.info("Building Tree..");
			}
			// Build the tree of the current group
			ParseOrthoXML.buildTree(group, rootNode);

		}
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
