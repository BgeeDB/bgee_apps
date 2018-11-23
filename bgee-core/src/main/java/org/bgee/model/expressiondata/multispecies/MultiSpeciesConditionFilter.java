package org.bgee.model.expressiondata.multispecies;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A filter to parameterize queries using expression data conditions. 
 * 
 * @author  Julien Wollbrett
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 14, Mar. 2017
 */

public class MultiSpeciesConditionFilter implements Predicate<MultiSpeciesCondition>{
	 private final static Logger log = LogManager.getLogger(MultiSpeciesCondition.class.getName());
	
	/**
     * @see #getAnatEntityIds()
     */
    private final Set<String> anatEntitieIds;
    /**
     * @see #getDevStageIds()
     */
    private final Set<String> devStageIds;
    
    //XXX Do we have to Enum on ECO evidence for similarity relations?
    private final Set<String> ecoIds;
    
    //XXX CIOId. Do we have to Enum on CIO confidence level?
    private final Set<String> cioIds;
	
    public MultiSpeciesConditionFilter(Collection<String> anatEntitieIds, Collection<String> devStageIds, Set<String> ecoIds, Set<String> cioIds)
            throws IllegalArgumentException {
    	if ((anatEntitieIds == null || anatEntitieIds.isEmpty()) && 
                (devStageIds == null || devStageIds.isEmpty())) {
            throw log.throwing(new IllegalArgumentException("Some anatatomical entity IDs,"
                    + " developmental stage IDs or species IDs must be provided."));
        }
        this.anatEntitieIds = Collections.unmodifiableSet(anatEntitieIds == null ? 
                new HashSet<>(): new HashSet<>(anatEntitieIds));
        this.devStageIds = Collections.unmodifiableSet(devStageIds == null? 
                new HashSet<>(): new HashSet<>(devStageIds));
        this.ecoIds = Collections.unmodifiableSet(ecoIds == null ? 
                new HashSet<>(): new HashSet<>(ecoIds));
        this.cioIds = Collections.unmodifiableSet(cioIds == null ? 
                new HashSet<>(): new HashSet<>(cioIds));
	}
    
    
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the anatomical entities that this {@code ConditionFilter} will specify to use.
     */
    public Set<String> getAnatEntityIds() {
        return anatEntitieIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the developmental stages that this {@code ConditionFilter} will specify to use.
     */
    public Set<String> getDevStageIds() {
        return devStageIds;
    }
    
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the confidence level that this {@code ConditionFilter} will specify to use.
     */
    public Set<String> getCioIds() {
        return cioIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the similarity relations that this {@code ConditionFilter} will specify to use.
     */
    public Set<String> getEcoIds() {
        return ecoIds;
    }
    
    @Override
	public boolean test(MultiSpeciesCondition t) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
