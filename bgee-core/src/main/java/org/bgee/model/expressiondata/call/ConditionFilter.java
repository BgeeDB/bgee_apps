package org.bgee.model.expressiondata.call;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.call.Condition.ConditionEntities;

/**
 * A filter to parameterize queries using expression data {@link Condition}s.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 15.0, Dec. 2022
 * @since   Bgee 13, Oct. 2015
 */
//TODO: be able to EXCLUDE anat. entities/stages. It would be convenient to discard
//non-informative anat. entities.
public class ConditionFilter extends ConditionFilterCondParam<CallService.Attribute> {
    private final static Logger log = LogManager.getLogger(ConditionFilter.class.getName());

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

    public ConditionFilter(Collection<String> anatEntityIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String> sexIds, Collection<String> strainIds,
            Collection<CallService.Attribute> observedCondForParams) throws IllegalArgumentException {
        this(null, anatEntityIds, devStageIds, cellTypeIds, sexIds, strainIds, observedCondForParams);
    }
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
    public ConditionFilter(Collection<Integer> speciesIds, Collection<String> anatEntityIds,
            Collection<String> devStageIds, Collection<String> cellTypeIds,
            Collection<String> sexIds, Collection<String> strainIds,
            Collection<CallService.Attribute> observedCondForParams) throws IllegalArgumentException {
        super(speciesIds, anatEntityIds, devStageIds, cellTypeIds, sexIds, strainIds,
                observedCondForParams, CallService.Attribute.class);
        if (!CallService.Attribute.getAllConditionParameters().containsAll(this.getObservedCondForParams())) {
            throw log.throwing(new IllegalArgumentException(
                    "A CallService.Attribute that is not a condition parameter was provided"));
        }
    }
}