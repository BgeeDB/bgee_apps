package org.bgee.pipeline.ontologycommon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;

import owltools.graph.OWLGraphWrapper;

/**
 * Class extending {@code OntologyUtils} to provide functionalities specific to the use 
 * of the Confidence Information Ontology (see 
 * <a href='https://github.com/BgeeDB/confidence-information-ontology'>CIO repository</a>).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
//TODO: check whether it is actually needed to extend OntologyUtils
//TODO: this class might be of interest to all CIO users, maybe could we provide it as a jar 
//with only owltools jar in classpath?
public class CIOUtils extends OntologyUtils {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(CIOUtils.class.getName());

    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "confidence from single evidence".
     * @see #MULTIPLE_EVIDENCE_CONF_ID
     */
    public final static String SINGLE_EVIDENCE_CONF_ID = "CIO:0000001";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "rejected".
     * @see #LOW_CONF_LEVEL_ID
     * @see #MEDIUM_CONF_LEVEL_ID
     * @see #HIGH_CONF_LEVEL_ID
     */
    public final static String REJECTED_TERM_ID = "CIO:0000039";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "confidence from multiple evidence lines".
     * @see #SINGLE_EVIDENCE_CONF_ID
     */
    public final static String MULTIPLE_EVIDENCE_CONF_ID = "CIO:0000002";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "confidence from congruent evidence lines of same type".
     */
    public final static String CONGRUENT_SAME_TYPE_EVIDENCE_CONF_ID = "CIO:0000016";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "confidence from congruent evidence lines of multiple types".
     */
    public final static String CONGRUENT_MULTIPLE_TYPES_EVIDENCE_CONF_ID = "CIO:0000008";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "confidence from weakly conflicting evidence lines of same type".
     */
    public final static String WEAK_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID = "CIO:0000021";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "confidence from weakly conflicting evidence lines of multiple types".
     */
    public final static String WEAK_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID = "CIO:0000011";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "confidence from strongly conflicting evidence lines of same type".
     */
    public final static String STRONG_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID = "CIO:0000020";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "confidence from strongly conflicting evidence lines of multiple types".
     */
    public final static String STRONG_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID = "CIO:0000010";
    
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "high confidence level".
     * @see #MEDIUM_CONF_LEVEL_ID
     * @see #LOW_CONF_LEVEL_ID
     * @see #REJECTED_TERM_ID
     */
    public final static String HIGH_CONF_LEVEL_ID = "CIO:0000029";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "medium confidence level".
     * @see #HIGH_CONF_LEVEL_ID
     * @see #LOW_CONF_LEVEL_ID
     * @see #REJECTED_TERM_ID
     */
    public final static String MEDIUM_CONF_LEVEL_ID = "CIO:0000030";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "low confidence level".
     * @see #MEDIUM_CONF_LEVEL_ID
     * @see #HIGH_CONF_LEVEL_ID
     * @see #REJECTED_TERM_ID
     */
    public final static String LOW_CONF_LEVEL_ID = "CIO:0000031";
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
     * @see #getSingleEvidenceConf()
     */
    private final OWLClass singleEvidenceConf;
    /**
     * @see #getMultipleEvidenceConf()
     */
    private final OWLClass multipleEvidenceConf;
    /**
     * @see #getCongruentSameTypeEvidenceConf()
     */
    private final OWLClass congruentSameTypeEvidenceConf;
    /**
     * @see #getCongruentMultipleTypesEvidenceConf()
     */
    private final OWLClass congruentMultipleTypesEvidenceConf;
    /**
     * @see #getWeakConflictSameTypeEvidenceConf()
     */
    private final OWLClass weakConflictSameTypeEvidenceConf;
    /**
     * @see #getWeakConflictMultipleTypesEvidenceConf()
     */
    private final OWLClass weakConflictMultipleTypesEvidenceConf;
    /**
     * @see #getStrongConflictSameTypeEvidenceConf()
     */
    private final OWLClass strongConflictSameTypeEvidenceConf;
    /**
     * @see #getStrongConflictMultipleTypesEvidenceConf()
     */
    private final OWLClass strongConflictMultipleTypesEvidenceConf;
    
    /**
     * @see #getHighConfLevel()
     */
    private final OWLClass highConfLevel;
    /**
     * @see #getMediumConfLevel()
     */
    private final OWLClass mediumConfLevel;
    /**
     * @see #getLowConfLevel()
     */
    private final OWLClass lowConfLevel;
    /**
     * @see #getRejectedTerm()
     */
    private final OWLClass rejectedTerm;
    
    /**
     * @see #getCongruentConcordance()
     */
    private final OWLClass congruentConcordance;
    /**
     * @see #getWeaklyConflictingConcordance()
     */
    private final OWLClass weaklyConflictingConcordance;
    /**
     * @see #getStronglyConflictingConcordance()
     */
    private final OWLClass stronglyConflictingConcordance;
    
    private final OWLClass sameTypeEvidenceConcordance;
    private final OWLClass differentTypesEvidenceConcordance;
    
    /**
     * Constructor providing the {@code OWLGraphWrapper} wrapping 
     * the {@code OWLOntology} which operations should be performed on.
     * 
     * @param wrapper   the {@code OWLGraphWrapper} wrapping the {@code OWLOntology} 
     *                  which operations should be performed on.
     */
    public CIOUtils(OWLGraphWrapper wrapper) {
        super(wrapper);
        
        this.singleEvidenceConf = this.getWrapper().getOWLClassByIdentifier(
                SINGLE_EVIDENCE_CONF_ID);
        this.multipleEvidenceConf = this.getWrapper().getOWLClassByIdentifier(
                MULTIPLE_EVIDENCE_CONF_ID);
        this.congruentSameTypeEvidenceConf = this.getWrapper().getOWLClassByIdentifier(
                CONGRUENT_SAME_TYPE_EVIDENCE_CONF_ID);
        this.congruentMultipleTypesEvidenceConf = this.getWrapper().getOWLClassByIdentifier(
                CONGRUENT_MULTIPLE_TYPES_EVIDENCE_CONF_ID);
        this.weakConflictSameTypeEvidenceConf = this.getWrapper().getOWLClassByIdentifier(
                WEAK_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID);
        this.weakConflictMultipleTypesEvidenceConf = this.getWrapper().getOWLClassByIdentifier(
                WEAK_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID);
        this.strongConflictSameTypeEvidenceConf = this.getWrapper().getOWLClassByIdentifier(
                STRONG_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID);
        this.strongConflictMultipleTypesEvidenceConf = this.getWrapper().getOWLClassByIdentifier(
                STRONG_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID);
        if (this.singleEvidenceConf == null || this.multipleEvidenceConf == null || 
                this.congruentSameTypeEvidenceConf == null || 
                this.congruentMultipleTypesEvidenceConf == null || 
                this.weakConflictSameTypeEvidenceConf == null || 
                this.weakConflictMultipleTypesEvidenceConf == null || 
                this.strongConflictSameTypeEvidenceConf == null || 
                this.strongConflictMultipleTypesEvidenceConf == null) {
            throw log.throwing(new IllegalArgumentException("The ontology used does not allow "
                + "to retrieve some branches. "
                + "Single evidence: " + this.singleEvidenceConf + 
                " - Multiple evidence lines: " + this.multipleEvidenceConf + 
                " - Congruent same type evidence lines: " + this.congruentSameTypeEvidenceConf + 
                " - Congruent multiple types evidence lines: " + 
                    this.congruentMultipleTypesEvidenceConf + 
                " - Weakly conflicting evidence lines of same type: " + 
                    this.weakConflictSameTypeEvidenceConf + 
                " - Weakly conflicting evidence lines of multiple types: " + 
                    this.weakConflictMultipleTypesEvidenceConf + 
                " - Strongly conflicting evidence lines of same type: " + 
                    this.strongConflictSameTypeEvidenceConf + 
                " - Strongly conflicting evidence lines of multiple types: " + 
                    this.strongConflictMultipleTypesEvidenceConf));
        }
        
        this.highConfLevel = this.getWrapper().getOWLClassByIdentifier(HIGH_CONF_LEVEL_ID);
        this.mediumConfLevel = this.getWrapper().getOWLClassByIdentifier(MEDIUM_CONF_LEVEL_ID);
        this.lowConfLevel = this.getWrapper().getOWLClassByIdentifier(LOW_CONF_LEVEL_ID);
        this.rejectedTerm = this.getWrapper().getOWLClassByIdentifier(REJECTED_TERM_ID);
        if (this.highConfLevel == null || this.mediumConfLevel == null || 
                this.lowConfLevel == null || this.rejectedTerm == null) {
            throw log.throwing(new IllegalArgumentException("The ontology used does not allow "
                    + "to retrieve confidence levels. High conf: " + this.highConfLevel + 
                    " - Medium conf: " + this.mediumConfLevel + 
                    " - Low conf: " + this.lowConfLevel + " - Rejected: " + this.rejectedTerm));
        }
        
        this.congruentConcordance = this.getWrapper().getOWLClassByIdentifier(
                CONGRUENT_CONCORDANCE_ID);
        this.weaklyConflictingConcordance = this.getWrapper().getOWLClassByIdentifier(
                WEAKLY_CONFLICTING_CONCORDANCE_ID);
        this.stronglyConflictingConcordance = this.getWrapper().getOWLClassByIdentifier(
                STRONGLY_CONFLICTING_CONCORDANCE_ID);
        if (this.congruentConcordance == null || this.weaklyConflictingConcordance == null || 
                this.stronglyConflictingConcordance == null) {
            throw log.throwing(new IllegalArgumentException("The ontology used does not allow "
                + "to retrieve concordance levels. "
                + "Congruent concordance: " + this.congruentConcordance + 
                " - Weakly conflicting concordance: " + this.weaklyConflictingConcordance + 
                " - Strongly conflicting concordance: " + this.stronglyConflictingConcordance));
        }
        
        this.sameTypeEvidenceConcordance = this.getWrapper().getOWLClassByIdentifier(
                SAME_TYPE_EVIDENCE_CONCORDANCE_ID);
        this.differentTypesEvidenceConcordance = this.getWrapper().getOWLClassByIdentifier(
                DIFFERENT_TYPES_EVIDENCE_CONCORDANCE_ID);
        if (this.sameTypeEvidenceConcordance == null || 
                this.differentTypesEvidenceConcordance == null) {
            throw log.throwing(new IllegalArgumentException("The ontology used does not allow "
                + "to retrieve 'same type' and 'different types' concordance levels. "
                + "Same type concordance: " + this.sameTypeEvidenceConcordance + 
                " - Different types concordance: " + this.differentTypesEvidenceConcordance));
        }
    }
    
    /**
     * Determines the best confidence term with level information among {@code cioTerms}. 
     * The {@code OWLClass}es in {@code cioTerms} must meet the following conditions: 
     * <ul>
     * <li>They must all be part of the same branch (all having as ancestor one and only one 
     * {@code OWLClass} among the {@code OWLClass}es returned by {@link #getValidBranches()}).
     * <li>They must all represent a confidence level (meaning, having as ancestor 
     * one of the {@code OWLClass}es returned by {@link #getConfidenceLevels()}).
     * </ul>
     * Otherwise, an {@code IllegalArgumentException} is thrown. Note that confidences 
     * from strongly conflicting evidence lines cannot be associated to 
     * a confidence level (see {@link #getStrongConflictSameTypeEvidenceConf()} and 
     * {@link #getStrongConflictMultipleTypesEvidenceConf()}).
     * <p>
     * This basically means that {@code OWLClass}es in {@code cioTerms} must be one 
     * of the pre-composed terms usable for annotations, leaves of the ontology,  
     * not belonging to the 'strongly conflicting evidence lines' branch.
     * 
     * @param cioTerms  A {@code Collection} of {@code OWLClass}es providing confidence level 
     *                  information, from a same branch, for which we want to determine 
     *                  the best one. 
     * @return          An {@code OWLClass} that is the best confidence among {@code cioTerms}.
     * @throws IllegalArgumentException If the {@code OWLClass}es in {@code cioTerms} do not 
     *                                  all belong to a same branch, or do not all provide 
     *                                  confidence level information.
     */
    public OWLClass getBestTermWithConfidenceLevel(Collection<OWLClass> cioTerms) 
            throws IllegalArgumentException {
        log.entry(cioTerms);
        
        //to check pre-conditions
        Set<OWLClass> branches = new HashSet<OWLClass>();
        //associate each term to its confidence level to order them afterwards 
        //(the alternative would be to create a class CIOTerm providing a method 
        //'getConfidenceLevel()')
        final Map<OWLClass, OWLClass> termToConfLevel = new HashMap<OWLClass, OWLClass>();
        for (OWLClass cioTerm: cioTerms) {
            //getValidBranch will throw an exception if the term does not belong 
            //to any valid branch, so we do not have to check that, and we are sure 
            //a term will always be returned.
            branches.add(this.getValidBranch(cioTerm));
            //getConfidenceLevel will throw an exception if the term does not provide 
            //any confidence level, so we do not need to check that. 
            termToConfLevel.put(cioTerm, this.extractConfidenceLevel(cioTerm));
        }
        assert branches.size() > 0; 
        if (branches.size() > 1) {
            throw log.throwing(new IllegalArgumentException("The terms provided do not "
                    + "all belong to the same branch, branches detected: " + branches));
        }
        
        List<OWLClass> orderedTerms = new ArrayList<OWLClass>(cioTerms);
        final List<OWLClass> orderedConfLevels = this.getOrderedConfidenceLevels();
        Collections.sort(orderedTerms, new Comparator<OWLClass>() {
            @Override
            public int compare(OWLClass o1, OWLClass o2) {
                log.entry(o1, o2);
                return log.exit(orderedConfLevels.indexOf(termToConfLevel.get(o1)) - 
                        orderedConfLevels.indexOf(termToConfLevel.get(o2))); 
            }
        });
        
        return log.exit(orderedTerms.get(orderedTerms.size() - 1));
    }
    
    /**
     * Determines the branch in the ontology which {@code cls} belongs to, among the branches 
     * usable for annotations. The {@code OWLClass} returned can only be one 
     * of the {@code OWLClass}es returned by {@link getValidBranches()}.
     * <p>
     * If {@code cls} does not belong to any of these branches, 
     * an {@code IllegalArgumentException} is thrown (terms belonging to other branches 
     * of the ontology are only used to compose terms usable for annotations).
     * 
     * @param cls   An {@code OWLClass} for which we want to determine the branch it belongs to.
     * @return      An {@code OWLClass} that is the root of the branch {@code cls} belongs to.
     * @throws IllegalArgumentException If {@code cls} does not belong to any 
     *                                  of the branches listed.
     * @see #getValidBranches()
     */
    public OWLClass getValidBranch(OWLClass cls) throws IllegalArgumentException {
        log.entry(cls);
        
        Set<OWLClass> relatedClasses = new HashSet<OWLClass>();
        relatedClasses.add(cls);
        relatedClasses.addAll(this.getWrapper().getAncestorsThroughIsA(cls));
        relatedClasses.retainAll(this.getValidBranches());
        if (relatedClasses.size() != 1) {
            throw log.throwing(new IllegalArgumentException(cls + " does not belong to "
                    + "any branch among valid branches: " + this.getValidBranches()));
        }
        return log.exit(relatedClasses.iterator().next());
    }
    /**
     * Retrieves {@code OWLClass}es that are the root of subgraphs containing 
     * terms that are directly usable for annotations. The {@code OWLClass} are of 7 types: 
     * <ul>
     * <li>equal to {@link #getSingleEvidenceConf()}
     * <li>equal to {@link #getCongruentSameTypeEvidenceConf()}
     * <li>equal to {@link #getCongruentMultipleTypesEvidenceConf()}
     * <li>equal to {@link #getWeakConflictSameTypeEvidenceConf()}
     * <li>equal to {@link #getWeakConflictMultipleTypesEvidenceConf()}
     * <li>equal to {@link #getStrongConflictSameTypeEvidenceConf()}
     * <li>equal to {@link #getStrongConflictMultipleTypesEvidenceConf()}
     * </ul>
     * <p>
     * Terms belonging to other branches of the ontology are only used to compose terms 
     * usable for annotations.
     * 
     * @return  A {@code Set} of {@code OWLClass}es representing roots of subgraphs 
     *          containing valid terms for annotations. The {@code Set} is a copy and 
     *          can be safely modified. 
     * @see #getValidBranch(OWLClass)
     */
    public Set<OWLClass> getValidBranches() {
        Set<OWLClass> validBranches = new HashSet<OWLClass>();
        validBranches.add(this.getSingleEvidenceConf());
        validBranches.add(this.getCongruentSameTypeEvidenceConf());
        validBranches.add(this.getCongruentMultipleTypesEvidenceConf());
        validBranches.add(this.getWeakConflictSameTypeEvidenceConf());
        validBranches.add(this.getWeakConflictMultipleTypesEvidenceConf());
        validBranches.add(this.getStrongConflictSameTypeEvidenceConf());
        validBranches.add(this.getStrongConflictMultipleTypesEvidenceConf());
        return validBranches;
    }
    
    /**
     * Extract the confidence level from a term providing confidence level information 
     * (for example 'high confidence from single evidence', or 'confidence from congruent 
     * evidence lines of same type, best confidence high'). The {@code OWLClass} returned 
     * can only be one of the {@code OWLClass}es returned by {@link #getConfidenceLevels()}.
     * <p>
     * If {@code cls} does not provide confidence level information, 
     * an {@code IllegalArgumentException} is thrown.
     * 
     * @param cls   An {@code OWLClass} for which we want to extract the confidence level.
     * @return      An {@code OWLClass} that is the confidence level.
     * @throws IllegalArgumentException If {@code cls} does not provide confidence level.
     * @see #getConfidenceLevels()
     * @see #isProvidingConfidenceLevelInfo()
     */
    public OWLClass extractConfidenceLevel(OWLClass cls) throws IllegalArgumentException {
        log.entry(cls);
        Set<OWLClass> relatedClasses = new HashSet<OWLClass>();
        relatedClasses.add(cls);
        relatedClasses.addAll(this.getWrapper().getAncestorsThroughIsA(cls));
        relatedClasses.retainAll(this.getOrderedConfidenceLevels());
        if (relatedClasses.size() != 1) {
            throw log.throwing(new IllegalArgumentException(cls + " does not provide "
                    + "confidence level information"));
        }
        return log.exit(relatedClasses.iterator().next());
    }
    /**
     * Determines whether {@code cls} provides a confidence level information. For instance, 
     * the terms 'high confidence from single evidence', or 'confidence from congruent 
     * evidence lines of same type, best confidence high'. More formally, this method 
     * checks whether {@code cls} is equal to or is a descendant of the {@code OWLClass}es 
     * returned by {@link #getConfidenceLevels()}.
     * 
     * @param cls   An {@code OWLClass} to check whether it provides confidence level information.
     * @return      {@code true} if {@code cls} provides confidence level information.
     * @see #getConfidenceLevels()
     * @see #getConfidenceLevel(OWLClass)
     */
    public boolean isProvidingConfidenceLevelInfo(OWLClass cls) {
        log.entry(cls);
        try {
            this.extractConfidenceLevel(cls);
            return log.exit(true);
        } catch (IllegalArgumentException e) {
            return log.exit(false);
        }
    }
    /**
     * @return  An unmodifiable {@code List} of {@code OWLClass}es that are 
     *          all confidence levels from the CIO provided, except the 'rejected' term, 
     *          ordered by ascending confidence level. 
     */
    public List<OWLClass> getOrderedConfidenceLevels() {
        return Arrays.asList(this.getLowConfLevel(), this.getMediumConfLevel(), 
                this.getHighConfLevel());
        
    }

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
//        return log.exit(this.isBelongingToSubgraph(cls, this.getSingleEvidenceConf()));
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
//        return log.exit(this.isBelongingToSubgraph(cls, this.getCongruentConcordance()));
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
//        return log.exit(this.isBelongingToSubgraph(cls, this.getWeaklyConflictingConcordance()));
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
//        return log.exit(this.isBelongingToSubgraph(cls, this.getStronglyConflictingConcordance()));
//    }
    
    /**
     * Determines whether {@code cls} is equal to or is a descendant of the {@code OWLClass} 
     * returns by {@link #getRejectedTerm()}.
     * 
     * @param cls   An {@code OWLClass} to determine whether it corresponds to a 'rejected' 
     *              confidence level.
     * @return      {@code true} if {@code cls} corresponds to a 'rejected' confidence level.
     * @throws NullPointerException     if {@code cls} is {@code null}.
     * @see #getRejectedTerm()
     */
    public boolean isRejectedConfidenceInformation(OWLClass cls) {
        log.entry(cls);
        return log.exit(this.isBelongingToSubgraph(cls, this.getRejectedTerm()));
    }
    
    /**
     * Checks whether {@code cls} belongs to the is_a subgraph with {@code subgraphRoot} as root. 
     * More formally, this methods checks whether {@code cls} is equal to {@code subgraphRoot}, 
     * or whether {@code subgraphRoot} is an ancestor of {@code cls} through is_a relations, 
     * even indirect. is_a relations are inferred by owltools, so this includes 
     * EquivalentClasses axiom, etc.
     * 
     * @param cls           An {@code OWLClass} to check for membership of the subgraph 
     *                      with {@code subgraphRoot} as root.
     * @param subgraphRoot  An {@code OWLClass} that is the root of the subgraph for which 
     *                      we want to know whether it contains {@code cls}.
     * @return              {@code true} if {@code cls} is part of the subgraph of is_a 
     *                      relations starting from {@code subgraphRoot}.
     * @throws NullPointerException     if {@code cls} is {@code null}.
     */
    private boolean isBelongingToSubgraph(OWLClass cls, OWLClass subgraphRoot) {
        log.entry(cls, subgraphRoot);
        if (subgraphRoot.equals(cls)) {
            return log.exit(true);
        }
        if (this.getWrapper().getAncestorsThroughIsA(cls).contains(subgraphRoot)) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    //*************************************
    //  GETTERS 
    //*************************************
    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #SINGLE_EVIDENCE_CONF_ID}.
     * @see #SINGLE_EVIDENCE_CONF_ID
     */
    public OWLClass getSingleEvidenceConf() {
        return singleEvidenceConf;
    }
    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #REJECTED_TERM_ID}.
     * @see #REJECTED_TERM_ID
     */
    public OWLClass getRejectedTerm() {
        return rejectedTerm;
    }
    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #MULTIPLE_EVIDENCE_CONF_ID}.
     * @see #MULTIPLE_EVIDENCE_CONF_ID
     */
    public OWLClass getMultipleEvidenceConf() {
        return multipleEvidenceConf;
    }
    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #CONGRUENT_SAME_TYPE_EVIDENCE_CONF_ID}.
     * @see #CONGRUENT_SAME_TYPE_EVIDENCE_CONF_ID
     */
    public OWLClass getCongruentSameTypeEvidenceConf() {
        return congruentSameTypeEvidenceConf;
    }
    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #CONGRUENT_MULTIPLE_TYPES_EVIDENCE_CONF_ID}.
     * @see #CONGRUENT_MULTIPLE_TYPES_EVIDENCE_CONF_ID
     */
    public OWLClass getCongruentMultipleTypesEvidenceConf() {
        return congruentMultipleTypesEvidenceConf;
    }
    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #WEAK_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID}.
     * @see #WEAK_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID
     */
    public OWLClass getWeakConflictSameTypeEvidenceConf() {
        return weakConflictSameTypeEvidenceConf;
    }
    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #WEAK_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID}.
     * @see #WEAK_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID
     */
    public OWLClass getWeakConflictMultipleTypesEvidenceConf() {
        return weakConflictMultipleTypesEvidenceConf;
    }
    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #STRONG_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID}.
     * @see #STRONG_CONFLICT_SAME_TYPE_EVIDENCE_CONF_ID
     */
    public OWLClass getStrongConflictSameTypeEvidenceConf() {
        return strongConflictSameTypeEvidenceConf;
    }
    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #STRONG_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID}.
     * @see #STRONG_CONFLICT_MULTIPLE_TYPES_EVIDENCE_CONF_ID
     */
    public OWLClass getStrongConflictMultipleTypesEvidenceConf() {
        return strongConflictMultipleTypesEvidenceConf;
    }

    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #HIGH_CONF_LEVEL_ID}.
     * @see #HIGH_CONF_LEVEL_ID
     */
    public OWLClass getHighConfLevel() {
        return highConfLevel;
    }
    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #MEDIUM_CONF_LEVEL_ID}.
     * @see #MEDIUM_CONF_LEVEL_ID
     */
    public OWLClass getMediumConfLevel() {
        return mediumConfLevel;
    }
    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #LOW_CONF_LEVEL_ID}.
     * @see #LOW_CONF_LEVEL_ID
     */
    public OWLClass getLowConfLevel() {
        return lowConfLevel;
    }
    
    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #CONGRUENT_CONCORDANCE_ID}.
     * @see #CONGRUENT_CONCORDANCE_ID
     */
    public OWLClass getCongruentConcordance() {
        return congruentConcordance;
    }
    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #WEAKLY_CONFLICTING_CONCORDANCE_ID}.
     * @see #WEAKLY_CONFLICTING_CONCORDANCE_ID
     */
    public OWLClass getWeaklyConflictingConcordance() {
        return weaklyConflictingConcordance;
    }
    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #STRONGLY_CONFLICTING_CONCORDANCE_ID}.
     * @see #STRONGLY_CONFLICTING_CONCORDANCE_ID
     */
    public OWLClass getStronglyConflictingConcordance() {
        return stronglyConflictingConcordance;
    }

    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #SAME_TYPE_EVIDENCE_CONCORDANCE_ID}.
     * @see #SAME_TYPE_EVIDENCE_CONCORDANCE_ID
     */
    public OWLClass getSameTypeEvidenceConcordance() {
        return sameTypeEvidenceConcordance;
    }
    /**
     * @return  An {@code OWLClass} corresponding to the term from provided ontology 
     *          with OBO-like ID {@link #DIFFERENT_TYPES_EVIDENCE_CONCORDANCE_ID}.
     * @see #DIFFERENT_TYPES_EVIDENCE_CONCORDANCE_ID
     */
    public OWLClass getDifferentTypesEvidenceConcordance() {
        return differentTypesEvidenceConcordance;
    }
}
