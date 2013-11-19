package org.bgee.pipeline.species;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.OntologyUtils;
import org.bgee.pipeline.Utils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import owltools.graph.OWLGraphManipulator;
import owltools.graph.OWLGraphWrapper;
import owltools.ncbi.NCBI2OWL;

/**
 * Use the NCBI taxonomy data files to generate a taxonomy ontology, and stored it 
 * in OWL format. This taxonomy will include only taxa related to a specified 
 * list of taxa, and will include disjoint classes axions necessary to correctly infer 
 * taxon constraints, for ontologies using the "in_taxon" relations.
 * <p>
 * This class uses the {@code owltools.ncbi.NCBI2OWL} class written by James A. Overton 
 * to generate the ontology. It is needed to provide the {@code taxonomy.dat} file 
 * that can be found at {@code ftp://ftp.ebi.ac.uk/pub/databases/taxonomy/}. 
 * This approach is based on the 
 * <a href='http://sourceforge.net/p/obo/svn/HEAD/tree/ncbitaxon/trunk/src/ontology/Makefile'>
 * OBOFoundry Makefile</a>, used to generate the <a 
 * href='http://www.obofoundry.org/cgi-bin/detail.cgi?id=ncbi_taxonomy'>OBOFoundry 
 * ontology</a>. We need to generate the ontology ourselves because, as of Bgee 13, 
 * the official ontology does not include the last modifications that we requested 
 * to NCBI, and that were accepted (e.g., addition of a <i>Dipnotetrapodomorpha</i> term).
 * <p>
 * As explained on a Chris Mungall 
 * <a href='http://douroucouli.wordpress.com/2012/04/24/taxon-constraints-in-owl/'>
 * blog post</a>, it is also necessary to generate disjoint classes axioms. 
 * Our code is based on the method 
 * {@code owltools.cli.TaxonCommandRunner.createTaxonDisjointOverInTaxon(Opts)}. 
 * <p>
 * To avoid storing a too large ontology, it will contain only ancestors of a specified 
 * list of taxa.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class GenerateTaxonOntology {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = 
            LogManager.getLogger(GenerateTaxonOntology.class.getName());
    
    /**
     * A {@code String} that is the IRI of the {@code ObjectProperty} "in_taxon", 
     * used to generate taxon disjoint axioms.
     */
    protected final static String INTAXONRELID = "http://purl.obolibrary.org/obo/RO_0002162";
    
    /**
     * Main method to trigger the generation of a taxonomy ontology, stored in 
     * OWL format, based on the NCBI taxonomy data, containing disjoint classes 
     * axioms. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the {@code taxonomy.dat} file.
     * <li>path to a TSV files containing the IDs of the taxa used as anchors of 
     * the ontology to generate: only these taxa and their ancestors will be kept 
     * in the ontology. These IDs must correspond to the NCBI IDs, with an ontology 
     * prefix added (e.g., "NCBITaxon:9606" for human). The first line is a header line, 
     * the second column is optional and is present only for human readability. 
     * Only the first column is used by the pipeline. 
     * <li>path to the file to store the generated ontology in OWL format. So 
     * it must finish with {@code .owl}
     * </ol>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If {@code args} does not contain the proper 
     *                                  parameters.
     * @throws IOException  IF the {@code taxonomy.dat} file could not be opened, 
     *                      or an error occurred while saving the converted ontology. 
     * @throws OWLOntologyCreationException Can be thrown by during the conversion 
     *                                      from NCBI data to OWL ontology.
     * @throws OWLOntologyStorageException  Can be thrown by {@code NCBI2OWL} 
     *                                      during the conversion, and when saving 
     *                                      the generated ontology.
     */
    public static void main(String[] args) throws OWLOntologyCreationException, 
        OWLOntologyStorageException, IOException {
        log.entry((Object[]) args);
        
        int expectedArgLength = 3;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
            		"provided, expected " + expectedArgLength + " arguments, " + args.length + 
            		" provided."));
        }
        
        GenerateTaxonOntology generate = new GenerateTaxonOntology();
        OWLOntology ont = generate.generateOntology(args[0], Utils.getTaxonIds(args[1]));
        //save in OWL
        new OntologyUtils(ont).saveAsOWL(args[2]);
        
        log.exit();
    }
    
    /**
     * Generates a taxonomy ontology, based on the NCBI taxonomy data. 
     * This taxonomy will include only the specified taxa and their ancestors. 
     * It will also include the disjoint classes axioms necessary to correctly 
     * infer taxon constraints, for ontologies using the "in_taxon" relations.
     * 
     * @param taxDataFile       A {@code String} that is the path to the NCBI 
     *                          {@code taxonomy.dat} file.
     * @param taxonIds          A {@code Set} of {@code String}s that are 
     *                          the NCBI IDs of the taxa to keep in the ontology, 
     *                          along with their ancestors. These IDs must correspond 
     *                          to the NCBI IDs, with an ontology prefix added 
     *                          (e.g., "NCBITaxon:9606" for human). 
     * @return  The {@code OWLOntology} generated as a result.                    
     * @throws IllegalArgumentException If some IDs in {@code taxonIds} are not found 
     *                                  in the generated ontology.
     * @throws IOException  IF the {@code taxonomy.dat} file could not be opened, 
     *                      or an error occurred while saving the converted ontology. 
     * @throws OWLOntologyCreationException Can be thrown during the conversion 
     *                                      from NCBI data to OWL ontology, or during 
     *                                      the conversion of the OWL ontology into 
     *                                      an OBO ontology.
     * @throws OWLOntologyStorageException  Can be thrown by {@code NCBI2OWL} 
     *                                      during the conversion.
     */
    public OWLOntology generateOntology(String taxDataFile, Set<String> taxonIds) 
            throws IllegalArgumentException, OWLOntologyCreationException, 
            OWLOntologyStorageException, IOException {
        log.entry(taxDataFile, taxonIds);
        
        OWLOntology ont = this.ncbi2owl(taxDataFile);
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        this.filterOntology(wrapper, taxonIds);
        //add the disjoint axioms necessary to correctly infer taxon constraints 
        //at later steps.
        this.createTaxonDisjointAxioms(wrapper);
        
        return log.exit(wrapper.getSourceOntology());
    }
    
    /**
     * Generate the NCBI taxonomy ontology using files obtained from the NCBI FTP 
     * ({@code taxonomy.dat}). This method uses the class {@code owltools.ncbi.NCBI2OWL} 
     * written by James A. Overton. 
     * 
     * @param pathToTaxonomyData    A {@code String} representing the path to the 
     *                              {@code taxonomy.dat} file, used by {@code 
     *                              owltools.ncbi.NCBI2OWL}
     * @throws IOException  Can be thrown by {@code NCBI2OWL} during the conversion. 
     * @throws OWLOntologyCreationException Can be thrown by {@code NCBI2OWL} 
     *                                      during the conversion.
     * @throws OWLOntologyStorageException  Can be thrown by {@code NCBI2OWL} 
     *                                      during the conversion.
     */
    private OWLOntology ncbi2owl(String pathToTaxonomyData) 
            throws OWLOntologyCreationException, OWLOntologyStorageException, 
            IOException {
        log.entry(pathToTaxonomyData);
        log.info("Starting convertion from .dat file to OWLOntology...");
        OWLOntology ont = NCBI2OWL.convertToOWL(pathToTaxonomyData, null);
        log.info("Done converting .dat file to OWLOntology.");
        return log.exit(ont);
    }
    
    /**
     * Keep in the {@code OWLOntology} wrapped into {@code ontWrapper} only 
     * the taxa specified by {@code taxonIds}, and their ancestors.
     * 
     * @param ontWrapper    The {@code OWLGraphWrapper} into which the 
     *                      {@code OWLOntology} to modify is wrapped.
     * @param taxonIds      A {@code Set} of {@code String}s that are 
     *                      the ontology IDs of the taxa to keep in the ontology, 
     *                      along with their ancestors (for instance, {@code NCBITaxon:9606} 
     *                      for human).
     */
    private void filterOntology(OWLGraphWrapper ontWrapper, Set<String> taxonIds) {
        log.entry(ontWrapper, taxonIds);
        log.info("Start filtering ontology for taxa {}...", taxonIds);
        
        Set<OWLClass> owlClassesToKeep = new HashSet<OWLClass>();
        for (String taxonId: taxonIds) {
            OWLClass taxClass = ontWrapper.getOWLClassByIdentifier(taxonId);
            owlClassesToKeep.add(taxClass);
            owlClassesToKeep.addAll(ontWrapper.getOWLClassAncestors(taxClass));
        }
        OWLGraphManipulator manipulator = new OWLGraphManipulator(ontWrapper);
        manipulator.filterClasses(owlClassesToKeep);
        
        log.info("Done filtering ontology for taxa {}.", taxonIds);
        
        log.exit();
    }
    
    /**
     * Add to the {@code OWLOntology} wrapped into {@code ontWrapper} the disjoint 
     * classes axioms necessary to correctly infer taxon constraints, for ontologies 
     * using "in_taxon" relations. 
     * <p>
     * This necessity is explained on a Chris Mungall 
     * <a href='http://douroucouli.wordpress.com/2012/04/24/taxon-constraints-in-owl/'>
     * blog post</a>. The code is copied from the method 
     * {@code owltools.cli.TaxonCommandRunner.createTaxonDisjointOverInTaxon(Opts)}. 
     * We have not use this class directly because it creates a new ontology to store 
     * the axioms in it. 
     * 
     * @param ontWrapper    The {@code OWLGraphWrapper} into which the 
     *                      {@code OWLOntology} to modify is wrapped.
     * @throws IllegalStateException    If some axioms were not correctly added.
     */
    private void createTaxonDisjointAxioms(OWLGraphWrapper ontWrapper) {
        log.entry(ontWrapper);
        log.info("Start creating disjoint classes axioms...");
        
        OWLOntologyManager m = ontWrapper.getManager();
        OWLDataFactory f = m.getOWLDataFactory();
        OWLObjectProperty inTaxon = f.getOWLObjectProperty(IRI.create(INTAXONRELID));
        log.trace("in_taxon property created: {}", inTaxon);
        
        // add disjoints
        Deque<OWLClass> queue = new ArrayDeque<OWLClass>();
        queue.addAll(ontWrapper.getOntologyRoots());
        Set<OWLClass> done = new HashSet<OWLClass>();
        
        final OWLOntology ont = ontWrapper.getSourceOntology();
        int axiomCount = 0;
        OWLClass current;
        while ((current = queue.pollFirst()) != null) {
            if (done.add(current)) {
                Set<OWLSubClassOfAxiom> axioms = ont.getSubClassAxiomsForSuperClass(current);
                Set<OWLClass> siblings = new HashSet<OWLClass>();
                for (OWLSubClassOfAxiom ax : axioms) {
                    OWLClassExpression ce = ax.getSubClass();
                    if (!ce.isAnonymous()) {
                        OWLClass subCls = ce.asOWLClass();
                        siblings.add(subCls);
                        queue.offerLast(subCls);
                    }
                }
                if (siblings.size() > 1) {
                    log.trace("Disjoint axioms for siblings: {}", siblings);
                    Set<OWLAxiom> disjointAxioms = new HashSet<OWLAxiom>();
                    // create compact disjoint and disjoint over never_in_taxon axioms
                    disjointAxioms.add(f.getOWLDisjointClassesAxiom(siblings));
                    Set<OWLClassExpression> expressions = new HashSet<OWLClassExpression>();
                    for (OWLClass cls : siblings) {
                        expressions.add(f.getOWLObjectSomeValuesFrom(inTaxon, cls));
                    }
                    disjointAxioms.add(f.getOWLDisjointClassesAxiom(expressions));
                    
                    int changeMade = m.addAxioms(ont, disjointAxioms).size();
                    if (changeMade != disjointAxioms.size()) {
                        throw log.throwing(new IllegalStateException("Some disjoint axioms " +
                        		"could not be added among: " + disjointAxioms));
                    }
                    axiomCount += disjointAxioms.size();
                }
            }
        }
        
        
        log.info("Done creating disjoint classes axioms, created {} disjoint axioms.", 
                axiomCount);
        log.exit();
    }
}
