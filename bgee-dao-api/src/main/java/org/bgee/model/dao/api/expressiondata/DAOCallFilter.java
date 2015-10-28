package org.bgee.model.dao.api.expressiondata;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;

/**
 * A filter to parameterize expression data queries. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 *
 * @param T     The type of {@code CallTO} used to filter the expression query.
 */
public class DAOCallFilter<T extends CallTO> {
    private final static Logger log = LogManager.getLogger(DAOCallFilter.class.getName());

    /**
     * @see #getGeneIds()
     */
    private final Set<String> geneIds;
    /**
     * @see #getSpeciesIds()
     */
    private final Set<String> speciesIds;
    /**
     * @see #getConditionFilters()
     */
    private final Set<DAOConditionFilter> conditionFilters;
    /**
     * @see #getCallTOFilters()
     */
    private final Set<T> callTOFilters;
    
    /**
     * Constructor accepting all requested parameters. In the provided {@code T}s, 
     * only the following methods are considered (if available for this type of {@code T}): 
     * <ul>
     * <li>{@code getAffymetrixData}, {@code getESTData}, {@code getInSituData}, {@code getRNASeqData}, 
     * and {@code getRelaxedInSituData}, to define the minimum quality level for each data type. 
     * if equal to {@code null} or {@code DataState.NODATA}, then no filtering is performed 
     * based on this data type. Also, if all these methods return {@code DataState.LOWQUALITY} 
     * in an {@code ExpressionCallTO}, then no filtering on any data type is performed 
     * for this {@code ExpressionCallTO}. 
     * <li>{@code isIncludeSubstructures}, {@code isIncludeSubStages}, and {@code isIncludeParentStructures}, 
     * to define whether calls should be propagated. 
     * <li>{@code getAnatOriginOfLine}, {@code getStageOriginOfLine}, and {@code getOriginOfLine}, 
     * to define whether the returned calls should be filtered based on the origin of the propagated data.
     * Must be {@code null} to accept all origins. 
     * <li>{@code isObservedData}, to define a criteria of having at least one observation 
     * of the returned calls (no propagated calls only). Must be {@code null} to accept 
     * all observed data states.
     * <li>{@code getComparisonFactor}, {@code getDiffExprCallTypeAffymetrix}, 
     * {@code getDiffExprCallTypeRNASeq}, {@code getBestPValueAffymetrix}, {@code getBestPValueRNASeq}, 
     * to define parameters related to differential expression analyses. The p-values define 
     * the minimum p-values requested. 
     * </ul>
     * <p>
     * Note that all the provided {@code T}s should have the same propagation states 
     * (methods {@code isIncludeSubstructures}, {@code isIncludeSubStages}, and 
     * {@code isIncludeParentStructures}, when available), otherwise, 
     * an {@code IllegalArgumentException} is thrown.
     * 
     * @param geneIds           A {@code Set} of {@code String}s that are IDs of genes 
     *                          to filter expression queries. Can be {@code null} or empty.
     * @param speciesIds        A {@code Set} of {@code String}s that are IDs of species 
     *                          to filter expression queries. Can be {@code null} or empty.
     * @param conditionFilters  A {@code Set} of {@code ConditionFilter}s to configure 
     *                          the filtering of conditions with expression data. If several 
     *                          {@code ConditionFilter}s are provided, they are seen as "OR" conditions.
     * @param callTOFilters     A {@code Set} of {@code T}s allowing to configure the minimum 
     *                          quality level for each data type, the call propagation method, 
     *                          the call types produced from each data type... If several 
     *                          {@code T}s are provided, they are seen as "OR" conditions.
     *                          
     * @throws IllegalArgumentException If some {@code T}s have different propagation states.
     */
    public DAOCallFilter(Set<String> geneIds, Set<String> speciesIds, 
            Set<DAOConditionFilter> conditionFilters, Set<T> callTOFilters) throws IllegalArgumentException {
        log.entry(geneIds, speciesIds, conditionFilters, callTOFilters);
        
        //sanity check for propagation states
        if (callTOFilters != null && callTOFilters.stream()
            //We map each CallTO to a String representing its propagation states. 
            //We should have only one equal String at the end.
            .map(e -> {
                if (e instanceof ExpressionCallTO) {
                    ExpressionCallTO exprTO = (ExpressionCallTO) e;
                    return (exprTO.isIncludeSubstructures() == null? false: exprTO.isIncludeSubstructures())
                    + "-" + (exprTO.isIncludeSubStages() == null? false: exprTO.isIncludeSubStages())
                    + "-" + (exprTO.getAnatOriginOfLine() == null? 
                            ExpressionCallTO.OriginOfLine.SELF: exprTO.getAnatOriginOfLine())
                    + "-" + (exprTO.getStageOriginOfLine() == null? 
                            ExpressionCallTO.OriginOfLine.SELF: exprTO.getStageOriginOfLine());
                } else if (e instanceof NoExpressionCallTO) {
                    return "" + ((NoExpressionCallTO) e).isIncludeParentStructures();
                } else if (e instanceof DiffExpressionCallTO) {
                    //no propagation for diff. expression calls
                    return "";
                } else {
                    throw log.throwing(new IllegalArgumentException("Unsupported CallTO"));
                }
            })
            .collect(Collectors.toSet()).size() > 1) {
            
            throw log.throwing(new IllegalArgumentException("It is not possible to mix "
                    + "different propagation states or different CallTO types."));
        }
        
        this.geneIds = geneIds == null? null: Collections.unmodifiableSet(new HashSet<>(geneIds));
        this.speciesIds = speciesIds == null? null: Collections.unmodifiableSet(new HashSet<>(speciesIds));
        this.conditionFilters = conditionFilters == null? null: Collections.unmodifiableSet(
                new HashSet<>(conditionFilters));
        this.callTOFilters = callTOFilters == null? null: Collections.unmodifiableSet(
                new HashSet<>(callTOFilters));
        
        log.exit();
    }

