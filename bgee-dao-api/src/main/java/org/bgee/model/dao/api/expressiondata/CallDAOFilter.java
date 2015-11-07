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
     * Constructor accepting all requested parameters. In the provided {@code U}s, 
     * only the following methods are considered (if available for this type of {@code U}): 
     * <ul>
     * <li>{@code getAffymetrixData}, {@code getESTData}, {@code getInSituData}, {@code getRNASeqData}, 
     * and {@code getRelaxedInSituData}, to define the minimum quality level for each data type. 
     * if equal to {@code null} or {@code DataState.NODATA}, then no filtering is performed 
     * based on this data type. All parameters in a same {@code U} are considered as AND conditions. 
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
     * Several {@code U}s are seen as OR conditions, and different parameters in a same {@code U} 
     * are seen as AND conditions. Notably, each quality associated to a data type 
     * in a same {@code CallTO} is considered as an AND condition (for instance, 
     * "affymetrixData >= HIGH_QUALITY AND rnaSeqData >= HIGH_QUALITY"). To configure OR conditions, 
     * (for instance, "affymetrixData >= HIGH_QUALITY OR rnaSeqData >= HIGH_QUALITY"), 
     * several {@code CallTO}s must be provided to this {@code CallDAOFilter}. 
     * So for instance, if the quality of all data types in a {@code CallTO} are set to 
     * {@code LOW_QUALITY}, it will only allow to retrieve calls with data in all data types. 
     * 
     * @param geneIds           A {@code Collection} of {@code String}s that are IDs of genes 
     *                          to filter expression queries. Can be {@code null} or empty.
     * @param speciesIds        A {@code Collection} of {@code String}s that are IDs of species 
     *                          to filter expression queries. Can be {@code null} or empty.
     * @param conditionFilters  A {@code Collection} of {@code ConditionFilter}s to configure 
     *                          the filtering of conditions with expression data. If several 
     *                          {@code ConditionFilter}s are provided, they are seen as "OR" conditions.
     *                          Can be {@code null} or empty.
     * @param callTOFilters     A {@code Collection} of {@code CallTO}s of type {@code U} allowing 
     *                          to configure the minimum quality level for each data type, etc. If several 
     *                          {@code U}s are provided, they are seen as "OR" conditions.
     *                          Can be {@code null} or empty.
     * @param attributeType     The class type of the {@code Attribute}s of type {@code T}.
     */
    public CallDAOFilter(Collection<String> geneIds, Collection<String> speciesIds, 
            Collection<DAOConditionFilter> conditionFilters, Collection<U> callTOFilters, 
            Class<T> attributeType) throws IllegalArgumentException {
        log.entry(geneIds, speciesIds, conditionFilters, callTOFilters, attributeType);
        
        if (attributeType == null) {
            throw log.throwing(new IllegalArgumentException("The Attribute class type must be provided."));
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
     *          Provided as a {@code LinkedHashSet} for convenience, to consistently set parameters 
     *          in queries.
     */
    public LinkedHashSet<DAOConditionFilter> getConditionFilters() {
        //defensive copying
        return new LinkedHashSet<>(conditionFilters);
    }
    /**
     * @return  A {@code LinkedHashSet} of {@code CallTO}s of type {@code U} allowing to configure 
     *          the minimum quality level for each data type, etc. If several 
     *          {@code U}s are provided, they are seen as "OR" conditions.
     *          Provided as a {@code LinkedHashSet} for convenience, to consistently set parameters 
     *          in queries.
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
     * in a filtering of the data. For instance, if all data qualities are {@code null},  
     * then it is equivalent to requesting no filtering at all, and the {@code EnumMap} returned 
     * by this method will be empty. 
     * <p>
     * Each quality associated to a data type in a same {@code CallTO} is considered 
     * as an AND condition (for instance, "affymetrixData >= HIGH_QUALITY AND 
     * rnaSeqData >= HIGH_QUALITY"). To configure OR conditions, (for instance, 
     * "affymetrixData >= HIGH_QUALITY OR rnaSeqData >= HIGH_QUALITY"), several {@code CallTO}s 
     * must be provided to this {@code CallDAOFilter}. So for instance, if the quality 
     * of all data types of {@code callTO} are set to {@code LOW_QUALITY}, it will only allow 
     * to retrieve calls with data in all data types. 
     *  
     * @param callTO    A {@code CallTO} to extract filtering data types from.
     * @return          An {@code EnumMap} where keys are {@code Attribute}s associated to a data type, 
     *                  the associated value being a {@code DataState} to be used 
     *                  to parameterize queries to the data source (results should have 
     *                  a data state equal to or higher than this value for this data type).
     *                  Returned as an {@code EnumMap} for consistent iteration order 
     *                  when setting parameters in a query. 
     */
    public EnumMap<T, DataState> extractFilteringDataTypes(U callTO) {
        log.entry(callTO);
        
        final Map<T, DataState> typesToStates = callTO.extractDataTypesToDataStates();
        
        Set<DataState> states = new HashSet<>(typesToStates.values());
        //if we only have null and/or DataState.NODATA values, 
        //it is equivalent to having no filtering for data types.
        if ((states.size() == 1 && 
                (states.contains(null) || states.contains(DataState.NODATA))) || 
            (states.size() == 2 && states.contains(null) && 
            states.contains(DataState.NODATA))) {
            
            return log.exit(new EnumMap<>(this.attributeType));
        }
        
        //otherwise, get the data types with a filtering requested
        return log.exit(typesToStates.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue() != DataState.NODATA)
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue(), 
                        (k, v) -> {throw log.throwing(
                                new IllegalArgumentException("Key used more than once: " + k));}, 
                        //Cannot write EnumMap<>, Eclipse manages to infer the correct type, but not javac. 
                        () -> new EnumMap<T, DataState>(this.attributeType))));
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
