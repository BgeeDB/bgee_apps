package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
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
 * @param U     The type of {@code CallTO} used to filter the expression query.
 * @param T     The type of {@code CallDAO.Attribute} associated to the {@code CallTO} type {@code T}.
 */
public abstract class CallDAOFilter<T extends Enum<T> & CallDAO.Attribute, U extends CallTO<T>> {
    private final static Logger log = LogManager.getLogger(CallDAOFilter.class.getName());
    
    /**
     * A {@code CallDAOFilter} for {@code ExpressionCallDAO}s.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Oct. 2015
     * @since Bgee 13 Oct. 2015
     */
    //inner static class created simply for easier typing
    public static class ExpressionCallDAOFilter extends CallDAOFilter<ExpressionCallDAO.Attribute, ExpressionCallTO> {

        /**
         * See {@link CallDAOFilter#CallDAOFilter(Collection, Collection, Collection, Collection, Class)}
         */
        public ExpressionCallDAOFilter(Collection<String> geneIds, Collection<String> speciesIds,
                Collection<DAOConditionFilter> conditionFilters, 
                Collection<ExpressionCallTO> callTOFilters) throws IllegalArgumentException {
            super(geneIds, speciesIds, conditionFilters, callTOFilters, ExpressionCallDAO.Attribute.class);
        }
    }
    /**
     * A {@code CallDAOFilter} for {@code NoExpressionCallDAO}s.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Oct. 2015
     * @since Bgee 13 Oct. 2015
     */
    //inner static class created simply for easier typing
    public static class NoExpressionCallDAOFilter 
        extends CallDAOFilter<NoExpressionCallDAO.Attribute, NoExpressionCallTO> {
        /**
         * See {@link CallDAOFilter#CallDAOFilter(Collection, Collection, Collection, Collection, Class)}
         */
        public NoExpressionCallDAOFilter(Collection<String> geneIds, Collection<String> speciesIds,
                Collection<DAOConditionFilter> conditionFilters, 
                Collection<NoExpressionCallTO> callTOFilters) throws IllegalArgumentException {
            super(geneIds, speciesIds, conditionFilters, callTOFilters, NoExpressionCallDAO.Attribute.class);
        }
    }
    /**
     * A {@code CallDAOFilter} for {@code DiffExpressionCallDAO}s.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Oct. 2015
     * @since Bgee 13 Oct. 2015
     */
    //inner static class created simply for easier typing
    public static class DiffExpressionCallDAOFilter 
        extends CallDAOFilter<DiffExpressionCallDAO.Attribute, DiffExpressionCallTO> {
        /**
         * See {@link CallDAOFilter#CallDAOFilter(Collection, Collection, Collection, Collection, Class)}
         */
        public DiffExpressionCallDAOFilter(Collection<String> geneIds, Collection<String> speciesIds,
                Collection<DAOConditionFilter> conditionFilters, 
                Collection<DiffExpressionCallTO> callTOFilters) throws IllegalArgumentException {
            super(geneIds, speciesIds, conditionFilters, callTOFilters, DiffExpressionCallDAO.Attribute.class);
        }
    }

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
    private final LinkedHashSet<DAOConditionFilter> conditionFilters;
    /**
     * @see #getCallTOFilters()
     */
    private final LinkedHashSet<U> callTOFilters;
    
    /**
     * The class type of the {@code Attribute}s of type {@code T}.
     */
    private final Class<T> attributeType;
    
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
     * @param geneIds           A {@code Collection} of {@code String}s that are IDs of genes 
     *                          to filter expression queries. Can be {@code null} or empty.
     * @param speciesIds        A {@code Collection} of {@code String}s that are IDs of species 
     *                          to filter expression queries. Can be {@code null} or empty.
     * @param conditionFilters  A {@code Collection} of {@code ConditionFilter}s to configure 
     *                          the filtering of conditions with expression data. If several 
     *                          {@code ConditionFilter}s are provided, they are seen as "OR" conditions.
     *                          Can be {@code null} or empty.
     * @param callTOFilters     A {@code Collection} of {@code T}s allowing to configure the minimum 
     *                          quality level for each data type, the call propagation method, 
     *                          the call types produced from each data type... If several 
     *                          {@code T}s are provided, they are seen as "OR" conditions.
     *                          Can be {@code null} or empty.
     * @param attributeType     The class type of the {@code Attribute}s of type {@code T}.
     * @throws IllegalArgumentException If some {@code T}s have different propagation states.
     */
    protected CallDAOFilter(Collection<String> geneIds, Collection<String> speciesIds, 
            Collection<DAOConditionFilter> conditionFilters, Collection<U> callTOFilters, 
            Class<T> attributeType) throws IllegalArgumentException {
        log.entry(geneIds, speciesIds, conditionFilters, callTOFilters, attributeType);
        
        if (attributeType == null) {
            throw log.throwing(new IllegalArgumentException("The Attribute class type must be provided."));
        }
        
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
                    NoExpressionCallTO noExprTO = (NoExpressionCallTO) e;
                    return "" + (noExprTO.isIncludeParentStructures() == null? false: 
                        noExprTO.isIncludeParentStructures());
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

        this.attributeType = attributeType;
        this.geneIds = Collections.unmodifiableSet(
                geneIds == null? new HashSet<>(): new HashSet<>(geneIds));
        this.speciesIds = Collections.unmodifiableSet(
                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));
        //we'll use defensive copying for those ones, no unmodifiableLinkedHashSet method
        this.conditionFilters = conditionFilters == null? new LinkedHashSet<>(): 
            new LinkedHashSet<>(conditionFilters);
        this.callTOFilters = callTOFilters == null? new LinkedHashSet<>(): 
            new LinkedHashSet<>(callTOFilters);
        