    /**
     * @return  The {@code Set} of {@code String}s that are IDs of genes used
     *          to filter expression queries. Can be {@code null} or empty.
     */
    public Set<String> getGeneIds() {
        return geneIds;
    }
    /**
     * @return  The {@code Set} of {@code String}s that are IDs of species used
     *          to filter expression queries. Can be {@code null} or empty.
     */
    public Set<String> getSpeciesIds() {
        return speciesIds;
    }
    /**
     * @return  A {@code Set} of {@code ConditionFilter}s to configure the filtering 
     *          of conditions with expression data. If several {@code ConditionFilter}s are provided, 
     *          they are seen as "OR" conditions. Can be {@code null} or empty.
     */
    public Set<DAOConditionFilter> getConditionFilters() {
        return conditionFilters;
    }
    /**
     * @return  The {@code Set} of {@code T}s allowing to configure the minimum 
     *          quality level for each data type, the call propagation method, 
     *          the call types produced from each data type... If several 
     *          {@code T}s are provided, they are seen as "OR" conditions.
     *          Only the following methods are considered (if available 
     *          for this type of {@code T}): {@code getAffymetrixData}, 
     *          {@code getESTData}, {@code getInSituData}, {@code getRNASeqData}, 
     *          {@code getRelaxedInSituData}, {@code isIncludeSubstructures}, 
     *          {@code isIncludeSubStages}, {@code isIncludeParentStructures}, 
     *          {@code getComparisonFactor}, {@code getDiffExprCallTypeAffymetrix}, 
     *          {@code getBestPValueAffymetrix}, {@code getDiffExprCallTypeRNASeq}, 
     *          {@code getBestPValueRNASeq}.
     */
    public Set<T> getCallTOFilters() {
        return callTOFilters;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callTOFilters == null) ? 0 : callTOFilters.hashCode());
        result = prime * result + ((conditionFilters == null) ? 0 : conditionFilters.hashCode());
        result = prime * result + ((geneIds == null) ? 0 : geneIds.hashCode());
        result = prime * result + ((speciesIds == null) ? 0 : speciesIds.hashCode());
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
        DAOCallFilter<?> other = (DAOCallFilter<?>) obj;
        if (callTOFilters == null) {
            if (other.callTOFilters != null) {
                return false;
            }
        } else if (!callTOFilters.equals(other.callTOFilters)) {
            return false;
        }
        if (conditionFilters == null) {
            if (other.conditionFilters != null) {
                return false;
            }
        } else if (!conditionFilters.equals(other.conditionFilters)) {
            return false;
        }
        if (geneIds == null) {
            if (other.geneIds != null) {
                return false;
            }
        } else if (!geneIds.equals(other.geneIds)) {
            return false;
        }
        if (speciesIds == null) {
            if (other.speciesIds != null) {
                return false;
            }
        } else if (!speciesIds.equals(other.speciesIds)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DAOCallFilter [geneIds=" + geneIds 
                + ", speciesIds=" + speciesIds 
                + ", conditionFilters=" + conditionFilters 
                + ", callTOFilters=" + callTOFilters + "]";
    }
}
