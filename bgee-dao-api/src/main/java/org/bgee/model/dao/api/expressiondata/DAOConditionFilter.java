package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A filter to parameterize queries using expression data conditions. 
 * 
 * @author Frederic Bastian
 * @version Bgee 15.0, May 2021
 * @since Bgee 13 Oct. 2015
 */
public class DAOConditionFilter extends DAOBaseConditionFilter {
    private final static Logger log = LogManager.getLogger(DAOConditionFilter.class.getName());

    /**
     * @see #getObservedCondForParams()
     */
    private final EnumSet<ConditionDAO.Attribute> observedCondForParams;
    
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
     * @param observedCondForParams A {@code Collection} of {@code ConditionDAO.Attribute}s specifying
     *                              that the conditions considered should have been observed
     *                              in data annotations (not created only from propagation),
     *                              using the specified condition parameters to perform the check.
     *                              For instance, if this {@code Collection} contains only the parameter
     *                              {@code ConditionDAO.Attribute.ANAT_ENTITY_ID}, any condition
     *                              using an anat. entity used in an annotation will be valid
     *                              (but of course, the other attributes of this {@code DAOConditionFilter}
     *                              will also be considered). If this {@code Collection} contains
     *                              a {@code ConditionDAO.Attribute} that is not a condition parameter,
     *                              (see {@link ConditionDAO.Attribute#isConditionParameter()}),
     *                              an {@code IllegalArgumentException} is thrown. If {@code null}
     *                              or empty, no filtering will be performed on whether
     *                              the global conditions considered have been observed in annotations.
     * @throws IllegalArgumentException If no anatomical entity IDs and no developmental stage IDs 
     *                                  are provided. 
     */
    public DAOConditionFilter(Collection<String> anatEntitieIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String> sexIds, Collection<String> strainIds,
            Collection<ConditionDAO.Attribute> observedCondForParams) throws IllegalArgumentException {
        super(anatEntitieIds, devStageIds, cellTypeIds, sexIds, strainIds);
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
                EnumSet.noneOf(ConditionDAO.Attribute.class): EnumSet.copyOf(observedCondForParams);
        if (this.observedCondForParams.stream().anyMatch(a -> !a.isConditionParameter())) {
            throw log.throwing(new IllegalArgumentException(
                    "A ConditionDAO.Attribute that is not a condition parameter was provided"));
        }
    }

    /**
     * @return  An {@code EnumSet} of {@code ConditionDAO.Attribute}s specifying
     *          that the conditions considered should have been observed in data annotations
     *          (not created only from propagation), using the specified condition parameters
     *          to perform the check. For instance, if this {@code EnumSet} contains only
     *          the parameter {@code ConditionDAO.Attribute.ANAT_ENTITY_ID}, any condition
     *          using an anat. entity used in an annotation will be valid (but of course,
     *          the other attributes of this {@code DAOConditionFilter} will also be considered).
     *          If empty, no filtering will be performed on whether
     *          the global conditions considered have been observed in annotations.
     */
    public EnumSet<ConditionDAO.Attribute> getObservedCondForParams() {
        return observedCondForParams;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((observedCondForParams == null) ? 0 : observedCondForParams.hashCode());
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
        DAOConditionFilter other = (DAOConditionFilter) obj;
        if (observedCondForParams == null) {
            if (other.observedCondForParams != null) {
                return false;
            }
        } else if (!observedCondForParams.equals(other.observedCondForParams)) {
            return false;
        }
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
        builder.append("DAOConditionFilter [anatEntityIds=").append(getAnatEntityIds())
               .append(", devStageIds=").append(getDevStageIds())
               .append(", cellTypeIds=").append(getCellTypeIds())
               .append(", sexIds=").append(getSexIds())
               .append(", strainIds=").append(getStrainIds())
               .append(", observedCondForParams=").append(observedCondForParams).append("]");
        return builder.toString();
    }
}
