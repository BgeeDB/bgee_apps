package org.bgee.pipeline.ontologycommon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.elk.reasoner.saturation.conclusions.ForwardLink.ThisBackwardLinkRule;
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
     * of the term "confidence from multiple evidence lines".
     * @see #SINGLE_EVIDENCE_CONF_ID
     */
    public final static String MULTIPLE_EVIDENCE_CONF_ID = "CIO:0000002";
    
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "high confidence level".
     * @see #MEDIUM_CONF_LEVEL_ID
     * @see #LOW_CONF_LEVEL_ID
     * @see #REJECTED_ID
     */
    public final static String HIGH_CONF_LEVEL_ID = "CIO:0000029";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "medium confidence level".
     * @see #HIGH_CONF_LEVEL_ID
     * @see #LOW_CONF_LEVEL_ID
     * @see #REJECTED_ID
     */
    public final static String MEDIUM_CONF_LEVEL_ID = "CIO:0000030";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "low confidence level".
     * @see #MEDIUM_CONF_LEVEL_ID
     * @see #HIGH_CONF_LEVEL_ID
     * @see #REJECTED_ID
     */
    public final static String LOW_CONF_LEVEL_ID = "CIO:0000031";
    /**
     * A {@code String} that is the OBO-like ID from the confidence information ontology 
     * of the term "rejected".
     * @see #LOW_CONF_LEVEL_ID
     * @see #MEDIUM_CONF_LEVEL_ID
     * @see #HIGH_CONF_LEVEL_ID
     */
    public final static String REJECTED_ID = "CIO:0000039";

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
        if (this.singleEvidenceConf == null || this.multipleEvidenceConf == null) {
            throw log.throwing(new IllegalArgumentException("The ontology used does not allow "
                + "to retrieve 'single evidence' and 'multiple evidence lines' confidences. "
                + "Single evidence: " + this.singleEvidenceConf + 
                " - Multiple evidence lines: " + this.multipleEvidenceConf));
        }
        
        this.highConfLevel = this.getWrapper().getOWLClassByIdentifier(HIGH_CONF_LEVEL_ID);
        this.mediumConfLevel = this.getWrapper().getOWLClassByIdentifier(MEDIUM_CONF_LEVEL_ID);
        this.lowConfLevel = this.getWrapper().getOWLClassByIdentifier(LOW_CONF_LEVEL_ID);
        this.rejectedTerm = this.getWrapper().getOWLClassByIdentifier(REJECTED_ID);
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
    
//    public OWLClass getConfidenceLevel(OWLClass cls) {
//        log.entry(cls);
//        Set<OWLClass> ancestors = this.getWrapper().getAncestorsThroughIsA(cls);
//        
//    }
    
    /**
     * Determines the best confidence level among {@code singleEvidenceCioTerms}. 
     * {@code singleEvidenceCioTerms} must all be part of the 'single evidence' branch 
     * ({@link #isSingleEvidenceConfidenceInformation()} returns {@code true}), 
     * 
     * @param singleEvidenceCioTerms
     * @return
     */
    //single evidende only, or just check that they all belong to the same branch? (congruent, weakly, single)
    public OWLClass getBestConfidenceLevel(Set<OWLClass> cioTerms) {
        log.entry(cioTerms);
    }
    
    /**
     * Determines whether {@code cls} is equal to or is a descendant of the {@code OWLClass} 
     * returns by {@link #getSingleEvidenceConf()}.
     * 
     * @param cls   An {@code OWLClass} to determine whether it belongs to 
     *              the 'single evidence' branch.
     * @return      {@code true} if {@code cls} belongs to the 'single evidence' branch.
     * @throws NullPointerException     if {@code cls} is {@code null}.
     * @see #getSingleEvidenceConf()
     */
    public boolean isSingleEvidenceConfidenceInformation(OWLClass cls) {
        log.entry(cls);
        if (this.getSingleEvidenceConf().equals(cls)) {
            return log.exit(true);
        }
        if (this.getWrapper().getAncestorsThroughIsA(cls).contains(this.getSingleEvidenceConf())) {
            return log.exit(true);
        }
        return log.exit(false);
    }

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
        if (this.getRejectedTerm().equals(cls)) {
            return log.exit(true);
        }
        if (this.getWrapper().getAncestorsThroughIsA(cls).contains(this.getRejectedTerm())) {
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
     *          with OBO-like ID {@link #MULTIPLE_EVIDENCE_CONF_ID}.
     * @see #MULTIPLE_EVIDENCE_CONF_ID
     */
    public OWLClass getMultipleEvidenceConf() {
        return multipleEvidenceConf;
    }

    /**
     * @return  A {@code Set} of {@code OWLClass}es that are all confidence levels 
     *          from the CIO provided, except the 'rejected' term 
     *          (see {@link #getRejectedTerm()}). This {@code Set} is a copy and 
     *          can be safely modified.
     */
    public Set<OWLClass> getConfidenceLevels() {
        return new HashSet<OWLClass>(Arrays.asList(
                this.getHighConfLevel(), 
                this.getMediumConfLevel(), 
                this.getLowConfLevel()));
        
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
     *          with OBO-like ID {@link #REJECTED_ID}.
     * @see #REJECTED_ID
     */
    public OWLClass getRejectedTerm() {
        return rejectedTerm;
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
