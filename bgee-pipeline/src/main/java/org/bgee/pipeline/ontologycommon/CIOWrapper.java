package org.bgee.pipeline.ontologycommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLPropertyExpression;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;
import owltools.io.ParserWrapper;

/**
 * Class providing convenient methods to use the Confidence Information Ontology (CIO, see the 
 * <a href='https://github.com/BgeeDB/confidence-information-ontology'>CIO repository</a>). 
 * This class does not use the usual bgee utils, to be provided as code example. 
 * The dependencies for this class are: OWLTools, OWLAPI, Log4j2. 
 * <p>
 * The CIO is divided into 'confidence information statement', that are terms usable 
 * in annotations, and 'confidence information element', that are terms representing 
 * intrinsic properties of the CI statements, used to pre-compose them. 
 * 'Confidence information element' has 3 sub-types: 'confidence level', 
 * 'evidence concordance', and 'evidence type concordance'. 
 * <p>
 * A 'confidence information statement' is made of at most 3 'confidence information element', 
 * one for each of these sub-types (see {@link #getConfidenceLevel(OWLClass)}, 
 * {@link #getEvidenceConcordance(OWLClass)}, and {@link #getEvidenceTypeConcordance(OWLClass)}).
 * <p>
 * Notable convenient methods allow to identify the best CI statements among 
 * a {@code Collection} (see {@link #getBestTermWithConfidenceLevel(Collection)}), 
 * or to retrieve a CI statement for the provided 'evidence concordance', 
 * 'evidence type concordance', and 'confidence level' (see {@link 
 * #getSingleEvidenceConfidenceStatement(OWLClass)} and 
 * {@link #getConfidenceStatement(OWLClass, OWLClass, OWLClass)}).
 * <p>
 * This class also provides static final {@code String}s that are OBO-like IDs 
 * of classes and object properties that are likely to be useful to users. They allow 
 * to retrieve the corresponding {@code OWLClass} or {@code OWLObjectProperty} by calling 
 * the {@code OWLGraphWrapper} methods {@code getOWLClassByIdentifier} and 
 * {@code getOWLObjectPropertyByIdentifier}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Feb. 2015
 * @since Bgee 13
 */
public class CIOWrapper {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(CIOWrapper.class.getName());
    
    /**
     * A class allowing to wrap an {@code OWLClass} retrieved from the CIO, 
     * providing functionalities common to all these {@code OWLClass}es.
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
//    private class CIOTerm {
//        /**
//         * The {@code OWLClass} that this object wraps. 
//         */
//        private final OWLClass clsWrapped;
//        /**
//         * The {@code CIOWrapper} which {@code clsWrapped} was retrieved from.
//         */
//        private final CIOWrapper cioWrapper;
//
//        /**
//         * See {@link #isConfidenceStatement()}.
//         */
//        private final boolean confidenceStatement;
//        /**
//         * See {@link #getConfidenceLevel()}.
//         */
//        private final OWLClass confidenceLevel;
//        /**
//         * See {@link #getEvidenceConcordance()}.
//         */
//        private final OWLClass evidenceConcordance;
//        /**
//         * See {@link #getEvidenceTypeConcordance()}.
//         */
//        private final OWLClass evidenceTypeConcordance;
//        
//        /**
//         * Constructor providing the {@code OWLClass} wrapped by this object, 
//         * and the {@code CIOWrapper} wrapping the ontology which {@code clsWrapped} 
//         * was retrieved from.
//         * 
//         * @param clsWrapped    the {@code OWLClass} from the CIO wrapped by this object.
//         * @param cioWrapper    the {@code CIOWrapper} wrapping the ontology {@code clsWrapped} 
//         *                      was retrieved from.
//         * @throws NullPointerException If {@code clsWrapped} or {@code cioWrapper} 
//         *                              are {@code null}.
//         */
//        private CIOTerm(OWLClass clsWrapped, CIOWrapper cioWrapper) throws NullPointerException {
//            log.entry(clsWrapped, cioWrapper);
//            this.clsWrapped = clsWrapped;
//            this.cioWrapper = cioWrapper;
//            
//            boolean isStatement = false;
//            OWLClass confidenceLevel = null;
//            OWLClass evidenceConcordance = null;
//            OWLClass evidenceTypeConcordance = null;
//            Set<OWLClass> ancestors = 
//                    this.cioWrapper.getWrapper().getAncestorsThroughIsA(this.clsWrapped);
//            
//            if (ancestors.contains(this.cioWrapper.getConfidenceStatement())) {
//                isStatement = true;
//            }
//            this.confidenceStatement = isStatement;
//
//            //if this OWLClas is a confidence information statement, 
//            //determine confidence elements composing it
//            if (this.isConfidenceStatement()) {
//                confidenceLevel = this.getConfidenceElement(ancestors, 
//                        this.cioWrapper.getConfidenceLevels());
//                evidenceConcordance = this.getConfidenceElement(ancestors, 
//                        this.cioWrapper.getEvidenceConcordances());
//                evidenceTypeConcordance = this.getConfidenceElement(ancestors, 
//                        this.cioWrapper.getEvidenceTypeConcordances());
//            }
//            
//            this.confidenceLevel = confidenceLevel;
//            this.evidenceConcordance = evidenceConcordance;
//            this.evidenceTypeConcordance = evidenceTypeConcordance;
//            
//            log.traceExit();
//        }
//        
//        /**
//         * Identify the most precise {@code OWLClass} among the {@code OWLClass}es 
//         * in {@code clsWrappedAncestors} that is present in {@code validConfidenceElements}. 
//         * This is a convenience method used by the constructor to identify the correct 
//         * 'confidence information element' of each type ('confidence level', 
//         * 'evidence concordance', 'evidence type concordance').
//         * {@code clsWrappedAncestors} should contain all is_a ancestors of {@link #clsWrapped}, 
//         * while {@code validConfidenceElements} should contain all valid 'confidence information 
//         * elements' of a given type.
//         * 
//         * @param clsWrappedAncestors       A {@code Set} of {@code OWLClass}es that are 
//         *                                  all ancestors of {@link #clsWrapped} by is_a relations.
//         * @param validConfidenceElements   A {@code Set} of {@code OWLClass}es that are 
//         *                                  the 'confidence information elements' of a same type.
//         * @return  An {@code OWLClass} that is the most precise term among 
//         *          {@code clsWrappedAncestors}, present in {@code validConfidenceElements}. 
//         *          Can be {@code null} if none could be identified. 
//         * @throws IllegalArgumentException If several same level {@code OWLClass}es 
//         *                                  are present in both {@code clsWrappedAncestors} 
//         *                                  and {@code validConfidenceElements}.
//         */
//        private OWLClass getConfidenceElement(Set<OWLClass> clsWrappedAncestors, 
//                Set<OWLClass> validConfidenceElements) throws IllegalArgumentException {
//            log.entry(clsWrappedAncestors, validConfidenceElements);
//            
//            Set<OWLClass> confElementsAncestors = new HashSet<OWLClass>(clsWrappedAncestors);
//            confElementsAncestors.retainAll(validConfidenceElements);
//            this.retainLeafClasses(confElementsAncestors);
//            if (confElementsAncestors.size() > 1) {
//                throw log.throwing(new IllegalArgumentException("The provided ontology "
//                        + "does not allow to identify a unique confidence information element "
//                        + "of a given type for OWLClass: " + this.clsWrapped + 
//                        " - Confidence information elements of same type identified: "
//                        + confElementsAncestors));
//            } else if (confElementsAncestors.size() == 1) {
//                return log.traceExit(confElementsAncestors.iterator().next());
//            }
//            
//            return log.traceExit(null);
//        }
//        /**
//         * Modify {@code classes} to retain only {@code OWLClass}es with no descendants 
//         * via is_a relations in the {@code Set}. {@code classes} will be modified 
//         * as a result of the call to this method.
//         * 
//         * @param classes   A {@code Set} of {@code OWLClass}es to be modified to retain only 
//         *                  leaf classes (with no descendants in this {@code Set} through 
//         *                  is_a relations)
//         */
//        /*
//         * Note: we do not use the methods from OntologyUtils to avoid dependency 
//         * to the Bgee project, to provide this class as an example, on the CIO tracker. 
//         * Unlike the OntolgyUtils method, this one does not manage cycles (should not be 
//         * necessary with the CIO).
//         */
//        private void retainLeafClasses(Set<OWLClass> classes) {
//            log.entry(classes);
//            Set<OWLClass> allAncestors = new HashSet<OWLClass>();
//            for (OWLClass cls: classes) {
//                allAncestors.addAll(this.cioWrapper.getWrapper().getAncestorsThroughIsA(cls));
//            }
//            classes.removeAll(allAncestors);
//            log.trace("Leaf classes retained: {}", classes);
//            log.traceExit();
//        }
//        
//        
//        /**
//         * @return  {@code true} if the wrapped {@code OWLClass} is a subclass of 
//         *          'confidence information statement' 
//         *          (see {@link CIOWrapper#CONFIDENCE_STATEMENT_ID}).
//         */
//        public boolean isConfidenceStatement() {
//            return confidenceStatement;
//        }
//        /**
//         * Gets the confidence level of this {@code CIOTerm}. The returned {@code OWLClass} 
//         * is one of the {@code OWLClass}es returned by {@link CIOWrapper#getConfidenceLevels()}. 
//         * It can be {@code null} in the following cases: 
//         * <ul>
//         * <li>if this {@code CIOTerm} is not a 'confidence information statement', 
//         * but a 'confidence information element' ({@link #isConfidenceStatement()} 
//         * returns {@code false}).
//         * <li>If this {@code CIOTerm} is not associated to a confidence level (non-leaf 
//         * statement, or statement for which a confidence level cannot be assigned, 
//         * such as 'confidence from strongly conflicting evidence lines').
//         * </ul>
//         *          
//         * @return  The {@code OWLClass} representing the confidence level 
//         *          of this {@code CIOTerm}. Can be {@code null}.
//         * @see CIOWrapper#getConfidenceLevels()
//         */
//        public OWLClass getConfidenceLevel() {
//            return this.confidenceLevel;
//        }
//        /**
//         * Gets the evidence concordance of this {@code CIOTerm}. The returned {@code OWLClass} 
//         * is one of the {@code OWLClass}es returned by 
//         * {@link CIOWrapper#getEvidenceConcordances()}. It can be {@code null} 
//         * in the following cases: 
//         * <ul>
//         * <li>if this {@code CIOTerm} is not a 'confidence information statement', 
//         * but a 'confidence information element' ({@link #isConfidenceStatement()} 
//         * returns {@code false}).
//         * <li>If this {@code CIOTerm} is a root of the ontology. In all other cases, 
//         * an evidence concordance is always associated to a CIO term. 
//         * </ul>
//         *          
//         * @return  The {@code OWLClass} representing the evidence concordance 
//         *          of this {@code CIOTerm}. Can be {@code null}.
//         * @see CIOWrapper#getEvidenceConcordances()
//         */
//        public OWLClass getEvidenceConcordance() {
//            return this.evidenceConcordance;
//        }
//        /**
//         * Gets the evidence type concordance of this {@code CIOTerm}. The returned 
//         * {@code OWLClass} is one of the {@code OWLClass}es returned by 
//         * {@link CIOWrapper#getEvidenceTypeConcordances()}. It can be {@code null} 
//         * in the following cases: 
//         * <ul>
//         * <li>if this {@code CIOTerm} is not a 'confidence information statement', 
//         * but a 'confidence information element' ({@link #isConfidenceStatement()} 
//         * returns {@code false}).
//         * <li>If this {@code CIOTerm} is not a subclass of the term 'confidence from multiple 
//         * evidence lines' ({@link #getEvidenceConcordance()} does not return a term 
//         * from the branch 'concordance of multiple evidence lines').
//         * </ul>
//         *          
//         * @return  The {@code OWLClass} representing the evidence type concordance 
//         *          of this {@code CIOTerm}. Can be {@code null}.
//         * @see CIOWrapper#getEvidenceTypeConcordances()
//         */
//        public OWLClass getEvidenceTypeConcordance() {
//            return this.evidenceTypeConcordance;
//        }
//        
//        /**
//         * @return  The {@code OWLClass} wrapped by this {@code CIOTerm}, 
//         *          provided at instantiation.
//         */
//        public OWLClass getWrappedOWLClass() {
//            return this.clsWrapped;
//        }
//    }


