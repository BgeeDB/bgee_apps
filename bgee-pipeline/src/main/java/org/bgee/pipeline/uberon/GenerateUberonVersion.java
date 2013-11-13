package org.bgee.pipeline.uberon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bgee.pipeline.OntologyUtils;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;

import owltools.graph.OWLGraphManipulator;
import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;
import owltools.io.ParserWrapper;

public class GenerateUberonVersion 
{
	public static void main(String[] args) throws OWLOntologyCreationException, OBOFormatParserException, IOException, OWLOntologyStorageException 
	{
        OWLOntology ont = OntologyUtils.loadOntology("/Users/admin/Desktop/uberon.owl");
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
    	OWLGraphManipulator graphManipulator = 
    			new OWLGraphManipulator(wrapper);
    	
    	OWLClass testClass = wrapper.getOWLClassByIdentifier("UBERON:0010032");
    	System.out.println("1: " + testClass);
    	for (OWLGraphEdge edge: wrapper.getOutgoingEdges(testClass)) {
    	    System.out.println("2: " + edge);
    	}
    	
    	
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
    	//graphManipulator.reducePartOfIsARelations();
    	
    	/*Owl2Obo converter = new Owl2Obo();
    	OBODoc oboOntology = converter.convert(
    			graphManipulator.getOwlGraphWrapper().getSourceOntology());
    	OBOFormatWriter writer = new OBOFormatWriter();
    	writer.write(oboOntology, "/Users/admin/Desktop/custom_uberon_nothing.obo");
        
        File rdfFile = new File("/Users/admin/Desktop/custom_uberon_nothing_rdf.owl"); 
        OWLOntologyManager manager = graphManipulator.getOwlGraphWrapper().getManager();
        RDFXMLOntologyFormat owlRdfFormat = new RDFXMLOntologyFormat();
        if (owlRdfFormat.isPrefixOWLOntologyFormat()) {
            owlRdfFormat.copyPrefixesFrom(owlRdfFormat.asPrefixOWLOntologyFormat());
        } 
        manager.saveOntology(graphManipulator.getOwlGraphWrapper().getSourceOntology(), 
                owlRdfFormat, IRI.create(rdfFile.toURI()));*/
	}
}
