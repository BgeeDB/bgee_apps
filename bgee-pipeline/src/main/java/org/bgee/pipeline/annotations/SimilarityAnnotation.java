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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
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
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.AncestralTaxaAnnotationBean;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.AnnotationBean;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.ParseMultipleValuesCell;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.ParseQualifierCell;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.RawAnnotationBean;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.SummaryAnnotationBean;
import org.bgee.pipeline.ontologycommon.CIOWrapper;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.bgee.pipeline.uberon.TaxonConstraints;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.io.ICsvMapWriter;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;

/**
 * Class related to the use, verification, and generation 
 * of the annotations of similarity between Uberon terms (similarity in the sense 
 * of the term in the HOM ontology HOM:0000000).
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Apr. 2015
 * @since Bgee 13
 */
//TODO: do not forget to generate annotations based on transformation_of relations.
public class SimilarityAnnotation {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = 
            LogManager.getLogger(SimilarityAnnotation.class.getName());
    
    /**
     * A bean representing a row from a curator annotation file. Getter and setter names 
     * must follow standard bean definitions. This bean simply extends 
     * {@code RawAnnotationBean} without adding any methods, it is only used to type 
     * annotations from curator annotation file for convenience. There are slight differences 
     * as compared to the released annotation files; notably, we do not care about 
     * the labels associated to IDs, as we will retrieve them directly from ontologies anyway; 
     * also, the title and the ID of references are mixed in the same column in curator file
     * (we only override {@link #setRefId(String)} and {@link #setRefTitle(String)} 
     * for this reason).
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    private static class CuratorAnnotationBean extends RawAnnotationBean {
        /**
         * 0-argument constructor of the bean.
         */
        public CuratorAnnotationBean() {
            super();
        }
        /**
         * Constructor providing all arguments of the class.
         * @param homId             See {@link #getHomId()}.
         * @param entityIds         See {@link #getEntityIds()}.
         * @param ncbiTaxonId       See {@link #getNcbiTaxonId()}.
         * @param negated           See {@link #isNegated()}.
         * @param ecoId             See {@link #getEcoId()}.
         * @param cioId             See {@link #getCioId()}.
         * @param refId             See {@link #getRefId()}.
         * @param refTitle          See {@link #getRefTitle()}.
         * @param supportingText    See {@link #getSupportingText()}.
         * @param assignedBy        See {@link #getAssignedBy()}.
         * @param curator           See {@link #getCurator()}.
         * @param curationDate      See {@link #getCurationDate()}.
         */
        public CuratorAnnotationBean(String homId, List<String> entityIds, 
                int ncbiTaxonId, boolean negated,String ecoId, String cioId, 
                String refId, String refTitle, String supportingText,
                String assignedBy, String curator, Date curationDate) {
            
            super(homId, null, entityIds, null, ncbiTaxonId, null, 
                    negated, ecoId, null, cioId, null, 
                    refId, refTitle, supportingText, assignedBy, curator, curationDate);
        }
        /**
         * Copy constructor.
         * @param toCopy    A {@code CuratorAnnotationBean} to clone into this 
         *                  {@code CuratorAnnotationBean}.
         */
        public CuratorAnnotationBean(CuratorAnnotationBean toCopy) {
            super(toCopy);
        }

        /**
         * @param refIdAndTitle A {@code String} representing the value of the refId column 
         *                      in curator annotation file, mixing ID and title of reference.
         */
        @Override
        public void setRefId(String refIdAndTitle) {
            log.entry(refIdAndTitle);
            
            super.setRefId(null);
            super.setRefTitle(null);
            
            if (StringUtils.isBlank(refIdAndTitle)) {
                log.exit(); return;
            }
            
            Matcher m = REF_COL_PATTERN.matcher(refIdAndTitle);
            if (m.matches()) {
                String refId = m.group(REF_ID_PATTERN_GROUP);
                String refTitle = m.group(REF_TITLE_PATTERN_GROUP);
                if (StringUtils.isNotBlank(refId)) {
                    super.setRefId(refId.trim());
                }
                if (StringUtils.isNotBlank(refTitle)) {
                    refTitle = refTitle.trim();
                    refTitle = refTitle.startsWith("\"") ? refTitle.substring(1) : refTitle;
                    refTitle = refTitle.endsWith("\"") ? 
                            refTitle.substring(0, refTitle.length()-1) : refTitle;
                    super.setRefTitle(refTitle);
                }
            }
            log.exit();
        }
        
        /**
         * This method does nothing, the reference title is set when calling 
         * {@link #setRefId(String)} in {@code CuratorAnnotationBean}.
         */
        @Override
        public void setRefTitle(String refTitle) {
            //nothing here, the title is set when calling setRefId
        }
    }

    /**
     * An {@code Enum} defining the file types that can be used/generated by 
     * the {@code SimilarityAnnotation} class.
     * <ul>
     * <li>{@code RAW}: file containing the raw annotations provided by curators.
     * <li>{@code RAW_CLEAN}: almost same as the file containing the raw annotations 
     * provided by curators, but with correct labels corresponding to ontology terms used 
     * added, lines ordered, and checks for correctness.
     * <li>{@code AGGREGATED_EVIDENCES}: same operations performed as for {@code RAW_CLEAN}, 
     * but annotations corresponding to different evidences for a same HOM ID, 
     * Uberon ID, taxon ID, are aggregated into a summary annotation taking all of them 
     * into account (notably to define a CIO term from the "multiple evidences" branch).
     * <li>{@code SINGLE_TAXON}: same operations performed as for {@code AGGREGATED_EVIDENCES}, 
     * but also, when there are annotations for a same HOM ID and Uberon ID, but different 
     * taxon IDs, only the most likely taxon is conserved, so that there is at most 
     * one line for a given HOM ID and Uberon ID.
     * </ul>
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum GeneratedFileType {
        RAW("RAW"), RAW_CLEAN("RAW"), AGGREGATED_EVIDENCES("SUMMARY"), SINGLE_TAXON("COMMON ANCESTOR");
        
        /**
         * A {@code String} corresponding to the value to be displayed in annotation files 
         * for this file type. 
         */
        private final String representation;
        private GeneratedFileType(String representation) {
            this.representation = representation;
        }
        /**
         * @return  A {@code String} corresponding to the value to be displayed 
         *          in annotation files for this file type. 
         */
        public String getRepresentation() {
            return this.representation;
        }
        @Override
        public String toString() {
            return this.getRepresentation();
        }
    }
    

    
    /**
     * A {@code Pattern} describing the possible values of the column storing reference IDs. 
     * This is because in the curator annotation file, the title of the reference 
     * is mixed in the column containing the reference ID, so we need to parse it.
     * @see #REF_ID_PATTERN_GROUP
     * @see #REF_TITLE_PATTERN_GROUP
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
     * A {@code String} that is the value to fill the column {@link #ASSIGN_COL_NAME} 
     * when annotation was produced by the Bgee team.
     */
    public final static String BGEE_ASSIGNMENT = "Bgee";
    /**
     * A {@code String} that is the OBO-like ID if the evidence code to use for 
     * a non-reviewed annotation from vHOG.
     */
    public final static String AUTOMATIC_IMPORT_ECO = "ECO:0000313";
    /**
     * A {@code String} that is the OBO-like ID if the evidence code to use for 
     * automatically inferred annotations.
     */
    public final static String AUTOMATIC_ASSERTION_ECO = "ECO:0000501";
    /**
     * A {@code String} that is the value to use in the column {@link #ASSIGN_COL_NAME} 
     * for non-reviewed annotations.
     */
    public final static String AUTOMATIC_ASSIGNED_BY = "bgee";
    
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
        
