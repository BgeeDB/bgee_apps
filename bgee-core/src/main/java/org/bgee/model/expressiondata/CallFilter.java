package org.bgee.model.expressiondata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.CallData.DiffExpressionCallData;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.gene.GeneFilter;

/**
 * A filter to parameterize queries to {@link CallService}. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 *
 * @param T The type of {@code CallData} to be used by this {@code CallFilter}. 
 *          Can be declared as {@code CallData}, to include a mixture of {@code CallData} subtypes, 
 *          or as a specific subtype, for instance, {@code ExpressionCallData}.
 */
//XXX: would several CallFilters represent AND or OR conditions.
//If OR conditions, we could provide a Set<Set<CallFilter>> to CallService methods, 
//to provide AND/OR conditions.
//IF AND conditions, then we cannot easily target different CallDatas for different ConditionFilters, e.g.: 
//((brain adult and affymetrixQual >= high) OR (liver adult and rnaSeqQual >= high) OR (heart adult over-expressed). 
//=> I don't think we'd often like to do such a query, most of the time we would target 
//the same CallDatas. If we really needed it, then we could still do it in several queries 
//(even if it is less optimized (only when it targets the same CallType)).
//=> let's consider several CallFilters as AND conditions for now, and let's see what happens in the future.  
//Note that even if they were OR conditions, they should be used in several queries, 
//as it is not possible from the DAO to make one query applying a different Set 
//of CallData filters to different Sets of GeneFilters, ConditionFilters, etc.
public class CallFilter<T extends CallData<?>> {
    private final static Logger log = LogManager.getLogger(CallFilter.class.getName());
    
    /**
     * A {@code CallFilter} for {@code ExpressionCall}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Nov. 2015
     * @since Bgee 13
     */
    public static class ExpressionCallFilter extends CallFilter<ExpressionCallData> {
        /**
         * Basic constructor allowing to provide one {@code ExpressionCallData} filter.
         * 
         * @param callDataFilter    A {@code ExpressionCallData} to configure the filtering 
         *                          based on the expression data generation (for instance, 
         *                          minimum quality level for each data type, or type of propagation allowed, 
         *                          e.g., propagation of expression calls from substructures).
         * @see #ExpressionCallFilter(GeneFilter, Set, DataPropagation, Set)
         */
        public ExpressionCallFilter(ExpressionCallData callDataFilter) {
            this(null, null, null, new HashSet<>(Arrays.asList(callDataFilter)));
        }
        /**
         * See {@link CallFilter#CallFilter(GeneFilter, Set, DataPropagation, Set)}.
         */
        public ExpressionCallFilter(GeneFilter geneFilter, Collection<ConditionFilter> conditionFilters, 
                DataPropagation dataPropagationFilter, Collection<ExpressionCallData> callDataFilters) 
                        throws IllegalArgumentException {
            super(geneFilter, conditionFilters, dataPropagationFilter, callDataFilters);
        }
    }
    
    /**
     * A {@code CallFilter} for {@code DiffExpressionCall}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Nov. 2015
     * @since Bgee 13
     */
    public static class DiffExpressionCallFilter extends CallFilter<DiffExpressionCallData> {
        /**
         * Basic constructor allowing to provide one {@code DiffExpressionCallData} filter.
         * 
         * @param callDataFilter    A {@code DiffExpressionCallData} to configure the filtering 
         *                          based on the expression data generation (for instance, 
         *                          minimum quality level for each data type).
         * @see #DiffExpressionCallFilter(GeneFilter, Set, Set)
         */
        public DiffExpressionCallFilter(DiffExpressionCallData callDataFilter) {
            this(null, null, new HashSet<>(Arrays.asList(callDataFilter)));
        }
        /**
         * See {@link CallFilter#CallFilter(GeneFilter, Set, Set)}.
         */
        public DiffExpressionCallFilter(GeneFilter geneFilter, Collection<ConditionFilter> conditionFilters, 
                Collection<DiffExpressionCallData> callDataFilters) 
                        throws IllegalArgumentException {
            super(geneFilter, conditionFilters, null, callDataFilters);
        }
    }
    
    /**
     * @see #getGeneFilter()
     */
    //XXX: The only problem with using directly ConditionFilters and CallDatas in this class, 
    //is that GeneFilters are costly to use in a query; using the class CallDataConditionFilter 
    //was allowing to have a same GeneFilter to target several conditions/call data combinations. 
    //Now, the same query would be doable by using several CallFilters, but with a same GeneFilter 
    //reused several times. This is costly, but we could have a mechanism to provide a global GeneFilter 
    //to the DAO when we see it is always the same GeneFilter used. 
    //I think it's worth it for the simplification it allows in the class CallFilter.
    private final GeneFilter geneFilter;
    
