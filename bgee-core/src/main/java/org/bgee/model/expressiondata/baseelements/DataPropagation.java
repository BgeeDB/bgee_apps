package org.bgee.model.expressiondata.baseelements;

import java.util.EnumSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Defines the source of expression data of a {@code CallData} or {@code Call}, along 
 * the ontologies used to capture conditions. For instance, the expression of a gene 
 * in a given anatomical entity could have been observed in the anatomical entity itself, 
 * or only in some substructures of the entity, or in both. Similarly, expression in a given 
 * developmental stage could have been observed only in a sub-stage, or in the stage itself, 
 * etc. 
 * 
 * @author Frederic Bastian
 * @version Bgee 15 Mar. 2021
 * @since Bgee 13 Sep. 2015
 *
 */
//TODO: actually, if we really wanted to abstract away details about what elements 
//compose a condition, we should use an Enum describing the condition elements 
//(e.g., ANAT_ENTITY, DEV_STAGE, ...).
//The constructor could accept a Map ConditionElement -> PropagationState. And a sanity check 
//could be performed to ensure that all ConditionElement enum elements are in the key set of the Map.
//If we don't want to change the class signature, we could keep the getAnatEntityPropagationState etc 
//as helper methods.
public class DataPropagation {
    private final static Logger log = LogManager.getLogger(DataPropagation.class.getName());
    
    /**
     * @see #getAnatEntityPropagationState()
     */
    private final PropagationState anatEntityPropagationState;
    /**
     * @see #getDevStagePropagationState()
     */
    private final PropagationState devStagePropagationState;
    /**
     * @see #getCellTypePropagationState()
     */
    private final PropagationState cellTypePropagationState;
    /**
     * @see #getSexPropagationState()
     */
    private final PropagationState sexPropagationState;
    /**
     * @see #getStrainPropagationState()
     */
    private final PropagationState strainPropagationState;
    /**
     * @see #getIncludingObservedData()
     */
    private final Boolean includingObservedData;
    
