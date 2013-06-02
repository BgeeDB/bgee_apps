package org.bgee.pipeline.uberon;

import java.io.IOException;
import java.util.Arrays;

import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owltools.graph.OWLGraphWrapper;
import owltools.io.ParserWrapper;

public class GenerateUberonVersion 
{
	public static void main(String[] args) throws OWLOntologyCreationException, OBOFormatParserException, IOException 
	{
		ParserWrapper parserWrapper = new ParserWrapper();
        OWLOntology ont = parserWrapper.parse("/Users/admin/Desktop/uberon.obo.1");
    	OWLGraphManipulator graphManipulator = 
    			new OWLGraphManipulator(new OWLGraphWrapper(ont));
    	
    	//graphManipulator.makeBasicOntology();
    	graphManipulator.delPartOfSubClassOfRelsToSubsetsIfNonOrphan(Arrays.asList("upper_level"));
    	graphManipulator.removeSubgraphs(Arrays.asList("UBERON:0000481"), true);
    	//graphManipulator.removeClassAndPropagateEdges("UBERON:0001459");
    	//graphManipulator.reducePartOfAndSubClassOfRelations();
    	
    	Owl2Obo converter = new Owl2Obo();
    	OBODoc oboOntology = converter.convert(
    			graphManipulator.getOwlGraphWrapper().getSourceOntology());
    	OBOFormatWriter writer = new OBOFormatWriter();
    	writer.write(oboOntology, "/Users/admin/Desktop/uberon_test2.obo");
	}
}