    /**
     * A {@code String} that is the OBO-like ID from the CIO 
     * of the relation "has_confidence_element", parent of all object properties 
     * used to relate CI statements to CI elements.
     */
    public final static String HAS_CONFIDENCE_ELEMENT_ID = "has_confidence_element";
    /**
     * A {@code String} that is the OBO-like ID from the CIO 
     * of the relation "has_evidence_concordance", sub-property of 'has_confidence_element'.
     * @see #HAS_CONFIDENCE_ELEMENT_ID
     */
    public final static String HAS_EVIDENCE_CONCORDANCE_ID = "has_evidence_concordance";
    /**
     * A {@code String} that is the OBO-like ID from the CIO 
     * of the relation "has_evidence_type_concordance", sub-property of 'has_confidence_element'.
     * @see #HAS_CONFIDENCE_ELEMENT_ID
     */
    public final static String HAS_EVIDENCE_TYPE_CONCORDANCE_ID = "has_evidence_type_concordance";
    /**
     * A {@code String} that is the OBO-like ID from the CIO 
     * of the relation "has_confidence_level", sub-property of 'has_confidence_element'.
     * @see #HAS_CONFIDENCE_ELEMENT_ID
     */
    public final static String HAS_CONFIDENCE_LEVEL_ID = "has_confidence_level";
    