    /**
     * Instantiate a new {@code DataPropagation} with a {@code PropagationState.SELF} state 
     * for all condition elements.
     * @see #DataPropagation(PropagationState, PropagationState)
     * @see #DataPropagation(PropagationState, PropagationState, Boolean)
     */
    public DataPropagation() {
        this(PropagationState.SELF, PropagationState.SELF);
    }
    /**
     * Instantiate a new {@code DataPropagation} by providing the propagation state along anatomy, 
     * and the propagation state along developmental stages. The observed data state 
     * is unknown.
     * 
     * @param anatEntityPropagationState    A {@code PropagationState} describing how data 
     *                                      are propagated along anatomy.
     * @param devStagePropagationState      A {@code PropagationState} describing how data 
     *                                      are propagated along dev. stages.
     * @see #DataPropagation(PropagationState, PropagationState, Boolean)
     */
    public DataPropagation(PropagationState anatEntityPropagationState, 
            PropagationState devStagePropagationState) throws IllegalArgumentException {
        this(anatEntityPropagationState, devStagePropagationState, null);
    }
    /**
     * Instantiate a new {@code DataPropagation} by providing the propagation state along anatomy,
     * the propagation state along developmental stages, and the observed data state.
     * If {@code includingObservedData} is {@code null}, it means that the observed data state
     * is unknown.
     *
     * @param anatEntityPropagationState    A {@code PropagationState} describing how data
     *                                      are propagated along anatomy.
     * @param devStagePropagationState      A {@code PropagationState} describing how data
     *                                      are propagated along dev. stages.
     * @param includingObservedData         A {@code Boolean} defining whether the data includes
     *                                      some that were observed in the condition itself,
     *                                      and not only in an ancestor or a descendant.
     *                                      If {@code null}, it means that this information is unknown
     *                                      (or not requested, if used as part of a {@code CallFilter}).
     * @throws IllegalArgumentException     If the provided {@code PropagationState}s are incompatible
     *                                      with {@code includingObservedData}.
     */
    //Note: it is allowed to provide only null arguments here, because see CallService.DATA_PROPAGATION_IDENTITY
    public DataPropagation(PropagationState anatEntityPropagationState,
            PropagationState devStagePropagationState, Boolean includingObservedData)
                    throws IllegalArgumentException {
        this(anatEntityPropagationState, devStagePropagationState, null, null, null, 
                includingObservedData);
    }
    /**
     * Instantiate a new {@code DataPropagation} by providing the propagation state along anatomy,
     * developmental stages, sex, strain, and the observed data state.
     * If {@code includingObservedData} is {@code null}, it means that the observed data state
     * is unknown.
     *
     * @param anatEntityPropagationState    A {@code PropagationState} describing how data
     *                                      are propagated along anatomy.
     * @param devStagePropagationState      A {@code PropagationState} describing how data
     *                                      are propagated along dev. stages.
     * @param cellTypePropagationState      A {@code PropagationState} describing how data
     *                                      are propagated along cell types.
     * @param sexPropagationState           A {@code PropagationState} describing how data
     *                                      are propagated along sexes.
     * @param strainPropagationState        A {@code PropagationState} describing how data
     *                                      are propagated along strains.
     * @param includingObservedData         A {@code Boolean} defining whether the data includes
     *                                      some that were observed in the condition itself,
     *                                      and not only in an ancestor or a descendant.
     *                                      If {@code null}, it means that this information is unknown
     *                                      (or not requested, if used as part of a {@code CallFilter}).
     * @throws IllegalArgumentException     If the provided {@code PropagationState}s are incompatible
     *                                      with {@code includingObservedData}.
     */
    //Note: it is allowed to provide only null arguments here, because see CallService.DATA_PROPAGATION_IDENTITY
    public DataPropagation(PropagationState anatEntityPropagationState,
            PropagationState devStagePropagationState, PropagationState cellTypePropagationState, 
            PropagationState sexPropagationState, PropagationState strainPropagationState, 
            Boolean includingObservedData) throws IllegalArgumentException {
        //Actually, we cannot infer the ObservedData state from looking at all individual
        //condition parameter propagation state: see comments inside method
        //org.bgee.model.expressiondata.CallService.mergeDataPropagations(DataPropagation, DataPropagation)
        //The only check we can make is the following:
        if (Boolean.TRUE.equals(includingObservedData) && EnumSet.of(
                anatEntityPropagationState == null? PropagationState.UNKNOWN: anatEntityPropagationState,
                devStagePropagationState == null? PropagationState.UNKNOWN: devStagePropagationState,
                cellTypePropagationState == null? PropagationState.UNKNOWN: cellTypePropagationState,
                sexPropagationState == null? PropagationState.UNKNOWN: sexPropagationState,
                strainPropagationState == null? PropagationState.UNKNOWN: strainPropagationState)
                .stream().anyMatch(s -> Boolean.FALSE.equals(s.isIncludingObservedData()))) {
            throw log.throwing(new IllegalArgumentException("The provided observed data state ("
                    + includingObservedData + ") is incompatible with the provided PropagationStates ("
                    + "anatomy: " + anatEntityPropagationState + " - stage: " + devStagePropagationState
                    + " - sex: " + sexPropagationState + " - strain: " + strainPropagationState));
        }

        this.anatEntityPropagationState = anatEntityPropagationState;
        this.devStagePropagationState = devStagePropagationState;
        this.cellTypePropagationState = cellTypePropagationState;
        this.sexPropagationState = sexPropagationState;
        this.strainPropagationState = strainPropagationState;
        this.includingObservedData  = includingObservedData;
    }
    
    /**
     * @return  The {@code PropagationState} describing how data are propagated along anatomy.
     */
    public PropagationState getAnatEntityPropagationState() {
        return anatEntityPropagationState;
    }
    /**
     * @return  The {@code PropagationState} describing how data are propagated along 
     *          developmental stages.
     */
    public PropagationState getDevStagePropagationState() {
        return devStagePropagationState;
    }
    /**
     * @return  The {@code PropagationState} describing how data are propagated along 
     *          cell types.
     */
    public PropagationState getCellTypePropagationState() {
        return cellTypePropagationState;
    }
    /**
     * @return  The {@code PropagationState} describing how data are propagated along
     *          sexes.
     */
    public PropagationState getSexPropagationState() {
        return sexPropagationState;
    }
    /**
     * @return  The {@code PropagationState} describing how data are propagated along
     *          strains.
     */
    public PropagationState getStrainPropagationState() {
        return strainPropagationState;
    }

