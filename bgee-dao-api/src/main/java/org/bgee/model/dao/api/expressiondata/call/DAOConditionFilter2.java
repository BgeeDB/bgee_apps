package org.bgee.model.dao.api.expressiondata.call;

import java.util.Collection;

public class DAOConditionFilter2 extends DAOConditionFilterBase<ConditionDAO.ConditionParameter> {
    
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
    public DAOConditionFilter2(Collection<String> anatEntitieIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String> sexIds, Collection<String> strainIds,
            Collection<ConditionDAO.ConditionParameter> observedCondForParams) throws IllegalArgumentException {
        super(anatEntitieIds, devStageIds, cellTypeIds, sexIds, strainIds, observedCondForParams,
                ConditionDAO.ConditionParameter.class);
    }

}