//        if (args[0].equalsIgnoreCase("extractTaxonIds")) {
//            if (args.length != 3) {
//                throw log.throwing(new IllegalArgumentException(
//                        "Incorrect number of arguments provided, expected " + 
//                        "3 arguments, " + args.length + " provided."));
//            }
//            new SimilarityAnnotation().extractTaxonIdsToFile(args[1], args[2]);
//        } else if (args[0].equalsIgnoreCase("generateReleaseFile")) {
//            if (args.length != 9) {
//                throw log.throwing(new IllegalArgumentException(
//                        "Incorrect number of arguments provided, expected " + 
//                        "9 arguments, " + args.length + " provided."));
//            }
//            new SimilarityAnnotation().generateReleaseFile(args[1], args[2], args[3], 
//                    args[4], args[5], args[6], args[7], args[8]);
//        } else if (args[0].equalsIgnoreCase("generateSummaryFileForTaxon")) {
//            if (args.length != 5) {
//                throw log.throwing(new IllegalArgumentException(
//                        "Incorrect number of arguments provided, expected " + 
//                        "5 arguments, " + args.length + " provided."));
//            }
//            new SimilarityAnnotation().writeToFileSummaryAnnotationsForTaxon(
//                    args[1], args[2], Integer.parseInt(args[3]), args[4]);
//        } else if (args[0].equalsIgnoreCase("getAnatEntitiesWithNoTransformationOf")) {
//            if (args.length != 4) {
//                throw log.throwing(new IllegalArgumentException(
//                        "Incorrect number of arguments provided, expected " + 
//                        "4 arguments, " + args.length + " provided."));
//            }
//            new SimilarityAnnotation().writeAnatEntitiesWithNoTransformationOfToFile(
//                    args[1], args[2], args[3]);
//        } else {
//            throw log.throwing(new UnsupportedOperationException("The following action " +
//                    "is not recognized: " + args[0]));
//        }
        
        log.exit();
    }
    
    /**
     * Extracts annotations from the provided curator annotation file containing information 
     * capable of populating {@code RawAnnotationBean}s. 
     * It returns a {@code List} of {@code RawAnnotationBean}s, where each 
     * {@code AnnotationBean} represents a row in the file. The elements 
     * in the {@code List} are ordered as they were read from the file. 
     * <p>
     * We do not use the method provided by {@link SimilarityAnnotationUtils}, 
     * because not the same fields are mandatory 
     * in the released files and in the file used by curator.
     * 
     * @param similarityFile    A {@code String} that is the path to an annotation file
     *                          from curators. 
     * @return                  A {@code List} of {@code RawAnnotationBean}s where each 
     *                          element represents a row in the file, ordered as 
     *                          they were read from the file.
     * @throws FileNotFoundException    If {@code similarityFile} could not be found.
     * @throws IOException              If {@code similarityFile} could not be read.
     * @throws IllegalArgumentException If {@code similarityFile} did not allow to retrieve 
     *                                  any annotation or could not be properly parsed.
     */
    //TODO: in java 8 we could use a functional interface to provide the method 
    //generating the mapping from header to CellProcessors (this is the only difference 
    //between the code for curator file or processed files).
    private static List<CuratorAnnotationBean> extractCuratorAnnotations(String similarityFile) 
            throws FileNotFoundException, IOException, IllegalArgumentException {
        log.entry(similarityFile);
        
        try (ICsvBeanReader annotReader = new CsvBeanReader(new FileReader(similarityFile), 
                SimilarityAnnotationUtils.TSV_COMMENTED)) {
            
            List<CuratorAnnotationBean> annots = new ArrayList<CuratorAnnotationBean>();
            final String[] header = annotReader.getHeader(true);
            String[] attributeMapping = SimilarityAnnotationUtils.mapHeaderToAttributes(
                    header, RawAnnotationBean.class);
            CellProcessor[] cellProcessorMapping = mapHeaderToCellProcessors(header);
            CuratorAnnotationBean annot;
            while((annot = annotReader.read(CuratorAnnotationBean.class, attributeMapping, 
                    cellProcessorMapping)) != null ) {
                annots.add(annot);
            }
            if (annots.isEmpty()) {
                throw log.throwing(new IllegalArgumentException("The provided file " 
                        + similarityFile + " did not allow to retrieve any annotation"));
            }
            return log.exit(annots);
            
        } catch (SuperCsvException e) {
            //hide implementation details
            throw log.throwing(new IllegalArgumentException("The provided file " 
                    + similarityFile + " could not be properly parsed", e));
        }
    }

    /**
     * Map the column names of a curator annotation file to the {@code CellProcessor}s 
     * used to populate {@code RawAnnotationBean}. We do not use the method provided 
     * by {@link SimilarityAnnotationUtils}, because not the same fields are mandatory 
     * in the released files and in the file used by curator.
     * 
     * @param header    An {@code Array} of {@code String}s representing the names 
     *                  of the columns of a curator similarity annotation file.
     * @return          An {@code Array} of {@code CellProcessor}s, put in 
     *                  the {@code Array} at the same index as the column they are supposed 
     *                  to process.
     * @throws IllegalArgumentException If a {@code String} in {@code header} 
     *                                  is not recognized.
     */
    private static CellProcessor[] mapHeaderToCellProcessors(String[] header) 
            throws IllegalArgumentException {
        log.entry((Object[]) header);
        
        CellProcessor[] processors = new CellProcessor[header.length];
        for (int i = 0; i < header.length; i++) {
            switch (header[i]) {
            // *** CellProcessors common to all AnnotationBean types ***
                case SimilarityAnnotationUtils.ENTITY_COL_NAME: 
                    processors[i] = new ParseMultipleValuesCell();
                    break;
                case SimilarityAnnotationUtils.TAXON_COL_NAME: 
                    processors[i] = new ParseInt();
                    break;
                case SimilarityAnnotationUtils.QUALIFIER_COL_NAME: 
                    processors[i] = new ParseQualifierCell();
                    break;
                case SimilarityAnnotationUtils.DATE_COL_NAME: 
                    processors[i] = new ParseDate(SimilarityAnnotationUtils.DATE_FORMAT);
                    break; 
                case SimilarityAnnotationUtils.HOM_COL_NAME: 
                case SimilarityAnnotationUtils.CONF_COL_NAME: 
                case SimilarityAnnotationUtils.REF_COL_NAME: 
                case SimilarityAnnotationUtils.ECO_COL_NAME: 
                case SimilarityAnnotationUtils.SUPPORT_TEXT_COL_NAME: 
                case SimilarityAnnotationUtils.ASSIGN_COL_NAME: 
                case SimilarityAnnotationUtils.CURATOR_COL_NAME: 
                    processors[i] = new StrNotNullOrEmpty();
                    break;
                //we don't care about any column providing names corresponding to IDs: 
                //we will retrieve the proper names from related ontologies directly
                case SimilarityAnnotationUtils.ENTITY_NAME_COL_NAME: 
                case SimilarityAnnotationUtils.HOM_NAME_COL_NAME: 
                case SimilarityAnnotationUtils.CONF_NAME_COL_NAME: 
                case SimilarityAnnotationUtils.TAXON_NAME_COL_NAME: 
                case SimilarityAnnotationUtils.ECO_NAME_COL_NAME: 
                //REF title is stored in the same column as REF ID in curator annotation file.
                //Only in generated files the title is stored in its own column
                case SimilarityAnnotationUtils.REF_TITLE_COL_NAME: 
                    processors[i] = new Optional();
                    break;
                default: 
                    throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                            + header[i]));
            }
        }
        return log.exit(processors);
    }

    /**
     * This methods is a helper method for 
     * {@link #getAnatEntitiesWithNoTransformationOf(String, OWLGraphWrapper)}, 
     * allowing to provide the path to the Uberon ontology as {@code String}. 
     * @param annotFile     A {@code String} that is the path to 
     *                      the similarity annotation file.
     * @param uberonOntFile A {@code String} that is the path to the Uberon 
     *                      ontology file.
     * @return              See related method.
     * @see #getAnatEntitiesWithNoTransformationOf(String, OWLGraphWrapper)
     * @throws IllegalArgumentException See related method.
     * @throws FileNotFoundException    See related method.
     * @throws IOException              See related method.
     * @throws UnknownOWLOntologyException  If an error occurred while loading Uberon.
     * @throws OWLOntologyCreationException If an error occurred while loading Uberon.
     * @throws OBOFormatParserException     If an error occurred while loading Uberon.
     */
    public static Set<OWLClass> getAnatEntitiesWithNoTransformationOf(String annotFile, 
            String uberonOntFile) throws IllegalArgumentException, FileNotFoundException, 
            IOException, UnknownOWLOntologyException, OWLOntologyCreationException, 
            OBOFormatParserException {
        log.entry(annotFile, uberonOntFile);
        return log.exit(getAnatEntitiesWithNoTransformationOf(annotFile, 
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
    public static Set<OWLClass> getAnatEntitiesWithNoTransformationOf(String annotFile, 
            OWLGraphWrapper uberonOntWrapper) throws IllegalArgumentException, 
            FileNotFoundException, IOException {
        log.entry(annotFile, uberonOntWrapper);
        
        OntologyUtils uberonUtils = new OntologyUtils(uberonOntWrapper);
        
        //get the transformation_of Object Property, and its sub-object properties
        Set<OWLObjectPropertyExpression> transfOfRels = uberonUtils.getTransformationOfProps();
        if (transfOfRels.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The Uberon ontology provided " +
            		"did not contain any transformation_of relations."));
        }
        
        //now, identify all entities used in our annotations, with no transformation_of relation
        Set<String> anatEntityIds = 
                AnnotationCommon.extractAnatEntityIdsFromFile(annotFile, false);
        Set<OWLClass> withNoTransfOf = new HashSet<OWLClass>();
        anatEntities: for (String anatEntityId: anatEntityIds) {
            OWLClass anatEntity = uberonOntWrapper.getOWLClassByIdentifier(anatEntityId, true);
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
        
        return log.exit(withNoTransfOf);
    }
    /**
     * Delegates to {@link #writeAnatEntitiesWithNoTransformationOfToFile(String, 
     * OWLGraphWrapper, String)} after having loaded the Uberon ontology.
     * 
     * @param annotFile     A {@code String} that is the path to 
     *                      the similarity annotation file.
     * @param uberonOntFile A {@code String} that is the path to the Uberon 
     *                      ontology file.
     * @param outputFile    A {@code String} that is the path to the file where to write results.
     * @throws UnknownOWLOntologyException
     * @throws IllegalArgumentException
     * @throws FileNotFoundException
     * @throws OWLOntologyCreationException
     * @throws OBOFormatParserException
     * @throws IOException
     */
    public static void writeAnatEntitiesWithNoTransformationOfToFile(String annotFile, 
            String uberonOntFile, String outputFile) throws UnknownOWLOntologyException, 
            IllegalArgumentException, FileNotFoundException, OWLOntologyCreationException, 
            OBOFormatParserException, IOException {
        log.entry(annotFile, uberonOntFile, outputFile);
        
        writeAnatEntitiesWithNoTransformationOfToFile(annotFile, 
                new OWLGraphWrapper(OntologyUtils.loadOntology(uberonOntFile)), 
                outputFile);
                
        log.exit();
    }
    /**
     * Call {@link #getAnatEntitiesWithNoTransformationOf(String, OWLGraphWrapper)}, 
     * and write the results into {@code outputFile}, one per line. This method will also 
     * add additional information about other relations {@code developmentally related to} 
     * that identified {@code OWLClass}es could have (because, often, the missing 
     * {@code transformation_of} relation is replaced by another relation, 
     * such as {@code develops_from} for instance)
     * 
     * @param annotFile         A {@code String} that is the path to 
     *                          the similarity annotation file.
     * @param uberonOntWrapper  An {@code OWLGraphWrapper} wrapping the Uberon ontology.
     * @param outputFile        A {@code String} that is the path to the file where 
     *                          to write results.
     * @throws IllegalArgumentException If the Uberon ontology provided did not contain 
     *                                  transformation_of relations.
     * @throws FileNotFoundException    If some files used could not be found.
     * @throws IOException              If some files used could not be used.
     * @see #getAnatEntitiesWithNoTransformationOf(String, OWLGraphWrapper)
     */
    public static void writeAnatEntitiesWithNoTransformationOfToFile(String annotFile, 
            OWLGraphWrapper uberonOntWrapper, String outputFile) throws 
            IllegalArgumentException, FileNotFoundException, IOException {
        log.entry(annotFile, uberonOntWrapper, outputFile);
        
        //note that the method getAnatEntitiesWithNoTransformationOf will load the Uberon ontology 
        //into the attribute uberonOntWrapper, so that we can use it afterwards. 
        Set<OWLClass> entities = getAnatEntitiesWithNoTransformationOf(annotFile, uberonOntWrapper);
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
        String[] header = new String[] {SimilarityAnnotationUtils.ENTITY_COL_NAME, 
                SimilarityAnnotationUtils.ENTITY_NAME_COL_NAME, dvlptRelsHeader};
        CellProcessor[] processors = new CellProcessor[] {new NotNull(), new NotNull(), 
                new Optional()};
        try (ICsvMapWriter mapWriter = new CsvMapWriter(new FileWriter(outputFile),
                Utils.TSVCOMMENTED)) {
            
            mapWriter.writeHeader(header);
            
            for (OWLClass entity: entities) {
                //get the relations "developmentally related to" outgoing from this class. 
                String dvlptRelInfo = "";
                for (OWLGraphEdge edge: uberonOntWrapper.getOutgoingEdges(entity)) {
                    log.trace("Testing OutgoingEdge for 'developmentally related to' relation: {}", edge);
                    if (dvlptRels.contains(edge.getSingleQuantifiedProperty().getProperty())) {
                        if (StringUtils.isNotEmpty(dvlptRelInfo)) {
                            dvlptRelInfo += " - ";
                        }
                        dvlptRelInfo += uberonOntWrapper.getLabel(
                                edge.getSingleQuantifiedProperty().getProperty()) + ": " + 
                                uberonOntWrapper.getIdentifier(edge.getTarget()) + " " + 
                                uberonOntWrapper.getLabel(edge.getTarget());
                    }
                }
                
                //write info to file
                Map<String, String> line = new HashMap<String, String>();
                line.put(SimilarityAnnotationUtils.ENTITY_COL_NAME, 
                        uberonOntWrapper.getIdentifier(entity));
                line.put(SimilarityAnnotationUtils.ENTITY_NAME_COL_NAME, 
                        uberonOntWrapper.getLabel(entity));
                line.put(dvlptRelsHeader, dvlptRelInfo);
                mapWriter.write(line, header, processors);
            }
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
     * A {@code Collection} of {@code AnnotationBean}s representing
     * annotation lines that were duplicated.
     * @see verifyErrors()
     */
    private final Set<AnnotationBean> duplicates;
    
    /**
     * A {@code Collection} of {@code AnnotationBean}s representing annotation lines 
     * that were incorrectly formatted.
     * @see verifyErrors()
     */
    private final Set<AnnotationBean> incorrectFormat;
    
    /**
     * A {@code Map} where keys are IDs of Uberon terms, and values are {@code Set}s 
     * of {@code Integer}s containing the IDs of taxa in which the Uberon term exists.
     */
    private final Map<String, Set<Integer>> taxonConstraints;
    /**
     * A {@code OWLGraphWrapper} storing the Uberon ontology.
     */
    private final OWLGraphWrapper uberonOntWrapper;
    /**
     * A {@code OWLGraphWrapper} storing the taxonomy ontology.
     */
    private final OWLGraphWrapper taxOntWrapper;
    /**
     * A {@code OWLGraphWrapper} storing the HOM ontology.
     */
    private final OWLGraphWrapper homOntWrapper;
    /**
     * A {@code OWLGraphWrapper} storing the ECO ontology.
     */
    private final OWLGraphWrapper ecoOntWrapper;
    /**
     * A {@code CIOWrapper} storing the CIO ontology.
     */
    private final CIOWrapper cioWrapper;
    
    /**
     * Constructor using pathes to the required files. 
     * 
     * @param taxonConstraintsFile          A {@code String} that is the path to the file 
     *                                      containing taxon constraints. 
     *                                      See {@link org.bgee.pipeline.uberon.TaxonConstraints}
     * @param idStartsToOverridenTaxonIds   A {@code Map} where keys are {@code String}s 
     *                                      representing prefixes of uberon terms to match, 
     *                                      the associated value being a {@code Set} 
     *                                      of {@code Integer}s to replace taxon constraints 
     *                                      of matching terms. Can be {@code null}.
     * @param uberonOntFile                 A {@code String} that is the path to the Uberon 
     *                                      ontology.
     * @param taxOntFile                    A {@code String} that is the path to the taxonomy 
     *                                      ontology.
     * @param homOntFile                    A {@code String} that is the path to the homology  
     *                                      and related concepts (HOM) ontology.
     * @param ecoOntFile                    A {@code String} that is the path to the ECO 
     *                                      ontology.
     * @param confOntFile                   A {@code String} that is the path to the confidence 
     *                                      information ontology.
     * @throws OBOFormatParserException     If an error occurs while loading the ontologies.
     * @throws FileNotFoundException        If a file could not be found.
     * @throws OWLOntologyCreationException If an error occurs while loading the ontologies.
     * @throws IOException                  If a file could not be read.
     */
    public SimilarityAnnotation(String taxonConstraintsFile, 
            Map<String, Set<Integer>> idStartsToOverridenTaxonIds, String uberonOntFile, 
            String taxOntFile, String homOntFile, String ecoOntFile, String confOntFile) 
                    throws OBOFormatParserException, FileNotFoundException, 
                    OWLOntologyCreationException, IOException {
        this(TaxonConstraints.extractTaxonConstraints(taxonConstraintsFile, 
                        idStartsToOverridenTaxonIds), 
                new OWLGraphWrapper(OntologyUtils.loadOntology(uberonOntFile)), 
                new OWLGraphWrapper(OntologyUtils.loadOntology(taxOntFile)), 
                new OWLGraphWrapper(OntologyUtils.loadOntology(homOntFile)), 
                new OWLGraphWrapper(OntologyUtils.loadOntology(ecoOntFile)), 
                new OWLGraphWrapper(OntologyUtils.loadOntology(confOntFile)));
    }
    /**
     * Constuctor receiving directly the objects needed.
     * 
     * @param taxonConstraints  A {@code Map} where keys are IDs of Uberon terms, 
     *                          and values are {@code Set}s of {@code Integer}s 
     *                          containing the IDs of taxa in which the Uberon term 
     *                          exists.
     * @param uberonOntWrapper  An {@code OWLGraphWrapper} wrapping the Uberon ontology.
     * @param taxOntWrapper     An {@code OWLGraphWrapper} wrapping the taxonomy ontology.
     * @param ecoOntWrapper     An {@code OWLGraphWrapper} wrapping the ECO ontology.
     * @param homOntWrapper     An {@code OWLGraphWrapper} wrapping the HOM ontology 
     *                          (ontology of homology an related concepts).
     * @param confOntWrapper    An {@code OWLGraphWrapper} wrapping the confidence 
     *                          code ontology.
     */
    public SimilarityAnnotation(Map<String, Set<Integer>> taxonConstraints, 
            OWLGraphWrapper uberonOntWrapper, OWLGraphWrapper taxOntWrapper, 
            OWLGraphWrapper ecoOntWrapper, OWLGraphWrapper homOntWrapper, 
            OWLGraphWrapper confOntWrapper) {
        this.missingUberonIds = new HashSet<String>();
        this.missingTaxonIds = new HashSet<Integer>();
        this.missingECOIds = new HashSet<String>();
        this.missingHOMIds = new HashSet<String>();
        this.missingCONFIds = new HashSet<String>();
        this.duplicates = new HashSet<AnnotationBean>();
        this.incorrectFormat = new HashSet<AnnotationBean>();
        
        this.taxonConstraints = taxonConstraints;
        this.uberonOntWrapper = uberonOntWrapper;
        this.taxOntWrapper = taxOntWrapper;
        this.ecoOntWrapper = ecoOntWrapper;
        this.homOntWrapper = homOntWrapper;
        this.cioWrapper = new CIOWrapper(confOntWrapper);
        
        this.idsNotExistingInTaxa = new HashMap<String, Set<Integer>>();
    }
    
    
    /**
     * Check the correctness of the annotations provided. This methods perform many checks, 
     * such as checking for presence of mandatory fields, checking for duplicated annotations, 
     * or potentially missing annotations. This methods will also log some warnings 
     * for issues that might be present on purpose, so that it does not raise an exception, 
     * but might be potential errors.
     * 
     * @param annots            A {@code List} of {@code T}s, where each {@code Map} 
     *                          represents a line of annotation. See {@link 
     *                          #extractAnnotations(String, GeneratedFileType)} for more details.
     * @param <T>               The type of {@code AnnotationBean} to check.
     * @throws IllegalArgumentException     If some errors were detected.
     */
    public <T extends AnnotationBean> void checkAnnotations(List<T> annots) 
                    throws IllegalArgumentException {
        log.entry(annots);
        
        //We will store taxa associated to positive and negative annotations, 
        //to verify NOT annotations in RAW and curator annotations (if there is a NOT annotation 
        //in a taxon for a structure, most likely there should be also a NOT annotation 
        //for all parent taxa annotated with positive annotations for the same structure). 
        //Also, if there are positive annotations using multiple Uberon IDs, most likely 
        //there should be positive annotations for the individual Uberon IDs as well.
        //We will use a RawAnnotationBean as key to store HOM ID - Uberon IDs.
        boolean checkPosNegAnnots = false;
        Map<RawAnnotationBean, Set<Integer>> positiveAnnotsToTaxa = 
                new HashMap<RawAnnotationBean, Set<Integer>>();
        Map<RawAnnotationBean, Set<Integer>> negativeAnnotsToTaxa = 
                new HashMap<RawAnnotationBean, Set<Integer>>();
        
        //Also, we will look for potentially duplicated annotations. We will search for 
        //exact duplicates, and potential duplicates (see below).
        Set<RawAnnotationBean> checkExactDuplicates = new HashSet<RawAnnotationBean>();
        Set<RawAnnotationBean> checkPotentialDuplicates = new HashSet<RawAnnotationBean>();
        //first pass, check each annotation
        int i = 0;
        Set<Integer> taxonIds = TaxonConstraints.extractTaxonIds(taxonConstraints);
        for (T annot: annots) {
            i++;
            if (!this.checkAnnotation(annot, taxonIds, i)) {
                continue;
            }
            if (annot instanceof RawAnnotationBean) {
                checkPosNegAnnots = true;
            }

            //need to order potential multiple values in ENTITY_COL_NAME to identify duplicates
            List<String> uberonIds = annot.getEntityIds();
            Collections.sort(uberonIds);
            
            //to check for duplicates, we will use only some columns
            RawAnnotationBean checkPotentialDuplicate = new RawAnnotationBean();
            checkPotentialDuplicate.setHomId(annot.getHomId());
            checkPotentialDuplicate.setEntityIds(uberonIds);
            checkPotentialDuplicate.setNegated(annot.isNegated());
            checkPotentialDuplicate.setNcbiTaxonId(annot.getNcbiTaxonId());
            if (annot instanceof RawAnnotationBean) {
                //use only the ID of the reference
                checkPotentialDuplicate.setRefId(((RawAnnotationBean)annot).getRefId());
                checkPotentialDuplicate.setEcoId(((RawAnnotationBean)annot).getEcoId());
            }
            //an exact duplicate will have same supporting text: we cannot rule out 
            //the possibility that 2 identical annotations come from a same reference with same 
            //ECOs, but in that case the supporting text should be different; 
            //but we cannot discard the fact that the supporting textes can be slightly 
            //different, but corresponding to a same quote, therefore we also need 
            //to check for "potential" duplicates. We do not consider CIO IDs here, 
            //as confidence is subjective and might be assigned different values 
            //by different curators. 
            RawAnnotationBean checkExactDuplicate = new RawAnnotationBean(checkPotentialDuplicate);
            checkExactDuplicate.setSupportingText(annot.getSupportingText());
            
            if (!checkExactDuplicates.add(checkExactDuplicate)) {
                //an exception will be thrown afterwards (see method verifyErrors)
                this.duplicates.add(checkExactDuplicate);
            } else if (!checkPotentialDuplicates.add(checkPotentialDuplicate)) {
                //we do not throw an exception for a potential duplicate, 
                //but we log a warn message, it's still most likely a duplicate
                log.warn("Some annotations seem duplicated (different supporting textes, "
                        + "but all other fields equal): " + checkPotentialDuplicate);
            }
            
            //we store positive and negative annotations associated to taxa here.
            RawAnnotationBean keyBean = new RawAnnotationBean();
            keyBean.setEntityIds(uberonIds);
            keyBean.setHomId(annot.getHomId());
            
            Map<RawAnnotationBean, Set<Integer>> posOrNegAnnotsToTaxa = positiveAnnotsToTaxa;
            if (annot.isNegated()) {
                posOrNegAnnotsToTaxa = negativeAnnotsToTaxa;
            }
            Set<Integer> taxIds = posOrNegAnnotsToTaxa.get(keyBean);
            if (taxIds == null) {
                taxIds = new HashSet<Integer>();
                posOrNegAnnotsToTaxa.put(keyBean, taxIds);
            }
            taxIds.add(annot.getNcbiTaxonId());
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
        
        //now we check coherence of positive and negative annotations 
        //related to relations between taxa, in RAW and curation files 
        //(if there is a NOT annotation in a taxon, most likely there should be also 
        //a NOT annotation for all parent taxa annotated; this is not mandatory, 
        //as a structure can disappear in a taxon, so we can only log a warning)
        if (checkPosNegAnnots) {
            
            for (Entry<RawAnnotationBean, Set<Integer>> missingNegativeAnnots: 
                this.checkNegativeAnnotsParentTaxa(positiveAnnotsToTaxa, negativeAnnotsToTaxa).
                entrySet()) {
                log.warn("Potentially missing annotation(s)! There exist negative annotation(s) "
                        + "for HOM ID - Uberon ID: "
                        + missingNegativeAnnots.getKey().getHomId() + " - " 
                        + missingNegativeAnnots.getKey().getEntityIds()
                        + " in taxon IDs: " 
                        + negativeAnnotsToTaxa.get(missingNegativeAnnots.getKey()) 
                        + " - There are also positive annotations in parent taxa for same "
                        + "HOM ID - Uberon ID, but some miss a corresponding negative annotation. "
                        + "Negative annotations potentially missing in taxa: " 
                        + missingNegativeAnnots.getValue());
            }
            
            //Also, if there are positive annotations using multiple Uberon IDs, most likely 
            //there should be positive annotations for the individual Uberon IDs as well.
            for (RawAnnotationBean posAnnot: positiveAnnotsToTaxa.keySet()) {
                if (posAnnot.getEntityIds() == null || posAnnot.getEntityIds().size() < 2) {
                    continue;
                }
                for (String uberonId: posAnnot.getEntityIds()) {
                    //check there is a positive annotation for individual Uberon IDs
                    RawAnnotationBean check = new RawAnnotationBean();
                    check.setHomId(posAnnot.getHomId());
                    check.setEntityIds(Arrays.asList(uberonId));
                    if (!positiveAnnotsToTaxa.containsKey(check)) {
                        log.warn("An annotation uses multiple entity IDs, but there is no annotation for the individual entity: {} - annotation: {}", 
                                uberonId, posAnnot);
                    }
                }
            }
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
     * The annotation is checked thanks to information provided at instantiation. 
     * The following checks are performed: 
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
     * <li>If a CIO ID cannot be found in the ontology wrapped in {@code confOntWrapper}, 
     * it will be stored in {@link #missingCONFIds}.
     * </ul>
     * 
     * @param annot             A {@code Map} that represents a line of annotation. 
     *                          See {@link #extractAnnotations(String, boolean)} for details 
     *                          about the key-value pairs in this {@code Map}.
     * @param taxonIds          A {@code Set} of {@code Integer}s that are the taxon IDs 
     *                          of all taxa that were used to define taxon constraints 
     *                          on Uberon terms. Provided to avoid extracting them 
     *                          for each annotation.
     * @param lineNumber        An {@code int} providing the line number where {@code annotation} 
     *                          was retrieved at. Should be equal to 0 if {@code annotation} 
     *                          was not retrieved from a file. Useful for logging purpose.
     * @return                  {@code false} if the line of annotation contained 
     *                          any error.
     * @throws IllegalArgumentException If {@code annotation} contains some incorrectly 
     *                                  formatted information.
     */
    private <T extends AnnotationBean> boolean checkAnnotation(T annot, 
            Set<Integer> taxonIds, int lineNumber) throws IllegalArgumentException {
        log.entry(annot, taxonIds, lineNumber);
        
        if (!annot.getClass().equals(RawAnnotationBean.class) && 
                !annot.getClass().equals(SummaryAnnotationBean.class) && 
                !annot.getClass().equals(AncestralTaxaAnnotationBean.class) && 
                !annot.getClass().equals(CuratorAnnotationBean.class)) {
            throw log.throwing(new IllegalArgumentException("Unrecognized "
                    + "AnnotationBean type: " + annot.getClass()));
        }
        
        boolean allGood = true;
        
        //*******************************************
        // Check presence of mandatory information
        //*******************************************
        
        //*** information mandatory for all annotation types ***
        if (annot.getNcbiTaxonId() <= 0) {
            log.error("Missing taxon ID at line {}", lineNumber);
            this.incorrectFormat.add(annot);
            allGood = false;
        }
        boolean missingUberon = false;
        if (annot.getEntityIds() == null || annot.getEntityIds().isEmpty()) {
            missingUberon = true;
        } else {
            for (String uberonId: annot.getEntityIds()) {
                if (StringUtils.isBlank(uberonId)) {
                    missingUberon = true;
                    break;
                }
            }
        }
        if (missingUberon) {
            log.error("Missing Uberon ID at line {}", lineNumber);
            this.incorrectFormat.add(annot);
            allGood = false;
        }
        if (StringUtils.isBlank(annot.getHomId())) {
            log.error("Missing HOM ID at line {}", lineNumber);
            this.incorrectFormat.add(annot);
            allGood = false;
        }
        if (StringUtils.isBlank(annot.getCioId())) {
            log.error("Missing CIO ID at line {}", lineNumber);
            this.incorrectFormat.add(annot);
            allGood = false;
        }
        
        //*** information optional in curator annotations, mandatory in all others ***
        if (!(annot instanceof CuratorAnnotationBean)) {
            if (StringUtils.isBlank(annot.getTaxonName())) {
                log.error("Missing taxon name at line {}", lineNumber);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
            boolean missingUberonName = false;
            if (annot.getEntityNames() == null || annot.getEntityNames().isEmpty()) {
                missingUberonName = true;
            } else {
                for (String uberonName: annot.getEntityNames()) {
                    if (StringUtils.isBlank(uberonName)) {
                        missingUberonName = true;
                        break;
                    }
                }
            }
            if (missingUberonName) {
                log.error("Missing Uberon name at line {}", lineNumber);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
            if (StringUtils.isBlank(annot.getHomLabel())) {
                log.error("Missing HOM name at line {}", lineNumber);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
            if (StringUtils.isBlank(annot.getCioLabel())) {
                log.error("Missing CIO name at line {}", lineNumber);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
        } 
        
        //*** information mandatory in both RAW and curator annotations ***
        if (annot instanceof RawAnnotationBean) {
    
            if (StringUtils.isBlank(((RawAnnotationBean) annot).getEcoId())) {
                log.error("Missing ECO ID at line {}", lineNumber);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
            if (StringUtils.isBlank(((RawAnnotationBean) annot).getSupportingText())) {
                log.error("Missing support text at line {}", lineNumber);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
            if (((RawAnnotationBean) annot).getCurationDate() == null) {
                log.error("Missing date at line {}", lineNumber);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
            if (StringUtils.isBlank(((RawAnnotationBean) annot).getAssignedBy())) {
                log.error("Missing assigned by info at line {}", lineNumber);
                this.incorrectFormat.add(annot);
                allGood = false;
            }

            //these fields are not mandatory in case of automatic annotations
            if (!AUTOMATIC_IMPORT_ECO.equals(((RawAnnotationBean) annot).getEcoId()) && 
                !AUTOMATIC_ASSERTION_ECO.equals(((RawAnnotationBean) annot).getEcoId())) {
                
                if (StringUtils.isBlank(((RawAnnotationBean) annot).getCurator())) {
                    log.error("Missing curator info at line {}", lineNumber);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                }
                String refId = ((RawAnnotationBean) annot).getRefId();
                if (StringUtils.isBlank(refId) || !refId.matches("\\S+?:\\S+")) {
                    log.error("Incorrect reference ID {} at line {}", refId, lineNumber);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                }
                String refTitle = ((RawAnnotationBean) annot).getRefTitle();
                if (StringUtils.isBlank(refTitle)) {
                    log.error("Missing reference title at line {}", lineNumber);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                }
            }
        }
        
        //*** information mandatory only in RAW annotations ***
        if (annot.getClass().equals(RawAnnotationBean.class)) {
            String ecoName = ((RawAnnotationBean) annot).getEcoLabel();
            if (StringUtils.isBlank(ecoName)) {
                log.error("Missing ECO name at line {}", lineNumber);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
            
            if (((RawAnnotationBean) annot).getCurationDate() == null) {
                log.error("Missing curation date at line {}", lineNumber);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
        }
        
        //if it is an annotation from curator, the date is not mandatory for 
        //unreviewed imported information, but we log a warning nevertheless.
        if (annot.getClass().equals(CuratorAnnotationBean.class) && 
                ((CuratorAnnotationBean) annot).getCurationDate() == null) {
            log.warn("Missing date in curator annotation at line {}, it's OK if it is an unreviewed annotation: {}", 
                    lineNumber, annot);
        }
        
        
        
        //*******************************************
        // Check validity of information provided
        //*******************************************
        //at this point, we do not check for presence of mandatory information, 
        //already done above.
        
        //check for existence of taxon and Uberon IDs
        int taxonId = annot.getNcbiTaxonId();
        if (taxonId > 0 && !taxonIds.contains(taxonId)) {
            log.error("Unrecognized taxon ID {} at line {}", taxonId, lineNumber);
            this.missingTaxonIds.add(taxonId);
            allGood = false;
        }
        for (String uberonId: annot.getEntityIds()) {
            if (StringUtils.isBlank(uberonId)) {
                continue;
            }
            OWLClass cls = uberonOntWrapper.getOWLClassByIdentifier(uberonId.trim(), true);
            if (cls == null || 
                    uberonOntWrapper.isObsolete(cls) || uberonOntWrapper.getIsObsolete(cls)) {
                log.trace("Unrecognized Uberon ID {} at line {}", uberonId, lineNumber);
                this.missingUberonIds.add(uberonId);
                allGood = false;
            }
            Set<Integer> existsIntaxa = taxonConstraints.get(uberonId);
            if (existsIntaxa == null) {
                log.error("Unrecognized Uberon ID {} at line {}", uberonId, lineNumber);
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
        
        String homId = annot.getHomId();
        if (StringUtils.isNotBlank(homId)) {
            OWLClass cls = homOntWrapper.getOWLClassByIdentifier(homId.trim(), true);
            if (cls == null || 
                    homOntWrapper.isObsolete(cls) || homOntWrapper.getIsObsolete(cls)) {
                log.trace("Unrecognized HOM ID {} at line {}", homId, lineNumber);
                this.missingHOMIds.add(homId);
                allGood = false;
            }
        }
        String confId = annot.getCioId();
        if (StringUtils.isNotBlank(confId)) {
            OWLGraphWrapper cioGraphWrapper = cioWrapper.getOWLGraphWrapper();
            OWLClass cls = cioGraphWrapper.getOWLClassByIdentifier(confId.trim(), true);
            if (cls == null || 
                    cioGraphWrapper.isObsolete(cls) || cioGraphWrapper.getIsObsolete(cls)) {
                log.trace("Unrecognized CONF ID {} at line {}", confId, lineNumber);
                this.missingCONFIds.add(confId);
                allGood = false;
            }
            if (annot instanceof SummaryAnnotationBean) {
                if (cioWrapper.isBgeeNotTrustedStatement(cls) && 
                        ((SummaryAnnotationBean) annot).isTrusted()) {
                    log.error("Inconsistent trust state for CIO statement {} at line {}", 
                            confId, lineNumber);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                }
            }
            if (annot instanceof AncestralTaxaAnnotationBean && 
                    cioWrapper.isBgeeNotTrustedStatement(cls)) {
                log.error("Only trusted annotations should be used for ancestral taxon file at line {}", 
                        lineNumber);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
        }
        if (annot instanceof RawAnnotationBean){
            String ecoId = ((RawAnnotationBean) annot).getEcoId();
            if (StringUtils.isNotBlank(ecoId)) {
                OWLClass cls = ecoOntWrapper.getOWLClassByIdentifier(ecoId.trim(), true);
                if (cls == null || 
                        ecoOntWrapper.isObsolete(cls) || ecoOntWrapper.getIsObsolete(cls)) {
                    log.error("Unrecognized ECO ID {} at line {}", ecoId, lineNumber);
                    this.missingECOIds.add(ecoId);
                    allGood = false;
                }
            }
            
            String refId = ((RawAnnotationBean) annot).getRefId();
            if (StringUtils.isBlank(refId) || !refId.matches("\\S+?:\\S+")) {
                log.error("Incorrect reference ID {} at line {}", refId, lineNumber);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
        }
        
        return log.exit(allGood);
    }


    /**
     * Identify potentially missing negative annotations: if there is a negative annotation 
     * in a taxon, most likely there should be also a negative annotation for all parent taxa 
     * annotated. This is not formally an error (maybe a structure can have been lost 
     * in a taxon, then reappeared independently later?). 
     * 
     * @param positiveAnnotsToTaxa  A {@code Map} where keys are {@code RawAnnotationBean}s
     *                              capturing HOM ID and Uberon IDs, 
     *                              the associated values being {@code Set}s of {@code Integer}s, 
     *                              representing the ID of the taxa which the annotation 
     *                              is valid in.
     * @param negativeAnnotsToTaxa  A {@code Map} where keys are {@code RawAnnotationBean}s
     *                              capturing HOM ID and Uberon IDs, 
     *                              the associated values being {@code Set}s of {@code Integer}s, 
     *                              representing the ID of the taxa which the annotation 
     *                              is negated in.
     * @return                      A {@code Map} where keys are {@code RawAnnotationBean}s
     *                              capturing HOM ID and Uberon IDs, 
     *                              the associated values being {@code Set}s of {@code Integer}s, 
     *                              representing the ID of taxa with potentially missing 
     *                              negative annotations.
     */
    private Map<RawAnnotationBean, Set<Integer>> checkNegativeAnnotsParentTaxa(
            Map<RawAnnotationBean, Set<Integer>> positiveAnnotsToTaxa, 
            Map<RawAnnotationBean, Set<Integer>> negativeAnnotsToTaxa) {
        log.entry(positiveAnnotsToTaxa, negativeAnnotsToTaxa);
    
        Map<RawAnnotationBean, Set<Integer>> missingNegativeAnnots = 
                new HashMap<RawAnnotationBean, Set<Integer>>();
        
        for (Entry<RawAnnotationBean, Set<Integer>> negativeAnnot: 
            negativeAnnotsToTaxa.entrySet()) {
            //the key should represent the concatenation of HOM ID and Uberon IDs, 
            //that were associated to a negative annotation
            RawAnnotationBean key = negativeAnnot.getKey();
            //if there are positive annotations for the same structure, in parent taxa 
            //of the taxa used, checked that there also exist corresponding NOT annotations. 
            //First, we retrieve taxa associated to corresponding positive annotations.
            if (positiveAnnotsToTaxa.get(key) == null) {
                continue;
            }
            //store in a new HashSet, as we will modify it
            Set<Integer> positiveTaxIds = new HashSet<Integer>(positiveAnnotsToTaxa.get(key));
            //identify the taxa used in corresponding positive annotations, that are parents 
            //of the taxa used in the negative annotation. 
            //First, we store all parents of the taxa associated to negative annotations.
            Set<Integer> negativeParentTaxa = new HashSet<Integer>();
            for (int negativeTaxonId: negativeAnnot.getValue()) {
                OWLClass taxCls = taxOntWrapper.getOWLClassByIdentifier(
                        OntologyUtils.getTaxOntologyId(negativeTaxonId), true);
                if (taxCls != null) {
                    for (OWLClass parentTaxon: taxOntWrapper.getAncestorsThroughIsA(taxCls)) {
                        negativeParentTaxa.add(OntologyUtils.getTaxNcbiId(
                                taxOntWrapper.getIdentifier(parentTaxon)));
                    }
                }
            }
            //now, retain taxa of positive annotations, parent of taxa used in neg. annotations.
            positiveTaxIds.retainAll(negativeParentTaxa);
            //and check whether there exist corresponding negative annotations
            positiveTaxIds.removeAll(negativeAnnot.getValue());
            if (!positiveTaxIds.isEmpty()) {
                missingNegativeAnnots.put(key, positiveTaxIds);
            }
        }
        
        return log.exit(missingNegativeAnnots);
    }

    /**
     * Checks that no errors were detected and stored, in {@link #missingUberonIds}, 
     * and/or {@link #missingTaxonIds}, and/or {@link #missingECOIds}, and/or 
     * {@link #missingHOMIds}, and/or {@link #missingCONFIds}, and/or 
     * {@link #idsNotExistingInTaxa}, and/or {@link #duplicates}. If some errors were stored, an 
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
            for (AnnotationBean annot: this.incorrectFormat) {
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
        if (!this.duplicates.isEmpty()) {
            errorMsg += Utils.CR + "Some annotations are duplicated: " + 
                Utils.CR;
            for (AnnotationBean duplicate: this.duplicates) {
                errorMsg += duplicate + Utils.CR;
            }
        }
        if (!errorMsg.equals("")) {
            throw log.throwing(new IllegalStateException(errorMsg));
        }
        
        log.exit();
    }
    
    public List<RawAnnotationBean> generateRawAnnotations(List<CuratorAnnotationBean> annots) {
        log.entry(annots);
        
        //check the curator annotations provided
        this.checkAnnotations(annots);
        
        //Generate RAW annotations
        List<RawAnnotationBean> rawAnnots = new ArrayList<RawAnnotationBean>();
        for (CuratorAnnotationBean curatorAnnot: annots) {
            rawAnnots.add(this.createRawAnnotWithExtraInfo(curatorAnnot));
        }
        if (rawAnnots.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The provided annotations " +
                    "did not allow to generate any clean-transformed annotations."));
        }
        //infer new annotations
        infer new annotations here
        

        //check and sort annotations generated 
        this.checkAnnotations(rawAnnots);
        this.sortAnnotations(rawAnnots);
    }

    /**
     * Create a new {@code RawAnnotationBean} with extra information from 
     * the curator annotation {@code annot}. This method will add labels associated to 
     * ontology term IDs, will order the Uberon IDs, etc. This information is retrieved 
     * from the ontologies provided at instantiation.
     * <p>
     * {@code annot} will not be modified as a result of the call to this method. 
     * 
     * @param annot             A {@code CuratorAnnotationBean} that represents 
     *                          an annotation from curators. 
     * @return                  A {@code RawAnnotationBean} representing the same annotation as 
     *                          {@code annot}, with added extra information.
     * @throws IllegalArgumentException If {@code annot} did not allow to obtain any 
     *                                  information about annotation.
     */
    private RawAnnotationBean createRawAnnotWithExtraInfo(CuratorAnnotationBean annot) 
            throws IllegalArgumentException {
        log.entry(annot);
        
        RawAnnotationBean rawAnnot = new RawAnnotationBean();
        
        //*** attributes simply copied
        rawAnnot.setNcbiTaxonId(annot.getNcbiTaxonId());
        rawAnnot.setNegated(annot.isNegated());
        
        //*** Date ***
        if (annot.getCurationDate() != null) {
            rawAnnot.setCurationDate(annot.getCurationDate());
        } else {
            //unreviewed annotation imported? Add current date.
            rawAnnot.setCurationDate(new Date());
        }
        
        //*** Trim text fields ***
        if (annot.getCioId() != null) {
            rawAnnot.setCioId(annot.getCioId().trim());
        }
        if (annot.getEcoId() != null) {
            rawAnnot.setEcoId(annot.getEcoId().trim());
        }
        if (annot.getHomId() != null) {
            rawAnnot.setHomId(annot.getHomId().trim());
        }
        if (annot.getRefId() != null) {
            rawAnnot.setRefId(annot.getRefId().trim());
        }
        if (annot.getRefTitle() != null) {
            rawAnnot.setRefTitle(annot.getRefTitle().trim());
        }
        if (annot.getSupportingText() != null) {
            rawAnnot.setSupportingText(annot.getSupportingText().trim());
        }
        if (annot.getCurator() != null) {
            rawAnnot.setCurator(annot.getCurator().trim());
        }
        if (annot.getAssignedBy() != null) {
            rawAnnot.setAssignedBy(annot.getAssignedBy());
        }
        
        //*** Add labels ***
        
        //Uberon ID(s) used to define the entity annotated. Get them ordered 
        //by alphabetical order, for easier diff between releases.
        List<String> uberonIds = annot.getEntityIds();
        Collections.sort(uberonIds);
        //get the corresponding names
        List<String> uberonNames = new ArrayList<String>();
        List<String> trimUberonIds = new ArrayList<String>();
        for (String uberonId: uberonIds) {
            //it is the responsibility of the checkAnnotation method to make sure 
            //the Uberon IDs exist, so we accept null values, it's not our job here.
            if (uberonId == null) {
                continue;
            }
            trimUberonIds.add(uberonId.trim());
            OWLClass cls = uberonOntWrapper.getOWLClassByIdentifier(uberonId.trim(), true);
            if (cls != null) {
                String name = uberonOntWrapper.getLabel(cls);
                if (name != null) {
                    uberonNames.add(name.trim());
                } else {
                    //it is important to have as many names as Uberon IDs
                    uberonNames.add("");
                }
            }
        }
        //store Uberon IDs and names
        rawAnnot.setEntityIds(trimUberonIds);
        rawAnnot.setEntityNames(uberonNames);
        
        //taxon
        if (annot.getNcbiTaxonId() != 0) {
            String ontologyTaxId = OntologyUtils.getTaxOntologyId(annot.getNcbiTaxonId());
            OWLClass cls = taxOntWrapper.getOWLClassByIdentifier(ontologyTaxId, true);
            if (cls != null) {
                rawAnnot.setTaxonName(taxOntWrapper.getLabel(cls));
            }
        }
        //HOM
        if (annot.getHomId() != null) {
            OWLClass cls = homOntWrapper.getOWLClassByIdentifier(annot.getHomId().trim(), true);
            if (cls != null) {
                rawAnnot.setHomLabel(homOntWrapper.getLabel(cls));
            }
        }
        //ECO
        if (annot.getEcoId() != null) {
            OWLClass cls = ecoOntWrapper.getOWLClassByIdentifier(annot.getEcoId().trim(), true);
            if (cls != null) {
                rawAnnot.setEcoLabel(ecoOntWrapper.getLabel(cls));
            }
        } else {
            //otherwise it means that it is an unreviewed annotations imported from vHOG
            rawAnnot.setEcoId(AUTOMATIC_IMPORT_ECO);
            rawAnnot.setEcoLabel(ecoOntWrapper.getLabel(
                    ecoOntWrapper.getOWLClassByIdentifier(AUTOMATIC_IMPORT_ECO, true)));
            rawAnnot.setCurator(AUTOMATIC_IMPORT_CURATOR);
            rawAnnot.setAssignedBy(AUTOMATIC_ASSIGNED_BY);
        }
        //CONF
        if (annot.getCioId() != null) {
            OWLClass cls = cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                    annot.getCioId().trim(), true);
            if (cls != null) {
                rawAnnot.setCioLabel(cioWrapper.getOWLGraphWrapper().getLabel(cls));
            }
        }
        
        
        //check whether we could get any information
        if (rawAnnot.getHomId() == null || rawAnnot.getNcbiTaxonId() == 0 || 
                rawAnnot.getEntityIds() == null || rawAnnot.getEntityIds().isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The provided annotation: " 
                    + annot + " - did not allow to generate a clean-transformed annotation."));
        }
        
        return log.exit(rawAnnot);
    }

    public void generateFiles(String rawAnnotFile, Set<GeneratedFileType> fileTypes, 
            String taxonConstraintsFile, Map<String, Set<Integer>> idStartsToOverridenTaxonIds, 
            String uberonOntFile, String taxOntFile, 
            String homOntFile, String ecoOntFile, String confOntFile, String outputDirectory) 
            throws FileNotFoundException, IOException, UnknownOWLOntologyException, 
            OWLOntologyCreationException, OBOFormatParserException {
        
        log.entry(rawAnnotFile, fileTypes, taxonConstraintsFile, idStartsToOverridenTaxonIds, 
                uberonOntFile, taxOntFile, homOntFile, ecoOntFile, confOntFile, 
                outputDirectory);
        
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
        
        this.generateFiles(this.extractAnnotations(rawAnnotFile, GeneratedFileType.RAW), 
                fileTypes, 
                TaxonConstraints.extractTaxonConstraints(taxonConstraintsFile, 
                        idStartsToOverridenTaxonIds), 
                TaxonConstraints.extractTaxonIds(taxonConstraintsFile), 
                uberonOntWrapper, taxOntWrapper, homOntWrapper, ecoOntWrapper, 
                confOntWrapper, outputDirectory);
        
        log.exit();
    }
    
    public void generateFiles(List<Map<String, Object>> rawAnnots, 
            Set<GeneratedFileType> fileTypes, 
            Map<String, Set<Integer>> taxonConstraints, Set<Integer> taxonIds, 
            OWLGraphWrapper uberonOntWrapper, OWLGraphWrapper taxOntWrapper, 
            OWLGraphWrapper homOntWrapper, OWLGraphWrapper ecoOntWrapper, 
            OWLGraphWrapper confOntWrapper, String outputDirectory)  {
        log.entry(rawAnnots, fileTypes, taxonConstraints, taxonIds, uberonOntWrapper, 
                taxOntWrapper, homOntWrapper, ecoOntWrapper, confOntWrapper, outputDirectory);
        
        
        log.exit();
    }
    
    /**
     * Generates the proper annotations to be used for the file of type {@code fileType}, 
     * from the raw annotations provided by curators. This method will check validity 
     * of provided annotations, will obtain the names corresponding to the IDs used, 
     * will generate summary annotation lines using the "multiple evidences" confidence codes 
     * for related annotations, will order the generated annotations for easier diff 
     * between releases, will identify the most likely taxon when multiple taxa 
     * are associated to a same structure.
     *  
     * @param rawAnnots         A {@code List} of {@code Map}s, where 
     *                          each {@code Map} represents a raw annotation line, 
     *                          as provided by curators.
     *                          See {@link #extractAnnotations(String, boolean)} for details 
     *                          about the key-value pairs in this {@code Map}.
     * @param fileType          A {@code GeneratedFileType} defining for which type of file 
     *                          these annotations are produced. 
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
    //XXX: ths method is not optimized: if you want to generate annotations for several 
    //GeneratedFileTypes, all computations are re-done from scratch each time. Also, 
    //the different types of annotations are built incrementaly, and all stored in memory, 
    //so, the memory usage could be largely reduced. However, we do not expect the number 
    //of annotations to be high enough to require any optimization.
    public List<Map<String, Object>> generateAnnotations(
            List<Map<String, Object>> rawAnnots, GeneratedFileType fileType, 
            Map<String, Set<Integer>> taxonConstraints, Set<Integer> taxonIds, 
            OWLGraphWrapper uberonOntWrapper, OWLGraphWrapper taxOntWrapper, 
            OWLGraphWrapper ecoOntWrapper, OWLGraphWrapper homOntWrapper, 
            OWLGraphWrapper confOntWrapper) {
        log.entry(rawAnnots, fileType, taxonConstraints, taxonIds, taxOntWrapper, 
                uberonOntWrapper, ecoOntWrapper, homOntWrapper, confOntWrapper);
        
        //check the raw annotations provided
        this.checkAnnotations(rawAnnots, GeneratedFileType.RAW, taxonConstraints, taxonIds, 
                taxOntWrapper, homOntWrapper, ecoOntWrapper, confOntWrapper);
        
        //Generate RAW CLEAN annotations
        List<Map<String, Object>> rawCleanAnnots = this.generateRawCleanAnnotations(rawAnnots, 
                uberonOntWrapper, taxOntWrapper, ecoOntWrapper, homOntWrapper, confOntWrapper);
        //check correctness 
        this.checkAnnotations(rawCleanAnnots, GeneratedFileType.RAW_CLEAN, taxonConstraints, 
                taxonIds, taxOntWrapper, homOntWrapper, ecoOntWrapper, confOntWrapper);
        //if only RAW CLEAN annotations were requested, we stop here
        if (fileType.equals(GeneratedFileType.RAW_CLEAN)) {
            return log.exit(rawCleanAnnots);
        }
        
        //Otherwise, now we need in any case to generate annotations that summarize 
        //several related annotations using a confidence code for multiple evidence lines.
        List<Map<String, Object>> aggregatedAnnots = this.generateAggregatedEvidencesAnnotations(
                rawCleanAnnots, ecoOntWrapper, confOntWrapper);
        //check correctness 
        this.checkAnnotations(aggregatedAnnots, GeneratedFileType.AGGREGATED_EVIDENCES, 
                taxonConstraints, taxonIds, taxOntWrapper, homOntWrapper, ecoOntWrapper, 
                confOntWrapper);
        //if only AGGREGATED_EVIDENCES annotations were requested, we stop here
        if (fileType.equals(GeneratedFileType.AGGREGATED_EVIDENCES)) {
            return log.exit(aggregatedAnnots);
        }
        
        //otherwise, now we need to generate SINGLE_TAXON annotations
        List<Map<String, Object>> singleTaxonAnnots = this.generateSingleTaxonAnnotations(
                aggregatedAnnots, taxOntWrapper, confOntWrapper, true);
        //check correctness 
        this.checkAnnotations(singleTaxonAnnots, GeneratedFileType.SINGLE_TAXON, 
                taxonConstraints, taxonIds, taxOntWrapper, homOntWrapper, ecoOntWrapper, 
                confOntWrapper);
        if (fileType.equals(GeneratedFileType.SINGLE_TAXON)) {
            return log.exit(singleTaxonAnnots);
        }
        
        
        //we should not reach that point
        throw log.throwing(new IllegalArgumentException("GeneratedFileType " + fileType 
                + " not supported."));
    }
    
    private List<Map<String, Object>> generateInferredAnnotations(
            List<Map<String, Object>> rawAnnots, OWLGraphWrapper uberonOntWrapper) 
                    throws IllegalArgumentException {
        log.entry(rawAnnots, uberonOntWrapper);
    }
    
    private Set<CuratorAnnotationBean> inferAnnotationsFromTransformationOf(
            Collection<CuratorAnnotationBean> annots) throws IllegalArgumentException {
        log.entry(annots);
        
        log.info("Inferring annotations based on transformation_of relations...");
        //first, we store HOM ID - Uberon IDs to be able to determine when an annotation 
        //already exists for some structures, independently of the taxon
        Set<CuratorAnnotationBean> existingAnnots = this.getExistingAnnots(annots);

        //get the transformation_of Object Property, and its sub-object properties
        OntologyUtils uberonUtils = new OntologyUtils(uberonOntWrapper);
        Set<OWLObjectPropertyExpression> transfOfRels = uberonUtils.getTransformationOfProps();
        if (transfOfRels.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The Uberon ontology provided " +
                    "did not contain any transformation_of relations."));
        }
        
        //propagate annotations using transformation_of relations.
        //we will associate inferred annotations to the original annotations they were
        //inferred from, to generate supporting texts and confidence levels 
        //(because a same inferred annotation can be based on different evidence lines 
        //with different confidence levels).
        //TODO: use Map.merge or streams library when we move to Java 8, 
        //see http://stackoverflow.com/a/25954862/1768736
        Map<CuratorAnnotationBean, Set<CuratorAnnotationBean>> inferredAnnots = 
                new HashMap<CuratorAnnotationBean, Set<CuratorAnnotationBean>>();
        for (CuratorAnnotationBean annot: annots) {
            //infer from transformation_of outgoing edges
            Set<CuratorAnnotationBean> newAnnots = 
                    this.createInferredAnnotationsFromTransformationOf(
                            annot, true, existingAnnots, transfOfRels);
            //infer from transformation_of incoming edges
            newAnnots.addAll(this.createInferredAnnotationsFromTransformationOf(
                    annot, false, existingAnnots, transfOfRels));
            //store association to original annot
            for (CuratorAnnotationBean newAnnot: newAnnots) {
                Set<CuratorAnnotationBean> sourceAnnots = inferredAnnots.get(newAnnot);
                if (sourceAnnots == null) {
                    sourceAnnots = new HashSet<CuratorAnnotationBean>();
                    inferredAnnots.put(newAnnot, sourceAnnots);
                }
                sourceAnnots.add(annot);
            }
        }
        
        //Determine confidence level and supporting text of new annotations
        for (Entry<CuratorAnnotationBean, Set<CuratorAnnotationBean>> inferredAnnot: 
            inferredAnnots.entrySet()) {
            
            //retrieve from source annotations CIO statements and information about Entity IDs.
            Set<OWLClass> cioStatements = new HashSet<OWLClass>();
            Set<String> supportingTextElements = new HashSet<String>();
            for (CuratorAnnotationBean sourceAnnot: inferredAnnot.getValue()) {
                
                cioStatements.add(cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                        sourceAnnot.getCioId(), true));
                
                List<String> entityIds = sourceAnnot.getEntityIds();
                Collections.sort(entityIds);
                supportingTextElements.add(
                        SimilarityAnnotationUtils.multipleValuesToString(entityIds));
            }
            
            //retrieve best CIO statement, to set the confidence in inferred annotation.
            inferredAnnot.getKey().setCioId(cioWrapper.getOWLGraphWrapper().getIdentifier(
                    cioWrapper.getBestTermWithConfidenceLevel(cioStatements)));
            
            //generate supporting text providing information about the source annotations.
            String supportingText = "Annotation inferred from transformation_of relations "
                    + "using annotations to same HOM ID, "
                    + "same NCBI taxon ID, and Entity IDs equal to: ";
            boolean firstIteration = true;
            for (String supportingTextElement: supportingTextElements) {
                if (firstIteration) {
                    supportingText += " - ";
                }
                supportingText += supportingTextElement;
                firstIteration = false;
            }
            inferredAnnot.getKey().setSupportingText(supportingText);
        }
        
        log.info("Done inferring annotations based on transformation_of relations, {} annotations inferred.", 
                inferredAnnots.size());
        return log.exit(inferredAnnots.keySet());
    }
    
    /**
     * Create new {@code CuratorAnnotationBean}s using transformation_of relations 
     * outgoing from or incoming to the entities used in {@code annot} (see method 
     * {@code getEntityIds}). New annotations will be created only if there is 
     * no corresponding annotation in {@code existingAnnots}, with same HOM ID (see method 
     * {@code getHomId}) and same entity IDs (for any taxon).
     * <p>
     * The newly created {@code CuratorAnnotationBean}s will have no confidence information 
     * (see method {@code getCioId}) nor supporting text (see method {@code getSupportingText}), 
     * it is up to the caller to define them based on the annotations used for inference. 
     * 
     * @param annot             A {@code CuratorAnnotationBean} used as the source of inference 
     *                          based on transformation_of relations.
     * @param outgoingEdges     A {@code boolean} defining whether outgoing or incoming edges 
     *                          should be examined. If {@code true}, outgoing edges 
     *                          are examined.
     * @param existingAnnots    A {@code Set} of {@code CuratorAnnotationBean}s with only 
     *                          HOM ID and entity IDs set, used to retrieve already existing 
     *                          annotations for any taxon.
     * @param transfOfRels      A {@code Set} of {@code OWLObjectPropertyExpression} representing 
     *                          the transformation_of properties in the Uberon ontology 
     *                          provided at instantiation.
     * @return                  A {@code Set} of {@code CuratorAnnotationBean}s that are 
     *                          the newly created annotations from inference based on 
     *                          {@code annot}, with no confidence information 
     *                          nor supporting text defined.
     */
    private Set<CuratorAnnotationBean> createInferredAnnotationsFromTransformationOf(
            CuratorAnnotationBean annot, boolean outgoingEdges, 
            Set<CuratorAnnotationBean> existingAnnots, 
            Set<OWLObjectPropertyExpression> transfOfRels) {
        log.entry(annot, outgoingEdges, existingAnnots, transfOfRels);
        
        Set<CuratorAnnotationBean> inferredAnnots = new HashSet<CuratorAnnotationBean>();
        
        //we will walk direct transformation_of relations
        CuratorAnnotationBean annotToInfer = annot;
        while (annotToInfer != null) {
            log.trace("Trying to propagate annotation through transformation_of relations: {} (using outgoing relations? {})", 
                    annotToInfer, outgoingEdges);
            
            //we retrieve the transformation_of targets/sources of each Uberon ID 
            //of the annotation.
            //uberon ID -> target/source IDs
            Map<String, Set<String>> mapTransfOfEntities = new HashMap<String, Set<String>>();
            for (String uberonId: annotToInfer.getEntityIds()) {
                
                Set<String> targetOrSourceIds = new HashSet<String>();
                mapTransfOfEntities.put(uberonId, targetOrSourceIds);
                
                OWLClass anatEntity = uberonOntWrapper.getOWLClassByIdentifier(
                        uberonId, true);
                if (anatEntity == null) {
                    continue;
                }
                
                Set<OWLGraphEdge> edges = null;
                if (outgoingEdges) {
                    edges = uberonOntWrapper.getOutgoingEdges(anatEntity);
                } else {
                    edges = uberonOntWrapper.getIncomingEdges(anatEntity);
                }
                for (OWLGraphEdge edge: edges) {
                    if (edge.getQuantifiedPropertyList().size() != 1 || 
                            !transfOfRels.contains(
                                edge.getSingleQuantifiedProperty().getProperty())) {
                        continue;
                    }
                    OWLObject cls = null;
                    if (outgoingEdges) {
                        cls = edge.getTarget();
                    } else {
                        cls = edge.getSource();
                    }
                    if (cls instanceof OWLClass) {
                        log.trace("Transformation_of relation identified for potential propagation: {}", 
                                edge);
                        targetOrSourceIds.add(uberonOntWrapper.getIdentifier(cls));
                    }
                }
            }
            
            //try to perform the propagation
            boolean toPropagate = true;
            List<String> transfOfEntityIds = new ArrayList<String>();
            for (Entry<String, Set<String>> entry: mapTransfOfEntities.entrySet()) {
                //we check that we have one and only one transformation_of target/source 
                //for each Uberon term used in the annotation (for simplicity)
                if (entry.getValue().size() != 1) {
                    toPropagate = false;
                    break;
                }
                transfOfEntityIds.add(entry.getValue().iterator().next());
            }
            if (!toPropagate) {
                log.trace("No propagation to perform.");
                return log.exit(inferredAnnots);
            }
            
            //important to order Uberon IDs for comparison to existing annotations
            Collections.sort(transfOfEntityIds);
            log.trace("Propagating annotation to entities: {}", transfOfEntityIds);
            
            //now, we check whether this corresponds to an already existing annotation
            CuratorAnnotationBean toCompare = new CuratorAnnotationBean();
            toCompare.setHomId(annotToInfer.getHomId());
            toCompare.setEntityIds(transfOfEntityIds);
            if (existingAnnots.contains(toCompare)) {
                log.trace("Propagated annotation already exists, not added.");
                return log.exit(inferredAnnots);
            }
            
            //OK, create the inferred annotation
            CuratorAnnotationBean inferredAnnot = new CuratorAnnotationBean();
            inferredAnnot.setHomId(annotToInfer.getHomId());
            inferredAnnot.setEntityIds(transfOfEntityIds);
            inferredAnnot.setNcbiTaxonId(annotToInfer.getNcbiTaxonId());
            inferredAnnot.setNegated(annotToInfer.isNegated());
            inferredAnnot.setEcoId(AUTOMATIC_ASSERTION_ECO);
            inferredAnnot.setAssignedBy(AUTOMATIC_ASSIGNED_BY);
            inferredAnnot.setCurationDate(new Date());
            
            inferredAnnots.add(inferredAnnot);
            
            //now, we continue propagation by creating an annotation identical to 
            //the original annotation, but with entity IDs updated
            CuratorAnnotationBean nextAnnot = new CuratorAnnotationBean(annotToInfer);
            nextAnnot.setEntityIds(transfOfEntityIds);
            nextAnnot.setEntityNames(null);
            annotToInfer = nextAnnot;
        }
        
        //the inferred annotations are returned when there are no more relation to walk 
        //in the previous loop
        throw log.throwing(new AssertionError("Unreachable code"));
    }
    
    private Set<CuratorAnnotationBean> inferAnnotationsFromLogicalConstraints(
            Collection<CuratorAnnotationBean> annots) throws IllegalArgumentException {
        log.entry(annots);

        log.info("Inferring annotations based on logical constraints...");
        //first, we store HOM ID - Uberon IDs to be able to determine when an annotation 
        //already exists for some structures, independently of the taxon
        //Set<CuratorAnnotationBean> existingAnnots = this.getExistingAnnots(annots);
        
        //now, we create a Map where keys are Uberon IDs used in annotations, 
        //the associated values being the annotations using these Uberon IDs; 
        //this will allow faster inferences.
        Map<String, Set<CuratorAnnotationBean>> entityIdToAnnots = 
                new HashMap<String, Set<CuratorAnnotationBean>>();
        for (CuratorAnnotationBean annot: annots) {
            for (String entityId: annot.getEntityIds()) {
                Set<CuratorAnnotationBean> mappedAnnots = entityIdToAnnots.get(entityId);
                if (mappedAnnots == null) {
                    mappedAnnots = new HashSet<CuratorAnnotationBean>();
                    entityIdToAnnots.put(entityId, mappedAnnots);
                }
                mappedAnnots.add(annot);
            }
        }
        
        //Now, we search for OWL classes defined as the intersection of annotated entities, 
        //and that are not themselves already annotated.
        //we will store in a Map the IDs of such OWL classes as keys, the associated value 
        //being the IDs of the entities composing the class.
        log.debug("Searching for IntersectionOf expressions composed of annotated classes...");
        Map<String, Set<String>> intersectMapping = new HashMap<String, Set<String>>();
        for (OWLClass cls: uberonOntWrapper.getAllOWLClasses()) {
            log.trace("Examining {}", cls);
            String clsId = uberonOntWrapper.getIdentifier(cls);
            if (entityIdToAnnots.containsKey(clsId)) {
                log.trace("Already annotated, skip");
                continue;
            }
            for (OWLClassExpression clsExpr: 
                cls.getEquivalentClasses(uberonOntWrapper.getAllOntologies())) {
                if (clsExpr instanceof OWLObjectIntersectionOf) {
                    log.trace("Examining IntersectionOf expression: {}", clsExpr);
                    boolean allClassesAnnotated = true;
                    Set<String> intersectClsIds = new HashSet<String>();
                    for (OWLClass intersectCls: clsExpr.getClassesInSignature()) {
                        String intersectClsId = uberonOntWrapper.getIdentifier(intersectCls);
                        if (!entityIdToAnnots.containsKey(intersectClsId)) {
                            allClassesAnnotated = false;
                            break;
                        }
                        intersectClsIds.add(intersectClsId);
                    }
                    if (allClassesAnnotated) {
                        log.trace("Valid IntersectionOf expression, intersect class IDs: {}", 
                                intersectClsIds);
                        intersectMapping.put(clsId, intersectClsIds);
                    }
                }
            }
        }
        log.debug("Done searching for IntersectionOf expressions, {} classes will be considered.", 
                intersectMapping.size());
        

        log.info("Done inferring annotations based on logical constraints, {} annotations inferred.", 
                );
    }
    
    /**
     * Generate a {@code Set} of {@code CuratorAnnotationBean}s derived from {@code annots} 
     * and allowing to determine which entities are already annotated. The returned 
     * {@code CuratorAnnotationBean}s have only their HOM ID (see method {@code getHomId}) 
     * and entity IDs (see method {@code getEntityIds}) defined. The entity IDs will be sorted 
     * by natural order.
     * 
     * @param annots    A {@code Set} of {@code CuratorAnnotationBean}s used to generate 
     *                  the returned {@code CuratorAnnotationBean}s.
     * @return          A {@code Set} of {@code CuratorAnnotationBean}s generated from 
     *                  {@code annots}, with only their HOM ID and entity IDs defined, 
     *                  with entity IDs sorted by natural order.
     */
    private Set<CuratorAnnotationBean> getExistingAnnots(
            Collection<CuratorAnnotationBean> annots) {
        log.entry(annots);
        
        Set<CuratorAnnotationBean> existingAnnots = new HashSet<CuratorAnnotationBean>();
        for (CuratorAnnotationBean annot: annots) {
            
            CuratorAnnotationBean existingAnnot = new CuratorAnnotationBean();
            existingAnnot.setHomId(annot.getHomId());
            List<String> entityIds = new ArrayList<String>(annot.getEntityIds());
            Collections.sort(entityIds);
            existingAnnot.setEntityIds(entityIds);
            
            existingAnnots.add(existingAnnot);
        }
        
        return log.exit(existingAnnots);
    }

    /**
     * Generates annotations for {@link GeneratedFileType} {@code RAW_CLEAN}: 
     * notably, this method adds Uberon names, ECO term names, etc (see method 
     * {@code getNewAnnotWithExtraInfo}), and add inferred annotations (for instance, 
     * using transformation_of relations).
     * <p>
     * The {@code Map}s in the returned {@code List} are ordered by calling 
     * {@link #sortAnnotations(List)}. {@code rawAnnots} will not be modified 
     * as a result of the call to this method.
     * 
     * @param rawAnnots         A {@code List} of {@code Map}s, where 
     *                          each {@code Map} represents a raw annotation line, 
     *                          as provided by curators.
     *                          See {@link #extractAnnotations(String, boolean)} for details 
     *                          about the key-value pairs in this {@code Map}.
     * @param uberonOntWrapper  An {@code OWLGraphWrapper} wrapping the Uberon ontology.
     * @param taxOntWrapper     An {@code OWLGraphWrapper} wrapping the taxonomy ontology.
     * @param ecoOntWrapper     An {@code OWLGraphWrapper} wrapping the ECO ontology.
     * @param homOntWrapper     An {@code OWLGraphWrapper} wrapping the HOM ontology 
     *                          (ontology of homology an related concepts).
     * @param confOntWrapper    An {@code OWLGraphWrapper} wrapping the confidence 
     *                          code ontology.
     * @return                  An ordered {@code List} of {@code Map}s containing newly 
     *                          created summary annotations, as well as original single 
     *                          evidence annotations.
     * @throws IllegalArgumentException If {@code rawAnnots} does not allow to produce any 
     *                                  raw clean annotations.
     */
    //TODO: add inferred annotations
    private List<Map<String, Object>> generateRawCleanAnnotations(
            List<Map<String, Object>> rawAnnots, 
            OWLGraphWrapper uberonOntWrapper, OWLGraphWrapper taxOntWrapper, 
            OWLGraphWrapper ecoOntWrapper, OWLGraphWrapper homOntWrapper, 
            OWLGraphWrapper confOntWrapper) throws IllegalArgumentException {
        
        List<Map<String, Object>> rawCleanAnnots = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> rawAnnot: rawAnnots) {
            //clone the raw annotation to not modify it.
            Map<String, Object> rawCleanAnnot = this.getNewAnnotWithExtraInfo(rawAnnot, 
                    uberonOntWrapper, taxOntWrapper, ecoOntWrapper, homOntWrapper, 
                    confOntWrapper);
            rawCleanAnnot.put(LINE_TYPE_COL_NAME, GeneratedFileType.RAW_CLEAN);
            rawCleanAnnots.add(rawCleanAnnot);
        }
        if (rawCleanAnnots.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The provided annotations " +
                    "did not allow to generate any clean-transformed annotations."));
        }
        
        this.sortAnnotations(rawCleanAnnots);
        
        return log.exit(rawCleanAnnots);
    }
    
    /**
     * Generates annotations for {@link GeneratedFileType} {@code AGGREGATED_EVIDENCES}: 
     * if some annotations are related to a same entity, taxon, and HOM IDs, then it is needed 
     * to generate automatic annotations summarizing these related annotations, and 
     * associated to a confidence statement from "multiple evidence lines". 
     * When an assertion is supported only by one evidence (single evidence annotation), 
     * then it is not modified.
     * <p>
     * The newly created summary annotations, and the original single evidence annotations, 
     * are returned as a {@code List} of {@code Map}s. {@code Map}s are ordered by calling 
     * {@link #sortAnnotations(List)}. {@code rawCleanAnnotations} will not be modified 
     * as a result of the call to this method.
     * <p>
     * All annotations in {@code rawCleanAnnotations} must be associated to an ECO term, 
     * and to a confidence statement from single evidence with confidence level. This means 
     * that these annotations should be of type {@code RAW} or {@code RAW_CLEAN}.
     * 
     * @param rawCleanAnnotations   A {@code Collection} of {@code Map}s where each {@code Map} 
     *                              represents an annotation. 
     *                              See {@link #extractAnnotations(String, boolean)} for details 
     *                              about the key-value pairs in this {@code Map}.
     * @param ecoOntWrapper         An {@code OWLGraphWrapper} wrapping the evidence code 
     *                              ontology.
     * @param confOntWrapper        An {@code OWLGraphWrapper} wrapping the confidence 
     *                              information ontology.
     * @return                      An ordered {@code List} of {@code Map}s containing newly 
     *                              created summary annotations, as well as original single 
     *                              evidence annotations.
     * @throws IllegalArgumentException If {@code rawCleanAnnotations} are not all associated 
     *                                  to an ECO term and to a confidence statement 
     *                                  from single evidence with a confidence level.
     */
    private List<Map<String, Object>> generateAggregatedEvidencesAnnotations(
            Collection<Map<String, Object>> rawCleanAnnotations, 
            OWLGraphWrapper ecoOntWrapper, OWLGraphWrapper confOntWrapper) 
                    throws IllegalArgumentException {
        log.entry(rawCleanAnnotations, ecoOntWrapper, confOntWrapper);
        
        OntologyUtils ecoUtils = new OntologyUtils(ecoOntWrapper);
        CIOWrapper cioWrapper = new CIOWrapper(confOntWrapper);
        
        //in order to identify related annotations, we will use a Map where keys 
        //are the concatenation of the entity column, the taxon column, the HOM column, and 
        //associated values are the related annotations.
        Map<String, Set<Map<String, Object>>> relatedAnnotMapper = 
                new HashMap<String, Set<Map<String, Object>>>();
        //OWLClass used for sanity checks
        OWLClass singleEvidenceConcordance = 
                cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                CIOWrapper.SINGLE_EVIDENCE_CONCORDANCE_ID);
        
        //first pass, group related annotations and do sanity checks
        for (Map<String, Object> annot: rawCleanAnnotations) {
            //discard rejected annotations to generate summaries
            if (cioWrapper.isRejectedStatement(
                    confOntWrapper.getOWLClassByIdentifier((String) annot.get(CONF_COL_NAME)))) {
                continue;
            }
            //check that the confidence statement is a confidence from single evidence 
            //associated to a confidence level.
            OWLClass confStatement = confOntWrapper.getOWLClassByIdentifier(
                    (String) annot.get(CONF_COL_NAME), true);
            if (!cioWrapper.getEvidenceConcordance(confStatement).equals(
                    singleEvidenceConcordance) || 
                 cioWrapper.getConfidenceLevel(confStatement) == null) {
                throw log.throwing(new IllegalArgumentException("All confidence statements used "
                        + "must be from single evidence with associated confidence level."));
            }
            
            //To generate the key to group annotations, we order Uberon IDs
            List<String> uberonIds = AnnotationCommon.parseMultipleEntitiesColumn(
                    (String) annot.get(ENTITY_COL_NAME));
            String key = "";
            for (String uberonId: uberonIds) {
                key += uberonId + "-";
            }
            key += annot.get(HOM_COL_NAME) + "-" + annot.get(TAXON_COL_NAME);
            
            if (relatedAnnotMapper.get(key) == null) {
                relatedAnnotMapper.put(key, new HashSet<Map<String, Object>>());
            }
            relatedAnnotMapper.get(key).add(annot);
        }
        
        //now, generate summarizing annotations
        final OWLClass congruent = confOntWrapper.getOWLClassByIdentifier(
                CIOWrapper.CONGRUENT_CONCORDANCE_ID);
        List<Map<String, Object>> aggregatedEvidencesAnnots = 
                new ArrayList<Map<String, Object>>();
        for (Set<Map<String, Object>> relatedAnnots: relatedAnnotMapper.values()) {
            assert relatedAnnots.size() > 0;
            
            //clone a reference annotation from the group
            Map<String, Object> newAnnot = new HashMap<String, Object>(
                    relatedAnnots.iterator().next());
            newAnnot.put(LINE_TYPE_COL_NAME, GeneratedFileType.AGGREGATED_EVIDENCES);
            aggregatedEvidencesAnnots.add(newAnnot);

            //columns that should not be set for a generated summary annotation
            newAnnot.put(REF_COL_NAME, null);
            newAnnot.put(REF_TITLE_COL_NAME, null);
            newAnnot.put(ECO_COL_NAME, null);
            newAnnot.put(ECO_NAME_COL_NAME, null);
            newAnnot.put(SUPPORT_TEXT_COL_NAME, null);
            newAnnot.put(CURATOR_COL_NAME, null);
            newAnnot.put(DATE_COL_NAME, null);
            
            //if more than one annotation related to this assertion, 
            //compute a global confidence score
            if (relatedAnnots.size() > 1) {

                //retrieve the global confidence score from all related evidence lines
                OWLClass summaryConf = this.getSummaryConfidenceStatement(relatedAnnots, 
                        ecoUtils, cioWrapper);

                //fill other columns of the annotation.
                //columns with values changed as compared to the reference annotation
                newAnnot.put(CONF_COL_NAME, confOntWrapper.getIdentifier(summaryConf));
                newAnnot.put(CONF_NAME_COL_NAME, confOntWrapper.getLabel(summaryConf));
                newAnnot.put(ASSIGN_COL_NAME, BGEE_ASSIGNMENT);
                
                if (!cioWrapper.getEvidenceConcordance(summaryConf).equals(congruent)) {
                    //in case of conflicts, we always favor the positive annotation, 
                    //this is what the annotation file is about.
                    newAnnot.put(QUALIFIER_COL_NAME, null);
                }
            }
        }
        
        if (aggregatedEvidencesAnnots.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The annotations provided did not "
                    + "allow to retrieve any AGGREGATED EVIDENCE annotations."));
        }
        
        this.sortAnnotations(aggregatedEvidencesAnnots);
        return log.exit(aggregatedEvidencesAnnots);
    }
    
    /**
     * Retrieve the proper confidence statement from multiple evidence lines from the provided 
     * {@code Collection} of annotations. The annotations provided should all be related to 
     * a same assertion, so that we can infer a global confidence level. The definition 
     * of whether annotations are related to a same assertion is left to the caller 
     * of this method, this is not verified by this method. The only check is that 
     * all annotations should be associated to a confidence statement from single evidence, 
     * with a confidence level, and should be associated to an ECO term.
     * 
     * @param annots        A {@code Collection} of {@code Map}s where each {@code Map} 
     *                      represents an annotation, each related to a same assertion. 
     *                      See {@link #extractAnnotations(String, boolean)} for details 
     *                      about the key-value pairs in this {@code Map}.
     * @param ecoUtils      An {@code OntologyUtils} wrapping the ECO.
     * @param cioWrapper    A {@code CIOWrapper} wrapping... the CIO.
     * @return              An {@code OWLClass} representing a confidence statement 
     *                      from multiple evidence lines, associated to a confidence level.
     * @throws IllegalArgumentException If some annotations are not associated to 
     *                                  a confidence statement from single evidence 
     *                                  with a confidence level, or if the annotations provided 
     *                                  do not allow to compute a global confidence score.
     */
    private OWLClass getSummaryConfidenceStatement(Collection<Map<String, Object>> annots, 
            OntologyUtils ecoUtils, CIOWrapper cioWrapper) throws IllegalArgumentException {
        log.entry(annots, ecoUtils, cioWrapper);
        
        //first, we need to know whether there are only evidences supporting 
        //the assertion, or negating the assertion, or both, and how many
        int positiveAnnotCount = 0;
        int negativeAnnotCount = 0;
        //we need to store all evidences related to positive annotations supporting 
        //the assertion, to determine whether evidences are of the same type, 
        //and whether they are of the same type as contradicting evidences. 
        Set<OWLClass> positiveECOs = new HashSet<OWLClass>();
        //same for negative assertions
        Set<OWLClass> negativeECOs = new HashSet<OWLClass>();
        //to determine the best confidence for positive annotations, we also store them all. 
        Set<OWLClass> positiveConfs = new HashSet<OWLClass>();
        //same for negative annots
        Set<OWLClass> negativeConfs = new HashSet<OWLClass>();
        
        final OWLClass singleEvidenceConcordance = 
                cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                CIOWrapper.SINGLE_EVIDENCE_CONCORDANCE_ID);
        
        for (Map<String, Object> annot: annots) {
            OWLClass confStatement = cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                    (String) annot.get(CONF_COL_NAME), true);
            OWLClass ecoTerm = ecoUtils.getWrapper().getOWLClassByIdentifier(
                    (String) annot.get(ECO_COL_NAME), true);
            
            //------ SANITY CHECKS---------------
            //discard rejected annotations to generate summaries
            if (cioWrapper.isRejectedStatement(confStatement)) {
                continue;
            }
            //check that this confidence statement is from the single evidence branch 
            //and is associated to a confidence level.
            if (!cioWrapper.getEvidenceConcordance(confStatement).equals(
                    singleEvidenceConcordance) || 
                 cioWrapper.getConfidenceLevel(confStatement) == null) {
                throw log.throwing(new IllegalArgumentException("All confidence statements used "
                        + "must be from single evidence with associated confidence level."));
            }
            if (ecoTerm == null) {
                throw log.throwing(new IllegalArgumentException("All annotations "
                        + "must be associated to an ECO term."));
            }
            //-----------------------------------
            
            boolean currentNegate = this.isNegativeAnnotations(annot);
            Set<OWLClass> toUseECOs  = positiveECOs;
            Set<OWLClass> toUseConfs = positiveConfs;
            if (currentNegate) {
                negativeAnnotCount++;
                toUseECOs = negativeECOs;
                toUseConfs = negativeConfs;
            } else {
                positiveAnnotCount++;
            }
            toUseECOs.add(ecoTerm);
            toUseConfs.add(confStatement);
        }
        if ((negativeAnnotCount + positiveAnnotCount) < 2) {
            throw log.throwing(new IllegalArgumentException("At least two valid annotations "
                    + "must be provided to compute a global confidence score."));
        }
        assert positiveAnnotCount > 0 || negativeAnnotCount > 0;
        assert !positiveConfs.isEmpty() || !negativeConfs.isEmpty();
        
        //if we have conflicting evidence lines, we want to know whether they are of the same 
        //or of different types (in that case, we do not want to know whether 
        //positive annotations on one hand, or negative annotations on the other hand, 
        //have same or multiple evidence types). Otherwise, we check that over all evidence lines. 
        //XXX: should we really consider ECOs 'author statement' as a 'different type'?
        OWLClass evidenceTypeConcordance = cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                CIOWrapper.SAME_TYPE_EVIDENCE_CONCORDANCE_ID);
        if ((hasPositiveAnnots && hasNegativeAnnots && 
                ecoUtils.containsUnrelatedClassesByIsAPartOf(positiveConfs, negativeConfs)) || 
                (hasPositiveAnnots && 
                        ecoUtils.containsUnrelatedClassesByIsAPartOf(positiveConfs)) || 
                (hasNegativeAnnots && 
                        ecoUtils.containsUnrelatedClassesByIsAPartOf(negativeConfs))) {
                evidenceTypeConcordance = cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                        CIOWrapper.DIFFERENT_TYPES_EVIDENCE_CONCORDANCE_ID);
        } 
        //Determine the best confidence level, and if we have conflicting evidence lines, 
        //determine the conflict level (weak or strong). 
        OWLClass evidenceConcordance = cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                CIOWrapper.CONGRUENT_CONCORDANCE_ID);
        OWLClass confidenceLevel = null;
        if (positiveAnnotCount > 0 && negativeAnnotCount > 0) {
            OWLClass bestPositiveTerm = cioWrapper.getBestTermWithConfidenceLevel(positiveConfs);
            OWLClass bestNegativeTerm = cioWrapper.getBestTermWithConfidenceLevel(negativeConfs);
            //if we have as much or less negative annotations than positive annotations, 
            //and bestNegativeTerm is of equal or higher confidence level than bestPositiveTerm, 
            //or if we have more negative annotations than positive annotations
            //=> strong conflict
            if ((negativeAnnotCount <= positiveAnnotCount && 
                    (bestNegativeTerm.equals(bestPositiveTerm) || 
                    cioWrapper.getBestTermWithConfidenceLevel(
                            Arrays.asList(bestNegativeTerm, bestPositiveTerm)).equals(
                                    bestNegativeTerm))) || 
                negativeAnnotCount > positiveAnnotCount) {
                
                evidenceConcordance = cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                        CIOWrapper.STRONGLY_CONFLICTING_CONCORDANCE_ID);
                //for strongly conflicting evidence lines, there is no confidence level associated.
                confidenceLevel = null;
            } else {
                evidenceConcordance = cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                        CIOWrapper.WEAKLY_CONFLICTING_CONCORDANCE_ID);
                //for weakly conflicting evidence lines, we take the confidence level 
                //from the best supporting evidence
                confidenceLevel = cioWrapper.getConfidenceLevel(bestPositiveTerm);
            }
        } else if (positiveAnnotCount > 0) {
            confidenceLevel = cioWrapper.getConfidenceLevel(
                    cioWrapper.getBestTermWithConfidenceLevel(positiveConfs));
        } else if (negativeAnnotCount > 0) {
            confidenceLevel = cioWrapper.getConfidenceLevel(
                    cioWrapper.getBestTermWithConfidenceLevel(negativeConfs));
        }
        
        //infer the confidence statement for the aggregated evidence lines.
        OWLClass summaryConf = cioWrapper.getConfidenceStatement(evidenceConcordance, 
                evidenceTypeConcordance, confidenceLevel);
        if (summaryConf == null) {
            throw log.throwing(new AssertionError("Could not find the appropriate " +
                    "multiple evidence lines confidence code."));
        }
        
        return log.exit(summaryConf);
    }
    
    /**
     * Generates annotations for {@link GeneratedFileType} {@code SINGLE_TAXON}: 
     * some annotations can be related to same entity and HOM IDs, but different taxa 
     * (alternative homology hypothesis, or confirmatory information of homology 
     * at the level of sub-taxa); in that case, we want to identify the 'true' common ancestor 
     * for an anatomical entity/HOM term. If {@code trustedAnnotations} is {@code true}, 
     * low confidence annotations will be discarded, if annotation of correct confidence 
     * are available.
     * <p>
     * The annotations are returned as a {@code List} of {@code Map}s. It contains at most 
     * one annotation for same anatomical entity/HOM IDs. {@code Map}s are ordered 
     * by calling {@link #sortAnnotations(List)}. 
     * {@code summaryAnnots} will not be modified as a result of the call to this method.
     * <p>
     * All annotations in {@code summaryAnnots} must be {@code AGGREGATED_EVIDENCE} annotations. 
     * This notably means that there should never multiple annotations related to same 
     * entity/HOM/taxon IDs.
     * 
     * @param summaryAnnots         A {@code List} of {@code Map}s, where 
     *                              each {@code Map} represents a raw annotation line, 
     *                              as provided by curators.
     *                              See {@link #extractAnnotations(String, boolean)} for details 
     *                              about the key-value pairs in this {@code Map}.
     * @param taxOntWrapper         An {@code OWLGraphWrapper} wrapping the taxonomy ontology.
     * @param confOntWrapper        An {@code OWLGraphWrapper} wrapping the confidence 
     *                              code ontology.
     * @param trustedAnnotations    A {@code boolean} defining whether low confidence annotations 
     *                              should be discarded if possible to identify common ancestor.  
     * @return                      An ordered {@code List} of {@code Map}s containing 
     *                              single taxon annotations.
     * @throws IllegalArgumentException If {@code summaryAnnots} does not allow to produce any 
     *                                  single taxon annotations, or contains annotations 
     *                                  related to same entity/HOM/taxon IDs.
     */
    private List<Map<String, Object>> generateAncestralTaxaAnnotations(
            List<Map<String, Object>> summaryAnnots, OWLGraphWrapper taxOntWrapper, 
            OWLGraphWrapper confOntWrapper, boolean trustedAnnotations) 
                    throws IllegalArgumentException {
        log.entry(summaryAnnots, taxOntWrapper, confOntWrapper, trustedAnnotations);
        
        OntologyUtils taxOntUtils = new OntologyUtils(taxOntWrapper);
        CIOWrapper cioWrapper = new CIOWrapper(confOntWrapper);
        
        //in order to identify annotations related to a same structure but different taxa, 
        //we will use a Map where keys are the concatenation of the entity column 
        //and the HOM column, and associated values are the related annotations.
        Map<String, Set<Map<String, Object>>> relatedAnnotMapper = 
                new HashMap<String, Set<Map<String, Object>>>();
        //for sanity checks
        Set<String> controlKeys = new HashSet<String>();
        for (Map<String, Object> annot: summaryAnnots) {
            //discard rejected and negative annotations to generate summaries 
            //(we only want positive annotations here)
            if (this.isNegativeAnnotations(annot) || 
                cioWrapper.isRejectedStatement(
                    confOntWrapper.getOWLClassByIdentifier((String) annot.get(CONF_COL_NAME)))) {
                continue;
            }
            //To generate the key to group annotations, we order Uberon IDs
            List<String> uberonIds = AnnotationCommon.parseMultipleEntitiesColumn(
                    (String) annot.get(ENTITY_COL_NAME));
            //key to group annotations related to a same structure but different taxa
            String key = "";
            //key used to do a sanity check (see below)
            String controlKey = "";
            for (String uberonId: uberonIds) {
                key        += uberonId + "-";
                controlKey += uberonId + "-";
            }
            key        += annot.get(HOM_COL_NAME);
            controlKey += annot.get(HOM_COL_NAME) + "-" + annot.get(TAXON_COL_NAME);
            
            //check that we never see twice annotations related to a same entity/HOM/taxon 
            //(because we are supposed to use summary annotations, which merge such annotations 
            //related to same entity/HOM/taxon)
            if (!controlKeys.add(controlKey)) {
                throw log.throwing(new IllegalArgumentException("RAW annotations were provided, "
                        + "while it should be AGGREGATED_EVIDENCE annotations. Redundant "
                        + "annotations: " + annot));
            }
            
            //group annotations
            if (relatedAnnotMapper.get(key) == null) {
                relatedAnnotMapper.put(key, new HashSet<Map<String, Object>>());
            }
            relatedAnnotMapper.get(key).add(annot);
        }
        
        //now, generate summarizing annotations. Remember that we have store only 
        //summary positive annotations.
        List<Map<String, Object>> singleTaxonAnnots = new ArrayList<Map<String, Object>>();
        for (Set<Map<String, Object>> relatedAnnots: relatedAnnotMapper.values()) {
            assert relatedAnnots.size() > 0;

            //if more than one annotation related to this assertion, find the correct 
            //ancestral taxon. We need to consider all potential taxa, in case 
            //trustedAnnotations is false, or, if trustedAnnotations is true, in case 
            //there are no annotations of sufficiently good confidence. 
            Set<OWLClass> allTaxa = new HashSet<OWLClass>();
            Set<OWLClass> trustedTaxa = new HashSet<OWLClass>();
            for (Map<String, Object> annot: relatedAnnots) {
                OWLClass taxon = taxOntWrapper.getOWLClassByIdentifier(
                        OntologyUtils.getTaxOntologyId((int) annot.get(TAXON_COL_NAME)));
                allTaxa.add(taxon);
                if (!cioWrapper.isBgeeNotTrustedStatement(cioWrapper.getOWLGraphWrapper().
                        getOWLClassByIdentifier((String) annot.get(CONF_COL_NAME)))) {
                    trustedTaxa.add(taxon);
                }
            }
            Set<OWLClass> taxaToUse = allTaxa;
            if (trustedAnnotations && !trustedTaxa.isEmpty()) {
                taxaToUse = trustedTaxa;
            }
            
            //retain only parent taxa; we should identify one taxon, but it is possible 
            //to have independent taxa in case of independent evolution
            if (taxaToUse.size() > 1) {
                taxOntUtils.retainParentClasses(taxaToUse, null);
            }
            assert taxaToUse.size() > 0;
            
            //identify valid annotations to retain
            for (Map<String, Object> annot: relatedAnnots) {
                OWLClass taxon = taxOntWrapper.getOWLClassByIdentifier(
                        OntologyUtils.getTaxOntologyId((int) annot.get(TAXON_COL_NAME)));
                if (taxaToUse.contains(taxon)) {
                    Map<String, Object> newAnnot = new HashMap<String, Object>(annot);
                    newAnnot.put(LINE_TYPE_COL_NAME, GeneratedFileType.SINGLE_TAXON);
                    //columns that should not be set for a generated single taxon annotation
                    newAnnot.put(REF_COL_NAME, null);
                    newAnnot.put(REF_TITLE_COL_NAME, null);
                    newAnnot.put(ECO_COL_NAME, null);
                    newAnnot.put(ECO_NAME_COL_NAME, null);
                    newAnnot.put(SUPPORT_TEXT_COL_NAME, null);
                    newAnnot.put(CURATOR_COL_NAME, null);
                    newAnnot.put(DATE_COL_NAME, null);
                    
                    singleTaxonAnnots.add(newAnnot);
                }
            }
        }
        
        if (singleTaxonAnnots.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The annotations provided did not "
                    + "allow to retrieve any SINGLE TAXON annotations."));
        }
        
        this.sortAnnotations(singleTaxonAnnots);
        return log.exit(singleTaxonAnnots);
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
    //TODO: remove method after taking javadoc
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
        Set<Integer> taxonIds = TaxonConstraints.extractTaxonIds(taxonConstraintsFile);
        Map<String, Set<Integer>> taxonConstraints = 
                TaxonConstraints.extractTaxonConstraints(taxonConstraintsFile);
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
     * that is of type {@code GeneratedFileType}, in a TSV file format.
     * 
     * @param outputFile    A {@code String} that is the path to the output file to be written.
     * @param fileType      A {@code GeneratedFileType} defining what type of file is going 
     *                      to be written. This allows to define headers, etc. 
     * @param annotations   A {@code List} of {@code Map}s, where each {@code Map} 
     *                      represents an annotation line.
     * @throws IOException  If an error occurs while trying to write in the file.
     */
    private void writeAnnotationsToFile(String outputFile, GeneratedFileType fileType, 
            List<Map<String, Object>> annotations) 
                    throws IOException {
        log.entry(outputFile, fileType, annotations);
        
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
     * Order {@code annotations} by alphabetical order of some fields, 
     * for easier diff between different releases of the annotation file. 
     * {@code annotations} will be modified as a result of the call to this method.
     * 
     * @param annotations   A {@code List} of {@code AnnotationBean}s to be ordered.
     * @param <T>           The type of {@code AnnotationBean} in {@code annotations}.
     */
    private <T extends AnnotationBean> void sortAnnotations(List<T> annotations) {
        Collections.sort(annotations, new Comparator<T>() {
            @Override
            public int compare(AnnotationBean o1, AnnotationBean o2) {

                String homId1 = o1.getHomId();
                if (homId1 == null) {
                    homId1 = "";
                }
                String homId2 = o2.getHomId();
                if (homId2 == null) {
                    homId2 = "";
                }
                int comp = homId1.compareTo(homId2);
                if (comp != 0) {
                    return comp;
                }
                
                String elementId1 = o1.getEntityIds().toString();
                String elementId2 = o2.getEntityIds().toString();
                comp = elementId1.compareTo(elementId2);
                if (comp != 0) {
                    return comp;
                }
                
                int taxonId1 = o1.getNcbiTaxonId();
                int taxonId2 = o2.getNcbiTaxonId();
                if (taxonId1 < taxonId2) {
                    return -1;
                } else if (taxonId1 > taxonId2) {
                    return 1;
                }
                
                if (!o1.isNegated() && o2.isNegated()) {
                    return -1;
                } else if (o1.isNegated() && !o2.isNegated()) {
                    return 1;
                }
                
                String confId1 = o1.getCioId();
                if (confId1 == null) {
                    confId1 = "";
                }
                String confId2 = o2.getCioId();
                if (confId2 == null) {
                    confId2 = "";
                }
                comp = confId1.compareTo(confId2);
                if (comp != 0) {
                    return comp;
                }
                
                
                if (o1 instanceof RawAnnotationBean && o2 instanceof RawAnnotationBean) {
                    String ecoId1 = ((RawAnnotationBean) o1).getEcoId();
                    if (ecoId1 == null) {
                        ecoId1 = "";
                    }
                    String ecoId2 = ((RawAnnotationBean) o2).getEcoId();
                    if (ecoId2 == null) {
                        ecoId2 = "";
                    }
                    comp = ecoId1.compareTo(ecoId2);
                    if (comp != 0) {
                        return comp;
                    }
                    
                    String refId1 = ((RawAnnotationBean) o1).getRefId();
                    if (refId1 == null) {
                        refId1 = "";
                    }
                    String refId2 = ((RawAnnotationBean) o2).getRefId();
                    if (refId2 == null) {
                        refId2 = "";
                    }
                    comp = refId1.compareTo(refId2);
                    if (comp != 0) {
                        return comp;
                    }
                }
                
                String supportText1 = o1.getSupportingText();
                if (supportText1 == null) {
                    supportText1 = "";
                }
                String supportText2 = o2.getSupportingText();
                if (supportText2 == null) {
                    supportText2 = "";
                }
                comp = supportText1.compareTo(supportText2);
                if (comp != 0) {
                    return comp;
                }
                
                
                return 0;
            }
        });
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
        
        Set<Integer> taxonIds = AnnotationCommon.getTaxonIds(annotFile);
        try(PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile), "utf-8")))) {
            for (int taxonId: taxonIds) {
                writer.println(taxonId);
            }
        }
        
        log.exit();
    }
    
    /**
     * Retrieve annotations for a specific taxon from a similarity annotation file 
     * and write them into an output file.
     * 
     * @param similarityFile    A {@code String} that is the path to the annotation file.
     * @param fileType          A {@code GeneratedFileType} defining what type of file is 
     *                          {@code similarityFile}. This allows to define headers, etc.
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
    public void writeToFileAnnotationsForTaxon(String similarityFile, 
            GeneratedFileType fileType, String taxOntFile, 
            int taxonId, String outputFile) throws FileNotFoundException, IOException, 
            OWLOntologyCreationException, OBOFormatParserException {
        log.entry(similarityFile, fileType, taxOntFile, taxonId, outputFile);
        
        //TODO: generalized for any type of annotation
        List<Map<String, Object>> summarizedAnnotations = 
                this.extractSummaryAnnotationsForTaxon(similarityFile, taxOntFile, taxonId);
        this.writeAnnotationsToFile(outputFile, fileType, summarizedAnnotations);
        
        log.exit();
    }
    
    /**
     * Retrieve summarized annotations for a specific taxon from a <strong>clean</strong> 
     * similarity annotation file. Only <strong>positive</strong> annotations are retrieved 
     * (annotations with soleley a "NOT" qualifier are not returned).
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
     * See {@link #extractAnnotations(String, boolean)} for details about the keys used 
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
        OWLClass taxClass = wrapper.getOWLClassByIdentifier(OntologyUtils.getTaxOntologyId(taxonId), true);
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
        //iterate all annotations
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
                    annotation.get(LINE_TYPE_COL_NAME).equals( ))) {
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
}