    /**
     * Returns whether this {@code DataPropagation} is linked to data including observed data,
     * meaning, not from call propagation only.
     * <p>
     * <strong>Warning:</strong>
     * <ul>
     * <li>the value returned by this method is informative only when the associated data
     * were based on calls using <strong>all</strong> condition parameters.
     * <li>when only one condition parameter was requested, users can use the related
     * {@code PropagationState} (for instance, when retrieving calls by requesting only
     * {@code CallService.Attribute.ANAT_ENTITY_ID}, using the value returned by
     * {@code #getAnatEntityPropagationState()#isIncludingObservedData()}.
     * <li>when more than one, but not all, condition parameters are requested, for now there is no way
     * to determine if the call was observed or not. Prior to Bgee 15.0 it was possible,
     * because we were computing calls for all combinations of condition parameters.
     * But we don't do that anymore, as we use the roots of the respective ontologies
     * for condition parameters that we do not want to consider.
     * </ul>
     * <p>
     *
     * @apiNote this {@code isIncludingObservedData} is problematic when user requested
     *          more than 1 condition parameter, but not all, for instance,
     *          {@code CallService.Attribute.ANAT_ENTITY_ID} and {@code CallService.Attribute.DEV_STAGE_ID}:
     *          <ul>
     *          <li>when not all condition parameters were requested, this method will almost always
     *          returns {@code false}, because we will have calls with {@code Condition}s
     *          using the root of the ontologies for all the condition parameters that were not requested.
     *          For instance, if we requested {@code CallService.Attribute.ANAT_ENTITY_ID} and
     *          {@code CallService.Attribute.DEV_STAGE_ID}, we could end up with a call
     *          in the following {@code Condition}:
     *          <pre>
     *          AnatEntity=brain, DevStage=embryo, CellType=cellular_component, Sex=any, Strain=wild-type
     *          </pre>
     *          => since the roots of the cell type, sex, and strain ontologies have been targeted,
     *          most likely we had no data annotated  in this condition, and it will be considered
     *          propagated. While actually we might have data annotated to:
     *          <pre>
     *          AnatEntity=brain and DevStage=embryo (with other cell type, sex, strain info).
     *          </pre>
     *          <li>So, what we want really, is to know whether we have data annotated to the condition
     *          considering only the requested condition parameters. In our example, it means
     *          considering only {@code anatEntityPropagationState} and {@code devStagePropagationState}.
     *          But actually, even if both {@code anatEntityPropagationState#isIncludingObservedData()}
     *          and {@code devStagePropagationState#isIncludingObservedData()} returns {@code true},
     *          we cannot conclude that {@code isIncludingObservedData()} for the call is {@code true}.
     *          It is because we could have for instance one experiment providing data in the organ itself,
     *          but not in the stage itself (e.g., {@code AnatEntity=brain and DevStage=gastrula});
     *          and another experiment providing data in the stage itself, but not in the organ itself
     *          (e.g., {@code AnatEntity=hypothalamus and DevStage=embryo}).
     *          => as a result, both {@code anatEntityPropagationState.isIncludingObservedData()} and
     *          {@code devStagePropagationState.isIncludingObservedData()} would return {@code true},
     *          but we would never have observed the call in {@code AnatEntity=brain and DevStage=embryo}.
     * @return  {@code true} if the related data included observed data, {@code false} otherwise,
     *          {@code null} if this cannot be determined or the information was not requested.
     */
    public Boolean isIncludingObservedData() {
        return includingObservedData;
    }
    
    /**
     * @return  A {@code Set} of {@code PropagationState}s that are all non-null states 
     *          associated to any condition parameter. 
     */
    //this method is useful to abstract away what are the elements defining a condition.
    public EnumSet<PropagationState> getAllPropagationStates() {
        return Stream.of(anatEntityPropagationState, devStagePropagationState,
                cellTypePropagationState, sexPropagationState, strainPropagationState)
                //XXX: maybe we should return null instead?
                .map(s -> s == null? PropagationState.UNKNOWN: s)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(PropagationState.class)));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatEntityPropagationState == null) ? 0 : anatEntityPropagationState.hashCode());
        result = prime * result + ((devStagePropagationState == null) ? 0 : devStagePropagationState.hashCode());
        result = prime * result + ((cellTypePropagationState == null) ? 0 : cellTypePropagationState.hashCode());
        result = prime * result + ((sexPropagationState == null) ? 0 : sexPropagationState.hashCode());
        result = prime * result + ((strainPropagationState == null) ? 0 : strainPropagationState.hashCode());
        result = prime * result + ((includingObservedData == null) ? 0 : includingObservedData.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DataPropagation other = (DataPropagation) obj;
        if (anatEntityPropagationState != other.anatEntityPropagationState) {
            return false;
        }
        if (devStagePropagationState != other.devStagePropagationState) {
            return false;
        }
        if (cellTypePropagationState != other.cellTypePropagationState) {
            return false;
        }
        if (sexPropagationState != other.sexPropagationState) {
            return false;
        }
        if (strainPropagationState != other.strainPropagationState) {
            return false;
        }
        if (includingObservedData == null) {
            if (other.includingObservedData != null) {
                return false;
            }
        } else if (!includingObservedData.equals(other.includingObservedData)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DataPropagation [anatEntityPropagationState=").append(anatEntityPropagationState)
                .append(", devStagePropagationState=").append(devStagePropagationState)
                .append(", cellTypePropagationState=").append(cellTypePropagationState)
                .append(", sexPropagationState=").append(sexPropagationState)
                .append(", strainPropagationState=").append(strainPropagationState)
                .append(", includingObservedData=").append(includingObservedData).append("]");
        return builder.toString();
    }
}