package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Parent class of classes allowing to filter different types of {@code BaseCondition}s.
 *
 * @author Frederic Bastian
 * @version Bgee 15, Mar. 2021
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
     * @param anatEntityIds        A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities that this {@code ConditionFilter} 
     *                              will specify to use.
     * @param devStageIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the developmental stages that this {@code ConditionFilter} 
     *                              will specify to use.
     * @param cellTypeIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities describing cell types that this 
     *                              {@code ConditionFilter} will specify to use.
     */
    public BaseConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds, 
            Collection<String> cellTypeIds)
            throws IllegalArgumentException {
        this.anatEntityIds = Collections.unmodifiableSet(anatEntityIds == null? new HashSet<>():
            anatEntityIds.stream().filter(id -> StringUtils.isNotBlank(id)).collect(Collectors.toSet()));
        this.cellTypeIds = Collections.unmodifiableSet(cellTypeIds == null? new HashSet<>():
            cellTypeIds.stream().filter(id -> StringUtils.isNotBlank(id)).collect(Collectors.toSet()));
        this.devStageIds = Collections.unmodifiableSet(devStageIds == null? new HashSet<>():
            devStageIds.stream().filter(id -> StringUtils.isNotBlank(id)).collect(Collectors.toSet()));
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

    public abstract boolean areAllCondParamFiltersEmpty();

    /**
     * Evaluates this {@code BaseConditionFilter} on the given {@code BaseCondition}.
     * 
     * @param condition A {@code BaseCondition} that is the condition to be evaluated.
     * @return          {@code true} if the {@code condition} matches the {@code BaseConditionFilter}.
     */
    @Override
    public boolean test(T condition) {
        log.traceEntry("{}", condition);
        
        // Check dev. stage ID 
        if (condition.getDevStageId() != null 
            && this.getDevStageIds() != null && !this.getDevStageIds().isEmpty()
            && !this.getDevStageIds().contains(condition.getDevStageId())) {
            log.debug("Dev. stage {} not validated: not in {}",
                condition.getDevStageId(), this.getDevStageIds());
            return log.traceExit(false);
        }
    
        // Check anat. entity ID 
        if (condition.getAnatEntityId() != null 
            && this.getAnatEntityIds() != null && !this.getAnatEntityIds().isEmpty()
            && !this.getAnatEntityIds().contains(condition.getAnatEntityId())) {
            log.debug("Anat. entity {} not validated: not in {}",
                condition.getAnatEntityId(), this.getAnatEntityIds());
            return log.traceExit(false);
        }
        
        // Check cell type ID 
        if (condition.getCellTypeId() != null 
            && this.getCellTypeIds() != null && !this.getCellTypeIds().isEmpty()
            && !this.getCellTypeIds().contains(condition.getCellTypeId())) {
            log.debug("Cell type {} not validated: not in {}",
                condition.getCellTypeId(), this.getCellTypeIds());
            return log.traceExit(false);
        }
        
        return log.traceExit(true);
    }


    @Override
    public int hashCode() {
        return Objects.hash(anatEntityIds, cellTypeIds, devStageIds);
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
        return Objects.equals(anatEntityIds, other.anatEntityIds) && Objects.equals(cellTypeIds, other.cellTypeIds)
                && Objects.equals(devStageIds, other.devStageIds);
    }
}