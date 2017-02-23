package org.bgee.model.dao.mysql.expressiondata;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;

/**
 * This {@code enum} allows to retrieve table and field names corresponding to specific 
 * combination of condition parameters (for more information, see, for instance, 
 * {@link ConditionDAO.Attribute#isConditionParameter()}).
 * <p>
 * The main method to retrieve an appropriate {@code CondParamCombination} 
 * is {@link CondParamCombination#getCombination(Collection)}.
 * 
 * @author Frederic Bastian
 * @version Bgee 14 Feb. 2017
 * @see ConditionDAO
 * @see RawExpressionCallDAO
 * @since Bgee 14 Feb. 2017
 */
public enum CondParamCombination {    
    ANAT(EnumSet.of(ConditionDAO.Attribute.ANAT_ENTITY_ID), false, 
            "anatEntityCond", "anatEntityConditionId", 
            "anatEntityExpression", "anatEntityExpressionId", 
            "globalAnatEntityExpression", "globalAnatEntityExpressionId", 
            "anatEntityGlobalExpressionToExpression"), 
    ANAT_STAGE(EnumSet.of(ConditionDAO.Attribute.ANAT_ENTITY_ID, ConditionDAO.Attribute.STAGE_ID), false, 
            "anatEntityStageCond", "anatEntityStageConditionId", 
            "anatEntityStageExpression", "anatEntityStageExpressionId", 
            "globalAnatEntityStageExpression", "globalAnatEntityStageExpressionId", 
            "anatEntityStageGlobalExpressionToExpression")
//    , ANAT_SEX, ANAT_STRAIN, ANAT_STAGE_SEX, ANAT_STAGE_STRAIN, ANAT_SEX_STRAIN, 
//    ANAT_STAGE_SEX_STRAIN
    ;
    
    private static final Logger log = LogManager.getLogger(CondParamCombination.class.getName());

    /**
     * Retrieve the {@code CondParamCombination} corresponding to the provided combination of 
     * {ConditionDAO.Attribute}.
     * 
     * @param conditionParameters   A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                              combination of condition parameters that were requested for queries, 
     *                              allowing to determine which condition and expression tables to target
     *                              (see {Attribute#isConditionParameter()}).
     * @return                      The {@code CondParamCombination} corresponding to {@code conditionParameters}.
     * @throws IllegalArgumentException If {@code conditionParameters} contains {@code Attribute}s 
     *                                  that are not condition parameters (see {@link 
     *                                  ConditionDAO.Attribute#isConditionParameter()}), 
     *                                  or if no corresponding {@code CondParamCombination} 
     *                                  could be found.
     */
    public static CondParamCombination getCombination(Collection<ConditionDAO.Attribute> conditionParameters) {
        log.entry(conditionParameters);
        
        Set<ConditionDAO.Attribute> paramSet = EnumSet.copyOf(conditionParameters);
        if (paramSet.stream().anyMatch(a -> !a.isConditionParameter())) {
            throw log.throwing(new IllegalArgumentException("Only condition parameters should be provided."));
        }
        
        Set<CondParamCombination> matchingComb = EnumSet.allOf(CondParamCombination.class)
                .stream().filter(comb -> comb.getParameters().equals(paramSet)).collect(Collectors.toSet());
        if (matchingComb.size() != 1) {
            throw log.throwing(new IllegalArgumentException(
                    "No condition parameter combination could be found for " + conditionParameters));
        }
        return log.exit(matchingComb.iterator().next());
    }
    /**
     * @return  The {@code CondParamCombination} taking into account all possible condition parameters.
     */
    public static CondParamCombination getAllParamCombination() {
        log.entry();
        Set<CondParamCombination> matchingComb = EnumSet.allOf(CondParamCombination.class)
                .stream().filter(comb -> comb.isAllParamCombination()).collect(Collectors.toSet());
        if (matchingComb.size() != 1) {
            throw log.throwing(new IllegalStateException("No \"all parameter\" combination defined"));
        }
        return log.exit(matchingComb.iterator().next());
    }

    
    private final Set<ConditionDAO.Attribute> parameters;
    private final boolean allParamCombination;
    
    private final String condIdField;
    private final String condTable;
    private final String rawExprIdField;
    private final String rawExprTable;
    private final String globalExprIdField;
    private final String globalExprTable;
    private final String globalToRawExprTable;

    private CondParamCombination(Set<ConditionDAO.Attribute> parameters, boolean allParamCombination, 
            String condTable, String condIdField, 
            String rawExprTable, String rawExprIdField, 
            String globalExprTable, String globalExprIdField, String globalToRawExprTable) {
        this.parameters = parameters;
        this.allParamCombination = allParamCombination;
        this.condIdField = condIdField;
        this.condTable = condTable;
        this.rawExprIdField = rawExprIdField;
        this.rawExprTable = rawExprTable;
        this.globalExprIdField = globalExprIdField;
        this.globalExprTable = globalExprTable;
        this.globalToRawExprTable = globalToRawExprTable;
    }


    /**
     * @return A {@code Set} of {@code ConditionDAO.Attribute}s that are the condition parameters 
     *          corresponding to this {@code CondParamCombination}.
     */
    public Set<ConditionDAO.Attribute> getParameters() {
        return parameters;
    }
    /**
     * @return  A {@code String} that is the name of the condition table 
     *          for this {@code CondParamCombination}.
     */
    public String getCondTable() {
        return condTable;
    }
    /**
     * @return  A {@code String} that is the name of the condition ID field 
     *          for this {@code CondParamCombination}.
     */
    public String getCondIdField() {
        return condIdField;
    }
    /**
     * @return  A {@code String} that is the name of the raw expression table
     *          for this {@code CondParamCombination}.
     */
    public String getRawExprTable() {
        return rawExprTable;
    }
    /**
     * @return  A {@code String} that is the name of the raw expression ID field 
     *          for this {@code CondParamCombination}.
     */
    public String getRawExprIdField() {
        return rawExprIdField;
    }
    /**
     * @return  A {@code String} that is the name of the global expression table
     *          for this {@code CondParamCombination}.
     */
    public String getGlobalExprTable() {
        return globalExprTable;
    }
    /**
     * @return  A {@code String} that is the name of the global expression ID field 
     *          for this {@code CondParamCombination}.
     */
    public String getGlobalExprIdField() {
        return globalExprIdField;
    }
    /**
     * @return  A {@code String} that is the name of the link table between global and raw expression IDs  
     *          for this {@code CondParamCombination}.
     */
    public String getGlobalToRawExprTable() {
        return globalToRawExprTable;
    }
    /**
     * @return  A {@code boolean} that is {@code true} if this {@code CondParamCombination} 
     *          takes into account all possible condition parameters.
     */
    public boolean isAllParamCombination() {
        return allParamCombination;
    }
}
