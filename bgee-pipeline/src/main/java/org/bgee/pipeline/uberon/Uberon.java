package org.bgee.pipeline.uberon;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.OntologyUtils;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;

/**
 * Class related to the use, and insertion into the database of the ontology Uberon.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class Uberon {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = 
            LogManager.getLogger(Uberon.class.getName());
    
    /**
     * Several actions can be launched from this main method, depending on the first 
     * element in {@code args}: 
     * <ul>
     * <li>If the first element in {@code args} is "extractTaxonIds", the action 
     * will be to extract from the Uberon ontology all NCBI taxon IDs that are the targets 
     * of {@code OWLRestriction}s over the object properties "in taxon" (or any 
     * sub-properties), or that are used in ontology annotations 
     * "treat-xrefs-as-reverse-genus-differentia", and to write them in a file.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the Uberon ontology (a version making use of such restrictions...).
     *   <li>path to the output file where to write taxon IDs into, one per line.
     *   </ol>
     * </ul>
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If {@code args} does not contain the proper 
     *                                  parameters or does not allow to obtain 
     *                                  correct information.
     */
    public static void main(String[] args) throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        log.entry((Object[]) args);
        
        if (args[0].equalsIgnoreCase("extractTaxonIds")) {
            if (args.length != 3) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "3 arguments, " + args.length + " provided."));
            }
            
            new Uberon().extractTaxonIds(args[1], args[2]);
        }
        
        log.exit();
    }
    
    /**
     * Extract from the Uberon ontology all NCBI taxon IDs that are the targets 
     * of {@code OWLRestriction}s over the object properties "in taxon" (or any 
     * sub-properties), or that are used in ontology annotations 
     * "treat-xrefs-as-reverse-genus-differentia", and to write them in a file.
     * The IDs used are {@code Integer}s that are the NCBI IDs (for instance, 
     * 9606 for human), not the ontology IDs with a prefix ("NCBITaxon:").
     * 
     * @param uberonFile    A {@code String} that is the path to the Uberon ontology file.
     * @param outputFile    A {@code String} that is the path to the file where 
     *                      to write IDs into.
     * @throws IllegalArgumentException     If {@code uberonFile} did not allow to obtain 
     *                                      any valid taxon ID, or was incorrectly formatted.
     * @throws OWLOntologyCreationException If an error occurred while loading 
     *                                      the ontology.
     * @throws OBOFormatParserException     If an error occurred while loading 
     *                                      the ontology.
     * @throws IOException                  If {@code uberonFile} could not be read, 
     *                                      or the output could not be written in file.
     */
    public void extractTaxonIds(String uberonFile, String outputFile) 
            throws OWLOntologyCreationException, OBOFormatParserException, 
            IllegalArgumentException, IOException {
        log.entry(uberonFile, outputFile);
        
        Set<Integer> taxonIds = this.extractTaxonIds(uberonFile);
        try(PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile), "utf-8")))) {
            for (int taxonId: taxonIds) {
                writer.println(taxonId);
            }
        }
        
        log.exit();
    }
    
    /**
     * Extract from the Uberon ontology all taxon IDs that are the targets 
     * of {@code OWLRestriction}s over the object properties "in taxon" (or any 
     * sub-properties), or that are used in ontology annotations 
     * "treat-xrefs-as-reverse-genus-differentia". The IDs returned are {@code Integer}s 
     * that are the NCBI IDs (for instance, 9606 for human), not the ontology IDs 
     * with a prefix ("NCBITaxon:").
     * 
     * @param uberonFile    A {@code String} that is the path to the Uberon ontology file.
     * @return              A {@code Set} of {@code Integer}s that are the NCBI IDs 
     *                      of the taxa used in Uberon as target of restrictions over 
     *                      "in taxon" object properties, or any sub-properties.
     * @throws IllegalArgumentException     If {@code uberonFile} did not allow to obtain 
     *                                      any valid taxon ID or was incorrectly formatted.
     * @throws OWLOntologyCreationException If an error occurred while loading 
     *                                      the ontology.
     * @throws OBOFormatParserException     If an error occurred while loading 
     *                                      the ontology.
     * @throws IOException                  If {@code uberonFile} could not be read.
     */
    public Set<Integer> extractTaxonIds(String uberonFile) 
            throws OWLOntologyCreationException, OBOFormatParserException, 
            IOException, IllegalArgumentException {
        log.entry(uberonFile);

        OWLOntology ont = OntologyUtils.loadOntology(uberonFile);
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OWLDataFactory factory = wrapper.getManager().getOWLDataFactory();
        
        Set<OWLObject> taxa = new HashSet<OWLObject>();
        Set<String> taxonIds = new HashSet<String>();
        
        //will get taxon IDs from axioms over object properties "in_taxon", 
        //"evolved_multiple_times_in" (or any sub-properties)
        Set<OWLObjectPropertyExpression> objectProps = this.getTaxonObjectProperties(wrapper);
        //will also get the taxon IDs from annotation axioms over annotation properties
        //"ambiguous_for_taxon", "dubious_for_taxon", "homologous_in","never_in_taxon", 
        //"RO:0002161", "present_in_taxon", "taxon"
        Set<OWLAnnotationProperty> annotProps = this.getTaxonAnnotationProperties(wrapper);
        
        for (OWLAxiom ax : ont.getAxioms()) {
            Set<OWLEntity> signature = ax.getSignature();
            if ((ax.isLogicalAxiom() || ax.isAnnotationAxiom()) && 
                 !(ax instanceof OWLSubAnnotationPropertyOfAxiom) && 
                 !(ax instanceof OWLSubObjectPropertyOfAxiom) && 
                 
               (!Collections.disjoint(signature, objectProps) || 
                    !Collections.disjoint(signature, annotProps))) {
                log.trace("Validating axiom {}", ax);
            }
        }
        
        for (OWLObject taxon: taxa) {
            taxonIds.add(wrapper.getIdentifier(taxon));
        }
        
        //now we get the "treat-xrefs-as-reverse-genus-differentia" ontology annotations
        OWLAnnotationProperty genusDifferentia = 
                factory.getOWLAnnotationProperty(OntologyUtils.GENUS_DIFFERENTIA_IRI);
        for (OWLAnnotation annot: ont.getAnnotations()) {
            if (annot.getProperty().equals(genusDifferentia)) {
                String value = ((OWLLiteral) annot.getValue()).getLiteral();
                Matcher m = OntologyUtils.GENUS_DIFFERENTIA_LITERAL_PATTERN.matcher(value);
                if (m.matches()) {
                    taxonIds.add(m.group(OntologyUtils.GENUS_DIFFERENTIA_TAXON_GROUP));
                } else {
                    throw log.throwing(new IllegalArgumentException("The provided ontology " +
                    		"contains genus-differentia annotations that does not match " +
                    		"the expected pattern"));
                }
            }
        }
        
        
        if (taxonIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The provided ontology " +
                    " did not allow to acquire any taxon ID"));
        }
        return log.exit(OntologyUtils.convertToNcbiIds(taxonIds));
    }
    
    /**
     * Obtain from the {@code OWLOntology} wrapped into {@code wrapper} all its 
     * {@code OWLObjectProperty}s that can lead to {@code OWLClass}es representing taxa.
     * 
     * @param wrapper   The {@code OWLGraphWrapper} to use to obtain the object properties.
     * @return          A {@code Set} of {@code OWLObjectPropertyExpression}s that 
     *                  can be used to retrieve {@code OWLClass}es representing taxa.
     */
    private Set<OWLObjectPropertyExpression> getTaxonObjectProperties(OWLGraphWrapper wrapper) {
        log.entry(wrapper);
        
        //get object properties "in_taxon" and "evolved_multiple_times_in", 
        //and any sub-properties
        OWLDataFactory factory = wrapper.getManager().getOWLDataFactory();
        Set<OWLObjectPropertyExpression> objectProps = 
                new HashSet<OWLObjectPropertyExpression>();
        
        OWLObjectProperty inTaxon = 
                factory.getOWLObjectProperty(OntologyUtils.IN_TAXON_IRI);
        if (inTaxon != null) {
            objectProps.addAll(wrapper.getSubPropertyReflexiveClosureOf(inTaxon));
        }
        
        OWLObjectProperty evolved = 
                factory.getOWLObjectProperty(OntologyUtils.EVOLVED_MULTIPLE_TIMES_IRI);
        if (evolved != null) {
            objectProps.addAll(wrapper.getSubPropertyReflexiveClosureOf(evolved));
        }
        
        return log.exit(objectProps);
    }

    
    /**
     * Obtain from the {@code OWLOntology} wrapped into {@code wrapper} all its 
     * {@code OWLAnnotationProperty}s that can lead to {@code OWLClass}es representing taxa.
     * 
     * @param wrapper   The {@code OWLGraphWrapper} to use to obtain the annotation properties.
     * @return          A {@code Set} of {@code OWLAnnotationProperty}s that can be used 
     *                  to retrieve {@code OWLClass}es representing taxa.
     */
    private Set<OWLAnnotationProperty> getTaxonAnnotationProperties(OWLGraphWrapper wrapper) {
        log.entry(wrapper);
        
        //get object properties "ambiguous_for_taxon", "dubious_for_taxon", 
        //"homologous_in","never_in_taxon", "RO:0002161", "present_in_taxon", 
        //"taxon", and any sub-properties
        OWLDataFactory factory = wrapper.getManager().getOWLDataFactory();
        Set<OWLAnnotationProperty> annotProps = new HashSet<OWLAnnotationProperty>();
        
        OWLAnnotationProperty prop = 
                factory.getOWLAnnotationProperty(OntologyUtils.AMBIGUOUS_FOR_TAXON_IRI);
        if (prop != null) {
            annotProps.addAll(wrapper.getSubAnnotationPropertyReflexiveClosureOf(prop));
        }
        prop = factory.getOWLAnnotationProperty(OntologyUtils.DUIOUS_FOR_TAXON_IRI);
        if (prop != null) {
            annotProps.addAll(wrapper.getSubAnnotationPropertyReflexiveClosureOf(prop));
        }
        prop = factory.getOWLAnnotationProperty(OntologyUtils.HOMOLOGOUS_IN_IRI);
        if (prop != null) {
            annotProps.addAll(wrapper.getSubAnnotationPropertyReflexiveClosureOf(prop));
        }
        prop = factory.getOWLAnnotationProperty(OntologyUtils.NEVER_IN_TAXON_IRI);
        if (prop != null) {
            annotProps.addAll(wrapper.getSubAnnotationPropertyReflexiveClosureOf(prop));
        }
        prop = factory.getOWLAnnotationProperty(OntologyUtils.NEVER_IN_TAXON_BIS_IRI);
        if (prop != null) {
            annotProps.addAll(wrapper.getSubAnnotationPropertyReflexiveClosureOf(prop));
        }
        prop = factory.getOWLAnnotationProperty(OntologyUtils.PRESENT_IN_TAXON_IRI);
        if (prop != null) {
            annotProps.addAll(wrapper.getSubAnnotationPropertyReflexiveClosureOf(prop));
        }
        prop = factory.getOWLAnnotationProperty(OntologyUtils.TAXON_IRI);
        if (prop != null) {
            annotProps.addAll(wrapper.getSubAnnotationPropertyReflexiveClosureOf(prop));
        }
        
        return log.exit(annotProps);
    }
}
