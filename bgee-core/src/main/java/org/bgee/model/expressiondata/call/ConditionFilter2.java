package org.bgee.model.expressiondata.call;

import java.util.Collection;

import org.bgee.model.expressiondata.baseelements.ConditionParameter;

public class ConditionFilter2 extends ConditionFilterCondParam<ConditionParameter> {

    /**
     * @param speciesIds            A {@code Collection} of {@code Integer}s that are the IDs
     *                              of the species that this {@code ConditionFilter}
     *                              will specify to use.
     * @param anatEntityIds         A {@code Collection} of {@code String}s that are the IDs
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
     * @param observedCondForParams A {@code Collection} of
     *                              {@code ExpressionCallService.ConditionParameter}s specifying
     *                              that the conditions considered should have been observed
     *                              in data annotations (not created only from propagation),
     *                              using the specified condition parameters to perform the check.
     *                              For instance, if this {@code Collection} contains only the parameter
     *                              {@code ConditionParameter.ANAT_ENTITY}, any condition
     *                              using an anat. entity used in an annotation will be valid
     *                              (but of course, the other attributes of this {@code ConditionFilter}
     *                              will also be considered). If {@code null}
     *                              or empty, no filtering will be performed on whether
     *                              the global conditions considered have been observed in annotations.
     * @throws IllegalArgumentException 
     */
    //XXX: Should we add two booleans to ask for considering sub-structures and sub-stages?
    //Because it seems it can be managed through query of data propagation in CallFilter
    //XXX: should we accept Sex as arguments rather than Strings for sexes?
    protected ConditionFilter2(Collection<Integer> speciesIds, Collection<String> anatEntityIds,
            Collection<String> devStageIds, Collection<String> cellTypeIds, Collection<String> sexIds,
            Collection<String> strainIds, Collection<ConditionParameter> observedCondForParams)
                    throws IllegalArgumentException {
        super(speciesIds, anatEntityIds, devStageIds, cellTypeIds, sexIds, strainIds,
                observedCondForParams, ConditionParameter.class);
    }
}
