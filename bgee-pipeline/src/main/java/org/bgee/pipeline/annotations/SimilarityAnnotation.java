package org.bgee.pipeline.annotations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.AncestralTaxaAnnotationBean;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.AnnotationBean;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.ParseMultipleStringValues;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.ParseQualifier;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.RawAnnotationBean;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.SummaryAnnotationBean;
import org.bgee.pipeline.ontologycommon.CIOWrapper;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.bgee.pipeline.uberon.TaxonConstraints;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.supercsv.cellprocessor.FmtBool;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.Trim;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvBeanWriter;
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
     * for this reason). Also, {@code equals}, {@code hashCode}, and {@code toString} 
     * methods are overridden to discard unused attributes.
     * <p>
     * This bean should have been the parent class of {@code RawAnnotationBean}, 
     * not the other way around, as it use only a subset of the attributes of 
     * {@code RawAnnotationBean}, and has none of its own. This is for two reasons: 
     * first, curators do provide labels associated to term IDs in their annotations, 
     * so originally they were considered, but they are not anymore; second, 
     * {@code RawAnnotationBean} was originally provided completely independently, 
     * outside of the Bgee pipeline, so it was not possible to extend {@code CuratorAnnotationBean}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public static class CuratorAnnotationBean extends RawAnnotationBean {
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
                    log.trace("Ref ID extracted: {}", refId);
                    super.setRefId(refId.trim());
                }
                if (StringUtils.isNotBlank(refTitle)) {
                    refTitle = refTitle.trim();
                    refTitle = refTitle.startsWith("\"") ? refTitle.substring(1) : refTitle;
                    refTitle = refTitle.endsWith("\"") ? 
                            refTitle.substring(0, refTitle.length()-1) : refTitle;
                    log.trace("Ref title extracted: {}", refTitle);
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
        //In this CuratorAnnotationBean, the title is actually set when calling setRefId. 
        //This method does nothing and is defined only for overriding 
        //the RawAnnotationBean default method. 
        @SuppressWarnings("unused")
        public void setRefTitle(String refTitle) {
            //nothing here, the title is set when calling setRefId
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.getCioId() == null) ? 0 : this.getCioId().hashCode());
            result = prime * result
                    + ((this.getEntityIds() == null) ? 0 : this.getEntityIds() .hashCode());
            result = prime * result + ((this.getHomId() == null) ? 0 : this.getHomId().hashCode());
            result = prime * result + this.getNcbiTaxonId();
            result = prime * result + (this.isNegated() ? 1231 : 1237);
            result = prime
                    * result
                    + ((this.getSupportingText() == null) ? 0 : this.getSupportingText().hashCode());
            result = prime * result
                    + ((this.getAssignedBy() == null) ? 0 : this.getAssignedBy().hashCode());
            result = prime * result
                    + ((this.getCurationDate() == null) ? 0 : this.getCurationDate().hashCode());
            result = prime * result
                    + ((this.getCurator() == null) ? 0 : this.getCurator().hashCode());
            result = prime * result + ((getEcoId() == null) ? 0 : getEcoId().hashCode());
            result = prime * result + ((getRefId() == null) ? 0 : getRefId().hashCode());
            result = prime * result
                    + ((getRefTitle() == null) ? 0 : getRefTitle().hashCode());
            return result;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CuratorAnnotationBean other = (CuratorAnnotationBean) obj;
            if (getCioId() == null) {
                if (other.getCioId() != null) {
                    return false;
                }
            } else if (!getCioId().equals(other.getCioId())) {
                return false;
            }
            if (getEntityIds() == null) {
                if (other.getEntityIds() != null) {
                    return false;
                }
            } else if (!getEntityIds().equals(other.getEntityIds())) {
                return false;
            }
            if (getHomId() == null) {
                if (other.getHomId() != null) {
                    return false;
                }
            } else if (!getHomId().equals(other.getHomId())) {
                return false;
            }
            if (getNcbiTaxonId() != other.getNcbiTaxonId()) {
                return false;
            }
            if (isNegated() != other.isNegated()) {
                return false;
            }
            if (getSupportingText() == null) {
                if (other.getSupportingText() != null) {
                    return false;
                }
            } else if (!getSupportingText().equals(other.getSupportingText())) {
                return false;
            }
            if (getAssignedBy() == null) {
                if (other.getAssignedBy() != null) {
                    return false;
                }
            } else if (!getAssignedBy().equals(other.getAssignedBy())) {
                return false;
            }
            if (getCurationDate() == null) {
                if (other.getCurationDate() != null) {
                    return false;
                }
            } else if (!getCurationDate().equals(other.getCurationDate())) {
                return false;
            }
            if (getCurator() == null) {
                if (other.getCurator() != null) {
                    return false;
                }
            } else if (!getCurator().equals(other.getCurator())) {
                return false;
            }
            if (getEcoId() == null) {
                if (other.getEcoId() != null) {
                    return false;
                }
            } else if (!getEcoId().equals(other.getEcoId())) {
                return false;
            }
            if (getRefId() == null) {
                if (other.getRefId() != null) {
                    return false;
                }
            } else if (!getRefId().equals(other.getRefId())) {
                return false;
            }
            if (getRefTitle() == null) {
                if (other.getRefTitle() != null) {
                    return false;
                }
            } else if (!getRefTitle().equals(other.getRefTitle())) {
                return false;
            }
            return true;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "CuratorAnnotationBean [" 
                    + "homId=" + getHomId() 
                    + ", entityIds=" + getEntityIds()
                    + ", ncbiTaxonId=" + getNcbiTaxonId()
                    + ", negated=" + isNegated()
                    + ", cioId=" + getCioId()  
                    + ", supportingText=" + getSupportingText()
                    + ", refId=" + getRefId() 
                    + ", refTitle=" + getRefTitle() 
                    + ", ecoId=" + getEcoId() 
                    + ", assignedBy=" + getAssignedBy() 
                    + ", curator=" + getCurator() 
                    + ", curationDate=" + getCurationDate() + "]";
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
     * A {@code String} that is the OBO-like ID of the 'historical homology' concept 
     * in the HOM ontology.
     */
    public final static String HISTORICAL_HOMOLOGY_ID = "HOM:0000007";
    
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
     * <li>If the first element in {@code args} is "generateReleaseFile", the action 
     * will be to generate proper annotations from the curator annotation file, and 
     * to write them into different release files, see 
     * {@link #generateReleaseFile(String, String, String, String)}.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>the path to the file containing taxon constraints. See {@link 
     *   org.bgee.pipeline.uberon.TaxonConstraints}. Can be empty (see 
     *   {@link org.bgee.pipeline.CommandRunner#parseArgument(String)}).
     *   <li>A {@code Map<String, Set<Integer>>} to potentially override taxon constraints, 
     *   see {@link org.bgee.pipeline.CommandRunner#parseMapArgumentAsInteger(String)} to see 
     *   how to provide it in command line. See constructor {@link SimilarityAnnotation#
     *   SimilarityAnnotation(String, Map, String, String, String, String, String)} 
     *   for more details about overriding taxon constraints. Can be empty (see 
     *   {@link org.bgee.pipeline.CommandRunner#EMPTY_LIST}).
     *   <li>the path to the Uberon ontology.
     *   <li>the path to the taxonomy ontology.
     *   <li>the path to the homology and related concepts (HOM) ontology.
     *   <li>the path to the ECO ontology.
     *   <li>the path to the confidence information ontology (CIO).
     *   <li>the path to the curator annotation file.
     *   <li>the path to the file in which to write RAW CLEAN annotations.
     *   <li>the path to the file in which to write SUMMARY annotations.
     *   <li>the path to the file in which to write ANCESTRAL TAXA annotations.
     *   </ol>
     * </ul>
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws FileNotFoundException    If some files could not be found.
     * @throws IllegalArgumentException If {@code args} does not contain the proper 
     *                                  parameters or does not allow to obtain 
     *                                  correct information.
     * @throws IllegalStateException    If some ontologies provided at instantiation do not 
     *                                  allow to retrieve required information to process 
     *                                  the annotations.
     * @throws IOException              If the annotation files provided could not be read 
     *                                  or write.
     * @throws OBOFormatParserException     If an error occurred while parsing an ontology file.
     * @throws OWLOntologyCreationException If an error occurred while loading an Ontology.
     * @throws UnknownOWLOntologyException  If an error occurred while loading an Ontology.
     */
    public static void main(String[] args) throws FileNotFoundException, 
        IllegalArgumentException, IllegalStateException, IOException, 
        UnknownOWLOntologyException, OWLOntologyCreationException, OBOFormatParserException {
        
        log.entry((Object[]) args);
        
        if (args[0].equalsIgnoreCase("extractTaxonIds")) {
            if (args.length != 3) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "3 arguments, " + args.length + " provided."));
            }
            extractTaxonIdsToFile(args[1], args[2]);
//        } else if (args[0].equalsIgnoreCase("generateSummaryFileForTaxon")) {
//            if (args.length != 5) {
//                throw log.throwing(new IllegalArgumentException(
//                        "Incorrect number of arguments provided, expected " + 
//                        "5 arguments, " + args.length + " provided."));
//            }
//            new SimilarityAnnotation().writeToFileSummaryAnnotationsForTaxon(
//                    args[1], args[2], Integer.parseInt(args[3]), args[4]);
        } else if (args[0].equalsIgnoreCase("getAnatEntitiesWithNoTransformationOf")) {
            if (args.length != 4) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "4 arguments, " + args.length + " provided."));
            }
            writeAnatEntitiesWithNoTransformationOfToFile(args[1], args[2], args[3]);
        } else if (args[0].equalsIgnoreCase("generateReleaseFile")) {
            if (args.length != 12) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "12 arguments, " + args.length + " provided."));
            }
            new SimilarityAnnotation(CommandRunner.parseArgument(args[1]), 
                    CommandRunner.parseMapArgumentAsInteger(args[2]), 
                    args[3], args[4], args[5], args[6], args[7]).generateReleaseFiles(
                            args[8], args[9], args[10], args[11]);
        } else {
            throw log.throwing(new UnsupportedOperationException("The following action " +
                    "is not recognized: " + args[0]));
        }
        
        log.exit();
    }
    
    /**
     * Extracts annotations from the provided curator annotation file containing information 
     * capable of populating {@code CuratorAnnotationBean}s. 
     * It returns a {@code List} of {@code CuratorAnnotationBean}s, where each 
     * {@code AnnotationBean} represents a row in the file. The elements 
     * in the {@code List} are ordered as they were read from the file. 
     * <p>
     * We do not use the method provided by {@link SimilarityAnnotationUtils}, 
     * because not the same fields are mandatory 
     * in the released files and in the file used by curator.
     * 
     * @param similarityFile    A {@code String} that is the path to an annotation file
     *                          from curators. 
     * @return                  A {@code List} of {@code CuratorAnnotationBean}s where each 
     *                          element represents a row in the file, ordered as 
     *                          they were read from the file.
     * @throws FileNotFoundException    If {@code similarityFile} could not be found.
     * @throws IOException              If {@code similarityFile} could not be read.
     * @throws IllegalArgumentException If {@code similarityFile} did not allow to retrieve 
     *                                  any annotation or could not be properly parsed.
     */
    public static List<CuratorAnnotationBean> extractCuratorAnnotations(String similarityFile) 
            throws FileNotFoundException, IOException, IllegalArgumentException {
        log.entry(similarityFile);
        
        //curator file has variable number of columns in different lines. 
        //We cannot use a CsvBeanReader for such files, 
        //so, first, we modify the file to have same number of columns in all lines.
        File originalFile = new File(similarityFile);
        File tmpFile = File.createTempFile("curator_generated_", "_tmp");
        //try clause to delete the tmp file in a finally clause.
        try {
            Utils.standardizeCSVFileColumnCount(originalFile, tmpFile, Utils.TSVCOMMENTED);
            
            //now, we can properly use a BeanReader on the temporary file.
            try (ICsvBeanReader annotReader = new CsvBeanReader(new FileReader(tmpFile), 
                    Utils.TSVCOMMENTED)) {
                
                List<CuratorAnnotationBean> annots = new ArrayList<CuratorAnnotationBean>();
                final String[] header = annotReader.getHeader(true);
                
                String[] attributeMapping = mapCuratorHeaderToAttributes(header);
                CellProcessor[] cellProcessorMapping = mapCuratorHeaderToCellProcessors(header);
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
        } finally {
            tmpFile.delete();
        }
    }

    /**
     * Map the column names of a curator annotation file to the {@code CellProcessor}s 
     * used to populate {@code CuratorAnnotationBean}. We do not use the method provided 
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
    private static CellProcessor[] mapCuratorHeaderToCellProcessors(String[] header) 
            throws IllegalArgumentException {
        log.entry((Object[]) header);
        
        CellProcessor[] processors = new CellProcessor[header.length];
        for (int i = 0; i < header.length; i++) {
            //curators often use additional non-standard columns in their annotation file, 
            //so we don't throw an exception in case of unrecognized column
            if (header[i] == null) {
                continue;
            }
            switch (header[i]) {
            // *** CellProcessors common to all AnnotationBean types ***
                case SimilarityAnnotationUtils.ENTITY_COL_NAME: 
                    processors[i] = new ParseMultipleStringValues();
                    break;
                case SimilarityAnnotationUtils.TAXON_COL_NAME: 
                    processors[i] = new ParseInt();
                    break;
                case SimilarityAnnotationUtils.QUALIFIER_COL_NAME: 
                    processors[i] = new ParseQualifier();
                    break;
                case SimilarityAnnotationUtils.DATE_COL_NAME: 
                    processors[i] = new ParseDate(SimilarityAnnotationUtils.DATE_FORMAT);
                    break; 
                case SimilarityAnnotationUtils.HOM_COL_NAME: 
                case SimilarityAnnotationUtils.CONF_COL_NAME: 
                case SimilarityAnnotationUtils.REF_COL_NAME: 
                case SimilarityAnnotationUtils.ECO_COL_NAME: 
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
                case SimilarityAnnotationUtils.SUPPORT_TEXT_COL_NAME: 
                //REF title is stored in the same column as REF ID in curator annotation file.
                //Only in generated files the title is stored in its own column
                case SimilarityAnnotationUtils.REF_TITLE_COL_NAME: 
                    processors[i] = new Optional();
                    break;
                default: 
                    //curators often use additional non-standard columns in their annotation file, 
                    //so we don't throw an exception in case of unrecognized column
                    processors[i] = new Optional();
                    break;
            }
        }
        return log.exit(processors);
    }


    /**
     * Map the column names of a CSV file to the attributes of {@code CuratorAnnotationBean}. 
     * This mapping will then be used to populate or read the bean, using standard 
     * getter/setter name convention. 
     * <p>
     * Thanks to this method, we can adapt to any change in column names or column order.
     * 
     * @param header    An {@code Array} of {@code String}s representing the names 
     *                  of the columns of a similarity annotation file.
     * @return          An {@code Array} of {@code String}s that are the names 
     *                  of the attributes of {@code CuratorAnnotationBean}, put in 
     *                  the {@code Array} at the same index as their corresponding column.
     * @throws IllegalArgumentException If a {@code String} in {@code header} 
     *                                  is not recognized.
     */
    private static String[] mapCuratorHeaderToAttributes(String[] header) 
            throws IllegalArgumentException {
        log.entry((Object[]) header);
        
        //we call the method to generate attribute mapping for RawAnnotationBean, 
        //and we override the fields not used for CuratorAnnotationBean, 
        //to avoid code duplication.
        String[] mapping = SimilarityAnnotationUtils.mapHeaderToAttributes(header, 
                RawAnnotationBean.class);
        for (int i = 0; i < header.length; i++) {
            //curators often use additional non-standard columns in their annotation file,
            //so we consider only necessary columns
            if (header[i] != null && 
                    !SimilarityAnnotationUtils.HOM_COL_NAME.equals(header[i]) && 
                    !SimilarityAnnotationUtils.ENTITY_COL_NAME.equals(header[i]) && 
                    !SimilarityAnnotationUtils.TAXON_COL_NAME.equals(header[i]) && 
                    !SimilarityAnnotationUtils.QUALIFIER_COL_NAME.equals(header[i]) && 
                    !SimilarityAnnotationUtils.CONF_COL_NAME.equals(header[i]) && 
                    !SimilarityAnnotationUtils.ASSIGN_COL_NAME.equals(header[i]) && 
                    !SimilarityAnnotationUtils.REF_COL_NAME.equals(header[i]) && 
                    !SimilarityAnnotationUtils.ECO_COL_NAME.equals(header[i]) && 
                    !SimilarityAnnotationUtils.SUPPORT_TEXT_COL_NAME.equals(header[i]) && 
                    !SimilarityAnnotationUtils.CURATOR_COL_NAME.equals(header[i]) && 
                    !SimilarityAnnotationUtils.DATE_COL_NAME.equals(header[i])) {
                mapping[i] = null;
            }
        }
        return log.exit(mapping);
    }

    /**
     * Write the provided annotations into the CSV file {@code outputFile}. 
     * The columns of the file will be defined depending on the provided bean type. 
     * Note that the type {@code CuratorAnnotationBean} is not accepted, 
     * as such annotations are not meant to be written in release files.
     * The parameters to write the CSV file are provided by 
     * {@link org.bgee.pipeline.Utils#TSVCOMMENTED}.
     * 
     * @param annots        A {@code List} of {@code AnnotationBean}s to be written 
     *                      in a CSV file.
     * @param outputFile    A {@code String} that is the path to the CSV output file.
     * @param beanType      A {@code Class} that is the type of {@code AnnotationBean} 
     *                      to be written.
     * @param <T>           The type of {@code AnnotationBean}.
     * @throws IllegalArgumentException If {@code beanType} is not supported by this method.
     * @throws IOException              If an error occurred while writing the CSV file.
     */
    public static <T extends AnnotationBean> void writeAnnotations(List<T> annots, 
            String outputFile, Class<T> beanType) throws IllegalArgumentException, IOException {
        log.entry(annots, outputFile, beanType);
        
        //here we don't accept CuratorAnnotationBean, they're not meant to be written 
        //in release files.
        if (!beanType.equals(RawAnnotationBean.class) && 
                !beanType.equals(SummaryAnnotationBean.class) && 
                !beanType.equals(AncestralTaxaAnnotationBean.class)) {
            throw log.throwing(new IllegalArgumentException("Unsupported "
                    + "AnnotationBean type: " + beanType));
        }

        final String[] header = getHeader(beanType);
        String[] attributeMapping = 
                SimilarityAnnotationUtils.mapHeaderToAttributes(header, beanType);
        final CellProcessor[] processors = mapHeaderToWriteCellProcessors(header, beanType);
        final boolean[] quoteModes = mapHeaderToQuoteModes(header);
        
        try (ICsvBeanWriter beanWriter = new CsvBeanWriter(new FileWriter(outputFile), 
                Utils.getCsvPreferenceWithQuote(quoteModes))) {
            // write the header
            beanWriter.writeHeader(header);
            
            // write the beans
            for (final T annot : annots) {
                    beanWriter.write(annot, attributeMapping, processors);
            }
        }
        
        log.exit();
    }
    
    /**
     * Generate the header used to write {@code AnnotationBean}s of the provided type 
     * in a CSV file. Note that the type {@code CuratorAnnotationBean} is not accepted, 
     * as such annotations are not meant to be written in release files.
     * 
     * @param beanType  A {@code Class} that is the type of {@code AnnotationBean} that 
     *                  will be written in a CSV file.
     * @return          An {@code Array} of {@code String}s that are the names of the columns, 
     *                  in the order to be displayed, of the CSV file to be written.
     * @throws IllegalArgumentException If {@code beanType} is not supported by this method.
     */
    private static String[] getHeader(Class<? extends AnnotationBean> beanType) 
            throws IllegalArgumentException {
        log.entry(beanType);
        
        //here we don't accept CuratorAnnotationBean, they're not meant to be written 
        //in release files.
        if (!beanType.equals(RawAnnotationBean.class) && 
                !beanType.equals(SummaryAnnotationBean.class) && 
                !beanType.equals(AncestralTaxaAnnotationBean.class)) {
            throw log.throwing(new IllegalArgumentException("Unsupported "
                    + "AnnotationBean type: " + beanType));
        }
        
        int columnCount = 0;
        if (beanType.equals(RawAnnotationBean.class)) {
            columnCount = 17;
        } else if (beanType.equals(SummaryAnnotationBean.class)) {
            columnCount = 11;
        } else if (beanType.equals(AncestralTaxaAnnotationBean.class)) {
            columnCount = 9;
        }
        assert columnCount > 0;
        
        String[] header = new String[columnCount];
        
        int i = 0;
        header[i] = SimilarityAnnotationUtils.HOM_COL_NAME;
        i++;
        header[i] = SimilarityAnnotationUtils.HOM_NAME_COL_NAME;
        i++;
        header[i] = SimilarityAnnotationUtils.ENTITY_COL_NAME;
        i++;
        header[i] = SimilarityAnnotationUtils.ENTITY_NAME_COL_NAME;
        i++;
        
        //no need for the qualifier column in Ancestral taxa files, annotations used 
        //are all positive.
        if (!beanType.equals(AncestralTaxaAnnotationBean.class)) {
            header[i] = SimilarityAnnotationUtils.QUALIFIER_COL_NAME;
            i++;
        }
        
        header[i] = SimilarityAnnotationUtils.TAXON_COL_NAME;
        i++;
        header[i] = SimilarityAnnotationUtils.TAXON_NAME_COL_NAME;
        i++;
        header[i] = SimilarityAnnotationUtils.CONF_COL_NAME;
        i++;
        header[i] = SimilarityAnnotationUtils.CONF_NAME_COL_NAME;
        i++;
        
        //columns specific to SummaryAnnotationBeans to interleave here.
        if (beanType.equals(SummaryAnnotationBean.class)) {
            header[i] = SimilarityAnnotationUtils.TRUSTED_COL_NAME;
            i++;
            header[i] = SimilarityAnnotationUtils.ANNOT_COUNT_COL_NAME;
            i++;
        }
        
        //columns specific to RawAnnotationBeans to interleave here.
        if (beanType.equals(RawAnnotationBean.class)) {
            header[i] = SimilarityAnnotationUtils.ECO_COL_NAME;
            i++;
            header[i] = SimilarityAnnotationUtils.ECO_NAME_COL_NAME;
            i++;
            header[i] = SimilarityAnnotationUtils.REF_COL_NAME;
            i++;
            header[i] = SimilarityAnnotationUtils.REF_TITLE_COL_NAME;
            i++;
        }
        
        //we write a supporting text only for non-summary annotations
        if (!beanType.equals(SummaryAnnotationBean.class)) {
            header[i] = SimilarityAnnotationUtils.SUPPORT_TEXT_COL_NAME;
            i++;
        }
        
        //columns specific to RawAnnotationBeans to interleave here.
        if (beanType.equals(RawAnnotationBean.class)) {
            header[i] = SimilarityAnnotationUtils.ASSIGN_COL_NAME;
            i++;
            header[i] = SimilarityAnnotationUtils.CURATOR_COL_NAME;
            i++;
            header[i] = SimilarityAnnotationUtils.DATE_COL_NAME;
            i++;
        }
        
        assert header.length > 0;
        return log.exit(header);
    }

    /**
     * Map the column names of a CSV file to the {@code CellProcessor}s 
     * used to write {@code AnnotationBean}s of the requested type in the file. 
     * Note that the type {@code CuratorAnnotationBean} is not accepted, 
     * as such annotations are not meant to be written in release files.
     * 
     * @param header    An {@code Array} of {@code String}s representing the names 
     *                  of the columns of a similarity annotation file, in which to write 
     *                  {@code AnnotationBean}s of type {@code beanType}.
     * @param beanType  A {@code Class} defining the type of {@code AnnotationBean} 
     *                  that will be written in CSV file.
     * @return          An {@code Array} of {@code CellProcessor}s, put in 
     *                  the {@code Array} at the same index as the column they are supposed 
     *                  to process for write.
     * @throws IllegalArgumentException If a {@code String} in {@code header} 
     *                                  is not recognized, or if {@code beanType} is not 
     *                                  supported by this method.
     */
    private static CellProcessor[] mapHeaderToWriteCellProcessors(String[] header, 
            Class<? extends AnnotationBean> beanType) throws IllegalArgumentException {
        log.entry(header, beanType);
        
        //here we don't accept CuratorAnnotationBean, they're not meant to be written 
        //in released files.
        if (!beanType.equals(RawAnnotationBean.class) && 
                !beanType.equals(SummaryAnnotationBean.class) && 
                !beanType.equals(AncestralTaxaAnnotationBean.class)) {
            throw log.throwing(new IllegalArgumentException("Unsupported "
                    + "AnnotationBean type: " + beanType));
        }
        
        CellProcessor[] processors = new CellProcessor[header.length];
        for (int i = 0; i < header.length; i++) {
            switch (header[i]) {
            // *** CellProcessors common to all AnnotationBean types ***
                case SimilarityAnnotationUtils.ENTITY_COL_NAME: 
                case SimilarityAnnotationUtils.ENTITY_NAME_COL_NAME: 
                    processors[i] = new Utils.FmtMultipleStringValues(new Trim());
                    break;
                case SimilarityAnnotationUtils.TAXON_COL_NAME: 
                    processors[i] = new NotNull();
                    break;
                case SimilarityAnnotationUtils.HOM_COL_NAME: 
                case SimilarityAnnotationUtils.HOM_NAME_COL_NAME: 
                case SimilarityAnnotationUtils.CONF_COL_NAME: 
                case SimilarityAnnotationUtils.CONF_NAME_COL_NAME: 
                case SimilarityAnnotationUtils.TAXON_NAME_COL_NAME: 
                    processors[i] = new StrNotNullOrEmpty(new Trim());
                    break;
                case SimilarityAnnotationUtils.QUALIFIER_COL_NAME: 
                    processors[i] = new FmtBool("NOT", "");
                    break;
                case SimilarityAnnotationUtils.SUPPORT_TEXT_COL_NAME: 
                    processors[i] = new Optional(new Trim());
                    break;
            }
            //if it was one of the column common to all AnnotationBeans, 
            //iterate next column name
            if (processors[i] != null) {
                continue;
            }
            
            if (beanType.equals(RawAnnotationBean.class)) {
                switch (header[i]) {
                // *** Attributes specific to RawAnnotationBean ***
                    case SimilarityAnnotationUtils.DATE_COL_NAME: 
                        processors[i] = new Optional(
                                new FmtDate(SimilarityAnnotationUtils.DATE_FORMAT));
                        break; 
                    case SimilarityAnnotationUtils.ECO_COL_NAME: 
                    case SimilarityAnnotationUtils.ECO_NAME_COL_NAME: 
                    case SimilarityAnnotationUtils.ASSIGN_COL_NAME: 
                        processors[i] = new StrNotNullOrEmpty(new Trim());
                        break;
                    //these fields are not mandatory in case of inferred annotations
                    case SimilarityAnnotationUtils.CURATOR_COL_NAME: 
                    case SimilarityAnnotationUtils.REF_COL_NAME: 
                    case SimilarityAnnotationUtils.REF_TITLE_COL_NAME:
                        processors[i] = new Optional(new Trim());
                        break;
                }
            } else if (beanType.equals(SummaryAnnotationBean.class)) {
                switch (header[i]) {
                // *** Attributes specific to SummaryAnnotationBean ***
                    case SimilarityAnnotationUtils.TRUSTED_COL_NAME: 
                        processors[i] = new FmtBool("T", "F");
                        break;
                    case SimilarityAnnotationUtils.ANNOT_COUNT_COL_NAME: 
                        processors[i] = new NotNull();
                        break;
                }
            } else if (beanType.equals(AncestralTaxaAnnotationBean.class)) {
                //no columns specific to AncestralTaxaAnnotationBean for now
            } 
            
            if (processors[i] == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                        + header[i] + " for AnnotationBean type: " + beanType));
            }
        }
        return log.exit(processors);
        
    }

    /**
     * Map the column names of a CSV file to an {@code Array} of {@code boolean}s 
     * (one per CSV column) indicating whether the quoting of the corresponding column 
     * should be forced. Note that this method does not perform any check 
     * on the validity of columns in {@code header}.
     * 
     * @param header    An {@code Array} of {@code String}s representing the names 
     *                  of the columns of a similarity annotation file.
     * @return          An {@code Array} of {@code boolean}s (one per CSV column) indicating 
     *                  whether quoting of the corresponding column should be forced.
     */
    private static boolean[] mapHeaderToQuoteModes(String[] header) {
        log.entry((Object[]) header);
        
        boolean[] quoteModes = new boolean[header.length];
        for (int i = 0; i < header.length; i++) {
            switch (header[i]) {
                //it seems that simple quotes (') are problematic for import in R, 
                //while not quoted by default by SuperCSV. So we only have to identify 
                //the columns for which we want to "force" the quoting.
                case SimilarityAnnotationUtils.ENTITY_NAME_COL_NAME: 
                case SimilarityAnnotationUtils.CONF_NAME_COL_NAME: 
                case SimilarityAnnotationUtils.TAXON_NAME_COL_NAME: 
                case SimilarityAnnotationUtils.HOM_NAME_COL_NAME: 
                case SimilarityAnnotationUtils.SUPPORT_TEXT_COL_NAME: 
                case SimilarityAnnotationUtils.ECO_NAME_COL_NAME: 
                case SimilarityAnnotationUtils.REF_TITLE_COL_NAME:
                    quoteModes[i] = true;
                    break;
                default: 
                    quoteModes[i] = false;
                    break;
            }
        }
        return log.exit(quoteModes);
        
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
    public static void extractTaxonIdsToFile(String annotFile, String outputFile) 
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
            OWLClass anatEntity = uberonOntWrapper.getOWLClassByIdentifier(
                    anatEntityId.trim(), true);
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
     *                                      containing taxon constraints. Can be {@code null}, 
     *                                      in which case the taxon constraint verifications 
     *                                      will not be performed.
     *                                      See {@link org.bgee.pipeline.uberon.TaxonConstraints}
     * @param idStartsToOverridenTaxonIds   A {@code Map} where keys are {@code String}s 
     *                                      representing prefixes of uberon terms to match, 
     *                                      the associated value being a {@code Set} 
     *                                      of {@code Integer}s to replace taxon constraints 
     *                                      of matching terms, if {@code taxonConstraintsFile} 
     *                                      is not {@code null}. See 
     *                                      {@link org.bgee.pipeline.uberon.TaxonConstraints#
     *                                      extractTaxonConstraints(String, Map)} for example 
     *                                      of use. Can be {@code null}.
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
        this((taxonConstraintsFile == null? 
                null: 
                TaxonConstraints.extractTaxonConstraints(taxonConstraintsFile, 
                        idStartsToOverridenTaxonIds)), 
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
     *                          exists. Can be {@code null}, in which case, the 
     *                          taxon constraint verifications will not be performed.
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
            OWLGraphWrapper homOntWrapper, OWLGraphWrapper ecoOntWrapper, 
            OWLGraphWrapper confOntWrapper) {
        this.missingUberonIds = new HashSet<String>();
        this.missingTaxonIds = new HashSet<Integer>();
        this.missingECOIds = new HashSet<String>();
        this.missingHOMIds = new HashSet<String>();
        this.missingCONFIds = new HashSet<String>();
        this.duplicates = new HashSet<AnnotationBean>();
        this.incorrectFormat = new HashSet<AnnotationBean>();
        this.idsNotExistingInTaxa = new HashMap<String, Set<Integer>>();
        
        this.taxonConstraints = taxonConstraints;
        this.uberonOntWrapper = uberonOntWrapper;
        this.taxOntWrapper = taxOntWrapper;
        this.ecoOntWrapper = ecoOntWrapper;
        this.homOntWrapper = homOntWrapper;
        this.cioWrapper = new CIOWrapper(confOntWrapper);
    }
    
    /**
     * Generate the similarity annotation release files from the annotations 
     * provided by curators. This method will: 
     * <ol>
     * <li>extract the curator annotations from {@code curatorAnnotsFilePath} by calling 
     * {@link #extractCuratorAnnotations(String)}.
     * <li>produce the RAW and inferred annotations based on the curator annotations by calling 
     * {@link #generateRawAnnotations(Collection)}, and write them into {@code rawAnnotsFilePath} 
     * by calling {@link #writeAnnotations(List, String, Class)}.
     * <li>produce the SUMMARY annotations from the generated RAW annotations by calling 
     * {@link #generateSummaryAnnotations(Collection)}, and write them into 
     * {@code summaryAnnotsFilePath} by calling {@link #writeAnnotations(List, String, Class)}.
     * <li>produce the ANCESTRAL TAXA annotations from the generated SUMMARY annotations 
     * by calling {@link #generateAncestralTaxaAnnotations(Collection)}, and write them into 
     * {@code ancestralTaxaAnnotsFilePath} by calling {@link #writeAnnotations(List, String, Class)}.
     * </ol>
     * 
     * @param curatorAnnotsFilePath         A {@code String} that is the path to the curator 
     *                                      annotation file to be read.
     * @param rawAnnotsFilePath             A {@code String} that is the path to the RAW 
     *                                      annotation file to be written.
     * @param summaryAnnotsFilePath         A {@code String} that is the path to the SUMMARY 
     *                                      annotation file to be written.
     * @param ancestralTaxaAnnotsFilePath   A {@code String} that is the path to the 
     *                                      ANCESTRAL TAXA annotation file to be written.
     * @throws FileNotFoundException        If {@code curatorAnnotsFilePath} could not be found.
     * @throws IllegalArgumentException     If some errors were detected in the provided 
     *                                      curator annotations.
     * @throws IllegalStateException        If the information provided at instantiation 
     *                                      did not allow to perform the requested operations.
     * @throws IOException                  If a file could not be read or written.
     */
    public void generateReleaseFiles(String curatorAnnotsFilePath, String rawAnnotsFilePath, 
            String summaryAnnotsFilePath, String ancestralTaxaAnnotsFilePath) 
                    throws FileNotFoundException, IllegalArgumentException, IOException {
        log.entry(curatorAnnotsFilePath, rawAnnotsFilePath, summaryAnnotsFilePath, 
                ancestralTaxaAnnotsFilePath);
        
        //extract curator annotations
        List<CuratorAnnotationBean> curatorAnnots = 
                extractCuratorAnnotations(curatorAnnotsFilePath);
        //check errors and warnings right away (methods latter called will only check 
        //for errors)
        this.checkAnnotations(curatorAnnots, true);
        
        //generate RAW annotations. This will verify correctness of curator annotations, 
        //will generate automatically inferred annotations and RAW annotations, 
        //and will verify correctness of these generated annotations.
        List<RawAnnotationBean> rawAnnots = this.generateRawAnnotations(curatorAnnots);
        //write in file
        writeAnnotations(rawAnnots, rawAnnotsFilePath, RawAnnotationBean.class);
        
        //generate SUMMARY annotations. This will verify corretness of the RAW annotations 
        //(yes, this will be redundant), will generate SUMMARY annotations, and will verify 
        //validity of these generated annotations.
        List<SummaryAnnotationBean> summaryAnnots = this.generateSummaryAnnotations(rawAnnots);
        //write in file
        writeAnnotations(summaryAnnots, summaryAnnotsFilePath, SummaryAnnotationBean.class);
        //release RAW annots, not needed anymore and might take lots of memory
        rawAnnots = null;
        
        //generate ANCESTRAL TAXA annotations. This will verify correctness of the SUMMARY 
        //annotations (yes, again), will filter the annotations to retrieve ancestral taxa, 
        //and will check correctness of the new annotations retrieved.
        List<AncestralTaxaAnnotationBean> ancestralTaxaAnnots = 
                this.generateAncestralTaxaAnnotations(summaryAnnots);
        //write in file
        writeAnnotations(ancestralTaxaAnnots, ancestralTaxaAnnotsFilePath, 
                AncestralTaxaAnnotationBean.class);
    }
    
    /**
     * Check the correctness of the annotations provided. This methods perform many checks, 
     * such as checking for presence of mandatory fields, checking for duplicated annotations, 
     * or potentially missing annotations. This methods will also log some warnings 
     * for issues that might be present on purpose, so that it does not raise an exception, 
     * but might be potential errors.
     * 
     * @param annots            A {@code Collection} of {@code T}s, where each {@code Map} 
     *                          represents a line of annotation. See {@link 
     *                          #extractAnnotations(String, GeneratedFileType)} for more details.
     * @param checkWarn         A {@code boolean} defining whether potential errors should be 
     *                          checked (formal errors are always checked). 
     * @param <T>               The type of {@code AnnotationBean} to check.
     * @throws IllegalArgumentException     If some errors were detected.
     */
    public <T extends AnnotationBean> void checkAnnotations(Collection<T> annots, 
            boolean checkWarn) throws IllegalArgumentException {
        log.entry(annots, checkWarn);
        log.debug("Start checking {} annotations (with warnings? {})", annots.size(), checkWarn);
        
        //* We will store taxa associated to positive and negative annotations, 
        //to verify NOT annotations in RAW and curator annotations (if there is a NOT annotation 
        //in a taxon for a structure, most likely there should be also a NOT annotation 
        //for all parent taxa annotated with positive annotations for the same structure). 
        //* Also, if there are positive annotations using multiple Uberon IDs, most likely 
        //there should be positive annotations for the individual Uberon IDs as well.
        //We will use a RawAnnotationBean as key to store HOM ID - Uberon IDs.
        //* Also, if there is a negative annotation about a structure in a taxon, 
        //most likely there should be positive annotations for the same structure in sub-taxa.
        boolean checkPosNegAnnots = false;
        Map<RawAnnotationBean, Set<Integer>> positiveAnnotsToTaxa = 
                new HashMap<RawAnnotationBean, Set<Integer>>();
        Map<RawAnnotationBean, Set<Integer>> negativeAnnotsToTaxa = 
                new HashMap<RawAnnotationBean, Set<Integer>>();
        
        //Also, we will look for potentially duplicated annotations. We will search for 
        //exact duplicates, and potential duplicates (see below).
        Set<RawAnnotationBean> checkExactDuplicates = new HashSet<RawAnnotationBean>();
        Set<RawAnnotationBean> checkPotentialDuplicates = new HashSet<RawAnnotationBean>();
        //Also, SUMMARY and ANCESTRAL TAXA annotations should be unique over same 
        //HOM ID - Uberon IDs - taxon ID
        Set<RawAnnotationBean> homEntityTaxDuplicates = new HashSet<RawAnnotationBean>();
        
        //for ANCESTRAL TAXA annotations, we can have annotations with same HOM ID - entity IDs 
        //and different taxa, in case of independent evolution, but we should never have 
        //annotations with same HOM ID - entity IDs to parent-child taxa. 
        //We will use a Map where keys are RawAnnotationBean 
        //storing HOM ID - entity IDs, the associated value being Set of Integers 
        //that are the taxon IDs associated.
        Map<RawAnnotationBean, Set<Integer>> annotsToAncestralTaxa = 
                new HashMap<RawAnnotationBean, Set<Integer>>();
        
        //first pass, check each annotation
        Set<Integer> taxonIds = null;
        if (taxonConstraints != null) {
            taxonIds = TaxonConstraints.extractTaxonIds(taxonConstraints);
        }
        for (T annot: annots) {
            if (!this.checkAnnotation(annot, taxonIds)) {
                continue;
            }
            if (annot instanceof RawAnnotationBean) {
                checkPosNegAnnots = true;
            }

            //need to order potential multiple values in ENTITY_COL_NAME to identify duplicates
            List<String> uberonIds = SimilarityAnnotationUtils.trimAndSort(annot.getEntityIds());
            
            //to check for duplicates, we will use only some columns
            //First, for ANCESTRAL TAXA annotations, we need to collect all taxa 
            //associated to a same HOM ID - entity IDs
            RawAnnotationBean annotToAncestralTaxa = new RawAnnotationBean();
            annotToAncestralTaxa.setHomId(annot.getHomId());
            annotToAncestralTaxa.setEntityIds(uberonIds);
            //Now, for SUMMARY and ANCESTRAL TAXA annotations, we should see only once 
            //a same HOM ID - entity IDs - taxon ID
            RawAnnotationBean homEntityTaxDuplicate = new RawAnnotationBean(annotToAncestralTaxa);
            homEntityTaxDuplicate.setNcbiTaxonId(annot.getNcbiTaxonId());
            //Then, for any annotation we check for potential duplicates, 
            //over all fields but supporting text
            RawAnnotationBean checkPotentialDuplicate = new RawAnnotationBean(homEntityTaxDuplicate);
            checkPotentialDuplicate.setNegated(annot.isNegated());
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
            } else if (checkWarn && !checkPotentialDuplicates.add(checkPotentialDuplicate)) {
                //we do not throw an exception for a potential duplicate, 
                //but we log a warn message, it's still most likely a duplicate
                log.warn("Some annotations seem duplicated (different supporting textes, "
                        + "but all other fields equal): " + checkPotentialDuplicate);
            }
            if ((annot instanceof SummaryAnnotationBean || 
                    annot instanceof AncestralTaxaAnnotationBean) && 
                    !homEntityTaxDuplicates.add(homEntityTaxDuplicate)) {
               //an exception will be thrown afterwards (see method verifyErrors)
                this.duplicates.add(homEntityTaxDuplicate);
            }
            if (annot instanceof AncestralTaxaAnnotationBean) {
                Set<Integer> ancestralTaxa = annotsToAncestralTaxa.get(annotToAncestralTaxa);
                if (ancestralTaxa == null) {
                    ancestralTaxa = new HashSet<Integer>();
                    annotsToAncestralTaxa.put(annotToAncestralTaxa, ancestralTaxa);
                }
                ancestralTaxa.add(annot.getNcbiTaxonId());
            }
            
            //we store positive and negative annotations associated to taxa here.
            RawAnnotationBean keyBean = new RawAnnotationBean();
            keyBean.setEntityIds(uberonIds);
            keyBean.setHomId(annot.getHomId().trim());
            
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
        
        //check incorrect redundant higher taxa in ANCESTRAL TAXA annotations
        OntologyUtils taxOntUtils = new OntologyUtils(taxOntWrapper);
        for (Entry<RawAnnotationBean, Set<Integer>> annotToAncestralTaxa: 
            annotsToAncestralTaxa.entrySet()) {
            //retrieve the taxa as OWLClasses
            Set<OWLClass> ancestralTaxClasses = new HashSet<OWLClass>();
            for (int taxId: annotToAncestralTaxa.getValue()) {
                ancestralTaxClasses.add(taxOntWrapper.getOWLClassByIdentifier(
                        OntologyUtils.getTaxOntologyId(taxId), true));
            }
            //now, we retain only parent classes from the Set; if it is modified, 
            //then it means that parent-child taxa were annotated to same HOM ID - entity IDs
            int taxCount = ancestralTaxClasses.size();
            assert taxCount > 0;
            taxOntUtils.retainParentClasses(ancestralTaxClasses, null);
            if (ancestralTaxClasses.size() != taxCount) {
                //generate fake annotations to be stored for error verification
                log.error("Some Ancestral taxa annotations with same HOM ID - entity IDs "
                        + "are annotated to parent-child taxa, this should never happen. "
                        + "Parent taxa: {} - annotation: {}", ancestralTaxClasses, 
                        annotToAncestralTaxa.getKey());
                for (OWLClass ancestralTaxCls: ancestralTaxClasses) {
                    RawAnnotationBean fakeAnnot = new RawAnnotationBean(
                            annotToAncestralTaxa.getKey());
                    fakeAnnot.setNcbiTaxonId(OntologyUtils.getTaxNcbiId(
                            taxOntWrapper.getIdentifier(ancestralTaxCls)));
                    this.incorrectFormat.add(fakeAnnot);
                }
            }
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
        if (checkWarn && checkPosNegAnnots) {
            
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
                    check.setHomId(posAnnot.getHomId().trim());
                    check.setEntityIds(Arrays.asList(uberonId.trim()));
                    if (!positiveAnnotsToTaxa.containsKey(check)) {
                        log.warn("An annotation uses multiple entity IDs, but there is no annotation for the individual entity: {} - annotation: {}", 
                                uberonId, posAnnot);
                    }
                }
            }
            
            //* Also, if there is a negative annotation about a structure in a taxon, 
            //most likely there should be positive annotations for the same structure in sub-taxa.
        }

        log.debug("Done checking {} annotations", annots.size());
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
     *                          for each annotation. Can be {@code null} if no taxon constraints 
     *                          were provided at instantiation (in order to skip taxon constraint 
     *                          verfications)
     * @param lineNumber        An {@code int} providing the line number where {@code annotation} 
     *                          was retrieved at. Should be equal to 0 if {@code annotation} 
     *                          was not retrieved from a file. Useful for logging purpose.
     * @return                  {@code false} if the line of annotation contained 
     *                          any error.
     * @throws IllegalArgumentException If {@code annotation} contains some incorrectly 
     *                                  formatted information.
     */
    private <T extends AnnotationBean> boolean checkAnnotation(T annot, 
            Set<Integer> taxonIds) throws IllegalArgumentException {
        log.entry(annot, taxonIds);
        
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
            log.error("Missing taxon ID in annotation {}", annot);
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
                    continue;
                }
                if (!(annot instanceof CuratorAnnotationBean) && 
                        !uberonId.trim().equals(uberonId)) {
                    //fields in all annotations but curator annotations should have 
                    //been trimmed
                    log.error("Entity ID not trimmed in annotation {}", annot);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                }
            }
        }
        if (missingUberon) {
            log.error("Missing Uberon ID in annotation {}", annot);
            this.incorrectFormat.add(annot);
            allGood = false;
        }
        if (StringUtils.isBlank(annot.getHomId())) {
            log.error("Missing HOM ID in annotation {}", annot);
            this.incorrectFormat.add(annot);
            allGood = false;
        } else if (!(annot instanceof CuratorAnnotationBean) && 
                !annot.getHomId().trim().equals(annot.getHomId())) {
            //fields in all annotations but curator annotations should have 
            //been trimmed
            log.error("HOM ID not trimmed in annotation {}", annot);
            this.incorrectFormat.add(annot);
            allGood = false;
        }
        if (StringUtils.isBlank(annot.getCioId())) {
            log.error("Missing CIO ID in annotation {}", annot);
            this.incorrectFormat.add(annot);
            allGood = false;
        } else if (!(annot instanceof CuratorAnnotationBean) && 
                !annot.getCioId().trim().equals(annot.getCioId())) {
            //fields in all annotations but curator annotations should have 
            //been trimmed
            log.error("CIO ID not trimmed in annotation {}", annot);
            this.incorrectFormat.add(annot);
            allGood = false;
        }
        
        //*** information optional in curator annotations, mandatory in all others ***
        if (!(annot instanceof CuratorAnnotationBean)) {
            //Uberon IDs should be ordered alphabetically
            if (annot.getEntityIds() != null) {
                List<String> orderedEntityIds = SimilarityAnnotationUtils.trimAndSort(annot.getEntityIds());
                if (!orderedEntityIds.equals(annot.getEntityIds())) {
                    log.error("Entity IDs are not ordered or not trimmed in annotation {}", 
                            annot);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                }
            }
            if (StringUtils.isBlank(annot.getTaxonName())) {
                log.error("Missing taxon name in annotation {}", annot);
                this.incorrectFormat.add(annot);
                allGood = false;
            } else if (!annot.getTaxonName().trim().equals(annot.getTaxonName())) {
                //fields in all annotations but curator annotations should have 
                //been trimmed
                log.error("Taxon name not trimmed in annotation {}", annot);
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
                        continue;
                    }
                    if (!uberonName.trim().equals(uberonName)) {
                        //fields in all annotations but curator annotations should have 
                        //been trimmed
                        log.error("Uberon name not trimmed in annotation {}", annot);
                        this.incorrectFormat.add(annot);
                        allGood = false;
                    }
                }
            }
            if (missingUberonName) {
                log.error("Missing Uberon name in annotation {}", annot);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
            if (StringUtils.isBlank(annot.getHomLabel())) {
                log.error("Missing HOM name in annotation {}", annot);
                this.incorrectFormat.add(annot);
                allGood = false;
            } else if (!annot.getHomLabel().trim().equals(annot.getHomLabel())) {
                //fields in all annotations but curator annotations should have 
                //been trimmed
                log.error("HOM label not trimmed in annotation {}", annot);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
            if (StringUtils.isBlank(annot.getCioLabel())) {
                log.error("Missing CIO name in annotation {}", annot);
                this.incorrectFormat.add(annot);
                allGood = false;
            } else if (!annot.getCioLabel().trim().equals(annot.getCioLabel())) {
                //fields in all annotations but curator annotations should have 
                //been trimmed
                log.error("CIO label not trimmed in annotation {}", annot);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
        } 
        
        //*** information mandatory in both RAW and curator annotations ***
        if (annot instanceof RawAnnotationBean) {
    
            if (StringUtils.isBlank(((RawAnnotationBean) annot).getEcoId())) {
                log.error("Missing ECO ID in annotation {}", annot);
                this.incorrectFormat.add(annot);
                allGood = false;
            } else if (!(annot instanceof CuratorAnnotationBean) && 
                    !((RawAnnotationBean) annot).getEcoId().trim().equals(
                            ((RawAnnotationBean) annot).getEcoId())) {
                //fields in all annotations but curator annotations should have 
                //been trimmed
                log.error("ECO ID not trimmed in annotation {}", annot);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
            if (StringUtils.isBlank(((RawAnnotationBean) annot).getAssignedBy())) {
                log.error("Missing assigned by info in annotation {}", annot);
                this.incorrectFormat.add(annot);
                allGood = false;
            } else if (!(annot instanceof CuratorAnnotationBean) && 
                    !((RawAnnotationBean) annot).getAssignedBy().trim().equals(
                            ((RawAnnotationBean) annot).getAssignedBy())) {
                //fields in all annotations but curator annotations should have 
                //been trimmed
                log.error("Assigned by not trimmed in annotation {}", annot);
                this.incorrectFormat.add(annot);
                allGood = false;
            }

            //these fields are not mandatory in case of automatic annotations
            if (!AUTOMATIC_IMPORT_ECO.equals(((RawAnnotationBean) annot).getEcoId()) && 
                !AUTOMATIC_ASSERTION_ECO.equals(((RawAnnotationBean) annot).getEcoId())) {

                if (((RawAnnotationBean) annot).getCurationDate() == null) {
                    log.error("Missing date in annotation {}", annot);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                }
                if (StringUtils.isBlank(((RawAnnotationBean) annot).getCurator())) {
                    log.error("Missing curator info in annotation {}", annot);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                } else if (!(annot instanceof CuratorAnnotationBean) && 
                        !((RawAnnotationBean) annot).getCurator().trim().equals(
                                ((RawAnnotationBean) annot).getCurator())) {
                    //fields in all annotations but curator annotations should have 
                    //been trimmed
                    log.error("Curator not trimmed in annotation {}", annot);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                }
                String refId = ((RawAnnotationBean) annot).getRefId();
                if (StringUtils.isBlank(refId) || !refId.matches("\\S+?:\\S+")) {
                    log.error("Incorrect reference ID {} in annotation {}", refId, annot);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                } else if (!(annot instanceof CuratorAnnotationBean) && 
                        !(refId.trim().equals(refId))) {
                    //fields in all annotations but curator annotations should have 
                    //been trimmed
                    log.error("Ref ID not trimmed in annotation {}", annot);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                }
                String refTitle = ((RawAnnotationBean) annot).getRefTitle();
                if (StringUtils.isBlank(refTitle)) {
                    log.error("Missing reference title in annotation {}", annot);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                } else if (!(annot instanceof CuratorAnnotationBean) && 
                        !(refTitle.trim().equals(refTitle))) {
                    //fields in all annotations but curator annotations should have 
                    //been trimmed
                    log.error("Ref title not trimmed in annotation {}", annot);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                }
            }
        }
        
        //*** information mandatory only in RAW annotations 
        //    (not for curator annotations, this is why we use a Class.equals) ***
        if (annot.getClass().equals(RawAnnotationBean.class)) {
            String ecoName = ((RawAnnotationBean) annot).getEcoLabel();
            if (StringUtils.isBlank(ecoName)) {
                log.error("Missing ECO name in annotation {}", annot);
                this.incorrectFormat.add(annot);
                allGood = false;
            } else if (!(annot instanceof CuratorAnnotationBean) && 
                    !((RawAnnotationBean) annot).getEcoLabel().trim().equals(
                            ((RawAnnotationBean) annot).getEcoLabel())) {
                //fields in all annotations but curator annotations should have 
                //been trimmed
                log.error("ECO label not trimmed in annotation {}", annot);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
        }
        
        //*** information mandatory only in SUMMARY annotations ***
        if (annot instanceof SummaryAnnotationBean) {
            if (((SummaryAnnotationBean) annot).getUnderlyingAnnotCount() <= 0) {
                log.error("Missing underlying annotation count in annotation {}", annot);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
        }
        
        
        
        //*******************************************
        // Check validity of information provided
        //*******************************************
        //at this point, we do not check for presence of mandatory information, 
        //already done above.
        
        //check for existence of taxon and Uberon IDs, and correct use of Uberon labels
        int taxonId = annot.getNcbiTaxonId();
        if (taxonId > 0) {
            OWLClass cls = taxOntWrapper.getOWLClassByIdentifier(
                    OntologyUtils.getTaxOntologyId(taxonId), true);
            if ((taxonIds != null && !taxonIds.contains(taxonId)) || 
                    (cls == null || 
                            taxOntWrapper.isObsolete(cls) || taxOntWrapper.getIsObsolete(cls))) {
                log.error("Unrecognized taxon ID {} in annotation {}", taxonId, annot);
                this.missingTaxonIds.add(taxonId);
                allGood = false;
            } else if (StringUtils.isNotBlank(annot.getTaxonName()) && 
                    !taxOntWrapper.getLabel(cls).equals(annot.getTaxonName())) {
                log.error("Incorrect taxon name in annotation {}", annot);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
        }
        for (int i = 0; i < annot.getEntityIds().size(); i++) {
            String uberonId = annot.getEntityIds().get(i);
            
            if (StringUtils.isBlank(uberonId)) {
                continue;
            }
            uberonId = uberonId.trim();
            OWLClass cls = uberonOntWrapper.getOWLClassByIdentifier(uberonId, true);
            if (cls == null || 
                    uberonOntWrapper.isObsolete(cls) || uberonOntWrapper.getIsObsolete(cls)) {
                log.trace("Unrecognized Uberon ID {} in annotation {}", uberonId, annot);
                this.missingUberonIds.add(uberonId);
                allGood = false;
            } else if (annot.getEntityNames() != null) {
                //check that we have the proper Uberon name in the correct order
                if (!uberonOntWrapper.getLabel(cls).equals(annot.getEntityNames().get(i))) {
                    log.error("Incorrect entity label or label order in annotation {}", annot);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                }
            }
            if (taxonConstraints != null) {
                Set<Integer> existsIntaxa = taxonConstraints.get(uberonId);
                if (existsIntaxa == null) {
                    log.error("Unrecognized Uberon ID {} in annotation {}", uberonId, annot);
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
        }
        
        String homId = annot.getHomId();
        if (StringUtils.isNotBlank(homId)) {
            OWLClass cls = homOntWrapper.getOWLClassByIdentifier(homId.trim(), true);
            if (cls == null || 
                    homOntWrapper.isObsolete(cls) || homOntWrapper.getIsObsolete(cls)) {
                log.trace("Unrecognized HOM ID {} in annotation {}", homId, annot);
                this.missingHOMIds.add(homId);
                allGood = false;
            } else if (StringUtils.isNotBlank(annot.getHomLabel()) && 
                    !homOntWrapper.getLabel(cls).equals(annot.getHomLabel())) {
                log.error("Incorrect HOM label in annotation {}", annot);
                this.incorrectFormat.add(annot);
                allGood = false;
            }
        }
        String confId = annot.getCioId();
        if (StringUtils.isNotBlank(confId)) {
            OWLGraphWrapper cioGraphWrapper = cioWrapper.getOWLGraphWrapper();
            OWLClass cls = cioGraphWrapper.getOWLClassByIdentifier(confId.trim(), true);
            if (cls == null || 
                    cioGraphWrapper.isObsolete(cls) || cioGraphWrapper.getIsObsolete(cls)) {
                log.trace("Unrecognized CONF ID {} in annotation {}", confId, annot);
                this.missingCONFIds.add(confId);
                allGood = false;
            } else {
                if (StringUtils.isNotBlank(annot.getCioLabel()) && 
                        !cioGraphWrapper.getLabel(cls).equals(annot.getCioLabel())) {
                    log.error("Incorrect CIO label in annotation {}", annot);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                }
                if (!cioWrapper.isStronglyConflicting(cls) && 
                        !cioWrapper.hasLeafConfidenceLevel(cls)) {
                    log.error("The CIO term used does not provide any confidence level, nor it is a strongly conflciting statement, in annotation {}", 
                            confId, annot);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                }
                if (annot instanceof CuratorAnnotationBean || annot instanceof RawAnnotationBean) {
                    if (!cioWrapper.isSingleEvidenceConcordance(
                            cioWrapper.getEvidenceConcordance(cls))) {
                        log.error("A RAW annotation uses a confidence statement not from the single evidence branch {} in annotation {}", 
                                confId, annot);
                        this.incorrectFormat.add(annot);
                        allGood = false;
                    }
                }
                if (annot instanceof SummaryAnnotationBean) {
                    if (cioWrapper.isBgeeNotTrustedStatement(cls) && 
                            ((SummaryAnnotationBean) annot).isTrusted()) {
                        log.error("Inconsistent trust state for CIO statement {} in annotation {}", 
                                confId, annot);
                        this.incorrectFormat.add(annot);
                        allGood = false;
                    }
                }
                if (annot instanceof AncestralTaxaAnnotationBean && 
                        cioWrapper.isBgeeNotTrustedStatement(cls)) {
                    log.error("Only trusted annotations should be used for ancestral taxon file in annotation {}", 
                            annot);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                }
            }
        }
        if (annot instanceof RawAnnotationBean){
            String ecoId = ((RawAnnotationBean) annot).getEcoId();
            if (StringUtils.isNotBlank(ecoId)) {
                OWLClass cls = ecoOntWrapper.getOWLClassByIdentifier(ecoId.trim(), true);
                if (cls == null || 
                        ecoOntWrapper.isObsolete(cls) || ecoOntWrapper.getIsObsolete(cls)) {
                    log.error("Unrecognized ECO ID {} in annotation {}", ecoId, annot);
                    this.missingECOIds.add(ecoId);
                    allGood = false;
                } else if (StringUtils.isNotBlank(((RawAnnotationBean) annot).getEcoLabel()) && 
                        !ecoOntWrapper.getLabel(cls).equals(
                                ((RawAnnotationBean) annot).getEcoLabel())) {
                    log.error("Incorrect ECO label in annotation {}", annot);
                    this.incorrectFormat.add(annot);
                    allGood = false;
                }
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
     * Identify negative annotations with potentially missing positive annotations: 
     * if there is a negative annotation in a taxon, 
     * most likely there should be positive annotations in some sub-taxa 
     * (if it was worth providing the NOT annotation, it means some structures in some sub-taxa 
     * have a similarity relation, some of them being true homology). Or, a NOT annotation 
     * could be used to capture a controversy, in which case there should be a corresponding 
     * positive annotation for the same taxon.
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
     * @param warnIfUnique          A {@code boolean} defining whether warnings should be generated 
     *                              if a negative annotation has no corresponding positive annotation 
     *                              in same taxon and in any sub-taxa (if {@code false}), 
     *                              or when a negative annotation has corresponding annotations 
     *                              only in one independent leaf sub-taxon (if {@code true}).
     * @return                      A {@code Map} where keys are {@code RawAnnotationBean}s
     *                              capturing HOM ID and Uberon IDs, 
     *                              the associated values being {@code Set}s of {@code Integer}s, 
     *                              representing the ID of taxa of negative annotations, 
     *                              with potentially missing positive annotations in the same taxon 
     *                              or in some sub-taxa.
     */
    //suppress warning because the getAncestors method of owltools uses unparameterized 
    //generic OWLPropertyExpression, so we need to do the same.
    @SuppressWarnings("rawtypes")
    private Map<RawAnnotationBean, Set<Integer>> checkNegativeAnnotsSubTaxa(
            Map<RawAnnotationBean, Set<Integer>> positiveAnnotsToTaxa, 
            Map<RawAnnotationBean, Set<Integer>> negativeAnnotsToTaxa, boolean warnIfUnique) {
        log.entry(positiveAnnotsToTaxa, negativeAnnotsToTaxa, warnIfUnique);
    
        Map<RawAnnotationBean, Set<Integer>> negAnnotsWithMissingPosAnnots = 
                new HashMap<RawAnnotationBean, Set<Integer>>();
        OntologyUtils utils = new OntologyUtils(this.taxOntWrapper);
        
        //first, we filter the negativeAnnotsToTaxa map, to keep only independent leaf taxa 
        //for each annot.
        Map<RawAnnotationBean, Set<Integer>> negAnnotsToFilteredTaxa = 
                new HashMap<>(negativeAnnotsToTaxa);
        negAnnotsToFilteredTaxa.replaceAll((an, ta) -> {
            Set<OWLClass> taxa = ta.stream().map(t -> taxOntWrapper.getOWLClassByIdentifier(
                    OntologyUtils.getTaxOntologyId(t), true)).collect(Collectors.toSet());
            utils.retainLeafClasses(taxa, new HashSet<OWLPropertyExpression>());
            return taxa.stream().map(t -> OntologyUtils.getTaxNcbiId(
                    taxOntWrapper.getIdentifier(t))).collect(Collectors.toSet());
        });
        
        for (Entry<RawAnnotationBean, Set<Integer>> negativeAnnot: 
            negAnnotsToFilteredTaxa.entrySet()) {
            //the key should represent the concatenation of HOM ID and Uberon IDs, 
            //that were associated to a negative annotation
            RawAnnotationBean key = negativeAnnot.getKey();
            //if there are positive annotations for the same structure, check that it corresponds 
            //to same taxon or to some sub-taxa. 
            
            //First, we retrieve taxa associated to corresponding positive annotations.
            //store in a new HashSet, as we will modify it
            final Set<Integer> positiveTaxIds = positiveAnnotsToTaxa.get(key);

            //check each taxon of the negative annot
            for (int negativeTaxonId: negativeAnnot.getValue()) {
                //if the negative annotation has a corresponding positive annotation 
                //to same taxon, do nothing, this is used to capture conflicting information
                if (positiveTaxIds.contains(negativeTaxonId)) {
                    continue;
                }
                
                //now, we get the descendants of the taxon used in the neg. annot., 
                //and the taxon annotated itself.
                OWLClass taxCls = taxOntWrapper.getOWLClassByIdentifier(
                        OntologyUtils.getTaxOntologyId(negativeTaxonId), true);
                
                Set<Integer> negativeSubTaxa = taxOntWrapper.getDescendantsThroughIsA(taxCls)
                        .stream()
                        .map(t -> OntologyUtils.getTaxNcbiId(taxOntWrapper.getIdentifier(t)))
                        .collect(Collectors.toSet());
                
                //if !warnIfUnique, check whether we have no positive annotation in any sub-taxon.
                //if warnIfUnique == true, generates a warning only if a NOT annotation 
                //has a single corresponding positive annotation, only in one leaf sub-taxon.
                boolean toWarn = false;
                if (!warnIfUnique && 
                        Collections.disjoint(positiveTaxIds, negativeSubTaxa)) {
                    //no taxa in common, the negative annotation is missing a positive annot 
                    //in some sub-taxa. 
                    toWarn = true;
                    
                } else if (warnIfUnique) {
                    //now, generates a warning only if a NOT annotation has a single 
                    //corresponding positive annotation, only in one leaf sub-taxon.
                    Set<OWLClass> leafTaxa = negativeSubTaxa.stream()
                            .filter(positiveTaxIds::contains)
                            .map(t -> taxOntWrapper.getOWLClassByIdentifier(
                                    OntologyUtils.getTaxOntologyId(t), true))
                            .collect(Collectors.toSet());
                    utils.retainLeafClasses(leafTaxa, new HashSet<OWLPropertyExpression>());
                    
                    toWarn = leafTaxa.size() == 1;
                }
                if (toWarn) {   
                    Set<Integer> storeTaxa = negAnnotsWithMissingPosAnnots.get(key);
                    if (storeTaxa == null) {
                        storeTaxa = new HashSet<Integer>();
                        negAnnotsWithMissingPosAnnots.put(key, storeTaxa);
                    }
                    
                    storeTaxa.add(negativeTaxonId);
                }
            }
        }
        
        return log.exit(negAnnotsWithMissingPosAnnots);
    }

    /**
     * Checks that no errors were detected and stored, in {@link #missingUberonIds}, 
     * and/or {@link #missingTaxonIds}, and/or {@link #missingECOIds}, and/or 
     * {@link #missingHOMIds}, and/or {@link #missingCONFIds}, and/or 
     * {@link #idsNotExistingInTaxa}, and/or {@link #duplicates}. If some errors were stored, an 
     * {@code IllegalStateException} will be thrown with a detailed error message, 
     * otherwise, nothing happens.
     * <p>
     * Errors are always reinit after a call to this method in any case.
     * 
     * @throws IllegalStateException    if some errors were detected and stored.
     */
    private void verifyErrors() throws IllegalStateException {
        log.entry();
        
        try {
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
        } finally {
            this.reinitErrors();
        }
        
        log.exit();
    }
    
    /**
     * Reinit the errors stored following a call to {@link verifyErrors()}.
     */
    private void reinitErrors() {
        log.entry();
        this.missingUberonIds.clear();
        this.missingTaxonIds.clear();
        this.missingECOIds.clear();
        this.missingHOMIds.clear();
        this.missingCONFIds.clear();
        this.duplicates.clear();
        this.incorrectFormat.clear();
        this.idsNotExistingInTaxa.clear();
        log.exit();
    }

    /**
     * Generate {@code RawAnnotationBean}s from the provided {@code CuratorAnnotationBean}s. 
     * This method takes annotations from curators, and transform them into clean RAW 
     * annotations. Notably, this method will: i) verify the validity of the provided 
     * annotations (see {@link #checkAnnotations(Collection)}); ii) infer new annotations 
     * (see {@link #generateInferredAnnotations(Collection)}); iii) create 
     * {@code RawAnnotationBean}s with correct label information and ordered entity IDs; 
     * iv) check the validity of the {@code RawAnnotationBean}s generated; v) sort these 
     * {@code RawAnnotationBean}s (see {@link #sortAnnotations(List)}).
     * 
     * @param annots    A {@code Collection} of {@code CuratorAnnotationBean}s, representing 
     *                  annotations from curators, to use to generate derived 
     *                  {@code RawAnnotationBean}s.
     * @return          A {@code List} of {@code RawAnnotationBean}s derived from {@code annots}, 
     *                  sorted.
     * @throws IllegalArgumentException If some errors were detected in the provided, 
     *                                  or in the generated annotations.
     * @throws IllegalStateException    If the ontologies provided at instantiation 
     *                                  did not allow to retrieve some required information.
     */
    public List<RawAnnotationBean> generateRawAnnotations(
            Collection<CuratorAnnotationBean> annots) throws IllegalArgumentException, 
            IllegalStateException {
        log.entry(annots);

        this.checkAnnotations(annots, false);
        //store annots in a new collection to not modify the collection passed as argument
        Set<CuratorAnnotationBean> filteredAnnots = new HashSet<CuratorAnnotationBean>(annots);
        //infer new annotations
        Set<CuratorAnnotationBean> inferredAnnots = 
                this.generateInferredAnnotations(filteredAnnots);
        //check the inferred annotations
        this.checkAnnotations(inferredAnnots, false);
        //add to curator annotations
        filteredAnnots.addAll(inferredAnnots);
        
        //Generate RAW annotations
        Set<RawAnnotationBean> rawAnnots = new HashSet<RawAnnotationBean>();
        for (CuratorAnnotationBean curatorAnnot: filteredAnnots) {
            rawAnnots.add(this.createRawAnnotWithExtraInfo(curatorAnnot));
        }
        if (rawAnnots.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The provided annotations " +
                    "did not allow to generate any RAW annotations."));
        }
        
        //check and sort annotations generated 
        this.checkAnnotations(rawAnnots, false);
        List<RawAnnotationBean> sortedRawAnnots = new ArrayList<RawAnnotationBean>(rawAnnots);
        Collections.sort(sortedRawAnnots, SimilarityAnnotationUtils.ANNOTATION_BEAN_COMPARATOR);
        
        return log.exit(sortedRawAnnots);
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
        
        List<String> entityIds = SimilarityAnnotationUtils.trimAndSort(annot.getEntityIds());
        //store Uberon IDs 
        rawAnnot.setEntityIds(entityIds);
        
        //*** Add labels ***
        
        //get the corresponding names
        List<String> uberonNames = new ArrayList<String>();
        for (String uberonId: entityIds) {
            //it is the responsibility of the checkAnnotation method to make sure 
            //the Uberon IDs exist, so we accept null values, it's not our job here.
            if (uberonId == null) {
                continue;
            }
            OWLClass cls = uberonOntWrapper.getOWLClassByIdentifier(uberonId, true);
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
        //store Uberon names
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
            rawAnnot.setCurator(null);
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

    /**
     * Generate inferred annotations. These annotations are inferred notably using information 
     * from the Uberon ontology provided at instantiation: 
     * <ul>
     * <li>Inferring new annotations from transformation_of relations: this method will examined 
     * transformation_of relations, in the Uberon ontology provided at instantiation, 
     * outgoing from or incoming to annotated classes, to generate new annotations 
     * for precursor or degeneration of entities annotated. This will also try 
     * to make inferences for multiple entities annotations.
     * <li>Inferring new annotations fron OWL class logical constraints: this method 
     * will examine OWL class in the Uberon ontology provided at instantiation, 
     * to identify classes defined as the intersection of annotated classes. 
     * This methods will generate all possible annotations, positive and negative, 
     * for all possible taxa, based on the annotations to intersect classes. It will also 
     * infer multiple entities annotations, to infer, for instance, that if skin is homologous, 
     * and limb is homologous to fin, then skin of limb is homologous to skin of fin.
     * (For now, this method only uses annotations to the concept of historical homology, 
     * see {@link #HISTORICAL_HOMOLOGY_ID}).
     * </ul>
     * 
     * @param annots    A {@code Collection} of {@code CuratorAnnotationBean}s 
     *                  that are the annotations to used to infer new annotations.
     * @return          A {@code Set} of {@code CuratorAnnotationBean}s that are 
     *                  the new annotations inferred.
     * @throws IllegalStateException    If the ontologies provided at instantiation 
     *                                  did not allow to retrieve some required information.
     */
    public Set<CuratorAnnotationBean> generateInferredAnnotations(
            Collection<CuratorAnnotationBean> annots) throws IllegalStateException {
        log.entry(annots);
        
        Set<CuratorAnnotationBean> inferredAnnots = 
                this.inferAnnotationsFromTransformationOf(annots);
        inferredAnnots.addAll(this.inferAnnotationsFromLogicalConstraints(annots));
        
        return log.exit(inferredAnnots);
    }
    
    /**
     * Infer new annotations from transformation_of relations. This method will examined 
     * transformation_of relations, in the Uberon ontology provided at instantiation, 
     * outgoing from or incoming to annotated classes, to generate new annotations 
     * for precursor or degeneration of entities annotated. This will also try 
     * to make the inference for multiple entities annotations.
     * 
     * @param annots    A {@code Collection} of {@code CuratorAnnotationBean}s 
     *                  that are the annotations to used to infer new annotations.
     * @return          A {@code Set} of {@code CuratorAnnotationBean}s that are 
     *                  the new annotations inferred from transformation_of relations.
     * @throws IllegalStateException    If the Uberon ontology provided at instantiation 
     *                                  does not contain a transformation_of relation type.
     */
    private Set<CuratorAnnotationBean> inferAnnotationsFromTransformationOf(
            Collection<CuratorAnnotationBean> annots) throws IllegalStateException {
        log.entry(annots);
        
        //first, we filter the annotations.
        //we discard annotations with a REJECTED confidence statement
        Set<CuratorAnnotationBean> filteredAnnots = this.filterRejectedAnnotations(annots);
        
        log.info("Inferring annotations based on transformation_of relations...");
        //first, we store HOM ID - Uberon IDs to be able to determine when an annotation 
        //already exists for some structures, independently of the taxon
        Set<CuratorAnnotationBean> existingAnnots = this.getExistingAnnots(filteredAnnots);

        //get the transformation_of Object Property, and its sub-object properties
        OntologyUtils uberonUtils = new OntologyUtils(uberonOntWrapper);
        Set<OWLObjectPropertyExpression> transfOfRels = uberonUtils.getTransformationOfProps();
        if (transfOfRels.isEmpty()) {
            throw log.throwing(new IllegalStateException("The Uberon ontology provided " +
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
        for (CuratorAnnotationBean annot: filteredAnnots) {
            log.trace("Inferring annotations from transformation_of relations from annotation: {}", 
                    annot);
            //infer from transformation_of outgoing edges
            Set<CuratorAnnotationBean> newAnnots = 
                    this.createInferredAnnotationsFromTransformationOf(
                            annot, true, existingAnnots, transfOfRels);
            log.trace("Annotations inferred from outgoing transformation_of relations: {}", 
                    newAnnots);
            Set<CuratorAnnotationBean> incomingEdgeAnnots = 
                    this.createInferredAnnotationsFromTransformationOf(
                    annot, false, existingAnnots, transfOfRels);
            log.trace("Annotations inferred from incoming transformation_of relations: {}", 
                    incomingEdgeAnnots);
            //infer from transformation_of incoming edges
            newAnnots.addAll(incomingEdgeAnnots);
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
            log.trace("Inferred annot and source annots: {}", inferredAnnot);
            
            //retrieve from source annotations CIO statements and information about Entity IDs.
            Set<OWLClass> cioStatements = new HashSet<OWLClass>();
            List<String> supportingTextElements = new ArrayList<String>();
            for (CuratorAnnotationBean sourceAnnot: inferredAnnot.getValue()) {
                
                cioStatements.add(cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                        sourceAnnot.getCioId().trim(), true));
                
                List<String> entityIds = SimilarityAnnotationUtils.trimAndSort(sourceAnnot.getEntityIds());
                String supportingTextElement = Utils.formatMultipleValuesToString(entityIds);
                log.trace("Adding supporting text element: {}", supportingTextElement);
                supportingTextElements.add(supportingTextElement);
            }
            Collections.sort(supportingTextElements);
            
            //retrieve best CIO statement, to set the confidence in inferred annotation.
            inferredAnnot.getKey().setCioId(cioWrapper.getOWLGraphWrapper().getIdentifier(
                    cioWrapper.getBestTermWithConfidenceLevel(cioStatements)));
            
            //generate supporting text providing information about the source annotations.
            String supportingText = "Annotation inferred from transformation_of relations "
                    + "using annotations to same HOM ID, "
                    + "same NCBI taxon ID, same qualifier, and Entity IDs equal to: ";
            boolean firstIteration = true;
            for (String supportingTextElement: supportingTextElements) {
                if (!firstIteration) {
                    supportingText += " - ";
                }
                supportingText += supportingTextElement;
                firstIteration = false;
            }
            inferredAnnot.getKey().setSupportingText(supportingText);
        }
        
        log.info("Done inferring annotations based on transformation_of relations, {} annotations inferred.", 
                inferredAnnots.size());
        //the keyset is unmodifiable, wrap it into a new HashSet
        return log.exit(new HashSet<CuratorAnnotationBean>(inferredAnnots.keySet()));
    }
    
    /**
     * Filter the provided {@code Collection} of elements of type {@code AnnotationBean}. 
     * Duplicates will be removed as the returned value is a {@code Set}, and annotations 
     * using a REJECTED confidence statement will be discarded. 
     *  
     * @param annots    A {@code Collection} of elements of type {@code AnnotationBean} 
     *                  to be filtered.
     * @return          A {@code Set} containing the elements in {@code annots}, minus 
     *                  the annotations using a REJECTED confidence statement.
     * @throws IllegalArgumentException     If the CIO ID of one of the provided annotation 
     *                                      (see method {@code getCioId}) is {@code null}, 
     *                                      or not found in the CIO ontology provided 
     *                                      at instantiation.
     */
    private <T extends AnnotationBean> Set<T> filterRejectedAnnotations(Collection<T> annots) 
        throws IllegalArgumentException{
        log.entry(annots);
        
        Set<T> filteredAnnots = new HashSet<T>();
        for (T annot: annots) {
            if (annot.getCioId() == null) {
                throw log.throwing(new IllegalArgumentException("All provided annotations "
                        + "must have a CIO ID defined. Offending annotation: " + annot));
            }
            OWLClass cls = cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                    annot.getCioId().trim());
            if (cls == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized CIO ID "
                        + "in annotation: " + annot));
            }
            if (!cioWrapper.isRejectedStatement(cls)) {
                filteredAnnots.add(annot);
            }
        }
        
        return log.exit(filteredAnnots);
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
                uberonId = uberonId.trim();
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
                transfOfEntityIds.add(entry.getValue().iterator().next().trim());
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
            toCompare.setHomId(annotToInfer.getHomId().trim());
            toCompare.setEntityIds(transfOfEntityIds);
            if (existingAnnots.contains(toCompare)) {
                log.trace("Propagated annotation already exists, not added.");
                return log.exit(inferredAnnots);
            }
            
            //OK, create the inferred annotation
            CuratorAnnotationBean inferredAnnot = new CuratorAnnotationBean();
            inferredAnnot.setHomId(annotToInfer.getHomId().trim());
            inferredAnnot.setEntityIds(transfOfEntityIds);
            inferredAnnot.setNcbiTaxonId(annotToInfer.getNcbiTaxonId());
            inferredAnnot.setNegated(annotToInfer.isNegated());
            inferredAnnot.setEcoId(AUTOMATIC_ASSERTION_ECO);
            inferredAnnot.setAssignedBy(AUTOMATIC_ASSIGNED_BY);
            inferredAnnot.setCurationDate(null);
            
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

    /**
     * Generate a {@code Set} of {@code CuratorAnnotationBean}s derived from {@code annots} 
     * and allowing to determine which entities are already annotated, discarding 
     * annotations using the REJECTED CIO statement. The returned 
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
            //discard annotations with rejected CIO statement
            if (annot.getCioId() != null) {
                OWLClass cls = cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                        annot.getCioId().trim(), true);
                if (cls != null && cioWrapper.isRejectedStatement(cls)) {
                    continue;
                }
            }
            CuratorAnnotationBean existingAnnot = new CuratorAnnotationBean();
            existingAnnot.setHomId(annot.getHomId().trim());
            List<String> entityIds = SimilarityAnnotationUtils.trimAndSort(annot.getEntityIds());
            existingAnnot.setEntityIds(entityIds);
            
            existingAnnots.add(existingAnnot);
        }
        
        return log.exit(existingAnnots);
    }

    /**
     * Infer new annotations fron OWL class logical constraints. This method 
     * will examine OWL class in the Uberon ontology provided at instantiation, 
     * to identify classes defined as the intersection of annotated classes. 
     * This methods will generate all possible annotations, positive and negative, 
     * for all possible taxa, based on the annotations to intersect classes. It will also 
     * infer multiple entities annotations, to infer, for instance, that if skin is homologous, 
     * and limb is homologous to fin, then skin of limb is homologous to skin of fin.
     * <p>
     * For now, this method only uses annotations to the concept of historical homology 
     * (see {@link #HISTORICAL_HOMOLOGY_ID}).
     * 
     * @param annots    A {@code Collection} of {@code CuratorAnnotationBean}s 
     *                  that are the annotations to used to infer new annotations.
     * @return          A {@code Set} of {@code CuratorAnnotationBean}s that are 
     *                  the new annotations inferred from logical constraints.
     */
    private Set<CuratorAnnotationBean> inferAnnotationsFromLogicalConstraints(
            Collection<CuratorAnnotationBean> annots) {
        log.entry(annots);
        log.info("Inferring annotations based on logical constraints...");
        

        //first, we filter the annotations.
        //XXX: this method currently uses only annotations of historical homology, 
        //this should be reconsidered if we used other HOM concepts.
        Set<CuratorAnnotationBean> filteredAnnots = new HashSet<CuratorAnnotationBean>();
        for (CuratorAnnotationBean annot: annots) {
            if (HISTORICAL_HOMOLOGY_ID.equals(annot.getHomId().trim())) {
                filteredAnnots.add(annot);
            }
        }
        //we discard annotations with a REJECTED confidence statement
        filteredAnnots = this.filterRejectedAnnotations(filteredAnnots);
        
        //we create a Map where keys are Uberon IDs used in annotations, 
        //the associated values being the annotations using these Uberon IDs; 
        //this will allow faster inferences.
        Map<String, Set<CuratorAnnotationBean>> entityIdToAnnots = 
                this.getEntityToAnnotsMapping(filteredAnnots);
        //We also need a mapping of the taxon IDs used to the IDs of their ancestors 
        //(and to their own ID for reflexivity)
        Map<Integer, Set<Integer>> taxToSelfAndAncestors = 
                this.getTaxonToSelfAndAncestorMapping(filteredAnnots);
        
        //Now, we search for OWL classes defined as the intersection of annotated entities, 
        //and that are not themselves already annotated.
        //we will store in a Map the IDs of such OWL classes as keys, the associated value 
        //being the IDs of the entities composing the class. Only classes composed 
        //of entities that are all annotated will be considered.
        Map<String, Set<String>> intersectMapping = this.getIntersectionOfMapping(
                entityIdToAnnots);
        
        
        //OK, infer annotations.
        Set<CuratorAnnotationBean> inferredAnnots = new HashSet<CuratorAnnotationBean>();
        
        for (Entry<String, Set<String>> intersectEntry: intersectMapping.entrySet()) {
            log.trace("Try to infer annotations for class {} based on intersecting classes {}", 
                    intersectEntry.getKey(), intersectEntry.getValue());
            
            //first we retrieve the taxa used in the related annotations.
            Set<Integer> taxIds = new HashSet<Integer>();
            for (String intersectClsId: intersectEntry.getValue()) {
                for (CuratorAnnotationBean relatedAnnot: entityIdToAnnots.get(intersectClsId.trim())) {
                    taxIds.add(relatedAnnot.getNcbiTaxonId());
                }
            }
            log.trace("Taxon IDs used in annotations of intersect classes: {}", taxIds);
            
            //now, for each taxon used in annotations, we check whether we have, 
            //for each intersect class, some annotations valid in the taxon, 
            //or any of its ancestors. This will allow to generate all possible annotations 
            //for all possible taxa, positive or negative.
            taxId: for (int taxId: taxIds) {
                log.trace("Checking potential inferrences for taxon {}", taxId);
                
                //to properly infer the confidence level, we need to store 
                //the source annotations used, grouped independently for each intersect class.
                //This is why we use a Set of Sets.
                //We search for annotations to each intersect class, valid 
                //in the iterated taxon or its ancestors; and for each intersect class, 
                //we only retain annotations to the leaf valid taxon.
                Set<Set<CuratorAnnotationBean>> annotsPerIntersectClass = 
                        this.getAnnotsToIntersectClasses(entityIdToAnnots, 
                                intersectEntry.getValue(), 
                                taxId, taxToSelfAndAncestors.get(taxId));
                if (annotsPerIntersectClass == null) {
                    //no annotation can possibly be inferred for the current taxon iterated, 
                    //try next taxon.
                    continue taxId;
                }
                
                //this new annotation will be used in any case, at least to be used as sead 
                //for the loadEntityIdsToAnnotsPerIntersectClass method. But maybe that, 
                //even if we used it as seed, we won't integrate it into the inferred 
                //annotations. Considered for instance the following example: 
                //skin of pes = (zone of skin AND part_of pes)
                //there are two annotations related to pes: 
                //1) pes|pelvic fin radial bone in Sarcopterygii, and 
                //2) pes in tetrapoda.
                //This leads to generate two annotations for skin of pes: one in tetrapoda, 
                //and one in Sarcopterygii; it is definitely weird to speak about 
                //a skin of pes on Sarcopterygii. So, if for an intersect class, 
                //there are both annotations with single-entity and multiple-entities, 
                //only the single-entity annotation should be taken into account.
                //
                //So, for each itersect class, we check whether we have multiple-entities 
                //annotations used, and whether we have a single-entity annotations 
                //available to a child taxon; in that case, we will discard the new annotation.
                //only positive annotations are discared that way (because negative annotations 
                //already have a taxon-based filtering)
                boolean toDiscard = false;
                for (Set<CuratorAnnotationBean> groupedAnnots: 
                    this.getAllAnnotsToIntersectClasses(entityIdToAnnots, 
                            intersectEntry.getValue())) {
                    //taxon IDs used in multiple-entities positive annotations
                    Set<Integer> multEntTaxa = new HashSet<Integer>();
                    //taxon IDs used in single-entity positive annotations
                    Set<Integer> singleEntTaxa = new HashSet<Integer>();
                    for (CuratorAnnotationBean annotFromGroup: groupedAnnots) {
                        assert annotFromGroup.getEntityIds().size() > 0;
                        if (annotFromGroup.isNegated()) {
                            continue;
                        }
                        if (annotFromGroup.getEntityIds().size() > 1) {
                            multEntTaxa.add(annotFromGroup.getNcbiTaxonId());
                        } else {
                            singleEntTaxa.add(annotFromGroup.getNcbiTaxonId());
                        }
                    }
                    //store IDs of taxon equal to taxId or to any of its ancestor, used 
                    //in single-entity positive annotations
                    Set<Integer> singleEntToSelfOrAncestor = new HashSet<Integer>(singleEntTaxa);
                    singleEntToSelfOrAncestor.retainAll(taxToSelfAndAncestors.get(taxId));
                    //store IDs of taxon equal to taxId or to any of its ancestor, used 
                    //in multiple-entity positive annotations
                    Set<Integer> multEntToSelfOrAncestor = new HashSet<Integer>(multEntTaxa);
                    multEntToSelfOrAncestor.retainAll(taxToSelfAndAncestors.get(taxId));
                    //store IDs of taxa children of taxId, used in single-entity positive annotations
                    Set<Integer> singleEntToChild = new HashSet<Integer>(singleEntTaxa);
                    for (int singleEntTaxon: singleEntTaxa) {
                        if (singleEntTaxon != taxId && 
                                taxToSelfAndAncestors.get(singleEntTaxon).contains(taxId)) {
                            singleEntToChild.add(singleEntTaxon);
                        }
                    }
                    //if we have no single-entity positive annotation for taxId or its ancestors, 
                    //and we're using a multiple-entities positive annotation to taxId or its ancestors, 
                    //while there exist single-entity positive annotation to children of taxId: 
                    //do not store annotation (but use it as seed for further inferrences)
                    if (singleEntToSelfOrAncestor.isEmpty() && !multEntToSelfOrAncestor.isEmpty() && 
                            !singleEntToChild.isEmpty()) {
                        log.trace("New annotation for class {} and taxon {} will be used to infer multiple-entities annotations, but will not be added: {}", 
                                intersectEntry.getKey(), taxId);
                        toDiscard = true;
                        break;
                    }
                }
                
                
                //now we try to find classes with same intersect annotations, to be able 
                //to recover, e.g.,  if skin is homologous, and limb is homologous to fin, 
                //then skin of limb is homologous to skin of fin. 
                //We store the IDs of homologous entities (key Set<String>), associated to 
                //their common annotations, grouped per intersecting class (value Set of Sets).
                Map<Set<String>, Set<Set<CuratorAnnotationBean>>> entityIdsToAnnotsPerIntersectClass = 
                        new HashMap<Set<String>, Set<Set<CuratorAnnotationBean>>>();
                //and first, we store the currently iterated entity
                entityIdsToAnnotsPerIntersectClass.put(
                        new HashSet<String>(Arrays.asList(intersectEntry.getKey())), 
                        annotsPerIntersectClass);
                //try to infer new mappings
                this.loadEntityIdsToAnnotsPerIntersectClass(entityIdsToAnnotsPerIntersectClass, 
                        entityIdToAnnots, intersectMapping);
                
                
                //OK, create actual inferred annotations.
                //We'll try to create a positive and a negative annotation for each mapping 
                //we have discovered.
                CuratorAnnotationBean baseInferredAnnot = new CuratorAnnotationBean();
                baseInferredAnnot.setHomId(HISTORICAL_HOMOLOGY_ID);
                baseInferredAnnot.setNcbiTaxonId(taxId);
                baseInferredAnnot.setEcoId(AUTOMATIC_ASSERTION_ECO);
                baseInferredAnnot.setAssignedBy(AUTOMATIC_ASSIGNED_BY);
                baseInferredAnnot.setCurationDate(null);
                
                for (Entry<Set<String>, Set<Set<CuratorAnnotationBean>>> mapping: 
                    entityIdsToAnnotsPerIntersectClass.entrySet()) {
                    List<String> entityIds = SimilarityAnnotationUtils.trimAndSort(mapping.getKey());
                    
                    //here, we retrieve a positive and/or a negative annotation 
                    //(between none and 2 new annotations inferred for this mapping 
                    //in this taxon)
                    for (CuratorAnnotationBean inferredInfo: 
                        this.getInferredInfoFromLogicalConstraints(
                                mapping.getValue())) {
                        if (!inferredInfo.isNegated() && toDiscard && 
                                entityIds.equals(Arrays.asList(intersectEntry.getKey()))) {
                            continue;
                        }
                        CuratorAnnotationBean newAnnot = 
                                new CuratorAnnotationBean(baseInferredAnnot);
                        newAnnot.setEntityIds(entityIds);
                        newAnnot.setNegated(inferredInfo.isNegated());
                        newAnnot.setCioId(inferredInfo.getCioId());
                        newAnnot.setSupportingText(inferredInfo.getSupportingText());
                        log.trace("New annotation inferred: {}", newAnnot);
                        
                        inferredAnnots.add(newAnnot);
                    }
                }
            }
        }
        
        //now, we filter annotations for which a same annotation with more entities exists
        this.filterInferredAnnotations(inferredAnnots);
        

        log.info("Done inferring annotations based on logical constraints, {} annotations inferred.", 
                inferredAnnots.size());
        return log.exit(inferredAnnots);
    }
    
    /**
     * Filter annotations from {@code annots}. This method removes 
     * annotations for which it exist an annotation with same HOM ID, taxon ID, 
     * negation status, CIO ID, supporting text, that includes all its entity IDs and more.
     * <p>
     * {@code annots} will be modified as a result of the call to this method 
     * (optional operation).
     * 
     * @param annots    A {@code Set} of {@code CuratorAnnotationBean}s to be filtered 
     *                  for redundant annotations. This {@code Set} can be modified 
     *                  as a result of the call to this method.
     */
    private void filterInferredAnnotations(Set<CuratorAnnotationBean> annots) {
        log.entry(annots);
        log.trace("Filtering inferred annotations...");
        
        Set<CuratorAnnotationBean> toRemove = new HashSet<CuratorAnnotationBean>();
        annot: for (CuratorAnnotationBean annot: annots) {
            for (CuratorAnnotationBean annot2: annots) {
                if (annot.equals(annot2) || toRemove.contains(annot2)) {
                    continue;
                }
                if (annot.getHomId().trim().equals(annot2.getHomId().trim()) && 
                        annot.isNegated() == annot2.isNegated() && 
                        annot.getNcbiTaxonId() == annot2.getNcbiTaxonId() && 
                        annot.getCioId().trim().equals(annot2.getCioId().trim()) && 
                        annot.getSupportingText().trim().equals(annot2.getSupportingText().trim()) && 
                        annot2.getEntityIds().containsAll(annot.getEntityIds()) && 
                        annot2.getEntityIds().size() >= annot.getEntityIds().size()) {
                    log.trace("Remove annotation {} because redundant as compared to annotation {}", 
                            annot, annot2);
                    toRemove.add(annot);
                    continue annot;
                }     
            }
        }
        annots.removeAll(toRemove);
        
        log.trace("Done filtering inferred annotations, {} annotations removed", 
                toRemove.size());
        log.exit();
    }
    
    /**
     * Try to find new mappings based on already inferred annotations to classes 
     * defined using logical constraints. This method uses the provided already 
     * inferred annotations, and try to find classes with common annotations 
     * for each of their intersect classes, and we the same number of intersecting classes.
     * <p>
     * The aim is to recover, for instance,  if skin is homologous, 
     * and limb is homologous to fin, then skin of limb is homologous to skin of fin. 
     * <p>
     * The provided mapping from entity IDs to annotations, grouped per intersecting class 
     * ({@code entityIdsToAnnotsPerIntersectClass}), will be modified as a result 
     * of the call to this method (optional operation). It stores the IDs of related entities 
     * (key {@code Set<String>}), associated to the supporting annotations, grouped per 
     * intersecting class (value {@code Set of Sets}). It must have been already seeded 
     * with at least one inferred annotation.
     * 
     * @param entityIdsToAnnotsPerIntersectClass    A {@code Map} where keys are {@code Set}s 
     *                                              of {@code String}s storing the IDs of 
     *                                              homologous entities, the associated value 
     *                                              being a {@code Set} of {@code Set}s, 
     *                                              where each {@code Set} contains 
     *                                              {@code CuratorAnnotationBean}s supporting 
     *                                              the annotation, grouped per intersecting class.
     *                                              This {@code Map} can be modified 
     *                                              as a result of the call to this method.
     * @param entityIdToAnnots                      A {@code Map} where keys are IDs of 
     *                                              annotated entities, the associated value 
     *                                              being a {@code Set} storing the 
     *                                              {@code CuratorAnnotationBean}s using 
     *                                              this entity (see 
     *                                              {@link #getEntityToAnnotsMapping(Collection)}).
     * @param intersectMapping                      A {@code Map} where keys are IDs of 
     *                                              entities defined as the intersection 
     *                                              of annotated classes, the associated value 
     *                                              being a {@code Set} storing the IDs 
     *                                              of the classes used in the IntersectionOf 
     *                                              class expression (see 
     *                                              {@link #getIntersectionOfMapping(Map)}).
     */
    private void loadEntityIdsToAnnotsPerIntersectClass(
        Map<Set<String>, Set<Set<CuratorAnnotationBean>>> entityIdsToAnnotsPerIntersectClass,
        Map<String, Set<CuratorAnnotationBean>> entityIdToAnnots, 
        Map<String, Set<String>> intersectMapping) {
        log.entry(entityIdsToAnnotsPerIntersectClass, entityIdToAnnots, intersectMapping);
        
        //try to find a mapping to already inferred annotation for each class 
        //defined using logical constraints.
        for (Entry<String, Set<String>> intersectEntry: intersectMapping.entrySet()) {
            log.trace("Trying to find mapping to already inferred annotation for class {}", 
                    intersectEntry.getKey());
            
            //if class already examined before calling this method, skip.
            if (entityIdsToAnnotsPerIntersectClass.containsKey(
                    new HashSet<String>(Arrays.asList(intersectEntry.getKey())))) {
                log.trace("Already examined, skip.");
                continue;
            }
            
            //try to find a mapping to any already defined inferred annotation
            Map<Set<String>, Set<Set<CuratorAnnotationBean>>> newMappings = 
                    new HashMap<Set<String>, Set<Set<CuratorAnnotationBean>>>();
            //new annotations would have been added at previous iterations, 
            //and will be built up during next iterations
            existingMapping: for (Entry<Set<String>, Set<Set<CuratorAnnotationBean>>> existingMapping: 
                entityIdsToAnnotsPerIntersectClass.entrySet()) {
                log.trace("Test existing inferred annotation: {}", existingMapping);

                //we need to check that the mapping we are going to create is between 
                //entities with at least some different intersect entities, 
                //otherwise it is not valid to create a mapping, e.g.: 
                //left lobe of thyroid gland = (lobe of thyroid gland AND 
                //    in_left_side_of some thyroid gland) 
                //right lobe of thyroid gland = (lobe of thyroid gland AND 
                //    in_right_side_of some thyroid gland) 
                //=> we should not generate an annotation 
                //left lobe of thyroid gland|right lobe of thyroid gland
                for (String alreadyMappedEntityId: existingMapping.getKey()) {
                    Set<String> mappedIntersectIds = new HashSet<String>(
                            intersectMapping.get(alreadyMappedEntityId));
                    Set<String> newIntersectIds = new HashSet<String>(
                            intersectEntry.getValue());
                    //check whether the entity used in the source annotation 
                    //and the entity used in the new annotation have some intersect elements 
                    //not in common for both of them
                    mappedIntersectIds.removeAll(intersectEntry.getValue());
                    newIntersectIds.removeAll(intersectMapping.get(alreadyMappedEntityId));
                    if (mappedIntersectIds.isEmpty() || newIntersectIds.isEmpty()) {
                        log.trace("No annotation with multiple entities to infer, no unique intersect class");
                        continue existingMapping;
                    }
                }
                
                Set<Set<CuratorAnnotationBean>> commonAnnotsPerIntersectClass = 
                        new HashSet<Set<CuratorAnnotationBean>>();
                
                annotGroup: for (Set<CuratorAnnotationBean> existingAnnotsGroup: 
                    existingMapping.getValue()) {
                    Set<String> clsIdsUsed = new HashSet<String>();
                    for (String intersectClassId: intersectEntry.getValue()) {
                        if (clsIdsUsed.contains(intersectClassId)) {
                            continue;
                        }
                        log.trace("Test intersect class {}", intersectClassId);
                        
                        Set<CuratorAnnotationBean> commonAnnots = 
                                new HashSet<CuratorAnnotationBean>();
                        commonAnnots.addAll(existingAnnotsGroup);
                        commonAnnots.retainAll(entityIdToAnnots.get(intersectClassId));
                        if (!commonAnnots.isEmpty()) {
                            log.trace("Common valid annotations found: {}", commonAnnots);
                            commonAnnotsPerIntersectClass.add(commonAnnots);
                            clsIdsUsed.add(intersectClassId);
                            //find valid common annotations for this intersect class, 
                            //try directly next group of annotation.
                            continue annotGroup;
                        }
                    }
                }
                //If we found some common annotations for each intersecting class, 
                //we have a mapping
                if (commonAnnotsPerIntersectClass.size() == existingMapping.getValue().size()) {
                    log.trace("New mapping found from {} to existing mapping {}", 
                            intersectEntry.getKey(), existingMapping.getKey());
                    Set<String> entityIds = new HashSet<String>(existingMapping.getKey());
                    entityIds.add(intersectEntry.getKey());
                    newMappings.put(entityIds, commonAnnotsPerIntersectClass);
                }
            }
            assert newMappings.size() == 0 || Collections.disjoint(newMappings.keySet(), 
                    entityIdsToAnnotsPerIntersectClass.keySet());
            entityIdsToAnnotsPerIntersectClass.putAll(newMappings);
        }
        
        log.exit();
    }
    
    /**
     * Search for annotations to each provided intersect class valid in the provided taxon 
     * or its ancestors. For each intersect class, we will only retain annotations 
     * to the taxon that is the leaf of the valid taxa considered (taxa corresponding to
     * the provided taxon or its ancestors). Retained annotations will be grouped 
     * per intersecting class. 
     * <p>
     * If it is not possible to retrieve annotations valid 
     * for the provided taxon for each intersecting class, this method returns {@code null}.
     * 
     * @param entityIdToAnnots      A {@code Map} where keys are IDs of annotated entities, 
     *                              the associated value being a {@code Set} storing 
     *                              the {@code CuratorAnnotationBean}s using this entity 
     *                              (see {@link #getEntityToAnnotsMapping(Collection)}).
     * @param intersectClassIds     A {@code Set} of {@code String}s that are the IDs of entities 
     *                              with annotations, that are used to define logical constraints 
     *                              of a class through IntersectionOf class expression 
     *                              (see {@link #getIntersectionOfMapping(Map)}).
     * @param taxId                 An {@code int} that is the ID of a taxon currently 
     *                              considered to infer new annotations for a class, 
     *                              using its logical constraints.
     * @param taxAndAncestorsIds    A {@code Set} of {@code Integer}s containing the IDs 
     *                              of the ancestors of the taxon with ID {@code taxId}, 
     *                              as well as {@code taxId} itself (see 
     *                              {@link #getTaxonToSelfAndAncestorMapping(Collection)}).
     * @return                      A {@code Set} of {@code Set}s, where each {@code Set} 
     *                              groups the valid annotations related to one 
     *                              of the intersect classes. So there will be as many 
     *                              {@code Set}s as intersect classes. If it is not possible 
     *                              to retrieve annotations valid for {@code taxId} 
     *                              for each class in {@code intersectClassIds}, 
     *                              this returned value is {@code null}.
     */
    private Set<Set<CuratorAnnotationBean>> getAnnotsToIntersectClasses(
            Map<String, Set<CuratorAnnotationBean>> entityIdToAnnots, 
            Set<String> intersectClassIds, int taxId, Set<Integer> taxAndAncestorsIds) {
        log.entry(entityIdToAnnots, intersectClassIds, taxId, taxAndAncestorsIds);
        
        Set<Set<CuratorAnnotationBean>> annotsPerIntersectClass = 
                new HashSet<Set<CuratorAnnotationBean>>();
        OntologyUtils taxOntUtils = new OntologyUtils(taxOntWrapper);
        
        for (String intersectClsId: intersectClassIds) {
            intersectClsId = intersectClsId.trim();
            log.trace("Checking annotations available from intersect class {} in taxon {}", 
                    intersectClsId, taxId);
            
            //first, retrieve the taxa of annotations to the iterated taxon, 
            //or its ancestors
            Set<OWLClass> intersectTaxCls = new HashSet<OWLClass>();
            for (CuratorAnnotationBean relatedAnnot: 
                entityIdToAnnots.get(intersectClsId)) {
                log.trace("Trying to use taxon of annotation {}", relatedAnnot);
                if (!taxAndAncestorsIds.contains(relatedAnnot.getNcbiTaxonId())) {
                    log.trace("Annotation discarded because not in related taxon: {}", 
                            relatedAnnot);
                    continue;
                }
                intersectTaxCls.add(taxOntWrapper.getOWLClassByIdentifier(
                        OntologyUtils.getTaxOntologyId(relatedAnnot.getNcbiTaxonId())));
            }

            //if no annotations related to the iterated taxon for this intersect class, 
            //no annotation will be inferred for this taxon.
            if (intersectTaxCls.size() == 0) {
                log.trace("No annotation for intersect class {} in related taxa of {}.", 
                        intersectClsId, taxId);
                return log.exit(null);
            }
            
            //now, we will only consider annotations to the leaf valid taxon
            taxOntUtils.retainLeafClasses(intersectTaxCls, null);
            assert intersectTaxCls.size() == 1;
            int leafTaxId = OntologyUtils.getTaxNcbiId(taxOntWrapper.getIdentifier(
                    intersectTaxCls.iterator().next()));
            log.trace("Annotations to intersect class {} and taxon ID {} will be considered", 
                    intersectClsId, leafTaxId);
            
            //OK, now we retrieve annotations for the current intersect class, 
            //annotated to the leaf taxon retained; 
            Set<CuratorAnnotationBean> relatedAnnots = new HashSet<CuratorAnnotationBean>();
            for (CuratorAnnotationBean relatedAnnot: entityIdToAnnots.get(intersectClsId)) {
                if (relatedAnnot.getNcbiTaxonId() == leafTaxId) {
                    log.trace("Annotation considered: {}", relatedAnnot);
                    relatedAnnots.add(relatedAnnot);
                }
            }
            assert !relatedAnnots.isEmpty();
            annotsPerIntersectClass.add(relatedAnnots);
        }

        log.trace("Annotations per intersect classes: {}", annotsPerIntersectClass);
        //if we couldn't find valid annotations for each intersect class, we should 
        //already have returned null in the previous loop.
        assert annotsPerIntersectClass.size() == intersectClassIds.size();
        
        return log.exit(annotsPerIntersectClass);
    }
    
    /**
     * Search for annotations to each provided intersect class. 
     * Annotations are grouped per intersecting class. 
     * 
     * @param entityIdToAnnots      A {@code Map} where keys are IDs of annotated entities, 
     *                              the associated value being a {@code Set} storing 
     *                              the {@code CuratorAnnotationBean}s using this entity 
     *                              (see {@link #getEntityToAnnotsMapping(Collection)}).
     * @param intersectClassIds     A {@code Set} of {@code String}s that are the IDs of entities 
     *                              with annotations, that are used to define logical constraints 
     *                              of a class through IntersectionOf class expression 
     *                              (see {@link #getIntersectionOfMapping(Map)}).
     * @return                      A {@code Set} of {@code Set}s, where each {@code Set} 
     *                              groups the valid annotations related to one 
     *                              of the intersect classes. So there will be as many 
     *                              {@code Set}s as intersect classes. 
     */
    private Set<Set<CuratorAnnotationBean>> getAllAnnotsToIntersectClasses(
            Map<String, Set<CuratorAnnotationBean>> entityIdToAnnots, 
            Set<String> intersectClassIds) {
        log.entry(entityIdToAnnots, intersectClassIds);
        
        Set<Set<CuratorAnnotationBean>> annotsPerIntersectClass = 
                new HashSet<Set<CuratorAnnotationBean>>();
        
        for (String intersectClsId: intersectClassIds) {
            intersectClsId = intersectClsId.trim();
            log.trace("Checking annotations available from intersect class {} in all taxa", 
                    intersectClsId);
            
            Set<CuratorAnnotationBean> relatedAnnots = new HashSet<CuratorAnnotationBean>();
            for (CuratorAnnotationBean relatedAnnot: entityIdToAnnots.get(intersectClsId)) {
                log.trace("Annotation available: {}", relatedAnnot);
                relatedAnnots.add(relatedAnnot);
            }

            annotsPerIntersectClass.add(relatedAnnots);
        }
        log.trace("Annotations per intersect classes: {}", annotsPerIntersectClass);
        assert intersectClassIds.size() == annotsPerIntersectClass.size();
        
        return log.exit(annotsPerIntersectClass);
    }
    
    /**
     * Create a {@code Map} where keys are entity IDs used in annotations, 
     * the associated values being the annotations using these entity IDs; 
     * this will allow faster inferences.
     * 
     * @param annots    A {@code Collection} of {@code CuratorAnnotationBean}s 
     *                  used to generate the mapping from entity IDs to annotations.
     * @return          A {@code Map} where keys are {@code String}s that are the entity IDs 
     *                  used in annotations, the associated value being a {@code Set} 
     *                  containing the {@code CuratorAnnotationBean}s using these entity IDs.
     */
    private Map<String, Set<CuratorAnnotationBean>> getEntityToAnnotsMapping(
            Collection<CuratorAnnotationBean> annots) {
        log.entry(annots);
        Map<String, Set<CuratorAnnotationBean>> entityIdToAnnots = 
                new HashMap<String, Set<CuratorAnnotationBean>>();
        for (CuratorAnnotationBean annot: annots) {
            //Uberon mapping
            for (String entityId: annot.getEntityIds()) {
                entityId = entityId.trim();
                Set<CuratorAnnotationBean> mappedAnnots = entityIdToAnnots.get(entityId);
                if (mappedAnnots == null) {
                    mappedAnnots = new HashSet<CuratorAnnotationBean>();
                    entityIdToAnnots.put(entityId, mappedAnnots);
                }
                mappedAnnots.add(annot);
            }
        }
        return log.exit(entityIdToAnnots);
    }
    
    /**
     * Create a {@code Map} where keys are IDs of taxon used in annotations, 
     * the associated values containing the IDs of their ancestors (and their own IDs, 
     * for reflexivity).
     * 
     * @param annots    A {@code Collection} of {@code CuratorAnnotationBean}s 
     *                  used to generate the mapping from taxon IDs to ancestor and self IDs.
     * @param <T>       The type of {@code AnnotationBean} in {@code annots}.
     * @return          A {@code Map} where keys are {@code Integer}s that are the IDs of taxa 
     *                  used in annotations, the associated value being a {@code Set} 
     *                  of {Integer}s containing the IDs of their ancestors, and their own IDs, 
     *                  for reflexivity.
     */
    private <T extends AnnotationBean> Map<Integer, Set<Integer>> getTaxonToSelfAndAncestorMapping(
            Collection<T> annots) {
        log.entry(annots);
        Map<Integer, Set<Integer>> taxToSelfAndAncestors = new HashMap<Integer, Set<Integer>>();
        for (T annot: annots) {
            log.trace("Retrieving taxon and ancestors from annotation: {}", annot);
            //taxon mapping
            if (!taxToSelfAndAncestors.containsKey(annot.getNcbiTaxonId())) {
                log.trace("Retrieving taxon and ancestors for taxon: {}", 
                        annot.getNcbiTaxonId());
                Set<Integer> selfAndAncestorsIds = new HashSet<Integer>();
                selfAndAncestorsIds.add(annot.getNcbiTaxonId());
                for (OWLClass ancestor: taxOntWrapper.getAncestorsThroughIsA(
                        taxOntWrapper.getOWLClassByIdentifier(
                                OntologyUtils.getTaxOntologyId(annot.getNcbiTaxonId())))) {
                    selfAndAncestorsIds.add(OntologyUtils.getTaxNcbiId(
                            taxOntWrapper.getIdentifier(ancestor)));
                }
                log.trace("Generating mapping for taxon {}: {}", annot.getNcbiTaxonId(), 
                        selfAndAncestorsIds);
                taxToSelfAndAncestors.put(annot.getNcbiTaxonId(), selfAndAncestorsIds);
            }
        }
        return log.exit(taxToSelfAndAncestors);
    }
    
    /**
     * Search for OWL classes defined as the intersection of annotated entities and returns 
     * a {@code Map} with the IDs of such OWL classes as keys and the IDs of the entities 
     * composing the class as the associated value. The classes are searched 
     * in the Uberon ontology provided at instantiation. Only OWL classes 
     * not already annotated, and composed of classes all annotated, will be considered.
     * 
     * @param entityIdToAnnots  A {@code Map} where keys are IDs of annotated entities, 
     *                          the associated value being a {@code Set} storing 
     *                          the {@code CuratorAnnotationBean}s using this entity.
     * @return                  A {@code Map} where keys are IDs of entities defined 
     *                          as the intersection of annotated classes, the associated value 
     *                          being a {@code Set} storing the IDs of the classes 
     *                          used in the IntersectionOf class expression.
     */
    private Map<String, Set<String>> getIntersectionOfMapping(
            Map<String, Set<CuratorAnnotationBean>> entityIdToAnnots) {
        log.entry(entityIdToAnnots);
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
                        Set<String> previousMapping;
                        if ((previousMapping = intersectMapping.put(clsId, intersectClsIds)) 
                                != null && !previousMapping.equals(intersectClsIds)) {
                            log.warn("Class {} defined with several valid IntersectionOf expressions, previous intersection: {} - current intersection: {}", 
                                    clsId, previousMapping, intersectClsIds);
                        }
                    }
                }
            }
        }
        
        log.debug("Done searching for IntersectionOf expressions, {} classes will be considered.", 
                intersectMapping.size());
        return log.exit(intersectMapping);
    }
    
    /**
     * Generate the confidence level and the supporting text of positive and/or negative 
     * annotation inferred from the provided annotations. This method is used when 
     * inferring annotations from logical constraints. {@code annotsPerIntersectClass} 
     * provides annotations grouped per intersect classes used in logical constraints. 
     * <p>
     * The returned {@code Set} will contain at most 2 annotations (in that case, 
     * one positive and one negative). It can contain none if no inferrence was possible. 
     * The returned {@code CuratorAnnotationBean}s 
     * have only their negation status (see method {@code isNegated}), confidence 
     * (see method {@code getCioId}), and supporting text (see method {@code getSupportingText}) 
     * set. 
     * 
     * @param annotsPerIntersectClass   A {@code Set} of {@code Set}s grouping the 
     *                                  {@code CuratorAnnotationBean}s related to  
     *                                  intersect classes used in logical constraints 
     *                                  of a class. 
     * @return                          A {@code Set} of {@code CuratorAnnotationBean}s, 
     *                                  with a size between 0 and 2, providing inferred 
     *                                  negation status, confidences, and supporting texts.
     */
    private Set<CuratorAnnotationBean> getInferredInfoFromLogicalConstraints(
            Set<Set<CuratorAnnotationBean>> annotsPerIntersectClass) 
                    throws IllegalArgumentException {
        log.entry(annotsPerIntersectClass);
        
        Set<CuratorAnnotationBean> posAndNegAnnot = new HashSet<CuratorAnnotationBean>();
        OntologyUtils taxUtils = new OntologyUtils(taxOntWrapper);
        //To determine the confidence level: for each intersect class, 
        //we determine the best confidence level (either from positive, or from negative annots); 
        boolean positiveTested = false;
        boolean negativeTested = false;
        posNegTest: while (!positiveTested || !negativeTested) {
            //to determine confidence term
            Set<OWLClass> bestConfsPerIntersectClass = new HashSet<OWLClass>();
            //to determine supporting text
            Set<CuratorAnnotationBean> annotationsUsed = new HashSet<CuratorAnnotationBean>();
            //to determine negation status
            boolean isPositiveTested = false;
            
            if (!positiveTested) {
                log.trace("Trying to create inferred positive annotation");
                positiveTested = true;
                isPositiveTested = true;
                
                for (Set<CuratorAnnotationBean> relatedAnnots: annotsPerIntersectClass) {
                    Set<OWLClass> confs = new HashSet<OWLClass>();
                    for (CuratorAnnotationBean annot: relatedAnnots) {
                        if (!annot.isNegated()) {
                            confs.add(cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                                    annot.getCioId().trim()));
                            annotationsUsed.add(annot);
                        }
                    }
                    if (confs.isEmpty()) {
                        log.trace("No positive annotations for each intersect class.");
                        bestConfsPerIntersectClass.clear();
                        continue posNegTest;
                    }
                    bestConfsPerIntersectClass.add(
                            cioWrapper.getBestTermWithConfidenceLevel(confs));
                }
                log.trace("Positive annotation will be created");
            } else if (!negativeTested) {
                log.trace("Trying to create inferred negative annotation");
                negativeTested = true;
                //we create negative annotations only if we have a negative annotation 
                //to the taxon that is the leaf of the taxa used in the grouped annotations. 
                //to iterate the interations only once, we store all taxa used in the annotations, 
                //and all taxa used in negative annotations, to compare them afterwards.
                Set<OWLClass> allTaxa = new HashSet<OWLClass>();
                Set<OWLClass> negativeTaxa = new HashSet<OWLClass>();
                for (Set<CuratorAnnotationBean> relatedAnnots: annotsPerIntersectClass) {
                    Set<OWLClass> confs = new HashSet<OWLClass>();
                    //we will used positive annotations for supporting text only 
                    //if there is no negative annotation for this intersect class
                    Set<CuratorAnnotationBean> posAnnots = new HashSet<CuratorAnnotationBean>();
                    Set<CuratorAnnotationBean> negAnnots = new HashSet<CuratorAnnotationBean>();
                    for (CuratorAnnotationBean annot: relatedAnnots) {
                        OWLClass taxCls = taxOntWrapper.getOWLClassByIdentifier(
                                OntologyUtils.getTaxOntologyId(annot.getNcbiTaxonId()), true);
                        //it should already have been checked that all taxa are valid, 
                        //so we only use an assert here.
                        assert taxCls != null;
                        allTaxa.add(taxCls);
                        
                        if (annot.isNegated()) {
                            negativeTaxa.add(taxCls);
                            
                            //we consider only confidence of negative annotations 
                            //to compute the confidence level of the inferred annotation
                            confs.add(cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                                    annot.getCioId().trim()));
                            negAnnots.add(annot);
                        } else {
                            posAnnots.add(annot);
                        }
                    }
                    assert !posAnnots.isEmpty() || !negAnnots.isEmpty();
                    if (!confs.isEmpty()) {
                        bestConfsPerIntersectClass.add(
                            cioWrapper.getBestTermWithConfidenceLevel(confs));
                    }
                    if (!negAnnots.isEmpty()) {
                        annotationsUsed.addAll(negAnnots);
                    } else {
                        annotationsUsed.addAll(posAnnots);
                    }
                }
                if (bestConfsPerIntersectClass.isEmpty()) {
                    log.trace("No related negative annotations.");
                    continue posNegTest;
                }
                //check that we have at least one negative annotation to the most recent 
                //taxon used in annotations, we do not use negative annotations inferrence 
                //from higher taxon (otherwise, we would say, e.g., that prostate epithelium 
                //is not homologous in mammalia, because we have an annotation saying 
                //that the epithelium is not homologous accros metazoa)
                taxUtils.retainLeafClasses(allTaxa, null);
                //all taxa provided should be related, so we should have only one leaf taxon
                assert allTaxa.size() == 1;
                allTaxa.removeAll(negativeTaxa);
                if (!allTaxa.isEmpty()) {
                    log.trace("No negative annotation to the most recent taxon of the group of annotations");
                    bestConfsPerIntersectClass.clear();
                    continue posNegTest;
                }
                
                log.trace("Negative annotation will be created");
            }
            
            //create the annotation to store the confidence level, negation status, 
            //and supporting text. 
            //it is important to test 'bestConfPerIntersectClass' and not 'annotationsUsed', 
            //as 'annotationsUsed' can store some annotations even if no annotation 
            //can be inferred.
            if (!bestConfsPerIntersectClass.isEmpty()) {
                assert !annotationsUsed.isEmpty();
                log.trace("Creating info for new annotation from confidences: {} - annotations: {}", 
                        bestConfsPerIntersectClass, annotationsUsed);
                
                CuratorAnnotationBean newAnnot = new CuratorAnnotationBean();
                newAnnot.setNegated(!isPositiveTested);
                
                //for positive annotation, we determine the lowest confidence level 
                //among the best confidences of the intersect classes; for negative annotation, 
                //we take the best confidence level from supporting negative annotations
                if (isPositiveTested) {
                    newAnnot.setCioId(cioWrapper.getOWLGraphWrapper().getIdentifier(
                        cioWrapper.getLowestTermWithConfidenceLevel(bestConfsPerIntersectClass)));
                } else {
                    newAnnot.setCioId(cioWrapper.getOWLGraphWrapper().getIdentifier(
                            cioWrapper.getBestTermWithConfidenceLevel(bestConfsPerIntersectClass)));
                }
                
                //generate supporting text from supporting annotations
                List<CuratorAnnotationBean> sortedAnnots = 
                        new ArrayList<CuratorAnnotationBean>(annotationsUsed);
                Collections.sort(sortedAnnots, 
                        SimilarityAnnotationUtils.ANNOTATION_BEAN_COMPARATOR);
                //just to be sure to eliminate any redundancy, and to get consistent 
                //supporting text, we store elements used to generate it in a LinkedHashSet
                LinkedHashSet<String> textElements = new LinkedHashSet<String>();
                for (CuratorAnnotationBean annot: sortedAnnots) {
                    List<String> entityIds = SimilarityAnnotationUtils.trimAndSort(annot.getEntityIds());
                    String textElement = SimilarityAnnotationUtils.ENTITY_COL_NAME + ": " 
                            + Utils.formatMultipleValuesToString(entityIds);
                    textElement += ", negated: " + annot.isNegated();
                    textElement += ", taxon ID: " + annot.getNcbiTaxonId();
                    
                    textElements.add(textElement);
                }
                String supportingText = "Annotation inferred from logical constraints "
                        + "using annotations to same HOM ID and: ";
                boolean firstIteration = true;
                for (String textElement: textElements) {
                    if (!firstIteration) {
                        supportingText += " - ";
                    }
                    supportingText += textElement;
                    firstIteration = false;
                }
                newAnnot.setSupportingText(supportingText);
                
                log.trace("Creating info for new annotation: {}", newAnnot);
                posAndNegAnnot.add(newAnnot);
            }
        }
        
        return log.exit(posAndNegAnnot);
    }
    
    /**
     * Generate {@code SummaryAnnotationBean}s from the provided {@code RawAnnotationBean}s. 
     * This method takes single-evidende annotations, and transform them into aggregated 
     * summary annotations. Notably, this method will: i) verify the validity of the provided 
     * annotations (see {@link #checkAnnotations(Collection)}); ii) create summary annotations;
     * iii) check the validity of the {@code SummaryAnnotationBean}s generated; iv) sort these 
     * {@code SummaryAnnotationBean}s (see {@link #sortAnnotations(List)}).
     * 
     * @param annots    A {@code Collection} of {@code RawAnnotationBean}s to aggregate 
     *                  to generate {@code SummaryAnnotationBean}s. 
     * @return          A {@code List} of {@code SummaryAnnotationBean}s generated from 
     *                  {@code annots}, ordered.
     * @throws IllegalArgumentException If some errors were detected in the provided, 
     *                                  or in the generated annotations.
     * @throws IllegalStateException    If the ontologies provided at instantiation 
     *                                  did not allow to retrieve some required information.
     */
    public List<SummaryAnnotationBean> generateSummaryAnnotations(
            Collection<RawAnnotationBean> annots) throws IllegalArgumentException, 
            IllegalStateException {
        log.entry(annots);

        //check the annotations provided
        this.checkAnnotations(annots, false);
        //make sure there are no duplicates and filter annotations with a REJECTED 
        //confidence statement.
        Set<RawAnnotationBean> filteredAnnots = this.filterRejectedAnnotations(annots);
        
        
        //in order to identify related annotations, we will use a Map where keys 
        //are SummaryAnnotationBeans with only the entity IDs, taxon ID, and HOM ID set, 
        //and where associated values are the related RAW annotations.
        Map<SummaryAnnotationBean, Set<RawAnnotationBean>> relatedAnnotMapper = 
                new HashMap<SummaryAnnotationBean, Set<RawAnnotationBean>>();
        //first pass, group related annotations
        for (RawAnnotationBean annot: filteredAnnots) {
            SummaryAnnotationBean keyAnnot = new SummaryAnnotationBean();
            keyAnnot.setHomId(annot.getHomId());
            keyAnnot.setNcbiTaxonId(annot.getNcbiTaxonId());
            //entity IDs in RawAnnotationBean should already be ordered, 
            //but we're never too sure
            keyAnnot.setEntityIds(SimilarityAnnotationUtils.trimAndSort(annot.getEntityIds()));
            //and to have names to generate new annotation, we also store them
            keyAnnot.setEntityNames(annot.getEntityNames());
            keyAnnot.setHomLabel(annot.getHomLabel());
            keyAnnot.setTaxonName(annot.getTaxonName());
            
            Set<RawAnnotationBean> relatedAnnots = relatedAnnotMapper.get(keyAnnot);
            if (relatedAnnots == null) {
                relatedAnnots = new HashSet<RawAnnotationBean>();
                relatedAnnotMapper.put(keyAnnot, relatedAnnots);
            }
            relatedAnnots.add(annot);
        }

        
        //Generate SUMMARY annotations
        Set<SummaryAnnotationBean> summaryAnnots = new HashSet<SummaryAnnotationBean>();
        
        for (Entry<SummaryAnnotationBean, Set<RawAnnotationBean>> relatedAnnotsEntry: 
            relatedAnnotMapper.entrySet()) {
            assert relatedAnnotsEntry.getValue().size() > 0;
            
            //create a new SummaryAnnotationBean (to not modify objects used as keys 
            //in relatedAnnotMapper), using the iterated key (because we have already 
            //ordered Uberon IDs in it)
            SummaryAnnotationBean newAnnot = new SummaryAnnotationBean();
            newAnnot.setHomId(relatedAnnotsEntry.getKey().getHomId());
            newAnnot.setHomLabel(relatedAnnotsEntry.getKey().getHomLabel());
            newAnnot.setNcbiTaxonId(relatedAnnotsEntry.getKey().getNcbiTaxonId());
            newAnnot.setTaxonName(relatedAnnotsEntry.getKey().getTaxonName());
            newAnnot.setEntityIds(SimilarityAnnotationUtils.trimAndSort(relatedAnnotsEntry.getKey().getEntityIds()));
            newAnnot.setEntityNames(relatedAnnotsEntry.getKey().getEntityNames());
            newAnnot.setUnderlyingAnnotCount(relatedAnnotsEntry.getValue().size());
            
            //determine whether there are only negative annotations, to know that 
            //the summary should also be negative (in case of conflicts we always favor 
            //positive annotations, this is what these annotations are about).
            boolean allNegative = true;
            for (RawAnnotationBean relatedAnnot: relatedAnnotsEntry.getValue()) {
                if (!relatedAnnot.isNegated()) {
                    allNegative = false;
                    break;
                }
            }
            newAnnot.setNegated(allNegative);
            
            OWLClass summaryConf = null;
            //if more than one annotation related to this assertion, 
            //compute a global confidence score
            if (relatedAnnotsEntry.getValue().size() > 1) {
    
                //retrieve the global confidence score from all related evidence lines
                summaryConf = this.getSummaryConfidenceStatement(
                        relatedAnnotsEntry.getValue());
                newAnnot.setCioId(cioWrapper.getOWLGraphWrapper().getIdentifier(summaryConf));
                newAnnot.setCioLabel(cioWrapper.getOWLGraphWrapper().getLabel(summaryConf));
                
            } else {
                //otherwise, if only one evidence, we use the original confidence statement
                RawAnnotationBean annot = relatedAnnotsEntry.getValue().iterator().next();
                summaryConf = cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                        annot.getCioId());
                newAnnot.setCioId(annot.getCioId());
                newAnnot.setCioLabel(annot.getCioLabel());
            }
            newAnnot.setTrusted(!cioWrapper.isBgeeNotTrustedStatement(summaryConf));
            summaryAnnots.add(newAnnot);
        }
        

        if (summaryAnnots.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The provided annotations " +
                    "did not allow to generate any SUMMARY annotations."));
        }
        
        //check and sort annotations generated 
        this.checkAnnotations(summaryAnnots, false);
        List<SummaryAnnotationBean> sortedSummaryAnnots = 
                new ArrayList<SummaryAnnotationBean>(summaryAnnots);
        Collections.sort(sortedSummaryAnnots, 
                SimilarityAnnotationUtils.ANNOTATION_BEAN_COMPARATOR);
        
        return log.exit(sortedSummaryAnnots);
    }

    /**
     * Retrieve the proper confidence statement from multiple evidence lines from the provided 
     * {@code Collection} of annotations. The annotations provided should all be related to 
     * a same assertion, so that we can infer a global confidence level. The definition 
     * of whether annotations are related to a same assertion is left to the caller 
     * of this method, this is not verified by this method. Here we assume that 
     * all annotations are associated to a confidence statement from single evidence, 
     * with a confidence level, and are associated to an ECO term, and that none 
     * are associated to the REJECTED confidence statement; this should have been 
     * previously checked, as this is will not be checked here.
     * 
     * @param annots        A {@code Set} of {@code RawAnnotationBean}s 
     *                      related to a same assertion. 
     * @return              An {@code OWLClass} representing a confidence statement 
     *                      from multiple evidence lines, associated to a confidence level.
     * @throws IllegalArgumentException If the annotations provided 
     *                                  do not allow to compute a global confidence score.
     */
    private OWLClass getSummaryConfidenceStatement(Set<RawAnnotationBean> annots) 
            throws IllegalArgumentException {
        log.entry(annots);
        
        OntologyUtils ecoUtils = new OntologyUtils(ecoOntWrapper);
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
        
        for (RawAnnotationBean annot: annots) {
            OWLClass confStatement = cioWrapper.getOWLGraphWrapper().getOWLClassByIdentifier(
                    annot.getCioId(), true);
            OWLClass ecoTerm = ecoOntWrapper.getOWLClassByIdentifier(annot.getEcoId(), true);
            
            Set<OWLClass> toUseECOs  = positiveECOs;
            Set<OWLClass> toUseConfs = positiveConfs;
            if (annot.isNegated()) {
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
        if ((positiveAnnotCount > 0 && negativeAnnotCount > 0 && 
                ecoUtils.containsUnrelatedClassesByIsAPartOf(positiveECOs, negativeECOs)) || 
                (positiveAnnotCount > 0 && 
                        ecoUtils.containsUnrelatedClassesByIsAPartOf(positiveECOs)) || 
                (negativeAnnotCount > 0 && 
                        ecoUtils.containsUnrelatedClassesByIsAPartOf(negativeECOs))) {
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
                //confidenceLevel = null;
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
     * Generate {@code AncestralTaxaAnnotationBean}s from the provided 
     * {@code SummaryAnnotationBean}s:  some annotations can be related to same entity IDs, 
     * but different taxa (alternative homology hypothesis, or confirmatory 
     * information of homology at the level of sub-taxa); and we want to identify 
     * the 'true' common ancestor for these entity IDs. 
     * <p>
     * This method takes summarized annotations for same HOM ID - entity IDs - taxon ID, 
     * and identify the trusted annotations to the most ancestral taxa. As a result, 
     * annotations to same entity IDs will appear only once, annotated to only one taxon, 
     * except in cases of independent evolution. This method explicitely targets 
     * only annotations to the concept of historical homology (see 
     * {@link #HISTORICAL_HOMOLOGY_ID}).
     * <p>
     * This method will: i) verify the validity of the provided 
     * annotations (see {@link #checkAnnotations(Collection)}); ii) identify ancestral taxa 
     * annotations; iii) check the validity of the {@code AncestralTaxaAnnotationBean}s 
     * generated; iv) sort these {@code AncestralTaxaAnnotationBean}s (see 
     * {@link #sortAnnotations(List)}).
     * 
     * @param annots    A {@code Collection} of {@code SummaryAnnotationBean}s to use 
     *                  to identify valid annotations to ancestral taxa. 
     * @return          A {@code List} of {@code AncestralTaxaAnnotationBean}s generated from 
     *                  {@code annots}, ordered.
     * @throws IllegalArgumentException If some errors were detected in the provided, 
     *                                  or in the generated annotations.
     * @throws IllegalStateException    If the ontologies provided at instantiation 
     *                                  did not allow to retrieve some required information.
     */
    public List<AncestralTaxaAnnotationBean> generateAncestralTaxaAnnotations(
            Collection<SummaryAnnotationBean> annots) throws IllegalArgumentException, 
            IllegalStateException {
        log.entry(annots);
        
        //check the annotations provided
        this.checkAnnotations(annots, false);
        //keep only positive annotations to historical homology concept 
        //(at this point we keep not-trusted annotations to properly infer supporting texts)
        Set<SummaryAnnotationBean> filteredAnnots = new HashSet<SummaryAnnotationBean>();
        for (SummaryAnnotationBean annot: annots) {
            if (!annot.isNegated() && HISTORICAL_HOMOLOGY_ID.equals(annot.getHomId())) {
                filteredAnnots.add(annot);
            }
        }
        
        
        //in order to identify related annotations, we will use a Map where keys 
        //are AncestralTaxaAnnotationBeans with only the entity IDs and HOM ID set, 
        //and where associated values are the related SUMMARY annotations.
        Map<AncestralTaxaAnnotationBean, Set<SummaryAnnotationBean>> relatedAnnotMapper = 
                new HashMap<AncestralTaxaAnnotationBean, Set<SummaryAnnotationBean>>();
        //first pass, group related annotations
        for (SummaryAnnotationBean annot: filteredAnnots) {
            AncestralTaxaAnnotationBean keyAnnot = new AncestralTaxaAnnotationBean();
            keyAnnot.setHomId(annot.getHomId());
            //entity IDs in SummaryAnnotationBean should already be ordered, 
            //but we're never too sure
            keyAnnot.setEntityIds(SimilarityAnnotationUtils.trimAndSort(annot.getEntityIds()));
            //and to have names to generate new annotation, we also store them
            keyAnnot.setEntityNames(annot.getEntityNames());
            keyAnnot.setHomLabel(annot.getHomLabel());
            
            Set<SummaryAnnotationBean> relatedAnnots = relatedAnnotMapper.get(keyAnnot);
            if (relatedAnnots == null) {
                relatedAnnots = new HashSet<SummaryAnnotationBean>();
                relatedAnnotMapper.put(keyAnnot, relatedAnnots);
            }
            relatedAnnots.add(annot);
        }
        
        //We also need a mapping of the taxon IDs used to the IDs of their ancestors
        Map<Integer, Set<Integer>> taxToSelfAndAncestors = 
                this.getTaxonToSelfAndAncestorMapping(filteredAnnots);

        
        //Generate ANCESTRAL TAXA annotations
        Set<AncestralTaxaAnnotationBean> newAnnots = new HashSet<AncestralTaxaAnnotationBean>();
        OntologyUtils taxOntUtils = new OntologyUtils(taxOntWrapper);
        
        for (Entry<AncestralTaxaAnnotationBean, Set<SummaryAnnotationBean>> relatedAnnotsEntry: 
            relatedAnnotMapper.entrySet()) {
            log.trace("Trying to find ancestral taxa for annotations {}", relatedAnnotsEntry);
            assert relatedAnnotsEntry.getValue().size() > 0;
            
            //we retrieve all taxa to trusted annotations as OWL classes
            Set<OWLClass> taxClasses = new HashSet<OWLClass>();
            for (SummaryAnnotationBean relatedAnnot: relatedAnnotsEntry.getValue()) {
                if (!relatedAnnot.isTrusted()) {
                    continue;
                }
                taxClasses.add(taxOntWrapper.getOWLClassByIdentifier(
                        OntologyUtils.getTaxOntologyId(relatedAnnot.getNcbiTaxonId())));
            }
            if (taxClasses.isEmpty()) {
                log.trace("No positive annotations for these entity IDs - HOM ID, skip.");
                continue;
            }
            log.trace("All valid taxon classes identified: {}", taxClasses);
            
            //identify the most ancestral taxa (there can be several in case of 
            //independent evolution)
            taxOntUtils.retainParentClasses(taxClasses, null);
            assert taxClasses.size() >= 1;
            Set<Integer> ancestralTaxIds = new HashSet<Integer>();
            for (OWLClass taxCls: taxClasses) {
                ancestralTaxIds.add(OntologyUtils.getTaxNcbiId(taxOntWrapper.getIdentifier(
                        taxCls)));
            }
            log.trace("Valid ancestral taxon IDs: {}", ancestralTaxIds);
            

            //now, we create the annotations based on the valid ancestral taxa
            for (SummaryAnnotationBean relatedAnnot: relatedAnnotsEntry.getValue()) {
                if (!ancestralTaxIds.contains(relatedAnnot.getNcbiTaxonId())) {
                    continue;
                }
                log.trace("Valid annotation identified: {}", relatedAnnot);
                
                //create a new AncestralTaxaAnnotationBean (to not modify objects used as keys 
                //in relatedAnnotMapper), using the iterated key (because we already stored 
                //information in it)
                AncestralTaxaAnnotationBean newAnnot = new AncestralTaxaAnnotationBean();
                newAnnot.setHomId(relatedAnnotsEntry.getKey().getHomId());
                newAnnot.setHomLabel(relatedAnnotsEntry.getKey().getHomLabel());
                newAnnot.setEntityIds(SimilarityAnnotationUtils.trimAndSort(
                        relatedAnnotsEntry.getKey().getEntityIds()));
                newAnnot.setEntityNames(relatedAnnotsEntry.getKey().getEntityNames());
                newAnnot.setNegated(false);
                newAnnot.setNcbiTaxonId(relatedAnnot.getNcbiTaxonId());
                newAnnot.setTaxonName(relatedAnnot.getTaxonName());
                newAnnot.setCioId(relatedAnnot.getCioId());
                newAnnot.setCioLabel(relatedAnnot.getCioLabel());
                
                //to generate the supporting text, we check whether there are 
                //alternative not-trusted homology hypothesis for higher taxa
                log.trace("Trying to find non-trusted alternative ancestral taxon hypotheses for new annot: {} - taxToAncestors: {}", 
                        newAnnot, taxToSelfAndAncestors.get(relatedAnnot.getNcbiTaxonId()));
                Set<String> higherTaxonNames = new HashSet<String>();
                for (SummaryAnnotationBean relatedAnnot2: relatedAnnotsEntry.getValue()) {
                    log.trace("Testing annotation: {}", relatedAnnot2);
                    if (relatedAnnot2.getNcbiTaxonId() != relatedAnnot.getNcbiTaxonId() && 
                            taxToSelfAndAncestors.get(relatedAnnot.getNcbiTaxonId()).contains(
                                    relatedAnnot2.getNcbiTaxonId())) {
                        assert !relatedAnnot2.isTrusted();
                        log.trace("Non-trusted alternative ancestral taxon found: {}", 
                                relatedAnnot2.getTaxonName());
                        higherTaxonNames.add(relatedAnnot2.getTaxonName());
                    }
                }
                if (!higherTaxonNames.isEmpty()) {
                    //order the names for consistent supporting text generation
                    List<String> orderedTaxNames = new ArrayList<String>(higherTaxonNames);
                    Collections.sort(orderedTaxNames);
                    String supportingText = "";
                    
                    if (orderedTaxNames.size() == 1) {
                        supportingText = "Alternative homology hypothesis of low confidence "
                                + "exists for taxon: ";
                    } else {
                        supportingText = "Alternative homology hypotheses of low confidence "
                                + "exist for taxa: ";
                    }      
                    boolean firstIteration = true;
                    for (String taxName: orderedTaxNames) {
                        if (!firstIteration) {
                            supportingText += ", ";
                        }
                        supportingText += taxName;
                        firstIteration = false;
                    }
                    newAnnot.setSupportingText(supportingText);
                } else {
                    newAnnot.setSupportingText(null);
                }
                
                newAnnots.add(newAnnot);
            }
        }
        

        if (newAnnots.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The provided annotations " +
                    "did not allow to generate any ANCESTRAL TAXA annotations."));
        }
        
        //check and sort annotations generated 
        this.checkAnnotations(newAnnots, false);
        List<AncestralTaxaAnnotationBean> sortedAnnots = 
                new ArrayList<AncestralTaxaAnnotationBean>(newAnnots);
        Collections.sort(sortedAnnots, SimilarityAnnotationUtils.ANNOTATION_BEAN_COMPARATOR);
        
        return log.exit(sortedAnnots);
    }

    
//    /**
//     * Order {@code annotations} by alphabetical order of some fields, 
//     * for easier diff between different releases of the annotation file. 
//     * {@code annotations} will be modified as a result of the call to this method.
//     * 
//     * @param annotations   A {@code List} of {@code AnnotationBean}s to be ordered.
//     * @param <T>           The type of {@code AnnotationBean} in {@code annotations}.
//     */
//    private static <T extends AnnotationBean> void sortAnnotations(List<T> annotations) {
//        Collections.sort(annotations, new Comparator<T>() {
//            @Override
//            public int compare(AnnotationBean o1, AnnotationBean o2) {
//
//                String homId1 = o1.getHomId();
//                if (homId1 == null) {
//                    homId1 = "";
//                }
//                String homId2 = o2.getHomId();
//                if (homId2 == null) {
//                    homId2 = "";
//                }
//                int comp = homId1.compareTo(homId2);
//                if (comp != 0) {
//                    return comp;
//                }
//                
//                String elementId1 = "";
//                if (o1.getEntityIds() != null) {
//                    List<String> elementIds = new ArrayList<String>(o1.getEntityIds());
//                    Collections.sort(elementIds);
//                    elementId1 = elementIds.toString();
//                }
//                String elementId2 = "";
//                if (o2.getEntityIds() != null) {
//                    List<String> elementIds = new ArrayList<String>(o2.getEntityIds());
//                    Collections.sort(elementIds);
//                    elementId2 = elementIds.toString();
//                }
//                comp = elementId1.compareTo(elementId2);
//                if (comp != 0) {
//                    return comp;
//                }
//                
//                int taxonId1 = o1.getNcbiTaxonId();
//                int taxonId2 = o2.getNcbiTaxonId();
//                if (taxonId1 < taxonId2) {
//                    return -1;
//                } else if (taxonId1 > taxonId2) {
//                    return 1;
//                }
//                
//                if (!o1.isNegated() && o2.isNegated()) {
//                    return -1;
//                } else if (o1.isNegated() && !o2.isNegated()) {
//                    return 1;
//                }
//                
//                if (o1 instanceof SummaryAnnotationBean && o2 instanceof SummaryAnnotationBean) {
//                    if (((SummaryAnnotationBean) o1).isTrusted() && 
//                            !((SummaryAnnotationBean) o2).isTrusted()) {
//                        return -1;
//                    } else if (!((SummaryAnnotationBean) o1).isTrusted() && 
//                            ((SummaryAnnotationBean) o2).isTrusted()) {
//                        return 1;
//                    }
//                }
//                
//                String confId1 = o1.getCioId();
//                if (confId1 == null) {
//                    confId1 = "";
//                }
//                String confId2 = o2.getCioId();
//                if (confId2 == null) {
//                    confId2 = "";
//                }
//                comp = confId1.compareTo(confId2);
//                if (comp != 0) {
//                    return comp;
//                }
//                
//                
//                if (o1 instanceof RawAnnotationBean && o2 instanceof RawAnnotationBean) {
//                    String ecoId1 = ((RawAnnotationBean) o1).getEcoId();
//                    if (ecoId1 == null) {
//                        ecoId1 = "";
//                    }
//                    String ecoId2 = ((RawAnnotationBean) o2).getEcoId();
//                    if (ecoId2 == null) {
//                        ecoId2 = "";
//                    }
//                    comp = ecoId1.compareTo(ecoId2);
//                    if (comp != 0) {
//                        return comp;
//                    }
//                    
//                    String refId1 = ((RawAnnotationBean) o1).getRefId();
//                    if (refId1 == null) {
//                        refId1 = "";
//                    }
//                    String refId2 = ((RawAnnotationBean) o2).getRefId();
//                    if (refId2 == null) {
//                        refId2 = "";
//                    }
//                    comp = refId1.compareTo(refId2);
//                    if (comp != 0) {
//                        return comp;
//                    }
//                }
//                
//                String supportText1 = o1.getSupportingText();
//                if (supportText1 == null) {
//                    supportText1 = "";
//                }
//                String supportText2 = o2.getSupportingText();
//                if (supportText2 == null) {
//                    supportText2 = "";
//                }
//                comp = supportText1.compareTo(supportText2);
//                if (comp != 0) {
//                    return comp;
//                }
//                
//                
//                return 0;
//            }
//        });
//    }
//    
//    /**
//     * Retrieve annotations for a specific taxon from a similarity annotation file 
//     * and write them into an output file.
//     * 
//     * @param similarityFile    A {@code String} that is the path to the annotation file.
//     * @param fileType          A {@code GeneratedFileType} defining what type of file is 
//     *                          {@code similarityFile}. This allows to define headers, etc.
//     * @param taxOntFile        An {@code String} that is the path to the taxonomy ontology, 
//     *                          to retrieve ancestors of the taxon with ID {@code taxonId}.
//     * @param taxonId           An {@code int} that is the NCBI ID of the taxon 
//     *                          for which we want to retrieve annotations, including 
//     *                          for its ancestral taxa.  
//     * @param outputFile        A {@code String} that is the path to the output file 
//     *                          to be written.
//     * @throws FileNotFoundException
//     * @throws IOException
//     * @throws OWLOntologyCreationException
//     * @throws OBOFormatParserException
//     * @see #extractSummaryAnnotationsForTaxon(String, String, int)
//     */
//    public void writeToFileAnnotationsForTaxon(String similarityFile, 
//            GeneratedFileType fileType, String taxOntFile, 
//            int taxonId, String outputFile) throws FileNotFoundException, IOException, 
//            OWLOntologyCreationException, OBOFormatParserException {
//        log.entry(similarityFile, fileType, taxOntFile, taxonId, outputFile);
//        
//        //TODO: generalized for any type of annotation
//        List<Map<String, Object>> summarizedAnnotations = 
//                this.extractSummaryAnnotationsForTaxon(similarityFile, taxOntFile, taxonId);
//        this.writeAnnotationsToFile(outputFile, fileType, summarizedAnnotations);
//        
//        log.exit();
//    }
//    
//    /**
//     * Retrieve summarized annotations for a specific taxon from a <strong>clean</strong> 
//     * similarity annotation file. Only <strong>positive</strong> annotations are retrieved 
//     * (annotations with soleley a "NOT" qualifier are not returned).
//     * <p>
//     * This method will retrieve all annotations that are applicable to the taxon 
//     * with the NCBI ID {@code taxonId} (for instance, {@code 9606}), and to 
//     * all its ancestral taxa. For a given entity ID and taxon ID, only one annotation 
//     * will be retrieved: either the {@code SUMMARY} annotation if available, 
//     * or the {@code RAW} annotation when only a single evidence is available 
//     * for this assertion. 
//     * <p>
//     * The assertions are returned as a {@code List} of {@code Map}s, where 
//     * each {@code Map} represents a summarized annotation. 
//     * See {@link #extractAnnotations(String, boolean)} for details about the keys used 
//     * in the {@code Map}s. 
//     *  
//     * @param similarityFile    A {@code String} that is the path to the annotation file.
//     * @param taxOntFile        An {@code String} that is the path to the taxonomy ontology, 
//     *                          to retrieve ancestors of the taxon with ID {@code taxonId}.
//     * @param taxonId           An {@code int} that is the NCBI ID of the taxon 
//     *                          for which we want to retrieve annotations, including 
//     *                          for its ancestral taxa.  
//     * @return                  A {@code List} of {@code Map}s, where each {@code Map} 
//     *                          represents a summarized annotation.
//     * @throws IOException 
//     * @throws FileNotFoundException 
//     * @throws OBOFormatParserException 
//     * @throws OWLOntologyCreationException 
//     */
//    public List<Map<String, Object>> extractSummaryAnnotationsForTaxon(String similarityFile, String taxOntFile, 
//            int taxonId) throws FileNotFoundException, IOException, 
//            OWLOntologyCreationException, OBOFormatParserException {
//        log.entry(similarityFile, taxOntFile, taxonId);
//        
//        //first, we retrieve from the taxonomy ontology the IDs of all the ancestor 
//        //of the taxon with ID taxonId
//        OWLOntology ont = OntologyUtils.loadOntology(taxOntFile);
//        OWLGraphWrapper wrapper = new OWLGraphWrapper(ont);
//        OWLClass taxClass = wrapper.getOWLClassByIdentifier(OntologyUtils.getTaxOntologyId(taxonId), true);
//        if (taxClass == null) {
//            throw log.throwing(new IllegalArgumentException("The taxon with ID " + taxonId + 
//                    " was not retrieved from the ontology file " + taxOntFile));
//        }
//        Set<Integer> allTaxIds = new HashSet<Integer>();
//        for (OWLClass ancestor: wrapper.getOWLClassAncestors(taxClass)) {
//            allTaxIds.add(OntologyUtils.getTaxNcbiId(wrapper.getIdentifier(ancestor)));
//        }
//        log.debug("Allowed tax IDs: {}", allTaxIds);
//        
//        List<Map<String, Object>> allAnnotations = this.extractAnnotations(similarityFile, false);
//        //associate annotations to a key to be able to identify SUMMARY annotations
//        Map<String, Map<String, Object>> summarizedAnnotations = new HashMap<String, Map<String, Object>>();
//        //iterate all annotations
//        for (Map<String, Object> annotation: allAnnotations) {
//            String key = annotation.get(ENTITY_COL_NAME) + " - " + 
//                annotation.get(HOM_COL_NAME) + " - " + annotation.get(TAXON_COL_NAME);
//            //check it is a requested taxon
//            if (!allTaxIds.contains(annotation.get(TAXON_COL_NAME))) {
//                continue;
//            }
//            
//            //if an annotation for this HOM ID/Entity ID/Taxon ID was already seen, 
//            //then we wait for the SUMMARY line. If it is the first time we see it, 
//            //we use it directly.
//            if (!summarizedAnnotations.containsKey(key) || 
//                    (summarizedAnnotations.containsKey(key) && 
//                    annotation.get(LINE_TYPE_COL_NAME).equals( ))) {
//                summarizedAnnotations.put(key, new HashMap<String, Object>(annotation));
//            }
//        }
//        
//        //now we filter to remove negative assertions.
//        //we do it afterwards, to be sure all information was taken into account 
//        //for the SUMMARY lines
//        List<Map<String, Object>> filteredAnnotations = new ArrayList<Map<String, Object>>();
//        for (Map<String, Object> annotation: summarizedAnnotations.values()) {
//            //check it is not a negative assertion
//            if (annotation.get(QUALIFIER_COL_NAME) == null || 
//                    !annotation.get(QUALIFIER_COL_NAME).equals(NEGATE_QUALIFIER)) {
//                filteredAnnotations.add(annotation);
//            }
//        }
//        
//        return log.exit(filteredAnnotations);
//    }
}