    /**
     * @see #getConditionFilters()
     */
    //XXX: all parameters are OR conditions
    private final Set<ConditionFilter> conditionFilters;

    /**
     * @see #getDataPropagationFilter()
     */
    private final DataPropagation dataPropagationFilter;
    
    /**
     * @see #getCallDataFilters()
     */
    //XXX: all CallData are OR conditions. The only type of query not easily doable is: 
    //affymetrixData = expressed high && rnaSeqData = expressed high. 
    //Note that they *must* remain OR conditions, because the DataPropagation 
    //is part of these CallData, and we need to do one query
    //XXX: again, where to accept the diffExpressionFactor
    
    private final Set<T> callDataFilters;
    
    
    /**
     * Basic constructor allowing to provide one {@code CallData} filter.
     * 
     * @param callDataFilter    A {@code CallData} to configure the filtering 
     *                          based on the expression data generation (for instance, 
     *                          minimum quality level for each data type, or type of propagation allowed, 
     *                          e.g., propagation of expression calls from substructures).
     * @see #CallFilter(GeneFilter, Set, Set)
     */
    public CallFilter(T callDataFilter) {
        this(null, null, null, new HashSet<T>(Arrays.asList(callDataFilter)));
    }
    /**
     * Constructor accepting all requested parameters to build a new {@code CallFilter}. 
     * {@code geneFilter} and {@code conditionFilters} can be {@code null} or empty, 
     * but {@code callDataFilters} cannot, otherwise an {@code IllegalArgumentException} is thrown. 
     * Indeed, at least one  {@code CallType} should be targeted through at least one {@code CallData}.
     * <p>
     * If the method {@link CallData#getDataType()} returns {@code null} for a {@code CallData}, 
     * then it means that it targets any {@code DataType}, otherwise, it means that it targets only 
     * that specific {@code DataType}. It is not possible to provide several {@code ExpressionCallData}s
     * targeting the same combination of {@code CallType} (see {@link CallData#getCallType()}) 
     * and {@code DataType} (see {@link CallData#getDataType()}), or targeting 
     * for a same {@code CallType} both a {@code null} {@code DataType} and a non-null {@code DataType};
     * for {@code DiffExpressionCallData}, it is similarly not possible to target a redundant combination 
     * of {@code CallType}, {@code DataType}, and {@code DiffExpressionFactor} (see 
     * {@link DiffExpressionCallData#getDiffExpressionFactor()}); otherwise, 
     * an {@code IllegalArgumentException} is thrown. 
     * <p>
     * Note that the {@code DataPropagation}s provided in {@code callDataFilters} 
     * are <strong>not</strong> considered. This is because this information cannot be inferred 
     * for each data type individually from one single query. This information is provided 
     * at the level of a {@code Call}, and so is the filtering allowed. 
     * 
     * @param geneFilter            A {@code GeneFilter} to configure gene-related filtering.
     * @param conditionFilters      A {@code Collection} of {@code ConditionFilter}s to configure 
     *                              the filtering of conditions with expression data. If several 
     *                              {@code ConditionFilter}s are provided, they are seen as "OR" conditions.
     * @param dataPropagationFilter A {@code DataPropagation}s, allowing to configure the origin 
     *                              of the data related to data propagation. Allows for instance 
     *                              to request that data returned includes expression 
     *                              from substructures, or to request that all data propagated 
     *                              also includes observed data. Note that the {@code DataPropagation}s 
     *                              provided in {@code callDataFilters} are <strong>not</strong> considered.
     * @param callDataFilters       A {@code Collection} of {@code CallData}s to configure the filtering 
     *                              based on the expression data generation (for instance, 
     *                              minimum quality level for each data type). If several 
     *                              {@code CallData}s are provided, they are seen as "OR" conditions.
     *                              Note that the {@code DataPropagation}s provided through 
     *                              these {@code CallData} objects are <strong>not</strong> considered.
     * @throws IllegalArgumentException If {@code callDataFilters} is {@code null} or empty, 
     *                                  or contains a {@code null} {@code CallData}; 
     *                                  or if the {@code ExpressionCallData}s provided target 
     *                                  a redundant combination of {@code CallType} and {@code DataType}; 
     *                                  or if the {@code DiffExpressionCallData}s provided target 
     *                                  a redundant combination of {@code CallType}, {@code DataType}, 
     *                                  and {@code DiffExpressionFactor}.
     */
    public CallFilter(GeneFilter geneFilter, Collection<ConditionFilter> conditionFilters, 
            DataPropagation dataPropagationFilter, Collection<T> callDataFilters) throws IllegalArgumentException {
        if (callDataFilters == null || callDataFilters.isEmpty() || callDataFilters.contains(null)) {
            throw log.throwing(new IllegalArgumentException(
                    "At least one CallData filter must be provided, and none can be null."));
        }
        //Check for redundant combinations of CallType/DataType.
        //Use forEach to be able to access the redundant CallDatas for the Exception message
        callDataFilters.stream().forEach(e1 -> callDataFilters.stream().forEach(e2 -> {
            if (!e1.equals(e2) && e1.getCallType().equals(e2.getCallType()) && 
                    (e1.getDataType() == null || e2.getDataType() == null || 
                    e1.getDataType().equals(e2.getDataType()) && 
                    //if they are of type ExpressionCallData, then they target redundant CallType/DataType
                    (e1 instanceof ExpressionCallData && e2 instanceof ExpressionCallData || 
                    //if they are of type DiffExpressionCallData, they will be considered redundant 
                    //only if they also target the same DiffExpressionFactor
                     e1 instanceof DiffExpressionCallData && e2 instanceof DiffExpressionCallData && 
                     ((DiffExpressionCallData) e1).getDiffExpressionFactor().equals(
                             ((DiffExpressionCallData) e2).getDiffExpressionFactor())))) {
                throw log.throwing(new IllegalArgumentException(
                        "The provided CallDatas target a redundant combination of CallType and DataType: "
                                + e1 + " - " + e2));
            }
        }));
        
        //check for compatibility of DataPropagation with the CallData
        if (dataPropagationFilter != null) {
            callDataFilters.stream().forEach(callData -> callData.getCallType()
                .checkDataPropagation(dataPropagationFilter));
        }
        
        this.geneFilter = geneFilter;
        this.conditionFilters = Collections.unmodifiableSet(
                conditionFilters == null? new HashSet<>(): new HashSet<>(conditionFilters));
        this.dataPropagationFilter = dataPropagationFilter == null? new DataPropagation(): dataPropagationFilter;
        this.callDataFilters = Collections.unmodifiableSet(
                callDataFilters == null? new HashSet<>(): new HashSet<>(callDataFilters));
    }

    
    /**
     * @return  The {@code GeneFilter} allowing to configure gene-related filtering.
     */
    public GeneFilter getGeneFilter() {
        return geneFilter;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code ConditionFilter}s, allowing to configure 
     *          the filtering of conditions with expression data. If several 
     *          {@code ConditionFilter}s are configured, they are seen as "OR" conditions.
     */
    public Set<ConditionFilter> getConditionFilters() {
        return conditionFilters;
    }
    /**
     * @return  A {@code DataPropagation}s, allowing to configure the origin of the data 
     *          related to data propagation. Allows for instance to request that data returned 
     *          includes expression from substructures, or to request that all data propagated 
     *          also includes observed data. Not that the {@code DataPropagation}s 
     *          provided in the {@code CallData} objects of this {@code CallFilter} are 
     *          <strong>not</strong> considered. 
     */
    public DataPropagation getDataPropagationFilter() {
        return dataPropagationFilter;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code T}s, allowing to configure the filtering based on 
     *          the expression data generation (for instance, minimum quality level 
     *          for each data type, or type of propagation allowed, e.g., propagation 
     *          of expression calls from substructures). If several {@code CallData}s 
     *          are configured, they are seen as "OR" conditions. Note that the {@code DataPropagation}s 
     *          provided through these {@code CallData} objects are <strong>not</strong> considered.
     */
    public Set<T> getCallDataFilters() {
        return callDataFilters;
    }
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callDataFilters == null) ? 0 : callDataFilters.hashCode());
        result = prime * result + ((conditionFilters == null) ? 0 : conditionFilters.hashCode());
        result = prime * result + ((geneFilter == null) ? 0 : geneFilter.hashCode());
        result = prime * result + ((dataPropagationFilter == null) ? 0 : dataPropagationFilter.hashCode());
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
        CallFilter<?> other = (CallFilter<?>) obj;
        if (callDataFilters == null) {
            if (other.callDataFilters != null) {
                return false;
            }
        } else if (!callDataFilters.equals(other.callDataFilters)) {
            return false;
        }
        if (conditionFilters == null) {
            if (other.conditionFilters != null) {
                return false;
            }
        } else if (!conditionFilters.equals(other.conditionFilters)) {
            return false;
        }
        if (geneFilter == null) {
            if (other.geneFilter != null) {
                return false;
            }
        } else if (!geneFilter.equals(other.geneFilter)) {
            return false;
        }
        if (dataPropagationFilter == null) {
            if (other.dataPropagationFilter != null) {
                return false;
            }
        } else if (!dataPropagationFilter.equals(other.dataPropagationFilter)) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        return "CallFilter [geneFilter=" + geneFilter 
                + ", conditionFilters=" + conditionFilters 
                + ", dataPropagationFilter=" + dataPropagationFilter
                + ", callDataFilters=" + callDataFilters + "]";
    }
}
