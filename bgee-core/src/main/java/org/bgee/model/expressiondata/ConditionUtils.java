package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;

/**
 * Class providing common operations on {@link Condition}s.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Dec. 2015
 * @since Bgee 13 Dec. 2015
 */
public class ConditionUtils {

    private static final Logger log = LogManager.getLogger(ConditionUtils.class.getName());
    
    /**
     * @see #getConditions()
     */
    private final Set<Condition> conditions;
    /**
     * A {@code ServiceFactory} allowing to acquire the {@code Service}s necessary to {@code ConditionUtils}.
     */
    private final ServiceFactory serviceFactory;
    
    /**
     * @see #getAnatEntityOntology()
     */
    private final Ontology<AnatEntity> anatEntityOnt;
    /**
     * @see #getDevStageOntology()
     */
    private final Ontology<DevStage> devStageOnt;
    
    public ConditionUtils(Collection<Condition> conditions, ServiceFactory serviceFactory) 
            throws IllegalArgumentException {
        if (conditions == null || conditions.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some conditions must be provided."));
        }
        if (serviceFactory == null) {
            throw log.throwing(new IllegalArgumentException("A ServiceFactory must be provided."));
        }
        
        this.conditions = new HashSet<>(conditions);
        this.serviceFactory = serviceFactory;
        
        Set<String> anatEntityIds = new HashSet<>();
        Set<String> devStageIds = new HashSet<>();
        for (Condition cond: this.conditions) {
            anatEntityIds.add(cond.getAnatEntityId());
            devStageIds.add(cond.getDevStageId());
        }
        
        OntologyService ontService = this.serviceFactory.getOntologyService();
        this.anatEntityOnt = ontService.getAnatEntityOntology(anatEntityIds, 
                this.serviceFactory.getAnatEntityService());
        this.devStageOnt = ontService.getDevStageOntology(devStageIds, 
                this.serviceFactory.getDevStageService());
    }
    
    /**
     * Determines whether the second condition is more precise than the first condition. 
     * "More precise" means that the anatomical structure of {@code secondCond} would be a descendant 
     * of the anatomical structure of {@code firstCond}, and the developmental stage 
     * of {@code secondCond} would be a descendant of the developmental stage of {@code firstCond}.
     * 
     * @param firstCond     The first {@code Condition} to be checked for relations to {@code secondCond}. 
     * @param secondCond    The second {@code Condition} to be checked for relations to {@code firstCond}. 
     * @return              {@code true} if {@code secondCond} is more precise than {@code firstCond}.
     * @throws IllegalArgumentException If one of the provided {@code Condition}s is not registered 
     *                                  to this {@code ConditionUtils}.
     */
    public boolean isConditionMorePrecise(Condition firstCond, Condition secondCond) throws IllegalArgumentException {
        log.entry(firstCond, secondCond);
        if (!this.getConditions().contains(firstCond) || !this.getConditions().contains(secondCond)) {
            throw log.throwing(new IllegalArgumentException("Some of the provided conditions "
                    + "are not registered to this ConditionUtils. First condition: " + firstCond 
                    + " - Second condition: " + secondCond));
        }
        if (firstCond.equals(secondCond)) {
            return log.exit(false);
        }
        
        //Of note, computations are three times faster when checking stages before anat. entities. 
        
        if (!firstCond.getDevStageId().equals(secondCond.getDevStageId()) && 
                !this.devStageOnt.getAncestors(
                        this.devStageOnt.getElement(secondCond.getDevStageId()))
                .contains(this.devStageOnt.getElement(firstCond.getDevStageId()))) {
            return log.exit(false);
        }
        
        if (!firstCond.getAnatEntityId().equals(secondCond.getAnatEntityId()) && 
                !this.anatEntityOnt.getAncestors(
                        this.anatEntityOnt.getElement(secondCond.getAnatEntityId()))
                .contains(this.anatEntityOnt.getElement(firstCond.getAnatEntityId()))) {
            return log.exit(false);
        }
        
        return log.exit(true);
    }
    
    /**
     * Retrieve an {@code AnatEntity} present in a {@code Condition} provided at instantiation, 
     * based on its ID.
     * 
     * @param anatEntityId  A {@code String} that is the ID of the {@code AnatEntity} to retrieve.
     * @return              The corresponding {@code AnatEntity}. {@code null} if no corresponding 
     *                      {@code AnatEntity} was present in the {@code Condition}s provided 
     *                      at instantiation.
     */
    public AnatEntity getAnatEntity(String anatEntityId) {
        log.entry(anatEntityId);
        return log.exit(this.getAnatEntityOntology().getElement(anatEntityId));
    }
    /**
     * Retrieve a {@code DevStage} present in a {@code Condition} provided at instantiation, 
     * based on its ID.
     * 
     * @param devStageId    A {@code String} that is the ID of the {@code DevStage} to retrieve.
     * @return              The corresponding {@code DevStage}. {@code null} if no corresponding 
     *                      {@code DevStage} was present in the {@code Condition}s provided 
     *                      at instantiation.
     */
    public DevStage getDevStage(String devStageId) {
        log.entry(devStageId);
        return log.exit(this.getDevStageOntology().getElement(devStageId));
    }
    
    //*********************************
    //  GETTERS/SETTERS
    //*********************************
    /**
     * @return  The {@code Set} of {@code Condition}s to be considered for operations on this {@code ConditionUtils}.
     */
    public Set<Condition> getConditions() {
        return conditions;
    }
    /**
     * @return  An {@code Ontology} of {@code AnatEntity}s used to infer relations between {@code Condition}s. 
     *          Contains only {@code AnatEntity}s and relations for entities present 
     *          in the {@code Condition}s provided at instantiation.
     * @see #getDevStageOntology()
     */
    public Ontology<AnatEntity> getAnatEntityOntology() {
        return anatEntityOnt;
    }
    /**
     * @return  An {@code Ontology} of {@code DevStage}s used to infer relations between {@code Condition}s. 
     *          Contains only {@code DevStage}s and relations for entities present 
     *          in the {@code Condition}s provided at instantiation.
     * @see #getAnatEntityOntology()
     */
    public Ontology<DevStage> getDevStageOntology() {
        return devStageOnt;
    }
    
}
