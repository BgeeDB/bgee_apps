package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.BaseConditionFilter;

/**
 * A filter to parameterize queries using {@link RawDataCondition}s.
 * 
 * @author  Frederic Bastian
 * @version Bgee 14, Sept 2018
 * @since   Bgee 14, Sept 2018
 */
public class RawDataConditionFilter extends BaseConditionFilter<RawDataCondition> {
    private final static Logger log = LogManager.getLogger(RawDataConditionFilter.class.getName());

    /**
     * @see #getSexes()
     */
    private final Set<String> sexes;
    /**
     * @see #getStrains()
     */
    private final Set<String> strains;
    /**
     * @see #getIncludeSubConditions()
     */
    private final boolean includeSubConditions;
    /**
     * @see #getIncludeParentConditions()
     */
    private final boolean includeParentConditions;

    /**
     * @param anatEntityIds             A {@code Collection} of {@code String}s that are the IDs 
     *                                  of the anatomical entities to use.
     * @param devStageIds               A {@code Collection} of {@code String}s that are the IDs 
     *                                  of the developmental stages to use.
     * @param cellTypeIds               A {@code Collection} of {@code String}s that are the IDs 
     *                                  of the anatomical entities describing cell types that this 
     *                                  {@code ConditionFilter} will specify to use.
     * @param sexes                     A {@code Collection} of {@code String}s that are the Names 
     *                                  of the sexes that this {@code ConditionFilter} will specify 
     *                                  to use.
     * @param strains                   A {@code Collection} of {@code String}s that are the Names 
     *                                  of the strains that this {@code ConditionFilter} will 
     *                                  specify to use.
     * @param includeSubConditions      A {@code boolean} defining whether the sub-conditions
     *                                  of the targeted raw conditions, from which calls of presence
     *                                  of expression are propagated, should be retrieved.
     * @param includeParentConditions   A {@code boolean} defining whether the parent conditions
     *                                  of the targeted raw conditions, from which calls of absence
     *                                  of expression are propagated, should be retrieved.
     * @throws IllegalArgumentException If no anatomical entity IDs nor developmental stage IDs are provided. 
     */
    public RawDataConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String> sexes, Collection<String> strains, boolean includeSubConditions, 
            boolean includeParentConditions)
            throws IllegalArgumentException {
        super(anatEntityIds, devStageIds, cellTypeIds);
        if ((anatEntityIds == null || anatEntityIds.isEmpty()) &&
                (devStageIds == null || devStageIds.isEmpty()) &&
                (cellTypeIds == null || cellTypeIds.isEmpty()) &&
                (sexes == null || sexes.isEmpty()) &&
                (strains == null || strains.isEmpty())) {
            throw log.throwing(new IllegalArgumentException("Some anatatomical entity IDs, "
                + "developmental stage IDs, cell type IDs, sexe, or strain IDs "
                + "must be provided."));
        }
        this.sexes = Collections.unmodifiableSet(sexes == null? 
                new HashSet<>(): new HashSet<>(sexes));
        this.strains = Collections.unmodifiableSet(strains == null? 
                new HashSet<>(): new HashSet<>(strains));
        this.includeSubConditions = includeSubConditions;
        this.includeParentConditions = includeParentConditions;
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the sexes that this 
     * {@code RawDataConditionFilter} will specify to use.
     */
    public Set<String> getSexes() {
        return sexes;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the strains that this 
     * {@code RawDataConditionFilter} will specify to use.
     */
    public Set<String> getStrains() {
        return strains;
    }
    /**
     * @return  A {@code boolean} defining whether the sub-conditions of the targeted raw conditions,
     *          from which calls of presence of expression are propagated, should be retrieved.
     */
    public boolean getIncludeSubConditions() {
        return this.includeSubConditions;
    }
    /**
     * @return  A {@code boolean} defining whether the parent conditions of the targeted raw conditions,
     *          from which calls of absence of expression are propagated, should be retrieved.
     */
    public boolean getIncludeParentConditions() {
        return this.includeParentConditions;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((sexes == null) ? 0 : sexes.hashCode());
        result = prime * result + (includeParentConditions ? 1231 : 1237);
        result = prime * result + (includeSubConditions ? 1231 : 1237);
        return result;
    }
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
        RawDataConditionFilter other = (RawDataConditionFilter) obj;
        if (sexes == null) {
            if (other.sexes != null)
                return false;
        } else if (!sexes.equals(other.sexes))
            return false;
        if (includeParentConditions != other.includeParentConditions) {
            return false;
        }
        if (includeSubConditions != other.includeSubConditions) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataConditionFilter [anatEntityIds=").append(getAnatEntityIds())
               .append(", devStageIds=").append(getDevStageIds())
               .append(", cellTypeIds=").append(getCellTypeIds())
               .append(", sexes=").append(getSexes())
               .append(", strains=").append(getStrains())
               .append(", includeSubConditions=").append(includeSubConditions)
               .append(", includeParentConditions=").append(includeParentConditions)
               .append("]");
        return builder.toString();
    }


    //We cannot use the attribute "includeSubConditions" and "includeParentConditions"
    //to check for the validity of the RawDataCondition
    /**
     * Evaluates this {@code RawDataConditionFilter} on the given {@code RawDataCondition}.
     * 
     * @param condition A {@code RawDataCondition} that is the condition to be evaluated.
     * @return          {@code true} if the {@code condition} matches the {@code RawDataConditionFilter}.
     */
    @Override
    public boolean test(RawDataCondition condition) {
        log.traceEntry("{}", condition);

        if (!super.test(condition)) {
            return log.traceExit(false);
        }

        // Check sex name
        if (condition.getSex() != null 
            && this.getSexes() != null && !this.getSexes().isEmpty()
            && this.getSexes().stream().map(s -> s.toLowerCase())
            .noneMatch(s -> s.equals(condition.getSex().getStringRepresentation().toLowerCase()))) {
            log.debug("Sex {} not validated: not in {}",
                condition.getSex(), this.getSexes());
            return log.traceExit(false);
        }
        
        return log.traceExit(true);
    }
}