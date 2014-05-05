package org.bgee.pipeline.uberon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.OntologyUtils;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;

public class GenerateUberonVersion {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = 
            LogManager.getLogger(GenerateUberonVersion.class.getName());
    
    /**
     * Main method to trigger the generation of a custom simplified version of Uberon 
     * for Bgee. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the source Uberon composite-metazan OWL ontology file.
     * <li>path to the taxon constraints TSV file, generated at a previous step 
     * (see {@link #TaxonConstraints}). The fist line should be a header line. 
     * The columns are: ID of the Uberon classes, IDs of each of the taxa that 
     * were examined. For each of the taxon columns, a boolean is provided 
     * as "T" or "F", to define whether the associated Uberon class exists in it.
     * <li>path to the tsv files containing the species used in Bgee. The first line should be 
     * a header line, defining at least a column named exactly as 
     * {@link org.bgee.pipeline.Utils#TAXONCOLUMNNAME}.
     * The IDs should correspond to the NCBI taxonomy ID (e.g., 9606 for human).
     * </ol>
     * Note that if a taxon is defined in the Bgee species file, but not in 
     * the taxon constraints file, an {@code IllegalArgumentException} will be thrown.
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     */
    public static void main(String[] args) {
        log.entry((Object[]) args);
        
        log.exit();
    }
    
    public void generate(String uberonOntFile, String taxonFile) 
            throws UnknownOWLOntologyException, OWLOntologyCreationException, 
            OBOFormatParserException, IOException {
        log.entry(uberonOntFile, taxonFile);
        
        OWLGraphManipulator graphManipulator = 
                new OWLGraphManipulator(OntologyUtils.loadOntology(uberonOntFile));
        
        log.exit();
    }
    
    //TODO: see org.bgee.pipeline.uberon.TaxonConstraints.extractTaxonConstraints(String) to get taxonConstraints (as strings?)
    //or reverse the Map? (taxonIds as keys, Uberon classes existing as values)
    public OWLGraphWrapper generate(OWLGraphWrapper uberonOntWrapper, Map<String, Set<Integer>> taxonConstraints) 
            throws UnknownOWLOntologyException, OWLOntologyCreationException {
        log.entry(uberonOntWrapper, taxonConstraints);
        
        //first generate main version
        this.generate(uberonOntWrapper);
        
        //then, one version for each taxon, just by removing classes not existing
        
        //TODO
        return log.exit(null);
    }
    
    public OWLGraphWrapper generate(OWLGraphWrapper uberonOntWrapper) 
            throws UnknownOWLOntologyException, OWLOntologyCreationException {
        log.entry(uberonOntWrapper);
        
        //first generate main version
        
        //TODO
        return log.exit(null);
    }
    
	public static void yo() throws OWLOntologyCreationException, OBOFormatParserException, IOException, OWLOntologyStorageException 
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
    	
    	//map all sub-relations of part_of and develops_from to these relations
    	Collection<String> relIds = new ArrayList<String>();
    	relIds.add("BFO:0000050");
    	relIds.add("RO:0002202");
    	int relsMapped = graphManipulator.mapRelationsToParent(relIds);
    	//keep only is_a, part_of and develops_from relations
    	int relsRemoved = graphManipulator.filterRelations(relIds, true);
    	
    	graphManipulator.removeRelsToSubsets(Arrays.asList("upper_level"));
    	graphManipulator.removeSubgraphs(Arrays.asList("UBERON:0000481"), true);
    	graphManipulator.removeClassAndPropagateEdges("UBERON:0001459");
    	graphManipulator.reducePartOfIsARelations();
    	
    	Owl2Obo converter = new Owl2Obo();
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
                owlRdfFormat, IRI.create(rdfFile.toURI()));
	}
}