        log.exit();
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code String}s containing the IDs of genes used
     *          to filter expression queries. Can be {@code null} or empty.
     */
    public Set<String> getGeneIds() {
        return geneIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s containing the IDs of species used
     *          to filter expression queries. Can be {@code null} or empty.
     */
    public Set<String> getSpeciesIds() {
        return speciesIds;
    }
    /**
     * @return  A {@code LinkedHashSet} of {@code ConditionFilter}s to configure the filtering 
     *          of conditions with expression data. If several {@code ConditionFilter}s are provided, 
     *          they are seen as "OR" conditions. Can be {@code null} or empty. 
     *          Provided as a {@code LinkedHashSet} for convenience when building queries 
     *          using several methods.
     */
    public LinkedHashSet<DAOConditionFilter> getConditionFilters() {
        //defensive copying
        return new LinkedHashSet<>(conditionFilters);
    }
    /**
     * @return  A {@code LinkedHashSet} of {@code T}s allowing to configure the minimum 
     *          quality level for each data type, the call propagation method, 
     *          the call types produced from each data type... If several 
     *          {@code T}s are provided, they are seen as "OR" conditions.
     *          See {@code CallDAOFilter} constructor for more details.
     *          Provided as a {@code LinkedHashSet} for convenience when building queries 
     *          using several methods.
     */
    public LinkedHashSet<U> getCallTOFilters() {
        //defensive copying
        return new LinkedHashSet<>(callTOFilters);
    }
    
    /**
     * Retrieve from {@code CallTO} the data types with a filtering requested, 
     * allowing to parameterize queries to the data source. For instance, to only retrieve 
     * calls with an Affymetrix data state equal to {@code HIGHQUALITY}, or with some RNA-Seq data 
     * of any quality (minimal data state {@code LOWQUALITY}).
     * <p>
     * The data types are represented as {@code Attribute}s allowing to request a data type parameter 
     * (see {@link CallDAO.Attribute#isDataTypeAttribute()}). The {@code DataState}s 
     * associated to each data type are retrieved using {@link CallTO#extractDataTypesToDataStates()}. 
     * A check is then performed to ensure that the {@code CallTO} will actually result 
     * in a filtering of the data. For instance, if all data types are associated to 
     * a {@code DataState} {@code LOWQUALITY}, then it is equivalent to requesting no filtering at all, 
     * and the {@code Map} returned by this method will be empty. 
     *  
     * @param callTO    A {@code CallTO} to extract filtering data types from.
     * @return          A {@code Map} where keys are {@code Attribute}s associated to a data type, 
     *                  the associated value being a {@code DataState} to be used 
     *                  to parameterize queries to the data source (results should have 
     *                  a data state equal to or higher than this value for this data type).
     */
    public Map<T, DataState> extractFilteringDataTypes(U callTO) {
        log.entry(callTO);
        
        final Map<T, DataState> typesToStates = callTO.extractDataTypesToDataStates();
        
        Set<DataState> states = new HashSet<>(typesToStates.values());
        //if all data types are null or requested to DataState.LOWQUALITY 
        //or DataState.NODATA, or if we only have null and DataState.NODATA values, 
        //it is equivalent to having no filtering for data types...
        if ((states.size() == 1 && 
                (states.contains(null) || states.contains(DataState.NODATA) || 
                 states.contains(DataState.LOWQUALITY))) || 
            (states.size() == 2 && states.contains(null) && 
            states.contains(DataState.NODATA))) {
            
            return log.exit(new EnumMap<>(this.attributeType));
        }
        
        //otherwise, get the data types with a filtering requested
        return log.exit(typesToStates.entrySet().stream()
                .filter(entry -> entry.getValue() != null && 
                        entry.getValue() != DataState.NODATA)
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));
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
        CallDAOFilter<?, ?> other = (CallDAOFilter<?, ?>) obj;
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
        return "CallDAOFilter [geneIds=" + geneIds 
                + ", speciesIds=" + speciesIds 
                + ", conditionFilters=" + conditionFilters 
                + ", callTOFilters=" + callTOFilters + "]";
    }
}
