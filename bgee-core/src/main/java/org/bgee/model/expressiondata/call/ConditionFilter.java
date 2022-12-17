package org.bgee.model.expressiondata.call;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.BaseConditionFilter;
import org.bgee.model.expressiondata.call.Condition.ConditionEntities;

/**
 * A filter to parameterize queries using expression data {@link Condition}s.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 15.0, May 2021
 * @since   Bgee 13, Oct. 2015
 */
//TODO: be able to EXCLUDE anat. entities/stages. It would be convenient to discard
//non-informative anat. entities.
public class ConditionFilter extends BaseConditionFilter<Condition> {
    private final static Logger log = LogManager.getLogger(ConditionFilter.class.getName());

    /**
     * @see #getSexeIds()
     */
    private final Set<String> sexIds;
    /**
     * @see #getStrainIds()
     */
    private final Set<String> strainIds;
    /**
     * @see #getObservedCondForParams()
     */
    private final EnumSet<CallService.Attribute> observedCondForParams;

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
     * @param sexes                 A {@code Collection} of {@code String}s that are the names
     *                              of the sexes that this {@code ConditionFilter} will specify 
     *                              to use.
     * @param strains               A {@code Collection} of {@code String}s that are the names
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
    public ConditionFilter(ConditionEntities condEntities,
            Collection<CallService.Attribute> observedCondForParams) {
        this(condEntities.getAnatEntityIds(), condEntities.getDevStageIds(),
                condEntities.getCellTypeIds(), condEntities.getSexIds(),
                condEntities.getStrainIds(), observedCondForParams);
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
     * @param sexIds                A {@code Collection} of {@code String}s that are the names
     *                              of the sexes that this {@code ConditionFilter} will specify 
     *                              to use.
     * @param strainIds             A {@code Collection} of {@code String}s that are the names
     *                              of the strains that this {@code ConditionFilter} will 
     *                              specify to use.
     * @param observedCondForParams A {@code Collection} of {@code CallService.Attribute}s specifying
     *                              that the conditions considered should have been observed
     *                              in data annotations (not created only from propagation),
     *                              using the specified condition parameters to perform the check.
     *                              For instance, if this {@code Collection} contains only the parameter
     *                              {@code CallService.Attribute.ANAT_ENTITY_ID}, any condition
     *                              using an anat. entity used in an annotation will be valid
     *                              (but of course, the other attributes of this {@code ConditionFilter}
     *                              will also be considered). If this {@code Collection} contains
     *                              a {@code CallService.Attribute} that is not a condition parameter,
     *                              (see {@link CallService.Attribute#getAllConditionParameters()}),
     *                              an {@code IllegalArgumentException} is thrown. If {@code null}
     *                              or empty, no filtering will be performed on whether
     *                              the global conditions considered have been observed in annotations.
     * @throws IllegalArgumentException If no anatomical entity IDs nor developmental stage IDs
     *                                  nor observed status are provided, or if {@code observedCondForParams}
     *                                  contains {@code CallService.Attribute}s that are not condition
     *                                  parameters. 
     */
    //XXX: Should we add two booleans to ask for considering sub-structures and sub-stages?
    //Because it seems it can be managed through query of data propagation in CallFilter
    //XXX: should we accept Sex as arguments rather than Strings for sexes?
    public ConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String> sexIds, Collection<String> strainIds, 
            Collection<CallService.Attribute> observedCondForParams) throws IllegalArgumentException {
        super(anatEntityIds, devStageIds, cellTypeIds);
        if ((anatEntityIds == null || anatEntityIds.isEmpty()) &&
                (devStageIds == null || devStageIds.isEmpty()) &&
                (cellTypeIds == null || cellTypeIds.isEmpty()) &&
                (sexIds == null || sexIds.isEmpty()) &&
                (strainIds == null || strainIds.isEmpty()) &&
                (observedCondForParams == null || observedCondForParams.isEmpty())) {
            throw log.throwing(new IllegalArgumentException("Some anatatomical entity IDs, "
                + "developmental stage IDs, cell type IDs, sexe, strain IDs or observed data "
                + "status must be provided."));
        }
        this.sexIds = Collections.unmodifiableSet(sexIds == null? 
                new HashSet<>(): new HashSet<>(sexIds));
        this.strainIds = Collections.unmodifiableSet(strainIds == null? 
                new HashSet<>(): new HashSet<>(strainIds));
        this.observedCondForParams = observedCondForParams == null || observedCondForParams.isEmpty()?
                EnumSet.noneOf(CallService.Attribute.class): EnumSet.copyOf(observedCondForParams);
        if (!CallService.Attribute.getAllConditionParameters().containsAll(this.observedCondForParams)) {
            throw log.throwing(new IllegalArgumentException(
                    "A CallService.Attribute that is not a condition parameter was provided"));
        }
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the sexes that this 
     * {@code ConditionFilter} will specify to use.
     */
    public Set<String> getSexIds() {
        return sexIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the strains that this 
     * {@code ConditionFilter} will specify to use.
     */
    public Set<String> getStrainIds() {
        return strainIds;
    }
    /**
     * @return  An {@code EnumSet} of {@code CallService.Attribute}s that are condition parameters,
     *          (see {@link CallService.Attribute#getAllConditionParameters()}), allowing
     *          to request that the conditions considered should have been observed
     *          in data annotations (not created only from propagation),
     *          using the specified condition parameters to perform the check.
     *          For instance, if this {@code EnumSet} contains only the parameter
     *          {@code CallService.Attribute.ANAT_ENTITY_ID}, any condition using an anat. entity
     *          used in an annotation will be valid (but of course, the other attributes of this
     *          {@code ConditionFilter} will also be considered). If empty,
     *          no filtering will be performed on whether the global conditions considered
     *          have been observed in annotations.
     */
    public EnumSet<CallService.Attribute> getObservedCondForParams() {
        return observedCondForParams;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((sexIds == null) ? 0 : sexIds.hashCode());
        result = prime * result + ((strainIds == null) ? 0 : strainIds.hashCode());
        result = prime * result + ((observedCondForParams == null) ? 0 : observedCondForParams.hashCode());
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
        if (sexIds == null) {
            if (other.sexIds != null)
                return false;
        } else if (!sexIds.equals(other.sexIds))
            return false;
        if (strainIds == null) {
            if (other.strainIds != null)
                return false;
        } else if (!strainIds.equals(other.strainIds))
            return false;
        if (observedCondForParams == null) {
            if (other.observedCondForParams != null)
                return false;
        } else if (!observedCondForParams.equals(other.observedCondForParams))
            return false;
        return true;
    }

    public String toParamString() {
        log.traceEntry();
        StringBuilder sb = new StringBuilder();
        boolean previousParams = false;
        if (!getAnatEntityIds().isEmpty()) {
            sb.append(getAnatEntityIds().stream().sorted().collect(Collectors.joining("_"))
                    .replaceAll(" ", "_").replaceAll(":", "_"));
            previousParams = true;
        }
        if (!getDevStageIds().isEmpty()) {
            if (previousParams) {
                sb.append("_");
            }
            sb.append(getDevStageIds().stream().sorted().collect(Collectors.joining("_"))
                    .replaceAll(" ", "_").replaceAll(":", "_"));
            previousParams = true;
        }
        if (!getCellTypeIds().isEmpty()) {
            if (previousParams) {
                sb.append("_");
            }
            sb.append(getCellTypeIds().stream().sorted().collect(Collectors.joining("_"))
                    .replaceAll(" ", "_").replaceAll(":", "_"));
            previousParams = true;
        }
        if (!getSexIds().isEmpty()) {
            if (previousParams) {
                sb.append("_");
            }
            sb.append(getSexIds().stream().sorted().collect(Collectors.joining("_"))
                    .replaceAll(" ", "_").replaceAll(":", "_"));
            previousParams = true;
        }
        if (!getStrainIds().isEmpty()) {
            if (previousParams) {
                sb.append("_");
            }
            sb.append(getStrainIds().stream().sorted().collect(Collectors.joining("_"))
                    .replaceAll(" ", "_").replaceAll(":", "_"));
            previousParams = true;
        }
        return log.traceExit(sb.toString());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConditionFilter [anatEntityIds=").append(getAnatEntityIds())
               .append(", devStageIds=").append(getDevStageIds())
               .append(", cellTypeIds=").append(getCellTypeIds())
               .append(", sexIds=").append(getSexIds())
               .append(", strainIds=").append(getStrainIds())
               .append(", observedCondForParams=").append(observedCondForParams).append("]");
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

        // Check Sex ID
        if (condition.getSex() != null 
            && this.getSexIds() != null && !this.getSexIds().isEmpty()
            && !this.getSexIds().contains(condition.getSex().getId())) {
            log.debug("Sex {} not validated: not in {}",
                condition.getSex().getId(), this.getSexIds());
            return log.traceExit(false);
        }
        // Check Strain ID 
        if (condition.getStrain() != null 
            && this.getStrainIds() != null && !this.getStrainIds().isEmpty()
            && !this.getStrainIds().contains(condition.getStrain().getId())) {
            log.debug("Strain {} not validated: not in {}",
                condition.getStrain().getId(), this.getStrainIds());
            return log.traceExit(false);
        }
        
        return log.traceExit(true);
    }
}