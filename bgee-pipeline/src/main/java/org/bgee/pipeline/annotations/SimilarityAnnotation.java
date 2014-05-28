package org.bgee.pipeline.annotations;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.OntologyUtils;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.uberon.TaxonConstraints;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvMapWriter;

import owltools.graph.OWLGraphEdge;
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
     * in the similarity annotation file (to state the an entity is <strong>not</stong> 
     * homologous in a taxon).
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
    public final static String REF_COL_NAME = "reference";
    /**
     * A {@code String} that is the name of the column containing the reference name 
     * in the similarity annotation file (for instance, 
     * "Liem KF, Bemis WE, Walker WF, Grande L, Functional Anatomy of the Vertebrates: 
     * An Evolutionary Perspective (2001) p.500").
     */
    public final static String REF_TITLE_COL_NAME = "reference title";
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
     * A {@code String} that is the name of the column containing the type   
     * of the current line, in the similarity annotation file. 
     * Either it is a raw annotation line, or it is a generated annotation summarizing 
     * several related raw annotations regarding, using a confidence code 
     * for multiple evidences.
     * @see #RAW_LINE
     * @see #SUMMARY_LINE
     */
    public final static String LINE_TYPE_COL_NAME = "line type";
    /**
     * A {@code String} that is the value of the {@link #QUALIFIER_COL_NAME} column, 
     * when the annotation is negated.
     */
    public final static String NEGATE_QUALIFIER = "NOT";
    /**
     * A {@code String} that is the value of the column {@link #LINE_TYPE_COL_NAME} 
     * when the line stores a raw annotation from curators.
     */
    public final static String RAW_LINE = "RAW";
    /**
     * A {@code String} that is the value of the column {@link #LINE_TYPE_COL_NAME} 
     * when the line stores a generated annotation summarizing several related annotations.
     */
    public final static String SUMMARY_LINE = "SUMMARY";
    
    /**
     * A {@code Pattern} describing the possible values of the column {@link #REF_COL_NAME}. 
     * This is because in the curator annotation file, the title of the reference 
     * is mixed in the column containing the reference ID, so we need to parse it.
     */
    private final static Pattern REF_COL_PATTERN = Pattern.compile("(.+?)( .+?)?");
    /**
     * An {@code int} that is the index of the group containing the reference ID 
     * in the {@code Pattern} {@link #REF_COL_PATTERN}.
     */
    private final static int REF_ID_PATTERN_GROUP = 1;
    /**
     * An {@code int} that is the index of the group containing the reference title 
     * in the {@code Pattern} {@link #REF_COL_PATTERN}.
     */
    private final static int REF_TITLE_PATTERN_GROUP = 2;
    
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "high confidence level".
     */
    private final static String HIGH_CONF_ID = "CONF:0000029";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "medium confidence level".
     */
    private final static String MEDIUM_CONF_ID = "CONF:0000030";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "low confidence level".
     */
    private final static String LOW_CONF_ID = "CONF:0000031";
    
    /**
     * A {@code String} that is the value to fill the column {@link #ASSIGN_COL_NAME} 
     * when annotation was produced by the Bgee team.
     */
    public final static String BGEE_ASSIGNMENT = "Bgee";
    /**
     * A {@code String} that is the OBO-like ID if the evidence code to use for 
     * a non-reviewed annotation.
     */
    public final static String AUTOMATIC_ECO = "ECO:0000313";
    /**
     * A {@code String} that is the value to use in the column {@link #CURATOR_COL_NAME} 
     * for non-reviewed annotations.
     */
    public final static String AUTOMATIC_CURATOR = "vHOG";
    
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
     * <li>If the first element in {@code args} is "generateReleaseFile", the action 
     * will be to generate proper annotations from the raw annotation file, and 
     * to write them in a file, see {@link #generateReleaseFile(String, String, String, 
     * String, String, String, String, String)}.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>the path to the raw annotation file.
     *   <li>the path to the file containing taxon constraints. See {@link 
     *   org.bgee.pipeline.uberon.TaxonConstraints}
     *   <li>the path to the Uberon ontology.
     *   <li>the path to the taxonomy ontology.
     *   <li>the path to the homology and related concepts (HOM) ontology.
     *   <li>the path to the ECO ontology.
     *   <li>the path to the confidence information ontology.
     *   <li>the path to the output file.
     *   </ol>
     * <li>If the first element in {@code args} is "generateSummaryFileForTaxon", the action will be 
     * to extract, from a clean annotation file, the annotations related to a taxon (and 
     * all its ancestors), to summarize them (for instance, if a {@code SUMMARY} annotation 
     * exists for a given HOM ID/Entity ID/Taxon ID, only this summary will be used), 
     * and to write them to an output file. See {@link 
     * #writeToFileSummaryAnnotationsForTaxon(String, String, int, String)}.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>the path to the clean annotation file.
     *   <li>the path to the taxonomy ontology.
     *   <li>the NCBI ID of the taxon to consider (for instance, 9606).
     *   <li>the path to the output file.
     *   </ol>
     * <li>If the first element in {@code args} is "getAnatEntitiesWithNoTransformationOf", 
     * the action will be to write, into an output file, the anatomical entities 
     * used in our annotations of similarity, but that do not have any {@code transformation_of}
     * relations in the Uberon ontology. See {@link 
     * #getAnatEntitiesWithNoTransformationOf(String, OWLGraphWrapper)} for more details.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>The path to the similarity annotation file
     *   <li>The path to the Uberon ontology
     *   <li>The path to the output file.
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
     * @throws IOException
     * @throws UnknownOWLOntologyException
     * @throws OWLOntologyCreationException
     */
    public static void main(String[] args) throws UnsupportedEncodingException,
        FileNotFoundException, IOException, UnknownOWLOntologyException, 
        OWLOntologyCreationException, OBOFormatParserException {
        
        log.entry((Object[]) args);
        
        if (args[0].equalsIgnoreCase("extractTaxonIds")) {
            if (args.length != 3) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "3 arguments, " + args.length + " provided."));
            }
            new SimilarityAnnotation().extractTaxonIdsToFile(args[1], args[2]);
        } else if (args[0].equalsIgnoreCase("generateReleaseFile")) {
            if (args.length != 9) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "9 arguments, " + args.length + " provided."));
            }
            new SimilarityAnnotation().generateReleaseFile(args[1], args[2], args[3], 
                    args[4], args[5], args[6], args[7], args[8]);
        } else if (args[0].equalsIgnoreCase("generateSummaryFileForTaxon")) {
            if (args.length != 5) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "5 arguments, " + args.length + " provided."));
            }
            new SimilarityAnnotation().writeToFileSummaryAnnotationsForTaxon(
                    args[1], args[2], Integer.parseInt(args[3]), args[4]);
        } else if (args[0].equalsIgnoreCase("getAnatEntitiesWithNoTransformationOf")) {
            if (args.length != 4) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "4 arguments, " + args.length + " provided."));
            }
            new SimilarityAnnotation().writeAnatEntitiesWithNoTransformationOfToFile(
                    args[1], args[2], args[3]);
        } else {
            throw log.throwing(new UnsupportedOperationException("The following action " +
                    "is not recognized: " + args[0]));
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
     * A {@code Collection} of {@code Map}s, where each {@code Map} represents 
     * an annotation line that was incorrectly formatted.
     * @see #checkAnnotation(Map)
     */
    private final Set<Map<String, Object>> incorrectFormat;
    
    /**
     * A {@code OWLGraphWrapper} storing the Uberon ontology, for methods 
     * that need to share such a wrapper. 
     * @see {@link #getAnatEntitiesWithNoTransformationOf(String, OWLGraphWrapper)}
     */
    private OWLGraphWrapper uberonOntWrapper;
    
    /**
     * Default constuctor.
     */
    public SimilarityAnnotation() {
        this.missingUberonIds = new HashSet<String>();
        this.missingTaxonIds = new HashSet<Integer>();
        this.missingECOIds = new HashSet<String>();
        this.missingHOMIds = new HashSet<String>();
        this.missingCONFIds = new HashSet<String>();
        this.incorrectFormat = new HashSet<Map<String, Object>>();
        this.uberonOntWrapper = null;
        
        this.idsNotExistingInTaxa = new HashMap<String, Set<Integer>>();
    }
    
    
    /**
     * Extracts the similarity annotations from the provided file. It returns a 
     * {@code List} of {@code Map}s, where each {@code Map} represents a row in the file. 
     * The {@code Map}s in the {@code List} are ordered as they were read from the file. 
     * The expected key-value in the {@code Map}s are: 
     * <ul>
     * <li>values associated to the keys {@link #ENTITY_COL_NAME}, {@link #HOM_COL_NAME}, 
     * {@link #REF_COL_NAME}, {@link #CONF_COL_NAME}, {@link #ASSIGN_COL_NAME} 
     * cannot be {@code null} and are {@code String}s.
     * <li>values associated to the key {@link #TAXON_COL_NAME} cannot be {@code null} 
     * and are {@code Integer}s.
     * <li>values associated to the keys {@link #ENTITY_NAME_COL_NAME}, 
     * {@link #QUALIFIER_COL_NAME}, {@link #REF_TITLE_COL_NAME}, 
     * {@link #ECO_COL_NAME}, {@link #ECO_NAME_COL_NAME}, {@link #CONF_NAME_COL_NAME}, 
     * {@link #TAXON_NAME_COL_NAME}, {@link #SUPPORT_TEXT_COL_NAME}, 
     * {@link #CURATOR_COL_NAME}, {@link #HOM_NAME_COL_NAME} can be {@code null} 
     * and are {@code String}s. {@link #ECO_COL_NAME}, {@link #ECO_NAME_COL_NAME}, 
     * {@link #SUPPORT_TEXT_COL_NAME}, and {@link #CURATOR_COL_NAME} are {@code null} 
     * when the annotation has not yet been manually reviewed by a curator. 
     * {@link #ENTITY_NAME_COL_NAME}, {@link #REF_TITLE_COL_NAME}, 
     * {@link #CONF_NAME_COL_NAME}, {@link #TAXON_NAME_COL_NAME} and 
     * {@link #HOM_NAME_COL_NAME} can be {@code null} because the file exists 
     * in different flavors (simple generated file does not include the names; the 
     * annotation file does not provide {@link #REF_TITLE_COL_NAME}, because 
     * this information is mixed in the {@link #REF_COL_NAME} column). 
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
     * @param rawFile           A {@code boolean} defining whether the similarity file used 
     *                          is a raw file from annotators, or a clean generated file 
     *                          ({@code false}).
     * @return                  A {@code List} of {@code Map}s where each {@code Map} 
     *                          represents a row in the file, the {@code Map}s being 
     *                          ordered in the order they were read from the file.
     * @throws FileNotFoundException    If {@code similarityFile} could not be found.
     * @throws IOException              If {@code similarityFile} could not be read.
     * @throws IllegalArgumentException If {@code similarityFile} could not be 
     *                                  properly parsed.
     */
    public List<Map<String, Object>> extractAnnotations(String similarityFile, boolean rawFile) 
            throws FileNotFoundException, IOException {
        log.entry(similarityFile, rawFile);
        
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
                    if (i < header.length && header[i] != null) {
                        if (header[i].equalsIgnoreCase(ENTITY_COL_NAME) || 
                                header[i].equalsIgnoreCase(HOM_COL_NAME) || 
                                (header[i].equalsIgnoreCase(REF_COL_NAME) && rawFile) || 
                                header[i].equalsIgnoreCase(CONF_COL_NAME) ||   
                                header[i].equalsIgnoreCase(ASSIGN_COL_NAME)) {
                            
                            processors[i] = new NotNull();
                            
                        } else if (header[i].equalsIgnoreCase(TAXON_COL_NAME)) {
                            
                            processors[i] = new NotNull(new ParseInt());
                            
                        } else if (header[i].equalsIgnoreCase(ENTITY_NAME_COL_NAME) || 
                                header[i].equalsIgnoreCase(QUALIFIER_COL_NAME) || 
                                header[i].equalsIgnoreCase(REF_TITLE_COL_NAME) || 
                                (header[i].equalsIgnoreCase(REF_COL_NAME) && !rawFile) || 
                                header[i].equalsIgnoreCase(ECO_COL_NAME) ||   
                                header[i].equalsIgnoreCase(ECO_NAME_COL_NAME) ||  
                                header[i].equalsIgnoreCase(CONF_NAME_COL_NAME) ||   
                                header[i].equalsIgnoreCase(TAXON_NAME_COL_NAME) ||
                                header[i].equalsIgnoreCase(SUPPORT_TEXT_COL_NAME) ||     
                                header[i].equalsIgnoreCase(CURATOR_COL_NAME) ||     
                                header[i].equalsIgnoreCase(HOM_NAME_COL_NAME)) {
                            
                            processors[i] = new Optional();
                            
                        } else if (header[i].equalsIgnoreCase(DATE_COL_NAME)) {
                            
                            processors[i] = new Optional(new ParseDate("yyyy-MM-dd"));
                            
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
                    if (value != null && value instanceof String) {
                        value = ((String) value).trim();
                    }
                    if (i < header.length && header[i] != null) {
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
                        (rawFile && StringUtils.isBlank((String) valuesMapped.get(REF_COL_NAME))) || 
                        StringUtils.isBlank((String) valuesMapped.get(CONF_COL_NAME)) || 
                        StringUtils.isBlank((String) valuesMapped.get(ASSIGN_COL_NAME))) {
                    throw log.throwing(new IllegalArgumentException(
                            "Some columns with null non-permitted are null: " + 
                            valuesMapped));
                }
                //the only values permitted for QUALIFIER_COL_NAME are null value, 
                //or a String equal to NEGATE_QUALIFIER.
                if (valuesMapped.get(QUALIFIER_COL_NAME) != null && 
                   !((String) valuesMapped.get(QUALIFIER_COL_NAME)).equalsIgnoreCase(
                           NEGATE_QUALIFIER)) {
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


    /**
     * Generates the proper annotations to be released, from the raw annotations 
     * from curators, and write them into {@code outputFile}. This method will 
     * perform all necessary checks, will obtain names corresponding to the IDs used, 
     * will generate summary annotation lines using the "multiple evidences" 
     * confidence codes for related annotations, will order the generated annotations 
     * for easier diff between releases. And will write the annotations 
     * in {@code outputFile}.
     * 
     * @param annotFile             A {@code String} that is the path to the raw 
     *                              annotation file.
     * @param taxonConstraintsFile  A {@code String} that is the path to the file 
     *                              containing taxon constraints. 
     *                              See {@link org.bgee.pipeline.uberon.TaxonConstraints}
     * @param uberonOntFile         A {@code String} that is the path to the Uberon 
     *                              ontology.
     * @param taxOntFile            A {@code String} that is the path to the taxonomy 
     *                              ontology.
     * @param homOntFile            A {@code String} that is the path to the homology  
     *                              and related concepts (HOM) ontology.
     * @param ecoOntFile            A {@code String} that is the path to the ECO 
     *                              ontology.
     * @param confOntFile           A {@code String} that is the path to the confidence 
     *                              information ontology.
     * @param outputFile            A {@code String} that is the path to the output file.
     * @throws FileNotFoundException
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws UnknownOWLOntologyException
     * @throws OWLOntologyCreationException
     */
    public void generateReleaseFile(String annotFile, String taxonConstraintsFile, 
            String uberonOntFile, String taxOntFile, String homOntFile, 
            String ecoOntFile, String confOntFile, String outputFile) 
            throws FileNotFoundException, IOException, UnknownOWLOntologyException, 
            OWLOntologyCreationException, OBOFormatParserException {
        log.entry(annotFile, taxonConstraintsFile, uberonOntFile, taxOntFile, 
                homOntFile, ecoOntFile, confOntFile, outputFile);
        
        //get the annotations
        List<Map<String, Object>> annotations = this.extractAnnotations(annotFile, true);
        
        //now, get all the information required to perform correctness checks 
        //on the annotations, and to add additional information (names corresponding 
        //to uberon IDs, etc).
        TaxonConstraints extractor = new TaxonConstraints();
        Set<Integer> taxonIds = extractor.extractTaxonIds(taxonConstraintsFile);
        Map<String, Set<Integer>> taxonConstraints = 
                extractor.extractTaxonConstraints(taxonConstraintsFile);
        OWLGraphWrapper uberonOntWrapper = new OWLGraphWrapper(
                OntologyUtils.loadOntology(uberonOntFile));
        OWLGraphWrapper taxOntWrapper = new OWLGraphWrapper(
                OntologyUtils.loadOntology(taxOntFile));
        OWLGraphWrapper ecoOntWrapper = new OWLGraphWrapper(
                OntologyUtils.loadOntology(ecoOntFile));
        OWLGraphWrapper homOntWrapper = new OWLGraphWrapper(
                OntologyUtils.loadOntology(homOntFile));
        OWLGraphWrapper confOntWrapper = new OWLGraphWrapper(
                OntologyUtils.loadOntology(confOntFile));
        
        List<Map<String, Object>> properAnnots = this.generateReleaseData(annotations, 
                taxonConstraints, taxonIds, uberonOntWrapper, taxOntWrapper, 
                ecoOntWrapper, homOntWrapper, confOntWrapper);
        //write to file
        this.writeAnnotationsToFile(outputFile, properAnnots);
        
        log.exit();
    }
    
    /**
     * Write the annotations contained in {@code annotations} to the file {@code outputFile}, 
     * in a TSV file format.
     * 
     * @param outputFile    A {@code String} that is the path to the output file to be written.
     * @param annotations   A {@code List} of {@code Map}s, where each {@code Map} 
     *                      represents an annotation line.
     * @throws IOException  If an error occurs while trying to write in the file.
     */
    private void writeAnnotationsToFile(String outputFile, 
            List<Map<String, Object>> annotations) throws IOException {
        log.entry(outputFile, annotations);
        
        //write the file
        String[] header = new String[] {HOM_COL_NAME, HOM_NAME_COL_NAME, 
                ENTITY_COL_NAME, ENTITY_NAME_COL_NAME, QUALIFIER_COL_NAME, 
                TAXON_COL_NAME, TAXON_NAME_COL_NAME, LINE_TYPE_COL_NAME, 
                ECO_COL_NAME, ECO_NAME_COL_NAME, CONF_COL_NAME, CONF_NAME_COL_NAME, 
                REF_COL_NAME, REF_TITLE_COL_NAME, SUPPORT_TEXT_COL_NAME, 
                ASSIGN_COL_NAME, CURATOR_COL_NAME, DATE_COL_NAME};
        CellProcessor[] processors = new CellProcessor[] {new NotNull(), new NotNull(), 
                new NotNull(), new Optional(), new Optional(), 
                new NotNull(), new NotNull(), new NotNull(), 
                new Optional(), new Optional(), new NotNull(), new NotNull(), 
                new Optional(), new Optional(), new Optional(), 
                new NotNull(), new Optional(), new Optional(new FmtDate("yyyy-MM-dd"))};
        try (ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(outputFile),
                Utils.TSVCOMMENTED)) {
            
            mapWriter.writeHeader(header);
            for (Map<String, Object> annot: annotations) {
                mapWriter.write(annot, header, processors);
            }
        }
        
        log.exit();
    }
    
    /**
     * Generates the proper annotations to be released, from the raw annotations 
     * from curators. This method will perform all necessary checks, will obtain 
     * names corresponding to the IDs used, will generate summary annotation lines 
     * using the "multiple evidences" confidence codes for related annotations, 
     * will order the generated annotations for easier diff between releases.
     *  
     * @param rawAnnots         A {@code List} of {@code Map}s, where 
     *                          each {@code Map} represents an annotation line.
     * @param taxonConstraints  A {@code Map} where keys are IDs of Uberon terms, 
     *                          and values are {@code Set}s of {@code Integer}s 
     *                          containing the IDs of taxa in which the Uberon term 
     *                          exists.
     * @param taxonIds          A {@code Set} of {@code Integer}s that are the taxon IDs 
     *                          of all taxa that were used to define taxon constraints 
     *                          on Uberon terms.
     * @param uberonOntWrapper  An {@code OWLGraphWrapper} wrapping the Uberon ontology.
     * @param taxOntWrapper     An {@code OWLGraphWrapper} wrapping the taxonomy ontology.
     * @param ecoOntWrapper     An {@code OWLGraphWrapper} wrapping the ECO ontology.
     * @param homOntWrapper     An {@code OWLGraphWrapper} wrapping the HOM ontology 
     *                          (ontology of homology an related concepts).
     * @param confOntWrapper    An {@code OWLGraphWrapper} wrapping the confidence 
     *                          code ontology.
     * @return                  A {@code List} of {@code Map}s, where each {@code Map} 
     *                          represents a verified, completed, or generated 
     *                          annotation line.
     */
    public List<Map<String, Object>> generateReleaseData(
            List<Map<String, Object>> rawAnnots, Map<String, Set<Integer>> taxonConstraints, 
            Set<Integer> taxonIds, OWLGraphWrapper uberonOntWrapper, 
            OWLGraphWrapper taxOntWrapper, OWLGraphWrapper ecoOntWrapper, 
            OWLGraphWrapper homOntWrapper, OWLGraphWrapper confOntWrapper) {
        log.entry(rawAnnots, taxonConstraints, taxonIds, taxOntWrapper, uberonOntWrapper, 
                ecoOntWrapper, homOntWrapper, confOntWrapper);
        
        List<Map<String, Object>> releaseData = new ArrayList<Map<String, Object>>();
        //first pass, check each annotation, and add extra information to them 
        //(names corresponding to Uberon IDs, etc). We will generate new Maps, 
        //not to modify the raw annotations.
        for (Map<String, Object> rawAnnot: rawAnnots) {
            
            if (!this.checkAnnotation(rawAnnot, taxonConstraints, taxonIds, 
                    ecoOntWrapper, homOntWrapper, confOntWrapper)) {
                continue;
            }
            
            Map<String, Object> releaseAnnot = new HashMap<String, Object>();
            releaseAnnot.put(LINE_TYPE_COL_NAME, RAW_LINE);
            
            //Uberon ID(s) used to define the entity annotated. Get them ordered 
            //by alphabetical order, for easier diff between different release files.
            List<String> uberonIds = AnnotationCommon.parseMultipleEntitiesColumn(
                    (String) rawAnnot.get(ENTITY_COL_NAME));
            //get the corresponding names
            List<String> uberonNames = new ArrayList<String>();
            for (String uberonId: uberonIds) {
                //it is the responsibility of the checkAnnotation method to make sure 
                //the Uberon IDs exist, so we accept null values, it's not our job here.
                if (uberonOntWrapper.getOWLClassByIdentifier(uberonId) != null) {
                    String name = uberonOntWrapper.getLabel(
                            uberonOntWrapper.getOWLClassByIdentifier(uberonId));
                    if (name != null) {
                        uberonNames.add(name);
                    }
                }
            }
            //store Uberon IDs and names as column values
            releaseAnnot.put(ENTITY_COL_NAME, 
                    AnnotationCommon.getTermsToColumnValue(uberonIds));
            releaseAnnot.put(ENTITY_NAME_COL_NAME, 
                    AnnotationCommon.getTermsToColumnValue(uberonNames));
            
            //taxon
            if (rawAnnot.get(TAXON_COL_NAME) != null) {
                int taxonId = (int) rawAnnot.get(TAXON_COL_NAME);
                releaseAnnot.put(TAXON_COL_NAME, taxonId);
                
                String ontologyTaxId = OntologyUtils.getTaxOntologyId(taxonId);
                if (taxOntWrapper.getOWLClassByIdentifier(ontologyTaxId) != null) {
                    releaseAnnot.put(TAXON_NAME_COL_NAME, taxOntWrapper.getLabel(
                            taxOntWrapper.getOWLClassByIdentifier(ontologyTaxId)));
                }
            }
            
            //qualifier
            if (rawAnnot.get(QUALIFIER_COL_NAME) != null) {
                releaseAnnot.put(QUALIFIER_COL_NAME, NEGATE_QUALIFIER);
            }
            
            //HOM
            if (rawAnnot.get(HOM_COL_NAME) != null) {
                String homId = ((String) rawAnnot.get(HOM_COL_NAME)).trim();
                releaseAnnot.put(HOM_COL_NAME, homId);
                if (homOntWrapper.getOWLClassByIdentifier(homId) != null) {
                    releaseAnnot.put(HOM_NAME_COL_NAME, homOntWrapper.getLabel(
                            homOntWrapper.getOWLClassByIdentifier(homId)));
                }
            }
            
            //ECO
            if (rawAnnot.get(ECO_COL_NAME) != null) {
                String ecoId = ((String) rawAnnot.get(ECO_COL_NAME)).trim();
                releaseAnnot.put(ECO_COL_NAME, ecoId);
                if (ecoOntWrapper.getOWLClassByIdentifier(ecoId) != null) {
                    releaseAnnot.put(ECO_NAME_COL_NAME, ecoOntWrapper.getLabel(
                            ecoOntWrapper.getOWLClassByIdentifier(ecoId)));
                }
            } else {
                //otherwise it means that it is an unreviewed annotations
                releaseAnnot.put(ECO_COL_NAME, AUTOMATIC_ECO);
                releaseAnnot.put(ECO_NAME_COL_NAME, ecoOntWrapper.getLabel(
                        ecoOntWrapper.getOWLClassByIdentifier(AUTOMATIC_ECO)));
                releaseAnnot.put(CURATOR_COL_NAME, AUTOMATIC_CURATOR);
            }
            
            //CONF
            if (rawAnnot.get(CONF_COL_NAME) != null) {
                String confId = ((String) rawAnnot.get(CONF_COL_NAME)).trim();
                releaseAnnot.put(CONF_COL_NAME, confId);
                if (confOntWrapper.getOWLClassByIdentifier(confId) != null) {
                    releaseAnnot.put(CONF_NAME_COL_NAME, confOntWrapper.getLabel(
                            confOntWrapper.getOWLClassByIdentifier(confId)));
                }
            }
            
            //Reference
            if (rawAnnot.get(REF_COL_NAME) != null) {
                String refValue = ((String) rawAnnot.get(REF_COL_NAME)).trim();
                //the raw annotation file mixes the title of the reference 
                //in the same column as the reference ID, so we need to parse refValue
                String refId = this.getRefIdFromRefColValue(refValue);
                releaseAnnot.put(REF_COL_NAME, refId);
                
                String refTitle = this.getRefTitleFromRefColValue(refValue);
                if (refTitle != null) {
                    releaseAnnot.put(REF_TITLE_COL_NAME, refTitle);
                }
            }
            if (rawAnnot.get(REF_TITLE_COL_NAME) != null) {
                String refTitle = ((String) rawAnnot.get(REF_TITLE_COL_NAME)).trim();
                refTitle = refTitle.startsWith("\"") ? refTitle.substring(1) : refTitle;
                refTitle = refTitle.endsWith("\"") ? 
                        refTitle.substring(0, refTitle.length()-1) : refTitle;
                releaseAnnot.put(REF_TITLE_COL_NAME, refTitle);
            }
            
            //Supporting text
            if (rawAnnot.get(SUPPORT_TEXT_COL_NAME) != null) {
                releaseAnnot.put(SUPPORT_TEXT_COL_NAME, 
                        ((String) rawAnnot.get(SUPPORT_TEXT_COL_NAME)).trim());
            }
            
            //Curator
            if (rawAnnot.get(CURATOR_COL_NAME) != null) {
                releaseAnnot.put(CURATOR_COL_NAME, 
                        ((String) rawAnnot.get(CURATOR_COL_NAME)).trim());
            }
            
            //Assigned by
            if (rawAnnot.get(ASSIGN_COL_NAME) != null) {
                releaseAnnot.put(ASSIGN_COL_NAME, 
                        ((String) rawAnnot.get(ASSIGN_COL_NAME)).trim());
            }
            
            //Annotation date
            if (rawAnnot.get(DATE_COL_NAME) != null) {
                releaseAnnot.put(DATE_COL_NAME, rawAnnot.get(DATE_COL_NAME));
            }
            
            releaseData.add(releaseAnnot);
        }
        
        if (releaseData.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The provided annotations " +
            		"did not allow to generate any clean-transformed annotations."));
        }
        
        //now we verify that the data provided are correct (the method checkAnnotation 
        //used in this method will have filled attributes of this class storing errors 
        //that are not syntax errors).
        try {
            this.verifyErrors();
        } catch (IllegalStateException e) {
            //wrap the IllegalStateException into an IllegalArgumentException
            throw new IllegalArgumentException(e);
        }
        
        //now we add the generated lines that summarize several related annotations 
        //using a confidence code for multiple evidences assertion.
        this.addGeneratedAnnotations(releaseData, ecoOntWrapper, confOntWrapper);
        
        //now we order the annotations alphabetically, for easier diff between 
        //different versions of the annotation file.
        this.sortAnnotations(releaseData);
        
        return log.exit(releaseData);
    }
    
    /**
     * Order {@code annotations} by alphabetical order of some values in the {@code Map}, 
     * for easier diff between different releases of the annotation file.
     * 
     * @param annotations   A {@code List} of {@code Map}s to be ordered, where 
     *                      each {@code Map} represents an annotation line.
     */
    private void sortAnnotations(List<Map<String, Object>> annotations) {
        Collections.sort(annotations, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {

                String homId1 = (String) o1.get(HOM_COL_NAME);
                if (homId1 == null) {
                    homId1 = "";
                }
                String homId2 = (String) o2.get(HOM_COL_NAME);
                if (homId2 == null) {
                    homId2 = "";
                }
                int comp = homId1.compareTo(homId2);
                if (comp != 0) {
                    return comp;
                }
                
                String elementId1 = (String) o1.get(ENTITY_COL_NAME);
                String elementId2 = (String) o2.get(ENTITY_COL_NAME);
                comp = elementId1.compareTo(elementId2);
                if (comp != 0) {
                    return comp;
                }
                int taxonId1 = (int) o1.get(TAXON_COL_NAME);
                int taxonId2 = (int) o2.get(TAXON_COL_NAME);
                if (taxonId1 < taxonId2) {
                    return -1;
                } else if (taxonId1 > taxonId2) {
                    return 1;
                }
                
                String lineType1 = (String) o1.get(LINE_TYPE_COL_NAME);
                if (lineType1 == null) {
                    lineType1 = "";
                }
                String lineType2 = (String) o2.get(LINE_TYPE_COL_NAME);
                if (lineType2 == null) {
                    lineType2 = "";
                }
                comp = lineType1.compareTo(lineType2);
                if (comp != 0) {
                    return comp;
                }
                
                String qualifier1 = (String) o1.get(QUALIFIER_COL_NAME);
                if (qualifier1 == null) {
                    qualifier1 = "";
                }
                String qualifier2 = (String) o2.get(QUALIFIER_COL_NAME);
                if (qualifier2 == null) {
                    qualifier2 = "";
                }
                comp = qualifier1.compareTo(qualifier2);
                if (comp != 0) {
                    return comp;
                }
                
                String ecoId1 = (String) o1.get(ECO_COL_NAME);
                if (ecoId1 == null) {
                    ecoId1 = "";
                }
                String ecoId2 = (String) o2.get(ECO_COL_NAME);
                if (ecoId2 == null) {
                    ecoId2 = "";
                }
                comp = ecoId1.compareTo(ecoId2);
                if (comp != 0) {
                    return comp;
                }
                
                String confId1 = (String) o1.get(CONF_COL_NAME);
                if (confId1 == null) {
                    confId1 = "";
                }
                String confId2 = (String) o2.get(CONF_COL_NAME);
                if (confId2 == null) {
                    confId2 = "";
                }
                comp = confId1.compareTo(confId2);
                if (comp != 0) {
                    return comp;
                }
                
                String refId1 = (String) o1.get(REF_COL_NAME);
                if (refId1 == null) {
                    refId1 = "";
                }
                String refId2 = (String) o2.get(REF_COL_NAME);
                if (refId2 == null) {
                    refId2 = "";
                }
                comp = refId1.compareTo(refId2);
                if (comp != 0) {
                    return comp;
                }
                
                return 0;
            }
        });
    }
    
    /**
     * Adds generated summary for related annotations to {@code annotations}. If some 
     * annotations are related to a same entity, taxon, and HOM ID, then it is needed 
     * to generate an automatic annotation summarizing these related annotations, 
     * to compute a "multiple evidences" confidence code from the confidence information 
     * ontology. These new generated annotations will be added directly to {@code annotations}, 
     * which can then be modified as a result of a call to this method.
     * 
     * @param annotations       A {@code Collection} of {@code Map}s where each {@code Map} 
     *                          represents an annotation.
     * @param ecoOntWrapper     An {@code OWLGraphWrapper} wrapping the evidence code 
     *                          ontology.
     * @param confOntWrapper    An {@code OWLGraphWrapper} wrapping the confidence 
     *                          information ontology.
     */
    private void addGeneratedAnnotations(Collection<Map<String, Object>> annotations, 
            OWLGraphWrapper ecoOntWrapper, OWLGraphWrapper confOntWrapper) 
                    throws IllegalArgumentException {
        log.entry(annotations, ecoOntWrapper, confOntWrapper);
        
        OWLClass highQual = confOntWrapper.getOWLClassByIdentifier(HIGH_CONF_ID);
        OWLClass mediumQual = confOntWrapper.getOWLClassByIdentifier(MEDIUM_CONF_ID);
        OWLClass lowQual = confOntWrapper.getOWLClassByIdentifier(LOW_CONF_ID);
        
        //in order to identify related annotations, we will use a Map where keys 
        //are the concatenation of the entity column, the taxon column, the HOM column, and 
        //associated values are the related annotations
        Map<String, Set<Map<String, Object>>> relatedAnnotMapper = 
                new HashMap<String, Set<Map<String, Object>>>();
        
        //first pass, group related annotations
        for (Map<String, Object> annot: annotations) {
            String concat = annot.get(ENTITY_COL_NAME) + "-" + 
                annot.get(HOM_COL_NAME) + "-" + annot.get(TAXON_COL_NAME);
            
            if (relatedAnnotMapper.get(concat) == null) {
                relatedAnnotMapper.put(concat, new HashSet<Map<String, Object>>());
            }
            relatedAnnotMapper.get(concat).add(annot);
        }
        
        //now, generate summarizing annotations
        for (Set<Map<String, Object>> relatedAnnots: relatedAnnotMapper.values()) {
            if (relatedAnnots.size() == 1) {
                continue;
            }
            
            OWLClass previousECO  = null;
            boolean previousNegate = false;
            Map<String, Object> previousAnnot = null;
            
            boolean congruentEvidences = true;
            boolean severalExpTypes = false;
            OWLClass bestQual = lowQual;
            
            for (Map<String, Object> annot: relatedAnnots) {
                //check that it is indeed a raw annotation from curators
                if (annot.get(LINE_TYPE_COL_NAME) != null && 
                        annot.get(LINE_TYPE_COL_NAME).equals(SUMMARY_LINE)) {
                    throw log.throwing(new IllegalArgumentException("A summary line " +
                    		"was provided to generate summary annotations."));
                }
                
                OWLClass currentECO = ecoOntWrapper.getOWLClassByIdentifier(
                        (String) annot.get(ECO_COL_NAME));
                OWLClass currentConf = confOntWrapper.getOWLClassByIdentifier(
                        (String) annot.get(CONF_COL_NAME));
                boolean currentNegate = annot.get(QUALIFIER_COL_NAME) != null ? true:false;

                //defines the best quality 
                if (confOntWrapper.getOWLClassAncestors(currentConf).contains(highQual)) {
                    bestQual = highQual;
                } else if (!bestQual.equals(highQual) && 
                    confOntWrapper.getOWLClassAncestors(currentConf).contains(mediumQual)) {
                    bestQual = mediumQual;
                }
                
                if (previousAnnot != null) {
                    //defines whether previous ECO and current ECO are related 
                    if (!currentECO.equals(previousECO) && 
                        !ecoOntWrapper.getOWLClassAncestors(currentECO).contains(previousECO) && 
                        !ecoOntWrapper.getOWLClassAncestors(previousECO).contains(currentECO)) {
                        
                        severalExpTypes = true;
                    }
                    //defines whether evidences were congruent or conflicting
                    if (currentNegate != previousNegate) {
                        congruentEvidences = false;
                    }
                }
                previousECO = currentECO;
                previousNegate = currentNegate;
                previousAnnot = annot;
            }
            
            //infer the confidence information ID for the summary line.
            //this is hardcoded, there should be a way to obtain this information 
            //directly from the ontology
            String summaryConfId = null;
            if (severalExpTypes) {
                if (congruentEvidences) {
                    if (bestQual.equals(highQual)) {
                        summaryConfId = "CONF:0000012";
                    } else if (bestQual.equals(mediumQual)) {
                        summaryConfId = "CONF:0000013";
                    } else {
                        summaryConfId = "CONF:0000014";
                    }
                } else {
                    summaryConfId = "CONF:0000010";
                }
            } else {
                if (congruentEvidences) {
                    if (bestQual.equals(highQual)) {
                        summaryConfId = "CONF:0000017";
                    } else if (bestQual.equals(mediumQual)) {
                        summaryConfId = "CONF:0000019";
                    } else {
                        summaryConfId = "CONF:0000018";
                    }
                } else {
                    summaryConfId = "CONF:0000020";
                }
            }
            OWLClass summaryConf = confOntWrapper.getOWLClassByIdentifier(summaryConfId);
            if (summaryConf == null) {
                throw log.throwing(new AssertionError("Could not find the appropriate " +
                		"multiple evidences confidence code."));
            }
            
            Map<String, Object> newAnnot = new HashMap<String, Object>();
            Map<String, Object> referenceAnnot = relatedAnnots.iterator().next();
            //columns taken directly from the reference annotation
            newAnnot.put(ENTITY_COL_NAME, referenceAnnot.get(ENTITY_COL_NAME));
            newAnnot.put(ENTITY_NAME_COL_NAME, referenceAnnot.get(ENTITY_NAME_COL_NAME));
            newAnnot.put(HOM_COL_NAME, referenceAnnot.get(HOM_COL_NAME));
            newAnnot.put(HOM_NAME_COL_NAME, referenceAnnot.get(HOM_NAME_COL_NAME));
            newAnnot.put(TAXON_COL_NAME, referenceAnnot.get(TAXON_COL_NAME));
            newAnnot.put(TAXON_NAME_COL_NAME, referenceAnnot.get(TAXON_NAME_COL_NAME));
            //columns with values changed as compared to the reference annotation
            newAnnot.put(LINE_TYPE_COL_NAME, SUMMARY_LINE);
            if (congruentEvidences) {
                newAnnot.put(QUALIFIER_COL_NAME, referenceAnnot.get(QUALIFIER_COL_NAME));
            } else {
                newAnnot.put(QUALIFIER_COL_NAME, null);
            }
            newAnnot.put(CONF_COL_NAME, summaryConfId);
            newAnnot.put(CONF_NAME_COL_NAME, confOntWrapper.getLabel(summaryConf));
            newAnnot.put(ASSIGN_COL_NAME, BGEE_ASSIGNMENT);
            //columns that should not be set for a generated summary annotation
            newAnnot.put(REF_COL_NAME, null);
            newAnnot.put(REF_TITLE_COL_NAME, null);
            newAnnot.put(ECO_COL_NAME, null);
            newAnnot.put(ECO_NAME_COL_NAME, null);
            newAnnot.put(SUPPORT_TEXT_COL_NAME, null);
            newAnnot.put(CURATOR_COL_NAME, null);
            newAnnot.put(DATE_COL_NAME, null);
            
            annotations.add(newAnnot);
        }
        
        log.exit();
    }
    
    /**
     * Checks that no errors were detected and stored, in {@link #missingUberonIds}, 
     * and/or {@link #missingTaxonIds}, and/or {@link #missingECOIds}, and/or 
     * {@link #missingHOMIds}, and/or {@link #missingCONFIds}, and/or 
     * {@link #idsNotExistingInTaxa}. If some errors were stored, an 
     * {@code IllegalStateException} will be thrown with a detailed error message, 
     * otherwise, nothing happens.
     * @throws IllegalStateException    if some errors were detected and stored.
     */
    private void verifyErrors() throws IllegalStateException {
        log.entry();
        
        String errorMsg = "";
        if (!this.incorrectFormat.isEmpty()) {
            errorMsg += Utils.CR + "Problem detected, incorrectly formatted annotation lines: " + 
                Utils.CR;
            for (Map<String, Object> annot: this.incorrectFormat) {
                errorMsg += annot + Utils.CR;
            }
        }
        if (!this.missingUberonIds.isEmpty()) {
            errorMsg += Utils.CR + "Problem detected, unknown or deprecated Uberon IDs: " + 
                Utils.CR;
            for (String uberonId: this.missingUberonIds) {
                errorMsg += uberonId + Utils.CR;
            }
        }
        if (!this.missingTaxonIds.isEmpty()) {
            errorMsg += Utils.CR + "Problem detected, unknown or deprecated taxon IDs: " + 
                Utils.CR;
            for (int taxonId: this.missingTaxonIds) {
                errorMsg += taxonId + Utils.CR;
            }
        }
        if (!this.idsNotExistingInTaxa.isEmpty()) {
            errorMsg += Utils.CR + "Problem detected, Uberon IDs annotated with a taxon " +
                    "there are not supposed to exist in: " + Utils.CR;
            for (Entry<String, Set<Integer>> entry: this.idsNotExistingInTaxa.entrySet()) {
                for (int taxonId: entry.getValue()) {
                    errorMsg += entry.getKey() + " - " + taxonId + Utils.CR;
                }
            }
        }
        if (!this.missingECOIds.isEmpty()) {
            errorMsg += Utils.CR + "Problem detected, unknown or deprecated ECO IDs: " + 
                Utils.CR;
            for (String ecoId: this.missingECOIds) {
                errorMsg += ecoId + Utils.CR;
            }
        }
        if (!this.missingHOMIds.isEmpty()) {
            errorMsg += Utils.CR + "Problem detected, unknown or deprecated HOM IDs: " + 
                Utils.CR;
            for (String homId: this.missingHOMIds) {
                errorMsg += homId + Utils.CR;
            }
        }
        if (!this.missingCONFIds.isEmpty()) {
            errorMsg += Utils.CR + "Problem detected, unknown or deprecated CONF IDs: " + 
                Utils.CR;
            for (String confId: this.missingCONFIds) {
                errorMsg += confId + Utils.CR;
            }
        }
        if (!errorMsg.equals("")) {
            throw log.throwing(new IllegalStateException(errorMsg));
        }
        
        log.exit();
    }


    /**
     * A method to check the correctness of a line of annotation. If there is 
     * a format exception in the annotation (for instance, an empty Uberon ID, 
     * or a taxon ID that is not an {@code Integer}), an {@code IllegalArgumentException} 
     * will be thrown right away. If there is a problem of incorrect information 
     * provided (for instance, a non-existing Uberon ID), this incorrect information 
     * is stored to be displayed later (possibly after reading all lines of annotations). 
     * This is to avoid correcting only one information at a time, a re-running 
     * this class after each correction. This way, we will see all the errors 
     * at once. If the line of annotation was incorrect in any way, this method 
     * returns {@code false}.
     * <p>
     * The line of annotation is checked thanks to the other arguments provided to 
     * this method. The following checks are performed: 
     * <ul>
     * <li>If an Uberon ID cannot be found in {@code taxonConstraints}, it will be 
     * stored in {@link #missingUberonIds}.
     * <li>If an Uberon term is annotated with a taxon it is not supposed to exist in, 
     * the Uberon and taxon IDs will be stored in {@link #idsNotExistingTaxa}.
     * <li>If a taxon ID cannot be found in {@code taxonIds}, it will be stored in 
     * {@link #missingTaxonIds}.
     * <li>If an ECO ID cannot be found in the ontology wrapped in {@code ecoOntWrapper}, 
     * it will be stored in {@link #missingECOIds}.
     * <li>If a HOM ID cannot be found in the ontology wrapped in {@code homOntWrapper}, 
     * it will be stored in {@link #missingHOMIds}.
     * <li>If a CONF ID cannot be found in the ontology wrapped in {@code confOntWrapper}, 
     * it will be stored in {@link #missingCONFIds}.
     * </ul>
     * 
     * @param annotation        A {@code Map} that represents a line of annotation. 
     *                          See {@link #extractAnnotations(String)} for details 
     *                          about the key-value pairs in this {@code Map}.
     * @param taxonConstraints  A {@code Map} where keys are IDs of Uberon terms, 
     *                          and values are {@code Set}s of {@code Integer}s 
     *                          containing the IDs of taxa in which the Uberon term 
     *                          exists.
     * @param taxonIds          A {@code Set} of {@code Integer}s that are the taxon IDs 
     *                          of all taxa that were used to define taxon constraints 
     *                          on Uberon terms.
     * @param ecoOntWrapper     An {@code OWLGraphWrapper} wrapping the ECO ontology.
     * @param homOntWrapper     An {@code OWLGraphWrapper} wrapping the HOM ontology 
     *                          (ontology of homology an related concepts).
     * @param confOntWrapper    An {@code OWLGraphWrapper} wrapping the confidence 
     *                          code ontology.
     * @return                  {@code false} if the line of annotation contained 
     *                          any error.
     * @throws IllegalArgumentException If {@code annotation} contains some incorrectly 
     *                                  formatted information.
     */
    private boolean checkAnnotation(Map<String, Object> annotation, 
            Map<String, Set<Integer>> taxonConstraints, Set<Integer> taxonIds, 
            OWLGraphWrapper ecoOntWrapper, OWLGraphWrapper homOntWrapper, 
            OWLGraphWrapper confOntWrapper) throws IllegalArgumentException {
        log.entry(annotation, taxonConstraints, taxonIds, ecoOntWrapper, 
                homOntWrapper, confOntWrapper);
        
        boolean allGood = true;
        
        int taxonId = (Integer) annotation.get(TAXON_COL_NAME);
        if (taxonId == 0) {
            log.error("Missing taxon ID");
            this.incorrectFormat.add(annotation);
            allGood = false;
        }
        if (!taxonIds.contains(taxonId)) {
            log.error("Missing taxon ID: {}", taxonId);
            this.missingTaxonIds.add(taxonId);
            allGood = false;
        }
        
        for (String uberonId: AnnotationCommon.parseMultipleEntitiesColumn(
                (String) annotation.get(ENTITY_COL_NAME))) {
            if (StringUtils.isBlank(uberonId)) {
                log.error("Missing Uberon ID");
                this.incorrectFormat.add(annotation);
                allGood = false;
            }
            
            Set<Integer> existsIntaxa = taxonConstraints.get(uberonId);
            if (existsIntaxa == null) {
                log.error("Missing Uberon ID: {}", uberonId);
                this.missingUberonIds.add(uberonId);
                allGood = false;
            } else if (!existsIntaxa.contains(taxonId)) {
                log.error("Uberon ID {} does not exist in taxa {}", uberonId, taxonId);
                if (this.idsNotExistingInTaxa.get(uberonId) == null) {
                    this.idsNotExistingInTaxa.put(uberonId, new HashSet<Integer>());
                }
                this.idsNotExistingInTaxa.get(uberonId).add(taxonId);
                allGood = false;
            }
        }
        
        String qualifier = (String) annotation.get(QUALIFIER_COL_NAME);
        if (qualifier != null && !qualifier.trim().equalsIgnoreCase(NEGATE_QUALIFIER)) {
            log.error("Incorrect qualifier {}", qualifier);
            this.incorrectFormat.add(annotation);
            allGood = false;
        }
        
        String refId = this.getRefIdFromRefColValue((String) annotation.get(REF_COL_NAME));
        if (refId == null || !refId.matches("\\S+?:\\S+")) {
            log.error("Incorrect reference ID: {}", refId);
            this.incorrectFormat.add(annotation);
            allGood = false;
        }
        
        String ecoId = (String) annotation.get(ECO_COL_NAME);
        if (ecoId != null) {
            OWLClass cls = ecoOntWrapper.getOWLClassByIdentifier(ecoId.trim());
            if (cls == null || 
                    ecoOntWrapper.isObsolete(cls) || ecoOntWrapper.getIsObsolete(cls)) {
                log.error("Missing ECO ID: {}", ecoId);
                this.missingECOIds.add(ecoId);
                allGood = false;
            }
        }
        
        String homId = (String) annotation.get(HOM_COL_NAME);
        if (homId != null) {
            OWLClass cls = homOntWrapper.getOWLClassByIdentifier(homId.trim());
            if (cls == null || 
                    homOntWrapper.isObsolete(cls) || homOntWrapper.getIsObsolete(cls)) {
                log.trace("Missing HOM ID: {}", homId);
                this.missingHOMIds.add(homId);
                allGood = false;
            }
        }
        
        String confId = (String) annotation.get(CONF_COL_NAME);
        if (confId != null) {
            OWLClass cls = confOntWrapper.getOWLClassByIdentifier(confId.trim());
            if (cls == null || 
                    confOntWrapper.isObsolete(cls) || confOntWrapper.getIsObsolete(cls)) {
                log.trace("Missing CONF ID: {}", confId);
                this.missingCONFIds.add(confId);
                allGood = false;
            }
        }
        
        //ecoId is not mandatory
        if (StringUtils.isBlank(homId) || StringUtils.isBlank(confId)) {
            log.error("Missing HOM or confidence ID");
            this.incorrectFormat.add(annotation);
            allGood = false;
        }
        
        return log.exit(allGood);
    }
    
    /**
     * Gets a reference ID from a value in the column {@link #REF_COL_NAME}. This is 
     * because in the curator annotation file, reference titles can be mixed in 
     * the column containing reference IDs, so we need to extract them.
     * 
     * @param refColValue   A {@code String} that is a value retrieved from 
     *                      the column {@link #REF_COL_NAME}.
     * @return              A {@code String} corresponding to a reference ID 
     *                      extracted from {@code refColValue}.
     * @throws IllegalArgumentException     If {@code refColValue} has an incorrect 
     *                                      format.
     */
    private String getRefIdFromRefColValue(String refColValue) 
            throws IllegalArgumentException {
        log.entry(refColValue);
        if (refColValue == null) {
            return log.exit(null);
        }
        
        Matcher m = REF_COL_PATTERN.matcher(refColValue);
        if (m.matches()) {
            String refId = m.group(REF_ID_PATTERN_GROUP);
            if (refId != null) {
                refId = refId.trim();
            }
            if (StringUtils.isNotBlank(refId)) {
                return log.exit(refId);
            }
        }
        throw log.throwing(new IllegalArgumentException("Incorrect format for " +
        		"the reference column: " + refColValue));
    }
    /**
     * Gets a reference title from a value in the column {@link #REF_COL_NAME}. This is 
     * because in the curator annotation file, reference titles can be mixed in 
     * the column containing reference IDs, so we need to extract them.
     * 
     * @param refColValue   A {@code String} that is a value retrieved from 
     *                      the column {@link #REF_COL_NAME}.
     * @return              A {@code String} corresponding to a reference title 
     *                      extracted from {@code refColValue}.
     * @throws IllegalArgumentException     If {@code refColValue} has an incorrect 
     *                                      format.
     */
    private String getRefTitleFromRefColValue(String refColValue) 
            throws IllegalArgumentException {
        log.entry(refColValue);
        if (refColValue == null) {
            return log.exit(null);
        }
        
        Matcher m = REF_COL_PATTERN.matcher(refColValue);
        if (m.matches()) {
            String refTitle = m.group(REF_TITLE_PATTERN_GROUP);
            if (refTitle == null) {
                return log.exit(null);
            }
            refTitle = refTitle.trim();
            refTitle = refTitle.startsWith("\"") ? refTitle.substring(1) : refTitle;
            refTitle = refTitle.endsWith("\"") ? 
                    refTitle.substring(0, refTitle.length()-1) : refTitle;
            return log.exit(refTitle);
        }
        throw log.throwing(new IllegalArgumentException("Incorrect format for " +
                "the reference column: " + refColValue));
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
     * defining a column to get IDs from, named exactly as {@link #TAXON_COL_NAME}. 
     * The IDs returned are {@code Integer}s corresponding to the NCBI ID, 
     * for instance, "9606" for human.
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
    
    /**
     * Retrieve summarized annotations for a specific taxon from a <strong>clean</strong> 
     * similarity annotation file, and write them into an output file.
     * 
     * @param similarityFile    A {@code String} that is the path to the annotation file.
     * @param taxOntFile        An {@code String} that is the path to the taxonomy ontology, 
     *                          to retrieve ancestors of the taxon with ID {@code taxonId}.
     * @param taxonId           An {@code int} that is the NCBI ID of the taxon 
     *                          for which we want to retrieve annotations, including 
     *                          for its ancestral taxa.  
     * @param outputFile        A {@code String} that is the path to the output file 
     *                          to be written.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws OWLOntologyCreationException
     * @throws OBOFormatParserException
     * @see #extractSummaryAnnotationsForTaxon(String, String, int)
     */
    public void writeToFileSummaryAnnotationsForTaxon(String similarityFile, String taxOntFile, 
            int taxonId, String outputFile) throws FileNotFoundException, IOException, 
            OWLOntologyCreationException, OBOFormatParserException {
        log.entry(similarityFile, taxOntFile, taxonId, outputFile);
        
        List<Map<String, Object>> summarizedAnnotations = 
                this.extractSummaryAnnotationsForTaxon(similarityFile, taxOntFile, taxonId);
        this.writeAnnotationsToFile(outputFile, summarizedAnnotations);
        
        log.exit();
    }
    
    /**
     * Retrieve summarized annotations for a specific taxon from a <strong>clean</strong> 
     * similarity annotation file. Only <strong>positive</strong> annotations are retrieved 
     * (annotations with soleley a "NOT" qualifier with not be returned).
     * <p>
     * This method will retrieve all annotations that are applicable to the taxon 
     * with the NCBI ID {@code taxonId} (for instance, {@code 9606}), and to 
     * all its ancestral taxa. For a given entity ID and taxon ID, only one annotation 
     * will be retrieved: either the {@code SUMMARY} annotation if available, 
     * or the {@code RAW} annotation when only a single evidence is available 
     * for this assertion. 
     * <p>
     * The assertions are returned as a {@code List} of {@code Map}s, where 
     * each {@code Map} represents a summarized annotation. 
     * See {@link #extractAnnotations(String)} for details about the keys used 
     * in the {@code Map}s. 
     *  
     * @param similarityFile    A {@code String} that is the path to the annotation file.
     * @param taxOntFile        An {@code String} that is the path to the taxonomy ontology, 
     *                          to retrieve ancestors of the taxon with ID {@code taxonId}.
     * @param taxonId           An {@code int} that is the NCBI ID of the taxon 
     *                          for which we want to retrieve annotations, including 
     *                          for its ancestral taxa.  
     * @return                  A {@code List} of {@code Map}s, where each {@code Map} 
     *                          represents a summarized annotation.
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     */
    public List<Map<String, Object>> extractSummaryAnnotationsForTaxon(String similarityFile, String taxOntFile, 
            int taxonId) throws FileNotFoundException, IOException, 
            OWLOntologyCreationException, OBOFormatParserException {
        log.entry(similarityFile, taxOntFile, taxonId);
        
        //first, we retrieve from the taxonomy ontology the IDs of all the ancestor 
        //of the taxon with ID taxonId
        OWLOntology ont = OntologyUtils.loadOntology(taxOntFile);
        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
        OWLClass taxClass = wrapper.getOWLClassByIdentifier(OntologyUtils.getTaxOntologyId(taxonId));
        if (taxClass == null) {
            throw log.throwing(new IllegalArgumentException("The taxon with ID " + taxonId + 
                    " was not retrieved from the ontology file " + taxOntFile));
        }
        Set<Integer> allTaxIds = new HashSet<Integer>();
        for (OWLClass ancestor: wrapper.getOWLClassAncestors(taxClass)) {
            allTaxIds.add(OntologyUtils.getTaxNcbiId(wrapper.getIdentifier(ancestor)));
        }
        log.debug("Allowed tax IDs: {}", allTaxIds);
        
        List<Map<String, Object>> allAnnotations = this.extractAnnotations(similarityFile, false);
        //associate annotations to a key to be able to identify SUMMARY annotations
        Map<String, Map<String, Object>> summarizedAnnotations = new HashMap<String, Map<String, Object>>();
        //we iterate all annotations, plus a last iteration after the last annotation
        for (Map<String, Object> annotation: allAnnotations) {
            String key = annotation.get(ENTITY_COL_NAME) + " - " + 
                annotation.get(HOM_COL_NAME) + " - " + annotation.get(TAXON_COL_NAME);
            //check it is a requested taxon
            if (!allTaxIds.contains(annotation.get(TAXON_COL_NAME))) {
                continue;
            }
            
            //if an annotation for this HOM ID/Entity ID/Taxon ID was already seen, 
            //then we wait for the SUMMARY line. If it is the first time we see it, 
            //we use it directly.
            if (!summarizedAnnotations.containsKey(key) || 
                    (summarizedAnnotations.containsKey(key) && 
                    annotation.get(LINE_TYPE_COL_NAME).equals(SUMMARY_LINE))) {
                summarizedAnnotations.put(key, new HashMap<String, Object>(annotation));
            }
        }
        
        //now we filter to remove negative assertions.
        //we do it afterwards, to be sure all information was taken into account 
        //for the SUMMARY lines
        List<Map<String, Object>> filteredAnnotations = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> annotation: summarizedAnnotations.values()) {
            //check it is not a negative assertion
            if (annotation.get(QUALIFIER_COL_NAME) == null || 
                    !annotation.get(QUALIFIER_COL_NAME).equals(NEGATE_QUALIFIER)) {
                filteredAnnotations.add(annotation);
            }
        }
        
        return log.exit(filteredAnnotations);
    }
    
    /**
     * This methods is a helper method for 
     * {@link #getAnatEntityIdsWithNoTransformationOf(String, OWLGraphWrapper)}, 
     * allowing to provide the path to the Uberon ontology as {@code String}. 
     * @param annotFile     A {@code String} that is the path to 
     *                      the similarity annotation file.
     * @param uberonOntFile A {@code String} that is the path to the Uberon 
     *                      ontology file.
     * @return              See related method.
     * @see #getAnatEntityIdsWithNoTransformationOf(String, OWLGraphWrapper)
     * @throws IllegalArgumentException See related method.
     * @throws FileNotFoundException    See related method.
     * @throws IOException              See related method.
     * @throws UnknownOWLOntologyException  If an error occurred while loading Uberon.
     * @throws OWLOntologyCreationException If an error occurred while loading Uberon.
     * @throws OBOFormatParserException     If an error occurred while loading Uberon.
     */
    public Set<OWLClass> getAnatEntitiesWithNoTransformationOf(String annotFile, 
            String uberonOntFile) throws IllegalArgumentException, FileNotFoundException, 
            IOException, UnknownOWLOntologyException, OWLOntologyCreationException, 
            OBOFormatParserException {
        log.entry(annotFile, uberonOntFile);
        return log.exit(
                this.getAnatEntitiesWithNoTransformationOf(annotFile, 
                    new OWLGraphWrapper(OntologyUtils.loadOntology(uberonOntFile))));
    }
    
    /**
     * Obtains the anatomical entities used in the similarity annotation file 
     * that have no {@code transformation_of} relations in the Uberon ontology 
     * (nor any sub-relation of {@code transformation_of}). 
     * <p>
     * The {@code transformation_of} relations are important to be able to link 
     * a same structure with its different developmental states, as we annotate 
     * by default only the fully formed structures. The {@code transformation_of} 
     * relations allow to expand our annotations to developmental structures. 
     * 
     * @param annotFile         A {@code String} that is the path to 
     *                          the similarity annotation file.
     * @param uberonOntWrapper  An {@code OWLGraphWrapper} wrapping the Uberon ontology.
     * @return                  A {@code Set} of {@code OWLClass}es representing 
     *                          the anatomical entities with no {@code transformation_of} 
     *                          relations.
     * @throws IllegalArgumentException If {@code annotFile} did not allow to obtain 
     *                                  any valid anatomical entity ID, or {@code uberonOntWrapper} 
     *                                  does not allow to retrieve any 
     *                                  {@code transformation_of} relations.
     * @throws FileNotFoundException    If {@code annotFile} could not be found.
     * @throws IOException              If {@code annotFile} could not be read.
     */
    /*
     * (non-javadoc)
     * Note that this method will load the Uberon ontology into {@link #uberonOntWrapper}, 
     * so that it can be called by other methods following a call to this one. 
     */
    public Set<OWLClass> getAnatEntitiesWithNoTransformationOf(String annotFile, 
            OWLGraphWrapper uberonOntWrapper) throws IllegalArgumentException, 
            FileNotFoundException, IOException {
        log.entry(annotFile, uberonOntWrapper);
        
        //get the transformation_of Object Property, and its sub-object properties
        OWLObjectProperty transfOfRel = 
                uberonOntWrapper.getOWLObjectPropertyByIdentifier(OntologyUtils.TRANSFORMATION_OF_ID);
        Set<OWLObjectPropertyExpression> transfOfRels = new HashSet<OWLObjectPropertyExpression>();
        if (transfOfRel != null) {
            transfOfRels = uberonOntWrapper.getSubPropertyReflexiveClosureOf(transfOfRel);
        } 
        if (transfOfRel == null || transfOfRels.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The Uberon ontology provided " +
            		"did not contain any transformation_of relations."));
        }
        log.debug("Transformation_of relations identified: {}", transfOfRels);
        
        //now, identify all entities used in our annotations, with no transformation_of relation
        Set<String> anatEntityIds = 
                AnnotationCommon.extractAnatEntityIdsFromFile(annotFile, false);
        Set<OWLClass> withNoTransfOf = new HashSet<OWLClass>();
        anatEntities: for (String anatEntityId: anatEntityIds) {
            OWLClass anatEntity = uberonOntWrapper.getOWLClassByIdentifier(anatEntityId);
            log.trace("Testing OWLClass for ID {}: {}", anatEntityId, anatEntity);
            if (anatEntity == null) {
                log.trace("Entity {} not found in the ontology.", anatEntityId);
                continue anatEntities;
            }
            for (OWLGraphEdge edge: uberonOntWrapper.getOutgoingEdges(anatEntity)) {
                log.trace("  OutgoingEdge: {}", edge);
                if (transfOfRels.contains(edge.getSingleQuantifiedProperty().getProperty())) {
                    continue anatEntities;
                }
            }
            //if we reach that point, it means that the anat entity does not have 
            //any transformation_of relation
            log.trace("No transformation_of relation found for {}.", anatEntityId);
            withNoTransfOf.add(anatEntity);
        }

        //store the uberon ontology for use by methods calling this one
        this.uberonOntWrapper = uberonOntWrapper;
        
        return log.exit(withNoTransfOf);
    }
    
    /**
     * Call {@link #getAnatEntitiesWithNoTransformationOf(String, String)}, 
     * and write the results into {@code outputFile}, one per line. This method will also 
     * add additional information about other relations {@code developmentally related to} 
     * that identified {@code OWLClass}es could have (because, often, the missing 
     * {@code transformation_of} relation is replaced by another relation, 
     * such as {@code develops_from} for instance)
     * 
     * @param annotFile     A {@code String} that is the path to 
     *                      the similarity annotation file.
     * @param uberonOntFile A {@code String} that is the path to the Uberon 
     *                      ontology file.
     * @param outputFile    A {@code String} that is the path to the file where to write results.
     * @see #getAnatEntityIdsWithNoTransformationOf(String, String)
     * @throws IllegalArgumentException See related method.
     * @throws FileNotFoundException    See related method.
     * @throws IOException              See related method.
     * @throws UnknownOWLOntologyException  See related method.
     * @throws OWLOntologyCreationException See related method.
     * @throws OBOFormatParserException     See related method.
     */
    public void writeAnatEntitiesWithNoTransformationOfToFile(String annotFile, 
            String uberonOntFile, String outputFile) throws UnknownOWLOntologyException, 
            IllegalArgumentException, FileNotFoundException, OWLOntologyCreationException, 
            OBOFormatParserException, IOException {
        log.entry(annotFile, uberonOntFile, outputFile);
        
        //note that the method getAnatEntitiesWithNoTransformationOf will load the Uberon ontology 
        //into the attribute uberonOntWrapper, so that we can use it afterwards. 
        Set<OWLClass> entities = this.getAnatEntitiesWithNoTransformationOf(annotFile, uberonOntFile);
        //we want to retrieve the "developmentally related to" relations, and sub-relations, 
        //for the OWLClasses with no transformation_of relations (because, often, the missing 
        //"transformation_of" relation is replaced by another relation, 
        //such as "develops_from" for instance)
        //we load all relations related to "developmentally related to".
        OWLObjectProperty dvlptRel = 
                uberonOntWrapper.getOWLObjectProperty(OntologyUtils.DEVELOPMENTALLY_RELATED_TO_IRI);
        Set<OWLObjectPropertyExpression> dvlptRels = new HashSet<OWLObjectPropertyExpression>();
        if (dvlptRel != null) {
            dvlptRels = uberonOntWrapper.getSubPropertyReflexiveClosureOf(dvlptRel);
        } 
        if (dvlptRel == null || dvlptRels.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The Uberon ontology provided " +
                    "did not contain any \"developmentally related to\" relations."));
        }
        
        //iterate the OWLClasses, and write info to output file
        String dvlptRelsHeader = "'developmentally related to' relations";
        String[] header = new String[] {ENTITY_COL_NAME, ENTITY_NAME_COL_NAME, dvlptRelsHeader};
        CellProcessor[] processors = new CellProcessor[] {new NotNull(), new NotNull(), 
                new Optional()};
        try (ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(outputFile),
                Utils.TSVCOMMENTED)) {
            
            mapWriter.writeHeader(header);
            
            for (OWLClass entity: entities) {
                //get the relations "developmentally related to" outgoing from this class. 
                String dvlptRelInfo = "";
                for (OWLGraphEdge edge: this.uberonOntWrapper.getOutgoingEdges(entity)) {
                    log.trace("Testing OutgoingEdge for 'developmentally related to' relation: {}", edge);
                    if (dvlptRels.contains(edge.getSingleQuantifiedProperty().getProperty())) {
                        if (StringUtils.isNotEmpty(dvlptRelInfo)) {
                            dvlptRelInfo += " - ";
                        }
                        dvlptRelInfo += this.uberonOntWrapper.getLabel(
                                edge.getSingleQuantifiedProperty().getProperty()) + ": " + 
                                this.uberonOntWrapper.getIdentifier(edge.getTarget()) + " " + 
                                this.uberonOntWrapper.getLabel(edge.getTarget());
                    }
                }
                
                //write info to file
                Map<String, String> line = new HashMap<String, String>();
                line.put(ENTITY_COL_NAME, this.uberonOntWrapper.getIdentifier(entity));
                line.put(ENTITY_NAME_COL_NAME, this.uberonOntWrapper.getLabel(entity));
                line.put(dvlptRelsHeader, dvlptRelInfo);
                mapWriter.write(line, header, processors);
            }
        }
        
        log.exit();
    }
}
