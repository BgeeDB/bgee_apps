package org.bgee.model.dao.api.expressiondata.call;

import java.util.Collection;

/**
 * A filter to parameterize queries using expression data conditions. 
 * 
 * @author Frederic Bastian
 * @version Bgee 15.0, May 2021
 * @since Bgee 13 Oct. 2015
 */
public class DAOConditionFilter extends DAOConditionFilterBase<ConditionDAO.Attribute> {
    
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
        super(anatEntitieIds, devStageIds, cellTypeIds, sexIds, strainIds, observedCondForParams,
                ConditionDAO.Attribute.class);
        if (this.getObservedCondForParams().stream().anyMatch(a -> !a.isConditionParameter())) {
            throw new IllegalArgumentException(
                    "A ConditionDAO.Attribute that is not a condition parameter was provided");
        }
    }
}
