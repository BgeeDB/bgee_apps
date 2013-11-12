package org.bgee.pipeline.uberon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.OntologyUtils;
import org.bgee.pipeline.Utils;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.supercsv.cellprocessor.FmtBool;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import owltools.graph.OWLGraphWrapper;
import owltools.mooncat.SpeciesSubsetterUtil;

/**
 * Generates a TSV files allowing to know, for each {@code OWLClass} in the Uberon 
 * {@code OWLOntology}, in which taxa it exits, among the taxa provided through 
 * another TSV file, containing their NCBI ID.
 * <p>
 * This class is based on the {@code owltools.mooncat.SpeciesSubsetterUtil} from 
 * owltools. This tool allows to produce a version of a source ontology, containing 
 * only the {@code OWLClass}es existing in a given taxon, for one taxon at a time. 
 * So our approach will be to generate a version of the Uberon ontology for each 
 * of the taxa provided through the TSV file, and to merge the generated information 
 * into a TSV files, where lines are {@code OWLClass}es and columns are taxa.
 * <p>
 * It is possible to request to store the intermediate ontologies generated 
 * for each taxon by the {@code SpeciesSubsetterUtil}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class GenerateTaxonConstraints {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = 
            LogManager.getLogger(GenerateTaxonConstraints.class.getName());
    
    /**
     * The {@code OWLReasonerFactory} used to obtain {@code OWLReasoner}s, 
     * used to produce the taxon-specific ontologies.
     */
    private OWLReasonerFactory reasonerFactory;
    
    /**
     * Main method to trigger the generation of a TSV files, allowing to know, 
     * for each {@code OWLClass} in the Uberon {@code OWLOntology}, in which taxa 
     * it exits, among the taxa provided through another TSV file, containing 
     * their NCBI ID. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the source Uberon OWL ontology file.
     * <li>path to the TSV files containing the ID of the species used in Bgee, 
     * corresponding to the NCBI taxonomy ID (e.g., 9606 for human). The first line 
     * should be a header line, and first column should be the IDs. A second column 
     * can be present for human readability. 
     * <li>path to the generated TSV file, output of the method.
     * <li>OPTIONNAL: a path to a directory where to store the intermediate generated 
     * ontologies. If this parameter is provided, an ontology will be generated 
     * for each taxon, and stored in this directory, containing only 
     * the {@code OWLClass}es existing in that taxon. If not provided, the intermediate 
     * ontologies will not be stored. 
     * </ol>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException     If some taxa in the taxon file could not
     *                                      be found in the ontology.
     * @throws FileNotFoundException        If some files could not be found.
     * @throws IOException                  If some files could not be read/written.
     * @throws UnknownOWLOntologyException  If the ontology provided could not be used.
     * @throws OWLOntologyCreationException If the ontology provided could not be used.
     * @throws OBOFormatParserException     If the ontology provided could not be parsed. 
     */
    public static void main(String[] args) throws UnknownOWLOntologyException, 
        IllegalArgumentException, FileNotFoundException, OWLOntologyCreationException, 
        OBOFormatParserException, IOException {
        log.entry((Object[]) args);
        
        if (args.length < 3 || args.length > 4) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                    "provided, expected 3 to 4 arguments, " + args.length + 
                    " provided."));
        }
        
        String storeDir = null;
        if (args.length >= 4) {
            storeDir = args[3];
        }
        GenerateTaxonConstraints generate = new GenerateTaxonConstraints();
        generate.generateTaxonConstraints(args[0], args[1], args[2], storeDir);
        
        log.exit();
    }
    
    /**
     * Launches the generation of a TSV files, allowing to know, 
     * for each {@code OWLClass} in the Uberon ontology stored in {@code uberonFile}, 
     * in which taxa it exits, among the taxa provided through the TSV file 
     * {@code taxonFile}, containing their NCBI ID. The results will be stored 
     * in the TSV file {@code outputFile}. The approach is, for each taxon provided, 
     * to generate a custom version of the ontology, that will contain only the 
     * {@code OWLClass}es existing in this taxon. If you want to keep these intermediate  
     * generated ontologies, you need to provide the path {@code storeOntologyDir} 
     * where to store them. The ontology files will be named 
     * <code>uberon_subset_TAXONID.obo</code>. If {@code storeOntologyDir} is 
     * {@code null}, the intermediate ontology files will not be saved. 
     * 
     * @param uberonFile        A {@code String} that is the path to the Uberon 
     *                          ontology file.
     * @param taxonFile         A {@code String} that is the path to the TSV file 
     *                          containing the IDs from the NCBI website of the taxa 
     *                          to consider (for instance, 9606 for human). The first line 
     *                          should be a header line, and first column should be the IDs. 
     *                          A second column can be present for human readability.
     * @param outputFile        A {@code String} that is the path to the generated 
     *                          TSV file, output of the method. It will have one header line. 
     *                          The columns will be: ID of the Uberon classes, IDs of each 
     *                          of the taxa that were examined. For each of the taxon column, 
     *                          a boolean is provided as "T" or "F", to define whether 
     *                          the associated Uberon class exists in it.
     * @param storeOntologyDir  A {@code String} that is the path to a directory 
     *                          where to store intermediate ontologies. If {@code null} 
     *                          the generated ontologies will not be stored.
     * @throws IllegalArgumentException     If some taxa in {@code taxonFile} could not
     *                                      be found in the ontology.
     * @throws FileNotFoundException        If some files could not be found.
     * @throws IOException                  If some files could not be read/written.
     * @throws UnknownOWLOntologyException  If the ontology stored in 
     *                                      {@code uberonFile} could not be used.
     * @throws OWLOntologyCreationException If the ontology stored in {@code uberonFile} 
     *                                      could not be used.
     * @throws OBOFormatParserException     If {@code uberonFile} could not be parsed. 
     */
    public void generateTaxonConstraints(String uberonFile, String taxonFile, 
            String outputFile, String storeOntologyDir) throws IllegalArgumentException, 
            FileNotFoundException, IOException, UnknownOWLOntologyException, 
            OWLOntologyCreationException, OBOFormatParserException {
        
        log.entry(uberonFile, taxonFile, outputFile, storeOntologyDir);
        
        Set<Integer> speciesIds = Utils.getSpeciesIds(taxonFile);
        Map<String, Set<Integer>> constraints = 
                this.generateTaxonConstraints(uberonFile, speciesIds, storeOntologyDir);
        this.writeToFile(constraints, speciesIds, outputFile);
        
        log.exit();
    }
    
    /**
     * Returns a {@code Map} representing the taxon constraints generated from 
     * the ontology stored in {@code uberonFile}, for the taxa provided through 
     * {@code taxonIds}. The returned {@code Map} contains the OBO-like IDs of all 
     * {@code OWLClass}es present in the ontology, as keys, associated to 
     * a {@code Set} of {@code Integer}s, that are the IDs of the taxa in which 
     * it exists, among the provided taxa. If the {@code Set} is empty, then 
     * it means that the {@code OWLClass} existed in none of the provided taxa. 
     * <p>
     * The approach is, for each taxon provided, 
     * to generate a custom version of the ontology, that will contain only the 
     * {@code OWLClass}es existing in this taxon. If you want to keep these intermediate  
     * generated ontologies, you need to provide the path {@code storeOntologyDir} 
     * where to store them. The ontology files will be named 
     * <code>uberon_subset_TAXONID.obo</code>. If {@code storeOntologyDir} is 
     * {@code null}, the intermediate ontology files will not be saved. 
     * 
     * @param uberonFile        A {@code String} that is the path to the Uberon 
     *                          ontology file.
     * @param taxonIds          A {@code Set} of {@code Integer}s that are the IDs 
     *                          from the NCBI website of the taxa to consider 
     *                          (for instance, 9606 for human).
     * @param storeOntologyDir  A {@code String} that is the path to a directory 
     *                          where to store intermediate ontologies. If {@code null} 
     *                          the generated ontologies will not be stored.
     * @return                  A {@code Map} where keys are IDs of the {@code OWLClass}es 
     *                          from the provided ontology, and values are  
     *                          {@code Set}s of {@code Integer}s containing the IDs 
     *                          of taxa in which the {@code OWLClass} exists.
     * @throws UnknownOWLOntologyException  If the ontology stored in 
     *                                      {@code uberonFile} could not be used.
     * @throws OWLOntologyCreationException If the ontology stored in {@code uberonFile} 
     *                                      could not be used.
     * @throws OBOFormatParserException     If {@code uberonFile} could not be parsed. 
     * @throws IOException                  If {@code uberonFile} could not be opened. 
     */
    public Map<String, Set<Integer>> generateTaxonConstraints(String uberonFile, 
            Set<Integer> taxonIds, String storeOntologyDir) 
                    throws UnknownOWLOntologyException, OWLOntologyCreationException, 
                    OBOFormatParserException, IOException {
        log.entry(uberonFile, taxonIds, storeOntologyDir);

        OWLGraphWrapper uberonWrapper = 
                new OWLGraphWrapper(OntologyUtils.loadOntology(uberonFile));
        Map<String, Set<Integer>> taxonConstraints = new HashMap<String, Set<Integer>>();
        for (OWLClass currentClass: uberonWrapper.getAllOWLClasses()) {
            taxonConstraints.put(uberonWrapper.getIdentifier(currentClass), 
                    new HashSet<Integer>());
        }
        
        for (int taxonId: taxonIds) {
            Set<OWLClass> classesDefined = this.getExistingOWLClasses(
                    uberonFile, taxonId, storeOntologyDir);
            for (OWLClass classDefined: classesDefined) {
                taxonConstraints.get(uberonWrapper.getIdentifier(classDefined)).add(taxonId);
            }
        }
        
        return log.exit(taxonConstraints);
    }
    
    /**
     * Returns a {@code Set} of {@code OWLClass}es obtained from the ontology 
     * stored in {@code uberonFile}, and that actually exists in the taxon with ID 
     * {@code taxonId}. If {@code storeOntologyDir} is not {@code null}, then 
     * the intermediate ontology, corresponding to the filtered version of the source 
     * ontology for the provided taxon, will be saved in that directory.
     * 
     * @param uberonFile        A {@code String} that is the path to the Uberon 
     *                          ontology file.
     * @param taxonId           An {@code int} that is the ID on the NCBI website 
     *                          of the taxon to consider (for instance, 9606 for human).
     * @param storeOntologyDir  A {@code String} that is the path to a directory 
     *                          where to store intermediate ontologies. If {@code null} 
     *                          the generated ontologies will not be stored.
     * @return                  A {@code Set} containing {@code OWLClass}es 
     *                          from the provided ontology, that exists in 
     *                          the provided taxon.
     * @throws UnknownOWLOntologyException  If the ontology stored in 
     *                                      {@code uberonFile} could not be used.
     * @throws OWLOntologyCreationException If the ontology stored in {@code uberonFile} 
     *                                      could not be used.
     * @throws OBOFormatParserException     If {@code uberonFile} could not be parsed. 
     * @throws IOException                  If {@code uberonFile} could not be opened. 
     */
    private Set<OWLClass> getExistingOWLClasses(String uberonFile, int taxonId, 
            String storeOntologyDir) throws UnknownOWLOntologyException, 
            OWLOntologyCreationException, OBOFormatParserException, IOException  {
        log.entry(uberonFile, taxonId, storeOntologyDir);
        
        //as there is no easy way to clone an ontology before modifying it, 
        //we need to reload it each time this method is called
        OWLGraphWrapper uberonWrapper = 
                new OWLGraphWrapper(OntologyUtils.loadOntology(uberonFile));
        
        //Get the OWLClass corresponding to the requested taxon
        String ontTaxonId = OntologyUtils.getTaxOntologyId(taxonId);
        OWLClass taxClass = uberonWrapper.getOWLClassByIdentifier(ontTaxonId);
        if (taxClass == null) {
            throw log.throwing(new IllegalArgumentException("A taxon ID " +
                    "provided could not be found in the provided ontology: " + 
                    taxonId));
        }
        
        //filter ontology
        SpeciesSubsetterUtil subSetter = new SpeciesSubsetterUtil(uberonWrapper);
        subSetter.taxClass = taxClass;
        subSetter.reasoner = this.createReasoner(uberonWrapper.getSourceOntology());
        subSetter.removeOtherSpecies();
        
        //if we want to store the intermediate ontology
        if (storeOntologyDir != null) {
            uberonWrapper.clearCachedEdges();
            String outputFilePath = new File(storeOntologyDir, 
                    "uberon_subset" + taxonId + ".obo").getPath();
            new OntologyUtils(uberonWrapper).saveAsOBO(outputFilePath);
        }
        
        return log.exit(uberonWrapper.getAllOWLClasses());
    }
    

    /**
     * Creates and returns an {@code OWLReasoner} to reason on the provided 
     * {@code OWLOntology}. If no {@code OWLReasonerFactory} was previously 
     * provided (see {@link #setReasonerFactory(OWLReasonerFactory)}), 
     * then the {@code ElkReasonerFactory} will be used (thus this method 
     * would return {@code ElkReasoner}s).
     * 
     * @param ont  The {@code OWLOntology} which the returned {@code OWLReasoner} 
     *              should reason on.
     * @return      An <code>OWLReasoner</code> set to reason on {@code ont}.
     */
    private OWLReasoner createReasoner(OWLOntology ont) {
        log.entry(ont);
        if (this.reasonerFactory == null) {
            this.setReasonerFactory(new ElkReasonerFactory());
        }
        return log.exit(this.reasonerFactory.createReasoner(ont));
    }
    /**
     * Sets the {@code OWLReasonerFactory}, used to obtain {@code OWLReasoner}s, 
     * used to produce the taxon-specific ontologies. Otherwise, by default, 
     * the {@code ElkReasonerFactory} will be used (see 
     * {@link #createReasoner()}).
     * 
     * @param factory   the {@code OWLReasonerFactory} to use.
     */
    public void setReasonerFactory(OWLReasonerFactory factory) {
        log.entry(factory);
        this.reasonerFactory = factory;
        log.exit();
    }
    
    /**
     * Write the taxon constraints in a TSV file. The taxon constraints are provided 
     * by {@code taxonConstraints} as a {@code Map} where keys are the OBO-like IDs 
     * of all {@code OWLClass}es examined, and are associated to a {@code Set} of 
     * {@code Integer}s, that are the NCBI IDs of the taxon in which the {@code OWLClass} 
     * exists, among all the taxa that were examined, listed in {@code taxonIds}. 
     * <p>
     * The generated TSV file will have one header line. The columns will be: ID 
     * of the {@code OWLClass}, IDs of each of the taxa that were examined. For 
     * each of the taxon column, a boolean is provided as "T" or "F", to define 
     * whether the associated {@code OWLClass} exists in it.
     * 
     * @param taxonConstraints  A {@code Map} where keys are IDs of the {@code OWLClass}es 
     *                          from the ontology that was examined, and values are  
     *                          {@code Set}s of {@code Integer}s containing the IDs 
     *                          of taxa in which the {@code OWLClass} exists.
     * @param taxonIds          A {@code Set} of {@code Integer}s that are the IDs 
     *                          from the NCBI website of the taxa that were considered 
     *                          (for instance, 9606 for human).
     * @param outputFile        A {@code String} that is the path to the output file 
     *                          were constraints will be written as TSV.
     * @throws IOException      If an error occurred while trying to write to 
     *                          {@code outputFile}.
     */
    private void writeToFile(Map<String, Set<Integer>> taxonConstraints, 
            Set<Integer> taxonIds, String outputFile) throws IOException {
        log.entry(taxonConstraints, taxonIds, outputFile);

        //order the taxon IDs to get consistent column ordering between releases
        List<Integer> sortedTaxonIds = new ArrayList<Integer>(taxonIds);
        Collections.sort(sortedTaxonIds);
        //also, ordered the IDs of the OWLClasses, for easier comparison between 
        //releases
        List<String> sortedClassIds = new ArrayList<String>(taxonConstraints.keySet());
        Collections.sort(sortedClassIds);
        
        //create the header of the file, and the conditions on the columns
        int taxonCount = taxonIds.size();
        CellProcessor[] processors = new CellProcessor[taxonCount + 1];
        String[] header = new String[taxonCount + 1];
        //ID of the OWLClass (must be unique)
        processors[0] = new UniqueHashCode(new NotNull());
        header[0] = "UBERON ID";
        //boolean defining for each taxon if the OWLClass exists in it
        for (int i = 0; i < taxonCount; i++) {
            processors[i + 1] = new NotNull(new FmtBool("T", "F"));
            header[i + 1] = sortedTaxonIds.get(i).toString();
        }
        
        
        try (ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(outputFile),
                CsvPreference.TAB_PREFERENCE)) {
            
            mapWriter.writeHeader(header);
            
            for (String uberonId: sortedClassIds) {
                Map<String, Object> row = new HashMap<String, Object>();
                row.put(header[0], uberonId);
                for (Integer taxonId: taxonIds) {
                    row.put(taxonId.toString(), 
                            taxonConstraints.get(uberonId).contains(taxonId));
                }
                
                mapWriter.write(row, header, processors);
            }
        }
        
        log.exit();
    }
}
