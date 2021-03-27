package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A filter to parameterize queries using expression data {@link Condition}s.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 15.0, Mar. 2021
 * @since   Bgee 13, Oct. 2015
 */
//TODO: be able to EXCLUDE anat. entities/stages. It would be convenient to discard
//non-informative anat. entities.
public class ConditionFilter extends BaseConditionFilter<Condition> {
    private final static Logger log = LogManager.getLogger(ConditionFilter.class.getName());

    /**
     * @see #getSexes()
     */
    private final Set<String> sexes;
    /**
     * @see #getObservedConditions()
     */
    private final Boolean observedConditions;

    /**
     * @param anatEntityIds        A {@code Collection} of {@code String}s that are the IDs 
     * @param anatEntityIds         A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities that this {@code ConditionFilter} 
     *                              will specify to use.
     * @throws IllegalArgumentException If no anatomical entity IDs are provided. 
     */
    public ConditionFilter(Collection<String> anatEntityIds)
            throws IllegalArgumentException {
        this(anatEntityIds, null, null, null, null, null);
    }

    /**
     * @param anatEntityIds         A {@code Collection} of {@code String}s that are the IDs 
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
     * @throws IllegalArgumentException If no anatomical entity IDs nor developmental stage IDs
     *                              are provided. 
     */
    public ConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String> sexes, Collection<String> strains)
            throws IllegalArgumentException {
        this(anatEntityIds, devStageIds, cellTypeIds, sexes, strains, null);
    }
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
     * @param observedConditions    A {@code Boolean} defining whether the conditions considered
     *                              should have been observed in expression data in any species.
     *                              See {@link #getObservedConditions()} for more details.
     * @throws IllegalArgumentException If no anatomical entity IDs nor developmental stage IDs
     *                                  nor observed status are provided. 
     */
    //XXX: Should we add two booleans to ask for considering sub-structures and sub-stages?
    //Because it seems it can be managed through query of data propagation in CallFilter
    public ConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String> sexes, Collection<String> strains, 
            Boolean observedConditions) throws IllegalArgumentException {
        super(anatEntityIds, devStageIds, cellTypeIds, strains);
        if ((anatEntityIds == null || anatEntityIds.isEmpty()) &&
                (devStageIds == null || devStageIds.isEmpty()) &&
                (cellTypeIds == null || cellTypeIds.isEmpty()) &&
                (sexes == null || sexes.isEmpty()) &&
                (strains == null || strains.isEmpty()) &&
                observedConditions == null) {
            throw log.throwing(new IllegalArgumentException("Some anatatomical entity IDs, "
                + "developmental stage IDs, cell type IDs, sexe, strain IDs or observed data "
                + "status must be provided."));
        }
        this.sexes = Collections.unmodifiableSet(sexes == null? 
                new HashSet<>(): new HashSet<>(sexes));
        this.observedConditions = observedConditions;
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the sexes that this 
     * {@code ConditionFilter} will specify to use.
     */
    public Set<String> getSexes() {
        return sexes;
    }
    /**
     * @return  A {@code Boolean} defining whether the conditions considered should have been
     *          observed in expression data. If {@code true}, only conditions
     *          observed in expression data are considered, not resulting
     *          only from a data propagation; if {@code false}, only conditions resulting
     *          from data propagation, never observed in expression data,
     *          are considered; if {@code null}, conditions are considered whatever
     *          their observed data status.
     */
    public Boolean getObservedConditions() {
        return observedConditions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((sexes == null) ? 0 : sexes.hashCode());
        result = prime * result + ((observedConditions == null) ? 0 : observedConditions.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConditionFilter other = (ConditionFilter) obj;
        if (sexes == null) {
            if (other.sexes != null)
                return false;
        } else if (!sexes.equals(other.sexes))
            return false;
        if (observedConditions == null) {
            if (other.observedConditions != null)
                return false;
        } else if (!observedConditions.equals(other.observedConditions))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConditionFilter [anatEntityIds=").append(getAnatEntityIds())
               .append(", devStageIds=").append(getDevStageIds())
               .append(", cellTypeIds=").append(getCellTypeIds())
               .append(", sexes=").append(getSexes())
               .append(", strains=").append(getStrains())
               .append(", observedConditions=").append(observedConditions).append("]");
        return builder.toString();
    }


    //Since we cannot use the attribute "observedConditions" to check for the validity of the Condition.

    /**
     * Evaluates this {@code RawDataConditionFilter} on the given {@code RawDataCondition}.
     * 
     * @param condition A {@code RawDataCondition} that is the condition to be evaluated.
     * @return          {@code true} if the {@code condition} matches the {@code RawDataConditionFilter}.
     */
    @Override
    public boolean test(Condition condition) {
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