    /**
     * A {@code String} that is the OBO-like ID from the CIO 
     * of the relation "provides_greater_confidence_than".
     */
    public final static String PROVIDES_GREATER_CONF_THAN_ID = "provides_greater_confidence_than";

    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the class 'confidence information statement', parent of all classes defining 
     * statements of confidence information, usable by annotators, pre-composed by using 
     * CI elements.
     * @see #CONFIDENCE_ELEMENT_ID
     */
    public final static String CONFIDENCE_STATEMENT_ID = "CIO:0000000";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the CI statement "rejected".
     */
    public final static String REJECTED_STATEMENT_ID = "CIO:0000039";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the class 'confidence information element', parent of all classes capturing 
     * Intrinsic properties of CI statements, used to pre-compose them.
     * @see #CONFIDENCE_STATEMENT_ID
     */
    public final static String CONFIDENCE_ELEMENT_ID = "CIO:0000040";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "confidence level", parent of all classes providing confidence level 
     * information, subclass of 'confidence element' (see {@link #CONFIDENCE_ELEMENT_ID}).
     */
    public final static String CONFIDENCE_LEVEL_ID = "CIO:0000028";
    //    /**
    //     * A {@code String} that is the OBO-like ID from the confidence information ontology 
    //     * of the term "confidence from single evidence".
    //     * @see #MULTIPLE_EVIDENCE_CONF_ID
    //     */
    //    public final static String SINGLE_EVIDENCE_CONF_ID = "CIO:0000001";
    //    /**
    //     * A {@code String} that is the OBO-like ID from the confidence information ontology 
    //     * of the term "confidence from multiple evidence lines".
    //     * @see #SINGLE_EVIDENCE_CONF_ID
    //     */
    //    public final static String MULTIPLE_EVIDENCE_CONF_ID = "CIO:0000002";
    //    /**
    //     * A {@code String} that is the OBO-like ID from the confidence information ontology 
    //     * of the term "confidence from congruent evidence lines of same type".
    //     */
    //    public final static String CONGRUENT_SAME_TYPE_EVIDENCE_CONF_ID = "CIO:0000016";
    //    /**
    //     * A {@code String} that is the OBO-like ID from the confidence information ontology 
    //     * of the term "confidence from congruent evidence lines of multiple types".
    //     */
    //    public final static String CONGRUENT_MULTIPLE_TYPES_EVIDENCE_CONF_ID = "CIO:0000008";
    //    /**
    //     * A {@code String} that is the OBO-like ID from the confidence information ontology 
    //     * of the term "confidence from weakly conflicting evidence lines of same type".
    //     */
    //    public final static String WEAK_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID = "CIO:0000021";
    //    /**
    //     * A {@code String} that is the OBO-like ID from the confidence information ontology 
    //     * of the term "confidence from weakly conflicting evidence lines of multiple types".
    //     */
    //    public final static String WEAK_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID = "CIO:0000011";
    //    /**
    //     * A {@code String} that is the OBO-like ID from the confidence information ontology 
    //     * of the term "confidence from strongly conflicting evidence lines of same type".
    //     */
    //    public final static String STRONG_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID = "CIO:0000020";
    //    /**
    //     * A {@code String} that is the OBO-like ID from the confidence information ontology 
    //     * of the term "confidence from strongly conflicting evidence lines of multiple types".
    //     */
    //    public final static String STRONG_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID = "CIO:0000010";
    //    
        /**
         * A {@code String} that is the OBO-like ID from the confidence information ontology 
         * of the term "high confidence level", subclass of 'confidence level'.
         * @see #CONFIDENCE_LEVEL_ID
         * @see #MEDIUM_CONF_LEVEL_ID
         * @see #LOW_CONF_LEVEL_ID
         */
        public final static String HIGH_CONF_LEVEL_ID = "CIO:0000029";

    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "medium confidence level", subclass of 'confidence level'.
     * @see #CONFIDENCE_LEVEL_ID
     * @see #HIGH_CONF_LEVEL_ID
     * @see #LOW_CONF_LEVEL_ID
     */
    public final static String MEDIUM_CONF_LEVEL_ID = "CIO:0000030";

    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "low confidence level", subclass of 'confidence level'.
     * @see #CONFIDENCE_LEVEL_ID
     * @see #MEDIUM_CONF_LEVEL_ID
     * @see #HIGH_CONF_LEVEL_ID
     */
    public final static String LOW_CONF_LEVEL_ID = "CIO:0000031";

    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "evidence concordance", parent of all classes providing 'evidence concordance' 
     * information, subclass of 'confidence element' (see {@link #CONFIDENCE_ELEMENT_ID}).
     */
    public final static String EVIDENCE_CONCORDANCE_ID = "CIO:0000032";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "single evidence", subclass of 'evidence concordance' (see 
     * {@link #EVIDENCE_CONCORDANCE_ID}).
     */
    public final static String SINGLE_EVIDENCE_CONCORDANCE_ID = "CIO:0000042";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "congruent".
     * @see #WEAKLY_CONFLICTING_CONCORDANCE_ID
     * @see #STRONGLY_CONFLICTING_CONCORDANCE_ID
     */
    public final static String CONGRUENT_CONCORDANCE_ID = "CIO:0000033";

    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "weakly conflicting".
     * @see #CONGRUENT_CONCORDANCE_ID
     * @see #STRONGLY_CONFLICTING_CONCORDANCE_ID
     */
    public final static String WEAKLY_CONFLICTING_CONCORDANCE_ID = "CIO:0000036";

    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "strongly conflicting".
     * @see #CONGRUENT_CONCORDANCE_ID
     * @see #WEAKLY_CONFLICTING_CONCORDANCE_ID
     */
    public final static String STRONGLY_CONFLICTING_CONCORDANCE_ID = "CIO:0000035";

    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "evidence concordance", parent of all classes providing 'evidence type 
     * concordance' information, subclass of 'confidence element' (see 
     * {@link #CONFIDENCE_ELEMENT_ID}).
     */
    public final static String EVIDENCE_TYPE_CONCORDANCE_ID = "CIO:0000041";

    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "same type".
     * @see #DIFFERENT_TYPES_EVIDENCE_CONCORDANCE_ID
     */
    public final static String SAME_TYPE_EVIDENCE_CONCORDANCE_ID = "CIO:0000037";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "different types".
     * @see #SAME_TYPE_EVIDENCE_CONCORDANCE_ID
     */
    public final static String DIFFERENT_TYPES_EVIDENCE_CONCORDANCE_ID = "CIO:0000038";
    
    /**
     * A {@code String} that is the name of the subset grouping CI statements 
     * that are trusted by the Bgee team.
     */
    public final static String BGEE_NOT_TRUSTED_SUBSET = "bgee_not_trusted";
    
    /**
     * Load {@code pathToOntology} as an {@code OWLOntology}.
     * @param pathToOntology    A {@code String} that is the path to an ontology to be loaded, 
     *                          either in obo or owl format.
     * @return                  An {@code OWLOntology} loaded from the file 
     *                          {@code pathToOntology}.
     * @throws OWLOntologyCreationException If an error occurred while loading the ontology.
     * @throws IOException                  If {@code pathToOntology} could not be read.
     */
    private static final OWLOntology loadOntology(String pathToOntology) 
            throws OWLOntologyCreationException, IOException {
        log.entry(pathToOntology);
        ParserWrapper parserWrapper = new ParserWrapper();
        parserWrapper.setCheckOboDoc(true);
        return log.traceExit(parserWrapper.parse(pathToOntology));
    }
    
    
    //*********************************************
    //    INSTANCE ATTRIBUTES AND METHODS
    //*********************************************
    
    /**
     * An {@code OWLGraphWrapper} wrapping the CIO ontology to use.
     */
    private final OWLGraphWrapper wrapper;
    /**
     * An {@code OWLObjectProperty} retrieved using the OBO-like ID 
     * {@link #HAS_CONFIDENCE_ELEMENT_ID}.
     * @see #HAS_CONFIDENCE_ELEMENT_ID
     */
    private final OWLObjectProperty hasConfidenceElement;
    /**
     * An {@code OWLObjectProperty} retrieved using the OBO-like ID 
     * {@link #HAS_EVIDENCE_CONCORDANCE_ID}.
     * @see #HAS_EVIDENCE_CONCORDANCE_ID
     */
    private final OWLObjectProperty hasEvidenceConcordance;
    /**
     * An {@code OWLObjectProperty} retrieved using the OBO-like ID 
     * {@link #HAS_EVIDENCE_TYPE_CONCORDANCE_ID}.
     * @see #HAS_EVIDENCE_TYPE_CONCORDANCE_ID
     */
    private final OWLObjectProperty hasEvidenceTypeConcordance;
    /**
     * An {@code OWLObjectProperty} retrieved using the OBO-like ID 
     * {@link #HAS_CONFIDENCE_LEVEL_ID}.
     * @see #HAS_CONFIDENCE_LEVEL_ID
     */
    private final OWLObjectProperty hasConfidenceLevel;
    /**
     * An {@code OWLObjectProperty} retrieved using the OBO-like ID 
     * {@link #PROVIDES_GREATER_CONF_THAN_ID}.
     * @see #PROVIDES_GREATER_CONF_THAN_ID
     */
    private final OWLObjectProperty providesGreaterConfidenceThan;
    
    /**
     * An {@code OWLClass} retrieved using the OBO-like ID {@link #CONFIDENCE_STATEMENT_ID}. 
     * pre-loaded for use by methods of this class.
     * @see #CONFIDENCE_STATEMENT_ID
     */
    private final OWLClass confidenceStatement;
    /**
     * An {@code OWLClass} retrieved using the OBO-like ID {@link #REJECTED_STATEMENT_ID}. 
     * pre-loaded for use by methods of this class.
     * @see #REJECTED_STATEMENT_ID
     */
    private final OWLClass rejectedStatement;
    /**
     * An {@code OWLClass} retrieved using the OBO-like ID {@link #CONFIDENCE_ELEMENT_ID}. 
     * pre-loaded for use by methods of this class.
     * @see #CONFIDENCE_ELEMENT_ID
     */
    private final OWLClass confidenceElement;
    /**
     * An {@code OWLClass} retrieved using the OBO-like ID {@link #CONFIDENCE_LEVEL_ID}. 
     * pre-loaded for use by methods of this class.
     * @see #CONFIDENCE_LEVEL_ID
     */
    private final OWLClass confidenceLevel;
    /**
     * An {@code OWLClass} retrieved using the OBO-like ID {@link #EVIDENCE_CONCORDANCE_ID}. 
     * pre-loaded for use by methods of this class.
     * @see #EVIDENCE_CONCORDANCE_ID
     */
    private final OWLClass evidenceConcordance;
    /**
     * An {@code OWLClass} retrieved using the OBO-like ID {@link #SINGLE_EVIDENCE_CONCORDANCE_ID}. 
     * pre-loaded for use by methods of this class.
     * @see #SINGLE_EVIDENCE_CONCORDANCE_ID
     */
    private final OWLClass singleEvidenceConcordance;
    /**
     * An {@code OWLClass} retrieved using the OBO-like ID {@link #EVIDENCE_TYPE_CONCORDANCE_ID}. 
     * pre-loaded for use by methods of this class.
     * @see #EVIDENCE_TYPE_CONCORDANCE_ID
     */
    private final OWLClass evidenceTypeConcordance;

//    /**
//     * @see #getSingleEvidenceConf()
//     */
//    private final OWLClass singleEvidenceConf;
//    /**
//     * @see #getMultipleEvidenceConf()
//     */
//    private final OWLClass multipleEvidenceConf;
//    /**
//     * @see #getCongruentSameTypeEvidenceConf()
//     */
//    private final OWLClass congruentSameTypeEvidenceConf;
//    /**
//     * @see #getCongruentMultipleTypesEvidenceConf()
//     */
//    private final OWLClass congruentMultipleTypesEvidenceConf;
//    /**
//     * @see #getWeakConflictSameTypeEvidenceConf()
//     */
//    private final OWLClass weakConflictSameTypeEvidenceConf;
//    /**
//     * @see #getWeakConflictMultipleTypesEvidenceConf()
//     */
//    private final OWLClass weakConflictMultipleTypesEvidenceConf;
//    /**
//     * @see #getStrongConflictSameTypeEvidenceConf()
//     */
//    private final OWLClass strongConflictSameTypeEvidenceConf;
//    /**
//     * @see #getStrongConflictMultipleTypesEvidenceConf()
//     */
//    private final OWLClass strongConflictMultipleTypesEvidenceConf;
    

    
//    /**
//     * @see #getOrderedConfidenceLevels()
//     */
//    private final List<OWLClass> confidenceLevels;
//    /**
//     * @see #getEvidenceConcordances()
//     */
//    private final Set<OWLClass> evidenceConcordances;
//    /**
//     * @see #getEvidenceTypeConcordances()
//     */
//    private final Set<OWLClass> evidenceTypeConcordances;
//    
//    /**
//     * @see #getHighConfLevel()
//     */
//    private final OWLClass highConfLevel;
//    /**
//     * @see #getMediumConfLevel()
//     */
//    private final OWLClass mediumConfLevel;
//    /**
//     * @see #getLowConfLevel()
//     */
//    private final OWLClass lowConfLevel;
//    /**
//     * @see #getCongruentConcordance()
//     */
//    private final OWLClass congruentConcordance;
//    /**
//     * @see #getWeaklyConflictingConcordance()
//     */
//    private final OWLClass weaklyConflictingConcordance;
//    /**
//     * @see #getStronglyConflictingConcordance()
//     */
//    private final OWLClass stronglyConflictingConcordance;
//    
//    private final OWLClass sameTypeEvidenceConcordance;
//    private final OWLClass differentTypesEvidenceConcordance;
    
    /**
     * Constructor providing the path to the file storing the CIO ontology  
     * to use. It can be either obo or owl format.
     * 
     * @param pathToOntology    A {@code String} that is the path to the CIO ontology. 
     * @throws OWLOntologyCreationException If an error occurred while loading the ontology.
     * @throws IOException                  If {@code pathToOntology} could not be read.
     */
    public CIOWrapper(String pathToOntology) throws OWLOntologyCreationException, IOException {
        this(CIOWrapper.loadOntology(pathToOntology));
    }
    /**
     * Constructor providing the CIO {@code OWLOntology} used.
     * @param ontology  the {@code OWLOntology} that is the CIO ontology to use.
     */
    public CIOWrapper(OWLOntology ontology) { 
        this(new OWLGraphWrapper(ontology));
    }
    /**
     * Constructor providing the {@code OWLGraphWrapper} wrapping 
     * the {@code OWLOntology} which operations should be performed on.
     * 
     * @param wrapper   the {@code OWLGraphWrapper} wrapping the {@code OWLOntology} 
     *                  which operations should be performed on.
     * @throws IllegalArgumentException If {@code wrapper} does not allow to retrieve 
     *                                  all necessary information.
     */
    public CIOWrapper(final OWLGraphWrapper wrapper) throws IllegalArgumentException {
        this.wrapper = wrapper;
        
        this.hasConfidenceElement = this.wrapper.getOWLObjectPropertyByIdentifier(
                HAS_CONFIDENCE_ELEMENT_ID);
        this.hasEvidenceConcordance = this.wrapper.getOWLObjectPropertyByIdentifier(
                HAS_EVIDENCE_CONCORDANCE_ID);
        this.hasEvidenceTypeConcordance = this.wrapper.getOWLObjectPropertyByIdentifier(
                HAS_EVIDENCE_TYPE_CONCORDANCE_ID);
        this.hasConfidenceLevel = this.wrapper.getOWLObjectPropertyByIdentifier(
                HAS_CONFIDENCE_LEVEL_ID);
        this.providesGreaterConfidenceThan = this.wrapper.getOWLObjectPropertyByIdentifier(
                PROVIDES_GREATER_CONF_THAN_ID);
        if (this.hasConfidenceElement == null || 
                this.hasEvidenceConcordance == null || this.hasEvidenceTypeConcordance == null || 
                this.hasConfidenceLevel == null || this.providesGreaterConfidenceThan == null) {
            throw log.throwing(new IllegalArgumentException("The provided CIO does not allow "
                    + "to retrieve required relations. "
                    + "'has_confidence_element': " + this.hasConfidenceElement 
                    + " - 'has_evidence_concordance': " + this.hasEvidenceConcordance 
                    + " - 'has_evidence_type_concordance': " + this.hasEvidenceTypeConcordance 
                    + " - 'has_confidence_level': " + this.hasConfidenceLevel
                    + " - 'provides_greater_confidence_than': " + 
                    this.providesGreaterConfidenceThan));
        }
        
        //load the basic components of CIO used to pre-compose other terms
        this.confidenceStatement = this.wrapper.getOWLClassByIdentifierNoAltIds(
                CONFIDENCE_STATEMENT_ID);
        this.rejectedStatement = this.wrapper.getOWLClassByIdentifierNoAltIds(
                REJECTED_STATEMENT_ID);
        this.confidenceElement = this.wrapper.getOWLClassByIdentifierNoAltIds(
                CONFIDENCE_ELEMENT_ID);
        this.confidenceLevel = this.wrapper.getOWLClassByIdentifierNoAltIds(
                CONFIDENCE_LEVEL_ID);
        this.evidenceConcordance = this.wrapper.getOWLClassByIdentifierNoAltIds(
                EVIDENCE_CONCORDANCE_ID);
        this.singleEvidenceConcordance = this.wrapper.getOWLClassByIdentifierNoAltIds(
                SINGLE_EVIDENCE_CONCORDANCE_ID);
        this.evidenceTypeConcordance = this.wrapper.getOWLClassByIdentifierNoAltIds(
                EVIDENCE_TYPE_CONCORDANCE_ID);
        if (this.confidenceStatement == null || this.rejectedStatement == null || 
                this.confidenceElement == null || 
                this.confidenceLevel == null || this.evidenceConcordance == null || 
                this.singleEvidenceConcordance == null || 
                this.evidenceTypeConcordance == null) {
            throw log.throwing(new IllegalArgumentException("The ontology used does not allow " + 
                "to retrieve necessary terms. " + 
                "Confidence statement: " + this.confidenceStatement + 
                " - Rejected statement: " + this.rejectedStatement + 
                " - Confidence element: " + this.confidenceElement + 
                " - Confidence level: " + this.confidenceLevel + 
                " - Evidence concordance: " + this.evidenceConcordance + 
                " - Single evidence concordance: " + this.singleEvidenceConcordance + 
                " - Evidence type concordance: " + this.evidenceTypeConcordance));
        }
        
//        
//        
//        //now, retrieve the confidence information elements, classified as 'confidence level', 
//        //'evidence concordance', and 'evidence type concordance'
//        log.trace("Loading confidence elements...");
//        List<OWLClass> confidenceLevels = new ArrayList<OWLClass>();
//        Set<OWLClass> evidenceConcordances = new HashSet<OWLClass>();
//        Set<OWLClass> evidenceTypeConcordances = new HashSet<OWLClass>();
//        for (OWLClass cls: this.wrapper.getAllRealOWLClasses()) {
//            log.trace("Iterating {}", cls);
//            Set<OWLClass> ancestors = this.wrapper.getAncestorsThroughIsA(cls);
//            if (ancestors.contains(this.getConfidenceStatement()) || 
//                    cls.equals(this.getConfidenceStatement())) {
//                log.trace("Is confidence statement, skip.");
//                continue;
//            }
//            if (cls.equals(this.getConfidenceElement()) || 
//                    cls.equals(this.getConfidenceLevel()) || 
//                    cls.equals(this.getEvidenceConcordance()) || 
//                    cls.equals(this.getEvidenceTypeConcordance())) {
//                log.trace("Is a base confidence information element, skip.");
//                continue;
//            }
//            
//            boolean assigned = false;
//            if (ancestors.contains(this.getConfidenceLevel())) {
//                log.trace("Is a confidence level");
//                confidenceLevels.add(cls);
//                assigned = true;
//            } 
//            if (ancestors.contains(this.getEvidenceConcordance())) {
//                if (assigned) {
//                    throw log.throwing(new IllegalArgumentException(
//                            "Class assigned to multiple elements of same type: " + cls));
//                }
//                log.trace("Is an evidence concordance");
//                evidenceConcordances.add(cls);
//                assigned = true;
//            } 
//            if (ancestors.contains(this.getEvidenceTypeConcordance())) {
//                if (assigned) {
//                    throw log.throwing(new IllegalArgumentException(
//                            "Class assigned to multiple elements of same type: " + cls));
//                }
//                log.trace("Is an evidence type concordance");
//                evidenceTypeConcordances.add(cls);
//                assigned = true;
//            } 
//            
//            if (!assigned) {
//                throw log.throwing(new IllegalArgumentException("Term not supported: " + cls));
//            }
//        }
//        if (confidenceLevels.isEmpty() || evidenceConcordances.isEmpty() || 
//                evidenceTypeConcordances.isEmpty()) {
//            throw log.throwing(new IllegalArgumentException("The CIO provided does not allow " + 
//                    "to retrieve necessary terms. " + 
//                    "Confidence levels: " + confidenceLevels + 
//                    " - Evidence concordance terms: " + evidenceConcordances + 
//                    " - Evidence type concordance terms: " + evidenceTypeConcordances));
//        }
//        
//        
//        //now we order the confidence levels.
//        OWLObjectProperty greaterConfThan = 
//                this.wrapper.getOWLObjectPropertyByIdentifier(GREATER_CONF_THAN_ID);
//        if (greaterConfThan == null) {
//            throw log.throwing(new IllegalArgumentException("The CIO provided does not allow "
//                    + "to retrieve the relation ordering confidence level."));
//        }
//        final Set<OWLPropertyExpression> greaterConfThanRels = new HashSet<OWLPropertyExpression>();
//        greaterConfThanRels.add(greaterConfThan);
//        Collections.sort(confidenceLevels, new Comparator<OWLClass>() {
//            @Override
//            public int compare(OWLClass o1, OWLClass o2) {
//                for (OWLGraphEdge edge: wrapper.getOutgoingEdgesClosure(o1, greaterConfThanRels)) {
//                    if (edge.getTarget().equals(o2)) {
//                        return log.traceExit(1);
//                    }
//                }
//                for (OWLGraphEdge edge: wrapper.getOutgoingEdgesClosure(o2, greaterConfThanRels)) {
//                    if (edge.getTarget().equals(o1)) {
//                        return log.traceExit(-1);
//                    }
//                }
//                throw log.throwing(new IllegalArgumentException("The CIO provided does not allow "
//                        + "to order some confidence levels: " + o1 + " - " + o2));
//            }
//        });
//        
//        
//        //assign confidence information elements
//        this.confidenceLevels = Collections.unmodifiableList(confidenceLevels);
//        log.trace("Retrieved ordered confidence levels: {}", this.confidenceLevels);
//        this.evidenceConcordances = Collections.unmodifiableSet(evidenceConcordances);
//        log.trace("Retrieved evidence concordances: {}", this.evidenceConcordances);
//        this.evidenceTypeConcordances = Collections.unmodifiableSet(evidenceTypeConcordances);
//        log.trace("Retrieved evidence type concordances: {}", this.evidenceTypeConcordances);
//        
//        log.trace("Done loading confidence elements.");
//        
//
//        
//        //now, we wrap and store all OWLClasses in CIOTerms
//        Map<OWLClass, CIOTerm> classesTOCIOTerm = new HashMap<OWLClass, CIOTerm>();
//        boolean hasStatement = false;
//        for (OWLClass cls: this.wrapper.getAllRealOWLClasses()) {
//            CIOTerm cioTerm = new CIOTerm(cls, this);
//            if (cioTerm.isConfidenceStatement()) {
//                hasStatement = true;
//            }
//            classesTOCIOTerm.put(cls, cioTerm);
//        }
//        if (!hasStatement) {
//            throw log.throwing(new IllegalArgumentException("The CIO provided does not allow "
//                    + "to retrieve any 'confidence information statement'."));
//        }
//        this.cioTerms = Collections.unmodifiableMap(classesTOCIOTerm);
//        
//        
//        
//        
//        this.singleEvidenceConf = this.wrapper.getOWLClassByIdentifierNoAltIds(
//                SINGLE_EVIDENCE_CONF_ID);
//        this.multipleEvidenceConf = this.wrapper.getOWLClassByIdentifierNoAltIds(
//                MULTIPLE_EVIDENCE_CONF_ID);
//        this.congruentSameTypeEvidenceConf = this.wrapper.getOWLClassByIdentifierNoAltIds(
//                CONGRUENT_SAME_TYPE_EVIDENCE_CONF_ID);
//        this.congruentMultipleTypesEvidenceConf = this.wrapper.getOWLClassByIdentifierNoAltIds(
//                CONGRUENT_MULTIPLE_TYPES_EVIDENCE_CONF_ID);
//        this.weakConflictSameTypeEvidenceConf = this.wrapper.getOWLClassByIdentifierNoAltIds(
//                WEAK_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID);
//        this.weakConflictMultipleTypesEvidenceConf = this.wrapper.getOWLClassByIdentifierNoAltIds(
//                WEAK_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID);
//        this.strongConflictSameTypeEvidenceConf = this.wrapper.getOWLClassByIdentifierNoAltIds(
//                STRONG_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID);
//        this.strongConflictMultipleTypesEvidenceConf = this.wrapper.getOWLClassByIdentifierNoAltIds(
//                STRONG_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID);
//        if (this.singleEvidenceConf == null || this.multipleEvidenceConf == null || 
//                this.congruentSameTypeEvidenceConf == null || 
//                this.congruentMultipleTypesEvidenceConf == null || 
//                this.weakConflictSameTypeEvidenceConf == null || 
//                this.weakConflictMultipleTypesEvidenceConf == null || 
//                this.strongConflictSameTypeEvidenceConf == null || 
//                this.strongConflictMultipleTypesEvidenceConf == null) {
//            throw log.throwing(new IllegalArgumentException("The ontology used does not allow "
//                + "to retrieve some branches. "
//                + "Single evidence: " + this.singleEvidenceConf + 
//                " - Multiple evidence lines: " + this.multipleEvidenceConf + 
//                " - Congruent same type evidence lines: " + this.congruentSameTypeEvidenceConf + 
//                " - Congruent multiple types evidence lines: " + 
//                    this.congruentMultipleTypesEvidenceConf + 
//                " - Weakly conflicting evidence lines of same type: " + 
//                    this.weakConflictSameTypeEvidenceConf + 
//                " - Weakly conflicting evidence lines of multiple types: " + 
//                    this.weakConflictMultipleTypesEvidenceConf + 
//                " - Strongly conflicting evidence lines of same type: " + 
//                    this.strongConflictSameTypeEvidenceConf + 
//                " - Strongly conflicting evidence lines of multiple types: " + 
//                    this.strongConflictMultipleTypesEvidenceConf));
//        }
//        
//        
//        this.highConfLevel = this.wrapper.getOWLClassByIdentifierNoAltIds(HIGH_CONF_LEVEL_ID);
//        this.mediumConfLevel = this.wrapper.getOWLClassByIdentifierNoAltIds(MEDIUM_CONF_LEVEL_ID);
//        this.lowConfLevel = this.wrapper.getOWLClassByIdentifierNoAltIds(LOW_CONF_LEVEL_ID);
//        this.rejectedTerm = this.wrapper.getOWLClassByIdentifierNoAltIds(REJECTED_TERM_ID);
//        if (this.highConfLevel == null || this.mediumConfLevel == null || 
//                this.lowConfLevel == null || this.rejectedTerm == null) {
//            throw log.throwing(new IllegalArgumentException("The ontology used does not allow "
//                    + "to retrieve confidence levels. High conf: " + this.highConfLevel + 
//                    " - Medium conf: " + this.mediumConfLevel + 
//                    " - Low conf: " + this.lowConfLevel + " - Rejected: " + this.rejectedTerm));
//        }
//        
//        this.congruentConcordance = this.wrapper.getOWLClassByIdentifierNoAltIds(
//                CONGRUENT_CONCORDANCE_ID);
//        this.weaklyConflictingConcordance = this.wrapper.getOWLClassByIdentifierNoAltIds(
//                WEAKLY_CONFLICTING_CONCORDANCE_ID);
//        this.stronglyConflictingConcordance = this.wrapper.getOWLClassByIdentifierNoAltIds(
//                STRONGLY_CONFLICTING_CONCORDANCE_ID);
//        if (this.congruentConcordance == null || this.weaklyConflictingConcordance == null || 
//                this.stronglyConflictingConcordance == null) {
//            throw log.throwing(new IllegalArgumentException("The ontology used does not allow "
//                + "to retrieve concordance levels. "
//                + "Congruent concordance: " + this.congruentConcordance + 
//                " - Weakly conflicting concordance: " + this.weaklyConflictingConcordance + 
//                " - Strongly conflicting concordance: " + this.stronglyConflictingConcordance));
//        }
//        
//        this.sameTypeEvidenceConcordance = this.wrapper.getOWLClassByIdentifierNoAltIds(
//                SAME_TYPE_EVIDENCE_CONCORDANCE_ID);
//        this.differentTypesEvidenceConcordance = this.wrapper.getOWLClassByIdentifierNoAltIds(
//                DIFFERENT_TYPES_EVIDENCE_CONCORDANCE_ID);
//        if (this.sameTypeEvidenceConcordance == null || 
//                this.differentTypesEvidenceConcordance == null) {
//            throw log.throwing(new IllegalArgumentException("The ontology used does not allow "
//                + "to retrieve 'same type' and 'different types' concordance levels. "
//                + "Same type concordance: " + this.sameTypeEvidenceConcordance + 
//                " - Different types concordance: " + this.differentTypesEvidenceConcordance));
//        }
    }

//    /**
//     * Retrieve all {@code OWLClass}es from the ontology wrapped that have 
//     * all {@code OWLClass}es in {@code ancestors} as ancestors by is_a relations 
//     * (is_a relations are inferred by owltools, so this includes EquivalentClasses axiom, etc.). 
//     * This is a bit similar to retrieving classes equivalent to the intersection of 
//     * {@code ancestors}. {@code ancestors} will not be included in the returned {@code Set}
//     * 
//     * @param ancestors     A {@code Set} of {@code OWLClass}es defining all the ancestors 
//     *                      that {@code OWLClass}es to retrieve must have.
//     * @return              A {@code Set} containing the {@code OWLClass}es that have all 
//     *                      {@code ancestors} as is_a ancestors. 
//     *                      Will not contain {@code ancestors} themselves.
//     */
//    private Set<OWLClass> getIntersectingClasses(Set<OWLClass> ancestors) {
//        log.entry(ancestors);
//        
//        Set<OWLClass> intersectingClasses = new HashSet<OWLClass>();
//        for (OWLClass cls: this.wrapper.getAllRealOWLClasses()) {
//            Set<OWLClass> relatedClasses = new HashSet<OWLClass>();
//            relatedClasses.add(cls);
//            relatedClasses.addAll(this.wrapper.getAncestorsThroughIsA(cls));
//            if (relatedClasses.containsAll(ancestors)) {
//                intersectingClasses.add(cls);
//            }
//            
//        }
//        return log.traceExit(intersectingClasses);
//    }
    
    /**
     * Determines whether {@code cls} is part if the 'confidence information statement' branch, 
     * containing terms usable for annotations.
     * 
     * @param cls   An {@code OWLClass} for which we want to determine whether it is 
     *              a 'confidence information statement'.
     * @return      {@code true} if {@code cls} is a 'confidence information statement', 
     *              {@code false} otherwise.
     */
    public boolean isConfidenceStatement(OWLClass cls) {
        log.entry(cls);
        return log.traceExit(this.isMemberOfBranch(cls, this.confidenceStatement));
    }
    /**
     * Determines whether {@code cls} is part if the 'confidence information element' branch, 
     * containing terms used to compose CI statements.
     * 
     * @param cls   An {@code OWLClass} for which we want to determine whether it is 
     *              a 'confidence information element'.
     * @return      {@code true} if {@code cls} is a 'confidence information element', 
     *              {@code false} otherwise.
     */
    public boolean isConfidenceElement(OWLClass cls) {
        log.entry(cls);
        return log.traceExit(this.isMemberOfBranch(cls, this.confidenceElement));
    }
    /**
     * Determines whether {@code cls} is of type 'evidence concordance'.
     * 
     * @param cls   An {@code OWLClass} for which we want to determine whether it is 
     *              an 'evidence concordance'.
     * @return      {@code true} if {@code cls} is an 'evidence concordance', 
     *              {@code false} otherwise.
     * @see #EVIDENCE_CONCORDANCE_ID
     */
    public boolean isEvidenceConcordance(OWLClass cls) {
        log.entry(cls);
        return log.traceExit(this.isMemberOfBranch(cls, this.evidenceConcordance));
    }
    /**
     * Determines whether {@code cls} is a type of evidence concordance 'single evidence'.
     * 
     * @param cls   An {@code OWLClass} for which we want to determine whether it is 
     *              a 'single evidence' concordance.
     * @return      {@code true} if {@code cls} is a 'single evidence' concordance, 
     *              {@code false} otherwise.
     * @see #SINGLE_EVIDENCE_CONCORDANCE_ID
     */
    public boolean isSingleEvidenceConcordance(OWLClass cls) {
        log.entry(cls);
        return log.traceExit(this.isMemberOfBranch(cls, this.singleEvidenceConcordance));
    }
    /**
     * Determines whether {@code cls} is of type 'evidence type concordance'.
     * 
     * @param cls   An {@code OWLClass} for which we want to determine whether it is 
     *              an 'evidence type concordance'.
     * @return      {@code true} if {@code cls} is an 'evidence type concordance', 
     *              {@code false} otherwise.
     * @see #EVIDENCE_TYPE_CONCORDANCE_ID
     */
    public boolean isEvidenceTypeConcordance(OWLClass cls) {
        log.entry(cls);
        return log.traceExit(this.isMemberOfBranch(cls, this.evidenceTypeConcordance));
    }
    /**
     * Determines whether {@code cls} is of type 'confidence level'.
     * 
     * @param cls   An {@code OWLClass} for which we want to determine whether it is 
     *              a 'confidence level'.
     * @return      {@code true} if {@code cls} is an 'confidence level', 
     *              {@code false} otherwise.
     * @see #CONFIDENCE_LEVEL_ID
     */
    public boolean isConfidenceLevel(OWLClass cls) {
        log.entry(cls);
        return log.traceExit(this.isMemberOfBranch(cls, this.confidenceLevel));
    }
    /**
     * Determines whether {@code cls} is a subclass of {@code branchRoot} 
     * or equal to {@code branchRoot}.
     * 
     * @param cls           An {@code OWLClass} for which we want to determine whether 
     *                      it belongs to the branch with root {@code branchRoot}.
     * @param branchRoot    An {@code OWLClass} that is the root of the branch to be tested.
     * @return              {@code true} if {@code cls} belongs to the branch having as root 
     *                      {@code branchRoot}, {@code false} otherwise.
     */
    private boolean isMemberOfBranch(OWLClass cls, OWLClass branchRoot) {
        log.entry(cls, branchRoot);
        if (branchRoot.equals(cls) || 
                this.wrapper.getAncestorsThroughIsA(cls).contains(branchRoot)) {
            return log.traceExit(true);
        }
        return log.traceExit(false);
    }

    /**
     * Determines whether {@code cls} corresponds to the CI statement 'rejected'.
     * 
     * @param cls   An {@code OWLClass} for which we want to determine whether it is 
     *              the 'rejected' term.
     * @return      {@code true} if {@code cls} is the 'rejected' term, 
     *              {@code false} otherwise.
     * @see #REJECTED_STATEMENT_ID
     */
    public boolean isRejectedStatement(OWLClass cls) {
        log.entry(cls);
        return log.traceExit(this.rejectedStatement.equals(cls));
    }
    
    /**
     * Returns the 'confidence level' associated to {@code cls}. The returned {@code OWLClass} 
     * is associated to {@code cls} through the relation 'has confidence level' 
     * (see {@link #HAS_CONFIDENCE_LEVEL_ID}). 
     * 
     * @param cls   An {@code OWLClass} for which we want to retrieve the associated 
     *              'confidence level' term.
     * @return      An {@code OWLClass} that is the 'confidence level' term associated to 
     *              {@code cls}. 
     * @throws IllegalArgumentException If {@code cls} is not a CI statement.
     * @throws IllegalStateException    If the ontology wrapped allowed to retrieve more than 
     *                                  one confidence level associated, or an invalid 
     *                                  confidence level. 
     */
    public OWLClass getConfidenceLevel(OWLClass cls) throws IllegalArgumentException, 
    IllegalStateException {
        log.entry(cls);
        return log.traceExit(this.getConfidenceElement(cls, this.hasConfidenceLevel, 
                this.confidenceLevel));
    }
    /**
     * Determine whether the confidence level associated to {@code cls} is usable 
     * for annotation. It means that the confidence level associated to 
     * {@code cls} is not the 'confidence level' term itself, but one of its subclass, 
     * usable for annotation (for instance 'high confidence').
     * 
     * @param cls   An {@code OWLClass} for which we want to check the confidence level.
     * @return      {@code true} if the confidence level of {@code cls} is a leaf of 
     *              the 'confidence level' branch, {@code false} otherwise.
     */
    public boolean hasLeafConfidenceLevel(OWLClass cls) {
        log.entry(cls);
        
        if (this.getConfidenceLevels().contains(this.getConfidenceLevel(cls))) {
            return log.traceExit(true);
        }
        return log.traceExit(false);
    }
    /**
     * Determine whether {@code cls} is one of the 'strongly conflicting' terms. 
     * This means that the method {@link #getConfidenceLevel(OWLClass)} would return 
     * {@code null} for {@code cls} (strongly conflicting statements are not associated to 
     * any confidence level).
     * 
     * @param cls   An {@code OWLClass} for which we want to determine whether it is 
     *              a strongly conflicting statement.
     * @return      {@code true} if {@code cls} is a strongly conflicting statement, 
     *              {@code false} otherwise.
     */
    public boolean isStronglyConflicting(OWLClass cls) {
        log.entry(cls);
        if (STRONGLY_CONFLICTING_CONCORDANCE_ID.equals(
                this.getOWLGraphWrapper().getIdentifier(this.getEvidenceConcordance(cls)))) {
            return log.traceExit(true);
        }
        return log.traceExit(false);
    }
    /**
     * Returns the 'evidence concordance' associated to {@code cls}. The returned {@code OWLClass} 
     * is associated to {@code cls} through the relation 'has evidence concordance' 
     * (see {@link #HAS_EVIDENCE_CONCORDANCE_ID}). 
     * All CI statements should always be associated to an 'evidence concordance'.
     * 
     * @param cls   An {@code OWLClass} for which we want to retrieve the associated 
     *              'evidence concordance' term.
     * @return      An {@code OWLClass} that is the 'evidence concordance' term associated to 
     *              {@code cls}. {@code null} if {@code cls} is not associated to 
     *              any 'evidence concordance' term.
     * @throws IllegalArgumentException If {@code cls} is not a CI statement.
     * @throws IllegalStateException    If the ontology wrapped allowed to retrieve more than 
     *                                  one evidence concordance associated, or an invalid 
     *                                  evidence concordance. 
     */
    public OWLClass getEvidenceConcordance(OWLClass cls) throws IllegalArgumentException, 
    IllegalStateException {
        log.entry(cls);
        
        OWLClass evidenceConcordance = this.getConfidenceElement(cls, 
                this.hasEvidenceConcordance, this.evidenceConcordance);
        //All CI statements should always be associated to an evidence concordance
        if (evidenceConcordance == null) {
            throw log.throwing(new IllegalStateException(cls + 
                    " is not associated to any evidence concordance, all CI statements should be."));
        }
        
        return log.traceExit(evidenceConcordance);
    }
    /**
     * Returns the 'evidence type concordance' associated to {@code cls}. The returned 
     * {@code OWLClass} is associated to {@code cls} through the relation 
     * 'has evidence type concordance' (see {@link #HAS_EVIDENCE_TYPE_CONCORDANCE_ID}). 
     * 
     * @param cls   An {@code OWLClass} for which we want to retrieve the associated 
     *              'evidence type concordance' term.
     * @return      An {@code OWLClass} that is the 'evidence type concordance' term associated to 
     *              {@code cls}. 
     * @throws IllegalArgumentException If {@code cls} is not a CI statement.
     * @throws IllegalStateException    If the ontology wrapped allowed to retrieve more than 
     *                                  one evidence type concordance associated, or an invalid 
     *                                  evidence type concordance. 
     */
    public OWLClass getEvidenceTypeConcordance(OWLClass cls) throws IllegalArgumentException, 
    IllegalStateException {
        log.entry(cls);
        return log.traceExit(this.getConfidenceElement(cls, 
                this.hasEvidenceTypeConcordance, this.evidenceTypeConcordance));
    }
    /**
     * Retrieve the confidence element associated to {@code cls} through {@code relation}. 
     * 
     * @param cls       An {@code OWLClass} being a CI statement, for which we want 
     *                  to retrieve a CI element associated through {@code relation}.
     * @param relation  An {@code OWLObjectProperty} that should be a sub-property 
     *                  of 'has confidence element', used to retrieve the appropriate 
     *                  CI element associated to {@code cls}.
     * @param expectedAncestor  An {@code OWLClass} that is the expected ancestor 
     *                          of the confidence element retrieved, if any is retrieved. 
     *                          This is used as a control.
     * @return          An {@code OWLClass} that is the CI element associated to {@code cls} 
     *                  through {@code relation}. {@code null} if {@code cls} was associated to 
     *                  no CI elements through {@code relation}.
     * @throws IllegalArgumentException If {@code cls} is not a CI statement, or {@code relation} 
     *                                  is not a 'has confidence element' relation type. 
     * @throws IllegalStateException    If the ontology wrapped allowed to retrieve more than 
     *                                  one CI element through {@code relation}, or the target 
     *                                  of the relation used is not a CI element, or is not 
     *                                  a descendants of {@code expectedAncestor}. 
     */
    private OWLClass getConfidenceElement(OWLClass cls, OWLObjectProperty relation, 
            OWLClass expectedAncestor) throws IllegalArgumentException, 
            IllegalStateException {
        log.entry(cls, relation, expectedAncestor);
        
        //check that cls is a CI statement
        if (!this.isConfidenceStatement(cls)) {
            throw log.throwing(new IllegalArgumentException(cls + " is not a CI statement."));
        }
        //check that relation is a 'has confidence element' type of relation, 
        //without being the 'has confidence element' relation itself
        if (!this.wrapper.getSuperPropertyClosureOf(relation).contains(
                this.hasConfidenceElement)) {
            throw log.throwing(new IllegalArgumentException("The provided relation "
                    + "is not a sub-property of 'has_confidence_element'"));
        }
        
        //retrieve associated CI element.
        
        //in order to store the target of edges
        Set<OWLClass> confElements = new HashSet<OWLClass>();
        //we check outgoing edges in two passes: first we check direct outgoing edges, 
        //and if no appropriate edge was found, we check also indirect outgoing edges.
        //This is to improve performances.
        int i = 0;
        while (i < 2) {
            Set<OWLGraphEdge> edges = null;
            if (i == 0) {
                //first pass
                edges = this.wrapper.getOutgoingEdges(cls);
            } else {
                //second pass
                edges = this.wrapper.getOutgoingEdgesClosure(cls);
            }
            for (OWLGraphEdge edge: edges) {
                //filter correct edges
                if (edge.getTarget() instanceof OWLClass && 
                        //only relations with successful composition
                        edge.getQuantifiedPropertyList().size() == 1 && 
                        //only objectAllValuesFrom and objectSomeValuesFrom accepted
                        (edge.getFirstQuantifiedProperty().isAllValuesFrom() || 
                                edge.getFirstQuantifiedProperty().isSomeValuesFrom()) && 
                        //and with the requested relation
                        relation.equals(edge.getFirstQuantifiedProperty().getProperty())) {
                    
                    log.trace("Edge used to retrieve CI element: {}", edge);
                    confElements.add((OWLClass) edge.getTarget());
                }
            }
            //if we found elements at the first pass, no need for a second pass.
            if (!confElements.isEmpty()) {
                break;
            }
            i++;
        }
        //keep only the most precises CI element, only if necessary, to improve performances.
        if (confElements.size() > 1) {
            Set<OWLClass> confElementAncestors = new HashSet<OWLClass>();
            for (OWLClass confElement: confElements) {
                confElementAncestors.addAll(this.wrapper.getAncestorsThroughIsA(confElement));
            }
            confElements.removeAll(confElementAncestors);
        }
        log.trace("Confidence elements retrieved: {}", confElements);
        
        //only one precise CI element at most should be retrieved for a given relation
        if (confElements.size() > 1) {
            throw log.throwing(new IllegalStateException(cls + " is associated to "
                    + "more than one confidence element through relation " + relation 
                    + ": " + confElements));
        }
        if (confElements.size() == 0) {
            return log.traceExit((OWLClass) null);
        }
        
        //check validity of CI element retrieved
        OWLClass element = confElements.iterator().next();
        if (!this.isConfidenceElement(element) || 
            !this.isMemberOfBranch(element, expectedAncestor)) {
            throw log.throwing(new IllegalStateException(cls + " is associated to "
                    + "an incorrect confidence element through relation " + relation 
                    + ": " + element + " - expected ancestor: " + expectedAncestor));
        }
        
        return log.traceExit(element);
    }
    
    /**
     * Determines whether {@code cls} is a CI statement considered as not-trusted 
     * by the Bgee team. 
     * 
     * @param cls   An {@code OWLClass} for which we want to know whether it is a CI statement 
     *              not trusted by the Bgee team.
     * @return      {@code true} if {@code cls} is not trusted, {@code false} otherwise.
     * @throws IllegalArgumentException If {@code cls} is not a CI statement (see 
     *                                  {@link #isConfidenceStatement(OWLClass)}).
     */
    public boolean isBgeeNotTrustedStatement(OWLClass cls) throws IllegalArgumentException {
        log.entry(cls);
        if (!this.isConfidenceStatement(cls)) {
            throw log.throwing(new IllegalArgumentException("Only CI statements can be trusted."));
        }
        return log.traceExit(this.wrapper.isOWLObjectInSubsets(cls, 
                Arrays.asList(BGEE_NOT_TRUSTED_SUBSET)));
    }
    
    /**
     * Determines the best confidence term with level information among {@code cioTerms}. 
     * The {@code OWLClass}es in {@code cioTerms} must meet the following conditions: 
     * <ul>
     * <li>They must be confidence information statements ({@link #isConfidenceStatement(OWLClass)} 
     * returns {@code true}).
     * <li>They must all be part of the same branch (meaning, they must have equal 
     * evidence concordance and evidence type concordance, see {@link 
     * #getEvidenceConcordance(OWLClass)} and {@link #getEvidenceTypeConcordance(OWLClass)}).
     * <li>They must all represent a confidence level (meaning, {@link 
     * #getConfidenceLevel(OWLClass)} must return a value not {@code null}, contained in 
     * the collection returned by {@link #getConfidenceLevels()}).
     * </ul>
     * Otherwise, an {@code IllegalArgumentException} is thrown. 
     * <p>
     * This means in practice that {@code OWLClass}es in {@code cioTerms} must be one 
     * of the pre-composed terms usable for annotations, leaves of the ontology,  
     * associated to a confidence level term.
     * 
     * @param cioTerms  A {@code Collection} of {@code OWLClass}es providing confidence level 
     *                  information, from a same branch, for which we want to determine 
     *                  the best one. 
     * @return          An {@code OWLClass} that is the best confidence among {@code cioTerms}.
     * @throws IllegalArgumentException If {@code cioTerms} is empty, or 
     *                                  if the {@code OWLClass}es in {@code cioTerms} do not 
     *                                  all belong to a same branch, or do not all provide 
     *                                  confidence level information.
     * @see #getLowestTermWithConfidenceLevel(Collection)
     */
    public OWLClass getBestTermWithConfidenceLevel(Collection<OWLClass> cioTerms) {
        log.entry(cioTerms);
        return log.traceExit(this.getBestOrLowestTermWithConfidenceLevel(cioTerms, true));
    }
    
    /**
     * Determines the lowest confidence term with level information among {@code cioTerms}. 
     * The {@code OWLClass}es in {@code cioTerms} must meet the following conditions: 
     * <ul>
     * <li>They must be confidence information statements ({@link #isConfidenceStatement(OWLClass)} 
     * returns {@code true}).
     * <li>They must all be part of the same branch (meaning, they must have equal 
     * evidence concordance and evidence type concordance, see {@link 
     * #getEvidenceConcordance(OWLClass)} and {@link #getEvidenceTypeConcordance(OWLClass)}).
     * <li>They must all represent a confidence level (meaning, {@link 
     * #getConfidenceLevel(OWLClass)} must return a value not {@code null}, contained in 
     * the collection returned by {@link #getConfidenceLevels()}).
     * </ul>
     * Otherwise, an {@code IllegalArgumentException} is thrown. 
     * <p>
     * This means in practice that {@code OWLClass}es in {@code cioTerms} must be one 
     * of the pre-composed terms usable for annotations, leaves of the ontology,  
     * associated to a confidence level term.
     * 
     * @param cioTerms  A {@code Collection} of {@code OWLClass}es providing confidence level 
     *                  information, from a same branch, for which we want to determine 
     *                  the lowest one. 
     * @return          An {@code OWLClass} that is the lowest confidence among {@code cioTerms}.
     * @throws IllegalArgumentException If {@code cioTerms} is empty, or 
     *                                  if the {@code OWLClass}es in {@code cioTerms} do not 
     *                                  all belong to a same branch, or do not all provide 
     *                                  confidence level information.
     * @see #getBestTermWithConfidenceLevel(Collection)
     */
    public OWLClass getLowestTermWithConfidenceLevel(Collection<OWLClass> cioTerms) {
        log.entry(cioTerms);
        return log.traceExit(this.getBestOrLowestTermWithConfidenceLevel(cioTerms, false));
    }
    
    /**
     * Determines either the best or the lowest confidence term with level information 
     * among {@code cioTerms}. This method is called by 
     * {@link #getBestTermWithConfidenceLevel(Collection)} and 
     * {@link #getLowestTermWithConfidenceLevel(Collection)}, see these methods 
     * for more details.
     * 
     * @param cioTerms  A {@code Collection} of {@code OWLClass}es providing confidence level 
     *                  information, from a same branch, for which we want to determine 
     *                  the best one. 
     * @param bestTerm  A {@code boolean} determining whether the best or the loewst term 
     *                  should be retrieved. If {@code true}, the best term is retrieved, 
     *                  otherwise, the lowest is retrieved.
     * @return          An {@code OWLClass} that is the best or the lowest confidence 
     *                  among {@code cioTerms}, depending on {@code bestTerm}.
     * @throws IllegalArgumentException If {@code cioTerms} is empty, or 
     *                                  if the {@code OWLClass}es in {@code cioTerms} do not 
     *                                  all belong to a same branch, or do not all provide 
     *                                  confidence level information.
     */
    private OWLClass getBestOrLowestTermWithConfidenceLevel(Collection<OWLClass> cioTerms, 
            boolean bestTerm) throws IllegalArgumentException {
        log.entry(cioTerms, bestTerm);
    
        //check pre-conditions
        if (cioTerms.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The provided Collection "
                    + "of OWLClasses to be ordered based on confidence levels is empty."));
        }
        Set<OWLClass> evidenceConcordances = new HashSet<OWLClass>();
        Set<OWLClass> evidenceTypeConcordances = new HashSet<OWLClass>();
        for (OWLClass cls: cioTerms) {
            if (!this.isConfidenceStatement(cls)) {
                throw log.throwing(new IllegalArgumentException(cls + " is not a confidence "
                        + "information statement, cannot identify best confidence term."));
            }
            if (!this.hasLeafConfidenceLevel(cls)) {
                throw log.throwing(new IllegalArgumentException(cls
                        + " is not associated to a valid confidence level, cannot be ordered."));
            }
            evidenceConcordances.add(this.getEvidenceConcordance(cls));
            evidenceTypeConcordances.add(this.getEvidenceTypeConcordance(cls));
        }
        if (evidenceConcordances.size() > 1 || evidenceTypeConcordances.size() > 1) {
            throw log.throwing(new IllegalArgumentException("The provided CIO terms "
                    + "are not all part of the same branch. Evidence concordances: "
                    +  evidenceConcordances + " - Evidence type concordances: "
                    + evidenceTypeConcordances + " - classes examined: " + cioTerms));
        }
        //evidenceConcordances and evidenceTypeConcordances accepts null values, 
        //so there should always be an element in them.
        assert evidenceConcordances.size() == 1 && evidenceTypeConcordances.size() == 1;
        
        //order CIO terms by confidence levels
        List<OWLClass> orderedCIOTerms = new ArrayList<OWLClass>(cioTerms);
        final List<OWLClass> orderedConfLevels = this.getOrderedConfidenceLevels();
        final CIOWrapper wrapper = this;
        Collections.sort(orderedCIOTerms, new Comparator<OWLClass>() {
            @Override
            public int compare(OWLClass o1, OWLClass o2) {
                log.entry(o1, o2);
                return log.traceExit(orderedConfLevels.indexOf(wrapper.getConfidenceLevel(o1)) - 
                        orderedConfLevels.indexOf(wrapper.getConfidenceLevel(o2))); 
            }
        });
        
        if (bestTerm) {
            return log.traceExit(orderedCIOTerms.get(orderedCIOTerms.size() - 1));
        } 
        return log.traceExit(orderedCIOTerms.get(0));
    }
    
    /**
     * Returns an {@code OWLClass} from the branch 'confidence statement from single evidence' 
     * associated to the 'confidence level' term {@code confidenceLevel}. The returned 
     * {@code OWLClass} is of type 'confidence information statement' (@link 
     * #isConfidenceStatement(OWLClass) returns {@code true}). {@code confidenceLevel} 
     * must be one of the 'confidence level' terms returned by {@link #getConfidenceLevels()} 
     * otherwise, an {@code IllegalArgumentException} is thrown.
     * 
     * @param confidenceLevel   An {@code OWLClass} representing a 'confidence level' term 
     *                          to be associated with the returned {@code OWLClass}.
     * @return                  An {@code OWLClass} from the branch 
     *                          'confidence statement from single evidence' associated to 
     *                          the confidence level {@code confidenceLevel}.
     * @throws IllegalArgumentException If {@code confidenceLevel} is not a valid 
     *                                  confidence level to be associated to a confidence 
     *                                  statement from single evidence.
     * @see #getConfidenceStatement(OWLClass, OWLClass, OWLClass)
     */
    public OWLClass getSingleEvidenceConfidenceStatement(OWLClass confidenceLevel) {
        log.entry(confidenceLevel);
        return log.traceExit(this.getConfidenceStatement(this.singleEvidenceConcordance, 
                this.evidenceTypeConcordance, confidenceLevel));
    }
    /**
     * Retrieve a CI statement associated to the provided {@code evidenceConcordance}, 
     * {@code evidenceTypeConcordance}, and {@code confidenceLevel}. The following requirements 
     * must be met: 
     * <ul>
     * <li>{@code evidenceConcordance} must be of type 
     * 'evidence concordance' ({@link #isEvidenceConcordance(OWLClass)} returns {@code true}); 
     * <li>{@code evidenceTypeConcordance} must be of type 
     * 'evidence type concordance' ({@link #isEvidenceTypeConcordance(OWLClass)} returns 
     * {@code true}); 
     * <li>{@code confidenceLevel} can be {@code null}, but when not-{@code null}, 
     * it must be one of the confidence levels returned by {@code #getConfidenceLevels()}.
     * </ul>
     * If one of these conditions is not respected, an {@code IllegalArgumentException} 
     * is thrown.
     * <p>
     * The returned {@code OWLClass} is of type 'confidence information statement' 
     * ({@link #isConfidenceStatement(OWLClass)} returns {@code true}), and is never 
     * {@code null}. If no CI statement could be unambiguously identified, while 
     * the provided arguments were valid, an {@code IllegalStateException} is thrown. 
     * 
     * @param evidenceConcordance       An {@code OWLClass} representing a term from the branch 
     *                                  'evidence concordance'. Cannot be {@code null}.
     * @param evidenceTypeConcordance   An {@code OWLClass} representing a term from the branch 
     *                                  'evidence type concordance'.
     * @param confidenceLevel           An {@code OWLClass} representing a term from the branch 
     *                                  'confidence level'.
     * @return  An {@code OWLClass} that is a CI statement corresponding to the composition 
     *          of the provided arguments.
     * @throws IllegalArgumentException If one of the arguments does not respect the conditions 
     *                                  detailed above.
     * @throws IllegalStateException    If the CIO wrapped did not allow to unambiguously 
     *                                  identified a valid CI statement, while the arguments 
     *                                  provided were all valid.
     * @see #getSingleEvidenceConfidenceStatement(OWLClass)
     */
    public OWLClass getConfidenceStatement(OWLClass evidenceConcordance, 
            OWLClass evidenceTypeConcordance, OWLClass confidenceLevel) 
                    throws IllegalArgumentException, IllegalStateException{
        log.entry(evidenceConcordance, evidenceTypeConcordance, confidenceLevel);
        
        Set<OWLClass> validConfidenceLevels = this.getConfidenceLevels();
        //Check pre-conditions.
        //evidenceConcordance is mandatory
        if (evidenceConcordance == null || !this.isEvidenceConcordance(evidenceConcordance)) {
            throw log.throwing(new IllegalArgumentException("The provided evidence concordance "
                    + "is invalid: " + evidenceConcordance));
        }
        if (evidenceTypeConcordance != null && 
                !this.isEvidenceTypeConcordance(evidenceTypeConcordance)) {
            throw log.throwing(new IllegalArgumentException("The provided "
                    + "evidence type concordance is invalid: " + evidenceTypeConcordance));
        }
        //evidence type concordance must be provided only when evidenceConcordance is not 
        //a single evidence concordance
        if ((this.isSingleEvidenceConcordance(evidenceConcordance) && 
                evidenceTypeConcordance != null && 
                !evidenceTypeConcordance.equals(this.evidenceTypeConcordance)) || 
            (!this.isSingleEvidenceConcordance(evidenceConcordance) && 
                        evidenceTypeConcordance == null)) {
            throw log.throwing(new IllegalArgumentException("An evidence type concordance "
                    + "must be provided if and only if the evidence concordance "
                    + "is not of type 'single evidence'. Provided evidence concordance: "
                    + evidenceConcordance + " - Provided evidence type concordance: " 
                    + evidenceTypeConcordance));
        }
        if (confidenceLevel != null && !validConfidenceLevels.contains(confidenceLevel)) {
            throw log.throwing(new IllegalArgumentException("The provided "
                    + "confidence level is invalid: " + confidenceLevel));
        }
        
        //to identify the correct class, we use a graph-based approach, 
        //rather than using a reasoner to retrieve equivalent classes. 
        //we do not retrieve all classes to check for errors, we stop iteration 
        //as soon as a class is found, it is too slow to iterate all classes each time
        Set<OWLClass> identifiedClasses = new HashSet<OWLClass>();
        for (OWLClass cls: this.wrapper.getAllRealOWLClasses()) {
            if (!this.isConfidenceStatement(cls)) {
                continue;
            }
            OWLClass clsEvidenceConcordance = this.getEvidenceConcordance(cls);
            if (!evidenceConcordance.equals(clsEvidenceConcordance)) {
                continue;
            }
            OWLClass clsEvidenceTypeConcordance = this.getEvidenceTypeConcordance(cls);
            if (!Objects.equals(evidenceTypeConcordance, clsEvidenceTypeConcordance)) {
                continue;
            }
            OWLClass clsConfidenceLevel = this.getConfidenceLevel(cls);
            if (!validConfidenceLevels.contains(clsConfidenceLevel)) {
                clsConfidenceLevel = null;
            }
            if (!Objects.equals(confidenceLevel, clsConfidenceLevel)) {
                continue;
            }

            identifiedClasses.add(cls);
            log.trace("Confidence elements retrieved for class {} - evidence conc.: {} - evidence type conc.: {} - conf. level: {}", 
                    cls, clsEvidenceConcordance, clsEvidenceTypeConcordance, clsConfidenceLevel);
            break;
        }
        
        //check validity of results
        if (identifiedClasses.size() > 1) {
            throw log.throwing(new IllegalStateException("The CIO wrapped identified "
                    + "more than one CI statement for the provided parameters: "
                    + identifiedClasses
                    + " - Provided evidence concordance: " + evidenceConcordance
                    + " - Provided evidence type concordance: " + evidenceTypeConcordance
                    + " - Provided confidence level: " + confidenceLevel));
        }
        if (identifiedClasses.size() == 0) {
            throw log.throwing(new IllegalArgumentException("The provided parameters "
                    + "did not allow to identify any confidence information statement. "
                    + "Provided evidence concordance: " + evidenceConcordance
                    + " - Provided evidence type concordance: " + evidenceTypeConcordance
                    + " - Provided confidence level: " + confidenceLevel));
        }
        
        return log.traceExit(identifiedClasses.iterator().next());
    }

    /**
     * @return  A {@code Set} of {@code OWLClass}es that are 
     *          confidence information elements representing confidence levels 
     *          in the wrapped CIO, subclasses of the term 'confidence level' 
     *          (and not the 'confidence level' term itself). 
     * @see #CONFIDENCE_LEVEL_ID
     */
    public Set<OWLClass> getConfidenceLevels() {
        log.traceEntry();
        return log.traceExit(this.wrapper.getDescendantsThroughIsA(this.confidenceLevel));
    }
    /**
     * @return  A {@code List} of {@code OWLClass}es that are 
     *          confidence information elements representing confidence levels 
     *          in the wrapped CIO, subclasses of the term 'confidence level', 
     *          ordered in ascending confidence level, based on the relation 
     *          'provides_greater_confidence_than'.
     * @throws IllegalStateException    If the CIO wrapped does not allow to correctly order 
     *                                  confidence levels.
     * @see #CONFIDENCE_LEVEL_ID
     * @see #PROVIDES_GREATER_CONF_THAN_ID
     * @see #getConfidenceLevels()
     */
    public List<OWLClass> getOrderedConfidenceLevels() {
        log.traceEntry();

        List<OWLClass> orderedConfLevels = new ArrayList<OWLClass>(this.getConfidenceLevels());
        final OWLGraphWrapper wrapper = this.wrapper;
        //suppress warning because the wrapper uses unparameterized arguments
        @SuppressWarnings("rawtypes")
        final Set<OWLPropertyExpression> greaterConfThanRels = new HashSet<OWLPropertyExpression>();
        greaterConfThanRels.add(this.providesGreaterConfidenceThan);
        
        Collections.sort(orderedConfLevels, new Comparator<OWLClass>() {
            @Override
            public int compare(OWLClass o1, OWLClass o2) {
                log.entry(o1, o2);
                
                if (wrapper.getAncestors(o1, greaterConfThanRels).contains(o2)) {
                    return log.traceExit(1);
                }
                if (wrapper.getAncestors(o2, greaterConfThanRels).contains(o1)) {
                    return log.traceExit(-1);
                }
                throw log.throwing(new IllegalStateException("The CIO provided does not allow "
                        + "to order some confidence levels: " + o1 + " - " + o2));
            }
        });
        
        return log.traceExit(orderedConfLevels);
    }
    
    /**
     * @return  The {@code OWLGraphWrapper} that this object is using.
     */
    public OWLGraphWrapper getOWLGraphWrapper() {
        return this.wrapper;
    }

//    /**
//     * Determines the branch in the ontology which {@code cls} belongs to, among the branches 
//     * usable for annotations. The {@code OWLClass} returned can only be one 
//     * of the {@code OWLClass}es returned by {@link getValidBranches()}.
//     * <p>
//     * If {@code cls} does not belong to any of these branches, 
//     * an {@code IllegalArgumentException} is thrown (terms belonging to other branches 
//     * of the ontology are only used to compose terms usable for annotations).
//     * 
//     * @param cls   An {@code OWLClass} for which we want to determine the branch it belongs to.
//     * @return      An {@code OWLClass} that is the root of the branch {@code cls} belongs to.
//     * @throws IllegalArgumentException If {@code cls} does not belong to any 
//     *                                  of the branches listed.
//     * @see #getValidBranches()
//     */
//    public OWLClass getValidBranch(OWLClass cls) throws IllegalArgumentException {
//        log.entry(cls);
//        
//        Set<OWLClass> relatedClasses = new HashSet<OWLClass>();
//        relatedClasses.add(cls);
//        relatedClasses.addAll(this.wrapper.getAncestorsThroughIsA(cls));
//        relatedClasses.retainAll(this.getValidBranches());
//        if (relatedClasses.size() != 1) {
//            throw log.throwing(new IllegalArgumentException(cls + " does not belong to "
//                    + "any branch among valid branches: " + this.getValidBranches()));
//        }
//        return log.traceExit(relatedClasses.iterator().next());
//    }
//    /**
//     * Retrieves {@code OWLClass}es that are the root of subgraphs containing 
//     * terms that are directly usable for annotations. The {@code OWLClass} are of 7 types: 
//     * <ul>
//     * <li>equal to {@link #getSingleEvidenceConf()}
//     * <li>equal to {@link #getCongruentSameTypeEvidenceConf()}
//     * <li>equal to {@link #getCongruentMultipleTypesEvidenceConf()}
//     * <li>equal to {@link #getWeakConflictSameTypeEvidenceConf()}
//     * <li>equal to {@link #getWeakConflictMultipleTypesEvidenceConf()}
//     * <li>equal to {@link #getStrongConflictSameTypeEvidenceConf()}
//     * <li>equal to {@link #getStrongConflictMultipleTypesEvidenceConf()}
//     * </ul>
//     * <p>
//     * Terms belonging to other branches of the ontology are only used to compose terms 
//     * usable for annotations.
//     * 
//     * @return  A {@code Set} of {@code OWLClass}es representing roots of subgraphs 
//     *          containing valid terms for annotations. The {@code Set} is a copy and 
//     *          can be safely modified. 
//     * @see #getValidBranch(OWLClass)
//     */
//    public Set<OWLClass> getValidBranches() {
//        Set<OWLClass> validBranches = new HashSet<OWLClass>();
//        validBranches.add(this.getSingleEvidenceConf());
//        validBranches.add(this.getCongruentSameTypeEvidenceConf());
//        validBranches.add(this.getCongruentMultipleTypesEvidenceConf());
//        validBranches.add(this.getWeakConflictSameTypeEvidenceConf());
//        validBranches.add(this.getWeakConflictMultipleTypesEvidenceConf());
//        validBranches.add(this.getStrongConflictSameTypeEvidenceConf());
//        validBranches.add(this.getStrongConflictMultipleTypesEvidenceConf());
//        return validBranches;
//    }
//    
//    /**
//     * Extract the confidence level from a term providing confidence level information 
//     * (for example 'high confidence from single evidence', or 'confidence from congruent 
//     * evidence lines of same type, best confidence high'). The {@code OWLClass} returned 
//     * can only be one of the {@code OWLClass}es returned by {@link #getConfidenceLevels()}.
//     * <p>
//     * If {@code cls} does not provide confidence level information, 
//     * an {@code IllegalArgumentException} is thrown.
//     * 
//     * @param cls   An {@code OWLClass} for which we want to extract the confidence level.
//     * @return      An {@code OWLClass} that is the confidence level.
//     * @throws IllegalArgumentException If {@code cls} does not provide confidence level.
//     * @see #getConfidenceLevels()
//     * @see #isProvidingConfidenceLevelInfo()
//     */
//    public OWLClass extractConfidenceLevel(OWLClass cls) throws IllegalArgumentException {
//        log.entry(cls);
//        Set<OWLClass> relatedClasses = new HashSet<OWLClass>();
//        relatedClasses.add(cls);
//        relatedClasses.addAll(this.wrapper.getAncestorsThroughIsA(cls));
//        relatedClasses.retainAll(this.getOrderedConfidenceLevels());
//        if (relatedClasses.size() != 1) {
//            throw log.throwing(new IllegalArgumentException(cls + " does not provide "
//                    + "confidence level information"));
//        }
//        return log.traceExit(relatedClasses.iterator().next());
//    }
//    /**
//     * Determines whether {@code cls} provides a confidence level information. For instance, 
//     * the terms 'high confidence from single evidence', or 'confidence from congruent 
//     * evidence lines of same type, best confidence high'. More formally, this method 
//     * checks whether {@code cls} is equal to or is a descendant of the {@code OWLClass}es 
//     * returned by {@link #getConfidenceLevels()}.
//     * 
//     * @param cls   An {@code OWLClass} to check whether it provides confidence level information.
//     * @return      {@code true} if {@code cls} provides confidence level information.
//     * @see #getConfidenceLevels()
//     * @see #getConfidenceLevel(OWLClass)
//     */
//    public boolean isProvidingConfidenceLevelInfo(OWLClass cls) {
//        log.entry(cls);
//        try {
//            this.extractConfidenceLevel(cls);
//            return log.traceExit(true);
//        } catch (IllegalArgumentException e) {
//            return log.traceExit(false);
//        }
//    }
    //    /**
//     * Determines whether {@code cls} is equal to or is a descendant of the {@code OWLClass} 
//     * returns by {@link #getSingleEvidenceConf()}.
//     * 
//     * @param cls   An {@code OWLClass} to determine whether it belongs to 
//     *              the 'single evidence' branch.
//     * @return      {@code true} if {@code cls} belongs to the 'single evidence' branch.
//     * @throws NullPointerException     if {@code cls} is {@code null}.
//     * @see #getSingleEvidenceConf()
//     */
//    public boolean isConfFromSingleEvidence(OWLClass cls) {
//        log.entry(cls);
//        return log.traceExit(this.isBelongingToSubgraph(cls, this.getSingleEvidenceConf()));
//    }
//    
//    /**
//     * Determines whether {@code cls} is a term representing confidence from congruent 
//     * evidence lines (see {@link #getCongruentConcordance()}).
//     * 
//     * @param cls   An {@code OWLClass} to determine whether it represents confidence 
//     *              from congruent evidence lines.
//     * @return      {@code true} if {@code cls} represents confidence 
//     *              from congruent evidence lines
//     * @throws NullPointerException     if {@code cls} is {@code null}.
//     * @see #getCongruentConcordance()
//     */
//    public boolean isConfFromCongruentEvidenceLines(OWLClass cls) {
//        log.entry(cls);
//        return log.traceExit(this.isBelongingToSubgraph(cls, this.getCongruentConcordance()));
//    }
//    /**
//     * Determines whether {@code cls} is a term representing confidence from weakly 
//     * conflicting evidence lines (see {@link #getWeaklyConflictingConcordance()}).
//     * 
//     * @param cls   An {@code OWLClass} to determine whether it represents confidence 
//     *              from weakly conflicting evidence lines.
//     * @return      {@code true} if {@code cls} represents confidence 
//     *              from weakly conflicting evidence lines
//     * @throws NullPointerException     if {@code cls} is {@code null}.
//     * @see #getWeaklyConflictingConcordance()
//     */
//    public boolean isConfFromWeakConflictEvidenceLines(OWLClass cls) {
//        log.entry(cls);
//        return log.traceExit(this.isBelongingToSubgraph(cls, this.getWeaklyConflictingConcordance()));
//    }
//    /**
//     * Determines whether {@code cls} is a term representing confidence from strongly 
//     * conflicting evidence lines (see {@link #getStronglyConflictingConcordance()}).
//     * 
//     * @param cls   An {@code OWLClass} to determine whether it represents confidence 
//     *              from strongly conflicting evidence lines.
//     * @return      {@code true} if {@code cls} represents confidence 
//     *              from strongly conflicting evidence lines
//     * @throws NullPointerException     if {@code cls} is {@code null}.
//     * @see #getStronglyConflictingConcordance()
//     */
//    public boolean isConfFromStrongConflictEvidenceLines(OWLClass cls) {
//        log.entry(cls);
//        return log.traceExit(this.isBelongingToSubgraph(cls, this.getStronglyConflictingConcordance()));
//    }
//    
//    /**
//     * Determines whether {@code cls} is equal to or is a descendant of the {@code OWLClass} 
//     * returns by {@link #getRejectedTerm()}.
//     * 
//     * @param cls   An {@code OWLClass} to determine whether it corresponds to a 'rejected' 
//     *              confidence level.
//     * @return      {@code true} if {@code cls} corresponds to a 'rejected' confidence level.
//     * @throws NullPointerException     if {@code cls} is {@code null}.
//     * @see #getRejectedTerm()
//     */
//    public boolean isRejectedConfidenceInformation(OWLClass cls) {
//        log.entry(cls);
//        return log.traceExit(this.isBelongingToSubgraph(cls, this.getRejectedTerm()));
//    }
//    
//    /**
//     * Checks whether {@code cls} belongs to the is_a subgraph with {@code subgraphRoot} as root. 
//     * More formally, this methods checks whether {@code cls} is equal to {@code subgraphRoot}, 
//     * or whether {@code subgraphRoot} is an ancestor of {@code cls} through is_a relations, 
//     * even indirect. is_a relations are inferred by owltools, so this includes 
//     * EquivalentClasses axiom, etc.
//     * 
//     * @param cls           An {@code OWLClass} to check for membership of the subgraph 
//     *                      with {@code subgraphRoot} as root.
//     * @param subgraphRoot  An {@code OWLClass} that is the root of the subgraph for which 
//     *                      we want to know whether it contains {@code cls}.
//     * @return              {@code true} if {@code cls} is part of the subgraph of is_a 
//     *                      relations starting from {@code subgraphRoot}.
//     * @throws NullPointerException     if {@code cls} is {@code null}.
//     */
//    private boolean isBelongingToSubgraph(OWLClass cls, OWLClass subgraphRoot) {
//        log.entry(cls, subgraphRoot);
//        if (subgraphRoot.equals(cls)) {
//            return log.traceExit(true);
//        }
//        if (this.wrapper.getAncestorsThroughIsA(cls).contains(subgraphRoot)) {
//            return log.traceExit(true);
//        }
//        return log.traceExit(false);
//    }
    //*************************************
    //  GETTERS 
    //*************************************
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #SINGLE_EVIDENCE_CONF_ID}.
//     * @see #SINGLE_EVIDENCE_CONF_ID
//     */
//    public OWLClass getSingleEvidenceConf() {
//        return singleEvidenceConf;
//    }
//
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #CONFIDENCE_STATEMENT_ID}.
//     * @see #CONFIDENCE_STATEMENT_ID
//     */
//    public OWLClass getConfidenceStatement() {
//        return this.confidenceStatement;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #CONFIDENCE_ELEMENT_ID}.
//     * @see #CONFIDENCE_ELEMENT_ID
//     */
//    public OWLClass getConfidenceElement() {
//        return this.confidenceElement;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #CONFIDENCE_LEVEL_ID}.
//     * @see #CONFIDENCE_LEVEL_ID
//     */
//    public OWLClass getConfidenceLevel() {
//        return confidenceLevel;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #EVIDENCE_CONCORDANCE_ID}.
//     * @see #EVIDENCE_CONCORDANCE_ID
//     */
//    public OWLClass getEvidenceConcordance() {
//        return this.evidenceConcordance;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #EVIDENCE_TYPE_CONCORDANCE_ID}.
//     * @see #EVIDENCE_TYPE_CONCORDANCE_ID
//     */
//    public OWLClass getEvidenceTypeConcordance() {
//        return this.evidenceTypeConcordance;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #REJECTED_TERM_ID}.
//     * @see #REJECTED_TERM_ID
//     */
//    public OWLClass getRejectedTerm() {
//        return rejectedTerm;
//    }
//    
//    /**
//     * @return  An unmodifiable {@code Set} of {@code OWLClass}es that are 
//     *          confidence information elements representing evidence concordance terms 
//     *          in the wrapped CIO (term 'evidence concordance' itself excluded).
//     */
//    public Set<OWLClass> getEvidenceConcordances() {
//        return this.evidenceConcordances;
//    }
//    /**
//     * @return  An unmodifiable {@code Set} of {@code OWLClass}es that are 
//     *          confidence information elements representing evidence type concordance terms 
//     *          in the wrapped CIO (term 'evidence type concordance' itself excluded).
//     */
//    public Set<OWLClass> getEvidenceTypeConcordances() {
//        return this.evidenceTypeConcordances;
//    }
//
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #MULTIPLE_EVIDENCE_CONF_ID}.
//     * @see #MULTIPLE_EVIDENCE_CONF_ID
//     */
//    public OWLClass getMultipleEvidenceConf() {
//        return multipleEvidenceConf;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #CONGRUENT_SAME_TYPE_EVIDENCE_CONF_ID}.
//     * @see #CONGRUENT_SAME_TYPE_EVIDENCE_CONF_ID
//     */
//    public OWLClass getCongruentSameTypeEvidenceConf() {
//        return congruentSameTypeEvidenceConf;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #CONGRUENT_MULTIPLE_TYPES_EVIDENCE_CONF_ID}.
//     * @see #CONGRUENT_MULTIPLE_TYPES_EVIDENCE_CONF_ID
//     */
//    public OWLClass getCongruentMultipleTypesEvidenceConf() {
//        return congruentMultipleTypesEvidenceConf;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #WEAK_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID}.
//     * @see #WEAK_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID
//     */
//    public OWLClass getWeakConflictSameTypeEvidenceConf() {
//        return weakConflictSameTypeEvidenceConf;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #WEAK_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID}.
//     * @see #WEAK_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID
//     */
//    public OWLClass getWeakConflictMultipleTypesEvidenceConf() {
//        return weakConflictMultipleTypesEvidenceConf;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #STRONG_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID}.
//     * @see #STRONG_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID
//     */
//    public OWLClass getStrongConflictSameTypeEvidenceConf() {
//        return strongConflictSameTypeEvidenceConf;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #STRONG_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID}.
//     * @see #STRONG_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID
//     */
//    public OWLClass getStrongConflictMultipleTypesEvidenceConf() {
//        return strongConflictMultipleTypesEvidenceConf;
//    }
//
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #HIGH_CONF_LEVEL_ID}.
//     * @see #HIGH_CONF_LEVEL_ID
//     */
//    public OWLClass getHighConfLevel() {
//        return highConfLevel;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #MEDIUM_CONF_LEVEL_ID}.
//     * @see #MEDIUM_CONF_LEVEL_ID
//     */
//    public OWLClass getMediumConfLevel() {
//        return mediumConfLevel;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #LOW_CONF_LEVEL_ID}.
//     * @see #LOW_CONF_LEVEL_ID
//     */
//    public OWLClass getLowConfLevel() {
//        return lowConfLevel;
//    }
//    
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #CONGRUENT_CONCORDANCE_ID}.
//     * @see #CONGRUENT_CONCORDANCE_ID
//     */
//    public OWLClass getCongruentConcordance() {
//        return congruentConcordance;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #WEAKLY_CONFLICTING_CONCORDANCE_ID}.
//     * @see #WEAKLY_CONFLICTING_CONCORDANCE_ID
//     */
//    public OWLClass getWeaklyConflictingConcordance() {
//        return weaklyConflictingConcordance;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #STRONGLY_CONFLICTING_CONCORDANCE_ID}.
//     * @see #STRONGLY_CONFLICTING_CONCORDANCE_ID
//     */
//    public OWLClass getStronglyConflictingConcordance() {
//        return stronglyConflictingConcordance;
//    }
//
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #SAME_TYPE_EVIDENCE_CONCORDANCE_ID}.
//     * @see #SAME_TYPE_EVIDENCE_CONCORDANCE_ID
//     */
//    public OWLClass getSameTypeEvidenceConcordance() {
//        return sameTypeEvidenceConcordance;
//    }
//    /**
//     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
//     *          with OBO-like ID {@link #DIFFERENT_TYPES_EVIDENCE_CONCORDANCE_ID}.
//     * @see #DIFFERENT_TYPES_EVIDENCE_CONCORDANCE_ID
//     */
//    public OWLClass getDifferentTypesEvidenceConcordance() {
//        return differentTypesEvidenceConcordance;
//    }
}
