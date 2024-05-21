package org.bgee.model.dao.api.expressiondata.call;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAOBaseConditionFilter;

public class DAOConditionFilterBase<T extends Enum<T>> extends DAOBaseConditionFilter {
    private final static Logger log = LogManager.getLogger(DAOConditionFilterBase.class.getName());

    /**
     * @see #getObservedCondForParams()
     */
    private final EnumSet<T> observedCondForParams;
    
    /**
     * @param anatEntityIds        A {@code Collection} of {@code String}s that are the IDs 
     *                              of the anatomical entities that this {@code DAOConditionFilter} 
     *                              will specify to use.
     * @param devStageIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the developmental stages that this {@code DAOConditionFilter} 
     *                              will specify to use.
     * @param cellTypeIds           A {@code Collection} of {@code String}s that are the IDs 
     *                              of the cell types that this {@code DAOConditionFilter} 
     *                              will specify to use.
     * @param sexIds                A {@code Collection} of {@code String}s that are the IDs 
     *                              of the sexes that this {@code DAOConditionFilter} 
     *                              will specify to use.
     * @param strainIds             A {@code Collection} of {@code String}s that are the IDs 
     *                              of the strains that this {@code DAOConditionFilter} 
     *                              will specify to use.
     * @param observedCondForParams A {@code Collection} of condition parameters specifying
     *                              that the conditions considered should have been observed
     *                              in data annotations (not created only from propagation),
     *                              using the specified condition parameters to perform the check.
     *                              For instance, if this {@code Collection} contains only the parameter
     *                              {@code ANAT_ENTITY}, any condition
     *                              using an anat. entity used in an annotation will be valid
     *                              (but of course, the other attributes of this {@code DAOConditionFilter}
     *                              will also be considered). If {@code null}
     *                              or empty, no filtering will be performed on whether
     *                              the global conditions considered have been observed in annotations.
     * @param excludedAnatEntityCellTypeIds  A {@code Collection} of {@code String}s that are the IDs
     *                                      of the anatomical entities and cell types that this
     *                                      {@code DAOConditionFilter} will specify to discard.
     * @throws IllegalArgumentException If no anatomical entity IDs and no developmental stage IDs 
     *                                  are provided. 
     */
    public DAOConditionFilterBase(Collection<String> anatEntitieIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String> sexIds, Collection<String> strainIds,
            Collection<T> observedCondForParams, Class<T> condParamType,
            Collection<String> excludedAnatEntityCellTypeIds) throws IllegalArgumentException {
        super(anatEntitieIds, devStageIds, cellTypeIds, sexIds, strainIds, excludedAnatEntityCellTypeIds);
        if ((anatEntitieIds == null || anatEntitieIds.isEmpty()) && 
                (devStageIds == null || devStageIds.isEmpty()) &&
                (cellTypeIds == null || cellTypeIds.isEmpty()) &&
                (sexIds == null || sexIds.isEmpty()) &&
                (strainIds == null || strainIds.isEmpty()) &&
                (observedCondForParams == null || observedCondForParams.isEmpty())) {
            throw log.throwing(new IllegalArgumentException("Some anatatomical entity IDs, "
                    + "developmental stage IDs, cell type IDs, sex IDs, strain IDs or observed "
                    + "data status must be provided."));
        }
        this.observedCondForParams = observedCondForParams == null || observedCondForParams.isEmpty()?
                EnumSet.noneOf(condParamType): EnumSet.copyOf(observedCondForParams);
    }

    /**
     * @return  An {@code EnumSet} of condition parameters specifying
     *          that the conditions considered should have been observed in data annotations
     *          (not created only from propagation), using the specified condition parameters
     *          to perform the check. For instance, if this {@code EnumSet} contains only
     *          the parameter {@code ANAT_ENTITY}, any condition
     *          using an anat. entity used in an annotation will be valid (but of course,
     *          the other attributes of this {@code DAOConditionFilter} will also be considered).
     *          If empty, no filtering will be performed on whether
     *          the global conditions considered have been observed in annotations.
     */
    public EnumSet<T> getObservedCondForParams() {
        //Defensive copying, no unmodifiableEnumSet
        return EnumSet.copyOf(observedCondForParams);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(observedCondForParams);
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
        DAOConditionFilterBase<?> other = (DAOConditionFilterBase<?>) obj;
        return Objects.equals(observedCondForParams, other.observedCondForParams);
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
        builder.append("DAOConditionFilter [")
               .append("anatEntityIds=").append(getAnatEntityIds())
               .append(", excludedAnatEntityCellTypeIds=").append(getExcludedAnatEntityCellTypeIds())
               .append(", devStageIds=").append(getDevStageIds())
               .append(", cellTypeIds=").append(getCellTypeIds())
               .append(", sexIds=").append(getSexIds())
               .append(", strainIds=").append(getStrainIds())
               .append(", observedCondForParams=").append(observedCondForParams)
               .append("]");
        return builder.toString();
    }

}
