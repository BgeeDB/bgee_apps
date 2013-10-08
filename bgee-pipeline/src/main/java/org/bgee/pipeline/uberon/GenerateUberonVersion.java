package org.bgee.pipeline.uberon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import owltools.graph.OWLGraphManipulator;
import owltools.graph.OWLGraphWrapper;
import owltools.io.ParserWrapper;

public class GenerateUberonVersion 
{
	public static void main(String[] args) throws OWLOntologyCreationException, OBOFormatParserException, IOException, OWLOntologyStorageException 
	{
		ParserWrapper parserWrapper = new ParserWrapper();
        OWLOntology ont = parserWrapper.parse("/Users/admin/Desktop/uberon.owl");
    	OWLGraphManipulator graphManipulator = 
    			new OWLGraphManipulator(new OWLGraphWrapper(ont));
    	
    	//graphManipulator.makeBasicOntology();
    	/*//map all sub-relations of part_of and develops_from to these relations
    	Collection<String> relIds = new ArrayList<String>();
    	relIds.add("BFO:0000050");
    	relIds.add("RO:0002202");
    	int relsMapped = graphManipulator.mapRelationsToParent(relIds);
    	//keep only is_a, part_of and develops_from relations
    	int relsRemoved = graphManipulator.filterRelations(relIds, true);*/
    	
    	//graphManipulator.removeRelsToSubsets(Arrays.asList("upper_level"));
    	//graphManipulator.removeSubgraphs(Arrays.asList("UBERON:0000481"), true);
    	//graphManipulator.removeClassAndPropagateEdges("UBERON:0001459");
    	graphManipulator.reducePartOfIsARelations();
    	
    	Owl2Obo converter = new Owl2Obo();
    	OBODoc oboOntology = converter.convert(
    			graphManipulator.getOwlGraphWrapper().getSourceOntology());
    	OBOFormatWriter writer = new OBOFormatWriter();
    	writer.write(oboOntology, "/Users/admin/Desktop/custom_uberon.obo");
    	
    	File owlFile = new File("/Users/admin/Desktop/custom_uberon.owl");
    	OWLOntologyManager manager = graphManipulator.getOwlGraphWrapper().getManager();
    	OWLOntologyFormat originalFormat = manager.getOntologyFormat(
    			graphManipulator.getOwlGraphWrapper().getSourceOntology()); 
    	OWLXMLOntologyFormat owlXmlFormat = new OWLXMLOntologyFormat();
    	if (originalFormat.isPrefixOWLOntologyFormat()) {
    		owlXmlFormat.copyPrefixesFrom(originalFormat.asPrefixOWLOntologyFormat());
    	} 
    	manager.saveOntology(graphManipulator.getOwlGraphWrapper().getSourceOntology(), 
    			owlXmlFormat, IRI.create(owlFile.toURI()));
	}
}
