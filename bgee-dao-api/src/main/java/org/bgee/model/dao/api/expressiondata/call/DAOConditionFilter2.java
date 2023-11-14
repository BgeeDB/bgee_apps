package org.bgee.model.dao.api.expressiondata.call;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class DAOConditionFilter2 extends DAOConditionFilterBase<ConditionDAO.ConditionParameter> {

    private final Set<Integer> speciesIds;
    /**
     * @param speciesIds            A {@code Collection} of {@code Integer}s that are the IDs 
     *                              of the species targeted by this filter.
     * @param anatEntityIds         A {@code Collection} of {@code String}s that are the IDs 
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
     * @param observedCondForParams A {@code Collection} of {@code ConditionDAO.ConditionParameter}s specifying
     *                              that the conditions considered should have been observed
     *                              in data annotations (not created only from propagation),
     *                              using the specified condition parameters to perform the check.
     *                              For instance, if this {@code Collection} contains only the parameter
     *                              {@code ConditionDAO.Attribute.ANAT_ENTITY_ID}, any condition
     *                              using an anat. entity used in an annotation will be valid
     *                              (but of course, the other attributes of this {@code DAOConditionFilter}
     *                              will also be considered). If {@code null}
     *                              or empty, no filtering will be performed on whether
     *                              the global conditions considered have been observed in annotations.
     * @throws IllegalArgumentException If no anatomical entity IDs and no developmental stage IDs 
     *                                  are provided. 
     */
    public DAOConditionFilter2(Collection<Integer> speciesIds, Collection<String> anatEntitieIds,
            Collection<String> devStageIds, Collection<String> cellTypeIds,
            Collection<String> sexIds, Collection<String> strainIds, 
            Collection<ConditionDAO.ConditionParameter> observedCondForParams)
                    throws IllegalArgumentException {
        super(anatEntitieIds, devStageIds, cellTypeIds, sexIds, strainIds, observedCondForParams,
                ConditionDAO.ConditionParameter.class);
        if (speciesIds != null && speciesIds.stream().anyMatch(id -> id == null || id < 1)) {
            throw new IllegalArgumentException("No speciesId can be null or less than 1");
        }
        //Set.of and Set.copyOf already returns immutable Sets
        this.speciesIds = speciesIds == null? Set.of(): Set.copyOf(speciesIds);
    }

    public Set<Integer> getSpeciesIds() {
        return speciesIds;
    }

    public boolean areAllFiltersExceptSpeciesEmpty() {
        return this.getAnatEntityIds().isEmpty() &&
                this.getCellTypeIds().isEmpty() &&
                this.getDevStageIds().isEmpty() &&
                this.getSexIds().isEmpty() &&
                this.getStrainIds().isEmpty() &&
                this.getObservedCondForParams().isEmpty();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(speciesIds);
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
        DAOConditionFilter2 other = (DAOConditionFilter2) obj;
        return Objects.equals(speciesIds, other.speciesIds);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DAOConditionFilter [")
               .append("speciesIds=").append(getSpeciesIds())
               .append(", anatEntityIds=").append(getAnatEntityIds())
               .append(", devStageIds=").append(getDevStageIds())
               .append(", cellTypeIds=").append(getCellTypeIds())
               .append(", sexIds=").append(getSexIds())
               .append(", strainIds=").append(getStrainIds())
               .append(", observedCondForParams=").append(this.getObservedCondForParams())
               .append("]");
        return builder.toString();
    }
}
