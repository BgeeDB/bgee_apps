package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Parent class of classes allowing to filter different types of {@code BaseCondition}s.
 *
 * @author Frederic Bastian
 * @version Bgee 14, Sept 2018
 * @since Bgee 14, Sept 2018
 *
 * @param <T>   The type of {@code BaseCondition} that will be treated by the subclasses.
 */
public abstract class BaseConditionFilter<T extends BaseCondition<?>> implements Predicate<T> {
    private final static Logger log = LogManager.getLogger(BaseConditionFilter.class.getName());
    
    /**
     * @see #getAnatEntityIds()
     */
    private final Set<String> anatEntityIds;
    /**
     * @see #getDevStageIds()
     */
    private final Set<String> devStageIds;
    /**
     * @see #getCellTypeIds()
     */
    private final Set<String> cellTypeIds;
    /**
     * @see #getSexes()
     */
    private final Set<String> sexes;
    /**
     * @see #getStrains()
     */
    private final Set<String> strains;

    /**
     * @param anatEntityIds        A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities that this {@code ConditionFilter} 
     *                              will specify to use.
     * @param devStageIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the developmental stages that this {@code ConditionFilter} 
     *                              will specify to use.
     * @param cellTypeIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities describing cell types that this 
     *                              {@code ConditionFilter} will specify to use.
     * @param sexes                 A {@code Collection} of {@code String}s that are the Names 
     *                              of the sexes that this {@code ConditionFilter} will specify 
     *                              to use.
     * @param strains               A {@code Collection} of {@code String}s that are the Names 
     *                              of the strains that this {@code ConditionFilter} will 
     *                              specify to use.
     * @throws IllegalArgumentException If no anatomical entity IDs nor developmental stage IDs are provided. 
     */
    public BaseConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds, 
            Collection<String> cellTypeIds, Collection<String> sexes, Collection<String> strains)
            throws IllegalArgumentException {
        this.anatEntityIds = Collections.unmodifiableSet(anatEntityIds == null ? 
                new HashSet<>(): new HashSet<>(anatEntityIds));
        this.devStageIds = Collections.unmodifiableSet(devStageIds == null? 
                new HashSet<>(): new HashSet<>(devStageIds));
        this.cellTypeIds = Collections.unmodifiableSet(cellTypeIds == null? 
                new HashSet<>(): new HashSet<>(devStageIds));
        this.sexes = Collections.unmodifiableSet(sexes == null? 
                new HashSet<>(): new HashSet<>(devStageIds));
        this.strains = Collections.unmodifiableSet(strains == null? 
                new HashSet<>(): new HashSet<>(devStageIds));
    }


    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the anatomical entities that this {@code ConditionFilter} will specify to use.
     */
    public Set<String> getAnatEntityIds() {
        return anatEntityIds;
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
     *          of the cell types that this {@code ConditionFilter} will specify to use.
     */
    public Set<String> getCellTypeIds() {
        return cellTypeIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the sexes that this 
     * {@code ConditionFilter} will specify to use.
     */
    public Set<String> getSexes() {
        return sexes;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the strains that 
     * this {@code ConditionFilter} will specify to use.
     */
    public Set<String> getStrains() {
        return strains;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatEntityIds == null) ? 0 : anatEntityIds.hashCode());
        result = prime * result + ((cellTypeIds == null) ? 0 : cellTypeIds.hashCode());
        result = prime * result + ((devStageIds == null) ? 0 : devStageIds.hashCode());
        result = prime * result + ((sexes == null) ? 0 : sexes.hashCode());
        result = prime * result + ((strains == null) ? 0 : strains.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseConditionFilter<?> other = (BaseConditionFilter<?>) obj;
        if (anatEntityIds == null) {
            if (other.anatEntityIds != null)
                return false;
        } else if (!anatEntityIds.equals(other.anatEntityIds))
            return false;
        if (cellTypeIds == null) {
            if (other.cellTypeIds != null)
                return false;
        } else if (!cellTypeIds.equals(other.cellTypeIds))
            return false;
        if (devStageIds == null) {
            if (other.devStageIds != null)
                return false;
        } else if (!devStageIds.equals(other.devStageIds))
            return false;
        if (sexes == null) {
            if (other.sexes != null)
                return false;
        } else if (!sexes.equals(other.sexes))
            return false;
        if (strains == null) {
            if (other.strains != null)
                return false;
        } else if (!strains.equals(other.strains))
            return false;
        return true;
    }


    /**
     * Evaluates this {@code BaseConditionFilter} on the given {@code BaseCondition}.
     * 
     * @param condition A {@code BaseCondition} that is the condition to be evaluated.
     * @return          {@code true} if the {@code condition} matches the {@code BaseConditionFilter}.
     */
    @Override
    public boolean test(T condition) {
        log.entry(condition);

        boolean isValid = true;
        
        // Check dev. stage ID 
        if (condition.getDevStageId() != null 
            && this.getDevStageIds() != null && !this.getDevStageIds().isEmpty()
            && !this.getDevStageIds().contains(condition.getDevStageId())) {
            log.debug("Dev. stage {} not validated: not in {}",
                condition.getDevStageId(), this.getDevStageIds());
            isValid = false;
        }
    
        // Check anat. entity ID 
        if (condition.getAnatEntityId() != null 
            && this.getAnatEntityIds() != null && !this.getAnatEntityIds().isEmpty()
            && !this.getAnatEntityIds().contains(condition.getAnatEntityId())) {
            log.debug("Anat. entity {} not validated: not in {}",
                condition.getAnatEntityId(), this.getAnatEntityIds());
            isValid = false;
        }
        
        // Check cell type ID 
        if (condition.getCellTypeId() != null 
            && this.getCellTypeIds() != null && !this.getCellTypeIds().isEmpty()
            && !this.getCellTypeIds().contains(condition.getCellTypeId())) {
            log.debug("Cell type {} not validated: not in {}",
                condition.getCellTypeId(), this.getCellTypeIds());
            isValid = false;
        }
        
        // Check strain name
        if (condition.getStrain() != null 
            && this.getStrains() != null && !this.getStrains().isEmpty()
            && !this.getStrains().contains(condition.getStrain())) {
            log.debug("Strain {} not validated: not in {}",
                condition.getStrain(), this.getStrains());
            isValid = false;
        }
        // Check sex name
        if (condition.getSex() != null 
            && this.getSexes() != null && !this.getSexes().isEmpty()
            && !this.getSexes().contains(condition.getSex())) {
            log.debug("Sex {} not validated: not in {}",
                condition.getSex(), this.getSexes());
            isValid = false;
        }
        
        return log.traceExit(isValid);
    }
}
