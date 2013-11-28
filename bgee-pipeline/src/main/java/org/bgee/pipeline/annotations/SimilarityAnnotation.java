package org.bgee.pipeline.annotations;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.uberon.TaxonConstraints;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;

import owltools.graph.OWLGraphWrapper;

/**
 * Class related to the use, verification, and insertion into the database 
 * of the annotations of similarity between Uberon terms (similarity in the sense 
 * of the term in the HOM ontology HOM:0000000).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class SimilarityAnnotation {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = 
            LogManager.getLogger(SimilarityAnnotation.class.getName());
    /**
     * A {@code String} that is the name of the column containing the entity IDs 
     * in the similarity annotation file (for instance, "UBERON:0001905|UBERON:0001787").
     */
    public final static String ENTITY_COL_NAME = "entity";
    /**
     * A {@code String} that is the name of the column containing the entity names 
     * in the similarity annotation file (for instance, 
     * "pineal body|photoreceptor layer of retina").
     */
    public final static String ENTITY_NAME_COL_NAME = "entity name";
    /**
     * A {@code String} that is the name of the column containing the qualifier 
     * in the similarity annotation file (for instance, "NOT" to state the an entity 
     * is <strong>not</stong> homologous in a taxon).
     */
    public final static String QUALIFIER_COL_NAME = "qualifier";
    /**
     * A {@code String} that is the name of the column containing the HOM IDs 
     * of terms from the ontology of homology and related concepts.
     */
    public final static String HOM_COL_NAME = "HOM ID";
    /**
     * A {@code String} that is the name of the column containing the HOM names 
     * of terms from the ontology of homology and related concepts.
     */
    public final static String HOM_NAME_COL_NAME = "HOM name";
    /**
     * A {@code String} that is the name of the column containing the reference ID 
     * in the similarity annotation file (for instance, "PMID:16771606").
     */
    public final static String REFERENCE_COL_NAME = "reference";
    /**
     * A {@code String} that is the name of the column containing the reference name 
     * in the similarity annotation file (for instance, 
     * "Liem KF, Bemis WE, Walker WF, Grande L, Functional Anatomy of the Vertebrates: 
     * An Evolutionary Perspective (2001) p.500").
     */
    public final static String REFERENCE_TITLE_COL_NAME = "reference title";
    /**
     * A {@code String} that is the name of the column containing the ECO IDs 
     * in the similarity annotation file (for instance, "ECO:0000067").
     */
    public final static String ECO_COL_NAME = "ECO ID";
    /**
     * A {@code String} that is the name of the column containing the ECO name 
     * in the similarity annotation file (for instance, "developmental similarity evidence").
     */
    public final static String ECO_NAME_COL_NAME = "ECO name";
    /**
     * A {@code String} that is the name of the column containing the confidence code IDs 
     * in the similarity annotation file (for instance, "CONF:0000003").
     */
    public final static String CONF_COL_NAME = "confidence code ID";
    /**
     * A {@code String} that is the name of the column containing the confidence code names 
     * in the similarity annotation file (for instance, "High confidence assertion").
     */
    public final static String CONF_NAME_COL_NAME = "confidence code name";
    /**
     * A {@code String} that is the name of the column containing the taxon IDs 
     * in the similarity annotation file (for instance, 9606).
     */
    public final static String TAXON_COL_NAME = "taxon ID";
    /**
     * A {@code String} that is the name of the column containing the taxon names 
     * in the similarity annotation file (for instance, "Homo sapiens").
     */
    public final static String TAXON_NAME_COL_NAME = "taxon name";
    /**
     * A {@code String} that is the name of the column containing a relevant quote from
     * the reference, in the similarity annotation file.
     */
    public final static String SUPPORT_TEXT_COL_NAME = "supporting text";
    /**
     * A {@code String} that is the name of the column containing the database which made 
     * the annotation, in the similarity annotation file (for instance, "Bgee").
     */
    public final static String ASSIGN_COL_NAME = "assigned by";
    /**
     * A {@code String} that is the name of the column containing the code representing  
     * the annotator which made the annotation, in the similarity annotation file 
     * (for instance "ANN").
     */
    public final static String CURATOR_COL_NAME = "curator";
    /**
     * A {@code String} that is the name of the column containing the date   
     * when the annotation was made, in the similarity annotation file 
     * (for instance "2013-07-03").
     */
    public final static String DATE_COL_NAME = "date";
    
    /**
     * Several actions can be launched from this main method, depending on the first 
     * element in {@code args}: 
     * <ul>
     * <li>If the first element in {@code args} is "extractTaxonIds", the action 
     * will be to extract the set of taxon IDs present in a similarity annotation file, 
     * and to write them in another file (with no headers), one ID per line. 
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the similarity annotation file to extract taxon IDs from.
     *   <li>path to the output file where write taxon IDs into, one per line.
     *   </ol>
     * </ul>
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If {@code args} does not contain the proper 
     *                                  parameters or does not allow to obtain 
     *                                  correct information.
     * @throws UnsupportedEncodingException If incorrect encoding was used to write 
     *                                      in output file.
     * @throws FileNotFoundException    If the annotation file provided could not be found.
     * @throws IOException              If the annotation file provided could not be read.
     */
    public static void main(String[] args) throws UnsupportedEncodingException,
        FileNotFoundException, IOException {
        
        log.entry((Object[]) args);
        
        if (args[0].equalsIgnoreCase("extractTaxonIds")) {
            if (args.length != 3) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "3 arguments, " + args.length + " provided."));
            }
            new SimilarityAnnotation().extractTaxonIdsToFile(args[1], args[2]);
        }
        
        log.exit();
    }

    /**
     * A {@code Set} of {@code String}s that are the IDs of the Uberon terms  
     * that were present in the similarity annotation file, but for which no taxon 
     * constraints could be found.
     * @see #checkAnnotation(Map)
     */
    private final Set<String> missingUberonIds;
    /**
     * A {@code Set} of {@code Integer}s that are the IDs of the taxa   
     * that were present in the similarity annotation file, but for which no taxon 
     * constraints could be found.
     * @see #checkAnnotation(Map)
     */
    private final Set<Integer> missingTaxonIds;
    /**
     * A {@code Set} of {@code String}s that are the IDs of the ECO terms that were 
     * present in the similarity annotation file, but could not be found in 
     * the ECO ontology. 
     * @see #checkAnnotation(Map)
     */
    private final Set<String> missingECOIds;
    /**
     * A {@code Set} of {@code String}s that are the IDs of the HOM terms that were 
     * present in the similarity annotation file, but could not be found in 
     * the HOM ontology (ontology of homology and related concepts). 
     * @see #checkAnnotation(Map)
     */
    private final Set<String> missingHOMIds;
    /**
     * A {@code Set} of {@code String}s that are the IDs of the CONF terms that were 
     * present in the similarity annotation file, but could not be found in 
     * the confidence code ontology. 
     * @see #checkAnnotation(Map)
     */
    private final Set<String> missingCONFIds;
    /**
     * A {@code Map} where keys are the IDs of the Uberon terms that were incorrectly 
     * scoped to some taxa, in which they are not supposed to exist, according to 
     * the taxon constraints. Associated value is a {@code Set} of {@code Integer}s 
     * that are the IDs of the taxa incorrectly used.
     * @see #checkAnnotation(Map)
     */
    private final Map<String, Set<Integer>> idsNotExistingInTaxa;
    
    /**
     * Default constuctor.
     */
    public SimilarityAnnotation() {
        this.missingUberonIds = new HashSet<String>();
        this.missingTaxonIds = new HashSet<Integer>();
        this.missingECOIds = new HashSet<String>();
        this.missingHOMIds = new HashSet<String>();
        this.missingCONFIds = new HashSet<String>();
        
        this.idsNotExistingInTaxa = new HashMap<String, Set<Integer>>();
    }
    
    
    public void generateReleaseFile(String annotFile, String taxonConstraintsFile, 
            String homOntFile, String ecoOntFile, String outputFile) {
        log.entry(annotFile, taxonConstraintsFile, homOntFile, ecoOntFile, outputFile);
        
        log.exit();
    }
    
    public void generateReleaseFile(String annotFile, String taxonConstraintsFile, 
            OWLOntology homOnt, OWLOntology ecoOnt, String outputFile) {
        log.entry(annotFile, taxonConstraintsFile, homOnt, ecoOnt, outputFile);
        
        
        
        log.exit();
    }
    
    public List<Map<String, Object>> generateReleaseData(String annotFile, 
            String taxonConstraintsFile, OWLOntology uberonOnt, OWLOntology homOnt, 
            OWLOntology ecoOnt) throws FileNotFoundException, IOException, 
            UnknownOWLOntologyException, OWLOntologyCreationException {
        log.entry(annotFile, taxonConstraintsFile, uberonOnt, homOnt, ecoOnt);
        
        TaxonConstraints extractor = new TaxonConstraints();
        Set<Integer> taxonIds = extractor.extractTaxonIds(taxonConstraintsFile);
        Map<String, Set<Integer>> taxonConstraints = 
                extractor.extractTaxonConstraints(taxonConstraintsFile);
        
        List<Map<String, Object>> annotations = this.extractAnnotations(annotFile);
        
        
        OWLGraphWrapper uberonOntWrapper = new OWLGraphWrapper(uberonOnt);
        OWLGraphWrapper ecoOntWrapper = new OWLGraphWrapper(ecoOnt);
        OWLGraphWrapper homOntWrapper = new OWLGraphWrapper(homOnt);
        
        return null;
    }
    
    /**
     * Extracts the similarity annotations from the provided file. It returns a 
     * {@code List} of {@code Map}s, where each {@code Map} represents a row in the file. 
     * The {@code Map}s in the {@code List} are ordered as they were read from the file. 
     * The expected key-value in the {@code Map}s are: 
     * <ul>
     * <li>values associated to the keys {@link #ENTITY_COL_NAME}, {@link #HOM_COL_NAME}, 
     * {@link #REFERENCE_COL_NAME}, {@link #CONF_COL_NAME}, {@link #ASSIGN_COL_NAME} 
     * cannot be {@code null} and are {@code String}s.
     * <li>values associated to the key {@link #TAXON_COL_NAME} cannot be {@code null} 
     * and are {@code Integer}s.
     * <li>values associated to the keys {@link #ENTITY_NAME_COL_NAME}, 
     * {@link #QUALIFIER_COL_NAME}, {@link #REFERENCE_TITLE_COL_NAME}, 
     * {@link #ECO_COL_NAME}, {@link #ECO_NAME_COL_NAME}, {@link #CONF_NAME_COL_NAME}, 
     * {@link #TAXON_NAME_COL_NAME}, {@link #SUPPORT_TEXT_COL_NAME}, 
     * {@link #CURATOR_COL_NAME}, {@link #HOM_NAME_COL_NAME} can be {@code null} 
     * and are {@code String}s. {@link #ECO_COL_NAME}, {@link #ECO_NAME_COL_NAME}, 
     * {@link #SUPPORT_TEXT_COL_NAME}, and {@link #CURATOR_COL_NAME} are {@code null} 
     * when the annotation has not yet been manually reviewed by a curator. 
     * {@link #ENTITY_NAME_COL_NAME}, {@link #REFERENCE_TITLE_COL_NAME}, 
     * {@link #CONF_NAME_COL_NAME}, {@link #TAXON_NAME_COL_NAME} and 
     * {@link #HOM_NAME_COL_NAME} can be {@code null} because the file exists 
     * in different flavors (simple generated file does not include the names; the 
     * annotation file does not provide {@link #REFERENCE_TITLE_COL_NAME}, because 
     * this information is mixed in the {@link #REFERENCE_COL_NAME} column). 
     * {@link #QUALIFIER_COL_NAME} is not {@code null} only when the annotation 
     * is negated. 
     * <li>values associated to the key {@link #DATE_COL_NAME} can be {@code null} 
     * and are {@code Date}s. This column is {@code null} when the annotation has not yet 
     * been manually reviewed by a curator.
     * </ul>
     * 
     * @param similarityFile    A {@code String} that is the path to a similarity 
     *                          annotation file. This file can be of any flavor 
     *                          (curator annotation file, genrated simple file, 
     *                          generated file with names).
     * @return                  A {@code List} of {@code Map}s where each {@code Map} 
     *                          represents a row in the file, the {@code Map}s being 
     *                          ordered in the order they were read from the file.
     * @throws FileNotFoundException    If {@code similarityFile} could not be found.
     * @throws IOException              If {@code similarityFile} could not be read.
     * @throws IllegalArgumentException If {@code similarityFile} could not be 
     *                                  properly parsed.
     */
    public List<Map<String, Object>> extractAnnotations(String similarityFile) 
            throws FileNotFoundException, IOException {
        log.entry(similarityFile);
        
        List<Map<String, Object>> annotations = new ArrayList<Map<String, Object>>();
        
        //we use a ListReader rather than a MapReader, because if we are parsing 
        //the annotation file from curators, it often has variable number of columns, 
        //and only the ListReader does support it. That's boring...
        try (ICsvListReader listReader = new CsvListReader(
                new FileReader(similarityFile), Utils.TSVCOMMENTED)) {
            
            String[] header = listReader.getHeader(true);
            
            while( (listReader.read()) != null ) {
                //get the proper CellProcessors
                CellProcessor[] processors = new CellProcessor[listReader.length()];
                for (int i = 0; i < listReader.length(); i++) {
                    if (header[i] != null) {
                        if (header[i].equalsIgnoreCase(ENTITY_COL_NAME) || 
                                header[i].equalsIgnoreCase(HOM_COL_NAME) || 
                                header[i].equalsIgnoreCase(REFERENCE_COL_NAME) || 
                                header[i].equalsIgnoreCase(CONF_COL_NAME) ||   
                                header[i].equalsIgnoreCase(ASSIGN_COL_NAME)) {
                            
                            processors[i] = new NotNull();
                            
                        } else if (header[i].equalsIgnoreCase(TAXON_COL_NAME)) {
                            
                            processors[i] = new NotNull(new ParseInt());
                            
                        } else if (header[i].equalsIgnoreCase(ENTITY_NAME_COL_NAME) || 
                                header[i].equalsIgnoreCase(QUALIFIER_COL_NAME) || 
                                header[i].equalsIgnoreCase(REFERENCE_TITLE_COL_NAME) || 
                                header[i].equalsIgnoreCase(ECO_COL_NAME) ||   
                                header[i].equalsIgnoreCase(ECO_NAME_COL_NAME) ||  
                                header[i].equalsIgnoreCase(CONF_NAME_COL_NAME) ||   
                                header[i].equalsIgnoreCase(TAXON_NAME_COL_NAME) ||
                                header[i].equalsIgnoreCase(SUPPORT_TEXT_COL_NAME) ||     
                                header[i].equalsIgnoreCase(CURATOR_COL_NAME) ||     
                                header[i].equalsIgnoreCase(HOM_NAME_COL_NAME)) {
                            
                            processors[i] = new Optional();
                            
                        } else if (header[i].equalsIgnoreCase(DATE_COL_NAME)) {
                            
                            processors[i] = new ParseDate("yyyy-MM-dd");
                            
                        } else {
                            throw log.throwing(new IllegalArgumentException(
                                    "The provided file contains an unknown column: " + 
                                    header[i]));
                        }
                    } else {
                        processors[i] = new Optional();
                    }
                }
                    
                List<Object> values = listReader.executeProcessors(processors);
                
                //now we transform the boring List into a more convenient Map, 
                //mapping column names to values.
                Map<String, Object> valuesMapped = new HashMap<String, Object>();
                int i = 0;
                for (Object value: values) {
                    if (header[i] != null) {
                        valuesMapped.put(header[i], value);
                    }
                    i++;
                }
                //fill potential missing columns
                for (int y = i; y < header.length; y++) {
                    if (header[y] != null) {
                        valuesMapped.put(header[y], null);
                    }
                }
                
                //check values again (maybe a column was missing and the parser did not 
                //get a chance to verify NotNull condition)
                if (StringUtils.isBlank((String) valuesMapped.get(ENTITY_COL_NAME)) || 
                        StringUtils.isBlank((String) valuesMapped.get(HOM_COL_NAME)) || 
                        StringUtils.isBlank((String) valuesMapped.get(REFERENCE_COL_NAME)) || 
                        StringUtils.isBlank((String) valuesMapped.get(CONF_COL_NAME)) || 
                        StringUtils.isBlank((String) valuesMapped.get(ASSIGN_COL_NAME))) {
                    throw log.throwing(new IllegalArgumentException(
                            "Some columns with null non-permitted are null: " + 
                            valuesMapped));
                }
                //the only values permitted for QUALIFIER_COL_NAME are null value, 
                //or a String equal to "NOT".
                if (valuesMapped.get(QUALIFIER_COL_NAME) != null && 
                   !((String) valuesMapped.get(QUALIFIER_COL_NAME)).equalsIgnoreCase("NOT")) {
                    throw log.throwing(new IllegalArgumentException(
                            "Incorrect value for column " + QUALIFIER_COL_NAME + 
                            ": " + valuesMapped.get(QUALIFIER_COL_NAME)));
                }
                
                annotations.add(valuesMapped);
            }
        }
        
        if (annotations.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The provided file " +
            		"does not contain any annotations."));
        }
        return log.exit(annotations);
    }
    
    javadoc
    private void checkAnnotation(Map<String, Object> annotation, Set<Integer> taxonIds, 
            Map<String, Set<Integer>> taxonConstraints, OWLGraphWrapper uberonOntWrapper, 
            OWLGraphWrapper ecoOntWrapper, OWLGraphWrapper homOntWrapper, 
            OWLGraphWrapper confOntWrapper) {
        log.entry(annotation, taxonIds, taxonConstraints, uberonOntWrapper, 
                ecoOntWrapper, homOntWrapper, confOntWrapper);
        
        //if there is a format error, it is different than from non-existing IDs, 
        //we will throw an exception right away.
        boolean formatError = false;
        
        int taxonId = (Integer) annotation.get(TAXON_COL_NAME);
        if (taxonId == 0) {
            formatError = true;
        }
        if (!taxonIds.contains(taxonId)) {
            this.missingTaxonIds.add(taxonId);
        }
        
        for (String uberonId: this.parseEntityColumn(
                (String) annotation.get(ENTITY_COL_NAME))) {
            if (StringUtils.isBlank(uberonId)) {
                formatError = true;
            }
            
            Set<Integer> existsIntaxa = taxonConstraints.get(uberonId);
            if (existsIntaxa == null) {
                this.missingUberonIds.add(uberonId);
            } else if (!existsIntaxa.contains(taxonId)) {
                if (this.idsNotExistingInTaxa.get(uberonId) == null) {
                    this.idsNotExistingInTaxa.put(uberonId, new HashSet<Integer>());
                }
                this.idsNotExistingInTaxa.get(uberonId).add(taxonId);
            }
        }
        
        String ecoId = (String) annotation.get(ECO_COL_NAME);
        if (ecoId != null && ecoOntWrapper.getOWLClassByIdentifier(ecoId) == null) {
            this.missingECOIds.add(ecoId);
        }
        
        String homId = (String) annotation.get(HOM_COL_NAME);
        if (homId != null && homOntWrapper.getOWLClassByIdentifier(homId) == null) {
            this.missingHOMIds.add(homId);
        }
        
        String confId = (String) annotation.get(CONF_COL_NAME);
        if (confId != null && confOntWrapper.getOWLClassByIdentifier(confId) == null) {
            this.missingCONFIds.add(confId);
        }
        
        if (StringUtils.isBlank(ecoId) || StringUtils.isBlank(homId) || 
                StringUtils.isBlank(confId)) {
            formatError = true;
        }
        
        if (formatError) {
            throw log.throwing(new IllegalArgumentException("Incorrect format " +
                    "for some values, annotation line is: " + annotation));
        }
        
        log.exit();
    }
    
    /**
     * Parses a value extracted from the column {@link #ENTITY_COL_NAME}. An entity 
     * can be represented by several Uberon IDs (like in the case of lung/swim bladder), 
     * separated by either by a pipe symbol ("|"), or a comma (",").
     * <p>
     * The {@code String}s in the {@code List} are ordered using their natural ordering, 
     * because this will allow easier diff on the generated file between releases.
     * 
     * @param entity    A {@code String} extracted from the entity column of 
     *                  the similarity annotation file.
     * @return          A {@code List} of {@code String}s that contains the individual 
     *                  Uberon ID(s), order by alphabetical order.
     */
    private List<String> parseEntityColumn(String entity) {
        log.entry(entity);
        
        String[] uberonIds = entity.split("\\|,");
        //perform the alphabetical ordering
        List<String> ids = new ArrayList<String>(Arrays.asList(uberonIds));
        Collections.sort(ids);
        
        return log.exit(ids);
    }
    
    /**
     * Extracts from the similarity annotation file {@code annotFile} the list 
     * of all taxon IDs used, and write them in {@code outputFile}, one ID per line. 
     * The first line of the annotation file should be a header line, defining 
     * a column to get IDs from, named exactly "taxon ID". The output file will 
     * have no headers. The IDs are supposed to be {@code Integer}s corresponding to 
     * the NCBI ID, for instance, "9606" for human.
     * 
     * @param annotFile     A {@code String} that is the path to the similarity 
     *                      annotation file.
     * @param outputFile    A {@code String} that is the path to the file where 
     *                      to write IDs into.
     * @throws UnsupportedEncodingException If incorrect encoding was used to write 
     *                                      in output file.
     * @throws FileNotFoundException        If {@code annotFile} could not be found.
     * @throws IOException                  If an error occurred while reading from 
     *                                      or writing into files.
     */
    public void extractTaxonIdsToFile(String annotFile, String outputFile) 
            throws UnsupportedEncodingException, FileNotFoundException, IOException {
        log.entry(annotFile, outputFile);
        
        Set<Integer> taxonIds = this.extractTaxonIds(annotFile);
        try(PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile), "utf-8")))) {
            for (int taxonId: taxonIds) {
                writer.println(taxonId);
            }
        }
        
        log.exit();
    }
    
    /**
     * Extract from the similarity annotation file {@code annotFile} the list 
     * of all taxon IDs used. The first line of the file should be a header line, 
     * defining a column to get IDs from, named exactly "taxon ID". The IDs returned 
     * are {@code Integer}s corresponding to the NCBI ID, for instance, "9606" for human.
     * 
     * @param annotFile A {@code String} that is the path to the similarity annotation file.
     * @return          A {@code Set} of {@code Integer}s that contains all IDs 
     *                  of the taxa used in the annotation file.
     * @throws IllegalArgumentException If {@code annotFile} did not allow to obtain 
     *                                  any valid taxon ID.
     * @throws FileNotFoundException    If {@code annotFile} could not be found.
     * @throws IOException              If {@code annotFile} could not be read.
     */
    public Set<Integer> extractTaxonIds(String annotFile) 
            throws IllegalArgumentException, FileNotFoundException, IOException {
        log.entry(annotFile);
        
        Set<Integer> taxonIds = new HashSet<Integer>(new Utils().parseColumnAsInteger(
                annotFile, TAXON_COL_NAME, new NotNull()));
        
        if (taxonIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The annotation file " +
                    annotFile + " did not contain any valid taxon ID"));
        }
        
        return log.exit(taxonIds);
    }
}
